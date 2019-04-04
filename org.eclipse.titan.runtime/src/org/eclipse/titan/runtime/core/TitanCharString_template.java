/******************************************************************************
 * Copyright (c) 2000-2019 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

import org.eclipse.titan.runtime.core.Base_Type.TTCN_Typedescriptor;
import org.eclipse.titan.runtime.core.Param_Types.Module_Parameter;
import org.eclipse.titan.runtime.core.Param_Types.Module_Parameter.basic_check_bits_t;
import org.eclipse.titan.runtime.core.Param_Types.Module_Parameter.expression_operand_t;
import org.eclipse.titan.runtime.core.Param_Types.Module_Parameter.type_t;
import org.eclipse.titan.runtime.core.TTCN_EncDec.error_behavior_type;
import org.eclipse.titan.runtime.core.TTCN_EncDec.error_type;
import org.eclipse.titan.runtime.core.TitanCharString.CharCoding;
import org.eclipse.titan.runtime.core.TitanUniversalCharString_template.Unichar_Decmatch;

/**
 * TTCN-3 charstring template
 *
 * @author Arpad Lovassy
 * @author Andrea Palfi
 */
public class TitanCharString_template extends Restricted_Length_Template {

	TitanCharString single_value;

	// value_list part
	ArrayList<TitanCharString_template> value_list;

	// value range part
	boolean min_is_set, max_is_set;
	boolean min_is_exclusive, max_is_exclusive;
	TitanCharString min_value, max_value;

	/** originally pattern_value/regexp_init */
	private boolean pattern_value_regexp_init;

	/**
	 * java/perl style pattern converted from TTCN-3 charstring pattern
	 * originally pattern_value/posix_regexp
	 */
	private Pattern pattern_value_posix_regexp;

	/** originally pattern_value/nocase */
	boolean pattern_value_nocase;

	protected Unichar_Decmatch dec_match;

	/**
	 * Initializes to unbound/uninitialized template.
	 * */
	public TitanCharString_template() {
		//do nothing
	}

	/**
	 * Initializes to a given template kind.
	 *
	 * @param otherValue
	 *                the template kind to initialize to.
	 * */
	public TitanCharString_template(final template_sel otherValue) {
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
	public TitanCharString_template(final String otherValue) {
		super(template_sel.SPECIFIC_VALUE);
		single_value = new TitanCharString(otherValue);
	}

	/**
	 * Initializes to a given value.
	 * The template becomes a specific template and the value is copied.
	 *
	 * @param otherValue
	 *                the value to initialize to.
	 * */
	public TitanCharString_template(final TitanCharString otherValue) {
		super(template_sel.SPECIFIC_VALUE);
		otherValue.must_bound("Creating a template from an unbound charstring value.");

		single_value = new TitanCharString(otherValue);
	}

	/**
	 * Initializes to a given value.
	 * The template becomes a specific template and the value is copied.
	 * Causes dynamic testcase error if the parameter is not present or omit.
	 *
	 * @param otherValue
	 *                the value to initialize to.
	 * */
	public TitanCharString_template(final Optional<TitanCharString> otherValue) {
		switch (otherValue.get_selection()) {
		case OPTIONAL_PRESENT:
			set_selection(template_sel.SPECIFIC_VALUE);
			single_value = new TitanCharString(otherValue.constGet());
			break;
		case OPTIONAL_OMIT:
			set_selection(template_sel.OMIT_VALUE);
			break;
		case OPTIONAL_UNBOUND:
			throw new TtcnError("Creating a charstring template from an unbound optional field.");
		}
	}

	/**
	 * Initializes to a given template.
	 *
	 * @param otherValue
	 *                the template to initialize to.
	 * */
	public TitanCharString_template(final TitanCharString_template otherValue) {
		copy_template(otherValue);
	}

	public TitanCharString_template(final template_sel p_sel, final TitanCharString p_str) {
		this(p_sel, p_str, false);
	}

	public TitanCharString_template(final template_sel p_sel, final TitanCharString p_str, final boolean p_nocase) {
		super(template_sel.STRING_PATTERN);
		single_value = new TitanCharString(p_str);
		if (p_sel != template_sel.STRING_PATTERN) {
			throw new TtcnError("Internal error: Initializing a charstring pattern template with invalid selection.");
		}
		pattern_value_regexp_init = false;
		pattern_value_nocase = p_nocase;
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
		case VALUE_RANGE:
			min_value = null;
			max_value = null;
			break;
		case STRING_PATTERN:
			pattern_value_regexp_init = false;
			pattern_value_posix_regexp = null;
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
	public TitanCharString_template operator_assign(final Base_Type otherValue) {
		if (otherValue instanceof TitanCharString) {
			return operator_assign((TitanCharString) otherValue);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to charstring", otherValue));
	}

	@Override
	public TitanCharString_template operator_assign(final Base_Template otherValue) {
		if (otherValue instanceof TitanCharString_template) {
			return operator_assign((TitanCharString_template) otherValue);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to charstring template", otherValue));
	}

	@Override
	public TitanCharString_template operator_assign(final template_sel otherValue) {
		check_single_selection(otherValue);
		clean_up();
		set_selection(otherValue);

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
	public TitanCharString_template operator_assign(final String otherValue) {
		clean_up();
		set_selection(template_sel.SPECIFIC_VALUE);
		single_value = new TitanCharString(otherValue);

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
	public TitanCharString_template operator_assign(final TitanCharString otherValue) {
		otherValue.must_bound("Assignment of an unbound charstring value to a template.");

		clean_up();
		set_selection(template_sel.SPECIFIC_VALUE);
		single_value = new TitanCharString(otherValue);

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
	public TitanCharString_template operator_assign(final TitanCharString_template otherValue) {
		if (otherValue != this) {
			clean_up();
			copy_template(otherValue);
		}

		return this;
	}

	private void copy_template(final TitanCharString_template otherValue) {
		switch (otherValue.template_selection) {
		case STRING_PATTERN:
			pattern_value_regexp_init = false;
			pattern_value_nocase = otherValue.pattern_value_nocase;
			// no break
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
			for (int i = 0; i < otherValue.value_list.size(); i++) {
				final TitanCharString_template temp = new TitanCharString_template(otherValue.value_list.get(i));
				value_list.add(temp);
			}
			break;
		case VALUE_RANGE:
			min_is_set = otherValue.min_is_set;
			min_is_exclusive = otherValue.min_is_exclusive;
			if (min_is_set) {
				min_value = new TitanCharString(otherValue.min_value);
			}
			max_is_set = otherValue.max_is_set;
			max_is_exclusive = otherValue.max_is_exclusive;
			if (max_is_set) {
				max_value = new TitanCharString(otherValue.max_value);
			}
			break;
		case DECODE_MATCH:
			dec_match = otherValue.dec_match;
			break;
		default:
			throw new TtcnError("Copying an uninitialized/unsupported charstring template.");
		}

		set_selection(otherValue);
	}

	/**
	 * Gives access to the given element. Indexing begins from zero.
	 * Over-indexing by 1 extends the charstring.
	 *
	 * Causes dynamic testcase error if the template is not a specific value.
	 * operator[] in the core.
	 *
	 * @param index_value
	 *            the index of the element to return.
	 * @return the element at the specified position in this charstring
	 * */
	public TitanCharString_Element get_at(final int index) {
		if (template_selection != template_sel.SPECIFIC_VALUE || is_ifPresent) {
			throw new TtcnError("Accessing a charstring element of a non-specific charstring template.");
		}

		return single_value.get_at(index);
	}

	/**
	 * Gives access to the given element. Indexing begins from zero.
	 * Over-indexing by 1 extends the charstring.
	 *
	 * Causes dynamic testcase error if the template is not a specific value.
	 * operator[] in the core.
	 *
	 * @param index_value
	 *            the index of the element to return.
	 * @return the element at the specified position in this charstring
	 * */
	public TitanCharString_Element get_at(final TitanInteger index_value) {
		index_value.must_bound("Indexing a charstring template with an unbound integer value.");

		return get_at(index_value.get_int());
	}

	/**
	 * Gives read-only access to the given element.
	 *
	 * Index underflow and overflow causes dynamic test case error.
	 * Also if the template is not a specific value template
	 *
	 * const operator[] const in the core.
	 *
	 * @param index
	 *            the index of the element to return.
	 * @return the element at the specified position in this charstring
	 * */
	public TitanCharString_Element constGet_at(final TitanInteger index_value) {
		index_value.must_bound("Indexing a charstring template with an unbound integer value.");

		return constGet_at(index_value.get_int());
	}

	/**
	 * Gives read-only access to the given element.
	 *
	 * Index underflow and overflow causes dynamic test case error.
	 * Also if the template is not a specific value template
	 *
	 * const operator[] const in the core.
	 *
	 * @param index
	 *            the index of the element to return.
	 * @return the element at the specified position in this charstring
	 * */
	public TitanCharString_Element constGet_at(final int index) {
		if (template_selection != template_sel.SPECIFIC_VALUE || is_ifPresent) {
			throw new TtcnError("Accessing a charstring element of a non-specific charstring template.");
		}

		return single_value.constGet_at(index);
	}

	@Override
	public boolean match(final Base_Type otherValue, final boolean legacy) {
		if (otherValue instanceof TitanCharString) {
			return match((TitanCharString) otherValue, legacy);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to charstring", otherValue));
	}

	@Override
	public void log_match(final Base_Type match_value, final boolean legacy) {
		if (match_value instanceof TitanCharString) {
			log_match((TitanCharString) match_value, legacy);
			return;
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to charstring", match_value));
	}

	/**
	 * Matches the provided value against this template.
	 *
	 * @param otherValue the value to be matched.
	 * */
	public boolean match(final TitanCharString otherValue) {
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
	public boolean match(final TitanCharString otherValue, final boolean legacy) {
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
		case VALUE_RANGE: {
			if (!min_is_set) {
				throw new TtcnError("The lower bound is not set when " +
						"matching with a charstring value range template.");
			}

			if (!max_is_set) {
				throw new TtcnError("The upper bound is not set when " +
						"matching with a charstring value range template.");
			}
			final char minValueChar = min_value.get_value().charAt(0);
			final char maxValueChar = max_value.get_value().charAt(0);
			if (minValueChar > maxValueChar) {
				throw new TtcnError("The lower bound (\"" + minValueChar + "\") is greater than the upper bound " +
						"(\"" + maxValueChar + "\") when matching with a charstring value range template.");
			}
			final StringBuilder otherStr = otherValue.get_value();
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
				if (otherStr.charAt(i) < (minValueChar + min_value_offset) || otherStr.charAt(i) > (maxValueChar - max_value_offset)) {
					return false;
				}
			}
			return true;
		}
		case STRING_PATTERN:
			if (!pattern_value_regexp_init) {
				pattern_value_posix_regexp = TTCN_Pattern.convert_pattern(single_value.get_value().toString(), pattern_value_nocase);
			}
			if (pattern_value_posix_regexp != null) {
				return TTCN_Pattern.match(otherValue.get_value().toString(), pattern_value_posix_regexp, pattern_value_nocase);
			}
			throw new TtcnError(MessageFormat.format("Cannot convert pattern \"{0}\" to POSIX-equivalent.", single_value.get_value().toString()));
		case DECODE_MATCH: {
			TTCN_EncDec.set_error_behavior(error_type.ET_ALL, error_behavior_type.EB_WARNING);
			TTCN_EncDec.clear_error();
			final TTCN_Buffer buffer = new TTCN_Buffer(otherValue);
			final boolean ret_val = dec_match.dec_match.match(buffer);
			TTCN_EncDec.set_error_behavior(error_type.ET_ALL, error_behavior_type.EB_DEFAULT);
			TTCN_EncDec.clear_error();
			return ret_val;
		}
		default:
			throw new TtcnError("Matching with an uninitialized/unsupported charstring template.");
		}
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
	public boolean match(final TitanCharString_Element otherValue, final boolean legacy) {
		return match(new TitanCharString(otherValue), legacy);
	}

	@Override
	public TitanCharString valueof() {
		if (template_selection != template_sel.SPECIFIC_VALUE) {
			throw new TtcnError("Performing a valueof or send operation on a non-specific charstring template.");
		}

		return new TitanCharString(single_value);
	}

	@Override
	public void set_type(final template_sel templateType, final int list_length) {
		clean_up();
		switch (templateType) {
		case VALUE_LIST:
		case COMPLEMENTED_LIST:
			set_selection(templateType);
			value_list = new ArrayList<TitanCharString_template>(list_length);
			for (int i = 0; i < list_length; i++) {
				value_list.add(new TitanCharString_template());
			}
			break;
		case VALUE_RANGE:
			set_selection(template_sel.VALUE_RANGE);
			min_is_set = false;
			max_is_set = false;
			min_is_exclusive = false;
			max_is_exclusive = false;
			break;
		case DECODE_MATCH:
			set_selection(template_sel.DECODE_MATCH);
			break;
		default:
			throw new TtcnError("Setting an invalid type for a charstring template.");
		}
	}

	@Override
	public TitanCharString_template list_item(final int list_index) {
		if (template_selection != template_sel.VALUE_LIST && template_selection != template_sel.COMPLEMENTED_LIST) {
			throw new TtcnError("Internal error: Accessing a list element of a non-list charstring template. ");
		}
		if (list_index < 0) {
			throw new TtcnError("Internal error: Indexing a charstring value list template with a negative index.");
		}
		if (list_index >= value_list.size()) {
			throw new TtcnError("Internal error: Index overflow in a charstring value list template.");
		}

		return value_list.get(list_index);
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

	/**
	 * Returns the number of elements, that is, the largest used index plus
	 * one and zero for the empty value.
	 *
	 * lengthof in the core
	 *
	 * @return the number of elements.
	 * */
	public TitanInteger lengthof() {
		int min_length = 0;
		boolean has_any_or_none = false;
		if (is_ifPresent) {
			throw new TtcnError("Performing lengthof() operation on a charstring template which has an ifpresent attribute.");
		}
		switch (template_selection) {
		case SPECIFIC_VALUE:
			min_length = single_value.lengthof().get_int();
			has_any_or_none = false;
			break;
		case OMIT_VALUE:
			throw new TtcnError("Performing lengthof() operation on a charstring template containing omit value.");
		case ANY_VALUE:
		case ANY_OR_OMIT:
		case VALUE_RANGE:
			min_length = 0;
			has_any_or_none = true;
			break;
		case VALUE_LIST:
			// error if any element does not have length or the
			// lengths differ
			if (value_list.isEmpty()) {
				throw new TtcnError("Internal error: Performing lengthof() operation on a charstring template containing an empty list.");
			}
			final int item_length = value_list.get(0).lengthof().get_int();
			for (int i = 1; i < value_list.size(); i++) {
				if (value_list.get(i).lengthof().get_int() != item_length) {
					throw new TtcnError("Performing lengthof() operation on a charstring template containing a value list with different lengths.");
				}
			}
			min_length = item_length;
			has_any_or_none = false;
			break;
		case COMPLEMENTED_LIST:
			throw new TtcnError("Performing lengthof() operation on a charstring template containing complemented list.");
		case STRING_PATTERN:
			throw new TtcnError("Performing lengthof() operation on a charstring template containing a pattern is not allowed.");
		default:
			throw new TtcnError("Performing lengthof() operation on an uninitialized/unsupported charstring template.");
		}

		return new TitanInteger(check_section_is_single(min_length, has_any_or_none, "length", "a", "charstring template"));
	}

	public void set_min(final String otherMinValue) {
		if (template_selection != template_sel.VALUE_RANGE) {
			throw new TtcnError("Setting the lower bound for a non-range charstring template.");
		}

		final int length = otherMinValue.length();
		if (length != 1) {
			throw new TtcnError(MessageFormat.format("The length of the lower bound in a charstring value range template must be 1 instead of `{0}''. ", length));
		}
		min_is_set = true;
		min_is_exclusive = false;
		min_value = new TitanCharString(otherMinValue);
		if (max_is_set && min_value.lengthof().get_int() > (max_value.lengthof().get_int())) {
			throw new TtcnError(MessageFormat.format("The lower bound {0} in a charstring value range template is greater than the upper bound {1}.", min_value, max_value));
		}
	}

	public void set_min(final TitanCharString otherMinValue) {
		if (template_selection != template_sel.VALUE_RANGE) {
			throw new TtcnError("Setting the lower bound for a non-range charstring template.");
		}
		otherMinValue.must_bound("Setting an unbound value as lower bound in a charstring value range template.");

		final int length = otherMinValue.lengthof().get_int();
		if (length != 1) {
			throw new TtcnError(MessageFormat.format("The length of the lower bound in a charstring value range template must be 1 instead of `{0}''. ", length));
		}
		min_is_set = true;
		min_is_exclusive = false;
		min_value = new TitanCharString(otherMinValue);
		if (max_is_set && min_value.lengthof().get_int() > (max_value.lengthof().get_int())) {
			throw new TtcnError(MessageFormat.format("The lower bound {0} in a charstring value range template is greater than the upper bound {1}.", min_value, max_value));
		}
	}

	public void set_max(final String otherMaxValue) {
		if (template_selection != template_sel.VALUE_RANGE) {
			throw new TtcnError("Setting the upper bound for a non-range charstring template.");
		}

		final int length = otherMaxValue.length();
		if (length != 1) {
			throw new TtcnError(MessageFormat.format("The length of the upper bound in a charstring value range template must be 1 instead of {0}.", length));
		}
		max_is_set = true;
		max_is_exclusive = false;
		max_value = new TitanCharString(otherMaxValue);
		if (min_is_set && min_value.lengthof().get_int() > max_value.lengthof().get_int()) {
			throw new TtcnError(MessageFormat.format("The upper bound `{0}'' in a charstring value range template is smaller than the lower bound {1}.", max_value, min_value));
		}
	}

	public void set_max(final TitanCharString otherMaxValue) {
		if (template_selection != template_sel.VALUE_RANGE) {
			throw new TtcnError("Setting the upper bound for a non-range charstring template.");
		}
		otherMaxValue.must_bound("Setting an unbound value as upper bound in a charstring value range template.");

		final int length = otherMaxValue.lengthof().get_int();
		if (length != 1) {
			throw new TtcnError(MessageFormat.format("The length of the upper bound in a charstring value range template must be 1 instead of {0}.", length));
		}
		max_is_set = true;
		max_is_exclusive = false;
		max_value = new TitanCharString(otherMaxValue);
		if (min_is_set && min_value.lengthof().get_int() > max_value.lengthof().get_int()) {
			throw new TtcnError(MessageFormat.format("The upper bound `{0}'' in a charstring value range template is smaller than the lower bound {1}.", max_value, min_value));
		}
	}

	public void set_min_exclusive(final boolean minExclusive) {
		if (template_selection != template_sel.VALUE_RANGE) {
			throw new TtcnError("Setting the lower bound for a non-range charstring template.");
		}
		min_is_exclusive = minExclusive;
	}

	public void set_max_exclusive(final boolean maxExclusive) {
		if (template_selection != template_sel.VALUE_RANGE) {
			throw new TtcnError("Setting the upper bound for a non-range charstring template.");
		}
		max_is_exclusive = maxExclusive;
	}

	public void set_decmatch(final IDecode_Match dec_match) {
		if (template_selection != template_sel.DECODE_MATCH) {
			throw new TtcnError("Setting the decoded content matching mechanism of a non-decmatch charstring template.");
		}

		this.dec_match = new Unichar_Decmatch();
		this.dec_match.dec_match = dec_match;
		this.dec_match.coding = CharCoding.UTF_8;
	}

	public Object get_decmatch_dec_res() {
		if (template_selection != template_sel.DECODE_MATCH) {
			throw new TtcnError("Retrieving the decoding result of a non-decmatch charstring template.");
		}

		return dec_match.dec_match.get_dec_res();
	}

	public TTCN_Typedescriptor get_decmatch_type_descr() {
		if (template_selection != template_sel.DECODE_MATCH) {
			throw new TtcnError("Retrieving the decoded type's descriptor in a non-decmatch charstring template.");
		}

		return dec_match.dec_match.get_type_descr();
	}

	@Override
	public void log() {
		switch (template_selection) {
		case STRING_PATTERN:
			log_pattern(single_value.lengthof().get_int(), single_value.get_value().toString(), pattern_value_nocase);
			break;
		case SPECIFIC_VALUE: {
			single_value.log();
			break;
		}
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
		case VALUE_RANGE:
			TTCN_Logger.log_char('(');
			if (min_is_exclusive) {
				TTCN_Logger.log_char('!');
			}
			if (min_is_set) {
				if (TTCN_Logger.is_printable(min_value.get_value().charAt(0))) {
					TTCN_Logger.log_char('"');
					TTCN_Logger.log_char_escaped(min_value.get_value().charAt(0));
					TTCN_Logger.log_char('"');

				} else {
					TTCN_Logger.log_event_str(MessageFormat.format("char(0, 0, 0, 0)", (int) min_value.get_value().charAt(0)));
				}
			} else {
				TTCN_Logger.log_event_str("<unknown lower bound>");
			}
			TTCN_Logger.log_event_str(" .. ");
			if (max_is_exclusive) {
				TTCN_Logger.log_char('!');
			}
			if (max_is_set) {
				if (TTCN_Logger.is_printable(max_value.get_value().charAt(0))) {
					TTCN_Logger.log_char('"');
					TTCN_Logger.log_char_escaped(max_value.get_value().charAt(0));
					TTCN_Logger.log_char('"');
				} else {
					TTCN_Logger.log_event_str(MessageFormat.format("char(0, 0, 0, 0)", (int) max_value.get_value().charAt(0)));
				}
			} else {
				TTCN_Logger.log_event_str("<unknown upper bound>");
			}

			TTCN_Logger.log_char(')');
			break;
		case DECODE_MATCH:
			TTCN_Logger.log_event_str("decmatch ");
			dec_match.dec_match.log();
			break;
		default:
			log_generic();
			break;
		}
		log_restricted();
		log_ifpresent();
	}

	@Override
	public void set_param(final Module_Parameter param) {
		param.basic_check(basic_check_bits_t.BC_TEMPLATE.getValue()|basic_check_bits_t.BC_LIST.getValue(), "charstring template");
		switch (param.get_type()) {
		case MP_Omit:
			operator_assign(template_sel.OMIT_VALUE);
			break;
		case MP_Any:
			operator_assign(template_sel.ANY_VALUE);
			break;
		case MP_AnyOrNone:
			operator_assign(template_sel.ANY_OR_OMIT);
			break;
		case MP_List_Template:
		case MP_ComplementList_Template: {
			final TitanCharString_template temp = new TitanCharString_template();
			temp.set_type(param.get_type() == type_t.MP_List_Template ? template_sel.VALUE_LIST : template_sel.COMPLEMENTED_LIST, param.get_size());
			for (int i = 0; i < param.get_size(); i++) {
				temp.list_item(i).set_param(param.get_elem(i));
			}
			operator_assign(temp);
			break;
		}
		case MP_Charstring:
			this.operator_assign(new TitanCharString((String)param.get_string_data()));
			break;
		case MP_StringRange: {
			final TitanUniversalChar lower_uchar = param.get_lower_uchar();
			final TitanUniversalChar upper_uchar = param.get_upper_uchar();
			if (!lower_uchar.is_char()) {
				param.error("Lower bound of char range cannot be a multiple-byte character");
			}
			if (!upper_uchar.is_char()) {
				param.error("Upper bound of char range cannot be a multiple-byte character");
			}
			clean_up();
			set_selection(template_sel.VALUE_RANGE);
			min_is_set = true;
			max_is_set = true;
			min_value = new TitanCharString(String.valueOf(lower_uchar.getUc_cell()));
			max_value = new TitanCharString(String.valueOf(upper_uchar.getUc_cell()));
			min_is_exclusive = param.get_is_min_exclusive();
			max_is_exclusive = param.get_is_max_exclusive();
			break;
		}
		case MP_Pattern:
			clean_up();
			single_value = new TitanCharString(param.get_pattern());
			pattern_value_regexp_init = false;
			pattern_value_nocase = param.get_nocase();
			set_selection(template_sel.STRING_PATTERN);
			break;
		case MP_Expression:
			if (param.get_expr_type() == expression_operand_t.EXPR_CONCATENATE) {
				// only allow string patterns for the first operand
				final TitanCharString operand1 = new TitanCharString();
				final TitanCharString operand2 = new TitanCharString();
				final TitanCharString result = new TitanCharString();
				final AtomicBoolean nocase = new AtomicBoolean();
				final boolean is_pattern = operand1.set_param_internal(param.get_operand1(),true,nocase);
				operand2.set_param(param.get_operand2());
				result.operator_assign(operand1.operator_concatenate(operand2));
				if (is_pattern) {
					clean_up();
					single_value = result;
					pattern_value_regexp_init = false;
					pattern_value_nocase = nocase.get();
					set_selection(template_sel.STRING_PATTERN);
				} else {
					operator_assign(result);
				}
			} else {
				param.expr_type_error("a charstring");
			}
			break;
		default:
			param.type_error("charstring template");
			break;
		}
		is_ifPresent = param.get_ifpresent();
		if (param.get_length_restriction() != null) {
			set_length_range(param);
		}
	}
	
	//We cannot cast TitanCharStringTemplates to TitanCharString like C++
	public TitanCharString castForPatterns() {
		if (template_selection == template_sel.STRING_PATTERN) {
			return new TitanCharString(single_value);
		} else if (template_selection == template_sel.SPECIFIC_VALUE) {
			return new TitanCharString(single_value);
		} else {
			//TODO: better error message
			throw new TtcnError("Internal error: using a non-acceptable template for pattern cast.");
		}
	}

	private enum LogPatternState {
		INITIAL, BACKSLASH, BACKSLASH_Q, QUADRUPLE, HASHMARK, REPETITIONS
	};

	static void log_pattern(final int n_chars, final String chars_ptr, final boolean nocase) {
		TTCN_Logger.log_event_str("pattern ");
		if (nocase) {
			TTCN_Logger.log_event_str("@nocase ");
		}
		TTCN_Logger.log_event_str("\"");
		LogPatternState state = LogPatternState.INITIAL;
		for (int i = 0; i < n_chars; i++) {
			final char c = chars_ptr.charAt(i);
			// print the character
			if (32 <= c) {
				// printable character
				switch (c) {
				case '"':
					TTCN_Logger.log_event_str("\\\"");
					break;
				case '{':
					if (state == LogPatternState.BACKSLASH || state == LogPatternState.BACKSLASH_Q) {
						TTCN_Logger.log_char('{');
					} else {
						TTCN_Logger.log_event_str("\\{");
					}
					break;
				case '}':
					if (state == LogPatternState.BACKSLASH || state == LogPatternState.QUADRUPLE) {
						TTCN_Logger.log_char('}');
					} else {
						TTCN_Logger.log_event_str("\\}");
					}
					break;
				case ' ':
					if (state != LogPatternState.INITIAL && state != LogPatternState.BACKSLASH) {
						break;
					}
					// no break
				default:
					TTCN_Logger.log_char(c);
					break;
				}
			} else {
				switch (c) {
				case '\t':
					if (state == LogPatternState.INITIAL || state == LogPatternState.BACKSLASH) {
						TTCN_Logger.log_event_str("\\t");
					}
					break;
				case '\r':
					if (state == LogPatternState.INITIAL || state == LogPatternState.BACKSLASH) {
						TTCN_Logger.log_event_str("\\r");
					}
					break;
				case '\n':
				case '\u000b': // \v
				case '\f':
					if (state != LogPatternState.INITIAL && state != LogPatternState.BACKSLASH) {
						break;
					}
					// no break
				default:
					TTCN_Logger.log_event("\\q{0,0,0,"+(int)c+"}");
					break;
				}
			}
			// update the state
			switch (state) {
			case INITIAL:
				switch (c) {
				case '\\':
					state = LogPatternState.BACKSLASH;
					break;
				case '#':
					state = LogPatternState.HASHMARK;
					break;
				default:
					break;
				}
				break;
			case BACKSLASH:
				if (c == 'q') {
					state = LogPatternState.BACKSLASH_Q;
				} else {
					state = LogPatternState.INITIAL;
				}
				break;
			case BACKSLASH_Q:
				switch (c) {
				case '{':
					state = LogPatternState.QUADRUPLE;
					break;
				case ' ':
				case '\t':
				case '\r':
				case '\n':
				case '\u000b': // \v
				case '\f':
					break;
				default:
					state = LogPatternState.INITIAL;
					break;
				}
				break;
			case HASHMARK:
				switch (c) {
				case '(':
					state = LogPatternState.REPETITIONS;
					break;
				case ' ':
				case '\t':
				case '\r':
				case '\n':
				case '\u000b': // \v
				case '\f':
					break;
				default:
					state = LogPatternState.INITIAL;
					break;
				}
				break;
			case QUADRUPLE:
			case REPETITIONS:
				switch (c) {
				case ' ':
				case '\t':
				case '\r':
				case '\n':
				case '\u000b': // \v
				case '\f':
				case ',':
					break;
				default:
					if (!Character.isDigit(c)) {
						state = LogPatternState.INITIAL;
					}
					break;
				}
				break;
			}
		}
		TTCN_Logger.log_char('"');
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
	public void log_match(final TitanCharString match_value, final boolean legacy) {
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
	/** {@inheritDoc} */
	public void encode_text(final Text_Buf text_buf) {
		encode_text_restricted(text_buf);

		switch (template_selection) {
		case OMIT_VALUE:
		case ANY_VALUE:
		case ANY_OR_OMIT:
			break;
		case STRING_PATTERN:
			text_buf.push_int(pattern_value_nocase ? 1 : 0);
			single_value.encode_text(text_buf);
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
		case VALUE_RANGE:
			if (!min_is_set) {
				throw new TtcnError("Text encoder: The lower bound is not set in a charstring value range template.");
			}
			if (!max_is_set) {
				throw new TtcnError("Text encoder: The upper bound is not set in a charstring value range template.");
			}
			final byte[] temp = new byte[1];
			temp[0] = (byte)min_value.get_value().charAt(0);
			text_buf.push_raw(1, temp);
			temp[0] = (byte)max_value.get_value().charAt(0);
			text_buf.push_raw(1, temp);
			break;
		default:
			throw new TtcnError("Text encoder: Encoding an uninitialized/unsupported charstring template.");
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
		case STRING_PATTERN:{
			pattern_value_regexp_init = false;
			final int temp = text_buf.pull_int().get_int();
			pattern_value_nocase = temp == 1;
			single_value = new TitanCharString();
			single_value.decode_text(text_buf);
			break;
		}
		case SPECIFIC_VALUE:
			single_value = new TitanCharString();
			single_value.decode_text(text_buf);
			break;
		case VALUE_LIST:
		case COMPLEMENTED_LIST: {
			final int size = text_buf.pull_int().get_int();
			value_list = new ArrayList<TitanCharString_template>(size);
			for (int i = 0; i < size; i++) {
				final TitanCharString_template temp = new TitanCharString_template();
				temp.decode_text(text_buf);
				value_list.add(temp);
			}
			break;
		}
		case VALUE_RANGE:
			final byte[] temp = new byte[1];
			text_buf.pull_raw(1, temp);
			min_value = new TitanCharString();
			min_value.get_value().setCharAt(0, (char)temp[0]);
			text_buf.pull_raw(1, temp);
			max_value = new TitanCharString();
			max_value.get_value().setCharAt(0, (char)temp[0]);
			min_is_set = true;
			max_is_set = true;
			min_is_exclusive = false;
			max_is_exclusive = false;
			break;
		default:
			throw new TtcnError("Text decoder: An unknown/unsupported selection was received for a charstring template.");
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

		throw new TtcnError(MessageFormat.format("Restriction `{0}'' on template of type {1} violated.", get_res_name(restriction), name == null ? "charstring" : name));
	}
}