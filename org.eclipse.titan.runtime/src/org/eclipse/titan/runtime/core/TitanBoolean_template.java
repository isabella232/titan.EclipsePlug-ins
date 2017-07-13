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
 * TTCN-3 boolean template
 *
 * @author Kristof Szabados
 * @author Arpad Lovassy
 */
public class TitanBoolean_template extends Base_Template {
	// single_value part
	private TitanBoolean single_value;

	// value_list part
	private ArrayList<TitanBoolean_template> value_list;

	public TitanBoolean_template () {
		//do nothing
	}

	public TitanBoolean_template (final template_sel otherValue) {
		super(otherValue);
		checkSingleSelection(otherValue);
	}

	public TitanBoolean_template (final boolean otherValue) {
		super(template_sel.SPECIFIC_VALUE);
		single_value = new TitanBoolean(otherValue);
	}

	public TitanBoolean_template (final TitanBoolean otherValue) {
		super(template_sel.SPECIFIC_VALUE);
		otherValue.mustBound("Creating a template from an unbound boolean value.");

		single_value = new TitanBoolean(otherValue);
	}

	public TitanBoolean_template (final TitanBoolean_template otherValue) {
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
	public TitanBoolean_template assign( final template_sel otherValue ) {
		checkSingleSelection(otherValue);
		cleanUp();
		setSelection(otherValue);

		return this;
	}

	//originally operator=
	public TitanBoolean_template assign( final boolean otherValue ) {
		cleanUp();
		setSelection(template_sel.SPECIFIC_VALUE);
		single_value = new TitanBoolean(otherValue);

		return this;
	}

	//originally operator=
	public TitanBoolean_template assign( final TitanBoolean otherValue ) {
		otherValue.mustBound("Assignment of an unbound boolean value to a template.");

		cleanUp();
		setSelection(template_sel.SPECIFIC_VALUE);
		single_value = new TitanBoolean(otherValue);

		return this;
	}

	//originally operator=
	public TitanBoolean_template assign( final TitanBoolean_template otherValue ) {
		if (otherValue != this) {
			cleanUp();
			copyTemplate(otherValue);
		}

		return this;
	}

	private void copyTemplate(final TitanBoolean_template otherValue) {
		switch (otherValue.templateSelection) {
		case SPECIFIC_VALUE:
			single_value = new TitanBoolean(otherValue.single_value);
			break;
		case OMIT_VALUE:
		case ANY_VALUE:
		case ANY_OR_OMIT:
			break;
		case VALUE_LIST:
		case COMPLEMENTED_LIST:
			value_list = new ArrayList<TitanBoolean_template>(otherValue.value_list.size());
			for(int i = 0; i < otherValue.value_list.size(); i++) {
				final TitanBoolean_template temp = new TitanBoolean_template(otherValue.value_list.get(i));
				value_list.add(temp);
			}
			break;
		default:
			throw new TtcnError("Copying an uninitialized/unsupported boolean template.");
		}

		setSelection(otherValue);
	}

	public TitanBoolean and( final TitanBoolean otherValue ) {
		if ( templateSelection != template_sel.SPECIFIC_VALUE ) {
			throw new TtcnError( "And operation of a non specific value template" );
		}

		return single_value.and( otherValue );
	}

	public TitanBoolean and( final TitanBoolean_template otherTemplate ) {
		if ( otherTemplate.templateSelection != template_sel.SPECIFIC_VALUE ) {
			throw new TtcnError( "And operation of a non specific value template argument" );
		}

		return and( otherTemplate.single_value );
	}

	public TitanBoolean operatorEquals( final TitanBoolean otherValue ) {
		if ( templateSelection != template_sel.SPECIFIC_VALUE ) {
			throw new TtcnError( "Equals operation of a non specific value template" );
		}

		return single_value.operatorEquals( otherValue );
	}

	// match
	public TitanBoolean match(final boolean otherValue) {
		return match(otherValue, false);
	}

	public TitanBoolean match(final boolean otherValue, final boolean legacy) {
		switch (templateSelection) {
		case SPECIFIC_VALUE:
			return single_value.operatorEquals(otherValue);
		case OMIT_VALUE:
			return new TitanBoolean(false);
		case ANY_VALUE:
		case ANY_OR_OMIT:
			return new TitanBoolean(true);
		case VALUE_LIST:
		case COMPLEMENTED_LIST:
			for (int i = 0; i < value_list.size(); i++) {
				if (value_list.get(i).match(otherValue, legacy).getValue()) {
					return new TitanBoolean(templateSelection == template_sel.VALUE_LIST);
				}
			}
			return new TitanBoolean(templateSelection == template_sel.COMPLEMENTED_LIST);
		default:
			throw new TtcnError("Matching with an uninitialized/unsupported boolean template.");
		}
	}

	public TitanBoolean match(final TitanBoolean otherValue) {
		return match(otherValue, false);
	}

	public TitanBoolean match(final TitanBoolean otherValue, final boolean legacy) {
		if (!otherValue.isBound()) {
			return new TitanBoolean(false);
		}

		return match(otherValue.getValue(), legacy);
	}

	// valueof
	public TitanBoolean valueOf() {
		if (templateSelection != template_sel.SPECIFIC_VALUE || is_ifPresent) {
			throw new TtcnError("Performing valueof or send operation on a non-specific boolean template.");
		}

		return single_value;
	}
}

