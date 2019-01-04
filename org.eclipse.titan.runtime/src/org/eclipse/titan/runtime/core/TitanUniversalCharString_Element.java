/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

import java.text.MessageFormat;
import java.util.ArrayList;

/**
 *
 * @author Arpad Lovassy
 * @author Farkas Izabella Ingrid
 */
public class TitanUniversalCharString_Element {
	private boolean bound_flag;
	private final TitanUniversalCharString str_val;
	private final int char_pos;

	public TitanUniversalCharString_Element(final boolean par_bound_flag, final TitanUniversalCharString par_str_val, final int par_char_pos) {
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

	public boolean is_present() {
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
	public TitanUniversalCharString_Element operator_assign(final TitanUniversalCharString_Element otherValue) {
		otherValue.must_bound("Assignment of an unbound universal charstring element.");

		if (otherValue != this) {
			bound_flag = true;
			if (str_val.charstring) {
				if (otherValue.str_val.charstring) {
					str_val.cstr.setCharAt(char_pos, otherValue.get_char().getUc_cell());
				} else {
					str_val.convert_cstr_to_uni();
					str_val.val_ptr.set(char_pos, otherValue.get_char());
				}
			} else {
				if (otherValue.str_val.charstring) {
					str_val.val_ptr.set(char_pos, new TitanUniversalChar((char) 0, (char) 0, (char) 0, otherValue.str_val.cstr.charAt(otherValue.char_pos)));
				} else {
					str_val.val_ptr.set(char_pos, otherValue.get_char());
				}
			}
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
	public TitanUniversalCharString_Element operator_assign(final TitanUniversalCharString otherValue) {
		otherValue.must_bound("Assignment of an unbound universal charstring value to a universal charstring element.");

		final int length = otherValue.charstring ? otherValue.cstr.length() : otherValue.val_ptr.size();
		if (length != 1) {
			throw new TtcnError("Assignment of a universal charstring value with length other than 1 to a universal charstring element.");
		}

		bound_flag = true;

		operator_assign(otherValue.constGet_at(0));

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
	public TitanUniversalCharString_Element operator_assign(final TitanUniversalChar otherValue) {
		bound_flag = true;

		if (str_val.charstring) {
			if (otherValue.is_char()) {
				str_val.cstr.setCharAt(char_pos, otherValue.getUc_cell());
				return this;
			} else {
				str_val.convert_cstr_to_uni();
			}
		}
		str_val.val_ptr.set(char_pos, otherValue);

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
	public TitanUniversalCharString_Element operator_assign(final String otherValue) {
		if (otherValue == null || otherValue.length() != 1) {
			throw new TtcnError("Assignment of a charstring value with length other than 1 to a universal charstring element.");
		}

		bound_flag = true;
		if (str_val.charstring) {
			str_val.cstr.setCharAt(char_pos, otherValue.charAt(0));
		} else {
			str_val.val_ptr.set(char_pos, new TitanUniversalChar((char) 0, (char) 0, (char) 0, otherValue.charAt(0)));
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
	public TitanUniversalCharString_Element operator_assign(final TitanCharString otherValue) {
		otherValue.must_bound("Assignment of an unbound charstring value to a universal charstring element.");

		if (otherValue.lengthof().get_int() != 1) {
			throw new TtcnError("Assignment of a charstring value with length other than 1 to a universal charstring element.");
		}
		bound_flag = true;
		if (str_val.charstring) {
			str_val.cstr.setCharAt(char_pos, otherValue.get_at(0).get_char());
		} else {
			str_val.val_ptr.set(char_pos, new TitanUniversalChar((char) 0, (char) 0, (char) 0, otherValue.get_at(0).get_char()));
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
	public TitanUniversalCharString_Element operator_assign(final TitanCharString_Element otherValue) {
		otherValue.must_bound("Assignment of an unbound charstring element to a universal charstring element.");

		bound_flag = true;
		if (str_val.charstring) {
			str_val.cstr.setCharAt(char_pos, otherValue.get_char());
		} else {
			str_val.val_ptr.set(char_pos, new TitanUniversalChar((char) 0, (char) 0, (char) 0, otherValue.get_char()));
		}

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
	public boolean operator_equals(final TitanUniversalCharString_Element otherValue) {
		must_bound("Unbound left operand of charstring element comparison.");
		otherValue.must_bound("Unbound right operand of charstring comparison.");

		return get_char().operator_equals(otherValue.get_char());
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
		must_bound("Unbound left operand of charstring element comparison.");
		otherValue.must_bound("Unbound right operand of charstring element comparison.");

		if (otherValue.get_value().size() != 1) {
			return false;
		}

		return get_char().operator_equals(otherValue.char_at(0));
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
	public boolean operator_equals(final TitanUniversalChar otherValue) {
		must_bound("The left operand of comparison is an unbound universal charstring element.");

		if (str_val.charstring && otherValue.is_char()) {
			return str_val.cstr.charAt(char_pos) == otherValue.getUc_cell();
		}
		if (str_val.charstring && !otherValue.is_char()) {
			return false;
		}
		if (!str_val.charstring && otherValue.is_char()) {
			final TitanUniversalChar uchar = new TitanUniversalChar(str_val.char_at(char_pos));
			return uchar.getUc_group() == 0 && uchar.getUc_plane() == 0 && uchar.getUc_row() == 0
					&& uchar.getUc_cell() == otherValue.getUc_cell();
		}

		return str_val.val_ptr.get(char_pos).operator_equals(otherValue);
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
		must_bound("The left operand of comparison is an unbound universal charstring element.");

		if (otherValue == null || otherValue.length() != 1) {
			return false;
		}
		if (str_val.charstring) {
			return str_val.cstr.charAt(char_pos) == otherValue.charAt(0);
		}
		final TitanUniversalChar uc = str_val.val_ptr.get(char_pos);
		return uc.getUc_group() == 0 && uc.getUc_plane() == 0 && uc.getUc_row() == 0
				&& uc.getUc_cell() == otherValue.charAt(0);
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
		must_bound("The left operand of comparison is an unbound universal charstring element.");
		otherValue.must_bound("The right operand of comparison is an unbound charstring value.");

		if (otherValue.lengthof().get_int() != 1) {
			return false;
		}
		if (str_val.charstring) {
			return str_val.cstr.charAt(char_pos) == otherValue.get_at(0).get_char();
		}

		final TitanUniversalChar uchar = str_val.val_ptr.get(char_pos);
		return uchar.getUc_group() == 0 && uchar.getUc_plane() == 0 && uchar.getUc_row() == 0
				&& uchar.getUc_cell() == otherValue.get_at(0).get_char();
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
		must_bound("The left operand of comparison is an unbound universal charstring element.");
		otherValue.must_bound("The right operand of comparison is an unbound charstring element.");

		if (str_val.charstring) {
			return str_val.cstr.charAt(char_pos) == otherValue.get_char();
		}
		final TitanUniversalChar uchar = str_val.val_ptr.get(char_pos);

		return uchar.getUc_group() == 0 && uchar.getUc_plane() == 0 && uchar.getUc_row() == 0
				&& uchar.getUc_cell() == otherValue.get_char();
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
	public boolean operator_not_equals(final TitanUniversalChar otherValue) {
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
	public boolean operator_not_equals(final TitanCharString otherValue) {
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
	public boolean operator_not_equals(final TitanCharString_Element otherValue) {
		return !operator_equals(otherValue);
	}

	/**
	 * Concatenates the current universal charstring with the universal
	 * charstring received as a parameter.
	 *
	 * operator+ in the core.
	 *
	 * @param other_value
	 *                the other value to concatenate with.
	 * @return the new universal charstring representing the concatenated
	 *         value.
	 * */
	public TitanUniversalCharString operator_concatenate(final TitanUniversalChar other_value) {
		must_bound("The left operand of concatenation is an unbound universal charstring element.");

		if (str_val.charstring && other_value.is_char()) {
			final TitanUniversalCharString result = new TitanUniversalCharString();
			result.cstr = new StringBuilder();
			result.cstr.append(str_val.cstr.charAt(char_pos));
			result.cstr.append(other_value.getUc_cell());
			result.charstring = true;
			return result;
		} else {
			if (str_val.charstring ^ other_value.is_char()) { // xor
				final TitanUniversalCharString result = new TitanUniversalCharString();
				result.val_ptr = new ArrayList<TitanUniversalChar>(2);
				result.charstring = false;
				if (str_val.charstring) {
					result.val_ptr.add(new TitanUniversalChar((char) 0, (char) 0, (char) 0, str_val.cstr.charAt(char_pos)));
				} else {
					result.val_ptr.add(str_val.val_ptr.get(char_pos));
				}
				result.val_ptr.add(other_value);
				return result;
			}
		}

		final TitanUniversalCharString result = new TitanUniversalCharString();
		result.val_ptr = new ArrayList<TitanUniversalChar>(2);
		result.val_ptr.add(str_val.val_ptr.get(char_pos));
		result.val_ptr.add(other_value);
		result.charstring = false;
		return result;
	}

	/**
	 * Concatenates the current universal charstring with the charstring
	 * received as a parameter.
	 *
	 * operator+ in the core.
	 *
	 * @param other_value
	 *                the other value to concatenate with.
	 * @return the new universal charstring representing the concatenated
	 *         value.
	 * */
	public TitanUniversalCharString operator_concatenate(final String other_value) {
		must_bound("The left operand of concatenation is an unbound universal charstring element.");

		if (other_value == null) {
			return new TitanUniversalCharString(str_val.char_at(char_pos));
		}

		final TitanUniversalCharString result = new TitanUniversalCharString();
		if (str_val.charstring) {
			result.cstr = new StringBuilder();
			result.cstr.append(str_val.cstr.charAt(char_pos));
			result.cstr.append(other_value);
			result.charstring = true;
		} else {
			result.val_ptr = new ArrayList<TitanUniversalChar>();
			result.charstring = false;
			result.val_ptr.add(this.get_char());
			for (int i = 0; i < other_value.length(); ++i) {
				result.val_ptr.add(new TitanUniversalChar((char) 0, (char) 0, (char) 0, other_value.charAt(i)));
			}
		}
		return result;
	}

	/**
	 * Concatenates the current universal charstring with the charstring
	 * received as a parameter.
	 *
	 * operator+ in the core.
	 *
	 * @param other_value
	 *                the other value to concatenate with.
	 * @return the new universal charstring representing the concatenated
	 *         value.
	 * */
	public TitanUniversalCharString operator_concatenate(final TitanCharString other_value) {
		must_bound("The left operand of concatenation is an unbound universal charstring element.");
		other_value.must_bound("The right operand of concatenation is an unbound charstring value.");

		final TitanUniversalCharString result = new TitanUniversalCharString();

		if (str_val.charstring) {
			result.cstr = new StringBuilder();
			result.cstr.append(str_val.cstr.charAt(char_pos));
			for (int i = 0; i < other_value.lengthof().get_int(); ++i) {
				result.cstr.append(other_value.get_at(i).get_char());
			}
			result.charstring = true;
		} else {
			result.val_ptr = new ArrayList<TitanUniversalChar>();
			result.val_ptr.add(0, str_val.val_ptr.get(char_pos));
			for (int i = 0; i < other_value.lengthof().get_int(); ++i) {
				result.val_ptr.add(new TitanUniversalChar((char) 0, (char) 0, (char) 0, other_value.get_at(i).get_char()));
			}
			result.charstring = false;
		}

		return result;
	}

	/**
	 * Concatenates the current universal charstring with the charstring
	 * received as a parameter.
	 *
	 * operator+ in the core.
	 *
	 * @param other_value
	 *                the other value to concatenate with.
	 * @return the new universal charstring representing the concatenated
	 *         value.
	 * */
	public TitanUniversalCharString operator_concatenate(final TitanCharString_Element other_value) {
		must_bound("The left operand of concatenation is an unbound universal charstring element.");
		other_value.must_bound("The right operand of concatenation is an unbound charstring element.");

		final TitanUniversalCharString result = new TitanUniversalCharString();

		if (str_val.charstring) {
			result.cstr = new StringBuilder();
			result.charstring = true;
			result.cstr.append(str_val.cstr.charAt(char_pos));
			result.cstr.append(other_value.get_char());
		} else {
			result.val_ptr = new ArrayList<TitanUniversalChar>(2);
			result.charstring = false;
			result.val_ptr.add(str_val.val_ptr.get(char_pos));
			result.val_ptr.add(new TitanUniversalChar((char) 0, (char) 0, (char) 0, other_value.get_char()));
		}

		return result;
	}

	/**
	 * Concatenates the current universal charstring with the universal
	 * charstring received as a parameter.
	 *
	 * operator+ in the core.
	 *
	 * @param other_value
	 *                the other value to concatenate with.
	 * @return the new universal charstring representing the concatenated
	 *         value.
	 * */
	public TitanUniversalCharString operator_concatenate(final TitanUniversalCharString other_value) {
		must_bound("The left operand of concatenation is an unbound universal charstring element.");
		other_value.must_bound("The right operand of concatenation is an unbound universal charstring value.");

		final TitanUniversalCharString result = new TitanUniversalCharString();

		if (str_val.charstring) {
			if (other_value.charstring) {
				result.charstring = true;
				result.cstr = new StringBuilder();
				result.cstr.append(str_val.cstr.charAt(char_pos));
				result.cstr.append(other_value.cstr);
			} else {
				result.charstring = false;
				result.val_ptr = new ArrayList<TitanUniversalChar>();
				result.val_ptr.add(new TitanUniversalChar((char) 0, (char) 0, (char) 0, str_val.cstr.charAt(char_pos)));
				result.val_ptr.addAll(other_value.val_ptr);
			}
		} else {
			result.charstring = false;
			result.val_ptr = new ArrayList<TitanUniversalChar>();
			result.val_ptr.add(str_val.val_ptr.get(char_pos));
			if (other_value.charstring) {
				for (int i = 0; i < other_value.val_ptr.size(); ++i) {
					result.val_ptr.add(new TitanUniversalChar((char) 0, (char) 0, (char) 0, other_value.cstr.charAt(i)));
				}
			} else {
				result.val_ptr.addAll(other_value.val_ptr);
			}
		}

		return result;
	}

	/**
	 * Concatenates the current universal charstring with the universal
	 * charstring received as a parameter.
	 *
	 * operator+ in the core.
	 *
	 * @param other_value
	 *                the other value to concatenate with.
	 * @return the new universal charstring representing the concatenated
	 *         value.
	 * */
	public TitanUniversalCharString operator_concatenate(final TitanUniversalCharString_Element other_value) {
		must_bound("The left operand of concatenation is an unbound universal charstring element.");
		other_value.must_bound("The right operand of concatenation is an unbound universal charstring element.");

		final TitanUniversalCharString result = new TitanUniversalCharString();

		if (str_val.charstring) {
			if (other_value.str_val.charstring) {
				result.charstring = true;
				result.cstr = new StringBuilder();
				result.cstr.append(str_val.cstr.charAt(char_pos));
				result.cstr.append(other_value.str_val.cstr.charAt(other_value.char_pos));
			} else {
				result.charstring = false;
				result.val_ptr = new ArrayList<TitanUniversalChar>(2);
				result.val_ptr.add(new TitanUniversalChar((char) 0, (char) 0, (char) 0, str_val.cstr.charAt(char_pos)));
				result.val_ptr.add(other_value.str_val.val_ptr.get(other_value.char_pos));
			}
		} else {
			result.charstring = false;
			result.val_ptr = new ArrayList<TitanUniversalChar>(2);
			result.val_ptr.add(str_val.val_ptr.get(char_pos));
			if (other_value.str_val.charstring) {
				result.val_ptr.add(new TitanUniversalChar((char) 0, (char) 0, (char) 0, other_value.str_val.cstr.charAt(other_value.char_pos)));
			} else {
				result.val_ptr.add(other_value.str_val.val_ptr.get(other_value.char_pos));
			}
		}

		return result;
	}

	public TitanUniversalChar get_char() {
		return str_val.char_at(char_pos);
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
		return str_val.toString() + " index: " + char_pos;
	}

	// static functions
	/**
	 * Checks if the first value is equivalent to the second one.
	 *
	 * static operator== in the core
	 *
	 * @param ucharValue
	 *                the first value.
	 * @param otherValue
	 *                the other value to check against.
	 * @return {@code true} if the values are equivalent.
	 */
	public static boolean operator_equals(final TitanUniversalChar ucharValue, final TitanUniversalCharString_Element otherValue) {
		otherValue.must_bound("The right operand of comparison is an unbound universal charstring element.");

		return ucharValue.operator_equals(otherValue.get_char());
	}

	/**
	 * Logs this element.
	 */
	public void log() {
		if (bound_flag) {
			if (str_val.charstring) {
				TTCN_Logger.log_char_escaped(str_val.cstr.charAt(char_pos));
				return;
			}
			final TitanUniversalChar uchar = str_val.val_ptr.get(char_pos);
			if (TitanUniversalCharString.is_printable(uchar)) {
				TTCN_Logger.log_char('"');
				TTCN_Logger.log_char_escaped(uchar.getUc_cell());
				TTCN_Logger.log_char('"');
			} else {
				TTCN_Logger.log_event_str(MessageFormat.format("char({0}, {1}, {2}, {3})", (int) uchar.getUc_group(),
						(int) uchar.getUc_plane(), (int) uchar.getUc_row(), (int) uchar.getUc_cell()));
			}
		} else {
			TTCN_Logger.log_event_unbound();
		}
	}

	/**
	 * Checks if the first value is not equivalent to the second one.
	 *
	 * static operator!= in the core
	 *
	 * @param ucharValue
	 *                the first value.
	 * @param otherValue
	 *                the other value to check against.
	 * @return {@code true} if the values are equivalent.
	 */
	public static boolean operator_not_equals(final TitanUniversalChar ucharValue, final TitanUniversalCharString_Element otherValue) {
		return !operator_equals(ucharValue, otherValue);
	}

	/**
	 * Concatenates the first universal charstring with the second universal
	 * charstring received as a parameter.
	 *
	 * static operator+ in the core.
	 *
	 * @param ucharValue the first universal charstring to concatenate.
	 * @param other_value
	 *                the other value to concatenate with.
	 * @return the new universal charstring representing the concatenated
	 *         value.
	 * */
	public static TitanUniversalCharString operator_concatenate(final TitanUniversalChar ucharValue, final TitanUniversalCharString_Element other_value) {
		other_value.must_bound("The right operand of concatenation is an unbound universal charstring element.");

		final TitanUniversalCharString result = new TitanUniversalCharString();
		if (other_value.str_val.charstring) {
			if (ucharValue.is_char()) {
				result.cstr = new StringBuilder();
				result.cstr.append(ucharValue.getUc_cell());
				result.cstr.append(other_value.str_val.cstr.charAt(other_value.char_pos));
				result.charstring = true;
			} else {
				result.val_ptr = new ArrayList<TitanUniversalChar>(2);
				result.charstring = false;
				result.val_ptr.add(ucharValue);
				result.val_ptr.add(new TitanUniversalChar((char) 0, (char) 0, (char) 0, other_value.str_val.cstr.charAt(other_value.char_pos)));
			}
		} else {
			result.val_ptr = new ArrayList<TitanUniversalChar>(2);
			result.charstring = false;
			result.val_ptr.add(ucharValue);
			result.val_ptr.add(other_value.get_char());
		}

		return result;
	}

	/**
	 * Checks if the first value is equivalent to the second one.
	 *
	 * static operator== in the core
	 *
	 * @param otherValue
	 *                the first value.
	 * @param rightValue
	 *                the other value to check against.
	 * @return {@code true} if the values are equivalent.
	 */
	public static boolean operator_equals(final String otherValue, final TitanUniversalCharString_Element rightValue) {
		rightValue.must_bound("The right operand of comparison is an unbound universal charstring element.");

		if (otherValue == null || otherValue.length() != 1) {
			return false;
		}
		if (rightValue.str_val.charstring) {
			return rightValue.str_val.cstr.charAt(rightValue.char_pos) == otherValue.charAt(0);
		}
		final TitanUniversalChar uc = rightValue.str_val.val_ptr.get(rightValue.char_pos);
		return uc.getUc_group() == 0 && uc.getUc_plane() == 0 && uc.getUc_row() == 0
				&& uc.getUc_cell() == otherValue.charAt(0);
	}

	/**
	 * Concatenates the first universal charstring with the second universal
	 * charstring received as a parameter.
	 *
	 * static operator+ in the core.
	 *
	 * @param stringValue the first universal charstring to concatenate.
	 * @param other_value
	 *                the other value to concatenate with.
	 * @return the new universal charstring representing the concatenated
	 *         value.
	 * */
	public static TitanUniversalCharString operator_concatenate(final String stringValue, final TitanUniversalCharString_Element other_value) {
		other_value.must_bound("The right operand of concatenation is an unbound universal charstring element.");

		if (stringValue == null) {
			return new TitanUniversalCharString(other_value);
		}

		final TitanUniversalCharString result = new TitanUniversalCharString();
		if (other_value.str_val.charstring) {
			result.cstr = new StringBuilder();
			result.cstr.append(stringValue);
			result.cstr.append(other_value.str_val.cstr.charAt(other_value.char_pos));
			result.charstring = true;
		} else {
			result.val_ptr = new ArrayList<TitanUniversalChar>();
			result.charstring = false;
			for (int i = 0; i < stringValue.length(); ++i) {
				result.val_ptr.add(new TitanUniversalChar((char) 0, (char) 0, (char) 0, stringValue.charAt(i)));
			}
			result.val_ptr.add(other_value.get_char());
		}
		return result;
	}
}