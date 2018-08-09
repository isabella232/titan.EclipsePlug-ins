/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

import java.text.MessageFormat;

/**
 * Module_Param_Length_Restriction in titan.core
 *
 * @author Arpad Lovassy
 */
public class Module_Param_Length_Restriction {
	private int min;
	private boolean has_max;
	private int max;

	public Module_Param_Length_Restriction() {
		min = 0;
		has_max = false;
		max = 0;
	}

	public void set_single(final int p_single) {
		has_max = true;
		min = max = p_single;
	}

	public void set_min(final int p_min) {
		min = p_min;
	}

	public void set_max(final int p_max) {
		has_max = true;
		max = p_max;
	}

	public int get_min() {
		return min;
	}

	public boolean get_has_max() {
		return has_max;
	}

	public int get_max() {
		return max;
	}

	public boolean is_single() {
		return has_max && min == max;
	}

	public void log() {
		TtcnLogger.log_event_str(MessageFormat.format(" length({0}", min));
		if (min != max) {
			TtcnLogger.log_event_str("..");
			if (!has_max) {
				TtcnLogger.log_event_str("infinity");
			} else {
				TtcnLogger.log_event_str(MessageFormat.format("{0}", max));
			}
		}
		TtcnLogger.log_event_str(")");
	}

}
