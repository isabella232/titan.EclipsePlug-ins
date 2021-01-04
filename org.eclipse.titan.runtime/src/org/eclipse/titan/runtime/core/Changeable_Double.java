/******************************************************************************
 * Copyright (c) 2000-2021 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

/**
 * Helper class for doubles,
 *  for situations when we need to be update a boolean value inside functions.
 *
 * @author Kristof Szabados
 * */
final class Changeable_Double {
	private double value;

	/**
	 * Initializes to the provided value.
	 *
	 * @param value the value to use.
	 * */
	public Changeable_Double(final double value) {
		this.value = value;
	}

	/**
	 * @return the current value.
	 * */
	public double getValue() {
		return value;
	}

	/**
	 * Sets the new value.
	 *
	 * @param value the new value to set.
	 * */
	public void setValue(final double value) {
		this.value = value;
	}
}
