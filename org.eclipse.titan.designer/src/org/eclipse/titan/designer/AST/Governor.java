/******************************************************************************
 * Copyright (c) 2000-2017 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST;

import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * Things that can be the governor of something.
 * 
 * @author Kristof Szabados
 */
public abstract class Governor extends Setting implements IGovernor {

	@Override
	/** {@inheritDoc} */
	public void check(final CompilationTimeStamp timestamp) {
	}		
	
	@Override
	/** {@inheritDoc} */
	public void check(final CompilationTimeStamp timestamp, final IReferenceChain refChain) {
	}
}
