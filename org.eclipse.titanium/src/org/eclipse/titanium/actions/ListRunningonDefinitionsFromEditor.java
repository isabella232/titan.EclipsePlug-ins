/******************************************************************************
 * Copyright (c) 2000-2021 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titanium.actions;

/**
 * @author Houssem Bahba
 * */

import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.IType.Type_type;
import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.AST.Type;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Altstep;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Function;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Testcase;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.Component_Type;
import org.eclipse.titan.designer.consoles.TITANConsole;
import org.eclipse.titan.designer.editors.ttcn3editor.TTCN3Editor;
import org.eclipse.titan.designer.editors.ttcnppeditor.TTCNPPEditor;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.GlobalParser;
import org.eclipse.titan.designer.parsers.ProjectSourceParser;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;

/**
 * Show what is running on a selected component, which can be called while
 * editing a ttcn3 file.
 *
 * @author Houssem Bahbah
 */
public final class ListRunningonDefinitionsFromEditor extends AbstractHandler {
	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		final IEditorPart editor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();

		if (editor == null || !(editor instanceof TTCN3Editor || editor instanceof TTCNPPEditor)) {
			ErrorReporter.logError("The editor is not found or not a Titan TTCN-3 editor");
			return null;
		}

		final IFile file = (IFile) editor.getEditorInput().getAdapter(IFile.class);
		final IProject project = file.getProject();
		final ProjectSourceParser projectSourceParser = GlobalParser.getProjectSourceParser(project);
		final Module module = projectSourceParser.containedModule(file);

		int offset;
		ISelection selection = null;
		if (editor instanceof TTCN3Editor) {
			selection = ((TTCN3Editor) editor).getSelectionProvider().getSelection();
		}

		if (selection instanceof TextSelection && !selection.isEmpty()
				&& !"".equals(((TextSelection) selection).getText())) {
			final TextSelection tSelection = (TextSelection) selection;
			offset = tSelection.getOffset() + tSelection.getLength();
		} else {
			offset = ((TTCN3Editor) editor).getCarretOffset();
		}

		TITANConsole.println("** What is running on this component** ");
		final ComponentFinderVisitor visitor = new ComponentFinderVisitor(offset);
		module.accept(visitor);
		final Component_Type component = visitor.component;
		if (component == null) {
			TITANConsole.println("Could not find a component under the selection.");

			return null;
		}

		TITANConsole.println("Running on this component :");
		final FunctionFinderVisitor fun_visitor = new FunctionFinderVisitor(component);

		final Set<String> knownModuleNames = projectSourceParser.getKnownModuleNames();
		for (final String moduleName : knownModuleNames) {
			final Module searchModule = projectSourceParser.getModuleByName(moduleName);
			searchModule.accept(fun_visitor);
		}

		return null;
	}

	// Component Visitor
	private static class ComponentFinderVisitor extends ASTVisitor {
		Component_Type component;
		int offset;

		public ComponentFinderVisitor(final int offset) {
			super();
			this.offset = offset;
		}

		@Override
		public int visit(final IVisitableNode node) {
			if (node instanceof Def_Type) {
				final Def_Type tyepDefinition = (Def_Type) node;
				final Location location = tyepDefinition.getLocation();
				final Type type = tyepDefinition.getType(CompilationTimeStamp.getBaseTimestamp());

				if (location.containsOffset(offset)) {
					if (type.getTypetype() != Type_type.TYPE_COMPONENT) {
						TITANConsole.println("The type selected ('" + tyepDefinition.getIdentifier().getDisplayName() + "') is not a component.");

						return V_CONTINUE;
					}

					TITANConsole.println("Component : " + tyepDefinition.getIdentifier().getDisplayName());
					component = (Component_Type)type;

					return V_CONTINUE;
				}
			}

			return V_CONTINUE;
		}
	}

	//Function Visitor 	
	private static class FunctionFinderVisitor extends ASTVisitor {
		Component_Type component;

		public FunctionFinderVisitor(final Component_Type component) {
			super();
			this.component = component;
		}

		@Override
		public int visit(final IVisitableNode node) {
			if (node instanceof Def_Function) {
				if (((Def_Function) node).getRunsOnType(CompilationTimeStamp.getBaseTimestamp()) == component) {
					TITANConsole.println(((Def_Function) node).getFullName());

					return V_CONTINUE;
				}
			} else if (node instanceof Def_Altstep) {
				if (((Def_Altstep) node).getRunsOnType(CompilationTimeStamp.getBaseTimestamp()) == component) {
					TITANConsole.println(((Def_Altstep) node).getFullName());

					return V_CONTINUE;
				}
			} else if (node instanceof Def_Testcase) {
				if (((Def_Testcase) node).getRunsOnType(CompilationTimeStamp.getBaseTimestamp()) == component) {
					TITANConsole.println(((Def_Testcase) node).getFullName());

					return V_CONTINUE;
				}
			}

			return V_CONTINUE;
		}
	}
}
