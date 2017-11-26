/******************************************************************************
 * Copyright (c) 2000-2017 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.markers.spotters.implementation;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.AST.TTCN3.statements.If_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.StatementBlock;
import org.eclipse.titanium.markers.spotters.BaseModuleCodeSmellSpotter;
import org.eclipse.titanium.markers.types.CodeSmellType;

/**
 * This class marks the following code smell:
 * The function has only one direct statement,
 * which is an if statement with a negated condition (not(x)).
 *
 * @author Kristof Szabados
 */
public class IfNotWithoutElse extends BaseModuleCodeSmellSpotter {
	private static final String ERROR_MESSAGE = "Conditional operation with 'not' expression and without else clause";

	public IfNotWithoutElse() {
		super(CodeSmellType.IF_NOT_WITHOUT_ELSE);
	}

	@Override
	public void process(final IVisitableNode node, final Problems problems) {
		if (node instanceof If_Statement) {
			final If_Statement s = (If_Statement) node;
			if (s.getStatementBlock() == null && s.getIfClauses() != null && s.getIfClauses().isExactlyOneNegated()) {
				final StatementBlock parentBlock = s.getMyStatementBlock();
				if (parentBlock != null && parentBlock.getSize() == 1) {
					problems.report(s.getLocation(), ERROR_MESSAGE);
				}
			}
		}
	}

	@Override
	public List<Class<? extends IVisitableNode>> getStartNode() {
		final List<Class<? extends IVisitableNode>> ret = new ArrayList<Class<? extends IVisitableNode>>(1);
		ret.add(If_Statement.class);
		return ret;
	}
}
