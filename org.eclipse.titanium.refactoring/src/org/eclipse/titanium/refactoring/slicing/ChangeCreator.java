/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titanium.refactoring.slicing;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.text.edits.DeleteEdit;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.titan.designer.AST.Assignments;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.AST.TTCN3.definitions.TTCN3Module;
import org.eclipse.titan.designer.parsers.GlobalParser;
import org.eclipse.titan.designer.parsers.ProjectSourceParser;
import org.eclipse.titanium.refactoring.slicing.SlicingRefactoring.SlicingSettings;

/**
 * This class is only instantiated by the {@link SlicingModuleRefactoring} once per each refactoring operation.
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
	private final SlicingSettings settings;
	private List<FunctionData> functions;
	// out
	private Change change;
	

	ChangeCreator(final IFile selectedFile, SlicingSettings settings, List<FunctionData> functions) {
		this.selectedFile = selectedFile;
		this.settings = settings;
		this.functions = functions;
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
		for (FunctionData fd : functions) {
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
		for(FunctionData function : functions) {
			if(function.getFinalDestination() == null || !function.isToBeMoved()) {
				continue;
			}
			TextFileChange tfcDestination = new TextFileChange(function.getFinalDestination().getModule().getName(), (IFile) function.getFinalDestination().getModule().getLocation().getFile());
			MultiTextEdit rootEdit2 = new MultiTextEdit();
			tfcDestination.setEdit(rootEdit2);
			cc.add(tfcDestination);
			int length = function.getDefiniton().getLocation().getEndOffset() - function.getDefiniton().getLocation().getOffset();
			DeleteEdit deleteEdit = new DeleteEdit(function.getDefiniton().getLocation().getOffset(), length);
			rootEdit.addChild(deleteEdit);			
			
			InsertEdit importEdit = insertMissingImports(function.getFinalDestination().getModule(), function.getUsedModules());
			if (importEdit != null) {
				rootEdit2.addChild(importEdit);

			}
			
			rootEdit2.addChild(new InsertEdit(function.getFinalDestination().getModule().getAssignments().getAssignmentByIndex(function.getFinalDestination().getModule().getAssignments().getNofAssignments()-1).getLocation().getEndOffset()
					, "\n"+function.getFunctionBody()+"\n"));
		}
		return cc;
	}
	
	
	private InsertEdit insertMissingImports(Module module, List<Module> usedModules) {
		List<Module> importedModules = module.getImportedModules();
		String importText = "";
		for (Module m : usedModules) {
			if (!importedModules.contains(m) && !m.equals(module)) {
				importText += "import from "+m.getIdentifier().getTtcnName()+" all;\n  ";
			}
		}
		final TextFileChange insertImports = new TextFileChange(module.getName(), (IFile)module.getLocation().getFile());
		final MultiTextEdit rootEdit = new MultiTextEdit();
		insertImports.setEdit(rootEdit);
		int offset = module.getLocation().getEndOffset();
		Assignments assignments = module.getAssignments();
		int nOfAssignments = assignments.getNofAssignments();
		for (int i=0; i<nOfAssignments; i++) {
			int assignmentOffset = assignments.getAssignmentByIndex(i).getLocation().getOffset();
			if (offset > assignmentOffset) {
				offset = assignmentOffset;
			}
		}
		rootEdit.addChild(new InsertEdit(offset, importText));
		if (importText.equals("")) {
			return null;
		}
		return new InsertEdit(offset, importText);
	}
}

