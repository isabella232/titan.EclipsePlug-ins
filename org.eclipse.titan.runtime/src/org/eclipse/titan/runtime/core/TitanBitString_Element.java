/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;


public class TitanBitString_Element {
	private boolean bound_flag;
	private final TitanBitString str_val;
	private final int bit_pos;

	public TitanBitString_Element(final boolean par_bound_flag, final TitanBitString par_str_val, final int par_bit_pos) {
		bound_flag = par_bound_flag;
		str_val = par_str_val;
		bit_pos = par_bit_pos;
	}

	public boolean is_bound() {
		return bound_flag;
	}

	public boolean is_value() {
		return is_bound();
	}

	/**
	 * Checks that this value is bound or not. Unbound value results in
	 * dynamic testcase error with the provided error message.
	 *
	 * @param errorMessage
	 *                the error message to report.
	 * */
	public void must_bound(final String errorMessage) {
		if (!bound_flag) {
			throw new TtcnError(errorMessage);
		}
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
	public TitanBitString_Element operator_assign(final TitanBitString_Element otherValue) {
		otherValue.must_bound("Assignment of an unbound bitstring element.");

		bound_flag = true;
		str_val.set_bit(bit_pos, otherValue.str_val.get_bit(otherValue.bit_pos));
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
	public TitanBitString_Element operator_assign(final TitanBitString otherValue) {
		otherValue.must_bound("Assignment of unbound bitstring value.");

		if (otherValue.lengthof().get_int() != 1) {
			throw new TtcnError("Assignment of a bitstring value with length other than 1 to a bitstring element.");
		}

		bound_flag = true;
		str_val.set_bit(bit_pos, otherValue.get_bit(0));
		return this;
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
	public boolean operator_equals(final TitanBitString_Element otherValue) {
		must_bound("Unbound left operand of bitstring element comparison.");
		otherValue.must_bound("Unbound right operand of bitstring comparison.");

		return str_val.get_bit(bit_pos) == otherValue.str_val.get_bit(otherValue.bit_pos);
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
	public boolean operator_equals(final TitanBitString otherValue) {
		must_bound("Unbound left operand of bitstring element comparison.");
		otherValue.must_bound("Unbound right operand of bitstring element comparison.");

		if (otherValue.lengthof().get_int() != 1) {
			return false;
		}

		return str_val.get_bit(bit_pos) == otherValue.get_bit(0);
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
	public boolean operator_not_equals(final TitanBitString_Element otherValue) {
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
	public boolean operator_not_equals(final TitanBitString otherValue) {
		return !operator_equals(otherValue);
	}

	/**
	 * Concatenates the current bitstring element with the bitstring received as a
	 * parameter.
	 *
	 * operator+ in the core.
	 *
	 * @param other_value
	 *                the other value to concatenate with.
	 * @return the new bitstring representing the concatenated value.
	 * */
	public TitanBitString operator_concatenate(final TitanBitString other_value) {
		must_bound("Unbound left operand of bitstring element concatenation.");
		other_value.must_bound("Unbound right operand of bitstring concatenation.");

		final int n_bits = other_value.lengthof().get_int();
		final int n_bytes = (n_bits + 7) / 8;
		final int result[] = new int[n_bytes];
		final int temp[] = other_value.get_value();

		result[0] = get_bit() ? 1 : 0;
		for (int byte_count = 0; byte_count < n_bytes; byte_count++) {
			result[byte_count] = (result[byte_count] | temp[byte_count] << 1) & 0xFF;
			if (n_bits > byte_count * 8 + 7) {
				result[byte_count + 1] = (temp[byte_count] & 128) >> 7;
			}
		}

		return new TitanBitString(result, n_bits + 1);
	}

	/**
	 * Concatenates the current bitstring element with the bitstring element
	 * received as a parameter.
	 *
	 * operator+ in the core.
	 *
	 * @param other_value
	 *                the other value to concatenate with.
	 * @return the new bitstring representing the concatenated value.
	 * */
	public TitanBitString operator_concatenate(final TitanBitString_Element other_value) {
		must_bound("Unbound left operand of bitstring element concatenation.");
		other_value.must_bound("Unbound right operand of bitstring element concatenation.");

		int result = str_val.get_bit(bit_pos) ? 1 : 2;
		if (other_value.get_bit()) {
			result = result | 2;
		}
		final int temp_ptr[] = new int[1];
		temp_ptr[0] = result;
		return new TitanBitString(temp_ptr, 2);
	}

	/**
	 * Creates a new bitstring with all bit inverted.
	 * 
	 * operator~ in the core.
	 *
	 * @return the new bitstring with the inverted bits.
	 * */
	public TitanBitString not4b() {
		must_bound("Unbound bitstring element operand of operator not4b.");

		final int result = str_val.get_bit(bit_pos) ? 0 : 1;
		final int dest_ptr[] = new int[1];
		dest_ptr[0] = result;
		return new TitanBitString(dest_ptr, 1);
	}

	// originally operator&
	public TitanBitString and4b(final TitanBitString otherValue) {
		must_bound("Left operand of operator and4b is an unbound bitstring element.");
		otherValue.must_bound("Right operand of operator and4b is an unbound bitstring value.");

		if (otherValue.lengthof().get_int() != 1) {
			throw new TtcnError("The bitstring operands of operator and4b must have the same length.");
		}

		final boolean temp = str_val.get_bit(bit_pos) & otherValue.get_bit(0);
		final int result = temp ? 1 : 0;
		final int dest_ptr[] = new int[1];
		dest_ptr[0] = result;
		return new TitanBitString(dest_ptr, 1);
	}

	// originally operator&
	public TitanBitString and4b(final TitanBitString_Element otherValue) {
		must_bound("Left operand of operator and4b is an unbound bitstring element.");
		otherValue.must_bound("Right operand of operator and4b is an unbound bitstring element.");

		final boolean temp = str_val.get_bit(bit_pos) & otherValue.get_bit();
		final int result = temp ? 1 : 0;
		final int dest_ptr[] = new int[1];
		dest_ptr[0] = result;
		return new TitanBitString(dest_ptr, 1);
	}

	/**
	 * Performs a bitwise or operation on this and the provided bitstring.
	 * the resulting value is 0 if both bits are set to 0,
	 *  otherwise the value for the resulting bit is 1.
	 * Both have to be the same length.
	 * 
	 * operator| in the core.
	 *
	 * @param other_value
	 *                the other value.
	 * @return the resulting bitstring.
	 * */
	public TitanBitString or4b(final TitanBitString otherValue) {
		must_bound("Left operand of operator or4b is an unbound bitstring element.");
		otherValue.must_bound("Right operand of operator or4b is an unbound bitstring value.");

		if (otherValue.lengthof().get_int() != 1) {
			throw new TtcnError("The bitstring operands of operator or4b must have the same length.");
		}

		final boolean temp = str_val.get_bit(bit_pos) | otherValue.get_bit(0);
		final int result = temp ? 1 : 0;
		final int dest_ptr[] = new int[1];
		dest_ptr[0] = result;
		return new TitanBitString(dest_ptr, 1);
	}

	/**
	 * Performs a bitwise or operation on this and the provided bitstring.
	 * the resulting value is 0 if both bits are set to 0,
	 *  otherwise the value for the resulting bit is 1.
	 * Both have to be the same length.
	 * 
	 * operator| in the core.
	 *
	 * @param other_value
	 *                the other value.
	 * @return the resulting bitstring.
	 * */
	public TitanBitString or4b(final TitanBitString_Element otherValue) {
		must_bound("Left operand of operator or4b is an unbound bitstring element.");
		otherValue.must_bound("Right operand of operator or4b is an unbound bitstring element.");

		final boolean temp = str_val.get_bit(bit_pos) | otherValue.get_bit();
		final int result = temp ? 1 : 0;
		final int dest_ptr[] = new int[1];
		dest_ptr[0] = result;
		return new TitanBitString(dest_ptr, 1);
	}

	//originally operator^
	public TitanBitString xor4b(final TitanBitString otherValue) {
		must_bound("Left operand of operator xor4b is an unbound bitstring element.");
		otherValue.must_bound("Right operand of operator xor4b is an unbound bitstring value.");

		if (otherValue.lengthof().get_int() != 1) {
			throw new TtcnError("The bitstring operands of operator xor4b must have the same length.");
		}

		final boolean temp = str_val.get_bit(bit_pos) ^ otherValue.get_bit(0);
		final int result = temp ? 1 : 0;
		final int dest_ptr[] = new int[1];
		dest_ptr[0] = result;
		return new TitanBitString(dest_ptr, 1);
	}

	//originally operator^
	public TitanBitString xor4b(final TitanBitString_Element otherValue) {
		must_bound("Left operand of operator xor4b is an unbound bitstring element.");
		otherValue.must_bound("Right operand of operator xor4b is an unbound bitstring element.");

		final boolean temp = str_val.get_bit(bit_pos) ^ otherValue.get_bit();
		final int result = temp ? 1 : 0;
		final int dest_ptr[] = new int[1];
		dest_ptr[0] = result;
		return new TitanBitString(dest_ptr, 1);
	}

	public boolean get_bit() {
		return str_val.get_bit(bit_pos);
	}

	/**
	 * Logs this value.
	 */
	public void log() {
		if (bound_flag) {
			TTCN_Logger.log_char('\'');
			TTCN_Logger.log_char(str_val.get_bit(bit_pos) ? '1' : '0');
			TTCN_Logger.log_event_str("'B");
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
		final StringBuilder result = new StringBuilder();
		result.append('\'');
		result.append(str_val.get_bit(bit_pos) ? '1' : '0');
		result.append("\'B");

		return result.toString();
	}
}
