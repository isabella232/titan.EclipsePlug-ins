/******************************************************************************
 * Copyright (c) 2000-2017 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.attributes;

import java.util.ArrayList;

import org.eclipse.titan.designer.AST.TTCN3.values.expressions.ExpressionStruct;

/**
 * Represents the RAW encoding related setting extracted from variant attributes, for the code generator.
 *
 * @author Kristof Szabados
 * */
public class RawASTStruct {
	//Logic kept to be in-line with the compiler side
	//TODO we could reuse the RawAST versions
	public static final int XDEFUNSIGNED = 1;
	public static final int XDEFCOMPL = 2;
	public static final int XDEFSIGNBIT = 3;
	public static final int XDEFYES = 2;
	public static final int XDEFNO = 1;
	public static final int XDEFREVERSE = 3;
	public static final int XDEFMSB = 1;
	public static final int XDEFLSB = 2;
	public static final int XDEFBITS = 1;
	public static final int XDEFOCTETS = 2;
	public static final int XDEFLEFT = 1;
	public static final int XDEFRIGHT = 2;
	public static final int XDEFFIRST = 1;
	public static final int XDEFLAST = 2;
	public static final int XDEFLOW = 1;
	public static final int XDEFHIGH = 2;
	public static final int XDEFDEFAULT = -1;

	public static class rawAST_toplevel {
		public int bitorder; /* Invert bitorder of the encoded data */
	}

	public static enum rawAST_coding_field_type {
		MANDATORY_FIELD,
		OPTIONAL_FIELD,
		UNION_FIELD
	}

	public static class rawAST_coding_fields {
		public int nthfield;
		public String nthfieldname;
		public rawAST_coding_field_type fieldtype;
		public String type;
		public String typedesc;
	}

	public static class rawAST_coding_field_list {
		public ArrayList<rawAST_coding_fields> fields;
		//public String value;
		public ExpressionStruct expression;
		public boolean isOmitValue;
		public int start_pos;
		public int temporal_variable_index;
	}

	public static class rawAST_coding_taglist {
		public String fieldname;
		public String varName;
		public int fieldnum;
		public ArrayList<rawAST_coding_field_list> fields;
	}

	public static class rawAST_coding_taglist_list {
		public ArrayList<rawAST_coding_taglist> list;
	}

	public static class rawAST_coding_ext_group {
		public int ext_bit;
		public int from;
		public int to;
	}


	public int fieldlength; // Nr of bits per character, hexstring : 4, octetstring and charstring : 8, etc
	public int comp; // Handling of sign: no, 2scomp, signbit
	public int byteorder; // XDEFMSB, XDEFLSB
	public int align; // XDEFLEFT, XDEFRIGHT
	public int bitorderinfield; // XDEFMSB, XDEFLSB
	public int bitorderinoctet; // XDEFMSB, XDEFLSB
	public int extension_bit; // XDEFYES, XDEFNO
	//ext_bit_goup_num is not stored as it is the sie of ext_bit_groups
	public ArrayList<rawAST_coding_ext_group> ext_bit_groups;
	public int hexorder; //XDEFLOW, XDEFHIGH
	public int padding;  // XDEFYES: next field starts at next octet

	public int fieldorder; // XDEFMSB, XDEFLSB
	public ArrayList<Integer> lengthto; //list of fields to generate length for
	public int lengthto_offset;
	public int pointerto; //pointer to the specified field is contained in this field

	/**< offset to the value in bits
	 Actual position will be:
	 pointerto*ptrunit + ptroffset */
	public int ptrunit; // number of bits in pointerto value
	public int ptroffset;
	public int pointerbase; //the identifier in PTROFFSET(identifier)
	public int unit; //XDEFOCTETS, XDEFBITS
	public rawAST_coding_fields lengthindex; // stores subattribute of the lengthto attribute
	// field IDs in form of [unionField.sub]field_N, keyField.subfield_M = tagValue multiple tagValues may be specified
	public rawAST_coding_taglist_list crosstaglist;
	public rawAST_coding_taglist_list taglist;
	
	public rawAST_coding_taglist presence; // Presence indicator expressions for an optional field
	public int toplevelind;
	public rawAST_toplevel toplevel;
	public int union_member_num;
	public ArrayList<String> member_name;
	public int repeatable;
	public int length;

	public RawASTStruct(final RawAST from) {
		fieldlength = from.fieldlength;
		comp = from.comp;
		byteorder = from.byteorder;
		align = from.align;
		bitorderinfield = from.bitorderinfield;
		bitorderinoctet = from.bitorderinoctet;
		extension_bit = from.extension_bit;
		if (from.ext_bit_groups != null) {
			ext_bit_groups = new ArrayList<RawASTStruct.rawAST_coding_ext_group>(from.ext_bit_groups.size());
		}
		hexorder = from.hexorder;
		padding = from.padding;
		if (from.lengthto != null) {
			lengthto = new ArrayList<Integer>(from.lengthto.size());
		}
		lengthto_offset = from.lengthto_offset;
		pointerto = -1;
		ptroffset = from.ptroffset;
		unit = from.unit;
		if (from.lengthindex != null) {
			lengthindex = new rawAST_coding_fields();
		}
		if (from.crosstaglist != null && from.crosstaglist.size() > 0) {
			crosstaglist = new rawAST_coding_taglist_list();
			crosstaglist.list = new ArrayList<RawASTStruct.rawAST_coding_taglist>(from.crosstaglist.size());
			for (int i = 0; i < from.crosstaglist.size(); i++) {
				crosstaglist.list.add(new rawAST_coding_taglist());
			}
		}
		if (from.taglist != null && from.taglist.size() > 0) {
			taglist = new rawAST_coding_taglist_list();
			taglist.list = new ArrayList<RawASTStruct.rawAST_coding_taglist>(from.taglist.size());
			for (int i = 0; i < from.taglist.size(); i++) {
				taglist.list.add(new rawAST_coding_taglist());
			}
		}
		if (from.presence != null && from.presence.keyList != null && from.presence.keyList.size() > 0) {
			presence = new rawAST_coding_taglist();
			presence.fields = new ArrayList<RawASTStruct.rawAST_coding_field_list>(from.presence.keyList.size());
		}
		toplevelind = from.toplevelind;
		toplevel = new rawAST_toplevel();
		toplevel.bitorder = from.toplevel.bitorder;
		union_member_num = 0;
		member_name = null;
		repeatable = from.repeatable;
		length = -1;
	}
}
