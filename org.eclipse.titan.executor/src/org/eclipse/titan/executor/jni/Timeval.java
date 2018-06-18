/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.executor.jni;

/**
 * Data structure for representing a time value.
 * <p>
 * The original C++ structure can be found at TTCNv3\mctr2\mctr\MainController.h
 *
 * @author Peter Dimitrov
 * */
public final class Timeval {
	// seconds
	public long tv_sec;
	// microSeconds
	public long tv_usec;

	public Timeval(final long tv_sec, final long tv_usec) {
		this.tv_sec = tv_sec;
		this.tv_usec = tv_usec;
	}
}
