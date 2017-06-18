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
		bits_ptr = new ArrayList<Byte>();
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
		for ( int i = aBitString8.length - 1; i >= 0 ; i--, digit *= 2 ) {
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
	public byte getBit( final int aBitIndex ) {
		return (byte) ( bits_ptr.get( aBitIndex / 8 ) & ( 1 << ( aBitIndex % 8 ) ) );
	}

	public void setBit( final int aBitIndex, final byte aNewValue ) {
		final int mask = 1 << ( aBitIndex % 8 );
		// the index of the actual byte, where the modification is made
		final int listIndex = aBitIndex / 8;
		byte bytevalue = bits_ptr.get( listIndex );
		if ( aNewValue != 0 ) {
			bytevalue |= mask;
		} else {
			bytevalue &= ~mask;
		}
		bits_ptr.set( listIndex, bytevalue );
	}

	//originally char*()
	public List<Byte> getValue() {
		return bits_ptr;
	}

	//takes ownership of aOtherValue
	public void setValue( final List<Byte> aOtherValue, final int aNoBits ) {
		bits_ptr = aOtherValue;
		this.n_bits = aNoBits;
		clear_unused_bits();
	}

	//originally operator=
	public TitanBitString assign( final TitanBitString aOtherValue ) {
		aOtherValue.mustBound( "Assignment of an unbound bitstring value." );

		bits_ptr = copyList( aOtherValue.bits_ptr );
		n_bits = aOtherValue.n_bits;
		return this;
	}

	@Override
	public TitanBitString assign(final Base_Type otherValue) {
		if (otherValue instanceof TitanBitString) {
			return assign((TitanBitString)otherValue);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to bitstring", otherValue));
	}

	public boolean isBound() {
		return bits_ptr != null;
	}

	public boolean isValue() {
		return isBound();
	}

	public void mustBound( final String aErrorMessage ) {
		if ( !isBound() ) {
			throw new TtcnError( aErrorMessage );
		}
	}

	//originally lengthof
	public int lengthOf() {
		mustBound("Performing lengthof operation on an unbound bitstring value.");

		return n_bits;
	}

	//originally operator==
	public TitanBoolean operatorEquals( final TitanBitString otherValue ) {
		mustBound("Unbound left operand of bitstring comparison.");
		otherValue.mustBound("Unbound right operand of bitstring comparison.");

		return new TitanBoolean(n_bits == otherValue.n_bits && bits_ptr.equals( otherValue.bits_ptr ));
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

	public void cleanUp() {
		n_bits = 0;
		bits_ptr = null;
	}

	//originally operator[](int)
	public TitanBitString_Element getAt(final int index_value) {
		if (bits_ptr == null && index_value == 0) {
			bits_ptr = new ArrayList<Byte>();
			return new TitanBitString_Element(false, this, 0);
		} else {
			mustBound("Accessing an element of an unbound bitstring value.");

			if (index_value < 0) {
				throw new TtcnError("Accessing an bitstring element using a negative index (" + index_value + ").");
			}

			final int n_nibbles = bits_ptr.size();
			if (index_value > n_nibbles) {
				throw new TtcnError("Index overflow when accessing a bitstring element: The index is " + index_value +
						", but the string has only " + n_nibbles + " hexadecimal digits.");
			}
			if (index_value == n_nibbles) {
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

		final int n_nibbles = bits_ptr.size();
		if (index_value >= n_nibbles) {
			throw new TtcnError("Index overflow when accessing a bitstring element: The index is " + index_value +
					", but the string has only " + n_nibbles + " hexadecimal digits.");
		}

		return new TitanBitString_Element(true, this, index_value);
	}

	//originally operator[](const INTEGER&) const
	public final TitanBitString_Element constGetAt(final TitanInteger index_value) {
		index_value.mustBound("Indexing a bitstring value with an unbound integer value.");

		return constGetAt( index_value.getInt() );
	}

	@Override
	public boolean isPresent() {
		return isBound();
	}
}
