package org.eclipse.titan.runtime.core;

/**
 * Enumeration indicating the status of an altstep branch.
 * 
 * originally alt_status in Types.h
 * 
 * @author Kristof Szabados
 */
public enum TitanAlt_Status {
	// TODO we might not need the ALT prefix in Java
	// TODO check if we use unchecked and maybe correctly

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
