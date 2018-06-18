/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.attributes;

import org.eclipse.titan.designer.AST.Identifier;

/**
 * Represents a version requirement on an imported module
 *
 * @author Csaba Raduly
 */
public final class VersionRequirementAttribute extends ModuleVersionAttribute {
	private final Identifier requiredModule;

	public VersionRequirementAttribute(final Identifier modid, final Identifier version) {
		super(version, false);
		requiredModule = modid;
	}

	public Identifier getRequiredModule() {
		return requiredModule;
	}

	@Override
	/** {@inheritDoc} */
	public ExtensionAttribute_type getAttributeType() {
		return ExtensionAttribute_type.REQUIRES;
	}
}
