/******************************************************************************
 * Copyright (c) 2000-2019 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.titan.runtime.core.Param_Types.Module_Param_Any;
import org.eclipse.titan.runtime.core.Param_Types.Module_Param_AnyOrNone;
import org.eclipse.titan.runtime.core.Param_Types.Module_Param_ComplementList_Template;
import org.eclipse.titan.runtime.core.Param_Types.Module_Param_List_Template;
import org.eclipse.titan.runtime.core.Param_Types.Module_Param_Name;
import org.eclipse.titan.runtime.core.Param_Types.Module_Param_Omit;
import org.eclipse.titan.runtime.core.Param_Types.Module_Param_Unbound;
import org.eclipse.titan.runtime.core.Param_Types.Module_Parameter;
import org.eclipse.titan.runtime.core.Param_Types.Module_Parameter.basic_check_bits_t;
import org.eclipse.titan.runtime.core.Param_Types.Module_Parameter.type_t;

/**
 * objid template
 *
 * @author Gergo Ujhelyi
 * @author Andrea Palfi
 *
 */
public class TitanObjectid_template extends Base_Template {

	private TitanObjectid single_value;

	private List<TitanObjectid_template> value_list;

	/**
	 * Initializes to unbound/uninitialized template.
	 * */
	public TitanObjectid_template() {
		super();
	}

	/**
	 * Initializes to a given template kind.
	 *
	 * @param otherValue
	 *                the template kind to initialize to.
	 * */
	public TitanObjectid_template(final template_sel otherValue) {
		super(otherValue);

		check_single_selection(otherValue);
	}

	/**
	 * Initializes to a given value.
	 * The template becomes a specific template and the value is copied.
	 *
	 * @param otherValue
	 *                the value to initialize to.
	 * */
	public TitanObjectid_template(final TitanObjectid otherValue) {
		super(template_sel.SPECIFIC_VALUE);

		single_value = new TitanObjectid(otherValue);
	}

	/**
	 * Initializes to a given value.
	 * The template becomes a specific template and the value is copied.
	 * Causes dynamic testcase error if the parameter is not present or omit.
	 *
	 * @param otherValue
	 *                the value to initialize to.
	 * */
	public TitanObjectid_template(final Optional<TitanObjectid> otherValue) {
		switch (otherValue.get_selection()) {
		case OPTIONAL_PRESENT:
			set_selection(template_sel.SPECIFIC_VALUE);
			single_value = new TitanObjectid(otherValue.constGet());
			break;
		case OPTIONAL_OMIT:
			set_selection(template_sel.OMIT_VALUE);
			break;
		case OPTIONAL_UNBOUND:
			throw new TtcnError("Creating a objid template from an unbound optional field.");
		}
	}

	/**
	 * Initializes to a given template.
	 *
	 * @param otherValue
	 *                the template to initialize to.
	 * */
	public TitanObjectid_template(final TitanObjectid_template otherValue) {

		copy_template(otherValue);
	}

	@Override
	public void clean_up() {
		if (template_selection == template_sel.VALUE_LIST || template_selection == template_sel.COMPLEMENTED_LIST) {
			value_list = null;
		} else {
			template_selection = template_sel.UNINITIALIZED_TEMPLATE;
		}
	}

	private void copy_template(final TitanObjectid_template otherValue) {
		switch (otherValue.template_selection) {
		case SPECIFIC_VALUE:
			single_value = otherValue.single_value;
			break;
		case OMIT_VALUE:
		case ANY_VALUE:
		case ANY_OR_OMIT:
			break;
		case VALUE_LIST:
		case COMPLEMENTED_LIST:
			value_list = new ArrayList<TitanObjectid_template>(otherValue.value_list.size());
			for (int i = 0; i < otherValue.value_list.size(); i++) {
				final TitanObjectid_template temp = new TitanObjectid_template(otherValue.value_list.get(i));
				value_list.add(temp);
			}
			break;
		default:
			throw new TtcnError("Copying an uninitialized/unsupported objid template.");
		}

		set_selection(otherValue);
	}

	@Override
	public TitanObjectid_template operator_assign(final template_sel otherValue) {
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
	public TitanObjectid_template operator_assign(final TitanObjectid otherValue) {
		otherValue.must_bound("Assignment of an unbound objid value to a template.");

		clean_up();
		set_selection(template_sel.SPECIFIC_VALUE);
		single_value = new TitanObjectid(otherValue);

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
	public TitanObjectid_template operator_assign(final TitanObjectid_template otherValue) {
		if (otherValue != this) {
			clean_up();
			copy_template(otherValue);
		}

		return this;
	}

	@Override
	public Base_Template operator_assign(final Base_Type otherValue) {
		if (otherValue instanceof TitanObjectid) {
			return operator_assign((TitanObjectid)otherValue);
		}
		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to objid", otherValue));
	}

	@Override
	public Base_Template operator_assign(final Base_Template otherValue) {
		if (otherValue instanceof TitanObjectid_template) {
			return operator_assign((TitanObjectid_template)otherValue);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to objid", otherValue));
	}

	@Override
	public void log_match(final Base_Type match_value, final boolean legacy) {
		if (match_value instanceof TitanObjectid) {
			log_match((TitanObjectid) match_value, legacy);
			return;
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to objid", match_value));
	}

	/**
	 * Matches the provided value against this template. In legacy mode
	 * omitted value fields are not matched against the template field.
	 *
	 * @param otherValue
	 *                the value to be matched.
	 * @param legacy
	 *                use legacy mode.
	 * */
	public boolean match(final TitanObjectid otherValue, final boolean legacy) {
		if (!otherValue.is_bound()) {
			return false;
		}

		switch (template_selection) {
		case SPECIFIC_VALUE:
			return single_value.operator_equals(otherValue);
		case OMIT_VALUE:
			return false;
		case ANY_VALUE:
		case ANY_OR_OMIT:
			return true;
		case VALUE_LIST:
		case COMPLEMENTED_LIST:
			for (int i = 0; i < value_list.size(); i++) {
				if (value_list.get(i).match(otherValue, legacy)) {
					return template_selection == template_sel.VALUE_LIST;
				}
			}
			return template_selection == template_sel.COMPLEMENTED_LIST;
		default:
			throw new TtcnError("Matching with an uninitialized/unsupported objid template.");
		}
	}

	/**
	 * Matches the provided value against this template.
	 *
	 * @param otherValue the value to be matched.
	 * */
	public boolean match(final TitanObjectid otherValue) {
		return match(otherValue, false);
	}

	@Override
	public boolean match(final Base_Type otherValue, final boolean legacy) {
		if (otherValue instanceof TitanObjectid) {
			return match((TitanObjectid) otherValue, legacy);
		}
		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to objid", otherValue));
	}

	@Override
	public TitanObjectid valueof() {
		if (template_selection != template_sel.SPECIFIC_VALUE || is_ifPresent) {
			throw new TtcnError("Performing a valueof or send operation on a non-specific objid template.");
		}

		return single_value;
	}

	/**
	 * Returns the number of elements, that is, the largest used index plus
	 * one and zero for the empty value.
	 *
	 * size_of in the core
	 *
	 * @return the number of elements.
	 * */
	public TitanInteger size_of() {
		switch (template_selection) {
		case SPECIFIC_VALUE:
			return single_value.size_of();
		case OMIT_VALUE:
			throw new TtcnError("Performing sizeof() operation on an objid template containing omit value.");
		case ANY_VALUE:
		case ANY_OR_OMIT:
			throw new TtcnError("Performing sizeof() operation on a */? objid template.");
		case VALUE_LIST: {
			if (value_list.isEmpty()) {
				throw new TtcnError("Internal error: Performing sizeof() operation on an objid template containing an empty list.");
			}

			final TitanInteger item_size = value_list.get(0).size_of();
			for (int i = 1; i < value_list.size(); i++) {
				if (!value_list.get(i).size_of().operator_equals(item_size)) {
					throw new TtcnError(
							"Performing sizeof() operation on an objid template containing a value list with different sizes.");
				}
			}
			return item_size;
		}
		case COMPLEMENTED_LIST:
			throw new TtcnError("Performing sizeof() operation on an objid template containing complemented list.");
		default:
			throw new TtcnError("Performing sizeof() operation on an uninitialized/unsupported objid template.");
		}
	}

	@Override
	public void set_type(final template_sel template_type, final int list_length) {
		if (template_type != template_sel.VALUE_LIST && template_type != template_sel.COMPLEMENTED_LIST) {
			throw new TtcnError("Setting an invalid list type for an objid template.");
		}
		clean_up();
		set_selection(template_type);
		value_list = new ArrayList<TitanObjectid_template>(list_length);
		for (int i = 0; i < list_length; i++) {
			value_list.add(new TitanObjectid_template());
		}
	}

	@Override
	public int n_list_elem() {
		if (template_selection != template_sel.VALUE_LIST && template_selection != template_sel.COMPLEMENTED_LIST) {
			throw new TtcnError("Accessing a list element of a non-list objid template.");
		}

		return value_list.size();
	}

	@Override
	public TitanObjectid_template list_item(final int list_index) {
		if (template_selection != template_sel.VALUE_LIST && template_selection != template_sel.COMPLEMENTED_LIST) {
			throw new TtcnError("Accessing a list element of a non-list objid template.");
		}
		if (list_index >= value_list.size()) {
			throw new TtcnError("Index overflow in an objid value list template.");
		}
		return value_list.get(list_index);
	}

	@Override
	public void log() {
		switch (template_selection) {
		case SPECIFIC_VALUE:
			single_value.log();
			break;
		case COMPLEMENTED_LIST:
			TTCN_Logger.log_event_str("complement");
			// no break;
		case VALUE_LIST:
			TTCN_Logger.log_char('(');
			for (int i = 0; i < value_list.size(); i++) {
				if (i > 0) {
					TTCN_Logger.log_event_str(", ");
				}
				value_list.get(i).log();
			}
			TTCN_Logger.log_char(')');
			break;
		default:
			log_generic();
			break;
		}
		log_ifpresent();
	}

	@Override
	/** {@inheritDoc} */
	public void set_param(final Module_Parameter param) {
		param.basic_check(basic_check_bits_t.BC_TEMPLATE.getValue(), "objid template");
		switch (param.get_type()) {
		case MP_Omit:
			this.operator_assign(template_sel.OMIT_VALUE);
			break;
		case MP_Any:
			this.operator_assign(template_sel.ANY_VALUE);
			break;
		case MP_AnyOrNone:
			this.operator_assign(template_sel.ANY_OR_OMIT);
			break;
		case MP_List_Template:
		case MP_ComplementList_Template: {
			final TitanObjectid_template temp = new TitanObjectid_template();
			temp.set_type(param.get_type() == type_t.MP_List_Template ? template_sel.VALUE_LIST : template_sel.COMPLEMENTED_LIST, param.get_size());
			for (int i = 0; i < param.get_size(); i++) {
				temp.list_item(i).set_param(param.get_elem(i));
			}
			this.operator_assign(temp);
			break;
		}
		case MP_Objid:
			this.operator_assign(new TitanObjectid(param.get_string_size(), (TitanInteger[]) param.get_string_data()));
			break;
			//TODO: MP_Objid_Template in both runtime
		default:
			param.type_error("objid template");
			break;
		}
		is_ifPresent = param.get_ifpresent();
	}

	public Module_Parameter get_param(final Module_Param_Name param_name) {
		Module_Parameter mp = null;
		switch (template_selection) {
		case UNINITIALIZED_TEMPLATE:
			mp = new Module_Param_Unbound();
			break;
		case OMIT_VALUE:
			mp = new Module_Param_Omit();
			break;
		case ANY_VALUE:
			mp = new Module_Param_Any();
			break;
		case ANY_OR_OMIT:
			mp = new Module_Param_AnyOrNone();
			break;
		case SPECIFIC_VALUE:
			mp = single_value.get_param(param_name);
			break;
		case VALUE_LIST:
		case COMPLEMENTED_LIST: {
			if (template_selection == template_sel.VALUE_LIST) {
				mp = new Module_Param_List_Template();
			} else {
				mp = new Module_Param_ComplementList_Template();
			}
			for (int i = 0; i < value_list.size(); ++i) {
				mp.add_elem(value_list.get(i).get_param(param_name));
			}
			break;
		}
		default:
			throw new TtcnError("Referencing an uninitialized/unsupported objid template.");
		}
		if (is_ifPresent) {
			mp.set_ifpresent();
		}
		return mp;
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
	public void log_match(final TitanObjectid match_value, final boolean legacy) {
		if (TTCN_Logger.matching_verbosity_t.VERBOSITY_COMPACT == TTCN_Logger.get_matching_verbosity()
				&& TTCN_Logger.get_logmatch_buffer_len() != 0) {
			TTCN_Logger.print_logmatch_buffer();
			TTCN_Logger.log_event_str(" := ");
		}
		match_value.log();
		TTCN_Logger.log_event_str(" with ");
		log();
		if (match(match_value)) {
			TTCN_Logger.log_event_str(" matched");
		} else {
			TTCN_Logger.log_event_str(" unmatched");
		}
	}

	@Override
	/** {@inheritDoc} */
	public boolean match_omit(final boolean legacy) {
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
				// legacy behavior: 'omit' can appear in the
				// value/complement list
				for (int i = 0; i < value_list.size(); i++) {
					if (value_list.get(i).match_omit()) {
						return template_selection == template_sel.VALUE_LIST;
					}
				}
				return template_selection == template_sel.COMPLEMENTED_LIST;
			}
			// else fall through
		default:
			return false;
		}
	}

	@Override
	/** {@inheritDoc} */
	public void encode_text(final Text_Buf text_buf) {
		encode_text_base(text_buf);

		switch (template_selection) {
		case OMIT_VALUE:
		case ANY_VALUE:
		case ANY_OR_OMIT:
			break;
		case SPECIFIC_VALUE:
			single_value.encode_text(text_buf);
			break;
		case VALUE_LIST:
		case COMPLEMENTED_LIST:
			text_buf.push_int(value_list.size());
			for (int i = 0; i < value_list.size(); i++) {
				value_list.get(i).encode_text(text_buf);
			}
			break;
		default:
			throw new TtcnError("Text encoder: Encoding an uninitialized/unsupported objid template.");
		}
	}

	@Override
	/** {@inheritDoc} */
	public void decode_text(final Text_Buf text_buf) {
		clean_up();
		decode_text_base(text_buf);

		switch (template_selection) {
		case OMIT_VALUE:
		case ANY_VALUE:
		case ANY_OR_OMIT:
			break;
		case SPECIFIC_VALUE:
			single_value.decode_text(text_buf);
			break;
		case VALUE_LIST:
		case COMPLEMENTED_LIST: {
			final int size = text_buf.pull_int().get_int();
			value_list = new ArrayList<TitanObjectid_template>(size);
			for (int i = 0; i < size; i++) {
				final TitanObjectid_template temp = new TitanObjectid_template();
				temp.decode_text(text_buf);
				value_list.add(temp);
			}
			break;
		}
		default:
			throw new TtcnError("Text decoder: An unknown/unsupported selection was received for an objid template.");
		}
	}

	@Override
	public void check_restriction(final template_res restriction, final String name, final boolean legacy) {
		if (template_selection == template_sel.UNINITIALIZED_TEMPLATE) {
			return;
		}

		switch ((name != null && restriction == template_res.TR_VALUE) ? template_res.TR_OMIT : restriction) {
		case TR_VALUE:
			if (!is_ifPresent && template_selection == template_sel.SPECIFIC_VALUE) {
				return;
			}
			break;
		case TR_OMIT:
			if (!is_ifPresent && (template_selection == template_sel.OMIT_VALUE || template_selection == template_sel.SPECIFIC_VALUE)) {
				return;
			}
			break;
		case TR_PRESENT:
			if (!match_omit(legacy)) {
				return;
			}
			break;
		default:
			return;
		}

		throw new TtcnError(MessageFormat.format("Restriction `{0}'' on template of type {1} violated.", get_res_name(restriction), name == null ? "objid" : name));
	}
}
