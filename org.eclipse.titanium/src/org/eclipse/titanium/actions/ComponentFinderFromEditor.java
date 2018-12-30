package org.eclipse.titanium.actions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.ILocateableNode;
import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.AST.PortReference;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Function;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Port;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Testcase;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Definition;
import org.eclipse.titan.designer.AST.TTCN3.statements.Connect_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Function_Instance_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Map_Statement;
import org.eclipse.titan.designer.AST.TTCN3.types.Component_Type;
import org.eclipse.titan.designer.editors.ttcn3editor.TTCN3Editor;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.GlobalParser;
import org.eclipse.titan.designer.parsers.ProjectSourceParser;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.PlatformUI;

/**
 * This class collects the components that are used during the execution
 * of the selected testcase.
 *
 * @author Bianka Bekefi
 */
public class ComponentFinderFromEditor extends AbstractHandler {

	public ComponentFinderFromEditor() {
	}


	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		final Definition selection = findSelection();
		if (selection instanceof Def_Testcase) {
			final Def_Testcase tc = (Def_Testcase)selection;
			final TestcaseVisitor vis = new TestcaseVisitor(new ArrayList<Def_Function>());
			tc.accept(vis);
			System.out.println("Eredmeny: ---------------------------------------------------------");
			for (final Component_Type ct : vis.getComponents()) {
				System.out.println(ct.getFullName());
			}
		}

		return null;
	}


	private Definition findSelection() {
		//getting the active editor
		final IEditorPart editor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		if (editor == null || !(editor instanceof TTCN3Editor)) {
			return null;
		}

		final TTCN3Editor targetEditor = (TTCN3Editor) editor;

		//iterating through part of the module
		final IResource selectedRes = extractResource(targetEditor);
		if (!(selectedRes instanceof IFile)) {
			ErrorReporter.logError("SelectionFinder.findSelection(): Selected resource `" + selectedRes.getName() + "' is not a file.");
			return null;
		}

		final IFile selectedFile = (IFile)selectedRes;
		final IProject sourceProj = selectedFile.getProject();
		final ProjectSourceParser projectSourceParser = GlobalParser.getProjectSourceParser(sourceProj);
		final Module selectedModule = projectSourceParser.containedModule(selectedFile);

		//getting current selection
		final ISelectionService selectionService = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService();
		final TextSelection textSelection = extractSelection(selectionService.getSelection());
		//getting current selection nodes
		final int selectionOffset = textSelection.getOffset() + textSelection.getLength();
		final SelectionFinderVisitor selVisitor = new SelectionFinderVisitor(selectionOffset);
		selectedModule.accept(selVisitor);
		final Definition selectedDef = selVisitor.getSelection();
		if (selectedDef == null) {
			ErrorReporter.logWarning("SelectionFinder.findSelection(): Visitor did not find a definition in the selection.");
			final IStatusLineManager statusLineManager = targetEditor.getEditorSite().getActionBars().getStatusLineManager();
			statusLineManager.setErrorMessage("No definition selected");
			return null;
		}
		return selectedDef;
	}

	private static class SelectionFinderVisitor extends ASTVisitor {

		private Definition def;
		private final int offset;

		SelectionFinderVisitor(final int selectionOffset) {
			offset = selectionOffset;
		}

		private Definition getSelection() {
			return def;
		}

		@Override
		public int visit(final IVisitableNode node) {
			if (!(node instanceof ILocateableNode)) {
				return V_CONTINUE;
			}
			final Location loc = ((ILocateableNode) node).getLocation();
			if (loc == null) {
				return V_ABORT;
			}
			if (!loc.containsOffset(offset)) {
				return V_SKIP;
			}
			if (node instanceof Definition) {
				def = (Definition)node;
			}
			return V_CONTINUE;
		}
	}


	private IResource extractResource(final IEditorPart editor) {
		final IEditorInput input = editor.getEditorInput();
		if (!(input instanceof IFileEditorInput)) {
			return null;
		}
		return ((IFileEditorInput)input).getFile();
	}

	private TextSelection extractSelection(final ISelection sel) {
		if (!(sel instanceof TextSelection)) {
			ErrorReporter.logError("Selection is not a TextSelection.");
			return null;
		}
		return (TextSelection)sel;
	}


	private static class TestcaseVisitor extends ASTVisitor {

		private List<Component_Type> comps = new ArrayList<Component_Type>();
		private List<Def_Function> checkedFunctions;
		private int counter;

		TestcaseVisitor(final List<Def_Function> checkedFunctions) {
			comps = new ArrayList<Component_Type>();
			this.checkedFunctions = checkedFunctions;
			counter = -1;
		}

		private List<Component_Type> getComponents() {
			return comps;
		}

		@Override
		public int visit(final IVisitableNode node) {
			if (node instanceof Connect_Statement) {
				counter = 0;
			}
			else if (node instanceof Map_Statement) {
				counter = 0;
			}
			else if (node instanceof PortReference && (counter == 0 || counter == 1)) {
				counter++;
				final PortReference pr = ((PortReference)node);

				final Assignment as = pr.getRefdAssignment(CompilationTimeStamp.getBaseTimestamp(), false);
				if (as != null && as instanceof Def_Port) {
					final Def_Port dp = (Def_Port)as;
					final ModuleVisitor mv = new ModuleVisitor(dp);
					final Module m = dp.getMyScope().getModuleScope();
					m.accept(mv);
					for (final Component_Type ct : mv.getComponents()) {
						if (!comps.contains(ct)) {
							comps.add(ct);
						}
					}
				}
			}
			else if (node instanceof Function_Instance_Statement) {
				final Function_Instance_Statement fis = (Function_Instance_Statement)node;
				final Assignment as = fis.getReference().getRefdAssignment(CompilationTimeStamp.getBaseTimestamp(), true);
				if (as != null && as instanceof Def_Function) {
					final Def_Function df = (Def_Function)as;
					if (!checkedFunctions.contains(df)) {
						checkedFunctions.add(df);
						final TestcaseVisitor tv = new TestcaseVisitor(checkedFunctions);
						df.accept(tv);
						for (final Component_Type ct : tv.getComponents()) {
							if (!comps.contains(ct)) {
								comps.add(ct);
							}
						}

					}
				}
			}
			return V_CONTINUE;
		}

	}

	private static class ModuleVisitor extends ASTVisitor {

		private List<Component_Type> comps = new ArrayList<Component_Type>();
		private Def_Port port;

		ModuleVisitor(final Def_Port port) {
			comps = new ArrayList<Component_Type>();
			this.port = port;
		}

		private List<Component_Type> getComponents() {
			return comps;
		}

		@Override
		public int visit(final IVisitableNode node) {
			if (node instanceof Component_Type) {
				final Component_Type ct = (Component_Type)node;
				final List<Definition> defs = ct.getComponentBody().getDefinitions();
				for (final Definition def : defs) {
					if (def != null && def.equals(port)) {
						comps.add(ct);
						return V_ABORT;
					}
				}
			}
			return V_CONTINUE;
		}

	}

}
