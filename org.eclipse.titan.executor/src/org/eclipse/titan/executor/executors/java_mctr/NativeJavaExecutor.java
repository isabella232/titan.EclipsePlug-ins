/******************************************************************************
 * Copyright (c) 2000-2020 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.executor.executors.java_mctr;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.common.parsers.cfg.CfgLexer;
import org.eclipse.titan.common.parsers.cfg.ConfigFileHandler;
import org.eclipse.titan.executor.Activator;
import org.eclipse.titan.executor.GeneralConstants;
import org.eclipse.titan.executor.TITANConsole;
import org.eclipse.titan.executor.designerconnection.EnvironmentHelper;
import org.eclipse.titan.executor.executors.BaseExecutor;
import org.eclipse.titan.executor.executors.ExecuteDialog;
import org.eclipse.titan.executor.executors.ExecuteDialog.ExecutableType;
import org.eclipse.titan.executor.executors.SeverityResolver;
import org.eclipse.titan.executor.jni.Timeval;
import org.eclipse.titan.executor.views.executormonitor.ComponentElement;
import org.eclipse.titan.executor.views.executormonitor.ExecutorMonitorView;
import org.eclipse.titan.executor.views.executormonitor.ExecutorStorage;
import org.eclipse.titan.executor.views.executormonitor.HostControllerElement;
import org.eclipse.titan.executor.views.executormonitor.InformationElement;
import org.eclipse.titan.executor.views.executormonitor.LaunchElement;
import org.eclipse.titan.executor.views.executormonitor.LaunchStorage;
import org.eclipse.titan.executor.views.executormonitor.MainControllerElement;
import org.eclipse.titan.executor.views.notification.Notification;
import org.eclipse.titan.executor.views.testexecution.ExecutedTestcase;
import org.eclipse.titan.executor.views.testexecution.TestExecutionView;
import org.eclipse.titan.runtime.core.TitanVerdictType;
import org.eclipse.titan.runtime.core.mctr.MainController;
import org.eclipse.titan.runtime.core.mctr.UserInterface;

/**
 * This executor handles the execution of tests compiled in a parallel mode, via the MainController written in Java.
 *
 * @author Kristof Szabados
 * */
public class NativeJavaExecutor extends BaseExecutor {
	private boolean startHCRequested = false;
	private boolean configureRequested = false;
	private int configFileExecutionRequestCounter = -1;
	private boolean createMTCRequested = false;
	private boolean executeRequested = false;
	private List<String> executeList = new ArrayList<String>();
	private boolean shutdownRequested = false;

	private boolean simpleExecutionRunning = false;
	private boolean isTerminated = false;

	private static boolean isRunning = false;
	private boolean loggingIsEnabled = true;
	private static final String EXECUTION_FINISHED = "^Test case (.*) finished\\. Verdict: (.*)$";
	private static final Pattern EXECUTION_FINISHED_PATTERN = Pattern.compile(EXECUTION_FINISHED);
	private final Matcher executionFinishedMatcher = EXECUTION_FINISHED_PATTERN.matcher("");
	private static final String REASON = "^(.*) reason: (.*)$";
	private static final Pattern REASON_PATTERN = Pattern.compile(REASON);
	private final Matcher reasonMatcher = REASON_PATTERN.matcher("");
	private static final String EMPTY_STRING = "";

	private Action automaticExecution, startSession, configure, startHCs, cmtc, smtc, generalPause, cont, stop, emtc, generalLogging,
			shutdownSession, info;

	private ConfigFileHandler configHandler = null;
	private MainController mainController = null;

	public static class NativeJavaUI extends UserInterface {
		private NativeJavaExecutor callback;

		public NativeJavaUI(final NativeJavaExecutor callback) {
			this.callback = callback;
		}

		@Override
		public void initialize() {
		}

		@Override
		public int enterLoop(final String[] args) {
			return 0;
		}

		@Override
		public void status_change() {
			callback.statusChangeCallback();
		}

		@Override
		public void error(final int severity, final String message) {
		}

		@Override
		public void notify(final long timestamp, final String source, final int severity, final String message) {
			callback.notifyCallback(new Timeval(timestamp / 1000, timestamp % 1000), source, severity, message);
		}

		@Override
		public void executeBatchFile(final String filename) {
		}
	}

	public NativeJavaExecutor(final ILaunchConfiguration configuration) throws CoreException {
		super(configuration);

		if (null == configFilePath) {
			final IStatus status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, IStatus.OK, CONFIGFILEPATH_NULL, null);
			throw new CoreException(status);
		}

		automaticExecution = new Action("automatic execution") {
			@Override
			public void run() {
				simpleExecutionRunning = true;
				startTest(false);
			}
		};
		automaticExecution.setToolTipText("automatic execution");

		startSession = new Action("Start session") {
			@Override
			public void run() {
				initialization();
			}
		};
		startSession.setToolTipText("Start session");

		startHCs = new Action("Start HCs") {
			@Override
			public void run() {
				startHostControllers();
			}
		};
		startHCs.setToolTipText("Start HCs");

		configure = new Action("Set parameters") {
			@Override
			public void run() {
				configure();
			}
		};
		configure.setToolTipText("Set parameters");

		cmtc = new Action("create MTC") {
			@Override
			public void run() {
				createMTC();
			}
		};
		cmtc.setToolTipText("create MTC");

		smtc = new Action("Execute..") {
			@Override
			public void run() {
				startTest(false);
			}
		};
		smtc.setToolTipText("Execute..");

		generalPause = new Action("Pause execution", IAction.AS_CHECK_BOX) {
			@Override
			public void run() {
				mainController.stop_after_testcase(generalPause.isChecked());
			}
		};
		generalPause.setToolTipText("Pause execution");
		generalPause.setChecked(false);

		cont = new Action("Continue execution") {
			@Override
			public void run() {
				mainController.continue_testcase();
			}
		};
		cont.setToolTipText("Continue execution");

		stop = new Action("Stop execution") {
			@Override
			public void run() {
				stop();
			}
		};
		stop.setToolTipText("Stop execution");

		emtc = new Action("Exit MTC") {
			@Override
			public void run() {
				exitMTC();
			}
		};
		emtc.setToolTipText("Exit MTC");

		generalLogging = new Action("Generate console log") {
			@Override
			public void run() {
				if (generalLogging.isChecked()) {
					loggingIsEnabled = true;
				} else {
					loggingIsEnabled = false;
				}
			}
		};
		generalLogging.setToolTipText("Console logging");
		generalLogging.setChecked(true);

		shutdownSession = new Action("Shutdown session") {
			@Override
			public void run() {
				shutdownSession();
			}
		};
		shutdownSession.setToolTipText("Shutdown session");

		info = new Action("Update status information") {
			@Override
			public void run() {
				updateInfoDisplay();
			}
		};
		info.setToolTipText("Updates the status displaying hierarchy");

		NativeJavaUI tempUI = new NativeJavaUI(this);
		mainController = new MainController();
		mainController.initialize(tempUI, 1500);// mx_ptcs

		setRunning(true);

		isTerminated = false;
		loggingIsEnabled = true;
		updateGUI();
	}

	public static boolean isRunning() {
		return isRunning;
	}

	private static void setRunning(final boolean newValue) {
		isRunning = newValue;
	}

	/**
	 * Initializes the Executor.
	 *
	 * @param launch
	 *                the ILaunch instance to start the session with.
	 * */
	@Override
	public void startSession(final ILaunch launch) {
		super.startSession(launch);

		if (automaticExecuteSectionExecution) {
			if (!LaunchStorage.getLaunchElementMap().containsKey(launch)) {
				final ILaunchConfiguration launchConfiguration = launch.getLaunchConfiguration();
				final LaunchElement launchElement = new LaunchElement(launchConfiguration.getName(), launch);
				LaunchStorage.registerLaunchElement(launchElement);
				ExecutorStorage.registerExecutorStorage(launchElement);
			}

			simpleExecutionRunning = true;
			startTest(true);
		}
	}

	@Override
	public MenuManager createMenu(final MenuManager manager) {
		if (!isTerminated) {
			manager.add(automaticExecution);
			manager.add(new Separator());
			manager.add(startSession);
			manager.add(configure);
			manager.add(startHCs);
			manager.add(cmtc);
			manager.add(smtc);
			manager.add(generalPause);
			manager.add(cont);
			manager.add(stop);
			manager.add(emtc);
			manager.add(shutdownSession);
			manager.add(generalLogging);
			manager.add(info);
		}
		return super.createMenu(manager);
	}

	/**
	 * Inserts an error message into the notifications view.
	 *
	 * @param severity
	 *                the severity of the message
	 * @param msg
	 *                the message to be shown
	 * */
	public void insertError(final int severity, final String msg) {
		TITANConsole.println("Error: " + msg);

		if (severityLevelExtraction) {
			addNotification(new Notification((new Formatter()).format(PADDEDDATETIMEFORMAT, new Date()).toString(),
					SeverityResolver.getSeverityString(severity), EMPTY_STRING, msg));
		} else {
			addNotification(new Notification((new Formatter()).format(PADDEDDATETIMEFORMAT, new Date()).toString(), EMPTY_STRING, EMPTY_STRING, msg));
		}
		if (simpleExecutionRunning) {
			shutdownRequested = true;
		}
	}

	/**
	 * Inserts an error message into the notifications view.
	 *
	 * @param severity
	 *                the severity of the message
	 * @param msg
	 *                the message to be shown
	 * */
	public void errorCallback(final int severity, final String msg) {
		insertError(severity, msg);

		if (null != Activator.getMainView()) {
			Activator.getMainView().refreshIfSelected(mainControllerRoot);
		}
	}

	/**
	 * Inserts a lists of messages into the notifications view in a batched
	 * manner
	 * <p>
	 * A list of String arrays issued to store every data reported regarding
	 * the message in a undecoded way. On this way if a data is not needed
	 * we don't need to decode it.
	 *
	 * @param s
	 *                the list of String arrays.
	 * */
	public void batchedInsertNotify(final ArrayList<String[]> s) {
		if (loggingIsEnabled && consoleLogging) {
			for (final String[] sv : s) {
				TITANConsole.println(sv[2] + ": " + sv[4]);
			}
		}

		final List<String> times = new ArrayList<String>(s.size());
		final List<Notification> tempNotifications = new ArrayList<Notification>(s.size());

		if (severityLevelExtraction) {
			int severity;
			for (final String[] value : s) {
				severity = Integer.parseInt(value[3]);
				final Formatter formatter = new Formatter();
				formatter.format(DATETIMEFORMAT, new Date(Long.parseLong(value[0]) * 1000), Long.valueOf(value[1]));
				times.add(formatter.toString());
				tempNotifications.add(new Notification(formatter.toString(), SeverityResolver.getSeverityString(severity), value[2], value[4]));
				formatter.close();
			}
		} else {
			for (final String[] value : s) {
				final Formatter formatter = new Formatter();
				formatter.format(DATETIMEFORMAT, new Date(Long.parseLong(value[0]) * 1000), Long.valueOf(value[1]));
				times.add(formatter.toString());
				tempNotifications.add(new Notification(formatter.toString(), EMPTY_STRING, value[2], value[4]));
				formatter.close();
			}
		}
		addNotifications(tempNotifications);
		if (verdictExtraction) {
			for (int i = 0; i < s.size(); i++) {
				if (executionFinishedMatcher.reset(s.get(i)[4]).matches()) {
					final String reason = executionFinishedMatcher.group(2);
					final String timestamp = times.get(i);
					if (reasonMatcher.reset(reason).matches()) {
						executedTests.add(new ExecutedTestcase(timestamp, executionFinishedMatcher.group(1), reasonMatcher.group(1), reasonMatcher.group(2)));
					} else {
						executedTests
						.add(new ExecutedTestcase(timestamp, executionFinishedMatcher.group(1), executionFinishedMatcher.group(2), ""));
					}
				}
			}
		}
	}

	/**
	 * Inserts a notification message into the notifications view.
	 *
	 * @param time
	 *                the MainController reported time, when the
	 *                notification message was created
	 * @param source
	 *                the source line info of the notification message
	 * @param severity
	 *                the severity of the message
	 * @param msg
	 *                the message to be shown
	 * */
	public void insertNotify(final Timeval time, final String source, final int severity, final String msg) {
		if (loggingIsEnabled && consoleLogging) {
			TITANConsole.println(source + ": " + msg);
		}

		final Formatter formatter = new Formatter();
		formatter.format(DATETIMEFORMAT, new Date(time.tv_sec * 1000), time.tv_usec);
		final String timestamp = formatter.toString();
		formatter.close();

		if (severityLevelExtraction) {
			addNotification(new Notification(timestamp, SeverityResolver.getSeverityString(severity), source, msg));
		} else {
			addNotification(new Notification(timestamp, EMPTY_STRING, source, msg));
		}

		if (verdictExtraction && executionFinishedMatcher.reset(msg).matches()) {
			final String reason = executionFinishedMatcher.group(2);
			if (reasonMatcher.reset(reason).matches()) {
				executedTests.add(new ExecutedTestcase(timestamp, executionFinishedMatcher.group(1), reasonMatcher.group(1), reasonMatcher.group(2)));
			} else {
				executedTests.add(new ExecutedTestcase(timestamp, executionFinishedMatcher.group(1), executionFinishedMatcher.group(2), ""));
			}
		}
	}

	/**
	 * Inserts a notification message into the notifications view.
	 *
	 * @param time
	 *                the MainController reported time, when the
	 *                notification message was created
	 * @param source
	 *                the source line info of the notification message
	 * @param severity
	 *                the severity of the message
	 * @param msg
	 *                the message to be shown
	 * */
	public void notifyCallback(final Timeval time, final String source, final int severity, final String msg) {
		insertNotify(time, source, severity, msg);

		if (Activator.getMainView() != null) {
			Activator.getMainView().refreshIfSelected(mainControllerRoot);
		} else {
			TestExecutionView.refreshInput(this);
		}
	}

	/**
	 * Handles a status change reported by the MainController.
	 * */
	public void statusChangeCallback() {
		final MainController.mcStateEnum state = mainController.get_state();
		switch (state) {
		case MC_LISTENING:
		case MC_LISTENING_CONFIGURED:
			break;
		case MC_HC_CONNECTED:
			if (shutdownRequested) {
				shutdownSession();
			} else if (configureRequested) {
				configure();
			}
			break;
		case MC_ACTIVE:
			if (createMTCRequested) {
				createMTC();
			} else if (shutdownRequested) {
				shutdownSession();
			}
			break;
		case MC_READY:
			if (executeList.isEmpty()) {
				executeRequested = false;
			}
			if (executeRequested) {
				executeNextTest();
			} else if (simpleExecutionRunning || shutdownRequested) {
				shutdownSession();
			}
			break;
		case MC_INACTIVE:
			if (shutdownRequested) {
				shutdownRequested = false;
				// session shutdown is finished (requested by jnimw.shutdown_session())
				//TODO: delete user interface?
				terminate(false);
				executeList.clear();

				disposeHostControllers();
			}
			break;
		case MC_SHUTDOWN:
			break;
		default:
		}

		updateGUI();
	}

	/**
	 * Initializes the test session loading the configuration file if
	 * provided.
	 * <p>
	 * If automatic execution is selected the HostControllers are started as
	 * the last step
	 * <p>
	 * This is called startSession in mctr_gui
	 */
	private void initialization() {
		configHandler = null;
		int tcpport = 0;
		String localAddress = null;

		if ((new File(configFilePath)).exists()) {
			configHandler = readConfigFile();

			final Map<String, String> env = new HashMap<String, String>(System.getenv());
			if (!appendEnvironmentalVariables) {
				env.clear();
			}

			if (environmentalVariables != null) {
				try {
					EnvironmentHelper.resolveVariables(env, environmentalVariables);
				} catch (CoreException e) {
					ErrorReporter.logExceptionStackTrace(e);
				}
			}

			EnvironmentHelper.setTitanPath(env);
			EnvironmentHelper.set_LICENSE_FILE_PATH(env);

			if (configHandler == null) {
				ErrorReporter.parallelErrorDisplayInMessageDialog(
						"An error was found while processing the configuration file",
						"Please refer to the Error Log view for further information.");
				return;
			} else if (configHandler.isErroneous()) {

				if (configHandler.parseExceptions().isEmpty()) {
					ErrorReporter.parallelErrorDisplayInMessageDialog(
							"An error was found while processing the configuration file",
							"Please refer to the Error Log view for further information.");
				} else {
					final Throwable exception = configHandler.parseExceptions().get(configHandler.parseExceptions().size() - 1);
					ErrorReporter.parallelErrorDisplayInMessageDialog(
							"Error while processing the configuration file",
							exception.getMessage() + "\n Please refer to the Error Log view for further information.");
				}
				return;
			}

			tcpport = configHandler.getTcpPort();
			final double killTimer = configHandler.getKillTimer();
			localAddress = configHandler.getLocalAddress();

			mainController.set_kill_timer(killTimer);
			mainController.destroy_host_groups();

			final Map<String, String[]> groups = configHandler.getGroups();
			final Map<String, String> components = configHandler.getComponents();

			for (final Map.Entry<String, String[]> group : groups.entrySet()) {
				for (final String hostName : group.getValue()) {
					mainController.add_host(group.getKey(), hostName);
				}
			}

			for (final Map.Entry<String, String> component : components.entrySet()) {
				mainController.assign_component(component.getValue(), component.getKey());
			}
		}

		if (localAddress != null && !EMPTY_STRING.equals(localAddress) && 0 == tcpport) {
			final Formatter formatter = new Formatter();
			addNotification(new Notification(formatter.format(PADDEDDATETIMEFORMAT, new Date()).toString(), EMPTY_STRING, EMPTY_STRING,
					"If LocalAddress is specified you must also set the TCPPort in the configuration file: " + configFilePath));
			formatter.close();

			ErrorReporter.parallelErrorDisplayInMessageDialog(
					"Error in the configuration",
					"If LocalAddress is specified you must also set the TCPPort in the configuration file: " + configFilePath);
			shutdownSession();
			return;
		}

		if (localAddress == null) {
			// FIXME temporal change to enable efficient work.
			localAddress = "127.0.0.1";
		}

		mcHost = localAddress;
		final int port = mainController.start_session(localAddress, tcpport);
		if (port == 0) {
			// there were some errors starting the session
			shutdownSession();
			return;
		}
		mcPort = EMPTY_STRING + port;

		if (configFileExecutionRequestCounter != -1 && configHandler != null) {
			for (int i = 0; i < configFileExecutionRequestCounter; i++) {
				executeList.addAll(configHandler.getExecuteElements());
			}
			configFileExecutionRequestCounter = -1;
		}

		if (startHCRequested) {
			startHC();
		}
	}

	/**
	 * Starts the HostControllers.
	 * <p>
	 * If the sessions initialization was not done, its done now.
	 * */
	private void startHC() {
		startHCRequested = true;
		final MainController.mcStateEnum state = mainController.get_state();
		if (MainController.mcStateEnum.MC_LISTENING != state && MainController.mcStateEnum.MC_LISTENING_CONFIGURED != state) {
			initialization();
			return;
		}

		startHostControllers();
		startHCRequested = false;
	}

	/**
	 * Configures the HostControllers.
	 * <p>
	 * They are also started if no HostControler was connected.
	 * */
	private void configure() {
		configureRequested = true;
		final MainController.mcStateEnum state = mainController.get_state();
		if (MainController.mcStateEnum.MC_HC_CONNECTED != state && MainController.mcStateEnum.MC_ACTIVE != state) {
			startHC();
			return;
		}

		mainController.configure(generateCfgString());

		configureRequested = false;
	}

	/**
	 * Creates the MainTestComponent.
	 * <p>
	 * If the HostControllers are not configured that is also done before
	 * creating the MainTestComponent.
	 * */
	private void createMTC() {
		createMTCRequested = true;
		final MainController.mcStateEnum state = mainController.get_state();
		if (MainController.mcStateEnum.MC_ACTIVE != state) {
			configure();
			return;
		}

		/* this is not a constant null, it just so happens to trick your eyes */
		MainController.Host host = mainController.get_host_data(0);
		mainController.release_data();
		mainController.create_mtc(host);
		createMTCRequested = false;
	}

	/**
	 * Initializes and displays a dialog to the user. If the user selected
	 * an executable element, it is also started here.
	 * <p>
	 * If the MainTestComponent is not yet created it is done before the
	 * execution starts.
	 *
	 * @param automaticExecution
	 *                true if the execution should not be done in
	 *                step-by-step.
	 * */
	private void startTest(final boolean automaticExecution) {
		boolean invalidSelection = false;
		do {
			if (automaticExecution && configFilePath != null && configFilePath.length() != 0 && !invalidSelection) {
				lastTimeSelection = "configuration file";
				lastTimeSelectionTime = 1;
				lastTimeSelectionType = ExecutableType.CONFIGURATIONFILE;
			} else {
				Display.getDefault().syncExec(new Runnable() {
					@Override
					public void run() {
						final ExecuteDialog dialog = new ExecuteDialog(null);
						dialog.setControlparts(availableControlParts);
						dialog.setTestcases(availableTestcases);
						dialog.setTestsets(availableTestSetNames);
						if (configFilePath != null) {
							dialog.setConfigurationFile(configFilePath);
						}
						dialog.setSelection(lastTimeSelection, lastTimeSelectionTime, lastTimeSelectionType);

						if (dialog.open() != Window.OK) {
							executionStarted = false;
							shutdownSession();
							return;
						}

						lastTimeSelection = dialog.getSelectedElement();
						lastTimeSelectionTime = dialog.getSelectionTimes();
						lastTimeSelectionType = dialog.getSelectionType();
					}
				});
			}

			switch (lastTimeSelectionType) {
			case TESTCASE:
				invalidSelection = false;
				for (int i = 0; i < lastTimeSelectionTime; i++) {
					executeList.add(lastTimeSelection);
				}
				executedTests.ensureCapacity(executedTests.size() + lastTimeSelectionTime);
				break;
			case TESTSET:
				invalidSelection = false;
				for (int i = 0, size = availableTestSetNames.size(); i < size; i++) {
					if (availableTestSetNames.get(i).equals(lastTimeSelection)) {
						for (int j = 0; j < lastTimeSelectionTime; j++) {
							executeList.addAll(availableTestSetContents.get(i));
						}
						executedTests.ensureCapacity(executedTests.size() + lastTimeSelectionTime * availableTestSetContents.get(i).size());
					}
				}
				break;
			case CONTROLPART:
				invalidSelection = false;
				for (int i = 0; i < lastTimeSelectionTime; i++) {
					executeList.add(lastTimeSelection);
				}
				executedTests.ensureCapacity(executedTests.size() + lastTimeSelectionTime * 5);
				break;
			case CONFIGURATIONFILE:
				if (configHandler == null) {
					configFileExecutionRequestCounter = lastTimeSelectionTime;
					invalidSelection = false;
				} else {
					final List<String> configurationFileElements = configHandler.getExecuteElements();
					if (configurationFileElements.isEmpty()) {
						invalidSelection = true;
						Display.getDefault().syncExec(new EmptyExecutionRunnable());
					} else {
						invalidSelection = false;
						for (int i = 0; i < lastTimeSelectionTime; i++) {
							executeList.addAll(configurationFileElements);
						}
					}
				}
				executedTests.ensureCapacity(executedTests.size() + lastTimeSelectionTime);
				break;
			default:
				break;
			}
		} while (invalidSelection);

		executionStarted = true;
		executeRequested = true;
		final MainController.mcStateEnum state = mainController.get_state();
		if (MainController.mcStateEnum.MC_READY == state) {
			executeNextTest();
		} else {
			createMTC();
		}

		saveLastTimeUsageInfo();
	}

	/**
	 * Executes the next testcase.
	 * <p>
	 * Also creates the MainTestComponent if needed.
	 * */
	private void executeNextTest() {
		final MainController.mcStateEnum state = mainController.get_state();
		if (MainController.mcStateEnum.MC_READY != state) {
			createMTC();
			return;
		}

		final String testElement = executeList.remove(0);
		final int i = testElement.indexOf('.');
		if (i != -1) {
			if ("control".equals(testElement.substring(i + 1))) {
				mainController.execute_control(testElement.substring(0, i));
			} else {
				mainController.execute_testcase(testElement.substring(0, i), testElement.substring(i + 1));
			}
		} else {
			mainController.execute_control(testElement);
		}
	}

	/**
	 * Stops the execution.
	 * */
	private void stop() {
		executeList.clear();
		executeRequested = false;
		mainController.stop_execution();
	}

	/**
	 * Exits the MainTestComponent
	 * <p>
	 * Also stops the execution if it was not done yet.
	 * */
	private void exitMTC() {
		final MainController.mcStateEnum state = mainController.get_state();
		if (MainController.mcStateEnum.MC_EXECUTING_CONTROL == state || MainController.mcStateEnum.MC_EXECUTING_TESTCASE == state
				|| MainController.mcStateEnum.MC_PAUSED == state) {
			stop();
			return;
		}

		if (MainController.mcStateEnum.MC_READY != state) {
			return;
		}

		mainController.exit_mtc();
	}

	/**
	 * Shuts down the session.
	 * <p>
	 * Also exits the MainTestComponent if not yet done
	 * */
	@Override
	protected void shutdownSession() {
		shutdownRequested = true;
		simpleExecutionRunning = false;
		final MainController.mcStateEnum state = mainController.get_state();
		if (MainController.mcStateEnum.MC_LISTENING == state || MainController.mcStateEnum.MC_LISTENING_CONFIGURED == state
				|| MainController.mcStateEnum.MC_HC_CONNECTED == state || MainController.mcStateEnum.MC_ACTIVE == state) {
			mainController.shutdown_session();
			// jnimw.terminate_internal() must be also called when shutdown is finished, see statusChangeCallback() case MC_INACTIVE
			startHCRequested = false;
			configureRequested = false;
			createMTCRequested = false;
			executeRequested = false;
		} else {
			exitMTC();
		}

		super.shutdownSession();
	}

	/**
	 * Updates the information displayed about the MainController's and
	 * HostControllers actual states.
	 * */
	private void updateInfoDisplay() {
		final MainController.mcStateEnum mcState = mainController.get_state();
		final MainControllerElement tempRoot = new MainControllerElement("Temporal root", this);
		final String mcStateName = MainController.get_mc_state_name(mcState);
		tempRoot.setStateInfo(new InformationElement("state: " + mcStateName));

		HostControllerElement tempHost;
		MainController.ComponentStruct comp;
		MainController.QualifiedName qualifiedName;
		ComponentElement tempComponent;
		StringBuilder builder;

		final int nofHosts = mainController.get_nof_hosts();
		MainController.Host host;
		for (int i = 0; i < nofHosts; i++) {
			host = mainController.get_host_data(i);

			tempHost = new HostControllerElement("Host Controller: ");
			tempRoot.addHostController(tempHost);
			tempHost.setIPAddressInfo(new InformationElement("IP address: " + host.hostname));
			tempHost.setIPNumberInfo(new InformationElement("IP number: " + host.ip_address.getHostAddress()));
			tempHost.setHostNameInfo(new InformationElement("Local host name:" + host.hostname_local));

			tempHost.setOperatingSystemInfo(new InformationElement(host.system_name + " " + host.system_release + " " + host.system_version));
			tempHost.setStateInfo(new InformationElement("State: " + MainController.get_hc_state_name(host.hc_state)));

			final int activeComponents = host.components.size();

			final List<Integer> components = host.components;
			mainController.release_data();
			for (int component_index = 0; component_index < activeComponents; component_index++) {
				comp = mainController.get_component_data(components.get(component_index));
				tempComponent = new ComponentElement("Component: " + comp.comp_name, new InformationElement("Component reference: " + comp.comp_ref));
				tempHost.addComponent(tempComponent);

				qualifiedName = comp.comp_type;
				if (qualifiedName != null && qualifiedName.definition_name != null) {
					builder = new StringBuilder("Component type: ");
					if (qualifiedName.module_name != null) {
						builder.append(qualifiedName.module_name).append('.');
					}
					builder.append(qualifiedName.definition_name);
					tempComponent.setTypeInfo(new InformationElement(builder.toString()));
				}

				tempComponent.setStateInfo(new InformationElement(MainController.get_tc_state_name(comp.tc_state)));

				qualifiedName = comp.tc_fn_name;
				if (qualifiedName != null && qualifiedName.definition_name != null) {
					builder = new StringBuilder(comp.comp_ref == 1 ? "test case" : "function");
					if (qualifiedName.module_name != null) {
						builder.append(qualifiedName.module_name).append('.');
					}
					builder.append(qualifiedName.definition_name);
					tempComponent.setExecutedInfo(new InformationElement(builder.toString()));
				}

				final TitanVerdictType.VerdictTypeEnum localVerdict = comp.local_verdict;
				if (localVerdict != null) {
					builder = new StringBuilder("local verdict: ");
					builder.append(localVerdict.getName());
				}

				mainController.release_data();
			}
		}

		if (mainControllerRoot != null) {
			mainControllerRoot.children().clear();
			mainControllerRoot.transferData(tempRoot);
		}

		final ExecutorMonitorView view = Activator.getMainView();
		if (view != null) {
			view.refreshAll();
		}
	}

	/**
	 * This function changes the status of the user interface elements.
	 */
	private void updateGUI() {
		final MainController.mcStateEnum stateValue = mainController.get_state();

		automaticExecution.setEnabled(!isTerminated && executeList.isEmpty());
		startSession.setEnabled(!isTerminated && MainController.mcStateEnum.MC_INACTIVE == stateValue);
		configure.setEnabled(MainController.mcStateEnum.MC_LISTENING == stateValue
				|| MainController.mcStateEnum.MC_LISTENING_CONFIGURED == stateValue
				|| MainController.mcStateEnum.MC_HC_CONNECTED == stateValue || MainController.mcStateEnum.MC_ACTIVE == stateValue);
		startHCs.setEnabled(MainController.mcStateEnum.MC_LISTENING == stateValue
				|| MainController.mcStateEnum.MC_LISTENING_CONFIGURED == stateValue);
		cmtc.setEnabled(MainController.mcStateEnum.MC_LISTENING == stateValue
				|| MainController.mcStateEnum.MC_LISTENING_CONFIGURED == stateValue
				|| MainController.mcStateEnum.MC_HC_CONNECTED == stateValue || MainController.mcStateEnum.MC_ACTIVE == stateValue);
		smtc.setEnabled(MainController.mcStateEnum.MC_READY == stateValue);
		cont.setEnabled(MainController.mcStateEnum.MC_PAUSED == stateValue);
		stop.setEnabled(MainController.mcStateEnum.MC_EXECUTING_CONTROL == stateValue
				|| MainController.mcStateEnum.MC_EXECUTING_TESTCASE == stateValue
				|| MainController.mcStateEnum.MC_PAUSED == stateValue
				|| MainController.mcStateEnum.MC_TERMINATING_TESTCASE == stateValue);
		emtc.setEnabled(MainController.mcStateEnum.MC_READY == stateValue);
		shutdownSession.setEnabled(MainController.mcStateEnum.MC_READY == stateValue || MainController.mcStateEnum.MC_LISTENING == stateValue
				|| MainController.mcStateEnum.MC_LISTENING_CONFIGURED == stateValue
				|| MainController.mcStateEnum.MC_HC_CONNECTED == stateValue || MainController.mcStateEnum.MC_ACTIVE == stateValue);

		generalPause.setEnabled(MainController.mcStateEnum.MC_ACTIVE == stateValue || MainController.mcStateEnum.MC_READY == stateValue
				|| MainController.mcStateEnum.MC_EXECUTING_CONTROL == stateValue
				|| MainController.mcStateEnum.MC_EXECUTING_TESTCASE == stateValue
				|| MainController.mcStateEnum.MC_PAUSED == stateValue);
		generalPause.setChecked(mainController.get_stop_after_testcase());
		generalLogging.setEnabled(MainController.mcStateEnum.MC_ACTIVE == stateValue || MainController.mcStateEnum.MC_READY == stateValue
				|| MainController.mcStateEnum.MC_EXECUTING_CONTROL == stateValue
				|| MainController.mcStateEnum.MC_EXECUTING_TESTCASE == stateValue
				|| MainController.mcStateEnum.MC_PAUSED == stateValue);
		info.setEnabled(MainController.mcStateEnum.MC_ACTIVE == stateValue || MainController.mcStateEnum.MC_READY == stateValue
				|| MainController.mcStateEnum.MC_EXECUTING_CONTROL == stateValue
				|| MainController.mcStateEnum.MC_EXECUTING_TESTCASE == stateValue
				|| MainController.mcStateEnum.MC_PAUSED == stateValue || MainController.mcStateEnum.MC_HC_CONNECTED == stateValue);

		final ExecutorMonitorView mainView = Activator.getMainView();
		if (mainView != null) {
			mainView.refreshIfSelected(mainControllerRoot);
		}
	}

	@Override
	public boolean isTerminated() {
		return isTerminated;
	}

	@Override
	public IProcess getProcess() {
		return null;
	}

	@Override
	public void terminate(final boolean external) {
		final MainController.mcStateEnum state = mainController.get_state();

		if (MainController.mcStateEnum.MC_INACTIVE == state) {
			setRunning(false);
			isTerminated = true;
			if (mainControllerRoot != null) {
				mainControllerRoot.setTerminated();
				LaunchElement launchElement = null;
				for (final Map.Entry<ILaunch, BaseExecutor> entry : ExecutorStorage.getExecutorMap().entrySet()) {
					if (entry.getValue().equals(mainControllerRoot.executor())
							&& LaunchStorage.getLaunchElementMap().containsKey(entry.getKey())) {
						launchElement = LaunchStorage.getLaunchElementMap().get(entry.getKey());
					}
				}
				if (launchElement != null) {
					launchElement.setTerminated();
				}
				if (Activator.getMainView() != null) {
					Activator.getMainView().refreshAll();
				}
			}
		} else {
			shutdownSession();
		}

		updateGUI();
	}

	@Override
	protected String getDefaultLogFileName() {
		return GeneralConstants.DEFAULT_LOGFILENAME_PARALLEL;
	}

	@Override
	protected String generateCfgString() {
		String result = super.generateCfgString();

		if (configHandler != null) {
			final List<Integer> disallowedNodes = new ArrayList<Integer>();

			disallowedNodes.add(CfgLexer.MAIN_CONTROLLER_SECTION);
			disallowedNodes.add(CfgLexer.DEFINE_SECTION);
			disallowedNodes.add(CfgLexer.INCLUDE_SECTION);
			disallowedNodes.add(CfgLexer.COMPONENTS_SECTION);
			disallowedNodes.add(CfgLexer.GROUPS_SECTION);
			disallowedNodes.add(CfgLexer.EXECUTE_SECTION);

			result += configHandler.toStringResolved(disallowedNodes);
		}
		return result;
	}
}
