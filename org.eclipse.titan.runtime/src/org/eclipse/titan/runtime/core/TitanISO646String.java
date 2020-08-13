/******************************************************************************
 * Copyright (c) 2000-2020 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

import org.eclipse.titan.runtime.core.BER.ASN_BERdescriptor;
import org.eclipse.titan.runtime.core.BER.ASN_Tag;
import org.eclipse.titan.runtime.core.BER.ASN_TagClass;
import org.eclipse.titan.runtime.core.JSON.TTCN_JSONdescriptor;
import org.eclipse.titan.runtime.core.JSON.json_string_escaping;

/**
 * ASN.1 ISO646 string
 *
 * @author Kristof Szabados
 */
public class TitanISO646String extends TitanCharString {
	private static final ASN_Tag TitanISO646String_tag_[] = new ASN_Tag[] {new ASN_Tag(ASN_TagClass.ASN_TAG_UNIV, 26)};
	public static final ASN_BERdescriptor TitanISO646String_Ber_ = new ASN_BERdescriptor(1, TitanISO646String_tag_);
	public static final TTCN_JSONdescriptor TitanISO646String_json_ = new TTCN_JSONdescriptor(false, null, false, null, null, false, false, false, 0, null, false, json_string_escaping.ESCAPE_AS_SHORT);
	public static final TTCN_Typedescriptor TitanISO646String_descr_ = new TTCN_Typedescriptor("ISO646String", TitanISO646String_Ber_, null, TitanISO646String_json_, null);

	/**
	 * Initializes to unbound value.
	 * */
	public TitanISO646String() {
		//intentionally empty
	}

	/**
	 * Initializes to a given value.
	 *
	 * @param otherValue
	 *                the value to initialize to.
	 * */
	public TitanISO646String(final String otherValue) {
		super(otherValue);
	}

	/**
	 * Initializes to a given value.
	 *
	 * @param otherValue
	 *                the value to initialize to.
	 * */
	public TitanISO646String(final TitanCharString otherValue) {
		super(otherValue);
	}

	/**
	 * Initializes to a given value.
	 *
	 * @param otherValue
	 *                the value to initialize to.
	 * */
	public TitanISO646String(final TitanCharString_Element otherValue) {
		super(otherValue);
	}

	/**
	 * Initializes to a given value.
	 *
	 * @param otherValue
	 *                the value to initialize to.
	 * */
	public TitanISO646String(final TitanISO646String otherValue) {
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
	 *                the other value to convert into a TitanISO646String.
	 * @return the converted value.
	 * */
	public static TitanISO646String convert_to_ISO646String(final TitanISO646String otherValue) {
		return otherValue;
	}

	/**
	 * This static function is used to help with implicit conversion, where
	 * there is no better to way.
	 *
	 * @param otherValue
	 *                the other value to convert into a TitanISO646String.
	 * @return the converted value.
	 * */
	public static TitanISO646String convert_to_ISO646String(final TitanCharString otherValue) {
		return new TitanISO646String(otherValue);
	}

	/**
	 * This static function is used to help with implicit conversion, where
	 * there is no better to way.
	 *
	 * @param otherValue
	 *                the other value to convert into a TitanISO646String.
	 * @return the converted value.
	 * */
	public static TitanISO646String convert_to_ISO646String(final TitanCharString_Element otherValue) {
		return new TitanISO646String(otherValue);
	}
}
