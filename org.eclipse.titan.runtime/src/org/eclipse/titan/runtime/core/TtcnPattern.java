/******************************************************************************
 * Copyright (c) 2000-2017 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

import java.text.MessageFormat;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility for handling TTCN-3 pattern
 * @author Arpad Lovassy
 */
public class TtcnPattern {

	/**
	 * Pattern for universal char quadruple
	 * "\q{i,i,i,i}" where i is non-negative integer
	 * NOTE: \q is already parsed
	 */
	private static final Pattern PATTERN_UNICHAR_QUADRUPLE = Pattern.compile( "\\{\\s*(0|[1-9][0-9]*)\\s*\\,\\s*(0|[1-9][0-9]*)\\s*\\,\\s*(0|[1-9][0-9]*)\\s*\\,\\s*(0|[1-9][0-9]*)\\s*\\}(.*)" );

	/**
	 * Pattern for universal char USI-like notation
	 * example: "\q{U0171}" or "\q{U171, U+172}"
	 * NOTE: \q is already parsed
	 */
	private static final Pattern PATTERN_UNICHAR_USI_LIST = Pattern.compile( "\\{\\s*(U\\+?[0-9A-Fa-f]+(\\s*\\,\\s*U\\+?[0-9A-Fa-f]+)*)\\s*\\}(.*)" );

	/**
	 * Pattern for single value repetition
	 * example: "#5", "#(5)"
	 * NOTE: # is already parsed
	 */
	private static final Pattern PATTERN_REPETITION_SINGLE = Pattern.compile( "\\s*(\\d+)(.*)" );

	/**
	 * Pattern for range repetition
	 * example: "#(,)", "#(5,)", "#(,5)", "#(3,5)"
	 * NOTE: # is already parsed
	 */
	private static final Pattern PATTERN_REPETITION_RANGE = Pattern.compile( "\\(\\s*(\\d*)\\s*(,\\s*(\\d*))?\\s*\\)(.*)" );

	/**
	 * converts TTCN-3 pattern to java pattern
	 * @param ttcnPattern TTCN-3 pattern
	 * @return converted java pattern
	 */
	public static Pattern convertPattern( final String ttcnPattern, final boolean nocase ) {
		Pattern javaPattern = null;
		try {
			String javaPatternString = convertPattern(ttcnPattern);
			if ( nocase ) {
				//NOTE: it is made this way because Pattern.compile( javaPatternString, Pattern.CASE_INSENSITIVE ) does NOT work
				javaPatternString = javaPatternString.toLowerCase();
			}
			javaPattern = Pattern.compile( javaPatternString );
		} catch (Exception e) {
			throw new TtcnError( MessageFormat.format( "Cannot convert pattern \"{0}\" to POSIX-equivalent.", ttcnPattern ) );
		}
		return javaPattern;
	}

	/**
	 * Match charstring with a Java regex pattern 
	 * @param s search string
	 * @param javaPattern regex pattern in Java format
	 * @param nocase true for case insensitive matching
	 * @return match result
	 */
	public static boolean match(final String s, final Pattern javaPattern, final boolean nocase ) {
		boolean result = false;
		try {
			Matcher m = javaPattern.matcher( nocase ? s.toLowerCase() : s);
			result = m.matches();
		} catch (Exception e) {
			throw new TtcnError( MessageFormat.format( "Pattern matching error: {0}", e.toString() ) );
		}
		return result;
	}

	/**
	 * converts TTCN-3 pattern to java pattern
	 * @param ttcnPattern TTCN-3 pattern
	 * @return converted java pattern
	 */
	private static String convertPattern( final String ttcnPattern ) {
		final StringBuilder javaPattern = new StringBuilder();
		convertPattern(ttcnPattern, new AtomicInteger(0), javaPattern );
		return javaPattern.toString();
	}

	/**
	 * converts TTCN-3 pattern to java pattern
	 * @param ttcnPattern TTCN-3 pattern
	 * @param pos character position in ttcnPattern
	 * @param javaPattern converted java pattern
	 */
	public static void convertPattern( final String ttcnPattern, final AtomicInteger pos, final StringBuilder javaPattern ) {
		while ( pos.get() < ttcnPattern.length() ) {
			final char c = ttcnPattern.charAt(pos.getAndIncrement());
			switch ( c ) {
			case '?':
				javaPattern.append('.');
				break;
			case '+':
				javaPattern.append('+');
				break;
			case '\\': {
				convertEscaped(ttcnPattern, pos, javaPattern, false);
				break;
			}
			case '"': {
				final char c2 = ttcnPattern.charAt(pos.getAndIncrement());
				if ( c2 == '"' ) {
					javaPattern.append('"');
				} else {
					//TODO: program error, single '"' is the end of the string, which is handled by the parser
				}
				break;
			}
			case '$':
				// $ is not escaped in TTCN-3 but escaped in Java, but not in set
				javaPattern.append("\\$");
				break;
			case '[': {
				javaPattern.append('[');
				convertSet( ttcnPattern, pos, javaPattern );
				break;
			}
			case '#': {
				convertRepetition( ttcnPattern, pos, javaPattern );
				break;
			}
			//TODO: special cases: ()|
			default:
				javaPattern.append(c);
				break;
			}
		}
	}

	/**
	 * converts TTCN-3 pattern escaped character to java pattern character.
	 * NOTE: \ is already parsed
	 * @param ttcnPattern TTCN-3 pattern
	 * @param pos character position in ttcnPattern
	 * @param javaPattern converted java pattern
	 * @param isSet true inside a set
	 */
	private static void convertEscaped(final String ttcnPattern, final AtomicInteger pos, final StringBuilder javaPattern, final boolean isSet) {
		final char c = ttcnPattern.charAt(pos.getAndIncrement());
		switch ( c ) {
		case '\\':
			javaPattern.append("\\\\");
			break;
		case 'd':
			javaPattern.append(isSet ? "0-9" : "[0-9]");
			break;
		case 'w':
			javaPattern.append(isSet ? "0-9A-Za-z" : "[0-9A-Za-z]");
			break;
		case 't':
			javaPattern.append("\\t");
			break;
		case 'n':
			javaPattern.append(isSet ? "\\n-\\r" : "[\\n-\\r]");
			break;
		case 'r':
			javaPattern.append("\\r");
			break;
		case 's':
			javaPattern.append(isSet ? "\\t-\\r " : "[\\t-\\r ]");
			break;
		case 'b':
			//NOTE: it does nothing in pattern_p.y
			javaPattern.append("\\b");
			break;
		case '"':
			javaPattern.append("\\\"");
			break;
		case '[':
			javaPattern.append("\\[");
			break;
		case ']':
			javaPattern.append("\\]");
			break;
		case '-':
			javaPattern.append("\\-");
			break;
		case '^':
			javaPattern.append("\\^");
			break;
		//TODO: #()|
		case 'q':
			convertUnicharList(ttcnPattern, pos, javaPattern);
			break;
		default:
			//TODO: error, not supported
			break;
		}
	}

	/**
	 * converts TTCN-3 pattern set to java pattern set (from '[' to ']').
	 * NOTE: '[' is already parsed
	 * @param ttcnPattern TTCN-3 pattern
	 * @param pos character position in ttcnPattern
	 * @param javaPattern converted java pattern
	 */
	private static void convertSet(final String ttcnPattern, final AtomicInteger pos, final StringBuilder javaPattern) {
		char c = ttcnPattern.charAt(pos.getAndIncrement());
		if ( c == '^' ) {
			javaPattern.append('^');
			c = ttcnPattern.charAt(pos.getAndIncrement());
		}
		while ( c != ']' ) {
			switch ( c ) {
			case '\\':
				convertEscaped(ttcnPattern, pos, javaPattern, true);
				break;
			case '^':
				//TODO: error, it can be only the first character
				break;
			case '[':
				// [ within a set defines a literal [
				javaPattern.append("\\[");
				break;
			case '-':
				//special case, needs to be checked
				//it defines a set range, so it can be used after a valid set item and cannot be used again until range is closed
				javaPattern.append('-');
				break;
			default:
				javaPattern.append(c);
				break;
			}
			c = ttcnPattern.charAt(pos.getAndIncrement());
		}
		javaPattern.append(']');
	}

	/**
	 * converts TTCN-3 pattern repetition to java pattern
	 * NOTE: # is already parsed
	 * @param ttcnPattern TTCN-3 pattern
	 * @param pos character position in ttcnPattern
	 * @param javaPattern converted java pattern
	 */
	private static void convertRepetition(final String ttcnPattern, final AtomicInteger pos, final StringBuilder javaPattern) {
		//TODO: add try-catch, and return error
		final String input = ttcnPattern.substring(pos.get());
		Matcher m = PATTERN_REPETITION_SINGLE.matcher(input);
		if ( m.matches() ) {
			final int offset = m.toMatchResult().start(2);
			pos.getAndAdd(offset);
			final String valueStr = m.group(1);
			final int value = Integer.parseInt(valueStr);
			javaPattern.append( "{" + value + "}" );
		} else {
			m = PATTERN_REPETITION_RANGE.matcher(input);
			if ( m.matches() ) {
				final int offset = m.toMatchResult().start(4);
				pos.getAndAdd(offset);
				final String minStr = m.group(1);
				final String commaWithMaxStr = m.group(2);
				//maxStr is null if there is no comma
				final String maxStr = m.group(3);
				javaPattern.append( "{" );
				javaPattern.append( minStr.isEmpty() ? '0' : minStr );
				if ( commaWithMaxStr != null && !commaWithMaxStr.isEmpty() ) {
					javaPattern.append( "," + maxStr );
				}
				javaPattern.append( "}" );
			} else {
				//TODO: error
			}
		}
	}

	/**
	 * converts TTCN-3 pattern escaped character within a set to java pattern character.
	 * NOTE: \ is already parsed
	 * @param ttcnPattern TTCN-3 pattern
	 * @param pos character position in ttcnPattern
	 * @param javaPattern converted java pattern
	 */
	private static void convertUnicharList(final String ttcnPattern, final AtomicInteger pos, final StringBuilder javaPattern) {
		//TODO: add try-catch, and return error
		final String input = ttcnPattern.substring(pos.get());
		Matcher m = PATTERN_UNICHAR_USI_LIST.matcher(input);
		if ( m.matches() ) {
			final int offset = m.toMatchResult().start(3);
			pos.getAndAdd(offset);
			final String ucharlist = m.group(1);
			final String[] uchars = ucharlist.split("\\s*,\\s*");
			for (String uchar : uchars) {
				final String hexstr = uchar.substring(1);
				final int hex = Integer.parseInt(hexstr, 16);
			    javaPattern.append((char) hex);
			}
		} else {
			m = PATTERN_UNICHAR_QUADRUPLE.matcher(input);
			if ( m.matches() ) {
				final int offset = m.toMatchResult().start(5);
				pos.getAndAdd(offset);
				final String groupStr = m.group(1);
				final String planeStr = m.group(2);
				final String rowStr = m.group(3);
				final String cellStr = m.group(4);
				final int group = Integer.parseInt(groupStr);
				final int plane = Integer.parseInt(planeStr);
				final int row = Integer.parseInt(rowStr);
				final int cell = Integer.parseInt(cellStr);
				//NOTE: java char supports only 2 bytes
				javaPattern.append( (char) ( 256 * row + cell ) );
			} else {
				//TODO: error
			}
		}
	}
}
