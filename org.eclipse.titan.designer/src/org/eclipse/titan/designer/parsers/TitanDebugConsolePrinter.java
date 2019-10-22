/******************************************************************************
 * Copyright (c) 2000-2019 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.parsers;

import org.eclipse.titan.common.parsers.IPrinter;
import org.eclipse.titan.common.parsers.ParserLogger;
import org.eclipse.titan.designer.consoles.TITANDebugConsole;

/**
 * SINGLETON
 * Printer wrapper to use {@link TITANDebugConsole} print methods from {@link ParserLogger}
 * <br>
 * http://stackoverflow.com/questions/70689/what-is-an-efficient-way-to-implement-a-singleton-pattern-in-java
 * <br>
 * Quote from Effective Java: "a single-element enum type is the best way to implement a singleton."
 * @author Arpad Lovassy
 */
public enum TitanDebugConsolePrinter implements IPrinter {

	INSTANCE;

	@Override
	public void print(final String aMsg) {
		TITANDebugConsole.print( aMsg );
	}

	@Override
	public void println() {
		TITANDebugConsole.println();
	}

	@Override
	public void println(final String aMsg) {
		TITANDebugConsole.println( aMsg );
	}
}
