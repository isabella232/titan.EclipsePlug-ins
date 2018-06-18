/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.common.utils;

import java.util.Arrays;


/**
 * This class consists of {@code static} utility methods for operating
 * on objects. It is intended to use while Java 6 needs to be supported.
 *
 * With Java 7 it can be replaced by {@link java.util.Objects}
 *
 */
public final class ObjectUtils {

	private ObjectUtils() {
		// Hide constructor
	}

	/**
	 * @see java.util.Objects#equals(Object, Object)
	 */
	public static boolean equals(final Object o1, final Object o2) {
		return (o1 == o2) || (o1 != null && o1.equals(o2));
	}

	/**
	 * @see java.util.Objects#hash(Object...)
	 */
	public static int hash(final Object... objects) {
		return Arrays.hashCode(objects);
	}

	/**
	 * @see java.util.Objects#hashCode(Object)
	 */
	public static int hashCode(final Object o) {
		return o != null ? o.hashCode() : 0;
	}
}
