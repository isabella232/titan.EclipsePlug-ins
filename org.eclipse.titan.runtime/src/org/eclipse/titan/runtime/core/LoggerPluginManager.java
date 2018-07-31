/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Stack;
import java.util.concurrent.LinkedBlockingQueue;

import org.eclipse.titan.runtime.core.Base_Template.template_sel;
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
import org.eclipse.titan.runtime.core.TtcnLogger.Severity;
import org.eclipse.titan.runtime.core.TtcnLogger.TTCN_Location;
import org.eclipse.titan.runtime.core.TtcnLogger.component_id_t;
import org.eclipse.titan.runtime.core.TtcnLogger.emergency_logging_behaviour_t;
import org.eclipse.titan.runtime.core.TtcnLogger.extcommand_t;

/**
 * The logger plugin manager, is responsible for managing all the runtime registered logger plug-ins
 *
 * FIXME lots to implement here, this is under construction right now
 *
 * @author Kristof Szabados
 */
public class LoggerPluginManager {
	private LinkedBlockingQueue<TitanLogEvent> ring_buffer = new LinkedBlockingQueue<TitanLoggerApi.TitanLogEvent>();

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

	private ArrayList<ILoggerPlugin> plugins_ = new ArrayList<ILoggerPlugin>();
	
	private LinkedList<LogEntry> entry_list_ = new LinkedList<LogEntry>();

	public LoggerPluginManager() {
		plugins_.add(new LegacyLogger());
	}

	public void ring_buffer_dump(final boolean do_close_file) {
		if (TtcnLogger.get_emergency_logging_behaviour() == emergency_logging_behaviour_t.BUFFER_ALL) {
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
				if (entry.event_.getSeverity().getInt() == TtcnLogger.Severity.EXECUTOR_LOGOPTIONS.ordinal()) {
					String new_log_message = TtcnLogger.get_logger_settings_str();
					entry.event_.getLogEvent().getChoice().getExecutorEvent().getChoice().getLogOptions().assign(new_log_message);
					new_log_message = null;
				}
				internal_log_to_all(entry.event_, true, false, false);
			}
		}
		entry_list_.clear();
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
		}
		// Init phase, log prebuffered events first if any.
		internal_log_prebuff_logevent();

		if (TtcnLogger.get_emergency_logging() == 0) {
			// emergency logging is not needed
			internal_log_to_all(event, false, false, false);

			return;
		}

		final int severityIndex = event.getSeverity().getInt();
		final Severity severity = Severity.values()[severityIndex];

		if (TtcnLogger.get_emergency_logging_behaviour() == emergency_logging_behaviour_t.BUFFER_MASKED) {
			internal_log_to_all(event, true, false, false);

			
			if (!TtcnLogger.should_log_to_file(severity) &&
				TtcnLogger.should_log_to_emergency(severity)) {
				ring_buffer.offer(event);
			}
		} else if (TtcnLogger.get_emergency_logging_behaviour() == emergency_logging_behaviour_t.BUFFER_ALL) {
			if (ring_buffer.remainingCapacity() == 0) {
				TitanLoggerApi.TitanLogEvent ring_event;
				ring_event = ring_buffer.poll();
				if (ring_event != null) {
					internal_log_to_all(ring_event, true, false, false);
				}
			}

			ring_buffer.offer(event);
		}

		if (severity == Severity.ERROR_UNQUALIFIED || 
				(TtcnLogger.get_emergency_logging_for_fail_verdict() &&
						severity == Severity.VERDICTOP_SETVERDICT &&
						event.getLogEvent().getChoice().getVerdictOp().getChoice().getSetVerdict().getNewVerdict().operatorEquals(TitanLoggerApi.Verdict.enum_type.v3fail)) 
				) {
			TitanLoggerApi.TitanLogEvent ring_event;
			while (!ring_buffer.isEmpty()) {
				ring_event = ring_buffer.poll();
				if (ring_event != null) {
					if (TtcnLogger.get_emergency_logging_behaviour() == emergency_logging_behaviour_t.BUFFER_MASKED) {
						internal_log_to_all(ring_event, true, true, false);
					} else if (TtcnLogger.get_emergency_logging_behaviour() == emergency_logging_behaviour_t.BUFFER_ALL) {
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

	public boolean set_disk_full_action(final component_id_t comp, final TtcnLogger.disk_full_action_t p_disk_full_action) {
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
			//FIXME: assert(this->n_plugins_ > 0)
			return;
		}
		for (int i = 0; i < plugins_.size(); i++) {
			plugins_.get(i).open_file(is_first.get().booleanValue());
			if (plugins_.get(i).is_configured()) {
				free_entry_list = true;
				for (final LogEntry entry : entry_list_) {
					if (entry.event_.getSeverity().getInt() == TtcnLogger.Severity.EXECUTOR_LOGOPTIONS.ordinal()) {
						String new_log_message = TtcnLogger.get_logger_settings_str();
						entry.event_.getLogEvent().getChoice().getExecutorEvent().getChoice().getLogOptions().assign(new_log_message);
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
		if (TtcnLogger.log_this_event(msg_severity)) {
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
		if (current_event.get() == null) {
			log_unhandled_event(Severity.WARNING_UNQUALIFIED, "TTCN_Logger::end_event(): not in event.");
			return;
		}

		switch (current_event.get().event_destination) {
		case ED_NONE:
			break;
		case ED_FILE:
			//FIXME implement
			//TODO temporary solution for filtering
			if (TtcnLogger.log_this_event(current_event.get().severity)) {
				log_unhandled_event(current_event.get().severity, current_event.get().buffer.toString());
			}
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
		if (current_event.get() == null) {
			log_unhandled_event(Severity.WARNING_UNQUALIFIED, "TTCN_Logger::end_event_log2str(): not in event.");
			return new TitanCharString();
		}

		final TitanCharString ret_val = new TitanCharString(current_event.get().buffer);
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
		if (current_event.get() != null) {
			if (current_event.get().event_destination == event_destination_t.ED_NONE) {
				return;
			}
			if (string == null) {
				current_event.get().buffer.append("<NULL pointer>");
			} else {
				current_event.get().buffer.append(string);
			}
		} else {
			log_unhandled_event(Severity.WARNING_UNQUALIFIED, "TTCN_Logger::log_event_str(): not in event.");
		}
	}

	public void log_char(final char c) {
		if (current_event.get() != null) {
			if (current_event.get().event_destination == event_destination_t.ED_NONE || c == '\0') {
				return;
			}
			current_event.get().buffer.append(c);
		} else {
			log_unhandled_event(Severity.WARNING_UNQUALIFIED, "TTCN_Logger::log_char(): not in event.");
		}
	}

	public void log_event_va_list(final String formatString, final Object... args) {
		if (current_event.get() != null) {
			if (current_event.get().event_destination == event_destination_t.ED_NONE) {
				return;
			}
			current_event.get().buffer.append(String.format(Locale.US, formatString, args));
		} else {
			log_unhandled_event(Severity.WARNING_UNQUALIFIED, "TTCN_Logger::log_event_va_list(): not in event.");
		}
	}

	public void log_unhandled_event(final Severity severity, final String message) {
		if (!TtcnLogger.log_this_event(severity) && TtcnLogger.get_emergency_logging() <= 0) {
			return;
		}

		final TitanLogEvent event = new TitanLogEvent();
		fill_common_fields(event, severity);
		event.getLogEvent().getChoice().getUnhandledEvent().assign(message);

		log(event);
	}

	public void log_log_options(final String message) {
		if (!TtcnLogger.log_this_event(Severity.EXECUTOR_LOGOPTIONS) && TtcnLogger.get_emergency_logging() <= 0) {
			return;
		}

		final TitanLogEvent event = new TitanLogEvent();
		fill_common_fields(event, Severity.EXECUTOR_LOGOPTIONS);

		event.getLogEvent().getChoice().getExecutorEvent().getChoice().getLogOptions().assign(message);

		log(event);
	}

	public void log_timer_read(final String timer_name, final double timeout_val) {
		if (!TtcnLogger.log_this_event(Severity.TIMEROP_READ) && TtcnLogger.get_emergency_logging() <= 0) {
			return;
		}

		final TitanLogEvent event = new TitanLogEvent();
		fill_common_fields(event, Severity.TIMEROP_READ);
		final TimerType timer = event.getLogEvent().getChoice().getTimerEvent().getChoice().getReadTimer();
		timer.getName().assign(timer_name);
		timer.getValue__().assign(timeout_val);

		log(event);
	}

	public void log_timer_start(final String timer_name, final double start_val) {
		if (!TtcnLogger.log_this_event(Severity.TIMEROP_START) && TtcnLogger.get_emergency_logging() <= 0) {
			return;
		}

		final TitanLogEvent event = new TitanLogEvent();
		fill_common_fields(event, Severity.TIMEROP_START);
		final TimerType timer = event.getLogEvent().getChoice().getTimerEvent().getChoice().getStartTimer();
		timer.getName().assign(timer_name);
		timer.getValue__().assign(start_val);

		log(event);
	}

	public void log_timer_guard(final double start_val) {
		if (!TtcnLogger.log_this_event(Severity.TIMEROP_GUARD) && TtcnLogger.get_emergency_logging() <= 0) {
			return;
		}

		final TitanLogEvent event = new TitanLogEvent();
		fill_common_fields(event, Severity.TIMEROP_GUARD);
		final TimerGuardType timer = event.getLogEvent().getChoice().getTimerEvent().getChoice().getGuardTimer();
		timer.getValue__().assign(start_val);

		log(event);
	}

	public void log_timer_stop(final String timer_name, final double stop_val) {
		if (!TtcnLogger.log_this_event(Severity.TIMEROP_STOP) && TtcnLogger.get_emergency_logging() <= 0) {
			return;
		}

		final TitanLogEvent event = new TitanLogEvent();
		fill_common_fields(event, Severity.TIMEROP_STOP);
		final TimerType timer = event.getLogEvent().getChoice().getTimerEvent().getChoice().getStopTimer();
		timer.getName().assign(timer_name);
		timer.getValue__().assign(stop_val);

		log(event);
	}

	public void log_timer_timeout(final String timer_name, final double timeout_val) {
		if (!TtcnLogger.log_this_event(Severity.TIMEROP_TIMEOUT) && TtcnLogger.get_emergency_logging() <= 0) {
			return;
		}

		final TitanLogEvent event = new TitanLogEvent();
		fill_common_fields(event, Severity.TIMEROP_TIMEOUT);
		final TimerType timer = event.getLogEvent().getChoice().getTimerEvent().getChoice().getTimeoutTimer();
		timer.getName().assign(timer_name);
		timer.getValue__().assign(timeout_val);

		log(event);
	}

	public void log_timer_any_timeout() {
		if (!TtcnLogger.log_this_event(Severity.TIMEROP_TIMEOUT) && TtcnLogger.get_emergency_logging() <= 0) {
			return;
		}

		final TitanLogEvent event = new TitanLogEvent();
		fill_common_fields(event, Severity.TIMEROP_TIMEOUT);
		event.getLogEvent().getChoice().getTimerEvent().getChoice().getTimeoutAnyTimer().assign(TitanNull_Type.NULL_VALUE);

		log(event);
	}

	public void log_timer_unqualified(final String message) {
		if (!TtcnLogger.log_this_event(Severity.TIMEROP_UNQUALIFIED) && TtcnLogger.get_emergency_logging() <= 0) {
			return;
		}

		final TitanLogEvent event = new TitanLogEvent();
		fill_common_fields(event, Severity.TIMEROP_UNQUALIFIED);
		event.getLogEvent().getChoice().getTimerEvent().getChoice().getUnqualifiedTimer().assign(message);

		log(event);
	}

	public void log_matching_timeout(final String timer_name) {
		if (!TtcnLogger.log_this_event(Severity.MATCHING_PROBLEM) && TtcnLogger.get_emergency_logging() <= 0) {
			return;
		}

		final TitanLogEvent event = new TitanLogEvent();
		fill_common_fields(event, Severity.MATCHING_PROBLEM);
		final MatchingTimeout mt = event.getLogEvent().getChoice().getMatchingEvent().getChoice().getMatchingTimeout();
		if (timer_name != null) {
			mt.getTimer__name().get().assign(timer_name);
		} else {
			mt.getTimer__name().assign(template_sel.OMIT_VALUE);
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

		if (!TtcnLogger.log_this_event(sev) && TtcnLogger.get_emergency_logging() <= 0) {
			return;
		}

		final TitanLogEvent event = new TitanLogEvent();
		fill_common_fields(event, sev);
		final Port__Queue portQueue = event.getLogEvent().getChoice().getPortEvent().getChoice().getPortQueue();
		portQueue.getOperation().assign(operation.ordinal());
		portQueue.getPort__name().assign(port_name);
		portQueue.getCompref().assign(adjust_compref(componentReference));
		portQueue.getMsgid().assign(id);
		portQueue.getAddress__().assign(address);
		portQueue.getParam__().assign(parameter);

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
		if (!TtcnLogger.log_this_event(Severity.PORTEVENT_STATE)) {
			return;
		}

		final TitanLogEvent event = new TitanLogEvent();
		fill_common_fields(event, Severity.PORTEVENT_STATE);
		final Port__State ps = event.getLogEvent().getChoice().getPortEvent().getChoice().getPortState();
		ps.getOperation().assign(operation);
		ps.getPort__name().assign(portname);

		log(event);
	}

	public void log_procport_send(final String portname, final TitanLoggerApi.Port__oper.enum_type operation, final int componentReference, final TitanCharString system, final TitanCharString parameter) {
		final Severity severity = componentReference == TitanComponent.SYSTEM_COMPREF ? Severity.PORTEVENT_PMOUT : Severity.PORTEVENT_PCOUT;
		if (!TtcnLogger.log_this_event(severity) && TtcnLogger.get_emergency_logging() <= 0) {
			return;
		}

		final TitanLogEvent event = new TitanLogEvent();
		fill_common_fields(event, severity);
		final Proc__port__out pt = event.getLogEvent().getChoice().getPortEvent().getChoice().getProcPortSend();
		pt.getOperation().assign(operation);
		pt.getPort__name().assign(portname);
		pt.getCompref().assign(componentReference);
		if (componentReference == TitanComponent.SYSTEM_COMPREF) {
			pt.getSys__name().assign(system);
		}
		pt.getParameter().assign(parameter);

		log(event);
	}

	public void log_procport_recv(final String portname, final TitanLoggerApi.Port__oper.enum_type operation, final int componentReference, final boolean check, final TitanCharString parameter, final int id) {
		final Severity severity = componentReference == TitanComponent.SYSTEM_COMPREF ? Severity.PORTEVENT_PMIN : Severity.PORTEVENT_PCIN;
		if (!TtcnLogger.log_this_event(severity) && TtcnLogger.get_emergency_logging() <= 0) {
			return;
		}

		final TitanLogEvent event = new TitanLogEvent();
		fill_common_fields(event, severity);
		final Proc__port__in pt = event.getLogEvent().getChoice().getPortEvent().getChoice().getProcPortRecv();
		pt.getOperation().assign(operation);
		pt.getPort__name().assign(portname);
		pt.getCompref().assign(componentReference);
		pt.getCheck__().assign(check);
		pt.getParameter().assign(parameter);
		pt.getMsgid().assign(id);

		log(event);
	}

	public void log_msgport_send(final String portname, final int componentReference, final TitanCharString parameter) {
		final Severity severity = componentReference == TitanComponent.SYSTEM_COMPREF ? Severity.PORTEVENT_MMSEND : Severity.PORTEVENT_MCSEND;
		if (!TtcnLogger.log_this_event(severity) && TtcnLogger.get_emergency_logging() <= 0) {
			return;
		}

		final TitanLogEvent event = new TitanLogEvent();
		fill_common_fields(event, severity);
		final Msg__port__send ms = event.getLogEvent().getChoice().getPortEvent().getChoice().getMsgPortSend();
		ms.getPort__name().assign(portname);
		ms.getCompref().assign(componentReference);
		ms.getParameter().assign(parameter);

		log(event);
	}

	public void log_msgport_recv(final String portname, final TitanLoggerApi.Msg__port__recv_operation.enum_type operation, final int componentReference, final TitanCharString system, final TitanCharString parameter, final int id) {
		final Severity severity = componentReference == TitanComponent.SYSTEM_COMPREF ? Severity.PORTEVENT_MMRECV : Severity.PORTEVENT_MCRECV;
		if (!TtcnLogger.log_this_event(severity) && TtcnLogger.get_emergency_logging() <= 0) {
			return;
		}

		final TitanLogEvent event = new TitanLogEvent();
		fill_common_fields(event, severity);
		final Msg__port__recv ms = event.getLogEvent().getChoice().getPortEvent().getChoice().getMsgPortRecv();
		ms.getPort__name().assign(portname);
		ms.getCompref().assign(componentReference);
		if (componentReference == TitanComponent.SYSTEM_COMPREF) {
			ms.getSys__name().assign(system);
		}
		ms.getOperation().assign(operation);
		ms.getMsgid().assign(id);
		ms.getParameter().assign(parameter);

		log(event);
	}

	public void log_dualport_map(final boolean incoming, final String target_type, final TitanCharString value, final int id) {
		final Severity severity = incoming ? Severity.PORTEVENT_DUALRECV : Severity.PORTEVENT_DUALSEND;
		if (!TtcnLogger.log_this_event(severity) && TtcnLogger.get_emergency_logging() <= 0) {
			return;
		}

		final TitanLogEvent event = new TitanLogEvent();
		fill_common_fields(event, severity);
		final Dualface__mapped dual = event.getLogEvent().getChoice().getPortEvent().getChoice().getDualMapped();
		dual.getIncoming().assign(incoming);
		dual.getTarget__type().assign(target_type);
		dual.getValue__().assign(value);
		dual.getMsgid().assign(id);

		log(event);
	}

	public void log_dualport_discard(final boolean incoming, final String target_type, final TitanCharString port_name, final boolean unhandled) {
		final Severity severity = incoming ? Severity.PORTEVENT_DUALRECV : Severity.PORTEVENT_DUALSEND;
		if (!TtcnLogger.log_this_event(severity) && TtcnLogger.get_emergency_logging() <= 0) {
			return;
		}

		final TitanLogEvent event = new TitanLogEvent();
		fill_common_fields(event, severity);
		final Dualface__discard dual = event.getLogEvent().getChoice().getPortEvent().getChoice().getDualDiscard();
		dual.getIncoming().assign(incoming);
		dual.getTarget__type().assign(target_type);
		dual.getPort__name().assign(port_name);
		dual.getUnhandled().assign(unhandled);

		log(event);
	}

	public void log_dualport_discard(final boolean incoming, final String target_type, final String port_name, final boolean unhandled) {
		final Severity severity = incoming ? Severity.PORTEVENT_DUALRECV : Severity.PORTEVENT_DUALSEND;
		if (!TtcnLogger.log_this_event(severity) && TtcnLogger.get_emergency_logging() <= 0) {
			return;
		}

		final TitanLogEvent event = new TitanLogEvent();
		fill_common_fields(event, severity);
		final Dualface__discard dual = event.getLogEvent().getChoice().getPortEvent().getChoice().getDualDiscard();
		dual.getIncoming().assign(incoming);
		dual.getTarget__type().assign(target_type);
		dual.getPort__name().assign(port_name);
		dual.getUnhandled().assign(unhandled);

		log(event);
	}

	public void log_setstate(final String port_name, final TitanPort.translation_port_state state, final TitanCharString info) {
		if (!TtcnLogger.log_this_event(Severity.PORTEVENT_SETSTATE) && TtcnLogger.get_emergency_logging() <= 0) {
			return;
		}

		final TitanLogEvent event = new TitanLogEvent();
		fill_common_fields(event, Severity.PORTEVENT_SETSTATE);

		final Setstate setstate = event.getLogEvent().getChoice().getPortEvent().getChoice().getSetState();
		setstate.getPort__name().assign(port_name);
		setstate.getInfo().assign(info);
		switch (state) {
		case UNSET:
			setstate.getState().assign("unset");
			break;
		case TRANSLATED:
			setstate.getState().assign("translated");
			break;
		case NOT_TRANSLATED:
			setstate.getState().assign("not translated");
			break;
		case FRAGMENTED:
			setstate.getState().assign("fragemnted");
			break;
		case PARTIALLY_TRANSLATED:
			setstate.getState().assign("partially translated");
			break;
		case DISCARDED:
			setstate.getState().assign("discarded");
			break;
		default:
			break;
		}

		log(event);
	}

	public void log_setverdict(final VerdictTypeEnum newVerdict, final VerdictTypeEnum oldVerdict, final VerdictTypeEnum localVerdict,
			final String oldReason, final String newReason) {
		if (!TtcnLogger.log_this_event(Severity.VERDICTOP_SETVERDICT) && TtcnLogger.get_emergency_logging() <= 0) {
			return;
		}

		final TitanLogEvent event = new TitanLogEvent();
		fill_common_fields(event, Severity.VERDICTOP_SETVERDICT);
		final SetVerdictType set = event.getLogEvent().getChoice().getVerdictOp().getChoice().getSetVerdict();
		set.getNewVerdict().assign(newVerdict.ordinal());
		set.getOldVerdict().assign(oldVerdict.ordinal());
		set.getLocalVerdict().assign(localVerdict.ordinal());
		if (oldReason != null) {
			set.getOldReason().get().assign(oldReason);
		} else {
			set.getOldReason().assign(template_sel.OMIT_VALUE);
		}
		if (newReason != null) {
			set.getNewReason().get().assign(newReason);
		} else {
			set.getNewReason().assign(template_sel.OMIT_VALUE);
		}

		log(event);
	}

	public void log_getverdict(final VerdictTypeEnum verdict) {
		if (!TtcnLogger.log_this_event(Severity.VERDICTOP_GETVERDICT) && TtcnLogger.get_emergency_logging() <= 0) {
			return;
		}

		final TitanLogEvent event = new TitanLogEvent();
		fill_common_fields(event, Severity.VERDICTOP_GETVERDICT);
		event.getLogEvent().getChoice().getVerdictOp().getChoice().getGetVerdict().assign(verdict.ordinal());

		log(event);
	}

	private void fill_common_fields(final TitanLogEvent event, final Severity severity) {
		//FIXME implement the rest
		final long timestamp = System.currentTimeMillis();
		event.getTimestamp().assign(new TimestampType(new TitanInteger((int)(timestamp / 1000)), new TitanInteger((int)(timestamp % 1000))));

		final TitanLogEvent_sourceInfo__list srcinfo = event.getSourceInfo__list();
		if (TTCN_Location.actualSize.get() == 0) {
			srcinfo.assign(TitanNull_Type.NULL_VALUE);
		} else {
			final int localSize = TTCN_Location.actualSize.get();
			final ArrayList<TTCN_Location> localLocations = TTCN_Location.locations.get();
			for (int i = 0; i < localSize; i++) {
				final LocationInfo loc = srcinfo.getAt(i);
				final TTCN_Location temp = localLocations.get(i);

				loc.getFilename().assign(temp.file_name);
				loc.getLine().assign(temp.line_number);
				loc.getEnt__type().assign(temp.entity_type.ordinal());
				loc.getEnt__name().assign(temp.entity_name);
			}
		}

		event.getSeverity().assign(severity.ordinal());
	}

	public void log_testcase_started(final String module_name, final String definition_name) {
		if (!TtcnLogger.log_this_event(Severity.TESTCASE_START) && TtcnLogger.get_emergency_logging() <= 0) {
			return;
		}

		final TitanLogEvent event = new TitanLogEvent();
		fill_common_fields(event, Severity.TESTCASE_START);
		final QualifiedName qname = event.getLogEvent().getChoice().getTestcaseOp().getChoice().getTestcaseStarted();
		qname.getModule__name().assign(module_name);
		qname.getTestcase__name().assign(definition_name);

		log(event);
	}

	public void log_testcase_finished(final String module_name, final String definition_name, final VerdictTypeEnum verdict, final String reason) {
		if (!TtcnLogger.log_this_event(Severity.TESTCASE_FINISH) && TtcnLogger.get_emergency_logging() <= 0) {
			return;
		}

		final TitanLogEvent event = new TitanLogEvent();
		fill_common_fields(event, Severity.TESTCASE_FINISH);
		final TestcaseType testcase = event.getLogEvent().getChoice().getTestcaseOp().getChoice().getTestcaseFinished();
		final QualifiedName qname = testcase.getName();
		qname.getModule__name().assign(module_name);
		qname.getTestcase__name().assign(definition_name);
		testcase.getVerdict().assign(verdict.ordinal());
		testcase.getReason().assign(reason);

		log(event);
	}

	public void log_final_verdict(final boolean is_ptc, final TitanVerdictType.VerdictTypeEnum ptc_verdict, final TitanVerdictType.VerdictTypeEnum local_verdict, final TitanVerdictType.VerdictTypeEnum new_verdict, final String verdict_reason, final int notification, final int ptc_compref, final String ptc_name) {
		if (!TtcnLogger.log_this_event(Severity.VERDICTOP_FINAL) && TtcnLogger.get_emergency_logging() <= 0) {
			return;
		}

		final TitanLogEvent event = new TitanLogEvent();
		fill_common_fields(event, Severity.VERDICTOP_FINAL);
		final FinalVerdictType finalVerdict = event.getLogEvent().getChoice().getVerdictOp().getChoice().getFinalVerdict();
		if (notification >= 0) {
			finalVerdict.getChoice().getNotification().assign(notification);
		} else {
			final FinalVerdictInfo info = finalVerdict.getChoice().getInfo();

			info.getIs__ptc().assign(is_ptc);
			info.getPtc__verdict().assign(ptc_verdict.ordinal());
			info.getLocal__verdict().assign(local_verdict.ordinal());
			info.getNew__verdict().assign(new_verdict.ordinal());
			info.getPtc__compref().get().assign(ptc_compref);
			if (verdict_reason == null) {
				info.getVerdict__reason().assign(template_sel.OMIT_VALUE);
			} else {
				info.getVerdict__reason().get().assign(verdict_reason);
			}
			if (ptc_name == null) {
				info.getPtc__name().assign(template_sel.OMIT_VALUE);
			} else {
				info.getPtc__name().get().assign(ptc_name);
			}
		}

		log(event);
	}

	public void log_controlpart_start_stop(final String moduleName, final boolean finished) {
		if (!TtcnLogger.log_this_event(Severity.STATISTICS_UNQUALIFIED) && TtcnLogger.get_emergency_logging() <= 0) {
			return;
		}

		final TitanLogEvent event = new TitanLogEvent();
		fill_common_fields(event, Severity.STATISTICS_UNQUALIFIED);
		final StatisticsType stats = event.getLogEvent().getChoice().getStatistics();
		if (finished) {
			stats.getChoice().getControlpartFinish().assign(moduleName);
		} else {
			stats.getChoice().getControlpartStart().assign(moduleName);
		}

		log(event);
	}

	public void log_controlpart_errors(final int error_count) {
		if (!TtcnLogger.log_this_event(Severity.STATISTICS_UNQUALIFIED) && TtcnLogger.get_emergency_logging() <= 0) {
			return;
		}

		final TitanLogEvent event = new TitanLogEvent();
		fill_common_fields(event, Severity.STATISTICS_UNQUALIFIED);
		final StatisticsType stats = event.getLogEvent().getChoice().getStatistics();
		stats.getChoice().getControlpartErrors().assign(error_count);

		log(event);
	}

	public void log_verdict_statistics(final int none_count, final double none_percent,
			final int pass_count, final double pass_percent,
			final int inconc_count, final double inconc_percent,
			final int fail_count, final double fail_percent,
			final int error_count, final double error_percent) {
		if (!TtcnLogger.log_this_event(Severity.STATISTICS_VERDICT) && TtcnLogger.get_emergency_logging() <= 0) {
			return;
		}

		final TitanLogEvent event = new TitanLogEvent();
		fill_common_fields(event, Severity.STATISTICS_VERDICT);
		final StatisticsType stats = event.getLogEvent().getChoice().getStatistics();
		final StatisticsType_choice_verdictStatistics verdictStats = stats.getChoice().getVerdictStatistics();
		verdictStats.getNone__().assign(none_count);
		verdictStats.getNonePercent().assign(none_percent);
		verdictStats.getPass__().assign(pass_count);
		verdictStats.getPassPercent().assign(pass_percent);
		verdictStats.getInconc__().assign(inconc_count);
		verdictStats.getInconcPercent().assign(inconc_percent);
		verdictStats.getFail__().assign(fail_count);
		verdictStats.getFailPercent().assign(fail_percent);
		verdictStats.getError__().assign(error_count);
		verdictStats.getErrorPercent().assign(error_percent);

		log(event);
	}

	public void log_defaultop_activate(final String name, final int id) {
		if (!TtcnLogger.log_this_event(Severity.DEFAULTOP_ACTIVATE) && TtcnLogger.get_emergency_logging() <= 0) {
			return;
		}

		final TitanLogEvent event = new TitanLogEvent();
		fill_common_fields(event, TtcnLogger.Severity.DEFAULTOP_ACTIVATE);
		final DefaultOp defaultop = event.getLogEvent().getChoice().getDefaultEvent().getChoice().getDefaultopActivate();
		defaultop.getName().assign(name);
		defaultop.getId().assign(id);
		defaultop.getEnd().assign(DefaultEnd.enum_type.UNKNOWN_VALUE);

		log(event);
	}

	public void log_defaultop_deactivate(final String name, final int id) {
		if (!TtcnLogger.log_this_event(Severity.DEFAULTOP_DEACTIVATE) && TtcnLogger.get_emergency_logging() <= 0) {
			return;
		}

		final TitanLogEvent event = new TitanLogEvent();
		fill_common_fields(event, TtcnLogger.Severity.DEFAULTOP_DEACTIVATE);
		final DefaultOp defaultop = event.getLogEvent().getChoice().getDefaultEvent().getChoice().getDefaultopDeactivate();
		defaultop.getName().assign(name);
		defaultop.getId().assign(id);
		defaultop.getEnd().assign(DefaultEnd.enum_type.UNKNOWN_VALUE);

		log(event);
	}

	public void log_defaultop_exit(final String name, final int id, final int x) {
		if (!TtcnLogger.log_this_event(Severity.DEFAULTOP_EXIT) && TtcnLogger.get_emergency_logging() <= 0) {
			return;
		}

		final TitanLogEvent event = new TitanLogEvent();
		fill_common_fields(event, TtcnLogger.Severity.DEFAULTOP_EXIT);
		final DefaultOp defaultop = event.getLogEvent().getChoice().getDefaultEvent().getChoice().getDefaultopExit();
		defaultop.getName().assign(name);
		defaultop.getId().assign(id);
		defaultop.getEnd().assign(x);

		log(event);
	}

	public void log_executor_runtime(final TitanLoggerApi.ExecutorRuntime_reason.enum_type reason) {
		if (!TtcnLogger.log_this_event(TtcnLogger.Severity.EXECUTOR_RUNTIME) && (TtcnLogger.get_emergency_logging() <= 0)) {
			return;
		}

		final TitanLogEvent event = new TitanLogEvent();
		fill_common_fields(event, TtcnLogger.Severity.EXECUTOR_RUNTIME);
		final ExecutorRuntime exec = event.getLogEvent().getChoice().getExecutorEvent().getChoice().getExecutorRuntime();
		exec.getReason().assign(reason);
		exec.getModule__name().assign(template_sel.OMIT_VALUE);
		exec.getTestcase__name().assign(template_sel.OMIT_VALUE);
		exec.getPid().assign(template_sel.OMIT_VALUE);
		exec.getFd__setsize().assign(template_sel.OMIT_VALUE);

		log(event);
	}

	public void log_hc_start(final String host) {
		if (!TtcnLogger.log_this_event(TtcnLogger.Severity.EXECUTOR_RUNTIME) && (TtcnLogger.get_emergency_logging() <= 0)) {
			return;
		}

		final TitanLogEvent event = new TitanLogEvent();
		fill_common_fields(event, TtcnLogger.Severity.EXECUTOR_RUNTIME);
		final ExecutorRuntime exec = event.getLogEvent().getChoice().getExecutorEvent().getChoice().getExecutorRuntime();
		exec.getReason().assign(ExecutorRuntime_reason.enum_type.host__controller__started);
		exec.getModule__name().get().assign(host);
		exec.getTestcase__name().assign(template_sel.OMIT_VALUE);
		exec.getPid().assign(template_sel.OMIT_VALUE);
		exec.getFd__setsize().assign(template_sel.OMIT_VALUE);

		log(event);
	}

	public void log_testcase_exec(final String testcase, final String module) {
		if (!TtcnLogger.log_this_event(TtcnLogger.Severity.EXECUTOR_RUNTIME) && (TtcnLogger.get_emergency_logging() <= 0)) {
			return;
		}

		final TitanLogEvent event = new TitanLogEvent();
		fill_common_fields(event, TtcnLogger.Severity.EXECUTOR_RUNTIME);
		final ExecutorRuntime exec = event.getLogEvent().getChoice().getExecutorEvent().getChoice().getExecutorRuntime();
		exec.getReason().assign(ExecutorRuntime_reason.enum_type.executing__testcase__in__module);
		exec.getModule__name().get().assign(module);
		exec.getTestcase__name().get().assign(testcase);
		exec.getPid().assign(template_sel.OMIT_VALUE);
		exec.getFd__setsize().assign(template_sel.OMIT_VALUE);

		log(event);
	}

	public void log_module_init(final String module, final boolean finish) {
		if (!TtcnLogger.log_this_event(TtcnLogger.Severity.EXECUTOR_RUNTIME) && (TtcnLogger.get_emergency_logging() <= 0)) {
			return;
		}

		final TitanLogEvent event = new TitanLogEvent();
		fill_common_fields(event, TtcnLogger.Severity.EXECUTOR_RUNTIME);
		final ExecutorRuntime exec = event.getLogEvent().getChoice().getExecutorEvent().getChoice().getExecutorRuntime();
		if (finish) {
			exec.getReason().assign(ExecutorRuntime_reason.enum_type.initialization__of__module__finished);
		} else {
			exec.getReason().assign(ExecutorRuntime_reason.enum_type.initializing__module);
		}
		
		exec.getModule__name().get().assign(module);
		exec.getTestcase__name().assign(template_sel.OMIT_VALUE);
		exec.getPid().assign(template_sel.OMIT_VALUE);
		exec.getFd__setsize().assign(template_sel.OMIT_VALUE);

		log(event);
	}

	public void log_mtc_created(final long pid) {
		if (!TtcnLogger.log_this_event(TtcnLogger.Severity.EXECUTOR_RUNTIME) && (TtcnLogger.get_emergency_logging() <= 0)) {
			return;
		}

		final TitanLogEvent event = new TitanLogEvent();
		fill_common_fields(event, TtcnLogger.Severity.EXECUTOR_RUNTIME);
		final ExecutorRuntime exec = event.getLogEvent().getChoice().getExecutorEvent().getChoice().getExecutorRuntime();
		exec.getReason().assign(ExecutorRuntime_reason.enum_type.mtc__created);
		
		exec.getModule__name().assign(template_sel.OMIT_VALUE);
		exec.getTestcase__name().assign(template_sel.OMIT_VALUE);
		exec.getPid().get().assign((int)pid);
		exec.getFd__setsize().assign(template_sel.OMIT_VALUE);

		log(event);
	}

	public void log_configdata(final ExecutorConfigdata_reason.enum_type reason, final String str) {
		if (!TtcnLogger.log_this_event(TtcnLogger.Severity.EXECUTOR_CONFIGDATA) && (TtcnLogger.get_emergency_logging() <= 0)) {
			return;
		}

		final TitanLogEvent event = new TitanLogEvent();
		fill_common_fields(event, TtcnLogger.Severity.EXECUTOR_CONFIGDATA);
		final ExecutorConfigdata cfg = event.getLogEvent().getChoice().getExecutorEvent().getChoice().getExecutorConfigdata();
		cfg.getReason().assign(reason);
		if (str != null) {
			cfg.getParam__().get().assign(str);
		} else {
			cfg.getParam__().assign(template_sel.OMIT_VALUE);
		}

		log(event);
	}

	public void log_executor_component(final ExecutorComponent_reason.enum_type reason) {
		if (!TtcnLogger.log_this_event(TtcnLogger.Severity.EXECUTOR_COMPONENT) && (TtcnLogger.get_emergency_logging() <= 0)) {
			return;
		}

		final TitanLogEvent event = new TitanLogEvent();
		fill_common_fields(event, TtcnLogger.Severity.EXECUTOR_COMPONENT);
		final ExecutorComponent ec = event.getLogEvent().getChoice().getExecutorEvent().getChoice().getExecutorComponent();
		ec.getReason().assign(reason);
		ec.getCompref().assign(template_sel.OMIT_VALUE);

		log(event);
	}

	public void log_executor_misc(final ExecutorUnqualified_reason.enum_type reason, final String name, final String address, final int port) {
		if (!TtcnLogger.log_this_event(TtcnLogger.Severity.EXECUTOR_UNQUALIFIED) && (TtcnLogger.get_emergency_logging() <= 0)) {
			return;
		}

		final TitanLogEvent event = new TitanLogEvent();
		fill_common_fields(event, TtcnLogger.Severity.EXECUTOR_UNQUALIFIED);
		final ExecutorUnqualified ex = event.getLogEvent().getChoice().getExecutorEvent().getChoice().getExecutorMisc();
		ex.getReason().assign(reason);
		ex.getName().assign(name);
		ex.getAddr().assign(address);
		ex.getPort__().assign(port);

		log(event);
	}

	public void log_extcommand(final TtcnLogger.extcommand_t action, final String cmd) {
		if (!TtcnLogger.log_this_event(TtcnLogger.Severity.EXECUTOR_EXTCOMMAND) && (TtcnLogger.get_emergency_logging() <= 0)) {
			return;
		}

		final TitanLogEvent event = new TitanLogEvent();
		fill_common_fields(event, TtcnLogger.Severity.EXECUTOR_EXTCOMMAND);
		if (action == extcommand_t.EXTCOMMAND_START) {
			event.getLogEvent().getChoice().getExecutorEvent().getChoice().getExtcommandStart().assign(cmd);
		} else {
			event.getLogEvent().getChoice().getExecutorEvent().getChoice().getExtcommandSuccess().assign(cmd);
		}

		log(event);
	}

	public void log_matching_done(final TitanLoggerApi.MatchingDoneType_reason.enum_type reason, final String type, final int ptc, final String return_type) {
		if (!TtcnLogger.log_this_event(TtcnLogger.Severity.MATCHING_DONE) && (TtcnLogger.get_emergency_logging() <= 0)) {
			return;
		}

		final TitanLogEvent event = new TitanLogEvent();
		fill_common_fields(event, TtcnLogger.Severity.MATCHING_DONE);
		final MatchingDoneType mp = event.getLogEvent().getChoice().getMatchingEvent().getChoice().getMatchingDone();
		mp.getReason().assign(reason);
		mp.getType__().assign(type);
		mp.getPtc().assign(ptc);
		mp.getReturn__type().assign(return_type);

		log(event);
	}

	public void log_matching_problem(final TitanLoggerApi.MatchingProblemType_reason.enum_type reason, final TitanLoggerApi.MatchingProblemType_operation.enum_type operation, final boolean check, final boolean anyport, final String port_name) {
		if (!TtcnLogger.log_this_event(TtcnLogger.Severity.MATCHING_PROBLEM) && (TtcnLogger.get_emergency_logging() <= 0)) {
			return;
		}

		final TitanLogEvent event = new TitanLogEvent();
		fill_common_fields(event, TtcnLogger.Severity.MATCHING_PROBLEM);
		final MatchingProblemType mp = event.getLogEvent().getChoice().getMatchingEvent().getChoice().getMatchingProblem();
		mp.getReason().assign(reason);
		mp.getAny__port().assign(anyport);
		mp.getCheck__().assign(check);
		mp.getOperation().assign(operation);
		mp.getPort__name().assign(port_name);

		log(event);
	}

	public void log_random(final TitanLoggerApi.RandomAction.enum_type rndAction, final double value, final long seed) {
		if (!TtcnLogger.log_this_event(Severity.FUNCTION_RND) && TtcnLogger.get_emergency_logging() <= 0) {
			return;
		}

		final TitanLogEvent event = new TitanLogEvent();
		fill_common_fields(event, Severity.FUNCTION_RND);
		final FunctionEvent_choice_random r = event.getLogEvent().getChoice().getFunctionEvent().getChoice().getRandom();
		r.getOperation().assign(rndAction);
		r.getRetval().assign(value);
		r.getIntseed().assign((int)seed);

		log(event);
	}

	public void log_matching_failure(final TitanLoggerApi.PortType.enum_type port_type, final String port_name, final int compref, final TitanLoggerApi.MatchingFailureType_reason.enum_type reason, final TitanCharString info) {
		Severity sev;
		if (compref == TitanComponent.SYSTEM_COMPREF) {
			sev = (port_type == enum_type.message__) ? Severity.MATCHING_MMUNSUCC : Severity.MATCHING_PMUNSUCC;
		} else {
			sev = (port_type == enum_type.message__) ? Severity.MATCHING_MCUNSUCC : Severity.MATCHING_PCUNSUCC;
		}
		if (!TtcnLogger.log_this_event(sev) && (TtcnLogger.get_emergency_logging() <= 0)) {
			return;
		}

		final TitanLogEvent event = new TitanLogEvent();
		fill_common_fields(event, sev);
		final MatchingFailureType mf = event.getLogEvent().getChoice().getMatchingEvent().getChoice().getMatchingFailure();
		mf.getPort__type().assign(port_type);
		mf.getPort__name().assign(port_name);
		mf.getReason().assign(reason);

		if (compref == TitanComponent.SYSTEM_COMPREF) {
			mf.getChoice().getSystem__();
		} else {
			mf.getChoice().getCompref().assign(compref);
		}

		mf.getInfo().assign(info);

		log(event);
	}

	public void log_matching_success(final TitanLoggerApi.PortType.enum_type port_type, final String port_name, final int compref, final TitanCharString info) {
		Severity sev;
		if (compref == TitanComponent.SYSTEM_COMPREF) {
			sev = port_type == enum_type.message__ ? Severity.MATCHING_MMSUCCESS : Severity.MATCHING_PMSUCCESS;
		} else {
			sev = port_type == enum_type.message__ ? Severity.MATCHING_MCSUCCESS : Severity.MATCHING_PCSUCCESS;
		}

		if (TtcnLogger.log_this_event(sev) && TtcnLogger.get_emergency_logging() <= 0) {
			return;
		}

		final TitanLogEvent event = new TitanLogEvent();
		fill_common_fields(event, sev);
		final MatchingSuccessType ms = event.getLogEvent().getChoice().getMatchingEvent().getChoice().getMatchingSuccess();
		ms.getPort__type().assign(port_type);
		ms.getPort__name().assign(port_name);

		log(event);
	}

	public void log_portconnmap(final ParPort_operation.enum_type operation, final int src_compref, final String src_port, final int dst_compref, final String dst_port) {
		TtcnLogger.Severity event_severity;
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

		if (!TtcnLogger.log_this_event(event_severity) && (TtcnLogger.get_emergency_logging() <= 0)) {
			return;
		}
		
		final TitanLogEvent event = new TitanLogEvent();
		fill_common_fields(event, event_severity);
		final ParPort pp = event.getLogEvent().getChoice().getParallelEvent().getChoice().getParallelPort();
		pp.getOperation().assign(operation);
		pp.getSrcCompref().assign(adjust_compref(src_compref));
		pp.getSrcPort().assign(src_port);
		pp.getDstCompref().assign(adjust_compref(dst_compref));
		pp.getDstPort().assign(dst_port);

		log(event);
	}

	public void log_parptc(final ParallelPTC_reason.enum_type reason, final String module, final String name, final int compref, final String compname, final String tc_loc, final int alive_pid, final int status) {
		TtcnLogger.Severity event_severity;
		if (alive_pid > 0 && reason == ParallelPTC_reason.enum_type.function__finished) {
			event_severity = Severity.PARALLEL_UNQUALIFIED;
		} else {
			event_severity = Severity.PARALLEL_PTC;
		}

		if (!TtcnLogger.log_this_event(event_severity) && (TtcnLogger.get_emergency_logging() <= 0)) {
			return;
		}
		
		final TitanLogEvent event = new TitanLogEvent();
		fill_common_fields(event, event_severity);
		final ParallelPTC ptc = event.getLogEvent().getChoice().getParallelEvent().getChoice().getParallelPTC();
		ptc.getReason().assign(reason);
		ptc.getModule__().assign(module);
		ptc.getName().assign(name);
		ptc.getCompref().assign(compref);
		ptc.getTc__loc().assign(tc_loc);
		ptc.getCompname().assign(compname);
		ptc.getAlive__pid().assign(alive_pid);
		ptc.getStatus().assign(status);

		log(event);
	}

	public void log_port_misc(final TitanLoggerApi.Port__Misc_reason.enum_type reason, final String port_name, final int remote_component, final String remote_port, final String ip_address, final int tcp_port, final int new_size) {
		if (!TtcnLogger.log_this_event(Severity.PORTEVENT_UNQUALIFIED) && (TtcnLogger.get_emergency_logging() <= 0)) {
			return;
		}

		final TitanLogEvent event = new TitanLogEvent();
		fill_common_fields(event, Severity.PORTEVENT_UNQUALIFIED);
		final Port__Misc portMisc = event.getLogEvent().getChoice().getPortEvent().getChoice().getPortMisc();
		portMisc.getReason().assign(reason.ordinal());
		portMisc.getPort__name().assign(port_name);
		portMisc.getRemote__component().assign(remote_component);
		portMisc.getRemote__port().assign(remote_port);
		portMisc.getIp__address().assign(ip_address);
		portMisc.getTcp__port().assign(tcp_port);
		portMisc.getNew__size().assign(new_size);

		log(event);
	}
}
