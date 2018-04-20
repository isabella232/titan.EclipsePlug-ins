/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
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
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Altstep;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Function;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Testcase;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Definition;
import org.eclipse.titan.designer.AST.TTCN3.types.ComponentTypeBody;
import org.eclipse.titan.designer.AST.TTCN3.types.Component_Type;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titanium.markers.spotters.BaseModuleCodeSmellSpotter;
import org.eclipse.titanium.markers.types.CodeSmellType;

/**
 * @author Farkas Izabella Ingrid
 * */
public class RunsOnScopeReduction extends BaseModuleCodeSmellSpotter{

	public RunsOnScopeReduction() {
		super(CodeSmellType.RUNS_ON_SCOPE_REDUCTION);
	}

	@Override
	protected void process(IVisitableNode node, Problems problems) {
 		final Set<Identifier> definitions = new HashSet<Identifier>();
		final Identifier componentIdentifier;
		final CompilationTimeStamp timestamp = CompilationTimeStamp.getBaseTimestamp();
		final Identifier identifier;
		boolean isTestCase = false;
		final Component_Type componentType;
		if (node instanceof Def_Function) {
			final Def_Function variable = (Def_Function) node;
			componentType = variable.getRunsOnType(timestamp); 
			if (componentType == null) {
				return;
			}

			componentIdentifier = componentType.getComponentBody().getIdentifier();
			identifier = variable.getIdentifier();
		} else if (node instanceof Def_Altstep) {
			final Def_Altstep variable = (Def_Altstep) node;
			componentType = variable.getRunsOnType(timestamp); 
			if (componentType == null) {
				return;
			}

			componentIdentifier = componentType.getComponentBody().getIdentifier();
			identifier = variable.getIdentifier();
		} else {
			final Def_Testcase variable = (Def_Testcase) node;
			componentType = variable.getRunsOnType(timestamp); 
			if (componentType == null) {
				return;
			}

			componentIdentifier = componentType.getComponentBody().getIdentifier();
			identifier = variable.getIdentifier();
			isTestCase = true;
		}

		final ReferenceCheck chek = new ReferenceCheck();
		node.accept(chek);
		definitions.addAll(chek.getIdentifiers());

		if (definitions.isEmpty()) {
			if (isTestCase) {
				List<Definition> attributes = componentType.getComponentBody().getDefinitions();
				if (!attributes.isEmpty()) {
					problems.report(identifier.getLocation(), MessageFormat.format("The runs on component `{0}'' seems to be never used. Use empty component.",componentIdentifier.getDisplayName()));
				}
			} else {
				problems.report(identifier.getLocation(), MessageFormat.format("The runs on component `{0}'' seems to be never used, can be removed.",componentIdentifier.getDisplayName()));
			}
		} else if (!definitions.contains(componentIdentifier)) {
			if (definitions.size() == 1) {
				final Identifier id = definitions.iterator().next();
				problems.report(identifier.getLocation(), MessageFormat.format("The runs on component `{0}'' seems to be never used. Use `{1}'' component.",
						componentIdentifier.getName(), id.getDisplayName()));
			} else {
				final ComponentTypeBody variable = searchComponent(componentType.getComponentBody(), definitions, new HashSet<Identifier>());
				if (variable != null && variable.getIdentifier() != componentIdentifier) {
					problems.report(identifier.getLocation(), MessageFormat.format("The runs on component `{0}'' seems to be never used.Use `{1}'' component.",
							componentIdentifier.getDisplayName(), variable.getIdentifier().getDisplayName()));
				}
			}
		}
	}

	private ComponentTypeBody searchComponent(final ComponentTypeBody component, final Set<Identifier> definitions, Set<Identifier> identifiersOfTree) {
		final List<ComponentTypeBody> parentComponentBodies = component.getExtensions().getComponentBodies();
		if (parentComponentBodies.isEmpty()) {
			identifiersOfTree.add(component.getIdentifier());
			return null;
		}
		final Set<Identifier> setNodes = new HashSet<Identifier>();
		setNodes.add(component.getIdentifier());
		for (ComponentTypeBody variable : parentComponentBodies) {
			final Set<Identifier> identifiersOfNode = new HashSet<Identifier>();
			final ComponentTypeBody cb = searchComponent(variable, definitions, identifiersOfNode);
			if (cb != null) {
				return cb;
			}
			setNodes.addAll(identifiersOfNode);
		}

		if (setNodes.containsAll(definitions)) {
			identifiersOfTree.addAll(setNodes);
			return component;
		}

		identifiersOfTree.addAll(setNodes);
		return null;
	}
	
	class ReferenceCheck extends ASTVisitor {

		private Set<Identifier> setOfIdentifier = new HashSet<Identifier>();

		public ReferenceCheck() {
			setOfIdentifier.clear();
		}

		public Set<Identifier> getIdentifiers() {
			return setOfIdentifier;
		}

		@Override
		public int visit(final IVisitableNode node) {
			if (node instanceof Reference) {
				if (((Reference) node).getIsErroneous(CompilationTimeStamp.getBaseTimestamp())) {
					return V_CONTINUE;
				}
				final Reference reference = (Reference) node;
				final CompilationTimeStamp timestamp = CompilationTimeStamp.getBaseTimestamp();
				if (reference != null) {
					final Assignment assignment = reference.getRefdAssignment(timestamp, false);
					if (assignment != null){
						if (assignment instanceof Def_Function) {
							final Component_Type componentType = ((Def_Function) assignment).getRunsOnType(timestamp); 
							if (componentType == null) {
								return V_CONTINUE;
							}
							final Identifier sc = componentType.getComponentBody().getIdentifier();
							setOfIdentifier.add(sc);
						}
						if (assignment.getMyScope() instanceof ComponentTypeBody ) {
							final Identifier sc =((ComponentTypeBody)assignment.getMyScope()).getIdentifier();
							setOfIdentifier.add(sc);
						}
					}
				}
			}
			return V_CONTINUE;
		}
	}

	@Override
	public List<Class<? extends IVisitableNode>> getStartNode() { 
		final List<Class<? extends IVisitableNode>> ret = new ArrayList<Class<? extends IVisitableNode>>(4);
		ret.add(Def_Altstep.class); 
		ret.add(Def_Function.class);
		ret.add(Def_Testcase.class);
		return ret;
	}
}
