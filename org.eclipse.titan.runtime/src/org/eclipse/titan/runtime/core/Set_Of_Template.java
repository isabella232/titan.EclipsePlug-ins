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

	public abstract int n_elem();
	public abstract Base_Template get_at(final int index_value);
	public abstract Base_Template get_at(final TitanInteger index_value);
	public abstract Base_Template constGet_at(final int index_value);
	public abstract Base_Template constGet_at(final TitanInteger index_value);

	public abstract int n_set_items();
	public abstract Base_Template set_item(final int set_index);
}
