/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titanium.markers.spotters.implementation;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.AST.TTCN3.statements.If_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.StatementBlock;
import org.eclipse.titanium.markers.spotters.BaseModuleCodeSmellSpotter;
import org.eclipse.titanium.markers.types.CodeSmellType;

public class IfWithoutElse extends BaseModuleCodeSmellSpotter {
	private static final String ERROR_MESSAGE = "Conditional operation with positive condition and without else clause";

	public IfWithoutElse() {
		super(CodeSmellType.IF_WITHOUT_ELSE);
	}

	@Override
	public void process(final IVisitableNode node, final Problems problems) {
		if (node instanceof If_Statement) {
			final If_Statement s = (If_Statement) node;
			if (s.getStatementBlock() == null && s.getIfClauses() != null && s.getIfClauses().isExactlyOne()
					&& !s.getIfClauses().isExactlyOneNegated()) {
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
