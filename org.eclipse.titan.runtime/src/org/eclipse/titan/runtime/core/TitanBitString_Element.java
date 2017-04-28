package org.eclipse.titan.runtime.core;

import java.util.List;

public class TitanBitString_Element {
	boolean bound_flag;
	TitanBitString str_val;
	int nibble_pos;

	public TitanBitString_Element( boolean par_bound_flag, TitanBitString par_str_val, int par_nibble_pos ) {
		bound_flag = par_bound_flag;
		str_val = par_str_val;
		nibble_pos = par_nibble_pos;
	}

	public boolean isBound() {
		return bound_flag;
	}

	public boolean isValue() {
		return isBound();
	}

	public void mustBound( final String aErrorMessage ) {
		if ( !bound_flag ) {
			throw new TtcnError( aErrorMessage );
		}
	}

	//originally operator=
	public TitanBitString_Element assign( final TitanBitString_Element other_value ) {
		other_value.mustBound("Assignment of an unbound bitstring element.");
		bound_flag = true;
		str_val = new TitanBitString( other_value.str_val );
		str_val.setBit(nibble_pos, other_value.str_val.getBit(other_value.nibble_pos));
		return this;
	}

	//originally operator=
	public TitanBitString_Element assign( final TitanBitString other_value ) {
		other_value.mustBound("Assignment of unbound bitstring value.");
		if (other_value.getValue().size() != 1) {
			throw new TtcnError( "Assignment of a bitstring value " +
				"with length other than 1 to a bitstring element." );
		}
		bound_flag = true;
		str_val = new TitanBitString( other_value );
		str_val.setBit(nibble_pos, other_value.getBit(0));
		return this;
	}

	//originally operator==
	public boolean equalsTo( final TitanBitString_Element other_value ) {
		mustBound("Unbound left operand of bitstring element comparison.");
		other_value.mustBound("Unbound right operand of bitstring comparison.");
		return str_val.getBit(nibble_pos) == other_value.str_val.getBit( other_value.nibble_pos );
	}

	//originally operator==
	public boolean equalsTo( final TitanBitString other_value ) {
		mustBound("Unbound left operand of bitstring element comparison.");
		other_value.mustBound("Unbound right operand of bitstring element comparison.");
		if (other_value.getValue().size() != 1) {
			return false;
		}
		return str_val.getBit(nibble_pos) == other_value.getBit(0);
	}

	//originally operator+
	public TitanBitString append( final TitanBitString other_value ) {
		mustBound("Unbound left operand of bitstring element concatenation.");
		other_value.mustBound("Unbound right operand of bitstring concatenation.");
		final List<Byte> src_ptr = other_value.getValue();
		int n_nibbles = src_ptr.size();
		TitanBitString ret_val = new TitanBitString();
		final List<Byte> dest_ptr = ret_val.getValue();
		dest_ptr.set(0, str_val.getBit(nibble_pos) );
		// chars in the result minus 1
		for (int i = 0; i < n_nibbles; i++) {
			dest_ptr.set( i, src_ptr.get( i ) );
		}
		return ret_val;
	}

	//originally operator+
	public TitanBitString append( final TitanBitString_Element other_value ) {
		mustBound("Unbound left operand of bitstring element concatenation.");
		other_value.mustBound("Unbound right operand of bitstring element concatenation.");
		return new TitanBitString( other_value.str_val );
	}

	//originally operator~
	TitanBitString operatorBitwiseNot()	{
		mustBound("Unbound bitstring element operand of operator not4b.");
		byte result = (byte) (~str_val.getBit(nibble_pos) & 0x0F);
		return new TitanBitString( result );
	}

	//originally operator&
	TitanBitString operatorBitwiseAnd(final TitanBitString other_value) {
		mustBound("Left operand of operator and4b is an unbound bitstring element.");
		other_value.mustBound("Right operand of operator and4b is an unbound bitstring value.");
		if (other_value.getValue().size() != 1) {
			throw new TtcnError("The bitstring operands of operator and4b must have the same length.");
		}
		byte result = (byte) (str_val.getBit(nibble_pos) & other_value.getBit(0));
		return new TitanBitString( result );
	}

	//originally operator&
	TitanBitString operatorBitwiseAnd(final TitanBitString_Element other_value) {
		mustBound("Left operand of operator and4b is an unbound bitstring element.");
		other_value.mustBound("Right operand of operator and4b is an unbound bitstring element.");
		byte result = (byte) (str_val.getBit(nibble_pos) & other_value.str_val.getBit(other_value.nibble_pos));
		return new TitanBitString( result );
	}

	//originally operator|
	TitanBitString operatorBitwiseOr(final TitanBitString other_value) {
		mustBound("Left operand of operator or4b is an unbound bitstring element.");
		other_value.mustBound("Right operand of operator or4b is an unbound bitstring value.");
		if (other_value.getValue().size() != 1) {
			throw new TtcnError("The bitstring operands of operator or4b must have the same length.");
		}
		byte result = (byte) (str_val.getBit(nibble_pos) | other_value.getBit(0));
		return new TitanBitString( result );
	}

	//originally operator|
	TitanBitString operatorBitwiseOr(final TitanBitString_Element other_value) {
		mustBound("Left operand of operator or4b is an unbound bitstring element.");
		other_value.mustBound("Right operand of operator or4b is an unbound bitstring element.");
		byte result = (byte) (str_val.getBit(nibble_pos) | other_value.str_val.getBit(other_value.nibble_pos));
		return new TitanBitString( result );
	}

	//originally operator^
	TitanBitString operatorBitwiseXor(final TitanBitString other_value) {
		mustBound("Left operand of operator xor4b is an unbound bitstring element.");
		other_value.mustBound("Right operand of operator xor4b is an unbound bitstring value.");
		if (other_value.getValue().size() != 1) {
			throw new TtcnError("The bitstring operands of operator xor4b must have the same length.");
		}
		byte result = (byte) (str_val.getBit(nibble_pos) ^ other_value.getBit(0));
		return new TitanBitString( result );
	}

	//originally operator^
	TitanBitString operatorBitwiseXor(final TitanBitString_Element other_value) {
		mustBound("Left operand of operator xor4b is an unbound bitstring element.");
		other_value.mustBound("Right operand of operator xor4b is an unbound bitstring element.");
		byte result = (byte) (str_val.getBit(nibble_pos) ^ other_value.str_val.getBit(other_value.nibble_pos));
		return new TitanBitString( result );
	}

	char get_nibble() {
		return (char) str_val.getBit( nibble_pos );
	}

	void log() {
		if ( bound_flag ) {
			TtcnLogger.log_char('\'');
			TtcnLogger.log_bit(str_val.getBit(nibble_pos));
			TtcnLogger.log_event_str("'B");
		}
		else {
			TtcnLogger.log_event_unbound();
		}
	}
}
