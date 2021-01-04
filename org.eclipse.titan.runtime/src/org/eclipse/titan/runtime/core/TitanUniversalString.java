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
 * ASN.1 universal string
 *
 * @author Kristof Szabados
 */
public class TitanUniversalString extends TitanUniversalCharString {
	private static final ASN_Tag TitanUniversalString_tag_[] = new ASN_Tag[] {new ASN_Tag(ASN_TagClass.ASN_TAG_UNIV, 28)};
	public static final ASN_BERdescriptor TitanUniversalString_Ber_ = new ASN_BERdescriptor(1, TitanUniversalString_tag_);
	public static final TTCN_JSONdescriptor TitanUniversalString_json_ = new TTCN_JSONdescriptor(false, null, false, null, false, false, false, 0, null, false, json_string_escaping.ESCAPE_AS_SHORT);
	public static final TTCN_Typedescriptor TitanUniversalString_descr_ = new TTCN_Typedescriptor("UniversalString", TitanUniversalString_Ber_, null, TitanUniversalString_json_, null);


	/**
	 * Initializes to unbound value.
	 * */
	public TitanUniversalString() {
		//intentionally empty
	}

	/**
	 * Initializes to a given value.
	 *
	 * @param otherValue
	 *                the value to initialize to.
	 * */
	public TitanUniversalString(final TitanUniversalCharString otherValue) {
		super(otherValue);
	}

	/**
	 * Initializes to a given value.
	 *
	 * @param otherValue
	 *                the value to initialize to.
	 * */
	public TitanUniversalString(final List<TitanUniversalChar> otherValue) {
		super(otherValue);
	}

	/**
	 * Initializes to a given value.
	 *
	 * @param otherValue
	 *                the value to initialize to.
	 * */
	public TitanUniversalString(final TitanUniversalString otherValue) {
		super(otherValue);
	}
}
