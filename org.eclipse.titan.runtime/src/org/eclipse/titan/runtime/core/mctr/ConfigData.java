/******************************************************************************
 * Copyright (c) 2000-2021 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core.mctr;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

import org.eclipse.titan.runtime.core.cfgparser.IOUtils;
import org.eclipse.titan.runtime.core.cfgparser.ComponentSectionHandler.Component;
import org.eclipse.titan.runtime.core.cfgparser.ExecuteSectionHandler.ExecuteItem;
import org.eclipse.titan.runtime.core.cfgparser.GroupSectionHandler.Group;

public class ConfigData {

	public static enum cf_timestamp_format { 
		TSF_NONE,
		TSF_TIME,
		TSF_DATE_TIME,
		TSF_SEC
	}
	private String log_file_name;
	private List<ExecuteItem> executeItems;
	private List<Group> group_list;
	private List<Component> component_list;
	private String local_addr;
	private int tcp_listen_port;
	private BigInteger num_hcs;
	private Double kill_timer;
	private cf_timestamp_format tsformat = cf_timestamp_format.TSF_NONE;

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
	public void add_host(final List<Group> group_list) {
		this.group_list = group_list;
	}

	/** Add an entry from the [COMPONENTS] section.
	 *
	 * @param component_list: The components in config file.
	 */
	public void add_component(final List<Component> component_list) {
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
	public void set_log_file(final String f) {
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

	public void setLocal_addr(final String local_addr) {
		this.local_addr = local_addr;
	}

	public void setTcp_listen_port(final int tcp_listen_port) {
		this.tcp_listen_port = tcp_listen_port;
	}

	public void setNum_hcs(final BigInteger num_hcs) {
		this.num_hcs = num_hcs;
	}

	public void setKill_timer(final Double kill_timer) {
		this.kill_timer = kill_timer;
	}

	//Package-private
	static String getConfigFileContent(File config_file) {
		if (config_file == null) {
			return "";
		}
		StringBuilder contentBuilder = new StringBuilder();
		BufferedReader br = null;

		try {
			br = new BufferedReader(new FileReader(config_file));
			String currentLine;
			while ((currentLine = br.readLine()) != null) {
				contentBuilder.append(currentLine).append("\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		IOUtils.closeQuietly(br);

		return contentBuilder.toString();
	}

	public cf_timestamp_format getTsformat() {
		return tsformat;
	}

	public void setTsformat(cf_timestamp_format tsformat) {
		this.tsformat = tsformat;
	}
	
	


}
