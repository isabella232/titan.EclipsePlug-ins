/******************************************************************************
 * Copyright (c) 2000-2021 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.common.parsers;

/**
 * Simple printer, that prints on the standard output stream
 * @author Arpad Lovassy
 */
public class ConsolePrinter implements IPrinter {

	@Override
	public void print(final String aMsg) {
		System.out.print( aMsg );
	}

	@Override
	public void println() {
		System.out.println();
	}

	@Override
	public void println(final String aMsg) {
		System.out.println( aMsg );
	}
}
