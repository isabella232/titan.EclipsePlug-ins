/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.executor.jni;

/**
 * Data structure for each host (and the corresponding HC).
 * <p>
 * The original C++ structure can be found at TTCNv3\mctr2\mctr\MainController.h
 *
 * @author Peter Dimitrov
 * */
public final class HostStruct {
	public String ip_addr;
	public String hostname;
	public String hostname_local;
	public String machine_type;
	public String system_name;
	public String system_release;
	public String system_version;
	public boolean[] transport_supported;
	public String log_source;
	public HcStateEnum hc_state;
	public int hc_fd;
	public byte[] text_buf;
	public int[] components;
	public String[] allowed_components;
	public boolean all_components_allowed;
	public int n_active_components;

	public HostStruct(final int tr, final int tb, final int co, final int ac) {
		transport_supported = new boolean[tr];
		text_buf = new byte[tb];
		components = new int[co];
		allowed_components = new String[ac];
	}

}
