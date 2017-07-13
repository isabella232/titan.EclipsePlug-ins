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
		str_val.setBit(bit_pos, other_value.str_val.getBit(other_value.bit_pos));
		return this;
	}

	//originally operator=
	public TitanBitString_Element assign( final TitanBitString other_value ) {
		other_value.mustBound("Assignment of unbound bitstring value.");

		if (other_value.lengthOf() != 1) {
			throw new TtcnError( "Assignment of a bitstring value " +
					"with length other than 1 to a bitstring element." );
		}

		bound_flag = true;
		str_val.setBit(bit_pos, other_value.getBit(0));
		return this;
	}

	//originally operator==
	public TitanBoolean operatorEquals( final TitanBitString_Element other_value ) {
		mustBound("Unbound left operand of bitstring element comparison.");
		other_value.mustBound("Unbound right operand of bitstring comparison.");

		return new TitanBoolean(str_val.getBit(bit_pos) == other_value.str_val.getBit( other_value.bit_pos ));
	}

	//originally operator==
	public TitanBoolean operatorEquals( final TitanBitString other_value ) {
		mustBound("Unbound left operand of bitstring element comparison.");
		other_value.mustBound("Unbound right operand of bitstring element comparison.");

		if (other_value.lengthOf() != 1) {
			return new TitanBoolean(false);
		}

		return new TitanBoolean( str_val.getBit(bit_pos) == other_value.getBit(0));
	}

	//originally operator!=
	public TitanBoolean operatorNotEquals(final TitanBitString_Element otherValue){
		return operatorEquals(otherValue).not();
	}

	//originally operator!=
	public TitanBoolean operatorNotEquals(final TitanBitString otherValue){
		return operatorEquals(otherValue).not();
	}

	//originally operator+
	public TitanBitString append( final TitanBitString other_value ) {
		mustBound("Unbound left operand of bitstring element concatenation.");
		other_value.mustBound("Unbound right operand of bitstring concatenation.");

		//FIXME optimize
		final List<Byte> src_ptr = other_value.getValue();
		final int n_nibbles = src_ptr.size();
		final TitanBitString ret_val = new TitanBitString();
		ret_val.setBit(0, str_val.getBit(bit_pos) );
		// chars in the result minus 1
		for (int i = 0; i < n_nibbles; i++) {
			ret_val.setBit( i, other_value.getBit( i ) );
		}
		return ret_val;
	}

	//originally operator+
	public TitanBitString append( final TitanBitString_Element other_value ) {
		mustBound("Unbound left operand of bitstring element concatenation.");
		other_value.mustBound("Unbound right operand of bitstring element concatenation.");

		int result = str_val.getBit(bit_pos) ? 1 : 2;
		if (other_value.get_bit()) {
			result = result | 2;
		}
		ArrayList<Byte> temp_ptr = new ArrayList<Byte>();
		temp_ptr.add((byte)result);
		return new TitanBitString( temp_ptr, 2 );
	}

	//originally operator~
	public TitanBitString operatorNot4b()	{
		mustBound("Unbound bitstring element operand of operator not4b.");

		final byte result = (byte) (str_val.getBit(bit_pos) ? 0 : 1);
		return new TitanBitString( result );
	}

	//originally operator&
	public TitanBitString operatorAnd4b(final TitanBitString other_value) {
		mustBound("Left operand of operator and4b is an unbound bitstring element.");
		other_value.mustBound("Right operand of operator and4b is an unbound bitstring value.");

		if (other_value.lengthOf() != 1) {
			throw new TtcnError("The bitstring operands of operator and4b must have the same length.");
		}

		final boolean temp = str_val.getBit(bit_pos) & other_value.getBit(0);
		final byte result = (byte) (temp ? 1 : 0);
		return new TitanBitString( result );
	}

	//originally operator&
	public TitanBitString operatorAnd4b(final TitanBitString_Element other_value) {
		mustBound("Left operand of operator and4b is an unbound bitstring element.");
		other_value.mustBound("Right operand of operator and4b is an unbound bitstring element.");

		final boolean temp = str_val.getBit(bit_pos) & other_value.get_bit();
		final byte result = (byte) (temp ? 1 : 0);
		return new TitanBitString( result );
	}

	//originally operator|
	public TitanBitString operatorOr4b(final TitanBitString other_value) {
		mustBound("Left operand of operator or4b is an unbound bitstring element.");
		other_value.mustBound("Right operand of operator or4b is an unbound bitstring value.");

		if (other_value.lengthOf() != 1) {
			throw new TtcnError("The bitstring operands of operator or4b must have the same length.");
		}

		final boolean temp = str_val.getBit(bit_pos) | other_value.getBit(0);
		final byte result = (byte) (temp ? 1 : 0);
		return new TitanBitString( result );
	}

	//originally operator|
	public TitanBitString operatorOr4b(final TitanBitString_Element other_value) {
		mustBound("Left operand of operator or4b is an unbound bitstring element.");
		other_value.mustBound("Right operand of operator or4b is an unbound bitstring element.");

		final boolean temp = str_val.getBit(bit_pos) | other_value.get_bit();
		final byte result = (byte) (temp ? 1 : 0);
		return new TitanBitString( result );
	}

	//originally operator^
	public TitanBitString operatorXor4b(final TitanBitString other_value) {
		mustBound("Left operand of operator xor4b is an unbound bitstring element.");
		other_value.mustBound("Right operand of operator xor4b is an unbound bitstring value.");

		if (other_value.lengthOf() != 1) {
			throw new TtcnError("The bitstring operands of operator xor4b must have the same length.");
		}

		final boolean temp = str_val.getBit(bit_pos) ^ other_value.getBit(0);
		final byte result = (byte) (temp ? 1 : 0);
		return new TitanBitString( result );
	}

	//originally operator^
	public TitanBitString operatorXor4b(final TitanBitString_Element other_value) {
		mustBound("Left operand of operator xor4b is an unbound bitstring element.");
		other_value.mustBound("Right operand of operator xor4b is an unbound bitstring element.");

		final boolean temp = str_val.getBit(bit_pos) ^ other_value.get_bit();
		final byte result = (byte) (temp ? 1 : 0);
		return new TitanBitString( result );
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
		StringBuilder result = new StringBuilder();
		result.append('\'');
		result.append(str_val.getBit(bit_pos) ? '1' : '0');
		result.append('\'');

		return result.toString();
	}
}
