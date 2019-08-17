/******************************************************************************
 * Copyright (c) 2000-2019 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import org.eclipse.titan.runtime.core.Event_Handler.Channel_And_Timeout_Event_Handler;
import org.eclipse.titan.runtime.core.Event_Handler.Channel_Event_Handler;

/**
 * Utility class to help working with snapshots
 *
 * @author Kristof Szabados
 */
public final class TTCN_Snapshot {
	// Right now start from theoretical max, until we discover platform limitations.
	private static final long MAX_BLOCK_TIME = Long.MAX_VALUE;

	//FIXME should be private
	public static ThreadLocal<Selector> selector = new ThreadLocal<Selector>() {
		@Override
		protected Selector initialValue() {
			return null;
		}
	};

	//[else] branch of alt was reached
	private static ThreadLocal<Boolean> else_branch_found = new ThreadLocal<Boolean>() {
		@Override
		protected Boolean initialValue() {
			return false;
		}
	};
	// The last time a snapshot was taken
	private static ThreadLocal<Double> alt_begin = new ThreadLocal<Double>() {
		@Override
		protected Double initialValue() {
			return time_now();
		}
	};

	public static ThreadLocal<HashMap<SelectableChannel, Channel_Event_Handler>> channelMap = new ThreadLocal<HashMap<SelectableChannel, Channel_Event_Handler>>() {
		@Override
		protected HashMap<SelectableChannel, Channel_Event_Handler> initialValue() {
			return new HashMap<SelectableChannel, Channel_Event_Handler>();
		}

	};

	private static ThreadLocal<ArrayList<Channel_And_Timeout_Event_Handler>> timedList = new ThreadLocal<ArrayList<Channel_And_Timeout_Event_Handler>>() {

		@Override
		protected ArrayList<Channel_And_Timeout_Event_Handler> initialValue() {
			return new ArrayList<Event_Handler.Channel_And_Timeout_Event_Handler>();
		}

	};

	private TTCN_Snapshot() {
		// private constructor to disable accidental instantiation
	}

	public static void initialize() {
		//FIXME initialize FdMap
		//TODO why do we initialize fdmap here?
		try {
			selector.set(Selector.open());
		} catch (IOException exception) {
			throw new TtcnError(exception);
		}

		else_branch_found.set(false);
		alt_begin.set(time_now());
	}

	// originially reopenEpollFd and in a bit different location
	public static void re_open() {
		try {
			selector.set(Selector.open());
		} catch (IOException exception) {
			throw new TtcnError(exception);
		}
	}

	public static void terminate() {
		//TODO implement
	}

	/**
	 * Execution reached an else branch of an alt.
	 * If this is the first time we must warn the user.
	 * */
	public static void else_branch_reached() {
		if (!else_branch_found.get()) {
			else_branch_found.set(true);
			TtcnError.TtcnWarning("An [else] branch of an alt construct has been reached."
					+ "Re-configuring the snapshot manager to call the event handlers even when taking the first snapshot.");
		}
	}

	/**
	 * Returns the current time.
	 * Should be used only when time is flowing normally.
	 *
	 * @return the current time as a double, in seconds.miliseconds form
	 *
	 * */
	public static double time_now() {
		return System.currentTimeMillis() / 1000.0;
	}

	/**
	 * Returns the time when the last altstep started.
	 * Should only be used when time is frozen.
	 *
	 * @return the current time as a double, in seconds.miliseconds form
	 *
	 * */
	public static double get_alt_begin() {
		return alt_begin.get();
	}

	/**
	 * Take a new snapshot:
	 * - if in blocking mode and no data arriving through ports,
	 *    sleep will be called to the nearest timer timeout point in time.
	 * Otherwise checks the timer and ports for data available for processing.
	 * Saves the actual time as the last time a snapshot was taken into alt_begin.
	 *
	 * @param blockExecution true if the function should block execution if there is nothing to process and timers need some time to timeout.
	 *
	 * originally take_new
	 * */
	public static void take_new(final boolean blockExecution) {
		if (blockExecution || else_branch_found.get()) {
			again: for (;;) {
				//FIXME implement
				double timeout = 0.0;
				long pollTimeout = 0;
				boolean handleTimer = false;
				if (blockExecution) {
					final Changeable_Double timerTimeout = new Changeable_Double(0.0);
					final boolean isTimerTimeout = TitanTimer.get_min_expiration(timerTimeout);
					//FIXME timeout handle still needed
					if (isTimerTimeout) {
						timeout = timerTimeout.getValue();
						final double currentTime = time_now();
						final double blockTime = timeout - currentTime;
						if (blockTime > 0.0) {
							if (blockTime < MAX_BLOCK_TIME) {
								pollTimeout = (long)Math.floor(blockTime * 1000);
								handleTimer = true;
							} else {
								TtcnError.TtcnWarning(MessageFormat.format("The time needed for the first timer expiry is {0} seconds. The operating system does not support such long waiting at once. The maximum time of blocking was set to {1} seconds (ca. {2} days).", blockTime, MAX_BLOCK_TIME, MAX_BLOCK_TIME / 86400));
								timeout = currentTime + MAX_BLOCK_TIME;
								pollTimeout = MAX_BLOCK_TIME * 1000;
								handleTimer = true;
							}
						} else {
							// first timer already expired: do not block
							handleTimer = true;
						}
					} else {
						// no active timers: infinite timeout
						pollTimeout = -1;
					}
				}

				final Selector localSelector = selector.get();
				int selectReturn = 0;
				if (localSelector.keys().isEmpty()) {
					//no channels to wait for
					if (pollTimeout > 0) {
						try {
							selectReturn = localSelector.select(pollTimeout);
						} catch (IOException exception) {
							throw new TtcnError("Interrupted while taking snapshot.");
						}
					} else if (pollTimeout < 0) {
						throw new TtcnError("There are no active timers and no installed event handlers. Execution would block forever.");
					}
				} else {
					if (pollTimeout > 0) {
						try {
							selectReturn = localSelector.select(pollTimeout);
						} catch (IOException exception) {
							throw new TtcnError("Interrupted while taking snapshot.");
						}
					} else if (pollTimeout < 0) {
						try {
							selectReturn = localSelector.select(0);
						} catch (IOException exception) {
							throw new TtcnError("Interrupted while taking snapshot.");
						}
					} else {
						try {
							selectReturn = localSelector.selectNow();
						} catch (IOException exception) {
							throw new TtcnError("Interrupted while taking snapshot.");
						}
					}
				}

				if (selectReturn > 0) {
					final Set<SelectionKey> selectedKeys = localSelector.selectedKeys();
					//call handlers
					try {
						final HashMap<SelectableChannel, Channel_Event_Handler> localChannelMap = channelMap.get();
						for (final SelectionKey key : selectedKeys) {
							final SelectableChannel keyChannel = key.channel();
							final Channel_Event_Handler handler = localChannelMap.get(keyChannel);
							final int readyOps = key.readyOps();
							final boolean isReadable = key.isValid() && ((readyOps & (SelectionKey.OP_READ | SelectionKey.OP_ACCEPT)) != 0);
							final boolean isWritable = key.isValid() && ((readyOps & SelectionKey.OP_WRITE) != 0);
							if (handler != null) {
								handler.Handle_Event(keyChannel, isReadable, isWritable);
							}
						}
					} finally {
						selectedKeys.clear();
					}
					//TODO handle timeout
				} else if (selectReturn == 0 && handleTimer) {
					// if select() returned because of the timeout, but too early
					// do an other round if it has to wait much,
					// or do a busy wait if only a few cycles are needed
					if (pollTimeout > 0) {
						final double difference = time_now() - timeout;
						if (difference < 0) {
							continue again;
						}
					}

					final Set<SelectionKey> selectedKeys = localSelector.selectedKeys();
					//call handlers
					try {
						final HashMap<SelectableChannel, Channel_Event_Handler> localChannelMap = channelMap.get();
						for (final SelectionKey key : selectedKeys) {
							final SelectableChannel keyChannel = key.channel();
							final Channel_Event_Handler handler = localChannelMap.get(keyChannel);
							final int readyOps = key.readyOps();
							final boolean isReadable = key.isValid() && ((readyOps & (SelectionKey.OP_READ | SelectionKey.OP_ACCEPT)) != 0);
							final boolean isWritable = key.isValid() && ((readyOps & SelectionKey.OP_WRITE) != 0);
							if (handler != null) {
								handler.Handle_Event(keyChannel, isReadable, isWritable);
							}
						}
					} finally {
						selectedKeys.clear();
					}
				}

				//leave the for loop
				break;
			}
		}
		// just update the time and check the testcase guard timer if blocking was
		// not requested and there is no [else] branch in the test suite
		alt_begin.set(time_now());

		if (TitanAlt_Status.ALT_YES == TitanTimer.testcaseTimer.timeout()) {
			throw new TtcnError("Guard timer has expired. Execution of current test case will be interrupted.");
		}
	}

	public static void set_timer(final Channel_And_Timeout_Event_Handler handler, final double call_interval, final boolean is_timeout, final boolean call_anyway, final boolean is_periodic) {
		if (call_interval == 0.0) {
			if (handler.list == timedList.get()) {
				timedList.get().remove(handler);
				handler.list = null;
			}

			handler.callIntervall = 0.0;
		} else {
			if (handler.list == null) {
				timedList.get().add(handler);
				handler.list = timedList.get();
			}

			handler.callIntervall = call_interval;
			handler.last_called = time_now();
			handler.isTimeout = is_timeout;
			handler.callAnyway = call_anyway;
			handler.isPeriodic = is_periodic;
		}
	}
}
