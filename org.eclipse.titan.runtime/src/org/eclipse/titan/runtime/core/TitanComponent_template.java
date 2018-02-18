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

/**
 * TTCN-3 component template
 *
 *  //FIXME a lot to implement
 *
 * @author Kristof Szabados
 * @author Andrea Pálfi
 */
public class TitanComponent_template extends Base_Template {
	public static final TitanComponent_template any_compref = new TitanComponent_template(template_sel.ANY_VALUE);

	// TODO maybe should be renamed in core
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

	public TitanComponent_template(final TitanComponent_template otherValue) {
		copyTemplate(otherValue);
	}

	//originally clean_up
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

		setSelection(otherValue);
	}

	// originally operator=
	public TitanComponent_template assign(final template_sel otherValue) {
		checkSingleSelection(otherValue);
		cleanUp();
		setSelection(otherValue);

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
		setSelection(template_sel.SPECIFIC_VALUE);
		single_value = otherValue;

		return this;
	}

	// originally operator=
	public TitanComponent_template assign(final TitanComponent otherValue) {
		otherValue.mustBound("Assignment of an unbound component reference to a template.");

		cleanUp();
		setSelection(template_sel.SPECIFIC_VALUE);
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

	public void log() {
		switch (templateSelection) {
		case SPECIFIC_VALUE:
			switch (single_value) {
			case TitanComponent.NULL_COMPREF:
				TtcnLogger.log_event_str("null");
				break;
			case TitanComponent.MTC_COMPREF:
				TtcnLogger.log_event_str("mtc");
				break;
			case TitanComponent.SYSTEM_COMPREF:
				TtcnLogger.log_event_str("system");
				break;
			default:
				TtcnLogger.log_event("%d", single_value);
				break;
			}
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

	public void log_match(final TitanComponent match_value, final boolean legacy) {
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
		case COMPLEMENTED_LIST:
			value_list = new ArrayList<TitanComponent_template>(text_buf.pull_int().getInt());
			for (int i = 0; i < value_list.size(); i++) {
				final TitanComponent_template temp = new TitanComponent_template();
				temp.decode_text(text_buf);
				value_list.add(temp);
			}
			break;
		default:
			throw new TtcnError("Text decoder: An unknown/unsupported selection was received for a component reference template.");
		}
	}
}
