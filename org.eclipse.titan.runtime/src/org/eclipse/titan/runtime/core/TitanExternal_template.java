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
 * Part of the representation of the ASN.1 EXTERNAL type
 *
 * @author Kristof Szabados
 */
public class TitanExternal_template extends Base_Template {
	private TitanExternal_identification_template identification; //ASN1_Choice_Type
	private TitanUniversalCharString_template data__value__descriptor; //ObjectDescriptor_Type
	private TitanOctetString_template data__value; //OctetString_Type
	//originally value_list/list_value
	private List<TitanExternal_template> list_value;


	/**
	 * Initializes to unbound/uninitialized template.
	 * */
	public TitanExternal_template() {
		// do nothing
	}

	/**
	 * Initializes to a given template kind.
	 *
	 * @param otherValue
	 *                the template kind to initialize to.
	 * */
	public TitanExternal_template(final template_sel otherValue ) {
		super( otherValue );
		checkSingleSelection( otherValue );
	}

	/**
	 * Initializes to a given value.
	 * The template becomes a specific template and the elements of the provided value are copied.
	 *
	 * @param otherValue
	 *                the value to initialize to.
	 * */
	public TitanExternal_template( final TitanExternal otherValue ) {
		copyValue(otherValue);
	}

	/**
	 * Initializes to a given template.
	 * The elements of the provided template are copied.
	 *
	 * @param otherValue
	 *                the value to initialize to.
	 * */
	public TitanExternal_template( final TitanExternal_template otherValue ) {
		copyTemplate( otherValue );
	}

	/**
	 * Initializes to a given value.
	 * The template becomes a specific template with the provided value.
	 * Causes a dynamic testcase error if the value is neither present nor optional.
	 *
	 * @param otherValue
	 *                the value to initialize to.
	 * */
	public TitanExternal_template( final Optional<TitanExternal> otherValue ) {
		switch (otherValue.get_selection()) {
		case OPTIONAL_PRESENT:
			copyValue(otherValue.constGet());
			break;
		case OPTIONAL_OMIT:
			set_selection(template_sel.OMIT_VALUE);
			break;
		default:
			throw new TtcnError("Creating a template of type EXTERNAL from an unbound optional field.");
		}
	}

	@Override
	public TitanExternal_template assign( final template_sel otherValue ) {
		checkSingleSelection(otherValue);
		cleanUp();
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
	public TitanExternal_template assign( final TitanExternal otherValue ) {
		cleanUp();
		copyValue(otherValue);
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
	public TitanExternal_template assign( final TitanExternal_template otherValue ) {
		if (otherValue != this) {
			cleanUp();
			copyTemplate(otherValue);
		}
		return this;
	}

	@Override
	public TitanExternal_template assign(final Base_Type otherValue) {
		if (otherValue instanceof TitanExternal) {
			return assign((TitanExternal) otherValue);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `TitanExternal' can not be cast to {1}", otherValue));
	}

	@Override
	public TitanExternal_template assign(final Base_Template otherValue) {
		if (otherValue instanceof TitanExternal_template) {
			return assign((TitanExternal_template) otherValue);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `TitanExternal' can not be cast to {1}_template", otherValue));
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
	public TitanExternal_template assign( final Optional<TitanExternal> otherValue ) {
		cleanUp();
		switch (otherValue.get_selection()) {
		case OPTIONAL_PRESENT:
			copyValue(otherValue.constGet());
			break;
		case OPTIONAL_OMIT:
			set_selection(template_sel.OMIT_VALUE);
			break;
		default:
			throw new TtcnError("Assignment of an unbound optional field to a template of type EXTERNAL.");
		}
		return this;
	}

	private void copyValue(final TitanExternal other_value) {
		if (other_value.getidentification().isBound()) {
			getidentification().assign(other_value.getidentification());
		} else {
			getidentification().cleanUp();
		}
		if (other_value.getdata__value__descriptor().isBound()) {
			if (other_value.getdata__value__descriptor().ispresent()) {
				getdata__value__descriptor().assign(other_value.getdata__value__descriptor().get());
			} else {
				getdata__value__descriptor().assign(template_sel.OMIT_VALUE);
			}
		} else {
			getdata__value__descriptor().cleanUp();
		}
		if (other_value.getdata__value().isBound()) {
			getdata__value().assign(other_value.getdata__value());
		} else {
			getdata__value().cleanUp();
		}
		set_selection(template_sel.SPECIFIC_VALUE);
	}

	private void copyTemplate(final TitanExternal_template other_value) {
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
			if (template_sel.UNINITIALIZED_TEMPLATE == other_value.getdata__value().get_selection()) {
				getdata__value().cleanUp();
			} else {
				getdata__value().assign(other_value.getdata__value());
			}
			break;
		case OMIT_VALUE:
		case ANY_VALUE:
		case ANY_OR_OMIT:
			break;
		case VALUE_LIST:
		case COMPLEMENTED_LIST:
			list_value = new ArrayList<TitanExternal_template>(other_value.list_value.size());
			for(int i = 0; i < other_value.list_value.size(); i++) {
				final TitanExternal_template temp = new TitanExternal_template(other_value.list_value.get(i));
				list_value.add(temp);
			}
			break;
		default:
			throw new TtcnError("Copying an uninitialized template of type EXTERNAL.");
		}
		set_selection(other_value);
	}

	@Override
	public void setType(final template_sel template_type, final int list_length) {
		if (template_type != template_sel.VALUE_LIST && template_type != template_sel.COMPLEMENTED_LIST) {
			throw new TtcnError("Setting an invalid list for a template of type EXTERNAL.");
		}
		cleanUp();
		set_selection(template_type);
		list_value = new ArrayList<TitanExternal_template>(list_length);
		for(int i = 0 ; i < list_length; i++) {
			list_value.add(new TitanExternal_template());
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
		if (data__value.isBound()) {
			return true;
		}
		return false;
	}

	@Override
	public boolean is_present(final boolean legacy) {
		return isPresent_(legacy);
	}

	private boolean isPresent_(final boolean legacy) {
		if (templateSelection==template_sel.UNINITIALIZED_TEMPLATE) {
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
		if (!data__value.isValue()) {
			return false;
		}
		return true;
	}
	/**
	 * Gives access to the field identification.
	 * Turning the template into a specific value template if needed.
	 *
	 * @return the field identification.
	 * */
	public TitanExternal_identification_template getidentification() {
		setSpecific();
		return identification;
	}

	/**
	 * Gives read-only access to the field identification.
	 * Being called on a non specific value template causes dynamic test case error.
	 *
	 * @return the field identification.
	 * */
	public TitanExternal_identification_template constGetidentification() {
		if (templateSelection != template_sel.SPECIFIC_VALUE) {
			throw new TtcnError("Accessing field identification of a non-specific template of type EXTERNAL.");
		}
		return identification;
	}

	/**
	 * Gives access to the field data-value-descriptor.
	 * Turning the template into a specific value template if needed.
	 *
	 * @return the field data-value-descriptor.
	 * */
	public TitanUniversalCharString_template getdata__value__descriptor() {
		setSpecific();
		return data__value__descriptor;
	}

	/**
	 * Gives read-only access to the field data-value-descriptor.
	 * Being called on a non specific value template causes dynamic test case error.
	 *
	 * @return the field data-value-descriptor.
	 * */
	public TitanUniversalCharString_template constGetdata__value__descriptor() {
		if (templateSelection != template_sel.SPECIFIC_VALUE) {
			throw new TtcnError("Accessing field data-value-descriptor of a non-specific template of type EXTERNAL.");
		}
		return data__value__descriptor;
	}

	/**
	 * Gives access to the field data-value.
	 * Turning the template into a specific value template if needed.
	 *
	 * @return the field data-value.
	 * */
	public TitanOctetString_template getdata__value() {
		setSpecific();
		return data__value;
	}

	/**
	 * Gives read-only access to the field data-value.
	 * Being called on a non specific value template causes dynamic test case error.
	 *
	 * @return the field data-value.
	 * */
	public TitanOctetString_template constGetdata__value() {
		if (templateSelection != template_sel.SPECIFIC_VALUE) {
			throw new TtcnError("Accessing field data-value of a non-specific template of type EXTERNAL.");
		}
		return data__value;
	}

	private void setSpecific() {
		if (templateSelection != template_sel.SPECIFIC_VALUE) {
			final template_sel old_selection = templateSelection;
			cleanUp();
			set_selection(template_sel.SPECIFIC_VALUE);
			identification = new TitanExternal_identification_template();
			data__value__descriptor = new TitanUniversalCharString_template();
			data__value = new TitanOctetString_template();
			if (old_selection == template_sel.ANY_VALUE || old_selection == template_sel.ANY_OR_OMIT) {
				identification.assign(template_sel.ANY_VALUE);
				data__value__descriptor.assign(template_sel.ANY_OR_OMIT);
				data__value.assign(template_sel.ANY_VALUE);
			}
		}
	}
	/**
	 * Matches the provided value against this template.
	 *
	 * @param other_value the value to be matched.
	 * */
	public boolean match(final TitanExternal other_value) {
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
	public boolean match(final TitanExternal other_value, final boolean legacy) {
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
			if((other_value.getdata__value__descriptor().ispresent() ? !data__value__descriptor.match(other_value.getdata__value__descriptor().get(), legacy) : !data__value__descriptor.match_omit(legacy))) {
				return false;
			}
			if(!other_value.getdata__value().isBound()) {
				return false;
			}
			if(!data__value.match(other_value.getdata__value(), legacy)) {
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
			throw new TtcnError("Matching an uninitialized/unsupported template of type EXTERNAL.");
		}
	}


	@Override
	public boolean match(final Base_Type otherValue, final boolean legacy) {
		if (otherValue instanceof TitanExternal) {
			return match((TitanExternal)otherValue, legacy);
		}

		throw new TtcnError("Internal Error: The left operand of assignment is not of type TitanExternal.");
	}


	@Override
	public TitanExternal valueOf() {
		if (templateSelection != template_sel.SPECIFIC_VALUE || is_ifPresent) {
			throw new TtcnError("Performing a valueof or send operation on a non-specific template of type EXTERNAL.");
		}
		final TitanExternal ret_val = new TitanExternal();
		if (identification.isBound()) {
			ret_val.getidentification().assign(identification.valueOf());
		}
		if (data__value__descriptor.isOmit()) {
			ret_val.getdata__value__descriptor().assign(template_sel.OMIT_VALUE);
		} else if (data__value__descriptor.isBound()) {
			ret_val.getdata__value__descriptor().assign(data__value__descriptor.valueOf());
		}
		if (data__value.isBound()) {
			ret_val.getdata__value().assign(data__value.valueOf());
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
	public TitanInteger sizeOf() {
		if (is_ifPresent) {
			throw new TtcnError("Performing sizeof() operation on a template of type EXTERNAL which has an ifpresent attribute.");
		}
		switch (templateSelection) {
		case SPECIFIC_VALUE:
			int sizeof = 2;
			if (data__value__descriptor.is_present()) {
				sizeof++;
			}
			return new TitanInteger(sizeof);
		case VALUE_LIST:
			if (list_value.isEmpty()) {
				throw new TtcnError("Internal error: Performing sizeof() operation on a template of type EXTERNAL containing an empty list.");
			}
			final int item_size = list_value.get(0).sizeOf().getInt();
			for (int l_idx = 1; l_idx < list_value.size(); l_idx++) {
				if (list_value.get(l_idx).sizeOf().getInt() != item_size) {
					throw new TtcnError("Performing sizeof() operation on a template of type EXTERNAL containing a value list with different sizes.");
				}
			}
			return new TitanInteger(item_size);
		case OMIT_VALUE:
			throw new TtcnError("Performing sizeof() operation on a template of type EXTERNAL containing omit value.");
		case ANY_VALUE:
		case ANY_OR_OMIT:
			throw new TtcnError("Performing sizeof() operation on a template of type EXTERNAL containing */? value.");
		case COMPLEMENTED_LIST:
			throw new TtcnError("Performing sizeof() operation on a template of type EXTERNAL containing complemented list.");
		default:
			throw new TtcnError("Performing sizeof() operation on an uninitialized/unsupported template of type EXTERNAL.");
		}
	}

	@Override
	public TitanExternal_template listItem(final int list_index) {
		if (templateSelection != template_sel.VALUE_LIST && templateSelection != template_sel.COMPLEMENTED_LIST) {
			throw new TtcnError("Accessing a list element of a non-list template of type EXTERNAL.");
		}
		if (list_index >= list_value.size()) {
			throw new TtcnError("Index overflow in a value list template of type EXTERNAL.");
		}
		return list_value.get(list_index);
	}

	@Override
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
			TTCN_Logger.log_event_str(" data-value := ");
			data__value.log();
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

	/**
	 * Logs the matching of the provided value to this template, to help
	 * identify the reason for mismatch.
	 *
	 * @param match_value
	 *                the value to be matched.
	 * */
	public void log_match(final TitanExternal match_value) {
		log_match(match_value, false);
	}

	@Override
	public void log_match(final Base_Type match_value, final boolean legacy) {
		if (match_value instanceof TitanExternal) {
			log_match((TitanExternal)match_value, legacy);
			return;
		}

		throw new TtcnError("Internal Error: value can not be cast to EXTERNAL.");
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
	public void log_match(final TitanExternal match_value, final boolean legacy) {
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
					if (match_value.constGetdata__value__descriptor().ispresent()) {
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
					if( !data__value.match(match_value.constGetdata__value(), legacy) ) {
						TTCN_Logger.log_logmatch_info(".data-value");
						data__value.log_match(match_value.constGetdata__value(), legacy);
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
			TTCN_Logger.log_event_str("{ data-value := ");
			data__value.log_match(match_value.constGetdata__value(), legacy);
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
			data__value.encode_text(text_buf);
			break;
		case VALUE_LIST:
		case COMPLEMENTED_LIST:
			text_buf.push_int(list_value.size());
			for (int i = 0; i < list_value.size(); i++) {
				list_value.get(i).encode_text(text_buf);
			}
			break;
		default:
			throw new TtcnError("Text encoder: Encoding an uninitialized/unsupported template of type EXTERNAL.");
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
			identification = new TitanExternal_identification_template();
			identification.decode_text(text_buf);
			data__value__descriptor = new TitanUniversalCharString_template();
			data__value__descriptor.decode_text(text_buf);
			data__value = new TitanOctetString_template();
			data__value.decode_text(text_buf);
			break;
		case VALUE_LIST:
		case COMPLEMENTED_LIST: {
			final int size = text_buf.pull_int().getInt();
			list_value = new ArrayList<TitanExternal_template>(size);
			for (int i = 0; i < size; i++) {
				final TitanExternal_template temp = new TitanExternal_template();
				temp.decode_text(text_buf);
				list_value.add(temp);
			}
			break;
		}
		default:
			throw new TtcnError("Text decoder: An unknown/unsupported selection was received in a template of type EXTERNAL.");
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
				param.error(MessageFormat.format("record template of type EXTERNAL has 3 fields but list value has {0} fields.", param.get_size()));
			}
			if (param.get_size() > 0 && param.get_elem(0).get_type() != Module_Parameter.type_t.MP_NotUsed) {
				getidentification().set_param(param.get_elem(0));
			}
			if (param.get_size() > 1 && param.get_elem(1).get_type() != Module_Parameter.type_t.MP_NotUsed) {
				getdata__value__descriptor().set_param(param.get_elem(1));
			}
			if (param.get_size() > 2 && param.get_elem(2).get_type() != Module_Parameter.type_t.MP_NotUsed) {
				getdata__value().set_param(param.get_elem(2));
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
				if ("data-value".equals(curr_param.get_id().get_name())) {
					if (curr_param.get_type() != Module_Parameter.type_t.MP_NotUsed) {
						getdata__value().set_param(curr_param);
					}
					value_used[val_idx] = true;
				}
			}
			for (int val_idx = 0; val_idx < param.get_size(); val_idx++) {
				if (!value_used[val_idx]) {
					final Module_Parameter curr_param = param.get_elem(val_idx);
					curr_param.error(MessageFormat.format("Non existent field name in type EXTERNAL: {0}", curr_param.get_id().get_name()));
					break;
				}
			}
			break;
		}
		default:
			param.type_error("record template", "EXTERNAL");
		}
		is_ifPresent = param.get_ifpresent();
	}

	@Override
	public void check_restriction(final template_res restriction, final String name, final boolean legacy) {
		if (templateSelection == template_sel.UNINITIALIZED_TEMPLATE) {
			return;
		}
		switch ((name != null && restriction == template_res.TR_VALUE) ? template_res.TR_OMIT : restriction) {
		case TR_OMIT:
			if (templateSelection == template_sel.OMIT_VALUE) {
				return;
			}
		case TR_VALUE:
			if (templateSelection != template_sel.SPECIFIC_VALUE || is_ifPresent) {
				break;
			}
			this.identification.check_restriction(restriction, name == null ? "EXTERNAL" : name, legacy);
			this.data__value__descriptor.check_restriction(restriction, name == null ? "EXTERNAL" : name, legacy);
			this.data__value.check_restriction(restriction, name == null ? "EXTERNAL" : name, legacy);
			return;
		case TR_PRESENT:
			if (!match_omit(legacy)) {
				return;
			}
			break;
		default:
			return;
		}
		throw new TtcnError(MessageFormat.format("Restriction `{0}'' on template of type {1} violated.", getResName(restriction), name == null ? "EXTERNAL" : name));
	}
}