/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.MessageFormat;
import java.util.ArrayList;

import org.eclipse.titan.runtime.core.TitanLoggerApi.ParPort_operation;
import org.eclipse.titan.runtime.core.TitanLoggerApi.ParallelPTC_reason;
import org.eclipse.titan.runtime.core.TitanVerdictType.VerdictTypeEnum;
import org.eclipse.titan.runtime.core.TtcnLogger.Severity;

/**
 * TTCN-3 runtime class
 *
 * TODO: lots to implement
 * TODO: reorganize according to .hh
 * 
 * INFO: the current threads + threadlocals are temporary structure to get enough functionality to work.
 * Once the business logic is running stable, we will experiment with this a bit more.
 *
 * @author Kristof Szabados
 */
public final class TTCN_Runtime {
	public static final int TTCN3_MAJOR = 6;
	public static final int TTCN3_MINOR = 4;
	public static final int TTCN3_PATCHLEVEL = 0;
	public static final int TTCN3_BUILDNUMBER = 0;

	public static final String PRODUCT_NUMBER = "CRL 113 200/6 R4A";

	public enum executorStateEnum {
		UNDEFINED_STATE,

		SINGLE_CONTROLPART, SINGLE_TESTCASE,

		HC_INITIAL, HC_IDLE, HC_CONFIGURING, HC_ACTIVE, HC_OVERLOADED,  HC_OVERLOADED_TIMEOUT, HC_EXIT,

		MTC_INITIAL, MTC_IDLE, MTC_CONTROLPART, MTC_TESTCASE,
		MTC_TERMINATING_TESTCASE, MTC_TERMINATING_EXECUTION, MTC_PAUSED,
		MTC_CREATE, MTC_START, MTC_STOP, MTC_KILL, MTC_RUNNING, MTC_ALIVE,
		MTC_DONE, MTC_KILLED, MTC_CONNECT, MTC_DISCONNECT, MTC_MAP, MTC_UNMAP,
		MTC_CONFIGURING, MTC_EXIT,

		PTC_INITIAL, PTC_IDLE, PTC_FUNCTION, PTC_CREATE, PTC_START, PTC_STOP,
		PTC_KILL, PTC_RUNNING, PTC_ALIVE, PTC_DONE, PTC_KILLED, PTC_CONNECT,
		PTC_DISCONNECT, PTC_MAP, PTC_UNMAP, PTC_STOPPED, PTC_EXIT
	}
	private static ThreadLocal<executorStateEnum> executorState = new ThreadLocal<TTCN_Runtime.executorStateEnum>() {
		@Override
		protected executorStateEnum initialValue() {
			return executorStateEnum.UNDEFINED_STATE;
		}
	};

	private static ThreadLocal<String> component_type_module = new ThreadLocal<String>();
	private static ThreadLocal<String> component_type_name = new ThreadLocal<String>();
	private static ThreadLocal<String> component_name = new ThreadLocal<String>();
	private static ThreadLocal<Boolean> is_alive = new ThreadLocal<Boolean>() {
		@Override
		protected Boolean initialValue() {
			return Boolean.FALSE;
		}
	}; 

	private static String control_module_name = null;

	//originally testcase_name
	private static ThreadLocal<String> testcaseModuleName = new ThreadLocal<String>();
	private static ThreadLocal<String> testcaseDefinitionName = new ThreadLocal<String>();

	private static VerdictTypeEnum localVerdict = VerdictTypeEnum.NONE;
	private static int verdictCount[] = new int[] {0,0,0,0,0};
	private static int controlErrorCount = 0;
	private static String verdictReason = "";

	//in the compiler in_ttcn_try_block
	private static int ttcn_try_block_counter = 0;

	private static ThreadLocal<Integer> create_done_killed_compref = new ThreadLocal<Integer>() {
		@Override
		protected Integer initialValue() {
			return TitanComponent.NULL_COMPREF;
		}
	};
	private static ThreadLocal<Boolean> running_alive_result = new ThreadLocal<Boolean>() {
		@Override
		protected Boolean initialValue() {
			return Boolean.FALSE;
		}
	};

	//MTC only static information
	private static TitanAlt_Status any_component_done_status = TitanAlt_Status.ALT_UNCHECKED;
	private static TitanAlt_Status all_component_done_status = TitanAlt_Status.ALT_UNCHECKED;
	private static TitanAlt_Status any_component_killed_status = TitanAlt_Status.ALT_UNCHECKED;
	private static TitanAlt_Status all_component_killed_status = TitanAlt_Status.ALT_UNCHECKED;

	private static class component_status_table_struct {
		TitanAlt_Status done_status;
		TitanAlt_Status killed_status;
		TitanVerdictType local_verdict;
		String return_type;
		Text_Buf return_value;
	}

	private static int component_status_table_offset = TitanComponent.FIRST_PTC_COMPREF;
	private static ArrayList<component_status_table_struct> component_status_table = new ArrayList<TTCN_Runtime.component_status_table_struct>();

	private TTCN_Runtime() {
		// private constructor to disable accidental instantiation
	}

	//originally get_state
	public static executorStateEnum get_state() {
		return executorState.get();
	}

	// originally set_state
	public static void set_state(final executorStateEnum newState) {
		executorState.set(newState);
	}

	//originally is_hc
	public static boolean is_hc() {
		switch (executorState.get()) {
		case HC_INITIAL:
		case HC_IDLE:
		case HC_CONFIGURING:
		case HC_ACTIVE:
		case HC_OVERLOADED:
		case HC_OVERLOADED_TIMEOUT:
		case HC_EXIT:
			return true;
		default:
			return false;
		}
	}

	//originally is_mtc
	public static boolean is_mtc() {
		switch (executorState.get()) {
		case MTC_INITIAL:
		case MTC_IDLE:
		case MTC_CONTROLPART:
		case MTC_TESTCASE:
		case MTC_TERMINATING_TESTCASE:
		case MTC_TERMINATING_EXECUTION:
		case MTC_PAUSED:
		case MTC_CREATE:
		case MTC_START:
		case MTC_STOP:
		case MTC_KILL:
		case MTC_RUNNING:
		case MTC_ALIVE:
		case MTC_DONE:
		case MTC_KILLED:
		case MTC_CONNECT:
		case MTC_DISCONNECT:
		case MTC_MAP:
		case MTC_UNMAP:
		case MTC_CONFIGURING:
		case MTC_EXIT:
			return true;
		default:
			return false;
		}
	}

	//originally is_ptc
	public static boolean is_ptc() {
		switch (executorState.get()) {
		case PTC_INITIAL:
		case PTC_IDLE:
		case PTC_FUNCTION:
		case PTC_CREATE:
		case PTC_START:
		case PTC_STOP:
		case PTC_KILL:
		case PTC_RUNNING:
		case PTC_ALIVE:
		case PTC_DONE:
		case PTC_KILLED:
		case PTC_CONNECT:
		case PTC_DISCONNECT:
		case PTC_MAP:
		case PTC_UNMAP:
		case PTC_STOPPED:
		case PTC_EXIT:
			return true;
		default:
			return false;
		}
	}

	//originally is_tc
	public static boolean is_tc() {
		return is_mtc() || is_ptc();
	}

	//originally is_single
	public static boolean is_single() {
		switch (executorState.get()) {
		case SINGLE_CONTROLPART:
		case SINGLE_TESTCASE:
			return true;
		default:
			return false;
		}
	}

	//originally is_undefined
	public static boolean is_undefined() {
		return executorState.get() == executorStateEnum.UNDEFINED_STATE;
	}

	//originally is_idle
	public static boolean is_idle() {
		switch (executorState.get()) {
		case HC_IDLE:
		case HC_ACTIVE:
		case HC_OVERLOADED:
		case MTC_IDLE:
		case PTC_IDLE:
		case PTC_STOPPED:
			return true;
		default:
			return false;
		}
	}

	//originally is_overloaded
	public static boolean is_overloaded() {
		switch (executorState.get()) {
		case HC_OVERLOADED:
		case HC_OVERLOADED_TIMEOUT:
			return true;
		default:
			return false;
		}
	}

	public static void increase_try_catch_counter() {
		ttcn_try_block_counter++;
	}

	public static void decrease_try_catch_counter() {
		ttcn_try_block_counter--;
	}

	public static boolean is_in_ttcn_try_block() {
		return ttcn_try_block_counter > 0;
	}

	//originally in_controlpart
	private static boolean in_controlPart() {
		return executorState.get() == executorStateEnum.SINGLE_CONTROLPART || executorState.get() == executorStateEnum.MTC_CONTROLPART;
	}

	//originally verdict_enabled
	private static boolean verdict_enabled() {
		return executorState.get() == executorStateEnum.SINGLE_TESTCASE || is_mtc() || is_ptc();
	}

	private static void wait_for_state_change() {
		final executorStateEnum oldState = executorState.get();

		do {
			TTCN_Snapshot.takeNew(true);
		} while (oldState == executorState.get());
	}

	public static void begin_controlpart(final String moduleName) {
		control_module_name = moduleName;
		//FIXME implement execute_command
		TtcnLogger.log_controlpart_start_stop(moduleName, false);
	}

	public static void end_controlpart() {
		TTCN_Default.deactivateAll();
		TTCN_Default.resetCounter();
		TitanTimer.allStop();
		TtcnLogger.log_controlpart_start_stop(control_module_name, true);
		//FIXME implement execute_command
		control_module_name = null;
	}

	//originally TTCN_Runtime::check_begin_testcase
	public static void check_begin_testcase(final boolean hasTimer, final TitanFloat timerValue) {
		if (!in_controlPart()) {
			if (is_single() || is_mtc()) {
				throw new TtcnError(MessageFormat.format("Test case cannot be executed while another one ({0}.{1}) is running.", testcaseModuleName, testcaseDefinitionName));
			} else if (is_ptc()) {
				throw new TtcnError("Test case cannot be executed on a PTC.");
			} else {
				throw new TtcnError("Internal error: Executing a test case in an invalid state.");
			}
		}

		if (hasTimer && timerValue.isLessThan(0.0)) {
			throw new TtcnError(MessageFormat.format("The test case supervisor timer has negative duration ({0} s).", timerValue.getValue()));
		}
	}

	// originally TTCN_Runtime::begin_testcase
	//FIXME this is more complex
	public static void begin_testcase(final String moduleName, final String testcaseName, final String mtc_comptype_module, final String mtc_comptype_name, final String system_comptype_module, final String system_comptype_name, final boolean hasTimer, final TitanFloat timerValue) {
		switch (executorState.get()) {
		case SINGLE_CONTROLPART:
			executorState.set(executorStateEnum.SINGLE_TESTCASE);
			break;
		case MTC_CONTROLPART:
			TTCN_Communication.send_testcase_started(moduleName, testcaseName, mtc_comptype_module, mtc_comptype_name, system_comptype_module, system_comptype_name);
			executorState.set(executorStateEnum.MTC_TESTCASE);
			break;
		default:
			throw new TtcnError("Internal error: Executing a test case in an invalid state.");
		}
		TitanTimer.saveControlTimers();
		TTCN_Default.save_control_defaults();
		set_testcase_name(moduleName, testcaseName);
		//FIXME implement command execution

		TtcnLogger.log_testcase_started(moduleName, testcaseName);
		if (hasTimer) {
			TitanTimer.testcaseTimer.start(timerValue.getValue());
		}

		set_component_type(mtc_comptype_module, mtc_comptype_name);
		initialize_component_type();

		any_component_done_status = TitanAlt_Status.ALT_NO;
		all_component_done_status = TitanAlt_Status.ALT_YES;
		any_component_killed_status = TitanAlt_Status.ALT_NO;
		all_component_killed_status = TitanAlt_Status.ALT_YES;
	}

	//originally TTCN_Runtime::end_testcase
	// FIXME this is more complex
	public static VerdictTypeEnum end_testcase() {
		switch (executorState.get()) {
		case MTC_CREATE:
		case MTC_START:
		case MTC_STOP:
		case MTC_KILL:
		case MTC_RUNNING:
		case MTC_ALIVE:
		case MTC_DONE:
		case MTC_KILLED:
		case MTC_CONNECT:
		case MTC_DISCONNECT:
		case MTC_MAP:
		case MTC_UNMAP:
			executorState.set(executorStateEnum.MTC_TESTCASE);
		case MTC_TESTCASE:
			break;
		case SINGLE_TESTCASE:
			// implement disable_interrupt_handler();
			break;
		default:
			throw new TtcnError("Internal error: Ending a testcase in an invalid state.");
		}
		TitanTimer.testcaseTimer.stop();
		terminate_component_type();

		if (executorState.get() == executorStateEnum.MTC_TESTCASE) {
			TtcnLogger.log_executor_runtime(TitanLoggerApi.ExecutorRuntime_reason.enum_type.waiting__for__ptcs__to__finish);
			// FIXME implement
			executorState.set(executorStateEnum.MTC_TERMINATING_TESTCASE);
			wait_for_state_change();
		} else if (executorState.get() == executorStateEnum.SINGLE_TESTCASE) {
			executorState.set(executorStateEnum.SINGLE_CONTROLPART);
			// FIXME implement
		}

		TtcnLogger.log_testcase_finished(testcaseModuleName.get(), testcaseDefinitionName.get(), localVerdict, verdictReason);

		verdictCount[localVerdict.getValue()]++;

		testcaseModuleName.set(null);
		testcaseDefinitionName.set(null);

		any_component_done_status = TitanAlt_Status.ALT_UNCHECKED;
		all_component_done_status = TitanAlt_Status.ALT_UNCHECKED;
		any_component_killed_status = TitanAlt_Status.ALT_UNCHECKED;
		all_component_killed_status = TitanAlt_Status.ALT_UNCHECKED;

		TTCN_Default.restoreControlDefaults();
		TitanTimer.restore_control_timers();

		if (executorState.get() == executorStateEnum.MTC_PAUSED) {
			TtcnLogger.log_executor_runtime(TitanLoggerApi.ExecutorRuntime_reason.enum_type.user__paused__waiting__to__resume);
			wait_for_state_change();
			if (executorState.get() != executorStateEnum.MTC_TERMINATING_EXECUTION) {
				TtcnLogger.log_executor_runtime(TitanLoggerApi.ExecutorRuntime_reason.enum_type.resuming__execution);
			}
		}
		if (executorState.get() == executorStateEnum.MTC_TERMINATING_EXECUTION) {
			executorState.set(executorStateEnum.MTC_CONTROLPART);
			TtcnLogger.log_executor_runtime(TitanLoggerApi.ExecutorRuntime_reason.enum_type.terminating__execution);
			// FIXME implement
		}

		//FIXME this is more complex
		return localVerdict;
	}

	//originally TTCN_Runtime::initialize_component_type
	private static void initialize_component_type() {
		Module_List.initialize_component(component_type_module.get(), component_type_name.get(), true);

		//FIXME port set parameters
		TitanPort.all_start();

		localVerdict = VerdictTypeEnum.NONE;
		verdictReason = "";
	}

	//originally TTCN_Runtime::terminate_component_type
	private static void terminate_component_type() {
		//TODO add check
		TTCN_Default.deactivateAll();
		TitanTimer.allStop();
		TitanPort.deactivate_all();
		//TODO add log

		component_type_module.set(null);
		component_type_name.set(null);
	}

	//originally TTCN_Runtime::set_component_type
	private static void set_component_type(final String par_component_type_module, final String par_component_type_name) {
		//FIXME add checks
		component_type_module.set(par_component_type_module);
		component_type_name.set(par_component_type_name);
	}

	// originally TTCN_Runtime::set_component_name
	public static void set_component_name(final String new_component_name) {
		if (new_component_name != null && new_component_name.length() > 0) {
			component_name.set(new_component_name);
		} else {
			component_name.set(null);
		}
	}

	//originally set_testcase_name
	private static void set_testcase_name(final String parModuleName, final String parTestcaseName) {
		if (parModuleName == null || parModuleName.length() == 0 ||
				parTestcaseName == null || parTestcaseName.length() == 0) {
			throw new TtcnError("Internal error: TTCN_Runtime::set_testcase_name: Trying to set an invalid testcase name.");
		}

		if (testcaseModuleName.get() != null || testcaseDefinitionName.get() != null) {
			throw new TtcnError(MessageFormat.format("Internal error: TTCN_Runtime::set_testcase_name: Trying to set testcase name {0}.{1} while another one is active.", parModuleName, parTestcaseName));
		}

		testcaseModuleName.set(parModuleName);
		testcaseDefinitionName.set(parTestcaseName);
	}

	public static String get_host_name() {
		try {
			return InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "unknown";
		}
	}

	//originally get_component_type
	public static String get_component_type() {
		return component_type_name.get();
	}

	public static String get_component_name() {
		return component_name.get();
	}

	public static String get_testcase_name() {
		return testcaseDefinitionName.get();
	}

	//originally get_testcase_id_macro
	public static TitanCharString get_testcase_id_macro() {
		if (in_controlPart()) {
			throw new TtcnError("Macro %%testcaseId cannot be used from the control part outside test cases.");
		}

		if (testcaseDefinitionName.get() == null || testcaseDefinitionName.get().length() == 0) {
			throw new TtcnError("Internal error: Evaluating macro %%testcaseId, but the name of the current testcase is not set.");
		}

		return new TitanCharString(testcaseDefinitionName.get());
	}

	//originally get_testcasename
	public static TitanCharString get_testcasename() {
		if (in_controlPart() || is_hc()) {
			return new TitanCharString("");
		}

		if (testcaseDefinitionName.get() == null || testcaseDefinitionName.get().length() == 0) {
			throw new TtcnError("Internal error: Evaluating predefined function testcasename(), but the name of the current testcase is not set.");
		}

		return new TitanCharString(testcaseDefinitionName.get());
	}

	//originally map_port
	public static void map_port(final TitanComponent sourceComponentRef, final String sourePort, final TitanComponent destinationComponentRef, final String destinationPort) {
		//FIXME implement
		if (!sourceComponentRef.isBound()) {
			throw new TtcnError("The first argument of map operation contains an unbound component reference.");
		}

		final TitanComponent sourceComponent = sourceComponentRef;
		if (sourceComponent.getComponent() == TitanComponent.NULL_COMPREF) {
			throw new TtcnError("The first argument of map operation contains the null component reference.");
		}

		if (!destinationComponentRef.isBound()) {
			throw new TtcnError("The second argument of map operation contains an unbound component reference.");
		}

		final TitanComponent destinationComponent = destinationComponentRef;
		if (destinationComponent.getComponent() == TitanComponent.NULL_COMPREF) {
			throw new TtcnError("The second argument of map operation contains the null component reference.");
		}

		TitanComponent componentReference;
		String componentPort;
		String systemPort;
		if (sourceComponent.getComponent() == TitanComponent.SYSTEM_COMPREF) {
			if (destinationComponent.getComponent() == TitanComponent.SYSTEM_COMPREF) {
				throw new TtcnError("Both arguments of map operation refer to system ports.");
			}
			componentReference = destinationComponent;
			componentPort = destinationPort;
			systemPort = sourePort;
		} else if (destinationComponent.getComponent() == TitanComponent.SYSTEM_COMPREF) {
			componentReference = sourceComponent;
			componentPort = sourePort;
			systemPort = destinationPort;
		} else {
			throw new TtcnError("Both arguments of map operation refer to test component ports.");
		}

		//FIXME implement
		TitanPort.map_port(componentPort, systemPort, false);
	}

	//originally unmap_port
	public static void unmap_port(final TitanComponent sourceComponentRef, final String sourePort, final TitanComponent destinationComponentRef, final String destinationPort) {
		//FIXME implement
		if (!sourceComponentRef.isBound()) {
			throw new TtcnError("The first argument of unmap operation contains an unbound component reference.");
		}

		final TitanComponent sourceComponent = sourceComponentRef;
		if (sourceComponent.getComponent() == TitanComponent.NULL_COMPREF) {
			throw new TtcnError("The first argument of unmap operation contains the null component reference.");
		}

		if (!destinationComponentRef.isBound()) {
			throw new TtcnError("The second argument of unmap operation contains an unbound component reference.");
		}

		final TitanComponent destinationComponent = destinationComponentRef;
		if (destinationComponent.getComponent() == TitanComponent.NULL_COMPREF) {
			throw new TtcnError("The second argument of unmap operation contains the null component reference.");
		}

		TitanComponent componentReference;
		String componentPort;
		String systemPort;
		if (sourceComponent.getComponent() == TitanComponent.SYSTEM_COMPREF) {
			if (destinationComponent.getComponent() == TitanComponent.SYSTEM_COMPREF) {
				throw new TtcnError("Both arguments of unmap operation refer to system ports.");
			}
			componentReference = destinationComponent;
			componentPort = destinationPort;
			systemPort = sourePort;
		} else if (destinationComponent.getComponent() == TitanComponent.SYSTEM_COMPREF) {
			componentReference = sourceComponent;
			componentPort = sourePort;
			systemPort = destinationPort;
		} else {
			throw new TtcnError("Both arguments of unmap operation refer to test component ports.");
		}

		//FIXME implement
		TitanPort.unmap_port(componentPort, systemPort, false);
	}

	private static void check_port_name(final String portName, final String operationName, final String whichArgument) {
		if (portName == null) {
			throw new TtcnError(MessageFormat.format("Internal error: The port name in the {0} argument of {1} operation is a NULL pointer.", whichArgument, operationName));
		}
		if (portName.length() == 0) {
			throw new TtcnError(MessageFormat.format("Internal error: The {0} argument of {1} operation contains an empty string as port name.", whichArgument, operationName));
		}
	}

	public static void connect_port(final TitanComponent sourceComponent, final String sourePort, final TitanComponent destinationComponent, final String destinationPort) {
		check_port_name(sourePort, "connect", "first");
		check_port_name(destinationPort, "connect", "second");

		TtcnLogger.begin_event(Severity.PARALLEL_UNQUALIFIED);
		TtcnLogger.log_event_str("Connecting ports ");
		sourceComponent.log();
		TtcnLogger.log_event_str(MessageFormat.format(":{0} and ", sourePort));
		destinationComponent.log();
		TtcnLogger.log_event_str(MessageFormat.format(":{0}.", destinationPort));
		TtcnLogger.end_event();

		if (!sourceComponent.isBound()) {
			throw new TtcnError("The first argument of connect operation contains an unbound component reference.");
		}
		switch (sourceComponent.getComponent()) {
		case TitanComponent.NULL_COMPREF:
			throw new TtcnError("The first argument of connect operation contains the null component reference.");
		case TitanComponent.SYSTEM_COMPREF:
			throw new TtcnError("The first argument of connect operation refers to a system port.");
		default:
			break;
		}

		if (!destinationComponent.isBound()) {
			throw new TtcnError("The second argument of connect operation contains an unbound component reference.");
		}
		switch (destinationComponent.getComponent()) {
		case TitanComponent.NULL_COMPREF:
			throw new TtcnError("The second argument of connect operation contains the null component reference.");
		case TitanComponent.SYSTEM_COMPREF:
			throw new TtcnError("The second argument of connect operation refers to a system port.");
		default:
			break;
		}

		switch (executorState.get()) {
		case SINGLE_TESTCASE:
			if (sourceComponent.getComponent() != TitanComponent.MTC_COMPREF || destinationComponent.getComponent() != TitanComponent.MTC_COMPREF) {
				throw new TtcnError("Both endpoints of connect operation must refer to ports of mtc in single mode.");
			}
			//FIXME implement
			throw new TtcnError("Connecting components is not yet supported!");
			//break;
		case MTC_TESTCASE:
			TTCN_Communication.send_connect_req(sourceComponent.getComponent(), sourePort, destinationComponent.getComponent(), destinationPort);
			executorState.set(executorStateEnum.MTC_CONNECT);
			wait_for_state_change();
			break;
		case PTC_FUNCTION:
			TTCN_Communication.send_connect_req(sourceComponent.getComponent(), sourePort, destinationComponent.getComponent(), destinationPort);
			executorState.set(executorStateEnum.PTC_CONNECT);
			wait_for_state_change();
			break;
		default:
			if (in_controlPart()) {
				throw new TtcnError("Connect operation cannot be performed in the control part.");
			} else {
				throw new TtcnError("Internal error: Executing connect operation in invalid state.");
			}
		}

		TtcnLogger.log_portconnmap(ParPort_operation.enum_type.connect__, sourceComponent.getComponent(), sourePort, destinationComponent.getComponent(), destinationPort);
	}

	public static void disconnect_port(final TitanComponent sourceComponent, final String sourePort, final TitanComponent destinationComponent, final String destinationPort) {
		check_port_name(sourePort, "disconnect", "first");
		check_port_name(destinationPort, "disconnect", "second");

		TtcnLogger.begin_event(Severity.PARALLEL_UNQUALIFIED);
		TtcnLogger.log_event_str("Disconnecting ports ");
		sourceComponent.log();
		TtcnLogger.log_event_str(MessageFormat.format(":{0} and ", sourePort));
		destinationComponent.log();
		TtcnLogger.log_event_str(MessageFormat.format(":{0}.", destinationPort));
		TtcnLogger.end_event();

		if (!sourceComponent.isBound()) {
			throw new TtcnError("The first argument of disconnect operation contains an unbound component reference.");
		}
		switch (sourceComponent.getComponent()) {
		case TitanComponent.NULL_COMPREF:
			throw new TtcnError("The first argument of disconnect operation contains the null component reference.");
		case TitanComponent.SYSTEM_COMPREF:
			throw new TtcnError("The first argument of disconnect operation refers to a system port.");
		default:
			break;
		}

		if (!destinationComponent.isBound()) {
			throw new TtcnError("The second argument of disconnect operation contains an unbound component reference.");
		}
		switch (destinationComponent.getComponent()) {
		case TitanComponent.NULL_COMPREF:
			throw new TtcnError("The second argument of disconnect operation contains the null component reference.");
		case TitanComponent.SYSTEM_COMPREF:
			throw new TtcnError("The second argument of disconnect operation refers to a system port.");
		default:
			break;
		}

		switch (executorState.get()) {
		case SINGLE_TESTCASE:
			if (sourceComponent.getComponent() != TitanComponent.MTC_COMPREF || destinationComponent.getComponent() != TitanComponent.MTC_COMPREF) {
				throw new TtcnError("Both endpoints of disconnect operation must refer to ports of mtc in single mode.");
			}
			//FIXME implement
			throw new TtcnError("Connecting components is not yet supported!");
			//break;
		case MTC_TESTCASE:
			TTCN_Communication.send_disconnect_req(sourceComponent.getComponent(), sourePort, destinationComponent.getComponent(), destinationPort);
			executorState.set(executorStateEnum.MTC_DISCONNECT);
			wait_for_state_change();
			break;
		case PTC_FUNCTION:
			TTCN_Communication.send_disconnect_req(sourceComponent.getComponent(), sourePort, destinationComponent.getComponent(), destinationPort);
			executorState.set(executorStateEnum.PTC_DISCONNECT);
			wait_for_state_change();
			break;
		default:
			if (in_controlPart()) {
				throw new TtcnError("Disconnect operation cannot be performed in the control part.");
			} else {
				throw new TtcnError("Internal error: Executing disconnect operation in invalid state.");
			}
		}

		TtcnLogger.log_portconnmap(ParPort_operation.enum_type.disconnect__, sourceComponent.getComponent(), sourePort, destinationComponent.getComponent(), destinationPort);	}

	public static int hc_main(final String local_addr, final String MC_host, final int MC_port) {
		TTCN_Runtime.set_state(executorStateEnum.HC_INITIAL);
		TtcnLogger.log_hc_start(get_host_name());
		TtcnLogger.write_logger_settings();

		// FIXME implement
		TTCN_Communication.set_mc_address(MC_host, MC_port);
		TTCN_Communication.connect_mc();
		
		executorState.set(executorStateEnum.HC_IDLE);
		TTCN_Communication.send_version();

		do {
			TTCN_Snapshot.takeNew(true);
			TTCN_Communication.process_all_messages_hc();
		} while (executorState.get().ordinal() >= executorStateEnum.HC_IDLE.ordinal() && executorState.get().ordinal() < executorStateEnum.HC_EXIT.ordinal());

		if (executorState.get() == executorStateEnum.HC_EXIT) {
			TTCN_Communication.disconnect_mc();
		}
		//FIXME implement
		if (is_hc()) {
			TtcnLogger.log_executor_runtime(TitanLoggerApi.ExecutorRuntime_reason.enum_type.host__controller__finished);
		}

		return 0;
	}

	public static int mtc_main() {
		//FIXME implement rest
		int ret_val = 0;

		TtcnLogger.log_executor_component(TitanLoggerApi.ExecutorComponent_reason.enum_type.mtc__started);

		TTCN_Communication.connect_mc();
		executorState.set(executorStateEnum.MTC_IDLE);
		TTCN_Communication.send_mtc_created();

		do {
			TTCN_Snapshot.takeNew(true);
			TTCN_Communication.process_all_messages_tc();
		} while (executorState.get() != executorStateEnum.MTC_EXIT);


		TTCN_Communication.disconnect_mc();

		TtcnLogger.log_executor_component(TitanLoggerApi.ExecutorComponent_reason.enum_type.mtc__finished);

		return ret_val;
	}

	public static int ptc_main() {
		//FIXME implement rest
		int ret_val = 0;

		TtcnLogger.begin_event(Severity.EXECUTOR_COMPONENT);
		TtcnLogger.log_event_str(MessageFormat.format("TTCN-3 Parallel Test Component started on {0}. Component reference: ", get_host_name()));
		TitanComponent.self.get().log();
		TtcnLogger.log_event_str(MessageFormat.format(", component type: {0}.{1}", component_type_module, component_type_name));
		if (component_name != null) {
			TtcnLogger.log_event_str(MessageFormat.format(", component name: {0}", component_name));
		}
		TtcnLogger.log_event_str(". Version: " + PRODUCT_NUMBER + '.');
		TtcnLogger.end_event();

		//FIXME implement missing parts
		//TODO add the exception handling

		TTCN_Communication.connect_mc();
		executorState.set(executorStateEnum.PTC_IDLE);
		TTCN_Communication.send_ptc_created(TitanComponent.self.get().componentValue);
		initialize_component_type();

		if (ret_val == 0) {
			do {
				TTCN_Snapshot.takeNew(true);
				TTCN_Communication.process_all_messages_tc();
			} while (executorState.get() != executorStateEnum.PTC_EXIT);
		}

		//FIXME implement rest

		return ret_val;
	}

	//originally create_component
	public static int create_component(final String createdComponentTypeModule, final String createdComponentTypeName,
			String createdComponentName, String createdComponentLocation, final boolean createdComponentAlive) {
		if (in_controlPart()) {
			throw new TtcnError("Create operation cannot be performed in the control part.");
		} else if (is_single()) {
			throw new TtcnError("Create operation cannot be performed in single mode.");
		}

		if (createdComponentName != null && createdComponentName.length() == 0) {
			TtcnError.TtcnWarning("Empty charstring value was ignored as component name in create operation.");
			createdComponentName = null;
		}

		if (createdComponentLocation != null && createdComponentLocation.length() == 0) {
			TtcnError.TtcnWarning("Empty charstring value was ignored as component location in create operation.");
			createdComponentLocation = null;
		}

		TtcnLogger.begin_event(Severity.PARALLEL_UNQUALIFIED);
		TtcnLogger.log_event_str(MessageFormat.format("Creating new {0}PTC with component type {1}.{2}", createdComponentAlive ? "alive " : "", createdComponentTypeModule, createdComponentName));
		if (createdComponentName != null) {
			TtcnLogger.log_event_str(MessageFormat.format(", component name: {0}", createdComponentName));
		}
		if (createdComponentLocation != null) {
			TtcnLogger.log_event_str(MessageFormat.format(", location: {0}", createdComponentName));
		}
		TtcnLogger.log_char('.');
		TtcnLogger.end_event();

		switch (executorState.get()) {
		case MTC_TESTCASE:
			executorState.set(executorStateEnum.MTC_CREATE);
			break;
		case PTC_FUNCTION:
			executorState.set(executorStateEnum.PTC_CREATE);
			break;
		default:
			throw new TtcnError("Internal error: Executing create operation in invalid state.");
		}

		TTCN_Communication.send_create_req(createdComponentTypeModule, createdComponentTypeName, createdComponentName, createdComponentLocation, createdComponentAlive);
		if (is_mtc()) {
			// updating the component status flags
			// 'any component.done' and 'any component.killed' might be successful
			// from now since the PTC can terminate by itself
			if (any_component_done_status == TitanAlt_Status.ALT_NO) {
				any_component_done_status = TitanAlt_Status.ALT_UNCHECKED;
			}
			if (any_component_killed_status == TitanAlt_Status.ALT_NO) {
				any_component_killed_status = TitanAlt_Status.ALT_UNCHECKED;
			}
			all_component_killed_status = TitanAlt_Status.ALT_UNCHECKED;
		}

		wait_for_state_change();

		TtcnLogger.log_par_ptc(ParallelPTC_reason.enum_type.ptc__created, createdComponentTypeModule, createdComponentTypeName, create_done_killed_compref.get().intValue(), createdComponentName, createdComponentLocation, createdComponentAlive ? 1: 0, 0);

		TitanComponent.register_component_name(create_done_killed_compref.get().intValue(), createdComponentName);

		return create_done_killed_compref.get();
	}

	public static void prepare_start_component(final TitanComponent component_reference, final String module_name, final String function_name, final Text_Buf text_buf) {
		if (in_controlPart()) {
			throw new TtcnError("Start test component operation cannot be performed in the control part.");
		} else if (is_single()) {
			throw new TtcnError("Start test component operation cannot be performed in single mode.");
		}
		if (!component_reference.isBound()) {
			throw new TtcnError("Performing a start operation on an unbound component reference.");
		}

		final int compref = component_reference.getComponent();
		switch (compref) {
		case TitanComponent.NULL_COMPREF:
			throw new TtcnError("Start operation cannot be performed on the null component reference.");
		case TitanComponent.MTC_COMPREF:
			throw new TtcnError("Start operation cannot be performed on the component reference of MTC.");
		case TitanComponent.SYSTEM_COMPREF:
			throw new TtcnError("Start operation cannot be performed on the component reference of system.");
		case TitanComponent.ANY_COMPREF:
			throw new TtcnError("Internal error: 'any component' cannot be started.");
		case TitanComponent.ALL_COMPREF:
			throw new TtcnError("Internal error: 'all component' cannot be started.");
		default:
			break;
		}

		if (TitanComponent.self.get().getComponent() == compref) {
			throw new TtcnError("Start operation cannot be performed on the own component reference of the initiating component (i.e. 'self.start' is not allowed).");
		}

		if (in_component_status_table(compref)) {
			if (get_killed_status(compref) == TitanAlt_Status.ALT_YES) {
				throw new TtcnError(MessageFormat.format("PTC with component reference {0} is not alive anymore. Start operation cannot be performed on it.", compref));
			}

			cancel_component_done(compref);
		}

		TTCN_Communication.prepare_start_req(text_buf, compref, module_name, function_name);
	}

	public static void send_start_component(final Text_Buf text_buf) {
		switch (executorState.get()) {
		case MTC_TESTCASE:
			executorState.set(executorStateEnum.MTC_START);
			break;
		case PTC_FUNCTION:
			executorState.set(executorStateEnum.PTC_START);
			break;
		default:
			throw new TtcnError("Internal error: Executing component start operation in invalid state.");
		}

		TTCN_Communication.send_message(text_buf);
		if (is_mtc()) {
			all_component_done_status = TitanAlt_Status.ALT_UNCHECKED;
		}

		wait_for_state_change();

		TtcnLogger.log_par_ptc(ParallelPTC_reason.enum_type.function__started, null, null, 0, null, null, 0, 0);
	}

	public static void start_function(final String module_name, final String function_name, final Text_Buf text_buf) {
		switch (executorState.get()) {
		case PTC_IDLE:
		case PTC_STOPPED:
			break;
		default:
			text_buf.cut_message();

			throw new TtcnError("Internal error: Message START arrived in invalid state.");
		}

		try {
			Module_List.start_function(module_name, function_name, text_buf);

			// do nothing: the function terminated normally
			return;
			//FIXME handle TC_End
		} catch (TtcnError e) {
			TtcnLogger.log_par_ptc(ParallelPTC_reason.enum_type.function__error, null, function_name, 0, null, null, 0, 0);
			executorState.set(executorStateEnum.PTC_EXIT);
		}

		// the control reaches this code if the PTC has to be terminated
		terminate_component_type();
		//FIXME implement send_stopped_killed
		//FIXME logging
	}

	public static void function_started(final Text_Buf text_buf) {
		// The buffer still contains the incoming START message.
		text_buf.cut_message();

		executorState.set(executorStateEnum.PTC_FUNCTION);
		// The remaining messages must be processed now.
		TTCN_Communication.process_all_messages_tc();
	}

	public static void prepare_function_finished(final String return_type, final Text_Buf text_buf) {
		if (executorState.get() != executorStateEnum.PTC_FUNCTION) {
			throw new TtcnError("Internal error: PTC behaviour function finished in invalid state.");
		}

		if (is_alive.get()) {
			// Prepare a STOPPED message with the current verdict and possible return value.
			TTCN_Communication.prepare_stopped(text_buf, localVerdict, return_type, verdictReason);
		} else {
			terminate_component_type();
			TTCN_Communication.prepare_stopped_killed(text_buf, localVerdict, return_type, verdictReason);
		}
	}

	public static void function_finished(final String function_name) {
		TtcnLogger.log_par_ptc(ParallelPTC_reason.enum_type.function__finished, null, function_name, 0, null, null, is_alive.get() ? 1 : 0, 0);

		final Text_Buf text_buf = new Text_Buf();
		prepare_function_finished(null, text_buf);
		send_function_finished(text_buf);
	}

	public static void send_function_finished(final Text_Buf text_buf) {
		// send out the STOPPED or STOPPED_KILLED message, which is already
		// complete and contains the return value
		TTCN_Communication.send_message(text_buf);
		if (is_alive.get()) {
			executorState.set(executorStateEnum.PTC_STOPPED);
		} else {
			TtcnLogger.log_final_verdict(true, localVerdict, localVerdict, localVerdict, verdictReason, -1, TitanComponent.UNBOUND_COMPREF, null);
			executorState.set(executorStateEnum.PTC_EXIT);
		}
	}

	//originally component_done, with component parameter
	public static TitanAlt_Status component_done(final int component_reference) {
		if (in_controlPart()) {
			throw new TtcnError("Done operation cannot be performed in the control part.");
		}

		switch (component_reference) {
		case TitanComponent.NULL_COMPREF:
			throw new TtcnError("Done operation cannot be performed on the null component reference.");
		case TitanComponent.MTC_COMPREF:
			throw new TtcnError("Done operation cannot be performed on the component reference of MTC.");
		case TitanComponent.SYSTEM_COMPREF:
			throw new TtcnError("Done operation cannot be performed on the component reference of system.");
		default:
			//FIXME implement rest of the branches
			throw new TtcnError("component_done is not yet supported!");
		}
	}

	//FIXME needs text_buffer parameter once decoding is available
	//originally component_done, with component parameter
	public static TitanAlt_Status component_done(final int component_reference, final String return_type) {
		if (in_controlPart()) {
			throw new TtcnError("Done operation cannot be performed in the control part.");
		}

		switch (component_reference) {
		case TitanComponent.NULL_COMPREF:
			throw new TtcnError("Done operation cannot be performed on the null component reference.");
		case TitanComponent.MTC_COMPREF:
			throw new TtcnError("Done operation cannot be performed on the component reference of MTC.");
		case TitanComponent.SYSTEM_COMPREF:
			throw new TtcnError("Done operation cannot be performed on the component reference of system.");
		case TitanComponent.ANY_COMPREF:
			throw new TtcnError("Done operation with return value cannot be performed on 'any component'.");
		case TitanComponent.ALL_COMPREF:
			throw new TtcnError("Done operation with return value cannot be performed on 'all component'.");
		default:
			break;
		}

		if (is_single()) {
			throw new TtcnError("Done operation on a component reference cannot be performed in single mode.");
		}
		//FIXME implement
		throw new TtcnError("component_done is not yet supported!");
	}

	//originally component_killed, with component parameter
	public static TitanAlt_Status component_killed(final int component_reference) {
		if (in_controlPart()) {
			throw new TtcnError("Killed operation cannot be performed in the control part.");
		}

		switch (component_reference) {
		case TitanComponent.NULL_COMPREF:
			throw new TtcnError("Killed operation cannot be performed on the null component reference.");
		case TitanComponent.MTC_COMPREF:
			throw new TtcnError("Killed operation cannot be performed on the component reference of MTC.");
		case TitanComponent.SYSTEM_COMPREF:
			throw new TtcnError("Killed operation cannot be performed on the component reference of system.");
		default:
			//FIXME implement rest of the branches
			throw new TtcnError("Component_killed is not yet supported!");
		}
	}

	//originally component_running, with component parameter
	public static boolean component_running(final int component_reference) {
		if (in_controlPart()) {
			throw new TtcnError("Component running operation cannot be performed in the control part.");
		}

		switch (component_reference) {
		case TitanComponent.NULL_COMPREF:
			throw new TtcnError("Running operation cannot be performed on the null component reference.");
		case TitanComponent.MTC_COMPREF:
			throw new TtcnError("Running operation cannot be performed on the component reference of MTC.");
		case TitanComponent.SYSTEM_COMPREF:
			throw new TtcnError("Running operation cannot be performed on the component reference of system.");
		default:
			//FIXME implement rest of the branches
			throw new TtcnError("Component_running is not yet supported!");
		}
	}

	//originally component_alive, with component parameter
	public static boolean component_alive(final int component_reference) {
		if (in_controlPart()) {
			throw new TtcnError("Alive operation cannot be performed in the control part.");
		}

		switch (component_reference) {
		case TitanComponent.NULL_COMPREF:
			throw new TtcnError("Alive operation cannot be performed on the null component reference.");
		case TitanComponent.MTC_COMPREF:
			throw new TtcnError("Alive operation cannot be performed on the component reference of MTC.");
		case TitanComponent.SYSTEM_COMPREF:
			throw new TtcnError("Alive operation cannot be performed on the component reference of system.");
		default:
			//FIXME implement rest of the branches
			throw new TtcnError("Component_alive is not yet supported!");
		}
	}

	//originally stop_component
	public static void stop_component(final int component_reference) {
		if (in_controlPart()) {
			throw new TtcnError("Component stop operation cannot be performed in the control part.");
		}

		if (TitanComponent.self.get().componentValue == component_reference) {
			stop_execution();
		}

		switch (component_reference) {
		case TitanComponent.NULL_COMPREF:
			throw new TtcnError("Stop operation cannot be performed on the null component reference.");
		case TitanComponent.MTC_COMPREF:
			stop_mtc();
			break;
		case TitanComponent.SYSTEM_COMPREF:
			throw new TtcnError("Stop operation cannot be performed on the component reference of system.");
		case TitanComponent.ANY_COMPREF:
			throw new TtcnError("Internal error: 'any component' cannot be stopped.");
		case TitanComponent.ALL_COMPREF:
			stop_all_component();
			break;
		default:
			stop_ptc(component_reference);
		}
	}

	//originally stop_execution
	public static void stop_execution() {
		if (in_controlPart()) {
			TtcnLogger.log_executor_runtime(TitanLoggerApi.ExecutorRuntime_reason.enum_type.stopping__control__part__execution);;
		} else {
			TtcnLogger.log_str(Severity.PARALLEL_UNQUALIFIED, "Stopping test component execution.");

			if (is_ptc()) {
				// the state variable indicates whether the component remains alive
				// after termination or not
				if (is_alive.get()) {
					executorState.set(executorStateEnum.PTC_STOPPED);
				} else {
					executorState.set(executorStateEnum.PTC_EXIT);
				}
			}
		}

		//FIXME implement
		throw new TtcnError("Stoping execution is not yet supported!");
	}

	public static void stop_mtc() {
		TtcnLogger.log_par_ptc(ParallelPTC_reason.enum_type.stopping__mtc, null, null, 0, null, null, 0, 0);
		TTCN_Communication.send_stop_req(TitanComponent.MTC_COMPREF);
		stop_execution();
	}

	public static void stop_ptc(final int component_reference) {
		if (is_single()) {
			throw new TtcnError("Stop operation on a component reference cannot be performed in single mode.");
		}

		// do nothing if a successful done or killed operation was performed on the component reference
		if (in_component_status_table(component_reference)) {
			int index = get_component_status_table_index(component_reference);
			if (component_status_table.get(index).done_status == TitanAlt_Status.ALT_YES ||
					component_status_table.get(index).killed_status == TitanAlt_Status.ALT_YES) {
				TtcnLogger.log_str(Severity.PARALLEL_UNQUALIFIED, MessageFormat.format("PTC with component reference {0} is not running. Stop operation had no effect.", component_reference));

				return;
			}
		}

		switch (executorState.get()) {
		case MTC_TESTCASE:
			executorState.set(executorStateEnum.MTC_STOP);
			break;
		case PTC_FUNCTION:
			executorState.set(executorStateEnum.PTC_STOP);
			break;
		default:
			throw new TtcnError("Internal error: Executing component stop operation in invalid state.");
		}

		TtcnLogger.log_str(Severity.PARALLEL_UNQUALIFIED, MessageFormat.format("Stopping PTC with component reference {0}.", component_reference));
		TTCN_Communication.send_stop_req(component_reference);
		//wait for STOP_ACK;
		wait_for_state_change();

		TtcnLogger.log_par_ptc(ParallelPTC_reason.enum_type.ptc__stopped, null, null, component_reference, null, null, 0, 0);
	}

	public static void stop_all_component() {
		if (is_single()) {
			TtcnLogger.log_str(Severity.PARALLEL_UNQUALIFIED, "No PTCs are running. Operation 'all component.stop' had no effect.");

			return;
		}

		if (!is_mtc()) {
			throw new TtcnError("Operation 'all component.stop' can only be performed on the MTC.");
		}

		// do nothing if 'all component.done' or 'all component.killed' was successful
		if (all_component_done_status == TitanAlt_Status.ALT_YES ||
				all_component_killed_status == TitanAlt_Status.ALT_YES) {
			TtcnLogger.log_str(Severity.PARALLEL_UNQUALIFIED, "No PTCs are running. Operation 'all component.stop' had no effect.");

			return;
		}

		// a request must be sent to MC
		if (executorState.get() != executorStateEnum.MTC_TESTCASE) {
			throw new TtcnError("Internal error: Executing 'all component.stop' in invalid state.");
		}

		executorState.set(executorStateEnum.MTC_STOP);
		TtcnLogger.log_str(Severity.PARALLEL_UNQUALIFIED, "Stopping all components.");
		TTCN_Communication.send_stop_req(TitanComponent.ALL_COMPREF);

		//wait for STOP_ACK
		wait_for_state_change();
		// 'all component.done' will be successful later
		all_component_done_status = TitanAlt_Status.ALT_YES;
		TtcnLogger.log_par_ptc(ParallelPTC_reason.enum_type.all__comps__stopped, null, null, 0, null, null, 0, 0);
	}

	//originally kill_component
	public static void kill_component(final int component_reference) {
		if (in_controlPart()) {
			throw new TtcnError("Kill operation cannot be performed in the control part.");
		}

		//FIXME implement
		throw new TtcnError("Killing a component is not yet supported!");
	}

	//originally kill_execution
	public static void kill_execution() {
		//FIXME implement
		throw new TtcnError("Killing execution is not yet supported!");
	}

	public static void setverdict(final TitanVerdictType.VerdictTypeEnum newValue) {
		setverdict(newValue, "");
	}

	public static void setverdict(final TitanVerdictType.VerdictTypeEnum newValue, final String reason) {
		if (verdict_enabled()) {
			if (VerdictTypeEnum.ERROR.equals(newValue)) {
				throw new TtcnError("Error verdict cannot be set explicitly.");
			}

			setverdict_internal(newValue, reason);
		} else if (in_controlPart()) {
			throw new TtcnError("Verdict cannot be set in the control part.");
		} else {
			throw new TtcnError("Internal error: Setting the verdict in invalid state.");
		}
	}

	public static void setverdict(final TitanVerdictType newValue) {
		setverdict(newValue, "");
	}

	public static void setverdict(final TitanVerdictType newValue, final String reason) {
		if (!newValue.isBound()) {
			throw new TtcnError("The argument of setverdict operation is an unbound verdict value.");
		}

		setverdict(newValue.getValue(), reason);
	}

	//originally set_error_verdict
	public static void set_error_verdict() {
		if (verdict_enabled()) {
			setverdict_internal(VerdictTypeEnum.ERROR, "");
		}
		//FIXME implement else
	}

	//originally getverdict
	public static TitanVerdictType get_verdict() {
		if (verdict_enabled()) {
			//FIXME logging
		} else if (in_controlPart()) {
			throw new TtcnError("Getverdict operation cannot be performed in the control part.");
		} else {
			throw new TtcnError("Internal error: Performing getverdict operation in invalid state.");
		}

		return new TitanVerdictType(localVerdict);
	}

	//originally setverdict_internal
	private static void setverdict_internal(final TitanVerdictType.VerdictTypeEnum newValue, final String reason) {
		if (newValue.getValue() < VerdictTypeEnum.NONE.getValue() || newValue.getValue() > VerdictTypeEnum.ERROR.getValue()) {
			throw new TtcnError(MessageFormat.format("Internal error: setting an invalid verdict value ({0}).", newValue.getValue()));
		}

		final VerdictTypeEnum oldVerdict = localVerdict;
		if (localVerdict.getValue() < newValue.getValue()) {
			verdictReason = reason;
			localVerdict = newValue;
			if (reason == null || reason.length() == 0) {
				TtcnLogger.log_setverdict(newValue, oldVerdict, localVerdict, null, null);
			} else {
				TtcnLogger.log_setverdict(newValue, oldVerdict, localVerdict, reason, reason);
			}
		} else if (localVerdict.getValue() == newValue.getValue()) {
			if (reason == null || reason.length() == 0) {
				TtcnLogger.log_setverdict(newValue, oldVerdict, localVerdict, null, null);
			} else {
				TtcnLogger.log_setverdict(newValue, oldVerdict, localVerdict, reason, reason);
			}
		}

		//FIXME handle debugger breakpoints
	}

	//originally log_verdict_statistics
	public static void log_verdict_statistics() {
		final int totalTestcases = verdictCount[VerdictTypeEnum.NONE.getValue()] + verdictCount[VerdictTypeEnum.PASS.getValue()]
				+ verdictCount[VerdictTypeEnum.INCONC.getValue()] + verdictCount[VerdictTypeEnum.FAIL.getValue()]
						+ verdictCount[VerdictTypeEnum.ERROR.getValue()];

		VerdictTypeEnum overallVerdict;
		if (controlErrorCount > 0 || verdictCount[VerdictTypeEnum.ERROR.getValue()] > 0) {
			overallVerdict = VerdictTypeEnum.ERROR;
		} else if (verdictCount[VerdictTypeEnum.FAIL.getValue()] > 0) {
			overallVerdict = VerdictTypeEnum.FAIL;
		} else if (verdictCount[VerdictTypeEnum.INCONC.getValue()] > 0) {
			overallVerdict = VerdictTypeEnum.INCONC;
		} else if (verdictCount[VerdictTypeEnum.PASS.getValue()] > 0) {
			overallVerdict = VerdictTypeEnum.PASS;
		} else {
			overallVerdict = VerdictTypeEnum.NONE;
		}

		if (totalTestcases > 0) {
			TtcnLogger.log_verdict_statistics(verdictCount[VerdictTypeEnum.NONE.getValue()], (100.0 * verdictCount[VerdictTypeEnum.NONE.getValue()]) / totalTestcases,
					verdictCount[VerdictTypeEnum.PASS.getValue()], (100.0 * verdictCount[VerdictTypeEnum.PASS.getValue()]) / totalTestcases,
					verdictCount[VerdictTypeEnum.INCONC.getValue()], (100.0 * verdictCount[VerdictTypeEnum.INCONC.getValue()]) / totalTestcases,
					verdictCount[VerdictTypeEnum.FAIL.getValue()], (100.0 * verdictCount[VerdictTypeEnum.FAIL.getValue()]) / totalTestcases,
					verdictCount[VerdictTypeEnum.ERROR.getValue()], (100.0 * verdictCount[VerdictTypeEnum.ERROR.getValue()]) / totalTestcases);
		} else {
			TtcnLogger.log_verdict_statistics(0, 0.0, 0, 0.0, 0, 0.0, 0, 0.0, 0, 0.0);
		}

		if (controlErrorCount > 0) {
			TtcnLogger.log_controlpart_errors(controlErrorCount);
		}

		TtcnLogger.log_str(Severity.STATISTICS_VERDICT, MessageFormat.format("Test execution summary: {0} test case{1} executed. Overall verdict: {2}", totalTestcases, totalTestcases > 1 ? "s were" : " was", overallVerdict.getName()));

		verdictCount[VerdictTypeEnum.NONE.getValue()] = 0;
		verdictCount[VerdictTypeEnum.PASS.getValue()] = 0;
		verdictCount[VerdictTypeEnum.INCONC.getValue()] = 0;
		verdictCount[VerdictTypeEnum.FAIL.getValue()] = 0;
		verdictCount[VerdictTypeEnum.ERROR.getValue()] = 0;
		controlErrorCount = 0;
	}

	public static void begin_action() {
		TtcnLogger.begin_event(Severity.ACTION_UNQUALIFIED);
		TtcnLogger.log_event_str("Action: ");
	}

	public static void end_action() {
		TtcnLogger.end_event();
	}

	public static void process_create_mtc(){
		switch (executorState.get()) {
		case HC_ACTIVE:
		case HC_OVERLOADED:
			break;
		default:
			TTCN_Communication.send_error("Message CREATE_MTC arrived in invalid state.");
			return;
		}

		// clean the emergency buffer
		TtcnLogger.ring_buffer_dump(false);

		final Thread MTC = new Thread() {

			@Override
			public void run() {
				//runs in the MTC
				TTCN_Snapshot.reOpen();
				TTCN_Communication.close_mc_connection();

				TitanComponent.self.set(new TitanComponent(TitanComponent.MTC_COMPREF));
				executorState.set(executorStateEnum.MTC_INITIAL);

				//stuff from Parallel_main::main after hc_main call
				//FIXME clear stuff before mtc_main
				//COMPONENT::clear_component_names();
				//TTCN_Logger::close_file();
				//TTCN_Logger::set_start_time();
				mtc_main();
				//FIXME close down stuff after mtc_main
			}
			
		};

		MTC.start();

		TtcnLogger.log_mtc_created(0);//TODO what is the pid?
		//add_component(MTC_COMPREF, ...)
		//successful_process_creation();

		//FIXME implement
	}

	public static void process_create_ptc(final int component_reference, final String component_type_module, final String component_type_name, final String par_component_name, final boolean par_is_alive, final String current_testcase_module, final String current_testcase_name) {
		switch (executorState.get()) {
		case HC_ACTIVE:
		case HC_OVERLOADED:
			break;
		default:
			TTCN_Communication.send_error("Message CREATE_PTC arrived in invalid state.");
			return;
		}

		// clean the emergency buffer
		TtcnLogger.ring_buffer_dump(false);

		final Thread PTC = new Thread() {

			@Override
			public void run() {
				//runs in the PTC
				TTCN_Snapshot.reOpen();
				TTCN_Communication.close_mc_connection();

				TitanComponent.self.set(new TitanComponent(component_reference));
				set_component_type(component_type_module, component_type_name);
				set_component_name(par_component_name);
				TTCN_Runtime.is_alive.set(par_is_alive);
				set_testcase_name(current_testcase_module, current_testcase_name);
				executorState.set(executorStateEnum.PTC_INITIAL);

				//What now???
				
				//stuff from Parallel_main::main after hc_main call
				//FIXME clear stuff before mtc_main
				//COMPONENT::clear_component_names();
				//TTCN_Logger::close_file();
				//TTCN_Logger::set_start_time();
				ptc_main();
				//FIXME close down stuff after mtc_main
			}
			
		};

		PTC.start();

		//TODO what is the PID?
		TtcnLogger.log_par_ptc(ParallelPTC_reason.enum_type.ptc__created__pid, component_type_module, component_type_name, component_reference, par_component_name, current_testcase_name, 0, 0);
		
		//FIXME implement
	}

	public static void process_create_ack(final int new_component) {
		switch (executorState.get()) {
		case MTC_CREATE:
			executorState.set(executorStateEnum.MTC_TESTCASE);
			break;
		case MTC_TERMINATING_TESTCASE:
			break;
		case PTC_CREATE:
			executorState.set(executorStateEnum.PTC_FUNCTION);
			break;
		default:
			throw new TtcnError("Internal error: Message CREATE_ACK arrived in invalid state.");
		}

		create_done_killed_compref.set(new_component);
		//FIXME implement
	}

	public static void process_kill() {
		if (!is_ptc()) {
			throw new TtcnError("Internal error: Message KILL arrived in invalid state.");
		}

		switch (executorState.get()) {
		case PTC_IDLE:
		case PTC_STOPPED:
			TtcnLogger.log_par_ptc(ParallelPTC_reason.enum_type.kill__request__frm__mc, null, null, 0, null, null, 0, 0);

			// This may affect the final verdict.
			terminate_component_type();

			TTCN_Communication.send_killed(localVerdict, null);
			TtcnLogger.log_final_verdict(true, localVerdict, localVerdict, localVerdict, verdictReason, -1, TitanComponent.UNBOUND_COMPREF, null);
			executorState.set(executorStateEnum.PTC_EXIT);
			break;
		case PTC_EXIT:
			break;
		default:
			TtcnLogger.log_str(Severity.PARALLEL_UNQUALIFIED, "Kill was requested from MC.");

			kill_execution();
		}
	}

	private static void cancel_component_done(final int component_reference) {
		switch (component_reference) {
		case TitanComponent.ANY_COMPREF:
			if (is_mtc()) {
				any_component_done_status = TitanAlt_Status.ALT_UNCHECKED;
			} else {
				throw new TtcnError("Internal error: TTCN_Runtime::cancel_component_done(ANY_COMPREF): can be used only on MTC.");
			}
			break;
		case TitanComponent.ALL_COMPREF:
		case TitanComponent.NULL_COMPREF:
		case TitanComponent.MTC_COMPREF:
		case TitanComponent.SYSTEM_COMPREF:
			throw new TtcnError(MessageFormat.format("Internal error: TTCN_Runtime::cancel_component_done: invalid component reference: {0}.", component_reference));
		default:
			if (in_component_status_table(component_reference)) {
				int index = get_component_status_table_index(component_reference);
				component_status_table_struct temp = component_status_table.get(index);
				temp.done_status = TitanAlt_Status.ALT_UNCHECKED;
				temp.return_type = null;
				temp.return_value = null;
			}
		}
	}

	private static int get_component_status_table_index(final int component_reference) {
		if (component_reference < TitanComponent.FIRST_PTC_COMPREF) {
			throw new TtcnError(MessageFormat.format("Internal error: TTCN_Runtime.get_component_status_table_index: invalid component reference: {0}.", component_reference));
		}

		if (component_status_table.size() == 0) {
			//the table is empty, this will be the first entry
			final component_status_table_struct temp = new component_status_table_struct();
			temp.done_status = TitanAlt_Status.ALT_UNCHECKED;
			temp.killed_status = TitanAlt_Status.ALT_UNCHECKED;
			temp.local_verdict = new TitanVerdictType(TitanVerdictType.VerdictTypeEnum.NONE);
			temp.return_type = null;
			temp.return_value = null;

			component_status_table.add(temp);
			component_status_table_offset = component_reference;

			return 0;
		} else if (component_reference >= component_status_table_offset) {
			// the table contains at least one entry that is smaller than component_reference
			int component_index = component_reference - component_status_table_offset;
			if (component_index >= component_status_table.size()) {
				// component_reference is still not in the table
				// the table has to be extended at the end
				for (int i = component_status_table.size(); i < component_index; i++) {
					final component_status_table_struct temp = new component_status_table_struct();
					temp.done_status = TitanAlt_Status.ALT_UNCHECKED;
					temp.killed_status = TitanAlt_Status.ALT_UNCHECKED;
					temp.local_verdict = new TitanVerdictType(TitanVerdictType.VerdictTypeEnum.NONE);
					temp.return_type = null;
					temp.return_value = null;

					component_status_table.add(i, temp);
				}
			}

			return component_index;
		} else {
			// component_reference has to be inserted before the existing table
			int offset_diff = component_status_table_offset - component_reference;
			int new_size = component_status_table.size() + offset_diff;
			final ArrayList<component_status_table_struct> temp_table = new ArrayList<TTCN_Runtime.component_status_table_struct>();
			for (int i = 0; i < offset_diff; i++) {
				final component_status_table_struct temp = new component_status_table_struct();
				temp.done_status = TitanAlt_Status.ALT_UNCHECKED;
				temp.killed_status = TitanAlt_Status.ALT_UNCHECKED;
				temp.local_verdict = new TitanVerdictType(TitanVerdictType.VerdictTypeEnum.NONE);
				temp.return_type = null;
				temp.return_value = null;

				temp_table.add(i, temp);
			}
			component_status_table.addAll(0, temp_table);
			component_status_table_offset = component_reference;

			return 0;
		}
	}

	private static TitanAlt_Status get_killed_status(final int component_reference) {
		return component_status_table.get(get_component_status_table_index(component_reference)).killed_status;
	}

	private static boolean in_component_status_table(final int component_reference) {
		return component_reference >= component_status_table_offset && component_reference < component_status_table.size() + component_status_table_offset;
	}
}
