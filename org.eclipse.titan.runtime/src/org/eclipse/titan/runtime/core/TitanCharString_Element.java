/******************************************************************************
 * Copyright (c) 2000-2020 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

import java.util.ArrayList;
import java.util.List;

/**
 * originally universal_char
 * Represents UTF-32 character
 *
 * @author Arpad Lovassy
 * @author Andrea Palfi
 */
public class TitanCharString_Element {
	private boolean bound_flag;
	private final TitanCharString str_val;
	private final int char_pos;

	public TitanCharString_Element(final boolean par_bound_flag, final TitanCharString par_str_val, final int par_char_pos) {
		bound_flag = par_bound_flag;
		str_val = par_str_val;
		char_pos = par_char_pos;
	}

	/**
	 * Whether the value is bound.
	 *
	 * @return {@code true} if the value is bound.
	 */
	public boolean is_bound() {
		return bound_flag;
	}

	/**
	 * Whether the value is a actual value.
	 *
	 * @return {@code true} if the value is a actual value.
	 */
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
	 * Assigns the other value to this value.
	 * Overwriting the current content in the process.
	 *<p>
	 * operator= in the core.
	 *
	 * @param otherValue
	 *                the other value to assign.
	 * @return the new value object.
	 */
	public TitanCharString_Element operator_assign(final String otherValue) {
		if (otherValue == null || otherValue.length() != 1) {
			throw new TtcnError("Assignment of a charstring value with length other than 1 to a charstring element.");
		}

		bound_flag = true;
		str_val.get_value().setCharAt(char_pos, otherValue.charAt(0));

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
	public TitanCharString_Element operator_assign(final TitanCharString_Element otherValue) {
		otherValue.must_bound("Assignment of an unbound charstring element.");

		bound_flag = true;
		str_val.get_value().setCharAt(char_pos, otherValue.str_val.get_value().charAt(otherValue.char_pos));

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
	public TitanCharString_Element operator_assign(final TitanCharString otherValue) {
		otherValue.must_bound("Assignment of unbound charstring value.");

		if (otherValue.get_value().length() != 1) {
			throw new TtcnError("Assignment of a charstring value with length other than 1 to a charstring element.");
		}

		bound_flag = true;
		str_val.get_value().setCharAt(char_pos, otherValue.get_value().charAt(0));

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
	public boolean operator_equals(final String otherValue) {
		must_bound("Comparison of an unbound charstring element.");

		if (otherValue == null || otherValue.length() != 1) {
			return false;
		} else {
			return str_val.get_at(char_pos).get_char() == otherValue.charAt(0);
		}
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
	public boolean operator_equals(final TitanCharString otherValue) {
		must_bound("Unbound left operand of charstring element comparison.");
		otherValue.must_bound("Unbound right operand of charstring element comparison.");

		if (otherValue.get_value().length() != 1) {
			return false;
		}

		return get_char() == otherValue.get_value().charAt(0);
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
	public boolean operator_equals(final TitanCharString_Element otherValue) {
		must_bound("Unbound left operand of charstring element comparison.");
		otherValue.must_bound("Unbound right operand of charstring element comparison.");

		return get_char() == otherValue.str_val.get_value().charAt(otherValue.char_pos);
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
	public boolean operator_equals(final TitanUniversalCharString otherValue) {
		must_bound("The left operand of comparison is an unbound charstring element.");
		otherValue.must_bound("The right operand of comparison is an unbound universal charstring value.");

		if (otherValue.val_ptr.size() != 1) {
			return false;
		} else if (otherValue.charstring) {
			return str_val.get_at(char_pos).get_char() == otherValue.char_at(0).getUc_cell();
		} else {
			final TitanUniversalChar temp = otherValue.char_at(0);
			return temp.getUc_group() == 0 && temp.getUc_plane() == 0 && temp.getUc_row() == 0
					&& str_val.get_at(char_pos).get_char() == temp.getUc_cell();
		}
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
	public boolean operator_equals(final TitanUniversalCharString_Element otherValue) {
		must_bound("The left operand of comparison is an unbound charstring element.");
		otherValue.must_bound("The right operand of comparison is an unbound universal charstring element.");

		final TitanUniversalChar temp = otherValue.get_char();
		return temp.getUc_group() == 0 && temp.getUc_plane() == 0 && temp.getUc_row() == 0
				&& str_val.get_at(char_pos).get_char() == temp.getUc_cell();
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
	public boolean operator_not_equals(final String otherValue) {
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
	public boolean operator_not_equals(final TitanUniversalCharString otherValue) {
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
	public boolean operator_not_equals(final TitanUniversalCharString_Element otherValue) {
		return !operator_equals(otherValue);
	}

	public char get_char() {
		return str_val.get_value().charAt(char_pos);
	}

	/**
	 * Concatenates the current charstring with the charstring received as a
	 * parameter.
	 *
	 * operator+ in the core.
	 *
	 * @param other_value
	 *                the other value to concatenate with.
	 * @return the new charstring representing the concatenated value.
	 * */
	public TitanCharString operator_concatenate(final String other_value) {
		must_bound("Unbound operand of charstring element concatenation.");

		if (other_value != null) {
			final int otherLen = other_value.length();
			final StringBuilder ret_val = new StringBuilder(otherLen + 1);
			ret_val.append(str_val.constGet_at(char_pos).get_char());
			ret_val.append(other_value);

			return new TitanCharString(ret_val);
		} else {
			final StringBuilder ret_val = new StringBuilder();
			ret_val.append(str_val.constGet_at(char_pos).get_char());

			return new TitanCharString(ret_val);
		}
	}

	/**
	 * Concatenates the current charstring with the charstring received as a
	 * parameter.
	 *
	 * operator+ in the core.
	 *
	 * @param other_value
	 *                the other value to concatenate with.
	 * @return the new charstring representing the concatenated value.
	 * */
	public TitanCharString operator_concatenate(final TitanCharString other_value) {
		must_bound("Unbound operand of charstring element concatenation.");
		other_value.must_bound("Unbound operand of charstring concatenation.");

		final int nChars = other_value.lengthof().get_int();
		final StringBuilder ret_val = new StringBuilder(nChars + 1);
		ret_val.append(str_val.constGet_at(char_pos).get_char());
		ret_val.append(other_value.get_value());

		return new TitanCharString(ret_val);
	}

	/**
	 * Concatenates the current charstring with the charstring received as a
	 * parameter.
	 *
	 * operator+ in the core.
	 *
	 * @param other_value
	 *                the other value to concatenate with.
	 * @return the new charstring representing the concatenated value.
	 * */
	public TitanCharString operator_concatenate(final TitanCharString_Element other_value) {
		must_bound("Unbound operand of charstring element concatenation.");
		other_value.must_bound("Unbound operand of charstring element concatenation.");

		final StringBuilder ret_val = new StringBuilder(2);
		ret_val.append(str_val.constGet_at(char_pos).get_char());
		ret_val.append(other_value.get_char());

		return new TitanCharString(ret_val);
	}

	/**
	 * Concatenates the current charstring with the universal charstring
	 * received as a parameter.
	 *
	 * operator+ in the core.
	 *
	 * @param other_value
	 *                the other value to concatenate with.
	 * @return the new universal charstring representing the concatenated
	 *         value.
	 * */
	public TitanUniversalCharString operator_concatenate(final TitanUniversalCharString other_value) {
		must_bound("The left operand of concatenation is an unbound charstring value.");
		other_value.must_bound("The right operand of concatenation is an unbound universal charstring value.");

		if (other_value.charstring) {
			final TitanUniversalCharString temp = new TitanUniversalCharString((char)0, (char)0, (char)0, get_char());

			return temp.operator_concatenate(other_value);
		} else {
			final List<TitanUniversalChar> ret_val = new ArrayList<TitanUniversalChar>();
			ret_val.add(new TitanUniversalChar((char) 0, (char) 0, (char) 0, get_char()));
			for (int i = 0; i < other_value.lengthof().get_int(); i++) {
				ret_val.add(other_value.char_at(i));
			}

			return new TitanUniversalCharString(ret_val);
		}
	}

	/**
	 * Concatenates the current charstring with the universal charstring received as a
	 * parameter.
	 *
	 * operator+ in the core.
	 *
	 * @param other_value
	 *                the other value to concatenate with.
	 * @return the new universal charstring representing the concatenated value.
	 * */
	public TitanUniversalCharString operator_concatenate(final TitanUniversalCharString_Element other_value) {
		must_bound("The left operand of concatenation is an unbound charstring element.");
		other_value.must_bound("The right operand of concatenation is an unbound universal charstring element.");

		TitanUniversalChar[] result = new TitanUniversalChar[2];
		result[0] = new TitanUniversalChar((char) 0, (char) 0, (char) 0, get_char());
		result[1] = other_value.get_char();
		return new TitanUniversalCharString(result);
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

		final StringBuilder builder = new StringBuilder();
		builder.append('"').append(str_val.get_value().charAt(char_pos)).append('"');

		return builder.toString();
	}

	/**
	 * Logs this value.
	 */
	public void log() {
		if (bound_flag) {
			final char c = str_val.get_at(char_pos).get_char();
			if (TTCN_Logger.is_printable(c)) {
				TTCN_Logger.log_char('"');
				TTCN_Logger.log_char_escaped(c);
				TTCN_Logger.log_char('"');
			} else {
				TTCN_Logger.log_event("char(0, 0, 0, {0})", (int) c);
			}
		} else {
			TTCN_Logger.log_event_unbound();
		}
	}
}
