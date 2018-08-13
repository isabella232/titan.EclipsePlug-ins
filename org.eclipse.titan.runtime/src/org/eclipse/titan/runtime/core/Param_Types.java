/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

/**
 * This class represents the Param_Types.hh/cc file containing module parameter related structures.
 * 
 * TODO: for now all class are in this class, to delay architectural decision until we know more detail.
 * (most probably will be turned into a package later.)
 * 
 * @author Kristof Szabados
 * */
public final class Param_Types {

	/**
	 * Base class representing a module parameter as read from the configuration file.
	 *
	 * FIXME a lot to implement here
	 * Right now this is just a placeholder so that some could start working on module parameters.
	 */
	public static class Module_Parameter {
		protected Module_Param_Id id;

		/**
		 * @return the Id or error, never returns NULL (because every module parameter should have either an explicit or an implicit id when this is called)
		 * */
		public Module_Param_Id get_id() {
			return id;
		}
	}

	public static class Module_Param_Id {
		public String get_current_name() {
			throw new TtcnError("Internal error: Module_Param_Id.get_current_name()");
		}
	}
}
