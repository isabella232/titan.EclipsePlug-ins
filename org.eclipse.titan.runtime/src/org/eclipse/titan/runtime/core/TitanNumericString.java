/******************************************************************************
 * Copyright (c) 2000-2020 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

import java.util.ArrayList;

import org.eclipse.titan.runtime.core.BER.ASN_BERdescriptor;
import org.eclipse.titan.runtime.core.BER.ASN_Tag;
import org.eclipse.titan.runtime.core.BER.ASN_TagClass;
import org.eclipse.titan.runtime.core.JSON.TTCN_JSONdescriptor;
import org.eclipse.titan.runtime.core.JSON.json_string_escaping;

/**
 * ASN.1 numeric string
 *
 * @author Kristof Szabados
 */
public class TitanNumericString extends TitanCharString {
	private static final ASN_Tag TitanNumericString_tag_[] = new ASN_Tag[] {new ASN_Tag(ASN_TagClass.ASN_TAG_UNIV, 18)};
	public static final ASN_BERdescriptor TitanNumericString_Ber_ = new ASN_BERdescriptor(1, TitanNumericString_tag_);
	public static final TTCN_JSONdescriptor TitanNumericString_json_ = new TTCN_JSONdescriptor(false, null, false, null, null, false, false, false, 0, null, false, json_string_escaping.ESCAPE_AS_SHORT);
	public static final TTCN_Typedescriptor TitanNumericString_descr_ = new TTCN_Typedescriptor("NumericString", TitanNumericString_Ber_, null, TitanNumericString_json_, null);

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
	public TitanNumericString() {
		//intentionally empty
	}

	/**
	 * Initializes to a given value.
	 *
	 * @param otherValue
	 *                the value to initialize to.
	 * */
	public TitanNumericString(final String otherValue) {
		super(otherValue);
	}

	/**
	 * Initializes to a given value.
	 *
	 * @param otherValue
	 *                the value to initialize to.
	 * */
	public TitanNumericString(final TitanCharString otherValue) {
		super(otherValue);
	}

	/**
	 * Initializes to a given value.
	 *
	 * @param otherValue
	 *                the value to initialize to.
	 * */
	public TitanNumericString(final TitanCharString_Element otherValue) {
		super(otherValue);
	}

	/**
	 * Initializes to a given value.
	 *
	 * @param otherValue
	 *                the value to initialize to.
	 * */
	public TitanNumericString(final TitanNumericString otherValue) {
		super(otherValue);
	}

	/**
	 * This static function is used to help with implicit conversion, where
	 * there is no better to way.
	 * <p>
	 * This particular function can be easily optimized away in during
	 * execution.
	 *
	 * @param otherValue
	 *                the other value to convert into a TitanNumericString.
	 * @return the converted value.
	 * */
	public static TitanNumericString convert_to_NumericString(final TitanNumericString otherValue) {
		return otherValue;
	}

	/**
	 * This static function is used to help with implicit conversion, where
	 * there is no better to way.
	 *
	 * @param otherValue
	 *                the other value to convert into a TitanNumericString.
	 * @return the converted value.
	 * */
	public static TitanNumericString convert_to_NumericString(final TitanCharString otherValue) {
		return new TitanNumericString(otherValue);
	}

	/**
	 * This static function is used to help with implicit conversion, where
	 * there is no better to way.
	 *
	 * @param otherValue
	 *                the other value to convert into a TitanNumericString.
	 * @return the converted value.
	 * */
	public static TitanNumericString convert_to_NumericString(final TitanCharString_Element otherValue) {
		return new TitanNumericString(otherValue);
	}
}
