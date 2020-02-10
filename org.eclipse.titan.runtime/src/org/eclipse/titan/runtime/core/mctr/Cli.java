/******************************************************************************
 * Copyright (c) 2000-2019 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core.mctr;

import java.io.File;

import org.eclipse.titan.runtime.core.MainController;
import org.eclipse.titan.runtime.core.TTCN_Runtime;
import org.eclipse.titan.runtime.core.cfgparser.CfgAnalyzer;
import org.eclipse.titan.runtime.core.cfgparser.MCSectionHandler;

/**
 * User interface cli implementation.
 */
public class Cli extends UserInterface {

	/*
	 * Functions for waiting until MC reaches the desired state.
	 */ 
	public enum waitStateEnum {
		WAIT_NOTHING, WAIT_HC_CONNECTED,
		WAIT_ACTIVE, WAIT_MTC_CREATED, WAIT_MTC_READY,
		WAIT_MTC_TERMINATED, WAIT_SHUTDOWN_COMPLETE,
		WAIT_EXECUTE_LIST
	}
	
	private static final MainControllerCommand cmtc_command = new MainControllerCommand(MainControllerCommand.CMTC_TEXT, " [hostname]", "Create the MTC.");
	private static final MainControllerCommand smtc_command = new MainControllerCommand(MainControllerCommand.SMTC_TEXT, " [module_name[[.control]|.testcase_name|.*]", "Start MTC with control part, test case or all test cases.");
	private static final MainControllerCommand stop_command = new MainControllerCommand(MainControllerCommand.STOP_TEXT, null,"Stop test execution.");
	private static final MainControllerCommand pause_command = new MainControllerCommand(MainControllerCommand.PAUSE_TEXT, " [on|off]","Set whether to interrupt test execution after each test case.");
	private static final MainControllerCommand continue_command = new MainControllerCommand(MainControllerCommand.CONTINUE_TEXT, null, "Resumes interrupted test execution.");
	private static final MainControllerCommand emtc_command = new MainControllerCommand(MainControllerCommand.EMTC_TEXT, null, "Terminate MTC.");
	private static final MainControllerCommand log_command = new MainControllerCommand(MainControllerCommand.LOG_TEXT, " [on|off]", "Enable/disable console logging.");
	private static final MainControllerCommand reconf_command = new MainControllerCommand(MainControllerCommand.RECONF_TEXT, " [config_file]", "Reload configuration file.");
	private static final MainControllerCommand help_command = new MainControllerCommand(MainControllerCommand.HELP_TEXT, " <command>", "Display help on command.");
	private static final MainControllerCommand shell_command = new MainControllerCommand(MainControllerCommand.SHELL_TEXT, "[shell cmds]", "Execute commands in subshell.");
	private static final MainControllerCommand exit_command = new MainControllerCommand(MainControllerCommand.EXIT_TEXT, null, "Exit Main Controller.");
	private static final MainControllerCommand exit_command2 = new MainControllerCommand(MainControllerCommand.EXIT_TEXT2, null, "Exit Main Controller.");
	private static final MainControllerCommand batch_command = new MainControllerCommand(MainControllerCommand.BATCH_TEXT, " <batch_file>", "Run commands from batch file.");
	private static final MainControllerCommand info_command = new MainControllerCommand(MainControllerCommand.INFO_TEXT, null, "Display test configuration information.");

	public boolean loggingEnabled;
	private boolean exitFlag;
	private String cfg_file_name;
	private waitStateEnum waitState;

	public Cli() {
		loggingEnabled = true;
		exitFlag = false;
		waitState = waitStateEnum.WAIT_NOTHING;
	}

	@Override
	public void initialize() {
		//Do nothing
	}

	@Override
	public int enterLoop(String[] args) {
		if (args.length > 1) {
			printUsage("mctr");
			return 1; //EXIT_FAILURE
		}

		printWelcome();

		if (args.length == 1) {
			final File config_file = new File(args[0]);
			System.out.printf("Using configuration file: %s\n", config_file.getName());
			
			CfgAnalyzer cfgAnalyzer = new CfgAnalyzer();
			final boolean config_file_failure = cfgAnalyzer.parse(config_file);
			if (config_file_failure) {
				System.out.println("Error was found in the configuration file. Exiting");
				//cleanup?
				return 1;
			} else {
				final MCSectionHandler mcSectionHandler = cfgAnalyzer.getMcSectionHandler();
			}
			

			
		}
		return 0;
	}

	@Override
	public void status_change() {
		// TODO Auto-generated method stub

	}

	@Override
	public void error(int severity, String message) {
		// TODO Auto-generated method stub

	}

	@Override
	public void notify(String source, int severity, String message) {
		// TODO Auto-generated method stub

	}

	@Override
	public void executeBatchFile(String filename) {
		// TODO Auto-generated method stub

	}
	/*
	 * Callback functions for command processing.
	 * (All callback functions MUST be public!!!)
	 */
	public void cmtcCallback(String arguments) {

	}

	public void smtcCallback(String arguments) {

	}

	public void stopCallback(String arguments) {

	}

	public void pauseCallback(String arguments) {

	}

	public void continueCallback(String arguments) {

	}

	public void emtcCallback(String arguments) {

	}

	public void logCallback(String arguments) {

	}

	public void infoCallback(String arguments) {

	}

	public void reconfCallback(String arguments) {

	}

	public void helpCallback(String arguments) {

	}

	public void shellCallback(String arguments) {

	}

	public void exitCallback(String arguments) {

	}

	/**
	 * Print the welcome text.
	 */
	private static void printWelcome() {
		System.out.printf("\n"+
				"*************************************************************************\n"+
				"* TTCN-3 Test Executor - Main Controller 2                              *\n"+
				"* Version: %-40s                     *\n"+
				"* Copyright (c) 2000-2019 Ericsson Telecom AB                           *\n"+
				"* All rights reserved. This program and the accompanying materials      *\n"+
				"* are made available under the terms of the Eclipse Public License v2.0 *\n"+
				"* which accompanies this distribution, and is available at              *\n"+
				"* https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html            *\n"+
				"*************************************************************************\n"+
				"\n", TTCN_Runtime.PRODUCT_NUMBER);
	}
	/**
	 * Print program usage information.
	 */
	private static void printUsage(String prg_name) {
		System.err.printf(
				"TTCN-3 Test Executor - Main Controller 2\n"+
						"Version: " + TTCN_Runtime.PRODUCT_NUMBER + "\n\n"+
						"usage: %s configuration_file\n" +
						"where: the 'configuration_file' parameter specifies the name and \n"+
				"location of the main controller configuration file\n", prg_name);
	}

	/**
	 * The main cli event loop.
	 */
	private int interactiveMode() {
		return 0;
	}

	/**
	 * Execution in batch mode.
	 */
	private int batchMode() {
		return 0;
	}

	private boolean conditionHolds(waitStateEnum askedState) {
		return false;
	}
	
	private void waitMCState(waitStateEnum newWaitState) {

	}
	
	/**
	 * Process the command to perform the action accordingly.
	 */
	private void processCommand(final String line_read) {
		//Read nothing -> skip
		if (line_read == null || line_read.isEmpty()) {
			return;
		}
		String[] splitted_line = line_read.split("\\s");
		String command = splitted_line[0];
		String argument = null;
		if (splitted_line.length == 2) {
			argument = splitted_line[1];
		}
		
		switch (command) {
		case MainControllerCommand.CMTC_TEXT:
			cmtcCallback(argument);
			break;
		case MainControllerCommand.SMTC_TEXT:
			smtcCallback(argument);
			break;
		case MainControllerCommand.STOP_TEXT:
			stopCallback(argument);
			break;
		case MainControllerCommand.PAUSE_TEXT:
			pauseCallback(argument);
			break;
		case MainControllerCommand.CONTINUE_TEXT:
			continueCallback(argument);
			break;
		case MainControllerCommand.EMTC_TEXT:
			emtcCallback(argument);
			break;
		case MainControllerCommand.LOG_TEXT:
			logCallback(argument);
			break;
		case MainControllerCommand.RECONF_TEXT:
			reconfCallback(argument);
			break;
		case MainControllerCommand.HELP_TEXT:
			helpCallback(argument);
		case MainControllerCommand.SHELL_TEXT:
			shellCallback(argument);
		case MainControllerCommand.EXIT_TEXT:
		case MainControllerCommand.EXIT_TEXT2:
			exitCallback(argument);
			break;
		case MainControllerCommand.BATCH_TEXT:
			executeBatchFile(argument);
			break;
		case MainControllerCommand.INFO_TEXT:
			infoCallback(argument);
			break;
		default:
			break;
		}
		
 	}
}
