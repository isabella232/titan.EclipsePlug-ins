/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.exceptions;

/**
 * Base class for technical exceptions
 *
 */
public class TechnicalException extends TitanLogException {

	private static final long serialVersionUID = 1262182590782561633L;

	/**
	 * Constructor for throwable
	 * @param t the throwable
	 */
	public TechnicalException(final Throwable t) {
		super(t);
	}

	/**
	 * Constructor for messages
	 * @param msg the message
	 */
	public TechnicalException(final String msg) {
		super(msg);
	}

	public TechnicalException(final String message, final Throwable cause) {
		super(message, cause);
	}
}
