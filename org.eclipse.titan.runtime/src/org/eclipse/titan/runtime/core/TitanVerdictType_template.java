/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

import java.text.MessageFormat;
import java.util.ArrayList;

import org.eclipse.titan.runtime.core.TitanVerdictType.VerdictTypeEnum;

/**
 * TTCN-3 verdict type template
 * @author Arpad Lovassy
 * @author Andrea Pálfi
 */
public class TitanVerdictType_template extends Base_Template {

	private TitanVerdictType single_value;

	// value_list part
	private ArrayList<TitanVerdictType_template> value_list;

	public TitanVerdictType_template() {
		//do nothing
	}

	public TitanVerdictType_template(final template_sel otherValue) {
		super(otherValue);
		checkSingleSelection(otherValue);
	}

	public TitanVerdictType_template(final VerdictTypeEnum otherValue) {
		super(template_sel.SPECIFIC_VALUE);
		if (!TitanVerdictType.isValid(otherValue)) {
			throw new TtcnError("Creating a template from an invalid verdict value (" + otherValue + ").");
		}

		single_value = new TitanVerdictType(otherValue);
	}

	public TitanVerdictType_template(final TitanVerdictType otherValue) {
		super(template_sel.SPECIFIC_VALUE);
		copyValue(otherValue);
	}

	public TitanVerdictType_template(final TitanVerdictType_template other_value) {
		super();
		copyTemplate(other_value);
	}

	public TitanVerdictType_template(final Optional<TitanVerdictType> other_value) {
		switch (other_value.getSelection()) {
		case OPTIONAL_PRESENT:
			copyValue(other_value.get());
			break;
		case OPTIONAL_OMIT:
			setSelection(template_sel.OMIT_VALUE);
			break;
		default:
			throw new TtcnError("Creating a verdict template from an unbound optional field.");
		}
	}

	//originally clean_up
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
	public TitanVerdictType_template assign(final Base_Type otherValue) {
		if (otherValue instanceof TitanVerdictType) {
			return assign((TitanVerdictType)otherValue);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to verdict type", otherValue));
	}

	@Override
	public TitanVerdictType_template assign(final Base_Template otherValue) {
		if (otherValue instanceof TitanVerdictType_template) {
			return assign((TitanVerdictType_template)otherValue);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to verdict type template", otherValue));
	}

	//originally operator=
	public TitanVerdictType_template assign(final template_sel otherValue) {
		checkSingleSelection(otherValue);
		cleanUp();
		setSelection(otherValue);

		return this;
	}

	// originally operator=
	public TitanVerdictType_template assign(final VerdictTypeEnum otherValue) {
		if (!TitanVerdictType.isValid(otherValue)) {
			throw new TtcnError("Assignment of an invalid verdict value (" + otherValue + ") to a template.");
		}

		cleanUp();
		setSelection(template_sel.SPECIFIC_VALUE);
		single_value = new TitanVerdictType(otherValue);

		return this;
	}

	// originally operator=
	public TitanVerdictType_template assign(final TitanVerdictType otherValue) {
		otherValue.mustBound("Assignment of an unbound verdict value to a template.");

		cleanUp();
		setSelection(template_sel.SPECIFIC_VALUE);
		copyValue(otherValue);

		return this;
	}

	// originally operator=
	public TitanVerdictType_template assign(final Optional<TitanVerdictType> otherValue) {
		cleanUp();
		switch (otherValue.getSelection()) {
		case OPTIONAL_PRESENT:
			copyValue(otherValue.get());
			break;
		case OPTIONAL_OMIT:
			setSelection(template_sel.OMIT_VALUE);
			break;
		default:
			throw new TtcnError("Assignment of an unbound optional field to a verdict template.");
		}
		return this;
	}

	//originally operator=
	public TitanVerdictType_template assign(final TitanVerdictType_template otherValue) {
		if (otherValue != this) {
			cleanUp();
			copyTemplate(otherValue);
		}

		return this;
	}

	private void copyValue(final TitanVerdictType otherValue)	{
		otherValue.mustBound("Creating a template from an unbound verdict value.");

		single_value = new TitanVerdictType(otherValue);
		setSelection(template_sel.SPECIFIC_VALUE);
	}

	private void copyTemplate(final TitanVerdictType_template otherValue) {
		switch (otherValue.templateSelection) {
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

		setSelection(otherValue);
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

	// originally match
	public boolean match(final TitanVerdictType otherValue) {
		return match(otherValue, false);
	}

	//originally boolean VERDICTTYPE_template::match(verdicttype other_value, boolean legacy ) const
	public boolean match(final VerdictTypeEnum otherValue, final boolean legacy) {
		if (!TitanVerdictType.isValid(otherValue)) {
			throw new TtcnError("Matching a verdict template with an invalid value (" + otherValue + ").");
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
		default:
			throw new TtcnError("Matching with an uninitialized/unsupported verdict template.");
		}
	}

	//originally boolean VERDICTTYPE_template::match(const VERDICTTYPE& other_value, boolean legacy) const
	public boolean match(final TitanVerdictType other_value, final boolean legacy) {
		if (!other_value.isBound()) {
			return false;
		}

		return match(other_value.getValue(), legacy);
	}

	//originally boolean operator==(verdicttype par_value, const VERDICTTYPE& other_value)
	public boolean operatorEquals(final VerdictTypeEnum par_value, final TitanVerdictType other_value) {
		if (!TitanVerdictType.isValid(par_value)) {
			throw new TtcnError("The left operand of comparison is an invalid verdict value (" + par_value + ").");
		}

		other_value.mustBound("The right operand of comparison is an unbound verdict value.");

		return par_value == other_value.getValue();
	}

	//originally valueof
	public TitanVerdictType valueOf() {
		if (templateSelection != template_sel.SPECIFIC_VALUE || is_ifPresent) {
			throw new TtcnError("Performing a valueof or send operation on a non-specific verdict template.");
		}

		return new TitanVerdictType(single_value);
	}

	public void set_type(final template_sel template_type, final int list_length) {
		if (template_type != template_sel.VALUE_LIST && template_type != template_sel.COMPLEMENTED_LIST) {
			throw new TtcnError("Internal error: Setting an invalid list type for a verdict template.");
		}

		cleanUp();
		setSelection(template_type);
		value_list = new ArrayList<TitanVerdictType_template>(list_length);
	}

	public TitanVerdictType_template listItem(final int listIndex) {
		if (templateSelection != template_sel.VALUE_LIST && templateSelection != template_sel.COMPLEMENTED_LIST) {
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

	public void log() {
		switch (templateSelection) {
		case SPECIFIC_VALUE:
			if (TitanVerdictType.isValid(single_value.getValue())) {
				TtcnLogger.log_event(TitanVerdictType.verdict_name[ single_value.getValue().ordinal() ]);
			} else {
				TtcnLogger.log_event("<unknown verdict value: " + single_value + ">");
			}
			break;
		case COMPLEMENTED_LIST:
			TtcnLogger.log_event_str("complement");
			// no break
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

	public void log_match(final TitanVerdictType match_value, final boolean legacy) {
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

	//TODO: implement VERDICTTYPE_template::set_param()

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
				// legacy behavior: 'omit' can appear in the
				// value/complement list
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

	public void check_restriction(final template_res t_res, final String t_name) {
		check_restriction(t_res, t_name, false);
	}

	/**
	 * originally
	 * #ifndef TITAN_RUNTIME_2
	 * void VERDICTTYPE_template::check_restriction(template_res t_res, const char* t_name, boolean legacy = FALSE ) const
	 */
	public void check_restriction(final template_res t_res, final String t_name, final boolean legacy) {
		if (templateSelection == template_sel.UNINITIALIZED_TEMPLATE) {
			return;
		}

		final template_res res = (t_name != null && (t_res == template_res.TR_VALUE)) ? template_res.TR_OMIT : t_res;
		switch (res) {
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
		throw new TtcnError("Restriction `" + getResName(t_res) + "' on template of type " + t_name != null ? t_name : "verdict" + " violated.");
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
		cleanUp();
		decode_text_base(text_buf);

		switch (templateSelection) {
		case OMIT_VALUE:
		case ANY_VALUE:
		case ANY_OR_OMIT:
			break;
		case SPECIFIC_VALUE:
			single_value.decode_text(text_buf);
			break;
		case VALUE_LIST:
		case COMPLEMENTED_LIST:
			value_list = new ArrayList<TitanVerdictType_template>(text_buf.pull_int().getInt());
			for (int i = 0; i < value_list.size(); i++) {
				final TitanVerdictType_template temp = new TitanVerdictType_template();
				temp.decode_text(text_buf);
				value_list.add(temp);
			}
			break;
		default:
			throw new TtcnError("Text decoder: An unknown/unsupported selection was received for a verdict template.");
		}
	}
}
