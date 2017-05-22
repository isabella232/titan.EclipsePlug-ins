package org.eclipse.titan.runtime.core;

/**
 * Helper calss for doubles, when we they need to be updated inside functions.
 *
 * @author Kristof Szabados
 * */
class Changeable_Double {
	private double value;

	public Changeable_Double(final double value) {
		this.value = value;
	}

	public double getValue() {
		return value;
	}

	public void setValue(final double value) {
		this.value = value;
	}
}
