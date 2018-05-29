/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import org.eclipse.titan.runtime.core.TitanLoggerApi.ExecutorComponent_reason;
import org.eclipse.titan.runtime.core.TitanLoggerApi.ExecutorConfigdata_reason;
import org.eclipse.titan.runtime.core.TitanLoggerApi.ExecutorUnqualified_reason;
import org.eclipse.titan.runtime.core.TitanLoggerApi.ParPort_operation;
import org.eclipse.titan.runtime.core.TitanLoggerApi.ParallelPTC_reason;
import org.eclipse.titan.runtime.core.TitanVerdictType.VerdictTypeEnum;

/**
 * originally TTCN_Logger
 * @author Arpad Lovassy
 * @author Andrea Palfi
 * @author Gergo Ujhelyi
 */
public final class TtcnLogger {
	private static LoggerPluginManager plugins_;

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

	private static log_mask_struct console_log_mask = new log_mask_struct();
	private static log_mask_struct file_log_mask = new log_mask_struct();
	private static log_mask_struct emergency_log_mask = new log_mask_struct();

	private static timestamp_format_t timestamp_format = timestamp_format_t.TIMESTAMP_TIME;
	private static source_info_format_t source_info_format = source_info_format_t.SINFO_SINGLE;
	private static log_event_types_t log_event_types = log_event_types_t.LOGEVENTTYPES_NO;

	private static final Calendar calendar = Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault(Locale.Category.FORMAT));
	private static final String month_names[] = { "Jan", "Feb", "Mar",
			"Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" };
	private static long start_time;

	private static log_event_types_t log_entity_name = log_event_types_t.LOGEVENTTYPES_NO;

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

	public static class disk_full_action_t {
		disk_full_action_type_t type;
		int retry_interval;
		
		public disk_full_action_t(disk_full_action_type_t disk_type, int retry) {
			type = disk_type;
			retry_interval = retry;
		}
	}
	
	public static enum disk_full_action_type_t {DISKFULL_ERROR, DISKFULL_STOP, DISKFULL_RETRY, DISKFULL_DELETE};
	public static enum timestamp_format_t {TIMESTAMP_TIME, TIMESTAMP_DATETIME, TIMESTAMP_SECONDS};
	public static enum source_info_format_t {SINFO_NONE, SINFO_SINGLE, SINFO_STACK };
	public static enum log_event_types_t {LOGEVENTTYPES_NO, LOGEVENTTYPES_YES, LOGEVENTTYPES_SUBCATEGORIES};
	public static enum emergency_logging_behaviour_t { BUFFER_ALL, BUFFER_MASKED };

	public static enum matching_verbosity_t { VERBOSITY_COMPACT, VERBOSITY_FULL };
	public static enum extcommand_t {EXTCOMMAND_START, EXTCOMMAND_SUCCESS };

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

	/** Sub-category names for all Severity enum values,
	* used when TTCN_Logger::log_event_types is set to log sub-categories */
	public static String severity_subcategory_names[] = {
		  "",
		  // ACTION:
		  "UNQUALIFIED",
		  // DEFAULTOP:
		  "ACTIVATE",
		  "DEACTIVATE",
		  "EXIT",
		  "UNQUALIFIED",
		  // ERROR:
		  "UNQUALIFIED",
		  // EXECUTOR:
		  "RUNTIME",
		  "CONFIGDATA",
		  "EXTCOMMAND",
		  "COMPONENT",
		  "LOGOPTIONS",
		  "UNQUALIFIED",
		  // FUNCTION:
		  "RND",
		  "UNQUALIFIED",
		  // PARALLEL:
		  "PTC",
		  "PORTCONN",
		  "PORTMAP",
		  "UNQUALIFIED",
		  // TESTCASE:
		  "START",
		  "FINISH",
		  "UNQUALIFIED",
		  // PORTEVENT:
		  "PQUEUE",
		  "MQUEUE",
		  "STATE",
		  "PMIN",
		  "PMOUT",
		  "PCIN",
		  "PCOUT",
		  "MMRECV",
		  "MMSEND",
		  "MCRECV",
		  "MCSEND",
		  "DUALRECV",
		  "DUALSEND",
		  "UNQUALIFIED",
		  "SETSTATE",
		  // STATISTICS:
		  "VERDICT",
		  "UNQUALIFIED",
		  // TIMEROP:
		  "READ",
		  "START",
		  "GUARD",
		  "STOP",
		  "TIMEOUT",
		  "UNQUALIFIED",
		  // USER:
		  "UNQUALIFIED",
		  // VERDICTOP:
		  "GETVERDICT",
		  "SETVERDICT",
		  "FINAL",
		  "UNQUALIFIED",
		  // WARNING:
		  "UNQUALIFIED",
		  // MATCHING:
		  "DONE",
		  "TIMEOUT",
		  "PCSUCCESS",
		  "PCUNSUCC",
		  "PMSUCCESS",
		  "PMUNSUCC",
		  "MCSUCCESS",
		  "MCUNSUCC",
		  "MMSUCCESS",
		  "MMUNSUCC",
		  "PROBLEM",
		  "UNQUALIFIED",
		  // DEBUG:
		  "ENCDEC",
		  "TESTPORT",
		  "USER",
		  "FRAMEWORK",
		  "UNQUALIFIED"
	};

	/**
	 * Represents a location in the source in the runtime.
	 * Used for logging precize source location when enabled.
	 * */
	public static class TTCN_Location {
		public static enum entity_type_t {
			LOCATION_UNKNOWN,
			LOCATION_CONTROLPART,
			LOCATION_TESTCASE,
			LOCATION_ALTSTEP,
			LOCATION_FUNCTION,
			LOCATION_EXTERNALFUNCTION,
			LOCATION_TEMPLATE
		};

		String file_name;
		int line_number;
		entity_type_t entity_type;
		String entity_name;

		static final ArrayList<TTCN_Location> locations = new ArrayList<TtcnLogger.TTCN_Location>();
		static int actualSize = 0;

		private TTCN_Location() {
			// intentionally empty
		}

		public static StringBuilder print_location(final boolean print_outers, final boolean print_innermost, final log_event_types_t print_entity_name) {
			final StringBuilder builder = new StringBuilder();

			if (print_outers) {
				for (int i = 0; i < TTCN_Location.actualSize - 1; i++) {
					final TTCN_Location temp = TTCN_Location.locations.get(i);

					temp.append_contents(builder, print_entity_name);
				}
			}
			if (print_innermost && TTCN_Location.actualSize > 0) {
				locations.get(TTCN_Location.actualSize - 1).append_contents(builder, print_entity_name);
			}

			return builder;
		}

		protected void append_contents(final StringBuilder builder, final log_event_types_t print_entity_name) {
			if (builder.length() > 0) {
				builder.append("->");
			}

			builder.append(file_name).append(':').append(line_number);
			switch (entity_type) {
			case LOCATION_CONTROLPART:
				builder.append(MessageFormat.format("(controlpart:{0})", entity_name));
				break;
			case LOCATION_TESTCASE:
				builder.append(MessageFormat.format("(testcase:{0})", entity_name));
				break;
			case LOCATION_ALTSTEP:
				builder.append(MessageFormat.format("(altstep:{0})", entity_name));
				break;
			case LOCATION_FUNCTION:
				builder.append(MessageFormat.format("(function:{0})", entity_name));
				break;
			case LOCATION_EXTERNALFUNCTION:
				builder.append(MessageFormat.format("(externalfunction:{0})", entity_name));
				break;
			case LOCATION_TEMPLATE:
				builder.append(MessageFormat.format("(template:{0})", entity_name));
				break;
			default:
				break;
			} 
		}

		/**
		 * Used to enter a new location block / statement block.
		 *
		 * @param file_name the name of the source file
		 * @param line_number the actual line number
		 * @param entity_type the type of the entity this location represents.
		 * @param entity_name the name of the entity this location represents (if any).
		 * */
		public static TTCN_Location enter(final String file_name, final int line_number, final entity_type_t entity_type, final String entity_name) {
			TTCN_Location temp;

			if (locations.size() > actualSize) {
				temp = locations.get(actualSize);
			} else {
				temp = new TTCN_Location();
				locations.add(temp);
			}

			actualSize++;

			if (file_name == null) {
				temp.file_name = "<unknown file>";
			} else {
				temp.file_name = file_name;
			}
			temp.line_number = line_number;
			temp.entity_type = entity_type;
			if (entity_name == null) {
				temp.entity_name = "<unknown>";
			} else {
				temp.entity_name = entity_name;
			}

			return temp;
		}

		/**
		 * Update the line number.
		 *
		 * @param new_lineno the new line number to use
		 * */
		public void update_lineno(final int new_lineno) {
			line_number = new_lineno;
		}

		/**
		 * Used to leave a new location block / statement block.
		 * */
		public void leave() {
			actualSize--;
		}
	}

	static StringBuilder logMatchBuffer = new StringBuilder();
	static boolean logMatchPrinted = false;
	static matching_verbosity_t matching_verbosity = matching_verbosity_t.VERBOSITY_COMPACT;
	static emergency_logging_behaviour_t emergency_logging_behaviour = emergency_logging_behaviour_t.BUFFER_MASKED;
	static boolean emergency_logging_for_fail_verdict = false;

	// length of the emergency logging buffer
	static int emergency_logging = 0;;

	/**
	 * Always return the single instance of the LoggerPluginManager.
	 * */
	private static LoggerPluginManager get_logger_plugin_manager() {
		if (plugins_ == null) {
			plugins_ = new LoggerPluginManager();
		}

		return plugins_;
	}

	public static void mput_severity(final StringBuilder str, final Severity severity) {
		switch (severity) {
		case ACTION_UNQUALIFIED:
			str.append("ACTION");
			break;
		case DEFAULTOP_ACTIVATE:
		case DEFAULTOP_DEACTIVATE:
		case DEFAULTOP_EXIT:
		case DEFAULTOP_UNQUALIFIED:
			str.append("DEFAULTOP");
			break;
		case ERROR_UNQUALIFIED:
			str.append("ERROR");
			break;
		case EXECUTOR_RUNTIME:
		case EXECUTOR_CONFIGDATA:
		case EXECUTOR_EXTCOMMAND:
		case EXECUTOR_COMPONENT:
		case EXECUTOR_LOGOPTIONS:
		case EXECUTOR_UNQUALIFIED:
			str.append("EXECUTOR");
			break;
		case FUNCTION_RND:
		case FUNCTION_UNQUALIFIED:
			str.append("FUNCTION");
			break;
		case PARALLEL_PTC:
		case PARALLEL_PORTCONN:
		case PARALLEL_PORTMAP:
		case PARALLEL_UNQUALIFIED:
			str.append("PARALLEL");
			break;
		case TESTCASE_START:
		case TESTCASE_FINISH:
		case TESTCASE_UNQUALIFIED:
			str.append("TESTCASE");
			break;
		case PORTEVENT_PQUEUE:
		case PORTEVENT_MQUEUE:
		case PORTEVENT_STATE:
		case PORTEVENT_PMIN:
		case PORTEVENT_PMOUT:
		case PORTEVENT_PCIN:
		case PORTEVENT_PCOUT:
		case PORTEVENT_MMRECV:
		case PORTEVENT_MMSEND:
		case PORTEVENT_MCRECV:
		case PORTEVENT_MCSEND:
		case PORTEVENT_DUALRECV:
		case PORTEVENT_DUALSEND:
		case PORTEVENT_UNQUALIFIED:
		case PORTEVENT_SETSTATE:
			str.append("PORTEVENT");
			break;
		case STATISTICS_VERDICT:
		case STATISTICS_UNQUALIFIED:
			str.append("STATISTICS");
			break;
		case TIMEROP_READ:
		case TIMEROP_START:
		case TIMEROP_GUARD:
		case TIMEROP_STOP:
		case TIMEROP_TIMEOUT:
		case TIMEROP_UNQUALIFIED:
			str.append("TIMEROP");
			break;
		case USER_UNQUALIFIED:
			str.append("USER");
			break;
		case VERDICTOP_GETVERDICT:
		case VERDICTOP_SETVERDICT:
		case VERDICTOP_FINAL:
		case VERDICTOP_UNQUALIFIED:
			str.append("VERDICTOP");
			break;
		case WARNING_UNQUALIFIED:
			str.append("WARNING");
			break;
		case MATCHING_DONE:
		case MATCHING_TIMEOUT:
		case MATCHING_PCSUCCESS:
		case MATCHING_PCUNSUCC:
		case MATCHING_PMSUCCESS:
		case MATCHING_PMUNSUCC:
		case MATCHING_MCSUCCESS:
		case MATCHING_MCUNSUCC:
		case MATCHING_MMSUCCESS:
		case MATCHING_MMUNSUCC:
		case MATCHING_PROBLEM:
		case MATCHING_UNQUALIFIED:
			str.append("MATCHING");
			break;
		case DEBUG_ENCDEC:
		case DEBUG_TESTPORT:
		case DEBUG_USER:
		case DEBUG_FRAMEWORK:
		case DEBUG_UNQUALIFIED:
			str.append("DEBUG");
			break;
		case NUMBER_OF_LOGSEVERITIES:
		case LOG_ALL_IMPORTANT:
		case NOTHING_TO_LOG:
			str.append("UNKNOWN");
			break;
		}
	}

	public static void mputstr_timestamp(final StringBuilder str, final timestamp_format_t p_timestamp_format, final int seconds, final int microseconds) {
		switch (p_timestamp_format) {
		case TIMESTAMP_SECONDS: {
			final long newSeconds = seconds;
			final long startSeconds = start_time / 1000;
			final long startMicroSeconds = start_time % 1000;
			if (microseconds < startMicroSeconds) {
				str.append(String.format("%d", newSeconds - startSeconds - 1)).append('.').append(String.format("%03d", microseconds + ( 1000 - startMicroSeconds)));
			} else {
				str.append(String.format("%d", newSeconds - startSeconds)).append('.').append(String.format("%03d", microseconds - startMicroSeconds));
			}
			break;
		}
		case TIMESTAMP_TIME: {
			final long timestamp = (long)seconds * 1000 + microseconds;
			calendar.setTimeInMillis(timestamp);
			str.append(String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY))).append(':').append(String.format("%02d", calendar.get(Calendar.MINUTE))).append(':').append(String.format("%02d", calendar.get(Calendar.SECOND))).append('.').append(String.format("%03d", microseconds)).append("000");
			break;
		}
		case TIMESTAMP_DATETIME: {
			final long timestamp = (long)seconds * 1000 + microseconds;
			calendar.setTimeInMillis(timestamp);
			str.append(String.format("%4d", calendar.get(Calendar.YEAR))).append('/').append(month_names[calendar.get(Calendar.MONTH)]).append('/').append(String.format("%02d", calendar.get(Calendar.DAY_OF_MONTH)));
			str.append(' ');
			str.append(String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY))).append(':').append(String.format("%02d", calendar.get(Calendar.MINUTE))).append(':').append(String.format("%02d", calendar.get(Calendar.SECOND))).append('.').append(String.format("%03d", microseconds)).append("000");
			break;
		}
		}
	}

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

		get_logger_plugin_manager().log_unhandled_event(msg_severity, string == null ? "<NULL pointer>": string);
		logMatchPrinted = false;
	}

	public static void log_va_list(final Severity msg_severity, final String formatString, final Object... args) {
		if (!log_this_event(msg_severity) && get_emergency_logging() <= 0) {
			return;
		}

		get_logger_plugin_manager().log_unhandled_event(msg_severity, String.format(Locale.US, formatString, args));
		logMatchPrinted = false;
	}

	public static void begin_event(final Severity msg_severity) {
		get_logger_plugin_manager().begin_event(msg_severity);
	}

	public static void begin_event_log2str() {
		get_logger_plugin_manager().begin_event_log2str();
	}

	public static void end_event() {
		get_logger_plugin_manager().end_event();
		logMatchPrinted = false;
	}

	public static TitanCharString end_event_log2str() {
		final TitanCharString returnValue = get_logger_plugin_manager().end_event_log2str();

		logMatchPrinted = false;

		return returnValue;
	}

	public static void finish_event() {
		get_logger_plugin_manager().finish_event();
	}

	public static void log_event( final String formatString, final Object... args ) {
		log_event_va_list(formatString, args);
	}

	public static void log_event_str( final String string ) {
		get_logger_plugin_manager().log_event_str(string);
		logMatchPrinted = false;
	}

	public static void log_event_va_list(final String formatString, final Object... args) {
		get_logger_plugin_manager().log_event_va_list(formatString, args);
		logMatchPrinted = false;
	}

	public static void log_char(final char c) {
		get_logger_plugin_manager().log_char(c);
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

	public static void ring_buffer_dump(final boolean do_close_file) {
		get_logger_plugin_manager().ring_buffer_dump(do_close_file);
	}

	public static matching_verbosity_t get_matching_verbosity() {
		return matching_verbosity;
	}

	// Called from the generated code and many more places...  Stay here.  The
	// existence of the file descriptors etc. is the responsibility of the
	// plug-ins.
	public static boolean log_this_event(final Severity event_severity) {
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

	private static String get_logger_settings_str() {
		final StringBuilder new_log_message = new StringBuilder();

		final String timestamp_format_names[] = {"Time", "DateTime", "Seconds"};
		final String logeventtype_names[] = {"No", "Yes", "Subcategories"};

		//FIXME insert correct version numbers
		new_log_message.append(MessageFormat.format("TTCN Logger v{0}.{1} options: ", "in development", "in development"));
		new_log_message.append(MessageFormat.format("TimeStampFormat:={0}; LogEntityName:={1};", timestamp_format_names[timestamp_format.ordinal()], logeventtype_names[log_entity_name.ordinal()]));
		//FIXME implement rest once relevant

		return new_log_message.toString();
	}

	public static void write_logger_settings() {
		final String new_log_message = get_logger_settings_str();
		//FIXME implement
		get_logger_plugin_manager().log_log_options(new_log_message);
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

	public static void set_timestamp_format(final timestamp_format_t new_timestamp_format){
		timestamp_format = new_timestamp_format;
	}

	public static timestamp_format_t get_timestamp_format() {
		return timestamp_format;
	}

	public static void set_source_info_format(final source_info_format_t new_source_info_format) {
		source_info_format = new_source_info_format;
	}

	public static source_info_format_t get_source_info_format() {
		return source_info_format;
	}

	public static void set_log_event_types(final log_event_types_t new_log_event_types) {
		log_event_types = new_log_event_types;
	}

	public static log_event_types_t get_log_event_types() {
		return log_event_types;
	}

	public static void set_log_entity_name(final log_event_types_t new_log_entity_name) {
		log_entity_name = new_log_entity_name;
	}

	public static log_event_types_t get_log_entity_name() {
		return log_entity_name;
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
		get_logger_plugin_manager().log_unhandled_event(severity, message);
	}

	public static void log_timer_read(final String timer_name, final double timeout_val) {
		get_logger_plugin_manager().log_timer_read(timer_name, timeout_val);
	}

	public static void log_timer_start(final String timer_name, final double start_val) {
		get_logger_plugin_manager().log_timer_start(timer_name, start_val);
	}

	public static void log_timer_guard(final double start_val) {
		get_logger_plugin_manager().log_timer_guard(start_val);
	}

	public static void log_timer_stop(final String timer_name, final double stop_val) {
		get_logger_plugin_manager().log_timer_stop(timer_name, stop_val);
	}

	public static void log_timer_timeout(final String timer_name, final double timeout_val) {
		get_logger_plugin_manager().log_timer_timeout(timer_name, timeout_val);
	}

	public static void log_timer_any_timeout() {
		get_logger_plugin_manager().log_timer_any_timeout();
	}

	public static void log_timer_unqualified(final String message) {
		get_logger_plugin_manager().log_timer_unqualified(message);
	}

	public static void log_matching_timeout(final String timer_name) {
		get_logger_plugin_manager().log_matching_timeout(timer_name);
	}

	public static void log_port_queue(final TitanLoggerApi.Port__Queue_operation.enum_type operation, final String port_name, final int componentReference, final int id, final TitanCharString address, final TitanCharString parameter) {
		get_logger_plugin_manager().log_port_queue(operation, port_name, componentReference, id, address, parameter);
	}

	public static void set_file_name(final String new_filename_skeleton, boolean from_config) {
		//FIXME: implement
	}

	public static void set_start_time() {
		start_time = System.currentTimeMillis();
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

	public static void set_append_file(boolean new_append_file) {
		get_logger_plugin_manager().set_append_file(new_append_file);
	}

	public static boolean set_file_size(final component_id_t comp, int p_size) {
		return get_logger_plugin_manager().set_file_size(comp, p_size);
	}

	public static boolean set_file_number(final component_id_t comp, int p_number) {
		return get_logger_plugin_manager().set_file_number(comp, p_number);
	}

	public static boolean set_disk_full_action(final component_id_t comp, disk_full_action_t p_disk_full_action) {
		return get_logger_plugin_manager().set_disk_full_action(comp, p_disk_full_action);
	}
	
	public static void open_file() {
		get_logger_plugin_manager().open_file();
	}
	
	public static void close_file() {
		get_logger_plugin_manager().close_file();
	}

	public static void set_emergency_logging_behaviour(final emergency_logging_behaviour_t behaviour){
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

	public static boolean get_emergency_logging_for_fail_verdict() {
		return emergency_logging_for_fail_verdict;
	}

	public static void set_emergency_logging_for_fail_verdict(final boolean b) {
		emergency_logging_for_fail_verdict = b;
	}

	public static void log_port_state(final TitanLoggerApi.Port__State_operation.enum_type operation, final String portname) {
		get_logger_plugin_manager().log_port_state(operation, portname);
	}

	public static void log_procport_send(final String portname, final TitanLoggerApi.Port__oper.enum_type operation, final int componentReference, final TitanCharString system, final TitanCharString parameter) {
		get_logger_plugin_manager().log_procport_send(portname, operation, componentReference, system, parameter);
	}

	public static void log_procport_recv(final String portname, final TitanLoggerApi.Port__oper.enum_type operation, final int componentReference, final boolean check, final TitanCharString parameter, final int id) {
		get_logger_plugin_manager().log_procport_recv(portname, operation, componentReference, check, parameter, id);
	}

	public static void log_msgport_send(final String portname, final int componentReference, final TitanCharString parameter) {
		get_logger_plugin_manager().log_msgport_send(portname, componentReference, parameter);
	}

	public static void log_msgport_recv(final String portname, final TitanLoggerApi.Msg__port__recv_operation.enum_type operation, final int componentReference, final TitanCharString system, final TitanCharString parameter, final int id) {
		get_logger_plugin_manager().log_msgport_recv(portname, operation, componentReference, system, parameter, id);
	}

	public static void log_dualport_map(final boolean incoming, final String target_type, final TitanCharString value, final int id) {
		get_logger_plugin_manager().log_dualport_map(incoming, target_type, value, id);
	}

	public static void log_dualport_discard(final boolean incoming, final String target_type, final TitanCharString port_name, final boolean unhandled) {
		get_logger_plugin_manager().log_dualport_discard(incoming, target_type, port_name, unhandled);
	}

//	FIXME enable once translation ports are supported
//	public static void log_setstate(final TitanCharString port_name, final translation_port_state state, final TitanCharString info) {
//		get_logger_plugin_manager().log_setstate(port_name, state, info);
//	}

	public static void log_setverdict(final VerdictTypeEnum newVerdict, final VerdictTypeEnum oldVerdict, final VerdictTypeEnum localVerdict,
			final String oldReason, final String newReason) {
		get_logger_plugin_manager().log_setverdict(newVerdict, oldVerdict, localVerdict, oldReason, newReason);
	}

	public static void log_getverdict(final VerdictTypeEnum verdict) {
		get_logger_plugin_manager().log_getverdict(verdict);
	}

	public static void log_testcase_started(final String module_name, final String definition_name ) {
		get_logger_plugin_manager().log_testcase_started(module_name, definition_name);
	}

	//TODO not yet called from generated code
	public static void log_testcase_finished(final String module_name, final String definition_name, final VerdictTypeEnum verdict, final String reason) {
		get_logger_plugin_manager().log_testcase_finished(module_name, definition_name, verdict, reason);
	}

	public static void log_final_verdict(final boolean is_ptc, final TitanVerdictType.VerdictTypeEnum ptc_verditc, final TitanVerdictType.VerdictTypeEnum local_verdict, final TitanVerdictType.VerdictTypeEnum new_verdict, final String verdict_reason, final int notification, final int ptc_compref, final String ptc_name) {
		get_logger_plugin_manager().log_final_verdict(is_ptc, ptc_verditc, local_verdict, new_verdict, verdict_reason, notification, ptc_compref, ptc_name);
	}

	public static void log_controlpart_start_stop(final String moduleName, final boolean finished) {
		get_logger_plugin_manager().log_controlpart_start_stop(moduleName, finished);
	}

	public static void log_controlpart_errors(final int error_count) {
		get_logger_plugin_manager().log_controlpart_errors(error_count);
	}

	public static void log_verdict_statistics(final int none_count, final double none_percent,
			final int pass_count, final double pass_percent,
			final int inconc_count, final double inconc_percent,
			final int fail_count, final double fail_percent,
			final int error_count, final double error_percent) {
		get_logger_plugin_manager().log_verdict_statistics(none_count, none_percent, pass_count, pass_percent, inconc_count, inconc_percent,
				fail_count, fail_percent, error_count, error_percent);
	}

	public static void log_defaultop_activate(final String name, final int id) {
		get_logger_plugin_manager().log_defaultop_activate(name, id);
	}

	public static void log_defaultop_deactivate(final String name, final int id) {
		get_logger_plugin_manager().log_defaultop_deactivate(name, id);
	}

	public static void log_defaultop_exit(final String name, final int id, final int x) {
		get_logger_plugin_manager().log_defaultop_exit(name, id, x);
	}

	public static void log_executor_runtime(final TitanLoggerApi.ExecutorRuntime_reason.enum_type reason) {
		get_logger_plugin_manager().log_executor_runtime(reason);
	}

	public static void log_hc_start(final String host) {
		get_logger_plugin_manager().log_hc_start(host);
	}

	public static void log_testcase_exec(final String testcase, final String module) {
		get_logger_plugin_manager().log_testcase_exec(testcase, module);
	}

	public static void log_module_init(final String module, final boolean finish) {
		get_logger_plugin_manager().log_module_init(module, finish);
	}

	public static void log_mtc_created(final long pid) {
		get_logger_plugin_manager().log_mtc_created(pid);
	}

	public static void log_configdata(final ExecutorConfigdata_reason.enum_type reason, final String str) {
		get_logger_plugin_manager().log_configdata(reason, str);
	}

	public static void log_executor_component(final ExecutorComponent_reason.enum_type reason) {
		get_logger_plugin_manager().log_executor_component(reason);
	}

	public static void log_executor_misc(final ExecutorUnqualified_reason.enum_type reason, final String name, final String address, final int port) {
		get_logger_plugin_manager().log_executor_misc(reason, name, address, port);
	}

	public static void log_extcommand(final TtcnLogger.extcommand_t action, final String cmd) {
		get_logger_plugin_manager().log_extcommand(action, cmd);
	}

	public static void log_matching_done(final String type, final int ptc, final String return_type, final TitanLoggerApi.MatchingDoneType_reason.enum_type reason) {
		get_logger_plugin_manager().log_matching_done(reason, type, ptc, return_type);
	}

	public static void log_matching_problem(final TitanLoggerApi.MatchingProblemType_reason.enum_type reason, final TitanLoggerApi.MatchingProblemType_operation.enum_type operation, final boolean check, final boolean anyport, final String port_name) {
		get_logger_plugin_manager().log_matching_problem(reason, operation, check, anyport, port_name);
	}

	public static void log_random(final TitanLoggerApi.RandomAction.enum_type rndAction, final double value, final long seed) {
		get_logger_plugin_manager().log_random(rndAction, value, seed);
	}

	public static void log_matching_failure(final TitanLoggerApi.PortType.enum_type port_type, final String port_name, final int compref, final TitanLoggerApi.MatchingFailureType_reason.enum_type reason, final TitanCharString info) {
		get_logger_plugin_manager().log_matching_failure(port_type, port_name, compref, reason, info);
	}

	public static void log_matching_success(final TitanLoggerApi.PortType.enum_type port_type, final String port_name, final int compref, final TitanCharString info) {
		get_logger_plugin_manager().log_matching_success(port_type, port_name, compref, info);
	}

	public static void log_port_misc(final TitanLoggerApi.Port__Misc_reason.enum_type reason, final String port_name, final int remote_component, final String remote_port, final String ip_address, final int tcp_port, final int new_size) {
		get_logger_plugin_manager().log_port_misc(reason, port_name, remote_component, remote_port, ip_address, tcp_port, new_size);
	}

	public static void log_portconnmap(final ParPort_operation.enum_type operation, final int src_compref, final String src_port, final int dst_compref, final String dst_port) {
		get_logger_plugin_manager().log_portconnmap(operation, src_compref, src_port, dst_compref, dst_port);
	}

	public static void log_par_ptc(final ParallelPTC_reason.enum_type reason, final String module, final String name, final int compref, final String compname, final String tc_loc, final int alive_pid, final int status) {
		get_logger_plugin_manager().log_parptc(reason, module, name, compref, compname, tc_loc, alive_pid, status);
	}
}
