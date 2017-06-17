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

	private static VerdictTypeEnum localVerdict = VerdictTypeEnum.NONE;
	private static String verdictReason = "";

	//originally in_controlpart
	private static boolean inControlPart() {
		//FIXME implement
		return false;
	}

	//originally verdict_enabled
	private static boolean verdictEnabled() {
		//FIXME implement
		return true;
	}

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

	//originally map_port
	public static void mapPort(final TitanComponent sourceComponentRef, final String sourePort, final TitanComponent destinationComponentRef, final String destinationPort) {
		//FIXME implement
		if (!sourceComponentRef.isBound()) {
			throw new TtcnError("The first argument of map operation contains an unbound component reference.");
		}

		TitanComponent sourceComponent = sourceComponentRef;
		if (sourceComponent.getComponent() == TitanComponent.NULL_COMPREF) {
			throw new TtcnError("The first argument of map operation contains the null component reference.");
		}

		if (!destinationComponentRef.isBound()) {
			throw new TtcnError("The second argument of map operation contains an unbound component reference.");
		}

		TitanComponent destinationComponent = destinationComponentRef;
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
		TitanPort.mapPort(componentPort, systemPort, false);
	}

	//originally unmap_port
	public static void unmapPort(final TitanComponent sourceComponentRef, final String sourePort, final TitanComponent destinationComponentRef, final String destinationPort) {
		//FIXME implement
		if (!sourceComponentRef.isBound()) {
			throw new TtcnError("The first argument of unmap operation contains an unbound component reference.");
		}

		TitanComponent sourceComponent = sourceComponentRef;
		if (sourceComponent.getComponent() == TitanComponent.NULL_COMPREF) {
			throw new TtcnError("The first argument of unmap operation contains the null component reference.");
		}

		if (!destinationComponentRef.isBound()) {
			throw new TtcnError("The second argument of unmap operation contains an unbound component reference.");
		}

		TitanComponent destinationComponent = destinationComponentRef;
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
		TitanPort.unmapPort(componentPort, systemPort, false);
	}

	public static void connectPort(final TitanComponent sourceComponent, final String sourePort, final TitanComponent destinationComponent, final String destinationPort) {
		//FIXME implement
		throw new TtcnError("Connecting components is not yet supported!");
	}

	public static void disconnectPort(final TitanComponent sourceComponent, final String sourePort, final TitanComponent destinationComponent, final String destinationPort) {
		//FIXME implement
		throw new TtcnError("Disconnecting components is not yet supported!");
	}

	//originally create_component
	public static int createComponent(final String createdComponentTypeModule, final String createdComponentTypeName,
			final String createdComponentName, final String createdComponentLocation, final boolean createdComponentAlive) {
		//FIXME implement
		throw new TtcnError("Creating component is not yet supported!");
	}

	public static void setverdict(final TitanVerdictType.VerdictTypeEnum newValue) {
		setverdict(newValue, "");
	}

	public static void setverdict(final TitanVerdictType.VerdictTypeEnum newValue, final String reason) {
		if (verdictEnabled()) {
			if (VerdictTypeEnum.ERROR.equals(newValue)) {
				throw new TtcnError("Error verdict cannot be set explicitly.");
			}

			setverdictInternal(newValue, reason);
		} else if (inControlPart()) {
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
	public static void setErrorVerdict() {
		if (verdictEnabled()) {
			setverdictInternal(VerdictTypeEnum.ERROR, "");
		}
		//FIXME implement else
	}

	//originally setverdict_internal
	private static void setverdictInternal(final TitanVerdictType.VerdictTypeEnum newValue, final String reason) {
		if (newValue.getValue() < VerdictTypeEnum.NONE.getValue() || newValue.getValue() > VerdictTypeEnum.ERROR.getValue()) {
			throw new TtcnError(MessageFormat.format("Internal error: setting an invalid verdict value ({0}).", newValue.getValue()));
		}

		VerdictTypeEnum oldVerdict = localVerdict;
		if (localVerdict.getValue() < newValue.getValue()) {
			verdictReason = reason;
			localVerdict = newValue;
			//FIXME implement logging
		} else if (localVerdict.getValue() == newValue.getValue()) {
			//FIXME implement logging
		}

		//FIXME handle debugger breakpoints
	}
}
