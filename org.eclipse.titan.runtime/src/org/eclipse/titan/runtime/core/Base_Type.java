/******************************************************************************
 * Copyright (c) 2000-2017 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

import org.eclipse.titan.runtime.core.TtcnLogger.Severity;

/**
 * TTCN-3 boolean
 * @author Kristof Szabados
 */
public abstract class Base_Type {

	public abstract boolean isPresent();
	public abstract boolean isBound();

	public boolean isValue() {
		return isBound();
	}

	public boolean isOptional() {
		return false;
	}

	public abstract boolean operatorEquals(final Base_Type otherValue);

	public abstract Base_Type assign( final Base_Type otherValue );
	public abstract void log();

	public void set_param (final Module_Parameter param) {
		// TODO once the setting module parameters is implemented for all classes this function should become abstract
		TtcnLogger.begin_event(Severity.ERROR_UNQUALIFIED);
		TtcnLogger.log_event_str( "//TODO: " );
		TtcnLogger.log_event_str( getClass().getSimpleName() );
		TtcnLogger.log_event_str( ".set_param() is not yet implemented!\n" );
		TtcnLogger.end_event();
	}

	public abstract void encode_text(final Text_Buf text_buf);

	public abstract void decode_text(final Text_Buf text_buf);
}
