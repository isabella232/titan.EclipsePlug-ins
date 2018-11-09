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
		checkSingleSelection(otherValue);
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
		otherValue.mustBound("Creating a template from an unbound float value.");

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
		copyTemplate(otherValue);
	}

	@Override
	public void cleanUp() {
		switch (templateSelection) {
		case SPECIFIC_VALUE:
			single_value = null;
			break;
		case VALUE_LIST:
		case COMPLEMENTED_LIST:
			value_list.clear();
			value_list = null;
		case VALUE_RANGE:
			min_value = null;
			max_value = null;
		default:
			break;
		}
		templateSelection = template_sel.UNINITIALIZED_TEMPLATE;
	}

	@Override
	public TitanFloat_template assign(final Base_Type otherValue) {
		if (otherValue instanceof TitanFloat) {
			return assign((TitanFloat) otherValue);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to float", otherValue));
	}

	@Override
	public TitanFloat_template assign(final Base_Template otherValue) {
		if (otherValue instanceof TitanFloat_template) {
			return assign((TitanFloat_template) otherValue);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to float template", otherValue));
	}

	@Override
	public TitanFloat_template assign(final template_sel otherValue) {
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
	public TitanFloat_template assign(final double otherValue) {
		cleanUp();
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
	public TitanFloat_template assign(final Ttcn3Float otherValue) {
		cleanUp();
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
	public TitanFloat_template assign(final TitanFloat otherValue) {
		otherValue.mustBound("Assignment of an unbound float value to a template.");

		cleanUp();
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
	public TitanFloat_template assign(final TitanFloat_template otherValue) {
		if (otherValue != this) {
			cleanUp();
			copyTemplate(otherValue);
		}

		return this;
	}

	private void copyTemplate(final TitanFloat_template otherValue) {
		switch (otherValue.templateSelection) {
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
		if (!otherValue.isBound()) {
			return false;
		}

		switch (templateSelection) {
		case SPECIFIC_VALUE:
			return single_value.operatorEquals(otherValue);
		case OMIT_VALUE:
			return false;
		case ANY_VALUE:
		case ANY_OR_OMIT:
			return true;
		case VALUE_LIST:
		case COMPLEMENTED_LIST:
			for (int i = 0; i < value_list.size(); i++) {
				if (value_list.get(i).match(otherValue, legacy)) {
					return templateSelection == template_sel.VALUE_LIST;
				}
			}
			return templateSelection == template_sel.COMPLEMENTED_LIST;
		case VALUE_RANGE: {
			boolean lowerMatch = false;
			boolean upperMatch = false;
			if (min_is_present) {
				if (!min_is_exclusive && min_value.isLessThanOrEqual(otherValue)) {
					lowerMatch = true;
				} else if (min_is_exclusive && min_value.isLessThan(otherValue)) {
					lowerMatch = true;
				}
			} else if (!min_is_exclusive || otherValue.isGreaterThan(Double.NEGATIVE_INFINITY)) {
				lowerMatch = true;
			}
			if (max_is_present) {
				if (!max_is_exclusive && max_value.isGreaterThanOrEqual(otherValue)) {
					upperMatch = true;
				} else if (max_is_exclusive && max_value.isGreaterThan(otherValue)) {
					upperMatch = true;
				}
			} else if (!max_is_exclusive || otherValue.isLessThan(Double.POSITIVE_INFINITY)) {
				upperMatch = true;
			}

			return lowerMatch && upperMatch;
		}
		default:
			throw new TtcnError("Matching with an uninitialized/unsupported float template.");
		}
	}

	public void setType(final template_sel templateType) {
		setType(templateType, 0);
	}

	public void setType(final template_sel templateType, final int listLength) {
		cleanUp();
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

	public TitanFloat_template listItem(final int listIndex) {
		if (templateSelection != template_sel.VALUE_LIST && templateSelection != template_sel.COMPLEMENTED_LIST) {
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

	public void setMin(final double minValue) {
		if (templateSelection != template_sel.VALUE_RANGE) {
			throw new TtcnError("Float template is not range when setting lower limit.");
		}
		if (max_is_present && min_is_present && max_value.isLessThan(min_value)) {
			throw new TtcnError("The lower limit of the range is greater than the " + "upper limit in a float template.");
		}

		min_is_present = true;
		min_is_exclusive = false;
		min_value = new TitanFloat(minValue);
	}

	public void setMin(final Ttcn3Float minValue) {
		if (templateSelection != template_sel.VALUE_RANGE) {
			throw new TtcnError("Float template is not range when setting lower limit.");
		}
		if (max_is_present && min_is_present && max_value.isLessThan(min_value)) {
			throw new TtcnError("The lower limit of the range is greater than the " + "upper limit in a float template.");
		}

		min_is_present = true;
		min_is_exclusive = false;
		min_value = new TitanFloat(minValue);
	}

	public void setMin(final TitanFloat minValue) {
		minValue.mustBound("Using an unbound value when setting the lower bound " + "in a float range template.");

		setMin(minValue.getValue());
	}

	public void setMax(final double maxValue) {
		if (templateSelection != template_sel.VALUE_RANGE) {
			throw new TtcnError("Float template is not range when setting upper limit.");
		}
		if (min_is_present && max_is_present && min_value.isGreaterThan(max_value)) {
			throw new TtcnError("The upper limit of the range is smaller than the " + "lower limit in a float template.");
		}

		max_is_present = true;
		max_is_exclusive = false;
		max_value = new TitanFloat(maxValue);
	}

	public void setMax(final Ttcn3Float maxValue) {
		if (templateSelection != template_sel.VALUE_RANGE) {
			throw new TtcnError("Float template is not range when setting upper limit.");
		}
		if (min_is_present && max_is_present && min_value.isGreaterThan(max_value)) {
			throw new TtcnError("The upper limit of the range is smaller than the " + "lower limit in a float template.");
		}

		max_is_present = true;
		max_is_exclusive = false;
		max_value = new TitanFloat(maxValue);
	}

	public void setMax(final TitanFloat maxValue) {
		maxValue.mustBound("Using an unbound value when setting the upper bound " + "in a float range template.");

		setMax(maxValue.getValue());
	}

	public void setMinExclusive(final boolean minExclusive) {
		if (templateSelection != template_sel.VALUE_RANGE) {
			throw new TtcnError("Float template is not range when setting lower limit exclusiveness.");
		}

		min_is_exclusive = minExclusive;
	}

	public void setMaxExclusive(final boolean maxExclusive) {
		if (templateSelection != template_sel.VALUE_RANGE) {
			throw new TtcnError("Float template is not range when setting upper limit exclusiveness.");
		}

		max_is_exclusive = maxExclusive;
	}

	@Override
	public TitanFloat valueOf() {
		if (templateSelection != template_sel.SPECIFIC_VALUE || is_ifPresent) {
			throw new TtcnError("Performing a valueof or send operation on a non-specific float template.");
		}

		return single_value;
	}

	@Override
	public void log() {
		switch (templateSelection) {
		case SPECIFIC_VALUE: {
			TitanFloat.log_float(single_value.getValue());
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
				TitanFloat.log_float(min_value.getValue());
			} else {
				TTCN_Logger.log_event_str("-infinity");
			}
			TTCN_Logger.log_event_str(" .. ");
			if (max_is_exclusive) {
				TTCN_Logger.log_char('!');
			}
			if (max_is_present) {
				TitanFloat.log_float(max_value.getValue());
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

	public boolean match_omit(final boolean legacy) {
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
				for (int i = 0; i < value_list.size(); i++) {
					if (value_list.get(i).match_omit()) {
						return templateSelection == template_sel.VALUE_LIST;
					}
				}
				return templateSelection == template_sel.COMPLEMENTED_LIST;
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

		switch (templateSelection) {
		case OMIT_VALUE:
		case ANY_VALUE:
		case ANY_OR_OMIT:
			break;
		case SPECIFIC_VALUE:
			text_buf.push_double(single_value.getValue());
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
				text_buf.push_double(min_value.getValue());
			}
			text_buf.push_int(max_is_present ? 1 : 0);
			if (max_is_present) {
				text_buf.push_double(max_value.getValue());
			}
			break;
		default:
			throw new TtcnError("Text encoder: Encoding an uninitialized/unsupported float template.");
		}
	}

	@Override
	/** {@inheritDoc} */
	public void decode_text(final Text_Buf text_buf) {
		cleanUp();
		decode_text_base(text_buf);

		switch (templateSelection) {
		case OMIT_VALUE:
		case ANY_VALUE:
		case ANY_OR_OMIT:
			break;
		case SPECIFIC_VALUE:
			single_value = new TitanFloat(text_buf.pull_double());
			break;
		case VALUE_LIST:
		case COMPLEMENTED_LIST: {
			final int size = text_buf.pull_int().getInt();
			value_list = new ArrayList<TitanFloat_template>(size);
			for (int i = 0; i < size; i++) {
				final TitanFloat_template temp = new TitanFloat_template();
				temp.decode_text(text_buf);
				value_list.add(temp);
			}
			break;
		}
		case VALUE_RANGE:
			min_is_present = text_buf.pull_int().getInt() != 0;
			if (min_is_present) {
				min_value = new TitanFloat(text_buf.pull_double());
			}
			max_is_present = text_buf.pull_int().getInt() != 0;
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
	public void set_param(final Module_Parameter param) {
		param.basic_check(basic_check_bits_t.BC_TEMPLATE.getValue(), "float template");
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
			final TitanFloat_template temp = new TitanFloat_template();
			temp.setType(param.get_type() == type_t.MP_List_Template ?
					template_sel.VALUE_LIST : template_sel.COMPLEMENTED_LIST, param.get_size());
			for (int i = 0; i < param.get_size(); i++) {
				temp.listItem(i).set_param(param.get_elem(i));
			}
			assign(temp);
			break;
		}
		case MP_Float:
			assign(param.get_float());
			break;
		case MP_FloatRange: {
			setType(template_sel.VALUE_RANGE);
			if (param.has_lower_float()) {
				setMin(param.get_lower_float());
			}
			setMinExclusive(param.get_is_min_exclusive());
			if (param.has_upper_float()) {
				setMax(param.get_upper_float());
			}
			setMaxExclusive(param.get_is_max_exclusive());
			break;
		}
		case MP_Expression:
			switch (param.get_expr_type()) {
			case EXPR_NEGATE: {
				final TitanFloat operand = new TitanFloat();
				operand.set_param(param.get_operand1());
				assign(operand.sub());
				break;
			}
			case EXPR_ADD: {
				final TitanFloat operand1 = new TitanFloat();
				final TitanFloat operand2 = new TitanFloat();
				operand1.set_param(param.get_operand1());
				operand2.set_param(param.get_operand2());
				assign(operand1.add(operand2));
				break;
			}
			case EXPR_SUBTRACT: {
				final TitanFloat operand1 = new TitanFloat();
				final TitanFloat operand2 = new TitanFloat();
				operand1.set_param(param.get_operand1());
				operand2.set_param(param.get_operand2());
				assign(operand1.sub(operand2));
				break;
			}
			case EXPR_MULTIPLY: {
				final TitanFloat operand1 = new TitanFloat();
				final TitanFloat operand2 = new TitanFloat();
				operand1.set_param(param.get_operand1());
				operand2.set_param(param.get_operand2());
				assign(operand1.mul(operand2));
				break;
			}
			case EXPR_DIVIDE: {
				final TitanFloat operand1 = new TitanFloat();
				final TitanFloat operand2 = new TitanFloat();
				operand1.set_param(param.get_operand1());
				operand2.set_param(param.get_operand2());
				if (operand2.operatorEquals(0)) {
					param.error("Floating point division by zero.");
				}
				assign(operand1.div(operand2));
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
	public void check_restriction(final template_res restriction, final String name, final boolean legacy) {
		if (templateSelection == template_sel.UNINITIALIZED_TEMPLATE) {
			return;
		}

		switch ((name != null && restriction == template_res.TR_VALUE) ? template_res.TR_OMIT : restriction) {
		case TR_VALUE:
			if (!is_ifPresent && templateSelection == template_sel.SPECIFIC_VALUE) {
				return;
			}
			break;
		case TR_OMIT:
			if (!is_ifPresent && (templateSelection == template_sel.OMIT_VALUE || templateSelection == template_sel.SPECIFIC_VALUE)) {
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

		throw new TtcnError(MessageFormat.format("Restriction `{0}'' on template of type {1} violated.", getResName(restriction), name == null ? "float" : name));
	}
}
