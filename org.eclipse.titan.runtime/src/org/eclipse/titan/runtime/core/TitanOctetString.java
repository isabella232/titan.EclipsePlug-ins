/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.regex.Pattern;

import org.eclipse.titan.runtime.core.Param_Types.Module_Parameter;
import org.eclipse.titan.runtime.core.Param_Types.Module_Parameter.basic_check_bits_t;
import org.eclipse.titan.runtime.core.Param_Types.Module_Parameter.expression_operand_t;
import org.eclipse.titan.runtime.core.Param_Types.Module_Parameter.operation_type_t;
import org.eclipse.titan.runtime.core.RAW.RAW_Force_Omit;
import org.eclipse.titan.runtime.core.RAW.RAW_coding_par;
import org.eclipse.titan.runtime.core.RAW.RAW_enc_tr_pos;
import org.eclipse.titan.runtime.core.RAW.RAW_enc_tree;
import org.eclipse.titan.runtime.core.RAW.ext_bit_t;
import org.eclipse.titan.runtime.core.TTCN_EncDec.coding_type;
import org.eclipse.titan.runtime.core.TTCN_EncDec.error_type;
import org.eclipse.titan.runtime.core.TTCN_EncDec.raw_order_t;

/**
 * TTCN-3 octetstring
 * @author Arpad Lovassy
 * @author Farkas Izabella Ingrid
 * @author Andrea Palfi
 */
public class TitanOctetString extends Base_Type {

	// originally octetstring_value_match
	private static final Pattern OCTETSTRING_VALUE_PATTERN = Pattern.compile("^([0-9A-Fa-f]{2})+$");

	private static final String HEX_DIGITS = "0123456789ABCDEF";

	/**
	 * octetstring value.
	 *
	 * Packed storage of hex digit pairs, filled from LSB.
	 */
	private char val_ptr[];

	/**
	 * Initializes to unbound value.
	 * */
	public TitanOctetString() {
		super();
	}

	/**
	 * Initializes to a given value.
	 *
	 * @param otherValue
	 *                the value to initialize to.
	 * */
	public TitanOctetString(final char otherValue[]) {
		val_ptr = TitanString_Utils.copy_char_list(otherValue);
	}

	/**
	 * Initializes to a given value.
	 *
	 * @param otherValue
	 *                the value to initialize to.
	 * */
	public TitanOctetString(final TitanOctetString otherValue) {
		otherValue.must_bound("Copying an unbound octetstring value.");

		val_ptr = TitanString_Utils.copy_char_list(otherValue.val_ptr);
	}

	/**
	 * Initializes to a given value.
	 *
	 * @param otherValue
	 *                the value to initialize to.
	 * */
	public TitanOctetString(final char value) {
		val_ptr = new char[1];
		val_ptr[0] = value;
	}

	/**
	 * Constructor
	 * @param aValue string representation of a octetstring value, without ''B, it contains only [0-9A-F] characters.
	 * NOTE: this is the way octetstring value is stored in Octetstring_Value
	 */
	public TitanOctetString(final String value) {
		val_ptr = octetstr2bytelist(value);
	}

	/**
	 * Converts a string representation of a octetstring to a list of Character
	 * @param aHexString string representation of octetstring, it contains exatcly even number of hex digits
	 * @return value list of the octetstring, groupped in 2 bytes (java Character)
	 */
	private static char[] octetstr2bytelist(final String aHexString) {
		final int len = aHexString.length();
		final char result[] = new char[(len + 1) / 2];
		for (int i = 0; i < len; i += 2) {
			final char hexDigit1 = aHexString.charAt(i);
			final char hexDigit2 = aHexString.charAt(i + 1);
			final char value = octet2value(hexDigit1, hexDigit2);
			result[i / 2] = value;
		}

		return result;
	}

	/**
	 * Converts a string representation of an octet to a value
	 * @param aHexDigit1 1st digit of an octet, string representation of hex digit, possible value: [0-9A-F] characters
	 * @param aHexDigit2 2nd digit of an octet, string representation of hex digit, possible value: [0-9A-F] characters
	 * @return value of the octet
	 */
	private static char octet2value(final char aHexDigit1, final char aHexDigit2) {
		return (char) (16 * TitanHexString.hexdigit2byte(aHexDigit1) + TitanHexString.hexdigit2byte(aHexDigit2));
	}

	/** Return the nibble at index i
	 *
	 * @param nibble_index
	 * @return
	 */
	public char get_nibble(final int nibble_index) {
		return val_ptr[nibble_index];
	}

	public void set_nibble(final int nibble_index, final char new_value) {
		val_ptr[nibble_index] = new_value;
	}

	//originally char*()
	public char[] get_value() {
		return val_ptr;
	}

	// takes ownership of aOtherValue
	public void set_value(final char[] aOtherValue) {
		val_ptr = aOtherValue;
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
	public TitanOctetString operator_assign(final TitanOctetString otherValue) {
		otherValue.must_bound("Assignment of an unbound octetstring value.");

		if (otherValue != this) {
			val_ptr = otherValue.val_ptr;
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
	public TitanOctetString operator_assign(final TitanOctetString_Element otherValue) {
		otherValue.must_bound("Assignment of an unbound octetstring element to an octetstring.");
		val_ptr = new char[1];
		val_ptr[0] = otherValue.get_nibble();

		return this;
	}

	@Override
	public TitanOctetString operator_assign(final Base_Type otherValue) {
		if (otherValue instanceof TitanOctetString) {
			return operator_assign((TitanOctetString)otherValue);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to octetstring", otherValue));
	}

	@Override
	public boolean is_bound() {
		return val_ptr != null;
	}

	@Override
	public boolean is_value() {
		return val_ptr != null;
	}

	// originally lengthof
	public TitanInteger lengthof() {
		must_bound("Performing lengthof operation on an unbound octetstring value.");

		return new TitanInteger(val_ptr.length);
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
	public boolean operator_equals(final TitanOctetString otherValue) {
		must_bound("Unbound left operand of octetstring comparison.");
		otherValue.must_bound("Unbound right operand of octetstring comparison.");

		return Arrays.equals(val_ptr, otherValue.val_ptr);
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
	public boolean operator_equals(final TitanOctetString_Element otherValue) {
		must_bound("Unbound left operand of octetstring comparison.");
		otherValue.must_bound("Unbound right operand of octetstring comparison.");

		return otherValue.operator_equals(this);
		// new TitanBoolean(val_ptr.equals( otherValue.get_nibble()));
	}

	@Override
	public boolean operator_equals(final Base_Type otherValue) {
		if (otherValue instanceof TitanOctetString) {
			return operator_equals((TitanOctetString)otherValue);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to octetstring", otherValue));
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
	public boolean operator_not_equals(final TitanOctetString otherValue) {
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
	public boolean operator_not_equals(final TitanOctetString_Element otherValue) {
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
	public boolean operator_not_equals(final Base_Type otherValue) {
		return !operator_equals(otherValue);
	}

	@Override
	public void clean_up() {
		val_ptr = null;
	}

	/**
	 * Gives access to the given element. Indexing begins from zero.
	 * Over-indexing by 1 extends the octetstring.
	 *
	 * operator[] in the core.
	 *
	 * @param index_value
	 *            the index of the element to return.
	 * @return the element at the specified position in this octetstring
	 * */
	public TitanOctetString_Element get_at(final int index_value) {
		if (val_ptr == null && index_value == 0) {
			val_ptr = new char[1];
			return new TitanOctetString_Element(false, this, 0);
		} else {
			must_bound("Accessing an element of an unbound octetstring value.");

			if (index_value < 0) {
				throw new TtcnError("Accessing an octetstring element using a negative index (" + index_value + ").");
			}

			final int n_nibbles = val_ptr.length;
			if (index_value > n_nibbles) {
				throw new TtcnError("Index overflow when accessing a octetstring element: The index is " + index_value +
						", but the string has only " + n_nibbles + " hexadecimal digits.");
			}
			if (index_value == n_nibbles) {
				final char temp[] = new char[val_ptr.length + 1];
				System.arraycopy(val_ptr, 0, temp, 0, val_ptr.length);
				val_ptr = temp;
				return new TitanOctetString_Element(false, this, index_value);
			} else {
				return new TitanOctetString_Element(true, this, index_value);
			}
		}
	}

	/**
	 * Gives access to the given element. Indexing begins from zero.
	 * Over-indexing by 1 extends the octetstring.
	 *
	 * operator[] in the core.
	 *
	 * @param index_value
	 *            the index of the element to return.
	 * @return the element at the specified position in this octetstring
	 * */
	public TitanOctetString_Element get_at(final TitanInteger index_value) {
		index_value.must_bound("Indexing a octetstring value with an unbound integer value.");

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
	 * @return the element at the specified position in this octetstring
	 * */
	public final TitanOctetString_Element constGet_at(final int index_value) {
		must_bound("Accessing an element of an unbound octetstring value.");

		if (index_value < 0) {
			throw new TtcnError("Accessing an octetstring element using a negative index (" + index_value + ").");
		}

		final int n_nibbles = val_ptr.length;
		if (index_value >= n_nibbles) {
			throw new TtcnError("Index overflow when accessing a octetstring element: The index is " + index_value +
					", but the string has only " + n_nibbles + " hexadecimal digits.");
		}
		return new TitanOctetString_Element(true, this, index_value);
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
	 * @return the element at the specified position in this octetstring
	 * */
	public final TitanOctetString_Element constGet_at(final TitanInteger index_value) {
		index_value.must_bound("Indexing a octetstring value with an unbound integer value.");

		return constGet_at(index_value.get_int());
	}

	@Override
	public void log() {
		if (val_ptr != null) {
			boolean onlyPrintable = true;
			TTCN_Logger.log_char('\'');
			for (int i = 0; i < val_ptr.length; i++) {
				final char octet = val_ptr[i];
				TTCN_Logger.log_octet(octet); // get_nibble(i)
				if (onlyPrintable && !(TTCN_Logger.is_printable(octet))) {
					onlyPrintable = false;
				}
			}
			TTCN_Logger.log_event_str("'O");
			if (onlyPrintable && val_ptr.length > 0) {
				TTCN_Logger.log_event_str("(\"");
				for (int i = 0; i < val_ptr.length; i++) {
					TTCN_Logger.log_char_escaped(val_ptr[i]);
				}
				TTCN_Logger.log_event_str("\")");
			}
		} else {
			TTCN_Logger.log_event_unbound();
		}
	}

	@Override
	/** {@inheritDoc} */
	public void set_param(final Module_Parameter param) {
		param.basic_check(basic_check_bits_t.BC_VALUE.getValue(), "octetstring value");
		switch (param.get_type()) {
		case MP_Octetstring:
			switch (param.get_operation_type()) {
			case OT_ASSIGN:
				clean_up();
				val_ptr = new char[param.get_string_size()];
				System.arraycopy((char[])param.get_string_data(), 0, val_ptr, 0, param.get_string_size());
				break;
			case OT_CONCAT:
				if (is_bound()) {
					this.operator_assign(this.operator_concatenate(new TitanOctetString((char[]) param.get_string_data())));
				} else {
					this.operator_assign(new TitanOctetString((char[]) param.get_string_data()));
				}
				break;
			default:
				throw new TtcnError("Internal error: OCTETSTRING::set_param()");
			}
			break;
		case MP_Expression:
			if (param.get_expr_type() == expression_operand_t.EXPR_CONCATENATE) {
				final TitanOctetString operand1 = new TitanOctetString();
				final TitanOctetString operand2 = new TitanOctetString();
				operand1.set_param(param.get_operand1());
				operand2.set_param(param.get_operand2());
				if (param.get_operation_type() == operation_type_t.OT_CONCAT) {
					this.operator_assign(this.operator_concatenate(operand1).operator_concatenate(operand2));
				} else {
					this.operator_assign(operand1.operator_concatenate(operand2));
				}
			} else {
				param.expr_type_error("a octetstring");
			}
			break;
		default:
			param.type_error("octetstring value");
			break;
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
		if (val_ptr == null) {
			return "<unbound>";
		}

		final StringBuilder sb = new StringBuilder();
		sb.append('\'');
		final int size = val_ptr.length;
		for (int i = 0; i < size; i++) {
			final int digit = val_ptr[i];
			sb.append(HEX_DIGITS.charAt((digit & 0xF0)>>4));
			sb.append(HEX_DIGITS.charAt(digit & 0x0F));
		}

		sb.append("\'O");

		return sb.toString();
	}

	@Override
	/** {@inheritDoc} */
	public void encode_text(final Text_Buf text_buf) {
		must_bound("Text encoder: Encoding an unbound octetstring value.");

		final int octets = val_ptr.length;
		text_buf.push_int(octets);
		if (octets > 0) {
			byte[] temp = new byte[octets];
			for (int i = 0; i < octets; i++) {
				temp[i] = (byte)(val_ptr[i] & 0xFF);
			}
			text_buf.push_raw(temp.length, temp);
		}
	}

	@Override
	/** {@inheritDoc} */
	public void decode_text(final Text_Buf text_buf) {
		clean_up();

		final int n_octets = text_buf.pull_int().get_int();
		if (n_octets < 0) {
			throw new TtcnError("Text decoder: Invalid length was received for an octetstring.");
		}

		val_ptr = new char[n_octets];
		if (n_octets > 0) {
			
			final byte[] temp = new byte[n_octets];
			text_buf.pull_raw(n_octets, temp);
			for (int i = 0; i < n_octets; i++) {
				val_ptr[i] = (char) (temp[i] & 0xFF);
			}
		}
	}

	@Override
	public boolean is_present() {
		return is_bound();
	}

	/**
	 * Concatenates the current octetstring with the octetstring received as a
	 * parameter.
	 *
	 * operator+ in the core.
	 *
	 * @param other_value
	 *                the other value to concatenate with.
	 * @return the new octetstring representing the concatenated value.
	 * */
	public TitanOctetString operator_concatenate(final TitanOctetString other_value) {
		must_bound("Unbound left operand of octetstring concatenation.");
		other_value.must_bound("Unbound right operand of octetstring concatenation.");

		if (val_ptr.length == 0) {
			return new TitanOctetString(other_value);
		}
		if (other_value.val_ptr.length == 0) {
			return new TitanOctetString(this);
		}

		final char temp[] = new char[val_ptr.length + other_value.val_ptr.length];
		System.arraycopy(val_ptr, 0, temp, 0, val_ptr.length);
		System.arraycopy(other_value.val_ptr, 0, temp, val_ptr.length, other_value.val_ptr.length);

		return new TitanOctetString(temp);
	}

	/**
	 * Concatenates the current octetstring with the octetstring received as a
	 * parameter.
	 *
	 * operator+ in the core.
	 *
	 * @param other_value
	 *                the other value to concatenate with.
	 * @return the new octetstring representing the concatenated value.
	 * */
	public TitanOctetString operator_concatenate(final TitanOctetString_Element other_value) {
		must_bound("Unbound left operand of octetstring concatenation.");
		other_value.must_bound("Unbound right operand of octetstring element concatenation.");

		final char temp[] = new char[val_ptr.length + 1];
		System.arraycopy(val_ptr, 0, temp, 0, val_ptr.length);
		temp[val_ptr.length] = other_value.get_nibble();

		return new TitanOctetString(temp);
	}

	/**
	 * Creates a new hexstring with all bit inverted.
	 * 
	 * operator~ in the core.
	 *
	 * @return the new hexstring with the inverted bits.
	 * */
	public TitanOctetString not4b() {
		must_bound("Unbound octetstring operand of operator not4b.");

		final TitanOctetString result = new TitanOctetString();
		result.val_ptr = new char[val_ptr.length];
		for (int i = 0; i < val_ptr.length; i++) {
			final int digit1 = val_ptr[i] / 16;
			final int digit2 = val_ptr[i] % 16;
			final int negDigit1 = ~digit1 & 0x0F;
			final int negDigit2 = ~digit2 & 0x0F;
			result.val_ptr[i] = (char)((negDigit1  << 4) + negDigit2);
		}

		return result;
	}

	/**
	 * Performs a bitwise and operation on this and the provided octetstring.
	 * The resulting value is 1 if both bits are set to 1,
	 *  otherwise the value for the resulting bit is 0.
	 * Both have to be the same length.
	 * 
	 * operator& in the core.
	 *
	 * @param other_value
	 *                the other value.
	 * @return the resulting octetstring.
	 * */
	public TitanOctetString and4b(final TitanOctetString otherValue) {
		must_bound("Left operand of operator and4b is an unbound octetstring value.");
		otherValue.must_bound("Right operand of operator and4b is an unbound octetstring value.");

		if (val_ptr.length != otherValue.val_ptr.length) {
			throw new TtcnError("The octetstring operands of operator and4b must have the same length.");
		}

		final TitanOctetString result = new TitanOctetString();
		result.val_ptr = new char[val_ptr.length];

		for (int i = 0; i < val_ptr.length; i++) {
			result.val_ptr[i] = (char) (val_ptr[i] & otherValue.val_ptr[i]);
		}

		return result;
	}

	/**
	 * Performs a bitwise and operation on this and the provided octetstring.
	 * The resulting value is 1 if both bits are set to 1,
	 *  otherwise the value for the resulting bit is 0.
	 * Both have to be the same length.
	 * 
	 * operator& in the core.
	 *
	 * @param other_value
	 *                the other value.
	 * @return the resulting octetstring.
	 * */
	public TitanOctetString and4b(final TitanOctetString_Element otherValue) {
		must_bound("Left operand of operator and4b is an unbound octetstring value.");
		otherValue.must_bound("Right operand of operator and4b is an unbound octetstring value.");

		if (val_ptr.length != 1) {
			throw new TtcnError("The octetstring operands of operator and4b must have the same length.");
		}

		return new TitanOctetString((char)(val_ptr[0] & otherValue.get_nibble()));
	}

	/**
	 * Performs a bitwise or operation on this and the provided octetstring.
	 * the resulting value is 0 if both bits are set to 0,
	 *  otherwise the value for the resulting bit is 1.
	 * Both have to be the same length.
	 * 
	 * operator| in the core.
	 *
	 * @param other_value
	 *                the other value.
	 * @return the resulting octetstring.
	 * */
	public TitanOctetString or4b(final TitanOctetString otherValue) {
		must_bound("Left operand of operator or4b is an unbound octetstring value.");
		otherValue.must_bound("Right operand of operator or4b is an unbound octetstring value.");

		if (val_ptr.length != otherValue.val_ptr.length) {
			throw new TtcnError("The octetstring operands of operator or4b must have the same length.");
		}

		final TitanOctetString result = new TitanOctetString();
		result.val_ptr = new char[val_ptr.length];
		for (int i = 0; i < val_ptr.length; i++) {
			result.val_ptr[i] = (char) (val_ptr[i] | otherValue.val_ptr[i]);
		}

		return result;

	}

	/**
	 * Performs a bitwise or operation on this and the provided octetstring.
	 * the resulting value is 0 if both bits are set to 0,
	 *  otherwise the value for the resulting bit is 1.
	 * Both have to be the same length.
	 * 
	 * operator| in the core.
	 *
	 * @param other_value
	 *                the other value.
	 * @return the resulting octetstring.
	 * */
	public TitanOctetString or4b(final TitanOctetString_Element otherValue) {
		must_bound("Left operand of operator or4b is an unbound octetstring value.");
		otherValue.must_bound("Right operand of operator or4b is an unbound octetstring value.");

		if (val_ptr.length != 1) {
			throw new TtcnError("The octetstring operands of operator or4b must have the same length.");
		}

		return new TitanOctetString((char)(val_ptr[0] | otherValue.get_nibble()));
	}

	//originally operator^
	public TitanOctetString xor4b(final TitanOctetString otherValue) {
		must_bound("Left operand of operator xor4b is an unbound octetstring value.");
		otherValue.must_bound("Right operand of operator xor4b is an unbound octetstring value.");

		if (val_ptr.length != otherValue.val_ptr.length) {
			throw new TtcnError("The octetstring operands of operator xor4b must have the same length.");
		}

		final TitanOctetString result = new TitanOctetString();
		result.val_ptr = new char[val_ptr.length];
		for (int i = 0; i < val_ptr.length; i++) {
			result.val_ptr[i] = (char)(val_ptr[i] ^ otherValue.val_ptr[i]);
		}

		return result;
	}

	//originally operator^
	public TitanOctetString xor4b(final TitanOctetString_Element otherValue) {
		must_bound("Left operand of operator xor4b is an unbound octetstring value.");
		otherValue.must_bound("Right operand of operator xor4b is an unbound octetstring element.");

		if (val_ptr.length != 1) {
			throw new TtcnError("The octetstring operands of operator xor4b must have the same length.");
		}

		return new TitanOctetString((char)(val_ptr[0] ^ otherValue.get_nibble()));
	}

	/**
	 * Creates a new octetstring, that is the equivalent of the
	 * current one with its elements shifted to the left with the provided
	 * amount and zeros coming in from the right.
	 *
	 * operator<< in the core.
	 *
	 * @param shift_count
	 *                the number of characters to shift left.
	 * @return the new octetstring.
	 * */
	public TitanOctetString shift_left(int shift_count) {
		must_bound("Unbound octetstring operand of shift left operator.");

		if (shift_count > 0) {
			if (val_ptr.length == 0) {
				return this;
			}

			final TitanOctetString result = new TitanOctetString();
			result.val_ptr = new char[val_ptr.length];
			if (shift_count > val_ptr.length) {
				shift_count = val_ptr.length;
			}

			System.arraycopy(val_ptr, shift_count, result.val_ptr, 0, val_ptr.length - shift_count);

			for (int i = val_ptr.length - shift_count; i < val_ptr.length; i++) {
				result.val_ptr[i] = (char) 0;
			}

			return result;
		} else {
			if (shift_count == 0) {
				return this;
			} else {
				return this.shift_right(-shift_count);
			}
		}
	}

	/**
	 * Creates a new octetstring, that is the equivalent of the
	 * current one with its elements shifted to the left with the provided
	 * amount and zeros coming in from the right.
	 *
	 * operator<< in the core.
	 *
	 * @param shift_count
	 *                the number of characters to shift left.
	 * @return the new octetstring.
	 * */
	public TitanOctetString shift_left(final TitanInteger shift_count) {
		shift_count.must_bound("Unbound right operand of octetstring shift left operator.");

		return shift_left(shift_count.get_int());
	}

	/**
	 * Creates a new octetstring, that is the equivalent of the
	 * current one with its elements shifted to the right with the provided
	 * amount and zeros coming in from the left.
	 *
	 * operator>> in the core.
	 *
	 * @param shift_count
	 *                the number of characters to shift right.
	 * @return the new octetstring.
	 * */
	public TitanOctetString shift_right(int shift_count) {
		must_bound("Unbound octetstring operand of shift right operator.");

		if (shift_count > 0) {
			if (val_ptr.length == 0) {
				return this;
			}

			final TitanOctetString result = new TitanOctetString();
			result.val_ptr = new char[val_ptr.length];
			if (shift_count > val_ptr.length) {
				shift_count = val_ptr.length;
			}
			for (int i = 0; i < shift_count; i++) {
				result.val_ptr[i] = (char) 0;
			}
			System.arraycopy(val_ptr, 0, result.val_ptr, shift_count, val_ptr.length - shift_count);

			return result;
		} else {
			if (shift_count == 0) {
				return this;
			} else {
				return this.shift_left(-shift_count);
			}
		}
	}

	/**
	 * Creates a new octetstring, that is the equivalent of the
	 * current one with its elements shifted to the right with the provided
	 * amount and zeros coming in from the left.
	 *
	 * operator>> in the core.
	 *
	 * @param shift_count
	 *                the number of characters to shift right.
	 * @return the new octetstring.
	 * */
	public TitanOctetString shift_right(final TitanInteger shift_count){
		shift_count.must_bound("Unbound right operand of octetstring shift right operator.");

		return shift_right(shift_count.get_int());
	}

	/**
	 * Creates a new octetstring, that is the equivalent of the
	 * current one with its elements rotated to the left with the provided
	 * amount.
	 *
	 * operator<<= in the core.
	 *
	 * @param rotate_count
	 *                the number of characters to rotate left.
	 * @return the new octetstring.
	 * */
	public TitanOctetString rotate_left(int rotate_count) {
		must_bound("Unbound octetstring operand of rotate left operator.");

		if (val_ptr.length == 0) {
			return this;
		}
		if (rotate_count >= 0) {
			rotate_count = rotate_count % val_ptr.length;
			if (rotate_count == 0) {
				return this;
			}

			final TitanOctetString result = new TitanOctetString();
			result.val_ptr = new char[val_ptr.length];
			System.arraycopy(val_ptr, rotate_count, result.val_ptr, 0, val_ptr.length - rotate_count);
			System.arraycopy(val_ptr, 0, result.val_ptr, val_ptr.length - rotate_count, rotate_count);

			return result;
		} else {
			return rotate_right(-rotate_count);
		}
	}

	/**
	 * Creates a new octetstring, that is the equivalent of the
	 * current one with its elements rotated to the left with the provided
	 * amount.
	 *
	 * operator<<= in the core.
	 *
	 * @param rotate_count
	 *                the number of characters to rotate left.
	 * @return the new octetstring.
	 * */
	public TitanOctetString rotate_left(final TitanInteger rotate_count) {
		rotate_count.must_bound("Unbound right operand of octetstring rotate left operator.");

		return rotate_left(rotate_count.get_int());
	}

	/**
	 * Creates a new octetstring, that is the equivalent of the
	 * current one with its elements rotated to the right with the provided
	 * amount.
	 *
	 * operator>>= in the core.
	 *
	 * @param rotate_count
	 *                the number of characters to rotate right.
	 * @return the new octetstring.
	 * */
	public TitanOctetString rotate_right(int rotate_count) {
		must_bound("Unbound octetstring operand of rotate right operator.");

		if (val_ptr.length == 0) {
			return this;
		}
		if (rotate_count >= 0) {
			rotate_count = rotate_count % val_ptr.length;
			if (rotate_count == 0) {
				return this;
			}

			final TitanOctetString result = new TitanOctetString();
			result.val_ptr = new char[val_ptr.length];
			if (rotate_count > val_ptr.length) {
				rotate_count = val_ptr.length;
			}

			System.arraycopy(val_ptr, val_ptr.length - rotate_count, result.val_ptr, 0, rotate_count);
			System.arraycopy(val_ptr, 0, result.val_ptr, rotate_count, val_ptr.length - rotate_count);

			return result;
		} else {
			return rotate_left(-rotate_count);
		}
	}

	/**
	 * Creates a new octetstring, that is the equivalent of the
	 * current one with its elements rotated to the right with the provided
	 * amount.
	 *
	 * operator>>= in the core.
	 *
	 * @param rotate_count
	 *                the number of characters to rotate right.
	 * @return the new octetstring.
	 * */
	public TitanOctetString rotate_right(final TitanInteger rotate_count) {
		rotate_count.must_bound("Unbound right operand of octetstring rotate left operator.");

		return rotate_right(rotate_count.get_int());
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

			final RAW_enc_tr_pos tree_position = new RAW_enc_tr_pos(0, null);
			final RAW_enc_tree root = new RAW_enc_tree(true, null, tree_position, 1, p_td.raw);
			RAW_encode(p_td, root);
			root.put_to_buf(p_buf);

			errorContext.leave_context();
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

			errorContext.leave_context();
			break;
		default:
			throw new TtcnError(MessageFormat.format("Unknown coding method requested to decode type '{0}'", p_td.name));
		}
	}

	@Override
	/** {@inheritDoc} */
	public int RAW_encode(final TTCN_Typedescriptor p_td, final RAW_enc_tree myleaf) {
		if (!is_bound()) {
			TTCN_EncDec_ErrorContext.error(error_type.ET_UNBOUND, "Encoding an unbound value.");
		}

		final TTCN_EncDec_ErrorContext errorcontext = new TTCN_EncDec_ErrorContext();
		char[] bc = new char[val_ptr.length];
		int bl = val_ptr.length * 8;
		int align_length = p_td.raw.fieldlength != 0 ? p_td.raw.fieldlength - bl : 0;
		int blength = val_ptr.length;
		if ((bl + align_length) < val_ptr.length * 8) {
			TTCN_EncDec_ErrorContext.error(error_type.ET_LEN_ERR, "There are insufficient bits to encode '%s' : ", p_td.name);
			blength = p_td.raw.fieldlength / 8;
			bl = p_td.raw.fieldlength;
			align_length = 0;
		}
		if (p_td.raw.extension_bit != ext_bit_t.EXT_BIT_NO && myleaf.coding_par.bitorder == raw_order_t.ORDER_MSB) {
			if (blength > RAW.RAW_INT_ENC_LENGTH) {
				myleaf.data_array = new char[blength];
			} else {
				bc = myleaf.data_array;
			}
			for (int a = 0; a < blength; a++){
				bc[a] = (char) (val_ptr[a] << 1);
			}
		} else {
			myleaf.data_array = val_ptr;
		}
		if (p_td.raw.endianness == raw_order_t.ORDER_MSB) {
			myleaf.align = align_length;
		} else {
			myleaf.align = -align_length;
		}
		errorcontext.leave_context();

		return myleaf.length = bl + align_length;
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
		final TTCN_EncDec_ErrorContext errorcontext = new TTCN_EncDec_ErrorContext();
		limit -= prepaddlength;
		int decode_length = p_td.raw.fieldlength == 0 ? (limit / 8) * 8 : p_td.raw.fieldlength;
		if (decode_length > limit || decode_length > buff.unread_len_bit()) {
			if (no_err) {
				errorcontext.leave_context();
				return -TTCN_EncDec.error_type.ET_LEN_ERR.ordinal();
			}
			TTCN_EncDec_ErrorContext.error(error_type.ET_LEN_ERR, "There is not enough bits in the buffer to decode type '%s'.", p_td.name);
			decode_length = ((limit > (int) buff.unread_len_bit() ? buff.unread_len_bit() : limit) / 8) * 8;
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
		cp.hexorder = raw_order_t.ORDER_LSB;
		if (p_td.raw.extension_bit != ext_bit_t.EXT_BIT_NO) {
			final char[] data = buff.get_read_data();
			int count = 1;
			final int rot = top_bit_ord == raw_order_t.ORDER_LSB ? 0 : 7;
			if (p_td.raw.extension_bit == ext_bit_t.EXT_BIT_YES) {
				while (((data[count - 1] >> rot) & 0x01) == 0 && count * 8 < decode_length) {
					count++;
				}
			} else {
				while (((data[count - 1] >> rot) & 0x01) == 1 && count * 8 < decode_length) {
					count++;
				}
			}
			decode_length = count * 8;
		}
		val_ptr = null;
		val_ptr = new char[decode_length / 8];
		buff.get_b(decode_length, val_ptr, cp, top_bit_ord);
		if (p_td.raw.length_restrition != -1 && decode_length > p_td.raw.length_restrition) {
			if (p_td.raw.endianness == raw_order_t.ORDER_MSB) {
				System.arraycopy(val_ptr, decode_length / 8 - val_ptr.length, val_ptr, 0, val_ptr.length);
			}
		}
		if (p_td.raw.extension_bit != ext_bit_t.EXT_BIT_NO && cp.bitorder == raw_order_t.ORDER_MSB) {
			for (int a = 0; a < decode_length / 8; a++) {
				val_ptr[a] = (char) (val_ptr[a] >> 1 | val_ptr[a] << 7);
			}
		}
		decode_length += buff.increase_pos_padd(p_td.raw.padding);
		errorcontext.leave_context();

		return decode_length + prepaddlength;
	}
}
