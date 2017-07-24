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
 *
 * @author Arpad Lovassy
 * @author Gergo Ujhelyi
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

	public TitanHexString(final List<Byte> aOtherValue) {
		nibbles_ptr = copyList(aOtherValue);
	}

	public TitanHexString(final TitanHexString aOtherValue) {
		aOtherValue.mustBound("Copying an unbound hexstring value.");

		nibbles_ptr = copyList(aOtherValue.nibbles_ptr);
	}

	public TitanHexString(final TitanHexString_Element otherValue) {
		otherValue.mustBound("Initialization from an unbound hexstring element.");

		nibbles_ptr = new ArrayList<Byte>(1);
		nibbles_ptr.add((byte) otherValue.get_nibble());
	}

	public TitanHexString(final byte aValue) {
		nibbles_ptr = new ArrayList<Byte>();
		nibbles_ptr.add(aValue);
	}

	/**
	 * Constructor
	 * @param aValue string representation of a hexstring value, without ''B, it contains only [0-9A-F] characters.
	 * NOTE: this is the way hexstring value is stored in Hexstring_Value
	 */
	public TitanHexString(final String aValue) {
		nibbles_ptr = hexstr2bytelist(aValue);
	}

	public final List<Byte> copyList(final List<Byte> srcList) {
		if (srcList == null) {
			return null;
		}

		final List<Byte> newList = new ArrayList<Byte>(srcList.size());
		for (Byte uc : srcList) {
			newList.add(Byte.valueOf(uc));
		}
		return newList;
	}

	private void clearUnusedNibble() {
		if (nibbles_ptr.size() % 2 == 1) {
			nibbles_ptr.set(lengthOf().getInt() / 2, (byte) (nibbles_ptr.get(lengthOf().getInt() / 2) & 0x0F));
		}
	}

	/**
	 * Converts a string representation of a hexstring to a list of bytes
	 * @param aHexString string representation of hexstring
	 * @return value list of the hexstring, groupped in bytes
	 */
	private static List<Byte> hexstr2bytelist(final String aHexString) {
		final List<Byte> result = new ArrayList<Byte>();
		final int len = aHexString.length();
		for (int i = 0; i < len; i++) {
			final char hexDigit = aHexString.charAt(i);
			final Byte byteValue = hexdigit2byte(hexDigit);
			result.add(byteValue);
		}

		return result;
	}

	/**
	 * Converts a string representation of a hexadecimal digit to a byte
	 * @param aHexDigit string representation of hex digit, possible value: [0-9A-F] characters
	 * @return value of the hex digit
	 */
	static byte hexdigit2byte(final char aHexDigit) {
		byte result;
		if ('0' <= aHexDigit && aHexDigit <= '9') {
			result = (byte) (aHexDigit - '0');
		} else if ('A' <= aHexDigit && aHexDigit <= 'F') {
			result = (byte) (aHexDigit - 'A' + 10);
		} else if ('a' <= aHexDigit && aHexDigit <= 'f') {
			result = (byte) (aHexDigit - 'a' + 10);
		} else {
			// TODO: handle error
			result = 0;
		}
		return result;
	}

	/** Return the nibble at index i
	 *
	 * @param nibble_index
	 * @return
	 */
	public byte get_nibble(final int nibble_index) {
		return nibbles_ptr.get(nibble_index);
	}

	public void set_nibble(final int nibble_index, final byte new_value) {
		nibbles_ptr.set(nibble_index, new_value);
	}

	// originally char*()
	public List<Byte> getValue() {
		return nibbles_ptr;
	}

	public void setValue(final List<Byte> aOtherValue) {
		nibbles_ptr = aOtherValue;
	}

	// originally operator=
	public TitanHexString assign(final TitanHexString aOtherValue) {
		aOtherValue.mustBound("Assignment of an unbound hexstring value.");

		if (aOtherValue != this) {
			nibbles_ptr = aOtherValue.nibbles_ptr;
		}

		return this;
	}

	// originally operator=
	public TitanHexString assign(final TitanHexString_Element otherValue) {
		otherValue.mustBound("Assignment of an unbound hexstring element to a hexstring.");

		cleanUp();
		nibbles_ptr = new ArrayList<Byte>(1);
		nibbles_ptr.add((byte) (otherValue.get_nibble()));

		return this;
	}

	@Override
	public TitanHexString assign(final Base_Type otherValue) {
		if (otherValue instanceof TitanHexString) {
			return assign((TitanHexString) otherValue);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to hexstring", otherValue));
	}

	public boolean isBound() {
		return nibbles_ptr != null;
	}

	public boolean isValue() {
		return nibbles_ptr != null;
	}

	public void mustBound(final String aErrorMessage) {
		if (nibbles_ptr == null) {
			throw new TtcnError(aErrorMessage);
		}
	}

	// originally lengthof
	public TitanInteger lengthOf() {
		mustBound("Performing lengthof operation on an unbound charstring value.");

		return new TitanInteger(nibbles_ptr.size());
	}

	// originally operator==
	public TitanBoolean operatorEquals(final TitanHexString otherValue) {
		mustBound("Unbound left operand of hexstring comparison.");
		otherValue.mustBound("Unbound right operand of hexstring comparison.");

		return new TitanBoolean(nibbles_ptr.equals(otherValue.nibbles_ptr));
	}

	// originally operator==
	public TitanBoolean operatorEquals(final TitanHexString_Element otherValue) {
		mustBound("Unbound left operand of hexstring comparison.");
		otherValue.mustBound("Unbound right operand of hexstring element comparison.");

		if (nibbles_ptr.size() != 1) {
			return new TitanBoolean(false);
		}

		return new TitanBoolean(get_nibble(0) == otherValue.get_nibble());
	}

	@Override
	public TitanBoolean operatorEquals(final Base_Type otherValue) {
		if (otherValue instanceof TitanHexString) {
			return operatorEquals((TitanHexString) otherValue);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to hexstring", otherValue));
	}

	// originally operator!=
	public TitanBoolean operatorNotEquals(final TitanHexString aOtherValue) {
		return operatorEquals(aOtherValue).not();
	}

	// originally operator!=
	public TitanBoolean operatorNotEquals(final TitanHexString_Element otherValue) {
		return operatorEquals(otherValue).not();
	}

	public void cleanUp() {
		nibbles_ptr = null;
	}

	// originally operator[](int)
	public TitanHexString_Element getAt(final int index_value) {
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
				return new TitanHexString_Element(false, this, index_value);
			} else {
				return new TitanHexString_Element(true, this, index_value);
			}
		}
	}

	// originally operator[](const INTEGER&)
	public TitanHexString_Element getAt(final TitanInteger index_value) {
		index_value.mustBound("Indexing a hexstring value with an unbound integer value.");

		return getAt(index_value.getInt());
	}

	// originally operator[](int) const
	public final TitanHexString_Element constGetAt(final int index_value) {
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

	// originally operator[](const INTEGER&) const
	public final TitanHexString_Element constGetAt(final TitanInteger index_value) {
		index_value.mustBound("Indexing a hexstring value with an unbound integer value.");

		return constGetAt(index_value.getInt());
	}

	@Override
	public String toString() {
		if (nibbles_ptr == null) {
			return "<unbound>";
		}

		final StringBuilder sb = new StringBuilder();
		final int size = nibbles_ptr.size();
		for (int i = 0; i < size; i++) {
			final Byte digit = nibbles_ptr.get(i);
			sb.append(HEX_DIGITS.charAt(digit));
		}
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
	public TitanHexString concatenate(final TitanHexString otherValue) {
		mustBound("Unbound left operand of hexstring concatenation.");
		otherValue.mustBound("Unbound right operand of hexstring concatenation.");

		TitanHexString result = new TitanHexString(nibbles_ptr);
		result.nibbles_ptr.addAll(copyList(otherValue.nibbles_ptr));

		return result;
	}

	// originally operator+
	public TitanHexString concatenate(final TitanHexString_Element otherValue) {
		mustBound("Unbound left operand of hexstring concatenation.");
		otherValue.mustBound("Unbound right operand of hexstring element concatenation.");

		TitanHexString result = new TitanHexString(nibbles_ptr);
		result.nibbles_ptr.add((byte) otherValue.get_nibble());

		return result;
	}

	// originally operator~
	public TitanHexString not4b() {
		mustBound("Unbound hexstring operand of operator not4b.");

		int n_bytes = (nibbles_ptr.size() + 1) / 2;
		if (n_bytes == 0) {
			return this;
		}
		List<Byte> result = new ArrayList<Byte>();
		result.addAll(nibbles_ptr);
		for (int i = 0; i < nibbles_ptr.size(); i++) {
			result.set(i, (byte)((~nibbles_ptr.get(i) & 0x0F)));
		}
		TitanHexString ret_val = new TitanHexString(result);
		ret_val.clearUnusedNibble();

		return ret_val;
	}

	// originally operator&
	public TitanHexString and4b(final TitanHexString otherValue) {
		mustBound("Left operand of operator and4b is an unbound hexstring value.");
		otherValue.mustBound("Right operand of operator and4b is an unbound hexstring value.");

		if (lengthOf() != otherValue.lengthOf()) {
			throw new TtcnError("The hexstring operands of operator and4b must have the same length.");
		}
		if (lengthOf().getInt() == 0) {
			return this;
		}
		int n_bytes = (lengthOf().getInt() + 1) / 2;
		List<Byte> result = new ArrayList<Byte>(n_bytes);
		for (int i = 0; i < nibbles_ptr.size(); i++) {
			result.set(i, (byte) (nibbles_ptr.get(i) & otherValue.nibbles_ptr.get(i)));
		}
		TitanHexString ret_val = new TitanHexString(result);
		clearUnusedNibble();

		return ret_val;
	}

	// originally operator&
	public TitanHexString and4b(final TitanHexString_Element otherValue) {
		mustBound("Left operand of operator and4b is an unbound hexstring value.");
		otherValue.mustBound("Right operand of operator and4b is an unbound hexstring element.");

		if (lengthOf().getInt() != 1) {
			throw new TtcnError("The hexstring operands of operator and4b must have the same length.");
		}
		byte result = (byte) (get_nibble(0) & otherValue.get_nibble());

		return new TitanHexString(result);
	}

	// originally operator|
	public TitanHexString or4b(final TitanHexString otherValue) {
		mustBound("Left operand of operator or4b is an unbound hexstring value.");
		otherValue.mustBound("Right operand of operator or4b is an unbound hexstring value.");

		if(nibbles_ptr.size() != otherValue.nibbles_ptr.size()){
			throw new TtcnError("The hexstring operands of operator or4b must have the same length.");
		}
		if (lengthOf().getInt() == 0) {
			return this;
		}
		List<Byte> result = new ArrayList<Byte>();
		result.addAll(nibbles_ptr);
		for (int i = 0; i < nibbles_ptr.size(); i++) {
			result.set(i, (byte) (nibbles_ptr.get(i) | otherValue.nibbles_ptr.get(i)));
		}
		TitanHexString ret_val = new TitanHexString(result);
		clearUnusedNibble();

		return ret_val;
	}

	// originally operator|
	public TitanHexString or4b(final TitanHexString_Element otherValue) {
		mustBound("Left operand of operator or4b is an unbound hexstring value.");
		otherValue.mustBound("Right operand of operator or4b is an unbound hexstring element.");

		if (lengthOf().getInt() != 1) {
			throw new TtcnError("The hexstring operands of operator or4b must have the same length.");
		}
		byte result = (byte) (get_nibble(0) | otherValue.get_nibble());

		return new TitanHexString(result);
	}

	// originally operator^
	public TitanHexString xor4b(final TitanHexString otherValue) {
		mustBound("Left operand of operator xor4b is an unbound hexstring value.");
		otherValue.mustBound("Right operand of operator xor4b is an unbound hexstring value.");

		if (lengthOf() != otherValue.lengthOf()) {
			throw new TtcnError("The hexstring operands of operator xor4b must have the same length.");
		}
		if (lengthOf().getInt() == 0) {
			return this;
		}
		int n_bytes = (lengthOf().getInt() + 1) / 2;
		List<Byte> result = new ArrayList<Byte>(n_bytes);
		for (int i = 0; i < nibbles_ptr.size(); i++) {
			result.set(i, (byte) (nibbles_ptr.get(i) ^ otherValue.nibbles_ptr.get(i)));
		}
		TitanHexString ret_val = new TitanHexString(result);
		clearUnusedNibble();

		return ret_val;
	}

	// originally operator^
	public TitanHexString xor4b(final TitanHexString_Element otherValue) {
		mustBound("Left operand of operator xor4b is an unbound hexstring value.");
		otherValue.mustBound("Right operand of operator xor4b is an unbound hexstring element.");

		if (lengthOf().getInt() != 1) {
			throw new TtcnError("The hexstring operands of operator xor4b must have the same length.");
		}
		byte result = (byte) (get_nibble(0) ^ otherValue.get_nibble());
		return new TitanHexString(result);
	}

	// originally operator<<
	public TitanHexString shiftLeft(int shiftCount) {
		mustBound("Unbound hexstring operand of shift left operator.");

		if (shiftCount > 0) {
			if (nibbles_ptr.size() == 0) {
				return this;
			}
			int n_nibbles = nibbles_ptr.size();
			List<Byte> result = new ArrayList<Byte>();
			result.addAll(nibbles_ptr);
			if (shiftCount > n_nibbles) {
				shiftCount = n_nibbles;
			}
			for (int i = 0; i < n_nibbles - shiftCount; i++) {
				result.set(i, nibbles_ptr.get(i + shiftCount));
			}
			for (int i = n_nibbles - shiftCount; i < n_nibbles; i++) {
				result.set(i, (byte) 0);
			}
			TitanHexString ret_val = new TitanHexString(result);
			return ret_val;
		} else if (shiftCount == 0) {
			return this;
		} else {
			return this.shiftRight(-shiftCount);
		}
	}

	// originally operator<<
	public TitanHexString shiftLeft(final TitanInteger shiftCount) {
		shiftCount.mustBound("Unbound right operand of hexstring shift left operator.");

		return this.shiftLeft(shiftCount.getInt());
	}

	// originally operator>>
	public TitanHexString shiftRight(int shiftCount) {
		mustBound("Unbound operand of hexstring shift right operator.");

		if (shiftCount > 0) {
			if (nibbles_ptr.size() == 0) {
				return this;
			}
			int n_nibbles = nibbles_ptr.size();
			List<Byte> result = new ArrayList<Byte>();
			result.addAll(nibbles_ptr);
			if (shiftCount > n_nibbles) {
				shiftCount = n_nibbles;
			}
			for (int i = 0; i < shiftCount; i++) {
				result.set(i, (byte) 0);
			}
			for (int i = 0; i < n_nibbles - shiftCount; i++) {
				result.set(i + shiftCount, nibbles_ptr.get(i));
			}
			TitanHexString ret_val = new TitanHexString(result);
			return ret_val;
		} else if (shiftCount == 0) {
			return this;
		} else {
			return this.shiftLeft(-shiftCount);
		}
	}

	// originally operator>>
	public TitanHexString shiftRight(final TitanInteger shiftCount) {
		shiftCount.mustBound("Unbound right operand of hexstring right left operator.");

		return this.shiftRight(shiftCount.getInt());
	}

	//originally operator<<=
	public TitanHexString rotateLeft(int rotateCount){
		mustBound("Unbound hexstring operand of rotate left operator.");

		if(nibbles_ptr.size() == 0){
			return this;
		}
		if(rotateCount >= 0){
			rotateCount %= nibbles_ptr.size();
			if(rotateCount == 0){
				return this;
			}
			return this.shiftLeft(rotateCount).or4b(this.shiftRight(nibbles_ptr.size() - rotateCount));
		} else {
			return this.rotateRight(-rotateCount);
		}	
	}
	//originally operator<<=
	public TitanHexString rotateLeft(final TitanInteger rotateCount){
		rotateCount.mustBound("Unbound right operand of hexstring rotate left operator.");

		return this.rotateLeft(rotateCount.getInt());
	}

	//originally operator>>=
	public TitanHexString rotateRight(int rotateCount){
		mustBound("Unbound hexstring operand of rotate right operator.");

		if(nibbles_ptr.size() == 0){
			return this;
		}
		if(rotateCount >= 0){
			rotateCount %= nibbles_ptr.size();
			if(rotateCount == 0){
				return this;
			}
			return this.shiftRight(rotateCount).or4b(this.shiftLeft(nibbles_ptr.size() - rotateCount));
		} else {
			return this.rotateLeft(-rotateCount);
		}
	}

	//originally operator>>=
	public TitanHexString rotateRight(final TitanInteger rotateCount){
		rotateCount.mustBound("Unbound right operand of hexstring rotate right operator.");

		return this.rotateRight(rotateCount.getInt());
	}
}
