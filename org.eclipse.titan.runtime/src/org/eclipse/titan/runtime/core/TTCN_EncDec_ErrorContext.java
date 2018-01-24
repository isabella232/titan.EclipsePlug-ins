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
 */

import java.util.LinkedList;
import java.util.List;

//FIXME implement destructor
public class TTCN_EncDec_ErrorContext {

	private static List<TTCN_EncDec_ErrorContext> errors = new LinkedList<TTCN_EncDec_ErrorContext>();
	private String msg;

	public static boolean head;

	public TTCN_EncDec_ErrorContext() {
		msg = null;
		if(!head) {
			errors.add(0, this);
			head = true;
		} else {
			errors.add(this);
		}
	}

	public TTCN_EncDec_ErrorContext(final String fmt, final Object... args) {
		msg = String.format(fmt, args);
		if(!head) {
			errors.add(0, this);
		} else {
			errors.add(this);
		}
	}

	public void set_msg(final String fmt, final Object... args) {
		msg = String.format(fmt, args);
	}

	public static void error(final TTCN_EncDec.error_type p_et, final String fmt, final Object... args) {
		String err_msg = "";
		for (int i = 0; i < errors.size(); i++) {
			//FIXME: Initial implement
			err_msg = errors.get(i).msg + " ";
		}
		err_msg = err_msg + String.format(fmt, args);
		TTCN_EncDec.error(p_et, err_msg);
	}

	public static void error_internal(final String fmt, final Object... args) {
		String err_msg = "Internal error: ";
		for (int i = 0; i < errors.size(); i++) {
			//FIXME: Initial implement
			err_msg = errors.get(i).msg + " ";
		}
		err_msg = err_msg + String.format(fmt, args);
		TTCN_EncDec.error(TTCN_EncDec.error_type.ET_INTERNAL, err_msg);
		throw new TtcnError(TTCN_EncDec.get_error_str());
	}

	public void warning(final String fmt, final Object... args) {
		String warn_msg = null;
		for (int i = 0; i < errors.size(); i++) {
			//FIXME: Initial implement
			warn_msg = errors.get(i).msg + " ";
		}
		warn_msg = String.format(fmt, args);
		TtcnError.TtcnWarning(warn_msg);
	}
}
