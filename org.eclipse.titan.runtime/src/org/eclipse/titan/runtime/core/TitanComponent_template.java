package org.eclipse.titan.runtime.core;

/**
 * TTCN-3 component template
 * @author Kristof Szabados
 */
public class TitanComponent_template extends Base_Template {
	public static final TitanComponent_template any_compref = new TitanComponent_template(template_sel.ANY_VALUE);

	public TitanComponent_template() {
		//intentionally empty
	}

	public TitanComponent_template(final template_sel otherValue) {
		super(otherValue);
		checkSingleSelection(otherValue);
	}

	// originally match
	public TitanBoolean match(final TitanComponent otherValue) {
		return match(otherValue, false);
	}

	// originally match
	public TitanBoolean match(final TitanComponent otherValue, final boolean legacy) {
		//FIXME not yet implemented
		return new TitanBoolean(false);
	}

	// originally match
	public TitanBoolean match(final int otherValue, final boolean legacy) {
		//FIXME not yet implemented
		return new TitanBoolean(false);
	}
}
