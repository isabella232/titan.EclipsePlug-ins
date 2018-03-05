package org.eclipse.titan.designer.AST.TTCN3.values;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.AST.TTCN3.types.CharString_Type.CharCoding;

/**
 * Utility functions for Unicode decoding.
 * Originally titan.core/compiler2/PredefFunc.cc
 * @author Arpad Lovassy
 */
public class PredefFunc {

	/**
	 * Class for handling universal (multi-byte) string values.
	 */
	private static class ustring {
		public class universal_char {
			private char group, plane, row, cell;

			public universal_char(char aGroup, char aPlane, char aRow, char aCell) {
				this.group = aGroup;
				this.plane = aPlane;
				this.row = aRow;
				this.cell = aCell;
			}

		}

		private List<universal_char> uchars;
		
		public ustring() {
			uchars = new ArrayList<universal_char>();
		}

		/**
		 * Constructor for creating unistring with 1 unichar element
		 * @param group
		 * @param plane
		 * @param row
		 * @param cell
		 */
		public ustring(char group, char plane, char row, char cell) {
			this();
			add(group, plane, row, cell);
		}

		public universal_char get( final int i ) {
			return uchars.get(i);
		}
		
		public int size() {
			return uchars.size();
		}

		public void add(final universal_char uc) {
			uchars.add(uc);
		}

		public void add(char group, char plane, char row, char cell) {
			final universal_char uc = new universal_char(group, plane, row, cell);
			add(uc);
		}
	}

	//BOMs
	private static final String utf32be = "0000FEFF";
	private static final String utf32le = "FFFE0000";
	private static final String utf16be = "FEFF";
	private static final String utf16le = "FFFE";
	private static final String utf8    = "EFBBBF";

	public static char get_bit_value(char c, char bit_value) {
		switch (c) {
		case '0':
			return 0;
		case '1':
			return bit_value;
		default:
			ErrorReporter.INTERNAL_ERROR(MessageFormat.format("Invalid binary digit ({0}) in bitstring value", c));
			return 0;
		}
	}

	public static char toupper(final char c) {
		if (('A' <= c && 'F' >= c) ||
				('0' <= c && '9' >= c)) return c;
		switch (c)
		{
		case 'a' : return 'A';
		case 'b' : return 'B';
		case 'c' : return 'C';
		case 'd' : return 'D';
		case 'e' : return 'E';
		case 'f' : return 'F';
		default:
			ErrorReporter.INTERNAL_ERROR(MessageFormat.format("{0} cannot be converted to hex character", c));
			return '\0';
		}
	}

	public static char hexdigit_to_char(char hexdigit)	{
		if (hexdigit < 10) return (char) ('0' + hexdigit);
		else if (hexdigit < 16) return (char) ('A' + hexdigit - 10);
		else {
			ErrorReporter.INTERNAL_ERROR(MessageFormat.format("hexdigit_to_char(): invalid argument: {0}", hexdigit));
			return '\0'; // to avoid warning
		}
	}

	public static char char_to_hexdigit(char c) {
		if (c >= '0' && c <= '9') return (char) (c - '0');
		else if (c >= 'A' && c <= 'F') return (char) (c - 'A' + 10);
		else if (c >= 'a' && c <= 'f') return (char) (c - 'a' + 10);
		else {
			ErrorReporter.INTERNAL_ERROR(MessageFormat.format("char_to_hexdigit(): invalid argument: {0}", c));
			return 0; // to avoid warning
		}
	}

	public static String uchar2str(char uchar) {
		char[] str = new char[2];
		str[0] = hexdigit_to_char((char) (uchar / 16));
		str[1] = hexdigit_to_char((char) (uchar % 16));
		return new String(str);
	}

	public static char str2uchar(final char c1, final char c2) {
		char uc = 0;
		uc = char_to_hexdigit(c1);
		uc <<= 4;
		uc += char_to_hexdigit(c2);
		return uc;
	}

	public static int rem(final int left, final int right) {
		return (left - right * (left / right));
	}

	public static int mod(final int left, final int right) {
		int r = right < 0 ? -right : right;
		if (left > 0) {
			return rem(left, r);
		} else {
			int result = rem(left, r);
			return result == 0 ? result : result + r;
		}
	}

	public static String to_uppercase(final String value) {
		StringBuilder s = new StringBuilder(value);
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (c >= 'a' && c <= 'z') {
				s.setCharAt(i, (char) (c - 'a' + 'A'));
			}
		}
		return s.toString();
	}

	public static String not4b_bit(final String bstr) {
		StringBuilder s = new StringBuilder(bstr);
		for(int i=0; i<s.length(); i++) {
			char c = s.charAt(i);
			switch(c) {
			case '0':
				s.setCharAt(i, '1');
				break;
			case '1':
				s.setCharAt(i, '0');
				break;
			default:
				ErrorReporter.INTERNAL_ERROR("not4b_bit(): Invalid char in bitstring.");
			} // switch c
		} // for i
		return s.toString();
	}

	public static String not4b_hex(final String hstr) {
		StringBuilder s = new StringBuilder(hstr);
		for(int i=0; i<s.length(); i++) {
			char c = s.charAt(i);
			switch(c) {
			case '0': s.setCharAt(i, 'F'); break;
			case '1': s.setCharAt(i, 'E'); break;
			case '2': s.setCharAt(i, 'D'); break;
			case '3': s.setCharAt(i, 'C'); break;
			case '4': s.setCharAt(i, 'B'); break;
			case '5': s.setCharAt(i, 'A'); break;
			case '6': s.setCharAt(i, '9'); break;
			case '7': s.setCharAt(i, '8'); break;
			case '8': s.setCharAt(i, '7'); break;
			case '9': s.setCharAt(i, '6'); break;
			case 'A': s.setCharAt(i, '5'); break;
			case 'B': s.setCharAt(i, '4'); break;
			case 'C': s.setCharAt(i, '3'); break;
			case 'D': s.setCharAt(i, '2'); break;
			case 'E': s.setCharAt(i, '1'); break;
			case 'F': s.setCharAt(i, '0'); break;
			case 'a': s.setCharAt(i, '5'); break;
			case 'b': s.setCharAt(i, '4'); break;
			case 'c': s.setCharAt(i, '3'); break;
			case 'd': s.setCharAt(i, '2'); break;
			case 'e': s.setCharAt(i, '1'); break;
			case 'f': s.setCharAt(i, '0'); break;
			default:
				ErrorReporter.INTERNAL_ERROR("not4b_hex(): Invalid char in hexstring.");
			} // switch c
		} // for i
		return s.toString();
	}

	public static String and4b(final String left, final String right) {
		StringBuilder s = new StringBuilder(left);
		for(int i=0; i<s.length(); i++) {
			char c = s.charAt(i);
			s.setCharAt(i, hexdigit_to_char((char) (char_to_hexdigit(c) & char_to_hexdigit(right.charAt(i)))));
		} // for i
		return s.toString();
	}

	public static String or4b(final String left, final String right) {
		StringBuilder s = new StringBuilder(left);
		for(int i=0; i<s.length(); i++) {
			char c = s.charAt(i);
			s.setCharAt(i, hexdigit_to_char((char) (char_to_hexdigit(c) | char_to_hexdigit(right.charAt(i)))));
		} // for i
		return s.toString();
	}

	public static String xor4b(final String left, final String right) {
		StringBuilder s = new StringBuilder(left);
		for(int i=0; i<s.length(); i++) {
			char c = s.charAt(i);
			s.setCharAt(i, hexdigit_to_char((char) (char_to_hexdigit(c) ^ char_to_hexdigit(right.charAt(i)))));
		} // for i
		return s.toString();
	}

	public static String shift_left(final String value, final int count) {
		if (count > 0) {
			StringBuilder s = new StringBuilder();
			if (count < value.length()) {
				s.append(value.substring(count));
				for ( int i = 0; i < count; i++ ) {
					s.append('0');
				}
			} else {
				for ( int i = 0; i < value.length(); i++ ) {
					s.append('0');
				}
			}
			return s.toString();
		} else if (count < 0) return shift_right(value, -count);
		else return new String(value);
	}

	public static String shift_right(final String value, final int count) {
		if (count > 0) {
			StringBuilder s = new StringBuilder();
			if (count < value.length()) {
				for ( int i = 0; i < count; i++ ) {
					s.append('0');
				}
				s.append(value.substring(0, value.length()-count));
			} else {
				for ( int i = 0; i < value.length(); i++ ) {
					s.append('0');
				}
			}
			return s.toString();
		} else if (count < 0) return shift_left(value, -count);
		else return new String(value);
	}

	public static String rotate_left(final String value, final int p_count) {
		int size = value.length();
		if (size == 0) return new String(value);
		else if (p_count < 0) return rotate_right(value, -p_count);
		int count = p_count % size;
		if (count == 0) return new String(value);
		else return new String(value.substring(count) + value.substring(0, count));
	}

	public static String rotate_right(final String value, final int p_count) {
		int size = value.length();
		if (size == 0) return new String(value);
		else if (p_count < 0) return rotate_left(value, -p_count);
		int count = p_count % size;
		if (count == 0) return new String(value);
		else return new String(value.substring(size - count) +
				value.substring(0, size - count));
	}

//TODO: implement
/*
	private static ustring rotate_left(final ustring value, final int p_count) {
		int size = value.size();
		if (size == 0) return new ustring(value);
		else if (p_count < 0) return rotate_right(value, -p_count);
		int count = p_count % size;
		if (count == 0) return new ustring(value);
		else return new ustring(value.substring(count) + value.substring(0, count));
	}

	private static ustring rotate_right(final ustring value, final int p_count) {
		int size = value.size();
		if (size == 0) return new ustring(value);
		else if (p_count < 0) return rotate_left(value, -p_count);
		int count = p_count % size;
		if (count == 0) return new ustring(value);
		else return new ustring(value.substring(size - count) +
				value.substring(0, size - count));
	}
//*/

	public static int bit2int(final String bstr) {
		int nof_bits = bstr.length();
		// skip the leading zeros
		int start_index = 0;
		while (start_index < nof_bits && bstr.charAt(start_index) == '0') {
			start_index++;
		}
		int ret_val = 0;
		for (int i = start_index; i < nof_bits; i++) {
			ret_val <<= 1;
			if (bstr.charAt(i) == '1') ret_val += 1;
		}
		return ret_val;
	}

	public static int hex2int(final String hstr) {
		int nof_digits = hstr.length();
		int start_index = 0;
		// Skip the leading zeros.
		while (start_index < nof_digits && hstr.charAt(start_index) == '0')
			start_index++;
		int ret_val = 0;
		for (int i = start_index; i < nof_digits; i++) {
			ret_val <<= 4;
			ret_val += char_to_hexdigit(hstr.charAt(i));
		}
		return ret_val;
	}

	public static int unichar2int(final ustring ustr) {
		if (ustr.size() != 1) ErrorReporter.INTERNAL_ERROR("unichar2int(): invalid argument");
		final ustring.universal_char uchar = ustr.get(0);
		int ret_val = (uchar.group << 24) | (uchar.plane << 16) | (uchar.row << 8) |
				uchar.cell;
		return ret_val;
	}

	public static String int2bit(final int value, final int length) {
		if (length < 0) ErrorReporter.INTERNAL_ERROR("int2bit(): negative length");
		int string_length = length;
		if (string_length != length ||
				string_length > Integer.MAX_VALUE)
			ErrorReporter.INTERNAL_ERROR("int2bit(): length is too large");
			if (value < 0) ErrorReporter.INTERNAL_ERROR("int2bit(): negative value");
			StringBuilder bstr = new StringBuilder();
			int tmp_value = value;
			for (int i = 1; i <= string_length; i++) {
				bstr.insert(0, (tmp_value & 1 ) != 0 ? '1' : '0');
				tmp_value >>= 1;
			}
			if (tmp_value != 0)
				ErrorReporter.INTERNAL_ERROR(MessageFormat.format("int2bit(): {0} does not fit in {1} bits",
						value, (long)string_length));
				return bstr.toString();
	}

	private static final char hdigits[] = { '0', '1', '2', '3', '4', '5', '6', '7',	'8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

	public static String int2hex(final int value, final int length) {
		if (length < 0)
			ErrorReporter.INTERNAL_ERROR("int2hex(): negative length");
			int string_length = length;
			if (string_length != length ||
					string_length > Integer.MAX_VALUE)
				ErrorReporter.INTERNAL_ERROR("int2hex(): length is too large");
				if (value < 0) ErrorReporter.INTERNAL_ERROR("int2hex(): negative value");
				StringBuilder hstr = new StringBuilder();
				int tmp_value = value;
				for (int i = 1; i <= string_length; i++) {
					hstr.insert(0, hdigits[tmp_value & 0x0f]);
					tmp_value >>= 4;
				}
				if (tmp_value != 0) {
					ErrorReporter.INTERNAL_ERROR(MessageFormat.format("int2hex(): {0} does not fit in {1} hexadecimal digits",
							value, (long)string_length));
				}
				return hstr.toString();
	}

	public static ustring int2unichar(final int value) {
		if (value < 0 || value > 2147483647)
			ErrorReporter.INTERNAL_ERROR("int2unichar(): invalid argument");
			char group = (char) ((value >> 24) & 0xFF),
					plane = (char) ((value >> 16) & 0xFF),
					row = (char) ((value >> 8) & 0xFF),
					cell = (char) (value & 0xFF);
			return new ustring(group, plane, row, cell);
	}

	public static String oct2char(final String ostr) {
		StringBuilder cstr = new StringBuilder();
		int ostr_size = ostr.length();
		if (ostr_size % 2 != 0)
			ErrorReporter.INTERNAL_ERROR(MessageFormat.format("oct2char(): argument has odd length: {0}", (long) ostr_size));
			int cstr_size = ostr_size / 2;
			for (int i = 0; i < cstr_size; i++) {
				char c = (char) (16 * char_to_hexdigit(ostr.charAt(2 * i)) +
						char_to_hexdigit(ostr.charAt(2 * i + 1)));
				if (c > 127) ErrorReporter.INTERNAL_ERROR(MessageFormat.format("oct2char(): resulting charstring contains " +
						"non-ascii character: {0}", c));
				cstr.append(c);
			}
			return cstr.toString();
	}

	public static String char2oct(final String cstr) {
		StringBuilder ostr = new StringBuilder();
		int cstr_size = cstr.length();
		for (int i = 0; i < cstr_size; i++) {
			char c = cstr.charAt(i);
			ostr.append(hexdigit_to_char((char) (c / 16)));
			ostr.append(hexdigit_to_char((char) (c % 16)));
		}
		return ostr.toString();
	}

	public static String bit2hex(final String bstr) {
		StringBuilder bstr4 = new StringBuilder(bstr);
		while ( bstr4.length() % 4 != 0 ) {
			bstr4.insert(0, '0');
		}
		StringBuilder hstr = new StringBuilder();
		for ( int i = 0; i < bstr4.length() / 4; i++ ) {
			final StringBuilder b4 = new StringBuilder(bstr4.substring(i*4,4));
			int u = 0;
			if(b4.charAt(0)=='1') u+=8;
			if(b4.charAt(1)=='1') u+=4;
			if(b4.charAt(2)=='1') u+=2;
			if(b4.charAt(3)=='1') u++;
			hstr.append(hdigits[u]);
		}
		return hstr.toString();
	}

	public static String hex2oct(final String hstr) {
		if ( hstr.length() % 2 == 0 ) {
			return hstr;
		} else {
			return "0" + hstr;
		}
	}

	public static String asn_hex2oct(final String hstr) {
		if ( hstr.length() % 2 == 0 ) {
			return hstr;
		} else {
			return hstr + "0";
		}
	}

	public static String bit2oct(final String bstr) {
		final String s1 = bit2hex(bstr);
		final String s2 = hex2oct(s1);
		return s2;
	}

//TODO: implement
/*
	private static String asn_bit2oct(final String bstr) {
		int size = bstr.length();
		StringBuilder ostr = new StringBuilder();
		ostr.resize(((size+7)/8)*2);
		for(int i=0, j=0; i<size; ) {
			char digit1=0, digit2=0;
			digit1 += get_bit_value(bstr[i++], 8);
			if (i < size) {
				digit1 += get_bit_value(bstr[i++], 4);
				if (i < size) {
					digit1 += get_bit_value(bstr[i++], 2);
					if (i < size) {
						digit1 += get_bit_value(bstr[i++], 1);
						if (i < size) {
							digit2 += get_bit_value(bstr[i++], 8);
							if (i < size) {
								digit2 += get_bit_value(bstr[i++], 4);
								if (i < size) {
									digit2 += get_bit_value(bstr[i++], 2);
									if (i < size) digit2 += get_bit_value(bstr[i++], 1);
								}
							}
						}
					}
				}
			}
			(*ostr)[j++] = hexdigit_to_char(digit1);
			(*ostr)[j++] = hexdigit_to_char(digit2);
		}
		return ostr.toString();
	}
//*/

	public static String hex2bit(final String hstr) {
		final int size=hstr.length();
		StringBuilder bstr = new StringBuilder();
		for(int i = 0; i < size; i++) {
			switch(hstr.charAt(i)) {
			case '0':
				bstr.append("0000");
				break;
			case '1':
				bstr.append("0001");
				break;
			case '2':
				bstr.append("0010");
				break;
			case '3':
				bstr.append("0011");
				break;
			case '4':
				bstr.append("0100");
				break;
			case '5':
				bstr.append("0101");
				break;
			case '6':
				bstr.append("0110");
				break;
			case '7':
				bstr.append("0111");
				break;
			case '8':
				bstr.append("1000");
				break;
			case '9':
				bstr.append("1001");
				break;
			case 'A':
			case 'a':
				bstr.append("1010");
				break;
			case 'B':
			case 'b':
				bstr.append("1011");
				break;
			case 'C':
			case 'c':
				bstr.append("1100");
				break;
			case 'D':
			case 'd':
				bstr.append("1101");
				break;
			case 'E':
			case 'e':
				bstr.append("1110");
				break;
			case 'F':
			case 'f':
				bstr.append("1111");
				break;
			default:
				ErrorReporter.INTERNAL_ERROR("Common::hex2bit(): invalid hexadecimal digit in hexstring value");
			}
		}
		return bstr.toString();
	}

//TODO: implement
/*
	private static int float2int(final Real& value, final Location& loc) {
		// We shouldn't mimic generality with `Int'.
		if (value >= (Real)LLONG_MIN && value <= (Real)LLONG_MAX)
			return new int((Int)value);
		char buf[512] = "";
		snprintf(buf, 511, "%f", value);
		String dot = strchr(buf, '.');
		if (!dot) ErrorReporter.INTERNAL_ERROR(MessageFormat.format("Conversion of float value `%f' to integer failed", value);
		else memset(dot, 0, sizeof(buf) - (dot - buf));
		return new int(buf, loc);
	}
//*/

//TODO: implement
/*
	// TTCN-3 float values that have absolute value smaller than this are
	// displayed in exponential notation. Same as in core/Float.hh
	#ifndef MIN_DECIMAL_FLOAT
	#define MIN_DECIMAL_FLOAT		1.0E-4
	#endif
	// TTCN-3 float values that have absolute value larger or equal than
	// this are displayed in exponential notation. Same as in
	// core/Float.hh
	#ifndef MAX_DECIMAL_FLOAT
	#define MAX_DECIMAL_FLOAT		1.0E+10
	#endif

	private static String float2str(final Real& value) {
		if (value == REAL_INFINITY) {
			return new String("infinity");
		}
		if (value == -REAL_INFINITY) {
			return new String("-infinity");
		}
		if (value != value) {
			return new String("not_a_number");
		}
		char str_buf[64];
		if ( (value > -MAX_DECIMAL_FLOAT && value <= -MIN_DECIMAL_FLOAT)
				|| (value >= MIN_DECIMAL_FLOAT && value <   MAX_DECIMAL_FLOAT)
				|| (value == 0.0))
			snprintf(str_buf,64,"%f",value);
		else snprintf(str_buf,64,"%e",value);
		return new String(str_buf);
	}

	private static String regexp(final String instr, final String expression,
			final int groupno, boolean nocase) {
		String retval=0;

		if(groupno<0) {
			ErrorReporter.INTERNAL_ERROR("regexp(): groupno must be a non-negative integer");
			return retval;
		}
		// do not report the warnings again
		// they were already reported while checking the operands
		int orig_verb_level = verb_level;
		verb_level &= ~(1|2);
		String posix_str=TTCN_pattern_to_regexp(expression.c_str());
		verb_level = orig_verb_level;
		if(posix_str==NULL) {
			ErrorReporter.INTERNAL_ERROR(MessageFormat.format("regexp(): Cannot convert pattern `%s' to POSIX-equivalent.",
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
			ErrorReporter.INTERNAL_ERROR(MessageFormat.format("regexp(): regcomp() failed: %s", msg);
			return retval;
		}

		int nmatch=groupno+1;
		if(nmatch>posix_regexp.re_nsub) {
			ErrorReporter.INTERNAL_ERROR(MessageFormat.format("regexp(): requested groupno is {0}, but this expression " +
					"contains only {1} group(s).", (long) (nmatch - 1),
					(long) posix_regexp.re_nsub));
			return retval;
		}
		regmatch_t* pmatch=(regmatch_t*)Malloc((nmatch+1)*sizeof(regmatch_t));
		ret_val=regexec(&posix_regexp, instr.c_str(), nmatch+1, pmatch, 0);
		if(ret_val==0) {
			if(pmatch[nmatch].rm_so != -1 && pmatch[nmatch].rm_eo != -1)
				retval = new String(instr.substring(pmatch[nmatch].rm_so,
						pmatch[nmatch].rm_eo - pmatch[nmatch].rm_so));
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
				ErrorReporter.INTERNAL_ERROR(MessageFormat.format("regexp(): regexec() failed: %s", msg);
			}
		}
		else regfree(&posix_regexp);

		return retval;
	}
//*/

//TODO
/*
	private static ustring regexp(final ustring instr, final ustring expression, final int groupno, bool nocase) {
		ustring retval=0;

		if(groupno<0) {
			ErrorReporter.INTERNAL_ERROR("regexp(): groupno must be a non-negative integer");
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
			ErrorReporter.INTERNAL_ERROR("regexp(): Cannot find any groups in the second argument.");
		verb_level = orig_verb_level;
		if(posix_str==null) {
			ErrorReporter.INTERNAL_ERROR(MessageFormat.format("regexp(): Cannot convert pattern `{0}' to POSIX-equivalent.",
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
			ErrorReporter.INTERNAL_ERROR(MessageFormat.format("regexp(): regcomp() failed: %s", msg);
			return retval;
		}

		int nmatch=user_groups[groupno+1]+1;
		if(nmatch>posix_regexp.re_nsub) {
			ErrorReporter.INTERNAL_ERROR(MessageFormat.format("regexp(): requested groupno is {0}, but this expression " +
					"contains only {1} group(s).", (long) (groupno),
					(long) user_groups[0]));
			return retval;
		}

		Free(user_groups);

		regmatch_t* pmatch = (regmatch_t*)Malloc((nmatch+1)*sizeof(regmatch_t));
		char* tmp = instr.convert_to_regexp_form();

		if (nocase) {
			unichar_pattern.convert_regex_str_to_lowercase(tmp);
		}

		string instr_conv(tmp);
		Free(tmp);
		ret_val = regexec(&posix_regexp, instr_conv.c_str(), nmatch+1, pmatch, 0);
		if(ret_val == 0) {
			if(pmatch[nmatch].rm_so != -1 && pmatch[nmatch].rm_eo != -1) {
				retval = new ustring(instr.extract_matched_section(pmatch[nmatch].rm_so,
						pmatch[nmatch].rm_eo));
			} else { retval = new ustring(); }
		}
		Free(pmatch);
		if(ret_val!=0) {
			if(ret_val==REG_NOMATCH) {
				regfree(&posix_regexp);
				retval=new ustring();
			}
			else {
				// regexp error
				char msg[ERRMSG_BUFSIZE];
				regerror(ret_val, &posix_regexp, msg, sizeof(msg));
				ErrorReporter.INTERNAL_ERROR(MessageFormat.format("regexp(): regexec() failed: %s", msg);
			}
		}
		else regfree(&posix_regexp);

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
	
	public static String remove_bom(final String encoded_value) {
		int length = encoded_value.length();
		if (0 == length) return new String();
		if (length % 2 != 0) {
			ErrorReporter.INTERNAL_ERROR( MessageFormat.format("remove_bom(): Wrong string. The number of nibbles ({0}) in string " +
					"shall be divisible by 2", length));
			return new String(encoded_value);
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

	public static CharCoding is_ascii (int length, final String strptr) {
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

	private static CharCoding is_utf8(int length, final String strptr) {
		final char MSB = 1 << 7; // MSB is 1 in case of non ASCII character  
		final char MSBmin1 = 1 << 6; // 0100 0000   
		int i = 0;
		while (length > i) {
			if ( (strptr.charAt(i) & MSB) != 0) { // non ASCII char
				char maskUTF8 = 1 << 6; // 111x xxxx shows how many additional bytes are there
				if ((strptr.charAt(i) & maskUTF8) == 0) return CharCoding.UNKNOWN; // accepted 11xxx xxxx but received 10xx xxxx
				int noofUTF8 = 0; // 11xx xxxxx -> 2 bytes, 111x xxxxx -> 3 bytes , 1111 xxxxx -> 4 bytes in UTF-8
				while ( (strptr.charAt(i) & maskUTF8) != 0) {
					++noofUTF8;
					maskUTF8 >>= 1; // shift right the mask
				}
				// the second and third (and so on) UTF-8 byte looks like 10xx xxxx      
				while (0 < noofUTF8 ) {
					++i;
					if ((strptr.charAt(i) & MSB) == 0 || (strptr.charAt(i) & MSBmin1) != 0 || i >= length) { // if not like this: 10xx xxxx
						return CharCoding.UNKNOWN;
					}
					--noofUTF8;
				}
			}
			++i;
		}
		return CharCoding.UTF_8;
	}

	public String get_stringencoding(final String encoded_value) {
		int length = encoded_value.length();
		if (0 == length) {
			return new String("<unknown>");
		}
		if ( length % 2 != 0 ) {
			ErrorReporter.INTERNAL_ERROR( MessageFormat.format(
					"get_stringencoding(): Wrong string. The number of nibbles ({0}) in string " +
							"shall be divisible by 2", length ));
			return new String("<unknown>");
		}

		if      (findBom(encoded_value, utf32be)) return "UTF-32BE";
		else if (findBom(encoded_value, utf32le)) return "UTF-32LE";
		else if (findBom(encoded_value, utf16be)) return "UTF-16BE";
		else if (findBom(encoded_value, utf16le)) return "UTF-16LE";
		else if (findBom(encoded_value, utf8)) return "UTF-8";

		StringBuilder uc_str = new StringBuilder();
		String ret;
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

	private static int check_BOM(CharCoding expected_coding, int n_uc, String uc_str) {
		if (0 == n_uc) return 0;

		switch (expected_coding) {
		case UTF32:
		case UTF32BE:
		case UTF32LE:
			if (4 > n_uc) {
				ErrorReporter.INTERNAL_ERROR("decode_utf32(): The string is shorter than the expected BOM");
				return 0;
			}
			break;
		case UTF16:
		case UTF16BE:
		case UTF16LE:
			if (2 > n_uc) {
				ErrorReporter.INTERNAL_ERROR("decode_utf16(): The string is shorter than the expected BOM");
				return 0;
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
			if (0x00 == uc_str.charAt(0) && 0x00 == uc_str.charAt(1) && 0xFE == uc_str.charAt(2) && 0xFF == uc_str.charAt(3)) 
				return 4;
			badBOM = true;
			caller = "decode_utf32()";
			errmsg = "UTF-32BE";
			break;
		case UTF32LE:
			if (0xFF == uc_str.charAt(0) && 0xFE == uc_str.charAt(1) && 0x00 == uc_str.charAt(2) && 0x00 == uc_str.charAt(3))
				return 4;
			badBOM = true;
			caller = "decode_utf32()";
			errmsg = "UTF-32LE";
			break;
		case UTF16BE:
		case UTF16:
			if (0xFE == uc_str.charAt(0) && 0xFF == uc_str.charAt(1))
				return 2;
			badBOM = true;
			caller = "decode_utf16()";
			errmsg = "UTF-16BE";
			break;
		case UTF16LE:
			if (0xFF == uc_str.charAt(0) && 0xFE == uc_str.charAt(1))
				return 2;
			badBOM = true;
			caller = "decode_utf16()";
			errmsg = "UTF-16LE";
			break;
		case UTF_8:
			if (0xEF == uc_str.charAt(0) && 0xBB == uc_str.charAt(1) && 0xBF == uc_str.charAt(2))
				return 3;
			return 0;
		default:
			if (CharCoding.UTF32 == expected_coding || CharCoding.UTF16 == expected_coding) {
				final String str = CharCoding.UTF32 == expected_coding ? "UTF-32" : "UTF-16";
				ErrorReporter.INTERNAL_ERROR(MessageFormat.format(
						"Wrong {0} string. No BOM detected, however the given coding type ({1}) " +
								"expects it to define the endianness", str, str));
			}
			else {
				ErrorReporter.INTERNAL_ERROR("Wrong string. No BOM detected");
			}
		}
		if (badBOM) ErrorReporter.INTERNAL_ERROR(MessageFormat.format(
				"{0}: Wrong {1} string. The expected coding could not be verified",
				caller, errmsg));
		return 0;
	}

	private static void fill_continuing_octets(int n_continuing, List<Character> continuing_ptr,
			int n_uc, final StringBuilder uc_str, int start_pos, int uchar_pos) {
		for (int i = 0; i < n_continuing; i++) {
			if (start_pos + i < n_uc) {
				char octet = uc_str.charAt(start_pos + i);
				if ((octet & 0xC0) != 0x80) {
					ErrorReporter.INTERNAL_ERROR(MessageFormat.format(
							"decode_utf8(): Malformed: At character position {0}, octet position {1}: {2} is " +
									"not a valid continuing octet.", uchar_pos, start_pos + i, String.format("%02X", octet)));
					return;
				}
				continuing_ptr.add((char) (octet & 0x3F));
			} 
			else {
				if (start_pos + i == n_uc) {
					if (i > 0) {
						// only a part of octets is missing
						ErrorReporter.INTERNAL_ERROR(MessageFormat.format(
								"decode_utf8(): Incomplete: At character position {0}, octet position {1}: {2} out " +
										"of {3} continuing octets {4} missing from the end of the stream.",
										uchar_pos, start_pos + i, n_continuing - i, n_continuing,
										n_continuing - i > 1 ? "are" : "is"));
						return;
					}
					else {
						// all octets are missing
						ErrorReporter.INTERNAL_ERROR(MessageFormat.format(
								"decode_utf8(): Incomplete: At character position {0}, octet position {1}: {2} " +
										"continuing octet{3} missing from the end of the stream.", uchar_pos,
										start_pos, n_continuing, n_continuing > 1 ? "s are" : " is"));
						return;
					}
				}
				continuing_ptr.add((char) 0);
			}
		}
	}

	public static ustring decode_utf8(final String ostr, CharCoding expected_coding) {
		int length = ostr.length();
		if (0 == length) return new ustring();
		if (length % 2 != 0) {
			ErrorReporter.INTERNAL_ERROR(MessageFormat.format(
					"decode_utf8(): Wrong UTF-8 string. The number of nibbles ({0}) in octetstring " +
							"shall be divisible by 2", length));
			return new ustring();
		}

		StringBuilder uc_str = new StringBuilder();
		for (int i = 0; i < length / 2; ++i) {
			uc_str.append(str2uchar(ostr.charAt(2 * i), ostr.charAt(2 * i + 1)));
		}
		ustring ucstr = new ustring();
		int start = check_BOM(CharCoding.UTF_8, length /2, uc_str.toString());

		for (int i = start; i < length / 2;) {
			// perform the decoding character by character
			if (uc_str.charAt(i) <= 0x7F) {
				// character encoded on a single octet: 0xxxxxxx (7 useful bits)
				char g = 0;
				char p = 0;
				char r = 0;
				char c = uc_str.charAt(i);
				ucstr.add(g, p, r, c);
				++i;
			}
			else if (uc_str.charAt(i) <= 0xBF) {
				// continuing octet (10xxxxxx) without leading octet ==> malformed
				ErrorReporter.INTERNAL_ERROR(MessageFormat.format(
						"decode_utf8(): Malformed: At character position {0}, octet position {1}: continuing " +
								"octet {2} without leading octet.", ucstr.size(),
								i, String.format("%02X",uc_str.charAt(i))));
				return ucstr;
			}
			else if (uc_str.charAt(i) <= 0xDF) {
				// character encoded on 2 octets: 110xxxxx 10xxxxxx (11 useful bits)
				List<Character> octets = new ArrayList<Character>();
				octets.add( (char) (uc_str.charAt(i) & 0x1F) );
				fill_continuing_octets(1, octets, length / 2, uc_str, i + 1, ucstr.size());
				char g = 0;
				char p = 0;
				char r = (char) (octets.get(0) >> 2);
				char c = (char) (octets.get(0) << 6 | octets.get(1));
				if (r == 0x00 && c < 0x80) {
					ErrorReporter.INTERNAL_ERROR(MessageFormat.format(
							"decode_utf8(): Overlong: At character position {0}, octet position {1}: 2-octet " +
									"encoding for quadruple (0, 0, 0, {2}).", ucstr.size(), i, c));
					return ucstr;
				}
				ucstr.add(g, p, r, c);
				i += 2;
			} 
			else if (uc_str.charAt(i) <= 0xEF) {
				// character encoded on 3 octets: 1110xxxx 10xxxxxx 10xxxxxx
				// (16 useful bits)
				List<Character> octets = new ArrayList<Character>();
				octets.add( (char) (uc_str.charAt(i) & 0x0F) );
				fill_continuing_octets(2, octets, length / 2, uc_str, i + 1,ucstr.size());
				char g = 0;
				char p = 0;
				char r = (char) (octets.get(0) << 4 | octets.get(1) >> 2);
				char c = (char) (octets.get(1) << 6 | octets.get(2));
				if (r < 0x08) {
					ErrorReporter.INTERNAL_ERROR(MessageFormat.format(
							"decode_utf8(): Overlong: At character position {0}, octet position {1}: 3-octet " +
									"encoding for quadruple (0, 0, {2}, {3}).", ucstr.size(), i, r, c));
					return ucstr;
				}
				ucstr.add(g, p, r, c);
				i += 3;
			} 
			else if (uc_str.charAt(i) <= 0xF7) {
				// character encoded on 4 octets: 11110xxx 10xxxxxx 10xxxxxx 10xxxxxx
				// (21 useful bits)
				List<Character> octets = new ArrayList<Character>();
				octets.add( (char) (uc_str.charAt(i) & 0x07) );
				fill_continuing_octets(3, octets, length / 2, uc_str, i + 1, ucstr.size());
				char g = 0;
				char p = (char) (octets.get(0) << 2 | octets.get(1) >> 4);
				char r = (char) (octets.get(1) << 4 | octets.get(2) >> 2);
				char c = (char) (octets.get(2) << 6 | octets.get(3));
				if (p == 0x00) {
					ErrorReporter.INTERNAL_ERROR(MessageFormat.format(
							"decode_utf8(): Overlong: At character position {0}, octet position {1}: 4-octet " +
									"encoding for quadruple (0, 0, {2}, {3}).", ucstr.size(), i, r, c));
					return ucstr;
				}
				ucstr.add(g, p, r, c);
				i += 4;
			}
			else if (uc_str.charAt(i) <= 0xFB) {
				// character encoded on 5 octets: 111110xx 10xxxxxx 10xxxxxx 10xxxxxx
				// 10xxxxxx (26 useful bits)
				List<Character> octets = new ArrayList<Character>();
				octets.add( (char) (uc_str.charAt(i) & 0x03) );
				fill_continuing_octets(4, octets, length / 2, uc_str, i + 1, ucstr.size());
				char g = octets.get(0);
				char p = (char) (octets.get(1) << 2 | octets.get(2) >> 4);
				char r = (char) (octets.get(2) << 4 | octets.get(3) >> 2);
				char c = (char) (octets.get(3) << 6 | octets.get(4));
				if (g == 0x00 && p < 0x20) {
					ErrorReporter.INTERNAL_ERROR(MessageFormat.format(
							"decode_utf8(): Overlong: At character position {0}, octet position {1}: 5-octet " +
									"encoding for quadruple (0, {2}, {3}, {4}).", ucstr.size(), i, p, r, c));
					return ucstr;
				}
				ucstr.add(g, p, r, c);
				i += 5;
			}
			else if (uc_str.charAt(i) <= 0xFD) {
				// character encoded on 6 octets: 1111110x 10xxxxxx 10xxxxxx 10xxxxxx
				// 10xxxxxx 10xxxxxx (31 useful bits)
				List<Character> octets = new ArrayList<Character>();
				octets.add( (char) (uc_str.charAt(i) & 0x01) );
				fill_continuing_octets(5, octets, length / 2, uc_str, i + 1,ucstr.size());
				char g = (char) (octets.get(0) << 6 | octets.get(1));
				char p = (char) (octets.get(2) << 2 | octets.get(3) >> 4);
				char r = (char) (octets.get(3) << 4 | octets.get(4) >> 2);
				char c = (char) (octets.get(4) << 6 | octets.get(5));
				if (g < 0x04) {
					ErrorReporter.INTERNAL_ERROR(MessageFormat.format(
							"decode_utf8(): Overlong: At character position {0}, octet position {1}: 6-octet " +
									"encoding for quadruple ({2}, {3}, {4}, {}5).", ucstr.size(), i, g, p, r, c));
					return ucstr;
				}
				ucstr.add(g, p, r, c);
				i += 6;
			}
			else {
				// not used code points: FE and FF => malformed
				ErrorReporter.INTERNAL_ERROR(MessageFormat.format(
						"decode_utf8(): Malformed: At character position {0}, octet position {1}: " +
								"unused/reserved octet {2}.", ucstr.size(), i, String.format("%02X", uc_str.charAt(i))));
				return ucstr;
			}
		}
		return ucstr;
	}
}
