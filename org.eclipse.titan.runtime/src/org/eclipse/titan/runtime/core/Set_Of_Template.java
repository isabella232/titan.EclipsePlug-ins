/******************************************************************************
 * Copyright (c) 2000-2019 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;


/**
 * Set_Of_Template in titan.core
 *
 * @author Kristof Szabados
 */
public abstract class Set_Of_Template extends Restricted_Length_Template {
	/**
	 * Initializes to unbound/uninitialized template.
	 * */
	public Set_Of_Template() {
		super();
	}

	/**
	 * Initializes to a given template kind.
	 *
	 * @param other_value
	 *                the template kind to initialize to.
	 * */
	public Set_Of_Template(final template_sel other_value) {
		super(other_value);
	}

	/**
	 * Returns the number of elements.
	 *
	 * n_elem in the core.
	 *
	 * @return the number of elements.
	 * */
	public abstract int n_elem();

	/**
	 * Gives access to the given element. Indexing begins from zero. If this
	 * element of the variable was never used before, new (unbound) elements
	 * will be allocated up to (and including) this index.
	 *
	 * Index underflow and overflow causes dynamic test case error.
	 * Also if the template is not a specific value template.
	 *
	 * operator[] in the core.
	 *
	 * @param index_value
	 *            the index of the element to return.
	 * @return the element at the specified position in this list
	 * */
	public abstract Base_Template get_at(final int index_value);

	/**
	 * Gives access to the given element. Indexing begins from zero. If this
	 * element of the variable was never used before, new (unbound) elements
	 * will be allocated up to (and including) this index.
	 *
	 * Index underflow and overflow causes dynamic test case error.
	 * Also if the template is not a specific value template.
	 *
	 * operator[] in the core.
	 *
	 * @param index_value
	 *            the index of the element to return.
	 * @return the element at the specified position in this list
	 * */
	public abstract Base_Template get_at(final TitanInteger index_value);

	/**
	 * Gives read-only access to the given element. Index underflow and overflow causes
	 * dynamic test case error. Also if the template is not a specific value template.
	 *
	 * const operator[] const in the core.
	 *
	 * @param index_value
	 *            the index of the element to return.
	 * @return the element at the specified position in this list
	 * */
	public abstract Base_Template constGet_at(final int index_value);

	/**
	 * Gives read-only access to the given element. Index underflow and overflow causes
	 * dynamic test case error. Also if the template is not a specific value template.
	 *
	 * const operator[] const in the core.
	 *
	 * @param index_value
	 *            the index of the element to return.
	 * @return the element at the specified position in this list
	 * */
	public abstract Base_Template constGet_at(final TitanInteger index_value);

	/**
	 * Returns the number of set elements.
	 * Calling on non-set templates causes dynamic test case error.
	 *
	 * @return the number of set elements.
	 * */
	public abstract int n_set_items();

	/**
	 * Internal function for setting an element of a superset of
	 * subset template.
	 *
	 * @param set_index
	 *                the index of the element to use.
	 * @return the element at the specified position.
	 * */
	public abstract Base_Template set_item(final int set_index);
}
