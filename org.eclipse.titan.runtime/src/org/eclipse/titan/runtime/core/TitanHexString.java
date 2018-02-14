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
 * TTCN-3 hexstring
 *
 * @author Arpad Lovassy
 * @author Gergo Ujhelyi
 * @author Andrea Palfi
 */
public class TitanHexString extends Base_Type {

	static final String HEX_DIGITS = "0123456789ABCDEF?*";

	/**
	 * hexstring value.
	 *
	 * Packed storage of hex digits, filled from LSB.
	 */
	private byte nibbles_ptr[];

	public TitanHexString() {
	}

	public TitanHexString(final byte aOtherValue[]) {
		nibbles_ptr = TitanStringUtils.copyByteList(aOtherValue);
	}

	public TitanHexString(final TitanHexString aOtherValue) {
		aOtherValue.mustBound("Copying an unbound hexstring value.");

		nibbles_ptr = TitanStringUtils.copyByteList(aOtherValue.nibbles_ptr);
	}

	public TitanHexString(final TitanHexString_Element otherValue) {
		otherValue.mustBound("Initialization from an unbound hexstring element.");

		nibbles_ptr = new byte[1];
		nibbles_ptr[0] = (byte) otherValue.get_nibble();
	}

	public TitanHexString(final byte aValue) {
		nibbles_ptr = new byte[1];
		nibbles_ptr[0] = aValue;
	}

	/**
	 * Constructor
	 * @param aValue string representation of a hexstring value, without ''B, it contains only [0-9A-F] characters.
	 * NOTE: this is the way hexstring value is stored in Hexstring_Value
	 */
	public TitanHexString(final String aValue) {
		nibbles_ptr = hexstr2bytelist(aValue);
	}

	private void clearUnusedNibble() {
		if (nibbles_ptr.length % 2 == 1) {
			nibbles_ptr[nibbles_ptr.length / 2] = (byte) (nibbles_ptr[nibbles_ptr.length / 2] & 0x0F);
		}
	}

	/**
	 * Converts a string representation of a hexstring to a list of bytes
	 * @param aHexString string representation of hexstring
	 * @return value list of the hexstring, groupped in bytes
	 */
	private static byte[] hexstr2bytelist(final String aHexString) {
		final int len = aHexString.length();
		final byte result[] = new byte[len];
		for (int i = 0; i < len; i++) {
			final char hexDigit = aHexString.charAt(i);
			final byte byteValue = hexdigit2byte(hexDigit);
			result[i] = byteValue;
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
		return nibbles_ptr[nibble_index];
	}

	public void set_nibble(final int nibble_index, final byte new_value) {
		nibbles_ptr[nibble_index] = new_value;
	}

	// originally char*()
	public byte[] getValue() {
		return nibbles_ptr;
	}

	public void setValue(final byte aOtherValue[]) {
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
		nibbles_ptr = new byte[1];
		nibbles_ptr[0] = (byte) (otherValue.get_nibble());

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

		return new TitanInteger(nibbles_ptr.length);
	}

	// originally operator==
	public boolean operatorEquals(final TitanHexString otherValue) {
		mustBound("Unbound left operand of hexstring comparison.");
		otherValue.mustBound("Unbound right operand of hexstring comparison.");

		return Arrays.equals(nibbles_ptr, otherValue.nibbles_ptr);
	}

	// originally operator==
	public boolean operatorEquals(final TitanHexString_Element otherValue) {
		mustBound("Unbound left operand of hexstring comparison.");
		otherValue.mustBound("Unbound right operand of hexstring element comparison.");

		if (nibbles_ptr.length != 1) {
			return false;
		}

		return get_nibble(0) == otherValue.get_nibble();
	}

	@Override
	public boolean operatorEquals(final Base_Type otherValue) {
		if (otherValue instanceof TitanHexString) {
			return operatorEquals((TitanHexString) otherValue);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to hexstring", otherValue));
	}

	// originally operator!=
	public boolean operatorNotEquals(final TitanHexString aOtherValue) {
		return !operatorEquals(aOtherValue);
	}

	// originally operator!=
	public boolean operatorNotEquals(final TitanHexString_Element otherValue) {
		return !operatorEquals(otherValue);
	}

	public void cleanUp() {
		nibbles_ptr = null;
	}

	// originally operator[](int)
	public TitanHexString_Element getAt(final int index_value) {
		if (nibbles_ptr == null && index_value == 0) {
			nibbles_ptr = new byte[1];
			return new TitanHexString_Element(false, this, 0);
		} else {
			mustBound("Accessing an element of an unbound hexstring value.");

			if (index_value < 0) {
				throw new TtcnError("Accessing an hexstring element using a negative index (" + index_value + ").");
			}

			final int n_nibbles = nibbles_ptr.length;
			if (index_value > n_nibbles) {
				throw new TtcnError("Index overflow when accessing a hexstring element: The index is " + index_value +
						", but the string has only " + n_nibbles + " hexadecimal digits.");
			}
			if (index_value == n_nibbles) {
				final byte temp[] = new byte[nibbles_ptr.length + 1];
				System.arraycopy(nibbles_ptr, 0, temp, 0, nibbles_ptr.length);
				nibbles_ptr = temp;
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

		final int n_nibbles = nibbles_ptr.length;
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

	public void log() {
		if (nibbles_ptr != null) {
			TtcnLogger.log_char('\'');
			for (int i = 0; i < nibbles_ptr.length; i++) {
				TtcnLogger.log_hex(get_nibble(i));
			}
			TtcnLogger.log_event_str("'H");
		} else {
			TtcnLogger.log_event_unbound();
		}
	}

	@Override
	public String toString() {
		if (nibbles_ptr == null) {
			return "<unbound>";
		}

		final StringBuilder sb = new StringBuilder();
		final int size = nibbles_ptr.length;
		for (int i = 0; i < size; i++) {
			final Byte digit = nibbles_ptr[i];
			sb.append(HEX_DIGITS.charAt(digit));
		}
		return sb.toString();
	}

	@Override
	/** {@inheritDoc} */
	public void encode_text(final Text_Buf text_buf) {
		mustBound("Text encoder: Encoding an unbound hexstring value.");

		final int nibbles = nibbles_ptr.length;
		text_buf.push_int(nibbles);
		if (nibbles > 0) {
			text_buf.push_raw(nibbles_ptr.length, nibbles_ptr);
		}
	}

	@Override
	/** {@inheritDoc} */
	public void decode_text(final Text_Buf text_buf) {
		cleanUp();

		final int n_nibbles = text_buf.pull_int().getInt();
		if (n_nibbles < 0) {
			throw new TtcnError("Text decoder: Invalid length was received for a hexstring.");
		}
		if (n_nibbles > 0) {
			nibbles_ptr = new byte[n_nibbles];
			text_buf.pull_raw(n_nibbles, nibbles_ptr);
		}
	}
	
	@Override
	/** {@inheritDoc} */
	public void encode(final TTCN_Typedescriptor p_td, final TTCN_Buffer p_buf, final coding_type p_coding, final int flavour) {
		switch (p_coding) {
		case CT_RAW: {
			final TTCN_EncDec_ErrorContext errorContext = new TTCN_EncDec_ErrorContext("While RAW-encoding type '%s': ", p_td.name);
			if (p_td.raw == null) {
				TTCN_EncDec_ErrorContext.error_internal("No RAW descriptor available for type '%s'.", p_td.name);
			}
			RAW_enc_tr_pos rp = new RAW_enc_tr_pos(0, null);
			RAW_enc_tree root = new RAW_enc_tree(true, null, rp, 1, p_td.raw);
			RAW_encode(p_td, root);
			root.put_to_buf(p_buf);

			errorContext.leaveContext();
			break;
		}
		default:
			throw new TtcnError("encoding of hexstrings is not yet completely implemented!");
		}
	}
	
	@Override
	/** {@inheritDoc} */
	public void decode(final TTCN_Typedescriptor p_td, final TTCN_Buffer p_buf, final coding_type p_coding, final int flavour) {
		switch (p_coding) {
		case CT_RAW:
			TTCN_EncDec_ErrorContext errorContext = new TTCN_EncDec_ErrorContext("While RAW-decoding type '%s': ", p_td.name);
			if(p_td.raw == null) {
				TTCN_EncDec_ErrorContext.error_internal("No RAW descriptor available for type '%s'.", p_td.name);
			}
			raw_order_t order;
			switch (p_td.raw.top_bit_order) {
			case TOP_BIT_LEFT:
				order = raw_order_t.ORDER_LSB;
				break;
			case TOP_BIT_RIGHT:
			default:
				order = raw_order_t.ORDER_MSB;
			}
			if (RAW_decode(p_td, p_buf, p_buf.get_len() * 8, order) < 0) {
				TTCN_EncDec_ErrorContext.error(error_type.ET_INCOMPL_MSG,  "Can not decode type '%s', because invalid or incomplete message was received" , p_td.name);
			}

			errorContext.leaveContext();
			break;
		default:
			throw new TtcnError("decoding of hexstrings is not yet completely implemented!");
		}
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

		if(nibbles_ptr.length == 0) {
			return new TitanHexString(otherValue);
		}
		if (otherValue.nibbles_ptr.length == 0) {
			return new TitanHexString(this);
		}

		final byte temp[] = new byte[nibbles_ptr.length + otherValue.nibbles_ptr.length];
		System.arraycopy(nibbles_ptr, 0, temp, 0, nibbles_ptr.length);
		System.arraycopy(otherValue.nibbles_ptr, 0, temp, nibbles_ptr.length, otherValue.nibbles_ptr.length);

		final TitanHexString result = new TitanHexString(temp);
		return result;
	}

	// originally operator+
	public TitanHexString concatenate(final TitanHexString_Element otherValue) {
		mustBound("Unbound left operand of hexstring concatenation.");
		otherValue.mustBound("Unbound right operand of hexstring element concatenation.");

		final byte temp[] = new byte[nibbles_ptr.length + 1];
		System.arraycopy(nibbles_ptr, 0, temp, 0, nibbles_ptr.length);
		temp[ nibbles_ptr.length ] = (byte) otherValue.get_nibble();

		final TitanHexString result = new TitanHexString(temp);

		return result;
	}

	// originally operator~
	public TitanHexString not4b() {
		mustBound("Unbound hexstring operand of operator not4b.");

		final int n_bytes = (nibbles_ptr.length + 1) / 2;
		if (n_bytes == 0) {
			return this;
		}

		final byte result[] = new byte[nibbles_ptr.length];
		for (int i = 0; i < nibbles_ptr.length; i++) {
			result[i] = (byte)((~nibbles_ptr[i] & 0x0F));
		}

		final TitanHexString ret_val = new TitanHexString(result);
		ret_val.clearUnusedNibble();

		return ret_val;
	}

	// originally operator&
	public TitanHexString and4b(final TitanHexString otherValue) {
		mustBound("Left operand of operator and4b is an unbound hexstring value.");
		otherValue.mustBound("Right operand of operator and4b is an unbound hexstring value.");

		if (nibbles_ptr.length != otherValue.nibbles_ptr.length) {
			throw new TtcnError("The hexstring operands of operator and4b must have the same length.");
		}
		if (nibbles_ptr.length == 0) {
			return this;
		}

		final byte result[] = new byte[nibbles_ptr.length];
		for (int i = 0; i < nibbles_ptr.length; i++) {
			result[i] = (byte) (nibbles_ptr[i] & otherValue.nibbles_ptr[i]);
		}

		final TitanHexString ret_val = new TitanHexString(result);
		clearUnusedNibble();

		return ret_val;
	}

	// originally operator&
	public TitanHexString and4b(final TitanHexString_Element otherValue) {
		mustBound("Left operand of operator and4b is an unbound hexstring value.");
		otherValue.mustBound("Right operand of operator and4b is an unbound hexstring element.");

		if (nibbles_ptr.length != 1) {
			throw new TtcnError("The hexstring operands of operator and4b must have the same length.");
		}
		final byte result = (byte) (get_nibble(0) & otherValue.get_nibble());

		return new TitanHexString(result);
	}

	// originally operator|
	public TitanHexString or4b(final TitanHexString otherValue) {
		mustBound("Left operand of operator or4b is an unbound hexstring value.");
		otherValue.mustBound("Right operand of operator or4b is an unbound hexstring value.");

		if(nibbles_ptr.length != otherValue.nibbles_ptr.length){
			throw new TtcnError("The hexstring operands of operator or4b must have the same length.");
		}
		if (nibbles_ptr.length == 0) {
			return this;
		}

		final byte result[] = new byte[nibbles_ptr.length];
		for (int i = 0; i < nibbles_ptr.length; i++) {
			result[i] = (byte) (nibbles_ptr[i] | otherValue.nibbles_ptr[i]);
		}

		final TitanHexString ret_val = new TitanHexString(result);
		clearUnusedNibble();

		return ret_val;
	}

	// originally operator|
	public TitanHexString or4b(final TitanHexString_Element otherValue) {
		mustBound("Left operand of operator or4b is an unbound hexstring value.");
		otherValue.mustBound("Right operand of operator or4b is an unbound hexstring element.");

		if (nibbles_ptr.length != 1) {
			throw new TtcnError("The hexstring operands of operator or4b must have the same length.");
		}
		final byte result = (byte) (get_nibble(0) | otherValue.get_nibble());

		return new TitanHexString(result);
	}

	// originally operator^
	public TitanHexString xor4b(final TitanHexString otherValue) {
		mustBound("Left operand of operator xor4b is an unbound hexstring value.");
		otherValue.mustBound("Right operand of operator xor4b is an unbound hexstring value.");

		if (nibbles_ptr.length != otherValue.nibbles_ptr.length) {
			throw new TtcnError("The hexstring operands of operator xor4b must have the same length.");
		}
		if (nibbles_ptr.length == 0) {
			return this;
		}

		final byte result[] = new byte[nibbles_ptr.length];
		for (int i = 0; i < nibbles_ptr.length; i++) {
			result[i] = (byte) (nibbles_ptr[i] ^ otherValue.nibbles_ptr[i]);
		}

		final TitanHexString ret_val = new TitanHexString(result);
		clearUnusedNibble();

		return ret_val;
	}

	// originally operator^
	public TitanHexString xor4b(final TitanHexString_Element otherValue) {
		mustBound("Left operand of operator xor4b is an unbound hexstring value.");
		otherValue.mustBound("Right operand of operator xor4b is an unbound hexstring element.");

		if (nibbles_ptr.length != 1) {
			throw new TtcnError("The hexstring operands of operator xor4b must have the same length.");
		}
		final byte result = (byte) (get_nibble(0) ^ otherValue.get_nibble());
		return new TitanHexString(result);
	}

	// originally operator<<
	public TitanHexString shiftLeft(int shiftCount) {
		mustBound("Unbound hexstring operand of shift left operator.");

		if (shiftCount > 0) {
			if (nibbles_ptr.length == 0) {
				return this;
			}

			final int n_nibbles = nibbles_ptr.length;
			final byte result[] = new byte[nibbles_ptr.length];
			if (shiftCount > n_nibbles) {
				shiftCount = n_nibbles;
			}
			for (int i = 0; i < n_nibbles - shiftCount; i++) {
				result[i] = nibbles_ptr[i + shiftCount];
			}
			for (int i = n_nibbles - shiftCount; i < n_nibbles; i++) {
				result[i] = (byte) 0;
			}

			return new TitanHexString(result);
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
			if (nibbles_ptr.length == 0) {
				return this;
			}

			final int n_nibbles = nibbles_ptr.length;
			final byte result[] = new byte[nibbles_ptr.length];
			if (shiftCount > n_nibbles) {
				shiftCount = n_nibbles;
			}
			for (int i = 0; i < shiftCount; i++) {
				result[i] = (byte) 0;
			}
			for (int i = 0; i < n_nibbles - shiftCount; i++) {
				result[i + shiftCount] = nibbles_ptr[i];
			}

			return new TitanHexString(result);
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

		if(nibbles_ptr.length == 0){
			return this;
		}
		if(rotateCount >= 0){
			rotateCount %= nibbles_ptr.length;
			if(rotateCount == 0){
				return this;
			}

			return this.shiftLeft(rotateCount).or4b(this.shiftRight(nibbles_ptr.length - rotateCount));
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

		if(nibbles_ptr.length == 0){
			return this;
		}
		if(rotateCount >= 0){
			rotateCount %= nibbles_ptr.length;
			if(rotateCount == 0){
				return this;
			}

			return this.shiftRight(rotateCount).or4b(this.shiftLeft(nibbles_ptr.length - rotateCount));
		} else {
			return this.rotateLeft(-rotateCount);
		}
	}

	//originally operator>>=
	public TitanHexString rotateRight(final TitanInteger rotateCount){
		rotateCount.mustBound("Unbound right operand of hexstring rotate right operator.");

		return this.rotateRight(rotateCount.getInt());
	}

	public int RAW_encode(final TTCN_Typedescriptor p_td, RAW_enc_tree myleaf) {
		if(!isBound()) {
			TTCN_EncDec_ErrorContext.error(error_type.ET_UNBOUND, "Encoding an unbound value.");
		}
		int nbits = nibbles_ptr.length * 4;
		int align_length = p_td.raw.fieldlength != 0 ? p_td.raw.fieldlength - nbits : 0;
		if((nbits + align_length) < nbits) {
			TTCN_EncDec_ErrorContext.error(error_type.ET_LEN_ERR, "There is no sufficient bits to encode {0}: ", p_td.name);
			nbits = p_td.raw.fieldlength;
			align_length = 0;
		}

		if(myleaf.must_free) {
			myleaf.data_ptr = null;
		}

		myleaf.must_free = false;
		myleaf.data_ptr_used = true;
		myleaf.data_ptr = new char[(nibbles_ptr.length + 1 ) / 2];
		for (int i = 1; i < nibbles_ptr.length; i+= 2) {
			myleaf.data_ptr[i / 2] = (char) ((nibbles_ptr[i] << 4 | nibbles_ptr[i - 1] & 0x0F) );
		}
		if(p_td.raw.endianness == raw_order_t.ORDER_MSB) {
			myleaf.align = -align_length;
		} else {
			myleaf.align = align_length;
		}

		return myleaf.length = nbits + align_length;
	}
	
	public int RAW_decode(final TTCN_Typedescriptor p_td, TTCN_Buffer buff, int limit, raw_order_t top_bit_ord) {
		return RAW_decode(p_td, buff, limit, top_bit_ord, false, -1, true);
	}

	public int RAW_decode(final TTCN_Typedescriptor p_td, TTCN_Buffer buff, int limit, raw_order_t top_bit_ord, boolean no_err, int sel_field, boolean first_call) {
		int prepaddlength = buff.increase_pos_padd(p_td.raw.prepadding);
		limit -= prepaddlength;
		int decode_length = p_td.raw.fieldlength == 0 ? (limit / 4) * 4 : p_td.raw.fieldlength;
		if ( p_td.raw.fieldlength > limit || p_td.raw.fieldlength > buff.unread_len_bit()) {
			if (no_err) {
				return -error_type.ET_LEN_ERR.ordinal();
			}
			TTCN_EncDec_ErrorContext.error(error_type.ET_LEN_ERR, "There is not enough bits in the buffer to decode type %s.", p_td.name);
			decode_length = ((limit > buff.unread_len_bit() ? buff.unread_len_bit() : limit) / 4) * 4;
		}
		RAW_coding_par cp = new RAW_coding_par();
		boolean orders = false;
		if (p_td.raw.bitorderinoctet == raw_order_t.ORDER_MSB) {
			orders = true; 
		}
		if (p_td.raw.bitorderinfield == raw_order_t.ORDER_MSB) {
			orders = !orders;
		}
		cp.bitorder = orders ? raw_order_t.ORDER_MSB : raw_order_t.ORDER_LSB;
		orders = false;
		if (p_td.raw.byteorder == raw_order_t.ORDER_MSB) {
			orders = true;
		}
		if (p_td.raw.bitorderinfield == raw_order_t.ORDER_MSB) {
			orders = !orders;
		}
		cp.byteorder = orders ? raw_order_t.ORDER_MSB : raw_order_t.ORDER_LSB;
		cp.fieldorder = p_td.raw.fieldorder;
		cp.hexorder = p_td.raw.hexorder;
		nibbles_ptr = null;
		nibbles_ptr = new byte[decode_length / 4];
		char[] tmp_nibbles = new char[decode_length / 4];
		buff.get_b(decode_length, tmp_nibbles, cp, top_bit_ord);
		int temp_index = 0;
		for (int i = 0; i < tmp_nibbles.length-1; i+=2) {
			nibbles_ptr[i] = (byte)(tmp_nibbles[temp_index] & 0x0F);
			nibbles_ptr[i+1] = (byte)(tmp_nibbles[temp_index] >> 4);
			temp_index++;
		}

		if (p_td.raw.length_restrition != -1 && decode_length > p_td.raw.length_restrition) {
			if (p_td.raw.endianness == raw_order_t.ORDER_MSB) {
				if ((decode_length - nibbles_ptr.length * 4) % 8 != 0) {
					int bound = (decode_length - nibbles_ptr.length * 4) % 8;
					int maxindex = (decode_length - 1) / 8;
					for (int a = 0, b = (decode_length - nibbles_ptr.length * 4 - 1) / 8; a < (nibbles_ptr.length * 4 + 7) / 8; a++, b++) {
						nibbles_ptr[a] = (byte) (nibbles_ptr[b] >> bound);
						if (b < maxindex) {
							nibbles_ptr[a] = (byte) (nibbles_ptr[b + 1] << (8 - bound));
						}
					}
				} else {
					System.arraycopy(nibbles_ptr, (decode_length - nibbles_ptr.length * 4) / 8, nibbles_ptr, 0, nibbles_ptr.length * 8);
				}
			}
		}
		decode_length += buff.increase_pos_padd(p_td.raw.padding);
		clearUnusedNibble();
		return decode_length + prepaddlength;
	}
}
