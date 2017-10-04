/******************************************************************************
 * Copyright (c) 2000-2017 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
	private TitanCharString str_val;
	private int char_pos;

	public TitanCharString_Element( final boolean par_bound_flag, final TitanCharString par_str_val, final int par_char_pos ) {
		bound_flag = par_bound_flag;
		str_val = par_str_val;
		char_pos = par_char_pos;
	}

	public TitanBoolean isBound() {
		return new TitanBoolean(bound_flag);
	}

	public TitanBoolean isValue() {
		return new TitanBoolean(bound_flag);
	}

	public void mustBound( final String aErrorMessage ) {
		if ( !bound_flag ) {
			throw new TtcnError( aErrorMessage );
		}
	}

	// assign for String
	public TitanCharString_Element assign(final String aOtherValue) {
		if (aOtherValue == null || aOtherValue.length() != 1) {
			throw new TtcnError("Assignment of a charstring value with length other than 1 to a charstring element.");
		}

		bound_flag = true;
		str_val = new TitanCharString(aOtherValue);
		char_pos = 0;

		return this;
	}

	//originally operator=
	public TitanCharString_Element assign( final TitanCharString_Element other_value ) {
		other_value.mustBound("Assignment of an unbound charstring element.");

		bound_flag = true;
		str_val.getValue().setCharAt( char_pos, other_value.str_val.getValue().charAt( other_value.char_pos ) );

		return this;
	}

	//originally operator=
	public TitanCharString_Element assign( final TitanCharString other_value ) {
		other_value.mustBound("Assignment of unbound charstring value.");

		if (other_value.getValue().length() != 1) {
			throw new TtcnError( "Assignment of a charstring value with length other than 1 to a charstring element." );
		}

		bound_flag = true;
		str_val.getValue().setCharAt( char_pos, other_value.getValue().charAt(0) );

		return this;
	}

	// originally operator==
	// operatorEquals for String
	public TitanBoolean operatorEquals(final String aOtherValue) {
		mustBound("Comparison of an unbound charstring element.");

		if (aOtherValue == null || aOtherValue.length() != 1) {
			return new TitanBoolean(false);
		} else {
			return new TitanBoolean(str_val.getAt(char_pos).get_char() == aOtherValue.charAt(0));
		}
	}

	//originally operator==
	public TitanBoolean operatorEquals( final TitanCharString other_value ) {
		mustBound("Unbound left operand of charstring element comparison.");
		other_value.mustBound("Unbound right operand of charstring element comparison.");

		if (other_value.getValue().length() != 1) {
			return new TitanBoolean(false);
		}

		return new TitanBoolean(get_char() == other_value.getValue().charAt(0));
	}

	//originally operator==
	public TitanBoolean operatorEquals( final TitanCharString_Element other_value ) {
		mustBound("Unbound left operand of charstring element comparison.");
		other_value.mustBound("Unbound right operand of charstring element comparison.");

		return new TitanBoolean(get_char() == other_value.str_val.getValue().charAt( other_value.char_pos ));
	}

	// operatorEquals for universalcharstring
	public TitanBoolean operatorEquals(final TitanUniversalCharString aOtherValue) {
		mustBound("The left operand of comparison is an unbound charstring element.");
		aOtherValue.mustBound("The right operand of comparison is an unbound universal charstring value.");

		if (aOtherValue.val_ptr.size() != 1) {
			return new TitanBoolean(false);
		} else if (aOtherValue.charstring) {
			return new TitanBoolean(str_val.getAt(char_pos).get_char() == aOtherValue.charAt(0).getUc_cell());
		} else {
			final TitanUniversalChar temp = aOtherValue.charAt(0);
			return new TitanBoolean(temp.getUc_group() == 0 && temp.getUc_plane() == 0 && temp.getUc_row() == 0
					&& str_val.getAt(char_pos).get_char() == temp.getUc_cell());
		}
	}

	// operatorEquals for universalcharstring_element
	public TitanBoolean operatorEquals(final TitanUniversalCharString_Element aOtherValue) {
		mustBound("The left operand of comparison is an unbound charstring element.");
		aOtherValue.mustBound("The right operand of comparison is an unbound universal charstring element.");

		final TitanUniversalChar temp = aOtherValue.get_char();
		return new TitanBoolean(temp.getUc_group() == 0 && temp.getUc_plane() == 0 && temp.getUc_row() == 0
				&& str_val.getAt(char_pos).get_char() == temp.getUc_cell());
	}

	// operatorNotEquals for String
	public TitanBoolean operatorNotEquals(final String aOtherValue) {
		return operatorEquals(aOtherValue).not();
	}

	// operatorNotEquals for charstring
	public TitanBoolean operatorNotEquals(final TitanUniversalCharString aOtherValue) {
		return operatorEquals(aOtherValue).not();
	}

	// operatorNotEquals for charstring_element

	public TitanBoolean operatorNotEquals(final TitanUniversalCharString_Element aOtherValue) {
		return operatorEquals(aOtherValue).not();
	}

	public char get_char() {
		return str_val.getValue().charAt( char_pos );
	}

	@Override
	public String toString() {
		if (str_val == null) {
			return "<unbound>";
		}

		return String.valueOf(str_val.getValue().charAt(char_pos));
	}

	// originally operator +
	public TitanCharString concatenate(final String aOtherValue) {
		mustBound("Unbound operand of charstring element concatenation.");

		if (aOtherValue != null) {
			final int otherLen = aOtherValue.length();
			final StringBuilder ret_val = new StringBuilder(otherLen + 1);
			ret_val.append(str_val.constGetAt(char_pos).toString());
			ret_val.append(aOtherValue);

			return new TitanCharString(ret_val);
		} else {
			return new TitanCharString(str_val.constGetAt(char_pos).toString());
		}
	}

	// originally operator +
	public TitanCharString concatenate(final TitanCharString aOtherValue) {
		mustBound("Unbound operand of charstring element concatenation.");
		aOtherValue.mustBound("Unbound operand of charstring concatenation.");

		final int nChars = aOtherValue.lengthOf().getInt();
		final StringBuilder ret_val = new StringBuilder(nChars + 1);
		ret_val.append(str_val.constGetAt(char_pos).toString());
		ret_val.append(aOtherValue.toString());

		return new TitanCharString(ret_val);
	}

	// originally operator +
	public TitanCharString concatenate(final TitanCharString_Element aOtherValue) {
		mustBound("Unbound operand of charstring element concatenation.");
		aOtherValue.mustBound("Unbound operand of charstring element concatenation.");

		final StringBuilder ret_val = new StringBuilder(2);
		ret_val.append(str_val.constGetAt(char_pos).toString());
		ret_val.append(aOtherValue.toString());

		return new TitanCharString(ret_val);
	}

	// originally operator +
	public TitanUniversalCharString concatenate(final TitanUniversalCharString aOtherValue) {
		mustBound("The left operand of concatenation is an unbound charstring value.");
		aOtherValue.mustBound("The right operand of concatenation is an unbound universal charstring value.");

		if (aOtherValue.charstring) {
			final StringBuilder val_ptr = new StringBuilder();
			val_ptr.append(get_char());
			val_ptr.append(aOtherValue.toString());
			return new TitanUniversalCharString(val_ptr);
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

	public void log() {
		if (bound_flag) {
			final char c = str_val.getAt(char_pos).get_char();
			if (TtcnLogger.isPrintable(c)) {
				TtcnLogger.log_char('"');
				TtcnLogger.logCharEscaped(c);
				TtcnLogger.log_char('"');
			} else {
				TtcnLogger.log_event("char(0, 0, 0, {0})", (int) c);
			}
		} else {
			TtcnLogger.log_event_unbound();
		}
	}

	//TODO: operatorEquals((TitanUniversalCharString) test
	//TODO: operatorEquals((TitanUniversalCharString_Element) test
	//TODO: operatorNotEquals((TitanUniversalCharString) test
	//TODO: operatorNotEquals((TitanUniversalCharString_Element) test

}
