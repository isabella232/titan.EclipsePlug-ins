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

import org.eclipse.titan.runtime.core.Param_Types.Module_Param_Name;
import org.eclipse.titan.runtime.core.Param_Types.Module_Parameter;

/**
 * Part of the representation of the ASN.1 EMBEDDED PDV type
 *
 * @author Kristof Szabados
 */
public class TitanEmbedded_PDV_identification_template extends Base_Template {
	//if single value which value?
	private TitanEmbedded_PDV_identification.union_selection_type single_value_union_selection;
	//originally a union which can not be mapped to Java
	private Base_Template single_value;
	// value_list part
	private ArrayList<TitanEmbedded_PDV_identification_template> value_list;

	private void copy_value(final TitanEmbedded_PDV_identification other_value) {
		single_value_union_selection = other_value.get_selection();
		switch (other_value.get_selection()) {
		case ALT_syntaxes:
			single_value = new TitanEmbedded_PDV_identification_syntaxes_template(other_value.constGetsyntaxes());
			break;
		case ALT_syntax:
			single_value = new TitanObjectid_template(other_value.constGetsyntax());
			break;
		case ALT_presentation__context__id:
			single_value = new TitanInteger_template(other_value.constGetpresentation__context__id());
			break;
		case ALT_context__negotiation:
			single_value = new TitanEmbedded_PDV_identification_context__negotiation_template(other_value.constGetcontext__negotiation());
			break;
		case ALT_transfer__syntax:
			single_value = new TitanObjectid_template(other_value.constGettransfer__syntax());
			break;
		case ALT_fixed:
			single_value = new TitanAsn_Null_template(other_value.constGetfixed());
			break;
		default:
			throw new TtcnError("Initializing a template with an unbound value of type EMBEDDED PDV.identification.");
		}
		set_selection(template_sel.SPECIFIC_VALUE);
	}
	private void copy_template(final TitanEmbedded_PDV_identification_template other_value) {
		switch (other_value.template_selection) {
		case SPECIFIC_VALUE:
			single_value_union_selection = other_value.single_value_union_selection;
			switch (single_value_union_selection) {
			case ALT_syntaxes:
				single_value = new TitanEmbedded_PDV_identification_syntaxes_template(other_value.constGetsyntaxes());
				break;
			case ALT_syntax:
				single_value = new TitanObjectid_template(other_value.constGetsyntax());
				break;
			case ALT_presentation__context__id:
				single_value = new TitanInteger_template(other_value.constGetpresentation__context__id());
				break;
			case ALT_context__negotiation:
				single_value = new TitanEmbedded_PDV_identification_context__negotiation_template(other_value.constGetcontext__negotiation());
				break;
			case ALT_transfer__syntax:
				single_value = new TitanObjectid_template(other_value.constGettransfer__syntax());
				break;
			case ALT_fixed:
				single_value = new TitanAsn_Null_template(other_value.constGetfixed());
				break;
			default:
				throw new TtcnError("Internal error: Invalid union selector in a specific value when copying a template of type EMBEDDED PDV.identification.");
			}
			break;
		case OMIT_VALUE:
		case ANY_VALUE:
		case ANY_OR_OMIT:
			break;
		case VALUE_LIST:
		case COMPLEMENTED_LIST:
			value_list = new ArrayList<TitanEmbedded_PDV_identification_template>(other_value.value_list.size());
			for(int i = 0; i < other_value.value_list.size(); i++) {
				final TitanEmbedded_PDV_identification_template temp = new TitanEmbedded_PDV_identification_template(other_value.value_list.get(i));
				value_list.add(temp);
			}
			break;
		default:
			throw new TtcnError("Copying an uninitialized template of union type EMBEDDED PDV.identification.");
		}
		set_selection(other_value);
	}

	/**
	 * Initializes to unbound/uninitialized template.
	 * */
	public TitanEmbedded_PDV_identification_template() {
		// do nothing
	}
	/**
	 * Initializes to a given template kind.
	 *
	 * @param other_value
	 *                the template kind to initialize to.
	 * */
	public TitanEmbedded_PDV_identification_template(final template_sel other_value) {
		super(other_value);
		check_single_selection(other_value);
	}
	/**
	 * Initializes to a given value.
	 * The template becomes a specific template and the elements of the provided value are copied.
	 *
	 * @param other_value
	 *                the value to initialize to.
	 * */
	public TitanEmbedded_PDV_identification_template(final TitanEmbedded_PDV_identification other_value) {
		copy_value(other_value);
	}
	/**
	 * Initializes to a given template.
	 * The elements of the provided template are copied.
	 *
	 * @param other_value
	 *                the value to initialize to.
	 * */
	public TitanEmbedded_PDV_identification_template(final TitanEmbedded_PDV_identification_template other_value) {
		copy_template(other_value);
	}

	@Override
	public void clean_up() {
		switch (template_selection) {
		case SPECIFIC_VALUE:
			switch (single_value_union_selection) {
			case ALT_syntaxes:
				((TitanEmbedded_PDV_identification_syntaxes_template)single_value).clean_up();
				break;
			case ALT_syntax:
				((TitanObjectid_template)single_value).clean_up();
				break;
			case ALT_presentation__context__id:
				((TitanInteger_template)single_value).clean_up();
				break;
			case ALT_context__negotiation:
				((TitanEmbedded_PDV_identification_context__negotiation_template)single_value).clean_up();
				break;
			case ALT_transfer__syntax:
				((TitanObjectid_template)single_value).clean_up();
				break;
			case ALT_fixed:
				((TitanAsn_Null_template)single_value).clean_up();
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
		template_selection = template_sel.UNINITIALIZED_TEMPLATE;
	}

	@Override
	public TitanEmbedded_PDV_identification_template assign(final template_sel otherValue ) {
		check_single_selection(otherValue);
		clean_up();
		set_selection(otherValue);
		return this;
	}

	/**
	 * Assigns the other value to this template.
	 * Overwriting the current content in the process.
	 *<p>
	 * operator= in the core.
	 *
	 * @param otherValue
	 *                the other value to assign.
	 * @return the new template object.
	 */
	public TitanEmbedded_PDV_identification_template assign(final TitanEmbedded_PDV_identification otherValue ) {
		clean_up();
		copy_value(otherValue);
		return this;
	}

	/**
	 * Assigns the other template to this template.
	 * Overwriting the current content in the process.
	 *<p>
	 * operator= in the core.
	 *
	 * @param otherValue
	 *                the other value to assign.
	 * @return the new template object.
	 */
	public TitanEmbedded_PDV_identification_template assign(final TitanEmbedded_PDV_identification_template otherValue ) {
		if (otherValue != this) {
			clean_up();
			copy_template(otherValue);
		}
		return this;
	}

	@Override
	public TitanEmbedded_PDV_identification_template assign(final Base_Type otherValue ) {
		if (otherValue instanceof TitanEmbedded_PDV_identification) {
			return assign((TitanEmbedded_PDV_identification)otherValue);
		}
		throw new TtcnError("Internal Error: value can not be cast to TitanEmbedded_PDV_identification.");
	}

	@Override
	public TitanEmbedded_PDV_identification_template assign(final Base_Template otherValue ) {
		if (otherValue instanceof TitanEmbedded_PDV_identification_template) {
			return assign((TitanEmbedded_PDV_identification_template)otherValue);
		}
		throw new TtcnError("Internal Error: value can not be cast to TitanEmbedded_PDV_identification_template.");
	}

	/**
	 * Matches the provided value against this template.
	 *
	 * @param other_value the value to be matched.
	 * */
	public boolean match(final TitanEmbedded_PDV_identification other_value) {
		return match(other_value, false);
	}

	/**
	 * Matches the provided value against this template. In legacy mode
	 * omitted value fields are not matched against the template field.
	 *
	 * @param other_value
	 *                the value to be matched.
	 * @param legacy
	 *                use legacy mode.
	 * */
	public boolean match(final TitanEmbedded_PDV_identification other_value, final boolean legacy) {
		if(!other_value.is_bound()) {
			return false;
		}
		switch (template_selection) {
		case ANY_VALUE:
		case ANY_OR_OMIT:
			return true;
		case OMIT_VALUE:
			return false;
		case SPECIFIC_VALUE:
			final TitanEmbedded_PDV_identification.union_selection_type value_selection = other_value.get_selection();
			if (value_selection == TitanEmbedded_PDV_identification.union_selection_type.UNBOUND_VALUE) {
				return false;
			}
			if (value_selection != single_value_union_selection) {
				return false;
			}
			switch (value_selection) {
			case ALT_syntaxes:
				return ((TitanEmbedded_PDV_identification_syntaxes_template)single_value).match(other_value.getsyntaxes(), legacy);
			case ALT_syntax:
				return ((TitanObjectid_template)single_value).match(other_value.getsyntax(), legacy);
			case ALT_presentation__context__id:
				return ((TitanInteger_template)single_value).match(other_value.getpresentation__context__id(), legacy);
			case ALT_context__negotiation:
				return ((TitanEmbedded_PDV_identification_context__negotiation_template)single_value).match(other_value.getcontext__negotiation(), legacy);
			case ALT_transfer__syntax:
				return ((TitanObjectid_template)single_value).match(other_value.gettransfer__syntax(), legacy);
			case ALT_fixed:
				return ((TitanAsn_Null_template)single_value).match(other_value.getfixed(), legacy);
			default:
				throw new TtcnError("Internal error: Invalid selector in a specific value when matching a template of union type EMBEDDED PDV.identification.");
			}
		case VALUE_LIST:
		case COMPLEMENTED_LIST:
			for(int i = 0 ; i < value_list.size(); i++) {
				if(value_list.get(i).match(other_value, legacy)) {
					return template_selection == template_sel.VALUE_LIST;
				}
			}
			return template_selection == template_sel.COMPLEMENTED_LIST;
		default:
			throw new TtcnError("Matching with an uninitialized/unsupported integer template.");
		}
	}

	@Override
	public boolean match(final Base_Type otherValue, final boolean legacy) {
		if (otherValue instanceof TitanEmbedded_PDV_identification) {
			return match((TitanEmbedded_PDV_identification)otherValue, legacy);
		}

		throw new TtcnError("Internal Error: The left operand of assignment is not of type TitanEmbedded_PDV_identification.");
	}

	public boolean ischosen(final TitanEmbedded_PDV_identification.union_selection_type checked_selection) {
		if(checked_selection == TitanEmbedded_PDV_identification.union_selection_type.UNBOUND_VALUE) {
			throw new TtcnError("Internal error: Performing ischosen() operation on an invalid field of union type EMBEDDED PDV.identification.");
		}
		switch (template_selection) {
		case SPECIFIC_VALUE:
			if (single_value_union_selection == TitanEmbedded_PDV_identification.union_selection_type.UNBOUND_VALUE) {
				throw new TtcnError("Internal error: Invalid selector in a specific value when performing ischosen() operation on a template of union type EMBEDDED PDV.identification.");
			}
			return single_value_union_selection == checked_selection;
		case VALUE_LIST:
			if (value_list.isEmpty()) {
				throw new TtcnError("Internal error: Performing ischosen() operation on a template of union type EMBEDDED PDV.identification containing an empty list.");
			}
			for (int i = 0; i < value_list.size(); i++) {
				if(!value_list.get(i).ischosen(checked_selection)) {
					return false;
				}
			}
			return true;
		default:
			return false;
		}
	}

	@Override
	public boolean is_value() {
		if (template_selection != template_sel.SPECIFIC_VALUE || is_ifPresent) {
			return false;
		}
		switch (single_value_union_selection) {
		case ALT_syntaxes:
			return ((TitanEmbedded_PDV_identification_syntaxes_template)single_value).is_value();
		case ALT_syntax:
			return ((TitanObjectid_template)single_value).is_value();
		case ALT_presentation__context__id:
			return ((TitanInteger_template)single_value).is_value();
		case ALT_context__negotiation:
			return ((TitanEmbedded_PDV_identification_context__negotiation_template)single_value).is_value();
		case ALT_transfer__syntax:
			return ((TitanObjectid_template)single_value).is_value();
		case ALT_fixed:
			return ((TitanAsn_Null_template)single_value).is_value();
		default:
			throw new TtcnError("Internal error: Invalid selector in a specific value when performing is_value operation on a template of union type EMBEDDED PDV.identification.");
		}
	}

	@Override
	public TitanEmbedded_PDV_identification valueof() {
		if (template_selection != template_sel.SPECIFIC_VALUE || is_ifPresent) {
			throw new TtcnError("Performing a valueof or send operation on a non-specific template of union type EMBEDDED PDV.identification.");
		}
		final TitanEmbedded_PDV_identification ret_val = new TitanEmbedded_PDV_identification();
		switch (single_value_union_selection) {
		case ALT_syntaxes:
			ret_val.getsyntaxes().assign(((TitanEmbedded_PDV_identification_syntaxes_template)single_value).valueof());
			break;
		case ALT_syntax:
			ret_val.getsyntax().assign(((TitanObjectid_template)single_value).valueof());
			break;
		case ALT_presentation__context__id:
			ret_val.getpresentation__context__id().assign(((TitanInteger_template)single_value).valueof());
			break;
		case ALT_context__negotiation:
			ret_val.getcontext__negotiation().assign(((TitanEmbedded_PDV_identification_context__negotiation_template)single_value).valueof());
			break;
		case ALT_transfer__syntax:
			ret_val.gettransfer__syntax().assign(((TitanObjectid_template)single_value).valueof());
			break;
		case ALT_fixed:
			ret_val.getfixed().assign(((TitanAsn_Null_template)single_value).valueof());
			break;
		default:
			throw new TtcnError("Internal error: Invalid selector in a specific value when performing valueof operation on a template of union type EMBEDDED PDV.identification.");
		}
		return ret_val;
	}

	@Override
	public void set_type(final template_sel template_type, final int list_length) {
		if (template_type != template_sel.VALUE_LIST && template_type != template_sel.COMPLEMENTED_LIST) {
			throw new TtcnError("Internal error: Setting an invalid list for a template of union type EMBEDDED PDV.identification.");
		}
		clean_up();
		set_selection(template_type);
		value_list = new ArrayList<TitanEmbedded_PDV_identification_template>(list_length);
		for(int i = 0 ; i < list_length; i++) {
			value_list.add(new TitanEmbedded_PDV_identification_template());
		}
	}

	@Override
	public TitanEmbedded_PDV_identification_template list_item(final int list_index)  {
		if (template_selection != template_sel.VALUE_LIST && template_selection != template_sel.COMPLEMENTED_LIST) {
			throw new TtcnError("Internal error: Accessing a list element of a non-list template of union type EMBEDDED PDV.identification.");
		}
		if (list_index < 0) {
			throw new TtcnError("Internal error: Index underflow in a value list template of union type EMBEDDED PDV.identification.");
		}
		if(list_index >= value_list.size()) {
			throw new TtcnError("Internal error: Index overflow in a value list template of union type EMBEDDED PDV.identification.");
		}
		return value_list.get(list_index);
	}

	@Override
	public boolean match_omit(final boolean legacy) {
		if (is_ifPresent) {
			return true;
		}
		switch (template_selection) {
		case OMIT_VALUE:
		case ANY_OR_OMIT:
			return true;
		case VALUE_LIST:
		case COMPLEMENTED_LIST:
			if (legacy) {
				for (int i = 0 ; i < value_list.size(); i++) {
					if (value_list.get(i).match_omit(legacy)) {
						return template_selection == template_sel.VALUE_LIST;
					}
				}
				return template_selection == template_sel.COMPLEMENTED_LIST;
			}
			return false;
		default:
			return false;
		}
	}

	/**
	 * Selects and gives access to field syntaxes.
	 * If other field was previously selected, its value will be destroyed.
	 *
	 * @return field syntaxes.
	 * */
	public TitanEmbedded_PDV_identification_syntaxes_template getsyntaxes() {
		if (template_selection != template_sel.SPECIFIC_VALUE || single_value_union_selection != TitanEmbedded_PDV_identification.union_selection_type.ALT_syntaxes) {
			final template_sel old_selection = template_selection;
			clean_up();
			if (old_selection == template_sel.ANY_VALUE || old_selection == template_sel.ANY_OR_OMIT) {
				single_value = new TitanEmbedded_PDV_identification_syntaxes_template(template_sel.ANY_VALUE);
			} else {
				single_value = new TitanEmbedded_PDV_identification_syntaxes_template();
			}
			single_value_union_selection = TitanEmbedded_PDV_identification.union_selection_type.ALT_syntaxes;
			set_selection(template_sel.SPECIFIC_VALUE);
		}
		return (TitanEmbedded_PDV_identification_syntaxes_template)single_value;
	}

	/**
	 * Gives read-only access to field syntaxes.
	 * If other field is not selected, this function will cause a dynamic test case error.
	 *
	 * @return field syntaxes.
	 * */
	public TitanEmbedded_PDV_identification_syntaxes_template constGetsyntaxes() {
		if (template_selection != template_sel.SPECIFIC_VALUE) {
			throw new TtcnError("Accessing field syntaxes in a non-specific template of union type EMBEDDED PDV.identification.");
		}
		if (single_value_union_selection != TitanEmbedded_PDV_identification.union_selection_type.ALT_syntaxes) {
			throw new TtcnError("Accessing non-selected field syntaxes in a template of union type EMBEDDED PDV.identification.");
		}
		return (TitanEmbedded_PDV_identification_syntaxes_template)single_value;
	}

	/**
	 * Selects and gives access to field syntax.
	 * If other field was previously selected, its value will be destroyed.
	 *
	 * @return field syntax.
	 * */
	public TitanObjectid_template getsyntax() {
		if (template_selection != template_sel.SPECIFIC_VALUE || single_value_union_selection != TitanEmbedded_PDV_identification.union_selection_type.ALT_syntax) {
			final template_sel old_selection = template_selection;
			clean_up();
			if (old_selection == template_sel.ANY_VALUE || old_selection == template_sel.ANY_OR_OMIT) {
				single_value = new TitanObjectid_template(template_sel.ANY_VALUE);
			} else {
				single_value = new TitanObjectid_template();
			}
			single_value_union_selection = TitanEmbedded_PDV_identification.union_selection_type.ALT_syntax;
			set_selection(template_sel.SPECIFIC_VALUE);
		}
		return (TitanObjectid_template)single_value;
	}

	/**
	 * Gives read-only access to field syntax.
	 * If other field is not selected, this function will cause a dynamic test case error.
	 *
	 * @return field syntax.
	 * */
	public TitanObjectid_template constGetsyntax() {
		if (template_selection != template_sel.SPECIFIC_VALUE) {
			throw new TtcnError("Accessing field syntax in a non-specific template of union type EMBEDDED PDV.identification.");
		}
		if (single_value_union_selection != TitanEmbedded_PDV_identification.union_selection_type.ALT_syntax) {
			throw new TtcnError("Accessing non-selected field syntax in a template of union type EMBEDDED PDV.identification.");
		}
		return (TitanObjectid_template)single_value;
	}

	/**
	 * Selects and gives access to field presentation-context-id.
	 * If other field was previously selected, its value will be destroyed.
	 *
	 * @return field presentation-context-id.
	 * */
	public TitanInteger_template getpresentation__context__id() {
		if (template_selection != template_sel.SPECIFIC_VALUE || single_value_union_selection != TitanEmbedded_PDV_identification.union_selection_type.ALT_presentation__context__id) {
			final template_sel old_selection = template_selection;
			clean_up();
			if (old_selection == template_sel.ANY_VALUE || old_selection == template_sel.ANY_OR_OMIT) {
				single_value = new TitanInteger_template(template_sel.ANY_VALUE);
			} else {
				single_value = new TitanInteger_template();
			}
			single_value_union_selection = TitanEmbedded_PDV_identification.union_selection_type.ALT_presentation__context__id;
			set_selection(template_sel.SPECIFIC_VALUE);
		}
		return (TitanInteger_template)single_value;
	}

	/**
	 * Gives read-only access to field presentation-context-id.
	 * If other field is not selected, this function will cause a dynamic test case error.
	 *
	 * @return field presentation-context-id.
	 * */
	public TitanInteger_template constGetpresentation__context__id() {
		if (template_selection != template_sel.SPECIFIC_VALUE) {
			throw new TtcnError("Accessing field presentation-context-id in a non-specific template of union type EMBEDDED PDV.identification.");
		}
		if (single_value_union_selection != TitanEmbedded_PDV_identification.union_selection_type.ALT_presentation__context__id) {
			throw new TtcnError("Accessing non-selected field presentation-context-id in a template of union type EMBEDDED PDV.identification.");
		}
		return (TitanInteger_template)single_value;
	}

	/**
	 * Selects and gives access to field context-negotiation.
	 * If other field was previously selected, its value will be destroyed.
	 *
	 * @return field context-negotiation.
	 * */
	public TitanEmbedded_PDV_identification_context__negotiation_template getcontext__negotiation() {
		if (template_selection != template_sel.SPECIFIC_VALUE || single_value_union_selection != TitanEmbedded_PDV_identification.union_selection_type.ALT_context__negotiation) {
			final template_sel old_selection = template_selection;
			clean_up();
			if (old_selection == template_sel.ANY_VALUE || old_selection == template_sel.ANY_OR_OMIT) {
				single_value = new TitanEmbedded_PDV_identification_context__negotiation_template(template_sel.ANY_VALUE);
			} else {
				single_value = new TitanEmbedded_PDV_identification_context__negotiation_template();
			}
			single_value_union_selection = TitanEmbedded_PDV_identification.union_selection_type.ALT_context__negotiation;
			set_selection(template_sel.SPECIFIC_VALUE);
		}
		return (TitanEmbedded_PDV_identification_context__negotiation_template)single_value;
	}

	/**
	 * Gives read-only access to field context-negotiation.
	 * If other field is not selected, this function will cause a dynamic test case error.
	 *
	 * @return field context-negotiation.
	 * */
	public TitanEmbedded_PDV_identification_context__negotiation_template constGetcontext__negotiation() {
		if (template_selection != template_sel.SPECIFIC_VALUE) {
			throw new TtcnError("Accessing field context-negotiation in a non-specific template of union type EMBEDDED PDV.identification.");
		}
		if (single_value_union_selection != TitanEmbedded_PDV_identification.union_selection_type.ALT_context__negotiation) {
			throw new TtcnError("Accessing non-selected field context-negotiation in a template of union type EMBEDDED PDV.identification.");
		}
		return (TitanEmbedded_PDV_identification_context__negotiation_template)single_value;
	}

	/**
	 * Selects and gives access to field transfer-syntax.
	 * If other field was previously selected, its value will be destroyed.
	 *
	 * @return field transfer-syntax.
	 * */
	public TitanObjectid_template gettransfer__syntax() {
		if (template_selection != template_sel.SPECIFIC_VALUE || single_value_union_selection != TitanEmbedded_PDV_identification.union_selection_type.ALT_transfer__syntax) {
			final template_sel old_selection = template_selection;
			clean_up();
			if (old_selection == template_sel.ANY_VALUE || old_selection == template_sel.ANY_OR_OMIT) {
				single_value = new TitanObjectid_template(template_sel.ANY_VALUE);
			} else {
				single_value = new TitanObjectid_template();
			}
			single_value_union_selection = TitanEmbedded_PDV_identification.union_selection_type.ALT_transfer__syntax;
			set_selection(template_sel.SPECIFIC_VALUE);
		}
		return (TitanObjectid_template)single_value;
	}

	/**
	 * Gives read-only access to field transfer-syntax.
	 * If other field is not selected, this function will cause a dynamic test case error.
	 *
	 * @return field transfer-syntax.
	 * */
	public TitanObjectid_template constGettransfer__syntax() {
		if (template_selection != template_sel.SPECIFIC_VALUE) {
			throw new TtcnError("Accessing field transfer-syntax in a non-specific template of union type EMBEDDED PDV.identification.");
		}
		if (single_value_union_selection != TitanEmbedded_PDV_identification.union_selection_type.ALT_transfer__syntax) {
			throw new TtcnError("Accessing non-selected field transfer-syntax in a template of union type EMBEDDED PDV.identification.");
		}
		return (TitanObjectid_template)single_value;
	}

	/**
	 * Selects and gives access to field fixed.
	 * If other field was previously selected, its value will be destroyed.
	 *
	 * @return field fixed.
	 * */
	public TitanAsn_Null_template getfixed() {
		if (template_selection != template_sel.SPECIFIC_VALUE || single_value_union_selection != TitanEmbedded_PDV_identification.union_selection_type.ALT_fixed) {
			final template_sel old_selection = template_selection;
			clean_up();
			if (old_selection == template_sel.ANY_VALUE || old_selection == template_sel.ANY_OR_OMIT) {
				single_value = new TitanAsn_Null_template(template_sel.ANY_VALUE);
			} else {
				single_value = new TitanAsn_Null_template();
			}
			single_value_union_selection = TitanEmbedded_PDV_identification.union_selection_type.ALT_fixed;
			set_selection(template_sel.SPECIFIC_VALUE);
		}
		return (TitanAsn_Null_template)single_value;
	}

	/**
	 * Gives read-only access to field fixed.
	 * If other field is not selected, this function will cause a dynamic test case error.
	 *
	 * @return field fixed.
	 * */
	public TitanAsn_Null_template constGetfixed() {
		if (template_selection != template_sel.SPECIFIC_VALUE) {
			throw new TtcnError("Accessing field fixed in a non-specific template of union type EMBEDDED PDV.identification.");
		}
		if (single_value_union_selection != TitanEmbedded_PDV_identification.union_selection_type.ALT_fixed) {
			throw new TtcnError("Accessing non-selected field fixed in a template of union type EMBEDDED PDV.identification.");
		}
		return (TitanAsn_Null_template)single_value;
	}

	@Override
	public void log() {
		switch (template_selection) {
		case SPECIFIC_VALUE:
			single_value.log();
			break;
		case COMPLEMENTED_LIST:
			TTCN_Logger.log_event_str("complement");
		case VALUE_LIST:
			TTCN_Logger.log_char('(');
			for (int list_count = 0; list_count < value_list.size(); list_count++) {
				if (list_count > 0) {
					TTCN_Logger.log_event_str(", ");
				}
				value_list.get(list_count).log();
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
	public void log_match(final Base_Type match_value, final boolean legacy) {
		if (match_value instanceof TitanEmbedded_PDV_identification) {
			log_match((TitanEmbedded_PDV_identification)match_value, legacy);
			return;
		}

		throw new TtcnError("Internal Error: value can not be cast to EMBEDDED PDV.identification.");
	}

	/**
	 * Logs the matching of the provided value to this template, to help
	 * identify the reason for mismatch. In legacy mode omitted value fields
	 * are not matched against the template field.
	 *
	 * @param match_value
	 *                the value to be matched.
	 * @param legacy
	 *                use legacy mode.
	 * */
	public void log_match(final TitanEmbedded_PDV_identification match_value, final boolean legacy) {
		if (TTCN_Logger.matching_verbosity_t.VERBOSITY_COMPACT == TTCN_Logger.get_matching_verbosity() && match(match_value, legacy)) {
			TTCN_Logger.print_logmatch_buffer();
			TTCN_Logger.log_event_str(" matched");
			return;
		}
		if (template_selection == template_sel.SPECIFIC_VALUE && single_value_union_selection == match_value.get_selection()) {
			switch (single_value_union_selection) {
			case ALT_syntaxes:
				if (TTCN_Logger.matching_verbosity_t.VERBOSITY_COMPACT == TTCN_Logger.get_matching_verbosity()) {
					TTCN_Logger.log_logmatch_info(".syntaxes");
					single_value.log_match(match_value.getsyntaxes(), legacy);
				} else {
					TTCN_Logger.log_logmatch_info("{ syntaxes := ");
					single_value.log_match(match_value.getsyntaxes(), legacy);
					TTCN_Logger.log_event_str(" }");
				}
				break;
			case ALT_syntax:
				if (TTCN_Logger.matching_verbosity_t.VERBOSITY_COMPACT == TTCN_Logger.get_matching_verbosity()) {
					TTCN_Logger.log_logmatch_info(".syntax");
					single_value.log_match(match_value.getsyntax(), legacy);
				} else {
					TTCN_Logger.log_logmatch_info("{ syntax := ");
					single_value.log_match(match_value.getsyntax(), legacy);
					TTCN_Logger.log_event_str(" }");
				}
				break;
			case ALT_presentation__context__id:
				if (TTCN_Logger.matching_verbosity_t.VERBOSITY_COMPACT == TTCN_Logger.get_matching_verbosity()) {
					TTCN_Logger.log_logmatch_info(".presentation-context-id");
					single_value.log_match(match_value.getpresentation__context__id(), legacy);
				} else {
					TTCN_Logger.log_logmatch_info("{ presentation-context-id := ");
					single_value.log_match(match_value.getpresentation__context__id(), legacy);
					TTCN_Logger.log_event_str(" }");
				}
				break;
			case ALT_context__negotiation:
				if (TTCN_Logger.matching_verbosity_t.VERBOSITY_COMPACT == TTCN_Logger.get_matching_verbosity()) {
					TTCN_Logger.log_logmatch_info(".context-negotiation");
					single_value.log_match(match_value.getcontext__negotiation(), legacy);
				} else {
					TTCN_Logger.log_logmatch_info("{ context-negotiation := ");
					single_value.log_match(match_value.getcontext__negotiation(), legacy);
					TTCN_Logger.log_event_str(" }");
				}
				break;
			case ALT_transfer__syntax:
				if (TTCN_Logger.matching_verbosity_t.VERBOSITY_COMPACT == TTCN_Logger.get_matching_verbosity()) {
					TTCN_Logger.log_logmatch_info(".transfer-syntax");
					single_value.log_match(match_value.gettransfer__syntax(), legacy);
				} else {
					TTCN_Logger.log_logmatch_info("{ transfer-syntax := ");
					single_value.log_match(match_value.gettransfer__syntax(), legacy);
					TTCN_Logger.log_event_str(" }");
				}
				break;
			case ALT_fixed:
				if (TTCN_Logger.matching_verbosity_t.VERBOSITY_COMPACT == TTCN_Logger.get_matching_verbosity()) {
					TTCN_Logger.log_logmatch_info(".fixed");
					single_value.log_match(match_value.getfixed(), legacy);
				} else {
					TTCN_Logger.log_logmatch_info("{ fixed := ");
					single_value.log_match(match_value.getfixed(), legacy);
					TTCN_Logger.log_event_str(" }");
				}
				break;
			default:
				TTCN_Logger.print_logmatch_buffer();
				TTCN_Logger.log_event_str("<invalid selector>");
			}
		} else {
			TTCN_Logger.print_logmatch_buffer();
			match_value.log();
			TTCN_Logger.log_event_str(" with ");
			log();
			if (match(match_value, legacy)) {
				TTCN_Logger.log_event_str(" matched");
			} else {
				TTCN_Logger.log_event_str(" unmatched");
			}
		}
	}

	@Override
	public void encode_text(final Text_Buf text_buf) {
		encode_text_base(text_buf);
		switch (template_selection) {
		case OMIT_VALUE:
		case ANY_VALUE:
		case ANY_OR_OMIT:
			break;
		case SPECIFIC_VALUE:
			text_buf.push_int(single_value_union_selection.ordinal());
			single_value.encode_text(text_buf);
			break;
		case VALUE_LIST:
		case COMPLEMENTED_LIST:
			text_buf.push_int(value_list.size());
			for (int i = 0; i < value_list.size(); i++) {
				value_list.get(i).encode_text(text_buf);
			}
			break;
		default:
			throw new TtcnError("Text encoder: Encoding an uninitialized template of type EMBEDDED PDV.identification.");
		}
	}

	@Override
	public void decode_text(final Text_Buf text_buf) {
		clean_up();
		decode_text_base(text_buf);
		switch (template_selection) {
		case OMIT_VALUE:
		case ANY_VALUE:
		case ANY_OR_OMIT:
			break;
		case SPECIFIC_VALUE:{
			final int temp = text_buf.pull_int().getInt();
			switch (temp) {
			case 0:
				single_value = new TitanEmbedded_PDV_identification_syntaxes_template();
				single_value.decode_text(text_buf);
				break;
			case 1:
				single_value = new TitanObjectid_template();
				single_value.decode_text(text_buf);
				break;
			case 2:
				single_value = new TitanInteger_template();
				single_value.decode_text(text_buf);
				break;
			case 3:
				single_value = new TitanEmbedded_PDV_identification_context__negotiation_template();
				single_value.decode_text(text_buf);
				break;
			case 4:
				single_value = new TitanObjectid_template();
				single_value.decode_text(text_buf);
				break;
			case 5:
				single_value = new TitanAsn_Null_template();
				single_value.decode_text(text_buf);
				break;
			}
		}
		case VALUE_LIST:
		case COMPLEMENTED_LIST: {
			final int size = text_buf.pull_int().getInt();
			value_list = new ArrayList<TitanEmbedded_PDV_identification_template>(size);
			for (int i = 0; i < size; i++) {
				final TitanEmbedded_PDV_identification_template temp2 = new TitanEmbedded_PDV_identification_template();
				temp2.decode_text(text_buf);
				value_list.add(temp2);
			}
			break;
		}
		default:
			throw new TtcnError("Text decoder: Unrecognized selector was received in a template of type EMBEDDED PDV.identification.");
		}
	}

	@Override
	public void set_param(final Module_Parameter param) {
		if((param.get_id() instanceof Module_Param_Name) && param.get_id().next_name()) {
			final String param_field = param.get_id().get_current_name();
			if (param_field.charAt(0) >= '0' && param_field.charAt(0) <= '9') {
				param.error("Unexpected array index in module parameter, expected a valid field name for union template type `EMBEDDED PDV.identification");
			}
			if("syntaxes".equals(param_field)) {
				single_value.set_param(param);
				return;
			} else if("syntax".equals(param_field)) {
				single_value.set_param(param);
				return;
			} else if("presentation-context-id".equals(param_field)) {
				single_value.set_param(param);
				return;
			} else if("context-negotiation".equals(param_field)) {
				single_value.set_param(param);
				return;
			} else if("transfer-syntax".equals(param_field)) {
				single_value.set_param(param);
				return;
			} else if("fixed".equals(param_field)) {
				single_value.set_param(param);
				return;
			} else {
				param.error(MessageFormat.format("Field `{0}' not found in union template type `{0}", param_field));
			}
		}
		param.basic_check(Module_Parameter.basic_check_bits_t.BC_TEMPLATE.getValue(), "union template");
		switch (param.get_type()) {
		case MP_Omit:
			assign(template_sel.OMIT_VALUE);
			break;
		case MP_Any:
			assign(template_sel.ANY_VALUE);
			break;
		case MP_AnyOrNone:
			assign(template_sel.ANY_OR_OMIT);
			break;
		case MP_List_Template:
		case MP_ComplementList_Template: {
			final int size = param.get_size();
			set_type(param.get_type() == Module_Parameter.type_t.MP_List_Template ? template_sel.VALUE_LIST : template_sel.COMPLEMENTED_LIST, size);
			for (int i = 0; i < size; i++) {
				list_item(i).set_param(param.get_elem(i));
			}
			break;
		}
		case MP_Value_List:
			if (param.get_size() == 0) {
				break;
			}
			param.type_error("union template", "EMBEDDED PDV.identification");
			break;
		case MP_Assignment_List: {
			final Module_Parameter mp_last = param.get_elem(param.get_size() - 1);
			final String last_name = mp_last.get_id().get_name();
			if("syntaxes".equals(last_name)) {
				getsyntaxes().set_param(mp_last);
				break;
			}
			if("syntax".equals(last_name)) {
				getsyntax().set_param(mp_last);
				break;
			}
			if("presentation-context-id".equals(last_name)) {
				getpresentation__context__id().set_param(mp_last);
				break;
			}
			if("context-negotiation".equals(last_name)) {
				getcontext__negotiation().set_param(mp_last);
				break;
			}
			if("transfer-syntax".equals(last_name)) {
				gettransfer__syntax().set_param(mp_last);
				break;
			}
			if("fixed".equals(last_name)) {
				getfixed().set_param(mp_last);
				break;
			}
			mp_last.error(MessageFormat.format("Field {0} does not exist in type EMBEDDED PDV.identification.", last_name));
			break;
		}
		default:
			param.type_error("union template", "EMBEDDED PDV.identification");
		}
		is_ifPresent = param.get_ifpresent();
	}

	@Override
	public void check_restriction(final template_res restriction, final String name, final boolean legacy) {
		if (template_selection == template_sel.UNINITIALIZED_TEMPLATE) {
			return;
		}
		switch ((name != null && restriction == template_res.TR_VALUE) ? template_res.TR_OMIT : restriction) {
		case TR_OMIT:
			if (template_selection == template_sel.OMIT_VALUE) {
				return;
			}
		case TR_VALUE:
			if (template_selection != template_sel.SPECIFIC_VALUE || is_ifPresent) {
				break;
			}
			switch (single_value_union_selection) {
			case ALT_syntaxes:
				((TitanEmbedded_PDV_identification_syntaxes_template)single_value).check_restriction(restriction, name == null ? "EMBEDDED PDV.identification" : name, legacy);
				return;
			case ALT_syntax:
				((TitanObjectid_template)single_value).check_restriction(restriction, name == null ? "EMBEDDED PDV.identification" : name, legacy);
				return;
			case ALT_presentation__context__id:
				((TitanInteger_template)single_value).check_restriction(restriction, name == null ? "EMBEDDED PDV.identification" : name, legacy);
				return;
			case ALT_context__negotiation:
				((TitanEmbedded_PDV_identification_context__negotiation_template)single_value).check_restriction(restriction, name == null ? "EMBEDDED PDV.identification" : name, legacy);
				return;
			case ALT_transfer__syntax:
				((TitanObjectid_template)single_value).check_restriction(restriction, name == null ? "EMBEDDED PDV.identification" : name, legacy);
				return;
			case ALT_fixed:
				((TitanAsn_Null_template)single_value).check_restriction(restriction, name == null ? "EMBEDDED PDV.identification" : name, legacy);
				return;
			default:
				throw new TtcnError("Internal error: Invalid selector in a specific value when performing check_restriction operation on a template of union type EMBEDDED PDV.identification.");
			}
		case TR_PRESENT:
			if (!match_omit(legacy)) {
				return;
			}
			break;
		default:
			return;
		}
		throw new TtcnError(MessageFormat.format("Restriction `{0}'' on template of type {1} violated.", get_res_name(restriction), name == null ? "EMBEDDED PDV.identification" : name));
	}
}