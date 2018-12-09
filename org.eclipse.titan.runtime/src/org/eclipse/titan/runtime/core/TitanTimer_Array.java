/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

import java.text.MessageFormat;
/**
 * @author Farkas Izabella Ingrid
 *
 * TODO check if we need to extend TitanTimer
 * */
public class TitanTimer_Array<T extends TitanTimer> extends TitanTimer {

	TitanTimer[] array_elements;
	String[] names;

	public Class<T> clazz;

	int array_size;
	int indexOffset;

	/// Copy constructor
	public TitanTimer_Array(final TitanTimer_Array<T> otherValue) {
		clazz = otherValue.clazz;
		array_size = otherValue.array_size;
		indexOffset = otherValue.indexOffset;
		array_elements = new TitanTimer[array_size];

		for (int i = 0; i < array_size; ++i) {
			try {
				final T helper = clazz.newInstance();
				helper.operator_assign(otherValue.array_elements[i]);
				array_elements[i] = helper;
			} catch (InstantiationException e) {
				throw new TtcnError(MessageFormat.format("Internal error: class `{0}'' could not be instantiated ({1}).", clazz, e));
			} catch (IllegalAccessException e) {
				throw new TtcnError(MessageFormat.format("Internal error: class `{0}'' could not be instantiated ({1}).", clazz, e));
			}
		}
	}

	/**
	 * Assigns the other value to this value.
	 * Overwriting the current content in the process.
	 *<p>
	 * operator= in the core.
	 *
	 * @param otherValue
	 *                the other value to assign.
	 * @return the new value object.
	 */
	TitanTimer_Array<T> operator_assign(final TitanTimer_Array<T> otherValue){
		array_size = otherValue.array_size;
		indexOffset = otherValue.indexOffset;
		array_elements = new TitanTimer[array_size];
		for (int i = 0; i < otherValue.array_size; ++i) {
			try {
				final T helper = clazz.newInstance();
				helper.operator_assign(otherValue.array_element(i));
				array_elements[i] = helper;
			} catch (InstantiationException e) {
				throw new TtcnError(MessageFormat.format("Internal error: class `{0}'' could not be instantiated ({1}).", clazz, e));
			} catch (IllegalAccessException e) {
				throw new TtcnError(MessageFormat.format("Internal error: class `{0}'' could not be instantiated ({1}).", clazz, e));
			}
		}
		return this;
	}

	public TitanTimer_Array(final Class<T> clazz, final int size, final int offset) {
		this.clazz = clazz;
		indexOffset = offset;

		array_elements = new TitanTimer[size];
		//TODO check strange usage
		set_size(size);

		names = new String[size];
	}

	public void set_size(final int length) {
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

	public void set_offset(final int offset) {
		indexOffset = offset;
	}

	// originally T& operator[](int)
	@SuppressWarnings("unchecked")
	public T get_at(final int index) {
		return (T)array_elements[get_timer_array_index(index, array_size, indexOffset)];
	}

	//originally T& operator[](const INTEGER)
	@SuppressWarnings("unchecked")
	public T get_at(final TitanInteger index) {
		return (T)array_elements[get_timer_array_index(index, array_size, indexOffset)];
	}

	//const originally T& operator[](int)
	@SuppressWarnings("unchecked")
	public T constGet_at(final int index) {
		return (T)array_elements[get_timer_array_index(index, array_size, indexOffset)];
	}

	// const // originally T& operator[](const INTEGER)
	@SuppressWarnings("unchecked")
	public T constGet_at(final TitanInteger index) {
		return (T)array_elements[get_timer_array_index(index, array_size, indexOffset)];
	}

	@SuppressWarnings("unchecked")
	public T array_element(final int index) {
		return (T)array_elements[index];
	}

	@SuppressWarnings("unchecked")
	public T array_element(final TitanInteger index) {
		index.must_bound("Accessing an element of an array using an unbound index.");

		return (T)array_elements[index.get_int()];
	}

	public int n_elem() {
		return array_size;
	}

	/**
	 * Returns the number of elements, that is, the largest used index plus
	 * one and zero for the empty value.
	 *
	 * size_of in the core
	 *
	 * @return the number of elements.
	 * */
	public int size_of() {
		return array_size;
	}
	public int lengthof() {
		return array_size;
	}

	public void set_name(final String name_string)
	{
		for (int i = 0; i < array_size; ++i) {
			// index_offset may be negative, hence i must be int (not size_t)
			// to ensure that signed arithmetic is used.
			names[i] = name_string + '[' + (indexOffset + i) + ']';
			array_elements[i].set_name(names[i]);
		}
	}

	@Override
	public void log() {
		TTCN_Logger.log_event_str("{ ");
		for (int v_index = 0; v_index < array_size; v_index++) {
			if (v_index > 0) {
				TTCN_Logger.log_event_str(", ");
			}
			array_elements[v_index].log();
		}
		TTCN_Logger.log_event_str(" }");
	}

	// static functions

	public static int get_timer_array_index(final int indexValue, final int arraySize, final int indexOffset) {
		if (arraySize < 0) {
			throw new TtcnError("Invalid array size");
		}

		if (indexValue < indexOffset) {
			throw new TtcnError("Index underflow when accessing an element of a timer array. "
					+ "The index value should be between " + indexOffset + " and " + (indexOffset + arraySize - 1) + " instead of " + indexValue + ".");
		}

		final int result = indexValue - indexOffset;
		if (result >= arraySize) {
			throw new TtcnError("Index underflow when accessing an element of a timer array. " + "The index value should be between "
					+ indexOffset + " and " + (indexOffset + arraySize - 1) + " instead of " + indexValue + ".");
		}

		return result;
	}
	public static int get_timer_array_index(final TitanInteger indexValue, final int arraySize, final int indexOffset) {
		indexValue.must_bound("Accessing an element of a timer array using an unbound index.");

		return get_timer_array_index(indexValue.get_int(), arraySize, indexOffset);
	}

	// alt-status priority: ALT_YES (return immediately) > ALT_REPEAT > ALT_MAYBE > ALT_NO

	// originally alt_status timeout(Index_Redirect* index_redirect)
	public TitanAlt_Status timeout(final Index_Redirect index_redirect) {
		if (index_redirect != null) {
			index_redirect.incr_pos();
		}

		TitanAlt_Status result = TitanAlt_Status.ALT_NO;
		for (int i = 0; i < array_size; ++i) {
			final TitanAlt_Status ret_val = array_elements[i].timeout(index_redirect);
			if (ret_val == TitanAlt_Status.ALT_YES) {
				if (index_redirect != null) {
					index_redirect.add_index(i + indexOffset);
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
			index_redirect.incr_pos();
		}
		boolean ret_val = false;
		for (int i = 0; i < array_size; ++i) {
			ret_val = array_elements[i].running(index_redirect);
			if (ret_val) {
				if (index_redirect != null) {
					index_redirect.add_index(i + indexOffset);
				}
				break;
			}
		}
		if (index_redirect != null) {
			index_redirect.decr_pos();
		}
		return ret_val;
	}
}
