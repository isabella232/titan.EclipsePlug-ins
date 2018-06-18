/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
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
import org.eclipse.titan.designer.AST.ModuleImportation;
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

	public UnusedImportsProject() {
		super(CodeSmellType.UNUSED_IMPORT);
	}

	@Override
	protected void process(final IProject project, final Problems problems) {
		TITANDebugConsole.println("Unused import");

		final ProjectSourceParser projectSourceParser = GlobalParser.getProjectSourceParser(project);
		final Set<String> knownModuleNames = projectSourceParser.getKnownModuleNames();
		final List<Module> modules = new ArrayList<Module>();
		for (final String moduleName : new TreeSet<String>(knownModuleNames)) {
			Module module = projectSourceParser.getModuleByName(moduleName);
			modules.add(module);
		}

		final Set<Module> setOfImportedModules = new HashSet<Module>();

		for (Module module : modules) {
			setOfImportedModules.clear();
			setOfImportedModules.addAll( module.getImportedModules());

			ImportsCheck check = new ImportsCheck();
			module.accept(check);

			setOfImportedModules.removeAll(check.getModules());

			if (module instanceof TTCN3Module) {
				for (ImportModule mod : ((TTCN3Module)module).getImports()){
					for (Module m : setOfImportedModules) {
						if(m.getIdentifier().equals(mod.getIdentifier())) {
							problems.report(mod.getIdentifier().getLocation(), "Possibly unused importation");
						}
					}
				}
			} else {
				ModuleImportsCheck importsCheck = new ModuleImportsCheck();
				module.accept(importsCheck);
				for (ModuleImportation im : importsCheck.getImports()) {
					for (Module m : setOfImportedModules) {
						if(m.getIdentifier().equals(im.getIdentifier())) {
							problems.report(im.getIdentifier().getLocation(), "Possibly unused importation");
						}
					}
				}
			}
		}
	}

	class ImportsCheck extends ASTVisitor {

		private Set<Module> setOfModules = new HashSet<Module>();

		public ImportsCheck() {
			setOfModules.clear();
		}

		public Set<Module> getModules() {
			return setOfModules;
		}

		@Override
		public int visit(final IVisitableNode node) {
			if (node instanceof Reference) {
				if(((Reference) node).getIsErroneous(CompilationTimeStamp.getBaseTimestamp())) {
					return V_CONTINUE;
				}

				final Assignment assignment = ((Reference) node).getRefdAssignment(CompilationTimeStamp.getBaseTimestamp(), false, null);
				if(assignment != null ) {
					final Scope scope =  assignment.getMyScope();
					if (scope != null) {
						setOfModules.add(scope.getModuleScope());
					}
					return V_CONTINUE;
				}
			}
			return V_CONTINUE;
		}
	}

	class ModuleImportsCheck extends ASTVisitor {
		private Set<ModuleImportation> setOfModules = new HashSet<ModuleImportation>();

		public ModuleImportsCheck() {
			setOfModules.clear();
		}

		public Set<ModuleImportation> getImports() {
			return setOfModules;
		}

		@Override
		public int visit(final IVisitableNode node) {
			if(node instanceof ModuleImportation){
				ModuleImportation mod = (ModuleImportation) node;
				setOfModules.add(mod);
			}
			return V_CONTINUE;
		}
	}
}
