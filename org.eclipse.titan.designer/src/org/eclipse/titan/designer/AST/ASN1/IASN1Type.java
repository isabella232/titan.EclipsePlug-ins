/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.ASN1;

import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.Location;

/**
 * @author Kristof Szabados
 * */
public interface IASN1Type extends IType {

	/** @return a new instance of this ASN.1 type */
	IASN1Type newInstance();

	// TODO: remove when location is fixed
	Location getLikelyLocation();
}
