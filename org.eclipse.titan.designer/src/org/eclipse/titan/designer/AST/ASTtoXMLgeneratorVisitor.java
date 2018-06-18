/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST;

import org.eclipse.titan.designer.consoles.TITANDebugConsole;

/**
 * @author Adam Delic
 * */
public class ASTtoXMLgeneratorVisitor extends ASTVisitor {
	private final StringBuilder sb = new StringBuilder();

	public ASTtoXMLgeneratorVisitor() {
	}

	@Override
	/** {@inheritDoc} */
	public int visit(final IVisitableNode node) {
		sb.append("<li><b>").append(node.getClass().getSimpleName()).append("</b>");

		if (node instanceof Identifier) {
			sb.append(" <u>").append(((Identifier)node).getDisplayName()).append("</u>");
		} else
			if (node instanceof INamedNode) {
				sb.append(" <i>").append(((INamedNode)node).getFullName()).append("</i>");
			}

		sb.append("<ul>");
		return V_CONTINUE;
	}

	@Override
	/** {@inheritDoc} */
	public int leave(final IVisitableNode node) {
		sb.append("</ul></li>");
		return V_CONTINUE;
	}

	public void printXML() {
		TITANDebugConsole.println(sb.toString());
	}
}
