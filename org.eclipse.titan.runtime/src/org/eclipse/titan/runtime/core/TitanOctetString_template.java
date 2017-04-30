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
 * TTCN-3 octetstring template
 *
 * @author Arpad Lovassy
 */
public class TitanOctetString_template extends Base_Template {

	private TitanOctetString single_value;

	// value_list part
	private ArrayList<TitanOctetString_template> value_list;

	/**
	 * octetstring pattern value
	 *
	 * Each element is represented as an unsigned short. Meaning of values:
	 * 0 .. 255 -> 00 .. FF, 256 -> ?, 257 -> *
	 */
	private List<Character> pattern_value;

	//TODO: implement: dec_match part

	public TitanOctetString_template () {
		//do nothing
	}

	public TitanOctetString_template (final template_sel otherValue) {
		super(otherValue);
		checkSingleSelection(otherValue);
	}

	public TitanOctetString_template (final List<Character> otherValue) {
		super(template_sel.SPECIFIC_VALUE);
		single_value = new TitanOctetString(otherValue);
	}

	public TitanOctetString_template (final TitanOctetString otherValue) {
		super(template_sel.SPECIFIC_VALUE);
		otherValue.mustBound("Creating a template from an unbound octetstring value.");
		single_value = new TitanOctetString(otherValue);
	}

	public TitanOctetString_template (final TitanOctetString_template otherValue) {
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
	public TitanOctetString_template assign( final template_sel otherValue ) {
		checkSingleSelection(otherValue);
		cleanUp();
		setSelection(otherValue);

		return this;
	}

	//originally operator=
	public TitanOctetString_template assign( final List<Character> otherValue ) {
		cleanUp();
		setSelection(template_sel.SPECIFIC_VALUE);
		single_value = new TitanOctetString(otherValue);

		return this;
	}

	//originally operator=
	public TitanOctetString_template assign( final TitanOctetString otherValue ) {
		otherValue.mustBound("Assignment of an unbound octetstring value to a template.");
		cleanUp();
		setSelection(template_sel.SPECIFIC_VALUE);
		single_value = new TitanOctetString(otherValue);

		return this;
	}

	//originally operator=
	public TitanOctetString_template assign( final TitanOctetString_template otherValue ) {
		if (otherValue != this) {
			cleanUp();
			copyTemplate(otherValue);
		}

		return this;
	}

	private void copyTemplate(final TitanOctetString_template otherValue) {
		switch (otherValue.templateSelection) {
		case SPECIFIC_VALUE:
			single_value = new TitanOctetString(otherValue.single_value);
			break;
		case OMIT_VALUE:
		case ANY_VALUE:
		case ANY_OR_OMIT:
			break;
		case VALUE_LIST:
		case COMPLEMENTED_LIST:
			value_list = new ArrayList<TitanOctetString_template>(otherValue.value_list.size());
			for(int i = 0; i < otherValue.value_list.size(); i++) {
				final TitanOctetString_template temp = new TitanOctetString_template(otherValue.value_list.get(i));
				value_list.add(temp);
			}
			break;
		default:
			throw new TtcnError("Copying an uninitialized/unsupported octetstring template.");
		}

		setSelection(otherValue);
	}

	//originally operator[](int)
	public TitanOctetString_Element getAt( final int index_value ) {
		if (templateSelection != template_sel.SPECIFIC_VALUE || is_ifPresent) {
			throw new TtcnError("Accessing a octetstring element of a non-specific octetstring template.");
		}
		return single_value.getAt( index_value );
	}

	//originally operator[](const INTEGER&)
	public TitanOctetString_Element getAt( final TitanInteger index_value ) {
		index_value.mustBound("Indexing a octetstring template with an unbound integer value.");
		return getAt( index_value.getInt() );
	}

	//originally operator[](int) const
	public TitanOctetString_Element constGetAt( final int index_value ) {
		if (templateSelection != template_sel.SPECIFIC_VALUE || is_ifPresent) {
			throw new TtcnError("Accessing a octetstring element of a non-specific octetstring template.");
		}
		return single_value.constGetAt( index_value );
	}

	//originally operator[](const INTEGER&) const
	TitanOctetString_Element constGetAt( final TitanInteger index_value) {
		index_value.mustBound("Indexing a octetstring template with an unbound integer value.");
		return constGetAt( index_value.getInt() );
	}

	// originally match
	public boolean match(final TitanOctetString otherValue) {
		return match(otherValue, false);
	}

	// originally match
	public boolean match(final TitanOctetString otherValue, final boolean legacy) {
		if(! otherValue.isBound()) {
			return false;
		}

		switch (templateSelection) {
		case SPECIFIC_VALUE:
			return single_value.operatorEquals( otherValue );
		case OMIT_VALUE:
			return false;
		case ANY_VALUE:
		case ANY_OR_OMIT:
			return true;
		case VALUE_LIST:
		case COMPLEMENTED_LIST:
			for(int i = 0 ; i < value_list.size(); i++) {
				if(value_list.get(i).match(otherValue, legacy)) {
					return templateSelection == template_sel.VALUE_LIST;
				}
			}
			return templateSelection == template_sel.COMPLEMENTED_LIST;
		case STRING_PATTERN:{
			//TODO: implement
		}
		default:
			throw new TtcnError("Matching with an uninitialized/unsupported octetstring template.");
		}
	}

	@Override
	public String toString() {
		if ( pattern_value == null ) {
			return "<unbound>";
		}
		final StringBuilder sb = new StringBuilder();
		final int size = pattern_value.size();
		for ( int i = 0; i < size; i++ ) {
			final Character digit = pattern_value.get( i );
			if ( digit == 256 ) {
				sb.append( '?' );
			} else if ( digit == 257 ) {
				sb.append( '*' );
			} else {
				sb.append( TitanHexString.HEX_DIGITS.charAt( digit >> 8 ) );
				sb.append( TitanHexString.HEX_DIGITS.charAt( digit & 0xFF ) );
			}
		}
		return sb.toString();
	}
}
