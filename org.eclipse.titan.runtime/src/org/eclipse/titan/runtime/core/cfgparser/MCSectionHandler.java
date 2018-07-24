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
public final class MCSectionHandler {

	private String localAddress = null;
	//TODO: change type to integer
	private CFGNumber tcpPort = null;
	private CFGNumber killTimer = null;
	//TODO: change type to integer
	private CFGNumber numHCsText = null;
	private Boolean unixDomainSocket = null;

	public String getLocalAddress() {
		return localAddress;
	}

	public void setLocalAddress(final String localAddress) {
		this.localAddress = localAddress;
	}

	public CFGNumber getTcpPort() {
		return tcpPort;
	}

	public void setTcpPort(final CFGNumber tcpPort) {
		this.tcpPort = tcpPort;
	}

	public CFGNumber getKillTimer() {
		return killTimer;
	}

	public void setKillTimer(final CFGNumber killTimer) {
		this.killTimer = killTimer;
	}

	public CFGNumber getNumHCsText() {
		return numHCsText;
	}

	public void setNumHCsText(final CFGNumber numHCsText) {
		this.numHCsText = numHCsText;
	}

	public Boolean getUnixDomainSocket() {
		return unixDomainSocket;
	}

	public void setUnixDomainSocket(final Boolean unixDomainSocket) {
		this.unixDomainSocket = unixDomainSocket;
	}
}
