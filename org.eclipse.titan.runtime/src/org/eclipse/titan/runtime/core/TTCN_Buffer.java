/******************************************************************************
 * Copyright (c) 2000-2017 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

/**
 * Buffer used by the different encoders/decoders.
 * 
 * FIXME the current implementation is only a placeholder to mark the architectural borders.
 *
 * @author Farkas Izabella Ingrid
 */
public class TTCN_Buffer {
	//FIXME a lot to implement here

	final static private int INITIAL_SIZE = 1024;
	/* The layout of this structure must match that of charstring_struct */
	//public class buffer_struct {
	private int ref_count;
	private int unused_length_field; /**< placeholder only */
	private char data_ptr[];
	//}

	//	buffer_struct buf_ptr; ///< pointer to internal data
	// The number of bytes of memory allocated
	private int buf_size;

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

	/** Deallocates the memory area that is associated with the buffer. */
	private void release_memory() {
		//TODO: implement
	}

	/** Returns the smallest preferred size of the allocated memory area
	 * that is at least \a target_size. */
	private int get_memory_size(int target_size) {
		int newSize = INITIAL_SIZE;
		while (newSize < target_size) {
			int nextSize = newSize + newSize;
			if (nextSize > newSize) {
				newSize = newSize + nextSize;
			} else {
				// integer overflow occurred
				return -1;
			}

		}
		return newSize;
	}

	/** Ensures that buffer has its own writable memory area. */
	private void copy_memory() {
		//TODO: implement
	}

	/** Ensures that there are at least \a target_size writable bytes in the
	 * memory area after \a buf_len. */
	private void increase_size(int size_incr) {
		//TODO: implement
	}

	/** Creates an empty buffer. */
	public TTCN_Buffer() {
		data_ptr = null;
		buf_size = 0;
		buf_len = 0;
		reset_buffer();
	}

	/** Copy constructor. */
	public TTCN_Buffer(final TTCN_Buffer p_buf) {
		data_ptr = p_buf.data_ptr;
		ref_count = p_buf.ref_count + 1;
		unused_length_field = p_buf.unused_length_field;
		buf_size = p_buf.buf_size;
		buf_len = p_buf.buf_len;
		reset_buffer();
	}

	/** Initializes the buffer with the contents of \a p_os.
	 * @pre The argument must be bound. */
	public  TTCN_Buffer(final TitanOctetString p_os) {
		p_os.mustBound("Initializing a TTCN_Buffer with an unbound octetstring value.");
		data_ptr = p_os.getValue();
		ref_count++;
		buf_size = p_os.lengthOf().getInt();
		buf_len = buf_size; //p_os.val_ptr->n_octets;
		reset_buffer();
	}

	/** Initializes the buffer with the contents of \a p_cs.
	 * @pre The argument must be bound. */
	public TTCN_Buffer(final TitanCharString p_cs) {
		p_cs.mustBound("Initializing a TTCN_Buffer with an unbound charstring value.");
		data_ptr = p_cs.getValue().toString().toCharArray();//(buffer_struct*)p_cs.val_ptr;
		ref_count++;
		buf_size = p_cs.lengthOf().getInt() + 1; //val_ptr->n_chars + 1;
		buf_len = p_cs.lengthOf().getInt();
		reset_buffer();
	}

	/** Copies the contents of \a p_buf into \a this.
	 * The read pointers and other attributes are reset. */
	public TTCN_Buffer assign(final TTCN_Buffer p_buf) {
		if (p_buf != null) {
			release_memory();
			data_ptr = TitanStringUtils.copyCharList(p_buf.data_ptr);
			unused_length_field = p_buf.unused_length_field;
			ref_count = p_buf.ref_count + 1;
			buf_size = p_buf.buf_size;
			buf_len = p_buf.buf_len;
		}
		reset_buffer();
		return this;
	}

	/** Copies the contents of \a p_os into \a this.
	 * The read pointers and other attributes are reset. */
	public  TTCN_Buffer assign(final TitanOctetString p_os) {
		// FIXME: implement
		return null;
	}

	/** Copies the contents of \a p_cs into \a this.
	 * The read pointers and other attributes are reset. */
	public TTCN_Buffer assign(final TitanCharString p_cs) {
		// FIXME: implement
		return null;
	}

	/** Erases the content of the buffer. */
	public void clear() {
		release_memory();
		data_ptr = null;
		ref_count = 0;
		unused_length_field = 0;
		buf_size = 0;
		buf_len = 0;
		reset_buffer();
	}

	public char[] get_data() {
		return data_ptr;
	}

	public char[] get_read_data() {
		if (data_ptr != null) {
			char[] result = new char[buf_len - buf_pos];
			for (int i = 0; i < buf_len-buf_pos; i++) {
				result[i] = data_ptr[buf_pos + i];
			}
			return result; //FIXME: buf_ptr + buf_pos 
		}
		return null;
	}



	/** Appends single character \a c to the buffer. */
	public void put_c(final char c) {
		increase_size(1);
		data_ptr[buf_len] = c;
		buf_len++;
	}

	/** Appends \a len bytes starting from address \a s to the buffer. */
	public void put_s(StringBuilder cstr) {
		final int length = cstr.length();
		if (length > 0) {
			increase_size(length);
			for (int i = 0; i < length; i++) {
				data_ptr[buf_len+i] = cstr.charAt(i);
			}
			buf_len += length; 
		}
	}

	public char[] put_string() {
		// TODO Auto-generated method stub
		return null;
	}

	/** 
	 * Stores the current contents of the buffer to variable p_os.
	 * 
	 * @param p_os the variable to store the contents of the buffer into.
	 * */
	public void get_string(final TitanOctetString p_os) {
		throw new TtcnError("get_string() for TTCN_Buffer is not implemented!");
	}
}
