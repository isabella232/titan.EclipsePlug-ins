/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titanium.refactoring.scope.nodes;

/**
 * A class representing an edit operation in the simplified representation of the AST.
 *
 * @author Viktor Varga
 */
public class Edit {

	public final StatementNode declSt;
	public final StatementNode insertionPoint;	//if null -> remove edit

	public Edit(final StatementNode declSt, final StatementNode insertionPoint) {
		this.declSt = declSt;
		this.insertionPoint = insertionPoint;
	}

	public boolean isRemoveEdit() {
		return insertionPoint == null;
	}

}
