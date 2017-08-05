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
			throw new TtcnError("The argument of function int2unichar() is {0}, which outside the allowed range 0 .. 2147483647.");
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
			tmp_value.shiftRight(4);
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

	public static TitanHexString int2hex(final TitanInteger value, TitanInteger length) {
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
			throw new TtcnError("The first argument of function int2oct(), which is {0}, does not fit in {1} octet(s).");
		}
		return new TitanOctetString(octets_ptr);
	}

	public static TitanOctetString int2oct(final int value, final TitanInteger length) {
		length.mustBound("The second argument (length) of function int2oct() is an unbound integer value.");

		return int2oct(value, length.getInt());
	}

	public static TitanOctetString int2oct(final TitanInteger value, final int length){
		value.mustBound("The first argument (value) of function int2oct() is an unbound integer value.");

		if(value.isNative()){
			return int2oct(value.getInt(), length);
		} else{
			BigInteger tmp_val = value.getBigInteger();
			if(value.isLessThan(0).getValue()){
				throw new TtcnError("The first argument (value) of function int2oct() is a negative integer value: {0}.");
			}
			if (length < 0){
				throw new TtcnError("The second argument (length) of function int2oct() is a negative integer value: {0}.");
			}
			if((tmp_val.bitCount() + 7) / 4 < length){
				throw new TtcnError("The first argument of function int2oct(), which is {0}, does not fit in {1} octet(s).");
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

		if(value.isNative()){
			return int2oct(value.getInt(), length.getInt());
		}	
		return int2oct(value, length.getInt());
	}

	// C.6 - int2str
	public static TitanCharString int2str(final int value){

		return new TitanCharString(Integer.valueOf(value).toString());
	}

	public static TitanCharString int2str(final TitanInteger value){
		value.mustBound("The argument of function int2str() is an unbound integer value.");

		if(value.isNative()){
			return int2str(value.getInt());
		}
		return new TitanCharString(value.getBigInteger().toString());
	}

	// C.7 - int2float
	public static TitanFloat int2float(final int value){

		return new TitanFloat((double) value);
	}

	public static TitanFloat int2float(final TitanInteger value){
		value.mustBound("The argument of function int2float() is an unbound integer value.");

		if(value.isNative()){
			return int2float(value.getInt());
		}

		return new TitanFloat(value.getBigInteger().doubleValue());
	}

	// C.8 - float2int
	public static TitanInteger float2int(double value){
		if(value > Integer.MIN_VALUE && value < Integer.MAX_VALUE){
			return new TitanInteger((int) value);
		}
		return new TitanInteger(new BigDecimal(value).toBigInteger());
	}

	public static TitanInteger float2int(TitanFloat value){
		value.mustBound("The argument of function float2int() is an unbound float value.");

		return float2int(value.getValue());
	}

	// C.9 - char2int
	public static TitanInteger char2int(char value){
		if(value > 127){
			throw new TtcnError("The argument of function char2int() contains a character with character code {0}, which is outside the allowed range 0 .. 127.");
		}
		return new TitanInteger((int) value);
	}

	public static TitanInteger char2int(String value){
		if(value == null){
			value = "";
		}
		if(value.length() != 1){	
			throw new TtcnError(MessageFormat.format("The length of the argument in function char2int() must be exactly 1 instead of {0}.",value.length()));
		}
		return char2int(value.charAt(0));
	}

	public static TitanInteger char2int(final TitanCharString value){
		value.mustBound("The argument of function char2int() is an unbound charstring value.");

		if(value.lengthOf().getInt() != 1){
			throw new TtcnError(MessageFormat.format("The length of the argument in function char2int() must be exactly 1 instead of {0}.",value.lengthOf()));
		}
		return char2int(value.constGetAt(0).get_char());
	}

	public static TitanInteger char2int(final TitanCharString_Element value){
		value.mustBound("The argument of function char2int() is an unbound charstring element.");

		return char2int(value.get_char());
	}

	// C.10 - char2oct
	public static TitanOctetString char2oct(String value){
		if(value == null){
			value = "";
		}
		if(value.length() <= 0){
			return new TitanOctetString("0");
		}
		List<Character> octets_ptr = new ArrayList<Character>();
		for (int i = 0; i < value.length(); i++) {
			octets_ptr.add(int2oct((int)value.charAt(i), 1).get_nibble(0));
		}
		return new TitanOctetString(octets_ptr);

	}

	public static TitanOctetString char2oct(TitanCharString value){
		value.mustBound("The argument of function char2oct() is an unbound charstring value.");

		return char2oct(value.toString());
	}

	public static TitanOctetString char2oct(TitanCharString_Element value){
		value.mustBound("The argument of function char2oct() is an unbound charstring element.");

		return char2oct(String.valueOf(value.get_char()));
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


	// FIXME:bit2hex
	// C.13 - bit2hexnew
	public static TitanHexString bit2hex(final TitanBitString value) {
		value.mustBound("The argument of function bit2hex() is an unbound bitstring value.");

		int n_bits = value.lengthOf().getInt();
		int n_nibbles = (n_bits + 3) / 4;
		int padding_bits = 4 * n_nibbles - n_bits;
		List<Byte> ret_val = new ArrayList<Byte>();
		List<Byte> bits_ptr = new ArrayList<Byte>();
		bits_ptr = value.getValue();
		for (int i = 0; i < n_nibbles; i++) {
			ret_val.add((byte) 0);
		}

		for (int i = 0; i < n_bits; i++) {
			int temp2 = (bits_ptr.get(i / 8) & (1 << (i % 8)));
			if ((bits_ptr.get(i / 8) & (1 << (i % 8))) != 0) {
				int temp1 = (0x80 >> ((i + padding_bits) % 8)) >> 4;
			ret_val.set((i + padding_bits) / 8, (byte) (ret_val.get((i + padding_bits) / 8) | temp1));
			}
		}

		return new TitanHexString(ret_val);
		/* return int2hex(bit2int(value), n_nibbles); */
	}

}
