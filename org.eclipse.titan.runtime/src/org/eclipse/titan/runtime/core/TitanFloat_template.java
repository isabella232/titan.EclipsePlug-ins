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
import org.eclipse.titan.runtime.core.Param_Types.Module_Param_Float;
import org.eclipse.titan.runtime.core.Param_Types.Module_Param_FloatRange;
import org.eclipse.titan.runtime.core.Param_Types.Module_Param_List_Template;
import org.eclipse.titan.runtime.core.Param_Types.Module_Param_Name;
import org.eclipse.titan.runtime.core.Param_Types.Module_Param_Omit;
import org.eclipse.titan.runtime.core.Param_Types.Module_Param_Unbound;
import org.eclipse.titan.runtime.core.Param_Types.Module_Parameter;
import org.eclipse.titan.runtime.core.Param_Types.Module_Parameter.basic_check_bits_t;
import org.eclipse.titan.runtime.core.Param_Types.Module_Parameter.type_t;

/**
 * TTCN-3 float template
 * @author Farkas Izabella Ingrid
 * @author Andrea Palfi
 * @author Arpad Lovassy
 *
 * Not yet complete rewrite
 */
public class TitanFloat_template extends Base_Template {
	private TitanFloat single_value;

	// value_list part
	private ArrayList<TitanFloat_template> value_list;

	// value range part
	private boolean min_is_present, max_is_present;
	private boolean min_is_exclusive, max_is_exclusive;
	private TitanFloat min_value, max_value;

	/**
	 * Initializes to unbound/uninitialized template.
	 * */
	public TitanFloat_template() {
		// do nothing
	}

	/**
	 * Initializes to a given template kind.
	 *
	 * @param otherValue
	 *                the template kind to initialize to.
	 * */
	public TitanFloat_template(final template_sel otherValue) {
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
	public TitanFloat_template(final double otherValue) {
		super(template_sel.SPECIFIC_VALUE);
		single_value = new TitanFloat(otherValue);
	}

	/**
	 * Initializes to a given value.
	 * The template becomes a specific template and the value is copied.
	 *
	 * @param otherValue
	 *                the value to initialize to.
	 * */
	public TitanFloat_template(final Ttcn3Float otherValue) {
		super(template_sel.SPECIFIC_VALUE);
		single_value = new TitanFloat(otherValue);
	}

	/**
	 * Initializes to a given value.
	 * The template becomes a specific template and the value is copied.
	 *
	 * @param otherValue
	 *                the value to initialize to.
	 * */
	public TitanFloat_template(final TitanFloat otherValue) {
		super(template_sel.SPECIFIC_VALUE);
		otherValue.must_bound("Creating a template from an unbound float value.");

		single_value = new TitanFloat(otherValue);
	}

	/**
	 * Initializes to a given value.
	 * The template becomes a specific template and the value is copied.
	 * Causes dynamic testcase error if the parameter is not present or omit.
	 *
	 * @param otherValue
	 *                the value to initialize to.
	 * */
	public TitanFloat_template(final Optional<TitanFloat> otherValue) {
		switch (otherValue.get_selection()) {
		case OPTIONAL_PRESENT:
			set_selection(template_sel.SPECIFIC_VALUE);
			single_value = new TitanFloat(otherValue.constGet());
			break;
		case OPTIONAL_OMIT:
			set_selection(template_sel.OMIT_VALUE);
			break;
		case OPTIONAL_UNBOUND:
			throw new TtcnError("Creating a float template from an unbound optional field.");
		}
	}

	/**
	 * Initializes to a given template.
	 *
	 * @param otherValue
	 *                the template to initialize to.
	 * */
	public TitanFloat_template(final TitanFloat_template otherValue) {
		copy_template(otherValue);
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
		case VALUE_RANGE:
			min_value = null;
			max_value = null;
			break;
		default:
			break;
		}
		template_selection = template_sel.UNINITIALIZED_TEMPLATE;
	}

	@Override
	public TitanFloat_template operator_assign(final Base_Type otherValue) {
		if (otherValue instanceof TitanFloat) {
			return operator_assign((TitanFloat) otherValue);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to float", otherValue));
	}

	@Override
	public TitanFloat_template operator_assign(final Base_Template otherValue) {
		if (otherValue instanceof TitanFloat_template) {
			return operator_assign((TitanFloat_template) otherValue);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to float template", otherValue));
	}

	@Override
	public TitanFloat_template operator_assign(final template_sel otherValue) {
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
	public TitanFloat_template operator_assign(final double otherValue) {
		clean_up();
		set_selection(template_sel.SPECIFIC_VALUE);
		single_value = new TitanFloat(otherValue);

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
	public TitanFloat_template operator_assign(final Ttcn3Float otherValue) {
		clean_up();
		set_selection(template_sel.SPECIFIC_VALUE);
		single_value = new TitanFloat(otherValue);

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
	public TitanFloat_template operator_assign(final TitanFloat otherValue) {
		otherValue.must_bound("Assignment of an unbound float value to a template.");

		clean_up();
		set_selection(template_sel.SPECIFIC_VALUE);
		single_value = new TitanFloat(otherValue);

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
	public TitanFloat_template operator_assign(final TitanFloat_template otherValue) {
		if (otherValue != this) {
			clean_up();
			copy_template(otherValue);
		}

		return this;
	}

	private void copy_template(final TitanFloat_template otherValue) {
		switch (otherValue.template_selection) {
		case SPECIFIC_VALUE:
			single_value = new TitanFloat(otherValue.single_value);
			break;
		case OMIT_VALUE:
		case ANY_VALUE:
		case ANY_OR_OMIT:
			break;
		case VALUE_LIST:
		case COMPLEMENTED_LIST:
			value_list = new ArrayList<TitanFloat_template>(otherValue.value_list.size());
			for (int i = 0; i < otherValue.value_list.size(); i++) {
				final TitanFloat_template temp = new TitanFloat_template(otherValue.value_list.get(i));
				value_list.add(temp);
			}
			break;
		case VALUE_RANGE:
			min_is_present = otherValue.min_is_present;
			min_is_exclusive = otherValue.min_is_exclusive;
			if (min_is_present) {
				min_value = new TitanFloat(otherValue.min_value);
			}
			max_is_present = otherValue.max_is_present;
			max_is_exclusive = otherValue.max_is_exclusive;
			if (max_is_present) {
				max_value = new TitanFloat(otherValue.max_value);
			}
			break;
		default:
			throw new TtcnError("Copying an uninitialized/unsupported float template.");
		}

		set_selection(otherValue);
	}

	@Override
	public boolean match(final Base_Type otherValue, final boolean legacy) {
		if (otherValue instanceof TitanFloat) {
			return match((TitanFloat) otherValue, legacy);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to float", otherValue));
	}

	@Override
	public void log_match(final Base_Type match_value, final boolean legacy) {
		if (match_value instanceof TitanFloat) {
			log_match((TitanFloat) match_value, legacy);
			return;
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to float", match_value));
	}

	/**
	 * Matches the provided value against this template.
	 *
	 * @param otherValue the value to be matched.
	 * */
	public boolean match(final TitanFloat otherValue) {
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
	public boolean match(final TitanFloat otherValue, final boolean legacy) {
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
		case VALUE_RANGE: {
			boolean lowerMatch = false;
			boolean upperMatch = false;
			if (min_is_present) {
				if (!min_is_exclusive && min_value.is_less_than_or_equal(otherValue)) {
					lowerMatch = true;
				} else if (min_is_exclusive && min_value.is_less_than(otherValue)) {
					lowerMatch = true;
				}
			} else if (!min_is_exclusive || otherValue.is_greater_than(Double.NEGATIVE_INFINITY)) {
				lowerMatch = true;
			}
			if (max_is_present) {
				if (!max_is_exclusive && max_value.is_greater_than_or_equal(otherValue)) {
					upperMatch = true;
				} else if (max_is_exclusive && max_value.is_greater_than(otherValue)) {
					upperMatch = true;
				}
			} else if (!max_is_exclusive || otherValue.is_less_than(Double.POSITIVE_INFINITY)) {
				upperMatch = true;
			}

			return lowerMatch && upperMatch;
		}
		default:
			throw new TtcnError("Matching with an uninitialized/unsupported float template.");
		}
	}

	@Override
	public void set_type(final template_sel templateType, final int listLength) {
		clean_up();
		switch (templateType) {
		case VALUE_LIST:
		case COMPLEMENTED_LIST:
			set_selection(templateType);
			value_list = new ArrayList<TitanFloat_template>(listLength);
			for (int i = 0; i < listLength; ++i) {
				value_list.add(new TitanFloat_template());
			}
			break;
		case VALUE_RANGE:
			set_selection(template_sel.VALUE_RANGE);
			min_is_present = false;
			max_is_present = false;
			min_is_exclusive = false;
			max_is_exclusive = false;
			break;
		default:
			throw new TtcnError("Setting an invalid type for a float template.");
		}
	}

	@Override
	public int n_list_elem() {
		if (template_selection != template_sel.VALUE_LIST && template_selection != template_sel.COMPLEMENTED_LIST) {
			throw new TtcnError("Accessing a list element of a non-list float template.");
		}

		return value_list.size();
	}

	@Override
	public TitanFloat_template list_item(final int listIndex) {
		if (template_selection != template_sel.VALUE_LIST && template_selection != template_sel.COMPLEMENTED_LIST) {
			throw new TtcnError("Accessing a list element of a non-list float template.");
		}
		if (listIndex < 0) {
			throw new TtcnError("Accessing an bitstring value list template using a negative index (" + listIndex + ").");
		}
		if (listIndex >= value_list.size()) {
			throw new TtcnError("Index overflow in a float value list template.");
		}

		return value_list.get(listIndex);
	}

	public void set_min(final double minValue) {
		if (template_selection != template_sel.VALUE_RANGE) {
			throw new TtcnError("Float template is not range when setting lower limit.");
		}
		if (max_is_present && min_is_present && max_value.is_less_than(min_value)) {
			throw new TtcnError("The lower limit of the range is greater than the " + "upper limit in a float template.");
		}

		min_is_present = true;
		min_is_exclusive = false;
		min_value = new TitanFloat(minValue);
	}

	public void set_min(final Ttcn3Float minValue) {
		if (template_selection != template_sel.VALUE_RANGE) {
			throw new TtcnError("Float template is not range when setting lower limit.");
		}
		if (max_is_present && min_is_present && max_value.is_less_than(min_value)) {
			throw new TtcnError("The lower limit of the range is greater than the " + "upper limit in a float template.");
		}

		min_is_present = true;
		min_is_exclusive = false;
		min_value = new TitanFloat(minValue);
	}

	public void set_min(final TitanFloat minValue) {
		minValue.must_bound("Using an unbound value when setting the lower bound " + "in a float range template.");

		set_min(minValue.get_value());
	}

	public void set_max(final double maxValue) {
		if (template_selection != template_sel.VALUE_RANGE) {
			throw new TtcnError("Float template is not range when setting upper limit.");
		}
		if (min_is_present && max_is_present && min_value.is_greater_than(max_value)) {
			throw new TtcnError("The upper limit of the range is smaller than the " + "lower limit in a float template.");
		}

		max_is_present = true;
		max_is_exclusive = false;
		max_value = new TitanFloat(maxValue);
	}

	public void set_max(final Ttcn3Float maxValue) {
		if (template_selection != template_sel.VALUE_RANGE) {
			throw new TtcnError("Float template is not range when setting upper limit.");
		}
		if (min_is_present && max_is_present && min_value.is_greater_than(max_value)) {
			throw new TtcnError("The upper limit of the range is smaller than the " + "lower limit in a float template.");
		}

		max_is_present = true;
		max_is_exclusive = false;
		max_value = new TitanFloat(maxValue);
	}

	public void set_max(final TitanFloat maxValue) {
		maxValue.must_bound("Using an unbound value when setting the upper bound " + "in a float range template.");

		set_max(maxValue.get_value());
	}

	public void set_min_exclusive(final boolean minExclusive) {
		if (template_selection != template_sel.VALUE_RANGE) {
			throw new TtcnError("Float template is not range when setting lower limit exclusiveness.");
		}

		min_is_exclusive = minExclusive;
	}

	public void set_max_exclusive(final boolean maxExclusive) {
		if (template_selection != template_sel.VALUE_RANGE) {
			throw new TtcnError("Float template is not range when setting upper limit exclusiveness.");
		}

		max_is_exclusive = maxExclusive;
	}

	@Override
	public TitanFloat valueof() {
		if (template_selection != template_sel.SPECIFIC_VALUE || is_ifPresent) {
			throw new TtcnError("Performing a valueof or send operation on a non-specific float template.");
		}

		return single_value;
	}

	@Override
	public void log() {
		switch (template_selection) {
		case SPECIFIC_VALUE: {
			TitanFloat.log_float(single_value.get_value());
			break;
		}
		case COMPLEMENTED_LIST:
			TTCN_Logger.log_event_str("complement");
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

		case VALUE_RANGE:
			TTCN_Logger.log_char('(');
			if (min_is_exclusive) {
				TTCN_Logger.log_char('!');
			}
			if (min_is_present) {
				TitanFloat.log_float(min_value.get_value());
			} else {
				TTCN_Logger.log_event_str("-infinity");
			}
			TTCN_Logger.log_event_str(" .. ");
			if (max_is_exclusive) {
				TTCN_Logger.log_char('!');
			}
			if (max_is_present) {
				TitanFloat.log_float(max_value.get_value());
			} else {
				TTCN_Logger.log_event_str("infinity");
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
	public void log_match(final TitanFloat match_value, final boolean legacy) {
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
	/** {@inheritDoc} */
	public void encode_text(final Text_Buf text_buf) {
		encode_text_base(text_buf);

		switch (template_selection) {
		case OMIT_VALUE:
		case ANY_VALUE:
		case ANY_OR_OMIT:
			break;
		case SPECIFIC_VALUE:
			text_buf.push_double(single_value.get_value());
			break;
		case VALUE_LIST:
		case COMPLEMENTED_LIST:
			text_buf.push_int(value_list.size());
			for (int i = 0; i < value_list.size(); i++) {
				value_list.get(i).encode_text(text_buf);
			}
			break;
		case VALUE_RANGE:
			text_buf.push_int(min_is_present ? 1 : 0);
			if (min_is_present) {
				text_buf.push_double(min_value.get_value());
			}
			text_buf.push_int(max_is_present ? 1 : 0);
			if (max_is_present) {
				text_buf.push_double(max_value.get_value());
			}
			break;
		default:
			throw new TtcnError("Text encoder: Encoding an uninitialized/unsupported float template.");
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
			single_value = new TitanFloat(text_buf.pull_double());
			break;
		case VALUE_LIST:
		case COMPLEMENTED_LIST: {
			final int size = text_buf.pull_int().get_int();
			value_list = new ArrayList<TitanFloat_template>(size);
			for (int i = 0; i < size; i++) {
				final TitanFloat_template temp = new TitanFloat_template();
				temp.decode_text(text_buf);
				value_list.add(temp);
			}
			break;
		}
		case VALUE_RANGE:
			min_is_present = text_buf.pull_int().get_int() != 0;
			if (min_is_present) {
				min_value = new TitanFloat(text_buf.pull_double());
			}
			max_is_present = text_buf.pull_int().get_int() != 0;
			if (max_is_present) {
				max_value = new TitanFloat(text_buf.pull_double());
			}
			min_is_exclusive = false;
			max_is_exclusive = false;
			break;
		default:
			throw new TtcnError("Text decoder: An unknown/unsupported selection was received for a float template.");
		}
	}

	@Override
	/** {@inheritDoc} */
	public void set_param(Module_Parameter param) {
		param.basic_check(basic_check_bits_t.BC_TEMPLATE.getValue(), "float template");

		// Originally RT2
		if (param.get_type() == Module_Parameter.type_t.MP_Reference) {
			param = param.get_referenced_param().get();
		}

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
			final TitanFloat_template temp = new TitanFloat_template();
			temp.set_type(param.get_type() == type_t.MP_List_Template ?
					template_sel.VALUE_LIST : template_sel.COMPLEMENTED_LIST, param.get_size());
			for (int i = 0; i < param.get_size(); i++) {
				temp.list_item(i).set_param(param.get_elem(i));
			}
			operator_assign(temp);
			break;
		}
		case MP_Float:
			operator_assign(param.get_float());
			break;
		case MP_FloatRange: {
			set_type(template_sel.VALUE_RANGE);
			if (param.has_lower_float()) {
				set_min(param.get_lower_float());
			}
			set_min_exclusive(param.get_is_min_exclusive());
			if (param.has_upper_float()) {
				set_max(param.get_upper_float());
			}
			set_max_exclusive(param.get_is_max_exclusive());
			break;
		}
		case MP_Expression:
			switch (param.get_expr_type()) {
			case EXPR_NEGATE: {
				final TitanFloat operand = new TitanFloat();
				operand.set_param(param.get_operand1());
				operator_assign(operand.sub());
				break;
			}
			case EXPR_ADD: {
				final TitanFloat operand1 = new TitanFloat();
				final TitanFloat operand2 = new TitanFloat();
				operand1.set_param(param.get_operand1());
				operand2.set_param(param.get_operand2());
				operator_assign(operand1.add(operand2));
				break;
			}
			case EXPR_SUBTRACT: {
				final TitanFloat operand1 = new TitanFloat();
				final TitanFloat operand2 = new TitanFloat();
				operand1.set_param(param.get_operand1());
				operand2.set_param(param.get_operand2());
				operator_assign(operand1.sub(operand2));
				break;
			}
			case EXPR_MULTIPLY: {
				final TitanFloat operand1 = new TitanFloat();
				final TitanFloat operand2 = new TitanFloat();
				operand1.set_param(param.get_operand1());
				operand2.set_param(param.get_operand2());
				operator_assign(operand1.mul(operand2));
				break;
			}
			case EXPR_DIVIDE: {
				final TitanFloat operand1 = new TitanFloat();
				final TitanFloat operand2 = new TitanFloat();
				operand1.set_param(param.get_operand1());
				operand2.set_param(param.get_operand2());
				if (operand2.operator_equals(0)) {
					param.error("Floating point division by zero.");
				}
				operator_assign(operand1.div(operand2));
				break;
			}
			default:
				param.expr_type_error("a float");
				break;
			}
			break;
		default:
			param.type_error("float template");
		}
		is_ifPresent = param.get_ifpresent() || param.get_ifpresent();
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
			mp = new Module_Param_Float(single_value.get_value());
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
		case VALUE_RANGE:
			mp = new Module_Param_FloatRange( min_value.get_value(), min_is_present,
											  max_value.get_value(), max_is_present,
											  min_is_exclusive, max_is_exclusive );
			break;
		default:
			throw new TtcnError("Referencing an uninitialized/unsupported float template.");
		}
		if (is_ifPresent) {
			mp.set_ifpresent();
		}
		return mp;
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
}
