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

/**
 * ASN.1 ASN UTCTime string
 *
 * @author Kristof Szabados
 */
public class TitanASN_UTCTime extends TitanVisibleString {
	private static final ASN_Tag TitanUTCTime_tag_[] = new ASN_Tag[] {new ASN_Tag(ASN_TagClass.ASN_TAG_UNIV, 23)};
	public static final ASN_BERdescriptor TitanUTCTime_Ber_ = new ASN_BERdescriptor(1, TitanUTCTime_tag_);
	public static final TTCN_Typedescriptor TitanASN_UTCTime_descr_ = new TTCN_Typedescriptor("UTCTime", TitanUTCTime_Ber_, null, null, null);

	/**
	 * Initializes to unbound value.
	 * */
	public TitanASN_UTCTime() {
		//intentionally empty
	}

	/**
	 * Initializes to a given value.
	 *
	 * @param otherValue
	 *                the value to initialize to.
	 * */
	public TitanASN_UTCTime(final String otherValue) {
		super(otherValue);
	}

	/**
	 * Initializes to a given value.
	 *
	 * @param otherValue
	 *                the value to initialize to.
	 * */
	public TitanASN_UTCTime(final TitanCharString otherValue) {
		super(otherValue);
	}

	/**
	 * Initializes to a given value.
	 *
	 * @param otherValue
	 *                the value to initialize to.
	 * */
	public TitanASN_UTCTime(final TitanCharString_Element otherValue) {
		super(otherValue);
	}

	/**
	 * Initializes to a given value.
	 *
	 * @param otherValue
	 *                the value to initialize to.
	 * */
	public TitanASN_UTCTime(final TitanASN_UTCTime otherValue) {
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
	 *                the other value to convert into a TitanASN_UTCTime.
	 * @return the converted value.
	 * */
	public static TitanASN_UTCTime convert_to_ASN_UTCTime(final TitanASN_UTCTime otherValue) {
		return otherValue;
	}

	/**
	 * This static function is used to help with implicit conversion, where
	 * there is no better to way.
	 *
	 * @param otherValue
	 *                the other value to convert into a TitanASN_UTCTime.
	 * @return the converted value.
	 * */
	public static TitanASN_UTCTime convert_to_ASN_UTCTime(final TitanCharString otherValue) {
		return new TitanASN_UTCTime(otherValue);
	}

	/**
	 * This static function is used to help with implicit conversion, where
	 * there is no better to way.
	 *
	 * @param otherValue
	 *                the other value to convert into a TitanASN_UTCTime.
	 * @return the converted value.
	 * */
	public static TitanASN_UTCTime convert_to_ASN_UTCTime(final TitanCharString_Element otherValue) {
		return new TitanASN_UTCTime(otherValue);
	}
}
