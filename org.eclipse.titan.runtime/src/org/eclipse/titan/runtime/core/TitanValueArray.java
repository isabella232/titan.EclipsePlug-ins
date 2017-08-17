package org.eclipse.titan.runtime.core;

import java.text.MessageFormat;
import java.util.ArrayList;

public class TitanValueArray<T extends Base_Type> extends Base_Type {

	private ArrayList<T> array_elements;
	
	private int array_size;
	private int indexOfset;
	
	public TitanValueArray() {
		
	}
	
	public void setSize(final int length) {
		array_size = length;
	}
	
	public void setOfset(final int ofset) {
		indexOfset = ofset;
	}
	
	
	@Override
	public TitanBoolean isPresent() {
		return isBound();
	}

	@Override
	public TitanBoolean isBound() {
		for (int i = 0; i < array_elements.size(); ++i) {
			if (!array_elements.get(i).isBound().getValue()) {
				return new TitanBoolean(false);
			}
		}
		return new TitanBoolean(true);
	}

	@Override
	public TitanValueArray<T> assign(Base_Type otherValue) {
		if (otherValue instanceof TitanValueArray<?>) {
			return assign((TitanValueArray<T>) otherValue);
		} 
		
		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to array value", otherValue));
	}
	
	public TitanValueArray<T> assign(TitanValueArray<T> otherValue) {
		//FIXME: implement
		return null;
	}
	
	@Override
	public TitanBoolean operatorEquals(Base_Type otherValue) {
		if (otherValue instanceof TitanValueArray<?>) {
			return operatorEquals((TitanValueArray<T>) otherValue);
		} 
		
		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to array value", otherValue));
	}

	public TitanBoolean operatorEquals(TitanValueArray<T> otherValue) {
		//FIXME: implement
		return null;
	}

}
