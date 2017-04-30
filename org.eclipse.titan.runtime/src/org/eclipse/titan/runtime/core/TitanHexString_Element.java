package org.eclipse.titan.runtime.core;

import java.util.List;

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

	//originally operator=
	public TitanHexString_Element assign( final TitanHexString_Element other_value ) {
		other_value.mustBound("Assignment of an unbound hexstring element.");
		bound_flag = true;
		str_val = new TitanHexString( other_value.str_val );
		str_val.set_nibble(nibble_pos, other_value.str_val.get_nibble(other_value.nibble_pos));
		return this;
	}

	//originally operator=
	public TitanHexString_Element assign( final TitanHexString other_value ) {
		other_value.mustBound("Assignment of unbound hexstring value.");
		if (other_value.getValue().size() != 1) {
			throw new TtcnError( "Assignment of a hexstring value " +
					"with length other than 1 to a hexstring element." );
		}
		bound_flag = true;
		str_val = new TitanHexString( other_value );
		str_val.set_nibble(nibble_pos, other_value.get_nibble(0));
		return this;
	}

	//originally operator==
	public boolean equalsTo( final TitanHexString_Element other_value ) {
		mustBound("Unbound left operand of hexstring element comparison.");
		other_value.mustBound("Unbound right operand of hexstring comparison.");
		return str_val.get_nibble(nibble_pos) == other_value.str_val.get_nibble( other_value.nibble_pos );
	}

	//originally operator==
	public boolean equalsTo( final TitanHexString other_value ) {
		mustBound("Unbound left operand of hexstring element comparison.");
		other_value.mustBound("Unbound right operand of hexstring element comparison.");
		if (other_value.getValue().size() != 1) {
			return false;
		}
		return str_val.get_nibble(nibble_pos) == other_value.get_nibble(0);
	}

	//originally operator+
	public TitanHexString append( final TitanHexString other_value ) {
		mustBound("Unbound left operand of hexstring element concatenation.");
		other_value.mustBound("Unbound right operand of hexstring concatenation.");
		final List<Byte> src_ptr = other_value.getValue();
		final int n_nibbles = src_ptr.size();
		final TitanHexString ret_val = new TitanHexString();
		final List<Byte> dest_ptr = ret_val.getValue();
		dest_ptr.set(0, str_val.get_nibble(nibble_pos) );
		// bytes in the result minus 1
		for (int i = 0; i < n_nibbles; i++) {
			dest_ptr.set( i, src_ptr.get( i ) );
		}
		return ret_val;
	}

	//originally operator+
	public TitanHexString append( final TitanHexString_Element other_value ) {
		mustBound("Unbound left operand of hexstring element concatenation.");
		other_value.mustBound("Unbound right operand of hexstring element concatenation.");
		return new TitanHexString( other_value.str_val );
	}

	//originally operator~
	public TitanHexString operatorBitwiseNot() {
		mustBound("Unbound hexstring element operand of operator not4b.");
		final byte result = (byte) (~str_val.get_nibble(nibble_pos) & 0x0F);
		return new TitanHexString( result );
	}

	//originally operator&
	public TitanHexString operatorBitwiseAnd(final TitanHexString other_value) {
		mustBound("Left operand of operator and4b is an unbound hexstring element.");
		other_value.mustBound("Right operand of operator and4b is an unbound hexstring value.");
		if (other_value.getValue().size() != 1) {
			throw new TtcnError("The hexstring operands of operator and4b must have the same length.");
		}
		final byte result = (byte) (str_val.get_nibble(nibble_pos) & other_value.get_nibble(0));
		return new TitanHexString( result );
	}

	//originally operator&
	public TitanHexString operatorBitwiseAnd(final TitanHexString_Element other_value) {
		mustBound("Left operand of operator and4b is an unbound hexstring element.");
		other_value.mustBound("Right operand of operator and4b is an unbound hexstring element.");
		final byte result = (byte) (str_val.get_nibble(nibble_pos) & other_value.str_val.get_nibble(other_value.nibble_pos));
		return new TitanHexString( result );
	}

	//originally operator|
	public TitanHexString operatorBitwiseOr(final TitanHexString other_value) {
		mustBound("Left operand of operator or4b is an unbound hexstring element.");
		other_value.mustBound("Right operand of operator or4b is an unbound hexstring value.");
		if (other_value.getValue().size() != 1) {
			throw new TtcnError("The hexstring operands of operator or4b must have the same length.");
		}
		final byte result = (byte) (str_val.get_nibble(nibble_pos) | other_value.get_nibble(0));
		return new TitanHexString( result );
	}

	//originally operator|
	public TitanHexString operatorBitwiseOr(final TitanHexString_Element other_value) {
		mustBound("Left operand of operator or4b is an unbound hexstring element.");
		other_value.mustBound("Right operand of operator or4b is an unbound hexstring element.");
		final byte result = (byte) (str_val.get_nibble(nibble_pos) | other_value.str_val.get_nibble(other_value.nibble_pos));
		return new TitanHexString( result );
	}

	//originally operator^
	public TitanHexString operatorBitwiseXor(final TitanHexString other_value) {
		mustBound("Left operand of operator xor4b is an unbound hexstring element.");
		other_value.mustBound("Right operand of operator xor4b is an unbound hexstring value.");
		if (other_value.getValue().size() != 1) {
			throw new TtcnError("The hexstring operands of operator xor4b must have the same length.");
		}
		final byte result = (byte) (str_val.get_nibble(nibble_pos) ^ other_value.get_nibble(0));
		return new TitanHexString( result );
	}

	//originally operator^
	public TitanHexString operatorBitwiseXor(final TitanHexString_Element other_value) {
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
