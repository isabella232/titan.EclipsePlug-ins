/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
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

	public boolean isBound() {
		return bound_flag;
	}

	public boolean isValue() {
		return bound_flag;
	}

	public void mustBound(final String aErrorMessage) {
		if (!bound_flag) {
			throw new TtcnError(aErrorMessage);
		}
	}

	// assign for String
	public TitanCharString_Element assign(final String aOtherValue) {
		if (aOtherValue == null || aOtherValue.length() != 1) {
			throw new TtcnError("Assignment of a charstring value with length other than 1 to a charstring element.");
		}

		bound_flag = true;
		str_val.getValue().setCharAt(char_pos, aOtherValue.charAt(0));

		return this;
	}

	// originally operator=
	public TitanCharString_Element assign(final TitanCharString_Element other_value) {
		other_value.mustBound("Assignment of an unbound charstring element.");

		bound_flag = true;
		str_val.getValue().setCharAt(char_pos, other_value.str_val.getValue().charAt(other_value.char_pos));

		return this;
	}

	// originally operator=
	public TitanCharString_Element assign(final TitanCharString other_value) {
		other_value.mustBound("Assignment of unbound charstring value.");

		if (other_value.getValue().length() != 1) {
			throw new TtcnError("Assignment of a charstring value with length other than 1 to a charstring element.");
		}

		bound_flag = true;
		str_val.getValue().setCharAt(char_pos, other_value.getValue().charAt(0));

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
	public boolean operatorEquals(final String otherValue) {
		mustBound("Comparison of an unbound charstring element.");

		if (otherValue == null || otherValue.length() != 1) {
			return false;
		} else {
			return str_val.getAt(char_pos).get_char() == otherValue.charAt(0);
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
	public boolean operatorEquals(final TitanCharString otherValue) {
		mustBound("Unbound left operand of charstring element comparison.");
		otherValue.mustBound("Unbound right operand of charstring element comparison.");

		if (otherValue.getValue().length() != 1) {
			return false;
		}

		return get_char() == otherValue.getValue().charAt(0);
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
	public boolean operatorEquals(final TitanCharString_Element otherValue) {
		mustBound("Unbound left operand of charstring element comparison.");
		otherValue.mustBound("Unbound right operand of charstring element comparison.");

		return get_char() == otherValue.str_val.getValue().charAt(otherValue.char_pos);
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
	public boolean operatorEquals(final TitanUniversalCharString otherValue) {
		mustBound("The left operand of comparison is an unbound charstring element.");
		otherValue.mustBound("The right operand of comparison is an unbound universal charstring value.");

		if (otherValue.val_ptr.size() != 1) {
			return false;
		} else if (otherValue.charstring) {
			return str_val.getAt(char_pos).get_char() == otherValue.charAt(0).getUc_cell();
		} else {
			final TitanUniversalChar temp = otherValue.charAt(0);
			return temp.getUc_group() == 0 && temp.getUc_plane() == 0 && temp.getUc_row() == 0
					&& str_val.getAt(char_pos).get_char() == temp.getUc_cell();
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
	public boolean operatorEquals(final TitanUniversalCharString_Element otherValue) {
		mustBound("The left operand of comparison is an unbound charstring element.");
		otherValue.mustBound("The right operand of comparison is an unbound universal charstring element.");

		final TitanUniversalChar temp = otherValue.get_char();
		return temp.getUc_group() == 0 && temp.getUc_plane() == 0 && temp.getUc_row() == 0
				&& str_val.getAt(char_pos).get_char() == temp.getUc_cell();
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
	public boolean operatorNotEquals(final String otherValue) {
		return !operatorEquals(otherValue);
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
	public boolean operatorNotEquals(final TitanUniversalCharString otherValue) {
		return !operatorEquals(otherValue);
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
	public boolean operatorNotEquals(final TitanUniversalCharString_Element otherValue) {
		return !operatorEquals(otherValue);
	}

	public char get_char() {
		return str_val.getValue().charAt(char_pos);
	}

	// originally operator +
	public TitanCharString concatenate(final String aOtherValue) {
		mustBound("Unbound operand of charstring element concatenation.");

		if (aOtherValue != null) {
			final int otherLen = aOtherValue.length();
			final StringBuilder ret_val = new StringBuilder(otherLen + 1);
			ret_val.append(str_val.constGetAt(char_pos).get_char());
			ret_val.append(aOtherValue);

			return new TitanCharString(ret_val);
		} else {
			final StringBuilder ret_val = new StringBuilder();
			ret_val.append(str_val.constGetAt(char_pos).get_char());

			return new TitanCharString(ret_val);
		}
	}

	// originally operator +
	public TitanCharString concatenate(final TitanCharString aOtherValue) {
		mustBound("Unbound operand of charstring element concatenation.");
		aOtherValue.mustBound("Unbound operand of charstring concatenation.");

		final int nChars = aOtherValue.lengthOf().getInt();
		final StringBuilder ret_val = new StringBuilder(nChars + 1);
		ret_val.append(str_val.constGetAt(char_pos).get_char());
		ret_val.append(aOtherValue.getValue());

		return new TitanCharString(ret_val);
	}

	// originally operator +
	public TitanCharString concatenate(final TitanCharString_Element aOtherValue) {
		mustBound("Unbound operand of charstring element concatenation.");
		aOtherValue.mustBound("Unbound operand of charstring element concatenation.");

		final StringBuilder ret_val = new StringBuilder(2);
		ret_val.append(str_val.constGetAt(char_pos).get_char());
		ret_val.append(aOtherValue.get_char());

		return new TitanCharString(ret_val);
	}

	// originally operator +
	public TitanUniversalCharString concatenate(final TitanUniversalCharString aOtherValue) {
		mustBound("The left operand of concatenation is an unbound charstring value.");
		aOtherValue.mustBound("The right operand of concatenation is an unbound universal charstring value.");

		if (aOtherValue.charstring) {
			final TitanUniversalCharString temp = new TitanUniversalCharString((char)0, (char)0, (char)0, get_char());

			return temp.concatenate(aOtherValue);
		} else {
			final List<TitanUniversalChar> ret_val = new ArrayList<TitanUniversalChar>();
			ret_val.add(new TitanUniversalChar((char) 0, (char) 0, (char) 0, get_char()));
			for (int i = 0; i < aOtherValue.lengthOf().getInt(); i++) {
				ret_val.add(aOtherValue.charAt(i));
			}

			return new TitanUniversalCharString(ret_val);
		}
	}

	// originally operator +
	public TitanUniversalCharString concatenate(final TitanUniversalCharString_Element aOtherValue) {
		mustBound("The left operand of concatenation is an unbound charstring element.");
		aOtherValue.mustBound("The right operand of concatenation is an unbound universal charstring element.");

		TitanUniversalChar[] result = new TitanUniversalChar[2];
		result[0] = new TitanUniversalChar((char) 0, (char) 0, (char) 0, get_char());
		result[1] = aOtherValue.get_char();
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
		builder.append('"').append(str_val.getValue().charAt(char_pos)).append('"');

		return builder.toString();
	}

	public void log() {
		if (bound_flag) {
			final char c = str_val.getAt(char_pos).get_char();
			if (TTCN_Logger.isPrintable(c)) {
				TTCN_Logger.log_char('"');
				TTCN_Logger.logCharEscaped(c);
				TTCN_Logger.log_char('"');
			} else {
				TTCN_Logger.log_event("char(0, 0, 0, {0})", (int) c);
			}
		} else {
			TTCN_Logger.log_event_unbound();
		}
	}
}
