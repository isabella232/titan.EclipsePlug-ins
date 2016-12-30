/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Adam Delic
 * */
class ScopeTreeNode {
	Scope scope;
	List<ScopeTreeNode> children = new ArrayList<ScopeTreeNode>();
	List<Assignment> assignments = new ArrayList<Assignment>();

	public ScopeTreeNode(final Scope scope) {
		this.scope = scope;
	}

	public void add(final Scope s, final Assignment a) {
		final List<Scope> scopePath = new ArrayList<Scope>();
		Scope tempScope = s;
		while (tempScope != null) {
			scopePath.add(tempScope);
			tempScope = tempScope.getParentScope();
		}
		addPath(scopePath, a);
	}

	void addPath(final List<Scope> scopePath, final Assignment a) {
		if (scopePath.isEmpty()) {
			if (a != null) {
				assignments.add(a);
			}
			return;
		}
		Scope lastParent = scopePath.remove(scopePath.size() - 1);
		for (ScopeTreeNode stn : children) {
			if (stn.scope == lastParent) {
				stn.addPath(scopePath, a);
				lastParent = null;
				break;
			}
		}
		if (lastParent != null) {
			final ScopeTreeNode newChild = new ScopeTreeNode(lastParent);
			children.add(newChild);
			newChild.addPath(scopePath, a);
		}
	}

	public void printTreeAsHTML(final StringBuilder sb) {
		if (scope != null) {
			sb.append("<li><b>").append(scope.getClass().getSimpleName()).append("</b> <i>").append(scope.getFullName()).append("</i>");
		}
		if (!assignments.isEmpty()) {
			sb.append("<ul>");
			for (Assignment a : assignments) {
				sb.append("<li><font color='blue'>").append(a.getClass().getSimpleName()).append(" : <u>").append(a.getIdentifier())
						.append("</u></font></li>");
			}
			sb.append("</ul>");
		}
		if (!children.isEmpty()) {
			sb.append("<ul>");
			for (ScopeTreeNode stn : children) {
				stn.printTreeAsHTML(sb);
			}
			sb.append("</ul>");
		}
	}
}

/**
 * @author Adam Delic
 * */
public class ScopeHierarchyVisitor extends ASTVisitor {
	ScopeTreeNode scopeTree = new ScopeTreeNode(null);

	@Override
	public int visit(final IVisitableNode node) {
		if (node instanceof Scope) {
			final Scope scope = (Scope) node;
			scopeTree.add(scope, null);
		} else if (node instanceof Assignment) {
			final Assignment ass = (Assignment) node;
			scopeTree.add(ass.getMyScope(), ass);
		}
		return V_CONTINUE;
	}

	public String getScopeTreeAsHTMLPage() {
		final StringBuilder sb = new StringBuilder();
		sb.append("<HTML><HEAD><TITLE>Scope Tree</TITLE></HEAD><BODY><ul>");
		scopeTree.printTreeAsHTML(sb);
		sb.append("</ul></BODY></HTML>");
		return sb.toString();
	}
}
