/******************************************************************************
 * Copyright (c) 2000-2021 Ericsson Telecom AB
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
 * ASN.1 BMPstring
 *
 * @author Kristof Szabados
 */
public class TitanBMPString  extends TitanUniversalCharString {
	private static final ASN_Tag TitanBMPString_tag_[] = new ASN_Tag[] {new ASN_Tag(ASN_TagClass.ASN_TAG_UNIV, 30)};
	public static final ASN_BERdescriptor TitanBMPString_Ber_ = new ASN_BERdescriptor(1, TitanBMPString_tag_);
	public static final TTCN_JSONdescriptor TitanBMPString_json_ = new TTCN_JSONdescriptor(false, null, false, null, false, false, false, 0, null, false, json_string_escaping.ESCAPE_AS_SHORT);
	public static final TTCN_Typedescriptor TitanBMPString_descr_ = new TTCN_Typedescriptor("BMPString", TitanBMPString_Ber_, null, TitanBMPString_json_, null);


	/**
	 * Initializes to unbound value.
	 * */
	public TitanBMPString() {
		//intentionally empty
	}

	/**
	 * Initializes to a given value.
	 *
	 * @param otherValue
	 *                the value to initialize to.
	 * */
	public TitanBMPString(final String otherValue) {
		super(otherValue);
	}

	/**
	 * Initializes to a given value.
	 *
	 * @param otherValue
	 *                the value to initialize to.
	 * */
	public TitanBMPString(final TitanCharString otherValue) {
		super(otherValue);
	}

	/**
	 * Initializes to a given value.
	 *
	 * @param otherValue
	 *                the value to initialize to.
	 * */
	public TitanBMPString(final TitanCharString_Element otherValue) {
		super(otherValue);
	}

	/**
	 * Initializes to a given value.
	 *
	 * @param otherValue
	 *                the value to initialize to.
	 * */
	public TitanBMPString(final TitanUniversalCharString otherValue) {
		super(otherValue);
	}

	/**
	 * Initializes to a given value.
	 *
	 * @param otherValue
	 *                the value to initialize to.
	 * */
	public TitanBMPString(final TitanUniversalCharString_Element otherValue) {
		super(otherValue);
	}

	/**
	 * Initializes to a given value.
	 *
	 * @param otherValue
	 *                the value to initialize to.
	 * */
	public TitanBMPString(final List<TitanUniversalChar> otherValue) {
		super(otherValue);
	}

	/**
	 * Initializes to a given value.
	 *
	 * @param otherValue
	 *                the value to initialize to.
	 * */
	public TitanBMPString(final TitanBMPString otherValue) {
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
	 *                the other value to convert into a TitanBMPString.
	 * @return the converted value.
	 * */
	public static TitanBMPString convert_to_BMPString(final TitanBMPString otherValue) {
		return otherValue;
	}

	/**
	 * This static function is used to help with implicit conversion, where
	 * there is no better to way.
	 *
	 * @param otherValue
	 *                the other value to convert into a TitanBMPString.
	 * @return the converted value.
	 * */
	public static TitanBMPString convert_to_BMPString(final TitanUniversalCharString otherValue) {
		return new TitanBMPString(otherValue);
	}

	/**
	 * This static function is used to help with implicit conversion, where
	 * there is no better to way.
	 *
	 * @param otherValue
	 *                the other value to convert into a TitanBMPString.
	 * @return the converted value.
	 * */
	public static TitanBMPString convert_to_BMPString(final TitanUniversalCharString_Element otherValue) {
		return new TitanBMPString(otherValue);
	}

	/**
	 * This static function is used to help with implicit conversion, where
	 * there is no better to way.
	 *
	 * @param otherValue
	 *                the other value to convert into a TitanBMPString.
	 * @return the converted value.
	 * */
	public static TitanBMPString convert_to_BMPString(final TitanCharString otherValue) {
		return new TitanBMPString(otherValue);
	}

	/**
	 * This static function is used to help with implicit conversion, where
	 * there is no better to way.
	 *
	 * @param otherValue
	 *                the other value to convert into a TitanBMPString.
	 * @return the converted value.
	 * */
	public static TitanBMPString convert_to_BMPString(final TitanCharString_Element otherValue) {
		return new TitanBMPString(new TitanCharString(otherValue));
	}
}
