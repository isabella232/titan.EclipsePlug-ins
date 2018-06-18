/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.types.subtypes;

/**
 * @author Adam Delic
 * */
public abstract class StringSubtypeTreeElement extends SubtypeConstraint {
	public enum StringType {
		CHARSTRING, UNIVERSAL_CHARSTRING
	}

	public enum ElementType {
		// EmptyStringSet
		NONE,
		// FullStringSet
		ALL,
		// StringSetOperation
		OPERATION,
		// StringSetConstraint
		CONSTRAINT
	}

	protected final StringType stringType;

	protected StringSubtypeTreeElement(final StringType stringType) {
		this.stringType = stringType;
	}

	public StringSubtypeTreeElement evaluate() {
		return this;
	}

	public abstract ElementType getElementType();
}
