/******************************************************************************
 * Copyright (c) 2000-2017 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;
import java.time.temporal.ValueRange;
import java.util.ArrayList;
import org.eclipse.titan.runtime.core.Base_Template.template_sel;
import java.text.MessageFormat;

//TODO: Not yet complete rewrite
/**
 * TTCN-3 charstring template
 *
 * @author Arpad Lovassy
 * @author Andrea Pálfi
 */
public class TitanCharString_template extends Restricted_Length_Template {

	TitanCharString single_value;

	// value_list part
	ArrayList<TitanCharString_template> value_list;

	// value range part
	boolean min_is_set, max_is_set;
	boolean min_is_exclusive, max_is_exclusive;
	TitanCharString min_value, max_value;

	//TODO: implement: pattern_value part for STRING_PATTERN case

	public TitanCharString_template () {
		//do  nothing
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

	//TODO: implement getAt
	//TODO: implement constGetAt

	// originally is_present
	public boolean isPresent(final boolean legacy) {
		if (templateSelection == template_sel.UNINITIALIZED_TEMPLATE) {
			return false;
		}

		return !match_omit(legacy);
	}
	
	// originally match
	public TitanBoolean match(final TitanCharString otherValue) {
		return match(otherValue, false);
	}

	// originally match
	public TitanBoolean match(final TitanCharString otherValue, final boolean legacy) {
		if(! otherValue.isBound()) {
			return new TitanBoolean(false);
		}

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
			for(int i = 0 ; i < value_list.size(); i++) {
				if(value_list.get(i).match(otherValue, legacy).getValue()) {
					return new TitanBoolean(templateSelection == template_sel.VALUE_LIST);
				}
			}
			return new TitanBoolean(templateSelection == template_sel.COMPLEMENTED_LIST);
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
					return new TitanBoolean(false);
				}
			}
			return new TitanBoolean(true);
		}
		case STRING_PATTERN:{
			//TODO: implement
		}
		default:
			throw new TtcnError("Matching with an uninitialized/unsupported charstring template.");
		}
	}

	public TitanCharString valueOf() {
		if (templateSelection != template_sel.SPECIFIC_VALUE) {
			throw new TtcnError("Performing a valueof or send operation on a non-specific charstring template.");
		}

		return new TitanCharString(single_value);
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
			value_list = new ArrayList<TitanCharString_template>(listLength);
			for(int i = 0; i < listLength; i++) {
				value_list.add(new TitanCharString_template());
			}
			break;
		case VALUE_RANGE:
			setSelection(template_sel.VALUE_RANGE);
			min_is_set = false;
			max_is_set = false;
			min_is_exclusive = false;
			max_is_exclusive = false;
			break;
		case DECODE_MATCH:
			setSelection(template_sel.DECODE_MATCH);
			break;
		default:
			throw new TtcnError("Setting an invalid type for a charstring template.");
		}
	}

	// originally list_item
	public TitanCharString_template listItem(final int listIndex) {
		if (templateSelection != template_sel.VALUE_LIST && templateSelection != template_sel.COMPLEMENTED_LIST) {
			throw new TtcnError("Internal error: Accessing a list element of a non-list charstring template. ");
		}
		if (listIndex < 0) {
			throw new TtcnError("Internal error: Indexing a charstring value list template with a negative index.");
		}
		if (listIndex >= value_list.size()) {
			throw new TtcnError("Internal error: Index overflow in a charstring value list template.");
		}

		return value_list.get(listIndex);
	}

	//originally match_omit
	public boolean match_omit(final boolean legacy) {
		if (is_ifPresent) {
			return true;
		}
		switch(templateSelection) {
		case OMIT_VALUE:
		case ANY_OR_OMIT:
			return true;
		case VALUE_LIST:
		case COMPLEMENTED_LIST:
			if (legacy) {
				// legacy behavior: 'omit' can appear in the value/complement list
				for (int i = 0; i < value_list.size(); i++) {
					if (value_list.get(i).match_omit(legacy)) {
						return templateSelection == template_sel.VALUE_LIST;
					}
				}
				return templateSelection == template_sel.COMPLEMENTED_LIST;
			}
			return false;
		default:
			return false;
		}
	}

	//FIXME: lengthOf
	// originally lengthOf
	public TitanInteger lengthOf() {
		int min_length;
		boolean has_any_or_none;
		if(is_ifPresent){
			throw new TtcnError("Performing lengthof() operation on a charstring template which has an ifpresent attribute.");
		}switch(templateSelection){
		case SPECIFIC_VALUE: 
			min_length = single_value.lengthOf().getInt();
			has_any_or_none = false;
			break;
		case OMIT_VALUE:
			throw new TtcnError("Performing lengthof() operation on a charstring template containing omit value.");
		case ANY_VALUE:
		case ANY_OR_OMIT:
		case VALUE_RANGE:
			min_length=0;
			has_any_or_none=true;
			break;
		case VALUE_LIST: 
			//error if any element does not have length or the lengths differ
			if(value_list.size()<1){
				throw new TtcnError("Internal error: Performing lengthof() operation on a charstring template containing an empty list.");
			}
			int item_length = value_list.get(0).lengthOf().getInt();
			for (int i = 1; i < value_list.size(); i++) {
				if(value_list.get(i).lengthOf().getInt() != item_length){
					throw new TtcnError("Performing lengthof() operation on a charstring template containing a value list with different lengths.");
				}
			}
			min_length=item_length;
			has_any_or_none=false;
			break;
		case COMPLEMENTED_LIST:
			throw new TtcnError("Performing lengthof() operation on a charstring template containing complemented list.");
		case STRING_PATTERN:
			throw new TtcnError("Performing lengthof() operation on a charstring template containing a pattern is not allowed.");
		default:
			throw new TtcnError("Performing lengthof() operation on an uninitialized/unsupported charstring template.");
		}
		//TODO: implement check_section_is_single
		return new TitanInteger(min_length);
	}

	//FIXME: set_min
	//originally set_min

	public void setMin(final TitanCharString otherMinValue) {
		if (templateSelection != template_sel.VALUE_RANGE) {
			throw new TtcnError("Setting the lower bound for a non-range charstring template.");
		}
		otherMinValue.mustBound("Setting an unbound value as lower bound in a charstring value range template.");
		int length=otherMinValue.lengthOf().getInt();
		if(length!=1){
			throw new TtcnError(MessageFormat.format("The length of the lower bound in a charstring value range template must be 1 instead of '{0}''. ", length));
		}
		min_is_set=true;
		min_is_exclusive=false;
		min_value = new TitanCharString(otherMinValue);
		if((max_is_set) && min_value.lengthOf().getInt() > (max_value.lengthOf().getInt()) ){
			throw new TtcnError(MessageFormat.format("The lower bound {0} in a charstring value range template is greater than the upper bound {1}.", min_value, max_value));
		}
	}


	// FIXME: set_max
	// originally set_max

	public void setMax(final TitanCharString otherMaxValue)
	{
		if(templateSelection != template_sel.VALUE_RANGE){
			throw new TtcnError("Setting the upper bound for a non-range charstring template.");
		}
		otherMaxValue.mustBound("Setting an unbound value as upper bound in a charstring value range template.");
		int length=otherMaxValue.lengthOf().getInt();
		if(length!=1){
			throw new TtcnError(MessageFormat.format("The length of the upper bound in a charstring value range template must be 1 instead of {0}.", length));
		}
		max_is_set=true;
		max_is_exclusive=false;
		max_value=new TitanCharString(otherMaxValue);
		if((min_is_set) && min_value.lengthOf().getInt() > max_value.lengthOf().getInt()){
			throw new TtcnError(MessageFormat.format("The upper bound `{0}'' in a charstring value range template is smaller than the lower bound {1}.", max_value, min_value));
		}
	}

	// originally set_min_exclusive
	public void setMinExclusive(final boolean minExclusive) {
		if(templateSelection!=template_sel.VALUE_RANGE){
			throw new TtcnError("Setting the lower bound for a non-range charstring template.");
		}
		min_is_exclusive = minExclusive;
	}

	// originally set_max_exclusive
	public void setMaxExclusive(final boolean maxExclusive) {
		if(templateSelection!=template_sel.VALUE_RANGE){
			throw new TtcnError("Setting the upper bound for a non-range charstring template.");
		}
		max_is_exclusive = maxExclusive;
	}

	//TODO: test isPresent
	//TODO: test lengthOf
	//TODO: test setType
	//TODO: test setMax
	//TODO: test setMin
	//TODO: test setMinExclusive
	//TODO: test setMaxExclusive
}