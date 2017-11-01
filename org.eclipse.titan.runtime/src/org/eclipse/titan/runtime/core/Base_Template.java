/******************************************************************************
 * Copyright (c) 2000-2017 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
		UNINITIALIZED_TEMPLATE,
		SPECIFIC_VALUE,
		OMIT_VALUE,
		ANY_VALUE,
		ANY_OR_OMIT,
		VALUE_LIST,
		COMPLEMENTED_LIST,
		VALUE_RANGE,
		STRING_PATTERN,
		SUPERSET_MATCH,
		SUBSET_MATCH,
		DECODE_MATCH
	};

	public enum template_res {
		TR_VALUE,
		TR_OMIT,
		TR_PRESENT
	};

	protected template_sel templateSelection;
	protected boolean is_ifPresent;

	protected Base_Template() {
		templateSelection = template_sel.UNINITIALIZED_TEMPLATE;
		is_ifPresent = false;
	}

	protected Base_Template(final template_sel otherValue) {
		templateSelection = otherValue;
		is_ifPresent = false;
	}

	protected void setSelection(final template_sel otherValue) {
		templateSelection = otherValue;
		is_ifPresent = false;
	}

	protected void setSelection(final Base_Template otherValue) {
		templateSelection = otherValue.templateSelection;
		is_ifPresent = otherValue.is_ifPresent;
	}

	public template_sel getSelection() {
		return templateSelection;
	}

	public void set_ifPresent() {
		is_ifPresent = true;
	}

	//originally isBound
	public boolean isBound() {
		return templateSelection != template_sel.UNINITIALIZED_TEMPLATE;
	}
	//originally is_value
	public boolean isValue() {
		return !is_ifPresent && templateSelection == template_sel.SPECIFIC_VALUE;
	}

	//originally clean_up
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
			TtcnLogger.log_event_uninitialized();
			break;
		case OMIT_VALUE:
			TtcnLogger.log_event_str("omit");
			break;
		case ANY_VALUE:
			TtcnLogger.log_char('?');
			break;
		case ANY_OR_OMIT:
			TtcnLogger.log_char('*');
			break;
		default:
			TtcnLogger.log_event_str("<unknown template selection>");
			break;
		}
	}

	protected void log_ifpresent() {
		if (is_ifPresent) {
			TtcnLogger.log_event_str(" ifpresent");
		}
	}

	protected boolean get_istemplate_kind(final String type) {
		if("value".equals(type)) {
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
			return templateSelection == template_sel.STRING_PATTERN;
		} else if ("AnyElement".equals(type) || "AnyElementsOrNone".equals(type) ||
				"permutation".equals(type) || "length".equals(type)) {
			return false;
		}
		throw new TtcnError( MessageFormat.format( "Incorrect second parameter ({0}) was passed to istemplatekind.", type ) );
	}

	public boolean isOmit() {
		return templateSelection == template_sel.OMIT_VALUE && !is_ifPresent;
	}

	public boolean is_any_or_omit() {
		return templateSelection == template_sel.ANY_OR_OMIT && !is_ifPresent;
	}

	public abstract Base_Template assign(final Base_Type otherValue);
	public abstract Base_Template assign(final Base_Template otherValue);
	public abstract boolean match(final Base_Type otherValue, final boolean legacy);
	public abstract Base_Type valueOf();
	public abstract void log();

	public void log_match(final Base_Type match_value, final boolean legacy) {
		//do nothing for now.
		// TODO once the log_match is implemented for all classes this function should become abstract
		TtcnLogger.log_event_str( "//TODO: " );
		TtcnLogger.log_event_str( getClass().getSimpleName() );
		TtcnLogger.log_event_str( ".log_match() is not implemented!\n" );
	}
}
