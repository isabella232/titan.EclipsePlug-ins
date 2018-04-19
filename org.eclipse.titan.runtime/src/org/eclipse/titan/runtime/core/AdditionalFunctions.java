/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
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
import java.util.Random;

import org.eclipse.titan.runtime.core.TitanCharString.CharCoding;

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
	// table to reverse the hex digits within an octet
	// input: ABCDEFGH, output: DCBAHGFE
	private static final int nibble_reverse_table[] = {
		0x00, 0x08, 0x04, 0x0c, 0x02, 0x0a, 0x06, 0x0e,
		0x01, 0x09, 0x05, 0x0d, 0x03, 0x0b, 0x07, 0x0f,
		0x80, 0x88, 0x84, 0x8c, 0x82, 0x8a, 0x86, 0x8e,
		0x81, 0x89, 0x85, 0x8d, 0x83, 0x8b, 0x87, 0x8f,
		0x40, 0x48, 0x44, 0x4c, 0x42, 0x4a, 0x46, 0x4e,
		0x41, 0x49, 0x45, 0x4d, 0x43, 0x4b, 0x47, 0x4f,
		0xc0, 0xc8, 0xc4, 0xcc, 0xc2, 0xca, 0xc6, 0xce,
		0xc1, 0xc9, 0xc5, 0xcd, 0xc3, 0xcb, 0xc7, 0xcf,
		0x20, 0x28, 0x24, 0x2c, 0x22, 0x2a, 0x26, 0x2e,
		0x21, 0x29, 0x25, 0x2d, 0x23, 0x2b, 0x27, 0x2f,
		0xa0, 0xa8, 0xa4, 0xac, 0xa2, 0xaa, 0xa6, 0xae,
		0xa1, 0xa9, 0xa5, 0xad, 0xa3, 0xab, 0xa7, 0xaf,
		0x60, 0x68, 0x64, 0x6c, 0x62, 0x6a, 0x66, 0x6e,
		0x61, 0x69, 0x65, 0x6d, 0x63, 0x6b, 0x67, 0x6f,
		0xe0, 0xe8, 0xe4, 0xec, 0xe2, 0xea, 0xe6, 0xee,
		0xe1, 0xe9, 0xe5, 0xed, 0xe3, 0xeb, 0xe7, 0xef,
		0x10, 0x18, 0x14, 0x1c, 0x12, 0x1a, 0x16, 0x1e,
		0x11, 0x19, 0x15, 0x1d, 0x13, 0x1b, 0x17, 0x1f,
		0x90, 0x98, 0x94, 0x9c, 0x92, 0x9a, 0x96, 0x9e,
		0x91, 0x99, 0x95, 0x9d, 0x93, 0x9b, 0x97, 0x9f,
		0x50, 0x58, 0x54, 0x5c, 0x52, 0x5a, 0x56, 0x5e,
		0x51, 0x59, 0x55, 0x5d, 0x53, 0x5b, 0x57, 0x5f,
		0xd0, 0xd8, 0xd4, 0xdc, 0xd2, 0xda, 0xd6, 0xde,
		0xd1, 0xd9, 0xd5, 0xdd, 0xd3, 0xdb, 0xd7, 0xdf,
		0x30, 0x38, 0x34, 0x3c, 0x32, 0x3a, 0x36, 0x3e,
		0x31, 0x39, 0x35, 0x3d, 0x33, 0x3b, 0x37, 0x3f,
		0xb0, 0xb8, 0xb4, 0xbc, 0xb2, 0xba, 0xb6, 0xbe,
		0xb1, 0xb9, 0xb5, 0xbd, 0xb3, 0xbb, 0xb7, 0xbf,
		0x70, 0x78, 0x74, 0x7c, 0x72, 0x7a, 0x76, 0x7e,
		0x71, 0x79, 0x75, 0x7d, 0x73, 0x7b, 0x77, 0x7f,
		0xf0, 0xf8, 0xf4, 0xfc, 0xf2, 0xfa, 0xf6, 0xfe,
		0xf1, 0xf9, 0xf5, 0xfd, 0xf3, 0xfb, 0xf7, 0xff
	};

	// table to reverse the bits within an octet
	// input: ABCDEFGH, output: HGFEDCBA
	private static final int bit_reverse_table[] = {
		0x00, 0x80, 0x40, 0xc0, 0x20, 0xa0, 0x60, 0xe0,
		0x10, 0x90, 0x50, 0xd0, 0x30, 0xb0, 0x70, 0xf0,
		0x08, 0x88, 0x48, 0xc8, 0x28, 0xa8, 0x68, 0xe8,
		0x18, 0x98, 0x58, 0xd8, 0x38, 0xb8, 0x78, 0xf8,
		0x04, 0x84, 0x44, 0xc4, 0x24, 0xa4, 0x64, 0xe4,
		0x14, 0x94, 0x54, 0xd4, 0x34, 0xb4, 0x74, 0xf4,
		0x0c, 0x8c, 0x4c, 0xcc, 0x2c, 0xac, 0x6c, 0xec,
		0x1c, 0x9c, 0x5c, 0xdc, 0x3c, 0xbc, 0x7c, 0xfc,
		0x02, 0x82, 0x42, 0xc2, 0x22, 0xa2, 0x62, 0xe2,
		0x12, 0x92, 0x52, 0xd2, 0x32, 0xb2, 0x72, 0xf2,
		0x0a, 0x8a, 0x4a, 0xca, 0x2a, 0xaa, 0x6a, 0xea,
		0x1a, 0x9a, 0x5a, 0xda, 0x3a, 0xba, 0x7a, 0xfa,
		0x06, 0x86, 0x46, 0xc6, 0x26, 0xa6, 0x66, 0xe6,
		0x16, 0x96, 0x56, 0xd6, 0x36, 0xb6, 0x76, 0xf6,
		0x0e, 0x8e, 0x4e, 0xce, 0x2e, 0xae, 0x6e, 0xee,
		0x1e, 0x9e, 0x5e, 0xde, 0x3e, 0xbe, 0x7e, 0xfe,
		0x01, 0x81, 0x41, 0xc1, 0x21, 0xa1, 0x61, 0xe1,
		0x11, 0x91, 0x51, 0xd1, 0x31, 0xb1, 0x71, 0xf1,
		0x09, 0x89, 0x49, 0xc9, 0x29, 0xa9, 0x69, 0xe9,
		0x19, 0x99, 0x59, 0xd9, 0x39, 0xb9, 0x79, 0xf9,
		0x05, 0x85, 0x45, 0xc5, 0x25, 0xa5, 0x65, 0xe5,
		0x15, 0x95, 0x55, 0xd5, 0x35, 0xb5, 0x75, 0xf5,
		0x0d, 0x8d, 0x4d, 0xcd, 0x2d, 0xad, 0x6d, 0xed,
		0x1d, 0x9d, 0x5d, 0xdd, 0x3d, 0xbd, 0x7d, 0xfd,
		0x03, 0x83, 0x43, 0xc3, 0x23, 0xa3, 0x63, 0xe3,
		0x13, 0x93, 0x53, 0xd3, 0x33, 0xb3, 0x73, 0xf3,
		0x0b, 0x8b, 0x4b, 0xcb, 0x2b, 0xab, 0x6b, 0xeb,
		0x1b, 0x9b, 0x5b, 0xdb, 0x3b, 0xbb, 0x7b, 0xfb,
		0x07, 0x87, 0x47, 0xc7, 0x27, 0xa7, 0x67, 0xe7,
		0x17, 0x97, 0x57, 0xd7, 0x37, 0xb7, 0x77, 0xf7,
		0x0f, 0x8f, 0x4f, 0xcf, 0x2f, 0xaf, 0x6f, 0xef,
		0x1f, 0x9f, 0x5f, 0xdf, 0x3f, 0xbf, 0x7f, 0xff
	};

	private static final char UTF8_BOM[] =  {0xef, 0xbb, 0xbf};
	private static final char UTF16BE_BOM[] = {0xfe, 0xff};
	private static final char UTF16LE_BOM[] = {0xff, 0xfe};
	private static final char UTF32BE_BOM[] = {0x00, 0x00, 0xfe, 0xff};
	private static final char UTF32LE_BOM[] = {0xff, 0xfe, 0x00, 0x00};

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

	private static CharCoding is_ascii(final TitanOctetString ostr) {
		final int nonASCII = 1 << 7; // MSB is 1 in case of non ASCII character
		CharCoding ret = CharCoding.ASCII;
		char[] strptr = ostr.getValue();
		for (int i = 0; i < strptr.length; i++) {
			if (((strptr[i] & 0xFF) & nonASCII) != 0) {
				ret = CharCoding.UNKNOWN;
				break;
			}
		}
		return ret;
	}

	private static CharCoding is_utf8(final TitanOctetString ostr) {
		final int MSB = 1 << 7; // MSB is 1 in case of non ASCII character
		final int MSBmin1 = 1 << 6; // 0100 0000 
		int i = 0;
		final char strptr[] = ostr.getValue();
		while (ostr.lengthOf().getInt() > i) {
			if (((strptr[i] & 0xFF) & MSB) != 0) { //non ASCII char
				int maskUTF8 = 1 << 6; // 111x xxxx shows how many additional bytes are there
				if (((strptr[i] & 0xFF) & maskUTF8) == 0) {
					return CharCoding.UNKNOWN; // accepted 11xxx xxxx but received 10xx xxxx
				}
				int noofUTF8 = 0; // 11xx xxxxx -> 2 bytes, 111x xxxxx -> 3 bytes , 1111 xxxxx -> 4 bytes in UTF-8
				while (((strptr[i] & 0xFF) & maskUTF8) != 0) {
					++noofUTF8;
					maskUTF8 >>= 1; // shift right the mask
				}
				// the second and third (and so on) UTF-8 byte looks like 10xx xxxx 
				while (0 < noofUTF8) {
					++i;
					if (i >= ostr.lengthOf().getInt() || ((strptr[i] & 0xFF) & MSB) == 0 || ((strptr[i] & 0xFF) & MSBmin1) != 0) { // if not like this: 10xx xxxx
						return CharCoding.UNKNOWN;
					}
					--noofUTF8;
				}
			}
			++i;
		}
		return CharCoding.UTF_8;
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
			final int bits_ptr[] = new int[(length + 7) / 8];
			for (int i = 0; i < (length + 7) / 8; i++) {
				bits_ptr[i] = 0;
			}
			for (int i = length - 1; tempValue != 0 && i >= 0; i--) {
				if ((tempValue & 1) > 0) {
					final int temp = bits_ptr[i / 8] | (1 << (i % 8));
					bits_ptr[i / 8] = temp;
				}
				tempValue >>= 1;
			}
			if (tempValue != 0) {
				int i = 0;
				while (tempValue != 0) {
					tempValue >>= 1;
					i++;
				}
				throw new TtcnError(MessageFormat.format("The first argument of function int2bit(), which is {0}, does not fit in {1} bit{2}, needs at least {3}.", value, length, length > 1 ? "s" : "", length + i));
			}

			return new TitanBitString(bits_ptr, length);
		} else {
			BigInteger tempValue = value.getBigInteger();
			final int bits_ptr[] = new int[(length + 7) / 8];
			for (int i = 0; i < (length + 7) / 8; i++) {
				bits_ptr[i] = 0;
			}
			for (int i = length - 1; tempValue.compareTo(BigInteger.ZERO) == 1 && i >= 0; i--) {
				if ((tempValue.and(BigInteger.ONE)).compareTo(BigInteger.ZERO) == 1) {
					final int temp = bits_ptr[i / 8] | (1 << (i % 8));
					bits_ptr[i / 8] = temp;
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
			throw new TtcnError(MessageFormat.format("The first argument (value) of function int2hex() is a negative integer value: {0}.", value));
		}
		if (length < 0) {
			throw new TtcnError(MessageFormat.format("The second argument (length) of function int2hex() is a negative integer value: {0}.", length));
		}
		if (value.isNative()) {
			int tmp_value = value.getInt();
			final byte nibbles_ptr[] = new byte[length];
			for (int i = length - 1; i >= 0; i--) {
				nibbles_ptr[i] = (byte) (tmp_value & 0xF);
				tmp_value >>= 4;
			}

			if (tmp_value != 0) {
				int i = 0;
				while (tmp_value != 0) {
					tmp_value >>= 4;
					i++;
				}
				throw new TtcnError(MessageFormat.format("The first argument of function int2hex(), which is {0}, does not fit in {1} hexadecimal digit{2}, needs at least {3}.", value, length, length > 1 ? "s" : "", length + i));
			}
			return new TitanHexString(nibbles_ptr);
		} else {
			BigInteger tmp_value = value.getBigInteger();
			final byte nibbles_ptr[] = new byte[length];
			for (int i = length - 1; i >= 0; i--) {
				final BigInteger temp = tmp_value.and(BigInteger.valueOf(0xF));
				nibbles_ptr[i] = temp.byteValue();
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

		final char octets_ptr[] = new char[length];
		int tmp_value = value;
		for (int i = length - 1; i >= 0; i--) {
			octets_ptr[i] = (char) (tmp_value & 0xFF);
			tmp_value >>= 8;
		}
		if (tmp_value != 0) {
			throw new TtcnError(MessageFormat.format("The first argument of function int2oct(), which is {0}, does not fit in {1} octet{2}.", value, length, length > 1 ? "s" : ""));
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

			final char octets_ptr[] = new char[length];
			final BigInteger helper = new BigInteger("255");
			for (int i = length - 1; i >= 0; i--) {
				octets_ptr[i] = (char) (tmp_val.and(helper).intValue());
				tmp_val = tmp_val.shiftRight(8);
			}
			if (tmp_val.compareTo(BigInteger.ZERO) != 0) {
				int i = 0;
				while (tmp_val.compareTo(BigInteger.ZERO) == 1) {
					tmp_val = tmp_val.shiftRight(8);
					i++;
				}
				throw new TtcnError(MessageFormat.format("The first argument of function int2oct(), which is {0}, does not fit in {1} octet{2}, needs at least {3}.", value, length, length > 1 ? "s" : "", length + i));
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
		return new TitanCharString(Integer.toString(value));
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

	public static TitanInteger float2int(final Ttcn3Float value) {
		return float2int(value.getValue());
	}

	public static TitanInteger float2int(final TitanFloat value) {
		value.mustBound("The argument of function float2int() is an unbound float value.");

		return float2int(value.getValue());
	}

	// C.9 - char2int
	public static TitanInteger char2int(final char value) {
		if (value > 127) {
			throw new TtcnError(MessageFormat.format("The argument of function char2int() contains a character with character code {0}, which is outside the allowed range 0 .. 127.", value));
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
			return new TitanOctetString("");
		}

		final char octets_ptr[] = new char[value.length()];
		for (int i = 0; i < value.length(); i++) {
			octets_ptr[i] = int2oct((int) value.charAt(i), 1).get_nibble(0);
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
			throw new TtcnError(MessageFormat.format("The length of the argument in function unichar2int() must be exactly 1 instead of {0}.", value.lengthOf().getInt()));
		}

		return unichar2int(value.getValue().get(0));
	}

	public static TitanInteger unichar2int(final TitanUniversalCharString_Element value) {
		value.mustBound("The argument of function unichar2int() is an unbound universal charstring element.");

		return unichar2int(value.get_char());
	}

	public static TitanOctetString unichar2oct(final TitanUniversalCharString value) {
		// no encoding parameter is default UTF-8
		value.mustBound("The argument of function unichar2oct() is an unbound universal charstring value.");

		final TTCN_EncDec.error_behavior_type err_behavior = TTCN_EncDec.get_error_behavior(TTCN_EncDec.error_type.ET_DEC_UCSTR);
		TTCN_EncDec.set_error_behavior(TTCN_EncDec.error_type.ET_DEC_UCSTR, TTCN_EncDec.error_behavior_type.EB_ERROR);

		final TTCN_Buffer buf = new TTCN_Buffer();
		value.encode_utf8(buf, false);

		TTCN_EncDec.set_error_behavior(TTCN_EncDec.error_type.ET_DEC_UCSTR, err_behavior);

		return new TitanOctetString(buf.get_data());
	}

	public static TitanOctetString unichar2oct(final TitanUniversalCharString value, final TitanCharString stringEncoding) {
		value.mustBound("The argument of function unichar2oct() is an unbound universal charstring value.");

		final TTCN_EncDec.error_behavior_type err_behavior = TTCN_EncDec.get_error_behavior(TTCN_EncDec.error_type.ET_DEC_UCSTR);
		TTCN_EncDec.set_error_behavior(TTCN_EncDec.error_type.ET_DEC_UCSTR, TTCN_EncDec.error_behavior_type.EB_ERROR);

		final TTCN_Buffer buf = new TTCN_Buffer();

		if (stringEncoding.operatorEquals("UTF-8")) {
			value.encode_utf8(buf, false);
		} else if (stringEncoding.operatorEquals("UTF-8 BOM")) {
			value.encode_utf8(buf, true);
		} else if (stringEncoding.operatorEquals("UTF-16")) {
			value.encode_utf16(buf, CharCoding.UTF16);
		} else if (stringEncoding.operatorEquals("UTF-16BE")) {
			value.encode_utf16(buf, CharCoding.UTF16BE);
		} else if (stringEncoding.operatorEquals("UTF-16LE")) {
			value.encode_utf16(buf, CharCoding.UTF16LE);
		} else if (stringEncoding.operatorEquals("UTF-32")) {
			value.encode_utf32(buf, CharCoding.UTF32);
		} else if (stringEncoding.operatorEquals("UTF-32BE")) {
			value.encode_utf32(buf, CharCoding.UTF32BE);
		} else if (stringEncoding.operatorEquals("UTF-32LE")) {
			value.encode_utf32(buf, CharCoding.UTF32LE);
		}
		else {
			throw new TtcnError("unichar2oct: Invalid parameter: "+ stringEncoding);
		}

		TTCN_EncDec.set_error_behavior(TTCN_EncDec.error_type.ET_DEC_UCSTR, err_behavior);

		return new TitanOctetString(buf.get_data());
	}

	public static TitanOctetString unichar2oct(final TitanUniversalCharString value, final String stringEncoding) {
		return unichar2oct(value, new TitanCharString(stringEncoding));
	}

	public static TitanCharString get_stringencoding(final TitanOctetString encoded_value) {
		if (encoded_value.lengthOf().operatorEquals(0)) {
			return new TitanCharString("<unknown>");
		}

		//TODO maybe we could use a switch to improve performance
		int i, j = 0;
		int length = encoded_value.lengthOf().getInt();
		char[] strptr = encoded_value.getValue();
		for (i = 0, j = 0; UTF8_BOM[i++] == (strptr[j++] & 0xFF) && i < UTF8_BOM.length;);
		if (i == UTF8_BOM.length && UTF8_BOM.length <= length) {
			return new TitanCharString("UTF-8");
		}

		// UTF-32 shall be tested before UTF-16 !!!
		for (i = 0, j = 0; UTF32BE_BOM[i++] == (strptr[j++] & 0xFF) && i < UTF32BE_BOM.length;);
		if (i == UTF32BE_BOM.length && UTF32BE_BOM.length <= length) {
			return new TitanCharString("UTF-32BE");
		}

		for (i = 0, j = 0; UTF32LE_BOM[i++] == (strptr[j++] & 0xFF) && i < UTF32LE_BOM.length;);
		if (i == UTF32LE_BOM.length && UTF32LE_BOM.length <= length) {
			return new TitanCharString("UTF-32LE");
		}

		// UTF-32 shall be tested before UTF-16 !!!
		for (i = 0, j = 0; UTF16BE_BOM[i++] == (strptr[j++] & 0xFF) && i < UTF16BE_BOM.length;);
		if (i == UTF16BE_BOM.length && UTF16BE_BOM.length <= length) {
			return new TitanCharString("UTF-16BE");
		}

		for (i = 0, j = 0; UTF16LE_BOM[i++] == (strptr[j++] & 0xFF) && i < UTF16LE_BOM.length;);
		if (i == UTF16LE_BOM.length && UTF16LE_BOM.length <= length) {
			return new TitanCharString("UTF-16LE");
		}

		if (is_ascii(encoded_value) == CharCoding.ASCII) {
			return new TitanCharString("ASCII");
		} else if (is_utf8(encoded_value) == CharCoding.UTF_8) {
			return new TitanCharString("UTF-8");
		} else {
			return new TitanCharString("<unknown>");
		}
	}

	// C.12 - bit2int
	public static TitanInteger bit2int(final TitanBitString value) {
		value.mustBound("The argument of function bit2int() is an unbound bitstring value.");

		final int n_bits = value.lengthOf().getInt();
		final int temp[] = value.getValue();

		// skip the leading zero bits
		int start_index = 0;
		for (; start_index < n_bits; start_index++) {
			if ((temp[start_index / 8] & (1 << (start_index % 8))) != 0) {
				break;
			}
		}

		// do the conversion
		if (n_bits - start_index < 32) {
			//fits into native
			int ret_val = 0;
			for (int i = start_index; i < n_bits; i++) {
				ret_val <<= 1;
				if ((temp[i / 8] & (1 << (i % 8))) != 0) {
					ret_val += 1;
				}
			}

			return new TitanInteger(ret_val);
		} else {
			int ret_val = 0;
			for (int i = start_index; i < start_index + 31; i++) {
				ret_val <<= 1;
				if ((temp[i / 8] & (1 << (i % 8))) != 0) {
					ret_val += 1;
				}
			}

			BigInteger ret_val2 = BigInteger.valueOf(ret_val);
			for (int i = start_index + 31; i < n_bits; i++) {
				ret_val2 = ret_val2.shiftLeft(1);
				if ((temp[i / 8] & (1 << (i % 8))) != 0) {
					ret_val2 = ret_val2.add(BigInteger.ONE);
				}
			}

			return new TitanInteger(ret_val2);
		}
	}

	public static TitanInteger bit2int(final TitanBitString_Element value) {
		value.mustBound("The argument of function bit2int() is an unbound bitstring element.");

		return new TitanInteger(value.get_bit() ? 1 : 0);
	}

	// C.13 - bit2hex
	public static TitanHexString bit2hex(final TitanBitString value) {
		value.mustBound("The argument of function bit2hex() is an unbound bitstring value.");

		final int n_bits = value.lengthOf().getInt();
		final int n_nibbles = (n_bits + 3) / 4;
		final int padding_bits = 4 * n_nibbles - n_bits;
		final int bits_ptr[] = value.getValue();
		final byte nibbles_ptr[] = new byte[n_nibbles];
		for (int i = 0; i < n_bits; i++) {
			if ((bits_ptr[i / 8] & (1 << (i % 8))) != 0) {
				nibbles_ptr[(i + padding_bits) / 4] |= 1 << (3 - ((i + padding_bits) % 4));
			}
		}

		return new TitanHexString(nibbles_ptr);
	}

	public static TitanHexString bit2hex(final TitanBitString_Element value) {
		value.mustBound("The argument of function bit2hex() is an unbound bitstring element.");

		return new TitanHexString((byte) (value.get_bit() ? 0x01 : 0x00));
	}

	// C.14 - bit2oct
	public static TitanOctetString bit2oct(final TitanBitString value) {
		value.mustBound("The argument of function bit2oct() is an unbound bitstring value.");

		final int n_bits = value.lengthOf().getInt();
		final int n_octets = (n_bits + 7) / 8;
		final int padding_bits = 8 * n_octets - n_bits;
		final int octets_ptr[] = new int[n_octets];
		final int bits_ptr[] = value.getValue();

		// bitstring conversion to hex characters
		for (int i = 0; i < n_bits; i++) {
			if ((bits_ptr[i / 8] & (1 << (i % 8))) != 0) {
				octets_ptr[(i + padding_bits) / 8] |= 1 << (7 - ((i + padding_bits) % 8));
			}
		}

		// to please the constructor
		final char ret_val[] = new char[octets_ptr.length];
		for (int i = 0; i < octets_ptr.length; i++) {
			ret_val[i] = (char) octets_ptr[i];
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
		if (n_nibbles - start_index < 8) {
			//fits into native
			int ret_val = 0;
			for (int i = start_index; i < n_nibbles; i++) {
				ret_val <<= 4;
				ret_val += value.get_nibble(i) & 0x0F;
			}

			return new TitanInteger(ret_val);
		} else {
			int ret_val = 0;
			for (int i = start_index; i < start_index + 7; i++) {
				ret_val <<= 4;
				ret_val += value.get_nibble(i) & 0x0F;
			}

			BigInteger ret_val2 = BigInteger.valueOf(ret_val);
			for (int i = start_index + 7; i < n_nibbles; i++) {
				ret_val2 = ret_val2.shiftLeft(4);
				ret_val2 = ret_val2.add(BigInteger.valueOf(value.get_nibble(i) & 0x0F));
			}

			return new TitanInteger(ret_val2);
		}
	}

	public static TitanInteger hex2int(final TitanHexString_Element value) {
		value.mustBound("The argument of function hex2int() is an unbound hexstring element.");

		return new TitanInteger(value.get_nibble());
	}

	// C.17 - hex2bit
	public static TitanBitString hex2bit(final TitanHexString value) {
		value.mustBound("The argument of function hex2bit() is an unbound hexstring value.");

		final int n_nibbles = value.lengthOf().getInt();
		final int bits_ptr[] = new int[(n_nibbles + 1) / 2];

		if (n_nibbles == 1) {
			final int temp = value.get_nibble(0);

			bits_ptr[0] = nibble_reverse_table[temp];

			return new TitanBitString(bits_ptr, 4);
		}

		final byte nibbles_ptr[] = value.getValue();
		for (int i = 0; i < n_nibbles - 1; i += 2) {
			int temp = nibble_reverse_table[nibbles_ptr[i + 1]];
			temp <<= 4;
			temp = (int) (temp | nibble_reverse_table[nibbles_ptr[i]]);

			bits_ptr[i / 2] = temp;
		}
		if ((n_nibbles & 1) == 1) {
			bits_ptr[n_nibbles / 2] = nibble_reverse_table[nibbles_ptr[n_nibbles - 1]];
		}

		return new TitanBitString(bits_ptr, 4 * n_nibbles);
	}

	public static TitanBitString hex2bit(final TitanHexString_Element value) {
		value.mustBound("The argument of function hex2bit() is an unbound hexstring element.");

		final int bits_ptr[] = new int[] {nibble_reverse_table[value.get_nibble()]};

		return new TitanBitString(bits_ptr, 4);
	}

	// C.18 - hex2oct
	public static TitanOctetString hex2oct(final TitanHexString value) {
		value.mustBound("The argument of function hex2oct() is an unbound hexstring value.");

		final int n_nibbles = value.lengthOf().getInt();
		final int n_octets = (n_nibbles + 1) / 2;
		final int n_padding_nibble = n_nibbles % 2;
		final char octet_ptr[] = new char[n_octets];
		final byte nibbles_ptr[] = new byte[n_nibbles + n_padding_nibble];

		if ((n_nibbles & 1) == 1) {
			nibbles_ptr[0] = (byte) 0;
		}
		System.arraycopy(value.getValue(), 0, nibbles_ptr, n_padding_nibble, value.getValue().length);
		for (int i = 1; i < nibbles_ptr.length; i += 2) {
			octet_ptr[i / 2] = (char) ((nibbles_ptr[i - 1] << 4) | nibbles_ptr[i]);
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

		final char octets_ptr[] = value.getValue();
		final int n_octets = octets_ptr.length;

		// skip the leading zero hex digits
		int start_index = 0;
		for (start_index = 0; start_index < n_octets; start_index++) {
			if (octets_ptr[start_index] != 0) {
				break;
			}
		}

		// do the conversion
		if (n_octets - start_index < 4) {
			//fits into native
			int ret_val = 0;
			for (int i = start_index; i < n_octets; i++) {
				ret_val <<= 8;
				ret_val += octets_ptr[i] & 0xFF;
			}

			return new TitanInteger(ret_val);
		} else {
			int ret_val = 0;
			for (int i = start_index; i < start_index + 3; i++) {
				ret_val <<= 8;
				ret_val += octets_ptr[i] & 0xFF;
			}

			BigInteger ret_val2 = BigInteger.valueOf(ret_val);
			for (int i = start_index + 3; i < n_octets; i++) {
				ret_val2 = ret_val2.shiftLeft(8);
				ret_val2 = ret_val2.add(BigInteger.valueOf(octets_ptr[i] & 0xFF));
			}

			return new TitanInteger(ret_val2);
		}
	}

	public static TitanInteger oct2int(final TitanOctetString_Element value) {
		value.mustBound("The argument of function oct2int() is an unbound octetstring element.");

		return new TitanInteger((int) value.get_nibble());
	}

	// C.21 - oct2bit
	public static TitanBitString oct2bit(final TitanOctetString value) {
		value.mustBound("The argument of function oct2bit() is an unbound octetstring value.");

		final int n_octets = value.lengthOf().getInt();
		final int bits_ptr[] = new int[n_octets];
		final char octets_ptr[] = value.getValue();

		for (int i = 0; i < n_octets; i++) {
			bits_ptr[i] = bit_reverse_table[octets_ptr[i] & 0xFF];
		}

		return new TitanBitString(bits_ptr, 8 * n_octets);
	}

	public static TitanBitString oct2bit(final TitanOctetString_Element value) {
		value.mustBound("The argument of function oct2bit() is an unbound octetstring value.");

		final int bits = bit_reverse_table[value.get_nibble()];
		return new TitanBitString((byte) bits);
	}

	// C.22 - oct2hex
	public static TitanHexString oct2hex(final TitanOctetString value) {
		value.mustBound("The argument of function oct2hex() is an unbound octetstring value.");

		final int n_octets = value.lengthOf().getInt();
		final byte ret_val[] = new byte[n_octets * 2];
		final char octets_ptr[] = value.getValue();

		for (int i = 0; i < n_octets; i++) {
			ret_val[i * 2] = (byte) ((octets_ptr[i] & 0xF0) >> 4);
			ret_val[i * 2 + 1] = (byte) (octets_ptr[i] & 0x0F);
		}

		return new TitanHexString(ret_val);
	}

	public static TitanHexString oct2hex(final TitanOctetString_Element value) {
		value.mustBound("The argument of function oct2hex() is an unbound octetstring element.");

		final byte ret_val[] = new byte[] {
			(byte) ((value.get_nibble() & 0xF0) >> 4),
			(byte) (value.get_nibble() & 0x0F)
		};

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

	public static TitanUniversalCharString oct2unichar(final TitanOctetString value) {
		// default encoding is UTF-8
		final TitanUniversalCharString unicharStr = new TitanUniversalCharString();
		final TTCN_EncDec.error_behavior_type err_behavior = TTCN_EncDec.get_error_behavior(TTCN_EncDec.error_type.ET_DEC_UCSTR);
		TTCN_EncDec.set_error_behavior(TTCN_EncDec.error_type.ET_DEC_UCSTR, TTCN_EncDec.error_behavior_type.EB_ERROR);

		unicharStr.decode_utf8(value.getValue(), CharCoding.UTF_8, true);

		TTCN_EncDec.set_error_behavior(TTCN_EncDec.error_type.ET_DEC_UCSTR, err_behavior);

		return unicharStr;
	}

	public static TitanUniversalCharString oct2unichar(final TitanOctetString value, final TitanCharString encodeStr) {
		// default encoding is UTF-8
		final TitanUniversalCharString unicharStr = new TitanUniversalCharString();

		final TTCN_EncDec.error_behavior_type err_behavior = TTCN_EncDec.get_error_behavior(TTCN_EncDec.error_type.ET_DEC_UCSTR);
		TTCN_EncDec.set_error_behavior(TTCN_EncDec.error_type.ET_DEC_UCSTR, TTCN_EncDec.error_behavior_type.EB_ERROR);

		if (encodeStr.operatorEquals("UTF-8")) {
			unicharStr.decode_utf8(value.getValue(), CharCoding.UTF_8, true);
		} else if (encodeStr.operatorEquals("UTF-16")) {
			unicharStr.decode_utf16(value.lengthOf().getInt(), value.getValue(), CharCoding.UTF16);
		} else if (encodeStr.operatorEquals("UTF-16BE")) {
			unicharStr.decode_utf16(value.lengthOf().getInt(), value.getValue(), CharCoding.UTF16BE);
		} else if (encodeStr.operatorEquals("UTF-16LE")) {
			unicharStr.decode_utf16(value.lengthOf().getInt(), value.getValue(), CharCoding.UTF16LE);
		} else if (encodeStr.operatorEquals("UTF-32")) {
			unicharStr.decode_utf32(value.lengthOf().getInt(), value.getValue(), CharCoding.UTF32);
		} else if (encodeStr.operatorEquals("UTF-32BE")) {
			unicharStr.decode_utf32(value.lengthOf().getInt(), value.getValue(), CharCoding.UTF32BE);
		} else if (encodeStr.operatorEquals("UTF-32LE")) {
			unicharStr.decode_utf32(value.lengthOf().getInt(), value.getValue(), CharCoding.UTF32LE);
		}
		else {
			throw new TtcnError("oct2unichar: Invalid parameter: " +encodeStr);
		}

		TTCN_EncDec.set_error_behavior(TTCN_EncDec.error_type.ET_DEC_UCSTR, err_behavior);

		return unicharStr;
	}

	public static TitanUniversalCharString oct2unichar(final TitanOctetString value, final String encodeStr) {
		return oct2unichar(value, new TitanCharString(encodeStr));
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
				if (c >= '0' && c <= '9') {
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
				TtcnError.TtcnErrorBegin("The argument of function str2int(), which is ");
				value.log();
				TtcnLogger.log_event_str(", does not represent a valid integer value. Invalid character `");
				TtcnLogger.logCharEscaped(c);
				TtcnLogger.log_event("' was found at index %d.", i);
				TtcnError.TtcnErrorEnd();
			}
		}
		if (state != str2intState.S_ZERO && state != str2intState.S_MORE && state != str2intState.S_END) {
			TtcnError.TtcnErrorBegin("The argument of function str2int(), which is ");
			value.log();
			TtcnLogger.log_event_str(", does not represent a valid integer value. Premature end of the string.");
			TtcnError.TtcnErrorEnd();
		}
		if (leading_ws) {
			TtcnError.TtcnWarningBegin("Leading whitespace was detected in the argument of function str2int(): ");
			value.log();
			TtcnLogger.log_event_str(".");
			TtcnError.TtcnWarningEnd();
		}
		if (leading_zero) {
			TtcnError.TtcnWarningBegin("Leading zero digit was detected in the argument of function str2int(): ");
			value.log();
			TtcnLogger.log_event_str(".");
			TtcnError.TtcnWarningEnd();
		}
		if (state == str2intState.S_END) {
			TtcnError.TtcnWarningBegin("Trailing whitespace was detected in the argument of function str2int(): ");
			value.log();
			TtcnLogger.log_event_str(".");
			TtcnError.TtcnWarningEnd();
		}

		return new TitanInteger(value_str.toString());
	}

	public static TitanInteger str2int(final TitanCharString_Element value) {
		value.mustBound("The argument of function str2int() is an unbound charstring element.");

		final char c = value.get_char();
		if (c < '0' || c > '9') {
			TtcnError.TtcnErrorBegin("The argument of function str2int(), which is a charstring element containing character `");
			TtcnLogger.logCharEscaped(c);
			TtcnLogger.log_event_str("', does not represent a valid integer value.");
			TtcnError.TtcnErrorEnd();
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

		final char octets_ptr[] = new char[value_len / 2];
		final StringBuilder chars_ptr = new StringBuilder();
		chars_ptr.append(value.getValue().toString());
		for (int i = 0; i < value_len; i++) {
			final char c = chars_ptr.charAt(i);
			final byte hexdigit = charToHexDigit(c);
			if (hexdigit > 0x0F) {
				TtcnError.TtcnErrorBegin("The argument of function str2oct() shall contain hexadecimal digits only, but character `");
				TtcnLogger.logCharEscaped(c);
				TtcnLogger.log_event_str(MessageFormat.format("' was found at index {0}.", i));
				TtcnError.TtcnErrorEnd();
			}
			if (i % 2 != 0) {
				octets_ptr[i / 2] = (char) (octets_ptr[i / 2] | hexdigit);
			} else {
				octets_ptr[i / 2] = (char) (hexdigit << 4);
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
		if (value_len == 0) {
			throw new TtcnError("The argument of function str2float() is an empty string, which does not represent a valid float value.");
		}
		if (value.operatorEquals("infinity")) {
			return new TitanFloat(Double.POSITIVE_INFINITY);
		}
		if (value.operatorEquals("-infinity")) {
			return new TitanFloat(Double.NEGATIVE_INFINITY);
		}
		if (value.operatorEquals("not_a_number")) {
			return new TitanFloat(Double.NaN);
		}
		final StringBuilder value_str = value.getValue();
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
				if (c == '+' || c == '-') {
					state = str2floatState.S_FIRST_M;
				} else if (c == '0') {
					state = str2floatState.S_ZERO_M;
				} else if (c >= '1' && c <= '9') {
					state = str2floatState.S_MORE_M;
				} else if (Character.isWhitespace(c)) {
					leading_ws = true;
				} else {
					state = str2floatState.S_ERR;
				}
				break;
			case S_FIRST_M: // first mantissa digit
				if (c == '0') {
					state = str2floatState.S_ZERO_M;
				} else if (c >= '1' && c <= '9') {
					state = str2floatState.S_MORE_M;
				} else {
					state = str2floatState.S_ERR;
				}
				break;
			case S_ZERO_M: // leading mantissa zero
				if (c == '.') {
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
				if (c == '.') {
					state = str2floatState.S_FIRST_F;
				} else if (c == 'E' || c == 'e') {
					state = str2floatState.S_INITIAL_E;
				} else if (c >= '0' && c <= '9') {
				} else {
					state = str2floatState.S_ERR;
				}
				break;
			case S_FIRST_F:
				if (c >= '0' && c <= '9') {
					state = str2floatState.S_MORE_F;
				} else {
					state = str2floatState.S_ERR;
				}
				break;
			case S_MORE_F:
				if (c == 'E' || c == 'e') {
					state = str2floatState.S_INITIAL_E;
				} else if (c >= '0' && c <= '9') {
				} else if (Character.isWhitespace(c)) {
					state = str2floatState.S_END;
				} else {
					state = str2floatState.S_ERR;
				}
				break;
			case S_INITIAL_E:
				if (c == '+' || c == '-') {
					state = str2floatState.S_FIRST_E;
				} else if (c == '0') {
					state = str2floatState.S_ZERO_E;
				} else if (c >= '1' && c <= '9') {
					state = str2floatState.S_MORE_E;
				} else {
					state = str2floatState.S_ERR;
				}
				break;
			case S_FIRST_E:
				if (c == '0') {
					state = str2floatState.S_ZERO_E;
				} else if (c >= '1' && c <= '9') {
					state = str2floatState.S_MORE_E;
				} else {
					state = str2floatState.S_ERR;
				}
				break;
			case S_ZERO_E:
				if (c >= '0' && c <= '9') {
					leading_zero = true;
					state = str2floatState.S_MORE_E;
				} else if (Character.isWhitespace(c)) {
					state = str2floatState.S_END;
				} else {
					state = str2floatState.S_ERR;
				}
				break;
			case S_MORE_E:
				if (c >= '0' && c <= '9') {
				} else if (Character.isWhitespace(c)) {
					state = str2floatState.S_END;
				} else {
					state = str2floatState.S_ERR;
				}
				break;
			case S_END:
				if (Character.isWhitespace(c)) {
					state = str2floatState.S_ERR;
				}
				break;
			default:
				break;
			}
			if (state == str2floatState.S_ERR) {
				TtcnError.TtcnErrorBegin("The argument of function str2float() which is ");
				value.log();
				TtcnLogger.log_event_str("' , does not represent a valid float value. Invalid character ");
				TtcnLogger.logCharEscaped(c);
				TtcnLogger.log_event_str(MessageFormat.format("' was found at index {0}.", i));
				TtcnError.TtcnErrorEnd();
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
			TtcnError.TtcnErrorBegin("The argument of function str2float() which is ");
			value.log();
			TtcnLogger.log_event_str("' , does not represent a valid float value. Premature end of the string.");
			TtcnError.TtcnErrorEnd();
		}
		if (leading_ws) {
			TtcnError.TtcnWarningBegin("Leading whitespace was detected in the argument of function str2float(): ");
			value.log();
			TtcnLogger.log_char('.');
			TtcnError.TtcnWarningEnd();
		}
		if (leading_zero) {
			TtcnError.TtcnWarningBegin("Leading zero digit was detected in the argument of function str2float(): ");
			value.log();
			TtcnLogger.log_char('.');
			TtcnError.TtcnWarningEnd();
		}
		if (state == str2floatState.S_END) {
			TtcnError.TtcnWarningBegin("Trailing whitespace was detected in the argument of function str2float(): ");
			value.log();
			TtcnLogger.log_char('.');
			TtcnError.TtcnWarningEnd();
		}

		return new TitanFloat(Double.valueOf(value_str.toString()));
	}

	// C.34 - substr
	public static void check_substr_arguments(final int value_length, final int idx, final int returncount, final String string_type, final String element_name) {
		if (idx < 0) {
			throw new TtcnError(MessageFormat.format("The second argument (index) of function substr() is a negative integer value: {0}.", idx));
		}
		if (idx > value_length) {
			throw new TtcnError(MessageFormat.format("The second argument (index) of function substr(), which is {0} , is greater than the length of the {1} value: {2}.", idx, string_type, value_length));
		}
		if (returncount < 0) {
			throw new TtcnError(MessageFormat.format("The third argument (returncount) of function substr() is a negative integer value: {0}.", returncount));
		}
		if (idx + returncount > value_length) {
			throw new TtcnError(MessageFormat.format("The first argument of function substr(), the length of which is {0}, does not have enough {1}s starting at index {2}: {3} {4}{5} needed, but there {6} only {7}.",
					value_length, element_name, idx, returncount, element_name, returncount > 1 ? "s are" : " is", value_length - idx > 1 ? "are" : "is", value_length - idx));
		}
	}

	public static void check_substr_arguments(final int idx, final int returncount, final String string_type, final String element_name) {
		if (idx < 0) {
			throw new TtcnError(MessageFormat.format("The second argument (index) of function substr() is a negative integer value: {0}.", idx));
		}
		if (idx > 1) {
			throw new TtcnError(MessageFormat.format("The second argument (index) of function substr(), which is {0}, is greater than 1 (i.e. the length of the {1} element).", idx, string_type));
		}
		if (returncount < 0) {
			throw new TtcnError(MessageFormat.format("The third argument (returncount) of function substr() is a negative integer value: {0}.", returncount));
		}
		if (idx + returncount > 1) {
			throw new TtcnError(MessageFormat.format("The first argument of function substr(), which is a{0} {1} element, does not have enough {2}s starting at index {3}: {4} {5}{6} needed, but there is only {7}.",
					string_type.charAt(0) == 'o' ? "n" : "", string_type, element_name, idx, returncount, element_name, returncount > 1 ? "s are" : " is", 1 - idx));
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
			final int bits_ptr[] = value.getValue();
			final int ret_val[] = new int[(returncount + 7) / 8];
			System.arraycopy(bits_ptr, idx / 8, ret_val, 0, (returncount + 7) / 8);
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
		final byte src_ptr[] = value.getValue();
		final byte ret_val[] = new byte[returncount];
		System.arraycopy(src_ptr, idx, ret_val, 0, returncount);

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
		final char ret_val[] = new char[returncount];
		final char src_ptr[] = value.getValue();
		System.arraycopy(src_ptr, idx, ret_val, 0, returncount);

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
		if (!value.isValue()) {
			throw new TtcnError("The first argument of function substr() is a template with non-specific value.");
		}

		return subString(value.valueOf(), idx, returncount);
	}

	public static TitanBitString subString(final TitanBitString_template value, final int idx, final TitanInteger returncount) {
		if (!value.isValue()) {
			throw new TtcnError("The first argument of function substr() is a template with non-specific value.");
		}

		return subString(value.valueOf(), idx, returncount);
	}

	public static TitanBitString subString(final TitanBitString_template value, final TitanInteger idx, final int returncount) {
		if (!value.isValue()) {
			throw new TtcnError("The first argument of function substr() is a template with non-specific value.");
		}

		return subString(value.valueOf(), idx, returncount);
	}

	public static TitanBitString subString(final TitanBitString_template value, final TitanInteger idx, final TitanInteger returncount) {
		if (!value.isValue()) {
			throw new TtcnError("The first argument of function substr() is a template with non-specific value.");
		}

		return subString(value.valueOf(), idx, returncount);
	}

	public static TitanHexString subString(final TitanHexString_template value, final int idx, final int returncount) {
		if (!value.isValue()) {
			throw new TtcnError("The first argument of function substr() is a template with non-specific value.");
		}

		return subString(value.valueOf(), idx, returncount);
	}

	public static TitanHexString subString(final TitanHexString_template value, final int idx, final TitanInteger returncount) {
		if (!value.isValue()) {
			throw new TtcnError("The first argument of function substr() is a template with non-specific value.");
		}

		return subString(value.valueOf(), idx, returncount);
	}

	public static TitanHexString subString(final TitanHexString_template value, final TitanInteger idx, final int returncount) {
		if (!value.isValue()) {
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
		if (!value.isValue()) {
			throw new TtcnError("The first argument of function substr() is a template with non-specific value.");
		}

		return subString(value.valueOf(), idx, returncount);
	}

	public static TitanOctetString subString(final TitanOctetString_template value, final int idx, final TitanInteger returncount) {
		if (!value.isValue()) {
			throw new TtcnError("The first argument of function substr() is a template with non-specific value.");
		}

		return subString(value.valueOf(), idx, returncount);
	}

	public static TitanOctetString subString(final TitanOctetString_template value, final TitanInteger idx, final int returncount) {
		if (!value.isValue()) {
			throw new TtcnError("The first argument of function substr() is a template with non-specific value.");
		}

		return subString(value.valueOf(), idx, returncount);
	}

	public static TitanOctetString subString(final TitanOctetString_template value, final TitanInteger idx, final TitanInteger returncount) {
		if (!value.isValue()) {
			throw new TtcnError("The first argument of function substr() is a template with non-specific value.");
		}

		return subString(value.valueOf(), idx, returncount);
	}

	public static TitanCharString subString(final TitanCharString_template value, final int idx, final int returncount) {
		if (!value.isValue()) {
			throw new TtcnError("The first argument of function substr() is a template with non-specific value.");
		}

		return subString(value.valueOf(), idx, returncount);
	}

	public static TitanCharString subString(final TitanCharString_template value, final int idx, final TitanInteger returncount) {
		if (!value.isValue()) {
			throw new TtcnError("The first argument of function substr() is a template with non-specific value.");
		}

		return subString(value.valueOf(), idx, returncount);
	}

	public static TitanCharString subString(final TitanCharString_template value, final TitanInteger idx, final int returncount) {
		if (!value.isValue()) {
			throw new TtcnError("The first argument of function substr() is a template with non-specific value.");
		}

		return subString(value.valueOf(), idx, returncount);
	}

	public static TitanCharString subString(final TitanCharString_template value, final TitanInteger idx, final TitanInteger returncount) {
		if (!value.isValue()) {
			throw new TtcnError("The first argument of function substr() is a template with non-specific value.");
		}

		return subString(value.valueOf(), idx, returncount);
	}

	public static TitanUniversalCharString subString(final TitanUniversalCharString_template value, final int idx, final int returncount) {
		if (!value.isValue()) {
			throw new TtcnError("The first argument of function substr() is a template with non-specific value.");
		}

		return subString(value.valueOf(), idx, returncount);
	}

	public static TitanUniversalCharString subString(final TitanUniversalCharString_template value, final int idx, final TitanInteger returncount) {
		if (!value.isValue()) {
			throw new TtcnError("The first argument of function substr() is a template with non-specific value.");
		}

		return subString(value.valueOf(), idx, returncount);
	}

	public static TitanUniversalCharString subString(final TitanUniversalCharString_template value, final TitanInteger idx, final int returncount) {
		if (!value.isValue()) {
			throw new TtcnError("The first argument of function substr() is a template with non-specific value.");
		}

		return subString(value.valueOf(), idx, returncount);
	}

	public static TitanUniversalCharString subString(final TitanUniversalCharString_template value, final TitanInteger idx, final TitanInteger returncount) {
		if (!value.isValue()) {
			throw new TtcnError("The first argument of function substr() is a template with non-specific value.");
		}

		return subString(value.valueOf(), idx, returncount);
	}

	// C.35 - replace

	public static void check_replace_arguments(final int value_length, final int idx, final int len, final String string_type, final String element_name) {
		if (idx < 0) {
			throw new TtcnError(MessageFormat.format("The second argument (index) of function replace() is a negative integer value: {0}.", idx));
		}
		if (idx > value_length) {
			throw new TtcnError(MessageFormat.format("The second argument (index) of function replace(), which is {0}, is greater than the length of the {1} value: {2}.",
					idx, string_type, value_length));
		}
		if (len < 0) {
			throw new TtcnError(MessageFormat.format("The third argument (len) of function replace() is a negative integer value: {0}.", len));
		}
		if (len > value_length) {
			throw new TtcnError(MessageFormat.format("The third argument (len) of function replace(), which is {0}, is greater than the length of the {1} value: {2}.", len, string_type, value_length));
		}
		if (idx + len > value_length) {
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
		final StringBuilder temp_sb = new StringBuilder(value_len);

		for (int i = 0; i < idx; i++) {
			temp_sb.append(value.getBit(i) ? '1' : '0');
		}
		for (int i = 0; i < repl_len; i++) {
			temp_sb.append(repl.getBit(i) ? '1' : '0');
		}
		for (int i = 0; i < value_len - idx - len; i++) {
			temp_sb.append(value.getBit(idx + len + i) ? '1' : '0');
		}
		return new TitanBitString(temp_sb.toString());
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
		final byte src_ptr[] = value.getValue();
		final byte repl_ptr[] = repl.getValue();
		final byte ret_val[] = new byte[value_len + repl_len - len];

		System.arraycopy(src_ptr, 0, ret_val, 0, idx);
		System.arraycopy(repl_ptr, 0, ret_val, idx, repl_len);
		System.arraycopy(src_ptr, idx + len, ret_val, idx + repl_len, value_len - idx - len);

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
		final char src_ptr[] = value.getValue();
		final char repl_ptr[] = repl.getValue();
		final char ret_val[] = new char[value_len + repl_len - len];

		System.arraycopy(src_ptr, 0, ret_val, 0, idx);
		System.arraycopy(repl_ptr, 0, ret_val, idx, repl_len);
		System.arraycopy(src_ptr, idx + len, ret_val, idx + repl_len, value_len - idx - len);

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
		ret_val.append(value.getValue());

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
				TtcnError.TtcnErrorBegin("The argument of function str2bit() shall contain characters '0' and '1' only, but character `");
				TtcnLogger.logCharEscaped(c);
				TtcnLogger.log_event_str(MessageFormat.format("'' was found at index {0}.", i));
				TtcnError.TtcnErrorEnd();
			}
		}

		return new TitanBitString(ret_val.toString());
	}

	public static TitanBitString str2bit(final TitanCharString_Element value) {
		value.mustBound("The argument of function str2bit() is an unbound charstring element.");

		final char c = value.get_char();
		if (c != '0' && c != '1') {
			TtcnError.TtcnErrorBegin("The argument of function str2bit() shall contain characters '0' and '1' only, but the given charstring element contains the character `");
			TtcnLogger.logCharEscaped(c);
			TtcnLogger.log_event_str("'.");
			TtcnError.TtcnErrorEnd();
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
		final byte ret_val[] = new byte[value_length];

		for (int i = 0; i < value_length; i++) {
			final char c = chars_ptr.charAt(i);
			final byte hexdigit = charToHexDigit(c);
			if (hexdigit < 0x00) {
				TtcnError.TtcnErrorBegin("The argument of function str2hex() shall contain hexadecimal digits only, but character `");
				TtcnLogger.logCharEscaped(c);
				TtcnLogger.log_event_str(MessageFormat.format("'' was found at index {0}.", i));
				TtcnError.TtcnErrorEnd();
			}
			ret_val[i] = hexdigit;
		}

		return new TitanHexString(ret_val);
	}

	public static TitanHexString str2hex(final TitanCharString_Element value) {
		value.mustBound("The argument of function str2hex() is an unbound charstring element.");

		final char c = value.get_char();
		final byte hexdigit = charToHexDigit(c);

		if (hexdigit < 0x00) {
			TtcnError.TtcnErrorBegin("The argument of function str2hex() shall contain only hexadecimal digits, but the given charstring element contains the character `");
			TtcnLogger.logCharEscaped(c);
			TtcnLogger.log_event_str("'.");
			TtcnError.TtcnErrorEnd();
		}

		return new TitanHexString(hexdigit);
	}

	// float2str
	public static TitanCharString float2str(final Ttcn3Float value) {
		//differnce between java and c++
		if (Double.isNaN(value.getValue())) {
			return new TitanCharString("not_a_number");
		} else if (value.getValue() == Double.NEGATIVE_INFINITY) {
			return new TitanCharString("-infinity");
		} else if (value.getValue() == Double.POSITIVE_INFINITY) {
			return new TitanCharString("infinity");
		} else if (value.getValue() == 0.0
				|| (value.getValue() > -TitanFloat.MAX_DECIMAL_FLOAT && value.getValue() <= -TitanFloat.MIN_DECIMAL_FLOAT)
				|| (value.getValue() >= TitanFloat.MIN_DECIMAL_FLOAT && value.getValue() < TitanFloat.MAX_DECIMAL_FLOAT)) {
			return new TitanCharString(String.format("%f", value.getValue()));
		} else {
			return new TitanCharString(String.format("%e", value.getValue()));
		}
	}

	public static TitanCharString float2str(final TitanFloat value) {
		value.mustBound("The argument of function float2str() is an unbound float value.");

		//differnce between java and c++
		if (value.getValue().isNaN()) {
			return new TitanCharString("not_a_number");
		} else if (value.getValue() == Double.NEGATIVE_INFINITY) {
			return new TitanCharString("-infinity");
		} else if (value.getValue() == Double.POSITIVE_INFINITY) {
			return new TitanCharString("infinity");
		} else if (value.getValue() == 0.0
				|| (value.getValue() > -TitanFloat.MAX_DECIMAL_FLOAT && value.getValue() <= -TitanFloat.MIN_DECIMAL_FLOAT)
				|| (value.getValue() >= TitanFloat.MIN_DECIMAL_FLOAT && value.getValue() < TitanFloat.MAX_DECIMAL_FLOAT)) {
			return new TitanCharString(String.format("%f", value.getValue()));
		} else {
			return new TitanCharString(String.format("%e", value.getValue()));
		}
	}

	// unichar2char
	public static TitanCharString unichar2char(final TitanUniversalCharString value) {
		value.mustBound("The argument of function unichar2char() is an unbound universal charstring value.");

		final int value_length = value.lengthOf().getInt();
		if (value.charstring) {
			return new TitanCharString(value.cstr);
		} else {
			final StringBuilder ret_val = new StringBuilder();
			final List<TitanUniversalChar> uchars_ptr = value.val_ptr;
			for (int i = 0; i < value_length; i++) {
				final TitanUniversalChar uchar = uchars_ptr.get(i);
				if (uchar.getUc_group() != 0 || uchar.getUc_plane() != 0 || uchar.getUc_row() != 0 || uchar.getUc_cell() > 127) {
					throw new TtcnError(MessageFormat.format("The characters in the argument of function unichar2char() shall be within the range char(0, 0, 0, 0) .. "
							+ "char(0, 0, 0, 127), but quadruple char({0}, {1}, {2}, {3}) was found at index {4}.",
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
			throw new TtcnError(MessageFormat.format("The characters in the argument of function unichar2char() shall be within the range char(0, 0, 0, 0) .. char(0, 0, 0, 127), "
					+ "but the given universal charstring element contains the quadruple char({0}, {1}, {2}, {3}).", uchar.getUc_group(),uchar.getUc_plane(),uchar.getUc_row(),uchar.getUc_row()));
		}

		return new TitanCharString(String.valueOf(uchar.getUc_cell()));
	}

	//TODO: C.33 - regexp

	//C.36 - rnd

	//TODO update with Java 1.7 to ThreadLocalRandom
	static boolean rndSeedSet = false;
	private final static Random random = new Random();

	public static void setRndSeed(final double floatSeed) {
		TitanFloat.checkNumeric(floatSeed,"The seed value of function rnd()");
		// FIXME: method caste double from long
		random.setSeed((long)floatSeed);
		TtcnLogger.log_random(TitanLoggerApi.RandomAction.enum_type.seed, floatSeed, (long)floatSeed);
		rndSeedSet = true;
	}

	public static TitanFloat rndGenerate() {
		final double returnValue;
		returnValue = random.nextDouble();
		TtcnLogger.log_random(TitanLoggerApi.RandomAction.enum_type.read__out, returnValue, 0);

		return new TitanFloat(returnValue);
	}

	public static TitanFloat rnd() {
		if (!rndSeedSet) {
			setRndSeed(TTCN_Snapshot.timeNow());
		}

		return rndGenerate();
	}

	public static TitanFloat rnd(final double seed) {
		setRndSeed(seed);

		return rndGenerate();
	}

	public static TitanFloat rnd(final TitanFloat seed) {
		seed.mustBound("Initializing the random number generator with an unbound float value as seed.");

		setRndSeed(seed.getValue());
		return rndGenerate();
	}
}