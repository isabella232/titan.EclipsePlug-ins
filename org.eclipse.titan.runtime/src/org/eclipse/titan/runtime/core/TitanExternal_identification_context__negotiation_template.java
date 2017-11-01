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
 * Part of the representation of the ASN.1 EXTERNAL type
 * 
 * @author Kristof Szabados
 */
public class TitanExternal_identification_context__negotiation_template extends Base_Template {

	private TitanInteger_template presentation__context__id; //ASN1_Integer_Type
	private TitanObjectid_template transfer__syntax; //ObjectID_Type
	//originally value_list/list_value
	List<TitanExternal_identification_context__negotiation_template> list_value;

	public TitanInteger_template getPresentation__context__id() {
		setSpecific();
		return presentation__context__id;
	}

	public TitanInteger_template constGetPresentation__context__id() {
		if (templateSelection != template_sel.SPECIFIC_VALUE) {
			throw new TtcnError("Accessing field presentation-context-id of a non-specific template of type EXTERNAL.identification.context-negotiation.");
		}
		return presentation__context__id;
	}

	public TitanObjectid_template getTransfer__syntax() {
		setSpecific();
		return transfer__syntax;
	}

	public TitanObjectid_template constGetTransfer__syntax() {
		if (templateSelection != template_sel.SPECIFIC_VALUE) {
			throw new TtcnError("Accessing field transfer-syntax of a non-specific template of type EXTERNAL.identification.context-negotiation.");
		}
		return transfer__syntax;
	}

	private void setSpecific() {
		if (templateSelection != template_sel.SPECIFIC_VALUE) {
			final template_sel old_selection = templateSelection;
			cleanUp();
			setSelection(template_sel.SPECIFIC_VALUE);
			presentation__context__id = new TitanInteger_template();
			transfer__syntax = new TitanObjectid_template();
			if (old_selection == template_sel.ANY_VALUE || old_selection == template_sel.ANY_OR_OMIT) {
				presentation__context__id.assign(template_sel.ANY_VALUE);
				transfer__syntax.assign(template_sel.ANY_VALUE);
			}
		}
	}

	public TitanExternal_identification_context__negotiation_template() {
	}

	public TitanExternal_identification_context__negotiation_template(final template_sel other_value ) {
		super( other_value );
		checkSingleSelection( other_value );
	}

	public TitanExternal_identification_context__negotiation_template( final TitanExternal_identification_context__negotiation otherValue ) {
		copyValue(otherValue);
	}

	public TitanExternal_identification_context__negotiation_template( final TitanExternal_identification_context__negotiation_template otherValue ) {
		copyTemplate( otherValue );
	}

	public TitanExternal_identification_context__negotiation_template( final Optional<TitanExternal_identification_context__negotiation> other_value ) {
		switch (other_value.getSelection()) {
		case OPTIONAL_PRESENT:
			copyValue(other_value.constGet());
			break;
		case OPTIONAL_OMIT:
			setSelection(template_sel.OMIT_VALUE);
			break;
		default:
			throw new TtcnError("Creating a template of type EXTERNAL.identification.context-negotiation from an unbound optional field.");
		}
	}

	//originally operator=
	public TitanExternal_identification_context__negotiation_template assign( final template_sel other_value ) {
		checkSingleSelection(other_value);
		cleanUp();
		setSelection(other_value);
		return this;
	}

	//originally operator=
	public TitanExternal_identification_context__negotiation_template assign( final TitanExternal_identification_context__negotiation other_value ) {
		cleanUp();
		copyValue(other_value);
		return this;
	}

	//originally operator=
	public TitanExternal_identification_context__negotiation_template assign( final TitanExternal_identification_context__negotiation_template other_value ) {
		if (other_value != this) {
			cleanUp();
			copyTemplate(other_value);
		}
		return this;
	}

	@Override
	public TitanExternal_identification_context__negotiation_template assign(final Base_Type otherValue) {
		if (otherValue instanceof TitanExternal_identification_context__negotiation) {
			return assign((TitanExternal_identification_context__negotiation) otherValue);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `TitanExternal_identification_context__negotiation' can not be cast to {1}", otherValue));
	}

	@Override
	public TitanExternal_identification_context__negotiation_template assign(final Base_Template otherValue) {
		if (otherValue instanceof TitanExternal_identification_context__negotiation_template) {
			return assign((TitanExternal_identification_context__negotiation_template) otherValue);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `TitanExternal_identification_context__negotiation' can not be cast to {1}_template", otherValue));
	}

	public TitanExternal_identification_context__negotiation_template assign( final Optional<TitanExternal_identification_context__negotiation> other_value ) {
		cleanUp();
		switch (other_value.getSelection()) {
		case OPTIONAL_PRESENT:
			copyValue(other_value.constGet());
			break;
		case OPTIONAL_OMIT:
			setSelection(template_sel.OMIT_VALUE);
			break;
		default:
			throw new TtcnError("Assignment of an unbound optional field to a template of type EXTERNAL.identification.context-negotiation.");
		}
		return this;
	}

	private void copyValue(final TitanExternal_identification_context__negotiation other_value) {
		if (other_value.getPresentation__context__id().isBound()) {
			getPresentation__context__id().assign(other_value.getPresentation__context__id());
		} else {
			getPresentation__context__id().cleanUp();
		}
		if (other_value.getTransfer__syntax().isBound()) {
			getTransfer__syntax().assign(other_value.getTransfer__syntax());
		} else {
			getTransfer__syntax().cleanUp();
		}
		setSelection(template_sel.SPECIFIC_VALUE);
	}

	private void copyTemplate(final TitanExternal_identification_context__negotiation_template other_value) {
		switch (other_value.templateSelection) {
		case SPECIFIC_VALUE:
			if (template_sel.UNINITIALIZED_TEMPLATE == other_value.getPresentation__context__id().getSelection()) {
				getPresentation__context__id().cleanUp();
			} else {
				getPresentation__context__id().assign(other_value.getPresentation__context__id());
			}
			if (template_sel.UNINITIALIZED_TEMPLATE == other_value.getTransfer__syntax().getSelection()) {
				getTransfer__syntax().cleanUp();
			} else {
				getTransfer__syntax().assign(other_value.getTransfer__syntax());
			}
			break;
		case OMIT_VALUE:
		case ANY_VALUE:
		case ANY_OR_OMIT:
			break;
		case VALUE_LIST:
		case COMPLEMENTED_LIST:
			list_value = new ArrayList<TitanExternal_identification_context__negotiation_template>(other_value.list_value.size());
			for(int i = 0; i < other_value.list_value.size(); i++) {
				final TitanExternal_identification_context__negotiation_template temp = new TitanExternal_identification_context__negotiation_template(other_value.list_value.get(i));
				list_value.add(temp);
			}
			break;
		default:
			throw new TtcnError("Copying an uninitialized template of type EXTERNAL.identification.context-negotiation.");
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

	public TitanExternal_identification_context__negotiation valueOf() {
		if (templateSelection != template_sel.SPECIFIC_VALUE || is_ifPresent) {
			throw new TtcnError("Performing a valueof or send operation on a non-specific template of type EXTERNAL.identification.context-negotiation.");
		}
		final TitanExternal_identification_context__negotiation ret_val = new TitanExternal_identification_context__negotiation();
		if (presentation__context__id.isBound()) {
			ret_val.presentation__context__id.assign(presentation__context__id.valueOf());
		}
		if (transfer__syntax.isBound()) {
			ret_val.transfer__syntax.assign(transfer__syntax.valueOf());
		}
		return ret_val;
	}

	public TitanExternal_identification_context__negotiation_template listItem(final int list_index) {
		if (templateSelection != template_sel.VALUE_LIST && templateSelection != template_sel.COMPLEMENTED_LIST) {
			throw new TtcnError("Accessing a list element of a non-list template of type EXTERNAL.identification.context-negotiation.");
		}
		if (list_index >= list_value.size()) {
			throw new TtcnError("Index overflow in a value list template of type EXTERNAL.identification.context-negotiation.");
		}
		return list_value.get(list_index);
	}

	public void setType(final template_sel template_type, final int list_length) {
		if (template_type != template_sel.VALUE_LIST && template_type != template_sel.COMPLEMENTED_LIST) {
			throw new TtcnError("Setting an invalid list for a template of type EXTERNAL.identification.context-negotiation.");
		}
		cleanUp();
		setSelection(template_type);
		list_value = new ArrayList<TitanExternal_identification_context__negotiation_template>(list_length);
		for(int i = 0 ; i < list_length; i++) {
			list_value.add(new TitanExternal_identification_context__negotiation_template());
		}
	}

	public boolean isBound() {
		if (templateSelection == template_sel.UNINITIALIZED_TEMPLATE && !is_ifPresent) {
			return false;
		}
		if (templateSelection != template_sel.SPECIFIC_VALUE) {
			return true;
		}
		if (presentation__context__id.isBound()) {
			return true;
		}
		if (transfer__syntax.isBound()) {
			return true;
		}
		return false;
	}

	public boolean isValue() {
		if (templateSelection != template_sel.SPECIFIC_VALUE || is_ifPresent) {
			return false;
		}
		if (!presentation__context__id.isValue()) {
			return false;
		}
		if (!transfer__syntax.isValue()) {
			return false;
		}
		return true;
	}

	public boolean match(final TitanExternal_identification_context__negotiation other_value) {
		return match(other_value, false);
	}

	public boolean match(final TitanExternal_identification_context__negotiation other_value, final boolean legacy) {
		return match_(other_value, legacy);
	}

	private boolean match_(final TitanExternal_identification_context__negotiation other_value, final boolean legacy) {
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
			if(!other_value.getPresentation__context__id().isBound()) {
				return false;
			}
			if(!presentation__context__id.match(other_value.getPresentation__context__id(), legacy)) {
				return false;
			}
			if(!other_value.getTransfer__syntax().isBound()) {
				return false;
			}
			if(!transfer__syntax.match(other_value.getTransfer__syntax(), legacy)) {
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
			throw new TtcnError("Matching an uninitialized/unsupported template of type EXTERNAL.identification.context-negotiation.");
		}
	}

	@Override
	public boolean match(final Base_Type otherValue, final boolean legacy) {
		if (otherValue instanceof TitanExternal_identification_context__negotiation) {
			return match((TitanExternal_identification_context__negotiation)otherValue, legacy);
		}

		throw new TtcnError("Internal Error: The left operand of assignment is not of type TitanExternal_identification_context__negotiation.");
	}

	public TitanInteger sizeOf() {
		if (is_ifPresent) {
			throw new TtcnError("Performing sizeof() operation on a template of type EXTERNAL.identification.context-negotiation which has an ifpresent attribute.");
		}
		switch (templateSelection) {
		case SPECIFIC_VALUE:
			int sizeof = 0;
			sizeof += 2;
			return new TitanInteger(sizeof);
		case VALUE_LIST:
			if (list_value.isEmpty()) {
				throw new TtcnError("Internal error: Performing sizeof() operation on a template of type EXTERNAL.identification.context-negotiation containing an empty list.");
			}
			final int item_size = list_value.get(0).sizeOf().getInt();
			for (int l_idx = 1; l_idx < list_value.size(); l_idx++) {
				if (list_value.get(l_idx).sizeOf().getInt() != item_size) {
					throw new TtcnError("Performing sizeof() operation on a template of type EXTERNAL.identification.context-negotiation containing a value list with different sizes.");
				}
			}
			return new TitanInteger(item_size);
		case OMIT_VALUE:
			throw new TtcnError("Performing sizeof() operation on a template of type EXTERNAL.identification.context-negotiation containing omit value.");
		case ANY_VALUE:
		case ANY_OR_OMIT:
			throw new TtcnError("Performing sizeof() operation on a template of type EXTERNAL.identification.context-negotiation containing */? value.");
		case COMPLEMENTED_LIST:
			throw new TtcnError("Performing sizeof() operation on a template of type EXTERNAL.identification.context-negotiation containing complemented list.");
		default:
			throw new TtcnError("Performing sizeof() operation on an uninitialized/unsupported template of type EXTERNAL.identification.context-negotiation.");
		}
	}

	public void log() {
		switch (templateSelection) {
		case SPECIFIC_VALUE:
			TtcnLogger.log_char('{');
			TtcnLogger.log_event_str(" presentation-context-id := ");
			presentation__context__id.log();
			TtcnLogger.log_char(',');
			TtcnLogger.log_event_str(" transfer-syntax := ");
			transfer__syntax.log();
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

	public void log_match(final TitanExternal_identification_context__negotiation match_value) {
		log_match(match_value, false);
	}

	@Override
	public void log_match(final Base_Type match_value, final boolean legacy) {
		if (match_value instanceof TitanExternal_identification_context__negotiation) {
			log_match((TitanExternal_identification_context__negotiation)match_value, legacy);
			return;
		}

		throw new TtcnError("Internal Error: value can not be cast to EXTERNAL.identification.context-negotiation.");
	}

	public void log_match(final TitanExternal_identification_context__negotiation match_value, boolean legacy) {
		if ( TtcnLogger.matching_verbosity_t.VERBOSITY_COMPACT == TtcnLogger.get_matching_verbosity() ) {
			if(match(match_value, legacy)) {
				TtcnLogger.print_logmatch_buffer();
				TtcnLogger.log_event_str(" matched");
			} else {
				if (templateSelection == template_sel.SPECIFIC_VALUE) {
					int previous_size = TtcnLogger.get_logmatch_buffer_len();
					if( !presentation__context__id.match(match_value.constGetPresentation__context__id(), legacy) ) {
						TtcnLogger.log_logmatch_info(".presentation-context-id");
						presentation__context__id.log_match(match_value.constGetPresentation__context__id(), legacy);
						TtcnLogger.set_logmatch_buffer_len(previous_size);
					}
					if( !transfer__syntax.match(match_value.constGetTransfer__syntax(), legacy) ) {
						TtcnLogger.log_logmatch_info(".transfer-syntax");
						transfer__syntax.log_match(match_value.constGetTransfer__syntax(), legacy);
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
			TtcnLogger.log_event_str("{ presentation-context-id := ");
			presentation__context__id.log_match(match_value.constGetPresentation__context__id(), legacy);
			TtcnLogger.log_event_str("{ transfer-syntax := ");
			transfer__syntax.log_match(match_value.constGetTransfer__syntax(), legacy);
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