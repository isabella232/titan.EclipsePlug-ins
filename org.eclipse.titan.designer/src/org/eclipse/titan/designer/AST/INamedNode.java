/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST;

/**
 * Provides an interface for nodes in the AST that can have names,
 * or can be part of a naming chain.
 *
 * @author Kristof Szabados
 * */
public interface INamedNode {

	String MODULENAMEPREFIX = "@";
	String DOT = ".";
	String LESSTHAN = "<";
	String MORETHAN = ">";
	String LEFTPARENTHESES = "(";
	String RIGHTPARENTHESES = ")";
	String SQUAREOPEN = "[";
	String SQUARECLOSE = "]";

	/**
	 * Sets the full name of the node.
	 *
	 * @param nameParent the name to be set
	 * */
	void setFullNameParent(INamedNode nameParent);

	/**
	 * @param child create the first part of the child's name
	 * @return the full name of the node
	 * */
	StringBuilder getFullName(INamedNode child);

	/**
	 * @return the full name of the node
	 * */
	String getFullName();

	/**
	 * @return the naming parent of this node, or null if none
	 * */
	INamedNode getNameParent();
}
