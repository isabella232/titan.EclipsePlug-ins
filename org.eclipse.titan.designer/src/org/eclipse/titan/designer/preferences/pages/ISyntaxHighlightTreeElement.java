/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.preferences.pages;

/**
 * This interface represents an element in tree present
 *  on the syntax highlight coloring preference page.
 *  
 *  @author Kristof Szabados
 * */
public interface ISyntaxHighlightTreeElement {

	/**
	 * @return the parent of this node in the tree.
	 * */
	public ISyntaxHighlightTreeElement getParent();

	/**
	 * Sets the parent of this node.
	 * 
	 * @param treeElement the parent node
	 * */
	public void setParent(final ISyntaxHighlightTreeElement treeElement);

	/**
	 * @return the children of the current node, or empty if none.
	 * */
	public ISyntaxHighlightTreeElement[] getChildren();
}
