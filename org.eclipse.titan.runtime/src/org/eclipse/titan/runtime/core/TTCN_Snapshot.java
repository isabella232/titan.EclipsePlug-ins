package org.eclipse.titan.runtime.core;

/**
 * Utility class to help working with snapshots
 * 
 * @author Kristof Szabados
 */
public class TTCN_Snapshot {

	// The last time a snapshot was taken
	private static double alt_begin;

	public static void initialize() {
		//FIXME initialize FdMap
		//TODO why do we initialize fdmap here?
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

	public static void takeNew(final boolean blockExecution) {
		//FIXME implement

		if (blockExecution) {
			//FIXME this is way more complex
			Changeable_Double timerTimeout = new Changeable_Double(0.0);
			boolean isTimerTimeout = TitanTimer.getMinExpiration(timerTimeout);
			if (isTimerTimeout) {
				double blockTime = timerTimeout.getValue() - timeNow();
				try {
					Thread.sleep((long)Math.floor(blockTime * 1000));
				} catch (InterruptedException exception) {
					throw new TtcnError("Interrupted while taking snapshot.");
				}
			}
		}

		// just update the time and check the testcase guard timer if blocking was
		// not requested and there is no [else] branch in the test suite
		alt_begin = timeNow();

		//FIXME implement
	}
}
