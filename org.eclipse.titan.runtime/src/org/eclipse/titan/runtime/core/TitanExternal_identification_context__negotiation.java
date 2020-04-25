/******************************************************************************
 * Copyright (c) 2000-2020 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

import java.text.MessageFormat;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.titan.runtime.core.JSON.TTCN_JSONdescriptor;
import org.eclipse.titan.runtime.core.JSON_Tokenizer.json_token_t;
import org.eclipse.titan.runtime.core.Param_Types.Module_Parameter;
import org.eclipse.titan.runtime.core.RAW.RAW_enc_tr_pos;
import org.eclipse.titan.runtime.core.RAW.RAW_enc_tree;
import org.eclipse.titan.runtime.core.RAW.top_bit_order_t;
import org.eclipse.titan.runtime.core.TTCN_EncDec.coding_type;
import org.eclipse.titan.runtime.core.TTCN_EncDec.error_type;
import org.eclipse.titan.runtime.core.TTCN_EncDec.raw_order_t;

/**
 * Part of the representation of the ASN.1 EXTERNAL type
 *
 * @author Kristof Szabados
 */
public class TitanExternal_identification_context__negotiation extends Base_Type {
	public static final TTCN_JSONdescriptor TitanExternal_identification_context__negotiation_json_ =new TTCN_JSONdescriptor(false, null, false, null, false, false, false, 0, null);
	public static final TTCN_Typedescriptor TitanExternal_identification_context__negotiation_descr_ = new TTCN_Typedescriptor("EXTERNAL.identification.context-negotiation", TitanExternal_identification_context__negotiation.TitanExternal_identification_context__negotiation_json_);
	public static final TitanUniversalCharString TitanExternal_identification_context__negotiation_default_coding = new TitanUniversalCharString("JSON");
	public static final TTCN_Typedescriptor TitanExternal_identification_context__negotiation_presentation__context__id_descr_ = new TTCN_Typedescriptor("EXTERNAL.identification.context-negotiation.presentation-context-id", TitanInteger.TitanInteger_json_);
	public static final TitanUniversalCharString TitanExternal_identification_context__negotiation_presentation__context__id_default_coding = new TitanUniversalCharString("JSON");
	public static final TTCN_Typedescriptor TitanExternal_identification_context__negotiation_transfer__syntax_descr_ = new TTCN_Typedescriptor("EXTERNAL.identification.context-negotiation.transfer-syntax", TitanObjectid.TitanObjectid_json_);
	public static final TitanUniversalCharString TitanExternal_identification_context__negotiation_transfer__syntax_default_coding = new TitanUniversalCharString("JSON");
	private final TitanInteger presentation__context__id; //ASN1_Integer_Type
	private final TitanObjectid transfer__syntax; //ObjectID_Type

	/**
	 * Initializes to unbound value.
	 * */
	public TitanExternal_identification_context__negotiation() {
		this.presentation__context__id = new TitanInteger();
		this.transfer__syntax = new TitanObjectid();
	}

	/**
	 * Initializes from given field values. The number of arguments equals
	 * to the number of fields.
	 *
	 * @param presentation__context__id
	 *                the value of field presentation-context-id
	 * @param transfer__syntax
	 *                the value of field transfer-syntax
	 * */
	public TitanExternal_identification_context__negotiation(final TitanInteger presentation__context__id, final TitanObjectid transfer__syntax ) {
		this.presentation__context__id = new TitanInteger( presentation__context__id );
		this.transfer__syntax = new TitanObjectid( transfer__syntax );
	}

	/**
	 * Initializes to a given value.
	 *
	 * @param otherValue
	 *                the value to initialize to.
	 * */
	public TitanExternal_identification_context__negotiation( final TitanExternal_identification_context__negotiation otherValue) {
		otherValue.must_bound("Copying of an unbound value of type EXTERNAL.identification.context-negotiation.");
		presentation__context__id = new TitanInteger();
		transfer__syntax = new TitanObjectid();
		operator_assign( otherValue );
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
	public TitanExternal_identification_context__negotiation operator_assign(final TitanExternal_identification_context__negotiation otherValue ) {
		otherValue.must_bound( "Assignment of an unbound value of type EXTERNAL.identification.context-negotiation");
		if (otherValue != this) {
			if ( otherValue.get_field_presentation__context__id().is_bound() ) {
				this.presentation__context__id.operator_assign( otherValue.get_field_presentation__context__id() );
			} else {
				this.presentation__context__id.clean_up();
			}
			if ( otherValue.get_field_transfer__syntax().is_bound() ) {
				this.transfer__syntax.operator_assign( otherValue.get_field_transfer__syntax() );
			} else {
				this.transfer__syntax.clean_up();
			}
		}

		return this;
	}

	@Override
	public TitanExternal_identification_context__negotiation operator_assign(final Base_Type otherValue) {
		if (otherValue instanceof TitanExternal_identification_context__negotiation ) {
			return operator_assign((TitanExternal_identification_context__negotiation) otherValue);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to EXTERNAL.identification.context-negotiation", otherValue));
	}

	@Override
	public void clean_up() {
		presentation__context__id.clean_up();
		transfer__syntax.clean_up();
	}

	@Override
	public boolean is_bound() {
		return presentation__context__id.is_bound()
				|| transfer__syntax.is_bound();
	}

	@Override
	public boolean is_present() {
		return is_bound();
	}

	@Override
	public boolean is_value() {
		return presentation__context__id.is_value()
				&& transfer__syntax.is_value();
	}

	/**
	 * Checks if the current value is equivalent to the provided one.
	 *
	 * operator== in the core
	 *
	 * @param other_value
	 *                the other value to check against.
	 * @return {@code true} if all fields are equivalent, {@code false} otherwise.
	 */
	public boolean operator_equals( final TitanExternal_identification_context__negotiation other_value) {
		return presentation__context__id.operator_equals( other_value.presentation__context__id )
				&& transfer__syntax.operator_equals( other_value.transfer__syntax );
	}

	@Override
	public boolean operator_equals(final Base_Type other_value) {
		if (other_value instanceof TitanExternal_identification_context__negotiation ) {
			return operator_equals((TitanExternal_identification_context__negotiation) other_value);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to EXTERNAL.identification.context-negotiation", other_value));
	}

	/**
	 * Checks if the current value is not equivalent to the provided one.
	 *
	 * operator!= in the core
	 *
	 * @param other_value
	 *                the other value to check against.
	 * @return {@code true} if all fields are not equivalent, {@code false} otherwise.
	 */
	public boolean operator_not_equals( final TitanExternal_identification_context__negotiation other_value) {
		return !operator_equals(other_value);
	}
	/**
	 * Gives access to the field presentation-context-id.
	 *
	 * @return the field presentation-context-id.
	 * */
	public TitanInteger get_field_presentation__context__id() {
		return presentation__context__id;
	}

	/**
	 * Gives read-only access to the field presentation-context-id.
	 *
	 * @return the field presentation-context-id.
	 * */
	public TitanInteger constGet_field_presentation__context__id() {
		return presentation__context__id;
	}

	/**
	 * Gives access to the field transfer-syntax.
	 *
	 * @return the field transfer-syntax.
	 * */
	public TitanObjectid get_field_transfer__syntax() {
		return transfer__syntax;
	}

	/**
	 * Gives read-only access to the field transfer-syntax.
	 *
	 * @return the field transfer-syntax.
	 * */
	public TitanObjectid constGet_field_transfer__syntax() {
		return transfer__syntax;
	}

	/**
	 * Returns the size (number of fields).
	 *
	 * size_of in the core
	 *
	 * @return the size of the structure.
	 * */
	public TitanInteger size_of() {
		return new TitanInteger(2);
	}

	@Override
	public void log() {
		if (!is_bound()) {
			TTCN_Logger.log_event_unbound();
			return;
		}
		TTCN_Logger.log_char('{');
		TTCN_Logger.log_event_str(" presentation-context-id := ");
		presentation__context__id.log();
		TTCN_Logger.log_char(',');
		TTCN_Logger.log_event_str(" transfer-syntax := ");
		transfer__syntax.log();
		TTCN_Logger.log_event_str(" }");
	}

	@Override
	public void set_param(final Module_Parameter param) {
		param.basic_check(Module_Parameter.basic_check_bits_t.BC_VALUE.getValue(), "set value");
		switch (param.get_type()) {
		case MP_Value_List:
			if (param.get_size() > 2) {
				param.error(MessageFormat.format("set value of type EXTERNAL.identification.context-negotiation has 2 fields but list value has {0} fields.", param.get_size()));
			}
			if (param.get_size() > 0 && param.get_elem(0).get_type() != Module_Parameter.type_t.MP_NotUsed) {
				get_field_presentation__context__id().set_param(param.get_elem(0));
			}
			if (param.get_size() > 1 && param.get_elem(1).get_type() != Module_Parameter.type_t.MP_NotUsed) {
				get_field_transfer__syntax().set_param(param.get_elem(1));
			}
			break;
		case MP_Assignment_List: {
			final boolean value_used[] = new boolean[param.get_size()];
			for (int val_idx = 0; val_idx < param.get_size(); val_idx++) {
				final Module_Parameter curr_param = param.get_elem(val_idx);
				if ("presentation-context-id".equals(curr_param.get_id().get_name())) {
					if (curr_param.get_type() != Module_Parameter.type_t.MP_NotUsed) {
						get_field_presentation__context__id().set_param(curr_param);
					}
					value_used[val_idx] = true;
				}
			}
			for (int val_idx = 0; val_idx < param.get_size(); val_idx++) {
				final Module_Parameter curr_param = param.get_elem(val_idx);
				if ("transfer-syntax".equals(curr_param.get_id().get_name())) {
					if (curr_param.get_type() != Module_Parameter.type_t.MP_NotUsed) {
						get_field_transfer__syntax().set_param(curr_param);
					}
					value_used[val_idx] = true;
				}
			}
			for (int val_idx = 0; val_idx < param.get_size(); val_idx++) {
				if (!value_used[val_idx]) {
					final Module_Parameter curr_param = param.get_elem(val_idx);
					curr_param.error(MessageFormat.format("Non existent field name in type EXTERNAL.identification.context-negotiation: {0}", curr_param.get_id().get_name()));
					break;
				}
			}
			break;
		}
		default:
			param.type_error("set value", "EXTERNAL.identification.context-negotiation");
			break;
		}
	}

	@Override
	public void set_implicit_omit() {
		if (presentation__context__id.is_bound()) {
			presentation__context__id.set_implicit_omit();
		}
		if (transfer__syntax.is_bound()) {
			transfer__syntax.set_implicit_omit();
		}
	}

	@Override
	public void encode_text(final Text_Buf text_buf) {
		presentation__context__id.encode_text(text_buf);
		transfer__syntax.encode_text(text_buf);
	}

	@Override
	public void decode_text(final Text_Buf text_buf) {
		presentation__context__id.decode_text(text_buf);
		transfer__syntax.decode_text(text_buf);
	}

	@Override
	public void encode(final TTCN_Typedescriptor p_td, final TTCN_Buffer p_buf, final coding_type p_coding, final int flavour) {
		switch (p_coding) {
		case CT_RAW: {
			final TTCN_EncDec_ErrorContext errorContext = new TTCN_EncDec_ErrorContext("While RAW-encoding type '%s': ", p_td.name);
			try{
				if (p_td.raw == null) {
					TTCN_EncDec_ErrorContext.error_internal("No RAW descriptor available for type '%s'.", p_td.name);
				}
				final RAW_enc_tr_pos tree_position = new RAW_enc_tr_pos(0, null);
				final RAW_enc_tree root = new RAW_enc_tree(false, null, tree_position, 1, p_td.raw);
				RAW_encode(p_td, root);
				root.put_to_buf(p_buf);
			} finally {
				errorContext.leave_context();
			}
			break;
		}
		case CT_JSON: {
			final TTCN_EncDec_ErrorContext errorContext = new TTCN_EncDec_ErrorContext("While JSON-encoding type '%s': ", p_td.name);
			try{
				if(p_td.json == null) {
					TTCN_EncDec_ErrorContext.error_internal("No JSON descriptor available for type '%s'.", p_td.name);
				}
				final JSON_Tokenizer tok = new JSON_Tokenizer(flavour != 0);
				JSON_encode(p_td, tok);
				p_buf.put_s(tok.get_buffer().toString().getBytes());
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
	public void decode(final TTCN_Typedescriptor p_td, final TTCN_Buffer p_buf, final coding_type p_coding, final int flavour) {
		switch (p_coding) {
		case CT_RAW: {
			final TTCN_EncDec_ErrorContext errorContext = new TTCN_EncDec_ErrorContext("While RAW-decoding type '%s': ", p_td.name);
			try{
				if (p_td.raw == null) {
					TTCN_EncDec_ErrorContext.error_internal("No RAW descriptor available for type '%s'.", p_td.name);
				}
				final raw_order_t order = p_td.raw.top_bit_order == top_bit_order_t.TOP_BIT_LEFT ? raw_order_t.ORDER_LSB : raw_order_t.ORDER_MSB;
				final int rawr = RAW_decode(p_td, p_buf, p_buf.get_len() * 8, order);
				if (rawr < 0) {
					final error_type temp = error_type.values()[-rawr];
					switch (temp) {
					case ET_INCOMPL_MSG:
					case ET_LEN_ERR:
						TTCN_EncDec_ErrorContext.error(temp, "Can not decode type '%s', because invalid or incomplete message was received", p_td.name);
						break;
					case ET_UNBOUND:
					default:
						TTCN_EncDec_ErrorContext.error(error_type.ET_INVAL_MSG, "Can not decode type '%s', because invalid or incomplete message was received", p_td.name);
						break;
					}
				}
			} finally {
				errorContext.leave_context();
			}
			break;
		}
		case CT_JSON: {
			final TTCN_EncDec_ErrorContext errorContext = new TTCN_EncDec_ErrorContext("While JSON-decoding type '%s': ", p_td.name);
			try{
				if(p_td.json == null) {
					TTCN_EncDec_ErrorContext.error_internal("No JSON descriptor available for type '%s'.", p_td.name);
				}
				final JSON_Tokenizer tok = new JSON_Tokenizer(new String(p_buf.get_data()), p_buf.get_len());
				if(JSON_decode(p_td, tok, false) < 0) {
					TTCN_EncDec_ErrorContext.error(error_type.ET_INCOMPL_MSG, "Can not decode type '%s', because invalid or incomplete message was received", p_td.name);
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
			TTCN_EncDec_ErrorContext.error(error_type.ET_UNBOUND, "Encoding an unbound value of type EXTERNAL.identification.context-negotiation.");
			return -1;
		}

		int enc_len = p_tok.put_next_token(json_token_t.JSON_TOKEN_OBJECT_START, null);

		{
			enc_len += p_tok.put_next_token(json_token_t.JSON_TOKEN_NAME, "presentation-context-id");
			enc_len += get_field_presentation__context__id().JSON_encode(TitanInteger.TitanInteger_descr_, p_tok);
		}

		{
			enc_len += p_tok.put_next_token(json_token_t.JSON_TOKEN_NAME, "transfer-syntax");
			enc_len += get_field_transfer__syntax().JSON_encode(TitanObjectid.TitanObjectid_descr_, p_tok);
		}

		enc_len += p_tok.put_next_token(json_token_t.JSON_TOKEN_OBJECT_END, null);
		return enc_len;
	}

	@Override
	/** {@inheritDoc} */
	public int JSON_decode(final TTCN_Typedescriptor p_td, final JSON_Tokenizer p_tok, final boolean p_silent, final boolean p_parent_is_map, final int p_chosen_field) {
		final AtomicReference<json_token_t> j_token = new AtomicReference<json_token_t>(json_token_t.JSON_TOKEN_NONE);
		int dec_len = p_tok.get_next_token(j_token, null, null);
		if (json_token_t.JSON_TOKEN_ERROR == j_token.get()) {
			JSON_ERROR(p_silent, error_type.ET_INVAL_MSG, JSON.JSON_DEC_BAD_TOKEN_ERROR, "");
			return JSON.JSON_ERROR_FATAL;
		}
		else if (json_token_t.JSON_TOKEN_OBJECT_START != j_token.get()) {
			return JSON.JSON_ERROR_INVALID_TOKEN;
		}
		boolean presentation__context__id_found = false;
		boolean transfer__syntax_found = false;

		while (true) {
			final StringBuilder fld_name = new StringBuilder();
			final AtomicInteger name_len = new AtomicInteger(0);
			int buf_pos = p_tok.get_buf_pos();
			dec_len += p_tok.get_next_token(j_token, fld_name, name_len);
			if (json_token_t.JSON_TOKEN_ERROR == j_token.get()) {
				JSON_ERROR(p_silent, error_type.ET_INVAL_MSG, JSON.JSON_DEC_NAME_TOKEN_ERROR);
				return JSON.JSON_ERROR_FATAL;
			}
			else if (json_token_t.JSON_TOKEN_NAME != j_token.get()) {
				p_tok.set_buf_pos(buf_pos);
				break;
			}
			else {
				if (23 == name_len.get() && "presentation-context-id".equals(fld_name.substring(0,name_len.get()))) {
					presentation__context__id_found = true;
					final int ret_val = get_field_presentation__context__id().JSON_decode(TitanInteger.TitanInteger_descr_, p_tok, p_silent);
					if (0 > ret_val) {
						if (JSON.JSON_ERROR_INVALID_TOKEN == ret_val) {
							JSON_ERROR(p_silent, error_type.ET_INVAL_MSG, JSON.JSON_DEC_FIELD_TOKEN_ERROR, "presentation-context-id");
						}
						return JSON.JSON_ERROR_FATAL;
					}
					dec_len += ret_val;
				}
				else if (15 == name_len.get() && "transfer-syntax".equals(fld_name.substring(0,name_len.get()))) {
					transfer__syntax_found = true;
					final int ret_val = get_field_transfer__syntax().JSON_decode(TitanObjectid.TitanObjectid_descr_, p_tok, p_silent);
					if (0 > ret_val) {
						if (JSON.JSON_ERROR_INVALID_TOKEN == ret_val) {
							JSON_ERROR(p_silent, error_type.ET_INVAL_MSG, JSON.JSON_DEC_FIELD_TOKEN_ERROR, "transfer-syntax");
						}
						return JSON.JSON_ERROR_FATAL;
					}
					dec_len += ret_val;
				}
				else {
					JSON_ERROR(p_silent, error_type.ET_INVAL_MSG, JSON.JSON_DEC_INVALID_NAME_ERROR, fld_name);
					dec_len += p_tok.get_next_token(j_token, null, null);
					if (json_token_t.JSON_TOKEN_NUMBER != j_token.get() && json_token_t.JSON_TOKEN_STRING != j_token.get() &&
							json_token_t.JSON_TOKEN_LITERAL_TRUE != j_token.get() && json_token_t.JSON_TOKEN_LITERAL_FALSE != j_token.get() &&
							json_token_t.JSON_TOKEN_LITERAL_NULL != j_token.get()) {
						JSON_ERROR(p_silent, error_type.ET_INVAL_MSG, JSON.JSON_DEC_FIELD_TOKEN_ERROR, fld_name);
						return JSON.JSON_ERROR_FATAL;
					}
				}
			}
		}

		dec_len += p_tok.get_next_token(j_token, null, null);
		if (json_token_t.JSON_TOKEN_OBJECT_END != j_token.get()) {
			JSON_ERROR(p_silent, error_type.ET_INVAL_MSG, JSON.JSON_DEC_OBJECT_END_TOKEN_ERROR, "");
			return JSON.JSON_ERROR_FATAL;
		}

		if (!presentation__context__id_found) {
			JSON_ERROR(p_silent, error_type.ET_INVAL_MSG, JSON.JSON_DEC_MISSING_FIELD_ERROR, "presentation-context-id");
			return JSON.JSON_ERROR_FATAL;
		}
		if (!transfer__syntax_found) {
			JSON_ERROR(p_silent, error_type.ET_INVAL_MSG, JSON.JSON_DEC_MISSING_FIELD_ERROR, "transfer-syntax");
			return JSON.JSON_ERROR_FATAL;
		}

		return dec_len;
	}

	private static void JSON_ERROR(final boolean p_silent, final error_type p_et, final String fmt, final java.lang.Object... args) {
		if (!p_silent) {
			TTCN_EncDec_ErrorContext.error(p_et, fmt, args);
		}
	}
	/**
	 * The encoder function for type EXTERNAL.identification.context-negotiation.
	 *
	 * @param input_value
	 *                the input value to encode.
	 * @param output_stream
	 *                the octetstring to be extend with the result of the
	 *                encoding.
	 * @param coding_name
	 *                the name of the coding to use.
	 * */
	public static void TitanExternal_identification_context__negotiation_encoder(final TitanExternal_identification_context__negotiation input_value, final TitanOctetString output_stream, final TitanUniversalCharString coding_name) {
		final AtomicInteger extra_options = new AtomicInteger(0);
		final TTCN_EncDec.coding_type codingType = TTCN_EncDec.get_coding_from_str(coding_name, extra_options, true);
		if (codingType != TTCN_EncDec.coding_type.CT_JSON) {
			TTCN_Logger.begin_event_log2str();
			coding_name.log();
			throw new TtcnError(MessageFormat.format("Type `EXTERNAL.identification.context-negotiation' does not support {0} encoding", TTCN_Logger.end_event_log2str()));
		}
		final TTCN_Buffer ttcnBuffer = new TTCN_Buffer();
		input_value.encode(TitanExternal_identification_context__negotiation.TitanExternal_identification_context__negotiation_descr_, ttcnBuffer, codingType, extra_options.get());
		ttcnBuffer.get_string(output_stream);
	}

	/**
	 * The decoder function for type EXTERNAL.identification.context-negotiation. In case
	 * of successful decoding the bits used for decoding are removed from
	 * the beginning of the input_stream.
	 *
	 * @param input_stream
	 *                the octetstring starting with the value to be decoded.
	 * @param output_value
	 *                the decoded value if the decoding was successful.
	 * @param coding_name
	 *                the name of the coding to use.
	 * @return 0 if nothing could be decoded, 1 in case of success, 2 in
	 *         case of error (incomplete message or length)
	 * */
	public static TitanInteger TitanExternal_identification_context__negotiation_decoder( final TitanOctetString input_stream, final TitanExternal_identification_context__negotiation output_value, final TitanUniversalCharString coding_name) {
		final AtomicInteger extra_options = new AtomicInteger(0);
		final TTCN_EncDec.coding_type codingType = TTCN_EncDec.get_coding_from_str(coding_name, extra_options, false);
		if (codingType != TTCN_EncDec.coding_type.CT_JSON) {
			TTCN_Logger.begin_event_log2str();
			coding_name.log();
			throw new TtcnError(MessageFormat.format("Type `EXTERNAL.identification.context-negotiation' does not support {0} encoding", TTCN_Logger.end_event_log2str()));
		}
		final TTCN_Buffer ttcnBuffer = new TTCN_Buffer(input_stream);
		output_value.decode(TitanExternal_identification_context__negotiation.TitanExternal_identification_context__negotiation_descr_, ttcnBuffer, codingType, extra_options.get());
		switch (TTCN_EncDec.get_last_error_type()) {
		case ET_NONE:
			ttcnBuffer.cut();
			ttcnBuffer.get_string(input_stream);
			return new TitanInteger(0);
		case ET_INCOMPL_MSG:
		case ET_LEN_ERR:
			return new TitanInteger(2);
		default:
			return new TitanInteger(1);
		}
	}

	/**
	 * The encoder function for type objid.
	 *
	 * @param input_value
	 *                the input value to encode.
	 * @param output_stream
	 *                the octetstring to be extend with the result of the
	 *                encoding.
	 * @param coding_name
	 *                the name of the coding to use.
	 * */
	public static void TitanExternal_identification_context__negotiation_transfer__syntax_encoder(final TitanObjectid input_value, final TitanOctetString output_stream, final TitanUniversalCharString coding_name) {
		final AtomicInteger extra_options = new AtomicInteger(0);
		final TTCN_EncDec.coding_type codingType = TTCN_EncDec.get_coding_from_str(coding_name, extra_options, true);
		if (codingType != TTCN_EncDec.coding_type.CT_JSON) {
			TTCN_Logger.begin_event_log2str();
			coding_name.log();
			throw new TtcnError(MessageFormat.format("Type `objid' does not support {0} encoding", TTCN_Logger.end_event_log2str()));
		}
		final TTCN_Buffer ttcnBuffer = new TTCN_Buffer();
		input_value.encode(TitanObjectid.TitanObjectid_descr_, ttcnBuffer, codingType, extra_options.get());
		ttcnBuffer.get_string(output_stream);
	}

	/**
	 * The decoder function for type objid. In case
	 * of successful decoding the bits used for decoding are removed from
	 * the beginning of the input_stream.
	 *
	 * @param input_stream
	 *                the octetstring starting with the value to be decoded.
	 * @param output_value
	 *                the decoded value if the decoding was successful.
	 * @param coding_name
	 *                the name of the coding to use.
	 * @return 0 if nothing could be decoded, 1 in case of success, 2 in
	 *         case of error (incomplete message or length)
	 * */
	public static TitanInteger TitanExternal_identification_context__negotiation_transfer__syntax_decoder( final TitanOctetString input_stream, final TitanObjectid output_value, final TitanUniversalCharString coding_name) {
		final AtomicInteger extra_options = new AtomicInteger(0);
		final TTCN_EncDec.coding_type codingType = TTCN_EncDec.get_coding_from_str(coding_name, extra_options, false);
		if (codingType != TTCN_EncDec.coding_type.CT_JSON) {
			TTCN_Logger.begin_event_log2str();
			coding_name.log();
			throw new TtcnError(MessageFormat.format("Type `objid' does not support {0} encoding", TTCN_Logger.end_event_log2str()));
		}
		final TTCN_Buffer ttcnBuffer = new TTCN_Buffer(input_stream);
		output_value.decode(TitanObjectid.TitanObjectid_descr_, ttcnBuffer, codingType, extra_options.get());
		switch (TTCN_EncDec.get_last_error_type()) {
		case ET_NONE:
			ttcnBuffer.cut();
			ttcnBuffer.get_string(input_stream);
			return new TitanInteger(0);
		case ET_INCOMPL_MSG:
		case ET_LEN_ERR:
			return new TitanInteger(2);
		default:
			return new TitanInteger(1);
		}
	}

}