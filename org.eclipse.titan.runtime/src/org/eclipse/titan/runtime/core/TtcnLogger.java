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
			//FIXME user unqualified should only be part of the default consol log, till we can configure it from config files
			default_console_mask.bits.add(Severity.USER_UNQUALIFIED);

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
	
	//temporary enum, original: TitanLoggerApi::MatchingFailureType.reason
	public static enum MatchingFailureType_reason {
		SENDER_DOES_NOT_MATCH_FROM_CLAUSE,
		SENDER_IS_NOT_SYSTEM,
		MESSAGE_DOES_NOT_MATCH_TEMPLATE,
		PARAMETERS_OF_CALL_DO_NOT_MATCH_TEMPLATE,
		PARAMETERS_OF_REPLY_DO_NOT_MATCH_TEMPLATE,
		EXCEPTION_DOES_NOT_MATCH_TEMPLATE,
		NOT_AN_EXCEPTION_FOR_SIGNATURE
	}

	//temporary enum, original: TitanLoggerApi::MatchingProblemType.reason
	public static enum MatchingProblemType_reason {
		PORT_NOT_STARTED_AND_QUEUE_EMPTY,
		NO_INCOMING_TYPES,
		NO_INCOMING_SIGNATURES,
		NO_OUTGOING_BLOCKING_SIGNATURES,
		NO_OUTGOING_BLOCKING_SIGNATURES_THAT_SUPPORT_EXCEPTIONS,
		COMPONENT_HAS_NO_PORTS
	}
	
	//temporary enum, original: TitanLoggerApi::MatchingProblemType.operation
	public static enum MatchingProblemType_operation {
		RECEIVE_,
		TRIGGER_,
		GETCALL_,
		GETREPLY_,
		CATCH_,
		CHECK_
	}
	
	//temporary enum, original: TitanLoggerApi::PortType
	public static enum PortType {
		MESSAGE_,
		PROCEDURE_
	}

	//temporary enum, original TitanLoggerApi::Port_Misc.reason
	public static enum Port_Misc_reason {
		REMOVING_UNTERMINATED_CONNECTION,
		REMOVING_UNTERMINATED_MAPPING,
		PORT_WAS_CLEARED,
		LOCAL_CONNECTION_ESTABLISHED,
		LOCAL_CONNECTION_TERMINATED,
		PORT_IS_WAITING_FOR_CONNECTION_TCP,
		PORT_IS_WAITING_FOR_CONNECTION_UNIX,
		CONNECTION_ESTABLISHED,
		DESTROYING_UNESTABLISHED_CONNECTION,
		TERMINATING_CONNECTION,
		SENDING_TERMINATION_REQUEST_FAILED,
		TERMINATION_REQUEST_RECEIVED,
		ACKNOWLEDGING_TERMINATION_REQUEST_FAILED,
		SENDING_WOULD_BLOCK,
		CONNECTION_ACCEPTED,
		CONNECTION_RESET_BY_PEER,
		CONNECTION_CLOSED_BY_PEER,
		PORT_DISCONNECTED,
		PORT_WAS_MAPPED_TO_SYSTEM,
		PORT_WAS_UNMAPPED_FROM_SYSTEM
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
		//// TODO this is a temporal implementation to display only console logs, until file logging is also supported.
		if (!should_log_to_console(msg_severity)) {
			return;
		}

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

	public static void set_start_time() {
		//FIXME implement
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


	public static void log_matching_problem(final MatchingProblemType_reason reason, final MatchingProblemType_operation operation, final boolean check, final boolean anyport, final String port_name) {
		if (!log_this_event(TtcnLogger.Severity.MATCHING_PROBLEM) && (get_emergency_logging() <= 0)) {
			return;
		}
		StringBuilder ret_val = new StringBuilder();
		ret_val.append("Operation `");
		if (anyport) {
			ret_val.append("any port.");
		}

		if (check) {
			ret_val.append("check(");
		}
		switch (operation) {
		case RECEIVE_:
			ret_val.append("receive");
			break;
		case TRIGGER_:
			ret_val.append("trigger");
			break;
		case GETCALL_:
			ret_val.append("getcall");
			break;
		case GETREPLY_:
			ret_val.append("getreply");
			break;
		case CATCH_:
			ret_val.append("catch");
			break;
		case CHECK_:
			ret_val.append("check");
			break;
		default:
			break;
		}
		if (check) {
			ret_val.append(")");
		}
		ret_val.append("' ");

		if (port_name != null) {
			ret_val.append(MessageFormat.format("on port {0} ", port_name));
		}
		// we could also check that any__port is false

		ret_val.append("failed: ");

		switch (reason) {
		case COMPONENT_HAS_NO_PORTS:
			ret_val.append("The test component does not have ports.");
			break;
		case NO_INCOMING_SIGNATURES:
			ret_val.append("The port type does not have any incoming signatures.");
			break;
		case NO_INCOMING_TYPES:
			ret_val.append("The port type does not have any incoming message types.");
			break;
		case NO_OUTGOING_BLOCKING_SIGNATURES:
			ret_val.append("The port type does not have any outgoing blocking signatures.");
			break;
		case NO_OUTGOING_BLOCKING_SIGNATURES_THAT_SUPPORT_EXCEPTIONS:
			ret_val.append("The port type does not have any outgoing blocking signatures that support exceptions.");
			break;
		case PORT_NOT_STARTED_AND_QUEUE_EMPTY:
			ret_val.append("Port is not started and the queue is empty.");
			break;
		default:
			break;
		}
		log_event_str(ret_val.toString());
	}

	//temporary enum, original: TitanLoggerApi::RandomAction
	public static enum RandomAction {
		seed,
		read_out,
		UNBOUND_VALUE,
		UNKNOWN_VALUE
	}

	public static void log_random(final RandomAction rndAction, double value, long seed) {
		if (!log_this_event(Severity.FUNCTION_RND) && get_emergency_logging() <= 0) {
			return;
		}

		StringBuilder ret_val = new StringBuilder();

		switch (rndAction) {
		case seed:
			ret_val.append(MessageFormat.format( "Random number generator was initialized with seed {0}: {1}",value,seed));
			break;
		case read_out:
			ret_val.append(MessageFormat.format("Function rnd() returned {0}.", value));
			break;
		case UNBOUND_VALUE:
		case UNKNOWN_VALUE:
		default:
			break;
		}

		log_line(Severity.FUNCTION_RND,ret_val.toString());
	}

	public static void log_matching_failure(final PortType port_type, final String port_name, final int compref, final MatchingFailureType_reason reason, final TitanCharString info) {
		Severity sev;
		boolean is_call = false;
		if (compref == TitanComponent.SYSTEM_COMPREF) {
			sev = (port_type == PortType.MESSAGE_) ? Severity.MATCHING_MMUNSUCC : Severity.MATCHING_PMUNSUCC;
		} else {
			sev = (port_type == PortType.MESSAGE_) ? Severity.MATCHING_MCUNSUCC : Severity.MATCHING_PCUNSUCC;
		}
		if (!log_this_event(sev) && (get_emergency_logging() <= 0)) {
			return;
		}

		StringBuilder ret_val = new StringBuilder();
		switch (reason) {
		case MESSAGE_DOES_NOT_MATCH_TEMPLATE:
			ret_val.append(MessageFormat.format("Matching on port {0} {1}: First message in the queue does not match the template: ", port_name, info.toString()));
			break;
		case EXCEPTION_DOES_NOT_MATCH_TEMPLATE:
			ret_val.append(MessageFormat.format("Matching on port {0} failed: The first exception in the queue does not match the template: {1}", port_name, info.toString()));
			break;
		case PARAMETERS_OF_CALL_DO_NOT_MATCH_TEMPLATE:
			is_call = true; // fall through
		case PARAMETERS_OF_REPLY_DO_NOT_MATCH_TEMPLATE:
			ret_val.append(MessageFormat.format("Matching on port {0} failed: The parameters of the first {1} in the queue do not match the template: {2}", port_name, is_call ? "call" : "reply", info.toString()));
			break;
		case SENDER_DOES_NOT_MATCH_FROM_CLAUSE:
			ret_val.append(MessageFormat.format("Matching on port {0} failed: Sender of the first entity in the queue does not match the from clause: {1}", port_name, info.toString()));
			break;
		case SENDER_IS_NOT_SYSTEM:
			ret_val.append(MessageFormat.format("Matching on port {0} failed: Sender of the first entity in the queue is not the system.", port_name));
			break;
		case NOT_AN_EXCEPTION_FOR_SIGNATURE:
			ret_val.append(MessageFormat.format("Matching on port {0} failed: The first entity in the queue is not an exception for signature {1}.", port_name, info.toString()));
			break;
		default:
			break;
		}
		log_event_str(ret_val.toString());
	}

	public static void log_matching_success(final PortType port_type, final String port_name, final int compref, final TitanCharString info) {
		Severity sev;
		if(compref == TitanComponent.SYSTEM_COMPREF) {
			sev = port_type == PortType.MESSAGE_ ? Severity.MATCHING_MMSUCCESS : Severity.MATCHING_PMSUCCESS;
		} else {
			sev = port_type == PortType.MESSAGE_ ? Severity.MATCHING_MCSUCCESS : Severity.MATCHING_PCSUCCESS;
		}

		if(log_this_event(sev) && get_emergency_logging() <= 0) {
			return;
		}
		log_event_str(MessageFormat.format("Matching on port {0} succeeded: {1}", port_name, info.toString()));
	}

	public static void log_port_misc(final Port_Misc_reason reason, final String port_name, final int remote_component, final String remote_port, final String ip_address, final int tcp_port, final int new_size) {
		if (!log_this_event(Severity.PORTEVENT_UNQUALIFIED) && (get_emergency_logging()<=0)) {
			return;
		}

		final StringBuilder ret_val = new StringBuilder();
		final String comp_str = TitanComponent.get_component_string(remote_component);
		switch (reason) {
		case REMOVING_UNTERMINATED_CONNECTION:
			ret_val.append(MessageFormat.format("Removing unterminated connection between port {0} and {1}:{2}.", port_name, comp_str, remote_port));
			break;
		case REMOVING_UNTERMINATED_MAPPING:
			ret_val.append(MessageFormat.format("Removing unterminated mapping between port {0} and system:{1}.", port_name, remote_port));
			break;
		case PORT_WAS_CLEARED:
			ret_val.append(MessageFormat.format("Port {0} was cleared.", port_name));
			break;
		case LOCAL_CONNECTION_ESTABLISHED:
			ret_val.append(MessageFormat.format("Port {0} has established the connection with local port {1}.", port_name, remote_port));
			break;
		case LOCAL_CONNECTION_TERMINATED:
			ret_val.append(MessageFormat.format("Port {0} has terminated the connection with local port {1}.", port_name, remote_port));
			break;
		case PORT_IS_WAITING_FOR_CONNECTION_TCP:
			ret_val.append(MessageFormat.format("Port {0} is waiting for connection from {1}:{2} on TCP port {3}:{4}.", port_name, comp_str, remote_port, ip_address, tcp_port));
			break;
		case PORT_IS_WAITING_FOR_CONNECTION_UNIX:
			ret_val.append(MessageFormat.format("Port {0} is waiting for connection from {1}:{2} on UNIX pathname {3}.", port_name, comp_str, remote_port, ip_address));
			break;
		case CONNECTION_ESTABLISHED:
			ret_val.append(MessageFormat.format("Port {0} has established the connection with {1}:{2} using transport type {3}.", port_name, comp_str, remote_port, ip_address));
			break;
		case DESTROYING_UNESTABLISHED_CONNECTION:
			ret_val.append(MessageFormat.format("Destroying unestablished connection of port {0} to {1}:{2} because the other endpoint has terminated.", port_name, comp_str, remote_port));
			break;
		case TERMINATING_CONNECTION:
			ret_val.append(MessageFormat.format("Terminating the connection of port {0} to {1}:{2}. No more messages can be sent through this connection.", port_name, comp_str, remote_port));
			break;
		case SENDING_TERMINATION_REQUEST_FAILED:
			ret_val.append(MessageFormat.format("Sending the connection termination request on port {0} to remote endpoint {1}:}{2} failed.", port_name, comp_str, remote_port));
			break;
		case TERMINATION_REQUEST_RECEIVED:
			ret_val.append(MessageFormat.format("Connection termination request was received on port {0} from {1}:{2}. No more data can be sent or received through this connection.", port_name, comp_str, remote_port));
			break;
		case ACKNOWLEDGING_TERMINATION_REQUEST_FAILED:
			ret_val.append(MessageFormat.format("Sending the acknowledgment for connection termination request on port {0} to remote endpoint {1}:{2} failed.", port_name, comp_str, remote_port));
			break;
		case SENDING_WOULD_BLOCK:
			ret_val.append(MessageFormat.format("Sending data on the connection of port {0} to {1}:{2} would block execution. The size of the outgoing buffer was increased from {3} to {4} bytes.", port_name, comp_str, remote_port, tcp_port, new_size));
			break;
		case CONNECTION_ACCEPTED:
			ret_val.append(MessageFormat.format("Port {0} has accepted the connection from {1}:{2}.", port_name, comp_str, remote_port));
			break;
		case CONNECTION_RESET_BY_PEER:
			ret_val.append(MessageFormat.format("Connection of port {0} to {1}:{2} was reset by the peer.", port_name, comp_str, remote_port));
			break;
		case CONNECTION_CLOSED_BY_PEER:
			ret_val.append(MessageFormat.format("Connection of port {0} to {1}:{2} was closed unexpectedly by the peer.", port_name, comp_str, remote_port));
			break;
		case PORT_DISCONNECTED:
			ret_val.append(MessageFormat.format("Port {0} was disconnected from {1}:{2}.", port_name, comp_str, remote_port));
			break;
		case PORT_WAS_MAPPED_TO_SYSTEM:
			ret_val.append(MessageFormat.format("Port {0} was mapped to system:{1}.", port_name, remote_port));
			break;
		case PORT_WAS_UNMAPPED_FROM_SYSTEM:
			ret_val.append(MessageFormat.format("Port {0} was unmapped from system:{1}.", port_name, remote_port));
			break;
		default:
			break;
		}

		log_line(Severity.PORTEVENT_UNQUALIFIED, ret_val.toString());
	}
}
