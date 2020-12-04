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
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.titan.runtime.core.TTCN_Runtime;
import org.eclipse.titan.runtime.core.TitanComponent;
import org.eclipse.titan.runtime.core.TitanVerdictType.VerdictTypeEnum;
import org.eclipse.titan.runtime.core.cfgparser.CfgAnalyzer;
import org.eclipse.titan.runtime.core.cfgparser.ExecuteSectionHandler;
import org.eclipse.titan.runtime.core.cfgparser.MCSectionHandler;
import org.eclipse.titan.runtime.core.mctr.MainController.ComponentStruct;
import org.eclipse.titan.runtime.core.mctr.MainController.Host;
import org.eclipse.titan.runtime.core.mctr.MainController.mcStateEnum;
import org.eclipse.titan.runtime.core.mctr.MainController.tc_state_enum;

/**
 * User interface cli implementation.
 */
public class Cli extends UserInterface {

	/*
	 * Functions for waiting until MC reaches the desired state.
	 */
	public enum waitStateEnum {
		WAIT_NOTHING, WAIT_HC_CONNECTED, WAIT_ACTIVE, WAIT_MTC_CREATED, WAIT_MTC_READY, WAIT_MTC_TERMINATED,
		WAIT_SHUTDOWN_COMPLETE, WAIT_EXECUTE_LIST
	}

	private final MainControllerCommand[] command_list = {
			new MainControllerCommand(MainControllerCommand.CMTC_TEXT, " [hostname]", "Create the MTC."),
			new MainControllerCommand(MainControllerCommand.SMTC_TEXT, " [module_name[[.control]|.testcase_name|.*]", "Start MTC with control part, test case or all test cases."),
			new MainControllerCommand(MainControllerCommand.STOP_TEXT, null, "Stop test execution."),
			new MainControllerCommand(MainControllerCommand.PAUSE_TEXT, " [on|off]", "Set whether to interrupt test execution after each test case."),
			new MainControllerCommand(MainControllerCommand.CONTINUE_TEXT, null, "Resumes interrupted test execution."),
			new MainControllerCommand(MainControllerCommand.EMTC_TEXT, null, "Terminate MTC."),
			new MainControllerCommand(MainControllerCommand.LOG_TEXT, " [on|off]", "Enable/disable console logging."),
			new MainControllerCommand( MainControllerCommand.RECONF_TEXT, " [config_file]", "Reload configuration file."),
			new MainControllerCommand(MainControllerCommand.HELP_TEXT, " <command>", "Display help on command."),
			new MainControllerCommand(MainControllerCommand.EXIT_TEXT, null, "Exit Main Controller."),
			new MainControllerCommand(MainControllerCommand.EXIT_TEXT2, null, "Exit Main Controller."),
			new MainControllerCommand(MainControllerCommand.BATCH_TEXT, " <batch_file>", "Run commands from batch file."),
			new MainControllerCommand(MainControllerCommand.INFO_TEXT, null, "Display test configuration information.")
	};

	private static final MainControllerCommand cmtc_command = new MainControllerCommand(MainControllerCommand.CMTC_TEXT,
			" [hostname]", "Create the MTC.");
	private static final MainControllerCommand smtc_command = new MainControllerCommand(MainControllerCommand.SMTC_TEXT,
			" [module_name[[.control]|.testcase_name|.*]", "Start MTC with control part, test case or all test cases.");
	private static final MainControllerCommand stop_command = new MainControllerCommand(MainControllerCommand.STOP_TEXT,
			null, "Stop test execution.");
	private static final MainControllerCommand pause_command = new MainControllerCommand(
			MainControllerCommand.PAUSE_TEXT, " [on|off]",
			"Set whether to interrupt test execution after each test case.");
	private static final MainControllerCommand continue_command = new MainControllerCommand(
			MainControllerCommand.CONTINUE_TEXT, null, "Resumes interrupted test execution.");
	private static final MainControllerCommand emtc_command = new MainControllerCommand(MainControllerCommand.EMTC_TEXT,
			null, "Terminate MTC.");
	private static final MainControllerCommand log_command = new MainControllerCommand(MainControllerCommand.LOG_TEXT,
			" [on|off]", "Enable/disable console logging.");
	private static final MainControllerCommand reconf_command = new MainControllerCommand(
			MainControllerCommand.RECONF_TEXT, " [config_file]", "Reload configuration file.");
	private static final MainControllerCommand help_command = new MainControllerCommand(MainControllerCommand.HELP_TEXT,
			" <command>", "Display help on command.");
	private static final MainControllerCommand shell_command = new MainControllerCommand(
			MainControllerCommand.SHELL_TEXT, "[shell cmds]", "Execute commands in subshell.");
	private static final MainControllerCommand exit_command = new MainControllerCommand(MainControllerCommand.EXIT_TEXT,
			null, "Exit Main Controller.");
	private static final MainControllerCommand exit_command2 = new MainControllerCommand(
			MainControllerCommand.EXIT_TEXT2, null, "Exit Main Controller.");
	private static final MainControllerCommand batch_command = new MainControllerCommand(
			MainControllerCommand.BATCH_TEXT, " <batch_file>", "Run commands from batch file.");
	private static final MainControllerCommand info_command = new MainControllerCommand(MainControllerCommand.INFO_TEXT,
			null, "Display test configuration information.");

	private static final int EXIT_FAILURE = 1;

	public boolean loggingEnabled;
	private boolean exitFlag;
	private waitStateEnum waitState;
	private final ConfigData mycfg = new ConfigData();
	private int executeListIndex;
	private final ReentrantLock mutex;
	private final Condition wakeup_condition;
	private MainController mainController;
	private File config_file;

	public Cli() {
		loggingEnabled = true;
		exitFlag = false;
		waitState = waitStateEnum.WAIT_NOTHING;
		executeListIndex = 0;
		mutex = new ReentrantLock();
		wakeup_condition = mutex.newCondition();
		config_file = null;
	}

	public void setMainController(final MainController mainController) {
		this.mainController = mainController;
	}

	@Override
	public void initialize() {
		// Do nothing
	}

	@Override
	public int enterLoop(final String[] args) {
		if (args.length > 1) {
			printUsage("mctr");
			return EXIT_FAILURE;
		}

		printWelcome();

		if (args.length == 1) {
			config_file = new File(args[0]);
			System.out.printf("Using configuration file: %s\n", config_file.getName());

			final CfgAnalyzer cfgAnalyzer = new CfgAnalyzer();
			final boolean config_file_failure = cfgAnalyzer.parse(config_file);
			if (config_file_failure) {
				System.out.println("Error was found in the configuration file. Exiting");
				cleanUp();
				return EXIT_FAILURE;
			} else {
				;
				final MCSectionHandler mcSectionHandler = cfgAnalyzer.getMcSectionHandler();
				final ExecuteSectionHandler executeSectionHandler = cfgAnalyzer.getExecuteSectionHandler();

				if (mcSectionHandler.getKillTimer() != null) {
					mainController.set_kill_timer(mcSectionHandler.getKillTimer());
				}

				if (mcSectionHandler.getNumHCsText() != null) {
					mycfg.setNum_hcs(mcSectionHandler.getNumHCsText());
				}

				mycfg.setLocal_addr(mcSectionHandler.getLocalAddress());
				if (mycfg.getLocal_addr() == null || mycfg.getLocal_addr().isEmpty()) {
					// By default set the host's address
					try {
						mycfg.setLocal_addr(InetAddress.getLocalHost().getHostAddress());
					} catch (UnknownHostException e) {
						System.err.println(e.getMessage());
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
				// TODO: assign groups, components and host
			}
		} else {
			try {
				mycfg.setLocal_addr(InetAddress.getLocalHost().getHostAddress());
			} catch (UnknownHostException e) {
				System.err.println(e.getMessage());
			}
			mycfg.setTcp_listen_port(0);
		}
		int ret_val = 0;
		if (mycfg.getNum_hcs().compareTo(BigInteger.ZERO) <= 0) {
			ret_val = interactiveMode();
		} else {
			ret_val = batchMode();
		}

		cleanUp();
		return ret_val;
	}

	@Override
	public void status_change() {
		mutex.lock();
		try {
			if (waitState != waitStateEnum.WAIT_NOTHING && conditionHolds(waitState)) {
				waitState = waitStateEnum.WAIT_NOTHING;
				signal();
			}
		} finally {
			mutex.unlock();
		}
	}

	@Override
	public void error(final int severity, final String message) {
		System.err.printf("Error: %s\n", message);
		// TODO: flush?
	}

	@Override
	public void notify(final Timeval timestamp, final String source, final int severity, final String message) {
		//TODO:implement
		System.out.printf("%s: %s\n", source, message);
	}

	@Override
	public void executeBatchFile(final String filename) {
		// TODO Auto-generated method stub

	}

	private void cmtcCallback(final String arguments) {
		int hostIndex;
		if (arguments == null || arguments.isEmpty()) {
			hostIndex = 0;
		} else {
			hostIndex = getHostIndex(arguments);
			if (hostIndex < 0) {
				return;
			}
		}
		switch (mainController.get_state()) {
		case MC_LISTENING:
		case MC_LISTENING_CONFIGURED:
			System.out.println("Waiting for HC to connect...");
			waitMCState(waitStateEnum.WAIT_HC_CONNECTED);
			// intentional fall through
		case MC_HC_CONNECTED:
			mainController.configure(ConfigData.getConfigFileContent(config_file));
			waitMCState(waitStateEnum.WAIT_ACTIVE);
			if (mainController.get_state() != mcStateEnum.MC_ACTIVE) {
				System.out.println("Error during initialization. Cannot create MTC.");
				break;
			}
			// intentional fall through
		case MC_ACTIVE:
			mainController.create_mtc(mainController.get_hosts().get(hostIndex));
			waitMCState(waitStateEnum.WAIT_MTC_CREATED);
			break;
		default:
			System.out.println("MTC already exists.");
			break;
		}
		// flush??
	}

	private void smtcCallback(final String arguments) {
		switch (mainController.get_state()) {
		case MC_LISTENING:
		case MC_LISTENING_CONFIGURED:
		case MC_HC_CONNECTED:
		case MC_ACTIVE:
			System.out.println("MTC does not exist.");
			break;
		case MC_READY:
			if (arguments == null || arguments.isEmpty()) {
				// Execute configuration file's execute section
				if (mycfg.getExecuteItems().size() > 0) {
					System.out.println("Executing all items of [EXECUTE] section.");
					waitState = waitStateEnum.WAIT_EXECUTE_LIST;
					executeListIndex = 0;
					executeFromList(0);
				} else {
					System.out.println("No [EXECUTE] section was given in the configuration file.");
				}
			} else {
				int doti = 0;
				int alen = arguments.length();
				int state = 0;
				for (int i = 0; i < alen; i++) {
					switch (arguments.charAt(i)) {
					case '.':
						++state;
						doti = i;
						break;
					case ' ':
					case '\t':
						state = 3;
					}
				}
				if (state > 1) { // incorrect argument
					System.out.println("Incorrect argument format.");
					helpCallback(MainControllerCommand.SMTC_TEXT);
				} else {
					if (state == 0) { // only modulename is given in arguments
						mainController.execute_control(arguments);
					} else { // modulename.something in arguments
						String testcaseName = arguments.substring(doti);
						String moduleName = arguments.substring(0, doti);
						if (!arguments.contains("*")) {
							mainController.execute_testcase(arguments, null);
						} else if (arguments.contains("control")) {
							mainController.execute_control(arguments);
						} else {
							mainController.execute_testcase(moduleName, testcaseName);
						}
					}
				}

			}
			break;
		default:
			System.out.println("MTC is busy.");
		}
	}

	private void stopCallback(final String arguments) {
		if (arguments == null || arguments.isEmpty()) {
			switch (mainController.get_state()) {
			case MC_TERMINATING_TESTCASE:
			case MC_EXECUTING_CONTROL:
			case MC_EXECUTING_TESTCASE:
			case MC_PAUSED:
				mainController.stop_execution();
				if (waitState == waitStateEnum.WAIT_EXECUTE_LIST) {
					waitState = waitStateEnum.WAIT_NOTHING;
				}
				break;
			default:
				System.out.println("Tests are not running.");
			}
		} else {
			helpCallback(MainControllerCommand.STOP_TEXT);
		}
	}

	private void pauseCallback(final String arguments) {
		if (arguments != null && !arguments.isEmpty()) {
			if (arguments.equals("on")) {
				if (!mainController.get_stop_after_testcase()) {
					mainController.stop_after_testcase(true);
					System.out.println("Pause function is enabled. Execution will stop at the end of each testcase.");
				} else {
					System.out.println("Pause function is already enabled.");
				}
			} else if (arguments.equals("off")) {
				if (mainController.get_stop_after_testcase()) {
					mainController.stop_after_testcase(false);
					System.out.println("Pause function is disabled. Execution will continue at the end of each testcase.");
				} else {
					System.out.println("Pause function is already disabled.");
				}
			} else {
				helpCallback(MainControllerCommand.PAUSE_TEXT);
			}
		} else {
			System.out.printf("Pause function is %s.\n", mainController.get_stop_after_testcase() ? "enabled" : "disabled");
		}
	}

	private void continueCallback(final String arguments) {
		if (arguments == null || arguments.isEmpty()) {
			switch (mainController.get_state()) {
			case MC_TERMINATING_TESTCASE:
			case MC_EXECUTING_CONTROL:
			case MC_EXECUTING_TESTCASE:
				System.out.println("Execution is not paused.");
				break;
			case MC_PAUSED:
				mainController.continue_testcase();
				break;
			default:
				System.out.println("Tests are not running.");
			}
		} else {
			helpCallback(MainControllerCommand.CONTINUE_TEXT);
		}
	}

	private void emtcCallback(final String arguments) {
		if (arguments == null || arguments.isEmpty()) {
			switch (mainController.get_state()) {
			case MC_LISTENING:
			case MC_LISTENING_CONFIGURED:
			case MC_HC_CONNECTED:
			case MC_ACTIVE:
				System.out.println("MTC does not exist.");
				break;
			case MC_READY:
				mainController.exit_mtc();
				waitMCState(waitStateEnum.WAIT_MTC_TERMINATED);
				break;
			default:
				System.out.println("MTC cannot be terminated.");
			}
		} else {
			helpCallback(MainControllerCommand.EMTC_TEXT);
		}
	}

	private void logCallback(final String arguments) {
		if (arguments != null && !arguments.isEmpty()) {
			if (arguments.equals("on")) {
				loggingEnabled = true;
				System.out.println("Console logging is enabled.");
			} else if (arguments.equals("off")) {
				loggingEnabled = false;
				System.out.println("Console logging is disabled.");
			} else {
				helpCallback(MainControllerCommand.LOG_TEXT);
			}
		} else {
			System.out.printf("Console logging is %s.\n", loggingEnabled ? "enabled" : "disabled");
		}
	}

	private void infoCallback(final String arguments) {
		if (arguments == null || arguments.isEmpty()) {
			printInfo();
		} else {
			helpCallback(MainControllerCommand.INFO_TEXT);
		}
	}

	//TODO: finish
	private void reconfCallback(final String arguments) {

	}

	private void helpCallback(final String arguments) {
		if (arguments == null || arguments.isEmpty()) {
			System.out.println("Help is available for the following commands:");
			for (MainControllerCommand command : command_list) {
				System.out.print(command.getName() + " ");
			}
			System.out.println();
		} else {
			for (MainControllerCommand command : command_list) {
				if (arguments.equals(command.getName())) {
					System.out.printf("%s usage: %s\n%s\n", command.getName(), command.getSynopsis(), command.getDescription());
					return;
				}
			}
			System.out.printf("No help for %s.\n", arguments);
		}
	}

	private void shellCallback(final String arguments) {

	}

	private void exitCallback(final String arguments) {
		if (arguments == null || arguments.isEmpty()) {
			switch (mainController.get_state()) {
			case MC_READY:
			case MC_RECONFIGURING:
				mainController.exit_mtc();
				waitMCState(waitStateEnum.WAIT_MTC_TERMINATED);
				break;
			case MC_LISTENING:
			case MC_LISTENING_CONFIGURED:
			case MC_HC_CONNECTED:
			case MC_ACTIVE:
				mainController.shutdown_session();
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
		// Empty by default
	}

	/**
	 * Print the welcome text.
	 */
	private static void printWelcome() {
		System.out.printf(
				"\n" + "*************************************************************************\n"
						+ "* TTCN-3 Test Executor - Main Controller 2                              *\n"
						+ "* Version: %-40s                     *\n"
						+ "* Copyright (c) 2000-2020 Ericsson Telecom AB                           *\n"
						+ "* All rights reserved. This program and the accompanying materials      *\n"
						+ "* are made available under the terms of the Eclipse Public License v2.0 *\n"
						+ "* which accompanies this distribution, and is available at              *\n"
						+ "* https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html            *\n"
						+ "*************************************************************************\n" + "\n",
						TTCN_Runtime.PRODUCT_NUMBER);
	}

	/**
	 * Print program usage information.
	 */
	private static void printUsage(final String prg_name) {
		System.err.printf("TTCN-3 Test Executor - Main Controller 2\n" + "Version: " + TTCN_Runtime.PRODUCT_NUMBER
				+ "\n\n" + "usage: %s configuration_file\n"
				+ "where: the 'configuration_file' parameter specifies the name and \n"
				+ "location of the main controller configuration file\n", prg_name);
	}

	/**
	 * The main cli event loop.
	 */
	private int interactiveMode() {
		if (mainController.start_session(mycfg.getLocal_addr(), mycfg.getTcp_listen_port()) == 0) {
			System.out.println("Initialization of TCP server failed. Exiting.");
			return EXIT_FAILURE; // EXIT_FAILURE
		}
		final BufferedReader console_reader = new BufferedReader(new InputStreamReader(System.in));

		do {
			try {
				System.out.print("MC2> ");
				final String line_read = console_reader.readLine();
				if (line_read != null) {
					processCommand(line_read.trim());
				} else {
					System.out.println("exit");
					exitCallback("");
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} while (!exitFlag);
		return 0; // EXIT_SUCCESS
	}

	/**
	 * Execution in batch mode.
	 */
	private int batchMode() {
		System.out.println(String.format("Entering batch mode. Waiting for %d HC%s to connect...", mycfg.getNum_hcs(),
				mycfg.getNum_hcs().compareTo(BigInteger.ONE) > 0 ? "s" : ""));
		if (mycfg.getExecuteItems() == null || mycfg.getExecuteItems().isEmpty()) {
			System.out.println("No [EXECUTE] section was given in the configuration file. Exiting.");
			return EXIT_FAILURE;
		}
		boolean error_flag = false;
		// start to listen on TCP port
		if (mainController.start_session(mycfg.getLocal_addr(), mycfg.getTcp_listen_port()) == 0) {
			System.out.println("Initialization of TCP server failed. Exiting.");
			return EXIT_FAILURE;
		}
		waitMCState(waitStateEnum.WAIT_HC_CONNECTED);
		// download config file
		mainController.configure(ConfigData.getConfigFileContent(config_file));
		waitMCState(waitStateEnum.WAIT_ACTIVE);
		if (mainController.get_state() != mcStateEnum.MC_ACTIVE) {
			System.out.println(mainController.get_state());
			System.out.println("Error during initialization. Cannot continue in batch mode.");
			error_flag = true;
		}

		if (!error_flag) {
			// create MTC on firstly connected HC
			mainController.create_mtc(mainController.get_hosts().get(0));
			waitMCState(waitStateEnum.WAIT_MTC_CREATED);
			if (mainController.get_state() != mcStateEnum.MC_READY) {
				System.out.println("Creation of MTC failed. Cannot continue in batch mode.");
				error_flag = true;
			}
		}
		if (!error_flag) {
			// execute each item of the list
			for (int i = 0; i < mycfg.getExecuteItems().size(); i++) {
				executeFromList(i);
				waitMCState(waitStateEnum.WAIT_MTC_READY);
				if (mainController.get_state() != mcStateEnum.MC_READY) {
					System.out.println("MTC terminated unexpectedly. Cannot continue in batch mode.");
					error_flag = true;
					break;
				}
			}
		}
		if (!error_flag) {
			// terminate the MTC
			mainController.exit_mtc();
			waitMCState(waitStateEnum.WAIT_MTC_TERMINATED);
		}
		// now MC must be in state MC_ACTIVE anyway
		// shutdown MC
		mainController.shutdown_session();
		waitMCState(waitStateEnum.WAIT_SHUTDOWN_COMPLETE);
		if (error_flag) {
			return EXIT_FAILURE; // EXIT_FAILURE
		} else {
			return 0; // EXIT_SUCCESS
		}
	}

	private boolean conditionHolds(final waitStateEnum askedState) {
		switch (askedState) {
		case WAIT_HC_CONNECTED:
			if (mainController.get_state() == mcStateEnum.MC_HC_CONNECTED) {
				if (mycfg.getNum_hcs().compareTo(BigInteger.ZERO) == 1) {
					return mainController.get_nof_hosts() == mycfg.getNum_hcs().intValue()
							|| mainController.get_nof_hosts() > mycfg.getNum_hcs().intValue();
				} else {
					return true;
				}
			} else {
				return false;
			}
		case WAIT_ACTIVE:
			switch (mainController.get_state()) {
			case MC_ACTIVE: // normal case
			case MC_HC_CONNECTED: // error happened with config file
			case MC_LISTENING: // even more strange situations
				return true;
			default:
				return false;
			}
		case WAIT_MTC_CREATED:
		case WAIT_MTC_READY:
			switch (mainController.get_state()) {
			case MC_READY: // normal case
			case MC_ACTIVE: // MTC crashed unexpectedly
			case MC_LISTENING_CONFIGURED: // MTC and all HCs are crashed at the same time
			case MC_HC_CONNECTED: // even more strange situations
				return true;
			default:
				return false;
			}
		case WAIT_MTC_TERMINATED:
			return mainController.get_state() == mcStateEnum.MC_ACTIVE;
		case WAIT_SHUTDOWN_COMPLETE:
			return mainController.get_state() == mcStateEnum.MC_INACTIVE;
		case WAIT_EXECUTE_LIST:
			if (mainController.get_state() == mcStateEnum.MC_READY) {
				++executeListIndex;
				if (executeListIndex < mycfg.getExecuteItems().size()) {
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

	private void waitMCState(final waitStateEnum newWaitState) {
		mutex.lock();
		if (newWaitState != waitStateEnum.WAIT_NOTHING) {
			if (conditionHolds(newWaitState)) {
				waitState = waitStateEnum.WAIT_NOTHING;
			} else {
				waitState = newWaitState;
				await();
			}
		} else {
			System.err.println("Cli.waitMCState: invalid argument");
			mutex.unlock();
			return;
		}
		mutex.unlock();
	}

	private int getHostIndex(final String hostname) {
		int found = -1;
		for (int i = 0; i < mainController.get_nof_hosts(); i++) {
			Host host = mainController.get_host_data(i);
			if (host != null) {
				if (!(host.hostname.equals(hostname)) || !(host.hostname_local.equals(hostname))) {
					mainController.release_data();
					if (found == -1) {
						found = i;
					} else {
						System.out.printf("Hostname %s is ambiguous.\n", hostname);
						found = -1;
						break;
					}
				} else {
					mainController.release_data();
				}
			} else {
				mainController.release_data();
				if (found == -1) {
					System.out.printf("No such host: %s.\n", hostname);
				}
				break;
			}
		}
		return found;
	}

	private void lock() {
		mutex.lock();
	}

	private void unlock() {
		mutex.unlock();
	}

	private void signal() {
		wakeup_condition.signal();
	}

	private void await() {
		try {
			wakeup_condition.await();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Process the command to perform the action accordingly.
	 */
	private void processCommand(final String line_read) {
		// Read nothing -> skip
		if (line_read == null || line_read.isEmpty()) {
			return;
		}

		final String[] splitted_line = line_read.split("\\s");
		final String command = splitted_line[0];
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
		} else if (command.equals(MainControllerCommand.EXIT_TEXT)
				|| command.equals(MainControllerCommand.EXIT_TEXT2)) {
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
	private void executeFromList(final int index) {
		if (index >= mycfg.getExecuteItems().size()) {
			System.err.println("Cli.executeFromList: invalid argument");
			return;
		}

		if (mycfg.getExecuteItems().get(index).getTestcaseName() == null) {
			mainController.execute_control(mycfg.getExecuteItems().get(index).getModuleName());
		} else if (!mycfg.getExecuteItems().get(index).getTestcaseName().equals("*")) {
			mainController.execute_testcase(mycfg.getExecuteItems().get(index).getModuleName(), null);
		} else {
			mainController.execute_testcase(mycfg.getExecuteItems().get(index).getModuleName(),
					mycfg.getExecuteItems().get(index).getTestcaseName());
		}
	}

	private String verdict2str(final VerdictTypeEnum verdict) {
		switch (verdict) {
		case NONE:
			return "none";
		case PASS:
			return "pass";
		case INCONC:
			return "inconc";
		case FAIL:
			return "fail";
		case ERROR:
			return "error";
		default:
			return "unknown";
		}
	}

	private void printInfo() {
		StringBuffer buffer = new StringBuffer();

		buffer.append("MC information:\n");
		buffer.append(String.format(" MC state: %s\n", MainController.get_mc_state_name(mainController.get_state())));
		buffer.append(" host information:\n");
		for (int i = 0; i < mainController.get_nof_hosts(); i++) {
			Host host = mainController.get_host_data(i);
			if (host != null) {
				buffer.append(String.format("  - %s", host.hostname));
				final String ip_addr = host.ip_address.getHostAddress();
				if (!ip_addr.equals(host.hostname)) {
					buffer.append(String.format(" [%s]", ip_addr));
				}
				if (!host.hostname.equals(host.hostname_local)) {
					buffer.append(String.format(" (%s)", host.hostname_local));
				}
				buffer.append(":\n");
				buffer.append(String.format("     operating system: %s %s on %s\n", host.system_name,
						host.system_release, host.machine_type));
				buffer.append(String.format("     HC state: %s\n", MainController.get_hc_state_name(host.hc_state)));
				buffer.append("     test component information:\n");
				final int n_components = host.components.size();
				mainController.release_data();
				for (int j = 0; j < n_components; j++) {
					ComponentStruct component = mainController.get_component_data(host.components.get(j));
					if (component != null) {
						if (component.comp_name != null) {
							buffer.append(String.format("      - name: %s, component reference: %d\n",
									component.comp_name, component.comp_ref));
						} else {
							buffer.append(String.format("      - component reference: %d\n", component.comp_ref));
						}
						if (component.comp_type != null && component.comp_type.definition_name != null) {
							buffer.append("         component type: ");
							if (component.comp_type.module_name != null) {
								buffer.append(String.format("%s.", component.comp_type.module_name));
							}
							buffer.append(String.format("%s\n", component.comp_type.definition_name));
						}
						buffer.append(String.format("         state: %s\n",
								MainController.get_tc_state_name(component.tc_state)));
						if (component.tc_fn_name != null && component.tc_fn_name.definition_name != null) {
							buffer.append(String.format("         executed %s: ",
									component.comp_ref == TitanComponent.MTC_COMPREF ? "test case" : "function"));
							if (component.tc_fn_name.module_name != null) {
								buffer.append(String.format("%s.", component.tc_fn_name.module_name));
							}
							buffer.append(String.format("%s\n", component.tc_fn_name.definition_name));
						}
						if (component.tc_state == tc_state_enum.TC_EXITING
								|| component.tc_state == tc_state_enum.TC_EXITED) {
							buffer.append(String.format("         local verdict: %s\n",
									verdict2str(component.local_verdict)));
						}
						mainController.release_data();
					}
				}

				if (n_components == 0) {
					buffer.append("      no components on this host\n");
				}
			} else {
				mainController.release_data();
				break;
			}
		}
		if (mainController.get_nof_hosts() == 0) {
			buffer.append("  no HCs are connected\n");
		}
		buffer.append(String.format(" pause function: %s\n",
				mainController.get_stop_after_testcase() ? "enabled" : "disabled"));
		buffer.append(String.format(" console logging: %s\n", loggingEnabled ? "enabled" : "disabled"));

		System.out.println(buffer.toString());
	}
}
