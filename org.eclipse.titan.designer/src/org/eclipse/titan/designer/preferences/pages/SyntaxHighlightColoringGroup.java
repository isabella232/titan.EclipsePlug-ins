/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.preferences.pages;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper class used to represent a branch of colorable elements in the Syntax
 * highlight selection tree.
 * 
 * @author Kristof Szabados
 */
final class SyntaxHighlightColoringGroup implements ISyntaxHighlightTreeElement {
	public List<ISyntaxHighlightTreeElement> elements;
	public String name;
	public ISyntaxHighlightTreeElement parent;

	SyntaxHighlightColoringGroup(final String name) {
		parent = null;
		this.name = name;
		elements = new ArrayList<ISyntaxHighlightTreeElement>();
	}

	public void add(final ISyntaxHighlightTreeElement element) {
		element.setParent(this);
		elements.add(element);
	}

	public ISyntaxHighlightTreeElement getParent() {
		return parent;
	}

	public void setParent(final ISyntaxHighlightTreeElement treeElement) {
		parent = treeElement;
	}

	public ISyntaxHighlightTreeElement[] getChildren() {
		return elements.toArray(new ISyntaxHighlightTreeElement[elements.size()]);
	}
}
