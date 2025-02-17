/******************************************************************************
 * Copyright (c) 2000-2021 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.titan.runtime.core.BER.ASN_BERdescriptor;
import org.eclipse.titan.runtime.core.BER.ASN_Tag;
import org.eclipse.titan.runtime.core.BER.ASN_TagClass;
import org.eclipse.titan.runtime.core.JSON.TTCN_JSONdescriptor;
import org.eclipse.titan.runtime.core.JSON.json_string_escaping;
import org.eclipse.titan.runtime.core.JSON_Tokenizer.json_token_t;
import org.eclipse.titan.runtime.core.Param_Types.Module_Param_Bitstring;
import org.eclipse.titan.runtime.core.Param_Types.Module_Param_Name;
import org.eclipse.titan.runtime.core.Param_Types.Module_Param_Unbound;
import org.eclipse.titan.runtime.core.Param_Types.Module_Parameter;
import org.eclipse.titan.runtime.core.Param_Types.Module_Parameter.expression_operand_t;
import org.eclipse.titan.runtime.core.Param_Types.Module_Parameter.operation_type_t;
import org.eclipse.titan.runtime.core.RAW.RAW_Force_Omit;
import org.eclipse.titan.runtime.core.RAW.RAW_coding_par;
import org.eclipse.titan.runtime.core.RAW.RAW_enc_tr_pos;
import org.eclipse.titan.runtime.core.RAW.RAW_enc_tree;
import org.eclipse.titan.runtime.core.RAW.TTCN_RAWdescriptor;
import org.eclipse.titan.runtime.core.RAW.ext_bit_t;
import org.eclipse.titan.runtime.core.RAW.raw_sign_t;
import org.eclipse.titan.runtime.core.RAW.top_bit_order_t;
import org.eclipse.titan.runtime.core.TTCN_EncDec.coding_type;
import org.eclipse.titan.runtime.core.TTCN_EncDec.error_type;
import org.eclipse.titan.runtime.core.TTCN_EncDec.raw_order_t;
import org.eclipse.titan.runtime.core.TitanCharString.CharCoding;

/**
 * TTCN-3 bitstring
 * @author Arpad Lovassy
 * @author Gergo Ujhelyi
 */
public class TitanBitString extends Base_Type {
	private static final ASN_Tag TitanBitString_tag_[] = new ASN_Tag[] {new ASN_Tag(ASN_TagClass.ASN_TAG_UNIV, 3)};
	public static final ASN_BERdescriptor TitanBitString_Ber_ = new ASN_BERdescriptor(1, TitanBitString_tag_);
	public static final TTCN_RAWdescriptor TitanBitString_raw_ = new TTCN_RAWdescriptor(0, raw_sign_t.SG_NO, raw_order_t.ORDER_LSB, raw_order_t.ORDER_LSB, raw_order_t.ORDER_LSB, raw_order_t.ORDER_LSB, ext_bit_t.EXT_BIT_NO, raw_order_t.ORDER_LSB, raw_order_t.ORDER_LSB, top_bit_order_t.TOP_BIT_INHERITED, 0, 0, 0, 8, 0, null, -1, CharCoding.UNKNOWN, null, false);
	public static final TTCN_JSONdescriptor TitanBitString_json_ = new TTCN_JSONdescriptor(false, null, false,  null, false, false, false, 0, null, false, json_string_escaping.ESCAPE_AS_SHORT);
	public static final TTCN_Typedescriptor TitanBitString_descr_ = new TTCN_Typedescriptor("BIT STRING", TitanBitString_Ber_, TitanBitString_raw_, TitanBitString_json_, null);

	/**
	 * bitstring value.
	 *
	 * Packed storage of bits, filled from LSB.
	 * Each element only stores 8 bits.
	 * //TODO check if using more bits would have better performance.
	 * //TODO also check using byte, to improve performance
	 */
	private int bits_ptr[];

	/** number of bits */
	private int n_bits;

	/**
	 * Initializes to unbound value.
	 * */
	public TitanBitString() {
		bits_ptr = null;
		n_bits = 0;
	}

	/**
	 * Initializes to a give value.
	 * Optimized, so that only a copy needs to be done.
	 *
	 * @param other_value
	 *                stores the values already in the internal
	 *                representation.
	 * @param nof_bits
	 *                the the number of bits in the otheer_value array.
	 * */
	public TitanBitString(final int other_value[], final int nof_bits) {
		bits_ptr = TitanString_Utils.copy_integer_list(other_value);
		n_bits = nof_bits;
		clear_unused_bits();
	}

	/**
	 * Initializes to a given value.
	 *
	 * @param otherValue
	 *                the value to initialize to.
	 * */
	public TitanBitString(final TitanBitString otherValue) {
		otherValue.must_bound("Copying an unbound bitstring value.");

		bits_ptr = TitanString_Utils.copy_integer_list(otherValue.bits_ptr);
		n_bits = otherValue.n_bits;
	}

	/**
	 * Creates a TitanBitString with a single bit.
	 *
	 * @param otherValue
	 *                must be bound
	 */
	public TitanBitString(final TitanBitString_Element otherValue) {
		otherValue.must_bound("Copying an unbound bitstring element.");

		bits_ptr = new int[1];
		bits_ptr[0] = otherValue.get_bit() ? 1 : 0;
		n_bits = 1;
	}

	/**
	 * Initializes to a given value.
	 *
	 * @param value
	 *                the value to initialize to.
	 * */
	public TitanBitString(final int value) {
		bits_ptr = new int[1];
		bits_ptr[0] = value;
		n_bits = 8;
	}

	/**
	 * Constructor
	 * @param aValue string representation of a bitstring value, without ''B, it contains only '0' and '1' characters.
	 * NOTE: this is the way bitstring value is stored in Bitstring_Value
	 */
	public TitanBitString(final String value) {
		// bitstring without spaces
		final String bitString = value.replaceAll(" ", "");
		bits_ptr = bitstr2intlist(bitString);
		n_bits = bitString.length();
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
	boolean get_bit(final int aBitIndex) {
		return (bits_ptr[aBitIndex / 8] & (1 << (aBitIndex % 8))) != 0;
	}

	void set_bit(final int aBitIndex, final boolean aNewValue) {
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

	/**
	 * Returns the internal data storage of this bitstring.
	 * <p>
	 * Please note, this code is for internal use only.
	 * Users are not recommended to use this function.
	 * As such it is also not part of the public API
	 *  and might change without notice.
	 *
	 * <p>
	 * char*() in the core
	 *
	 * @return the internal representation of the bitstring.
	 * */
	public int[] get_value() {
		must_bound("Casting an unbound bitstring value to const unsigned char*.");

		return bits_ptr;
	}

	/**
	 * Overwrites the internal data storage of this bitstring.
	 * Takes ownership of the provided data.
	 * <p>
	 * Please note, this code is for internal use only.
	 * Users are not recommended to use this function.
	 * As such it is also not part of the public API
	 *  and might change without notice.
	 *
	 * <p>
	 * char*() in the core
	 *
	 * @param other_value the internal representation of the bitstring.
	 * @param nof_bits the number of bits used.
	 * */
	public void setValue( final int[] other_value, final int nof_bits ) {
		bits_ptr = other_value;
		this.n_bits = nof_bits;
		clear_unused_bits();
	}

	/**
	 * Assigns the other value to this value.
	 * Overwriting the current content in the process.
	 *<p>
	 * operator= in the core.
	 *
	 * @param otherValue
	 *                the other value to assign.
	 * @return the new value object.
	 */
	public TitanBitString operator_assign(final TitanBitString_Element otherValue) {
		otherValue.must_bound("Assignment of an unbound bitstring element to a bitstring.");

		final boolean bitValue = otherValue.get_bit();
		clean_up();
		n_bits = 1;
		bits_ptr = new int[1];
		bits_ptr[0] = (int) (bitValue ? 1 : 0);

		return this;
	}


	/**
	 * Assigns the other value to this value.
	 * Overwriting the current content in the process.
	 *<p>
	 * operator= in the core.
	 *
	 * @param otherValue
	 *                the other value to assign.
	 * @return the new value object.
	 */
	public TitanBitString operator_assign(final TitanBitString otherValue) {
		otherValue.must_bound("Assignment of an unbound bitstring value.");

		if (otherValue != this) {
			clean_up();
			bits_ptr = TitanString_Utils.copy_integer_list(otherValue.bits_ptr);
			n_bits = otherValue.n_bits;
		}

		return this;
	}

	@Override
	public TitanBitString operator_assign(final Base_Type otherValue) {
		if (otherValue instanceof TitanBitString) {
			return operator_assign((TitanBitString)otherValue);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to bitstring", otherValue));
	}

	@Override
	public boolean is_bound() {
		return bits_ptr != null;
	}

	@Override
	public boolean is_value() {
		return is_bound();
	}

	/**
	 * Returns the number of elements, that is, the largest used index plus
	 * one and zero for the empty value.
	 *
	 * lengthof in the core
	 *
	 * @return the number of elements.
	 * */
	public TitanInteger lengthof() {
		must_bound("Performing lengthof operation on an unbound bitstring value.");

		return new TitanInteger(n_bits);
	}

	/**
	 * Checks if the current value is equivalent to the provided one.
	 *
	 * operator== in the core
	 *
	 * @param otherValue
	 *                the other value to check against.
	 * @return {@code true} if the values are equivalent.
	 */
	public boolean operator_equals(final TitanBitString otherValue) {
		must_bound("Unbound left operand of bitstring comparison.");
		otherValue.must_bound("Unbound right operand of bitstring comparison.");

		return n_bits == otherValue.n_bits && Arrays.equals(bits_ptr, otherValue.bits_ptr);
	}

	/**
	 * Checks if the current value is equivalent to the provided one.
	 *
	 * operator== in the core
	 *
	 * @param otherValue
	 *                the other value to check against.
	 * @return {@code true} if the values are equivalent.
	 */
	public boolean operator_equals(final TitanBitString_Element otherValue) {
		must_bound("Unbound left operand of bitstring comparison.");
		otherValue.must_bound("Unbound right operand of bitstring element comparison.");

		if (n_bits != 1) {
			return false;
		}

		return get_bit(0) == otherValue.get_bit();
	}

	@Override
	public boolean operator_equals(final Base_Type otherValue) {
		if (otherValue instanceof TitanBitString) {
			return operator_equals((TitanBitString)otherValue);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to bitstring", otherValue));
	}

	/**
	 * Checks if the current value is not equivalent to the provided one.
	 *
	 * operator!= in the core
	 *
	 * @param otherValue
	 *                the other value to check against.
	 * @return {@code true} if the values are not equivalent.
	 */
	public boolean operator_not_equals(final TitanBitString otherValue) {
		return !operator_equals(otherValue);
	}

	/**
	 * Checks if the current value is not equivalent to the provided one.
	 *
	 * operator!= in the core
	 *
	 * @param otherValue
	 *                the other value to check against.
	 * @return {@code true} if the values are not equivalent.
	 */
	public boolean operator_not_equals(final TitanBitString_Element otherValue) {
		return !operator_equals(otherValue);
	}

	@Override
	public void clean_up() {
		n_bits = 0;
		bits_ptr = null;
	}

	/**
	 * Concatenates the current bitstring with the bitstring received as a
	 * parameter.
	 *
	 * operator+ in the core.
	 *
	 * @param other_value
	 *                the other value to concatenate with.
	 * @return the new bitstring representing the concatenated value.
	 * */
	public TitanBitString operator_concatenate(final TitanBitString other_value) {
		must_bound("Unbound left operand of bitstring concatenation.");
		other_value.must_bound("Unbound right operand of bitstring element concatenation.");

		if (n_bits == 0) {
			return new TitanBitString(other_value);
		}
		if (other_value.n_bits == 0) {
			return new TitanBitString(this);
		}

		// the length of result
		final int resultBits = n_bits + other_value.n_bits;

		// the number of bytes used
		final int left_n_bytes = (n_bits + 7) / 8;
		final int right_n_bytes = (other_value.n_bits + 7) / 8;

		// the number of bits used in the last incomplete octet of the left operand
		final int last_octet_bits = n_bits % 8;

		final TitanBitString result = new TitanBitString();
		result.bits_ptr = new int[(resultBits + 7) / 8];
		result.n_bits = resultBits;
		System.arraycopy(bits_ptr, 0, result.bits_ptr, 0, bits_ptr.length);

		if (last_octet_bits != 0) {
			// non-trivial case: the length of left fragment is not
			// a multiply of 8 the bytes used in the result
			final int n_bytes = (resultBits + 7) / 8;
			// placing the bytes from the right fragment until the
			// result is filled
			for (int i = left_n_bytes; i < n_bytes; i++) {
				final Integer right_byte = other_value.bits_ptr[i - left_n_bytes];
				// finish filling the previous byte
				int temp = result.bits_ptr[i - 1] | right_byte << last_octet_bits;
				result.bits_ptr[i - 1] = temp & 0xFF;
				// start filling the actual byte
				temp = right_byte >> (8 - last_octet_bits);
				result.bits_ptr[i] = temp;
			}
			if (left_n_bytes + right_n_bytes > n_bytes) {
				// if the result data area is shorter than the two operands together
				// the last bits of right fragment were not placed into the result in the previous for loop
				final int temp = result.bits_ptr[n_bytes - 1] | other_value.bits_ptr[right_n_bytes - 1] << last_octet_bits;
				result.bits_ptr[n_bytes - 1] = temp & 0xFF;
			}
		} else {
			System.arraycopy(other_value.bits_ptr, 0, result.bits_ptr, bits_ptr.length, other_value.bits_ptr.length);
		}

		return result;
	}

	/**
	 * Concatenates the current bitstring with the bitstring element
	 * received as a parameter.
	 *
	 * operator+ in the core.
	 *
	 * @param other_value
	 *                the other value to concatenate with.
	 * @return the new bitstring representing the concatenated value.
	 * */
	public TitanBitString operator_concatenate(final TitanBitString_Element other_value) {
		must_bound("Unbound left operand of bitstring concatenation.");
		other_value.must_bound("Unbound right operand of bitstring element");

		final int n_bytes = (n_bits / 8) + 1;
		final TitanBitString ret_val = new TitanBitString();
		ret_val.bits_ptr = new int[n_bytes];
		ret_val.n_bits = n_bits + 1;
		System.arraycopy(bits_ptr, 0, ret_val.bits_ptr, 0, bits_ptr.length);

		ret_val.set_bit(n_bits, other_value.get_bit());
		ret_val.clear_unused_bits();

		return ret_val;
	}

	/**
	 * Creates a new bitstring with all bit inverted.
	 *
	 * operator~ in the core.
	 *
	 * @return the new bitstring with the inverted bits.
	 * */
	public TitanBitString not4b() {
		must_bound("Unbound bitstring operand of operator not4b.");

		final int n_bytes = (n_bits + 7) / 8;
		if (n_bytes == 0) {
			return new TitanBitString(this);
		}

		final TitanBitString result = new TitanBitString();
		result.n_bits = n_bits;
		result.bits_ptr = new int[(n_bits + 7) / 8];
		for (int i = 0; i < bits_ptr.length; i++) {
			result.bits_ptr[i] = ~bits_ptr[i] & 0xFF;
		}

		result.clear_unused_bits();

		return result;
	}

	/**
	 * Performs a bitwise and operation on this and the provided bitstring.
	 * The resulting value is 1 if both bits are set to 1,
	 *  otherwise the value for the resulting bit is 0.
	 * Both have to be the same length.
	 *
	 * operator& in the core.
	 *
	 * @param other_value
	 *                the other value.
	 * @return the resulting bitstring.
	 * */
	public TitanBitString and4b(final TitanBitString otherValue) {
		must_bound("Left operand of operator and4b is an unbound bitstring value.");
		otherValue.must_bound("Right operand of operator and4b is an unbound bitstring value.");

		if (n_bits != otherValue.n_bits) {
			throw new TtcnError("The bitstring operands of operator and4b must have the same length.");
		}
		if (n_bits == 0) {
			return new TitanBitString(this);
		}

		final int n_bytes = (n_bits + 7) / 8;
		final TitanBitString result = new TitanBitString();
		result.bits_ptr = new int[n_bytes];
		result.n_bits = n_bits;
		for (int i = 0; i < bits_ptr.length; i++) {
			result.bits_ptr[i] = bits_ptr[i] & otherValue.bits_ptr[i];
		}

		result.clear_unused_bits();

		return result;
	}

	/**
	 * Performs a bitwise and operation on this and the provided bitstring.
	 * The resulting value is 1 if both bits are set to 1,
	 *  otherwise the value for the resulting bit is 0.
	 * Both have to be the same length.
	 *
	 * operator& in the core.
	 *
	 * @param other_value
	 *                the other value.
	 * @return the resulting bitstring.
	 * */
	public TitanBitString and4b(final TitanBitString_Element otherValue) {
		must_bound("Left operand of operator and4b is an unbound bitstring value.");
		otherValue.must_bound("Right operand of operator and4b is an unbound bitstring element.");

		if (n_bits != 1) {
			throw new TtcnError("The bitstring operands of operator and4b must have the same length.");
		}

		final TitanBitString result = new TitanBitString();
		result.bits_ptr = new int[1];
		result.n_bits = 1;
		result.bits_ptr[0] = get_bit(0) && otherValue.get_bit() ? 1 : 0;

		return result;
	}

	/**
	 * Performs a bitwise or operation on this and the provided bitstring.
	 * the resulting value is 0 if both bits are set to 0,
	 *  otherwise the value for the resulting bit is 1.
	 * Both have to be the same length.
	 *
	 * operator| in the core.
	 *
	 * @param other_value
	 *                the other value.
	 * @return the resulting bitstring.
	 * */
	public TitanBitString or4b(final TitanBitString otherValue) {
		must_bound("Left operand of operator or4b is an unbound bitstring value.");
		otherValue.must_bound("Right operand of operator or4b is an unbound bitstring value.");

		if (n_bits != otherValue.n_bits) {
			throw new TtcnError("The bitstring operands of operator or4b must have the same length.");
		}
		if (n_bits == 0) {
			return new TitanBitString(this);
		}

		final int n_bytes = (n_bits + 7) / 8;
		final TitanBitString result = new TitanBitString();
		result.bits_ptr = new int[n_bytes];
		result.n_bits = n_bits;
		for (int i = 0; i < bits_ptr.length; i++) {
			result.bits_ptr[i] = bits_ptr[i] | otherValue.bits_ptr[i];
		}

		result.clear_unused_bits();

		return result;
	}

	/**
	 * Performs a bitwise or operation on this and the provided bitstring.
	 * the resulting value is 0 if both bits are set to 0,
	 *  otherwise the value for the resulting bit is 1.
	 * Both have to be the same length.
	 *
	 * operator| in the core.
	 *
	 * @param other_value
	 *                the other value.
	 * @return the resulting bitstring.
	 * */
	public TitanBitString or4b(final TitanBitString_Element otherValue) {
		must_bound("Left operand of operator or4b is an unbound bitstring value.");
		otherValue.must_bound("Right operand of operator or4b is an unbound bitstring element.");

		if (n_bits != 1) {
			throw new TtcnError("The bitstring operands of operator or4b must have the same length.");
		}

		final TitanBitString result = new TitanBitString();
		result.bits_ptr = new int[1];
		result.n_bits = 1;
		result.bits_ptr[0] = get_bit(0) || otherValue.get_bit() ? 1 : 0;

		return result;
	}

	/**
	 * Performs a bitwise xor operation on this and the provided bitstring.
	 * The resulting value is 0 if both bits are the same,
	 *  otherwise the value for the resulting bit is 1.
	 * Both have to be the same length.
	 *
	 * operator^ in the core.
	 *
	 * @param other_value
	 *                the other value.
	 * @return the resulting bitstring.
	 * */
	public TitanBitString xor4b(final TitanBitString otherValue) {
		must_bound("Left operand of operator xor4b is an unbound bitstring value.");
		otherValue.must_bound("Right operand of operator xor4b is an unbound bitstring value.");

		if (n_bits != otherValue.n_bits) {
			throw new TtcnError("The bitstring operands of operator xor4b must have the same length.");
		}
		if (n_bits == 0) {
			return new TitanBitString(this);
		}

		final int n_bytes = (n_bits + 7) / 8;
		final TitanBitString result = new TitanBitString();
		result.bits_ptr = new int[n_bytes];
		result.n_bits = n_bits;
		for (int i = 0; i < bits_ptr.length; i++) {
			result.bits_ptr[i] = bits_ptr[i] ^ otherValue.bits_ptr[i];
		}

		result.clear_unused_bits();

		return result;
	}

	/**
	 * Performs a bitwise xor operation on this and the provided bitstring.
	 * The resulting value is 0 if both bits are the same,
	 *  otherwise the value for the resulting bit is 1.
	 * Both have to be the same length.
	 *
	 * operator^ in the core.
	 *
	 * @param other_value
	 *                the other value.
	 * @return the resulting bitstring.
	 * */
	public TitanBitString xor4b(final TitanBitString_Element otherValue) {
		must_bound("Left operand of operator xor4b is an unbound bitstring value.");
		otherValue.must_bound("Right operand of operator xor4b is an unbound bitstring element.");

		if (n_bits != 1) {
			throw new TtcnError("The bitstring operands of operator xor4b must have the same length.");
		}

		final TitanBitString result = new TitanBitString();
		result.bits_ptr = new int[1];
		result.n_bits = 1;
		result.bits_ptr[0] = get_bit(0) ^ otherValue.get_bit() ? 1 : 0;

		return result;
	}

	/**
	 * Creates a new bitstring, that is the equivalent of the
	 * current one with its elements shifted to the left with the provided
	 * amount and zeros coming in from the right.
	 *
	 * operator<< in the core.
	 *
	 * @param shift_count
	 *                the number of characters to shift left.
	 * @return the new bitstring.
	 * */
	public TitanBitString shift_left(int shift_count) {
		must_bound("Unbound bitstring operand of shift left operator.");

		if (shift_count > 0) {
			if (n_bits == 0) {
				return new TitanBitString(this);
			}
			final int n_bytes = (n_bits + 7) / 8;
			clear_unused_bits();
			if (shift_count > n_bits) {
				shift_count = n_bits;
			}

			final int shift_bytes = shift_count / 8;
			final int shift_bits = shift_count % 8;
			final TitanBitString result = new TitanBitString();
			result.bits_ptr = new int[n_bytes];
			result.n_bits = n_bits;
			// result.addAll(bits_ptr);
			if (shift_bits != 0) {
				for (int byte_count = 0; byte_count < n_bytes - shift_bytes - 1; byte_count++) {
					result.bits_ptr[byte_count] = ((bits_ptr[byte_count + shift_bytes] >> shift_bits) | (bits_ptr[byte_count + shift_bytes
							+ 1] << (8 - shift_bits))) & 0xFF;
				}

				result.bits_ptr[n_bytes - shift_bytes - 1] = bits_ptr[n_bytes - 1] >> shift_bits;
			} else {
				for (int i = shift_bytes; i < n_bytes; i++) {
					result.bits_ptr[i - shift_bytes] = bits_ptr[i];
				}
			}
			for (int i = n_bytes - shift_bytes; i < n_bytes; i++) {
				result.bits_ptr[i] = 0;
			}

			result.clear_unused_bits();
			return result;
		} else if (shift_count == 0) {
			return new TitanBitString(this);
		} else {
			return this.shift_right(-shift_count);
		}
	}

	/**
	 * Creates a new bitstring, that is the equivalent of the
	 * current one with its elements shifted to the left with the provided
	 * amount and zeros coming in from the right.
	 *
	 * operator<< in the core.
	 *
	 * @param shift_count
	 *                the number of characters to shift left.
	 * @return the new bitstring.
	 * */
	public TitanBitString shift_left(final TitanInteger shift_count) {
		shift_count.must_bound("Unbound right operand of shift left operator.");

		return shift_left(shift_count.get_int());
	}

	/**
	 * Creates a new bitstring, that is the equivalent of the
	 * current one with its elements shifted to the right with the provided
	 * amount and zeros coming in from the left.
	 *
	 * operator>> in the core.
	 *
	 * @param shift_count
	 *                the number of characters to shift right.
	 * @return the new bitstring.
	 * */
	public TitanBitString shift_right(int shift_count) {
		must_bound("Unbound bitstring operand of shift right operator.");

		if (shift_count > 0) {
			if (n_bits == 0) {
				return new TitanBitString(this);
			}
			final int n_bytes = (n_bits + 7) / 8;
			clear_unused_bits();
			if (shift_count > n_bits) {
				shift_count = n_bits;
			}

			final int shift_bytes = shift_count / 8;
			final int shift_bits = shift_count % 8;
			final TitanBitString result = new TitanBitString();
			result.bits_ptr = new int[n_bytes];
			result.n_bits = n_bits;
			//result.addAll(bits_ptr);
			for (int i = 0; i < shift_bytes; i++) {
				result.bits_ptr[i] = 0;
			}
			if (shift_bits != 0) {
				result.bits_ptr[shift_bytes] = (bits_ptr[0] << shift_bits) & 0xFF;
				for (int byte_count = shift_bytes + 1; byte_count < n_bytes; byte_count++) {
					result.bits_ptr[byte_count] = (bits_ptr[byte_count - shift_bytes - 1] >> (8 - shift_bits))
							| (bits_ptr[byte_count - shift_bytes] << shift_bits) & 0xFF;
				}
			} else {
				for (int i = shift_bytes; i < n_bytes; i++) {
					result.bits_ptr[i] = bits_ptr[i - shift_bytes];
				}
			}

			result.clear_unused_bits();
			return result;
		} else if (shift_count == 0) {
			return new TitanBitString(this);
		} else {
			return this.shift_left(-shift_count);
		}
	}

	/**
	 * Creates a new bitstring, that is the equivalent of the
	 * current one with its elements shifted to the right with the provided
	 * amount and zeros coming in from the left.
	 *
	 * operator>> in the core.
	 *
	 * @param shift_count
	 *                the number of characters to shift right.
	 * @return the new bitstring.
	 * */
	public TitanBitString shift_right(final TitanInteger shift_count) {
		shift_count.must_bound("Unbound bitstring operand of shift left operator.");

		return shift_right(shift_count.get_int());
	}

	/**
	 * Creates a new bitstring, that is the equivalent of the
	 * current one with its elements rotated to the left with the provided
	 * amount.
	 *
	 * operator<<= in the core.
	 *
	 * @param rotate_count
	 *                the number of characters to rotate left.
	 * @return the new bitstring.
	 * */
	public TitanBitString rotate_left(int rotate_count) {
		must_bound("Unbound bitstring operand of rotate left operator.");

		if (n_bits == 0) {
			return new TitanBitString(this);
		}
		if (rotate_count >= 0) {
			rotate_count %= n_bits;
			if (rotate_count == 0) {
				return new TitanBitString(this);
			} else {
				return this.shift_left(rotate_count).or4b(this.shift_right(n_bits - rotate_count));
			}
		} else {
			return this.rotate_right(-rotate_count);
		}
	}

	/**
	 * Creates a new bitstring, that is the equivalent of the
	 * current one with its elements rotated to the left with the provided
	 * amount.
	 *
	 * operator<<= in the core.
	 *
	 * @param rotate_count
	 *                the number of characters to rotate left.
	 * @return the new bitstring.
	 * */
	public TitanBitString rotate_left(final TitanInteger rotate_count) {
		rotate_count.must_bound("Unbound right operand operand of rotate left operator.");

		return this.rotate_left(rotate_count.get_int());
	}

	/**
	 * Creates a new bitstring, that is the equivalent of the
	 * current one with its elements rotated to the right with the provided
	 * amount.
	 *
	 * operator>>= in the core.
	 *
	 * @param rotate_count
	 *                the number of characters to rotate right.
	 * @return the new bitstring.
	 * */
	public TitanBitString rotate_right(int rotate_count) {
		must_bound("Unbound bitstring operand of rotate right operator.");

		if (n_bits == 0) {
			return new TitanBitString(this);
		}
		if (rotate_count >= 0) {
			rotate_count %= n_bits;
			if (rotate_count == 0) {
				return new TitanBitString(this);
			} else {
				return ((this.shift_right(rotate_count)).or4b(this.shift_left(n_bits - rotate_count)));
			}
		} else {
			return this.rotate_left(-rotate_count);
		}
	}

	/**
	 * Creates a new bitstring, that is the equivalent of the
	 * current one with its elements rotated to the right with the provided
	 * amount.
	 *
	 * operator>>= in the core.
	 *
	 * @param rotate_count
	 *                the number of characters to rotate right.
	 * @return the new bitstring.
	 * */
	public TitanBitString rotate_right(final TitanInteger rotate_count) {
		rotate_count.must_bound("Unbound right operand operand of rotate left operator.");

		return this.rotate_right(rotate_count.get_int());
	}

	/**
	 * Gives access to the given element. Indexing begins from zero.
	 * Over-indexing by 1 extends the bitstring.
	 *
	 * operator[] in the core.
	 *
	 * @param index_value
	 *            the index of the element to return.
	 * @return the element at the specified position in this bitstring
	 * */
	public TitanBitString_Element get_at(final int index_value) {
		if (bits_ptr == null && index_value == 0) {
			bits_ptr = new int[1];
			n_bits = 1;
			return new TitanBitString_Element(false, this, 0);
		} else {
			must_bound("Accessing an element of an unbound bitstring value.");

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

				return new TitanBitString_Element(false, this, index_value);
			} else {
				return new TitanBitString_Element(true, this, index_value);
			}
		}
	}

	/**
	 * Gives access to the given element. Indexing begins from zero.
	 * Over-indexing by 1 extends the bitstring.
	 *
	 * operator[] in the core.
	 *
	 * @param index_value
	 *            the index of the element to return.
	 * @return the element at the specified position in this bitstring
	 * */
	public TitanBitString_Element get_at(final TitanInteger index_value) {
		index_value.must_bound("Indexing a bitstring value with an unbound integer value.");

		return get_at(index_value.get_int());
	}

	/**
	 * Gives read-only access to the given element.
	 *
	 * Index underflow and overflow causes dynamic test case error.
	 *
	 * const operator[] const in the core.
	 *
	 * @param index
	 *            the index of the element to return.
	 * @return the element at the specified position in this bitstring
	 * */
	public final TitanBitString_Element constGet_at(final int index_value) {
		must_bound("Accessing an element of an unbound bitstring value.");

		if (index_value < 0) {
			throw new TtcnError("Accessing an bitstring element using a negative index (" + index_value + ").");
		}

		if (index_value >= n_bits) {
			throw new TtcnError("Index overflow when accessing a bitstring element: The index is " + index_value +
					", but the string has only " + n_bits + " bits.");
		}

		return new TitanBitString_Element(true, this, index_value);
	}

	/**
	 * Gives read-only access to the given element.
	 *
	 * Index underflow and overflow causes dynamic test case error.
	 *
	 * const operator[] const in the core.
	 *
	 * @param index
	 *            the index of the element to return.
	 * @return the element at the specified position in this bitstring
	 * */
	public final TitanBitString_Element constGet_at(final TitanInteger index_value) {
		index_value.must_bound("Indexing a bitstring value with an unbound integer value.");

		return constGet_at(index_value.get_int());
	}

	@Override
	public void log() {
		if (bits_ptr != null) {
			TTCN_Logger.log_char('\'');
			for (int bit_count = 0; bit_count < n_bits; bit_count++) {
				TTCN_Logger.log_char(get_bit(bit_count) ? '1' : '0');
			}
			TTCN_Logger.log_event_str("'B");
		} else {
			TTCN_Logger.log_event_unbound();
		}
	}

	@Override
	/** {@inheritDoc} */
	public void set_param(Module_Parameter param) {
		param.basic_check(Module_Parameter.basic_check_bits_t.BC_VALUE.getValue() | Module_Parameter.basic_check_bits_t.BC_LIST.getValue(), "bitstring value");

		// Originally RT2
		if (param.get_type() == Module_Parameter.type_t.MP_Reference) {
			param = param.get_referenced_param().get();
		}

		switch (param.get_type()) {
		case MP_Bitstring:
			switch (param.get_operation_type()) {
			case OT_ASSIGN:
				clean_up();
				n_bits = param.get_string_size();
				bits_ptr = (int[]) param.get_string_data();
				clear_unused_bits();
				break;
			case OT_CONCAT: {
				final TitanBitString temp = new TitanBitString((int[]) param.get_string_data(), param.get_string_size());
				if (is_bound()) {
					this.operator_assign(this.operator_concatenate(temp));
				} else {
					this.operator_assign(temp);
				}
				break;
			}
			default:
				throw new TtcnError("Internal error: TitanBitString.set_param()");
			}
			break;
		case MP_Expression:
			if (param.get_expr_type() == expression_operand_t.EXPR_CONCATENATE) {
				final TitanBitString operand1 = new TitanBitString();
				final TitanBitString operand2 = new TitanBitString();
				operand1.set_param(param.get_operand1());
				operand2.set_param(param.get_operand2());
				if (param.get_operation_type() == operation_type_t.OT_CONCAT) {
					this.operator_assign(this.operator_concatenate(operand1).operator_concatenate(operand2));
				} else {
					this.operator_assign(operand1.operator_concatenate(operand2));
				}
			} else {
				param.expr_type_error("a bitstring");
			}
			break;
		default:
			param.expr_type_error("a bitstring value");
			break;
		}
	}

	@Override
	/** {@inheritDoc} */
	public Module_Parameter get_param(final Module_Param_Name param_name) {
		if (!is_bound()) {
			return new Module_Param_Unbound();
		}
		return new Module_Param_Bitstring(this);
	}

	@Override
	public boolean is_present() {
		return is_bound();
	}

	/**
	 * Do not use this function!<br>
	 * It is provided by Java and currently used for debugging.
	 * But it is not part of the intentionally provided interface,
	 *   and so can be changed without notice.
	 * <p>
	 * JAVA DESCRIPTION:
	 * <p>
	 * {@inheritDoc}
	 *  */
	@Override
	public String toString() {
		final StringBuilder result = new StringBuilder(n_bits + 2);
		result.append('\'');
		for (int i = 0; i < n_bits; i++) {
			result.append(get_bit(i) ? '1' : '0');
		}
		result.append("\'B");

		return result.toString();
	}

	@Override
	/** {@inheritDoc} */
	public void encode_text(final Text_Buf text_buf) {
		must_bound("Text encoder: Encoding an unbound bitstring value.");

		text_buf.push_int(n_bits);
		if (n_bits > 0) {
			byte[] temp = new byte[bits_ptr.length];
			for (int i = 0; i < bits_ptr.length; i++) {
				temp[i] = (byte) bits_ptr[i];
			}
			text_buf.push_raw(temp);
		}
	}

	@Override
	/** {@inheritDoc} */
	public void decode_text(final Text_Buf text_buf) {
		clean_up();

		n_bits = text_buf.pull_int().get_int();
		if (n_bits < 0) {
			throw new TtcnError("Text decoder: Invalid length was received for a bitstring.");
		}

		final int bytes = (n_bits + 7) / 8;
		bits_ptr = new int[bytes];
		if (n_bits > 0) {
			final byte[] temp = new byte[bytes];
			text_buf.pull_raw(bytes, temp);
			for (int i = 0; i < bytes; i++) {
				bits_ptr[i] = (int) temp[i];
			}
			clear_unused_bits();
		}
	}

	public int get_n_bits() {
		return n_bits;
	}

	@Override
	/** {@inheritDoc} */
	public void encode(final TTCN_Typedescriptor p_td, final TTCN_Buffer p_buf, final coding_type p_coding, final int flavour) {
		switch (p_coding) {
		case CT_RAW: {
			final TTCN_EncDec_ErrorContext errorContext = new TTCN_EncDec_ErrorContext("While RAW-encoding type '%s': ", p_td.name);
			try {
				if (p_td.raw == null) {
					TTCN_EncDec_ErrorContext.error_internal("No RAW descriptor available for type '%s'.", p_td.name);
				}

				final RAW_enc_tr_pos tree_position = new RAW_enc_tr_pos(0, null);
				final RAW_enc_tree root = new RAW_enc_tree(true, null, tree_position, 1, p_td.raw);
				RAW_encode(p_td, root);
				root.put_to_buf(p_buf);
			} finally {
				errorContext.leave_context();
			}
			break;
		}
		case CT_JSON: {
			final TTCN_EncDec_ErrorContext errorContext = new TTCN_EncDec_ErrorContext("While JSON-encoding type '%s': ", p_td.name);
			try {
				if(p_td.json == null) {
					TTCN_EncDec_ErrorContext.error_internal("No JSON descriptor available for type '%s'.", p_td.name);
				}

				final JSON_Tokenizer tok = new JSON_Tokenizer(flavour != 0);
				JSON_encode(p_td, tok);
				final StringBuilder temp = tok.get_buffer();
				for (int i = 0; i < temp.length(); i++) {
					final int temp2 = temp.charAt(i);
					p_buf.put_c((byte)temp2);
				}
			} finally {
				errorContext.leave_context();
			}
			break;
		}
		default:
			throw new TtcnError(MessageFormat.format("Unknown coding method requested to encode type `{0}''", p_td.name));
		}
	}

	@Override
	/** {@inheritDoc} */
	public void decode(final TTCN_Typedescriptor p_td, final TTCN_Buffer p_buf, final coding_type p_coding, final int flavour) {
		switch (p_coding) {
		case CT_RAW: {
			final TTCN_EncDec_ErrorContext errorContext = new TTCN_EncDec_ErrorContext("While RAW-decoding type '%s': ", p_td.name);
			try {
				if (p_td.raw == null) {
					TTCN_EncDec_ErrorContext.error_internal("No RAW descriptor available for type '%s'.", p_td.name);
				}

				final raw_order_t order = p_td.raw.top_bit_order == top_bit_order_t.TOP_BIT_LEFT ? raw_order_t.ORDER_LSB : raw_order_t.ORDER_MSB;
				if (RAW_decode(p_td, p_buf, p_buf.get_len() * 8, order) < 0) {
					TTCN_EncDec_ErrorContext.error(TTCN_EncDec.error_type.ET_INCOMPL_MSG, "Can not decode type '%s', because invalid or incomplete message was received", p_td.name);
				}
			} finally {
				errorContext.leave_context();
			}
			break;
		}
		case CT_JSON: {
			final TTCN_EncDec_ErrorContext errorContext = new TTCN_EncDec_ErrorContext("While JSON-decoding type '%s': ", p_td.name);
			try {
				if(p_td.json == null) {
					TTCN_EncDec_ErrorContext.error_internal("No JSON descriptor available for type '%s'.", p_td.name);
				}

				final byte[] data = p_buf.get_data();
				final char[] temp = new char[data.length];
				for (int i = 0; i < data.length; i++) {
					temp[i] = (char)data[i];
				}
				final JSON_Tokenizer tok = new JSON_Tokenizer(new String(temp), p_buf.get_len());
				if(JSON_decode(p_td, tok, false) < 0) {
					TTCN_EncDec_ErrorContext.error(TTCN_EncDec.error_type.ET_INCOMPL_MSG,
							"Can not decode type '%s', because invalid or incomplete message was received", p_td.name);
				}
				p_buf.set_pos(tok.get_buf_pos());
			} finally {
				errorContext.leave_context();
			}
			break;
		}
		default:
			throw new TtcnError(MessageFormat.format("Unknown coding method requested to decode type `{0}''", p_td.name));
		}
	}

	@Override
	/** {@inheritDoc} */
	public int RAW_encode(final TTCN_Typedescriptor p_td, final RAW_enc_tree myleaf) {
		if (!is_bound()) {
			TTCN_EncDec_ErrorContext.error(error_type.ET_UNBOUND, "Encoding an unbound value.");
		}

		final TTCN_EncDec_ErrorContext errorcontext = new TTCN_EncDec_ErrorContext();
		try {
			int bl = n_bits;
			int align_length = p_td.raw.fieldlength != 0 ? p_td.raw.fieldlength - bl : 0;
			if ((bl + align_length) < n_bits) {
				TTCN_EncDec_ErrorContext.error(error_type.ET_LEN_ERR, "There is no sufficient bits to encode '%s':", p_td.name);
				bl = p_td.raw.fieldlength;
				align_length = 0;
			}
			myleaf.data_array = new byte[bits_ptr.length];
			for (int i = 0; i < bits_ptr.length; i++) {
				myleaf.data_array[i] = (byte)bits_ptr[i];
			}
			boolean orders = p_td.raw.byteorder == raw_order_t.ORDER_MSB;
			if (p_td.raw.bitorderinfield == raw_order_t.ORDER_LSB) {
				orders = !orders;
			}
			myleaf.coding_par.byteorder = orders ? raw_order_t.ORDER_MSB : raw_order_t.ORDER_LSB;
			orders = p_td.raw.bitorderinoctet == raw_order_t.ORDER_MSB;
			if (p_td.raw.bitorderinfield == raw_order_t.ORDER_LSB) {
				orders = !orders;
			}
			myleaf.coding_par.bitorder = orders ? raw_order_t.ORDER_MSB : raw_order_t.ORDER_LSB;
			myleaf.coding_par.csn1lh = p_td.raw.csn1lh;
			if (p_td.raw.endianness == raw_order_t.ORDER_MSB) {
				myleaf.align = align_length;
			} else {
				myleaf.align = -align_length;
			}

			return myleaf.length = bl + align_length;
		} finally {
			errorcontext.leave_context();
		}
	}

	@Override
	/** {@inheritDoc} */
	public int RAW_decode(final TTCN_Typedescriptor p_td, final TTCN_Buffer buff, final int limit, final raw_order_t top_bit_ord) {
		return RAW_decode(p_td, buff, limit, top_bit_ord, false, -1, true, null);
	}

	@Override
	/** {@inheritDoc} */
	public int RAW_decode(final TTCN_Typedescriptor p_td, final TTCN_Buffer buff, int limit, final raw_order_t top_bit_ord, final boolean no_err, final int sel_field, final boolean first_call, final RAW_Force_Omit force_omit) {
		final int prepaddlength = buff.increase_pos_padd(p_td.raw.prepadding);
		limit -= prepaddlength;
		int decode_length = p_td.raw.fieldlength == 0 ? limit : p_td.raw.fieldlength;
		final TTCN_EncDec_ErrorContext errorcontext = new TTCN_EncDec_ErrorContext();
		try {
			if (p_td.raw.fieldlength > limit
					|| p_td.raw.fieldlength > buff.unread_len_bit()) {
				if (no_err) {
					return -TTCN_EncDec.error_type.ET_LEN_ERR.ordinal();
				}

				TTCN_EncDec_ErrorContext.error(error_type.ET_LEN_ERR, "There is not enough bits in the buffer to decode type %s.", p_td.name);
				decode_length = limit > (int) buff.unread_len_bit() ? buff.unread_len_bit() : limit;
			}
			clean_up();
			n_bits = decode_length;
			bits_ptr = new int[(decode_length + 7) / 8];
			final RAW_coding_par cp = new RAW_coding_par();
			boolean orders = p_td.raw.bitorderinoctet == raw_order_t.ORDER_MSB;
			if (p_td.raw.bitorderinfield == raw_order_t.ORDER_LSB) {
				orders = !orders;
			}
			cp.bitorder = orders ? raw_order_t.ORDER_MSB : raw_order_t.ORDER_LSB;
			orders = p_td.raw.byteorder == raw_order_t.ORDER_MSB;
			if (p_td.raw.bitorderinfield == raw_order_t.ORDER_LSB) {
				orders = !orders;
			}
			cp.byteorder = orders ? raw_order_t.ORDER_MSB : raw_order_t.ORDER_LSB;
			cp.fieldorder = p_td.raw.fieldorder;
			cp.hexorder = raw_order_t.ORDER_LSB;
			cp.csn1lh = p_td.raw.csn1lh;
			final byte[] tmp_bits = new byte[bits_ptr.length];
			buff.get_b(decode_length, tmp_bits, cp, top_bit_ord);
			for (int i = 0; i < tmp_bits.length; i++) {
				bits_ptr[i] = (int) (tmp_bits[i] & 0xFF);
			}
			if (p_td.raw.length_restrition != -1
					&& decode_length > p_td.raw.length_restrition) {
				n_bits = p_td.raw.length_restrition;
				if (p_td.raw.endianness == raw_order_t.ORDER_LSB) {
					if ((decode_length - n_bits) % 8 != 0) {
						final int bound = (decode_length - n_bits) % 8;
						final int maxindex = (decode_length - 1) / 8;
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
		} finally {
			errorcontext.leave_context();
		}

		return decode_length + prepaddlength;
	}

	@Override
	/** {@inheritDoc} */
	public int JSON_encode(final TTCN_Typedescriptor p_td, final JSON_Tokenizer p_tok, final boolean p_parent_is_map) {
		if (!is_bound()) {
			TTCN_EncDec_ErrorContext.error(TTCN_EncDec.error_type.ET_UNBOUND, "Encoding an unbound bitstring value.");
			return -1;
		}

		final StringBuilder tmp_str = new StringBuilder();
		tmp_str.append('\"');
		for (int i = 0; i < n_bits; ++i) {
			tmp_str.append( get_bit(i) ? '1' : '0' );
		}
		tmp_str.append('\"');
		final int enc_len = p_tok.put_next_token(json_token_t.JSON_TOKEN_STRING, tmp_str.toString());
		return enc_len;
	}

	@Override
	/** {@inheritDoc} */
	public int JSON_decode(final TTCN_Typedescriptor p_td, final JSON_Tokenizer p_tok, final boolean p_silent, final boolean p_parent_is_map, final int p_chosen_field) {
		final AtomicReference<json_token_t> token = new AtomicReference<json_token_t>(json_token_t.JSON_TOKEN_NONE);
		final StringBuilder value = new StringBuilder();
		final AtomicInteger value_len = new AtomicInteger(0);
		boolean error = false;
		if (p_td.json.getActualDefaultValue() != null && 0 == p_tok.get_buffer_length()) {
			operator_assign(p_td.json.getActualDefaultValue());

			return 0;
		}

		final int dec_len = p_tok.get_next_token(token, value, value_len);
		if (json_token_t.JSON_TOKEN_ERROR == token.get()) {
			if(!p_silent) {
				TTCN_EncDec_ErrorContext.error(TTCN_EncDec.error_type.ET_INVAL_MSG, JSON.JSON_DEC_BAD_TOKEN_ERROR, "");
			}
			return JSON.JSON_ERROR_FATAL;
		} else if (json_token_t.JSON_TOKEN_STRING == token.get()) {
			if (value_len.get() >= 2 && value.charAt(0) == '\"' && value.charAt(value_len.get() - 1) == '\"') {
				// The default value doesn't have quotes around it
				final String valueWithoutQuotes = value.substring(1, value.length() - 1);
				value.setLength(0);
				value.append( valueWithoutQuotes );
				value_len.set(value.length());

				// White spaces are ignored, so the resulting bitstring might be shorter
				// than the extracted JSON string
				int bits = value_len.get();
				for (int i = 0; i < value_len.get(); ++i) {
					if (value.charAt(i) == ' ') {
						--bits;
					} else if (value.charAt(i) != '0' && value.charAt(i) != '1') {
						if (value.charAt(i) == '\\' && i + 1 < value_len.get() &&
								(value.charAt(i+1) == 'n' || value.charAt(i+1) == 'r' || value.charAt(i+1) == 't')) {
							// Escaped white space character
							++i;
							bits -= 2;
						} else {
							error = true;
							break;
						}
					}
				}
				if (!error) {
					init_struct(bits);
					int bit_index = 0;
					for (int i = 0; i < value_len.get(); ++i) {
						if (value.charAt(i) == '0' || value.charAt(i) == '1') {
							set_bit(bit_index, value.charAt(i) - '0' != 0);
							++bit_index;
						}
					}
				}
			} else {
				error = true;
			}
		} else {
			return JSON.JSON_ERROR_INVALID_TOKEN;
		}

		if (error) {
			if(!p_silent) {
				TTCN_EncDec_ErrorContext.error(TTCN_EncDec.error_type.ET_INVAL_MSG, JSON.JSON_DEC_FORMAT_ERROR, "string", "bitstring");
			}

			return JSON.JSON_ERROR_FATAL;
		}

		return dec_len;
	}

	private void init_struct(final int n_bits) {
		if (n_bits < 0) {
			bits_ptr = null;
			this.n_bits = 0;
			throw new TtcnError("Initializing a bitstring with a negative length.");
		} else if (n_bits == 0) {
			// This will represent the empty strings so they won't need allocated
			// memory, this delays the memory allocation until it is really needed.
			bits_ptr = new int[0];
			this.n_bits = 0;
		} else {
			bits_ptr = new int[(n_bits + 7) / 8];
			this.n_bits = n_bits;
		}
	}

	/**
	 * This static function is used to help with implicit conversion, where
	 * there is no better to way.
	 * <p>
	 * This particular function can be easily optimized away in during
	 * execution.
	 *
	 * @param otherValue
	 *                the other value to convert into a TitanBitString.
	 * @return the converted value.
	 * */
	public static TitanBitString convert_to_BitString(final TitanBitString otherValue) {
		return otherValue;
	}

	/**
	 * This static function is used to help with implicit conversion, where
	 * there is no better to way.
	 *
	 * @param otherValue
	 *                the other value to convert into a TitanBitString.
	 * @return the converted value.
	 * */
	public static TitanBitString convert_to_BitString(final TitanBitString_Element otherValue) {
		return new TitanBitString(otherValue);
	}
}
