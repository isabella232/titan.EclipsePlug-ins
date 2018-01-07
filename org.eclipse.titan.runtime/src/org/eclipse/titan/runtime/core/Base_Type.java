/******************************************************************************
 * Copyright (c) 2000-2017 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

import org.eclipse.titan.runtime.core.RAW.TTCN_RAWdescriptor;
import org.eclipse.titan.runtime.core.TtcnLogger.Severity;

/**
 * The base class of all types.
 *
 * @author Kristof Szabados
 */
public abstract class Base_Type {

	/**
	 * There is one type descriptor object for each type. Descriptors for
	 * built-in types are supplied by the runtime. Descriptors for
	 * user-defined types are written by the compiler in the generated code.
	 * 
	 *TODO: currently no encoding/decoding is supported yet!
	 * */
	public static class TTCN_Typedescriptor {
		// name of the type
		public final String name;
		// information for RAW coding
		public final TTCN_RAWdescriptor raw;
		//FIXME the other encoding specific descriptors

		public TTCN_Typedescriptor(final String name, final TTCN_RAWdescriptor raw) {
			this.name = name;
			this.raw = raw;
		}
	}

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

	// originally flavour is not used for all encodings.
	public void encode(final TTCN_Typedescriptor p_td, final TTCN_Buffer p_buf, final TTCN_EncDec.coding_type p_coding, final int flavour) {
		// TODO once encoding is implemented for all classes this function should become abstract
		TtcnLogger.begin_event(Severity.ERROR_UNQUALIFIED);
		TtcnLogger.log_event_str( "//TODO: " );
		TtcnLogger.log_event_str( getClass().getSimpleName() );
		TtcnLogger.log_event_str( ".encode() is not yet implemented!\n" );
		TtcnLogger.end_event();
	}

	// originally flavour is not used for all encodings.
	public void decode(final TTCN_Typedescriptor p_td, final TTCN_Buffer p_buf, final TTCN_EncDec.coding_type p_coding, final int flavour) {
		// TODO once decoding is implemented for all classes this function should become abstract
		TtcnLogger.begin_event(Severity.ERROR_UNQUALIFIED);
		TtcnLogger.log_event_str( "//TODO: " );
		TtcnLogger.log_event_str( getClass().getSimpleName() );
		TtcnLogger.log_event_str( ".decode() is not yet implemented!\n" );
		TtcnLogger.end_event();
	}
}
