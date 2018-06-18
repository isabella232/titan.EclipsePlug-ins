/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.parsers;

/**
 * Analysis the message for a single mode file with file format 2
 */
public class MessageAnalyserSingle2 extends MessageAnalyserFormat2 {

	@Override
	public String getType() {
		return "MessageAnalyserSingle2"; //$NON-NLS-1$
	}

	@Override
	protected boolean isSystemCreation() {
		return this.message.contains(ETS_STARTUP);
	}

	@Override
	protected boolean isSystemTermination() {
		return this.message.contains(ETS_TERMINATION);
	}
}

