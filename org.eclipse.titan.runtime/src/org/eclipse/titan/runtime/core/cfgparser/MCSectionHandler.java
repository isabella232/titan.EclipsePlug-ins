/******************************************************************************
 * Copyright (c) 2000-2021 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core.cfgparser;

import java.math.BigInteger;

/**
 * @author Kristof Szabados
 * @author Arpad Lovassy
 */
public final class MCSectionHandler {

	private String localAddress = null;
	private BigInteger tcpPort = null;
	private Double killTimer = null;
	private BigInteger numHCsText = null;
	private Boolean unixDomainSocket = null;

	public String getLocalAddress() {
		return localAddress;
	}

	public void setLocalAddress(final String localAddress) {
		this.localAddress = localAddress;
	}

	public BigInteger getTcpPort() {
		return tcpPort;
	}

	public void setTcpPort(final BigInteger tcpPort) {
		this.tcpPort = tcpPort;
	}

	public Double getKillTimer() {
		return killTimer;
	}

	public void setKillTimer(final double floatnum) {
		this.killTimer = floatnum;
	}

	public BigInteger getNumHCsText() {
		return numHCsText;
	}

	public void setNumHCsText(final BigInteger numHCsText) {
		this.numHCsText = numHCsText;
	}

	public Boolean getUnixDomainSocket() {
		return unixDomainSocket;
	}

	public void setUnixDomainSocket(final Boolean unixDomainSocket) {
		this.unixDomainSocket = unixDomainSocket;
	}
}
