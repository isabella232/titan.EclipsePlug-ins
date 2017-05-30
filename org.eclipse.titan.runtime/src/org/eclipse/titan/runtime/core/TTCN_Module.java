package org.eclipse.titan.runtime.core;

/**
 * An experimental base class for a module.
 * 
 * TODO: lots to implement
 * 
 * @author Kristof Szabados
 */
public class TTCN_Module {
	public final String name;

	public TTCN_Module(final String name) {
		this.name = name;
	}

	public void pre_init_module() {
		//intentionally left empty
	}

	public void post_init_module() {
		//intentionally left empty
	}

	public boolean init_comp_type(final String component_type, final boolean init_base_comps) {
		return false;
	}

	public void control() {
		//intentionally left empty
	}
}
