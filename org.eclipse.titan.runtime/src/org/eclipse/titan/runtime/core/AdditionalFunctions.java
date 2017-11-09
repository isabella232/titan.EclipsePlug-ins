/******************************************************************************
 * Copyright (c) 2000-2017 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Additional (predefined) functions
 * 
 * @author Kristof Szabados
 * @author Gergo Ujhelyi
 * 
 * originally in Addfunc.{hh,cc}
 * 
 * FIXME implement rest
 */
public final class AdditionalFunctions {

	private static enum str2intState { S_INITIAL, S_FIRST, S_ZERO, S_MORE, S_END, S_ERR };
	private static enum str2floatState { S_INITIAL, S_FIRST_M, S_ZERO_M, S_MORE_M, S_FIRST_F, S_MORE_F,
		S_INITIAL_E, S_FIRST_E, S_ZERO_E, S_MORE_E, S_END, S_ERR }

	private AdditionalFunctions() {
		//intentionally private to disable instantiation
	}

	private static byte charToHexDigit(final char c) {
		if (c >= '0' && c <= '9') {
			return (byte) (c - '0');
		} else if (c >= 'A' && c <= 'F') {
			return (byte) (c - 'A' + 10);
		} else if (c >= 'a' && c <= 'f') {
			return (byte) (c - 'a' + 10);
		} else {
			return (byte) 0xFF;
		}
	}

	// C.1 - int2char
	public static TitanCharString int2char(final int value) {
		if (value < 0 || value > 127) {
			throw new TtcnError(MessageFormat.format("The argument of function int2char() is {0}, which is outside the allowed range 0 .. 127.", value));
		}

		return new TitanCharString(String.valueOf(Character.toChars(value)[0]));
	}

	public static TitanCharString int2char(final TitanInteger value) {
		value.mustBound("The argument of function int2char() is an unbound integer value.");

		final int ivt = value.getInt();
		if (ivt < 0 || ivt > 127) {
			throw new TtcnError(MessageFormat.format("The argument of function int2char() is {0}, which is outside the allowed range 0 .. 127.", value));
		}

		return new TitanCharString(String.valueOf(Character.toChars(ivt)[0]));
	}

	// C.2 - int2unichar

	public static TitanUniversalCharString int2unichar(final int value) {
		if (value < 0 || value > Integer.MAX_VALUE) {
			throw new TtcnError(MessageFormat.format("The argument of function int2unichar() is {0}, which outside the allowed range 0 .. 2147483647.", value));
		}

		return new TitanUniversalCharString(Character.toChars(value >> 24)[0],Character.toChars((value >> 16) & 0xFF)[0],Character.toChars((value >> 8) & 0xFF)[0],Character.toChars(value & 0xFF)[0]);
	}

	public static TitanUniversalCharString int2unichar(final TitanInteger value) {
		value.mustBound("The argument of function int2unichar() is an unbound integer value.");

		final int ivt = value.getInt();
		if (ivt < 0 || ivt > Integer.MAX_VALUE) {
			throw new TtcnError(MessageFormat.format("The argument of function int2unichar() is {0}, which outside the allowed range 0 .. 2147483647.", value));
		}

		return int2unichar(ivt);
	}

	// C.3 - int2bit
	public static TitanBitString int2bit(final int value, final int length) {
		return int2bit(new TitanInteger(value), length);
	}

	public static TitanBitString int2bit(final int value, final TitanInteger length) {
		length.mustBound("The second argument (length) of function int2bit() is an unbound integer value.");

		return int2bit(value, length.getInt());
	}

	public static TitanBitString int2bit(final TitanInteger value, final int length) {
		value.mustBound("The first argument (value) of function int2bit() is an unbound integer value.");

		if (value.isLessThan(0)) {
			throw new TtcnError(MessageFormat.format("The first argument (value) of function int2bit() is a negative integer value: {0}.", value));
		}
		if (length < 0) {
			throw new TtcnError(MessageFormat.format("The second argument (length) of function int2bit() is a negative integer value: {0}.", length));
		}

		if (value.isNative()) {
			int tempValue = value.getInt();
			final ArrayList<Byte> bits_ptr = new ArrayList<Byte>((length + 7) / 8);
			for (int i = 0; i < (length + 7) / 8; i++) {
				bits_ptr.add(Byte.valueOf((byte)0));
			}
			for (int i = length - 1; tempValue != 0 && i >= 0; i--) {
				if((tempValue & 1) > 0) {
					final int temp = bits_ptr.get(i / 8) | (1 << (i % 8));
					bits_ptr.set(i / 8, Byte.valueOf((byte) temp));
				}
				tempValue = tempValue >> 1;
			}
			if (tempValue != 0) {
				int i = 0;
				while (tempValue != 0) {
					tempValue = tempValue >> 1;
					i++;
				}
				throw new TtcnError(MessageFormat.format("The first argument of function int2bit(), which is {0}, does not fit in {1} bit{2}, needs at least {3}.", value, length, length > 1 ? "s" : "", length + i));
			}

			return new TitanBitString(bits_ptr, length);
		} else {
			BigInteger tempValue = value.getBigInteger();
			final ArrayList<Byte> bits_ptr = new ArrayList<Byte>((length + 7) / 8);
			for (int i = 0; i < (length + 7) / 8; i++) {
				bits_ptr.add(Byte.valueOf((byte)0));
			}
			for (int i = length - 1; tempValue.compareTo(BigInteger.ZERO) == 1 && i >= 0; i--) {
				if((tempValue.and(BigInteger.ONE)).compareTo(BigInteger.ZERO) == 1) {
					final int temp = bits_ptr.get(i / 8) | (1 << (i % 8));
					bits_ptr.set(i / 8, Byte.valueOf((byte) temp));
				}
				tempValue = tempValue.shiftRight(1);
			}
			if (tempValue.compareTo(BigInteger.ZERO) != 0) {
				int i = 0;
				while (tempValue.compareTo(BigInteger.ZERO) == 1) {
					tempValue = tempValue.shiftRight(1);
					i++;
				}
				throw new TtcnError(MessageFormat.format("The first argument of function int2bit(), which is {0}, does not fit in {1} bit{2}, needs at least {3}.", value, length, length > 1 ? "s" : "", length + i));
			}

			return new TitanBitString(bits_ptr, length);
		}
	}

	public static TitanBitString int2bit(final TitanInteger value, final TitanInteger length) {
		value.mustBound("The first argument (value) of function int2bit() is an unbound integer value.");
		length.mustBound("The second argument (length) of function int2bit() is an unbound integer value.");

		return int2bit(value, length.getInt());
	}

	// C.4 - int2hex
	public static TitanHexString int2hex(final int value, final int length) {
		return int2hex(new TitanInteger(value), length);
	}

	public static TitanHexString int2hex(final int value, final TitanInteger length) {
		length.mustBound("The second argument (length) of function int2hex() is an unbound integer value.");

		return int2hex(value, length.getInt());
	}

	public static TitanHexString int2hex(final TitanInteger value, final int length) {
		value.mustBound("The first argument (value) of function int2hex() is an unbound integer value.");

		if (value.isLessThan(0)) {
			throw new TtcnError(MessageFormat.format("The first argument (value) of function int2hex() is a  negative integer value: {0}.", value));
		}
		if (length < 0) {
			throw new TtcnError(MessageFormat.format("The second argument (length) of function int2hex() is a negative integer value: {0}.", length));
		}
		if (value.isNative()) {
			int tmp_value = value.getInt();
			final ArrayList<Byte> nibbles_ptr = new ArrayList<Byte>(length);
			for (int i = 0; i < length; i++) {
				nibbles_ptr.add((byte) 0);
			}
			for (int i = length - 1; i >= 0; i--) {
				nibbles_ptr.set(i, (byte) (tmp_value & 0xF));
				tmp_value = tmp_value >> 4;
			}

			if (tmp_value != 0) {
				int i = 0;
				while (tmp_value != 0) {
					tmp_value = tmp_value >> 4;
					i++;
				}
				throw new TtcnError(MessageFormat.format("The first argument of function int2hex(), which is {0}, does not fit in {1} hexadecimal digit{2}, needs at least {3}.", value, length, length > 1 ? "s" : "", length + i));
			}
			return new TitanHexString(nibbles_ptr);
		} else {
			BigInteger tmp_value = value.getBigInteger();
			final ArrayList<Byte> nibbles_ptr = new ArrayList<Byte>(length);
			for (int i = 0; i < length; i++) {
				nibbles_ptr.add((byte) 0);
			}
			for (int i = length - 1; i >= 0; i--) {
				final BigInteger temp = tmp_value.and(BigInteger.valueOf(0xF));
				nibbles_ptr.set(i, temp.byteValue());
				tmp_value = tmp_value.shiftRight(4);
			}
			tmp_value.shiftRight(4);//TODO check: does not seem to do anything
			if (tmp_value.compareTo(BigInteger.ZERO) != 0) {
				int i = 0;
				while (tmp_value.compareTo(BigInteger.ZERO) != 0) {
					tmp_value = tmp_value.shiftRight(4);
					i++;
				}
				throw new TtcnError(MessageFormat.format("The first argument of function int2hex(), which is {0}, does not fit in {1} hexadecimal digit{2}, needs at least {3}.", value, length, length > 1 ? "s" : "", length + i));
			}
			return new TitanHexString(nibbles_ptr);
		}
	}

	public static TitanHexString int2hex(final TitanInteger value, final TitanInteger length) {
		value.mustBound("The first argument (value) of function int2hex() is an unbound integer value.");
		length.mustBound("The second argument (length) of function int2hex() is an unbound integer value.");

		return int2hex(value, length.getInt());
	}

	// C.5 - int2oct
	public static TitanOctetString int2oct(final int value, final int length) {
		if (value < 0) {
			throw new TtcnError(MessageFormat.format("The first argument (value) of function int2oct() is a negative integer value:", value));
		}
		if (length < 0) {
			throw new TtcnError(MessageFormat.format("The second argument (length) of function int2oct() is a negative integer value:", length));
		}
		final List<Character> octets_ptr = new ArrayList<Character>(length);
		for (int i = 0; i < length; i++) {
			octets_ptr.add((char) 0);
		}
		int tmp_value = value;
		for (int i = length - 1; i >= 0; i--) {
			octets_ptr.set(i, (char) (tmp_value & 0xFF));
			tmp_value = tmp_value >> 8;
		}
		if (tmp_value != 0) {
			throw new TtcnError(MessageFormat.format("The first argument of function int2oct(), which is {0}, does not fit in {1} octet{2}.", value, length, length > 1 ? "s" :""));
		}
		return new TitanOctetString(octets_ptr);
	}

	public static TitanOctetString int2oct(final int value, final TitanInteger length) {
		length.mustBound("The second argument (length) of function int2oct() is an unbound integer value.");

		return int2oct(value, length.getInt());
	}

	public static TitanOctetString int2oct(final TitanInteger value, final int length) {
		value.mustBound("The first argument (value) of function int2oct() is an unbound integer value.");

		if (value.isNative()) {
			return int2oct(value.getInt(), length);
		} else {
			BigInteger tmp_val = value.getBigInteger();
			if (value.isLessThan(0)) {
				throw new TtcnError(MessageFormat.format("The first argument (value) of function int2oct() is a negative integer value: {0}.", value));
			}
			if (length < 0) {
				throw new TtcnError(MessageFormat.format("The second argument (length) of function int2oct() is a negative integer value: {0}.", length));
			}
			if ((tmp_val.bitCount() + 7) / 4 < length) {
				throw new TtcnError(MessageFormat.format("The first argument of function int2oct(), which is {0}, does not fit in {1} octet{2}.", (tmp_val.bitCount() + 7) / 4, length, length > 1 ? "s" : ""));
			}
			final List<Character> octets_ptr = new ArrayList<Character>(length);
			for (int i = 0; i < length; i++) {
				octets_ptr.add((char) 0);
			}
			for (int i = length - 1; i >= 0; i--) {
				octets_ptr.set(i, (char) (tmp_val.and(new BigInteger("255")).intValue()));
				tmp_val = tmp_val.shiftRight(8);
			}
			return new TitanOctetString(octets_ptr);
		}
	}

	public static TitanOctetString int2oct(final TitanInteger value, final TitanInteger length) {
		value.mustBound("The first argument (value) of function int2oct() is an unbound integer value.");
		length.mustBound("The second argument (length) of function int2oct() is an unbound integer value.");

		if (value.isNative()) {
			return int2oct(value.getInt(), length.getInt());
		}
		return int2oct(value, length.getInt());
	}

	// C.6 - int2str
	public static TitanCharString int2str(final int value) {
		return new TitanCharString(Integer.valueOf(value).toString());
	}

	public static TitanCharString int2str(final TitanInteger value) {
		value.mustBound("The argument of function int2str() is an unbound integer value.");

		if (value.isNative()) {
			return int2str(value.getInt());
		}
		return new TitanCharString(value.getBigInteger().toString());
	}

	// C.7 - int2float
	public static TitanFloat int2float(final int value) {
		return new TitanFloat((double) value);
	}

	public static TitanFloat int2float(final TitanInteger value) {
		value.mustBound("The argument of function int2float() is an unbound integer value.");

		if (value.isNative()) {
			return int2float(value.getInt());
		}

		return new TitanFloat(value.getBigInteger().doubleValue());
	}

	// C.8 - float2int
	public static TitanInteger float2int(final double value) {
		if (value > Integer.MIN_VALUE && value < Integer.MAX_VALUE) {
			return new TitanInteger((int) value);
		}
		return new TitanInteger(new BigDecimal(value).toBigInteger());
	}

	public static TitanInteger float2int(final TitanFloat value) {
		value.mustBound("The argument of function float2int() is an unbound float value.");

		return float2int(value.getValue());
	}

	// C.9 - char2int
	public static TitanInteger char2int(final char value) {
		if (value > 127) {
			throw new TtcnError(MessageFormat.format("The argument of function char2int() contains a character with character code {0}, which is outside the allowed range 0 .. 127.", value) );
		}
		return new TitanInteger((int) value);
	}

	public static TitanInteger char2int(String value) {
		//TODO this way of working is strange in the compiler, check later
		if (value == null) {
			value = "";
		}
		if (value.length() != 1) {
			throw new TtcnError(MessageFormat.format("The length of the argument in function char2int() must be exactly 1 instead of {0}.", value.length()));
		}
		return char2int(value.charAt(0));
	}

	public static TitanInteger char2int(final TitanCharString value) {
		value.mustBound("The argument of function char2int() is an unbound charstring value.");

		if (value.lengthOf().getInt() != 1) {
			throw new TtcnError(MessageFormat.format("The length of the argument in function char2int() must be exactly 1 instead of {0}.", value.lengthOf()));
		}
		return char2int(value.constGetAt(0).get_char());
	}

	public static TitanInteger char2int(final TitanCharString_Element value) {
		value.mustBound("The argument of function char2int() is an unbound charstring element.");

		return char2int(value.get_char());
	}

	// C.10 - char2oct
	public static TitanOctetString char2oct(final String value) {
		if (value == null || value.length() <= 0) {
			return new TitanOctetString("0");
		}

		final List<Character> octets_ptr = new ArrayList<Character>();
		for (int i = 0; i < value.length(); i++) {
			octets_ptr.add(int2oct((int) value.charAt(i), 1).get_nibble(0));
		}

		return new TitanOctetString(octets_ptr);
	}

	public static TitanOctetString char2oct(final TitanCharString value){
		value.mustBound("The argument of function char2oct() is an unbound charstring value.");

		return char2oct(value.toString());
	}

	public static TitanOctetString char2oct(final TitanCharString_Element value){
		value.mustBound("The argument of function char2oct() is an unbound charstring element.");

		return char2oct(String.valueOf(value.get_char()));
	}

	// C.11 - unichar2int
	public static TitanInteger unichar2int(final TitanUniversalChar value) {
		if (value.getUc_group() > 127) {
			throw new TtcnError(MessageFormat.format("The argument of function unichar2int() is the invalid quadruple char {0},"
					+ "the first number of which is outside the allowed range 0 .. 127.", value.getUc_group()));
		}
		final int result = (value.getUc_group() << 24) | (value.getUc_plane() << 16) | (value.getUc_row() << 8) | value.getUc_cell();

		return new TitanInteger(result);
	}

	public static TitanInteger unichar2int(final TitanUniversalCharString value) {
		value.mustBound("The argument of function unichar2int() is an unbound universal charstring value.");

		if (value.lengthOf().getInt() != 1) {
			throw new TtcnError(MessageFormat.format("The length of the argument in function unichar2int() must be exactly 1 instead of %d.", value.lengthOf().getInt()));
		}

		return unichar2int(value.getValue().get(0));
	}

	public static TitanInteger unichar2int(final TitanUniversalCharString_Element value) {
		value.mustBound("The argument of function unichar2int() is an unbound universal charstring element.");

		return unichar2int(value.get_char());
	}

	// C.12 - bit2int
	public static TitanInteger bit2int(final TitanBitString value) {
		value.mustBound("The argument of function bit2int() is an unbound bitstring value.");

		final int n_bits = value.lengthOf().getInt();
		final List<Byte> temp = value.getValue();

		// skip the leading zero bits
		int start_index = 0;
		for (; start_index < n_bits; start_index++) {
			if ((temp.get(start_index / 8) & (1 << (start_index % 8))) != 0) {
				break;
			}
		}
		// do the conversion
		BigInteger ret_val = BigInteger.ZERO;
		for (int i = start_index; i < n_bits; i++) {
			ret_val = ret_val.shiftLeft(1);
			if ((temp.get(i / 8) & (1 << (i % 8))) != 0) {
				ret_val = ret_val.add(BigInteger.ONE);
			}
		}
		if(ret_val.compareTo(BigInteger.valueOf((long)Integer.MIN_VALUE)) == 1 && ret_val.compareTo(BigInteger.valueOf((long) Integer.MAX_VALUE)) == -1 ){
			return new TitanInteger(ret_val.intValue());
		}
		return new TitanInteger(ret_val);
	}

	public static TitanInteger bit2int(final TitanBitString_Element value) {
		value.mustBound("The argument of function bit2int() is an unbound bitstring element.");

		return new TitanInteger(value.get_bit() ? 1 : 0);
	}

	// C.13 - bit2hex
	public static TitanHexString bit2hex(final TitanBitString value) {
		value.mustBound("The argument of function bit2hex() is an unbound bitstring value.");

		final int n_bits = value.lengthOf().getInt();
		final List<Byte> ret_val = new ArrayList<Byte>();
		final StringBuilder sb = new StringBuilder();

		// reverse the order
		for (int i = n_bits - 1; i >= 0; i--) {
			sb.append(value.getBit(i) ? '1' : '0');
		}

		final TitanBitString temp_val = new TitanBitString(sb.toString());
		final List<Byte> bits_ptr = temp_val.getValue();
		// do the conversion
		for (int i = bits_ptr.size() - 1; i >= 0; i--) {
			if (bits_ptr.get(i) > -1 && bits_ptr.get(i) < 16) {
				ret_val.add(bits_ptr.get(i));
			} else {
				ret_val.add((byte) ((bits_ptr.get(i) >> 4) & 0x0F));
				ret_val.add((byte) (bits_ptr.get(i) & 0x0F));
			}
		}

		return new TitanHexString(ret_val);
	}

	public static TitanHexString bit2hex(final TitanBitString_Element value) {
		value.mustBound("The argument of function bit2hex() is an unbound bitstring element.");

		return new TitanHexString((byte) (value.get_bit() ? 0x01 : 0x00));
	}

	// C.14 - bit2oct
	public static TitanOctetString bit2oct(final TitanBitString value) {
		value.mustBound("The argument of function bit2oct() is an unbound bitstring value.");

		final int n_bits = value.lengthOf().getInt();
		final int n_nibbles = (n_bits + 3) / 4;
		final List<Byte> nibbles_ptr = new ArrayList<Byte>();
		final StringBuilder sb = new StringBuilder();

		// reverse the order
		for (int i = n_bits - 1; i >= 0; i--) {
			sb.append(value.getBit(i) ? '1' : '0');
		}

		if ((n_nibbles & 1) == 1) {
			nibbles_ptr.add((byte) 0);
		}

		final TitanBitString temp_val = new TitanBitString(sb.toString());
		final List<Byte> bits_ptr =  temp_val.getValue();

		// bitstring conversion to hex characters
		for (int i = bits_ptr.size() - 1; i >= 0; i--) {
			if (bits_ptr.get(i) > -1 && bits_ptr.get(i) < 16) {
				nibbles_ptr.add(bits_ptr.get(i));
			} else {
				nibbles_ptr.add((byte) ((bits_ptr.get(i) >> 4) & 0x0F));
				nibbles_ptr.add((byte) (bits_ptr.get(i) & 0x0F));
			}
		}

		// hex characters to octets
		final List<Character> ret_val = new ArrayList<Character>();
		for (int i = 1; i < nibbles_ptr.size(); i += 2) {
			ret_val.add((char) ((nibbles_ptr.get(i - 1) << 4) | nibbles_ptr.get(i)));
		}

		return new TitanOctetString(ret_val);
	}

	public static TitanOctetString bit2oct(final TitanBitString_Element value) {
		value.mustBound("The argument of function bit2oct() is an unbound bitstring element.");

		return new TitanOctetString((char) (value.get_bit() ? 1 : 0));
	}

	// C.15 - bit2str
	public static TitanCharString bit2str(final TitanBitString value) {
		value.mustBound("The argument of function bit2str() is an unbound bitstring value.");

		final int n_bits = value.lengthOf().getInt();
		final StringBuilder ret_val = new StringBuilder(n_bits);

		for (int i = 0; i < n_bits; i++) {
			if (value.getBit(i)) {
				ret_val.append('1');
			} else {
				ret_val.append('0');
			}
		}

		return new TitanCharString(ret_val);
	}

	public static TitanCharString bit2str(final TitanBitString_Element value) {
		value.mustBound("The argument of function bit2str() is an unbound bitstring element.");

		if (value.get_bit()) {
			return new TitanCharString("1");
		} else {
			return new TitanCharString("0");
		}
	}

	// C.16 - hex2int
	public static TitanInteger hex2int(final TitanHexString value) {
		value.mustBound("The argument of function hex2int() is an unbound hexstring value.");

		final int n_nibbles = value.lengthOf().getInt();

		// skip the leading zero hex digits
		int start_index = 0;
		for (start_index = 0; start_index < n_nibbles; start_index++) {
			if (value.get_nibble(start_index) != 0) {
				break;
			}
		}

		// do the conversion
		BigInteger ret_val = BigInteger.ZERO;
		for (int i = start_index; i < n_nibbles; i++) {
			ret_val = ret_val.shiftLeft(4);
			ret_val = ret_val.add(BigInteger.valueOf(value.get_nibble(i) & 0x0F));
		}
		if (ret_val.compareTo(BigInteger.valueOf((long) Integer.MIN_VALUE)) == 1 && ret_val.compareTo(BigInteger.valueOf((long) Integer.MAX_VALUE)) == -1) {
			return new TitanInteger(ret_val.intValue());
		}
		return new TitanInteger(ret_val);
	}

	public static TitanInteger hex2int(final TitanHexString_Element value) {
		value.mustBound("The argument of function hex2int() is an unbound hexstring element.");

		return new TitanInteger(value.get_nibble());
	}

	// C.17 - hex2bit
	public static TitanBitString hex2bit(final TitanHexString value) {
		value.mustBound("The argument of function hex2bit() is an unbound hexstring value.");

		final int n_nibbles = value.lengthOf().getInt();
		final List<Byte> bits_ptr = new ArrayList<Byte>();
		final List<Byte> nibbles_ptr = new ArrayList<Byte>();
		for (int i = n_nibbles; i > 0; i--) {
			nibbles_ptr.add(value.get_nibble(n_nibbles - i));
		}

		if (n_nibbles == 1) {
			bits_ptr.add(nibbles_ptr.get(0));
			bits_ptr.set(0, (byte) ((bits_ptr.get(0) & 0xCC) >> 2 | (bits_ptr.get(0) & 0x33) << 2));
			bits_ptr.set(0, (byte) ((bits_ptr.get(0) & 0xAA) >> 1 | (bits_ptr.get(0) & 0x55) << 1));

			return new TitanBitString(bits_ptr, 4);
		}

		int j = 0;
		for (int i = 0; i < n_nibbles; i += 2) {
			bits_ptr.add(nibbles_ptr.get(i));
			bits_ptr.set(j, (byte) (bits_ptr.get(j) << 4));
			bits_ptr.set(j, (byte) (bits_ptr.get(j) | nibbles_ptr.get(i + 1)));
			j++;
		}

		// FIXME:can be simple
		// reverse the order of bits
		for (int i = 0; i < bits_ptr.size(); i++) {
			bits_ptr.set(i, (byte) ((bits_ptr.get(i) & 0xF0) >> 4 | (bits_ptr.get(i) & 0x0F) << 4));
			bits_ptr.set(i, (byte) ((bits_ptr.get(i) & 0xCC) >> 2 | (bits_ptr.get(i) & 0x33) << 2));
			bits_ptr.set(i, (byte) ((bits_ptr.get(i) & 0xAA) >> 1 | (bits_ptr.get(i) & 0x55) << 1));
		}

		return new TitanBitString(bits_ptr, 4 * n_nibbles);
	}

	public static TitanBitString hex2bit(final TitanHexString_Element value) {
		value.mustBound("The argument of function hex2bit() is an unbound hexstring element.");

		byte bits = (byte) value.get_nibble();

		bits = (byte) ((bits & 0xCC) >> 2 | (bits & 0x33) << 2);
		bits = (byte) ((bits & 0xAA) >> 1 | (bits & 0x55) << 1);
		final List<Byte> bits_ptr = new ArrayList<Byte>();
		bits_ptr.add(bits);

		return new TitanBitString(bits_ptr, 4);
	}

	// C.18 - hex2oct
	public static TitanOctetString hex2oct(final TitanHexString value) {
		value.mustBound("The argument of function hex2oct() is an unbound hexstring value.");

		final int n_nibbles = value.lengthOf().getInt();
		final int n_octets = (n_nibbles + 1) / 2;
		final List<Character> octet_ptr = new ArrayList<Character>(n_octets);
		final List<Byte> nibbles_ptr = new ArrayList<Byte>();

		if ((n_nibbles & 1) == 1) {
			nibbles_ptr.add((byte) 0);
		}
		nibbles_ptr.addAll(value.getValue());
		for (int i = 1; i < nibbles_ptr.size(); i += 2) {
			octet_ptr.add((char) ((nibbles_ptr.get(i - 1) << 4) | nibbles_ptr.get(i)));
		}

		return new TitanOctetString(octet_ptr);
	}

	public static TitanOctetString hex2oct(final TitanHexString_Element value) {
		value.mustBound("The argument of function hex2oct() is an unbound hexstring element.");

		final char octet = value.get_nibble();
		return new TitanOctetString(octet);
	}

	// C.19 - hex2str
	public static TitanCharString hex2str(final TitanHexString value) {
		value.mustBound("The argument of function hex2str() is an unbound hexstring value.");

		final int n_nibbles = value.lengthOf().getInt();
		final StringBuilder ret_val = new StringBuilder();
		for (int i = 0; i < n_nibbles; i++) {
			ret_val.append(value.constGetAt(i));
		}

		return new TitanCharString(ret_val);
	}

	public static TitanCharString hex2str(final TitanHexString_Element value) {
		value.mustBound("The argument of function hex2str() is an unbound hexstring element.");

		return new TitanCharString(value.toString());
	}

	// C.20 - oct2int
	public static TitanInteger oct2int(final TitanOctetString value) {
		value.mustBound("The argument of function oct2int() is an unbound octetstring value.");

		final int n_octets = value.lengthOf().getInt();

		// skip the leading zero hex digits
		int start_index = 0;
		for (start_index = 0; start_index < n_octets; start_index++) {
			if (value.get_nibble(start_index) != 0) {
				break;
			}
		}

		// do the conversion
		BigInteger ret_val = BigInteger.ZERO;
		for (int i = start_index; i < n_octets; i++) {
			ret_val = ret_val.shiftLeft(8);
			ret_val = ret_val.add(BigInteger.valueOf(value.get_nibble(i) & 0xF0));
			ret_val = ret_val.add(BigInteger.valueOf(value.get_nibble(i) & 0x0F));
		}
		if (ret_val.compareTo(BigInteger.valueOf((long) Integer.MIN_VALUE)) == 1 && ret_val.compareTo(BigInteger.valueOf((long) Integer.MAX_VALUE)) == -1) {
			return new TitanInteger(ret_val.intValue());
		}
		return new TitanInteger(ret_val);
	}

	public static TitanInteger oct2int(final TitanOctetString_Element value) {
		value.mustBound("The argument of function oct2int() is an unbound octetstring element.");

		return new TitanInteger((int) value.get_nibble());
	}

	// C.21 - oct2bit
	public static TitanBitString oct2bit(final TitanOctetString value) {
		value.mustBound("The argument of function oct2bit() is an unbound octetstring value.");

		final int n_octets = value.lengthOf().getInt();
		final List<Byte> bits_ptr = new ArrayList<Byte>();
		final List<Character> octets_ptr = new ArrayList<Character>();
		octets_ptr.addAll(value.getValue());

		for (int i = 0; i < n_octets; i++) {
			bits_ptr.add((byte) ((octets_ptr.get(i) & 0xF0) >> 4));
			bits_ptr.set(i, (byte) (bits_ptr.get(i) << 4));
			bits_ptr.set(i, (byte) (bits_ptr.get(i) | octets_ptr.get(i) & 0x0F));
		}

		// FIXME:can be simple
		// reverse the order of bits
		for (int i = 0; i < bits_ptr.size(); i++) {
			bits_ptr.set(i, (byte) ((bits_ptr.get(i) & 0xF0) >> 4 | (bits_ptr.get(i) & 0x0F) << 4));
			bits_ptr.set(i, (byte) ((bits_ptr.get(i) & 0xCC) >> 2 | (bits_ptr.get(i) & 0x33) << 2));
			bits_ptr.set(i, (byte) ((bits_ptr.get(i) & 0xAA) >> 1 | (bits_ptr.get(i) & 0x55) << 1));
		}

		return new TitanBitString(bits_ptr, 8 * n_octets);
	}

	public static TitanBitString oct2bit(final TitanOctetString_Element value) {
		value.mustBound("The argument of function oct2bit() is an unbound octetstring value.");

		int bits = ((value.get_nibble() & 0xF0) | (value.get_nibble() & 0x0F));
		bits = (bits & 0xF0) >> 4 | (bits & 0x0F) << 4;
		bits = (bits & 0xCC) >> 2 | (bits & 0x33) << 2;
		bits = (bits & 0xAA) >> 1 | (bits & 0x55) << 1;
		return new TitanBitString((byte) bits);
	}

	// C.22 - oct2hex
	public static TitanHexString oct2hex(final TitanOctetString value) {
		value.mustBound("The argument of function oct2hex() is an unbound octetstring value.");

		final int n_octets = value.lengthOf().getInt();
		final List<Byte> ret_val = new ArrayList<Byte>();
		final List<Character> octets_ptr = new ArrayList<Character>();
		octets_ptr.addAll(value.getValue());

		for (int i = 0; i < n_octets; i++) {
			ret_val.add((byte) ((octets_ptr.get(i) & 0xF0) >> 4));
			ret_val.add((byte) (octets_ptr.get(i) & 0x0F));
		}

		return new TitanHexString(ret_val);
	}

	public static TitanHexString oct2hex(final TitanOctetString_Element value) {
		value.mustBound("The argument of function oct2hex() is an unbound octetstring element.");

		final List<Byte> ret_val = new ArrayList<Byte>();
		ret_val.add((byte) ((value.get_nibble() & 0xF0) >> 4));
		ret_val.add((byte) (value.get_nibble() & 0x0F));

		return new TitanHexString(ret_val);
	}

	// C.23 - oct2str
	public static TitanCharString oct2str(final TitanOctetString value) {
		value.mustBound("The argument of function oct2str() is an unbound octetstring value.");

		final int n_octets = value.lengthOf().getInt();
		final StringBuilder ret_val = new StringBuilder();
		for (int i = 0; i < n_octets; i++) {
			ret_val.append(value.constGetAt(i).toString());
		}

		return new TitanCharString(ret_val);
	}

	public static TitanCharString oct2str(final TitanOctetString_Element value) {
		value.mustBound("The argument of function oct2str() is an unbound octetstring element.");

		return new TitanCharString(value.toString());
	}

	// C.24 - oct2char
	public static TitanCharString oct2char(final TitanOctetString value) {
		value.mustBound("The argument of function oct2char() is an unbound octetstring value.");

		final int value_length = value.lengthOf().getInt();
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < value_length; i++) {
			if ((int) value.get_nibble(i) > 127) {
				throw new TtcnError(MessageFormat.format("The argument of function oct2char() contains octet {0} at index {1}, which is outside the allowed range 00 .. 7F.", (int) value.get_nibble(i), i));
			}
			sb.append(value.get_nibble(i));
		}
		return new TitanCharString(sb);
	}

	public static TitanCharString oct2char(final TitanOctetString_Element value) {
		value.mustBound("The argument of function oct2char() is an unbound octetstring element.");

		final char octet = value.get_nibble();
		if ((int) octet > 127) {
			throw new TtcnError(MessageFormat.format("The argument of function oct2char() contains the octet {0}, which is outside the allowed range 00 .. 7F.", octet));
		}

		return new TitanCharString(String.valueOf(octet));
	}

	// C.25 - str2int
	public static TitanInteger str2int(final String value) {
		return str2int(new TitanCharString(value));
	}

	public static TitanInteger str2int(final TitanCharString value) {
		value.mustBound("The argument of function str2int() is an unbound charstring value.");

		final int value_len = value.lengthOf().getInt();
		if (value_len == 0) {
			throw new TtcnError("The argument of function str2int() is an empty string, which does not represent a valid integer value.");
		}
		final StringBuilder value_str = new StringBuilder();
		value_str.append(value.getValue().toString());
		str2intState state = str2intState.S_INITIAL;
		// state: expected characters
		// S_INITIAL: +, -, first digit, leading whitespace
		// S_FIRST: first digit
		// S_ZERO, S_MORE: more digit(s), trailing whitespace
		// S_END: trailing whitespace
		// S_ERR: error was found, stop
		boolean leading_ws = false;
		boolean leading_zero = false;
		for (int i = 0; i < value_len; i++) {
			final char c = value_str.charAt(i);
			switch (state) {
			case S_INITIAL:
				if (c == '+' || c == '-') {
					state = str2intState.S_FIRST;
				} else if (c == '0') {
					state = str2intState.S_ZERO;
				} else if (c >= '1' && c <= '9') {
					state = str2intState.S_MORE;
				} else if (Character.isWhitespace(c)) {
					leading_ws = true;
				} else {
					state = str2intState.S_ERR;
				}
				break;
			case S_FIRST:
				if (c == '0') {
					state = str2intState.S_ZERO;
				} else if (c >= '1' && c <= '9') {
					state = str2intState.S_MORE;
				} else {
					state = str2intState.S_ERR;
				}
				break;
			case S_ZERO:
				if (c >= '1' && c <= '9') {
					leading_zero = true;
					state = str2intState.S_MORE;
				} else if (Character.isWhitespace(c)) {
					state = str2intState.S_END;
				} else {
					state = str2intState.S_ERR;
				}
				break;
			case S_MORE:
				if (c >= '0' && c <= '9') {
				} else if (Character.isWhitespace(c)) {
					state = str2intState.S_END;
				} else {
					state = str2intState.S_ERR;
				}
				break;
			case S_END:
				if (!Character.isWhitespace(c)) {
					state = str2intState.S_ERR;
				}
				break;
			default:
				break;
			}
			if (state == str2intState.S_ERR) {
				// TODO: Initial implementation
				throw new TtcnError( MessageFormat.format( "The argument of function str2int(), which is {0} , does not represent a valid integer value. Invalid character {1} was found at index {2}.", value_str.toString(), c, i));
			}
		}
		if (state != str2intState.S_ZERO && state != str2intState.S_MORE && state != str2intState.S_END) {
			// TODO: Initial implementation
			throw new TtcnError( MessageFormat.format( "The argument of function str2int(), which is {0} , does not represent a valid integer value. Premature end of the string.", value_str.toString()));
		}
		if (leading_ws) {
			// TODO: Initial implementation
			throw new TtcnError(MessageFormat.format("Leading whitespace was detected in the argument of function str2int(): {0}.", value_str.toString()));
		}
		if (leading_zero) {
			// TODO: Initial implementation
			throw new TtcnError(MessageFormat.format("Leading zero digit was detected in the argument of function str2int(): {0}.", value_str.toString()));
		}
		if (state == str2intState.S_END) {
			// TODO: Initial implementation
			TtcnError.TtcnWarning(MessageFormat.format("Trailing whitespace was detected in the argument of function str2int(): {0}.", value_str.toString()));
		}

		return new TitanInteger(value_str.toString());
	}

	public static TitanInteger str2int(final TitanCharString_Element value) {
		value.mustBound("The argument of function str2int() is an unbound charstring element.");

		final char c = value.get_char();
		if (c < '0' || c > '9') {
			// TODO: Initial implementation
			throw new TtcnError(MessageFormat.format("The argument of function str2int(), which is a charstring element containing character {0}, does not represent a valid integer value.", c));
		}
		return new TitanInteger(Integer.valueOf(c - '0'));
	}

	// C.26 - str2oct
	public static TitanOctetString str2oct(final String value) {
		if (value == null) {
			return new TitanOctetString();
		} else {
			return str2oct(new TitanCharString(value));
		}
	}

	public static TitanOctetString str2oct(final TitanCharString value) {
		value.mustBound("The argument of function str2oct() is an unbound charstring value.");

		final int value_len = value.lengthOf().getInt();
		if (value_len % 2 != 0) {
			throw new TtcnError(MessageFormat.format("The argument of function str2oct() must have even number of characters containing hexadecimal digits, but the length of the string is odd: {0}.", value_len));
		}
		final List<Character> octets_ptr = new ArrayList<Character>(value_len / 2);
		for (int i = 0; i < value_len / 2; i++) {
			octets_ptr.add((char) 0);
		}
		final StringBuilder chars_ptr = new StringBuilder();
		chars_ptr.append(value.getValue().toString());
		for (int i = 0; i < value_len; i++) {
			final char c = chars_ptr.charAt(i);
			final byte hexdigit = charToHexDigit(c);
			if (hexdigit > 0x0F) {
				// TODO: Initial implementation
				throw new TtcnError(MessageFormat.format("The argument of function str2oct() shall contain hexadecimal digits only, but character {0} was found at index {1}.", c, i));
			}
			if (i % 2 != 0) {
				octets_ptr.set(i / 2, (char) (octets_ptr.get(i / 2) | hexdigit));
			} else {
				octets_ptr.set(i / 2, (char) (hexdigit << 4));
			}
		}

		return new TitanOctetString(octets_ptr);
	}

	// C.27 - str2float
	public static TitanFloat str2float(final String value){
		return str2float(new TitanCharString(value));
	}

	/*
	 * leading zeros are allowed;
	 * leading "+" sign before positive values is allowed;
	 * "-0.0" is allowed;
	 * no numbers after the dot in the decimal notation are allowed.
	 */

	public static TitanFloat str2float(final TitanCharString value) {
		value.mustBound("The argument of function str2float() is an unbound charstring value.");

		final int value_len = value.lengthOf().getInt();
		if(value_len == 0) {
			throw new TtcnError("The argument of function str2float() is an empty string, which does not represent a valid float value.");
		}
		final StringBuilder value_str = new StringBuilder();
		value_str.append(value.getValue().toString());
		str2floatState state = str2floatState.S_INITIAL;
		// state: expected characters
		// S_INITIAL: +, -, first digit of integer part in mantissa,
		//            leading whitespace
		// S_FIRST_M: first digit of integer part in mantissa
		// S_ZERO_M, S_MORE_M: more digits of mantissa, decimal dot, E
		// S_FIRST_F: first digit of fraction
		// S_MORE_F: more digits of fraction, E, trailing whitespace
		// S_INITIAL_E: +, -, first digit of exponent
		// S_FIRST_E: first digit of exponent
		// S_ZERO_E, S_MORE_E: more digits of exponent, trailing whitespace
		// S_END: trailing whitespace
		// S_ERR: error was found, stop
		boolean leading_ws = false;
		boolean leading_zero = false;
		for (int i = 0; i < value_len; i++) {
			final char c = value_str.charAt(i);
			switch (state) {
			case S_INITIAL:
				if(c == '+' || c == '-') {
					state = str2floatState.S_FIRST_M;
				} else if(c == '0') {
					state = str2floatState.S_ZERO_M;
				} else if(c >= '1' && c <= '9') {
					state = str2floatState.S_MORE_M;
				} else if(Character.isWhitespace(c)) {
					leading_ws = true;
				} else {
					state = str2floatState.S_ERR;
				}
				break;
			case S_FIRST_M:  // first mantissa digit
				if(c == '0') {
					state = str2floatState.S_ZERO_M;
				} else if(c >= '1' && c <= '9') {
					state = str2floatState.S_MORE_M;
				} else {
					state = str2floatState.S_ERR;
				}
				break;
			case S_ZERO_M: // leading mantissa zero
				if(c == '.') {
					state = str2floatState.S_FIRST_F;
				} else if (c == 'E' || c == 'e'){
					state = str2floatState.S_INITIAL_E;
				} else if (c >= '0' && c <= '9') {
					leading_zero = true;
					state = str2floatState.S_MORE_M;
				} else {
					state = str2floatState.S_ERR;
				}
				break;
			case S_MORE_M:
				if(c == '.') {
					state = str2floatState.S_FIRST_F;
				} else if (c == 'E' || c == 'e') {
					state = str2floatState.S_INITIAL_E;
				} else if(c >= '0' && c <= '9') {}
				else {
					state = str2floatState.S_ERR;
				}
				break;
			case S_FIRST_F:
				if(c >= '0' && c <= '9') {
					state = str2floatState.S_MORE_F;
				} else {
					state = str2floatState.S_ERR;
				}
				break;
			case S_MORE_F:
				if (c == 'E' || c == 'e') {
					state = str2floatState.S_INITIAL_E;
				} else if (c >= '0' && c <= '9') {}
				else if(Character.isWhitespace(c)) {
					state = str2floatState.S_END;
				} else {
					state = str2floatState.S_ERR;
				}
				break;
			case S_INITIAL_E:
				if (c == '+' || c == '-') {
					state = str2floatState.S_FIRST_E;
				} else if(c == '0') {
					state = str2floatState.S_ZERO_E;
				} else if(c >= '1' && c <= '9') {
					state = str2floatState.S_MORE_E;
				} else {
					state = str2floatState.S_ERR;
				}
				break;
			case S_FIRST_E:
				if(c == '0') {
					state = str2floatState.S_ZERO_E;
				} else if(c >= '1' && c <= '9') {
					state = str2floatState.S_MORE_E;
				} else {
					state = str2floatState.S_ERR;
				}
				break;
			case S_ZERO_E:
				if (c >= '0' && c <= '9') {
					leading_zero = true;
					state = str2floatState.S_MORE_E;
				} else if(Character.isWhitespace(c)) {
					state = str2floatState.S_END;
				} else {
					state = str2floatState.S_ERR;
				}
				break;
			case S_MORE_E:
				if (c >= '0' && c <= '9') {}
				else if(Character.isWhitespace(c)) {
					state = str2floatState.S_END;
				} else {
					state = str2floatState.S_ERR;
				}
				break;
			case S_END:
				if(Character.isWhitespace(c)) {
					state = str2floatState.S_ERR;
				}
				break;
			default:
				break;
			}
			if(state == str2floatState.S_ERR) {
				//TODO: Initial implementation
				throw new TtcnError(MessageFormat.format("The argument of function str2float(), which is {0} , does not represent a valid float value. Invalid character {1} was found at index {2}. ", value_str.toString(),c,i));
			}
		}
		switch (state) {
		case S_MORE_F:
		case S_ZERO_E:
		case S_MORE_E:
			// OK, originally
			break;
		case S_ZERO_M:
		case S_MORE_M:
			// OK now (decimal dot missing after mantissa)
			break;
		case S_FIRST_F:
			// OK now (fraction part missing)
			break;
		default:
			//TODO: Initial implementation
			throw new TtcnError(MessageFormat.format("The argument of function str2float(), which is {0} , does not represent a valid float value. Premature end of the string.",value_str.toString()));
		}
		if(leading_ws) {
			//TODO: Initial implementation
			TtcnError.TtcnWarning(MessageFormat.format("Leading whitespace was detected in the argument of function str2float(): {0}." , value_str.toString()));
		}
		if(leading_zero) {
			//TODO: Initial implementation
			TtcnError.TtcnWarning(MessageFormat.format("Leading zero digit was detected in the argument of function str2float(): {0}.", value_str.toString()));
		}
		if(state == str2floatState.S_END) {
			//TODO: Initial implementation
			TtcnError.TtcnWarning(MessageFormat.format("Trailing whitespace was detected in the argument of function str2float(): {0}.", value_str.toString()));
		}
		return new TitanFloat(Double.valueOf(value_str.toString()));
	}

	// C.34 - substr
	public static void check_substr_arguments(final int value_length, final int idx, final int returncount, final String string_type, final String element_name) {
		if(idx < 0) {
			throw new TtcnError(MessageFormat.format("The second argument (index) of function substr() is a negative integer value: {0}.", idx));
		}
		if(idx > value_length) {
			throw new TtcnError(MessageFormat.format("The second argument (index) of function substr(), which is {0} , is greater than the length of the {1} value: {2}.", idx, string_type , value_length));
		}
		if(returncount < 0) {
			throw new TtcnError(MessageFormat.format("The third argument (returncount) of function substr() is a negative integer value: {0}.", returncount));
		}
		if(idx + returncount > value_length) {
			throw new TtcnError(MessageFormat.format("The first argument of function substr(), the length of which is {0}, does not have enough {1}s starting at index {2}: {3} {4}{5} needed, but there {6} only {7}.",
					value_length, element_name, idx, returncount, element_name, returncount > 1 ? "s are" : " is", value_length - idx > 1 ? "are" : "is", value_length - idx));
		}
	}

	public static void check_substr_arguments(final int idx, final int returncount, final String string_type, final String element_name) {
		if(idx < 0) {
			throw new TtcnError(MessageFormat.format("The second argument (index) of function substr() is a negative integer value: {0}.", idx));
		}
		if(idx > 1) {
			throw new TtcnError(MessageFormat.format("The second argument (index) of function substr(), which is {0}, is greater than 1 (i.e. the length of the {1} element).", idx, string_type));
		}
		if(returncount < 0) {
			throw new TtcnError(MessageFormat.format("The third argument (returncount) of function substr() is a negative integer value: {0}.", returncount));
		}
		if(idx + returncount > 1) {
			throw new TtcnError(MessageFormat.format("The first argument of function substr(), which is a{0} {1} element, does not have enough {2}s starting at index {3}: {4} {5}{6} needed, but there is only {7}.",
					string_type.charAt(0) == 'o' ? "n" : "", string_type, element_name, idx, returncount, element_name, returncount > 1 ? "s are" : " is",  1 - idx));
		}
	}

	public static TitanBitString subString(final TitanBitString value, final int idx, final int returncount) {
		value.mustBound("The first argument (value) of function substr() is an unbound bitstring value.");

		check_substr_arguments(value.lengthOf().getInt(), idx, returncount, "bitstring", "bit");
		if (idx % 8 != 0) {
			final StringBuilder sb = new StringBuilder();
			for (int i = 0; i < returncount; i++) {
				sb.append(value.getBit(idx + i) ? '1' : '0');
			}
			return new TitanBitString(sb.toString());
		} else {
			final List<Byte> bits_ptr = value.getValue();
			final List<Byte> ret_val = new ArrayList<Byte>();
			for (int i = 0; i < (returncount + 7) / 8; i++) {
				ret_val.add(bits_ptr.get(i));
			}
			return new TitanBitString(ret_val, returncount);
		}
	}

	public static TitanBitString subString(final TitanBitString value, final int idx, final TitanInteger returncount) {
		returncount.mustBound("The third argument (returncount) of function substr() is an unbound integer value.");

		return subString(value, idx, returncount.getInt());
	}

	public static TitanBitString subString(final TitanBitString value, final TitanInteger idx, final int returncount) {
		idx.mustBound("The second argument (index) of function substr() is an unbound integer value.");

		return subString(value, idx.getInt(), returncount);
	}

	public static TitanBitString subString(final TitanBitString value, final TitanInteger idx, final TitanInteger returncount) {
		idx.mustBound("The second argument (index) of function substr() is an unbound integer value.");
		returncount.mustBound("The third argument (returncount) of function substr() is an unbound integer value.");

		return subString(value, idx.getInt(), returncount.getInt());
	}

	public static TitanBitString subString(final TitanBitString_Element value, final int idx, final int returncount) {
		value.mustBound("The first argument (value) of function substr() is an unbound bitstring element.");

		check_substr_arguments(idx, returncount, "bitstring", "bit");
		if (returncount == 0) {
			return new TitanBitString();
		} else {
			return new TitanBitString(value.get_bit() ? "1" : "0");
		}
	}

	public static TitanBitString subString(final TitanBitString_Element value, final int idx, final TitanInteger returncount) {
		returncount.mustBound("The third argument (returncount) of function substr() is an unbound integer value.");

		return subString(value, idx, returncount.getInt());
	}

	public static TitanBitString subString(final TitanBitString_Element value, final TitanInteger idx, final int returncount) {
		idx.mustBound("The second argument (index) of function substr() is an unbound integer value.");

		return subString(value, idx.getInt(), returncount);
	}

	public static TitanBitString subString(final TitanBitString_Element value, final TitanInteger idx, final TitanInteger returncount) {
		idx.mustBound("The second argument (index) of function substr() is an unbound integer value.");
		returncount.mustBound("The third argument (returncount) of function substr() is an unbound integer value.");

		return subString(value, idx.getInt(), returncount.getInt());
	}

	public static TitanHexString subString(final TitanHexString value, final int idx, final int returncount) {
		value.mustBound("The first argument (value) of function substr() is an unbound hexstring value.");

		check_substr_arguments(value.lengthOf().getInt(), idx, returncount, "hexstring", "hexadecimal digi");
		final List<Byte> src_ptr = value.getValue();
		final List<Byte> ret_val = new ArrayList<Byte>();

		for (int i = 0; i < returncount; i++) {
			ret_val.add(src_ptr.get(i + idx));
		}

		return new TitanHexString(ret_val);
	}

	public static TitanHexString subString(final TitanHexString value, final int idx, final TitanInteger returncount) {
		returncount.mustBound("The third argument (returncount) of function substr() is an unbound integer value.");

		return subString(value, idx, returncount.getInt());
	}

	public static TitanHexString subString(final TitanHexString value, final TitanInteger idx, final int returncount) {
		idx.mustBound("The second argument (index) of function substr() is an unbound integer value.");

		return subString(value, idx.getInt(), returncount);
	}

	public static TitanHexString subString(final TitanHexString value, final TitanInteger idx, final TitanInteger returncount) {
		idx.mustBound("The second argument (index) of function substr() is an unbound integer value.");
		returncount.mustBound("The third argument (returncount) of function substr() is an unbound integer value.");

		return subString(value, idx.getInt(), returncount.getInt());
	}

	public static TitanHexString subString(final TitanHexString_Element value, final int idx, final int returncount) {
		value.mustBound("The first argument (value) of function substr() is an unbound hexstring element.");

		check_substr_arguments(idx, returncount, "hexstring", "hexadecimal digit");
		if (returncount == 0) {
			return new TitanHexString();
		} else {
			return new TitanHexString(String.valueOf(value.get_nibble()));
		}
	}

	public static TitanHexString subString(final TitanHexString_Element value, final int idx, final TitanInteger returncount) {
		returncount.mustBound("The third argument (returncount) of function substr() is an unbound integer value.");

		return subString(value, idx, returncount.getInt());
	}

	public static TitanHexString subString(final TitanHexString_Element value, final TitanInteger idx, final int returncount) {
		idx.mustBound("The second argument (index) of function substr() is an unbound integer value.");

		return subString(value, idx.getInt(), returncount);
	}

	public static TitanHexString subString(final TitanHexString_Element value, final TitanInteger idx, final TitanInteger returncount) {
		idx.mustBound("The second argument (index) of function substr() is an unbound integer value.");
		returncount.mustBound("The third argument (returncount) of function substr() is an unbound integer value.");

		return subString(value, idx.getInt(), returncount.getInt());
	}

	public static TitanOctetString subString(final TitanOctetString value, final int idx, final int returncount) {
		value.mustBound("The first argument (value) of function substr() is an unbound octetstring value.");

		check_substr_arguments(value.lengthOf().getInt(), idx, returncount, "octetstring", "octet");
		final List<Character> ret_val = new ArrayList<Character>();
		final List<Character> src_ptr = value.getValue();
		for (int i = 0; i < returncount; i++) {
			ret_val.add(src_ptr.get(i + idx));
		}

		return new TitanOctetString(ret_val);
	}

	public static TitanOctetString subString(final TitanOctetString value, final int idx, final TitanInteger returncount) {
		returncount.mustBound("The third argument (returncount) of function substr() is an unbound integer value.");

		return subString(value, idx, returncount.getInt());
	}

	public static TitanOctetString subString(final TitanOctetString value, final TitanInteger idx, final int returncount) {
		idx.mustBound("The second argument (index) of function substr() is an unbound integer value.");

		return subString(value, idx.getInt(), returncount);
	}

	public static TitanOctetString subString(final TitanOctetString value, final TitanInteger idx, final TitanInteger returncount) {
		idx.mustBound("The second argument (index) of function substr() is an unbound integer value.");
		returncount.mustBound("The third argument (returncount) of function substr() is an unbound integer value.");

		return subString(value, idx.getInt(), returncount.getInt());
	}

	public static TitanOctetString subString(final TitanOctetString_Element value, final int idx, final int returncount) {
		value.mustBound("The first argument (value) of function substr() is an unbound octetstring element.");

		check_substr_arguments(idx, returncount, "octetstring", "octet");
		if (returncount == 0) {
			return new TitanOctetString();
		} else {
			return new TitanOctetString(value.get_nibble());
		}
	}

	public static TitanOctetString subString(final TitanOctetString_Element value, final int idx, final TitanInteger returncount) {
		returncount.mustBound("The third argument (returncount) of function substr() is an unbound integer value.");

		return subString(value, idx, returncount.getInt());
	}

	public static TitanOctetString subString(final TitanOctetString_Element value, final TitanInteger idx, final int returncount) {
		idx.mustBound("The second argument (index) of function substr() is an unbound integer value.");

		return subString(value, idx.getInt(), returncount);
	}

	public static TitanOctetString subString(final TitanOctetString_Element value, final TitanInteger idx, final TitanInteger returncount) {
		idx.mustBound("The second argument (index) of function substr() is an unbound integer value.");
		returncount.mustBound("The third argument (returncount) of function substr() is an unbound integer value.");

		return subString(value, idx.getInt(), returncount.getInt());
	}

	public static TitanCharString subString(final TitanCharString value, final int idx, final int returncount) {
		value.mustBound("The first argument (value) of function substr() is an unbound charstring value.");

		check_substr_arguments(value.lengthOf().getInt(), idx, returncount, "charstring", "character");

		return new TitanCharString(value.getValue().substring(idx, idx + returncount));
	}

	public static TitanCharString subString(final TitanCharString value, final int idx, final TitanInteger returncount) {
		returncount.mustBound("The third argument (returncount) of function substr() is an unbound integer value.");

		return subString(value, idx, returncount.getInt());
	}

	public static TitanCharString subString(final TitanCharString value, final TitanInteger idx, final int returncount) {
		idx.mustBound("The second argument (index) of function substr() is an unbound integer value.");

		return subString(value, idx.getInt(), returncount);
	}

	public static TitanCharString subString(final TitanCharString value, final TitanInteger idx, final TitanInteger returncount) {
		idx.mustBound("The second argument (index) of function substr() is an unbound integer value.");
		returncount.mustBound("The third argument (returncount) of function substr() is an unbound integer value.");

		return subString(value, idx.getInt(), returncount.getInt());
	}

	public static TitanCharString subString(final TitanCharString_Element value, final int idx, final int returncount) {
		value.mustBound("The first argument (value) of function substr() is an unbound charstring element.");

		check_substr_arguments(idx, returncount, "charstring", "character");

		return new TitanCharString(String.valueOf(value.get_char()));
	}

	public static TitanCharString subString(final TitanCharString_Element value, final int idx, final TitanInteger returncount) {
		returncount.mustBound("The third argument (returncount) of function substr() is an unbound integer value.");

		return subString(value, idx, returncount.getInt());
	}

	public static TitanCharString subString(final TitanCharString_Element value, final TitanInteger idx, final int returncount) {
		idx.mustBound("The second argument (index) of function substr() is an unbound integer value.");

		return subString(value, idx.getInt(), returncount);
	}

	public static TitanCharString subString(final TitanCharString_Element value, final TitanInteger idx, final TitanInteger returncount) {
		idx.mustBound("The second argument (index) of function substr() is an unbound integer value.");
		returncount.mustBound("The third argument (returncount) of function substr() is an unbound integer value.");

		return subString(value, idx.getInt(), returncount.getInt());
	}

	public static TitanUniversalCharString subString(final TitanUniversalCharString value, final int idx, final int returncount) {
		value.mustBound("The first argument (value) of function substr() is an unbound universal charstring value.");

		check_substr_arguments(value.lengthOf().getInt(), idx, returncount, "universal charstring", "character");
		if (value.charstring) {
			return new TitanUniversalCharString(value.cstr.substring(idx, idx + returncount));
		} else {
			final List<TitanUniversalChar> ret_val = new ArrayList<TitanUniversalChar>();
			final List<TitanUniversalChar> src_ptr = value.getValue();
			for (int i = 0; i < returncount; i++) {
				ret_val.add(src_ptr.get(i + idx));
			}

			return new TitanUniversalCharString(ret_val);
		}
	}

	public static TitanUniversalCharString subString(final TitanUniversalCharString value, final int idx, final TitanInteger returncount) {
		returncount.mustBound("The third argument (returncount) of function substr() is an unbound integer value.");

		return subString(value, idx, returncount.getInt());
	}

	public static TitanUniversalCharString subString(final TitanUniversalCharString value, final TitanInteger idx, final int returncount) {
		idx.mustBound("The second argument (index) of function substr() is an unbound integer value.");

		return subString(value, idx.getInt(), returncount);
	}

	public static TitanUniversalCharString subString(final TitanUniversalCharString value, final TitanInteger idx, final TitanInteger returncount) {
		idx.mustBound("The second argument (index) of function substr() is an unbound integer value.");
		returncount.mustBound("The third argument (returncount) of function substr() is an unbound integer value.");

		return subString(value, idx.getInt(), returncount.getInt());
	}

	public static TitanUniversalCharString subString(final TitanUniversalCharString_Element value, final int idx, final int returncount) {
		value.mustBound("The first argument (value) of function substr() is an unbound universal charstring element.");

		check_substr_arguments(idx, returncount, "universal charstring", "character");
		if (returncount == 0) {
			return new TitanUniversalCharString();
		} else {
			return new TitanUniversalCharString(value.get_char());
		}
	}

	public static TitanUniversalCharString subString(final TitanUniversalCharString_Element value, final int idx, final TitanInteger returncount) {
		returncount.mustBound("The third argument (returncount) of function substr() is an unbound integer value.");

		return subString(value, idx, returncount.getInt());
	}

	public static TitanUniversalCharString subString(final TitanUniversalCharString_Element value, final TitanInteger idx, final int returncount) {
		idx.mustBound("The second argument (index) of function substr() is an unbound integer value.");

		return subString(value, idx.getInt(), returncount);
	}

	public static TitanUniversalCharString subString(final TitanUniversalCharString_Element value, final TitanInteger idx, final TitanInteger returncount) {
		idx.mustBound("The second argument (index) of function substr() is an unbound integer value.");
		returncount.mustBound("The third argument (returncount) of function substr() is an unbound integer value.");

		return subString(value, idx.getInt(), returncount.getInt());
	}

	//subString() on templates
	public static TitanBitString subString(final TitanBitString_template value, final int idx, final int returncount) {
		if(!value.isValue()) {
			throw new TtcnError("The first argument of function substr() is a template with non-specific value.");
		}

		return subString(value.valueOf(), idx, returncount);
	}

	public static TitanBitString subString(final TitanBitString_template value, final int idx, final TitanInteger returncount) {
		if(!value.isValue()) {
			throw new TtcnError("The first argument of function substr() is a template with non-specific value.");
		}

		return subString(value.valueOf(), idx, returncount);
	}

	public static TitanBitString subString(final TitanBitString_template value, final TitanInteger idx, final int returncount) {
		if(!value.isValue()) {
			throw new TtcnError("The first argument of function substr() is a template with non-specific value.");
		}

		return subString(value.valueOf(), idx, returncount);
	}

	public static TitanBitString subString(final TitanBitString_template value, final TitanInteger idx, final TitanInteger returncount) {
		if(!value.isValue()) {
			throw new TtcnError("The first argument of function substr() is a template with non-specific value.");
		}

		return subString(value.valueOf(), idx, returncount);
	}

	public static TitanHexString subString(final TitanHexString_template value, final int idx, final int returncount) {
		if(!value.isValue()) {
			throw new TtcnError("The first argument of function substr() is a template with non-specific value.");
		}

		return subString(value.valueOf(), idx, returncount);
	}

	public static TitanHexString subString(final TitanHexString_template value, final int idx, final TitanInteger returncount) {
		if(!value.isValue()) {
			throw new TtcnError("The first argument of function substr() is a template with non-specific value.");
		}

		return subString(value.valueOf(), idx, returncount);
	}

	public static TitanHexString subString(final TitanHexString_template value, final TitanInteger idx, final int returncount) {
		if(!value.isValue()) {
			throw new TtcnError("The first argument of function substr() is a template with non-specific value.");
		}

		return subString(value.valueOf(), idx, returncount);
	}

	public static TitanHexString subString(final TitanHexString_template value, final TitanInteger idx, final TitanInteger returncount) {
		if(!value.isValue()) {
			throw new TtcnError("The first argument of function substr() is a template with non-specific value.");
		}

		return subString(value.valueOf(), idx, returncount);
	}

	public static TitanOctetString subString(final TitanOctetString_template value, final int idx, final int returncount) {
		if(!value.isValue()) {
			throw new TtcnError("The first argument of function substr() is a template with non-specific value.");
		}

		return subString(value.valueOf(), idx, returncount);
	}

	public static TitanOctetString subString(final TitanOctetString_template value, final int idx, final TitanInteger returncount) {
		if(!value.isValue()) {
			throw new TtcnError("The first argument of function substr() is a template with non-specific value.");
		}

		return subString(value.valueOf(), idx, returncount);
	}

	public static TitanOctetString subString(final TitanOctetString_template value, final TitanInteger idx, final int returncount) {
		if(!value.isValue()) {
			throw new TtcnError("The first argument of function substr() is a template with non-specific value.");
		}

		return subString(value.valueOf(), idx, returncount);
	}

	public static TitanOctetString subString(final TitanOctetString_template value, final TitanInteger idx, final TitanInteger returncount) {
		if(!value.isValue()) {
			throw new TtcnError("The first argument of function substr() is a template with non-specific value.");
		}

		return subString(value.valueOf(), idx, returncount);
	}
	
	public static TitanCharString subString(final TitanCharString_template value, final int idx, final int returncount) {
		if(!value.isValue()) {
			throw new TtcnError("The first argument of function substr() is a template with non-specific value.");
		}
		
		return subString(value.valueOf(), idx, returncount);
	}

	public static TitanCharString subString(final TitanCharString_template value, final int idx, final TitanInteger returncount) {
		if(!value.isValue()) {
			throw new TtcnError("The first argument of function substr() is a template with non-specific value.");
		}
		
		return subString(value.valueOf(), idx, returncount);
	}

	public static TitanCharString subString(final TitanCharString_template value, final TitanInteger idx, final int returncount) {
		if(!value.isValue()) {
			throw new TtcnError("The first argument of function substr() is a template with non-specific value.");
		}
		
		return subString(value.valueOf(), idx, returncount);
	}

	public static TitanCharString subString(final TitanCharString_template value, final TitanInteger idx, final TitanInteger returncount) {
		if(!value.isValue()) {
			throw new TtcnError("The first argument of function substr() is a template with non-specific value.");
		}
		
		return subString(value.valueOf(), idx, returncount);
	}
	
	public static TitanUniversalCharString subString(final TitanUniversalCharString_template value, final int idx, final int returncount) {
		if(!value.isValue()) {
			throw new TtcnError("The first argument of function substr() is a template with non-specific value.");
		}
		
		return subString(value.valueOf(), idx, returncount);
	}

	public static TitanUniversalCharString subString(final TitanUniversalCharString_template value, final int idx, final TitanInteger returncount) {
		if(!value.isValue()) {
			throw new TtcnError("The first argument of function substr() is a template with non-specific value.");
		}
		
		return subString(value.valueOf(), idx, returncount);
	}

	public static TitanUniversalCharString subString(final TitanUniversalCharString_template value, final TitanInteger idx, final int returncount) {
		if(!value.isValue()) {
			throw new TtcnError("The first argument of function substr() is a template with non-specific value.");
		}
		
		return subString(value.valueOf(), idx, returncount);
	}

	public static TitanUniversalCharString subString(final TitanUniversalCharString_template value, final TitanInteger idx, final TitanInteger returncount) {
		if(!value.isValue()) {
			throw new TtcnError("The first argument of function substr() is a template with non-specific value.");
		}
		
		return subString(value.valueOf(), idx, returncount);
	}

	// C.35 - replace

	public static void check_replace_arguments(final int value_length, final int idx, final int len, final String string_type, final String element_name) {
		if(idx < 0) {
			throw new TtcnError(MessageFormat.format("The second argument (index) of function replace() is a negative integer value: {0}.", idx));
		}
		if(idx > value_length) {
			throw new TtcnError(MessageFormat.format("The second argument (index) of function replace(), which is {0}, is greater than the length of the {1} value: {2}.",  
					idx, string_type, value_length));
		}
		if(len < 0) {
			throw new TtcnError(MessageFormat.format("The third argument (len) of function replace() is a negative integer value: {0}." , len));
		}
		if(len > value_length) {
			throw new TtcnError(MessageFormat.format("The third argument (len) of function replace(), which is {0}, is greater than the length of the {1} value: {2}.", len, string_type, value_length));
		}
		if(idx + len > value_length) {
			throw new TtcnError(MessageFormat.format("The first argument of function replace(), the length of which is {0}, does not have enough {1}s starting at index {2}: {3} {4}{5} needed, but there {6} only {7}.",
				    value_length, element_name, idx, len, element_name, len > 1 ? "s are" : " is", value_length - idx > 1 ? "are" : "is", value_length - idx));
		}
	}

	public static TitanBitString replace(final TitanBitString value, final int idx, final int len, final TitanBitString repl) {
		value.mustBound("The first argument (value) of function replace() is an unbound bitstring value.");
		repl.mustBound("The fourth argument (repl) of function replace() is an unbound bitstring value.");

		final int value_len = value.lengthOf().getInt();

		check_replace_arguments(value_len, idx, len, "bitstring", "bit");

		final int repl_len = repl.lengthOf().getInt();
		final StringBuilder temp_sb = new StringBuilder();
		for (int i = 0; i < value_len; i++) {
			temp_sb.append('0');
		}
		final TitanBitString ret_val = new TitanBitString(temp_sb.toString());

		for (int i = 0; i < idx; i++) {
			ret_val.setBit(i, value.getBit(i));
		}
		for (int i = 0; i < repl_len; i++) {
			ret_val.setBit(i + idx, repl.getBit(i));
		}
		for (int i = 0; i < value_len - idx - len; i++) {
			ret_val.setBit(i + idx + repl_len, value.getBit(idx + len + i));
		}
		return ret_val;
	}

	public static TitanBitString replace(final TitanBitString value, final int idx, final TitanInteger len, final TitanBitString repl) {
		len.mustBound("The third argument (len) of function replace() is an unbound integer value.");

		return replace(value, idx, len.getInt(), repl);
	}

	public static TitanBitString replace(final TitanBitString value, final TitanInteger idx, final int len, final TitanBitString repl) {
		idx.mustBound("The second argument (index) of function replace() is an unbound integer value.");

		return replace(value, idx.getInt(), len, repl);
	}

	public static TitanBitString replace(final TitanBitString value, final TitanInteger idx, final TitanInteger len, final TitanBitString repl) {
		idx.mustBound("The second argument (index) of function replace() is an unbound integer value.");
		len.mustBound("The third argument (len) of function replace() is an unbound integer value.");

		return replace(value, idx.getInt(), len.getInt(), repl);
	}

	public static TitanHexString replace(final TitanHexString value, final int idx, final int len, final TitanHexString repl) {
		value.mustBound("The first argument (value) of function replace() is an unbound hexstring value.");
		repl.mustBound("The fourth argument (repl) of function replace() is an unbound hexstring value.");

		final int value_len = value.lengthOf().getInt();

		check_replace_arguments(value_len, idx, len, "hexstring", "hexadecimal digit");

		final int repl_len = repl.lengthOf().getInt();
		final List<Byte> ret_val = new ArrayList<Byte>(value_len + repl_len - len);

		for (int i = 0; i < idx; i++) {
			ret_val.add(i, value.get_nibble(i));
		}
		for (int i = 0; i < repl_len; i++) {
			ret_val.add(idx + i, repl.get_nibble(i));
		}
		for (int i = 0; i < value_len - idx - len; i++) {
			ret_val.add(idx + i + repl_len, value.get_nibble(idx + i + len));
		}

		return new TitanHexString(ret_val);
	}

	public static TitanHexString replace(final TitanHexString value, final int idx, final TitanInteger len, final TitanHexString repl) {
		len.mustBound("The third argument (len) of function replace() is an unbound integer value.");

		return replace(value, idx, len.getInt(), repl);
	}

	public static TitanHexString replace(final TitanHexString value, final TitanInteger idx, final int len, final TitanHexString repl) {
		idx.mustBound("The second argument (index) of function replace() is an unbound integer value.");

		return replace(value, idx.getInt(), len, repl);
	}

	public static TitanHexString replace(final TitanHexString value, final TitanInteger idx, final TitanInteger len, final TitanHexString repl) {
		idx.mustBound("The second argument (index) of function replace() is an unbound integer value.");
		len.mustBound("The third argument (len) of function replace() is an unbound integer value.");

		return replace(value, idx.getInt(), len.getInt(), repl);
	}

	public static TitanOctetString replace(final TitanOctetString value, final int idx, final int len, final TitanOctetString repl) {
		value.mustBound("The first argument (value) of function replace() is an unbound octetstring value.");
		repl.mustBound("The fourth argument (repl) of function replace() is an unbound octetstring value.");

		final int value_len = value.lengthOf().getInt();

		check_replace_arguments(value_len, idx, len, "octetstring", "octet");

		final int repl_len = repl.lengthOf().getInt();
		final List<Character> ret_val = new ArrayList<Character>(value_len + repl_len - len);

		for (int i = 0; i < idx; i++) {
			ret_val.add(i, value.get_nibble(i));
		}
		for (int i = 0; i < repl_len; i++) {
			ret_val.add(idx + i, repl.get_nibble(i));
		}
		for (int i = 0; i < value_len - idx - len; i++) {
			ret_val.add(idx + i + repl_len, value.get_nibble(idx + i + len));
		}

		return new TitanOctetString(ret_val);
	}

	public static TitanOctetString replace(final TitanOctetString value, final int idx, final TitanInteger len, final TitanOctetString repl) {
		len.mustBound("The third argument (len) of function replace() is an unbound integer value.");

		return replace(value, idx, len.getInt(), repl);
	}

	public static TitanOctetString replace(final TitanOctetString value, final TitanInteger idx, final int len, final TitanOctetString repl) {
		idx.mustBound("The second argument (index) of function replace() is an unbound integer value.");

		return replace(value, idx.getInt(), len, repl);
	}

	public static TitanOctetString replace(final TitanOctetString value, final TitanInteger idx, final TitanInteger len, final TitanOctetString repl) {
		idx.mustBound("The second argument (index) of function replace() is an unbound integer value.");
		len.mustBound("The third argument (len) of function replace() is an unbound integer value.");

		return replace(value, idx.getInt(), len.getInt(), repl);
	}

	public static TitanCharString replace(final TitanCharString value, final int idx, final int len, final TitanCharString repl) {
		value.mustBound("The first argument (value) of function replace() is an unbound charstring value.");
		repl.mustBound("The fourth argument (repl) of function replace() is an unbound charstring value.");

		final int value_len = value.lengthOf().getInt();

		check_replace_arguments(value_len, idx, len, "charstring", "character");

		final StringBuilder ret_val = new StringBuilder();
		ret_val.append(value.getValue().toString());

		ret_val.replace(idx, idx + len, repl.getValue().toString());

		return new TitanCharString(ret_val);
	}

	public static TitanCharString replace(final TitanCharString value, final int idx, final TitanInteger len, final TitanCharString repl) {
		len.mustBound("The third argument (len) of function replace() is an unbound integer value.");

		return replace(value, idx, len.getInt(), repl);
	}

	public static TitanCharString replace(final TitanCharString value, final TitanInteger idx, final int len, final TitanCharString repl) {
		idx.mustBound("The second argument (index) of function replace() is an unbound integer value.");

		return replace(value, idx.getInt(), len, repl);
	}

	public static TitanCharString replace(final TitanCharString value, final TitanInteger idx, final TitanInteger len, final TitanCharString repl) {
		idx.mustBound("The second argument (index) of function replace() is an unbound integer value.");
		len.mustBound("The third argument (len) of function replace() is an unbound integer value.");

		return replace(value, idx.getInt(), len.getInt(), repl);
	}

	public static TitanUniversalCharString replace(final TitanUniversalCharString value, final int idx, final int len, final TitanUniversalCharString repl) {
		value.mustBound("The first argument (value) of function replace() is an unbound universal charstring value.");
		repl.mustBound("The fourth argument (repl) of function replace() is an unbound universal charstring value.");

		final int value_len = value.lengthOf().getInt();

		check_replace_arguments(value_len, idx, len, "universal charstring", "character");

		final int repl_len = repl.lengthOf().getInt();

		if (value.charstring) {
			if (repl.charstring) {
				final StringBuilder ret_val = new StringBuilder();
				ret_val.append(value.cstr.toString());
				ret_val.replace(idx, idx + len, repl.cstr.toString());
				return new TitanUniversalCharString(ret_val);
			} else {
				final List<TitanUniversalChar> ret_val = new ArrayList<TitanUniversalChar>();
				for (int i = 0; i < idx; i++) {
					ret_val.add(i, new TitanUniversalChar((char) 0, (char) 0, (char) 0, value.cstr.charAt(i)));
				}
				for (int i = 0; i < repl_len; i++) {
					ret_val.add(idx + i, repl.val_ptr.get(i));
				}
				for (int i = 0; i < value_len - idx - len; i++) {
					ret_val.add(idx + i + repl_len,
							new TitanUniversalChar((char) 0, (char) 0, (char) 0, value.cstr.charAt((idx + i + len))));
				}
				return new TitanUniversalCharString(ret_val);
			}
		} else {
			if (repl.charstring) {
				final List<TitanUniversalChar> ret_val = new ArrayList<TitanUniversalChar>();
				for (int i = 0; i < idx; i++) {
					ret_val.add(idx + i, value.val_ptr.get(i));
				}
				for (int i = 0; i < repl_len; i++) {
					ret_val.add(i, new TitanUniversalChar((char) 0, (char) 0, (char) 0, repl.cstr.charAt(i)));
				}
				for (int i = 0; i < value_len - idx - len; i++) {
					ret_val.add(idx + i + repl_len, value.val_ptr.get(idx + i + len));
				}
				return new TitanUniversalCharString(ret_val);
			} else {
				final List<TitanUniversalChar> ret_val = new ArrayList<TitanUniversalChar>();
				for (int i = 0; i < idx; i++) {
					ret_val.add(i, value.val_ptr.get(i));
				}
				for (int i = 0; i < repl_len; i++) {
					ret_val.add(idx + i, repl.val_ptr.get(i));
				}
				for (int i = 0; i < value_len - idx - len; i++) {
					ret_val.add(idx + i + repl_len, value.val_ptr.get(idx + i + len));
				}
				return new TitanUniversalCharString(ret_val);
			}
		}
	}

	public static TitanUniversalCharString replace(final TitanUniversalCharString value, final int idx, final TitanInteger len, final TitanUniversalCharString repl) {
		len.mustBound("The third argument (len) of function replace() is an unbound integer value.");

		return replace(value, idx, len.getInt(), repl);
	}

	public static TitanUniversalCharString replace(final TitanUniversalCharString value, final TitanInteger idx, final int len, final TitanUniversalCharString repl) {
		idx.mustBound("The second argument (index) of function replace() is an unbound integer value.");

		return replace(value, idx.getInt(), len, repl);
	}

	public static TitanUniversalCharString replace(final TitanUniversalCharString value, final TitanInteger idx, final TitanInteger len, final TitanUniversalCharString repl) {
		idx.mustBound("The second argument (index) of function replace() is an unbound integer value.");
		len.mustBound("The third argument (len) of function replace() is an unbound integer value.");

		return replace(value, idx.getInt(), len.getInt(), repl);
	}

	// replace on templates

	public static TitanBitString replace(final TitanBitString_template value, final int idx, final int len, final TitanBitString_template repl) {
		if (!value.isValue()) {
			throw new TtcnError("The first argument of function replace() is a template with non-specific value.");
		}
		if (!repl.isValue()) {
			throw new TtcnError("The fourth argument of function replace() is a template with non-specific value.");
		}

		return replace(value.valueOf(), idx, len, repl.valueOf());
	}

	public static TitanBitString replace(final TitanBitString_template value, final int idx, final TitanInteger len, final TitanBitString_template repl) {
		if (!value.isValue()) {
			throw new TtcnError("The first argument of function replace() is a template with non-specific value.");
		}
		if (!repl.isValue()) {
			throw new TtcnError("The fourth argument of function replace() is a template with non-specific value.");
		}

		return replace(value.valueOf(), idx, len, repl.valueOf());
	}

	public static TitanBitString replace(final TitanBitString_template value, final TitanInteger idx, final int len, final TitanBitString_template repl) {
		if (!value.isValue()) {
			throw new TtcnError("The first argument of function replace() is a template with non-specific value.");
		}
		if (!repl.isValue()) {
			throw new TtcnError("The fourth argument of function replace() is a template with non-specific value.");
		}

		return replace(value.valueOf(), idx, len, repl.valueOf());
	}

	public static TitanBitString replace(final TitanBitString_template value, final TitanInteger idx, final TitanInteger len, final TitanBitString_template repl) {
		if (!value.isValue()) {
			throw new TtcnError("The first argument of function replace() is a template with non-specific value.");
		}
		if (!repl.isValue()) {
			throw new TtcnError("The fourth argument of function replace() is a template with non-specific value.");
		}

		return replace(value.valueOf(), idx, len, repl.valueOf());
	}

	public static TitanHexString replace(final TitanHexString_template value, final int idx, final int len, final TitanHexString_template repl) {
		if (!value.isValue()) {
			throw new TtcnError("The first argument of function replace() is a template with non-specific value.");
		}
		if (!repl.isValue()) {
			throw new TtcnError("The fourth argument of function replace() is a template with non-specific value.");
		}

		return replace(value.valueOf(), idx, len, repl.valueOf());
	}

	public static TitanHexString replace(final TitanHexString_template value, final int idx, final TitanInteger len, final TitanHexString_template repl) {
		if (!value.isValue()) {
			throw new TtcnError("The first argument of function replace() is a template with non-specific value.");
		}
		if (!repl.isValue()) {
			throw new TtcnError("The fourth argument of function replace() is a template with non-specific value.");
		}

		return replace(value.valueOf(), idx, len, repl.valueOf());
	}

	public static TitanHexString replace(final TitanHexString_template value, final TitanInteger idx, final int len, final TitanHexString_template repl) {
		if (!value.isValue()) {
			throw new TtcnError("The first argument of function replace() is a template with non-specific value.");
		}
		if (!repl.isValue()) {
			throw new TtcnError("The fourth argument of function replace() is a template with non-specific value.");
		}

		return replace(value.valueOf(), idx, len, repl.valueOf());
	}

	public static TitanHexString replace(final TitanHexString_template value, final TitanInteger idx, final TitanInteger len,
			final TitanHexString_template repl) {
		if (!value.isValue()) {
			throw new TtcnError("The first argument of function replace() is a template with non-specific value.");
		}
		if (!repl.isValue()) {
			throw new TtcnError("The fourth argument of function replace() is a template with non-specific value.");
		}

		return replace(value.valueOf(), idx, len, repl.valueOf());
	}

	public static TitanOctetString replace(final TitanOctetString_template value, final int idx, final int len, final TitanOctetString_template repl) {
		if (!value.isValue()) {
			throw new TtcnError("The first argument of function replace() is a template with non-specific value.");
		}
		if (!repl.isValue()) {
			throw new TtcnError("The fourth argument of function replace() is a template with non-specific value.");
		}

		return replace(value.valueOf(), idx, len, repl.valueOf());
	}

	public static TitanOctetString replace(final TitanOctetString_template value, final int idx, final TitanInteger len, final TitanOctetString_template repl) {
		if (!value.isValue()) {
			throw new TtcnError("The first argument of function replace() is a template with non-specific value.");
		}
		if (!repl.isValue()) {
			throw new TtcnError("The fourth argument of function replace() is a template with non-specific value.");
		}

		return replace(value.valueOf(), idx, len, repl.valueOf());
	}

	public static TitanOctetString replace(final TitanOctetString_template value, final TitanInteger idx, final int len, final TitanOctetString_template repl) {
		if (!value.isValue()) {
			throw new TtcnError("The first argument of function replace() is a template with non-specific value.");
		}
		if (!repl.isValue()) {
			throw new TtcnError("The fourth argument of function replace() is a template with non-specific value.");
		}

		return replace(value.valueOf(), idx, len, repl.valueOf());
	}

	public static TitanOctetString replace(final TitanOctetString_template value, final TitanInteger idx, final TitanInteger len, final TitanOctetString_template repl) {
		if (!value.isValue()) {
			throw new TtcnError("The first argument of function replace() is a template with non-specific value.");
		}
		if (!repl.isValue()) {
			throw new TtcnError("The fourth argument of function replace() is a template with non-specific value.");
		}

		return replace(value.valueOf(), idx, len, repl.valueOf());
	}

	public static TitanCharString replace(final TitanCharString_template value, final int idx, final int len, final TitanCharString_template repl) {
		if (!value.isValue()) {
			throw new TtcnError("The first argument of function replace() is a template with non-specific value.");
		}
		if (!repl.isValue()) {
			throw new TtcnError("The fourth argument of function replace() is a template with non-specific value.");
		}

		return replace(value.valueOf(), idx, len, repl.valueOf());
	}

	public static TitanCharString replace(final TitanCharString_template value, final int idx, final TitanInteger len, final TitanCharString_template repl) {
		if (!value.isValue()) {
			throw new TtcnError("The first argument of function replace() is a template with non-specific value.");
		}
		if (!repl.isValue()) {
			throw new TtcnError("The fourth argument of function replace() is a template with non-specific value.");
		}

		return replace(value.valueOf(), idx, len, repl.valueOf());
	}

	public static TitanCharString replace(final TitanCharString_template value, final TitanInteger idx, final int len, final TitanCharString_template repl) {
		if (!value.isValue()) {
			throw new TtcnError("The first argument of function replace() is a template with non-specific value.");
		}
		if (!repl.isValue()) {
			throw new TtcnError("The fourth argument of function replace() is a template with non-specific value.");
		}

		return replace(value.valueOf(), idx, len, repl.valueOf());
	}

	public static TitanCharString replace(final TitanCharString_template value, final TitanInteger idx, final TitanInteger len, final TitanCharString_template repl) {
		if (!value.isValue()) {
			throw new TtcnError("The first argument of function replace() is a template with non-specific value.");
		}
		if (!repl.isValue()) {
			throw new TtcnError("The fourth argument of function replace() is a template with non-specific value.");
		}

		return replace(value.valueOf(), idx, len, repl.valueOf());
	}

	public static TitanUniversalCharString replace(final TitanUniversalCharString_template value, final int idx, final int len, final TitanUniversalCharString_template repl) {
		if (!value.isValue()) {
			throw new TtcnError("The first argument of function replace() is a template with non-specific value.");
		}
		if (!repl.isValue()) {
			throw new TtcnError("The fourth argument of function replace() is a template with non-specific value.");
		}

		return replace(value.valueOf(), idx, len, repl.valueOf());
	}

	public static TitanUniversalCharString replace(final TitanUniversalCharString_template value, final int idx, final TitanInteger len, final TitanUniversalCharString_template repl) {
		if (!value.isValue()) {
			throw new TtcnError("The first argument of function replace() is a template with non-specific value.");
		}
		if (!repl.isValue()) {
			throw new TtcnError("The fourth argument of function replace() is a template with non-specific value.");
		}

		return replace(value.valueOf(), idx, len, repl.valueOf());
	}

	public static TitanUniversalCharString replace(final TitanUniversalCharString_template value, final TitanInteger idx, final int len, final TitanUniversalCharString_template repl) {
		if (!value.isValue()) {
			throw new TtcnError("The first argument of function replace() is a template with non-specific value.");
		}
		if (!repl.isValue()) {
			throw new TtcnError("The fourth argument of function replace() is a template with non-specific value.");
		}

		return replace(value.valueOf(), idx, len, repl.valueOf());
	}

	public static TitanUniversalCharString replace(final TitanUniversalCharString_template value, final TitanInteger idx, final TitanInteger len, final TitanUniversalCharString_template repl) {
		if (!value.isValue()) {
			throw new TtcnError("The first argument of function replace() is a template with non-specific value.");
		}
		if (!repl.isValue()) {
			throw new TtcnError("The fourth argument of function replace() is a template with non-specific value.");
		}

		return replace(value.valueOf(), idx, len, repl.valueOf());
	}

	// Additional predefined functions defined in Annex B of ES 101 873-7

	// B.1 decomp - not implemented yet

	// Non-standard functions

	// str2bit
	public static TitanBitString str2bit(final String value) {
		if (value == null) {
			return new TitanBitString();
		} else {
			return str2bit(new TitanCharString(value));
		}
	}

	public static TitanBitString str2bit(final TitanCharString value) {
		value.mustBound("The argument of function str2bit() is an unbound charstring value.");

		final int value_length = value.lengthOf().getInt();
		final StringBuilder chars_ptr = new StringBuilder();
		chars_ptr.append(value.getValue().toString());
		final StringBuilder ret_val = new StringBuilder();

		for (int i = 0; i < value_length; i++) {
			final char c = chars_ptr.charAt(i);
			switch (c) {
			case '0':
				ret_val.append('0');
				break;
			case '1':
				ret_val.append('1');
				break;
			default:
				// TODO: Initial implementation
				throw new TtcnError(MessageFormat.format("The argument of function str2bit() shall contain characters '0' and '1' only, but character {0} was found at index {1}.", c, i));
			}
		}

		return new TitanBitString(ret_val.toString());
	}

	public static TitanBitString str2bit(final TitanCharString_Element value) {
		value.mustBound("The argument of function str2bit() is an unbound charstring element.");

		final char c = value.get_char();
		if (c != '0' && c != '1') {
			// TODO: Initial implementation
			throw new TtcnError(MessageFormat.format("The argument of function str2bit() shall contain characters `0' and `1' only, but the given charstring element contains the character {0}.", c));
		}

		return new TitanBitString((value.get_char() == '1' ? "1" : "0"));
	}

	// str2hex
	public static TitanHexString str2hex(final String value) {
		if (value == null) {
			return new TitanHexString();
		} else {
			return str2hex(new TitanCharString(value));
		}
	}

	public static TitanHexString str2hex(final TitanCharString value) {
		value.mustBound("The argument of function str2hex() is an unbound charstring value.");

		final int value_length = value.lengthOf().getInt();
		final StringBuilder chars_ptr = new StringBuilder();
		chars_ptr.append(value.getValue().toString());
		final List<Byte> ret_val = new ArrayList<Byte>(value_length);

		for (int i = 0; i < value_length; i++) {
			final char c = chars_ptr.charAt(i);
			final byte hexdigit = charToHexDigit(c);
			if (hexdigit > 0x0F) {
				// TODO: Initial implementation
				throw new TtcnError(MessageFormat.format("The argument of function str2hex() shall contain hexadecimal digits only, but character {0} was found at index {1}.", c, i));
			}
			ret_val.add(hexdigit);
		}

		return new TitanHexString(ret_val);
	}

	public static TitanHexString str2hex(final TitanCharString_Element value) {
		value.mustBound("The argument of function str2hex() is an unbound charstring element.");

		final char c = value.get_char();
		final byte hexdigit = charToHexDigit(c);

		if (hexdigit > 0x0F) {
			// TODO: Initial implementation
			throw new TtcnError(MessageFormat.format( "The argument of function str2hex() shall contain only hexadecimal digits, but the given charstring element contains the character {0} .", c));
		}

		return new TitanHexString(hexdigit);
	}

	// float2str
	public static TitanCharString float2str(final TitanFloat value) {
		value.mustBound("The argument of function float2str() is an unbound float value.");

		return new TitanCharString(value.toString());
	}

	// unichar2char
	public static TitanCharString unichar2char(final TitanUniversalCharString value) {
		value.mustBound("The argument of function unichar2char() is an unbound universal charstring value.");

		final int value_length = value.lengthOf().getInt();
		if (value.charstring) {
			return new TitanCharString(value.cstr);
		} else {
			final StringBuilder ret_val = new StringBuilder();
			final List<TitanUniversalChar> uchars_ptr =  value.val_ptr;
			for (int i = 0; i < value_length; i++) {
				final TitanUniversalChar uchar = uchars_ptr.get(i);
				if (uchar.getUc_group() != 0 || uchar.getUc_plane() != 0 || uchar.getUc_row() != 0 || uchar.getUc_cell() > 127) {
					//TODO: Initial implementation
					throw new TtcnError(MessageFormat.format("The characters in the argument of function unichar2char() shall be within the range char(0, 0, 0, 0) .. "
							+ "char(0, 0, 0, 127), but quadruple char({0}, {1}, {2}, {3}) was found at index {4}." , 
							uchar.getUc_group(),uchar.getUc_plane(),uchar.getUc_row(),uchar.getUc_row(),i));
				}
				ret_val.append((char) uchar.getUc_cell());
			}

			return new TitanCharString(ret_val);
		}
	}

	public static TitanCharString unichar2char(final TitanUniversalCharString_Element value) {
		value.mustBound("The argument of function unichar2char() is an unbound universal charstring element.");

		final TitanUniversalChar uchar = value.get_char();
		if (uchar.getUc_group() != 0 || uchar.getUc_plane() != 0 || uchar.getUc_row() != 0 || uchar.getUc_cell() > 127) {
			// TODO: Initial implementation
			throw new TtcnError(MessageFormat.format("The characters in the argument of function unichar2char() shall be within the range char(0, 0, 0, 0) .. char(0, 0, 0, 127), " 
					+ "but the given universal charstring element contains the quadruple char({0}, {1}, {2}, {3})." , uchar.getUc_group(),uchar.getUc_plane(),uchar.getUc_row(),uchar.getUc_row()));
		}

		return new TitanCharString(String.valueOf(uchar.getUc_cell()));
	}

	//TODO: C.33 - regexp
	//TODO: C.36 - rnd
}