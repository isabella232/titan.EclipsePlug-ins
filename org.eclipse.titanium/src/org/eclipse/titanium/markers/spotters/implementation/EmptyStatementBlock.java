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
import org.eclipse.titan.designer.AST.TTCN3.statements.StatementBlock;
import org.eclipse.titanium.markers.spotters.BaseModuleCodeSmellSpotter;
import org.eclipse.titanium.markers.types.CodeSmellType;

public class EmptyStatementBlock extends BaseModuleCodeSmellSpotter {
	private static final String ERROR_MESSAGE = "Empty statement block";

	public EmptyStatementBlock() {
		super(CodeSmellType.EMPTY_STATEMENT_BLOCK);
	}

	@Override
	public void process(final IVisitableNode node, final Problems problems) {
		if (node instanceof StatementBlock) {
			final StatementBlock s = (StatementBlock) node;
			if (s.isEmpty()) {
				problems.report(s.getLocation(), ERROR_MESSAGE);
			}
		}
	}

	@Override
	public List<Class<? extends IVisitableNode>> getStartNode() {
		final List<Class<? extends IVisitableNode>> ret = new ArrayList<Class<? extends IVisitableNode>>(1);
		ret.add(StatementBlock.class);
		return ret;
	}
}
