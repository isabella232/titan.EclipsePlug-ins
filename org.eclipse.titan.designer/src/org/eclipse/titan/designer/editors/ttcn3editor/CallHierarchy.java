/******************************************************************************
 * Copyright (c) 2000-2019 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors.ttcn3editor;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.ArrayList;
import java.util.HashSet;
import java.text.MessageFormat;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.Assignment.Assignment_type;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Definition;
import org.eclipse.titan.designer.commonFilters.ResourceExclusionHelper;
import org.eclipse.titan.designer.consoles.TITANDebugConsole;
import org.eclipse.titan.designer.core.TITANNature;
import org.eclipse.titan.designer.editors.IEditorWithCarretOffset;
import org.eclipse.titan.designer.graphics.ImageCache;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.GlobalParser;
import org.eclipse.titan.designer.parsers.ProjectSourceParser;
import org.eclipse.titan.designer.preferences.PreferenceConstants;
import org.eclipse.titan.designer.productUtilities.ProductConstants;

/**
 * This class implement the searching algorithms for the Call Hierarchy View.
 * The class use the {@link CallHierarchyNode} for the Call Hierarchy graph representation.
 * 
 * @see CallHierarchyNode
 * @author Sandor Balazs
 */
public class CallHierarchy {
	/**
	 * Filter set for the {@link #functionCallFinder(ISelection)}
	 * Contains the result types for the {@link ReferenceFinder#detectAssignmentDataByOffset}
	 */
	private final HashSet<Assignment_type> filterAssignmentType;

	private static final String FILENOTIDENTIFIABLE 			= "The file related to the editor could not be identified.";
	private static final String EXCLUDEDFROMBUILD 				= "The name of the module in the file \"{0}\" could not be identified, the file is excluded from build.";
	private static final String NOTFOUNDMODULE 					= "The module in file \"{0}\" could not be found.";
	private static final String PROJECT_NOT_FOUND 				= "The project not fund for this file: \"{0}\".";
	private static final String SELECTED_ASSIGNMENT_NOT_FOUND 	= "The selected object is not a function or testcase or not found.";
	private static final String CALL_HIERARCY_BUILDING 			= "Call hierarchy view building in progress ...";
	private static final String CALL_HIERARCY_BUILDING_COMPLETE = "Call Hierarchy building complete on the \"{0}\".";
	private static final String STATUS_LINE_ERROR_ICON 			= "compiler_error_fresh.gif";
	private static final String STATUS_LINE_MESSAGE_ICON 		= "titan.gif";
	private static final int    STATUS_LINE_LEVEL_MESSAGE 		= 0;
	private static final int    STATUS_LINE_LEVEL_ERROR 		= 1;

	/**
	 * The current editor. <br> Setting in the {@link #initialization()} or in the  {@link #setActiveEditor(IEditorPart)}.
	 */
	private IEditorPart targetEditor = null;

	/**
	 * The current project. <br> Setting in the {@link #initialization()}.
	 */
	private IProject currentProject = null;

	/**
	 * The projectSourceParser. <br> Setting in the {@link #initialization()}.
	 */
	private ProjectSourceParser projectSourceParser = null;

	/**
	 * The selected module. <br> Setting in the {@link #initialization()}.
	 */
	private Module selectedModule = null;

	/**
	 *  The statusLineManager. <br> Setting in the {@link #initialization()} or in the  {@link #setStatusLineManager(IStatusLineManager)}.
	 */
	private IStatusLineManager  statusLineManager	= null;

	/**
	 * The selected CallHierarchyNode. Setting in the {@link #functionCallFinder(ISelection)} and the {@link #setcurrentNode()}.
	 */
	private static CallHierarchyNode currentNode = null;

	/**
	 * Store the search history.
	 */
	private static ArrayList<CallHierarchyNode> searchLog = new ArrayList<CallHierarchyNode>();;

	/**
	 * The search history list allowed long.
	 */
	private final int SEARCH_LOG_HISTORY_LONG = 15;

	/**
	 * Constructor of CallHierarchy.<br>
	 * Set the Assignment filters ({@link #filterAssignmentType}) for the find {@link #functionCallFinder(ISelection)}.
	 */
	public CallHierarchy() {
		filterAssignmentType = new HashSet<Assignment_type>();
		filterAssignmentType.add(Assignment_type.A_FUNCTION);
		filterAssignmentType.add(Assignment_type.A_FUNCTION_RVAL);
		filterAssignmentType.add(Assignment_type.A_FUNCTION_RTEMP);
		filterAssignmentType.add(Assignment_type.A_EXT_FUNCTION);
		filterAssignmentType.add(Assignment_type.A_EXT_FUNCTION_RVAL);
		filterAssignmentType.add(Assignment_type.A_EXT_FUNCTION_RTEMP);
		filterAssignmentType.add(Assignment_type.A_TESTCASE);
		this.initialization();
	}

	/**
	 * Initialization process for the search algorithms.<br>
	 * This method initialize the global variables.<br>
	 * This method run before all searches.<br>
	 * @see #functionCallFinder(ISelection)
	 * @see #functionCallFinder(CallHierarchyNode)
	 * @return
	 * 			Return true when the initialization is success!
	 */
	public boolean initialization() {
		targetEditor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		if (targetEditor == null)  {
			return false;
		}

		if (statusLineManager == null) {
			statusLineManager = targetEditor.getEditorSite().getActionBars().getStatusLineManager();
		}
		if (statusLineManager == null)  {
			return false;
		}
		statusLineManager.setErrorMessage(null);

		final IFile selectedFile = (IFile) targetEditor.getEditorInput().getAdapter(IFile.class);
		if (selectedFile == null) {
			showStatusLineMessage(FILENOTIDENTIFIABLE, STATUS_LINE_LEVEL_ERROR);
			return false;
		}
		if (!TITANNature.hasTITANNature(selectedFile.getProject())) {
			showStatusLineMessage(TITANNature.NO_TITAN_FILE_NATURE_FOUND, STATUS_LINE_LEVEL_ERROR);
			return false;
		}

		currentProject = selectedFile.getProject();
		if (currentProject == null) {
			showStatusLineMessage(MessageFormat.format(PROJECT_NOT_FOUND, selectedFile.getName()), STATUS_LINE_LEVEL_ERROR);
			return false;
		}

		projectSourceParser = GlobalParser.getProjectSourceParser(selectedFile.getProject());
		if (ResourceExclusionHelper.isExcluded(selectedFile)) {
			showStatusLineMessage(MessageFormat.format(EXCLUDEDFROMBUILD, selectedFile.getFullPath()), STATUS_LINE_LEVEL_ERROR);
			return false;
		}

		selectedModule = projectSourceParser.containedModule(selectedFile);
		if (selectedModule == null) {
			showStatusLineMessage(MessageFormat.format(NOTFOUNDMODULE, selectedFile.getName()), STATUS_LINE_LEVEL_ERROR);
			return false;
		}

		return true;
	}

	/**
	 * The <code>functionCallFinder<code> can find an Assignment from a selection and can search this Assignment's references.<br>
	 * The <code>functionCallFinder<code> use the {@link ReferenceFinder}.
	 *
	 * @param selection
	 * 			The searched selection.
	 * @return
	 * 			The <code>functionCallFinder<code> return a {@link CallHierarchyNode} with the selected Definition and the caller functions
	 * 			and the references.
	 * @see ReferenceFinder
	 * @see CallHierarchyNode
	 */
	public CallHierarchyNode functionCallFinder(final ISelection selection) {
		final boolean initializationStatus = initialization();
		if(!initializationStatus)  {
			return null;
		}
		showStatusLineMessage(CALL_HIERARCY_BUILDING);

		final IPreferencesService preferencesService = Platform.getPreferencesService();
		final boolean reportDebugInformation = preferencesService.getBoolean(ProductConstants.PRODUCT_ID_DESIGNER,
				PreferenceConstants.DISPLAYDEBUGINFORMATION, true, null);

		int offset;
		if (selection instanceof TextSelection && !selection.isEmpty() && !"".equals(((TextSelection) selection).getText())) {
			if (reportDebugInformation) TITANDebugConsole.println("text selected: " + ((TextSelection) selection).getText());
			final TextSelection textSelection = (TextSelection) selection;
			offset = textSelection.getOffset() + textSelection.getLength();
		} else {
			offset = ((IEditorWithCarretOffset) targetEditor).getCarretOffset();
		}

		final ReferenceFinder referenceFinder = new ReferenceFinder();
		final boolean isDetected = referenceFinder.detectAssignmentDataByOffset(selectedModule, offset, targetEditor, true,
				reportDebugInformation, filterAssignmentType);

		if (!isDetected) {
			showStatusLineMessage(SELECTED_ASSIGNMENT_NOT_FOUND, STATUS_LINE_LEVEL_ERROR);
			return null;
		}

		final Assignment selectedAssignment = referenceFinder.assignment;
		if(!(selectedAssignment instanceof Definition)) {
			return null;
		}

		final Definition selectedDefinition = (Definition) selectedAssignment;
		final CallHierarchyNode node = new CallHierarchyNode(selectedModule, selectedDefinition);
		final Map<Module, List<Hit>> functionCalls = referenceFinder.findAllReferences(selectedModule, currentProject, null , false);
		for (final Map.Entry<Module, List<Hit>> functionCallsInModule : functionCalls.entrySet()) {
			final Module currentModule = functionCallsInModule.getKey();

			for (final Hit functionCallHit : functionCallsInModule.getValue()) {
				node.addChild(currentModule, functionCallHit.reference);
			}
		}
		setCurrentNode(node);

		showStatusLineMessage(MessageFormat.format(CALL_HIERARCY_BUILDING_COMPLETE, currentNode.getName()));
		return node;
	}

	/**
	 * The <code>functionCallFinder<code> can search an Assignment's references and add them to the {@link CallHierarchyNode}<br>
	 * The <code>functionCallFinder<code> use the {@link FunctionCallVisitor}.
	 *
	 * @param selection
	 * 			The searched selection.
	 * @return
	 * 			The <code>functionCallFinder<code> return a {@link CallHierarchyNode} with the selected Definition and the caller functions
	 * 			and the references.
	 * @see FunctionCallVisitor
	 * @see CallHierarchyNode
	 */
	public CallHierarchyNode functionCallFinder(final CallHierarchyNode node) {
		if(node == null) {
			return node;
		}
		if(node.getNodeDefinition() == null) {
			return node;
		}
		showStatusLineMessage(CALL_HIERARCY_BUILDING);
		final boolean initializationStatus = initialization();
		if(!initializationStatus)  {
			return null;
		}

		final Set<String> modules = projectSourceParser.getKnownModuleNames();
		for (final String moduleName : modules) {
			final Module module = projectSourceParser.getModuleByName(moduleName);
			if(module == null) continue;

			final FunctionCallVisitor functionCallVisitor = new FunctionCallVisitor((Assignment) node.getNodeDefinition());
			module.accept(functionCallVisitor);

			final Set<Reference> setOfCallreferences = functionCallVisitor.getFunctionCalls();
			for (final Reference reference : setOfCallreferences) {
				node.addChild(module, reference);
			}
		}
		showStatusLineMessage(MessageFormat.format(CALL_HIERARCY_BUILDING_COMPLETE, currentNode.getName()));
		return node;
	}

	/**
	 * <p>
	 * The <code>FunctionCallVisitor</code> is a reference searcher visitor.
	 * The visitor collect the target Assigment's references to the setOfReferences.
	 * </p>
	 * @author Sandor Balazs
	 * @see ASTVisitor
	 */
	private class FunctionCallVisitor extends ASTVisitor {
		/**
		 * The search's target Assignment.
		 */
		private final Assignment target;

		/**
		 * The results of the searching.
		 */
		private final Set<Reference> setOfreferences = new HashSet<Reference>();

		/**
		 * The <code>FunctionCallVisitor</code>'s constructor.
		 *
		 * @param target
		 * 			The search's target Assignment.
		 */
		public FunctionCallVisitor(final Assignment target) {
			this.target = target;
			setOfreferences.clear();
		}

		/**
		 * Get the collected references.
		 *
		 * @return
		 * 			The results of the searching.
		 */
		public Set<Reference> getFunctionCalls() {
			return setOfreferences;
		}

		/**
		 * The target matching with the current node.
		 *
		 * @param node
		 * 			The actual visited node.
		 * @see ASTVisitor
		 */
		@Override
		public int visit(final IVisitableNode node) {
			if (!(node instanceof Reference)) {
				return V_CONTINUE;
			}

			final Reference reference = (Reference) node;
			final Assignment referedAssignment = reference.getRefdAssignment(CompilationTimeStamp.getBaseTimestamp(), true);

			if(referedAssignment == null) {
				return V_CONTINUE;
			}

			if(!(referedAssignment instanceof Definition)) {
				return V_CONTINUE;
			}

			if(!(referedAssignment.getFullName().equals(target.getFullName()))) {
				return V_CONTINUE;
			}

			setOfreferences.add(reference);
			return V_CONTINUE;
		}
	}

	/**
	 * Add a new search start selection for the searchLog.
	 * When the pushed element already exist, the method remove it and push it to the top.
	 * The method limit them list length to the SEARCH_LOG_HISTORY_LONG limit.
	 * 
	 * @param name The name of the searched object.
	 * @param selection The new search start point for the log.
	 */
	public void addToSearchLog(final CallHierarchyNode selectedNode) {
		final String selectedNodeName = selectedNode.getName();
		for (int i = 0; i < searchLog.size(); i++) {
			if(searchLog.get(i).getName().equals(selectedNodeName)) {
				searchLog.remove(searchLog.get(i));
				break;
			}
		}
		searchLog.add(selectedNode);
		if(searchLog.size() > SEARCH_LOG_HISTORY_LONG) {
			searchLog.remove(0);
		}
	}

	/**
	 * Getter for the search log.
	 * 
	 * @return The search log.
	 */
	public ArrayList<CallHierarchyNode> getSearchLog() {
		return searchLog;
	}

	/**
	 * Show message on the target editors status bar.<br>
	 * The message level is automatically STATUS_LINE_LEVEL_MESSAGE.
	 *
	 * @see #showStatusLineMessage(String, int)
	 * @param message
	 * 			The string of the message.
	 */
	public void showStatusLineMessage(final String message) {
		showStatusLineMessage(message, STATUS_LINE_LEVEL_MESSAGE);
	}

	/**
	 * Show message on the target editors status bar.<br>
	 * The message level possible ERROR OR MESSAGE. The level define by the level parameter.
	 *
	 * @param message
	 * 			The string of the message.
	 * @param level
	 * 			The level of message.<br>
	 * 			Possible: STATUS_LINE_LEVEL_MESSAGE or STATUS_LINE_LEVEL_ERROR
	 */
	public void showStatusLineMessage(final String message, final int level) {
		if (statusLineManager == null)  {
			return;
		}

		statusLineManager.setErrorMessage(null);

		if(level == STATUS_LINE_LEVEL_MESSAGE) {
			statusLineManager.setMessage(ImageCache.getImage(STATUS_LINE_MESSAGE_ICON), message);
		}

		if(level == STATUS_LINE_LEVEL_ERROR) {
			statusLineManager.setMessage(ImageCache.getImage(STATUS_LINE_ERROR_ICON), message);
		}
	}

	/**
	 * Return the current project.<br>
	 * Setting in the {@link #initialization()}.
	 *
	 * @return
	 * 			The current project.
	 * @see #initialization()
	 */
	public IProject getCurrentProject() {
		return this.currentProject;
	}

	/**
	 * Return the actual selected CallHierarchyNode.<br>
	 * Setting in the {@link #functionCallFinder(ISelection)} and the {@link #setcurrentNode()}.
	 *
	 * @return
	 * 			The selected CallHierarchyNode.
	 * @see #functionCallFinder(ISelection)
	 * @see #setcurrentNode()
	 */
	public CallHierarchyNode getCurrentNode() {
		return currentNode;
	}

	/**
	 * Set the actual selected CallHierarchyNode.<br>
	 * Setting in the {@link #functionCallFinder(ISelection)} and the {@link #setcurrentNode()}.
	 * 
	 * @param currentNode
	 * 			The actual selected CallHierarchyNode.
	 */
	public void setCurrentNode(final CallHierarchyNode newNode) {
		currentNode = newNode;
	}

	/**
	 * Set the used StatusLineManager for change the messages place.<br>
	 * Usage in: {@link #showStatusLineMessage(String, int)}
	 *
	 * @param newStatusLineManager
	 * 			The new StatusLineManager.
	 * @see #initialization()
	 */
	public void setStatusLineManager(final IStatusLineManager newStatusLineManager) {
		this.statusLineManager = newStatusLineManager;
	}
}