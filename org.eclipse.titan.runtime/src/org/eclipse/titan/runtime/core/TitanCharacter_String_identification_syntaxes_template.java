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
public class TitanCharacter_String_identification_syntaxes_template extends Base_Template {

	private TitanObjectid_template abstract_; //ObjectID_Type
	private TitanObjectid_template transfer; //ObjectID_Type
	//originally value_list/list_value
	List<TitanCharacter_String_identification_syntaxes_template> list_value;

	public TitanObjectid_template getAbstract_() {
		setSpecific();
		return abstract_;
	}

	public TitanObjectid_template constGetAbstract_() {
		if (templateSelection != template_sel.SPECIFIC_VALUE) {
			throw new TtcnError("Accessing field abstract of a non-specific template of type CHARACTER STRING.identification.syntaxes.");
		}
		return abstract_;
	}

	public TitanObjectid_template getTransfer() {
		setSpecific();
		return transfer;
	}

	public TitanObjectid_template constGetTransfer() {
		if (templateSelection != template_sel.SPECIFIC_VALUE) {
			throw new TtcnError("Accessing field transfer of a non-specific template of type CHARACTER STRING.identification.syntaxes.");
		}
		return transfer;
	}

	private void setSpecific() {
		if (templateSelection != template_sel.SPECIFIC_VALUE) {
			final template_sel old_selection = templateSelection;
			cleanUp();
			setSelection(template_sel.SPECIFIC_VALUE);
			abstract_ = new TitanObjectid_template();
			transfer = new TitanObjectid_template();
			if (old_selection == template_sel.ANY_VALUE || old_selection == template_sel.ANY_OR_OMIT) {
				abstract_.assign(template_sel.ANY_VALUE);
				transfer.assign(template_sel.ANY_VALUE);
			}
		}
	}

	public TitanCharacter_String_identification_syntaxes_template() {
	}

	public TitanCharacter_String_identification_syntaxes_template(final template_sel other_value ) {
		super( other_value );
		checkSingleSelection( other_value );
	}

	public TitanCharacter_String_identification_syntaxes_template( final TitanCharacter_String_identification_syntaxes otherValue ) {
		copyValue(otherValue);
	}

	public TitanCharacter_String_identification_syntaxes_template( final TitanCharacter_String_identification_syntaxes_template otherValue ) {
		copyTemplate( otherValue );
	}

	public TitanCharacter_String_identification_syntaxes_template( final Optional<TitanCharacter_String_identification_syntaxes> other_value ) {
		switch (other_value.getSelection()) {
		case OPTIONAL_PRESENT:
			copyValue(other_value.constGet());
			break;
		case OPTIONAL_OMIT:
			setSelection(template_sel.OMIT_VALUE);
			break;
		default:
			throw new TtcnError("Creating a template of type CHARACTER STRING.identification.syntaxes from an unbound optional field.");
		}
	}

	//originally operator=
	public TitanCharacter_String_identification_syntaxes_template assign( final template_sel other_value ) {
		checkSingleSelection(other_value);
		cleanUp();
		setSelection(other_value);
		return this;
	}

	//originally operator=
	public TitanCharacter_String_identification_syntaxes_template assign( final TitanCharacter_String_identification_syntaxes other_value ) {
		cleanUp();
		copyValue(other_value);
		return this;
	}

	//originally operator=
	public TitanCharacter_String_identification_syntaxes_template assign( final TitanCharacter_String_identification_syntaxes_template other_value ) {
		if (other_value != this) {
			cleanUp();
			copyTemplate(other_value);
		}
		return this;
	}

	@Override
	public TitanCharacter_String_identification_syntaxes_template assign(final Base_Type otherValue) {
		if (otherValue instanceof TitanCharacter_String_identification_syntaxes) {
			return assign((TitanCharacter_String_identification_syntaxes) otherValue);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `TitanCharacter_String_identification_syntaxes' can not be cast to {1}", otherValue));
	}

	@Override
	public TitanCharacter_String_identification_syntaxes_template assign(final Base_Template otherValue) {
		if (otherValue instanceof TitanCharacter_String_identification_syntaxes_template) {
			return assign((TitanCharacter_String_identification_syntaxes_template) otherValue);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `TitanCharacter_String_identification_syntaxes' can not be cast to {1}_template", otherValue));
	}

	public TitanCharacter_String_identification_syntaxes_template assign( final Optional<TitanCharacter_String_identification_syntaxes> other_value ) {
		cleanUp();
		switch (other_value.getSelection()) {
		case OPTIONAL_PRESENT:
			copyValue(other_value.constGet());
			break;
		case OPTIONAL_OMIT:
			setSelection(template_sel.OMIT_VALUE);
			break;
		default:
			throw new TtcnError("Assignment of an unbound optional field to a template of type CHARACTER STRING.identification.syntaxes.");
		}
		return this;
	}

	private void copyValue(final TitanCharacter_String_identification_syntaxes other_value) {
		if (other_value.getAbstract_().isBound()) {
			getAbstract_().assign(other_value.getAbstract_());
		} else {
			getAbstract_().cleanUp();
		}
		if (other_value.getTransfer().isBound()) {
			getTransfer().assign(other_value.getTransfer());
		} else {
			getTransfer().cleanUp();
		}
		setSelection(template_sel.SPECIFIC_VALUE);
	}

	private void copyTemplate(final TitanCharacter_String_identification_syntaxes_template other_value) {
		switch (other_value.templateSelection) {
		case SPECIFIC_VALUE:
			if (template_sel.UNINITIALIZED_TEMPLATE == other_value.getAbstract_().getSelection()) {
				getAbstract_().cleanUp();
			} else {
				getAbstract_().assign(other_value.getAbstract_());
			}
			if (template_sel.UNINITIALIZED_TEMPLATE == other_value.getTransfer().getSelection()) {
				getTransfer().cleanUp();
			} else {
				getTransfer().assign(other_value.getTransfer());
			}
			break;
		case OMIT_VALUE:
		case ANY_VALUE:
		case ANY_OR_OMIT:
			break;
		case VALUE_LIST:
		case COMPLEMENTED_LIST:
			list_value = new ArrayList<TitanCharacter_String_identification_syntaxes_template>(other_value.list_value.size());
			for(int i = 0; i < other_value.list_value.size(); i++) {
				final TitanCharacter_String_identification_syntaxes_template temp = new TitanCharacter_String_identification_syntaxes_template(other_value.list_value.get(i));
				list_value.add(temp);
			}
			break;
		default:
			throw new TtcnError("Copying an uninitialized template of type CHARACTER STRING.identification.syntaxes.");
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

	public TitanCharacter_String_identification_syntaxes valueOf() {
		if (templateSelection != template_sel.SPECIFIC_VALUE || is_ifPresent) {
			throw new TtcnError("Performing a valueof or send operation on a non-specific template of type CHARACTER STRING.identification.syntaxes.");
		}
		final TitanCharacter_String_identification_syntaxes ret_val = new TitanCharacter_String_identification_syntaxes();
		if (abstract_.isBound()) {
			ret_val.abstract_.assign(abstract_.valueOf());
		}
		if (transfer.isBound()) {
			ret_val.transfer.assign(transfer.valueOf());
		}
		return ret_val;
	}

	public TitanCharacter_String_identification_syntaxes_template listItem(final int list_index) {
		if (templateSelection != template_sel.VALUE_LIST && templateSelection != template_sel.COMPLEMENTED_LIST) {
			throw new TtcnError("Accessing a list element of a non-list template of type CHARACTER STRING.identification.syntaxes.");
		}
		if (list_index >= list_value.size()) {
			throw new TtcnError("Index overflow in a value list template of type CHARACTER STRING.identification.syntaxes.");
		}
		return list_value.get(list_index);
	}

	public void setType(final template_sel template_type, final int list_length) {
		if (template_type != template_sel.VALUE_LIST && template_type != template_sel.COMPLEMENTED_LIST) {
			throw new TtcnError("Setting an invalid list for a template of type CHARACTER STRING.identification.syntaxes.");
		}
		cleanUp();
		setSelection(template_type);
		list_value = new ArrayList<TitanCharacter_String_identification_syntaxes_template>(list_length);
		for(int i = 0 ; i < list_length; i++) {
			list_value.add(new TitanCharacter_String_identification_syntaxes_template());
		}
	}

	public boolean isBound() {
		if (templateSelection == template_sel.UNINITIALIZED_TEMPLATE && !is_ifPresent) {
			return false;
		}
		if (templateSelection != template_sel.SPECIFIC_VALUE) {
			return true;
		}
		if (abstract_.isBound()) {
			return true;
		}
		if (transfer.isBound()) {
			return true;
		}
		return false;
	}

	public boolean isValue() {
		if (templateSelection != template_sel.SPECIFIC_VALUE || is_ifPresent) {
			return false;
		}
		if (!abstract_.isValue()) {
			return false;
		}
		if (!transfer.isValue()) {
			return false;
		}
		return true;
	}

	public boolean match(final TitanCharacter_String_identification_syntaxes other_value) {
		return match(other_value, false);
	}

	public boolean match(final TitanCharacter_String_identification_syntaxes other_value, final boolean legacy) {
		return match_(other_value, legacy);
	}

	private boolean match_(final TitanCharacter_String_identification_syntaxes other_value, final boolean legacy) {
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
			if(!other_value.getAbstract_().isBound()) {
				return false;
			}
			if(!abstract_.match(other_value.getAbstract_(), legacy)) {
				return false;
			}
			if(!other_value.getTransfer().isBound()) {
				return false;
			}
			if(!transfer.match(other_value.getTransfer(), legacy)) {
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
			throw new TtcnError("Matching an uninitialized/unsupported template of type CHARACTER STRING.identification.syntaxes.");
		}
	}

	@Override
	public boolean match(final Base_Type otherValue, final boolean legacy) {
		if (otherValue instanceof TitanCharacter_String_identification_syntaxes) {
			return match((TitanCharacter_String_identification_syntaxes)otherValue, legacy);
		}

		throw new TtcnError("Internal Error: The left operand of assignment is not of type TitanCharacter_String_identification_syntaxes.");
	}

	public TitanInteger sizeOf() {
		if (is_ifPresent) {
			throw new TtcnError("Performing sizeof() operation on a template of type CHARACTER STRING.identification.syntaxes which has an ifpresent attribute.");
		}
		switch (templateSelection) {
		case SPECIFIC_VALUE:
			int sizeof = 0;
			sizeof += 2;
			return new TitanInteger(sizeof);
		case VALUE_LIST:
			if (list_value.isEmpty()) {
				throw new TtcnError("Internal error: Performing sizeof() operation on a template of type CHARACTER STRING.identification.syntaxes containing an empty list.");
			}
			final int item_size = list_value.get(0).sizeOf().getInt();
			for (int l_idx = 1; l_idx < list_value.size(); l_idx++) {
				if (list_value.get(l_idx).sizeOf().getInt() != item_size) {
					throw new TtcnError("Performing sizeof() operation on a template of type CHARACTER STRING.identification.syntaxes containing a value list with different sizes.");
				}
			}
			return new TitanInteger(item_size);
		case OMIT_VALUE:
			throw new TtcnError("Performing sizeof() operation on a template of type CHARACTER STRING.identification.syntaxes containing omit value.");
		case ANY_VALUE:
		case ANY_OR_OMIT:
			throw new TtcnError("Performing sizeof() operation on a template of type CHARACTER STRING.identification.syntaxes containing */? value.");
		case COMPLEMENTED_LIST:
			throw new TtcnError("Performing sizeof() operation on a template of type CHARACTER STRING.identification.syntaxes containing complemented list.");
		default:
			throw new TtcnError("Performing sizeof() operation on an uninitialized/unsupported template of type CHARACTER STRING.identification.syntaxes.");
		}
	}

	public void log() {
		switch (templateSelection) {
		case SPECIFIC_VALUE:
			TtcnLogger.log_char('{');
			TtcnLogger.log_event_str(" abstract := ");
			abstract_.log();
			TtcnLogger.log_char(',');
			TtcnLogger.log_event_str(" transfer := ");
			transfer.log();
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

	public void log_match(final TitanCharacter_String_identification_syntaxes match_value) {
		log_match(match_value, false);
	}

	@Override
	public void log_match(final Base_Type match_value, final boolean legacy) {
		if (match_value instanceof TitanCharacter_String_identification_syntaxes) {
			log_match((TitanCharacter_String_identification_syntaxes)match_value, legacy);
			return;
		}

		throw new TtcnError("Internal Error: value can not be cast to CHARACTER STRING.identification.syntaxes.");
	}

	public void log_match(final TitanCharacter_String_identification_syntaxes match_value, final boolean legacy) {
		if ( TtcnLogger.matching_verbosity_t.VERBOSITY_COMPACT == TtcnLogger.get_matching_verbosity() ) {
			if(match(match_value, legacy)) {
				TtcnLogger.print_logmatch_buffer();
				TtcnLogger.log_event_str(" matched");
			} else {
				if (templateSelection == template_sel.SPECIFIC_VALUE) {
					int previous_size = TtcnLogger.get_logmatch_buffer_len();
					if( !abstract_.match(match_value.constGetAbstract_(), legacy) ) {
						TtcnLogger.log_logmatch_info(".abstract");
						abstract_.log_match(match_value.constGetAbstract_(), legacy);
						TtcnLogger.set_logmatch_buffer_len(previous_size);
					}
					if( !transfer.match(match_value.constGetTransfer(), legacy) ) {
						TtcnLogger.log_logmatch_info(".transfer");
						transfer.log_match(match_value.constGetTransfer(), legacy);
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
			TtcnLogger.log_event_str("{ abstract := ");
			abstract_.log_match(match_value.constGetAbstract_(), legacy);
			TtcnLogger.log_event_str("{ transfer := ");
			transfer.log_match(match_value.constGetTransfer(), legacy);
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
}