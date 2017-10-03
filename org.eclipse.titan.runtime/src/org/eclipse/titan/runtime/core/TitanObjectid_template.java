/******************************************************************************
 * Copyright (c) 2000-2017 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * objid template
 * 
 * @author Gergo Ujhelyi
 *
 */
public class TitanObjectid_template extends Base_Template {

	private TitanObjectid single_value;

	private List<TitanObjectid_template> value_list;

	public TitanObjectid_template() {

	}

	public TitanObjectid_template(final template_sel otherValue) {
		super(otherValue);

		checkSingleSelection(otherValue);
	}

	public TitanObjectid_template(final TitanObjectid otherValue) {
		super(template_sel.SPECIFIC_VALUE);

		single_value = new TitanObjectid(otherValue);
	}

	public TitanObjectid_template(final TitanObjectid_template otherValue) {

		copyTemplate(otherValue);
	}

	public void cleanUp() {
		if (templateSelection == template_sel.VALUE_LIST || templateSelection == template_sel.COMPLEMENTED_LIST) {
			value_list = null;
		} else {
			templateSelection = template_sel.UNINITIALIZED_TEMPLATE;
		}
	}

	private void copyTemplate(final TitanObjectid_template otherValue) {
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
			value_list = new ArrayList<TitanObjectid_template>(otherValue.value_list.size());
			for (int i = 0; i < otherValue.value_list.size(); i++) {
				final TitanObjectid_template temp = new TitanObjectid_template(otherValue.value_list.get(i));
				value_list.add(temp);
			}
			break;
		default:
			throw new TtcnError("Copying an uninitialized/unsupported objid template.");
		}

		setSelection(otherValue);
	}

	// originally operator=
	public TitanObjectid_template assign(final template_sel otherValue) {
		checkSingleSelection(otherValue);
		cleanUp();
		setSelection(otherValue);

		return this;
	}

	// originally operator=
	public TitanObjectid_template assign(final TitanObjectid otherValue) {
		if (!otherValue.isBound().getValue()) {
			throw new TtcnError("Assignment of an unbound objid value to a template.");
		}
		cleanUp();
		setSelection(template_sel.SPECIFIC_VALUE);
		single_value = new TitanObjectid(otherValue);

		return this;
	}

	// originally operator=
	public TitanObjectid_template assign(final TitanObjectid_template otherValue) {
		if (otherValue != this) {
			cleanUp();
			copyTemplate(otherValue);
		}

		return this;
	}

	// originally operator=
	@Override
	public Base_Template assign(Base_Type otherValue) {
		if (otherValue instanceof TitanObjectid) {
			return assign(otherValue);
		}
		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to objid", otherValue));
	}

	// originally operator=
	@Override
	public Base_Template assign(Base_Template otherValue) {
		if (otherValue instanceof TitanObjectid_template) {
			return assign(otherValue);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to objid", otherValue));
	}

	public TitanBoolean match(final TitanObjectid otherValue, final boolean legacy) {
		if (!otherValue.isBound().getValue()) {
			return new TitanBoolean(false);
		}

		switch (templateSelection) {
		case SPECIFIC_VALUE:
			return single_value.operatorEquals(otherValue);
		case OMIT_VALUE:
			return new TitanBoolean(false);
		case ANY_VALUE:
		case ANY_OR_OMIT:
			return new TitanBoolean(true);
		case VALUE_LIST:
		case COMPLEMENTED_LIST:
			for (int i = 0; i < value_list.size(); i++) {
				if (value_list.get(i).match(otherValue, legacy).getValue()) {
					return new TitanBoolean(templateSelection == template_sel.VALUE_LIST);
				}
			}
			return new TitanBoolean(templateSelection == template_sel.COMPLEMENTED_LIST);
		default:
			throw new TtcnError("Matching with an uninitialized/unsupported objid template.");
		}
	}

	public TitanBoolean match(final TitanObjectid otherValue) {
		return match(otherValue, false);
	}

	@Override
	public TitanBoolean match(Base_Type otherValue, boolean legacy) {
		if (otherValue instanceof TitanObjectid) {
			return match((TitanObjectid) otherValue, legacy);
		}
		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to objid", otherValue));
	}

	@Override
	public Base_Type valueOf() {
		if (templateSelection != template_sel.SPECIFIC_VALUE || is_ifPresent) {
			throw new TtcnError("Performing a valueof or send operation on a non-specific objid template.");
		}

		return single_value;
	}

	public TitanInteger sizeOf() {
		switch (templateSelection) {
		case SPECIFIC_VALUE:
			return single_value.sizeOf();
		case OMIT_VALUE:
			throw new TtcnError("Performing sizeof() operation on an objid template containing omit value.");
		case ANY_VALUE:
		case ANY_OR_OMIT:
			throw new TtcnError("Performing sizeof() operation on a */? objid template.");
		case VALUE_LIST: {
			if (value_list.size() < 1) {
				throw new TtcnError("Internal error: Performing sizeof() operation on an objid template containing an empty list.");
			}
			TitanInteger item_size = value_list.get(0).sizeOf();
			for (int i = 1; i < value_list.size(); i++) {
				if (!value_list.get(i).sizeOf().operatorEquals(item_size).getValue()) {
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

	public void setType(final template_sel template_type) {
		setType(template_type, 0);
	}

	public void setType(final template_sel template_type, final int list_length) {
		if (template_type != template_sel.VALUE_LIST && template_type != template_sel.COMPLEMENTED_LIST) {
			throw new TtcnError("Setting an invalid list type for an objid template.");
		}
		cleanUp();
		setSelection(template_type);
		value_list = new ArrayList<TitanObjectid_template>(list_length);
		for (int i = 0; i < list_length; i++) {
			value_list.add(new TitanObjectid_template());
		}
	}

	public TitanObjectid_template listItem(final int list_index) {
		if (templateSelection != template_sel.VALUE_LIST && templateSelection != template_sel.COMPLEMENTED_LIST) {
			throw new TtcnError("Accessing a list element of a non-list objid template.");
		}
		if (list_index >= value_list.size()) {
			throw new TtcnError("Index overflow in an objid value list template.");
		}
		return value_list.get(list_index);
	}

	public void log() {
		switch (templateSelection) {
		case SPECIFIC_VALUE:
			single_value.log();
			break;
		case COMPLEMENTED_LIST:
			TtcnLogger.log_event_str("complement ");
			// no break;
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

	public void logMatch(final TitanObjectid match_value, boolean legacy) {
		if (TtcnLogger.matching_verbosity_t.VERBOSITY_COMPACT == TtcnLogger.get_matching_verbosity()
				&& TtcnLogger.get_logmatch_buffer_len() != 0) {
			TtcnLogger.print_logmatch_buffer();
			TtcnLogger.log_event_str(" := ");
		}
		match_value.log();
		TtcnLogger.log_event_str(" with ");
		log();
		if (match(match_value).getValue()) {
			TtcnLogger.log_event_str(" matched");
		} else {
			TtcnLogger.log_event_str(" unmatched");
		}
	}

	public TitanBoolean isPresent() {
		return isPresent(false);
	}

	public TitanBoolean isPresent(final boolean legacy) {
		if (templateSelection == template_sel.UNINITIALIZED_TEMPLATE) {
			return new TitanBoolean(false);
		} else {
			return matchOmit(legacy).not();
		}
	}

	public TitanBoolean matchOmit(boolean legacy) {
		if (is_ifPresent) {
			return new TitanBoolean(true);
		}
		switch (templateSelection) {
		case OMIT_VALUE:
		case ANY_OR_OMIT:
			return new TitanBoolean(true);
		case VALUE_LIST:
		case COMPLEMENTED_LIST:
			if (legacy) {
				// legacy behavior: 'omit' can appear in the
				// value/complement list
				for (int i = 0; i < value_list.size(); i++) {
					if (value_list.get(i).matchOmit().getValue()) {
						return new TitanBoolean(templateSelection == template_sel.VALUE_LIST);
					}
				}
				return new TitanBoolean(templateSelection == template_sel.COMPLEMENTED_LIST);
			}
			// else fall through
		default:
			return new TitanBoolean(false);
		}
	}

	public TitanBoolean matchOmit() {
		return matchOmit(false);
	}
}
