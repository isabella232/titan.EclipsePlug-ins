package org.eclipse.titan.designer.AST.TTCN3;

public class Code {

	/**
	 * Converts character to escaped string in the format, which is used by TtcnLogger.
	 * Originally Code::translate_character()
	 * @param str output converted (escaped) string
	 * @param c input character to convert
	 * @param in_string true if character is converted within a string
	 */
	private static void translate_character(final StringBuilder str, final char c, final boolean in_string) {
		switch (c) {
		case 0x07: //'\a' Audible bell
			str.append("\\a");
			break;
		case '\b':
			str.append("\\b");
			break;
		case '\f':
			str.append("\\f");
			break;
		case '\n':
			str.append("\\n");
			break;
		case '\r':
			str.append("\\r");
			break;
		case '\t':
			str.append("\\t");
			break;
		case 0x0b: // '\v' Vertical tab
			str.append("\\v");
			break;
		case '\\':
			str.append("\\\\");
			break;
		case '\'':
			if (in_string) {
				str.append('\'');
			} else {
				str.append("\\'");
			}
			break;
		case '"':
			if (in_string) {
				str.append("\\\"");
			} else {
				str.append('"');
			}
			break;
		case '?':
			//TODO: remove
			/*
			// to avoid recognition of trigraphs
			if (in_string) {
				str.append("\\?");
			} else {
				str.append('?');
			}
			*/
			str.append('?');
			break;
		default:
			// originally if (isascii(c) && isprint(c))
			if (c < 256 && !Character.isISOControl(c)) {
				str.append(c);
			} else {
				str.append(String.format(in_string ? "\\%03o" : "\\%o", c));
			}
			break;
		}
	}


	/**
	 * Converts character to escaped string in the format, which is used by TtcnLogger.
	 * @param c input character to convert
	 * @return str output converted (escaped) string
	 */
	public static String translate_character(char c) {
		final StringBuilder str = new StringBuilder();
		translate_character(str, c, false);
		return str.toString();
	}

	/**
	 * Converts string to escaped string in the format, which is used by TtcnLogger.
	 * Originally Code::translate_string()
	 * @param src input string to convert
	 * @return converted (escaped) string
	 */
	public static String translate_string(final String src) {
		final StringBuilder str = new StringBuilder();
		for (int i = 0; i < src.length(); i++)
			translate_character(str, src.charAt(i), true);
		return str.toString();
	}

}
