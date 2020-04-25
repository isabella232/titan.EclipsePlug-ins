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
 * ASN.1 ASN GeneralizedTime
 *
 * @author Kristof Szabados
 */
public class TitanASN_GeneralizedTime extends TitanVisibleString {
	private static final ASN_Tag TitanGeneralizedTime_tag_[] = new ASN_Tag[] {new ASN_Tag(ASN_TagClass.ASN_TAG_UNIV, 24)};
	public static final ASN_BERdescriptor TitanGeneralizedTime_Ber_ = new ASN_BERdescriptor(1, TitanGeneralizedTime_tag_);
	public static final TTCN_Typedescriptor TitanASN_GeneralizedTime_descr_ = new TTCN_Typedescriptor("GeneralizedTime", TitanGeneralizedTime_Ber_, null, null, null);

	/**
	 * Initializes to unbound value.
	 * */
	public TitanASN_GeneralizedTime() {
		//intentionally empty
	}

	/**
	 * Initializes to a given value.
	 *
	 * @param otherValue
	 *                the value to initialize to.
	 * */
	public TitanASN_GeneralizedTime(final TitanCharString otherValue) {
		super(otherValue);
	}

	/**
	 * Initializes to a given value.
	 *
	 * @param otherValue
	 *                the value to initialize to.
	 * */
	public TitanASN_GeneralizedTime(final TitanCharString_Element otherValue) {
		super(otherValue);
	}

	/**
	 * Initializes to a given value.
	 *
	 * @param otherValue
	 *                the value to initialize to.
	 * */
	public TitanASN_GeneralizedTime(final TitanASN_GeneralizedTime otherValue) {
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
	 *                the other value to convert into a TitanASN_GeneralizedTime.
	 * @return the converted value.
	 * */
	public static TitanASN_GeneralizedTime convert_to_ASN_GeneralizedTime(final TitanASN_GeneralizedTime otherValue) {
		return otherValue;
	}

	/**
	 * This static function is used to help with implicit conversion, where
	 * there is no better to way.
	 *
	 * @param otherValue
	 *                the other value to convert into a TitanASN_GeneralizedTime.
	 * @return the converted value.
	 * */
	public static TitanASN_GeneralizedTime convert_to_ASN_GeneralizedTime(final TitanCharString otherValue) {
		return new TitanASN_GeneralizedTime(otherValue);
	}

	/**
	 * This static function is used to help with implicit conversion, where
	 * there is no better to way.
	 *
	 * @param otherValue
	 *                the other value to convert into a TitanASN_GeneralizedTime.
	 * @return the converted value.
	 * */
	public static TitanASN_GeneralizedTime convert_to_ASN_GeneralizedTime(final TitanCharString_Element otherValue) {
		return new TitanASN_GeneralizedTime(otherValue);
	}
}
