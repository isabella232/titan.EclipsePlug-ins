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

import org.eclipse.titan.runtime.core.Base_Template.template_sel;
import org.eclipse.titan.runtime.core.TitanLoggerApi.DefaultEvent_choice;
import org.eclipse.titan.runtime.core.TitanLoggerApi.DefaultOp;
import org.eclipse.titan.runtime.core.TitanLoggerApi.Dualface__mapped;
import org.eclipse.titan.runtime.core.TitanLoggerApi.FunctionEvent_choice_random;
import org.eclipse.titan.runtime.core.TitanLoggerApi.LogEventType_choice;
import org.eclipse.titan.runtime.core.TitanLoggerApi.MatchingEvent_choice;
import org.eclipse.titan.runtime.core.TitanLoggerApi.MatchingFailureType;
import org.eclipse.titan.runtime.core.TitanLoggerApi.MatchingProblemType;
import org.eclipse.titan.runtime.core.TitanLoggerApi.MatchingSuccessType;
import org.eclipse.titan.runtime.core.TitanLoggerApi.MatchingTimeout;
import org.eclipse.titan.runtime.core.TitanLoggerApi.Msg__port__recv;
import org.eclipse.titan.runtime.core.TitanLoggerApi.Msg__port__send;
import org.eclipse.titan.runtime.core.TitanLoggerApi.PortEvent_choice;
import org.eclipse.titan.runtime.core.TitanLoggerApi.PortType.enum_type;
import org.eclipse.titan.runtime.core.TitanLoggerApi.DefaultEnd;
import org.eclipse.titan.runtime.core.TitanLoggerApi.Port__Misc;
import org.eclipse.titan.runtime.core.TitanLoggerApi.Port__Queue;
import org.eclipse.titan.runtime.core.TitanLoggerApi.Port__State;
import org.eclipse.titan.runtime.core.TitanLoggerApi.Proc__port__in;
import org.eclipse.titan.runtime.core.TitanLoggerApi.Proc__port__out;
import org.eclipse.titan.runtime.core.TitanLoggerApi.QualifiedName;
import org.eclipse.titan.runtime.core.TitanLoggerApi.SetVerdictType;
import org.eclipse.titan.runtime.core.TitanLoggerApi.StatisticsType;
import org.eclipse.titan.runtime.core.TitanLoggerApi.StatisticsType_choice;
import org.eclipse.titan.runtime.core.TitanLoggerApi.TestcaseEvent_choice;
import org.eclipse.titan.runtime.core.TitanLoggerApi.TestcaseType;
import org.eclipse.titan.runtime.core.TitanLoggerApi.TimerEvent_choice;
import org.eclipse.titan.runtime.core.TitanLoggerApi.TimerGuardType;
import org.eclipse.titan.runtime.core.TitanLoggerApi.TimerType;
import org.eclipse.titan.runtime.core.TitanLoggerApi.TitanLogEvent;
import org.eclipse.titan.runtime.core.TitanLoggerApi.VerdictOp_choice;
import org.eclipse.titan.runtime.core.TitanVerdictType.VerdictTypeEnum;

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

		final public boolean bits[] = new boolean[Severity.values().length];

		// static initializer
		static {
			// TTCN_ERROR | TTCN_WARNING | TTCN_ACTION | TTCN_TESTCASE | TTCN_STATISTICS
			default_console_mask.bits[Severity.ACTION_UNQUALIFIED.ordinal()] = true;
			default_console_mask.bits[Severity.ERROR_UNQUALIFIED.ordinal()] = true;
			default_console_mask.bits[Severity.TESTCASE_UNQUALIFIED.ordinal()] = true;
			default_console_mask.bits[Severity.TESTCASE_START.ordinal()] = true;
			default_console_mask.bits[Severity.TESTCASE_FINISH.ordinal()] = true;
			default_console_mask.bits[Severity.STATISTICS_UNQUALIFIED.ordinal()] = true;
			default_console_mask.bits[Severity.STATISTICS_VERDICT.ordinal()] = true;
			default_console_mask.bits[Severity.WARNING_UNQUALIFIED.ordinal()] = true;
			//FIXME user unqualified should only be part of the default consol log, till we can configure it from config files
			default_console_mask.bits[Severity.USER_UNQUALIFIED.ordinal()] = true;

			log_all.bits[Severity.ACTION_UNQUALIFIED.ordinal()] = true;
			log_all.bits[Severity.DEFAULTOP_UNQUALIFIED.ordinal()] = true;
			log_all.bits[Severity.DEFAULTOP_ACTIVATE.ordinal()] = true;
			log_all.bits[Severity.DEFAULTOP_DEACTIVATE.ordinal()] = true;
			log_all.bits[Severity.DEFAULTOP_EXIT.ordinal()] = true;
			log_all.bits[Severity.ERROR_UNQUALIFIED.ordinal()] = true;
			log_all.bits[Severity.EXECUTOR_UNQUALIFIED.ordinal()] = true;
			log_all.bits[Severity.EXECUTOR_COMPONENT.ordinal()] = true;
			log_all.bits[Severity.EXECUTOR_CONFIGDATA.ordinal()] = true;
			log_all.bits[Severity.EXECUTOR_EXTCOMMAND.ordinal()] = true;
			log_all.bits[Severity.EXECUTOR_LOGOPTIONS.ordinal()] = true;
			log_all.bits[Severity.EXECUTOR_RUNTIME.ordinal()] = true;
			log_all.bits[Severity.FUNCTION_UNQUALIFIED.ordinal()] = true;
			log_all.bits[Severity.FUNCTION_RND.ordinal()] = true;
			log_all.bits[Severity.PARALLEL_UNQUALIFIED.ordinal()] = true;
			log_all.bits[Severity.PARALLEL_PORTCONN.ordinal()] = true;
			log_all.bits[Severity.PARALLEL_PORTMAP.ordinal()] = true;
			log_all.bits[Severity.PARALLEL_PTC.ordinal()] = true;
			log_all.bits[Severity.TESTCASE_UNQUALIFIED.ordinal()] = true;
			log_all.bits[Severity.TESTCASE_START.ordinal()] = true;
			log_all.bits[Severity.TESTCASE_FINISH.ordinal()] = true;
			log_all.bits[Severity.PORTEVENT_UNQUALIFIED.ordinal()] = true;
			log_all.bits[Severity.PORTEVENT_DUALRECV.ordinal()] = true;
			log_all.bits[Severity.PORTEVENT_DUALSEND.ordinal()] = true;
			log_all.bits[Severity.PORTEVENT_MCRECV.ordinal()] = true;
			log_all.bits[Severity.PORTEVENT_MCSEND.ordinal()] = true;
			log_all.bits[Severity.PORTEVENT_MMRECV.ordinal()] = true;
			log_all.bits[Severity.PORTEVENT_MMSEND.ordinal()] = true;
			log_all.bits[Severity.PORTEVENT_MQUEUE.ordinal()] = true;
			log_all.bits[Severity.PORTEVENT_PCIN.ordinal()] = true;
			log_all.bits[Severity.PORTEVENT_PCOUT.ordinal()] = true;
			log_all.bits[Severity.PORTEVENT_PMIN.ordinal()] = true;
			log_all.bits[Severity.PORTEVENT_PMOUT.ordinal()] = true;
			log_all.bits[Severity.PORTEVENT_PQUEUE.ordinal()] = true;
			log_all.bits[Severity.PORTEVENT_SETSTATE.ordinal()] = true;
			log_all.bits[Severity.PORTEVENT_STATE.ordinal()] = true;
			log_all.bits[Severity.STATISTICS_UNQUALIFIED.ordinal()] = true;
			log_all.bits[Severity.STATISTICS_VERDICT.ordinal()] = true;
			log_all.bits[Severity.TIMEROP_UNQUALIFIED.ordinal()] = true;
			log_all.bits[Severity.TIMEROP_GUARD.ordinal()] = true;
			log_all.bits[Severity.TIMEROP_READ.ordinal()] = true;
			log_all.bits[Severity.TIMEROP_START.ordinal()] = true;
			log_all.bits[Severity.TIMEROP_STOP.ordinal()] = true;
			log_all.bits[Severity.TIMEROP_TIMEOUT.ordinal()] = true;
			log_all.bits[Severity.USER_UNQUALIFIED.ordinal()] = true;
			log_all.bits[Severity.VERDICTOP_UNQUALIFIED.ordinal()] = true;
			log_all.bits[Severity.VERDICTOP_FINAL.ordinal()] = true;
			log_all.bits[Severity.VERDICTOP_GETVERDICT.ordinal()] = true;
			log_all.bits[Severity.VERDICTOP_SETVERDICT.ordinal()] = true;
			log_all.bits[Severity.WARNING_UNQUALIFIED.ordinal()] = true;
		}

		public Logging_Bits() {
			//do nothing
		}

		public Logging_Bits(final Logging_Bits other) {
			System.arraycopy(other.bits, 0, bits, 0, other.bits.length);
		}
	}

	static log_mask_struct console_log_mask = new log_mask_struct();
	static log_mask_struct file_log_mask = new log_mask_struct();
	static log_mask_struct emergency_log_mask = new log_mask_struct();

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
	public static enum emergency_logging_behaviour_t { BUFFER_ALL, BUFFER_MASKED };

	public static enum matching_verbosity_t { VERBOSITY_COMPACT, VERBOSITY_FULL };

	//public static void set_timestamp_format(timestamp_format_t = new timestamp_format);
	//public static timestamp_format_t timestamp_format = TIMESTAMP_TIME;
	public static void set_matching_verbosity(final matching_verbosity_t v) {
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

	static StringBuilder logMatchBuffer = new StringBuilder();
	static boolean logMatchPrinted = false;
	static matching_verbosity_t matching_verbosity = matching_verbosity_t.VERBOSITY_COMPACT;
	static emergency_logging_behaviour_t emergency_logging_behaviour = emergency_logging_behaviour_t.BUFFER_MASKED;

	// length of the emergency logging buffer
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

		emergency_log_mask.component_id.id_selector = component_id_selector_enum.COMPONENT_ID_ALL;
		emergency_log_mask.component_id.id_compref=TitanComponent.ANY_COMPREF;
		emergency_log_mask.mask = new Logging_Bits(Logging_Bits.log_all);		

		// TODO initialize emergency buffer too.
	}

	public static void terminate_logger() {
		//empty for now
	}

	public static void log(final Severity msg_severity, final String formatString, final Object... args ) {
		log_va_list(msg_severity, formatString, args);
	}

	public static void log_str(final Severity msg_severity, final String string ) {
		if (!log_this_event(msg_severity) && get_emergency_logging() <= 0) {
			return;
		}

		log_unhandled_event(msg_severity, string == null ? "<NULL pointer>": string);
		logMatchPrinted = false;
	}

	public static void log_va_list(final Severity msg_severity, final String formatString, final Object... args) {
		if (!log_this_event(msg_severity) && get_emergency_logging() <= 0) {
			return;
		}

		log_unhandled_event(msg_severity, String.format(Locale.US, formatString, args));
		logMatchPrinted = false;
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
				log_unhandled_event(current_event.severity, current_event.buffer.toString());
			}

			events.pop();
			if (!events.isEmpty()) {
				current_event = events.peek();
			} else {
				current_event = null;
			}
		}
		logMatchPrinted = false;
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

			logMatchPrinted = false;

			return ret_val;
		}

		logMatchPrinted = false;

		return new TitanCharString();
	}

//	private static void log_line(final Severity msg_severity, final String message) {
//		//// TODO this is a temporal implementation to display only console logs, until file logging is also supported.
//		if (!should_log_to_console(msg_severity)) {
//			return;
//		}
//
//		long timestamp = System.currentTimeMillis(); //TODO: time zone is not handled yet!
//		final long millisec = timestamp % 1000;
//		timestamp = timestamp / 1000;
//		final long secs = timestamp % 60;
//		timestamp = timestamp / 60;
//		final long minutes = timestamp % 60;
//		timestamp = timestamp / 60;
//		final long hours = timestamp % 24;
////		timestamp = timestamp / 24; //not used yet
//		final StringBuilder temp = new StringBuilder(20 + message.length());
//		temp.append(String.format("%02d", hours)).append(':').append(String.format("%02d", minutes)).append(':').append(String.format("%02d", secs)).append('.').append(String.format("%03d", millisec)).append("000");
//		temp.append(' ').append(message);
//
//		System.out.println(temp);
//	}

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
		logMatchPrinted = false;
	}

	public static void log_event_va_list(final String formatString, final Object... args) {
		if (current_event != null) {
			current_event.buffer.append(String.format(Locale.US, formatString, args));
		}
		logMatchPrinted = false;
	}

	public static void log_char(final char c) {
		// TODO: correct log_char
		if (current_event != null) {
			current_event.buffer.append(c);
		}
		logMatchPrinted = false;
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
		} else if (should_log_to_emergency(event_severity) && (get_emergency_logging() > 0)) {
			return true;
		} else {
			return false;
		}
	}

	public static boolean should_log_to_file(final Severity sev) {
		return file_log_mask.mask.bits[sev.ordinal()];
	}

	public static boolean should_log_to_console(final Severity sev) {
		if (sev == Severity.EXECUTOR_EXTCOMMAND) {
			return true;
		}

		return console_log_mask.mask.bits[sev.ordinal()];
	}

	public static boolean should_log_to_emergency(final Severity sev) {
		return emergency_log_mask.mask.bits[sev.ordinal()];
	}

	/*public static void set_timestamp_format(timestamp_format_t new_timestamp_format){
		timestamp_format = new_timestamp_format;
	}*/

	/**
	 * The internal logging function representing the interface between the logger and the loggerPluginManager.
	 * 
	 * log(const API::TitanLogEvent& event) in the LoggerPluginManager
	 * not yet using the event objects to save on complexity and runtime cost.
	 *
	 * quickly becoming deprecated
	 * */
	private static void log(final TitanLoggerApi.TitanLogEvent event) {
		//FIXME more complicated
		internal_log_to_all(event, false, false, false);
	}

	/**
	 * The internal logging function of the LoggerPluginManager towards the plugins themselves.
	 *
	 * This will be sending of the event to be logged to the logger plugins later,
	 * Right now we only have one (legacy) logger simulated within this same class.
	 * */
	private static void internal_log_to_all(final TitanLoggerApi.TitanLogEvent event, final boolean log_buffered, final boolean separate_file, final boolean use_emergency_mask) {
		//right now it behaves as if we have only the legacy logger installed
		//FIXME more complicated
		log(event, log_buffered, separate_file, use_emergency_mask);
	}

	/**
	 * This function represents the entry point for the legacy style logger plugin.
	 * (still embedded in this generic class while transitioning the design)
	 * */
	private static void log(final TitanLoggerApi.TitanLogEvent event, final boolean log_buffered, final boolean separate_file, final boolean use_emergency_mask) {
		if (separate_file) {
			//FIXME implement
		}

		final int severityIndex = event.getSeverity().getInt();
		final Severity severity = Severity.values()[severityIndex];
		if (use_emergency_mask) {
			//FIXME implement file logging
			if (should_log_to_console(severity)) {
				log_console(event, severity);
			}
		} else {
			//FIXME implement file logging
			if (should_log_to_console(severity)) {
				log_console(event, severity);
			}
		}
	}

	/**
	 * The log_console function from the legacy logger.
	 *
	 * Not the final implementation though.
	 * */
	private static void log_console(final TitanLoggerApi.TitanLogEvent event, final Severity msg_severity) {
		//FIXME once we have objects calculating the time will have to be moved earlier.
		//FIXME a bit more complicated in reality
		long timestamp = System.currentTimeMillis(); //TODO: time zone is not handled yet!
		final long millisec = timestamp % 1000;
		timestamp = timestamp / 1000;
		final long secs = timestamp % 60;
		timestamp = timestamp / 60;
		final long minutes = timestamp % 60;
		timestamp = timestamp / 60;
		final long hours = timestamp % 24;
//		timestamp = timestamp / 24; //not used yet
		//Time
		final String event_str = event_to_string(event, true);
		final StringBuilder temp = new StringBuilder(20 + event_str.length());
		temp.append(String.format("%02d", hours)).append(':').append(String.format("%02d", minutes)).append(':').append(String.format("%02d", secs)).append('.').append(String.format("%03d", millisec)).append("000");
		temp.append(' ').append(event_str);

		// DateTime
		// TODO: SECONDS ARE NOT HANDLED YET
		//final Date datum = new Date();
		//final SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MMM/dd" + " " + "HH:mm:ss.SSSSSS");

		// TODO: Seconds
		// final SimpleDateFormat sdf2 = new SimpleDateFormat("SSS");
		
		//temp.append(sdf.format(datum));
		System.out.println(temp);
	}

	private static String event_to_string(final TitanLoggerApi.TitanLogEvent event, final boolean without_header) {
		//FIXME implement header handling
		final StringBuilder returnValue = new StringBuilder();
		final LogEventType_choice choice = event.getLogEvent().getChoice();
		switch(choice.get_selection()) {
		case UNBOUND_VALUE:
			return returnValue.toString();
		case ALT_UnhandledEvent:
			returnValue.append(choice.getUnhandledEvent().getValue());
			break;
		case ALT_TimerEvent:
			timer_event_str(returnValue, choice.getTimerEvent().getChoice());
			break;
		case ALT_VerdictOp:
			verdictop_str(returnValue, choice.getVerdictOp().getChoice());
			break;
		case ALT_Statistics:
			statistics_str(returnValue, choice.getStatistics().getChoice());
			break;
		case ALT_TestcaseOp:
			testcaseop_str(returnValue, choice.getTestcaseOp().getChoice());
			break;
		case ALT_DefaultEvent:
			defaultop_event_str(returnValue, choice.getDefaultEvent().getChoice());
			break;
		case ALT_MatchingEvent:
			matchingop_str(returnValue, choice.getMatchingEvent().getChoice());
			break;
		case ALT_PortEvent:
			portevent_str(returnValue, choice.getPortEvent().getChoice());
			break;
		case ALT_FunctionEvent: {
			switch (choice.getFunctionEvent().getChoice().get_selection()) {
			case ALT_Random : {
				final FunctionEvent_choice_random ra = choice.getFunctionEvent().getChoice().getRandom();
				switch (ra.getOperation().enum_value) {
				case seed:
					returnValue.append(MessageFormat.format( "Random number generator was initialized with seed {0}: {1}", ra.getRetval().getValue(), ra.getIntseed().getInt()));
					break;
				case read__out:
					returnValue.append(MessageFormat.format("Function rnd() returned {0}.", ra.getRetval().getValue()));
					break;
				case UNBOUND_VALUE:
				case UNKNOWN_VALUE:
				default:
					break;
				}
				break;
			}
			default:
				break;
			}
			break;
		}
		//FIXME implement missing branches
		}
		return returnValue.toString();
	}

	private static void timer_event_str(final StringBuilder returnValue, final TimerEvent_choice choice) {
		switch (choice.get_selection()) {
		case ALT_ReadTimer:{
			final TimerType timer = choice.getReadTimer();
			returnValue.append(MessageFormat.format("Read timer {0}: {1} s", timer.getName().getValue(), timer.getValue__().getValue()));
			break;}
		case ALT_StartTimer: {
			final TimerType timer = choice.getStartTimer();
			returnValue.append(MessageFormat.format("Start timer {0}: {1} s", timer.getName().getValue(), timer.getValue__().getValue()));
			break;}
		case ALT_GuardTimer: {
			final TimerGuardType timer = choice.getGuardTimer();
			returnValue.append(MessageFormat.format("Test case guard timer was set to {0} s", timer.getValue__().getValue()));
			break;}
		case ALT_StopTimer: {
			final TimerType timer = choice.getStopTimer();
			returnValue.append(MessageFormat.format("Stop timer {0}: {1} s", timer.getName().getValue(), timer.getValue__().getValue()));
			break;}
		case ALT_TimeoutTimer: {
			final TimerType timer = choice.getTimeoutTimer();
			returnValue.append(MessageFormat.format("Timeout {0}: {1} s", timer.getName().getValue(), timer.getValue__().getValue()));
			break;}
		case ALT_TimeoutAnyTimer: {
			returnValue.append("Operation `any timer.timeout' was successful.");
			break;}
		case ALT_UnqualifiedTimer: {
			returnValue.append(choice.getUnqualifiedTimer().getValue());
			break;}
		//FIXME implement missing branches
		}
	}

	private static void defaultop_event_str(final StringBuilder returnValue, final DefaultEvent_choice choice) {
		switch(choice.get_selection()) {
		case ALT_DefaultopActivate: {
			final DefaultOp dflt = choice.getDefaultopActivate();
			returnValue.append(MessageFormat.format("Altstep {0} was activated as default, id {1}", dflt.getName().getValue(), dflt.getId().getInt()));
			break;
		}
		//FIXME implement missing branches
		}
	}

	private static void verdictop_str(final StringBuilder returnValue, final VerdictOp_choice choice) {
		final SetVerdictType set = choice.getSetVerdict();
		final int newOrdinal = set.getNewVerdict().enum_value.ordinal();
		final String newVerdictName = VerdictTypeEnum.values()[newOrdinal].getName();
		final int oldOrdinal = set.getOldVerdict().enum_value.ordinal();
		final String oldVerdictName = VerdictTypeEnum.values()[oldOrdinal].getName();
		final int localOrdinal = set.getLocalVerdict().enum_value.ordinal();
		final String localVerdictName = VerdictTypeEnum.values()[localOrdinal].getName();

		if (set.getNewVerdict().isGreaterThan(set.getOldVerdict())) {
			if (!set.getOldReason().isPresent() || !set.getNewReason().isPresent()) {
				returnValue.append(MessageFormat.format("setverdict({0}): {1} -> {2}", newVerdictName, oldVerdictName, localVerdictName));
			} else {
				returnValue.append(MessageFormat.format("setverdict({0}): {1} -> {2} reason: \"{3}\", new component reason: \"{4}\"", newVerdictName, oldVerdictName, localVerdictName, set.getOldReason().get().getValue(), set.getNewReason().get().getValue()));
			}
		} else {
			if (!set.getOldReason().isPresent() || !set.getNewReason().isPresent()) {
				returnValue.append(MessageFormat.format("setverdict({0}): {1} -> {2} component reason not changed", newVerdictName, oldVerdictName, localVerdictName));
			} else {
				returnValue.append(MessageFormat.format("setverdict({0}): {1} -> {2} reason: \"{3}\", component reason not changed", newVerdictName, oldVerdictName, localVerdictName, set.getOldReason().get().getValue()));
			}
		}
	}

	private static void statistics_str(final StringBuilder returnValue, final StatisticsType_choice choice) {
		switch(choice.get_selection()) {
		case ALT_ControlpartStart:
			returnValue.append(MessageFormat.format("Execution of control part in module {0} started.", choice.getControlpartStart().getValue()));
			break;
		case ALT_ControlpartFinish:
			returnValue.append(MessageFormat.format("Execution of control part in module {0} finished.", choice.getControlpartFinish().getValue()));
			break;
			//FIXME implement the rest of the branches
		}
	}

	private static void testcaseop_str(final StringBuilder returnValue, final TestcaseEvent_choice choice) {
		switch(choice.get_selection()) {
		case ALT_TestcaseStarted:
			returnValue.append(MessageFormat.format("Test case {0} started.", choice.getTestcaseStarted().getTestcase__name().getValue()));
			break;
		case ALT_TestcaseFinished:
			final int ordinal = choice.getTestcaseFinished().getVerdict().enum_value.ordinal();
			final String verdictName = VerdictTypeEnum.values()[ordinal].getName();
			returnValue.append(MessageFormat.format("Test case {0} finished. Verdict: {1}", choice.getTestcaseFinished().getName().getTestcase__name().getValue(), verdictName));
			break;
		case UNBOUND_VALUE:
		default:
			break;
		}
	}

	private static void matchingop_str(final StringBuilder returnValue, final MatchingEvent_choice choice) {
		switch (choice.get_selection()) {
		case ALT_MatchingTimeout: {
			final MatchingTimeout mt = choice.getMatchingTimeout();
			if (mt.getTimer__name().isPresent()) {
				returnValue.append(MessageFormat.format("Timeout operation on timer {0} failed: The timer is not started.", mt.getTimer__name().get().getValue()));
			} else {
				returnValue.append("Operation `any timer.timeout' failed: The test component does not have active timers.");
			}
			break;
		}
		case ALT_MatchingFailure: {
			final MatchingFailureType mf = choice.getMatchingFailure();
			boolean is_call = false;
			switch (mf.getReason().enum_value) {
			case message__does__not__match__template:
				returnValue.append(MessageFormat.format("Matching on port {0} {1}: First message in the queue does not match the template: ", mf.getPort__name().getValue(), mf.getInfo().getValue()));
				break;
			case exception__does__not__match__template:
				returnValue.append(MessageFormat.format("Matching on port {0} failed: The first exception in the queue does not match the template: {1}", mf.getPort__name().getValue(), mf.getInfo().getValue()));
				break;
			case parameters__of__call__do__not__match__template:
				is_call = true; // fall through
			case parameters__of__reply__do__not__match__template:
				returnValue.append(MessageFormat.format("Matching on port {0} failed: The parameters of the first {1} in the queue do not match the template: {2}", mf.getPort__name().getValue(), is_call ? "call" : "reply", mf.getInfo().getValue()));
				break;
			case sender__does__not__match__from__clause:
				returnValue.append(MessageFormat.format("Matching on port {0} failed: Sender of the first entity in the queue does not match the from clause: {1}", mf.getPort__name().getValue(), mf.getInfo().getValue()));
				break;
			case sender__is__not__system:
				returnValue.append(MessageFormat.format("Matching on port {0} failed: Sender of the first entity in the queue is not the system.", mf.getPort__name().getValue()));
				break;
			case not__an__exception__for__signature:
				returnValue.append(MessageFormat.format("Matching on port {0} failed: The first entity in the queue is not an exception for signature {1}.", mf.getPort__name().getValue(), mf.getInfo().getValue()));
				break;
			default:
				break;
			}
			break;
		}
		case ALT_MatchingSuccess: {
			final MatchingSuccessType ms = choice.getMatchingSuccess();
			returnValue.append(MessageFormat.format("Matching on port {0} succeeded: {1}", ms.getPort__name().getValue(), ms.getInfo().getValue()));
			break;
		}
		case ALT_MatchingProblem: {
			MatchingProblemType mp = choice.getMatchingProblem();
			returnValue.append("Operation `");
			if (mp.getAny__port().getValue()) {
				returnValue.append("any port.");
			}

			if (mp.getCheck__().getValue()) {
				returnValue.append("check(");
			}
			switch (mp.getOperation().enum_value) {
			case receive__:
				returnValue.append("receive");
				break;
			case trigger__:
				returnValue.append("trigger");
				break;
			case getcall__:
				returnValue.append("getcall");
				break;
			case getreply__:
				returnValue.append("getreply");
				break;
			case catch__:
				returnValue.append("catch");
				break;
			case check__:
				returnValue.append("check");
				break;
			default:
				break;
			}
			if (mp.getCheck__().getValue()) {
				returnValue.append(')');
			}
			returnValue.append("' ");

			if (mp.getPort__name().isBound()) {
				returnValue.append(MessageFormat.format("on port {0} ", mp.getPort__name().getValue()));
			}
			// we could also check that any__port is false

			returnValue.append("failed: ");

			switch (mp.getReason().enum_value) {
			case component__has__no__ports:
				returnValue.append("The test component does not have ports.");
				break;
			case no__incoming__signatures:
				returnValue.append("The port type does not have any incoming signatures.");
				break;
			case no__incoming__types:
				returnValue.append("The port type does not have any incoming message types.");
				break;
			case no__outgoing__blocking__signatures:
				returnValue.append("The port type does not have any outgoing blocking signatures.");
				break;
			case no__outgoing__blocking__signatures__that__support__exceptions:
				returnValue.append("The port type does not have any outgoing blocking signatures that support exceptions.");
				break;
			case port__not__started__and__queue__empty:
				returnValue.append("Port is not started and the queue is empty.");
				break;
			default:
				break;
			}
			break;
		}
		//FIXME implement the rest of the branches
		}
	}

	private static void portevent_str(final StringBuilder returnValue, final PortEvent_choice choice) {
		switch (choice.get_selection()) {
		case ALT_PortQueue: {
			final Port__Queue portQueue = choice.getPortQueue();
			switch (portQueue.getOperation().enum_value) {
			case enqueue__msg:{
				final String comp_str = TitanComponent.get_component_string(portQueue.getCompref().getInt());
				returnValue.append(MessageFormat.format("Message enqueued on {0} from {1}{2}{3} id {4}", portQueue.getPort__name().getValue() , comp_str, portQueue.getAddress__().getValue(), portQueue.getParam__().getValue(), portQueue.getMsgid().getInt()));
				break;}
			case enqueue__call:{
				final String comp_str = TitanComponent.get_component_string(portQueue.getCompref().getInt());
				returnValue.append(MessageFormat.format("Call enqueued on {0} from {1}{2}{3} id {4}", portQueue.getPort__name().getValue() , comp_str, portQueue.getAddress__().getValue(), portQueue.getParam__().getValue(), portQueue.getMsgid().getInt()));
				break;}
			case enqueue__reply:{
				final String comp_str = TitanComponent.get_component_string(portQueue.getCompref().getInt());
				returnValue.append(MessageFormat.format("Reply enqueued on {0} from {1}{2}{3} id {4}", portQueue.getPort__name().getValue() , comp_str, portQueue.getAddress__().getValue(), portQueue.getParam__().getValue(), portQueue.getMsgid().getInt()));
				break;}
			case enqueue__exception:{
				final String comp_str = TitanComponent.get_component_string(portQueue.getCompref().getInt());
				returnValue.append(MessageFormat.format("Exception enqueued on {0} from {1}{2}{3} id {4}", portQueue.getPort__name().getValue() , comp_str, portQueue.getAddress__().getValue(), portQueue.getParam__().getValue(), portQueue.getMsgid().getInt()));
				break;}
			case extract__msg:
				returnValue.append(MessageFormat.format("Message with id {0} was extracted from the queue of {1}.", portQueue.getMsgid().getInt(), portQueue.getPort__name().getValue()));
				break;
			case extract__op:
				returnValue.append(MessageFormat.format("Operation with id {0} was extracted from the queue of {1}.", portQueue.getMsgid().getInt(), portQueue.getPort__name().getValue()));
				break;
			default:
				break;
			}
			break;
		}
		case ALT_PortState: {
			final Port__State ps = choice.getPortState();
			String what = "";
			switch (ps.getOperation().enum_value) {
			case started:
				what = "started";
				break;
			case stopped:
				what = "stopped";
				break;
			case halted:
				what = "halted";
				break;
			default:
				return;
			}
			returnValue.append(MessageFormat.format("Port {0} was {1}.", ps.getPort__name().getValue(), what));
			break;
		}
		case ALT_ProcPortSend: {
			Proc__port__out ps = choice.getProcPortSend();
			final String dest;
			if (ps.getCompref().getInt() == TitanComponent.SYSTEM_COMPREF) {
				dest = ps.getSys__name().getValue().toString();
			} else {
				dest = TitanComponent.get_component_string(ps.getCompref().getInt());
			}
			
			switch (ps.getOperation().enum_value) {
			case call__op:
				returnValue.append("Called");
				break;
			case reply__op:
				returnValue.append("Replied");
				break;
			case exception__op:
				returnValue.append("Raised");
			default:
				return;
			}

			returnValue.append(MessageFormat.format(" on {0} to {1} {2}", ps.getPort__name().getValue(), dest, ps.getParameter().getValue()));
			break;
		}
		case ALT_ProcPortRecv: {
			final Proc__port__in ps = choice.getProcPortRecv();
			String op2 = "";
			switch (ps.getOperation().enum_value) {
			case call__op:
				returnValue.append(ps.getCheck__().getValue() ? "Check-getcall" : "Getcall");
				op2 = "call";
				break;
			case reply__op:
				returnValue.append(ps.getCheck__().getValue() ? "Check-getreply" : "Getreply");
				op2 = "reply";
			case exception__op:
				returnValue.append(ps.getCheck__().getValue() ? "Check-catch" : "Catch");
				op2 = "exception";
			default:
				return;
			}

			final String source = TitanComponent.get_component_string(ps.getCompref().getInt());
			returnValue.append(MessageFormat.format(" operation on port {0} succeeded, {1} from {2}: {3} id {4}", ps.getPort__name().getValue(), op2, source, ps.getParameter().getValue(), ps.getMsgid().getInt()));
			break;
		}
		case ALT_MsgPortSend: {
			final Msg__port__send ms = choice.getMsgPortSend();
			final String dest = TitanComponent.get_component_string(ms.getCompref().getInt());
			returnValue.append(MessageFormat.format("Sent on {0} to {1}{2}", ms.getPort__name().getValue(), dest, ms.getParameter().getValue()));
			break;
		}
		case ALT_MsgPortRecv: {
			final Msg__port__recv ms = choice.getMsgPortRecv();
			switch (ms.getOperation().enum_value) {
			case receive__op:
				returnValue.append("Receive");
				break;
			case check__receive__op:
				returnValue.append("Check-receive");
				break;
			case trigger__op:
				returnValue.append("Trigger");
				break;
			default:
				return;
			}

			returnValue.append(MessageFormat.format(" operation on port {0} succeeded, message from ", ms.getPort__name().getValue()));
			if (ms.getCompref().getInt() == TitanComponent.SYSTEM_COMPREF) {
				returnValue.append(MessageFormat.format("system({0})", ms.getSys__name().getValue()));
			} else {
				final String dest = TitanComponent.get_component_string(ms.getCompref().getInt());
				returnValue.append(dest);
			}

			returnValue.append(MessageFormat.format("{0} id {1}", ms.getParameter().getValue(), ms.getMsgid().getInt()));
			break;
		}
		case ALT_DualMapped: {
			final Dualface__mapped dual = choice.getDualMapped();
			returnValue.append(MessageFormat.format("{0} message was mapped to {1} : {2}", (dual.getIncoming().getValue() ? "Incoming" : "Outgoing"), dual.getTarget__type().getValue(), dual.getValue__().getValue()));
			if (dual.getIncoming().getValue()) {
				returnValue.append(MessageFormat.format(" id {0}", dual.getMsgid().getInt()));
			}
			break;
		}
		case ALT_PortMisc: {
			final Port__Misc portMisc = choice.getPortMisc();
			final String comp_str = TitanComponent.get_component_string(portMisc.getRemote__component().getInt());
			switch (portMisc.getReason().enum_value) {
			case removing__unterminated__connection:
				returnValue.append(MessageFormat.format("Removing unterminated connection between port {0} and {1}:{2}.", portMisc.getPort__name().getValue(), comp_str, portMisc.getRemote__port().getValue()));
				break;
			case removing__unterminated__mapping:
				returnValue.append(MessageFormat.format("Removing unterminated mapping between port {0} and system:{1}.", portMisc.getPort__name().getValue(), portMisc.getRemote__port().getValue()));
				break;
			case port__was__cleared:
				returnValue.append(MessageFormat.format("Port {0} was cleared.", portMisc.getPort__name().getValue()));
				break;
			case local__connection__established:
				returnValue.append(MessageFormat.format("Port {0} has established the connection with local port {1}.", portMisc.getPort__name().getValue(), portMisc.getRemote__port().getValue()));
				break;
			case local__connection__terminated:
				returnValue.append(MessageFormat.format("Port {0} has terminated the connection with local port {1}.", portMisc.getPort__name().getValue(), portMisc.getRemote__port().getValue()));
				break;
			case port__is__waiting__for__connection__tcp:
				returnValue.append(MessageFormat.format("Port {0} is waiting for connection from {1}:{2} on TCP port {3}:{4}.", portMisc.getPort__name().getValue(), comp_str, portMisc.getRemote__port().getValue(), portMisc.getIp__address().getValue(), portMisc.getTcp__port().getInt()));
				break;
			case port__is__waiting__for__connection__unix:
				returnValue.append(MessageFormat.format("Port {0} is waiting for connection from {1}:{2} on UNIX pathname {3}.", portMisc.getPort__name().getValue(), comp_str, portMisc.getRemote__port().getValue(), portMisc.getIp__address().getValue()));
				break;
			case connection__established:
				returnValue.append(MessageFormat.format("Port {0} has established the connection with {1}:{2} using transport type {3}.", portMisc.getPort__name().getValue(), comp_str, portMisc.getRemote__port().getValue(), portMisc.getIp__address().getValue()));
				break;
			case destroying__unestablished__connection:
				returnValue.append(MessageFormat.format("Destroying unestablished connection of port {0} to {1}:{2} because the other endpoint has terminated.", portMisc.getPort__name().getValue(), comp_str, portMisc.getRemote__port().getValue()));
				break;
			case terminating__connection:
				returnValue.append(MessageFormat.format("Terminating the connection of port {0} to {1}:{2}. No more messages can be sent through this connection.", portMisc.getPort__name().getValue(), comp_str, portMisc.getRemote__port().getValue()));
				break;
			case sending__termination__request__failed:
				returnValue.append(MessageFormat.format("Sending the connection termination request on port {0} to remote endpoint {1}:}{2} failed.", portMisc.getPort__name().getValue(), comp_str, portMisc.getRemote__port().getValue()));
				break;
			case termination__request__received:
				returnValue.append(MessageFormat.format("Connection termination request was received on port {0} from {1}:{2}. No more data can be sent or received through this connection.", portMisc.getPort__name().getValue(), comp_str, portMisc.getRemote__port().getValue()));
				break;
			case acknowledging__termination__request__failed:
				returnValue.append(MessageFormat.format("Sending the acknowledgment for connection termination request on port {0} to remote endpoint {1}:{2} failed.", portMisc.getPort__name().getValue(), comp_str, portMisc.getRemote__port().getValue()));
				break;
			case sending__would__block:
				returnValue.append(MessageFormat.format("Sending data on the connection of port {0} to {1}:{2} would block execution. The size of the outgoing buffer was increased from {3} to {4} bytes.", portMisc.getPort__name().getValue(), comp_str, portMisc.getRemote__port().getValue(), portMisc.getTcp__port().getInt(), portMisc.getNew__size().getInt()));
				break;
			case connection__accepted:
				returnValue.append(MessageFormat.format("Port {0} has accepted the connection from {1}:{2}.", portMisc.getPort__name().getValue(), comp_str, portMisc.getRemote__port().getValue()));
				break;
			case connection__reset__by__peer:
				returnValue.append(MessageFormat.format("Connection of port {0} to {1}:{2} was reset by the peer.", portMisc.getPort__name().getValue(), comp_str, portMisc.getRemote__port().getValue()));
				break;
			case connection__closed__by__peer:
				returnValue.append(MessageFormat.format("Connection of port {0} to {1}:{2} was closed unexpectedly by the peer.", portMisc.getPort__name().getValue(), comp_str, portMisc.getRemote__port().getValue()));
				break;
			case port__disconnected:
				returnValue.append(MessageFormat.format("Port {0} was disconnected from {1}:{2}.", portMisc.getPort__name().getValue(), comp_str, portMisc.getRemote__port().getValue()));
				break;
			case port__was__mapped__to__system:
				returnValue.append(MessageFormat.format("Port {0} was mapped to system:{1}.", portMisc.getPort__name().getValue(), portMisc.getRemote__port().getValue()));
				break;
			case port__was__unmapped__from__system:
				returnValue.append(MessageFormat.format("Port {0} was unmapped from system:{1}.", portMisc.getPort__name().getValue(), portMisc.getRemote__port().getValue()));
				break;
			default:
				break;
			}
			break;
		}
		//FIXME implement rest
		}
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

	public static void log_unhandled_event(final Severity severity, final String message) {
		if (!log_this_event(severity) && get_emergency_logging() <= 0) {
			return;
		}

		final TitanLogEvent event = new TitanLogEvent();
		fill_common_fields(event, severity);
		event.getLogEvent().getChoice().getUnhandledEvent().assign(message);

		log(event);
	}

	public static void log_timer_read(final String timer_name, final double timeout_val) {
		if (!log_this_event(Severity.TIMEROP_READ) && get_emergency_logging() <= 0) {
			return;
		}

		final TitanLogEvent event = new TitanLogEvent();
		fill_common_fields(event, Severity.TIMEROP_READ);
		final TimerType timer = event.getLogEvent().getChoice().getTimerEvent().getChoice().getReadTimer();
		timer.getName().assign(timer_name);
		timer.getValue__().assign(timeout_val);

		log(event);
	}

	public static void log_timer_start(final String timer_name, final double start_val) {
		if (!log_this_event(Severity.TIMEROP_START) && get_emergency_logging() <= 0) {
			return;
		}

		final TitanLogEvent event = new TitanLogEvent();
		fill_common_fields(event, Severity.TIMEROP_START);
		final TimerType timer = event.getLogEvent().getChoice().getTimerEvent().getChoice().getStartTimer();
		timer.getName().assign(timer_name);
		timer.getValue__().assign(start_val);

		log(event);
	}

	public static void log_timer_guard(final double start_val) {
		if (!log_this_event(Severity.TIMEROP_GUARD) && get_emergency_logging() <= 0) {
			return;
		}

		final TitanLogEvent event = new TitanLogEvent();
		fill_common_fields(event, Severity.TIMEROP_GUARD);
		final TimerGuardType timer = event.getLogEvent().getChoice().getTimerEvent().getChoice().getGuardTimer();
		timer.getValue__().assign(start_val);

		log(event);
	}

	public static void log_timer_stop(final String timer_name, final double stop_val) {
		if (!log_this_event(Severity.TIMEROP_STOP) && get_emergency_logging() <= 0) {
			return;
		}

		final TitanLogEvent event = new TitanLogEvent();
		fill_common_fields(event, Severity.TIMEROP_STOP);
		final TimerType timer = event.getLogEvent().getChoice().getTimerEvent().getChoice().getStopTimer();
		timer.getName().assign(timer_name);
		timer.getValue__().assign(stop_val);

		log(event);
	}

	public static void log_timer_timeout(final String timer_name, final double timeout_val) {
		if (!log_this_event(Severity.TIMEROP_TIMEOUT) && get_emergency_logging() <= 0) {
			return;
		}

		final TitanLogEvent event = new TitanLogEvent();
		fill_common_fields(event, Severity.TIMEROP_TIMEOUT);
		final TimerType timer = event.getLogEvent().getChoice().getTimerEvent().getChoice().getTimeoutTimer();
		timer.getName().assign(timer_name);
		timer.getValue__().assign(timeout_val);

		log(event);
	}

	public static void log_timer_any_timeout() {
		if (!log_this_event(Severity.TIMEROP_TIMEOUT) && get_emergency_logging() <= 0) {
			return;
		}

		final TitanLogEvent event = new TitanLogEvent();
		fill_common_fields(event, Severity.TIMEROP_TIMEOUT);
		event.getLogEvent().getChoice().getTimerEvent().getChoice().getTimeoutAnyTimer().assign(TitanNull_Type.NULL_VALUE);

		log(event);
	}

	public static void log_timer_unqualified(final String message) {
		if (!log_this_event(Severity.TIMEROP_UNQUALIFIED) && get_emergency_logging() <= 0) {
			return;
		}

		final TitanLogEvent event = new TitanLogEvent();
		fill_common_fields(event, Severity.TIMEROP_UNQUALIFIED);
		event.getLogEvent().getChoice().getTimerEvent().getChoice().getUnqualifiedTimer().assign(message);

		log(event);
	}

	public static void log_matching_timeout(final String timer_name) {
		if (!log_this_event(Severity.MATCHING_PROBLEM) && get_emergency_logging() <= 0) {
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

	public static void log_port_queue(final TitanLoggerApi.Port__Queue_operation.enum_type operation, final String port_name, final int componentReference, final int id, final TitanCharString address, final TitanCharString parameter) {
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
		default:
			throw new TtcnError("Invalid operation");
		}

		if (!log_this_event(sev) && get_emergency_logging() <= 0) {
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
	public static void set_start_time() {
		//FIXME implement
	}

	public static void set_file_mask(final component_id_t cmpt,
			final Logging_Bits new_file_mask) {
		if (file_log_mask.component_id.id_selector == component_id_selector_enum.COMPONENT_ID_COMPREF && cmpt.id_selector == component_id_selector_enum.COMPONENT_ID_ALL) {
			return;
		}

		file_log_mask.mask = new_file_mask;
		if (cmpt.id_selector == component_id_selector_enum.COMPONENT_ID_NAME) {
			file_log_mask.component_id.id_selector = component_id_selector_enum.COMPONENT_ID_NAME;
			file_log_mask.component_id.id_name = cmpt.id_name;
		} else {
			file_log_mask.component_id = cmpt;
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

	public static void set_emergency_logging_mask(final component_id_t cmpt,
			final Logging_Bits new_logging_mask) {
		// If Emergency Logging Mask was set with a component-specific value,
		// do not allow overwriting with a generic value.
		if (emergency_log_mask.component_id.id_selector == component_id_selector_enum.COMPONENT_ID_COMPREF && cmpt.id_selector == component_id_selector_enum.COMPONENT_ID_ALL) {
			return;
		}
		emergency_log_mask.mask = new_logging_mask;
		if (cmpt.id_selector == component_id_selector_enum.COMPONENT_ID_NAME) {
			emergency_log_mask.component_id.id_selector = component_id_selector_enum.COMPONENT_ID_NAME;
			emergency_log_mask.component_id.id_name = cmpt.id_name;
		} else {
			emergency_log_mask.component_id = cmpt;
		}
	}

	public static void set_emergency_logging_behaviour(emergency_logging_behaviour_t behaviour){
		emergency_logging_behaviour=behaviour;
	}

	public static emergency_logging_behaviour_t get_emergency_logging_behaviour(){
		return emergency_logging_behaviour;
	}

	public static int get_emergency_logging() {
		return emergency_logging;
	}

	public static void set_emergency_logging(final int size) {
		emergency_logging = size;
	}

	public static void log_port_state(final TitanLoggerApi.Port__State_operation.enum_type operation, final String portname) {
		if (!log_this_event(Severity.PORTEVENT_STATE)) {
			return;
		}

		final TitanLogEvent event = new TitanLogEvent();
		fill_common_fields(event, Severity.PORTEVENT_STATE);
		final Port__State ps = event.getLogEvent().getChoice().getPortEvent().getChoice().getPortState();
		ps.getOperation().assign(operation);
		ps.getPort__name().assign(portname);

		log(event);
	}

	public static void log_procport_send(final String portname, final TitanLoggerApi.Port__oper.enum_type operation, final int componentReference, final TitanCharString system, final TitanCharString parameter) {
		final Severity severity = componentReference == TitanComponent.SYSTEM_COMPREF ? Severity.PORTEVENT_PMOUT : Severity.PORTEVENT_PCOUT;
		if (!log_this_event(severity) && get_emergency_logging() <= 0) {
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

	public static void log_procport_recv(final String portname, final TitanLoggerApi.Port__oper.enum_type operation, final int componentReference, final boolean check, final TitanCharString parameter, final int id) {
		final Severity severity = componentReference == TitanComponent.SYSTEM_COMPREF ? Severity.PORTEVENT_PMIN : Severity.PORTEVENT_PCIN;
		if (!log_this_event(severity) && get_emergency_logging() <= 0) {
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

	public static void log_msgport_send(final String portname, final int componentReference, final TitanCharString parameter) {
		final Severity severity = componentReference == TitanComponent.SYSTEM_COMPREF ? Severity.PORTEVENT_MMSEND : Severity.PORTEVENT_MCSEND;
		if (!log_this_event(severity) && get_emergency_logging() <= 0) {
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

	public static void log_msgport_recv(final String portname, final TitanLoggerApi.Msg__port__recv_operation.enum_type operation, final int componentReference, final TitanCharString system, final TitanCharString parameter, final int id) {
		final Severity severity = componentReference == TitanComponent.SYSTEM_COMPREF ? Severity.PORTEVENT_MMRECV : Severity.PORTEVENT_MCRECV;
		if (!log_this_event(severity) && get_emergency_logging() <= 0) {
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

	public static void log_dualport_map(final boolean incoming, final String target_type, final TitanCharString value, final int id) {
		final Severity severity = incoming ? Severity.PORTEVENT_DUALRECV : Severity.PORTEVENT_DUALSEND;
		if (!log_this_event(severity) && get_emergency_logging() <= 0) {
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

	public static void log_setverdict(final VerdictTypeEnum newVerdict, final VerdictTypeEnum oldVerdict, final VerdictTypeEnum localVerdict,
			final String oldReason, final String newReason) {
		if (!log_this_event(Severity.VERDICTOP_SETVERDICT) && get_emergency_logging() <= 0) {
			return;
		}

		final TitanLogEvent event = new TitanLogEvent();
		fill_common_fields(event, Severity.VERDICTOP_SETVERDICT);
		SetVerdictType set = event.getLogEvent().getChoice().getVerdictOp().getChoice().getSetVerdict();
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

	private static void fill_common_fields(final TitanLogEvent event, final Severity severity) {
		//FIXME implement the rest
		event.getSeverity().assign(severity.ordinal());
	}

	public static void log_testcase_started(final String module_name, final String definition_name ) {
		if (!log_this_event(Severity.TESTCASE_START) && get_emergency_logging() <= 0) {
			return;
		}

		final TitanLogEvent event = new TitanLogEvent();
		fill_common_fields(event, Severity.TESTCASE_START);
		final QualifiedName qname = event.getLogEvent().getChoice().getTestcaseOp().getChoice().getTestcaseStarted();
		qname.getModule__name().assign(module_name);
		qname.getTestcase__name().assign(definition_name);

		log(event);
	}

	//TODO not yet called from generated code
	public static void log_testcase_finished(final String module_name, final String definition_name, final VerdictTypeEnum verdict, final String reason) {
		if (!log_this_event(Severity.TESTCASE_FINISH) && get_emergency_logging() <= 0) {
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

	public static void log_controlpart_start_stop(final String moduleName, final boolean finished) {
		if (!log_this_event(Severity.STATISTICS_UNQUALIFIED) && get_emergency_logging() <= 0) {
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

	public static void log_defaultop_activate(final String name, final int id) {
		if (!log_this_event(Severity.DEFAULTOP_ACTIVATE) && get_emergency_logging() <= 0) {
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


	public static void log_matching_problem(final TitanLoggerApi.MatchingProblemType_reason.enum_type reason, final TitanLoggerApi.MatchingProblemType_operation.enum_type operation, final boolean check, final boolean anyport, final String port_name) {
		if (!log_this_event(TtcnLogger.Severity.MATCHING_PROBLEM) && (get_emergency_logging() <= 0)) {
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

	public static void log_random(final TitanLoggerApi.RandomAction.enum_type rndAction, final double value, final long seed) {
		if (!log_this_event(Severity.FUNCTION_RND) && get_emergency_logging() <= 0) {
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

	public static void log_matching_failure(final TitanLoggerApi.PortType.enum_type port_type, final String port_name, final int compref, final TitanLoggerApi.MatchingFailureType_reason.enum_type reason, final TitanCharString info) {
		Severity sev;
		if (compref == TitanComponent.SYSTEM_COMPREF) {
			sev = (port_type == enum_type.message__) ? Severity.MATCHING_MMUNSUCC : Severity.MATCHING_PMUNSUCC;
		} else {
			sev = (port_type == enum_type.message__) ? Severity.MATCHING_MCUNSUCC : Severity.MATCHING_PCUNSUCC;
		}
		if (!log_this_event(sev) && (get_emergency_logging() <= 0)) {
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

		log(event);
	}

	public static void log_matching_success(final TitanLoggerApi.PortType.enum_type port_type, final String port_name, final int compref, final TitanCharString info) {
		Severity sev;
		if(compref == TitanComponent.SYSTEM_COMPREF) {
			sev = port_type == enum_type.message__ ? Severity.MATCHING_MMSUCCESS : Severity.MATCHING_PMSUCCESS;
		} else {
			sev = port_type == enum_type.message__ ? Severity.MATCHING_MCSUCCESS : Severity.MATCHING_PCSUCCESS;
		}

		if(log_this_event(sev) && get_emergency_logging() <= 0) {
			return;
		}

		final TitanLogEvent event = new TitanLogEvent();
		fill_common_fields(event, sev);
		final MatchingSuccessType ms = event.getLogEvent().getChoice().getMatchingEvent().getChoice().getMatchingSuccess();
		ms.getPort__type().assign(port_type);
		ms.getPort__name().assign(port_name);

		log(event);
	}

	public static void log_port_misc(final TitanLoggerApi.Port__Misc_reason.enum_type reason, final String port_name, final int remote_component, final String remote_port, final String ip_address, final int tcp_port, final int new_size) {
		if (!log_this_event(Severity.PORTEVENT_UNQUALIFIED) && (get_emergency_logging()<=0)) {
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
