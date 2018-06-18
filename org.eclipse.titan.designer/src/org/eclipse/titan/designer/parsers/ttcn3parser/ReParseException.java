/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.parsers.ttcn3parser;

/**
 * @author Kristof Szabados
 * */
public class ReParseException extends Exception {

	/**
	 * All serializable objects should have a stable serialVersionUID
	 */
	private static final long serialVersionUID = 1L;

	private int depth;

	public ReParseException() {
		depth = 1;
	}

	public ReParseException (final int depth) {
		this.depth = depth;
	}

	public int getDepth() {
		return depth;
	}

	public void decreaseDepth() {
		depth--;
	}

	public void increaseDepth() {
		depth++;
	}
}
