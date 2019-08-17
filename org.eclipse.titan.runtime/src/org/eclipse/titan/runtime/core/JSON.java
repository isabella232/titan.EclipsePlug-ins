/******************************************************************************
 * Copyright (c) 2000-2019 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

import java.util.ArrayList;

/**
 * FIXME comment
 *
 * @author Farkas Izabella Ingrid
 **/
public class JSON {

	public static final class JsonEnumText {
		public int index;
		public String text;
	}

	public static final class TTCN_JSONdescriptor {
		//TODO could the members be private?
		public boolean omit_as_null;
		public String alias;
		public boolean as_value;
		public String default_value;
		public boolean metainfo_unbound;
		public boolean as_number;
		public boolean as_map;
		public int nof_enum_texts;
		public ArrayList<JsonEnumText> enum_texts;

		public TTCN_JSONdescriptor() {
			// TODO Auto-generated constructor stub
		}

		public TTCN_JSONdescriptor(final boolean omit_as_null,
				final String alias,
				final boolean as_value,
				final String default_value,
				final boolean metainfo_unbound,
				final boolean as_number,
				final boolean as_map,
				final int nof_enum_texts,
				final ArrayList<JsonEnumText> enum_texts) {
			this.omit_as_null = omit_as_null;
			this.alias = alias;
			this.as_value = as_value;
			this.default_value = default_value;
			this.metainfo_unbound = metainfo_unbound;
			this.as_number = as_number;
			this.nof_enum_texts = nof_enum_texts;
			this.enum_texts = enum_texts;
		}
	}

	// JSON descriptors for base types
	public static final TTCN_JSONdescriptor TitanInteger_json_ = new TTCN_JSONdescriptor(false, null, false, null, false, false, false, 0, null);
	public static final TTCN_JSONdescriptor TitanBoolean_json_ = new TTCN_JSONdescriptor(false, null, false, null, false, false, false, 0, null);
	public static final TTCN_JSONdescriptor TitanBitString_json_ = new TTCN_JSONdescriptor(false, null, false, null, false, false, false, 0, null);
	public static final TTCN_JSONdescriptor TitanOctetString_json_ = new TTCN_JSONdescriptor(false, null, false, null, false, false, false, 0, null);
	public static final TTCN_JSONdescriptor TitanHexString_json_ = new TTCN_JSONdescriptor(false, null, false, null, false, false, false, 0, null);
	public static final TTCN_JSONdescriptor TitanCharString_json_ = new TTCN_JSONdescriptor(false, null, false, null, false, false, false, 0, null);
	public static final TTCN_JSONdescriptor TitanFloat_json_ = new TTCN_JSONdescriptor(false, null, false, null, false, false, false, 0, null);
	public static final TTCN_JSONdescriptor TitanUniversalCharString_json_ = new TTCN_JSONdescriptor(false, null, false, null, false, false, false, 0, null);
	public static final TTCN_JSONdescriptor TitanVerdictType_json_ = new TTCN_JSONdescriptor(false, null, false, null, false, false, false, 0, null);
	public static final TTCN_JSONdescriptor TitanNumericString_json_ = new TTCN_JSONdescriptor(false, null, false, null, false, false, false, 0, null);
	public static final TTCN_JSONdescriptor TitanUTF8String_json_ = new TTCN_JSONdescriptor(false, null, false, null, false, false, false, 0, null);
	public static final TTCN_JSONdescriptor TitanGeneralString_json_= new TTCN_JSONdescriptor(false, null, false, null, false, false, false, 0, null);
	public static final TTCN_JSONdescriptor TitanPrintableString_json_ = new TTCN_JSONdescriptor(false, null, false, null, false, false, false, 0, null);
	public static final TTCN_JSONdescriptor TitanUniversalString_json_ = new TTCN_JSONdescriptor(false, null, false, null, false, false, false, 0, null);
	public static final TTCN_JSONdescriptor TitanBMPString_json_ = new TTCN_JSONdescriptor(false, null, false, null, false, false, false, 0, null);
	public static final TTCN_JSONdescriptor TitanGraphicString_json_ = new TTCN_JSONdescriptor(false, null, false, null, false, false, false, 0, null);
	public static final TTCN_JSONdescriptor TitanIA5String_json_ = new TTCN_JSONdescriptor(false, null, false, null, false, false, false, 0, null);
	public static final TTCN_JSONdescriptor TitanTeletexString_json_ = new TTCN_JSONdescriptor(false, null, false, null, false, false, false, 0, null);
	public static final TTCN_JSONdescriptor TitanVideotexString_json_ = new TTCN_JSONdescriptor(false, null, false, null, false, false, false, 0, null);
	public static final TTCN_JSONdescriptor TitanVisibleString_json_ = new TTCN_JSONdescriptor(false, null, false, null, false, false, false, 0, null);
	public static final TTCN_JSONdescriptor TitanAsn_Null_json_ = new TTCN_JSONdescriptor(false, null, false, null, false, false, false, 0, null);
	public static final TTCN_JSONdescriptor TitanObjectid_json_ = new TTCN_JSONdescriptor(false, null, false, null, false, false, false, 0, null);
	public static final TTCN_JSONdescriptor TitanAsn_Roid_json_ = new TTCN_JSONdescriptor(false, null, false, null, false, false, false, 0, null);
	public static final TTCN_JSONdescriptor TitanAsn_Any_json_ = new TTCN_JSONdescriptor(false, null, false, null, false, false, false, 0, null);
	public static final TTCN_JSONdescriptor ENUMERATED_json_ = new TTCN_JSONdescriptor(false, null, false, null, false, false, false, 0, null);


	/** JSON decoder error codes */
	enum json_decode_error {
		/** An unexpected JSON token was extracted. The token might still be valid and
		 * useful for the caller structured type. */
		JSON_ERROR_INVALID_TOKEN /*= -1*/,
		/**
		 * The JSON tokeniser couldn't extract a valid token
		 * (JSON_TOKEN_ERROR) or the format of the data extracted is
		 * invalid. In either case, this is a fatal error and the
		 * decoding cannot continue.
		 *
		 * @note This error code is always preceeded by a decoding
		 *       error, if the caller receives this code, it means that
		 *       decoding error behavior is (at least partially) set to
		 *       warnings.
		 */
		JSON_ERROR_FATAL /*= -2*/
	}

	/** JSON meta info states during decoding */
	enum json_metainfo_t {
		/** The field does not have meta info enabled */
		JSON_METAINFO_NOT_APPLICABLE,
		/** Initial state if meta info is enabled for the field */
		JSON_METAINFO_NONE,
		/** The field's value is set to null, but no meta info was received for the field yet */
		JSON_METAINFO_NEEDED,
		/** Meta info received: the field is unbound */
		JSON_METAINFO_UNBOUND
	}

	enum json_chosen_field_t {
		CHOSEN_FIELD_UNSET /*= -1*/,
		CHOSEN_FIELD_OMITTED /*= -2*/
	}
}
