/******************************************************************************
 * Copyright (c) 2000-2017 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

import java.util.ArrayList;

//TODO: Not yet complete rewrite
/**
 * TTCN-3 charstring template
 *
 * @author Arpad Lovassy
 */
public class TitanCharString_template extends Base_Template {

	private TitanCharString single_value;

	// value_list part
	private ArrayList<TitanCharString_template> value_list;

	// value range part
	private boolean min_is_set, max_is_set;
	private boolean min_is_exclusive, max_is_exclusive;
	private TitanCharString min_value, max_value;

	//TODO: implement: pattern_value part for STRING_PATTERN case

	public TitanCharString_template () {
		//do nothing
	}

	public TitanCharString_template (final template_sel otherValue) {
		super(otherValue);
		checkSingleSelection(otherValue);
	}

	public TitanCharString_template (final String otherValue) {
		super(template_sel.SPECIFIC_VALUE);
		single_value = new TitanCharString(otherValue);
	}

	public TitanCharString_template (final TitanCharString otherValue) {
		super(template_sel.SPECIFIC_VALUE);
		otherValue.mustBound("Creating a template from an unbound charstring value.");

		single_value = new TitanCharString(otherValue);
	}

	public TitanCharString_template (final TitanCharString_template otherValue) {
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
	public TitanCharString_template assign( final template_sel otherValue ) {
		checkSingleSelection(otherValue);
		cleanUp();
		setSelection(otherValue);

		return this;
	}

	//originally operator=
	public TitanCharString_template assign( final String otherValue ) {
		cleanUp();
		setSelection(template_sel.SPECIFIC_VALUE);
		single_value = new TitanCharString(otherValue);

		return this;
	}

	//originally operator=
	public TitanCharString_template assign( final TitanCharString otherValue ) {
		otherValue.mustBound("Assignment of an unbound charstring value to a template.");

		cleanUp();
		setSelection(template_sel.SPECIFIC_VALUE);
		single_value = new TitanCharString(otherValue);

		return this;
	}

	//originally operator=
	public TitanCharString_template assign( final TitanCharString_template otherValue ) {
		if (otherValue != this) {
			cleanUp();
			copyTemplate(otherValue);
		}

		return this;
	}

	private void copyTemplate(final TitanCharString_template otherValue) {
		switch (otherValue.templateSelection) {
		case SPECIFIC_VALUE:
			single_value = new TitanCharString(otherValue.single_value);
			break;
		case OMIT_VALUE:
		case ANY_VALUE:
		case ANY_OR_OMIT:
			break;
		case VALUE_LIST:
		case COMPLEMENTED_LIST:
			value_list = new ArrayList<TitanCharString_template>(otherValue.value_list.size());
			for(int i = 0; i < otherValue.value_list.size(); i++) {
				final TitanCharString_template temp = new TitanCharString_template(otherValue.value_list.get(i));
				value_list.add(temp);
			}
			break;
		case VALUE_RANGE:
			min_is_set = otherValue.min_is_set;
			min_is_exclusive = otherValue.min_is_exclusive;
			if(min_is_set) {
				min_value = new TitanCharString(otherValue.min_value);
			}
			max_is_set = otherValue.max_is_set;
			max_is_exclusive = otherValue.max_is_exclusive;
			if(max_is_set) {
				max_value = new TitanCharString(otherValue.max_value);
			}
			break;
		default:
			throw new TtcnError("Copying an uninitialized/unsupported charstring template.");
		}

		setSelection(otherValue);
	}

	// originally match
	public boolean match(final TitanCharString otherValue) {
		return match(otherValue, false);
	}

	// originally match
	public boolean match(final TitanCharString otherValue, final boolean legacy) {
		if(! otherValue.isBound()) {
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
						"matching with a charstring value range template.");
			}

			if (!max_is_set) {
				throw new TtcnError("The upper bound is not set when " +
						"matching with a charstring value range template.");
			}
			final char minValueChar = min_value.getValue().charAt( 0 );
			final char maxValueChar = max_value.getValue().charAt( 0 );
			if (minValueChar > maxValueChar) {
				throw new TtcnError("The lower bound (\"" + minValueChar + "\") is greater than the upper bound " +
						"(\"" + maxValueChar + "\") when matching with a charstring value range template.");
			}
			final StringBuilder otherStr = otherValue.getValue();
			int min_value_offset = 0;
			int max_value_offset = 0;
			if (min_is_exclusive) {
				min_value_offset = 1;
			}
			if (max_is_exclusive) {
				max_value_offset = 1;
			}
			final int otherLen = otherStr.length();
			for (int i = 0; i < otherLen; i++) {
				if ( otherStr.charAt( i ) < (minValueChar + min_value_offset) ||
						otherStr.charAt( i ) > (maxValueChar - max_value_offset)) {
					return false;
				}
			}
			return true;
		}
		case STRING_PATTERN:{
			//TODO: implement
		}
		default:
			throw new TtcnError("Matching with an uninitialized/unsupported charstring template.");
		}
	}
}
