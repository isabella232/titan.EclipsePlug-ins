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
 * @author Farkas Izabella Ingrid
 */
public class TitanOctetString extends Base_Type {

	// originally octetstring_value_match
	private static final Pattern OCTETSTRING_VALUE_PATTERN = Pattern.compile( "^(([0-9A-Fa-f]{2})+).*$" );

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

	/**
	 * Constructor
	 * @param aValue string representation of a octetstring value, without ''B, it contains only [0-9A-F] characters.
	 * NOTE: this is the way octetstring value is stored in Octetstring_Value
	 */
	public TitanOctetString( final String aValue ) {
		val_ptr = octetstr2bytelist( aValue );
	}

	/**
	 * Converts a string representation of a octetstring to a list of Character
	 * @param aHexString string representation of octetstring, it contains exatcly even number of hex digits
	 * @return value list of the octetstring, groupped in 2 bytes (java Character)
	 */
	private static List<Character> octetstr2bytelist(final String aHexString) {
		final List<Character> result = new ArrayList<Character>();
		final int len = aHexString.length();
		for ( int i = 0; i < len; i += 2 ) {
			final char hexDigit1 = aHexString.charAt( i );
			final char hexDigit2 = aHexString.charAt( i + 1 );
			final char value = octet2value( hexDigit1, hexDigit2 );
			result.add( value );
		}

		return result;
	}

	/**
	 * Converts a string representation of an octet to a value
	 * @param aHexDigit1 1st digit of an octet, string representation of hex digit, possible value: [0-9A-F] characters
	 * @param aHexDigit2 2nd digit of an octet, string representation of hex digit, possible value: [0-9A-F] characters
	 * @return value of the octet
	 */
	private static char octet2value( final char aHexDigit1, final char aHexDigit2 ) {
		final char result = (char) ( 16 * TitanHexString.hexdigit2byte( aHexDigit1 ) + TitanHexString.hexdigit2byte( aHexDigit2 ));
		return result;
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

		if (aOtherValue != this) {
			val_ptr = aOtherValue.val_ptr;
		}

		return this;
	}

	public TitanOctetString assign( final TitanOctetString_Element aOtherValue ) {
		aOtherValue.mustBound( "Assignment of an unbound octetstring element to an octetstring." );
		val_ptr = new ArrayList<Character>();
		val_ptr.add(aOtherValue.get_nibble());

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

	// originally lengthof
	public int lengthOf() {
		mustBound("Performing lengthof operation on an unbound octetstring value.");

		return val_ptr.size();
	}

	// originally operator==
	public TitanBoolean operatorEquals( final TitanOctetString otherValue ) {
		mustBound("Unbound left operand of octetstring comparison.");
		otherValue.mustBound("Unbound right operand of octetstring comparison.");

		return new TitanBoolean(val_ptr.equals( otherValue.val_ptr ));
	}

	public TitanBoolean operatorEquals( final TitanOctetString_Element otherValue ) {
		mustBound("Unbound left operand of octetstring comparison.");
		otherValue.mustBound("Unbound right operand of octetstring comparison.");

		return otherValue.operatorEquals(this);
		//new TitanBoolean(val_ptr.equals( otherValue.get_nibble()));
	}

	@Override
	public TitanBoolean operatorEquals(final Base_Type otherValue) {
		if (otherValue instanceof TitanOctetString) {
			return operatorEquals((TitanOctetString)otherValue);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to octetstring", otherValue));
	}

	//originally operator!=
	public TitanBoolean operatorNotEquals( final TitanOctetString aOtherValue ) {
		return operatorEquals( aOtherValue ).not();
	}

	public TitanBoolean operatorNotEquals( final TitanOctetString_Element aOtherValue ) {
		return operatorEquals( aOtherValue ).not();
	}

	public TitanBoolean operatorNotEquals( final Base_Type aOtherValue ) {
		return operatorEquals( aOtherValue ).not();
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
				val_ptr.add('e');
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
		sb.append('\'');
		final int size = val_ptr.size();
		for ( int i = 0; i < size; i++ ) {
			final int digit = val_ptr.get( i ).charValue();
			sb.append( HEX_DIGITS.charAt( digit / 16 ) );
			sb.append( HEX_DIGITS.charAt( digit % 16 ) );
		}
		sb.append('\'');
		return sb.toString();
	}

	@Override
	public boolean isPresent() {
		return isBound();
	}

	/**
	 * this + otherValue (concatenation)
	 * originally operator+
	 */
	public TitanOctetString add( final TitanOctetString otherValue ) {
		mustBound( "Unbound left operand of octetstring concatenation." );
		otherValue.mustBound( "Unbound right operand of octetstring concatenation." );
		TitanOctetString result = new TitanOctetString( val_ptr );
		result.val_ptr.addAll( copyList( otherValue.val_ptr ) );
		return result;
	}

	public TitanOctetString add( final TitanOctetString_Element otherValue ) {
		mustBound( "Unbound left operand of octetstring concatenation." );
		otherValue.mustBound( "Unbound right operand of octetstring element concatenation." );

		TitanOctetString result = new TitanOctetString( val_ptr );
		result.val_ptr.add( new Character( otherValue.get_nibble()) );

		return result;
	}

	// originally operator~
	public TitanOctetString not4b() {
		mustBound("Unbound octetstring operand of operator not4b.");

		TitanOctetString result = new TitanOctetString();
		result.val_ptr = new ArrayList<Character>();
		for (int i = 0; i < val_ptr.size(); i++) {
			final int digit1 = val_ptr.get(i) / 16;
			final int digit2 = val_ptr.get(i) % 16;
			final int negDigit1 = ~digit1 & 0x0F;
			final int negDigit2 = ~digit2 & 0x0F;
			result.val_ptr.add((char)((negDigit1  << 4) + negDigit2));
		}

		return result;
	}

	// originally operator&
	public TitanOctetString and4b(final TitanOctetString otherValue) {
		mustBound("Left operand of operator and4b is an unbound octetstring value.");
		otherValue.mustBound("Right operand of operator and4b is an unbound octetstring value.");

		if (val_ptr.size() != otherValue.val_ptr.size()) {
			throw new TtcnError("The octetstring operands of operator and4b must have the same length.");
		}

		TitanOctetString result = new TitanOctetString();
		result.val_ptr = new ArrayList<Character>();

		for (int i = 0; i < val_ptr.size(); i++) {
			result.val_ptr.add((char) (val_ptr.get(i) & otherValue.val_ptr.get(i)));
		}

		return result;
	}

	// originally operator&
	public TitanOctetString and4b(final TitanOctetString_Element otherValue) {
		mustBound("Left operand of operator and4b is an unbound octetstring value.");
		otherValue.mustBound("Right operand of operator and4b is an unbound octetstring value.");

		if (val_ptr.size() != 1) {
			throw new TtcnError("The octetstring operands of operator and4b must have the same length.");
		}

		return new TitanOctetString((char)(val_ptr.get(0) & otherValue.get_nibble()));
	}

	// originally operator|
	public TitanOctetString or4b(final TitanOctetString otherValue) {
		mustBound("Left operand of operator or4b is an unbound octetstring value.");
		otherValue.mustBound("Right operand of operator or4b is an unbound octetstring value.");

		if (val_ptr.size() != otherValue.val_ptr.size()) {
			throw new TtcnError("The octetstring operands of operator or4b must have the same length.");
		}

		TitanOctetString result = new TitanOctetString();
		result.val_ptr = new ArrayList<Character>();
		for (int i = 0; i < val_ptr.size(); i++) {
			result.val_ptr.add((char)(val_ptr.get(i) | otherValue.val_ptr.get(i)));
		}

		return result;

	}

	// originally operator|
	public TitanOctetString or4b(final TitanOctetString_Element otherValue) {
		mustBound("Left operand of operator or4b is an unbound octetstring value.");
		otherValue.mustBound("Right operand of operator or4b is an unbound octetstring value.");

		if (lengthOf() != 1) {
			throw new TtcnError("The octetstring operands of operator or4b must have the same length.");
		}

		return new TitanOctetString((char)(val_ptr.get(0) | otherValue.get_nibble()));
	}

	//originally operator^
	public TitanOctetString xor4b(final TitanOctetString otherValue) {
		mustBound("Left operand of operator xor4b is an unbound octetstring value.");
		otherValue.mustBound("Right operand of operator xor4b is an unbound octetstring value.");

		if (val_ptr.size() != otherValue.val_ptr.size()) {
			throw new TtcnError("The octetstring operands of operator xor4b must have the same length.");
		}

		TitanOctetString result = new TitanOctetString();
		result.val_ptr = new ArrayList<Character>();
		for (int i = 0; i < val_ptr.size(); i++) {
			result.val_ptr.add((char)(val_ptr.get(i) ^ otherValue.val_ptr.get(i)));
		}

		return result;
	}

	//originally operator^
	public TitanOctetString xor4b(final TitanOctetString_Element otherValue) {
		mustBound("Left operand of operator xor4b is an unbound octetstring value.");
		otherValue.mustBound("Right operand of operator xor4b is an unbound octetstring element.");

		if (val_ptr.size() != 1) {
			throw new TtcnError("The octetstring operands of operator xor4b must have the same length.");
		}

		return new TitanOctetString((char)(val_ptr.get(0) ^ otherValue.get_nibble()));
	}

	//originally operator<<
	public TitanOctetString shiftLeft(int shiftCount) {
		mustBound("Unbound octetstring operand of shift left operator.");
		
		if (shiftCount > 0) {
			if (val_ptr.size() == 0) return this;

			TitanOctetString result = new TitanOctetString();
			result.val_ptr = new ArrayList<Character>();
			if (shiftCount > val_ptr.size()) shiftCount = val_ptr.size();
		
			for (int i = 0; i < val_ptr.size() - shiftCount; i++) {
				result.val_ptr.add(i, val_ptr.get(i+shiftCount));
			}
		
			for (int i = val_ptr.size() - shiftCount; i < val_ptr.size(); i++) {
				result.val_ptr.add(i, (char) 0);
			}
			return result;
		} else {
			if (shiftCount == 0) return this;
			else return this.shiftRight(-shiftCount);
		}
	}

	public TitanOctetString shiftLeft(final TitanInteger otherValue) {
		mustBound("Unbound right operand of octetstring shift left operator.");

		return shiftLeft(otherValue.getInt());
	}

	// originally operator>>
	public TitanOctetString shiftRight(int shiftCount) {
		mustBound("Unbound octetstring operand of shift right operator.");

		if (shiftCount > 0) {
			if (val_ptr.size() == 0) return this;

			TitanOctetString result = new TitanOctetString();
			result.val_ptr =  new ArrayList<Character>();
			if (shiftCount > val_ptr.size()) shiftCount = val_ptr.size();
			for (int i = 0; i < shiftCount; i++) {
				result.val_ptr.add(i, (char) 0);
			}
			for (int i = shiftCount; i < val_ptr.size(); i++) {
				result.val_ptr.add(i, val_ptr.get(i-shiftCount));
			}
			return result;
		} else {
			if (shiftCount == 0) return this;
			else return this.shiftLeft(-shiftCount);
		}
	}

	public TitanOctetString shiftRight(final TitanInteger otherValue){
		mustBound("Unbound right operand of octetstring shift right operator.");
		
		return shiftRight(otherValue.getInt());
	}
	
	// originally operator<<=
	public TitanOctetString rotateLeft(int rotateCount) {
		mustBound("Unbound octetstring operand of rotate left operator.");
		
		if (val_ptr.size() == 0) return this;
		if (rotateCount >= 0) {
			rotateCount = rotateCount % val_ptr.size();
			if (rotateCount == 0) return this;
			
			TitanOctetString result = new TitanOctetString();
			result.val_ptr =  new ArrayList<Character>();
			for (int i = 0; i < val_ptr.size() - rotateCount; i++) {
				result.val_ptr.add(i, val_ptr.get(i+rotateCount));
			}
			for (int i = val_ptr.size() - rotateCount; i < val_ptr.size(); i++) {
				result.val_ptr.add(i, val_ptr.get(i+rotateCount-val_ptr.size()));
			}

			return result;
		} else {
			return rotateRight(-rotateCount);
		}
	}
	
	public TitanOctetString rotateLeft(final TitanInteger rotateCount) {
		rotateCount.mustBound("Unbound right operand of octetstring rotate left operator.");
		
		return rotateLeft(rotateCount.getInt());
	}
	
	// originally operator>>=
	public TitanOctetString rotateRight(int rotateCount) {
		mustBound("Unbound octetstring operand of rotate right operator.");
		
		if (val_ptr.size() == 0) return this;
		if (rotateCount >= 0) {
			rotateCount = rotateCount % val_ptr.size();
			if (rotateCount == 0) return this;
			TitanOctetString result = new TitanOctetString();
			result.val_ptr =  new ArrayList<Character>();
			if (rotateCount > val_ptr.size()) rotateCount = val_ptr.size();
			for (int i = 0; i < rotateCount; i++) {
				result.val_ptr.add(i, val_ptr.get(i-rotateCount+val_ptr.size()));
			}
			for (int i = rotateCount; i < val_ptr.size(); i++) {
				result.val_ptr.add(i, val_ptr.get(i-rotateCount));
			}
			return result;
		} else {
			return rotateLeft(-rotateCount);
		}
	}
	
	public TitanOctetString rotateRight(final TitanInteger rotateCount) {
		rotateCount.mustBound("Unbound right operand of octetstring rotate left operator.");
		
		return rotateRight(rotateCount.getInt());
	}
}
