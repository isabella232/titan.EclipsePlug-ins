package org.eclipse.titan.runtime.core;

/**
 * Base_Template in core
 * */
public class Base_Template {
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

}
