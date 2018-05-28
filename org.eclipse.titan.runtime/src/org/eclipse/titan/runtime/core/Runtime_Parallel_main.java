/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

import org.eclipse.titan.runtime.core.TTCN_Runtime.executorStateEnum;
import org.eclipse.titan.runtime.core.TtcnLogger.Severity;

/**
 * The class handling parallel mode operations.
 *
 * TODO: lots to implement
 *
 * @author Kristof Szabados
 */
public class Runtime_Parallel_main {

	private Runtime_Parallel_main() {
		// private constructor to disable accidental instantiation
	}

	//FIXME this is much more complicated
	public static int parallelMain(final String[] argv) {
		int returnValue = 0;

		TitanComponent.self.set(new TitanComponent(TitanComponent.MTC_COMPREF));
		TTCN_Runtime.set_state(executorStateEnum.SINGLE_CONTROLPART);

		if (argv.length != 2) {
			System.out.println("For now only 2 arguments can be passed the host address and port number");
		}

		String local_addr = null;
		final String MC_host = argv[0];
		final int MC_port = Integer.parseInt(argv[1]);

		try {
			TTCN_Snapshot.initialize();
			TtcnLogger.initialize_logger();
			//TTCN_Logger::set_executable_name(argv[0]);
			TtcnLogger.set_start_time();

			try {
				Module_List.pre_init_modules();
				returnValue = TTCN_Runtime.hc_main(local_addr, MC_host, MC_port);
				//FIXME implement missing parts
			} catch (TtcnError error) {
				returnValue = -1;
			}
		} catch (Throwable t) {
			TtcnLogger.log_str(Severity.ERROR_UNQUALIFIED, "Fatal error. Aborting execution.");

			returnValue = -1;
		}

		//FIXME implement clears
		TitanComponent.clear_component_names();
		TTCN_EncDec.clear_error();

		TtcnLogger.terminate_logger();
		TTCN_Snapshot.terminate();

		return returnValue;
	}
}
