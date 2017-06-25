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

	//TODO: implement constructor with bitstring element
	//TODO: implement constructor with optional element
	
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

	//TODO: implement BITSTRING_template::assign for bitstring element
	//TODO: implement BITSTRING_template::assign for optional

	//originally operator=
	public TitanBitString_template assign( final TitanBitString_template otherValue ) {
		if (otherValue != this) {
			cleanUp();
			copyTemplate(otherValue);
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

	//TODO: implement BITSTRING_template::valueof
	//TODO: implement BITSTRING_template::lengthof
	//TODO: implement BITSTRING_template::list_item
	//TODO: implement BITSTRING_template::ispresent
	//TODO: implement BITSTRING_template::match_omit

}
