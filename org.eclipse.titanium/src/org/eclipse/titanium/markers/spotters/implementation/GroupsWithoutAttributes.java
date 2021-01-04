/******************************************************************************
 * Copyright (c) 2000-2021 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titanium.markers.spotters.implementation;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.AST.TTCN3.attributes.MultipleWithAttributes;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Group;
import org.eclipse.titanium.markers.spotters.BaseModuleCodeSmellSpotter;
import org.eclipse.titanium.markers.types.CodeSmellType;

/**
 * This class identifies and throws a warning
 * for groups without any attributes.
 *
 * @author Srinivasan Venkatesan
 */

public class GroupsWithoutAttributes extends BaseModuleCodeSmellSpotter {
	private static final String WARNING_MESSAGE = "The group `{0}'' does not contain any attribute";
	
	public GroupsWithoutAttributes() {
		super(CodeSmellType.GROUPS_WITHOUT_ATTRIBUTES);
	}

	@Override
	protected void process(final IVisitableNode node, final Problems problems) {
		if (node instanceof Group) {
			final Group g = (Group) node;
			final MultipleWithAttributes attributePath = g.getAttributePath().getAttributes();
			if(attributePath == null) {
				final String msg = MessageFormat.format(WARNING_MESSAGE, g.getIdentifier().getDisplayName());
				problems.report(g.getIdentifier().getLocation(), msg);
			}
		}
	}

	@Override
	public List<Class<? extends IVisitableNode>> getStartNode() {
		final List<Class<? extends IVisitableNode>> ret = new ArrayList<Class<? extends IVisitableNode>>(1);
		ret.add(Group.class);
		return ret;
	}
}
