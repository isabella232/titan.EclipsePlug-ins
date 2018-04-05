/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST;

import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * @author Kristof Szabados
 * */
public interface IGovernor extends ISetting {

	/**
	 * Does the semantic checking of the governor.
	 * A short version, that does not take a reference chain as parameter.
	 *
	 * Should be used when the check is the first entry point.
	 * And can not already be part of a recursive checking loop.
	 *
	 * @param timestamp the time stamp of the actual semantic check cycle.
	 * */
	void check(final CompilationTimeStamp timestamp);

	/**
	 * Does the semantic checking of the governor.
	 * This version should be used, when there is a chance for the check to recursively loop bask into itself.
	 *
	 * @param timestamp the time stamp of the actual semantic check cycle.
	 * @param referenceChain
	 *                the ReferenceChain used to detect circular references,
	 *                must not be null.
	 * */
	void check(final CompilationTimeStamp timestamp, IReferenceChain refChain);
}