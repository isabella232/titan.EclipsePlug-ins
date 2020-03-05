/******************************************************************************
 * Copyright (c) 2000-2020 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

import java.text.MessageFormat;
import java.util.ArrayList;

import org.eclipse.titan.runtime.core.Param_Types.Module_Param_Any;
import org.eclipse.titan.runtime.core.Param_Types.Module_Param_AnyOrNone;
import org.eclipse.titan.runtime.core.Param_Types.Module_Param_ComplementList_Template;
import org.eclipse.titan.runtime.core.Param_Types.Module_Param_List_Template;
import org.eclipse.titan.runtime.core.Param_Types.Module_Param_Name;
import org.eclipse.titan.runtime.core.Param_Types.Module_Param_Omit;
import org.eclipse.titan.runtime.core.Param_Types.Module_Param_Unbound;
import org.eclipse.titan.runtime.core.Param_Types.Module_Param_Verdict;
import org.eclipse.titan.runtime.core.Param_Types.Module_Parameter;
import org.eclipse.titan.runtime.core.Param_Types.Module_Parameter.basic_check_bits_t;
import org.eclipse.titan.runtime.core.Param_Types.Module_Parameter.type_t;
import org.eclipse.titan.runtime.core.TitanVerdictType.VerdictTypeEnum;

/**
 * TTCN-3 verdict type template
 * @author Arpad Lovassy
 * @author Andrea Palfi
 */
public class TitanVerdictType_template extends Base_Template {

	private TitanVerdictType single_value;

	// value_list part
	private ArrayList<TitanVerdictType_template> value_list;

	/**
	 * Initializes to unbound/uninitialized template.
	 * */
	public TitanVerdictType_template() {
		//do nothing
	}

	/**
	 * Initializes to a given template kind.
	 *
	 * @param otherValue
	 *                the template kind to initialize to.
	 * */
	public TitanVerdictType_template(final template_sel otherValue) {
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
	public TitanVerdictType_template(final VerdictTypeEnum otherValue) {
		super(template_sel.SPECIFIC_VALUE);
		if (!TitanVerdictType.is_valid(otherValue)) {
			throw new TtcnError("Creating a template from an invalid verdict value (" + otherValue + ").");
		}

		single_value = new TitanVerdictType(otherValue);
	}

	/**
	 * Initializes to a given value.
	 * The template becomes a specific template and the value is copied.
	 *
	 * @param otherValue
	 *                the value to initialize to.
	 * */
	public TitanVerdictType_template(final TitanVerdictType otherValue) {
		super(template_sel.SPECIFIC_VALUE);
		copy_value(otherValue);
	}

	/**
	 * Initializes to a given value.
	 * The template becomes a specific template and the value is copied.
	 * Causes dynamic testcase error if the parameter is not present or omit.
	 *
	 * @param otherValue
	 *                the value to initialize to.
	 * */
	public TitanVerdictType_template(final Optional<TitanVerdictType> other_value) {
		switch (other_value.get_selection()) {
		case OPTIONAL_PRESENT:
			copy_value(other_value.get());
			break;
		case OPTIONAL_OMIT:
			set_selection(template_sel.OMIT_VALUE);
			break;
		default:
			throw new TtcnError("Creating a verdict template from an unbound optional field.");
		}
	}

	/**
	 * Initializes to a given template.
	 *
	 * @param otherValue
	 *                the template to initialize to.
	 * */
	public TitanVerdictType_template(final TitanVerdictType_template other_value) {
		super();
		copy_template(other_value);
	}

	@Override
	public void clean_up() {
		switch (template_selection) {
		case SPECIFIC_VALUE:
			single_value = null;
			break;
		case VALUE_LIST:
		case COMPLEMENTED_LIST:
			value_list.clear();
			value_list = null;
			break;
		default:
			break;
		}
		template_selection = template_sel.UNINITIALIZED_TEMPLATE;
	}

	@Override
	public TitanVerdictType_template operator_assign(final Base_Type otherValue) {
		if (otherValue instanceof TitanVerdictType) {
			return operator_assign((TitanVerdictType)otherValue);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to verdict type", otherValue));
	}

	@Override
	public TitanVerdictType_template operator_assign(final Base_Template otherValue) {
		if (otherValue instanceof TitanVerdictType_template) {
			return operator_assign((TitanVerdictType_template)otherValue);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to verdict type template", otherValue));
	}

	@Override
	public TitanVerdictType_template operator_assign(final template_sel otherValue) {
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
	public TitanVerdictType_template operator_assign(final VerdictTypeEnum otherValue) {
		if (!TitanVerdictType.is_valid(otherValue)) {
			throw new TtcnError("Assignment of an invalid verdict value (" + otherValue + ") to a template.");
		}

		clean_up();
		set_selection(template_sel.SPECIFIC_VALUE);
		single_value = new TitanVerdictType(otherValue);

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
	public TitanVerdictType_template operator_assign(final TitanVerdictType otherValue) {
		otherValue.must_bound("Assignment of an unbound verdict value to a template.");

		clean_up();
		set_selection(template_sel.SPECIFIC_VALUE);
		copy_value(otherValue);

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
	public TitanVerdictType_template operator_assign(final Optional<TitanVerdictType> otherValue) {
		clean_up();
		switch (otherValue.get_selection()) {
		case OPTIONAL_PRESENT:
			copy_value(otherValue.get());
			break;
		case OPTIONAL_OMIT:
			set_selection(template_sel.OMIT_VALUE);
			break;
		default:
			throw new TtcnError("Assignment of an unbound optional field to a verdict template.");
		}
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
	public TitanVerdictType_template operator_assign(final TitanVerdictType_template otherValue) {
		if (otherValue != this) {
			clean_up();
			copy_template(otherValue);
		}

		return this;
	}

	private void copy_value(final TitanVerdictType otherValue)	{
		otherValue.must_bound("Creating a template from an unbound verdict value.");

		single_value = new TitanVerdictType(otherValue);
		set_selection(template_sel.SPECIFIC_VALUE);
	}

	private void copy_template(final TitanVerdictType_template otherValue) {
		switch (otherValue.template_selection) {
		case SPECIFIC_VALUE:
			single_value = new TitanVerdictType(otherValue.single_value);
			break;
		case OMIT_VALUE:
		case ANY_VALUE:
		case ANY_OR_OMIT:
			break;
		case VALUE_LIST:
		case COMPLEMENTED_LIST:
			value_list = new ArrayList<TitanVerdictType_template>(otherValue.value_list.size());
			for (int i = 0; i < otherValue.value_list.size(); i++) {
				final TitanVerdictType_template temp = new TitanVerdictType_template(otherValue.value_list.get(i));
				value_list.add(temp);
			}
			break;
		default:
			throw new TtcnError("Copying an uninitialized/unsupported verdict template.");
		}

		set_selection(otherValue);
	}

	@Override
	public boolean match(final Base_Type otherValue, final boolean legacy) {
		if (otherValue instanceof TitanVerdictType) {
			return match((TitanVerdictType) otherValue, legacy);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to verdict type", otherValue));
	}

	@Override
	public void log_match(final Base_Type match_value, final boolean legacy) {
		if (match_value instanceof TitanVerdictType) {
			log_match((TitanVerdictType) match_value, legacy);
			return;
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to verdict type", match_value));
	}

	/**
	 * Matches the provided value against this template.
	 *
	 * @param otherValue the value to be matched.
	 * */
	public boolean match(final TitanVerdictType otherValue) {
		return match(otherValue, false);
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
	public boolean match(final VerdictTypeEnum otherValue, final boolean legacy) {
		if (!TitanVerdictType.is_valid(otherValue)) {
			throw new TtcnError("Matching a verdict template with an invalid value (" + otherValue + ").");
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
			throw new TtcnError("Matching with an uninitialized/unsupported verdict template.");
		}
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
	public boolean match(final TitanVerdictType other_value, final boolean legacy) {
		if (!other_value.is_bound()) {
			return false;
		}

		return match(other_value.get_value(), legacy);
	}

	@Override
	public TitanVerdictType valueof() {
		if (template_selection != template_sel.SPECIFIC_VALUE || is_ifPresent) {
			throw new TtcnError("Performing a valueof or send operation on a non-specific verdict template.");
		}

		return new TitanVerdictType(single_value);
	}

	@Override
	public void set_type(final template_sel template_type, final int list_length) {
		if (template_type != template_sel.VALUE_LIST && template_type != template_sel.COMPLEMENTED_LIST) {
			throw new TtcnError("Internal error: Setting an invalid list type for a verdict template.");
		}

		clean_up();
		set_selection(template_type);
		value_list = new ArrayList<TitanVerdictType_template>(list_length);
		for (int i = 0; i < list_length; i++) {
			value_list.add(new TitanVerdictType_template());
		}
	}

	@Override
	public int n_list_elem() {
		if (template_selection != template_sel.VALUE_LIST && template_selection != template_sel.COMPLEMENTED_LIST) {
			throw new TtcnError("Accessing a list element of a non-list verdict template.");
		}

		return value_list.size();
	}

	@Override
	public TitanVerdictType_template list_item(final int listIndex) {
		if (template_selection != template_sel.VALUE_LIST && template_selection != template_sel.COMPLEMENTED_LIST) {
			throw new TtcnError("Internal error: Accessing a list element of a non-list verdict template.");
		}
		if (listIndex < 0) {
			throw new TtcnError("Accessing an verdict value list template using a negative index (" + listIndex + ").");
		}
		if (listIndex >= value_list.size()) {
			throw new TtcnError("Internal error: Index overflow in a verdict value list template.");
		}

		return value_list.get(listIndex);
	}

	@Override
	public void log() {
		switch (template_selection) {
		case SPECIFIC_VALUE:
			if (TitanVerdictType.is_valid(single_value.get_value())) {
				TTCN_Logger.log_event(TitanVerdictType.verdict_name[ single_value.get_value().ordinal() ]);
			} else {
				TTCN_Logger.log_event("<unknown verdict value: " + single_value + ">");
			}
			break;
		case COMPLEMENTED_LIST:
			TTCN_Logger.log_event_str("complement");
			// no break
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
	public void log_match(final TitanVerdictType match_value, final boolean legacy) {
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
	public void set_param(Module_Parameter param) {
		param.basic_check(basic_check_bits_t.BC_TEMPLATE.getValue(), "verdict template");

		// Originally RT2
		if (param.get_type() == Module_Parameter.type_t.MP_Reference) {
			param = param.get_referenced_param().get();
		}

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
			final TitanVerdictType_template temp = new TitanVerdictType_template();
			temp.set_type(param.get_type() == type_t.MP_List_Template ? template_sel.VALUE_LIST : template_sel.COMPLEMENTED_LIST, param.get_size());
			for (int i = 0; i < param.get_size(); i++) {
				temp.list_item(i).set_param(param.get_elem(i));
			}
			this.operator_assign(temp);
			break;
		}
		case MP_Verdict:
			this.operator_assign(param.get_verdict());
			break;
		default:
			param.type_error("verdict template");
			break;
		}
		is_ifPresent = param.get_ifpresent();
	}

	@Override
	/** {@inheritDoc} */
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
			mp = new Module_Param_Verdict(single_value);
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
			throw new TtcnError("Referencing an uninitialized/unsupported verdict template.");
		}
		if (is_ifPresent) {
			mp.set_ifpresent();
		}
		return mp;
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
			return false;
		default:
			return false;
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

		throw new TtcnError(MessageFormat.format("Restriction `{0}'' on template of type {1} violated.", get_res_name(restriction), name == null ? "float" : name));
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
			throw new TtcnError("Text encoder: Encoding an uninitialized/unsupported verdict template.");
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
			value_list = new ArrayList<TitanVerdictType_template>(size);
			for (int i = 0; i < size; i++) {
				final TitanVerdictType_template temp = new TitanVerdictType_template();
				temp.decode_text(text_buf);
				value_list.add(temp);
			}
			break;
		}
		default:
			throw new TtcnError("Text decoder: An unknown/unsupported selection was received for a verdict template.");
		}
	}
}
