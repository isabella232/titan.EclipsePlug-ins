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
import org.eclipse.titan.runtime.core.JSON.json_string_escaping;
import org.eclipse.titan.runtime.core.JSON_Tokenizer.json_token_t;
import org.eclipse.titan.runtime.core.Param_Types.Module_Parameter;
import org.eclipse.titan.runtime.core.RAW.RAW_enc_tr_pos;
import org.eclipse.titan.runtime.core.RAW.RAW_enc_tree;
import org.eclipse.titan.runtime.core.RAW.top_bit_order_t;
import org.eclipse.titan.runtime.core.TTCN_EncDec.coding_type;
import org.eclipse.titan.runtime.core.TTCN_EncDec.error_type;
import org.eclipse.titan.runtime.core.TTCN_EncDec.raw_order_t;

/**
 * Part of the representation of the ASN.1 unrestricted string (CHARACTER STRING) type.
 *
 * @author Kristof Szabados
 * @author Arpad Lovassy
 */
public class TitanCharacter_String_identification_syntaxes extends Base_Type {
	public static final TTCN_JSONdescriptor TitanCharacter_String_identification_syntaxes_json_ = new TTCN_JSONdescriptor(false, null, false, null, false, false, false, 0, null, false, json_string_escaping.ESCAPE_AS_SHORT);
	public static final TTCN_Typedescriptor TitanCharacter_String_identification_syntaxes_descr_ = new TTCN_Typedescriptor("CHARACTER STRING.identification.syntaxes", TitanCharacter_String_identification_syntaxes.TitanCharacter_String_identification_syntaxes_json_);
	public static final TitanUniversalCharString TitanCharacter_String_identification_syntaxes_default_coding = new TitanUniversalCharString("JSON");
	public static final TTCN_Typedescriptor TitanCharacter_String_identification_syntaxes_abstract__descr_ = new TTCN_Typedescriptor("CHARACTER STRING.identification.syntaxes.abstract", TitanObjectid.TitanObjectid_json_);
	public static final TitanUniversalCharString TitanCharacter_String_identification_syntaxes_abstract__default_coding = new TitanUniversalCharString("JSON");
	public static final TTCN_Typedescriptor TitanCharacter_String_identification_syntaxes_transfer_descr_ = new TTCN_Typedescriptor("CHARACTER STRING.identification.syntaxes.transfer", TitanObjectid.TitanObjectid_json_);
	public static final TitanUniversalCharString TitanCharacter_String_identification_syntaxes_transfer_default_coding = new TitanUniversalCharString("JSON");
	private final TitanObjectid abstract_; //ObjectID_Type
	private final TitanObjectid transfer; //ObjectID_Type

	/**
	 * Initializes to unbound value.
	 * */
	public TitanCharacter_String_identification_syntaxes() {
		this.abstract_ = new TitanObjectid();
		this.transfer = new TitanObjectid();
	}

	/**
	 * Initializes from given field values. The number of arguments equals
	 * to the number of fields.
	 *
	 * @param abstract_
	 *                the value of field abstract
	 * @param transfer
	 *                the value of field transfer
	 * */
	public TitanCharacter_String_identification_syntaxes(final TitanObjectid abstract_, final TitanObjectid transfer ) {
		this.abstract_ = new TitanObjectid( abstract_ );
		this.transfer = new TitanObjectid( transfer );
	}

	/**
	 * Initializes to a given value.
	 *
	 * @param otherValue
	 *                the value to initialize to.
	 * */
	public TitanCharacter_String_identification_syntaxes( final TitanCharacter_String_identification_syntaxes otherValue) {
		otherValue.must_bound("Copying of an unbound value of type CHARACTER STRING.identification.syntaxes.");
		abstract_ = new TitanObjectid();
		transfer = new TitanObjectid();
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
	public TitanCharacter_String_identification_syntaxes operator_assign(final TitanCharacter_String_identification_syntaxes otherValue ) {
		otherValue.must_bound( "Assignment of an unbound value of type CHARACTER STRING.identification.syntaxes");
		if (otherValue != this) {
			if ( otherValue.get_field_abstract_().is_bound() ) {
				this.abstract_.operator_assign( otherValue.get_field_abstract_() );
			} else {
				this.abstract_.clean_up();
			}
			if ( otherValue.get_field_transfer().is_bound() ) {
				this.transfer.operator_assign( otherValue.get_field_transfer() );
			} else {
				this.transfer.clean_up();
			}
		}

		return this;
	}

	@Override
	public TitanCharacter_String_identification_syntaxes operator_assign(final Base_Type otherValue) {
		if (otherValue instanceof TitanCharacter_String_identification_syntaxes ) {
			return operator_assign((TitanCharacter_String_identification_syntaxes) otherValue);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to CHARACTER STRING.identification.syntaxes", otherValue));
	}

	@Override
	public void clean_up() {
		abstract_.clean_up();
		transfer.clean_up();
	}

	@Override
	public boolean is_bound() {
		return abstract_.is_bound()
				|| transfer.is_bound();
	}

	@Override
	public boolean is_present() {
		return is_bound();
	}

	@Override
	public boolean is_value() {
		return abstract_.is_value()
				&& transfer.is_value();
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
	public boolean operator_equals( final TitanCharacter_String_identification_syntaxes other_value) {
		return abstract_.operator_equals( other_value.abstract_ )
				&& transfer.operator_equals( other_value.transfer );
	}

	@Override
	public boolean operator_equals(final Base_Type other_value) {
		if (other_value instanceof TitanCharacter_String_identification_syntaxes ) {
			return operator_equals((TitanCharacter_String_identification_syntaxes) other_value);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to CHARACTER STRING.identification.syntaxes", other_value));
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
	public boolean operator_not_equals( final TitanCharacter_String_identification_syntaxes other_value) {
		return !operator_equals(other_value);
	}
	/**
	 * Gives access to the field abstract.
	 *
	 * @return the field abstract.
	 * */
	public TitanObjectid get_field_abstract_() {
		return abstract_;
	}

	/**
	 * Gives read-only access to the field abstract.
	 *
	 * @return the field abstract.
	 * */
	public TitanObjectid constGet_field_abstract_() {
		return abstract_;
	}

	/**
	 * Gives access to the field transfer.
	 *
	 * @return the field transfer.
	 * */
	public TitanObjectid get_field_transfer() {
		return transfer;
	}

	/**
	 * Gives read-only access to the field transfer.
	 *
	 * @return the field transfer.
	 * */
	public TitanObjectid constGet_field_transfer() {
		return transfer;
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
		TTCN_Logger.log_event_str(" abstract := ");
		abstract_.log();
		TTCN_Logger.log_char(',');
		TTCN_Logger.log_event_str(" transfer := ");
		transfer.log();
		TTCN_Logger.log_event_str(" }");
	}

	@Override
	public void set_param(final Module_Parameter param) {
		param.basic_check(Module_Parameter.basic_check_bits_t.BC_VALUE.getValue(), "set value");
		switch (param.get_type()) {
		case MP_Value_List:
			if (param.get_size() > 2) {
				param.error(MessageFormat.format("set value of type CHARACTER STRING.identification.syntaxes has 2 fields but list value has {0} fields.", param.get_size()));
			}
			if (param.get_size() > 0 && param.get_elem(0).get_type() != Module_Parameter.type_t.MP_NotUsed) {
				get_field_abstract_().set_param(param.get_elem(0));
			}
			if (param.get_size() > 1 && param.get_elem(1).get_type() != Module_Parameter.type_t.MP_NotUsed) {
				get_field_transfer().set_param(param.get_elem(1));
			}
			break;
		case MP_Assignment_List: {
			final boolean value_used[] = new boolean[param.get_size()];
			for (int val_idx = 0; val_idx < param.get_size(); val_idx++) {
				final Module_Parameter curr_param = param.get_elem(val_idx);
				if ("abstract".equals(curr_param.get_id().get_name())) {
					if (curr_param.get_type() != Module_Parameter.type_t.MP_NotUsed) {
						get_field_abstract_().set_param(curr_param);
					}
					value_used[val_idx] = true;
				}
			}
			for (int val_idx = 0; val_idx < param.get_size(); val_idx++) {
				final Module_Parameter curr_param = param.get_elem(val_idx);
				if ("transfer".equals(curr_param.get_id().get_name())) {
					if (curr_param.get_type() != Module_Parameter.type_t.MP_NotUsed) {
						get_field_transfer().set_param(curr_param);
					}
					value_used[val_idx] = true;
				}
			}
			for (int val_idx = 0; val_idx < param.get_size(); val_idx++) {
				if (!value_used[val_idx]) {
					final Module_Parameter curr_param = param.get_elem(val_idx);
					curr_param.error(MessageFormat.format("Non existent field name in type CHARACTER STRING.identification.syntaxes: {0}", curr_param.get_id().get_name()));
					break;
				}
			}
			break;
		}
		default:
			param.type_error("set value", "CHARACTER STRING.identification.syntaxes");
			break;
		}
	}

	@Override
	public void set_implicit_omit() {
		if (abstract_.is_bound()) {
			abstract_.set_implicit_omit();
		}
		if (transfer.is_bound()) {
			transfer.set_implicit_omit();
		}
	}

	@Override
	public void encode_text(final Text_Buf text_buf) {
		abstract_.encode_text(text_buf);
		transfer.encode_text(text_buf);
	}

	@Override
	public void decode_text(final Text_Buf text_buf) {
		abstract_.decode_text(text_buf);
		transfer.decode_text(text_buf);
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
				final byte[] data = p_buf.get_data();
				final char[] temp = new char[data.length];
				for (int i = 0; i < data.length; i++) {
					temp[i] = (char)data[i];
				}
				final JSON_Tokenizer tok = new JSON_Tokenizer(new String(temp), p_buf.get_len());
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
			TTCN_EncDec_ErrorContext.error(error_type.ET_UNBOUND, "Encoding an unbound value of type CHARACTER STRING.identification.syntaxes.");
			return -1;
		}

		int enc_len = p_tok.put_next_token(json_token_t.JSON_TOKEN_OBJECT_START, null);

		{
			enc_len += p_tok.put_next_token(json_token_t.JSON_TOKEN_NAME, "abstract");
			enc_len += get_field_abstract_().JSON_encode(TitanObjectid.TitanObjectid_descr_, p_tok);
		}

		{
			enc_len += p_tok.put_next_token(json_token_t.JSON_TOKEN_NAME, "transfer");
			enc_len += get_field_transfer().JSON_encode(TitanObjectid.TitanObjectid_descr_, p_tok);
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
		boolean abstract__found = false;
		boolean transfer_found = false;

		while (true) {
			final StringBuilder fld_name = new StringBuilder();
			final AtomicInteger name_len = new AtomicInteger(0);
			final int buf_pos = p_tok.get_buf_pos();
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
				if (8 == name_len.get() && "abstract".equals(fld_name.substring(0,name_len.get()))) {
					abstract__found = true;
					final int ret_val = get_field_abstract_().JSON_decode(TitanObjectid.TitanObjectid_descr_, p_tok, p_silent, false);
					if (0 > ret_val) {
						if (JSON.JSON_ERROR_INVALID_TOKEN == ret_val) {
							JSON_ERROR(p_silent, error_type.ET_INVAL_MSG, JSON.JSON_DEC_FIELD_TOKEN_ERROR, "abstract");
						}
						return JSON.JSON_ERROR_FATAL;
					}
					dec_len += ret_val;
				}
				else if (8 == name_len.get() && "transfer".equals(fld_name.substring(0,name_len.get()))) {
					transfer_found = true;
					final int ret_val = get_field_transfer().JSON_decode(TitanObjectid.TitanObjectid_descr_, p_tok, p_silent, false);
					if (0 > ret_val) {
						if (JSON.JSON_ERROR_INVALID_TOKEN == ret_val) {
							JSON_ERROR(p_silent, error_type.ET_INVAL_MSG, JSON.JSON_DEC_FIELD_TOKEN_ERROR, "transfer");
						}
						return JSON.JSON_ERROR_FATAL;
					}
					dec_len += ret_val;
				}
				else {
					if (p_silent) {
						return JSON.JSON_ERROR_INVALID_TOKEN;
					}
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

		if (!abstract__found) {
			JSON_ERROR(p_silent, error_type.ET_INVAL_MSG, JSON.JSON_DEC_MISSING_FIELD_ERROR, "abstract");
			return JSON.JSON_ERROR_FATAL;
		}
		if (!transfer_found) {
			JSON_ERROR(p_silent, error_type.ET_INVAL_MSG, JSON.JSON_DEC_MISSING_FIELD_ERROR, "transfer");
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
	 * The encoder function for type CHARACTER STRING.identification.syntaxes.
	 *
	 * @param input_value
	 *                the input value to encode.
	 * @param output_stream
	 *                the octetstring to be extend with the result of the
	 *                encoding.
	 * @param coding_name
	 *                the name of the coding to use.
	 * */
	public static void TitanCharacter_String_identification_syntaxes_encoder(final TitanCharacter_String_identification_syntaxes input_value, final TitanOctetString output_stream, final TitanUniversalCharString coding_name) {
		final AtomicInteger extra_options = new AtomicInteger(0);
		final TTCN_EncDec.coding_type codingType = TTCN_EncDec.get_coding_from_str(coding_name, extra_options, true);
		if (codingType != TTCN_EncDec.coding_type.CT_JSON) {
			TTCN_Logger.begin_event_log2str();
			coding_name.log();
			throw new TtcnError(MessageFormat.format("Type `CHARACTER STRING.identification.syntaxes' does not support {0} encoding", TTCN_Logger.end_event_log2str()));
		}
		final TTCN_Buffer ttcnBuffer = new TTCN_Buffer();
		input_value.encode(TitanCharacter_String_identification_syntaxes.TitanCharacter_String_identification_syntaxes_descr_, ttcnBuffer, codingType, extra_options.get());
		ttcnBuffer.get_string(output_stream);
	}

	/**
	 * The decoder function for type CHARACTER STRING.identification.syntaxes. In case
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
	public static int TitanCharacter_String_identification_syntaxes_decoder(final TitanOctetString input_stream, final TitanCharacter_String_identification_syntaxes output_value, final TitanUniversalCharString coding_name) {
		final AtomicInteger extra_options = new AtomicInteger(0);
		final TTCN_EncDec.coding_type codingType = TTCN_EncDec.get_coding_from_str(coding_name, extra_options, false);
		if (codingType != TTCN_EncDec.coding_type.CT_JSON) {
			TTCN_Logger.begin_event_log2str();
			coding_name.log();
			throw new TtcnError(MessageFormat.format("Type `CHARACTER STRING.identification.syntaxes' does not support {0} encoding", TTCN_Logger.end_event_log2str()));
		}
		final TTCN_Buffer ttcnBuffer = new TTCN_Buffer(input_stream);
		output_value.decode(TitanCharacter_String_identification_syntaxes.TitanCharacter_String_identification_syntaxes_descr_, ttcnBuffer, codingType, extra_options.get());
		switch (TTCN_EncDec.get_last_error_type()) {
		case ET_NONE:
			ttcnBuffer.cut();
			ttcnBuffer.get_string(input_stream);
			return 0;
		case ET_INCOMPL_MSG:
		case ET_LEN_ERR:
			return 2;
		default:
			return 1;
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
	public static void TitanCharacter_String_identification_syntaxes_abstract__encoder(final TitanObjectid input_value, final TitanOctetString output_stream, final TitanUniversalCharString coding_name) {
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
	public static int TitanCharacter_String_identification_syntaxes_abstract__decoder(final TitanOctetString input_stream, final TitanObjectid output_value, final TitanUniversalCharString coding_name) {
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
			return 0;
		case ET_INCOMPL_MSG:
		case ET_LEN_ERR:
			return 2;
		default:
			return 1;
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
	public static void TitanCharacter_String_identification_syntaxes_transfer_encoder(final TitanObjectid input_value, final TitanOctetString output_stream, final TitanUniversalCharString coding_name) {
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
	public static int TitanCharacter_String_identification_syntaxes_transfer_decoder(final TitanOctetString input_stream, final TitanObjectid output_value, final TitanUniversalCharString coding_name) {
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
			return 0;
		case ET_INCOMPL_MSG:
		case ET_LEN_ERR:
			return 2;
		default:
			return 1;
		}
	}

}