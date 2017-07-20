/******************************************************************************
 * Copyright (c) 2000-2017 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

import java.util.ArrayList;
import java.util.List;

/**
 * TTCN-3 bitstring template
 *
 * @author Arpad Lovassy
 * @author Gergo Ujhelyi
 */
public class TitanBitString_template extends Base_Template {

	private TitanBitString single_value;

	// value_list part
	private List<TitanBitString_template> value_list;

	/**
	 * bitstring pattern value.
	 *
	 * Each element occupies one byte. Meaning of values:
	 * 0 -> 0, 1 -> 1, 2 -> ?, 3 -> *
	 */
	private List<Byte> pattern_value;

	//TODO: implement
	//private DecMatchStruct dec_match;

	public TitanBitString_template () {
		//do nothing
	}

	public TitanBitString_template (final template_sel otherValue) {
		super(otherValue);
		checkSingleSelection(otherValue);
	}

	public TitanBitString_template (final List<Byte> otherValue, final int aNoBits) {
		super(template_sel.SPECIFIC_VALUE);
		single_value = new TitanBitString(otherValue, aNoBits);
	}

	public TitanBitString_template (final TitanBitString otherValue) {
		super(template_sel.SPECIFIC_VALUE);
		otherValue.mustBound("Creating a template from an unbound bitstring value.");
		single_value = new TitanBitString(otherValue);
	}

	public TitanBitString_template (final TitanBitString_Element otherValue){
		super(template_sel.SPECIFIC_VALUE);
		single_value = new TitanBitString((byte)(otherValue.get_bit() ? 1 : 0));
	}

	public TitanBitString_template(final Optional<TitanBitString> otherValue){
		switch (otherValue.getSelection()) {
		case OPTIONAL_PRESENT:
			setSelection(template_sel.SPECIFIC_VALUE);
			single_value = new TitanBitString(otherValue.constGet());
			break;
		case OPTIONAL_OMIT:
			setSelection(template_sel.OMIT_VALUE);
			break;
		case OPTIONAL_UNBOUND:
			throw new TtcnError("Creating a bitstring template from an unbound optional field.");
		}
	}
	
	public TitanBitString_template (final TitanBitString_template otherValue) {
		copyTemplate(otherValue);
	}

	//originally clean_up
	public void cleanUp() {
		switch (templateSelection) {
		case SPECIFIC_VALUE:
			single_value = null;
			break;
		case VALUE_LIST:
		case COMPLEMENTED_LIST:
			value_list.clear();
			value_list = null;
		default:
			break;
		}
		templateSelection = template_sel.UNINITIALIZED_TEMPLATE;
	}

	//originally operator=
	public TitanBitString_template assign( final template_sel otherValue ) {
		checkSingleSelection(otherValue);
		cleanUp();
		setSelection(otherValue);

		return this;
	}

	//originally operator=
	public TitanBitString_template assign( final List<Byte> otherValue, final int aNoBits ) {
		cleanUp();
		setSelection(template_sel.SPECIFIC_VALUE);
		single_value = new TitanBitString(otherValue, aNoBits);

		return this;
	}

	//originally operator=
	public TitanBitString_template assign( final TitanBitString otherValue ) {
		otherValue.mustBound("Assignment of an unbound bitstring value to a template.");

		cleanUp();
		setSelection(template_sel.SPECIFIC_VALUE);
		single_value = new TitanBitString(otherValue);

		return this;
	}

	//originally operator=
	public TitanBitString_template assign( final TitanBitString_Element otherValue ){
		otherValue.mustBound("Assignment of an unbound bitstring element to a template.");
		cleanUp();
		setSelection(template_sel.SPECIFIC_VALUE);
		single_value = new TitanBitString((byte)(otherValue.get_bit() ? 1 : 0));
		return this;
		
	}

	//originally operator=
	public TitanBitString_template assign( final TitanBitString_template otherValue ) {
		if (otherValue != this) {
			cleanUp();
			copyTemplate(otherValue);
		}

		return this;
	}

	//originally operator=
	public TitanBitString_template assign(final Optional<TitanBitString> otherValue){
		cleanUp();
		switch (otherValue.getSelection()) {
		case OPTIONAL_PRESENT:
			setSelection(template_sel.SPECIFIC_VALUE);
			single_value = new TitanBitString(otherValue.constGet());
			break;
		case OPTIONAL_OMIT:
			setSelection(template_sel.OMIT_VALUE);
			break;
		case OPTIONAL_UNBOUND:
			throw new TtcnError("Assignment of an unbound optional field to a bitstring template.");
		}
		return this;
	}

	private void copyTemplate(final TitanBitString_template otherValue) {
		switch (otherValue.templateSelection) {
		case SPECIFIC_VALUE:
			single_value = new TitanBitString(otherValue.single_value);
			break;
		case OMIT_VALUE:
		case ANY_VALUE:
		case ANY_OR_OMIT:
			break;
		case VALUE_LIST:
		case COMPLEMENTED_LIST:
			value_list = new ArrayList<TitanBitString_template>(otherValue.value_list.size());
			for(int i = 0; i < otherValue.value_list.size(); i++) {
				final TitanBitString_template temp = new TitanBitString_template(otherValue.value_list.get(i));
				value_list.add(temp);
			}
			break;
		default:
			throw new TtcnError("Copying an uninitialized/unsupported bitstring template.");
		}

		setSelection(otherValue);
	}

	//originally operator[](int)
	public TitanBitString_Element getAt( final int index_value ) {
		if (templateSelection != template_sel.SPECIFIC_VALUE || is_ifPresent) {
			throw new TtcnError("Accessing a bitstring element of a non-specific bitstring template.");
		}

		return single_value.getAt( index_value );
	}

	//originally operator[](const INTEGER&)
	public TitanBitString_Element getAt( final TitanInteger index_value ) {
		index_value.mustBound("Indexing a bitstring template with an unbound integer value.");

		return getAt( index_value.getInt() );
	}

	//originally operator[](int) const
	public TitanBitString_Element constGetAt( final int index_value ) {
		if (templateSelection != template_sel.SPECIFIC_VALUE || is_ifPresent) {
			throw new TtcnError("Accessing a bitstring element of a non-specific bitstring template.");
		}

		return single_value.constGetAt( index_value );
	}

	//originally operator[](const INTEGER&) const
	public TitanBitString_Element constGetAt( final TitanInteger index_value) {
		index_value.mustBound("Indexing a bitstring template with an unbound integer value.");

		return constGetAt( index_value.getInt() );
	}

	// originally match
	public TitanBoolean match(final TitanBitString otherValue) {
		return match(otherValue, false);
	}

	// originally match
	public TitanBoolean match(final TitanBitString otherValue, final boolean legacy) {
		if(! otherValue.isBound()) {
			return new TitanBoolean(false);
		}

		switch (templateSelection) {
		case SPECIFIC_VALUE:
			return single_value.operatorEquals( otherValue );
		case OMIT_VALUE:
			return new TitanBoolean(false);
		case ANY_VALUE:
		case ANY_OR_OMIT:
			return new TitanBoolean(true);
		case VALUE_LIST:
		case COMPLEMENTED_LIST:
			for(int i = 0 ; i < value_list.size(); i++) {
				if(value_list.get(i).match(otherValue, legacy).getValue()) {
					return new TitanBoolean(templateSelection == template_sel.VALUE_LIST);
				}
			}
			return new TitanBoolean(templateSelection == template_sel.COMPLEMENTED_LIST);
		case STRING_PATTERN:{
			//TODO: implement
		}
		default:
			throw new TtcnError("Matching with an uninitialized/unsupported bitstring template.");
		}
	}

	// originally valueof
	public TitanBitString valueOf() {
		if (templateSelection != template_sel.SPECIFIC_VALUE || is_ifPresent) {
			throw new TtcnError("Performing a valueof or send operation on a non-specific bitstring template.");
		}

		return single_value;
	}

	// originally lengthof
	public int lengthOf() {
		if (is_ifPresent) {
			throw new TtcnError("Performing lengthof() operation on a bitstring template which has an ifpresent attribute.");
		}
		int min_length = 0;
		boolean has_any_or_none = false;
		switch (templateSelection) {
		case SPECIFIC_VALUE:
			min_length = single_value.lengthOf().getInt();
			has_any_or_none = false;
			break;
		case OMIT_VALUE:
			throw new TtcnError("Performing lengthof() operation on a bitstring template containing omit value.");
		case ANY_VALUE:
		case ANY_OR_OMIT:
			min_length = 0;
			has_any_or_none = true;
			break;
		case VALUE_LIST:
			// error if any element does not have length or the lengths differ
			if (value_list.isEmpty()) {
				throw new TtcnError("Internal error: Performing lengthof() operation on a bitstring template containing an empty list.");
			}
			int item_length = value_list.get(0).lengthOf();
			for (int i = 1; i < value_list.size(); i++) {
				if (value_list.get(i).lengthOf() != item_length) {
					throw new TtcnError("Performing lengthof() operation on a bitstring template containing a value list with different lengths.");
				}
			}
			min_length = item_length;
			has_any_or_none = false;
			break;
		case COMPLEMENTED_LIST:
			throw new TtcnError("Performing lengthof() operation on a bitstring template containing complemented list.");
		case STRING_PATTERN:
			min_length = 0;
			has_any_or_none = false;
			for (int i = 0; i < pattern_value.size(); i++) {
				if (pattern_value.get(i) < 3) { // case of 1, 0, ?
					min_length++;
				} else {
					has_any_or_none = true;
				}
			}
			break;
		default:
			throw new TtcnError("Performing lengthof() operation on an uninitialized/unsupported bitstring template.");
		}

		//FIXME implement check_section_is_single 
		return min_length;
	}


	//originally set_type
	public void setType(template_sel templateType, int listLength /* = 0 */){
		if(templateType != template_sel.VALUE_LIST && templateType != template_sel.COMPLEMENTED_LIST &&
				templateType != template_sel.DECODE_MATCH){
			throw new TtcnError("Setting an invalid list type for a bitstring template.");
		}
		cleanUp();
		setSelection(templateType);
		if(templateType != template_sel.DECODE_MATCH){
			value_list = new ArrayList<TitanBitString_template>(listLength);
			//FIXME: check the correction
			value_list.add(new TitanBitString_template(this.constGetAt(listLength)));
		}
	}

	public TitanBitString_template listItem(final int listIndex) {
		if (templateSelection != template_sel.VALUE_LIST && templateSelection != template_sel.COMPLEMENTED_LIST) {
			throw new TtcnError("Accessing a list element of a non-list bitstring template.");
		}
		if (listIndex >= value_list.size()) {
			throw new TtcnError("Index overflow in a bitstring value list template.");
		}

		return value_list.get(listIndex);
	}

	// originally is_present
	public boolean isPresent(boolean legacy) {
		if (templateSelection == template_sel.UNINITIALIZED_TEMPLATE) {
			return false;
		}

		return !match_omit(legacy);
	}

	public boolean match_omit() {
		return match_omit(false);
	}

	public boolean match_omit(boolean legacy) {
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
			// else fall through
		}
		return false;
	}

}
