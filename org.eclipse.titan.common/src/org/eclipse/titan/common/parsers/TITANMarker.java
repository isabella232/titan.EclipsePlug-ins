/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.common.parsers;

/**
 * @author Kristof Szabados
 * */
public class TITANMarker {
	private String message;
	private int line = -1;
	private int offset = -1;
	private int endOffset = -1;
	private int severity;
	private int priority;

	public TITANMarker(final String message, final int line, final int offset, final int endOffset, final int severity, final int priority) {
		this.message = message;
		this.line = line;
		this.offset = offset;
		this.endOffset = endOffset;
		this.severity = severity;
		this.priority = priority;
	}

	public String getMessage() {
		return message;
	}

	public int getLine() {
		return line;
	}

	public int getOffset() {
		return offset;
	}

	public void setOffset(final int offset) {
		this.offset = offset;
	}

	public int getEndOffset() {
		return endOffset;
	}

	public void setEndOffset(final int endOffset) {
		this.endOffset = endOffset;
	}

	public int getSeverity() {
		return severity;
	}

	public void setSeverity(final int severity) {
		this.severity = severity;
	}

	public int getPriority() {
		return priority;
	}
}
