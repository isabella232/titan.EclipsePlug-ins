/******************************************************************************
 * Copyright (c) 2000-2019 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

import java.text.MessageFormat;
import java.util.Arrays;

import org.eclipse.titan.runtime.core.Param_Types.Module_Parameter;
import org.eclipse.titan.runtime.core.Param_Types.Module_Parameter.basic_check_bits_t;
import org.eclipse.titan.runtime.core.Param_Types.Module_Parameter.expression_operand_t;
import org.eclipse.titan.runtime.core.Param_Types.Module_Parameter.operation_type_t;
import org.eclipse.titan.runtime.core.RAW.RAW_Force_Omit;
import org.eclipse.titan.runtime.core.RAW.RAW_coding_par;
import org.eclipse.titan.runtime.core.RAW.RAW_enc_tr_pos;
import org.eclipse.titan.runtime.core.RAW.RAW_enc_tree;
import org.eclipse.titan.runtime.core.TTCN_EncDec.coding_type;
import org.eclipse.titan.runtime.core.TTCN_EncDec.error_type;
import org.eclipse.titan.runtime.core.TTCN_EncDec.raw_order_t;

/**
 * TTCN-3 hexstring
 *
 * @author Arpad Lovassy
 * @author Gergo Ujhelyi
 * @author Andrea Palfi
 */
public class TitanHexString extends Base_Type {

	static final String HEX_DIGITS = "0123456789ABCDEF?*";

	/**
	 * hexstring value.
	 *
	 * Packed storage of hex digits, filled from LSB.
	 */
	private byte nibbles_ptr[];

	/**
	 * Initializes to unbound value.
	 * */
	public TitanHexString() {
		super();
	}

	/**
	 * Initializes to a given value.
	 *
	 * @param otherValue
	 *                the value to initialize to.
	 * */
	public TitanHexString(final byte otherValue[]) {
		nibbles_ptr = TitanString_Utils.copy_byte_list(otherValue);
	}

	/**
	 * Initializes to a given value.
	 *
	 * @param otherValue
	 *                the value to initialize to.
	 * */
	public TitanHexString(final TitanHexString otherValue) {
		otherValue.must_bound("Copying an unbound hexstring value.");

		nibbles_ptr = TitanString_Utils.copy_byte_list(otherValue.nibbles_ptr);
	}

	/**
	 * Initializes to a given value.
	 *
	 * @param otherValue
	 *                the value to initialize to.
	 * */
	public TitanHexString(final TitanHexString_Element otherValue) {
		otherValue.must_bound("Initialization from an unbound hexstring element.");

		nibbles_ptr = new byte[1];
		nibbles_ptr[0] = (byte) otherValue.get_nibble();
	}

	/**
	 * Initializes to a given value.
	 *
	 * @param otherValue
	 *                the value to initialize to.
	 * */
	public TitanHexString(final byte aValue) {
		nibbles_ptr = new byte[1];
		nibbles_ptr[0] = aValue;
	}

	/**
	 * Constructor
	 * @param aValue string representation of a hexstring value, without ''B, it contains only [0-9A-F] characters.
	 * NOTE: this is the way hexstring value is stored in Hexstring_Value
	 */
	public TitanHexString(final String aValue) {
		nibbles_ptr = hexstr2bytelist(aValue);
	}

	private void clearUnusedNibble() {
		if (nibbles_ptr.length % 2 == 1) {
			nibbles_ptr[nibbles_ptr.length / 2] = (byte) (nibbles_ptr[nibbles_ptr.length / 2] & 0x0F);
		}
	}

	/**
	 * Converts a string representation of a hexstring to a list of bytes
	 * @param aHexString string representation of hexstring
	 * @return value list of the hexstring, groupped in bytes
	 */
	private static byte[] hexstr2bytelist(final String aHexString) {
		final int len = aHexString.length();
		final byte result[] = new byte[len];
		for (int i = 0; i < len; i++) {
			final char hexDigit = aHexString.charAt(i);
			final byte byteValue = hexdigit2byte(hexDigit);
			result[i] = byteValue;
		}

		return result;
	}

	/**
	 * Converts a string representation of a hexadecimal digit to a byte
	 * @param aHexDigit string representation of hex digit, possible value: [0-9A-F] characters
	 * @return value of the hex digit
	 */
	static byte hexdigit2byte(final char aHexDigit) {
		byte result;
		if ('0' <= aHexDigit && aHexDigit <= '9') {
			result = (byte) (aHexDigit - '0');
		} else if ('A' <= aHexDigit && aHexDigit <= 'F') {
			result = (byte) (aHexDigit - 'A' + 10);
		} else if ('a' <= aHexDigit && aHexDigit <= 'f') {
			result = (byte) (aHexDigit - 'a' + 10);
		} else {
			// TODO: handle error
			result = 0;
		}
		return result;
	}

	/** Return the nibble at index i
	 *
	 * @param nibble_index
	 * @return
	 */
	public byte get_nibble(final int nibble_index) {
		return nibbles_ptr[nibble_index];
	}

	/**
	 * Overwrites the internal data storage of this hexstring directly.
	 * <p>
	 * Please note, this code is for internal use only.
	 * Users are not recommended to use this function.
	 * As such it is also not part of the public API
	 *  and might change without notice.
	 * <p>
	 *
	 * @param nibble_index
	 *                the index to overwrite.
	 * @param new_value
	 *                the value to use.
	 * */
	void set_nibble(final int nibble_index, final byte new_value) {
		nibbles_ptr[nibble_index] = new_value;
	}

	/**
	 * Returns the internal data storage of this hexstring.
	 * <p>
	 * Please note, this code is for internal use only.
	 * Users are not recommended to use this function.
	 * As such it is also not part of the public API
	 *  and might change without notice.
	 *
	 * <p>
	 * char*() in the core
	 *
	 * @return the internal representation of the hexstring.
	 * */
	public byte[] get_value() {
		return nibbles_ptr;
	}

	/**
	 * Overwrites the internal data storage of this hexstring.
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
	 * @param other_value the internal representation of the hexstring.
	 * */
	public void set_value(final byte other_value[]) {
		nibbles_ptr = other_value;
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
	public TitanHexString operator_assign(final TitanHexString otherValue) {
		otherValue.must_bound("Assignment of an unbound hexstring value.");

		if (otherValue != this) {
			nibbles_ptr = otherValue.nibbles_ptr;
		}

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
	public TitanHexString operator_assign(final TitanHexString_Element otherValue) {
		otherValue.must_bound("Assignment of an unbound hexstring element to a hexstring.");

		clean_up();
		nibbles_ptr = new byte[1];
		nibbles_ptr[0] = (byte) (otherValue.get_nibble());

		return this;
	}

	@Override
	public TitanHexString operator_assign(final Base_Type otherValue) {
		if (otherValue instanceof TitanHexString) {
			return operator_assign((TitanHexString) otherValue);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to hexstring", otherValue));
	}

	@Override
	public boolean is_bound() {
		return nibbles_ptr != null;
	}

	@Override
	public boolean is_value() {
		return nibbles_ptr != null;
	}

	/**
	 * Returns the number of elements, that is, the largest used index plus
	 * one and zero for the empty value.
	 *
	 * lengthof in the core
	 *
	 * @return the number of elements.
	 * */
	public TitanInteger lengthof() {
		must_bound("Performing lengthof operation on an unbound charstring value.");

		return new TitanInteger(nibbles_ptr.length);
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
	public boolean operator_equals(final TitanHexString otherValue) {
		must_bound("Unbound left operand of hexstring comparison.");
		otherValue.must_bound("Unbound right operand of hexstring comparison.");

		return Arrays.equals(nibbles_ptr, otherValue.nibbles_ptr);
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
	public boolean operator_equals(final TitanHexString_Element otherValue) {
		must_bound("Unbound left operand of hexstring comparison.");
		otherValue.must_bound("Unbound right operand of hexstring element comparison.");

		if (nibbles_ptr.length != 1) {
			return false;
		}

		return get_nibble(0) == otherValue.get_nibble();
	}

	@Override
	public boolean operator_equals(final Base_Type otherValue) {
		if (otherValue instanceof TitanHexString) {
			return operator_equals((TitanHexString) otherValue);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to hexstring", otherValue));
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
	public boolean operator_not_equals(final TitanHexString otherValue) {
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
	public boolean operator_not_equals(final TitanHexString_Element otherValue) {
		return !operator_equals(otherValue);
	}

	@Override
	public void clean_up() {
		nibbles_ptr = null;
	}

	/**
	 * Gives access to the given element. Indexing begins from zero.
	 * Over-indexing by 1 extends the hexstring.
	 *
	 * operator[] in the core.
	 *
	 * @param index_value
	 *            the index of the element to return.
	 * @return the element at the specified position in this hexstring
	 * */
	public TitanHexString_Element get_at(final int index_value) {
		if (nibbles_ptr == null && index_value == 0) {
			nibbles_ptr = new byte[1];
			return new TitanHexString_Element(false, this, 0);
		} else {
			must_bound("Accessing an element of an unbound hexstring value.");

			if (index_value < 0) {
				throw new TtcnError("Accessing an hexstring element using a negative index (" + index_value + ").");
			}

			final int n_nibbles = nibbles_ptr.length;
			if (index_value > n_nibbles) {
				throw new TtcnError("Index overflow when accessing a hexstring element: The index is " + index_value +
						", but the string has only " + n_nibbles + " hexadecimal digits.");
			}
			if (index_value == n_nibbles) {
				final byte temp[] = new byte[nibbles_ptr.length + 1];
				System.arraycopy(nibbles_ptr, 0, temp, 0, nibbles_ptr.length);
				nibbles_ptr = temp;
				return new TitanHexString_Element(false, this, index_value);
			} else {
				return new TitanHexString_Element(true, this, index_value);
			}
		}
	}

	/**
	 * Gives access to the given element. Indexing begins from zero.
	 * Over-indexing by 1 extends the hexstring.
	 *
	 * operator[] in the core.
	 *
	 * @param index_value
	 *            the index of the element to return.
	 * @return the element at the specified position in this hexstring
	 * */
	public TitanHexString_Element get_at(final TitanInteger index_value) {
		index_value.must_bound("Indexing a hexstring value with an unbound integer value.");

		return get_at(index_value.get_int());
	}

	/**
	 * Gives read-only access to the given element.
	 *
	 * Index underflow and overflow causes dynamic test case error.
	 *
	 * const operator[] const in the core.
	 *
	 * @param index
	 *            the index of the element to return.
	 * @return the element at the specified position in this hexstring
	 * */
	public final TitanHexString_Element constGet_at(final int index_value) {
		must_bound("Accessing an element of an unbound hexstring value.");

		if (index_value < 0) {
			throw new TtcnError("Accessing an hexstring element using a negative index (" + index_value + ").");
		}

		final int n_nibbles = nibbles_ptr.length;
		if (index_value >= n_nibbles) {
			throw new TtcnError("Index overflow when accessing a hexstring element: The index is " + index_value +
					", but the string has only " + n_nibbles + " hexadecimal digits.");
		}

		return new TitanHexString_Element(true, this, index_value);
	}

	/**
	 * Gives read-only access to the given element.
	 *
	 * Index underflow and overflow causes dynamic test case error.
	 *
	 * const operator[] const in the core.
	 *
	 * @param index
	 *            the index of the element to return.
	 * @return the element at the specified position in this hexstring
	 * */
	public final TitanHexString_Element constGet_at(final TitanInteger index_value) {
		index_value.must_bound("Indexing a hexstring value with an unbound integer value.");

		return constGet_at(index_value.get_int());
	}

	@Override
	public void log() {
		if (nibbles_ptr != null) {
			TTCN_Logger.log_char('\'');
			for (int i = 0; i < nibbles_ptr.length; i++) {
				TTCN_Logger.log_hex(get_nibble(i));
			}
			TTCN_Logger.log_event_str("'H");
		} else {
			TTCN_Logger.log_event_unbound();
		}
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
		if (nibbles_ptr == null) {
			return "<unbound>";
		}

		final StringBuilder sb = new StringBuilder();
		final int size = nibbles_ptr.length;
		for (int i = 0; i < size; i++) {
			final Byte digit = nibbles_ptr[i];
			sb.append(HEX_DIGITS.charAt(digit));
		}
		return sb.toString();
	}

	@Override
	/** {@inheritDoc} */
	public void encode_text(final Text_Buf text_buf) {
		must_bound("Text encoder: Encoding an unbound hexstring value.");

		final int nibbles = nibbles_ptr.length;
		text_buf.push_int(nibbles);
		if (nibbles > 0) {
			text_buf.push_raw(nibbles_ptr);
		}
	}

	@Override
	/** {@inheritDoc} */
	public void decode_text(final Text_Buf text_buf) {
		clean_up();

		final int n_nibbles = text_buf.pull_int().get_int();
		if (n_nibbles < 0) {
			throw new TtcnError("Text decoder: Invalid length was received for a hexstring.");
		}

		nibbles_ptr = new byte[n_nibbles];
		if (n_nibbles > 0) {
			text_buf.pull_raw(n_nibbles, nibbles_ptr);
		}
	}

	@Override
	/** {@inheritDoc} */
	public void set_param(final Module_Parameter param) {
		param.basic_check(basic_check_bits_t.BC_VALUE.getValue(), "hexstring value");
		switch (param.get_type()) {
		case MP_Hexstring:
			switch (param.get_operation_type()) {
			case OT_ASSIGN:
				clean_up();
				nibbles_ptr = new byte[param.get_string_size()];
				System.arraycopy((byte[])param.get_string_data(), 0, nibbles_ptr, 0, param.get_string_size());
				clearUnusedNibble();
				break;
			case OT_CONCAT:
				if (is_bound()) {
					this.operator_assign(this.operator_concatenate(new TitanHexString((byte[]) param.get_string_data())));
				} else {
					this.operator_assign(new TitanHexString((byte[]) param.get_string_data()));
				}
				break;
			default:
				throw new TtcnError("Internal error: HEXSTRING::set_param()");
			}
			break;
		case MP_Expression:
			if (param.get_expr_type() == expression_operand_t.EXPR_CONCATENATE) {
				final TitanHexString operand1 = new TitanHexString();
				final TitanHexString operand2 = new TitanHexString();
				operand1.set_param(param.get_operand1());
				operand2.set_param(param.get_operand2());
				if (param.get_operation_type() == operation_type_t.OT_CONCAT) {
					this.operator_assign(this.operator_concatenate(operand1).operator_concatenate(operand2));
				} else {
					this.operator_assign(operand1.operator_concatenate(operand2));
				}
			} else {
				param.expr_type_error("a hexstring");
			}
			break;
		default:
			param.type_error("hexstring value");
			break;
		}
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
		default:
			throw new TtcnError(MessageFormat.format("Unknown coding method requested to encode type `{0}''", p_td.name));
		}
	}
	
	@Override
	/** {@inheritDoc} */
	public void decode(final TTCN_Typedescriptor p_td, final TTCN_Buffer p_buf, final coding_type p_coding, final int flavour) {
		switch (p_coding) {
		case CT_RAW:
			final TTCN_EncDec_ErrorContext errorContext = new TTCN_EncDec_ErrorContext("While RAW-decoding type '%s': ", p_td.name);
			try {
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
					TTCN_EncDec_ErrorContext.error(error_type.ET_INCOMPL_MSG,  "Can not decode type '%s', because invalid or incomplete message was received", p_td.name);
				}
			} finally {
				errorContext.leave_context();
			}
			break;
		default:
			throw new TtcnError(MessageFormat.format("Unknown coding method requested to decode type `{0}''", p_td.name));
		}
	}

	@Override
	public boolean is_present() {
		return is_bound();
	}

	/**
	 * Concatenates the current hexstring with the hexstring received as a
	 * parameter.
	 *
	 * operator+ in the core.
	 *
	 * @param other_value
	 *                the other value to concatenate with.
	 * @return the new hexstring representing the concatenated value.
	 * */
	public TitanHexString operator_concatenate(final TitanHexString other_value) {
		must_bound("Unbound left operand of hexstring concatenation.");
		other_value.must_bound("Unbound right operand of hexstring concatenation.");

		if (nibbles_ptr.length == 0) {
			return new TitanHexString(other_value);
		}
		if (other_value.nibbles_ptr.length == 0) {
			return new TitanHexString(this);
		}

		final byte temp[] = new byte[nibbles_ptr.length + other_value.nibbles_ptr.length];
		System.arraycopy(nibbles_ptr, 0, temp, 0, nibbles_ptr.length);
		System.arraycopy(other_value.nibbles_ptr, 0, temp, nibbles_ptr.length, other_value.nibbles_ptr.length);

		return new TitanHexString(temp);
	}

	/**
	 * Concatenates the current hexstring with the hexstring received as a
	 * parameter.
	 *
	 * operator+ in the core.
	 *
	 * @param other_value
	 *                the other value to concatenate with.
	 * @return the new hexstring representing the concatenated value.
	 * */
	public TitanHexString operator_concatenate(final TitanHexString_Element other_value) {
		must_bound("Unbound left operand of hexstring concatenation.");
		other_value.must_bound("Unbound right operand of hexstring element concatenation.");

		final byte temp[] = new byte[nibbles_ptr.length + 1];
		System.arraycopy(nibbles_ptr, 0, temp, 0, nibbles_ptr.length);
		temp[ nibbles_ptr.length ] = (byte) other_value.get_nibble();

		return new TitanHexString(temp);
	}

	/**
	 * Creates a new hexstring with all bit inverted.
	 * 
	 * operator~ in the core.
	 *
	 * @return the new hexstring with the inverted bits.
	 * */
	public TitanHexString not4b() {
		must_bound("Unbound hexstring operand of operator not4b.");

		final int n_bytes = (nibbles_ptr.length + 1) / 2;
		if (n_bytes == 0) {
			return new TitanHexString(this);
		}

		final byte result[] = new byte[nibbles_ptr.length];
		for (int i = 0; i < nibbles_ptr.length; i++) {
			result[i] = (byte)((~nibbles_ptr[i] & 0x0F));
		}

		final TitanHexString ret_val = new TitanHexString(result);
		ret_val.clearUnusedNibble();

		return ret_val;
	}

	/**
	 * Performs a bitwise and operation on this and the provided hexstring.
	 * The resulting value is 1 if both bits are set to 1,
	 *  otherwise the value for the resulting bit is 0.
	 * Both have to be the same length.
	 * 
	 * operator& in the core.
	 *
	 * @param other_value
	 *                the other value.
	 * @return the resulting hexstring.
	 * */
	public TitanHexString and4b(final TitanHexString otherValue) {
		must_bound("Left operand of operator and4b is an unbound hexstring value.");
		otherValue.must_bound("Right operand of operator and4b is an unbound hexstring value.");

		if (nibbles_ptr.length != otherValue.nibbles_ptr.length) {
			throw new TtcnError("The hexstring operands of operator and4b must have the same length.");
		}
		if (nibbles_ptr.length == 0) {
			return new TitanHexString(this);
		}

		final byte result[] = new byte[nibbles_ptr.length];
		for (int i = 0; i < nibbles_ptr.length; i++) {
			result[i] = (byte) (nibbles_ptr[i] & otherValue.nibbles_ptr[i]);
		}

		final TitanHexString ret_val = new TitanHexString(result);
		clearUnusedNibble();

		return ret_val;
	}

	/**
	 * Performs a bitwise and operation on this and the provided hexstring.
	 * The resulting value is 1 if both bits are set to 1,
	 *  otherwise the value for the resulting bit is 0.
	 * Both have to be the same length.
	 * 
	 * operator& in the core.
	 *
	 * @param other_value
	 *                the other value.
	 * @return the resulting hexstring.
	 * */
	public TitanHexString and4b(final TitanHexString_Element otherValue) {
		must_bound("Left operand of operator and4b is an unbound hexstring value.");
		otherValue.must_bound("Right operand of operator and4b is an unbound hexstring element.");

		if (nibbles_ptr.length != 1) {
			throw new TtcnError("The hexstring operands of operator and4b must have the same length.");
		}
		final byte result = (byte) (get_nibble(0) & otherValue.get_nibble());

		return new TitanHexString(result);
	}

	/**
	 * Performs a bitwise or operation on this and the provided hexstring.
	 * the resulting value is 0 if both bits are set to 0,
	 *  otherwise the value for the resulting bit is 1.
	 * Both have to be the same length.
	 * 
	 * operator| in the core.
	 *
	 * @param other_value
	 *                the other value.
	 * @return the resulting hexstring.
	 * */
	public TitanHexString or4b(final TitanHexString otherValue) {
		must_bound("Left operand of operator or4b is an unbound hexstring value.");
		otherValue.must_bound("Right operand of operator or4b is an unbound hexstring value.");

		if (nibbles_ptr.length != otherValue.nibbles_ptr.length) {
			throw new TtcnError("The hexstring operands of operator or4b must have the same length.");
		}
		if (nibbles_ptr.length == 0) {
			return new TitanHexString(this);
		}

		final byte result[] = new byte[nibbles_ptr.length];
		for (int i = 0; i < nibbles_ptr.length; i++) {
			result[i] = (byte) (nibbles_ptr[i] | otherValue.nibbles_ptr[i]);
		}

		final TitanHexString ret_val = new TitanHexString(result);
		clearUnusedNibble();

		return ret_val;
	}

	/**
	 * Performs a bitwise or operation on this and the provided hexstring.
	 * the resulting value is 0 if both bits are set to 0,
	 *  otherwise the value for the resulting bit is 1.
	 * Both have to be the same length.
	 * 
	 * operator| in the core.
	 *
	 * @param other_value
	 *                the other value.
	 * @return the resulting hexstring.
	 * */
	public TitanHexString or4b(final TitanHexString_Element otherValue) {
		must_bound("Left operand of operator or4b is an unbound hexstring value.");
		otherValue.must_bound("Right operand of operator or4b is an unbound hexstring element.");

		if (nibbles_ptr.length != 1) {
			throw new TtcnError("The hexstring operands of operator or4b must have the same length.");
		}
		final byte result = (byte) (get_nibble(0) | otherValue.get_nibble());

		return new TitanHexString(result);
	}

	/**
	 * Performs a bitwise xor operation on this and the provided hexstring.
	 * The resulting value is 0 if both bits are the same,
	 *  otherwise the value for the resulting bit is 1.
	 * Both have to be the same length.
	 * 
	 * operator^ in the core.
	 *
	 * @param other_value
	 *                the other value.
	 * @return the resulting hexstring.
	 * */
	public TitanHexString xor4b(final TitanHexString otherValue) {
		must_bound("Left operand of operator xor4b is an unbound hexstring value.");
		otherValue.must_bound("Right operand of operator xor4b is an unbound hexstring value.");

		if (nibbles_ptr.length != otherValue.nibbles_ptr.length) {
			throw new TtcnError("The hexstring operands of operator xor4b must have the same length.");
		}
		if (nibbles_ptr.length == 0) {
			return new TitanHexString(this);
		}

		final byte result[] = new byte[nibbles_ptr.length];
		for (int i = 0; i < nibbles_ptr.length; i++) {
			result[i] = (byte) (nibbles_ptr[i] ^ otherValue.nibbles_ptr[i]);
		}

		final TitanHexString ret_val = new TitanHexString(result);
		clearUnusedNibble();

		return ret_val;
	}

	/**
	 * Performs a bitwise xor operation on this and the provided hexstring.
	 * The resulting value is 0 if both bits are the same,
	 *  otherwise the value for the resulting bit is 1.
	 * Both have to be the same length.
	 * 
	 * operator^ in the core.
	 *
	 * @param other_value
	 *                the other value.
	 * @return the resulting hexstring.
	 * */
	public TitanHexString xor4b(final TitanHexString_Element otherValue) {
		must_bound("Left operand of operator xor4b is an unbound hexstring value.");
		otherValue.must_bound("Right operand of operator xor4b is an unbound hexstring element.");

		if (nibbles_ptr.length != 1) {
			throw new TtcnError("The hexstring operands of operator xor4b must have the same length.");
		}
		final byte result = (byte) (get_nibble(0) ^ otherValue.get_nibble());
		return new TitanHexString(result);
	}

	/**
	 * Creates a new hexstring, that is the equivalent of the
	 * current one with its elements shifted to the left with the provided
	 * amount and zeros coming in from the right.
	 *
	 * operator<< in the core.
	 *
	 * @param shift_count
	 *                the number of characters to shift left.
	 * @return the new hexstring.
	 * */
	public TitanHexString shift_left(int shift_count) {
		must_bound("Unbound hexstring operand of shift left operator.");

		if (shift_count > 0) {
			if (nibbles_ptr.length == 0) {
				return new TitanHexString(this);
			}

			final int n_nibbles = nibbles_ptr.length;
			final byte result[] = new byte[nibbles_ptr.length];
			if (shift_count > n_nibbles) {
				shift_count = n_nibbles;
			}
			for (int i = 0; i < n_nibbles - shift_count; i++) {
				result[i] = nibbles_ptr[i + shift_count];
			}
			for (int i = n_nibbles - shift_count; i < n_nibbles; i++) {
				result[i] = (byte) 0;
			}

			return new TitanHexString(result);
		} else if (shift_count == 0) {
			return new TitanHexString(this);
		} else {
			return this.shift_right(-shift_count);
		}
	}

	/**
	 * Creates a new hexstring, that is the equivalent of the
	 * current one with its elements shifted to the left with the provided
	 * amount and zeros coming in from the right.
	 *
	 * operator<< in the core.
	 *
	 * @param shift_count
	 *                the number of characters to shift left.
	 * @return the new hexstring.
	 * */
	public TitanHexString shift_left(final TitanInteger shift_count) {
		shift_count.must_bound("Unbound right operand of hexstring shift left operator.");

		return this.shift_left(shift_count.get_int());
	}

	/**
	 * Creates a new hexstring, that is the equivalent of the
	 * current one with its elements shifted to the right with the provided
	 * amount and zeros coming in from the left.
	 *
	 * operator>> in the core.
	 *
	 * @param shift_count
	 *                the number of characters to shift right.
	 * @return the new hexstring.
	 * */
	public TitanHexString shift_right(int shift_count) {
		must_bound("Unbound operand of hexstring shift right operator.");

		if (shift_count > 0) {
			if (nibbles_ptr.length == 0) {
				return new TitanHexString(this);
			}

			final int n_nibbles = nibbles_ptr.length;
			final byte result[] = new byte[nibbles_ptr.length];
			if (shift_count > n_nibbles) {
				shift_count = n_nibbles;
			}
			for (int i = 0; i < shift_count; i++) {
				result[i] = (byte) 0;
			}
			for (int i = 0; i < n_nibbles - shift_count; i++) {
				result[i + shift_count] = nibbles_ptr[i];
			}

			return new TitanHexString(result);
		} else if (shift_count == 0) {
			return new TitanHexString(this);
		} else {
			return this.shift_left(-shift_count);
		}
	}

	/**
	 * Creates a new hexstring, that is the equivalent of the
	 * current one with its elements shifted to the right with the provided
	 * amount and zeros coming in from the left.
	 *
	 * operator>> in the core.
	 *
	 * @param shift_count
	 *                the number of characters to shift right.
	 * @return the new hexstring.
	 * */
	public TitanHexString shift_right(final TitanInteger shift_count) {
		shift_count.must_bound("Unbound right operand of hexstring right left operator.");

		return this.shift_right(shift_count.get_int());
	}

	/**
	 * Creates a new hexstring, that is the equivalent of the
	 * current one with its elements rotated to the left with the provided
	 * amount.
	 *
	 * operator<<= in the core.
	 *
	 * @param rotate_count
	 *                the number of characters to rotate left.
	 * @return the new hexstring.
	 * */
	public TitanHexString rotate_left(int rotate_count) {
		must_bound("Unbound hexstring operand of rotate left operator.");

		if (nibbles_ptr.length == 0) {
			return new TitanHexString(this);
		}
		if (rotate_count >= 0) {
			rotate_count %= nibbles_ptr.length;
			if (rotate_count == 0) {
				return new TitanHexString(this);
			}

			return this.shift_left(rotate_count).or4b(this.shift_right(nibbles_ptr.length - rotate_count));
		} else {
			return this.rotate_right(-rotate_count);
		}
	}

	/**
	 * Creates a new hexstring, that is the equivalent of the
	 * current one with its elements rotated to the left with the provided
	 * amount.
	 *
	 * operator<<= in the core.
	 *
	 * @param rotate_count
	 *                the number of characters to rotate left.
	 * @return the new hexstring.
	 * */
	public TitanHexString rotate_left(final TitanInteger rotate_count) {
		rotate_count.must_bound("Unbound right operand of hexstring rotate left operator.");

		return this.rotate_left(rotate_count.get_int());
	}

	/**
	 * Creates a new hexstring, that is the equivalent of the
	 * current one with its elements rotated to the right with the provided
	 * amount.
	 *
	 * operator>>= in the core.
	 *
	 * @param rotatecount
	 *                the number of characters to rotate right.
	 * @return the new hexstring.
	 * */
	public TitanHexString rotate_right(int rotateCount) {
		must_bound("Unbound hexstring operand of rotate right operator.");

		if (nibbles_ptr.length == 0) {
			return new TitanHexString(this);
		}
		if (rotateCount >= 0) {
			rotateCount %= nibbles_ptr.length;
			if (rotateCount == 0) {
				return new TitanHexString(this);
			}

			return this.shift_right(rotateCount).or4b(this.shift_left(nibbles_ptr.length - rotateCount));
		} else {
			return this.rotate_left(-rotateCount);
		}
	}

	/**
	 * Creates a new hexstring, that is the equivalent of the
	 * current one with its elements rotated to the right with the provided
	 * amount.
	 *
	 * operator>>= in the core.
	 *
	 * @param rotatecount
	 *                the number of characters to rotate right.
	 * @return the new hexstring.
	 * */
	public TitanHexString rotate_right(final TitanInteger rotateCount) {
		rotateCount.must_bound("Unbound right operand of hexstring rotate right operator.");

		return this.rotate_right(rotateCount.get_int());
	}

	@Override
	/** {@inheritDoc} */
	public int RAW_encode(final TTCN_Typedescriptor p_td, final RAW_enc_tree myleaf) {
		if (!is_bound()) {
			TTCN_EncDec_ErrorContext.error(error_type.ET_UNBOUND, "Encoding an unbound value.");
		}
		int nbits = nibbles_ptr.length * 4;
		int align_length = p_td.raw.fieldlength != 0 ? p_td.raw.fieldlength - nbits : 0;
		if ((nbits + align_length) < nbits) {
			TTCN_EncDec_ErrorContext.error(error_type.ET_LEN_ERR, "There is no sufficient bits to encode %s: ", p_td.name);
			nbits = p_td.raw.fieldlength;
			align_length = 0;
		}
		//myleaf.data_ptr_used = true;
		myleaf.data_array = new byte[(nibbles_ptr.length + 1) / 2];

		for (int i = 1; i < nibbles_ptr.length; i += 2) {
			myleaf.data_array[i / 2] = (byte) (nibbles_ptr[i] << 4 | nibbles_ptr[i - 1] & 0x0F);
		}

		if((nibbles_ptr.length & 1) == 1) {
			myleaf.data_array[nibbles_ptr.length / 2] = (byte) (nibbles_ptr[nibbles_ptr.length - 1] & 0x0F);
		}

		if (p_td.raw.endianness == raw_order_t.ORDER_MSB) {
			myleaf.align = -align_length;
		} else {
			myleaf.align = align_length;
		}

		return myleaf.length = nbits + align_length;
	}

	@Override
	/** {@inheritDoc} */
	public int RAW_decode(final TTCN_Typedescriptor p_td, final TTCN_Buffer buff, final int limit, final raw_order_t top_bit_ord) {
		return RAW_decode(p_td, buff, limit, top_bit_ord, false, -1, true, null);
	}

	@Override
	/** {@inheritDoc} */
	public int RAW_decode(final TTCN_Typedescriptor p_td, final TTCN_Buffer buff, int limit, final raw_order_t top_bit_ord, final boolean no_err, final int sel_field, final boolean first_call, final RAW_Force_Omit force_omit) {
		final int prepaddlength = buff.increase_pos_padd(p_td.raw.prepadding);
		limit -= prepaddlength;
		int decode_length = p_td.raw.fieldlength == 0 ? (limit / 4) * 4 : p_td.raw.fieldlength;
		final TTCN_EncDec_ErrorContext errorcontext = new TTCN_EncDec_ErrorContext();
		try {
			if (p_td.raw.fieldlength > limit || p_td.raw.fieldlength > buff.unread_len_bit()) {
				if (no_err) {
					return -error_type.ET_LEN_ERR.ordinal();
				}
				TTCN_EncDec_ErrorContext.error(error_type.ET_LEN_ERR, "There is not enough bits in the buffer to decode type %s.", p_td.name);
				decode_length = ((limit > buff.unread_len_bit() ? buff.unread_len_bit() : limit) / 4) * 4;
			}

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
			cp.hexorder = p_td.raw.hexorder;
			nibbles_ptr = new byte[decode_length / 4];
			final byte[] tmp_nibbles = new byte[decode_length / 4];
			buff.get_b(decode_length, tmp_nibbles, cp, top_bit_ord);
			if(tmp_nibbles.length == 1) {
				nibbles_ptr[0] = tmp_nibbles[0];
			} else {
				for (int i = 0, j = 0; i < nibbles_ptr.length; i += 2, j++) {
					nibbles_ptr[i] = (byte) (tmp_nibbles[j] & 0x0F);

					if(i + 1 == nibbles_ptr.length){ //if decode_length % 2 == 1
						continue;
					}
					nibbles_ptr[i + 1] = (byte) (((tmp_nibbles[j] & 0xFF) >> 4) & 0x0F);
				}
			}

			if (p_td.raw.length_restrition != -1 && decode_length > p_td.raw.length_restrition) {
				if (p_td.raw.endianness == raw_order_t.ORDER_MSB) {
					if ((decode_length - nibbles_ptr.length * 4) % 8 != 0) {
						final int bound = (decode_length - nibbles_ptr.length * 4) % 8;
						final int maxindex = (decode_length - 1) / 8;
						for (int a = 0, b = (decode_length - nibbles_ptr.length * 4 - 1) / 8; a < (nibbles_ptr.length * 4 + 7) / 8; a++, b++) {
							nibbles_ptr[a] = (byte) (nibbles_ptr[b] >> bound);
							if (b < maxindex) {
								nibbles_ptr[a] = (byte) (nibbles_ptr[b + 1] << (8 - bound));
							}
						}
					} else {
						System.arraycopy(nibbles_ptr, (decode_length - nibbles_ptr.length * 4) / 8, nibbles_ptr, 0, nibbles_ptr.length * 8);
					}
				}
			}
			decode_length += buff.increase_pos_padd(p_td.raw.padding);
			clearUnusedNibble();
		} finally {
			errorcontext.leave_context();
		}

		return decode_length + prepaddlength;
	}

	/**
	 * This static function is used to help with implicit conversion, where
	 * there is no better to way.
	 * <p>
	 * This particular function can be easily optimized away in during
	 * execution.
	 *
	 * @param otherValue
	 *                the other value to convert into a TitanHexString.
	 * @return the converted value.
	 * */
	public static TitanHexString convert_to_HexString(final TitanHexString otherValue) {
		return otherValue;
	}

	/**
	 * This static function is used to help with implicit conversion, where
	 * there is no better to way.
	 *
	 * @param otherValue
	 *                the other value to convert into a TitanHexString.
	 * @return the converted value.
	 * */
	public static TitanHexString convert_to_HexString(final TitanHexString_Element otherValue) {
		return new TitanHexString(otherValue);
	}
}
