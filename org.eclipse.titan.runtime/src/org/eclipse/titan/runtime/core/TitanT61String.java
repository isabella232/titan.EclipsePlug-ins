/******************************************************************************
 * Copyright (c) 2000-2020 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.titan.runtime.core.BER.ASN_BERdescriptor;
import org.eclipse.titan.runtime.core.BER.ASN_Tag;
import org.eclipse.titan.runtime.core.BER.ASN_TagClass;
import org.eclipse.titan.runtime.core.JSON.TTCN_JSONdescriptor;
import org.eclipse.titan.runtime.core.JSON.json_string_escaping;

/**
 * ASN.1 T61 string
 *
 * @author Kristof Szabados
 */
public class TitanT61String extends TitanUniversalCharString {
	private static final ASN_Tag TitanT61String_tag_[] = new ASN_Tag[] {new ASN_Tag(ASN_TagClass.ASN_TAG_UNIV, 20)};
	public static final ASN_BERdescriptor TitanT61String_Ber_ = new ASN_BERdescriptor(1, TitanT61String_tag_);
	public static final TTCN_JSONdescriptor TitanT61String_json_ = new TTCN_JSONdescriptor(false, null, false, null, null, false, false, false, 0, null, false, json_string_escaping.ESCAPE_AS_SHORT);
	public static final TTCN_Typedescriptor TitanT61String_descr_ = new TTCN_Typedescriptor("T61String", TitanT61String_Ber_, null, TitanT61String_json_, null);

	/**
	 * Factory function to create a T61 string from an octetstring
	 * containing iso2022 format content.
	 *
	 * @param p_os
	 *                the source octetstring.
	 * */
	public static TitanT61String TTCN_ISO2022_2_T61String(final TitanOctetString p_os) {
		final byte osstr[] = p_os.get_value();
		final int len = osstr.length;
		final ArrayList<TitanUniversalChar> ucstr = new ArrayList<TitanUniversalChar>(len);

		for (int i = 0; i < len; i++) {
			ucstr.add(new TitanUniversalChar((char) 0, (char) 0, (char) 0, (char)osstr[i]));
		}

		return new TitanT61String(ucstr);
	}

	/**
	 * Initializes to unbound value.
	 * */
	public TitanT61String() {
		//intentionally empty
	}

	/**
	 * Initializes to a given value.
	 *
	 * @param otherValue
	 *                the value to initialize to.
	 * */
	public TitanT61String(final TitanUniversalCharString otherValue) {
		super(otherValue);
	}

	/**
	 * Initializes to a given value.
	 *
	 * @param otherValue
	 *                the value to initialize to.
	 * */
	public TitanT61String(final List<TitanUniversalChar> otherValue) {
		super(otherValue);
	}

	/**
	 * Initializes to a given value.
	 *
	 * @param otherValue
	 *                the value to initialize to.
	 * */
	public TitanT61String(final TitanT61String otherValue) {
		super(otherValue);
	}
}
