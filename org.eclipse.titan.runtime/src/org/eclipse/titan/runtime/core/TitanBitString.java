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
import java.util.List;

/**
 * TTCN-3 bitstring
 * @author Arpad Lovassy
 * @author Gergo Ujhelyi
 */
public class TitanBitString extends Base_Type {

	/**
	 * bitstring value.
	 *
	 * Packed storage of bits, filled from LSB.
	 */
	private List<Byte> bits_ptr;

	/** number of bits */
	private int n_bits;

	public TitanBitString() {
		bits_ptr = null;
		n_bits = 0;
	}

	public TitanBitString( final List<Byte> aOtherValue, final int aNoBits ) {
		bits_ptr = copyList( aOtherValue );
		n_bits = aNoBits;
		clear_unused_bits();
	}

	public TitanBitString( final TitanBitString aOtherValue ) {
		aOtherValue.mustBound( "Copying an unbound bitstring value." );

		bits_ptr = copyList( aOtherValue.bits_ptr );
		n_bits = aOtherValue.n_bits;
	}

	public TitanBitString( final byte aValue ) {
		bits_ptr = new ArrayList<Byte>();
		bits_ptr.add( aValue );
		n_bits = 8;
	}

	/**
	 * Constructor
	 * @param aValue string representation of a bitstring value, without ''B, it contains only '0' and '1' characters.
	 * NOTE: this is the way bitstring value is stored in Bitstring_Value
	 */
	public TitanBitString( final String aValue ) {
		bits_ptr = bitstr2bytelist( aValue );
		n_bits = aValue.length();
	}

	/**
	 * Converts a string representation of a bitstring to a list of bytes
	 * @param aBitString string representation of bitstring
	 * @return value list of the bitstring, groupped in bytes
	 */
	private static List<Byte> bitstr2bytelist(final String aBitString) {
		final List<Byte> result = new ArrayList<Byte>();
		final int len = aBitString.length();
		for ( int i = 0; i < len; i += 8 ) {
			final String byteStr = aBitString.substring( i, i + 8 < len ? i + 8 : len );
			final byte[] byteArray = byteStr.getBytes();
			final Byte byteValue = bitstr2byte( byteArray );
			result.add( byteValue );
		}

		return result;
	}

	/**
	 * Converts a string representation of a short bitstring (max length 8) to a byte
	 * @param aBitString8 string representation of bitstring as byte array, maximum length is 8, byte values are '0' or '1'
	 * @return value of the bitstring
	 */
	private static byte bitstr2byte(final byte[] aBitString8 ) {
		byte result = 0;
		byte digit = 1;
		for ( int i = 0; i < aBitString8.length ; i++, digit *= 2 ) {
			if ( aBitString8[i] == '1' ) {
				result += digit;
			}
		}
		return result;
	}

	public final List<Byte> copyList( final List<Byte> srcList ) {
		if ( srcList == null ) {
			return null;
		}

		final List<Byte> newList = new ArrayList<Byte>( srcList.size() );
		for (Byte uc : srcList) {
			newList.add( Byte.valueOf( uc ) );
		}
		return newList;
	}

	/**
	 * Sets unused bits to 0
	 */
	private void clear_unused_bits() {
		final int listIndex = (n_bits - 1) / 8;
		byte bytevalue = bits_ptr.get( listIndex );
		if (n_bits % 8 != 0) {
			bytevalue &= 0xFF >> (7 - (n_bits - 1) % 8);
		}
		bits_ptr.set( listIndex, bytevalue );
	}

	/** Return the nibble at index i
	 *
	 * @param aBitIndex
	 * @return bit value ( 0 or 1 )
	 */
	boolean getBit( final int aBitIndex ) {
		return ( bits_ptr.get( aBitIndex / 8 ) & ( 1 << ( aBitIndex % 8 ) ) ) != 0;
	}

	void setBit( final int aBitIndex, final boolean aNewValue ) {
		final int mask = 1 << ( aBitIndex % 8 );
		// the index of the actual byte, where the modification is made
		final int listIndex = aBitIndex / 8;
		byte bytevalue = bits_ptr.get( listIndex );
		if ( aNewValue) {
			bytevalue |= mask;
		} else {
			bytevalue &= ~mask;
		}
		bits_ptr.set( listIndex, bytevalue );
	}

	//originally char*()
	public List<Byte> getValue() {
		mustBound("Casting an unbound bitstring value to const unsigned char*.");

		return bits_ptr;
	}

	//takes ownership of aOtherValue
	//runtime 2 only
//	public void setValue( final List<Byte> aOtherValue, final int aNoBits ) {
//		bits_ptr = aOtherValue;
//		this.n_bits = aNoBits;
//		clear_unused_bits();
//	}
	
	//originally operator=
	public TitanBitString assign(final TitanBitString_Element otherValue){
		otherValue.mustBound("Assignment of an unbound bitstring element to a bitstring.");

		final boolean bitValue = otherValue.get_bit();
		cleanUp();
		n_bits = 1;
		bits_ptr = new ArrayList<Byte>();
		bits_ptr.add(0,(byte)(bitValue ? 1 : 0));

		return this;
	}
	

	//originally operator=
	public TitanBitString assign( final TitanBitString aOtherValue ) {
		aOtherValue.mustBound( "Assignment of an unbound bitstring value." );

		if (aOtherValue != this) {
			cleanUp();
			bits_ptr = copyList( aOtherValue.bits_ptr );
			n_bits = aOtherValue.n_bits;
		}

		return this;
	}

	@Override
	public TitanBitString assign(final Base_Type otherValue) {
		if (otherValue instanceof TitanBitString) {
			return assign((TitanBitString)otherValue);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to bitstring", otherValue));
	}

	public TitanBoolean isBound() {
		return new TitanBoolean(bits_ptr != null);
	}

	public TitanBoolean isValue() {
		return isBound();
	}

	public void mustBound( final String aErrorMessage ) {
		if ( !isBound().getValue() ) {
			throw new TtcnError( aErrorMessage );
		}
	}

	//originally lengthof
	public TitanInteger lengthOf() {
		mustBound("Performing lengthof operation on an unbound bitstring value.");

		return new TitanInteger(n_bits);
	}

	//originally operator==
	public TitanBoolean operatorEquals( final TitanBitString otherValue ) {
		mustBound("Unbound left operand of bitstring comparison.");
		otherValue.mustBound("Unbound right operand of bitstring comparison.");

		return new TitanBoolean(n_bits == otherValue.n_bits && bits_ptr.equals( otherValue.bits_ptr ));
	}

	//originally operator==
	public TitanBoolean operatorEquals(final TitanBitString_Element otherValue){
		mustBound("Unbound left operand of bitstring comparison.");
		otherValue.mustBound("Unbound right operand of bitstring element comparison.");

		if(n_bits != 1){
			return new TitanBoolean(false);
		}

		return new TitanBoolean(getBit(0) == otherValue.get_bit());
	}

	@Override
	public TitanBoolean operatorEquals(final Base_Type otherValue) {
		if (otherValue instanceof TitanBitString) {
			return operatorEquals((TitanBitString)otherValue);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to bitstring", otherValue));
	}

	//originally operator!=
	public TitanBoolean operatorNotEquals( final TitanBitString aOtherValue ) {
		return operatorEquals( aOtherValue ).not();
	}

	//originally operator !=
	public TitanBoolean operatorNotEquals(final TitanBitString_Element aOtherValue){
		return operatorEquals(aOtherValue).not();
	}

	public void cleanUp() {
		n_bits = 0;
		bits_ptr = null;
	}

	//originally operator+
	public TitanBitString concatenate(final TitanBitString aOtherValue) {
		mustBound("Unbound left operand of bitstring concatenation.");
		aOtherValue.mustBound("Unbound right operand of bitstring element concatenation.");

		if (n_bits == 0) {
			return new TitanBitString(aOtherValue);
		}
		if (aOtherValue.n_bits == 0) {
			return new TitanBitString(this);
		}

		// the length of result
		final int resultBits = n_bits + aOtherValue.n_bits;

		// the number of bytes used
		final int left_n_bytes = (n_bits + 7) / 8;
		final int right_n_bytes = (aOtherValue.n_bits + 7) / 8;

		// the number of bits used in the last incomplete octet of the left operand
		final int last_octet_bits = n_bits % 8;

		final ArrayList<Byte> dest_ptr = new ArrayList<Byte>((resultBits + 7) / 8);
		dest_ptr.addAll(bits_ptr);

		if (last_octet_bits != 0) {
			// non-trivial case: the length of left fragment is not a multiply of 8
			// the bytes used in the result
			final int n_bytes = (resultBits + 7) / 8;
			// placing the bytes from the right fragment until the result is filled
			for (int i = left_n_bytes; i < n_bytes; i++) {
				final Byte right_byte = aOtherValue.bits_ptr.get(i - left_n_bytes);
				// finish filling the previous byte
				int temp = dest_ptr.get(i-1) | right_byte << last_octet_bits;
				dest_ptr.set(i-1, new Byte((byte)temp));
				// start filling the actual byte
				temp = right_byte >> (8 - last_octet_bits);
				dest_ptr.add(new Byte((byte)temp));
			}
			if (left_n_bytes + right_n_bytes > n_bytes) {
				// if the result data area is shorter than the two operands together
				// the last bits of right fragment were not placed into the result
				// in the previous for loop
				final int temp = dest_ptr.get(n_bytes-1) | aOtherValue.bits_ptr.get(right_n_bytes - 1) << last_octet_bits;
				dest_ptr.set(n_bytes - 1, new Byte((byte) temp));
			}
		} else {
			dest_ptr.addAll(aOtherValue.bits_ptr);
		}

		return new TitanBitString(dest_ptr, resultBits);
	}
	
	//originally operator+
	public TitanBitString concatenate(final TitanBitString_Element otherValue){
		mustBound("Unbound left operand of bitstring concatenation.");
		otherValue.mustBound("Unbound right operand of bitstring element");

		final TitanBitString ret_val = new TitanBitString(bits_ptr, n_bits+1);
		ret_val.setBit(n_bits, otherValue.get_bit());

		return ret_val;
	}

	//originally operator~
	public TitanBitString not4b(){
		mustBound("Unbound bitstring operand of operator not4b.");

		final int n_bytes = (n_bits + 7) /8;
		if(n_bytes == 0){
			return this;
		}

		final List<Byte> dest_ptr = new ArrayList<Byte>((n_bits + 7) / 8);
		dest_ptr.addAll(bits_ptr);
		for (int i = 0; i < bits_ptr.size(); i++) {
			dest_ptr.set(i, (byte)(~dest_ptr.get(i) & 0x0F));
		}

		final TitanBitString ret_val = new TitanBitString(dest_ptr,n_bits);
		ret_val.clear_unused_bits();

		return ret_val;
	}
	
	//originally operator&
	public TitanBitString and4b(final TitanBitString otherValue){
		mustBound("Left operand of operator and4b is an unbound bitstring value.");
		otherValue.mustBound("Right operand of operator and4b is an unbound bitstring value.");

		if(n_bits != otherValue.n_bits){
			throw new TtcnError("The bitstring operands of operator and4b must have the same length.");
		}
		if(n_bits == 0){
			return this;
		}

		final int n_bytes = (n_bits + 7) /8;
		final List<Byte> dest_ptr = new ArrayList<Byte>(n_bytes);
		dest_ptr.addAll(bits_ptr);
		for (int i = 0; i < bits_ptr.size(); i++) {
			dest_ptr.set(i, (byte)(dest_ptr.get(i)&otherValue.bits_ptr.get(i)));
		}

		final TitanBitString ret_val = new TitanBitString(dest_ptr,n_bits);
		ret_val.clear_unused_bits();

		return ret_val;
	}

	//originally operator&
	public TitanBitString and4b(final TitanBitString_Element otherValue){
		mustBound("Left operand of operator and4b is an unbound bitstring value.");
		otherValue.mustBound("Right operand of operator and4b is an unbound bitstring element.");

		if(n_bits != 1){
			throw new TtcnError("The bitstring operands of operator and4b must have the same length.");
		}
		final List<Byte> result = new ArrayList<Byte>();
		result.add((byte)(getBit(0) && otherValue.get_bit() ? 1 : 0));

		return new TitanBitString(result,1);
	}

	// originally operator|
	public TitanBitString or4b(final TitanBitString otherValue) {
		mustBound("Left operand of operator or4b is an unbound bitstring value.");
		otherValue.mustBound("Right operand of operator or4b is an unbound bitstring value.");

		if (n_bits != otherValue.n_bits) {
			throw new TtcnError("The bitstring operands of operator or4b must have the same length.");
		}
		if (n_bits == 0) {
			return this;
		}
		final int n_bytes = (n_bits + 7) / 8;
		final List<Byte> dest_ptr = new ArrayList<Byte>(n_bytes);
		dest_ptr.addAll(bits_ptr);
		for (int i = 0; i < bits_ptr.size(); i++) {
			dest_ptr.set(i, (byte) (dest_ptr.get(i) | otherValue.bits_ptr.get(i)));
		}

		final TitanBitString ret_val = new TitanBitString(dest_ptr, n_bits);
		ret_val.clear_unused_bits();

		return ret_val;
	}

	// originally operator|
	public TitanBitString or4b(final TitanBitString_Element otherValue) {
		mustBound("Left operand of operator or4b is an unbound bitstring value.");
		otherValue.mustBound("Right operand of operator or4b is an unbound bitstring element.");

		if (n_bits != 1) {
			throw new TtcnError("The bitstring operands of operator or4b must have the same length.");
		}

		final List<Byte> result = new ArrayList<Byte>();
		result.add((byte) (getBit(0) || otherValue.get_bit() ? 1 : 0));

		return new TitanBitString(result, 1);
	}

	// originally operator^
	public TitanBitString xor4b(final TitanBitString otherValue) {
		mustBound("Left operand of operator xor4b is an unbound bitstring value.");
		otherValue.mustBound("Right operand of operator xor4b is an unbound bitstring value.");

		if (n_bits != otherValue.n_bits) {
			throw new TtcnError("The bitstring operands of operator xor4b must have the same length.");
		}
		if (n_bits == 0) {
			return this;
		}

		final int n_bytes = (n_bits + 7) / 8;
		final List<Byte> dest_ptr = new ArrayList<Byte>(n_bytes);
		dest_ptr.addAll(bits_ptr);
		for (int i = 0; i < bits_ptr.size(); i++) {
			dest_ptr.set(i, (byte) (dest_ptr.get(i) ^ otherValue.bits_ptr.get(i)));
		}

		final TitanBitString ret_val = new TitanBitString(dest_ptr, n_bits);
		ret_val.clear_unused_bits();

		return ret_val;
	}

	// originally operator^
	public TitanBitString xor4b(final TitanBitString_Element otherValue) {
		mustBound("Left operand of operator xor4b is an unbound bitstring value.");
		otherValue.mustBound("Right operand of operator xor4b is an unbound bitstring element.");

		if (n_bits != 1) {
			throw new TtcnError("The bitstring operands of operator xor4b must have the same length.");
		}

		final List<Byte> result = new ArrayList<Byte>();
		result.add((byte) (getBit(0) ^ otherValue.get_bit() ? 1 : 0));

		return new TitanBitString(result, 1);
	}

	// originally operator<<
	public TitanBitString shiftLeft(int shiftCount) {
		mustBound("Unbound bitstring operand of shift left operator.");

		if (shiftCount > 0) {
			if (n_bits == 0) {
				return this;
			}
			final int n_bytes = (n_bits + 7) / 8;
			clear_unused_bits();
			if (shiftCount > n_bits) {
				shiftCount = n_bits;
			}
			final int shift_bytes = shiftCount / 8;
			final int shift_bits = shiftCount % 8;
			final List<Byte> result = new ArrayList<Byte>(n_bytes);
			result.addAll(bits_ptr);
			if (shift_bits != 0) {
				int byte_count = 0;
				for( ; byte_count < n_bytes - shift_bytes - 1; byte_count++){
					result.set(byte_count, (byte)((bits_ptr.get(byte_count+shift_bytes) >> shift_bits)|
							(bits_ptr.get(byte_count+shift_bytes+1) <<
									(8-shift_bits))));
				}
				result.set(n_bytes - shift_bytes - 1, (byte) (bits_ptr.get(n_bytes - 1) >> shift_bits));
			} else {
				for (int i = shift_bytes; i < n_bytes; i++) {
					result.set(i - shift_bytes, bits_ptr.get(i));
				}
			}
			for (int i = n_bytes - shift_bytes; i < n_bytes; i++) {
				result.set(i, (byte) 0);
			}

			final TitanBitString ret_val = new TitanBitString(result, n_bits);
			ret_val.clear_unused_bits();
			return ret_val;
		} else if (shiftCount == 0) {
			return this;
		} else {
			return this.shiftRight(-shiftCount);
		}
	}

	//originally operator<<
	public TitanBitString shiftLeft(final TitanInteger otherValue){
		mustBound("Unbound bitstring operand of shift left operator.");

		return shiftLeft(otherValue.getInt());
	}

	// originally operator>>
	public TitanBitString shiftRight(int shiftCount) {
		mustBound("Unbound bitstring operand of shift right operator.");

		if (shiftCount > 0) {
			if (n_bits == 0) {
				return this;
			}
			final int n_bytes = (n_bits + 7) / 8;
			clear_unused_bits();
			if (shiftCount > n_bits) {
				shiftCount = n_bits;
			}

			final int shift_bytes = shiftCount / 8;
			final int shift_bits = shiftCount % 8;
			final List<Byte> result = new ArrayList<Byte>(n_bytes);
			result.addAll(bits_ptr);
			for (int i = 0; i < shift_bytes; i++) {
				result.set(i, (byte) 0);
			}
			if (shift_bits != 0) {
				result.set(shift_bytes, (byte)(bits_ptr.get(0) << shift_bits));
				for(int byte_count = shift_bytes + 1; byte_count < n_bytes; byte_count++){
					result.set(byte_count, (byte)(((bits_ptr.get(byte_count-shift_bytes-1) >>(8-shift_bits)) 
							|(bits_ptr.set(byte_count-shift_bytes, (byte)(bits_ptr.get(byte_count-shift_bytes) << shift_bits )))) ));
				}
			} else {
				for (int i = shift_bytes; i < n_bytes; i++) {
					result.set(i, (byte) (bits_ptr.get(i - shift_bytes)));
				}
			}
			final TitanBitString ret_val = new TitanBitString(result, n_bits);
			ret_val.clear_unused_bits();
			return ret_val;
		} else if (shiftCount == 0) {
			return this;
		} else {
			return this.shiftLeft(-shiftCount);
		}
	}

	// originally operator>>
	public TitanBitString shiftRight(final TitanInteger otherValue) {
		mustBound("Unbound bitstring operand of shift left operator.");
		return shiftRight(otherValue.getInt());
	}

	// originally operator<<=
	public TitanBitString rotateLeft(int rotateCount) {
		mustBound("Unbound bistring operand of rotate left operator.");

		if (n_bits == 0) {
			return this;
		}
		if (rotateCount >= 0) {
			rotateCount %= n_bits;
			if (rotateCount == 0) {
				return this;
			} else {
				return ((this.shiftLeft(rotateCount)).or4b(this.shiftRight(n_bits - rotateCount)));
			}
		} else {
			return this.rotateRight(-rotateCount);
		}
	}

	// originally operator<<=
	public TitanBitString rotateLeft(final TitanInteger rotateCount) {
		mustBound("Unbound bistring operand of rotate left operator.");

		return this.rotateLeft(rotateCount.getInt());
	}

	// originally operator>>=
	public TitanBitString rotateRight(int rotateCount) {
		mustBound("Unbound bistring operand of rotate right operator.");

		if (n_bits == 0) {
			return this;
		}
		if (rotateCount >= 0) {
			rotateCount %= n_bits;
			if (rotateCount == 0) {
				return this;
			} else {
				return ((this.shiftRight(rotateCount)).or4b(this.shiftLeft(n_bits - rotateCount)));
			}
		} else {
			return this.rotateLeft(-rotateCount);
		}
	}

	//originally operator<<=
	public TitanBitString rotateRight(final TitanInteger rotateCount){
		mustBound("Unbound bistring operand of rotate left operator.");

		return this.rotateRight(rotateCount.getInt());
	}

	//originally operator[](int)
	public TitanBitString_Element getAt(final int index_value) {
		if (bits_ptr == null && index_value == 0) {
			bits_ptr = new ArrayList<Byte>();
			n_bits = 1;
			return new TitanBitString_Element(false, this, 0);
		} else {
			mustBound("Accessing an element of an unbound bitstring value.");

			if (index_value < 0) {
				throw new TtcnError("Accessing an bitstring element using a negative index (" + index_value + ").");
			}

			if (index_value > n_bits) {
				throw new TtcnError("Index overflow when accessing a bitstring element: The index is " + index_value +
						", but the string has only " + n_bits + " bits.");
			}
			if (index_value == n_bits) {
				n_bits++;
				return new TitanBitString_Element( false, this, index_value );
			} else {
				return new TitanBitString_Element( true, this, index_value );
			}
		}
	}

	//originally operator[](const INTEGER&)
	public TitanBitString_Element getAt(final TitanInteger index_value) {
		index_value.mustBound("Indexing a bitstring value with an unbound integer value.");

		return getAt( index_value.getInt() );
	}

	//originally operator[](int) const
	public final TitanBitString_Element constGetAt( final int index_value ) {
		mustBound("Accessing an element of an unbound bitstring value.");

		if (index_value < 0) {
			throw new TtcnError("Accessing an bitstring element using a negative index (" + index_value + ").");
		}

		if (index_value >= n_bits) {
			throw new TtcnError("Index overflow when accessing a bitstring element: The index is " + index_value +
					", but the string has only " + n_bits + " bits.");
		}

		return new TitanBitString_Element(true, this, index_value);
	}

	//originally operator[](const INTEGER&) const
	public final TitanBitString_Element constGetAt(final TitanInteger index_value) {
		index_value.mustBound("Indexing a bitstring value with an unbound integer value.");

		return constGetAt( index_value.getInt() );
	}
	
	public void log() {
		if(bits_ptr != null) {
			TtcnLogger.log_char('\'');
			for (int bit_count = 0; bit_count < n_bits; bit_count++) {
				TtcnLogger.log_char(getBit(bit_count) ? '1' : '0');
			}
			TtcnLogger.log_event_str("'B");
		} else {
			TtcnLogger.log_event_unbound();
		}
	}

	@Override
	public TitanBoolean isPresent() {
		return isBound();
	}

	@Override
	public String toString() {
		final StringBuilder result = new StringBuilder(n_bits + 2);
		result.append('\'');
		for (int i = 0; i < n_bits; i++) {
			result.append(getBit(i) ? '1':'0');
		}
		result.append('\'');

		return result.toString();
	}

	
	//TODO: implement BITSTRING::int2bit as static
	//TODO: implement BITSTRING::hex2bit as static
	//TODO: implement BITSTRING::oct2bit as static
	//TODO: implement BITSTRING::str2bit as static
	//TODO: implement BITSTRING::substr as static
	//TODO: implement BITSTRING::replace as static
}
