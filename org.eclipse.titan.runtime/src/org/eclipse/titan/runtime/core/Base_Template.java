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

	protected template_sel templateSelection;
	protected boolean is_ifPresent;

	/**
	 * Initializes to unbound/uninitialized template.
	 * */
	protected Base_Template() {
		templateSelection = template_sel.UNINITIALIZED_TEMPLATE;
		is_ifPresent = false;
	}

	/**
	 * Initializes to a given template kind.
	 *
	 * @param other_value
	 *                the template kind to initialize to.
	 * */
	protected Base_Template(final template_sel otherValue) {
		templateSelection = otherValue;
		is_ifPresent = false;
	}

	protected void set_selection(final template_sel otherValue) {
		templateSelection = otherValue;
		is_ifPresent = false;
	}

	protected void set_selection(final Base_Template otherValue) {
		templateSelection = otherValue.templateSelection;
		is_ifPresent = otherValue.is_ifPresent;
	}

	public template_sel get_selection() {
		return templateSelection;
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
	public boolean isBound() {
		return templateSelection != template_sel.UNINITIALIZED_TEMPLATE;
	}

	/**
	 * Whether the value is a actual value.
	 *
	 * is_value in the core.
	 *
	 * @return {@code true} if the value is a actual value.
	 */
	public boolean isValue() {
		return !is_ifPresent && templateSelection == template_sel.SPECIFIC_VALUE;
	}

	/**
	 * Deletes the template, setting it to unbound.
	 *
	 * clean_up() in the core
	 * */
	public void cleanUp() {
		templateSelection = template_sel.UNINITIALIZED_TEMPLATE;
	}

	//originally check_single_selection
	protected static void checkSingleSelection(final template_sel otherValue) {
		switch (otherValue) {
		case ANY_VALUE:
		case OMIT_VALUE:
		case ANY_OR_OMIT:
			break;
		default:
			throw new TtcnError("Initialization of a template with an invalid selection.");
		}
	}

	protected static String getResName(final template_res tr) {
		switch (tr) {
		case TR_VALUE: return "value";
		case TR_OMIT: return "omit";
		case TR_PRESENT: return "present";
		default: break;
		}
		return "<unknown/invalid>";
	}

	protected void log_generic() {
		switch (templateSelection) {
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
		text_buf.push_int(templateSelection.getValue());
		text_buf.push_int(is_ifPresent ? 1 : 0);
	}

	protected void decode_text_base(final Text_Buf text_buf) {
		templateSelection = template_sel.getWithValue(text_buf.pull_int().getInt());
		is_ifPresent = text_buf.pull_int().getInt() == 1;
	}

	public boolean get_istemplate_kind(final String type) {
		if ("value".equals(type)) {
			return isValue();
		} else if ("list".equals(type)) {
			return templateSelection == template_sel.VALUE_LIST;
		} else if ("complement".equals(type)) {
			return templateSelection == template_sel.COMPLEMENTED_LIST;
		} else if ("?".equals(type) || "AnyValue".equals(type)) {
			return templateSelection == template_sel.ANY_VALUE;
		} else if ("*".equals(type) || "AnyValueOrNone".equals(type)) {
			return templateSelection == template_sel.ANY_OR_OMIT;
		} else if ("range".equals(type)) {
			return templateSelection == template_sel.VALUE_RANGE;
		} else if ("superset".equals(type)) {
			return templateSelection == template_sel.SUPERSET_MATCH;
		} else if ("subset".equals(type)) {
			return templateSelection == template_sel.SUBSET_MATCH;
		} else if ("omit".equals(type)) {
			return templateSelection == template_sel.OMIT_VALUE;
		} else if ("decmatch".equals(type)) {
			return templateSelection == template_sel.DECODE_MATCH;
		} else if ("ifpresent".equals(type)) {
			return is_ifPresent;
		} else if ("pattern".equals(type)) {
			throw new TtcnError("Pattenr support is not yet implement!");
//			return templateSelection == template_sel.STRING_PATTERN;
		} else if ("AnyElement".equals(type) || "AnyElementsOrNone".equals(type) ||
				"permutation".equals(type) || "length".equals(type)) {
			return false;
		}
		throw new TtcnError(MessageFormat.format( "Incorrect second parameter ({0}) was passed to istemplatekind.", type));
	}

	protected boolean get_istemplate_kind(final TitanCharString type) {
		return get_istemplate_kind(type.getValue().toString());
	}

	public boolean isOmit() {
		return templateSelection == template_sel.OMIT_VALUE && !is_ifPresent;
	}

	public boolean is_any_or_omit() {
		return templateSelection == template_sel.ANY_OR_OMIT && !is_ifPresent;
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
	public abstract Base_Template assign(final Base_Type otherValue);

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
	public abstract Base_Template assign(final Base_Template otherValue);

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
	public abstract Base_Template assign(final template_sel otherValue);

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
	 * Returns the value of a specific value template, causes dynamic testcase error otherwise.
	 *<p>
	 * valueof() in the core.
	 *
	 * @return the value of the specific value template.
	 * */
	public abstract Base_Type valueOf();

	/**
	 * Logs this template.
	 */
	public abstract void log();

	public abstract void log_match(final Base_Type match_value, final boolean legacy);

	public abstract void set_param(final Param_Types.Module_Parameter param);

	// originally is_present (with default parameter)
	public boolean isPresent() {
		return isPresent(false);
	}

	// originally is_present
	public boolean isPresent(final boolean legacy) {
		if (templateSelection == template_sel.UNINITIALIZED_TEMPLATE) {
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
