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
import org.eclipse.titan.runtime.core.Param_Types.Module_Param_Assignment_List;
import org.eclipse.titan.runtime.core.Param_Types.Module_Param_FieldName;
import org.eclipse.titan.runtime.core.Param_Types.Module_Param_Name;
import org.eclipse.titan.runtime.core.Param_Types.Module_Param_Unbound;
import org.eclipse.titan.runtime.core.Param_Types.Module_Parameter;
import org.eclipse.titan.runtime.core.RAW.RAW_enc_tr_pos;
import org.eclipse.titan.runtime.core.RAW.RAW_enc_tree;
import org.eclipse.titan.runtime.core.RAW.top_bit_order_t;
import org.eclipse.titan.runtime.core.TTCN_EncDec.coding_type;
import org.eclipse.titan.runtime.core.TTCN_EncDec.error_type;
import org.eclipse.titan.runtime.core.TTCN_EncDec.raw_order_t;


/**
 * Part of the representation of the ASN.1 EMBEDDED PDV type
 *
 * @author Kristof Szabados
 */
public class TitanEmbedded_PDV_identification extends Base_Type {
	public static final TTCN_JSONdescriptor TitanEmbedded_PDV_identification_json_ = new TTCN_JSONdescriptor(false, null, false, null, false, false, false, 0, null, false, json_string_escaping.ESCAPE_AS_SHORT);
	public static final TTCN_Typedescriptor TitanEmbedded_PDV_identification_descr_ = new TTCN_Typedescriptor("EMBEDDED PDV.identification", TitanEmbedded_PDV_identification.TitanEmbedded_PDV_identification_json_);
	public static final TitanUniversalCharString TitanEmbedded_PDV_identification_default_coding = new TitanUniversalCharString("JSON");
	public static final TTCN_Typedescriptor TitanEmbedded_PDV_identification_syntax_descr_ = new TTCN_Typedescriptor("EMBEDDED PDV.identification.syntax", TitanObjectid.TitanObjectid_json_);
	public static final TitanUniversalCharString TitanEmbedded_PDV_identification_syntax_default_coding = new TitanUniversalCharString("JSON");
	public static final TTCN_Typedescriptor TitanEmbedded_PDV_identification_presentation__context__id_descr_ = new TTCN_Typedescriptor("EMBEDDED PDV.identification.presentation-context-id", TitanInteger.TitanInteger_json_);
	public static final TitanUniversalCharString TitanEmbedded_PDV_identification_presentation__context__id_default_coding = new TitanUniversalCharString("JSON");
	public static final TTCN_Typedescriptor TitanEmbedded_PDV_identification_transfer__syntax_descr_ = new TTCN_Typedescriptor("EMBEDDED PDV.identification.transfer-syntax", TitanObjectid.TitanObjectid_json_);
	public static final TitanUniversalCharString TitanEmbedded_PDV_identification_transfer__syntax_default_coding = new TitanUniversalCharString("JSON");
	public static final TTCN_Typedescriptor TitanEmbedded_PDV_identification_fixed_descr_ = new TTCN_Typedescriptor("EMBEDDED PDV.identification.fixed", TitanAsn_Null.TitanAsn_Null_json_);
	public static final TitanUniversalCharString TitanEmbedded_PDV_identification_fixed_default_coding = new TitanUniversalCharString("JSON");
	/**
	 * Indicates the state/selection of this union kind.
	 * When union_selection is UNBOUND_VALUE, the union is unbound.
	 * When union_selection is any other enumeration,
	 * the appropriate field is selected.
	 * */
	public enum union_selection_type { UNBOUND_VALUE,  ALT_syntaxes,  ALT_syntax,  ALT_presentation__context__id,  ALT_context__negotiation,  ALT_transfer__syntax,  ALT_fixed };
	private TitanEmbedded_PDV_identification.union_selection_type union_selection;
	//originally a union which can not be mapped to Java
	private Base_Type field;

	/**
	 * Initializes to unbound value.
	 * */
	public TitanEmbedded_PDV_identification() {
		union_selection = union_selection_type.UNBOUND_VALUE;
	};

	/**
	 * Initializes to a given value.
	 *
	 * @param otherValue
	 *                the value to initialize to.
	 * */
	public TitanEmbedded_PDV_identification(final TitanEmbedded_PDV_identification otherValue) {
		copy_value(otherValue);
	};

	/**
	 * Internal function to copy the provided value into this template.
	 * The template becomes a specific value template.
	 * The already existing content is overwritten.
	 *
	 * @param other_value the value to be copied.
	 * */
	private void copy_value(final TitanEmbedded_PDV_identification other_value) {
		switch (other_value.union_selection){
		case ALT_syntaxes:
			field = new TitanEmbedded_PDV_identification_syntaxes((TitanEmbedded_PDV_identification_syntaxes)other_value.field);
			break;
		case ALT_syntax:
			field = new TitanObjectid((TitanObjectid)other_value.field);
			break;
		case ALT_presentation__context__id:
			field = new TitanInteger((TitanInteger)other_value.field);
			break;
		case ALT_context__negotiation:
			field = new TitanEmbedded_PDV_identification_context__negotiation((TitanEmbedded_PDV_identification_context__negotiation)other_value.field);
			break;
		case ALT_transfer__syntax:
			field = new TitanObjectid((TitanObjectid)other_value.field);
			break;
		case ALT_fixed:
			field = new TitanAsn_Null((TitanAsn_Null)other_value.field);
			break;
		default:
			throw new TtcnError("Assignment of an unbound union value of type EMBEDDED PDV.identification.");
		}
		union_selection = other_value.union_selection;
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
	public TitanEmbedded_PDV_identification operator_assign( final TitanEmbedded_PDV_identification otherValue ) {
		if (otherValue != this) {
			clean_up();
			copy_value(otherValue);
		}

		return this;
	}

	@Override
	public TitanEmbedded_PDV_identification operator_assign( final Base_Type otherValue ) {
		if (otherValue instanceof TitanEmbedded_PDV_identification) {
			return operator_assign((TitanEmbedded_PDV_identification)otherValue);
		}
		throw new TtcnError("Internal Error: value can not be cast to EMBEDDED PDV.identification.");
	}

	@Override
	public void clean_up() {
		field = null;
		union_selection = union_selection_type.UNBOUND_VALUE;
	}

	/**
	 * Checks and reports whether the union has the provided alternative active or not.
	 *
	 * ischosen in the core.
	 *
	 * @param checked_selection the selection to check for.
	 *
	 * @return {@code true} if the unions has the provided selection active.
	 */
	public boolean ischosen(final TitanEmbedded_PDV_identification.union_selection_type checked_selection) {
		if(checked_selection == union_selection_type.UNBOUND_VALUE) {
			throw new TtcnError("Internal error: Performing ischosen() operation on an invalid field of union type EMBEDDED PDV.identification.");
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
	public boolean operator_equals( final TitanEmbedded_PDV_identification otherValue ) {
		must_bound( "The left operand of comparison is an unbound value of union type EMBEDDED PDV.identification." );
		otherValue.must_bound( "The right operand of comparison is an unbound value of union type EMBEDDED PDV.identification." );
		if (union_selection != otherValue.union_selection) {
			return false;
		}
		switch (union_selection) {
		case ALT_syntaxes:
			return ((TitanEmbedded_PDV_identification_syntaxes)field).operator_equals((TitanEmbedded_PDV_identification_syntaxes)otherValue.field);
		case ALT_syntax:
			return ((TitanObjectid)field).operator_equals((TitanObjectid)otherValue.field);
		case ALT_presentation__context__id:
			return ((TitanInteger)field).operator_equals((TitanInteger)otherValue.field);
		case ALT_context__negotiation:
			return ((TitanEmbedded_PDV_identification_context__negotiation)field).operator_equals((TitanEmbedded_PDV_identification_context__negotiation)otherValue.field);
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
		if (otherValue instanceof TitanEmbedded_PDV_identification) {
			return operator_equals((TitanEmbedded_PDV_identification)otherValue);
		}
		throw new TtcnError("Internal Error: value can not be cast to EMBEDDED PDV.identification.");
	}

	/**
	 * Checks if the current value is not equivalent to the provided one.
	 *
	 * operator!= in the core
	 *
	 * @param otherValue
	 *                the other value to check against.
	 * @return {@code true} if either the selections or the field
	 *         values are not equivalent.
	 */
	public boolean operator_not_equals( final TitanEmbedded_PDV_identification otherValue ) {
		return !operator_equals(otherValue);
	}

	/**
	 * Selects and gives access to field syntaxes.
	 * If other field was previously selected, its value will be destroyed.
	 *
	 * @return field syntaxes.
	 * */
	public TitanEmbedded_PDV_identification_syntaxes get_field_syntaxes() {
		if (union_selection != union_selection_type.ALT_syntaxes) {
			//clean_up();
			field = new TitanEmbedded_PDV_identification_syntaxes();
			union_selection = union_selection_type.ALT_syntaxes;
		}
		return (TitanEmbedded_PDV_identification_syntaxes)field;
	}

	/**
	 * Gives read-only access to field syntaxes.
	 * If field syntaxes is not selected,
	 * this function will cause a dynamic test case error.
	 *
	 * @return field syntaxes.
	 * */
	public TitanEmbedded_PDV_identification_syntaxes constGet_field_syntaxes() {
		if (union_selection != union_selection_type.ALT_syntaxes) {
			throw new TtcnError("Using non-selected field syntaxes in a value of union type EMBEDDED PDV.identification.");
		}
		return (TitanEmbedded_PDV_identification_syntaxes)field;
	}

	/**
	 * Selects and gives access to field syntax.
	 * If other field was previously selected, its value will be destroyed.
	 *
	 * @return field syntax.
	 * */
	public TitanObjectid get_field_syntax() {
		if (union_selection != union_selection_type.ALT_syntax) {
			//clean_up();
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
			throw new TtcnError("Using non-selected field syntax in a value of union type EMBEDDED PDV.identification.");
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
			//clean_up();
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
			throw new TtcnError("Using non-selected field presentation-context-id in a value of union type EMBEDDED PDV.identification.");
		}
		return (TitanInteger)field;
	}

	/**
	 * Selects and gives access to field context-negotiation.
	 * If other field was previously selected, its value will be destroyed.
	 *
	 * @return field context-negotiation.
	 * */
	public TitanEmbedded_PDV_identification_context__negotiation get_field_context__negotiation() {
		if (union_selection != union_selection_type.ALT_context__negotiation) {
			//clean_up();
			field = new TitanEmbedded_PDV_identification_context__negotiation();
			union_selection = union_selection_type.ALT_context__negotiation;
		}
		return (TitanEmbedded_PDV_identification_context__negotiation)field;
	}

	/**
	 * Gives read-only access to field context-negotiation.
	 * If field context-negotiation is not selected,
	 * this function will cause a dynamic test case error.
	 *
	 * @return field context-negotiation.
	 * */
	public TitanEmbedded_PDV_identification_context__negotiation constGet_field_context__negotiation() {
		if (union_selection != union_selection_type.ALT_context__negotiation) {
			throw new TtcnError("Using non-selected field context-negotiation in a value of union type EMBEDDED PDV.identification.");
		}
		return (TitanEmbedded_PDV_identification_context__negotiation)field;
	}

	/**
	 * Selects and gives access to field transfer-syntax.
	 * If other field was previously selected, its value will be destroyed.
	 *
	 * @return field transfer-syntax.
	 * */
	public TitanObjectid get_field_transfer__syntax() {
		if (union_selection != union_selection_type.ALT_transfer__syntax) {
			//clean_up();
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
			throw new TtcnError("Using non-selected field transfer-syntax in a value of union type EMBEDDED PDV.identification.");
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
			//clean_up();
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
			throw new TtcnError("Using non-selected field fixed in a value of union type EMBEDDED PDV.identification.");
		}
		return (TitanAsn_Null)field;
	}

	/**
	 * Returns the current selection.
	 * It will return TitanEmbedded_PDV_identification.union_selection_type.UNBOUND_VALUE if the value is unbound,
	 * TitanEmbedded_PDV_identification.union_selection_type.ALT_syntaxes if the first field was selected, and so on.
	 *
	 * @return the current selection.
	 * */
	public TitanEmbedded_PDV_identification.union_selection_type get_selection() {
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
	public void set_param(Module_Parameter param) {
		if (param.get_id() != null && param.get_id().next_name()) {
			final String param_field = param.get_id().get_current_name();
			final char first_char = param_field.charAt(0);
			if (first_char >= '0' && first_char <= '9') {
				param.error("Unexpected array index in module parameter, expected a valid field name for union type `EMBEDDED PDV.identification'");
			}
			if("syntaxes".equals(param_field)) {
				get_field_syntaxes().set_param(param);
				return;
			} else if("syntax".equals(param_field)) {
				get_field_syntax().set_param(param);
				return;
			} else if("presentation-context-id".equals(param_field)) {
				get_field_presentation__context__id().set_param(param);
				return;
			} else if("context-negotiation".equals(param_field)) {
				get_field_context__negotiation().set_param(param);
				return;
			} else if("transfer-syntax".equals(param_field)) {
				get_field_transfer__syntax().set_param(param);
				return;
			} else if("fixed".equals(param_field)) {
				get_field_fixed().set_param(param);
				return;
			} else {
				param.error(MessageFormat.format("Field `{0}' not found in union template type `EMBEDDED PDV.identification'", param_field));
			}
		}
		param.basic_check(Module_Parameter.basic_check_bits_t.BC_VALUE.getValue(), "union value");
		if (param.get_type() == Module_Parameter.type_t.MP_Reference) {
			param = param.get_referenced_param().get();
		}
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
		mp_last.error(MessageFormat.format("Field {0} does not exist in type EMBEDDED PDV.identification.", last_name));
	}

	@Override
	public Module_Parameter get_param(final Module_Param_Name param_name) {
		if (!is_bound()) {
			return new Module_Param_Unbound();
		}
		if (param_name.next_name()) {
			final String param_field = param_name.get_current_name();
			if (param_field.charAt(0) >= '0' && param_field.charAt(0) <= '9') {
				throw new TtcnError("Unexpected array index in module parameter, expected a valid field name for union type `EMBEDDED PDV.identification'");
			}
			if ("syntaxes".equals(param_field)) {
				return get_field_syntaxes().get_param(param_name);
			} else if ("syntax".equals(param_field)) {
				return get_field_syntax().get_param(param_name);
			} else if ("presentation-context-id".equals(param_field)) {
				return get_field_presentation__context__id().get_param(param_name);
			} else if ("context-negotiation".equals(param_field)) {
				return get_field_context__negotiation().get_param(param_name);
			} else if ("transfer-syntax".equals(param_field)) {
				return get_field_transfer__syntax().get_param(param_name);
			} else if ("fixed".equals(param_field)) {
				return get_field_fixed().get_param(param_name);
			} else {
				throw new TtcnError(MessageFormat.format("Field `{0}' not found in union type `EMBEDDED PDV.identification'", param_field));
			}
		}
		Module_Parameter mp_field;
		switch(union_selection) {
		case ALT_syntaxes:
			mp_field = get_field_syntaxes().get_param(param_name);
			mp_field.set_id(new Module_Param_FieldName("syntaxes"));
			break;
		case ALT_syntax:
			mp_field = get_field_syntax().get_param(param_name);
			mp_field.set_id(new Module_Param_FieldName("syntax"));
			break;
		case ALT_presentation__context__id:
			mp_field = get_field_presentation__context__id().get_param(param_name);
			mp_field.set_id(new Module_Param_FieldName("presentation-context-id"));
			break;
		case ALT_context__negotiation:
			mp_field = get_field_context__negotiation().get_param(param_name);
			mp_field.set_id(new Module_Param_FieldName("context-negotiation"));
			break;
		case ALT_transfer__syntax:
			mp_field = get_field_transfer__syntax().get_param(param_name);
			mp_field.set_id(new Module_Param_FieldName("transfer-syntax"));
			break;
		case ALT_fixed:
			mp_field = get_field_fixed().get_param(param_name);
			mp_field.set_id(new Module_Param_FieldName("fixed"));
			break;
		default:
			mp_field = null;
		}
		final Module_Param_Assignment_List mp = new Module_Param_Assignment_List();
		mp.add_elem(mp_field);
		return mp;
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
			text_buf.push_int(1);
			break;
		case ALT_syntax:
			text_buf.push_int(2);
			break;
		case ALT_presentation__context__id:
			text_buf.push_int(3);
			break;
		case ALT_context__negotiation:
			text_buf.push_int(4);
			break;
		case ALT_transfer__syntax:
			text_buf.push_int(5);
			break;
		case ALT_fixed:
			text_buf.push_int(6);
			break;
		default:
			throw new TtcnError("Text encoder: Encoding an unbound value of union type EMBEDDED PDV.identification.");
		}
		field.encode_text(text_buf);
	}

	@Override
	public void decode_text(final Text_Buf text_buf) {
		final int temp = text_buf.pull_int().get_int();
		switch (temp) {
		case 1:
			get_field_syntaxes().decode_text(text_buf);
			break;
		case 2:
			get_field_syntax().decode_text(text_buf);
			break;
		case 3:
			get_field_presentation__context__id().decode_text(text_buf);
			break;
		case 4:
			get_field_context__negotiation().decode_text(text_buf);
			break;
		case 5:
			get_field_transfer__syntax().decode_text(text_buf);
			break;
		case 6:
			get_field_fixed().decode_text(text_buf);
			break;
		default:
			throw new TtcnError("Text decoder: Unrecognized union selector was received for type EMBEDDED PDV.identification.");
		}
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
				final RAW_enc_tree root = new RAW_enc_tree(true, null, tree_position, 1, p_td.raw);
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
		final boolean as_value = p_td.json.isAs_value();
		int enc_len = as_value ? 0 : p_tok.put_next_token(json_token_t.JSON_TOKEN_OBJECT_START, null);
		switch(union_selection) {
		case ALT_syntaxes:
			if (!as_value) {
				enc_len += p_tok.put_next_token(json_token_t.JSON_TOKEN_NAME, "syntaxes");
			}
			enc_len += get_field_syntaxes().JSON_encode(TitanEmbedded_PDV_identification_syntaxes.TitanEmbedded_PDV_identification_syntaxes_descr_, p_tok);
			break;
		case ALT_syntax:
			if (!as_value) {
				enc_len += p_tok.put_next_token(json_token_t.JSON_TOKEN_NAME, "syntax");
			}
			enc_len += get_field_syntax().JSON_encode(TitanObjectid.TitanObjectid_descr_, p_tok);
			break;
		case ALT_presentation__context__id:
			if (!as_value) {
				enc_len += p_tok.put_next_token(json_token_t.JSON_TOKEN_NAME, "presentation-context-id");
			}
			enc_len += get_field_presentation__context__id().JSON_encode(TitanInteger.TitanInteger_descr_, p_tok);
			break;
		case ALT_context__negotiation:
			if (!as_value) {
				enc_len += p_tok.put_next_token(json_token_t.JSON_TOKEN_NAME, "context-negotiation");
			}
			enc_len += get_field_context__negotiation().JSON_encode(TitanEmbedded_PDV_identification_context__negotiation.TitanEmbedded_PDV_identification_context__negotiation_descr_, p_tok);
			break;
		case ALT_transfer__syntax:
			if (!as_value) {
				enc_len += p_tok.put_next_token(json_token_t.JSON_TOKEN_NAME, "transfer-syntax");
			}
			enc_len += get_field_transfer__syntax().JSON_encode(TitanObjectid.TitanObjectid_descr_, p_tok);
			break;
		case ALT_fixed:
			if (!as_value) {
				enc_len += p_tok.put_next_token(json_token_t.JSON_TOKEN_NAME, "fixed");
			}
			enc_len += get_field_fixed().JSON_encode(TitanAsn_Null.TitanAsn_Null_descr_, p_tok);
			break;
		default:
			TTCN_EncDec_ErrorContext.error(error_type.ET_UNBOUND, "Encoding an unbound value of type EMBEDDED PDV.identification.");
			return -1;
		}

		if (!as_value) {
			enc_len += p_tok.put_next_token(json_token_t.JSON_TOKEN_OBJECT_END, null);
		}
		return enc_len;
	}

	@Override
	/** {@inheritDoc} */
	public int JSON_decode(final TTCN_Typedescriptor p_td, final JSON_Tokenizer p_tok, final boolean p_silent, final boolean p_parent_is_map, final int p_chosen_field) {
		if (0 <= p_chosen_field && 6 > p_chosen_field) {
			switch (p_chosen_field) {
			case 0:
				return get_field_syntaxes().JSON_decode(TitanEmbedded_PDV_identification_syntaxes.TitanEmbedded_PDV_identification_syntaxes_descr_, p_tok, true);
			case 1:
				return get_field_syntax().JSON_decode(TitanObjectid.TitanObjectid_descr_, p_tok, true);
			case 2:
				return get_field_presentation__context__id().JSON_decode(TitanInteger.TitanInteger_descr_, p_tok, true);
			case 3:
				return get_field_context__negotiation().JSON_decode(TitanEmbedded_PDV_identification_context__negotiation.TitanEmbedded_PDV_identification_context__negotiation_descr_, p_tok, true);
			case 4:
				return get_field_transfer__syntax().JSON_decode(TitanObjectid.TitanObjectid_descr_, p_tok, true);
			case 5:
				return get_field_fixed().JSON_decode(TitanAsn_Null.TitanAsn_Null_descr_, p_tok, true);
			}
		}
		final AtomicReference<json_token_t> j_token = new AtomicReference<json_token_t>(json_token_t.JSON_TOKEN_NONE);
		if (p_td.json.isAs_value()) {
			final int buf_pos = p_tok.get_buf_pos();
			p_tok.get_next_token(j_token, null, null);
			int ret_val = 0;
			switch(j_token.get()) {
			case JSON_TOKEN_NUMBER: {
				p_tok.set_buf_pos(buf_pos);
				ret_val = get_field_presentation__context__id().JSON_decode(TitanInteger.TitanInteger_descr_, p_tok, true);
				if (0 <= ret_val) {
					return ret_val;
				}
				if (!p_silent) {
					TTCN_EncDec_ErrorContext.error(error_type.ET_INVAL_MSG, JSON.JSON_DEC_AS_VALUE_ERROR, "number");
				}
				clean_up();
				return JSON.JSON_ERROR_FATAL;
			}
			case JSON_TOKEN_STRING: {
				p_tok.set_buf_pos(buf_pos);
				ret_val = get_field_syntax().JSON_decode(TitanObjectid.TitanObjectid_descr_, p_tok, true);
				if (0 <= ret_val) {
					return ret_val;
				}
				p_tok.set_buf_pos(buf_pos);
				ret_val = get_field_transfer__syntax().JSON_decode(TitanObjectid.TitanObjectid_descr_, p_tok, true);
				if (0 <= ret_val) {
					return ret_val;
				}
				if (!p_silent) {
					TTCN_EncDec_ErrorContext.error(error_type.ET_INVAL_MSG, JSON.JSON_DEC_AS_VALUE_ERROR, "string");
				}
				clean_up();
				return JSON.JSON_ERROR_FATAL;
			}
			case JSON_TOKEN_LITERAL_TRUE:
			case JSON_TOKEN_LITERAL_FALSE: {
				final String literal_str = "literal (" + ((json_token_t.JSON_TOKEN_LITERAL_TRUE == j_token.get()) ? "true" : "false") + ")";
				if (!p_silent) {
					TTCN_EncDec_ErrorContext.error(error_type.ET_INVAL_MSG, JSON.JSON_DEC_AS_VALUE_ERROR, literal_str);
				}
				clean_up();
				return JSON.JSON_ERROR_FATAL;
			}
			case JSON_TOKEN_ARRAY_START: {
				if (!p_silent) {
					TTCN_EncDec_ErrorContext.error(error_type.ET_INVAL_MSG, JSON.JSON_DEC_AS_VALUE_ERROR, "array");
				}
				clean_up();
				return JSON.JSON_ERROR_FATAL;
			}
			case JSON_TOKEN_OBJECT_START: {
				p_tok.set_buf_pos(buf_pos);
				ret_val = get_field_syntaxes().JSON_decode(TitanEmbedded_PDV_identification_syntaxes.TitanEmbedded_PDV_identification_syntaxes_descr_, p_tok, true);
				if (0 <= ret_val) {
					return ret_val;
				}
				p_tok.set_buf_pos(buf_pos);
				ret_val = get_field_context__negotiation().JSON_decode(TitanEmbedded_PDV_identification_context__negotiation.TitanEmbedded_PDV_identification_context__negotiation_descr_, p_tok, true);
				if (0 <= ret_val) {
					return ret_val;
				}
				if (!p_silent) {
					TTCN_EncDec_ErrorContext.error(error_type.ET_INVAL_MSG, JSON.JSON_DEC_AS_VALUE_ERROR, "object");
				}
				clean_up();
				return JSON.JSON_ERROR_FATAL;
			}
			case JSON_TOKEN_LITERAL_NULL: {
				p_tok.set_buf_pos(buf_pos);
				ret_val = get_field_fixed().JSON_decode(TitanAsn_Null.TitanAsn_Null_descr_, p_tok, true);
				if (0 <= ret_val) {
					return ret_val;
				}
				clean_up();
				return JSON.JSON_ERROR_INVALID_TOKEN;
			}
			case JSON_TOKEN_ERROR:
				TTCN_EncDec_ErrorContext.error(error_type.ET_INVAL_MSG, JSON.JSON_DEC_BAD_TOKEN_ERROR, "");
				return JSON.JSON_ERROR_FATAL;
			default:
				return JSON.JSON_ERROR_INVALID_TOKEN;
			}
		}
		else {
			int dec_len = p_tok.get_next_token(j_token, null, null);
			if (json_token_t.JSON_TOKEN_ERROR == j_token.get()) {
				TTCN_EncDec_ErrorContext.error(error_type.ET_INVAL_MSG, JSON.JSON_DEC_BAD_TOKEN_ERROR, "");
				return JSON.JSON_ERROR_FATAL;
			}
			else if (json_token_t.JSON_TOKEN_OBJECT_START != j_token.get()) {
				return JSON.JSON_ERROR_INVALID_TOKEN;
			}

			final StringBuilder fld_name = new StringBuilder();
			final AtomicInteger name_len = new AtomicInteger(0);
			dec_len += p_tok.get_next_token(j_token, fld_name, name_len);
			if (json_token_t.JSON_TOKEN_NAME != j_token.get()) {
				TTCN_EncDec_ErrorContext.error(error_type.ET_INVAL_MSG, JSON.JSON_DEC_NAME_TOKEN_ERROR);
				return JSON.JSON_ERROR_FATAL;
			} else {
				if (8 == name_len.get() && "syntaxes".equals(fld_name.substring(0,name_len.get()))) {
					final int ret_val = get_field_syntaxes().JSON_decode(TitanEmbedded_PDV_identification_syntaxes.TitanEmbedded_PDV_identification_syntaxes_descr_, p_tok, p_silent);
					if (0 > ret_val) {
						if (JSON.JSON_ERROR_INVALID_TOKEN == ret_val) {
							TTCN_EncDec_ErrorContext.error(error_type.ET_INVAL_MSG, JSON.JSON_DEC_FIELD_TOKEN_ERROR, "syntaxes");
						}
						return JSON.JSON_ERROR_FATAL;
					} else {
						dec_len += ret_val;
					}
				} else if (6 == name_len.get() && "syntax".equals(fld_name.substring(0,name_len.get()))) {
					final int ret_val = get_field_syntax().JSON_decode(TitanObjectid.TitanObjectid_descr_, p_tok, p_silent);
					if (0 > ret_val) {
						if (JSON.JSON_ERROR_INVALID_TOKEN == ret_val) {
							TTCN_EncDec_ErrorContext.error(error_type.ET_INVAL_MSG, JSON.JSON_DEC_FIELD_TOKEN_ERROR, "syntax");
						}
						return JSON.JSON_ERROR_FATAL;
					} else {
						dec_len += ret_val;
					}
				} else if (23 == name_len.get() && "presentation-context-id".equals(fld_name.substring(0,name_len.get()))) {
					final int ret_val = get_field_presentation__context__id().JSON_decode(TitanInteger.TitanInteger_descr_, p_tok, p_silent);
					if (0 > ret_val) {
						if (JSON.JSON_ERROR_INVALID_TOKEN == ret_val) {
							TTCN_EncDec_ErrorContext.error(error_type.ET_INVAL_MSG, JSON.JSON_DEC_FIELD_TOKEN_ERROR, "presentation-context-id");
						}
						return JSON.JSON_ERROR_FATAL;
					} else {
						dec_len += ret_val;
					}
				} else if (19 == name_len.get() && "context-negotiation".equals(fld_name.substring(0,name_len.get()))) {
					final int ret_val = get_field_context__negotiation().JSON_decode(TitanEmbedded_PDV_identification_context__negotiation.TitanEmbedded_PDV_identification_context__negotiation_descr_, p_tok, p_silent);
					if (0 > ret_val) {
						if (JSON.JSON_ERROR_INVALID_TOKEN == ret_val) {
							TTCN_EncDec_ErrorContext.error(error_type.ET_INVAL_MSG, JSON.JSON_DEC_FIELD_TOKEN_ERROR, "context-negotiation");
						}
						return JSON.JSON_ERROR_FATAL;
					} else {
						dec_len += ret_val;
					}
				} else if (15 == name_len.get() && "transfer-syntax".equals(fld_name.substring(0,name_len.get()))) {
					final int ret_val = get_field_transfer__syntax().JSON_decode(TitanObjectid.TitanObjectid_descr_, p_tok, p_silent);
					if (0 > ret_val) {
						if (JSON.JSON_ERROR_INVALID_TOKEN == ret_val) {
							TTCN_EncDec_ErrorContext.error(error_type.ET_INVAL_MSG, JSON.JSON_DEC_FIELD_TOKEN_ERROR, "transfer-syntax");
						}
						return JSON.JSON_ERROR_FATAL;
					} else {
						dec_len += ret_val;
					}
				} else if (5 == name_len.get() && "fixed".equals(fld_name.substring(0,name_len.get()))) {
					final int ret_val = get_field_fixed().JSON_decode(TitanAsn_Null.TitanAsn_Null_descr_, p_tok, p_silent);
					if (0 > ret_val) {
						if (JSON.JSON_ERROR_INVALID_TOKEN == ret_val) {
							TTCN_EncDec_ErrorContext.error(error_type.ET_INVAL_MSG, JSON.JSON_DEC_FIELD_TOKEN_ERROR, "fixed");
						}
						return JSON.JSON_ERROR_FATAL;
					} else {
						dec_len += ret_val;
					}
				} else {
					TTCN_EncDec_ErrorContext.error(error_type.ET_INVAL_MSG, JSON.JSON_DEC_INVALID_NAME_ERROR, fld_name);
					return JSON.JSON_ERROR_FATAL;
				}
			}

			dec_len += p_tok.get_next_token(j_token, null, null);
			if (json_token_t.JSON_TOKEN_OBJECT_END != j_token.get()) {
				if (!p_silent) {
					TTCN_EncDec_ErrorContext.error(error_type.ET_INVAL_MSG, JSON.JSON_DEC_STATIC_OBJECT_END_TOKEN_ERROR, "");
				}
				return JSON.JSON_ERROR_FATAL;
			}

			return dec_len;
		}
	}

	/**
	 * The encoder function for type EMBEDDED PDV.identification.
	 *
	 * @param input_value
	 *                the input value to encode.
	 * @param output_stream
	 *                the octetstring to be extend with the result of the
	 *                encoding.
	 * @param coding_name
	 *                the name of the coding to use.
	 * */
	public static void TitanEmbedded_PDV_identification_encoder(final TitanEmbedded_PDV_identification input_value, final TitanOctetString output_stream, final TitanUniversalCharString coding_name) {
		final AtomicInteger extra_options = new AtomicInteger(0);
		final TTCN_EncDec.coding_type codingType = TTCN_EncDec.get_coding_from_str(coding_name, extra_options, true);
		if (codingType != TTCN_EncDec.coding_type.CT_JSON) {
			TTCN_Logger.begin_event_log2str();
			coding_name.log();
			throw new TtcnError(MessageFormat.format("Type `EMBEDDED PDV.identification' does not support {0} encoding", TTCN_Logger.end_event_log2str()));
		}
		final TTCN_Buffer ttcnBuffer = new TTCN_Buffer();
		input_value.encode(TitanEmbedded_PDV_identification.TitanEmbedded_PDV_identification_descr_, ttcnBuffer, codingType, extra_options.get());
		ttcnBuffer.get_string(output_stream);
	}

	/**
	 * The decoder function for type EMBEDDED PDV.identification. In case
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
	public static int TitanEmbedded_PDV_identification_decoder(final TitanOctetString input_stream, final TitanEmbedded_PDV_identification output_value, final TitanUniversalCharString coding_name) {
		final AtomicInteger extra_options = new AtomicInteger(0);
		final TTCN_EncDec.coding_type codingType = TTCN_EncDec.get_coding_from_str(coding_name, extra_options, false);
		if (codingType != TTCN_EncDec.coding_type.CT_JSON) {
			TTCN_Logger.begin_event_log2str();
			coding_name.log();
			throw new TtcnError(MessageFormat.format("Type `EMBEDDED PDV.identification' does not support {0} encoding", TTCN_Logger.end_event_log2str()));
		}
		final TTCN_Buffer ttcnBuffer = new TTCN_Buffer(input_stream);
		output_value.decode(TitanEmbedded_PDV_identification.TitanEmbedded_PDV_identification_descr_, ttcnBuffer, codingType, extra_options.get());
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
	public static void TitanEmbedded_PDV_identification_syntax_encoder(final TitanObjectid input_value, final TitanOctetString output_stream, final TitanUniversalCharString coding_name) {
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
	public static int TitanEmbedded_PDV_identification_syntax_decoder(final TitanOctetString input_stream, final TitanObjectid output_value, final TitanUniversalCharString coding_name) {
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
	public static void TitanEmbedded_PDV_identification_transfer__syntax_encoder(final TitanObjectid input_value, final TitanOctetString output_stream, final TitanUniversalCharString coding_name) {
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
	public static int TitanEmbedded_PDV_identification_transfer__syntax_decoder(final TitanOctetString input_stream, final TitanObjectid output_value, final TitanUniversalCharString coding_name) {
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
	 * The encoder function for type NULL.
	 *
	 * @param input_value
	 *                the input value to encode.
	 * @param output_stream
	 *                the octetstring to be extend with the result of the
	 *                encoding.
	 * @param coding_name
	 *                the name of the coding to use.
	 * */
	public static void TitanEmbedded_PDV_identification_fixed_encoder(final TitanAsn_Null input_value, final TitanOctetString output_stream, final TitanUniversalCharString coding_name) {
		final AtomicInteger extra_options = new AtomicInteger(0);
		final TTCN_EncDec.coding_type codingType = TTCN_EncDec.get_coding_from_str(coding_name, extra_options, true);
		if (codingType != TTCN_EncDec.coding_type.CT_JSON) {
			TTCN_Logger.begin_event_log2str();
			coding_name.log();
			throw new TtcnError(MessageFormat.format("Type `NULL' does not support {0} encoding", TTCN_Logger.end_event_log2str()));
		}
		final TTCN_Buffer ttcnBuffer = new TTCN_Buffer();
		input_value.encode(TitanAsn_Null.TitanAsn_Null_descr_, ttcnBuffer, codingType, extra_options.get());
		ttcnBuffer.get_string(output_stream);
	}

	/**
	 * The decoder function for type NULL. In case
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
	public static int TitanEmbedded_PDV_identification_fixed_decoder(final TitanOctetString input_stream, final TitanAsn_Null output_value, final TitanUniversalCharString coding_name) {
		final AtomicInteger extra_options = new AtomicInteger(0);
		final TTCN_EncDec.coding_type codingType = TTCN_EncDec.get_coding_from_str(coding_name, extra_options, false);
		if (codingType != TTCN_EncDec.coding_type.CT_JSON) {
			TTCN_Logger.begin_event_log2str();
			coding_name.log();
			throw new TtcnError(MessageFormat.format("Type `NULL' does not support {0} encoding", TTCN_Logger.end_event_log2str()));
		}
		final TTCN_Buffer ttcnBuffer = new TTCN_Buffer(input_stream);
		output_value.decode(TitanAsn_Null.TitanAsn_Null_descr_, ttcnBuffer, codingType, extra_options.get());
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