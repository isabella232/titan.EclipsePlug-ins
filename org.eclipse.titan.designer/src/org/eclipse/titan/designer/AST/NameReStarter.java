/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST;

import java.lang.ref.WeakReference;

/**
 * Represents a naming node, that is part of a naming chain,
 * but will not use the name prefix gathered from his naming parents.
 * But instead function as a new root in the naming hierarchy.
 *
 * @author Kristof Szabados
 * */
public final class NameReStarter implements INamedNode {
	/** the string to be used as the new name root. */
	private final String newNameStart;
	/** the naming parent of the node. */
	private WeakReference<INamedNode> nameParent;

	public NameReStarter(final String newNameStart) {
		this.newNameStart = newNameStart;
	}

	@Override
	/** {@inheritDoc} */
	public StringBuilder getFullName(final INamedNode child) {
		return new StringBuilder(newNameStart);
	}

	@Override
	/** {@inheritDoc} */
	public String getFullName() {
		return newNameStart;
	}

	@Override
	/** {@inheritDoc} */
	public void setFullNameParent(final INamedNode nameParent) {
		this.nameParent = new WeakReference<INamedNode>(nameParent);
	}

	@Override
	/** {@inheritDoc} */
	public INamedNode getNameParent() {
		if (nameParent == null) {
			return null;
		}

		return nameParent.get();
	}
}
