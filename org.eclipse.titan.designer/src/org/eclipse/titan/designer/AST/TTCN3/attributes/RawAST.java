/******************************************************************************
 * Copyright (c) 2000-2017 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.attributes;

import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.TTCN3.types.CharString_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.CharString_Type.CharCoding;

/**
 * Represents the RAW encoding related setting extracted from variant attributes.
 *
 * @author Kristof Szabados
 * */
public class RawAST {
	public static class rawAST_toplevel {
		public int bitorder;
	}

	//Logic kept to be in-line with the compiler side
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

	public int fieldlength; // Nr of bits per character, hexstring : 4, octetstring and charstring : 8, etc
	public int comp; // Handling of sign: no, 2scomp, signbit
	public int byteorder; // XDEFMSB, XDEFLSB
	public int align; // XDEFLEFT, XDEFRIGHT
	public int bitorderinfield; // XDEFMSB, XDEFLSB
	public int bitorderinoctet; // XDEFMSB, XDEFLSB
	public int extension_bit; // XDEFYES, XDEFNO
	//..
	public int hexorder; //XDEFLOW, XDEFHIGH
	public int padding;  // XDEFYES: next field starts at next octet
	public int prepadding;
	public int padding_pattern_length;
	//..
	public String padding_pattern;
	public int fieldorder; // XDEFMSB, XDEFLSB
	//..

	/**< offset to the pointer value in bits
	 Actual position will be:
	 pointerto*ptrunit + ptroffset */
	public int ptroffset;
	public Identifier ptrbase; //the identifier in PTROFFSET(identifier)
	public int unit; //XDEFOCTETS, XDEFBITS
	//..
	public int toplevelind;
	public rawAST_toplevel toplevel = new rawAST_toplevel();
	public int length_restriction;
	public boolean intX; //IntX encoding for integers
	public CharString_Type.CharCoding stringformat; //String serialization type for universal charstrings

	//FIXME add rest of the fields

	// disabled constructor
	private RawAST() {
		init_rawast(0);
	}

	public RawAST(final int defaultLength) {
		init_rawast(defaultLength);
	}

	/**
	 * Sort of copy- constructor
	 * */
	public RawAST(final RawAST other, final int defaultLength) {
		if (other == null) {
			init_rawast(defaultLength);
		} else {
			fieldlength = other.fieldlength;
			comp = other.comp;
			byteorder = other.byteorder;
			align = other.align;
			bitorderinfield = other.bitorderinfield;
			bitorderinoctet = other.bitorderinoctet;
			extension_bit = other.extension_bit;
			hexorder = other.hexorder;
			padding = other.padding;
			prepadding = other.prepadding;
			padding_pattern_length = other.padding_pattern_length;
			padding_pattern = other.padding_pattern;
			fieldorder = other.fieldorder;
			ptroffset = other.ptroffset;
			ptrbase = other.ptrbase;
			unit = other.unit;
			toplevelind = other.toplevelind;
			toplevel.bitorder = other.toplevel.bitorder;
			length_restriction = other.length_restriction;
			intX = other.intX;
			stringformat = other.stringformat;
		}
	}

	private void init_rawast(final int defaultLength) {
		fieldlength = defaultLength;
		comp = XDEFDEFAULT;
		byteorder = XDEFDEFAULT;
		align = XDEFDEFAULT;
		bitorderinfield = XDEFDEFAULT;
		bitorderinoctet = XDEFDEFAULT;
		extension_bit = XDEFDEFAULT;
		hexorder = XDEFDEFAULT;
		padding = 0;
		prepadding = 0;
		padding_pattern_length = 0;
		padding_pattern = null;
		fieldorder = XDEFDEFAULT;
		ptroffset = 0;
		ptrbase = null;
		unit = 8;
		toplevelind = 0;
		toplevel.bitorder = XDEFDEFAULT;
		length_restriction = -1;
		intX = false;
		stringformat = CharCoding.UNKNOWN;
	}
}
