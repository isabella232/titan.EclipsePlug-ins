/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST;

/**
 * @author Adam Delic
 * */
public interface IVisitableNode {
	/**
	 * Accept a visitor on this node
	 * @param v the visitor
	 * @return false to abort visiting the tree, true otherwise
	 */
	boolean accept(final ASTVisitor v);
}
