/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.ASN1.Object;

import java.util.ArrayList;

import org.eclipse.titan.designer.AST.ASTNode;
import org.eclipse.titan.designer.AST.ASTVisitor;

/**
 * ObjectSet elements (flat container).
 * Warning: the objects are not owned by
 * Objects, it stores only the pointers...
 *
 * @author Kristof Szabados
 */
public final class ASN1Objects extends ASTNode {

	private final ArrayList<Object_Definition> objects = new ArrayList<Object_Definition>();

	public void addObject(final Object_Definition object) {
		objects.add(object);
	}

	public int getNofObjects() {
		return objects.size();
	}

	public Object_Definition getObjectByIndex(final int index) {
		return objects.get(index);
	}

	public void trimToSize() {
		objects.trimToSize();
	}

	@Override
	/** {@inheritDoc} */
	protected boolean memberAccept(final ASTVisitor v) {
		// TODO: objects ?
		return true;
	}
}
