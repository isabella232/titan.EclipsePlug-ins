/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.parsers.ttcn3parser;

import org.eclipse.titan.designer.AST.Identifier;

public interface IIdentifierReparser {

	/**
	 * Runs the reparsing process
	 * @return 0 on success
	 *         failure otherwise, where the value indicates the number of tokens we need to read back
	 */
	public int parse();

	/**
	 * Runs the reparsing process
	 * Also runs default code (reparser.setNameChanged(true);) on success
	 * @return 0 on success
	 *         failure otherwise, where the value indicates the number of tokens we need to read back
	 */
	public int parseAndSetNameChanged();

	/**
	 * @return the parsed identifierIdentifierReparser_V2.java
	 */
	public Identifier getIdentifier();

}
