/******************************************************************************
 * Copyright (c) 2000-2020 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

import java.util.List;

import org.eclipse.titan.runtime.core.BER.ASN_BERdescriptor;
import org.eclipse.titan.runtime.core.BER.ASN_Tag;
import org.eclipse.titan.runtime.core.BER.ASN_TagClass;
import org.eclipse.titan.runtime.core.JSON.TTCN_JSONdescriptor;
import org.eclipse.titan.runtime.core.JSON.json_string_escaping;

/**
 * ASN.1 UTF8 string
 *
 * @author Kristof Szabados
 */
public class TitanUTF8String extends TitanUniversalCharString {
	private static final ASN_Tag TitanUTF8String_tag_[] = new ASN_Tag[] {new ASN_Tag(ASN_TagClass.ASN_TAG_UNIV, 12)};
	public static final ASN_BERdescriptor TitanUTF8String_Ber_ = new ASN_BERdescriptor(1, TitanUTF8String_tag_);
	public static final TTCN_JSONdescriptor TitanUTF8String_json_ = new TTCN_JSONdescriptor(false, null, false, null, false, false, false, 0, null, false, json_string_escaping.ESCAPE_AS_SHORT);
	public static final TTCN_Typedescriptor TitanUTF8String_descr_ = new TTCN_Typedescriptor("UTF8String", TitanUTF8String_Ber_, null, TitanUTF8String_json_, null);


	/**
	 * Initializes to unbound value.
	 * */
	public TitanUTF8String() {
		//intentionally empty
	}

	/**
	 * Initializes to a given value.
	 *
	 * @param otherValue
	 *                the value to initialize to.
	 * */
	public TitanUTF8String(final TitanCharString otherValue) {
		super(otherValue);
	}

	/**
	 * Initializes to a given value.
	 *
	 * @param otherValue
	 *                the value to initialize to.
	 * */
	public TitanUTF8String(final TitanCharString_Element otherValue) {
		super(otherValue);
	}

	/**
	 * Initializes to a given value.
	 *
	 * @param otherValue
	 *                the value to initialize to.
	 * */
	public TitanUTF8String(final TitanUniversalCharString otherValue) {
		super(otherValue);
	}

	/**
	 * Initializes to a given value.
	 *
	 * @param otherValue
	 *                the value to initialize to.
	 * */
	public TitanUTF8String(final TitanUniversalCharString_Element otherValue) {
		super(otherValue);
	}

	/**
	 * Initializes to a given value.
	 *
	 * @param otherValue
	 *                the value to initialize to.
	 * */
	public TitanUTF8String(final List<TitanUniversalChar> otherValue) {
		super(otherValue);
	}

	/**
	 * Initializes to a given value.
	 *
	 * @param otherValue
	 *                the value to initialize to.
	 * */
	public TitanUTF8String(final TitanUTF8String otherValue) {
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
	 *                the other value to convert into a TitanUTF8String.
	 * @return the converted value.
	 * */
	public static TitanUTF8String convert_to_UTF8String(final TitanUTF8String otherValue) {
		return otherValue;
	}

	/**
	 * This static function is used to help with implicit conversion, where
	 * there is no better to way.
	 *
	 * @param otherValue
	 *                the other value to convert into a TitanUTF8String.
	 * @return the converted value.
	 * */
	public static TitanUTF8String convert_to_UTF8String(final TitanCharString otherValue) {
		return new TitanUTF8String(otherValue);
	}

	/**
	 * This static function is used to help with implicit conversion, where
	 * there is no better to way.
	 *
	 * @param otherValue
	 *                the other value to convert into a TitanUTF8String.
	 * @return the converted value.
	 * */
	public static TitanUTF8String convert_to_UTF8String(final TitanCharString_Element otherValue) {
		return new TitanUTF8String(otherValue);
	}

	/**
	 * This static function is used to help with implicit conversion, where
	 * there is no better to way.
	 *
	 * @param otherValue
	 *                the other value to convert into a TitanUTF8String.
	 * @return the converted value.
	 * */
	public static TitanUTF8String convert_to_UTF8String(final TitanUniversalCharString otherValue) {
		return new TitanUTF8String(otherValue);
	}

	/**
	 * This static function is used to help with implicit conversion, where
	 * there is no better to way.
	 *
	 * @param otherValue
	 *                the other value to convert into a TitanUTF8String.
	 * @return the converted value.
	 * */
	public static TitanUTF8String convert_to_UTF8String(final TitanUniversalCharString_Element otherValue) {
		return new TitanUTF8String(otherValue);
	}
}
