/******************************************************************************
 * Copyright (c) 2000-2017 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.Stack;

/**
 * originally TTCN_Logger
 * @author Arpad Lovassy
 * @author Andrea Palfi
 */
public final class TtcnLogger {

	public static enum matching_verbosity_t { VERBOSITY_COMPACT, VERBOSITY_FULL };

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



	static StringBuilder logMatchBuffer = new StringBuilder();
	static boolean logMatchPrinted = false;
	static matching_verbosity_t matching_verbosity = matching_verbosity_t.VERBOSITY_COMPACT;

	private static log_event_struct current_event = null;
	private static Stack<log_event_struct> events = new Stack<TtcnLogger.log_event_struct>();


	public static void initialize_logger() {
		//empty for the time being
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

	private static void log_line(final Severity event_severity, final String message) {
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
		temp.append(hours).append(':').append(minutes).append(':').append(secs).append('.').append(millisec).append("000");
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

	public static void logCharEscaped(final char c, StringBuilder p_buffer) {
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
		//FIXME implement once we get to configurability
		return true;
	}

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

	public static void log_msgport_send(final String portname, final int componentRefernce, final TitanCharString parameter) {
		final String dest = TitanComponent.get_component_string(componentRefernce);
		log_event_str(MessageFormat.format("Sent on {0} to {1}{2}", portname, dest, parameter.getValue()));
	}

	public static void log_port_queue(final Port_Queue_operation operation, final String port_name, int componentReference, int id, final TitanCharString address, final TitanCharString parameter) {
		final String dest = TitanComponent.get_component_string(componentReference);
		String ret_val = "";
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
			return;
		}
	}

	public static void log_controlpart_start_stop(final String moduleName, final boolean finished) {
		//FIXME also needs to check emergency logging
		if (!log_this_event(Severity.STATISTICS_UNQUALIFIED)) {
			return;
		}

		if (finished) {
			TtcnLogger.log(Severity.TESTCASE_FINISH, "Execution of control part in module %s finished.", moduleName);
		} else {
			TtcnLogger.log(Severity.TESTCASE_START, "Execution of control part in module %s started.", moduleName);
		}
	}

	public static void log_defaultop_activate(final String name, final int id) {
		//FIXME also needs to check emergency logging
		if (!log_this_event(Severity.DEFAULTOP_ACTIVATE)) {
			return;
		}

		log_event_str(MessageFormat.format("Altstep {0} was activated as default, id {1}", name, id));
	}
}
