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

	public static TitanComponent self = new TitanComponent();

	private class ComponentNameStruct {
		public int componentReference;
		public String componentName;
	}

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

	public void cleanUp() {
		componentValue = UNBOUND_COMPREF;
	}

	@Override
	public TitanBoolean isPresent() {
		return new TitanBoolean(componentValue != UNBOUND_COMPREF);
	}

	@Override
	public TitanBoolean isBound() {
		return new TitanBoolean(componentValue != UNBOUND_COMPREF);
	}

	@Override
	public TitanBoolean isValue() {
		return new TitanBoolean(componentValue != UNBOUND_COMPREF);
	}

	public void mustBound( final String aErrorMessage ) {
		if ( componentValue == UNBOUND_COMPREF ) {
			throw new TtcnError( aErrorMessage );
		}
	}

	// originally done, TODO needs index redirection support
	public TitanAlt_Status done() {
		if (componentValue == UNBOUND_COMPREF) {
			throw new TtcnError("Performing done operation on an unbound component reference.");
		}

		return TTCN_Runtime.component_done(componentValue);
	}

	//originally operator==
	public TitanBoolean operatorEquals(final int otherValue) {
		if (componentValue == UNBOUND_COMPREF) {
			throw new TtcnError("The left operand of comparison is an unbound component reference.");
		}
		if (otherValue == UNBOUND_COMPREF) {
			throw new TtcnError("The right operand of comparison is an unbound component reference.");
		}

		return new TitanBoolean(componentValue == otherValue);
	}

	//originally operator==
	public TitanBoolean operatorEquals(final TitanComponent otherValue) {
		if (componentValue == UNBOUND_COMPREF) {
			throw new TtcnError("The left operand of comparison is an unbound component reference.");
		}
		if (otherValue.componentValue == UNBOUND_COMPREF) {
			throw new TtcnError("The right operand of comparison is an unbound component reference.");
		}

		return new TitanBoolean(componentValue == otherValue.componentValue);
	}

	@Override
	public TitanBoolean operatorEquals(final Base_Type otherValue) {
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

}
