/******************************************************************************
 * Copyright (c) 2000-2019 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;

import org.eclipse.titan.runtime.core.Base_Template.template_sel;
import org.eclipse.titan.runtime.core.TTCN_Logger.Severity;
import org.eclipse.titan.runtime.core.TitanCharString.CharCoding;

/**
 * Additional (predefined) functions
 *
 * References like C.1 mark the chapter in the TTCN-3 standard declaring the
 * predefined function.
 *
 * @author Kristof Szabados
 * @author Gergo Ujhelyi
 *
 * In the core in Addfunc.{hh,cc}
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

	private static final String code_table = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
	private static int[] decode_table = new int[] {
		    80, 80, 80, 80, 80, 80, 80, 80,   80, 80, 80, 80, 80, 80, 80, 80,
		    80, 80, 80, 80, 80, 80, 80, 80,   80, 80, 80, 80, 80, 80, 80, 80,
		    80, 80, 80, 80, 80, 80, 80, 80,   80, 80, 80, 62, 80, 80, 80, 63,
		    52, 53, 54, 55, 56, 57, 58, 59,   60, 61, 80, 80, 80, 70, 80, 80,
		    80,  0,  1,  2,  3,  4,  5,  6,    7,  8,  9, 10, 11, 12, 13, 14,
		    15, 16, 17, 18, 19, 20, 21, 22,   23, 24, 25, 80, 80, 80, 80, 80,
		    80, 26, 27, 28, 29, 30, 31, 32,   33, 34, 35, 36, 37, 38, 39, 40,
		    41, 42, 43, 44, 45, 46, 47, 48,   49, 50, 51, 80, 80, 80, 80, 80
		};

	private AdditionalFunctions() {
		//intentionally private to disable instantiation
	}

	private static byte char_to_hexdigit(final char c) {
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

	private static char hexdigit_to_char(final int hexdigit) {
		if (hexdigit < 16) {
			return TitanHexString.HEX_DIGITS.charAt(hexdigit);
		} else {
			return '\0';
		}
	}

	private static CharCoding is_ascii(final TitanOctetString ostr) {
		final int nonASCII = 1 << 7; // MSB is 1 in case of non ASCII character
		CharCoding ret = CharCoding.ASCII;
		final byte[] strptr = ostr.get_value();
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
		final byte strptr[] = ostr.get_value();
		while (ostr.lengthof().get_int() > i) {
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
					if (i >= ostr.lengthof().get_int() || ((strptr[i] & 0xFF) & MSB) == 0 || ((strptr[i] & 0xFF) & MSBmin1) != 0) { // if not like this: 10xx xxxx
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

	/**
	 * Converts an integer to a character.
	 * <p>
	 * For more details see chapter C.1.1
	 *
	 * @param value
	 *                the integer to convert.
	 * @return the converted charstring.
	 * */
	public static TitanCharString int2char(final int value) {
		if (value < 0 || value > 127) {
			throw new TtcnError(MessageFormat.format("The argument of function int2char() is {0}, which is outside the allowed range 0 .. 127.", value));
		}

		return new TitanCharString(String.valueOf(Character.toChars(value)[0]));
	}

	/**
	 * Converts an integer to a character.
	 * <p>
	 * For more details see chapter C.1.1
	 *
	 * @param value
	 *                the integer to convert.
	 * @return the converted charstring.
	 * */
	public static TitanCharString int2char(final TitanInteger value) {
		value.must_bound("The argument of function int2char() is an unbound integer value.");

		final int ivt = value.get_int();
		if (ivt < 0 || ivt > 127) {
			throw new TtcnError(MessageFormat.format("The argument of function int2char() is {0}, which is outside the allowed range 0 .. 127.", value));
		}

		return new TitanCharString(String.valueOf(Character.toChars(ivt)[0]));
	}

	// C.2 - int2unichar

	/**
	 * Converts an integer to a universal character.
	 * <p>
	 * For more details see chapter C.1.2
	 *
	 * @param value
	 *                the integer to convert.
	 * @return the converted universal charstring.
	 * */
	public static TitanUniversalCharString int2unichar(final int value) {
		if (value < 0 || value > Integer.MAX_VALUE) {
			throw new TtcnError(MessageFormat.format("The argument of function int2unichar() is {0}, which outside the allowed range 0 .. 2147483647.", value));
		}

		return new TitanUniversalCharString(Character.toChars(value >> 24)[0],Character.toChars((value >> 16) & 0xFF)[0],Character.toChars((value >> 8) & 0xFF)[0],Character.toChars(value & 0xFF)[0]);
	}

	/**
	 * Converts an integer to a universal character.
	 * <p>
	 * For more details see chapter C.1.2
	 *
	 * @param value
	 *                the integer to convert.
	 * @return the converted universal charstring.
	 * */
	public static TitanUniversalCharString int2unichar(final TitanInteger value) {
		value.must_bound("The argument of function int2unichar() is an unbound integer value.");

		final int ivt = value.get_int();
		if (ivt < 0 || ivt > Integer.MAX_VALUE) {
			throw new TtcnError(MessageFormat.format("The argument of function int2unichar() is {0}, which outside the allowed range 0 .. 2147483647.", value));
		}

		return int2unichar(ivt);
	}

	// C.3 - int2bit

	/**
	 * Converts an integer to a bitstring of given length. If the length
	 * parameter is more than needed, the resulting bitstring will be padded
	 * with zeroes from the left.
	 * <p>
	 * For more details see chapter C.1.3
	 *
	 * @param value
	 *                the integer to convert.
	 * @param length
	 *                the expected length of the resulting bitstring.
	 * @return the converted bitstrng.
	 * */
	public static TitanBitString int2bit(final int value, final int length) {
		return int2bit(new TitanInteger(value), length);
	}

	/**
	 * Converts an integer to a bitstring of given length. If the length
	 * parameter is more than needed, the resulting bitstring will be padded
	 * with zeroes from the left.
	 * <p>
	 * For more details see chapter C.1.3
	 *
	 * @param value
	 *                the integer to convert.
	 * @param length
	 *                the expected length of the resulting bitstring.
	 * @return the converted bitstrng.
	 * */
	public static TitanBitString int2bit(final int value, final TitanInteger length) {
		length.must_bound("The second argument (length) of function int2bit() is an unbound integer value.");

		return int2bit(value, length.get_int());
	}

	/**
	 * Converts an integer to a bitstring of given length. If the length
	 * parameter is more than needed, the resulting bitstring will be padded
	 * with zeroes from the left.
	 * <p>
	 * For more details see chapter C.1.3
	 *
	 * @param value
	 *                the integer to convert.
	 * @param length
	 *                the expected length of the resulting bitstring.
	 * @return the converted bitstrng.
	 * */
	public static TitanBitString int2bit(final TitanInteger value, final int length) {
		value.must_bound("The first argument (value) of function int2bit() is an unbound integer value.");

		if (value.is_less_than(0)) {
			throw new TtcnError(MessageFormat.format("The first argument (value) of function int2bit() is a negative integer value: {0}.", value));
		}
		if (length < 0) {
			throw new TtcnError(MessageFormat.format("The second argument (length) of function int2bit() is a negative integer value: {0}.", length));
		}

		if (value.is_native()) {
			int tempValue = value.get_int();
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
			BigInteger tempValue = value.get_BigInteger();
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

	/**
	 * Converts an integer to a bitstring of given length. If the length
	 * parameter is more than needed, the resulting bitstring will be padded
	 * with zeroes from the left.
	 * <p>
	 * For more details see chapter C.1.3
	 *
	 * @param value
	 *                the integer to convert.
	 * @param length
	 *                the expected length of the resulting bitstring.
	 * @return the converted bitstrng.
	 * */
	public static TitanBitString int2bit(final TitanInteger value, final TitanInteger length) {
		value.must_bound("The first argument (value) of function int2bit() is an unbound integer value.");
		length.must_bound("The second argument (length) of function int2bit() is an unbound integer value.");

		return int2bit(value, length.get_int());
	}

	// C.4 - int2hex

	/**
	 * Converts an integer to a hexstring of given length. If the length
	 * parameter is more than needed, the resulting hexstring will be padded
	 * with zeroes from the left.
	 * <p>
	 * For more details see chapter C.1.5
	 *
	 * @param value
	 *                the integer to convert.
	 * @param length
	 *                the expected length of the resulting hexstring.
	 * @return the converted hexstrng.
	 * */
	public static TitanHexString int2hex(final int value, final int length) {
		return int2hex(new TitanInteger(value), length);
	}

	/**
	 * Converts an integer to a hexstring of given length. If the length
	 * parameter is more than needed, the resulting hexstring will be padded
	 * with zeroes from the left.
	 * <p>
	 * For more details see chapter C.1.5
	 *
	 * @param value
	 *                the integer to convert.
	 * @param length
	 *                the expected length of the resulting hexstring.
	 * @return the converted hexstrng.
	 * */
	public static TitanHexString int2hex(final int value, final TitanInteger length) {
		length.must_bound("The second argument (length) of function int2hex() is an unbound integer value.");

		return int2hex(value, length.get_int());
	}

	/**
	 * Converts an integer to a hexstring of given length. If the length
	 * parameter is more than needed, the resulting hexstring will be padded
	 * with zeroes from the left.
	 * <p>
	 * For more details see chapter C.1.5
	 *
	 * @param value
	 *                the integer to convert.
	 * @param length
	 *                the expected length of the resulting hexstring.
	 * @return the converted hexstrng.
	 * */
	public static TitanHexString int2hex(final TitanInteger value, final int length) {
		value.must_bound("The first argument (value) of function int2hex() is an unbound integer value.");

		if (value.is_less_than(0)) {
			throw new TtcnError(MessageFormat.format("The first argument (value) of function int2hex() is a negative integer value: {0}.", value));
		}
		if (length < 0) {
			throw new TtcnError(MessageFormat.format("The second argument (length) of function int2hex() is a negative integer value: {0}.", length));
		}
		if (value.is_native()) {
			int tmp_value = value.get_int();
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
			BigInteger tmp_value = value.get_BigInteger();
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

	/**
	 * Converts an integer to a hexstring of given length. If the length
	 * parameter is more than needed, the resulting hexstring will be padded
	 * with zeroes from the left.
	 * <p>
	 * For more details see chapter C.1.5
	 *
	 * @param value
	 *                the integer to convert.
	 * @param length
	 *                the expected length of the resulting hexstring.
	 * @return the converted hexstrng.
	 * */
	public static TitanHexString int2hex(final TitanInteger value, final TitanInteger length) {
		value.must_bound("The first argument (value) of function int2hex() is an unbound integer value.");
		length.must_bound("The second argument (length) of function int2hex() is an unbound integer value.");

		return int2hex(value, length.get_int());
	}

	// C.5 - int2oct

	/**
	 * Converts an integer to a octetstring of given length. If the length
	 * parameter is more than needed, the resulting octetstring will be padded
	 * with zeroes from the left.
	 * <p>
	 * For more details see chapter C.1.6
	 *
	 * @param value
	 *                the integer to convert.
	 * @param length
	 *                the expected length of the resulting octetstring.
	 * @return the converted octetstrng.
	 * */
	public static TitanOctetString int2oct(final int value, final int length) {
		if (value < 0) {
			throw new TtcnError(MessageFormat.format("The first argument (value) of function int2oct() is a negative integer value:", value));
		}
		if (length < 0) {
			throw new TtcnError(MessageFormat.format("The second argument (length) of function int2oct() is a negative integer value:", length));
		}

		final byte octets_ptr[] = new byte[length];
		int tmp_value = value;
		for (int i = length - 1; i >= 0; i--) {
			octets_ptr[i] = (byte) (tmp_value & 0xFF);
			tmp_value >>= 8;
		}
		if (tmp_value != 0) {
			throw new TtcnError(MessageFormat.format("The first argument of function int2oct(), which is {0}, does not fit in {1} octet{2}.", value, length, length > 1 ? "s" : ""));
		}
		return new TitanOctetString(octets_ptr);
	}

	/**
	 * Converts an integer to a octetstring of given length. If the length
	 * parameter is more than needed, the resulting octetstring will be padded
	 * with zeroes from the left.
	 * <p>
	 * For more details see chapter C.1.6
	 *
	 * @param value
	 *                the integer to convert.
	 * @param length
	 *                the expected length of the resulting octetstring.
	 * @return the converted octetstrng.
	 * */
	public static TitanOctetString int2oct(final int value, final TitanInteger length) {
		length.must_bound("The second argument (length) of function int2oct() is an unbound integer value.");

		return int2oct(value, length.get_int());
	}

	/**
	 * Converts an integer to a octetstring of given length. If the length
	 * parameter is more than needed, the resulting octetstring will be padded
	 * with zeroes from the left.
	 * <p>
	 * For more details see chapter C.1.6
	 *
	 * @param value
	 *                the integer to convert.
	 * @param length
	 *                the expected length of the resulting octetstring.
	 * @return the converted octetstrng.
	 * */
	public static TitanOctetString int2oct(final TitanInteger value, final int length) {
		value.must_bound("The first argument (value) of function int2oct() is an unbound integer value.");

		if (value.is_native()) {
			return int2oct(value.get_int(), length);
		} else {
			if (value.is_less_than(0)) {
				throw new TtcnError(MessageFormat.format("The first argument (value) of function int2oct() is a negative integer value: {0}.", value));
			}
			if (length < 0) {
				throw new TtcnError(MessageFormat.format("The second argument (length) of function int2oct() is a negative integer value: {0}.", length));
			}

			BigInteger tmp_val = value.get_BigInteger();
			final byte octets_ptr[] = new byte[length];
			final BigInteger helper = new BigInteger("255");
			for (int i = length - 1; i >= 0; i--) {
				octets_ptr[i] = (byte) (tmp_val.and(helper).intValue());
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

	/**
	 * Converts an integer to a octetstring of given length. If the length
	 * parameter is more than needed, the resulting octetstring will be padded
	 * with zeroes from the left.
	 * <p>
	 * For more details see chapter C.1.6
	 *
	 * @param value
	 *                the integer to convert.
	 * @param length
	 *                the expected length of the resulting octetstring.
	 * @return the converted octetstrng.
	 * */
	public static TitanOctetString int2oct(final TitanInteger value, final TitanInteger length) {
		value.must_bound("The first argument (value) of function int2oct() is an unbound integer value.");
		length.must_bound("The second argument (length) of function int2oct() is an unbound integer value.");

		if (value.is_native()) {
			return int2oct(value.get_int(), length.get_int());
		}
		return int2oct(value, length.get_int());
	}

	// C.6 - int2str

	/**
	 * Converts an integer to its charstring equivalent.
	 * <p>
	 * For more details see chapter C.1.7
	 *
	 * @param value
	 *                the integer to convert.
	 * @return the converted charstring.
	 * */
	public static TitanCharString int2str(final int value) {
		return new TitanCharString(Integer.toString(value));
	}

	/**
	 * Converts an integer to its charstring equivalent.
	 * <p>
	 * For more details see chapter C.1.7
	 *
	 * @param value
	 *                the integer to convert.
	 * @return the converted charstring.
	 * */
	public static TitanCharString int2str(final TitanInteger value) {
		value.must_bound("The argument of function int2str() is an unbound integer value.");

		if (value.is_native()) {
			return int2str(value.get_int());
		}
		return new TitanCharString(value.get_BigInteger().toString());
	}

	// C.7 - int2float

	/**
	 * Converts an integer to a float value.
	 * <p>
	 * For more details see chapter C.1.9
	 *
	 * @param value
	 *                the integer to convert.
	 * @return the converted float.
	 * */
	public static TitanFloat int2float(final int value) {
		return new TitanFloat((double) value);
	}

	/**
	 * Converts an integer to a float value.
	 * <p>
	 * For more details see chapter C.1.9
	 *
	 * @param value
	 *                the integer to convert.
	 * @return the converted float.
	 * */
	public static TitanFloat int2float(final TitanInteger value) {
		value.must_bound("The argument of function int2float() is an unbound integer value.");

		if (value.is_native()) {
			return int2float(value.get_int());
		}

		return new TitanFloat(value.get_BigInteger().doubleValue());
	}

	// C.8 - float2int

	/**
	 * Converts a float value to an integer value,
	 *  by removing the fractional part.
	 *
	 * <p>
	 * For more details see chapter C.1.9
	 *
	 * @param value
	 *                the float to convert.
	 * @return the converted integer.
	 * */
	public static TitanInteger float2int(final double value) {
		if (value > Integer.MIN_VALUE && value < Integer.MAX_VALUE) {
			return new TitanInteger((int) value);
		}
		return new TitanInteger(new BigDecimal(value).toBigInteger());
	}

	/**
	 * Converts a float value to an integer value,
	 *  by removing the fractional part.
	 *
	 * <p>
	 * For more details see chapter C.1.9
	 *
	 * @param value
	 *                the float to convert.
	 * @return the converted integer.
	 * */
	public static TitanInteger float2int(final Ttcn3Float value) {
		return float2int(value.getValue());
	}

	/**
	 * Converts a float value to an integer value,
	 *  by removing the fractional part.
	 *
	 * <p>
	 * For more details see chapter C.1.9
	 *
	 * @param value
	 *                the float to convert.
	 * @return the converted integer.
	 * */
	public static TitanInteger float2int(final TitanFloat value) {
		value.must_bound("The argument of function float2int() is an unbound float value.");

		return float2int(value.get_value());
	}

	// C.9 - char2int
	/**
	 * Converts a single character into an integer
	 *  value  in the 0.. 127 range.
	 *
	 * <p>
	 * For more details see chapter C.1.10
	 *
	 * @param value
	 *                the character to convert.
	 * @return the converted integer.
	 * */
	public static TitanInteger char2int(final char value) {
		if (value > 127) {
			throw new TtcnError(MessageFormat.format("The argument of function char2int() contains a character with character code {0}, which is outside the allowed range 0 .. 127.", value));
		}
		return new TitanInteger((int) value);
	}

	/**
	 * Converts a single character long charstring into an integer
	 *  value  in the 0.. 127 range.
	 *
	 * <p>
	 * For more details see chapter C.1.10
	 *
	 * @param value
	 *                the charstring to convert.
	 * @return the converted integer.
	 * */
	public static TitanInteger char2int(final String value) {
		if (value == null) {
			throw new TtcnError("The length of the argument in function char2int() must be exactly 1 instead of 0.");
		}
		if (value.length() != 1) {
			throw new TtcnError(MessageFormat.format("The length of the argument in function char2int() must be exactly 1 instead of {0}.", value.length()));
		}

		return char2int(value.charAt(0));
	}

	/**
	 * Converts a single character long charstring into an integer
	 *  value  in the 0.. 127 range.
	 *
	 * <p>
	 * For more details see chapter C.1.10
	 *
	 * @param value
	 *                the charstring to convert.
	 * @return the converted integer.
	 * */
	public static TitanInteger char2int(final TitanCharString value) {
		value.must_bound("The argument of function char2int() is an unbound charstring value.");

		if (value.lengthof().get_int() != 1) {
			throw new TtcnError(MessageFormat.format("The length of the argument in function char2int() must be exactly 1 instead of {0}.", value.lengthof()));
		}
		return char2int(value.constGet_at(0).get_char());
	}

	/**
	 * Converts a single character long charstring into an integer
	 *  value  in the 0.. 127 range.
	 *
	 * <p>
	 * For more details see chapter C.1.10
	 *
	 * @param value
	 *                the charstring to convert.
	 * @return the converted integer.
	 * */
	public static TitanInteger char2int(final TitanCharString_Element value) {
		value.must_bound("The argument of function char2int() is an unbound charstring element.");

		return char2int(value.get_char());
	}

	// C.10 - char2oct
	/**
	 * Converts a charstring into an octetstring.
	 *
	 * <p>
	 * For more details see chapter C.1.11
	 *
	 * @param value
	 *                the charstring to convert.
	 * @return the converted octetstring.
	 * */
	public static TitanOctetString char2oct(final String value) {
		if (value == null || value.length() <= 0) {
			return new TitanOctetString("");
		}

		final byte octets_ptr[] = new byte[value.length()];
		for (int i = 0; i < value.length(); i++) {
			octets_ptr[i] = int2oct((int) value.charAt(i), 1).get_nibble(0);
		}

		return new TitanOctetString(octets_ptr);
	}

	/**
	 * Converts a charstring into an octetstring.
	 *
	 * <p>
	 * For more details see chapter C.1.11
	 *
	 * @param value
	 *                the charstring to convert.
	 * @return the converted octetstring.
	 * */
	public static TitanOctetString char2oct(final TitanCharString value){
		value.must_bound("The argument of function char2oct() is an unbound charstring value.");

		return char2oct(value.get_value().toString());
	}

	/**
	 * Converts a charstring into an octetstring.
	 *
	 * <p>
	 * For more details see chapter C.1.11
	 *
	 * @param value
	 *                the charstring to convert.
	 * @return the converted octetstring.
	 * */
	public static TitanOctetString char2oct(final TitanCharString_Element value){
		value.must_bound("The argument of function char2oct() is an unbound charstring element.");

		return char2oct(String.valueOf(value.get_char()));
	}

	// C.11 - unichar2int

	/**
	 * Converts an universal charstring into an integer.
	 *
	 * <p>
	 * For more details see chapter C.1.12
	 *
	 * @param value
	 *                the universal charstring to convert.
	 * @return the converted integer.
	 * */
	public static TitanInteger unichar2int(final TitanUniversalChar value) {
		if (value.getUc_group() > 127) {
			throw new TtcnError(MessageFormat.format("The argument of function unichar2int() is the invalid quadruple char {0},"
					+ "the first number of which is outside the allowed range 0 .. 127.", value.getUc_group()));
		}
		final int result = (value.getUc_group() << 24) | (value.getUc_plane() << 16) | (value.getUc_row() << 8) | value.getUc_cell();

		return new TitanInteger(result);
	}

	/**
	 * Converts an universal charstring into an integer.
	 *
	 * <p>
	 * For more details see chapter C.1.12
	 *
	 * @param value
	 *                the universal charstring to convert.
	 * @return the converted integer.
	 * */
	public static TitanInteger unichar2int(final TitanUniversalCharString value) {
		value.must_bound("The argument of function unichar2int() is an unbound universal charstring value.");

		if (value.lengthof().get_int() != 1) {
			throw new TtcnError(MessageFormat.format("The length of the argument in function unichar2int() must be exactly 1 instead of {0}.", value.lengthof().get_int()));
		}

		return unichar2int(value.get_value().get(0));
	}

	/**
	 * Converts an universal charstring into an integer.
	 *
	 * <p>
	 * For more details see chapter C.1.12
	 *
	 * @param value
	 *                the universal charstring to convert.
	 * @return the converted integer.
	 * */
	public static TitanInteger unichar2int(final TitanUniversalCharString_Element value) {
		value.must_bound("The argument of function unichar2int() is an unbound universal charstring element.");

		return unichar2int(value.get_char());
	}

	// C.12 - bit2int

	/**
	 * Converts a bitstring into an integer.
	 * The rightmost bit is least significant,
	 *  the leftmost bit is the most significant.
	 *
	 * <p>
	 * For more details see chapter C.1.13
	 *
	 * @param value
	 *                the bitstring to convert.
	 * @return the converted integer.
	 * */
	public static TitanInteger bit2int(final TitanBitString value) {
		value.must_bound("The argument of function bit2int() is an unbound bitstring value.");

		final int n_bits = value.lengthof().get_int();
		final int temp[] = value.get_value();

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

	/**
	 * Converts a bitstring into an integer.
	 * The rightmost bit is least significant,
	 *  the leftmost bit is the most significant.
	 *
	 * <p>
	 * For more details see chapter C.1.13
	 *
	 * @param value
	 *                the bitstring to convert.
	 * @return the converted integer.
	 * */
	public static TitanInteger bit2int(final TitanBitString_Element value) {
		value.must_bound("The argument of function bit2int() is an unbound bitstring element.");

		return new TitanInteger(value.get_bit() ? 1 : 0);
	}

	// C.13 - bit2hex

	/**
	 * Converts a bitstring into a hexstring.
	 *
	 * <p>
	 * For more details see chapter C.1.14
	 *
	 * @param value
	 *                the bitstring to convert.
	 * @return the converted hexstring.
	 * */
	public static TitanHexString bit2hex(final TitanBitString value) {
		value.must_bound("The argument of function bit2hex() is an unbound bitstring value.");

		final int n_bits = value.lengthof().get_int();
		final int n_nibbles = (n_bits + 3) / 4;
		final int padding_bits = 4 * n_nibbles - n_bits;
		final int bits_ptr[] = value.get_value();
		final byte nibbles_ptr[] = new byte[n_nibbles];
		for (int i = 0; i < n_bits; i++) {
			if ((bits_ptr[i / 8] & (1 << (i % 8))) != 0) {
				nibbles_ptr[(i + padding_bits) / 4] |= 1 << (3 - ((i + padding_bits) % 4));
			}
		}

		return new TitanHexString(nibbles_ptr);
	}

	/**
	 * Converts a bitstring into a hexstring.
	 *
	 * <p>
	 * For more details see chapter C.1.14
	 *
	 * @param value
	 *                the bitstring to convert.
	 * @return the converted hexstring.
	 * */
	public static TitanHexString bit2hex(final TitanBitString_Element value) {
		value.must_bound("The argument of function bit2hex() is an unbound bitstring element.");

		return new TitanHexString((byte) (value.get_bit() ? 0x01 : 0x00));
	}

	// C.14 - bit2oct

	/**
	 * Converts a bitstring into a octetstring.
	 *
	 * <p>
	 * For more details see chapter C.1.15
	 *
	 * @param value
	 *                the bitstring to convert.
	 * @return the converted octetstring.
	 * */
	public static TitanOctetString bit2oct(final TitanBitString value) {
		value.must_bound("The argument of function bit2oct() is an unbound bitstring value.");

		final int n_bits = value.lengthof().get_int();
		final int n_octets = (n_bits + 7) / 8;
		final int padding_bits = 8 * n_octets - n_bits;
		final byte octets_ptr[] = new byte[n_octets];
		final int bits_ptr[] = value.get_value();

		// bitstring conversion to hex characters
		for (int i = 0; i < n_bits; i++) {
			if ((bits_ptr[i / 8] & (1 << (i % 8))) != 0) {
				octets_ptr[(i + padding_bits) / 8] |= 1 << (7 - ((i + padding_bits) % 8));
			}
		}

		return new TitanOctetString(octets_ptr);
	}

	/**
	 * Converts a bitstring into a octetstring.
	 *
	 * <p>
	 * For more details see chapter C.1.15
	 *
	 * @param value
	 *                the bitstring to convert.
	 * @return the converted octetstring.
	 * */
	public static TitanOctetString bit2oct(final TitanBitString_Element value) {
		value.must_bound("The argument of function bit2oct() is an unbound bitstring element.");

		return new TitanOctetString((byte) (value.get_bit() ? 1 : 0));
	}

	// C.15 - bit2str

	/**
	 * Converts a bitstring into a charstring.
	 *
	 * <p>
	 * For more details see chapter C.1.16
	 *
	 * @param value
	 *                the bitstring to convert.
	 * @return the converted charstring.
	 * */
	public static TitanCharString bit2str(final TitanBitString value) {
		value.must_bound("The argument of function bit2str() is an unbound bitstring value.");

		final int n_bits = value.lengthof().get_int();
		final StringBuilder ret_val = new StringBuilder(n_bits);

		for (int i = 0; i < n_bits; i++) {
			if (value.get_bit(i)) {
				ret_val.append('1');
			} else {
				ret_val.append('0');
			}
		}

		return new TitanCharString(ret_val);
	}

	/**
	 * Converts a bitstring into a charstring.
	 *
	 * <p>
	 * For more details see chapter C.1.16
	 *
	 * @param value
	 *                the bitstring to convert.
	 * @return the converted charstring.
	 * */
	public static TitanCharString bit2str(final TitanBitString_Element value) {
		value.must_bound("The argument of function bit2str() is an unbound bitstring element.");

		if (value.get_bit()) {
			return new TitanCharString("1");
		} else {
			return new TitanCharString("0");
		}
	}

	// C.16 - hex2int

	/**
	 * Converts a hexstring into an integer.
	 * The rightmost hexadecimal digit is least significant,
	 *  the leftmost hexadecimal digit is the most significant
	 *
	 * <p>
	 * For more details see chapter C.1.17
	 *
	 * @param value
	 *                the hexstring to convert.
	 * @return the converted integer.
	 * */
	public static TitanInteger hex2int(final TitanHexString value) {
		value.must_bound("The argument of function hex2int() is an unbound hexstring value.");

		final int n_nibbles = value.lengthof().get_int();

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

	/**
	 * Converts a hexstring into an integer.
	 * The rightmost hexadecimal digit is least significant,
	 *  the leftmost hexadecimal digit is the most significant
	 *
	 * <p>
	 * For more details see chapter C.1.17
	 *
	 * @param value
	 *                the hexstring to convert.
	 * @return the converted integer.
	 * */
	public static TitanInteger hex2int(final TitanHexString_Element value) {
		value.must_bound("The argument of function hex2int() is an unbound hexstring element.");

		return new TitanInteger(value.get_nibble());
	}

	// C.17 - hex2bit

	/**
	 * Converts a hexstring into a bitstring.
	 *
	 * <p>
	 * For more details see chapter C.1.18
	 *
	 * @param value
	 *                the hexstring to convert.
	 * @return the converted bitstring.
	 * */
	public static TitanBitString hex2bit(final TitanHexString value) {
		value.must_bound("The argument of function hex2bit() is an unbound hexstring value.");

		final int n_nibbles = value.lengthof().get_int();
		final int bits_ptr[] = new int[(n_nibbles + 1) / 2];

		if (n_nibbles == 1) {
			final int temp = value.get_nibble(0);

			bits_ptr[0] = nibble_reverse_table[temp];

			return new TitanBitString(bits_ptr, 4);
		}

		final byte nibbles_ptr[] = value.get_value();
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

	/**
	 * Converts a hexstring into a bitstring.
	 *
	 * <p>
	 * For more details see chapter C.1.18
	 *
	 * @param value
	 *                the hexstring to convert.
	 * @return the converted bitstring.
	 * */
	public static TitanBitString hex2bit(final TitanHexString_Element value) {
		value.must_bound("The argument of function hex2bit() is an unbound hexstring element.");

		final int bits_ptr[] = new int[] {nibble_reverse_table[value.get_nibble()]};

		return new TitanBitString(bits_ptr, 4);
	}

	// C.18 - hex2oct

	/**
	 * Converts a hexstring into a octetstring.
	 *
	 * <p>
	 * For more details see chapter C.1.19
	 *
	 * @param value
	 *                the hexstring to convert.
	 * @return the converted octetstring.
	 * */
	public static TitanOctetString hex2oct(final TitanHexString value) {
		value.must_bound("The argument of function hex2oct() is an unbound hexstring value.");

		final int n_nibbles = value.lengthof().get_int();
		final int n_octets = (n_nibbles + 1) / 2;
		final int n_padding_nibble = n_nibbles % 2;
		final byte octet_ptr[] = new byte[n_octets];
		final byte nibbles_ptr[] = new byte[n_nibbles + n_padding_nibble];

		if ((n_nibbles & 1) == 1) {
			nibbles_ptr[0] = (byte) 0;
		}
		System.arraycopy(value.get_value(), 0, nibbles_ptr, n_padding_nibble, value.get_value().length);
		for (int i = 1; i < nibbles_ptr.length; i += 2) {
			octet_ptr[i / 2] = (byte) ((nibbles_ptr[i - 1] << 4) | nibbles_ptr[i]);
		}

		return new TitanOctetString(octet_ptr);
	}

	/**
	 * Converts a hexstring into a octetstring.
	 *
	 * <p>
	 * For more details see chapter C.1.19
	 *
	 * @param value
	 *                the hexstring to convert.
	 * @return the converted octetstring.
	 * */
	public static TitanOctetString hex2oct(final TitanHexString_Element value) {
		value.must_bound("The argument of function hex2oct() is an unbound hexstring element.");

		final byte octet = value.get_nibble();
		return new TitanOctetString(octet);
	}

	// C.19 - hex2str

	/**
	 * Converts a hexstring into a charstring.
	 * The resulting charstring has the same length as the hexstring
	 *  and contains only the characters '0' to '9'and 'A' to 'F'
	 *
	 * <p>
	 * For more details see chapter C.1.20
	 *
	 * @param value
	 *                the hexstring to convert.
	 * @return the converted charstring.
	 * */
	public static TitanCharString hex2str(final TitanHexString value) {
		value.must_bound("The argument of function hex2str() is an unbound hexstring value.");

		final int n_nibbles = value.lengthof().get_int();
		final StringBuilder ret_val = new StringBuilder();
		for (int i = 0; i < n_nibbles; i++) {
			final int hexdigit = value.constGet_at(i).get_nibble();
			ret_val.append(hexdigit_to_char(hexdigit));
		}

		return new TitanCharString(ret_val);
	}

	/**
	 * Converts a hexstring into a charstring.
	 * The resulting charstring has the same length as the hexstring
	 *  and contains only the characters '0' to '9'and 'A' to 'F'
	 *
	 * <p>
	 * For more details see chapter C.1.20
	 *
	 * @param value
	 *                the hexstring to convert.
	 * @return the converted charstring.
	 * */
	public static TitanCharString hex2str(final TitanHexString_Element value) {
		value.must_bound("The argument of function hex2str() is an unbound hexstring element.");

		return new TitanCharString(String.valueOf(value.get_nibble()));
	}

	// C.20 - oct2int

	/**
	 * Converts an octetstring (interpreted as a base 16 value) into an integer.
	 * The rightmost hexadecimal digit is least significant,
	 *  the leftmost hexadecimal digit is the most significant.
	 *
	 * <p>
	 * For more details see chapter C.1.21
	 *
	 * @param value
	 *                the octetstring to convert.
	 * @return the converted integer.
	 * */
	public static TitanInteger oct2int(final TitanOctetString value) {
		value.must_bound("The argument of function oct2int() is an unbound octetstring value.");

		final byte octets_ptr[] = value.get_value();
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

	/**
	 * Converts an octetstring (interpreted as a base 16 value) into an integer.
	 * The rightmost hexadecimal digit is least significant,
	 *  the leftmost hexadecimal digit is the most significant.
	 *
	 * <p>
	 * For more details see chapter C.1.21
	 *
	 * @param value
	 *                the octetstring to convert.
	 * @return the converted integer.
	 * */
	public static TitanInteger oct2int(final TitanOctetString_Element value) {
		value.must_bound("The argument of function oct2int() is an unbound octetstring element.");

		return new TitanInteger((int) value.get_nibble() & 0xFF);
	}

	// C.21 - oct2bit

	/**
	 * Converts an octetstring into a bitstring.
	 *
	 * <p>
	 * For more details see chapter C.1.22
	 *
	 * @param value
	 *                the octetstring to convert.
	 * @return the converted bitstring.
	 * */
	public static TitanBitString oct2bit(final TitanOctetString value) {
		value.must_bound("The argument of function oct2bit() is an unbound octetstring value.");

		final int n_octets = value.lengthof().get_int();
		final int bits_ptr[] = new int[n_octets];
		final byte octets_ptr[] = value.get_value();

		for (int i = 0; i < n_octets; i++) {
			bits_ptr[i] = bit_reverse_table[octets_ptr[i] & 0xFF];
		}

		return new TitanBitString(bits_ptr, 8 * n_octets);
	}

	/**
	 * Converts an octetstring into a bitstring.
	 *
	 * <p>
	 * For more details see chapter C.1.22
	 *
	 * @param value
	 *                the octetstring to convert.
	 * @return the converted bitstring.
	 * */
	public static TitanBitString oct2bit(final TitanOctetString_Element value) {
		value.must_bound("The argument of function oct2bit() is an unbound octetstring value.");

		final int bits = bit_reverse_table[value.get_nibble() & 0xFF];
		return new TitanBitString((byte) bits);
	}

	// C.22 - oct2hex

	/**
	 * Converts an octetstring into a hexstring.
	 *
	 * <p>
	 * For more details see chapter C.1.23
	 *
	 * @param value
	 *                the octetstring to convert.
	 * @return the converted hexstring.
	 * */
	public static TitanHexString oct2hex(final TitanOctetString value) {
		value.must_bound("The argument of function oct2hex() is an unbound octetstring value.");

		final int n_octets = value.lengthof().get_int();
		final byte ret_val[] = new byte[n_octets * 2];
		final byte octets_ptr[] = value.get_value();

		for (int i = 0; i < n_octets; i++) {
			ret_val[i * 2] = (byte) ((octets_ptr[i] & 0xF0) >> 4);
			ret_val[i * 2 + 1] = (byte) (octets_ptr[i] & 0x0F);
		}

		return new TitanHexString(ret_val);
	}

	/**
	 * Converts an octetstring into a hexstring.
	 *
	 * <p>
	 * For more details see chapter C.1.23
	 *
	 * @param value
	 *                the octetstring to convert.
	 * @return the converted hexstring.
	 * */
	public static TitanHexString oct2hex(final TitanOctetString_Element value) {
		value.must_bound("The argument of function oct2hex() is an unbound octetstring element.");

		final byte ret_val[] = new byte[] {
			(byte) ((value.get_nibble() & 0xF0) >> 4),
			(byte) (value.get_nibble() & 0x0F)
		};

		return new TitanHexString(ret_val);
	}

	// C.23 - oct2str

	/**
	 * Converts an octetstring into a charstring.
	 * The consecutive order of characters in the resulting charstring
	 *  is the same as the order of hex digits in the octetstring.
	 *
	 * <p>
	 * For more details see chapter C.1.24
	 *
	 * @param value
	 *                the octetstring to convert.
	 * @return the converted charstring.
	 * */
	public static TitanCharString oct2str(final TitanOctetString value) {
		value.must_bound("The argument of function oct2str() is an unbound octetstring value.");

		final StringBuilder ret_val = new StringBuilder();
		final byte octets_ptr[] = value.get_value();
		final int n_octets = octets_ptr.length;
		for (int i = 0; i < n_octets; i++) {
			final int digit = octets_ptr[i] & 0xFF;
			ret_val.append(hexdigit_to_char(digit / 16));
			ret_val.append(hexdigit_to_char(digit % 16));
		}

		return new TitanCharString(ret_val);
	}

	/**
	 * Converts an octetstring into a charstring.
	 * The consecutive order of characters in the resulting charstring
	 *  is the same as the order of hex digits in the octetstring.
	 *
	 * <p>
	 * For more details see chapter C.1.24
	 *
	 * @param value
	 *                the octetstring to convert.
	 * @return the converted charstring.
	 * */
	public static TitanCharString oct2str(final TitanOctetString_Element value) {
		value.must_bound("The argument of function oct2str() is an unbound octetstring element.");

		return new TitanCharString(String.valueOf(value.get_nibble() & 0xFF));
	}

	// C.24 - oct2char

	/**
	 * Converts an octetstring into a charstring.
	 * The octets are interpreted as Recommendation ITU-T T.50 code.
	 *
	 * <p>
	 * For more details see chapter C.1.25
	 *
	 * @param value
	 *                the octetstring to convert.
	 * @return the converted charstring.
	 * */
	public static TitanCharString oct2char(final TitanOctetString value) {
		value.must_bound("The argument of function oct2char() is an unbound octetstring value.");

		final byte octets_ptr[] = value.get_value();
		final int value_length = octets_ptr.length;
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < value_length; i++) {
			if ( (octets_ptr[i] & 0xFF) > 127) {
				throw new TtcnError(MessageFormat.format("The argument of function oct2char() contains octet {0} at index {1}, which is outside the allowed range 00 .. 7F.", (int) value.get_nibble(i), i));
			}
			sb.append(octets_ptr[i] & 0xFF);
		}
		return new TitanCharString(sb);
	}

	/**
	 * Converts an octetstring into a charstring.
	 * The octets are interpreted as Recommendation ITU-T T.50 code.
	 *
	 * <p>
	 * For more details see chapter C.1.25
	 *
	 * @param value
	 *                the octetstring to convert.
	 * @return the converted charstring.
	 * */
	public static TitanCharString oct2char(final TitanOctetString_Element value) {
		value.must_bound("The argument of function oct2char() is an unbound octetstring element.");

		final int octet = value.get_nibble() & 0xFF;
		if (octet > 127) {
			throw new TtcnError(MessageFormat.format("The argument of function oct2char() contains the octet {0}, which is outside the allowed range 00 .. 7F.", octet));
		}

		return new TitanCharString(String.valueOf(octet));
	}

	// C.25 - str2int

	/**
	 * Converts an charstring representing an integer into an integer.
	 *
	 * <p>
	 * For more details see chapter C.1.26
	 *
	 * @param value
	 *                the charstring to convert.
	 * @return the converted integer.
	 * */
	public static TitanInteger str2int(final String value) {
		return str2int(new TitanCharString(value));
	}

	/**
	 * Converts an charstring representing an integer into an integer.
	 *
	 * <p>
	 * For more details see chapter C.1.26
	 *
	 * @param value
	 *                the charstring to convert.
	 * @return the converted integer.
	 * */
	public static TitanInteger str2int(final TitanCharString value) {
		value.must_bound("The argument of function str2int() is an unbound charstring value.");

		final int value_len = value.lengthof().get_int();
		if (value_len == 0) {
			throw new TtcnError("The argument of function str2int() is an empty string, which does not represent a valid integer value.");
		}
		final StringBuilder value_str = new StringBuilder();
		value_str.append(value.get_value().toString());
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
				TTCN_Logger.log_event_str(", does not represent a valid integer value. Invalid character `");
				TTCN_Logger.log_char_escaped(c);
				TTCN_Logger.log_event("' was found at index %d.", i);
				TtcnError.TtcnErrorEnd();
			}
		}
		if (state != str2intState.S_ZERO && state != str2intState.S_MORE && state != str2intState.S_END) {
			TtcnError.TtcnErrorBegin("The argument of function str2int(), which is ");
			value.log();
			TTCN_Logger.log_event_str(", does not represent a valid integer value. Premature end of the string.");
			TtcnError.TtcnErrorEnd();
		}
		if (leading_ws) {
			TtcnError.TtcnWarningBegin("Leading whitespace was detected in the argument of function str2int(): ");
			value.log();
			TTCN_Logger.log_event_str(".");
			TtcnError.TtcnWarningEnd();
		}
		if (leading_zero) {
			TtcnError.TtcnWarningBegin("Leading zero digit was detected in the argument of function str2int(): ");
			value.log();
			TTCN_Logger.log_event_str(".");
			TtcnError.TtcnWarningEnd();
		}
		if (state == str2intState.S_END) {
			TtcnError.TtcnWarningBegin("Trailing whitespace was detected in the argument of function str2int(): ");
			value.log();
			TTCN_Logger.log_event_str(".");
			TtcnError.TtcnWarningEnd();
		}

		return new TitanInteger(value_str.toString());
	}

	/**
	 * Converts an charstring representing an integer into an integer.
	 *
	 * <p>
	 * For more details see chapter C.1.26
	 *
	 * @param value
	 *                the charstring to convert.
	 * @return the converted integer.
	 * */
	public static TitanInteger str2int(final TitanCharString_Element value) {
		value.must_bound("The argument of function str2int() is an unbound charstring element.");

		final char c = value.get_char();
		if (c < '0' || c > '9') {
			TtcnError.TtcnErrorBegin("The argument of function str2int(), which is a charstring element containing character `");
			TTCN_Logger.log_char_escaped(c);
			TTCN_Logger.log_event_str("', does not represent a valid integer value.");
			TtcnError.TtcnErrorEnd();
		}
		return new TitanInteger(Integer.valueOf(c - '0'));
	}

	// str2hex

	/**
	 * Converts an charstring representing a hexstring.
	 * Each character of invalue shall be converted to the corresponding hexadecimal digit
	 * <p>
	 * For more details see chapter C.1.27
	 *
	 * @param value
	 *                the charstring to convert.
	 * @return the converted hexstring.
	 * */
	public static TitanHexString str2hex(final String value) {
		if (value == null) {
			return new TitanHexString();
		} else {
			return str2hex(new TitanCharString(value));
		}
	}

	/**
	 * Converts an charstring representing a hexstring.
	 * Each character of invalue shall be converted to the corresponding hexadecimal digit
	 * <p>
	 * For more details see chapter C.1.27
	 *
	 * @param value
	 *                the charstring to convert.
	 * @return the converted hexstring.
	 * */
	public static TitanHexString str2hex(final TitanCharString value) {
		value.must_bound("The argument of function str2hex() is an unbound charstring value.");

		final int value_length = value.lengthof().get_int();
		final StringBuilder chars_ptr = new StringBuilder();
		chars_ptr.append(value.get_value().toString());
		final byte ret_val[] = new byte[value_length];

		for (int i = 0; i < value_length; i++) {
			final char c = chars_ptr.charAt(i);
			final byte hexdigit = char_to_hexdigit(c);
			if (hexdigit < 0x00) {
				TtcnError.TtcnErrorBegin("The argument of function str2hex() shall contain hexadecimal digits only, but character `");
				TTCN_Logger.log_char_escaped(c);
				TTCN_Logger.log_event_str(MessageFormat.format("'' was found at index {0}.", i));
				TtcnError.TtcnErrorEnd();
			}
			ret_val[i] = hexdigit;
		}

		return new TitanHexString(ret_val);
	}

	/**
	 * Converts an charstring representing a hexstring.
	 * Each character of invalue shall be converted to the corresponding hexadecimal digit
	 * <p>
	 * For more details see chapter C.1.27
	 *
	 * @param value
	 *                the charstring to convert.
	 * @return the converted hexstring.
	 * */
	public static TitanHexString str2hex(final TitanCharString_Element value) {
		value.must_bound("The argument of function str2hex() is an unbound charstring element.");

		final char c = value.get_char();
		final byte hexdigit = char_to_hexdigit(c);

		if (hexdigit < 0x00) {
			TtcnError.TtcnErrorBegin("The argument of function str2hex() shall contain only hexadecimal digits, but the given charstring element contains the character `");
			TTCN_Logger.log_char_escaped(c);
			TTCN_Logger.log_event_str("'.");
			TtcnError.TtcnErrorEnd();
		}

		return new TitanHexString(hexdigit);
	}

	// C.26 - str2oct

	/**
	 * Converts an charstring representing an octetstring.
	 * Each character of invalue shall be converted to the corresponding hexadecimal digit.
	 * When the input contains even number of characters,
	 *  the resulting octetstring is extended with a 0 as leftmost character.
	 * <p>
	 * For more details see chapter C.1.28
	 *
	 * @param value
	 *                the charstring to convert.
	 * @return the converted octetstring.
	 * */
	public static TitanOctetString str2oct(final String value) {
		if (value == null) {
			return new TitanOctetString();
		} else {
			return str2oct(new TitanCharString(value));
		}
	}

	/**
	 * Converts an charstring representing an octetstring.
	 * Each character of invalue shall be converted to the corresponding hexadecimal digit.
	 * When the input contains even number of characters,
	 *  the resulting octetstring is extended with a 0 as leftmost character.
	 * <p>
	 * For more details see chapter C.1.28
	 *
	 * @param value
	 *                the charstring to convert.
	 * @return the converted octetstring.
	 * */
	public static TitanOctetString str2oct(final TitanCharString value) {
		value.must_bound("The argument of function str2oct() is an unbound charstring value.");

		final int value_len = value.lengthof().get_int();
		if (value_len % 2 != 0) {
			throw new TtcnError(MessageFormat.format("The argument of function str2oct() must have even number of characters containing hexadecimal digits, but the length of the string is odd: {0}.", value_len));
		}

		final byte octets_ptr[] = new byte[value_len / 2];
		final StringBuilder chars_ptr = new StringBuilder();
		chars_ptr.append(value.get_value().toString());
		for (int i = 0; i < value_len; i++) {
			final char c = chars_ptr.charAt(i);
			final byte hexdigit = char_to_hexdigit(c);
			if (hexdigit > 0x0F) {
				TtcnError.TtcnErrorBegin("The argument of function str2oct() shall contain hexadecimal digits only, but character `");
				TTCN_Logger.log_char_escaped(c);
				TTCN_Logger.log_event_str(MessageFormat.format("' was found at index {0}.", i));
				TtcnError.TtcnErrorEnd();
			}
			if (i % 2 != 0) {
				octets_ptr[i / 2] = (byte) (octets_ptr[i / 2] | hexdigit);
			} else {
				octets_ptr[i / 2] = (byte) (hexdigit << 4);
			}
		}

		return new TitanOctetString(octets_ptr);
	}

	// C.27 - str2float

	/**
	 * Converts an charstring representing a float.
	 * <p>
	 * leading zeros are allowed;
	 * leading "+" sign before positive values is allowed;
	 * "-0.0" is allowed;
	 * no numbers after the dot in the decimal notation are allowed.
	 * <p>
	 * For more details see chapter C.1.29
	 *
	 * @param value
	 *                the charstring to convert.
	 * @return the converted float.
	 * */
	public static TitanFloat str2float(final String value){
		return str2float(new TitanCharString(value));
	}

	/**
	 * Converts an charstring representing a float.
	 * <p>
	 * leading zeros are allowed;
	 * leading "+" sign before positive values is allowed;
	 * "-0.0" is allowed;
	 * no numbers after the dot in the decimal notation are allowed.
	 * <p>
	 * For more details see chapter C.1.29
	 *
	 * @param value
	 *                the charstring to convert.
	 * @return the converted float.
	 * */
	public static TitanFloat str2float(final TitanCharString value) {
		value.must_bound("The argument of function str2float() is an unbound charstring value.");

		final int value_len = value.lengthof().get_int();
		if (value_len == 0) {
			throw new TtcnError("The argument of function str2float() is an empty string, which does not represent a valid float value.");
		}
		if (value.operator_equals("infinity")) {
			return new TitanFloat(Double.POSITIVE_INFINITY);
		}
		if (value.operator_equals("-infinity")) {
			return new TitanFloat(Double.NEGATIVE_INFINITY);
		}
		if (value.operator_equals("not_a_number")) {
			return new TitanFloat(Double.NaN);
		}
		final StringBuilder value_str = value.get_value();
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
				TTCN_Logger.log_event_str("' , does not represent a valid float value. Invalid character ");
				TTCN_Logger.log_char_escaped(c);
				TTCN_Logger.log_event_str(MessageFormat.format("' was found at index {0}.", i));
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
			TTCN_Logger.log_event_str("' , does not represent a valid float value. Premature end of the string.");
			TtcnError.TtcnErrorEnd();
			break;
		}
		if (leading_ws) {
			TtcnError.TtcnWarningBegin("Leading whitespace was detected in the argument of function str2float(): ");
			value.log();
			TTCN_Logger.log_char('.');
			TtcnError.TtcnWarningEnd();
		}
		if (leading_zero) {
			TtcnError.TtcnWarningBegin("Leading zero digit was detected in the argument of function str2float(): ");
			value.log();
			TTCN_Logger.log_char('.');
			TtcnError.TtcnWarningEnd();
		}
		if (state == str2floatState.S_END) {
			TtcnError.TtcnWarningBegin("Trailing whitespace was detected in the argument of function str2float(): ");
			value.log();
			TTCN_Logger.log_char('.');
			TtcnError.TtcnWarningEnd();
		}

		return new TitanFloat(Double.valueOf(value_str.toString()));
	}

	/**
	 * Converts an octetstring into a universal charstring.
	 * <p>
	 * The octets are interpreted as UTF-8 encoded.
	 * <p>
	 * For more details see chapter C.1.31
	 *
	 * @param value
	 *                the octetstring to convert.
	 * @return the converted universal charstring.
	 * */
	public static TitanUniversalCharString oct2unichar(final TitanOctetString value) {
		// default encoding is UTF-8
		final TitanUniversalCharString unicharStr = new TitanUniversalCharString();
		final TTCN_EncDec.error_behavior_type err_behavior = TTCN_EncDec.get_error_behavior(TTCN_EncDec.error_type.ET_DEC_UCSTR);
		TTCN_EncDec.set_error_behavior(TTCN_EncDec.error_type.ET_DEC_UCSTR, TTCN_EncDec.error_behavior_type.EB_ERROR);

		unicharStr.decode_utf8(value.get_value(), CharCoding.UTF_8, true);

		TTCN_EncDec.set_error_behavior(TTCN_EncDec.error_type.ET_DEC_UCSTR, err_behavior);

		return unicharStr;
	}

	/**
	 * Converts an octetstring into a universal charstring.
	 * <p>
	 * The octets are interpreted using the mapping associated with the
	 * given encodeStr.
	 * <p>
	 * For more details see chapter C.1.31
	 *
	 * @param value
	 *                the octetstring to convert.
	 * @param encodeStr
	 *                the string encoding to use for mapping.
	 * @return the converted universal charstring.
	 * */
	public static TitanUniversalCharString oct2unichar(final TitanOctetString value, final TitanCharString encodeStr) {
		// default encoding is UTF-8
		final TitanUniversalCharString unicharStr = new TitanUniversalCharString();

		final TTCN_EncDec.error_behavior_type err_behavior = TTCN_EncDec.get_error_behavior(TTCN_EncDec.error_type.ET_DEC_UCSTR);
		TTCN_EncDec.set_error_behavior(TTCN_EncDec.error_type.ET_DEC_UCSTR, TTCN_EncDec.error_behavior_type.EB_ERROR);

		if (encodeStr.operator_equals("UTF-8")) {
			unicharStr.decode_utf8(value.get_value(), CharCoding.UTF_8, true);
		} else if (encodeStr.operator_equals("UTF-16")) {
			unicharStr.decode_utf16(value.lengthof().get_int(), value.get_value(), CharCoding.UTF16);
		} else if (encodeStr.operator_equals("UTF-16BE")) {
			unicharStr.decode_utf16(value.lengthof().get_int(), value.get_value(), CharCoding.UTF16BE);
		} else if (encodeStr.operator_equals("UTF-16LE")) {
			unicharStr.decode_utf16(value.lengthof().get_int(), value.get_value(), CharCoding.UTF16LE);
		} else if (encodeStr.operator_equals("UTF-32")) {
			unicharStr.decode_utf32(value.lengthof().get_int(), value.get_value(), CharCoding.UTF32);
		} else if (encodeStr.operator_equals("UTF-32BE")) {
			unicharStr.decode_utf32(value.lengthof().get_int(), value.get_value(), CharCoding.UTF32BE);
		} else if (encodeStr.operator_equals("UTF-32LE")) {
			unicharStr.decode_utf32(value.lengthof().get_int(), value.get_value(), CharCoding.UTF32LE);
		} else {
			throw new TtcnError("oct2unichar: Invalid parameter: " + encodeStr);
		}

		TTCN_EncDec.set_error_behavior(TTCN_EncDec.error_type.ET_DEC_UCSTR, err_behavior);

		return unicharStr;
	}

	/**
	 * Converts an octetstring into a universal charstring.
	 * <p>
	 * The octets are interpreted using the mapping associated with the
	 * given encodeStr.
	 * <p>
	 * For more details see chapter C.1.31
	 *
	 * @param value
	 *                the octetstring to convert.
	 * @param encodeStr
	 *                the string encoding to use for mapping.
	 * @return the converted universal charstring.
	 * */
	public static TitanUniversalCharString oct2unichar(final TitanOctetString value, final String encodeStr) {
		return oct2unichar(value, new TitanCharString(encodeStr));
	}

	/**
	 * Converts a universal charstring into an octetstring.
	 * <p>
	 * The octets are interpreted using the UTF-8 mapping rules.
	 * <p>
	 * For more details see chapter C.1.32
	 *
	 * @param value
	 *                the universal charstring to convert.
	 * @return the converted octetstring.
	 * */
	public static TitanOctetString unichar2oct(final TitanUniversalCharString value) {
		// no encoding parameter is default UTF-8
		value.must_bound("The argument of function unichar2oct() is an unbound universal charstring value.");

		final TTCN_EncDec.error_behavior_type err_behavior = TTCN_EncDec.get_error_behavior(TTCN_EncDec.error_type.ET_DEC_UCSTR);
		TTCN_EncDec.set_error_behavior(TTCN_EncDec.error_type.ET_DEC_UCSTR, TTCN_EncDec.error_behavior_type.EB_ERROR);

		final TTCN_Buffer buf = new TTCN_Buffer();
		value.encode_utf8(buf, false);

		TTCN_EncDec.set_error_behavior(TTCN_EncDec.error_type.ET_DEC_UCSTR, err_behavior);

		return new TitanOctetString(buf.get_data());
	}

	/**
	 * Converts a universal charstring into an octetstring.
	 * <p>
	 * The octets are interpreted using the mapping rule in stringEncoding
	 * parameter.
	 * <p>
	 * For more details see chapter C.1.32
	 *
	 * @param value
	 *                the universal charstring to convert.
	 * @param stringEncoding
	 *                the string encoding to use for mapping.
	 * @return the converted octetstring.
	 * */
	public static TitanOctetString unichar2oct(final TitanUniversalCharString value, final TitanCharString stringEncoding) {
		value.must_bound("The argument of function unichar2oct() is an unbound universal charstring value.");

		final TTCN_EncDec.error_behavior_type err_behavior = TTCN_EncDec.get_error_behavior(TTCN_EncDec.error_type.ET_DEC_UCSTR);
		TTCN_EncDec.set_error_behavior(TTCN_EncDec.error_type.ET_DEC_UCSTR, TTCN_EncDec.error_behavior_type.EB_ERROR);

		final TTCN_Buffer buf = new TTCN_Buffer();

		if (stringEncoding.operator_equals("UTF-8")) {
			value.encode_utf8(buf, false);
		} else if (stringEncoding.operator_equals("UTF-8 BOM")) {
			value.encode_utf8(buf, true);
		} else if (stringEncoding.operator_equals("UTF-16")) {
			value.encode_utf16(buf, CharCoding.UTF16);
		} else if (stringEncoding.operator_equals("UTF-16BE")) {
			value.encode_utf16(buf, CharCoding.UTF16BE);
		} else if (stringEncoding.operator_equals("UTF-16LE")) {
			value.encode_utf16(buf, CharCoding.UTF16LE);
		} else if (stringEncoding.operator_equals("UTF-32")) {
			value.encode_utf32(buf, CharCoding.UTF32);
		} else if (stringEncoding.operator_equals("UTF-32BE")) {
			value.encode_utf32(buf, CharCoding.UTF32BE);
		} else if (stringEncoding.operator_equals("UTF-32LE")) {
			value.encode_utf32(buf, CharCoding.UTF32LE);
		} else {
			throw new TtcnError("unichar2oct: Invalid parameter: " + stringEncoding);
		}

		TTCN_EncDec.set_error_behavior(TTCN_EncDec.error_type.ET_DEC_UCSTR, err_behavior);

		return new TitanOctetString(buf.get_data());
	}

	/**
	 * Converts a universal charstring into an octetstring.
	 * <p>
	 * The octets are interpreted using the mapping rule in stringEncoding
	 * parameter.
	 * <p>
	 * For more details see chapter C.1.32
	 *
	 * @param value
	 *                the universal charstring to convert.
	 * @param stringEncoding
	 *                the string encoding to use for mapping.
	 * @return the converted octetstring.
	 * */
	public static TitanOctetString unichar2oct(final TitanUniversalCharString value, final String stringEncoding) {
		return unichar2oct(value, new TitanCharString(stringEncoding));
	}

	// C.34 - substr
	/**
	 * Check the arguments of the substring functions.
	 * <p>
	 * The index must not be negative or larger then the length.
	 * Return count must not be negative.
	 * The index and return count must not be larger then the length.
	 *
	 * @param value_length
	 *                the current value length.
	 * @param idx
	 *                the starting index.
	 * @param returncount
	 *                the length to be returned.
	 * @param string_type
	 *                the type of the string to be used in error messages.
	 * @param element_name
	 *                the name of the string element to be used in error
	 *                messages.
	 * */
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

	/**
	 * Check the arguments of the substring functions.
	 * <p>
	 * The index must be 0 or 1.
	 * Return count must not be negative.
	 * The index and return count must be more than 1.
	 *
	 * @param value_length
	 *                the current value length.
	 * @param idx
	 *                the starting index.
	 * @param returncount
	 *                the length to be returned.
	 * @param string_type
	 *                the type of the string to be used in error messages.
	 * @param element_name
	 *                the name of the string element to be used in error
	 *                messages.
	 * */
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

	/**
	 * Return a substring of the provided string.
	 * <p>
	 * For more details see chapter C.4.2
	 *
	 * @param value
	 *                the input string.
	 * @param idx
	 *                the starting index.
	 * @param returncount
	 *                the number of characters to return.
	 * @return the resulting string.
	 * */
	public static TitanBitString substr(final TitanBitString value, final int idx, final int returncount) {
		value.must_bound("The first argument (value) of function substr() is an unbound bitstring value.");

		check_substr_arguments(value.lengthof().get_int(), idx, returncount, "bitstring", "bit");
		if (idx % 8 != 0) {
			final StringBuilder sb = new StringBuilder(returncount);
			for (int i = 0; i < returncount; i++) {
				sb.append(value.get_bit(idx + i) ? '1' : '0');
			}
			return new TitanBitString(sb.toString());
		} else {
			final int bits_ptr[] = value.get_value();
			final int ret_val[] = new int[(returncount + 7) / 8];
			System.arraycopy(bits_ptr, idx / 8, ret_val, 0, (returncount + 7) / 8);
			return new TitanBitString(ret_val, returncount);
		}
	}

	/**
	 * Return a substring of the provided string.
	 * <p>
	 * For more details see chapter C.4.2
	 *
	 * @param value
	 *                the input string.
	 * @param idx
	 *                the starting index.
	 * @param returncount
	 *                the number of characters to return.
	 * @return the resulting string.
	 * */
	public static TitanBitString substr(final TitanBitString value, final int idx, final TitanInteger returncount) {
		returncount.must_bound("The third argument (returncount) of function substr() is an unbound integer value.");

		return substr(value, idx, returncount.get_int());
	}

	/**
	 * Return a substring of the provided string.
	 * <p>
	 * For more details see chapter C.4.2
	 *
	 * @param value
	 *                the input string.
	 * @param idx
	 *                the starting index.
	 * @param returncount
	 *                the number of characters to return.
	 * @return the resulting string.
	 * */
	public static TitanBitString substr(final TitanBitString value, final TitanInteger idx, final int returncount) {
		idx.must_bound("The second argument (index) of function substr() is an unbound integer value.");

		return substr(value, idx.get_int(), returncount);
	}

	/**
	 * Return a substring of the provided string.
	 * <p>
	 * For more details see chapter C.4.2
	 *
	 * @param value
	 *                the input string.
	 * @param idx
	 *                the starting index.
	 * @param returncount
	 *                the number of characters to return.
	 * @return the resulting string.
	 * */
	public static TitanBitString substr(final TitanBitString value, final TitanInteger idx, final TitanInteger returncount) {
		idx.must_bound("The second argument (index) of function substr() is an unbound integer value.");
		returncount.must_bound("The third argument (returncount) of function substr() is an unbound integer value.");

		return substr(value, idx.get_int(), returncount.get_int());
	}

	/**
	 * Return a substring of the provided string.
	 * <p>
	 * For more details see chapter C.4.2
	 *
	 * @param value
	 *                the input string.
	 * @param idx
	 *                the starting index.
	 * @param returncount
	 *                the number of characters to return.
	 * @return the resulting string.
	 * */
	public static TitanBitString substr(final TitanBitString_Element value, final int idx, final int returncount) {
		value.must_bound("The first argument (value) of function substr() is an unbound bitstring element.");

		check_substr_arguments(idx, returncount, "bitstring", "bit");
		if (returncount == 0) {
			return new TitanBitString();
		} else {
			return new TitanBitString(value.get_bit() ? "1" : "0");
		}
	}

	/**
	 * Return a substring of the provided string.
	 * <p>
	 * For more details see chapter C.4.2
	 *
	 * @param value
	 *                the input string.
	 * @param idx
	 *                the starting index.
	 * @param returncount
	 *                the number of characters to return.
	 * @return the resulting string.
	 * */
	public static TitanBitString substr(final TitanBitString_Element value, final int idx, final TitanInteger returncount) {
		returncount.must_bound("The third argument (returncount) of function substr() is an unbound integer value.");

		return substr(value, idx, returncount.get_int());
	}

	/**
	 * Return a substring of the provided string.
	 * <p>
	 * For more details see chapter C.4.2
	 *
	 * @param value
	 *                the input string.
	 * @param idx
	 *                the starting index.
	 * @param returncount
	 *                the number of characters to return.
	 * @return the resulting string.
	 * */
	public static TitanBitString substr(final TitanBitString_Element value, final TitanInteger idx, final int returncount) {
		idx.must_bound("The second argument (index) of function substr() is an unbound integer value.");

		return substr(value, idx.get_int(), returncount);
	}

	/**
	 * Return a substring of the provided string.
	 * <p>
	 * For more details see chapter C.4.2
	 *
	 * @param value
	 *                the input string.
	 * @param idx
	 *                the starting index.
	 * @param returncount
	 *                the number of characters to return.
	 * @return the resulting string.
	 * */
	public static TitanBitString substr(final TitanBitString_Element value, final TitanInteger idx, final TitanInteger returncount) {
		idx.must_bound("The second argument (index) of function substr() is an unbound integer value.");
		returncount.must_bound("The third argument (returncount) of function substr() is an unbound integer value.");

		return substr(value, idx.get_int(), returncount.get_int());
	}

	/**
	 * Return a substring of the provided string.
	 * <p>
	 * For more details see chapter C.4.2
	 *
	 * @param value
	 *                the input string.
	 * @param idx
	 *                the starting index.
	 * @param returncount
	 *                the number of characters to return.
	 * @return the resulting string.
	 * */
	public static TitanHexString substr(final TitanHexString value, final int idx, final int returncount) {
		value.must_bound("The first argument (value) of function substr() is an unbound hexstring value.");

		check_substr_arguments(value.lengthof().get_int(), idx, returncount, "hexstring", "hexadecimal digi");
		final byte src_ptr[] = value.get_value();
		final byte ret_val[] = new byte[returncount];
		System.arraycopy(src_ptr, idx, ret_val, 0, returncount);

		return new TitanHexString(ret_val);
	}

	/**
	 * Return a substring of the provided string.
	 * <p>
	 * For more details see chapter C.4.2
	 *
	 * @param value
	 *                the input string.
	 * @param idx
	 *                the starting index.
	 * @param returncount
	 *                the number of characters to return.
	 * @return the resulting string.
	 * */
	public static TitanHexString substr(final TitanHexString value, final int idx, final TitanInteger returncount) {
		returncount.must_bound("The third argument (returncount) of function substr() is an unbound integer value.");

		return substr(value, idx, returncount.get_int());
	}

	/**
	 * Return a substring of the provided string.
	 * <p>
	 * For more details see chapter C.4.2
	 *
	 * @param value
	 *                the input string.
	 * @param idx
	 *                the starting index.
	 * @param returncount
	 *                the number of characters to return.
	 * @return the resulting string.
	 * */
	public static TitanHexString substr(final TitanHexString value, final TitanInteger idx, final int returncount) {
		idx.must_bound("The second argument (index) of function substr() is an unbound integer value.");

		return substr(value, idx.get_int(), returncount);
	}

	/**
	 * Return a substring of the provided string.
	 * <p>
	 * For more details see chapter C.4.2
	 *
	 * @param value
	 *                the input string.
	 * @param idx
	 *                the starting index.
	 * @param returncount
	 *                the number of characters to return.
	 * @return the resulting string.
	 * */
	public static TitanHexString substr(final TitanHexString value, final TitanInteger idx, final TitanInteger returncount) {
		idx.must_bound("The second argument (index) of function substr() is an unbound integer value.");
		returncount.must_bound("The third argument (returncount) of function substr() is an unbound integer value.");

		return substr(value, idx.get_int(), returncount.get_int());
	}

	/**
	 * Return a substring of the provided string.
	 * <p>
	 * For more details see chapter C.4.2
	 *
	 * @param value
	 *                the input string.
	 * @param idx
	 *                the starting index.
	 * @param returncount
	 *                the number of characters to return.
	 * @return the resulting string.
	 * */
	public static TitanHexString substr(final TitanHexString_Element value, final int idx, final int returncount) {
		value.must_bound("The first argument (value) of function substr() is an unbound hexstring element.");

		check_substr_arguments(idx, returncount, "hexstring", "hexadecimal digit");
		if (returncount == 0) {
			return new TitanHexString();
		} else {
			return new TitanHexString(String.valueOf(value.get_nibble()));
		}
	}

	/**
	 * Return a substring of the provided string.
	 * <p>
	 * For more details see chapter C.4.2
	 *
	 * @param value
	 *                the input string.
	 * @param idx
	 *                the starting index.
	 * @param returncount
	 *                the number of characters to return.
	 * @return the resulting string.
	 * */
	public static TitanHexString substr(final TitanHexString_Element value, final int idx, final TitanInteger returncount) {
		returncount.must_bound("The third argument (returncount) of function substr() is an unbound integer value.");

		return substr(value, idx, returncount.get_int());
	}

	/**
	 * Return a substring of the provided string.
	 * <p>
	 * For more details see chapter C.4.2
	 *
	 * @param value
	 *                the input string.
	 * @param idx
	 *                the starting index.
	 * @param returncount
	 *                the number of characters to return.
	 * @return the resulting string.
	 * */
	public static TitanHexString substr(final TitanHexString_Element value, final TitanInteger idx, final int returncount) {
		idx.must_bound("The second argument (index) of function substr() is an unbound integer value.");

		return substr(value, idx.get_int(), returncount);
	}

	/**
	 * Return a substring of the provided string.
	 * <p>
	 * For more details see chapter C.4.2
	 *
	 * @param value
	 *                the input string.
	 * @param idx
	 *                the starting index.
	 * @param returncount
	 *                the number of characters to return.
	 * @return the resulting string.
	 * */
	public static TitanHexString substr(final TitanHexString_Element value, final TitanInteger idx, final TitanInteger returncount) {
		idx.must_bound("The second argument (index) of function substr() is an unbound integer value.");
		returncount.must_bound("The third argument (returncount) of function substr() is an unbound integer value.");

		return substr(value, idx.get_int(), returncount.get_int());
	}

	/**
	 * Return a substring of the provided string.
	 * <p>
	 * For more details see chapter C.4.2
	 *
	 * @param value
	 *                the input string.
	 * @param idx
	 *                the starting index.
	 * @param returncount
	 *                the number of characters to return.
	 * @return the resulting string.
	 * */
	public static TitanOctetString substr(final TitanOctetString value, final int idx, final int returncount) {
		value.must_bound("The first argument (value) of function substr() is an unbound octetstring value.");

		check_substr_arguments(value.lengthof().get_int(), idx, returncount, "octetstring", "octet");
		final byte ret_val[] = new byte[returncount];
		final byte src_ptr[] = value.get_value();
		System.arraycopy(src_ptr, idx, ret_val, 0, returncount);

		return new TitanOctetString(ret_val);
	}

	/**
	 * Return a substring of the provided string.
	 * <p>
	 * For more details see chapter C.4.2
	 *
	 * @param value
	 *                the input string.
	 * @param idx
	 *                the starting index.
	 * @param returncount
	 *                the number of characters to return.
	 * @return the resulting string.
	 * */
	public static TitanOctetString substr(final TitanOctetString value, final int idx, final TitanInteger returncount) {
		returncount.must_bound("The third argument (returncount) of function substr() is an unbound integer value.");

		return substr(value, idx, returncount.get_int());
	}

	/**
	 * Return a substring of the provided string.
	 * <p>
	 * For more details see chapter C.4.2
	 *
	 * @param value
	 *                the input string.
	 * @param idx
	 *                the starting index.
	 * @param returncount
	 *                the number of characters to return.
	 * @return the resulting string.
	 * */
	public static TitanOctetString substr(final TitanOctetString value, final TitanInteger idx, final int returncount) {
		idx.must_bound("The second argument (index) of function substr() is an unbound integer value.");

		return substr(value, idx.get_int(), returncount);
	}

	/**
	 * Return a substring of the provided string.
	 * <p>
	 * For more details see chapter C.4.2
	 *
	 * @param value
	 *                the input string.
	 * @param idx
	 *                the starting index.
	 * @param returncount
	 *                the number of characters to return.
	 * @return the resulting string.
	 * */
	public static TitanOctetString substr(final TitanOctetString value, final TitanInteger idx, final TitanInteger returncount) {
		idx.must_bound("The second argument (index) of function substr() is an unbound integer value.");
		returncount.must_bound("The third argument (returncount) of function substr() is an unbound integer value.");

		return substr(value, idx.get_int(), returncount.get_int());
	}

	/**
	 * Return a substring of the provided string.
	 * <p>
	 * For more details see chapter C.4.2
	 *
	 * @param value
	 *                the input string.
	 * @param idx
	 *                the starting index.
	 * @param returncount
	 *                the number of characters to return.
	 * @return the resulting string.
	 * */
	public static TitanOctetString substr(final TitanOctetString_Element value, final int idx, final int returncount) {
		value.must_bound("The first argument (value) of function substr() is an unbound octetstring element.");

		check_substr_arguments(idx, returncount, "octetstring", "octet");
		if (returncount == 0) {
			return new TitanOctetString();
		} else {
			return new TitanOctetString(value.get_nibble());
		}
	}

	/**
	 * Return a substring of the provided string.
	 * <p>
	 * For more details see chapter C.4.2
	 *
	 * @param value
	 *                the input string.
	 * @param idx
	 *                the starting index.
	 * @param returncount
	 *                the number of characters to return.
	 * @return the resulting string.
	 * */
	public static TitanOctetString substr(final TitanOctetString_Element value, final int idx, final TitanInteger returncount) {
		returncount.must_bound("The third argument (returncount) of function substr() is an unbound integer value.");

		return substr(value, idx, returncount.get_int());
	}

	/**
	 * Return a substring of the provided string.
	 * <p>
	 * For more details see chapter C.4.2
	 *
	 * @param value
	 *                the input string.
	 * @param idx
	 *                the starting index.
	 * @param returncount
	 *                the number of characters to return.
	 * @return the resulting string.
	 * */
	public static TitanOctetString substr(final TitanOctetString_Element value, final TitanInteger idx, final int returncount) {
		idx.must_bound("The second argument (index) of function substr() is an unbound integer value.");

		return substr(value, idx.get_int(), returncount);
	}

	/**
	 * Return a substring of the provided string.
	 * <p>
	 * For more details see chapter C.4.2
	 *
	 * @param value
	 *                the input string.
	 * @param idx
	 *                the starting index.
	 * @param returncount
	 *                the number of characters to return.
	 * @return the resulting string.
	 * */
	public static TitanOctetString substr(final TitanOctetString_Element value, final TitanInteger idx, final TitanInteger returncount) {
		idx.must_bound("The second argument (index) of function substr() is an unbound integer value.");
		returncount.must_bound("The third argument (returncount) of function substr() is an unbound integer value.");

		return substr(value, idx.get_int(), returncount.get_int());
	}

	/**
	 * Return a substring of the provided string.
	 * <p>
	 * For more details see chapter C.4.2
	 *
	 * @param value
	 *                the input string.
	 * @param idx
	 *                the starting index.
	 * @param returncount
	 *                the number of characters to return.
	 * @return the resulting string.
	 * */
	public static TitanCharString substr(final TitanCharString value, final int idx, final int returncount) {
		value.must_bound("The first argument (value) of function substr() is an unbound charstring value.");

		check_substr_arguments(value.lengthof().get_int(), idx, returncount, "charstring", "character");

		return new TitanCharString(value.get_value().substring(idx, idx + returncount));
	}

	/**
	 * Return a substring of the provided string.
	 * <p>
	 * For more details see chapter C.4.2
	 *
	 * @param value
	 *                the input string.
	 * @param idx
	 *                the starting index.
	 * @param returncount
	 *                the number of characters to return.
	 * @return the resulting string.
	 * */
	public static TitanCharString substr(final TitanCharString value, final int idx, final TitanInteger returncount) {
		returncount.must_bound("The third argument (returncount) of function substr() is an unbound integer value.");

		return substr(value, idx, returncount.get_int());
	}

	/**
	 * Return a substring of the provided string.
	 * <p>
	 * For more details see chapter C.4.2
	 *
	 * @param value
	 *                the input string.
	 * @param idx
	 *                the starting index.
	 * @param returncount
	 *                the number of characters to return.
	 * @return the resulting string.
	 * */
	public static TitanCharString substr(final TitanCharString value, final TitanInteger idx, final int returncount) {
		idx.must_bound("The second argument (index) of function substr() is an unbound integer value.");

		return substr(value, idx.get_int(), returncount);
	}

	/**
	 * Return a substring of the provided string.
	 * <p>
	 * For more details see chapter C.4.2
	 *
	 * @param value
	 *                the input string.
	 * @param idx
	 *                the starting index.
	 * @param returncount
	 *                the number of characters to return.
	 * @return the resulting string.
	 * */
	public static TitanCharString substr(final TitanCharString value, final TitanInteger idx, final TitanInteger returncount) {
		idx.must_bound("The second argument (index) of function substr() is an unbound integer value.");
		returncount.must_bound("The third argument (returncount) of function substr() is an unbound integer value.");

		return substr(value, idx.get_int(), returncount.get_int());
	}

	/**
	 * Return a substring of the provided string.
	 * <p>
	 * For more details see chapter C.4.2
	 *
	 * @param value
	 *                the input string.
	 * @param idx
	 *                the starting index.
	 * @param returncount
	 *                the number of characters to return.
	 * @return the resulting string.
	 * */
	public static TitanCharString substr(final TitanCharString_Element value, final int idx, final int returncount) {
		value.must_bound("The first argument (value) of function substr() is an unbound charstring element.");

		check_substr_arguments(idx, returncount, "charstring", "character");

		return new TitanCharString(String.valueOf(value.get_char()));
	}

	/**
	 * Return a substring of the provided string.
	 * <p>
	 * For more details see chapter C.4.2
	 *
	 * @param value
	 *                the input string.
	 * @param idx
	 *                the starting index.
	 * @param returncount
	 *                the number of characters to return.
	 * @return the resulting string.
	 * */
	public static TitanCharString substr(final TitanCharString_Element value, final int idx, final TitanInteger returncount) {
		returncount.must_bound("The third argument (returncount) of function substr() is an unbound integer value.");

		return substr(value, idx, returncount.get_int());
	}

	/**
	 * Return a substring of the provided string.
	 * <p>
	 * For more details see chapter C.4.2
	 *
	 * @param value
	 *                the input string.
	 * @param idx
	 *                the starting index.
	 * @param returncount
	 *                the number of characters to return.
	 * @return the resulting string.
	 * */
	public static TitanCharString substr(final TitanCharString_Element value, final TitanInteger idx, final int returncount) {
		idx.must_bound("The second argument (index) of function substr() is an unbound integer value.");

		return substr(value, idx.get_int(), returncount);
	}

	/**
	 * Return a substring of the provided string.
	 * <p>
	 * For more details see chapter C.4.2
	 *
	 * @param value
	 *                the input string.
	 * @param idx
	 *                the starting index.
	 * @param returncount
	 *                the number of characters to return.
	 * @return the resulting string.
	 * */
	public static TitanCharString substr(final TitanCharString_Element value, final TitanInteger idx, final TitanInteger returncount) {
		idx.must_bound("The second argument (index) of function substr() is an unbound integer value.");
		returncount.must_bound("The third argument (returncount) of function substr() is an unbound integer value.");

		return substr(value, idx.get_int(), returncount.get_int());
	}

	/**
	 * Return a substring of the provided string.
	 * <p>
	 * For more details see chapter C.4.2
	 *
	 * @param value
	 *                the input string.
	 * @param idx
	 *                the starting index.
	 * @param returncount
	 *                the number of characters to return.
	 * @return the resulting string.
	 * */
	public static TitanUniversalCharString substr(final TitanUniversalCharString value, final int idx, final int returncount) {
		value.must_bound("The first argument (value) of function substr() is an unbound universal charstring value.");

		check_substr_arguments(value.lengthof().get_int(), idx, returncount, "universal charstring", "character");
		if (value.charstring) {
			return new TitanUniversalCharString(value.cstr.substring(idx, idx + returncount));
		} else {
			final List<TitanUniversalChar> ret_val = new ArrayList<TitanUniversalChar>(returncount);
			final List<TitanUniversalChar> src_ptr = value.get_value();
			for (int i = 0; i < returncount; i++) {
				ret_val.add(src_ptr.get(i + idx));
			}

			return new TitanUniversalCharString(ret_val);
		}
	}

	/**
	 * Return a substring of the provided string.
	 * <p>
	 * For more details see chapter C.4.2
	 *
	 * @param value
	 *                the input string.
	 * @param idx
	 *                the starting index.
	 * @param returncount
	 *                the number of characters to return.
	 * @return the resulting string.
	 * */
	public static TitanUniversalCharString substr(final TitanUniversalCharString value, final int idx, final TitanInteger returncount) {
		returncount.must_bound("The third argument (returncount) of function substr() is an unbound integer value.");

		return substr(value, idx, returncount.get_int());
	}

	/**
	 * Return a substring of the provided string.
	 * <p>
	 * For more details see chapter C.4.2
	 *
	 * @param value
	 *                the input string.
	 * @param idx
	 *                the starting index.
	 * @param returncount
	 *                the number of characters to return.
	 * @return the resulting string.
	 * */
	public static TitanUniversalCharString substr(final TitanUniversalCharString value, final TitanInteger idx, final int returncount) {
		idx.must_bound("The second argument (index) of function substr() is an unbound integer value.");

		return substr(value, idx.get_int(), returncount);
	}

	/**
	 * Return a substring of the provided string.
	 * <p>
	 * For more details see chapter C.4.2
	 *
	 * @param value
	 *                the input string.
	 * @param idx
	 *                the starting index.
	 * @param returncount
	 *                the number of characters to return.
	 * @return the resulting string.
	 * */
	public static TitanUniversalCharString substr(final TitanUniversalCharString value, final TitanInteger idx, final TitanInteger returncount) {
		idx.must_bound("The second argument (index) of function substr() is an unbound integer value.");
		returncount.must_bound("The third argument (returncount) of function substr() is an unbound integer value.");

		return substr(value, idx.get_int(), returncount.get_int());
	}

	/**
	 * Return a substring of the provided string.
	 * <p>
	 * For more details see chapter C.4.2
	 *
	 * @param value
	 *                the input string.
	 * @param idx
	 *                the starting index.
	 * @param returncount
	 *                the number of characters to return.
	 * @return the resulting string.
	 * */
	public static TitanUniversalCharString substr(final TitanUniversalCharString_Element value, final int idx, final int returncount) {
		value.must_bound("The first argument (value) of function substr() is an unbound universal charstring element.");

		check_substr_arguments(idx, returncount, "universal charstring", "character");
		if (returncount == 0) {
			return new TitanUniversalCharString();
		} else {
			return new TitanUniversalCharString(value.get_char());
		}
	}

	/**
	 * Return a substring of the provided string.
	 * <p>
	 * For more details see chapter C.4.2
	 *
	 * @param value
	 *                the input string.
	 * @param idx
	 *                the starting index.
	 * @param returncount
	 *                the number of characters to return.
	 * @return the resulting string.
	 * */
	public static TitanUniversalCharString substr(final TitanUniversalCharString_Element value, final int idx, final TitanInteger returncount) {
		returncount.must_bound("The third argument (returncount) of function substr() is an unbound integer value.");

		return substr(value, idx, returncount.get_int());
	}

	/**
	 * Return a substring of the provided string.
	 * <p>
	 * For more details see chapter C.4.2
	 *
	 * @param value
	 *                the input string.
	 * @param idx
	 *                the starting index.
	 * @param returncount
	 *                the number of characters to return.
	 * @return the resulting string.
	 * */
	public static TitanUniversalCharString substr(final TitanUniversalCharString_Element value, final TitanInteger idx, final int returncount) {
		idx.must_bound("The second argument (index) of function substr() is an unbound integer value.");

		return substr(value, idx.get_int(), returncount);
	}

	/**
	 * Return a substring of the provided string.
	 * <p>
	 * For more details see chapter C.4.2
	 *
	 * @param value
	 *                the input string.
	 * @param idx
	 *                the starting index.
	 * @param returncount
	 *                the number of characters to return.
	 * @return the resulting string.
	 * */
	public static TitanUniversalCharString substr(final TitanUniversalCharString_Element value, final TitanInteger idx, final TitanInteger returncount) {
		idx.must_bound("The second argument (index) of function substr() is an unbound integer value.");
		returncount.must_bound("The third argument (returncount) of function substr() is an unbound integer value.");

		return substr(value, idx.get_int(), returncount.get_int());
	}

	//substr() on templates

	/**
	 * Return a substring value of the provided string template.
	 * <p>
	 * For more details see chapter C.4.2
	 *
	 * @param value
	 *                the input string template.
	 * @param idx
	 *                the starting index.
	 * @param returncount
	 *                the number of characters to return.
	 * @return the resulting string.
	 * */
	public static TitanBitString substr(final TitanBitString_template value, final int idx, final int returncount) {
		if (!value.is_value()) {
			throw new TtcnError("The first argument of function substr() is a template with non-specific value.");
		}

		return substr(value.valueof(), idx, returncount);
	}

	/**
	 * Return a substring value of the provided string template.
	 * <p>
	 * For more details see chapter C.4.2
	 *
	 * @param value
	 *                the input string template.
	 * @param idx
	 *                the starting index.
	 * @param returncount
	 *                the number of characters to return.
	 * @return the resulting string.
	 * */
	public static TitanBitString substr(final TitanBitString_template value, final int idx, final TitanInteger returncount) {
		if (!value.is_value()) {
			throw new TtcnError("The first argument of function substr() is a template with non-specific value.");
		}

		return substr(value.valueof(), idx, returncount);
	}

	/**
	 * Return a substring value of the provided string template.
	 * <p>
	 * For more details see chapter C.4.2
	 *
	 * @param value
	 *                the input string template.
	 * @param idx
	 *                the starting index.
	 * @param returncount
	 *                the number of characters to return.
	 * @return the resulting string.
	 * */
	public static TitanBitString substr(final TitanBitString_template value, final TitanInteger idx, final int returncount) {
		if (!value.is_value()) {
			throw new TtcnError("The first argument of function substr() is a template with non-specific value.");
		}

		return substr(value.valueof(), idx, returncount);
	}

	/**
	 * Return a substring value of the provided string template.
	 * <p>
	 * For more details see chapter C.4.2
	 *
	 * @param value
	 *                the input string template.
	 * @param idx
	 *                the starting index.
	 * @param returncount
	 *                the number of characters to return.
	 * @return the resulting string.
	 * */
	public static TitanBitString substr(final TitanBitString_template value, final TitanInteger idx, final TitanInteger returncount) {
		if (!value.is_value()) {
			throw new TtcnError("The first argument of function substr() is a template with non-specific value.");
		}

		return substr(value.valueof(), idx, returncount);
	}

	/**
	 * Return a substring value of the provided string template.
	 * <p>
	 * For more details see chapter C.4.2
	 *
	 * @param value
	 *                the input string template.
	 * @param idx
	 *                the starting index.
	 * @param returncount
	 *                the number of characters to return.
	 * @return the resulting string.
	 * */
	public static TitanHexString substr(final TitanHexString_template value, final int idx, final int returncount) {
		if (!value.is_value()) {
			throw new TtcnError("The first argument of function substr() is a template with non-specific value.");
		}

		return substr(value.valueof(), idx, returncount);
	}

	/**
	 * Return a substring value of the provided string template.
	 * <p>
	 * For more details see chapter C.4.2
	 *
	 * @param value
	 *                the input string template.
	 * @param idx
	 *                the starting index.
	 * @param returncount
	 *                the number of characters to return.
	 * @return the resulting string.
	 * */
	public static TitanHexString substr(final TitanHexString_template value, final int idx, final TitanInteger returncount) {
		if (!value.is_value()) {
			throw new TtcnError("The first argument of function substr() is a template with non-specific value.");
		}

		return substr(value.valueof(), idx, returncount);
	}

	/**
	 * Return a substring value of the provided string template.
	 * <p>
	 * For more details see chapter C.4.2
	 *
	 * @param value
	 *                the input string template.
	 * @param idx
	 *                the starting index.
	 * @param returncount
	 *                the number of characters to return.
	 * @return the resulting string.
	 * */
	public static TitanHexString substr(final TitanHexString_template value, final TitanInteger idx, final int returncount) {
		if (!value.is_value()) {
			throw new TtcnError("The first argument of function substr() is a template with non-specific value.");
		}

		return substr(value.valueof(), idx, returncount);
	}

	/**
	 * Return a substring value of the provided string template.
	 * <p>
	 * For more details see chapter C.4.2
	 *
	 * @param value
	 *                the input string template.
	 * @param idx
	 *                the starting index.
	 * @param returncount
	 *                the number of characters to return.
	 * @return the resulting string.
	 * */
	public static TitanHexString substr(final TitanHexString_template value, final TitanInteger idx, final TitanInteger returncount) {
		if(!value.is_value()) {
			throw new TtcnError("The first argument of function substr() is a template with non-specific value.");
		}

		return substr(value.valueof(), idx, returncount);
	}

	/**
	 * Return a substring value of the provided string template.
	 * <p>
	 * For more details see chapter C.4.2
	 *
	 * @param value
	 *                the input string template.
	 * @param idx
	 *                the starting index.
	 * @param returncount
	 *                the number of characters to return.
	 * @return the resulting string.
	 * */
	public static TitanOctetString substr(final TitanOctetString_template value, final int idx, final int returncount) {
		if (!value.is_value()) {
			throw new TtcnError("The first argument of function substr() is a template with non-specific value.");
		}

		return substr(value.valueof(), idx, returncount);
	}

	/**
	 * Return a substring value of the provided string template.
	 * <p>
	 * For more details see chapter C.4.2
	 *
	 * @param value
	 *                the input string template.
	 * @param idx
	 *                the starting index.
	 * @param returncount
	 *                the number of characters to return.
	 * @return the resulting string.
	 * */
	public static TitanOctetString substr(final TitanOctetString_template value, final int idx, final TitanInteger returncount) {
		if (!value.is_value()) {
			throw new TtcnError("The first argument of function substr() is a template with non-specific value.");
		}

		return substr(value.valueof(), idx, returncount);
	}

	/**
	 * Return a substring value of the provided string template.
	 * <p>
	 * For more details see chapter C.4.2
	 *
	 * @param value
	 *                the input string template.
	 * @param idx
	 *                the starting index.
	 * @param returncount
	 *                the number of characters to return.
	 * @return the resulting string.
	 * */
	public static TitanOctetString substr(final TitanOctetString_template value, final TitanInteger idx, final int returncount) {
		if (!value.is_value()) {
			throw new TtcnError("The first argument of function substr() is a template with non-specific value.");
		}

		return substr(value.valueof(), idx, returncount);
	}

	/**
	 * Return a substring value of the provided string template.
	 * <p>
	 * For more details see chapter C.4.2
	 *
	 * @param value
	 *                the input string template.
	 * @param idx
	 *                the starting index.
	 * @param returncount
	 *                the number of characters to return.
	 * @return the resulting string.
	 * */
	public static TitanOctetString substr(final TitanOctetString_template value, final TitanInteger idx, final TitanInteger returncount) {
		if (!value.is_value()) {
			throw new TtcnError("The first argument of function substr() is a template with non-specific value.");
		}

		return substr(value.valueof(), idx, returncount);
	}

	/**
	 * Return a substring value of the provided string template.
	 * <p>
	 * For more details see chapter C.4.2
	 *
	 * @param value
	 *                the input string template.
	 * @param idx
	 *                the starting index.
	 * @param returncount
	 *                the number of characters to return.
	 * @return the resulting string.
	 * */
	public static TitanCharString substr(final TitanCharString_template value, final int idx, final int returncount) {
		if (!value.is_value()) {
			throw new TtcnError("The first argument of function substr() is a template with non-specific value.");
		}

		return substr(value.valueof(), idx, returncount);
	}

	/**
	 * Return a substring value of the provided string template.
	 * <p>
	 * For more details see chapter C.4.2
	 *
	 * @param value
	 *                the input string template.
	 * @param idx
	 *                the starting index.
	 * @param returncount
	 *                the number of characters to return.
	 * @return the resulting string.
	 * */
	public static TitanCharString substr(final TitanCharString_template value, final int idx, final TitanInteger returncount) {
		if (!value.is_value()) {
			throw new TtcnError("The first argument of function substr() is a template with non-specific value.");
		}

		return substr(value.valueof(), idx, returncount);
	}

	/**
	 * Return a substring value of the provided string template.
	 * <p>
	 * For more details see chapter C.4.2
	 *
	 * @param value
	 *                the input string template.
	 * @param idx
	 *                the starting index.
	 * @param returncount
	 *                the number of characters to return.
	 * @return the resulting string.
	 * */
	public static TitanCharString substr(final TitanCharString_template value, final TitanInteger idx, final int returncount) {
		if (!value.is_value()) {
			throw new TtcnError("The first argument of function substr() is a template with non-specific value.");
		}

		return substr(value.valueof(), idx, returncount);
	}

	/**
	 * Return a substring value of the provided string template.
	 * <p>
	 * For more details see chapter C.4.2
	 *
	 * @param value
	 *                the input string template.
	 * @param idx
	 *                the starting index.
	 * @param returncount
	 *                the number of characters to return.
	 * @return the resulting string.
	 * */
	public static TitanCharString substr(final TitanCharString_template value, final TitanInteger idx, final TitanInteger returncount) {
		if (!value.is_value()) {
			throw new TtcnError("The first argument of function substr() is a template with non-specific value.");
		}

		return substr(value.valueof(), idx, returncount);
	}

	/**
	 * Return a substring value of the provided string template.
	 * <p>
	 * For more details see chapter C.4.2
	 *
	 * @param value
	 *                the input string template.
	 * @param idx
	 *                the starting index.
	 * @param returncount
	 *                the number of characters to return.
	 * @return the resulting string.
	 * */
	public static TitanUniversalCharString substr(final TitanUniversalCharString_template value, final int idx, final int returncount) {
		if (!value.is_value()) {
			throw new TtcnError("The first argument of function substr() is a template with non-specific value.");
		}

		return substr(value.valueof(), idx, returncount);
	}

	/**
	 * Return a substring value of the provided string template.
	 * <p>
	 * For more details see chapter C.4.2
	 *
	 * @param value
	 *                the input string template.
	 * @param idx
	 *                the starting index.
	 * @param returncount
	 *                the number of characters to return.
	 * @return the resulting string.
	 * */
	public static TitanUniversalCharString substr(final TitanUniversalCharString_template value, final int idx, final TitanInteger returncount) {
		if (!value.is_value()) {
			throw new TtcnError("The first argument of function substr() is a template with non-specific value.");
		}

		return substr(value.valueof(), idx, returncount);
	}

	/**
	 * Return a substring value of the provided string template.
	 * <p>
	 * For more details see chapter C.4.2
	 *
	 * @param value
	 *                the input string template.
	 * @param idx
	 *                the starting index.
	 * @param returncount
	 *                the number of characters to return.
	 * @return the resulting string.
	 * */
	public static TitanUniversalCharString substr(final TitanUniversalCharString_template value, final TitanInteger idx, final int returncount) {
		if (!value.is_value()) {
			throw new TtcnError("The first argument of function substr() is a template with non-specific value.");
		}

		return substr(value.valueof(), idx, returncount);
	}

	/**
	 * Return a substring value of the provided string template.
	 * <p>
	 * For more details see chapter C.4.2
	 *
	 * @param value
	 *                the input string template.
	 * @param idx
	 *                the starting index.
	 * @param returncount
	 *                the number of characters to return.
	 * @return the resulting string.
	 * */
	public static TitanUniversalCharString substr(final TitanUniversalCharString_template value, final TitanInteger idx, final TitanInteger returncount) {
		if (!value.is_value()) {
			throw new TtcnError("The first argument of function substr() is a template with non-specific value.");
		}

		return substr(value.valueof(), idx, returncount);
	}

	// C.35 - replace

	/**
	 * Check the arguments of the replace functions.
	 * <p>
	 * The index must not be negative or larger then the length.
	 * Length count must not be negative or larger then the length.
	 * The index and length must not be larger then the length.
	 *
	 * @param value_length
	 *                the current value length.
	 * @param idx
	 *                the starting index.
	 * @param len
	 *                the length to be returned.
	 * @param string_type
	 *                the type of the string to be used in error messages.
	 * @param element_name
	 *                the name of the string element to be used in error
	 *                messages.
	 * */
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

	/**
	 * Replaces the substring of value, starting at idx of length len, with
	 * the string provided in repl and returns the resulting string.
	 * <p>
	 * For more details see chapter C.4.3
	 *
	 * @param value
	 *                the input string.
	 * @param idx
	 *                the starting index.
	 * @param len
	 *                the number of characters to replace.
	 * @param repl
	 *                the string to replace with.
	 * @return the resulting string.
	 * */
	public static TitanBitString replace(final TitanBitString value, final int idx, final int len, final TitanBitString repl) {
		value.must_bound("The first argument (value) of function replace() is an unbound bitstring value.");
		repl.must_bound("The fourth argument (repl) of function replace() is an unbound bitstring value.");

		final int value_len = value.lengthof().get_int();

		check_replace_arguments(value_len, idx, len, "bitstring", "bit");

		final int repl_len = repl.lengthof().get_int();
		final StringBuilder temp_sb = new StringBuilder(value_len);

		for (int i = 0; i < idx; i++) {
			temp_sb.append(value.get_bit(i) ? '1' : '0');
		}
		for (int i = 0; i < repl_len; i++) {
			temp_sb.append(repl.get_bit(i) ? '1' : '0');
		}
		for (int i = 0; i < value_len - idx - len; i++) {
			temp_sb.append(value.get_bit(idx + len + i) ? '1' : '0');
		}
		return new TitanBitString(temp_sb.toString());
	}

	/**
	 * Replaces the substring of value, starting at idx of length len, with
	 * the string provided in repl and returns the resulting string.
	 * <p>
	 * For more details see chapter C.4.3
	 *
	 * @param value
	 *                the input string.
	 * @param idx
	 *                the starting index.
	 * @param len
	 *                the number of characters to replace.
	 * @param repl
	 *                the string to replace with.
	 * @return the resulting string.
	 * */
	public static TitanBitString replace(final TitanBitString value, final int idx, final TitanInteger len, final TitanBitString repl) {
		len.must_bound("The third argument (len) of function replace() is an unbound integer value.");

		return replace(value, idx, len.get_int(), repl);
	}

	/**
	 * Replaces the substring of value, starting at idx of length len, with
	 * the string provided in repl and returns the resulting string.
	 * <p>
	 * For more details see chapter C.4.3
	 *
	 * @param value
	 *                the input string.
	 * @param idx
	 *                the starting index.
	 * @param len
	 *                the number of characters to replace.
	 * @param repl
	 *                the string to replace with.
	 * @return the resulting string.
	 * */
	public static TitanBitString replace(final TitanBitString value, final TitanInteger idx, final int len, final TitanBitString repl) {
		idx.must_bound("The second argument (index) of function replace() is an unbound integer value.");

		return replace(value, idx.get_int(), len, repl);
	}

	/**
	 * Replaces the substring of value, starting at idx of length len, with
	 * the string provided in repl and returns the resulting string.
	 * <p>
	 * For more details see chapter C.4.3
	 *
	 * @param value
	 *                the input string.
	 * @param idx
	 *                the starting index.
	 * @param len
	 *                the number of characters to replace.
	 * @param repl
	 *                the string to replace with.
	 * @return the resulting string.
	 * */
	public static TitanBitString replace(final TitanBitString value, final TitanInteger idx, final TitanInteger len, final TitanBitString repl) {
		idx.must_bound("The second argument (index) of function replace() is an unbound integer value.");
		len.must_bound("The third argument (len) of function replace() is an unbound integer value.");

		return replace(value, idx.get_int(), len.get_int(), repl);
	}

	/**
	 * Replaces the substring of value, starting at idx of length len, with
	 * the string provided in repl and returns the resulting string.
	 * <p>
	 * For more details see chapter C.4.3
	 *
	 * @param value
	 *                the input string.
	 * @param idx
	 *                the starting index.
	 * @param len
	 *                the number of characters to replace.
	 * @param repl
	 *                the string to replace with.
	 * @return the resulting string.
	 * */
	public static TitanHexString replace(final TitanHexString value, final int idx, final int len, final TitanHexString repl) {
		value.must_bound("The first argument (value) of function replace() is an unbound hexstring value.");
		repl.must_bound("The fourth argument (repl) of function replace() is an unbound hexstring value.");

		final int value_len = value.lengthof().get_int();

		check_replace_arguments(value_len, idx, len, "hexstring", "hexadecimal digit");

		final int repl_len = repl.lengthof().get_int();
		final byte src_ptr[] = value.get_value();
		final byte repl_ptr[] = repl.get_value();
		final byte ret_val[] = new byte[value_len + repl_len - len];

		System.arraycopy(src_ptr, 0, ret_val, 0, idx);
		System.arraycopy(repl_ptr, 0, ret_val, idx, repl_len);
		System.arraycopy(src_ptr, idx + len, ret_val, idx + repl_len, value_len - idx - len);

		return new TitanHexString(ret_val);
	}

	/**
	 * Replaces the substring of value, starting at idx of length len, with
	 * the string provided in repl and returns the resulting string.
	 * <p>
	 * For more details see chapter C.4.3
	 *
	 * @param value
	 *                the input string.
	 * @param idx
	 *                the starting index.
	 * @param len
	 *                the number of characters to replace.
	 * @param repl
	 *                the string to replace with.
	 * @return the resulting string.
	 * */
	public static TitanHexString replace(final TitanHexString value, final int idx, final TitanInteger len, final TitanHexString repl) {
		len.must_bound("The third argument (len) of function replace() is an unbound integer value.");

		return replace(value, idx, len.get_int(), repl);
	}

	/**
	 * Replaces the substring of value, starting at idx of length len, with
	 * the string provided in repl and returns the resulting string.
	 * <p>
	 * For more details see chapter C.4.3
	 *
	 * @param value
	 *                the input string.
	 * @param idx
	 *                the starting index.
	 * @param len
	 *                the number of characters to replace.
	 * @param repl
	 *                the string to replace with.
	 * @return the resulting string.
	 * */
	public static TitanHexString replace(final TitanHexString value, final TitanInteger idx, final int len, final TitanHexString repl) {
		idx.must_bound("The second argument (index) of function replace() is an unbound integer value.");

		return replace(value, idx.get_int(), len, repl);
	}

	/**
	 * Replaces the substring of value, starting at idx of length len, with
	 * the string provided in repl and returns the resulting string.
	 * <p>
	 * For more details see chapter C.4.3
	 *
	 * @param value
	 *                the input string.
	 * @param idx
	 *                the starting index.
	 * @param len
	 *                the number of characters to replace.
	 * @param repl
	 *                the string to replace with.
	 * @return the resulting string.
	 * */
	public static TitanHexString replace(final TitanHexString value, final TitanInteger idx, final TitanInteger len, final TitanHexString repl) {
		idx.must_bound("The second argument (index) of function replace() is an unbound integer value.");
		len.must_bound("The third argument (len) of function replace() is an unbound integer value.");

		return replace(value, idx.get_int(), len.get_int(), repl);
	}

	/**
	 * Replaces the substring of value, starting at idx of length len, with
	 * the string provided in repl and returns the resulting string.
	 * <p>
	 * For more details see chapter C.4.3
	 *
	 * @param value
	 *                the input string.
	 * @param idx
	 *                the starting index.
	 * @param len
	 *                the number of characters to replace.
	 * @param repl
	 *                the string to replace with.
	 * @return the resulting string.
	 * */
	public static TitanOctetString replace(final TitanOctetString value, final int idx, final int len, final TitanOctetString repl) {
		value.must_bound("The first argument (value) of function replace() is an unbound octetstring value.");
		repl.must_bound("The fourth argument (repl) of function replace() is an unbound octetstring value.");

		final int value_len = value.lengthof().get_int();

		check_replace_arguments(value_len, idx, len, "octetstring", "octet");

		final int repl_len = repl.lengthof().get_int();
		final byte src_ptr[] = value.get_value();
		final byte repl_ptr[] = repl.get_value();
		final byte ret_val[] = new byte[value_len + repl_len - len];

		System.arraycopy(src_ptr, 0, ret_val, 0, idx);
		System.arraycopy(repl_ptr, 0, ret_val, idx, repl_len);
		System.arraycopy(src_ptr, idx + len, ret_val, idx + repl_len, value_len - idx - len);

		return new TitanOctetString(ret_val);
	}

	/**
	 * Replaces the substring of value, starting at idx of length len, with
	 * the string provided in repl and returns the resulting string.
	 * <p>
	 * For more details see chapter C.4.3
	 *
	 * @param value
	 *                the input string.
	 * @param idx
	 *                the starting index.
	 * @param len
	 *                the number of characters to replace.
	 * @param repl
	 *                the string to replace with.
	 * @return the resulting string.
	 * */
	public static TitanOctetString replace(final TitanOctetString value, final int idx, final TitanInteger len, final TitanOctetString repl) {
		len.must_bound("The third argument (len) of function replace() is an unbound integer value.");

		return replace(value, idx, len.get_int(), repl);
	}

	/**
	 * Replaces the substring of value, starting at idx of length len, with
	 * the string provided in repl and returns the resulting string.
	 * <p>
	 * For more details see chapter C.4.3
	 *
	 * @param value
	 *                the input string.
	 * @param idx
	 *                the starting index.
	 * @param len
	 *                the number of characters to replace.
	 * @param repl
	 *                the string to replace with.
	 * @return the resulting string.
	 * */
	public static TitanOctetString replace(final TitanOctetString value, final TitanInteger idx, final int len, final TitanOctetString repl) {
		idx.must_bound("The second argument (index) of function replace() is an unbound integer value.");

		return replace(value, idx.get_int(), len, repl);
	}

	/**
	 * Replaces the substring of value, starting at idx of length len, with
	 * the string provided in repl and returns the resulting string.
	 * <p>
	 * For more details see chapter C.4.3
	 *
	 * @param value
	 *                the input string.
	 * @param idx
	 *                the starting index.
	 * @param len
	 *                the number of characters to replace.
	 * @param repl
	 *                the string to replace with.
	 * @return the resulting string.
	 * */
	public static TitanOctetString replace(final TitanOctetString value, final TitanInteger idx, final TitanInteger len, final TitanOctetString repl) {
		idx.must_bound("The second argument (index) of function replace() is an unbound integer value.");
		len.must_bound("The third argument (len) of function replace() is an unbound integer value.");

		return replace(value, idx.get_int(), len.get_int(), repl);
	}

	/**
	 * Replaces the substring of value, starting at idx of length len, with
	 * the string provided in repl and returns the resulting string.
	 * <p>
	 * For more details see chapter C.4.3
	 *
	 * @param value
	 *                the input string.
	 * @param idx
	 *                the starting index.
	 * @param len
	 *                the number of characters to replace.
	 * @param repl
	 *                the string to replace with.
	 * @return the resulting string.
	 * */
	public static TitanCharString replace(final TitanCharString value, final int idx, final int len, final TitanCharString repl) {
		value.must_bound("The first argument (value) of function replace() is an unbound charstring value.");
		repl.must_bound("The fourth argument (repl) of function replace() is an unbound charstring value.");

		final int value_len = value.lengthof().get_int();

		check_replace_arguments(value_len, idx, len, "charstring", "character");

		final StringBuilder ret_val = new StringBuilder();
		ret_val.append(value.get_value());

		ret_val.replace(idx, idx + len, repl.get_value().toString());

		return new TitanCharString(ret_val);
	}

	/**
	 * Replaces the substring of value, starting at idx of length len, with
	 * the string provided in repl and returns the resulting string.
	 * <p>
	 * For more details see chapter C.4.3
	 *
	 * @param value
	 *                the input string.
	 * @param idx
	 *                the starting index.
	 * @param len
	 *                the number of characters to replace.
	 * @param repl
	 *                the string to replace with.
	 * @return the resulting string.
	 * */
	public static TitanCharString replace(final TitanCharString value, final int idx, final TitanInteger len, final TitanCharString repl) {
		len.must_bound("The third argument (len) of function replace() is an unbound integer value.");

		return replace(value, idx, len.get_int(), repl);
	}

	/**
	 * Replaces the substring of value, starting at idx of length len, with
	 * the string provided in repl and returns the resulting string.
	 * <p>
	 * For more details see chapter C.4.3
	 *
	 * @param value
	 *                the input string.
	 * @param idx
	 *                the starting index.
	 * @param len
	 *                the number of characters to replace.
	 * @param repl
	 *                the string to replace with.
	 * @return the resulting string.
	 * */
	public static TitanCharString replace(final TitanCharString value, final TitanInteger idx, final int len, final TitanCharString repl) {
		idx.must_bound("The second argument (index) of function replace() is an unbound integer value.");

		return replace(value, idx.get_int(), len, repl);
	}

	/**
	 * Replaces the substring of value, starting at idx of length len, with
	 * the string provided in repl and returns the resulting string.
	 * <p>
	 * For more details see chapter C.4.3
	 *
	 * @param value
	 *                the input string.
	 * @param idx
	 *                the starting index.
	 * @param len
	 *                the number of characters to replace.
	 * @param repl
	 *                the string to replace with.
	 * @return the resulting string.
	 * */
	public static TitanCharString replace(final TitanCharString value, final TitanInteger idx, final TitanInteger len, final TitanCharString repl) {
		idx.must_bound("The second argument (index) of function replace() is an unbound integer value.");
		len.must_bound("The third argument (len) of function replace() is an unbound integer value.");

		return replace(value, idx.get_int(), len.get_int(), repl);
	}

	/**
	 * Replaces the substring of value, starting at idx of length len, with
	 * the string provided in repl and returns the resulting string.
	 * <p>
	 * For more details see chapter C.4.3
	 *
	 * @param value
	 *                the input string.
	 * @param idx
	 *                the starting index.
	 * @param len
	 *                the number of characters to replace.
	 * @param repl
	 *                the string to replace with.
	 * @return the resulting string.
	 * */
	public static TitanUniversalCharString replace(final TitanUniversalCharString value, final int idx, final int len, final TitanUniversalCharString repl) {
		value.must_bound("The first argument (value) of function replace() is an unbound universal charstring value.");
		repl.must_bound("The fourth argument (repl) of function replace() is an unbound universal charstring value.");

		final int value_len = value.lengthof().get_int();

		check_replace_arguments(value_len, idx, len, "universal charstring", "character");

		final int repl_len = repl.lengthof().get_int();

		if (value.charstring) {
			if (repl.charstring) {
				final StringBuilder ret_val = new StringBuilder();
				ret_val.append(value.cstr.toString());
				ret_val.replace(idx, idx + len, repl.cstr.toString());
				return new TitanUniversalCharString(ret_val);
			} else {
				final int newSize = repl_len + value_len - len;
				final List<TitanUniversalChar> ret_val = new ArrayList<TitanUniversalChar>(newSize);
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
				final int newSize = repl_len + value_len - len;
				final List<TitanUniversalChar> ret_val = new ArrayList<TitanUniversalChar>(newSize);
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
				final int newSize = repl_len + value_len - len;
				final List<TitanUniversalChar> ret_val = new ArrayList<TitanUniversalChar>(newSize);
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

	/**
	 * Replaces the substring of value, starting at idx of length len, with
	 * the string provided in repl and returns the resulting string.
	 * <p>
	 * For more details see chapter C.4.3
	 *
	 * @param value
	 *                the input string.
	 * @param idx
	 *                the starting index.
	 * @param len
	 *                the number of characters to replace.
	 * @param repl
	 *                the string to replace with.
	 * @return the resulting string.
	 * */
	public static TitanUniversalCharString replace(final TitanUniversalCharString value, final int idx, final TitanInteger len, final TitanUniversalCharString repl) {
		len.must_bound("The third argument (len) of function replace() is an unbound integer value.");

		return replace(value, idx, len.get_int(), repl);
	}

	/**
	 * Replaces the substring of value, starting at idx of length len, with
	 * the string provided in repl and returns the resulting string.
	 * <p>
	 * For more details see chapter C.4.3
	 *
	 * @param value
	 *                the input string.
	 * @param idx
	 *                the starting index.
	 * @param len
	 *                the number of characters to replace.
	 * @param repl
	 *                the string to replace with.
	 * @return the resulting string.
	 * */
	public static TitanUniversalCharString replace(final TitanUniversalCharString value, final TitanInteger idx, final int len, final TitanUniversalCharString repl) {
		idx.must_bound("The second argument (index) of function replace() is an unbound integer value.");

		return replace(value, idx.get_int(), len, repl);
	}

	/**
	 * Replaces the substring of value, starting at idx of length len, with
	 * the string provided in repl and returns the resulting string.
	 * <p>
	 * For more details see chapter C.4.3
	 *
	 * @param value
	 *                the input string.
	 * @param idx
	 *                the starting index.
	 * @param len
	 *                the number of characters to replace.
	 * @param repl
	 *                the string to replace with.
	 * @return the resulting string.
	 * */
	public static TitanUniversalCharString replace(final TitanUniversalCharString value, final TitanInteger idx, final TitanInteger len, final TitanUniversalCharString repl) {
		idx.must_bound("The second argument (index) of function replace() is an unbound integer value.");
		len.must_bound("The third argument (len) of function replace() is an unbound integer value.");

		return replace(value, idx.get_int(), len.get_int(), repl);
	}

	// replace on templates

	/**
	 * Replaces the substring of value template, starting at idx of length len, with
	 * the string provided in repl and returns the resulting string.
	 * <p>
	 * For more details see chapter C.4.3
	 *
	 * @param value
	 *                the input string.
	 * @param idx
	 *                the starting index.
	 * @param len
	 *                the number of characters to replace.
	 * @param repl
	 *                the string to replace with.
	 * @return the resulting string.
	 * */
	public static TitanBitString replace(final TitanBitString_template value, final int idx, final int len, final TitanBitString_template repl) {
		if (!value.is_value()) {
			throw new TtcnError("The first argument of function replace() is a template with non-specific value.");
		}
		if (!repl.is_value()) {
			throw new TtcnError("The fourth argument of function replace() is a template with non-specific value.");
		}

		return replace(value.valueof(), idx, len, repl.valueof());
	}

	/**
	 * Replaces the substring of value template, starting at idx of length len, with
	 * the string provided in repl and returns the resulting string.
	 * <p>
	 * For more details see chapter C.4.3
	 *
	 * @param value
	 *                the input string.
	 * @param idx
	 *                the starting index.
	 * @param len
	 *                the number of characters to replace.
	 * @param repl
	 *                the string to replace with.
	 * @return the resulting string.
	 * */
	public static TitanBitString replace(final TitanBitString_template value, final int idx, final TitanInteger len, final TitanBitString_template repl) {
		if (!value.is_value()) {
			throw new TtcnError("The first argument of function replace() is a template with non-specific value.");
		}
		if (!repl.is_value()) {
			throw new TtcnError("The fourth argument of function replace() is a template with non-specific value.");
		}

		return replace(value.valueof(), idx, len, repl.valueof());
	}

	/**
	 * Replaces the substring of value template, starting at idx of length len, with
	 * the string provided in repl and returns the resulting string.
	 * <p>
	 * For more details see chapter C.4.3
	 *
	 * @param value
	 *                the input string.
	 * @param idx
	 *                the starting index.
	 * @param len
	 *                the number of characters to replace.
	 * @param repl
	 *                the string to replace with.
	 * @return the resulting string.
	 * */
	public static TitanBitString replace(final TitanBitString_template value, final TitanInteger idx, final int len, final TitanBitString_template repl) {
		if (!value.is_value()) {
			throw new TtcnError("The first argument of function replace() is a template with non-specific value.");
		}
		if (!repl.is_value()) {
			throw new TtcnError("The fourth argument of function replace() is a template with non-specific value.");
		}

		return replace(value.valueof(), idx, len, repl.valueof());
	}

	/**
	 * Replaces the substring of value template, starting at idx of length len, with
	 * the string provided in repl and returns the resulting string.
	 * <p>
	 * For more details see chapter C.4.3
	 *
	 * @param value
	 *                the input string.
	 * @param idx
	 *                the starting index.
	 * @param len
	 *                the number of characters to replace.
	 * @param repl
	 *                the string to replace with.
	 * @return the resulting string.
	 * */
	public static TitanBitString replace(final TitanBitString_template value, final TitanInteger idx, final TitanInteger len, final TitanBitString_template repl) {
		if (!value.is_value()) {
			throw new TtcnError("The first argument of function replace() is a template with non-specific value.");
		}
		if (!repl.is_value()) {
			throw new TtcnError("The fourth argument of function replace() is a template with non-specific value.");
		}

		return replace(value.valueof(), idx, len, repl.valueof());
	}

	/**
	 * Replaces the substring of value template, starting at idx of length len, with
	 * the string provided in repl and returns the resulting string.
	 * <p>
	 * For more details see chapter C.4.3
	 *
	 * @param value
	 *                the input string.
	 * @param idx
	 *                the starting index.
	 * @param len
	 *                the number of characters to replace.
	 * @param repl
	 *                the string to replace with.
	 * @return the resulting string.
	 * */
	public static TitanHexString replace(final TitanHexString_template value, final int idx, final int len, final TitanHexString_template repl) {
		if (!value.is_value()) {
			throw new TtcnError("The first argument of function replace() is a template with non-specific value.");
		}
		if (!repl.is_value()) {
			throw new TtcnError("The fourth argument of function replace() is a template with non-specific value.");
		}

		return replace(value.valueof(), idx, len, repl.valueof());
	}

	/**
	 * Replaces the substring of value template, starting at idx of length len, with
	 * the string provided in repl and returns the resulting string.
	 * <p>
	 * For more details see chapter C.4.3
	 *
	 * @param value
	 *                the input string.
	 * @param idx
	 *                the starting index.
	 * @param len
	 *                the number of characters to replace.
	 * @param repl
	 *                the string to replace with.
	 * @return the resulting string.
	 * */
	public static TitanHexString replace(final TitanHexString_template value, final int idx, final TitanInteger len, final TitanHexString_template repl) {
		if (!value.is_value()) {
			throw new TtcnError("The first argument of function replace() is a template with non-specific value.");
		}
		if (!repl.is_value()) {
			throw new TtcnError("The fourth argument of function replace() is a template with non-specific value.");
		}

		return replace(value.valueof(), idx, len, repl.valueof());
	}

	/**
	 * Replaces the substring of value template, starting at idx of length len, with
	 * the string provided in repl and returns the resulting string.
	 * <p>
	 * For more details see chapter C.4.3
	 *
	 * @param value
	 *                the input string.
	 * @param idx
	 *                the starting index.
	 * @param len
	 *                the number of characters to replace.
	 * @param repl
	 *                the string to replace with.
	 * @return the resulting string.
	 * */
	public static TitanHexString replace(final TitanHexString_template value, final TitanInteger idx, final int len, final TitanHexString_template repl) {
		if (!value.is_value()) {
			throw new TtcnError("The first argument of function replace() is a template with non-specific value.");
		}
		if (!repl.is_value()) {
			throw new TtcnError("The fourth argument of function replace() is a template with non-specific value.");
		}

		return replace(value.valueof(), idx, len, repl.valueof());
	}

	/**
	 * Replaces the substring of value template, starting at idx of length len, with
	 * the string provided in repl and returns the resulting string.
	 * <p>
	 * For more details see chapter C.4.3
	 *
	 * @param value
	 *                the input string.
	 * @param idx
	 *                the starting index.
	 * @param len
	 *                the number of characters to replace.
	 * @param repl
	 *                the string to replace with.
	 * @return the resulting string.
	 * */
	public static TitanHexString replace(final TitanHexString_template value, final TitanInteger idx, final TitanInteger len,
			final TitanHexString_template repl) {
		if (!value.is_value()) {
			throw new TtcnError("The first argument of function replace() is a template with non-specific value.");
		}
		if (!repl.is_value()) {
			throw new TtcnError("The fourth argument of function replace() is a template with non-specific value.");
		}

		return replace(value.valueof(), idx, len, repl.valueof());
	}

	/**
	 * Replaces the substring of value template, starting at idx of length len, with
	 * the string provided in repl and returns the resulting string.
	 * <p>
	 * For more details see chapter C.4.3
	 *
	 * @param value
	 *                the input string.
	 * @param idx
	 *                the starting index.
	 * @param len
	 *                the number of characters to replace.
	 * @param repl
	 *                the string to replace with.
	 * @return the resulting string.
	 * */
	public static TitanOctetString replace(final TitanOctetString_template value, final int idx, final int len, final TitanOctetString_template repl) {
		if (!value.is_value()) {
			throw new TtcnError("The first argument of function replace() is a template with non-specific value.");
		}
		if (!repl.is_value()) {
			throw new TtcnError("The fourth argument of function replace() is a template with non-specific value.");
		}

		return replace(value.valueof(), idx, len, repl.valueof());
	}

	/**
	 * Replaces the substring of value template, starting at idx of length len, with
	 * the string provided in repl and returns the resulting string.
	 * <p>
	 * For more details see chapter C.4.3
	 *
	 * @param value
	 *                the input string.
	 * @param idx
	 *                the starting index.
	 * @param len
	 *                the number of characters to replace.
	 * @param repl
	 *                the string to replace with.
	 * @return the resulting string.
	 * */
	public static TitanOctetString replace(final TitanOctetString_template value, final int idx, final TitanInteger len, final TitanOctetString_template repl) {
		if (!value.is_value()) {
			throw new TtcnError("The first argument of function replace() is a template with non-specific value.");
		}
		if (!repl.is_value()) {
			throw new TtcnError("The fourth argument of function replace() is a template with non-specific value.");
		}

		return replace(value.valueof(), idx, len, repl.valueof());
	}

	/**
	 * Replaces the substring of value template, starting at idx of length len, with
	 * the string provided in repl and returns the resulting string.
	 * <p>
	 * For more details see chapter C.4.3
	 *
	 * @param value
	 *                the input string.
	 * @param idx
	 *                the starting index.
	 * @param len
	 *                the number of characters to replace.
	 * @param repl
	 *                the string to replace with.
	 * @return the resulting string.
	 * */
	public static TitanOctetString replace(final TitanOctetString_template value, final TitanInteger idx, final int len, final TitanOctetString_template repl) {
		if (!value.is_value()) {
			throw new TtcnError("The first argument of function replace() is a template with non-specific value.");
		}
		if (!repl.is_value()) {
			throw new TtcnError("The fourth argument of function replace() is a template with non-specific value.");
		}

		return replace(value.valueof(), idx, len, repl.valueof());
	}

	/**
	 * Replaces the substring of value template, starting at idx of length len, with
	 * the string provided in repl and returns the resulting string.
	 * <p>
	 * For more details see chapter C.4.3
	 *
	 * @param value
	 *                the input string.
	 * @param idx
	 *                the starting index.
	 * @param len
	 *                the number of characters to replace.
	 * @param repl
	 *                the string to replace with.
	 * @return the resulting string.
	 * */
	public static TitanOctetString replace(final TitanOctetString_template value, final TitanInteger idx, final TitanInteger len, final TitanOctetString_template repl) {
		if (!value.is_value()) {
			throw new TtcnError("The first argument of function replace() is a template with non-specific value.");
		}
		if (!repl.is_value()) {
			throw new TtcnError("The fourth argument of function replace() is a template with non-specific value.");
		}

		return replace(value.valueof(), idx, len, repl.valueof());
	}

	/**
	 * Replaces the substring of value template, starting at idx of length len, with
	 * the string provided in repl and returns the resulting string.
	 * <p>
	 * For more details see chapter C.4.3
	 *
	 * @param value
	 *                the input string.
	 * @param idx
	 *                the starting index.
	 * @param len
	 *                the number of characters to replace.
	 * @param repl
	 *                the string to replace with.
	 * @return the resulting string.
	 * */
	public static TitanCharString replace(final TitanCharString_template value, final int idx, final int len, final TitanCharString_template repl) {
		if (!value.is_value()) {
			throw new TtcnError("The first argument of function replace() is a template with non-specific value.");
		}
		if (!repl.is_value()) {
			throw new TtcnError("The fourth argument of function replace() is a template with non-specific value.");
		}

		return replace(value.valueof(), idx, len, repl.valueof());
	}

	/**
	 * Replaces the substring of value template, starting at idx of length len, with
	 * the string provided in repl and returns the resulting string.
	 * <p>
	 * For more details see chapter C.4.3
	 *
	 * @param value
	 *                the input string.
	 * @param idx
	 *                the starting index.
	 * @param len
	 *                the number of characters to replace.
	 * @param repl
	 *                the string to replace with.
	 * @return the resulting string.
	 * */
	public static TitanCharString replace(final TitanCharString_template value, final int idx, final TitanInteger len, final TitanCharString_template repl) {
		if (!value.is_value()) {
			throw new TtcnError("The first argument of function replace() is a template with non-specific value.");
		}
		if (!repl.is_value()) {
			throw new TtcnError("The fourth argument of function replace() is a template with non-specific value.");
		}

		return replace(value.valueof(), idx, len, repl.valueof());
	}

	/**
	 * Replaces the substring of value template, starting at idx of length len, with
	 * the string provided in repl and returns the resulting string.
	 * <p>
	 * For more details see chapter C.4.3
	 *
	 * @param value
	 *                the input string.
	 * @param idx
	 *                the starting index.
	 * @param len
	 *                the number of characters to replace.
	 * @param repl
	 *                the string to replace with.
	 * @return the resulting string.
	 * */
	public static TitanCharString replace(final TitanCharString_template value, final TitanInteger idx, final int len, final TitanCharString_template repl) {
		if (!value.is_value()) {
			throw new TtcnError("The first argument of function replace() is a template with non-specific value.");
		}
		if (!repl.is_value()) {
			throw new TtcnError("The fourth argument of function replace() is a template with non-specific value.");
		}

		return replace(value.valueof(), idx, len, repl.valueof());
	}

	/**
	 * Replaces the substring of value template, starting at idx of length len, with
	 * the string provided in repl and returns the resulting string.
	 * <p>
	 * For more details see chapter C.4.3
	 *
	 * @param value
	 *                the input string.
	 * @param idx
	 *                the starting index.
	 * @param len
	 *                the number of characters to replace.
	 * @param repl
	 *                the string to replace with.
	 * @return the resulting string.
	 * */
	public static TitanCharString replace(final TitanCharString_template value, final TitanInteger idx, final TitanInteger len, final TitanCharString_template repl) {
		if (!value.is_value()) {
			throw new TtcnError("The first argument of function replace() is a template with non-specific value.");
		}
		if (!repl.is_value()) {
			throw new TtcnError("The fourth argument of function replace() is a template with non-specific value.");
		}

		return replace(value.valueof(), idx, len, repl.valueof());
	}

	/**
	 * Replaces the substring of value template, starting at idx of length len, with
	 * the string provided in repl and returns the resulting string.
	 * <p>
	 * For more details see chapter C.4.3
	 *
	 * @param value
	 *                the input string.
	 * @param idx
	 *                the starting index.
	 * @param len
	 *                the number of characters to replace.
	 * @param repl
	 *                the string to replace with.
	 * @return the resulting string.
	 * */
	public static TitanUniversalCharString replace(final TitanUniversalCharString_template value, final int idx, final int len, final TitanUniversalCharString_template repl) {
		if (!value.is_value()) {
			throw new TtcnError("The first argument of function replace() is a template with non-specific value.");
		}
		if (!repl.is_value()) {
			throw new TtcnError("The fourth argument of function replace() is a template with non-specific value.");
		}

		return replace(value.valueof(), idx, len, repl.valueof());
	}

	/**
	 * Replaces the substring of value template, starting at idx of length len, with
	 * the string provided in repl and returns the resulting string.
	 * <p>
	 * For more details see chapter C.4.3
	 *
	 * @param value
	 *                the input string.
	 * @param idx
	 *                the starting index.
	 * @param len
	 *                the number of characters to replace.
	 * @param repl
	 *                the string to replace with.
	 * @return the resulting string.
	 * */
	public static TitanUniversalCharString replace(final TitanUniversalCharString_template value, final int idx, final TitanInteger len, final TitanUniversalCharString_template repl) {
		if (!value.is_value()) {
			throw new TtcnError("The first argument of function replace() is a template with non-specific value.");
		}
		if (!repl.is_value()) {
			throw new TtcnError("The fourth argument of function replace() is a template with non-specific value.");
		}

		return replace(value.valueof(), idx, len, repl.valueof());
	}

	/**
	 * Replaces the substring of value template, starting at idx of length len, with
	 * the string provided in repl and returns the resulting string.
	 * <p>
	 * For more details see chapter C.4.3
	 *
	 * @param value
	 *                the input string.
	 * @param idx
	 *                the starting index.
	 * @param len
	 *                the number of characters to replace.
	 * @param repl
	 *                the string to replace with.
	 * @return the resulting string.
	 * */
	public static TitanUniversalCharString replace(final TitanUniversalCharString_template value, final TitanInteger idx, final int len, final TitanUniversalCharString_template repl) {
		if (!value.is_value()) {
			throw new TtcnError("The first argument of function replace() is a template with non-specific value.");
		}
		if (!repl.is_value()) {
			throw new TtcnError("The fourth argument of function replace() is a template with non-specific value.");
		}

		return replace(value.valueof(), idx, len, repl.valueof());
	}

	/**
	 * Replaces the substring of value template, starting at idx of length len, with
	 * the string provided in repl and returns the resulting string.
	 * <p>
	 * For more details see chapter C.4.3
	 *
	 * @param value
	 *                the input string.
	 * @param idx
	 *                the starting index.
	 * @param len
	 *                the number of characters to replace.
	 * @param repl
	 *                the string to replace with.
	 * @return the resulting string.
	 * */
	public static TitanUniversalCharString replace(final TitanUniversalCharString_template value, final TitanInteger idx, final TitanInteger len, final TitanUniversalCharString_template repl) {
		if (!value.is_value()) {
			throw new TtcnError("The first argument of function replace() is a template with non-specific value.");
		}
		if (!repl.is_value()) {
			throw new TtcnError("The fourth argument of function replace() is a template with non-specific value.");
		}

		return replace(value.valueof(), idx, len, repl.valueof());
	}

	/**
	 * Detects and returns the UCS encoding scheme
	 *  according to clause 10 of ISO/IEC 10646 or
	 *  or {@code "<unknown>"} if not found.
	 * <p>
	 * For more details see chapter C.5.7
	 *
	 * @param encoded_value
	 *                the input string.
	 * @return the detected encoding or {@code "<unknown>"}.
	 * */
	public static TitanCharString get_stringencoding(final TitanOctetString encoded_value) {
		if (encoded_value.lengthof().operator_equals(0)) {
			return new TitanCharString("<unknown>");
		}

		int i = 0;
		final byte[] strptr = encoded_value.get_value();
		final int length = strptr.length;

		if(length >= 2) {
			switch (strptr[0] & 0xFF) {
			case 0xef:
				for (i = 1; i < UTF8_BOM.length && i < strptr.length && UTF8_BOM[i] == (strptr[i] & 0xFF); i++) {}
				if (i == UTF8_BOM.length && UTF8_BOM.length <= length) {
					return new TitanCharString("UTF-8");
				}
				break;
			case 0xfe:
				for (i = 1; i < UTF16BE_BOM.length && i < strptr.length && UTF16BE_BOM[i] == (strptr[i] & 0xFF); i++) {}
				if (i == UTF16BE_BOM.length && UTF16BE_BOM.length <= length) {
					return new TitanCharString("UTF-16BE");
				}
				break;
			case 0xff:
				for (i = 1; i < UTF32LE_BOM.length && i < strptr.length && UTF32LE_BOM[i] == (strptr[i] & 0xFF); i++) {}
				if (i == UTF32LE_BOM.length && UTF32LE_BOM.length <= length) {
					return new TitanCharString("UTF-32LE");
				}

				for (i = 1; i < UTF16LE_BOM.length && i < strptr.length && UTF16LE_BOM[i] == (strptr[i] & 0xFF); i++) {}
				if (i == UTF16LE_BOM.length && UTF16LE_BOM.length <= length) {
					return new TitanCharString("UTF-16LE");
				}
				break;
			case 0x00:
				for (i = 1; i < UTF32BE_BOM.length && i < strptr.length && UTF32BE_BOM[i] == (strptr[i] & 0xFF); i++) {}
				if (i == UTF32BE_BOM.length && UTF32BE_BOM.length <= length) {
					return new TitanCharString("UTF-32BE");
				}
				break;
			default:
				break;
			}
		}

		if (is_ascii(encoded_value) == CharCoding.ASCII) {
			return new TitanCharString("ASCII");
		} else if (is_utf8(encoded_value) == CharCoding.UTF_8) {
			return new TitanCharString("UTF-8");
		} else {
			return new TitanCharString("<unknown>");
		}
	}

	/**
	 * Removes the optional sequence of characters that might be present at
	 * the beginning of a stream of encoded universal charstring to indicate
	 * the order of octets in the encoded form.
	 * <p>
	 * For more details see chapter C.5.8
	 *
	 * @param encoded_value
	 *                the input string.
	 * @return the resulting octetstring without the optional starting
	 *         sequence.
	 * */
	public static TitanOctetString remove_bom(final TitanOctetString encoded_value) {
		final byte str[] = encoded_value.get_value();
		int length_of_BOM = 0;
		//TODO maybe some common checks could be extracted
		if (0x00 == (str[0] & 0xFF) && 0x00 == (str[1] & 0xFF) && 0xFE == (str[2] & 0xFF) && 0xFF == (str[3] & 0xFF)) { // UTF-32BE
			length_of_BOM = 4;
		} else if (0xFF == (str[0] & 0xFF) && 0xFE == (str[1] & 0xFF) && 0x00 == (str[2] & 0xFF) && 0x00 == (str[3] & 0xFF)) { // UTF-32LE
			length_of_BOM = 4;
		} else if (0xFE == (str[0] & 0xFF) && 0xFF == (str[1] & 0xFF)) { // UTF-16BE
			length_of_BOM = 2;
		} else if (0xFF == (str[0] & 0xFF) && 0xFE == (str[1] & 0xFF)) { // UTF-16LE
			length_of_BOM = 2;
		} else if (0xEF == (str[0] & 0xFF) && 0xBB == (str[1] & 0xFF) && 0xBF == (str[2] & 0xFF)) { // UTF-8
			length_of_BOM = 3;
		} else {
			return new TitanOctetString(encoded_value);
		}

		final byte tmp_str[] = new byte[str.length - length_of_BOM];
		System.arraycopy(str, length_of_BOM, tmp_str, 0, str.length - length_of_BOM);

		return new TitanOctetString(tmp_str);
	}

	//C.36 - rnd

	//TODO update with Java 1.7 to ThreadLocalRandom
	private static boolean rndSeedSet = false;
	private final static Random random = new Random();

	public static void set_rnd_seed(final double floatSeed) {
		TitanFloat.check_numeric(floatSeed,"The seed value of function rnd()");
		random.setSeed((long)floatSeed);
		TTCN_Logger.log_random(TitanLoggerApi.RandomAction.enum_type.seed, floatSeed, (long)floatSeed);
		rndSeedSet = true;
	}

	public static TitanFloat rnd_generate() {
		final double returnValue = random.nextDouble();
		TTCN_Logger.log_random(TitanLoggerApi.RandomAction.enum_type.read__out, returnValue, 0);

		return new TitanFloat(returnValue);
	}

	/**
	 * Returns a random number less than 1, but greater or equal to 0.
	 * <p>
	 * For more details see chapter C.6.1
	 *
	 * @return the new random number.
	 * */
	public static TitanFloat rnd() {
		if (!rndSeedSet) {
			set_rnd_seed(TTCN_Snapshot.time_now());
		}

		return rnd_generate();
	}

	/**
	 * Returns a random number less than 1, but greater or equal to 0.
	 * <p>
	 * For more details see chapter C.6.1
	 *
	 * @param seed
	 *                the seed value to initialize the random number
	 *                generation.
	 * @return the new random number.
	 * */
	public static TitanFloat rnd(final double seed) {
		set_rnd_seed(seed);

		return rnd_generate();
	}

	/**
	 * Returns a random number less than 1, but greater or equal to 0.
	 * <p>
	 * For more details see chapter C.6.1
	 *
	 * @param seed
	 *                the seed value to initialize the random number
	 *                generation.
	 * @return the new random number.
	 * */
	public static TitanFloat rnd(final TitanFloat seed) {
		seed.must_bound("Initializing the random number generator with an unbound float value as seed.");

		set_rnd_seed(seed.get_value());
		return rnd_generate();
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
		value.must_bound("The argument of function str2bit() is an unbound charstring value.");

		final int value_length = value.lengthof().get_int();
		final StringBuilder chars_ptr = new StringBuilder();
		chars_ptr.append(value.get_value().toString());
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
				TTCN_Logger.log_char_escaped(c);
				TTCN_Logger.log_event_str(MessageFormat.format("'' was found at index {0}.", i));
				TtcnError.TtcnErrorEnd();
				break;
			}
		}

		return new TitanBitString(ret_val.toString());
	}

	public static TitanBitString str2bit(final TitanCharString_Element value) {
		value.must_bound("The argument of function str2bit() is an unbound charstring element.");

		final char c = value.get_char();
		if (c != '0' && c != '1') {
			TtcnError.TtcnErrorBegin("The argument of function str2bit() shall contain characters '0' and '1' only, but the given charstring element contains the character `");
			TTCN_Logger.log_char_escaped(c);
			TTCN_Logger.log_event_str("'.");
			TtcnError.TtcnErrorEnd();
		}

		return new TitanBitString((value.get_char() == '1' ? "1" : "0"));
	}

	// float2str
	public static TitanCharString float2str(final double value) {
		//differnce between java and c++
		if (Double.isNaN(value)) {
			return new TitanCharString("not_a_number");
		} else if (value == Double.NEGATIVE_INFINITY) {
			return new TitanCharString("-infinity");
		} else if (value == Double.POSITIVE_INFINITY) {
			return new TitanCharString("infinity");
		} else if (value == 0.0
				|| (value > -TitanFloat.MAX_DECIMAL_FLOAT && value <= -TitanFloat.MIN_DECIMAL_FLOAT)
				|| (value >= TitanFloat.MIN_DECIMAL_FLOAT && value < TitanFloat.MAX_DECIMAL_FLOAT)) {
			return new TitanCharString(String.format("%f", value));
		} else {
			return new TitanCharString(String.format("%e", value));
		}
	}

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
		value.must_bound("The argument of function float2str() is an unbound float value.");

		//differnce between java and c++
		if (value.get_value().isNaN()) {
			return new TitanCharString("not_a_number");
		} else if (value.get_value() == Double.NEGATIVE_INFINITY) {
			return new TitanCharString("-infinity");
		} else if (value.get_value() == Double.POSITIVE_INFINITY) {
			return new TitanCharString("infinity");
		} else if (value.get_value() == 0.0
				|| (value.get_value() > -TitanFloat.MAX_DECIMAL_FLOAT && value.get_value() <= -TitanFloat.MIN_DECIMAL_FLOAT)
				|| (value.get_value() >= TitanFloat.MIN_DECIMAL_FLOAT && value.get_value() < TitanFloat.MAX_DECIMAL_FLOAT)) {
			return new TitanCharString(String.format("%f", value.get_value()));
		} else {
			return new TitanCharString(String.format("%e", value.get_value()));
		}
	}

	// unichar2char
	public static TitanCharString unichar2char(final TitanUniversalCharString value) {
		value.must_bound("The argument of function unichar2char() is an unbound universal charstring value.");

		final int value_length = value.lengthof().get_int();
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
		value.must_bound("The argument of function unichar2char() is an unbound universal charstring element.");

		final TitanUniversalChar uchar = value.get_char();
		if (uchar.getUc_group() != 0 || uchar.getUc_plane() != 0 || uchar.getUc_row() != 0 || uchar.getUc_cell() > 127) {
			throw new TtcnError(MessageFormat.format("The characters in the argument of function unichar2char() shall be within the range char(0, 0, 0, 0) .. char(0, 0, 0, 127), "
					+ "but the given universal charstring element contains the quadruple char({0}, {1}, {2}, {3}).", uchar.getUc_group(),uchar.getUc_plane(),uchar.getUc_row(),uchar.getUc_row()));
		}

		return new TitanCharString(String.valueOf(uchar.getUc_cell()));
	}

	// C.33 - regexp
	public static TitanCharString regexp(final TitanCharString instr, final TitanCharString expression, final int groupno, final boolean nocase) {
		instr.must_bound("The first argument (instr) of function regexp() is an unbound charstring value.");
		expression.must_bound("The second argument (expression) of function regexp() is an unbound charstring value.");

		if (groupno < 0) {
			throw new TtcnError(MessageFormat.format("The third argument (groupno) of function regexp() is a negative integer value: {0}.", groupno));
		}

		final String instr_str = instr.get_value().toString();
		for (int i = 0; i < instr_str.length(); i++) {
			if (instr_str.charAt(i) == '\0') {
				TtcnError.TtcnWarningBegin("The first argument (instr) of function regexp(), which is ");
				instr.log();
				TTCN_Logger.log_event(", contains a character with zero character code at index %d. The rest of the string will be ignored during matching.", i);
				TtcnError.TtcnWarningEnd();
				break;
			}
		}

		final String expression_str = expression.get_value().toString();
		for (int i = 0; i < expression_str.length(); i++) {
			if (expression_str.charAt(i) == '\0') {
				TtcnError.TtcnWarningBegin("The second argument (expression) of function regexp(), which is ");
				expression.log();
				TTCN_Logger.log_event(", contains a character with zero character code at index %d. The rest of the string will be ignored during matching.", i);
				TtcnError.TtcnWarningEnd();
				break;
			}
		}

		final Pattern posix_str = TTCN_Pattern.convert_pattern(expression_str, nocase);
		if (posix_str == null) {
			TtcnError.TtcnErrorBegin("The second argument (expression) of function regexp(), which is ");
			expression.log();
			TTCN_Logger.log_event(", is not a valid TTCN-3 character pattern.");
			TtcnError.TtcnErrorEnd();
		}
		if (TTCN_Logger.log_this_event(Severity.DEBUG_UNQUALIFIED)) {
			TTCN_Logger.begin_event(Severity.DEBUG_UNQUALIFIED);
			TTCN_Logger.log_event_str("regexp(): POSIX ERE equivalent of ");
			new TitanCharString_template(template_sel.STRING_PATTERN, expression, nocase).log();
			TTCN_Logger.log_event_str(" is: ");
			new TitanCharString(posix_str.toString()).log();
			TTCN_Logger.end_event();
		}

		return new TitanCharString(TTCN_Pattern.regexp(instr_str, posix_str, groupno, nocase));
	}

	public static TitanCharString regexp(final TitanCharString instr, final TitanCharString expression, final TitanInteger groupno, final boolean nocase) {
		groupno.must_bound("The third argument (groupno) of function regexp() is an unbound integer value.");

		return regexp(instr, expression, groupno.get_int(), nocase);
	}

	public static TitanUniversalCharString regexp(final TitanUniversalCharString instr, final TitanUniversalCharString expression_val, final TitanUniversalCharString expression_templ, final int groupno, final boolean nocase) {
		if((expression_val != null && expression_templ != null) || (expression_val == null && expression_templ == null)) {
			throw new TtcnError("Internal error: regexp(): invalid parameters");
		}
		instr.must_bound("The first argument (instr) of function regexp() is an unbound charstring value.");
		if (expression_val != null) {
			expression_val.must_bound("The second argument (expression) of function regexp() is an unbound universal charstring value.");
		} else {
			if (!expression_templ.is_bound()) {
				throw new TtcnError("The second argument (expression) of function regexp() is an unbound universal charstring template.");
			}
		}
		if (groupno < 0) {
			throw new TtcnError(MessageFormat.format("The third argument (groupno) of function regexp() is a negative integer value: {0}.", groupno));
		}

		Pattern posix_str = null;
		if (expression_val != null) {
			posix_str = TTCN_Pattern.convert_pattern(expression_val.get_stringRepr_for_pattern().toString(), nocase);
		} else {
			posix_str = TTCN_Pattern.convert_pattern(expression_templ.get_stringRepr_for_pattern().toString(), nocase);
		}
		if (posix_str == null) {
			TtcnError.TtcnErrorBegin("The second argument (expression) of function regexp(), which is ");
			if (expression_val != null) {
				expression_val.log();
			} else {
				expression_templ.log();
			}
			TTCN_Logger.log_event(", is not a valid TTCN-3 character pattern.");
			TtcnError.TtcnErrorEnd();
		}
		if (TTCN_Logger.log_this_event(Severity.DEBUG_UNQUALIFIED)) {
			TTCN_Logger.begin_event(Severity.DEBUG_UNQUALIFIED);
			TTCN_Logger.log_event_str("regexp(): POSIX ERE equivalent of ");
			new TitanCharString_template(template_sel.STRING_PATTERN,new TitanCharString(posix_str.toString()), nocase).log();
			TTCN_Logger.log_event_str(" is: ");
			new TitanCharString(posix_str.toString()).log();
			TTCN_Logger.end_event();
		}

		if (instr.charstring) {
			return new TitanUniversalCharString(TTCN_Pattern.regexp(instr.cstr.toString(), posix_str, groupno, nocase));
		} else {
			//convert String to TitanUniversChars
			String regexp_str = TTCN_Pattern.regexp(instr.to_utf(), posix_str, groupno, nocase);
			List<TitanUniversalChar> uc_chars = new ArrayList<TitanUniversalChar>(regexp_str.length());
			try {
				byte[] utf_bytes = regexp_str.getBytes("UTF-32");
				for (int i = 0; i < utf_bytes.length; i+=4) {
					final char uc_group = (char)(utf_bytes[i] & 0xFF);
					final char uc_plane = (char)(utf_bytes[i + 1] & 0xFF);
					final char uc_row = (char)(utf_bytes[i + 2] & 0xFF);
					final char uc_cell = (char)(utf_bytes[i + 3] & 0xFF);
					uc_chars.add(new TitanUniversalChar(uc_group, uc_plane, uc_row, uc_cell));
				}
			} catch (UnsupportedEncodingException e) {
				throw new TtcnError(e);
			}
			return new TitanUniversalCharString(uc_chars);
		}
	}

	public static TitanUniversalCharString regexp(final TitanUniversalCharString instr, final TitanUniversalCharString expression, final int groupno, final boolean nocase) {
		return regexp(instr, expression, null, groupno, nocase);
	}

	public static TitanUniversalCharString regexp(final TitanUniversalCharString instr, final TitanUniversalCharString expression, final TitanInteger groupno, final boolean nocase) {
		groupno.must_bound("The third argument (groupno) of function regexp() is an unbound integer value.");

		return regexp(instr, expression, groupno.get_int(), nocase);
	}

	// regexp on templates
	public static TitanCharString regexp(final TitanCharString_template instr, final TitanCharString_template expression, final int groupno, final boolean nocase) {
		if (!instr.is_value()) {
			throw new TtcnError("The first argument of function regexp() is a template with non-specific value.");
		}
		if (expression.is_value()) {
			return regexp(instr.valueof(), expression.valueof(), groupno, nocase);
		}
		// pattern matching to specific value
		if (expression.get_selection() == template_sel.STRING_PATTERN) {
			return regexp(instr.valueof(), expression.single_value, groupno, nocase);
		}

		throw new TtcnError("The second argument of function regexp() should be specific value or pattern matching template.");
	}

	public static TitanCharString regexp(final TitanCharString_template instr, final TitanCharString_template expression, final TitanInteger groupno, final boolean nocase) {
		groupno.must_bound("The third argument (groupno) of function regexp() is an unbound integer value.");

		return regexp(instr, expression, groupno.get_int(), nocase);
	}

	public static TitanUniversalCharString regexp(final TitanUniversalCharString_template instr, final TitanUniversalCharString_template expression, final int groupno, final boolean nocase) {
		if (!instr.is_value()) {
			throw new TtcnError("The first argument of function regexp() is a template with non-specific value.");
		}
		if (expression.is_value()) {
			return regexp(instr.valueof(), expression.valueof(), groupno, nocase);
		}
		// pattern matching to specific value
		if (expression.get_selection() == template_sel.STRING_PATTERN) {
			return regexp(instr.valueof(), null, expression.valueof(), groupno, nocase);
		}

		throw new TtcnError("The second argument of function regexp() should be specific value or pattern matching template.");
	}

	public static TitanUniversalCharString regexp(final TitanUniversalCharString_template instr, final TitanUniversalCharString_template expression, final TitanInteger groupno, final boolean nocase) {
		groupno.must_bound("The third argument (groupno) of function regexp() is an unbound integer value.");

		return regexp(instr, expression, groupno.get_int(), nocase);
	}

	public static TitanCharString regexp(final TitanCharString instr, final TitanCharString_template expression, final int groupno, final boolean nocase) {
		if (!instr.is_value()) {
			throw new TtcnError("The first argument of function regexp() is a template with non-specific value.");
		}
		if (expression.is_value()) {
			return regexp(instr, expression.valueof(), groupno, nocase);
		}
		// pattern matching to specific value
		if (expression.get_selection() == template_sel.STRING_PATTERN) {
			return regexp(instr, expression.single_value, groupno, nocase);
		}

		throw new TtcnError("The second argument of function regexp() should be specific value or pattern matching template.");
	}

	public static TitanCharString regexp(final TitanCharString instr, final TitanCharString_template expression, final TitanInteger groupno, final boolean nocase) {
		groupno.must_bound("The third argument (groupno) of function regexp() is an unbound integer value.");

		return regexp(instr, expression, groupno.get_int(), nocase);
	}

	public static TitanCharString regexp(final TitanUniversalCharString instr, final TitanCharString expression, final TitanInteger groupno, final boolean nocase) {
		instr.must_bound("The first argument (instr) of function regexp() is an unbound universal charstring value.");

		if (instr.charstring) {
			return regexp(new TitanCharString(instr.cstr), expression, groupno, nocase);
		} else {
			throw new TtcnError("The first argument (instr) of function regexp() should be a charstring value.");
		}
	}

	// for internal purposes
	public static String get_port_name(final String port_name, final int array_index) {
		return MessageFormat.format("{0}[{1}]", port_name, array_index);
	}

	public static String get_port_name(final String port_name, final TitanInteger array_index) {
		array_index.must_bound("Using an unbound integer value for indexing an array of ports.");

		return get_port_name(port_name, array_index.get_int());
	}

	public static String get_port_name(final TitanCharString port_name, final int array_index) {
		port_name.must_bound("Internal error: Using an unbound charstring value to obtain the name of a port.");

		return get_port_name(port_name.get_value().toString(), array_index);
	}

	public static String get_port_name(final TitanCharString port_name, final TitanInteger array_index) {
		port_name.must_bound("Internal error: Using an unbound charstring value to obtain the name of a port.");
		array_index.must_bound("Using an unbound integer value for indexing an array of ports.");

		return get_port_name(port_name.get_value().toString(), array_index.get_int());
	}

	public static TitanCharString encode_base64(final TitanOctetString msg, final TitanBoolean use_linebreaks) {
		final char pad = '=';
		final byte[] p_msg = msg.get_value();
		int msgPos = 0;
		int octets_left = p_msg.length;
		char[] output = new char[((octets_left*22) >> 4) + 7];
		int outpotPos = 0;
		int n_4chars = 0;
		final boolean linebreaks = use_linebreaks.get_value();
		while (octets_left >= 3) {
			output[outpotPos++] = code_table.charAt((p_msg[msgPos + 0] & 0xFF) >> 2);
			output[outpotPos++] = code_table.charAt((((p_msg[msgPos + 0] & 0xFF) << 4) | ((p_msg[msgPos + 1] & 0xFF) >> 4)) & 0x3f);
			output[outpotPos++] = code_table.charAt((((p_msg[msgPos + 1] & 0xFF) << 2) | ((p_msg[msgPos + 2] & 0xFF) >> 6)) & 0x3f);
			output[outpotPos++] = code_table.charAt((p_msg[msgPos + 2] & 0xFF) & 0x3f);
			n_4chars++;
			if (linebreaks && n_4chars >= 19 && octets_left != 3) {
				output[outpotPos++] = '\r';
				output[outpotPos++] = '\n';
				n_4chars = 0;
			}
			msgPos += 3;
			octets_left -= 3;
		}
		switch (octets_left) {
		case 1:
			output[outpotPos++] = code_table.charAt((p_msg[msgPos + 0] & 0xFF) >> 2);
			output[outpotPos++] = code_table.charAt(((p_msg[msgPos + 0] & 0xFF) << 4) & 0x3f);
			output[outpotPos++] = pad;
			output[outpotPos++] = pad;
			break;
		case 2:
			output[outpotPos++] = code_table.charAt((p_msg[msgPos + 0] & 0xFF) >> 2);
			output[outpotPos++] = code_table.charAt((((p_msg[msgPos + 0] & 0xFF) << 4) | ((p_msg[msgPos + 1] & 0xFF) >> 4)) & 0x3f);
			output[outpotPos++] = code_table.charAt(((p_msg[msgPos + 1] & 0xFF) << 2) & 0x3f);
			output[outpotPos++] = pad;
			break;
		default:
			break;
		}

		return new TitanCharString(new String(output, 0, outpotPos));
	}

	public static TitanCharString encode_base64(final TitanOctetString msg) {
		final char pad = '=';
		final byte[] p_msg = msg.get_value();
		int msgPos = 0;
		int octets_left = p_msg.length;
		char[] output = new char[((octets_left*22) >> 4) + 7];
		int outpotPos = 0;
		while (octets_left >= 3) {
			output[outpotPos++] = code_table.charAt((p_msg[msgPos + 0] & 0xFF) >> 2);
			output[outpotPos++] = code_table.charAt((((p_msg[msgPos + 0] & 0xFF) << 4) | ((p_msg[msgPos + 1] & 0xFF) >> 4)) & 0x3f);
			output[outpotPos++] = code_table.charAt((((p_msg[msgPos + 1] & 0xFF) << 2) | ((p_msg[msgPos + 2] & 0xFF) >> 6)) & 0x3f);
			output[outpotPos++] = code_table.charAt((p_msg[msgPos + 2] & 0xFF) & 0x3f);
			msgPos += 3;
			octets_left -= 3;
		}
		switch (octets_left) {
		case 1:
			output[outpotPos++] = code_table.charAt((p_msg[msgPos + 0] & 0xFF) >> 2);
			output[outpotPos++] = code_table.charAt(((p_msg[msgPos + 0] & 0xFF) << 4) & 0x3f);
			output[outpotPos++] = pad;
			output[outpotPos++] = pad;
			break;
		case 2:
			output[outpotPos++] = code_table.charAt((p_msg[msgPos + 0] & 0xFF) >> 2);
			output[outpotPos++] = code_table.charAt((((p_msg[msgPos + 0] & 0xFF) << 4) | ((p_msg[msgPos + 1] & 0xFF) >> 4)) & 0x3f);
			output[outpotPos++] = code_table.charAt(((p_msg[msgPos + 1] & 0xFF) << 2) & 0x3f);
			output[outpotPos++] = pad;
			break;
		default:
			break;
		}

		return new TitanCharString(new String(output, 0, outpotPos));
	}

	public static TitanOctetString decode_base64(final TitanCharString b64) {
		final byte[] p_b64 = b64.get_value().toString().getBytes();
		int b64Pos = 0;
		int chars_left = b64.lengthof().get_int();
		byte[] output = new byte[((chars_left >> 2) + 1 ) * 3 ];
		int outpotPos = 0;
		int bits = 0;
		int n_bits = 0;

		while (chars_left > 0) {
			chars_left--;
			if (p_b64[b64Pos] > 0 && decode_table[p_b64[b64Pos]] < 64) {
				bits <<= 6;
				bits |= decode_table[p_b64[b64Pos]];
				n_bits += 6;
				if (n_bits >= 8) {
					output[outpotPos++] = (byte)(( bits >> (n_bits - 8)) & 0xff);
					n_bits -= 8;
				}
			} else if (p_b64[b64Pos] == '=') {
				break;
			} else {
				if (p_b64[b64Pos] == '\r' && p_b64[b64Pos + 1] == '\n') {
					b64Pos++; // skip \n too
				} else {
					throw new TtcnError(String.format("Error: Invalid character in Base64 encoded data: 0x%02X", p_b64[b64Pos]));
				}
			}

			b64Pos++;
		}

		final byte[] result = new byte[outpotPos];
		System.arraycopy(output, 0, result, 0, outpotPos);

		return new TitanOctetString(result);
	}
}