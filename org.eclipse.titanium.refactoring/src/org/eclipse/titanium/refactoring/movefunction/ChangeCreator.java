/******************************************************************************
 * Copyright (c) 2000-2020 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titanium.refactoring.movefunction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.text.edits.DeleteEdit;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.Assignments;
import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Function;
import org.eclipse.titan.designer.AST.TTCN3.definitions.TTCN3Module;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.GlobalParser;
import org.eclipse.titan.designer.parsers.ProjectSourceParser;
import org.eclipse.titanium.refactoring.movefunction.MoveFunctionRefactoring.MoveFunctionSettings;

/**
 * This class is only instantiated by the {@link MoveFunctionModuleRefactoring} once per each refactoring operation.
 * <p>
 * By passing the selection through the constructor and calling {@link ChangeCreator#perform()}, this class
 *  creates a {@link Change} object, which can be returned by the standard
 *  {@link Refactoring#createChange(IProgressMonitor)} method in the refactoring class.
 *
 * @author Bianka Bekefi
 */
class ChangeCreator {
	// in
	private final IFile selectedFile;
	private final MoveFunctionSettings settings;
	private final List<FunctionData> functions;
	private final IProject project;
	private static Map<Module, List<Module>> moduleImports = new HashMap<Module, List<Module>>();
	// out
	private Change change;


	ChangeCreator(final IFile selectedFile, final MoveFunctionSettings settings, final List<FunctionData> functions, final IProject project) {
		this.selectedFile = selectedFile;
		this.settings = settings;
		this.functions = functions;
		this.project = project;
	}

	ChangeCreator(final IFile selectedFile, final MoveFunctionSettings settings, final List<FunctionData> functions, final IProject project, final Map<Module, List<Module>> mi) {
		this.selectedFile = selectedFile;
		this.settings = settings;
		this.functions = functions;
		this.project = project;
		moduleImports = mi;
	}

	public Change getChange() {
		return change;
	}

	/**
	 * Creates the {@link #change} object, which contains all the inserted and edited visibility modifiers
	 * in the selected resources.
	 * */
	public void perform() {
		if (selectedFile == null) {
			return;
		}

		change = createFileChange(selectedFile);
	}

	private Change createFileChange(final IFile toVisit) {
		if (toVisit == null) {
			return null;
		}

		final ProjectSourceParser sourceParser = GlobalParser.getProjectSourceParser(toVisit.getProject());
		final Module module = sourceParser.containedModule(toVisit);
		if (module == null) {
			return null;
		}

		if (functions.isEmpty()) {
			return null;
		}

		if (settings.getExcludedModuleNames() != null && settings.getExcludedModuleNames().matcher(module.getIdentifier().getTtcnName()).matches()) {
			return null;
		}
		boolean noDestination = true;
		for (final FunctionData fd : functions) {
			if (fd.isToBeMoved() && fd.getFinalDestination() != null) {
				noDestination = false;
			}
		}
		if (noDestination) {
			return null;
		}

		CompositeChange cc = null;
		if (module instanceof TTCN3Module) {
			cc = new CompositeChange("Moving functions from: "+module.getFullName());
		}
		else {
			return null;
		}

		final TextFileChange tfc = new TextFileChange(toVisit.getName(), toVisit);
		cc.add(tfc);
		final MultiTextEdit rootEdit = new MultiTextEdit();
		tfc.setEdit(rootEdit);
		for(final FunctionData function : functions) {
			if(function.getFinalDestination() == null || !function.isToBeMoved()) {
				continue;
			}

			final Module finalDestinationModule = function.getFinalDestination().getModule();
			final TextFileChange tfcDestination = new TextFileChange(finalDestinationModule.getName(), (IFile) finalDestinationModule.getLocation().getFile());
			final MultiTextEdit rootEdit2 = new MultiTextEdit();
			tfcDestination.setEdit(rootEdit2);
			cc.add(tfcDestination);
			final Location funcDefLocation = function.getDefiniton().getLocation();
			final int length = funcDefLocation.getEndOffset() - funcDefLocation.getOffset();
			final DeleteEdit deleteEdit = new DeleteEdit(funcDefLocation.getOffset(), length);
			rootEdit.addChild(deleteEdit);
			if (!moduleImports.containsKey(finalDestinationModule)) {
				moduleImports.put(finalDestinationModule, new ArrayList<Module>());
			}
			final InsertEdit importEdit = insertMissingImports(finalDestinationModule, function.getUsedModules());
			if (importEdit != null) {
				rootEdit2.addChild(importEdit);

			}
			findFunctionUses(function);
			for (final Module m : function.getUsedBy()) {
				final TextFileChange tfcModuleUsedMethod = new TextFileChange(m.getName(), (IFile) m.getLocation().getFile());
				final MultiTextEdit rootEdit3 = new MultiTextEdit();
				tfcModuleUsedMethod.setEdit(rootEdit3);
				int offset = m.getLocation().getEndOffset();
				final Assignments assignments = m.getAssignments();
				final int nOfAssignments = assignments.getNofAssignments();
				for (int i=0; i<nOfAssignments; i++) {
					final int assignmentOffset = assignments.getAssignmentByIndex(i).getLocation().getOffset();
					if (offset > assignmentOffset) {
						offset = assignmentOffset;
					}
				}
				rootEdit3.addChild(new InsertEdit(offset
						, "\n import from "+finalDestinationModule.getIdentifier().getTtcnName()+" all;\n  "));
				cc.add(tfcModuleUsedMethod);
			}

			final Assignments destinationAssignments = finalDestinationModule.getAssignments();
			rootEdit2.addChild(new InsertEdit(destinationAssignments.getAssignmentByIndex(destinationAssignments.getNofAssignments()-1).getLocation().getEndOffset()
					, "\n"+function.getFunctionBody()+"\n"));
		}
		return cc;
	}


	private InsertEdit insertMissingImports(final Module destinationModule, final List<Module> usedModules) {
		final List<Module> importedModules = destinationModule.getImportedModules();
		String importText = "";

		for (final Module m : usedModules) {
			if (!importedModules.contains(m)
					&& !m.equals(destinationModule)
					&& !moduleImports.get(destinationModule).contains(m)) {
				importText += "import from "+m.getIdentifier().getTtcnName()+" all;\n  ";
				moduleImports.get(destinationModule).add(m);
			}
		}
		final TextFileChange insertImports = new TextFileChange(destinationModule.getName(),
				(IFile)destinationModule.getLocation().getFile());
		final MultiTextEdit rootEdit = new MultiTextEdit();
		insertImports.setEdit(rootEdit);
		int offset = destinationModule.getLocation().getEndOffset();
		final Assignments assignments = destinationModule.getAssignments();
		final int nOfAssignments = assignments.getNofAssignments();
		for (int i=0; i<nOfAssignments; i++) {
			final int assignmentOffset = assignments.getAssignmentByIndex(i).getLocation().getOffset();
			if (offset > assignmentOffset) {
				offset = assignmentOffset;
			}
		}
		rootEdit.addChild(new InsertEdit(offset, importText));
		if ("".equals(importText)) {
			return null;
		}

		return new InsertEdit(offset, importText);
	}

	private void findFunctionUses(final FunctionData function) {
		final ProjectSourceParser projectSourceParser = GlobalParser.getProjectSourceParser(project);
		for(final Module m : projectSourceParser.getModules()) {
			if (!m.equals(function.getFinalDestination().getModule())) {
				final ModuleVisitor vis = new ModuleVisitor(function.getDefiniton());
				m.accept(vis);
				if (vis.getIsUsed()
						& !m.getImportedModules().contains(function.getFinalDestination().getModule())
						& !m.equals(function.getFinalDestination().getModule())) {
					function.addUsedBy(m);
				}
			}
		}
	}

	private static class ModuleVisitor extends ASTVisitor {

		private final Def_Function function;
		private boolean isUsed;

		public ModuleVisitor(final Def_Function function) {
			this.function = function;
			this.isUsed = false;
		}

		@Override
		public int visit(final IVisitableNode node) {
			if (node instanceof Reference) {
				final Assignment assignment = ((Reference) node).getRefdAssignment(CompilationTimeStamp.getBaseTimestamp(), false, null);
				if (assignment != null && assignment.equals(function)) {
					isUsed = true;
				}
			}
			return V_CONTINUE;
		}

		public boolean getIsUsed() {
			return isUsed;
		}
	}
}

