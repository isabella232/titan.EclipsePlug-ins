/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.definitions;

/**
 * This interface represents a parameterized assignment. One that has formal
 * parameters
 *
 * @author Kristof Szabados
 * */
public interface IParameterisedAssignment {

	/**
	 * @return The list of formal parameters the assignment has
	 * */
	FormalParameterList getFormalParameterList();
}
