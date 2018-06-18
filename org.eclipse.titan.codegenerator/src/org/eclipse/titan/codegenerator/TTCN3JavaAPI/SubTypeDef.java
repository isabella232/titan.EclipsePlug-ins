/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *   
 *   Keremi, Andras
 *   Eros, Levente
 *   Kovacs, Gabor
 *
 ******************************************************************************/

package org.eclipse.titan.codegenerator.TTCN3JavaAPI;

import java.util.ArrayList;
import java.util.List;

public abstract class SubTypeDef<T extends PrimitiveTypeDef> extends PrimitiveTypeDef{
    protected List<T> allowedValues; //allowed values of subtype
    protected List<SubTypeInterval<T>> allowedIntervals; //allowed intervals of subtype

    public T value;

    public SubTypeDef() {
        allowedValues = new ArrayList<T>();
        allowedIntervals = new ArrayList<SubTypeInterval<T>>();
    }

    public SubTypeDef(T val) {
    	this();
    	value = val;
    }

    public void checkValue() throws IndexOutOfBoundsException {
        if (allowedValues.size() == 0 && allowedIntervals.size() == 0)
            return;
        for (SubTypeInterval<T> i : allowedIntervals)
            if(i.checkValue(value)) return;
        for (T i : allowedValues)
            if (i.equals(value)) return;
        throw new IndexOutOfBoundsException("out of intervals!");
    }

    public String toString(){
    	return toString("");
    }
    
    public String toString(String tabs){
    	return value.toString();
    }

}