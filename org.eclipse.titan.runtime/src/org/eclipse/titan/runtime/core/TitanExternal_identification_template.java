/******************************************************************************
 * Copyright (c) 2000-2017 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

import java.util.ArrayList;

import org.eclipse.titan.runtime.core.Base_Template;
import org.eclipse.titan.runtime.core.Base_Type;
import org.eclipse.titan.runtime.core.TitanAsn_Null;
import org.eclipse.titan.runtime.core.TitanAsn_Null_template;
import org.eclipse.titan.runtime.core.TitanBoolean;
import org.eclipse.titan.runtime.core.TitanInteger;
import org.eclipse.titan.runtime.core.TitanInteger_template;
import org.eclipse.titan.runtime.core.TitanObjectid;
import org.eclipse.titan.runtime.core.TitanObjectid_template;
import org.eclipse.titan.runtime.core.TtcnError;
import org.eclipse.titan.runtime.core.TtcnLogger;

/**
 * Part of the representation of the ASN.1 EXTERNAL type
 * 
 * @author Kristof Szabados
 */
public class TitanExternal_identification_template extends Base_Template {
	//if single value which value?
	private TitanExternal_identification.union_selection_type single_value_union_selection;
	//originally a union which can not be mapped to Java
	private Base_Template single_value;
	// value_list part
	private ArrayList<TitanExternal_identification_template> value_list;

	private void copy_value(final TitanExternal_identification other_value) {
		single_value_union_selection = other_value.union_selection;
		switch (other_value.union_selection) {
		case ALT_Syntaxes:
			single_value = new TitanExternal_identification_syntaxes_template((TitanExternal_identification_syntaxes)other_value.field);
			break;
		case ALT_Syntax:
			single_value = new TitanObjectid_template((TitanObjectid)other_value.field);
			break;
		case ALT_Presentation__context__id:
			single_value = new TitanInteger_template((TitanInteger)other_value.field);
			break;
		case ALT_Context__negotiation:
			single_value = new TitanExternal_identification_context__negotiation_template((TitanExternal_identification_context__negotiation)other_value.field);
			break;
		case ALT_Transfer__syntax:
			single_value = new TitanObjectid_template((TitanObjectid)other_value.field);
			break;
		case ALT_Fixed:
			single_value = new TitanAsn_Null_template((TitanAsn_Null)other_value.field);
			break;
		default:
			throw new TtcnError("Initializing a template with an unbound value of type EXTERNAL.identification.");
		}
		setSelection(template_sel.SPECIFIC_VALUE);
	}
	private void copy_template(final TitanExternal_identification_template other_value) {
		switch (other_value.templateSelection) {
		case SPECIFIC_VALUE:
			single_value_union_selection = other_value.single_value_union_selection;
			switch (single_value_union_selection) {
			case ALT_Syntaxes:
				single_value = new TitanExternal_identification_syntaxes_template(other_value.getSyntaxes());
				break;
			case ALT_Syntax:
				single_value = new TitanObjectid_template(other_value.getSyntax());
				break;
			case ALT_Presentation__context__id:
				single_value = new TitanInteger_template(other_value.getPresentation__context__id());
				break;
			case ALT_Context__negotiation:
				single_value = new TitanExternal_identification_context__negotiation_template(other_value.getContext__negotiation());
				break;
			case ALT_Transfer__syntax:
				single_value = new TitanObjectid_template(other_value.getTransfer__syntax());
				break;
			case ALT_Fixed:
				single_value = new TitanAsn_Null_template(other_value.getFixed());
				break;
			default:
				throw new TtcnError("Internal error: Invalid union selector in a specific value when copying a template of type EXTERNAL.identification.");
			}
			break;
		case OMIT_VALUE:
		case ANY_VALUE:
		case ANY_OR_OMIT:
			break;
		case VALUE_LIST:
		case COMPLEMENTED_LIST:
			value_list = new ArrayList<TitanExternal_identification_template>(other_value.value_list.size());
			for(int i = 0; i < other_value.value_list.size(); i++) {
				final TitanExternal_identification_template temp = new TitanExternal_identification_template(other_value.value_list.get(i));
				value_list.add(temp);
			}
			break;
		default:
			throw new TtcnError("Copying an uninitialized template of union type EXTERNAL.identification.");
		}
		setSelection(other_value);
	}

	public TitanExternal_identification_template() {
	}
	public TitanExternal_identification_template(final template_sel other_value) {
		super(other_value);
		checkSingleSelection(other_value);
	}
	public TitanExternal_identification_template(final TitanExternal_identification other_value) {
		copy_value(other_value);
	}
	public TitanExternal_identification_template(final TitanExternal_identification_template other_value) {
		copy_template(other_value);
	}

	@Override
	public void cleanUp() {
		switch(templateSelection) {
		case SPECIFIC_VALUE:
			switch(single_value_union_selection) {
			case ALT_Syntaxes:
				((TitanExternal_identification_syntaxes_template)single_value).cleanUp();
				break;
			case ALT_Syntax:
				((TitanObjectid_template)single_value).cleanUp();
				break;
			case ALT_Presentation__context__id:
				((TitanInteger_template)single_value).cleanUp();
				break;
			case ALT_Context__negotiation:
				((TitanExternal_identification_context__negotiation_template)single_value).cleanUp();
				break;
			case ALT_Transfer__syntax:
				((TitanObjectid_template)single_value).cleanUp();
				break;
			case ALT_Fixed:
				((TitanAsn_Null_template)single_value).cleanUp();
				break;
			default:
				break;
			}
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

	//originally operator=
	public TitanExternal_identification_template assign( final template_sel other_value ) {
		checkSingleSelection(other_value);
		cleanUp();
		setSelection(other_value);
		return this;
	}

	//originally operator=
	public TitanExternal_identification_template assign( final TitanExternal_identification other_value ) {
		cleanUp();
		copy_value(other_value);
		return this;
	}

	//originally operator=
	public TitanExternal_identification_template assign( final TitanExternal_identification_template other_value ) {
		if (other_value != this) {
			cleanUp();
			copy_template(other_value);
		}
		return this;
	}

	@Override
	public TitanExternal_identification_template assign( final Base_Type otherValue ) {
		if (otherValue instanceof TitanExternal_identification) {
			return assign((TitanExternal_identification)otherValue);
		}
		throw new TtcnError("Internal Error: value can not be cast to EXTERNAL_identification.");
	}

	@Override
	public TitanExternal_identification_template assign( final Base_Template otherValue ) {
		if (otherValue instanceof TitanExternal_identification_template) {
			return assign((TitanExternal_identification_template)otherValue);
		}
		throw new TtcnError("Internal Error: value can not be cast to EXTERNAL_identification_template.");
	}

	// originally match
	public TitanBoolean match(final TitanExternal_identification other_value) {
		return match(other_value, false);
	}

	// originally match
	public TitanBoolean match(final TitanExternal_identification other_value, final boolean legacy) {
		if(!other_value.isBound().getValue()) {
			return new TitanBoolean(false);
		}
		switch (templateSelection) {
		case ANY_VALUE:
		case ANY_OR_OMIT:
			return new TitanBoolean(true);
		case OMIT_VALUE:
			return new TitanBoolean(false);
		case SPECIFIC_VALUE:
			TitanExternal_identification.union_selection_type value_selection = other_value.get_selection();
			if (value_selection == TitanExternal_identification.union_selection_type.UNBOUND_VALUE) {
				return new TitanBoolean(false);
			}
			if (value_selection != single_value_union_selection) {
				return new TitanBoolean(false);
			}
			switch(value_selection) {
			case ALT_Syntaxes:
				return ((TitanExternal_identification_syntaxes_template)single_value).match(other_value.getSyntaxes(), legacy);
			case ALT_Syntax:
				return ((TitanObjectid_template)single_value).match(other_value.getSyntax(), legacy);
			case ALT_Presentation__context__id:
				return ((TitanInteger_template)single_value).match(other_value.getPresentation__context__id(), legacy);
			case ALT_Context__negotiation:
				return ((TitanExternal_identification_context__negotiation_template)single_value).match(other_value.getContext__negotiation(), legacy);
			case ALT_Transfer__syntax:
				return ((TitanObjectid_template)single_value).match(other_value.getTransfer__syntax(), legacy);
			case ALT_Fixed:
				return ((TitanAsn_Null_template)single_value).match(other_value.getFixed(), legacy);
			default:
				throw new TtcnError("Internal error: Invalid selector in a specific value when matching a template of union type EXTERNAL.identification.");
			}
		case VALUE_LIST:
		case COMPLEMENTED_LIST:
			for(int i = 0 ; i < value_list.size(); i++) {
				if(value_list.get(i).match(other_value, legacy).getValue()) {
					return new TitanBoolean(templateSelection == template_sel.VALUE_LIST);
				}
			}
			return new TitanBoolean(templateSelection == template_sel.COMPLEMENTED_LIST);
		default:
			throw new TtcnError("Matching with an uninitialized/unsupported integer template.");
		}
	}

	@Override
	public TitanBoolean match(final Base_Type otherValue, final boolean legacy) {
		if (otherValue instanceof TitanExternal_identification) {
			return match((TitanExternal_identification)otherValue, legacy);
		}

		throw new TtcnError("Internal Error: The left operand of assignment is not of type EXTERNAL_identification.");
	}
	public boolean isChosen(final TitanExternal_identification.union_selection_type checked_selection) {
		if(checked_selection == TitanExternal_identification.union_selection_type.UNBOUND_VALUE) {
			throw new TtcnError("Internal error: Performing ischosen() operation on an invalid field of union type EXTERNAL.identification.");
		}
		switch(templateSelection) {
		case SPECIFIC_VALUE:
			if (single_value_union_selection == TitanExternal_identification.union_selection_type.UNBOUND_VALUE) {
				throw new TtcnError("Internal error: Invalid selector in a specific value when performing ischosen() operation on a template of union type EXTERNAL.identification.");
			}
			return single_value_union_selection == checked_selection;
		case VALUE_LIST:
			if (value_list.size() < 1) {
				throw new TtcnError("Internal error: Performing ischosen() operation on a template of union type EXTERNAL.identification containing an empty list.");
			}
			for (int i = 0; i < value_list.size(); i++) {
				if(!value_list.get(i).isChosen(checked_selection)) {
					return false;
				}
			}
			return true;
		case ANY_VALUE:
		case ANY_OR_OMIT:
		case OMIT_VALUE:
		case COMPLEMENTED_LIST:
			throw new TtcnError("Performing ischosen() operation on a template of union type EXTERNAL.identification, which does not determine unambiguously the chosen field of the matching values.");
		default:
			throw new TtcnError("Performing ischosen() operation on an uninitialized template of union type EXTERNAL.identification.");
		}
	}

	@Override
	public TitanBoolean isValue() {
		if (templateSelection != template_sel.SPECIFIC_VALUE || is_ifPresent) {
			return new TitanBoolean(false);
		}
		switch(single_value_union_selection) {
		case ALT_Syntaxes:
			return ((TitanExternal_identification_syntaxes_template)single_value).isValue();
		case ALT_Syntax:
			return ((TitanObjectid_template)single_value).isValue();
		case ALT_Presentation__context__id:
			return ((TitanInteger_template)single_value).isValue();
		case ALT_Context__negotiation:
			return ((TitanExternal_identification_context__negotiation_template)single_value).isValue();
		case ALT_Transfer__syntax:
			return ((TitanObjectid_template)single_value).isValue();
		case ALT_Fixed:
			return ((TitanAsn_Null_template)single_value).isValue();
		default:
			throw new TtcnError("Internal error: Invalid selector in a specific value when performing is_value operation on a template of union type EXTERNAL.identification.");
		}
	}

	public TitanExternal_identification valueOf() {
		if (templateSelection != template_sel.SPECIFIC_VALUE || is_ifPresent) {
			throw new TtcnError("Performing a valueof or send operation on a non-specific template of union type EXTERNAL.identification.");
		}
		TitanExternal_identification ret_val = new TitanExternal_identification();
		switch(single_value_union_selection) {
		case ALT_Syntaxes:
			ret_val.getSyntaxes().assign(((TitanExternal_identification_syntaxes_template)single_value).valueOf());
			break;
		case ALT_Syntax:
			ret_val.getSyntax().assign(((TitanObjectid_template)single_value).valueOf());
			break;
		case ALT_Presentation__context__id:
			ret_val.getPresentation__context__id().assign(((TitanInteger_template)single_value).valueOf());
			break;
		case ALT_Context__negotiation:
			ret_val.getContext__negotiation().assign(((TitanExternal_identification_context__negotiation_template)single_value).valueOf());
			break;
		case ALT_Transfer__syntax:
			ret_val.getTransfer__syntax().assign(((TitanObjectid_template)single_value).valueOf());
			break;
		case ALT_Fixed:
			ret_val.getFixed().assign(((TitanAsn_Null_template)single_value).valueOf());
			break;
		default:
			throw new TtcnError("Internal error: Invalid selector in a specific value when performing valueof operation on a template of union type EXTERNAL.identification.");
		}
		return ret_val;
	}

	public void setType(template_sel template_type, int list_length) {
		if (template_type != template_sel.VALUE_LIST && template_type != template_sel.COMPLEMENTED_LIST) {
			throw new TtcnError("Internal error: Setting an invalid list for a template of union type EXTERNAL.identification.");
		}
		cleanUp();
		setSelection(template_type);
		value_list = new ArrayList<TitanExternal_identification_template>(list_length);
		for(int i = 0 ; i < list_length; i++) {
			value_list.add(new TitanExternal_identification_template());
		}
	}

	public TitanExternal_identification_template listItem(int list_index)  {
		if (templateSelection != template_sel.VALUE_LIST && templateSelection != template_sel.COMPLEMENTED_LIST) {
			throw new TtcnError("Internal error: Accessing a list element of a non-list template of union type EXTERNAL.identification.");
		}
		if (list_index < 0) {
			throw new TtcnError("Internal error: Index underflow in a value list template of union type EXTERNAL.identification.");
		}
		if(list_index >= value_list.size()) {
			throw new TtcnError("Internal error: Index overflow in a value list template of union type EXTERNAL.identification.");
		}
		return value_list.get(list_index);
	}

	public TitanBoolean isPresent() {
		return isPresent(false);
	}

	public TitanBoolean isPresent(final boolean legacy) {
		if (templateSelection == template_sel.UNINITIALIZED_TEMPLATE) {
			return new TitanBoolean(false);
		}
		return new TitanBoolean(!match_omit(legacy).getValue());
	}

	public TitanBoolean match_omit() {
		return match_omit(false);
	}

	public TitanBoolean match_omit(final boolean legacy) {
		if (is_ifPresent) {
			return new TitanBoolean(true);
		}
		switch(templateSelection) {
		case OMIT_VALUE:
		case ANY_OR_OMIT:
			return new TitanBoolean(true);
		case VALUE_LIST:
		case COMPLEMENTED_LIST:
			if (legacy) {
				for (int i = 0 ; i < value_list.size(); i++) {
					if (value_list.get(i).match_omit(legacy).getValue()) {
						return new TitanBoolean(templateSelection == template_sel.VALUE_LIST);
					}
				}
				return new TitanBoolean(templateSelection == template_sel.COMPLEMENTED_LIST);
			}
			return new TitanBoolean(false);
		default:
			return new TitanBoolean(false);
		}
	}

	public TitanExternal_identification_syntaxes_template getSyntaxes() {
		if (templateSelection != template_sel.SPECIFIC_VALUE || single_value_union_selection != TitanExternal_identification.union_selection_type.ALT_Syntaxes) {
			template_sel old_selection = templateSelection;
			cleanUp();
			if (old_selection == template_sel.ANY_VALUE || old_selection == template_sel.ANY_OR_OMIT) {
				single_value = new TitanExternal_identification_syntaxes_template(template_sel.ANY_VALUE);
			} else {
				single_value = new TitanExternal_identification_syntaxes_template();
			}
			single_value_union_selection = TitanExternal_identification.union_selection_type.ALT_Syntaxes;
			setSelection(template_sel.SPECIFIC_VALUE);
		}
		return (TitanExternal_identification_syntaxes_template)single_value;
	}

	public TitanExternal_identification_syntaxes_template constGetSyntaxes() {
		if (templateSelection != template_sel.SPECIFIC_VALUE) {
			throw new TtcnError("Accessing field field1 in a non-specific template of union type EXTERNAL.identification.");
		}
		if (single_value_union_selection != TitanExternal_identification.union_selection_type.ALT_Syntaxes) {
			throw new TtcnError("Accessing non-selected field field1 in a template of union type EXTERNAL.identification.");
		}
		return (TitanExternal_identification_syntaxes_template)single_value;
	}

	public TitanObjectid_template getSyntax() {
		if (templateSelection != template_sel.SPECIFIC_VALUE || single_value_union_selection != TitanExternal_identification.union_selection_type.ALT_Syntax) {
			template_sel old_selection = templateSelection;
			cleanUp();
			if (old_selection == template_sel.ANY_VALUE || old_selection == template_sel.ANY_OR_OMIT) {
				single_value = new TitanObjectid_template(template_sel.ANY_VALUE);
			} else {
				single_value = new TitanObjectid_template();
			}
			single_value_union_selection = TitanExternal_identification.union_selection_type.ALT_Syntax;
			setSelection(template_sel.SPECIFIC_VALUE);
		}
		return (TitanObjectid_template)single_value;
	}

	public TitanObjectid_template constGetSyntax() {
		if (templateSelection != template_sel.SPECIFIC_VALUE) {
			throw new TtcnError("Accessing field field1 in a non-specific template of union type EXTERNAL.identification.");
		}
		if (single_value_union_selection != TitanExternal_identification.union_selection_type.ALT_Syntax) {
			throw new TtcnError("Accessing non-selected field field1 in a template of union type EXTERNAL.identification.");
		}
		return (TitanObjectid_template)single_value;
	}

	public TitanInteger_template getPresentation__context__id() {
		if (templateSelection != template_sel.SPECIFIC_VALUE || single_value_union_selection != TitanExternal_identification.union_selection_type.ALT_Presentation__context__id) {
			template_sel old_selection = templateSelection;
			cleanUp();
			if (old_selection == template_sel.ANY_VALUE || old_selection == template_sel.ANY_OR_OMIT) {
				single_value = new TitanInteger_template(template_sel.ANY_VALUE);
			} else {
				single_value = new TitanInteger_template();
			}
			single_value_union_selection = TitanExternal_identification.union_selection_type.ALT_Presentation__context__id;
			setSelection(template_sel.SPECIFIC_VALUE);
		}
		return (TitanInteger_template)single_value;
	}

	public TitanInteger_template constGetPresentation__context__id() {
		if (templateSelection != template_sel.SPECIFIC_VALUE) {
			throw new TtcnError("Accessing field field1 in a non-specific template of union type EXTERNAL.identification.");
		}
		if (single_value_union_selection != TitanExternal_identification.union_selection_type.ALT_Presentation__context__id) {
			throw new TtcnError("Accessing non-selected field field1 in a template of union type EXTERNAL.identification.");
		}
		return (TitanInteger_template)single_value;
	}

	public TitanExternal_identification_context__negotiation_template getContext__negotiation() {
		if (templateSelection != template_sel.SPECIFIC_VALUE || single_value_union_selection != TitanExternal_identification.union_selection_type.ALT_Context__negotiation) {
			template_sel old_selection = templateSelection;
			cleanUp();
			if (old_selection == template_sel.ANY_VALUE || old_selection == template_sel.ANY_OR_OMIT) {
				single_value = new TitanExternal_identification_context__negotiation_template(template_sel.ANY_VALUE);
			} else {
				single_value = new TitanExternal_identification_context__negotiation_template();
			}
			single_value_union_selection = TitanExternal_identification.union_selection_type.ALT_Context__negotiation;
			setSelection(template_sel.SPECIFIC_VALUE);
		}
		return (TitanExternal_identification_context__negotiation_template)single_value;
	}

	public TitanExternal_identification_context__negotiation_template constGetContext__negotiation() {
		if (templateSelection != template_sel.SPECIFIC_VALUE) {
			throw new TtcnError("Accessing field field1 in a non-specific template of union type EXTERNAL.identification.");
		}
		if (single_value_union_selection != TitanExternal_identification.union_selection_type.ALT_Context__negotiation) {
			throw new TtcnError("Accessing non-selected field field1 in a template of union type EXTERNAL.identification.");
		}
		return (TitanExternal_identification_context__negotiation_template)single_value;
	}

	public TitanObjectid_template getTransfer__syntax() {
		if (templateSelection != template_sel.SPECIFIC_VALUE || single_value_union_selection != TitanExternal_identification.union_selection_type.ALT_Transfer__syntax) {
			template_sel old_selection = templateSelection;
			cleanUp();
			if (old_selection == template_sel.ANY_VALUE || old_selection == template_sel.ANY_OR_OMIT) {
				single_value = new TitanObjectid_template(template_sel.ANY_VALUE);
			} else {
				single_value = new TitanObjectid_template();
			}
			single_value_union_selection = TitanExternal_identification.union_selection_type.ALT_Transfer__syntax;
			setSelection(template_sel.SPECIFIC_VALUE);
		}
		return (TitanObjectid_template)single_value;
	}

	public TitanObjectid_template constGetTransfer__syntax() {
		if (templateSelection != template_sel.SPECIFIC_VALUE) {
			throw new TtcnError("Accessing field field1 in a non-specific template of union type EXTERNAL.identification.");
		}
		if (single_value_union_selection != TitanExternal_identification.union_selection_type.ALT_Transfer__syntax) {
			throw new TtcnError("Accessing non-selected field field1 in a template of union type EXTERNAL.identification.");
		}
		return (TitanObjectid_template)single_value;
	}

	public TitanAsn_Null_template getFixed() {
		if (templateSelection != template_sel.SPECIFIC_VALUE || single_value_union_selection != TitanExternal_identification.union_selection_type.ALT_Fixed) {
			template_sel old_selection = templateSelection;
			cleanUp();
			if (old_selection == template_sel.ANY_VALUE || old_selection == template_sel.ANY_OR_OMIT) {
				single_value = new TitanAsn_Null_template(template_sel.ANY_VALUE);
			} else {
				single_value = new TitanAsn_Null_template();
			}
			single_value_union_selection = TitanExternal_identification.union_selection_type.ALT_Fixed;
			setSelection(template_sel.SPECIFIC_VALUE);
		}
		return (TitanAsn_Null_template)single_value;
	}

	public TitanAsn_Null_template constGetFixed() {
		if (templateSelection != template_sel.SPECIFIC_VALUE) {
			throw new TtcnError("Accessing field field1 in a non-specific template of union type EXTERNAL.identification.");
		}
		if (single_value_union_selection != TitanExternal_identification.union_selection_type.ALT_Fixed) {
			throw new TtcnError("Accessing non-selected field field1 in a template of union type EXTERNAL.identification.");
		}
		return (TitanAsn_Null_template)single_value;
	}

	public void log() {
		switch (templateSelection) {
		case SPECIFIC_VALUE:
			single_value.log();
			break;
		case COMPLEMENTED_LIST:
			TtcnLogger.log_event_str("complement ");
		case VALUE_LIST:
			TtcnLogger.log_char('(');
			for (int list_count = 0; list_count < value_list.size(); list_count++) {
				if (list_count > 0) {
					TtcnLogger.log_event_str(", ");
				}
				value_list.get(list_count).log();
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