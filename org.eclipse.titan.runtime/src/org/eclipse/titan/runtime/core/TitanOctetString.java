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
import java.util.regex.Pattern;

/**
 * TTCN-3 octetstring
 * @author Arpad Lovassy
 */
public class TitanOctetString extends Base_Type {

	// originally octetstring_value_match
	private Pattern OCTETSTRING_VALUE_PATTERN = Pattern.compile( "^(([0-9A-Fa-f]{2})+).*$" );

	private static final String HEX_DIGITS = "0123456789ABCDEF";

	/**
	 * octetstring value.
	 *
	 * Packed storage of hex digit pairs, filled from LSB.
	 */
	private List<Character> val_ptr;

	public TitanOctetString() {
	}

	public TitanOctetString( final List<Character> aOtherValue ) {
		val_ptr = copyList( aOtherValue );
	}

	public TitanOctetString( final TitanOctetString aOtherValue ) {
		aOtherValue.mustBound( "Copying an unbound octetstring value." );
		val_ptr = copyList( aOtherValue.val_ptr );
	}

	public TitanOctetString( final char aValue ) {
		val_ptr = new ArrayList<Character>();
		val_ptr.add( aValue );
	}

	private static List<Character> copyList(final List<Character> aList) {
		if ( aList == null ) {
			return null;
		}
		final List<Character> clonedList = new ArrayList<Character>( aList.size() );
		for (Character uc : aList) {
			clonedList.add( new Character( uc ) );
		}
		return clonedList;
	}

	/** Return the nibble at index i
	 *
	 * @param nibble_index
	 * @return
	 */
	public char get_nibble( final int nibble_index ) {
		return val_ptr.get( nibble_index );
	}

	public void set_nibble( final int nibble_index, final char new_value ) {
		val_ptr.set( nibble_index, new_value );
	}

	//originally char*()
	public List<Character> getValue() {
		return val_ptr;
	}

	//takes ownership of aOtherValue
	public void setValue( final List<Character> aOtherValue ) {
		val_ptr = aOtherValue;
	}

	//originally operator=
	public TitanOctetString assign( final TitanOctetString aOtherValue ) {
		aOtherValue.mustBound( "Assignment of an unbound octetstring value." );
		val_ptr = aOtherValue.val_ptr;

		return this;
	}

	@Override
	public TitanOctetString assign(final Base_Type otherValue) {
		if (otherValue instanceof TitanOctetString) {
			return assign((TitanOctetString)otherValue);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to octetstring", otherValue));
	}

	public boolean isBound() {
		return val_ptr != null;
	}

	public boolean isValue() {
		return val_ptr != null;
	}

	public void mustBound( final String aErrorMessage ) {
		if ( val_ptr == null ) {
			throw new TtcnError( aErrorMessage );
		}
	}

	//originally operator==
	public boolean operatorEquals( final TitanOctetString otherValue ) {
		mustBound("Unbound left operand of octetstring comparison.");
		otherValue.mustBound("Unbound right operand of octetstring comparison.");

		return val_ptr.equals( otherValue.val_ptr );
	}

	@Override
	public boolean operatorEquals(final Base_Type otherValue) {
		if (otherValue instanceof TitanOctetString) {
			return operatorEquals((TitanOctetString)otherValue);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to octetstring", otherValue));
	}

	//originally operator!=
	public boolean operatorNotEquals( final TitanOctetString aOtherValue ) {
		return !operatorEquals( aOtherValue );
	}

	public void cleanUp() {
		val_ptr = null;
	}

	//originally operator[](int)
	public TitanOctetString_Element getAt(final int index_value) {
		if (val_ptr == null && index_value == 0) {
			val_ptr = new ArrayList<Character>();
			return new TitanOctetString_Element(false, this, 0);
		} else {
			mustBound("Accessing an element of an unbound octetstring value.");
			if (index_value < 0) {
				throw new TtcnError("Accessing an octetstring element using a negative index (" + index_value + ").");
			}

			final int n_nibbles = val_ptr.size();
			if (index_value > n_nibbles) {
				throw new TtcnError("Index overflow when accessing a octetstring element: The index is " + index_value +
						", but the string has only " + n_nibbles + " hexadecimal digits.");
			}
			if (index_value == n_nibbles) {
				return new TitanOctetString_Element( false, this, index_value );
			} else {
				return new TitanOctetString_Element( true, this, index_value );
			}
		}
	}

	//originally operator[](const INTEGER&)
	public TitanOctetString_Element getAt(final TitanInteger index_value) {
		index_value.mustBound("Indexing a octetstring value with an unbound integer value.");
		return getAt( index_value.getInt() );
	}

	//originally operator[](int) const
	public final TitanOctetString_Element constGetAt( final int index_value ) {
		mustBound("Accessing an element of an unbound octetstring value.");
		if (index_value < 0) {
			throw new TtcnError("Accessing an octetstring element using a negative index (" + index_value + ").");
		}
		final int n_nibbles = val_ptr.size();
		if (index_value >= n_nibbles) {
			throw new TtcnError("Index overflow when accessing a octetstring element: The index is " + index_value +
					", but the string has only " + n_nibbles + " hexadecimal digits.");
		}
		return new TitanOctetString_Element(true, this, index_value);
	}

	//originally operator[](const INTEGER&) const
	public final TitanOctetString_Element constGetAt(final TitanInteger index_value) {
		index_value.mustBound("Indexing a octetstring value with an unbound integer value.");
		return constGetAt( index_value.getInt() );
	}

	@Override
	public String toString() {
		if ( val_ptr == null ) {
			return "<unbound>";
		}
		final StringBuilder sb = new StringBuilder();
		final int size = val_ptr.size();
		for ( int i = 0; i < size; i++ ) {
			final Character digit = val_ptr.get( i );
			if ( digit == 256 ) {
				sb.append( '?' );
			} else if ( digit == 257 ) {
				sb.append( '*' );
			} else {
				sb.append( HEX_DIGITS.charAt( digit >> 8 ) );
				sb.append( HEX_DIGITS.charAt( digit & 0xFF ) );
			}
		}
		return sb.toString();
	}

	@Override
	public boolean isPresent() {
		return isBound();
	}
}
