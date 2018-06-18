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

/**
 * TTCN-3 float template
 * @author Farkas Izabella Ingrid
 * @author Andrea Pálfi
 *
 * Not yet complete rewrite
 */
public class TitanFloat_template extends Base_Template {
	// int_val part
	// TODO maybe should be renamed in core
	private TitanFloat single_value;

	// value_list part
	private ArrayList<TitanFloat_template> value_list;

	// value range part
	private boolean min_is_present, max_is_present;
	private boolean min_is_exclusive, max_is_exclusive;
	private TitanFloat min_value, max_value;


	public TitanFloat_template() {
		// do nothing
	}

	public TitanFloat_template(final template_sel otherValue) {
		super(otherValue);
		checkSingleSelection(otherValue);
	}

	public TitanFloat_template(final double otherValue) {
		super(template_sel.SPECIFIC_VALUE);
		single_value = new TitanFloat(otherValue);
	}

	public TitanFloat_template(final Ttcn3Float otherValue) {
		super(template_sel.SPECIFIC_VALUE);
		single_value = new TitanFloat(otherValue);
	}

	public TitanFloat_template(final TitanFloat otherValue) {
		super(template_sel.SPECIFIC_VALUE);
		otherValue.mustBound("Creating a template from an unbound float value.");

		single_value = new TitanFloat(otherValue);
	}

	public TitanFloat_template(final TitanFloat_template otherValue) {
		copyTemplate(otherValue);
	}

	// originally clean_up
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

	// originally operator=
	public TitanFloat_template assign(final template_sel otherValue) {
		checkSingleSelection(otherValue);
		cleanUp();
		set_selection(otherValue);

		return this;
	}

	// originally operator=
	public TitanFloat_template assign(final double otherValue) {
		cleanUp();
		set_selection(template_sel.SPECIFIC_VALUE);
		single_value = new TitanFloat(otherValue);

		return this;
	}

	// originally operator=
	public TitanFloat_template assign(final Ttcn3Float otherValue) {
		cleanUp();
		set_selection(template_sel.SPECIFIC_VALUE);
		single_value = new TitanFloat(otherValue);

		return this;
	}

	// originally operator=
	public TitanFloat_template assign(final TitanFloat otherValue) {
		otherValue.mustBound("Assignment of an unbound float value to a template.");

		cleanUp();
		set_selection(template_sel.SPECIFIC_VALUE);
		single_value = new TitanFloat(otherValue);

		return this;
	}

	// originally operator=
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

	// originally match
	public boolean match(final TitanFloat otherValue) {
		return match(otherValue, false);
	}

	// originally match
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

	public TitanFloat valueOf() {
		if (templateSelection != template_sel.SPECIFIC_VALUE || is_ifPresent) {
			throw new TtcnError("Performing a valueof or send operation on a non-specific float template.");
		}

		return single_value;
	}

	public void log() {
		switch (templateSelection) {
		case SPECIFIC_VALUE: {
			TitanFloat.log_float(single_value.getValue());
			break;
		}
		case COMPLEMENTED_LIST:
			TtcnLogger.log_event_str("complement");
		case VALUE_LIST:
			TtcnLogger.log_char('(');
			for (int i = 0; i < value_list.size(); i++) {
				if (i > 0) {
					TtcnLogger.log_event_str(", ");
				}
				value_list.get(i).log();
			}
			TtcnLogger.log_char(')');
			break;

		case VALUE_RANGE:
			TtcnLogger.log_char('(');
			if (min_is_exclusive) {
				TtcnLogger.log_char('!');
			}
			if (min_is_present) {
				TitanFloat.log_float(min_value.getValue());
			} else {
				TtcnLogger.log_event_str("-infinity");
			}
			TtcnLogger.log_event_str(" .. ");
			if (max_is_exclusive) {
				TtcnLogger.log_char('!');
			}
			if (max_is_present) {
				TitanFloat.log_float(max_value.getValue());
			} else {
				TtcnLogger.log_event_str("infinity");
			}
			TtcnLogger.log_char(')');
			break;
		default:
			log_generic();
			break;
		}
		log_ifpresent();
	}

	public void log_match(final TitanFloat match_value, final boolean legacy) {
		if (TtcnLogger.matching_verbosity_t.VERBOSITY_COMPACT == TtcnLogger.get_matching_verbosity()
				&& TtcnLogger.get_logmatch_buffer_len() != 0) {
			TtcnLogger.print_logmatch_buffer();
			TtcnLogger.log_event_str(" := ");
		}
		match_value.log();
		TtcnLogger.log_event_str(" with ");
		log();
		if (match(match_value)) {
			TtcnLogger.log_event_str(" matched");
		} else {
			TtcnLogger.log_event_str(" unmatched");
		}
	}

	// originally is_present (with default parameter)
	public boolean isPresent() {
		return isPresent(false);
	}

	// originally is_present
	public boolean isPresent(final boolean legacy) {
		if (templateSelection == template_sel.UNINITIALIZED_TEMPLATE) {
			return false;
		}

		return !match_omit(legacy);
	}

	public boolean match_omit() {
		return match_omit(false);
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
}
