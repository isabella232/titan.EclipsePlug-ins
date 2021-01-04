/******************************************************************************
 * Copyright (c) 2000-2021 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
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

	public void set_family(final String p_addr, final int tcp_port) {
		if (p_addr == null) {
			m_family = NetworkFamily.ipv4;
		} else {
			set_family(new InetSocketAddress(p_addr, tcp_port));
		}
	}

	public void set_family(final NetworkFamily p_family) {
		m_family = p_family;
	}

	public void set_family(final InetSocketAddress p_addr) {
		if (p_addr.getAddress() instanceof Inet4Address) {
			m_family = NetworkFamily.ipv4;
			m_addr = p_addr;
			return;
		}
		if (p_addr.getAddress() instanceof Inet6Address) {
			m_family = NetworkFamily.ipv6;
			m_addr = p_addr;
			return;
		} else {
			m_family = NetworkFamily.ipv0;
			m_addr = p_addr;
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

	public void clean_up() {
		m_family = NetworkFamily.ipv0;
		m_addr = null;
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
			set_family(m_local_addr);
			return m_local_addr != null;
		}

		public boolean set_mc_addr(final String p_addr, final int p_port) {
			if (p_addr == null) {
				return false;
			}
			m_mc_addr = new InetSocketAddress(p_addr, p_port);
			set_family(m_mc_addr);
			return m_mc_addr != null;
		}

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

		public void clean_up() {
			m_local_addr = null;
			m_mc_addr = null;
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

		/**
		 * originally push_raw()
		 * Use it when you want to push the local address to @param p_buf Text Buffer.
		 */
		public void push_raw_local_addr(final Text_Buf p_buf) {
			if (m_local_addr.getAddress() instanceof Inet4Address) {
				p_buf.push_raw(1, new byte[]{2}); //C++ AF_INET
				p_buf.push_int(m_local_addr.getPort());
				p_buf.push_raw(4, m_local_addr.getAddress().getAddress());
				//TODO: check to need
				//p_buf.push_raw(8, new byte[]{0,0,0,0,0,0,0,0});
				return;
			}
			if (m_local_addr.getAddress() instanceof Inet6Address) {
				p_buf.push_raw(1, new byte[] {23}); //C++ AF_INET6
				p_buf.push_int(m_local_addr.getPort());
				//TODO: push flow info
				p_buf.push_raw(16, m_local_addr.getAddress().getAddress());
				p_buf.push_int(getIpv6Address(m_local_addr.getAddress()).getScopeId());
			}
		}

		/**
		 * originally push_raw()
		 * Use it when you want to push the main controller address to @param p_buf Text Buffer.
		 */
		public void push_raw_mc_addr(final Text_Buf p_buf) {
			if (m_mc_addr.getAddress() instanceof Inet4Address) {
				p_buf.push_raw(1, new byte[]{2}); //C++ AF_INET
				p_buf.push_int(m_mc_addr.getPort());
				p_buf.push_raw(4, m_mc_addr.getAddress().getAddress());
				//TODO: check to need
				//p_buf.push_raw(8, new byte[]{0,0,0,0,0,0,0,0});
				return;
			}
			if (m_mc_addr.getAddress() instanceof Inet6Address) {
				p_buf.push_raw(1, new byte[] {23}); //C++ AF_INET6
				p_buf.push_int(m_mc_addr.getPort());
				//TODO: push flow info
				p_buf.push_raw(16, m_mc_addr.getAddress().getAddress());
				p_buf.push_int(getIpv6Address(m_mc_addr.getAddress()).getScopeId());
			}
		}

		/**
		 * originally pull_raw()
		 * Use it when you want to pull the local address from @param p_buf Text Buffer.
		 */
		public void pull_raw_local_addr(final Text_Buf p_buf) {
			final byte network_family[] = new byte[1];
			p_buf.pull_raw(1, network_family);
			//IPv4 address
			if (network_family[0] == 2) {
				final int port_number = p_buf.pull_int().get_int();
				final byte ip_address[] = new byte[4];
				p_buf.pull_raw(4, ip_address);
				try {
					m_local_addr = new InetSocketAddress(InetAddress.getByAddress(ip_address), port_number);
					return;
				} catch (UnknownHostException e) {
					m_local_addr = null;
					return;
				}
				//IPv6 address
			} else if (network_family[0] == 23) {
				final int port_number = p_buf.pull_int().get_int();
				final byte ip_address[] = new byte[16];
				p_buf.pull_raw(16, ip_address);
				int scope_id = 0;
				if (p_buf.pull_int().is_bound()) {
					scope_id = p_buf.pull_int().get_int();
				}
				try {
					if (scope_id != 0) {
						m_local_addr = new InetSocketAddress(Inet6Address.getByAddress("", ip_address, scope_id), port_number);
					} else {
						m_local_addr = new InetSocketAddress(InetAddress.getByAddress(ip_address), port_number);
						return;
					}
				} catch (UnknownHostException e) {
					m_local_addr = null;
					return;
				}
			}
		}

		/**
		 * originally pull_raw()
		 * Use it when you want to pull the main controller address from @param p_buf Text Buffer.
		 */
		public void pull_raw_mc_addr(final Text_Buf p_buf) {
			final byte network_family[] = new byte[1];
			p_buf.pull_raw(1, network_family);
			//IPv4 address
			if (network_family[0] == 2) {
				final int port_number = p_buf.pull_int().get_int();
				final byte ip_address[] = new byte[4];
				p_buf.pull_raw(4, ip_address);
				try {
					m_mc_addr = new InetSocketAddress(InetAddress.getByAddress(ip_address), port_number);
					return;
				} catch (UnknownHostException e) {
					m_mc_addr = null;
					return;
				}
				//IPv6 address
			} else if (network_family[0] == 23) {
				final int port_number = p_buf.pull_int().get_int();
				final byte ip_address[] = new byte[16];
				p_buf.pull_raw(16, ip_address);
				try {
					m_mc_addr = new InetSocketAddress(InetAddress.getByAddress(ip_address), port_number);
					return;
				} catch (UnknownHostException e) {
					m_mc_addr = null;
					return;
				}
			}
		}

		//returns an IPv6 address.
		private Inet6Address getIpv6Address(final InetAddress p_address) {
			return (Inet6Address)p_address;
		}
	}
}
