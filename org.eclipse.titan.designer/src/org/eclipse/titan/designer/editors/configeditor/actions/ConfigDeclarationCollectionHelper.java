/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors.configeditor.actions;

import org.eclipse.titan.common.parsers.cfg.CfgLocation;

/**
 * @author Kristof Szabados
 * */
public final class ConfigDeclarationCollectionHelper {
	public String description;
	public CfgLocation location;

	public ConfigDeclarationCollectionHelper(final String description, final CfgLocation location) {
		this.description = description;
		this.location = location;
	}
}
