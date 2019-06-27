/******************************************************************************
 * Copyright (c) 2000-2019 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

import java.util.ArrayList;
import java.util.List;

/**
 * ASN.1 graphic string
 *
 * @author Kristof Szabados
 */
public class TitanGraphicString extends TitanUniversalCharString {
	/**
	 * Factory function to create a graphicstring from an octetstring
	 * containing iso2022 format content.
	 *
	 * @param p_os
	 *                the source octetstring.
	 * */
	public static TitanGraphicString TTCN_ISO2022_2_GraphicString(final TitanOctetString p_os) {
		final byte osstr[] = p_os.get_value();
		final int len = osstr.length;
		final ArrayList<TitanUniversalChar> ucstr = new ArrayList<TitanUniversalChar>(len);

		for (int i = 0; i < len; i++) {
			ucstr.add(new TitanUniversalChar((char) 0, (char) 0, (char) 0, (char)osstr[i]));
		}

		return new TitanGraphicString(ucstr);
	}

	/**
	 * Initializes to unbound value.
	 * */
	public TitanGraphicString() {
		//intentionally empty
	}

	/**
	 * Initializes to a given value.
	 *
	 * @param otherValue
	 *                the value to initialize to.
	 * */
	public TitanGraphicString(final TitanUniversalCharString otherValue) {
		super(otherValue);
	}

	/**
	 * Initializes to a given value.
	 *
	 * @param otherValue
	 *                the value to initialize to.
	 * */
	public TitanGraphicString(final List<TitanUniversalChar> otherValue) {
		super(otherValue);
	}

	/**
	 * Initializes to a given value.
	 *
	 * @param otherValue
	 *                the value to initialize to.
	 * */
	public TitanGraphicString(final TitanGraphicString otherValue) {
		super(otherValue);
	}
}
