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
	
	//TODO add is_ifpresent
	
	template_sel templateSelection;
	
	protected Base_Template() {
		templateSelection = template_sel.UNINITIALIZED_TEMPLATE;
	}
	
	protected Base_Template(final template_sel otherValue) {
		templateSelection = otherValue;
	}
	
	protected void setSelection(final template_sel otherValue) {
		templateSelection = otherValue;
	}
	
	protected void setSelection(final Base_Template otherValue) {
		templateSelection = otherValue.templateSelection;
	}
	
	public template_sel getSelection() {
		return templateSelection;
	}
	
	protected void checkSingleSelection(final template_sel otherValue) {
		switch (otherValue) {
		case ANY_VALUE:
		case OMIT_VALUE:
		case ANY_OR_OMIT:
			break;
		default:
			throw new TtcnError("Initialization of a template with an invalid selection.");
		}
	}
}
