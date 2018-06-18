/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.common.utils;

import java.io.IOException;

import org.eclipse.titan.common.logging.ErrorReporter;

/**
 * This class is able to join strings, with the specified delimiter
 *
 * (Something like com.google.common.base.Joiner, but with limited functionality)
 *
 */
public final class Joiner {

	private static final String CANNOT_HAPPEN = "Cannot happen";

	private final Appender appender;
	private final  StringBuilder builder = new StringBuilder();

	public Joiner() {
		this("");
	}

	public Joiner(final String delimiter) {
		appender = new Appender(builder, delimiter);
	}

	public Joiner join(final Object item) {
		try {
			appender.append(item);
		} catch (IOException e) {
			ErrorReporter.logExceptionStackTrace(CANNOT_HAPPEN, e);
		}
		return this;
	}

	public Joiner join(final Iterable<?> items) {
		try {
			appender.append(items);
		} catch (IOException e) {
			ErrorReporter.logExceptionStackTrace(CANNOT_HAPPEN, e);
		}
		return this;
	}

	public Appendable appendTo(final Appendable appendable) throws IOException {
		return appendable.append(builder.toString());
	}

	public StringBuilder appendTo(final StringBuilder appendable) {
		return appendable.append(builder.toString());
	}

	@Override
	public String toString() {
		return builder.toString();
	}

}
