package org.eclipse.titan.runtime.core;

import java.util.ArrayList;

/**
 * TTCN-3 component template
 * 
 *  //FIXME a lot to implement 
 *  
 * @author Kristof Szabados
 */
public class TitanComponent_template extends Base_Template {
	public static final TitanComponent_template any_compref = new TitanComponent_template(template_sel.ANY_VALUE);

	// TODO maybe should be renamed in core
	private int single_value;

	// value_list part
	private ArrayList<TitanComponent_template> value_list;

	public TitanComponent_template() {
		//intentionally empty
	}

	public TitanComponent_template(final template_sel otherValue) {
		super(otherValue);
		checkSingleSelection(otherValue);
	}

	public TitanComponent_template(final int otherValue) {
		super(template_sel.SPECIFIC_VALUE);
		single_value = otherValue;
	}

	public TitanComponent_template(final TitanComponent otherValue) {
		super(template_sel.SPECIFIC_VALUE);

		if (otherValue.componentValue == TitanComponent.UNBOUND_COMPREF) {
			throw new TtcnError("Creating a template from an unbound component reference.");
		}

		single_value = otherValue.componentValue;
	}

	public TitanComponent_template(final TitanComponent_template otherValue) {
		copyTemplate(otherValue);
	}

	//originally clean_up
	public void cleanUp() {
		switch (templateSelection) {
		case VALUE_LIST:
		case COMPLEMENTED_LIST:
			value_list.clear();
			value_list = null;
			break;
		default:
			break;
		}
		templateSelection = template_sel.UNINITIALIZED_TEMPLATE;
	}

	//originally copy_template
	private void copyTemplate(final TitanComponent_template otherValue) {
		switch (otherValue.templateSelection) {
		case SPECIFIC_VALUE:
			single_value = otherValue.single_value;
			break;
		case OMIT_VALUE:
		case ANY_VALUE:
		case ANY_OR_OMIT:
			break;
		case VALUE_LIST:
		case COMPLEMENTED_LIST:
			value_list = new ArrayList<TitanComponent_template>(otherValue.value_list.size());
			for(int i = 0; i < otherValue.value_list.size(); i++) {
				final TitanComponent_template temp = new TitanComponent_template(otherValue.value_list.get(i));
				value_list.add(temp);
			}
			break;
		default:
			throw new TtcnError("Copying an uninitialized/unsupported component reference template.");
		}

		setSelection(otherValue);
	}

	// originally match
	public TitanBoolean match(final TitanComponent otherValue) {
		return match(otherValue, false);
	}

	// originally match
	public TitanBoolean match(final TitanComponent otherValue, final boolean legacy) {
		if (otherValue.componentValue == TitanComponent.UNBOUND_COMPREF) {
			throw new TtcnError("Matching an unbound component reference with a template.");
		}

		return match(otherValue.componentValue, legacy);
	}

	// originally match
	public TitanBoolean match(final int otherValue, final boolean legacy) {
		switch (templateSelection) {
		case SPECIFIC_VALUE:
			return new TitanBoolean(single_value == otherValue);
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
		default:
			throw new TtcnError("Matching with an uninitialized/unsupported component reference template.");
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
