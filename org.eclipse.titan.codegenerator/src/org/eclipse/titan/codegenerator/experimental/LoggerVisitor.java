/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *
 *   Meszaros, Mate Robert
 *
 ******************************************************************************/

package org.eclipse.titan.codegenerator.experimental;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.IVisitableNode;

public class LoggerVisitor extends ASTVisitor {

	private HierarchyLogger logger = new HierarchyLogger();

	@Override
	public int visit(IVisitableNode node) {
		logger.visit(node);
		return V_CONTINUE;
	}

	@Override
	public int leave(IVisitableNode node) {
		logger.leave(node);
		return V_CONTINUE;
	}
}
