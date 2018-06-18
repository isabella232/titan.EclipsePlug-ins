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
 *   Meszaros, Mate Robert
 *
 ******************************************************************************/

package org.eclipse.titan.codegenerator.TTCN3JavaAPI;

public class SubTypeInterval<T> {
    T lowerbound;
    T upperbound;

    public SubTypeInterval(T lower, T upper) {
        lowerbound=lower;
        upperbound=upper;
    }

    boolean checkValue(T value) {
    	if(lowerbound instanceof Relational<?>)
    		return ((Relational<T>)lowerbound).isLessOrEqualThan(value).getValue()&&((Relational<T>)upperbound).isGreaterOrEqualThan(value).getValue();
    	else throw new IndexOutOfBoundsException("bound is not relational");
    }
}
