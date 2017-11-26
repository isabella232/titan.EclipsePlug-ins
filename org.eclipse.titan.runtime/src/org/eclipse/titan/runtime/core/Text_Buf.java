/******************************************************************************
 * Copyright (c) 2000-2017 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.text.MessageFormat;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Storing and converting data for use in the internal encoder/decoder
 *
 * @author Kristof Szabados
 *
 * FIXME not yet complete
 */
public final class Text_Buf {
	final static private int BUF_SIZE = 1000;
	final static private int BUF_HEAD = 24;

	// the size of the buffer (allocated bytes)
	private int buf_size;

	// number of reserved bytes for head
	private int buf_begin;

	// read position into the payload
	private int buf_pos;

	// number of payload bytes
	private int buf_len;
	private byte data_ptr[];

	public Text_Buf() {
		buf_size = 0;
		buf_begin = BUF_HEAD;
		buf_pos = BUF_HEAD;
		buf_len = 0;

		Allocate(BUF_SIZE);
	}

	private void Allocate(final int size) {
		int new_buf_size = BUF_SIZE + BUF_HEAD;
		while (new_buf_size < size + buf_begin) {
			new_buf_size *= 2;
		}
		data_ptr = new byte[new_buf_size];
		buf_size = new_buf_size;
	}

	private void Reallocate(final int size) {
		int new_buf_size = BUF_SIZE + BUF_HEAD;
		while (new_buf_size < size + buf_begin) {
			new_buf_size *= 2;
		}

		if (new_buf_size != buf_size) {
			final byte temp_data_ptr[] = new byte[new_buf_size];
			System.arraycopy(data_ptr, 0, temp_data_ptr, 0, new_buf_size < buf_size ? new_buf_size : buf_size);
			data_ptr = temp_data_ptr;
		}

		buf_size = new_buf_size;
	}

	public void reset() {
		buf_begin = BUF_HEAD;
		Reallocate(buf_size);
		buf_pos = BUF_HEAD;
		buf_len = 0;
	}

	public void rewind() {
		buf_pos = buf_begin;
	}

	public int get_len() {
		return buf_len;
	}

	public int get_pos() {
		return buf_pos - buf_len;
	}

	public void buf_seek(final int new_pos) {
		buf_pos = buf_begin + new_pos;
	}

	/**
	 * Encode an integer (only native) into the text buffer
	 *
	 * @param value the value to be pushed
	 * */
	public void push_int(final int value) {
		final boolean isNegative = value < 0;
		int unsignedValue = isNegative ? -value : value;
		int bytesNeeded = 1;
		for (int tmp = unsignedValue >> 6; tmp != 0; tmp >>= 7) {
			bytesNeeded++;
		}
		Reallocate(buf_len + bytesNeeded);
		for (int i = bytesNeeded - 1; ; i--) {
			// The top bit is always 1 for a "middle" byte, 0 for the last byte.
			// That leaves 7 bits, except for the first byte where the 2nd highest
			// bit is the sign bit, so only 6 payload bits are available.
			if (i > 0) {
				data_ptr[i + buf_begin + buf_len] = (byte) (unsignedValue & 0x7F);
				unsignedValue >>= 7;
			} else {
				data_ptr[i + buf_begin + buf_len] = (byte) (unsignedValue & 0x3F);
			}
			if (i < bytesNeeded - 1) {
				// Set the top bit for all but the last byte
				data_ptr[i + buf_begin + buf_len] |= 0x80;
			}
			if (i == 0) {
				break;
			}
		}
		if (isNegative) {
			data_ptr[0 + buf_begin + buf_len] |= 0x40; // Put in the sign bit
		}
		buf_len += bytesNeeded;
	}

	/**
	 * Encode an integer (may be bigint) into the text buffer
	 *
	 * @param value the value to be pushed
	 * */
	public void push_int(final TitanInteger value) {
		if (value.isNative()) {
			final int nativeValue = value.getInt();
			push_int(nativeValue);
		} else {
			final BigInteger bigValue = value.getBigInteger();
			final boolean isNegative = bigValue.compareTo(BigInteger.ZERO) == -1;
			BigInteger unsignedValue = bigValue.abs();
			final int numBits = bigValue.bitLength();
			// Calculation
			// first 6 bit +the sign bit are stored in the first octet
			// the remaining bits stored in 7 bit group + continuation bit
			// So how many octest needed to store the (num_bits + 1) many bits
			// in 7 bit ber octet form?
			// ((num_bits+1)+6)/7 =>
			// (num_bits+7)/7 =>
			// (num_bits / 7)+1

			final int bytesNeeded = (numBits / 7) + 1;
			Reallocate(buf_len + bytesNeeded);
			for (int i = bytesNeeded - 1;; i--) {
				if (i > 0) {
					data_ptr[i + buf_begin + buf_len] = (byte) (unsignedValue.intValue() & 0x7F);
					unsignedValue = unsignedValue.shiftRight(7);
				} else {
					data_ptr[i + buf_begin + buf_len] = (byte) (unsignedValue.intValue() & 0x3F);
				}
				if (i < bytesNeeded - 1) {
					// Set the top bit for all but the last byte
					data_ptr[i + buf_begin + buf_len] |= 0x80;
				}
				if (i == 0) {
					break;
				}
			}
			if (isNegative) {
				data_ptr[0 + buf_begin + buf_len] |= 0x40; // Put in the sign bit
			}
			buf_len += bytesNeeded;
		}
	}

	/**
	 * Extract an integer from the buffer
	 *
	 * @return the extracted value
	 * @throw TtcnError if there is no integer available.
	 * */
	public TitanInteger pull_int() {
		final TitanInteger value = new TitanInteger();
		if (!safe_pull_int(value)) {
			throw new TtcnError("Text decoder: Decoding of integer failed.");
		}
		return value;
	}

	/**
	 * Extract an integer if it's safe to do so.
	 *
	 * @param value the value to be set when successful.
	 * @return true if an integer could be extracted, false otherwise
	 * */
	public boolean safe_pull_int(final TitanInteger value) {
		final int buf_end = buf_begin + buf_len;
		if (buf_pos >= buf_end) {
			return false;
		}

		int pos = buf_pos;
		// Count continuation flags.
		while(pos < buf_end && ((data_ptr[pos] & 0x80) != 0)) {
			pos++;
		}
		if (pos >= buf_end) {
			return false;
		}
		final int bytesNeeded = pos - buf_pos + 1;
		if (bytesNeeded > 4) {
			//will be a biginteger
			BigInteger bigValue = BigInteger.ZERO;// originally D
			for (int i = 0; i < bytesNeeded; i++) {
				if (i > 0) {
					bigValue = bigValue.add(BigInteger.valueOf(data_ptr[i + buf_pos] & 0x7f));
				} else {
					bigValue = bigValue.add(BigInteger.valueOf(data_ptr[i + buf_pos] & 0x3f));
				}
				if (i < bytesNeeded - 1) {
					bigValue = bigValue.shiftLeft(7);
				}
			}
			if ((data_ptr[0 + buf_pos] & 0x40) != 0) {
				bigValue = bigValue.negate();
			}
			if (bigValue.bitLength() > 4 * 8 - 1) {
				value.assign(bigValue);
			} else {
				value.assign(bigValue.intValue());
			}
		} else {
			// can be stored in native int
			int locValue = 0;
			for (int i = 0; i < bytesNeeded; i++) {
				if (i > 0) {
					locValue |= data_ptr[i + buf_pos] & 0x7f;
				} else {
					locValue |= data_ptr[i + buf_pos] & 0x3f;
				}
				if (i < bytesNeeded - 1) {
					locValue <<= 7;
				}
				if ((data_ptr[0 + buf_pos] & 0x40) != 0) {
					value.assign(-locValue);
				} else {
					value.assign(locValue);
				}
			}
		}

		buf_pos = pos + 1;

		return true;
	}

	/**
	 * Encode a double precision floating point number in the buffer.
	 *
	 * @param the value to encode
	 * */
	public void push_double(final double value) {
		final byte[] bytes = new byte[8];
		ByteBuffer.wrap(bytes).putDouble(value);

		Reallocate(buf_len + 8);
		System.arraycopy(bytes, 0, data_ptr, buf_pos, 8);
		buf_pos += 8;
	}

	/**
	 * Extract a double precision floating point number.
	 *
	 * @return the extracted double
	 * */
	public double pull_double() {
		if (buf_pos + 8 > buf_begin + buf_len) {
			throw new TtcnError("Text decoder: Decoding of float failed. (End of buffer reached)");
		}
		final byte[] bytes = new byte[8];
		System.arraycopy(data_ptr, buf_pos, bytes, 0, 8);
		buf_pos += 8;

		return ByteBuffer.wrap(bytes).getDouble();
	}

	/**
	 * Write a fixed number of bytes in the buffer.
	 *
	 * @param len the number for bytes to write
	 * @param data the bytes to write.
	 * */
	public void push_raw(final int len, final byte[] data) {
		if (len < 0) {
			throw new TtcnError(MessageFormat.format("Text encoder: Encoding raw data with negative length ({0}).", len));
		}

		Reallocate(buf_len + len);
		System.arraycopy(data, 0, data_ptr, buf_begin + buf_len, len);
		buf_len += len;
	}

	public void push_raw_front(final int len, final byte[] data) {
		if (len < 0) {
			throw new TtcnError(MessageFormat.format("Text encoder: Encoding raw data with negative length ({0}).", len));
		}

		Reallocate(buf_len + len);
		for (int i = buf_len - 1; i >= 0; --i) {
			data_ptr[buf_begin + len + i] = data_ptr[buf_begin + i];
		}
		System.arraycopy(data, len, data_ptr, buf_begin, len);
		buf_len += len;
	}

	/**
	 * Extract a fixed number of bytes from the buffer.
	 *
	 * @param len the number of bytes to read
	 * @param data the buffer to extract into
	 * */
	public void pull_raw(final int len, final byte[] data) {
		if (len < 0) {
			throw new TtcnError(MessageFormat.format("Text decoder: Decoding raw data with negative length ({0}).", len));
		}

		if (buf_pos + len > buf_begin + buf_len) {
			throw new TtcnError("Text decoder: End of buffer reached.");
		}
		System.arraycopy(data_ptr, buf_pos, data, 0, len);
		buf_pos += len;
	}

	/**
	 * Write a 0-terminated string
	 *
	 * Writes the length followed by the raw bytes (no end marker)
	 *
	 * @param string the string to be written
	 * */
	public void push_string(final String string) {
		if (string != null) {
			final int len = string.length();
			push_int(len);
			push_raw(len, string.getBytes());
		} else {
			push_int(0);
		}
	}

	/**
	 * Extract a string.
	 *
	 * @return the extracted string
	 * */
	public String pull_string() {
		final int len = pull_int().getInt();
		if (len < 0) {
			throw new TtcnError(MessageFormat.format("Text decoder: Negative string length ({0}).", len));
		}

		final byte[] raw = new byte[len];
		pull_raw(len, raw);
		return new String(raw);
	}

	/**
	 * Calculate the length of the buffer and write it at the beginning.
	 * */
	public void calculate_length() {
		int value = buf_len;
		int bytes_needed = 1;
		for (int tmp = value >> 6; tmp != 0; tmp >>= 7) {
			bytes_needed++;
		}
		if (buf_begin < bytes_needed) {
			throw new TtcnError("Text encoder: There is not enough space to calculate message length.");
		}
		for (int i = bytes_needed - 1;; i--) {
			if (i > 0) {
				data_ptr[buf_begin - bytes_needed + i] = (byte) (value & 0x7F);
				value >>= 7;
			} else {
				data_ptr[buf_begin - bytes_needed + i] = (byte) (value & 0x3F);
			}
			if (i < bytes_needed - 1) {
				data_ptr[buf_begin - bytes_needed + i] |= 0x80;
			}
			if (i == 0) {
				break;
			}
		}

		buf_begin -= bytes_needed;
		buf_len += bytes_needed;
	}

	public void get_end(final AtomicInteger end_index, final AtomicInteger end_len) {
		final int buf_end = buf_begin + buf_len;
		if (buf_size - buf_end < BUF_SIZE) {
			Reallocate(buf_len + BUF_SIZE);
		}
		end_index.set(buf_end);
		end_len.set(buf_size - buf_end);
	}

	public void increase_length(final int add_len) {
		if (add_len < 0) {
			throw new TtcnError(MessageFormat.format("Text decoder: Addition is negative ({0}) when increasing length.", add_len));
		}

		if (buf_begin + buf_len + add_len > buf_size) {
			throw new TtcnError("Text decoder: Addition is too big when increasing length.");
		}
		buf_len += add_len;
	}
}
