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
	public TitanAsn_Any() {
		//intentionally empty
	}

	public TitanAsn_Any(final TitanOctetString aOtherValue) {
		super(aOtherValue);
	}

	public TitanAsn_Any(final TitanAsn_Any aOtherValue) {
		super(aOtherValue);
	}

  // TODO encoding/decoding support needed later
}
