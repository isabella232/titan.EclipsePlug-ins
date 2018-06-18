/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titanium.graph.visualization;

/**
 * This exception is thrown in case of layout problems (not existing layout, the
 * provided graph is <code>null</code> or the graph contains a circle (this is
 * not acceptable for DAG layout))
 *
 * @author Gabor Jenei
 *
 */
public class BadLayoutException extends Exception {
	private static final long serialVersionUID = -869670176586448172L;
	private final ErrorType type;

	/**
	 * Constructor
	 * @param str : Error message
	 * @param t : The type of error {@see ErrorType}
	 */
	public BadLayoutException(final String str, final ErrorType t) {
		super(str);
		type = t;
	}

	/**
	 * Constructor
	 * @param str : Error message
	 * @param t : The type of error {@see ErrorType}
	 * @param cause : The cause of the error (a Throwable object)
	 */
	public BadLayoutException(final String str, final ErrorType t, final Throwable cause) {
		super(str, cause);
		type = t;
	}

	/**
	 * @return The type attribute {@see ErrorType}
	 */
	public ErrorType getType() {
		return type;
	}

}