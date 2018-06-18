/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.parsers;

/**
 * Parse a log file in parallel execution mode with file mask n
 */
public class MessageAnalyserParallel1 extends MessageAnalyserFormat1 {

	@Override
	protected boolean isSystemCreation() {
		return this.message.contains(HOST_CONTROLLER_STARTED);
	}

	@Override
	protected boolean isSystemTermination() {
		return this.message.contains(HOST_CONTROLLER_FINISHED);
	}

	@Override
	public String getType() {
		return "MessageAnalyserParallel1"; //$NON-NLS-1$
	}
}

