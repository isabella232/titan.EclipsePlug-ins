/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

import java.nio.channels.SelectableChannel;
import java.util.List;

/**
 * Internal classes to help working with events.
 * The classes are meant for internal use, not for user code.
 * These classes are used as base classes for class PORT.
 *
 * @author Kristof Szabados
 *
 * FIXME not yet complete
 */
public class Event_Handler {

	/**
	 * generic event handler base class so that PORTs and internal connection could use the same mechanisms.
	 * 
	 * Fd_And_Timeout_Event_Handler in the compiler.
	 * */
	static abstract class Channel_And_Timeout_Event_Handler {
		public List<Channel_And_Timeout_Event_Handler> list;

		public double callIntervall;
		public double last_called;
		public boolean isTimeout;
		public boolean callAnyway;
		public boolean isPeriodic;

		//FIXME implement rest
		public abstract void Handle_Event(final SelectableChannel channel, final boolean is_readable, final boolean is_writeable);

		public abstract void Handle_Timeout(final double time_since_last_call);
	}
}
