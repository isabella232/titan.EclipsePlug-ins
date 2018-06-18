/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.attributes;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.IType.MessageEncoding_type;
import org.eclipse.titan.designer.AST.IVisitableNode;

/**
 * Represents a single decode attribute on an external function, used to
 * automatically generate the decoding function, according to the encoding type
 * and options passed as parameters..
 *
 * @author Kristof Szabados
 * */
public final class DecodeAttribute extends ExtensionAttribute implements IVisitableNode {

	private final MessageEncoding_type encodingType;
	private final String options;

	public DecodeAttribute(final MessageEncoding_type encodingType, final String options) {
		this.encodingType = encodingType;
		this.options = options;
	}

	@Override
	/** {@inheritDoc} */
	public ExtensionAttribute_type getAttributeType() {
		return ExtensionAttribute_type.DECODE;
	}

	public MessageEncoding_type getEncodingType() {
		return encodingType;
	}

	public String getOptions() {
		return options;
	}

	@Override
	/** {@inheritDoc} */
	public boolean accept(final ASTVisitor v) {
		switch (v.visit(this)) {
		case ASTVisitor.V_ABORT:
			return false;
		case ASTVisitor.V_SKIP:
			return true;
		}
		// no members
		if (v.leave(this) == ASTVisitor.V_ABORT) {
			return false;
		}
		return true;
	}
}
