/******************************************************************************
 * Copyright (c) 2000-2021 Ericsson Telecom AB
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
 * Part of the representation of the ASN.1 EXTERNAL type
 *
 * @author Kristof Szabados
 */
public class TitanExternal_identification_syntaxes_template extends Base_Template {
	private TitanObjectid_template abstract_; //ObjectID_Type
	private TitanObjectid_template transfer; //ObjectID_Type
	//originally value_list/list_value
	private List<TitanExternal_identification_syntaxes_template> list_value;


	/**
	 * Initializes to unbound/uninitialized template.
	 * */
	public TitanExternal_identification_syntaxes_template() {
		// do nothing
	}

	/**
	 * Initializes to a given template kind.
	 *
	 * @param otherValue
	 *                the template kind to initialize to.
	 * */
	public TitanExternal_identification_syntaxes_template(final template_sel otherValue ) {
		super( otherValue );
		check_single_selection( otherValue );
	}

	/**
	 * Initializes to a given value.
	 * The template becomes a specific template.
	 * The elements of the provided value are copied.
	 *
	 * @param otherValue
	 *                the value to initialize to.
	 * */
	public TitanExternal_identification_syntaxes_template( final TitanExternal_identification_syntaxes otherValue ) {
		copy_value(otherValue);
	}

	/**
	 * Initializes to a given template.
	 * The elements of the provided template are copied.
	 *
	 * @param otherValue
	 *                the value to initialize to.
	 * */
	public TitanExternal_identification_syntaxes_template( final TitanExternal_identification_syntaxes_template otherValue ) {
		copy_template( otherValue );
	}

	/**
	 * Initializes to a given value.
	 * The template becomes a specific template with the provided value.
	 * Causes a dynamic testcase error if the value is neither present nor optional.
	 *
	 * @param otherValue
	 *                the value to initialize to.
	 * */
	public TitanExternal_identification_syntaxes_template( final Optional<TitanExternal_identification_syntaxes> otherValue ) {
		switch (otherValue.get_selection()) {
		case OPTIONAL_PRESENT:
			copy_value(otherValue.constGet());
			break;
		case OPTIONAL_OMIT:
			set_selection(template_sel.OMIT_VALUE);
			break;
		default:
			throw new TtcnError("Creating a template of type EXTERNAL.identification.syntaxes from an unbound optional field.");
		}
	}

	@Override
	public TitanExternal_identification_syntaxes_template operator_assign( final template_sel otherValue ) {
		check_single_selection(otherValue);
		clean_up();
		set_selection(otherValue);
		return this;
	}

	/**
	 * Assigns the other value to this template.
	 * Overwriting the current content in the process.
	 *<p>
	 * operator= in the core.
	 *
	 * @param otherValue
	 *                the other value to assign.
	 * @return the new template object.
	 */
	public TitanExternal_identification_syntaxes_template operator_assign( final TitanExternal_identification_syntaxes otherValue ) {
		clean_up();
		copy_value(otherValue);
		return this;
	}

	/**
	 * Assigns the other template to this template.
	 * Overwriting the current content in the process.
	 *<p>
	 * operator= in the core.
	 *
	 * @param otherValue
	 *                the other value to assign.
	 * @return the new template object.
	 */
	public TitanExternal_identification_syntaxes_template operator_assign( final TitanExternal_identification_syntaxes_template otherValue ) {
		if (otherValue != this) {
			clean_up();
			copy_template(otherValue);
		}
		return this;
	}

	@Override
	public TitanExternal_identification_syntaxes_template operator_assign(final Base_Type otherValue) {
		if (otherValue instanceof TitanExternal_identification_syntaxes) {
			return operator_assign((TitanExternal_identification_syntaxes) otherValue);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to `TitanExternal_identification_syntaxes''", otherValue));
	}

	@Override
	public TitanExternal_identification_syntaxes_template operator_assign(final Base_Template otherValue) {
		if (otherValue instanceof TitanExternal_identification_syntaxes_template) {
			return operator_assign((TitanExternal_identification_syntaxes_template) otherValue);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to `TitanExternal_identification_syntaxes_template''", otherValue));
	}

	/**
	 * Assigns the other value to this template.
	 * Overwriting the current content in the process.
	 *<p>
	 * operator= in the core.
	 *
	 * @param otherValue
	 *                the other value to assign.
	 * @return the new template object.
	 */
	public TitanExternal_identification_syntaxes_template operator_assign( final Optional<TitanExternal_identification_syntaxes> otherValue ) {
		clean_up();
		switch (otherValue.get_selection()) {
		case OPTIONAL_PRESENT:
			copy_value(otherValue.constGet());
			break;
		case OPTIONAL_OMIT:
			set_selection(template_sel.OMIT_VALUE);
			break;
		default:
			throw new TtcnError("Assignment of an unbound optional field to a template of type EXTERNAL.identification.syntaxes.");
		}
		return this;
	}

	/**
	 * Internal function to copy the provided value into this template.
	 * The template becomes a specific value template.
	 * The already existing content is overwritten.
	 *
	 * @param other_value the value to be copied.
	 * */
	private void copy_value(final TitanExternal_identification_syntaxes other_value) {
		if (other_value.get_field_abstract_().is_bound()) {
			get_field_abstract_().operator_assign(other_value.get_field_abstract_());
		} else {
			get_field_abstract_().clean_up();
		}
		if (other_value.get_field_transfer().is_bound()) {
			get_field_transfer().operator_assign(other_value.get_field_transfer());
		} else {
			get_field_transfer().clean_up();
		}
		set_selection(template_sel.SPECIFIC_VALUE);
	}

	/**
	 * Internal function to copy the provided template into this template.
	 * The already existing content is overwritten.
	 *
	 * @param other_value the value to be copied.
	 * */
	private void copy_template(final TitanExternal_identification_syntaxes_template other_value) {
		switch (other_value.template_selection) {
		case SPECIFIC_VALUE:
			if (template_sel.UNINITIALIZED_TEMPLATE == other_value.get_field_abstract_().get_selection()) {
				get_field_abstract_().clean_up();
			} else {
				get_field_abstract_().operator_assign(other_value.get_field_abstract_());
			}
			if (template_sel.UNINITIALIZED_TEMPLATE == other_value.get_field_transfer().get_selection()) {
				get_field_transfer().clean_up();
			} else {
				get_field_transfer().operator_assign(other_value.get_field_transfer());
			}
			break;
		case OMIT_VALUE:
		case ANY_VALUE:
		case ANY_OR_OMIT:
			break;
		case VALUE_LIST:
		case COMPLEMENTED_LIST:
			list_value = new ArrayList<TitanExternal_identification_syntaxes_template>(other_value.list_value.size());
			for(int i = 0; i < other_value.list_value.size(); i++) {
				final TitanExternal_identification_syntaxes_template temp = new TitanExternal_identification_syntaxes_template(other_value.list_value.get(i));
				list_value.add(temp);
			}
			break;
		default:
			throw new TtcnError("Copying an uninitialized/unsupported template of type EXTERNAL.identification.syntaxes.");
		}
		set_selection(other_value);
	}

	@Override
	public void set_type(final template_sel template_type, final int list_length) {
		if (template_type != template_sel.VALUE_LIST && template_type != template_sel.COMPLEMENTED_LIST) {
			throw new TtcnError("Setting an invalid list for a template of type EXTERNAL.identification.syntaxes.");
		}
		clean_up();
		set_selection(template_type);
		list_value = new ArrayList<TitanExternal_identification_syntaxes_template>(list_length);
		for(int i = 0 ; i < list_length; i++) {
			list_value.add(new TitanExternal_identification_syntaxes_template());
		}
	}


	@Override
	public boolean is_bound() {
		if (template_selection == template_sel.UNINITIALIZED_TEMPLATE && !is_ifPresent) {
			return false;
		}
		if (template_selection != template_sel.SPECIFIC_VALUE) {
			return true;
		}
		return abstract_.is_bound()
				|| transfer.is_bound();
	}

	@Override
	public boolean is_present(final boolean legacy) {
		return is_present_(legacy);
	}

	private boolean is_present_(final boolean legacy) {
		if (template_selection==template_sel.UNINITIALIZED_TEMPLATE) {
			return false;
		}
		return !match_omit_(legacy);
	}

	@Override
	public boolean match_omit(final boolean legacy) {
		return match_omit_(legacy);
	}

	private boolean match_omit_(final boolean legacy) {
		if (is_ifPresent) {
			return true;
		}
		switch (template_selection) {
		case OMIT_VALUE:
		case ANY_OR_OMIT:
			return true;
		case VALUE_LIST:
		case COMPLEMENTED_LIST:
			if (legacy) {
				final int list_size = list_value.size();
				for (int l_idx = 0; l_idx < list_size; l_idx++) {
					if (list_value.get(l_idx).match_omit_(legacy)) {
						return template_selection==template_sel.VALUE_LIST;
					}
				}
				return template_selection==template_sel.COMPLEMENTED_LIST;
			} // else fall through
		default:
			return false;
		}
	}

	@Override
	public boolean is_value() {
		if (template_selection != template_sel.SPECIFIC_VALUE || is_ifPresent) {
			return false;
		}
		return abstract_.is_value()
				&& transfer.is_value();
	}
	/**
	 * Gives access to the field abstract.
	 * Turning the template into a specific value template if needed.
	 *
	 * @return the field abstract.
	 * */
	public TitanObjectid_template get_field_abstract_() {
		set_specific();
		return abstract_;
	}

	/**
	 * Gives read-only access to the field abstract.
	 * Being called on a non specific value template causes dynamic test case error.
	 *
	 * @return the field abstract.
	 * */
	public TitanObjectid_template constGet_field_abstract_() {
		if (template_selection != template_sel.SPECIFIC_VALUE) {
			throw new TtcnError("Accessing field abstract of a non-specific template of type EXTERNAL.identification.syntaxes.");
		}
		return abstract_;
	}

	/**
	 * Gives access to the field transfer.
	 * Turning the template into a specific value template if needed.
	 *
	 * @return the field transfer.
	 * */
	public TitanObjectid_template get_field_transfer() {
		set_specific();
		return transfer;
	}

	/**
	 * Gives read-only access to the field transfer.
	 * Being called on a non specific value template causes dynamic test case error.
	 *
	 * @return the field transfer.
	 * */
	public TitanObjectid_template constGet_field_transfer() {
		if (template_selection != template_sel.SPECIFIC_VALUE) {
			throw new TtcnError("Accessing field transfer of a non-specific template of type EXTERNAL.identification.syntaxes.");
		}
		return transfer;
	}

	private void set_specific() {
		if (template_selection != template_sel.SPECIFIC_VALUE) {
			final template_sel old_selection = template_selection;
			clean_up();
			set_selection(template_sel.SPECIFIC_VALUE);
			if (old_selection == template_sel.ANY_VALUE || old_selection == template_sel.ANY_OR_OMIT) {
				abstract_ = new TitanObjectid_template(template_sel.ANY_VALUE);
				transfer = new TitanObjectid_template(template_sel.ANY_VALUE);
			} else {
				abstract_ = new TitanObjectid_template();
				transfer = new TitanObjectid_template();
			}
		}
	}
	/**
	 * Matches the provided value against this template.
	 *
	 * @param other_value the value to be matched.
	 * */
	public boolean match(final TitanExternal_identification_syntaxes other_value) {
		return match(other_value, false);
	}

	/**
	 * Matches the provided value against this template. In legacy mode
	 * omitted value fields are not matched against the template field.
	 *
	 * @param other_value
	 *                the value to be matched.
	 * @param legacy
	 *                use legacy mode.
	 * */
	public boolean match(final TitanExternal_identification_syntaxes other_value, final boolean legacy) {
		if (!other_value.is_bound()) {
			return false;
		}
		switch (template_selection) {
		case ANY_VALUE:
		case ANY_OR_OMIT:
			return true;
		case OMIT_VALUE:
			return false;
		case SPECIFIC_VALUE:
			if(!other_value.get_field_abstract_().is_bound()) {
				return false;
			}
			if(!abstract_.match(other_value.get_field_abstract_(), legacy)) {
				return false;
			}
			if(!other_value.get_field_transfer().is_bound()) {
				return false;
			}
			if(!transfer.match(other_value.get_field_transfer(), legacy)) {
				return false;
			}
			return true;
		case VALUE_LIST:
		case COMPLEMENTED_LIST: {
			final int list_size = list_value.size();
			for (int list_count = 0; list_count < list_size; list_count++) {
				if (list_value.get(list_count).match(other_value, legacy)) {
					return template_selection == template_sel.VALUE_LIST;
				}
			}
			return template_selection == template_sel.COMPLEMENTED_LIST;
		}
		default:
			throw new TtcnError("Matching an uninitialized/unsupported template of type EXTERNAL.identification.syntaxes.");
		}
	}


	@Override
	public boolean match(final Base_Type otherValue, final boolean legacy) {
		if (otherValue instanceof TitanExternal_identification_syntaxes) {
			return match((TitanExternal_identification_syntaxes)otherValue, legacy);
		}

		throw new TtcnError("Internal Error: The left operand of assignment is not of type TitanExternal_identification_syntaxes.");
	}


	@Override
	public TitanExternal_identification_syntaxes valueof() {
		if (template_selection != template_sel.SPECIFIC_VALUE || is_ifPresent) {
			throw new TtcnError("Performing a valueof or send operation on a non-specific template of type EXTERNAL.identification.syntaxes.");
		}
		final TitanExternal_identification_syntaxes ret_val = new TitanExternal_identification_syntaxes();
		if (abstract_.is_bound()) {
			ret_val.get_field_abstract_().operator_assign(abstract_.valueof());
		}
		if (transfer.is_bound()) {
			ret_val.get_field_transfer().operator_assign(transfer.valueof());
		}
		return ret_val;
	}

	/**
	 * Returns the size (number of fields).
	 *
	 * size_of in the core
	 *
	 * @return the size of the structure.
	 * */
	public TitanInteger size_of() {
		if (is_ifPresent) {
			throw new TtcnError("Performing sizeof() operation on a template of type EXTERNAL.identification.syntaxes which has an ifpresent attribute.");
		}
		switch (template_selection) {
		case SPECIFIC_VALUE:
			return new TitanInteger(2);
		case VALUE_LIST: {
			if (list_value.isEmpty()) {
				throw new TtcnError("Internal error: Performing sizeof() operation on a template of type EXTERNAL.identification.syntaxes containing an empty list.");
			}
			final int item_size = list_value.get(0).size_of().get_int();
			final int list_size = list_value.size();
			for (int l_idx = 1; l_idx < list_size; l_idx++) {
				if (list_value.get(l_idx).size_of().get_int() != item_size) {
					throw new TtcnError("Performing sizeof() operation on a template of type EXTERNAL.identification.syntaxes containing a value list with different sizes.");
				}
			}
			return new TitanInteger(item_size);
		}
		case OMIT_VALUE:
			throw new TtcnError("Performing sizeof() operation on a template of type EXTERNAL.identification.syntaxes containing omit value.");
		case ANY_VALUE:
		case ANY_OR_OMIT:
			throw new TtcnError("Performing sizeof() operation on a template of type EXTERNAL.identification.syntaxes containing */? value.");
		case COMPLEMENTED_LIST:
			throw new TtcnError("Performing sizeof() operation on a template of type EXTERNAL.identification.syntaxes containing complemented list.");
		default:
			throw new TtcnError("Performing sizeof() operation on an uninitialized/unsupported template of type EXTERNAL.identification.syntaxes.");
		}
	}
	@Override
	public int n_list_elem() {
		if (template_selection != template_sel.VALUE_LIST && template_selection != template_sel.COMPLEMENTED_LIST) {
			throw new TtcnError("Internal error: Accessing a list element of a non-list template of enumeration type EXTERNAL.identification.syntaxes.");
		}
		return list_value.size();
	}

	@Override
	public TitanExternal_identification_syntaxes_template list_item(final int list_index) {
		if (template_selection != template_sel.VALUE_LIST && template_selection != template_sel.COMPLEMENTED_LIST) {
			throw new TtcnError("Accessing a list element of a non-list template of type EXTERNAL.identification.syntaxes.");
		}
		if (list_index < 0) {
			throw new TtcnError(MessageFormat.format("Internal error: Accessing a value list template of type EXTERNAL.identification.syntaxes using a negative index ({0}).", list_index));
		} else if (list_index >= list_value.size()) {
			throw new TtcnError("Index overflow in a value list template of type EXTERNAL.identification.syntaxes.");
		}
		return list_value.get(list_index);
	}

	@Override
	public void log() {
		switch (template_selection) {
		case SPECIFIC_VALUE:
			TTCN_Logger.log_char('{');
			TTCN_Logger.log_event_str(" abstract := ");
			abstract_.log();
			TTCN_Logger.log_char(',');
			TTCN_Logger.log_event_str(" transfer := ");
			transfer.log();
			TTCN_Logger.log_event_str(" }");
			break;
		case COMPLEMENTED_LIST:
			TTCN_Logger.log_event_str("complement");
		case VALUE_LIST: {
			TTCN_Logger.log_char('(');
			final int list_size = list_value.size();
			for (int list_count = 0; list_count < list_size; list_count++) {
				if (list_count > 0) {
					TTCN_Logger.log_event_str(", ");
				}
				list_value.get(list_count).log();
			}
			TTCN_Logger.log_char(')');
			break;
		}
		default:
			log_generic();
			break;
		}
		log_ifpresent();
	}

	/**
	 * Logs the matching of the provided value to this template, to help
	 * identify the reason for mismatch.
	 *
	 * @param match_value
	 *                the value to be matched.
	 * */
	public void log_match(final TitanExternal_identification_syntaxes match_value) {
		log_match(match_value, false);
	}

	@Override
	public void log_match(final Base_Type match_value, final boolean legacy) {
		if (match_value instanceof TitanExternal_identification_syntaxes) {
			log_match((TitanExternal_identification_syntaxes)match_value, legacy);
			return;
		}

		throw new TtcnError("Internal Error: value can not be cast to EXTERNAL.identification.syntaxes.");
	}

	/**
	 * Logs the matching of the provided value to this template, to help
	 * identify the reason for mismatch. In legacy mode omitted value fields
	 * are not matched against the template field.
	 *
	 * @param match_value
	 *                the value to be matched.
	 * @param legacy
	 *                use legacy mode.
	 * */
	public void log_match(final TitanExternal_identification_syntaxes match_value, final boolean legacy) {
		if ( TTCN_Logger.matching_verbosity_t.VERBOSITY_COMPACT == TTCN_Logger.get_matching_verbosity() ) {
			if(match(match_value, legacy)) {
				TTCN_Logger.print_logmatch_buffer();
				TTCN_Logger.log_event_str(" matched");
			} else {
				if (template_selection == template_sel.SPECIFIC_VALUE) {
					final int previous_size = TTCN_Logger.get_logmatch_buffer_len();
					if( !abstract_.match(match_value.constGet_field_abstract_(), legacy) ) {
						TTCN_Logger.log_logmatch_info(".abstract");
						abstract_.log_match(match_value.constGet_field_abstract_(), legacy);
						TTCN_Logger.set_logmatch_buffer_len(previous_size);
					}
					if( !transfer.match(match_value.constGet_field_transfer(), legacy) ) {
						TTCN_Logger.log_logmatch_info(".transfer");
						transfer.log_match(match_value.constGet_field_transfer(), legacy);
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
		if (template_selection == template_sel.SPECIFIC_VALUE) {
			TTCN_Logger.log_event_str("{ abstract := ");
			abstract_.log_match(match_value.constGet_field_abstract_(), legacy);
			TTCN_Logger.log_event_str(", transfer := ");
			transfer.log_match(match_value.constGet_field_transfer(), legacy);
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
		switch (template_selection) {
		case OMIT_VALUE:
		case ANY_VALUE:
		case ANY_OR_OMIT:
			break;
		case SPECIFIC_VALUE:
			abstract_.encode_text(text_buf);
			transfer.encode_text(text_buf);
			break;
		case VALUE_LIST:
		case COMPLEMENTED_LIST: {
			final int list_size = list_value.size();
			text_buf.push_int(list_size);
			for (int i = 0; i < list_size; i++) {
				list_value.get(i).encode_text(text_buf);
			}
			break;
		}
		default:
			throw new TtcnError("Text encoder: Encoding an uninitialized/unsupported template of type EXTERNAL.identification.syntaxes.");
		}
	}

	@Override
	public void decode_text(final Text_Buf text_buf) {
		clean_up();
		decode_text_base(text_buf);
		switch (template_selection) {
		case OMIT_VALUE:
		case ANY_VALUE:
		case ANY_OR_OMIT:
			break;
		case SPECIFIC_VALUE:
			abstract_ = new TitanObjectid_template();
			abstract_.decode_text(text_buf);
			transfer = new TitanObjectid_template();
			transfer.decode_text(text_buf);
			break;
		case VALUE_LIST:
		case COMPLEMENTED_LIST: {
			final int size = text_buf.pull_int().get_int();
			list_value = new ArrayList<TitanExternal_identification_syntaxes_template>(size);
			for (int i = 0; i < size; i++) {
				final TitanExternal_identification_syntaxes_template temp = new TitanExternal_identification_syntaxes_template();
				temp.decode_text(text_buf);
				list_value.add(temp);
			}
			break;
		}
		default:
			throw new TtcnError("Text decoder: An unknown/unsupported selection was received in a template of type EXTERNAL.identification.syntaxes.");
		}
	}

	@Override
	public void set_param(final Module_Parameter param) {
		param.basic_check(Module_Parameter.basic_check_bits_t.BC_TEMPLATE.getValue(), "record template");
		switch (param.get_type()) {
		case MP_Omit:
			operator_assign(template_sel.OMIT_VALUE);
			break;
		case MP_Any:
			operator_assign(template_sel.ANY_VALUE);
			break;
		case MP_AnyOrNone:
			operator_assign(template_sel.ANY_OR_OMIT);
			break;
		case MP_List_Template:
		case MP_ComplementList_Template: {
			final int size = param.get_size();
			set_type(param.get_type() == Module_Parameter.type_t.MP_List_Template ? template_sel.VALUE_LIST : template_sel.COMPLEMENTED_LIST, size);
			for (int i = 0; i < size; i++) {
				list_item(i).set_param(param.get_elem(i));
			}
			break;
		}
		case MP_Value_List:
			if (param.get_size() > 2) {
				param.error(MessageFormat.format("record template of type EXTERNAL.identification.syntaxes has 2 fields but list value has {0} fields.", param.get_size()));
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
					curr_param.error(MessageFormat.format("Non existent field name in type EXTERNAL.identification.syntaxes: {0}", curr_param.get_id().get_name()));
					break;
				}
			}
			break;
		}
		default:
			param.type_error("record template", "EXTERNAL.identification.syntaxes");
			break;
		}
		is_ifPresent = param.get_ifpresent();
	}

	@Override
	public void check_restriction(final template_res restriction, final String name, final boolean legacy) {
		if (template_selection == template_sel.UNINITIALIZED_TEMPLATE) {
			return;
		}
		switch ((name != null && restriction == template_res.TR_VALUE) ? template_res.TR_OMIT : restriction) {
		case TR_OMIT:
			if (template_selection == template_sel.OMIT_VALUE) {
				return;
			}
		case TR_VALUE:
			if (template_selection != template_sel.SPECIFIC_VALUE || is_ifPresent) {
				break;
			}
			this.abstract_.check_restriction(restriction, name == null ? "EXTERNAL.identification.syntaxes" : name, legacy);
			this.transfer.check_restriction(restriction, name == null ? "EXTERNAL.identification.syntaxes" : name, legacy);
			return;
		case TR_PRESENT:
			if (!match_omit(legacy)) {
				return;
			}
			break;
		default:
			return;
		}
		throw new TtcnError(MessageFormat.format("Restriction `{0}'' on template of type {1} violated.", get_res_name(restriction), name == null ? "EXTERNAL.identification.syntaxes" : name));
	}
}