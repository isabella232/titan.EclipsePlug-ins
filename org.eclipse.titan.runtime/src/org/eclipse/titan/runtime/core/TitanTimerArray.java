/******************************************************************************
 * Copyright (c) 2000-2017 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

import java.util.ArrayList;
/**
 * @author Farkas Izabella Ingrid
 * 
 * TODO check if we need to extend TitanTimer
 * */
public class TitanTimerArray<T extends TitanTimer> extends TitanTimer {

	ArrayList<T> array_elements;
	ArrayList<String> names;

	public Class<T> clazz;

	int array_size;
	int indexOffset;

	/// Copy constructor disallowed.
	TitanTimerArray(final TitanTimerArray<T> otherValue) {
		clazz = otherValue.clazz; 
		array_elements = new ArrayList<T>();
		array_size = otherValue.array_size;
		indexOffset = otherValue.indexOffset;

		for (int i = 0; i < array_size ; ++i) {
			try {
				T helper = clazz.newInstance();
				helper.assign(otherValue.array_elements.get(i));
				array_elements.add(helper);
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/// Assignment disallowed.
	// originally operator=
	TitanTimerArray<T> assign(final TitanTimerArray<T> otherValue){
		array_size = otherValue.array_size;
		indexOffset = otherValue.indexOffset;
		array_elements = new ArrayList<T>(array_size);
		for (int i = 0; i < otherValue.array_size; ++i) {
			array_elements.add(otherValue.array_element(i));
		}
		return this;
	}

	public TitanTimerArray(final Class<T> clazz) {
		this.clazz = clazz;
		array_elements = new ArrayList<T>();
		names = new ArrayList<String>();
	}

	public void setSize(final int length) {
		for (int i = array_size; i < length; ++i) {
			try {
				T emply = clazz.newInstance();
				array_elements.add(emply);
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		array_size = length;
	}

	public void setOffset(final int offset) {
		indexOffset = offset;
	}

	// originally T& operator[](int)
	public T getAt(final int index) {
		return array_elements.get(getTimerArrayIndex(index, array_size, indexOffset));
	}

	//originally T& operator[](const INTEGER)
	public T getAt(final TitanInteger index) {
		return array_elements.get(getTimerArrayIndex(index, array_size, indexOffset));
	}
	//const originally T& operator[](int)
	public T constGetAt(final int index) {
		return array_elements.get(getTimerArrayIndex(index, array_size, indexOffset));
	}

	// const // originally T& operator[](const INTEGER)
	public T constGetAt(final TitanInteger index) {
		return array_elements.get(getTimerArrayIndex(index, array_size, indexOffset));
	}

	public T array_element(final int index) {
		return array_elements.get(index); 
	}

	public T array_element(final TitanInteger index) {
		if (! index.isBound().getValue()) {
			throw new TtcnError("Accessing an element of an array using an unbound index.");
		}

		return array_elements.get(index.getInt()); 
	}

	public int n_elem() {
		return array_size;
	}
	public int sizeOf() {
		return array_size;
	}
	public int lengthOf() {
		return array_size;
	}

	public void setName(String name_string)
	{
		for (int i = 0; i < array_size; ++i) {
			// index_offset may be negative, hence i must be int (not size_t)
			// to ensure that signed arithmetic is used.
			names.add(name_string + '['+(indexOffset+i) + ']');
			array_elements.get(i).setName(names.get(i));
		}
	}

	public void log()
	{
		TtcnLogger.log_event_str("{ ");
		for (int v_index = 0; v_index < array_size; v_index++) {
			if (v_index > 0) {
				TtcnLogger.log_event_str(", ");
			}
			array_elements.get(v_index).log();
		}
		TtcnLogger.log_event_str(" }");
	}

	// static functions

	public static int getTimerArrayIndex(int indexValue, int arraySize, int indexOffset) {
		if (arraySize < 0) {
			throw new TtcnError("Invalid array size");
		}

		if (indexValue < indexOffset) {
			throw new TtcnError("Index underflow when accessing an element of a timer array. "+
					"The index value should be between "+indexOffset+" and "+(indexOffset+arraySize-1)+" instead of "+indexValue+".");
		}

		final int result = indexValue - indexOffset;
		if (result >= arraySize) {
			throw new TtcnError("Index underflow when accessing an element of a timer array. "+
					"The index value should be between "+indexOffset+" and "+(indexOffset+arraySize-1)+" instead of "+indexValue+".");
		}

		return result;
	}
	public static int getTimerArrayIndex(TitanInteger indexValue, int arraySize, int indexOffset) {
		if (! indexValue.isBound().getValue()) {
			throw new TtcnError("Accessing an element of a timer array using an unbound index.");
		}

		return getTimerArrayIndex(indexValue.getInt(), arraySize, indexOffset);
	}

	//TODO: timeout, running
	// alt-status priority: ALT_YES (return immediately) > ALT_REPEAT > ALT_MAYBE > ALT_NO

	// originally alt_status timeout(Index_Redirect* index_redirect)
	public TitanAlt_Status timeout() {
		//FIXME handle redirection
		/* if (index_redirect != NULL) {
	      index_redirect->incr_pos();
	    }*/
		TitanAlt_Status result = TitanAlt_Status.ALT_NO;
		for (int i = 0; i < array_size; ++i) {
			TitanAlt_Status ret_val = array_elements.get(i).timeout();
			if (ret_val == TitanAlt_Status.ALT_YES) {
				// if (index_redirect != NULL) {
				//	 index_redirect->add_index((int)i + index_offset);
				// }
				result = ret_val;
				break;
			}
			else if (ret_val == TitanAlt_Status.ALT_REPEAT ||
					(ret_val == TitanAlt_Status.ALT_MAYBE && result == TitanAlt_Status.ALT_NO)) {
				result = ret_val;
			}
		}

		return result;
	}

	// originally boolean running(Index_Redirect* index_redirect) const
	public boolean running() {
		//FIXME handle redirection
		// if (index_redirect != NULL) {
		//	 index_redirect->incr_pos();
		// }
		boolean ret_val = false;
		for (int i = 0; i < array_size; ++i) {
			ret_val = array_elements.get(i).running();
			if (ret_val) {
				//	        if (index_redirect != NULL) {
				//	          index_redirect->add_index((int)i + index_offset);
				//	        }
				break;
			}
		}
		// if (index_redirect != NULL) {
		// 	 index_redirect->decr_pos();
		// }
		return ret_val;
	}
}
