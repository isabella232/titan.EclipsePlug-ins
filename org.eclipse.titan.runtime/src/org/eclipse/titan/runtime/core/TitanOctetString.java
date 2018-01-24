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
import java.util.regex.Pattern;

import org.eclipse.titan.runtime.core.RAW.RAW_coding_par;
import org.eclipse.titan.runtime.core.RAW.RAW_enc_tree;
import org.eclipse.titan.runtime.core.RAW.ext_bit_t;
import org.eclipse.titan.runtime.core.TTCN_EncDec.error_type;
import org.eclipse.titan.runtime.core.TTCN_EncDec.raw_order_t;

/**
 * TTCN-3 octetstring
 * @author Arpad Lovassy
 * @author Farkas Izabella Ingrid
 * @author Andrea Palfi
 */
public class TitanOctetString extends Base_Type {

	// originally octetstring_value_match
	private static final Pattern OCTETSTRING_VALUE_PATTERN = Pattern.compile( "^([0-9A-Fa-f]{2})+$" );

	private static final String HEX_DIGITS = "0123456789ABCDEF";

	/**
	 * octetstring value.
	 *
	 * Packed storage of hex digit pairs, filled from LSB.
	 */
	private char val_ptr[];

	public TitanOctetString() {
	}

	public TitanOctetString( final char aOtherValue[] ) {
		val_ptr = TitanStringUtils.copyCharList( aOtherValue );
	}

	public TitanOctetString( final TitanOctetString aOtherValue ) {
		aOtherValue.mustBound( "Copying an unbound octetstring value." );

		val_ptr = TitanStringUtils.copyCharList( aOtherValue.val_ptr );
	}

	public TitanOctetString( final char aValue ) {
		val_ptr = new char[1];
		val_ptr[0] = aValue;
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
	private static char[] octetstr2bytelist(final String aHexString) {
		final int len = aHexString.length();
		final char result[] = new char[(len + 1) / 2];
		for (int i = 0; i < len; i += 2) {
			final char hexDigit1 = aHexString.charAt(i);
			final char hexDigit2 = aHexString.charAt(i + 1);
			final char value = octet2value(hexDigit1, hexDigit2);
			result[i / 2] = value;
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

	/** Return the nibble at index i
	 *
	 * @param nibble_index
	 * @return
	 */
	public char get_nibble(final int nibble_index) {
		return val_ptr[nibble_index];
	}

	public void set_nibble(final int nibble_index, final char new_value) {
		val_ptr[nibble_index] = new_value;
	}

	//originally char*()
	public char[] getValue() {
		return val_ptr;
	}

	//takes ownership of aOtherValue
	public void setValue( final char[] aOtherValue ) {
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
		val_ptr = new char[1];
		val_ptr[0] = aOtherValue.get_nibble();

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
	public TitanInteger lengthOf() {
		mustBound("Performing lengthof operation on an unbound octetstring value.");

		return new TitanInteger(val_ptr.length);
	}

	// originally operator==
	public boolean operatorEquals( final TitanOctetString otherValue ) {
		mustBound("Unbound left operand of octetstring comparison.");
		otherValue.mustBound("Unbound right operand of octetstring comparison.");

		return Arrays.equals(val_ptr, otherValue.val_ptr );
	}

	public boolean operatorEquals( final TitanOctetString_Element otherValue ) {
		mustBound("Unbound left operand of octetstring comparison.");
		otherValue.mustBound("Unbound right operand of octetstring comparison.");

		return otherValue.operatorEquals(this);
		//new TitanBoolean(val_ptr.equals( otherValue.get_nibble()));
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

	public boolean operatorNotEquals( final TitanOctetString_Element aOtherValue ) {
		return !operatorEquals( aOtherValue );
	}

	public boolean operatorNotEquals( final Base_Type aOtherValue ) {
		return !operatorEquals( aOtherValue );
	}

	public void cleanUp() {
		val_ptr = null;
	}

	//originally operator[](int)
	public TitanOctetString_Element getAt(final int index_value) {
		if (val_ptr == null && index_value == 0) {
			val_ptr = new char[1];
			return new TitanOctetString_Element(false, this, 0);
		} else {
			mustBound("Accessing an element of an unbound octetstring value.");

			if (index_value < 0) {
				throw new TtcnError("Accessing an octetstring element using a negative index (" + index_value + ").");
			}

			final int n_nibbles = val_ptr.length;
			if (index_value > n_nibbles) {
				throw new TtcnError("Index overflow when accessing a octetstring element: The index is " + index_value +
						", but the string has only " + n_nibbles + " hexadecimal digits.");
			}
			if (index_value == n_nibbles) {
				final char temp[] = new char[val_ptr.length + 1];
				System.arraycopy(val_ptr, 0, temp, 0, val_ptr.length);
				val_ptr = temp;
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

		final int n_nibbles = val_ptr.length;
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

	public void log() {
		if (val_ptr != null) {
			boolean onlyPrintable = true;
			TtcnLogger.log_char('\'');
			for (int i = 0; i < val_ptr.length; i++) {
				final char octet = val_ptr[i];
				TtcnLogger.log_octet(octet); // get_nibble(i)
				if (onlyPrintable && !(TtcnLogger.isPrintable(octet))) {
					onlyPrintable = false;
				}
			}
			TtcnLogger.log_event_str("'O");
			if (onlyPrintable && val_ptr.length > 0) {
				TtcnLogger.log_event_str("(\"");
				for (int i = 0; i < val_ptr.length; i++) {
					TtcnLogger.logCharEscaped(val_ptr[i]);
				}
				TtcnLogger.log_event_str("\")");
			}
		} else {
			TtcnLogger.log_event_unbound();
		}
	}

	@Override
	public String toString() {
		if ( val_ptr == null ) {
			return "<unbound>";
		}

		final StringBuilder sb = new StringBuilder();
		sb.append('\'');
		final int size = val_ptr.length;
		for (int i = 0; i < size; i++) {
			final int digit = val_ptr[i];
			sb.append(HEX_DIGITS.charAt(digit / 16));
			sb.append(HEX_DIGITS.charAt(digit % 16));
		}
		sb.append('\'');
		return sb.toString();
	}

	@Override
	/** {@inheritDoc} */
	public void encode_text(final Text_Buf text_buf) {
		mustBound("Text encoder: Encoding an unbound octetstring value.");

		final int octets = val_ptr.length;
		text_buf.push_int(octets);
		if (octets > 0) {
			byte[] temp = new byte[octets];
			for (int i = 0; i < octets; i++) {
				temp[i] = (byte)val_ptr[i];
			}
			text_buf.push_raw(temp.length, temp);
		}
	}

	@Override
	/** {@inheritDoc} */
	public void decode_text(final Text_Buf text_buf) {
		cleanUp();

		final int n_octets = text_buf.pull_int().getInt();
		if (n_octets < 0) {
			throw new TtcnError("Text decoder: Invalid length was received for an octetstring.");
		}
		if (n_octets > 0) {
			val_ptr = new char[n_octets];
			final byte[] temp = new byte[n_octets];
			text_buf.pull_raw(n_octets, temp);
			for (int i = 0; i < n_octets; i++) {
				val_ptr[i] = (char) temp[i];
			}
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
	public TitanOctetString concatenate( final TitanOctetString otherValue ) {
		mustBound( "Unbound left operand of octetstring concatenation." );
		otherValue.mustBound( "Unbound right operand of octetstring concatenation." );

		if(val_ptr.length == 0) {
			return new TitanOctetString(otherValue);
		}
		if (otherValue.val_ptr.length == 0) {
			return new TitanOctetString(this);
		}

		final char temp[] = new char[val_ptr.length + otherValue.val_ptr.length];
		System.arraycopy(val_ptr, 0, temp, 0, val_ptr.length);
		System.arraycopy(otherValue.val_ptr, 0, temp, val_ptr.length, otherValue.val_ptr.length);

		final TitanOctetString result = new TitanOctetString( temp );

		return result;
	}

	public TitanOctetString concatenate( final TitanOctetString_Element otherValue ) {
		mustBound( "Unbound left operand of octetstring concatenation." );
		otherValue.mustBound( "Unbound right operand of octetstring element concatenation." );

		final char temp[] = new char[val_ptr.length + 1];
		System.arraycopy(val_ptr, 0, temp, 0, val_ptr.length);
		temp[ val_ptr.length ] = otherValue.get_nibble();

		final TitanOctetString result = new TitanOctetString( temp );

		return result;
	}

	// originally operator~
	public TitanOctetString not4b() {
		mustBound("Unbound octetstring operand of operator not4b.");

		final TitanOctetString result = new TitanOctetString();
		result.val_ptr = new char[val_ptr.length];
		for (int i = 0; i < val_ptr.length; i++) {
			final int digit1 = val_ptr[i] / 16;
			final int digit2 = val_ptr[i] % 16;
			final int negDigit1 = ~digit1 & 0x0F;
			final int negDigit2 = ~digit2 & 0x0F;
			result.val_ptr[i] = (char)((negDigit1  << 4) + negDigit2);
		}

		return result;
	}

	// originally operator&
	public TitanOctetString and4b(final TitanOctetString otherValue) {
		mustBound("Left operand of operator and4b is an unbound octetstring value.");
		otherValue.mustBound("Right operand of operator and4b is an unbound octetstring value.");

		if (val_ptr.length != otherValue.val_ptr.length) {
			throw new TtcnError("The octetstring operands of operator and4b must have the same length.");
		}

		final TitanOctetString result = new TitanOctetString();
		result.val_ptr = new char[val_ptr.length];

		for (int i = 0; i < val_ptr.length; i++) {
			result.val_ptr[i] = (char) (val_ptr[i] & otherValue.val_ptr[i]);
		}

		return result;
	}

	// originally operator&
	public TitanOctetString and4b(final TitanOctetString_Element otherValue) {
		mustBound("Left operand of operator and4b is an unbound octetstring value.");
		otherValue.mustBound("Right operand of operator and4b is an unbound octetstring value.");

		if (val_ptr.length != 1) {
			throw new TtcnError("The octetstring operands of operator and4b must have the same length.");
		}

		return new TitanOctetString((char)(val_ptr[0] & otherValue.get_nibble()));
	}

	// originally operator|
	public TitanOctetString or4b(final TitanOctetString otherValue) {
		mustBound("Left operand of operator or4b is an unbound octetstring value.");
		otherValue.mustBound("Right operand of operator or4b is an unbound octetstring value.");

		if (val_ptr.length != otherValue.val_ptr.length) {
			throw new TtcnError("The octetstring operands of operator or4b must have the same length.");
		}

		final TitanOctetString result = new TitanOctetString();
		result.val_ptr = new char[val_ptr.length];
		for (int i = 0; i < val_ptr.length; i++) {
			result.val_ptr[i] = (char) (val_ptr[i] | otherValue.val_ptr[i]);
		}

		return result;

	}

	// originally operator|
	public TitanOctetString or4b(final TitanOctetString_Element otherValue) {
		mustBound("Left operand of operator or4b is an unbound octetstring value.");
		otherValue.mustBound("Right operand of operator or4b is an unbound octetstring value.");

		if (val_ptr.length != 1) {
			throw new TtcnError("The octetstring operands of operator or4b must have the same length.");
		}

		return new TitanOctetString((char)(val_ptr[0] | otherValue.get_nibble()));
	}

	//originally operator^
	public TitanOctetString xor4b(final TitanOctetString otherValue) {
		mustBound("Left operand of operator xor4b is an unbound octetstring value.");
		otherValue.mustBound("Right operand of operator xor4b is an unbound octetstring value.");

		if (val_ptr.length != otherValue.val_ptr.length) {
			throw new TtcnError("The octetstring operands of operator xor4b must have the same length.");
		}

		final TitanOctetString result = new TitanOctetString();
		result.val_ptr = new char[val_ptr.length];
		for (int i = 0; i < val_ptr.length; i++) {
			result.val_ptr[i] = (char)(val_ptr[i] ^ otherValue.val_ptr[i]);
		}

		return result;
	}

	//originally operator^
	public TitanOctetString xor4b(final TitanOctetString_Element otherValue) {
		mustBound("Left operand of operator xor4b is an unbound octetstring value.");
		otherValue.mustBound("Right operand of operator xor4b is an unbound octetstring element.");

		if (val_ptr.length != 1) {
			throw new TtcnError("The octetstring operands of operator xor4b must have the same length.");
		}

		return new TitanOctetString((char)(val_ptr[0] ^ otherValue.get_nibble()));
	}

	//originally operator<<
	public TitanOctetString shiftLeft(int shiftCount) {
		mustBound("Unbound octetstring operand of shift left operator.");

		if (shiftCount > 0) {
			if (val_ptr.length == 0) {
				return this;
			}

			final TitanOctetString result = new TitanOctetString();
			result.val_ptr = new char[val_ptr.length];
			if (shiftCount > val_ptr.length) {
				shiftCount = val_ptr.length;
			}

			for (int i = 0; i < val_ptr.length - shiftCount; i++) {
				result.val_ptr[i] = val_ptr[i + shiftCount];
			}

			for (int i = val_ptr.length - shiftCount; i < val_ptr.length; i++) {
				result.val_ptr[i] = (char) 0;
			}
			return result;
		} else {
			if (shiftCount == 0) {
				return this;
			} else {
				return this.shiftRight(-shiftCount);
			}
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
			if (val_ptr.length == 0) {
				return this;
			}

			final TitanOctetString result = new TitanOctetString();
			result.val_ptr = new char[val_ptr.length];
			if (shiftCount > val_ptr.length) {
				shiftCount = val_ptr.length;
			}
			for (int i = 0; i < shiftCount; i++) {
				result.val_ptr[i] = (char) 0;
			}
			for (int i = shiftCount; i < val_ptr.length; i++) {
				result.val_ptr[i] = val_ptr[i - shiftCount];
			}
			return result;
		} else {
			if (shiftCount == 0) {
				return this;
			} else {
				return this.shiftLeft(-shiftCount);
			}
		}
	}

	public TitanOctetString shiftRight(final TitanInteger otherValue){
		mustBound("Unbound right operand of octetstring shift right operator.");

		return shiftRight(otherValue.getInt());
	}

	// originally operator<<=
	public TitanOctetString rotateLeft(int rotateCount) {
		mustBound("Unbound octetstring operand of rotate left operator.");

		if (val_ptr.length == 0) {
			return this;
		}
		if (rotateCount >= 0) {
			rotateCount = rotateCount % val_ptr.length;
			if (rotateCount == 0) {
				return this;
			}

			final TitanOctetString result = new TitanOctetString();
			result.val_ptr = new char[val_ptr.length];
			for (int i = 0; i < val_ptr.length - rotateCount; i++) {
				result.val_ptr[i] = val_ptr[i + rotateCount];
			}
			for (int i = val_ptr.length - rotateCount; i < val_ptr.length; i++) {
				result.val_ptr[i] = val_ptr[i + rotateCount - val_ptr.length];
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

		if (val_ptr.length == 0) {
			return this;
		}
		if (rotateCount >= 0) {
			rotateCount = rotateCount % val_ptr.length;
			if (rotateCount == 0) {
				return this;
			}
			final TitanOctetString result = new TitanOctetString();
			result.val_ptr = new char[val_ptr.length];
			if (rotateCount > val_ptr.length) {
				rotateCount = val_ptr.length;
			}
			for (int i = 0; i < rotateCount; i++) {
				result.val_ptr[i] = val_ptr[i - rotateCount + val_ptr.length];
			}
			for (int i = rotateCount; i < val_ptr.length; i++) {
				result.val_ptr[i] = val_ptr[i - rotateCount];
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

	public int RAW_encode(final TTCN_Typedescriptor p_td, RAW_enc_tree myleaf) {
		if(!isBound()) {
			TTCN_EncDec_ErrorContext.error(error_type.ET_UNBOUND, "Encoding an unbound value.");
		}
		char[] bc = new char[val_ptr.length];
		int bl = val_ptr.length * 8;
		int align_length = p_td.raw.fieldlength != 0 ? p_td.raw.fieldlength - bl : 0;
		int blength = val_ptr.length;
		if((bl + align_length) < val_ptr.length * 8) {
			TTCN_EncDec_ErrorContext.error(error_type.ET_LEN_ERR, "There are insufficient bits to encode {0}: ", p_td.name);
			blength = p_td.raw.fieldlength / 8;
			bl = p_td.raw.fieldlength;
			align_length = 0;
		}
		if(myleaf.must_free) {
			myleaf.data_ptr = null;
		}
		myleaf.must_free = false;
		myleaf.data_ptr_used = true;
		if(p_td.raw.extension_bit != ext_bit_t.EXT_BIT_NO && myleaf.coding_par.bitorder == raw_order_t.ORDER_MSB) {
			if(blength > RAW.RAW_INT_ENC_LENGTH) {
				myleaf.data_ptr = new char[blength];
				myleaf.must_free = true;
				myleaf.data_ptr_used = true;
			} else {
				bc = myleaf.data_array;
				myleaf.data_ptr_used = false;
			}
			for (int a = 0; a < blength; a++){
				bc[a] = (char) (val_ptr[a] << 1);
			}
		} else {
			myleaf.data_ptr = val_ptr;
		}
		if(p_td.raw.endianness == raw_order_t.ORDER_MSB) {
			myleaf.align = -align_length;
		} else {
			myleaf.align = -align_length;
		}
		return myleaf.length = bl + align_length;
	}

	public int RAW_decode(final TTCN_Typedescriptor p_td, TTCN_Buffer buff, int limit, raw_order_t top_bit_ord, boolean no_err, int sel_field, boolean first_call) {
		int prepaddlength = buff.increase_pos_padd(p_td.raw.prepadding);
		limit -= prepaddlength;
		int decode_length = p_td.raw.fieldlength == 0 ? (limit / 8) * 8 : p_td.raw.fieldlength;
		if (decode_length > limit || decode_length > buff.unread_len_bit()) {
			if (no_err) {
				return -TTCN_EncDec.error_type.ET_LEN_ERR.ordinal();
			}
			TTCN_EncDec_ErrorContext.error(error_type.ET_LEN_ERR, "There is not enough bits in the buffer to decode type {0}.", p_td.name);
			decode_length = ((limit > (int) buff.unread_len_bit() ? buff.unread_len_bit() : limit) / 8) * 8;
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
		cp.hexorder = raw_order_t.ORDER_LSB;
		if (p_td.raw.extension_bit != ext_bit_t.EXT_BIT_NO) {
			final char[] data = buff.get_read_data();
			int count = 1;
			int rot = top_bit_ord == raw_order_t.ORDER_LSB ? 0 : 7;
			if (p_td.raw.extension_bit == ext_bit_t.EXT_BIT_YES) {
				while (((data[count - 1] >> rot) & 0x01) == 0 && count * 8 < decode_length)
					count++;
			}
			else {
				while (((data[count - 1] >> rot) & 0x01) == 1 && count * 8 < decode_length)
					count++;
			}
			decode_length = count * 8;
		}
		val_ptr = null;
		val_ptr = new char[decode_length / 8];
		buff.get_b(decode_length, val_ptr, cp, top_bit_ord);
		if (p_td.raw.length_restrition != -1 && decode_length > p_td.raw.length_restrition) {
			if (p_td.raw.endianness == raw_order_t.ORDER_MSB) {
				System.arraycopy(val_ptr, decode_length / 8 - val_ptr.length, val_ptr, 0, val_ptr.length);
			}
		}
		if (p_td.raw.extension_bit != ext_bit_t.EXT_BIT_NO && cp.bitorder == raw_order_t.ORDER_MSB) {
			for (int a = 0; a < decode_length / 8; a++)
				val_ptr[a] = (char) (val_ptr[a] >> 1 | val_ptr[a] << 7);
		}
		decode_length += buff.increase_pos_padd(p_td.raw.padding);
		return decode_length + prepaddlength;
	}
}
