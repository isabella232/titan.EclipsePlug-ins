package org.eclipse.titan.runtime.core;

import java.text.MessageFormat;

/**
 * TTCN-3 timer
 * 
 * @author Kristof Szabados
 */
public class TitanTimer {
	public static final TitanTimer testcaseTimer = new TitanTimer("<testcase guard timer>");

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

	// originally start
	public void start() {
		if (!hasDefault) {
			throw new TtcnError(MessageFormat.format("Timer {0} does not have default duration. It can only be started with a given duration.",
					timerName));
		}

		start(defaultValue);
	}

	// originally start(double start_val)
	public void start(final double startValue) {
		if (this != testcaseTimer) {
			if (startValue < 0.0) {
				throw new TtcnError(MessageFormat.format("Starting timer {0} with a negative duration ({1}).",
						timerName, startValue));
			}
			if (Double.isNaN(startValue) || Double.isInfinite(startValue)) {
				throw new TtcnError(MessageFormat.format("Starting timer {0} with a non-numeric float value ({1}).",
						timerName, startValue));
			}
			if(isStarted) {
				TtcnError.TtcnWarning(MessageFormat.format("Re-starting timer {0}, which is already active (running or expired).",
						timerName));
				// TODO remove from list
			} else {
				isStarted = true;
			}
			// TODO add to list and logging
		} else {
			if (startValue < 0.0) {
				throw new TtcnError(MessageFormat.format("Using a negative duration ({0}) for the guard timer of the test case.",
						startValue));
			}
			if (Double.isNaN(startValue) || Double.isInfinite(startValue)) {
				throw new TtcnError(MessageFormat.format("Using a non-numeric float value ({0}) for the guard timer of the test case.",
						startValue));
			}

			isStarted = true;
			//TODO logging
		}

		// FIXME TTCN_Snapshot::time_now
		timeStarted = System.currentTimeMillis() / 1000;
		timeExpires = timeStarted + startValue;
	}

	// originally start(const FLOAT& start_val)
	public void start(final TitanFloat startValue) {
		startValue.mustBound(MessageFormat.format("Starting timer {0} with an unbound float value as duration.", timerName));

		start(startValue.getValue());
	}

	// originally read
	public double read() {
		double returnValue;

		if (isStarted) {
			// FIXME TTCN_Snapshot::time_now
			double currentTime = System.currentTimeMillis() / 1000;
			if (currentTime >= timeExpires) {
				returnValue = 0.0;
			} else {
				returnValue = currentTime - timeStarted;
			}
		} else {
			returnValue = 0.0;
		}

		//TODO log
		return returnValue;
	}
}
