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

	public static final TTCN_Typedescriptor TitanBoolean_descr_ = new TTCN_Typedescriptor("BOOLEAN", null);
	public static final TTCN_Typedescriptor TitanInteger_descr_ = new TTCN_Typedescriptor("INTEGER", null);
	public static final TTCN_Typedescriptor TitanFloat_descr_ = new TTCN_Typedescriptor("REAL", null);
	public static final TTCN_Typedescriptor TitanVerdicttype_descr_ = new TTCN_Typedescriptor("verdicttype", null);
	public static final TTCN_Typedescriptor TitanObjectid_descr_ = new TTCN_Typedescriptor("OBJECT IDENTIFIER", null);
	public static final TTCN_Typedescriptor TitanBitstring_descr_ = new TTCN_Typedescriptor("BIT STRING", null);
	public static final TTCN_Typedescriptor TitanHexstring_descr_ = new TTCN_Typedescriptor("hexstring", null);
	public static final TTCN_Typedescriptor TitanOctetstring_descr_ = new TTCN_Typedescriptor("OCTET STRING", null);
	public static final TTCN_Typedescriptor TitanCharstring_descr_ = new TTCN_Typedescriptor("charstring", null);
	public static final TTCN_Typedescriptor TitanUniversalCharString_descr_ = new TTCN_Typedescriptor("universal charstring", null);
	public static final TTCN_Typedescriptor TitanComponent_descr_ = new TTCN_Typedescriptor("component", null);
	public static final TTCN_Typedescriptor TitanDefault_descr_ = new TTCN_Typedescriptor("default", null);
	public static final TTCN_Typedescriptor TitanAsn_Null_descr_ = new TTCN_Typedescriptor("NULL", null);
	public static final TTCN_Typedescriptor TitanAsn_Any_descr_ = new TTCN_Typedescriptor("ANY", null);
	public static final TTCN_Typedescriptor TitanExternal_descr_ = new TTCN_Typedescriptor("EXTERNAL", null);
	public static final TTCN_Typedescriptor TitanEmbedded_PDV_descr_ = new TTCN_Typedescriptor("EMBEDDED PDV", null);
	public static final TTCN_Typedescriptor TitanCharacter_String_descr_ = new TTCN_Typedescriptor("CHARACTER STRING", null);
	public static final TTCN_Typedescriptor TitanObjectDescriptor_descr_ = new TTCN_Typedescriptor("ObjectDescriptor", null);
	public static final TTCN_Typedescriptor TitanUTF8String_descr_ = new TTCN_Typedescriptor("UTF8String", null);
	public static final TTCN_Typedescriptor TitanAsn_Roid_descr_ = new TTCN_Typedescriptor("RELATIVE-OID", null);
	public static final TTCN_Typedescriptor TitanNumericString_descr_ = new TTCN_Typedescriptor("NumericString", null);
	public static final TTCN_Typedescriptor TitanPrintableString_descr_ = new TTCN_Typedescriptor("PrintableString", null);
	public static final TTCN_Typedescriptor TitanTeletexString_descr_ = new TTCN_Typedescriptor("TeletexString", null);
	public static final TTCN_Typedescriptor TitanT61String_descr_ = TitanTeletexString_descr_;
	public static final TTCN_Typedescriptor TitanVideotexString_descr_ = new TTCN_Typedescriptor("VideotexString", null);
	public static final TTCN_Typedescriptor TitanIA5String_descr_ = new TTCN_Typedescriptor("IA5String", null);
	public static final TTCN_Typedescriptor TitanASN_GeneralizedTime_descr_ = new TTCN_Typedescriptor("GeneralizedTime", null);
	public static final TTCN_Typedescriptor TitanASN_UTCTime_descr_ = new TTCN_Typedescriptor("UTCTime", null);
	public static final TTCN_Typedescriptor TitanGraphicString_descr_ = new TTCN_Typedescriptor("GraphicString", null);
	public static final TTCN_Typedescriptor TitanVisibleString_descr_ = new TTCN_Typedescriptor("VisibleString", null);
	public static final TTCN_Typedescriptor TitanISO646String_descr_ = TitanVisibleString_descr_;
	public static final TTCN_Typedescriptor TitanGeneralString_descr_ = new TTCN_Typedescriptor("GeneralString", null);
	public static final TTCN_Typedescriptor TitanUniversalString_descr_ = new TTCN_Typedescriptor("UniversalString", null);
	public static final TTCN_Typedescriptor TitanBMPString_descr_ = new TTCN_Typedescriptor("BMPString", null);

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
