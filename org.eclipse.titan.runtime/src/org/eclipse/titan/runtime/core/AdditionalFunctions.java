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
public class AdditionalFunctions {

	private AdditionalFunctions() {
		//intentionally private to disable instantiation
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

		int ivt = value.getInt();
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

		int ivt = value.getInt();
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

		if (TitanBoolean.getNative(value.isLessThan(0))) {
			throw new TtcnError(MessageFormat.format("The first argument (value) of function int2bit() is a negative integer value: {0}.", value));
		}
		if (length < 0) {
			throw new TtcnError(MessageFormat.format("The second argument (length) of function int2bit() is a negative integer value: {0}.", length));
		}

		if (value.isNative()) {
			int tempValue = value.getInt();
			ArrayList<Byte> bits_ptr = new ArrayList<Byte>((length + 7) / 8);
			for (int i = 0; i < (length + 7) / 8; i++) {
				bits_ptr.add(new Byte((byte)0));
			}
			for (int i = length - 1; tempValue != 0 && i >= 0; i--) {
				if((tempValue & 1) > 0) {
					int temp = bits_ptr.get(i / 8) | (1 << (i % 8));
					bits_ptr.set(i / 8, new Byte((byte) temp));
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
			ArrayList<Byte> bits_ptr = new ArrayList<Byte>((length + 7) / 8);
			for (int i = 0; i < (length + 7) / 8; i++) {
				bits_ptr.add(new Byte((byte)0));
			}
			for (int i = length - 1; tempValue.compareTo(BigInteger.ZERO) == 1 && i >= 0; i--) {
				if((tempValue.and(BigInteger.ONE)).compareTo(BigInteger.ZERO) == 1) {
					int temp = bits_ptr.get(i / 8) | (1 << (i % 8));
					bits_ptr.set(i / 8, new Byte((byte) temp));
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

		if (TitanBoolean.getNative(value.isLessThan(0))) {
			throw new TtcnError(MessageFormat.format("The first argument (value) of function int2hex() is a  negative integer value: {0}.", value));
		}
		if (length < 0) {
			throw new TtcnError(MessageFormat.format("The second argument (length) of function int2hex() is a negative integer value: {0}.", length));
		}
		if (value.isNative()) {
			int tmp_value = value.getInt();
			ArrayList<Byte> nibbles_ptr = new ArrayList<Byte>(length);
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
			ArrayList<Byte> nibbles_ptr = new ArrayList<Byte>(length);
			for (int i = 0; i < length; i++) {
				nibbles_ptr.add((byte) 0);
			}
			for (int i = length - 1; i >= 0; i--) {
				BigInteger temp = tmp_value.and(BigInteger.valueOf(0xF));
				nibbles_ptr.set(i, temp.byteValue());
				tmp_value = tmp_value.shiftRight(4);
			}
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
		List<Character> octets_ptr = new ArrayList<Character>(length);
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
			if (value.isLessThan(0).getValue()) {
				throw new TtcnError(MessageFormat.format("The first argument (value) of function int2oct() is a negative integer value: {0}.", value));
			}
			if (length < 0) {
				throw new TtcnError(MessageFormat.format("The second argument (length) of function int2oct() is a negative integer value: {0}.", length));
			}
			if ((tmp_val.bitCount() + 7) / 4 < length) {
				throw new TtcnError(MessageFormat.format("The first argument of function int2oct(), which is {0}, does not fit in {1} octet{2}.", (tmp_val.bitCount() + 7) / 4, length, length > 1 ? "s" : ""));
			}
			List<Character> octets_ptr = new ArrayList<Character>(length);
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

		List<Character> octets_ptr = new ArrayList<Character>();
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
		int result = (value.getUc_group() << 24) | (value.getUc_plane() << 16) | (value.getUc_row() << 8) | value.getUc_cell();

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

		int n_bits = value.lengthOf().getInt();
		List<Byte> temp = new ArrayList<Byte>();
		temp = value.getValue();

		// skip the leading zero bits
		int start_index = 0;
		for (; start_index < n_bits; start_index++) {
			if ((temp.get(start_index / 8) & (1 << (start_index % 8))) != 0) {
				break;
			}
		}
		// do the conversion
		BigInteger ret_val = new BigInteger("0");
		for (int i = start_index; i < n_bits; i++) {
			ret_val = ret_val.shiftLeft(1);
			if ((temp.get(i / 8) & (1 << (i % 8))) != 0) {
				ret_val = ret_val.add(new BigInteger("1"));
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

		int n_bits = value.lengthOf().getInt();
		List<Byte> ret_val = new ArrayList<Byte>();
		StringBuilder sb = new StringBuilder();

		// reverse the order
		for (int i = n_bits - 1; i >= 0; i--) {
			sb.append(value.getBit(i) ? "1" : "0");
		}

		TitanBitString temp_val = new TitanBitString(sb.toString());
		List<Byte> bits_ptr = new ArrayList<Byte>();
		bits_ptr = temp_val.getValue();
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

		int n_bits = value.lengthOf().getInt();
		int n_nibbles = (n_bits + 3) / 4;
		List<Byte> nibbles_ptr = new ArrayList<Byte>();
		StringBuilder sb = new StringBuilder();

		// reverse the order
		for (int i = n_bits - 1; i >= 0; i--) {
			sb.append(value.getBit(i) ? "1" : "0");
		}

		if (n_nibbles % 2 == 1) {
			nibbles_ptr.add((byte) 0);
		}

		TitanBitString temp_val = new TitanBitString(sb.toString());
		List<Byte> bits_ptr = new ArrayList<Byte>();
		bits_ptr = temp_val.getValue();

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
		List<Character> ret_val = new ArrayList<Character>();
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

		int n_bits = value.lengthOf().getInt();
		StringBuilder ret_val = new StringBuilder(n_bits);

		for (int i = 0; i < n_bits; i++) {
			if (value.getBit(i)) {
				ret_val.append("1");
			} else {
				ret_val.append("0");
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

		int n_nibbles = value.lengthOf().getInt();

		// skip the leading zero hex digits
		int start_index = 0;
		for (start_index = 0; start_index < n_nibbles; start_index++) {
			if (value.get_nibble(start_index) != 0) {
				break;
			}
		}

		// do the conversion
		BigInteger ret_val = new BigInteger("0");
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

		int n_nibbles = value.lengthOf().getInt();
		List<Byte> bits_ptr = new ArrayList<Byte>();
		List<Byte> nibbles_ptr = new ArrayList<Byte>();
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
		List<Byte> bits_ptr = new ArrayList<Byte>();
		bits_ptr.add(bits);

		return new TitanBitString(bits_ptr, 4);
	}

	// C.18 - hex2oct
	public static TitanOctetString hex2oct(final TitanHexString value) {
		value.mustBound("The argument of function hex2oct() is an unbound hexstring value.");

		int n_nibbles = value.lengthOf().getInt();
		int n_octets = (n_nibbles + 1) / 2;
		List<Character> octet_ptr = new ArrayList<Character>(n_octets);
		List<Byte> nibbles_ptr = new ArrayList<Byte>();

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
		char octet = value.get_nibble();
		return new TitanOctetString(octet);
	}

	// C.19 - hex2str
	public static TitanCharString hex2str(final TitanHexString value) {
		value.mustBound("The argument of function hex2str() is an unbound hexstring value.");

		int n_nibbles = value.lengthOf().getInt();
		StringBuilder ret_val = new StringBuilder();
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

		int n_octets = value.lengthOf().getInt();

		// skip the leading zero hex digits
		int start_index = 0;
		for (start_index = 0; start_index < n_octets; start_index++) {
			if (value.get_nibble(start_index) != 0) {
				break;
			}
		}

		// do the conversion
		BigInteger ret_val = new BigInteger("0");
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

		int n_octets = value.lengthOf().getInt();
		List<Byte> bits_ptr = new ArrayList<Byte>();
		List<Character> octets_ptr = new ArrayList<Character>();
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

		int n_octets = value.lengthOf().getInt();
		List<Byte> ret_val = new ArrayList<Byte>();
		List<Character> octets_ptr = new ArrayList<Character>();
		octets_ptr.addAll(value.getValue());

		for (int i = 0; i < n_octets; i++) {
			ret_val.add((byte) ((octets_ptr.get(i) & 0xF0) >> 4));
			ret_val.add((byte) (octets_ptr.get(i) & 0x0F));
		}

		return new TitanHexString(ret_val);
	}

	public static TitanHexString oct2hex(final TitanOctetString_Element value) {
		value.mustBound("The argument of function oct2hex() is an unbound octetstring element.");

		List<Byte> ret_val = new ArrayList<Byte>();
		ret_val.add((byte) ((value.get_nibble() & 0xF0) >> 4));
		ret_val.add((byte) (value.get_nibble() & 0x0F));

		return new TitanHexString(ret_val);
	}

	// C.23 - oct2str
	public static TitanCharString oct2str(final TitanOctetString value) {
		value.mustBound("The argument of function oct2str() is an unbound octetstring value.");

		int n_octets = value.lengthOf().getInt();
		StringBuilder ret_val = new StringBuilder();
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

		int value_length = value.lengthOf().getInt();
		StringBuilder sb = new StringBuilder();
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

		char octet = value.get_nibble();
		if ((int) octet > 127) {
			throw new TtcnError(MessageFormat.format("The argument of function oct2char() contains the octet {0}, which is outside the allowed range 00 .. 7F.", octet));
		}

		return new TitanCharString(String.valueOf(octet));
	}
}
