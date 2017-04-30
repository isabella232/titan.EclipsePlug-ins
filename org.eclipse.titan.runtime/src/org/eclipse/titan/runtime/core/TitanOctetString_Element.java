package org.eclipse.titan.runtime.core;

import java.util.List;

public class TitanOctetString_Element {
	boolean bound_flag;
	TitanOctetString str_val;
	int nibble_pos;

	public TitanOctetString_Element( final boolean par_bound_flag, final TitanOctetString par_str_val, final int par_nibble_pos ) {
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
	public TitanOctetString_Element assign( final TitanOctetString_Element other_value ) {
		other_value.mustBound("Assignment of an unbound octetstring element.");
		bound_flag = true;
		str_val = new TitanOctetString( other_value.str_val );
		str_val.set_nibble(nibble_pos, other_value.str_val.get_nibble(other_value.nibble_pos));
		return this;
	}

	//originally operator=
	public TitanOctetString_Element assign( final TitanOctetString other_value ) {
		other_value.mustBound("Assignment of unbound octetstring value.");
		if (other_value.getValue().size() != 1) {
			throw new TtcnError( "Assignment of a octetstring value " +
				"with length other than 1 to a octetstring element." );
		}
		bound_flag = true;
		str_val = new TitanOctetString( other_value );
		str_val.set_nibble(nibble_pos, other_value.get_nibble(0));
		return this;
	}
	
	//originally operator==
	public boolean equalsTo( final TitanOctetString_Element other_value ) {
		mustBound("Unbound left operand of octetstring element comparison.");
		other_value.mustBound("Unbound right operand of octetstring comparison.");
		return str_val.get_nibble(nibble_pos) == other_value.str_val.get_nibble( other_value.nibble_pos );
	}

	//originally operator==
	public boolean equalsTo( final TitanOctetString other_value ) {
		mustBound("Unbound left operand of octetstring element comparison.");
		other_value.mustBound("Unbound right operand of octetstring element comparison.");
		if (other_value.getValue().size() != 1) {
			return false;
		}
		return str_val.get_nibble(nibble_pos) == other_value.get_nibble(0);
	}

	//originally operator+
	public TitanOctetString append( final TitanOctetString other_value ) {
		mustBound("Unbound left operand of octetstring element concatenation.");
		other_value.mustBound("Unbound right operand of octetstring concatenation.");
		final List<Character> src_ptr = other_value.getValue();
		int n_nibbles = src_ptr.size();
		TitanOctetString ret_val = new TitanOctetString();
		final List<Character> dest_ptr = ret_val.getValue();
		dest_ptr.set(0, str_val.get_nibble(nibble_pos) );
		// chars in the result minus 1
		for (int i = 0; i < n_nibbles; i++) {
			dest_ptr.set( i, src_ptr.get( i ) );
		}
		return ret_val;
	}

	//originally operator+
	public TitanOctetString append( final TitanOctetString_Element other_value ) {
		mustBound("Unbound left operand of octetstring element concatenation.");
		other_value.mustBound("Unbound right operand of octetstring element concatenation.");
		return new TitanOctetString( other_value.str_val );
	}

	//originally operator~
	TitanOctetString operatorBitwiseNot()	{
		mustBound("Unbound octetstring element operand of operator not4b.");
		char result = (char) (~str_val.get_nibble(nibble_pos) & 0x0F);
		return new TitanOctetString( result );
	}

	//originally operator&
	TitanOctetString operatorBitwiseAnd(final TitanOctetString other_value) {
		mustBound("Left operand of operator and4b is an unbound octetstring element.");
		other_value.mustBound("Right operand of operator and4b is an unbound octetstring value.");
		if (other_value.getValue().size() != 1) {
			throw new TtcnError("The octetstring operands of operator and4b must have the same length.");
		}
		char result = (char) (str_val.get_nibble(nibble_pos) & other_value.get_nibble(0));
		return new TitanOctetString( result );
	}

	//originally operator&
	TitanOctetString operatorBitwiseAnd(final TitanOctetString_Element other_value) {
		mustBound("Left operand of operator and4b is an unbound octetstring element.");
		other_value.mustBound("Right operand of operator and4b is an unbound octetstring element.");
		char result = (char) (str_val.get_nibble(nibble_pos) & other_value.str_val.get_nibble(other_value.nibble_pos));
		return new TitanOctetString( result );
	}

	//originally operator|
	TitanOctetString operatorBitwiseOr(final TitanOctetString other_value) {
		mustBound("Left operand of operator or4b is an unbound octetstring element.");
		other_value.mustBound("Right operand of operator or4b is an unbound octetstring value.");
		if (other_value.getValue().size() != 1) {
			throw new TtcnError("The octetstring operands of operator or4b must have the same length.");
		}
		char result = (char) (str_val.get_nibble(nibble_pos) | other_value.get_nibble(0));
		return new TitanOctetString( result );
	}

	//originally operator|
	TitanOctetString operatorBitwiseOr(final TitanOctetString_Element other_value) {
		mustBound("Left operand of operator or4b is an unbound octetstring element.");
		other_value.mustBound("Right operand of operator or4b is an unbound octetstring element.");
		char result = (char) (str_val.get_nibble(nibble_pos) | other_value.str_val.get_nibble(other_value.nibble_pos));
		return new TitanOctetString( result );
	}

	//originally operator^
	TitanOctetString operatorBitwiseXor(final TitanOctetString other_value) {
		mustBound("Left operand of operator xor4b is an unbound octetstring element.");
		other_value.mustBound("Right operand of operator xor4b is an unbound octetstring value.");
		if (other_value.getValue().size() != 1) {
			throw new TtcnError("The octetstring operands of operator xor4b must have the same length.");
		}
		char result = (char) (str_val.get_nibble(nibble_pos) ^ other_value.get_nibble(0));
		return new TitanOctetString( result );
	}

	//originally operator^
	TitanOctetString operatorBitwiseXor(final TitanOctetString_Element other_value) {
		mustBound("Left operand of operator xor4b is an unbound octetstring element.");
		other_value.mustBound("Right operand of operator xor4b is an unbound octetstring element.");
		char result = (char) (str_val.get_nibble(nibble_pos) ^ other_value.str_val.get_nibble(other_value.nibble_pos));
		return new TitanOctetString( result );
	}

	char get_nibble() {
		return (char) str_val.get_nibble( nibble_pos );
	}

	void log() {
		if ( bound_flag ) {
			TtcnLogger.log_char('\'');
			TtcnLogger.log_octet(str_val.get_nibble(nibble_pos));
			TtcnLogger.log_event_str("'O");
		}
		else {
			TtcnLogger.log_event_unbound();
		}
	}
}
