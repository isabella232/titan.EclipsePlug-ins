/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.parsers.ttcn3parser;

import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.TTCN3.statements.Value_Redirection;
import org.eclipse.titan.designer.parsers.Parser_Helper;

/**
 * @author Kristof Szabados
 * */
@Parser_Helper
public class PortRedirect_Helper {
	public Value_Redirection redirectValue;
	public Reference redirectSender;
	public Reference redirectIndex;
	public Reference redirectTimestamp;

	public PortRedirect_Helper(final Value_Redirection redirectValue, final Reference redirectSender, final Reference redirectIndex, final Reference redirectTimestamp) {
		this.redirectValue = redirectValue;
		this.redirectSender = redirectSender;
		this.redirectIndex = redirectIndex;
		this.redirectTimestamp = redirectTimestamp;
	}
}
