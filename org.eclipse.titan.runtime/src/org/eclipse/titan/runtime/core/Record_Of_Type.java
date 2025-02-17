/******************************************************************************
 * Copyright (c) 2000-2021 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

/**
 * Record_Of_Template in titan.core
 *
 * @author Kristof Szabados
 */
public abstract class Record_Of_Type extends Base_Type {

	/**
	 * Gives access to the given element. Indexing begins from zero. If this
	 * element of the variable was never used before, new (unbound) elements
	 * will be allocated up to (and including) this index.
	 *
	 * operator[] in the core.
	 *
	 * @param index_value
	 *            the index of the element to return.
	 * @return the element at the specified position in this list
	 * */
	public abstract Base_Type get_at( final int index_value );

	/**
	 * Gives access to the given element. Indexing begins from zero. If this
	 * element of the variable was never used before, new (unbound) elements
	 * will be allocated up to (and including) this index.
	 *
	 * operator[] in the core.
	 *
	 * @param index_value
	 *            the index of the element to return.
	 * @return the element at the specified position in this list
	 * */
	public abstract Base_Type get_at(final TitanInteger index_value);

	/**
	 * Gives read-only access to the given element. Index overflow causes
	 * dynamic test case error.
	 *
	 * const operator[] const in the core.
	 *
	 * @param index_value
	 *            the index of the element to return.
	 * @return the element at the specified position in this list
	 * */
	public abstract Base_Type constGet_at( final int index_value );

	/**
	 * Gives read-only access to the given element. Index overflow causes
	 * dynamic test case error.
	 *
	 * const operator[] const in the core.
	 *
	 * @param index_value
	 *            the index of the element to return.
	 * @return the element at the specified position in this list
	 * */
	public abstract Base_Type constGet_at(final TitanInteger index_value);

	/**
	 * Returns the number of elements.
	 *
	 * n_elem in the core.
	 *
	 * @return the number of elements.
	 * */
	public abstract int n_elem();

	/**
	 * Assigns the other value to this value.
	 * Overwriting the current content in the process.
	 *<p>
	 * operator= in the core.
	 *
	 * @param other_value
	 *                the other value to assign.
	 * @return the new value object.
	 */
	public abstract Record_Of_Type operator_assign( final Record_Of_Type other_value );

	/**
	 * Checks if the current value is equivalent to the provided one.
	 *<p>
	 * operator== in the core
	 *
	 * @param other_value
	 *                the other value to check against.
	 * @return {@code true} if the values are equivalent.
	 */
	public abstract boolean operator_equals( final Record_Of_Type other_value );
}
