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

import org.eclipse.titan.runtime.core.Base_Type.TTCN_Typedescriptor;
import org.eclipse.titan.runtime.core.Param_Types.Module_Parameter;
import org.eclipse.titan.runtime.core.Param_Types.Module_Parameter.basic_check_bits_t;
import org.eclipse.titan.runtime.core.Param_Types.Module_Parameter.expression_operand_t;
import org.eclipse.titan.runtime.core.Param_Types.Module_Parameter.type_t;
import org.eclipse.titan.runtime.core.TTCN_EncDec.error_behavior_type;
import org.eclipse.titan.runtime.core.TTCN_EncDec.error_type;

/**
 * TTCN-3 hexstring template
 *
 * @author Arpad Lovassy
 * @author Gergo Ujhelyi
 * @author Andrea Palfi
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
	private byte pattern_value[];

	/** reference counter for pattern_value */
	private int pattern_value_ref_count;

	private IDecode_Match dec_match;

	/**
	 * Initializes to unbound/uninitialized template.
	 * */
	public TitanHexString_template() {
		// do nothing
	}

	/**
	 * Initializes to a given template kind.
	 *
	 * @param otherValue
	 *                the template kind to initialize to.
	 * */
	public TitanHexString_template(final template_sel otherValue) {
		super(otherValue);
		check_single_selection(otherValue);
	}

	/**
	 * Initializes to a given value.
	 * The template becomes a specific template and the value is copied.
	 *
	 * @param otherValue
	 *                the value to initialize to.
	 * */
	public TitanHexString_template(final TitanHexString otherValue) {
		super(template_sel.SPECIFIC_VALUE);
		otherValue.must_bound("Creating a template from an unbound hexstring value.");

		single_value = new TitanHexString(otherValue);
	}

	/**
	 * Initializes to a given value.
	 * The template becomes a specific template and the value is copied.
	 * Causes dynamic testcase error if the parameter is not present or omit.
	 *
	 * @param otherValue
	 *                the value to initialize to.
	 * */
	public TitanHexString_template(final Optional<TitanHexString> otherValue) {
		switch (otherValue.get_selection()) {
		case OPTIONAL_PRESENT:
			set_selection(template_sel.SPECIFIC_VALUE);
			single_value = new TitanHexString(otherValue.constGet());
			break;
		case OPTIONAL_OMIT:
			set_selection(template_sel.OMIT_VALUE);
			break;
		case OPTIONAL_UNBOUND:
			throw new TtcnError("Creating a hexstring template from an unbound optional field.");
		}
	}

	/**
	 * Initializes to a given template.
	 *
	 * @param otherValue
	 *                the template to initialize to.
	 * */
	public TitanHexString_template(final TitanHexString_template otherValue) {
		copy_template(otherValue);
	}

	/**
	 * Initializes to a given value.
	 * The template becomes a specific template and the value is copied.
	 *
	 * @param otherValue
	 *                the value to initialize to.
	 * */
	public TitanHexString_template(final TitanHexString_Element otherValue) {
		super(template_sel.SPECIFIC_VALUE);
		single_value = new TitanHexString(otherValue);
	}

	public TitanHexString_template(final byte pattern_elements[]) {
		super(template_sel.STRING_PATTERN);
		pattern_value = TitanString_Utils.copy_byte_list(pattern_elements);
	}

	public TitanHexString_template(final String patternString) {
		super(template_sel.STRING_PATTERN);
		pattern_value = pattern_string_2_list(patternString);
	}

	private static byte[] pattern_string_2_list(final String patternString) {
		if (patternString == null) {
			throw new TtcnError("Internal error: hexstring pattern is null.");
		}

		final byte result[] = new byte[patternString.length()];
		for (int i = 0; i < patternString.length(); i++) {
			final char patternChar = patternString.charAt(i);
			result[i] = pattern_char_2_byte(patternChar);
		}
		return result;
	}

	/**
	 * converts hexstring template digit to pattern value.
	 *
	 * Each element occupies one byte. Meaning of values:
	 * 0 .. 15 -> 0 .. F, 16 -> ?, 17 -> *
	 */
	private static byte pattern_char_2_byte(final char patternChar) {
		if ('0' <= patternChar && '9' >= patternChar) {
			return (byte) (patternChar - '0');
		}

		if ('A' <= patternChar && 'F' >= patternChar) {
			return (byte) (patternChar - 'A' + 10);
		}

		if ('a' <= patternChar && 'f' >= patternChar) {
			return (byte) (patternChar - 'a' + 10);
		}

		if ('?' == patternChar) {
			return 16;
		}

		if ('*' == patternChar) {
			return 17;
		}

		throw new TtcnError("Internal error: invalid element in hexstring pattern.");
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
			break;
		case STRING_PATTERN:
			if (pattern_value_ref_count > 1) {
				pattern_value_ref_count--;
			} else if (pattern_value_ref_count == 1) {
				pattern_value = null;
			} else {
				throw new TtcnError("Internal error: Invalid reference counter in a hexstring pattern.");
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
	public TitanHexString_template operator_assign(final Base_Type otherValue) {
		if (otherValue instanceof TitanHexString) {
			return operator_assign((TitanHexString) otherValue);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to hexstring", otherValue));
	}

	@Override
	public TitanHexString_template operator_assign(final Base_Template otherValue) {
		if (otherValue instanceof TitanHexString_template) {
			return operator_assign((TitanHexString_template) otherValue);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to hexstring", otherValue));
	}

	@Override
	public TitanHexString_template operator_assign(final template_sel otherValue) {
		check_single_selection(otherValue);
		clean_up();
		set_selection(otherValue);

		return this;
	}

	// originally operator=
	public TitanHexString_template operator_assign(final byte otherValue[]) {
		clean_up();
		set_selection(template_sel.SPECIFIC_VALUE);
		single_value = new TitanHexString(otherValue);

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
	public TitanHexString_template operator_assign(final TitanHexString otherValue) {
		otherValue.must_bound("Assignment of an unbound hexstring value to a template.");

		clean_up();
		set_selection(template_sel.SPECIFIC_VALUE);
		single_value = new TitanHexString(otherValue);

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
	public TitanHexString_template operator_assign(final TitanHexString_Element otherValue) {
		otherValue.must_bound("Assignment of an unbound hexstring element to a template.");

		clean_up();
		set_selection(template_sel.SPECIFIC_VALUE);
		single_value = new TitanHexString(otherValue);

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
	public TitanHexString_template operator_assign(final TitanHexString_template otherValue) {
		if (otherValue != this) {
			clean_up();
			copy_template(otherValue);
		}

		return this;
	}

	private void copy_template(final TitanHexString_template otherValue) {
		switch (otherValue.template_selection) {
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
			for (int i = 0; i < otherValue.value_list.size(); i++) {
				final TitanHexString_template temp = new TitanHexString_template(otherValue.value_list.get(i));
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
			throw new TtcnError("Copying an uninitialized/unsupported hexstring template.");
		}

		set_selection(otherValue);
	}

	// originally operator[](int)
	public TitanHexString_Element get_at(final int index_value) {
		if (template_selection != template_sel.SPECIFIC_VALUE || is_ifPresent) {
			throw new TtcnError("Accessing a hexstring element of a non-specific hexstring template.");
		}

		return single_value.get_at(index_value);
	}

	// originally operator[](const INTEGER&)
	public TitanHexString_Element get_at(final TitanInteger index_value) {
		index_value.must_bound("Indexing a hexstring template with an unbound integer value.");

		return get_at(index_value.get_int());
	}

	// originally operator[](int) const
	public TitanHexString_Element constGet_at(final int index_value) {
		if (template_selection != template_sel.SPECIFIC_VALUE || is_ifPresent) {
			throw new TtcnError("Accessing a hexstring element of a non-specific hexstring template.");
		}

		return single_value.constGet_at(index_value);
	}

	// originally operator[](const INTEGER&) const
	public TitanHexString_Element constGet_at(final TitanInteger index_value) {
		index_value.must_bound("Indexing a hexstring template with an unbound integer value.");

		return constGet_at(index_value.get_int());
	}

	@Override
	public boolean match(final Base_Type otherValue, final boolean legacy) {
		if (otherValue instanceof TitanHexString) {
			return match((TitanHexString) otherValue, legacy);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to hexstring", otherValue));
	}

	@Override
	public void log_match(final Base_Type match_value, final boolean legacy) {
		if (match_value instanceof TitanHexString) {
			log_match((TitanHexString) match_value, legacy);
			return;
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to hexstring", match_value));
	}

	/**
	 * Matches the provided value against this template.
	 *
	 * @param otherValue the value to be matched.
	 * */
	public boolean match(final TitanHexString otherValue) {
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
	public boolean match(final TitanHexString otherValue, final boolean legacy) {
		if (!otherValue.is_bound()) {
			return false;
		}

		final TitanInteger value_length = otherValue.lengthof();
		if (!match_length(value_length.get_int())) {
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
			final TitanOctetString os = new TitanOctetString(AdditionalFunctions.hex2oct(otherValue));
			final TTCN_Buffer buffer = new TTCN_Buffer(os);
			final boolean ret_val = dec_match.match(buffer);
			TTCN_EncDec.set_error_behavior(error_type.ET_ALL, error_behavior_type.EB_DEFAULT);
			TTCN_EncDec.clear_error();
			return ret_val;
		}
		default:
			throw new TtcnError("Matching with an uninitialized/unsupported hexstring template.");
		}
	}

	/**
	 * This is the same algorithm that match_array uses
	 * to match 'record of' types.
	 * The only differences are: how two elements are matched and
	 * how an asterisk or ? is identified in the template
	 */
	private boolean match_pattern(final byte string_pattern[], final TitanHexString string_value) {
		final int stringPatternSize = string_pattern.length;
		final int stringValueNNibbles = string_value.get_value().length;
		// the empty pattern matches the empty hexstring only
		if (stringPatternSize == 0) {
			return stringValueNNibbles == 0;
		}

		int value_index = 0;
		int template_index = 0;
		int last_asterisk = -1;
		int last_value_to_asterisk = -1;
		//the following variables are just to speed up the function
		byte pattern_element;
		byte hex_digit;

		for (;;) {
			pattern_element = string_pattern[ template_index ];
			if (pattern_element < 16) {
				/*
				In titan core hexdigit is stored in 2 bytes:

				octet = string_value.get_nibble( value_index / 2 );
				if (value_index % 2) {
					hex_digit = octet >> 4;
				} else {
					hex_digit = octet & 0x0F;
				}
				*/
				hex_digit = string_value.get_nibble(value_index);
				if (hex_digit == pattern_element) {
					value_index++;
					template_index++;
				} else {
					if (last_asterisk == -1) {
						return false;
					}
					template_index = last_asterisk + 1;
					value_index = ++last_value_to_asterisk;
				}
			} else if (pattern_element == 16) {
				// ?
				value_index++;
				template_index++;
			} else if (pattern_element == 17) {
				//*
				last_asterisk = template_index++;
				last_value_to_asterisk = value_index;
			} else {
				throw new TtcnError("Internal error: invalid element in a hexstring pattern.");
			}

			if (value_index == stringValueNNibbles && template_index == stringPatternSize) {
				return true;
			} else if (template_index == stringPatternSize) {
				if (string_pattern[template_index - 1] == 17) {
					return true;
				} else if (last_asterisk == -1) {
					return false;
				} else {
					template_index = last_asterisk + 1;
					value_index = ++last_value_to_asterisk;
				}
			} else if (value_index == stringValueNNibbles) {
				while (template_index < stringPatternSize && string_pattern[template_index] == 17) {
					template_index++;
				}

				return template_index == stringPatternSize;
			}
		}
	}

	@Override
	public TitanHexString valueof() {
		if (template_selection != template_sel.SPECIFIC_VALUE || is_ifPresent) {
			throw new TtcnError("Performing a valueof or send operation on a non-specific hexstring template.");
		}

		return single_value;
	}

	// originally lengthof
	public TitanInteger lengthof() {
		if (is_ifPresent) {
			throw new TtcnError("Performing lengthof() operation on a hexstring template which has an ifpresent attribute.");
		}

		int min_length = 0;
		boolean has_any_or_none = false;
		switch (template_selection) {
		case SPECIFIC_VALUE:
			min_length = single_value.lengthof().get_int();
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
			final int item_length = value_list.get(0).lengthof().get_int();
			for (int i = 1; i < value_list.size(); i++) {
				if (value_list.get(i).lengthof().get_int() != item_length) {
					throw new TtcnError("Performing lengthof() operation on a hexstring template containing a value list with different lengths.");
				}
			}
			min_length = item_length;
			break;
		case COMPLEMENTED_LIST:
			throw new TtcnError("Performing lengthof() operation on a hexstring template containing complemented list.");
		case STRING_PATTERN:
			has_any_or_none = false; // TRUE if * chars in the pattern
			for (int i = 0; i < pattern_value.length; i++) {
				if (pattern_value[i] < 17) {
					min_length++; // case of 0-F, ?
				} else {
					has_any_or_none = true; // case of * character
				}
			}
			break;
		default:
			throw new TtcnError("Performing lengthof() operation on an uninitialized/unsupported hexstring template.");
		}
		return new TitanInteger(check_section_is_single(min_length, has_any_or_none, "length", "a", "hexstring template"));
	}

	@Override
	public void set_type(final template_sel templateType, final int listLength) {
		if (templateType != template_sel.VALUE_LIST && templateType != template_sel.COMPLEMENTED_LIST
				&& templateType != template_sel.DECODE_MATCH) {
			throw new TtcnError("Setting an invalid list type for a hexstring template.");
		}

		clean_up();
		set_selection(templateType);
		if (templateType != template_sel.DECODE_MATCH) {
			value_list = new ArrayList<TitanHexString_template>(listLength);
			for (int i = 0; i < listLength; i++) {
				value_list.add(new TitanHexString_template());
			}
		}
	}

	@Override
	public TitanHexString_template list_item(final int listIndex) {
		if (template_selection != template_sel.VALUE_LIST && template_selection != template_sel.COMPLEMENTED_LIST) {
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

	public void set_decmatch(final IDecode_Match dec_match) {
		if (template_selection != template_sel.DECODE_MATCH) {
			throw new TtcnError("Setting the decoded content matching mechanism of a non-decmatch hexstring template.");
		}

		this.dec_match = dec_match;
	}

	public Object get_decmatch_dec_res() {
		if (template_selection != template_sel.DECODE_MATCH) {
			throw new TtcnError("Retrieving the decoding result of a non-decmatch hexstring template.");
		}

		return dec_match.get_dec_res();
	}

	public TTCN_Typedescriptor get_decmatch_type_descr() {
		if (template_selection != template_sel.DECODE_MATCH) {
			throw new TtcnError("Retrieving the decoded type's descriptor in a non-decmatch hexstring template.");
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
				final byte pattern = pattern_value[i];
				if (pattern < 16) {
					TTCN_Logger.log_hex(pattern);
				} else if (pattern == 16) {
					TTCN_Logger.log_char('?');
				} else if (pattern == 17) {
					TTCN_Logger.log_char('*');
				} else {
					TTCN_Logger.log_event_str("<unknown>");
				}
			}
			TTCN_Logger.log_event_str("'H");
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
	public void log_match(final TitanHexString match_value, final boolean legacy) {
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
		param.basic_check(basic_check_bits_t.BC_TEMPLATE.getValue()|basic_check_bits_t.BC_LIST.getValue() , "hexstring template");
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
			final TitanHexString_template temp = new TitanHexString_template();
			temp.set_type(param.get_type() == type_t.MP_List_Template ? template_sel.VALUE_LIST : template_sel.COMPLEMENTED_LIST , param.get_size());
			for (int i = 0; i < param.get_size(); i++) {
				temp.list_item(i).set_param(param.get_elem(i));
			}
			this.operator_assign(temp);
			break;
		}
		case MP_Hexstring:
			this.operator_assign(new TitanHexString((byte[]) param.get_string_data()));
			break;
		case MP_Hexstring_Template:
			this.operator_assign(new TitanHexString_template((TitanHexString_template)param.get_string_data()));
			break;
		case MP_Expression:
			if (param.get_expr_type() == expression_operand_t.EXPR_CONCATENATE) {
				final TitanHexString operand1 = new TitanHexString();
				final TitanHexString operand2 = new TitanHexString();
				operand1.set_param(param.get_operand1());
				operand2.set_param(param.get_operand2());
				this.operator_assign(operand1.operator_concatenate(operand2));
			} else {
				param.expr_type_error("a hexstring");
			}
			break;
		default:
			param.type_error("hexstring template");
			break;
		}
		is_ifPresent = param.get_ifpresent();
		if (param.get_length_restriction() != null) {
			set_length_range(param);
		}
	}

	@Override
	/** {@inheritDoc} */
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
				// legacy behavior: 'omit' can appear in the value/complement list
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
			text_buf.push_raw((pattern_value.length + 7) / 8, pattern_value);
			break;
		default:
			throw new TtcnError("Text encoder: Encoding an uninitialized/unsupported hexstring template.");
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
			single_value = new TitanHexString();
			single_value.decode_text(text_buf);
			break;
		case VALUE_LIST:
		case COMPLEMENTED_LIST: {
			final int size = text_buf.pull_int().get_int();
			value_list = new ArrayList<TitanHexString_template>(size);
			for (int i = 0; i < size; i++) {
				final TitanHexString_template temp = new TitanHexString_template();
				temp.decode_text(text_buf);
				value_list.add(temp);
			}
			break;
		}
		case STRING_PATTERN: {
			final int n_elements = text_buf.pull_int().get_int();
			pattern_value = new byte[n_elements];
			text_buf.pull_raw(n_elements, pattern_value);
			break;
		}
		default:
			throw new TtcnError("Text decoder: An unknown/unsupported selection was received for a hexstring template.");
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

		throw new TtcnError(MessageFormat.format("Restriction `{0}'' on template of type {1} violated.", get_res_name(restriction), name == null ? "hexstring" : name));
	}
}
