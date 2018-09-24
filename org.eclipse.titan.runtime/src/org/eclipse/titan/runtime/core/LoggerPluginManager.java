/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Stack;
import java.util.concurrent.LinkedBlockingQueue;

import org.eclipse.titan.runtime.core.Base_Template.template_sel;
import org.eclipse.titan.runtime.core.LoggingParam.logging_param_type;
import org.eclipse.titan.runtime.core.LoggingParam.logging_setting_t;
import org.eclipse.titan.runtime.core.TTCN_Logger.component_id_selector_enum;
import org.eclipse.titan.runtime.core.TitanLoggerApi.DefaultEnd;
import org.eclipse.titan.runtime.core.TitanLoggerApi.DefaultOp;
import org.eclipse.titan.runtime.core.TitanLoggerApi.Dualface__discard;
import org.eclipse.titan.runtime.core.TitanLoggerApi.Dualface__mapped;
import org.eclipse.titan.runtime.core.TitanLoggerApi.ExecutorComponent;
import org.eclipse.titan.runtime.core.TitanLoggerApi.ExecutorComponent_reason;
import org.eclipse.titan.runtime.core.TitanLoggerApi.ExecutorConfigdata;
import org.eclipse.titan.runtime.core.TitanLoggerApi.ExecutorConfigdata_reason;
import org.eclipse.titan.runtime.core.TitanLoggerApi.ExecutorRuntime;
import org.eclipse.titan.runtime.core.TitanLoggerApi.ExecutorRuntime_reason;
import org.eclipse.titan.runtime.core.TitanLoggerApi.ExecutorUnqualified;
import org.eclipse.titan.runtime.core.TitanLoggerApi.ExecutorUnqualified_reason;
import org.eclipse.titan.runtime.core.TitanLoggerApi.FinalVerdictInfo;
import org.eclipse.titan.runtime.core.TitanLoggerApi.FinalVerdictType;
import org.eclipse.titan.runtime.core.TitanLoggerApi.FunctionEvent_choice_random;
import org.eclipse.titan.runtime.core.TitanLoggerApi.LocationInfo;
import org.eclipse.titan.runtime.core.TitanLoggerApi.MatchingDoneType;
import org.eclipse.titan.runtime.core.TitanLoggerApi.MatchingFailureType;
import org.eclipse.titan.runtime.core.TitanLoggerApi.MatchingProblemType;
import org.eclipse.titan.runtime.core.TitanLoggerApi.MatchingSuccessType;
import org.eclipse.titan.runtime.core.TitanLoggerApi.MatchingTimeout;
import org.eclipse.titan.runtime.core.TitanLoggerApi.Msg__port__recv;
import org.eclipse.titan.runtime.core.TitanLoggerApi.Msg__port__send;
import org.eclipse.titan.runtime.core.TitanLoggerApi.ParPort;
import org.eclipse.titan.runtime.core.TitanLoggerApi.ParPort_operation;
import org.eclipse.titan.runtime.core.TitanLoggerApi.ParallelPTC;
import org.eclipse.titan.runtime.core.TitanLoggerApi.ParallelPTC_reason;
import org.eclipse.titan.runtime.core.TitanLoggerApi.PortType.enum_type;
import org.eclipse.titan.runtime.core.TitanLoggerApi.Port__Misc;
import org.eclipse.titan.runtime.core.TitanLoggerApi.Port__Queue;
import org.eclipse.titan.runtime.core.TitanLoggerApi.Port__State;
import org.eclipse.titan.runtime.core.TitanLoggerApi.Proc__port__in;
import org.eclipse.titan.runtime.core.TitanLoggerApi.Proc__port__out;
import org.eclipse.titan.runtime.core.TitanLoggerApi.QualifiedName;
import org.eclipse.titan.runtime.core.TitanLoggerApi.SetVerdictType;
import org.eclipse.titan.runtime.core.TitanLoggerApi.Setstate;
import org.eclipse.titan.runtime.core.TitanLoggerApi.StatisticsType;
import org.eclipse.titan.runtime.core.TitanLoggerApi.StatisticsType_choice_verdictStatistics;
import org.eclipse.titan.runtime.core.TitanLoggerApi.TestcaseType;
import org.eclipse.titan.runtime.core.TitanLoggerApi.TimerGuardType;
import org.eclipse.titan.runtime.core.TitanLoggerApi.TimerType;
import org.eclipse.titan.runtime.core.TitanLoggerApi.TimestampType;
import org.eclipse.titan.runtime.core.TitanLoggerApi.TitanLogEvent;
import org.eclipse.titan.runtime.core.TitanLoggerApi.TitanLogEvent_sourceInfo__list;
import org.eclipse.titan.runtime.core.TitanVerdictType.VerdictTypeEnum;
import org.eclipse.titan.runtime.core.TTCN_Logger.Severity;
import org.eclipse.titan.runtime.core.TTCN_Logger.TTCN_Location;
import org.eclipse.titan.runtime.core.TTCN_Logger.component_id_t;
import org.eclipse.titan.runtime.core.TTCN_Logger.emergency_logging_behaviour_t;
import org.eclipse.titan.runtime.core.TTCN_Logger.extcommand_t;
import org.eclipse.titan.runtime.core.TTCN_Logger.log_event_types_t;

/**
 * The logger plugin manager, is responsible for managing all the runtime registered logger plug-ins
 *
 * FIXME lots to implement here, this is under construction right now
 *
 * @author Kristof Szabados
 */
public class LoggerPluginManager {
	private final LinkedBlockingQueue<TitanLogEvent> ring_buffer = new LinkedBlockingQueue<TitanLoggerApi.TitanLogEvent>();

	private enum event_destination_t {
		ED_NONE,  // To be discarded.
		ED_FILE,  // Event goes to log file or console, it's a historic name.
		ED_STRING // Event goes to CHARSTRING.
	};

	private static class log_event_struct {
		StringBuilder buffer;
		Severity severity;
		event_destination_t event_destination;
		//etc...
	}

	private static class LogEntry {
		TitanLoggerApi.TitanLogEvent event_;

		public LogEntry(final TitanLoggerApi.TitanLogEvent event) {
			event_ = event;
		}
	}

	private static ThreadLocal<log_event_struct> current_event = new ThreadLocal<LoggerPluginManager.log_event_struct>();
	private static ThreadLocal<Stack<log_event_struct>> events = new ThreadLocal<Stack<log_event_struct>>() {
		@Override
		protected Stack<log_event_struct> initialValue() {
			return new Stack<log_event_struct>();
		}
	};

	private static ThreadLocal<Boolean> is_first = new ThreadLocal<Boolean>() {
		@Override
		protected Boolean initialValue() {
			return Boolean.TRUE;
		}
	};

	private ArrayList<logging_setting_t> logparams = new ArrayList<LoggingParam.logging_setting_t>();
	private ArrayList<ILoggerPlugin> plugins_ = new ArrayList<ILoggerPlugin>();
	
	private LinkedList<LogEntry> entry_list_ = new LinkedList<LogEntry>();

	public LoggerPluginManager() {
		plugins_.add(new LegacyLogger());
	}

	public void ring_buffer_dump(final boolean do_close_file) {
		if (TTCN_Logger.get_emergency_logging_behaviour() == emergency_logging_behaviour_t.BUFFER_ALL) {
			TitanLoggerApi.TitanLogEvent ringEvent;
			while (!ring_buffer.isEmpty()) {
				ringEvent = ring_buffer.poll();
				if (ringEvent != null) {
					internal_log_to_all(ringEvent, true, false, false);
				}
			}
		}

		if (do_close_file) {
			for (int i = 0; i < plugins_.size(); i++) {
				plugins_.get(i).close_file();
			}
		}

		ring_buffer.clear();
	}

	// If an event appears before any logger is configured we have to pre-buffer it.
	public void internal_prebuff_logevent(final TitanLogEvent event) {
		final LogEntry new_entry = new LogEntry(event);
		if (entry_list_ == null) {
			entry_list_ = new LinkedList<LogEntry>();
			entry_list_.add(new_entry);
		} else {
			entry_list_.add(new_entry);
		}
	}

	// When the loggers get configured we have to log everything we have buffered so far
	public void internal_log_prebuff_logevent() {
		if (entry_list_ == null) {
			return;
		} else {
			for (final LogEntry entry : entry_list_) {
				if (entry.event_.getseverity().getInt() == TTCN_Logger.Severity.EXECUTOR_LOGOPTIONS.ordinal()) {
					String new_log_message = TTCN_Logger.get_logger_settings_str();
					entry.event_.getlogEvent().getchoice().getexecutorEvent().getchoice().getlogOptions().assign(new_log_message);
					new_log_message = null;
				}

				internal_log_to_all(entry.event_, true, false, false);
			}
		}

		entry_list_.clear();
	}

	public boolean add_parameter(final logging_setting_t logging_param) {
		boolean duplication_warning = false;

		for (final logging_setting_t par: logparams) {
			final boolean for_all_components = logging_param.component.id_selector == component_id_selector_enum.COMPONENT_ID_ALL || par.component.id_selector == component_id_selector_enum.COMPONENT_ID_ALL;
			final boolean for_all_plugins = logging_param.pluginId == null || par.pluginId == null || "*".equals(logging_param.pluginId) || "*".equals(par.pluginId);
			final boolean component_overlaps = for_all_components || logging_param.component == par.component;
			final boolean plugin_overlaps = for_all_plugins || ((logging_param.pluginId == null && par.pluginId == null) || (logging_param.pluginId != null && logging_param.pluginId.equals(par.pluginId)));
			boolean parameter_overlaps = logging_param.logparam.log_param_selection == par.logparam.log_param_selection;
			if (parameter_overlaps && logging_param.logparam.log_param_selection == logging_param_type.LP_PLUGIN_SPECIFIC) {
				parameter_overlaps = logging_param.logparam.param_name == par.logparam.param_name;
			}

			duplication_warning = component_overlaps && plugin_overlaps && parameter_overlaps;
			if (duplication_warning) {
				break;
			}
		}

		final logging_setting_t newParam = new logging_setting_t(logging_param);
		logparams.add(newParam);

		return duplication_warning;
	}

	public void set_parameters(final TitanComponent component_reference, final String component_name) {
		if (logparams.size() == 0) {
			return;
		}

		for (final logging_setting_t par: logparams) {
			switch (par.component.id_selector) {
			case COMPONENT_ID_NAME:
				if (component_name != null && component_name.equals(par.component.id_name)) {
					apply_parameter(par);
				}
				break;
			case COMPONENT_ID_COMPREF:
				if (par.component.id_compref == component_reference.getComponent()) {
					apply_parameter(par);
				}
				break;
			case COMPONENT_ID_ALL:
				apply_parameter(par);
				break;
			default:
				break;
			}
		}
	}

	private void apply_parameter(final logging_setting_t logparam) {
		if (logparam.pluginId != null && !(logparam.pluginId.length() == 1 &&  !(logparam.pluginId.charAt(0) == '*'))) {
			// The parameter refers to a specific plug-in.  If the plug-in is not found the execution will stop.
			final ILoggerPlugin plugin = find_plugin(logparam.pluginId);
			if (plugin != null) {
				send_parameter_to_plugin(plugin, logparam);
			} else {
				//FIXME:TTCN_Error replace with TTCN_Logger.fatal_error()
				throw new TtcnError(MessageFormat.format("Logger plug-in with name {0} was not found.", logparam.pluginId));
			}
		} else {
			// The parameter refers to all plug-ins.
			for (int i = 0; i < plugins_.size(); i++) {
				send_parameter_to_plugin(plugins_.get(i), logparam);
			}
		}
	}

	private void send_parameter_to_plugin(final ILoggerPlugin plugin, final logging_setting_t logparam) {
		switch (logparam.logparam.log_param_selection) {
		case LP_FILEMASK:
			TTCN_Logger.set_file_mask(logparam.component, logparam.logparam.logoptions_val);
			break;
		case LP_CONSOLEMASK:
			TTCN_Logger.set_console_mask(logparam.component, logparam.logparam.logoptions_val);
			break;
		case LP_LOGFILESIZE:
			plugin.set_file_size(logparam.logparam.int_val);
			break;
		case LP_LOGFILENUMBER:
			plugin.set_file_number(logparam.logparam.int_val);
			break;
		case LP_DISKFULLACTION:
			plugin.set_disk_full_action(logparam.logparam.disk_full_action_value);
			break;
		case LP_LOGFILE:
			plugin.set_file_name(logparam.logparam.str_val, true);
			break;
		case LP_TIMESTAMPFORMAT:
			TTCN_Logger.set_timestamp_format(logparam.logparam.timestamp_value);
			break;
		case LP_SOURCEINFOFORMAT:
			TTCN_Logger.set_source_info_format(logparam.logparam.source_info_value);
			break;
		case LP_APPENDFILE:
			plugin.set_append_file(logparam.logparam.bool_val);
			break;
		case LP_LOGEVENTTYPES:
			TTCN_Logger.set_log_event_types(logparam.logparam.log_event_types_values);
			break;
		case LP_LOGENTITYNAME:
			// TODO: log_event_types_t.LOGEVENTTYPES_SUBCATEGORIES never be
			TTCN_Logger.set_log_entity_name(logparam.logparam.bool_val ? log_event_types_t.LOGEVENTTYPES_YES : log_event_types_t.LOGEVENTTYPES_NO);
			break;
		case LP_MATCHINGHINTS:
			TTCN_Logger.set_matching_verbosity(logparam.logparam.matching_verbosity_values);
			break;
		case LP_PLUGIN_SPECIFIC:
			plugin.set_parameter(logparam.logparam.param_name, logparam.logparam.str_val);
			break;
		case LP_EMERGENCY:
			TTCN_Logger.set_emergency_logging(logparam.logparam.emergency_logging);
			// ring_buffer.set_size() doesn't need
			break;
		case LP_EMERGENCYBEHAVIOR:
			TTCN_Logger.set_emergency_logging_behaviour(logparam.logparam.emergency_logging_behaviour_value);
			break;
		case LP_EMERGENCYMASK:
			TTCN_Logger.set_emergency_logging_mask(logparam.component, logparam.logparam.logoptions_val);
			break;
		case LP_EMERGENCYFORFAIL:
			TTCN_Logger.set_emergency_logging_for_fail_verdict(logparam.logparam.bool_val);
			break;
		default:
			break;
		}
	}
	
	public void clear_param_list() {
		logparams = null;
		logparams = new ArrayList<logging_setting_t>();
	}

	public void clear_plugin_list() {
		plugins_ = null;
		plugins_ = new ArrayList<ILoggerPlugin>();
	}

	public ILoggerPlugin find_plugin(final String name) {
		if (name == null) {
			//TODO: throw an error
		}

		for (int i = 0; i < plugins_.size(); i++) {
			final String plugin_name = plugins_.get(i).plugin_name();
			if ((plugin_name != null) && (plugin_name.equals(name))) {
				return plugins_.get(i);
			}
		}

		return null;
	}

	public boolean plugins_ready() {
		for (int i = 0; i < plugins_.size(); i++) {
			if (plugins_.get(i).is_configured()) {
				return true;
			}
		}

		return false;
	}

	/**
	 * The internal logging function representing the interface between the logger and the loggerPluginManager.
	 *
	 * log(const API::TitanLogEvent& event) in the LoggerPluginManager
	 * not yet using the event objects to save on complexity and runtime cost.
	 *
	 * quickly becoming deprecated
	 * */
	private void log(final TitanLoggerApi.TitanLogEvent event) {
		if (!plugins_ready()) {
			// buffer quick events
			internal_prebuff_logevent(event);

			return;
		}

		// Init phase, log prebuffered events first if any.
		internal_log_prebuff_logevent();

		if (TTCN_Logger.get_emergency_logging() == 0) {
			// emergency logging is not needed
			internal_log_to_all(event, false, false, false);

			return;
		}

		final int severityIndex = event.getseverity().getInt();
		final Severity severity = Severity.values()[severityIndex];

		if (TTCN_Logger.get_emergency_logging_behaviour() == emergency_logging_behaviour_t.BUFFER_MASKED) {
			internal_log_to_all(event, true, false, false);

			if (!TTCN_Logger.should_log_to_file(severity) &&
				TTCN_Logger.should_log_to_emergency(severity)) {
				ring_buffer.offer(event);
			}
		} else if (TTCN_Logger.get_emergency_logging_behaviour() == emergency_logging_behaviour_t.BUFFER_ALL) {
			if (ring_buffer.remainingCapacity() == 0) {
				final TitanLoggerApi.TitanLogEvent ring_event = ring_buffer.poll();
				if (ring_event != null) {
					internal_log_to_all(ring_event, true, false, false);
				}
			}

			ring_buffer.offer(event);
		}

		if (severity == Severity.ERROR_UNQUALIFIED || 
				(TTCN_Logger.get_emergency_logging_for_fail_verdict() &&
						severity == Severity.VERDICTOP_SETVERDICT &&
						event.getlogEvent().getchoice().getverdictOp().getchoice().getsetVerdict().getnewVerdict().operatorEquals(TitanLoggerApi.Verdict.enum_type.v3fail)) 
				) {
			TitanLoggerApi.TitanLogEvent ring_event;
			while (!ring_buffer.isEmpty()) {
				ring_event = ring_buffer.poll();
				if (ring_event != null) {
					if (TTCN_Logger.get_emergency_logging_behaviour() == emergency_logging_behaviour_t.BUFFER_MASKED) {
						internal_log_to_all(ring_event, true, true, false);
					} else if (TTCN_Logger.get_emergency_logging_behaviour() == emergency_logging_behaviour_t.BUFFER_ALL) {
						internal_log_to_all(ring_event, true, false, true);
					}
				}
			}

			ring_buffer.clear();
		}
	}

	/**
	 * The internal logging function of the LoggerPluginManager towards the plugins themselves.
	 *
	 * This will be sending of the event to be logged to the logger plugins later,
	 * Right now we only have one (legacy) logger simulated within this same class.
	 * */
	private void internal_log_to_all(final TitanLoggerApi.TitanLogEvent event, final boolean log_buffered, final boolean separate_file, final boolean use_emergency_mask) {
		for (int i = 0; i < plugins_.size(); i++) {
			plugins_.get(i).log(event, log_buffered, separate_file, use_emergency_mask);
		}
	}

	public void set_file_name(final String new_filename_skeleton, final boolean from_config) {
		for (int i = 0; i < plugins_.size(); i++) {
			plugins_.get(i).set_file_name(new_filename_skeleton, from_config);
		}
	}

	public void set_append_file(final boolean new_append_file) {
		for (int i = 0; i < plugins_.size(); i++) {
			plugins_.get(i).set_append_file(new_append_file);
		}
	}

	public boolean set_file_size(final component_id_t comp, final int p_size) {
		boolean ret_val = false;
		for (int i = 0; i < plugins_.size(); i++) {
			if (plugins_.get(i).set_file_size(p_size)) {
				ret_val = true;
			}
		}

		return ret_val;
	}

	public boolean set_file_number(final component_id_t cmpt, final int p_number) {
		boolean ret_val = false;
		for (int i = 0; i < plugins_.size(); i++) {
			if (plugins_.get(i).set_file_number(p_number)) {
				ret_val = true;
			}
		}

		return ret_val;
	}

	public boolean set_disk_full_action(final component_id_t comp, final TTCN_Logger.disk_full_action_t p_disk_full_action) {
		boolean ret_val = false;
		for (int i = 0; i < plugins_.size(); i++) {
			if (plugins_.get(i).set_disk_full_action(p_disk_full_action)) {
				ret_val = true;
			}
		}

		return ret_val;
	}

	public void open_file() {
		boolean free_entry_list = false;
		if (plugins_.isEmpty()) {
			//FIXME: report fatal error as this can not be.
			return;
		}

		for (int i = 0; i < plugins_.size(); i++) {
			plugins_.get(i).open_file(is_first.get().booleanValue());
			if (plugins_.get(i).is_configured()) {
				free_entry_list = true;
				for (final LogEntry entry : entry_list_) {
					if (entry.event_.getseverity().getInt() == TTCN_Logger.Severity.EXECUTOR_LOGOPTIONS.ordinal()) {
						String new_log_message = TTCN_Logger.get_logger_settings_str();
						entry.event_.getlogEvent().getchoice().getexecutorEvent().getchoice().getlogOptions().assign(new_log_message);
						new_log_message = "";
					}
					plugins_.get(i).log(entry.event_, true, false, false);
				}
			}
		}

		if (free_entry_list) {
			entry_list_.clear();
		}
		is_first.set(false);
	}

	public void close_file() {
		while (current_event.get() != null) {
			finish_event();
		}

		ring_buffer_dump(true);
	}

	public void begin_event(final Severity msg_severity) {
		final log_event_struct temp = new log_event_struct();

		current_event.set(temp);
		temp.severity = msg_severity;
		temp.buffer = new StringBuilder(100);
		if (TTCN_Logger.log_this_event(msg_severity)) {
			temp.event_destination = event_destination_t.ED_FILE;
		} else {
			temp.event_destination = event_destination_t.ED_NONE;
		}

		events.get().push(temp);
	}

	public void begin_event_log2str() {
		final log_event_struct temp = new log_event_struct();

		current_event.set(temp);
		temp.severity = Severity.USER_UNQUALIFIED;
		temp.buffer = new StringBuilder(100);
		temp.event_destination = event_destination_t.ED_STRING;

		events.get().push(temp);
	}

	public void end_event() {
		final log_event_struct tempEventStruct = current_event.get();
		if (tempEventStruct == null) {
			log_unhandled_event(Severity.WARNING_UNQUALIFIED, "TTCN_Logger::end_event(): not in event.");
			return;
		}

		switch (tempEventStruct.event_destination) {
		case ED_NONE:
			break;
		case ED_FILE:
			//FIXME implement
			log_unhandled_event(tempEventStruct.severity, current_event.get().buffer.toString());
			break;
		case ED_STRING:
			//FIXME report error
			break;
		}

		final Stack<log_event_struct> localEvents = events.get();
		localEvents.pop();
		if (!localEvents.isEmpty()) {
			current_event.set(localEvents.peek());
		} else {
			current_event.set(null);
		}
	}

	public TitanCharString end_event_log2str() {
		final log_event_struct tempEventStruct = current_event.get();
		if (tempEventStruct == null) {
			log_unhandled_event(Severity.WARNING_UNQUALIFIED, "TTCN_Logger::end_event_log2str(): not in event.");
			return new TitanCharString();
		}

		final TitanCharString ret_val = new TitanCharString(tempEventStruct.buffer);
		final Stack<log_event_struct> localEvents = events.get();
		localEvents.pop();
		if (!localEvents.isEmpty()) {
			current_event.set(localEvents.peek());
		} else {
			current_event.set(null);
		}

		return ret_val;
	}

	public void finish_event() {
		// There is no try-catch block to delete string targeted operations.
		while (current_event.get() != null && current_event.get().event_destination == event_destination_t.ED_STRING) {
			end_event_log2str();
		}

		if (current_event.get() != null) {
			log_event_str("<unfinished>");
			end_event();
		}
	}

	public void log_event_str(final String string) {
		final log_event_struct tempEventStruct = current_event.get();
		if (tempEventStruct != null) {
			if (tempEventStruct.event_destination == event_destination_t.ED_NONE) {
				return;
			}
			if (string == null) {
				tempEventStruct.buffer.append("<NULL pointer>");
			} else {
				tempEventStruct.buffer.append(string);
			}
		} else {
			log_unhandled_event(Severity.WARNING_UNQUALIFIED, "TTCN_Logger::log_event_str(): not in event.");
		}
	}

	public void log_char(final char c) {
		final log_event_struct tempEventStruct = current_event.get();
		if (tempEventStruct != null) {
			if (tempEventStruct.event_destination == event_destination_t.ED_NONE || c == '\0') {
				return;
			}

			tempEventStruct.buffer.append(c);
		} else {
			log_unhandled_event(Severity.WARNING_UNQUALIFIED, "TTCN_Logger::log_char(): not in event.");
		}
	}

	public void log_event_va_list(final String formatString, final Object... args) {
		final log_event_struct tempEventStruct = current_event.get();
		if (tempEventStruct != null) {
			if (tempEventStruct.event_destination == event_destination_t.ED_NONE) {
				return;
			}

			tempEventStruct.buffer.append(String.format(Locale.US, formatString, args));
		} else {
			log_unhandled_event(Severity.WARNING_UNQUALIFIED, "TTCN_Logger::log_event_va_list(): not in event.");
		}
	}

	public void log_unhandled_event(final Severity severity, final String message) {
		if (!TTCN_Logger.log_this_event(severity) && TTCN_Logger.get_emergency_logging() <= 0) {
			return;
		}

		final TitanLogEvent event = new TitanLogEvent();
		fill_common_fields(event, severity);
		event.getlogEvent().getchoice().getunhandledEvent().assign(message);

		log(event);
	}

	public void log_log_options(final String message) {
		if (!TTCN_Logger.log_this_event(Severity.EXECUTOR_LOGOPTIONS) && TTCN_Logger.get_emergency_logging() <= 0) {
			return;
		}

		final TitanLogEvent event = new TitanLogEvent();
		fill_common_fields(event, Severity.EXECUTOR_LOGOPTIONS);

		event.getlogEvent().getchoice().getexecutorEvent().getchoice().getlogOptions().assign(message);

		log(event);
	}

	public void log_timer_read(final String timer_name, final double timeout_val) {
		if (!TTCN_Logger.log_this_event(Severity.TIMEROP_READ) && TTCN_Logger.get_emergency_logging() <= 0) {
			return;
		}

		final TitanLogEvent event = new TitanLogEvent();
		fill_common_fields(event, Severity.TIMEROP_READ);
		final TimerType timer = event.getlogEvent().getchoice().gettimerEvent().getchoice().getreadTimer();
		timer.getname().assign(timer_name);
		timer.getvalue__().assign(timeout_val);

		log(event);
	}

	public void log_timer_start(final String timer_name, final double start_val) {
		if (!TTCN_Logger.log_this_event(Severity.TIMEROP_START) && TTCN_Logger.get_emergency_logging() <= 0) {
			return;
		}

		final TitanLogEvent event = new TitanLogEvent();
		fill_common_fields(event, Severity.TIMEROP_START);
		final TimerType timer = event.getlogEvent().getchoice().gettimerEvent().getchoice().getstartTimer();
		timer.getname().assign(timer_name);
		timer.getvalue__().assign(start_val);

		log(event);
	}

	public void log_timer_guard(final double start_val) {
		if (!TTCN_Logger.log_this_event(Severity.TIMEROP_GUARD) && TTCN_Logger.get_emergency_logging() <= 0) {
			return;
		}

		final TitanLogEvent event = new TitanLogEvent();
		fill_common_fields(event, Severity.TIMEROP_GUARD);
		final TimerGuardType timer = event.getlogEvent().getchoice().gettimerEvent().getchoice().getguardTimer();
		timer.getvalue__().assign(start_val);

		log(event);
	}

	public void log_timer_stop(final String timer_name, final double stop_val) {
		if (!TTCN_Logger.log_this_event(Severity.TIMEROP_STOP) && TTCN_Logger.get_emergency_logging() <= 0) {
			return;
		}

		final TitanLogEvent event = new TitanLogEvent();
		fill_common_fields(event, Severity.TIMEROP_STOP);
		final TimerType timer = event.getlogEvent().getchoice().gettimerEvent().getchoice().getstopTimer();
		timer.getname().assign(timer_name);
		timer.getvalue__().assign(stop_val);

		log(event);
	}

	public void log_timer_timeout(final String timer_name, final double timeout_val) {
		if (!TTCN_Logger.log_this_event(Severity.TIMEROP_TIMEOUT) && TTCN_Logger.get_emergency_logging() <= 0) {
			return;
		}

		final TitanLogEvent event = new TitanLogEvent();
		fill_common_fields(event, Severity.TIMEROP_TIMEOUT);
		final TimerType timer = event.getlogEvent().getchoice().gettimerEvent().getchoice().gettimeoutTimer();
		timer.getname().assign(timer_name);
		timer.getvalue__().assign(timeout_val);

		log(event);
	}

	public void log_timer_any_timeout() {
		if (!TTCN_Logger.log_this_event(Severity.TIMEROP_TIMEOUT) && TTCN_Logger.get_emergency_logging() <= 0) {
			return;
		}

		final TitanLogEvent event = new TitanLogEvent();
		fill_common_fields(event, Severity.TIMEROP_TIMEOUT);
		event.getlogEvent().getchoice().gettimerEvent().getchoice().gettimeoutAnyTimer().assign(TitanNull_Type.NULL_VALUE);

		log(event);
	}

	public void log_timer_unqualified(final String message) {
		if (!TTCN_Logger.log_this_event(Severity.TIMEROP_UNQUALIFIED) && TTCN_Logger.get_emergency_logging() <= 0) {
			return;
		}

		final TitanLogEvent event = new TitanLogEvent();
		fill_common_fields(event, Severity.TIMEROP_UNQUALIFIED);
		event.getlogEvent().getchoice().gettimerEvent().getchoice().getunqualifiedTimer().assign(message);

		log(event);
	}

	public void log_matching_timeout(final String timer_name) {
		if (!TTCN_Logger.log_this_event(Severity.MATCHING_PROBLEM) && TTCN_Logger.get_emergency_logging() <= 0) {
			return;
		}

		final TitanLogEvent event = new TitanLogEvent();
		fill_common_fields(event, Severity.MATCHING_PROBLEM);
		final MatchingTimeout mt = event.getlogEvent().getchoice().getmatchingEvent().getchoice().getmatchingTimeout();
		if (timer_name != null) {
			mt.gettimer__name().get().assign(timer_name);
		} else {
			mt.gettimer__name().assign(template_sel.OMIT_VALUE);
		}

		log(event);
	}

	public void log_port_queue(final TitanLoggerApi.Port__Queue_operation.enum_type operation, final String port_name, final int componentReference, final int id, final TitanCharString address, final TitanCharString parameter) {
		Severity sev;
		switch (operation) {
		case enqueue__msg:
		case extract__msg:
			sev = Severity.PORTEVENT_MQUEUE;
			break;
		case enqueue__call:
		case enqueue__reply:
		case enqueue__exception:
		case extract__op:
			sev = Severity.PORTEVENT_PQUEUE;
			break;
		default:
			throw new TtcnError("Invalid operation");
		}

		if (!TTCN_Logger.log_this_event(sev) && TTCN_Logger.get_emergency_logging() <= 0) {
			return;
		}

		final TitanLogEvent event = new TitanLogEvent();
		fill_common_fields(event, sev);
		final Port__Queue portQueue = event.getlogEvent().getchoice().getportEvent().getchoice().getportQueue();
		portQueue.getoperation().assign(operation.ordinal());
		portQueue.getport__name().assign(port_name);
		portQueue.getcompref().assign(adjust_compref(componentReference));
		portQueue.getmsgid().assign(id);
		portQueue.getaddress__().assign(address);
		portQueue.getparam__().assign(parameter);

		log(event);
	}

	private static int adjust_compref(final int compref) {
		if (compref == TitanComponent.MTC_COMPREF) {
			switch (TTCN_Runtime.get_state()) {
			case MTC_CONTROLPART:
			case SINGLE_CONTROLPART:
				return TitanComponent.CONTROL_COMPREF;
			default:
				break;
			}
		}

		return compref;
	}

	public void log_port_state(final TitanLoggerApi.Port__State_operation.enum_type operation, final String portname) {
		if (!TTCN_Logger.log_this_event(Severity.PORTEVENT_STATE)) {
			return;
		}

		final TitanLogEvent event = new TitanLogEvent();
		fill_common_fields(event, Severity.PORTEVENT_STATE);
		final Port__State ps = event.getlogEvent().getchoice().getportEvent().getchoice().getportState();
		ps.getoperation().assign(operation);
		ps.getport__name().assign(portname);

		log(event);
	}

	public void log_procport_send(final String portname, final TitanLoggerApi.Port__oper.enum_type operation, final int componentReference, final TitanCharString system, final TitanCharString parameter) {
		final Severity severity = componentReference == TitanComponent.SYSTEM_COMPREF ? Severity.PORTEVENT_PMOUT : Severity.PORTEVENT_PCOUT;
		if (!TTCN_Logger.log_this_event(severity) && TTCN_Logger.get_emergency_logging() <= 0) {
			return;
		}

		final TitanLogEvent event = new TitanLogEvent();
		fill_common_fields(event, severity);
		final Proc__port__out pt = event.getlogEvent().getchoice().getportEvent().getchoice().getprocPortSend();
		pt.getoperation().assign(operation);
		pt.getport__name().assign(portname);
		pt.getcompref().assign(componentReference);
		if (componentReference == TitanComponent.SYSTEM_COMPREF) {
			pt.getsys__name().assign(system);
		}
		pt.getparameter().assign(parameter);

		log(event);
	}

	public void log_procport_recv(final String portname, final TitanLoggerApi.Port__oper.enum_type operation, final int componentReference, final boolean check, final TitanCharString parameter, final int id) {
		final Severity severity = componentReference == TitanComponent.SYSTEM_COMPREF ? Severity.PORTEVENT_PMIN : Severity.PORTEVENT_PCIN;
		if (!TTCN_Logger.log_this_event(severity) && TTCN_Logger.get_emergency_logging() <= 0) {
			return;
		}

		final TitanLogEvent event = new TitanLogEvent();
		fill_common_fields(event, severity);
		final Proc__port__in pt = event.getlogEvent().getchoice().getportEvent().getchoice().getprocPortRecv();
		pt.getoperation().assign(operation);
		pt.getport__name().assign(portname);
		pt.getcompref().assign(componentReference);
		pt.getcheck__().assign(check);
		pt.getparameter().assign(parameter);
		pt.getmsgid().assign(id);

		log(event);
	}

	public void log_msgport_send(final String portname, final int componentReference, final TitanCharString parameter) {
		final Severity severity = componentReference == TitanComponent.SYSTEM_COMPREF ? Severity.PORTEVENT_MMSEND : Severity.PORTEVENT_MCSEND;
		if (!TTCN_Logger.log_this_event(severity) && TTCN_Logger.get_emergency_logging() <= 0) {
			return;
		}

		final TitanLogEvent event = new TitanLogEvent();
		fill_common_fields(event, severity);
		final Msg__port__send ms = event.getlogEvent().getchoice().getportEvent().getchoice().getmsgPortSend();
		ms.getport__name().assign(portname);
		ms.getcompref().assign(componentReference);
		ms.getparameter().assign(parameter);

		log(event);
	}

	public void log_msgport_recv(final String portname, final TitanLoggerApi.Msg__port__recv_operation.enum_type operation, final int componentReference, final TitanCharString system, final TitanCharString parameter, final int id) {
		final Severity severity = componentReference == TitanComponent.SYSTEM_COMPREF ? Severity.PORTEVENT_MMRECV : Severity.PORTEVENT_MCRECV;
		if (!TTCN_Logger.log_this_event(severity) && TTCN_Logger.get_emergency_logging() <= 0) {
			return;
		}

		final TitanLogEvent event = new TitanLogEvent();
		fill_common_fields(event, severity);
		final Msg__port__recv ms = event.getlogEvent().getchoice().getportEvent().getchoice().getmsgPortRecv();
		ms.getport__name().assign(portname);
		ms.getcompref().assign(componentReference);
		if (componentReference == TitanComponent.SYSTEM_COMPREF) {
			ms.getsys__name().assign(system);
		}
		ms.getoperation().assign(operation);
		ms.getmsgid().assign(id);
		ms.getparameter().assign(parameter);

		log(event);
	}

	public void log_dualport_map(final boolean incoming, final String target_type, final TitanCharString value, final int id) {
		final Severity severity = incoming ? Severity.PORTEVENT_DUALRECV : Severity.PORTEVENT_DUALSEND;
		if (!TTCN_Logger.log_this_event(severity) && TTCN_Logger.get_emergency_logging() <= 0) {
			return;
		}

		final TitanLogEvent event = new TitanLogEvent();
		fill_common_fields(event, severity);
		final Dualface__mapped dual = event.getlogEvent().getchoice().getportEvent().getchoice().getdualMapped();
		dual.getincoming().assign(incoming);
		dual.gettarget__type().assign(target_type);
		dual.getvalue__().assign(value);
		dual.getmsgid().assign(id);

		log(event);
	}

	public void log_dualport_discard(final boolean incoming, final String target_type, final TitanCharString port_name, final boolean unhandled) {
		final Severity severity = incoming ? Severity.PORTEVENT_DUALRECV : Severity.PORTEVENT_DUALSEND;
		if (!TTCN_Logger.log_this_event(severity) && TTCN_Logger.get_emergency_logging() <= 0) {
			return;
		}

		final TitanLogEvent event = new TitanLogEvent();
		fill_common_fields(event, severity);
		final Dualface__discard dual = event.getlogEvent().getchoice().getportEvent().getchoice().getdualDiscard();
		dual.getincoming().assign(incoming);
		dual.gettarget__type().assign(target_type);
		dual.getport__name().assign(port_name);
		dual.getunhandled().assign(unhandled);

		log(event);
	}

	public void log_dualport_discard(final boolean incoming, final String target_type, final String port_name, final boolean unhandled) {
		final Severity severity = incoming ? Severity.PORTEVENT_DUALRECV : Severity.PORTEVENT_DUALSEND;
		if (!TTCN_Logger.log_this_event(severity) && TTCN_Logger.get_emergency_logging() <= 0) {
			return;
		}

		final TitanLogEvent event = new TitanLogEvent();
		fill_common_fields(event, severity);
		final Dualface__discard dual = event.getlogEvent().getchoice().getportEvent().getchoice().getdualDiscard();
		dual.getincoming().assign(incoming);
		dual.gettarget__type().assign(target_type);
		dual.getport__name().assign(port_name);
		dual.getunhandled().assign(unhandled);

		log(event);
	}

	public void log_setstate(final String port_name, final TitanPort.translation_port_state state, final TitanCharString info) {
		if (!TTCN_Logger.log_this_event(Severity.PORTEVENT_SETSTATE) && TTCN_Logger.get_emergency_logging() <= 0) {
			return;
		}

		final TitanLogEvent event = new TitanLogEvent();
		fill_common_fields(event, Severity.PORTEVENT_SETSTATE);

		final Setstate setstate = event.getlogEvent().getchoice().getportEvent().getchoice().getsetState();
		setstate.getport__name().assign(port_name);
		setstate.getinfo().assign(info);
		switch (state) {
		case UNSET:
			setstate.getstate().assign("unset");
			break;
		case TRANSLATED:
			setstate.getstate().assign("translated");
			break;
		case NOT_TRANSLATED:
			setstate.getstate().assign("not translated");
			break;
		case FRAGMENTED:
			setstate.getstate().assign("fragemnted");
			break;
		case PARTIALLY_TRANSLATED:
			setstate.getstate().assign("partially translated");
			break;
		case DISCARDED:
			setstate.getstate().assign("discarded");
			break;
		default:
			break;
		}

		log(event);
	}

	public void log_setverdict(final VerdictTypeEnum newVerdict, final VerdictTypeEnum oldVerdict, final VerdictTypeEnum localVerdict,
			final String oldReason, final String newReason) {
		if (!TTCN_Logger.log_this_event(Severity.VERDICTOP_SETVERDICT) && TTCN_Logger.get_emergency_logging() <= 0) {
			return;
		}

		final TitanLogEvent event = new TitanLogEvent();
		fill_common_fields(event, Severity.VERDICTOP_SETVERDICT);
		final SetVerdictType set = event.getlogEvent().getchoice().getverdictOp().getchoice().getsetVerdict();
		set.getnewVerdict().assign(newVerdict.ordinal());
		set.getoldVerdict().assign(oldVerdict.ordinal());
		set.getlocalVerdict().assign(localVerdict.ordinal());
		if (oldReason != null) {
			set.getoldReason().get().assign(oldReason);
		} else {
			set.getoldReason().assign(template_sel.OMIT_VALUE);
		}
		if (newReason != null) {
			set.getnewReason().get().assign(newReason);
		} else {
			set.getnewReason().assign(template_sel.OMIT_VALUE);
		}

		log(event);
	}

	public void log_getverdict(final VerdictTypeEnum verdict) {
		if (!TTCN_Logger.log_this_event(Severity.VERDICTOP_GETVERDICT) && TTCN_Logger.get_emergency_logging() <= 0) {
			return;
		}

		final TitanLogEvent event = new TitanLogEvent();
		fill_common_fields(event, Severity.VERDICTOP_GETVERDICT);
		event.getlogEvent().getchoice().getverdictOp().getchoice().getgetVerdict().assign(verdict.ordinal());

		log(event);
	}

	private void fill_common_fields(final TitanLogEvent event, final Severity severity) {
		final long timestamp = System.currentTimeMillis();
		event.gettimestamp().assign(new TimestampType(new TitanInteger((int)(timestamp / 1000)), new TitanInteger((int)(timestamp % 1000))));

		final TitanLogEvent_sourceInfo__list srcinfo = event.getsourceInfo__list();
		if (TTCN_Location.actualSize.get() == 0) {
			srcinfo.assign(TitanNull_Type.NULL_VALUE);
		} else {
			final int localSize = TTCN_Location.actualSize.get();
			final ArrayList<TTCN_Location> localLocations = TTCN_Location.locations.get();
			for (int i = 0; i < localSize; i++) {
				final LocationInfo loc = srcinfo.getAt(i);
				final TTCN_Location temp = localLocations.get(i);

				loc.getfilename().assign(temp.file_name);
				loc.getline().assign(temp.line_number);
				loc.getent__type().assign(temp.entity_type.ordinal());
				loc.getent__name().assign(temp.entity_name);
			}
		}

		event.getseverity().assign(severity.ordinal());
	}

	public void log_testcase_started(final String module_name, final String definition_name) {
		if (!TTCN_Logger.log_this_event(Severity.TESTCASE_START) && TTCN_Logger.get_emergency_logging() <= 0) {
			return;
		}

		final TitanLogEvent event = new TitanLogEvent();
		fill_common_fields(event, Severity.TESTCASE_START);
		final QualifiedName qname = event.getlogEvent().getchoice().gettestcaseOp().getchoice().gettestcaseStarted();
		qname.getmodule__name().assign(module_name);
		qname.gettestcase__name().assign(definition_name);

		log(event);
	}

	public void log_testcase_finished(final String module_name, final String definition_name, final VerdictTypeEnum verdict, final String reason) {
		if (!TTCN_Logger.log_this_event(Severity.TESTCASE_FINISH) && TTCN_Logger.get_emergency_logging() <= 0) {
			return;
		}

		final TitanLogEvent event = new TitanLogEvent();
		fill_common_fields(event, Severity.TESTCASE_FINISH);
		final TestcaseType testcase = event.getlogEvent().getchoice().gettestcaseOp().getchoice().gettestcaseFinished();
		final QualifiedName qname = testcase.getname();
		qname.getmodule__name().assign(module_name);
		qname.gettestcase__name().assign(definition_name);
		testcase.getverdict().assign(verdict.ordinal());
		testcase.getreason().assign(reason);

		log(event);
	}

	public void log_final_verdict(final boolean is_ptc, final TitanVerdictType.VerdictTypeEnum ptc_verdict, final TitanVerdictType.VerdictTypeEnum local_verdict, final TitanVerdictType.VerdictTypeEnum new_verdict, final String verdict_reason, final int notification, final int ptc_compref, final String ptc_name) {
		if (!TTCN_Logger.log_this_event(Severity.VERDICTOP_FINAL) && TTCN_Logger.get_emergency_logging() <= 0) {
			return;
		}

		final TitanLogEvent event = new TitanLogEvent();
		fill_common_fields(event, Severity.VERDICTOP_FINAL);
		final FinalVerdictType finalVerdict = event.getlogEvent().getchoice().getverdictOp().getchoice().getfinalVerdict();
		if (notification >= 0) {
			finalVerdict.getchoice().getnotification().assign(notification);
		} else {
			final FinalVerdictInfo info = finalVerdict.getchoice().getinfo();

			info.getis__ptc().assign(is_ptc);
			info.getptc__verdict().assign(ptc_verdict.ordinal());
			info.getlocal__verdict().assign(local_verdict.ordinal());
			info.getnew__verdict().assign(new_verdict.ordinal());
			info.getptc__compref().get().assign(ptc_compref);
			if (verdict_reason == null) {
				info.getverdict__reason().assign(template_sel.OMIT_VALUE);
			} else {
				info.getverdict__reason().get().assign(verdict_reason);
			}
			if (ptc_name == null) {
				info.getptc__name().assign(template_sel.OMIT_VALUE);
			} else {
				info.getptc__name().get().assign(ptc_name);
			}
		}

		log(event);
	}

	public void log_controlpart_start_stop(final String moduleName, final boolean finished) {
		if (!TTCN_Logger.log_this_event(Severity.STATISTICS_UNQUALIFIED) && TTCN_Logger.get_emergency_logging() <= 0) {
			return;
		}

		final TitanLogEvent event = new TitanLogEvent();
		fill_common_fields(event, Severity.STATISTICS_UNQUALIFIED);
		final StatisticsType stats = event.getlogEvent().getchoice().getstatistics();
		if (finished) {
			stats.getchoice().getcontrolpartFinish().assign(moduleName);
		} else {
			stats.getchoice().getcontrolpartStart().assign(moduleName);
		}

		log(event);
	}

	public void log_controlpart_errors(final int error_count) {
		if (!TTCN_Logger.log_this_event(Severity.STATISTICS_UNQUALIFIED) && TTCN_Logger.get_emergency_logging() <= 0) {
			return;
		}

		final TitanLogEvent event = new TitanLogEvent();
		fill_common_fields(event, Severity.STATISTICS_UNQUALIFIED);
		final StatisticsType stats = event.getlogEvent().getchoice().getstatistics();
		stats.getchoice().getcontrolpartErrors().assign(error_count);

		log(event);
	}

	public void log_verdict_statistics(final int none_count, final double none_percent,
			final int pass_count, final double pass_percent,
			final int inconc_count, final double inconc_percent,
			final int fail_count, final double fail_percent,
			final int error_count, final double error_percent) {
		if (!TTCN_Logger.log_this_event(Severity.STATISTICS_VERDICT) && TTCN_Logger.get_emergency_logging() <= 0) {
			return;
		}

		final TitanLogEvent event = new TitanLogEvent();
		fill_common_fields(event, Severity.STATISTICS_VERDICT);
		final StatisticsType stats = event.getlogEvent().getchoice().getstatistics();
		final StatisticsType_choice_verdictStatistics verdictStats = stats.getchoice().getverdictStatistics();
		verdictStats.getnone__().assign(none_count);
		verdictStats.getnonePercent().assign(none_percent);
		verdictStats.getpass__().assign(pass_count);
		verdictStats.getpassPercent().assign(pass_percent);
		verdictStats.getinconc__().assign(inconc_count);
		verdictStats.getinconcPercent().assign(inconc_percent);
		verdictStats.getfail__().assign(fail_count);
		verdictStats.getfailPercent().assign(fail_percent);
		verdictStats.geterror__().assign(error_count);
		verdictStats.geterrorPercent().assign(error_percent);

		log(event);
	}

	public void log_defaultop_activate(final String name, final int id) {
		if (!TTCN_Logger.log_this_event(Severity.DEFAULTOP_ACTIVATE) && TTCN_Logger.get_emergency_logging() <= 0) {
			return;
		}

		final TitanLogEvent event = new TitanLogEvent();
		fill_common_fields(event, TTCN_Logger.Severity.DEFAULTOP_ACTIVATE);
		final DefaultOp defaultop = event.getlogEvent().getchoice().getdefaultEvent().getchoice().getdefaultopActivate();
		defaultop.getname().assign(name);
		defaultop.getid().assign(id);
		defaultop.getend().assign(DefaultEnd.enum_type.UNKNOWN_VALUE);

		log(event);
	}

	public void log_defaultop_deactivate(final String name, final int id) {
		if (!TTCN_Logger.log_this_event(Severity.DEFAULTOP_DEACTIVATE) && TTCN_Logger.get_emergency_logging() <= 0) {
			return;
		}

		final TitanLogEvent event = new TitanLogEvent();
		fill_common_fields(event, TTCN_Logger.Severity.DEFAULTOP_DEACTIVATE);
		final DefaultOp defaultop = event.getlogEvent().getchoice().getdefaultEvent().getchoice().getdefaultopDeactivate();
		defaultop.getname().assign(name);
		defaultop.getid().assign(id);
		defaultop.getend().assign(DefaultEnd.enum_type.UNKNOWN_VALUE);

		log(event);
	}

	public void log_defaultop_exit(final String name, final int id, final int x) {
		if (!TTCN_Logger.log_this_event(Severity.DEFAULTOP_EXIT) && TTCN_Logger.get_emergency_logging() <= 0) {
			return;
		}

		final TitanLogEvent event = new TitanLogEvent();
		fill_common_fields(event, TTCN_Logger.Severity.DEFAULTOP_EXIT);
		final DefaultOp defaultop = event.getlogEvent().getchoice().getdefaultEvent().getchoice().getdefaultopExit();
		defaultop.getname().assign(name);
		defaultop.getid().assign(id);
		defaultop.getend().assign(x);

		log(event);
	}

	public void log_executor_runtime(final TitanLoggerApi.ExecutorRuntime_reason.enum_type reason) {
		if (!TTCN_Logger.log_this_event(TTCN_Logger.Severity.EXECUTOR_RUNTIME) && (TTCN_Logger.get_emergency_logging() <= 0)) {
			return;
		}

		final TitanLogEvent event = new TitanLogEvent();
		fill_common_fields(event, TTCN_Logger.Severity.EXECUTOR_RUNTIME);
		final ExecutorRuntime exec = event.getlogEvent().getchoice().getexecutorEvent().getchoice().getexecutorRuntime();
		exec.getreason().assign(reason);
		exec.getmodule__name().assign(template_sel.OMIT_VALUE);
		exec.gettestcase__name().assign(template_sel.OMIT_VALUE);
		exec.getpid().assign(template_sel.OMIT_VALUE);
		exec.getfd__setsize().assign(template_sel.OMIT_VALUE);

		log(event);
	}

	public void log_hc_start(final String host) {
		if (!TTCN_Logger.log_this_event(TTCN_Logger.Severity.EXECUTOR_RUNTIME) && (TTCN_Logger.get_emergency_logging() <= 0)) {
			return;
		}

		final TitanLogEvent event = new TitanLogEvent();
		fill_common_fields(event, TTCN_Logger.Severity.EXECUTOR_RUNTIME);
		final ExecutorRuntime exec = event.getlogEvent().getchoice().getexecutorEvent().getchoice().getexecutorRuntime();
		exec.getreason().assign(ExecutorRuntime_reason.enum_type.host__controller__started);
		exec.getmodule__name().get().assign(host);
		exec.gettestcase__name().assign(template_sel.OMIT_VALUE);
		exec.getpid().assign(template_sel.OMIT_VALUE);
		exec.getfd__setsize().assign(template_sel.OMIT_VALUE);

		log(event);
	}

	public void log_testcase_exec(final String testcase, final String module) {
		if (!TTCN_Logger.log_this_event(TTCN_Logger.Severity.EXECUTOR_RUNTIME) && (TTCN_Logger.get_emergency_logging() <= 0)) {
			return;
		}

		final TitanLogEvent event = new TitanLogEvent();
		fill_common_fields(event, TTCN_Logger.Severity.EXECUTOR_RUNTIME);
		final ExecutorRuntime exec = event.getlogEvent().getchoice().getexecutorEvent().getchoice().getexecutorRuntime();
		exec.getreason().assign(ExecutorRuntime_reason.enum_type.executing__testcase__in__module);
		exec.getmodule__name().get().assign(module);
		exec.gettestcase__name().get().assign(testcase);
		exec.getpid().assign(template_sel.OMIT_VALUE);
		exec.getfd__setsize().assign(template_sel.OMIT_VALUE);

		log(event);
	}

	public void log_module_init(final String module, final boolean finish) {
		if (!TTCN_Logger.log_this_event(TTCN_Logger.Severity.EXECUTOR_RUNTIME) && (TTCN_Logger.get_emergency_logging() <= 0)) {
			return;
		}

		final TitanLogEvent event = new TitanLogEvent();
		fill_common_fields(event, TTCN_Logger.Severity.EXECUTOR_RUNTIME);
		final ExecutorRuntime exec = event.getlogEvent().getchoice().getexecutorEvent().getchoice().getexecutorRuntime();
		if (finish) {
			exec.getreason().assign(ExecutorRuntime_reason.enum_type.initialization__of__module__finished);
		} else {
			exec.getreason().assign(ExecutorRuntime_reason.enum_type.initializing__module);
		}

		exec.getmodule__name().get().assign(module);
		exec.gettestcase__name().assign(template_sel.OMIT_VALUE);
		exec.getpid().assign(template_sel.OMIT_VALUE);
		exec.getfd__setsize().assign(template_sel.OMIT_VALUE);

		log(event);
	}

	public void log_mtc_created(final long pid) {
		if (!TTCN_Logger.log_this_event(TTCN_Logger.Severity.EXECUTOR_RUNTIME) && (TTCN_Logger.get_emergency_logging() <= 0)) {
			return;
		}

		final TitanLogEvent event = new TitanLogEvent();
		fill_common_fields(event, TTCN_Logger.Severity.EXECUTOR_RUNTIME);
		final ExecutorRuntime exec = event.getlogEvent().getchoice().getexecutorEvent().getchoice().getexecutorRuntime();
		exec.getreason().assign(ExecutorRuntime_reason.enum_type.mtc__created);
		exec.getmodule__name().assign(template_sel.OMIT_VALUE);
		exec.gettestcase__name().assign(template_sel.OMIT_VALUE);
		exec.getpid().get().assign((int)pid);
		exec.getfd__setsize().assign(template_sel.OMIT_VALUE);

		log(event);
	}

	public void log_configdata(final ExecutorConfigdata_reason.enum_type reason, final String str) {
		if (!TTCN_Logger.log_this_event(TTCN_Logger.Severity.EXECUTOR_CONFIGDATA) && (TTCN_Logger.get_emergency_logging() <= 0)) {
			return;
		}

		final TitanLogEvent event = new TitanLogEvent();
		fill_common_fields(event, TTCN_Logger.Severity.EXECUTOR_CONFIGDATA);
		final ExecutorConfigdata cfg = event.getlogEvent().getchoice().getexecutorEvent().getchoice().getexecutorConfigdata();
		cfg.getreason().assign(reason);
		if (str != null) {
			cfg.getparam__().get().assign(str);
		} else {
			cfg.getparam__().assign(template_sel.OMIT_VALUE);
		}

		log(event);
	}

	public void log_executor_component(final ExecutorComponent_reason.enum_type reason) {
		if (!TTCN_Logger.log_this_event(TTCN_Logger.Severity.EXECUTOR_COMPONENT) && (TTCN_Logger.get_emergency_logging() <= 0)) {
			return;
		}

		final TitanLogEvent event = new TitanLogEvent();
		fill_common_fields(event, TTCN_Logger.Severity.EXECUTOR_COMPONENT);
		final ExecutorComponent ec = event.getlogEvent().getchoice().getexecutorEvent().getchoice().getexecutorComponent();
		ec.getreason().assign(reason);
		ec.getcompref().assign(template_sel.OMIT_VALUE);

		log(event);
	}

	public void log_executor_misc(final ExecutorUnqualified_reason.enum_type reason, final String name, final String address, final int port) {
		if (!TTCN_Logger.log_this_event(TTCN_Logger.Severity.EXECUTOR_UNQUALIFIED) && (TTCN_Logger.get_emergency_logging() <= 0)) {
			return;
		}

		final TitanLogEvent event = new TitanLogEvent();
		fill_common_fields(event, TTCN_Logger.Severity.EXECUTOR_UNQUALIFIED);
		final ExecutorUnqualified ex = event.getlogEvent().getchoice().getexecutorEvent().getchoice().getexecutorMisc();
		ex.getreason().assign(reason);
		ex.getname().assign(name);
		ex.getaddr().assign(address);
		ex.getport__().assign(port);

		log(event);
	}

	public void log_extcommand(final TTCN_Logger.extcommand_t action, final String cmd) {
		if (!TTCN_Logger.log_this_event(TTCN_Logger.Severity.EXECUTOR_EXTCOMMAND) && (TTCN_Logger.get_emergency_logging() <= 0)) {
			return;
		}

		final TitanLogEvent event = new TitanLogEvent();
		fill_common_fields(event, TTCN_Logger.Severity.EXECUTOR_EXTCOMMAND);
		if (action == extcommand_t.EXTCOMMAND_START) {
			event.getlogEvent().getchoice().getexecutorEvent().getchoice().getextcommandStart().assign(cmd);
		} else {
			event.getlogEvent().getchoice().getexecutorEvent().getchoice().getextcommandSuccess().assign(cmd);
		}

		log(event);
	}

	public void log_matching_done(final TitanLoggerApi.MatchingDoneType_reason.enum_type reason, final String type, final int ptc, final String return_type) {
		if (!TTCN_Logger.log_this_event(TTCN_Logger.Severity.MATCHING_DONE) && (TTCN_Logger.get_emergency_logging() <= 0)) {
			return;
		}

		final TitanLogEvent event = new TitanLogEvent();
		fill_common_fields(event, TTCN_Logger.Severity.MATCHING_DONE);
		final MatchingDoneType mp = event.getlogEvent().getchoice().getmatchingEvent().getchoice().getmatchingDone();
		mp.getreason().assign(reason);
		mp.gettype__().assign(type);
		mp.getptc().assign(ptc);
		mp.getreturn__type().assign(return_type);

		log(event);
	}

	public void log_matching_problem(final TitanLoggerApi.MatchingProblemType_reason.enum_type reason, final TitanLoggerApi.MatchingProblemType_operation.enum_type operation, final boolean check, final boolean anyport, final String port_name) {
		if (!TTCN_Logger.log_this_event(TTCN_Logger.Severity.MATCHING_PROBLEM) && (TTCN_Logger.get_emergency_logging() <= 0)) {
			return;
		}

		final TitanLogEvent event = new TitanLogEvent();
		fill_common_fields(event, TTCN_Logger.Severity.MATCHING_PROBLEM);
		final MatchingProblemType mp = event.getlogEvent().getchoice().getmatchingEvent().getchoice().getmatchingProblem();
		mp.getreason().assign(reason);
		mp.getany__port().assign(anyport);
		mp.getcheck__().assign(check);
		mp.getoperation().assign(operation);
		mp.getport__name().assign(port_name);

		log(event);
	}

	public void log_random(final TitanLoggerApi.RandomAction.enum_type rndAction, final double value, final long seed) {
		if (!TTCN_Logger.log_this_event(Severity.FUNCTION_RND) && TTCN_Logger.get_emergency_logging() <= 0) {
			return;
		}

		final TitanLogEvent event = new TitanLogEvent();
		fill_common_fields(event, Severity.FUNCTION_RND);
		final FunctionEvent_choice_random r = event.getlogEvent().getchoice().getfunctionEvent().getchoice().getrandom();
		r.getoperation().assign(rndAction);
		r.getretval().assign(value);
		r.getintseed().assign((int)seed);

		log(event);
	}

	public void log_matching_failure(final TitanLoggerApi.PortType.enum_type port_type, final String port_name, final int compref, final TitanLoggerApi.MatchingFailureType_reason.enum_type reason, final TitanCharString info) {
		Severity sev;
		if (compref == TitanComponent.SYSTEM_COMPREF) {
			sev = (port_type == enum_type.message__) ? Severity.MATCHING_MMUNSUCC : Severity.MATCHING_PMUNSUCC;
		} else {
			sev = (port_type == enum_type.message__) ? Severity.MATCHING_MCUNSUCC : Severity.MATCHING_PCUNSUCC;
		}
		if (!TTCN_Logger.log_this_event(sev) && (TTCN_Logger.get_emergency_logging() <= 0)) {
			return;
		}

		final TitanLogEvent event = new TitanLogEvent();
		fill_common_fields(event, sev);
		final MatchingFailureType mf = event.getlogEvent().getchoice().getmatchingEvent().getchoice().getmatchingFailure();
		mf.getport__type().assign(port_type);
		mf.getport__name().assign(port_name);
		mf.getreason().assign(reason);

		if (compref == TitanComponent.SYSTEM_COMPREF) {
			mf.getchoice().getsystem__();
		} else {
			mf.getchoice().getcompref().assign(compref);
		}

		mf.getinfo().assign(info);

		log(event);
	}

	public void log_matching_success(final TitanLoggerApi.PortType.enum_type port_type, final String port_name, final int compref, final TitanCharString info) {
		Severity sev;
		if (compref == TitanComponent.SYSTEM_COMPREF) {
			sev = port_type == enum_type.message__ ? Severity.MATCHING_MMSUCCESS : Severity.MATCHING_PMSUCCESS;
		} else {
			sev = port_type == enum_type.message__ ? Severity.MATCHING_MCSUCCESS : Severity.MATCHING_PCSUCCESS;
		}

		if (TTCN_Logger.log_this_event(sev) && TTCN_Logger.get_emergency_logging() <= 0) {
			return;
		}

		final TitanLogEvent event = new TitanLogEvent();
		fill_common_fields(event, sev);
		final MatchingSuccessType ms = event.getlogEvent().getchoice().getmatchingEvent().getchoice().getmatchingSuccess();
		ms.getport__type().assign(port_type);
		ms.getport__name().assign(port_name);

		log(event);
	}

	public void log_portconnmap(final ParPort_operation.enum_type operation, final int src_compref, final String src_port, final int dst_compref, final String dst_port) {
		TTCN_Logger.Severity event_severity;
		switch (operation) {
		case connect__:
		case disconnect__:
			event_severity = Severity.PARALLEL_PORTCONN;
			break;
		case map__:
		case unmap__:
			event_severity = Severity.PARALLEL_PORTMAP;
			break;
		default:
			throw new TtcnError("Invalid operation");
		}

		if (!TTCN_Logger.log_this_event(event_severity) && (TTCN_Logger.get_emergency_logging() <= 0)) {
			return;
		}

		final TitanLogEvent event = new TitanLogEvent();
		fill_common_fields(event, event_severity);
		final ParPort pp = event.getlogEvent().getchoice().getparallelEvent().getchoice().getparallelPort();
		pp.getoperation().assign(operation);
		pp.getsrcCompref().assign(adjust_compref(src_compref));
		pp.getsrcPort().assign(src_port);
		pp.getdstCompref().assign(adjust_compref(dst_compref));
		pp.getdstPort().assign(dst_port);

		log(event);
	}

	public void log_parptc(final ParallelPTC_reason.enum_type reason, final String module, final String name, final int compref, final String compname, final String tc_loc, final int alive_pid, final int status) {
		TTCN_Logger.Severity event_severity;
		if (alive_pid > 0 && reason == ParallelPTC_reason.enum_type.function__finished) {
			event_severity = Severity.PARALLEL_UNQUALIFIED;
		} else {
			event_severity = Severity.PARALLEL_PTC;
		}

		if (!TTCN_Logger.log_this_event(event_severity) && (TTCN_Logger.get_emergency_logging() <= 0)) {
			return;
		}

		final TitanLogEvent event = new TitanLogEvent();
		fill_common_fields(event, event_severity);
		final ParallelPTC ptc = event.getlogEvent().getchoice().getparallelEvent().getchoice().getparallelPTC();
		ptc.getreason().assign(reason);
		ptc.getmodule__().assign(module);
		ptc.getname().assign(name);
		ptc.getcompref().assign(compref);
		ptc.gettc__loc().assign(tc_loc);
		ptc.getcompname().assign(compname);
		ptc.getalive__pid().assign(alive_pid);
		ptc.getstatus().assign(status);

		log(event);
	}

	public void log_port_misc(final TitanLoggerApi.Port__Misc_reason.enum_type reason, final String port_name, final int remote_component, final String remote_port, final String ip_address, final int tcp_port, final int new_size) {
		if (!TTCN_Logger.log_this_event(Severity.PORTEVENT_UNQUALIFIED) && (TTCN_Logger.get_emergency_logging() <= 0)) {
			return;
		}

		final TitanLogEvent event = new TitanLogEvent();
		fill_common_fields(event, Severity.PORTEVENT_UNQUALIFIED);
		final Port__Misc portMisc = event.getlogEvent().getchoice().getportEvent().getchoice().getportMisc();
		portMisc.getreason().assign(reason.ordinal());
		portMisc.getport__name().assign(port_name);
		portMisc.getremote__component().assign(remote_component);
		portMisc.getremote__port().assign(remote_port);
		portMisc.getip__address().assign(ip_address);
		portMisc.gettcp__port().assign(tcp_port);
		portMisc.getnew__size().assign(new_size);

		log(event);
	}
}
