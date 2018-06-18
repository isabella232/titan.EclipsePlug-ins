/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors.configeditor;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.forms.FormColors;

/**
 * @author Kristof Szabados
 * */
public final class FormColorCache {

	private static FormColors formColors;

	private FormColorCache() {
		// Hide constructor
	}

	/**
	 * Returns the form colors class used to set up the colors in the
	 * designer plug-in on the Form pages.
	 *
	 * @param display
	 *                the display for which the colors should be common.
	 * @return the common formColors instance
	 * */
	public static synchronized FormColors getFormColors(final Display display) {
		if (formColors == null) {
			formColors = new FormColors(display);
			formColors.markShared();
		}
		return formColors;
	}
}
