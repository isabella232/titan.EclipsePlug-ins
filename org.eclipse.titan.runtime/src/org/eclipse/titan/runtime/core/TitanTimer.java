package org.eclipse.titan.runtime.core;

import java.text.MessageFormat;

/**
 * TTCN-3 timer
 * 
 * @author Kristof Szabados
 */
public class TitanTimer {

	private String timerName;
	private boolean hasDefault;
	private boolean isStarted;
	private double defaultValue;
	private double timeStarted;
	private double timeExpires;

	public TitanTimer(final String name) {
		if (name == null) {
			timerName = "<unknown>";
		} else {
			timerName = name;
		}
		hasDefault = false;
		isStarted = false;
	}

	public TitanTimer(final String name, final double defaultValue) {
		if (name == null) {
			throw new TtcnError("Internal Error: Creating a timer with an invalid name.");
		}

		timerName = name;
		setDefaultDuration(defaultValue);
		isStarted = false;
	}

	public TitanTimer(final String name, final TitanFloat defaultValue) {
		if (name == null) {
			throw new TtcnError("Internal Error: Creating a timer with an invalid name.");
		}
		defaultValue.mustBound("Initializing a timer duration with an unbound float value.");

		timerName = name;
		setDefaultDuration(defaultValue);
		isStarted = false;
	}

	// originally set_default_duration
	public void setDefaultDuration(final double defaultValue) {
		if (defaultValue < 0.0) {
			throw new TtcnError(MessageFormat.format("Setting the default duration of timer {0} to a negative float value ({1}).",
					timerName, defaultValue));
		} else if (Double.isInfinite(defaultValue) || Double.isNaN(defaultValue)) {
			throw new TtcnError(
					MessageFormat.format("Setting the default duration of timer {0} to a non-numeric float value ({1}).",
							timerName, defaultValue));
		}

		hasDefault = true;
		this.defaultValue = defaultValue;
	}

	// originally set_default_duration
	public void setDefaultDuration(final TitanFloat defaultValue) {
		defaultValue.mustBound(MessageFormat.format("Setting the default duration of timer {0} to an unbound float value.", timerName));

		setDefaultDuration(defaultValue.getValue());
	}
}
