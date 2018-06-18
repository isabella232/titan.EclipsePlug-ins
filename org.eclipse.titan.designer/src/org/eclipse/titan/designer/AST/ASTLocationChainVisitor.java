/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.titan.designer.consoles.TITANDebugConsole;

/**
 * @author Adam Delic
 * @author Arpad Lovassy
 */
public class ASTLocationChainVisitor extends ASTVisitor {
	private final List<IVisitableNode> chain = new ArrayList<IVisitableNode>();
	private final int offset;

	public ASTLocationChainVisitor(final int offset) {
		this.offset = offset;
	}

	public List<IVisitableNode> getChain() {
		return chain;
	}

	@Override
	/** {@inheritDoc} */
	public int visit(final IVisitableNode node) {
		if (node instanceof ILocateableNode) {
			final Location loc = ((ILocateableNode)node).getLocation();
			if (loc != null && loc.containsOffset(offset)) {
				chain.add(node);
			} else {
				// skip the children, the offset is not inside this node
				return V_SKIP;
			}
		}
		return V_CONTINUE;
	}

	public void printChain() {
		final StringBuilder sb = new StringBuilder();
		sb.append("Node chain for offset ").append(offset).append(" : ");
		boolean first = true;
		for (IVisitableNode node : chain) {
			if (!first) {
				sb.append(" -> ");
			} else {
				first = false;
			}
			sb.append(node.getClass().getSimpleName());
		}
		TITANDebugConsole.println(sb.toString());
	}

}
