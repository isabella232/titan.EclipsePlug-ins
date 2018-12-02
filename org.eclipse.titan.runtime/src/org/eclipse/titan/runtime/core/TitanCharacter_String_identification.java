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
 * Part of the representation of the ASN.1 unrestricted string (CHARACTER STRING) type.
 *
 * @author Kristof Szabados
 */
public class TitanCharacter_String_identification extends Base_Type {
	public enum union_selection_type { UNBOUND_VALUE,  ALT_syntaxes,  ALT_syntax,  ALT_presentation__context__id,  ALT_context__negotiation,  ALT_transfer__syntax,  ALT_fixed };
	private TitanCharacter_String_identification.union_selection_type union_selection;
	//originally a union which can not be mapped to Java
	private Base_Type field;
	/**
	 * Initializes to unbound value.
	 * */
	public TitanCharacter_String_identification() {
		union_selection = union_selection_type.UNBOUND_VALUE;
	};

	/**
	 * Initializes to a given value.
	 *
	 * @param otherValue
	 *                the value to initialize to.
	 * */
	public TitanCharacter_String_identification(final TitanCharacter_String_identification otherValue) {
		copy_value(otherValue);
	};

	private void copy_value(final TitanCharacter_String_identification otherValue) {
		switch (otherValue.union_selection){
		case ALT_syntaxes:
			field = new TitanCharacter_String_identification_syntaxes((TitanCharacter_String_identification_syntaxes)otherValue.field);
			break;
		case ALT_syntax:
			field = new TitanObjectid((TitanObjectid)otherValue.field);
			break;
		case ALT_presentation__context__id:
			field = new TitanInteger((TitanInteger)otherValue.field);
			break;
		case ALT_context__negotiation:
			field = new TitanCharacter_String_identification_context__negotiation((TitanCharacter_String_identification_context__negotiation)otherValue.field);
			break;
		case ALT_transfer__syntax:
			field = new TitanObjectid((TitanObjectid)otherValue.field);
			break;
		case ALT_fixed:
			field = new TitanAsn_Null((TitanAsn_Null)otherValue.field);
			break;
		default:
			throw new TtcnError("Assignment of an unbound union value of type CHARACTER STRING.identification.");
		}
		union_selection = otherValue.union_selection;
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
	public TitanCharacter_String_identification operator_assign( final TitanCharacter_String_identification otherValue ) {
		if (otherValue != this) {
			clean_up();
			copy_value(otherValue);
		}

		return this;
	}
	@Override
	public TitanCharacter_String_identification operator_assign( final Base_Type otherValue ) {
		if (otherValue instanceof TitanCharacter_String_identification) {
			return operator_assign((TitanCharacter_String_identification)otherValue);
		}
		throw new TtcnError("Internal Error: value can not be cast to CHARACTER STRING.identification.");
	}

	@Override
	public void clean_up() {
		field = null;
		union_selection = union_selection_type.UNBOUND_VALUE;
	}

	public boolean ischosen(final TitanCharacter_String_identification.union_selection_type checked_selection) {
		if(checked_selection == union_selection_type.UNBOUND_VALUE) {
			throw new TtcnError("Internal error: Performing ischosen() operation on an invalid field of union type CHARACTER STRING.identification.");
		}
		return union_selection == checked_selection;
	}

	@Override
	public boolean is_bound() {
		return union_selection != union_selection_type.UNBOUND_VALUE;
	}

	@Override
	public boolean is_value() {
		switch (union_selection) {
		case UNBOUND_VALUE:
			return false;
		case ALT_syntaxes:
			return field.is_value();
		case ALT_syntax:
			return field.is_value();
		case ALT_presentation__context__id:
			return field.is_value();
		case ALT_context__negotiation:
			return field.is_value();
		case ALT_transfer__syntax:
			return field.is_value();
		case ALT_fixed:
			return field.is_value();
		default:
			throw new TtcnError("Invalid selection in union is_bound");
		}
	}

	@Override
	public boolean is_present() {
		return is_bound();
	}

	/**
	 * Checks if the current value is equivalent to the provided one.
	 *
	 * operator== in the core
	 *
	 * @param otherValue
	 *                the other value to check against.
	 * @return {@code true} if the selections and field values are equivalent.
	 */
	public boolean operator_equals( final TitanCharacter_String_identification otherValue ) {
		must_bound( "The left operand of comparison is an unbound value of union type CHARACTER STRING.identification." );
		otherValue.must_bound( "The right operand of comparison is an unbound value of union type CHARACTER STRING.identification." );
		if (union_selection != otherValue.union_selection) {
			return false;
		}
		switch (union_selection) {
		case ALT_syntaxes:
			return ((TitanCharacter_String_identification_syntaxes)field).operator_equals((TitanCharacter_String_identification_syntaxes)otherValue.field);
		case ALT_syntax:
			return ((TitanObjectid)field).operator_equals((TitanObjectid)otherValue.field);
		case ALT_presentation__context__id:
			return ((TitanInteger)field).operator_equals((TitanInteger)otherValue.field);
		case ALT_context__negotiation:
			return ((TitanCharacter_String_identification_context__negotiation)field).operator_equals((TitanCharacter_String_identification_context__negotiation)otherValue.field);
		case ALT_transfer__syntax:
			return ((TitanObjectid)field).operator_equals((TitanObjectid)otherValue.field);
		case ALT_fixed:
			return ((TitanAsn_Null)field).operator_equals((TitanAsn_Null)otherValue.field);
		default:
			return false;
		}
	}
	@Override
	public boolean operator_equals( final Base_Type otherValue ) {
		if (otherValue instanceof TitanCharacter_String_identification) {
			return operator_equals((TitanCharacter_String_identification)otherValue);
		}
		throw new TtcnError("Internal Error: value can not be cast to CHARACTER STRING.identification.");
	}

	/**
	 * Checks if the current value is not equivalent to the provided one.
	 *
	 * operator!= in the core
	 *
	 * @param otherValue
	 *                the other value to check against.
	 * @return {@code true} if either the selections or the field values are not equivalent.
	 */
	public boolean operator_not_equals( final TitanCharacter_String_identification otherValue ) {
		return !operator_equals(otherValue);
	}

	/**
	 * Selects and gives access to field syntaxes.
	 * If other field was previously selected, its value will be destroyed.
	 *
	 * @return field syntaxes.
	 * */
	public TitanCharacter_String_identification_syntaxes get_field_syntaxes() {
		if (union_selection != union_selection_type.ALT_syntaxes) {
			clean_up();
			field = new TitanCharacter_String_identification_syntaxes();
			union_selection = union_selection_type.ALT_syntaxes;
		}
		return (TitanCharacter_String_identification_syntaxes)field;
	}

	/**
	 * Gives read-only access to field syntaxes.
	 * If field syntaxes is not selected,
	 * this function will cause a dynamic test case error.
	 *
	 * @return field syntaxes.
	 * */
	public TitanCharacter_String_identification_syntaxes constGet_field_syntaxes() {
		if (union_selection != union_selection_type.ALT_syntaxes) {
			throw new TtcnError("Using non-selected field syntaxes in a value of union type CHARACTER STRING.identification.");
		}
		return (TitanCharacter_String_identification_syntaxes)field;
	}

	/**
	 * Selects and gives access to field syntax.
	 * If other field was previously selected, its value will be destroyed.
	 *
	 * @return field syntax.
	 * */
	public TitanObjectid get_field_syntax() {
		if (union_selection != union_selection_type.ALT_syntax) {
			clean_up();
			field = new TitanObjectid();
			union_selection = union_selection_type.ALT_syntax;
		}
		return (TitanObjectid)field;
	}

	/**
	 * Gives read-only access to field syntax.
	 * If field syntax is not selected,
	 * this function will cause a dynamic test case error.
	 *
	 * @return field syntax.
	 * */
	public TitanObjectid constGet_field_syntax() {
		if (union_selection != union_selection_type.ALT_syntax) {
			throw new TtcnError("Using non-selected field syntax in a value of union type CHARACTER STRING.identification.");
		}
		return (TitanObjectid)field;
	}

	/**
	 * Selects and gives access to field presentation-context-id.
	 * If other field was previously selected, its value will be destroyed.
	 *
	 * @return field presentation-context-id.
	 * */
	public TitanInteger get_field_presentation__context__id() {
		if (union_selection != union_selection_type.ALT_presentation__context__id) {
			clean_up();
			field = new TitanInteger();
			union_selection = union_selection_type.ALT_presentation__context__id;
		}
		return (TitanInteger)field;
	}

	/**
	 * Gives read-only access to field presentation-context-id.
	 * If field presentation-context-id is not selected,
	 * this function will cause a dynamic test case error.
	 *
	 * @return field presentation-context-id.
	 * */
	public TitanInteger constGet_field_presentation__context__id() {
		if (union_selection != union_selection_type.ALT_presentation__context__id) {
			throw new TtcnError("Using non-selected field presentation-context-id in a value of union type CHARACTER STRING.identification.");
		}
		return (TitanInteger)field;
	}

	/**
	 * Selects and gives access to field context-negotiation.
	 * If other field was previously selected, its value will be destroyed.
	 *
	 * @return field context-negotiation.
	 * */
	public TitanCharacter_String_identification_context__negotiation get_field_context__negotiation() {
		if (union_selection != union_selection_type.ALT_context__negotiation) {
			clean_up();
			field = new TitanCharacter_String_identification_context__negotiation();
			union_selection = union_selection_type.ALT_context__negotiation;
		}
		return (TitanCharacter_String_identification_context__negotiation)field;
	}

	/**
	 * Gives read-only access to field context-negotiation.
	 * If field context-negotiation is not selected,
	 * this function will cause a dynamic test case error.
	 *
	 * @return field context-negotiation.
	 * */
	public TitanCharacter_String_identification_context__negotiation constGet_field_context__negotiation() {
		if (union_selection != union_selection_type.ALT_context__negotiation) {
			throw new TtcnError("Using non-selected field context-negotiation in a value of union type CHARACTER STRING.identification.");
		}
		return (TitanCharacter_String_identification_context__negotiation)field;
	}

	/**
	 * Selects and gives access to field transfer-syntax.
	 * If other field was previously selected, its value will be destroyed.
	 *
	 * @return field transfer-syntax.
	 * */
	public TitanObjectid get_field_transfer__syntax() {
		if (union_selection != union_selection_type.ALT_transfer__syntax) {
			clean_up();
			field = new TitanObjectid();
			union_selection = union_selection_type.ALT_transfer__syntax;
		}
		return (TitanObjectid)field;
	}

	/**
	 * Gives read-only access to field transfer-syntax.
	 * If field transfer-syntax is not selected,
	 * this function will cause a dynamic test case error.
	 *
	 * @return field transfer-syntax.
	 * */
	public TitanObjectid constGet_field_transfer__syntax() {
		if (union_selection != union_selection_type.ALT_transfer__syntax) {
			throw new TtcnError("Using non-selected field transfer-syntax in a value of union type CHARACTER STRING.identification.");
		}
		return (TitanObjectid)field;
	}

	/**
	 * Selects and gives access to field fixed.
	 * If other field was previously selected, its value will be destroyed.
	 *
	 * @return field fixed.
	 * */
	public TitanAsn_Null get_field_fixed() {
		if (union_selection != union_selection_type.ALT_fixed) {
			clean_up();
			field = new TitanAsn_Null();
			union_selection = union_selection_type.ALT_fixed;
		}
		return (TitanAsn_Null)field;
	}

	/**
	 * Gives read-only access to field fixed.
	 * If field fixed is not selected,
	 * this function will cause a dynamic test case error.
	 *
	 * @return field fixed.
	 * */
	public TitanAsn_Null constGet_field_fixed() {
		if (union_selection != union_selection_type.ALT_fixed) {
			throw new TtcnError("Using non-selected field fixed in a value of union type CHARACTER STRING.identification.");
		}
		return (TitanAsn_Null)field;
	}

	/**
	 * Returns the current selection.
	 * It will return TitanCharacter_String_identification.union_selection_type.UNBOUND_VALUE if the value is unbound,
	 * TitanCharacter_String_identification.union_selection_type.ALT_syntaxes if the first field was selected, and so on.
	 *
	 * @return the current selection.
	 * */
	public TitanCharacter_String_identification.union_selection_type get_selection() {
		return union_selection;
	}
	@Override
	public void log() {
		switch (union_selection) {
		case ALT_syntaxes:
			TTCN_Logger.log_event_str("{ syntaxes := ");
			field.log();
			TTCN_Logger.log_event_str(" }");
			break;
		case ALT_syntax:
			TTCN_Logger.log_event_str("{ syntax := ");
			field.log();
			TTCN_Logger.log_event_str(" }");
			break;
		case ALT_presentation__context__id:
			TTCN_Logger.log_event_str("{ presentation-context-id := ");
			field.log();
			TTCN_Logger.log_event_str(" }");
			break;
		case ALT_context__negotiation:
			TTCN_Logger.log_event_str("{ context-negotiation := ");
			field.log();
			TTCN_Logger.log_event_str(" }");
			break;
		case ALT_transfer__syntax:
			TTCN_Logger.log_event_str("{ transfer-syntax := ");
			field.log();
			TTCN_Logger.log_event_str(" }");
			break;
		case ALT_fixed:
			TTCN_Logger.log_event_str("{ fixed := ");
			field.log();
			TTCN_Logger.log_event_str(" }");
			break;
		default:
			TTCN_Logger.log_event_unbound();
			break;
		}
	}

	@Override
	public void set_param(final Module_Parameter param) {
		param.basic_check(Module_Parameter.basic_check_bits_t.BC_VALUE.getValue(), "union value");
		if(param.get_type() == Module_Parameter.type_t.MP_Value_List && param.get_size() == 0) {
			return;
		}
		if (param.get_type() != Module_Parameter.type_t.MP_Assignment_List) {
			param.error("union value with field name was expected");
		}
		final Module_Parameter mp_last = param.get_elem(param.get_size() - 1);
		final String last_name = mp_last.get_id().get_name();
		if ("syntaxes".equals(last_name)) {
			get_field_syntaxes().set_param(mp_last);
			if (!field.is_bound()) {
				clean_up();
			}
			return;
		}
		if ("syntax".equals(last_name)) {
			get_field_syntax().set_param(mp_last);
			if (!field.is_bound()) {
				clean_up();
			}
			return;
		}
		if ("presentation-context-id".equals(last_name)) {
			get_field_presentation__context__id().set_param(mp_last);
			if (!field.is_bound()) {
				clean_up();
			}
			return;
		}
		if ("context-negotiation".equals(last_name)) {
			get_field_context__negotiation().set_param(mp_last);
			if (!field.is_bound()) {
				clean_up();
			}
			return;
		}
		if ("transfer-syntax".equals(last_name)) {
			get_field_transfer__syntax().set_param(mp_last);
			if (!field.is_bound()) {
				clean_up();
			}
			return;
		}
		if ("fixed".equals(last_name)) {
			get_field_fixed().set_param(mp_last);
			if (!field.is_bound()) {
				clean_up();
			}
			return;
		}
		mp_last.error(MessageFormat.format("Field {0} does not exist in type CHARACTER STRING.identification.", last_name));
	}

	@Override
	public void set_implicit_omit() {
		switch (union_selection) {
		case ALT_syntaxes:
		case ALT_syntax:
		case ALT_presentation__context__id:
		case ALT_context__negotiation:
		case ALT_transfer__syntax:
		case ALT_fixed:
			field.set_implicit_omit();
			break;
		default:
			break;
		}
	}

	@Override
	public void encode_text(final Text_Buf text_buf) {
		switch (union_selection) {
		case ALT_syntaxes:
			text_buf.push_int(0);
			break;
		case ALT_syntax:
			text_buf.push_int(1);
			break;
		case ALT_presentation__context__id:
			text_buf.push_int(2);
			break;
		case ALT_context__negotiation:
			text_buf.push_int(3);
			break;
		case ALT_transfer__syntax:
			text_buf.push_int(4);
			break;
		case ALT_fixed:
			text_buf.push_int(5);
			break;
		default:
			throw new TtcnError("Text encoder: Encoding an unbound value of union type CHARACTER STRING.identification.");
		}
		field.encode_text(text_buf);
	}

	@Override
	public void decode_text(final Text_Buf text_buf) {
		final int temp = text_buf.pull_int().getInt();
		switch (temp) {
		case 0:
			get_field_syntaxes().decode_text(text_buf);
			break;
		case 1:
			get_field_syntax().decode_text(text_buf);
			break;
		case 2:
			get_field_presentation__context__id().decode_text(text_buf);
			break;
		case 3:
			get_field_context__negotiation().decode_text(text_buf);
			break;
		case 4:
			get_field_transfer__syntax().decode_text(text_buf);
			break;
		case 5:
			get_field_fixed().decode_text(text_buf);
			break;
		default:
			throw new TtcnError("Text decoder: Unrecognized union selector was received for type CHARACTER STRING.identification.");
		}
	}

	@Override
	public void encode(final TTCN_Typedescriptor p_td, final TTCN_Buffer p_buf, final coding_type p_coding, final int flavour) {
		switch (p_coding) {
		case CT_RAW: {
			final TTCN_EncDec_ErrorContext errorContext = new TTCN_EncDec_ErrorContext("While RAW-encoding type '%s': ", p_td.name);
			if (p_td.raw == null) {
				TTCN_EncDec_ErrorContext.error_internal("No RAW descriptor available for type '%s'.", p_td.name);
			}
			final RAW_enc_tr_pos rp = new RAW_enc_tr_pos(0, null);
			final RAW_enc_tree root = new RAW_enc_tree(true, null, rp, 1, p_td.raw);
			RAW_encode(p_td, root);
			root.put_to_buf(p_buf);
			errorContext.leaveContext();
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
			errorContext.leaveContext();
			break;
		}
		default:
			throw new TtcnError(MessageFormat.format("Unknown coding method requested to decode type `{0}''", p_td.name));
		}
	}

}