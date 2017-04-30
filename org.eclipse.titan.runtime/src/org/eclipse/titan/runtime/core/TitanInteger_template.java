/******************************************************************************
 * Copyright (c) 2000-2017 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

import java.util.ArrayList;

/**
 * TTCN-3 integer template
 * 
 * Not yet complete rewrite
 */
public class TitanInteger_template extends Base_Template {
	// int_val part
	// TODO maybe should be renamed in core
	private TitanInteger single_value;
	
	// value_list part
	private ArrayList<TitanInteger_template> value_list;
	
	// value range part
	private boolean min_is_present, max_is_present;
	private boolean min_is_exclusive, max_is_exclusive;
	private TitanInteger min_value, max_value;
	
	
	public TitanInteger_template () {
		//do nothing
	}
	
	public TitanInteger_template (final template_sel otherValue) {
		super(otherValue);
		checkSingleSelection(otherValue);
	}
	
	public TitanInteger_template (final int otherValue) {
		super(template_sel.SPECIFIC_VALUE);
		single_value = new TitanInteger(otherValue);
	}
	
	public TitanInteger_template (final TitanInteger otherValue) {
		super(template_sel.SPECIFIC_VALUE);
		otherValue.mustBound("Creating a template from an unbound integer value.");
		single_value = new TitanInteger(otherValue);
	}
	
	public TitanInteger_template (final TitanInteger_template otherValue) {
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
	public TitanInteger_template assign( final template_sel otherValue ) {
		checkSingleSelection(otherValue);
		cleanUp();
		setSelection(otherValue);

		return this;
	}
	
	//originally operator=
	public TitanInteger_template assign( final int otherValue ) {
		cleanUp();
		setSelection(template_sel.SPECIFIC_VALUE);
		single_value = new TitanInteger(otherValue);
		
		return this;
	}

	//originally operator=
	public TitanInteger_template assign( final TitanInteger otherValue ) {
		otherValue.mustBound("Assignment of an unbound integer value to a template.");
		cleanUp();
		setSelection(template_sel.SPECIFIC_VALUE);
		single_value = new TitanInteger(otherValue);
		
		return this;
	}

	//originally operator=
	public TitanInteger_template assign( final TitanInteger_template otherValue ) {
		if (otherValue != this) {
			cleanUp();
			copyTemplate(otherValue);
		}
		
		return this;
	}
	
	private void copyTemplate(final TitanInteger_template otherValue) {
		switch (otherValue.templateSelection) {
		case SPECIFIC_VALUE:
			single_value = new TitanInteger(otherValue.single_value);
			break;
		case OMIT_VALUE:
		case ANY_VALUE:
		case ANY_OR_OMIT:
			break;
		case VALUE_LIST:
		case COMPLEMENTED_LIST:
			value_list = new ArrayList<TitanInteger_template>(otherValue.value_list.size());
			for(int i = 0; i < otherValue.value_list.size(); i++) {
				final TitanInteger_template temp = new TitanInteger_template(otherValue.value_list.get(i));
				value_list.add(temp);
			}
			break;
		case VALUE_RANGE:
			min_is_present = otherValue.min_is_present;
			min_is_exclusive = otherValue.min_is_exclusive;
			if(min_is_present) {
				min_value = new TitanInteger(otherValue.min_value);
			}
			max_is_present = otherValue.max_is_present;
			max_is_exclusive = otherValue.max_is_exclusive;
			if(max_is_present) {
				max_value = new TitanInteger(otherValue.max_value);
			}
			break;
		default:
			throw new TtcnError("Copying an uninitialized/unsupported integer template.");
		}

		setSelection(otherValue);
	}
	
	// originally match
	public boolean match(final TitanInteger otherValue) {
		return match(otherValue, false);
	}

	// originally match
	public boolean match(final TitanInteger otherValue, final boolean legacy) {
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
			boolean lowerMissMatch = true;
			boolean upperMissMatch = true;
			if(min_is_present) {
				if(min_is_exclusive) {
					lowerMissMatch = min_value.isLessThanOrEqual(otherValue);
				} else {
					lowerMissMatch = min_value.isLessThan(otherValue);
				}
			}
			if(max_is_present) {
				if (max_is_exclusive) {
					upperMissMatch = min_value.isGreaterThanOrEqual(otherValue);
				} else {
					upperMissMatch = min_value.isGreaterThan(otherValue);
				}
			}
			return lowerMissMatch && upperMissMatch;
		}
		default:
			throw new TtcnError("Matching with an uninitialized/unsupported integer template.");
		}
	}
	
	// originally set_type
	public void setType(final template_sel templateType) {
		setType(templateType, 0);
	}

	// originally set_type
	public void setType(final template_sel templateType, final int listLength) {
		cleanUp();
		switch (templateType) {
		case VALUE_LIST:
		case COMPLEMENTED_LIST:
			setSelection(templateType);
			value_list = new ArrayList<TitanInteger_template>(listLength);
			for(int i = 0; i < listLength; i++) {
				value_list.add(new TitanInteger_template());
			}
			break;
		case VALUE_RANGE:
			setSelection(template_sel.VALUE_RANGE);
			min_is_present = false;
			max_is_present = false;
			min_is_exclusive = false;
			max_is_exclusive = false;
			break;
		default:
			throw new TtcnError("Setting an invalid type for an integer template.");
		}
	}
	
	// originally list_iem
	public TitanInteger_template listItem(final int listIndex) {
		if (!template_sel.VALUE_LIST.equals(templateSelection) &&
			!template_sel.COMPLEMENTED_LIST.equals(templateSelection)) {
			throw new TtcnError("Accessing a list element of a non-list integer template.");
		}
		
		if (listIndex > value_list.size()) {
			throw new TtcnError("Index overflow in an integer value list template.");
		}
		
		return value_list.get(listIndex);
	}
	
	// originally set_min
	public void setMin(final int otherMinValue) {
		if (!template_sel.VALUE_RANGE.equals(templateSelection)) {
			throw new TtcnError("Integer template is not range when setting lower limit.");
		}
		
		if (max_is_present) {
			if (!max_value.isGreaterThanOrEqual(otherMinValue)) {
				throw new TtcnError("The lower limit of the range is greater than the upper limit in an integer template.");
			}
		}
		
		min_is_present = true;
		min_is_exclusive = false;
		min_value = new TitanInteger(otherMinValue);
	}
	
	// originally set_min
	public void setMin(final TitanInteger otherMinValue) {
		otherMinValue.mustBound("Using an unbound value when setting the lower bound in an integer range template.");
		
		if (!template_sel.VALUE_RANGE.equals(templateSelection)) {
			throw new TtcnError("Integer template is not range when setting lower limit.");
		}
		
		if (max_is_present) {
			if (!max_value.isGreaterThanOrEqual(otherMinValue)) {
				throw new TtcnError("The lower limit of the range is greater than the upper limit in an integer template.");
			}
		}
		
		min_is_present = true;
		min_is_exclusive = false;
		min_value = otherMinValue;
	}
	
	//originally set_min_exclusive
	public void setMinExclusive(final boolean minExclusive) {
		min_is_exclusive = minExclusive;
	}
	
	// originally set_max
	public void setMax(final int otherMaxValue) {
		if (!template_sel.VALUE_RANGE.equals(templateSelection)) {
			throw new TtcnError("Integer template is not range when setting upper limit.");
		}

		if (min_is_present) {
			if (min_value.isGreaterThan(otherMaxValue)) {
				throw new TtcnError("The upper limit of the range is smaller than the lower limit in an integer template.");
			}
		}

		max_is_present = true;
		max_is_exclusive = false;
		max_value = new TitanInteger(otherMaxValue);
	}

	// originally set_max
	public void setMax(final TitanInteger otherMaxValue) {
		otherMaxValue.mustBound("Using an unbound value when setting the upper bound in an integer range template.");

		if (!template_sel.VALUE_RANGE.equals(templateSelection)) {
			throw new TtcnError("Integer template is not range when setting upper limit.");
		}

		if (max_is_present) {
			if (!max_value.isGreaterThan(otherMaxValue)) {
				throw new TtcnError("TThe upper limit of the range is smaller than the lower limit in an integer template.");
			}
		}

		max_is_present = true;
		max_is_exclusive = false;
		max_value = otherMaxValue;
	}
	
	//originally set_max_exclusive
	public void setMaxExclusive(final boolean maxExclusive) {
		max_is_exclusive = maxExclusive;
	}
}
