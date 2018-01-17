/******************************************************************************
 * Copyright (c) 2000-2017 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

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
		int  new_buf_pos = buf_pos + size_incr;
		if (new_buf_pos < buf_pos || new_buf_pos > buf_len) {
			buf_pos = buf_len;
		} else {
			buf_pos = new_buf_pos;
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
			data_ptr = new char[buf_len];
			System.arraycopy(p_buf.data_ptr, 0, data_ptr, 0, p_buf.buf_len);
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

	public char[] get_read_data() {
		if (data_ptr != null) {
			char[] result = new char[buf_len - buf_pos];
			System.arraycopy(data_ptr, buf_pos, result, 0, buf_len - buf_pos);
			return result;
		}
		return null;
	}

	/**
	 * @return The (reading) position of the buffer.
	 * */
	public int get_pos() {
		return buf_pos;
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
			for (int i = 0; i < length; i++) {
				data_ptr[buf_len+i] = cstr[i];
			}
			buf_len += length; 
		}
	}

	// TODO: implement functions

	/** Appends the contents of octetstring \a p_os to the buffer. */
	void put_string(final TitanOctetString p_os) {
		//FIXME: implement
	}

	/** Same as \a put_string(). Provided only for backward compatibility. */
	void put_os(final TitanOctetString p_os) { 
		put_string(p_os);
	}

	/** Appends the contents of charstring \a p_cs to the buffer. */
	public char[] put_string(final TitanCharString p_cs) {
		// TODO Auto-generated method stub
		return null;
	}

	/** Same as \a put_string(). Provided only for backward compatibility. */
	void put_cs(final TitanCharString p_cs) { 
		put_string(p_cs);
	}
	/** Appends the content of \a p_buf to the buffer */
	void put_buf(final TTCN_Buffer p_buf) {
		//FIXME: implement
	}

	/** 
	 * Stores the current contents of the buffer to variable p_os.
	 * 
	 * @param p_os the variable to store the contents of the buffer into.
	 * */
	public void get_string(final TitanOctetString p_os) {
		p_os.cleanUp();
		if (buf_len > 0) {
//			if (buf_size != buf_len) {
//				//buf_ptr = (buffer_struct*)Realloc(buf_ptr, MEMORY_SIZE(buf_len));
//				buf_size = buf_len;
//			}
			// p_os.setValue(data_ptr);
			// p_os.ref_count++;
			// p_os.n_octets = buf_len;
		} else {
			// p_os.init_struct(0);
		}
		// throw new TtcnError("get_string() for TTCN_Buffer is not implemented!");
	}

	public void get_string(final TitanCharString p_cs) {
		p_cs.cleanUp();
		if (buf_len > 0) {
			// we are the sole owner
			// Share our buffer_struct with CHARSTRING's charstring_struct
			// (they have the same layout), after putting in a string terminator.
//			if (buf_size != buf_len + 1) {
//				//buf_ptr = (buffer_struct*)Realloc(buf_ptr, MEMORY_SIZE(buf_len + 1));
//				buf_size = buf_len + 1;
//			}
			//				p_cs.val_ptr = (CHARSTRING::charstring_struct*)buf_ptr;
			//				p_cs.val_ptr->ref_count++;
			//				p_cs.val_ptr->n_chars = buf_len;
			//				p_cs.val_ptr->chars_ptr[buf_len] = '\0';
		} else {
			// p_os.init_struct(0);
		}
	}

	public void get_string(final TitanUniversalCharString p_cs) {
		p_cs.cleanUp();
		if (buf_len > 0) {
			// TODO what if not multiple of 4 ?
			//p_cs.setValue(aOtherValue);e(null);
			//final Array
			// p_cs.init_struct(buf_len / 4);
			// memcpy(p_cs.val_ptr->uchars_ptr, buf_ptr->data_ptr, buf_len);
		} else {
			// p_cs.init_struct(0);
		}
	}

	/** Cuts the bytes between the beginning of the buffer and the read
	 * position. After that the read position will point to the beginning
	 * of the updated buffer. */
	public void cut() {
		//FIXME implement
		throw new TtcnError("cut in TTCN_Buffer is not implemented!");
	}

	/** Cuts the bytes between the read position and the end of the buffer.
	 * The read position remains unchanged (i.e. it will point to the end
	 * of the truncated buffer. */
	public void cut_end() {
		throw new TtcnError("cut_end in TTCN_Buffer is not implemented!");
	}

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
	void put_b(int len, final char s, final RAW_coding_par coding_par, int align) {

	}
	/** Reads a bit string from the buffer. Use only this function if you use the buffer as bit buffer. */
	void get_b(int len, char s, final RAW_coding_par coding_par, raw_order_t top_bit_order) {}
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
	void set_pos(int pos, int bitpos) {}

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
