/******************************************************************************
 * Copyright (c) 2000-2017 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.markers.spotters.implementation;

import java.text.MessageFormat;
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
import org.eclipse.titan.designer.AST.TTCN3.types.ComponentTypeBody;
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
public class UnusedGlobalDefinition extends BaseProjectCodeSmellSpotter {

	public UnusedGlobalDefinition() {
		super(CodeSmellType.UNUSED_GLOBAL_DEFINITION);
	}

	@Override
	protected void process(IProject project, Problems problems) {
		TITANDebugConsole.println("Unused global definition");

		final ProjectSourceParser projectSourceParser = GlobalParser.getProjectSourceParser(project);
		final Set<String> knownModuleNames = projectSourceParser.getKnownModuleNames();
		final List<Module> modules = new ArrayList<Module>();
		final Set<Assignment> unused = new HashSet<Assignment>();

		for (final String moduleName : new TreeSet<String>(knownModuleNames)) {
			Module module = projectSourceParser.getModuleByName(moduleName);
			modules.add(module);
			final GlobalDefinitionCheck chek = new GlobalDefinitionCheck();
			module.accept(chek);
			unused.addAll(chek.getDefinitions());
		}

		for (Module module : modules) {
			final GlobalUsedDefinitionCheck chekUsed = new GlobalUsedDefinitionCheck();
			module.accept(chekUsed);
			unused.removeAll(chekUsed.getDefinitions());
		}

		for (Assignment ass : unused) {
			final String name = ass.getIdentifier().getDisplayName();
			final String msg = MessageFormat.format("The {0} `{1}'' seems to be never used globally", ass.getAssignmentName(), name);
			problems.report(ass.getIdentifier().getLocation(), msg);
		}
	}

	class GlobalDefinitionCheck extends ASTVisitor {

		private Set<Assignment> setOfDefinition = new HashSet<Assignment>();

		public GlobalDefinitionCheck() {
			setOfDefinition.clear();
		}

		public Set<Assignment> getDefinitions() {
			return setOfDefinition;
		}

		@Override
		public int visit(final IVisitableNode node) {
			if (node instanceof Assignment) {
				final Assignment assignment = (Assignment) node;
				if (!assignment.isLocal() && !(assignment.getMyScope() instanceof ComponentTypeBody)) {
					setOfDefinition.add(assignment);
				}
			}
			return V_CONTINUE;
		}
	}

	class GlobalUsedDefinitionCheck extends ASTVisitor {

		private Set<Assignment> setOfDefinition = new HashSet<Assignment>();

		public GlobalUsedDefinitionCheck() {
			setOfDefinition.clear();
		}

		public Set<Assignment> getDefinitions() {
			return setOfDefinition;
		}

		@Override
		public int visit(final IVisitableNode node) {
			if (node instanceof Reference) {
				if (((Reference) node).getIsErroneous(CompilationTimeStamp.getBaseTimestamp())) {
					return V_CONTINUE;
				}

				final Assignment assignment = ((Reference) node).getRefdAssignment(CompilationTimeStamp.getBaseTimestamp(), false, null);
				if (assignment != null && !assignment.isLocal()) {
					setOfDefinition.add(assignment);
				}
			}
			return V_CONTINUE;
		}
	}
}