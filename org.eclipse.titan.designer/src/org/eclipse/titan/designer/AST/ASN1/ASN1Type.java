/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.ASN1;

import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.Type;

/**
 * This class represents those types which can be reached/used in ASN.1 .
 *
 * @author Kristof Szabados
 * */
// TODO shouldn't this be an interface?
public abstract class ASN1Type extends Type implements IASN1Type {

	@Override
	/** {@inheritDoc} */
	public abstract IASN1Type newInstance();

	// TODO: remove when location is fixed
	@Override
	/** {@inheritDoc} */
	public Location getLikelyLocation() {
		return getLocation();
	}
}
