/******************************************************************************
 * Copyright (c) 2000-2019 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titanium.markers.spotters.implementation;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.AST.Type;
import org.eclipse.titan.designer.AST.TTCN3.attributes.MultipleWithAttributes;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Type;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Definition;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Group;
import org.eclipse.titan.designer.AST.TTCN3.definitions.TTCN3Module;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titanium.markers.spotters.BaseModuleCodeSmellSpotter;
import org.eclipse.titanium.markers.types.CodeSmellType;

/**
 * This class shows a warning when an attribute is overriden.
 *
 * @author Amit Kumar
 */


public class OverrideInAttributes extends BaseModuleCodeSmellSpotter {
	private static final String WARNING_MESSAGE = "It is not recommended to override attributes";

	public OverrideInAttributes() {
		super(CodeSmellType.OVERRIDE_IN_ATTRIBUTES);
	}
	
	@Override
	protected void process(IVisitableNode node, Problems problems) {
		if (node instanceof Group) {
			final Group g = (Group) node;
			final MultipleWithAttributes attributePath = g.getAttributePath().getAttributes();
			if(attributePath != null) {
				final int sizeG = attributePath.getNofElements();
				for(int i = 0; i < sizeG; i++) {
					if(attributePath.getAttribute(i).getModifier().toString() == "MOD_OVERRIDE") {
						problems.report(attributePath.getAttribute(i).getLocation(), WARNING_MESSAGE);
					}
				}
			}
		} else if (node instanceof TTCN3Module) {
			final TTCN3Module t = (TTCN3Module) node;
			final MultipleWithAttributes attributePathForModule = t.getAttributePath().getAttributes();
			if(attributePathForModule != null) {
				final int sizeM = attributePathForModule.getNofElements();
				for(int i = 0; i < sizeM; i++) {
					if(attributePathForModule.getAttribute(i).getModifier().toString() == "MOD_OVERRIDE") {
						problems.report(attributePathForModule.getAttribute(i).getLocation(), WARNING_MESSAGE);
					}
				}
			}
		} else if (node instanceof Definition) {
			final Definition d = (Definition) node;
			final Assignment type = d.getType(CompilationTimeStamp.getBaseTimestamp()).getDefiningAssignment();
			
			if (type != null) {
				if(type.getAssignmentName() == "type") {
					final Def_Type def = (Def_Type) node;
					final Type tempType = def.getType(CompilationTimeStamp.getBaseTimestamp());
					final MultipleWithAttributes attributePathForTypeDefinition = tempType.getAttributePath().getAttributes();
					if(attributePathForTypeDefinition != null) {
						final int sizeD = attributePathForTypeDefinition.getNofElements();
						for(int i = 0; i < sizeD; i++) {
							if(attributePathForTypeDefinition.getAttribute(i).getModifier().toString() == "MOD_OVERRIDE") {
								problems.report(attributePathForTypeDefinition.getAttribute(i).getLocation(), WARNING_MESSAGE);
							}
						}
					}
				} else {
					final MultipleWithAttributes attributePathForDefinition = d.getAttributePath().getAttributes();
					if(attributePathForDefinition != null) {
						final int sizeD = attributePathForDefinition.getNofElements();
						for(int i = 0; i < sizeD; i++) {
							if(attributePathForDefinition.getAttribute(i).getModifier().toString() == "MOD_OVERRIDE") {
								problems.report(attributePathForDefinition.getAttribute(i).getLocation(), WARNING_MESSAGE);
							}
						}
					}
				}
			}
		}
	}
	
	@Override
	public List<Class<? extends IVisitableNode>> getStartNode() {
		final List<Class<? extends IVisitableNode>> ret = new ArrayList<Class<? extends IVisitableNode>>(1);
		ret.add(Group.class);
		ret.add(Definition.class);
		ret.add(TTCN3Module.class);
		ret.add(Def_Type.class);
		return ret;
	}

}
