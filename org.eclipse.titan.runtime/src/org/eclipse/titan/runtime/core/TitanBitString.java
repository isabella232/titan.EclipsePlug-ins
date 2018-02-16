/******************************************************************************
 * Copyright (c) 2000-2017 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

import java.text.MessageFormat;
import java.util.Arrays;

import org.eclipse.titan.runtime.core.RAW.RAW_coding_par;
import org.eclipse.titan.runtime.core.RAW.RAW_enc_tr_pos;
import org.eclipse.titan.runtime.core.RAW.RAW_enc_tree;
import org.eclipse.titan.runtime.core.TTCN_EncDec.coding_type;
import org.eclipse.titan.runtime.core.TTCN_EncDec.error_type;
import org.eclipse.titan.runtime.core.TTCN_EncDec.raw_order_t;

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
	 * Each element only stores 8 bits.
	 * //TODO check if using more bits would have better performance.
	 */
	private int bits_ptr[];

	/** number of bits */
	private int n_bits;

	public TitanBitString() {
		bits_ptr = null;
		n_bits = 0;
	}

	public TitanBitString( final int aOtherValue[], final int aNoBits ) {
		bits_ptr = TitanStringUtils.copyIntegerList( aOtherValue );
		n_bits = aNoBits;
		clear_unused_bits();
	}

	public TitanBitString( final TitanBitString aOtherValue ) {
		aOtherValue.mustBound( "Copying an unbound bitstring value." );

		bits_ptr = TitanStringUtils.copyIntegerList( aOtherValue.bits_ptr );
		n_bits = aOtherValue.n_bits;
	}

	public TitanBitString( final int aValue ) {
		bits_ptr = new int[1];
		bits_ptr[0] = aValue;
		n_bits = 8;
	}

	/**
	 * Constructor
	 * @param aValue string representation of a bitstring value, without ''B, it contains only '0' and '1' characters.
	 * NOTE: this is the way bitstring value is stored in Bitstring_Value
	 */
	public TitanBitString( final String aValue ) {
		bits_ptr = bitstr2intlist( aValue );
		n_bits = aValue.length();
	}

	/**
	 * Converts a string representation of a bitstring to a list of bytes
	 * @param aBitString string representation of bitstring
	 * @return value list of the bitstring, groupped in bytes
	 */
	private static int[] bitstr2intlist(final String aBitString) {
		final int len = aBitString.length();
		final int result[] = new int[(len + 7) / 8];
		for (int i = 0; i < len; i += 8) {
			final String byteStr = aBitString.substring(i, i + 8 < len ? i + 8 : len);
			final byte[] byteArray = byteStr.getBytes();
			final int byteValue = bitstr2byte(byteArray);
			result[i / 8] = byteValue;
		}

		return result;
	}

	/**
	 * Converts a string representation of a short bitstring (max length 8) to a byte
	 * @param aBitString8 string representation of bitstring as byte array, maximum length is 8, byte values are '0' or '1'
	 * @return value of the bitstring
	 */
	private static int bitstr2byte(final byte[] aBitString8) {
		int result = 0;
		int digit = 1;
		for (int i = 0; i < aBitString8.length; i++, digit *= 2) {
			if (aBitString8[i] == '1') {
				result += digit;
			}
		}
		return result;
	}

	/**
	 * Sets unused bits to 0
	 */
	private void clear_unused_bits() {
		if (n_bits % 8 != 0) {
			final int listIndex = (n_bits - 1) / 8;
			int bytevalue = bits_ptr[listIndex];
			bytevalue &= 0xFF >> (7 - (n_bits - 1) % 8);
			bits_ptr[listIndex] = bytevalue;
		}
	}

	/** Return the nibble at index i
	 *
	 * @param aBitIndex
	 * @return bit value ( 0 or 1 )
	 */
	boolean getBit(final int aBitIndex) {
		return (bits_ptr[aBitIndex / 8] & (1 << (aBitIndex % 8))) != 0;
	}

	void setBit(final int aBitIndex, final boolean aNewValue) {
		final int mask = 1 << (aBitIndex % 8);
		// the index of the actual byte, where the modification is made
		final int listIndex = aBitIndex / 8;
		int bytevalue = bits_ptr[listIndex];
		if (aNewValue) {
			bytevalue |= mask;
		} else {
			bytevalue &= ~mask;
		}
		bits_ptr[listIndex] = bytevalue;
	}

	//originally char*()
	public int[] getValue() {
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
	public TitanBitString assign(final TitanBitString_Element otherValue) {
		otherValue.mustBound("Assignment of an unbound bitstring element to a bitstring.");

		final boolean bitValue = otherValue.get_bit();
		cleanUp();
		n_bits = 1;
		bits_ptr = new int[1];
		bits_ptr[0] = (int) (bitValue ? 1 : 0);

		return this;
	}


	//originally operator=
	public TitanBitString assign(final TitanBitString aOtherValue) {
		aOtherValue.mustBound("Assignment of an unbound bitstring value.");

		if (aOtherValue != this) {
			cleanUp();
			bits_ptr = TitanStringUtils.copyIntegerList(aOtherValue.bits_ptr);
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
	public TitanInteger lengthOf() {
		mustBound("Performing lengthof operation on an unbound bitstring value.");

		return new TitanInteger(n_bits);
	}

	//originally operator==
	public boolean operatorEquals( final TitanBitString otherValue ) {
		mustBound("Unbound left operand of bitstring comparison.");
		otherValue.mustBound("Unbound right operand of bitstring comparison.");

		return n_bits == otherValue.n_bits && Arrays.equals(bits_ptr, otherValue.bits_ptr );
	}

	//originally operator==
	public boolean operatorEquals(final TitanBitString_Element otherValue){
		mustBound("Unbound left operand of bitstring comparison.");
		otherValue.mustBound("Unbound right operand of bitstring element comparison.");

		if(n_bits != 1){
			return false;
		}

		return getBit(0) == otherValue.get_bit();
	}

	@Override
	public boolean operatorEquals(final Base_Type otherValue) {
		if (otherValue instanceof TitanBitString) {
			return operatorEquals((TitanBitString)otherValue);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to bitstring", otherValue));
	}

	//originally operator!=
	public boolean operatorNotEquals( final TitanBitString aOtherValue ) {
		return !operatorEquals( aOtherValue );
	}

	//originally operator !=
	public boolean operatorNotEquals(final TitanBitString_Element aOtherValue){
		return !operatorEquals(aOtherValue);
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

		final int dest_ptr[] = new int[(resultBits + 7) / 8];
		System.arraycopy(bits_ptr, 0, dest_ptr, 0, bits_ptr.length);

		if (last_octet_bits != 0) {
			// non-trivial case: the length of left fragment is not
			// a multiply of 8 the bytes used in the result
			final int n_bytes = (resultBits + 7) / 8;
			// placing the bytes from the right fragment until the
			// result is filled
			for (int i = left_n_bytes; i < n_bytes; i++) {
				final Integer right_byte = aOtherValue.bits_ptr[i - left_n_bytes];
				// finish filling the previous byte
				int temp = dest_ptr[i - 1] | right_byte << last_octet_bits;
				dest_ptr[i - 1] = temp & 0xFF;
				// start filling the actual byte
				temp = right_byte >> (8 - last_octet_bits);
				dest_ptr[i] = temp;
			}
			if (left_n_bytes + right_n_bytes > n_bytes) {
				// if the result data area is shorter than the two operands together
				// the last bits of right fragment were not placed into the result in the previous for loop
				final int temp = dest_ptr[n_bytes - 1] | aOtherValue.bits_ptr[right_n_bytes - 1] << last_octet_bits;
				dest_ptr[n_bytes - 1] = temp & 0xFF;
			}
		} else {
			System.arraycopy(aOtherValue.bits_ptr, 0, dest_ptr, bits_ptr.length, aOtherValue.bits_ptr.length);
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
	public TitanBitString not4b() {
		mustBound("Unbound bitstring operand of operator not4b.");

		final int n_bytes = (n_bits + 7) / 8;
		if (n_bytes == 0) {
			return this;
		}

		final int dest_ptr[] = new int[(n_bits + 7) / 8];
		for (int i = 0; i < bits_ptr.length; i++) {
			dest_ptr[i] = ~bits_ptr[i] & 0xFF;
		}

		final TitanBitString ret_val = new TitanBitString(dest_ptr, n_bits);
		ret_val.clear_unused_bits();

		return ret_val;
	}

	//originally operator&
	public TitanBitString and4b(final TitanBitString otherValue){
		mustBound("Left operand of operator and4b is an unbound bitstring value.");
		otherValue.mustBound("Right operand of operator and4b is an unbound bitstring value.");

		if (n_bits != otherValue.n_bits) {
			throw new TtcnError("The bitstring operands of operator and4b must have the same length.");
		}
		if (n_bits == 0) {
			return this;
		}

		final int n_bytes = (n_bits + 7) / 8;
		final int dest_ptr[] = new int[n_bytes];
		for (int i = 0; i < bits_ptr.length; i++) {
			dest_ptr[i] = bits_ptr[i] & otherValue.bits_ptr[i];
		}

		final TitanBitString ret_val = new TitanBitString(dest_ptr, n_bits);
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

		final int result[] = new int[1];
		result[0] = getBit(0) && otherValue.get_bit() ? 1 : 0;

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
		final int dest_ptr[] = new int[n_bytes];
		for (int i = 0; i < bits_ptr.length; i++) {
			dest_ptr[i] = bits_ptr[i] | otherValue.bits_ptr[i];
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

		final int result[] = new int[1];
		result[0] = getBit(0) || otherValue.get_bit() ? 1 : 0;

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
		final int dest_ptr[] = new int[n_bytes];
		for (int i = 0; i < bits_ptr.length; i++) {
			dest_ptr[i] = bits_ptr[i] ^ otherValue.bits_ptr[i];
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

		final int result[] = new int[1];
		result[0] = getBit(0) ^ otherValue.get_bit() ? 1 : 0;

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
			final int result[] = new int[n_bytes];
			// result.addAll(bits_ptr);
			if (shift_bits != 0) {
				for (int byte_count = 0; byte_count < n_bytes - shift_bytes - 1; byte_count++) {
					result[byte_count] = ((bits_ptr[byte_count + shift_bytes] >> shift_bits) | (bits_ptr[byte_count + shift_bytes
							+ 1] << (8 - shift_bits))) & 0xFF;
				}

				result[n_bytes - shift_bytes - 1] = bits_ptr[n_bytes - 1] >> shift_bits;
			} else {
				for (int i = shift_bytes; i < n_bytes; i++) {
					result[i - shift_bytes] = bits_ptr[i];
				}
			}
			for (int i = n_bytes - shift_bytes; i < n_bytes; i++) {
				result[i] = 0;
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
			final int result[] = new int[n_bytes];
			//result.addAll(bits_ptr);
			for (int i = 0; i < shift_bytes; i++) {
				result[i] = 0;
			}
			if (shift_bits != 0) {
				result[shift_bytes] = (bits_ptr[0] << shift_bits) & 0xFF;
				for (int byte_count = shift_bytes + 1; byte_count < n_bytes; byte_count++) {
					result[byte_count] = (bits_ptr[byte_count - shift_bytes - 1] >> (8 - shift_bits))
							| (bits_ptr[byte_count - shift_bytes] << shift_bits) & 0xFF;
				}
			} else {
				for (int i = shift_bytes; i < n_bytes; i++) {
					result[i] = bits_ptr[i - shift_bytes];
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
			bits_ptr = new int[1];
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
				final int temp[] = new int[(n_bits + 7) / 8];
				System.arraycopy(bits_ptr, 0, temp, 0, bits_ptr.length);
				bits_ptr = temp;

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
		if (bits_ptr != null) {
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
	public boolean isPresent() {
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

	@Override
	/** {@inheritDoc} */
	public void encode_text(final Text_Buf text_buf) {
		mustBound("Text encoder: Encoding an unbound bitstring value.");

		text_buf.push_int(n_bits);
		if (n_bits > 0) {
			byte[] temp = new byte[bits_ptr.length];
			for (int i = 0; i < bits_ptr.length; i++) {
				temp[i] = (byte) bits_ptr[i];
			}
			text_buf.push_raw(temp.length, temp);
		}
	}

	@Override
	/** {@inheritDoc} */
	public void decode_text(final Text_Buf text_buf) {
		cleanUp();

		n_bits = text_buf.pull_int().getInt();
		if (n_bits < 0) {
			throw new TtcnError("Text decoder: Invalid length was received for a bitstring.");
		}
		if (n_bits > 0) {
			final int bytes = (n_bits + 7) / 8;
			bits_ptr = new int[bytes];
			final byte[] temp = new byte[bytes];
			text_buf.pull_raw(bytes, temp);
			for (int i = 0; i < bytes; i++) {
				bits_ptr[i] = (int) temp[i];
			}
			clear_unused_bits();
		}
	}

	public int getNBits() {
		return n_bits;
	}

	@Override
	/** {@inheritDoc} */
	public void encode(final TTCN_Typedescriptor p_td, final TTCN_Buffer p_buf, final coding_type p_coding, final int flavour) {
		switch (p_coding) {
		case CT_RAW:
			TTCN_EncDec_ErrorContext errorContext = new TTCN_EncDec_ErrorContext("While RAW-encoding type '%s': ", p_td.name);
			if (p_td.raw == null) {
				TTCN_EncDec_ErrorContext.error_internal("No RAW descriptor available for type '%s'.", p_td.name);
			}
			RAW_enc_tr_pos rp = new RAW_enc_tr_pos(0, null);
			RAW_enc_tree root = new RAW_enc_tree(true, null, rp, 1, p_td.raw);
			RAW_encode(p_td, root);
			root.put_to_buf(p_buf);

			errorContext.leaveContext();
			break;

		default:
			throw new TtcnError(MessageFormat.format("Unknown coding method requested to encode type '{0}''", p_td.name));
		}
	}

	@Override
	/** {@inheritDoc} */
	public void decode(final TTCN_Typedescriptor p_td, final TTCN_Buffer p_buf, final coding_type p_coding, final int flavour) {
		switch (p_coding) {
		case CT_RAW:
			final TTCN_EncDec_ErrorContext errorContext = new TTCN_EncDec_ErrorContext("While RAW-decoding type '%s': ", p_td.name);
			if (p_td.raw == null) {
				TTCN_EncDec_ErrorContext.error_internal("No RAW descriptor available for type '%s'.", p_td.name);
			}
			raw_order_t order;
			switch (p_td.raw.top_bit_order) {
			case TOP_BIT_LEFT:
				order=raw_order_t.ORDER_LSB;
				break;
			case TOP_BIT_RIGHT:
			default:
				order = raw_order_t.ORDER_MSB;
			}
			if(RAW_decode(p_td, p_buf, p_buf.get_len() * 8, order) < 0) {
				TTCN_EncDec_ErrorContext.error(TTCN_EncDec.error_type.ET_INCOMPL_MSG, "Can not decode type '%s', because invalid or incomplete message was received" , p_td.name);
			}

			errorContext.leaveContext();
			break;

		default:
			throw new TtcnError(MessageFormat.format("Unknown coding method requested to decode type '{0}''", p_td.name));
		}
	}

	public int RAW_encode(final TTCN_Typedescriptor p_td, RAW_enc_tree myleaf) {
		if (!isBound()) {
			TTCN_EncDec_ErrorContext.error(error_type.ET_UNBOUND, "Encoding an unbound value.");
		}
		int bl = n_bits;
		int align_length = p_td.raw.fieldlength != 0 ? p_td.raw.fieldlength - bl : 0;
		if ((bl + align_length) < n_bits) {
			TTCN_EncDec_ErrorContext.error(error_type.ET_LEN_ERR, "There is no sufficient bits to encode: ", p_td.name);
			bl = p_td.raw.fieldlength;
			align_length = 0;
		}
		// myleaf.ext_bit=EXT_BIT_NO;
		if (myleaf.must_free) {
			myleaf.data_ptr = null;
		}
		myleaf.must_free = false;
		myleaf.data_ptr_used = true;
		myleaf.data_ptr = new char[bits_ptr.length];
		for (int i = 0; i < bits_ptr.length; i++) {
			myleaf.data_ptr[i] = (char)bits_ptr[i];
		}
		boolean orders = false;
		if (p_td.raw.byteorder == raw_order_t.ORDER_MSB) {
			orders = true;
		}
		if (p_td.raw.bitorderinfield == raw_order_t.ORDER_LSB) {
			orders = !orders;
		}
		myleaf.coding_par.byteorder = orders ? raw_order_t.ORDER_MSB : raw_order_t.ORDER_LSB;
		orders = false;
		if (p_td.raw.bitorderinoctet == raw_order_t.ORDER_MSB) {
			orders = true;
		}
		if (p_td.raw.bitorderinfield == raw_order_t.ORDER_LSB) {
			orders = !orders;
		}
		myleaf.coding_par.bitorder = orders ? raw_order_t.ORDER_MSB : raw_order_t.ORDER_LSB;
		if (p_td.raw.endianness == raw_order_t.ORDER_MSB) {
			myleaf.align = align_length;
		} else {
			myleaf.align = -align_length;
		}
		return myleaf.length = bl + align_length;
	}

	public int RAW_decode(final TTCN_Typedescriptor p_td, TTCN_Buffer buff, int limit, raw_order_t top_bit_ord) {
		return RAW_decode(p_td, buff, limit, top_bit_ord, false, -1, true);
	}

	public int RAW_decode(final TTCN_Typedescriptor p_td, TTCN_Buffer buff, int limit, raw_order_t top_bit_ord, boolean no_err, int sel_field, boolean first_call) {
		int prepaddlength = buff.increase_pos_padd(p_td.raw.prepadding);
		limit -= prepaddlength;
		int decode_length = p_td.raw.fieldlength == 0 ? limit : p_td.raw.fieldlength;
		if (p_td.raw.fieldlength > limit
				|| p_td.raw.fieldlength > buff.unread_len_bit()) {
			if (no_err) {
				return -TTCN_EncDec.error_type.ET_LEN_ERR.ordinal();
			}
			TTCN_EncDec_ErrorContext.error(error_type.ET_LEN_ERR, "There is not enough bits in the buffer to decode type {0}.", p_td.name);
			decode_length = limit > (int) buff.unread_len_bit() ? buff.unread_len_bit() : limit;
		}
		cleanUp();
		n_bits = decode_length;
		bits_ptr = new int[(decode_length + 7) / 8];
		RAW_coding_par cp = new RAW_coding_par();
		boolean orders = false;
		if (p_td.raw.bitorderinoctet == raw_order_t.ORDER_MSB) {
			orders = true;
		}
		if (p_td.raw.bitorderinfield == raw_order_t.ORDER_LSB) {
			orders = !orders;
		}
		cp.bitorder = orders ? raw_order_t.ORDER_MSB : raw_order_t.ORDER_LSB;
		orders = false;
		if (p_td.raw.byteorder == raw_order_t.ORDER_MSB) {
			orders = true;
		}
		if (p_td.raw.bitorderinfield == raw_order_t.ORDER_LSB) {
			orders = !orders;
		}
		cp.byteorder = orders ? raw_order_t.ORDER_MSB : raw_order_t.ORDER_LSB;
		cp.fieldorder = p_td.raw.fieldorder;
		cp.hexorder = raw_order_t.ORDER_LSB;
		char[] tmp_bits = new char[bits_ptr.length];
		buff.get_b(decode_length, tmp_bits, cp, top_bit_ord);
		for (int i = 0; i < tmp_bits.length; i++) {
			bits_ptr[i] = (int) tmp_bits[i] ;
		}
		if (p_td.raw.length_restrition != -1
				&& decode_length > p_td.raw.length_restrition) {
			n_bits = p_td.raw.length_restrition;
			if (p_td.raw.endianness == raw_order_t.ORDER_LSB) {
				if ((decode_length - n_bits) % 8 != 0) {
					int bound = (decode_length - n_bits) % 8;
					int maxindex = (decode_length - 1) / 8;
					for (int a = 0, b = (decode_length - n_bits - 1) / 8; a < (n_bits + 7) / 8; a++, b++) {
						bits_ptr[a] = bits_ptr[b] >> bound;
					if (b < maxindex) {
						bits_ptr[a] = bits_ptr[b + 1] << (8 - bound);
					}
					}
				} else {
					System.arraycopy(bits_ptr, (decode_length - n_bits) / 8, bits_ptr, 0, n_bits / 8);
				}
			}
		}
		decode_length += buff.increase_pos_padd(p_td.raw.padding);
		clear_unused_bits();
		return decode_length + prepaddlength;
	}
}
