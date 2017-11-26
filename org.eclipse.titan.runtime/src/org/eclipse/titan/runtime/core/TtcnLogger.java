/******************************************************************************
 * Copyright (c) 2000-2017 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Locale;
import java.util.Stack;

/**
 * originally TTCN_Logger
 * @author Arpad Lovassy
 * @author Andrea Palfi
 * @author Gergo Ujhelyi
 */
public final class TtcnLogger {

	public static class Logging_Bits {
		public static final Logging_Bits log_nothing = new Logging_Bits();
		public static final Logging_Bits log_all = new Logging_Bits();
		public static final Logging_Bits default_console_mask = new Logging_Bits();

		final public HashSet<Severity> bits = new HashSet<Severity>();

		// static initializer
		static {
			// TTCN_ERROR | TTCN_WARNING | TTCN_ACTION | TTCN_TESTCASE | TTCN_STATISTICS
			default_console_mask.bits.add(Severity.ACTION_UNQUALIFIED);
			default_console_mask.bits.add(Severity.ERROR_UNQUALIFIED);
			default_console_mask.bits.add(Severity.TESTCASE_UNQUALIFIED);
			default_console_mask.bits.add(Severity.TESTCASE_START);
			default_console_mask.bits.add(Severity.TESTCASE_FINISH);
			default_console_mask.bits.add(Severity.STATISTICS_UNQUALIFIED);
			default_console_mask.bits.add(Severity.STATISTICS_VERDICT);
			default_console_mask.bits.add(Severity.WARNING_UNQUALIFIED);

			log_all.bits.add(Severity.ACTION_UNQUALIFIED);
			log_all.bits.add(Severity.DEFAULTOP_UNQUALIFIED);
			log_all.bits.add(Severity.DEFAULTOP_ACTIVATE);
			log_all.bits.add(Severity.DEFAULTOP_DEACTIVATE);
			log_all.bits.add(Severity.DEFAULTOP_EXIT);
			log_all.bits.add(Severity.ERROR_UNQUALIFIED);
			log_all.bits.add(Severity.EXECUTOR_UNQUALIFIED);
			log_all.bits.add(Severity.EXECUTOR_COMPONENT);
			log_all.bits.add(Severity.EXECUTOR_CONFIGDATA);
			log_all.bits.add(Severity.EXECUTOR_EXTCOMMAND);
			log_all.bits.add(Severity.EXECUTOR_LOGOPTIONS);
			log_all.bits.add(Severity.EXECUTOR_RUNTIME);
			log_all.bits.add(Severity.FUNCTION_UNQUALIFIED);
			log_all.bits.add(Severity.FUNCTION_RND);
			log_all.bits.add(Severity.PARALLEL_UNQUALIFIED);
			log_all.bits.add(Severity.PARALLEL_PORTCONN);
			log_all.bits.add(Severity.PARALLEL_PORTMAP);
			log_all.bits.add(Severity.PARALLEL_PTC);
			log_all.bits.add(Severity.TESTCASE_UNQUALIFIED);
			log_all.bits.add(Severity.TESTCASE_START);
			log_all.bits.add(Severity.TESTCASE_FINISH);
			log_all.bits.add(Severity.PORTEVENT_UNQUALIFIED);
			log_all.bits.add(Severity.PORTEVENT_DUALRECV);
			log_all.bits.add(Severity.PORTEVENT_DUALSEND);
			log_all.bits.add(Severity.PORTEVENT_MCRECV);
			log_all.bits.add(Severity.PORTEVENT_MCSEND);
			log_all.bits.add(Severity.PORTEVENT_MMRECV);
			log_all.bits.add(Severity.PORTEVENT_MMSEND);
			log_all.bits.add(Severity.PORTEVENT_MQUEUE);
			log_all.bits.add(Severity.PORTEVENT_PCIN);
			log_all.bits.add(Severity.PORTEVENT_PCOUT);
			log_all.bits.add(Severity.PORTEVENT_PMIN);
			log_all.bits.add(Severity.PORTEVENT_PMOUT);
			log_all.bits.add(Severity.PORTEVENT_PQUEUE);
			log_all.bits.add(Severity.PORTEVENT_SETSTATE);
			log_all.bits.add(Severity.PORTEVENT_STATE);
			log_all.bits.add(Severity.STATISTICS_UNQUALIFIED);
			log_all.bits.add(Severity.STATISTICS_VERDICT);
			log_all.bits.add(Severity.TIMEROP_UNQUALIFIED);
			log_all.bits.add(Severity.TIMEROP_GUARD);
			log_all.bits.add(Severity.TIMEROP_READ);
			log_all.bits.add(Severity.TIMEROP_START);
			log_all.bits.add(Severity.TIMEROP_STOP);
			log_all.bits.add(Severity.TIMEROP_TIMEOUT);
			log_all.bits.add(Severity.USER_UNQUALIFIED);
			log_all.bits.add(Severity.VERDICTOP_UNQUALIFIED);
			log_all.bits.add(Severity.VERDICTOP_FINAL);
			log_all.bits.add(Severity.VERDICTOP_GETVERDICT);
			log_all.bits.add(Severity.VERDICTOP_SETVERDICT);
			log_all.bits.add(Severity.WARNING_UNQUALIFIED);
		}

		public Logging_Bits() {
			//do nothing
		}

		public Logging_Bits(final Logging_Bits other) {
			bits.addAll(other.bits);
		}
	}

	static log_mask_struct console_log_mask = new log_mask_struct();
	static log_mask_struct file_log_mask = new log_mask_struct();
	//static log_mask_struct emergency_log_mask = new log_mask_struct();

	public static enum component_id_selector_enum {
		COMPONENT_ID_NAME,
		COMPONENT_ID_COMPREF,
		COMPONENT_ID_ALL,
		COMPONENT_ID_SYSTEM
	}

	public static class component_id_t {
		public component_id_selector_enum id_selector;
		public int id_compref;
		public String id_name;

		public component_id_t() {
			id_selector = component_id_selector_enum.COMPONENT_ID_ALL;
		}
	}

	public static class log_mask_struct {
		component_id_t component_id;
		Logging_Bits mask;

		public log_mask_struct() {
			component_id = new component_id_t();
			mask = new Logging_Bits();
		}
	}

	public static enum matching_verbosity_t { VERBOSITY_COMPACT, VERBOSITY_FULL };

	public static void set_matching_verbosity(matching_verbosity_t v) {
		matching_verbosity = v;
	}

	public static enum Severity {
		NOTHING_TO_LOG,
		ACTION_UNQUALIFIED,
		DEFAULTOP_ACTIVATE,
		DEFAULTOP_DEACTIVATE,
		DEFAULTOP_EXIT,
		DEFAULTOP_UNQUALIFIED,
		ERROR_UNQUALIFIED,
		EXECUTOR_RUNTIME,
		EXECUTOR_CONFIGDATA,
		EXECUTOR_EXTCOMMAND,
		EXECUTOR_COMPONENT,
		EXECUTOR_LOGOPTIONS,
		EXECUTOR_UNQUALIFIED,
		FUNCTION_RND,
		FUNCTION_UNQUALIFIED,
		PARALLEL_PTC,
		PARALLEL_PORTCONN,
		PARALLEL_PORTMAP,
		PARALLEL_UNQUALIFIED,
		TESTCASE_START,
		TESTCASE_FINISH,
		TESTCASE_UNQUALIFIED,
		PORTEVENT_PQUEUE,
		PORTEVENT_MQUEUE,
		PORTEVENT_STATE,
		PORTEVENT_PMIN,
		PORTEVENT_PMOUT,
		PORTEVENT_PCIN,
		PORTEVENT_PCOUT,
		PORTEVENT_MMRECV,
		PORTEVENT_MMSEND,
		PORTEVENT_MCRECV,
		PORTEVENT_MCSEND,
		PORTEVENT_DUALRECV,
		PORTEVENT_DUALSEND,
		PORTEVENT_UNQUALIFIED,
		PORTEVENT_SETSTATE,
		STATISTICS_VERDICT,
		STATISTICS_UNQUALIFIED,
		TIMEROP_READ,
		TIMEROP_START,
		TIMEROP_GUARD,
		TIMEROP_STOP,
		TIMEROP_TIMEOUT,
		TIMEROP_UNQUALIFIED,
		USER_UNQUALIFIED,
		VERDICTOP_GETVERDICT,
		VERDICTOP_SETVERDICT,
		VERDICTOP_FINAL,
		VERDICTOP_UNQUALIFIED,
		WARNING_UNQUALIFIED,
		// MATCHING and DEBUG should be at the end (not included in LOG_ALL)
		MATCHING_DONE,
		MATCHING_TIMEOUT,
		MATCHING_PCSUCCESS,
		MATCHING_PCUNSUCC,
		MATCHING_PMSUCCESS,
		MATCHING_PMUNSUCC,
		MATCHING_MCSUCCESS,
		MATCHING_MCUNSUCC,
		MATCHING_MMSUCCESS,
		MATCHING_MMUNSUCC,
		MATCHING_PROBLEM,
		MATCHING_UNQUALIFIED,
		DEBUG_ENCDEC,
		DEBUG_TESTPORT,
		DEBUG_USER,
		DEBUG_FRAMEWORK,
		DEBUG_UNQUALIFIED,
		NUMBER_OF_LOGSEVERITIES, // must follow the last individual severity
		LOG_ALL_IMPORTANT
	};

	public static final Severity sev_categories[]=
	{
		Severity.NOTHING_TO_LOG,//=0
		Severity.ACTION_UNQUALIFIED,
		Severity.DEFAULTOP_UNQUALIFIED,
		Severity.ERROR_UNQUALIFIED,
		Severity.EXECUTOR_UNQUALIFIED,
		Severity.FUNCTION_UNQUALIFIED,
		Severity.PARALLEL_UNQUALIFIED,
		Severity.TESTCASE_UNQUALIFIED,
		Severity.PORTEVENT_UNQUALIFIED,
		Severity.STATISTICS_UNQUALIFIED,
		Severity.TIMEROP_UNQUALIFIED,
		Severity.USER_UNQUALIFIED,
		Severity.VERDICTOP_UNQUALIFIED,
		Severity.WARNING_UNQUALIFIED,
		Severity.MATCHING_UNQUALIFIED,
		Severity.DEBUG_UNQUALIFIED,
	};

	public static String severity_category_names[]={
		"NULL",
		"ACTION",
		"DEFAULTOP",
		"ERROR",
		"EXECUTOR",
		"FUNCTION",
		"PARALLEL",
		"TESTCASE",
		"PORTEVENT",
		"STATISTICS",
		"TIMEROP",
		"USER",
		"VERDICTOP",
		"WARNING",
		"MATCHING",
		"DEBUG",
	};

	private static class log_event_struct {
		StringBuilder buffer;
		Severity severity;
		//event_destination, etc...
	}

	//temporary enum, original: TitanLoggerApi::Port_Queue.operation
	public static enum Port_Queue_operation {
		ENQUEUE_MSG,
		ENQUEUE_CALL,
		ENQUEUE_REPLY,
		ENQUEUE_EXCEPTION,
		EXTRACT_MSG,
		EXTRACT_OP
	}

	//temporary enum, original: TitanLoggerApi::Port_State.operation
	public static enum Port_State_operation {
		STARTED,
		STOPPED,
		HALTED
	}

	//temporary enum, original:TitanLoggerApi::Port_oper
	public static enum Port_oper {
		CALL_OP,
		EXCEPTION_OP,
		REPLY_OP
	}

	//temporary enum, original:TitanLoggerApi::Msg_port_recv.operation
	public static enum Msg_port_recv_operation {
		RECEIVE_OP,
		CHECK_RECEIVE_OP,
		TRIGGER_OP
	}

	static StringBuilder logMatchBuffer = new StringBuilder();
	static boolean logMatchPrinted = false;
	static matching_verbosity_t matching_verbosity = matching_verbosity_t.VERBOSITY_COMPACT;

	// length of the emergencylogging buffer
	static int emergency_logging = 0;;

	private static log_event_struct current_event = null;
	private static Stack<log_event_struct> events = new Stack<TtcnLogger.log_event_struct>();


	public static void initialize_logger() {
		console_log_mask.component_id.id_selector = component_id_selector_enum.COMPONENT_ID_ALL;
		console_log_mask.component_id.id_compref = TitanComponent.ANY_COMPREF;
		console_log_mask.mask = new Logging_Bits(Logging_Bits.default_console_mask);

		file_log_mask.component_id.id_selector = component_id_selector_enum.COMPONENT_ID_ALL;
		file_log_mask.component_id.id_compref = TitanComponent.ALL_COMPREF;
		file_log_mask.mask = new Logging_Bits(Logging_Bits.log_all);

		// TODO initialize emergency buffer too.
	}

	public static void terminate_logger() {
		//empty for now
	}

	public static void log(final Severity msg_severity, final String formatString, final Object... args ) {
		log_va_list(msg_severity, formatString, args);
	}

	public static void log_str(final Severity msg_severity, final String string ) {
		if (!log_this_event(msg_severity)) {
			return;
		}
		log_line(msg_severity, string == null ? "<NULL pointer>": string);
	}

	public static void log_va_list(final Severity msg_severity, final String formatString, final Object... args) {
		if (!log_this_event(msg_severity)) {
			return;
		}
		log_line(msg_severity, String.format(Locale.US, formatString, args));
	}

	public static void begin_event(final Severity msg_severity) {
		current_event = new log_event_struct();
		current_event.severity = msg_severity;
		current_event.buffer = new StringBuilder(100);
		events.push(current_event);
	}

	public static void begin_event_log2str() {
		begin_event(Severity.USER_UNQUALIFIED);//and true
	}

	public static void end_event() {
		if (current_event != null) {
			//TODO temporary solution for filtering
			if (log_this_event(current_event.severity)) {
				log_line(current_event.severity, current_event.buffer.toString());
			}

			events.pop();
			if (!events.isEmpty()) {
				current_event = events.peek();
			} else {
				current_event = null;
			}
		}
	}

	public static TitanCharString end_event_log2str() {
		if (current_event != null) {
			final TitanCharString ret_val = new TitanCharString(current_event.buffer);

			events.pop();
			if (!events.isEmpty()) {
				current_event = events.peek();
			} else {
				current_event = null;
			}

			return ret_val;
		}

		return new TitanCharString();
	}

	private static void log_line(final Severity msg_severity, final String message) {
		long timestamp = System.currentTimeMillis(); //TODO: time zone is not handled yet!
		final long millisec = timestamp % 1000;
		timestamp = timestamp / 1000;
		final long secs = timestamp % 60;
		timestamp = timestamp / 60;
		final long minutes = timestamp % 60;
		timestamp = timestamp / 60;
		final long hours = timestamp % 24;
//		timestamp = timestamp / 24; //not used yet
		final StringBuilder temp = new StringBuilder(20 + message.length());
		temp.append(String.format("%02d", hours)).append(':').append(String.format("%02d", minutes)).append(':').append(String.format("%02d", secs)).append('.').append(String.format("%03d", millisec)).append("000");
		temp.append(' ').append(message);

		System.out.println(temp);
	}

	public static void finish_event() {
		if (current_event != null) {
			log_event_str("<unfinished>");
			end_event();
		}
	}

	public static void log_event( final String formatString, final Object... args ) {
		log_event_va_list(formatString, args);
	}

	public static void log_event_str( final String string ) {
		if (current_event != null) {
			current_event.buffer.append(string);
		}
	}

	public static void log_event_va_list(final String formatString, final Object... args) {
		if (current_event != null) {
			current_event.buffer.append(String.format(Locale.US, formatString, args));
		}
	}

	public static void log_char(final char c) {
		// TODO: correct log_char
		if (current_event != null) {
			current_event.buffer.append(c);
		}
	}

	public static void log_event_uninitialized() {
		log_event_str("<uninitialized template>");
	}

	public static void log_event_enum(final String enum_name_str, final int enum_value) {
		//FIXME this is a bit more complicated
		log_event("%s (%d)", enum_name_str, enum_value);
	}

	public static boolean isPrintable(final char c) {
		if (c >= 32 && c <= 126) {
			// it includes all the printable characters in the ascii code table
			return true;
		}

		switch (c) {
		case '\b':
		case '\t':
		case '\n':
		case '\f':
		case '\r':
			return true;
		default:
			return false;
		}
	}

	public static void logCharEscaped(final char c, final StringBuilder p_buffer) {
		switch (c) {
		case '\n':
			p_buffer.append("\\n");
			break;
		case '\t':
			p_buffer.append("\\t");
			break;
		case '\b':
			p_buffer.append("\\b");
			break;
		case '\r':
			p_buffer.append("\\r");
			break;
		case '\f':
			p_buffer.append("\\f");
			break;
		case '\\':
			p_buffer.append("\\\\");
			break;
		case '"':
			p_buffer.append("\\\"");
			break;
		default:
			if (isPrintable(c)) {
				p_buffer.append(c);
			} else {
				log_event("\\%03o", c);
				break;
			}
		}
	}

	public static void logCharEscaped(final char c) {
		switch (c) {
		case '\n':
			log_event_str("\\n");
			break;
		case '\t':
			log_event_str("\\t");
			break;
		case '\b':
			log_event_str("\\b");
			break;
		case '\r':
			log_event_str("\\r");
			break;
		case '\f':
			log_event_str("\\f");
			break;
		case '\\':
			log_event_str("\\\\");
			break;
		case '"':
			log_event_str("\\\"");
			break;
		default:
			if (isPrintable(c)) {
				log_char(c);
			} else {
				log_event("\\%03o", c);
				break;
			}
		}
	}


	public static void log_hex( final byte aHexDigit ) {
		if(aHexDigit<16){
			log_char(TitanHexString.HEX_DIGITS.charAt(aHexDigit));
		} else {
			log_event_str("<unknown>");
		}
	}

	public static void log_event_unbound() {
		log_event_str("<unbound>");
	}

	public static void log_octet( final char aOctet ) {
		log_char(TitanHexString.HEX_DIGITS.charAt((aOctet & 0xF0)>>4));
		log_char(TitanHexString.HEX_DIGITS.charAt(aOctet & 0x0F));
	}

	public static void log_bit( final byte aBit ) {
		// TODO Auto-generated method stub
	}

	public static matching_verbosity_t get_matching_verbosity() {
		return matching_verbosity;
	}

	// Called from the generated code and many more places...  Stay here.  The
	// existence of the file descriptors etc. is the responsibility of the
	// plug-ins.
	public static boolean log_this_event(final Severity event_severity) {
		// FIXME: log_this_event
		// FIXME implement once we get to configurability
		// TODO: emergency logging=true
		// TODO: should_log_to_emergency
		if (should_log_to_file(event_severity)) {
			return true;
		} else if (should_log_to_console(event_severity)) {
			return true;
		}/*
		 * else if(should_log_to_emergency(event_severity) &&
		 * (get_emergency_logging()>0)){ return true; }
		 */
		else {
			return false;
		}
	}

	public static boolean should_log_to_file(final Severity sev) {

		return file_log_mask.mask.bits.contains(sev);
	}

	public static boolean should_log_to_console(final Severity sev) {
		if (sev == Severity.EXECUTOR_EXTCOMMAND) {
			return true;
		}
		return console_log_mask.mask.bits.contains(sev);
	}

	/*
	 * public static boolean should_log_to_emergency(final Severity sev) {
	 * return emergency_log_mask.mask.bits.contains(sev); }
	 */

	public static void print_logmatch_buffer() {
		if (logMatchPrinted) {
			log_event_str(" , ");
		} else {
			logMatchPrinted = true;
		}
		if (logMatchBuffer.length() > 0) {
			log_event_str(logMatchBuffer.toString());
		}
	}

	public static void log_logmatch_info(final String formatString, final Object... args) {
		if (formatString == null) {
			logMatchBuffer.append("<NULL format string>");
		} else {
			logMatchBuffer.append(String.format(Locale.US, formatString, args));
		}
	}

	public static void set_logmatch_buffer_len(final int previous_size) {
		logMatchBuffer.setLength(previous_size);
	}

	public static int get_logmatch_buffer_len() {
		return logMatchBuffer.length();
	}

	public static void log_timer_read(final String timer_name, final double timeout_val) {
		if (!log_this_event(Severity.TIMEROP_READ) && get_emergency_logging() <= 0) {
			return;
		}

		log_line(Severity.TIMEROP_READ, MessageFormat.format("Read timer {0}: {1} s", timer_name, timeout_val));
	}

	public static void log_timer_start(final String timer_name, final double start_val) {
		if (!log_this_event(Severity.TIMEROP_START) && get_emergency_logging() <= 0) {
			return;
		}

		log_line(Severity.TIMEROP_START, MessageFormat.format("Start timer {0}: {1} s", timer_name, start_val));

	}

	public static void log_timer_guard(final double start_val) {
		if (!log_this_event(Severity.TIMEROP_GUARD) && get_emergency_logging() <= 0) {
			return;
		}

		log_line(Severity.TIMEROP_GUARD, MessageFormat.format("Test case guard timer was set to {0} s", start_val));

	}

	public static void log_timer_stop(final String timer_name, final double stop_val) {
		if (!log_this_event(Severity.TIMEROP_STOP) && get_emergency_logging() <= 0) {
			return;
		}

		log_line(Severity.TIMEROP_STOP, MessageFormat.format("Stop timer {0}: {1} s", timer_name, stop_val));

	}

	public static void log_timer_timeout(final String timer_name, final double timeout_val) {
		if (!log_this_event(Severity.TIMEROP_TIMEOUT) && get_emergency_logging() <= 0) {
			return;
		}

		log_line(Severity.TIMEROP_TIMEOUT, MessageFormat.format("Timeout {0}: {1} s", timer_name, timeout_val));

	}

	public static void log_timer_any_timeout() {
		if (!log_this_event(Severity.TIMEROP_TIMEOUT) && get_emergency_logging() <= 0) {
			return;
		}

		log_line(Severity.TIMEROP_TIMEOUT, "Operation `any timer.timeout' was successful.");

	}

	public static void log_timer_unqualified(final String message) {
		if (!log_this_event(Severity.TIMEROP_UNQUALIFIED) && get_emergency_logging() <= 0) {
			return;
		}

		log_line(Severity.TIMEROP_UNQUALIFIED, message);

	}

	public static void log_matching_timeout(final String timer_name) {
		if (!log_this_event(Severity.MATCHING_PROBLEM) && get_emergency_logging() <= 0) {
			return;
		}

		if (timer_name == null) {
			log_line(Severity.MATCHING_PROBLEM, "Operation `any timer.timeout' failed: The test component does not have active timers.");
		} else {
			log_line(Severity.MATCHING_PROBLEM, MessageFormat.format("Timeout operation on timer {0} failed: The timer is not started.", timer_name));
		}
	}

	public static void log_port_queue(final Port_Queue_operation operation, final String port_name, final int componentReference, final int id, final TitanCharString address, final TitanCharString parameter) {
		final String dest = TitanComponent.get_component_string(componentReference);
		String ret_val = "";
		Severity sev;
		switch (operation) {
		case ENQUEUE_MSG:
		case EXTRACT_MSG:
			sev = Severity.PORTEVENT_MQUEUE;
			break;
		case ENQUEUE_CALL:
		case ENQUEUE_REPLY:
		case ENQUEUE_EXCEPTION:
		case EXTRACT_OP:
			sev = Severity.PORTEVENT_PQUEUE;
		default:
			throw new TtcnError("Invalid operation");
		}

		if (!log_this_event(sev) && get_emergency_logging() <= 0) {
			return;
		}

		switch (operation) {
		case ENQUEUE_MSG:
			ret_val = "Message";
			log_event_str(MessageFormat.format("{0} enqueued on {1} from {2}{3}{4} id {5}", ret_val, port_name , dest, address, parameter, id));
			break;
		case ENQUEUE_CALL:
			ret_val = "Call";
			log_event_str(MessageFormat.format("{0} enqueued on {1} from {2}{3}{4} id {5}", ret_val, port_name , dest, address, parameter, id));
			break;
		case ENQUEUE_REPLY:
			ret_val = "Reply";
			log_event_str(MessageFormat.format("{0} enqueued on {1} from {2}{3}{4} id {5}", ret_val, port_name , dest, address, parameter, id));
			break;
		case ENQUEUE_EXCEPTION:
			ret_val = "Exception";
			log_event_str(MessageFormat.format("{0} enqueued on {1} from {2}{3}{4} id {5}", ret_val, port_name , dest, address, parameter, id));
			break;
		case EXTRACT_MSG:
			ret_val = "Message";
			log_event_str(MessageFormat.format("{0} with id {1} was extracted from the queue of {2}.", ret_val, id, port_name));
			break;
		case EXTRACT_OP:
			ret_val = "Operation";
			log_event_str(MessageFormat.format("{0} with id {1} was extracted from the queue of {2}.", ret_val, id, port_name));
			break;
		default:
			throw new TtcnError("Invalid operation");
		}
	}

	public static void set_console_mask(final component_id_t cmpt,
			final Logging_Bits new_console_mask) {
		if (console_log_mask.component_id.id_selector == component_id_selector_enum.COMPONENT_ID_COMPREF && cmpt.id_selector == component_id_selector_enum.COMPONENT_ID_ALL) {
			return;
		}
		console_log_mask.mask = new_console_mask;
		if (cmpt.id_selector == component_id_selector_enum.COMPONENT_ID_NAME) {
			console_log_mask.component_id.id_selector = component_id_selector_enum.COMPONENT_ID_NAME;
			console_log_mask.component_id.id_name = cmpt.id_name;
		} else {
			console_log_mask.component_id = cmpt;
		}
	}

	public static void set_emergency_logging(final int size) {
		emergency_logging = size;
	}

	public static int get_emergency_logging() {
		return emergency_logging;
	}

	public static void log_port_state(final Port_State_operation operation, final String portname) {
		if (!log_this_event(Severity.PORTEVENT_STATE)) {
			return;
		}

		String what = "";
		switch (operation) {
		case STARTED:
			what = "started";
			break;
		case STOPPED:
			what = "stopped";
			break;
		case HALTED:
			what = "halted";
			break;
		default:
			return;
		}
		log_event_str(MessageFormat.format("Port {0} was {1}.", portname, what));
	}

	public static void log_procport_send(final String portname, final Port_oper operation, final int componentReference, final TitanCharString system, final TitanCharString parameter) {
		final Severity severity = componentReference == TitanComponent.SYSTEM_COMPREF ? Severity.PORTEVENT_PMOUT : Severity.PORTEVENT_PCOUT;
		if (!log_this_event(severity) && get_emergency_logging() <= 0) {
			return;
		}

		final String dest = TitanComponent.get_component_string(componentReference);
		String ret_val = "";
		switch (operation) {
		case CALL_OP:
			ret_val = "Called";
			break;
		case REPLY_OP:
			ret_val = "Replied";
			break;
		case EXCEPTION_OP:
			ret_val = "Raised";
		default:
			return;
		}
		log_event_str(MessageFormat.format("{0} on {1} to {2} {3}", ret_val, portname, dest, parameter.getValue()));
	}

	public static void log_procport_recv(final String portname, final Port_oper operation, final int componentReference, final boolean check, final TitanCharString parameter, final int id) {
		final Severity severity = componentReference == TitanComponent.SYSTEM_COMPREF ? Severity.PORTEVENT_PMIN : Severity.PORTEVENT_PCIN;
		if (!log_this_event(severity) && get_emergency_logging() <= 0) {
			return;
		}

		final String source = TitanComponent.get_component_string(componentReference);
		String ret_val = "";
		String op2 = "";
		switch (operation) {
		case CALL_OP:
			ret_val = (check ? "Check-getcall" : "Getcall");
			op2 = "call";
			break;
		case REPLY_OP:
			ret_val = (check ? "Check-getreply" : "Getreply");
			op2 = "reply";
		case EXCEPTION_OP:
			ret_val = (check ? "Check-catch" : "Catch");
			op2 = "exception";
		default:
			return;
		}
		log_event_str(MessageFormat.format("{0} operation on port {1} succeeded, {2} from {3}: {4} id {5}", ret_val, portname, op2, source, parameter.getValue(), id));
	}

	public static void log_msgport_send(final String portname, final int componentReference, final TitanCharString parameter) {
		final Severity severity = componentReference == TitanComponent.SYSTEM_COMPREF ? Severity.PORTEVENT_MMSEND : Severity.PORTEVENT_MCSEND;
		if (!log_this_event(severity) && get_emergency_logging() <= 0) {
			return;
		}

		final String dest = TitanComponent.get_component_string(componentReference);
		log_event_str(MessageFormat.format("Sent on {0} to {1}{2}", portname, dest, parameter.getValue()));
	}

	public static void log_msgport_recv(final String portname, final Msg_port_recv_operation operation, final int componentReference, final TitanCharString system, final TitanCharString parameter, final int id) {
		final Severity severity = componentReference == TitanComponent.SYSTEM_COMPREF ? Severity.PORTEVENT_MMRECV : Severity.PORTEVENT_MCRECV;
		if (!log_this_event(severity) && get_emergency_logging() <= 0) {
			return;
		}

		final String dest = TitanComponent.get_component_string(componentReference);
		String ret_val = "";
		switch (operation) {
		case RECEIVE_OP:
			ret_val = "Receive";
			break;
		case CHECK_RECEIVE_OP:
			ret_val = "Check-receive";
			break;
		case TRIGGER_OP:
			ret_val = "Trigger";
			break;
		default:
			return;
		}
		// FIXME:more complicated
		log_event_str(MessageFormat.format("{0} operation on port {1} succeeded, message from {2} {3} id {4}", ret_val, portname, dest, parameter.getValue(), id));
	}

	public static void log_dualport_map(final boolean incoming, final String target_type, final TitanCharString value, final int id) {
		final Severity severity = incoming ? Severity.PORTEVENT_DUALRECV : Severity.PORTEVENT_DUALSEND;
		if (!log_this_event(severity) && get_emergency_logging() <= 0) {
			return;
		}

		String ret_val = MessageFormat.format("{0} message was mapped to {1} : {2}", (incoming ? "Incoming" : "Outgoing"), target_type, value.getValue());
		if (incoming) {
			ret_val += MessageFormat.format(" id {0}", id);
		}
		log_event_str(ret_val);
	}

	public static void log_controlpart_start_stop(final String moduleName, final boolean finished) {
		if (!log_this_event(Severity.STATISTICS_UNQUALIFIED) && get_emergency_logging() <= 0) {
			return;
		}

		if (finished) {
			TtcnLogger.log(Severity.TESTCASE_FINISH, "Execution of control part in module %s finished.", moduleName);
		} else {
			TtcnLogger.log(Severity.TESTCASE_START, "Execution of control part in module %s started.", moduleName);
		}
	}

	public static void log_defaultop_activate(final String name, final int id) {
		if (!log_this_event(Severity.DEFAULTOP_ACTIVATE) && get_emergency_logging() <= 0) {
			return;
		}

		log_event_str(MessageFormat.format("Altstep {0} was activated as default, id {1}", name, id));
	}
}
