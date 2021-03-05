/******************************************************************************
 * Copyright (c) 2000-2021 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core.cfgparser;

import java.text.MessageFormat;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.eclipse.titan.runtime.core.TTCN_Logger;

public class CFGListener extends BaseErrorListener {
	private String filename;
	private boolean encounteredError;

	public CFGListener(final String filename) {
		this.filename = filename;
		encounteredError = false;
	}

	public void setFilename(final String filename) {
		this.filename = filename;
	}

	public boolean encounteredError() {
		return encounteredError;
	}

	@Override
	public void syntaxError(final Recognizer<?, ?> recognizer, final Object offendingSymbol, final int line, final int charPositionInLine,
			final String msg, final RecognitionException e) {
		encounteredError = true;

		TTCN_Logger.begin_event(TTCN_Logger.Severity.ERROR_UNQUALIFIED);
		if (filename == null) {
			TTCN_Logger.log_event_str(MessageFormat.format("Parse error while reading configuration information: in line {0}: {1}", line, msg));
		} else {
			TTCN_Logger.log_event_str(MessageFormat.format("Parse error in configuration file `{0}'': in line {1}: {2}", filename, line, msg));
		}
		TTCN_Logger.end_event();
	}

}
