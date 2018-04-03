/******************************************************************************
 * Copyright (c) 2000-2017 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

import org.eclipse.titan.runtime.core.TTCN_Runtime.executorStateEnum;

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
		public static void parallelMain(String[] argv) {
			TitanComponent.self.assign(TitanComponent.MTC_COMPREF);
			TTCN_Runtime.set_state(executorStateEnum.SINGLE_CONTROLPART);

			if (argv.length != 2) {
				System.out.println("For now only 2 arguments can be passed the host address and port number");
			}

			String local_addr = null;
			String MC_host = argv[0];
			int MC_port = Integer.parseInt(argv[1]);

			TTCN_Snapshot.initialize();
			TtcnLogger.initialize_logger();
			//TTCN_Logger::set_executable_name(argv[0]);
			TtcnLogger.set_start_time();

			Module_List.pre_init_modules();
			int ret_val = TTCN_Runtime.hc_main(local_addr, MC_host, MC_port);
			if (!TTCN_Runtime.is_hc()) {
				System.out.println("it is a HC");
				//FIXME implement
			} else {
				System.out.println("it is not a HC");
			}
//			Module_List.post_init_modules();

//			for (final TTCN_Module module : Module_List.modules) {
//				module.control();
//			}

//			TTCN_Runtime.log_verdict_statistics();

			TtcnLogger.terminate_logger();
			TTCN_Snapshot.terminate();
		}
}
