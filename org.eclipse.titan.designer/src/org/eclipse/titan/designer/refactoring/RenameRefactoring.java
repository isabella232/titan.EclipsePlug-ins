/******************************************************************************
 * Copyright (c) 2000-2021 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.refactoring;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.GeneralConstants;
import org.eclipse.titan.designer.AST.ASTLocationChainVisitor;
import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.FieldSubReference;
import org.eclipse.titan.designer.AST.ISubReference;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.Identifier.Identifier_type;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.AST.NamedBridgeScope;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.SubScopeVisitor;
import org.eclipse.titan.designer.AST.ASN1.definitions.ASN1Module;
import org.eclipse.titan.designer.AST.ASN1.types.ASN1_Choice_Type;
import org.eclipse.titan.designer.AST.ASN1.types.ASN1_Enumerated_Type;
import org.eclipse.titan.designer.AST.ASN1.types.ASN1_Sequence_Type;
import org.eclipse.titan.designer.AST.ASN1.types.ASN1_Set_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.TTCN3_Enumerated_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.TTCN3_Set_Seq_Choice_BaseType;
import org.eclipse.titan.designer.commonFilters.ResourceExclusionHelper;
import org.eclipse.titan.designer.consoles.TITANDebugConsole;
import org.eclipse.titan.designer.core.TITANNature;
import org.eclipse.titan.designer.declarationsearch.Declaration;
import org.eclipse.titan.designer.declarationsearch.IdentifierFinderVisitor;
import org.eclipse.titan.designer.editors.IEditorWithCarretOffset;
import org.eclipse.titan.designer.graphics.ImageCache;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.GlobalParser;
import org.eclipse.titan.designer.parsers.ProjectSourceParser;
import org.eclipse.titan.designer.preferences.PreferenceConstants;
import org.eclipse.titan.designer.productUtilities.ProductConstants;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.progress.IProgressConstants;

/**
 * FIXME component variables are not handled correctly
 * @author Adam Delic
 * */
public class RenameRefactoring extends Refactoring {
	public static final String FILENOTIDENTIFIABLE = "The file related to the editor could not be identified";
	public static final String NORECOGNISABLEMODULENAME = "The name of the module in the file `{0}'' could not be identified";
	public static final String EXCLUDEDFROMBUILD = "The name of the module in the file `{0}'' could not be identified, the file is excluded from build";
	public static final String NOTFOUNDMODULE = "The module in file `{0}'' could not be found";
	public static final String PROJECTCONTAINSERRORS = "The project `{0}'' contains errors, which might corrupt the result of the refactoring";
	public static final String PROJECTCONTAINSTTCNPPFILES = "The project `{0}'' contains .ttcnpp files, which might corrupt the result of the refactoring";
	public static final String FIELDALREADYEXISTS = "Field with name `{0}'' already exists in type `{1}''";
	public static final String DEFINITIONALREADYEXISTS = "Name conflict:"
			+ " definition with name `{0}'' already exists in the scope of the selected definition or in one of its parent scopes";
	public static final String DEFINITIONALREADYEXISTS2 = "Name conflict:"
			+ " definition with name `{0}'' already exists in module `{1}'' at line {2}";

	private static final String ONTHEFLYANALAYSISDISABLED = "On-the-fly analysis is disabled,"
			+ " there is no reliable semantic information present for the refactoring to work on";

	private final IFile file;
	private final Module module;
	private Map<Module, List<Hit>> idsMap = null;
	// found identifiers will be renamed to this
	private String newIdentifierName;
	private ReferenceFinder referenceFinder;

	public RenameRefactoring(final IFile file, final Module module, final ReferenceFinder referenceFinder) {
		super();
		this.file = file;
		this.module = module;
		this.referenceFinder = referenceFinder;
	}

	@Override
	public String getName() {
		return "Rename " + referenceFinder.getSearchName();
	}

	public Module getModule() {
		return module;
	}

	public Identifier getRefdIdentifier() {
		return referenceFinder.getReferredIdentifier();
	}

	public void setNewIdentifierName(final String newIdentifierName) {
		this.newIdentifierName = newIdentifierName;
	}

	@Override
	public RefactoringStatus checkInitialConditions(final IProgressMonitor monitor) throws CoreException {

		// for debugging
		// ScopeHierarchyVisitor v = new ScopeHierarchyVisitor();
		// module.accept(v);
		// TITANDebugConsole.getConsole().newMessageStream().println(v.getScopeTreeAsHTMLPage());

		final RefactoringStatus result = new RefactoringStatus();
		try {
			monitor.beginTask("Checking preconditions...", 2);

			final IPreferencesService prefs = Platform.getPreferencesService();//PreferenceConstants.USEONTHEFLYPARSING
			if (! prefs.getBoolean(ProductConstants.PRODUCT_ID_DESIGNER, PreferenceConstants.USEONTHEFLYPARSING, false, null)) {
				result.addFatalError(ONTHEFLYANALAYSISDISABLED);
			}

			// check that there are no ttcnpp files in the project
			if (GlobalParser.hasTtcnppFiles(file.getProject())) {//FIXME actually all referencing and referenced projects need to be checked too !
				result.addError(MessageFormat.format(PROJECTCONTAINSTTCNPPFILES, file.getProject()));
			}
			monitor.worked(1);
			
			//Check that there are no syntactic, semantic or mixed error markers in the project. Compilation error does not matter
			final IProject project = file.getProject();
			if (projectHasOnTheFlyError (project)) {
				result.addError(MessageFormat.format(PROJECTCONTAINSERRORS, project));
			}
			monitor.worked(1);
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace(e);
			result.addFatalError(e.getMessage());
		} finally {
			monitor.done();
		}
		return result;
	}
	
	// Returns true if the project has on-the-fly error (syntactic, semantic or mixed error)
	private boolean projectHasOnTheFlyError(final IProject project) throws CoreException {
		final String[] onTheFlyMarkerTypes = {
				GeneralConstants.ONTHEFLY_SYNTACTIC_MARKER,
				GeneralConstants.ONTHEFLY_SEMANTIC_MARKER,
				GeneralConstants.ONTHEFLY_MIXED_MARKER
		};
		for(final String markerType : onTheFlyMarkerTypes){
			final IMarker[] markers = project.findMarkers(markerType, true, IResource.DEPTH_INFINITE);
			for (final IMarker marker : markers) {
				if (IMarker.SEVERITY_ERROR == marker.getAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR)) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public RefactoringStatus checkFinalConditions(final IProgressMonitor monitor) throws CoreException {
		final RefactoringStatus result = new RefactoringStatus();
		final boolean reportDebugInformation = Platform.getPreferencesService().getBoolean(ProductConstants.PRODUCT_ID_DESIGNER,
				PreferenceConstants.DISPLAYDEBUGINFORMATION, true, null);
		// search
		idsMap = referenceFinder.findAllReferences(module, file.getProject(), monitor, reportDebugInformation);
		// add the referred identifier to the map of found identifiers
		final Identifier refdIdentifier = referenceFinder.getReferredIdentifier();
		final Module refdModule = referenceFinder.assignment.getMyScope().getModuleScope();
		if (idsMap.containsKey(refdModule)) {
			idsMap.get(refdModule).add(new Hit(refdIdentifier));
		} else {
			final ArrayList<Hit> identifierList = new ArrayList<Hit>();
			identifierList.add(new Hit(refdIdentifier));
			idsMap.put(refdModule, identifierList);
		}

		// check if there are name collisions in any of the affected
		// scopes
		if (referenceFinder.fieldId == null) {
			// check that in all affected scopes there is no
			// definition with the new name
			Identifier.Identifier_type idType;
			if (referenceFinder.scope.getModuleScope() instanceof ASN1Module) {
				idType = Identifier_type.ID_ASN;
			} else {
				idType = Identifier_type.ID_TTCN;
			}

			final Identifier newId = new Identifier(idType, newIdentifierName);
			// check for assignment with given id in all sub-scopes
			// of the assignment's scope
			// TODO: this does not detect runs on <-> component
			// member conflicts because the RunsOnScope is not a
			// sub-scope of the ComponentTypeBody scope,
			// also it does not go into other modules
			Scope rootScope = referenceFinder.assignment.getMyScope();
			if (rootScope instanceof NamedBridgeScope && rootScope.getParentScope() != null) {
				rootScope = rootScope.getParentScope();
			}

			final SubScopeVisitor subScopeVisitor = new SubScopeVisitor(rootScope);
			module.accept(subScopeVisitor);
			final List<Scope> subScopes = subScopeVisitor.getSubScopes();
			subScopes.add(rootScope);
			for (final Scope ss : subScopes) {
				if (ss.hasAssignmentWithId(CompilationTimeStamp.getBaseTimestamp(), newId)) {
					final List<ISubReference> subReferences = new ArrayList<ISubReference>();
					subReferences.add(new FieldSubReference(newId));
					final Reference reference = new Reference(null, subReferences);
					final Assignment assignment = ss.getAssBySRef(CompilationTimeStamp.getBaseTimestamp(), reference);
					if (assignment != null && assignment.getLocation() != null) {
						result.addError(MessageFormat.format(DEFINITIONALREADYEXISTS2, newId.getDisplayName(),
								module.getName(), assignment.getLocation().getLine()));
					} else {
						result.addError(MessageFormat.format(DEFINITIONALREADYEXISTS, newId.getDisplayName()));
					}
					// to avoid spam and multiple messages for the same conflict
					return result;
				}
			}
		} else {
			boolean alreadyExists;
			// check if the type has already a field with the new
			// name
			if (referenceFinder.type instanceof TTCN3_Set_Seq_Choice_BaseType) {
				alreadyExists = ((TTCN3_Set_Seq_Choice_BaseType) referenceFinder.type).hasComponentWithName(newIdentifierName);
			} else if (referenceFinder.type instanceof TTCN3_Enumerated_Type) {
				alreadyExists = ((TTCN3_Enumerated_Type) referenceFinder.type).hasEnumItemWithName(new Identifier(Identifier_type.ID_TTCN,
						newIdentifierName));
			} else if (referenceFinder.type instanceof ASN1_Choice_Type) {
				alreadyExists = ((ASN1_Choice_Type) referenceFinder.type).hasComponentWithName(new Identifier(Identifier_type.ID_ASN,
						newIdentifierName));
			} else if (referenceFinder.type instanceof ASN1_Enumerated_Type) {
				alreadyExists = ((ASN1_Enumerated_Type) referenceFinder.type).hasEnumItemWithName(new Identifier(Identifier_type.ID_ASN,
						newIdentifierName));
			} else if (referenceFinder.type instanceof ASN1_Sequence_Type) {
				alreadyExists = ((ASN1_Sequence_Type) referenceFinder.type).hasComponentWithName(new Identifier(Identifier_type.ID_ASN,
						newIdentifierName));
			} else if (referenceFinder.type instanceof ASN1_Set_Type) {
				alreadyExists = ((ASN1_Set_Type) referenceFinder.type).hasComponentWithName(new Identifier(Identifier_type.ID_ASN,
						newIdentifierName));
			} else {
				alreadyExists = false;
			}
			if (alreadyExists) {
				result.addError(MessageFormat.format(FIELDALREADYEXISTS, newIdentifierName, referenceFinder.type.getTypename()));
			}
		}

		return result;
	}

	@Override
	public Change createChange(final IProgressMonitor monitor) throws CoreException {
		final CompositeChange result = new CompositeChange(getName());
		// add the change of all found identifiers grouped by module
		final boolean isAsnRename = module.getModuletype() == Module.module_type.ASN_MODULE;
		String newTtcnIdentifierName;
		if (isAsnRename) {
			newTtcnIdentifierName = Identifier.getTtcnNameFromAsnName(newIdentifierName);
		} else {
			newTtcnIdentifierName = newIdentifierName;
		}

		final List<IFile> filesToProcess = new ArrayList<IFile>(idsMap.size());

		for (final Module m : idsMap.keySet()) {
			final List<Hit> hitList = idsMap.get(m);
			final boolean isTtcnModule = m.getModuletype() == Module.module_type.TTCN3_MODULE;
			final IFile file = (IFile) hitList.get(0).identifier.getLocation().getFile();
			final TextFileChange tfc = new TextFileChange(file.getName(), file);
			result.add(tfc);
			final MultiTextEdit rootEdit = new MultiTextEdit();
			tfc.setEdit(rootEdit);
			for (final Hit hit : hitList) {
				final int offset = hit.identifier.getLocation().getOffset();
				final int length = hit.identifier.getLocation().getEndOffset() - offset;
				String newName = isTtcnModule ? newTtcnIdentifierName : newIdentifierName;
				// special case: referencing the definition from
				// another module without a module name prefix
				// and there is a definition with the new name
				// in the scope of the reference.
				// The module name must be added to the
				// reference.
				if (referenceFinder.fieldId == null
						&& hit.reference != null
						&& hit.reference.getModuleIdentifier() == null
						&& referenceFinder.assignment.getMyScope().getModuleScope() != hit.reference.getMyScope().getModuleScope()
						&& hit.reference.getMyScope().hasAssignmentWithId(
								CompilationTimeStamp.getBaseTimestamp(),
								new Identifier(isTtcnModule ? Identifier_type.ID_TTCN : Identifier_type.ID_ASN,
										newIdentifierName))) {
					newName = referenceFinder.assignment.getMyScope().getModuleScope().getName() + "." + newName;
				}
				rootEdit.addChild(new ReplaceEdit(offset, length, newName));
			}

			filesToProcess.add((IFile) m.getLocation().getFile());
		}

		return result;
	}

	/**
	 * Helper function used by RenameRefactoringAction classes for TTCN-3,
	 * ASN.1 and TTCNPP editors
	 * 
	 * @param targetEditor
	 *                the editor the user is working in.
	 * @param selection
	 *                the selection in the editor.
	 */
	public static void runAction(final IEditorPart targetEditor, final ISelection selection) {
		final IStatusLineManager statusLineManager = targetEditor.getEditorSite().getActionBars().getStatusLineManager();
		statusLineManager.setErrorMessage(null);

		final IFile file = (IFile) targetEditor.getEditorInput().getAdapter(IFile.class);
		if (file == null) {
			statusLineManager.setErrorMessage(FILENOTIDENTIFIABLE);
			return;
		}

		if (!TITANNature.hasTITANNature(file.getProject())) {
			statusLineManager.setErrorMessage(TITANNature.NO_TITAN_FILE_NATURE_FOUND);
			return;
		}

		final IPreferencesService prefs = Platform.getPreferencesService();
		final boolean reportDebugInformation = prefs.getBoolean(ProductConstants.PRODUCT_ID_DESIGNER,
				PreferenceConstants.DISPLAYDEBUGINFORMATION, true, null);

		int offset;
		if (selection instanceof TextSelection && !selection.isEmpty() && !"".equals(((TextSelection) selection).getText())) {
			if (reportDebugInformation) {
				TITANDebugConsole.println("text selected: " + ((TextSelection) selection).getText());
			}
			final TextSelection tSelection = (TextSelection) selection;
			offset = tSelection.getOffset() + tSelection.getLength();
		} else {
			offset = ((IEditorWithCarretOffset) targetEditor).getCarretOffset();
		}

		// run semantic analysis to have up-to-date AST
		// FIXME: it does not work for incremental parsing
		final ProjectSourceParser projectSourceParser = GlobalParser.getProjectSourceParser(file.getProject());
		final WorkspaceJob job = projectSourceParser.analyzeAll();
		if (job == null) {
			if (reportDebugInformation) {
				TITANDebugConsole.println("Rename refactoring: WorkspaceJob to analyze project could not be created.");
			}
			return;
		}
		try {
			job.join();
		} catch (InterruptedException e) {
			ErrorReporter.logExceptionStackTrace(e);
			return;
		}

		// find the module
		if (ResourceExclusionHelper.isExcluded(file)) {
			targetEditor.getEditorSite().getActionBars().getStatusLineManager()
			.setErrorMessage(MessageFormat.format(EXCLUDEDFROMBUILD, file.getFullPath()));
			return;
		}

		final Module module = projectSourceParser.containedModule(file);
		if (module == null) {
			statusLineManager.setErrorMessage(MessageFormat.format(NOTFOUNDMODULE, file.getName()));
			return;
		}

		ReferenceFinder referenceFinder = findOccurrencesLocationBased(module, offset);

		if (referenceFinder == null) {
			referenceFinder = new ReferenceFinder();
			final boolean isDetected = referenceFinder.detectAssignmentDataByOffset(module, offset, targetEditor, true, reportDebugInformation, null);
			if (!isDetected) {
				return;
			}
		}

		final RenameRefactoring renameRefactoring = new RenameRefactoring(file, module, referenceFinder);
		final RenameRefactoringWizard renameWizard = new RenameRefactoringWizard(renameRefactoring);
		final RefactoringWizardOpenOperation operation = new RefactoringWizardOpenOperation(renameWizard);
		try {
			operation.run(targetEditor.getEditorSite().getShell(), "");
		} catch (InterruptedException irex) {
			// operation was canceled
			if (reportDebugInformation) {
				TITANDebugConsole.println("Rename refactoring has been cancelled");
			}
		} finally {

			//===================================
			//=== Re-analysis after renaming ====
			//===================================
			final Map<Module, List<Hit>> changed = referenceFinder.findAllReferences(module, file.getProject(), null, reportDebugInformation);

			final Set<Module> modules = new HashSet<Module>();
			modules.add(module);
			modules.addAll(changed.keySet());

			reanalyseAstAfterRefactoring(file.getProject(), modules );
		}

	}

	/**
	 * Re-analyzes AST after a rename-refactoring finished At first reports
	 * outdating for the projects containing file has been refactored. Then
	 * analyzes the project where the reanalysis started from (and its
	 * dependencies)
	 * 
	 * @param project
	 *                The project where the renaming started from
	 * @param modules
	 *                The modules containing renaming
	 */
	public static void reanalyseAstAfterRefactoring(final IProject project, final Set<Module> modules ){
		final ConcurrentLinkedQueue<WorkspaceJob> reportOutdatingJobs = new ConcurrentLinkedQueue<WorkspaceJob>();
		for(final Module tempModule : modules) {
			final IFile file = (IFile)tempModule.getLocation().getFile();
			final WorkspaceJob operation = new WorkspaceJob("Reports outdating for file: " + file.getName()) {
				@Override
				public IStatus runInWorkspace(final IProgressMonitor monitor) {
					IProject proj = file.getProject();
					reportOutdatingJobs.add(GlobalParser.getProjectSourceParser(proj).reportOutdating(file));
					return Status.OK_STATUS;
				}
			};
			operation.setPriority(Job.LONG);
			operation.setSystem(true);
			operation.setUser(false);
			operation.setRule(file); //waiting for f to be released
			operation.setProperty(IProgressConstants.ICON_PROPERTY, ImageCache.getImageDescriptor("titan.gif"));
			reportOutdatingJobs.add(operation);
			operation.schedule();
		}

		//Waits for finishing update then analyzes all projects related to this change
		final WorkspaceJob operation = new WorkspaceJob("Analyzes all projects related to this change") {
			@Override
			public IStatus runInWorkspace(final IProgressMonitor monitor) {
				while (!reportOutdatingJobs.isEmpty()) {
					WorkspaceJob job = reportOutdatingJobs.poll();
					try {
						if (job != null) {
							job.join();
						}
					} catch (InterruptedException e) {
						ErrorReporter.logExceptionStackTrace(e);
					}
				}

				//Now everything is released and reported outdated, so the analysis can start:
				final ProjectSourceParser projectSourceParser = GlobalParser.getProjectSourceParser(project);
				projectSourceParser.analyzeAll();

				return Status.OK_STATUS;
			}
		};
		operation.setPriority(Job.LONG);
		operation.setSystem(true);
		operation.setUser(false);
//		op.setRule(file); //waiting for file to be released << Don't apply, it will wait forever!!!
		operation.setProperty(IProgressConstants.ICON_PROPERTY, ImageCache.getImageDescriptor("titan.gif"));
		operation.schedule();
	}

	/**
	 * Finds the occurrences of the element located on the given offset.
	 * This search is based on the {@link ASTLocationChainVisitor}.
	 *
	 * @param module
	 *                The module to search the occurrences in
	 * @param offset
	 *                An offset in the module
	 * @return The referencefinder
	 */
	protected static ReferenceFinder findOccurrencesLocationBased(final Module module, final int offset) {
		final IdentifierFinderVisitor visitor = new IdentifierFinderVisitor(offset);
		module.accept(visitor);
		//It works for fields as well
		final Declaration def = visitor.getReferencedDeclaration();

		if (def == null || !def.shouldMarkOccurrences()) {
			return null;
		}

		return def.getReferenceFinder(module);
	}

}
