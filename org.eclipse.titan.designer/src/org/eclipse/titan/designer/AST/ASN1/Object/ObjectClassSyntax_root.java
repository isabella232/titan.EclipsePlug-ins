/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.ASN1.Object;

/**
 * Class to represent the root of an OCS.
 *
 * @author Kristof Szabados
 */
public final class ObjectClassSyntax_root extends ObjectClassSyntax_Node {

	private final ObjectClassSyntax_sequence sequence;

	public ObjectClassSyntax_root() {
		sequence = new ObjectClassSyntax_sequence(false, false);
	}

	@Override
	/** {@inheritDoc} */
	public void accept(final ObjectClassSyntax_Visitor visitor) {
		visitor.visitRoot(this);
	}

	@Override
	/** {@inheritDoc} */
	public String getDisplayName() {
		return sequence.getDisplayName();
	}

	public ObjectClassSyntax_sequence getSequence() {
		return sequence;
	}
}
