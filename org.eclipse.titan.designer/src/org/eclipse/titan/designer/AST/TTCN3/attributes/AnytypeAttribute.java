/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.attributes;

import org.eclipse.titan.designer.AST.Type;

/**
 * An extension attribute representing the rules that describe the contents of
 * the anytype of the module where such attributes are applied.
 *
 * @author Kristof Szabados
 * */
public final class AnytypeAttribute extends ExtensionAttribute {

	private final Types types;

	public AnytypeAttribute(final Types types) {
		this.types = types;
	}

	@Override
	/** {@inheritDoc} */
	public ExtensionAttribute_type getAttributeType() {
		return ExtensionAttribute_type.ANYTYPE;
	}

	public int getNofTypes() {
		return types.getNofTypes();
	}

	public Type getType(final int index) {
		return types.getType(index);
	}
}
