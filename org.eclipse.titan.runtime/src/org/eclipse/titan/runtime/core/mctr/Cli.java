/******************************************************************************
 * Copyright (c) 2000-2020 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core.mctr;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.titan.runtime.core.TTCN_Runtime;
import org.eclipse.titan.runtime.core.cfgparser.CfgAnalyzer;
import org.eclipse.titan.runtime.core.cfgparser.ExecuteSectionHandler;
import org.eclipse.titan.runtime.core.cfgparser.MCSectionHandler;
import org.eclipse.titan.runtime.core.mctr.MainController.mcStateEnum;

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
	private ConfigData mycfg = new ConfigData();
	private int executeListIndex;
	private ReentrantLock mutex;

	public Cli() {
		loggingEnabled = true;
		exitFlag = false;
		waitState = waitStateEnum.WAIT_NOTHING;
		executeListIndex = 0;
		mutex = new ReentrantLock();
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
				mycfg.set_log_file(args[0]);
				final MCSectionHandler mcSectionHandler = cfgAnalyzer.getMcSectionHandler();
				final ExecuteSectionHandler executeSectionHandler = cfgAnalyzer.getExecuteSectionHandler();

				if (mcSectionHandler.getKillTimer() != null) {
					MainController.set_kill_timer(mcSectionHandler.getKillTimer());
				}

				if (mcSectionHandler.getNumHCsText() != null) {
					mycfg.setNum_hcs(mcSectionHandler.getNumHCsText());
				}

				mycfg.setLocal_addr(mcSectionHandler.getLocalAddress()); 
				if ( mycfg.getLocal_addr() == null || mycfg.getLocal_addr().isEmpty()) {
					//By default set the host's address
					try {
						mycfg.setLocal_addr(InetAddress.getLocalHost().getHostAddress());
					} catch (UnknownHostException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				if (mcSectionHandler.getTcpPort() != null) {
					mycfg.setTcp_listen_port(mcSectionHandler.getTcpPort().intValue());
					if (mycfg.getTcp_listen_port() < 0 || mycfg.getTcp_listen_port() > 65535) {
						mycfg.setTcp_listen_port(0);
					}
				} else {
					mycfg.setTcp_listen_port(0);
				}

				mycfg.add_exec(executeSectionHandler.getExecuteitems());
				//TODO: assign groups, components and host
			}
		}
		int ret_val = 0;
		
		if (mycfg.getNum_hcs().compareTo(BigInteger.ZERO) <= 0) {
	
			ret_val = interactiveMode();
		} else {
			ret_val = batchMode();
		}

		//cleanUp ?

		return ret_val;
	}

	@Override
	public void status_change() {
		mutex.lock();
		try {
			if (waitState != waitStateEnum.WAIT_NOTHING && conditionHolds(waitState)) {
				waitState = waitStateEnum.WAIT_NOTHING;
			}
		} finally {
			mutex.unlock();
		}
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
		if (arguments == null || arguments.isEmpty()) {
			switch (MainController.get_state()) {
			case MC_READY:
			case MC_RECONFIGURING:
				MainController.exit_mtc();
				waitMCState(waitStateEnum.WAIT_MTC_TERMINATED);
				break;
		    case MC_LISTENING:
		    case MC_LISTENING_CONFIGURED:
		    case MC_HC_CONNECTED:
		    case MC_ACTIVE:
		    	MainController.shutdown_session();
		    	waitMCState(waitStateEnum.WAIT_SHUTDOWN_COMPLETE);
		    	exitFlag = true;
		    	break;
			default:
				System.out.println("Cannot exit until execution is finished.");
				break;
			}
		} else {
			helpCallback(MainControllerCommand.EXIT_TEXT);
		}
	}

	private static void cleanUp() {
		//Empty by default
	}

	/**
	 * Print the welcome text.
	 */
	private static void printWelcome() {
		System.out.printf("\n"+
				"*************************************************************************\n"+
				"* TTCN-3 Test Executor - Main Controller 2                              *\n"+
				"* Version: %-40s                     *\n"+
				"* Copyright (c) 2000-2020 Ericsson Telecom AB                           *\n"+
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
		if (MainController.start_session(mycfg.getLocal_addr(), mycfg.getTcp_listen_port()) == 0) {
			System.out.println("Initialization of TCP server failed. Exiting.");
			return 1; //EXIT_FAILURE
		}

		BufferedReader console_reader = new BufferedReader(new InputStreamReader(System.in));

		do {
			try {
				String line_read = console_reader.readLine();
				if (line_read != null) {
					line_read = line_read.trim();
					processCommand(line_read);
					line_read = null;
				} else {
					System.out.println("exit");
					exitCallback("");
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} while(!exitFlag);
		return 0; //EXIT_SUCCESS
	}

	/**
	 * Execution in batch mode.
	 */
	private int batchMode() {
		System.out.println(String.format("Entering batch mode. Waiting for %d HC%s to connect...", mycfg.getNum_hcs(), mycfg.getNum_hcs().compareTo(BigInteger.ONE) > 0 ? "s" : ""));
		if (mycfg.getExecuteItems() == null || mycfg.getExecuteItems().isEmpty()) {
			System.out.println("No [EXECUTE] section was given in the configuration file. Exiting.");
			return 1; //EXIT_FAILURE
		}
		boolean error_flag = false;
		// start to listen on TCP port
		if (MainController.start_session(mycfg.getLocal_addr(), mycfg.getTcp_listen_port()) == 0 ) {
			System.out.println("Initialization of TCP server failed. Exiting.");
			return 1;
		}
		waitMCState(waitStateEnum.WAIT_HC_CONNECTED);
		// download config file
		MainController.configure(mycfg.getLog_file_name());
		waitMCState(waitStateEnum.WAIT_ACTIVE);
		if (MainController.get_state() != mcStateEnum.MC_ACTIVE) {
			System.out.println("Error during initialization. Cannot continue in batch mode.");
			error_flag = true;
		}

		if (!error_flag) {
			// create MTC on firstly connected HC
			MainController.create_mtc(MainController.get_hosts().get(0));
			/*try {
				//TODO: need to test on different machines
				Thread.currentThread().join(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
			waitMCState(waitStateEnum.WAIT_MTC_CREATED);
			if (MainController.get_state() != mcStateEnum.MC_READY) {
				System.out.println("Creation of MTC failed. Cannot continue in batch mode.");
				error_flag = true;
			}
		}
		if (!error_flag) {
			// execute each item of the list
			for (int i = 0; i < mycfg.getExecuteItems().size(); i++) {
				executeFromList(i);
				waitMCState(waitStateEnum.WAIT_MTC_READY);
				if (MainController.get_state() != mcStateEnum.MC_READY) {
					System.out.println("MTC terminated unexpectedly. Cannot continue in batch mode.");
					error_flag = true;
					break;
				}
			}
		}
		if(!error_flag) {
			// terminate the MTC
			MainController.exit_mtc();
			waitMCState(waitStateEnum.WAIT_MTC_TERMINATED);
		}
		// now MC must be in state MC_ACTIVE anyway
		// shutdown MC
		MainController.shutdown_session();
		waitMCState(waitStateEnum.WAIT_SHUTDOWN_COMPLETE);
		if (error_flag) {
			return 1; //EXIT_FAILURE
		} else {
			return 0; //EXIT_SUCCESS
		}
	}

	private boolean conditionHolds(waitStateEnum askedState) {
		switch (askedState) {
		case WAIT_HC_CONNECTED:
			if (MainController.get_state() == mcStateEnum.MC_HC_CONNECTED) {
				if (mycfg.getNum_hcs().compareTo(BigInteger.ZERO) == 1) {
					return MainController.get_nof_hosts().compareTo(mycfg.getNum_hcs()) == 0 || MainController.get_nof_hosts().compareTo(mycfg.getNum_hcs()) == 1;
				} else {
					return true;
				}
			} else {
				return false;
			}
		case WAIT_ACTIVE:
			switch (MainController.get_state()) {
			case MC_ACTIVE: // normal case
			case MC_HC_CONNECTED: // error happened with config file
			case MC_LISTENING: // even more strange situations
				return true;
			default:
				return false;
			}
		case WAIT_MTC_CREATED:
		case WAIT_MTC_READY:
			switch (MainController.get_state()) {
			case MC_READY: // normal case
			case MC_ACTIVE: // MTC crashed unexpectedly
			case MC_LISTENING_CONFIGURED: // MTC and all HCs are crashed at the same time
			case MC_HC_CONNECTED: // even more strange situations
				return true;
			default:
				return false;
			}
		case WAIT_MTC_TERMINATED:
			return MainController.get_state() == mcStateEnum.MC_ACTIVE;
		case WAIT_SHUTDOWN_COMPLETE:
			return MainController.get_state() == mcStateEnum.MC_INACTIVE;
		case WAIT_EXECUTE_LIST:
			if (MainController.get_state() == mcStateEnum.MC_READY) {
				if (++executeListIndex < mycfg.getExecuteItems().size()) {
					executeFromList(executeListIndex);
				} else {
					System.out.println("Execution of [EXECUTE] section finished.");
					waitState = waitStateEnum.WAIT_NOTHING;
				}
			}
			return false;
		default:
			return false;
		}
	}

	private void waitMCState(waitStateEnum newWaitState) {
		if (newWaitState != waitStateEnum.WAIT_NOTHING) {
			if (conditionHolds(newWaitState) == true) {
				waitState = waitStateEnum.WAIT_NOTHING;
			} else {
				waitState = newWaitState;
			}
		} else {
			System.err.println("Cli.waitMCState: invalid argument");
			return;
		}
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

		if (command.equals(MainControllerCommand.CMTC_TEXT)) {
			cmtcCallback(argument);
		} else if (command.equals(MainControllerCommand.SMTC_TEXT)) {
			smtcCallback(argument);
		} else if (command.equals(MainControllerCommand.STOP_TEXT)) {
			stopCallback(argument);
		} else if (command.equals(MainControllerCommand.PAUSE_TEXT)) {
			pauseCallback(argument);
		} else if (command.equals(MainControllerCommand.CONTINUE_TEXT)) {
			continueCallback(argument);
		} else if (command.equals(MainControllerCommand.EMTC_TEXT)) {
			emtcCallback(argument);
		} else if (command.equals(MainControllerCommand.LOG_TEXT)) {
			logCallback(argument);
		} else if (command.equals(MainControllerCommand.RECONF_TEXT)) {
			reconfCallback(argument);
		} else if (command.equals(MainControllerCommand.HELP_TEXT)) {
			helpCallback(argument);
		} else if (command.equals(MainControllerCommand.SHELL_TEXT)) {
			shellCallback(argument);
		} else if (command.equals(MainControllerCommand.EXIT_TEXT) || command.equals(MainControllerCommand.EXIT_TEXT2)) {
			exitCallback(argument);
		} else if (command.equals(MainControllerCommand.BATCH_TEXT)) {
			executeBatchFile(argument);
		} else if (command.equals(MainControllerCommand.INFO_TEXT)) {
			infoCallback(argument);
		}
	}
	/*
	 * Executes the index-th element of the execute list
	 */
	private void executeFromList(int index) {
		if (index >= mycfg.getExecuteItems().size()) {
			System.err.println("Cli.executeFromList: invalid argument");
			return;
		}

		if (mycfg.getExecuteItems().get(index).getTestcaseName() == null) {
			MainController.execute_control(mycfg.getExecuteItems().get(index).getModuleName());
		} else if (!mycfg.getExecuteItems().get(index).getTestcaseName().equals("*")) {
			MainController.execute_testcase(mycfg.getExecuteItems().get(index).getModuleName(), null);
		} else {
			MainController.execute_testcase(mycfg.getExecuteItems().get(index).getModuleName(), mycfg.getExecuteItems().get(index).getTestcaseName());
		}
	}
}
