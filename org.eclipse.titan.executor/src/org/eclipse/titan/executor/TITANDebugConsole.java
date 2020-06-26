/******************************************************************************
 * Copyright (c) 2000-2020 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.executor;

import java.io.IOException;

import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

/**
 * @author Kristof Szabados
 * */
public final class TITANDebugConsole {
	private static final String TITLE = "TITAN RUNTIME Debug console";
	private static MessageConsole console = null;
	private static boolean inHeadLessMode;

	static {
		inHeadLessMode = !PlatformUI.isWorkbenchRunning();
	}

	private TITANDebugConsole() {
	}

	public static synchronized MessageConsole getConsole() {
		if (null == console) {
			console = new MessageConsole(TITLE, null);
			console.activate();
			ConsolePlugin.getDefault().getConsoleManager().addConsoles(new IConsole[] {console});
		}
		return console;
	}

	public static void println(final String message, final MessageConsoleStream stream) {
		if(inHeadLessMode) {
			return;
		}
		stream.println(message);
		try {
			stream.flush();
		} catch (IOException e) {
			ErrorReporter.logExceptionStackTrace(e);
		}
	}

	// It creates a MessageStream just for this println
	public static void println(final String message) {
		if(inHeadLessMode) {
			return;
		}

		println(message, getConsole().newMessageStream());
	}

	public static void print(final String message, final MessageConsoleStream stream) {
		if(inHeadLessMode) {
			return;
		}

		stream.print(message);
	}

	// It creates a MessageStream just for this println
	public static void print(final String message) {
		if(inHeadLessMode) {
			return;
		}

		print(message, getConsole().newMessageStream());
	}

	public static void println(final StringBuilder message) {
		if(inHeadLessMode) {
			return;
		}

		println(message.toString(), getConsole().newMessageStream());
	}
}
