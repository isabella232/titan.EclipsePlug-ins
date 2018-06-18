/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST;


/**
 * Things that have a governor. Object, Value...
 *
 * @author Kristof Szabados
 */
public abstract class Governed extends Setting implements IGoverned {

	@Override
	/** {@inheritDoc} */
	public abstract IGovernor getMyGovernor();
}
