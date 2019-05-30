/******************************************************************************
 * Copyright (c) 2000-2019 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

import java.math.BigInteger;
import java.text.MessageFormat;
import java.util.ArrayList;

import org.eclipse.titan.runtime.core.Param_Types.Module_Parameter;
import org.eclipse.titan.runtime.core.Param_Types.Module_Parameter.basic_check_bits_t;
import org.eclipse.titan.runtime.core.Param_Types.Module_Parameter.type_t;

/**
 * TTCN-3 integer template
 *
 * Not yet complete rewrite
 */
public class TitanInteger_template extends Base_Template {
	// int_val part
	// TODO maybe should be renamed in core
	private TitanInteger single_value;

	// value_list part
	private ArrayList<TitanInteger_template> value_list;

	// value range part
	private boolean min_is_present, max_is_present;
	private boolean min_is_exclusive, max_is_exclusive;
	private TitanInteger min_value, max_value;

	/**
	 * Initializes to unbound/uninitialized template.
	 * */
	public TitanInteger_template() {
		// do nothing
	}

	/**
	 * Initializes to a given template kind.
	 *
	 * @param otherValue
	 *                the template kind to initialize to.
	 * */
	public TitanInteger_template(final template_sel otherValue) {
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
	public TitanInteger_template(final int otherValue) {
		super(template_sel.SPECIFIC_VALUE);
		single_value = new TitanInteger(otherValue);
	}

	/**
	 * Initializes to a given value.
	 * The template becomes a specific template and the value is copied.
	 *
	 * @param otherValue
	 *                the value to initialize to.
	 * */
	public TitanInteger_template(final BigInteger otherValue) {
		super(template_sel.SPECIFIC_VALUE);
		single_value = new TitanInteger(otherValue);
	}

	/**
	 * Initializes to a given value.
	 * The template becomes a specific template and the value is copied.
	 *
	 * @param otherValue
	 *                the value to initialize to.
	 * */
	public TitanInteger_template(final TitanInteger otherValue) {
		super(template_sel.SPECIFIC_VALUE);
		otherValue.must_bound("Creating a template from an unbound integer value.");

		single_value = new TitanInteger(otherValue);
	}

	/**
	 * Initializes to a given value.
	 * The template becomes a specific template and the value is copied.
	 * Causes dynamic testcase error if the parameter is not present or omit.
	 *
	 * @param otherValue
	 *                the value to initialize to.
	 * */
	public TitanInteger_template(final Optional<TitanInteger> otherValue) {
		switch (otherValue.get_selection()) {
		case OPTIONAL_PRESENT:
			set_selection(template_sel.SPECIFIC_VALUE);
			single_value = new TitanInteger(otherValue.constGet());
			break;
		case OPTIONAL_OMIT:
			set_selection(template_sel.OMIT_VALUE);
			break;
		case OPTIONAL_UNBOUND:
			throw new TtcnError("Creating an integer template from an unbound optional field.");
		}
	}

	/**
	 * Initializes to a given template.
	 *
	 * @param otherValue
	 *                the template to initialize to.
	 * */
	public TitanInteger_template(final TitanInteger_template otherValue) {
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
	public TitanInteger_template operator_assign(final Base_Type otherValue) {
		if (otherValue instanceof TitanInteger) {
			return operator_assign((TitanInteger)otherValue);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to integer", otherValue));
	}

	@Override
	public TitanInteger_template operator_assign(final Base_Template otherValue) {
		if (otherValue instanceof TitanInteger_template) {
			return operator_assign((TitanInteger_template)otherValue);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to integer", otherValue));
	}

	@Override
	public void log_match(final Base_Type match_value, final boolean legacy) {
		if (match_value instanceof TitanInteger) {
			log_match((TitanInteger) match_value, legacy);
			return;
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to integer", match_value));
	}

	@Override
	public TitanInteger_template operator_assign(final template_sel otherValue) {
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
	public TitanInteger_template operator_assign(final int otherValue) {
		clean_up();
		set_selection(template_sel.SPECIFIC_VALUE);
		single_value = new TitanInteger(otherValue);

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
	public TitanInteger_template operator_assign(final BigInteger otherValue) {
		clean_up();
		set_selection(template_sel.SPECIFIC_VALUE);
		single_value = new TitanInteger(otherValue);

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
	public TitanInteger_template operator_assign(final TitanInteger otherValue) {
		otherValue.must_bound("Assignment of an unbound integer value to a template.");

		clean_up();
		set_selection(template_sel.SPECIFIC_VALUE);
		single_value = new TitanInteger(otherValue);

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
	public TitanInteger_template operator_assign(final TitanInteger_template otherValue) {
		if (otherValue != this) {
			clean_up();
			copy_template(otherValue);
		}

		return this;
	}

	private void copy_template(final TitanInteger_template otherValue) {
		switch (otherValue.template_selection) {
		case SPECIFIC_VALUE:
			single_value = new TitanInteger(otherValue.single_value);
			break;
		case OMIT_VALUE:
		case ANY_VALUE:
		case ANY_OR_OMIT:
			break;
		case VALUE_LIST:
		case COMPLEMENTED_LIST:
			value_list = new ArrayList<TitanInteger_template>(otherValue.value_list.size());
			for (int i = 0; i < otherValue.value_list.size(); i++) {
				final TitanInteger_template temp = new TitanInteger_template(otherValue.value_list.get(i));
				value_list.add(temp);
			}
			break;
		case VALUE_RANGE:
			min_is_present = otherValue.min_is_present;
			min_is_exclusive = otherValue.min_is_exclusive;
			if (min_is_present) {
				min_value = new TitanInteger(otherValue.min_value);
			}
			max_is_present = otherValue.max_is_present;
			max_is_exclusive = otherValue.max_is_exclusive;
			if (max_is_present) {
				max_value = new TitanInteger(otherValue.max_value);
			}
			break;
		default:
			throw new TtcnError("Copying an uninitialized/unsupported integer template.");
		}

		set_selection(otherValue);
	}

	@Override
	public boolean match(final Base_Type otherValue, final boolean legacy) {
		if (otherValue instanceof TitanInteger) {
			return match((TitanInteger) otherValue, legacy);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to integer", otherValue));
	}

	/**
	 * Matches the provided value against this template.
	 *
	 * @param otherValue the value to be matched.
	 * */
	public boolean match(final TitanInteger otherValue) {
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
	public boolean match(final TitanInteger otherValue, final boolean legacy) {
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
			boolean lowerMatch = true;
			boolean upperMatch = true;
			if (min_is_present) {
				if (min_is_exclusive) {
					lowerMatch = min_value.is_less_than(otherValue);
				} else {
					lowerMatch = min_value.is_less_than_or_equal(otherValue);
				}
			}
			if (max_is_present) {
				if (max_is_exclusive) {
					upperMatch = max_value.is_greater_than(otherValue);
				} else {
					upperMatch = max_value.is_greater_than_or_equal(otherValue);
				}
			}
			return lowerMatch && upperMatch;
		}
		default:
			throw new TtcnError("Matching with an uninitialized/unsupported integer template.");
		}
	}

	@Override
	public TitanInteger valueof() {
		if (template_selection != template_sel.SPECIFIC_VALUE) {
			throw new TtcnError("Performing a valueof or send operation on a non-specific integer template.");
		}

		return new TitanInteger(single_value);
	}

	@Override
	public void set_type(final template_sel templateType, final int listLength) {
		clean_up();
		switch (templateType) {
		case VALUE_LIST:
		case COMPLEMENTED_LIST:
			set_selection(templateType);
			value_list = new ArrayList<TitanInteger_template>(listLength);
			for (int i = 0; i < listLength; i++) {
				value_list.add(new TitanInteger_template());
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
			throw new TtcnError("Setting an invalid type for an integer template.");
		}
	}

	@Override
	public int n_list_elem() {
		if (template_selection != template_sel.VALUE_LIST && template_selection != template_sel.COMPLEMENTED_LIST) {
			throw new TtcnError("Accessing a list element of a non-list integer template.");
		}

		return value_list.size();
	}

	@Override
	public TitanInteger_template list_item(final int listIndex) {
		if (!template_sel.VALUE_LIST.equals(template_selection) &&
				!template_sel.COMPLEMENTED_LIST.equals(template_selection)) {
			throw new TtcnError("Accessing a list element of a non-list integer template.");
		}
		if (listIndex < 0) {
			throw new TtcnError("Accessing an integer value list template using a negative index (" + listIndex + ").");
		}
		if (listIndex > value_list.size()) {
			throw new TtcnError("Index overflow in an integer value list template.");
		}

		return value_list.get(listIndex);
	}

	public void set_min(final int otherMinValue) {
		if (!template_sel.VALUE_RANGE.equals(template_selection)) {
			throw new TtcnError("Integer template is not range when setting lower limit.");
		}

		if (max_is_present) {
			if (!max_value.is_greater_than_or_equal(otherMinValue)) {
				throw new TtcnError("The lower limit of the range is greater than the upper limit in an integer template.");
			}
		}

		min_is_present = true;
		min_is_exclusive = false;
		min_value = new TitanInteger(otherMinValue);
	}

	public void set_min(final TitanInteger otherMinValue) {
		otherMinValue.must_bound("Using an unbound value when setting the lower bound in an integer range template.");

		if (!template_sel.VALUE_RANGE.equals(template_selection)) {
			throw new TtcnError("Integer template is not range when setting lower limit.");
		}

		if (max_is_present) {
			if (!max_value.is_greater_than_or_equal(otherMinValue)) {
				throw new TtcnError("The lower limit of the range is greater than the upper limit in an integer template.");
			}
		}

		min_is_present = true;
		min_is_exclusive = false;
		min_value = otherMinValue;
	}

	public void set_min_exclusive(final boolean minExclusive) {
		min_is_exclusive = minExclusive;
	}

	public void set_max(final int otherMaxValue) {
		if (!template_sel.VALUE_RANGE.equals(template_selection)) {
			throw new TtcnError("Integer template is not range when setting upper limit.");
		}

		if (min_is_present) {
			if (min_value.is_greater_than(otherMaxValue)) {
				throw new TtcnError("The upper limit of the range is smaller than the lower limit in an integer template.");
			}
		}

		max_is_present = true;
		max_is_exclusive = false;
		max_value = new TitanInteger(otherMaxValue);
	}

	public void set_max(final TitanInteger otherMaxValue) {
		otherMaxValue.must_bound("Using an unbound value when setting the upper bound in an integer range template.");

		if (!template_sel.VALUE_RANGE.equals(template_selection)) {
			throw new TtcnError("Integer template is not range when setting upper limit.");
		}

		if (max_is_present) {
			if (!max_value.is_greater_than(otherMaxValue)) {
				throw new TtcnError("TThe upper limit of the range is smaller than the lower limit in an integer template.");
			}
		}

		max_is_present = true;
		max_is_exclusive = false;
		max_value = otherMaxValue;
	}

	public void set_max_exclusive(final boolean maxExclusive) {
		max_is_exclusive = maxExclusive;
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
	public void log() {
		switch (template_selection) {
		case SPECIFIC_VALUE: {
			String tmp_str;
			if (single_value.is_native()) {
				tmp_str = Integer.toString(single_value.get_int());
			} else {
				tmp_str = single_value.get_BigInteger().toString();
			}
			TTCN_Logger.log_event("%s", tmp_str);
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
				if (min_value.is_native()) {
					TTCN_Logger.log_event("%s", Integer.toString(min_value.get_int()));
				} else {
					TTCN_Logger.log_event("%s", min_value.get_BigInteger().toString());
				}
			} else {
				TTCN_Logger.log_event_str("-infinity");
			}
			TTCN_Logger.log_event_str(" .. ");

			if (max_is_exclusive) {
				TTCN_Logger.log_char('!');
			}
			if (max_is_present) {
				if (max_value.is_native()) {
					TTCN_Logger.log_event("%s", Integer.toString(max_value.get_int()));
				} else {
					TTCN_Logger.log_event("%s", max_value.get_BigInteger().toString());
				}
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
	public void log_match(final TitanInteger match_value, final boolean legacy) {
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
	public void encode_text(final Text_Buf text_buf) {
		encode_text_base(text_buf);

		switch (template_selection) {
		case OMIT_VALUE:
		case ANY_VALUE:
		case ANY_OR_OMIT:
			break;
		case SPECIFIC_VALUE:
			text_buf.push_int(single_value);
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
				text_buf.push_int(min_value);
			}
			text_buf.push_int(max_is_present ? 1 : 0);
			if (max_is_present) {
				text_buf.push_int(max_value);
			}
			break;
		default:
			throw new TtcnError("Text encoder: Encoding an uninitialized/unsupported integer template.");
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
			single_value = text_buf.pull_int();
			break;
		case VALUE_LIST:
		case COMPLEMENTED_LIST: {
			final int size = text_buf.pull_int().get_int();
			value_list = new ArrayList<TitanInteger_template>(size);
			for (int i = 0; i < size; i++) {
				final TitanInteger_template temp = new TitanInteger_template();
				temp.decode_text(text_buf);
				value_list.add(temp);
			}
			break;
		}
		case VALUE_RANGE:
			min_is_present = text_buf.pull_int().get_int() != 0;
			if (min_is_present) {
				min_value = text_buf.pull_int();
			}
			max_is_present = text_buf.pull_int().get_int() != 0;
			if (max_is_present) {
				max_value = text_buf.pull_int();
			}
			min_is_exclusive = false;
			max_is_exclusive = false;
			break;
		default:
			throw new TtcnError("Text decoder: An unknown/unsupported selection was received for an integer template.");
		}
	}

	@Override
	/** {@inheritDoc} */
	public void set_param(final Module_Parameter param) {
		param.basic_check(basic_check_bits_t.BC_TEMPLATE.getValue(), "integer template");
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
			final TitanInteger_template temp = new TitanInteger_template();
			temp.set_type(param.get_type() == type_t.MP_List_Template ?
					template_sel.VALUE_LIST : template_sel.COMPLEMENTED_LIST, param.get_size());
			for (int i = 0; i < param.get_size(); i++) {
				temp.list_item(i).set_param(param.get_elem(i));
			}
			operator_assign(temp);
			break;
		}
		case MP_Integer:
			operator_assign(param.get_integer());
			break;
		case MP_IntRange: {
			set_type(template_sel.VALUE_RANGE);
			if (param.get_lower_int() != null) {
				set_min(param.get_lower_int());
			}
			set_min_exclusive(param.get_is_min_exclusive());
			if (param.get_upper_int() != null) {
				set_max(param.get_upper_int());
			}
			set_max_exclusive(param.get_is_max_exclusive());
			break;
		}
		case MP_Expression:
			switch (param.get_expr_type()) {
			case EXPR_NEGATE: {
				final TitanInteger operand = new TitanInteger();
				operand.set_param(param.get_operand1());
				operator_assign(operand.sub());
				break;
			}
			case EXPR_ADD: {
				final TitanInteger operand1 = new TitanInteger();
				final TitanInteger operand2 = new TitanInteger();
				operand1.set_param(param.get_operand1());
				operand2.set_param(param.get_operand2());
				operator_assign(operand1.add(operand2));
				break;
			}
			case EXPR_SUBTRACT: {
				final TitanInteger operand1 = new TitanInteger();
				final TitanInteger operand2 = new TitanInteger();
				operand1.set_param(param.get_operand1());
				operand2.set_param(param.get_operand2());
				operator_assign(operand1.sub(operand2));
				break;
			}
			case EXPR_MULTIPLY: {
				final TitanInteger operand1 = new TitanInteger();
				final TitanInteger operand2 = new TitanInteger();
				operand1.set_param(param.get_operand1());
				operand2.set_param(param.get_operand2());
				operator_assign(operand1.mul(operand2));
				break;
			}
			case EXPR_DIVIDE: {
				final TitanInteger operand1 = new TitanInteger();
				final TitanInteger operand2 = new TitanInteger();
				operand1.set_param(param.get_operand1());
				operand2.set_param(param.get_operand2());
				if (operand2.operator_equals(0)) {
					param.error("Integer division by zero.");
				}
				operator_assign(operand1.div(operand2));
				break;
			}
			default:
				param.expr_type_error("an integer");
				break;
			}
			break;
		default:
			param.type_error("integer template");
		}
		is_ifPresent = param.get_ifpresent() || param.get_ifpresent();
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

		throw new TtcnError(MessageFormat.format("Restriction `{0}'' on template of type {1} violated.", get_res_name(restriction), name == null ? "integer" : name));
	}
}
