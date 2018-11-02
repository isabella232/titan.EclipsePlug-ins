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
 * TTCN-3 default template
 *
 * @author Kristof Szabados
 * @author Andrea Palfi
 *
 */
public class TitanDefault_template extends Base_Template {
	private Default_Base single_value;

	// value_list part
	private ArrayList<TitanDefault_template> value_list;

	public TitanDefault_template() {
		//intentionally empty
	}

	public TitanDefault_template(final template_sel otherValue) {
		super(otherValue);

		checkSingleSelection(otherValue);
	}

	//originally has component parameter
	public TitanDefault_template(final int otherValue) {
		super(template_sel.SPECIFIC_VALUE);

		if (otherValue != TitanComponent.NULL_COMPREF) {
			throw new TtcnError("Creating a template from an invalid default reference.");
		}

		single_value = null;
	}

	public TitanDefault_template(final Default_Base otherValue) {
		super(template_sel.SPECIFIC_VALUE);

		single_value = otherValue;
	}

	public TitanDefault_template(final TitanDefault otherValue) {
		super(template_sel.SPECIFIC_VALUE);

		if (otherValue.default_ptr == TitanDefault.UNBOUND_DEFAULT) {
			throw new TtcnError("Creating a template from an unbound default reference.");
		}

		single_value = otherValue.default_ptr;
	}

	public TitanDefault_template(final Optional<TitanDefault> otherValue) {
		switch (otherValue.get_selection()) {
		case OPTIONAL_PRESENT:
			set_selection(template_sel.SPECIFIC_VALUE);
			single_value = otherValue.constGet().default_ptr;
			break;
		case OPTIONAL_OMIT:
			set_selection(template_sel.OMIT_VALUE);
			break;
		case OPTIONAL_UNBOUND:
			throw new TtcnError("Creating a default reference template from an unbound optional field.");
		}
	}

	public TitanDefault_template(final TitanDefault_template otherValue) {
		super();

		copyTemplate(otherValue);
	}

	private void copyTemplate(final TitanDefault_template otherValue) {
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
			value_list = new ArrayList<TitanDefault_template>(otherValue.value_list.size());
			for (int i = 0; i < otherValue.value_list.size(); i++) {
				final TitanDefault_template temp = new TitanDefault_template(otherValue.value_list.get(i));
				value_list.add(temp);
			}
			break;
		default:
			throw new TtcnError("Copying an uninitialized/unsupported default reference template.");
		}

		set_selection(otherValue);
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
			break;
		default:
			break;
		}
		templateSelection = template_sel.UNINITIALIZED_TEMPLATE;
	}

	@Override
	public TitanDefault_template assign(final Base_Type otherValue) {
		if (otherValue instanceof TitanDefault) {
			return assign((TitanDefault)otherValue);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to default", otherValue));
	}

	@Override
	public TitanDefault_template assign(final Base_Template otherValue) {
		if (otherValue instanceof TitanDefault_template) {
			return assign((TitanDefault_template)otherValue);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to default", otherValue));
	}

	@Override
	public void log_match(final Base_Type match_value, final boolean legacy) {
		if (match_value instanceof TitanDefault) {
			log_match((TitanDefault) match_value, legacy);
			return;
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to default", match_value));
	}

	//originally operator=
	public TitanDefault_template assign(final template_sel otherValue) {
		checkSingleSelection(otherValue);
		cleanUp();
		set_selection(otherValue);

		return this;
	}

	// originally operator= has component parameter
	public TitanDefault_template assign(final int otherValue) {
		if (otherValue != TitanComponent.NULL_COMPREF) {
			throw new TtcnError("Assignment of an invalid default reference to a template.");
		}

		cleanUp();
		set_selection(template_sel.SPECIFIC_VALUE);
		single_value = null;

		return this;
	}

	// originally operator=
	public TitanDefault_template assign(final Default_Base otherValue) {
		cleanUp();
		set_selection(template_sel.SPECIFIC_VALUE);
		single_value = otherValue;

		return this;
	}

	//originally operator=
	public TitanDefault_template assign(final TitanDefault otherValue) {
		if (otherValue.default_ptr == TitanDefault.UNBOUND_DEFAULT) {
			throw new TtcnError("Assignment of an unbound default reference to a template.");
		}

		cleanUp();
		set_selection(template_sel.SPECIFIC_VALUE);
		single_value = otherValue.default_ptr;

		return this;
	}

	//originally operator=
	public TitanDefault_template assign(final TitanDefault_template otherValue) {
		if (otherValue != this) {
			cleanUp();
			copyTemplate(otherValue);
		}

		return this;
	}

	@Override
	public boolean match(final Base_Type otherValue, final boolean legacy) {
		if (otherValue instanceof TitanDefault) {
			return match((TitanDefault) otherValue, legacy);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to default", otherValue));
	}

	//originally match has component parameter
	public boolean match(final int otherValue) {
		return match(otherValue, false);
	}

	// originally match has component parameter
	public boolean match(final int otherValue, final boolean legacy) {
		if (otherValue == TitanComponent.NULL_COMPREF) {
			return false;
		}

		return match((Default_Base) null);
	}

	// originally match
	public boolean match(final Default_Base otherValue) {
		return match(otherValue, false);
	}

	// originally match
	public boolean match(final Default_Base otherValue, final boolean legacy) {
		if (otherValue == TitanDefault.UNBOUND_DEFAULT) {
			return false;
		}

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
			throw new TtcnError("Matching with an uninitialized/unsupported default reference template.");
		}
	}

	// originally match
	public boolean match(final TitanDefault otherValue) {
		return match(otherValue, false);
	}

	// originally match
	public boolean match(final TitanDefault otherValue, final boolean legacy) {
		if (!otherValue.isBound()) {
			return false;
		}

		return match(otherValue.default_ptr);
	}

	// originally valueof
	public TitanDefault valueOf() {
		if (templateSelection != template_sel.SPECIFIC_VALUE || is_ifPresent) {
			throw new TtcnError("Performing a valueof or send operation on a non-specific default reference template.");
		}

		return new TitanDefault(single_value);
	}

	// originally set_type
	public void setType(final template_sel templateType, final int listLength) {
		if (templateType != template_sel.VALUE_LIST && templateType != template_sel.COMPLEMENTED_LIST) {
			throw new TtcnError("Setting an invalid list type for a default reference template.");
		}

		cleanUp();
		set_selection(templateType);
		value_list = new ArrayList<TitanDefault_template>(listLength);
	}

	// originally list_iem
	public TitanDefault_template listItem(final int listIndex) {
		if (!template_sel.VALUE_LIST.equals(templateSelection) &&
				!template_sel.COMPLEMENTED_LIST.equals(templateSelection)) {
			throw new TtcnError("Accessing a list element of a non-list default reference template.");
		}
		if (listIndex < 0) {
			throw new TtcnError("Accessing an default reference value list template using a negative index (" + listIndex + ").");
		}
		if (listIndex > value_list.size()) {
			throw new TtcnError("Index overflow in an default reference value list template.");
		}

		return value_list.get(listIndex);
	}

	public void log() {
		switch (templateSelection) {
		case SPECIFIC_VALUE:
			TTCN_Default.log(single_value);
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
		param.basic_check(basic_check_bits_t.BC_TEMPLATE.getValue(), "default reference (null) template");
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
		case MP_ComplementList_Template:
			final TitanDefault_template temp = new TitanDefault_template();
			temp.setType(param.get_type() == type_t.MP_List_Template ? template_sel.VALUE_LIST : template_sel.COMPLEMENTED_LIST, param.get_size());
			for (int i = 0; i < param.get_size(); i++) {
				temp.listItem(i).set_param(param.get_elem(i));
			}
			this.assign(temp);
			break;
		default:
			param.type_error("default reference (null) template");
		}
		is_ifPresent = param.get_ifpresent();
	}

	public void log_match(final TitanDefault match_value, final boolean legacy) {
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
		throw new TtcnError("Default reference templates cannot be sent to other test components.");
	}

	@Override
	/** {@inheritDoc} */
	public void decode_text(final Text_Buf text_buf) {
		throw new TtcnError("Default reference templates cannot be received from other test components.");
	}
}
