/******************************************************************************
 * Copyright (c) 2000-2017 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

/**
 * An experimental base class for a module.
 * 
 * TODO: lots to implement
 * 
 * @author Kristof Szabados
 */
public class TTCN_Module {
	//originally module_type_enum
	public static enum moduleTypeEnum {TTCN3_MODULE, ASN1_MODULE};

	private final moduleTypeEnum moduleType;
	public final String name;

	public TTCN_Module(final String name, final moduleTypeEnum moduleType) {
		this.name = name;
		this.moduleType = moduleType;
	}

	public boolean set_module_param() {
		return false;
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
