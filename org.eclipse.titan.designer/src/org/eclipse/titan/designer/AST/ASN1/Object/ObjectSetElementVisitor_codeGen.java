/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.ASN1.Object;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.ASN1.ASN1Object;
import org.eclipse.titan.designer.AST.ASN1.ObjectSet;
import org.eclipse.titan.designer.AST.ASN1.ObjectSetElement_Visitor;
import org.eclipse.titan.designer.compiler.JavaGenData;

/**
 * ObjectSetElement Visitor, code generator.
 *
 * @author Kristof Szabados
 */
public class ObjectSetElementVisitor_codeGen extends ObjectSetElement_Visitor {
	private final JavaGenData aData;

	public ObjectSetElementVisitor_codeGen(final ObjectSet parent, final JavaGenData aData) {
		super(parent.getLocation());
		this.aData = aData;
	}

	@Override
	public void visitObject(final ASN1Object p) {
		p.generateCode(aData);
	}

	@Override
	public void visitObjectSetReferenced(final Referenced_ObjectSet p) {
		p.generateCode(aData);
	}

	@Override
	protected boolean memberAccept(final ASTVisitor v) {
		return true;
	}

}
