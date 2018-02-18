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
		setSize(size);
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

	// originally operator=
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
		return (T)array_elements[getPortArrayIndex(index_value, array_size, indexofset)];
	}

	// originally operator[]
	@SuppressWarnings("unchecked")
	public T getAt(final TitanInteger index_value) {
		return (T)array_elements[getPortArrayIndex(index_value.getInt(), array_size, indexofset)];
	}

	// originally operator[]
	public final T constGetAt(final int index_value) {
		return getAt(index_value);
	}

	//originally operator[]
	public final T constGetAt(final TitanInteger index_value) {
		return getAt(index_value);
	}

	//originally n_elem
	public int nElem() {
		return array_size;
	}

	//originally size_of
	public int sizeOf() {
		return array_size;
	}

	//originally lengthof
	public int lengthOf() {
		return array_size;
	}

	//originally set_name
	public void set_name(final String name_string) {
		for (int i = 0; i < array_size; i++) {
			names[i] = name_string;
			array_elements[i].setName(name_string);
		}
	}

	//originally activate_port
	public void activatePort() {
		for (int v_index = 0; v_index < array_size; v_index++) {
			//FIXME: TitanPort.activatePort()
			array_elements[v_index].activate_port(false);
		}
	}

	public void log() {
		TtcnLogger.log_event_str("{ ");
		for (int v_index = 0; v_index < array_size; v_index++) {
			if (v_index > 0) {
				TtcnLogger.log_event_str(", ");
			}
			array_elements[v_index].log();
		}
		TtcnLogger.log_event_str(" }");
	}

	// alt-status priority: ALT_YES (return immediately) > ALT_REPEAT > ALT_MAYBE > ALT_NO

	public TitanAlt_Status receive(final TitanComponent_template sender_template, final TitanComponent sender_ptr, final Index_Redirect index_redirect) {
		if (index_redirect != null) {
			index_redirect.incrPos();
		}

		TitanAlt_Status result = TitanAlt_Status.ALT_NO;
		for (int i = 0; i < array_size; i++) {
			final TitanAlt_Status ret_val = array_elements[i].receive(sender_template, sender_ptr, index_redirect);
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
	public TitanAlt_Status checkReceive(final TitanComponent_template sender_template, final TitanComponent sender_ptr, final Index_Redirect index_redirect) {
		if (index_redirect != null) {
			index_redirect.incrPos();
		}

		TitanAlt_Status result = TitanAlt_Status.ALT_NO;
		for (int i = 0; i < array_size; i++) {
			final TitanAlt_Status ret_val = array_elements[i].check_receive(sender_template, sender_ptr, index_redirect);
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

	public TitanAlt_Status trigger(final TitanComponent_template sender_template, final TitanComponent sender_ptr, final Index_Redirect index_redirect) {
		if (index_redirect != null) {
			index_redirect.incrPos();
		}

		TitanAlt_Status result = TitanAlt_Status.ALT_NO;
		for (int i = 0; i < array_size; i++) {
			final TitanAlt_Status ret_val = array_elements[i].trigger(sender_template, sender_ptr, index_redirect);
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

	public TitanAlt_Status getcall(final TitanComponent_template sender_template, final TitanComponent sender_ptr, final Index_Redirect index_redirect) {
		if (index_redirect != null) {
			index_redirect.incrPos();
		}

		TitanAlt_Status result = TitanAlt_Status.ALT_NO;
		for (int i = 0; i < array_size; i++) {
			final TitanAlt_Status ret_val = array_elements[i].getcall(sender_template, sender_ptr, index_redirect);
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

	public TitanAlt_Status getreply(final TitanComponent_template sender_template, final TitanComponent sender_ptr, final Index_Redirect index_redirect) {
		if (index_redirect != null) {
			index_redirect.incrPos();
		}

		TitanAlt_Status result = TitanAlt_Status.ALT_NO;
		for (int i = 0; i < array_size; i++) {
			final TitanAlt_Status ret_val = array_elements[i].getreply(sender_template, sender_ptr, index_redirect);
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

	public TitanAlt_Status getException(final TitanComponent_template sender_template, final TitanComponent sender_ptr,
			final Index_Redirect index_redirect) {
		if (index_redirect != null) {
			index_redirect.incrPos();
		}

		TitanAlt_Status result = TitanAlt_Status.ALT_NO;
		for (int i = 0; i < array_size; i++) {
			final TitanAlt_Status ret_val = array_elements[i].get_exception(sender_template, sender_ptr, index_redirect);
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

	public TitanAlt_Status checkCatch(final TitanComponent_template sender_template, final TitanComponent sender_ptr,
			final Index_Redirect index_redirect) {
		if (index_redirect != null) {
			index_redirect.incrPos();
		}

		TitanAlt_Status result = TitanAlt_Status.ALT_NO;
		for (int i = 0; i < array_size; i++) {
			final TitanAlt_Status ret_val = array_elements[i].check_catch(sender_template, sender_ptr, index_redirect);
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

	public TitanAlt_Status check(final TitanComponent_template sender_template, final TitanComponent sender_ptr, final Index_Redirect index_redirect) {
		if (index_redirect != null) {
			index_redirect.incrPos();
		}

		TitanAlt_Status result = TitanAlt_Status.ALT_NO;
		for (int i = 0; i < array_size; i++) {
			final TitanAlt_Status ret_val = array_elements[i].check(sender_template, sender_ptr, index_redirect);
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

	//originally get_port_array_index
	public static int getPortArrayIndex(final int index_value,final int array_size,final int index_offset) {
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

	public static int getPortArrayIndex(final TitanInteger index_value, final int array_size, final int index_offset) {
		index_value.mustBound("Accessing an element of a port array using an unbound index.");

		return getPortArrayIndex(index_value.getInt(), array_size, index_offset);
	}

}
