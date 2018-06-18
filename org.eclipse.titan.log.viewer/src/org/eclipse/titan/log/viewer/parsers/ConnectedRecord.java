/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.parsers;

/**
 * This class defines the offset and length for an event
 *
 */
public class ConnectedRecord {
	private long recordOffset;
	private int recordLength;
	private int recordNumber;

	/**
	 * Protected constructor
	 */
	public ConnectedRecord(final long recordOffset, final int recordLength, final int recordNumber) {
		this.recordOffset = recordOffset;
		this.recordLength = recordLength;
		this.recordNumber = recordNumber;
	}
	public int getRecordLength() {
		return this.recordLength;
	}
	public void setRecordLength(final int recordLength) {
		this.recordLength = recordLength;
	}
	public int getRecordNumber() {
		return this.recordNumber;
	}
	public void setRecordNumber(final int recordNumber) {
		this.recordNumber = recordNumber;
	}
	public long getRecordOffset() {
		return this.recordOffset;
	}
	public void setRecordOffset(final long recordOffset) {
		this.recordOffset = recordOffset;
	}

}
