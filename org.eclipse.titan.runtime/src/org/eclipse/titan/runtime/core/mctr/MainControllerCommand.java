/******************************************************************************
 * Copyright (c) 2000-2020 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core.mctr;

public class MainControllerCommand {
	
	public static final String PROMPT = "MC2> ";
	public static final String CMTC_TEXT = "cmtc";
	public static final String SMTC_TEXT = "smtc";
	public static final String EMTC_TEXT = "emtc";
	public static final String STOP_TEXT = "stop";
	public static final String PAUSE_TEXT = "pause";
	public static final String CONTINUE_TEXT = "continue";
	public static final String INFO_TEXT = "info";
	public static final String HELP_TEXT = "help";
	public static final String RECONF_TEXT = "reconf";
	public static final String LOG_TEXT = "log";
	public static final String SHELL_TEXT = "!";
	public static final String EXIT_TEXT = "quit";
	public static final String EXIT_TEXT2 = "exit";
	public static final String BATCH_TEXT = "batch";
	public static final String SHELL_ESCAPE = "!";
	public static final String TTCN3_HISTORY_FILENAME = ".ttcn3_history";
	
	private String name;
	private String synopsis;
	private String description;
	
	public MainControllerCommand(final String name, final String synopsis, final String description) {
		this.name = name;
		this.synopsis = synopsis;
		this.description = description;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public String getSynopsis() {
		return synopsis;
	}

	public void setSynopsis(final String synopsis) {
		this.synopsis = synopsis;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(final String description) {
		this.description = description;
	}
}
