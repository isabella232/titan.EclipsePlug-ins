package org.eclipse.titan.designer.AST.TTCN3.values;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.titan.designer.AST.TTCN3.types.CharString_Type.CharCoding;

/**
 * Utility functions for Unicode decoding.
 * Originally titan.core/compiler2/PredefFunc.cc
 * 
 * NOTE: Some converter functions are already implemented in other classes, so these are removed from here.
 * 
 * @author Arpad Lovassy
 */
public class PredefFunc {

	private PredefFunc() {
		// Hide constructor
	}

	//BOMs
	private static final String utf32be = "0000FEFF";
	private static final String utf32le = "FFFE0000";
	private static final String utf16be = "FEFF";
	private static final String utf16le = "FFFE";
	private static final String utf8    = "EFBBBF";

	public static class DecodeException extends Exception {

		private static final long serialVersionUID = -5356312750957137499L;

		public DecodeException(String msg) {
			super(msg);
		}
		
	}

	public static char hexdigit_to_char(char hexdigit) throws DecodeException {
		if (hexdigit < 10) {
			return (char) ('0' + hexdigit);
		} else if (hexdigit < 16) {
			return (char) ('A' + hexdigit - 10);
		}
		else {
			throw new DecodeException(MessageFormat.format("hexdigit_to_char(): invalid argument: {0}", hexdigit));
		}
	}

	public static char char_to_hexdigit(final char c) throws DecodeException {
		if (c >= '0' && c <= '9') {
			return (char) (c - '0');
		} else if (c >= 'A' && c <= 'F') {
			return (char) (c - 'A' + 10);
		} else if (c >= 'a' && c <= 'f') {
			return (char) (c - 'a' + 10);
		} else {
			throw new DecodeException(MessageFormat.format("char_to_hexdigit(): invalid argument: {0}", c));
		}
	}

	public static char str2uchar(final char c1, final char c2) throws DecodeException {
		char uc = 0;
		uc = char_to_hexdigit(c1);
		uc <<= 4;
		uc += char_to_hexdigit(c2);
		return uc;
	}

//TODO: implement
/*
	private static String regexp(final String instr, final String expression,
			final int groupno, boolean nocase) {
		String retval=0;

		if(groupno<0) {
			throw new DecodeException("regexp(): groupno must be a non-negative integer");
			return retval;
		}
		// do not report the warnings again
		// they were already reported while checking the operands
		int orig_verb_level = verb_level;
		verb_level &= ~(1|2);
		String posix_str=TTCN_pattern_to_regexp(expression.c_str());
		verb_level = orig_verb_level;
		if(posix_str==NULL) {
			throw new DecodeException(MessageFormat.format("regexp(): Cannot convert pattern `%s' to POSIX-equivalent.",
					expression.c_str());
			return retval;
		}

		regex_t posix_regexp;
		int ret_val=regcomp(&posix_regexp, posix_str, REG_EXTENDED |
				(nocase ? REG_ICASE : 0));
		Free(posix_str);
		if(ret_val!=0) {
			// regexp error
			char msg[ERRMSG_BUFSIZE];
			regerror(ret_val, &posix_regexp, msg, sizeof(msg));
			throw new DecodeException(MessageFormat.format("regexp(): regcomp() failed: %s", msg);
			return retval;
		}

		int nmatch=groupno+1;
		if(nmatch>posix_regexp.re_nsub) {
			throw new DecodeException(MessageFormat.format("regexp(): requested groupno is {0}, but this expression " +
					"contains only {1} group(s).", (long) (nmatch - 1),
					(long) posix_regexp.re_nsub));
			return retval;
		}
		regmatch_t* pmatch=(regmatch_t*)Malloc((nmatch+1)*sizeof(regmatch_t));
		ret_val=regexec(&posix_regexp, instr.c_str(), nmatch+1, pmatch, 0);
		if(ret_val==0) {
			if(pmatch[nmatch].rm_so != -1 && pmatch[nmatch].rm_eo != -1)
				retval = instr.substring(pmatch[nmatch].rm_so, pmatch[nmatch].rm_eo - pmatch[nmatch].rm_so);
			else retval=new String();
		}
		Free(pmatch);
		if(ret_val!=0) {
			if(ret_val==REG_NOMATCH) {
				regfree(&posix_regexp);
				retval=new String();
			}
			else {
				// regexp error
				char msg[ERRMSG_BUFSIZE];
				regerror(ret_val, &posix_regexp, msg, sizeof(msg));
				throw new DecodeException(MessageFormat.format("regexp(): regexec() failed: %s", msg);
			}
		}
		else {
			regfree(&posix_regexp);
		}

		return retval;
	}
//*/

//TODO: implement
/*
	private static ustring regexp(final ustring instr, final ustring expression, final int groupno, bool nocase) {
		ustring retval=0;

		if(groupno<0) {
			throw new DecodeException("regexp(): groupno must be a non-negative integer");
			return retval;
		}
		// do not report the warnings again
		// they were already reported while checking the operands
		int orig_verb_level = verb_level;
		verb_level &= ~(1|2);
		int user_groups;
		String posix_str = TTCN_pattern_to_regexp_uni(
				expression.get_stringRepr_for_pattern().c_str(), nocase, &user_groups);
		if (user_groups == 0)
			throw new DecodeException("regexp(): Cannot find any groups in the second argument.");
		verb_level = orig_verb_level;
		if(posix_str==null) {
			throw new DecodeException(MessageFormat.format("regexp(): Cannot convert pattern `{0}' to POSIX-equivalent.",
					expression.get_stringRepr()));
			return retval;
		}

		regex_t posix_regexp;
		int ret_val=regcomp(&posix_regexp, posix_str, REG_EXTENDED);
		Free(posix_str);
		if(ret_val!=0) {
			// regexp error
			char msg[ERRMSG_BUFSIZE];
			regerror(ret_val, &posix_regexp, msg, sizeof(msg));
			throw new DecodeException(MessageFormat.format("regexp(): regcomp() failed: %s", msg);
			return retval;
		}

		int nmatch=user_groups[groupno+1]+1;
		if(nmatch>posix_regexp.re_nsub) {
			throw new DecodeException(MessageFormat.format("regexp(): requested groupno is {0}, but this expression " +
					"contains only {1} group(s).", (long) (groupno),
					(long) user_groups[0]));
			return retval;
		}

		regmatch_t* pmatch = (regmatch_t*)Malloc((nmatch+1)*sizeof(regmatch_t));
		char* tmp = instr.convert_to_regexp_form();

		if (nocase) {
			unichar_pattern.convert_regex_str_to_lowercase(tmp);
		}

		string instr_conv(tmp);
		ret_val = regexec(&posix_regexp, instr_conv.c_str(), nmatch+1, pmatch, 0);
		if(ret_val == 0) {
			if(pmatch[nmatch].rm_so != -1 && pmatch[nmatch].rm_eo != -1) {
				retval = new ustring(instr.extract_matched_section(pmatch[nmatch].rm_so,
						pmatch[nmatch].rm_eo));
			} else { retval = new ustring(); }
		}
		if(ret_val!=0) {
			if(ret_val==REG_NOMATCH) {
				regfree(&posix_regexp);
				retval=new ustring();
			}
			else {
				// regexp error
				char msg[ERRMSG_BUFSIZE];
				regerror(ret_val, &posix_regexp, msg, sizeof(msg));
				throw new DecodeException(MessageFormat.format("regexp(): regexec() failed: %s", msg);
			}
		}
		else {
			regfree(&posix_regexp);
		}

		return retval;
	}
//*/

	/**
	 * Search the given BOM in a string
	 * @param s the string where we search
	 * @param bom the BOM we are searching for
	 * @return true if s starts with the given BOM
	 */
	private static boolean findBom(final String s, final String bom) {
		return s.regionMatches(true, 0, bom, 0, bom.length());
	}

	public static String remove_bom(final String encoded_value) throws DecodeException {
		final int length = encoded_value.length();
		if (0 == length) {
			return new String();
		}

		if (length % 2 != 0) {
			throw new DecodeException( MessageFormat.format("remove_bom(): Wrong string. The number of nibbles ({0}) in string " +
					"shall be divisible by 2", length));
			//TODO: remove
			//return encoded_value;
		}

		int length_of_BOM = 0;

		if      (findBom(encoded_value, utf32be)) length_of_BOM = utf32be.length();
		else if (findBom(encoded_value, utf32le)) length_of_BOM = utf32le.length();
		else if (findBom(encoded_value, utf16be)) length_of_BOM = utf16be.length();
		else if (findBom(encoded_value, utf16le)) length_of_BOM = utf16le.length();
		else if (findBom(encoded_value, utf8)) length_of_BOM = utf8.length();
		else return encoded_value; // no BOM found

		return encoded_value.substring(length_of_BOM, length);
	}

	public static CharCoding is_ascii (final int length, final String strptr) {
		final char nonASCII = 1 << 7;// MSB is 1 in case of non ASCII character  
		CharCoding ret = CharCoding.ASCII;
		for (int i = 0; i < length; ++i) {
			if ( ( strptr.charAt(i) & nonASCII ) != 0) {
				ret = CharCoding.UNKNOWN;
				break;
			}
		}
		return ret;
	}

	private static CharCoding is_utf8(final int length, final String strptr) {
		if (length > strptr.length()) {
			// string is too short to be UTF-8
			return CharCoding.UNKNOWN;
		}
		// MSB is 1 in case of non ASCII character
		final char MSB = 1 << 7;
		// 0100 0000
		final char MSBmin1 = 1 << 6;
		for ( int i = 0; length > i; ++i ) {
			if ( (strptr.charAt(i) & MSB) != 0) {
				// non ASCII char
				// 111x xxxx shows how many additional bytes are there
				char maskUTF8 = 1 << 6;
				if ((strptr.charAt(i) & maskUTF8) == 0) {
					// accepted 11xxx xxxx but received 10xx xxxx
					return CharCoding.UNKNOWN;
				}
				// 11xx xxxxx -> 2 bytes, 111x xxxxx -> 3 bytes , 1111 xxxxx -> 4 bytes in UTF-8
				int noofUTF8 = 0;
				while ( (strptr.charAt(i) & maskUTF8) != 0) {
					++noofUTF8;
					// shift right the mask
					maskUTF8 >>= 1;
				}
				// the second and third (and so on) UTF-8 byte looks like 10xx xxxx
				while (0 < noofUTF8 ) {
					++i;
					if ((strptr.charAt(i) & MSB) == 0 || (strptr.charAt(i) & MSBmin1) != 0 || i >= length) {
						// if not like this: 10xx xxxx
						return CharCoding.UNKNOWN;
					}
					--noofUTF8;
				}
			}
		}
		return CharCoding.UTF_8;
	}

	//TODO: remove
	/*
	public static String get_stringencoding(final String encoded_value) throws DecodeException {
		final int length = encoded_value.length();
		if (0 == length) {
			return "<unknown>";
		}

		if ( length % 2 != 0 ) {
			throw new DecodeException( MessageFormat.format(
					"get_stringencoding(): Wrong string. The number of nibbles ({0}) in string " +
							"shall be divisible by 2", length ));
			//TODO: remove
			//return "<unknown>";
		}

		if      (findBom(encoded_value, utf32be)) return "UTF-32BE";
		else if (findBom(encoded_value, utf32le)) return "UTF-32LE";
		else if (findBom(encoded_value, utf16be)) return "UTF-16BE";
		else if (findBom(encoded_value, utf16le)) return "UTF-16LE";
		else if (findBom(encoded_value, utf8)) return "UTF-8";

		final StringBuilder uc_str = new StringBuilder();
		final String ret;
		for (int i = 0; i < length / 2; ++i) {
			uc_str.append(str2uchar(encoded_value.charAt(2 * i), encoded_value.charAt(2 * i + 1)));
		}

		if (is_ascii(length / 2, uc_str.toString()) == CharCoding.ASCII) {
			ret = "ASCII";
		} else if (CharCoding.UTF_8 == is_utf8(length / 2, uc_str.toString())) {
			ret = "UTF-8";
		} else {
			ret = "<unknown>";
		}

		return ret;
	}
	*/

	public static CharCoding getCharCoding(final String encoded_value) {
		final int length = encoded_value.length();
		if (0 == length) {
			return CharCoding.UNKNOWN;
		}

		if ( length % 2 != 0 ) {
			// Wrong string. The number of nibbles ({0}) in string shall be divisible by 2
			return CharCoding.UNKNOWN;
		}

		if      (findBom(encoded_value, utf32be)) return CharCoding.UTF32BE;
		else if (findBom(encoded_value, utf32le)) return CharCoding.UTF32LE;
		else if (findBom(encoded_value, utf16be)) return CharCoding.UTF16BE;
		else if (findBom(encoded_value, utf16le)) return CharCoding.UTF16LE;
		else if (findBom(encoded_value, utf8)) return CharCoding.UTF_8;

		final StringBuilder uc_str = new StringBuilder();
		final CharCoding ret;
		for (int i = 0; i < length / 2; ++i) {
			try {
				uc_str.append(str2uchar(encoded_value.charAt(2 * i), encoded_value.charAt(2 * i + 1)));
			} catch (DecodeException e) {
				return CharCoding.UNKNOWN;
			}
		}

		if (is_ascii(length / 2, uc_str.toString()) == CharCoding.ASCII) {
			ret = CharCoding.ASCII;
		} else if (CharCoding.UTF_8 == is_utf8(length / 2, uc_str.toString())) {
			ret = CharCoding.UTF_8;
		} else {
			ret = CharCoding.UNKNOWN;
		}

		return ret;
	}

	private static int check_BOM(final CharCoding expected_coding, final int n_uc, final String uc_str) throws DecodeException {
		if (0 == n_uc) {
			return 0;
		}

		switch (expected_coding) {
		case UTF32:
		case UTF32BE:
		case UTF32LE:
			if (4 > n_uc) {
				throw new DecodeException("decode_utf32(): The string is shorter than the expected BOM");
			}
			break;
		case UTF16:
		case UTF16BE:
		case UTF16LE:
			if (2 > n_uc) {
				throw new DecodeException("decode_utf16(): The string is shorter than the expected BOM");
			}
			break;
		default: break;
		}

		//BOM indicates that the byte order is determined by a byte order mark, 
		//if present at the beginning the length of BOM is returned.
		boolean badBOM = false;
		String errmsg = "<UNKNOWN>";
		String caller = "<UNKNOWN>";
		switch (expected_coding) {
		case UTF32BE:
		case UTF32:
			if (0x00 == uc_str.charAt(0) && 0x00 == uc_str.charAt(1) && 0xFE == uc_str.charAt(2) && 0xFF == uc_str.charAt(3)) { 
				return 4;
			}
			badBOM = true;
			caller = "decode_utf32()";
			errmsg = "UTF-32BE";
			break;
		case UTF32LE:
			if (0xFF == uc_str.charAt(0) && 0xFE == uc_str.charAt(1) && 0x00 == uc_str.charAt(2) && 0x00 == uc_str.charAt(3)) {
				return 4;
			}
			badBOM = true;
			caller = "decode_utf32()";
			errmsg = "UTF-32LE";
			break;
		case UTF16BE:
		case UTF16:
			if (0xFE == uc_str.charAt(0) && 0xFF == uc_str.charAt(1)) {
				return 2;
			}
			badBOM = true;
			caller = "decode_utf16()";
			errmsg = "UTF-16BE";
			break;
		case UTF16LE:
			if (0xFF == uc_str.charAt(0) && 0xFE == uc_str.charAt(1)) {
				return 2;
			}
			badBOM = true;
			caller = "decode_utf16()";
			errmsg = "UTF-16LE";
			break;
		case UTF_8:
			if (0xEF == uc_str.charAt(0) && 0xBB == uc_str.charAt(1) && 0xBF == uc_str.charAt(2)) {
				return 3;
			}
			return 0;
		default:
			if (CharCoding.UTF32 == expected_coding || CharCoding.UTF16 == expected_coding) {
				final String str = CharCoding.UTF32 == expected_coding ? "UTF-32" : "UTF-16";
				throw new DecodeException(MessageFormat.format(
						"Wrong {0} string. No BOM detected, however the given coding type ({1}) " +
								"expects it to define the endianness", str, str));
			}
			else {
				throw new DecodeException("Wrong string. No BOM detected");
			}
		}
		if (badBOM) {
			throw new DecodeException(MessageFormat.format(
				"{0}: Wrong {1} string. The expected coding could not be verified",
				caller, errmsg));
		}
		return 0;
	}

	private static void fill_continuing_octets(final int n_continuing, final List<Character> continuing_ptr,
			final int n_uc, final StringBuilder uc_str, final int start_pos, final int uchar_pos) throws DecodeException {
		for (int i = 0; i < n_continuing; i++) {
			if (start_pos + i < n_uc) {
				final char octet = uc_str.charAt(start_pos + i);
				if ((octet & 0xC0) != 0x80) {
					throw new DecodeException(MessageFormat.format(
							"decode_utf8(): Malformed: At character position {0}, octet position {1}: {2} is " +
									"not a valid continuing octet.", uchar_pos, start_pos + i, String.format("0x%02X", octet)));
				}
				continuing_ptr.add((char) (octet & 0x3F));
			} 
			else {
				if (start_pos + i == n_uc) {
					if (i > 0) {
						// only a part of octets is missing
						throw new DecodeException(MessageFormat.format(
								"decode_utf8(): Incomplete: At character position {0}, octet position {1}: {2} out " +
										"of {3} continuing octets {4} missing from the end of the stream.",
										uchar_pos, start_pos + i, n_continuing - i, n_continuing,
										n_continuing - i > 1 ? "are" : "is"));
					}
					else {
						// all octets are missing
						throw new DecodeException(MessageFormat.format(
								"decode_utf8(): Incomplete: At character position {0}, octet position {1}: {2} " +
										"continuing octet{3} missing from the end of the stream.", uchar_pos,
										start_pos, n_continuing, n_continuing > 1 ? "s are" : " is"));
					}
				}
				continuing_ptr.add((char) 0);
			}
		}
	}

	public static UniversalCharstring decode_utf8(final String ostr, final CharCoding expected_coding) throws Exception {
		final int length = ostr.length();
		if (0 == length) {
			return new UniversalCharstring();
		}
		if (length % 2 != 0) {
			throw new DecodeException(MessageFormat.format(
					"decode_utf8(): Wrong UTF-8 string. The number of nibbles ({0}) in octetstring " +
							"shall be divisible by 2", length));
		}

		final StringBuilder uc_str = new StringBuilder();
		for (int i = 0; i < length / 2; ++i) {
			uc_str.append(str2uchar(ostr.charAt(2 * i), ostr.charAt(2 * i + 1)));
		}
		final UniversalCharstring ucstr = new UniversalCharstring();
		final int start = check_BOM(CharCoding.UTF_8, length /2, uc_str.toString());

		for (int i = start; i < length / 2;) {
			// perform the decoding character by character
			if (uc_str.charAt(i) <= 0x7F) {
				// character encoded on a single octet: 0xxxxxxx (7 useful bits)
				final char g = 0;
				final char p = 0;
				final char r = 0;
				final char c = uc_str.charAt(i);
				ucstr.append(new UniversalChar(g, p, r, c));
				++i;
			}
			else if (uc_str.charAt(i) <= 0xBF) {
				// continuing octet (10xxxxxx) without leading octet ==> malformed
				throw new DecodeException(MessageFormat.format(
						"decode_utf8(): Malformed: At character position {0}, octet position {1}: continuing " +
								"octet {2} without leading octet.", ucstr.length(),
								i, String.format("0x%02X",uc_str.charAt(i))));
			}
			else if (uc_str.charAt(i) <= 0xDF) {
				// character encoded on 2 octets: 110xxxxx 10xxxxxx (11 useful bits)
				final List<Character> octets = new ArrayList<Character>();
				octets.add( (char) (uc_str.charAt(i) & 0x1F) );
				fill_continuing_octets(1, octets, length / 2, uc_str, i + 1, ucstr.length());
				final char g = 0;
				final char p = 0;
				final char r = (char) (octets.get(0) >> 2);
				final char c = (char) (octets.get(0) << 6 | octets.get(1));
				if (r == 0x00 && c < 0x80) {
					throw new DecodeException(MessageFormat.format(
							"decode_utf8(): Overlong: At character position {0}, octet position {1}: 2-octet " +
									"encoding for quadruple (0, 0, 0, {2}).", ucstr.length(), i, c));
				}
				ucstr.append(new UniversalChar(g, p, r, c));
				i += 2;
			} 
			else if (uc_str.charAt(i) <= 0xEF) {
				// character encoded on 3 octets: 1110xxxx 10xxxxxx 10xxxxxx
				// (16 useful bits)
				final List<Character> octets = new ArrayList<Character>();
				octets.add( (char) (uc_str.charAt(i) & 0x0F) );
				fill_continuing_octets(2, octets, length / 2, uc_str, i + 1,ucstr.length());
				final char g = 0;
				final char p = 0;
				final char r = (char) (octets.get(0) << 4 | octets.get(1) >> 2);
				final char c = (char) (octets.get(1) << 6 | octets.get(2));
				if (r < 0x08) {
					throw new DecodeException(MessageFormat.format(
							"decode_utf8(): Overlong: At character position {0}, octet position {1}: 3-octet " +
									"encoding for quadruple (0, 0, {2}, {3}).", ucstr.length(), i, r, c));
				}
				ucstr.append(new UniversalChar(g, p, r, c));
				i += 3;
			} 
			else if (uc_str.charAt(i) <= 0xF7) {
				// character encoded on 4 octets: 11110xxx 10xxxxxx 10xxxxxx 10xxxxxx
				// (21 useful bits)
				final List<Character> octets = new ArrayList<Character>();
				octets.add( (char) (uc_str.charAt(i) & 0x07) );
				fill_continuing_octets(3, octets, length / 2, uc_str, i + 1, ucstr.length());
				final char g = 0;
				final char p = (char) (octets.get(0) << 2 | octets.get(1) >> 4);
				final char r = (char) (octets.get(1) << 4 | octets.get(2) >> 2);
				final char c = (char) (octets.get(2) << 6 | octets.get(3));
				if (p == 0x00) {
					throw new DecodeException(MessageFormat.format(
							"decode_utf8(): Overlong: At character position {0}, octet position {1}: 4-octet " +
									"encoding for quadruple (0, 0, {2}, {3}).", ucstr.length(), i, r, c));
				}
				ucstr.append(new UniversalChar(g, p, r, c));
				i += 4;
			}
			else if (uc_str.charAt(i) <= 0xFB) {
				// character encoded on 5 octets: 111110xx 10xxxxxx 10xxxxxx 10xxxxxx
				// 10xxxxxx (26 useful bits)
				final List<Character> octets = new ArrayList<Character>();
				octets.add( (char) (uc_str.charAt(i) & 0x03) );
				fill_continuing_octets(4, octets, length / 2, uc_str, i + 1, ucstr.length());
				final char g = octets.get(0);
				final char p = (char) (octets.get(1) << 2 | octets.get(2) >> 4);
				final char r = (char) (octets.get(2) << 4 | octets.get(3) >> 2);
				final char c = (char) (octets.get(3) << 6 | octets.get(4));
				if (g == 0x00 && p < 0x20) {
					throw new DecodeException(MessageFormat.format(
							"decode_utf8(): Overlong: At character position {0}, octet position {1}: 5-octet " +
									"encoding for quadruple (0, {2}, {3}, {4}).", ucstr.length(), i, p, r, c));
				}
				ucstr.append(new UniversalChar(g, p, r, c));
				i += 5;
			}
			else if (uc_str.charAt(i) <= 0xFD) {
				// character encoded on 6 octets: 1111110x 10xxxxxx 10xxxxxx 10xxxxxx
				// 10xxxxxx 10xxxxxx (31 useful bits)
				final List<Character> octets = new ArrayList<Character>();
				octets.add( (char) (uc_str.charAt(i) & 0x01) );
				fill_continuing_octets(5, octets, length / 2, uc_str, i + 1,ucstr.length());
				final char g = (char) (octets.get(0) << 6 | octets.get(1));
				final char p = (char) (octets.get(2) << 2 | octets.get(3) >> 4);
				final char r = (char) (octets.get(3) << 4 | octets.get(4) >> 2);
				final char c = (char) (octets.get(4) << 6 | octets.get(5));
				if (g < 0x04) {
					throw new DecodeException(MessageFormat.format(
							"decode_utf8(): Overlong: At character position {0}, octet position {1}: 6-octet " +
									"encoding for quadruple ({2}, {3}, {4}, {5}).", ucstr.length(), i, g, p, r, c));
				}
				ucstr.append(new UniversalChar(g, p, r, c));
				i += 6;
			}
			else {
				// not used code points: FE and FF => malformed
				throw new DecodeException(MessageFormat.format(
						"decode_utf8(): Malformed: At character position {0}, octet position {1}: " +
								"unused/reserved octet {2}.", ucstr.length(), i, String.format("0x%02X", uc_str.charAt(i))));
			}
		}
		return ucstr;
	}
}
