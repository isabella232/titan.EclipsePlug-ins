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
 * TTCN-3 universal charstring template
 * 
 * @author Arpad Lovassy
 */
public class TitanUniversalCharString_template extends Base_Template {

	private TitanUniversalCharString single_value;

	// value_list part
	private ArrayList<TitanUniversalCharString_template> value_list;

	// value range part
	private boolean min_is_set, max_is_set;
	private boolean min_is_exclusive, max_is_exclusive;
	private TitanUniversalChar min_value, max_value;

	//TODO: implement: pattern_value part for STRING_PATTERN case

	public TitanUniversalCharString_template () {
		//do nothing
	}

	public TitanUniversalCharString_template (final template_sel otherValue) {
		super(otherValue);
		checkSingleSelection(otherValue);
	}

	public TitanUniversalCharString_template (final String otherValue) {
		super(template_sel.SPECIFIC_VALUE);
		single_value = new TitanUniversalCharString(otherValue);
	}

	public TitanUniversalCharString_template (final TitanUniversalCharString otherValue) {
		super(template_sel.SPECIFIC_VALUE);
		otherValue.mustBound("Creating a template from an unbound universal charstring value.");
		single_value = new TitanUniversalCharString(otherValue);
	}

	public TitanUniversalCharString_template (final TitanUniversalCharString_template otherValue) {
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
		case VALUE_RANGE:
			min_value = null;
			max_value = null;
		default:
			break;
		}
		templateSelection = template_sel.UNINITIALIZED_TEMPLATE;
	}

	//originally operator=
	public TitanUniversalCharString_template assign( final template_sel otherValue ) {
		checkSingleSelection(otherValue);
		cleanUp();
		setSelection(otherValue);

		return this;
	}

	//originally operator=
	public TitanUniversalCharString_template assign( final String otherValue ) {
		cleanUp();
		setSelection(template_sel.SPECIFIC_VALUE);
		single_value = new TitanUniversalCharString(otherValue);

		return this;
	}

	//originally operator=
	public TitanUniversalCharString_template assign( final TitanUniversalCharString otherValue ) {
		otherValue.mustBound("Assignment of an unbound universal charstring value to a template.");
		cleanUp();
		setSelection(template_sel.SPECIFIC_VALUE);
		single_value = new TitanUniversalCharString(otherValue);
		
		return this;
	}

	//originally operator=
	public TitanUniversalCharString_template assign( final TitanUniversalCharString_template otherValue ) {
		if (otherValue != this) {
			cleanUp();
			copyTemplate(otherValue);
		}

		return this;
	}

	private void copyTemplate(final TitanUniversalCharString_template otherValue) {
		switch (otherValue.templateSelection) {
		case SPECIFIC_VALUE:
			single_value = new TitanUniversalCharString(otherValue.single_value);
			break;
		case OMIT_VALUE:
		case ANY_VALUE:
		case ANY_OR_OMIT:
			break;
		case VALUE_LIST:
		case COMPLEMENTED_LIST:
			value_list = new ArrayList<TitanUniversalCharString_template>(otherValue.value_list.size());
			for(int i = 0; i < otherValue.value_list.size(); i++) {
				final TitanUniversalCharString_template temp = new TitanUniversalCharString_template(otherValue.value_list.get(i));
				value_list.add(temp);
			}
			break;
		case VALUE_RANGE:
			min_is_set = otherValue.min_is_set;
			min_is_exclusive = otherValue.min_is_exclusive;
			if(min_is_set) {
				min_value = new TitanUniversalChar(otherValue.min_value);
			}
			max_is_set = otherValue.max_is_set;
			max_is_exclusive = otherValue.max_is_exclusive;
			if(max_is_set) {
				max_value = new TitanUniversalChar(otherValue.max_value);
			}
			break;
		default:
			throw new TtcnError("Copying an uninitialized/unsupported universal charstring template.");
		}

		setSelection(otherValue);
	}

	// originally match
	public boolean match(final TitanUniversalCharString otherValue) {
		return match(otherValue, false);
	}

	private boolean matchLength( final TitanUniversalCharString otherValue ) {
		return value_list.size() == otherValue.getValue().size();
	}

	// originally match
	public boolean match(final TitanUniversalCharString otherValue, final boolean legacy) {
		if(! otherValue.isBound()) {
			return false;
		}
		final List<TitanUniversalChar> otherStr = otherValue.getValue();
		final int otherLen = otherStr.size();
		if ( !matchLength( otherValue ) ) {
			return false;
		}
		switch (templateSelection) {
		case SPECIFIC_VALUE:
			return single_value.operatorEquals(otherValue);
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
		case VALUE_RANGE:{
			if (!min_is_set) {
				throw new TtcnError("The lower bound is not set when " +
					"matching with a universal charstring value range template.");
			}
			if (!max_is_set) {
				throw new TtcnError("The upper bound is not set when " +
					"matching with a universal charstring value range template.");
			}
			for (int i = 0; i < otherLen; i++) {
				final TitanUniversalChar uc = otherStr.get( i );
				if ( uc.lessThan( min_value ) || max_value.lessThan( uc ) ) {
					return false;
				} else if ( ( min_is_exclusive && uc.operatorEquals( min_value ) ) || ( max_is_exclusive && uc.operatorEquals( max_value ) ) ) {
					return false;
				}
			}
			return true;
		}
		case STRING_PATTERN:{
			//TODO: implement
		}
		default:
			throw new TtcnError("Matching with an uninitialized/unsupported universal charstring template.");
		}
	}
}
