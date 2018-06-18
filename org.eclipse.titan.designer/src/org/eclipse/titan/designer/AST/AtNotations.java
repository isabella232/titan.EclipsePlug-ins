/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST;

import java.util.ArrayList;

/**
 * @author Kristof Szabados
 * */
public final class AtNotations extends ASTNode {

	private final ArrayList<AtNotation> atnotations;

	public AtNotations() {
		atnotations = new ArrayList<AtNotation>();
	}

	public void addAtNotation(final AtNotation notation) {
		if (null != notation) {
			atnotations.add(notation);
		}
	}

	public int getNofAtNotations() {
		return atnotations.size();
	}

	public AtNotation getAtNotationByIndex(final int i) {
		return atnotations.get(i);
	}

	@Override
	/** {@inheritDoc} */
	protected boolean memberAccept(final ASTVisitor v) {
		if (atnotations != null) {
			for (AtNotation an : atnotations) {
				if (!an.accept(v)) {
					return false;
				}
			}
		}
		return true;
	}
}
