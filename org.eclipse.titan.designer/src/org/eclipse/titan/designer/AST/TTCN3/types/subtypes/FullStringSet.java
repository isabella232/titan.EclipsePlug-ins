/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.types.subtypes;

/**
 * @author Adam Delic
 * */
public final class FullStringSet extends StringSubtypeTreeElement {

	public FullStringSet(final StringType stringType) {
		super(stringType);
	}

	@Override
	/** {@inheritDoc} */
	public ElementType getElementType() {
		return ElementType.ALL;
	}

	@Override
	/** {@inheritDoc} */
	public EmptyStringSet complement() {
		return new EmptyStringSet(stringType);
	}

	@Override
	/** {@inheritDoc} */
	public SubtypeConstraint intersection(final SubtypeConstraint other) {
		return other;
	}

	@Override
	/** {@inheritDoc} */
	public boolean isElement(final Object o) {
		return true;
	}

	@Override
	/** {@inheritDoc} */
	public TernaryBool isEmpty() {
		return TernaryBool.TFALSE;
	}

	@Override
	/** {@inheritDoc} */
	public TernaryBool isEqual(final SubtypeConstraint other) {
		return other.isFull();
	}

	@Override
	/** {@inheritDoc} */
	public TernaryBool isFull() {
		return TernaryBool.TTRUE;
	}

	@Override
	/** {@inheritDoc} */
	public void toString(final StringBuilder sb) {
		sb.append("ALL");
	}

	@Override
	/** {@inheritDoc} */
	public FullStringSet union(final SubtypeConstraint other) {
		return this;
	}

	@Override
	/** {@inheritDoc} */
	public TernaryBool isSubset(final SubtypeConstraint other) {
		return TernaryBool.TTRUE;
	}

}
