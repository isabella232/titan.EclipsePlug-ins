/******************************************************************************
 * Copyright (c) 2000-2019 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.parsers.ttcn3parser;

import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.TTCN3.statements.Parameter_Redirection;
import org.eclipse.titan.designer.AST.TTCN3.statements.Value_Redirection;
import org.eclipse.titan.designer.parsers.Parser_Helper;

/**
 * @author Kristof Szabados
 * */
@Parser_Helper
public class Redirection_Helper {
	public Value_Redirection redirectValue;
	public Parameter_Redirection redirectionParameters;
	public Reference senderReference;
	public Reference indexReference;
	public Reference timestampReference;

	public Redirection_Helper(final Value_Redirection redirectValue, final Parameter_Redirection redirectionParameters, final Reference senderReference,
			final Reference indexReference, final Reference timestampReference) {
		this.redirectValue = redirectValue;
		this.redirectionParameters = redirectionParameters;
		this.senderReference = senderReference;
		this.indexReference = indexReference;
		this.timestampReference = timestampReference;
	}
}
