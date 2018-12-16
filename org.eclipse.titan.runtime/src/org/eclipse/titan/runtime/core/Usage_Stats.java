/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

public final class Usage_Stats {
	/**
	 * Global configuration flag for usage statistics sending, true if usage statistics is sent
	 */
	public static final boolean USAGE_STAT_SENDING = false;

	private static final String HOST = "ttcn.ericsson.se";
	private static final String PAGE = "/download/usage_stats/usage_stats.php";
	private static final int PORT = 80;

	private static StringBuilder collectSystemData() {
		StringBuilder result2 = new StringBuilder();
		result2.append("plugin_id=").append("org.eclipse.titan.runtime");
		//does not really know yet how to add the full qualifier, without depending on eclipse
		result2.append("&").append("plugin_version_qualifier=").append(TTCN_Runtime.PRODUCT_NUMBER);
		result2.append("&").append("plugin_version=").append(TTCN_Runtime.VERSION_STRING);

		try {
			result2.append("&").append("os_version=").append(System.getProperty("os.version"));
			result2.append("&").append("os_arch=").append(System.getProperty("os.arch"));
			result2.append("&").append("eclipse_version=").append("n/a");
			result2.append("&").append("eclipse_version_qualifier=").append("n/a");
		} catch (final SecurityException e) {
			return result2;
		}
		try {
			result2.append("&").append("hostname=").append(InetAddress.getLocalHost().getCanonicalHostName());
		} catch (final Exception e) {
			result2.append("&").append("hostname=").append("UNKNOWN");
		}
		try {
			result2.append("&").append("java_vendor=").append(System.getProperty("java.vendor"));
			result2.append("&").append("user_id=").append(System.getProperty("user.name"));
			result2.append("&").append("java_version=").append(System.getProperty("java.version"));
			result2.append("&").append("os_name=").append(System.getProperty("os.name"));
		} catch (final SecurityException e) {
			return result2;
		}

		result2.append("&").append("info=").append("Java runtime(just experimenting)");
		return result2;
	}

	private static void post(final String message) {
		//the first three tries are "named" so that they can be opened on firewalls in labs.
		final int[] ports = {49555, 59555, 61555, 0};
		final Socket socket = new Socket();

		for (int i = 0; i < ports.length; i++) {
			try {
				// try binding the first port
				socket.bind(new InetSocketAddress(InetAddress.getLocalHost(), ports[i]));
				break;
			} catch (IOException e) {
				if (socket.isBound()) {
					try {
						socket.close();
					} catch (final Exception e2) {
						// stay silent
					}
				}
			}
		}

		try {
			socket.connect(new InetSocketAddress(HOST, PORT));

			final String urlParameters = collectSystemData().toString();
			final DataOutputStream wr = new DataOutputStream(socket.getOutputStream());
			wr.writeBytes("POST " + PAGE + " HTTP/1.0\r\n" +
					"Host: " + HOST + "\r\n" +
					"Content-type: application/x-www-form-urlencoded\r\n" +
					"Content-length: " + Integer.toString(urlParameters.length()) + "\r\n\r\n");
			wr.writeBytes(urlParameters);
			wr.flush();
			wr.close();

			socket.close();
			return;
		} catch (final IOException e) {
			//unable to connect, silently exit
		} catch (final Exception e) {
			//unable to connect, silently exit
		}

		if( socket != null ) {
			try {
				socket.close();
			} catch (final IOException e) {
				//silently exit
			} catch (final Exception e) {
				//silently exit
			}
		}
	}

	public static void sendAsync(final String message) {
		final Thread thread = new Thread() {

			@Override
			public void run() {
				post(message);
			}
		};
		thread.start();
	}
}
