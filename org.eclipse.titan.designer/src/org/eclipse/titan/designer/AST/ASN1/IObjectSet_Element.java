/******************************************************************************
 * Copyright (c) 2000-2020 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.ASN1;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.Scope;

/**
 * Something that can be in an ObjectSet.
 *
 * @author Kristof Szabados
 */
public interface IObjectSet_Element {
	IObjectSet_Element newOseInstance();

	void accept(ObjectSetElement_Visitor visitor);

	/**
	 * Sets the full name of the node.
	 *
	 * @param nameParent the name to be set
	 * */
	void setFullNameParent(INamedNode nameParent);

	void setMyScopeOse(Scope scope);

	void setGenNameOse(final String prefix, final String suffix);

	boolean memberAccept(final ASTVisitor v);
}
