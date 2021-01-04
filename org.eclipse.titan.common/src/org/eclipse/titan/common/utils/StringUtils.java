/******************************************************************************
 * Copyright (c) 2000-2021 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.common.utils;

import java.util.ArrayList;
import java.util.List;

public final class StringUtils {

	private StringUtils() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Returns the system-dependent line separator string.
	 * It always returns the same value - the initial value of the system property line.separator.
	 * On UNIX systems, it returns "\n";
	 * on Microsoft Windows systems it returns "\r\n".
	 *
	 * (This function is available in Java SE 7 via System.lineSeparator())
	 */
	public static String lineSeparator() {
		return System.getProperty("line.separator");
	}

	public static String removePrefix(final String original, final String prefix) {
		if (original.startsWith(prefix)) {
			return original.substring(prefix.length());
		}
		return original;
	}

	public static boolean isNullOrEmpty(final String string) {
		return string == null || string.isEmpty();
	}

	/**
	 * Calculates the ordinal indicator for an integer number, like 5th, it can be 1st, 2nd, 3rd, but 11th, 12th, 13th
	 * @param n integer number
	 * @return st, nd, rd or th
	 */
	public static String getOrdinalIndicator(final int n) {
		if (11 <= n % 100 && n % 100 <= 13) {
			// exception case
			return "th";
		}
		switch (n % 10) {
		case 1:
			return "st";
		case 2:
			return "nd";
		case 3:
			return "rd";
		default:
			return "th";
		}
	}
	
	/**
	 * Splits the input string into a list of strings.
	 * 
	 * @param input
	 *                the input string to split.
	 * @param delimiter
	 *                the delimiter according which to split.
	 * @param escape
	 *                the escape character noting the delimiters not to be
	 *                used as delimiters.
	 * */
	public static final List<String> intelligentSplit(final String input, final char delimiter, final char escape) {
		final List<String> results = new ArrayList<String>();
		if (input == null || input.length() == 0) {
			return results;
		}

		final StringBuilder tempResult = new StringBuilder();
		// no over indexing is possible if the input was converted
		// correctly, as an escape must be escaping something
		char c;
		for (int i = 0; i < input.length();) {
			c = input.charAt(i);
			if (escape == c) {
				// this is either a delimiter or an escape character
				tempResult.append(c);
				i++;
			} else if (delimiter == c) {
				results.add(tempResult.toString());
				tempResult.setLength(0);
				i++;
			} else {
				tempResult.append(c);
				i++;
			}
		}
		results.add(tempResult.toString());
		return results;
	}
	
}
