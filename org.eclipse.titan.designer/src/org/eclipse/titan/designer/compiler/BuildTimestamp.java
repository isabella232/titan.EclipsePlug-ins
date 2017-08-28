/******************************************************************************
 * Copyright (c) 2000-2017 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.compiler;


/**
 * @author Kristof Szabados
 * */
public final class BuildTimestamp {
	/** static counter of timestamps.
	 * Ensures that no 2 are handed out at the same time */
	private static long buildCounter = 0;

	/** the base timestamp object has the least likelihood to trigger analysis,
	 * as all already analysed node in the AST were analyzed later. */
	private static final BuildTimestamp BASE_TIMESTAMP = new BuildTimestamp(0);

	/** The actual info on the current timestamp */
	private final long internalBuildTimestamp;

	private BuildTimestamp(final long buildCounter) {
		internalBuildTimestamp = buildCounter;
	}

	/**
	 * Returns a new compilation counter, which can be used to provide a
	 * clear baseline between different semantic checks of the same module.
	 * <p>
	 * It is expected that the time difference between returning the same
	 * value twice will be huge enough, to be assumed that this function
	 * always returns a unique value.
	 * <p>
	 * The values returned are always positive..
	 * 
	 * @return a new compilationCounter.
	 * */
	public static BuildTimestamp getNewBuildCounter() {
		buildCounter++;
		if (buildCounter == Long.MAX_VALUE) {
			buildCounter = 0;
		}
		return new BuildTimestamp(buildCounter);
	}

	/**
	 * Returns true if this timestamp is smaller / earlier that the one
	 * provided as parameter.
	 * 
	 * @param other
	 *                the other timestamp to compare to.
	 * @return true if the provided timestamp is newer that the actual.
	 * */
	public boolean isLess(final BuildTimestamp other) {
		return internalBuildTimestamp < other.internalBuildTimestamp;
	}
}
