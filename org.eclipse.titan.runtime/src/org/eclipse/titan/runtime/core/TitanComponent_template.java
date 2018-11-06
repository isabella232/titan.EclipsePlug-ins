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
 * TTCN-3 component template
 *
 * @author Kristof Szabados
 * @author Andrea Palfi
 */
public class TitanComponent_template extends Base_Template {
	public static final TitanComponent_template any_compref = new TitanComponent_template(template_sel.ANY_VALUE);

	private int single_value;

	// value_list part
	private ArrayList<TitanComponent_template> value_list;

	public TitanComponent_template() {
		//intentionally empty
	}

	public TitanComponent_template(final template_sel otherValue) {
		super(otherValue);
		checkSingleSelection(otherValue);
	}

	public TitanComponent_template(final int otherValue) {
		super(template_sel.SPECIFIC_VALUE);
		single_value = otherValue;
	}

	public TitanComponent_template(final TitanComponent otherValue) {
		super(template_sel.SPECIFIC_VALUE);

		if (otherValue.componentValue == TitanComponent.UNBOUND_COMPREF) {
			throw new TtcnError("Creating a template from an unbound component reference.");
		}

		single_value = otherValue.componentValue;
	}

	public TitanComponent_template(final Optional<TitanComponent> otherValue) {
		switch (otherValue.get_selection()) {
		case OPTIONAL_PRESENT:
			set_selection(template_sel.SPECIFIC_VALUE);
			single_value = otherValue.constGet().componentValue;
			break;
		case OPTIONAL_OMIT:
			set_selection(template_sel.OMIT_VALUE);
			break;
		case OPTIONAL_UNBOUND:
			throw new TtcnError("Creating a component reference template from an unbound optional field.");
		}
	}

	public TitanComponent_template(final TitanComponent_template otherValue) {
		copyTemplate(otherValue);
	}

	@Override
	public void cleanUp() {
		switch (templateSelection) {
		case VALUE_LIST:
		case COMPLEMENTED_LIST:
			value_list.clear();
			value_list = null;
			break;
		default:
			break;
		}
		templateSelection = template_sel.UNINITIALIZED_TEMPLATE;
	}

	//originally copy_template
	private void copyTemplate(final TitanComponent_template otherValue) {
		switch (otherValue.templateSelection) {
		case SPECIFIC_VALUE:
			single_value = otherValue.single_value;
			break;
		case OMIT_VALUE:
		case ANY_VALUE:
		case ANY_OR_OMIT:
			break;
		case VALUE_LIST:
		case COMPLEMENTED_LIST:
			value_list = new ArrayList<TitanComponent_template>(otherValue.value_list.size());
			for (int i = 0; i < otherValue.value_list.size(); i++) {
				final TitanComponent_template temp = new TitanComponent_template(otherValue.value_list.get(i));
				value_list.add(temp);
			}
			break;
		default:
			throw new TtcnError("Copying an uninitialized/unsupported component reference template.");
		}

		set_selection(otherValue);
	}

	// originally operator=
	public TitanComponent_template assign(final template_sel otherValue) {
		checkSingleSelection(otherValue);
		cleanUp();
		set_selection(otherValue);

		return this;
	}

	@Override
	public TitanComponent_template assign(final Base_Type otherValue) {
		if (otherValue instanceof TitanComponent) {
			return assign((TitanComponent)otherValue);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to component reference", otherValue));
	}

	@Override
	public TitanComponent_template assign(final Base_Template otherValue) {
		if (otherValue instanceof TitanComponent_template) {
			return assign((TitanComponent_template)otherValue);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to component reference template", otherValue));
	}

	@Override
	public void log_match(final Base_Type match_value, final boolean legacy) {
		if (match_value instanceof TitanComponent) {
			log_match((TitanComponent) match_value, legacy);
			return;
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to component", match_value));
	}

	// originally operator=
	public TitanComponent_template assign(final int otherValue) {
		cleanUp();
		set_selection(template_sel.SPECIFIC_VALUE);
		single_value = otherValue;

		return this;
	}

	// originally operator=
	public TitanComponent_template assign(final TitanComponent otherValue) {
		otherValue.mustBound("Assignment of an unbound component reference to a template.");

		cleanUp();
		set_selection(template_sel.SPECIFIC_VALUE);
		single_value = otherValue.componentValue;

		return this;
	}

	// originally operator=
	public TitanComponent_template assign(final TitanComponent_template otherValue) {
		if (otherValue != this) {
			cleanUp();
			copyTemplate(otherValue);
		}

		return this;
	}

	@Override
	public boolean match(final Base_Type otherValue,final boolean legacy) {
		if (otherValue instanceof TitanComponent) {
			return match((TitanComponent) otherValue, legacy);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to charstring", otherValue));
	}

	// originally match
	public boolean match(final TitanComponent otherValue) {
		return match(otherValue, false);
	}

	// originally match
	public boolean match(final TitanComponent otherValue, final boolean legacy) {
		if (otherValue.componentValue == TitanComponent.UNBOUND_COMPREF) {
			throw new TtcnError("Matching an unbound component reference with a template.");
		}

		return match(otherValue.componentValue, legacy);
	}

	// originally match
	public boolean match(final int otherValue, final boolean legacy) {
		switch (templateSelection) {
		case SPECIFIC_VALUE:
			return single_value == otherValue;
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
			throw new TtcnError("Matching with an uninitialized/unsupported component reference template.");
		}
	}

	public TitanComponent valueOf() {
		if (templateSelection != template_sel.SPECIFIC_VALUE || is_ifPresent) {
			throw new TtcnError("Performing a valueof or send operation on a non-specific component reference template.");
		}

		return new TitanComponent(single_value);
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

	public void setType(final template_sel template_type, final int list_length) {
		if (template_type != template_sel.VALUE_LIST && template_type != template_sel.COMPLEMENTED_LIST) {
			throw new TtcnError("Setting an invalid list type for a component reference template.");
		}
		cleanUp();
		set_selection(template_type);
		value_list = new ArrayList<TitanComponent_template>(list_length);
	}

	public TitanComponent_template listItem(final int list_index) {
		if (templateSelection != template_sel.VALUE_LIST && templateSelection != template_sel.COMPLEMENTED_LIST) {
			throw new TtcnError("Accessing a list element of a non-list component reference template.");
		}
		if (list_index >= value_list.size()) {
			throw new TtcnError("Index overflow in a component reference value list template.");
		}
		return value_list.get(list_index);
	}

	@Override
	public void log() {
		switch (templateSelection) {
		case SPECIFIC_VALUE:
			switch (single_value) {
			case TitanComponent.NULL_COMPREF:
				TTCN_Logger.log_event_str("null");
				break;
			case TitanComponent.MTC_COMPREF:
				TTCN_Logger.log_event_str("mtc");
				break;
			case TitanComponent.SYSTEM_COMPREF:
				TTCN_Logger.log_event_str("system");
				break;
			default:
				TTCN_Logger.log_event("%d", single_value);
				break;
			}
			break;
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
		default:
			log_generic();
			break;
		}
		log_ifpresent();
	}

	@Override
	public void set_param(final Module_Parameter param) {
		param.basic_check(basic_check_bits_t.BC_TEMPLATE.getValue(), "component reference (integer or null) template");
		switch (param.get_type()) {
		case MP_Omit:
			this.assign(template_sel.OMIT_VALUE);
			break;
		case MP_Any:
			this.assign(template_sel.ANY_VALUE);
			break;
		case MP_AnyOrNone:
			this.assign(template_sel.ANY_OR_OMIT);
			break;
		case MP_List_Template:
		case MP_ComplementList_Template: {
			final TitanComponent_template temp = new TitanComponent_template();
			temp.setType(param.get_type() == type_t.MP_List_Template ? template_sel.VALUE_LIST : template_sel.COMPLEMENTED_LIST, param.get_size());
			for (int i = 0; i < param.get_size(); i++) {
				temp.listItem(i).set_param(param.get_elem(i));
			}
			this.assign(temp);
			break;
		}
		case MP_Integer:
			this.assign(param.get_integer());
			break;
		case MP_Ttcn_Null:
			this.assign(TitanComponent.NULL_COMPREF);
			break;
		case MP_Ttcn_mtc:
			this.assign(TitanComponent.MTC_COMPREF);
			break;
		case MP_Ttcn_system:
			this.assign(TitanComponent.SYSTEM_COMPREF);
			break;
		default:
			param.type_error("component reference (integer or null) template");
		}
		is_ifPresent = param.get_ifpresent();
	}

	public void log_match(final TitanComponent match_value, final boolean legacy) {
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

		switch (templateSelection) {
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
		default:
			throw new TtcnError("Text encoder: Encoding an uninitialized/unsupported component reference template.");
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
			single_value = text_buf.pull_int().getInt();
			break;
		case VALUE_LIST:
		case COMPLEMENTED_LIST: {
			final int size = text_buf.pull_int().getInt();
			value_list = new ArrayList<TitanComponent_template>(size);
			for (int i = 0; i < size; i++) {
				final TitanComponent_template temp = new TitanComponent_template();
				temp.decode_text(text_buf);
				value_list.add(temp);
			}
			break;
		}
		default:
			throw new TtcnError("Text decoder: An unknown/unsupported selection was received for a component reference template.");
		}
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

		throw new TtcnError(MessageFormat.format("Restriction `{0}'' on template of type {1} violated.", getResName(restriction), name == null ? "component reference" : name));
	}
}
