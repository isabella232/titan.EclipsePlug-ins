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

import org.eclipse.titan.runtime.core.Base_Template;
import org.eclipse.titan.runtime.core.Base_Type;
import org.eclipse.titan.runtime.core.Optional;
import org.eclipse.titan.runtime.core.TitanBoolean;
import org.eclipse.titan.runtime.core.TitanInteger;
import org.eclipse.titan.runtime.core.TitanObjectid_template;
import org.eclipse.titan.runtime.core.TtcnError;
import org.eclipse.titan.runtime.core.TtcnLogger;

/**
 * Part of the representation of the ASN.1 EXTERNAL type
 * 
 * @author Kristof Szabados
 */
public class TitanExternal_identification_syntaxes_template extends Base_Template {

	private TitanObjectid_template abstract_; //ObjectID_Type
	private TitanObjectid_template transfer; //ObjectID_Type
	//originally value_list/list_value
	List<TitanExternal_identification_syntaxes_template> list_value;

	public TitanObjectid_template getAbstract_() {
		setSpecific();
		return abstract_;
	}

	public TitanObjectid_template constGetAbstract_() {
		if (templateSelection != template_sel.SPECIFIC_VALUE) {
			throw new TtcnError("Accessing field abstract_ of a non-specific template of type EXTERNAL.identification.syntaxes.");
		}
		return abstract_;
	}

	public TitanObjectid_template getTransfer() {
		setSpecific();
		return transfer;
	}

	public TitanObjectid_template constGetTransfer() {
		if (templateSelection != template_sel.SPECIFIC_VALUE) {
			throw new TtcnError("Accessing field transfer of a non-specific template of type EXTERNAL.identification.syntaxes.");
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

	public TitanExternal_identification_syntaxes_template() {
	}

	public TitanExternal_identification_syntaxes_template(final template_sel other_value ) {
		super( other_value );
		checkSingleSelection( other_value );
	}

	public TitanExternal_identification_syntaxes_template( final TitanExternal_identification_syntaxes otherValue ) {
		copyValue(otherValue);
	}

	public TitanExternal_identification_syntaxes_template( final TitanExternal_identification_syntaxes_template otherValue ) {
		copyTemplate( otherValue );
	}

	public TitanExternal_identification_syntaxes_template( final Optional<TitanExternal_identification_syntaxes> other_value ) {
		switch (other_value.getSelection()) {
		case OPTIONAL_PRESENT:
			copyValue(other_value.constGet());
			break;
		case OPTIONAL_OMIT:
			setSelection(template_sel.OMIT_VALUE);
			break;
		default:
			throw new TtcnError("Creating a template of type EXTERNAL.identification.syntaxes from an unbound optional field.");
		}
	}

	//originally operator=
	public TitanExternal_identification_syntaxes_template assign( final template_sel other_value ) {
		checkSingleSelection(other_value);
		cleanUp();
		setSelection(other_value);
		return this;
	}

	//originally operator=
	public TitanExternal_identification_syntaxes_template assign( final TitanExternal_identification_syntaxes other_value ) {
		cleanUp();
		copyValue(other_value);
		return this;
	}

	//originally operator=
	public TitanExternal_identification_syntaxes_template assign( final TitanExternal_identification_syntaxes_template other_value ) {
		if (other_value != this) {
			cleanUp();
			copyTemplate(other_value);
		}
		return this;
	}

	@Override
	public TitanExternal_identification_syntaxes_template assign(final Base_Type otherValue) {
		if (otherValue instanceof TitanExternal_identification_syntaxes) {
			return assign((TitanExternal_identification_syntaxes) otherValue);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `EXTERNAL_identification_syntaxes' can not be cast to {1}", otherValue));
	}

	@Override
	public TitanExternal_identification_syntaxes_template assign(final Base_Template otherValue) {
		if (otherValue instanceof TitanExternal_identification_syntaxes_template) {
			return assign((TitanExternal_identification_syntaxes_template) otherValue);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `EXTERNAL_identification_syntaxes' can not be cast to {1}_template", otherValue));
	}

	public TitanExternal_identification_syntaxes_template assign( final Optional<TitanExternal_identification_syntaxes> other_value ) {
		cleanUp();
		switch (other_value.getSelection()) {
		case OPTIONAL_PRESENT:
			copyValue(other_value.constGet());
			break;
		case OPTIONAL_OMIT:
			setSelection(template_sel.OMIT_VALUE);
			break;
		default:
			throw new TtcnError("Assignment of an unbound optional field to a template of type EXTERNAL.identification.syntaxes.");
		}
		return this;
	}

	private void copyValue(final TitanExternal_identification_syntaxes other_value) {
		if (other_value.getAbstract_().isBound().getValue()) {
			getAbstract_().assign(other_value.getAbstract_());
		} else {
			getAbstract_().cleanUp();
		}
		if (other_value.getTransfer().isBound().getValue()) {
			getTransfer().assign(other_value.getTransfer());
		} else {
			getTransfer().cleanUp();
		}
		setSelection(template_sel.SPECIFIC_VALUE);
	}

	private void copyTemplate(final TitanExternal_identification_syntaxes_template other_value) {
		switch (other_value.templateSelection) {
		case SPECIFIC_VALUE:
			if (template_sel.UNINITIALIZED_TEMPLATE != other_value.getAbstract_().getSelection()) {
				getAbstract_().assign(other_value.getAbstract_());
			} else {
				getAbstract_().cleanUp();
			}
			if (template_sel.UNINITIALIZED_TEMPLATE != other_value.getTransfer().getSelection()) {
				getTransfer().assign(other_value.getTransfer());
			} else {
				getTransfer().cleanUp();
			}
			break;
		case OMIT_VALUE:
		case ANY_VALUE:
		case ANY_OR_OMIT:
			break;
		case VALUE_LIST:
		case COMPLEMENTED_LIST:
			list_value = new ArrayList<TitanExternal_identification_syntaxes_template>(other_value.list_value.size());
			for(int i = 0; i < other_value.list_value.size(); i++) {
				final TitanExternal_identification_syntaxes_template temp = new TitanExternal_identification_syntaxes_template(other_value.list_value.get(i));
				list_value.add(temp);
			}
			break;
		default:
			throw new TtcnError("Copying an uninitialized template of type EXTERNAL.identification.syntaxes.");
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

	public TitanExternal_identification_syntaxes valueOf() {
		if (templateSelection != template_sel.SPECIFIC_VALUE || is_ifPresent) {
			throw new TtcnError("Performing a valueof or send operation on a non-specific template of type EXTERNAL.identification.syntaxes.");
		}
		TitanExternal_identification_syntaxes ret_val = new TitanExternal_identification_syntaxes();
		if (abstract_.isBound().getValue()) {
			ret_val.abstract_.assign(abstract_.valueOf());
		}
		if (transfer.isBound().getValue()) {
			ret_val.transfer.assign(transfer.valueOf());
		}
		return ret_val;
	}

	public TitanExternal_identification_syntaxes_template listItem(int list_index) {
		if (templateSelection != template_sel.VALUE_LIST && templateSelection != template_sel.COMPLEMENTED_LIST) {
			throw new TtcnError("Accessing a list element of a non-list template of type EXTERNAL.identification.syntaxes.");
		}
		if (list_index >= list_value.size()) {
			throw new TtcnError("Index overflow in a value list template of type EXTERNAL.identification.syntaxes.");
		}
		return list_value.get(list_index);
	}

	public void setType(template_sel template_type, int list_length) {
		if (template_type != template_sel.VALUE_LIST && template_type != template_sel.COMPLEMENTED_LIST) {
			throw new TtcnError("Setting an invalid list for a template of type EXTERNAL.identification.syntaxes.");
		}
		cleanUp();
		setSelection(template_type);
		list_value = new ArrayList<TitanExternal_identification_syntaxes_template>(list_length);
		for(int i = 0 ; i < list_length; i++) {
			list_value.add(new TitanExternal_identification_syntaxes_template());
		}
	}

	public TitanBoolean isBound() {
		if (templateSelection == template_sel.UNINITIALIZED_TEMPLATE && !is_ifPresent) {
			return new TitanBoolean(false);
		}
		if (templateSelection != template_sel.SPECIFIC_VALUE) {
			return new TitanBoolean(true);
		}
		if (abstract_.isBound().getValue()) {
			return new TitanBoolean(true);
		}
		if (transfer.isBound().getValue()) {
			return new TitanBoolean(true);
		}
		return new TitanBoolean(false);
	}

	public TitanBoolean isValue() {
		if (templateSelection != template_sel.SPECIFIC_VALUE || is_ifPresent) {
			return new TitanBoolean(false);
		}
		if (!abstract_.isValue().getValue()) {
			return new TitanBoolean(false);
		}
		if (!transfer.isValue().getValue()) {
			return new TitanBoolean(false);
		}
		return new TitanBoolean(true);
	}

	public TitanBoolean match(TitanExternal_identification_syntaxes other_value) {
		return match(other_value, false);
	}

	public TitanBoolean match(TitanExternal_identification_syntaxes other_value, boolean legacy) {
		return new TitanBoolean(match_(other_value, legacy));
	}

	private boolean match_(TitanExternal_identification_syntaxes other_value, boolean legacy) {
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
			if(!other_value.getAbstract_().isBound().getValue()) {
				return false;
			}
			if(!abstract_.match(other_value.getAbstract_(), legacy).getValue()) {
				return false;
			}
			if(!other_value.getTransfer().isBound().getValue()) {
				return false;
			}
			if(!transfer.match(other_value.getTransfer(), legacy).getValue()) {
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
			throw new TtcnError("Matching an uninitialized/unsupported template of type EXTERNAL.identification.syntaxes.");
		}
	}

	@Override
	public TitanBoolean match(final Base_Type otherValue, final boolean legacy) {
		if (otherValue instanceof TitanExternal_identification_syntaxes) {
			return match((TitanExternal_identification_syntaxes)otherValue, legacy);
		}

		throw new TtcnError("Internal Error: The left operand of assignment is not of type EXTERNAL_identification_syntaxes.");
	}

	public TitanInteger sizeOf() {
		if (is_ifPresent) {
			throw new TtcnError("Performing sizeof() operation on a template of type EXTERNAL.identification.syntaxes which has an ifpresent attribute.");
		}
		switch (templateSelection) {
		case SPECIFIC_VALUE:
			int sizeof = 0;
			sizeof += 2;
			return new TitanInteger(sizeof);
		case VALUE_LIST:
			if (list_value.size() < 1) {
				throw new TtcnError("Internal error: Performing sizeof() operation on a template of type EXTERNAL.identification.syntaxes containing an empty list.");
			}
			int item_size = list_value.get(0).sizeOf().getInt();
			for (int l_idx = 1; l_idx < list_value.size(); l_idx++) {
				if (list_value.get(l_idx).sizeOf().getInt() != item_size) {
					throw new TtcnError("Performing sizeof() operation on a template of type EXTERNAL.identification.syntaxes containing a value list with different sizes.");
				}
			}
			return new TitanInteger(item_size);
		case OMIT_VALUE:
			throw new TtcnError("Performing sizeof() operation on a template of type EXTERNAL.identification.syntaxes containing omit value.");
		case ANY_VALUE:
		case ANY_OR_OMIT:
			throw new TtcnError("Performing sizeof() operation on a template of type EXTERNAL.identification.syntaxes containing */? value.");
		case COMPLEMENTED_LIST:
			throw new TtcnError("Performing sizeof() operation on a template of type EXTERNAL.identification.syntaxes containing complemented list.");
		default:
			throw new TtcnError("Performing sizeof() operation on an uninitialized/unsupported template of type EXTERNAL.identification.syntaxes.");
		}
	}
	public void log() {
		switch (templateSelection) {
		case SPECIFIC_VALUE:
			TtcnLogger.log_char('{');
			TtcnLogger.log_event_str(" abstract_ := ");
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
}