/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.SocketChannel;
import java.util.Arrays;

/**
 * Originally common.NetworkHandler.hh/cc.
 * This class handle the local and the MC connection.
 * Use Inet4Address java class instead of IPv4Address and Inet6Address java class instead of IPV6Address.
 * 
 * @author Gergo Ujhelyi
 *
 */
public class NetworkHandler {

	public static enum NetworkFamily { ipv0, ipv4, ipv6 };

	protected NetworkFamily m_family;
	protected InetSocketAddress m_addr;

	public NetworkHandler() {
		m_family = NetworkFamily.ipv4;
	}

	public NetworkHandler(final NetworkFamily p_family) {
		m_family = p_family;
	}

	public NetworkHandler(final InetSocketAddress p_addr) {
		m_addr = p_addr;
		set_family(m_addr);
	}

	public void set_family(final NetworkFamily p_family) {
		m_family = p_family;
	}

	public void set_family(final InetSocketAddress p_addr) {
		if (p_addr.getAddress() instanceof Inet4Address) {
			m_family = NetworkFamily.ipv4;
			return;
		}
		if (p_addr.getAddress() instanceof Inet6Address) {
			m_family = NetworkFamily.ipv6;
			return;
		} else {
			m_family = NetworkFamily.ipv0;
		}
	}

	public NetworkFamily get_family() {
		return m_family;
	}

	public InetSocketAddress get_addr() {
		return m_addr;
	}

	public Socket socket() {
		if (m_addr == null) {
			return null;
		} else {
			try {
				return new Socket(m_addr.getAddress(), m_addr.getPort());
			} catch (IOException e) {
				return null;
			}
		}
	}

	//IPAdress functions
	public boolean is_local() {
		if (m_addr == null) {
			return false;
		} 
		if (m_addr.getAddress() instanceof Inet4Address) {
			final byte[] ipv4_localhost_bytes = {127, 0, 0, 1};
			return Arrays.equals(m_addr.getAddress().getAddress(),ipv4_localhost_bytes);
		}
		if (m_addr.getAddress() instanceof Inet6Address) {
			final byte localhost_bytes[] = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1 };
			final byte mapped_ipv4_localhost[] = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1, -1, 127, 0, 0, 1 };
			return Arrays.equals(m_addr.getAddress().getAddress(), localhost_bytes) || Arrays.equals(m_addr.getAddress().getAddress(), mapped_ipv4_localhost);
		}
		return false;
	}

	public static class HCNetworkHandler extends NetworkHandler {

		private InetSocketAddress m_mc_addr;
		private InetSocketAddress m_local_addr;

		public HCNetworkHandler() {
			m_mc_addr = null;
			m_local_addr = null;
		}

		public boolean set_local_addr(final String p_addr, final int p_port) {
			if (p_addr == null) {
				return false;
			}
			m_local_addr = new InetSocketAddress(p_addr, p_port);
			return m_local_addr != null;
		}

		public boolean set_mc_addr(final String p_addr, final int p_port) {
			if (p_addr == null) {
				return false;
			}
			m_mc_addr = new InetSocketAddress(p_addr, p_port);
			return m_mc_addr != null;
		}

		//TODO: implement public int getsockname_local_addr(int p_sockfd);
		//TODO: implement socket()

		public SocketChannel bind_local_addr() {
			try {
				return SocketChannel.open().bind(m_local_addr);
			} catch (IOException e) {
				return null;
			}
		}

		public SocketChannel connect_to_mc() {
			SocketChannel sc;
			try {
				sc = SocketChannel.open();
			} catch (IOException e) {
				return null;
			}
			try {
				if (sc.connect(m_mc_addr)) {
					return sc;
				} else {
					return null;
				}
			} catch (IOException e) {
				return null;
			}
		}

		public String get_mc_host_str() { 
			return m_mc_addr.getHostString(); 
		}

		public String get_mc_addr_str() { 
			return m_mc_addr.getAddress().getHostAddress(); 
		}

		public String get_local_host_str() { 
			return m_local_addr.getHostString(); 
		}

		public String get_local_addr_str() {
			return m_local_addr.getAddress().getHostAddress(); 
		}

		public InetSocketAddress get_mc_addr() { 
			return m_mc_addr; 
		}

		public InetSocketAddress get_local_addr() { 
			return m_local_addr; 
		}

		public int get_mc_port() {
			return m_mc_addr.getPort(); 
		}

		public int get_local_port() { 
			return m_local_addr.getPort(); 
		}

		//IPAdress functions
		public boolean is_local(final InetSocketAddress p_addr) {
			if (p_addr == null) {
				return false;
			} 
			if (p_addr.getAddress() instanceof Inet4Address) {
				final byte[] ipv4_localhost_bytes = {127, 0, 0, 1};
				return Arrays.equals(p_addr.getAddress().getAddress(),ipv4_localhost_bytes);
			}
			if (p_addr.getAddress() instanceof Inet6Address) {
				final byte localhost_bytes[] = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1 };
				final byte mapped_ipv4_localhost[] = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1, -1, 127, 0, 0, 1 };
				return Arrays.equals(p_addr.getAddress().getAddress(), localhost_bytes) || Arrays.equals(p_addr.getAddress().getAddress(), mapped_ipv4_localhost);
			}
			return false;
		}
	}
}
