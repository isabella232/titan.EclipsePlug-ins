/******************************************************************************
 * Copyright (c) 2000-2017 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

/**
 * TTCN-3 default (base)
 *
 * @author Kristof Szabados
 *
 * FIXME implement destructor.
 */
public class Default_Base {
	private int defaultId;
	private String altstepName;

	// package private constructor to have a null reference
	Default_Base() {
		defaultId = 0;
		altstepName = "<UNBOUND>";
	}

	public Default_Base(final String altstepName) {
		defaultId = TTCN_Default.activate(this);
		this.altstepName = altstepName;
		TtcnLogger.log_defaultop_activate(altstepName, defaultId);
	}

	public int getDefaultId() {
		return defaultId;
	}

	public String getAlstepName() {
		return altstepName;
	}

	// originally call_altstep
	// TODO should be abstract
	public TitanAlt_Status call_altstep() {
		return TitanAlt_Status.ALT_NO;
	}

	public void log() {
		TtcnLogger.log_event("default reference: altstep: %s, id: %u", altstepName, defaultId);
	}
}
