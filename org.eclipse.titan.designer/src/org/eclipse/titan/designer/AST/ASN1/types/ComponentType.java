/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.ASN1.types;

import org.eclipse.titan.designer.AST.ILocateableNode;
import org.eclipse.titan.designer.AST.Location;

/**
 * ComponentType (abstract class).
 * <p>
 * Originally CT in TITAN
 *
 * @author Kristof Szabados
 */
public abstract class ComponentType extends ExtensionAddition implements ILocateableNode {
	/**
	 * The location of the whole componentType. This location encloses it
	 * fully, as it is used to report errors to.
	 **/
	protected Location location;

	@Override
	/** {@inheritDoc} */
	public final void setLocation(final Location location) {
		this.location = location;
	}

	@Override
	/** {@inheritDoc} */
	public final Location getLocation() {
		return location;
	}
}
