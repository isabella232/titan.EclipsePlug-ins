/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

/**
 * ASN.1 any type
 *
 * @author Kristof Szabados
 */
public class TitanAsn_Any extends TitanOctetString {
	/**
	 * Initializes to unbound value.
	 * */
	public TitanAsn_Any() {
		//intentionally empty
	}

	/**
	 * Initializes to a given value.
	 *
	 * @param otherValue
	 *                the value to initialize to.
	 * */
	public TitanAsn_Any(final TitanOctetString otherValue) {
		super(otherValue);
	}

	/**
	 * Initializes to a given value.
	 *
	 * @param otherValue
	 *                the value to initialize to.
	 * */
	public TitanAsn_Any(final TitanAsn_Any otherValue) {
		super(otherValue);
	}

  // TODO encoding/decoding support needed later
}
