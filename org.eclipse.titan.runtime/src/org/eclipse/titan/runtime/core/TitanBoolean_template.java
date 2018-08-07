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

//TODO: Not yet complete rewrite
/**
 * TTCN-3 boolean template
 *
 * @author Kristof Szabados
 * @author Arpad Lovassy
 * @author Andrea Palfi
 */
public class TitanBoolean_template extends Base_Template {
	// single_value part
	private TitanBoolean single_value;

	// value_list part
	private ArrayList<TitanBoolean_template> value_list;

	public TitanBoolean_template() {
		// do nothing
	}

	public TitanBoolean_template(final template_sel otherValue) {
		super(otherValue);
		checkSingleSelection(otherValue);
	}

	public TitanBoolean_template(final boolean otherValue) {
		super(template_sel.SPECIFIC_VALUE);
		single_value = new TitanBoolean(otherValue);
	}

	public TitanBoolean_template(final TitanBoolean otherValue) {
		super(template_sel.SPECIFIC_VALUE);
		otherValue.mustBound("Creating a template from an unbound boolean value.");

		single_value = new TitanBoolean(otherValue);
	}

	public TitanBoolean_template(final Optional<TitanBoolean> otherValue) {
		switch (otherValue.get_selection()) {
		case OPTIONAL_PRESENT:
			set_selection(template_sel.SPECIFIC_VALUE);
			single_value = new TitanBoolean(otherValue.constGet());
			break;
		case OPTIONAL_OMIT:
			set_selection(template_sel.OMIT_VALUE);
			break;
		case OPTIONAL_UNBOUND:
			throw new TtcnError("Creating a boolean template from an unbound optional field.");
		}
	}

	public TitanBoolean_template(final TitanBoolean_template otherValue) {
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
		default:
			break;
		}
		templateSelection = template_sel.UNINITIALIZED_TEMPLATE;
	}

	@Override
	public TitanBoolean_template assign(final Base_Type otherValue) {
		if (otherValue instanceof TitanBoolean) {
			return assign((TitanBoolean) otherValue);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to boolean", otherValue));
	}

	@Override
	public TitanBoolean_template assign(final Base_Template otherValue) {
		if (otherValue instanceof TitanBoolean_template) {
			return assign((TitanBoolean_template) otherValue);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to boolean", otherValue));
	}

	// originally operator=
	public TitanBoolean_template assign(final template_sel otherValue) {
		checkSingleSelection(otherValue);
		cleanUp();
		set_selection(otherValue);

		return this;
	}

	// originally operator=
	public TitanBoolean_template assign(final boolean otherValue) {
		cleanUp();
		set_selection(template_sel.SPECIFIC_VALUE);
		single_value = new TitanBoolean(otherValue);

		return this;
	}

	// originally operator=
	public TitanBoolean_template assign(final TitanBoolean otherValue) {
		otherValue.mustBound("Assignment of an unbound boolean value to a template.");

		cleanUp();
		set_selection(template_sel.SPECIFIC_VALUE);
		single_value = new TitanBoolean(otherValue);

		return this;
	}

	// originally operator=
	public TitanBoolean_template assign(final TitanBoolean_template otherValue) {
		if (otherValue != this) {
			cleanUp();
			copyTemplate(otherValue);
		}

		return this;
	}

	private void copyTemplate(final TitanBoolean_template otherValue) {
		switch (otherValue.templateSelection) {
		case SPECIFIC_VALUE:
			single_value = new TitanBoolean(otherValue.single_value);
			break;
		case OMIT_VALUE:
		case ANY_VALUE:
		case ANY_OR_OMIT:
			break;
		case VALUE_LIST:
		case COMPLEMENTED_LIST:
			value_list = new ArrayList<TitanBoolean_template>(otherValue.value_list.size());
			for (int i = 0; i < otherValue.value_list.size(); i++) {
				final TitanBoolean_template temp = new TitanBoolean_template(otherValue.value_list.get(i));
				value_list.add(temp);
			}
			break;
		default:
			throw new TtcnError("Copying an uninitialized/unsupported boolean template.");
		}

		set_selection(otherValue);
	}

	public boolean and(final TitanBoolean otherValue) {
		if (templateSelection != template_sel.SPECIFIC_VALUE) {
			throw new TtcnError("And operation of a non specific value template");
		}

		return single_value.and(otherValue);
	}

	public boolean and(final TitanBoolean_template otherTemplate) {
		if (otherTemplate.templateSelection != template_sel.SPECIFIC_VALUE) {
			throw new TtcnError("And operation of a non specific value template argument");
		}

		return and(otherTemplate.single_value);
	}

	public boolean operatorEquals(final TitanBoolean otherValue) {
		if (templateSelection != template_sel.SPECIFIC_VALUE) {
			throw new TtcnError("Equals operation of a non specific value template");
		}

		return single_value.operatorEquals(otherValue);
	}

	@Override
	public boolean match(final Base_Type otherValue, final boolean legacy) {
		if (otherValue instanceof TitanBoolean) {
			return match((TitanBoolean) otherValue, legacy);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to boolean", otherValue));
	}

	@Override
	public void log_match(final Base_Type match_value, final boolean legacy) {
		if (match_value instanceof TitanBoolean) {
			log_match((TitanBoolean) match_value, legacy);
			return;
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to boolean", match_value));
	}

	// match
	public boolean match(final boolean otherValue) {
		return match(otherValue, false);
	}

	public boolean match(final boolean otherValue, final boolean legacy) {
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
		default:
			throw new TtcnError("Matching with an uninitialized/unsupported boolean template.");
		}
	}

	public boolean match(final TitanBoolean otherValue) {
		return match(otherValue, false);
	}

	public boolean match(final TitanBoolean otherValue, final boolean legacy) {
		if (!otherValue.isBound()) {
			return false;
		}

		return match(otherValue.getValue(), legacy);
	}

	// valueof
	public TitanBoolean valueOf() {
		if (templateSelection != template_sel.SPECIFIC_VALUE || is_ifPresent) {
			throw new TtcnError("Performing valueof or send operation on a non-specific boolean template.");
		}

		return single_value;
	}

	// set_type
	public void setType(final template_sel templateType, final int listLength) {
		if (!template_sel.VALUE_LIST.equals(templateType) && !template_sel.COMPLEMENTED_LIST.equals(templateType)) {
			throw new TtcnError("Setting an invalid list type for a boolean template.");
		}

		cleanUp();
		set_selection(templateType);
		value_list = new ArrayList<TitanBoolean_template>(listLength);
		for (int i = 0; i < listLength; i++) {
			value_list.add(new TitanBoolean_template());
		}
	}

	// listItem
	public TitanBoolean_template listItem(final int listIndex) {
		if (!template_sel.VALUE_LIST.equals(templateSelection) && !template_sel.COMPLEMENTED_LIST.equals(templateSelection)) {
			throw new TtcnError("Accessing a list element of a non-list boolean template.");
		}
		if (listIndex < 0) {
			throw new TtcnError("Accessing an boolean value list template using a negative index (" + listIndex + ").");
		}
		if (listIndex > value_list.size()) {
			throw new TtcnError("Index overflow in an boolean value list template.");
		}

		return value_list.get(listIndex);
	}

	public void log() {
		switch (templateSelection) {
		case SPECIFIC_VALUE:
			TtcnLogger.log_event_str(single_value.getValue() ? "true" : "false");
			break;
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
		default:
			log_generic();
			break;
		}
		log_ifpresent();
	}

	public void log_match(final TitanBoolean match_value, final boolean legacy) {
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

	// originally match_omit (with default parameter)
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
				// legacy behavior: 'omit' can appear in the value/complement list
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
			text_buf.push_int(single_value.getValue() ? 1 : 0);
			break;
		case VALUE_LIST:
		case COMPLEMENTED_LIST:
			text_buf.push_int(value_list.size());
			for (int i = 0; i < value_list.size(); i++) {
				value_list.get(i).encode_text(text_buf);
			}
			break;
		default:
			throw new TtcnError("Text encoder: Encoding an uninitialized/unsupported boolean template.");
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
			single_value = new TitanBoolean();
			single_value.decode_text(text_buf);
			break;
		case VALUE_LIST:
		case COMPLEMENTED_LIST: {
			final int size = text_buf.pull_int().getInt();
			value_list = new ArrayList<TitanBoolean_template>(size);
			for (int i = 0; i < size; i++) {
				final TitanBoolean_template temp = new TitanBoolean_template();
				temp.decode_text(text_buf);
				value_list.add(temp);
			}
			break;
		}
		default:
			throw new TtcnError("Text decoder: An unknown/unsupported selection was received for a boolean template.");
		}
	}
}

