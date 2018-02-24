/******************************************************************************
 * Copyright (c) 2000-2017 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

import java.text.MessageFormat;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Gergo Ujhelyi
 * @author Kristof Szabados
 *
 * Static class which encapsulates the stuff related to
 * encoding/decoding.
 *
 * TODO only RAW encoding/decoding is supported for now.
 * the current implementation is only a placeholder to mark the architectural borders.
 * */
public final class TTCN_EncDec {
	public static final int BER_ENCODE_CER = 1;
	public static final int BER_ENCODE_DER = 2;
	public static final int BER_ACCEPT_SHORT = 0x01;
	public static final int BER_ACCEPT_LONG = 0x02;
	public static final int BER_ACCEPT_INDEFINITE = 0x04;
	public static final int BER_ACCEPT_DEFINITE = 0x03;
	public static final int BER_ACCEPT_ALL = 0x07;
	public static final int XER_EXTENDED = 1 << 2;

	/** Last error value */
	private static error_type last_error_type = error_type.ET_NONE;

	/** Error string for the last error */
	private static String error_str;

	/** Default error behaviours for all error types */
	private static final error_behavior_type[] default_error_behavior = {
		error_behavior_type.EB_ERROR,
		error_behavior_type.EB_ERROR,
		error_behavior_type.EB_ERROR,
		error_behavior_type.EB_WARNING,
		error_behavior_type.EB_ERROR,
		error_behavior_type.EB_WARNING,
		error_behavior_type.EB_ERROR,
		error_behavior_type.EB_WARNING,
		error_behavior_type.EB_WARNING,
		error_behavior_type.EB_ERROR,
		error_behavior_type.EB_ERROR,
		error_behavior_type.EB_ERROR,
		error_behavior_type.EB_ERROR,
		error_behavior_type.EB_ERROR,
		error_behavior_type.EB_WARNING,
		error_behavior_type.EB_WARNING,
		error_behavior_type.EB_WARNING,
		error_behavior_type.EB_ERROR,
		error_behavior_type.EB_ERROR,
		error_behavior_type.EB_ERROR,
		error_behavior_type.EB_ERROR,
		error_behavior_type.EB_IGNORE,
		error_behavior_type.EB_WARNING,
		error_behavior_type.EB_ERROR,
		error_behavior_type.EB_ERROR,
		error_behavior_type.EB_ERROR
	};

	/** Current error behaviours for all error types */
	private static final error_behavior_type[] error_behavior  = {
		error_behavior_type.EB_ERROR,
		error_behavior_type.EB_ERROR,
		error_behavior_type.EB_ERROR,
		error_behavior_type.EB_WARNING,
		error_behavior_type.EB_ERROR,
		error_behavior_type.EB_WARNING,
		error_behavior_type.EB_ERROR,
		error_behavior_type.EB_WARNING,
		error_behavior_type.EB_WARNING,
		error_behavior_type.EB_ERROR,
		error_behavior_type.EB_ERROR,
		error_behavior_type.EB_ERROR,
		error_behavior_type.EB_ERROR,
		error_behavior_type.EB_ERROR,
		error_behavior_type.EB_WARNING,
		error_behavior_type.EB_WARNING,
		error_behavior_type.EB_WARNING,
		error_behavior_type.EB_ERROR,
		error_behavior_type.EB_ERROR,
		error_behavior_type.EB_ERROR,
		error_behavior_type.EB_ERROR,
		error_behavior_type.EB_IGNORE,
		error_behavior_type.EB_WARNING,
		error_behavior_type.EB_ERROR,
		error_behavior_type.EB_ERROR,
		error_behavior_type.EB_ERROR
	};

	/** Endianness indicator */
	public static enum raw_order_t {
		ORDER_MSB,
		ORDER_LSB
	}

	/**
	 * The indicator of the encoding.
	 *
	 * Only RAW is supported for now
	 * */
	public static enum coding_type {
		CT_BER,
		CT_PER,
		CT_RAW,
		CT_TEXT,
		CT_XER,
		CT_JSON,
		CT_OER
	}

	public enum error_type {
		ET_UNDEF,     /**< Undefined error. 0*/
		ET_UNBOUND,   /**< Encoding of an unbound value. 1*/
		ET_INCOMPL_ANY,   /**< Encoding of an ASN ANY value which does not
				                           contain a valid BER TLV. */
		ET_ENC_ENUM,      /**< Encoding of an unknown enumerated value. */
		ET_INCOMPL_MSG,   /**< Decode error: incomplete message. */
		ET_LEN_FORM,      /**< During decoding: the received message has a
				                           non-acceptable length form (BER). 5*/
		ET_INVAL_MSG,     /**< Decode error: invalid message. */
		ET_REPR,          /**< Representation error, e.g.: internal
				                           representation of integral numbers. */
		ET_CONSTRAINT,    /**< The value breaks some constraint. */
		ET_TAG,           /**< During decoding: unexpected tag. */
		ET_SUPERFL,       /**< During decoding: superfluous part
				                           detected. In case of BER, this can be
				                           superfluous TLV at the end of a constructed
				                           TLV. 10 */
		ET_EXTENSION,     /**< During decoding: there was something in the
				                           extension (e.g.: in ASN.1 ellipsis). */
		ET_DEC_ENUM,      /**< During decoding of an (inextensible)
				                           enumerated value: unknown value received. */
		ET_DEC_DUPFLD,    /**< During decoding: duplicated field. For
				                           example, while decoding a SET type. */
		ET_DEC_MISSFLD,   /**< During decoding: missing field. For example,
				                           while decoding a SET type. */
		ET_DEC_OPENTYPE,  /**< Cannot decode an opentype (broken component
				                           relation constraint). 15 */
		ET_DEC_UCSTR,     /**< During decoding a universal charstring:
				                           something went wrong. */
		ET_LEN_ERR,       /**< During RAW encoding: the specified field
				                           length is not enough to encode the value of
				                           the field. During RAW decoding: the available
				                           number of bits is less than it needed to
				                           decode the field. */
		ET_SIGN_ERR,      /**< Unsigned encoding of a negative number. */
		ET_INCOMP_ORDER,  /**< Incompatible combination of orders attribute
				                           for RAW coding. */
		ET_TOKEN_ERR,     /**< During TEXT decoding the specified token is
				                           not found. 20 */
		ET_LOG_MATCHING,  /**< During TEXT decoding the matching is logged
				                           if the behavior was set to EB_WARNING. */
		ET_FLOAT_TR,      /**< The float value will be truncated during
				                           double -> single precision conversion */
		ET_FLOAT_NAN,     /**< Not a Number has been received */
		ET_OMITTED_TAG,   /**< During encoding the key of a tag references
				                           an optional field with omitted value */
		ET_NEGTEST_CONFL, /**< Contradictory negative testing and RAW attributes. */
		ET_ALL,           /**< Used only when setting error behavior. 26 */
		ET_INTERNAL,      /**< Internal error. Error behavior cannot be set
				                           for this. */
		ET_NONE           /**< There was no error. */
	}
	/** Error behavior enum type. */
	public enum error_behavior_type {
		EB_DEFAULT, /**< Used only when setting error behavior. */
		EB_ERROR,   /**< Raises an error. */
		EB_WARNING, /**< Logs the error but continues the activity. */
		EB_IGNORE   /**< Totally ignores the error. */
	}

	/** @brief Set the error behaviour for encoding/decoding functions
	 *
	 *  @param p_et error type
	 *  @param p_eb error behaviour
	 */
	public static void set_error_behavior(final error_type p_et, final error_behavior_type p_eb) {
		if (p_et.ordinal() < error_type.ET_UNDEF.ordinal() || p_et.ordinal() > error_type.ET_ALL.ordinal()
				|| p_eb.ordinal() < error_behavior_type.EB_DEFAULT.ordinal()
				|| p_eb.ordinal() > error_behavior_type.EB_IGNORE.ordinal()) {
			throw new TtcnError("EncDec::set_error_behavior(): Invalid parameter.");
		}
		if (p_eb == error_behavior_type.EB_DEFAULT) {
			if (p_et == error_type.ET_ALL) {
				for (int i = error_type.ET_UNDEF.ordinal(); i < error_type.ET_ALL.ordinal(); i++) {
					error_behavior[i] = default_error_behavior[i];
				}
			} else {
				error_behavior[p_et.ordinal()] = default_error_behavior[p_et.ordinal()];
			}
		} else {
			if (p_et == error_type.ET_ALL) {
				for (int i = error_type.ET_UNDEF.ordinal(); i < error_type.ET_ALL.ordinal(); i++) {
					error_behavior[i] = p_eb;
				}
			} else {
				error_behavior[p_et.ordinal()] = p_eb;
			}
		}
	}

	/** @brief Get the current error behaviour
	 *
	 *  @param p_et error type
	 *  @return error behaviour for the supplied error type
	 */
	public static error_behavior_type get_error_behavior(final error_type p_et) {
		if (p_et.ordinal() < error_type.ET_UNDEF.ordinal() || p_et.ordinal() >= error_type.ET_ALL.ordinal()) {
			throw new TtcnError("EncDec::get_error_behavior(): Invalid parameter.");
		}
		return error_behavior[p_et.ordinal()];
	}

	/** @brief Get the default error behaviour
	 *
	 *  @param p_et error type
	 *  @return default error behaviour for the supplied error type
	 */
	public static error_behavior_type get_default_error_behavior(final error_type p_et) {
		if (p_et.ordinal() < error_type.ET_UNDEF.ordinal() || p_et.ordinal() >= error_type.ET_ALL.ordinal()) {
			throw new TtcnError("EncDec::get_error_behavior(): Invalid parameter.");
		}
		return default_error_behavior[p_et.ordinal()];
	}

	/** @brief Get the last error code.
	 *
	 *  @return last_error_type
	 */
	public static error_type get_last_error_type() {
		return last_error_type;
	}

	/** @brief Get the error string corresponding to the last error.
	 *
	 *  @return error_str
	 */
	public static String get_error_str() {
		return error_str;
	}

	/** @brief Set a clean slate
	 *
	 *  Sets last_error_type to ET_NONE.
	 *  Frees error_str.
	 */
	public static void clear_error() {
		last_error_type = error_type.ET_NONE;
		error_str = null;
	}

	/* The stuff below this line is for internal use only */
	public static void error(final error_type p_et, final String msg) {
		last_error_type = p_et;
		error_str = "";
		error_str = msg;
		if (p_et.ordinal() >= error_type.ET_UNDEF.ordinal() && p_et.ordinal() < error_type.ET_ALL.ordinal()) {
			switch (error_behavior[p_et.ordinal()]) {
			case EB_ERROR:
				throw new TtcnError(error_str);
			case EB_WARNING:
				TtcnError.TtcnWarning(error_str);
				break;
			default:
				break;
			} // switch
		}
	}

	/**
	 * Extract the parameters (coding type and extra settings) from the dynamic
	 * coding string of coding predefined functions.
	 *
	 * coding_type has to be returned in Java.
	 * */
	public static coding_type get_coding_from_str(final TitanUniversalCharString coding_str, final AtomicInteger extra, final boolean encode) {
		if (coding_str.operatorEquals("BER:2002") || coding_str.operatorEquals("DER:2002")) {
			if (encode) {
				extra.set(BER_ENCODE_DER);
			} else {
				extra.set(BER_ACCEPT_ALL);
			}

			return coding_type.CT_BER;
		} else if (coding_str.operatorEquals("CER:2002")) {
			if (encode) {
				extra.set(BER_ENCODE_CER);
			} else {
				extra.set(BER_ACCEPT_ALL);
			}

			return coding_type.CT_BER;
		} else if (coding_str.operatorEquals("RAW")) {
			return coding_type.CT_RAW;
		} else if (coding_str.operatorEquals("TEXT")) {
			return coding_type.CT_TEXT;
		} else if (coding_str.operatorEquals("JSON")) {
			return coding_type.CT_JSON;
		} else if (coding_str.operatorEquals("XML") || coding_str.operatorEquals("XER")) {
			if (extra != null) {
				extra.set(XER_EXTENDED);
			}

			return coding_type.CT_XER;
		} else if (coding_str.operatorEquals("OER")) {
			return coding_type.CT_OER;
		} else {
			TtcnLogger.begin_event_log2str();
			coding_str.log();
			throw new TtcnError(MessageFormat.format("Invalid encoding string: {0}", TtcnLogger.end_event_log2str()));
		}
	}
}
