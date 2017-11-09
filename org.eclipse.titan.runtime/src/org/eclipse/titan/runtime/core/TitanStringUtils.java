/******************************************************************************
 * Copyright (c) 2000-2017 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

import java.util.ArrayList;
import java.util.List;

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
	static final List<Byte> copyByteList( final List<Byte> srcList ) {
		if ( srcList == null ) {
			return null;
		}

		final List<Byte> newList = new ArrayList<Byte>( srcList.size() );
		for (Byte uc : srcList) {
			newList.add( Byte.valueOf( uc ) );
		}
		return newList;
	}

	/**
	 * Creates a new char (2 bytes) list with new elements
	 * @param srcList source list to copy
	 * @return new list instance
	 */
	static List<Character> copyCharList(final List<Character> srcList) {
		if ( srcList == null ) {
			return null;
		}

		final List<Character> newList = new ArrayList<Character>( srcList.size() );
		for (Character uc : srcList) {
			newList.add( new Character( uc ) );
		}

		return newList;
	}
}
