/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.common.parsers.cfg;

import org.eclipse.titan.common.parsers.Interval;

/**
 * @author eferkov
 * @author Arpad Lovassy
 */
public final class CfgInterval extends Interval {
	public enum section_type {
		LOGGING,
		EXECUTE,
		TESTPORT_PARAMETERS,
		MODULE_PARAMETERS,
		MAIN_CONTROLLER,
		EXTERNAL_COMMANDS,
		GROUPS,
		COMPONENTS,
		INCLUDE,
		ORDERED_INCLUDE,
		DEFINE,
		PROFILER,
		// Error indicator.
		UNKNOWN
	}

	private final section_type sectionType;

	public CfgInterval(final Interval parent, final interval_type type, final section_type sectionType) {
		super(parent, type);
		this.sectionType = sectionType;
	}

	/**
	 * Returns the section type for the interval.
	 */
	public section_type getSectionType(){
		return sectionType;
	}
}
