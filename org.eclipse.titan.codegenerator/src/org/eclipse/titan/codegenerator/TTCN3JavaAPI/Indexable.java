/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *
 *   Meszaros, Mate Robert
 *
 ******************************************************************************/

package org.eclipse.titan.codegenerator.TTCN3JavaAPI;

/**
 * This interface imposes an indexability of the object that implements it.
 * @param <T> the type of objects that can be retrieved from and set to
 */
public interface Indexable<T> {
	/**
	 * Returns with the value at the specified index of this indexable object.
	 * @param index the position to be queried
	 * @return the value at the specified position
	 */
	T get(int index);

	/**
	 * Replaces the value at the specified index with the given value
	 * @param index the position to be replaced
	 * @param t the value to be set to the given position of this indexable object
	 */
	void set(int index, T t);
}
