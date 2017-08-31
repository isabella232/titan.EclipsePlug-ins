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

public class TitanBitString_Element {
	private boolean bound_flag;
	private TitanBitString str_val;
	private int bit_pos;

	public TitanBitString_Element( final boolean par_bound_flag, final TitanBitString par_str_val, final int par_bit_pos ) {
		bound_flag = par_bound_flag;
		str_val = par_str_val;
		bit_pos = par_bit_pos;
	}

	public TitanBoolean isBound() {
		return new TitanBoolean(bound_flag);
	}

	public TitanBoolean isValue() {
		return isBound();
	}

	public void mustBound( final String aErrorMessage ) {
		if ( !bound_flag ) {
			throw new TtcnError( aErrorMessage );
		}
	}

	//originally operator=
	public TitanBitString_Element assign( final TitanBitString_Element otherValue ) {
		otherValue.mustBound("Assignment of an unbound bitstring element.");

		bound_flag = true;
		str_val.setBit(bit_pos, otherValue.str_val.getBit(otherValue.bit_pos));
		return this;
	}

	//originally operator=
	public TitanBitString_Element assign( final TitanBitString otherValue ) {
		otherValue.mustBound("Assignment of unbound bitstring value.");

		if (otherValue.lengthOf().getInt() != 1) {
			throw new TtcnError( "Assignment of a bitstring value " +
					"with length other than 1 to a bitstring element." );
		}

		bound_flag = true;
		str_val.setBit(bit_pos, otherValue.getBit(0));
		return this;
	}

	//originally operator==
	public TitanBoolean operatorEquals( final TitanBitString_Element otherValue ) {
		mustBound("Unbound left operand of bitstring element comparison.");
		otherValue.mustBound("Unbound right operand of bitstring comparison.");

		return new TitanBoolean(str_val.getBit(bit_pos) == otherValue.str_val.getBit( otherValue.bit_pos ));
	}

	//originally operator==
	public TitanBoolean operatorEquals( final TitanBitString otherValue ) {
		mustBound("Unbound left operand of bitstring element comparison.");
		otherValue.mustBound("Unbound right operand of bitstring element comparison.");

		if (otherValue.lengthOf().getInt() != 1) {
			return new TitanBoolean(false);
		}

		return new TitanBoolean( str_val.getBit(bit_pos) == otherValue.getBit(0));
	}

	//originally operator!=
	public TitanBoolean operatorNotEquals(final TitanBitString_Element otherValue){
		return operatorEquals(otherValue).not();
	}

	//originally operator!=
	public TitanBoolean operatorNotEquals(final TitanBitString otherValue){
		return operatorEquals(otherValue).not();
	}

	//FIXME: can be faster
	//originally operator+
	public TitanBitString concatenate( final TitanBitString otherValue ) {
		mustBound("Unbound left operand of bitstring element concatenation.");
		otherValue.mustBound("Unbound right operand of bitstring concatenation.");

		final int n_bits = otherValue.lengthOf().getInt();
		final List<Byte> result = new ArrayList<Byte>();
		final List<Byte> temp = new ArrayList<Byte>(otherValue.getValue());
		final int n_bytes = (n_bits + 7) / 8;
		
		for (int byte_count = 0; byte_count < n_bytes; byte_count++) {
			result.add((byte)0);
		}
		result.set(0,(byte)(get_bit() ? 1 : 0));
		for (int byte_count = 0; byte_count < n_bytes; byte_count++) {
			result.set(byte_count, (byte)(result.get(byte_count) | temp.get(byte_count) << 1));
			if(n_bits > byte_count * 8 + 7){
				result.set(byte_count+1, (byte)((temp.get(byte_count) & 128) >> 7));
			}
		}

		final TitanBitString ret_val = new TitanBitString(result, n_bits+1);
		return ret_val;
	}

	//originally operator+
	public TitanBitString concatenate( final TitanBitString_Element otherValue ) {
		mustBound("Unbound left operand of bitstring element concatenation.");
		otherValue.mustBound("Unbound right operand of bitstring element concatenation.");

		int result = str_val.getBit(bit_pos) ? 1 : 2;
		if (otherValue.get_bit()) {
			result = result | 2;
		}
		final ArrayList<Byte> temp_ptr = new ArrayList<Byte>();
		temp_ptr.add((byte)result);
		return new TitanBitString( temp_ptr, 2 );
	}

	//originally operator~
	public TitanBitString not4b()	{
		mustBound("Unbound bitstring element operand of operator not4b.");

		final byte result = (byte) (str_val.getBit(bit_pos) ? 0 : 1);
		//FIXME: can be faster
		final List<Byte> dest_ptr = new ArrayList<Byte>();
		dest_ptr.add(result);
		return new TitanBitString( dest_ptr,1 );
	}

	//originally operator&
	public TitanBitString and4b(final TitanBitString otherValue) {
		mustBound("Left operand of operator and4b is an unbound bitstring element.");
		otherValue.mustBound("Right operand of operator and4b is an unbound bitstring value.");

		if (otherValue.lengthOf().getInt() != 1) {
			throw new TtcnError("The bitstring operands of operator and4b must have the same length.");
		}

		final boolean temp = str_val.getBit(bit_pos) & otherValue.getBit(0);
		final byte result = (byte) (temp ? 1 : 0);
		//FIXME: can be faster
		final List<Byte> dest_ptr = new ArrayList<Byte>();
		dest_ptr.add(result);
		return new TitanBitString( dest_ptr,1 );
	}

	//originally operator&
	public TitanBitString and4b(final TitanBitString_Element otherValue) {
		mustBound("Left operand of operator and4b is an unbound bitstring element.");
		otherValue.mustBound("Right operand of operator and4b is an unbound bitstring element.");

		final boolean temp = str_val.getBit(bit_pos) & otherValue.get_bit();
		final byte result = (byte) (temp ? 1 : 0);
		//FIXME: can be faster
		final List<Byte> dest_ptr = new ArrayList<Byte>();
		dest_ptr.add(result);
		return new TitanBitString( dest_ptr,1 );
	}

	//originally operator|
	public TitanBitString or4b(final TitanBitString otherValue) {
		mustBound("Left operand of operator or4b is an unbound bitstring element.");
		otherValue.mustBound("Right operand of operator or4b is an unbound bitstring value.");

		if (otherValue.lengthOf().getInt() != 1) {
			throw new TtcnError("The bitstring operands of operator or4b must have the same length.");
		}

		final boolean temp = str_val.getBit(bit_pos) | otherValue.getBit(0);
		final byte result = (byte) (temp ? 1 : 0);
		//FIXME: can be faster
		final List<Byte> dest_ptr = new ArrayList<Byte>();
		dest_ptr.add(result);
		return new TitanBitString( dest_ptr,1 );
	}

	//originally operator|
	public TitanBitString or4b(final TitanBitString_Element otherValue) {
		mustBound("Left operand of operator or4b is an unbound bitstring element.");
		otherValue.mustBound("Right operand of operator or4b is an unbound bitstring element.");

		final boolean temp = str_val.getBit(bit_pos) | otherValue.get_bit();
		final byte result = (byte) (temp ? 1 : 0);
		//FIXME: can be faster
		final List<Byte> dest_ptr = new ArrayList<Byte>();
		dest_ptr.add(result);
		return new TitanBitString( dest_ptr,1 );
	}

	//originally operator^
	public TitanBitString xor4b(final TitanBitString otherValue) {
		mustBound("Left operand of operator xor4b is an unbound bitstring element.");
		otherValue.mustBound("Right operand of operator xor4b is an unbound bitstring value.");

		if (otherValue.lengthOf().getInt()!= 1) {
			throw new TtcnError("The bitstring operands of operator xor4b must have the same length.");
		}

		final boolean temp = str_val.getBit(bit_pos) ^ otherValue.getBit(0);
		final byte result = (byte) (temp ? 1 : 0);
		//FIXME: can be faster
		final List<Byte> dest_ptr = new ArrayList<Byte>();
		dest_ptr.add(result);
		return new TitanBitString( dest_ptr,1 );
	}

	//originally operator^
	public TitanBitString xor4b(final TitanBitString_Element otherValue) {
		mustBound("Left operand of operator xor4b is an unbound bitstring element.");
		otherValue.mustBound("Right operand of operator xor4b is an unbound bitstring element.");

		final boolean temp = str_val.getBit(bit_pos) ^ otherValue.get_bit();
		final byte result = (byte) (temp ? 1 : 0);
		//FIXME: can be faster
		final List<Byte> dest_ptr = new ArrayList<Byte>();
		dest_ptr.add(result);
		return new TitanBitString( dest_ptr,1 );
	}

	public boolean get_bit() {
		return str_val.getBit( bit_pos );
	}

	public void log() {
		if ( bound_flag ) {
			TtcnLogger.log_char('\'');
			TtcnLogger.log_char(str_val.getBit(bit_pos) ? '1' : '0');
			TtcnLogger.log_event_str("'B");
		}
		else {
			TtcnLogger.log_event_unbound();
		}
	}

	@Override
	public String toString() {
		final StringBuilder result = new StringBuilder();
		result.append('\'');
		result.append(str_val.getBit(bit_pos) ? '1' : '0');
		result.append('\'');

		return result.toString();
	}
}
