/******************************************************************************
 * Copyright (c) 2000-2017 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

import java.text.MessageFormat;

import org.eclipse.titan.runtime.core.RAW.RAW_enc_tree;
import org.eclipse.titan.runtime.core.RAW.TTCN_RAWdescriptor;
import org.eclipse.titan.runtime.core.TTCN_EncDec.raw_order_t;
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
		public final TTCN_Typedescriptor oftype_descr;

		public TTCN_Typedescriptor(final String name, final TTCN_RAWdescriptor raw, final TTCN_Typedescriptor oftype_descr) {
			this.name = name;
			this.raw = raw;
			this.oftype_descr = oftype_descr;
		}
	}

	public static final TTCN_Typedescriptor TitanBoolean_descr_ = new TTCN_Typedescriptor("BOOLEAN", RAW.TitanBoolean_raw_, null);
	public static final TTCN_Typedescriptor TitanInteger_descr_ = new TTCN_Typedescriptor("INTEGER", RAW.TitanInteger_raw_, null);
	public static final TTCN_Typedescriptor TitanFloat_descr_ = new TTCN_Typedescriptor("REAL", RAW.TitanFloat_raw_, null);
	public static final TTCN_Typedescriptor TitanVerdictType_descr_ = new TTCN_Typedescriptor("verdicttype", null, null);
	public static final TTCN_Typedescriptor TitanObjectid_descr_ = new TTCN_Typedescriptor("OBJECT IDENTIFIER", null, null);
	public static final TTCN_Typedescriptor TitanBitString_descr_ = new TTCN_Typedescriptor("BIT STRING", RAW.TitanBitString_raw_, null);
	public static final TTCN_Typedescriptor TitanHexString_descr_ = new TTCN_Typedescriptor("hexstring", RAW.TitanHexString_raw_, null);
	public static final TTCN_Typedescriptor TitanOctetString_descr_ = new TTCN_Typedescriptor("OCTET STRING", RAW.TitanOctetString_raw_, null);
	public static final TTCN_Typedescriptor TitanCharString_descr_ = new TTCN_Typedescriptor("charstring", RAW.TitanCharString_raw_, null);
	public static final TTCN_Typedescriptor TitanUniversalCharString_descr_ = new TTCN_Typedescriptor("universal charstring", RAW.TitanUniversalCharString_raw_, null);
	public static final TTCN_Typedescriptor TitanComponent_descr_ = new TTCN_Typedescriptor("component", null, null);
	public static final TTCN_Typedescriptor TitanDefault_descr_ = new TTCN_Typedescriptor("default", null, null);
	public static final TTCN_Typedescriptor TitanAsn_Null_descr_ = new TTCN_Typedescriptor("NULL", null, null);
	public static final TTCN_Typedescriptor TitanAsn_Any_descr_ = new TTCN_Typedescriptor("ANY", null, null);
	public static final TTCN_Typedescriptor TitanExternal_descr_ = new TTCN_Typedescriptor("EXTERNAL", null, null);
	public static final TTCN_Typedescriptor TitanEmbedded_PDV_descr_ = new TTCN_Typedescriptor("EMBEDDED PDV", null, null);
	public static final TTCN_Typedescriptor TitanCharacter_String_descr_ = new TTCN_Typedescriptor("CHARACTER STRING", null, null);
	public static final TTCN_Typedescriptor TitanObjectDescriptor_descr_ = new TTCN_Typedescriptor("ObjectDescriptor", null, null);
	public static final TTCN_Typedescriptor TitanUTF8String_descr_ = new TTCN_Typedescriptor("UTF8String", null, null);
	public static final TTCN_Typedescriptor TitanAsn_Roid_descr_ = new TTCN_Typedescriptor("RELATIVE-OID", null, null);
	public static final TTCN_Typedescriptor TitanNumericString_descr_ = new TTCN_Typedescriptor("NumericString", null, null);
	public static final TTCN_Typedescriptor TitanPrintableString_descr_ = new TTCN_Typedescriptor("PrintableString", null, null);
	public static final TTCN_Typedescriptor TitanTeletexString_descr_ = new TTCN_Typedescriptor("TeletexString", null, null);
	public static final TTCN_Typedescriptor TitanT61String_descr_ = TitanTeletexString_descr_;
	public static final TTCN_Typedescriptor TitanVideotexString_descr_ = new TTCN_Typedescriptor("VideotexString", null, null);
	public static final TTCN_Typedescriptor TitanIA5String_descr_ = new TTCN_Typedescriptor("IA5String", null, null);
	public static final TTCN_Typedescriptor TitanASN_GeneralizedTime_descr_ = new TTCN_Typedescriptor("GeneralizedTime", null, null);
	public static final TTCN_Typedescriptor TitanASN_UTCTime_descr_ = new TTCN_Typedescriptor("UTCTime", null, null);
	public static final TTCN_Typedescriptor TitanGraphicString_descr_ = new TTCN_Typedescriptor("GraphicString", null, null);
	public static final TTCN_Typedescriptor TitanVisibleString_descr_ = new TTCN_Typedescriptor("VisibleString", null, null);
	public static final TTCN_Typedescriptor TitanISO646String_descr_ = TitanVisibleString_descr_;
	public static final TTCN_Typedescriptor TitanGeneralString_descr_ = new TTCN_Typedescriptor("GeneralString", null, null);
	public static final TTCN_Typedescriptor TitanUniversalString_descr_ = new TTCN_Typedescriptor("UniversalString", null, null);
	public static final TTCN_Typedescriptor TitanBMPString_descr_ = new TTCN_Typedescriptor("BMPString", null, null);

	public abstract boolean isPresent();
	public abstract boolean isBound();

	public boolean isValue() {
		return isBound();
	}

	public boolean isOptional() {
		return false;
	}

	public abstract boolean operatorEquals(final Base_Type otherValue);

	public abstract Base_Type assign(final Base_Type otherValue);

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

	/**
	 * Encode with RAW coding.
	 *
	 * @param p_td
	 *                type descriptor
	 * @param myleaf
	 *                filled with RAW encoding data
	 * @return the length of the encoding
	 *
	 * @throws TtcnError
	 *                 in case of not being implemented
	 * */
	public int RAW_encode(final TTCN_Typedescriptor p_td, final RAW_enc_tree myleaf) {
		throw new TtcnError(MessageFormat.format("RAW encoding requested for type '{0}'' which has no RAW encoding method.", p_td.name));
	}

	/**
	 * Decode with RAW coding
	 *
	 * @param p_td
	 *                type descriptor
	 * @param p_buf
	 *                buffer with data to be decoded
	 * @param limit
	 *                number of bits the decoder is allowed to use. At the
	 *                top level this is 8x the number of bytes in the
	 *                buffer.
	 * @param top_bit_ord
	 *                (LSB/MSB) from TTCN_RAWdescriptor_t::top_bit_order
	 *
	 * @throws TtcnError
	 *                 in case of not being implemented
	 * */
	public int RAW_decode(final TTCN_Typedescriptor p_td, final TTCN_Buffer buff, final int limit, final raw_order_t top_bit_ord) {
		return RAW_decode(p_td, buff, limit, top_bit_ord, false, -1, true);
	}

	/**
	 * Decode with RAW coding
	 *
	 * @param p_td
	 *                type descriptor
	 * @param p_buf
	 *                buffer with data to be decoded
	 * @param limit
	 *                number of bits the decoder is allowed to use. At the
	 *                top level this is 8x the number of bytes in the
	 *                buffer.
	 * @param top_bit_ord
	 *                (LSB/MSB) from TTCN_RAWdescriptor_t::top_bit_order
	 * @param no_err
	 *                set to TRUE if the decoder is to return errors
	 *                silently, without calling
	 *                TTCN_EncDec_ErrorContext::error
	 * @param sel_field
	 *                selected field indicator for CROSSTAG, or -1
	 * @param first_call
	 *                default TRUE. May be FALSE for a REPEATABLE record-of
	 *                inside a set, if an element has been successfully
	 *                decoded.
	 * @return length of decoded field, or a negative number for error
	 *
	 * @throws TtcnError
	 *                 in case of not being implemented
	 * */
	public int RAW_decode(final TTCN_Typedescriptor p_td, final TTCN_Buffer buff, int limit, final raw_order_t top_bit_ord, final boolean no_err, int sel_field, final boolean first_call) {
		throw new TtcnError(MessageFormat.format("RAW decoding requested for type '{0}'' which has no RAW encoding method.", p_td.name));
	}
}
