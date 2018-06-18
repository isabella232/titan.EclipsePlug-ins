/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.executor.views.testexecution;

import org.eclipse.titan.common.utils.Joiner;

/**
 * @author Kristof Szabados
 * */
public final class ExecutedTestcase {
	private final String timestamp;
	private final String testcaseName;
	private final String verdict;
	private final String reason;

	public ExecutedTestcase(final String timestamp, final String testcaseName, final String verdict, final String reason) {
		this.timestamp = timestamp;
		this.testcaseName = testcaseName;
		this.verdict = verdict;
		this.reason = reason;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public String getTestCaseName() {
		return testcaseName;
	}

	public String getVerdict() {
		return verdict;
	}

	public String getReason() {
		return reason;
	}

	@Override
	public String toString() {
		Joiner joiner = new Joiner(" ")
		.join(timestamp)
		.join(testcaseName)
		.join(verdict);
		if (!reason.isEmpty()) {
			joiner.join(reason);
		}
		return joiner.toString();
	}
}
