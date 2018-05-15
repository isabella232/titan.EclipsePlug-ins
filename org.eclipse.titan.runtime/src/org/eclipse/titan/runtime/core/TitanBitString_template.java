/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
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
 * TTCN-3 bitstring template
 *
 * @author Arpad Lovassy
 * @author Gergo Ujhelyi
 */
public class TitanBitString_template extends Restricted_Length_Template {

	/**
	 * pattern table, converts value to printable character
	 */
	private static final char patterns[] = { '0', '1', '?', '*' };

	private TitanBitString single_value;

	// value_list part
	private List<TitanBitString_template> value_list;

	/**
	 * bitstring pattern value.
	 *
	 * Each element occupies one byte. Meaning of values:
	 * 0 -> 0, 1 -> 1, 2 -> ?, 3 -> *
	 */
	private int pattern_value[];

	/** reference counter for pattern_value */
	private int pattern_value_ref_count;

	private DecMatchStruct dec_match;

	/** reference counter for dec_match */
	private int dec_match_ref_count;

	public TitanBitString_template() {
		// do nothing
	}

	public TitanBitString_template(final template_sel otherValue) {
		super(otherValue);
		checkSingleSelection(otherValue);
	}

	public TitanBitString_template(final int otherValue[], final int aNoBits) {
		super(template_sel.SPECIFIC_VALUE);
		single_value = new TitanBitString(otherValue, aNoBits);
	}

	public TitanBitString_template(final TitanBitString otherValue) {
		super(template_sel.SPECIFIC_VALUE);
		otherValue.mustBound("Creating a template from an unbound bitstring value.");
		single_value = new TitanBitString(otherValue);
	}

	public TitanBitString_template(final TitanBitString_Element otherValue) {
		super(template_sel.SPECIFIC_VALUE);
		single_value = new TitanBitString((byte) (otherValue.get_bit() ? 1 : 0));
	}

	public TitanBitString_template(final Optional<TitanBitString> otherValue) {
		switch (otherValue.get_selection()) {
		case OPTIONAL_PRESENT:
			set_selection(template_sel.SPECIFIC_VALUE);
			single_value = new TitanBitString(otherValue.constGet());
			break;
		case OPTIONAL_OMIT:
			set_selection(template_sel.OMIT_VALUE);
			break;
		case OPTIONAL_UNBOUND:
			throw new TtcnError("Creating a bitstring template from an unbound optional field.");
		}
	}

	public TitanBitString_template(final TitanBitString_template otherValue) {
		copyTemplate(otherValue);
	}

	public TitanBitString_template(final int pattern_elements[]) {
		super(template_sel.STRING_PATTERN);
		pattern_value = TitanStringUtils.copyIntegerList(pattern_elements);
	}

	public TitanBitString_template(final String patternString) {
		super(template_sel.STRING_PATTERN);
		pattern_value = patternString2List(patternString);
	}

	private static int[] patternString2List(final String patternString) {
		if (patternString == null) {
			throw new TtcnError("Internal error: bitstring pattern is null.");
		}
		final int result[] = new int[patternString.length()];
		for (int i = 0; i < patternString.length(); i++) {
			final char patternChar = patternString.charAt(i);
			result[i] = patternChar2byte(patternChar);
		}
		return result;
	}

	private static int patternChar2byte(final char patternChar) {
		for (int j = 0; j < patterns.length; j++) {
			if (patternChar == patterns[j]) {
				return j;
			}
		}
		throw new TtcnError("Internal error: invalid element in bitstring pattern.");
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
		case STRING_PATTERN:
			if (pattern_value_ref_count > 1) {
				pattern_value_ref_count--;
			} else if (pattern_value_ref_count == 1) {
				pattern_value = null;
			} else {
				throw new TtcnError("Internal error: Invalid reference counter in a bitstring pattern.");
			}
			break;
		case DECODE_MATCH:
			if (dec_match_ref_count > 1) {
				dec_match_ref_count--;
			}
			else if (dec_match_ref_count == 1) {
				dec_match = null;
			}
			else {
				throw new TtcnError("Internal error: Invalid reference counter in a decoded content match.");
			}
			break;
		default:
			break;
		}
		templateSelection = template_sel.UNINITIALIZED_TEMPLATE;
	}

	@Override
	public TitanBitString_template assign(final Base_Type otherValue) {
		if (otherValue instanceof TitanBitString) {
			return assign((TitanBitString) otherValue);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to bitstring", otherValue));
	}

	@Override
	public TitanBitString_template assign(final Base_Template otherValue) {
		if (otherValue instanceof TitanBitString_template) {
			return assign((TitanBitString_template) otherValue);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to bitstring", otherValue));
	}

	// originally operator=
	public TitanBitString_template assign(final template_sel otherValue) {
		checkSingleSelection(otherValue);
		cleanUp();
		set_selection(otherValue);

		return this;
	}

	// originally operator=
	public TitanBitString_template assign(final int otherValue[], final int aNoBits) {
		cleanUp();
		set_selection(template_sel.SPECIFIC_VALUE);
		single_value = new TitanBitString(otherValue, aNoBits);

		return this;
	}

	// originally operator=
	public TitanBitString_template assign(final TitanBitString otherValue) {
		otherValue.mustBound("Assignment of an unbound bitstring value to a template.");

		cleanUp();
		set_selection(template_sel.SPECIFIC_VALUE);
		single_value = new TitanBitString(otherValue);

		return this;
	}

	// originally operator=
	public TitanBitString_template assign(final TitanBitString_Element otherValue) {
		otherValue.mustBound("Assignment of an unbound bitstring element to a template.");
		cleanUp();
		set_selection(template_sel.SPECIFIC_VALUE);
		single_value = new TitanBitString((byte) (otherValue.get_bit() ? 1 : 0));
		return this;

	}

	// originally operator=
	public TitanBitString_template assign(final TitanBitString_template otherValue) {
		if (otherValue != this) {
			cleanUp();
			copyTemplate(otherValue);
		}

		return this;
	}

	// originally operator=
	public TitanBitString_template assign(final Optional<TitanBitString> otherValue) {
		cleanUp();
		switch (otherValue.get_selection()) {
		case OPTIONAL_PRESENT:
			set_selection(template_sel.SPECIFIC_VALUE);
			single_value = new TitanBitString(otherValue.constGet());
			break;
		case OPTIONAL_OMIT:
			set_selection(template_sel.OMIT_VALUE);
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
			for (int i = 0; i < otherValue.value_list.size(); i++) {
				final TitanBitString_template temp = new TitanBitString_template(otherValue.value_list.get(i));
				value_list.add(temp);
			}
			break;
		case STRING_PATTERN:
			//TODO: use copyList()
			pattern_value = otherValue.pattern_value;
			pattern_value_ref_count++;
			break;
		case DECODE_MATCH:
			//TODO: use copyList()
			dec_match = otherValue.dec_match;
			dec_match_ref_count++;
			break;
		default:
			throw new TtcnError("Copying an uninitialized/unsupported bitstring template.");
		}

		set_selection(otherValue);
	}

	// originally operator[](int)
	public TitanBitString_Element getAt(final int index_value) {
		if (templateSelection != template_sel.SPECIFIC_VALUE || is_ifPresent) {
			throw new TtcnError("Accessing a bitstring element of a non-specific bitstring template.");
		}

		return single_value.getAt(index_value);
	}

	// originally operator[](const INTEGER&)
	public TitanBitString_Element getAt(final TitanInteger index_value) {
		index_value.mustBound("Indexing a bitstring template with an unbound integer value.");

		return getAt(index_value.getInt());
	}

	// originally operator[](int) const
	public TitanBitString_Element constGetAt(final int index_value) {
		if (templateSelection != template_sel.SPECIFIC_VALUE || is_ifPresent) {
			throw new TtcnError("Accessing a bitstring element of a non-specific bitstring template.");
		}

		return single_value.constGetAt(index_value);
	}

	// originally operator[](const INTEGER&) const
	public TitanBitString_Element constGetAt(final TitanInteger index_value) {
		index_value.mustBound("Indexing a bitstring template with an unbound integer value.");

		return constGetAt(index_value.getInt());
	}

	@Override
	public boolean match(final Base_Type otherValue, final boolean legacy) {
		if (otherValue instanceof TitanBitString) {
			return match((TitanBitString) otherValue, legacy);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to bitstring", otherValue));
	}

	@Override
	public void log_match(final Base_Type match_value, final boolean legacy) {
		if (match_value instanceof TitanBitString) {
			log_match((TitanBitString) match_value, legacy);
			return;
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to bitstring", match_value));
	}

	// originally match
	public boolean match(final TitanBitString otherValue) {
		return match(otherValue, false);
	}

	// originally match
	public boolean match(final TitanBitString otherValue, final boolean legacy) {
		if (!otherValue.isBound()) {
			return false;
		}

		final TitanInteger value_length = otherValue.lengthOf();
		if (!match_length(value_length.getInt())) {
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
			for (int i = 0; i < value_list.size(); i++) {
				if (value_list.get(i).match(otherValue, legacy)) {
					return templateSelection == template_sel.VALUE_LIST;
				}
			}
			return templateSelection == template_sel.COMPLEMENTED_LIST;
		case STRING_PATTERN:
			return match_pattern(pattern_value, otherValue);
		//TODO: implement
		//case DECODE_MATCH:
		default:
			throw new TtcnError("Matching with an uninitialized/unsupported bitstring template.");
		}
	}

	/**
	 * This is the same algorithm that match_array uses
	 * to match 'record of' types.
	 * The only differences are: how two elements are matched and
	 * how an asterisk or ? is identified in the template
	 */
	private boolean match_pattern(final int string_pattern[], final TitanBitString string_value) {
		final int stringPatternSize = string_pattern.length;
		final int stringValueNBits = string_value.getNBits();
		if (stringPatternSize == 0) {
			return stringValueNBits == 0;
		}

		int value_index = 0;
		int template_index = 0;
		int last_asterisk = -1;
		int last_value_to_asterisk = -1;

		for (;;) {
			switch (string_pattern[template_index]) {
			case 0:
				if (!string_value.getBit(value_index)) {
					value_index++;
					template_index++;
				} else {
					if (last_asterisk == -1) {
						return false;
					}
					template_index = last_asterisk + 1;
					value_index = ++last_value_to_asterisk;
				}
				break;
			case 1:
				if (string_value.getBit(value_index)) {
					value_index++;
					template_index++;
				} else {
					if (last_asterisk == -1) {
						return false;
					}
					template_index = last_asterisk + 1;
					value_index = ++last_value_to_asterisk;
				}
				break;
			case 2:
				// we found a ? element, it matches anything
				value_index++;
				template_index++;
				break;
			case 3:
				// we found an asterisk
				last_asterisk = template_index++;
				last_value_to_asterisk = value_index;
				break;
			default:
				throw new TtcnError("Internal error: invalid element in bitstring pattern.");
			}

			if (value_index == stringValueNBits && template_index == stringPatternSize) {
				return true;
			} else if (template_index == stringPatternSize) {
				if (string_pattern[template_index - 1] == 3) {
					return true;
				} else if (last_asterisk == -1) {
					return false;
				} else {
					template_index = last_asterisk + 1;
					value_index = ++last_value_to_asterisk;
				}
			} else if (value_index == stringValueNBits) {
				while (template_index < stringPatternSize && string_pattern[template_index] == 3) {
					template_index++;
				}
				return template_index == stringPatternSize;
			}
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
	public TitanInteger lengthOf() {
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
			final int item_length = value_list.get(0).lengthOf().getInt();
			for (int i = 1; i < value_list.size(); i++) {
				if (value_list.get(i).lengthOf().getInt() != item_length) {
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
			for (int i = 0; i < pattern_value.length; i++) {
				if (pattern_value[i] < 3) { // case of 1, 0, ?
					min_length++;
				} else {
					has_any_or_none = true;
				}
			}
			break;
		default:
			throw new TtcnError("Performing lengthof() operation on an uninitialized/unsupported bitstring template.");
		}

		return new TitanInteger(check_section_is_single(min_length, has_any_or_none, "length", "a", "bitstring template"));
	}


	//originally set_type
	public void setType(final template_sel templateType, final int listLength /* = 0 */) {
		if(templateType != template_sel.VALUE_LIST && templateType != template_sel.COMPLEMENTED_LIST &&
				templateType != template_sel.DECODE_MATCH) {
			throw new TtcnError("Setting an invalid list type for a bitstring template.");
		}
		cleanUp();
		set_selection(templateType);
		if (templateType != template_sel.DECODE_MATCH) {
			value_list = new ArrayList<TitanBitString_template>(listLength);
			for (int i = 0; i < listLength; i++) {
				value_list.add(new TitanBitString_template());
			}
		}
	}

	public TitanBitString_template listItem(final int listIndex) {
		if (templateSelection != template_sel.VALUE_LIST && templateSelection != template_sel.COMPLEMENTED_LIST) {
			throw new TtcnError("Accessing a list element of a non-list bitstring template.");
		}
		if (listIndex < 0) {
			throw new TtcnError("Accessing an bitstring value list template using a negative index (" + listIndex + ").");
		}
		if (listIndex >= value_list.size()) {
			throw new TtcnError("Index overflow in a bitstring value list template.");
		}

		return value_list.get(listIndex);
	}

	public void log() {
		switch (templateSelection) {
		case SPECIFIC_VALUE:
			single_value.log();
			break;
		case COMPLEMENTED_LIST:
			TtcnLogger.log_event_str("complement");
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
			TtcnLogger.log_char('\'');
			for (int i = 0; i < pattern_value.length; i++) {
				final int pattern = pattern_value[i];
				if (pattern < 4) {
					TtcnLogger.log_char(patterns[pattern]);
				} else {
					TtcnLogger.log_event_str("<unknown>");
				}
			}
			TtcnLogger.log_event_str("'B");
			break;
		case DECODE_MATCH:
			TtcnLogger.log_event_str("decmatch ");
			dec_match.log();
			break;
		default:
			log_generic();
			break;
		}
		log_restricted();
		log_ifpresent();
	}

	public void log_match(final TitanBitString match_value, final boolean legacy) {
		if (TtcnLogger.matching_verbosity_t.VERBOSITY_COMPACT == TtcnLogger.get_matching_verbosity()
				&& TtcnLogger.get_logmatch_buffer_len() != 0) {
			TtcnLogger.print_logmatch_buffer();
			TtcnLogger.log_event_str(" := ");
		}
		match_value.log();
		TtcnLogger.log_event_str(" with ");
		log();
		if (match(match_value)) {
			TtcnLogger.log_event_str(" matched");
		} else {
			TtcnLogger.log_event_str(" unmatched");
		}
	}

	// originally is_present (with default parameter)
	public boolean isPresent() {
		return isPresent(false);
	}

	public boolean isPresent(final boolean legacy) {
		if (templateSelection == template_sel.UNINITIALIZED_TEMPLATE) {
			return false;
		}

		return !match_omit(legacy);
	}

	public boolean match_omit() {
		return match_omit(false);
	}

	public boolean match_omit(final boolean legacy) {
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
				for (int i = 0; i < value_list.size(); i++) {
					if (value_list.get(i).match_omit()) {
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

	@Override
	/** {@inheritDoc} */
	public void encode_text(final Text_Buf text_buf) {
		encode_text_restricted(text_buf);

		switch (templateSelection) {
		case OMIT_VALUE:
		case ANY_VALUE:
		case ANY_OR_OMIT:
			break;
		case SPECIFIC_VALUE:
			single_value.encode_text(text_buf);
			break;
		case VALUE_LIST:
		case COMPLEMENTED_LIST:
			text_buf.push_int(value_list.size());
			for (int i = 0; i < value_list.size(); i++) {
				value_list.get(i).encode_text(text_buf);
			}
			break;
		case STRING_PATTERN:
			text_buf.push_int(pattern_value.length);
			byte[] temp = new byte[pattern_value.length];
			for (int i = 0; i < pattern_value.length; i++) {
				temp[i] = (byte) pattern_value[i];
			}
			text_buf.push_raw((pattern_value.length + 7) / 8, temp);
			break;
		default:
			throw new TtcnError("Text encoder: Encoding an uninitialized/unsupported bitstring template.");
		}
	}

	@Override
	/** {@inheritDoc} */
	public void decode_text(final Text_Buf text_buf) {
		cleanUp();
		decode_text_restricted(text_buf);

		switch (templateSelection) {
		case OMIT_VALUE:
		case ANY_VALUE:
		case ANY_OR_OMIT:
			break;
		case SPECIFIC_VALUE:
			single_value.decode_text(text_buf);
			break;
		case VALUE_LIST:
		case COMPLEMENTED_LIST:
			value_list = new ArrayList<TitanBitString_template>(text_buf.pull_int().getInt());
			for (int i = 0; i < value_list.size(); i++) {
				final TitanBitString_template temp = new TitanBitString_template();
				temp.decode_text(text_buf);
				value_list.add(temp);
			}
			break;
		case STRING_PATTERN: {
			final int n_elements = text_buf.pull_int().getInt();
			pattern_value = new int[n_elements];
			final byte[] temp = new byte[n_elements];
			text_buf.pull_raw(n_elements, temp);
			for (int i = 0; i < n_elements; i++) {
				pattern_value[i] = (int) temp[i];
			}
			break;
		}
		default:
			throw new TtcnError("Text decoder: An unknown/unsupported selection was received for a bitstring template.");
		}
	}
}
