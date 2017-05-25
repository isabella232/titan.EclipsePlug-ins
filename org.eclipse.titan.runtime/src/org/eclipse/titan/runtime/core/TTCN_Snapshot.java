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
public class TTCN_Snapshot {
	//FIXME should be private
	public static Selector selector;

	// The last time a snapshot was taken
	private static double alt_begin;

	public static HashMap<SelectableChannel, TitanPort> channelMap = new HashMap<SelectableChannel, TitanPort>();

	public static void initialize() {
		//FIXME initialize FdMap
		//TODO why do we initialize fdmap here?
		try{
			selector = Selector.open();
		} catch (IOException exception) {
			
		}

		alt_begin = timeNow();
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
		//FIXME implement
		long pollTimeout = 0;
		if (blockExecution) {
			//FIXME this is way more complex
			Changeable_Double timerTimeout = new Changeable_Double(0.0);
			boolean isTimerTimeout = TitanTimer.getMinExpiration(timerTimeout);
			if (isTimerTimeout) {
				double blockTime = timerTimeout.getValue() - timeNow();
				pollTimeout = (long)Math.floor(blockTime * 1000);
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
			} else {
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
			Set<SelectionKey> selectedKeys = selector.selectedKeys();
			//call handlers
			for (SelectionKey key : selectedKeys) {
				TitanPort handler = channelMap.get(key.channel());
				handler.Handle_Event(key.channel(), key.isReadable(), key.isWritable());
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
