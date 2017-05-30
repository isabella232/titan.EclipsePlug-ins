package org.eclipse.titan.runtime.core;

/**
 * The class handling single mode operations.
 * 
 * TODO: lots to implement
 * 
 * @author Kristof Szabados
 */
public class Runtime_Single_main {

	//FIXME this is much more complicated
	public static void singleMain() {
		TTCN_Snapshot.initialize();

		Module_List.pre_init_modules();
		Module_List.post_init_modules();

		for (TTCN_Module module : Module_List.modules) {
			module.control();
		}
	}
}
