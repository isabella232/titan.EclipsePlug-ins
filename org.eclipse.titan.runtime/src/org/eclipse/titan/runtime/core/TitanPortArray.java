/******************************************************************************
 * Copyright (c) 2000-2017 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/**
 *	Represent an array of ports.	
 *
 *
 *	@author Gergo Ujhelyi 
 **/

public class TitanPortArray<T extends TitanPort> extends TitanPort {

	private List<T> array_elements;
	private List<String> names;

	private Class<T> clazz;

	private int array_size;
	private int indexofset;

	/// Copy constructor disallowed.
	public TitanPortArray(final TitanPortArray<T> otherValue) {
		clazz = otherValue.clazz;
		array_elements = new ArrayList<T>();
		names = new ArrayList<String>();

		array_size = otherValue.array_size;
		indexofset = otherValue.indexofset;

		for (int i = 0; i < otherValue.array_size; i++) {
			//TODO: check otherValue.array_element[i] need a new variable 
			array_elements.add(otherValue.array_elements.get(i));
			names.add(otherValue.names.get(i));
		}
	}

	//originally operator=
	public TitanPortArray<T> assign(final TitanPortArray<T> otherValue) {
		array_elements = new ArrayList<T>();
		names = new ArrayList<String>();

		array_size = otherValue.array_size;
		indexofset = otherValue.indexofset;
		clazz = otherValue.clazz;
		for (int i = 0; i < array_size; i++) {
			//TODO: check otherValue.array_element[i] need a new variable 
			array_elements.add(otherValue.array_elements.get(i));
			names.add(otherValue.names.get(i));
		}

		return this;
	}
	
	//originally operator[]
	public T getAt(final int index_value) {
		return array_elements.get(getPortArrayIndex(index_value, array_elements.size(), indexofset));
	}

	//originally operator[]
	public T getAt(final TitanInteger index_value) {
		return array_elements.get(getPortArrayIndex(index_value.getInt(), array_elements.size(), indexofset));
	}

	//Static methods
	
	//originally get_port_array_index
	public static int getPortArrayIndex(final int index_value,final int array_size,final int index_offset) {
		if(index_value < index_offset) {
			throw new TtcnError(MessageFormat.format("Index underflow when accessing an element of a port array. The index value should be between {0} and {1} instead of {2}."
					, index_offset, index_offset + array_size - 1, index_value));
		}
		final int ret_val = index_value - index_offset;
		if(ret_val >= array_size) {
			throw new TtcnError(MessageFormat.format("Index overflow when accessing an element of a port array. The index value should be between {0} and {1} instead of {2}."
					,index_offset, index_offset + array_size - 1, index_value));
		}
		return ret_val;
	}
	
	public static int getPortArrayIndex(final TitanInteger index_value,final int array_size,final int index_offset) {
		index_value.mustBound("Accessing an element of a port array using an unbound index.");
		
		return getPortArrayIndex(index_value.getInt(), array_size, index_offset);
	}
	
}
