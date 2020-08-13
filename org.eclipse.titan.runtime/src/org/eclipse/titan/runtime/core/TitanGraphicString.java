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
 * ASN.1 graphic string
 *
 * @author Kristof Szabados
 */
public class TitanGraphicString extends TitanUniversalCharString {
	private static final ASN_Tag TitanGraphicString_tag_[] = new ASN_Tag[] {new ASN_Tag(ASN_TagClass.ASN_TAG_UNIV, 25)};
	public static final ASN_BERdescriptor TitanGraphicString_Ber_ = new ASN_BERdescriptor(1, TitanGraphicString_tag_);
	public static final TTCN_JSONdescriptor TitanGraphicString_json_ = new TTCN_JSONdescriptor(false, null, false, null, null, false, false, false, 0, null, false, json_string_escaping.ESCAPE_AS_SHORT);
	public static final TTCN_Typedescriptor TitanGraphicString_descr_ = new TTCN_Typedescriptor("GraphicString", TitanGraphicString_Ber_, null, TitanGraphicString_json_, null);

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
	public TitanGraphicString(final TitanCharString otherValue) {
		super(otherValue);
	}

	/**
	 * Initializes to a given value.
	 *
	 * @param otherValue
	 *                the value to initialize to.
	 * */
	public TitanGraphicString(final TitanCharString_Element otherValue) {
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

	/**
	 * This static function is used to help with implicit conversion, where
	 * there is no better to way.
	 * <p>
	 * This particular function can be easily optimized away in during
	 * execution.
	 *
	 * @param otherValue
	 *                the other value to convert into a TitanGraphicString.
	 * @return the converted value.
	 * */
	public static TitanGraphicString convert_to_GraphicString(final TitanGraphicString otherValue) {
		return otherValue;
	}

	/**
	 * This static function is used to help with implicit conversion, where
	 * there is no better to way.
	 *
	 * @param otherValue
	 *                the other value to convert into a TitanGraphicString.
	 * @return the converted value.
	 * */
	public static TitanGraphicString convert_to_GraphicString(final TitanCharString otherValue) {
		return new TitanGraphicString(otherValue);
	}

	/**
	 * This static function is used to help with implicit conversion, where
	 * there is no better to way.
	 *
	 * @param otherValue
	 *                the other value to convert into a TitanGraphicString.
	 * @return the converted value.
	 * */
	public static TitanGraphicString convert_to_GraphicString(final TitanCharString_Element otherValue) {
		return new TitanGraphicString(otherValue);
	}
}
