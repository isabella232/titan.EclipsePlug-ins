package org.eclipse.titan.runtime.core;

import java.text.MessageFormat;

import org.eclipse.titan.runtime.core.TitanVerdictType.VerdictTypeEnum;

/**
 * TTCN-3 runtime class
 * 
 * TODO: lots to implement
 * 
 * @author Kristof Szabados
 */
public class TTCN_Runtime {
	private static String component_type_module = null;
	private static String component_type_name = null;

	//originally TTCN_Runtime::check_begin_testcase
	public static void check_begin_testcase(final boolean hasTimer, final TitanFloat timerValue) {
		//FIXME missing checks

		if (hasTimer && timerValue.getValue() < 0.0) {
			throw new TtcnError(MessageFormat.format("The test case supervisortimer has negative duration ({0} s).", timerValue.getValue()));
		}
	}

	// originally TTCN_Runtime::begin_testcase
	//FIXME this is more complex
	public static void begin_testcase(final String moduleName, final String componentName, final boolean hasTimer, final TitanFloat timerValue) {
		TitanTimer.saveControlTimers();
		TTCN_Default.save_control_defaults();
		//FIXME this is much more complex

		if (hasTimer) {
			TitanTimer.testcaseTimer.start(timerValue.getValue());
		}

		set_component_type(moduleName, componentName);
		initialize_component_type();
	}

	//originally TTCN_Runtime::end_testcase
	// FIXME this is more complex
	public static TitanVerdictType end_testcase() {
		TitanTimer.testcaseTimer.stop();
		terminate_component_type();

		TTCN_Default.restoreControlDefaults();
		TitanTimer.restore_control_timers();

		//FIXME this is more complex
		return new TitanVerdictType(VerdictTypeEnum.NONE);
	}

	//originally TTCN_Runtime::set_component_type
	private static void initialize_component_type() {
		Module_List.initialize_component(component_type_module, component_type_name, true);

		//FIXME port set parameters
		TitanPort.all_start();
	}

	//originally TTCN_Runtime::terminate_component_type
	private static void terminate_component_type() {
		//TODO add check
		TTCN_Default.deactivateAll();
		TitanTimer.allStop();
		TitanPort.deactivateAll();
		//TODO add log

		component_type_module = null;
		component_type_name = null;
	}

	//originally TTCN_Runtime::set_component_type
	private static void set_component_type(final String par_component_type_module, final String par_component_type_name) {
		//FIXME add checks
		component_type_module = par_component_type_module;
		component_type_name = par_component_type_name;
	}

	public static void mapPort(final String sourePort, final String destinationPort) {
		//FIXME implement
		TitanPort.mapPort(sourePort, destinationPort);
	}

	public static void unmapPort(final String sourePort, final String destinationPort) {
		//FIXME implement
		TitanPort.unmapPort(sourePort, destinationPort);
	}

	//originally create_component
	public static int createComponent(final String createdComponentTypeModule, final String createdComponentTypeName,
			final String createdComponentName, final String createdComponentLocation, final boolean createdComponentAlive) {
		//FIXME implement
		throw new TtcnError("Creating component is not yet supported!");
	}
}
