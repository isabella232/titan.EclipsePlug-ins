/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors;

/**
 * Helper class, used by the matching brackets functionality.
 *
 * @author Kristof Szabados
 */
public final class Pair {
	public final char start;
	public final char end;

	public Pair(final char start, final char end) {
		this.start = start;
		this.end = end;
	}
}
