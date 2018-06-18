/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST;

/**
 * @author Kristof Szabados
 * */
public interface IASTNode extends INamedNode {

	/**
	 * Sets the actual scope of this node.
	 *
	 * @param scope the scope to be set
	 * */
	void setMyScope(final Scope scope);

	/**
	 * @return the scope of the actual node
	 * */
	Scope getMyScope();
}
