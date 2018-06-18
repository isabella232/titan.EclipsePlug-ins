/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.common.preferences;

import org.eclipse.titan.common.Activator;

public final class PreferenceConstants {

	public static final String LOG_MERGE_OPTIONS = Activator.PLUGIN_ID + ".automaticMergeOptions";
	public static final String LOG_MERGE_OPTIONS_OVERWRITE = "overwrite";
	public static final String LOG_MERGE_OPTIONS_CREATE = "create";
	public static final String LOG_MERGE_OPTIONS_ASK = "ask";

	/** private constructor to disable instantiation */
	private PreferenceConstants() {
	}
}
