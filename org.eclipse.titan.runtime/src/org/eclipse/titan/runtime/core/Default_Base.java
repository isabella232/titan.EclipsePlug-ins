package org.eclipse.titan.runtime.core;

/**
 * TTCN-3 default (base)
 *
 * @author Kristof Szabados
 * 
 * FIXME implement rest
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
		//TODO log
	}

	// originally call_altstep
	// TODO should be abstract
	public TitanAlt_Status call_altstep() {
		return TitanAlt_Status.ALT_NO;
	}
}
