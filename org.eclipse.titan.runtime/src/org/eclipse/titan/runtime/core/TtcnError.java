/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.eclipse.titan.runtime.core.TtcnLogger.Severity;
import org.eclipse.titan.runtime.core.TtcnLogger.TTCN_Location;
import org.eclipse.titan.runtime.core.TtcnLogger.log_event_types_t;

/**
 * This class is used to report a Dynamic Test Case error to upper parts of the system.
 * 
 * Please note that in the compiler the constructor creates a new exception,.
 * Here this class itself is used as the exception.
 *
 * Please also note, that these functions don't accept variadic parameters.
 * */
//TODO: maybe change it to exception
public class TtcnError extends Error {

	// if the error is reported in a try-catch clause this member stores the error text
	private String errorMessage;

	public TtcnError( final String errorMessage ) {
		if (TTCN_Runtime.is_in_ttcn_try_block()) {
			final StringBuilder error_str = TTCN_Location.print_location(TtcnLogger.get_source_info_format() == TtcnLogger.source_info_format_t.SINFO_STACK, TtcnLogger.get_source_info_format() != TtcnLogger.source_info_format_t.SINFO_NONE, TtcnLogger.get_log_entity_name());
			if (error_str.length() > 0) {
				error_str.append(' ');
			}

			error_str.append("Dynamic testcase error: ");
			error_str.append(errorMessage);
			this.errorMessage = error_str.toString();
		} else {
			TtcnLogger.begin_event(Severity.ERROR_UNQUALIFIED);
			if (TtcnLogger.get_source_info_format() == TtcnLogger.source_info_format_t.SINFO_NONE) {
				final StringBuilder loc = TTCN_Location.print_location(false, true, log_event_types_t.LOGEVENTTYPES_NO);
				if (loc.length() > 0) {
					TtcnLogger.log_event_str(loc.toString());
					TtcnLogger.log_event_str(": ");
				}
			}
			TtcnLogger.log_event_str("Dynamic test case error: ");
			TtcnLogger.log_event_str(errorMessage);
			TtcnLogger.end_event();
	
			TTCN_Runtime.set_error_verdict();
			TtcnLogger.log_executor_runtime(TitanLoggerApi.ExecutorRuntime_reason.enum_type.performing__error__recovery);
		}
	}

	/**
	 * New function to catch exceptions coming from the external world.
	 *
	 * @param exception the exception
	 * */
	public TtcnError( final Exception exception ) {
		if (TTCN_Runtime.is_in_ttcn_try_block()) {
			final StringBuilder error_str = TTCN_Location.print_location(TtcnLogger.get_source_info_format() == TtcnLogger.source_info_format_t.SINFO_STACK, TtcnLogger.get_source_info_format() != TtcnLogger.source_info_format_t.SINFO_NONE, TtcnLogger.get_log_entity_name());
			if (error_str.length() > 0) {
				error_str.append(' ');
			}

			final StringWriter error = new StringWriter();
			exception.printStackTrace(new PrintWriter(error));

			error_str.append("Dynamic testcase error: ");
			error_str.append(error.toString());

			this.errorMessage = error_str.toString();
		} else {
			final StringWriter error = new StringWriter();
			exception.printStackTrace(new PrintWriter(error));
	
			TtcnLogger.begin_event(Severity.ERROR_UNQUALIFIED);
			TtcnLogger.log_event_str("Dynamic test case error: ");
			TtcnLogger.log_event_str(error.toString());
			TtcnLogger.end_event();
	
			TTCN_Runtime.set_error_verdict();
			TtcnLogger.log_executor_runtime(TitanLoggerApi.ExecutorRuntime_reason.enum_type.performing__error__recovery);
		}
	}

	/**
	 * If the Dynamic Testcase Error happens in a try-catch block
	 * this function will return the stored error text.
	 *
	 * @return the error text if thrown in try-catch, null otherwise.
	 * */
	public String get_message() {
		return errorMessage;
	}

	// FIXME this function is not ok here.
	// As soon as we have implemented all functions from the old core it should be moved.
	// Till then similarity with the old structure rules.
	public static void TtcnWarning(final String warningMessage) {
		TtcnLogger.begin_event(Severity.WARNING_UNQUALIFIED);
		TtcnLogger.log_event_str("Warning: ");
		TtcnLogger.log_event_str(warningMessage);
		TtcnLogger.end_event();
	}

	public static void TtcnErrorBegin(final String errorMessage) {
		if (TTCN_Runtime.is_in_ttcn_try_block()) {
			TtcnLogger.begin_event_log2str();

			final StringBuilder loc = TTCN_Location.print_location(TtcnLogger.get_source_info_format() == TtcnLogger.source_info_format_t.SINFO_STACK, TtcnLogger.get_source_info_format() != TtcnLogger.source_info_format_t.SINFO_NONE, TtcnLogger.get_log_entity_name());
			if (loc.length() > 0) {
				TtcnLogger.log_event_str(loc.toString());
				TtcnLogger.log_event_str(" ");
			}

			TtcnLogger.log_event_str("Dynamic test case error: ");
			TtcnLogger.log_event_str(errorMessage);
		} else {
			TtcnLogger.begin_event(Severity.ERROR_UNQUALIFIED);
			TtcnLogger.log_event_str("Dynamic test case error: ");
			TtcnLogger.log_event_str(errorMessage);
		}
	}

	public static void TtcnErrorEnd() {
		if (TTCN_Runtime.is_in_ttcn_try_block()) {
			final TitanCharString error_str = TtcnLogger.end_event_log2str();

			throw new TtcnError(error_str.getValue().toString());
		} else {
			TtcnLogger.end_event();
			TTCN_Runtime.set_error_verdict();
			TtcnLogger.log_executor_runtime(TitanLoggerApi.ExecutorRuntime_reason.enum_type.performing__error__recovery);
		}

	}

	public static void TtcnWarningBegin(final String errorMessage) {
		TtcnLogger.begin_event(Severity.WARNING_UNQUALIFIED);
		TtcnLogger.log_event_str("Warning: ");
		TtcnLogger.log_event_str(errorMessage);
	}

	public static void TtcnWarningEnd() {
		TtcnLogger.end_event();
	}
}
