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
 * Part of the representation of the ASN.1 unrestricted string (CHARACTER STRING) type.
 * 
 * @author Kristof Szabados
 */
public class TitanCharacter_String_template extends Base_Template {

	private TitanCharacter_String_identification_template identification; //ASN1_Choice_Type
	private TitanUniversalCharString_template data__value__descriptor; //ObjectDescriptor_Type
	private TitanOctetString_template string__value; //OctetString_Type
	//originally value_list/list_value
	List<TitanCharacter_String_template> list_value;

	public TitanCharacter_String_identification_template getIdentification() {
		setSpecific();
		return identification;
	}

	public TitanCharacter_String_identification_template constGetIdentification() {
		if (templateSelection != template_sel.SPECIFIC_VALUE) {
			throw new TtcnError("Accessing field identification of a non-specific template of type CHARACTER STRING.");
		}
		return identification;
	}

	public TitanUniversalCharString_template getData__value__descriptor() {
		setSpecific();
		return data__value__descriptor;
	}

	public TitanUniversalCharString_template constGetData__value__descriptor() {
		if (templateSelection != template_sel.SPECIFIC_VALUE) {
			throw new TtcnError("Accessing field data__value__descriptor of a non-specific template of type CHARACTER STRING.");
		}
		return data__value__descriptor;
	}

	public TitanOctetString_template getString__value() {
		setSpecific();
		return string__value;
	}

	public TitanOctetString_template constGetString__value() {
		if (templateSelection != template_sel.SPECIFIC_VALUE) {
			throw new TtcnError("Accessing field string__value of a non-specific template of type CHARACTER STRING.");
		}
		return string__value;
	}

	private void setSpecific() {
		if (templateSelection != template_sel.SPECIFIC_VALUE) {
			final template_sel old_selection = templateSelection;
			cleanUp();
			setSelection(template_sel.SPECIFIC_VALUE);
			identification = new TitanCharacter_String_identification_template();
			data__value__descriptor = new TitanUniversalCharString_template();
			string__value = new TitanOctetString_template();
			if (old_selection == template_sel.ANY_VALUE || old_selection == template_sel.ANY_OR_OMIT) {
				identification.assign(template_sel.ANY_VALUE);
				data__value__descriptor.assign(template_sel.ANY_OR_OMIT);
				string__value.assign(template_sel.ANY_VALUE);
			}
		}
	}

	public TitanCharacter_String_template() {
	}

	public TitanCharacter_String_template(final template_sel other_value ) {
		super( other_value );
		checkSingleSelection( other_value );
	}

	public TitanCharacter_String_template( final TitanCharacter_String otherValue ) {
		copyValue(otherValue);
	}

	public TitanCharacter_String_template( final TitanCharacter_String_template otherValue ) {
		copyTemplate( otherValue );
	}

	public TitanCharacter_String_template( final Optional<TitanCharacter_String> other_value ) {
		switch (other_value.getSelection()) {
		case OPTIONAL_PRESENT:
			copyValue(other_value.constGet());
			break;
		case OPTIONAL_OMIT:
			setSelection(template_sel.OMIT_VALUE);
			break;
		default:
			throw new TtcnError("Creating a template of type CHARACTER STRING from an unbound optional field.");
		}
	}

	//originally operator=
	public TitanCharacter_String_template assign( final template_sel other_value ) {
		checkSingleSelection(other_value);
		cleanUp();
		setSelection(other_value);
		return this;
	}

	//originally operator=
	public TitanCharacter_String_template assign( final TitanCharacter_String other_value ) {
		cleanUp();
		copyValue(other_value);
		return this;
	}

	//originally operator=
	public TitanCharacter_String_template assign( final TitanCharacter_String_template other_value ) {
		if (other_value != this) {
			cleanUp();
			copyTemplate(other_value);
		}
		return this;
	}

	@Override
	public TitanCharacter_String_template assign(final Base_Type otherValue) {
		if (otherValue instanceof TitanCharacter_String) {
			return assign((TitanCharacter_String) otherValue);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `CHARACTER__STRING' can not be cast to {1}", otherValue));
	}

	@Override
	public TitanCharacter_String_template assign(final Base_Template otherValue) {
		if (otherValue instanceof TitanCharacter_String_template) {
			return assign((TitanCharacter_String_template) otherValue);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `CHARACTER__STRING' can not be cast to {1}_template", otherValue));
	}

	public TitanCharacter_String_template assign( final Optional<TitanCharacter_String> other_value ) {
		cleanUp();
		switch (other_value.getSelection()) {
		case OPTIONAL_PRESENT:
			copyValue(other_value.constGet());
			break;
		case OPTIONAL_OMIT:
			setSelection(template_sel.OMIT_VALUE);
			break;
		default:
			throw new TtcnError("Assignment of an unbound optional field to a template of type CHARACTER STRING.");
		}
		return this;
	}

	private void copyValue(final TitanCharacter_String other_value) {
		if (other_value.getIdentification().isBound().getValue()) {
			getIdentification().assign(other_value.getIdentification());
		} else {
			getIdentification().cleanUp();
		}
		if (other_value.getData__value__descriptor().isBound().getValue()) {
			if (other_value.getData__value__descriptor().isPresent().getValue()) {
				getData__value__descriptor().assign(other_value.getData__value__descriptor().get());
			} else {
				getData__value__descriptor().assign(template_sel.OMIT_VALUE);
			}
		} else {
			getData__value__descriptor().cleanUp();
		}
		if (other_value.getString__value().isBound().getValue()) {
			getString__value().assign(other_value.getString__value());
		} else {
			getString__value().cleanUp();
		}
		setSelection(template_sel.SPECIFIC_VALUE);
	}

	private void copyTemplate(final TitanCharacter_String_template other_value) {
		switch (other_value.templateSelection) {
		case SPECIFIC_VALUE:
			if (template_sel.UNINITIALIZED_TEMPLATE != other_value.getIdentification().getSelection()) {
				getIdentification().assign(other_value.getIdentification());
			} else {
				getIdentification().cleanUp();
			}
			if (template_sel.UNINITIALIZED_TEMPLATE != other_value.getData__value__descriptor().getSelection()) {
				getData__value__descriptor().assign(other_value.getData__value__descriptor());
			} else {
				getData__value__descriptor().cleanUp();
			}
			if (template_sel.UNINITIALIZED_TEMPLATE != other_value.getString__value().getSelection()) {
				getString__value().assign(other_value.getString__value());
			} else {
				getString__value().cleanUp();
			}
			break;
		case OMIT_VALUE:
		case ANY_VALUE:
		case ANY_OR_OMIT:
			break;
		case VALUE_LIST:
		case COMPLEMENTED_LIST:
			list_value = new ArrayList<TitanCharacter_String_template>(other_value.list_value.size());
			for(int i = 0; i < other_value.list_value.size(); i++) {
				final TitanCharacter_String_template temp = new TitanCharacter_String_template(other_value.list_value.get(i));
				list_value.add(temp);
			}
			break;
		default:
			throw new TtcnError("Copying an uninitialized template of type CHARACTER STRING.");
		}
		setSelection(other_value);
	}

	public TitanBoolean isPresent() {
		return isPresent(false);
	}

	public TitanBoolean isPresent(boolean legacy) {
		return new TitanBoolean(isPresent_(legacy));
	}

	private boolean isPresent_(boolean legacy) {
		if (templateSelection==template_sel.UNINITIALIZED_TEMPLATE) {
			return false;
		}
		return !match_omit_(legacy);
	}

	public TitanBoolean match_omit() {
		return match_omit(false);
	}

	public TitanBoolean match_omit(boolean legacy) {
		return new TitanBoolean(match_omit_(legacy));
	}

	private boolean match_omit_(boolean legacy) {
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

	public TitanCharacter_String valueOf() {
		if (templateSelection != template_sel.SPECIFIC_VALUE || is_ifPresent) {
			throw new TtcnError("Performing a valueof or send operation on a non-specific template of type CHARACTER STRING.");
		}
		TitanCharacter_String ret_val = new TitanCharacter_String();
		if (identification.isBound().getValue()) {
			ret_val.identification.assign(identification.valueOf());
		}
		if (data__value__descriptor.isOmit()) {
			ret_val.data__value__descriptor.assign(template_sel.OMIT_VALUE);
		} else if (data__value__descriptor.isBound().getValue()) {
			ret_val.data__value__descriptor.assign(data__value__descriptor.valueOf());
		}
		if (string__value.isBound().getValue()) {
			ret_val.string__value.assign(string__value.valueOf());
		}
		return ret_val;
	}

	public TitanCharacter_String_template listItem(int list_index) {
		if (templateSelection != template_sel.VALUE_LIST && templateSelection != template_sel.COMPLEMENTED_LIST) {
			throw new TtcnError("Accessing a list element of a non-list template of type CHARACTER STRING.");
		}
		if (list_index >= list_value.size()) {
			throw new TtcnError("Index overflow in a value list template of type CHARACTER STRING.");
		}
		return list_value.get(list_index);
	}

	public void setType(template_sel template_type, int list_length) {
		if (template_type != template_sel.VALUE_LIST && template_type != template_sel.COMPLEMENTED_LIST) {
			throw new TtcnError("Setting an invalid list for a template of type CHARACTER STRING.");
		}
		cleanUp();
		setSelection(template_type);
		list_value = new ArrayList<TitanCharacter_String_template>(list_length);
		for(int i = 0 ; i < list_length; i++) {
			list_value.add(new TitanCharacter_String_template());
		}
	}

	public TitanBoolean isBound() {
		if (templateSelection == template_sel.UNINITIALIZED_TEMPLATE && !is_ifPresent) {
			return new TitanBoolean(false);
		}
		if (templateSelection != template_sel.SPECIFIC_VALUE) {
			return new TitanBoolean(true);
		}
		if (identification.isBound().getValue()) {
			return new TitanBoolean(true);
		}
		if (data__value__descriptor.isOmit() || data__value__descriptor.isBound().getValue()) {
			return new TitanBoolean(true);
		}
		if (string__value.isBound().getValue()) {
			return new TitanBoolean(true);
		}
		return new TitanBoolean(false);
	}

	public TitanBoolean isValue() {
		if (templateSelection != template_sel.SPECIFIC_VALUE || is_ifPresent) {
			return new TitanBoolean(false);
		}
		if (!identification.isValue().getValue()) {
			return new TitanBoolean(false);
		}
		if (!data__value__descriptor.isOmit() && !data__value__descriptor.isValue().getValue()) {
			return new TitanBoolean(false);
		}
		if (!string__value.isValue().getValue()) {
			return new TitanBoolean(false);
		}
		return new TitanBoolean(true);
	}

	public TitanBoolean match(TitanCharacter_String other_value) {
		return match(other_value, false);
	}

	public TitanBoolean match(TitanCharacter_String other_value, boolean legacy) {
		return new TitanBoolean(match_(other_value, legacy));
	}

	private boolean match_(TitanCharacter_String other_value, boolean legacy) {
		if (!other_value.isBound().getValue()) {
			return false;
		}
		switch (templateSelection) {
		case ANY_VALUE:
		case ANY_OR_OMIT:
			return true;
		case OMIT_VALUE:
			return false;
		case SPECIFIC_VALUE:
			if(!other_value.getIdentification().isBound().getValue()) {
				return false;
			}
			if(!identification.match(other_value.getIdentification(), legacy).getValue()) {
				return false;
			}
			if(!other_value.getData__value__descriptor().isBound().getValue()) {
				return false;
			}
			if((other_value.getData__value__descriptor().isPresent().getValue() ? !data__value__descriptor.match(other_value.getData__value__descriptor().get(), legacy).getValue() : !data__value__descriptor.match_omit(legacy).getValue())) {
				return false;
			}
			if(!other_value.getString__value().isBound().getValue()) {
				return false;
			}
			if(!string__value.match(other_value.getString__value(), legacy).getValue()) {
				return false;
			}
			return true;
		case VALUE_LIST:
		case COMPLEMENTED_LIST:
			for (int list_count = 0; list_count < list_value.size(); list_count++)
				if (list_value.get(list_count).match(other_value, legacy).getValue()) {
					return templateSelection == template_sel.VALUE_LIST;
				}
			return templateSelection == template_sel.COMPLEMENTED_LIST;
		default:
			throw new TtcnError("Matching an uninitialized/unsupported template of type CHARACTER STRING.");
		}
	}

	@Override
	public TitanBoolean match(final Base_Type otherValue, final boolean legacy) {
		if (otherValue instanceof TitanCharacter_String) {
			return match((TitanCharacter_String)otherValue, legacy);
		}

		throw new TtcnError("Internal Error: The left operand of assignment is not of type CHARACTER__STRING.");
	}

	public TitanInteger sizeOf() {
		if (is_ifPresent) {
			throw new TtcnError("Performing sizeof() operation on a template of type CHARACTER STRING which has an ifpresent attribute.");
		}
		switch (templateSelection) {
		case SPECIFIC_VALUE:
			int sizeof = 0;
			if (data__value__descriptor.isPresent().getValue()) {
				sizeof++;
			}
			sizeof += 2;
			return new TitanInteger(sizeof);
		case VALUE_LIST:
			if (list_value.size() < 1) {
				throw new TtcnError("Internal error: Performing sizeof() operation on a template of type CHARACTER STRING containing an empty list.");
			}
			int item_size = list_value.get(0).sizeOf().getInt();
			for (int l_idx = 1; l_idx < list_value.size(); l_idx++) {
				if (list_value.get(l_idx).sizeOf().getInt() != item_size) {
					throw new TtcnError("Performing sizeof() operation on a template of type CHARACTER STRING containing a value list with different sizes.");
				}
			}
			return new TitanInteger(item_size);
		case OMIT_VALUE:
			throw new TtcnError("Performing sizeof() operation on a template of type CHARACTER STRING containing omit value.");
		case ANY_VALUE:
		case ANY_OR_OMIT:
			throw new TtcnError("Performing sizeof() operation on a template of type CHARACTER STRING containing */? value.");
		case COMPLEMENTED_LIST:
			throw new TtcnError("Performing sizeof() operation on a template of type CHARACTER STRING containing complemented list.");
		default:
			throw new TtcnError("Performing sizeof() operation on an uninitialized/unsupported template of type CHARACTER STRING.");
		}
	}
	public void log() {
		switch (templateSelection) {
		case SPECIFIC_VALUE:
			TtcnLogger.log_char('{');
			TtcnLogger.log_event_str(" identification := ");
			identification.log();
			TtcnLogger.log_char(',');
			TtcnLogger.log_event_str(" data__value__descriptor := ");
			data__value__descriptor.log();
			TtcnLogger.log_char(',');
			TtcnLogger.log_event_str(" string__value := ");
			string__value.log();
			TtcnLogger.log_event_str(" }");
			break;
		case COMPLEMENTED_LIST:
			TtcnLogger.log_event_str("complement ");
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
}