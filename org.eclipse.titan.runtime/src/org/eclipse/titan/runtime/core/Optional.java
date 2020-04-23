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

import org.eclipse.titan.runtime.core.Base_Template.template_sel;
import org.eclipse.titan.runtime.core.JSON_Tokenizer.json_token_t;
import org.eclipse.titan.runtime.core.Param_Types.Module_Param_Name;
import org.eclipse.titan.runtime.core.Param_Types.Module_Param_Omit;
import org.eclipse.titan.runtime.core.Param_Types.Module_Param_Unbound;
import org.eclipse.titan.runtime.core.Param_Types.Module_Parameter;
import org.eclipse.titan.runtime.core.Param_Types.Module_Parameter.type_t;
import org.eclipse.titan.runtime.core.TTCN_EncDec.coding_type;

/**
 * TTCN-3 boolean
 * @author Kristof Szabados
 * @author Arpad Lovassy
 */
public final class Optional<TYPE extends Base_Type> extends Base_Type {
	public enum optional_sel { OPTIONAL_UNBOUND, OPTIONAL_OMIT, OPTIONAL_PRESENT };

	private TYPE optionalValue;

	private optional_sel optionalSelection;

	private final Class<TYPE> clazz;

	public Optional(final Class<TYPE> clazz) {
		optionalValue = null;
		optionalSelection = optional_sel.OPTIONAL_UNBOUND;
		this.clazz = clazz;
	}

	public Optional(final Class<TYPE> clazz, final template_sel otherValue) {
		if (otherValue != template_sel.OMIT_VALUE) {
			throw new TtcnError("Setting an optional field to an invalid value.");
		}
		optionalValue = null;
		optionalSelection = optional_sel.OPTIONAL_OMIT;
		this.clazz = clazz;
	}

	public Optional(final Optional<TYPE> otherValue) {
		//super(otherValue);
		optionalValue = null;
		optionalSelection = otherValue.optionalSelection;
		clazz = otherValue.clazz;
		if (optional_sel.OPTIONAL_PRESENT.equals(otherValue.optionalSelection)) {
			try {
				optionalValue = clazz.newInstance();
			} catch (Exception e) {
				throw new TtcnError(MessageFormat.format("Internal Error: exception `{0}'' thrown while instantiating class of `{1}'' type", e.getMessage(), clazz.getName()));
			}

			optionalValue.operator_assign(otherValue.optionalValue);
		}
	}

	@Override
	public void clean_up() {
		if (optional_sel.OPTIONAL_PRESENT.equals(optionalSelection)) {
			optionalValue = null;
		}
		optionalSelection = optional_sel.OPTIONAL_UNBOUND;
	}

	/**
	 * Sets the current selection to be omit.
	 * Any other parameter causes dynamic testcase error.
	 *<p>
	 * operator= in the core.
	 *
	 * @param otherValue
	 *                the other value to assign.
	 * @return the new value object.
	 */
	public Optional<TYPE> operator_assign(final template_sel otherValue) {
		if (!template_sel.OMIT_VALUE.equals(otherValue)) {
			throw new TtcnError("Internal error: Setting an optional field to an invalid value.");
		}
		set_to_omit();
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
	public Optional<TYPE> operator_assign(final Optional<TYPE> otherValue) {
		switch (otherValue.optionalSelection) {
		case OPTIONAL_PRESENT:
			if (optional_sel.OPTIONAL_PRESENT.equals(optionalSelection)) {
				optionalValue.operator_assign(otherValue.optionalValue);
			} else {
				try {
					optionalValue = clazz.newInstance();
				} catch (Exception e) {
					throw new TtcnError(MessageFormat.format("Internal Error: exception `{0}'' thrown while instantiating class of `{1}'' type", e.getMessage(), clazz.getName()));
				}
				optionalValue.operator_assign(otherValue.optionalValue);
				optionalSelection = optional_sel.OPTIONAL_PRESENT;
			}
			break;
		case OPTIONAL_OMIT:
			if (otherValue != this) {
				set_to_omit();
			}
			break;
		default:
			clean_up();
			break;
		}

		return this;
	}

	@Override
	public Optional<TYPE> operator_assign(final Base_Type otherValue) {
		if (!(otherValue instanceof Optional<?>)) {
			if (optional_sel.OPTIONAL_PRESENT.equals(optionalSelection)) {
				optionalValue.operator_assign(otherValue);
			} else {
				try {
					optionalValue = clazz.newInstance();
				} catch (Exception e) {
					throw new TtcnError(MessageFormat.format("Internal Error: exception `{0}'' thrown while instantiating class of `{1}'' type", e.getMessage(), clazz.getName()));
				}
				optionalValue.operator_assign(otherValue);
				optionalSelection = optional_sel.OPTIONAL_PRESENT;
			}
			return this;
		}

		final Optional<?> optionalOther = (Optional<?>)otherValue;
		switch (optionalOther.optionalSelection) {
		case OPTIONAL_PRESENT:
			if (optional_sel.OPTIONAL_PRESENT.equals(optionalSelection)) {
				optionalValue.operator_assign(optionalOther.optionalValue);
			} else {
				try {
					optionalValue = clazz.newInstance();
				} catch (Exception e) {
					throw new TtcnError(MessageFormat.format("Internal Error: exception `{0}'' thrown while instantiating class of `{1}'' type", e.getMessage(), clazz.getName()));
				}
				optionalValue.operator_assign(optionalOther.optionalValue);
				optionalSelection = optional_sel.OPTIONAL_PRESENT;
			}
			break;
		case OPTIONAL_OMIT:
			if (optionalOther != this) {
				set_to_omit();
			}
			break;
		default:
			clean_up();
			break;
		}

		return this;
	}

	public void set_to_present() {
		if (!optional_sel.OPTIONAL_PRESENT.equals(optionalSelection)) {
			optionalSelection = optional_sel.OPTIONAL_PRESENT;
			try {
				optionalValue = clazz.newInstance();
			} catch (Exception e) {
				throw new TtcnError(MessageFormat.format("Internal Error: exception `{0}'' thrown while instantiating class of `{1}'' type", e.getMessage(), clazz.getName()));
			}
		}
	}

	public void set_to_omit() {
		if (optional_sel.OPTIONAL_PRESENT.equals(optionalSelection)) {
			optionalValue = null;
		}
		optionalSelection = optional_sel.OPTIONAL_OMIT;
	}

	public optional_sel get_selection() {
		return optionalSelection;
	}

	@Override
	public void log() {
		switch (optionalSelection) {
		case OPTIONAL_PRESENT:
			optionalValue.log();
			break;
		case OPTIONAL_OMIT:
			TTCN_Logger.log_event_str("omit");
			break;
		case OPTIONAL_UNBOUND:
			TTCN_Logger.log_event_unbound();
			break;
		}
	}

	@Override
	/** {@inheritDoc} */
	public void set_param(final Module_Parameter param) {
		if (param.get_type() == type_t.MP_Omit) {
			if (param.get_ifpresent()) {
				param.error("An optional field of a record value cannot have an 'ifpresent' attribute");
			}
			if (param.get_length_restriction() != null) {
				param.error("An optional field of a record value cannot have a length restriction");
			}
			set_to_omit();
			return;
		}
		set_to_present();
		optionalValue.set_param(param);
		if (!optionalValue.is_bound()) {
			clean_up();
		}
	}

	@Override
	/** {@inheritDoc} */
	public Module_Parameter get_param(final Module_Param_Name param_name) {
		switch (optionalSelection) {
		case OPTIONAL_PRESENT:
			return optionalValue.get_param(param_name);
		case OPTIONAL_OMIT:
			return new Module_Param_Omit();
		case OPTIONAL_UNBOUND:
		default:
			return new Module_Param_Unbound();
		}
	}

	@Override
	/** {@inheritDoc} */
	public void encode_text(final Text_Buf text_buf) {
		switch (optionalSelection) {
		case OPTIONAL_OMIT:
			text_buf.push_int(0);
			break;
		case OPTIONAL_PRESENT:
			text_buf.push_int(1);
			optionalValue.encode_text(text_buf);
			break;
		case OPTIONAL_UNBOUND:
			throw new TtcnError("Text encoder: Encoding an unbound optional value.");
		}
	}

	@Override
	/** {@inheritDoc} */
	public void decode_text(final Text_Buf text_buf) {
		clean_up();

		final int temp = text_buf.pull_int().get_int();
		if (temp == 1) {
			set_to_present();
			optionalValue.decode_text(text_buf);
		} else {
			set_to_omit();
		}
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
	/** {@inheritDoc} */
	public void decode(final TTCN_Typedescriptor p_td, final TTCN_Buffer p_buf, final coding_type p_coding, final int flavour) {
		switch (p_coding) {
		case CT_JSON: {
			final TTCN_EncDec_ErrorContext errorContext = new TTCN_EncDec_ErrorContext("While JSON-decoding type '%s': ", p_td.name);
			try {
				if(p_td.json == null) {
					TTCN_EncDec_ErrorContext.error_internal("No JSON descriptor available for type '%s'.", p_td.name);
				}

				final JSON_Tokenizer tok = new JSON_Tokenizer(new String(p_buf.get_data()), p_buf.get_len());
				if(JSON_decode(p_td, tok, false) < 0) {
					TTCN_EncDec_ErrorContext.error(TTCN_EncDec.error_type.ET_INCOMPL_MSG,
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
		//TODO
		//#ifdef TITAN_RUNTIME_2
		//switch(get_selection()) {
		//#else
		switch(optionalSelection) {
		//#endif
		case OPTIONAL_PRESENT:
			return optionalValue.JSON_encode(p_td, p_tok);
		case OPTIONAL_OMIT:
			return p_tok.put_next_token(json_token_t.JSON_TOKEN_LITERAL_NULL, null);
		case OPTIONAL_UNBOUND:
		default:
			TTCN_EncDec_ErrorContext.error(TTCN_EncDec.error_type.ET_UNBOUND, "Encoding an unbound optional value.");
			return -1;
		}
	}

	//TODO
	/*
		#ifdef TITAN_RUNTIME_2
		template<typename T_type>
		int OPTIONAL<T_type>::JSON_encode_negtest(const Erroneous_descriptor_t* p_err_descr,
				const TTCN_Typedescriptor_t& p_td,
				JSON_Tokenizer& p_tok) const 
		{
			switch (get_selection()) {
			case OPTIONAL_PRESENT:
				return optional_value->JSON_encode_negtest(p_err_descr, p_td, p_tok);
			case OPTIONAL_OMIT:
				return p_tok.put_next_token(JSON_TOKEN_LITERAL_NULL, NULL);
			case OPTIONAL_UNBOUND:
			default:
				TTCN_EncDec_ErrorContext::error(TTCN_EncDec::ET_UNBOUND,
						"Encoding an unbound optional value.");
				return -1;
			}
		}
		#endif
	 */

	@Override
	/** {@inheritDoc} */
	public int JSON_decode(final TTCN_Typedescriptor p_td, final JSON_Tokenizer p_tok, final boolean p_silent, final boolean p_parent_is_map, final int p_chosen_field) {
		// try the optional value first
		set_to_present();
		final int buf_pos = p_tok.get_buf_pos();
		int dec_len = 0;
		if (JSON.CHOSEN_FIELD_OMITTED == p_chosen_field) {
			// the attribute 'chosen' says that this field has to be omitted
			final AtomicReference<json_token_t> token = new AtomicReference<json_token_t>(json_token_t.JSON_TOKEN_NONE);
			dec_len = p_tok.get_next_token(token, null, null);
			if (json_token_t.JSON_TOKEN_LITERAL_NULL == token.get()) {
				set_to_omit();
				return dec_len;
			} else {
				if(!p_silent) {
					TTCN_EncDec_ErrorContext.error(TTCN_EncDec.error_type.ET_INVAL_MSG, JSON.JSON_DEC_CHOSEN_FIELD_NOT_NULL, "");
				}
				// if this is set to warning, return to the beginning of the value and
				// decode it as normal
				p_tok.set_buf_pos(buf_pos);
			}
		}
		dec_len = optionalValue.JSON_decode(p_td, p_tok, p_silent, false, p_chosen_field);
		if (JSON.JSON_ERROR_FATAL == dec_len) {
			if (p_silent) {
				clean_up();
			} else {
				set_to_omit();
			}
		} else if (JSON.JSON_ERROR_INVALID_TOKEN == dec_len) {
			// invalid token, rewind the buffer and check if it's a "null" (= omit)
			// this needs to be checked after the optional value, because it might also be
			// able to decode a "null" value
			p_tok.set_buf_pos(buf_pos);
			final AtomicReference<json_token_t> token = new AtomicReference<json_token_t>(json_token_t.JSON_TOKEN_NONE);
			dec_len = p_tok.get_next_token(token, null, null);
			if (json_token_t.JSON_TOKEN_LITERAL_NULL == token.get()) {
				if (0 <= p_chosen_field) {
					if(!p_silent) {
						TTCN_EncDec_ErrorContext.error(TTCN_EncDec.error_type.ET_INVAL_MSG, JSON.JSON_DEC_CHOSEN_FIELD_OMITTED_NULL, "");
					}
				}
				set_to_omit();
			} else {
				// cannot get JSON_TOKEN_ERROR here, that was already checked by the optional value
				dec_len = JSON.JSON_ERROR_INVALID_TOKEN;
			}
		}
		return dec_len;
	}

	@Override
	public boolean is_bound() {
		switch (optionalSelection) {
		case OPTIONAL_PRESENT:
		case OPTIONAL_OMIT:
			return true;
		default:
			if (null != optionalValue) {
				return optionalValue.is_bound();
			}
			return false;
		}
	}

	@Override
	public boolean is_present() {
		return optional_sel.OPTIONAL_PRESENT.equals(optionalSelection);
	}

	/**
	 * Checks if this optional value contains a value. Please note the
	 * optional value itself can be present (checked with is_present), while
	 * its value is set to omit (checked with ispresent).
	 * <p>
	 * Note: this is not the TTCN-3 ispresent(), kept for backward
	 * compatibility with the runtime and existing testports which use this
	 * version where unbound errors are caught before causing more trouble.
	 *
	 * @return {@code true} if the value in this optional value is present
	 *         (optionalSelection == OPTIONAL_PRESENT), {@code false}
	 *         otherwise.
	 * */
	public boolean ispresent() {
		switch (optionalSelection) {
		case OPTIONAL_PRESENT:
			return true;
		case OPTIONAL_OMIT:
			return false;
		default:
			throw new TtcnError("Using an unbound optional field.");
		}
	}

	@Override
	public boolean is_value() {
		return optional_sel.OPTIONAL_PRESENT.equals(optionalSelection)
				&& optionalValue.is_value();
	}

	@Override
	public boolean is_optional() {
		return true;
	}

	//originally operator()
	public TYPE get() {
		set_to_present();
		return optionalValue;
	}

	// originally const operator()
	public TYPE constGet() {
		switch (optionalSelection) {
		case OPTIONAL_UNBOUND:
			throw new TtcnError("Using the value of an unbound optional field ");
		case OPTIONAL_OMIT:
			throw new TtcnError("Using the value of an optional field containing omit.");
		default:
			return optionalValue;
		}
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
	public boolean operator_equals(final template_sel otherValue) {
		if (optional_sel.OPTIONAL_UNBOUND.equals(optionalSelection)) {
			if (template_sel.UNINITIALIZED_TEMPLATE.equals(otherValue)) {
				return true;
			}
			throw new TtcnError("The left operand of comparison is an unbound optional value.");
		}

		if (!template_sel.OMIT_VALUE.equals(otherValue)) {
			throw new TtcnError("Internal error: The right operand of comparison is an invalid value.");
		}

		return optional_sel.OPTIONAL_OMIT.equals(optionalSelection);
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
	public boolean operator_equals(final Optional<TYPE> otherValue) {
		if (optional_sel.OPTIONAL_UNBOUND.equals(optionalSelection)) {
			if (optional_sel.OPTIONAL_UNBOUND.equals(otherValue.optionalSelection)) {
				return true;
			} else {
				throw new TtcnError("The left operand of comparison is an unbound optional value.");
			}
		} else {
			if (optional_sel.OPTIONAL_UNBOUND.equals(otherValue.optionalSelection)) {
				throw new TtcnError("The right operand of comparison is an unbound optional value.");
			} else {
				if (optionalSelection != otherValue.optionalSelection) {
					return false;
				} else if (optional_sel.OPTIONAL_PRESENT.equals(optionalSelection)) {
					return optionalValue.operator_equals(otherValue.optionalValue);
				} else {
					return true;
				}
			}
		}
	}

	@Override
	public boolean operator_equals(final Base_Type otherValue) {
		if (!(otherValue instanceof Optional<?>)) {
			if (optional_sel.OPTIONAL_UNBOUND.equals(optionalSelection)) {
				if (!otherValue.is_bound()) {
					return true;
				} else {
					throw new TtcnError("The left operand of comparison is an unbound optional value.");
				}
			} else {
				if (!otherValue.is_bound()) {
					throw new TtcnError("The right operand of comparison is an unbound optional value.");
				} else {
					if (optional_sel.OPTIONAL_PRESENT.equals(optionalSelection)) {
						return optionalValue.operator_equals(otherValue);
					} else {
						return false;
					}
				}
			}
		}

		final Optional<?> optionalOther = (Optional<?>) otherValue;
		if (optional_sel.OPTIONAL_UNBOUND.equals(optionalSelection)) {
			if (optional_sel.OPTIONAL_UNBOUND.equals(optionalOther.optionalSelection)) {
				return true;
			} else {
				throw new TtcnError("The left operand of comparison is an unbound optional value.");
			}
		} else {
			if (optional_sel.OPTIONAL_UNBOUND.equals(optionalOther.optionalSelection)) {
				throw new TtcnError("The right operand of comparison is an unbound optional value.");
			} else {
				if (optionalSelection != optionalOther.optionalSelection) {
					return false;
				} else if (optional_sel.OPTIONAL_PRESENT.equals(optionalSelection)) {
					return optionalValue.operator_equals(optionalOther.optionalValue);
				} else {
					return true;
				}
			}
		}
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
	public boolean operator_not_equals(final template_sel otherValue) {
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
	public boolean operator_not_equals(final Optional<TYPE> otherValue) {
		return !operator_equals(otherValue);
	}
}
