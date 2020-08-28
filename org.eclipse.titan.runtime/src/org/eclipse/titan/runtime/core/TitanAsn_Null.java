/******************************************************************************
 * Copyright (c) 2000-2020 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

import java.text.MessageFormat;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.titan.runtime.core.BER.ASN_BERdescriptor;
import org.eclipse.titan.runtime.core.BER.ASN_Tag;
import org.eclipse.titan.runtime.core.BER.ASN_TagClass;
import org.eclipse.titan.runtime.core.JSON.TTCN_JSONdescriptor;
import org.eclipse.titan.runtime.core.JSON.json_string_escaping;
import org.eclipse.titan.runtime.core.JSON_Tokenizer.json_token_t;
import org.eclipse.titan.runtime.core.Param_Types.Module_Param_Asn_Null;
import org.eclipse.titan.runtime.core.Param_Types.Module_Param_Name;
import org.eclipse.titan.runtime.core.Param_Types.Module_Param_Unbound;
import org.eclipse.titan.runtime.core.Param_Types.Module_Parameter;
import org.eclipse.titan.runtime.core.Param_Types.Module_Parameter.basic_check_bits_t;
import org.eclipse.titan.runtime.core.Param_Types.Module_Parameter.type_t;
import org.eclipse.titan.runtime.core.TTCN_EncDec.coding_type;
import org.eclipse.titan.runtime.core.TTCN_EncDec.error_type;

/**
 * ASN.1 NULL type
 *
 * @author Kristof Szabados
 * @author Arpad Lovassy
 */
public class TitanAsn_Null extends Base_Type {
	private static final ASN_Tag TitanASN_Null_tag_[] = new ASN_Tag[] {new ASN_Tag(ASN_TagClass.ASN_TAG_UNIV, 5)};
	public static final ASN_BERdescriptor TitanASN_Null_Ber_ = new ASN_BERdescriptor(1, TitanASN_Null_tag_);
	public static final TTCN_JSONdescriptor TitanAsn_Null_json_ = new TTCN_JSONdescriptor(false, null, false, null, false, false, false, 0, null, false, json_string_escaping.ESCAPE_AS_SHORT);

	public static final TTCN_Typedescriptor TitanAsn_Null_descr_ = new TTCN_Typedescriptor("NULL", TitanASN_Null_Ber_, null, TitanAsn_Null_json_, null);

	/**
	 * This enum is only used so that we can create a bound ASN.1 null.
	 * Needed to differentiate between the constructors.
	 * */
	public enum Asn_Null_Type {
		ASN_NULL_VALUE
	};

	private boolean boundFlag;

	/**
	 * Initializes to unbound value.
	 * */
	public TitanAsn_Null() {
		boundFlag = false;
	}

	/**
	 * Initializes to a given value.
	 *
	 * @param otherValue
	 *                the value to initialize to.
	 * */
	public TitanAsn_Null(final Asn_Null_Type otherValue) {
		//the formal parameter is intentionally unused here.
		boundFlag = true;
	}

	/**
	 * Copy constructor.
	 *
	 * @param otherValue
	 *                the value to copy.
	 * */
	public TitanAsn_Null(final TitanAsn_Null otherValue) {
		otherValue.must_bound("Copying an unbound ASN.1 NULL value.");

		boundFlag = true;
	}

	@Override
	public void clean_up() {
		boundFlag = false;
	}

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
	public TitanAsn_Null operator_assign(final Asn_Null_Type otherValue) {
		boundFlag = true;

		return this;
	}

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
	public TitanAsn_Null operator_assign(final TitanAsn_Null otherValue) {
		otherValue.must_bound("Assignment of an unbound ASN.1 NULL value.");

		boundFlag = true;

		return this;
	}

	@Override
	public TitanAsn_Null operator_assign(final Base_Type otherValue) {
		if (otherValue instanceof TitanAsn_Null) {
			return operator_assign((TitanAsn_Null)otherValue);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to ASN.1 NULL", otherValue));
	}

	/**
	 * Checks if the current value is equivalent to the provided one.
	 *
	 * operator== in the core
	 *
	 * @param otherValue
	 *                the other value to check against.
	 * @return {@code true} if the values are equivalent.
	 */
	public boolean operator_equals(final Asn_Null_Type otherValue) {
		must_bound("The left operand of comparison is an unbound ASN.1 NULL value.");

		return true;
	}

	/**
	 * Checks if the current value is equivalent to the provided one.
	 *
	 * operator== in the core
	 *
	 * @param otherValue
	 *                the other value to check against.
	 * @return {@code true} if the values are equivalent.
	 */
	public boolean operator_equals(final TitanAsn_Null otherValue) {
		must_bound("The left operand of comparison is an unbound ASN.1 NULL value.");
		otherValue.must_bound("The right operand of comparison is an unbound ASN.1 NULL value.");

		return true;
	}

	@Override
	public boolean operator_equals(final Base_Type otherValue) {
		if (otherValue instanceof TitanAsn_Null) {
			return operator_equals((TitanAsn_Null) otherValue);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to ASN.1 NULL", otherValue));
	}

	/**
	 * Checks if the current value is not equivalent to the provided one.
	 *
	 * operator!= in the core
	 *
	 * @param otherValue
	 *                the other value to check against.
	 * @return {@code true} if the values are not equivalent.
	 */
	public boolean operator_not_equals(final Asn_Null_Type otherValue) {
		return !operator_equals(otherValue);
	}

	/**
	 * Checks if the current value is not equivalent to the provided one.
	 *
	 * operator!= in the core
	 *
	 * @param otherValue
	 *                the other value to check against.
	 * @return {@code true} if the values are not equivalent.
	 */
	public boolean operator_not_equals(final TitanAsn_Null otherValue) {
		return !operator_equals(otherValue);
	}

	@Override
	public boolean is_bound() {
		return boundFlag;
	}

	@Override
	public boolean is_present() {
		return boundFlag;
	}

	@Override
	public boolean is_value() {
		return boundFlag;
	}

	@Override
	public void log() {
		if (boundFlag) {
			TTCN_Logger.log_event_str("NULL");
		} else {
			TTCN_Logger.log_event_unbound();
		}
	}

	@Override
	/** {@inheritDoc} */
	public void set_param(Module_Parameter param) {
		param.basic_check(basic_check_bits_t.BC_VALUE.getValue(), "NULL value");

		// Originally RT2
		if (param.get_type() == Module_Parameter.type_t.MP_Reference) {
			param = param.get_referenced_param().get();
		}

		if (param.get_type() != type_t.MP_Asn_Null) {
			param.type_error("NULL value");
		}
		boundFlag = true;
	}

	@Override
	/** {@inheritDoc} */
	public Module_Parameter get_param(final Module_Param_Name param_name) {
		if (!is_bound()) {
			return new Module_Param_Unbound();
		}
		return new Module_Param_Asn_Null();
	}

	@Override
	/** {@inheritDoc} */
	public void encode_text(final Text_Buf text_buf) {
		if (!boundFlag) {
			throw new TtcnError("Text encoder: Encoding an ASN.1 NULL value.");
		}
	}

	@Override
	/** {@inheritDoc} */
	public void decode_text(final Text_Buf text_buf) {
		boundFlag = true;
	}

	@Override
	/** {@inheritDoc} */
	public void encode(final TTCN_Typedescriptor p_td, final TTCN_Buffer p_buf, final coding_type p_coding, final int flavour) {
		switch (p_coding) {
		case CT_JSON: {
			final TTCN_EncDec_ErrorContext errorContext = new TTCN_EncDec_ErrorContext("While JSON-encoding type '%s': ", p_td.name);
			try {
				if(p_td.json == null) {
					TTCN_EncDec_ErrorContext.error_internal("No JSON descriptor available for type '%s'.", p_td.name);
				}

				final JSON_Tokenizer tok = new JSON_Tokenizer(flavour != 0);
				JSON_encode(p_td, tok);
				final StringBuilder temp = tok.get_buffer();
				for (int i = 0; i < temp.length(); i++) {
					final int temp2 = temp.charAt(i);
					p_buf.put_c((byte)temp2);
				}
			} finally {
				errorContext.leave_context();
			}
			break;
		}
		default:
			throw new TtcnError(MessageFormat.format("Unknown coding method requested to encode type `{0}''", p_td.name));
		}
	}

	@Override
	/** {@inheritDoc} */
	public void decode(final TTCN_Typedescriptor p_td, final TTCN_Buffer p_buf, final coding_type p_coding, final int flavour) {
		switch (p_coding) {
		case CT_JSON: {
			final TTCN_EncDec_ErrorContext errorContext = new TTCN_EncDec_ErrorContext("While JSON-decoding type '%s': ", p_td.name);
			try {
				if(p_td.json == null) {
					TTCN_EncDec_ErrorContext.error_internal("No JSON descriptor available for type '%s'.", p_td.name);
				}

				final byte[] data = p_buf.get_data();
				final char[] temp = new char[data.length];
				for (int i = 0; i < data.length; i++) {
					temp[i] = (char)data[i];
				}
				final JSON_Tokenizer tok = new JSON_Tokenizer(new String(temp), p_buf.get_len());
				if(JSON_decode(p_td, tok, false) < 0) {
					TTCN_EncDec_ErrorContext.error(error_type.ET_INCOMPL_MSG,
							"Can not decode type '%s', because invalid or incomplete message was received", p_td.name);
				}
				p_buf.set_pos(tok.get_buf_pos());
			} finally {
				errorContext.leave_context();
			}
			break;
		}
		default:
			throw new TtcnError(MessageFormat.format("Unknown coding method requested to decode type `{0}''", p_td.name));
		}
	}

	@Override
	/** {@inheritDoc} */
	public int JSON_encode(final TTCN_Typedescriptor p_td, final JSON_Tokenizer p_tok, final boolean p_parent_is_map) {
		if (!is_bound()) {
			TTCN_EncDec_ErrorContext.error(error_type.ET_UNBOUND, "Encoding an unbound ASN.1 NULL value.");
			return -1;
		}

		return p_tok.put_next_token(json_token_t.JSON_TOKEN_LITERAL_NULL);
	}

	@Override
	/** {@inheritDoc} */
	public int JSON_decode(final TTCN_Typedescriptor p_td, final JSON_Tokenizer p_tok, final boolean p_silent, final boolean p_parent_is_map, final int p_chosen_field) {
		final AtomicReference<json_token_t> token = new AtomicReference<json_token_t>(json_token_t.JSON_TOKEN_NONE);
		final int dec_len = p_tok.get_next_token(token, null, null);
		if (json_token_t.JSON_TOKEN_ERROR == token.get()) {
			if (!p_silent) {
				TTCN_EncDec_ErrorContext.error(error_type.ET_INVAL_MSG, JSON.JSON_DEC_BAD_TOKEN_ERROR, "");
			}

			return JSON.JSON_ERROR_FATAL;
		} else if (json_token_t.JSON_TOKEN_LITERAL_NULL != token.get()) {
			return JSON.JSON_ERROR_INVALID_TOKEN;
		}

		boundFlag = true;
		return dec_len;
	}

	/**
	 * Checks if the first value is equivalent to the second one.
	 *
	 * static operator== in the core
	 *
	 * @param parValue
	 *                the first value.
	 * @param otherValue
	 *                the other value to check against.
	 * @return {@code true} if the values are equivalent.
	 */
	public static boolean operator_equals(final Asn_Null_Type parValue, final TitanAsn_Null otherValue) {
		otherValue.must_bound("The right operand of comparison is an unbound ASN.1 NULL value.");

		return true;
	}

	/**
	 * Checks if the first value is not equivalent to the second one.
	 *
	 * static operator!= in the core
	 *
	 * @param parValue
	 *                the first value.
	 * @param otherValue
	 *                the other value to check against.
	 * @return {@code true} if the values are equivalent.
	 */
	public static boolean operator_not_equals(final Asn_Null_Type parValue, final TitanAsn_Null otherValue) {
		return !operator_equals(parValue, otherValue);
	}
}
