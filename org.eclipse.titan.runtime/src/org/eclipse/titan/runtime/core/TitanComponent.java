/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

import java.text.MessageFormat;
import java.util.ArrayList;

/**
 * TTCN-3 component variable
 *
 * //FIXME a lot to implement
 * The component type from the compiler is represented as int.
 *
 * @author Kristof Szabados
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

	//TODO might not be needed
	private static int numberOfComponentNames = 0;
	private static final ArrayList<ComponentNameStruct> componentNames = new ArrayList<TitanComponent.ComponentNameStruct>();

	int componentValue;

	public TitanComponent() {
		componentValue = UNBOUND_COMPREF;
	}

	public TitanComponent(final int aOtherValue) {
		componentValue = aOtherValue;
	}

	public TitanComponent(final TitanComponent aOtherValue) {
		if (aOtherValue.componentValue == UNBOUND_COMPREF) {
			throw new TtcnError("Copying an unbound component reference.");
		}

		componentValue = aOtherValue.componentValue;
	}

	//originally operator=
	public TitanComponent assign(final int otherValue) {
		componentValue = otherValue;

		return this;
	}

	//originally operator=
	public TitanComponent assign(final TitanComponent otherValue) {
		if (otherValue.componentValue == UNBOUND_COMPREF) {
			throw new TtcnError("Copying an unbound component reference.");
		}

		componentValue = otherValue.componentValue;

		return this;
	}

	@Override
	public TitanComponent assign(final Base_Type otherValue) {
		if (otherValue instanceof TitanComponent) {
			return assign((TitanComponent)otherValue);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to component reference", otherValue));

	}


	//originally operator==
	public boolean operatorEquals(final int otherValue) {
		if (componentValue == UNBOUND_COMPREF) {
			throw new TtcnError("The left operand of comparison is an unbound component reference.");
		}
		if (otherValue == UNBOUND_COMPREF) {
			throw new TtcnError("The right operand of comparison is an unbound component reference.");
		}

		return componentValue == otherValue;
	}

	//originally operator==
	public boolean operatorEquals(final TitanComponent otherValue) {
		if (componentValue == UNBOUND_COMPREF) {
			throw new TtcnError("The left operand of comparison is an unbound component reference.");
		}
		if (otherValue.componentValue == UNBOUND_COMPREF) {
			throw new TtcnError("The right operand of comparison is an unbound component reference.");
		}

		return componentValue == otherValue.componentValue;
	}

	@Override
	public boolean operatorEquals(final Base_Type otherValue) {
		if (otherValue instanceof TitanComponent) {
			return operatorEquals((TitanComponent)otherValue);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to component reference", otherValue));
	}

	// originally component cast operator
	public int getComponent() {
		if (componentValue == UNBOUND_COMPREF) {
			throw new TtcnError("Using the value of an unbound component reference.");
		}

		return componentValue;
	}

	public void cleanUp() {
		componentValue = UNBOUND_COMPREF;
	}

	@Override
	public boolean isPresent() {
		return componentValue != UNBOUND_COMPREF;
	}

	@Override
	public boolean isBound() {
		return componentValue != UNBOUND_COMPREF;
	}

	@Override
	public boolean isValue() {
		return componentValue != UNBOUND_COMPREF;
	}

	public void mustBound(final String aErrorMessage) {
		if (componentValue == UNBOUND_COMPREF) {
			throw new TtcnError(aErrorMessage);
		}
	}

	public void log() {
		if (componentValue == UNBOUND_COMPREF) {
			TtcnLogger.log_event_unbound();
		} else {
			log_component_reference(this);
		}
	}

	// originally done, TODO needs index redirection support
	public TitanAlt_Status done() {
		if (componentValue == UNBOUND_COMPREF) {
			throw new TtcnError("Performing done operation on an unbound component reference.");
		}

		return TTCN_Runtime.component_done(componentValue);
	}

	// originally killed, TODO needs index redirection support
	public TitanAlt_Status killed() {
		if (componentValue == UNBOUND_COMPREF) {
			throw new TtcnError("Performing killed operation on an unbound component reference.");
		}

		return TTCN_Runtime.component_killed(componentValue);
	}

	// originally running, TODO needs index redirection support
	public boolean running() {
		if (componentValue == UNBOUND_COMPREF) {
			throw new TtcnError("Performing running operation on an unbound component reference.");
		}

		return TTCN_Runtime.component_running(componentValue);
	}

	// originally alive, TODO needs index redirection support
	public boolean alive() {
		if (componentValue == UNBOUND_COMPREF) {
			throw new TtcnError("Performing alive operation on an unbound component reference.");
		}

		return TTCN_Runtime.component_alive(componentValue);
	}

	public void stop() {
		if (componentValue == UNBOUND_COMPREF) {
			throw new TtcnError("Performing stop operation on an unbound component reference.");
		}

		TTCN_Runtime.stop_component(componentValue);
	}

	public void kill() {
		if (componentValue == UNBOUND_COMPREF) {
			throw new TtcnError("Performing kill operation on an unbound component reference.");
		}

		TTCN_Runtime.kill_component(componentValue);
	}

	private static void log_component_reference(final TitanComponent component_reference) {
		switch (component_reference.componentValue) {
		case NULL_COMPREF:
			TtcnLogger.log_event_str("null");
			break;
		case MTC_COMPREF:
			TtcnLogger.log_event_str("mtc");
			break;
		case SYSTEM_COMPREF:
			TtcnLogger.log_event_str("system");
			break;
		default:
			//FIXME implement
			break;
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
		default:
			//FIXME implement
			return "FIXME implement get_component_string";
		}
	}

	@Override
	/** {@inheritDoc} */
	public void encode_text(final Text_Buf text_buf) {
		if (componentValue == UNBOUND_COMPREF) {
			throw new TtcnError("Text encoder: Encoding an unbound component reference.");
		}

		text_buf.push_int(componentValue);
		switch (componentValue) {
		case NULL_COMPREF:
		case MTC_COMPREF:
		case SYSTEM_COMPREF:
			break;
		default:
			text_buf.push_string(get_component_string(componentValue));
			break;
		}
	}

	@Override
	/** {@inheritDoc} */
	public void decode_text(final Text_Buf text_buf) {
		componentValue = text_buf.pull_int().getInt();

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
		if (componentNames.size() > 0) {
			// perform a binary search to find the place for the component reference
			int max = numberOfComponentNames - 1;
			while (min < max) {
				final int mid = min + (max - min) / 2;
				if (componentNames.get(mid).componentReference < component_reference) {
					min = mid + 1;
				} else if (componentNames.get(mid).componentReference == component_reference) {
					min = mid;
					break;
				} else {
					max = mid;
				}
			}
			if (componentNames.get(min).componentReference == component_reference) {
				// the component reference is already registered
				final String stored_name = componentNames.get(min).componentName;
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
				if (componentNames.get(min).componentReference < component_reference) {
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
		componentNames.add(min, tempElement);
		numberOfComponentNames++;
	}
}
