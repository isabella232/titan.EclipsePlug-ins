/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST;

/**
 * This interface tells that the node has a location.
 *
 * @author Kristof Szabados
 * */
public interface ILocateableNode {
	/**
	 * Sets the location of the node.
	 *
	 * @param location the location to be set
	 * */
	void setLocation(Location location);

	/** @return the location of the node */
	Location getLocation();
}
