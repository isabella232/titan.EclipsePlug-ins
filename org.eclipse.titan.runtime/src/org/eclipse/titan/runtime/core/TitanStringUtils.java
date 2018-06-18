/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;


/**
 * Utility functions for (bit|hex|octet)string
 * @author Arpad Lovassy
 */
public class TitanStringUtils {

	/**
	 * Creates a new byte list with new elements
	 * @param srcList source list to copy
	 * @return new list instance
	 */
	static final byte[] copyByteList(final byte srcList[]) {
		if (srcList == null) {
			return null;
		}

		final byte newList[] = new byte[srcList.length];
		System.arraycopy(srcList, 0, newList, 0, srcList.length);

		return newList;
	}

	/**
	 * Creates a new integer list with new elements
	 * @param srcList source list to copy
	 * @return new list instance
	 */
	static final int[] copyIntegerList(final int srcList[]) {
		if (srcList == null) {
			return null;
		}

		final int newList[] = new int[srcList.length];
		System.arraycopy(srcList, 0, newList, 0, srcList.length);
		return newList;
	}

	/**
	 * Creates a new char (2 bytes) list with new elements
	 * @param srcList source list to copy
	 * @return new list instance
	 */
	static char[] copyCharList(final char srcList[]) {
		if (srcList == null) {
			return null;
		}

		final char newList[] = new char[srcList.length];
		System.arraycopy(srcList, 0, newList, 0, srcList.length);

		return newList;
	}
}
