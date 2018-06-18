/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titanium.metrics.view;

import org.eclipse.titan.designer.AST.Location;

/**
 * A node that corresponds to an entity in the TTCN3 code, so it can be opened
 * in an eclipse editor.
 *
 * @author poroszd
 *
 */
interface IOpenable {
	/**
	 * @return The Location where its code starts.
	 */
	Location getLocation();
}
