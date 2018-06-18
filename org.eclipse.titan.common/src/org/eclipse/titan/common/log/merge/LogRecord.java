/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.common.log.merge;

final class LogRecord {
	private final String timestamp;
	private final String text;

	public LogRecord(final String timestamp, final String text) {
		this.timestamp = timestamp;
		this.text = text.substring(timestamp.length());
	}

	public String getTimestamp() {
		return timestamp;
	}

	public String getText() {
		return text;
	}
}
