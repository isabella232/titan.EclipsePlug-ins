/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.ASN1.Object;

/**
 * Abstract class for OCS visitors.
 *
 * @author Kristof Szabados
 */
public abstract class ObjectClassSyntax_Visitor {
	public abstract void visitRoot(ObjectClassSyntax_root parameter);

	public abstract void visitSequence(ObjectClassSyntax_sequence parameter);

	public abstract void visitLiteral(ObjectClassSyntax_literal parameter);

	public abstract void visitSetting(ObjectClassSyntax_setting parameter);
}
