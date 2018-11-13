/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * "record of" type for runtime 2
 * Originally Basetype.hh/Record_Of_Type, Basetype2.cc/Record_Of_Type
 * @author Arpad Lovassy
 */
public abstract class TitanRecordOf extends Base_Type {

	/**
	 * Indexed sequence of elements of the same type
	 * Originally value_elements
	 */
	List<Base_Type> valueElements;

	/** type of the element of "record of" */
	private final Class<? extends Base_Type> ofType;

	public TitanRecordOf(final Class<? extends Base_Type> aOfType) {
		this.ofType = aOfType;
	}

	public TitanRecordOf(final TitanRecordOf other_value) {
		other_value.mustBound("Copying an unbound record of/set of value.");
		ofType = other_value.ofType;
		valueElements = copy_list(other_value.valueElements);
	}

	@Override
	public boolean is_present() {
		return is_bound();
	}

	@Override
	public boolean is_bound() {
		return valueElements != null;
	}

	public void mustBound(final String aErrorMessage) {
		if (!is_bound()) {
			throw new TtcnError(aErrorMessage);
		}
	}

	@Override
	public void clean_up() {
		valueElements = null;
	}

	@Override
	public void log() {
		if (valueElements == null) {
			TTCN_Logger.log_event_unbound();
			return;
		}

		TTCN_Logger.log_event_str("{ ");
		final int size = valueElements.size();
		for (int i = 0; i < size; i++) {
			if (i > 0) {
				TTCN_Logger.log_event_str(", ");
			}
			valueElements.get(i).log();
		}
		TTCN_Logger.log_event_str(" }");
	}

	@Override
	/** {@inheritDoc} */
	public void encode_text(final Text_Buf text_buf) {
		if (valueElements == null) {
			throw new TtcnError("Text encoder: Encoding an unbound value of type record of.");
		}
		text_buf.push_int(valueElements.size());
		for (int i = 0; i < valueElements.size(); i++) {
			valueElements.get(i).encode_text(text_buf);
		}
	}

	@Override
	/** {@inheritDoc} */
	public void decode_text(final Text_Buf text_buf) {
		final int new_size = text_buf.pull_int().getInt();
		if (new_size < 0) {
			throw new TtcnError("Text decoder: Negative size was received for a value of type record of.");
		}
		valueElements = new ArrayList<Base_Type>(new_size);
		for (int i = 0; i < new_size; i++) {
			final Base_Type newElem = get_unbound_elem();
			newElem.decode_text(text_buf);
			valueElements.add(newElem);
		}
	}

	/**
	 * @return {@code true} if and only if otherValue is the same record of type as this
	 */
	private boolean isSameType(final Base_Type otherValue) {
		return otherValue instanceof TitanRecordOf && ofType == ((TitanRecordOf)otherValue).ofType;
	}

	@Override
	public boolean operatorEquals(final Base_Type otherValue) {
		if (!isSameType(otherValue)) {
			throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to record of {1}", otherValue, getOfTypeName()));
		}

		//compiler warning is incorrect, it is not unchecked
		return operatorEquals((TitanRecordOf) otherValue);
	}

	/**
	 * Checks if the current value is equivalent to the provided one.
	 *
	 * operator== in the core
	 *
	 * @param otherValue
	 *                the other value to check against.
	 * @return {@code true} if the values are equivalent.
	 */
	public boolean operatorEquals(final TitanRecordOf otherValue) {
		mustBound("The left operand of comparison is an unbound value of type record of " + getOfTypeName() + ".");
		otherValue.mustBound("The right operand of comparison is an unbound value of type" + otherValue.getOfTypeName() + ".");

		final int size = valueElements.size();
		if (size != otherValue.valueElements.size()) {
			return false;
		}

		for (int i = 0; i < size; i++) {
			final Base_Type leftElem = valueElements.get(i);
			final Base_Type rightElem = otherValue.valueElements.get(i);
			if (!leftElem.operatorEquals(rightElem)) {
				return false;
			}
		}

		return true;
	}

	@Override
	public Base_Type assign(final Base_Type otherValue) {
		if (!isSameType(otherValue)) {
			throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to record of {1}", otherValue, getOfTypeName()));
		}

		// compiler warning is incorrect, it is not unchecked
		return assign((TitanRecordOf) otherValue);
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
	public TitanRecordOf assign(final TitanRecordOf otherValue) {
		otherValue.mustBound("Assignment of an unbound record of value.");

		valueElements = otherValue.valueElements;
		return this;
	}

	public final List<Base_Type> copy_list(final List<Base_Type> srcList) {
		if (srcList == null) {
			return null;
		}

		final List<Base_Type> newList = new ArrayList<Base_Type>(srcList.size());
		for (final Base_Type srcElem : srcList) {
			final Base_Type newElem = get_unbound_elem();
			newElem.assign(srcElem);
			newList.add(newElem);
		}
		return newList;
	}

	//originally get_at(int)
	public Base_Type get_at(final int index_value) {
		if (index_value < 0) {
			throw new TtcnError(MessageFormat.format("Accessing an element of type record of {0} using a negative index: {1}.", getOfTypeName(), index_value));
		}

		if (index_value >= valueElements.size()) {
			// increase list size
			for (int i = valueElements.size(); i <= index_value; i++) {
				valueElements.set(index_value, null);
			}
		}

		if (valueElements.get(index_value) == null) {
			final Base_Type newElem = get_unbound_elem();
			valueElements.set(index_value, newElem);
		}
		return valueElements.get(index_value);
	}

	//originally get_at(const INTEGER&)
	public Base_Type get_at(final TitanInteger index_value) {
		index_value.mustBound(MessageFormat.format("Using an unbound integer value for indexing a value of type {0}.", getOfTypeName()));
		return get_at(index_value.getInt());
	}

	//originally get_at(int) const
	public Base_Type constGet_at(final int index_value) {
		if (!is_bound()) {
			throw new TtcnError(MessageFormat.format("Accessing an element in an unbound value of type record of {0}.", getOfTypeName()));
		}
		if (index_value < 0) {
			throw new TtcnError(MessageFormat.format("Accessing an element of type record of {0} using a negative index: {1}.", getOfTypeName(), index_value));
		}
		final int nofElements = n_elem();
		if (index_value >= nofElements) {
			throw new TtcnError(MessageFormat.format("Index overflow in a value of type record of {0}: The index is {1}, but the value has only {2} elements.",
					getOfTypeName(), index_value, nofElements));
		}

		final Base_Type elem = valueElements.get(index_value);
		return (elem != null) ? elem : get_unbound_elem();
	}

	//originally get_at(const INTEGER&) const
	public Base_Type constGet_at(final TitanInteger index_value) {
		index_value.mustBound(MessageFormat.format("Using an unbound integer value for indexing a value of type {0}.", getOfTypeName()));

		return constGet_at(index_value.getInt());
	}

	private Base_Type get_unbound_elem() {
		try {
			return ofType.newInstance();
		} catch (Exception e) {
			throw new TtcnError(MessageFormat.format("Internal Error: exception `{0}'' thrown while instantiating class of `{1}'' type", e.getMessage(), getOfTypeName()));
		}
	}

	private static String getOfTypeName(final Class<? extends Base_Type> aOfType) {
		// FIXME: this is surely not good like this
		if (aOfType == TitanBoolean.class) {
			return "boolean";
		} else if (aOfType == TitanBitString.class) {
			return "bitstring";
		} else {
			return aOfType.toString();
		}
	}

	private String getOfTypeName() {
		return getOfTypeName(ofType);
	}

	public int n_elem() {
		if (valueElements == null) {
			return 0;
		}
		return valueElements.size();
	}

	public void add(final Base_Type aElement) {
		if (aElement.getClass() != ofType) {
			throw new TtcnError(MessageFormat.format("Adding a {0} type variable to a record of {1}", getOfTypeName(aElement.getClass()), getOfTypeName()));
		}
		if (valueElements == null) {
			valueElements = new ArrayList<Base_Type>();
		}
		valueElements.add(aElement);
	}

}
