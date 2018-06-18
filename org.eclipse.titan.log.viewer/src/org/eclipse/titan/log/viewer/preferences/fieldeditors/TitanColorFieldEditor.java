/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.preferences.fieldeditors;

import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.swt.widgets.Composite;

/**
 * TitanColorFieldEditor
 *
 */
public class TitanColorFieldEditor extends ColorFieldEditor {

	/**
	 * Constructor
	 *
	 * @param name The name of the editor
	 * @param labelText The label text
	 * @param parent The composite parent
	 */
	public TitanColorFieldEditor(final String name, final String labelText, final Composite parent) {
		super(name, labelText, parent);
	}

	@Override
	public int getNumberOfControls() {
		return 1;
	}
}
