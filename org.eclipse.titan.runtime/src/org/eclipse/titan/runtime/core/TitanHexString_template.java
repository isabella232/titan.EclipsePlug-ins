/******************************************************************************
 * Copyright (c) 2000-2017 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * TTCN-3 hexstring template
 *
 * @author Arpad Lovassy
 * @author Gergo Ujhelyi
 * @author Andrea Pálfi
 */
public class TitanHexString_template extends Restricted_Length_Template {

	private TitanHexString single_value;

	// value_list part
	private ArrayList<TitanHexString_template> value_list;

	/**
	 * hexstring pattern value.
	 *
	 * Each element occupies one byte. Meaning of values:
	 * 0 .. 15 -> 0 .. F, 16 -> ?, 17 -> *
	 */
	private List<Byte> pattern_value;

	//TODO: implement: dec_match part

	public TitanHexString_template () {
		//do nothing
	}

	public TitanHexString_template (final template_sel otherValue) {
		super(otherValue);
		checkSingleSelection(otherValue);
	}

	public TitanHexString_template (final List<Byte> otherValue) {
		super(template_sel.SPECIFIC_VALUE);
		single_value = new TitanHexString(otherValue);
	}

	public TitanHexString_template (final TitanHexString otherValue) {
		super(template_sel.SPECIFIC_VALUE);
		otherValue.mustBound("Creating a template from an unbound hexstring value.");

		single_value = new TitanHexString(otherValue);
	}

	public TitanHexString_template (final TitanHexString_template otherValue) {
		copyTemplate(otherValue);
	}

	public TitanHexString_template(final TitanHexString_Element otherValue) {
		super(template_sel.SPECIFIC_VALUE);
		single_value = new TitanHexString(otherValue);
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

	@Override
	public TitanHexString_template assign(final Base_Type otherValue) {
		if (otherValue instanceof TitanHexString) {
			return assign((TitanHexString) otherValue);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to hexstring", otherValue));
	}

	@Override
	public TitanHexString_template assign(final Base_Template otherValue) {
		if (otherValue instanceof TitanHexString_template) {
			return assign((TitanHexString_template) otherValue);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to hexstring", otherValue));
	}

	//originally operator=
	public TitanHexString_template assign( final template_sel otherValue ) {
		checkSingleSelection(otherValue);
		cleanUp();
		setSelection(otherValue);

		return this;
	}

	//originally operator=
	public TitanHexString_template assign( final List<Byte> otherValue ) {
		cleanUp();
		setSelection(template_sel.SPECIFIC_VALUE);
		single_value = new TitanHexString(otherValue);

		return this;
	}

	//originally operator=
	public TitanHexString_template assign( final TitanHexString otherValue ) {
		otherValue.mustBound("Assignment of an unbound hexstring value to a template.");

		cleanUp();
		setSelection(template_sel.SPECIFIC_VALUE);
		single_value = new TitanHexString(otherValue);

		return this;
	}

	// originally operator=
	public TitanHexString_template assign(final TitanHexString_Element otherValue) {
		otherValue.mustBound("Assignment of an unbound hexstring element to a template.");

		cleanUp();
		setSelection(template_sel.SPECIFIC_VALUE);
		single_value = new TitanHexString(otherValue);

		return this;
	}

	//originally operator=
	public TitanHexString_template assign( final TitanHexString_template otherValue ) {
		if (otherValue != this) {
			cleanUp();
			copyTemplate(otherValue);
		}

		return this;
	}

	private void copyTemplate(final TitanHexString_template otherValue) {
		switch (otherValue.templateSelection) {
		case SPECIFIC_VALUE:
			single_value = new TitanHexString(otherValue.single_value);
			break;
		case OMIT_VALUE:
		case ANY_VALUE:
		case ANY_OR_OMIT:
			break;
		case VALUE_LIST:
		case COMPLEMENTED_LIST:
			value_list = new ArrayList<TitanHexString_template>(otherValue.value_list.size());
			for(int i = 0; i < otherValue.value_list.size(); i++) {
				final TitanHexString_template temp = new TitanHexString_template(otherValue.value_list.get(i));
				value_list.add(temp);
			}
			break;
		default:
			throw new TtcnError("Copying an uninitialized/unsupported hexstring template.");
		}

		setSelection(otherValue);
	}

	//originally operator[](int)
	public TitanHexString_Element getAt( final int index_value ) {
		if (templateSelection != template_sel.SPECIFIC_VALUE || is_ifPresent) {
			throw new TtcnError("Accessing a hexstring element of a non-specific hexstring template.");
		}

		return single_value.getAt( index_value );
	}

	//originally operator[](const INTEGER&)
	public TitanHexString_Element getAt( final TitanInteger index_value ) {
		index_value.mustBound("Indexing a hexstring template with an unbound integer value.");

		return getAt( index_value.getInt() );
	}

	//originally operator[](int) const
	public TitanHexString_Element constGetAt( final int index_value ) {
		if (templateSelection != template_sel.SPECIFIC_VALUE || is_ifPresent) {
			throw new TtcnError("Accessing a hexstring element of a non-specific hexstring template.");
		}

		return single_value.constGetAt( index_value );
	}

	//originally operator[](const INTEGER&) const
	public TitanHexString_Element constGetAt( final TitanInteger index_value) {
		index_value.mustBound("Indexing a hexstring template with an unbound integer value.");

		return constGetAt( index_value.getInt() );
	}

	@Override
	public TitanBoolean match(final Base_Type otherValue, final boolean legacy) {
		if (otherValue instanceof TitanHexString) {
			return match((TitanHexString) otherValue, legacy);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to hexstring", otherValue));
	}

	@Override
	public void log_match(final Base_Type match_value, final boolean legacy) {
		if (match_value instanceof TitanHexString) {
			log_match((TitanHexString) match_value, legacy);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to hexstring", match_value));
	}

	// originally match
	public TitanBoolean match(final TitanHexString otherValue) {
		return match(otherValue, false);
	}

	// originally match
	public TitanBoolean match(final TitanHexString otherValue, final boolean legacy) {
		if(!otherValue.isBound().getValue()) {
			return new TitanBoolean(false);
		}

		final TitanInteger value_length = otherValue.lengthOf();
		if(!match_length(value_length.getInt())) {
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
			throw new TtcnError("Matching with an uninitialized/unsupported hexstring template.");
		}
	}

	// originally valueof
	public TitanHexString valueOf() {
		if (templateSelection != template_sel.SPECIFIC_VALUE || is_ifPresent) {
			throw new TtcnError("Performing a valueof or send operation on a non-specific hexstring template.");
		}

		return single_value;
	}

	// originally lengthof
	public TitanInteger lengthOf() {
		if (is_ifPresent) {
			throw new TtcnError("Performing lengthof() operation on a hexstring template which has an ifpresent attribute.");
		}

		int min_length = 0;
		boolean has_any_or_none = false;
		switch (templateSelection) {
		case SPECIFIC_VALUE:
			min_length = single_value.lengthOf().getInt();
			break;
		case OMIT_VALUE:
			throw new TtcnError("Performing lengthof() operation on a hexstring template containing omit value.");
		case ANY_VALUE:
		case ANY_OR_OMIT:
			has_any_or_none = true;
			break;
		case VALUE_LIST:
			// error if any element does not have length or the lengths differ
			if (value_list.isEmpty()) {
				throw new TtcnError("Internal error: Performing lengthof() operation on a hexstring template containing an empty list.");
			}
			final int item_length = value_list.get(0).lengthOf().getInt();
			for (int i = 1; i < value_list.size(); i++) {
				if(value_list.get(i).lengthOf().getInt() != item_length){
					throw new TtcnError("Performing lengthof() operation on a hexstring template containing a value list with different lengths.");
				}
			}
			min_length = item_length;
			break;
		case COMPLEMENTED_LIST:
			throw new TtcnError("Performing lengthof() operation on a hexstring template containing complemented list.");
		case STRING_PATTERN:
			has_any_or_none = false; // TRUE if * chars in the pattern
			for (int i = 0; i < pattern_value.size(); i++) {
				if (pattern_value.get(i) < 17) {
					min_length++; // case of 0-F, ?
				} else {
					has_any_or_none = true; // case of * character
				}
			}
			break;
		default:
			throw new TtcnError("Performing lengthof() operation on an uninitialized/unsupported hexstring template.");
		}
		return new TitanInteger(check_section_is_single(min_length, has_any_or_none, "length", "a" , "hexstring template"));
	}

	// originally set_type
	public void setType(final template_sel templateType, final int listLength) {
		if (templateType != template_sel.VALUE_LIST && templateType != template_sel.COMPLEMENTED_LIST
				&& templateType != template_sel.DECODE_MATCH) {
			throw new TtcnError("Setting an invalid list type for a hexstring template.");
		}

		cleanUp();
		setSelection(templateType);
		if (templateType != template_sel.DECODE_MATCH) {
			value_list = new ArrayList<TitanHexString_template>(listLength);
			for (int i = 0; i < listLength; i++) {
				value_list.add(new TitanHexString_template());
			}
		}
	}

	// originally list_item
	public TitanHexString_template listItem(final int listIndex) {
		if (templateSelection != template_sel.VALUE_LIST && templateSelection != template_sel.COMPLEMENTED_LIST) {
			throw new TtcnError("Accessing a list element of a non-list hexstring template.");
		}
		if (listIndex < 0) {
			throw new TtcnError("Accessing an hexstring value list template using a negative index (" + listIndex + ").");
		}
		if (listIndex >= value_list.size()) {
			throw new TtcnError("Index overflow in a hexstring value list template.");
		}

		return value_list.get(listIndex);
	}

	public void log() {
		switch (templateSelection) {
		case SPECIFIC_VALUE:
			single_value.log();
			break;
		case COMPLEMENTED_LIST:
			TtcnLogger.log_event_str("complement ");
		case VALUE_LIST:
			TtcnLogger.log_char('(');
			for (int i = 0; i < value_list.size(); i++) {
				if (i > 0) {
					TtcnLogger.log_event_str(", ");
				}
				value_list.get(i).log();
			}
			TtcnLogger.log_char(')');
			break;
		case STRING_PATTERN:
			// TODO: implement STRING_PATTERN
			TtcnLogger.log_event_str("'H");
			break;
		case DECODE_MATCH:
			TtcnLogger.log_event_str("decmatch ");
			// TODO: dec_match->instance->log();
			break;
		default:
			log_generic();
			break;
		}
		log_restricted();
		log_ifpresent();
	}

	public void log_match(final TitanHexString match_value, boolean legacy) {
		if (TtcnLogger.matching_verbosity_t.VERBOSITY_COMPACT == TtcnLogger.get_matching_verbosity()
				&& TtcnLogger.get_logmatch_buffer_len() != 0) {
			TtcnLogger.print_logmatch_buffer();
			TtcnLogger.log_event_str(" := ");
		}
		match_value.log();
		TtcnLogger.log_event_str(" with ");
		log();
		if (match(match_value).getValue()) {
			TtcnLogger.log_event_str(" matched");
		} else {
			TtcnLogger.log_event_str(" unmatched");
		}
	}

	// originally is_present (with default parameter)
	public TitanBoolean isPresent() {
		return isPresent(false);
	}

	// originally is_present
	public TitanBoolean isPresent(final boolean legacy) {
		if (templateSelection == template_sel.UNINITIALIZED_TEMPLATE) {
			return new TitanBoolean(false);
		}

		return match_omit(legacy).not();
	}

	// originally matc_omit (with default parameter)
	public TitanBoolean match_omit() {
		return match_omit(false);
	}

	public TitanBoolean match_omit(final boolean legacy) {
		if (is_ifPresent) {
			return new TitanBoolean(true);
		}

		switch (templateSelection) {
		case OMIT_VALUE:
		case ANY_OR_OMIT:
			return new TitanBoolean(true);
		case VALUE_LIST:
		case COMPLEMENTED_LIST:
			if (legacy) {
				// legacy behavior: 'omit' can appear in the value/complement list
				for (int i = 0; i < value_list.size(); i++) {
					if (value_list.get(i).match_omit().getValue()) {
						return new TitanBoolean(templateSelection == template_sel.VALUE_LIST);
					}
				}
				return new TitanBoolean(templateSelection == template_sel.COMPLEMENTED_LIST);
			}
			return new TitanBoolean(false);
		default:
			return new TitanBoolean(false);
		}
	}
}
