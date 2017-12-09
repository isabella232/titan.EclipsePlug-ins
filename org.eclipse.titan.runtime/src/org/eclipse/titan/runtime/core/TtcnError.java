/******************************************************************************
 * Copyright (c) 2000-2017 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

import org.eclipse.titan.runtime.core.TtcnLogger.Severity;

//TODO: maybe change it to exception
public class TtcnError extends Error {

	public TtcnError( final String aErrorMessage ) {
		//FIXME implement
		super( aErrorMessage );

	}

	// FIXME this function is not ok here.
	// As soon as we have implemented all functions from the old core it should be moved.
	// Till then similarity with the old structure rules.
	public static void TtcnWarning(final String aWarningMessage) {
		//FIXME implement logging of warnings
		System.out.println("warning: " + aWarningMessage);
	}

	//FIXME comment
	public static void TtcnErrorBegin(final String errorMessage) {
		//FIXME implement
		TtcnLogger.begin_event(Severity.ERROR_UNQUALIFIED);
		TtcnLogger.log_event_str("Dynamic test case error: ");
		TtcnLogger.log_event_str(errorMessage);
	}

	//FIXME comment
	public static void TtcnErrorEnd() {
		if (TTCN_Runtime.is_in_ttcn_try_block()) {
			final TitanCharString error_str = TtcnLogger.end_event_log2str();
			throw new TtcnError(error_str.getValue().toString());
		} else {
			//FIXME implement
			TtcnLogger.end_event();
		}
	}

	//FIXME comment
	public static void TtcnWarningBegin(final String errorMessage) {
		//FIXME implement
		TtcnLogger.begin_event(Severity.WARNING_UNQUALIFIED);
		TtcnLogger.log_event_str("Warning: ");
		TtcnLogger.log_event_str(errorMessage);
	}

	//FIXME comment
	public static void TtcnWarningEnd() {
		TtcnLogger.end_event();
	}
}
