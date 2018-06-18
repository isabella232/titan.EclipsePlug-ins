/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.parsers.ttcn3parser;

import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.Value;
import org.eclipse.titan.designer.parsers.Parser_Helper;

/**
 * @author Kristof Szabados
 * */
@Parser_Helper
public class Connection_Helper {
	public Value componentReference1;
	public Reference portReference1;
	public Value componentReference2;
	public Reference portReference2;

	public Connection_Helper(final PortReference_Helper helper1, final PortReference_Helper helper2) {
		this.componentReference1 = helper1.componentReference;
		this.portReference1 = helper1.portReference;
		this.componentReference2 = helper2.componentReference;
		this.portReference2 = helper2.portReference;
	}
}
