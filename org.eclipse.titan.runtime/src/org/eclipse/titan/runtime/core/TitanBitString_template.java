/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.titan.runtime.core.Base_Type.TTCN_Typedescriptor;
import org.eclipse.titan.runtime.core.Param_Types.Module_Parameter;
import org.eclipse.titan.runtime.core.Param_Types.Module_Parameter.expression_operand_t;
import org.eclipse.titan.runtime.core.Param_Types.Module_Parameter.type_t;
import org.eclipse.titan.runtime.core.TTCN_EncDec.error_behavior_type;
import org.eclipse.titan.runtime.core.TTCN_EncDec.error_type;

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

	private IDecode_Match dec_match;

	/**
	 * Initializes to unbound/uninitialized template.
	 * */
	public TitanBitString_template() {
		// do nothing
	}

	/**
	 * Initializes to a given template kind.
	 *
	 * @param otherValue
	 *                the template kind to initialize to.
	 * */
	public TitanBitString_template(final template_sel otherValue) {
		super(otherValue);
		check_single_selection(otherValue);
	}

	public TitanBitString_template(final int otherValue[], final int aNoBits) {
		super(template_sel.SPECIFIC_VALUE);
		single_value = new TitanBitString(otherValue, aNoBits);
	}

	/**
	 * Initializes to a given value.
	 * The template becomes a specific template and the value is copied.
	 *
	 * @param otherValue
	 *                the value to initialize to.
	 * */
	public TitanBitString_template(final TitanBitString otherValue) {
		super(template_sel.SPECIFIC_VALUE);

		otherValue.must_bound("Creating a template from an unbound bitstring value.");

		single_value = new TitanBitString(otherValue);
	}

	/**
	 * Initializes to a given value.
	 * The template becomes a specific template and the value is copied.
	 *
	 * @param otherValue
	 *                the value to initialize to.
	 * */
	public TitanBitString_template(final TitanBitString_Element otherValue) {
		super(template_sel.SPECIFIC_VALUE);
		single_value = new TitanBitString((byte) (otherValue.get_bit() ? 1 : 0));
	}

	/**
	 * Initializes to a given value.
	 * The template becomes a specific template and the value is copied.
	 * Causes dynamic testcase error if the parameter is not present or omit.
	 *
	 * @param otherValue
	 *                the value to initialize to.
	 * */
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

	/**
	 * Initializes to a given template.
	 *
	 * @param otherValue
	 *                the template to initialize to.
	 * */
	public TitanBitString_template(final TitanBitString_template otherValue) {
		copy_template(otherValue);
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

	@Override
	public void clean_up() {
		switch (template_selection) {
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
			dec_match = null;
			break;
		default:
			break;
		}
		template_selection = template_sel.UNINITIALIZED_TEMPLATE;
	}

	@Override
	public TitanBitString_template operator_assign(final Base_Type otherValue) {
		if (otherValue instanceof TitanBitString) {
			return operator_assign((TitanBitString) otherValue);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to bitstring", otherValue));
	}

	@Override
	public TitanBitString_template operator_assign(final Base_Template otherValue) {
		if (otherValue instanceof TitanBitString_template) {
			return operator_assign((TitanBitString_template) otherValue);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to bitstring", otherValue));
	}

	@Override
	public TitanBitString_template operator_assign(final template_sel otherValue) {
		check_single_selection(otherValue);
		clean_up();
		set_selection(otherValue);

		return this;
	}

	// originally operator=
	public TitanBitString_template operator_assign(final int otherValue[], final int aNoBits) {
		clean_up();
		set_selection(template_sel.SPECIFIC_VALUE);
		single_value = new TitanBitString(otherValue, aNoBits);

		return this;
	}

	/**
	 * Assigns the other value to this template.
	 * Overwriting the current content in the process.
	 *<p>
	 * operator= in the core.
	 *
	 * @param otherValue
	 *                the other value to assign.
	 * @return the new template object.
	 */
	public TitanBitString_template operator_assign(final TitanBitString otherValue) {
		otherValue.must_bound("Assignment of an unbound bitstring value to a template.");

		clean_up();
		set_selection(template_sel.SPECIFIC_VALUE);
		single_value = new TitanBitString(otherValue);

		return this;
	}

	/**
	 * Assigns the other value to this template.
	 * Overwriting the current content in the process.
	 *<p>
	 * operator= in the core.
	 *
	 * @param otherValue
	 *                the other value to assign.
	 * @return the new template object.
	 */
	public TitanBitString_template operator_assign(final TitanBitString_Element otherValue) {
		otherValue.must_bound("Assignment of an unbound bitstring element to a template.");

		clean_up();
		set_selection(template_sel.SPECIFIC_VALUE);
		single_value = new TitanBitString((byte) (otherValue.get_bit() ? 1 : 0));

		return this;
	}

	/**
	 * Assigns the other template to this template.
	 * Overwriting the current content in the process.
	 *<p>
	 * operator= in the core.
	 *
	 * @param otherValue
	 *                the other value to assign.
	 * @return the new template object.
	 */
	public TitanBitString_template operator_assign(final TitanBitString_template otherValue) {
		if (otherValue != this) {
			clean_up();
			copy_template(otherValue);
		}

		return this;
	}

	/**
	 * Assigns the other value to this template.
	 * Overwriting the current content in the process.
	 *<p>
	 * operator= in the core.
	 *
	 * @param otherValue
	 *                the other value to assign.
	 * @return the new template object.
	 */
	public TitanBitString_template operator_assign(final Optional<TitanBitString> otherValue) {
		clean_up();
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

	private void copy_template(final TitanBitString_template otherValue) {
		switch (otherValue.template_selection) {
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
			pattern_value = otherValue.pattern_value;
			pattern_value_ref_count++;
			break;
		case DECODE_MATCH:
			dec_match = otherValue.dec_match;
			break;
		default:
			throw new TtcnError("Copying an uninitialized/unsupported bitstring template.");
		}

		set_selection(otherValue);
	}

	// originally operator[](int)
	public TitanBitString_Element get_at(final int index_value) {
		if (template_selection != template_sel.SPECIFIC_VALUE || is_ifPresent) {
			throw new TtcnError("Accessing a bitstring element of a non-specific bitstring template.");
		}

		return single_value.get_at(index_value);
	}

	// originally operator[](const INTEGER&)
	public TitanBitString_Element get_at(final TitanInteger index_value) {
		index_value.must_bound("Indexing a bitstring template with an unbound integer value.");

		return get_at(index_value.getInt());
	}

	// originally operator[](int) const
	public TitanBitString_Element constGet_at(final int index_value) {
		if (template_selection != template_sel.SPECIFIC_VALUE || is_ifPresent) {
			throw new TtcnError("Accessing a bitstring element of a non-specific bitstring template.");
		}

		return single_value.constGet_at(index_value);
	}

	// originally operator[](const INTEGER&) const
	public TitanBitString_Element constGet_at(final TitanInteger index_value) {
		index_value.must_bound("Indexing a bitstring template with an unbound integer value.");

		return constGet_at(index_value.getInt());
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

	/**
	 * Matches the provided value against this template.
	 *
	 * @param otherValue the value to be matched.
	 * */
	public boolean match(final TitanBitString otherValue) {
		return match(otherValue, false);
	}

	/**
	 * Matches the provided value against this template. In legacy mode
	 * omitted value fields are not matched against the template field.
	 *
	 * @param otherValue
	 *                the value to be matched.
	 * @param legacy
	 *                use legacy mode.
	 * */
	public boolean match(final TitanBitString otherValue, final boolean legacy) {
		if (!otherValue.is_bound()) {
			return false;
		}

		final TitanInteger value_length = otherValue.lengthof();
		if (!match_length(value_length.getInt())) {
			return false;
		}

		switch (template_selection) {
		case SPECIFIC_VALUE:
			return single_value.operator_equals(otherValue);
		case OMIT_VALUE:
			return false;
		case ANY_VALUE:
		case ANY_OR_OMIT:
			return true;
		case VALUE_LIST:
		case COMPLEMENTED_LIST:
			for (int i = 0; i < value_list.size(); i++) {
				if (value_list.get(i).match(otherValue, legacy)) {
					return template_selection == template_sel.VALUE_LIST;
				}
			}
			return template_selection == template_sel.COMPLEMENTED_LIST;
		case STRING_PATTERN:
			return match_pattern(pattern_value, otherValue);
		case DECODE_MATCH: {
			TTCN_EncDec.set_error_behavior(error_type.ET_ALL, error_behavior_type.EB_WARNING);
			TTCN_EncDec.clear_error();
			final TitanOctetString os = new TitanOctetString(AdditionalFunctions.bit2oct(otherValue));
			final TTCN_Buffer buffer = new TTCN_Buffer(os);
			final boolean ret_val = dec_match.match(buffer);
			TTCN_EncDec.set_error_behavior(error_type.ET_ALL, error_behavior_type.EB_DEFAULT);
			TTCN_EncDec.clear_error();
			return ret_val;
		}
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

	@Override
	public TitanBitString valueof() {
		if (template_selection != template_sel.SPECIFIC_VALUE || is_ifPresent) {
			throw new TtcnError("Performing a valueof or send operation on a non-specific bitstring template.");
		}

		return single_value;
	}

	// originally lengthof
	public TitanInteger lengthof() {
		if (is_ifPresent) {
			throw new TtcnError("Performing lengthof() operation on a bitstring template which has an ifpresent attribute.");
		}
		int min_length = 0;
		boolean has_any_or_none = false;
		switch (template_selection) {
		case SPECIFIC_VALUE:
			min_length = single_value.lengthof().getInt();
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
			final int item_length = value_list.get(0).lengthof().getInt();
			for (int i = 1; i < value_list.size(); i++) {
				if (value_list.get(i).lengthof().getInt() != item_length) {
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


	@Override
	public void set_type(final template_sel templateType, final int listLength /* = 0 */) {
		if(templateType != template_sel.VALUE_LIST && templateType != template_sel.COMPLEMENTED_LIST &&
				templateType != template_sel.DECODE_MATCH) {
			throw new TtcnError("Setting an invalid list type for a bitstring template.");
		}
		clean_up();
		set_selection(templateType);
		if (templateType != template_sel.DECODE_MATCH) {
			value_list = new ArrayList<TitanBitString_template>(listLength);
			for (int i = 0; i < listLength; i++) {
				value_list.add(new TitanBitString_template());
			}
		}
	}

	@Override
	public TitanBitString_template list_item(final int listIndex) {
		if (template_selection != template_sel.VALUE_LIST && template_selection != template_sel.COMPLEMENTED_LIST) {
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

	public void set_decmatch(final IDecode_Match dec_match) {
		if (template_selection != template_sel.DECODE_MATCH) {
			throw new TtcnError("Setting the decoded content matching mechanism of a non-decmatch bitstring template.");
		}

		this.dec_match = dec_match;
	}

	public Object get_decmatch_dec_res() {
		if (template_selection != template_sel.DECODE_MATCH) {
			throw new TtcnError("Retrieving the decoding result of a non-decmatch bitstring template.");
		}

		return dec_match.get_dec_res();
	}

	public TTCN_Typedescriptor get_decmatch_type_descr() {
		if (template_selection != template_sel.DECODE_MATCH) {
			throw new TtcnError("Retrieving the decoded type's descriptor in a non-decmatch bitstring template.");
		}

		return dec_match.get_type_descr();
	}

	@Override
	public void log() {
		switch (template_selection) {
		case SPECIFIC_VALUE:
			single_value.log();
			break;
		case COMPLEMENTED_LIST:
			TTCN_Logger.log_event_str("complement");
		case VALUE_LIST:
			TTCN_Logger.log_char('(');
			for (int i = 0; i < value_list.size(); i++) {
				if (i > 0) {
					TTCN_Logger.log_event_str(", ");
				}
				value_list.get(i).log();
			}
			TTCN_Logger.log_char(')');
			break;
		case STRING_PATTERN:
			TTCN_Logger.log_char('\'');
			for (int i = 0; i < pattern_value.length; i++) {
				final int pattern = pattern_value[i];
				if (pattern < 4) {
					TTCN_Logger.log_char(patterns[pattern]);
				} else {
					TTCN_Logger.log_event_str("<unknown>");
				}
			}
			TTCN_Logger.log_event_str("'B");
			break;
		case DECODE_MATCH:
			TTCN_Logger.log_event_str("decmatch ");
			dec_match.log();
			break;
		default:
			log_generic();
			break;
		}
		log_restricted();
		log_ifpresent();
	}

	/**
	 * Logs the matching of the provided value to this template, to help
	 * identify the reason for mismatch. In legacy mode omitted value fields
	 * are not matched against the template field.
	 *
	 * @param match_value
	 *                the value to be matched.
	 * @param legacy
	 *                use legacy mode.
	 * */
	public void log_match(final TitanBitString match_value, final boolean legacy) {
		if (TTCN_Logger.matching_verbosity_t.VERBOSITY_COMPACT == TTCN_Logger.get_matching_verbosity()
				&& TTCN_Logger.get_logmatch_buffer_len() != 0) {
			TTCN_Logger.print_logmatch_buffer();
			TTCN_Logger.log_event_str(" := ");
		}
		match_value.log();
		TTCN_Logger.log_event_str(" with ");
		log();
		if (match(match_value)) {
			TTCN_Logger.log_event_str(" matched");
		} else {
			TTCN_Logger.log_event_str(" unmatched");
		}
	}

	@Override
	public void set_param(final Module_Parameter param) {
		param.basic_check(Module_Parameter.basic_check_bits_t.BC_TEMPLATE.getValue() | Module_Parameter.basic_check_bits_t.BC_LIST.getValue(), "bitstring template");
		switch (param.get_type()) {
		case MP_Omit:
			this.operator_assign(template_sel.OMIT_VALUE);
			break;
		case MP_Any:
			this.operator_assign(template_sel.ANY_VALUE);
			break;
		case MP_AnyOrNone:
			this.operator_assign(template_sel.ANY_OR_OMIT);
			break;
		case MP_List_Template:
		case MP_ComplementList_Template: {
			final TitanBitString_template temp = new TitanBitString_template();
			temp.set_type(param.get_type() == type_t.MP_List_Template ? template_sel.VALUE_LIST : template_sel.COMPLEMENTED_LIST, param.get_size());
			for (int i = 0; i < param.get_size(); i++) {
				temp.list_item(i).set_param(param.get_elem(i));
			}
			this.operator_assign(temp);
			break;
		}
		case MP_Bitstring:
			this.operator_assign(new TitanBitString((int[])param.get_string_data(), param.get_string_size()));
			break;
		case MP_Bitstring_Template:
			this.operator_assign(new TitanBitString_template((String)param.get_string_data()));
			break;
		case MP_Expression:
			if (param.get_expr_type() == expression_operand_t.EXPR_CONCATENATE) {
				final TitanBitString operand1 = new TitanBitString();
				final TitanBitString operand2 = new TitanBitString();
				operand1.set_param(param.get_operand1());
				operand2.set_param(param.get_operand2());
				this.operator_assign(operand1.operator_concatenate(operand2));
			} else {
				param.expr_type_error("a bitstring");
			}
			break;
		default:
			param.type_error("bitstring template");
		}
		is_ifPresent = param.get_ifpresent();
		if (param.get_length_restriction() != null) {
			set_length_range(param);
		}
	}

	public boolean match_omit(final boolean legacy) {
		if (is_ifPresent) {
			return true;
		}

		switch (template_selection) {
		case OMIT_VALUE:
		case ANY_OR_OMIT:
			return true;
		case VALUE_LIST:
		case COMPLEMENTED_LIST:
			if (legacy) {
				for (int i = 0; i < value_list.size(); i++) {
					if (value_list.get(i).match_omit()) {
						return template_selection == template_sel.VALUE_LIST;
					}
				}
				return template_selection == template_sel.COMPLEMENTED_LIST;
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

		switch (template_selection) {
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
		clean_up();
		decode_text_restricted(text_buf);

		switch (template_selection) {
		case OMIT_VALUE:
		case ANY_VALUE:
		case ANY_OR_OMIT:
			break;
		case SPECIFIC_VALUE:
			single_value = new TitanBitString();
			single_value.decode_text(text_buf);
			break;
		case VALUE_LIST:
		case COMPLEMENTED_LIST: {
			final int size = text_buf.pull_int().getInt();
			value_list = new ArrayList<TitanBitString_template>(size);
			for (int i = 0; i < size; i++) {
				final TitanBitString_template temp = new TitanBitString_template();
				temp.decode_text(text_buf);
				value_list.add(temp);
			}
			break;
		}
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

	@Override
	public void check_restriction(final template_res restriction, final String name, final boolean legacy) {
		if (template_selection == template_sel.UNINITIALIZED_TEMPLATE) {
			return;
		}

		switch ((name != null && restriction == template_res.TR_VALUE) ? template_res.TR_OMIT : restriction) {
		case TR_VALUE:
			if (!is_ifPresent && template_selection == template_sel.SPECIFIC_VALUE) {
				return;
			}
			break;
		case TR_OMIT:
			if (!is_ifPresent && (template_selection == template_sel.OMIT_VALUE || template_selection == template_sel.SPECIFIC_VALUE)) {
				return;
			}
			break;
		case TR_PRESENT:
			if (!match_omit(legacy)) {
				return;
			}
			break;
		default:
			return;
		}

		throw new TtcnError(MessageFormat.format("Restriction `{0}'' on template of type {1} violated.", get_res_name(restriction), name == null ? "bitstring" : name));
	}
}
