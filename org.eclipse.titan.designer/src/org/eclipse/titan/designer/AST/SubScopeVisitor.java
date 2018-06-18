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

/**
 * @author Adam Delic
 * */
public class SubScopeVisitor extends ASTVisitor {
	private final Scope root;
	private final List<Scope> subScopes = new ArrayList<Scope>();

	public SubScopeVisitor(final Scope root) {
		this.root = root;
	}

	@Override
	/** {@inheritDoc} */
	public int visit(final IVisitableNode node) {
		if (node instanceof Scope) {
			final Scope scope = (Scope)node;
			if (scope.isChildOf(root)) {
				// this is a sub-scope of the root
				subScopes.add(scope);
			}
		}
		return V_CONTINUE;
	}

	public List<Scope> getSubScopes() {
		return subScopes;
	}
}
