package org.eclipse.titan.runtime.core.cfgparser;

import java.text.MessageFormat;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.misc.Nullable;
import org.eclipse.titan.runtime.core.TTCN_Logger;

public class CFGListener extends BaseErrorListener {
	private final String filename;
	private boolean encounteredError;

	public CFGListener(final String filename) {
		this.filename = filename;
		encounteredError = false;
	}

	public boolean encounteredError() {
		return encounteredError;
	}

	@Override
	public void syntaxError(@NotNull final Recognizer<?, ?> recognizer, @Nullable final Object offendingSymbol, final int line, final int charPositionInLine,
			@NotNull final String msg, @Nullable final RecognitionException e) {
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
