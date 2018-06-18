/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.attributes;

/**
 * Represents a provider port type attribute. This has to be used on the port
 * type with external interface in a dual-faced port setup.
 *
 * @author Kristof Szabados
 * */
public final class ProviderPortTypeAttribute extends PortTypeAttribute {

	@Override
	/** {@inheritDoc} */
	public PortType_type getPortTypeType() {
		return PortType_type.PROVIDER;
	}
}
