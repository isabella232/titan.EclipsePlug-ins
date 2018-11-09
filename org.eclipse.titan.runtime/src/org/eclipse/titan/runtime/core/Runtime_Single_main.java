/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.List;

import org.eclipse.titan.runtime.core.TTCN_Runtime.executorStateEnum;
import org.eclipse.titan.runtime.core.cfgparser.CfgAnalyzer;
import org.eclipse.titan.runtime.core.cfgparser.ExecuteSectionHandler.ExecuteItem;
import org.eclipse.titan.runtime.core.TTCN_Logger.Severity;

/**
 * The class handling single mode operations.
 *
 * TODO: lots to implement
 *
 * @author Kristof Szabados
 * @author Arpad Lovassy
 */
public final class Runtime_Single_main {

	private Runtime_Single_main() {
		// private constructor to disable accidental instantiation
	}

	//FIXME this is much more complicated
	public static int singleMain(final String[] args ) {
		int returnValue = 0;
		TitanComponent.self.set(new TitanComponent(TitanComponent.MTC_COMPREF));
		TTCN_Runtime.set_state(executorStateEnum.SINGLE_CONTROLPART);
		TTCN_Snapshot.initialize();
		TTCN_Logger.initialize_logger();
		TTCN_Logger.set_executable_name();
		TTCN_Logger.set_start_time();

		try {
			TTCN_Logger.log_executor_runtime(TitanLoggerApi.ExecutorRuntime_reason.enum_type.executor__start__single__mode);
			Module_List.pre_init_modules();

			//TODO: getting cfg file name will be more complicated
			final File config_file = args.length > 0 ?  new File( args[ 0 ] ) : null;
			if (config_file != null) {
				System.err.println(MessageFormat.format( "Using configuration file: `{0}''", config_file ) );
				TTCN_Logger.log_configdata(TitanLoggerApi.ExecutorConfigdata_reason.enum_type.using__config__file, config_file.getName());
				final CfgAnalyzer cfgAnalyzer = new CfgAnalyzer();
				final boolean config_file_failure = cfgAnalyzer.directParse(config_file, config_file.getName(), null);

				TTCN_Runtime.set_logger_parameters();
				TTCN_Logger.open_file();
				TTCN_Logger.write_logger_settings();

				if (!config_file_failure) {
					// EXECUTE section
					final List<ExecuteItem> executeItems = cfgAnalyzer.getExecuteSectionHandler().getExecuteitems();

					//FIXME implement Module_List.log_param();
					Module_List.post_init_modules();
					// run testcases
					for (final ExecuteItem executeItem : executeItems) {
						final String module = executeItem.getModuleName();
						final String testcase = executeItem.getTestcaseName();
						if ("*".equals(testcase) ) {
							Module_List.execute_all_testcases(module);
						} else if (testcase == null || "control".equals(testcase)) {
							Module_List.execute_control(module);
						} else {
							Module_List.execute_testcase(module, testcase);
						}
					}
				}
			} else {
				TTCN_Runtime.set_logger_parameters();
				TTCN_Logger.open_file();
				TTCN_Logger.write_logger_settings();

				Module_List.post_init_modules();

				for (final TTCN_Module module : Module_List.modules) {
					module.control();
				}
			}
		} catch (TtcnError error) {
			// intentionally empty
		} catch (Throwable e) {
			TTCN_Logger.log_str(Severity.ERROR_UNQUALIFIED, "Fatal error. Aborting execution.");
			final StringWriter error = new StringWriter();
			e.printStackTrace(new PrintWriter(error));
	
			TTCN_Logger.begin_event(Severity.ERROR_UNQUALIFIED);
			TTCN_Logger.log_event_str("Dynamic test case error: ");
			TTCN_Logger.log_event_str(error.toString());
			TTCN_Logger.end_event();
			returnValue = -1;
		}
		TTCN_Logger.finish_event();
		TTCN_Runtime.log_verdict_statistics();
		TTCN_Logger.log_executor_runtime(TitanLoggerApi.ExecutorRuntime_reason.enum_type.executor__finish__single__mode);
		TTCN_Logger.close_file();
		TitanPort.clear_parameters();
		TitanComponent.clear_component_names();
		TTCN_EncDec.clear_error();

		TTCN_Logger.terminate_logger();
		TTCN_Snapshot.terminate();
		TTCN_Runtime.clean_up();

		return returnValue;
	}
}
