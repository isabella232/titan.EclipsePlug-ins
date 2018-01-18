/******************************************************************************
 * Copyright (c) 2000-2017 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.titan.runtime.core.RAW.RAW_coding_par;
import org.eclipse.titan.runtime.core.TTCN_EncDec.raw_order_t;

/**
 * Buffer used by the different encoders/decoders.
 * 
 * FIXME the current implementation is only a placeholder to mark the architectural borders.
 * @author Farkas Izabella Ingrid
 */
public class TTCN_Buffer {
	final static private int INITIAL_SIZE = 1024;
	/* The layout of this structure must match that of charstring_struct */

	private char data_ptr[];

	// The number of bytes of memory used (buf_len <= buf_size)
	private int buf_len; 

	// Read offset in bytes in the buffer (buf_pos <= buf_len)
	private int buf_pos; 

	// Read offset of the current bit inside the byte at buf_pos
	private int bit_pos;
	private int last_bit_pos;
	private int last_bit_bitpos;
	private int start_of_ext_bit;

	// the state of the "last bit" (only after get_b was called)
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

	/** Returns the smallest preferred size of the allocated memory area
	 * that is at least \a target_size. */
	private static int get_memory_size(int target_size) {
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

	/** Ensures that there are at least \a target_size writable bytes in the
	 * memory area after \a buf_len. */
	private void increase_size(int size_incr) {
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

	/** Copy constructor. */
	public TTCN_Buffer(final TTCN_Buffer p_buf) {
		data_ptr = new char[p_buf.data_ptr.length];
		System.arraycopy(p_buf.data_ptr, 0, data_ptr, 0, p_buf.data_ptr.length);
		buf_len = p_buf.buf_len;
		reset_buffer();
	}

	/** Initializes the buffer with the contents of \a p_os.
	 * @pre The argument must be bound. */
	public  TTCN_Buffer(final TitanOctetString p_os) {
		p_os.mustBound("Initializing a TTCN_Buffer with an unbound octetstring value.");

		buf_len = p_os.lengthOf().getInt();
		data_ptr = new char[buf_len];
		System.arraycopy(p_os.getValue(), 0, data_ptr, 0, buf_len);
		reset_buffer();
	}

	/** Initializes the buffer with the contents of \a p_cs.
	 * @pre The argument must be bound. */
	public TTCN_Buffer(final TitanCharString p_cs) {
		p_cs.mustBound("Initializing a TTCN_Buffer with an unbound charstring value.");

		buf_len = p_cs.lengthOf().getInt();
		data_ptr = new char[buf_len];
		for (int i = 0; i < buf_len; i++) { 
			data_ptr[i] =  p_cs.getAt(i).get_char();
		}
		reset_buffer();
	}

	/** Copies the contents of \a p_buf into \a this.
	 * The read pointers and other attributes are reset. */
	public TTCN_Buffer assign(final TTCN_Buffer p_buf) {
		if (p_buf != this) {
			buf_len = p_buf.buf_len;
			data_ptr = new char[p_buf.data_ptr.length];
			System.arraycopy(p_buf.data_ptr, 0, data_ptr, 0, p_buf.data_ptr.length);
		}
		reset_buffer();
		return this;
	}

	/** Copies the contents of \a p_os into \a this.
	 * The read pointers and other attributes are reset. */
	public  TTCN_Buffer assign(final TitanOctetString p_os) {
		p_os.mustBound("Assignment of an unbound octetstring value to a TTCN_Buffer.");
		buf_len = p_os.lengthOf().getInt();
		data_ptr = new char[buf_len];
		System.arraycopy(p_os.getValue(), 0, data_ptr, 0, buf_len);
		reset_buffer();
		return this;
	}

	/** Copies the contents of \a p_cs into \a this.
	 * The read pointers and other attributes are reset. */
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
		return data_ptr;
	}

	/** Returns how many bytes are in the buffer to read. */
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

	/** Sets the (reading) position to \a pos, or to the end of buffer,
	 * if pos > len. */
	public void set_pos(final int new_pos) {
		if (new_pos < buf_len) buf_pos = new_pos;
		else buf_pos = buf_len;
	}

	/** Increases the (reading) position by \a delta, or sets it to the
	 * end of buffer, if get_pos() + delta > len. */
	public void increase_pos(final int delta)  {
		int  new_buf_pos = buf_pos + delta;
		if (new_buf_pos < buf_pos || new_buf_pos > buf_len) {
			buf_pos = buf_len;
		} else {
			buf_pos = new_buf_pos;
		}
	}

	/** You can write up to \a end_len chars beginning from \a end_ptr;
	 * after writing, you have to call also increase_length()! Useful
	 * if you want to use memcpy. @see increase_length(). \param
	 * end_ptr out. \param end_len inout. */
	public char[] get_end() {
		final int end_len = data_ptr.length - buf_len;
		final char[] end_ptr;
		if (data_ptr != null) {
			end_ptr = new char[end_len];//buf_ptr->data_ptr + buf_len;
			System.arraycopy(data_ptr, buf_pos, end_ptr, 0, end_len);
		}
		else {
			end_ptr = null;
		}
		return end_ptr;
	}

	/** How many chars have you written after a get_end(), beginning
	 * from end_ptr. @see get_end() */
	public void increase_length(final int size_incr) {
		if (data_ptr.length < buf_len + size_incr) {
			increase_size(size_incr);
		}
		buf_len += size_incr;
	}

	/** Appends single character \a c to the buffer. */
	public void put_c(final char c) {
		increase_size(1);
		data_ptr[buf_len] = c;
		buf_len++;
	}

	/** Appends \a len bytes starting from address \a s to the buffer. */
	public void put_s(char[] cstr) {
		final int length = cstr.length;

		if (length > 0) {
			increase_size(length);
			System.arraycopy(cstr, 0, data_ptr, buf_len, length);
			buf_len += length; 
		}
	}

	/** Appends the contents of octetstring \a p_os to the buffer. */
	public void put_string(final TitanOctetString p_os) {
		p_os.mustBound("Appending an unbound octetstring value to a TTCN_Buffer.");

		final int n_octets = p_os.lengthOf().getInt();
		if ( n_octets > 0) {
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

	/** Same as \a put_string(). Provided only for backward compatibility. */
	public void put_os(final TitanOctetString p_os) { 
		put_string(p_os);
	}

	/** Appends the contents of charstring \a p_cs to the buffer. */
	public void put_string(final TitanCharString p_cs) {
		p_cs.mustBound("Appending an unbound charstring value to a TTCN_Buffer.");

		final int n_chars = p_cs.lengthOf().getInt();
		if (n_chars > 0) { // there is something in the CHARSTRING
			if (buf_len > 0) { // there is something in this buffer, append
				increase_size(n_chars);
				// memcpy(buf_ptr->data_ptr + buf_len, p_cs.val_ptr->chars_ptr,p_cs.val_ptr->n_chars);
				//System.arraycopy(p_cs.getValue(), 0, data_ptr, buf_len, n_chars);
				for (int i = 0; i < n_chars; i++ ) {
					data_ptr[buf_len + i] = p_cs.getValue().charAt(i);
				}
				buf_len += n_chars;
			} else { // share the data
				data_ptr = new char[n_chars];
				for (int i = 0; i < n_chars; i++ ) {
					data_ptr[i] = p_cs.getValue().charAt(i);
				}
				//System.arraycopy(p_cs.getValue(), 0, data_ptr, 0, n_chars);
				buf_len = n_chars;
			}
		}
	}

	/** Same as \a put_string(). Provided only for backward compatibility. */
	public void put_cs(final TitanCharString p_cs) { 
		put_string(p_cs);
	}
	/** Appends the content of \a p_buf to the buffer */
	public void put_buf(final TTCN_Buffer p_buf) {
		if (p_buf.data_ptr == null) {
			return;
		}
		if (p_buf.buf_len > 0) { // there is something in the other buffer
			if (buf_len > 0) { // there is something in this buffer, append
				increase_size(p_buf.buf_len);
				// memcpy(buf_ptr->data_ptr + buf_len, p_buf.buf_ptr->data_ptr, p_buf.buf_len);
				System.arraycopy(p_buf.data_ptr, 0, data_ptr, buf_len, p_buf.data_ptr.length);
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
	public void get_string(TitanOctetString p_os) {
		p_os.cleanUp();
		if (buf_len > 0) {
			char[] data = new char[buf_len];
			System.arraycopy(data_ptr, 0, data, 0, buf_len);
			p_os.setValue(data);
		} else {
			p_os.cleanUp();
		}
		// throw new TtcnError("get_string() for TTCN_Buffer is not implemented!");
	}

	public void get_string(TitanCharString p_cs) {
		p_cs.cleanUp();
		if (buf_len > 0) {
			final StringBuilder str = new StringBuilder();
			for (int i = 0; i < buf_len; i++) {
				str.append(data_ptr[i]);
			}
			p_cs.assign(str.toString());
		} else {
		   p_cs.cleanUp();
		}
	}

	public void get_string(TitanUniversalCharString p_cs) {
		p_cs.cleanUp();
		if (buf_len > 0) {
			// TODO what if not multiple of 4 ?
			List<TitanUniversalChar> data = new ArrayList<TitanUniversalChar>(data_ptr.length/4);
			for (int i = 0; i < buf_len/4; i++) {
				data.add(new TitanUniversalChar(data_ptr[4*i],data_ptr[4*i+1],data_ptr[4*i+2],data_ptr[4*i+3]));
			}
			p_cs.setValue(data);
		} else {
			p_cs.cleanUp();
		}
	}

	// TODO: implement functions

	/** Cuts the bytes between the beginning of the buffer and the read
	 * position. After that the read position will point to the beginning
	 * of the updated buffer. */
	void cut() {}
	/** Cuts the bytes between the read position and the end of the buffer.
	 * The read position remains unchanged (i.e. it will point to the end
	 * of the truncated buffer. */
	void cut_end() {}
	/** Returns whether the buffer (beginning from the read position)
	 * contains a complete TLV. */
	boolean contains_complete_TLV() {
		return current_bitorder;
	}

	void log() {

	}

	/** Puts a bit string in the buffer. Use only this function if you
	 * use the buffer as bit buffer.
	 *
	 * @param len number of _bits_ to write
	 * @param s pointer to the data (bytes)
	 * @param coding_par
	 * @param align alignment length (in ???)
	 */
	void put_b(int len, final char[] s, final RAW_coding_par coding_par, int align) {

	}
	/** Reads a bit string from the buffer. Use only this function if you use the buffer as bit buffer. */
	void get_b(int len, char[] s, final RAW_coding_par coding_par, raw_order_t top_bit_order) {}
	/** Puts @p len number of zeros in the buffer. */
	void put_zero(int len, raw_order_t fieldorder) {}

	/** Returns a pointer which points to read position of data and the
	 * starting position of the bitstring within first the octet. */
	char[] get_read_data(int bitpos) {
		bitpos = bit_pos;
		if (data_ptr != null) {
			return get_data();
		}
		return null;
	}

	/** Sets the (reading) position to \a pos and the bit position to \a
	 * bit_pos, or to the end of buffer, if pos > len. */
	void set_pos(int pos, int bitpos) {

	}

	/** Sets the (reading) position to \a pos
	 * or to the end of buffer, if pos > len. */
	void set_pos_bit(int new_bit_pos) {}

	/** Returns the (reading) position of the buffer in bits. */
	int get_pos_bit() { 
		return buf_pos * 8 + bit_pos;
	}

	/** Increases the (reading) position by \a delta bits, or sets it to
	 * the end of buffer, if get_pos() + delta > len. */
	void increase_pos_bit(int delta) {}

	/** Increases the (reading) position to a multiple of \p padding.
	 *  @return the number of bits used up. */
	int increase_pos_padd(int padding) {
		return padding; //FIXME implement
	}

	/** Returns the number of bits remaining in the buffer */
	int unread_len_bit() {
		return bit_pos; //FIXME: implement
	}

	/** Mark the start of extension bit processing during encoding. */
	void start_ext_bit(boolean p_reverse) {}

	/** Apply the extension bit to the encoded bytes. */
	void stop_ext_bit(){}

	boolean get_order() {
		return current_bitorder;
	}

	void set_order(boolean new_order) {
		current_bitorder = new_order; 
	}

	void put_pad(int len, final char[] s, int pat_len, raw_order_t fieldorder) {

	}

	void set_last_bit(boolean p_last_bit) {

	}

	boolean get_last_bit() { 
		return last_bit;
	}

	private static char get_byte_rev(final char[] data, int len, int index) {
		return 0;
	}

	private char get_byte_align(int len, raw_order_t fieldorder, raw_order_t req_align ,int index) {
		return 0;
	}
}
