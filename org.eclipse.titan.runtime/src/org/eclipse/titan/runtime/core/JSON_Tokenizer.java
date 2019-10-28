/******************************************************************************
 * Copyright (c) 2000-2019 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/** 
 * A class for building and processing JSON documents. Stores the document in a buffer.
 * Can build JSON documents by inserting tokens into an empty buffer.
 * Can extract tokens from an existing JSON document.
 * 
 * @author Arpad Lovassy
 */
public class JSON_Tokenizer {

	/** JSON token types */
	public enum json_token_t {
		JSON_TOKEN_ERROR,         // not actually a token, used when get_next_token() fails
		JSON_TOKEN_NONE,          // not actually a token, used for initializing
		JSON_TOKEN_OBJECT_START,  // "{"
		JSON_TOKEN_OBJECT_END,    // "}"
		JSON_TOKEN_ARRAY_START,   // "["
		JSON_TOKEN_ARRAY_END,     // "]"
		JSON_TOKEN_NAME,          // field name (key) in a JSON object, followed by ":"
		JSON_TOKEN_NUMBER,        // JSON number value
		JSON_TOKEN_STRING,        // JSON string value
		JSON_TOKEN_LITERAL_TRUE,  // "true" value
		JSON_TOKEN_LITERAL_FALSE, // "false" value
		JSON_TOKEN_LITERAL_NULL   // "null" value
	};

	/** The buffer that stores the JSON document 
	 * This is a buffer with exponential allocation (expstring), only uses expstring
	 * memory operations from memory.h (ex.: mputstr, mputprintf) */
	private StringBuilder buf_ptr;

	/** Number of bytes currently in the buffer */
	private int buf_len;

	/** Current position in the buffer */
	private int buf_pos;

	/** Current depth in the JSON document (only used if pretty printing is set */
	private int depth;

	/** Stores the previous JSON token inserted by put_next_token() */
	private json_token_t previous_token;

	/** Activates or deactivates pretty printing
	 * If active, put_next_token() and put_separator() will add extra newlines 
	 * and indenting to the JSON code to make it more readable for you humans,
	 * otherwise it will be compact (no white spaces). */
	private boolean pretty;

	/** Initializes the properties of the tokenizer. 
	 * The buffer is initialized with the parameter data (unless it's empty). */
	private void init(final String p_buf, final int p_buf_len) {
		if (p_buf != null && p_buf_len != 0) {
			//TODO: do we need p_buf_len?
			buf_ptr = new StringBuilder(p_buf.substring(0, p_buf_len));
		} else {
			buf_ptr = null;
		}
		buf_len = p_buf_len;
		buf_pos = 0;
		depth = 0;
		previous_token = json_token_t.JSON_TOKEN_NONE;
	}

	/** Inserts a character to the end of the buffer */
	private void put_c(final char c) {
		buf_ptr.append(c);
		++buf_len;
	}

	/** Inserts a null-terminated string to the end of the buffer */
	private void put_s(final String s) {
		buf_ptr.append(s);
		buf_len += s.length();
	}

	/** Indents a new line in JSON code depending on the current depth.
	 * If the maximum depth is reached, the code is not indented further.
	 * Used only if pretty printing is set. */
	private void put_depth() {
		put_s(TABS + ((depth > MAX_TABS) ? 0 : MAX_TABS - depth));
	}

	/** Skips white spaces until a non-white-space character is found.
	 * Returns false if the end of the buffer is reached before a non-white-space
	 * character is found, otherwise returns true. */
	private boolean skip_white_spaces() {
		while(buf_pos < buf_len) {
			switch(buf_ptr.charAt(buf_pos)) {
			case ' ':
			case '\r':
			case '\n':
			case '\t':
			case '\f':
				++buf_pos;
				break;
			default:
				return true;
			}
		}
		return false;
	}

	/** Attempts to find a JSON string at the current buffer position. 
	 * Returns true if a valid string is found before the end of the buffer
	 * is reached, otherwise returns false. */
	private boolean check_for_string() {
		if ('\"' == buf_ptr.charAt(buf_pos)) {
			++buf_pos;
		} else {
			return false;
		}
		while (buf_pos < buf_len) {
			if ('\"' == buf_ptr.charAt(buf_pos)) {
				return true;
			}
			else if ('\\' == buf_ptr.charAt(buf_pos)) {
				// skip escaped character (so escaped quotes (\") are not mistaken for the ending quotes)
				++buf_pos;
			}
			++buf_pos;
		}
		return false;
	}

	/** Checks if the current character in the buffer is a valid JSON separator.
	 * Separators are: commas (,), colons (:) and curly and square brackets ({}[]).
	 * This function also steps over the separator if it's a comma.
	 * Returns true if a separator is found, otherwise returns false. */
	private boolean check_for_separator() {
		if (buf_pos < buf_len) {
			switch(buf_ptr.charAt(buf_pos)) {
			case ',':
				++buf_pos;
				// no break
			case ':':
			case '{':
			case '}':
			case '[':
			case ']':
				return true;
			default:
				return false;
			}
		}
		return true;
	}

	/** Attempts to find a specific JSON literal at the current buffer position.
	 * Returns true if the literal is found, otherwise returns false.
	 * @param p_literal [in] Literal value to find */
	private boolean check_for_literal(final String p_literal) {
		int len = p_literal.length();
		if (buf_len - buf_pos >= len &&
				buf_ptr.substring(buf_pos).equals(p_literal)) {
			int start_pos = buf_pos;
			buf_pos += len;
			if (!skip_white_spaces() || check_for_separator()) {
				return true;
			} else {
				// must be followed by a separator (or only white spaces until the buffer ends) -> undo buffer action
				buf_pos = start_pos;
			}
		}
		return false;
	}

	/** Adds a separating comma (,) if the previous token is a value, or an object or
	 * array end mark. */
	private void put_separator() {
		if (json_token_t.JSON_TOKEN_NAME != previous_token && json_token_t.JSON_TOKEN_NONE != previous_token &&
				json_token_t.JSON_TOKEN_ARRAY_START != previous_token && json_token_t.JSON_TOKEN_OBJECT_START != previous_token) {
			put_c(',');
			if (pretty) {
				put_c('\n');
				put_depth();
			}
		}
	}

	/** Constructs a tokenizer with an empty buffer.
	 * Use put_next_token() to build a JSON document and get_buffer()/get_buffer_length() to retrieve it */
	public JSON_Tokenizer() {
		this(false);
	}

	/** Constructs a tokenizer with an empty buffer.
	 * Use put_next_token() to build a JSON document and get_buffer()/get_buffer_length() to retrieve it */
	public JSON_Tokenizer(boolean p_pretty) {
		pretty = p_pretty;
		init(null, 0);
	}

	/** Constructs a tokenizer with the buffer parameter.
	 * Use get_next_token() to read JSON tokens and get_pos()/set_pos() to move around in the buffer */
	public JSON_Tokenizer(final String p_buf, final int p_buf_len) {
		pretty = false;
		init(p_buf, p_buf_len);
	}

	/** Reinitializes the tokenizer with a new buffer. */
	public  void set_buffer(final String p_buf, final int p_buf_len) {
		init(p_buf, p_buf_len);
	}

	/** Retrieves the buffer containing the JSON document. */
	public StringBuilder get_buffer() {
		return buf_ptr;
	}

	/** Retrieves the length of the buffer containing the JSON document. */
	public int get_buffer_length() {
		return buf_len;
	}

	/** Extracts a JSON token from the current buffer position.
	 * @param p_token [out] Extracted token type, or JSON_TOKEN_ERROR if no token
	 * could be extracted, or JSON_TOKEN_NONE if the buffer end is reached
	 * @param p_token_str [out] A pointer to the token data (if any):
	 * the name of a JSON object field (without quotes), or the string representation
	 * of a JSON number, or a JSON string (with quotes and double-escaped).
	 * @param p_str_len [out] The character length of the token data (if there is data)
	 * @return The number of characters extracted 
	 * @note The token data is not copied, *p_token_str will point to the start of the 
	 * data in the tokenizer's buffer. */
	public int get_next_token(final AtomicReference<json_token_t> p_token, final StringBuilder p_token_str, AtomicInteger p_str_len)	{
		int start_pos = buf_pos;
		p_token.set(json_token_t.JSON_TOKEN_NONE);
		if (null != p_token_str && null != p_str_len) {
			p_token_str.setLength(0);
			p_str_len.set(0);
		}

		if (skip_white_spaces()) {
			char c = buf_ptr.charAt(buf_pos);
			switch (c) {
			case '{':
			case '[':
				p_token.set( ('{' == c) ? json_token_t.JSON_TOKEN_OBJECT_START : json_token_t.JSON_TOKEN_ARRAY_START );
				++buf_pos;
				break;
			case '}':
			case ']':
				++buf_pos;
				if (skip_white_spaces() && !check_for_separator()) {
					// must be followed by a separator (or only white spaces until the buffer ends)
					p_token.set(json_token_t.JSON_TOKEN_ERROR);
				} else {
					p_token.set( ('}' == c) ? json_token_t.JSON_TOKEN_OBJECT_END : json_token_t.JSON_TOKEN_ARRAY_END );
				}
				break;
			case '\"': {
				// string value or field name
				int string_start_pos = buf_pos;
				if(!check_for_string()) {
					// invalid string value
					p_token.set(json_token_t.JSON_TOKEN_ERROR);
					break;
				}
				int string_end_pos = ++buf_pos; // step over the string's ending quotes
				if (skip_white_spaces() && ':' == buf_ptr.charAt(buf_pos)) {
					// name token - don't include the starting and ending quotes
					p_token.set(json_token_t.JSON_TOKEN_NAME);
					if (null != p_token_str && null != p_str_len) {
						p_token_str.setLength(0);
						p_token_str.append( buf_ptr.substring( string_start_pos + 1 ) );
						p_str_len.set(string_end_pos - string_start_pos - 2);
					}
					++buf_pos;
				} else if (check_for_separator()) {
					// value token - include the starting and ending quotes
					p_token.set(json_token_t.JSON_TOKEN_STRING);
					if (null != p_token_str && null != p_str_len) {
						p_token_str.setLength(0);
						p_token_str.append( buf_ptr.substring( string_start_pos ) );
						p_str_len.set(string_end_pos - string_start_pos);
					}
				} else {
					// value token, but there is no separator after it -> error
					p_token.set(json_token_t.JSON_TOKEN_ERROR);
					break;
				}
				break;
			} // case: string value or field name
			default:
				if (('0' <= buf_ptr.charAt(buf_pos) && '9' >= buf_ptr.charAt(buf_pos)) ||
						'-' == buf_ptr.charAt(buf_pos)) {
					// number value
					int number_start_pos = buf_pos;
					if (!check_for_number()) {
						// invalid number
						p_token.set(json_token_t.JSON_TOKEN_ERROR);
						break;
					}
					int number_length = buf_pos - number_start_pos;
					if (skip_white_spaces() && !check_for_separator()) {
						// must be followed by a separator (or only white spaces until the buffer ends)
						p_token.set(json_token_t.JSON_TOKEN_ERROR);
						break;
					}
					p_token.set(json_token_t.JSON_TOKEN_NUMBER);
					if (null != p_token_str && null != p_str_len) {
						p_token_str.setLength(0);
						p_token_str.append( buf_ptr.substring( number_start_pos ) );
						p_str_len.set(number_length);
					}
					break;
				} // if (number value)
				else if (check_for_literal("true")) {
					p_token.set(json_token_t.JSON_TOKEN_LITERAL_TRUE);
					break;
				} 
				else if (check_for_literal("false")) {
					p_token.set(json_token_t.JSON_TOKEN_LITERAL_FALSE);
					break;
				} 
				else if (check_for_literal("null")) {
					p_token.set(json_token_t.JSON_TOKEN_LITERAL_NULL);
					break;
				}
				else {
					p_token.set(json_token_t.JSON_TOKEN_ERROR);
					break;
				}
			} // switch (current char)
		} // if (skip_white_spaces())

		return buf_pos - start_pos;
	}

	/** Gets the current read position in the buffer.
	 * This is where get_next_token() will read from next. */
	public int get_buf_pos() {
		return buf_pos;
	}

	/** Sets the current read position in the buffer.
	 * This is where get_next_buffer() will read from next. */
	public void set_buf_pos(final int p_buf_pos) {
		buf_pos = p_buf_pos;
	}

	public int put_next_token(json_token_t p_token) {
		return put_next_token(p_token, null);
	}
	
	/** Adds the specified JSON token to end of the buffer. 
	 * @param p_token [in] Token type
	 * @param p_token_str [in] The name of a JSON object field (without quotes), or
	 * the string representation of a JSON number, or a JSON string (with quotes 
	 * and double-escaped). For all the other tokens this parameter will be ignored.
	 * @return The number of characters added to the JSON document */
	public int put_next_token(json_token_t p_token, final String p_token_str) {
		int start_len = buf_len;
		switch(p_token) {
		case JSON_TOKEN_OBJECT_START: 
		case JSON_TOKEN_ARRAY_START: {
			put_separator();
			put_c( (json_token_t.JSON_TOKEN_OBJECT_START == p_token) ? '{' : '[' );
			if (pretty) {
				put_c('\n');
				++depth;
				put_depth();
			}
			break;
		}
		case JSON_TOKEN_OBJECT_END: 
		case JSON_TOKEN_ARRAY_END: {
			if (pretty) {
				if (json_token_t.JSON_TOKEN_OBJECT_START != previous_token && json_token_t.JSON_TOKEN_ARRAY_START != previous_token) {
					put_c('\n');
					--depth;
					put_depth();
				} else if (MAX_TABS >= depth) {
					// empty object or array -> remove the extra tab added at the start token
					--depth;
					--buf_len;
					buf_ptr.setCharAt(buf_len, (char) 0);
				}    
			}
			put_c( (json_token_t.JSON_TOKEN_OBJECT_END == p_token) ? '}' : ']' );
			break;
		}
		case JSON_TOKEN_NUMBER:
		case JSON_TOKEN_STRING:
			put_separator();
			put_s(p_token_str);
			break;
		case JSON_TOKEN_LITERAL_TRUE:
			put_separator();
			put_s("true");
			break;
		case JSON_TOKEN_LITERAL_FALSE:
			put_separator();
			put_s("false");
			break;
		case JSON_TOKEN_LITERAL_NULL:
			put_separator();
			put_s("null");
			break;
		case JSON_TOKEN_NAME:
			put_separator();
			put_c('\"');
			put_s(p_token_str);
			if (pretty) {
				put_s("\" : ");
			} else {
				put_s("\":");
			}
			break;
		default:
			return 0;
		}

		previous_token = p_token;
		return buf_len - start_len;
	}

	/** Adds raw data to the end of the buffer.
	 * @param p_data [in] Pointer to the beginning of the data
	 * @param p_len [in] Length of the data in bytes */
	public void put_raw_data(final String p_data, int p_len) {
		//TODO: do we need p_len?
		buf_ptr.append(p_data.substring(0, p_len));
		buf_len += p_len;
	}

	public boolean check_for_number() {
		return check_for_number(null);
	}
	
	/** Attempts to find a JSON number at the current buffer position.
	 * For number format see http://json.org/.
	 * Returns true if a valid number is found before the end of the buffer
	 * is reached, otherwise returns false.
	 * is_float variable is true if the number is a float. The result
	 * should be used only if this function returns true. */
	public boolean check_for_number(AtomicBoolean is_float)	{
		boolean first_digit = false; // first non-zero digit reached
		boolean zero = false; // first zero digit reached
		boolean decimal_point = false; // decimal point (.) reached
		boolean exponent_mark = false; // exponential mark (e or E) reached
		boolean exponent_sign = false; // sign of the exponential (- or +) reached

		if ('-' == buf_ptr.charAt(buf_pos)) {
			++buf_pos;
		}

		while (buf_pos < buf_len) {
			switch(buf_ptr.charAt(buf_pos)) {
			case '.':
				if (decimal_point || exponent_mark || (!first_digit && !zero)) {
					return false;
				}
				decimal_point = true;
				first_digit = false;
				zero = false;
				break;
			case 'e':
			case 'E':
				if (exponent_mark || (!first_digit && !zero)) {
					return false;
				}
				exponent_mark = true;
				first_digit = false;
				zero = false;
				break;
			case '0':
				if (!first_digit && (exponent_mark || (!decimal_point && zero))) {
					return false;
				}
				zero = true;
				break;
			case '1':
			case '2':
			case '3':
			case '4':
			case '5':
			case '6':
			case '7':
			case '8':
			case '9':
				if (!first_digit && zero && (!decimal_point || exponent_mark)) {
					return false;
				}
				first_digit = true;
				break;
			case '-':
			case '+':
				if (exponent_sign || !exponent_mark || zero || first_digit) {
					return false;
				}
				exponent_sign = true;
				break;
			default:
				if (is_float != null) {
					is_float.set( decimal_point || exponent_mark );
				}
				return first_digit || zero;
			}

			++buf_pos; 
		}
		if (is_float != null) {
			is_float.set( decimal_point || exponent_mark );
		}
		return first_digit || zero;
	}

	private static final String TABS =
			"\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t" +
			"\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t" +
			"\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t" +
			"\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t";

	private static final int MAX_TABS = TABS.length(); // 64

	private static StringBuilder convert_to_json_string(final String str) {
		StringBuilder ret_val = new StringBuilder("\"");
		// control characters (like \n) cannot be placed in a JSON string, replace
		// them with JSON metacharacters
		// double quotes and backslashes need to be escaped, too
		int str_len = str.length();
		for (int i = 0; i < str_len; ++i) {
			switch (str.charAt(i)) {
			case '\n':
				ret_val.append("\\n");
				break;
			case '\r':
				ret_val.append("\\r");
				break;
			case '\t':
				ret_val.append("\\t");
				break;
			case '\f':
				ret_val.append("\\f");
				break;
			case '\b':
				ret_val.append("\\b");
				break;
			case '\"':
				ret_val.append("\\\"");
				break;
			case '\\':
				ret_val.append("\\\\");
				break;
			default:
				if (str.charAt(i) < 32 && str.charAt(i) > 0) {
					// use the JSON \ uHHHH notation for other control characters
					// (this is just for esthetic reasons, these wouldn't break the JSON 
					// string format)
					ret_val.append("\\u00");
					ret_val.append(str.charAt(i) / 16);
					ret_val.append((char)((str.charAt(i) % 16 < 10) ? (str.charAt(i) % 16 + '0') : (str.charAt(i) % 16 - 10 + 'A')));
				}
				else {
					ret_val.append(str.charAt(i));
				}
				break;
			}
		}
		return ret_val.append("\"");
	}
}
