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
 * Part of the representation of the ASN.1 EMBEDDED PDV type
 * 
 * @author Kristof Szabados
 */
public class TitanEmbedded_PDV_identification_context__negotiation_template extends Base_Template {

	private TitanInteger_template presentation__context__id; //ASN1_Integer_Type
	private TitanObjectid_template transfer__syntax; //ObjectID_Type
	//originally value_list/list_value
	List<TitanEmbedded_PDV_identification_context__negotiation_template> list_value;

	public TitanInteger_template getPresentation__context__id() {
		setSpecific();
		return presentation__context__id;
	}

	public TitanInteger_template constGetPresentation__context__id() {
		if (templateSelection != template_sel.SPECIFIC_VALUE) {
			throw new TtcnError("Accessing field presentation__context__id of a non-specific template of type EMBEDDED PDV.identification.context-negotiation.");
		}
		return presentation__context__id;
	}

	public TitanObjectid_template getTransfer__syntax() {
		setSpecific();
		return transfer__syntax;
	}

	public TitanObjectid_template constGetTransfer__syntax() {
		if (templateSelection != template_sel.SPECIFIC_VALUE) {
			throw new TtcnError("Accessing field transfer__syntax of a non-specific template of type EMBEDDED PDV.identification.context-negotiation.");
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

	public TitanEmbedded_PDV_identification_context__negotiation_template() {
	}

	public TitanEmbedded_PDV_identification_context__negotiation_template(final template_sel other_value ) {
		super( other_value );
		checkSingleSelection( other_value );
	}

	public TitanEmbedded_PDV_identification_context__negotiation_template( final TitanEmbedded_PDV_identification_context__negotiation otherValue ) {
		copyValue(otherValue);
	}

	public TitanEmbedded_PDV_identification_context__negotiation_template( final TitanEmbedded_PDV_identification_context__negotiation_template otherValue ) {
		copyTemplate( otherValue );
	}

	public TitanEmbedded_PDV_identification_context__negotiation_template( final Optional<TitanEmbedded_PDV_identification_context__negotiation> other_value ) {
		switch (other_value.getSelection()) {
		case OPTIONAL_PRESENT:
			copyValue(other_value.constGet());
			break;
		case OPTIONAL_OMIT:
			setSelection(template_sel.OMIT_VALUE);
			break;
		default:
			throw new TtcnError("Creating a template of type EMBEDDED PDV.identification.context-negotiation from an unbound optional field.");
		}
	}

	//originally operator=
	public TitanEmbedded_PDV_identification_context__negotiation_template assign( final template_sel other_value ) {
		checkSingleSelection(other_value);
		cleanUp();
		setSelection(other_value);
		return this;
	}

	//originally operator=
	public TitanEmbedded_PDV_identification_context__negotiation_template assign( final TitanEmbedded_PDV_identification_context__negotiation other_value ) {
		cleanUp();
		copyValue(other_value);
		return this;
	}

	//originally operator=
	public TitanEmbedded_PDV_identification_context__negotiation_template assign( final TitanEmbedded_PDV_identification_context__negotiation_template other_value ) {
		if (other_value != this) {
			cleanUp();
			copyTemplate(other_value);
		}
		return this;
	}

	@Override
	public TitanEmbedded_PDV_identification_context__negotiation_template assign(final Base_Type otherValue) {
		if (otherValue instanceof TitanEmbedded_PDV_identification_context__negotiation) {
			return assign((TitanEmbedded_PDV_identification_context__negotiation) otherValue);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `EMBEDDED__PDV_identification_context__negotiation' can not be cast to {1}", otherValue));
	}

	@Override
	public TitanEmbedded_PDV_identification_context__negotiation_template assign(final Base_Template otherValue) {
		if (otherValue instanceof TitanEmbedded_PDV_identification_context__negotiation_template) {
			return assign((TitanEmbedded_PDV_identification_context__negotiation_template) otherValue);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `EMBEDDED__PDV_identification_context__negotiation' can not be cast to {1}_template", otherValue));
	}

	public TitanEmbedded_PDV_identification_context__negotiation_template assign( final Optional<TitanEmbedded_PDV_identification_context__negotiation> other_value ) {
		cleanUp();
		switch (other_value.getSelection()) {
		case OPTIONAL_PRESENT:
			copyValue(other_value.constGet());
			break;
		case OPTIONAL_OMIT:
			setSelection(template_sel.OMIT_VALUE);
			break;
		default:
			throw new TtcnError("Assignment of an unbound optional field to a template of type EMBEDDED PDV.identification.context-negotiation.");
		}
		return this;
	}

	private void copyValue(final TitanEmbedded_PDV_identification_context__negotiation other_value) {
		if (other_value.getPresentation__context__id().isBound().getValue()) {
			getPresentation__context__id().assign(other_value.getPresentation__context__id());
		} else {
			getPresentation__context__id().cleanUp();
		}
		if (other_value.getTransfer__syntax().isBound().getValue()) {
			getTransfer__syntax().assign(other_value.getTransfer__syntax());
		} else {
			getTransfer__syntax().cleanUp();
		}
		setSelection(template_sel.SPECIFIC_VALUE);
	}

	private void copyTemplate(final TitanEmbedded_PDV_identification_context__negotiation_template other_value) {
		switch (other_value.templateSelection) {
		case SPECIFIC_VALUE:
			if (template_sel.UNINITIALIZED_TEMPLATE != other_value.getPresentation__context__id().getSelection()) {
				getPresentation__context__id().assign(other_value.getPresentation__context__id());
			} else {
				getPresentation__context__id().cleanUp();
			}
			if (template_sel.UNINITIALIZED_TEMPLATE != other_value.getTransfer__syntax().getSelection()) {
				getTransfer__syntax().assign(other_value.getTransfer__syntax());
			} else {
				getTransfer__syntax().cleanUp();
			}
			break;
		case OMIT_VALUE:
		case ANY_VALUE:
		case ANY_OR_OMIT:
			break;
		case VALUE_LIST:
		case COMPLEMENTED_LIST:
			list_value = new ArrayList<TitanEmbedded_PDV_identification_context__negotiation_template>(other_value.list_value.size());
			for(int i = 0; i < other_value.list_value.size(); i++) {
				final TitanEmbedded_PDV_identification_context__negotiation_template temp = new TitanEmbedded_PDV_identification_context__negotiation_template(other_value.list_value.get(i));
				list_value.add(temp);
			}
			break;
		default:
			throw new TtcnError("Copying an uninitialized template of type EMBEDDED PDV.identification.context-negotiation.");
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

	public TitanEmbedded_PDV_identification_context__negotiation valueOf() {
		if (templateSelection != template_sel.SPECIFIC_VALUE || is_ifPresent) {
			throw new TtcnError("Performing a valueof or send operation on a non-specific template of type EMBEDDED PDV.identification.context-negotiation.");
		}
		TitanEmbedded_PDV_identification_context__negotiation ret_val = new TitanEmbedded_PDV_identification_context__negotiation();
		if (presentation__context__id.isBound().getValue()) {
			ret_val.presentation__context__id.assign(presentation__context__id.valueOf());
		}
		if (transfer__syntax.isBound().getValue()) {
			ret_val.transfer__syntax.assign(transfer__syntax.valueOf());
		}
		return ret_val;
	}

	public TitanEmbedded_PDV_identification_context__negotiation_template listItem(int list_index) {
		if (templateSelection != template_sel.VALUE_LIST && templateSelection != template_sel.COMPLEMENTED_LIST) {
			throw new TtcnError("Accessing a list element of a non-list template of type EMBEDDED PDV.identification.context-negotiation.");
		}
		if (list_index >= list_value.size()) {
			throw new TtcnError("Index overflow in a value list template of type EMBEDDED PDV.identification.context-negotiation.");
		}
		return list_value.get(list_index);
	}

	public void setType(template_sel template_type, int list_length) {
		if (template_type != template_sel.VALUE_LIST && template_type != template_sel.COMPLEMENTED_LIST) {
			throw new TtcnError("Setting an invalid list for a template of type EMBEDDED PDV.identification.context-negotiation.");
		}
		cleanUp();
		setSelection(template_type);
		list_value = new ArrayList<TitanEmbedded_PDV_identification_context__negotiation_template>(list_length);
		for(int i = 0 ; i < list_length; i++) {
			list_value.add(new TitanEmbedded_PDV_identification_context__negotiation_template());
		}
	}

	public TitanBoolean isBound() {
		if (templateSelection == template_sel.UNINITIALIZED_TEMPLATE && !is_ifPresent) {
			return new TitanBoolean(false);
		}
		if (templateSelection != template_sel.SPECIFIC_VALUE) {
			return new TitanBoolean(true);
		}
		if (presentation__context__id.isBound().getValue()) {
			return new TitanBoolean(true);
		}
		if (transfer__syntax.isBound().getValue()) {
			return new TitanBoolean(true);
		}
		return new TitanBoolean(false);
	}

	public TitanBoolean isValue() {
		if (templateSelection != template_sel.SPECIFIC_VALUE || is_ifPresent) {
			return new TitanBoolean(false);
		}
		if (!presentation__context__id.isValue().getValue()) {
			return new TitanBoolean(false);
		}
		if (!transfer__syntax.isValue().getValue()) {
			return new TitanBoolean(false);
		}
		return new TitanBoolean(true);
	}

	public TitanBoolean match(TitanEmbedded_PDV_identification_context__negotiation other_value) {
		return match(other_value, false);
	}

	public TitanBoolean match(TitanEmbedded_PDV_identification_context__negotiation other_value, boolean legacy) {
		return new TitanBoolean(match_(other_value, legacy));
	}

	private boolean match_(TitanEmbedded_PDV_identification_context__negotiation other_value, boolean legacy) {
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
			if(!other_value.getPresentation__context__id().isBound().getValue()) {
				return false;
			}
			if(!presentation__context__id.match(other_value.getPresentation__context__id(), legacy).getValue()) {
				return false;
			}
			if(!other_value.getTransfer__syntax().isBound().getValue()) {
				return false;
			}
			if(!transfer__syntax.match(other_value.getTransfer__syntax(), legacy).getValue()) {
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
			throw new TtcnError("Matching an uninitialized/unsupported template of type EMBEDDED PDV.identification.context-negotiation.");
		}
	}

	@Override
	public TitanBoolean match(final Base_Type otherValue, final boolean legacy) {
		if (otherValue instanceof TitanEmbedded_PDV_identification_context__negotiation) {
			return match((TitanEmbedded_PDV_identification_context__negotiation)otherValue, legacy);
		}

		throw new TtcnError("Internal Error: The left operand of assignment is not of type EMBEDDED__PDV_identification_context__negotiation.");
	}

	public TitanInteger sizeOf() {
		if (is_ifPresent) {
			throw new TtcnError("Performing sizeof() operation on a template of type EMBEDDED PDV.identification.context-negotiation which has an ifpresent attribute.");
		}
		switch (templateSelection) {
		case SPECIFIC_VALUE:
			int sizeof = 0;
			sizeof += 2;
			return new TitanInteger(sizeof);
		case VALUE_LIST:
			if (list_value.size() < 1) {
				throw new TtcnError("Internal error: Performing sizeof() operation on a template of type EMBEDDED PDV.identification.context-negotiation containing an empty list.");
			}
			int item_size = list_value.get(0).sizeOf().getInt();
			for (int l_idx = 1; l_idx < list_value.size(); l_idx++) {
				if (list_value.get(l_idx).sizeOf().getInt() != item_size) {
					throw new TtcnError("Performing sizeof() operation on a template of type EMBEDDED PDV.identification.context-negotiation containing a value list with different sizes.");
				}
			}
			return new TitanInteger(item_size);
		case OMIT_VALUE:
			throw new TtcnError("Performing sizeof() operation on a template of type EMBEDDED PDV.identification.context-negotiation containing omit value.");
		case ANY_VALUE:
		case ANY_OR_OMIT:
			throw new TtcnError("Performing sizeof() operation on a template of type EMBEDDED PDV.identification.context-negotiation containing */? value.");
		case COMPLEMENTED_LIST:
			throw new TtcnError("Performing sizeof() operation on a template of type EMBEDDED PDV.identification.context-negotiation containing complemented list.");
		default:
			throw new TtcnError("Performing sizeof() operation on an uninitialized/unsupported template of type EMBEDDED PDV.identification.context-negotiation.");
		}
	}
	public void log() {
		switch (templateSelection) {
		case SPECIFIC_VALUE:
			TtcnLogger.log_char('{');
			TtcnLogger.log_event_str(" presentation__context__id := ");
			presentation__context__id.log();
			TtcnLogger.log_char(',');
			TtcnLogger.log_event_str(" transfer__syntax := ");
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
}