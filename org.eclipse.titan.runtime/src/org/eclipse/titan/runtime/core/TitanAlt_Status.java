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
	ALT_UNCHECKED, ALT_YES, ALT_MAYBE, ALT_NO, ALT_REPEAT, ALT_BREAK;
}
