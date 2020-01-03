/******************************************************************************
 * Copyright (c) 2000-2019 Ericsson Telecom AB
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

	/**
	 * Add a new object to the object list.
	 *
	 * @param object the object to add.
	 * */
	public void addObject(final Object_Definition object) {
		objects.add(object);
	}

	/**
	 * Returns the number of objects.
	 *
	 * @return the number of objects.
	 */
	public int getNofObjects() {
		return objects.size();
	}

	/**
	 * Returns the object at a given index.
	 *
	 * @param index the index of the element to return.
	 * @return the object at a given index.
	 */
	public Object_Definition getObjectByIndex(final int index) {
		return objects.get(index);
	}

	/**
	 * Trim the data list to the current size.
	 */
	public void trimToSize() {
		objects.trimToSize();
	}

	@Override
	/** {@inheritDoc} */
	protected boolean memberAccept(final ASTVisitor v) {
		if (objects != null) {
			for (final Object_Definition object : objects) {
				if (!object.accept(v)) {
					return false;
				}
			}
		}

		return true;
	}
}
