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

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.TTCN3.definitions.ControlPart;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Altstep;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Function;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Testcase;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titanium.markers.spotters.BaseModuleCodeSmellSpotter;
import org.eclipse.titanium.markers.types.CodeSmellType;

/**
*
* @author Farkas Izabella Ingrid
*/
public class UnusedLocalDefinition extends BaseModuleCodeSmellSpotter {
	public UnusedLocalDefinition() {
		super(CodeSmellType.UNUSED_LOCAL_DEFINITION);
	}

	@Override
	public void process(final IVisitableNode node, final Problems problems) {
		final Set<Assignment> unused = new HashSet<Assignment>();

		final LocalDefinitionCheck chek = new LocalDefinitionCheck();
		node.accept(chek);
		unused.addAll(chek.getDefinitions());
		
		final LocalUsedDefinitionCheck chekUsed = new LocalUsedDefinitionCheck();
		node.accept(chekUsed);
		unused.removeAll(chekUsed.getDefinitions());
		
		for (Assignment ass : unused) {
			final String name = ass.getIdentifier().getDisplayName();
			final String msg = MessageFormat.format("The {0} `{1}'' seems to be never used locally (new)", ass.getAssignmentName(), name);
			problems.report(ass.getIdentifier().getLocation(), msg);
		}
	}

	@Override
	public List<Class<? extends IVisitableNode>> getStartNode() { 
		final List<Class<? extends IVisitableNode>> ret = new ArrayList<Class<? extends IVisitableNode>>(4);
		ret.add(Def_Altstep.class); 
		ret.add(Def_Function.class);
		ret.add(Def_Testcase.class);
		ret.add(ControlPart.class);
		return ret;
	}
	
	class LocalDefinitionCheck extends ASTVisitor {

		private Set<Assignment> setOfDefinition = new HashSet<Assignment>();

		public LocalDefinitionCheck() {
			setOfDefinition.clear();
		}

		public Set<Assignment> getDefinitions() {
			return setOfDefinition;
		}

		@Override
		public int visit(final IVisitableNode node) {
			if (node instanceof Assignment) {
				final Assignment assignment = (Assignment) node;
				if (assignment.isLocal()) {
					setOfDefinition.add(assignment);
				}
			}
			return V_CONTINUE;
		}
	}

	class LocalUsedDefinitionCheck extends ASTVisitor {

		private Set<Assignment> setOfDefinition = new HashSet<Assignment>();

		public LocalUsedDefinitionCheck() {
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
				if (assignment != null && assignment.isLocal()) {
					setOfDefinition.add(assignment);
				}
			}
			return V_CONTINUE;
		}
	}
}
