/******************************************************************************
 * Copyright (c) 2000-2017 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
	private TitanUniversalCharString str_val;
	private int char_pos;

	public TitanUniversalCharString_Element( final boolean par_bound_flag, final TitanUniversalCharString par_str_val, final int par_char_pos ) {
		bound_flag = par_bound_flag;
		str_val = par_str_val;
		char_pos = par_char_pos;
	}

	public boolean isBound() {
		return bound_flag;
	}

	public boolean isPresent() {
		return bound_flag;
	}

	public boolean isValue() {
		return bound_flag;
	}

	public void mustBound( final String aErrorMessage ) {
		if ( !bound_flag ) {
			throw new TtcnError( aErrorMessage );
		}
	}

	public void cleanUp() {
		str_val = null;
	}

	// originally operator=
	public TitanUniversalCharString_Element assign(final TitanUniversalCharString_Element other_value) {
		other_value.mustBound("Assignment of an unbound universal charstring element.");

		if (other_value != this) {
			bound_flag = true;
			if (str_val.charstring) {
				if (other_value.str_val.charstring) {
					str_val.cstr.setCharAt(char_pos, other_value.get_char().getUc_cell());
				} else {
					str_val.convertCstrToUni();
					str_val.val_ptr.set(char_pos, other_value.get_char());
				}
			} else {
				if (other_value.str_val.charstring) {
					str_val.val_ptr.set(char_pos, new TitanUniversalChar((char) 0, (char) 0, (char) 0, other_value.str_val.cstr.charAt(other_value.char_pos)));
				} else {
					str_val.val_ptr.set(char_pos, other_value.get_char());
				}
			}
		}

		return this;
	}

	// originally operator=
	public TitanUniversalCharString_Element assign(final TitanUniversalCharString other_value) {
		other_value.mustBound("Assignment of an unbound universal charstring value to a universal charstring element.");

		final int length = other_value.charstring ? other_value.cstr.length() : other_value.val_ptr.size();
		if (length != 1) {
			throw new TtcnError("Assignment of a universal charstring value with length other than 1 to a universal charstring element.");
		}

		bound_flag = true;

		if (other_value.charstring) {
			str_val.cstr = other_value.cstr;
			str_val.charstring = true;
			str_val.val_ptr = null;
		} else {
			str_val.val_ptr = other_value.val_ptr;
			str_val.charstring = false;
			str_val.cstr = null;
		}
		char_pos = 0;
		return this;
	}

	// originally operator=
	public TitanUniversalCharString_Element assign(final TitanUniversalChar otherValue) {
		bound_flag = true;

		if (str_val.charstring) {
			if (otherValue.is_char()) {
				str_val.cstr.setCharAt(char_pos, otherValue.getUc_cell());
				return this;
			} else {
				str_val.convertCstrToUni();
			}
		}
		str_val.val_ptr.set(char_pos, otherValue);

		return this;
	}

	// originally operator=
	public TitanUniversalCharString_Element assign(final String otherValue) {
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

	public TitanUniversalCharString_Element assign(final TitanCharString otherValue) {
		otherValue.mustBound("Assignment of an unbound charstring value to a universal charstring element.");

		if (otherValue.lengthOf().getInt() != 1) {
			throw new TtcnError("Assignment of a charstring value with length other than 1 to a universal charstring element.");
		}
		bound_flag = true;
		if (str_val.charstring) {
			str_val.cstr.setCharAt(char_pos, otherValue.getAt(0).get_char());
		} else {
			str_val.val_ptr.set(char_pos, new TitanUniversalChar((char) 0, (char) 0, (char) 0, otherValue.getAt(0).get_char()));
		}

		return this;
	}

	// originally operator=
	public TitanUniversalCharString_Element assign(final TitanCharString_Element otherValue) {
		otherValue.mustBound("Assignment of an unbound charstring element to a universal charstring element.");

		bound_flag = true;
		if (str_val.charstring) {
			str_val.cstr.setCharAt(char_pos, otherValue.get_char());
		} else {
			str_val.val_ptr.set(char_pos, new TitanUniversalChar((char) 0, (char) 0, (char) 0, otherValue.get_char()));
		}

		return this;
	}

	// originally operator==
	public boolean operatorEquals(final TitanUniversalCharString_Element other_value) {
		mustBound("Unbound left operand of charstring element comparison.");
		other_value.mustBound("Unbound right operand of charstring comparison.");

		return get_char().operatorEquals(other_value.get_char());
	}

	// originally operator==
	public boolean operatorEquals(final TitanUniversalCharString other_value) {
		mustBound("Unbound left operand of charstring element comparison.");
		other_value.mustBound("Unbound right operand of charstring element comparison.");

		if (other_value.getValue().size() != 1) {
			return false;
		}

		return get_char().operatorEquals(other_value.charAt(0));
	}

	public boolean operatorEquals(final TitanUniversalChar otherValue) {
		mustBound("The left operand of comparison is an unbound universal charstring element.");

		if (str_val.charstring && otherValue.is_char()) {
			return str_val.cstr.charAt(char_pos) == otherValue.getUc_cell();
		}
		if (str_val.charstring && !otherValue.is_char()) {
			return false;
		}
		if (!str_val.charstring && otherValue.is_char()) {
			final TitanUniversalChar uchar = new TitanUniversalChar(str_val.charAt(char_pos));
			return uchar.getUc_group() == 0 && uchar.getUc_plane() == 0 && uchar.getUc_row() == 0
					&& uchar.getUc_cell() == otherValue.getUc_cell();
		}

		return str_val.val_ptr.get(char_pos).operatorEquals(otherValue);
	}

	public boolean operatorEquals(final String otherValue) {
		mustBound("The left operand of comparison is an unbound universal charstring element.");

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

	public boolean operatorEquals(final TitanCharString otherValue) {
		mustBound("The left operand of comparison is an unbound universal charstring element.");
		otherValue.mustBound("The right operand of comparison is an unbound charstring value.");

		if (otherValue.lengthOf().getInt() != 1) {
			return false;
		}
		if (str_val.charstring) {
			return str_val.cstr.charAt(char_pos) == otherValue.getAt(0).get_char();
		}

		final TitanUniversalChar uchar = str_val.val_ptr.get(char_pos);
		return uchar.getUc_group() == 0 && uchar.getUc_plane() == 0 && uchar.getUc_row() == 0
				&& uchar.getUc_cell() == otherValue.getAt(0).get_char();
	}

	public boolean operatorEquals(final TitanCharString_Element otherValue) {
		mustBound("The left operand of comparison is an unbound universal charstring element.");
		otherValue.mustBound("The right operand of comparison is an unbound charstring element.");

		if (str_val.charstring) {
			return str_val.cstr.charAt(char_pos) == otherValue.get_char();
		}
		final TitanUniversalChar uchar = str_val.val_ptr.get(char_pos);

		return uchar.getUc_group() == 0 && uchar.getUc_plane() == 0 && uchar.getUc_row() == 0
				&& uchar.getUc_cell() == otherValue.get_char();
	}

	// originally operator!=
	public boolean operatorNotEquals(final TitanUniversalCharString_Element otherValue) {
		return !operatorEquals(otherValue);
	}

	public boolean operatorNotEquals(final TitanUniversalCharString otherValue) {
		return !operatorEquals(otherValue);
	}

	public boolean operatorNotEquals(final TitanUniversalChar otherValue) {
		return !operatorEquals(otherValue);
	}

	public boolean operatorNotEquals(final String otherValue) {
		return !operatorEquals(otherValue);
	}

	public boolean operatorNotEquals(final TitanCharString otherValue) {
		return !operatorEquals(otherValue);
	}

	public boolean operatorNotEquals(final TitanCharString_Element otherValue) {
		return !operatorEquals(otherValue);
	}

	// originally operator+
	public TitanUniversalCharString concatenate(final TitanUniversalChar otherValue) {
		mustBound("The left operand of concatenation is an unbound universal charstring element.");

		if (str_val.charstring && otherValue.is_char()) {
			final TitanUniversalCharString result = new TitanUniversalCharString();
			result.cstr = new StringBuilder();
			result.cstr.append(str_val.cstr.charAt(char_pos));
			result.cstr.append(otherValue.getUc_cell());
			result.charstring = true;
			return result;
		} else {
			if (str_val.charstring ^ otherValue.is_char()) { // xor
				final TitanUniversalCharString result = new TitanUniversalCharString();
				result.val_ptr = new ArrayList<TitanUniversalChar>(2);
				result.charstring = false;
				if (str_val.charstring) {
					result.val_ptr.add(new TitanUniversalChar((char) 0, (char) 0, (char) 0, str_val.cstr.charAt(char_pos)));
				} else {
					result.val_ptr.add(str_val.val_ptr.get(char_pos));
				}
				result.val_ptr.add(otherValue);
				return result;
			}
		}

		final TitanUniversalCharString result = new TitanUniversalCharString();
		result.val_ptr = new ArrayList<TitanUniversalChar>(2);
		result.val_ptr.add(str_val.val_ptr.get(char_pos));
		result.val_ptr.add(otherValue);
		result.charstring = false;
		return result;
	}

	// originally operator+
	public TitanUniversalCharString concatenate(final String otherValue) {
		mustBound("The left operand of concatenation is an unbound universal charstring element.");

		if (otherValue == null) {
			return new TitanUniversalCharString(str_val.charAt(char_pos));
		}

		final TitanUniversalCharString result = new TitanUniversalCharString();
		if (str_val.charstring) {
			result.cstr = new StringBuilder();
			result.cstr.append(str_val.cstr.charAt(char_pos));
			result.cstr.append(otherValue);
			result.charstring = true;
		} else {
			result.val_ptr = new ArrayList<TitanUniversalChar>();
			result.charstring = false;
			result.val_ptr.add(this.get_char());
			for (int i = 0; i < otherValue.length(); ++i) {
				result.val_ptr.add(new TitanUniversalChar((char) 0, (char) 0, (char) 0, otherValue.charAt(i)));
			}
		}
		return result;
	}

	// originally operator+
	public TitanUniversalCharString concatenate(final TitanCharString otherValue) {
		mustBound("The left operand of concatenation is an unbound universal charstring element.");
		otherValue.mustBound("The right operand of concatenation is an unbound charstring value.");

		final TitanUniversalCharString result = new TitanUniversalCharString();

		if (str_val.charstring) {
			result.cstr = new StringBuilder();
			result.cstr.append(str_val.cstr.charAt(char_pos));
			for (int i = 0; i < otherValue.lengthOf().getInt(); ++i) {
				result.cstr.append(otherValue.getAt(i).get_char());
			}
			result.charstring = true;
		} else {
			result.val_ptr = new ArrayList<TitanUniversalChar>();
			result.val_ptr.add(0, str_val.val_ptr.get(char_pos));
			for (int i = 0; i < otherValue.lengthOf().getInt(); ++i) {
				result.val_ptr.add(new TitanUniversalChar((char) 0, (char) 0, (char) 0, otherValue.getAt(i).get_char()));
			}
			result.charstring = false;
		}

		return result;
	}

	// originally operator+
	public TitanUniversalCharString concatenate(final TitanCharString_Element otherValue) {
		mustBound("The left operand of concatenation is an unbound universal charstring element.");
		otherValue.mustBound("The right operand of concatenation is an unbound charstring element.");

		final TitanUniversalCharString result = new TitanUniversalCharString();

		if (str_val.charstring) {
			result.cstr = new StringBuilder();
			result.charstring = true;
			result.cstr.append(str_val.cstr.charAt(char_pos));
			result.cstr.append(otherValue.get_char());
		} else {
			result.val_ptr = new ArrayList<TitanUniversalChar>(2);
			result.charstring = false;
			result.val_ptr.add(str_val.val_ptr.get(char_pos));
			result.val_ptr.add(new TitanUniversalChar((char) 0, (char) 0, (char) 0, otherValue.get_char()));
		}

		return result;
	}

	// originally operator+
	public TitanUniversalCharString concatenate(final TitanUniversalCharString otherValue) {
		mustBound("The left operand of concatenation is an unbound universal charstring element.");
		otherValue.mustBound("The right operand of concatenation is an unbound universal charstring value.");

		final TitanUniversalCharString result = new TitanUniversalCharString();

		if (str_val.charstring) {
			if (otherValue.charstring) {
				result.charstring = true;
				result.cstr = new StringBuilder();
				result.cstr.append(str_val.cstr.charAt(char_pos));
				result.cstr.append(otherValue.cstr);
			} else {
				result.charstring = false;
				result.val_ptr = new ArrayList<TitanUniversalChar>();
				result.val_ptr.add(new TitanUniversalChar((char) 0, (char) 0, (char) 0, str_val.cstr.charAt(char_pos)));
				result.val_ptr.addAll(otherValue.val_ptr);
			}
		} else {
			result.charstring = false;
			result.val_ptr = new ArrayList<TitanUniversalChar>();
			result.val_ptr.add(str_val.val_ptr.get(char_pos));
			if (otherValue.charstring) {
				for (int i = 0; i < otherValue.val_ptr.size(); ++i) {
					result.val_ptr.add(new TitanUniversalChar((char) 0, (char) 0, (char) 0, otherValue.cstr.charAt(i)));
				}
			} else {
				result.val_ptr.addAll(otherValue.val_ptr);
			}
		}

		return result;
	}

	// originally operator+
	public TitanUniversalCharString concatenate(final TitanUniversalCharString_Element otherValue) {
		mustBound("The left operand of concatenation is an unbound universal charstring element.");
		otherValue.mustBound("The right operand of concatenation is an unbound universal charstring element.");

		final TitanUniversalCharString result = new TitanUniversalCharString();

		if (str_val.charstring) {
			if (otherValue.str_val.charstring) {
				result.charstring = true;
				result.cstr = new StringBuilder();
				result.cstr.append(str_val.cstr.charAt(char_pos));
				result.cstr.append(otherValue.str_val.cstr.charAt(otherValue.char_pos));
			} else {
				result.charstring = false;
				result.val_ptr = new ArrayList<TitanUniversalChar>(2);
				result.val_ptr.add(new TitanUniversalChar((char) 0, (char) 0, (char) 0, str_val.cstr.charAt(char_pos)));
				result.val_ptr.add(otherValue.str_val.val_ptr.get(otherValue.char_pos));
			}
		} else {
			result.charstring = false;
			result.val_ptr = new ArrayList<TitanUniversalChar>(2);
			result.val_ptr.add(str_val.val_ptr.get(char_pos));
			if (otherValue.str_val.charstring) {
				result.val_ptr.add(new TitanUniversalChar((char) 0, (char) 0, (char) 0, otherValue.str_val.cstr.charAt(otherValue.char_pos)));
			} else {
				result.val_ptr.add(otherValue.str_val.val_ptr.get(otherValue.char_pos));
			}
		}

		return result;
	}

	public TitanUniversalChar get_char() {
		return str_val.charAt(char_pos);
	}

	@Override
	public String toString() {
		return str_val.toString() + " index: " + char_pos;
	}

	// static functions

	public static boolean operatorEquals(final TitanUniversalChar ucharValue, final TitanUniversalCharString_Element otherValue) {
		otherValue.mustBound("The right operand of comparison is an unbound universal charstring element.");

		return ucharValue.operatorEquals(otherValue.get_char());
	}

	public void log() {
		if (bound_flag) {
			if (str_val.charstring) {
				TtcnLogger.logCharEscaped((str_val.cstr.charAt(char_pos)));
				return;
			}
			final TitanUniversalChar uchar = str_val.val_ptr.get(char_pos);
			if (TitanUniversalCharString.isPrintable(uchar)) {
				TtcnLogger.log_char('"');
				TtcnLogger.logCharEscaped(uchar.getUc_cell());
				TtcnLogger.log_char('"');
			} else {
				TtcnLogger.log_event_str(MessageFormat.format("char({0}, {1}, {2}, {3})", (int) uchar.getUc_group(),
						(int) uchar.getUc_plane(), (int) uchar.getUc_row(), (int) uchar.getUc_cell()));
			}
		} else {
			TtcnLogger.log_event_unbound();
		}
	}

	public static boolean operatorNotEquals(final TitanUniversalChar ucharValue, final TitanUniversalCharString_Element otherValue) {
		return !operatorEquals(ucharValue, otherValue);
	}

	public static TitanUniversalCharString concatenate(final TitanUniversalChar ucharValue, final TitanUniversalCharString_Element otherValue) {
		otherValue.mustBound("The right operand of concatenation is an unbound universal charstring element.");

		final TitanUniversalCharString result = new TitanUniversalCharString();
		if (otherValue.str_val.charstring) {
			if (ucharValue.is_char()) {
				result.cstr = new StringBuilder();
				result.cstr.append(ucharValue.getUc_cell());
				result.cstr.append(otherValue.str_val.cstr.charAt(otherValue.char_pos));
				result.charstring = true;
			} else {
				result.val_ptr = new ArrayList<TitanUniversalChar>(2);
				result.charstring = false;
				result.val_ptr.add(ucharValue);
				result.val_ptr.add(new TitanUniversalChar((char) 0, (char) 0, (char) 0, otherValue.str_val.cstr.charAt(otherValue.char_pos)));
			}
		} else {
			result.val_ptr = new ArrayList<TitanUniversalChar>(2);
			result.charstring = false;
			result.val_ptr.add(ucharValue);
			result.val_ptr.add(otherValue.get_char());
		}

		return result;
	}

	public static boolean operatorEquals(final String otherValue, final TitanUniversalCharString_Element rightValue) {
		rightValue.mustBound("The right operand of comparison is an unbound universal charstring element.");

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

	public static TitanUniversalCharString concatenate(final String otherValue, final TitanUniversalCharString_Element rightValue) {
		rightValue.mustBound("The right operand of concatenation is an unbound universal charstring element.");

		if (otherValue == null) {
			return new TitanUniversalCharString(rightValue);
		}

		final TitanUniversalCharString result = new TitanUniversalCharString();
		if (rightValue.str_val.charstring) {
			result.cstr = new StringBuilder();
			result.cstr.append(otherValue);
			result.cstr.append(rightValue.str_val.cstr.charAt(rightValue.char_pos));
			result.charstring = true;
		} else {
			result.val_ptr = new ArrayList<TitanUniversalChar>();
			result.charstring = false;
			for (int i = 0; i < otherValue.length(); ++i) {
				result.val_ptr.add(new TitanUniversalChar((char) 0, (char) 0, (char) 0, otherValue.charAt(i)));
			}
			result.val_ptr.add(rightValue.get_char());
		}
		return result;
	}
}