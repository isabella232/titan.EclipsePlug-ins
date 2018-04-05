/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.titan.runtime.core.RAW.RAW_coding_par;
import org.eclipse.titan.runtime.core.TTCN_EncDec.raw_order_t;

/**
 * Buffer used by the different encoders/decoders.
 *
 * TODO too frequent array access might be optimized with temporal variables
 *
 * @author Farkas Izabella Ingrid
 */
public class TTCN_Buffer {
	final static private int INITIAL_SIZE = 1024;

	private char data_ptr[];

	/** The number of char of memory used (buf_len <= buf_size)*/
	private int buf_len;

	/** Read offset in char in the buffer (buf_pos <= buf_len)*/
	private int buf_pos;

	/**Read offset of the current bit inside the byte at buf_pos*/
	private int bit_pos;
	private int last_bit_pos;
	private int last_bit_bitpos;
	private int start_of_ext_bit;

	/**the state of the "last bit" (only after get_b was called)*/
	private boolean last_bit;

	// \c true for TOP_BIT_LEFT
	private boolean current_bitorder;

	// \c true if 0 signals the end
	private  boolean ext_bit_reverse;
	private int ext_level;

	/** Resets all fields except the size and length indicators. */
	private void reset_buffer() {
		buf_pos = 0;
		bit_pos = 0;
		last_bit_pos = 0;
		last_bit_bitpos = 0;
		start_of_ext_bit = 0;
		last_bit = false;
		current_bitorder = false;
		ext_bit_reverse = false;
		ext_level = 0;
	}

	/**
	 * Returns the smallest preferred size of the allocated memory area
	 * that is at least target_size.
	 *
	 * @param target_size the amount of memory requested.
	 * @return the amount of memory to be allocated.
	 * */
	private static int get_memory_size(final int target_size) {
		int newSize = INITIAL_SIZE;
		while (newSize < target_size) {
			final int nextSize = newSize + newSize;
			if (nextSize > newSize) {
				newSize = nextSize;
			} else {
				// integer overflow occurred
				return -1;
			}
		}
		return newSize;
	}

	/** Ensures that there are at least target_size writable bytes in the
	 * memory area after buf_len. 
	 * @param size_incr inctement buffer the number of size_incr
	 * */
	private void increase_size(final int size_incr) {
		if (data_ptr != null) {
			int target_size = buf_len + size_incr;
			if (target_size < buf_len) {
				TTCN_EncDec_ErrorContext.error_internal("TTCN_Buffer: Overflow error (cannot increase buffer size).");
			}
			if (target_size > data_ptr.length) {
				final int buf_size = get_memory_size(target_size);
				final char[] data_ptr_new = new char[buf_size];
				System.arraycopy(data_ptr_new, 0, data_ptr, 0, buf_len);
				data_ptr = data_ptr_new;
			}
		} else {  // a brand new buffer
			final int buf_size = get_memory_size(size_incr);
			data_ptr = new char[buf_size];
		}
	}

	/** Creates an empty buffer. */
	public TTCN_Buffer() {
		data_ptr = null;
		buf_len = 0;
		reset_buffer();
	}

	/** Copy constructor.
	 * @param p_buf the {@link TTCN_Buffer}  used to initialize the buffer.
	 *  */
	public TTCN_Buffer(final TTCN_Buffer p_buf) {
		data_ptr = new char[p_buf.data_ptr.length];
		System.arraycopy(p_buf.data_ptr, 0, data_ptr, 0, p_buf.data_ptr.length);
		buf_len = p_buf.buf_len;
		reset_buffer();
	}

	/**
	 * Initializes the buffer with the contents of p_os.
	 *
	 * @param p_os 
	 * 			the {@link TitanOctetString} used to initialize the buffer. */
	public  TTCN_Buffer(final TitanOctetString p_os) {
		p_os.mustBound("Initializing a TTCN_Buffer with an unbound octetstring value.");

		buf_len = p_os.lengthOf().getInt();
		data_ptr = new char[buf_len];
		System.arraycopy(p_os.getValue(), 0, data_ptr, 0, buf_len);
		reset_buffer();
	}

	/**
	 * Initializes the buffer with the contents of p_cs.
	 * @param p_cs 
	 * 			the {@link TitanCharString} used to initialize the buffer.*/
	public TTCN_Buffer(final TitanCharString p_cs) {
		p_cs.mustBound("Initializing a TTCN_Buffer with an unbound charstring value.");

		buf_len = p_cs.lengthOf().getInt();
		data_ptr = new char[buf_len];
		for (int i = 0; i < buf_len; i++) {
			data_ptr[i] =  p_cs.getAt(i).get_char();
		}
		reset_buffer();
	}

	/** Copies the contents of p_buf into this.
	 * The read position and other attributes are reset.
	 * @param p_buf
	 *  */
	public TTCN_Buffer assign(final TTCN_Buffer p_buf) {
		if (p_buf != this) {
			buf_len = p_buf.buf_len;
			if (p_buf.data_ptr != null) {
				data_ptr = new char[p_buf.data_ptr.length];
				System.arraycopy(p_buf.data_ptr, 0, data_ptr, 0, p_buf.data_ptr.length);
			} else {
				data_ptr = null;
			}
		}
		reset_buffer();
		return this;
	}

	/** Copies the contents of p_os into this. Other attributes are reset.
	 * @param p_os
	 * 				  */
	public  TTCN_Buffer assign(final TitanOctetString p_os) {
		p_os.mustBound("Assignment of an unbound octetstring value to a TTCN_Buffer.");
		buf_len = p_os.lengthOf().getInt();
		data_ptr = new char[buf_len];
		System.arraycopy(p_os.getValue(), 0, data_ptr, 0, buf_len);
		reset_buffer();
		return this;
	}

	/** Copies the contents of p_cs into this.
	 * The read position and other attributes are reset.
	 * @param p_cs 
	 * 			the {@link TitanCharString}
	 *  */
	public TTCN_Buffer assign(final TitanCharString p_cs) {
		p_cs.mustBound("Assignment of an unbound charstring value to a TTCN_Buffer.");

		buf_len = p_cs.lengthOf().getInt();
		data_ptr = new char[buf_len];
		for (int i = 0; i < buf_len; i++) {
			data_ptr[i] =  p_cs.getAt(i).get_char();
		}
		reset_buffer();
		return this;
	}

	/** Erases the content of the buffer. */
	public void clear() {
		data_ptr = null;
		buf_len = 0;
		reset_buffer();
	}

	/**
	 * @return The number of bytes in the buffer.
	 * */
	public int get_len() {
		return buf_len;
	}

	public char[] get_data() {
		if (data_ptr == null) {
			return null;
		}
		final char[] result = new char[buf_len];
		System.arraycopy(data_ptr, buf_pos, result, 0, buf_len);
		return result;
	}

	/** @return how many chars (2 bytes) are in the buffer to read. */
	public int get_read_len() {
		return buf_len - buf_pos;
	}

	public char[] get_read_data() {
		if (data_ptr != null) {
			final char[] result = new char[buf_len - buf_pos];
			System.arraycopy(data_ptr, buf_pos, result, 0, buf_len - buf_pos);
			return result;
		}
		return null;
	}

	/** Sets the read position to the beginning of the buffer. */
	public void rewind() {
		buf_pos = 0;
		bit_pos = 0;
	}

	/**
	 * @return The (reading) position of the buffer.
	 * */
	public int get_pos() {
		return buf_pos;
	}

	/** Sets the (reading) position to pos, or to the end of buffer,
	 * if pos > len. 
	 * @param new_pos 
	 * */
	public void set_pos(final int new_pos) {
		if (new_pos < buf_len) {
			buf_pos = new_pos;
		} else {
			buf_pos = buf_len;
		}
	}

	/** Increases the (reading) position by delta, or sets it to the
	 * end of buffer, if get_pos() + delta > len. 
	 * @param delta 
	 * */
	public void increase_pos(final int delta)  {
		int  new_buf_pos = buf_pos + delta;
		if (new_buf_pos < buf_pos || new_buf_pos > buf_len) {
			buf_pos = buf_len;
		} else {
			buf_pos = new_buf_pos;
		}
	}

	/** You can write up to end_len chars beginning from end_ptr;
	 * after writing, you have to call also increase_length()!
	 * @see increase_length().
	 * @return the empty part of buffer (after buf_len piece). */
	public char[] get_end() {
		final char[] end_ptr;

		if (data_ptr != null) {
			final int end_len = data_ptr.length - buf_len;
			end_ptr = new char[end_len];
			System.arraycopy(data_ptr, buf_len, end_ptr, 0, end_len);
		} else {
			end_ptr = null;
		}

		return end_ptr;
	}

	/** How many chars have you written after a get_end(), beginning from end_ptr.
	 * @see get_end()
	 * @param size_incr the buffer length increment this value
	 */
	public void increase_length(final int size_incr) {
		if (data_ptr.length < buf_len + size_incr) {
			increase_size(size_incr);
		}
		buf_len += size_incr;
	}

	/** Appends single character c to the buffer. 
	 * @param c add c the buffer in the buf_len position
	 * */
	public void put_c(final char c) {
		increase_size(1);
		data_ptr[buf_len] = c;
		buf_len++;
	}

	/** Appends char array to the buffer. 
	 * @param cstr appends to the buffer
	 * */
	public void put_s(final char[] cstr) {
		final int length = cstr.length;

		if (length > 0) {
			increase_size(length);
			System.arraycopy(cstr, 0, data_ptr, buf_len, length);
			buf_len += length;
		}
	}

	/** Appends the contents of octetstring p_os to the buffer.
	 * @param p_os append to the buffer
	 *  */
	public void put_string(final TitanOctetString p_os) {
		p_os.mustBound("Appending an unbound octetstring value to a TTCN_Buffer.");

		final int n_octets = p_os.lengthOf().getInt();
		if (n_octets > 0) {
			if (buf_len > 0) {
				increase_size(n_octets);
				System.arraycopy(p_os.getValue(), 0, data_ptr, buf_len, n_octets);
				buf_len += n_octets;
			} else {
				data_ptr = new char[n_octets];
				System.arraycopy(p_os.getValue(), 0, data_ptr, 0, n_octets);
				buf_len = n_octets;
			}
		}
	}

	/** Same as put_string(). Provided only for backward compatibility. 
	 * @param p_os append to the buffer (call put_string method)
	 * */
	public void put_os(final TitanOctetString p_os) {
		put_string(p_os);
	}

	/** Appends the contents of charstring p_cs to the buffer. 
	 * @param p_cs append to the buffer
	 * */
	public void put_string(final TitanCharString p_cs) {
		p_cs.mustBound("Appending an unbound charstring value to a TTCN_Buffer.");

		final int n_chars = p_cs.lengthOf().getInt();
		if (n_chars > 0) { // there is something in the CHARSTRING
			if (buf_len > 0) { // there is something in this buffer, append
				increase_size(n_chars);
				for (int i = 0; i < n_chars; i++) {
					data_ptr[buf_len + i] = p_cs.getValue().charAt(i);
				}
				buf_len += n_chars;
			} else { // share the data
				data_ptr = new char[n_chars];
				for (int i = 0; i < n_chars; i++) {
					data_ptr[i] = p_cs.getValue().charAt(i);
				}
				buf_len = n_chars;
			}
		}
	}

	/** Same as put_string(). Provided only for backward compatibility.
	 * @param p_cs append to the buffer (call put_string method)
	 *  */
	public void put_cs(final TitanCharString p_cs) {
		put_string(p_cs);
	}

	/** Appends the content of p_buf to the buffer. 
	 * @param p_buf append to the buffer
	 * */
	public void put_buf(final TTCN_Buffer p_buf) {
		if (p_buf.data_ptr == null) {
			return;
		}
		if (p_buf.buf_len > 0) { // there is something in the other buffer
			if (buf_len > 0) { // there is something in this buffer, append
				increase_size(p_buf.buf_len);
				System.arraycopy(p_buf.data_ptr, 0, data_ptr, buf_len, p_buf.buf_len);
				buf_len += p_buf.buf_len;
			}
			else { // share the data
				//*this = p_buf;
				data_ptr =  new char[p_buf.data_ptr.length];
				System.arraycopy(p_buf.data_ptr, 0, data_ptr, 0, p_buf.data_ptr.length);
				buf_len = p_buf.buf_len;
				buf_pos = p_buf.buf_pos;
				bit_pos = p_buf.bit_pos;
				last_bit_pos = p_buf.last_bit_pos;
				last_bit_bitpos = p_buf.last_bit_bitpos;
				start_of_ext_bit = p_buf.start_of_ext_bit;
				last_bit = p_buf.last_bit;
				current_bitorder = p_buf.current_bitorder;
				ext_bit_reverse = p_buf.ext_bit_reverse;
				ext_level = p_buf.ext_level;
			}
		}
	}

	/**
	 * Stores the current contents of the buffer to variable p_os.
	 *
	 * @param p_os the variable to store the contents of the buffer into.
	 * */
	public void get_string(final TitanOctetString p_os) {
		p_os.cleanUp();
		if (buf_len > 0) {
			char[] data = new char[buf_len];
			System.arraycopy(data_ptr, 0, data, 0, buf_len);
			p_os.setValue(data);
		} else {
			p_os.setValue(new char[]{});
		}
	}

	/**
	 * Stores the current contents of the buffer to variable p_cs.
	 *
	 * @param p_cs the variable to store the contents of the buffer into.
	 * */
	public void get_string(final TitanCharString p_cs) {
		p_cs.cleanUp();
		if (buf_len > 0) {
			final StringBuilder str = new StringBuilder();
			for (int i = 0; i < buf_len; i++) {
				str.append(data_ptr[i]);
			}
			p_cs.assign(str.toString());
		} else {
			p_cs.assign("");
		}
	}

	/**
	 * Stores the current contents of the buffer to variable p_cs.
	 *
	 * @param p_cs the variable to store the contents of the buffer into.
	 * */
	public void get_string(final TitanUniversalCharString p_cs) {
		p_cs.cleanUp();
		if (buf_len > 0) {
			// TODO what if not multiple of 4 ?
			List<TitanUniversalChar> data = new ArrayList<TitanUniversalChar>(data_ptr.length / 4);
			for (int i = 0; i < buf_len / 4; i++) {
				data.add(new TitanUniversalChar(data_ptr[4 * i], data_ptr[4 * i + 1], data_ptr[4 * i + 2], data_ptr[4 * i + 3]));
			}
			p_cs.setValue(data);
		} else {
			p_cs.assign("");
		}
	}

	/** Cuts the bytes between the beginning of the buffer and the read position.
	 *  After that the read position will point to the beginning of the updated buffer.
	 */
	public void cut() {
		if (buf_pos > 0) {
			if (buf_pos > buf_len) {
				TTCN_EncDec_ErrorContext.error_internal("Read pointer points beyond the buffer end when cutting from a TTCN_Buffer.");
			}
			int new_len = buf_len - buf_pos;
			if (new_len > 0) {
				final char[] data;
				int new_size = get_memory_size(new_len);
				if (new_size < data_ptr.length) {
					data = new char[new_size];
				} else {
					data = new char[data_ptr.length];
				}  // FIXME: originally memmove
				System.arraycopy(data_ptr, buf_pos, data, 0, new_len);
				data_ptr = data;
			} else {
				data_ptr = null;
			}
			buf_len = new_len;
		}
		reset_buffer();
	}

	/** Cuts the bytes between the read position and the end of the buffer.
	 * The read position remains unchanged (i.e. it will point to the end
	 * of the truncated buffer. */
	public void cut_end() {
		if (buf_pos > buf_len) {
			TTCN_EncDec_ErrorContext.error_internal("Read pointer points beyond the buffer end when cutting from a TTCN_Buffer.");
		}

		if (buf_pos < buf_len) {
			if (buf_pos > 0) {
				if (data_ptr == null) {
					TTCN_EncDec_ErrorContext.error_internal("Data pointer is NULL when cutting from a TTCN_Buffer.");
				}

				int new_size = get_memory_size(buf_pos);
				if (new_size < data_ptr.length) {
					final char[] helper = new char[new_size];
					System.arraycopy(data_ptr, 0, helper, 0, new_size);
					data_ptr = helper;
				}
			} else {
				data_ptr = null;
			}

			buf_len = buf_pos;
		}

		last_bit_pos = 0;
		last_bit_bitpos = 0;
		start_of_ext_bit = 0;
		last_bit = false;
		current_bitorder = false;
		ext_bit_reverse = false;
		ext_level = 0;
	}

	/** Returns whether the buffer (beginning from the read position)
	 * contains a complete TLV. */
	public boolean contains_complete_TLV() {
		throw new TtcnError("contains_complete_TLV() for TTCN_Buffer is not implemented!");
	}

	public void log() {
		TtcnLogger.log_event_str(MessageFormat.format("Buffer: size: {0}, pos: {1}, len: {2} data: (", data_ptr.length, buf_pos, buf_len));
		if (buf_len > 0) {
			for (int i = 0; i < buf_pos; i++) {
				TtcnLogger.log_octet(data_ptr[i]);
			}
			TtcnLogger.log_event_str(" | ");
			for (int i = buf_pos; i < buf_len; i++) {
				TtcnLogger.log_octet(data_ptr[i]);
			}
		}
		TtcnLogger.log_char(')');
	}

	/** Puts a bit string in the buffer. Use only this function if you use the buffer as bit buffer.
	 *
	 * @param len number of bits to write
	 * @param s char list
	 * @param coding_par
	 * @param align alignment length
	 */
	public void put_b(int len, char[] s, final RAW_coding_par coding_par, int align) {
		char[] st, st2;
		int loc_align = align < 0 ? -align : align;
		boolean must_align = false;
		raw_order_t local_bitorder = coding_par.bitorder;
		raw_order_t local_fieldorder = coding_par.fieldorder;

		if (current_bitorder) {
			if (local_bitorder == raw_order_t.ORDER_LSB) {
				local_bitorder = raw_order_t.ORDER_MSB;
			} else {
				local_bitorder = raw_order_t.ORDER_LSB;
			}
			if (local_fieldorder == raw_order_t.ORDER_LSB) {
				local_fieldorder = raw_order_t.ORDER_MSB;
			} else {
				local_fieldorder = raw_order_t.ORDER_LSB;
			}
		}
		if (align != 0) {
			if ((local_fieldorder == raw_order_t.ORDER_LSB && (local_bitorder != coding_par.byteorder)) ||
					(local_fieldorder == raw_order_t.ORDER_MSB && (local_bitorder == coding_par.byteorder))) {
				st = new char[(len + loc_align + 7) / 8];
				if (align > 0) {
					System.arraycopy(s, 0, st, 0, (len + 7) / 8);
					if (len % 8 != 0) {
						st[(len + 7) / 8 - 1] &= RAW.BitMaskTable[len % 8];
					}
				} else {
					if (loc_align % 8 != 0) {
						final int bit_bound = loc_align % 8;
						final int max_index = (len + loc_align + 7) / 8 - loc_align / 8 - 1;
						char[] ptr = new char[st.length - loc_align / 8];
						System.arraycopy(st, loc_align / 8, ptr, 0, st.length - loc_align / 8);
						final int mask = RAW.BitMaskTable[bit_bound];
						for (int a = 0; a < (len + 7) / 8; a++) {
							ptr[a] &= mask;
							ptr[a] |= s[a] << (8 - bit_bound);
							if (a < max_index) {
								ptr[a + 1] = (char) (s[a] >> bit_bound);
							}
						}
						System.arraycopy(ptr, 0 , st, loc_align / 8, ptr.length);
					} else {
						System.arraycopy(s, 0, st, loc_align / 8, (len + 7) / 8);
					}
				}
				s = st;
				len += loc_align;
			} else {
				if (coding_par.byteorder == raw_order_t.ORDER_MSB) {
					align = -align;
				}
				if (align < 0) {
					put_zero(loc_align, local_fieldorder);
				} else {
					must_align = true;
				}
			}
		}

		if (len == 0) {
			if (must_align) {
				put_zero(loc_align, local_fieldorder);
			}
			return;
		}

		int new_size = ((bit_pos == 0 ? buf_len * 8 : buf_len * 8 - (8 - bit_pos)) + len + 7) / 8;
		int new_bit_pos = (bit_pos + len) % 8;
		if (new_size > buf_len) {
			increase_size(new_size - buf_len);
		} else {
			// copy_memory();
		}

		// System.out.println("buf_len: "+buf_len +" bit_pos: "+bit_pos+"\r\n");
		// System.out.println("new_size: "+new_size+" new_bit_pos: "+new_bit_pos+"\r\n");

		if (coding_par.hexorder == raw_order_t.ORDER_MSB) {
			st2 = new char[(len + 7) / 8];
			if (bit_pos == 4) {
				st2[0] = s[0];
				for (int a = 1; a < (len + 7) / 8; a++) {
					char ch = '\0';
					ch |= s[a - 1] >> 4;
					st2[a - 1] = (char) ((st2[a - 1] & 0x0f) | ((s[a] << 4) & 0xf0));
					st2[a] = (char) ((s[a] & 0xf0) | ch);
				}
			} else {
				for (int a = 0; a < (len + 7) / 8; a++) {
					st2[a] = (char) (((s[a] << 4) & 0xf0) | (s[a] >> 4));
				}
				if (len % 8 != 0) {
					st2[(len + 7) / 8] >>= 4;
				}
			}
			s = st2;
		}

		if (bit_pos + len <= 8) { // there is enough space within 1 octet to store the data
			if (local_bitorder == raw_order_t.ORDER_LSB) {
				if (local_fieldorder == raw_order_t.ORDER_LSB) {
					data_ptr[new_size - 1] = (char) ((data_ptr[new_size - 1] & RAW.BitMaskTable[bit_pos]) | (s[0] << bit_pos) & 0xff) ;
				} else {
					data_ptr[new_size - 1] = (char) ((data_ptr[new_size - 1] & ~RAW.BitMaskTable[8 - bit_pos]) | ((s[0] & RAW.BitMaskTable[len]) << (8 - bit_pos - len)));
				}
			} else {
				if (local_fieldorder == raw_order_t.ORDER_LSB) {
					data_ptr[new_size - 1] = (char) ((data_ptr[new_size - 1] & RAW.BitMaskTable[bit_pos]) | (RAW.REVERSE_BITS(s[0]) >> (8 - len - bit_pos)));
				} else {
					data_ptr[new_size - 1] = (char) ((data_ptr[new_size - 1] & ~RAW.BitMaskTable[8 - bit_pos]) | (RAW.REVERSE_BITS(s[0] & RAW.BitMaskTable[len]) >> bit_pos));
				}
			}
		} else if (bit_pos == 0 && (len % 8) == 0) { // octet aligned data
			if (coding_par.byteorder == raw_order_t.ORDER_LSB) {
				if (local_bitorder == raw_order_t.ORDER_LSB) {
					System.arraycopy(s, 0, data_ptr, buf_len, len / 8);
				} else {
					for (int a = 0; a < len / 8; a++) {
						data_ptr[a + buf_len] = (char) RAW.REVERSE_BITS(s[a]);
					}
				}
			} else {
				if (local_bitorder == raw_order_t.ORDER_LSB) {
					for (int a = 0, b = len / 8 - 1; a < len / 8; a++, b--) {
						data_ptr[a + buf_len] = s[b];
					}
				} else {
					for (int a = 0, b = len / 8 - 1; a < len / 8; a++, b--) {
						data_ptr[a + buf_len] = (char) RAW.REVERSE_BITS(s[b]);
					}
				}
			}
		} else {
			int maxindex = new_size - 1;
			if (coding_par.byteorder == raw_order_t.ORDER_LSB) {
				if (local_bitorder == raw_order_t.ORDER_LSB) {
					if (bit_pos != 0) {
						int mask1 = RAW.BitMaskTable[bit_pos];
						final int offset = buf_len == 0 ? 0 : buf_len - 1;
						if (local_fieldorder == raw_order_t.ORDER_MSB) {
							int num_bytes = (len + 7) / 8;
							int active_bits_in_last = len % 8;
							if (active_bits_in_last == 0) {
								active_bits_in_last = 8;
							}
							for (int a = 0; a < num_bytes; a++) {
								data_ptr[a + offset] &= RAW.REVERSE_BITS(mask1);
								char sa = s[a];
								if (a == num_bytes - 1) { // last byte
									sa <<= (8 - active_bits_in_last);
									// push bits up so the first active bit is in MSB
								}
								data_ptr[a + offset] |= (sa >> bit_pos) & ~RAW.REVERSE_BITS(mask1);
								if (a < maxindex) {
									data_ptr[a + offset + 1] = (char) (sa << (8 - bit_pos));
								}
							}
						} else {
							for (int a = 0; a < (len + 7) / 8; a++) {
								data_ptr[a + offset] &= mask1;
								data_ptr[a + offset] |= (s[a] << bit_pos) & 0xFF;
								if (a < maxindex) {
									data_ptr[a + offset + 1] = (char) (s[a] >> (8 - bit_pos));
								}
							}
						}
					} else {  // start from octet boundary
						System.arraycopy(s, 0, data_ptr, buf_len, (len + 7) / 8);
						if (local_fieldorder == raw_order_t.ORDER_MSB && new_bit_pos != 0) {
							data_ptr[new_size - 1] <<= (8 - new_bit_pos);
						}
					}
				} else { // bitorder==ORDER_MSB
					if (bit_pos != 0) {
						int mask1 = RAW.REVERSE_BITS(RAW.BitMaskTable[bit_pos]);
						final int offset = buf_len == 0 ? 0 : buf_len - 1;
						if (local_fieldorder == raw_order_t.ORDER_MSB) {
							data_ptr[offset] &= mask1;
							data_ptr[offset] |= RAW.REVERSE_BITS(s[0]) >> bit_pos;
							data_ptr[offset + 1] = (char) (RAW.REVERSE_BITS(s[0]) << (8 - bit_pos));
						} else {
							data_ptr[offset] &= RAW.REVERSE_BITS(mask1);
							data_ptr[offset] |= RAW.REVERSE_BITS(s[0]) & ~RAW.REVERSE_BITS(mask1);
							data_ptr[offset + 1] = (char) (RAW.REVERSE_BITS(s[0]) << (8 - bit_pos));
						}
						for (int a = 1; a < (len + 7) / 8; a++) {
							data_ptr[a + offset] &= mask1;
							data_ptr[a + offset] |= RAW.REVERSE_BITS(s[a]) >> bit_pos;
							if (a < maxindex) {
								data_ptr[a + offset + 1] = (char) (RAW.REVERSE_BITS(s[a]) << (8 - bit_pos));
							}
						}
					} else {  // start from octet boundary
						for (int a = 0; a < (len + 7) / 8; a++) {
							data_ptr[a + buf_len] = (char) RAW.REVERSE_BITS(s[a]);
						}
					}

					if (local_fieldorder == raw_order_t.ORDER_LSB && new_bit_pos != 0) {
						data_ptr[new_size - 1] >>= (8 - new_bit_pos);
					}
				}
			} else { // byteorder==ORDER_MSB
				if (local_bitorder == raw_order_t.ORDER_LSB) {
					if (bit_pos != 0) {
						int mask1 = RAW.BitMaskTable[bit_pos];
						char ch = get_byte_rev(s, len, 0);
						final int offset = buf_len == 0 ? 0 : buf_len - 1;
						if (local_fieldorder == raw_order_t.ORDER_MSB) {
							data_ptr[offset] &= RAW.REVERSE_BITS(mask1);
							data_ptr[offset] |= ch >> bit_pos;
							data_ptr[offset + 1] = (char) (ch << (8 - bit_pos));
						} else {
							data_ptr[offset] &= mask1;
							data_ptr[offset] |= ch & ~mask1;
							data_ptr[offset + 1] = (char) (ch << (8 - bit_pos));
						}

						for (int a = 1; a < (len + 7) / 8; a++) {
							ch = get_byte_rev(s, len, a);
							data_ptr[a + offset] &= RAW.REVERSE_BITS(mask1);
							data_ptr[a + offset] |= ch >> bit_pos;
							if (a < maxindex) {
								data_ptr[a + offset + 1] = (char) (ch << (8 - bit_pos));
							}
						}
					} else {
						for (int a = 0; a < (len + 7) / 8; a++) {
							data_ptr[a + buf_len] = get_byte_rev(s, len, a);
						}
					}
					if (local_fieldorder == raw_order_t.ORDER_LSB && new_bit_pos != 0) {
						data_ptr[new_size - 1] >>= (8 - new_bit_pos);
					}
				} else {  // bitorder==ORDER_MSB
					if (bit_pos != 0) {
						int mask1 = RAW.BitMaskTable[bit_pos];
						char ch = get_byte_rev(s, len, 0);
						final int offset = buf_len == 0 ? 0 : buf_len - 1;
						if (local_fieldorder == raw_order_t.ORDER_MSB) {
							data_ptr[offset] &= RAW.REVERSE_BITS(mask1);
							data_ptr[offset] |= RAW.REVERSE_BITS(ch) & ~RAW.REVERSE_BITS(mask1);
							data_ptr[offset + 1] = (char) (RAW.REVERSE_BITS(ch) >> (8 - bit_pos));
						} else {
							data_ptr[offset] &= mask1;
							data_ptr[offset] |= RAW.REVERSE_BITS(ch) << bit_pos;
							data_ptr[offset] &= 0xff;
							data_ptr[offset + 1] = (char) (RAW.REVERSE_BITS(ch) >> (8 - bit_pos));
						}

						for (int a = 1; a < (len + 7) / 8; a++) {
							ch = get_byte_rev(s, len, a);
							data_ptr[offset + a] &= mask1;
							data_ptr[offset + a] |= RAW.REVERSE_BITS(ch) << bit_pos;
							if (a < maxindex) {
								data_ptr[offset + a + 1] = (char) (RAW.REVERSE_BITS(ch) >> (8 - bit_pos));
							}
						}
					} else { // start from octet boundary
						for (int a = 0; a < (len + 7) / 8; a++) {
							data_ptr[a + buf_len] = (char) RAW.REVERSE_BITS(get_byte_rev(s, len, a));

						}
					}

					if (local_fieldorder == raw_order_t.ORDER_MSB && new_bit_pos != 0) {
						data_ptr[new_size - 1] <<= (8 - new_bit_pos);
					}
				}
			}
		}

		buf_len = new_size;
		bit_pos = new_bit_pos;
		if (bit_pos != 0) {
			last_bit_pos = buf_len - 1;
			if (local_fieldorder == raw_order_t.ORDER_LSB) {
				last_bit_bitpos = bit_pos - 1;
			} else {
				last_bit_bitpos = 7 - (bit_pos - 1);
			}
		} else {
			last_bit_pos = buf_len - 1;
			if (local_fieldorder == raw_order_t.ORDER_LSB) {
				last_bit_bitpos = 7;
			} else {
				last_bit_bitpos = 0;
			}
		}
		if (must_align) {
			put_zero(loc_align, local_fieldorder);
		}
	}

	/** Reads a bit string from the buffer. Use only this function if you use the buffer as bit buffer.
	 * @param len
	 * @param s
	 * @param coding_par
	 * @param top_bit_order
	 *  */
	public void get_b(final int len, final char[] s, final RAW_coding_par coding_par,final raw_order_t top_bit_order) {
		if (len == 0) {
			return;
		}

		int new_buf_pos = buf_pos + (bit_pos + len) / 8;
		int new_bit_pos = (bit_pos + len) % 8;
		raw_order_t local_bitorder = coding_par.bitorder;
		raw_order_t local_fieldorder = coding_par.fieldorder;

		if (top_bit_order == raw_order_t.ORDER_LSB) {
			if (local_bitorder == raw_order_t.ORDER_LSB) {
				local_bitorder = raw_order_t.ORDER_MSB;
			} else {
				local_bitorder = raw_order_t.ORDER_LSB;
			}

			if (local_fieldorder == raw_order_t.ORDER_LSB) {
				local_fieldorder = raw_order_t.ORDER_MSB;
			} else {
				local_fieldorder = raw_order_t.ORDER_LSB;
			}
		}

		if (bit_pos + len <= 8) { // the data is within 1 octet
			if (local_bitorder == raw_order_t.ORDER_LSB) {
				if (local_fieldorder == raw_order_t.ORDER_LSB) {
					s[0] = (char) (data_ptr[buf_pos] >> bit_pos);
				} else {
					s[0] = (char) (data_ptr[buf_pos] >> (8 - bit_pos - len));
				}
			} else {
				if (local_fieldorder == raw_order_t.ORDER_LSB) {
					s[0] = (char) (RAW.REVERSE_BITS(data_ptr[buf_pos]) >> (8 - bit_pos - len));
				} else {
					s[0] = (char) (RAW.REVERSE_BITS(data_ptr[buf_pos]) >> bit_pos);
				}
			}
		} else if (bit_pos == 0 && (len % 8) == 0) { // octet aligned data
			if (coding_par.byteorder == raw_order_t.ORDER_LSB) {
				if (local_bitorder == raw_order_t.ORDER_LSB) {
					System.arraycopy(data_ptr, buf_pos, s, 0, len / 8);
				} else {
					for (int a = 0; a < len / 8; a++) {
						s[a] = (char) RAW.REVERSE_BITS(data_ptr[a + buf_pos]);
					}
				}
			} else {
				if (local_bitorder == raw_order_t.ORDER_LSB) {
					for (int a = 0, b = len / 8 - 1; a < len / 8; a++, b--) {
						s[a] = data_ptr[b + buf_pos];
					}
				} else {
					for (int a = 0, b = len / 8 - 1; a < len / 8; a++, b--) {
						s[a] = (char) RAW.REVERSE_BITS(data_ptr[b + buf_pos]);
					}
				}
			}
		} else { // unaligned
			final int num_bytes = (len + 7) / 8;
			if (coding_par.byteorder == raw_order_t.ORDER_LSB) {
				if (local_bitorder == raw_order_t.ORDER_LSB) {
					if (bit_pos != 0) {
						int mask1 = RAW.BitMaskTable[8 - bit_pos];
						if (local_fieldorder == raw_order_t.ORDER_LSB) {
							for (int a = 0; a < num_bytes; a++) {
								s[a] = (char) ((get_byte_align(len, local_fieldorder, raw_order_t.ORDER_MSB, a + 1) << (8 - bit_pos)) & 0xFF |
										((get_byte_align(len, local_fieldorder, raw_order_t.ORDER_MSB,a) >> bit_pos) & mask1 ));
							}
						} else {
							mask1 = RAW.BitMaskTable[bit_pos];
							for (int a = 0; a < num_bytes; a++) {
								s[a] = (char) ((get_byte_align(len, local_fieldorder, raw_order_t.ORDER_LSB, a + 1) >> (8 - bit_pos) & mask1) |
										((get_byte_align(len, local_fieldorder, raw_order_t.ORDER_LSB, a) << bit_pos)) & 0xFF);
							}
							int active_bits_in_last_byte = len % 8;
							if (active_bits_in_last_byte != 0) {
								s[num_bytes - 1] >>= (8 - active_bits_in_last_byte);
							}
						}
					} else {  // start from octet boundary
						System.arraycopy(data_ptr, buf_pos, s, 0, num_bytes);
						if (local_fieldorder == raw_order_t.ORDER_MSB && new_bit_pos != 0) {
							s[num_bytes - 1] >>= (8 - new_bit_pos);
						}
					}
				} else { // bitorder == ORDER_MSB
					if (bit_pos != 0){
						int mask1 = RAW.BitMaskTable[bit_pos];
						for (int a = 0; a < num_bytes; a++) {
							s[a] = (char) RAW.REVERSE_BITS(((get_byte_align(len,local_fieldorder,raw_order_t.ORDER_LSB,a + 1) >> (8 - bit_pos)) & mask1) |
									(get_byte_align(len,local_fieldorder,raw_order_t.ORDER_LSB,a) << bit_pos) & 0xFF);
						}
					} else {  // start from octet boundary
						for (int a = 0; a < num_bytes; a++) {
							s[a] = (char) RAW.REVERSE_BITS(data_ptr[a + buf_pos]);
						}

						if (local_fieldorder == raw_order_t.ORDER_LSB && new_bit_pos != 0) {
							s[num_bytes - 1] >>= (8 - new_bit_pos);
						}
					}
				}
			} else { // byteorder==ORDER_MSB
				if (local_bitorder == raw_order_t.ORDER_LSB) {
					if (new_bit_pos != 0) {
						int mask1 = RAW.BitMaskTable[new_bit_pos];
						for (int a = 0, b = (bit_pos + len) / 8; a < num_bytes; a++, b--) {
							if(get_byte_align(len,local_fieldorder,raw_order_t.ORDER_LSB,b) < 16) {
								s[a] = get_byte_align(len,local_fieldorder,raw_order_t.ORDER_LSB,b);
							} else {
								s[a] = (char) (((get_byte_align(len,local_fieldorder,raw_order_t.ORDER_LSB,b) >> (8 - new_bit_pos)) & mask1) |
										(get_byte_align(len,local_fieldorder,raw_order_t.ORDER_LSB,b - 1) << new_bit_pos) & 0xFF);
							}
						}
					} else {
						for (int a = 0, b = new_buf_pos - 1; a < num_bytes; a++, b--) {
							s[a] = data_ptr[b];
						}
						if (local_fieldorder == raw_order_t.ORDER_LSB && bit_pos != 0) {
							s[num_bytes - 1] >>= bit_pos;
						}
					}
				} else {  // bitorder==ORDER_MSB
					if (new_bit_pos != 0) {
						for (int a = 0, b = (bit_pos + len) / 8; a < num_bytes; a++, b--) {
							s[a] = (char) RAW.REVERSE_BITS((get_byte_align(len,local_fieldorder,raw_order_t.ORDER_MSB,b) << (8 - new_bit_pos)) |
									(get_byte_align(len,local_fieldorder,raw_order_t.ORDER_MSB,b - 1) >> new_bit_pos) & 0xFF);
						}
					} else {  // start from octet boundary
						for (int a = 0, b = new_buf_pos - 1; a < num_bytes; a++, b--) {
							s[a] = (char) RAW.REVERSE_BITS(data_ptr[b]);
						}
						if (local_fieldorder == raw_order_t.ORDER_MSB && bit_pos != 0) {
							s[num_bytes - 1] >>= bit_pos;
						}
					}
				}
			}
		}

		if (coding_par.hexorder == raw_order_t.ORDER_MSB) {
			if (bit_pos == 4) {
				for (int a = 1; a < (len + 7) / 8; a++) {
					char ch = '\0';
					ch |= s[a - 1] >> 4;
					s[a - 1] = (char) ((s[a - 1] & 0x0f) | (s[a] << 4));
					s[a] = (char) ((s[a] & 0xf0) | ch);
				}
			} else {
				for (int a = 0; a < (len + 7) / 8; a++) {
					s[a] = (char) ((s[a] << 4) | (s[a] >> 4));
				}
				if (len % 8 != 0) {
					s[(len + 7) / 8] >>= 4;
				}
			}
		}

		int last_bit_offset = bit_pos + len - 1;
		char last_bit_octet = data_ptr[buf_pos + last_bit_offset / 8];
		if (local_fieldorder == raw_order_t.ORDER_LSB) {
			last_bit_octet >>= last_bit_offset % 8;
		} else {
			last_bit_octet >>= 7 - last_bit_offset % 8;
		}
		if ((last_bit_octet & 0x01) != 0) {
			last_bit = true;
		} else {
			last_bit = false;
		}

		buf_pos = new_buf_pos;
		bit_pos = new_bit_pos;
	}

	/** Puts
	 * @param len number of zeros in the buffer.
	 * @param fieldorder
	 */
	public void put_zero(final int len, final raw_order_t fieldorder) {
		if (len == 0) {
			return;
		}

		final int new_size = ((bit_pos == 0 ? buf_len * 8 : buf_len * 8 - (8 - bit_pos)) + len + 7) / 8;
		if (new_size > buf_len) {
			increase_size(new_size - buf_len);
		}

		if (bit_pos != 0) {
			if (bit_pos + len > 8) {
				int mask1 = RAW.BitMaskTable[bit_pos];
				final int offset = buf_len == 0 ? 0 : buf_len - 1;

				if (fieldorder == raw_order_t.ORDER_LSB) {
					data_ptr[offset] &= mask1;
				} else {
					data_ptr[offset] &= ~mask1;
				}

				for (int i = 1; i < (len - 1 + bit_pos) / 8; i++) {
					data_ptr[i + offset] = 0;
				}
			} else {
				if (fieldorder == raw_order_t.ORDER_LSB) {
					data_ptr[new_size - 1] = (char) (data_ptr[new_size - 1] & RAW.BitMaskTable[bit_pos]);
				} else {
					data_ptr[new_size - 1] = (char) (data_ptr[new_size - 1] & RAW.REVERSE_BITS(RAW.BitMaskTable[bit_pos]));
				}
			}
		} else {
			for (int i = buf_len; i < (len + 7) / 8; i++) {
				data_ptr[i] = 0;
			}
		}

		buf_len = new_size;
		bit_pos = (bit_pos + len) % 8;

		if (bit_pos != 0) {
			last_bit_pos = buf_len - 1;
			if (fieldorder == raw_order_t.ORDER_LSB) {
				last_bit_bitpos = bit_pos - 1;
			} else {
				last_bit_bitpos = 7 - (bit_pos - 1);
			}
		} else {
			last_bit_pos = buf_len - 1;
			if (fieldorder == raw_order_t.ORDER_LSB) {
				last_bit_bitpos = 7;
			} else {
				last_bit_bitpos = 0;
			}
		}
	}

	/** Get data of buffer and modify bitpos value. 
	 * @param bitpos
	 * @return a char array of the bitstring within first the octet.
	 * */
	public char[] get_read_data(final AtomicInteger bitpos) {
		bitpos.set(bit_pos);
		if (data_ptr != null) {
			return get_data();
		}
		return null;
	}

	/** Sets the (reading) position to pos and the bit position to
	 * bit_pos, or to the end of buffer, if pos > len. 
	 * @param pos
	 * @param bitpos
	 * */
	public void set_pos(final int pos, final int bitpos) {
		buf_pos = pos < buf_len ? pos : buf_len;
		bit_pos = bitpos;
	}

	/** Sets the (reading) position to new_bit_pos or to the end of buffer, if new_bit_pos > len. 
	 * @param new_bit_pos
	 * */
	public void set_pos_bit(final int new_bit_pos) {
		int new_pos = new_bit_pos / 8;
		if (new_pos < buf_len) {
			buf_pos = new_pos;
			bit_pos = new_bit_pos % 8;
		} else {
			buf_pos = buf_len;
			bit_pos = 0;
		}
	}

	/** @return the (reading) position of the buffer in bits. */
	public int get_pos_bit() {
		return buf_pos * 8 + bit_pos;
	}

	/** Increases the (reading) position by delta bits, or sets it to
	 * the end of buffer, if get_pos() + delta > len. 
	 * @param delta
	 * */
	public void increase_pos_bit(final int delta) {
		int new_buf_pos = buf_pos + (bit_pos + delta) / 8; // bytes
		if (new_buf_pos < buf_pos || new_buf_pos > buf_len) {
			buf_pos = buf_len;
			bit_pos = 7;
		} else {
			buf_pos = new_buf_pos;
			bit_pos = (bit_pos + delta) % 8;
		}
	}

	/** Increases the (reading) position to a multiple of padding.
	 *  @param padding
	 *  @return the number of bits used up.
	 */
	public int increase_pos_padd(final int padding) {
		if (padding != 0) { // <---old bit pos--->
			int new_bit_pos = ((buf_pos * 8 + bit_pos + padding - 1) / padding) * padding;
			int padded = new_bit_pos - buf_pos * 8 - bit_pos;
			//  padded = bits skipped to reach the next multiple of padding (bits)
			buf_pos = new_bit_pos / 8;
			bit_pos = new_bit_pos % 8;
			return padded;
		}
		return 0;
	}

	/** @return the number of bits remaining in the buffer */
	public int unread_len_bit() {
		return (buf_len - buf_pos) * 8 - bit_pos;
	}

	/** Mark the start of extension bit processing during encoding. 
	 * @param p_reverse*/
	public void start_ext_bit(final boolean p_reverse) {
		if (ext_level++ == 0) {
			start_of_ext_bit = buf_len;
			ext_bit_reverse = p_reverse;
		}
	}

	/** Apply the extension bit to the encoded bytes. */
	public void stop_ext_bit() {
		if (ext_level <= 0) {
			TTCN_EncDec_ErrorContext.error_internal("TTCN_Buffer::stop_ext_bit() was called without start_ext_bit().");
		}

		if (--ext_level == 0) {
			int one = current_bitorder ? 0x01 : 0x80;
			int zero = ~one;

			if (ext_bit_reverse) {
				for (int a = start_of_ext_bit; a < buf_len - 1; a++) {
					data_ptr[a] |= one;
				}
				data_ptr[buf_len - 1] &= zero;
			} else {
				for (int a = start_of_ext_bit; a < buf_len - 1; a++) {
					data_ptr[a] &= zero;
				}
				data_ptr[buf_len - 1] |= one;
			}
		}
	}

	/**
	 * @return current_bitorder
	 * */
	public boolean get_order() {
		return current_bitorder;
	}

	/** Sets current_bitorder to the new_order
	 * @param new_order
	 * */
	public void set_order(final boolean new_order) {
		current_bitorder = new_order;
	}

	/**  Appends s to the buffer.
	 * @param len
	 * @param s
	 * @param pat_len
	 * @param fieldorder
	 */
	public void put_pad(final int len, final char[] s, final int pat_len, final raw_order_t fieldorder) {
		if (len == 0) {
			return;
		}
		if (pat_len == 0) {
			put_zero(len,fieldorder);
			return;
		}
		final RAW_coding_par cp = new RAW_coding_par(raw_order_t.ORDER_LSB,raw_order_t.ORDER_LSB,
				raw_order_t.ORDER_LSB, fieldorder);
		int length = len;
		while (length > 0) {
			put_b(length > pat_len ? pat_len : length, s, cp, 0);
			length -= pat_len;
		}
	}

	/** Appends s to the buffer. 
	 * 
	 * @param len 
	 * @param s
	 * @param pat_len pice length
	 * @param fieldorder
	 */
	public void put_pad(final int len, final String s, final int pat_len, final raw_order_t fieldorder) {
		if (len == 0) {
			return;
		}
		if (pat_len == 0) {
			put_zero(len,fieldorder);
			return;
		}
		final RAW_coding_par cp = new RAW_coding_par(raw_order_t.ORDER_LSB,raw_order_t.ORDER_LSB,
				raw_order_t.ORDER_LSB, fieldorder);

		char[] str = new char[s.length()];//TODO maybe can be faster
		for (int i = 0; i < s.length(); i++) {
			str[i] = s.charAt(i);
		}

		int length = len;
		while (length > 0) {
			put_b(length > pat_len ? pat_len : length, str, cp, 0);
			length -= pat_len;
		}
	}

	/** Sets data the last bit position
	 * @param p_last_bit 
	 * */
	public void set_last_bit(final boolean p_last_bit) {
		int bitmask = 0x01 << last_bit_bitpos;
		if (p_last_bit) {
			data_ptr[last_bit_pos] |= bitmask;
		}
		else {
			data_ptr[last_bit_pos] &= ~bitmask;
			data_ptr[last_bit_pos] &= 0xff;
		}
	}

	/**@return last bit*/
	public boolean get_last_bit() {
		return last_bit;
	}

	/**
	 * 
	 * @param data
	 * @param len
	 * @param idx
	 * @return
	 */
	private static char get_byte_rev(final char[] data,final int len,final int idx) {
		char ch = '\0';
		final int hossz = (len + 7) / 8 - 1;
		if (idx > hossz) {
			return ch;
		}
		final int bit_limit = len % 8;
		if (bit_limit == 0) {
			return data[hossz - idx];
		}
		ch = (char) (data[hossz - idx] << (8 - bit_limit));
		if ((hossz - idx) > 0) {
			ch |= (data[hossz - idx - 1] >> bit_limit) & RAW.BitMaskTable[8 - bit_limit];
		}
		return ch;
	}

	/**
	 * @param len
	 * @param fieldorder
	 * @param req_align
	 * @param index
	 * @return
	 */
	private char get_byte_align(final int len, final raw_order_t fieldorder, final raw_order_t req_align, final int index) {
		if (index < 0 || index > (bit_pos + len) / 8 || data_ptr == null) {
			return '\0';
		}

		if (index == 0) { // first byte
			if (fieldorder == req_align) {
				if (fieldorder == raw_order_t.ORDER_LSB) {
					return (char) (data_ptr[buf_pos] >> bit_pos);
				}
				return (char) (data_ptr[buf_pos] << bit_pos);
			}
			return data_ptr[buf_pos];
		}

		if (index == (bit_pos + len) / 8) { // last byte
			if (fieldorder == req_align) {
				if (fieldorder == raw_order_t.ORDER_LSB) {
					return (char) (data_ptr[buf_pos + index] << (8 - (bit_pos + len) % 8));
				}
				return (char) (data_ptr[buf_pos + index] >> (8 - (bit_pos + len) % 8));
			}
			return data_ptr[buf_pos + index];
		}

		return data_ptr[buf_pos + index];
	}
}
