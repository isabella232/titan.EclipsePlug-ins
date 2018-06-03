/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.parsers.ttcn3parser;

import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.TTCN3.statements.Parameter_Redirect;
import org.eclipse.titan.designer.parsers.Parser_Helper;

/**
 * @author Kristof Szabados
 * */
@Parser_Helper
public class Redirection_Helper {
	public Reference redirectValue;
	public Parameter_Redirect redirectParameters;
	public Reference senderReference;
	public Reference indexReference;

	public Redirection_Helper(final Reference redirectValue, final Parameter_Redirect redirectParameters, final Reference senderReference, final Reference indexReference) {
		this.redirectValue = redirectValue;
		this.redirectParameters = redirectParameters;
		this.senderReference = senderReference;
		this.indexReference = indexReference;
	}
}
