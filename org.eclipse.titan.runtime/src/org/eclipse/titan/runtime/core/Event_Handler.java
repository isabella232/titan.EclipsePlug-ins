/******************************************************************************
 * Copyright (c) 2000-2017 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

import java.nio.channels.SelectableChannel;

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
		//FIXME implement rest
		public abstract void Handle_Event(final SelectableChannel channel, final boolean is_readable, final boolean is_writeable);
	}
}
