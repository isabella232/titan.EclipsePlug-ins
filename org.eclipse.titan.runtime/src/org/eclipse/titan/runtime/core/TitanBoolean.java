/******************************************************************************
 * Copyright (c) 2000-2021 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;
import java.text.MessageFormat;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.titan.runtime.core.BER.ASN_BERdescriptor;
import org.eclipse.titan.runtime.core.BER.ASN_Tag;
import org.eclipse.titan.runtime.core.BER.ASN_TagClass;
import org.eclipse.titan.runtime.core.JSON.TTCN_JSONdescriptor;
import org.eclipse.titan.runtime.core.JSON.json_string_escaping;
import org.eclipse.titan.runtime.core.JSON_Tokenizer.json_token_t;
import org.eclipse.titan.runtime.core.Param_Types.Module_Param_Boolean;
import org.eclipse.titan.runtime.core.Param_Types.Module_Param_Name;
import org.eclipse.titan.runtime.core.Param_Types.Module_Param_Unbound;
import org.eclipse.titan.runtime.core.Param_Types.Module_Parameter;
import org.eclipse.titan.runtime.core.Param_Types.Module_Parameter.basic_check_bits_t;
import org.eclipse.titan.runtime.core.Param_Types.Module_Parameter.type_t;
import org.eclipse.titan.runtime.core.RAW.RAW_Force_Omit;
import org.eclipse.titan.runtime.core.RAW.RAW_coding_par;
import org.eclipse.titan.runtime.core.RAW.RAW_enc_tr_pos;
import org.eclipse.titan.runtime.core.RAW.RAW_enc_tree;
import org.eclipse.titan.runtime.core.RAW.TTCN_RAWdescriptor;
import org.eclipse.titan.runtime.core.RAW.ext_bit_t;
import org.eclipse.titan.runtime.core.RAW.raw_sign_t;
import org.eclipse.titan.runtime.core.RAW.top_bit_order_t;
import org.eclipse.titan.runtime.core.TTCN_EncDec.coding_type;
import org.eclipse.titan.runtime.core.TTCN_EncDec.error_type;
import org.eclipse.titan.runtime.core.TTCN_EncDec.raw_order_t;
import org.eclipse.titan.runtime.core.TitanCharString.CharCoding;

/**
 * TTCN-3 boolean
 * @author Arpad Lovassy
 * @author Gergo Ujhelyi
 * @author Andrea Palfi
 */
public class TitanBoolean extends Base_Type {

	private static final ASN_Tag TitanBoolean_tag_[] = new ASN_Tag[] {new ASN_Tag(ASN_TagClass.ASN_TAG_UNIV, 1)};
	public static final ASN_BERdescriptor TitanBoolean_Ber_ = new ASN_BERdescriptor(1, TitanBoolean_tag_);
	public static final TTCN_RAWdescriptor TitanBoolean_raw_ = new TTCN_RAWdescriptor(1, raw_sign_t.SG_NO, raw_order_t.ORDER_LSB, raw_order_t.ORDER_LSB, raw_order_t.ORDER_LSB, raw_order_t.ORDER_LSB, ext_bit_t.EXT_BIT_NO, raw_order_t.ORDER_LSB, raw_order_t.ORDER_LSB, top_bit_order_t.TOP_BIT_INHERITED, 0, 0, 0, 8, 0, null, -1, CharCoding.UNKNOWN, null, false);
	public static final TTCN_JSONdescriptor TitanBoolean_json_ = new TTCN_JSONdescriptor(false, null, false, null, false, false, false, 0, null, false, json_string_escaping.ESCAPE_AS_SHORT);
	public static final TTCN_Typedescriptor TitanBoolean_descr_ = new TTCN_Typedescriptor("BOOLEAN", TitanBoolean_Ber_, TitanBoolean_raw_, TitanBoolean_json_, null);

	/**
	 * boolean_value in core.
	 * Unbound if null
	 */
	private Boolean boolean_value;

	/**
	 * Initializes to unbound value.
	 * */
	public TitanBoolean() {
		super();
	}

	/**
	 * Initializes to a given value.
	 *
	 * @param otherValue
	 *                the value to initialize to.
	 * */
	public TitanBoolean(final Boolean otherValue) {
		boolean_value = otherValue;
	}

	/**
	 * Initializes to a given value.
	 *
	 * @param otherValue
	 *                the value to initialize to.
	 * */
	public TitanBoolean(final TitanBoolean otherValue) {
		otherValue.must_bound("Copying an unbound boolean value.");

		boolean_value = otherValue.boolean_value;
	}

	/**
	 * Returns the Java native boolean value.
	 *
	 * @return the boolean value.
	 * */
	public Boolean get_value() {
		must_bound("Using the value of an unbound boolean variable.");
		return boolean_value;
	}

	/**
	 * Overwrites the internal data storage of this boolean.
	 * Takes ownership of the provided data.
	 * <p>
	 * Please note, this code is for internal use only.
	 * Users are not recommended to use this function.
	 * As such it is also not part of the public API
	 *  and might change without notice.
	 *
	 * <p>
	 * char*() in the core
	 *
	 * @param other_value the new internal value of this boolean.
	 * */
	public void set_value(final Boolean other_value) {
		boolean_value = other_value;
	}

	/**
	 * Assigns the other value to this value.
	 * Overwriting the current content in the process.
	 *<p>
	 * operator= in the core.
	 *
	 * @param otherValue
	 *                the other value to assign.
	 * @return the new value object.
	 */
	public TitanBoolean operator_assign(final boolean otherValue) {
		boolean_value = otherValue;

		return this;
	}

	/**
	 * Assigns the other value to this value.
	 * Overwriting the current content in the process.
	 *<p>
	 * operator= in the core.
	 *
	 * @param otherValue
	 *                the other value to assign.
	 * @return the new value object.
	 */
	public TitanBoolean operator_assign(final TitanBoolean otherValue) {
		otherValue.must_bound("Assignment of an unbound boolean value.");

		if (otherValue != this) {
			boolean_value = otherValue.boolean_value;
		}

		return this;
	}

	@Override
	public TitanBoolean operator_assign(final Base_Type otherValue) {
		if (otherValue instanceof TitanBoolean) {
			return operator_assign((TitanBoolean)otherValue);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to boolean", otherValue));
	}

	@Override
	public boolean is_bound() {
		return boolean_value != null;
	}

	@Override
	public boolean is_present() {
		return is_bound();
	}

	@Override
	public boolean is_value() {
		return boolean_value != null;
	}

	/**
	 * Performs a bitwise or operation on this and the provided boolean.
	 *
	 * operator|| in the core.
	 *
	 * @param other_value
	 *                the other value.
	 * @return the resulting boolean.
	 * */
	public boolean or(final boolean other_value) {
		must_bound("The left operand of or operator is an unbound boolean value.");

		return boolean_value || other_value;
	}

	/**
	 * Performs a bitwise or operation on this and the provided boolean.
	 *
	 * operator|| in the core.
	 *
	 * @param other_value
	 *                the other value.
	 * @return the resulting boolean.
	 * */
	public boolean or(final TitanBoolean other_value) {
		must_bound("The left operand of or operator is an unbound boolean value.");
		other_value.must_bound("The right operand of or operator is an unbound boolean value.");

		return boolean_value || other_value.boolean_value;
	}

	/**
	 * Performs a bitwise and operation on this and the provided boolean.
	 *
	 * operator&& in the core.
	 *
	 * @param other_value
	 *                the other value.
	 * @return the resulting boolean.
	 * */
	public boolean and(final boolean other_value) {
		must_bound("The left operand of and operator is an unbound boolean value.");

		return boolean_value && other_value;
	}

	/**
	 * Performs a bitwise and operation on this and the provided boolean.
	 *
	 * operator&& in the core.
	 *
	 * @param other_value
	 *                the other value.
	 * @return the resulting boolean.
	 * */
	public boolean and(final TitanBoolean other_value) {
		must_bound("The left operand of and operator is an unbound boolean value.");
		other_value.must_bound("The right operand of and operator is an unbound boolean value.");

		return boolean_value && other_value.boolean_value;
	}

	/**
	 * Performs a bitwise xor operation on this and the provided boolean.
	 *
	 * operator^ in the core.
	 *
	 * @param other_value
	 *                the other value.
	 * @return the resulting boolean.
	 * */
	public boolean xor(final boolean other_value) {
		must_bound("The left operand of xor operator is an unbound boolean value.");

		return boolean_value.booleanValue() != other_value;
	}

	/**
	 * Performs a bitwise xor operation on this and the provided boolean.
	 *
	 * operator^ in the core.
	 *
	 * @param other_value
	 *                the other value.
	 * @return the resulting boolean.
	 * */
	public boolean xor(final TitanBoolean other_value) {
		must_bound("The left operand of xor operator is an unbound boolean value.");
		other_value.must_bound("The right operand of xor operator is an unbound boolean value.");

		return boolean_value.booleanValue() != other_value.boolean_value.booleanValue();
	}

	/**
	 * Performs a bitwise negation.
	 *
	 * operator! in the core.
	 *
	 * @return the resulting boolean.
	 * */
	public boolean not() {
		must_bound("The operand of not operator is an unbound boolean value.");

		return !boolean_value;
	}

	/**
	 * Checks if the current value is equivalent to the provided one.
	 *
	 * operator== in the core
	 *
	 * @param otherValue
	 *                the other value to check against.
	 * @return {@code true} if the values are equivalent.
	 */
	public boolean operator_equals(final TitanBoolean otherValue) {
		must_bound("The left operand of comparison is an unbound boolean value.");
		otherValue.must_bound("The right operand of comparison is an unbound boolean value.");

		return boolean_value.equals(otherValue.boolean_value);
	}

	/**
	 * Checks if the current value is equivalent to the provided one.
	 *
	 * operator== in the core
	 *
	 * @param otherValue
	 *                the other value to check against.
	 * @return {@code true} if the values are equivalent.
	 */
	public boolean operator_equals(final boolean otherValue) {
		must_bound("The left operand of comparison is an unbound boolean value.");

		return boolean_value == otherValue;
	}


	@Override
	public boolean operator_equals(final Base_Type otherValue) {
		if (otherValue instanceof TitanBoolean) {
			return operator_equals((TitanBoolean)otherValue);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to boolean", otherValue));
	}

	/**
	 * Checks if the current value is not equivalent to the provided one.
	 *
	 * operator!= in the core
	 *
	 * @param otherValue
	 *                the other value to check against.
	 * @return {@code true} if the values are not equivalent.
	 */
	public boolean operator_not_equals(final boolean otherValue) {
		must_bound("The left operand of comparison is an unbound boolean value.");

		return !operator_equals(otherValue);
	}

	/**
	 * Checks if the current value is not equivalent to the provided one.
	 *
	 * operator!= in the core
	 *
	 * @param otherValue
	 *                the other value to check against.
	 * @return {@code true} if the values are not equivalent.
	 */
	public boolean operator_not_equals(final TitanBoolean otherValue) {
		return !operator_equals(otherValue);
	}

	@Override
	public void clean_up() {
		boolean_value = null;
	}

	/**
	 * Do not use this function!<br>
	 * It is provided by Java and currently used for debugging.
	 * But it is not part of the intentionally provided interface,
	 *   and so can be changed without notice.
	 * <p>
	 * JAVA DESCRIPTION:
	 * <p>
	 * {@inheritDoc}
	 *  */
	@Override
	public String toString() {
		if (boolean_value == null) {
			return "<unbound>";
		}

		return boolean_value.toString();
	}

	@Override
	public void log() {
		if (boolean_value != null) {
			TTCN_Logger.log_event_str(boolean_value.toString());
		} else {
			TTCN_Logger.log_event_unbound();
		}
	}

	@Override
	/** {@inheritDoc} */
	public void set_param(Module_Parameter param) {
		param.basic_check(basic_check_bits_t.BC_VALUE.getValue(), "boolean value");

		// Originally RT2
		if (param.get_type() == Module_Parameter.type_t.MP_Reference) {
			param = param.get_referenced_param().get();
		}

		if (param.get_type() != type_t.MP_Boolean) {
			param.type_error("boolean value");
		}
		boolean_value = param.get_boolean();
	}

	@Override
	/** {@inheritDoc} */
	public Module_Parameter get_param(final Module_Param_Name param_name) {
		if (!is_bound()) {
			return new Module_Param_Unbound();
		}
		return new Module_Param_Boolean(boolean_value);
	}

	@Override
	/** {@inheritDoc} */
	public void encode_text(final Text_Buf text_buf) {
		must_bound("Text encoder: Encoding an unbound boolean value.");

		text_buf.push_int(boolean_value ? 1 : 0);
	}

	@Override
	/** {@inheritDoc} */
	public void decode_text(final Text_Buf text_buf) {
		final int int_value = text_buf.pull_int().get_int();
		switch (int_value) {
		case 0:
			boolean_value = false;
			break;
		case 1:
			boolean_value = true;
			break;
		default:
			throw new TtcnError(MessageFormat.format("Text decoder: An invalid boolean value ({0}) was received.", int_value));
		}
	}

	/**
	 * Returns the native version of the boolean.
	 * <p>
	 * This version is used when the code generator could not be sure if the
	 * expression will result in native or non-native boolean.
	 *
	 * @param value
	 *                the boolean value.
	 * @return the value itself.
	 * */
	public static boolean get_native(final boolean value) {
		return value;
	}

	/**
	 * Returns the native version of the boolean.
	 *
	 * @param value
	 *                the boolean value.
	 * @return the native value of the provided boolean.
	 * */
	public static boolean get_native(final TitanBoolean value) {
		return value.get_value();
	}

	/**
	 * Performs a bitwise and operation on the provided booleans.
	 *
	 * static operator&& in the core.
	 *
	 * @param bool_value
	 *                the first value.
	 * @param other_value
	 *                the other value.
	 * @return the resulting boolean.
	 * */
	public static boolean and(final boolean bool_value, final TitanBoolean other_value) {
		if (!bool_value) {
			return false;
		}
		other_value.must_bound("The right operand of and operator is an unbound boolean value.");

		return other_value.boolean_value;
	}

	/**
	 * Performs a bitwise or operation on the provided booleans.
	 *
	 * static operator|| in the core.
	 *
	 * @param bool_value
	 *                the first value.
	 * @param other_value
	 *                the other value.
	 * @return the resulting boolean.
	 * */
	public static boolean or(final boolean bool_value, final TitanBoolean other_value) {
		if (bool_value) {
			return true;
		}
		other_value.must_bound("The right operand of or operator is an unbound boolean value.");

		return other_value.boolean_value;
	}

	/**
	 * Performs a bitwise xor operation on the provided booleans.
	 *
	 * static operator^ in the core.
	 *
	 * @param bool_value
	 *                the first value.
	 * @param other_value
	 *                the other value.
	 * @return the resulting boolean.
	 * */
	public static boolean xor(final boolean bool_value, final TitanBoolean other_value) {
		other_value.must_bound("The right operand of xor operator is an unbound boolean value.");

		return bool_value != other_value.boolean_value;
	}

	/**
	 * Checks if the first value is equivalent to the second one.
	 *
	 * static operator== in the core
	 *
	 * @param boolValue
	 *                the first value.
	 * @param otherValue
	 *                the other value to check against.
	 * @return {@code true} if the values are equivalent.
	 */
	public static boolean operator_equals(final boolean boolValue, final TitanBoolean otherValue) {
		otherValue.must_bound("The right operand of comparison is an unbound boolean value.");

		return boolValue == otherValue.boolean_value;
	}

	/**
	 * Checks if the first value is not equivalent to the second one.
	 *
	 * static operator!= in the core
	 *
	 * @param boolValue
	 *                the first value.
	 * @param otherValue
	 *                the other value to check against.
	 * @return {@code true} if the values are equivalent.
	 */
	public static boolean operator_not_equals(final boolean boolValue, final TitanBoolean otherValue) {
		otherValue.must_bound("The right operand of comparison is an unbound boolean value.");

		return new TitanBoolean(boolValue).operator_not_equals(otherValue.boolean_value);
	}

	@Override
	/** {@inheritDoc} */
	public void encode(final TTCN_Typedescriptor p_td, final TTCN_Buffer p_buf, final coding_type p_coding, final int flavour) {
		switch (p_coding) {
		case CT_RAW: {
			final TTCN_EncDec_ErrorContext errorContext = new TTCN_EncDec_ErrorContext("While RAW-encoding type '%s': ", p_td.name);
			try {
				if (p_td.raw == null) {
					TTCN_EncDec_ErrorContext.error_internal("No RAW descriptor available for type '%s'.", p_td.name);
				}

				final RAW_enc_tr_pos tree_position = new RAW_enc_tr_pos(0, null);
				final RAW_enc_tree root = new RAW_enc_tree(true, null, tree_position, 1, p_td.raw);
				RAW_encode(p_td, root);
				root.put_to_buf(p_buf);
			} finally {
				errorContext.leave_context();
			}
			break;
		}
		case CT_JSON: {
			final TTCN_EncDec_ErrorContext errorContext = new TTCN_EncDec_ErrorContext("While JSON-encoding type '%s': ", p_td.name);
			try {
				if(p_td.json == null) {
					TTCN_EncDec_ErrorContext.error_internal("No JSON descriptor available for type '%s'.", p_td.name);
				}

				final JSON_Tokenizer tok = new JSON_Tokenizer(flavour != 0);
				JSON_encode(p_td, tok);
				final StringBuilder temp = tok.get_buffer();
				for (int i = 0; i < temp.length(); i++) {
					final int temp2 = temp.charAt(i);
					p_buf.put_c((byte)temp2);
				}
			} finally {
				errorContext.leave_context();
			}
			break;
		}
		default:
			throw new TtcnError(MessageFormat.format("Unknown coding method requested to encode type `{0}''", p_td.name));
		}
	}

	@Override
	/** {@inheritDoc} */
	public void decode(final TTCN_Typedescriptor p_td, final TTCN_Buffer p_buf, final coding_type p_coding, final int flavour) {
		switch (p_coding) {
		case CT_RAW: {
			final TTCN_EncDec_ErrorContext errorContext = new TTCN_EncDec_ErrorContext("While RAW-decoding type '%s': ", p_td.name);
			try {
				if (p_td.raw == null) {
					TTCN_EncDec_ErrorContext.error_internal("No RAW descriptor available for type '%s'.", p_td.name);
				}

				final raw_order_t order = p_td.raw.top_bit_order == top_bit_order_t.TOP_BIT_LEFT ? raw_order_t.ORDER_LSB : raw_order_t.ORDER_MSB;
				if (RAW_decode(p_td, p_buf, p_buf.get_len() * 8, order) < 0) {
					TTCN_EncDec_ErrorContext.error(error_type.ET_INCOMPL_ANY, "Can not decode type '%s', because invalid or incomplete message was received", p_td.name);
				}
			} finally {
				errorContext.leave_context();
			}
			break;
		}
		case CT_JSON: {
			final TTCN_EncDec_ErrorContext errorContext = new TTCN_EncDec_ErrorContext("While JSON-decoding type '%s': ", p_td.name);
			try {
				if(p_td.json == null) {
					TTCN_EncDec_ErrorContext.error_internal("No JSON descriptor available for type '%s'.", p_td.name);
				}

				final byte[] data = p_buf.get_data();
				final char[] temp = new char[data.length];
				for (int i = 0; i < data.length; i++) {
					temp[i] = (char)data[i];
				}
				final JSON_Tokenizer tok = new JSON_Tokenizer(new String(temp), p_buf.get_len());
				if(JSON_decode(p_td, tok, false) < 0) {
					TTCN_EncDec_ErrorContext.error(TTCN_EncDec.error_type.ET_INCOMPL_MSG,
							"Can not decode type '%s', because invalid or incomplete message was received", p_td.name);
				}
				p_buf.set_pos(tok.get_buf_pos());
			} finally {
				errorContext.leave_context();
			}
			break;
		}
		default:
			throw new TtcnError(MessageFormat.format("Unknown coding method requested to decode type `{0}''", p_td.name));
		}
	}

	@Override
	/** {@inheritDoc} */
	public int RAW_encode(final TTCN_Typedescriptor p_td, final RAW_enc_tree myleaf) {
		byte bc[];
		final int loc_length = p_td.raw.fieldlength != 0 ? p_td.raw.fieldlength : 1;
		final int length = (loc_length + 7) / 8;
		byte tmp;
		if (!is_bound()) {
			TTCN_EncDec_ErrorContext.error(error_type.ET_UNBOUND, "Encoding an unbound value.");
			tmp = 0;
		} else {
			tmp = boolean_value ? (byte)0xFF : (byte)0x00;
		}
		if (length > RAW.RAW_INT_ENC_LENGTH) {
			myleaf.data_array = bc = new byte[length];
		} else {
			bc = myleaf.data_array;
		}
		for (int i = 0; i < bc.length; i++) {
			bc[i] = tmp;
		}
		if (boolean_value && loc_length % 8 != 0) {
			// remove the extra ones from the last octet
			bc[length - 1] &= RAW.BitMaskTable[loc_length % 8];
		}
		myleaf.coding_par.csn1lh = p_td.raw.csn1lh;

		return myleaf.length = loc_length;
	}

	@Override
	/** {@inheritDoc} */
	public int RAW_decode(final TTCN_Typedescriptor p_td, final TTCN_Buffer buff, int limit, final raw_order_t top_bit_ord, final boolean no_err, final int sel_field, final boolean first_call, final RAW_Force_Omit force_omit) {
		final int prepaddlength = buff.increase_pos_padd(p_td.raw.prepadding);
		limit -= prepaddlength;
		int decode_length = p_td.raw.fieldlength > 0 ? p_td.raw.fieldlength : 1;
		final TTCN_EncDec_ErrorContext errorcontext = new TTCN_EncDec_ErrorContext();
		try {
			if (decode_length > limit) {
				if (no_err) {
					return -TTCN_EncDec.error_type.ET_LEN_ERR.ordinal();
				}
				TTCN_EncDec_ErrorContext.error(error_type.ET_LEN_ERR, "There is not enough bits in the buffer to decode type %s (needed: %d, found: %d).", p_td.name, decode_length, limit);
				decode_length = limit;
			}
			final int nof_unread_bits = buff.unread_len_bit();
			if (decode_length > nof_unread_bits) {
				if (no_err) {
					return -TTCN_EncDec.error_type.ET_INCOMPL_MSG.ordinal();
				}
				TTCN_EncDec_ErrorContext.error(error_type.ET_INCOMPL_MSG, "There is not enough bits in the buffer to decode type %s (needed: %d, found: %d).", p_td.name, decode_length, nof_unread_bits);
				decode_length = nof_unread_bits;
			}
			if (decode_length < 0) {
				return -1;
			} else if (decode_length == 0) {
				boolean_value = false;
			} else {
				final RAW_coding_par cp = new RAW_coding_par();
				boolean orders = p_td.raw.bitorderinoctet == raw_order_t.ORDER_MSB;
				if (p_td.raw.bitorderinfield == raw_order_t.ORDER_MSB) {
					orders = !orders;
				}
				cp.bitorder = orders ? raw_order_t.ORDER_MSB : raw_order_t.ORDER_LSB;
				orders = p_td.raw.byteorder == raw_order_t.ORDER_MSB;
				if (p_td.raw.bitorderinfield == raw_order_t.ORDER_MSB) {
					orders = !orders;
				}
				cp.byteorder = orders ? raw_order_t.ORDER_MSB : raw_order_t.ORDER_LSB;
				cp.fieldorder = p_td.raw.fieldorder;
				cp.hexorder = raw_order_t.ORDER_LSB;
				cp.csn1lh = p_td.raw.csn1lh;
				final int length = (decode_length + 7) / 8;
				byte[] data = new byte[length];
				buff.get_b(decode_length, data, cp, top_bit_ord);
				if (decode_length % 8 != 0) {
					data[length - 1] &= RAW.BitMaskTable[decode_length % 8];
				}
				char ch = '\0';
				for (int i = 0; i < length; i++) {
					ch |= data[i];
				}
				boolean_value = ch != '\0';
			}
			decode_length += buff.increase_pos_padd(p_td.raw.padding);
		} finally {
			errorcontext.leave_context();
		}

		return decode_length + prepaddlength;
	}
	@Override
	/** {@inheritDoc} */
	public int JSON_encode(final TTCN_Typedescriptor p_td, final JSON_Tokenizer p_tok, final boolean p_parent_is_map) {
		if (!is_bound()) {
			TTCN_EncDec_ErrorContext.error(TTCN_EncDec.error_type.ET_UNBOUND,"Encoding an unbound boolean value.");
			return -1;
		}

		return p_tok.put_next_token((boolean_value) ? json_token_t.JSON_TOKEN_LITERAL_TRUE : json_token_t.JSON_TOKEN_LITERAL_FALSE, null);
	}

	@Override
	/** {@inheritDoc} */
	public int JSON_decode(final TTCN_Typedescriptor p_td, final JSON_Tokenizer p_tok, final boolean p_silent, final boolean p_parent_is_map, final int p_chosen_field) {
		if (p_td.json.getActualDefaultValue() != null && 0 == p_tok.get_buffer_length()) {
			operator_assign(p_td.json.getActualDefaultValue());

			return 0;
		}

		final AtomicReference<json_token_t> token = new AtomicReference<json_token_t>(json_token_t.JSON_TOKEN_NONE);
		final int dec_len = p_tok.get_next_token(token, null, null);
		if (json_token_t.JSON_TOKEN_ERROR == token.get()) {
			if(!p_silent) {
				TTCN_EncDec_ErrorContext.error(TTCN_EncDec.error_type.ET_INVAL_MSG, JSON.JSON_DEC_BAD_TOKEN_ERROR, "");
			}
			return JSON.JSON_ERROR_FATAL;
		} else if (json_token_t.JSON_TOKEN_LITERAL_TRUE == token.get()) {
			boolean_value = true;
		} else if (json_token_t.JSON_TOKEN_LITERAL_FALSE == token.get()) {
			boolean_value = false;
		} else {
			return JSON.JSON_ERROR_INVALID_TOKEN;
		}

		return dec_len;
	}
}
