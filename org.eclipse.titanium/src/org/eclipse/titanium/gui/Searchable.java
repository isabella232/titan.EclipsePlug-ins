/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titanium.gui;

import java.util.Collection;

/**
 * This interface should be used for GUI elements that have searchable elements
 *
 * @author Gabor Jenei
 */
@SuppressWarnings("rawtypes")
public interface Searchable<T extends Comparable> {

	/**
	 * This method sets totally new results on the GUI object
	 * @param results : The results to set
	 */
	public void setResults(Collection<T> results);

	/**
	 * This method deletes all the formerly set results on the object
	 */
	public void clearResults();

	/**
	 * This method adds new results, but doesn't deletes the old ones
	 * @param results : The results to add
	 */
	public void addResults(Collection<T> results);

	/**
	 * This method implements the action to be run if an
	 * element in the result list is chosen
	 * @param element
	 */
	public void elemChosen(T element);

}
