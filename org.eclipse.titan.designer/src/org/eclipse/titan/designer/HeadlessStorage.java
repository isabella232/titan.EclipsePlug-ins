/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer;

/**
 * This is a helper class, where the headless mode can reach values otherwise reachable only from the user interface.
 * 
 * @author Kristof Szabados
 * */
public final class HeadlessStorage {
	// the width of a tab character in the TTCN-3 editor, in spaces.
	private static int ttcn3EditorTabWidth = 4;

	private HeadlessStorage() {
		// Hide constructor
	}

	public static int getTabWidth() {
		return ttcn3EditorTabWidth;
	}

	public static void setTabWidth(final int tabWidth) {
		ttcn3EditorTabWidth = tabWidth;
	}
}
