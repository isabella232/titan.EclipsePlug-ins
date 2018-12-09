/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.MessageFormat;

import org.eclipse.titan.runtime.core.TitanLoggerApi.DefaultEvent_choice;
import org.eclipse.titan.runtime.core.TitanLoggerApi.DefaultOp;
import org.eclipse.titan.runtime.core.TitanLoggerApi.Dualface__discard;
import org.eclipse.titan.runtime.core.TitanLoggerApi.Dualface__mapped;
import org.eclipse.titan.runtime.core.TitanLoggerApi.ExecutorComponent;
import org.eclipse.titan.runtime.core.TitanLoggerApi.ExecutorConfigdata;
import org.eclipse.titan.runtime.core.TitanLoggerApi.ExecutorRuntime;
import org.eclipse.titan.runtime.core.TitanLoggerApi.ExecutorUnqualified;
import org.eclipse.titan.runtime.core.TitanLoggerApi.FinalVerdictInfo;
import org.eclipse.titan.runtime.core.TitanLoggerApi.FunctionEvent_choice_random;
import org.eclipse.titan.runtime.core.TitanLoggerApi.LocationInfo;
import org.eclipse.titan.runtime.core.TitanLoggerApi.LogEventType_choice;
import org.eclipse.titan.runtime.core.TitanLoggerApi.MatchingDoneType;
import org.eclipse.titan.runtime.core.TitanLoggerApi.MatchingEvent_choice;
import org.eclipse.titan.runtime.core.TitanLoggerApi.MatchingFailureType;
import org.eclipse.titan.runtime.core.TitanLoggerApi.MatchingProblemType;
import org.eclipse.titan.runtime.core.TitanLoggerApi.MatchingSuccessType;
import org.eclipse.titan.runtime.core.TitanLoggerApi.MatchingTimeout;
import org.eclipse.titan.runtime.core.TitanLoggerApi.Msg__port__recv;
import org.eclipse.titan.runtime.core.TitanLoggerApi.Msg__port__send;
import org.eclipse.titan.runtime.core.TitanLoggerApi.PTC__exit;
import org.eclipse.titan.runtime.core.TitanLoggerApi.ParPort;
import org.eclipse.titan.runtime.core.TitanLoggerApi.ParallelEvent_choice;
import org.eclipse.titan.runtime.core.TitanLoggerApi.ParallelPTC;
import org.eclipse.titan.runtime.core.TitanLoggerApi.PortEvent_choice;
import org.eclipse.titan.runtime.core.TitanLoggerApi.Port__Misc;
import org.eclipse.titan.runtime.core.TitanLoggerApi.Port__Queue;
import org.eclipse.titan.runtime.core.TitanLoggerApi.Port__State;
import org.eclipse.titan.runtime.core.TitanLoggerApi.Proc__port__in;
import org.eclipse.titan.runtime.core.TitanLoggerApi.Proc__port__out;
import org.eclipse.titan.runtime.core.TitanLoggerApi.SetVerdictType;
import org.eclipse.titan.runtime.core.TitanLoggerApi.Setstate;
import org.eclipse.titan.runtime.core.TitanLoggerApi.StatisticsType_choice;
import org.eclipse.titan.runtime.core.TitanLoggerApi.StatisticsType_choice_verdictStatistics;
import org.eclipse.titan.runtime.core.TitanLoggerApi.Strings_str__list;
import org.eclipse.titan.runtime.core.TitanLoggerApi.TestcaseEvent_choice;
import org.eclipse.titan.runtime.core.TitanLoggerApi.TimerEvent_choice;
import org.eclipse.titan.runtime.core.TitanLoggerApi.TimerGuardType;
import org.eclipse.titan.runtime.core.TitanLoggerApi.TimerType;
import org.eclipse.titan.runtime.core.TitanLoggerApi.TitanLogEvent;
import org.eclipse.titan.runtime.core.TitanLoggerApi.VerdictOp_choice;
import org.eclipse.titan.runtime.core.TitanVerdictType.VerdictTypeEnum;
import org.eclipse.titan.runtime.core.TTCN_Logger.Severity;
import org.eclipse.titan.runtime.core.TTCN_Logger.disk_full_action_t;
import org.eclipse.titan.runtime.core.TTCN_Logger.disk_full_action_type_t;
import org.eclipse.titan.runtime.core.TTCN_Logger.log_event_types_t;
import org.eclipse.titan.runtime.core.TTCN_Logger.source_info_format_t;

/**
 * A logger plugin implementing the legacy logger behaviour.
 *
 * FIXME lots to implement here, this is under construction right now
 *
 * @author Kristof Szabados
 */
public class LegacyLogger implements ILoggerPlugin {
	/**
	 * This function represents the entry point for the legacy style logger plugin.
	 * (still embedded in this generic class while transitioning the design)
	 * */

	private String filename_skeleton_;
	private TTCN_Logger.disk_full_action_t disk_full_action_ = new disk_full_action_t(disk_full_action_type_t.DISKFULL_ERROR, 0);
	private int disk_full_time_seconds = 0;
	private int disk_full_time_microseconds = 0;
	private boolean skeleton_given_ = false;
	private boolean append_file_ = false;
	private boolean is_disk_full_ = false;
	private String current_filename_;
	private int logfile_size_ = 0;
	private int logfile_number_ = 1;
	private int logfile_index_ = 1;
	private int logfile_bytes_ = 0;
	private boolean format_c_present_ = false;
	private boolean format_t_present_ = false;
	private File log_fp_;
	private boolean is_configured;
	private File er_;
	private static final ThreadLocal<BufferedWriter> log_file_writer = new ThreadLocal<BufferedWriter>(){
		@Override
		protected BufferedWriter initialValue(){
			return null;
		}
	};

	private static final String name_ = "LegacyLogger";
	private static final String help_ = "LegacyLogger";

	private static final ThreadLocal<Boolean> already_warned = new ThreadLocal<Boolean>() {
		@Override
		protected Boolean initialValue() {
			return Boolean.FALSE;
		}
	};

	private static LegacyLogger myself = null;

	public LegacyLogger() {
		log_fp_ = null;
		er_ = null;
		logfile_bytes_ = 0;
		logfile_size_ = 0;
		logfile_number_ = 1;
		logfile_index_ = 1;
		filename_skeleton_ = null;
		skeleton_given_ = false;
		append_file_ = false;
		is_disk_full_ = false;
		format_c_present_ = false;
		format_t_present_ = false;
		current_filename_ = null;
		if (myself == null) {
			myself = this;
		} else {
			//TODO: handle it
			System.err.print("Only one LegacyLogger allowed!\n");
		}
		disk_full_action_.type = disk_full_action_type_t.DISKFULL_ERROR;
		disk_full_action_.retry_interval = 0;
	}

	public void reset() {
		disk_full_action_.type = disk_full_action_type_t.DISKFULL_ERROR;
		disk_full_action_.retry_interval = 0;
		logfile_size_ = 0;
		logfile_number_ = 1;
		logfile_bytes_ = 0;
		logfile_index_ = 1;
		is_disk_full_ = false;
		skeleton_given_ = false;
		append_file_ = false;
		is_configured = false;
	}
	
	public String plugin_name() {
		return name_;
	}
	
	public String plugin_help() {
		return help_;
	}
	
	public void set_parameter(final String parameter_name, final String parameter_value) {
		//Just a place holder.
	}

	public void log(final TitanLoggerApi.TitanLogEvent event, final boolean log_buffered, final boolean separate_file, final boolean use_emergency_mask) {
		if (separate_file) {
			log_file_emerg(event);
		}

		final int severityIndex = event.get_field_severity().get_int();
		final Severity severity = Severity.values()[severityIndex];
		if (use_emergency_mask) {
			if (TTCN_Logger.should_log_to_emergency(severity) || TTCN_Logger.should_log_to_file(severity)) {
				log_file(event, log_buffered);
			}
			if (TTCN_Logger.should_log_to_console(severity)) {
				log_console(event, severity);
			}
		} else {
			if (TTCN_Logger.should_log_to_file(severity)) {
				log_file(event, log_buffered);
			}
			if (TTCN_Logger.should_log_to_console(severity)) {
				log_console(event, severity);
			}
		}
	}

	public void set_file_name(final String new_filename_skeleton, final boolean from_config) {
		filename_skeleton_ = new_filename_skeleton;
		if (from_config) {
			skeleton_given_ = true;
		}
	}

	public void set_append_file(final boolean new_append_file) {
		append_file_ = new_append_file;
	}

	public boolean set_file_size(final int p_size) {
		logfile_size_ = p_size;
		return true;
	}

	public boolean set_file_number(final int p_number) {
		logfile_number_ = p_number;
		return true;
	}

	public boolean set_disk_full_action(final TTCN_Logger.disk_full_action_t  p_disk_full_action) {
		disk_full_action_ = p_disk_full_action;
		return true;
	}
	
	public void open_file(final boolean is_first) {
		//TODO: different than C++ and initial implement 
		if (is_first) {
			chk_log_file();
			if (!skeleton_given_) {
				set_file_name(TTCN_Runtime.is_single() ? (logfile_number_ == 1 ? "%e.%s" : "%e-part%i.%s") : (logfile_number_ == 1 ? "%e.%h-%r.%s" : "%e.%h-%r-part%i.%s"), false);
			}
		}
		current_filename_ = get_file_name(logfile_index_);
		if (current_filename_ != null) {
			create_parent_directories(current_filename_);
			log_fp_ = new File(current_filename_);
			if (!log_fp_.exists()) {
				try {
					log_fp_.createNewFile();
				} catch (IOException e) {
					throw new TtcnError(e);
				}
			} else {
				if (log_file_writer.get() != null) {
					try {
						log_file_writer.get().close();
					} catch (IOException e) {
						System.err.println("Cannot close file writer!");
					}
				}
			}
			try {
				log_file_writer.set(new BufferedWriter(new FileWriter(log_fp_, append_file_),32768));
			} catch (IOException e) {
				System.err.println("Cannot open file!");
			}
		}

		logfile_bytes_ = 0;
		is_configured = true;	
	}
	
	public void close_file() {
		if (log_file_writer.get() == null || log_fp_ == null) {
			return;
		}

		try {
			log_file_writer.get().close();
		} catch ( IOException e ) {
			System.err.println("Cannot close file!");
		}
		log_fp_ = null;
	}
	
	public boolean is_configured() {
		return is_configured;
	}
	
	private void fatal_error(final String err_msg, final Object... args) {
		System.err.println("Fatal error during logging: ");
		if (args == null || err_msg == null) {
			System.err.println(" (Unkown error!)");
		}
		if (args == null && err_msg != null) {
			System.err.println(err_msg);
		}
		if (args != null && err_msg != null) {
			final MessageFormat err_str = new MessageFormat(err_msg);
			System.err.println(err_str.format(args));
		}
		System.err.println("Exiting.\n");
		System.exit(1);
	}

	private enum whoami{SINGLE, HC, MTC, PTC};
	
	/** @brief Construct the log file name, performs substitutions.
    @return NULL if filename_skeleton is NULL or if the result would have been
    the empty string.
    @return an String with the actual filename.**/
	private String get_file_name(final int idx) {
		if (filename_skeleton_ == null) {
			return null;
		}
		whoami whoami_variable = whoami.SINGLE;
		if (TTCN_Runtime.is_single()) {
			whoami_variable = whoami.SINGLE;
		}
		if (TTCN_Runtime.is_hc()) {
			whoami_variable = whoami.HC;
		}
		if (TTCN_Runtime.is_mtc()) {
			whoami_variable = whoami.MTC;
		}
		if (TTCN_Runtime.is_ptc()) {
			whoami_variable = whoami.PTC;
		}
		boolean h_present = false, p_present = false, r_present = false, i_present = false;
		format_c_present_ = false;
		format_t_present_ = false;
		final StringBuilder ret_val = new StringBuilder();
		for (int i = 0; i < filename_skeleton_.length(); i++) {
			if (filename_skeleton_.charAt(i) != '%') {
				if (filename_skeleton_.charAt(i) == '/' || filename_skeleton_.charAt(i) == '\\') {
					ret_val.append(File.separatorChar);
				} else {
					ret_val.append(filename_skeleton_.charAt(i));
				}
				continue;
			}
			switch (filename_skeleton_.charAt(++i)) {
			case 'c': // %c -> name of the current testcase (only on PTCs)
				ret_val.append(TTCN_Runtime.get_testcase_name());
				format_c_present_ = true;
				break;
			case 'e': // %e -> name of executable
				ret_val.append(TTCN_Logger.get_executable_name());
				break;
			case 'h': //%h -> hostname
				ret_val.append(TTCN_Runtime.get_host_name());
				h_present = true;
				break;
			case 'l': //%l -> login name
				ret_val.append(System.getProperty("user.name").toString());
				break;
			case 'n': //%n -> component name (optional)
				switch (whoami_variable) {
				case SINGLE:
				case MTC:
					ret_val.append("MTC");
					break;
				case HC:
					ret_val.append("HC");
					break;
				case PTC:
					ret_val.append(TTCN_Runtime.get_component_name());
					break;
				default:
					break;
				}
				break;
			case 'p': //%p -> process id (thread id)
				//TODO: different from C++ getpid()
				ret_val.append(Thread.currentThread().getId());
				p_present = true;
				break;
			case 'r': //%r -> component reference
				switch (whoami_variable) {
				case SINGLE:
					ret_val.append("single");
					break;
				case HC:
					ret_val.append("hc");
					break;
				case MTC:
					ret_val.append("mtc");
					break;
				case PTC:
				default:
					ret_val.append(TitanComponent.self.get().get_component());
					break;
				}
				r_present = true;
				break;
			case 's': // %s -> default suffix (currently: always "log")
				ret_val.append("log");
				break;
			case 't': // %t -> component type (only on PTCs)
				ret_val.append(TTCN_Runtime.get_component_type());
				format_t_present_ = true;
				break;
			case 'i': // %i -> log file index
				if (logfile_number_ != 1) {
					ret_val.append(idx);
				}
				i_present = true;
				break;
			case '\0':
				i--;
			case '%':
				ret_val.append('%');
				break;
			default:
				ret_val.append('%');
				ret_val.append(filename_skeleton_.charAt(i));
				break;
			}
		}

		if (ret_val.length() == 0) {
			return null;
		} else if (whoami_variable == whoami.HC && !already_warned.get().booleanValue()) {
			already_warned.set(true);
			if (!h_present || (!p_present && !r_present)) {
				TtcnError.TtcnWarning(MessageFormat.format("Skeleton {0} does not guarantee unique log file name for every test system process. It may cause unpredictable results if several test components try to write into the same log file.", filename_skeleton_));
			}
		}
		if (logfile_number_ != 1 && !i_present) {
			TtcnError.TtcnWarning(MessageFormat.format("LogFileNumber = {0}, but `%%i' is missing from the log file name skeleton. `%%i' was appended to the skeleton.", logfile_number_));
			filename_skeleton_ = filename_skeleton_ + "%i";
			ret_val.append(idx);
		}
		return ret_val.toString();
	}
	
	private void chk_log_file() {
		if (logfile_size_ == 0 && logfile_number_ != 1) {
			TtcnError.TtcnWarning(MessageFormat.format("Invalid combination of LogFileSize (= {0}) and LogFileNumber (= {1}). LogFileNumber was reset to 1.", logfile_size_ , logfile_number_));
			logfile_number_ = 1;
		}
		if (logfile_size_ > 0 && logfile_number_ == 1) {
			TtcnError.TtcnWarning(MessageFormat.format("Invalid combination of LogFileSize (= {0}) and LogFileNumber (= {1}). LogFileSize was reset to 0.", logfile_size_, logfile_number_));
			logfile_size_ = 0;
		}
		if (logfile_number_ == 1 && disk_full_action_.type == disk_full_action_type_t.DISKFULL_DELETE) {
			TtcnError.TtcnWarning("Invalid combination of LogFileNumber (= 1) and DiskFullAction (= Delete). DiskFullAction was reset to Error.");
			disk_full_action_.type = disk_full_action_type_t.DISKFULL_ERROR;
		}
		if (logfile_number_ != 1 && append_file_) {
			TtcnError.TtcnWarning(MessageFormat.format("Invalid combination of LogFileNumber (= {0}) and AppendFile (= Yes). AppendFile was reset to No.", logfile_number_));
			append_file_ = false;
		}
	}

	private void create_parent_directories(final String path_name) {
		String path_backup = null;
		for (int i = 0; i < path_name.length(); i++) {
			if (path_name.charAt(i) == File.separatorChar) {
				path_backup = path_name.substring(0,i+1);
				final File path_backup_file = new File(path_backup);
				if (!path_backup_file.exists()) {
					if (!path_backup_file.mkdir()) {
						fatal_error("Creation of directory {0} failed when trying to open log file {1}.", path_backup, path_name);
					}
				}
			}
		}
	}

	/**
	 * The log_console function from the legacy logger.
	 *
	 * Not the final implementation though.
	 * */
	private static boolean log_console(final TitanLoggerApi.TitanLogEvent event, final Severity msg_severity) {
		final String event_str = event_to_string(event, true);
		if (event_str == null) {
			TtcnError.TtcnWarning("No text for event");

			return false;
		}

		if (!TTCN_Communication.send_log(event.get_field_timestamp__().get_field_seconds().get_int(), event.get_field_timestamp__().get_field_microSeconds().get_int(), event.get_field_severity().get_int(), event_str)) {
			// The event text shall be printed to stderr when there is no control
			// connection towards MC (e.g. in single mode or in case of network
			// error).
			if (event_str.length() > 0) {
				// Write the location info to the console for user logs only.
				if (msg_severity == Severity.USER_UNQUALIFIED && event_str.startsWith(":") && event.get_field_sourceInfo__list().lengthof().get_int() > 0) {
					final int stackdepth = event.get_field_sourceInfo__list().lengthof().get_int();
					final LocationInfo loc = event.get_field_sourceInfo__list().get_at(stackdepth - 1);
					System.err.print(MessageFormat.format("{0}:{1}", loc.get_field_filename().get_value(), loc.get_field_line().get_int()));
				}

				System.err.print(event_str);
			}
			System.err.println();
		}

		return true;
	}
	
	private boolean log_file_emerg(final TitanLoggerApi.TitanLogEvent event) {
		boolean write_succes = true;
		final String event_str = event_to_string(event, false);
		if (event_str == null) {
			TtcnError.TtcnWarning("No text for event");
			return true;
		}
		if (er_ == null) {
			String filename_emergency = get_file_name(0);
			if (filename_emergency == null) {
				// Valid filename is not available, use specific one.
				filename_emergency = "emergency.log";
			} else {
				filename_emergency += "_emergency";
			}
			er_ = new File(filename_emergency);
			if (er_ == null) {
				fatal_error("Opening of log file {0} for writing failed.", filename_emergency);
			}
		}
		write_succes = true;
		try{
			final BufferedWriter localWriter = new BufferedWriter(new FileWriter(er_), 32768);
			log_file_writer.set(localWriter);
			localWriter.write(event_str);
			localWriter.newLine();
			localWriter.flush();
			localWriter.close();
		} catch (IOException e) {
			write_succes = false;
		}

		return write_succes;
	}
	
	private boolean log_file(final TitanLoggerApi.TitanLogEvent event, final boolean log_buffered) {
		if (is_disk_full_) {
			if (disk_full_action_.type == disk_full_action_type_t.DISKFULL_RETRY) {
				final int event_timestamp_seconds = event.get_field_timestamp__().get_field_seconds().get_int();
				final int event_timestamp_microseconds = event.get_field_timestamp__().get_field_microSeconds().get_int();
				int diff_seconds = 0;
				int diff_microseconds = 0;
				// If the specified time period has elapsed retry logging to file.
				if (event_timestamp_microseconds < disk_full_time_microseconds) {
					diff_seconds = event_timestamp_seconds - disk_full_time_seconds - 1;
					diff_microseconds = event_timestamp_microseconds + (1000000 - disk_full_time_microseconds);
				} else {
					diff_seconds = event_timestamp_seconds - disk_full_time_seconds;
					diff_microseconds = event_timestamp_microseconds - disk_full_time_microseconds;
				}
				if (diff_seconds >= disk_full_action_.retry_interval) {
					is_disk_full_ = false;
				} else {
					return false;
				}
				return false;
			}
		}

		final String event_str = event_to_string(event, false);
		if (event_str == null) {
			TtcnError.TtcnWarning("No text for event");
			return true;
		}

		final int bytes_to_log = event_str.length() + 1;
		if (logfile_size_ != 0 && logfile_bytes_ != 0 && !log_buffered) {
			if ((bytes_to_log + logfile_bytes_ + 1023) / 1024 > logfile_size_) {
				// Close current log file and open the next one.
				close_file();
				logfile_index_++;
				// Delete oldest log file if there is a file number limitation.
				if (logfile_number_ > 1) {
					if (logfile_index_ > logfile_number_) {
						final String filename_to_delete = get_file_name(logfile_index_- logfile_number_);
						final File file_to_delete = new File(filename_to_delete);
						if (file_to_delete.exists()) {
							file_to_delete.delete();
						}
					}
				}
				open_file(false);
			}
		}
		if (!log_buffered && (format_c_present_ || format_t_present_)) {
			switch (TTCN_Runtime.get_state()) {
			case HC_EXIT:
			case MTC_EXIT:
			case PTC_EXIT:
				// Can't call get_filename(), because it may call
				// TTCN_Runtime.get_host_name(), and TTCN_Runtime.clean_up() (which is
				// called once) has already happened.
				break;
			default: {
				final String new_filename = get_file_name(logfile_index_);
				if (new_filename != current_filename_) {
					String switched = "Switching to log file " + new_filename;
					final TitanLogEvent switched_event = new TitanLogEvent();
					switched_event.get_field_timestamp__().operator_assign(event.get_field_timestamp__());
					switched_event.get_field_sourceInfo__list().operator_assign(event.get_field_sourceInfo__list());
					switched_event.get_field_severity().operator_assign(TTCN_Logger.Severity.EXECUTOR_RUNTIME.ordinal());
					switched_event.get_field_logEvent().get_field_choice().get_field_unhandledEvent().operator_assign(switched);
					log_file(switched_event, true);
					switched = null;
					close_file();
					open_file(false);
				}
				break;
			}
			}
		}

		if (log_fp_ == null) {
			open_file(true);
		}
		boolean print_success = log_to_file(event_str);
		if (!print_success) {
			switch (disk_full_action_.type) {
			case DISKFULL_ERROR:
				fatal_error("Writing to log file failed.");
				break;
			case DISKFULL_STOP:
				is_disk_full_ = true;
				break;
			case DISKFULL_RETRY:
				is_disk_full_ = true;
				disk_full_time_seconds = event.get_field_timestamp__().get_field_seconds().get_int();
				disk_full_time_microseconds = event.get_field_timestamp__().get_field_microSeconds().get_int();
				break;
			case DISKFULL_DELETE:
				// Try to delete older logfiles while writing fails, must leave at least
				// two log files.  Stop with error if cannot delete more files and
				// cannot write log.
				if (logfile_number_ == 0) {
					logfile_number_ = logfile_index_;
				}
				while (!print_success && logfile_number_ > 2) {
					logfile_number_--;
					if (logfile_index_ > logfile_number_) {
						final String filename_to_delete = get_file_name(logfile_index_ - logfile_number_);
						final File file_to_delete = new File(filename_to_delete);
						boolean remove_ret_val = false;
						if (file_to_delete.exists()) {
							remove_ret_val = file_to_delete.delete();
						}
						if (!remove_ret_val) {
							break;
						}
						print_success = log_to_file(event_str);
					}
				}
				if (!print_success) {
					fatal_error("Writing to log file failed.");
				} else {
					logfile_bytes_ += bytes_to_log;
				}
				break;
			default:
				fatal_error("LegacyLogger.log(): invalid DiskFullAction type.");
				break;
			}
		} else {
			logfile_bytes_+= bytes_to_log;
		}
		return true;
	}

	private boolean log_to_file(final String message_ptr) {
		boolean is_success = true;
		final BufferedWriter localWriter = log_file_writer.get();
		try {
			localWriter.write(message_ptr);
		} catch (IOException e) {
			is_success = false;
		}

		if (is_success) {
			try {
				localWriter.newLine();
				localWriter.flush();
			} catch (IOException e) {
				is_success = false;
			}
		}

		return is_success;
	}

	private static void append_header(final StringBuilder returnValue, final int seconds, final int microseconds, final Severity severity, final StringBuilder sourceInfo) {
		TTCN_Logger.mputstr_timestamp(returnValue, TTCN_Logger.get_timestamp_format(), seconds, microseconds);

		returnValue.append(' ');

		if (TTCN_Logger.get_log_event_types() != log_event_types_t.LOGEVENTTYPES_NO) {
			TTCN_Logger.mput_severity(returnValue, severity);
			if (TTCN_Logger.get_log_event_types() == log_event_types_t.LOGEVENTTYPES_SUBCATEGORIES) {
				returnValue.append('_').append(TTCN_Logger.severity_subcategory_names[severity.ordinal()]);
			}

			returnValue.append(' ');
		}

		if (sourceInfo != null) {
			returnValue.append(sourceInfo).append(' ');
		}
	}

	private static String event_to_string(final TitanLoggerApi.TitanLogEvent event, final boolean without_header) {
		final StringBuilder returnValue = new StringBuilder();
		final StringBuilder sourceInfo = new StringBuilder();
		if (event.get_field_sourceInfo__list().is_bound()) {
			final source_info_format_t source_info_format = TTCN_Logger.get_source_info_format();
			final int stack_size = event.get_field_sourceInfo__list().size_of().get_int();
			if (stack_size > 0) {
				int i = 0;
				switch (source_info_format) {
				case SINFO_NONE:
					i = stack_size;
					break;
				case SINFO_SINGLE:
					i = stack_size - 1;
					break;
				case SINFO_STACK:
					break;
				}
				boolean firstLocation = true;
				for (; i < stack_size; i++) {
					final LocationInfo loc = event.get_field_sourceInfo__list().get_at(i);
					if (firstLocation) {
						firstLocation = false;
					} else {
						sourceInfo.append("->");
					}

					sourceInfo.append(loc.get_field_filename().get_value()).append(':').append(loc.get_field_line().get_int());

					switch (loc.get_field_ent__type().enum_value) {
					case controlpart:
						sourceInfo.append(MessageFormat.format("(controlpart:{0})", loc.get_field_ent__name()));
						break;
					case testcase__:
						sourceInfo.append(MessageFormat.format("(testcase:{0})", loc.get_field_ent__name()));
						break;
					case altstep__:
						sourceInfo.append(MessageFormat.format("(altstep:{0})", loc.get_field_ent__name()));
						break;
					case function__:
						sourceInfo.append(MessageFormat.format("(function:{0})", loc.get_field_ent__name()));
						break;
					case external__function:
						sourceInfo.append(MessageFormat.format("(externalfunction:{0})", loc.get_field_ent__name()));
						break;
					case template__:
						sourceInfo.append(MessageFormat.format("(template:{0})", loc.get_field_ent__name()));
						break;
					case UNBOUND_VALUE:
					case UNKNOWN_VALUE:
					case unknown:
						break;
					} 
				}
			} else {
				if (source_info_format == source_info_format_t.SINFO_SINGLE ||
						source_info_format == source_info_format_t.SINFO_STACK) {
					sourceInfo.append('-');
				}
			}
		}

		final int severityIndex = event.get_field_severity().get_int();
		final Severity severity = Severity.values()[severityIndex];
		append_header(returnValue, event.get_field_timestamp__().get_field_seconds().get_int(), event.get_field_timestamp__().get_field_microSeconds().get_int(), severity, sourceInfo);

		final LogEventType_choice choice = event.get_field_logEvent().get_field_choice();
		switch (choice.get_selection()) {
		case UNBOUND_VALUE:
			return returnValue.toString();
		case ALT_unhandledEvent:
			returnValue.append(choice.get_field_unhandledEvent().get_value());
			break;
		case ALT_timerEvent:
			timer_event_str(returnValue, choice.get_field_timerEvent().get_field_choice());
			break;
		case ALT_statistics:
			statistics_str(returnValue, choice.get_field_statistics().get_field_choice());
			break;
		case ALT_verdictOp:
			verdictop_str(returnValue, choice.get_field_verdictOp().get_field_choice());
			break;
		case ALT_testcaseOp:
			testcaseop_str(returnValue, choice.get_field_testcaseOp().get_field_choice());
			break;
		case ALT_actionEvent: {
			final Strings_str__list slist = choice.get_field_actionEvent().get_field_str__list();
			final int size = slist.size_of().get_int();
			for (int i = 0; i < size; i++) {
				returnValue.append(slist.get_at(i).get_value());
			}
			break;
		}
		case ALT_userLog: {
			final Strings_str__list slist = choice.get_field_userLog().get_field_str__list();
			final int size = slist.size_of().get_int();
			for (int i = 0; i < size; i++) {
				returnValue.append(slist.get_at(i).get_value());
			}
			break;
		}
		case ALT_debugLog:
			returnValue.append(choice.get_field_debugLog().get_field_text().get_value());
			break;
		case ALT_errorLog:
			returnValue.append(choice.get_field_errorLog().get_field_text().get_value());
			break;
		case ALT_warningLog:
			returnValue.append(choice.get_field_warningLog().get_field_text().get_value());
			break;
		case ALT_defaultEvent:
			defaultop_event_str(returnValue, choice.get_field_defaultEvent().get_field_choice());
			break;
		case ALT_executionSummary:
			//TODO needs to be checked if this needs to be empty.
			break;
		case ALT_executorEvent:
			executor_event_str(returnValue, choice.get_field_executorEvent().get_field_choice());
			break;
		case ALT_matchingEvent:
			matchingop_str(returnValue, choice.get_field_matchingEvent().get_field_choice());
			break;
		case ALT_functionEvent: {
			switch (choice.get_field_functionEvent().get_field_choice().get_selection()) {
			case ALT_random : {
				final FunctionEvent_choice_random ra = choice.get_field_functionEvent().get_field_choice().get_field_random();
				switch (ra.get_field_operation().enum_value) {
				case seed:
					returnValue.append(MessageFormat.format("Random number generator was initialized with seed {0}: {1}", ra.get_field_retval().get_value(), ra.get_field_intseed().get_int()));
					break;
				case read__out:
					returnValue.append(MessageFormat.format("Function rnd() returned {0}.", ra.get_field_retval().get_value()));
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
		case ALT_parallelEvent:
			parallel_str(returnValue, choice.get_field_parallelEvent().get_field_choice());
			break;
		case ALT_portEvent:
			portevent_str(returnValue, choice.get_field_portEvent().get_field_choice());
			break;
		}

		return returnValue.toString();
	}

	private static void timer_event_str(final StringBuilder returnValue, final TimerEvent_choice choice) {
		switch (choice.get_selection()) {
		case ALT_readTimer: {
			final TimerType timer = choice.get_field_readTimer();
			returnValue.append(MessageFormat.format("Read timer {0}: {1} s", timer.get_field_name().get_value(), timer.get_field_value__().get_value()));
			break;
		}
		case ALT_startTimer: {
			final TimerType timer = choice.get_field_startTimer();
			returnValue.append(MessageFormat.format("Start timer {0}: {1} s", timer.get_field_name().get_value(), timer.get_field_value__().get_value()));
			break;
		}
		case ALT_guardTimer: {
			final TimerGuardType timer = choice.get_field_guardTimer();
			returnValue.append(MessageFormat.format("Test case guard timer was set to {0} s", timer.get_field_value__().get_value()));
			break;
		}
		case ALT_stopTimer: {
			final TimerType timer = choice.get_field_stopTimer();
			returnValue.append(MessageFormat.format("Stop timer {0}: {1} s", timer.get_field_name().get_value(), timer.get_field_value__().get_value()));
			break;
		}
		case ALT_timeoutTimer: {
			final TimerType timer = choice.get_field_timeoutTimer();
			returnValue.append(MessageFormat.format("Timeout {0}: {1} s", timer.get_field_name().get_value(), timer.get_field_value__().get_value()));
			break;
		}
		case ALT_timeoutAnyTimer: {
			returnValue.append("Operation `any timer.timeout' was successful.");
			break;
		}
		case ALT_unqualifiedTimer: {
			returnValue.append(choice.get_field_unqualifiedTimer().get_value());
			break;
		}
		case UNBOUND_VALUE:
			break;
		}
	}

	private static void defaultop_event_str(final StringBuilder returnValue, final DefaultEvent_choice choice) {
		switch (choice.get_selection()) {
		case ALT_defaultopActivate: {
			final DefaultOp dflt = choice.get_field_defaultopActivate();
			returnValue.append(MessageFormat.format("Altstep {0} was activated as default, id {1}", dflt.get_field_name().get_value(), dflt.get_field_id().get_int()));
			break;
		}
		case ALT_defaultopDeactivate: {
			final DefaultOp dflt = choice.get_field_defaultopDeactivate();
			if (dflt.get_field_name().lengthof().is_greater_than(0)) {
				returnValue.append(MessageFormat.format("Default with id {0} (altstep {1}) was deactivated.", dflt.get_field_id().get_int(), dflt.get_field_name().get_value()));
			} else {
				returnValue.append("Deactivate operation on a null default reference was ignored.");
			}
			break;
		}
		case ALT_defaultopExit: {
			final DefaultOp dflt = choice.get_field_defaultopExit();
			returnValue.append(MessageFormat.format("Default with id {0} (altstep {1}) ", dflt.get_field_id().get_int(), dflt.get_field_name().get_value()));

			switch (dflt.get_field_end().enum_value) {
			case UNBOUND_VALUE:
			case UNKNOWN_VALUE:
				break;
			case finish:
				returnValue.append("finished. Skipping current alt statement or receiving operation.");
				break;
			case break__:
				returnValue.append("has reached a repeat statement.");
				//FIXME break and repeat might be mixed up !!!
				break;
			case repeat__:
				returnValue.append("has reached a break statement. Skipping current alt statement or receiving operation.");
				break;
			}
		}
		case UNBOUND_VALUE:
			break;
		}
	}

	private static void executor_event_str(final StringBuilder returnValue, final TitanLoggerApi.ExecutorEvent_choice eec) {
		switch (eec.get_selection()) {
		case UNBOUND_VALUE:
			break;
		case ALT_executorRuntime: {
			final ExecutorRuntime rt = eec.get_field_executorRuntime();
			switch (rt.get_field_reason().enum_value) {
			case UNBOUND_VALUE:
			case UNKNOWN_VALUE:
				break;
			case connected__to__mc:
				returnValue.append("Connected to MC.");
				break;
			case disconnected__from__mc:
				returnValue.append("Disconnected from MC.");
				break;
			case initialization__of__modules__failed:
				returnValue.append("Initialization of modules failed.");
				break;
			case exit__requested__from__mc__hc:
				returnValue.append("Exit was requested from MC. Terminating HC.");
				break;
			case exit__requested__from__mc__mtc:
				returnValue.append("Exit was requested from MC. Terminating MTC.");
				break;
			case stop__was__requested__from__mc:
				returnValue.append("Stop was requested from MC.");
				break;
			case stop__was__requested__from__mc__ignored__on__idle__mtc:
				returnValue.append("Stop was requested from MC. Ignored on idle MTC.");
				break;
			case stop__was__requested__from__mc__ignored__on__idle__ptc:
				returnValue.append("Stop was requested from MC. Ignored on idle PTC.");
				break;
			case executing__testcase__in__module:
				returnValue.append(MessageFormat.format("Executing test case {0} in module {1}.", rt.get_field_testcase__name().get(), rt.get_field_module__name().get()));
				break;
			case performing__error__recovery:
				returnValue.append("Performing error recovery.");
				break;
			case executor__start__single__mode:
				returnValue.append(MessageFormat.format("TTCN-3 Test Executor started in single mode. Version: {0} .", TTCN_Runtime.PRODUCT_NUMBER));
				break;
			case executor__finish__single__mode:
				returnValue.append("TTCN-3 Test Executor finished in single mode.");
				break;
			case exiting:
				returnValue.append("Exiting.");
				break;
			case fd__limits:
				returnValue.append(MessageFormat.format("Maximum number of open file descriptors: {0},   FD_SETSIZE = {1}", rt.get_field_pid().get().get_int(), rt.get_field_fd__setsize()));
				break;
			case host__controller__started:
				returnValue.append(MessageFormat.format("TTCN-3 Host Controller started on {0}. Version: {1}. ", rt.get_field_module__name().get().get_value(), TTCN_Runtime.PRODUCT_NUMBER));
				break;
			case host__controller__finished:
				returnValue.append("TTCN-3 Host Controller finished.");
				break;
			case initializing__module:
				returnValue.append(MessageFormat.format("Initializing module {0}.", rt.get_field_module__name().get().get_value()));
				break;
			case initialization__of__module__finished:
				returnValue.append(MessageFormat.format("Initializing module {0} finished.", rt.get_field_module__name().get().get_value()));
				break;
			case mtc__created:
				returnValue.append(MessageFormat.format("MTC was created. Process id: {0}.", rt.get_field_pid().get().get_int()));
				break;
			case overload__check:
				returnValue.append("Trying to create a dummy child process to verify if the host is still overloaded.");
				break;
			case overload__check__fail:
				returnValue.append("Creation of the dummy child process failed.");
				break;
			case overloaded__no__more:
				break;
			case resuming__execution:
				returnValue.append("Resuming execution.");
				break;
			case stopping__control__part__execution:
				returnValue.append("Resuming control part execution.");
				break;
			case stopping__current__testcase:
				returnValue.append("Stopping current testcase.");
				break;
			case stopping__test__component__execution:
				returnValue.append("Stopping test component execution.");
				break;
			case terminating__execution:
				returnValue.append("Terminating execution.");
				break;
			case user__paused__waiting__to__resume:
				returnValue.append("User has paused execution. Waiting for continue.");
				break;
			case waiting__for__ptcs__to__finish:
				returnValue.append("Waiting for PTCs to finish.");
				break;
			}
			break;
		}
		case ALT_executorConfigdata: {
			final ExecutorConfigdata cfg = eec.get_field_executorConfigdata();
			switch (cfg.get_field_reason().enum_value) {
			case UNBOUND_VALUE:
			case UNKNOWN_VALUE:
				break;
			case received__from__mc:
				returnValue.append("Processing configuration data received from MC.");
				break;
			case processing__failed:
				returnValue.append("Processing of configuration data failed.");
				break;
			case processing__succeeded:
				returnValue.append("Configuration data was processed successfully.");
				break;
			case module__has__parameters:
				break;
			case using__config__file:
				returnValue.append(MessageFormat.format("Using configuration file: `{0}''.", cfg.get_field_param__().get().get_value()));
				break;
			case overriding__testcase__list:
				returnValue.append(MessageFormat.format("Overriding testcase list: {0}.", cfg.get_field_param__().get().get_value()));
				break;
			}
			break;
		}
		case ALT_executorComponent: {
			final ExecutorComponent cm = eec.get_field_executorComponent();
			switch (cm.get_field_reason().enum_value) {
			case UNBOUND_VALUE:
			case UNKNOWN_VALUE:
				break;
			case mtc__started:
				returnValue.append(MessageFormat.format("TTCN-3 Main Test Component started on {0}. Version: {1}.", TTCN_Runtime.get_host_name(), TTCN_Runtime.PRODUCT_NUMBER));
				break;
			case mtc__finished:
				returnValue.append("TTCN-3 Main Test Component finished.");
				break;
			case ptc__started:
				break;
			case ptc__finished:
				returnValue.append("TTCN-3 Parallel Test Component finished.");
				break;
			case component__init__fail:
				returnValue.append("Component type initialization failed. PTC terminates.");
				break;
			}
			break;
		}
		case ALT_executorMisc: {
			final ExecutorUnqualified ex = eec.get_field_executorMisc();
			final String name = ex.get_field_name().get_value().toString();
			final String ip_addr_str = ex.get_field_addr().get_value().toString();
			switch (ex.get_field_reason().enum_value) {
			case UNBOUND_VALUE:
			case UNKNOWN_VALUE:
				break;
			case address__of__mc__was__set:
				if (name.equals(ip_addr_str)) {
					returnValue.append(MessageFormat.format("The address of MC was set to {0}[{1}]:{2}.", name, ip_addr_str, ex.get_field_port__().get_int()));
				} else {
					returnValue.append(MessageFormat.format("The address of MC was set to {0}:{1}.", ip_addr_str, ex.get_field_port__().get_int()));
				}
				break;
			case address__of__control__connection:
				returnValue.append(MessageFormat.format("The local IP address of the control connection to MC is {0}.", ip_addr_str));
				break;
			case host__support__unix__domain__sockets:
				if (ex.get_field_port__().get_int() == 0) {
					returnValue.append("This host supports UNIX domain sockets for local communication.");
				} else {
					returnValue.append("This host does not support UNIX domain sockets for local communication.");
				}
				break;
			case local__address__was__set:
				if (name.equals(ip_addr_str)) {
					returnValue.append(MessageFormat.format("The local address was set to {0}[{1}].", name, ip_addr_str));
				} else {
					returnValue.append(MessageFormat.format("The local address was set to {0}.", ip_addr_str));
				}
				break;
			}
			break;
		}
		case ALT_logOptions:
			returnValue.append(eec.get_field_logOptions().get_value());
			returnValue.append(plugin_specific_settings());
			//FIXME also log plugin specific setting
			break;
		case ALT_extcommandStart:
			returnValue.append(MessageFormat.format("Starting external command `{0}''.", eec.get_field_extcommandStart().get_value()));
			break;
		case ALT_extcommandSuccess:
			returnValue.append(MessageFormat.format("External command `{0}'' was executed successfully (exit status: 0).", eec.get_field_extcommandSuccess()));
			break;
		}
	}

	private static void verdictop_str(final StringBuilder returnValue, final VerdictOp_choice choice) {
		switch (choice.get_selection()) {
		case UNBOUND_VALUE:
			break;
		case ALT_setVerdict: {
			final SetVerdictType set = choice.get_field_setVerdict();
			final int newOrdinal = set.get_field_newVerdict().enum_value.ordinal();
			final String newVerdictName = VerdictTypeEnum.values()[newOrdinal].getName();
			final int oldOrdinal = set.get_field_oldVerdict().enum_value.ordinal();
			final String oldVerdictName = VerdictTypeEnum.values()[oldOrdinal].getName();
			final int localOrdinal = set.get_field_localVerdict().enum_value.ordinal();
			final String localVerdictName = VerdictTypeEnum.values()[localOrdinal].getName();

			if (set.get_field_newVerdict().is_greater_than(set.get_field_oldVerdict())) {
				if (!set.get_field_oldReason().is_present() || !set.get_field_newReason().is_present()) {
					returnValue.append(MessageFormat.format("setverdict({0}): {1} -> {2}", newVerdictName, oldVerdictName, localVerdictName));
				} else {
					returnValue.append(MessageFormat.format("setverdict({0}): {1} -> {2} reason: \"{3}\", new component reason: \"{4}\"", newVerdictName, oldVerdictName, localVerdictName, set.get_field_oldReason().get().get_value(), set.get_field_newReason().get().get_value()));
				}
			} else {
				if (!set.get_field_oldReason().is_present() || !set.get_field_newReason().is_present()) {
					returnValue.append(MessageFormat.format("setverdict({0}): {1} -> {2}, component reason not changed", newVerdictName, oldVerdictName, localVerdictName));
				} else {
					returnValue.append(MessageFormat.format("setverdict({0}): {1} -> {2} reason: \"{3}\", component reason not changed", newVerdictName, oldVerdictName, localVerdictName, set.get_field_oldReason().get().get_value()));
				}
			}
			break;
		}
		case ALT_getVerdict: {
			final int tempOrdinal = choice.get_field_getVerdict().enum_value.ordinal();
			final String tempVerdictName = VerdictTypeEnum.values()[tempOrdinal].getName();
			returnValue.append(MessageFormat.format("getverdict: {0}", tempVerdictName));
			break;
		}
		case ALT_finalVerdict:
			switch (choice.get_field_finalVerdict().get_field_choice().get_selection()) {
			case UNBOUND_VALUE:
				break;
			case ALT_info: {
				final FinalVerdictInfo info = choice.get_field_finalVerdict().get_field_choice().get_field_info();
				if (info.get_field_is__ptc().getValue()) {
					if (info.get_field_ptc__compref().is_present() && info.get_field_ptc__compref().get().get_int() != TitanComponent.UNBOUND_COMPREF) {
						if (info.get_field_ptc__name().is_present() && info.get_field_ptc__name().get().lengthof().get_int() > 0) {
							returnValue.append(MessageFormat.format("Local verdict of PTC {0}({1}): ", info.get_field_ptc__name().get().get_value(), info.get_field_ptc__compref().get().get_int()));
						} else {
							returnValue.append(MessageFormat.format("Local verdict of PTC with component reference {0}: ", info.get_field_ptc__compref().get().get_int()));
						}

						final int ptcOrdinal = info.get_field_ptc__verdict().enum_value.ordinal();
						final String ptcVerdictName = VerdictTypeEnum.values()[ptcOrdinal].getName();
						final int localOrdinal = info.get_field_local__verdict().enum_value.ordinal();
						final String localVerdictName = VerdictTypeEnum.values()[localOrdinal].getName();
						final int newOrdinal = info.get_field_new__verdict().enum_value.ordinal();
						final String newVerdictName = VerdictTypeEnum.values()[newOrdinal].getName();

						returnValue.append(MessageFormat.format("{0} ({1} -> {2})", ptcVerdictName, localVerdictName, newVerdictName));
						if (info.get_field_verdict__reason().is_present() && info.get_field_verdict__reason().get().lengthof().get_int() > 0) {
							returnValue.append(MessageFormat.format(" reason: \"{0}\"", info.get_field_verdict__reason().get().get_value()));
						}
					} else {
						final int localOrdinal = info.get_field_local__verdict().enum_value.ordinal();
						final String localVerdictName = VerdictTypeEnum.values()[localOrdinal].getName();

						returnValue.append(MessageFormat.format("Final verdict of PTC: {0}", localVerdictName));
						if (info.get_field_verdict__reason().is_present() && info.get_field_verdict__reason().get().lengthof().get_int() > 0) {
							returnValue.append(MessageFormat.format(" reason: \"{0}\"", info.get_field_verdict__reason().get().get_value()));
						}
					}
				} else {
					final int localOrdinal = info.get_field_local__verdict().enum_value.ordinal();
					final String localVerdictName = VerdictTypeEnum.values()[localOrdinal].getName();

					returnValue.append(MessageFormat.format("Local verdict of MTC: {0}", localVerdictName));
					if (info.get_field_verdict__reason().is_present() && info.get_field_verdict__reason().get().lengthof().get_int() > 0) {
						returnValue.append(MessageFormat.format(" reason: \"{0}\"", info.get_field_verdict__reason().get().get_value()));
					}
				}
				break;
			}
			case ALT_notification:
				switch (choice.get_field_finalVerdict().get_field_choice().get_field_notification().enum_value) {
				case UNBOUND_VALUE:
				case UNKNOWN_VALUE:
					break;
				case no__ptcs__were__created:
					returnValue.append("No PTCs were created.");
					break;
				case setting__final__verdict__of__the__test__case:
					returnValue.append("Setting final verdict of the test case.");
					break;
				}
				break;
			}
		}
	}

	private static void statistics_str(final StringBuilder returnValue, final StatisticsType_choice choice) {
		switch (choice.get_selection()) {
		case ALT_verdictStatistics: {
			final StatisticsType_choice_verdictStatistics statistics = choice.get_field_verdictStatistics();
			final int none_count = statistics.get_field_none__().get_int();
			final int pass_count = statistics.get_field_pass__().get_int();
			final int inconc_count = statistics.get_field_inconc__().get_int();
			final int fail_count = statistics.get_field_fail__().get_int();
			final int error_count = statistics.get_field_error__().get_int();
			if (none_count > 0 || pass_count > 0 || inconc_count > 0 || fail_count > 0 || error_count > 0) {
				returnValue.append(MessageFormat.format("Verdict Statistics: {0} none ({1} %), {2} pass ({3} %), {4} inconc ({5} %), {6} fail ({7} %), {8} error ({9} %)",
								none_count, statistics.get_field_nonePercent().get_value(),
								pass_count, statistics.get_field_passPercent().get_value(),
								inconc_count, statistics.get_field_inconcPercent().get_value(),
								fail_count, statistics.get_field_failPercent().get_value(),
								error_count, statistics.get_field_errorPercent().get_value()));
			} else {
				returnValue.append("Verdict statistics: 0 none, 0 pass, 0 inconc, 0 fail, 0 error.");
			}
			break;
		}
		case ALT_controlpartStart:
			returnValue.append(MessageFormat.format("Execution of control part in module {0} started.", choice.get_field_controlpartStart().get_value()));
			break;
		case ALT_controlpartFinish:
			returnValue.append(MessageFormat.format("Execution of control part in module {0} finished.", choice.get_field_controlpartFinish().get_value()));
			break;
		case ALT_controlpartErrors:
			returnValue.append(MessageFormat.format("Number of errors outside test cases: {0}", choice.get_field_controlpartErrors().get_int()));
			break;
		case UNBOUND_VALUE:
			break;
		}
	}

	private static void parallel_str(final StringBuilder returnValue, final ParallelEvent_choice choice) {
		switch (choice.get_selection()) {
		case UNBOUND_VALUE:
			break;
		case ALT_parallelPTC: {
			final ParallelPTC ptc = choice.get_field_parallelPTC();
			switch (ptc.get_field_reason().enum_value) {
			case UNBOUND_VALUE:
			case UNKNOWN_VALUE:
				break;
			case init__component__start:
				returnValue.append(MessageFormat.format("Initializing variables, timers and ports of component type {0}.{1}", ptc.get_field_module__().get_value(), ptc.get_field_name().get_value()));
				if (ptc.get_field_tc__loc().lengthof().get_int() > 0) {
					returnValue.append(MessageFormat.format(" inside testcase {0}", ptc.get_field_tc__loc().get_value()));
				}
				returnValue.append('.');
				break;
			case init__component__finish:
				returnValue.append(MessageFormat.format("Component type {0}.{1} was initialized.", ptc.get_field_module__().get_value(), ptc.get_field_name().get_value()));
				break;
			case terminating__component:
				returnValue.append(MessageFormat.format("Terminating component type {0}.{1}.", ptc.get_field_module__().get_value(), ptc.get_field_name().get_value()));
				break;
			case component__shut__down:
				returnValue.append(MessageFormat.format("Component type {0}.{1} was shut down", ptc.get_field_module__().get_value(), ptc.get_field_name().get_value()));
				if (ptc.get_field_tc__loc().lengthof().get_int() > 0) {
					returnValue.append(MessageFormat.format(" inside testcase {0}", ptc.get_field_tc__loc().get_value()));
				}
				returnValue.append('.');
				break;
			case error__idle__ptc:
				returnValue.append("Error occurred on idle PTC. The component terminates.");
				break;
			case ptc__created:
				returnValue.append(MessageFormat.format("PTC was created. Component reference: {0}, alive: {1}, type: {2}.{3}", ptc.get_field_compref().get_int(), ptc.get_field_alive__pid().get_int() > 0 ? "yes" : "no", ptc.get_field_module__().get_value(), ptc.get_field_name().get_value()));
				if (ptc.get_field_compname().lengthof().get_int() > 0) {
					returnValue.append(MessageFormat.format(", component name: {0}", ptc.get_field_compname().get_value()));
				}
				if (ptc.get_field_tc__loc().lengthof().get_int() != 0) {
					returnValue.append(MessageFormat.format(", location: {0}", ptc.get_field_tc__loc().get_value()));
				}
				returnValue.append('.');
				break;
			case ptc__created__pid:
				returnValue.append(MessageFormat.format("PTC was created. Component reference: {0}, component type: {2}.{3}", ptc.get_field_compref().get_int(), ptc.get_field_module__().get_value(), ptc.get_field_name().get_value()));
				if (ptc.get_field_compname().lengthof().get_int() > 0) {
					returnValue.append(MessageFormat.format(", component name: {0}", ptc.get_field_compname().get_value()));
				}
				if (ptc.get_field_tc__loc().lengthof().get_int() != 0) {
					returnValue.append(MessageFormat.format(", testcase name: {0}", ptc.get_field_tc__loc().get_value()));
				}
				returnValue.append(MessageFormat.format(", process id: {0}.", ptc.get_field_alive__pid().get_int()));
				break;
			case function__started:
				returnValue.append("Function was started.");
				break;
			case function__stopped:
				returnValue.append(MessageFormat.format("Function {0} was stopped. PTC terminates.", ptc.get_field_name().get_value()));
				break;
			case function__finished:
				returnValue.append(MessageFormat.format("Function {0} finished. PTC {1}.", ptc.get_field_name().get_value(), ptc.get_field_alive__pid().get_int() == 0 ? "terminates" :  "remains alive and is waiting for next start"));
				break;
			case function__error:
				returnValue.append(MessageFormat.format("Function {0} finished with an error. PTC terminates.", ptc.get_field_name().get_value()));
				break;
			case ptc__done:
				returnValue.append(MessageFormat.format("PTC with component reference {0} is done.", ptc.get_field_compref().get_int()));
				break;
			case ptc__killed:
				returnValue.append(MessageFormat.format("PTC with component reference {0} is killed.", ptc.get_field_compref().get_int()));
				break;
			case stopping__mtc:
				returnValue.append("Stopping MTC. The current test case will be terminated.");
				break;
			case ptc__stopped:
				returnValue.append(MessageFormat.format("PTC with component reference {0} was stopped.", ptc.get_field_compref().get_int()));
				break;
			case all__comps__stopped:
				returnValue.append("All components were stopped.");
				break;
			case ptc__was__killed:
				returnValue.append(MessageFormat.format("PTC with component reference {0} was killed.", ptc.get_field_compref().get_int()));
				break;
			case all__comps__killed:
				returnValue.append("All components were killed.");
				break;
			case kill__request__frm__mc:
				returnValue.append("Kill was requested from MC. Terminating idle PTC.");
				break;
			case mtc__finished:
				returnValue.append("MTC finished.");
				//TODO add process statistics
				break;
			case ptc__finished:
				returnValue.append(MessageFormat.format("TC {0}({1}) finished.", ptc.get_field_compname().get_value(), ptc.get_field_compref().get_int()));
				//TODO add process statistics
				break;
			case starting__function:
				break;
			}
			break; 
		}
		case ALT_parallelPTC__exit: {
			final PTC__exit px = choice.get_field_parallelPTC__exit();
			final int compref = px.get_field_compref().get_int();
			if (compref == TitanComponent.MTC_COMPREF) {
				returnValue.append("MTC finished.");
			} else {
				final String comp_name = TitanComponent.get_component_string(compref);
				if (comp_name == null) {
					returnValue.append(MessageFormat.format("PTC with component reference {0} finished.", compref));
				} else {
					returnValue.append(MessageFormat.format("PTC {0}({1}) finished.", comp_name, compref));
				}
				returnValue.append(MessageFormat.format(" Process statistics: { process id: {0}, ", px.get_field_pid().get_int()));
				//TOXO not finished in compiler
			}
			break;
		}
		case ALT_parallelPort: {
			final ParPort pp = choice.get_field_parallelPort();
			String direction = "on";
			String preposition = "and";
			switch (pp.get_field_operation().enum_value) {
			case UNBOUND_VALUE:
			case UNKNOWN_VALUE:
				break;
			case connect__:
				returnValue.append("Connect");
				break;
			case disconnect__:
				returnValue.append("Disconnect");
				break;
			case map__:
				returnValue.append("Map");
				direction = "of";
				preposition = "to";
				break;
			case unmap__:
				returnValue.append("Unmap");
				direction = "of";
				preposition = "from";
				break;
			}

			final String src = TitanComponent.get_component_string(pp.get_field_srcCompref().get_int());
			final String dst = TitanComponent.get_component_string(pp.get_field_dstCompref().get_int());
			returnValue.append(MessageFormat.format(" operation {0} {1}:{2} {3} {4}:{5} finished.", direction, src, pp.get_field_srcPort().get_value(), preposition, dst, pp.get_field_dstPort().get_value()));
			break;
		}
		}
	}

	private static void testcaseop_str(final StringBuilder returnValue, final TestcaseEvent_choice choice) {
		switch (choice.get_selection()) {
		case ALT_testcaseStarted:
			returnValue.append(MessageFormat.format("Test case {0} started.", choice.get_field_testcaseStarted().get_field_testcase__name().get_value()));
			break;
		case ALT_testcaseFinished:
			final int ordinal = choice.get_field_testcaseFinished().get_field_verdict().enum_value.ordinal();
			final String verdictName = VerdictTypeEnum.values()[ordinal].getName();
			returnValue.append(MessageFormat.format("Test case {0} finished. Verdict: {1}", choice.get_field_testcaseFinished().get_field_name().get_field_testcase__name().get_value(), verdictName));
			break;
		case UNBOUND_VALUE:
		default:
			break;
		}
	}

	private static void matchingop_str(final StringBuilder returnValue, final MatchingEvent_choice choice) {
		switch (choice.get_selection()) {
		case ALT_matchingDone: {
			final MatchingDoneType md = choice.get_field_matchingDone();
			switch (md.get_field_reason().enum_value) {
			case UNBOUND_VALUE:
			case UNKNOWN_VALUE:
				break;
			case done__failed__no__return:
				returnValue.append(MessageFormat.format("Done operation with type {0} on PTC {1}  failed: The started function did not return a value.", md.get_field_type__().get_value(), md.get_field_ptc().get_int()));
				break;
			case done__failed__wrong__return__type:
				returnValue.append(MessageFormat.format("Done operation with type {0} on PTC {1}  failed: The started function returned a value of type {2}.", md.get_field_type__().get_value(), md.get_field_ptc().get_int(), md.get_field_return__type().get_value()));
				break;
			case any__component__done__successful:
				returnValue.append("Operation 'any component.done' was successful.");
				break;
			case any__component__done__failed:
				returnValue.append("Operation 'any component.done' failed because no PTCs were created in the testcase.");
				break;
			case all__component__done__successful:
				returnValue.append("Operation 'all component.done' was successful.");
				break;
			case any__component__killed__successful:
				returnValue.append("Operation 'any component.killed' was successful.");
				break;
			case any__component__killed__failed:
				returnValue.append("Operation 'any component.killed' failed because no PTCs were created in the testcase.");
				break;
			case all__component__killed__successful:
				returnValue.append("Operation 'all component.killed' was successful.");
				break;
			}
			break;
		}
		case ALT_matchingTimeout: {
			final MatchingTimeout mt = choice.get_field_matchingTimeout();
			if (mt.get_field_timer__name().is_present()) {
				returnValue.append(MessageFormat.format("Timeout operation on timer {0} failed: The timer is not started.", mt.get_field_timer__name().get().get_value()));
			} else {
				returnValue.append("Operation `any timer.timeout' failed: The test component does not have active timers.");
			}
			break;
		}
		case ALT_matchingFailure: {
			final MatchingFailureType mf = choice.get_field_matchingFailure();
			boolean is_call = false;
			switch (mf.get_field_reason().enum_value) {
			case message__does__not__match__template:
				returnValue.append(MessageFormat.format("Matching on port {0} {1}: First message in the queue does not match the template: ", mf.get_field_port__name().get_value(), mf.get_field_info().get_value()));
				break;
			case exception__does__not__match__template:
				returnValue.append(MessageFormat.format("Matching on port {0} failed: The first exception in the queue does not match the template: {1}", mf.get_field_port__name().get_value(), mf.get_field_info().get_value()));
				break;
			case parameters__of__call__do__not__match__template:
				is_call = true; // fall through
			case parameters__of__reply__do__not__match__template:
				returnValue.append(MessageFormat.format("Matching on port {0} failed: The parameters of the first {1} in the queue do not match the template: {2}", mf.get_field_port__name().get_value(), is_call ? "call" : "reply", mf.get_field_info().get_value()));
				break;
			case sender__does__not__match__from__clause:
				returnValue.append(MessageFormat.format("Matching on port {0} failed: Sender of the first entity in the queue does not match the from clause: {1}", mf.get_field_port__name().get_value(), mf.get_field_info().get_value()));
				break;
			case sender__is__not__system:
				returnValue.append(MessageFormat.format("Matching on port {0} failed: Sender of the first entity in the queue is not the system.", mf.get_field_port__name().get_value()));
				break;
			case not__an__exception__for__signature:
				returnValue.append(MessageFormat.format("Matching on port {0} failed: The first entity in the queue is not an exception for signature {1}.", mf.get_field_port__name().get_value(), mf.get_field_info().get_value()));
				break;
			default:
				break;
			}
			break;
		}
		case ALT_matchingSuccess: {
			final MatchingSuccessType ms = choice.get_field_matchingSuccess();
			returnValue.append(MessageFormat.format("Matching on port {0} succeeded: {1}", ms.get_field_port__name().get_value(), ms.get_field_info().get_value()));
			break;
		}
		case ALT_matchingProblem: {
			final MatchingProblemType mp = choice.get_field_matchingProblem();
			returnValue.append("Operation `");
			if (mp.get_field_any__port().getValue()) {
				returnValue.append("any port.");
			}

			if (mp.get_field_check__().getValue()) {
				returnValue.append("check(");
			}
			switch (mp.get_field_operation().enum_value) {
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
			if (mp.get_field_check__().getValue()) {
				returnValue.append(')');
			}
			returnValue.append("' ");

			if (mp.get_field_port__name().is_bound()) {
				returnValue.append(MessageFormat.format("on port {0} ", mp.get_field_port__name().get_value()));
			}
			// we could also check that any__port is false

			returnValue.append("failed: ");

			switch (mp.get_field_reason().enum_value) {
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
		case UNBOUND_VALUE:
			break;
		}
	}

	public static String plugin_specific_settings() {
		final String disk_full_action_type_names[] = { "Error", "Stop", "Retry", "Delete" };
		String disk_full_action_str = null;
		if (myself.disk_full_action_.type == disk_full_action_type_t.DISKFULL_RETRY) {
			disk_full_action_str = MessageFormat.format("Retry({0})", myself.disk_full_action_.retry_interval);
		} else {
			disk_full_action_str = disk_full_action_type_names[myself.disk_full_action_.type.ordinal()];
		}
		return MessageFormat.format("LogFileSize:={0}; LogFileNumber:={1}; DiskFullAction:={2}", myself.logfile_size_,myself.logfile_number_,disk_full_action_str);
	}

	private static void portevent_str(final StringBuilder returnValue, final PortEvent_choice choice) {
		switch (choice.get_selection()) {
		case ALT_portQueue: {
			final Port__Queue portQueue = choice.get_field_portQueue();
			switch (portQueue.get_field_operation().enum_value) {
			case enqueue__msg: {
				final String comp_str = TitanComponent.get_component_string(portQueue.get_field_compref().get_int());
				returnValue.append(MessageFormat.format("Message enqueued on {0} from {1}{2}{3} id {4}", portQueue.get_field_port__name().get_value(), comp_str, portQueue.get_field_address__().get_value(), portQueue.get_field_param__().get_value(), portQueue.get_field_msgid().get_int()));
				break;
			}
			case enqueue__call: {
				final String comp_str = TitanComponent.get_component_string(portQueue.get_field_compref().get_int());
				returnValue.append(MessageFormat.format("Call enqueued on {0} from {1}{2}{3} id {4}", portQueue.get_field_port__name().get_value(), comp_str, portQueue.get_field_address__().get_value(), portQueue.get_field_param__().get_value(), portQueue.get_field_msgid().get_int()));
				break;
			}
			case enqueue__reply: {
				final String comp_str = TitanComponent.get_component_string(portQueue.get_field_compref().get_int());
				returnValue.append(MessageFormat.format("Reply enqueued on {0} from {1}{2}{3} id {4}", portQueue.get_field_port__name().get_value(), comp_str, portQueue.get_field_address__().get_value(), portQueue.get_field_param__().get_value(), portQueue.get_field_msgid().get_int()));
				break;
			}
			case enqueue__exception: {
				final String comp_str = TitanComponent.get_component_string(portQueue.get_field_compref().get_int());
				returnValue.append(MessageFormat.format("Exception enqueued on {0} from {1}{2}{3} id {4}", portQueue.get_field_port__name().get_value(), comp_str, portQueue.get_field_address__().get_value(), portQueue.get_field_param__().get_value(), portQueue.get_field_msgid().get_int()));
				break;
			}
			case extract__msg:
				returnValue.append(MessageFormat.format("Message with id {0} was extracted from the queue of {1}.", portQueue.get_field_msgid().get_int(), portQueue.get_field_port__name().get_value()));
				break;
			case extract__op:
				returnValue.append(MessageFormat.format("Operation with id {0} was extracted from the queue of {1}.", portQueue.get_field_msgid().get_int(), portQueue.get_field_port__name().get_value()));
				break;
			default:
				break;
			}
			break;
		}
		case ALT_portState: {
			final Port__State ps = choice.get_field_portState();
			String what = "";
			switch (ps.get_field_operation().enum_value) {
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
			returnValue.append(MessageFormat.format("Port {0} was {1}.", ps.get_field_port__name().get_value(), what));
			break;
		}
		case ALT_procPortSend: {
			final Proc__port__out ps = choice.get_field_procPortSend();
			final String dest;
			if (ps.get_field_compref().get_int() == TitanComponent.SYSTEM_COMPREF) {
				dest = ps.get_field_sys__name().get_value().toString();
			} else {
				dest = TitanComponent.get_component_string(ps.get_field_compref().get_int());
			}

			switch (ps.get_field_operation().enum_value) {
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

			returnValue.append(MessageFormat.format(" on {0} to {1} {2}", ps.get_field_port__name().get_value(), dest, ps.get_field_parameter().get_value()));
			break;
		}
		case ALT_procPortRecv: {
			final Proc__port__in ps = choice.get_field_procPortRecv();
			String op2 = "";
			switch (ps.get_field_operation().enum_value) {
			case call__op:
				returnValue.append(ps.get_field_check__().getValue() ? "Check-getcall" : "Getcall");
				op2 = "call";
				break;
			case reply__op:
				returnValue.append(ps.get_field_check__().getValue() ? "Check-getreply" : "Getreply");
				op2 = "reply";
				break;
			case exception__op:
				returnValue.append(ps.get_field_check__().getValue() ? "Check-catch" : "Catch");
				op2 = "exception";
			default:
				return;
			}

			final String source = TitanComponent.get_component_string(ps.get_field_compref().get_int());
			returnValue.append(MessageFormat.format(" operation on port {0} succeeded, {1} from {2}: {3} id {4}", ps.get_field_port__name().get_value(), op2, source, ps.get_field_parameter().get_value(), ps.get_field_msgid().get_int()));
			break;
		}
		case ALT_msgPortSend: {
			final Msg__port__send ms = choice.get_field_msgPortSend();
			final String dest = TitanComponent.get_component_string(ms.get_field_compref().get_int());
			returnValue.append(MessageFormat.format("Sent on {0} to {1}{2}", ms.get_field_port__name().get_value(), dest, ms.get_field_parameter().get_value()));
			break;
		}
		case ALT_msgPortRecv: {
			final Msg__port__recv ms = choice.get_field_msgPortRecv();
			switch (ms.get_field_operation().enum_value) {
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

			returnValue.append(MessageFormat.format(" operation on port {0} succeeded, message from ", ms.get_field_port__name().get_value()));
			if (ms.get_field_compref().get_int() == TitanComponent.SYSTEM_COMPREF) {
				returnValue.append(MessageFormat.format("system({0})", ms.get_field_sys__name().get_value()));
			} else {
				final String dest = TitanComponent.get_component_string(ms.get_field_compref().get_int());
				returnValue.append(dest);
			}

			returnValue.append(MessageFormat.format("{0} id {1}", ms.get_field_parameter().get_value(), ms.get_field_msgid().get_int()));
			break;
		}
		case ALT_dualMapped: {
			final Dualface__mapped dual = choice.get_field_dualMapped();
			returnValue.append(MessageFormat.format("{0} message was mapped to {1} : {2}", (dual.get_field_incoming().getValue() ? "Incoming" : "Outgoing"), dual.get_field_target__type().get_value(), dual.get_field_value__().get_value()));
			if (dual.get_field_incoming().getValue()) {
				returnValue.append(MessageFormat.format(" id {0}", dual.get_field_msgid().get_int()));
			}
			break;
		}
		case ALT_dualDiscard: {
			final Dualface__discard dual = choice.get_field_dualDiscard();
			returnValue.append(MessageFormat.format("{0} message of type {1} ", (dual.get_field_incoming().getValue() ? "Incoming" : "Outgoing"), dual.get_field_target__type().get_value()));
			if (dual.get_field_unhandled().getValue()) {
				returnValue.append(MessageFormat.format("could not be handled by the type mapping rules on port {0}.  The message was discarded.", dual.get_field_port__name().get_value()));
			} else {
				returnValue.append(MessageFormat.format(" was discarded on port {0}", dual.get_field_port__name().get_value()));
			}
			break;
		}
		case ALT_setState: {
			final Setstate setstate = choice.get_field_setState();
			returnValue.append(MessageFormat.format("The state of the {0} port was changed by a setstate operation to {1}.", setstate.get_field_port__name().get_value(), setstate.get_field_state().get_value()));
			if (setstate.get_field_info().lengthof().get_int() != 0) {
				returnValue.append(MessageFormat.format(" Information: {0}", setstate.get_field_info().get_value()));
			}
			break;
		}
		case ALT_portMisc: {
			final Port__Misc portMisc = choice.get_field_portMisc();
			final String comp_str = TitanComponent.get_component_string(portMisc.get_field_remote__component().get_int());
			switch (portMisc.get_field_reason().enum_value) {
			case removing__unterminated__connection:
				returnValue.append(MessageFormat.format("Removing unterminated connection between port {0} and {1}:{2}.", portMisc.get_field_port__name().get_value(), comp_str, portMisc.get_field_remote__port().get_value()));
				break;
			case removing__unterminated__mapping:
				returnValue.append(MessageFormat.format("Removing unterminated mapping between port {0} and system:{1}.", portMisc.get_field_port__name().get_value(), portMisc.get_field_remote__port().get_value()));
				break;
			case port__was__cleared:
				returnValue.append(MessageFormat.format("Port {0} was cleared.", portMisc.get_field_port__name().get_value()));
				break;
			case local__connection__established:
				returnValue.append(MessageFormat.format("Port {0} has established the connection with local port {1}.", portMisc.get_field_port__name().get_value(), portMisc.get_field_remote__port().get_value()));
				break;
			case local__connection__terminated:
				returnValue.append(MessageFormat.format("Port {0} has terminated the connection with local port {1}.", portMisc.get_field_port__name().get_value(), portMisc.get_field_remote__port().get_value()));
				break;
			case port__is__waiting__for__connection__tcp:
				returnValue.append(MessageFormat.format("Port {0} is waiting for connection from {1}:{2} on TCP port {3}:{4}.", portMisc.get_field_port__name().get_value(), comp_str, portMisc.get_field_remote__port().get_value(), portMisc.get_field_ip__address().get_value(), portMisc.get_field_tcp__port().get_int()));
				break;
			case port__is__waiting__for__connection__unix:
				returnValue.append(MessageFormat.format("Port {0} is waiting for connection from {1}:{2} on UNIX pathname {3}.", portMisc.get_field_port__name().get_value(), comp_str, portMisc.get_field_remote__port().get_value(), portMisc.get_field_ip__address().get_value()));
				break;
			case connection__established:
				returnValue.append(MessageFormat.format("Port {0} has established the connection with {1}:{2} using transport type {3}.", portMisc.get_field_port__name().get_value(), comp_str, portMisc.get_field_remote__port().get_value(), portMisc.get_field_ip__address().get_value()));
				break;
			case destroying__unestablished__connection:
				returnValue.append(MessageFormat.format("Destroying unestablished connection of port {0} to {1}:{2} because the other endpoint has terminated.", portMisc.get_field_port__name().get_value(), comp_str, portMisc.get_field_remote__port().get_value()));
				break;
			case terminating__connection:
				returnValue.append(MessageFormat.format("Terminating the connection of port {0} to {1}:{2}. No more messages can be sent through this connection.", portMisc.get_field_port__name().get_value(), comp_str, portMisc.get_field_remote__port().get_value()));
				break;
			case sending__termination__request__failed:
				returnValue.append(MessageFormat.format("Sending the connection termination request on port {0} to remote endpoint {1}:}{2} failed.", portMisc.get_field_port__name().get_value(), comp_str, portMisc.get_field_remote__port().get_value()));
				break;
			case termination__request__received:
				returnValue.append(MessageFormat.format("Connection termination request was received on port {0} from {1}:{2}. No more data can be sent or received through this connection.", portMisc.get_field_port__name().get_value(), comp_str, portMisc.get_field_remote__port().get_value()));
				break;
			case acknowledging__termination__request__failed:
				returnValue.append(MessageFormat.format("Sending the acknowledgment for connection termination request on port {0} to remote endpoint {1}:{2} failed.", portMisc.get_field_port__name().get_value(), comp_str, portMisc.get_field_remote__port().get_value()));
				break;
			case sending__would__block:
				returnValue.append(MessageFormat.format("Sending data on the connection of port {0} to {1}:{2} would block execution. The size of the outgoing buffer was increased from {3} to {4} bytes.", portMisc.get_field_port__name().get_value(), comp_str, portMisc.get_field_remote__port().get_value(), portMisc.get_field_tcp__port().get_int(), portMisc.get_field_new__size().get_int()));
				break;
			case connection__accepted:
				returnValue.append(MessageFormat.format("Port {0} has accepted the connection from {1}:{2}.", portMisc.get_field_port__name().get_value(), comp_str, portMisc.get_field_remote__port().get_value()));
				break;
			case connection__reset__by__peer:
				returnValue.append(MessageFormat.format("Connection of port {0} to {1}:{2} was reset by the peer.", portMisc.get_field_port__name().get_value(), comp_str, portMisc.get_field_remote__port().get_value()));
				break;
			case connection__closed__by__peer:
				returnValue.append(MessageFormat.format("Connection of port {0} to {1}:{2} was closed unexpectedly by the peer.", portMisc.get_field_port__name().get_value(), comp_str, portMisc.get_field_remote__port().get_value()));
				break;
			case port__disconnected:
				returnValue.append(MessageFormat.format("Port {0} was disconnected from {1}:{2}.", portMisc.get_field_port__name().get_value(), comp_str, portMisc.get_field_remote__port().get_value()));
				break;
			case port__was__mapped__to__system:
				returnValue.append(MessageFormat.format("Port {0} was mapped to system:{1}.", portMisc.get_field_port__name().get_value(), portMisc.get_field_remote__port().get_value()));
				break;
			case port__was__unmapped__from__system:
				returnValue.append(MessageFormat.format("Port {0} was unmapped from system:{1}.", portMisc.get_field_port__name().get_value(), portMisc.get_field_remote__port().get_value()));
				break;
			default:
				break;
			}
			break;
		}
		case UNBOUND_VALUE:
			break;
		}
	}
}
