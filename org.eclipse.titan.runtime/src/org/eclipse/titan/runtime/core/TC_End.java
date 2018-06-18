/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

/**
 * This class represents a forced ending of a test behavior.
 * Will be thrown as an exception, to bypass the usual workings.
 *
 * @author Kristof Szabados
 */
public class TC_End extends Error {

	public TC_End() {
		//having an explicit constructor makes it easy to put a debug breakpoint into it.
		super();
	}
}
