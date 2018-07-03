/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

import java.math.BigInteger;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.titan.runtime.core.Base_Type.TTCN_Typedescriptor;
import org.eclipse.titan.runtime.core.TTCN_EncDec.raw_order_t;
import org.eclipse.titan.runtime.core.TitanCharString.CharCoding;

/**
 * Base class for RAW encoding/decoiding.
 *
 * \defgroup RAW RAW-related stuff.
 *
 * The RAW encoder/decoder can be used to handle protocols where the position
 * of information elements must be specified with bit-level precision.
 *
 * @{
 *
 * The RAW encoder is a two-pass encoder. In the first pass, information about
 * all the information elements is collected in a RAW_enc_tree object.
 * This information is used to write the actual encoding into the buffer.
 *
 * This two-pass mechanism is needed because the contents of earlier fields
 * can depend on fields which have not been encoded yet (e.g. length or
 * CROSSTAG selector fields).
 *
 * @author Gergo Ujhelyi
 **/
public class RAW {

	public static final int[] BitReverseTable = {
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

	public static final int[] BitMaskTable = {
		0x00, 0x01, 0x03, 0x07, 0x0f, 0x1f, 0x3f, 0x7f, 0xff
	};

	public static int REVERSE_BITS(final int b) {
		return BitReverseTable[(b) & 0xFF];
	}

	public static final int RAW_INT_ENC_LENGTH = 4;
	public static final int RAW_INTX = -1;

	/**
	 * Initialize the RAW encoding tree. The tree representations makes it easier
	 * to encode/decode structured types with various attributes. Each node in the
	 * tree stores information about the node's parent, position, attributes,
	 * child nodes etc. The root of the tree is on the first level and its ``par''
	 * is always NULL. That's why there's a ``par_pos'' parameter, but it could be
	 * omitted. The first part of the position route in ``curr_pos.pos'' is
	 * inherited from ``par''. The last element is the number of the current
	 * element. Only the leaves carry data. Other nodes are just for construction
	 * purposes.
	 *
	 * @param is_leaf true if it's a node with no children
	 * @param par the parent of the current node
	 * @param par_pos the parent's position
	 * @param my_pos the child node's number of ``par''
	 * @param raw_attr encoding attributes
	 */
	public static class RAW_enc_tree {
		/** indicates that the node is leaf (contains actual data) or not
		 *  (contains pointers to other nodes) */
		public boolean isleaf;
		public boolean rec_of;
		public RAW_enc_tree parent;
		public RAW_enc_tr_pos curr_pos;
		public int length;  /**< Encoded length */
		/** @name Encoding parameters related to the filling of the buffer @{ */
		public int padding;
		public int prepadding;
		public int startpos;
		public int padlength;
		public int prepadlength;
		public int padding_pattern_length;
		public char[] padding_pattern;
		public int align; /**< alignment length */
		/** @} */
		public int ext_bit_handling; /**< 1: start, 2: stop, 3: only this */
		public ext_bit_t ext_bit;
		public top_bit_order_t top_bit_order;
		public TTCN_Typedescriptor coding_descr;
		public calc_type calc; /**< is it a calculated field or not */
		public RAW_coding_par coding_par;
		public RAW_enc_lengthto lengthto; /**< calc is CALC_LENGTH */
		public RAW_enc_pointer  pointerto; /**< calc is CALC_POINTER */
		public int num_of_nodes;
		public RAW_enc_tree nodes[];
		public char data_array[] = new char[RAW_INT_ENC_LENGTH];  /**< false */

		public RAW_enc_tree(final boolean is_leaf, final RAW_enc_tree par, final RAW_enc_tr_pos par_pos, final int my_pos, final TTCN_RAWdescriptor raw_attr) {
			boolean orders = false;
			this.isleaf = is_leaf;
			rec_of = false;
			parent = par;
			curr_pos = new RAW_enc_tr_pos(par_pos.level + 1, new int[par_pos.level + 1]);
			if (par_pos.level > 0) {
				System.arraycopy(par_pos.pos, 0, curr_pos.pos, 0, par_pos.pos.length);
			}
			curr_pos.pos[curr_pos.level - 1] = my_pos;
			length = 0;
			padding = raw_attr.padding;
			prepadding = raw_attr.prepadding;
			padding_pattern_length = raw_attr.padding_pattern_length;
			padding_pattern = raw_attr.padding_pattern;
			startpos = 0;
			padlength = 0;
			prepadlength = 0;
			align = 0;
			ext_bit_handling = 0;
			coding_descr = null;
			ext_bit = raw_attr.extension_bit;
			top_bit_order = raw_attr.top_bit_order;
			calc = calc_type.CALC_NO;
			if (raw_attr.byteorder == raw_order_t.ORDER_MSB) {
				orders = true;
			}
			if (raw_attr.bitorderinfield == raw_order_t.ORDER_MSB) {
				orders = !orders;
			}

			final raw_order_t tempbyteorder = orders ? raw_order_t.ORDER_MSB : raw_order_t.ORDER_LSB;
			orders = false;
			if (raw_attr.bitorderinoctet == raw_order_t.ORDER_MSB) {
				orders = true;
			}
			if (raw_attr.bitorderinfield == raw_order_t.ORDER_MSB) {
				orders = !orders;
			}
			final raw_order_t tempbitorder = orders ? raw_order_t.ORDER_MSB : raw_order_t.ORDER_LSB;
			final raw_order_t temphexorder = raw_attr.hexorder;
			final raw_order_t tempfieldorder = raw_attr.fieldorder;
			coding_par = new RAW_coding_par(tempbitorder, tempbyteorder, temphexorder, tempfieldorder);
			if (!is_leaf) {
				num_of_nodes = 0;
				nodes = null;
			}
		}

		public void put_to_buf(final TTCN_Buffer buf) {
			calc_padding(0);
			calc_fields();
			fill_buf(buf);
		}

		private void calc_fields() {
			if (isleaf) {
				switch (calc) {
				case CALC_LENGTH:{
					int szumm = 0;
					RAW_enc_tree atm;
					if (lengthto.unit != -1) {
						for (int a = 0; a < lengthto.num_of_fields; a++) {
							atm = get_node(lengthto.fields[a]);
							if (atm != null) {
								szumm += atm.length + atm.padlength + atm.prepadlength;
							}
						}
						szumm = (szumm + lengthto.unit - 1) / lengthto.unit;
					} else {
						atm = get_node(lengthto.fields[0]);
						if (atm != null) {
							szumm = atm.num_of_nodes;
						}
					}

					szumm += lengthto.offset;
					final TitanInteger temp = new TitanInteger(szumm);
					temp.RAW_encode(coding_descr, this);
					break;
				}
				case CALC_POINTER:{
					final int cl = curr_pos.pos[curr_pos.level - 1];
					curr_pos.pos[curr_pos.level - 1] = pointerto.ptr_base;
					int base = pointerto.ptr_base;
					RAW_enc_tree b = get_node(curr_pos);
					while (b == null) {
						base++;
						curr_pos.pos[curr_pos.level - 1] = base;
						b = get_node(curr_pos);
					}
					curr_pos.pos[curr_pos.level - 1] = cl;
					int szumm = 0;
					final RAW_enc_tree atm = get_node(pointerto.target);
					if (atm != null) {
						szumm = (atm.startpos - b.startpos + pointerto.unit - 1 - pointerto.ptr_offset) / pointerto.unit;
					}

					final TitanInteger temp = new TitanInteger(szumm);
					temp.RAW_encode(coding_descr, this);
					break;
				}
				default:
					break;
				}
			} else {
				for (int a = 0; a < num_of_nodes; a++) {
					if (nodes[a] != null) {
						nodes[a].calc_fields();
					}
				}
			}
		}

		private int calc_padding(final int position) {
			int current_pos = position;
			startpos = position;
			if (prepadding != 0) {
				final int new_pos = ((current_pos + prepadding - 1) / prepadding) * prepadding;
				prepadlength = new_pos - position;
				current_pos = new_pos;
			}
			if (!isleaf) {
				for (int a = 0; a < num_of_nodes; a++) {
					if (nodes[a] != null) {
						current_pos = nodes[a].calc_padding(current_pos);
					}
				}
				length = current_pos - position - prepadlength;
			} else {
				current_pos += length;
			}
			if (padding != 0) {
				final int new_pos = ((current_pos + padding - 1) / padding) * padding;
				padlength = new_pos - length - position - prepadlength;
				current_pos = new_pos;
			}
			return current_pos;
		}

		private void fill_buf(final TTCN_Buffer buf) {
			final boolean old_order = buf.get_order();
			if (top_bit_order != top_bit_order_t.TOP_BIT_INHERITED) {
				buf.set_order(top_bit_order != top_bit_order_t.TOP_BIT_RIGHT);
			}

			buf.put_pad(prepadlength, padding_pattern, padding_pattern_length, coding_par.fieldorder);
			if (isleaf) {
				final int align_length = align < 0 ? -align : align;
				if (ext_bit != ext_bit_t.EXT_BIT_NO) {
					buf.start_ext_bit(ext_bit == ext_bit_t.EXT_BIT_REVERSE);
				}
					buf.put_b(length - align_length, data_array, coding_par, align);
				if (ext_bit_handling > 1) {
					buf.stop_ext_bit();
				} else if (ext_bit != ext_bit_t.EXT_BIT_NO && !(ext_bit_handling != 0)) {
					buf.stop_ext_bit();
				}
			} else {
				if (ext_bit != ext_bit_t.EXT_BIT_NO && (!rec_of || ext_bit_handling % 2 != 0)) {
					buf.start_ext_bit(ext_bit == ext_bit_t.EXT_BIT_REVERSE);
				}
				for (int a = 0; a < num_of_nodes; a++) {
					if (nodes[a] != null) {
						nodes[a].fill_buf(buf);
					}
					if (ext_bit != ext_bit_t.EXT_BIT_NO && rec_of && !(ext_bit_handling != 0)) {
						buf.set_last_bit(ext_bit != ext_bit_t.EXT_BIT_YES);
					}
				}
				if (!(ext_bit_handling != 0)) {
					if (ext_bit != ext_bit_t.EXT_BIT_NO && !rec_of) {
						buf.stop_ext_bit();
					} else if (ext_bit != ext_bit_t.EXT_BIT_NO && rec_of) {
						buf.set_last_bit(ext_bit == ext_bit_t.EXT_BIT_YES);
					}
				} else if (ext_bit_handling > 1) {
					buf.stop_ext_bit();
				}
			}
			buf.put_pad(padlength, padding_pattern,padding_pattern_length, coding_par.fieldorder);
			buf.set_order(old_order);
		}

		/**
		 * Return the element at ``req_pos'' from the RAW encoding tree. At first get
		 * the root of the whole tree at the first level. Then go down in the tree
		 * following the route in the ``req_pos.pos'' array. If the element was not
		 * found NULL is returned.
		 *
		 * @param req_pos the position of the element
		 * @return the element at the given position
		 */
		public RAW_enc_tree get_node(final RAW_enc_tr_pos req_pos) {
			if (req_pos.level == 0) {
				return null;
			}
			RAW_enc_tree t = this;
			int cur_level = curr_pos.level;
			for (int b = 1; b < cur_level; b++) {
				t = t.parent;
			}
			for (cur_level = 1; cur_level < req_pos.level; cur_level++) {
				if (t == null || t.isleaf || t.num_of_nodes <= req_pos.pos[cur_level]) {
					return null;
				}
				t = t.nodes[req_pos.pos[cur_level]];
			}
			return t;
		}
	}

	public static final class TTCN_RAWdescriptor {
		public int fieldlength; /**< length of field in \a unit s */
		public raw_sign_t comp; /**< the method used for storing negative numbers */
		public raw_order_t byteorder;
		public raw_order_t endianness;
		public raw_order_t bitorderinfield;
		public raw_order_t bitorderinoctet;
		public ext_bit_t extension_bit; /**< MSB mangling */
		public raw_order_t hexorder;
		public raw_order_t fieldorder;
		public top_bit_order_t top_bit_order;
		public int padding;
		public int prepadding;
		public int ptroffset;
		public int unit; /**< number of bits per unit */
		public int padding_pattern_length;
		public char[] padding_pattern;
		public int length_restrition;
		public CharCoding stringformat;

		public TTCN_RAWdescriptor(final int fieldlength, final raw_sign_t comp,
				final raw_order_t byteorder, final raw_order_t endianness,
				final raw_order_t bitorderinfield, final raw_order_t bitorderinoctet,
				final ext_bit_t extension_bit, final raw_order_t hexorder,
				final raw_order_t fieldorder, final top_bit_order_t top_bit_order,
				final int padding, final int prepadding, final int ptroffset, final int unit,
				final int padding_pattern_length, final char[] padding_pattern,
				final int length_restrition, final CharCoding stringformat) {
			this.fieldlength = fieldlength;
			this.comp = comp;
			this.byteorder = byteorder;
			this.endianness = endianness;
			this.bitorderinfield = bitorderinfield;
			this.bitorderinoctet = bitorderinoctet;
			this.extension_bit = extension_bit;
			this.hexorder = hexorder;
			this.fieldorder = fieldorder;
			this.top_bit_order = top_bit_order;
			this.padding = padding;
			this.prepadding = prepadding;
			this.ptroffset = ptroffset;
			this.unit = unit;
			this.padding_pattern_length = padding_pattern_length;
			this.padding_pattern = padding_pattern;
			this.length_restrition = length_restrition;
			this.stringformat = stringformat;
		}

		public TTCN_RAWdescriptor() {

		}
	}

	public static class RAW_enc_tr_pos {
		public int level;
		public int pos[];

		public RAW_enc_tr_pos(final int level, final int pos[]) {
			this.level = level;
			this.pos = pos;
		}
	}

	public static class RAW_enc_pointer {
		public RAW_enc_tr_pos target;
		public int ptr_offset;
		public int unit;
		public int ptr_base;

		public RAW_enc_pointer(final RAW_enc_tr_pos target, final int ptr_offset, final int unit, final int ptr_base) {
			this.target = target;
			this.ptr_offset = ptr_offset;
			this.unit = unit;
			this.ptr_base = ptr_base;
		}
	}

	public static class RAW_enc_lengthto {
		public int num_of_fields;
		public RAW_enc_tr_pos fields[];
		public int unit;
		public int offset;

		public RAW_enc_lengthto(final int num_of_fields, final RAW_enc_tr_pos fields[], final int unit, final int offset) {
			this.num_of_fields = num_of_fields;
			this.fields = fields;
			this.unit = unit;
			this.offset = offset;
		}
	}

	public static class RAW_coding_par {
		public raw_order_t bitorder;
		public raw_order_t byteorder;
		public raw_order_t hexorder;
		public raw_order_t fieldorder;

		public RAW_coding_par(final raw_order_t bitorder, final raw_order_t byteorder, final raw_order_t hexorder, final raw_order_t fieldorder) {
			this.bitorder = bitorder;
			this.byteorder = byteorder;
			this.hexorder = hexorder;
			this.fieldorder = fieldorder;
		}

		public RAW_coding_par() {

		}
	}

	public static enum ext_bit_t {
		EXT_BIT_NO,      /**< No extension bit */
		EXT_BIT_YES,     /**< Extension bit used 0: more octets, 1: no more octets */
		EXT_BIT_REVERSE  /**< Extension bit used 1: more octets, 0: no more octets */
	}

	public static enum top_bit_order_t {
		TOP_BIT_INHERITED,
		TOP_BIT_LEFT,
		TOP_BIT_RIGHT
	}

	public static enum raw_sign_t {
		SG_NO,         /**< no sign, value coded as positive number */
		SG_2COMPL,     /**< the value coded as 2s complement */
		SG_SG_BIT      /**< the MSB used to encode the sign of the value */
	}

	public static enum calc_type {
		CALC_NO, /**< not a calculated field */
		CALC_LENGTH, /**< calculated field for LENGTHTO */
		CALC_POINTER /**< calculated field for POINTERTO */
	}

	private RAW() {
		//intentionally empty
	}

	/**
	 * Return the number of bits needed to represent an integer value `a'.  The
	 * sign bit is added for negative values.  It has a different implementation
	 * for BigInteger values.
	 *
	 * @param a the integer in question
	 * @return the number of bits needed to represent the given integer
	 * in sign+magnitude
	 */
	public static int min_bits(final int a) {
		int bits = 0;
		int tmp = a;
		if (a < 0) {
			bits = 1;
			tmp = -a;
		}
		while (tmp != 0) {
			bits++;
			tmp /= 2;
		}
		return bits;
	}

	public static int min_bits(final BigInteger a) {
		if (a == null) {
			return 0;
		}

		return a.bitLength();
	}

	public static int RAW_encode_enum_type(final TTCN_Typedescriptor p_td, final RAW_enc_tree myleaf, final int integer_value, final int min_bits_enum) {
		final int fl = p_td.raw.fieldlength != 0 ? p_td.raw.fieldlength : min_bits_enum;
		final TTCN_RAWdescriptor my_raw = new TTCN_RAWdescriptor();
		my_raw.fieldlength = fl;
		my_raw.comp = p_td.raw.comp;
		my_raw.byteorder = p_td.raw.byteorder;
		my_raw.endianness = p_td.raw.endianness;
		my_raw.bitorderinfield = p_td.raw.bitorderinfield;
		my_raw.bitorderinoctet = p_td.raw.bitorderinoctet;
		my_raw.extension_bit = p_td.raw.extension_bit;
		my_raw.hexorder = p_td.raw.hexorder;
		my_raw.fieldorder = p_td.raw.fieldorder;
		my_raw.top_bit_order = p_td.raw.top_bit_order;
		my_raw.padding = p_td.raw.padding;
		my_raw.prepadding = p_td.raw.prepadding;
		my_raw.ptroffset = p_td.raw.ptroffset;
		my_raw.unit = p_td.raw.unit;
		//FIXME: initial implementation of Typedescriptor
		final TTCN_Typedescriptor my_descr = new TTCN_Typedescriptor(p_td.name, my_raw, null);
		final TitanInteger i = new TitanInteger(integer_value);
		i.RAW_encode(my_descr, myleaf);
		//  myleaf.align=0;//p_td.raw.endianness==raw_order_t.ORDER_MSB ? min_bits_enum-fl : fl-min_bits_enum;
		return myleaf.length = fl;
	}

	public static int RAW_decode_enum_type(final TTCN_Typedescriptor p_td, final TTCN_Buffer buff, final int limit, final raw_order_t top_bit_ord, final AtomicInteger value, final int min_bits_enum, final boolean no_err) {
		int fl = p_td.raw.fieldlength != 0 ? p_td.raw.fieldlength : min_bits_enum;
		final TTCN_RAWdescriptor my_raw = new TTCN_RAWdescriptor();
		my_raw.fieldlength = fl;
		my_raw.comp = p_td.raw.comp;
		my_raw.byteorder = p_td.raw.byteorder;
		my_raw.endianness = p_td.raw.endianness;
		my_raw.bitorderinfield = p_td.raw.bitorderinfield;
		my_raw.bitorderinoctet = p_td.raw.bitorderinoctet;
		my_raw.extension_bit = p_td.raw.extension_bit;
		my_raw.hexorder = p_td.raw.hexorder;
		my_raw.fieldorder = p_td.raw.fieldorder;
		my_raw.top_bit_order = p_td.raw.top_bit_order;
		my_raw.padding = p_td.raw.padding;
		my_raw.prepadding = p_td.raw.prepadding;
		my_raw.ptroffset = p_td.raw.ptroffset;
		my_raw.unit = p_td.raw.unit;
		//FIXME: initial implementation of Typedescriptor
		final TTCN_Typedescriptor my_descr = new TTCN_Typedescriptor(p_td.name, my_raw, null);
		final TitanInteger i = new TitanInteger();
		fl = i.RAW_decode(my_descr, buff, limit, top_bit_ord);
		if(fl < 0) {
			return fl;
		}
		value.set(i.getInt());

		return fl + buff.increase_pos_padd(p_td.raw.padding);
	}
	
	public int[] init_new_tree_pos(final RAW_enc_tr_pos old_pos, final int new_levels, final int[] new_pos) {
		int[] new_position = new int[old_pos.level + new_levels];
		System.arraycopy(old_pos.pos, 0, new_pos, 0, old_pos.level);
		System.arraycopy(new_pos, 0, new_pos, new_pos.length + old_pos.level, new_levels);
		return new_position;
 	}
	
	public void free_tree_pos(RAW_enc_tr_pos ptr) {
		ptr = null;
	}

	//Default descriptors of RAW encoding for primitive types.
	public static final TTCN_RAWdescriptor TitanInteger_raw_ = new TTCN_RAWdescriptor(8, raw_sign_t.SG_NO, raw_order_t.ORDER_LSB, raw_order_t.ORDER_LSB, raw_order_t.ORDER_LSB, raw_order_t.ORDER_LSB, ext_bit_t.EXT_BIT_NO, raw_order_t.ORDER_LSB, raw_order_t.ORDER_LSB, top_bit_order_t.TOP_BIT_INHERITED, 0, 0, 0, 8, 0, null, -1, CharCoding.UNKNOWN);
	public static final TTCN_RAWdescriptor TitanBoolean_raw_ = new TTCN_RAWdescriptor(1, raw_sign_t.SG_NO, raw_order_t.ORDER_LSB, raw_order_t.ORDER_LSB, raw_order_t.ORDER_LSB, raw_order_t.ORDER_LSB, ext_bit_t.EXT_BIT_NO, raw_order_t.ORDER_LSB, raw_order_t.ORDER_LSB, top_bit_order_t.TOP_BIT_INHERITED, 0, 0, 0, 8, 0, null, -1, CharCoding.UNKNOWN);
	public static final TTCN_RAWdescriptor TitanBitString_raw_ = new TTCN_RAWdescriptor(0, raw_sign_t.SG_NO, raw_order_t.ORDER_LSB, raw_order_t.ORDER_LSB, raw_order_t.ORDER_LSB, raw_order_t.ORDER_LSB, ext_bit_t.EXT_BIT_NO, raw_order_t.ORDER_LSB, raw_order_t.ORDER_LSB, top_bit_order_t.TOP_BIT_INHERITED, 0, 0, 0, 8, 0, null, -1, CharCoding.UNKNOWN);
	public static final TTCN_RAWdescriptor TitanOctetString_raw_ = new TTCN_RAWdescriptor(0, raw_sign_t.SG_NO, raw_order_t.ORDER_LSB, raw_order_t.ORDER_LSB, raw_order_t.ORDER_LSB, raw_order_t.ORDER_LSB, ext_bit_t.EXT_BIT_NO, raw_order_t.ORDER_LSB, raw_order_t.ORDER_LSB, top_bit_order_t.TOP_BIT_INHERITED, 0, 0, 0, 8, 0, null, -1, CharCoding.UNKNOWN);
	public static final TTCN_RAWdescriptor TitanHexString_raw_ = new TTCN_RAWdescriptor(0, raw_sign_t.SG_NO, raw_order_t.ORDER_LSB, raw_order_t.ORDER_LSB, raw_order_t.ORDER_LSB, raw_order_t.ORDER_LSB, ext_bit_t.EXT_BIT_NO, raw_order_t.ORDER_LSB, raw_order_t.ORDER_LSB, top_bit_order_t.TOP_BIT_INHERITED, 0, 0, 0, 8, 0, null, -1, CharCoding.UNKNOWN);
	public static final TTCN_RAWdescriptor TitanCharString_raw_ = new TTCN_RAWdescriptor(0, raw_sign_t.SG_NO, raw_order_t.ORDER_LSB, raw_order_t.ORDER_LSB, raw_order_t.ORDER_LSB, raw_order_t.ORDER_LSB, ext_bit_t.EXT_BIT_NO, raw_order_t.ORDER_LSB, raw_order_t.ORDER_LSB, top_bit_order_t.TOP_BIT_INHERITED, 0, 0, 0, 8, 0, null, -1, CharCoding.UNKNOWN);
	public static final TTCN_RAWdescriptor TitanFloat_raw_ = new TTCN_RAWdescriptor(64, raw_sign_t.SG_NO, raw_order_t.ORDER_LSB, raw_order_t.ORDER_LSB, raw_order_t.ORDER_LSB, raw_order_t.ORDER_LSB, ext_bit_t.EXT_BIT_NO, raw_order_t.ORDER_LSB, raw_order_t.ORDER_LSB, top_bit_order_t.TOP_BIT_INHERITED, 0, 0, 0, 8, 0, null, -1, CharCoding.UNKNOWN);
	public static final TTCN_RAWdescriptor TitanUniversalCharString_raw_ = new TTCN_RAWdescriptor(0, raw_sign_t.SG_NO, raw_order_t.ORDER_LSB, raw_order_t.ORDER_LSB, raw_order_t.ORDER_LSB, raw_order_t.ORDER_LSB, ext_bit_t.EXT_BIT_NO, raw_order_t.ORDER_LSB, raw_order_t.ORDER_LSB, top_bit_order_t.TOP_BIT_INHERITED, 0, 0, 0, 8, 0, null, -1, CharCoding.UNKNOWN);
}