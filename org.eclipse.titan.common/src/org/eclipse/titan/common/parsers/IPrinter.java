/******************************************************************************
 * Copyright (c) 2000-2019 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.common.parsers;

/**
 * Used by {@link ParserLogger} for logging to different consoles
 * @author Arpad Lovassy
 */
public interface IPrinter {

	void print( final String aMsg );

	void println();

	void println( final String aMsg );
}
