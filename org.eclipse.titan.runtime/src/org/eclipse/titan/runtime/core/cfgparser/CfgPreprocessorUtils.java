/******************************************************************************
 * Copyright (c) 2000-2019 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *   Balasko, Jeno
 *   Beres, Szabolcs
 *
 ******************************************************************************/
package org.eclipse.titan.runtime.core.cfgparser;

/**
 * Originally config_preproc.cc
 * Utility functions:
 *   - get macro id from typed macro
 *   - type checking for typed macro
 * @author Arpad Lovassy
 */
public class CfgPreprocessorUtils {

	/**
	 * @param str typed macro string (example: ${abc, integer})
	 * @return macro id from the typed macro (example: abc)
	 *         or null if typed macro is invalid
	 */
	static String get_macro_id_from_ref(final String str) {
		if (str != null && str.charAt(0) == '$' && str.charAt(1) == '{') {
			StringBuilder sb = new StringBuilder();
			int i = 2;
			// skip over the whitespaces after the brace
			while (str.charAt(i) == ' ' || str.charAt(i) == '\t') {
				i++;
			}
			if ((str.charAt(i) >= 'A' && str.charAt(i) <= 'Z') ||
				(str.charAt(i) >= 'a' && str.charAt(i) <= 'z')) {
				// the first character of the id shall be a letter
				do {
					sb.append(str.charAt(i));
					i++;
				} while ((str.charAt(i) >= 'A' && str.charAt(i) <= 'Z') ||
						 (str.charAt(i) >= 'a' && str.charAt(i) <= 'z') ||
						 (str.charAt(i) >= '0' && str.charAt(i) <= '9') ||
						 str.charAt(i) == '_');
				if (str.charAt(i) != ' ' && str.charAt(i) != '\t' && str.charAt(i) != ',' && str.charAt(i) != '}') {
					// the next character after the id is not a whitespace or , or }
					return null;
				}
			}
			return sb.toString();
		}
		return null;
	}

	/* state: expected characters
	 * INITIAL: +, -, first digit
	 * FIRST: first digit
	 * MORE, ZERO: more digit(s)
	 * ERR: error was found, stop
	 */
	private enum int_state { INITIAL, FIRST, ZERO, MORE, ERR }

	static boolean string_is_int(final String str) {
		if ( str == null) {
			return false;
		}

		final int len = str.length();
		if (len == 0 ) {
			return false;
		}

		int_state state = int_state.INITIAL;
		for (int i = 0; i < len; i++) {
			final char c = str.charAt(i);
			switch (state) {
			case INITIAL:
				if (c == '+' || c == '-') {
					state = int_state.FIRST;
				} else if (c == '0') {
					state = int_state.ZERO;
				} else if (c >= '1' && c <= '9') {
					state = int_state.MORE;
				} else {
					state = int_state.ERR;
				}
				break;
			case FIRST:
				if (c == '0') {
					state = int_state.ZERO;
				} else if (c >= '1' && c <= '9') {
					state = int_state.MORE;
				} else {
					state = int_state.ERR;
				}
				break;
			case ZERO:
				if (c >= '0' && c <= '9') {
					state = int_state.MORE;
				} else {
					state = int_state.ERR;
				}
				break;
			case MORE:
				if (c >= '0' && c <= '9') {
					// do nothing
				} else {
					state = int_state.ERR;
				}
			default:
				break;
			}
			if (state == int_state.ERR) {
				return false;
			}
		}
		return (state == int_state.ZERO || state == int_state.MORE);
	}

	/* state: expected characters
	 * INITIAL: +, -, first digit of integer part in mantissa
	 * FIRST_M: first digit of integer part in mantissa
	 * ZERO_M, MORE_M: more digits of mantissa, decimal dot, E
	 * FIRST_F: first digit of fraction
	 * MORE_F: more digits of fraction, E
	 * INITIAL_E: +, -, first digit of exponent
	 * FIRST_E: first digit of exponent
	 * ZERO_E, MORE_E: more digits of exponent
	 * ERR: error was found, stop
	 */
	private enum float_state { INITIAL, FIRST_M, ZERO_M, MORE_M, FIRST_F, MORE_F, INITIAL_E,
		FIRST_E, ZERO_E, MORE_E, ERR }

	static boolean string_is_float(final String str) {
		if ( str == null) {
			return false;
		}

		final int len = str.length();
		if (len == 0 ) {
			return false;
		}

		float_state state = float_state.INITIAL;
		for (int i = 0; i < len; i++) {
			final char c = str.charAt(i);
			switch (state) {
			case INITIAL:
				if (c == '+' || c == '-') {
					state = float_state.FIRST_M;
				} else if (c == '0') {
					state = float_state.ZERO_M;
				} else if (c >= '1' && c <= '9') {
					state = float_state.MORE_M;
				} else {
					state = float_state.ERR;
				}
				break;
			case FIRST_M:
				if (c == '0') {
					state = float_state.ZERO_M;
				} else if (c >= '1' && c <= '9') {
					state = float_state.MORE_M;
				} else {
					state = float_state.ERR;
				}
				break;
			case ZERO_M:
				if (c == '.') {
					state = float_state.FIRST_F;
				} else if (c == 'E' || c == 'e') {
					state = float_state.INITIAL_E;
				} else if (c >= '0' && c <= '9') {
					state = float_state.MORE_M;
				} else {
					state = float_state.ERR;
				}
				break;
			case MORE_M:
				if (c == '.') {
					state = float_state.FIRST_F;
				} else if (c == 'E' || c == 'e') {
					state = float_state.INITIAL_E;
				} else if (c >= '0' && c <= '9') {
					// do nothing
				} else {
					state = float_state.ERR;
				}
				break;
			case FIRST_F:
				if (c >= '0' && c <= '9') {
					state = float_state.MORE_F;
				} else {
					state = float_state.ERR;
				}
				break;
			case MORE_F:
				if (c == 'E' || c == 'e') {
					state = float_state.INITIAL_E;
				} else if (c >= '0' && c <= '9') {
					// do nothing
				} else {
					state = float_state.ERR;
				}
				break;
			case INITIAL_E:
				if (c == '+' || c == '-') {
					state = float_state.FIRST_E;
				} else if (c == '0') {
					state = float_state.ZERO_E;
				} else if (c >= '1' && c <= '9') {
					state = float_state.MORE_E;
				} else {
					state = float_state.ERR;
				}
				break;
			case FIRST_E:
				if (c == '0') {
					state = float_state.ZERO_E;
				} else if (c >= '1' && c <= '9') {
					state = float_state.MORE_E;
				} else {
					state = float_state.ERR;
				}
				break;
			case ZERO_E:
				if (c >= '0' && c <= '9') {
					state = float_state.MORE_E;
				} else state = float_state.ERR;
				break;
			case MORE_E:
				if (c >= '0' && c <= '9') {
					// do nothing
				} else {
					state = float_state.ERR;
				}
			default:
				break;
			}
			if (state == float_state.ERR) {
				return false;
			}
		}
		return (state == float_state.MORE_F ||
				state == float_state.ZERO_E ||
				state == float_state.MORE_E	||
				state == float_state.ZERO_M ||
				state == float_state.MORE_M);
	}

	static boolean string_is_id(final String str) {
		if ( str == null) {
			return false;
		}

		final int len = str.length();
		if (len == 0 ) {
			return false;
		}

		final char first_char = str.charAt(0);
		if ((first_char < 'a' || first_char > 'z') && (first_char < 'A' || first_char > 'Z')) {
			return false;
		}
		boolean has_hyphen = false;
		boolean has_underscore = false;
		for (int i = 1; i < len; i++) {
			final char c = str.charAt(i);
			if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9')) {
				// do nothing
			} else if (c == '_') {
				if (has_hyphen) {
					return false;
				} else {
					has_underscore = true;
				}
			} else if (c == '-') {
				if (has_underscore || str.charAt(i - 1) == '-' || i == len - 1 ||
					first_char < 'a' || first_char > 'z') {
					return false;
				} else {
					has_hyphen = true;
				}
			} else {
				return false;
			}
		}
		return true;
	}

	static boolean string_is_bstr(final String str) {
		if (str == null) {
			return false;
		}

		final int len = str.length();
		if (len == 0) {
			return false;
		}

		for (int i = 0; i < len; i++) {
			final char c = str.charAt(i);
			if (c != '0' && c != '1') {
				return false;
			}
		}
		return true;
	}

	static boolean string_is_hstr(final String str) {
		if ( str == null) {
			return false;
		}

		final int len = str.length();
		if (len == 0 ) {
			return false;
		}
		for (int i = 0; i < len; i++) {
			final char c = str.charAt(i);
			if ( (c < '0' || c > '9') && (c < 'A' || c > 'F') && (c < 'a' || c > 'f') ) {
				return false;
			}
		}
		return true;
	}

	static boolean string_is_ostr(final String str) {
		if ( str == null) {
			return false;
		}

		final int len = str.length();
		if (len == 0 ) {
			return false;
		}

		if (len % 2 != 0) {
			return false;
		} else {
			return string_is_hstr(str);
		}
	}

	private enum string_state { INITIAL, ALPHANUM, DOT, DASH, COLON, PERCENT }

	static boolean string_is_hostname(final String str) {
		if ( str == null) {
			return false;
		}

		final int len = str.length();
		if (len == 0 ) {
			return false;
		}

		string_state state = string_state.INITIAL;
		for (int i = 0; i < len; i++) {
			final char c = str.charAt(i);
			if ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9')) {
				state = string_state.ALPHANUM;
			} else if (c == '.') {
				if (state == string_state.ALPHANUM) {
					state = string_state.DOT;
				} else {
					return false;
				}
			} else if (c == ':') {
				if (state == string_state.INITIAL || state == string_state.ALPHANUM || state == string_state.COLON) {
					state = string_state.COLON;
				}
				else return false;
			} else if (c == '%') {
				if (state == string_state.ALPHANUM) {
					state = string_state.PERCENT;
				} else {
					return false;
				}
			} else if (c == '-' || c == '_') {
				if (state == string_state.INITIAL || state == string_state.DOT ||
					state == string_state.COLON || state == string_state.PERCENT) {
					return false;
				} else {
					state = string_state.DASH;
				}
			} else {
				return false;
			}
		}
		return (state == string_state.ALPHANUM || state == string_state.DOT);
	}

	public static boolean string_is_bool(String typedMacroValue) {
		if ("true".equals(typedMacroValue)) {
			return true;
		}
		if ("false".equals(typedMacroValue)) {
			return true;
		}
		return false;
	}
}
