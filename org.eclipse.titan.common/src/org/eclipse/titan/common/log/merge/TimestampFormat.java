/******************************************************************************
 * Copyright (c) 2000-2021 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.common.log.merge;

import java.util.regex.Pattern;

/**
 * This enumeration represents the time stamp formats possible ni log files.
 * This way they can be referenced with name.
 * */
enum TimestampFormat {
	DATETIME_FORMAT("DateTime", "\\d\\d\\d\\d/\\w*/\\d\\d \\d\\d:\\d\\d:\\d\\d\\.\\d\\d\\d\\d\\d\\d", 27),
	TIME_FORMAT("Time", "\\d\\d:\\d\\d:\\d\\d\\.\\d\\d\\d\\d\\d\\d", 15),
	SECOND_FORMAT("Second", "\\d\\.\\d\\d\\d\\d\\d\\d", 8);

	private final String formatName;
	private final Pattern timestampPattern;
	private final int formatSize;

	TimestampFormat(final String formatName, final String format, final int formatSize) {
		this.formatName = formatName;
		this.timestampPattern = Pattern.compile(format);
		this.formatSize = formatSize;
	}

	/**
	 * @return the name of this timestamp format.
	 * */
	public String getFormatName() {
		return formatName;
	}

	/**
	 * @return the pattern of this timestamp format.
	 * */
	public Pattern getPattern() {
		return timestampPattern;
	}

	/**
	 * @return the size of this timestamp format in characters.
	 * */
	public int getFormatSize() {
		return formatSize;
	}
}
