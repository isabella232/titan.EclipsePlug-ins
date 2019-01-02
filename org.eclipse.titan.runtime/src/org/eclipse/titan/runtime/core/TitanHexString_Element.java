/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;



public class TitanHexString_Element {
	private boolean bound_flag;
	private final TitanHexString str_val;
	private final int nibble_pos;

	public TitanHexString_Element(final boolean par_bound_flag, final TitanHexString par_str_val, final int par_nibble_pos) {
		bound_flag = par_bound_flag;
		str_val = par_str_val;
		nibble_pos = par_nibble_pos;
	}

	public boolean is_bound() {
		return bound_flag;
	}

	public boolean is_value() {
		return bound_flag;
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
		if (str_val == null) {
			return "<unbound>";
		}

		final StringBuilder sb = new StringBuilder();
		final Byte digit = str_val.get_nibble(nibble_pos);
		sb.append(TitanHexString.HEX_DIGITS.charAt(digit));

		return sb.toString();
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
	public TitanHexString_Element operator_assign(final TitanHexString_Element otherValue) {
		otherValue.must_bound("Assignment of an unbound hexstring element.");

		bound_flag = true;
		str_val.set_nibble(nibble_pos, otherValue.str_val.get_nibble(otherValue.nibble_pos));
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
	public TitanHexString_Element operator_assign(final TitanHexString otherValue) {
		otherValue.must_bound("Assignment of unbound hexstring value.");

		if (otherValue.get_value().length != 1) {
			throw new TtcnError("Assignment of a hexstring value with length other than 1 to a hexstring element.");
		}

		bound_flag = true;
		str_val.set_nibble(nibble_pos, otherValue.get_nibble(0));
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
	public boolean operator_equals(final TitanHexString_Element otherValue) {
		must_bound("Unbound left operand of hexstring element comparison.");
		otherValue.must_bound("Unbound right operand of hexstring comparison.");

		return str_val.get_nibble(nibble_pos) == otherValue.str_val.get_nibble(otherValue.nibble_pos);
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
		must_bound("Unbound left operand of hexstring element comparison.");
		otherValue.must_bound("Unbound right operand of hexstring element comparison.");

		if (otherValue.get_value().length != 1) {
			return false;
		}

		return str_val.get_nibble(nibble_pos) == otherValue.get_nibble(0);
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
		must_bound("Unbound left operand of hexstring element concatenation.");
		other_value.must_bound("Unbound right operand of hexstring concatenation.");

		final byte src_ptr[] = other_value.get_value();
		final int n_nibbles = src_ptr.length;
		final byte dest_ptr[] = new byte[1 + n_nibbles];
		dest_ptr[0] = str_val.get_nibble(nibble_pos);
		System.arraycopy(src_ptr, 0, dest_ptr, 1, n_nibbles);

		return new TitanHexString(dest_ptr);
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
		must_bound("Unbound left operand of hexstring element concatenation.");
		other_value.must_bound("Unbound right operand of hexstring element concatenation.");

		return new TitanHexString(other_value.str_val);
	}

	// originally operator~
	public TitanHexString not4b() {
		must_bound("Unbound hexstring element operand of operator not4b.");

		final byte result = (byte) (~str_val.get_nibble(nibble_pos) & 0x0F);
		return new TitanHexString(result);
	}

	// originally operator&
	public TitanHexString and4b(final TitanHexString other_value) {
		must_bound("Left operand of operator and4b is an unbound hexstring element.");
		other_value.must_bound("Right operand of operator and4b is an unbound hexstring value.");

		if (other_value.get_value().length != 1) {
			throw new TtcnError("The hexstring operands of operator and4b must have the same length.");
		}

		final byte result = (byte) (str_val.get_nibble(nibble_pos) & other_value.get_nibble(0));
		return new TitanHexString(result);
	}

	// originally operator&
	public TitanHexString and4b(final TitanHexString_Element other_value) {
		must_bound("Left operand of operator and4b is an unbound hexstring element.");
		other_value.must_bound("Right operand of operator and4b is an unbound hexstring element.");

		final byte result = (byte) (str_val.get_nibble(nibble_pos) & other_value.str_val.get_nibble(other_value.nibble_pos));
		return new TitanHexString(result);
	}

	// originally operator|
	public TitanHexString or4b(final TitanHexString other_value) {
		must_bound("Left operand of operator or4b is an unbound hexstring element.");
		other_value.must_bound("Right operand of operator or4b is an unbound hexstring value.");

		if (other_value.get_value().length != 1) {
			throw new TtcnError("The hexstring operands of operator or4b must have the same length.");
		}

		final byte result = (byte) (str_val.get_nibble(nibble_pos) | other_value.get_nibble(0));
		return new TitanHexString(result);
	}

	// originally operator|
	public TitanHexString or4b(final TitanHexString_Element other_value) {
		must_bound("Left operand of operator or4b is an unbound hexstring element.");
		other_value.must_bound("Right operand of operator or4b is an unbound hexstring element.");

		final byte result = (byte) (str_val.get_nibble(nibble_pos) | other_value.str_val.get_nibble(other_value.nibble_pos));
		return new TitanHexString(result);
	}

	// originally operator^
	public TitanHexString xor4b(final TitanHexString other_value) {
		must_bound("Left operand of operator xor4b is an unbound hexstring element.");
		other_value.must_bound("Right operand of operator xor4b is an unbound hexstring value.");

		if (other_value.get_value().length != 1) {
			throw new TtcnError("The hexstring operands of operator xor4b must have the same length.");
		}

		final byte result = (byte) (str_val.get_nibble(nibble_pos) ^ other_value.get_nibble(0));
		return new TitanHexString(result);
	}

	// originally operator^
	public TitanHexString xor4b(final TitanHexString_Element other_value) {
		must_bound("Left operand of operator xor4b is an unbound hexstring element.");
		other_value.must_bound("Right operand of operator xor4b is an unbound hexstring element.");

		final byte result = (byte) (str_val.get_nibble(nibble_pos) ^ other_value.str_val.get_nibble(other_value.nibble_pos));
		return new TitanHexString(result);
	}

	public char get_nibble() {
		return (char) str_val.get_nibble(nibble_pos);
	}

	/**
	 * Logs this value element.
	 */
	public void log() {
		if (bound_flag) {
			TTCN_Logger.log_char('\'');
			TTCN_Logger.log_hex(str_val.get_nibble(nibble_pos));
			TTCN_Logger.log_event_str("'H");
		} else {
			TTCN_Logger.log_event_unbound();
		}
	}
}
