package org.eclipse.titan.runtime.core;

import java.text.MessageFormat;
import java.util.LinkedList;

/**
 * TTCN-3 timer
 * 
 * TODO: the destructor can be a problem.
 * 
 * @author Kristof Szabados
 */
public class TitanTimer {
	public static final TitanTimer testcaseTimer = new TitanTimer("<testcase guard timer>");

	// linked list of running timers
	private static final LinkedList<TitanTimer> TIMERS = new LinkedList<TitanTimer>();

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

	/**
	 * Add the current timer instance to the end of the running timers list.
	 * */
	private void addToList() {
		if (TIMERS.contains(this)) {
			return;
		}

		TIMERS.addLast(this);
	}

	/**
	 * Remove the current timer from the list of running timers
	 * */
	private void removeFromList() {
		TIMERS.remove(this);
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
				removeFromList();
			} else {
				isStarted = true;
			}
			// TODO logging
			addToList();
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

		timeStarted = TTCN_Snapshot.timeNow();
		timeExpires = timeStarted + startValue;
	}

	// originally start(const FLOAT& start_val)
	public void start(final TitanFloat startValue) {
		startValue.mustBound(MessageFormat.format("Starting timer {0} with an unbound float value as duration.", timerName));

		start(startValue.getValue());
	}

	// originally stop()
	public void stop() {
		if (this != testcaseTimer) {
			if (isStarted) {
				isStarted = false;
				// TODO log
				removeFromList();
			} else {
				TtcnError.TtcnWarning(MessageFormat.format("Stopping inactive timer {0}.", timerName));
			}
		} else {
			isStarted = false;
		}
	}

	/**
	 * originally read
	 * 
	 * @return the number of seconds until the timer expires.
	 * */
	public double read() {
		double returnValue;

		if (isStarted) {
			double currentTime = TTCN_Snapshot.timeNow();
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

	/**
	 * @return true if is_started and not yet expired, false otherwise.
	 *
	 * originally running(Index_Redirect*) 
	 */
	public boolean running() {
		//FIXME handle redirection
		return isStarted && TTCN_Snapshot.timeNow() < timeExpires;
	}

	/**
	 * Return the alt status.
	 *
	 * @return ALT_NO if the timer is not started.
	 * @return ALT_MAYBE if it's started and the snapshot was taken before
	 *         the expiration time
	 * @return ALT_YES if it's started and the snapshot is past the
	 *         expiration time
	 *
	 *         originally timeout(Index_Redirect*)
	 * */
	public TitanAlt_Status timeout() {
		if (isStarted) {
			if (TTCN_Snapshot.getAltBegin() < timeExpires) {
				return TitanAlt_Status.ALT_MAYBE;
			}

			isStarted = false;
			if (this != testcaseTimer) {
				// TODO log
				removeFromList();
			}

			return TitanAlt_Status.ALT_YES;
		} else {
			if (this != testcaseTimer) {
				// TODO log
			}

			return TitanAlt_Status.ALT_NO;
		}
	}
}
