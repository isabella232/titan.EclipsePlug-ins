/******************************************************************************
 * Copyright (c) 2000-2020 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.titan.runtime.core.Param_Types.Module_Param_Name;
import org.eclipse.titan.runtime.core.Param_Types.Module_Param_Ttcn_Null;
import org.eclipse.titan.runtime.core.Param_Types.Module_Param_Unbound;
import org.eclipse.titan.runtime.core.Param_Types.Module_Parameter;
import org.eclipse.titan.runtime.core.Param_Types.Module_Parameter.basic_check_bits_t;
import org.eclipse.titan.runtime.core.TitanVerdictType.VerdictTypeEnum;

/**
 * TTCN-3 component variable
 *
 * The component type from the compiler is represented as int.
 *
 * @author Kristof Szabados
 * @author Arpad Lovassy
 */
public class TitanComponent extends Base_Type {
	// the predefined component reference values
	public static final int NULL_COMPREF = 0;
	public static final int MTC_COMPREF = 1;
	public static final int SYSTEM_COMPREF = 2;
	public static final int FIRST_PTC_COMPREF = 3;
	public static final int ANY_COMPREF = -1;
	public static final int ALL_COMPREF = -2;
	public static final int UNBOUND_COMPREF = -3;
	//Pseudo-component for logging when the MTC is executing a controlpart
	public static final int CONTROL_COMPREF = -4;

	public static ThreadLocal<TitanComponent> self = new ThreadLocal<TitanComponent>() {
		@Override
		protected TitanComponent initialValue() {
			return new TitanComponent();
		}
	};

	private static class ComponentNameStruct {
		public int componentReference;
		public String componentName;
	}

	private static final ThreadLocal<ArrayList<ComponentNameStruct>> componentNames = new ThreadLocal<ArrayList<TitanComponent.ComponentNameStruct>>() {
		@Override
		protected ArrayList<ComponentNameStruct> initialValue() {
			return new ArrayList<ComponentNameStruct>();
		}
	};

	int componentValue;

	/**
	 * Initializes to unbound value.
	 * */
	public TitanComponent() {
		componentValue = UNBOUND_COMPREF;
	}

	/**
	 * Initializes to a given value.
	 *
	 * @param otherValue
	 *                the value to initialize to.
	 * */
	public TitanComponent(final int otherValue) {
		componentValue = otherValue;
	}

	/**
	 * Initializes to a given value.
	 *
	 * @param otherValue
	 *                the value to initialize to.
	 * */
	public TitanComponent(final TitanComponent otherValue) {
		otherValue.must_bound("Copying an unbound component reference.");

		componentValue = otherValue.componentValue;
	}

	/**
	 * Assigns the other value to this value.
	 * Overwriting the current content in the process.
	 *<p>
	 * operator= with component parameter in the core.
	 *
	 * @param otherValue
	 *                the other value to assign.
	 * @return the new value object.
	 */
	public TitanComponent operator_assign(final int otherValue) {
		componentValue = otherValue;

		return this;
	}

	/**
	 * Assigns the other value to this value.
	 * Overwriting the current content in the process.
	 *<p>
	 * operator= in the core.
	 *
	 * @param otherValue
	 *                the other value to assign.
	 * @return the new value object.
	 */
	public TitanComponent operator_assign(final TitanComponent otherValue) {
		otherValue.must_bound("Copying an unbound component reference.");

		componentValue = otherValue.componentValue;

		return this;
	}

	@Override
	public TitanComponent operator_assign(final Base_Type otherValue) {
		if (otherValue instanceof TitanComponent) {
			return operator_assign((TitanComponent)otherValue);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to component reference", otherValue));

	}


	/**
	 * Checks if the current value is equivalent to the provided one.
	 *
	 * operator== in the core
	 *
	 * @param otherValue
	 *                the other value to check against.
	 * @return {@code true} if the values are equivalent.
	 */
	public boolean operator_equals(final int otherValue) {
		must_bound("The left operand of comparison is an unbound component reference.");
		if (otherValue == UNBOUND_COMPREF) {
			throw new TtcnError("The right operand of comparison is an unbound component reference.");
		}

		return componentValue == otherValue;
	}

	/**
	 * Checks if the current value is equivalent to the provided one.
	 *
	 * operator== in the core
	 *
	 * @param otherValue
	 *                the other value to check against.
	 * @return {@code true} if the values are equivalent.
	 */
	public boolean operator_equals(final TitanComponent otherValue) {
		must_bound("The left operand of comparison is an unbound component reference.");
		otherValue.must_bound("The right operand of comparison is an unbound component reference.");

		return componentValue == otherValue.componentValue;
	}

	@Override
	public boolean operator_equals(final Base_Type otherValue) {
		if (otherValue instanceof TitanComponent) {
			return operator_equals((TitanComponent)otherValue);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to component reference", otherValue));
	}

	/**
	 * Checks if the current value is not equivalent to the provided one.
	 *
	 * operator!= in the core
	 *
	 * @param otherValue
	 *                the other value to check against.
	 * @return {@code true} if the values are not equivalent.
	 */
	public boolean operator_not_equals(final int otherValue) {
		return !operator_equals(otherValue);
	}

	/**
	 * Checks if the current value is not equivalent to the provided one.
	 *
	 * operator!= in the core
	 *
	 * @param otherValue
	 *                the other value to check against.
	 * @return {@code true} if the values are not equivalent.
	 */
	public boolean operator_not_equals(final TitanComponent otherValue) {
		return !operator_equals(otherValue);
	}

	// originally component cast operator
	public int get_component() {
		if ( componentValue == UNBOUND_COMPREF) {
			throw new TtcnError( "Using the value of an unbound component reference." );
		}

		return componentValue;
	}

	@Override
	public void clean_up() {
		componentValue = UNBOUND_COMPREF;
	}

	@Override
	public boolean is_present() {
		return componentValue != UNBOUND_COMPREF;
	}

	@Override
	public boolean is_bound() {
		return componentValue != UNBOUND_COMPREF;
	}

	@Override
	public boolean is_value() {
		return componentValue != UNBOUND_COMPREF;
	}

	@Override
	public void log() {
		if (componentValue == UNBOUND_COMPREF) {
			TTCN_Logger.log_event_unbound();
		} else {
			log_component_reference(componentValue);
		}
	}

	public TitanAlt_Status done(final TitanVerdictType value_redirect, final Index_Redirect index_redirection) {
		must_bound("Performing done operation on an unbound component reference.");

		final AtomicReference<VerdictTypeEnum> ptc_verdict = new AtomicReference<VerdictTypeEnum>(VerdictTypeEnum.NONE);

		final TitanAlt_Status status = TTCN_Runtime.component_done(componentValue, ptc_verdict);
		if (value_redirect != null) {
			value_redirect.operator_assign(ptc_verdict.get());
		}

		return status;
	}

	// originally killed
	public TitanAlt_Status killed(final Index_Redirect index_redirection) {
		must_bound("Performing killed operation on an unbound component reference.");

		return TTCN_Runtime.component_killed(componentValue);
	}

	// originally running
	public boolean running(final Index_Redirect index_redirection) {
		must_bound("Performing running operation on an unbound component reference.");

		return TTCN_Runtime.component_running(componentValue);
	}

	// originally alive
	public boolean alive(final Index_Redirect index_redirection) {
		must_bound("Performing alive operation on an unbound component reference.");

		return TTCN_Runtime.component_alive(componentValue);
	}

	public void stop() {
		must_bound("Performing stop operation on an unbound component reference.");

		TTCN_Runtime.stop_component(componentValue);
	}

	public void kill() {
		must_bound("Performing kill operation on an unbound component reference.");

		TTCN_Runtime.kill_component(componentValue);
	}

	@Override
	/** {@inheritDoc} */
	public void set_param(Module_Parameter param) {
		param.basic_check(basic_check_bits_t.BC_VALUE.getValue(), "component reference (integer or null) value");

		// Originally RT2
		if (param.get_type() == Module_Parameter.type_t.MP_Reference) {
			param = param.get_referenced_param().get();
		}

		switch (param.get_type()) {
		case MP_Integer:
			componentValue = param.get_integer().get_int();
			break;
		case MP_Ttcn_Null:
			componentValue = NULL_COMPREF;
			break;
		case MP_Ttcn_mtc:
			componentValue = MTC_COMPREF;
			break;
		case MP_Ttcn_system:
			componentValue = SYSTEM_COMPREF;
			break;
		default:
			param.type_error("component reference (integer or null) value");
			break;
		}
	}

	@Override
	/** {@inheritDoc} */
	public Module_Parameter get_param(final Module_Param_Name param_name){
		if (!is_bound()) {
			return new Module_Param_Unbound();
		}
		return new Module_Param_Ttcn_Null();
	}

	public static void log_component_reference(final int component_reference) {
		switch (component_reference) {
		case NULL_COMPREF:
			TTCN_Logger.log_event_str("null");
			break;
		case MTC_COMPREF:
			TTCN_Logger.log_event_str("mtc");
			break;
		case SYSTEM_COMPREF:
			TTCN_Logger.log_event_str("system");
			break;
		default: {
			final String component_name = get_component_name(component_reference);
			if (component_name == null) {
				TTCN_Logger.log_event_str(MessageFormat.format("{0}", component_reference));
			} else {
				TTCN_Logger.log_event_str(MessageFormat.format("{0}({1})", component_name, component_reference));
			}
			break;
		}
		}
	}

	public static String get_component_string(final int component_reference) {
		switch (component_reference) {
		case NULL_COMPREF:
			return "null";
		case MTC_COMPREF:
			return "mtc";
		case SYSTEM_COMPREF:
			return "system";
		default: {
			final String component_name = get_component_name(component_reference);
			if (component_name == null) {
				return MessageFormat.format("{0}", component_reference);
			} else {
				return MessageFormat.format("{0}({1})", component_name, component_reference);
			}
		}
		}
	}

	@Override
	/** {@inheritDoc} */
	public void encode_text(final Text_Buf text_buf) {
		must_bound("Text encoder: Encoding an unbound component reference.");

		text_buf.push_int(componentValue);
		switch (componentValue) {
		case NULL_COMPREF:
		case MTC_COMPREF:
		case SYSTEM_COMPREF:
			break;
		default:
			text_buf.push_string(get_component_name(componentValue));
			break;
		}
	}

	@Override
	/** {@inheritDoc} */
	public void decode_text(final Text_Buf text_buf) {
		componentValue = text_buf.pull_int().get_int();

		switch (componentValue) {
		case NULL_COMPREF:
		case MTC_COMPREF:
		case SYSTEM_COMPREF:
			break;
		default:
			final String componentName = text_buf.pull_string();
			register_component_name(componentValue, componentName);
			break;
		}
	}

	public static void register_component_name(final int component_reference, final String component_name) {
		if (self.get().componentValue == component_reference) {
			// the own name of the component will not be registered,
			// but check whether we got the right string
			final String local_name = TTCN_Runtime.get_component_name();
			if (component_name == null || component_name.length() == 0) {
				if (local_name != null) {
					throw new TtcnError(MessageFormat.format("Internal error: Trying to register the component reference of this PTC without any name, but this component has name {0}.", local_name));
				}
			} else {
				if (local_name == null) {
					throw new TtcnError(MessageFormat.format("Internal error: Trying to register the component reference of this PTC with name {0}, but this component does not have name.", component_name));
				} else if (!component_name.equals(local_name)) {
					throw new TtcnError(MessageFormat.format("Internal error: Trying to register the component reference of this PTC with name {0}, but this component has name {1}.", component_name, local_name));
				}
			}

			return;
		}

		int min = 0;
		final ArrayList<ComponentNameStruct> localComponentNames = componentNames.get();
		if (!localComponentNames.isEmpty()) {
			// perform a binary search to find the place for the component reference
			int max = localComponentNames.size() - 1;
			while (min < max) {
				final int mid = min + (max - min) / 2;
				if (localComponentNames.get(mid).componentReference < component_reference) {
					min = mid + 1;
				} else if (localComponentNames.get(mid).componentReference == component_reference) {
					min = mid;
					break;
				} else {
					max = mid;
				}
			}
			if (localComponentNames.get(min).componentReference == component_reference) {
				// the component reference is already registered
				final String stored_name = localComponentNames.get(min).componentName;
				if (component_name == null || component_name.length() == 0) {
					if (stored_name != null) {
						throw new TtcnError(MessageFormat.format("Internal error: Trying to register component reference {0} without any name, but this component reference is already registered with name {1}.", component_reference, stored_name));
					}
				} else {
					if (stored_name == null) {
						throw new TtcnError(MessageFormat.format("Internal error: Trying to register component reference {0} with name {1}, but this component reference is already registered without name.", component_reference, component_name));
					} else if (!component_name.equals(stored_name)) {
						throw new TtcnError(MessageFormat.format("Internal error: Trying to register component reference {0} with name {1}, but this component reference is already registered with a different name ({2}).", component_reference, component_name, stored_name));
					}
				}
				return;
			} else {
				if (localComponentNames.get(min).componentReference < component_reference) {
					min++;
				}
			}
		}

		final ComponentNameStruct tempElement = new ComponentNameStruct();
		tempElement.componentReference = component_reference;
		if (component_name == null || component_name.length() == 0) {
			tempElement.componentName = null;
		} else {
			tempElement.componentName = component_name;
		}

		localComponentNames.add(min, tempElement);
	}

	public static String get_component_name(final int component_reference) {
		final ArrayList<ComponentNameStruct> localComponentNames = componentNames.get();

		if (self.get().componentValue == component_reference) {
			return TTCN_Runtime.get_component_name();
		} else if(!localComponentNames.isEmpty()) {
			int min = 0;
			int max = localComponentNames.size() - 1;
			while (min < max) {
				final int mid = min + (max - min) / 2;
				if (localComponentNames.get(mid).componentReference < component_reference) {
					min = mid + 1;
				} else if (localComponentNames.get(mid).componentReference == component_reference) {
					return localComponentNames.get(mid).componentName;
				} else {
					max = mid;
				}
			}
			if (localComponentNames.get(min).componentReference != component_reference) {
				throw new TtcnError(MessageFormat.format("Internal error: Trying to get the name of PTC with component reference {0}, but the name of the component is not registered.", component_reference));
			}

			return localComponentNames.get(min).componentName;
		} else {
			throw new TtcnError(MessageFormat.format("Internal error: Trying to get the name of PTC with component reference {0}, but there are no component names registered.", component_reference));
		}
	}

	public static void clear_component_names() {
		componentNames.get().clear();
	}

	/**
	 * Checks if the first value is equivalent to the second one.
	 *
	 * static operator== in the core
	 *
	 * @param left_value
	 *                the first value.
	 * @param right_value
	 *                the second value to check against.
	 * @return {@code true} if the values are equivalent.
	 */
	public static boolean operator_equals(final int left_value, final TitanComponent right_value) {
		right_value.must_bound("Unbound right operand of float comparison.");

		return right_value.operator_equals(left_value);
	}

	/**
	 * Checks if the first value is not equivalent to the second one.
	 *
	 * static operator!= in the core
	 *
	 * @param left_value
	 *                the first value.
	 * @param right_value
	 *                the second value to check against.
	 * @return {@code true} if the values are not equivalent.
	 */
	public static boolean operator_not_equals(final int left_value, final TitanComponent right_value) {
		right_value.must_bound("Unbound right operand of float comparison.");

		return right_value.operator_not_equals(left_value);
	}
}
