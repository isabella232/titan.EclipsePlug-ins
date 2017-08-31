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

/**
 * @author Farkas Izabella Ingrid
 */
public class TitanValueArray<T extends Base_Type> extends Base_Type {

	ArrayList<T> array_elements;

	public Class<T> clazz;

	int array_size;
	int indexOffset;

	public TitanValueArray(final Class<T> clazz) {
		this.clazz = clazz;
		array_elements = new ArrayList<T>();
	}
	
	public TitanValueArray(final TitanValueArray<T> otherValue) {
		clazz = otherValue.clazz; 
		array_elements = new ArrayList<T>();
		array_size = otherValue.array_size;
		indexOffset = otherValue.indexOffset;
		
		for (int i = 0; i < array_size ; ++i) {
			try {
				final T helper = clazz.newInstance();
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
	
	//FIXME: implement
	public void setSize(final int length) {
		for (int i = array_size; i < length; ++i) {
			try {
				final T emply = clazz.newInstance();
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


	@Override
	public TitanBoolean isPresent() {
		return isBound();
	}

	@Override
	public TitanBoolean isBound() {
		for (int i = 0; i < array_size; ++i) {
			if (array_elements.get(i).isBound().getValue()) {
				return new TitanBoolean(true);
			}
		}
		
		return new TitanBoolean(false);
	}

	//FIXME: originally array_elements.get(i).clean_up()
	public void cleanUp() {
		array_elements.clear();
		array_elements = null;
	}

	public TitanBoolean isValue() {
		for (int i = 0; i < array_size; ++i) {
			if (!array_elements.get(i).isValue().getValue()) {
				return new TitanBoolean(false);
			}
		}

		return new TitanBoolean(true);
	}

	public TitanInteger lengthOf() {
		for (int i = array_size-1; i >= 0; --i) {
			if (array_elements.get(i).isBound().getValue()) {
				return new TitanInteger(i+1);
			}
		}

		return new TitanInteger(0);
	}

	// TODO: void set_param(Module_Param& param);

	// originally not implemented operator=
	@Override
	public TitanValueArray<T> assign(final Base_Type otherValue) {
		if (otherValue instanceof TitanValueArray<?>) {
			final TitanValueArray<?> arrayOther = (TitanValueArray<?>)otherValue;
			return assign(arrayOther);
		} else {
			try {
				array_elements = new ArrayList<T>();
				final T value = clazz.newInstance();
				value.assign(otherValue);
				array_elements.add(value);
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return this;
		}
		//	throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be assign to array value", otherValue));
	}
	
	public TitanValueArray<T> assign(final TitanValueArray<T> otherValue) {
		cleanUp();
		array_size = otherValue.array_size;
		array_elements = new ArrayList<T>(array_size);
		indexOffset = otherValue.indexOffset;
		for (int i = 0; i < otherValue.array_size; ++i) {
			array_elements.add(otherValue.array_element(i));
		}
		return this;
	}

	@Override
	public TitanBoolean operatorEquals(final Base_Type otherValue) {
		if (otherValue instanceof TitanValueArray<?>) {
			final TitanValueArray<?> arrayOther = (TitanValueArray<?>)otherValue;
			return operatorEquals(arrayOther);
		} else {
			if(array_size == 1 ) {
				return array_elements.get(0).operatorEquals(otherValue);
			}
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to array value", otherValue));
	}

	public TitanBoolean operatorEquals(final TitanValueArray<T> otherValue) {
		if (array_size != otherValue.array_size) {
			return new TitanBoolean(false);
		}

		for (int i = 0; i < array_size; ++i) {
			if (! array_elements.get(getArrayIndex(i, array_size, indexOffset)).operatorEquals(otherValue.array_elements.get(getArrayIndex(i, otherValue.array_size, otherValue.indexOffset))).getValue()) {
				return new TitanBoolean(false);
			}
		}

		return new TitanBoolean(true);
	}

	public TitanBoolean operatorNotEquals(final Base_Type otherValue) {
		return operatorEquals(otherValue).not();
	}

	public TitanBoolean operatorNotEquals(final TitanValueArray<T> otherValue) {
		return operatorEquals(otherValue).not();
	}

	//originally  operator<<=
	public TitanValueArray<T> rotateLeft(int rotateCount) {
		//new TitanValueArray<T>((TitanValueArray<T>).getClass());
		if (array_size == 0) return this;
		if (rotateCount >= 0) {
			rotateCount = rotateCount % array_size;
			if (rotateCount == 0) {
				return this;
			}

			final TitanValueArray<T> result = new TitanValueArray<T>(clazz);
			result.array_size = array_size;
			result.indexOffset = indexOffset;
			if (rotateCount > array_size) {
				rotateCount = array_size;
			}
			for (int i = 0; i < array_size - rotateCount; i++) {
				result.array_elements.add(i, array_elements.get(i+rotateCount));
			}
			for (int i =array_size - rotateCount; i < array_size; i++) {
				result.array_elements.add(i, array_elements.get(i+rotateCount - array_size));
			}
			return result;
		} else {
			return rotateLeft(-rotateCount);
		}
	}

	//originally  operator<<=
	public TitanValueArray<T> rotateLeft(final TitanInteger rotateCount) {
		rotateCount.mustBound("Unbound integer operand of rotate left operator.");

		return rotateLeft(rotateCount.getInt());
	}

	//originally  operator>>=
	public TitanValueArray<T> rotateRight(int rotateCount) {
		if (array_size == 0) return this;
		if (rotateCount >= 0) {
			rotateCount = rotateCount % array_size;
			if (rotateCount == 0) {
				return this;
			}

			final TitanValueArray<T> result = new TitanValueArray<T>(clazz);
			result.array_size = array_size;
			result.indexOffset = indexOffset;
			if (rotateCount > array_size) {
				rotateCount = array_size;
			}
			for (int i = 0; i < rotateCount; i++) {
				result.array_elements.add(i, array_elements.get(i-rotateCount+array_size));
			}
			for (int i = rotateCount; i < array_size; i++) {
				result.array_elements.add(i, array_elements.get(i-rotateCount));
			}
			return result;
		} else {
			return rotateLeft(-rotateCount);
		}
	}

	//originally  operator>>=
	public TitanValueArray<T> rotateRight(final TitanInteger rotateCount) {
		rotateCount.mustBound("Unbound integer operand of rotate right operator.");

		return rotateRight(rotateCount.getInt());
	}

	// originally T& operator[](int)
	public T getAt(final int index) {
		return array_elements.get(getArrayIndex(index, array_size, indexOffset));
	}

	//originally T& operator[](const INTEGER)
	public T getAt(final TitanInteger index) {
		return array_elements.get(getArrayIndex(index, array_size, indexOffset));
	}
	//const originally T& operator[](int)
	public T constGetAt(final int index) {
		return array_elements.get(getArrayIndex(index, array_size, indexOffset));
	}

	// const // originally T& operator[](const INTEGER)
	public T constGetAt(final TitanInteger index) {
		return array_elements.get(getArrayIndex(index, array_size, indexOffset));
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

	//TODO: void set_implicit_omit()

	// originally n_elem()
	public int n_elem() {
		return array_size;
	}

	// originally size_of()
	public TitanInteger sizeOf() {
		return new TitanInteger(array_size);
	}

	//TODO: void set_param(Module_Param)
	//TODO: set_param

	@Override
	public String toString() {
		final StringBuilder str = new StringBuilder("{");
		for (int i = 0; i < array_size-1; ++i) {
			str.append(array_elements.get(i).toString());
			str.append(" , ");
		}
		if (array_size > 0 ) {
			str.append(array_elements.get(array_size-1).toString());
		}
		str.append('}');
		return str.toString();
	}
	
	//static method

	public static int getArrayIndex(final int index, final int arraySize, final int indexofset) {
		if (arraySize < 0) {
			throw new TtcnError("Invalid array size");
		}
		if (index < indexofset) {
			throw new TtcnError("Index underflow when accessing an element of an array. "+
					"The index value should be between "+indexofset+" and "+(indexofset+arraySize-1)+" instead of "+index+".");
		}

		final int result = index - indexofset;
		if (result >= arraySize) {
			throw new TtcnError("Index underflow when accessing an element of an array. "+
					"The index value should be between "+indexofset+" and "+(indexofset+arraySize-1)+" instead of "+index+".");
		}

		return result;
	}

	public static int getArrayIndex(final TitanInteger index, final int arraySize, final int indexofset) {
		if (! index.isBound().getValue()) {
			throw new TtcnError("Accessing an element of an array using an unbound index.");
		}

		return getArrayIndex(index.getInt(), arraySize, indexofset);
	}
}
