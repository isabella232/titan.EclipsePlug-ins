/******************************************************************************
 * Copyright (c) 2000-2019 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

/**
 * Collection of logging parameterization related types.
 * 
 * @author Kristof Szabados
 * */
public final class LoggingParam {

	public static enum logging_param_type {
		LP_FILEMASK,
		LP_CONSOLEMASK,
		LP_LOGFILESIZE,
		LP_LOGFILENUMBER,
		LP_DISKFULLACTION,
		LP_LOGFILE,
		LP_TIMESTAMPFORMAT,
		LP_SOURCEINFOFORMAT,
		LP_APPENDFILE,
		LP_LOGEVENTTYPES,
		LP_LOGENTITYNAME,
		LP_MATCHINGHINTS,
		LP_PLUGIN_SPECIFIC,
		LP_UNKNOWN,
		LP_EMERGENCY,
		LP_EMERGENCYBEHAVIOR,
		LP_EMERGENCYMASK,
		LP_EMERGENCYFORFAIL
	}

	public static final class logging_param_t {
		public logging_param_type log_param_selection;
		public String param_name;

		//union in the compiler
		public String str_val;
		public int int_val;
		public boolean bool_val;
		public TTCN_Logger.Logging_Bits logoptions_val;
		public TTCN_Logger.disk_full_action_t disk_full_action_value;
		public TTCN_Logger.timestamp_format_t timestamp_value;
		public TTCN_Logger.source_info_format_t source_info_value;
		public TTCN_Logger.log_event_types_t log_event_types_values;
		public TTCN_Logger.matching_verbosity_t matching_verbosity_values;
		public int emergency_logging;
		public TTCN_Logger.emergency_logging_behaviour_t emergency_logging_behaviour_value;
	}

	public static final class logging_setting_t {
		public TTCN_Logger.component_id_t component;
		public String pluginId;
		public logging_param_t logparam;
		//nextparam intentionally missing

		public logging_setting_t() {
			//empty by default
		}

		public logging_setting_t(final logging_setting_t other) {
			this.component = other.component;
			this.pluginId = other.pluginId;
			this.logparam = other.logparam;
		}
	}
}
