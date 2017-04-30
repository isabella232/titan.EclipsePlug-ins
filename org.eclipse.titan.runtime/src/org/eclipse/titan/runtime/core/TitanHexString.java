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
 * TTCN-3 hexstring
 * @author Arpad Lovassy
 */
public class TitanHexString extends Base_Type {

	static final String HEX_DIGITS = "0123456789ABCDEF?*";

	/**
	 * hexstring value.
	 *
	 * Packed storage of hex digits, filled from LSB.
	 */
	private List<Byte> nibbles_ptr;

	public TitanHexString() {
	}

	public TitanHexString( final List<Byte> aOtherValue ) {
		nibbles_ptr = aOtherValue;
	}

	public TitanHexString( final TitanHexString aOtherValue ) {
		aOtherValue.mustBound( "Copying an unbound hexstring value." );
		nibbles_ptr = aOtherValue.nibbles_ptr;
	}

	public TitanHexString( final byte aValue ) {
		nibbles_ptr = new ArrayList<Byte>();
		nibbles_ptr.add( aValue );
	}

	/** Return the nibble at index i
	 *
	 * @param nibble_index
	 * @return
	 */
	public byte get_nibble( final int nibble_index ) {
		return nibbles_ptr.get( nibble_index );
	}

	public void set_nibble( final int nibble_index, final byte new_value ) {
		nibbles_ptr.set( nibble_index, new_value );
	}

	//originally char*()
	public List<Byte> getValue() {
		return nibbles_ptr;
	}

	public void setValue( final List<Byte> aOtherValue ) {
		nibbles_ptr = aOtherValue;
	}

	//originally operator=
	public TitanHexString assign( final TitanHexString aOtherValue ) {
		aOtherValue.mustBound( "Assignment of an unbound hexstring value." );
		nibbles_ptr = aOtherValue.nibbles_ptr;

		return this;
	}

	@Override
	public TitanHexString assign(final Base_Type otherValue) {
		if (otherValue instanceof TitanHexString) {
			return assign((TitanHexString)otherValue);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to hexstring", otherValue));
	}

	public boolean isBound() {
		return nibbles_ptr != null;
	}

	public boolean isValue() {
		return nibbles_ptr != null;
	}

	public void mustBound( final String aErrorMessage ) {
		if ( nibbles_ptr == null ) {
			throw new TtcnError( aErrorMessage );
		}
	}

	//originally operator==
	public boolean operatorEquals( final TitanHexString otherValue ) {
		mustBound("Unbound left operand of hexstring comparison.");
		otherValue.mustBound("Unbound right operand of hexstring comparison.");

		return nibbles_ptr.equals( otherValue.nibbles_ptr );
	}

	@Override
	public boolean operatorEquals(final Base_Type otherValue) {
		if (otherValue instanceof TitanHexString) {
			return operatorEquals((TitanHexString)otherValue);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to hexstring", otherValue));
	}

	//originally operator!=
	public boolean operatorNotEquals( final TitanHexString aOtherValue ) {
		return !operatorEquals( aOtherValue );
	}

	public void cleanUp() {
		nibbles_ptr = null;
	}

	//originally operator[](int)
	TitanHexString_Element getAt(final int index_value) {
		if (nibbles_ptr == null && index_value == 0) {
			nibbles_ptr = new ArrayList<Byte>();
			return new TitanHexString_Element(false, this, 0);
		} else {
			mustBound("Accessing an element of an unbound hexstring value.");
			if (index_value < 0) {
				throw new TtcnError("Accessing an hexstring element using a negative index (" + index_value + ").");
			}

			final int n_nibbles = nibbles_ptr.size();
			if (index_value > n_nibbles) {
				throw new TtcnError("Index overflow when accessing a hexstring element: The index is " + index_value +
						", but the string has only " + n_nibbles + " hexadecimal digits.");
			}
			if (index_value == n_nibbles) {
				return new TitanHexString_Element( false, this, index_value );
			} else {
				return new TitanHexString_Element( true, this, index_value );
			}
		}
	}

	//originally operator[](const INTEGER&)
	TitanHexString_Element getAt(final TitanInteger index_value) {
		index_value.mustBound("Indexing a hexstring value with an unbound integer value.");
		return getAt( index_value.getInt() );
	}

	//originally operator[](int) const
	final TitanHexString_Element constGetAt( final int index_value ) {
		mustBound("Accessing an element of an unbound hexstring value.");
		if (index_value < 0) {
			throw new TtcnError("Accessing an hexstring element using a negative index (" + index_value + ").");
		}
		final int n_nibbles = nibbles_ptr.size();
		if (index_value >= n_nibbles) {
			throw new TtcnError("Index overflow when accessing a hexstring element: The index is " + index_value +
					", but the string has only " + n_nibbles + " hexadecimal digits.");
		}
		return new TitanHexString_Element(true, this, index_value);
	}

	//originally operator[](const INTEGER&) const
	final TitanHexString_Element constGetAt(final TitanInteger index_value) {
		index_value.mustBound("Indexing a hexstring value with an unbound integer value.");
		return constGetAt( index_value.getInt() );
	}

	@Override
	public String toString() {
		if ( nibbles_ptr == null ) {
			return "<unbound>";
		}
		final StringBuilder sb = new StringBuilder();
		final int size = nibbles_ptr.size();
		for ( int i = 0; i < size; i++ ) {
			final Byte digit = nibbles_ptr.get( i );
			sb.append( HEX_DIGITS.charAt( digit ) );
		}
		return sb.toString();
	}

	@Override
	public boolean isPresent() {
		return isBound();
	}
}
