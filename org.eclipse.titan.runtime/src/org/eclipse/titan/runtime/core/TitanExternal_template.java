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
import java.util.List;

/**
 * Part of the representation of the ASN.1 EXTERNAL type
 *
 * @author Kristof Szabados
 */
public class TitanExternal_template extends Base_Template {

	private TitanExternal_identification_template identification; //ASN1_Choice_Type
	private TitanUniversalCharString_template data__value__descriptor; //ObjectDescriptor_Type
	private TitanOctetString_template data__value; //OctetString_Type
	//originally value_list/list_value
	List<TitanExternal_template> list_value;

	public TitanExternal_identification_template getIdentification() {
		setSpecific();
		return identification;
	}

	public TitanExternal_identification_template constGetIdentification() {
		if (templateSelection != template_sel.SPECIFIC_VALUE) {
			throw new TtcnError("Accessing field identification of a non-specific template of type EXTERNAL.");
		}
		return identification;
	}

	public TitanUniversalCharString_template getData__value__descriptor() {
		setSpecific();
		return data__value__descriptor;
	}

	public TitanUniversalCharString_template constGetData__value__descriptor() {
		if (templateSelection != template_sel.SPECIFIC_VALUE) {
			throw new TtcnError("Accessing field data-value-descriptor of a non-specific template of type EXTERNAL.");
		}
		return data__value__descriptor;
	}

	public TitanOctetString_template getData__value() {
		setSpecific();
		return data__value;
	}

	public TitanOctetString_template constGetData__value() {
		if (templateSelection != template_sel.SPECIFIC_VALUE) {
			throw new TtcnError("Accessing field data-value of a non-specific template of type EXTERNAL.");
		}
		return data__value;
	}

	private void setSpecific() {
		if (templateSelection != template_sel.SPECIFIC_VALUE) {
			final template_sel old_selection = templateSelection;
			cleanUp();
			setSelection(template_sel.SPECIFIC_VALUE);
			identification = new TitanExternal_identification_template();
			data__value__descriptor = new TitanUniversalCharString_template();
			data__value = new TitanOctetString_template();
			if (old_selection == template_sel.ANY_VALUE || old_selection == template_sel.ANY_OR_OMIT) {
				identification.assign(template_sel.ANY_VALUE);
				data__value__descriptor.assign(template_sel.ANY_OR_OMIT);
				data__value.assign(template_sel.ANY_VALUE);
			}
		}
	}

	public TitanExternal_template() {
	}

	public TitanExternal_template(final template_sel other_value ) {
		super( other_value );
		checkSingleSelection( other_value );
	}

	public TitanExternal_template( final TitanExternal otherValue ) {
		copyValue(otherValue);
	}

	public TitanExternal_template( final TitanExternal_template otherValue ) {
		copyTemplate( otherValue );
	}

	public TitanExternal_template( final Optional<TitanExternal> other_value ) {
		switch (other_value.getSelection()) {
		case OPTIONAL_PRESENT:
			copyValue(other_value.constGet());
			break;
		case OPTIONAL_OMIT:
			setSelection(template_sel.OMIT_VALUE);
			break;
		default:
			throw new TtcnError("Creating a template of type EXTERNAL from an unbound optional field.");
		}
	}

	//originally operator=
	public TitanExternal_template assign( final template_sel other_value ) {
		checkSingleSelection(other_value);
		cleanUp();
		setSelection(other_value);
		return this;
	}

	//originally operator=
	public TitanExternal_template assign( final TitanExternal other_value ) {
		cleanUp();
		copyValue(other_value);
		return this;
	}

	//originally operator=
	public TitanExternal_template assign( final TitanExternal_template other_value ) {
		if (other_value != this) {
			cleanUp();
			copyTemplate(other_value);
		}
		return this;
	}

	@Override
	public TitanExternal_template assign(final Base_Type otherValue) {
		if (otherValue instanceof TitanExternal) {
			return assign((TitanExternal) otherValue);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `TitanExternal' can not be cast to {1}", otherValue));
	}

	@Override
	public TitanExternal_template assign(final Base_Template otherValue) {
		if (otherValue instanceof TitanExternal_template) {
			return assign((TitanExternal_template) otherValue);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `TitanExternal' can not be cast to {1}_template", otherValue));
	}

	public TitanExternal_template assign( final Optional<TitanExternal> other_value ) {
		cleanUp();
		switch (other_value.getSelection()) {
		case OPTIONAL_PRESENT:
			copyValue(other_value.constGet());
			break;
		case OPTIONAL_OMIT:
			setSelection(template_sel.OMIT_VALUE);
			break;
		default:
			throw new TtcnError("Assignment of an unbound optional field to a template of type EXTERNAL.");
		}
		return this;
	}

	private void copyValue(final TitanExternal other_value) {
		if (other_value.getIdentification().isBound()) {
			getIdentification().assign(other_value.getIdentification());
		} else {
			getIdentification().cleanUp();
		}
		if (other_value.getData__value__descriptor().isBound()) {
			if (other_value.getData__value__descriptor().isPresent()) {
				getData__value__descriptor().assign(other_value.getData__value__descriptor().get());
			} else {
				getData__value__descriptor().assign(template_sel.OMIT_VALUE);
			}
		} else {
			getData__value__descriptor().cleanUp();
		}
		if (other_value.getData__value().isBound()) {
			getData__value().assign(other_value.getData__value());
		} else {
			getData__value().cleanUp();
		}
		setSelection(template_sel.SPECIFIC_VALUE);
	}

	private void copyTemplate(final TitanExternal_template other_value) {
		switch (other_value.templateSelection) {
		case SPECIFIC_VALUE:
			if (template_sel.UNINITIALIZED_TEMPLATE == other_value.getIdentification().getSelection()) {
				getIdentification().cleanUp();
			} else {
				getIdentification().assign(other_value.getIdentification());
			}
			if (template_sel.UNINITIALIZED_TEMPLATE == other_value.getData__value__descriptor().getSelection()) {
				getData__value__descriptor().cleanUp();
			} else {
				getData__value__descriptor().assign(other_value.getData__value__descriptor());
			}
			if (template_sel.UNINITIALIZED_TEMPLATE == other_value.getData__value().getSelection()) {
				getData__value().cleanUp();
			} else {
				getData__value().assign(other_value.getData__value());
			}
			break;
		case OMIT_VALUE:
		case ANY_VALUE:
		case ANY_OR_OMIT:
			break;
		case VALUE_LIST:
		case COMPLEMENTED_LIST:
			list_value = new ArrayList<TitanExternal_template>(other_value.list_value.size());
			for(int i = 0; i < other_value.list_value.size(); i++) {
				final TitanExternal_template temp = new TitanExternal_template(other_value.list_value.get(i));
				list_value.add(temp);
			}
			break;
		default:
			throw new TtcnError("Copying an uninitialized template of type EXTERNAL.");
		}
		setSelection(other_value);
	}

	public boolean isPresent() {
		return isPresent(false);
	}

	public boolean isPresent(final boolean legacy) {
		return isPresent_(legacy);
	}

	private boolean isPresent_(final boolean legacy) {
		if (templateSelection==template_sel.UNINITIALIZED_TEMPLATE) {
			return false;
		}
		return !match_omit_(legacy);
	}

	public boolean match_omit() {
		return match_omit(false);
	}

	public boolean match_omit(final boolean legacy) {
		return match_omit_(legacy);
	}

	private boolean match_omit_(final boolean legacy) {
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
				for (int l_idx=0; l_idx<list_value.size(); l_idx++) {
					if (list_value.get(l_idx).match_omit_(legacy)) {
						return templateSelection==template_sel.VALUE_LIST;
					}
				}
				return templateSelection==template_sel.COMPLEMENTED_LIST;
			} // else fall through
		default:
			return false;
		}
	}

	public TitanExternal valueOf() {
		if (templateSelection != template_sel.SPECIFIC_VALUE || is_ifPresent) {
			throw new TtcnError("Performing a valueof or send operation on a non-specific template of type EXTERNAL.");
		}
		final TitanExternal ret_val = new TitanExternal();
		if (identification.isBound()) {
			ret_val.identification.assign(identification.valueOf());
		}
		if (data__value__descriptor.isOmit()) {
			ret_val.data__value__descriptor.assign(template_sel.OMIT_VALUE);
		} else if (data__value__descriptor.isBound()) {
			ret_val.data__value__descriptor.assign(data__value__descriptor.valueOf());
		}
		if (data__value.isBound()) {
			ret_val.data__value.assign(data__value.valueOf());
		}
		return ret_val;
	}

	public TitanExternal_template listItem(final int list_index) {
		if (templateSelection != template_sel.VALUE_LIST && templateSelection != template_sel.COMPLEMENTED_LIST) {
			throw new TtcnError("Accessing a list element of a non-list template of type EXTERNAL.");
		}
		if (list_index >= list_value.size()) {
			throw new TtcnError("Index overflow in a value list template of type EXTERNAL.");
		}
		return list_value.get(list_index);
	}

	public void setType(final template_sel template_type, final int list_length) {
		if (template_type != template_sel.VALUE_LIST && template_type != template_sel.COMPLEMENTED_LIST) {
			throw new TtcnError("Setting an invalid list for a template of type EXTERNAL.");
		}
		cleanUp();
		setSelection(template_type);
		list_value = new ArrayList<TitanExternal_template>(list_length);
		for(int i = 0 ; i < list_length; i++) {
			list_value.add(new TitanExternal_template());
		}
	}

	public boolean isBound() {
		if (templateSelection == template_sel.UNINITIALIZED_TEMPLATE && !is_ifPresent) {
			return false;
		}
		if (templateSelection != template_sel.SPECIFIC_VALUE) {
			return true;
		}
		if (identification.isBound()) {
			return true;
		}
		if (data__value__descriptor.isOmit() || data__value__descriptor.isBound()) {
			return true;
		}
		if (data__value.isBound()) {
			return true;
		}
		return false;
	}

	public boolean isValue() {
		if (templateSelection != template_sel.SPECIFIC_VALUE || is_ifPresent) {
			return false;
		}
		if (!identification.isValue()) {
			return false;
		}
		if (!data__value__descriptor.isOmit() && !data__value__descriptor.isValue()) {
			return false;
		}
		if (!data__value.isValue()) {
			return false;
		}
		return true;
	}

	public boolean match(final TitanExternal other_value) {
		return match(other_value, false);
	}

	public boolean match(final TitanExternal other_value, final boolean legacy) {
		return match_(other_value, legacy);
	}

	private boolean match_(final TitanExternal other_value, final boolean legacy) {
		if (!other_value.isBound()) {
			return false;
		}
		switch (templateSelection) {
		case ANY_VALUE:
		case ANY_OR_OMIT:
			return true;
		case OMIT_VALUE:
			return false;
		case SPECIFIC_VALUE:
			if(!other_value.getIdentification().isBound()) {
				return false;
			}
			if(!identification.match(other_value.getIdentification(), legacy)) {
				return false;
			}
			if(!other_value.getData__value__descriptor().isBound()) {
				return false;
			}
			if((other_value.getData__value__descriptor().isPresent() ? !data__value__descriptor.match(other_value.getData__value__descriptor().get(), legacy) : !data__value__descriptor.match_omit(legacy))) {
				return false;
			}
			if(!other_value.getData__value().isBound()) {
				return false;
			}
			if(!data__value.match(other_value.getData__value(), legacy)) {
				return false;
			}
			return true;
		case VALUE_LIST:
		case COMPLEMENTED_LIST:
			for (int list_count = 0; list_count < list_value.size(); list_count++) {
				if (list_value.get(list_count).match(other_value, legacy)) {
					return templateSelection == template_sel.VALUE_LIST;
				}
			}
			return templateSelection == template_sel.COMPLEMENTED_LIST;
		default:
			throw new TtcnError("Matching an uninitialized/unsupported template of type EXTERNAL.");
		}
	}

	@Override
	public boolean match(final Base_Type otherValue, final boolean legacy) {
		if (otherValue instanceof TitanExternal) {
			return match((TitanExternal)otherValue, legacy);
		}

		throw new TtcnError("Internal Error: The left operand of assignment is not of type TitanExternal.");
	}

	public TitanInteger sizeOf() {
		if (is_ifPresent) {
			throw new TtcnError("Performing sizeof() operation on a template of type EXTERNAL which has an ifpresent attribute.");
		}
		switch (templateSelection) {
		case SPECIFIC_VALUE:
			int sizeof = 0;
			if (data__value__descriptor.isPresent()) {
				sizeof++;
			}
			sizeof += 2;
			return new TitanInteger(sizeof);
		case VALUE_LIST:
			if (list_value.isEmpty()) {
				throw new TtcnError("Internal error: Performing sizeof() operation on a template of type EXTERNAL containing an empty list.");
			}
			final int item_size = list_value.get(0).sizeOf().getInt();
			for (int l_idx = 1; l_idx < list_value.size(); l_idx++) {
				if (list_value.get(l_idx).sizeOf().getInt() != item_size) {
					throw new TtcnError("Performing sizeof() operation on a template of type EXTERNAL containing a value list with different sizes.");
				}
			}
			return new TitanInteger(item_size);
		case OMIT_VALUE:
			throw new TtcnError("Performing sizeof() operation on a template of type EXTERNAL containing omit value.");
		case ANY_VALUE:
		case ANY_OR_OMIT:
			throw new TtcnError("Performing sizeof() operation on a template of type EXTERNAL containing */? value.");
		case COMPLEMENTED_LIST:
			throw new TtcnError("Performing sizeof() operation on a template of type EXTERNAL containing complemented list.");
		default:
			throw new TtcnError("Performing sizeof() operation on an uninitialized/unsupported template of type EXTERNAL.");
		}
	}

	public void log() {
		switch (templateSelection) {
		case SPECIFIC_VALUE:
			TtcnLogger.log_char('{');
			TtcnLogger.log_event_str(" identification := ");
			identification.log();
			TtcnLogger.log_char(',');
			TtcnLogger.log_event_str(" data-value-descriptor := ");
			data__value__descriptor.log();
			TtcnLogger.log_char(',');
			TtcnLogger.log_event_str(" data-value := ");
			data__value.log();
			TtcnLogger.log_event_str(" }");
			break;
		case COMPLEMENTED_LIST:
			TtcnLogger.log_event_str("complement");
		case VALUE_LIST:
			TtcnLogger.log_char('(');
			for (int list_count = 0; list_count < list_value.size(); list_count++) {
				if (list_count > 0) {
					TtcnLogger.log_event_str(", ");
				}
				list_value.get(list_count).log();
			}
			TtcnLogger.log_char(')');
			break;
		default:
			log_generic();
			break;
		}
		log_ifpresent();
	}

	public void log_match(final TitanExternal match_value) {
		log_match(match_value, false);
	}

	@Override
	public void log_match(final Base_Type match_value, final boolean legacy) {
		if (match_value instanceof TitanExternal) {
			log_match((TitanExternal)match_value, legacy);
			return;
		}

		throw new TtcnError("Internal Error: value can not be cast to EXTERNAL.");
	}

	public void log_match(final TitanExternal match_value, final boolean legacy) {
		if ( TtcnLogger.matching_verbosity_t.VERBOSITY_COMPACT == TtcnLogger.get_matching_verbosity() ) {
			if(match(match_value, legacy)) {
				TtcnLogger.print_logmatch_buffer();
				TtcnLogger.log_event_str(" matched");
			} else {
				if (templateSelection == template_sel.SPECIFIC_VALUE) {
					final int previous_size = TtcnLogger.get_logmatch_buffer_len();
					if( !identification.match(match_value.constGetIdentification(), legacy) ) {
						TtcnLogger.log_logmatch_info(".identification");
						identification.log_match(match_value.constGetIdentification(), legacy);
						TtcnLogger.set_logmatch_buffer_len(previous_size);
					}
					if( !data__value__descriptor.match(match_value.constGetData__value__descriptor(), legacy) ) {
						TtcnLogger.log_logmatch_info(".data-value-descriptor");
						data__value__descriptor.log_match(match_value.constGetData__value__descriptor(), legacy);
						TtcnLogger.set_logmatch_buffer_len(previous_size);
					}
					if( !data__value.match(match_value.constGetData__value(), legacy) ) {
						TtcnLogger.log_logmatch_info(".data-value");
						data__value.log_match(match_value.constGetData__value(), legacy);
						TtcnLogger.set_logmatch_buffer_len(previous_size);
					}
				} else {
					TtcnLogger.print_logmatch_buffer();
					match_value.log();
					TtcnLogger.log_event_str(" with ");
					log();
					TtcnLogger.log_event_str(" unmatched");
				}
			}
			return;
		}
		if (templateSelection == template_sel.SPECIFIC_VALUE) {
			TtcnLogger.log_event_str("{ identification := ");
			identification.log_match(match_value.constGetIdentification(), legacy);
			TtcnLogger.log_event_str("{ data-value-descriptor := ");
			data__value__descriptor.log_match(match_value.constGetData__value__descriptor(), legacy);
			TtcnLogger.log_event_str("{ data-value := ");
			data__value.log_match(match_value.constGetData__value(), legacy);
			TtcnLogger.log_event_str(" }");
		} else {
			match_value.log();
			TtcnLogger.log_event_str(" with ");
			log();
			if ( match(match_value, legacy) ) {
				TtcnLogger.log_event_str(" matched");
			} else {
				TtcnLogger.log_event_str(" unmatched");
			}
		}
	}

	@Override
	public void encode_text(final Text_Buf text_buf) {
		encode_text_base(text_buf);
		switch (templateSelection) {
		case OMIT_VALUE:
		case ANY_VALUE:
		case ANY_OR_OMIT:
			break;
		case SPECIFIC_VALUE:
			identification.encode_text(text_buf);
			data__value__descriptor.encode_text(text_buf);
			data__value.encode_text(text_buf);
			break;
		case VALUE_LIST:
		case COMPLEMENTED_LIST:
			text_buf.push_int(list_value.size());
			for (int i = 0; i < list_value.size(); i++) {
				list_value.get(i).encode_text(text_buf);
			}
			break;
		default:
			throw new TtcnError("Text encoder: Encoding an uninitialized/unsupported template of type EXTERNAL.");
		}
	}

	@Override
	public void decode_text(final Text_Buf text_buf) {
		cleanUp();
		decode_text_base(text_buf);
		switch (templateSelection) {
		case OMIT_VALUE:
		case ANY_VALUE:
		case ANY_OR_OMIT:
			break;
		case SPECIFIC_VALUE:
			identification.decode_text(text_buf);
			data__value__descriptor.decode_text(text_buf);
			data__value.decode_text(text_buf);
			break;
		case VALUE_LIST:
		case COMPLEMENTED_LIST:
			list_value = new ArrayList<TitanExternal_template>(text_buf.pull_int().getInt());
			for (int i = 0; i < list_value.size(); i++) {
				final TitanExternal_template temp = new TitanExternal_template();
				temp.decode_text(text_buf);
				list_value.add(temp);
			}
			break;
		default:
			throw new TtcnError("Text decoder: An unknown/unsupported selection was received in a template of type EXTERNAL.");
		}
	}
}