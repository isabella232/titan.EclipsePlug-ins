/******************************************************************************
 * Copyright (c) 2000-2020 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

import java.text.MessageFormat;
import java.util.ArrayList;

import org.eclipse.titan.runtime.core.Param_Types.Module_Param_Any;
import org.eclipse.titan.runtime.core.Param_Types.Module_Param_AnyOrNone;
import org.eclipse.titan.runtime.core.Param_Types.Module_Param_Assignment_List;
import org.eclipse.titan.runtime.core.Param_Types.Module_Param_ComplementList_Template;
import org.eclipse.titan.runtime.core.Param_Types.Module_Param_FieldName;
import org.eclipse.titan.runtime.core.Param_Types.Module_Param_List_Template;
import org.eclipse.titan.runtime.core.Param_Types.Module_Param_Name;
import org.eclipse.titan.runtime.core.Param_Types.Module_Param_Omit;
import org.eclipse.titan.runtime.core.Param_Types.Module_Param_Unbound;
import org.eclipse.titan.runtime.core.Param_Types.Module_Parameter;

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

	/**
	 * Internal function to copy the provided value into this template.
	 * The template becomes a specific value template.
	 * The already existing content is overwritten.
	 *
	 * @param other_value the value to be copied.
	 * */
	private void copy_value(final TitanExternal_identification other_value) {
		single_value_union_selection = other_value.get_selection();
		switch (other_value.get_selection()) {
		case ALT_syntaxes:
			single_value = new TitanExternal_identification_syntaxes_template(other_value.constGet_field_syntaxes());
			break;
		case ALT_syntax:
			single_value = new TitanObjectid_template(other_value.constGet_field_syntax());
			break;
		case ALT_presentation__context__id:
			single_value = new TitanInteger_template(other_value.constGet_field_presentation__context__id());
			break;
		case ALT_context__negotiation:
			single_value = new TitanExternal_identification_context__negotiation_template(other_value.constGet_field_context__negotiation());
			break;
		case ALT_transfer__syntax:
			single_value = new TitanObjectid_template(other_value.constGet_field_transfer__syntax());
			break;
		case ALT_fixed:
			single_value = new TitanAsn_Null_template(other_value.constGet_field_fixed());
			break;
		default:
			throw new TtcnError("Initializing a template with an unbound value of type EXTERNAL.identification.");
		}
		set_selection(template_sel.SPECIFIC_VALUE);
	}

	/**
	 * Internal function to copy the provided template into this template.
	 * The already existing content is overwritten.
	 *
	 * @param other_value the value to be copied.
	 * */
	private void copy_template(final TitanExternal_identification_template other_value) {
		switch (other_value.template_selection) {
		case SPECIFIC_VALUE:
			single_value_union_selection = other_value.single_value_union_selection;
			switch (single_value_union_selection) {
			case ALT_syntaxes:
				single_value = new TitanExternal_identification_syntaxes_template(other_value.constGet_field_syntaxes());
				break;
			case ALT_syntax:
				single_value = new TitanObjectid_template(other_value.constGet_field_syntax());
				break;
			case ALT_presentation__context__id:
				single_value = new TitanInteger_template(other_value.constGet_field_presentation__context__id());
				break;
			case ALT_context__negotiation:
				single_value = new TitanExternal_identification_context__negotiation_template(other_value.constGet_field_context__negotiation());
				break;
			case ALT_transfer__syntax:
				single_value = new TitanObjectid_template(other_value.constGet_field_transfer__syntax());
				break;
			case ALT_fixed:
				single_value = new TitanAsn_Null_template(other_value.constGet_field_fixed());
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
		set_selection(other_value);
	}

	/**
	 * Initializes to unbound/uninitialized template.
	 * */
	public TitanExternal_identification_template() {
		// do nothing
	}
	/**
	 * Initializes to a given template kind.
	 *
	 * @param other_value
	 *                the template kind to initialize to.
	 * */
	public TitanExternal_identification_template(final template_sel other_value) {
		super(other_value);
		check_single_selection(other_value);
	}
	/**
	 * Initializes to a given value.
	 * The template becomes a specific template.
	 * The elements of the provided value are copied.
	 *
	 * @param other_value
	 *                the value to initialize to.
	 * */
	public TitanExternal_identification_template(final TitanExternal_identification other_value) {
		copy_value(other_value);
	}
	/**
	 * Initializes to a given template.
	 * The elements of the provided template are copied.
	 *
	 * @param other_value
	 *                the value to initialize to.
	 * */
	public TitanExternal_identification_template(final TitanExternal_identification_template other_value) {
		copy_template(other_value);
	}

	@Override
	public void clean_up() {
		switch (template_selection) {
		case SPECIFIC_VALUE:
			switch (single_value_union_selection) {
			case ALT_syntaxes:
				((TitanExternal_identification_syntaxes_template)single_value).clean_up();
				break;
			case ALT_syntax:
				((TitanObjectid_template)single_value).clean_up();
				break;
			case ALT_presentation__context__id:
				((TitanInteger_template)single_value).clean_up();
				break;
			case ALT_context__negotiation:
				((TitanExternal_identification_context__negotiation_template)single_value).clean_up();
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
	public TitanExternal_identification_template operator_assign(final template_sel otherValue ) {
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
	public TitanExternal_identification_template operator_assign(final TitanExternal_identification otherValue ) {
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
	public TitanExternal_identification_template operator_assign(final TitanExternal_identification_template otherValue ) {
		if (otherValue != this) {
			clean_up();
			copy_template(otherValue);
		}
		return this;
	}

	@Override
	public TitanExternal_identification_template operator_assign(final Base_Type otherValue ) {
		if (otherValue instanceof TitanExternal_identification) {
			return operator_assign((TitanExternal_identification)otherValue);
		}
		throw new TtcnError("Internal Error: value can not be cast to TitanExternal_identification.");
	}

	@Override
	public TitanExternal_identification_template operator_assign(final Base_Template otherValue ) {
		if (otherValue instanceof TitanExternal_identification_template) {
			return operator_assign((TitanExternal_identification_template)otherValue);
		}
		throw new TtcnError("Internal Error: value can not be cast to TitanExternal_identification_template.");
	}

	/**
	 * Matches the provided value against this template.
	 *
	 * @param other_value the value to be matched.
	 * */
	public boolean match(final TitanExternal_identification other_value) {
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
	public boolean match(final TitanExternal_identification other_value, final boolean legacy) {
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
			final TitanExternal_identification.union_selection_type value_selection = other_value.get_selection();
			if (value_selection == TitanExternal_identification.union_selection_type.UNBOUND_VALUE) {
				return false;
			}
			if (value_selection != single_value_union_selection) {
				return false;
			}
			switch (value_selection) {
			case ALT_syntaxes:
				return ((TitanExternal_identification_syntaxes_template)single_value).match(other_value.get_field_syntaxes(), legacy);
			case ALT_syntax:
				return ((TitanObjectid_template)single_value).match(other_value.get_field_syntax(), legacy);
			case ALT_presentation__context__id:
				return ((TitanInteger_template)single_value).match(other_value.get_field_presentation__context__id(), legacy);
			case ALT_context__negotiation:
				return ((TitanExternal_identification_context__negotiation_template)single_value).match(other_value.get_field_context__negotiation(), legacy);
			case ALT_transfer__syntax:
				return ((TitanObjectid_template)single_value).match(other_value.get_field_transfer__syntax(), legacy);
			case ALT_fixed:
				return ((TitanAsn_Null_template)single_value).match(other_value.get_field_fixed(), legacy);
			default:
				throw new TtcnError("Internal error: Invalid selector in a specific value when matching a template of union type EXTERNAL.identification.");
			}
		case VALUE_LIST:
		case COMPLEMENTED_LIST: {
			final int list_size = value_list.size();
			for(int i = 0 ; i < list_size; i++) {
				if(value_list.get(i).match(other_value, legacy)) {
					return template_selection == template_sel.VALUE_LIST;
				}
			}
			return template_selection == template_sel.COMPLEMENTED_LIST;
		}
		default:
			throw new TtcnError("Matching with an uninitialized/unsupported integer template.");
		}
	}

	@Override
	public boolean match(final Base_Type otherValue, final boolean legacy) {
		if (otherValue instanceof TitanExternal_identification) {
			return match((TitanExternal_identification)otherValue, legacy);
		}

		throw new TtcnError("Internal Error: The left operand of assignment is not of type TitanExternal_identification.");
	}

	/**
	 * Checks and reports whether the union has the provided alternative active or not.
	 *
	 * ischosen in the core.
	 *
	 * @param checked_selection the selection to check for.
	 *
	 * @return {@code true} if the unions has the provided selection active.
	 */
	public boolean ischosen(final TitanExternal_identification.union_selection_type checked_selection) {
		if(checked_selection == TitanExternal_identification.union_selection_type.UNBOUND_VALUE) {
			throw new TtcnError("Internal error: Performing ischosen() operation on an invalid field of union type EXTERNAL.identification.");
		}
		switch (template_selection) {
		case SPECIFIC_VALUE:
			if (single_value_union_selection == TitanExternal_identification.union_selection_type.UNBOUND_VALUE) {
				throw new TtcnError("Internal error: Invalid selector in a specific value when performing ischosen() operation on a template of union type EXTERNAL.identification.");
			}
			return single_value_union_selection == checked_selection;
		case VALUE_LIST: {
			if (value_list.isEmpty()) {
				throw new TtcnError("Internal error: Performing ischosen() operation on a template of union type EXTERNAL.identification containing an empty list.");
			}
			final int list_size = value_list.size();
			for (int i = 0; i < list_size; i++) {
				if(!value_list.get(i).ischosen(checked_selection)) {
					return false;
				}
			}
			return true;
		}
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
			return ((TitanExternal_identification_syntaxes_template)single_value).is_value();
		case ALT_syntax:
			return ((TitanObjectid_template)single_value).is_value();
		case ALT_presentation__context__id:
			return ((TitanInteger_template)single_value).is_value();
		case ALT_context__negotiation:
			return ((TitanExternal_identification_context__negotiation_template)single_value).is_value();
		case ALT_transfer__syntax:
			return ((TitanObjectid_template)single_value).is_value();
		case ALT_fixed:
			return ((TitanAsn_Null_template)single_value).is_value();
		default:
			throw new TtcnError("Internal error: Invalid selector in a specific value when performing is_value operation on a template of union type EXTERNAL.identification.");
		}
	}

	@Override
	public TitanExternal_identification valueof() {
		if (template_selection != template_sel.SPECIFIC_VALUE || is_ifPresent) {
			throw new TtcnError("Performing a valueof or send operation on a non-specific template of union type EXTERNAL.identification.");
		}
		final TitanExternal_identification ret_val = new TitanExternal_identification();
		switch (single_value_union_selection) {
		case ALT_syntaxes:
			ret_val.get_field_syntaxes().operator_assign(((TitanExternal_identification_syntaxes_template)single_value).valueof());
			break;
		case ALT_syntax:
			ret_val.get_field_syntax().operator_assign(((TitanObjectid_template)single_value).valueof());
			break;
		case ALT_presentation__context__id:
			ret_val.get_field_presentation__context__id().operator_assign(((TitanInteger_template)single_value).valueof());
			break;
		case ALT_context__negotiation:
			ret_val.get_field_context__negotiation().operator_assign(((TitanExternal_identification_context__negotiation_template)single_value).valueof());
			break;
		case ALT_transfer__syntax:
			ret_val.get_field_transfer__syntax().operator_assign(((TitanObjectid_template)single_value).valueof());
			break;
		case ALT_fixed:
			ret_val.get_field_fixed().operator_assign(((TitanAsn_Null_template)single_value).valueof());
			break;
		default:
			throw new TtcnError("Internal error: Invalid selector in a specific value when performing valueof operation on a template of union type EXTERNAL.identification.");
		}
		return ret_val;
	}

	@Override
	public void set_type(final template_sel template_type, final int list_length) {
		if (template_type != template_sel.VALUE_LIST && template_type != template_sel.COMPLEMENTED_LIST) {
			throw new TtcnError("Internal error: Setting an invalid list for a template of union type EXTERNAL.identification.");
		}
		clean_up();
		set_selection(template_type);
		value_list = new ArrayList<TitanExternal_identification_template>(list_length);
		for(int i = 0 ; i < list_length; i++) {
			value_list.add(new TitanExternal_identification_template());
		}
	}

	@Override
	public int n_list_elem() {
		if (template_selection != template_sel.VALUE_LIST && template_selection != template_sel.COMPLEMENTED_LIST) {
			throw new TtcnError("Internal error: Accessing a list element of a non-list template of union type EXTERNAL.identification.");
		}
		return value_list.size();
	}

	@Override
	public TitanExternal_identification_template list_item(final int list_index)  {
		if (template_selection != template_sel.VALUE_LIST && template_selection != template_sel.COMPLEMENTED_LIST) {
			throw new TtcnError("Internal error: Accessing a list element of a non-list template of union type EXTERNAL.identification.");
		}
		if (list_index < 0) {
			throw new TtcnError(MessageFormat.format("Internal error: Accessing a value list template of type EXTERNAL.identification using a negative index ({0}).", list_index));
		} else if(list_index >= value_list.size()) {
			throw new TtcnError("Internal error: Index overflow in a value list template of union type EXTERNAL.identification.");
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
				final int list_size = value_list.size();
				for (int i = 0 ; i < list_size; i++) {
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
	 * If field syntaxes was previously selected,
	 * its value will be destroyed.
	 *
	 * @return field syntaxes.
	 * */
	public TitanExternal_identification_syntaxes_template get_field_syntaxes() {
		if (template_selection != template_sel.SPECIFIC_VALUE || single_value_union_selection != TitanExternal_identification.union_selection_type.ALT_syntaxes) {
			final template_sel old_selection = template_selection;
			clean_up();
			if (old_selection == template_sel.ANY_VALUE || old_selection == template_sel.ANY_OR_OMIT) {
				single_value = new TitanExternal_identification_syntaxes_template(template_sel.ANY_VALUE);
			} else {
				single_value = new TitanExternal_identification_syntaxes_template();
			}
			single_value_union_selection = TitanExternal_identification.union_selection_type.ALT_syntaxes;
			set_selection(template_sel.SPECIFIC_VALUE);
		}
		return (TitanExternal_identification_syntaxes_template)single_value;
	}

	/**
	 * Gives read-only access to field syntaxes.
	 * If field syntaxes is not selected,
	 * this function will cause a dynamic test case error.
	 *
	 * @return field syntaxes.
	 * */
	public TitanExternal_identification_syntaxes_template constGet_field_syntaxes() {
		if (template_selection != template_sel.SPECIFIC_VALUE) {
			throw new TtcnError("Accessing field syntaxes in a non-specific template of union type EXTERNAL.identification.");
		}
		if (single_value_union_selection != TitanExternal_identification.union_selection_type.ALT_syntaxes) {
			throw new TtcnError("Accessing non-selected field syntaxes in a template of union type EXTERNAL.identification.");
		}
		return (TitanExternal_identification_syntaxes_template)single_value;
	}

	/**
	 * Selects and gives access to field syntax.
	 * If field syntax was previously selected,
	 * its value will be destroyed.
	 *
	 * @return field syntax.
	 * */
	public TitanObjectid_template get_field_syntax() {
		if (template_selection != template_sel.SPECIFIC_VALUE || single_value_union_selection != TitanExternal_identification.union_selection_type.ALT_syntax) {
			final template_sel old_selection = template_selection;
			clean_up();
			if (old_selection == template_sel.ANY_VALUE || old_selection == template_sel.ANY_OR_OMIT) {
				single_value = new TitanObjectid_template(template_sel.ANY_VALUE);
			} else {
				single_value = new TitanObjectid_template();
			}
			single_value_union_selection = TitanExternal_identification.union_selection_type.ALT_syntax;
			set_selection(template_sel.SPECIFIC_VALUE);
		}
		return (TitanObjectid_template)single_value;
	}

	/**
	 * Gives read-only access to field syntax.
	 * If field syntax is not selected,
	 * this function will cause a dynamic test case error.
	 *
	 * @return field syntax.
	 * */
	public TitanObjectid_template constGet_field_syntax() {
		if (template_selection != template_sel.SPECIFIC_VALUE) {
			throw new TtcnError("Accessing field syntax in a non-specific template of union type EXTERNAL.identification.");
		}
		if (single_value_union_selection != TitanExternal_identification.union_selection_type.ALT_syntax) {
			throw new TtcnError("Accessing non-selected field syntax in a template of union type EXTERNAL.identification.");
		}
		return (TitanObjectid_template)single_value;
	}

	/**
	 * Selects and gives access to field presentation-context-id.
	 * If field presentation-context-id was previously selected,
	 * its value will be destroyed.
	 *
	 * @return field presentation-context-id.
	 * */
	public TitanInteger_template get_field_presentation__context__id() {
		if (template_selection != template_sel.SPECIFIC_VALUE || single_value_union_selection != TitanExternal_identification.union_selection_type.ALT_presentation__context__id) {
			final template_sel old_selection = template_selection;
			clean_up();
			if (old_selection == template_sel.ANY_VALUE || old_selection == template_sel.ANY_OR_OMIT) {
				single_value = new TitanInteger_template(template_sel.ANY_VALUE);
			} else {
				single_value = new TitanInteger_template();
			}
			single_value_union_selection = TitanExternal_identification.union_selection_type.ALT_presentation__context__id;
			set_selection(template_sel.SPECIFIC_VALUE);
		}
		return (TitanInteger_template)single_value;
	}

	/**
	 * Gives read-only access to field presentation-context-id.
	 * If field presentation-context-id is not selected,
	 * this function will cause a dynamic test case error.
	 *
	 * @return field presentation-context-id.
	 * */
	public TitanInteger_template constGet_field_presentation__context__id() {
		if (template_selection != template_sel.SPECIFIC_VALUE) {
			throw new TtcnError("Accessing field presentation-context-id in a non-specific template of union type EXTERNAL.identification.");
		}
		if (single_value_union_selection != TitanExternal_identification.union_selection_type.ALT_presentation__context__id) {
			throw new TtcnError("Accessing non-selected field presentation-context-id in a template of union type EXTERNAL.identification.");
		}
		return (TitanInteger_template)single_value;
	}

	/**
	 * Selects and gives access to field context-negotiation.
	 * If field context-negotiation was previously selected,
	 * its value will be destroyed.
	 *
	 * @return field context-negotiation.
	 * */
	public TitanExternal_identification_context__negotiation_template get_field_context__negotiation() {
		if (template_selection != template_sel.SPECIFIC_VALUE || single_value_union_selection != TitanExternal_identification.union_selection_type.ALT_context__negotiation) {
			final template_sel old_selection = template_selection;
			clean_up();
			if (old_selection == template_sel.ANY_VALUE || old_selection == template_sel.ANY_OR_OMIT) {
				single_value = new TitanExternal_identification_context__negotiation_template(template_sel.ANY_VALUE);
			} else {
				single_value = new TitanExternal_identification_context__negotiation_template();
			}
			single_value_union_selection = TitanExternal_identification.union_selection_type.ALT_context__negotiation;
			set_selection(template_sel.SPECIFIC_VALUE);
		}
		return (TitanExternal_identification_context__negotiation_template)single_value;
	}

	/**
	 * Gives read-only access to field context-negotiation.
	 * If field context-negotiation is not selected,
	 * this function will cause a dynamic test case error.
	 *
	 * @return field context-negotiation.
	 * */
	public TitanExternal_identification_context__negotiation_template constGet_field_context__negotiation() {
		if (template_selection != template_sel.SPECIFIC_VALUE) {
			throw new TtcnError("Accessing field context-negotiation in a non-specific template of union type EXTERNAL.identification.");
		}
		if (single_value_union_selection != TitanExternal_identification.union_selection_type.ALT_context__negotiation) {
			throw new TtcnError("Accessing non-selected field context-negotiation in a template of union type EXTERNAL.identification.");
		}
		return (TitanExternal_identification_context__negotiation_template)single_value;
	}

	/**
	 * Selects and gives access to field transfer-syntax.
	 * If field transfer-syntax was previously selected,
	 * its value will be destroyed.
	 *
	 * @return field transfer-syntax.
	 * */
	public TitanObjectid_template get_field_transfer__syntax() {
		if (template_selection != template_sel.SPECIFIC_VALUE || single_value_union_selection != TitanExternal_identification.union_selection_type.ALT_transfer__syntax) {
			final template_sel old_selection = template_selection;
			clean_up();
			if (old_selection == template_sel.ANY_VALUE || old_selection == template_sel.ANY_OR_OMIT) {
				single_value = new TitanObjectid_template(template_sel.ANY_VALUE);
			} else {
				single_value = new TitanObjectid_template();
			}
			single_value_union_selection = TitanExternal_identification.union_selection_type.ALT_transfer__syntax;
			set_selection(template_sel.SPECIFIC_VALUE);
		}
		return (TitanObjectid_template)single_value;
	}

	/**
	 * Gives read-only access to field transfer-syntax.
	 * If field transfer-syntax is not selected,
	 * this function will cause a dynamic test case error.
	 *
	 * @return field transfer-syntax.
	 * */
	public TitanObjectid_template constGet_field_transfer__syntax() {
		if (template_selection != template_sel.SPECIFIC_VALUE) {
			throw new TtcnError("Accessing field transfer-syntax in a non-specific template of union type EXTERNAL.identification.");
		}
		if (single_value_union_selection != TitanExternal_identification.union_selection_type.ALT_transfer__syntax) {
			throw new TtcnError("Accessing non-selected field transfer-syntax in a template of union type EXTERNAL.identification.");
		}
		return (TitanObjectid_template)single_value;
	}

	/**
	 * Selects and gives access to field fixed.
	 * If field fixed was previously selected,
	 * its value will be destroyed.
	 *
	 * @return field fixed.
	 * */
	public TitanAsn_Null_template get_field_fixed() {
		if (template_selection != template_sel.SPECIFIC_VALUE || single_value_union_selection != TitanExternal_identification.union_selection_type.ALT_fixed) {
			final template_sel old_selection = template_selection;
			clean_up();
			if (old_selection == template_sel.ANY_VALUE || old_selection == template_sel.ANY_OR_OMIT) {
				single_value = new TitanAsn_Null_template(template_sel.ANY_VALUE);
			} else {
				single_value = new TitanAsn_Null_template();
			}
			single_value_union_selection = TitanExternal_identification.union_selection_type.ALT_fixed;
			set_selection(template_sel.SPECIFIC_VALUE);
		}
		return (TitanAsn_Null_template)single_value;
	}

	/**
	 * Gives read-only access to field fixed.
	 * If field fixed is not selected,
	 * this function will cause a dynamic test case error.
	 *
	 * @return field fixed.
	 * */
	public TitanAsn_Null_template constGet_field_fixed() {
		if (template_selection != template_sel.SPECIFIC_VALUE) {
			throw new TtcnError("Accessing field fixed in a non-specific template of union type EXTERNAL.identification.");
		}
		if (single_value_union_selection != TitanExternal_identification.union_selection_type.ALT_fixed) {
			throw new TtcnError("Accessing non-selected field fixed in a template of union type EXTERNAL.identification.");
		}
		return (TitanAsn_Null_template)single_value;
	}

	@Override
	public void log() {
		switch (template_selection) {
		case SPECIFIC_VALUE:
			switch (single_value_union_selection) {
			case ALT_syntaxes:
				TTCN_Logger.log_event_str("{ syntaxes := ");
				single_value.log();
				TTCN_Logger.log_event_str(" }");
				break;
			case ALT_syntax:
				TTCN_Logger.log_event_str("{ syntax := ");
				single_value.log();
				TTCN_Logger.log_event_str(" }");
				break;
			case ALT_presentation__context__id:
				TTCN_Logger.log_event_str("{ presentation__context__id := ");
				single_value.log();
				TTCN_Logger.log_event_str(" }");
				break;
			case ALT_context__negotiation:
				TTCN_Logger.log_event_str("{ context__negotiation := ");
				single_value.log();
				TTCN_Logger.log_event_str(" }");
				break;
			case ALT_transfer__syntax:
				TTCN_Logger.log_event_str("{ transfer__syntax := ");
				single_value.log();
				TTCN_Logger.log_event_str(" }");
				break;
			case ALT_fixed:
				TTCN_Logger.log_event_str("{ fixed := ");
				single_value.log();
				TTCN_Logger.log_event_str(" }");
				break;
			default:
				TTCN_Logger.log_event_unbound();
			}
			break;
		case COMPLEMENTED_LIST:
			TTCN_Logger.log_event_str("complement");
		case VALUE_LIST: {
			TTCN_Logger.log_char('(');
			final int list_size = value_list.size();
			for (int list_count = 0; list_count < list_size; list_count++) {
				if (list_count > 0) {
					TTCN_Logger.log_event_str(", ");
				}
				value_list.get(list_count).log();
			}
			TTCN_Logger.log_char(')');
			break;
		}
		default:
			log_generic();
			break;
		}
		log_ifpresent();
	}

	@Override
	public void log_match(final Base_Type match_value, final boolean legacy) {
		if (match_value instanceof TitanExternal_identification) {
			log_match((TitanExternal_identification)match_value, legacy);
			return;
		}

		throw new TtcnError("Internal Error: value can not be cast to EXTERNAL.identification.");
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
	public void log_match(final TitanExternal_identification match_value, final boolean legacy) {
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
					single_value.log_match(match_value.get_field_syntaxes(), legacy);
				} else {
					TTCN_Logger.log_logmatch_info("{ syntaxes := ");
					single_value.log_match(match_value.get_field_syntaxes(), legacy);
					TTCN_Logger.log_event_str(" }");
				}
				break;
			case ALT_syntax:
				if (TTCN_Logger.matching_verbosity_t.VERBOSITY_COMPACT == TTCN_Logger.get_matching_verbosity()) {
					TTCN_Logger.log_logmatch_info(".syntax");
					single_value.log_match(match_value.get_field_syntax(), legacy);
				} else {
					TTCN_Logger.log_logmatch_info("{ syntax := ");
					single_value.log_match(match_value.get_field_syntax(), legacy);
					TTCN_Logger.log_event_str(" }");
				}
				break;
			case ALT_presentation__context__id:
				if (TTCN_Logger.matching_verbosity_t.VERBOSITY_COMPACT == TTCN_Logger.get_matching_verbosity()) {
					TTCN_Logger.log_logmatch_info(".presentation-context-id");
					single_value.log_match(match_value.get_field_presentation__context__id(), legacy);
				} else {
					TTCN_Logger.log_logmatch_info("{ presentation-context-id := ");
					single_value.log_match(match_value.get_field_presentation__context__id(), legacy);
					TTCN_Logger.log_event_str(" }");
				}
				break;
			case ALT_context__negotiation:
				if (TTCN_Logger.matching_verbosity_t.VERBOSITY_COMPACT == TTCN_Logger.get_matching_verbosity()) {
					TTCN_Logger.log_logmatch_info(".context-negotiation");
					single_value.log_match(match_value.get_field_context__negotiation(), legacy);
				} else {
					TTCN_Logger.log_logmatch_info("{ context-negotiation := ");
					single_value.log_match(match_value.get_field_context__negotiation(), legacy);
					TTCN_Logger.log_event_str(" }");
				}
				break;
			case ALT_transfer__syntax:
				if (TTCN_Logger.matching_verbosity_t.VERBOSITY_COMPACT == TTCN_Logger.get_matching_verbosity()) {
					TTCN_Logger.log_logmatch_info(".transfer-syntax");
					single_value.log_match(match_value.get_field_transfer__syntax(), legacy);
				} else {
					TTCN_Logger.log_logmatch_info("{ transfer-syntax := ");
					single_value.log_match(match_value.get_field_transfer__syntax(), legacy);
					TTCN_Logger.log_event_str(" }");
				}
				break;
			case ALT_fixed:
				if (TTCN_Logger.matching_verbosity_t.VERBOSITY_COMPACT == TTCN_Logger.get_matching_verbosity()) {
					TTCN_Logger.log_logmatch_info(".fixed");
					single_value.log_match(match_value.get_field_fixed(), legacy);
				} else {
					TTCN_Logger.log_logmatch_info("{ fixed := ");
					single_value.log_match(match_value.get_field_fixed(), legacy);
					TTCN_Logger.log_event_str(" }");
				}
				break;
			default:
				TTCN_Logger.print_logmatch_buffer();
				TTCN_Logger.log_event_str("<invalid selector>");
				break;
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
		case COMPLEMENTED_LIST: {
			final int list_size = value_list.size();
			text_buf.push_int(list_size);
			for (int i = 0; i < list_size; i++) {
				value_list.get(i).encode_text(text_buf);
			}
			break;
		}
		default:
			throw new TtcnError("Text encoder: Encoding an uninitialized template of type EXTERNAL.identification.");
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
			final int temp = text_buf.pull_int().get_int();
			single_value_union_selection = TitanExternal_identification.union_selection_type.values()[temp];
			switch (single_value_union_selection) {
			case UNBOUND_VALUE:
				throw new TtcnError("Text decoder: Unrecognized union selector was received for a template of type EXTERNAL.identification.");
			case ALT_syntaxes:
				single_value = new TitanExternal_identification_syntaxes_template();
				single_value.decode_text(text_buf);
				break;
			case ALT_syntax:
				single_value = new TitanObjectid_template();
				single_value.decode_text(text_buf);
				break;
			case ALT_presentation__context__id:
				single_value = new TitanInteger_template();
				single_value.decode_text(text_buf);
				break;
			case ALT_context__negotiation:
				single_value = new TitanExternal_identification_context__negotiation_template();
				single_value.decode_text(text_buf);
				break;
			case ALT_transfer__syntax:
				single_value = new TitanObjectid_template();
				single_value.decode_text(text_buf);
				break;
			case ALT_fixed:
				single_value = new TitanAsn_Null_template();
				single_value.decode_text(text_buf);
				break;
			}
			break;
		}
		case VALUE_LIST:
		case COMPLEMENTED_LIST: {
			final int size = text_buf.pull_int().get_int();
			value_list = new ArrayList<TitanExternal_identification_template>(size);
			for (int i = 0; i < size; i++) {
				final TitanExternal_identification_template temp2 = new TitanExternal_identification_template();
				temp2.decode_text(text_buf);
				value_list.add(temp2);
			}
			break;
		}
		default:
			throw new TtcnError("Text decoder: Unrecognized selector was received in a template of type EXTERNAL.identification.");
		}
	}

	@Override
	public void set_param(Module_Parameter param) {
		if((param.get_id() instanceof Module_Param_Name) && param.get_id().next_name()) {
			final String param_field = param.get_id().get_current_name();
			if (param_field.charAt(0) >= '0' && param_field.charAt(0) <= '9') {
				param.error("Unexpected array index in module parameter, expected a valid field name for union template type `EXTERNAL.identification'");
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
				param.error(MessageFormat.format("Field `{0}' not found in union template type `EXTERNAL.identification'", param_field));
			}
		}
		param.basic_check(Module_Parameter.basic_check_bits_t.BC_TEMPLATE.getValue(), "union template");
		if (param.get_type() == Module_Parameter.type_t.MP_Reference) {
			param = param.get_referenced_param().get();
		}
		switch (param.get_type()) {
		case MP_Omit:
			operator_assign(template_sel.OMIT_VALUE);
			break;
		case MP_Any:
			operator_assign(template_sel.ANY_VALUE);
			break;
		case MP_AnyOrNone:
			operator_assign(template_sel.ANY_OR_OMIT);
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
			param.type_error("union template", "EXTERNAL.identification");
			break;
		case MP_Assignment_List: {
			final Module_Parameter mp_last = param.get_elem(param.get_size() - 1);
			final String last_name = mp_last.get_id().get_name();
			if("syntaxes".equals(last_name)) {
				get_field_syntaxes().set_param(mp_last);
				break;
			}
			if("syntax".equals(last_name)) {
				get_field_syntax().set_param(mp_last);
				break;
			}
			if("presentation-context-id".equals(last_name)) {
				get_field_presentation__context__id().set_param(mp_last);
				break;
			}
			if("context-negotiation".equals(last_name)) {
				get_field_context__negotiation().set_param(mp_last);
				break;
			}
			if("transfer-syntax".equals(last_name)) {
				get_field_transfer__syntax().set_param(mp_last);
				break;
			}
			if("fixed".equals(last_name)) {
				get_field_fixed().set_param(mp_last);
				break;
			}
			mp_last.error(MessageFormat.format("Field {0} does not exist in type EXTERNAL.identification.", last_name));
			break;
		}
		default:
			param.type_error("union template", "EXTERNAL.identification");
		}
		is_ifPresent = param.get_ifpresent();
	}

	@Override
	public Module_Parameter get_param(final Module_Param_Name param_name) {
		if (param_name.next_name()) {
			final String param_field = param_name.get_current_name();
			if (param_field.charAt(0) >= '0' && param_field.charAt(0) <= '9') {
				throw new TtcnError("Unexpected array index in module parameter reference, expected a valid field name for union template type `EXTERNAL.identification'");
			}
			if ("syntaxes".equals(param_field)) {
				return get_field_syntaxes().get_param(param_name);
			} else if ("syntax".equals(param_field)) {
				return get_field_syntax().get_param(param_name);
			} else if ("presentation-context-id".equals(param_field)) {
				return get_field_presentation__context__id().get_param(param_name);
			} else if ("context-negotiation".equals(param_field)) {
				return get_field_context__negotiation().get_param(param_name);
			} else if ("transfer-syntax".equals(param_field)) {
				return get_field_transfer__syntax().get_param(param_name);
			} else if ("fixed".equals(param_field)) {
				return get_field_fixed().get_param(param_name);
			} else {
				throw new TtcnError(MessageFormat.format("Field `{0}' not found in union type `EXTERNAL.identification'", param_field));
			}
		}
		Module_Parameter mp = null;
		switch (template_selection) {
		case UNINITIALIZED_TEMPLATE:
			mp = new Module_Param_Unbound();
			break;
		case OMIT_VALUE:
			mp = new Module_Param_Omit();
			break;
		case ANY_VALUE:
			mp = new Module_Param_Any();
			break;
		case ANY_OR_OMIT:
			mp = new Module_Param_AnyOrNone();
			break;
		case SPECIFIC_VALUE: {
			Module_Parameter mp_field = null;
			switch(single_value_union_selection) {
			case ALT_syntaxes:
				mp_field = get_field_syntaxes().get_param(param_name);
				mp_field.set_id(new Module_Param_FieldName("syntaxes"));
				break;
			case ALT_syntax:
				mp_field = get_field_syntax().get_param(param_name);
				mp_field.set_id(new Module_Param_FieldName("syntax"));
				break;
			case ALT_presentation__context__id:
				mp_field = get_field_presentation__context__id().get_param(param_name);
				mp_field.set_id(new Module_Param_FieldName("presentation-context-id"));
				break;
			case ALT_context__negotiation:
				mp_field = get_field_context__negotiation().get_param(param_name);
				mp_field.set_id(new Module_Param_FieldName("context-negotiation"));
				break;
			case ALT_transfer__syntax:
				mp_field = get_field_transfer__syntax().get_param(param_name);
				mp_field.set_id(new Module_Param_FieldName("transfer-syntax"));
				break;
			case ALT_fixed:
				mp_field = get_field_fixed().get_param(param_name);
				mp_field.set_id(new Module_Param_FieldName("fixed"));
				break;
			default:
				break;
			}
			mp = new Module_Param_Assignment_List();
			mp.add_elem(mp_field);
			break;
		}
		case VALUE_LIST:
		case COMPLEMENTED_LIST: {
			if (template_selection == template_sel.VALUE_LIST) {
				mp = new Module_Param_List_Template();
			} else {
				mp = new Module_Param_ComplementList_Template();
			}
			for (int i_i = 0; i_i < value_list.size(); ++i_i) {
				mp.add_elem(value_list.get(i_i).get_param(param_name));
			}
			break;
		}
		default:
			break;
		}
		if (is_ifPresent) {
			mp.set_ifpresent();
		}
		return mp;
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
				((TitanExternal_identification_syntaxes_template)single_value).check_restriction(restriction, name == null ? "EXTERNAL.identification" : name, legacy);
				return;
			case ALT_syntax:
				((TitanObjectid_template)single_value).check_restriction(restriction, name == null ? "EXTERNAL.identification" : name, legacy);
				return;
			case ALT_presentation__context__id:
				((TitanInteger_template)single_value).check_restriction(restriction, name == null ? "EXTERNAL.identification" : name, legacy);
				return;
			case ALT_context__negotiation:
				((TitanExternal_identification_context__negotiation_template)single_value).check_restriction(restriction, name == null ? "EXTERNAL.identification" : name, legacy);
				return;
			case ALT_transfer__syntax:
				((TitanObjectid_template)single_value).check_restriction(restriction, name == null ? "EXTERNAL.identification" : name, legacy);
				return;
			case ALT_fixed:
				((TitanAsn_Null_template)single_value).check_restriction(restriction, name == null ? "EXTERNAL.identification" : name, legacy);
				return;
			default:
				throw new TtcnError("Internal error: Invalid selector in a specific value when performing check_restriction operation on a template of union type EXTERNAL.identification.");
			}
		case TR_PRESENT:
			if (!match_omit(legacy)) {
				return;
			}
			break;
		default:
			return;
		}
		throw new TtcnError(MessageFormat.format("Restriction `{0}'' on template of type {1} violated.", get_res_name(restriction), name == null ? "EXTERNAL.identification" : name));
	}
}