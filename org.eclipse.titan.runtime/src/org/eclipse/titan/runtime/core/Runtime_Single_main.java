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
 * The class handling single mode operations.
 *
 * TODO: lots to implement
 *
 * @author Kristof Szabados
 */
public final class Runtime_Single_main {

	private Runtime_Single_main() {
		// private constructor to disable accidental instantiation
	}

	//FIXME this is much more complicated
	public static void singleMain() {
		TitanComponent.self.assign(TitanComponent.MTC_COMPREF);
		TTCN_Runtime.set_state(executorStateEnum.SINGLE_CONTROLPART);
		TTCN_Snapshot.initialize();
		TtcnLogger.initialize_logger();
		TtcnLogger.set_start_time();

		TtcnLogger.log_executor_runtime(TitanLoggerApi.ExecutorRuntime_reason.enum_type.executor__start__single__mode);
		Module_List.pre_init_modules();

		TtcnLogger.write_logger_settings();

		Module_List.post_init_modules();

		for (final TTCN_Module module : Module_List.modules) {
			module.control();
		}

		TTCN_Runtime.log_verdict_statistics();
		TtcnLogger.log_executor_runtime(TitanLoggerApi.ExecutorRuntime_reason.enum_type.executor__finish__single__mode);
		TtcnLogger.terminate_logger();
		TTCN_Snapshot.terminate();
	}
}
