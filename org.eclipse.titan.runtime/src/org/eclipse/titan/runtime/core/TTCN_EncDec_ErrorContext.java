/******************************************************************************
 * Copyright (c) 2000-2017 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

/** @brief Error context
 *
 *  Maintains a list of linked TTCN_EncDec_ErrorContext objects
 *
 * @author Gergo Ujhelyi
 * @author Kristof Szabados
 */

import java.util.ArrayList;

/**
 * This class represents an error context.
 * When an error is encountered during encoding/decoding this allows us to write the stack of operations.
 * On each level in this hierarchy a context starts when it's object is created,
 *  and ends when the leaveContext function is called.
 * 
 * Please note the constructors and leaveContext function has to be called in pairs.
 * */
public class TTCN_EncDec_ErrorContext {

	private static ArrayList<TTCN_EncDec_ErrorContext> errors = new ArrayList<TTCN_EncDec_ErrorContext>();
	private String format;
	private Object[] arguments;

	public TTCN_EncDec_ErrorContext() {
		errors.add(this);
	}

	public TTCN_EncDec_ErrorContext(final String fmt, final Object... args) {
		format = fmt;
		arguments = args;
		errors.add(this);
	}

	public void leaveContext() {
		if (errors.get(errors.size() - 1) != this) {
			throw new TtcnError(" Internal error: TTCN_EncDec_ErrorContext.leaveContext()");
		}
		errors.remove(errors.size() - 1);
	}

	public void set_msg(final String fmt, final Object... args) {
		format = fmt;
		arguments = args;
	}

	public static void error(final TTCN_EncDec.error_type p_et, final String fmt, final Object... args) {
		final StringBuilder err_msg = new StringBuilder();
		for (int i = 0; i < errors.size(); i++) {
			final TTCN_EncDec_ErrorContext temp = errors.get(i);
			err_msg.append(String.format(temp.format, temp.arguments)).append(' ');
		}

		err_msg.append(String.format(fmt, args));
		TTCN_EncDec.error(p_et, err_msg.toString());
	}

	public static void error_internal(final String fmt, final Object... args) {
		final StringBuilder err_msg = new StringBuilder("Internal error: ");
		for (int i = 0; i < errors.size(); i++) {
			final TTCN_EncDec_ErrorContext temp = errors.get(i);
			err_msg.append(String.format(temp.format, temp.arguments)).append(' ');
		}

		err_msg.append(String.format(fmt, args));
		TTCN_EncDec.error(TTCN_EncDec.error_type.ET_INTERNAL, err_msg.toString());
		throw new TtcnError(TTCN_EncDec.get_error_str());
	}

	public void warning(final String fmt, final Object... args) {
		final StringBuilder warn_msg = new StringBuilder();
		for (int i = 0; i < errors.size(); i++) {
			final TTCN_EncDec_ErrorContext temp = errors.get(i);
			warn_msg.append(String.format(temp.format, temp.arguments)).append(' ');
		}

		warn_msg.append(String.format(fmt, args));
		TtcnError.TtcnWarning(warn_msg.toString());
	}
}
