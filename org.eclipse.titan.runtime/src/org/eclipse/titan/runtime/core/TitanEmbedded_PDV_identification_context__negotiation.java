/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

import java.text.MessageFormat;

import org.eclipse.titan.runtime.core.Param_Types.Module_Parameter;
import org.eclipse.titan.runtime.core.RAW.RAW_enc_tr_pos;
import org.eclipse.titan.runtime.core.RAW.RAW_enc_tree;
import org.eclipse.titan.runtime.core.TTCN_EncDec.coding_type;
import org.eclipse.titan.runtime.core.TTCN_EncDec.error_type;
import org.eclipse.titan.runtime.core.TTCN_EncDec.raw_order_t;

/**
 * Part of the representation of the ASN.1 EMBEDDED PDV type
 *
 * @author Kristof Szabados
 */
public class TitanEmbedded_PDV_identification_context__negotiation extends Base_Type {
	private final TitanInteger presentation__context__id; //ASN1_Integer_Type
	private final TitanObjectid transfer__syntax; //ObjectID_Type

	/**
	 * Initializes to unbound value.
	 * */
	public TitanEmbedded_PDV_identification_context__negotiation() {
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
	public TitanEmbedded_PDV_identification_context__negotiation(final TitanInteger presentation__context__id, final TitanObjectid transfer__syntax ) {
		this.presentation__context__id = new TitanInteger( presentation__context__id );
		this.transfer__syntax = new TitanObjectid( transfer__syntax );
	}

	/**
	 * Initializes to a given value.
	 *
	 * @param otherValue
	 *                the value to initialize to.
	 * */
	public TitanEmbedded_PDV_identification_context__negotiation( final TitanEmbedded_PDV_identification_context__negotiation otherValue) {
		otherValue.must_bound("Copying of an unbound value of type EMBEDDED PDV.identification.context-negotiation.");
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
	public TitanEmbedded_PDV_identification_context__negotiation operator_assign(final TitanEmbedded_PDV_identification_context__negotiation otherValue ) {
		otherValue.must_bound( "Assignment of an unbound value of type EMBEDDED PDV.identification.context-negotiation");
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
	public TitanEmbedded_PDV_identification_context__negotiation operator_assign(final Base_Type otherValue) {
		if (otherValue instanceof TitanEmbedded_PDV_identification_context__negotiation ) {
			return operator_assign((TitanEmbedded_PDV_identification_context__negotiation) otherValue);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to EMBEDDED PDV.identification.context-negotiation", otherValue));
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
	public boolean operator_equals( final TitanEmbedded_PDV_identification_context__negotiation other_value) {
		return presentation__context__id.operator_equals( other_value.presentation__context__id )
				&& transfer__syntax.operator_equals( other_value.transfer__syntax );
	}

	@Override
	public boolean operator_equals(final Base_Type other_value) {
		if (other_value instanceof TitanEmbedded_PDV_identification_context__negotiation ) {
			return operator_equals((TitanEmbedded_PDV_identification_context__negotiation) other_value);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to EMBEDDED PDV.identification.context-negotiation", other_value));
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
				param.error(MessageFormat.format("set value of type EMBEDDED PDV.identification.context-negotiation has 2 fields but list value has {0} fields.", param.get_size()));
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
					curr_param.error(MessageFormat.format("Non existent field name in type EMBEDDED PDV.identification.context-negotiation: {0}", curr_param.get_id().get_name()));
					break;
				}
			}
			break;
		}
		default:
			param.type_error("set value", "EMBEDDED PDV.identification.context-negotiation");
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
			if (p_td.raw == null) {
				TTCN_EncDec_ErrorContext.error_internal("No RAW descriptor available for type '%s'.", p_td.name);
			}
			final RAW_enc_tr_pos tree_position = new RAW_enc_tr_pos(0, null);
			final RAW_enc_tree root = new RAW_enc_tree(false, null, tree_position, 1, p_td.raw);
			RAW_encode(p_td, root);
			root.put_to_buf(p_buf);
			errorContext.leave_context();
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
			if (p_td.raw == null) {
				TTCN_EncDec_ErrorContext.error_internal("No RAW descriptor available for type '%s'.", p_td.name);
			}
			raw_order_t order;
			switch (p_td.raw.top_bit_order) {
			case TOP_BIT_LEFT:
				order = raw_order_t.ORDER_LSB;
				break;
			case TOP_BIT_RIGHT:
			default:
				order = raw_order_t.ORDER_MSB;
				break;
			}
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
			errorContext.leave_context();
			break;
		}
		default:
			throw new TtcnError(MessageFormat.format("Unknown coding method requested to decode type `{0}''", p_td.name));
		}
	}

}