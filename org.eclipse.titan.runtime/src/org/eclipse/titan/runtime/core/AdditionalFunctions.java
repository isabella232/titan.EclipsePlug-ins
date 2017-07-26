/******************************************************************************
 * Copyright (c) 2000-2017 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

import java.math.BigInteger;
import java.text.MessageFormat;
import java.util.ArrayList;

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
	public static TitanHexString int2hex(final int value, final int length){
		return int2hex(new TitanInteger(value), length);
	}

	public static TitanHexString int2hex(final TitanInteger value, final int length){
		value.mustBound("The first argument (value) of function int2hex() is an unbound integer value.");

		if(TitanBoolean.getNative(value.isLessThan(0))){
			throw new TtcnError(MessageFormat.format("The first argument (value) of function int2hex() is a  negative integer value: {0}.",value));
		}
		if(length < 0){
			throw new TtcnError(MessageFormat.format("The second argument (length) of function int2hex() is a negative integer value: {0}.", length));
		}
		if(value.isNative()){
			int tmp_value = value.getInt();
			ArrayList<Byte> nibbles_ptr = new ArrayList<Byte>(length);
			for (int i = 0; i < length; i++) {
				nibbles_ptr.add((byte)0);
			}
			for(int i = length - 1; i >= 0; i--){
				nibbles_ptr.set(i,(byte)( tmp_value & 0xF));
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
				nibbles_ptr.add((byte)0);
			}
			for(int i = length - 1; i >= 0; i--){
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
	
	public static TitanHexString int2hex(final TitanInteger value, TitanInteger length){
		value.mustBound("The first argument (value) of function int2hex() is an unbound integer value.");
		length.mustBound("The second argument (length) of function int2hex() is an unbound integer value.");
		
		return int2hex(value,length.getInt());
	}
	
	public static TitanHexString int2hex(final int value, final TitanInteger length) {
		length.mustBound("The second argument (length) of function int2hex() is an unbound integer value.");

		return int2hex(value, length.getInt());
	}
}
