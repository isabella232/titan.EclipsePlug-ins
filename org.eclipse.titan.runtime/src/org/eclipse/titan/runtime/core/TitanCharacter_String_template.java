/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.titan.runtime.core.Param_Types.Module_Parameter;

/**
 * Part of the representation of the ASN.1 unrestricted string (CHARACTER STRING) type.
 *
 * @author Kristof Szabados
 */
public class TitanCharacter_String_template extends Base_Template {

	private TitanCharacter_String_identification_template identification; //ASN1_Choice_Type
	private TitanUniversalCharString_template data__value__descriptor; //ObjectDescriptor_Type
	private TitanOctetString_template string__value; //OctetString_Type
	//originally value_list/list_value
	private List<TitanCharacter_String_template> list_value;

	public TitanCharacter_String_identification_template getidentification() {
		setSpecific();
		return identification;
	}

	public TitanCharacter_String_identification_template constGetidentification() {
		if (templateSelection != template_sel.SPECIFIC_VALUE) {
			throw new TtcnError("Accessing field identification of a non-specific template of type CHARACTER STRING.");
		}
		return identification;
	}

	public TitanUniversalCharString_template getdata__value__descriptor() {
		setSpecific();
		return data__value__descriptor;
	}

	public TitanUniversalCharString_template constGetdata__value__descriptor() {
		if (templateSelection != template_sel.SPECIFIC_VALUE) {
			throw new TtcnError("Accessing field data-value-descriptor of a non-specific template of type CHARACTER STRING.");
		}
		return data__value__descriptor;
	}

	public TitanOctetString_template getstring__value() {
		setSpecific();
		return string__value;
	}

	public TitanOctetString_template constGetstring__value() {
		if (templateSelection != template_sel.SPECIFIC_VALUE) {
			throw new TtcnError("Accessing field string-value of a non-specific template of type CHARACTER STRING.");
		}
		return string__value;
	}

	private void setSpecific() {
		if (templateSelection != template_sel.SPECIFIC_VALUE) {
			final template_sel old_selection = templateSelection;
			cleanUp();
			set_selection(template_sel.SPECIFIC_VALUE);
			identification = new TitanCharacter_String_identification_template();
			data__value__descriptor = new TitanUniversalCharString_template();
			string__value = new TitanOctetString_template();
			if (old_selection == template_sel.ANY_VALUE || old_selection == template_sel.ANY_OR_OMIT) {
				identification.assign(template_sel.ANY_VALUE);
				data__value__descriptor.assign(template_sel.ANY_OR_OMIT);
				string__value.assign(template_sel.ANY_VALUE);
			}
		}
	}

	public TitanCharacter_String_template() {
		// do nothing
	}

	public TitanCharacter_String_template(final template_sel other_value ) {
		super( other_value );
		checkSingleSelection( other_value );
	}

	public TitanCharacter_String_template( final TitanCharacter_String otherValue ) {
		copyValue(otherValue);
	}

	public TitanCharacter_String_template( final TitanCharacter_String_template otherValue ) {
		copyTemplate( otherValue );
	}

	public TitanCharacter_String_template( final Optional<TitanCharacter_String> other_value ) {
		switch (other_value.get_selection()) {
		case OPTIONAL_PRESENT:
			copyValue(other_value.constGet());
			break;
		case OPTIONAL_OMIT:
			set_selection(template_sel.OMIT_VALUE);
			break;
		default:
			throw new TtcnError("Creating a template of type CHARACTER STRING from an unbound optional field.");
		}
	}

	//originally operator=
	public TitanCharacter_String_template assign( final template_sel other_value ) {
		checkSingleSelection(other_value);
		cleanUp();
		set_selection(other_value);
		return this;
	}

	//originally operator=
	public TitanCharacter_String_template assign( final TitanCharacter_String other_value ) {
		cleanUp();
		copyValue(other_value);
		return this;
	}

	//originally operator=
	public TitanCharacter_String_template assign( final TitanCharacter_String_template other_value ) {
		if (other_value != this) {
			cleanUp();
			copyTemplate(other_value);
		}
		return this;
	}

	@Override
	public TitanCharacter_String_template assign(final Base_Type otherValue) {
		if (otherValue instanceof TitanCharacter_String) {
			return assign((TitanCharacter_String) otherValue);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `TitanCharacter_String' can not be cast to {1}", otherValue));
	}

	@Override
	public TitanCharacter_String_template assign(final Base_Template otherValue) {
		if (otherValue instanceof TitanCharacter_String_template) {
			return assign((TitanCharacter_String_template) otherValue);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `TitanCharacter_String' can not be cast to {1}_template", otherValue));
	}

	public TitanCharacter_String_template assign( final Optional<TitanCharacter_String> other_value ) {
		cleanUp();
		switch (other_value.get_selection()) {
		case OPTIONAL_PRESENT:
			copyValue(other_value.constGet());
			break;
		case OPTIONAL_OMIT:
			set_selection(template_sel.OMIT_VALUE);
			break;
		default:
			throw new TtcnError("Assignment of an unbound optional field to a template of type CHARACTER STRING.");
		}
		return this;
	}

	private void copyValue(final TitanCharacter_String other_value) {
		if (other_value.getidentification().isBound()) {
			getidentification().assign(other_value.getidentification());
		} else {
			getidentification().cleanUp();
		}
		if (other_value.getdata__value__descriptor().isBound()) {
			if (other_value.getdata__value__descriptor().isPresent()) {
				getdata__value__descriptor().assign(other_value.getdata__value__descriptor().get());
			} else {
				getdata__value__descriptor().assign(template_sel.OMIT_VALUE);
			}
		} else {
			getdata__value__descriptor().cleanUp();
		}
		if (other_value.getstring__value().isBound()) {
			getstring__value().assign(other_value.getstring__value());
		} else {
			getstring__value().cleanUp();
		}
		set_selection(template_sel.SPECIFIC_VALUE);
	}

	private void copyTemplate(final TitanCharacter_String_template other_value) {
		switch (other_value.templateSelection) {
		case SPECIFIC_VALUE:
			if (template_sel.UNINITIALIZED_TEMPLATE == other_value.getidentification().get_selection()) {
				getidentification().cleanUp();
			} else {
				getidentification().assign(other_value.getidentification());
			}
			if (template_sel.UNINITIALIZED_TEMPLATE == other_value.getdata__value__descriptor().get_selection()) {
				getdata__value__descriptor().cleanUp();
			} else {
				getdata__value__descriptor().assign(other_value.getdata__value__descriptor());
			}
			if (template_sel.UNINITIALIZED_TEMPLATE == other_value.getstring__value().get_selection()) {
				getstring__value().cleanUp();
			} else {
				getstring__value().assign(other_value.getstring__value());
			}
			break;
		case OMIT_VALUE:
		case ANY_VALUE:
		case ANY_OR_OMIT:
			break;
		case VALUE_LIST:
		case COMPLEMENTED_LIST:
			list_value = new ArrayList<TitanCharacter_String_template>(other_value.list_value.size());
			for(int i = 0; i < other_value.list_value.size(); i++) {
				final TitanCharacter_String_template temp = new TitanCharacter_String_template(other_value.list_value.get(i));
				list_value.add(temp);
			}
			break;
		default:
			throw new TtcnError("Copying an uninitialized template of type CHARACTER STRING.");
		}
		set_selection(other_value);
	}

	public boolean isPresent() {
		return isPresent(false);
	}

	public boolean isPresent(final boolean legacy) {
		return isPresent_(legacy);
	}

	private boolean isPresent_(final boolean legacy) {
		if (templateSelection==template_sel.UNINITIALIZED_TEMPLATE) {
			return false;
		}
		return !match_omit_(legacy);
	}

	public boolean match_omit() {
		return match_omit(false);
	}

	public boolean match_omit(final boolean legacy) {
		return match_omit_(legacy);
	}

	private boolean match_omit_(final boolean legacy) {
		if (is_ifPresent) {
			return true;
		}
		switch (templateSelection) {
		case OMIT_VALUE:
		case ANY_OR_OMIT:
			return true;
		case VALUE_LIST:
		case COMPLEMENTED_LIST:
			if (legacy) {
				for (int l_idx=0; l_idx<list_value.size(); l_idx++) {
					if (list_value.get(l_idx).match_omit_(legacy)) {
						return templateSelection==template_sel.VALUE_LIST;
					}
				}
				return templateSelection==template_sel.COMPLEMENTED_LIST;
			} // else fall through
		default:
			return false;
		}
	}

	public TitanCharacter_String valueOf() {
		if (templateSelection != template_sel.SPECIFIC_VALUE || is_ifPresent) {
			throw new TtcnError("Performing a valueof or send operation on a non-specific template of type CHARACTER STRING.");
		}
		final TitanCharacter_String ret_val = new TitanCharacter_String();
		if (identification.isBound()) {
			ret_val.getidentification().assign(identification.valueOf());
		}
		if (data__value__descriptor.isOmit()) {
			ret_val.getdata__value__descriptor().assign(template_sel.OMIT_VALUE);
		} else if (data__value__descriptor.isBound()) {
			ret_val.getdata__value__descriptor().assign(data__value__descriptor.valueOf());
		}
		if (string__value.isBound()) {
			ret_val.getstring__value().assign(string__value.valueOf());
		}
		return ret_val;
	}

	public TitanCharacter_String_template listItem(final int list_index) {
		if (templateSelection != template_sel.VALUE_LIST && templateSelection != template_sel.COMPLEMENTED_LIST) {
			throw new TtcnError("Accessing a list element of a non-list template of type CHARACTER STRING.");
		}
		if (list_index >= list_value.size()) {
			throw new TtcnError("Index overflow in a value list template of type CHARACTER STRING.");
		}
		return list_value.get(list_index);
	}

	public void setType(final template_sel template_type, final int list_length) {
		if (template_type != template_sel.VALUE_LIST && template_type != template_sel.COMPLEMENTED_LIST) {
			throw new TtcnError("Setting an invalid list for a template of type CHARACTER STRING.");
		}
		cleanUp();
		set_selection(template_type);
		list_value = new ArrayList<TitanCharacter_String_template>(list_length);
		for(int i = 0 ; i < list_length; i++) {
			list_value.add(new TitanCharacter_String_template());
		}
	}

	@Override
	public boolean isBound() {
		if (templateSelection == template_sel.UNINITIALIZED_TEMPLATE && !is_ifPresent) {
			return false;
		}
		if (templateSelection != template_sel.SPECIFIC_VALUE) {
			return true;
		}
		if (identification.isBound()) {
			return true;
		}
		if (data__value__descriptor.isOmit() || data__value__descriptor.isBound()) {
			return true;
		}
		if (string__value.isBound()) {
			return true;
		}
		return false;
	}

	@Override
	public boolean isValue() {
		if (templateSelection != template_sel.SPECIFIC_VALUE || is_ifPresent) {
			return false;
		}
		if (!identification.isValue()) {
			return false;
		}
		if (!data__value__descriptor.isOmit() && !data__value__descriptor.isValue()) {
			return false;
		}
		if (!string__value.isValue()) {
			return false;
		}
		return true;
	}

	public boolean match(final TitanCharacter_String other_value) {
		return match(other_value, false);
	}

	public boolean match(final TitanCharacter_String other_value, final boolean legacy) {
		if (!other_value.isBound()) {
			return false;
		}
		switch (templateSelection) {
		case ANY_VALUE:
		case ANY_OR_OMIT:
			return true;
		case OMIT_VALUE:
			return false;
		case SPECIFIC_VALUE:
			if(!other_value.getidentification().isBound()) {
				return false;
			}
			if(!identification.match(other_value.getidentification(), legacy)) {
				return false;
			}
			if(!other_value.getdata__value__descriptor().isBound()) {
				return false;
			}
			if((other_value.getdata__value__descriptor().isPresent() ? !data__value__descriptor.match(other_value.getdata__value__descriptor().get(), legacy) : !data__value__descriptor.match_omit(legacy))) {
				return false;
			}
			if(!other_value.getstring__value().isBound()) {
				return false;
			}
			if(!string__value.match(other_value.getstring__value(), legacy)) {
				return false;
			}
			return true;
		case VALUE_LIST:
		case COMPLEMENTED_LIST:
			for (int list_count = 0; list_count < list_value.size(); list_count++) {
				if (list_value.get(list_count).match(other_value, legacy)) {
					return templateSelection == template_sel.VALUE_LIST;
				}
			}
			return templateSelection == template_sel.COMPLEMENTED_LIST;
		default:
			throw new TtcnError("Matching an uninitialized/unsupported template of type CHARACTER STRING.");
		}
	}

	@Override
	public boolean match(final Base_Type otherValue, final boolean legacy) {
		if (otherValue instanceof TitanCharacter_String) {
			return match((TitanCharacter_String)otherValue, legacy);
		}

		throw new TtcnError("Internal Error: The left operand of assignment is not of type TitanCharacter_String.");
	}

	public TitanInteger sizeOf() {
		if (is_ifPresent) {
			throw new TtcnError("Performing sizeof() operation on a template of type CHARACTER STRING which has an ifpresent attribute.");
		}
		switch (templateSelection) {
		case SPECIFIC_VALUE:
			int sizeof = 2;
			if (data__value__descriptor.isPresent()) {
				sizeof++;
			}
			return new TitanInteger(sizeof);
		case VALUE_LIST:
			if (list_value.isEmpty()) {
				throw new TtcnError("Internal error: Performing sizeof() operation on a template of type CHARACTER STRING containing an empty list.");
			}
			final int item_size = list_value.get(0).sizeOf().getInt();
			for (int l_idx = 1; l_idx < list_value.size(); l_idx++) {
				if (list_value.get(l_idx).sizeOf().getInt() != item_size) {
					throw new TtcnError("Performing sizeof() operation on a template of type CHARACTER STRING containing a value list with different sizes.");
				}
			}
			return new TitanInteger(item_size);
		case OMIT_VALUE:
			throw new TtcnError("Performing sizeof() operation on a template of type CHARACTER STRING containing omit value.");
		case ANY_VALUE:
		case ANY_OR_OMIT:
			throw new TtcnError("Performing sizeof() operation on a template of type CHARACTER STRING containing */? value.");
		case COMPLEMENTED_LIST:
			throw new TtcnError("Performing sizeof() operation on a template of type CHARACTER STRING containing complemented list.");
		default:
			throw new TtcnError("Performing sizeof() operation on an uninitialized/unsupported template of type CHARACTER STRING.");
		}
	}

	public void log() {
		switch (templateSelection) {
		case SPECIFIC_VALUE:
			TTCN_Logger.log_char('{');
			TTCN_Logger.log_event_str(" identification := ");
			identification.log();
			TTCN_Logger.log_char(',');
			TTCN_Logger.log_event_str(" data-value-descriptor := ");
			data__value__descriptor.log();
			TTCN_Logger.log_char(',');
			TTCN_Logger.log_event_str(" string-value := ");
			string__value.log();
			TTCN_Logger.log_event_str(" }");
			break;
		case COMPLEMENTED_LIST:
			TTCN_Logger.log_event_str("complement");
		case VALUE_LIST:
			TTCN_Logger.log_char('(');
			for (int list_count = 0; list_count < list_value.size(); list_count++) {
				if (list_count > 0) {
					TTCN_Logger.log_event_str(", ");
				}
				list_value.get(list_count).log();
			}
			TTCN_Logger.log_char(')');
			break;
		default:
			log_generic();
			break;
		}
		log_ifpresent();
	}

	public void log_match(final TitanCharacter_String match_value) {
		log_match(match_value, false);
	}

	@Override
	public void log_match(final Base_Type match_value, final boolean legacy) {
		if (match_value instanceof TitanCharacter_String) {
			log_match((TitanCharacter_String)match_value, legacy);
			return;
		}

		throw new TtcnError("Internal Error: value can not be cast to CHARACTER STRING.");
	}

	public void log_match(final TitanCharacter_String match_value, final boolean legacy) {
		if ( TTCN_Logger.matching_verbosity_t.VERBOSITY_COMPACT == TTCN_Logger.get_matching_verbosity() ) {
			if(match(match_value, legacy)) {
				TTCN_Logger.print_logmatch_buffer();
				TTCN_Logger.log_event_str(" matched");
			} else {
				if (templateSelection == template_sel.SPECIFIC_VALUE) {
					final int previous_size = TTCN_Logger.get_logmatch_buffer_len();
					if( !identification.match(match_value.constGetidentification(), legacy) ) {
						TTCN_Logger.log_logmatch_info(".identification");
						identification.log_match(match_value.constGetidentification(), legacy);
						TTCN_Logger.set_logmatch_buffer_len(previous_size);
					}
					if (match_value.constGetdata__value__descriptor().isPresent()) {
						if( !data__value__descriptor.match(match_value.constGetdata__value__descriptor().get(), legacy) ) {
							TTCN_Logger.log_logmatch_info(".data-value-descriptor");
							data__value__descriptor.log_match(match_value.constGetdata__value__descriptor().get(), legacy);
							TTCN_Logger.set_logmatch_buffer_len(previous_size);
						}
					} else {
						if (!data__value__descriptor.match_omit(legacy)) {
							TTCN_Logger.log_logmatch_info(".data-value-descriptor := omit with ");
							TTCN_Logger.print_logmatch_buffer();
							data__value__descriptor.log();
							TTCN_Logger.log_event_str(" unmatched");
							TTCN_Logger.set_logmatch_buffer_len(previous_size);
						}
					}
					if( !string__value.match(match_value.constGetstring__value(), legacy) ) {
						TTCN_Logger.log_logmatch_info(".string-value");
						string__value.log_match(match_value.constGetstring__value(), legacy);
						TTCN_Logger.set_logmatch_buffer_len(previous_size);
					}
				} else {
					TTCN_Logger.print_logmatch_buffer();
					match_value.log();
					TTCN_Logger.log_event_str(" with ");
					log();
					TTCN_Logger.log_event_str(" unmatched");
				}
			}
			return;
		}
		if (templateSelection == template_sel.SPECIFIC_VALUE) {
			TTCN_Logger.log_event_str("{ identification := ");
			identification.log_match(match_value.constGetidentification(), legacy);
			TTCN_Logger.log_event_str("{ data-value-descriptor := ");
			data__value__descriptor.log_match(match_value.constGetdata__value__descriptor(), legacy);
			TTCN_Logger.log_event_str("{ string-value := ");
			string__value.log_match(match_value.constGetstring__value(), legacy);
			TTCN_Logger.log_event_str(" }");
		} else {
			match_value.log();
			TTCN_Logger.log_event_str(" with ");
			log();
			if ( match(match_value, legacy) ) {
				TTCN_Logger.log_event_str(" matched");
			} else {
				TTCN_Logger.log_event_str(" unmatched");
			}
		}
	}

	@Override
	public void encode_text(final Text_Buf text_buf) {
		encode_text_base(text_buf);
		switch (templateSelection) {
		case OMIT_VALUE:
		case ANY_VALUE:
		case ANY_OR_OMIT:
			break;
		case SPECIFIC_VALUE:
			identification.encode_text(text_buf);
			data__value__descriptor.encode_text(text_buf);
			string__value.encode_text(text_buf);
			break;
		case VALUE_LIST:
		case COMPLEMENTED_LIST:
			text_buf.push_int(list_value.size());
			for (int i = 0; i < list_value.size(); i++) {
				list_value.get(i).encode_text(text_buf);
			}
			break;
		default:
			throw new TtcnError("Text encoder: Encoding an uninitialized/unsupported template of type CHARACTER STRING.");
		}
	}

	@Override
	public void decode_text(final Text_Buf text_buf) {
		cleanUp();
		decode_text_base(text_buf);
		switch (templateSelection) {
		case OMIT_VALUE:
		case ANY_VALUE:
		case ANY_OR_OMIT:
			break;
		case SPECIFIC_VALUE:
			identification = new TitanCharacter_String_identification_template();
			identification.decode_text(text_buf);
			data__value__descriptor = new TitanUniversalCharString_template();
			data__value__descriptor.decode_text(text_buf);
			string__value = new TitanOctetString_template();
			string__value.decode_text(text_buf);
			break;
		case VALUE_LIST:
		case COMPLEMENTED_LIST: {
			final int size = text_buf.pull_int().getInt();
			list_value = new ArrayList<TitanCharacter_String_template>(size);
			for (int i = 0; i < size; i++) {
				final TitanCharacter_String_template temp = new TitanCharacter_String_template();
				temp.decode_text(text_buf);
				list_value.add(temp);
			}
			break;
		}
		default:
			throw new TtcnError("Text decoder: An unknown/unsupported selection was received in a template of type CHARACTER STRING.");
		}
	}

	@Override
	public void set_param(final Module_Parameter param) {
		param.basic_check(Module_Parameter.basic_check_bits_t.BC_TEMPLATE.getValue(), "record template");
		switch (param.get_type()) {
		case MP_Omit:
			assign(template_sel.OMIT_VALUE);
			break;
		case MP_Any:
			assign(template_sel.ANY_VALUE);
			break;
		case MP_AnyOrNone:
			assign(template_sel.ANY_OR_OMIT);
			break;
		case MP_List_Template:
		case MP_ComplementList_Template: {
			final int size = param.get_size();
			setType(param.get_type() == Module_Parameter.type_t.MP_List_Template ? template_sel.VALUE_LIST : template_sel.COMPLEMENTED_LIST, size);
			for (int i = 0; i < size; i++) {
				listItem(i).set_param(param.get_elem(i));
			}
			break;
		}
		case MP_Value_List:
			if (param.get_size() > 3) {
				param.error(MessageFormat.format("record template of type CHARACTER STRING has 3 fields but list value has {0} fields.", param.get_size()));
			}
			if (param.get_size() > 0 && param.get_elem(0).get_type() != Module_Parameter.type_t.MP_NotUsed) {
				getidentification().set_param(param.get_elem(0));
			}
			if (param.get_size() > 1 && param.get_elem(1).get_type() != Module_Parameter.type_t.MP_NotUsed) {
				getdata__value__descriptor().set_param(param.get_elem(1));
			}
			if (param.get_size() > 2 && param.get_elem(2).get_type() != Module_Parameter.type_t.MP_NotUsed) {
				getstring__value().set_param(param.get_elem(2));
			}
			break;
		case MP_Assignment_List: {
			final boolean value_used[] = new boolean[param.get_size()];
			for (int val_idx = 0; val_idx < param.get_size(); val_idx++) {
				final Module_Parameter curr_param = param.get_elem(val_idx);
				if ("identification".equals(curr_param.get_id().get_name())) {
					if (curr_param.get_type() != Module_Parameter.type_t.MP_NotUsed) {
						getidentification().set_param(curr_param);
					}
					value_used[val_idx] = true;
				}
			}
			for (int val_idx = 0; val_idx < param.get_size(); val_idx++) {
				final Module_Parameter curr_param = param.get_elem(val_idx);
				if ("data-value-descriptor".equals(curr_param.get_id().get_name())) {
					if (curr_param.get_type() != Module_Parameter.type_t.MP_NotUsed) {
						getdata__value__descriptor().set_param(curr_param);
					}
					value_used[val_idx] = true;
				}
			}
			for (int val_idx = 0; val_idx < param.get_size(); val_idx++) {
				final Module_Parameter curr_param = param.get_elem(val_idx);
				if ("string-value".equals(curr_param.get_id().get_name())) {
					if (curr_param.get_type() != Module_Parameter.type_t.MP_NotUsed) {
						getstring__value().set_param(curr_param);
					}
					value_used[val_idx] = true;
				}
			}
			for (int val_idx = 0; val_idx < param.get_size(); val_idx++) {
				if (!value_used[val_idx]) {
					final Module_Parameter curr_param = param.get_elem(val_idx);
					curr_param.error(MessageFormat.format("Non existent field name in type CHARACTER STRING: {0}", curr_param.get_id().get_name()));
					break;
				}
			}
			break;
		}
		default:
			param.type_error("record template", "CHARACTER STRING");
		}
		is_ifPresent = param.get_ifpresent();
	}
}