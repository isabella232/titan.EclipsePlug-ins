/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

import java.text.MessageFormat;

/**
 * Base_Template in core
 *
 * @author Kristof Szabados
 * @author Arpad Lovassy
 */
public abstract class Base_Template {
	public enum template_sel {
		UNINITIALIZED_TEMPLATE (0),
		SPECIFIC_VALUE (1),
		OMIT_VALUE (2),
		ANY_VALUE (3),
		ANY_OR_OMIT (4),
		VALUE_LIST (5),
		COMPLEMENTED_LIST (6),
		VALUE_RANGE (7),
		STRING_PATTERN (8),
		SUPERSET_MATCH (9),
		SUBSET_MATCH (10),
		DECODE_MATCH (11);

		private final int value;
		template_sel (final int value) {
			this.value = value;
		}
		public int getValue() {
			return value;
		}
		public static template_sel getWithValue(final int value) {
			return values()[value];
		}
	};

	public enum template_res {
		TR_VALUE,
		TR_OMIT,
		TR_PRESENT
	};

	protected template_sel template_selection;
	protected boolean is_ifPresent;

	/**
	 * Initializes to unbound/uninitialized template.
	 * */
	protected Base_Template() {
		template_selection = template_sel.UNINITIALIZED_TEMPLATE;
		is_ifPresent = false;
	}

	/**
	 * Initializes to a given template kind.
	 *
	 * @param other_value
	 *                the template kind to initialize to.
	 * */
	protected Base_Template(final template_sel otherValue) {
		template_selection = otherValue;
		is_ifPresent = false;
	}

	protected void set_selection(final template_sel otherValue) {
		template_selection = otherValue;
		is_ifPresent = false;
	}

	protected void set_selection(final Base_Template otherValue) {
		template_selection = otherValue.template_selection;
		is_ifPresent = otherValue.is_ifPresent;
	}

	public template_sel get_selection() {
		return template_selection;
	}

	public void set_ifPresent() {
		is_ifPresent = true;
	}

	/**
	 * Whether the value is bound.
	 * 
	 * is_bound() in the core.
	 * 
	 * @return {@code true} if the value is bound.
	 */
	public boolean is_bound() {
		return template_selection != template_sel.UNINITIALIZED_TEMPLATE;
	}

	/**
	 * Whether the value is a actual value.
	 *
	 * is_value in the core.
	 *
	 * @return {@code true} if the value is a actual value.
	 */
	public boolean is_value() {
		return !is_ifPresent && template_selection == template_sel.SPECIFIC_VALUE;
	}

	/**
	 * Deletes the template, setting it to unbound.
	 *
	 * clean_up() in the core
	 * */
	public void clean_up() {
		template_selection = template_sel.UNINITIALIZED_TEMPLATE;
	}

	/**
	 * Checks that the provided template selection is ?, * or omit. Any
	 * other selection type causes a dynamic testcase error.
	 *
	 * @param otherValue
	 *                the template selection to check.
	 * */
	protected static void check_single_selection(final template_sel otherValue) {
		switch (otherValue) {
		case ANY_VALUE:
		case OMIT_VALUE:
		case ANY_OR_OMIT:
			break;
		default:
			throw new TtcnError("Initialization of a template with an invalid selection.");
		}
	}

	/**
	 * returns the name of template restriction.
	 *
	 * @param tr
	 *                the template restriction.
	 * @return the name of the provided template restriction.
	 * */
	protected static String get_res_name(final template_res tr) {
		switch (tr) {
		case TR_VALUE: return "value";
		case TR_OMIT: return "omit";
		case TR_PRESENT: return "present";
		default: break;
		}
		return "<unknown/invalid>";
	}

	protected void log_generic() {
		switch (template_selection) {
		case UNINITIALIZED_TEMPLATE:
			TTCN_Logger.log_event_uninitialized();
			break;
		case OMIT_VALUE:
			TTCN_Logger.log_event_str("omit");
			break;
		case ANY_VALUE:
			TTCN_Logger.log_char('?');
			break;
		case ANY_OR_OMIT:
			TTCN_Logger.log_char('*');
			break;
		default:
			TTCN_Logger.log_event_str("<unknown template selection>");
			break;
		}
	}

	protected void log_ifpresent() {
		if (is_ifPresent) {
			TTCN_Logger.log_event_str(" ifpresent");
		}
	}

	protected void encode_text_base(final Text_Buf text_buf) {
		text_buf.push_int(template_selection.getValue());
		text_buf.push_int(is_ifPresent ? 1 : 0);
	}

	protected void decode_text_base(final Text_Buf text_buf) {
		template_selection = template_sel.getWithValue(text_buf.pull_int().get_int());
		is_ifPresent = text_buf.pull_int().get_int() == 1;
	}

	public boolean get_istemplate_kind(final String type) {
		if ("value".equals(type)) {
			return is_value();
		} else if ("list".equals(type)) {
			return template_selection == template_sel.VALUE_LIST;
		} else if ("complement".equals(type)) {
			return template_selection == template_sel.COMPLEMENTED_LIST;
		} else if ("?".equals(type) || "AnyValue".equals(type)) {
			return template_selection == template_sel.ANY_VALUE;
		} else if ("*".equals(type) || "AnyValueOrNone".equals(type)) {
			return template_selection == template_sel.ANY_OR_OMIT;
		} else if ("range".equals(type)) {
			return template_selection == template_sel.VALUE_RANGE;
		} else if ("superset".equals(type)) {
			return template_selection == template_sel.SUPERSET_MATCH;
		} else if ("subset".equals(type)) {
			return template_selection == template_sel.SUBSET_MATCH;
		} else if ("omit".equals(type)) {
			return template_selection == template_sel.OMIT_VALUE;
		} else if ("decmatch".equals(type)) {
			return template_selection == template_sel.DECODE_MATCH;
		} else if ("ifpresent".equals(type)) {
			return is_ifPresent;
		} else if ("pattern".equals(type)) {
			throw new TtcnError("Pattenr support is not yet implement!");
//			return template_selection == template_sel.STRING_PATTERN;
		} else if ("AnyElement".equals(type) || "AnyElementsOrNone".equals(type) ||
				"permutation".equals(type) || "length".equals(type)) {
			return false;
		}
		throw new TtcnError(MessageFormat.format( "Incorrect second parameter ({0}) was passed to istemplatekind.", type));
	}

	protected boolean get_istemplate_kind(final TitanCharString type) {
		return get_istemplate_kind(type.get_value().toString());
	}

	public boolean is_omit() {
		return template_selection == template_sel.OMIT_VALUE && !is_ifPresent;
	}

	public boolean is_any_or_omit() {
		return template_selection == template_sel.ANY_OR_OMIT && !is_ifPresent;
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
	public abstract Base_Template operator_assign(final Base_Type otherValue);

	/**
	 * Assigns the other template to this template.
	 * Overwriting the current content in the process.
	 *<p>
	 * operator= in the core.
	 *
	 * @param otherValue
	 *                the other template to assign.
	 * @return the new template object.
	 */
	public abstract Base_Template operator_assign(final Base_Template otherValue);

	/**
	 * Sets the current selection to the provided value.
	 * Overwriting the current content in the process.
	 *<p>
	 * operator= in the core.
	 *
	 * @param otherValue
	 *                the other value to assign.
	 * @return the new template object.
	 */
	public abstract Base_Template operator_assign(final template_sel otherValue);

	/**
	 * Matches the provided value against this template. In legacy mode
	 * omitted value fields are not matched against the template field.
	 *
	 * @param otherValue
	 *                the value to be matched.
	 * @param legacy
	 *                use legacy mode.
	 * */
	public abstract boolean match(final Base_Type otherValue, final boolean legacy);

	/**
	 * Returns the value of a specific value template, causes dynamic
	 * testcase error otherwise.
	 *
	 * @return the value of the specific value template.
	 * */
	public abstract Base_Type valueof();

	/**
	 * Sets the type of the template to the provided value list or
	 * complemented list kind, initializing the list to be empty.
	 * <p>
	 * Using any other template kind than value list or complemented list
	 * causes dynamic testcase error.
	 * <p>
	 * set_type in the core (with default parameter).
	 *
	 * @param template_type
	 *                the template kind to set (value list or complemented
	 *                list).
	 * */
	public void set_type(final template_sel template_type) {
		set_type(template_type, 0);
	}

	/**
	 * Sets the type of the template to the provided value list or
	 * complemented list kind, also setting its expected size.
	 * <p>
	 * Using any other template kind than value list or complemented list
	 * causes dynamic testcase error.
	 * <p>
	 * set_type in the core.
	 *
	 * @param template_type
	 *                the template kind to set (value list or complemented
	 *                list).
	 * @param list_length
	 *                the length the list should be initialized to.
	 * */
	public abstract void set_type(final template_sel template_type, final int list_length);

	/**
	 * Returns the template at the specified position in a value list or
	 * complemented list template.
	 * <p>
	 * Under and over indexing causes dynamic testcase error, also if the
	 * template is not a value list or complemented list template.
	 * <p>
	 * list_item in the core.
	 * 
	 * @param list_index
	 *                index of the element to return
	 * @return the template at the specified position in this list
	 */
	public abstract Base_Template list_item(final int list_index);

	/**
	 * Logs this template.
	 */
	public abstract void log();

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
	public abstract void log_match(final Base_Type match_value, final boolean legacy);

	public abstract void set_param(final Param_Types.Module_Parameter param);

	/**
	 * Checks whether the template is present. A template is_present if it
	 * is not uninitialized and does not match omit.
	 * 
	 * Note: this is not the TTCN-3 ispresent()! causes DTE, must be used
	 * only if the field is OPTIONAL<>
	 *
	 * is_present() in the core (with default parameter).
	 *
	 * @return {@code true} if the template is present.
	 */
	public boolean is_present() {
		return is_present(false);
	}

	/**
	 * Checks whether the template is present. A template is_present if it
	 * is not uninitialized and does not match omit.
	 * 
	 * Note: this is not the TTCN-3 ispresent()! causes DTE, must be used
	 * only if the field is OPTIONAL<>
	 *
	 * is_present() in the core.
	 *
	 * @param legacy
	 *                in this mode if any of the list restriction of the
	 *                template matching omit, the template is recognized as
	 *                matching omit.
	 * @return {@code true} if the template is present.
	 */
	public boolean is_present(final boolean legacy) {
		if (template_selection == template_sel.UNINITIALIZED_TEMPLATE) {
			return false;
		}

		return !match_omit(legacy);
	}

	// originally match_omit (with default parameter)
	public boolean match_omit() {
		return match_omit(false);
	}

	public abstract boolean match_omit(final boolean legacy);

	public abstract void encode_text(final Text_Buf text_buf);

	public abstract void decode_text(final Text_Buf text_buf);

	public void check_restriction(final template_res restriction, final String name) {
		check_restriction(restriction, name, false);
	}

	//TODO investigate how to extract the common implementations here
	public abstract void check_restriction(final template_res restriction, final String name, final boolean legacy);
}
