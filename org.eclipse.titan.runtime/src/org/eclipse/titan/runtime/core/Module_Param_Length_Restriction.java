/******************************************************************************
 * Copyright (c) 2000-2017 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

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
		//TODO: implement
	}

}
