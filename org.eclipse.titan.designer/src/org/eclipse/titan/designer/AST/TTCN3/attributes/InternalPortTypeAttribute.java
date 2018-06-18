/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.attributes;

/**
 * Represents the internal attribute, that can be assigned to a port type. If
 * set, the code for the port type will be automatically generated. But than the
 * port can only be used for internal communication (between TTCN-3 components)
 *
 * @author Kristof Szabados
 * */
public final class InternalPortTypeAttribute extends PortTypeAttribute {

	@Override
	/** {@inheritDoc} */
	public PortType_type getPortTypeType() {
		return PortType_type.INTERNAL;
	}
}
