/******************************************************************************
 * Copyright (c) 2000-2019 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titanium.actions;

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.ILocateableNode;
import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.AST.PortReference;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Function;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Port;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Testcase;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Definition;
import org.eclipse.titan.designer.AST.TTCN3.statements.Connect_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Function_Instance_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Map_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Start_Component_Statement;
import org.eclipse.titan.designer.AST.TTCN3.types.Component_Type;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.ComponentCreateExpression;
import org.eclipse.titan.designer.consoles.TITANDebugConsole;
import org.eclipse.titan.designer.editors.ttcn3editor.TTCN3Editor;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.GlobalParser;
import org.eclipse.titan.designer.parsers.ProjectSourceParser;
import org.eclipse.titan.designer.properties.data.ProjectBuildPropertyData;
import org.eclipse.titanium.error.ErrorHandler;
import org.eclipse.titanium.error.GUIErrorHandler;
import org.eclipse.titanium.graph.components.EdgeDescriptor;
import org.eclipse.titanium.graph.components.NodeColours;
import org.eclipse.titanium.graph.components.NodeDescriptor;
import org.eclipse.titanium.graph.gui.windows.ComponentFinderGraphEditor;
import org.eclipse.titanium.graph.visualization.BadLayoutException;
import org.eclipse.titanium.graph.visualization.GraphHandler;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.part.FileEditorInput;

import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.util.EdgeType;

/**
 * This class does the generation and show of component finder graph.
 *
 * @author Bianka Bekefi
 */
public class ComponentFinderFromEditor extends AbstractHandler implements IObjectActionDelegate {

	private ISelection selection;

	public ComponentFinderFromEditor() {
	}
	
	@Override
	public void setActivePart(final IAction action, final IWorkbenchPart targetPart) {
		// Do nothing
	}

	@Override
	public void run(final IAction action) {
		doOpenComponentFinderGraphForSelected();
	}

	@Override
	public void selectionChanged(final IAction action, final ISelection selection) {
		this.selection = selection;
	}

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		selection = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage().getSelection();
		doOpenComponentFinderGraphForSelected();
		return null;
	}

	
	private void doOpenComponentFinderGraphForSelected() {
		final Definition def = findSelection();
		if (! (def instanceof Def_Testcase)) {
			return;
		}

		final IFile selectedFile = (IFile)def.getLocation().getFile();
		final IProject project = selectedFile.getProject();

		final IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		final DirectedSparseGraph<NodeDescriptor, EdgeDescriptor> graph = new DirectedSparseGraph<NodeDescriptor, EdgeDescriptor>();
		Map<String, NodeDescriptor> labels = new HashMap<String, NodeDescriptor>();

		final Def_Testcase tc = (Def_Testcase) def;
		
		System.out.println("dOCFGFS: "+tc.getFullName());
		
		HashMap<Component_Type, List<Component_Type>> components = new HashMap<Component_Type, List<Component_Type>>();
		Component_Type ct = tc.getRunsOnType(CompilationTimeStamp.getBaseTimestamp());
		components.put(ct, new ArrayList<Component_Type>());
		TestcaseVisitor vis = new TestcaseVisitor(new ArrayList<Def_Function>(), components, ct);
		tc.accept(vis);
		//TITANDebugConsole.println("Eredmeny: ---------------------------------------------------------");
		for (Entry<Component_Type, List<Component_Type>> entry : vis.getComponents().entrySet()) {
			for (Component_Type comp : entry.getValue()) {
				//TITANDebugConsole.println(entry.getKey().getFullName()+": "+comp.getFullName());
			}
		}
		

		for (Entry<Component_Type, List<Component_Type>> entry : vis.getComponents().entrySet()) {
			NodeDescriptor node = new NodeDescriptor(entry.getKey().getFullName(), entry.getKey().getFullName(),
					NodeColours.LIGHT_GREEN, project, false, entry.getKey().getLocation());
			if (!graph.containsVertex(node)) {
				graph.addVertex(node);
				labels.put(node.getName(), node);
			}

			for (Component_Type ct2 : entry.getValue()) {
				final NodeDescriptor node2 = new NodeDescriptor(ct2.getFullName(), ct2.getFullName(),
						NodeColours.LIGHT_GREEN, project, false, ct2.getLocation());
				if (!graph.containsVertex(node2)) {
					graph.addVertex(node2);
					labels.put(node2.getName(), node2);
				}
				
				final EdgeDescriptor edge = new EdgeDescriptor(entry.getKey().getFullName() + "->" + ct2.getFullName(), Color.black);
				if (!graph.containsEdge(edge)) {
					graph.addEdge(edge, labels.get(entry.getKey().getFullName()), labels.get(ct2.getFullName()), EdgeType.DIRECTED);
				}
			}
			
			
		}
		
		String path = "";
		try {
			path = project.getPersistentProperty(
					new QualifiedName(ProjectBuildPropertyData.QUALIFIER, "Graph_Save_Path"));
		} catch (CoreException exc) {
			System.out.println("hiba");
		}
		final String oldPath = path;
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				System.out.println("Dialog: "+tc.getFullName());
				FileDialog dialog = new FileDialog(new Shell(), SWT.SAVE);
				dialog.setText("Save graph");
				dialog.setFilterPath(oldPath);
				dialog.setFilterExtensions(new String[] { "*.dot" });
				String graphFilePath = dialog.open();
				if (graphFilePath == null) {
					return;
				}
				String newPath = graphFilePath.substring(0, graphFilePath.lastIndexOf(File.separator) + 1);
				try {
					QualifiedName name = new QualifiedName(ProjectBuildPropertyData.QUALIFIER, "Graph_Save_Path");
					project.setPersistentProperty(name, newPath);
					GraphHandler.saveGraphToDot(graph, graphFilePath, tc.getFullName());
				} catch (BadLayoutException be) {
					ErrorReporter.logExceptionStackTrace("Error while saving image to " + newPath, be);
				} catch (Exception ce) {
					ErrorReporter.logExceptionStackTrace("Error while saving image to " + newPath, ce);
				}
			}
		});
		
		
		
		try {
			final IEditorPart editor = page.findEditor(new FileEditorInput(selectedFile));
			if (editor instanceof ComponentFinderGraphEditor) {
				((ComponentFinderGraphEditor) editor).refreshGraph();
			} else {
				page.openEditor(new FileEditorInput(selectedFile), ComponentFinderGraphEditor.ID, true, IWorkbenchPage.MATCH_ID
						| IWorkbenchPage.MATCH_INPUT);
			}
		} catch (Exception exc) {
			final ErrorHandler errorHandler = new GUIErrorHandler();
			errorHandler.reportException("Error while parsing the project", exc);
		}
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
		IProject sourceProj = selectedFile.getProject();
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

		private HashMap<Component_Type, List<Component_Type>> components = new HashMap<Component_Type, List<Component_Type>>();
		private List<Def_Function> checkedFunctions;
		private int counter;
		private boolean cce;
		private Component_Type comp;
		
		TestcaseVisitor(final List<Def_Function> checkedFunctions, HashMap<Component_Type, List<Component_Type>> components, final Component_Type comp) {
			this.components.putAll(components);
			this.checkedFunctions = checkedFunctions;
			counter = -1;
			cce = false;
			this.comp = comp;
		}

		
		private HashMap<Component_Type, List<Component_Type>> getComponents() {
			return components;
		}

		@Override
		public int visit(final IVisitableNode node) {
			if (node instanceof Connect_Statement || node instanceof Map_Statement) {
				counter = 0;
			}
			else if (node instanceof PortReference && (counter == 0 || counter == 1)) {
				counter++;
				PortReference pr = ((PortReference)node);
				
				Assignment as = pr.getRefdAssignment(CompilationTimeStamp.getBaseTimestamp(), false);
				if (as != null && as instanceof Def_Port) {
					Def_Port dp = (Def_Port)as;
					ModuleVisitor mv = new ModuleVisitor(dp);
					Module m = dp.getMyScope().getModuleScope();
					m.accept(mv);
					for (Component_Type ct : mv.getComponents()) {
						if (!components.containsKey(comp)) {
							components.put(comp, new ArrayList<Component_Type>());
						}
							
						if (!components.get(comp).contains(ct) && !comp.equals(ct)) {
							components.get(comp).add(ct);
						}
							
					}
				}
			}
			else if (node instanceof Function_Instance_Statement) {
				Function_Instance_Statement fis = (Function_Instance_Statement)node;
				Assignment as = fis.getReference().getRefdAssignment(CompilationTimeStamp.getBaseTimestamp(), true);
				analyzeFunction(as, comp);			
			}
			else if (node instanceof ComponentCreateExpression) {
				cce = true;
			}
			else if (node instanceof Reference && cce) {
				cce = false;
				Reference ref = (Reference)node;
				Assignment as = ref.getRefdAssignment(CompilationTimeStamp.getBaseTimestamp(), true);
				if (as.getType(CompilationTimeStamp.getBaseTimestamp()) instanceof Component_Type) {
					Component_Type ct = (Component_Type)as.getType(CompilationTimeStamp.getBaseTimestamp());
					if (!components.containsKey(comp)) {
						components.put(comp, new ArrayList<Component_Type>());
					}
					if (!components.get(comp).contains(ct) && !comp.equals(ct)) {
						components.get(comp).add(ct);
					}
				}

			}
			else if (node instanceof Start_Component_Statement) {
				Assignment as = ((Start_Component_Statement)node).getFunctionInstanceReference().getRefdAssignment(CompilationTimeStamp.getBaseTimestamp(), true);
				analyzeFunction(as, comp);
			}

			return V_CONTINUE;
		}
		
		public void analyzeFunction(final Assignment assignment, final Component_Type component) {
			if (assignment != null && assignment instanceof Def_Function) {
				Def_Function df = (Def_Function)assignment;
				if (!checkedFunctions.contains(df)) {
					checkedFunctions.add(df);
					TestcaseVisitor tv = null;
					if (df.getRunsOnType(CompilationTimeStamp.getBaseTimestamp()) != null) {
						Component_Type fComp = df.getRunsOnType(CompilationTimeStamp.getBaseTimestamp());
						if (!components.containsKey(comp)) {
							components.put(comp, new ArrayList<Component_Type>());
						}
						if (!fComp.equals(component) && !components.get(component).contains(fComp)) {
							components.get(component).add(fComp);
							components.put(fComp, new ArrayList<Component_Type>());
						}
						tv = new TestcaseVisitor(checkedFunctions, components, fComp);
					}
					else {
						tv = new TestcaseVisitor(checkedFunctions, components, component);
					}
					df.accept(tv);
					components.putAll(tv.getComponents());
				}
			}
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
				Component_Type ct = (Component_Type)node;
				List<Definition> defs = ct.getComponentBody().getDefinitions();
				for (Definition def : defs) {
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

