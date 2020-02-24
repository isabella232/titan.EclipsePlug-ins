/******************************************************************************
 * Copyright (c) 2000-2019 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core.mctr;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.titan.runtime.core.cfgparser.ComponentSectionHandler.Component;
import org.eclipse.titan.runtime.core.cfgparser.ExecuteSectionHandler.ExecuteItem;
import org.eclipse.titan.runtime.core.cfgparser.GroupSectionHandler.Group;

public class ConfigData {

	private String log_file_name;
	private List<ExecuteItem> executeItems;
	private List<Group> group_list;
	private List<Component> component_list;
	private String local_addr;
	private int tcp_listen_port;
	private BigInteger num_hcs;
	private Double kill_timer;

	public ConfigData() {
		log_file_name = null;
		local_addr = null;
		tcp_listen_port = 0;
		num_hcs = new BigInteger("-1");
		kill_timer = 10.0;
	}

	public void clear() {
		log_file_name = null;
		executeItems = null;
		group_list = null;
		component_list = null;
		local_addr = null;
		tcp_listen_port = 0;
		num_hcs = new BigInteger("-1");
		kill_timer = 10.0;
	}

	/** Add an entry from the [GROUPS] section.
	 *
	 * @param group_list: The list of groups in config file.
	 */
	public void add_host(List<Group> group_list) {
		this.group_list = group_list;
	}

	/** Add an entry from the [COMPONENTS] section.
	 *
	 * @param component_list: The components in config file.
	 */
	public void add_component(List<Component> component_list) {
		this.component_list = component_list;
	}

	/** Add an entry from the [EXECUTE] section
	 *
	 * @param exec_items: The execute items in config file.
	 */
	public void add_exec(final List<ExecuteItem> exec_items) {
		this.executeItems = exec_items;
	}

	/** Set the log file name
	 *
	 * @param f file name skeleton; the function takes ownership
	 */
	public void set_log_file(String f) {
		if (f != null) {
			log_file_name = f;
		}
	}

	public String getLog_file_name() {
		return log_file_name;
	}

	public List<ExecuteItem> getExecuteItems() {
		return executeItems;
	}

	public List<Group> getGroup_list() {
		return group_list;
	}

	public List<Component> getComponent_list() {
		return component_list;
	}

	public String getLocal_addr() {
		return local_addr;
	}

	public int getTcp_listen_port() {
		return tcp_listen_port;
	}

	public BigInteger getNum_hcs() {
		return num_hcs;
	}

	public Double getKill_timer() {
		return kill_timer;
	}
}
