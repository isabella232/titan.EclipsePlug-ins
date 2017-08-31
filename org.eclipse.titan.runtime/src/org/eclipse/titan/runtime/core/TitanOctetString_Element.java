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
 * @author Farkas Izabella Ingrid
 * */

public class TitanOctetString_Element {
	private boolean bound_flag;
	private TitanOctetString str_val;
	private int nibble_pos;

	public TitanOctetString_Element( final boolean par_bound_flag, final TitanOctetString par_str_val, final int par_nibble_pos ) {
		bound_flag = par_bound_flag;
		str_val = par_str_val;
		nibble_pos = par_nibble_pos;
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

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		final int digit = get_nibble();
		sb.append( TitanHexString.HEX_DIGITS.charAt( digit / 16 ) );
		sb.append( TitanHexString.HEX_DIGITS.charAt( digit % 16 ) );
		return sb.toString();
	}

	// originally operator=
	public TitanOctetString_Element assign( final TitanOctetString_Element other_value ) {
		other_value.mustBound("Assignment of an unbound octetstring element.");

		bound_flag = true;
		str_val.set_nibble(nibble_pos, other_value.str_val.get_nibble(other_value.nibble_pos));

		return this;
	}

	// originally operator=
	public TitanOctetString_Element assign( final TitanOctetString other_value ) {
		other_value.mustBound("Assignment of unbound octetstring value.");

		if (other_value.getValue().size() != 1) {
			throw new TtcnError( "Assignment of a octetstring value " +
				"with length other than 1 to a octetstring element." );
		}

		bound_flag = true;
		str_val.set_nibble(nibble_pos, other_value.get_nibble(0));
		return this;
	}
	
	// originally operator==
	public TitanBoolean operatorEquals( final TitanOctetString_Element other_value ) {
		mustBound("Unbound left operand of octetstring element comparison.");
		other_value.mustBound("Unbound right operand of octetstring comparison.");

		return new TitanBoolean(str_val.get_nibble(nibble_pos) == other_value.str_val.get_nibble( other_value.nibble_pos ));
	}

	// originally operator==
	public TitanBoolean operatorEquals( final TitanOctetString other_value ) {
		mustBound("Unbound left operand of octetstring element comparison.");
		other_value.mustBound("Unbound right operand of octetstring element comparison.");

		if (other_value.getValue().size() != 1) {
			return new TitanBoolean(false);
		}

		return new TitanBoolean(str_val.get_nibble(nibble_pos) == other_value.get_nibble(0));
	}

	// originally operator!=
	public TitanBoolean operatorNotEquals( final TitanOctetString_Element aOtherValue ) {
		return operatorEquals( aOtherValue ).not();
	}

	// originally operator!=
	public TitanBoolean operatorNotEquals( final TitanOctetString aOtherValue ) {
		return operatorEquals( aOtherValue ).not();
	}
	
	// originally operator+
	public TitanOctetString concatenate( final TitanOctetString other_value ) {
		mustBound("Unbound left operand of octetstring element concatenation.");
		other_value.mustBound("Unbound right operand of octetstring concatenation.");

		final List<Character> src_ptr = other_value.getValue();
		final int n_nibbles = src_ptr.size();
		final List<Character> dest_ptr = new ArrayList<Character>();
		dest_ptr.add(0, str_val.get_nibble(nibble_pos) );
		// chars in the result minus 1
		for (int i = 0; i < n_nibbles; i++) {
			dest_ptr.add( i+1, src_ptr.get( i ) );
		}

		return new TitanOctetString(dest_ptr);
	}

	// originally operator+
	public TitanOctetString concatenate( final TitanOctetString_Element other_value ) {
		mustBound("Unbound left operand of octetstring element concatenation.");
		other_value.mustBound("Unbound right operand of octetstring element concatenation.");

		final List<Character> dest_ptr = new ArrayList<Character>();
		dest_ptr.add(0, str_val.get_nibble(nibble_pos) );
		dest_ptr.add(1,other_value.get_nibble());

		return new TitanOctetString(dest_ptr);
	}

	// originally operator~
	public TitanOctetString not4b() {
		mustBound("Unbound octetstring element operand of operator not4b.");

		final int temp = str_val.get_nibble(nibble_pos);
		final int digit1 = temp >> 4;
		final int digit2 = temp & 0x0F;
		final int negDigit1 = ~digit1 & 0x0F;
		final int negDigit2 = ~digit2 & 0x0F;
		return new TitanOctetString( (char) ((negDigit1  << 4) + negDigit2) );
	}

	// originally operator&
	public TitanOctetString and4b(final TitanOctetString other_value) {
		mustBound("Left operand of operator and4b is an unbound octetstring element.");
		other_value.mustBound("Right operand of operator and4b is an unbound octetstring value.");

		if (other_value.getValue().size() != 1) {
			throw new TtcnError("The octetstring operands of operator and4b must have the same length.");
		}

		final char result = (char) (str_val.get_nibble(nibble_pos) & other_value.get_nibble(0));
		return new TitanOctetString( result );
	}

	// originally operator&
	public TitanOctetString and4b(final TitanOctetString_Element other_value) {
		mustBound("Left operand of operator and4b is an unbound octetstring element.");
		other_value.mustBound("Right operand of operator and4b is an unbound octetstring element.");

		final char result = (char) (str_val.get_nibble(nibble_pos) & other_value.str_val.get_nibble(other_value.nibble_pos));
		return new TitanOctetString( result );
	}

	// originally operator|
	public TitanOctetString or4b(final TitanOctetString other_value) {
		mustBound("Left operand of operator or4b is an unbound octetstring element.");
		other_value.mustBound("Right operand of operator or4b is an unbound octetstring value.");

		if (other_value.getValue().size() != 1) {
			throw new TtcnError("The octetstring operands of operator or4b must have the same length.");
		}

		final char result = (char) (str_val.get_nibble(nibble_pos) | other_value.get_nibble(0));
		return new TitanOctetString( result );
	}

	//originally operator|
	public TitanOctetString or4b(final TitanOctetString_Element other_value) {
		mustBound("Left operand of operator or4b is an unbound octetstring element.");
		other_value.mustBound("Right operand of operator or4b is an unbound octetstring element.");

		final char result = (char) (str_val.get_nibble(nibble_pos) | other_value.str_val.get_nibble(other_value.nibble_pos));
		return new TitanOctetString( result );
	}

	//originally operator^
	public TitanOctetString xor4b(final TitanOctetString other_value) {
		mustBound("Left operand of operator xor4b is an unbound octetstring element.");
		other_value.mustBound("Right operand of operator xor4b is an unbound octetstring value.");

		if (other_value.getValue().size() != 1) {
			throw new TtcnError("The octetstring operands of operator xor4b must have the same length.");
		}

		final char result = (char) (str_val.get_nibble(nibble_pos) ^ other_value.get_nibble(0));
		return new TitanOctetString( result );
	}

	//originally operator^
	public TitanOctetString xor4b(final TitanOctetString_Element other_value) {
		mustBound("Left operand of operator xor4b is an unbound octetstring element.");
		other_value.mustBound("Right operand of operator xor4b is an unbound octetstring element.");

		final char result = (char) (str_val.get_nibble(nibble_pos) ^ other_value.str_val.get_nibble(other_value.nibble_pos));
		return new TitanOctetString( result );
	}

	public char get_nibble() {
		return (char) str_val.get_nibble( nibble_pos );
	}

	public void log() {
		if ( bound_flag ) {
			TtcnLogger.log_char('\'');
			TtcnLogger.log_octet(str_val.get_nibble(nibble_pos));
			TtcnLogger.log_event_str("'O");
		} else {
			TtcnLogger.log_event_unbound();
		}
	}
}
