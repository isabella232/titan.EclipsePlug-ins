/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.ASN1.Object;

import org.eclipse.titan.designer.AST.Identifier;

/**
 * Class to represent a literal element in an OCS.
 *
 * @author Kristof Szabados
 */
public final class ObjectClassSyntax_literal extends ObjectClassSyntax_Node {

	private final Identifier word;
	private final String text;

	public ObjectClassSyntax_literal(final Identifier word) {
		this.word = word;
		text = null;
	}

	public ObjectClassSyntax_literal(final String text) {
		this.text = text;
		word = null;
	}

	@Override
	/** {@inheritDoc} */
	public void accept(final ObjectClassSyntax_Visitor visitor) {
		visitor.visitLiteral(this);
	}

	public String getLiteral() {
		if (null == word) {
			return text;
		}

		return word.getDisplayName();
	}

	@Override
	/** {@inheritDoc} */
	public String getDisplayName() {
		final StringBuilder builder = new StringBuilder("`");
		if (null == word) {
			builder.append(text);
		} else {
			builder.append(word.getDisplayName());
		}
		builder.append("''");
		return builder.toString();
	}
}
