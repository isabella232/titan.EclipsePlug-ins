/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST;

/**
 * @author Adam Delic
 * */
public class ASTVisitor {
	public static final int V_SKIP = 1;
	public static final int V_ABORT = 2;
	public static final int V_CONTINUE = 3;

	public int visit(final IVisitableNode node) {
		return V_CONTINUE;
	}

	public int leave(final IVisitableNode node) {
		return V_CONTINUE;
	}
}
