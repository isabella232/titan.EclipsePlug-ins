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
				TitanInteger_template temp = new TitanInteger_template(otherValue.value_list.get(i));
				value_list.add(temp);
			}
			break;
		case VALUE_RANGE:
			min_is_present = otherValue.min_is_present;
			min_is_exclusive = otherValue.min_is_exclusive;
			if(min_is_present) {
				min_value = new TitanInteger(otherValue.min_value);
			}
			if(max_is_present) {
				max_value = new TitanInteger(otherValue.max_value);
			}
			max_is_present = otherValue.max_is_present;
			otherValue.max_is_exclusive = otherValue.max_is_exclusive;
			break;
		default:
			throw new TtcnError("Copying an uninitialized/unsupported integer template.");
		}

		setSelection(otherValue);
	}
}
