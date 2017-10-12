/******************************************************************************
 * Copyright (c) 2000-2017 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.markers.spotters.implementation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.resources.IProject;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.TTCN3.definitions.ImportModule;
import org.eclipse.titan.designer.AST.TTCN3.definitions.TTCN3Module;
import org.eclipse.titan.designer.consoles.TITANDebugConsole;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.GlobalParser;
import org.eclipse.titan.designer.parsers.ProjectSourceParser;
import org.eclipse.titanium.markers.spotters.BaseProjectCodeSmellSpotter;
import org.eclipse.titanium.markers.types.CodeSmellType;

/**
*
* @author Farkas Izabella Ingrid
*/
public class UnusedImportsProject extends BaseProjectCodeSmellSpotter{

	private Set<TTCN3Module> setOfModules = new HashSet<TTCN3Module>();
	private Set<TTCN3Module> setOfImportModules = new HashSet<TTCN3Module>();
	
	public UnusedImportsProject() {
		super(CodeSmellType.UNUSED_IMPORTS_PROJECT);
	}
	
	@Override
	protected void process(IProject project, Problems problems) {
		TITANDebugConsole.print("Unused import in project called");
		
		final ProjectSourceParser projectSourceParser = GlobalParser.getProjectSourceParser(project);
		final Set<String> knownModuleNames = projectSourceParser.getKnownModuleNames();
		final List<TTCN3Module> modules = new ArrayList<TTCN3Module>();
		for (final String moduleName : new TreeSet<String>(knownModuleNames)) {
			Module module = projectSourceParser.getModuleByName(moduleName); 
			if (module instanceof TTCN3Module) {
				modules.add((TTCN3Module) module);
			}
		}
		
		for (TTCN3Module module : modules) {
			setOfModules.clear();
			setOfImportModules.clear();
			for (Module impMod : module.getImportedModules()) {
				if (impMod instanceof TTCN3Module) {
					setOfImportModules.add((TTCN3Module)impMod);
				}
			}
			
			ImportsCheck check = new ImportsCheck();
			module.accept(check);
			
			setOfImportModules.removeAll(setOfModules);

			for (ImportModule mod : module.getImports()){
				for (TTCN3Module m : setOfImportModules) {
					if(m.getIdentifier().equals(mod.getIdentifier())) { 
						problems.report(mod.getLocation(), "Possibly unused importation");
					}
				}
			}
		}
	}

	class ImportsCheck extends ASTVisitor {
		public int visit(final IVisitableNode node){
			if (node instanceof Reference) {
				if(((Reference) node).getIsErroneous(CompilationTimeStamp.getBaseTimestamp())) {	
					return V_CONTINUE;
				}

				final Assignment assignment = ((Reference) node).getRefdAssignment(CompilationTimeStamp.getBaseTimestamp(), false, null);
				if(assignment != null ) {
					final Scope scope =  assignment.getMyScope();
					if (scope != null) {
						final Module module = scope.getModuleScope();
						if (module instanceof TTCN3Module){
							TTCN3Module mod = (TTCN3Module)module;
							setOfModules.add(mod);
						}
						
					}
					return V_CONTINUE;
				}
			}
			return V_CONTINUE;
		}
	}
}
