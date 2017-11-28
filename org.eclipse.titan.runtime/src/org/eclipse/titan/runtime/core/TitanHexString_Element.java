/******************************************************************************
 * Copyright (c) 2000-2017 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;



public class TitanHexString_Element {
	private boolean bound_flag;
	private TitanHexString str_val;
	private int nibble_pos;

	public TitanHexString_Element( final boolean par_bound_flag, final TitanHexString par_str_val, final int par_nibble_pos ) {
		bound_flag = par_bound_flag;
		str_val = par_str_val;
		nibble_pos = par_nibble_pos;
	}

	public boolean isBound() {
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

	//originally operator=
	public TitanHexString_Element assign( final TitanHexString_Element other_value ) {
		other_value.mustBound("Assignment of an unbound hexstring element.");

		bound_flag = true;
		str_val.set_nibble(nibble_pos, other_value.str_val.get_nibble(other_value.nibble_pos));
		return this;
	}

	//originally operator=
	public TitanHexString_Element assign( final TitanHexString other_value ) {
		other_value.mustBound("Assignment of unbound hexstring value.");

		if (other_value.getValue().length != 1) {
			throw new TtcnError( "Assignment of a hexstring value " +
					"with length other than 1 to a hexstring element." );
		}

		bound_flag = true;
		str_val.set_nibble(nibble_pos, other_value.get_nibble(0));
		return this;
	}

	//originally operator==
	public boolean operatorEquals( final TitanHexString_Element other_value ) {
		mustBound("Unbound left operand of hexstring element comparison.");
		other_value.mustBound("Unbound right operand of hexstring comparison.");

		return str_val.get_nibble(nibble_pos) == other_value.str_val.get_nibble( other_value.nibble_pos );
	}

	//originally operator==
	public boolean operatorEquals( final TitanHexString other_value ) {
		mustBound("Unbound left operand of hexstring element comparison.");
		other_value.mustBound("Unbound right operand of hexstring element comparison.");

		if (other_value.getValue().length != 1) {
			return false;
		}

		return str_val.get_nibble(nibble_pos) == other_value.get_nibble(0);
	}

	//originally operator+
	public TitanHexString concatenate( final TitanHexString other_value ) {
		mustBound("Unbound left operand of hexstring element concatenation.");
		other_value.mustBound("Unbound right operand of hexstring concatenation.");

		final byte src_ptr[] = other_value.getValue();
		final int n_nibbles = src_ptr.length;
		final byte dest_ptr[] = new byte[1 + n_nibbles];
		dest_ptr[0] = str_val.get_nibble(nibble_pos);
		System.arraycopy(src_ptr, 0, dest_ptr, 1, n_nibbles);

		final TitanHexString ret_val = new TitanHexString(dest_ptr);
		return ret_val;
	}

	//originally operator+
	public TitanHexString concatenate( final TitanHexString_Element other_value ) {
		mustBound("Unbound left operand of hexstring element concatenation.");
		other_value.mustBound("Unbound right operand of hexstring element concatenation.");

		return new TitanHexString( other_value.str_val );
	}

	//originally operator~
	public TitanHexString not4b() {
		mustBound("Unbound hexstring element operand of operator not4b.");

		final byte result = (byte) (~str_val.get_nibble(nibble_pos) & 0x0F);
		return new TitanHexString( result );
	}

	//originally operator&
	public TitanHexString and4b(final TitanHexString other_value) {
		mustBound("Left operand of operator and4b is an unbound hexstring element.");
		other_value.mustBound("Right operand of operator and4b is an unbound hexstring value.");

		if (other_value.getValue().length != 1) {
			throw new TtcnError("The hexstring operands of operator and4b must have the same length.");
		}

		final byte result = (byte) (str_val.get_nibble(nibble_pos) & other_value.get_nibble(0));
		return new TitanHexString( result );
	}

	//originally operator&
	public TitanHexString and4b(final TitanHexString_Element other_value) {
		mustBound("Left operand of operator and4b is an unbound hexstring element.");
		other_value.mustBound("Right operand of operator and4b is an unbound hexstring element.");

		final byte result = (byte) (str_val.get_nibble(nibble_pos) & other_value.str_val.get_nibble(other_value.nibble_pos));
		return new TitanHexString( result );
	}

	//originally operator|
	public TitanHexString or4b(final TitanHexString other_value) {
		mustBound("Left operand of operator or4b is an unbound hexstring element.");
		other_value.mustBound("Right operand of operator or4b is an unbound hexstring value.");

		if (other_value.getValue().length != 1) {
			throw new TtcnError("The hexstring operands of operator or4b must have the same length.");
		}

		final byte result = (byte) (str_val.get_nibble(nibble_pos) | other_value.get_nibble(0));
		return new TitanHexString( result );
	}

	//originally operator|
	public TitanHexString or4b(final TitanHexString_Element other_value) {
		mustBound("Left operand of operator or4b is an unbound hexstring element.");
		other_value.mustBound("Right operand of operator or4b is an unbound hexstring element.");

		final byte result = (byte) (str_val.get_nibble(nibble_pos) | other_value.str_val.get_nibble(other_value.nibble_pos));
		return new TitanHexString( result );
	}

	//originally operator^
	public TitanHexString xor4b(final TitanHexString other_value) {
		mustBound("Left operand of operator xor4b is an unbound hexstring element.");
		other_value.mustBound("Right operand of operator xor4b is an unbound hexstring value.");

		if (other_value.getValue().length != 1) {
			throw new TtcnError("The hexstring operands of operator xor4b must have the same length.");
		}

		final byte result = (byte) (str_val.get_nibble(nibble_pos) ^ other_value.get_nibble(0));
		return new TitanHexString( result );
	}

	//originally operator^
	public TitanHexString xor4b(final TitanHexString_Element other_value) {
		mustBound("Left operand of operator xor4b is an unbound hexstring element.");
		other_value.mustBound("Right operand of operator xor4b is an unbound hexstring element.");

		final byte result = (byte) (str_val.get_nibble(nibble_pos) ^ other_value.str_val.get_nibble(other_value.nibble_pos));
		return new TitanHexString( result );
	}

	public char get_nibble() {
		return (char) str_val.get_nibble( nibble_pos );
	}

	public void log() {
		if ( bound_flag ) {
			TtcnLogger.log_char('\'');
			TtcnLogger.log_hex(str_val.get_nibble(nibble_pos));
			TtcnLogger.log_event_str("'H");
		}
		else {
			TtcnLogger.log_event_unbound();
		}
	}
}
