/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.common.fieldeditors;

import org.eclipse.swt.widgets.Composite;

/**
 * @author Kristof Szabados
 * */
public final class TITANResourceLocator extends TITANResourceLocatorFieldEditor {

	public TITANResourceLocator(final String labelText, final Composite parent, final int type, final String rootPath) {
		super("dummy", labelText, parent, type, rootPath);
	}

	@Override
	protected void doLoad() {
		// Do nothing
	}

	@Override
	protected void doLoadDefault() {
		// Do nothing
	}

	@Override
	protected void doStore() {
		// Do nothing
	}

}
