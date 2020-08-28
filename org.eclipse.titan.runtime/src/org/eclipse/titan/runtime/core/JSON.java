/******************************************************************************
 * Copyright (c) 2000-2020 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

import java.math.BigInteger;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.titan.runtime.core.Base_Type.TTCN_Typedescriptor;
import org.eclipse.titan.runtime.core.JSON_Tokenizer.json_token_t;
import org.eclipse.titan.runtime.core.RAW.TTCN_RAWdescriptor;
import org.eclipse.titan.runtime.core.RAW.ext_bit_t;
import org.eclipse.titan.runtime.core.RAW.raw_sign_t;
import org.eclipse.titan.runtime.core.RAW.top_bit_order_t;
import org.eclipse.titan.runtime.core.TTCN_EncDec.raw_order_t;
import org.eclipse.titan.runtime.core.TitanCharString.CharCoding;

/**
 * JSON encoding/decoding functions
 *
 * @author Farkas Izabella Ingrid
 * @author Arpad Lovassy
 */
public class JSON {

	private JSON() {
		// Hide constructor
	}

	/**
	 * Enumerated text change structure
	 */
	public static final class JsonEnumText {
		private final int index;
		private final String text;

		public JsonEnumText(final int index, final String text) {
			this.index = index;
			this.text = text;
		}
		
		public int getIndex() {
			return index;
		}

		public String getText() {
			return text;
		}
	}

	/** Helper enumerated type for storing the different methods of escaping in character strings */
	public enum json_string_escaping {
		ESCAPE_AS_SHORT, /* use short escapes wherever possible (e.g. '\n', '\\', etc.)*/
		ESCAPE_AS_USI, /* use USI escapes in all cases (i.e. '\\u' followed by 4 hex nibbles) */
		ESCAPE_AS_TRANSPARENT /* do not escape anything, except control characters */
	}

	/**
	 * Descriptor for JSON encoding/decoding during runtime
	 * Originally TTCN_JSONdescriptor_t
	 */
	public static final class TTCN_JSONdescriptor {

		/**
		 * Encoding only.
		 * true  : use the null literal to encode omitted fields in records or sets
		 *         example: { "field1" : value1, "field2" : null, "field3" : value3 }
		 * false : skip both the field name and the value if a field is omitted
		 *         example: { "field1" : value1, "field3" : value3 }
		 * The decoder will always accept both variants.
		 */
		private final boolean omit_as_null;

		/**
		 * An alias for the name of the field (in a record, set or union).
		 * Encoding: this alias will appear instead of the name of the field
		 * Decoding: the decoder will look for this alias instead of the field's real name
		 */
		private final String alias;

		/**
		 * If set, the union will be encoded as a JSON value instead of a JSON object
		 * with one name-value pair.
		 * Since the field name is no longer present, the decoder will determine the
		 * selected field based on the type of the value. The first field (in the order
		 * of declaration) that can successfully decode the value will be the selected one.
		 */
		private final boolean as_value;

		/**
		 * Decoding only.
		 * Fields that don't appear in the JSON code will decode this value instead.
		 * Old version kept for backward compatibility.
		 */
		private final String default_value;

		/**
		 * Decoding only.
		 * Fields that don't appear in the JSON code will decode this value instead.
		 * TODO rename once the old version is removed.
		 */
		private final Base_Type actual_default_value;

		/**
		 * If set, encodes unbound fields of records and sets as null and inserts a
		 * meta info field into the JSON object specifying that the field is unbound.
		 * The decoder sets the field to unbound if the meta info field is present and
		 * the field's value in the JSON code is either null or a valid value for that
		 * field.
		 * Example: { "field1" : null, "metainfo field1" : "unbound" }
		 *
		 * Also usable on record of/set of/array types to indicate that an element is
		 * unbound. Unbound elements are encoded as a JSON object containing one
		 * metainfo member. The decoder sets the element to unbound if the object
		 * with the meta information is found.
		 * Example: [ value1, value2, { "metainfo []" : "unbound" }, value3 ]
		 */
		private final boolean metainfo_unbound;

		/**
		 * If set, the enumerated value's numeric form will be encoded as a JSON
		 * number, instead of its name form as a JSON string (affects both encoding
		 * and decoding).
		 */
		private final boolean as_number;

		/**
		 * If set, encodes the value into a map of key-value pairs (i.e. a fully
		 * customizable JSON object). The encoded type has to be a record of/set of
		 * with a record/set element type, which has 2 fields, the first of which is
		 * a non-optional universal charstring.
		 * Example: { "key1" : value1, "key2" : value2 }
		 */
		private final boolean as_map;

		/**
		 * Number of enumerated values whose texts are changed.
		 */
		private final int nof_enum_texts;

		/**
		 * List of enumerated values whose texts are changed.
		 */
		private final List<JsonEnumText> enum_texts;

		/** If set, encodes this value as the JSON literal 'null'. */
		private final boolean use_null;

		/** Setting for escaping of special characters in character strings */
		private final json_string_escaping escaping;

		public TTCN_JSONdescriptor(final boolean omit_as_null,
				final String alias,
				final boolean as_value,
				final String default_value,
				final Base_Type actual_default_value,
				final boolean metainfo_unbound,
				final boolean as_number,
				final boolean as_map,
				final int nof_enum_texts,
				final List<JsonEnumText> enum_texts,
				final boolean use_null,
				final json_string_escaping escaping) {
			this.omit_as_null = omit_as_null;
			this.alias = alias;
			this.as_value = as_value;
			this.default_value = default_value;
			this.actual_default_value = actual_default_value;
			this.metainfo_unbound = metainfo_unbound;
			this.as_number = as_number;
			this.as_map = as_map;
			this.nof_enum_texts = nof_enum_texts;
			this.enum_texts = enum_texts;
			this.use_null = use_null;
			this.escaping = escaping;
		}

		public boolean isOmit_as_null() {
			return omit_as_null;
		}

		public String getAlias() {
			return alias;
		}

		public boolean isAs_value() {
			return as_value;
		}

		public String getDefault_value() {
			return default_value;
		}

		public Base_Type getActualDefaultValue() {
			return actual_default_value;
		}

		public boolean isMetainfo_unbound() {
			return metainfo_unbound;
		}

		public boolean isAs_number() {
			return as_number;
		}

		public boolean isAs_map() {
			return as_map;
		}

		public int getNof_enum_texts() {
			return nof_enum_texts;
		}

		public List<JsonEnumText> getEnum_texts() {
			return enum_texts;
		}

		public boolean isUse_null() {
			return use_null;
		}

		public json_string_escaping getEscaping() {
			return escaping;
		}
	}

	// JSON decoder error codes, originally json_decode_error

	/**
	 * An unexpected JSON token was extracted. The token might still be valid and
	 * useful for the caller structured type. */
	public static final int JSON_ERROR_INVALID_TOKEN = -1;

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
	public static final int JSON_ERROR_FATAL = -2;

	// JSON meta info states during decoding
	// originally enum json_metainfo_t
	/** The field does not have meta info enabled */
	public static final int JSON_METAINFO_NOT_APPLICABLE = 0;
	/** Initial state if meta info is enabled for the field */
	public static final int JSON_METAINFO_NONE = 1;
	/** The field's value is set to null, but no meta info was received for the field yet */
	public static final int JSON_METAINFO_NEEDED = 2;
	/** Meta info received: the field is unbound */
	public static final int JSON_METAINFO_UNBOUND = 3;

	// originally enum json_chosen_field_t
	public static final int CHOSEN_FIELD_UNSET = -1;
	public static final int CHOSEN_FIELD_OMITTED = -2;

	// JSON decoding error messages
	public static final String JSON_DEC_BAD_TOKEN_ERROR = "Failed to extract valid token, invalid JSON format%s";
	public static final String JSON_DEC_FORMAT_ERROR = "Invalid JSON %s format, expecting %s value";
	public static final String JSON_DEC_NAME_TOKEN_ERROR = "Invalid JSON token, expecting JSON field name";
	public static final String JSON_DEC_OBJECT_END_TOKEN_ERROR = "Invalid JSON token, expecting JSON name-value pair or object end mark%s";
	public static final String JSON_DEC_REC_OF_END_TOKEN_ERROR = "Invalid JSON token, expecting JSON value or array end mark%s";
	public static final String JSON_DEC_ARRAY_ELEM_TOKEN_ERROR = "Invalid JSON token, expecting %d more JSON value%s";
	public static final String JSON_DEC_ARRAY_END_TOKEN_ERROR = "Invalid JSON token, expecting JSON array end mark%s";
	public static final String JSON_DEC_FIELD_TOKEN_ERROR = "Invalid JSON token found while decoding field '%s'";
	public static final String JSON_DEC_INVALID_NAME_ERROR = "Invalid field name '%s'";
	public static final String JSON_DEC_MISSING_FIELD_ERROR = "No JSON data found for field '%s'";
	public static final String JSON_DEC_STATIC_OBJECT_END_TOKEN_ERROR = "Invalid JSON token, expecting JSON object end mark%s";
	public static final String JSON_DEC_AS_VALUE_ERROR = "Extracted JSON %s could not be decoded by any field of the union";
	public static final String JSON_DEC_METAINFO_NAME_ERROR = "Meta info provided for non-existent field '%s'";
	public static final String JSON_DEC_METAINFO_VALUE_ERROR = "Invalid meta info for field '%s'";
	public static final String JSON_DEC_METAINFO_NOT_APPLICABLE = "Meta info not applicable to field '%s'";
	public static final String JSON_DEC_CHOSEN_FIELD_NOT_NULL = "Invalid JSON token, expecting 'null' (as indicated by a condition in attribute 'chosen')%s";
	public static final String JSON_DEC_CHOSEN_FIELD_OMITTED = "Field '%s' cannot be omitted (as indicated by a condition in attribute 'chosen')";
	public static final String JSON_DEC_CHOSEN_FIELD_OMITTED_NULL = "Field cannot be omitted (as indicated by a condition in attribute 'chosen')%s";

	// JSON descriptors for base types
	public static final TTCN_JSONdescriptor ENUMERATED_json_ = new TTCN_JSONdescriptor(false, null, false, null, null, false, false, false, 0, null, false, json_string_escaping.ESCAPE_AS_SHORT);

	////////////////////////////////////////////////////////////////////////////////
	//// CBOR conversion
	////////////////////////////////////////////////////////////////////////////////

	// Never use buff.get_read_data() without checking if it has enough bytes in the
	// buffer.
	private static byte[] check_and_get_buffer(final TTCN_Buffer buff, final int bytes) {
		if (bytes < 0) {
			throw new TtcnError(MessageFormat.format("Incorrect length byte received: {0}, while decoding using cbor2json()", bytes));
		}
		if (buff.get_pos() + bytes > buff.get_len()) {
			throw new TtcnError("Not enough bytes in bytestream while decoding using cbor2json().");
		}

		return buff.get_read_data();
	}

	private static void encode_ulong_long_int_cbor(final TTCN_Buffer buff, final int bytes, final long value) {
		for (int i = bytes - 1; i >= 0; i--) {
			buff.put_c((byte)((value >> i*8)));
		}
	}

	// major_type parameter needed for the string encoding
	private static void encode_int_cbor(final TTCN_Buffer buff, int major_type, TitanInteger int_num) {
		boolean is_negative = false;
		if (int_num.is_less_than(0)) {
			major_type = 1 << 5;
			int_num = int_num.sub().sub(1);
			is_negative = true;
		}
		if (int_num.is_native()) {
			final int uns_num = int_num.get_int();
			if (uns_num <= 23) {
				buff.put_c((byte)(major_type + uns_num));
			} else if (uns_num <= 0xFF) { // 8 bit
				buff.put_c((byte)(major_type + 24));
				encode_ulong_long_int_cbor(buff, 1, uns_num);
			} else if (uns_num <= 0xFFFF) { // 16 bit
				buff.put_c((byte)(major_type + 25));
				encode_ulong_long_int_cbor(buff, 2, uns_num);
			} else { // 32 bit
				buff.put_c((byte)(major_type + 26));
				encode_ulong_long_int_cbor(buff, 4, uns_num);
			}
		} else {
			final BigInteger bn = int_num.get_BigInteger();
			final byte byteArray[] = bn.toByteArray();
			final int bn_length = byteArray.length; //originally BN_num_bytes(bn);
			final long long_int = int_num.get_long();
			if (bn_length <= 4) { // 32 bit
				buff.put_c((byte)(major_type + 26));
				encode_ulong_long_int_cbor(buff, 4, long_int);
			} else if (bn_length <= 8) {
				buff.put_c((byte)(major_type + 27));
				encode_ulong_long_int_cbor(buff, 8, long_int);
			} else {
				// It is a bignum. Encode as bytestring
				major_type = 6 << 5; // Major type 6 for bignum
				major_type += is_negative ? 3 : 2; // Tag 2 or 3 for negative
				buff.put_c((byte)(major_type));
				major_type = 2 << 5; // Major type 2 for bytestring
				encode_int_cbor(buff, major_type, new TitanInteger( bn_length ) ); // encode the length of the bignum
				buff.put_s(byteArray); // originally BN_bn2bin(bn, tmp_num);
			}
		}
	}

	private static void decode_int_cbor(final TTCN_Buffer buff, final int bytes, final TitanInteger result) {
		final byte[] tmp = check_and_get_buffer(buff, bytes);
		final TTCN_Buffer tmp_buf = new TTCN_Buffer();
		tmp_buf.put_s(tmp);
		final TitanOctetString os = new TitanOctetString();
		tmp_buf.get_string(os);
		result.operator_assign( AdditionalFunctions.oct2int(os).get_int() );
		buff.increase_pos(bytes);
	}

	private static void decode_uint_cbor(final TTCN_Buffer buff, final int bytes, final TitanInteger result) {
		result.operator_assign(0);
		final byte[] tmp = check_and_get_buffer(buff, bytes);
		for (int i = bytes - 1; i >= 0; i--) {
			result.operator_assign(result.add( tmp[bytes - 1 - i] << i*8 ));
		}
		buff.increase_pos(bytes);
	}

	private static void decode_ulong_long_int_cbor(final TTCN_Buffer buff, final int bytes, final AtomicLong value) {
		value.set(0);
		final byte[] tmp = check_and_get_buffer(buff, bytes);
		for (int i = bytes - 1; i >= 0; i--) {
			value.getAndAdd( tmp[bytes - 1 - i] << i*8 );
		}
		buff.increase_pos(bytes);
	}

	private static void decode_integer_cbor(final TTCN_Buffer buff, final int minor_type, final TitanInteger result) {
		if (minor_type <= 23) {
			result.operator_assign( minor_type );
		} else if (minor_type == 24) { // A number in 8 bits
			decode_uint_cbor(buff, 1, result);
		} else if (minor_type == 25) { // A number in 16 bits
			decode_uint_cbor(buff, 2, result);
		} else if (minor_type == 26) { // A number in 32 bits
			decode_uint_cbor(buff, 4, result);
		} else if (minor_type == 27) { // A number in 64 bits
			decode_int_cbor(buff, 8, result);
		}
	}

	private static void decode_bytestring_cbor(final TTCN_Buffer buff, final JSON_Tokenizer tok, final int minor_type, final int tag) {
		final TitanInteger length = new TitanInteger();
		decode_integer_cbor(buff, minor_type, length);
		final byte[] tmp = check_and_get_buffer(buff, length.get_int());
		final TitanOctetString os = new TitanOctetString(tmp);
		buff.increase_pos(length.get_int());
		TitanCharString cs = new TitanCharString();
		if (tag == 22 || tag == 23 || tag == 2 || tag == 3) { // base64 or base64url or +-bigint
			cs = AdditionalFunctions.encode_base64(os);
			// The difference between the base64 and base64url encoding is that the
			// + is replaced with -, the / replaced with _ and the trailing = padding
			// is removed.
			if (tag != 22) { // if tag is not base64 >--> base64url
				String data = cs.get_value().toString();
				data = data.replace('+', '-').replace('/', '_');
				// Max 2 padding = is possible
				if ( data.length() > 0 && data.charAt(data.length()-1) == '=') {
					data = data.substring(0, data.length() - 1 );
				}
				if ( data.length() > 0 && data.charAt(data.length()-1) == '=') {
					data = data.substring(0, data.length() - 1 );
				}
				cs = new TitanCharString(data);
			}
		} else if (tag == 21) { // base16
			cs = AdditionalFunctions.oct2str(os);
		}
		// If the bignum encoded as bytestring is negative the tilde is needed before
		// the base64url encoding
		final String tmp_str = MessageFormat.format("\"{0}{1}\"", tag == 3 ? "~" : "", cs);
		tok.put_next_token(json_token_t.JSON_TOKEN_STRING, tmp_str);
	}

	// RAW descriptor for raw encoding
	private static final TTCN_RAWdescriptor cbor_float_raw_ = new TTCN_RAWdescriptor(64, raw_sign_t.SG_NO, raw_order_t.ORDER_LSB,
			raw_order_t.ORDER_LSB, raw_order_t.ORDER_LSB, raw_order_t.ORDER_LSB, ext_bit_t.EXT_BIT_NO, raw_order_t.ORDER_LSB,
			raw_order_t.ORDER_LSB, top_bit_order_t.TOP_BIT_INHERITED, 0, 0, 0, 8, 0, null, -1, CharCoding.UNKNOWN, null, false);
	// originally { null, null, cbor_float_raw_, null, null, null, null, TTCN_Typedescriptor.DONTCARE }
	private static final TTCN_Typedescriptor cbor_float_descr_ = new TTCN_Typedescriptor( null, null, cbor_float_raw_, null, null );

	public static void json2cbor_coding(final TTCN_Buffer buff, final JSON_Tokenizer tok, final AtomicInteger num_of_items) {
		final AtomicReference<json_token_t> token = new AtomicReference<json_token_t>();
		final StringBuilder content = new StringBuilder();
		final AtomicInteger len = new AtomicInteger();
		int prev_pos = tok.get_buf_pos();
		tok.get_next_token(token, content, len);
		switch(token.get()) {
		case JSON_TOKEN_NUMBER: {
			final String str = content.toString();
			final int curr_pos = tok.get_buf_pos();
			tok.set_buf_pos(prev_pos);
			final AtomicBoolean is_float = new AtomicBoolean(false);
			tok.check_for_number(is_float);
			tok.set_buf_pos(curr_pos);
			if (is_float.get()) {
				int c = 7 << 5; // Major type 7
				c += 27; // Minor type 27 (64 bit floating point) always
				buff.put_c((byte)c);
				final TitanFloat f = AdditionalFunctions.str2float(str);
				f.encode(cbor_float_descr_, buff, TTCN_EncDec.coding_type.CT_RAW, 0);
			} else {
				final int c = 0; // Major type 0
				final TitanInteger int_num = AdditionalFunctions.str2int(str);
				encode_int_cbor(buff, c, int_num);
			}
			num_of_items.incrementAndGet();
			break;
		}
		case JSON_TOKEN_STRING:
		case JSON_TOKEN_NAME: {
			final int c = 3 << 5; // Major type 3
			final TitanInteger length = new TitanInteger( token.get() == json_token_t.JSON_TOKEN_NAME ? len.get() : len.get() - 2 ); // 2 "-s
			encode_int_cbor(buff, c, length);
			final String str = token.get() == json_token_t.JSON_TOKEN_NAME ? content.toString() : content.substring(1, content.length()-1); // Remove "-s
			buff.put_string(new TitanCharString(str));
			num_of_items.incrementAndGet();
			break;
		}
		case JSON_TOKEN_ARRAY_START: {
			final int c = 4 << 5; // Major type 4
			final AtomicInteger nof_items = new AtomicInteger(0);
			final TTCN_Buffer sub_buff = new TTCN_Buffer();
			tok.get_next_token(token, null, null);
			prev_pos = tok.get_buf_pos();
			while (prev_pos != 0) {
				if (token.get() != json_token_t.JSON_TOKEN_ARRAY_END) {
					tok.set_buf_pos(prev_pos);
					json2cbor_coding(sub_buff, tok, nof_items);
				} else {
					final TitanInteger num = new TitanInteger(nof_items.get());
					encode_int_cbor(buff, c, num);
					buff.put_buf(sub_buff);
					break;
				}
				tok.get_next_token(token, null, null);
				prev_pos = tok.get_buf_pos();
			}
			num_of_items.incrementAndGet();
			break;
		}
		case JSON_TOKEN_ARRAY_END:
			throw new TtcnError("Unexpected array end character while encoding using json2cbor().");
		case JSON_TOKEN_OBJECT_START: {
			final int c = 5 << 5; // Major type 5
			final AtomicInteger nof_items = new AtomicInteger(0);
			final TTCN_Buffer sub_buff = new TTCN_Buffer();
			tok.get_next_token(token, null, null);
			prev_pos = tok.get_buf_pos();
			while (prev_pos != 0) {
				if (token.get() != json_token_t.JSON_TOKEN_OBJECT_END) { // todo hibas json eseten vegtelen ciklus?
					tok.set_buf_pos(prev_pos);
					json2cbor_coding(sub_buff, tok, nof_items);
				} else {
					final TitanInteger num = new TitanInteger(nof_items.get() / 2); // num is the number of key-value pairs
					encode_int_cbor(buff, c, num);
					buff.put_buf(sub_buff);
					break;
				}
				tok.get_next_token(token, null, null);
				prev_pos = tok.get_buf_pos();
			}
			num_of_items.incrementAndGet();
			break;
		}
		case JSON_TOKEN_OBJECT_END:
			throw new TtcnError("Unexpected object end character while encoding using json2cbor().");
		case JSON_TOKEN_LITERAL_FALSE:
		case JSON_TOKEN_LITERAL_TRUE:
		case JSON_TOKEN_LITERAL_NULL: {
			final int c = 7 << 5; // Major type 7
			int i = 0;
			if (token.get() == json_token_t.JSON_TOKEN_LITERAL_FALSE) {
				i = 20;
			} else if (token.get() == json_token_t.JSON_TOKEN_LITERAL_TRUE) {
				i = 21;
			} else if (token.get() == json_token_t.JSON_TOKEN_LITERAL_NULL) {
				i = 22;
			}
			encode_int_cbor(buff, c, new TitanInteger(i));
			num_of_items.incrementAndGet();
			break;
		}
		default:
			throw new TtcnError("Unexpected json token " + token.get() + ", while encoding using json2cbor().");
		}
	}

	public static void cbor2json_coding(final TTCN_Buffer buff, final JSON_Tokenizer tok, final boolean in_object) {
		byte type = check_and_get_buffer(buff, 1)[0];
		buff.increase_pos(1);
		final int major_type = type >> 5; // First 3 bit of byte
		int minor_type = type & 0x1F; // Get the last 5 bits
		switch(major_type) {
		case 0: { // Integer
			final TitanInteger i = new TitanInteger();
			decode_integer_cbor(buff, minor_type, i);
			if (i.is_native()) {
				final String tmp_str = Integer.toString(i.get_int());
				tok.put_next_token(json_token_t.JSON_TOKEN_NUMBER, tmp_str);
			} else {
				final String tmp_str = i.get_BigInteger().toString();
				tok.put_next_token(json_token_t.JSON_TOKEN_NUMBER, tmp_str);
			}
			break;
		}
		case 1: // Negative integer
			switch (minor_type) {
			case 24: { // Integer on 1 byte
				final TitanInteger num = new TitanInteger();
				decode_uint_cbor(buff, 1, num);
				final TitanInteger i = (num.add(1).mul(-1));
				final String tmp_str = (i.is_native() ? Integer.toString(i.get_int()) : i.get_BigInteger().toString());
				tok.put_next_token(json_token_t.JSON_TOKEN_NUMBER, tmp_str);
				break;
			}
			case 25: { // Integer on 2 byte
				final TitanInteger num = new TitanInteger();
				decode_uint_cbor(buff, 2, num);
				final TitanInteger i = (num.add(1).mul(-1));
				final String tmp_str = (i.is_native() ? Integer.toString(i.get_int()) : i.get_BigInteger().toString());
				tok.put_next_token(json_token_t.JSON_TOKEN_NUMBER, tmp_str);
				break;
			}
			case 26: { // Integer on 4 byte
				final AtomicLong num = new AtomicLong();
				decode_ulong_long_int_cbor(buff, 4, num);
				final TitanInteger i = new TitanInteger();
				i.operator_assign((int) ((num.get()+1)*-1));
				final String tmp_str = (i.is_native() ? Integer.toString(i.get_int()) : i.get_BigInteger().toString());
				tok.put_next_token(json_token_t.JSON_TOKEN_NUMBER, tmp_str);
				break;
			}
			case 27: { // Integer on 8 byte
				final TitanInteger i = new TitanInteger();
				decode_int_cbor(buff, 8, i);
				i.operator_assign(i.add(1));
				i.operator_assign(i.mul(-1));
				final String tmp_str = (i.is_native() ? Integer.toString(i.get_int()) : i.get_BigInteger().toString());
				tok.put_next_token(json_token_t.JSON_TOKEN_NUMBER, tmp_str);
				break;
			}
			default:
				if (minor_type < 24) { // Integer  0 <= num <= 24
					final String tmp_str = Integer.toString((minor_type+1)*-1);
					tok.put_next_token(json_token_t.JSON_TOKEN_NUMBER, tmp_str);
				}
			}
			break;
		case 2: // Bytestring, encoded into a base64url json string
			decode_bytestring_cbor(buff, tok, minor_type, 23); // base64url by default
			break;
		case 3: { // String
			final TitanInteger length = new TitanInteger();
			decode_integer_cbor(buff, minor_type, length);
			final byte[] tmp = check_and_get_buffer(buff, length.get_int());
			final String json_str = new String(tmp);
			if (in_object) {
				tok.put_next_token(json_token_t.JSON_TOKEN_NAME, json_str);
			} else {
				final String tmp_str = "\"" + json_str +"\"";
				tok.put_next_token(json_token_t.JSON_TOKEN_STRING, tmp_str);
			}
			buff.increase_pos(length.get_int());
			break;
		}
		case 4: { // Array
			tok.put_next_token(json_token_t.JSON_TOKEN_ARRAY_START, null);
			final TitanInteger num_of_items = new TitanInteger();
			decode_integer_cbor(buff, minor_type, num_of_items);
			for (int i = 0; i < num_of_items.get_int(); i = i + 1) {
				cbor2json_coding(buff, tok, false);
			}
			tok.put_next_token(json_token_t.JSON_TOKEN_ARRAY_END, null);
			break;
		}
		case 5: { // Object
			tok.put_next_token(json_token_t.JSON_TOKEN_OBJECT_START, null);
			final TitanInteger num_of_items = new TitanInteger();
			decode_integer_cbor(buff, minor_type, num_of_items);
			final int num_of_pairs = num_of_items.get_int() * 2; // Number of all keys and values
			for (int i = 0; i < num_of_pairs; i = i + 1) {
				// whether to put : or , after the next token
				cbor2json_coding(buff, tok, i % 2 == 0);
			}
			tok.put_next_token(json_token_t.JSON_TOKEN_OBJECT_END, null);
			break;
		}
		case 6: {
			final int tag = minor_type;
			switch(tag) {
			case 2: // Positive bignum
			case 3: // Negative bignum
			case 21: // bytestring as base16
			case 22: // bytestring as base64
			case 23: // bytestring as base64url
				type = check_and_get_buffer(buff, 1)[0];
				buff.increase_pos(1);
				minor_type = type & 0x1F; // Get the last 5 bits
				decode_bytestring_cbor(buff, tok, minor_type, tag);
				break;
			default:
				cbor2json_coding(buff, tok, in_object);
			}
			break;
		}
		case 7: // Other values
			switch (minor_type) {
			case 20: // False
				tok.put_next_token(json_token_t.JSON_TOKEN_LITERAL_FALSE, null);
				break;
			case 21: // True
				tok.put_next_token(json_token_t.JSON_TOKEN_LITERAL_TRUE, null);
				break;
			case 22: // null
				tok.put_next_token(json_token_t.JSON_TOKEN_LITERAL_NULL, null);
				break;
			case 25: { // Half precision float 16 bit
				// Decoding algorithm from the standard
				final byte[] halfp = check_and_get_buffer(buff, 2);
				buff.increase_pos(2);
				final int half = (halfp[0] << 8) + (halfp[1] & 0xFF);
				final int exp = (half >> 10) & 0x1f;
				final int mant = half & 0x3ff;
				double val;
				if (exp == 0) {
					val = Math.pow(mant, -24);
				} else if (exp != 31) {
					val = Math.pow(mant + 1024, exp - 25);
				} else {
					val = mant == 0 ? Double.POSITIVE_INFINITY : Double.NaN;
				}
				val = (half & 0x8000) != 0 ? -val : val;
				final TitanFloat f = new TitanFloat(val);
				f.JSON_encode(cbor_float_descr_, tok);
				break;
			}
			case 26: { // Single precision float 32bit
				final TitanOctetString os = new TitanOctetString(check_and_get_buffer(buff, 4));
				buff.increase_pos(4);
				final TitanInteger i = AdditionalFunctions.oct2int(os);
				final TitanFloat f = AdditionalFunctions.int2float(i);
				f.JSON_encode(cbor_float_descr_, tok);
				break;
			}
			case 27: { // Double precision float 64bit
				cbor_float_raw_.fieldlength = 64;
				final TitanFloat f = new TitanFloat();
				final TitanOctetString os = new TitanOctetString(check_and_get_buffer(buff, 8));
				final TitanInteger i = AdditionalFunctions.oct2int(os);
				if (i.get_long() != 0x7FF8000000000000L) { // NAN
					f.decode(cbor_float_descr_, buff, TTCN_EncDec.coding_type.CT_RAW, 0);
					f.JSON_encode(cbor_float_descr_, tok);
				} else {
					tok.put_next_token(json_token_t.JSON_TOKEN_STRING, "\"not_a_number\"");
					buff.increase_pos(8);
				}
				break;
			}
			default: {
				// put the simple value into the the json
				if (minor_type >= 0 && minor_type <= 23) {
					final String tmp_str = Integer.toString(minor_type);
					tok.put_next_token(json_token_t.JSON_TOKEN_NUMBER, tmp_str);
				} else if (minor_type == 24) { // The value is on the next byte
					final int simple_value = buff.get_read_data()[0];
					buff.increase_pos(1);
					final String tmp_str = Integer.toString(simple_value);
					tok.put_next_token(json_token_t.JSON_TOKEN_NUMBER, tmp_str);
				}
			}
			}
			break;
		default:
			throw new TtcnError("Unexpected major type " + major_type + " while decoding using cbor2json().");
		}
	}

	////////////////////////////////////////////////////////////////////////////////
	////  BSON conversion
	////////////////////////////////////////////////////////////////////////////////

	private static final TTCN_RAWdescriptor bson_float_raw_ = new TTCN_RAWdescriptor(64, raw_sign_t.SG_NO, raw_order_t.ORDER_MSB,
			raw_order_t.ORDER_LSB, raw_order_t.ORDER_LSB, raw_order_t.ORDER_LSB, ext_bit_t.EXT_BIT_NO, raw_order_t.ORDER_LSB,
			raw_order_t.ORDER_LSB, top_bit_order_t.TOP_BIT_INHERITED, 0, 0, 0, 8, 0, null, -1, CharCoding.UNKNOWN, null, false);
	// originally { null, null, bson_float_raw_, null, null, null, null, TTCN_Typedescriptor.DONTCARE }
	private static final TTCN_Typedescriptor bson_float_descr_ = new TTCN_Typedescriptor( null, null, bson_float_raw_, null, null );

	// Never use buff.get_read_data() without checking if it has enough bytes in the
	// buffer.
	private static byte[] check_and_get_buffer_bson(final TTCN_Buffer buff, final int bytes) {
		if (bytes < 0) {
			throw new TtcnError("Incorrect length byte received: " + bytes + "%d, while decoding using bson2json()");
		}
		if (buff.get_pos() + bytes > buff.get_len()) {
			throw new TtcnError("Not enough bytes in bytestream while decoding using bson2json().");
		}
		return buff.get_read_data();
	}

	private static void encode_int_bson(final TTCN_Buffer buff, final TitanInteger int_num, final TitanInteger length) {
		if (int_num.is_native()) { // 32 bit
			length.operator_assign(length.add(4));
			final int value = int_num.get_int();
			for (int i = 0; i < 4; i++) {
				buff.put_c((byte)(value >> i*8));
			}
		} else {
			final BigInteger bn = int_num.get_BigInteger();
			final byte byteArray[] = bn.toByteArray();
			final int bn_length = byteArray.length; //originally BN_num_bytes(bn);
			long long_int = 0;
			int bytes = 0;
			if (bn_length <= 4) { // 32 bit
				bytes = 4;
				long_int = int_num.get_long();
			} else if (bn_length <= 8) { //64 bit
				bytes = 8;
				long_int = int_num.get_long();
			} else {
				// The standard encodes max 64 bits
				throw new TtcnError("An integer value which cannot be represented on 64bits cannot be encoded using json2bson()");
			}
			for (int i = 0; i < bytes; i++) {
				buff.put_c((byte)(long_int >> i*8));
			}
			length.operator_assign(length.add(bytes));
		}
	}

	private static TitanInteger decode_int_bson(final TTCN_Buffer buff, final int bytes) {
		final byte[] uc = check_and_get_buffer_bson(buff, bytes);
		buff.increase_pos(bytes);
		if (bytes <= 4) { //32 bit
			int value = 0;
			for (int i = 0; i < 4; i++) {
				value += uc[i] << i*8;
			}
			return new TitanInteger(value);
		} else if (bytes <= 8) {
			final TTCN_Buffer tmp_buf = new TTCN_Buffer();
			for (int i = 0; i < bytes; i++) {
				tmp_buf.put_c(uc[bytes-i-1]);
			}
			final TitanOctetString os = new TitanOctetString();
			tmp_buf.get_string(os);
			final TitanInteger value = AdditionalFunctions.oct2int(os);
			return value;
		} else {
			throw new TtcnError("An integer value larger than 64 bytes cannot be decoded using bson2json()");
		}
	}

	private static void put_name(final TTCN_Buffer buff, final TitanInteger length, final TitanCharString name, final boolean in_array) {
		if (in_array) {
			buff.put_cs(name);
			buff.put_c((byte) 0); // Closing 0
			length.operator_assign(length.add(name.lengthof()).add(1));
			// TODO: is it very slow?
			// Increment index
			TitanInteger num = AdditionalFunctions.str2int(name);
			num = num.add(1);
			name.operator_assign(AdditionalFunctions.int2str(num));
		} else {
			buff.put_cs(name);
			buff.put_c((byte) 0); // Closing 0
			length.operator_assign(length.add(name.lengthof()).add(1));
		}
	}

	private static void get_name(final TTCN_Buffer buff, final JSON_Tokenizer tok, final boolean in_array) {
		final byte[] uc = buff.get_read_data();
		// Copy until closing 0
		final String tmp_str = new String(uc);
		if (!in_array) { // We dont need name when in array
			tok.put_next_token(json_token_t.JSON_TOKEN_NAME, tmp_str);
		}
		buff.increase_pos(tmp_str.length()+1);
	}

	private static boolean encode_bson_binary(final TTCN_Buffer buff, final JSON_Tokenizer tok, final TitanInteger length) {
		final AtomicReference<json_token_t> token = new AtomicReference<json_token_t>();
		final StringBuilder content = new StringBuilder();
		final AtomicInteger len = new AtomicInteger();
		// Check if this is really binary
		tok.get_next_token(token, content, len);
		if (token.get() != json_token_t.JSON_TOKEN_STRING) {
			return false;
		}
		final TitanCharString cs2 = new TitanCharString(content.substring(1, content.length()-1));
		tok.get_next_token(token, content, len);
		if (token.get() != json_token_t.JSON_TOKEN_NAME) {
			return false;
		}
		final TitanCharString cs3 = new TitanCharString(content);
		if (cs3.operator_not_equals("$type")) {
			return false;
		}

		tok.get_next_token(token, content, len);
		if (token.get() != json_token_t.JSON_TOKEN_STRING) {
			return false;
		}
		final TitanCharString cs4 = new TitanCharString(content.substring(1, content.length()-1));
		if (cs4.lengthof().operator_not_equals(2)) {
			return false;
		}

		tok.get_next_token(token, content, len);
		if (token.get() != json_token_t.JSON_TOKEN_OBJECT_END) {
			return false;
		}

		buff.put_c((byte) 5);
		length.operator_assign(length.add(1));
		// We do not know the name here. It will be inserted later.
		final TitanOctetString os = AdditionalFunctions.decode_base64(cs2);
		final TitanInteger os_len = os.lengthof();
		encode_int_bson(buff, os_len, length);
		try {
			final int type = Integer.parseInt(cs4.get_value().toString(), 16);
			buff.put_c((byte) type);
		} catch (NumberFormatException e) {
			throw new TtcnError("Incorrect binary format while encoding with json2bson()");
		}
		length.operator_assign(length.add(1));
		buff.put_os(os);
		length.operator_assign(length.add(os_len));
		return true;
	}

	private static boolean encode_bson_date(final TTCN_Buffer buff, final JSON_Tokenizer tok, final TitanInteger length) {
		final AtomicReference<json_token_t> token = new AtomicReference<json_token_t>();
		final StringBuilder content = new StringBuilder();
		final AtomicInteger len = new AtomicInteger();
		tok.get_next_token(token, null, null);
		if (token.get() != json_token_t.JSON_TOKEN_OBJECT_START) {
			return false;
		}

		tok.get_next_token(token, content, len);
		if (token.get() != json_token_t.JSON_TOKEN_NAME) {
			return false;
		}

		final TitanCharString cs = new TitanCharString(content);
		if (cs.operator_not_equals("$numberLong")) {
			return false;
		}

		tok.get_next_token(token, content, len);
		if (token.get() != json_token_t.JSON_TOKEN_NUMBER) {
			return false;
		}

		final TitanCharString cs2 = new TitanCharString(content);

		tok.get_next_token(token, null, null);
		if (token.get() != json_token_t.JSON_TOKEN_OBJECT_END) {
			return false;
		}

		tok.get_next_token(token, null, null);
		if (token.get() != json_token_t.JSON_TOKEN_OBJECT_END) {
			return false;
		}

		final TitanInteger int_num = AdditionalFunctions.str2int(cs2);
		buff.put_c((byte) 9); // datetime
		length.operator_assign(length.add(1));
		// We do not know the name here. It will be inserted later.
		// Encode on 64 bit
		final long long_int = int_num.get_long();
		for (int i = 0; i < 8; i++) {
			buff.put_c((byte)(long_int >> i*8));
		}
		length.operator_assign(length.add(8));
		return true;
	}

	private static boolean encode_bson_timestamp(final TTCN_Buffer buff, final JSON_Tokenizer tok, final TitanInteger length) {
		final AtomicReference<json_token_t> token = new AtomicReference<json_token_t>();
		final StringBuilder content = new StringBuilder();
		final AtomicInteger len = new AtomicInteger();
		tok.get_next_token(token, null, null);
		if (token.get() != json_token_t.JSON_TOKEN_OBJECT_START) {
			return false;
		}

		tok.get_next_token(token, content, len);
		if (token.get() != json_token_t.JSON_TOKEN_NAME) {
			return false;
		}

		final TitanCharString cs = new TitanCharString(content);
		if (cs.operator_not_equals("t")) {
			return false;
		}

		tok.get_next_token(token, content, len);
		if (token.get() != json_token_t.JSON_TOKEN_NUMBER) {
			return false;
		}

		final TitanCharString cs2 = new TitanCharString(content);

		tok.get_next_token(token, content, len);
		if (token.get() != json_token_t.JSON_TOKEN_NAME) {
			return false;
		}

		final TitanCharString cs3 = new TitanCharString(content);
		if (cs3.operator_not_equals("i")) {
			return false;
		}

		tok.get_next_token(token, content, len);
		if (token.get() != json_token_t.JSON_TOKEN_NUMBER) {
			return false;
		}

		final TitanCharString cs4 = new TitanCharString(content);

		tok.get_next_token(token, null, null);
		if (token.get() != json_token_t.JSON_TOKEN_OBJECT_END) {
			return false;
		}
		tok.get_next_token(token, null, null);
		if (token.get() != json_token_t.JSON_TOKEN_OBJECT_END) {
			return false;
		}

		final TitanInteger timestamp = AdditionalFunctions.str2int(cs2);
		final TitanInteger increment = AdditionalFunctions.str2int(cs4);
		buff.put_c((byte) 17);
		length.operator_assign(length.add(1));
		// We do not know the name here. It will be inserted later.
		encode_int_bson(buff, increment, length);
		encode_int_bson(buff, timestamp, length);
		return true;
	}

	private static boolean encode_bson_regex(final TTCN_Buffer buff, final JSON_Tokenizer tok, final TitanInteger length) {
		final AtomicReference<json_token_t> token = new AtomicReference<json_token_t>();
		final StringBuilder content = new StringBuilder();
		final AtomicInteger len = new AtomicInteger();
		tok.get_next_token(token, content, len);
		if (token.get() != json_token_t.JSON_TOKEN_STRING) {
			return false;
		}

		final TitanCharString regex = new TitanCharString(content.substring(1, content.length()-1));
		tok.get_next_token(token, content, len);
		if (token.get() != json_token_t.JSON_TOKEN_NAME) {
			return false;
		}

		final TitanCharString cs2 = new TitanCharString(content);
		if (cs2.operator_not_equals("$options")) {
			return false;
		}

		tok.get_next_token(token, content, len);
		if (token.get() != json_token_t.JSON_TOKEN_STRING) {
			return false;
		}

		final TitanCharString options = new TitanCharString(content.substring(1, content.length()-1));
		tok.get_next_token(token, null, null);
		if (token.get() != json_token_t.JSON_TOKEN_OBJECT_END) {
			return false;
		}

		buff.put_c((byte) 11);
		length.operator_assign(length.add(1));
		// We do not know the name here. It will be inserted later.
		buff.put_cs(regex);
		length.operator_assign(length.add(regex.lengthof()));
		buff.put_c((byte) 0); // Closing 0
		length.operator_assign(length.add(1));
		buff.put_cs(options);
		length.operator_assign(length.add(options.lengthof()));
		buff.put_c((byte) 0); // Closing 0
		length.operator_assign(length.add(1));
		return true;
	}

	private static boolean encode_bson_oid(final TTCN_Buffer buff, final JSON_Tokenizer tok, final TitanInteger length) {
		final AtomicReference<json_token_t> token = new AtomicReference<json_token_t>();
		final StringBuilder content = new StringBuilder();
		final AtomicInteger len = new AtomicInteger();
		tok.get_next_token(token, content, len);
		if (token.get() != json_token_t.JSON_TOKEN_STRING) {
			return false;
		}

		final TitanCharString id = new TitanCharString(content.substring(1, content.length()-1));
		if (id.lengthof().operator_not_equals(24)) {
			return false;
		}

		tok.get_next_token(token, null, null);
		if (token.get() != json_token_t.JSON_TOKEN_OBJECT_END) {
			return false;
		}

		buff.put_c((byte) 7);
		length.operator_assign(length.add(1));
		// We do not know the name here. It will be inserted later.
		final char hex[] = new char[12];
		for (int i = 0; i < 24; i = i + 2) {
			final String octDigit = "" + id.get_at(i).get_char() + id.get_at(i+1).get_char();
			try {
				final int value = Integer.parseInt(octDigit, 16);
				hex[i/2] = (char) value;
			} catch (NumberFormatException e) {
				throw new TtcnError("Incorrect binary format while encoding with json2bson()");
			}
		}
		buff.put_s(hex);
		length.operator_assign(length.add(12));
		return true;
	}

	private static boolean encode_bson_ref(final TTCN_Buffer buff, final JSON_Tokenizer tok, final TitanInteger length) {
		final AtomicReference<json_token_t> token = new AtomicReference<json_token_t>();
		final StringBuilder content = new StringBuilder();
		final AtomicInteger len = new AtomicInteger();
		tok.get_next_token(token, content, len);
		if (token.get() != json_token_t.JSON_TOKEN_STRING) {
			return false;
		}

		final TitanCharString name = new TitanCharString(content.substring(1, content.length()-1));
		tok.get_next_token(token, content, len);
		if (token.get() != json_token_t.JSON_TOKEN_NAME) {
			return false;
		}

		final TitanCharString cs = new TitanCharString(content);
		if (cs.operator_not_equals("$id")) {
			return false;
		}

		tok.get_next_token(token, content, len);
		if (token.get() != json_token_t.JSON_TOKEN_STRING) {
			return false;
		}

		final TitanCharString id = new TitanCharString(content.substring(1, content.length()-1));
		if (id.lengthof().operator_not_equals(24)) {
			return false;
		}

		tok.get_next_token(token, null, null);
		if (token.get() != json_token_t.JSON_TOKEN_OBJECT_END) {
			return false;
		}

		buff.put_c((byte) 12);
		length.operator_assign(length.add(1));
		// We do not know the name here. It will be inserted later.
		final TitanInteger name_length = name.lengthof().add(1);
		encode_int_bson(buff, name_length, length);
		buff.put_cs(name);
		buff.put_c((byte) 0); // Closing 0
		length.operator_assign(length.add(name_length));
		final char hex[] = new char[12];
		for (int i = 0; i < 24; i = i + 2) {
			final String octDigit = "" + id.get_at(i).get_char() + id.get_at(i+1).get_char();
			try {
				final int value = Integer.parseInt(octDigit, 16);
				hex[i/2] = (char) value;
			} catch (NumberFormatException e) {
				throw new TtcnError("Incorrect binary format while encoding with json2bson()");
			}
		}
		buff.put_s(hex);
		length.operator_assign(length.add(12));
		return true;
	}

	private static boolean encode_bson_undefined(final TTCN_Buffer buff, final JSON_Tokenizer tok, final TitanInteger length) {
		final AtomicReference<json_token_t> token = new AtomicReference<json_token_t>();
		final StringBuilder content = new StringBuilder();
		final AtomicInteger len = new AtomicInteger();
		tok.get_next_token(token, content, len);
		if (token.get() != json_token_t.JSON_TOKEN_LITERAL_TRUE) {
			return false;
		}

		tok.get_next_token(token, null, null);
		if (token.get() != json_token_t.JSON_TOKEN_OBJECT_END) {
			return false;
		}

		buff.put_c((byte) 6);
		length.operator_assign(length.add(1));
		// We do not know the name here. It will be inserted later.
		return true;
	}

	private static boolean encode_bson_minkey(final TTCN_Buffer buff, final JSON_Tokenizer tok, final TitanInteger length) {
		final AtomicReference<json_token_t> token = new AtomicReference<json_token_t>();
		final StringBuilder content = new StringBuilder();
		final AtomicInteger len = new AtomicInteger();
		tok.get_next_token(token, content, len);
		if (token.get() != json_token_t.JSON_TOKEN_NUMBER) {
			return false;
		}

		final TitanCharString cs = new TitanCharString(content);
		if (cs.operator_not_equals("1")) {
			return false;
		}

		tok.get_next_token(token, null, null);
		if (token.get() != json_token_t.JSON_TOKEN_OBJECT_END) {
			return false;
		}

		buff.put_c((byte) 255);
		length.operator_assign(length.add(1));
		// We do not know the name here. It will be inserted later.
		return true;
	}

	private static boolean encode_bson_maxkey(final TTCN_Buffer buff, final JSON_Tokenizer tok, final TitanInteger length) {
		final AtomicReference<json_token_t> token = new AtomicReference<json_token_t>();
		final StringBuilder content = new StringBuilder();
		final AtomicInteger len = new AtomicInteger();
		tok.get_next_token(token, content, len);
		if (token.get() != json_token_t.JSON_TOKEN_NUMBER) {
			return false;
		}

		final TitanCharString cs = new TitanCharString(content);
		if (cs.operator_not_equals("1")) {
			return false;
		}

		tok.get_next_token(token, null, null);
		if (token.get() != json_token_t.JSON_TOKEN_OBJECT_END) {
			return false;
		}

		buff.put_c((byte) 127);
		length.operator_assign(length.add(1));
		// We do not know the name here. It will be inserted later.
		return true;
	}

	private static boolean encode_bson_numberlong(final TTCN_Buffer buff, final JSON_Tokenizer tok, final TitanInteger length) {
		final AtomicReference<json_token_t> token = new AtomicReference<json_token_t>();
		final StringBuilder content = new StringBuilder();
		final AtomicInteger len = new AtomicInteger();
		tok.get_next_token(token, content, len);
		if (token.get() != json_token_t.JSON_TOKEN_STRING) {
			return false;
		}

		final TitanCharString cs = new TitanCharString(content.substring(1, content.length()-1));

		tok.get_next_token(token, null, null);
		if (token.get() != json_token_t.JSON_TOKEN_OBJECT_END) {
			return false;
		}

		buff.put_c((byte) 18);
		length.operator_assign(length.add(1));
		// We do not know the name here. It will be inserted later.
		final TitanInteger number = AdditionalFunctions.str2int(cs);
		final long value = number.get_long();
		for (int i = 0; i < 8; i++) {
			buff.put_c((byte)(value >> i*8));
		}
		length.operator_assign(length.add(8));
		return true;
	}

	private static boolean encode_bson_code_with_scope(final TTCN_Buffer buff, final JSON_Tokenizer tok, final TitanInteger length) {
		final AtomicReference<json_token_t> token = new AtomicReference<json_token_t>();
		final StringBuilder content = new StringBuilder();
		final AtomicInteger len = new AtomicInteger();
		tok.get_next_token(token, content, len);
		if (token.get() != json_token_t.JSON_TOKEN_STRING) {
			return false;
		}

		final TitanCharString cs = new TitanCharString(content.substring(1, content.length()-1));

		tok.get_next_token(token, content, len);
		if (token.get() != json_token_t.JSON_TOKEN_NAME) {
			return false;
		}

		final TitanCharString cs2 = new TitanCharString(content);
		if (cs2.operator_not_equals("$scope")) {
			return false;
		}

		final TitanInteger code_w_scope_length = new TitanInteger(0);
		final boolean is_special = false;
		final TitanCharString f_name = new TitanCharString();
		final TTCN_Buffer sub_buff = new TTCN_Buffer();
		json2bson_coding(sub_buff, tok, false, false, code_w_scope_length, f_name, is_special);

		tok.get_next_token(token, null, null);
		if (token.get() != json_token_t.JSON_TOKEN_OBJECT_END) {
			return false;
		}

		buff.put_c((byte) 15);
		length.operator_assign(length.add(1));
		// We do not know the name here. It will be inserted later.
		code_w_scope_length.add(code_w_scope_length.add(cs.lengthof().add(4 + 1)));
		encode_int_bson(buff, code_w_scope_length, code_w_scope_length);
		encode_int_bson(buff, cs.lengthof().add(1), length);
		buff.put_string(cs);
		buff.put_c((byte) 0); // Closing 0
		buff.put_buf(sub_buff);
		length.operator_assign(length.add(code_w_scope_length.sub(4))); // We added the length of cs twice
		return true;
	}

	public static void json2bson_coding(final TTCN_Buffer buff, final JSON_Tokenizer tok, boolean in_object, boolean in_array,
			final TitanInteger length, final TitanCharString obj_name, boolean is_special) {
		final AtomicReference<json_token_t> token = new AtomicReference<json_token_t>();
		final StringBuilder content = new StringBuilder();
		final AtomicInteger len = new AtomicInteger();
		int prev_pos = tok.get_buf_pos();
		tok.get_next_token(token, content, len);
		if (!in_object && token.get() != json_token_t.JSON_TOKEN_OBJECT_START && token.get() != json_token_t.JSON_TOKEN_ARRAY_START) {
			throw new TtcnError("Json document must be an object or array when encoding with json2bson()");
		}
		switch(token.get()) {
		case JSON_TOKEN_OBJECT_START: {
			TTCN_Buffer sub_buff = new TTCN_Buffer();
			final TitanInteger sub_len = new TitanInteger(0);
			TitanCharString subobj_name = new TitanCharString();
			if (obj_name.is_bound()) {
				subobj_name = obj_name;
			}
			tok.get_next_token(token, null, null);
			prev_pos = tok.get_buf_pos();
			while (prev_pos != 0) {
				if (token.get() != json_token_t.JSON_TOKEN_OBJECT_END) {
					tok.set_buf_pos(prev_pos);
					json2bson_coding(sub_buff, tok, true, false, sub_len, subobj_name, is_special);
					// We found a specially translated json
					if (is_special) {
						// The sub_buff contains the encoded bson except the obj_name.
						// We put it in here after the first byte
						final TTCN_Buffer tmp_buff = new TTCN_Buffer();
						tmp_buff.put_c(sub_buff.get_data()[0]);
						put_name(tmp_buff, sub_len, subobj_name, in_array);
						tmp_buff.put_s(Arrays.copyOfRange(sub_buff.get_data(), 1, sub_buff.get_data().length));
						sub_buff = tmp_buff;
						in_object = false;
						break;
					}
				} else {
					sub_buff.put_c((byte) 0);// Closing zero
					sub_len.operator_assign(sub_len.add(1));
					break;
				}
				tok.get_next_token(token, null, null);
				prev_pos = tok.get_buf_pos();
			}

			if (in_object) {
				final TTCN_Buffer tmp_buff = new TTCN_Buffer();
				tmp_buff.put_c((byte) 3); // embedded document
				length.operator_assign(length.add(1));
				put_name(tmp_buff, length, obj_name, in_array);
				encode_int_bson(tmp_buff, sub_len, sub_len);
				length.operator_assign(length.add(sub_len));
				tmp_buff.put_buf(sub_buff);
				sub_buff = tmp_buff;
			} else if (!is_special) {
				length.operator_assign(length.add(sub_len));
				encode_int_bson(buff, length, length);
			} else {
				length.operator_assign(length.add(sub_len));
				is_special = false;
			}
			buff.put_buf(sub_buff);
			break;
		}
		case JSON_TOKEN_OBJECT_END:
			throw new TtcnError("Unexpected object end character while encoding using json2bson().");
		case JSON_TOKEN_NAME: {
			final TitanCharString cs = new TitanCharString(content);
			prev_pos = tok.get_buf_pos();
			if (cs.operator_equals("$binary")) {
				is_special = encode_bson_binary(buff, tok, length);
			} else if (cs.operator_equals("$date")) {
				is_special = encode_bson_date(buff, tok, length);
			} else if (cs.operator_equals("$timestamp")) {
				is_special = encode_bson_timestamp(buff, tok, length);
			} else if (cs.operator_equals("$regex")) {
				is_special = encode_bson_regex(buff, tok, length);
			} else if (cs.operator_equals("$oid")) {
				is_special = encode_bson_oid(buff, tok, length);
			} else if (cs.operator_equals("$ref")) {
				is_special = encode_bson_ref(buff, tok, length);
			} else if (cs.operator_equals("$undefined")) {
				is_special = encode_bson_undefined(buff, tok, length);
			} else if (cs.operator_equals("$minKey")) {
				is_special = encode_bson_minkey(buff, tok, length);
			} else if (cs.operator_equals("$maxKey")) {
				is_special = encode_bson_maxkey(buff, tok, length);
			} else if (cs.operator_equals("$numberLong")) {
				is_special = encode_bson_numberlong(buff, tok, length);
			} else if (cs.operator_equals("$code")) {
				is_special = encode_bson_code_with_scope(buff, tok, length);
			} else {
				obj_name.operator_assign(cs);
			}
			if (!is_special) {
				tok.set_buf_pos(prev_pos);
				obj_name.operator_assign(cs);
			}
			break;
		}
		case JSON_TOKEN_STRING: {
			buff.put_c((byte) 2); // string
			length.operator_assign(length.add(1));
			put_name(buff, length, obj_name, in_array);
			encode_int_bson(buff, new TitanInteger(len.get()-1), length); // Remove "-s but add terminating null
			final String tmp_str = content.substring(1, content.length()-1); // Remove "-s
			buff.put_string(new TitanCharString(tmp_str));
			buff.put_c((byte) 0); // Closing 0
			length.operator_assign(length.add(len.get()-1)); // Remove "-s but add terminating null
			break;
		}
		case JSON_TOKEN_NUMBER: {
			final String str = content.toString();
			final int curr_pos = tok.get_buf_pos();
			tok.set_buf_pos(prev_pos);
			final AtomicBoolean is_float = new AtomicBoolean(false);
			tok.check_for_number(is_float);
			tok.set_buf_pos(curr_pos);
			if (is_float.get()) {
				buff.put_c((byte) 1); // 64bit float
				put_name(buff, length, obj_name, in_array);
				final TitanFloat f = AdditionalFunctions.str2float(str);
				f.encode(bson_float_descr_, buff, TTCN_EncDec.coding_type.CT_RAW, 0);
			} else {
				final TitanInteger int_num = AdditionalFunctions.str2int(str);
				if (int_num.is_native()) {
					buff.put_c((byte) 16); //32bit integer
					length.operator_assign(length.add(1));
				} else {
					buff.put_c((byte) 18); // 64bit integer
					length.operator_assign(length.add(1));
				}
				put_name(buff, length, obj_name, in_array);
				encode_int_bson(buff, int_num, length);
			}
			break;
		}
		case JSON_TOKEN_LITERAL_FALSE: {
			buff.put_c((byte) 8); // true or false
			put_name(buff, length, obj_name, in_array);
			buff.put_c((byte) 0); // false
			break;
		}
		case JSON_TOKEN_LITERAL_TRUE: {
			buff.put_c((byte) 8); // true or false
			put_name(buff, length, obj_name, in_array);
			buff.put_c((byte) 1); // true
			break;
		}
		case JSON_TOKEN_LITERAL_NULL: {
			buff.put_c((byte) 10); // null
			put_name(buff, length, obj_name, in_array);
			break;
		}
		case JSON_TOKEN_ARRAY_START: {
			if (!in_object) { // The top level json is an array
				in_object = true;
			} else {
				buff.put_c((byte) 4); // array
				length.operator_assign(length.add(1));
				put_name(buff, length, obj_name, in_array);
			}
			obj_name.operator_assign("0"); // arrays are objects but the key is a number which increases
			final TTCN_Buffer sub_buff = new TTCN_Buffer();
			final TitanInteger sub_length = new TitanInteger(0);
			tok.get_next_token(token, null, null);
			prev_pos = tok.get_buf_pos();
			while (prev_pos != 0) {
				if (token.get() != json_token_t.JSON_TOKEN_ARRAY_END) {
					tok.set_buf_pos(prev_pos);
					in_array = true;
					json2bson_coding(sub_buff, tok, in_object, in_array, sub_length, obj_name, is_special);
				} else {
					sub_buff.put_c((byte) 0);// Closing zero
					sub_length.operator_assign(sub_length.add(1));
					break;
				}
				tok.get_next_token(token, null, null);
				prev_pos = tok.get_buf_pos();
			}
			encode_int_bson(buff, sub_length, sub_length);
			length.operator_assign(length.add(sub_length));
			buff.put_buf(sub_buff);
			break;
		}
		default:
			throw new TtcnError("Unexpected json token " + token.get() + ", while encoding using json2bson().");
		}
	}

	public static void bson2json_coding(final TTCN_Buffer buff, final JSON_Tokenizer tok, final boolean in_object, final boolean in_array) {
		TitanInteger length = new TitanInteger(0);
		// Beginning of the document
		if (!in_object) {
			length = decode_int_bson(buff, 4);
			// Check if the input is long enough
			check_and_get_buffer_bson(buff, length.get_int()-4);
			tok.put_next_token(json_token_t.JSON_TOKEN_OBJECT_START, null);
			while (check_and_get_buffer_bson(buff, 1)[0] != 0) {
				bson2json_coding(buff, tok, true, in_array);
			}
			buff.increase_pos(1);
			tok.put_next_token(json_token_t.JSON_TOKEN_OBJECT_END, null);
		} else {
			final byte[] type = check_and_get_buffer_bson(buff, 1);
			buff.increase_pos(1);
			// There is always a name
			get_name(buff, tok, in_array);
			switch(type[0]) {
			case 0: // document end
				throw new TtcnError("Unexpected document end character while decoding with bson2json()");
			case 1: { // 64bit float
				final TitanFloat f = new TitanFloat();
				check_and_get_buffer_bson(buff, 8);
				f.decode(bson_float_descr_, buff, TTCN_EncDec.coding_type.CT_RAW, 0);
				f.JSON_encode(bson_float_descr_, tok);
				break;
			}
			case 13: // Javascript code. Decoded as string
			case 14: // Symbol. Decoded as string
			case 2: { // UTF8 string
				final TitanInteger len = decode_int_bson(buff, 4);
				// Get the value of the pair
				final byte[] uc = check_and_get_buffer_bson(buff, len.get_int());
				final String tmp_str = new String(uc);
				buff.increase_pos(len.get_int());
				final String tmp_str2 = "\"" + tmp_str + "\"";
				tok.put_next_token(json_token_t.JSON_TOKEN_STRING, tmp_str2);
				break;
			}
			case 3: { // Embedded document
				length = decode_int_bson(buff, 4);
				// Check if the input is long enough
				check_and_get_buffer_bson(buff, length.get_int()-4);
				tok.put_next_token(json_token_t.JSON_TOKEN_OBJECT_START, null);
				while (check_and_get_buffer_bson(buff, 1)[0] != 0) { // error message while converting
					bson2json_coding(buff, tok, in_object, false);
				}
				buff.increase_pos(1); // Skip the closing 0
				tok.put_next_token(json_token_t.JSON_TOKEN_OBJECT_END, null);
				break;
			}
			case 4: { // array
				length = decode_int_bson(buff, 4);
				// Check if the input is long enough
				check_and_get_buffer_bson(buff, length.get_int()-4);
				tok.put_next_token(json_token_t.JSON_TOKEN_ARRAY_START, null);
				while (check_and_get_buffer_bson(buff, 1)[0] != 0) { // error message while converting
					bson2json_coding(buff, tok, in_object, true);
				}
				buff.increase_pos(1); // Skip the closing 0
				tok.put_next_token(json_token_t.JSON_TOKEN_ARRAY_END, null);
				break;
			}
			case 5: { // bytestring
				// decode bytestring length
				final TitanInteger bytestr_length = decode_int_bson(buff, 4);
				final TitanOctetString os = new TitanOctetString(check_and_get_buffer_bson(buff, 1));
				buff.increase_pos(1);
				final TitanInteger typestr_type = AdditionalFunctions.oct2int(os);
				final String str_type = String.format("%02X", typestr_type.get_int());
				final TitanOctetString data = new TitanOctetString(check_and_get_buffer_bson(buff, bytestr_length.get_int()));
				buff.increase_pos(bytestr_length.get_int());
				final TitanCharString cs = AdditionalFunctions.encode_base64(data);
				final String data_str = "\"" + cs.get_value() + "\"";
				tok.put_next_token(json_token_t.JSON_TOKEN_OBJECT_START, null);
				tok.put_next_token(json_token_t.JSON_TOKEN_NAME, "$binary");
				tok.put_next_token(json_token_t.JSON_TOKEN_STRING, data_str);
				tok.put_next_token(json_token_t.JSON_TOKEN_NAME, "$type");
				tok.put_next_token(json_token_t.JSON_TOKEN_STRING, str_type);
				tok.put_next_token(json_token_t.JSON_TOKEN_OBJECT_END, null);
				break;
			}
			case 6: { // undefined
				tok.put_next_token(json_token_t.JSON_TOKEN_OBJECT_START, null);
				tok.put_next_token(json_token_t.JSON_TOKEN_NAME, "$undefined");
				tok.put_next_token(json_token_t.JSON_TOKEN_LITERAL_TRUE);
				tok.put_next_token(json_token_t.JSON_TOKEN_OBJECT_END, null);
				break;
			}
			case 7: { // oid
				final TitanOctetString os = new TitanOctetString(check_and_get_buffer_bson(buff, 12));
				final StringBuilder tmp_oct = new StringBuilder();
				for (int i = 0; i < 12; i++) {
					tmp_oct.append(String.format("%02X", os.get_at(i).get_nibble()));
				}
				final String str_hex = "\"" + tmp_oct + "\"";
				buff.increase_pos(12);
				tok.put_next_token(json_token_t.JSON_TOKEN_OBJECT_START, null);
				tok.put_next_token(json_token_t.JSON_TOKEN_NAME, "$oid");
				tok.put_next_token(json_token_t.JSON_TOKEN_STRING, str_hex);
				tok.put_next_token(json_token_t.JSON_TOKEN_OBJECT_END, null);
				break;
			}
			case 8: {  // true or false
				final byte[] uc = check_and_get_buffer_bson(buff, 1);
				if (uc[0] == 0) {
					tok.put_next_token(json_token_t.JSON_TOKEN_LITERAL_FALSE, null);
				} else {
					tok.put_next_token(json_token_t.JSON_TOKEN_LITERAL_TRUE, null);
				}
				buff.increase_pos(1);
				break;
			}
			case 9: { // datetime
				final TitanInteger date = decode_int_bson(buff, 8);
				final String tmp_str = Long.toString(date.get_long());
				tok.put_next_token(json_token_t.JSON_TOKEN_OBJECT_START, null);
				tok.put_next_token(json_token_t.JSON_TOKEN_NAME, "$date");
				tok.put_next_token(json_token_t.JSON_TOKEN_OBJECT_START, null);
				tok.put_next_token(json_token_t.JSON_TOKEN_NAME, "$numberLong");
				tok.put_next_token(json_token_t.JSON_TOKEN_NUMBER, tmp_str);
				tok.put_next_token(json_token_t.JSON_TOKEN_OBJECT_END, null);
				tok.put_next_token(json_token_t.JSON_TOKEN_OBJECT_END, null);
				break;
			}
			case 10: { // null
				tok.put_next_token(json_token_t.JSON_TOKEN_LITERAL_NULL, null);
				break;
			}
			case 11: { // regex
				// copy until closing 0
				byte[] uc = check_and_get_buffer_bson(buff, 1);
				String tmp_str = new String(uc);
				buff.increase_pos(tmp_str.length()+1);
				final String regex = "\"" + tmp_str + "\"";
				uc = check_and_get_buffer_bson(buff, 1);
				tmp_str = new String(uc);
				buff.increase_pos(tmp_str.length()+1);
				final String options = "\"" + tmp_str + "\"";
				tok.put_next_token(json_token_t.JSON_TOKEN_OBJECT_START, null);
				tok.put_next_token(json_token_t.JSON_TOKEN_NAME, "$regex");
				tok.put_next_token(json_token_t.JSON_TOKEN_STRING, regex);
				tok.put_next_token(json_token_t.JSON_TOKEN_NAME, "$options");
				tok.put_next_token(json_token_t.JSON_TOKEN_STRING, options);
				tok.put_next_token(json_token_t.JSON_TOKEN_OBJECT_END, null);
				break;
			}
			case 12: { // dbref
				final TitanInteger name_len = decode_int_bson(buff, 4);
				final byte[] uc = check_and_get_buffer_bson(buff, name_len.get_int());
				final String tmp_name = new String(uc);
				buff.increase_pos(name_len.get_int());
				final String tmp_str = "\"" + tmp_name + "\"";
				final TitanOctetString os = new TitanOctetString(check_and_get_buffer_bson(buff, 12));
				buff.increase_pos(12);
				final StringBuilder tmp_oct = new StringBuilder();
				for (int i = 0; i < 12; i++) {
					tmp_oct.append(String.format("%02X", os.get_at(i).get_nibble()));
				}
				final String str_hex = "\"" + tmp_oct + "\"";
				tok.put_next_token(json_token_t.JSON_TOKEN_OBJECT_START, null);
				tok.put_next_token(json_token_t.JSON_TOKEN_NAME, "$ref");
				tok.put_next_token(json_token_t.JSON_TOKEN_STRING, tmp_str);
				tok.put_next_token(json_token_t.JSON_TOKEN_NAME, "$id");
				tok.put_next_token(json_token_t.JSON_TOKEN_STRING, str_hex);
				tok.put_next_token(json_token_t.JSON_TOKEN_OBJECT_END, null);
				break;
			}
			case 15: { // code_with_scope
				TitanInteger len = decode_int_bson(buff, 4);
				check_and_get_buffer_bson(buff, len.get_int()-4); // len contains the length of itself
				len = decode_int_bson(buff, 4);
				final byte[] uc = check_and_get_buffer_bson(buff, len.get_int());
				final String tmp_str = new String(uc);
				final String tmp_str2 = "\"" + tmp_str + "\"";
				buff.increase_pos(len.get_int());
				tok.put_next_token(json_token_t.JSON_TOKEN_OBJECT_START, null);
				tok.put_next_token(json_token_t.JSON_TOKEN_NAME, "$code");
				tok.put_next_token(json_token_t.JSON_TOKEN_STRING, tmp_str2);
				tok.put_next_token(json_token_t.JSON_TOKEN_NAME, "$scope");
				bson2json_coding(buff, tok, false, false);
				tok.put_next_token(json_token_t.JSON_TOKEN_OBJECT_END, null);
				break;
			}
			case 16: { // 32bit integer
				final TitanInteger value = decode_int_bson(buff, 4);
				final String tmp_str = Integer.toString(value.get_int());
				tok.put_next_token(json_token_t.JSON_TOKEN_NUMBER, tmp_str);
				break;
			}
			case 17: { // timestamp
				final TitanInteger increment = decode_int_bson(buff, 4);
				final TitanInteger timestamp = decode_int_bson(buff, 4);
				final String increment_str = Integer.toString(increment.get_int());
				final String timestamp_str = Integer.toString(timestamp.get_int());
				tok.put_next_token(json_token_t.JSON_TOKEN_OBJECT_START, null);
				tok.put_next_token(json_token_t.JSON_TOKEN_NAME, "$timestamp");
				tok.put_next_token(json_token_t.JSON_TOKEN_OBJECT_START, null);
				tok.put_next_token(json_token_t.JSON_TOKEN_NAME, "t");
				tok.put_next_token(json_token_t.JSON_TOKEN_STRING, timestamp_str);
				tok.put_next_token(json_token_t.JSON_TOKEN_NAME, "i");
				tok.put_next_token(json_token_t.JSON_TOKEN_STRING, increment_str);
				tok.put_next_token(json_token_t.JSON_TOKEN_OBJECT_END, null);
				tok.put_next_token(json_token_t.JSON_TOKEN_OBJECT_END, null);
				break;
			}
			case 18: { //64 bit integer
				final TitanInteger value = decode_int_bson(buff, 8);
				final String tmp_str = Long.toString(value.get_long());
				tok.put_next_token(json_token_t.JSON_TOKEN_NUMBER, tmp_str);
				break;
			}
			case 127: { // maxkey
				tok.put_next_token(json_token_t.JSON_TOKEN_OBJECT_START, null);
				tok.put_next_token(json_token_t.JSON_TOKEN_NAME, "$maxKey");
				tok.put_next_token(json_token_t.JSON_TOKEN_NUMBER, "1");
				tok.put_next_token(json_token_t.JSON_TOKEN_OBJECT_END, null);
				break;
			}
			case (byte) 255: { // minkey
				tok.put_next_token(json_token_t.JSON_TOKEN_OBJECT_START, null);
				tok.put_next_token(json_token_t.JSON_TOKEN_NAME, "$minKey");
				tok.put_next_token(json_token_t.JSON_TOKEN_NUMBER, "1");
				tok.put_next_token(json_token_t.JSON_TOKEN_OBJECT_END, null);
				break;
			}
			default:
				throw new TtcnError("Unexpected type " + type[0] + " while decoding using bson2json().");
			}
		}
	}
}
