/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

import java.text.MessageFormat;
import java.util.LinkedList;

/**
 * TTCN-3 timer
 *
 * TODO: the destructor can be a problem.
 *
 * @author Kristof Szabados
 * @author Farkas Izabella Ingrid
 */
public class TitanTimer {
	public static final TitanTimer testcaseTimer = new TitanTimer("<testcase guard timer>");

	// linked list of running timers
	private static final ThreadLocal<LinkedList<TitanTimer>> RUNNING_TIMERS = new ThreadLocal<LinkedList<TitanTimer>>() {
		@Override
		protected LinkedList<TitanTimer> initialValue() {
			return new LinkedList<TitanTimer>();
		}
	};
	private static final ThreadLocal<LinkedList<TitanTimer>> BACKUP_TIMERS = new ThreadLocal<LinkedList<TitanTimer>>() {
		@Override
		protected LinkedList<TitanTimer> initialValue() {
			return new LinkedList<TitanTimer>();
		}
	};

	private String timer_name;
	private boolean has_default;
	private boolean is_started;
	private double default_value;
	private double time_started;
	private double time_expires;
	private static boolean control_timer_saved = false;

	protected TitanTimer(){
		//intentionally left empty
	}

	/**
	 * Assigns the other value to this value.
	 * Overwriting the current content in the process.
	 *<p>
	 * operator= in the core.
	 *
	 * @param otherValue
	 *                the other value to assign.
	 * @return the new value object.
	 */
	TitanTimer assign(final TitanTimer otherValue) {
		timer_name = otherValue.timer_name;
		has_default = otherValue.has_default;
		is_started = otherValue.is_started;
		default_value = otherValue.default_value;
		time_started = otherValue.time_started;
		time_expires = otherValue.time_expires;

		return this;
	}

	/**
	 * Assigns the other value to this value.
	 * Overwriting the current content in the process.
	 *<p>
	 * operator= in the core.
	 *
	 * @param otherValue
	 *                the other value to assign.
	 * @return the new value object.
	 */
	public TitanTimer assign(final Ttcn3Float defaultValue) {
		set_default_duration(defaultValue);
		is_started = false;

		return this;
	}

	/**
	 * Assigns the other value to this value.
	 * Overwriting the current content in the process.
	 *<p>
	 * operator= in the core.
	 *
	 * @param otherValue
	 *                the other value to assign.
	 * @return the new value object.
	 */
	public TitanTimer assign(final TitanFloat defaultValue) {
		defaultValue.mustBound("Initializing a timer duration with an unbound float value.");

		set_default_duration(defaultValue);
		is_started = false;

		return this;
	}

	public TitanTimer(final String name) {
		if (name == null) {
			timer_name = "<unknown>";
		} else {
			timer_name = name;
		}
		has_default = false;
		is_started = false;
	}

	public TitanTimer(final String name, final double defaultValue) {
		if (name == null) {
			throw new TtcnError("Internal Error: Creating a timer with an invalid name.");
		}

		timer_name = name;
		set_default_duration(defaultValue);
		is_started = false;
	}

	public TitanTimer(final String name, final Ttcn3Float defaultValue) {
		if (name == null) {
			throw new TtcnError("Internal Error: Creating a timer with an invalid name.");
		}

		timer_name = name;
		set_default_duration(defaultValue);
		is_started = false;
	}

	public TitanTimer(final String name, final TitanFloat defaultValue) {
		if (name == null) {
			throw new TtcnError("Internal Error: Creating a timer with an invalid name.");
		}
		defaultValue.mustBound("Initializing a timer duration with an unbound float value.");

		timer_name = name;
		set_default_duration(defaultValue);
		is_started = false;
	}

	/**
	 * Add the current timer instance to the end of the running timers list.
	 * */
	private void add_to_list() {
		final LinkedList<TitanTimer> localTimers = RUNNING_TIMERS.get();
		if (localTimers.contains(this)) {
			return;
		}

		localTimers.addLast(this);
	}

	/**
	 * Remove the current timer from the list of running timers
	 * */
	private void remove_from_list() {
		RUNNING_TIMERS.get().remove(this);
	}

	public void set_name(final String name) {
		if (name == null) {
			throw new TtcnError("Internal error: Setting an invalid name for a single element of a timer array.");
		}
		timer_name = name;
	}

	public final void set_default_duration(final double defaultValue) {
		if (defaultValue < 0.0) {
			throw new TtcnError(MessageFormat.format("Setting the default duration of timer {0} to a negative float value ({1}).",
					timer_name, defaultValue));
		} else if (Double.isInfinite(defaultValue) || Double.isNaN(defaultValue)) {
			throw new TtcnError(
					MessageFormat.format("Setting the default duration of timer {0} to a non-numeric float value ({1}).",
							timer_name, defaultValue));
		}

		has_default = true;
		this.default_value = defaultValue;
	}

	// originally set_default_duration
	public final void set_default_duration(final Ttcn3Float defaultValue) {
		set_default_duration(defaultValue.getValue());
	}

	// originally set_default_duration
	public final void set_default_duration(final TitanFloat defaultValue) {
		defaultValue.mustBound(MessageFormat.format("Setting the default duration of timer {0} to an unbound float value.", timer_name));

		set_default_duration(defaultValue.getValue());
	}

	// originally start
	public void start() {
		if (!has_default) {
			throw new TtcnError(MessageFormat.format("Timer {0} does not have default duration. It can only be started with a given duration.",
					timer_name));
		}

		start(default_value);
	}

	// originally start(double start_val)
	public void start(final double startValue) {
		if (this != testcaseTimer) {
			if (startValue < 0.0) {
				throw new TtcnError(MessageFormat.format("Starting timer {0} with a negative duration ({1}).",
						timer_name, startValue));
			}
			if (Double.isNaN(startValue) || Double.isInfinite(startValue)) {
				throw new TtcnError(MessageFormat.format("Starting timer {0} with a non-numeric float value ({1}).",
						timer_name, startValue));
			}
			if (is_started) {
				TtcnError.TtcnWarning(MessageFormat.format("Re-starting timer {0}, which is already active (running or expired).",
						timer_name));
				remove_from_list();
			} else {
				is_started = true;
			}

			TTCN_Logger.log_timer_start(timer_name, startValue);
			add_to_list();
		} else {
			if (startValue < 0.0) {
				throw new TtcnError(MessageFormat.format("Using a negative duration ({0}) for the guard timer of the test case.",
						startValue));
			}
			if (Double.isNaN(startValue) || Double.isInfinite(startValue)) {
				throw new TtcnError(MessageFormat.format("Using a non-numeric float value ({0}) for the guard timer of the test case.",
						startValue));
			}

			is_started = true;
			TTCN_Logger.log_timer_guard(startValue);
		}

		time_started = TTCN_Snapshot.timeNow();
		time_expires = time_started + startValue;
	}

	// originally start(const FLOAT& start_val)
	public void start(final Ttcn3Float startValue) {
		start(startValue.getValue());
	}

	// originally start(const FLOAT& start_val)
	public void start(final TitanFloat startValue) {
		startValue.mustBound(MessageFormat.format("Starting timer {0} with an unbound float value as duration.", timer_name));

		start(startValue.getValue());
	}

	// originally stop()
	public void stop() {
		if (this != testcaseTimer) {
			if (is_started) {
				is_started = false;
				TTCN_Logger.log_timer_stop(timer_name, time_expires - time_started);
				remove_from_list();
			} else {
				TtcnError.TtcnWarning(MessageFormat.format("Stopping inactive timer {0}.", timer_name));
			}
		} else {
			is_started = false;
		}
	}

	/**
	 * originally read
	 *
	 * @return the number of seconds until the timer expires.
	 * */
	public TitanFloat read() {
		double returnValue;

		if (is_started) {
			final double currentTime = TTCN_Snapshot.timeNow();
			if (currentTime >= time_expires) {
				returnValue = 0.0;
			} else {
				returnValue = currentTime - time_started;
			}
		} else {
			returnValue = 0.0;
		}

		TTCN_Logger.log_timer_read(timer_name, returnValue);

		return new TitanFloat(returnValue);
	}

	/**
	 * @return {@code true} if is_started and not yet expired, {@code false} otherwise.
	 *
	 * originally running(Index_Redirect* = null)
	 */
	public boolean running() {
		return running(null);
	}

	/**
	 * @return {@code true} if is_started and not yet expired, {@code false} otherwise.
	 *
	 * originally running(Index_Redirect*)
	 */
	public boolean running(final Index_Redirect index_redirect) {
		return is_started && TTCN_Snapshot.timeNow() < time_expires;
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
	 *         originally timeout(Index_Redirect* = null)
	 * */
	public TitanAlt_Status timeout() {
		return timeout(null);
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
	public TitanAlt_Status timeout(final Index_Redirect index_redirect) {
		if (is_started) {
			if (TTCN_Snapshot.getAltBegin() < time_expires) {
				return TitanAlt_Status.ALT_MAYBE;
			}

			is_started = false;
			if (this != testcaseTimer) {
				TTCN_Logger.log_timer_timeout(timer_name, time_expires - time_started);
				remove_from_list();
			}

			return TitanAlt_Status.ALT_YES;
		} else {
			if (this != testcaseTimer) {
				TTCN_Logger.log_matching_timeout(timer_name);
			}

			return TitanAlt_Status.ALT_NO;
		}
	}

	/**
	 * stop all running timers.
	 * (empty the list)
	 * */
	public static void all_stop() {
		final LinkedList<TitanTimer> localTimers = RUNNING_TIMERS.get();
		while (!localTimers.isEmpty()) {
			localTimers.get(0).stop();
		}
	}

	/**
	 * @return {@code true} if there is a running timer.
	 * */
	public static boolean any_running() {
		for (final TitanTimer timer : RUNNING_TIMERS.get()) {
			if (timer.running(null)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Return the alt status.
	 *
	 * @return ALT_NO if no timer is running.
	 * @return ALT_MAYBE if there is at least one timer that is started
	 *         and the snapshot was taken before it's expiration time
	 * @return ALT_YES if there is at least one time that is started
	 *         and the snapshot is past it's expiration time
	 * */
	public static TitanAlt_Status any_timeout() {
		TitanAlt_Status returnValue = TitanAlt_Status.ALT_NO;
		for (final TitanTimer timer : RUNNING_TIMERS.get()) {
			switch (timer.timeout(null)) {
			case ALT_YES:
				TTCN_Logger.log_timer_any_timeout();
				return TitanAlt_Status.ALT_YES;
			case ALT_MAYBE:
				returnValue = TitanAlt_Status.ALT_MAYBE;
				break;
			default:
				throw new TtcnError(MessageFormat.format("Internal error: Timer {0} returned unexpected status code while evaluating `any timer.timeout'.",
						timer.timer_name));
			}
		}

		if (returnValue == TitanAlt_Status.ALT_NO) {
			TTCN_Logger.log_matching_timeout(null);
		}

		return returnValue;
	}

	/**
	 * Get the earliest expiration time for the running timers.
	 * Includes the testcase's guard timer.
	 *
	 * @param minValue will return the expiration time if one is found.
	 * @return {@code true} if an active timer was found, {@code false} otherwise.
	 * */
	public static boolean get_min_expiration(final Changeable_Double minValue) {
		boolean minFlag = false;
		final double altBegin = TTCN_Snapshot.getAltBegin();

		if (testcaseTimer.is_started && testcaseTimer.time_expires > altBegin) {
			minValue.setValue(testcaseTimer.time_expires);
			minFlag = true;
		}

		for (final TitanTimer timer : RUNNING_TIMERS.get()) {
			if (timer.time_expires < altBegin) {
				//ignore timers that expired before the snapshot
				continue;
			} else if (!minFlag || timer.time_expires < minValue.getValue()){
				minValue.setValue(timer.time_expires);
				minFlag = true;
			}
		}


		return minFlag;
	}

	public static void save_control_timers() {
		if (control_timer_saved) {
			throw new TtcnError("Internal error: Control part timers are already saved.");
		}

		final LinkedList<TitanTimer> localTimers = RUNNING_TIMERS.get();
		if (!localTimers.isEmpty()) {
			BACKUP_TIMERS.get().addAll(localTimers);
			localTimers.clear();
		}
		control_timer_saved = true;
	}

	public static void restore_control_timers() {
		if (!control_timer_saved) {
			throw new TtcnError("Internal error: Control part timers are not saved.");
		}

		if (!RUNNING_TIMERS.get().isEmpty()) {
			throw new TtcnError("Internal error: There are active timers. Control part timers cannot be restored.");
		}

		final LinkedList<TitanTimer> localBackupTimers = BACKUP_TIMERS.get();
		if (!localBackupTimers.isEmpty()) {
			RUNNING_TIMERS.get().addAll(localBackupTimers);
			localBackupTimers.clear();
		}
		control_timer_saved = false;
	}

	/**
	 * Logs this timer.
	 */
	public void log() {
		// the time is not frozen (i.e. time_now() is used)
		TTCN_Logger.log_event("timer: { name: " + timer_name + ", default duration: ");
		if (has_default) {
			TTCN_Logger.log_event(default_value + " s");
		} else {
			TTCN_Logger.log_event_str("none");
		}
		TTCN_Logger.log_event_str(", state: ");
		if (is_started) {
			final double current_time = TTCN_Snapshot.timeNow();
			if (current_time < time_expires) {
				TTCN_Logger.log_event_str("running");
			} else {
				TTCN_Logger.log_event_str("expired");
			}
			TTCN_Logger.log_event(", actual duration: " + (time_expires - time_started) + " s,elapsed time: " + (current_time - time_started)
					+ " s");
		} else {
			TTCN_Logger.log_event_str("inactive");
		}
		TTCN_Logger.log_event_str(" }");
	}
}
