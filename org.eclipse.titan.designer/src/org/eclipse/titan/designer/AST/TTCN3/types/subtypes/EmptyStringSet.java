/******************************************************************************
 * Copyright (c) 2000-2017 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.types.subtypes;

/**
 * @author Adam Delic
 * */
public final class EmptyStringSet extends StringSubtypeTreeElement {

	public EmptyStringSet(final StringType stringType) {
		super(stringType);
	}

	@Override
	/** {@inheritDoc} */
	public ElementType getElementType() {
		return ElementType.NONE;
	}

	@Override
	/** {@inheritDoc} */
	public SubtypeConstraint complement() {
		return new FullStringSet(stringType);
	}

	@Override
	/** {@inheritDoc} */
	public EmptyStringSet intersection(final SubtypeConstraint other) {
		return this;
	}

	@Override
	/** {@inheritDoc} */
	public boolean isElement(final Object o) {
		return false;
	}

	@Override
	/** {@inheritDoc} */
	public TernaryBool isEmpty() {
		return TernaryBool.TTRUE;
	}

	@Override
	/** {@inheritDoc} */
	public TernaryBool isEqual(final SubtypeConstraint other) {
		return other.isEmpty();
	}

	@Override
	/** {@inheritDoc} */
	public TernaryBool isFull() {
		return TernaryBool.TFALSE;
	}

	@Override
	/** {@inheritDoc} */
	public void toString(final StringBuilder sb) {
		// nothing to write
	}

	@Override
	/** {@inheritDoc} */
	public SubtypeConstraint union(final SubtypeConstraint other) {
		return other;
	}

	@Override
	/** {@inheritDoc} */
	public EmptyStringSet except(final SubtypeConstraint other) {
		return this;
	}

	@Override
	/** {@inheritDoc} */
	public TernaryBool isSubset(final SubtypeConstraint other) {
		return other.isEmpty();
	}

}
