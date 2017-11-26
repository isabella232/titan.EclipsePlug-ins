/******************************************************************************
 * Copyright (c) 2000-2017 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

import java.text.MessageFormat;
/**
 * @author Farkas Izabella Ingrid
 *
 * TODO check if we need to extend TitanTimer
 * */
public class TitanTimerArray<T extends TitanTimer> extends TitanTimer {

	TitanTimer[] array_elements;
	String[] names;

	public Class<T> clazz;

	int array_size;
	int indexOffset;

	/// Copy constructor
	public TitanTimerArray(final TitanTimerArray<T> otherValue) {
		clazz = otherValue.clazz;
		array_size = otherValue.array_size;
		indexOffset = otherValue.indexOffset;
		array_elements = new TitanTimer[array_size];

		for (int i = 0; i < array_size ; ++i) {
			try {
				final T helper = clazz.newInstance();
				helper.assign(otherValue.array_elements[i]);
				array_elements[i] = helper;
			} catch (InstantiationException e) {
				throw new TtcnError(MessageFormat.format("Internal error: class `{0}'' could not be instantiated ({1}).", clazz, e));
			} catch (IllegalAccessException e) {
				throw new TtcnError(MessageFormat.format("Internal error: class `{0}'' could not be instantiated ({1}).", clazz, e));
			}
		}
	}

	/// Assignment disallowed.
	// originally operator=
	TitanTimerArray<T> assign(final TitanTimerArray<T> otherValue){
		array_size = otherValue.array_size;
		indexOffset = otherValue.indexOffset;
		array_elements = new TitanTimer[array_size];
		for (int i = 0; i < otherValue.array_size; ++i) {
			try {
				final T helper = clazz.newInstance();
				helper.assign(otherValue.array_element(i));
				array_elements[i] = helper;
			} catch (InstantiationException e) {
				throw new TtcnError(MessageFormat.format("Internal error: class `{0}'' could not be instantiated ({1}).", clazz, e));
			} catch (IllegalAccessException e) {
				throw new TtcnError(MessageFormat.format("Internal error: class `{0}'' could not be instantiated ({1}).", clazz, e));
			}
		}
		return this;
	}

	public TitanTimerArray(final Class<T> clazz, final int size, final int offset) {
		this.clazz = clazz;
		indexOffset = offset;

		array_elements = new TitanTimer[size];
		//TODO check strange usage
		setSize(size);

		names = new String[size];
	}

	public void setSize(final int length) {
		for (int i = array_size; i < length; ++i) {
			try {
				final T empty = clazz.newInstance();
				array_elements[i] = empty;
			} catch (InstantiationException e) {
				throw new TtcnError(MessageFormat.format("Internal error: class `{0}'' could not be instantiated ({1}).", clazz, e));
			} catch (IllegalAccessException e) {
				throw new TtcnError(MessageFormat.format("Internal error: class `{0}'' could not be instantiated ({1}).", clazz, e));
			}
		}
		array_size = length;
	}

	public void setOffset(final int offset) {
		indexOffset = offset;
	}

	// originally T& operator[](int)
	@SuppressWarnings("unchecked")
	public T getAt(final int index) {
		return (T)array_elements[getTimerArrayIndex(index, array_size, indexOffset)];
	}

	//originally T& operator[](const INTEGER)
	@SuppressWarnings("unchecked")
	public T getAt(final TitanInteger index) {
		return (T)array_elements[getTimerArrayIndex(index, array_size, indexOffset)];
	}

	//const originally T& operator[](int)
	@SuppressWarnings("unchecked")
	public T constGetAt(final int index) {
		return (T)array_elements[getTimerArrayIndex(index, array_size, indexOffset)];
	}

	// const // originally T& operator[](const INTEGER)
	@SuppressWarnings("unchecked")
	public T constGetAt(final TitanInteger index) {
		return (T)array_elements[getTimerArrayIndex(index, array_size, indexOffset)];
	}

	@SuppressWarnings("unchecked")
	public T array_element(final int index) {
		return (T)array_elements[index];
	}

	@SuppressWarnings("unchecked")
	public T array_element(final TitanInteger index) {
		if (!index.isBound()) {
			throw new TtcnError("Accessing an element of an array using an unbound index.");
		}

		return (T)array_elements[index.getInt()];
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

	public void setName(final String name_string)
	{
		for (int i = 0; i < array_size; ++i) {
			// index_offset may be negative, hence i must be int (not size_t)
			// to ensure that signed arithmetic is used.
			names[i] = name_string + '['+(indexOffset+i) + ']';
			array_elements[i].setName(names[i]);
		}
	}

	public void log()
	{
		TtcnLogger.log_event_str("{ ");
		for (int v_index = 0; v_index < array_size; v_index++) {
			if (v_index > 0) {
				TtcnLogger.log_event_str(", ");
			}
			array_elements[v_index].log();
		}
		TtcnLogger.log_event_str(" }");
	}

	// static functions

	public static int getTimerArrayIndex(final int indexValue, final int arraySize, final int indexOffset) {
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
	public static int getTimerArrayIndex(final TitanInteger indexValue, final int arraySize, final int indexOffset) {
		if (! indexValue.isBound()) {
			throw new TtcnError("Accessing an element of a timer array using an unbound index.");
		}

		return getTimerArrayIndex(indexValue.getInt(), arraySize, indexOffset);
	}

	// alt-status priority: ALT_YES (return immediately) > ALT_REPEAT > ALT_MAYBE > ALT_NO

	// originally alt_status timeout(Index_Redirect* index_redirect)
	public TitanAlt_Status timeout(final Index_Redirect index_redirect) {
		if (index_redirect != null) {
			index_redirect.incrPos();
		}

		TitanAlt_Status result = TitanAlt_Status.ALT_NO;
		for (int i = 0; i < array_size; ++i) {
			final TitanAlt_Status ret_val = array_elements[i].timeout(index_redirect);
			if (ret_val == TitanAlt_Status.ALT_YES) {
				if (index_redirect != null) {
					index_redirect.addIndex(i + indexOffset);
				}
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
	public boolean running(final Index_Redirect index_redirect) {
		if (index_redirect != null) {
			index_redirect.incrPos();
		}
		boolean ret_val = false;
		for (int i = 0; i < array_size; ++i) {
			ret_val = array_elements[i].running(index_redirect);
			if (ret_val) {
				if (index_redirect != null) {
					index_redirect.addIndex(i + indexOffset);
				}
				break;
			}
		}
		if (index_redirect != null) {
			index_redirect.decrPos();
		}
		return ret_val;
	}
}
