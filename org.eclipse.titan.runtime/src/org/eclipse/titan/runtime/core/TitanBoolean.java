/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;
import java.text.MessageFormat;

import org.eclipse.titan.runtime.core.RAW.RAW_coding_par;
import org.eclipse.titan.runtime.core.RAW.RAW_enc_tr_pos;
import org.eclipse.titan.runtime.core.RAW.RAW_enc_tree;
import org.eclipse.titan.runtime.core.TTCN_EncDec.coding_type;
import org.eclipse.titan.runtime.core.TTCN_EncDec.error_type;
import org.eclipse.titan.runtime.core.TTCN_EncDec.raw_order_t;

/**
 * TTCN-3 boolean
 * @author Arpad Lovassy
 * @author Gergo Ujhelyi
 * @author Andrea Pálfi
 */
public class TitanBoolean extends Base_Type {

	/**
	 * boolean_value in core.
	 * Unbound if null
	 */
	private Boolean boolean_value;

	public TitanBoolean() {
		super();
	}

	public TitanBoolean(final Boolean aOtherValue) {
		boolean_value = aOtherValue;
	}

	public TitanBoolean(final TitanBoolean aOtherValue) {
		aOtherValue.mustBound("Copying an unbound boolean value.");

		boolean_value = aOtherValue.boolean_value;
	}

	public Boolean getValue() {
		return boolean_value;
	}

	public void setValue(final Boolean aOtherValue) {
		boolean_value = aOtherValue;
	}

	// originally operator=
	public TitanBoolean assign(final boolean aOtherValue) {
		boolean_value = aOtherValue;

		return this;
	}

	// originally operator=
	public TitanBoolean assign(final TitanBoolean aOtherValue) {
		aOtherValue.mustBound("Assignment of an unbound boolean value.");

		if (aOtherValue != this) {
			boolean_value = aOtherValue.boolean_value;
		}

		return this;
	}

	@Override
	public TitanBoolean assign(final Base_Type otherValue) {
		if (otherValue instanceof TitanBoolean) {
			return assign((TitanBoolean)otherValue);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to boolean", otherValue));
	}

	public boolean isBound() {
		return boolean_value != null;
	}

	public boolean isPresent() {
		return isBound();
	}

	public boolean isValue() {
		return boolean_value != null;
	}

	public void mustBound(final String aErrorMessage) {
		if (boolean_value == null) {
			throw new TtcnError(aErrorMessage);
		}
	}

	/**
	 * this or aOtherValue
	 * originally operator or
	 */
	public boolean or(final boolean aOtherValue) {
		mustBound("The left operand of or operator is an unbound boolean value.");

		return boolean_value || aOtherValue;
	}

	/**
	 * this or aOtherValue
	 * originally operator or
	 */
	public boolean or(final TitanBoolean aOtherValue) {
		mustBound("The left operand of or operator is an unbound boolean value.");
		aOtherValue.mustBound("The right operand of or operator is an unbound boolean value.");

		return boolean_value || aOtherValue.boolean_value;
	}

	/**
	 * this and aOtherValue
	 * originally operator and
	 */
	public boolean and(final boolean aOtherValue) {
		mustBound("The left operand of and operator is an unbound boolean value.");

		return boolean_value && aOtherValue;
	}

	/**
	 * this and aOtherValue
	 * originally operator and
	 */
	public boolean and(final TitanBoolean aOtherValue) {
		mustBound("The left operand of and operator is an unbound boolean value.");
		aOtherValue.mustBound("The right operand of and operator is an unbound boolean value.");

		return boolean_value && aOtherValue.boolean_value;
	}

	/**
	 * this xor aOtherValue
	 * originally operator ^
	 */
	public boolean xor(final boolean aOtherValue) {
		mustBound("The left operand of xor operator is an unbound boolean value.");

		return boolean_value.booleanValue() != aOtherValue;
	}

	/**
	 * this xor aOtherValue
	 * originally operator ^
	 */
	public boolean xor(final TitanBoolean aOtherValue) {
		mustBound("The left operand of xor operator is an unbound boolean value.");
		aOtherValue.mustBound("The right operand of xor operator is an unbound boolean value.");

		return boolean_value.booleanValue() != aOtherValue.boolean_value.booleanValue();
	}

	/**
	 * not this
	 * originally operator not
	 */
	public boolean not() {
		mustBound("The operand of not operator is an unbound boolean value.");

		return !boolean_value;
	}

	// originally operator==
	public boolean operatorEquals(final TitanBoolean aOtherValue) {
		mustBound("The left operand of comparison is an unbound boolean value.");
		aOtherValue.mustBound("The right operand of comparison is an unbound boolean value.");

		return boolean_value.equals(aOtherValue.boolean_value);
	}

	// originally operator==
	public boolean operatorEquals(final boolean otherValue) {
		mustBound("The left operand of comparison is an unbound boolean value.");

		return boolean_value == otherValue;
	}


	@Override
	public boolean operatorEquals(final Base_Type otherValue) {
		if (otherValue instanceof TitanBoolean) {
			return operatorEquals((TitanBoolean)otherValue);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to boolean", otherValue));
	}

	// originally operator !=
	public boolean operatorNotEquals(final boolean otherValue) {
		mustBound("The left operand of comparison is an unbound boolean value.");

		return !operatorEquals(otherValue);
	}

	// originally operator!=
	public boolean operatorNotEquals(final TitanBoolean aOtherValue) {
		return !operatorEquals(aOtherValue);
	}

	public void cleanUp() {
		boolean_value = null;
	}

	@Override
	public String toString() {
		if (boolean_value == null) {
			return "<unbound>";
		}

		return boolean_value.toString();
	}

	// log()
	public void log() {
		if (boolean_value != null) {
			TtcnLogger.log_event_str(boolean_value.toString());
		} else {
			TtcnLogger.log_event_unbound();
		}
	}

	@Override
	/** {@inheritDoc} */
	public void encode_text(final Text_Buf text_buf) {
		mustBound("Text encoder: Encoding an unbound boolean value.");

		text_buf.push_int(boolean_value ? 1 : 0);
	}

	@Override
	/** {@inheritDoc} */
	public void decode_text(final Text_Buf text_buf) {
		final int int_value = text_buf.pull_int().getInt();
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

	public static boolean getNative(final boolean value) {
		return value;
	}

	public static boolean getNative(final TitanBoolean otherValue) {
		return otherValue.getValue();
	}

	// static and
	public static boolean and(final boolean boolValue, final TitanBoolean otherValue) {
		if (!boolValue) {
			return false;
		}
		otherValue.mustBound("The right operand of and operator is an unbound boolean value.");

		return otherValue.boolean_value;
	}

	// static or
	public static boolean or(final boolean boolValue, final TitanBoolean otherValue) {
		if (boolValue) {
			return true;
		}
		otherValue.mustBound("The right operand of or operator is an unbound boolean value.");

		return otherValue.boolean_value;
	}

	// static xor
	public static boolean xor(final boolean boolValue, final TitanBoolean otherValue) {
		otherValue.mustBound("The right operand of xor operator is an unbound boolean value.");

		return boolValue != otherValue.boolean_value;
	}

	// static equals
	public static boolean operatorEquals(final boolean boolValue, final TitanBoolean otherValue) {
		otherValue.mustBound("The right operand of comparison is an unbound boolean value.");

		return boolValue == otherValue.boolean_value;
	}

	// static notEquals
	public static boolean opeatorNotEquals(final boolean boolValue, final TitanBoolean otherValue) {
		otherValue.mustBound("The right operand of comparison is an unbound boolean value.");

		return new TitanBoolean(boolValue).operatorNotEquals(otherValue.boolean_value);
	}

	@Override
	/** {@inheritDoc} */
	public void encode(final TTCN_Typedescriptor p_td, final TTCN_Buffer p_buf, final coding_type p_coding, final int flavour) {
		switch (p_coding) {
		case CT_RAW: {
			final TTCN_EncDec_ErrorContext errorContext = new TTCN_EncDec_ErrorContext("While RAW-encoding type '%s': ", p_td.name);
			if (p_td.raw == null) {
				TTCN_EncDec_ErrorContext.error_internal("No RAW descriptor available for type '%s'.", p_td.name);
			}

			final RAW_enc_tr_pos rp = new RAW_enc_tr_pos(0, null);
			final RAW_enc_tree root = new RAW_enc_tree(true, null, rp, 1, p_td.raw);
			RAW_encode(p_td, root);
			root.put_to_buf(p_buf);

			errorContext.leaveContext();
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
			if (p_td.raw == null) {
				TTCN_EncDec_ErrorContext.error_internal("No RAW descriptor available for type '%s'.", p_td.name);
			}
			raw_order_t order;
			switch (p_td.raw.top_bit_order) {
			case TOP_BIT_LEFT:
				order = raw_order_t.ORDER_LSB;
				break;
			case TOP_BIT_RIGHT:
			default:
				order = raw_order_t.ORDER_MSB;
				break;
			}

			if (RAW_decode(p_td, p_buf, p_buf.get_len() * 8, order) < 0) {
				TTCN_EncDec_ErrorContext.error(error_type.ET_INCOMPL_ANY, "Can not decode type '%s', because invalid or incomplete message was received", p_td.name);
			}

			errorContext.leaveContext();
			break;
		}
		default:
			throw new TtcnError(MessageFormat.format("Unknown coding method requested to decode type `{0}''", p_td.name));
		}
	}


	public int RAW_encode(final TTCN_Typedescriptor p_td, final RAW_enc_tree myleaf) {
		char bc[];
		final int loc_length = p_td.raw.fieldlength != 0 ? p_td.raw.fieldlength : 1;
		final int length = (loc_length + 7) / 8;
		int tmp;
		if (!isBound()) {
			TTCN_EncDec_ErrorContext.error(error_type.ET_UNBOUND, "Encoding an unbound value.");
			tmp = 0;
		} else {
			tmp = boolean_value ? 0xFF : 0x00;
		}
		if (length > RAW.RAW_INT_ENC_LENGTH) {
			myleaf.data_array = bc = new char[length];
		} else {
			bc = myleaf.data_array;
		}
		for (int i = 0; i < bc.length; i++) {
			bc[i] = (char)tmp;
		}
		if (boolean_value && loc_length % 8 != 0) {
			// remove the extra ones from the last octet
			bc[length - 1] &= RAW.BitMaskTable[loc_length % 8];
		}
		return myleaf.length = loc_length;
	}

	public int RAW_decode(final TTCN_Typedescriptor p_td, final TTCN_Buffer buff, int limit, final raw_order_t top_bit_ord, final boolean no_err, final int sel_field, final boolean first_call) {
		final int prepaddlength = buff.increase_pos_padd(p_td.raw.prepadding);
		limit -= prepaddlength;
		int decode_length = p_td.raw.fieldlength > 0 ? p_td.raw.fieldlength : 1;
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
			boolean orders = false;
			if (p_td.raw.bitorderinoctet == raw_order_t.ORDER_MSB) {
				orders = true;
			}
			if (p_td.raw.bitorderinfield == raw_order_t.ORDER_MSB) {
				orders = !orders;
			}
			cp.bitorder = orders ? raw_order_t.ORDER_MSB : raw_order_t.ORDER_LSB;
			orders = false;
			if (p_td.raw.byteorder == raw_order_t.ORDER_MSB) {
				orders = true;
			}
			if (p_td.raw.bitorderinfield == raw_order_t.ORDER_MSB) {
				orders = !orders;
			}
			cp.byteorder = orders ? raw_order_t.ORDER_MSB : raw_order_t.ORDER_LSB;
			cp.fieldorder = p_td.raw.fieldorder;
			cp.hexorder = raw_order_t.ORDER_LSB;
			final int length = (decode_length + 7) / 8;
			char[] data = new char[length];
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
		return decode_length + prepaddlength;
	}
}
