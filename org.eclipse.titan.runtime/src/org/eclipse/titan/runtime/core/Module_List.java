/******************************************************************************
 * Copyright (c) 2000-2017 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

import java.util.ArrayList;

/**
 * The base class of handling modules
 * 
 * TODO: lots to implement
 * 
 * @author Kristof Szabados
 */
public class Module_List {
	public static ArrayList<TTCN_Module> modules = new ArrayList<TTCN_Module>();

	public static void add_module(final TTCN_Module module) {
		modules.add(module);
		System.out.println("registered: " + module.name);
	}

	public static TTCN_Module lookup_module(final String module_name) {
		for (TTCN_Module module : modules) {
			if (module.name.equals(module_name)) {
				return module;
			}
		}

		return null;
	}

	public static void pre_init_modules() {
		for (TTCN_Module module: modules) {
			module.pre_init_module();
		}
	}

	public static void post_init_modules() {
		for (TTCN_Module module: modules) {
			module.post_init_module();
		}
	}

	//originally Module_List::initialize_component
	public static void initialize_component(final String module_name, final String component_type, final boolean init_base_comps) {
		final TTCN_Module module = lookup_module(module_name);
		//FIXME add checks
		module.init_comp_type(component_type, init_base_comps);
		//FIXME handle error
	}
}
