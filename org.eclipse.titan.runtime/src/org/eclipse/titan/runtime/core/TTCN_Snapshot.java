/******************************************************************************
 * Copyright (c) 2000-2017 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.HashMap;
import java.util.Set;

/**
 * Utility class to help working with snapshots
 * 
 * @author Kristof Szabados
 */
public final class TTCN_Snapshot {
	// Right now start from theoretical max, until we discover platform limitations.
	private static final long MAX_BLOCK_TIME = Long.MAX_VALUE;

	//FIXME should be private
	public static Selector selector;

	//[else] branch of alt was reached
	private static boolean else_branch_found;
	// The last time a snapshot was taken
	private static double alt_begin;

	public static HashMap<SelectableChannel, TitanPort> channelMap = new HashMap<SelectableChannel, TitanPort>();

	private TTCN_Snapshot() {
		// private constructor to disable accidental instantiation
	}

	public static void initialize() {
		//FIXME initialize FdMap
		//TODO why do we initialize fdmap here?
		try{
			selector = Selector.open();
		} catch (IOException exception) {
			
		}

		else_branch_found = false;
		alt_begin = timeNow();
	}

	public static void terminate() {
		//TODO implement
	}

	/**
	 * Execution reached an else branch of an alt.
	 * If this is the first time we must warn the user.
	 * */
	public static void elseBranchReached() {
		if (!else_branch_found) {
			else_branch_found = true;
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
	public static double timeNow() {
		return System.currentTimeMillis() / 1000.0;
	}

	/**
	 * Returns the time when the last altstep started.
	 * Should only be used when time is frozen.
	 *
	 * @return the current time as a double, in seconds.miliseconds form
	 *
	 * */
	public static double getAltBegin() {
		return alt_begin;
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
	public static void takeNew(final boolean blockExecution) {
		if (blockExecution || else_branch_found) {
			again: for(;;){
				//FIXME implement
				double timeout = 0.0;
				long pollTimeout = 0;
				boolean handleTimer = false;
				if (blockExecution) {
					//FIXME this is way more complex
					final Changeable_Double timerTimeout = new Changeable_Double(0.0);
					final boolean isTimerTimeout = TitanTimer.getMinExpiration(timerTimeout);
					if (isTimerTimeout) {
						timeout = timerTimeout.getValue();
						double currentTime = timeNow();
						final double blockTime = timeout - currentTime;
						if (blockTime > 0.0) {
							if (blockTime < MAX_BLOCK_TIME) {
								pollTimeout = (long)Math.floor(blockTime * 1000);
								handleTimer = true;
							} else {
								//FIXME log warning
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

				if (selector.keys().isEmpty() && pollTimeout < 0) {
					throw new TtcnError("There are no active timers and no installed event handlers. Execution would block forever.");
				}

				int selectReturn = 0;
				if (selector.keys().isEmpty()) {
					//no channels to wait for
					//TODO this check is not needed
					if (pollTimeout > 0) {
						try {
							selectReturn = selector.select(pollTimeout);
						} catch (IOException exception) {
							throw new TtcnError("Interrupted while taking snapshot.");
						}
					} else if(pollTimeout < 0) {
						throw new TtcnError("There are no active timers and no installed event handlers. Execution would block forever.");
					}
				} else {
					if (pollTimeout > 0) {
						try {
							selectReturn = selector.select(pollTimeout);
						} catch (IOException exception) {
							throw new TtcnError("Interrupted while taking snapshot.");
						}
					} else {
						try {
							selectReturn = selector.selectNow();
						} catch (IOException exception) {
							throw new TtcnError("Interrupted while taking snapshot.");
						}
					}
				}

				if (selectReturn > 0 ){
					final Set<SelectionKey> selectedKeys = selector.selectedKeys();
					//call handlers
					for (final SelectionKey key : selectedKeys) {
						final TitanPort handler = channelMap.get(key.channel());
						handler.Handle_Event(key.channel(), key.isReadable(), key.isWritable());
					}
				} else if (selectReturn == 0 && handleTimer) {
					// if select() returned because of the timeout, but too early
					// do an other round if it has to wait much,
					// or do a busy wait if only a few cycles are needed
					if (pollTimeout > 0) {
						double difference = timeNow() - timeout;
						if (difference < 0) {
							continue again;
						}
					}

					final Set<SelectionKey> selectedKeys = selector.selectedKeys();
					//call handlers
					for (final SelectionKey key : selectedKeys) {
						final TitanPort handler = channelMap.get(key.channel());
						handler.Handle_Event(key.channel(), key.isReadable(), key.isWritable());
					}
				}

				//leave the for loop
				break;
			}
		}
		// just update the time and check the testcase guard timer if blocking was
		// not requested and there is no [else] branch in the test suite
		alt_begin = timeNow();

		if (TitanAlt_Status.ALT_YES == TitanTimer.testcaseTimer.timeout()) {
			throw new TtcnError("Guard timer has expired. Execution of current test case will be interrupted.");
		}
	}
}
