/******************************************************************************
 * Copyright (c) 2000-2019 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

import java.text.MessageFormat;

import org.eclipse.titan.runtime.core.JSON.TTCN_JSONdescriptor;
import org.eclipse.titan.runtime.core.Param_Types.Module_Param_Name;
import org.eclipse.titan.runtime.core.Param_Types.Module_Parameter;
import org.eclipse.titan.runtime.core.RAW.RAW_Force_Omit;
import org.eclipse.titan.runtime.core.RAW.RAW_enc_tree;
import org.eclipse.titan.runtime.core.RAW.TTCN_RAWdescriptor;
import org.eclipse.titan.runtime.core.TTCN_EncDec.raw_order_t;
import org.eclipse.titan.runtime.core.TTCN_Logger.Severity;

/**
 * The base class of all types.
 *
 * @author Kristof Szabados
 * @author Arpad Lovassy
 */
public abstract class Base_Type {

	/**
	 * There is one type descriptor object for each type. Descriptors for
	 * built-in types are supplied by the runtime. Descriptors for
	 * user-defined types are written by the compiler in the generated code.
	 *
	 *TODO: currently only RAW encoding/decoding is supported!
	 * */
	public static class TTCN_Typedescriptor {
		// name of the type
		public final String name;
		// information for RAW coding
		public final TTCN_RAWdescriptor raw;
		public final TTCN_JSONdescriptor json;
		//FIXME the other encoding specific descriptors
		/**
		 * In case of record of/ set of type the descriptor of the "of type".
		 * Can not be final as it will be set in the preInit phase
		 * instead of the static module initialization phase when the descriptor is created.
		 * */
		public TTCN_Typedescriptor oftype_descr;

		/**
		 * Initializes the type descriptor.
		 *
		 * @param name
		 *                the name of the type in its generated, unique
		 *                form.
		 * @param raw
		 *                the RAW descriptor if the type has one,
		 *                {@code null} otherwise.
		 * @param oftype_descr
		 *                in case of record of and set of types, the
		 *                type descriptor of the of type, {@code false}
		 *                otherwise.
		 * */
		public TTCN_Typedescriptor(final String name,  final TTCN_RAWdescriptor raw, final TTCN_JSONdescriptor json, final TTCN_Typedescriptor oftype_descr) {
			this.name = name;
			this.raw = raw;
			this.json = json;
			this.oftype_descr = oftype_descr;
		}

		/**
		 * Initializes the type descriptor.
		 *
		 * @param name
		 *                the name of the type in its generated, unique
		 *                form.
		 * @param raw
		 *                the RAW descriptor if the type has one,
		 *                {@code null} otherwise.
		 * */
		public TTCN_Typedescriptor(final String name,  final TTCN_RAWdescriptor raw, final TTCN_JSONdescriptor json) {
			this.name = name;
			this.raw = raw;
			this.json = json;
			this.oftype_descr = null;
		}
	}

	public static final TTCN_Typedescriptor TitanBoolean_descr_ = new TTCN_Typedescriptor("BOOLEAN", RAW.TitanBoolean_raw_, JSON.TitanBoolean_json_, null);
	public static final TTCN_Typedescriptor TitanInteger_descr_ = new TTCN_Typedescriptor("INTEGER", RAW.TitanInteger_raw_, JSON.TitanInteger_json_, null);
	public static final TTCN_Typedescriptor TitanFloat_descr_ = new TTCN_Typedescriptor("REAL", RAW.TitanFloat_raw_, JSON.TitanFloat_json_, null);
	public static final TTCN_Typedescriptor TitanVerdictType_descr_ = new TTCN_Typedescriptor("verdicttype", null, null, null);
	public static final TTCN_Typedescriptor TitanObjectid_descr_ = new TTCN_Typedescriptor("OBJECT IDENTIFIER", null, null, null);
	public static final TTCN_Typedescriptor TitanBitString_descr_ = new TTCN_Typedescriptor("BIT STRING", RAW.TitanBitString_raw_, JSON.TitanBitString_json_, null);
	public static final TTCN_Typedescriptor TitanHexString_descr_ = new TTCN_Typedescriptor("hexstring", RAW.TitanHexString_raw_, JSON.TitanHexString_json_, null);
	public static final TTCN_Typedescriptor TitanOctetString_descr_ = new TTCN_Typedescriptor("OCTET STRING", RAW.TitanOctetString_raw_, JSON.TitanOctetString_json_, null);
	public static final TTCN_Typedescriptor TitanCharString_descr_ = new TTCN_Typedescriptor("charstring", RAW.TitanCharString_raw_, JSON.TitanCharString_json_, null);
	public static final TTCN_Typedescriptor TitanUniversalCharString_descr_ = new TTCN_Typedescriptor("universal charstring", RAW.TitanUniversalCharString_raw_, JSON.TitanUniversalCharString_json_, null);
	public static final TTCN_Typedescriptor TitanComponent_descr_ = new TTCN_Typedescriptor("component", null, null, null);
	public static final TTCN_Typedescriptor TitanDefault_descr_ = new TTCN_Typedescriptor("default", null, null, null);
	public static final TTCN_Typedescriptor TitanAsn_Null_descr_ = new TTCN_Typedescriptor("NULL", null, null, null);
	public static final TTCN_Typedescriptor TitanAsn_Any_descr_ = new TTCN_Typedescriptor("ANY", null, null, null);
	public static final TTCN_Typedescriptor TitanExternal_descr_ = new TTCN_Typedescriptor("EXTERNAL", null, null, null);
	public static final TTCN_Typedescriptor TitanEmbedded_PDV_descr_ = new TTCN_Typedescriptor("EMBEDDED PDV", null, null, null);
	public static final TTCN_Typedescriptor TitanCharacter_String_descr_ = new TTCN_Typedescriptor("CHARACTER STRING", null, null, null);
	public static final TTCN_Typedescriptor TitanObjectDescriptor_descr_ = new TTCN_Typedescriptor("ObjectDescriptor", null, null, null);
	public static final TTCN_Typedescriptor TitanUTF8String_descr_ = new TTCN_Typedescriptor("UTF8String", null, null, null);
	public static final TTCN_Typedescriptor TitanAsn_Roid_descr_ = new TTCN_Typedescriptor("RELATIVE-OID", null, null, null);
	public static final TTCN_Typedescriptor TitanNumericString_descr_ = new TTCN_Typedescriptor("NumericString", null, null, null);
	public static final TTCN_Typedescriptor TitanPrintableString_descr_ = new TTCN_Typedescriptor("PrintableString", null, null, null);
	public static final TTCN_Typedescriptor TitanTeletexString_descr_ = new TTCN_Typedescriptor("TeletexString", null, null, null);
	public static final TTCN_Typedescriptor TitanT61String_descr_ = TitanTeletexString_descr_;
	public static final TTCN_Typedescriptor TitanVideotexString_descr_ = new TTCN_Typedescriptor("VideotexString", null, null, null);
	public static final TTCN_Typedescriptor TitanIA5String_descr_ = new TTCN_Typedescriptor("IA5String", null, null, null);
	public static final TTCN_Typedescriptor TitanASN_GeneralizedTime_descr_ = new TTCN_Typedescriptor("GeneralizedTime", null, null, null);
	public static final TTCN_Typedescriptor TitanASN_UTCTime_descr_ = new TTCN_Typedescriptor("UTCTime", null, null, null);
	public static final TTCN_Typedescriptor TitanGraphicString_descr_ = new TTCN_Typedescriptor("GraphicString", null, null, null);
	public static final TTCN_Typedescriptor TitanVisibleString_descr_ = new TTCN_Typedescriptor("VisibleString", null, null, null);
	public static final TTCN_Typedescriptor TitanISO646String_descr_ = TitanVisibleString_descr_;
	public static final TTCN_Typedescriptor TitanGeneralString_descr_ = new TTCN_Typedescriptor("GeneralString", null, null, null);
	public static final TTCN_Typedescriptor TitanUniversalString_descr_ = new TTCN_Typedescriptor("UniversalString", null, null, null);
	public static final TTCN_Typedescriptor TitanBMPString_descr_ = new TTCN_Typedescriptor("BMPString", null, null, null);

	/**
	 * Deletes the value, setting it to unbound.
	 * */
	public abstract void clean_up();

	/**
	 * Whether the value is present.
	 * Note: this is not the TTCN-3 ispresent()!
	 * causes DTE, must be used only if the field is OPTIONAL<>
	 *
	 * @return {@code true} if the value is present.
	 */
	public abstract boolean is_present();

	/**
	 * Whether the value is bound.
	 *
	 * @return {@code true} if the value is bound.
	 */
	public abstract boolean is_bound();

	/**
	 * Whether the value is a actual value.
	 *
	 * @return {@code true} if the value is a actual value.
	 */
	public boolean is_value() {
		return is_bound();
	}

	/**
	 * Whether the value is optional.
	 *
	 * @return {@code true} if the value is optional.
	 */
	public boolean is_optional() {
		return false;
	}

	/**
	 * Checks that this value is bound or not. Unbound value results in
	 * dynamic testcase error with the provided error message.
	 *
	 * @param errorMessage
	 *                the error message to report.
	 * */
	public void must_bound(final String errorMessage) {
		if ( !is_bound() ) {
			throw new TtcnError( errorMessage );
		}
	}

	/**
	 * Checks if the current value is equivalent to the provided one.
	 *<p>
	 * operator== in the core
	 *
	 * @param otherValue
	 *                the other value to check against.
	 * @return {@code true} if the values are equivalent.
	 */
	public abstract boolean operator_equals(final Base_Type otherValue);

	/**
	 * Assigns the other value to this value.
	 * Overwriting the current content in the process.
	 *<p>
	 * operator= in the core.
	 *
	 * @param otherValue
	 *                the other value to assign.
	 * @return the new value object.
	 */
	public abstract Base_Type operator_assign(final Base_Type otherValue);

	/**
	 * Logs this value.
	 */
	public abstract void log();

	/**
	 * Initialize this object (or one of its fields/elements) with a
	 * module parameter value. The module parameter may contain references to
	 * other module parameters or module parameter expressions, which are processed
	 * by this method to calculated the final result.
	 * @param param module parameter value (its ID specifies which object is to be set) */
	public abstract void set_param (final Param_Types.Module_Parameter param);

	// Originally RT2
	//TODO: make it abstract
	//public abstract Module_Parameter get_param(Module_Param_Name param_name);
	public Module_Parameter get_param(final Module_Param_Name param_name) {
		return null;
	}

	/**
	 * Recursively set the optional fields to omit.
	 */
	public void set_implicit_omit() {
		//default implementation is intentionally empty
	}

	/**
	 * Encodes this value object with Titan's internal encoding and appends it into the provided buffer.
	 *
	 * @param text_buf the buffer to extend with the value's internal encoding.
	 * */
	public abstract void encode_text(final Text_Buf text_buf);

	/**
	 * Decodes from the provided buffer the actual contents of this value.
	 * Overwriting previous contents.
	 *
	 * @param text_buf the buffer containing the value's internal encoding.
	 * */
	public abstract void decode_text(final Text_Buf text_buf);

	/**
	 * Encode this value into a buffer.
	 *
	 * originally flavour is not used for all encodings.
	 *
	 * @param p_td the type descriptor containing details for the encoding.
	 * @param p_buf the buffer to but the encoded bytes into.
	 * @param p_coding the coding to use.
	 * @param flavour the flavor of the coding to use.
	 * */
	public void encode(final TTCN_Typedescriptor p_td, final TTCN_Buffer p_buf, final TTCN_EncDec.coding_type p_coding, final int flavour) {
		// TODO once encoding is implemented for all classes this function should become abstract
		TTCN_Logger.begin_event(Severity.ERROR_UNQUALIFIED);
		TTCN_Logger.log_event_str( "//TODO: " );
		TTCN_Logger.log_event_str( getClass().getSimpleName() );
		TTCN_Logger.log_event_str( ".encode() is not yet implemented!\n" );
		TTCN_Logger.end_event();
	}

	/**
	 * Decode this value from a buffer.
	 *
	 * originally flavour is not used for all decodings.
	 *
	 * @param p_td the type descriptor containing details for the decoding.
	 * @param p_buf the buffer to read the bytes from.
	 * @param p_coding the coding to use.
	 * @param flavour the flavor of the coding to use.
	 * */
	public void decode(final TTCN_Typedescriptor p_td, final TTCN_Buffer p_buf, final TTCN_EncDec.coding_type p_coding, final int flavour) {
		// TODO once decoding is implemented for all classes this function should become abstract
		TTCN_Logger.begin_event(Severity.ERROR_UNQUALIFIED);
		TTCN_Logger.log_event_str( "//TODO: " );
		TTCN_Logger.log_event_str( getClass().getSimpleName() );
		TTCN_Logger.log_event_str( ".decode() is not yet implemented!\n" );
		TTCN_Logger.end_event();
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
		throw new TtcnError(MessageFormat.format("RAW encoding requested for type `{0}'' which has no RAW encoding method.", p_td.name));
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
		return RAW_decode(p_td, buff, limit, top_bit_ord, false, -1, true, null);
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
	 *                In case of unions the selected field indicator for CROSSTAG, or -1
	 *                In case of record of type the number of elements in the list, or -1
	 * @param first_call
	 *                Indicates that the decode is called for the first time,
	 *                or it is a repeated call.
	 *                default TRUE. May be FALSE for a REPEATABLE record-of
	 *                inside a set, if an element has been successfully
	 *                decoded.
	 * @param forceOmit indicates how optional fields should be treated, according to the force omit coding variant.
	 * @return length of decoded field, or a negative number for error
	 *
	 * @throws TtcnError
	 *                 in case of not being implemented
	 * */
	public int RAW_decode(final TTCN_Typedescriptor p_td, final TTCN_Buffer buff, final int limit, final raw_order_t top_bit_ord, final boolean no_err, final int sel_field, final boolean first_call, final RAW_Force_Omit force_omit) {
		throw new TtcnError(MessageFormat.format("RAW decoding requested for type `{0}'' which has no RAW encoding method.", p_td.name));
	}

//TODO: remove abstract methods
/*
	public abstract int JSON_encode(final TTCN_Typedescriptor p_td, final JSON_Tokenizer p_tok);
	public abstract int JSON_decode(final TTCN_Typedescriptor p_td, final JSON_Tokenizer p_tok, final boolean p_silent, final int p_chosen_field);
/*/
	//TODO: comment
	//It is assumed that when calling this function p_td.json is not null
	public int JSON_encode(final TTCN_Typedescriptor p_td, final JSON_Tokenizer p_tok) {
		throw new TtcnError(MessageFormat.format("JSON encoding requested for type `{0}'' which has no JSON encoding method.", p_td.name));
	}

	//TODO: comment
	//It is assumed that when calling this function p_td.json is not null
	public int JSON_decode(final TTCN_Typedescriptor p_td, final JSON_Tokenizer p_tok, final boolean p_silent, final int p_chosen_field) {
		throw new TtcnError(MessageFormat.format("JSON decoding requested for type `{0}'' which has no JSON decoding method.", p_td.name));
	}
//*/
	//It is assumed that when calling this function p_td.json is not null
	public final int JSON_decode(final TTCN_Typedescriptor p_td, final JSON_Tokenizer p_tok, final boolean p_silent) {
		return JSON_decode(p_td, p_tok, p_silent, JSON.CHOSEN_FIELD_UNSET);
	}
}
