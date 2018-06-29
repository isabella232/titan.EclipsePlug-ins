/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

/**
 * Enumeration indicating the status of an altstep branch.
 *
 * originally alt_status in Types.h
 *
 * @author Kristof Szabados
 */
public enum TitanAlt_Status {

	// the branch status was not yet checked
	ALT_UNCHECKED,
	// the branch was selected/guard returned true
	ALT_YES,
	// the branch is not yet selectable, but could be
	// for example a timer has not yet timedout
	ALT_MAYBE,
	// the branch can not be choosen
	ALT_NO,
	// a repeat statement was encountered, the guards have to be evaluated again
	ALT_REPEAT,
	// a break was encountered, the alt/altstep must be exited
	ALT_BREAK;
}
