/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core.cfgparser;

/**
 * @author Kristof Szabados
 * @author Arpad Lovassy
 */
public final class ExternalCommandSectionHandler {

	private String beginControlPart = null;
	private String endControlPart = null;
	private String beginTestcase = null;
	private String endTestcase = null;

	public String getBeginControlPart() {
		return beginControlPart;
	}

	public void setBeginControlPart(final String beginControlPart) {
		this.beginControlPart = beginControlPart;
	}

	public String getEndControlPart() {
		return endControlPart;
	}

	public void setEndControlPart(final String endControlPart) {
		this.endControlPart = endControlPart;
	}

	public String getBeginTestcase() {
		return beginTestcase;
	}

	public void setBeginTestcase(final String beginTestcase) {
		this.beginTestcase = beginTestcase;
	}

	public String getEndTestcase() {
		return endTestcase;
	}

	public void setEndTestcase(final String endTestcase) {
		this.endTestcase = endTestcase;
	}
}
