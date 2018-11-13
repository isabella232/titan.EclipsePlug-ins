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
 * Represent an array of ports.
 *
 *
 * @author Gergo Ujhelyi
 **/

public class TitanPortArray<T extends TitanPort> extends TitanPort {

	private TitanPort[] array_elements;
	private String[] names;

	private Class<T> clazz;

	private int array_size;
	private int indexofset;

	// Copy constructor
	public TitanPortArray(final TitanPortArray<T> otherValue) {
		clazz = otherValue.clazz;
		array_size = otherValue.array_size;
		indexofset = otherValue.indexofset;
		array_elements = new TitanPort[array_size];
		names = new String[array_size];

		for (int i = 0; i < array_size; i++) {
			// TODO: check otherValue.array_element[i] need a new variable
			array_elements[i] = otherValue.array_elements[i];
			names[i] = otherValue.names[i];
		}
	}

	public TitanPortArray(final Class<T> clazz, final int size, final int offset) {
		this.clazz = clazz;
		indexofset = offset;
		array_elements = new TitanPort[size];
		names = new String[size];
		set_size(size);
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
	public TitanPortArray<T> assign(final TitanPortArray<T> otherValue) {
		array_size = otherValue.array_size;
		indexofset = otherValue.indexofset;
		clazz = otherValue.clazz;
		array_elements = new TitanPort[array_size];
		names = new String[array_size];

		for (int i = 0; i < array_size; i++) {
			// TODO: check otherValue.array_element[i] need a new variable
			array_elements[i] = otherValue.array_elements[i];
			names[i] = otherValue.names[i];
		}

		return this;
	}

	// originally operator[]
	@SuppressWarnings("unchecked")
	public T getAt(final int index_value) {
		return (T)array_elements[get_port_array_index(index_value, array_size, indexofset)];
	}

	// originally operator[]
	@SuppressWarnings("unchecked")
	public T getAt(final TitanInteger index_value) {
		return (T)array_elements[get_port_array_index(index_value.getInt(), array_size, indexofset)];
	}

	// originally operator[]
	public final T constGetAt(final int index_value) {
		return getAt(index_value);
	}

	//originally operator[]
	public final T constGetAt(final TitanInteger index_value) {
		return getAt(index_value);
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

	//originally lengthof
	public int lengthof() {
		return array_size;
	}

	@Override
	public void set_name(final String name_string) {
		for (int i = 0; i < array_size; i++) {
			names[i] = name_string + '[' + i + ']';
			array_elements[i].set_name(names[i]);
		}
	}

	@Override
	public void activate_port(final boolean system) {
		for (int v_index = 0; v_index < array_size; v_index++) {
			array_elements[v_index].activate_port(system);
		}
	}

	@Override
	// needed by the init_system_port function
	public void safe_start() {
		for (int v_index = 0; v_index < array_size; v_index++) {
			array_elements[v_index].safe_start();
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

	// alt-status priority: ALT_YES (return immediately) > ALT_REPEAT > ALT_MAYBE > ALT_NO
	@Override
	public TitanAlt_Status receive(final TitanComponent_template sender_template, final TitanComponent sender_ptr, final TitanFloat timestemp_redirect, final Index_Redirect index_redirect) {
		if (index_redirect != null) {
			index_redirect.incrPos();
		}

		TitanAlt_Status result = TitanAlt_Status.ALT_NO;
		for (int i = 0; i < array_size; i++) {
			final TitanAlt_Status ret_val = array_elements[i].receive(sender_template, sender_ptr, timestemp_redirect, index_redirect);
			if (ret_val == TitanAlt_Status.ALT_YES) {
				if (index_redirect != null) {
					index_redirect.addIndex(i + indexofset);
				}
				result = ret_val;
				break;
			} else if (ret_val == TitanAlt_Status.ALT_REPEAT || (ret_val == TitanAlt_Status.ALT_MAYBE && result == TitanAlt_Status.ALT_NO)) {
				result = ret_val;
			}
		}
		if (index_redirect != null) {
			index_redirect.decrPos();
		}

		return result;
	}

	// originally check_receive
	@Override
	public TitanAlt_Status check_receive(final TitanComponent_template sender_template, final TitanComponent sender_ptr, final TitanFloat timestemp_redirect, final Index_Redirect index_redirect) {
		if (index_redirect != null) {
			index_redirect.incrPos();
		}

		TitanAlt_Status result = TitanAlt_Status.ALT_NO;
		for (int i = 0; i < array_size; i++) {
			final TitanAlt_Status ret_val = array_elements[i].check_receive(sender_template, sender_ptr, timestemp_redirect, index_redirect);
			if (ret_val == TitanAlt_Status.ALT_YES) {
				if (index_redirect != null) {
					index_redirect.addIndex(i + indexofset);
				}
				result = ret_val;
				break;
			} else if (ret_val == TitanAlt_Status.ALT_REPEAT || (ret_val == TitanAlt_Status.ALT_MAYBE && result == TitanAlt_Status.ALT_NO)) {
				result = ret_val;
			}
		}
		if (index_redirect != null) {
			index_redirect.decrPos();
		}

		return result;
	}

	@Override
	public TitanAlt_Status trigger(final TitanComponent_template sender_template, final TitanComponent sender_ptr, final TitanFloat timestemp_redirect, final Index_Redirect index_redirect) {
		if (index_redirect != null) {
			index_redirect.incrPos();
		}

		TitanAlt_Status result = TitanAlt_Status.ALT_NO;
		for (int i = 0; i < array_size; i++) {
			final TitanAlt_Status ret_val = array_elements[i].trigger(sender_template, sender_ptr, timestemp_redirect, index_redirect);
			if (ret_val == TitanAlt_Status.ALT_YES) {
				if (index_redirect != null) {
					index_redirect.addIndex(i + indexofset);
				}
				result = ret_val;
				break;
			} else if (ret_val == TitanAlt_Status.ALT_REPEAT || (ret_val == TitanAlt_Status.ALT_MAYBE && result == TitanAlt_Status.ALT_NO)) {
				result = ret_val;
			}
		}
		if (index_redirect != null) {
			index_redirect.decrPos();
		}

		return result;
	}

	@Override
	public TitanAlt_Status getcall(final TitanComponent_template sender_template, final TitanComponent sender_ptr, final TitanFloat timestemp_redirect, final Index_Redirect index_redirect) {
		if (index_redirect != null) {
			index_redirect.incrPos();
		}

		TitanAlt_Status result = TitanAlt_Status.ALT_NO;
		for (int i = 0; i < array_size; i++) {
			final TitanAlt_Status ret_val = array_elements[i].getcall(sender_template, sender_ptr, timestemp_redirect, index_redirect);
			if (ret_val == TitanAlt_Status.ALT_YES) {
				if (index_redirect != null) {
					index_redirect.addIndex(i + indexofset);
				}
				result = ret_val;
				break;
			} else if (ret_val == TitanAlt_Status.ALT_REPEAT || (ret_val == TitanAlt_Status.ALT_MAYBE && result == TitanAlt_Status.ALT_NO)) {
				result = ret_val;
			}
		}
		if (index_redirect != null) {
			index_redirect.decrPos();
		}

		return result;
	}

	@Override
	public TitanAlt_Status getreply(final TitanComponent_template sender_template, final TitanComponent sender_ptr, final TitanFloat timestemp_redirect, final Index_Redirect index_redirect) {
		if (index_redirect != null) {
			index_redirect.incrPos();
		}

		TitanAlt_Status result = TitanAlt_Status.ALT_NO;
		for (int i = 0; i < array_size; i++) {
			final TitanAlt_Status ret_val = array_elements[i].getreply(sender_template, sender_ptr, timestemp_redirect, index_redirect);
			if (ret_val == TitanAlt_Status.ALT_YES) {
				if (index_redirect != null) {
					index_redirect.addIndex(i + indexofset);
				}
				result = ret_val;
				break;
			} else if (ret_val == TitanAlt_Status.ALT_REPEAT || (ret_val == TitanAlt_Status.ALT_MAYBE && result == TitanAlt_Status.ALT_NO)) {
				result = ret_val;
			}
		}
		if (index_redirect != null) {
			index_redirect.decrPos();
		}

		return result;
	}

	@Override
	public TitanAlt_Status get_exception(final TitanComponent_template sender_template, final TitanComponent sender_ptr,
			final TitanFloat timestemp_redirect, final Index_Redirect index_redirect) {
		if (index_redirect != null) {
			index_redirect.incrPos();
		}

		TitanAlt_Status result = TitanAlt_Status.ALT_NO;
		for (int i = 0; i < array_size; i++) {
			final TitanAlt_Status ret_val = array_elements[i].get_exception(sender_template, sender_ptr, timestemp_redirect, index_redirect);
			if (ret_val == TitanAlt_Status.ALT_YES) {
				if (index_redirect != null) {
					index_redirect.addIndex(i + indexofset);
				}
				result = ret_val;
				break;
			} else if (ret_val == TitanAlt_Status.ALT_REPEAT || (ret_val == TitanAlt_Status.ALT_MAYBE && result == TitanAlt_Status.ALT_NO)) {
				result = ret_val;
			}
		}
		if (index_redirect != null) {
			index_redirect.decrPos();
		}

		return result;
	}

	@Override
	public TitanAlt_Status check_catch(final TitanComponent_template sender_template, final TitanComponent sender_ptr,
			final TitanFloat timestemp_redirect, final Index_Redirect index_redirect) {
		if (index_redirect != null) {
			index_redirect.incrPos();
		}

		TitanAlt_Status result = TitanAlt_Status.ALT_NO;
		for (int i = 0; i < array_size; i++) {
			final TitanAlt_Status ret_val = array_elements[i].check_catch(sender_template, sender_ptr, timestemp_redirect, index_redirect);
			if (ret_val == TitanAlt_Status.ALT_YES) {
				if (index_redirect != null) {
					index_redirect.addIndex(i + indexofset);
				}
				result = ret_val;
				break;
			} else if (ret_val == TitanAlt_Status.ALT_REPEAT || (ret_val == TitanAlt_Status.ALT_MAYBE && result == TitanAlt_Status.ALT_NO)) {
				result = ret_val;
			}
		}
		if (index_redirect != null) {
			index_redirect.decrPos();
		}

		return result;
	}

	@Override
	public TitanAlt_Status check(final TitanComponent_template sender_template, final TitanComponent sender_ptr, final TitanFloat timestemp_redirect, final Index_Redirect index_redirect) {
		if (index_redirect != null) {
			index_redirect.incrPos();
		}

		TitanAlt_Status result = TitanAlt_Status.ALT_NO;
		for (int i = 0; i < array_size; i++) {
			final TitanAlt_Status ret_val = array_elements[i].check(sender_template, sender_ptr, timestemp_redirect, index_redirect);
			if (ret_val == TitanAlt_Status.ALT_YES) {
				if (index_redirect != null) {
					index_redirect.addIndex(i + indexofset);
				}
				result = ret_val;
				break;
			} else if (ret_val == TitanAlt_Status.ALT_REPEAT || (ret_val == TitanAlt_Status.ALT_MAYBE && result == TitanAlt_Status.ALT_NO)) {
				result = ret_val;
			}
		}
		if (index_redirect != null) {
			index_redirect.decrPos();
		}

		return result;
	}

	//Static methods
	public static int get_port_array_index(final int index_value,final int array_size,final int index_offset) {
		if (index_value < index_offset) {
			throw new TtcnError(MessageFormat.format("Index underflow when accessing an element of a port array. The index value should be between {0} and {1} instead of {2}."
					, index_offset, index_offset + array_size - 1, index_value));
		}
		final int ret_val = index_value - index_offset;
		if (ret_val >= array_size) {
			throw new TtcnError(MessageFormat.format("Index overflow when accessing an element of a port array. The index value should be between {0} and {1} instead of {2}.",
					index_offset, index_offset + array_size - 1, index_value));
		}
		return ret_val;
	}

	public static int get_port_array_index(final TitanInteger index_value, final int array_size, final int index_offset) {
		index_value.mustBound("Accessing an element of a port array using an unbound index.");

		return get_port_array_index(index_value.getInt(), array_size, index_offset);
	}

}
