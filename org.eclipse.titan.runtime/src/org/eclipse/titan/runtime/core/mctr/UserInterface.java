/******************************************************************************
 * Copyright (c) 2000-2020 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core.mctr;

/**
 * The user interface singleton interface class.
 */
public abstract class UserInterface {

	/**
	 * Constructs the UserInterface.
	 */
	public UserInterface() {

	}

	/**
	 * Initialize the user interface.
	 */
	public abstract void initialize();

	/**
	 * Enters the main loop.
	 */
	public abstract int enterLoop(final String[] args);

	/**
	 * Status of MC has changed.
	 */
	public abstract void status_change();

	/**
	 * Error message from MC.
	 */
	public abstract void error(final int severity, final String message);

	/**
	 * General notification from MC.
	 * timestamp is in miliseconds.
	 */
	public abstract void notify(final long timestamp, final String source, final int severity, final String message);

	public abstract void executeBatchFile(final String filename);
}