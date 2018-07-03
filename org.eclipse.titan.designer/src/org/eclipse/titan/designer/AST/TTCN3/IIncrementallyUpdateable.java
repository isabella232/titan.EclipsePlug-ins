/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3;

import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * The base interface for classes that can be re-parsed incrementally.
 *
 * ASN.1 values do not support incremental re-parsing, they throw ReParseException.
 *
 * @author Kristof Szabados
 * */
public interface IIncrementallyUpdateable {

	/**
	 * Handles the incremental parsing of this value.
	 *
	 * @param reparser
	 *                the parser doing the incremental parsing.
	 * @param isDamaged
	 *                true if the location contains the damaged area, false
	 *                if only its' location needs to be updated.
	 * @throws ReParseException
	 *                 when the mechanism was not able to handle the
	 *                 re-parsing need at a node.
	 * */
	void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException;
}
