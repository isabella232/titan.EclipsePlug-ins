/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titanium.refactoring.scope.nodes;

import org.eclipse.titan.designer.AST.IVisitableNode;

/**
 * Abstract class for nodes.
 *
 * @author Viktor Varga
 */
public abstract class Node {

	protected final IVisitableNode astNode;

	protected Node(final IVisitableNode astNode) {
		this.astNode = astNode;
	}

	public IVisitableNode getAstNode() {
		return astNode;
	}

	/**
	 * Returns true if the given Node equals this or any descendants.
	 * */
	protected abstract boolean containsNode(Node n);

	public abstract boolean isBlockAncestorOfThis(BlockNode bn);
	public abstract boolean isStmtAncestorOfThis(StatementNode sn);

}
