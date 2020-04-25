/******************************************************************************
 * Copyright (c) 2000-2020 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;


/**
 * ASN.1 ASN GeneralizedTime template
 *
 * @author Kristof Szabados
 */
public class TitanASN_GeneralizedTime_template extends TitanVisibleString_template {
	/**
	 * Initializes to unbound/uninitialized template.
	 * */
	public TitanASN_GeneralizedTime_template() {
		//intentionally empty
	}

	/**
	 * Initializes to a given template kind.
	 *
	 * @param otherValue
	 *                the template kind to initialize to.
	 * */
	public TitanASN_GeneralizedTime_template(final template_sel otherValue) {
		super(otherValue);
		check_single_selection(otherValue);
	}

	/**
	 * Initializes to a given value.
	 * The template becomes a specific template and the value is copied.
	 *
	 * @param otherValue
	 *                the value to initialize to.
	 * */
	public TitanASN_GeneralizedTime_template(final TitanASN_GeneralizedTime otherValue) {
		super(otherValue);
	}

	/**
	 * Initializes to a given template.
	 *
	 * @param otherValue
	 *                the template to initialize to.
	 * */
	public TitanASN_GeneralizedTime_template(final TitanASN_GeneralizedTime_template otherValue) {
		super(otherValue);
	}

	@Override
	public TitanASN_GeneralizedTime valueof() {
		if (template_selection != template_sel.SPECIFIC_VALUE || is_ifPresent) {
			throw new TtcnError("Performing a valueof or send operation on a non-specific `GeneralizedTime' template.");
		}

		return new TitanASN_GeneralizedTime(single_value);
	}
}
