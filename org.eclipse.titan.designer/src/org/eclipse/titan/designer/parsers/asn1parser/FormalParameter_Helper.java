/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.parsers.asn1parser;

import org.antlr.v4.runtime.Token;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.parsers.Parser_Helper;

/**
 * Data structure for ASN1Parser/pr_FormalParameter
 * @author Kristof Szabados
 * @author Arpad Lovassy
 */
@Parser_Helper
public class FormalParameter_Helper {
	public Token governorToken;
	public Token formalParameterToken;
	public Identifier identifier;
}
