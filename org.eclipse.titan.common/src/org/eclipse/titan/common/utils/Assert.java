/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.common.utils;

public final class Assert {

	private Assert() {
		// Hide constructor
	}

	/**
	 * Assert that an object is not null.
	 * @param object the object to check
	 * @param message the exception message
	 * @throws java.lang.IllegalArgumentException if the object is {@code null}
	 */
	public static void notNull(final Object object, final String message) {
		if (object == null) {
			throw new IllegalArgumentException(message);
		}
	}
}
