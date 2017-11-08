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
}
