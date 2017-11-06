/******************************************************************************
 * Copyright (c) 2000-2017 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

import java.text.MessageFormat;

/**
 * TTCN-3 float
 * @author Arpad Lovassy
 * @author Farkas Izabella Ingrid
 * @author Andrea Pálfi
 */
public class TitanFloat extends Base_Type {
	/**
	 * float value.
	 */
	private Ttcn3Float float_value;

	//static members PLUS_INFINITY, MINUS_INFINITY, NOT_A_NUMBER
	public static final double PLUS_INFINITY = Double.POSITIVE_INFINITY;
	public static final double MINUS_INFINITY = Double.NEGATIVE_INFINITY;
	public static final double NOT_A_NUMBER = Double.NaN;
	public static final double MIN_DECIMAL_FLOAT = 1.0E-4;
	public static final double MAX_DECIMAL_FLOAT = 1.0E+10;

	public TitanFloat() {
		super();
	}

	public TitanFloat( final double aOtherValue ) {
		float_value = new Ttcn3Float( aOtherValue );
	}

	public TitanFloat( final Ttcn3Float aOtherValue ) {
		float_value = aOtherValue;
	}

	public TitanFloat( final TitanFloat aOtherValue ) {
		aOtherValue.mustBound( "Copying an unbound float value." );

		float_value = aOtherValue.float_value;
	}

	public Double getValue() {
		return float_value.getValue();
	}

	// originally operator=
	public TitanFloat assign(final double aOtherValue) {
		float_value = new Ttcn3Float(aOtherValue);

		return this;
	}

	// originally operator=
	public TitanFloat assign(final Ttcn3Float aOtherValue) {
		float_value = aOtherValue;

		return this;
	}

	// originally operator=
	public TitanFloat assign(final TitanFloat aOtherValue) {
		aOtherValue.mustBound("Assignment of an unbound float value.");

		if (aOtherValue != this) {
			float_value = aOtherValue.float_value;
		}

		return this;
	}

	@Override
	public TitanFloat assign(final Base_Type otherValue) {
		if (otherValue instanceof TitanFloat) {
			return assign((TitanFloat) otherValue);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to float", otherValue));
	}

	public boolean isBound() {
		return float_value != null;
	}

	public boolean isPresent() {
		return isBound();
	};

	public boolean isValue() {
		return float_value != null;
	}

	public void mustBound(final String aErrorMessage) {
		if (float_value == null) {
			throw new TtcnError(aErrorMessage);
		}
	}

	// isspecial
	public static TitanBoolean isSpecial(final double aOtherValue) {
		return new TitanBoolean(aOtherValue == PLUS_INFINITY || aOtherValue == MINUS_INFINITY || Double.isNaN(aOtherValue));
	}

	/**
	 * this + aOtherValue
	 * originally operator+
	 */

	public TitanFloat add() {
		mustBound("Unbound float operand of unary + operator.");

		return new TitanFloat(float_value);
	}

	/**
	 * this + aOtherValue
	 * originally operator+
	 */
	public TitanFloat add(final double aOtherValue) {
		mustBound("Unbound left operand of float addition.");

		return new TitanFloat(float_value.add(aOtherValue));
	}

	/**
	 * this + aOtherValue
	 * originally operator+
	 */
	public TitanFloat add(final Ttcn3Float aOtherValue) {
		mustBound("Unbound left operand of float addition.");

		return new TitanFloat(float_value.add(aOtherValue.getValue()));
	}

	/**
	 * this + aOtherValue
	 * originally operator+
	 */
	public TitanFloat add(final TitanFloat aOtherValue) {
		mustBound("Unbound left operand of float addition.");
		aOtherValue.mustBound("Unbound right operand of float addition.");

		return new TitanFloat(float_value.add(aOtherValue.float_value.getValue()));
	}

	/**
	 * this - aOtherValue
	 * originally operator-
	 */
	public TitanFloat sub() {
		mustBound("Unbound float operand of unary - operator (negation).");

		return new TitanFloat(-float_value.getValue());
	}

	/**
	 * this - aOtherValue
	 * originally operator-
	 */
	public TitanFloat sub(final double aOtherValue) {
		mustBound("Unbound left operand of float subtraction.");

		return new TitanFloat(float_value.sub(aOtherValue));
	}

	/**
	 * this - aOtherValue
	 * originally operator-
	 */
	public TitanFloat sub(final Ttcn3Float aOtherValue) {
		mustBound("Unbound left operand of float subtraction.");

		return new TitanFloat(float_value.sub(aOtherValue.getValue()));
	}

	/**
	 * this - aOtherValue
	 * originally operator-
	 */
	public TitanFloat sub(final TitanFloat aOtherValue) {
		mustBound("Unbound left operand of float subtraction.");
		aOtherValue.mustBound("Unbound right operand of float subtraction.");

		return new TitanFloat(float_value.sub(aOtherValue.float_value.getValue()));
	}

	/**
	 * this * aOtherValue
	 * originally operator*
	 */
	public TitanFloat mul(final double aOtherValue) {
		mustBound("Unbound left operand of float multiplication.");

		return new TitanFloat(float_value.mul(aOtherValue));
	}

	/**
	 * this * aOtherValue
	 * originally operator*
	 */
	public TitanFloat mul(final Ttcn3Float aOtherValue) {
		mustBound("Unbound left operand of float multiplication.");

		return new TitanFloat(float_value.mul(aOtherValue.getValue()));
	}

	/**
	 * this * aOtherValue
	 * originally operator*
	 */
	public TitanFloat mul(final TitanFloat aOtherValue) {
		mustBound("Unbound left operand of float multiplication.");
		aOtherValue.mustBound("Unbound right operand of float multiplication.");

		return new TitanFloat(float_value.mul(aOtherValue.float_value.getValue()));
	}

	/**
	 * this / aOtherValue
	 * originally operator/
	 */
	public TitanFloat div(final double aOtherValue) {
		mustBound("Unbound left operand of float division.");

		if (aOtherValue == 0.0) {
			throw new TtcnError("Float division by zero.");
		}

		return new TitanFloat(float_value.div(aOtherValue));
	}

	/**
	 * this / aOtherValue
	 * originally operator/
	 */
	public TitanFloat div(final Ttcn3Float aOtherValue) {
		mustBound("Unbound left operand of float division.");

		if (aOtherValue.getValue() == 0.0) {
			throw new TtcnError("Float division by zero.");
		}

		return new TitanFloat(float_value.div(aOtherValue.getValue()));
	}

	/**
	 * this / aOtherValue
	 * originally operator/
	 */
	public TitanFloat div(final TitanFloat aOtherValue) {
		mustBound("Unbound left operand of float division.");
		aOtherValue.mustBound("Unbound right operand of float division.");

		final double otherValue = aOtherValue.float_value.getValue();
		if (otherValue == 0.0) {
			throw new TtcnError("Float division by zero.");
		}

		return new TitanFloat(float_value.div(otherValue));
	}

	// operatorEquals native
	public boolean operatorEquals(final double aOtherValue) {
		mustBound("Unbound left operand of float comparison.");

		return float_value.operatorEquals(aOtherValue);
	}

	// operatorEquals native
	public boolean operatorEquals(final Ttcn3Float aOtherValue) {
		mustBound("Unbound left operand of float comparison.");

		return float_value.operatorEquals(aOtherValue.getValue());
	}

	// originally operator==
	public boolean operatorEquals(final TitanFloat aOtherValue) {
		mustBound("Unbound left operand of float comparison.");
		aOtherValue.mustBound("Unbound right operand of float comparison.");

		return float_value.operatorEquals(aOtherValue.float_value.getValue());
	}

	@Override
	public boolean operatorEquals(final Base_Type otherValue) {
		if (otherValue instanceof TitanFloat) {
			return operatorEquals((TitanFloat) otherValue);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to charstring", otherValue));
	}

	// operatorNotEquals native
	public boolean operatorNotEquals(final double aOtherValue) {
		return !operatorEquals(aOtherValue);
	}

	// operatorNotEquals native
	public boolean operatorNotEquals(final Ttcn3Float aOtherValue) {
		return !operatorEquals(aOtherValue);
	}

	// originally operator!=
	public boolean operatorNotEquals(final TitanFloat aOtherValue) {
		return !operatorEquals(aOtherValue);
	}

	// originally operator <
	public boolean isLessThan(final double otherValue) {
		mustBound("Unbound left operand of float comparison.");

		return float_value.isLessThan(otherValue);
	}

	// originally operator <
	public boolean isLessThan(final Ttcn3Float otherValue) {
		mustBound("Unbound left operand of float comparison.");

		return float_value.isLessThan(otherValue.getValue());
	}

	// originally operator <
	public boolean isLessThan(final TitanFloat otherValue) {
		mustBound("Unbound left operand of float comparison.");
		otherValue.mustBound("Unbound right operand of float comparison.");

		return float_value.isLessThan(otherValue.float_value.getValue());
	}

	// originally operator >
	public boolean isGreaterThan(final double otherValue) {
		mustBound("Unbound left operand of float comparison.");

		return float_value.isGreaterThan(otherValue);
	}

	// originally operator >
	public boolean isGreaterThan(final Ttcn3Float otherValue) {
		mustBound("Unbound left operand of float comparison.");

		return float_value.isGreaterThan(otherValue.getValue());
	}

	// originally operator >
	public boolean isGreaterThan(final TitanFloat otherValue) {
		mustBound("Unbound left operand of float comparison.");
		otherValue.mustBound("Unbound right operand of float comparison.");

		return float_value.isGreaterThan(otherValue.float_value.getValue());
	}

	// originally operator <=
	public boolean isLessThanOrEqual(final double otherValue) {
		return !isGreaterThan(otherValue);
	}

	// originally operator <=
	public boolean isLessThanOrEqual(final Ttcn3Float otherValue) {
		return !isGreaterThan(otherValue);
	}

	// originally operator <=
	public boolean isLessThanOrEqual(final TitanFloat otherValue) {
		return !isGreaterThan(otherValue);
	}

	// originally operator >=
	public boolean isGreaterThanOrEqual(final double otherValue) {
		return !isLessThan(otherValue);
	}

	// originally operator >=
	public boolean isGreaterThanOrEqual(final Ttcn3Float otherValue) {
		return !isLessThan(otherValue);
	}

	// originally operator >=
	public boolean isGreaterThanOrEqual(final TitanFloat otherValue) {
		return !isLessThan(otherValue);
	}

	public void log() {
		if (float_value != null) {
			log_float(float_value.getValue());
		} else {
			TtcnLogger.log_event_unbound();
		}
	}

	public void cleanUp() {
		float_value = null;
	}

	static void log_float(final double float_val) {
		if ((float_val > -TitanFloat.MAX_DECIMAL_FLOAT && float_val <= -TitanFloat.MIN_DECIMAL_FLOAT)
				|| (float_val >= MIN_DECIMAL_FLOAT && float_val < TitanFloat.MAX_DECIMAL_FLOAT) || (float_val == 0.0)) {
			TtcnLogger.log_event("%f", float_val);
		} else if (float_val == PLUS_INFINITY) {
			TtcnLogger.log_event_str("infinity");
		} else if (float_val == MINUS_INFINITY) {
			TtcnLogger.log_event_str("-infinity");
		} else if (float_val != float_val) {
			TtcnLogger.log_event_str("not_a_number");
		} else {
			TtcnLogger.log_event("%e", float_val);
		}
	}

	@Override
	public String toString() {
		if ( float_value == null ) {
			return "<unbound>";
		}

		return float_value.toString();
	}

	// static add
	public static TitanFloat add(final double doubleValue, final TitanFloat otherValue) {
		otherValue.mustBound("Unbound right operand of float addition.");

		return new TitanFloat(otherValue.add(doubleValue));
	}

	// static sub
	public static TitanFloat sub(final double doubleValue, final TitanFloat otherValue) {
		otherValue.mustBound("Unbound right operand of float subtraction.");

		return new TitanFloat(doubleValue - otherValue.getValue());
	}

	// static mul
	public static TitanFloat mul(final double doubleValue, final TitanFloat otherValue) {
		otherValue.mustBound("Unbound right operand of float multiplication.");

		return new TitanFloat(otherValue.mul(doubleValue));
	}

	// static div
	public static TitanFloat div(final double doubleValue, final TitanFloat otherValue) {
		otherValue.mustBound("Unbound right operand of float division.");

		final double value = otherValue.float_value.getValue();
		if (value == 0.0) {
			throw new TtcnError("Float division by zero.");
		}

		return new TitanFloat(doubleValue / otherValue.getValue());
	}

	// static operatorEquals
	public static boolean operatorEquals(final double doubleValue, final TitanFloat otherValue) {
		otherValue.mustBound("Unbound right operand of float comparison.");

		return otherValue.operatorEquals(doubleValue);
	}

	// static operatorNotEquals
	public static boolean operatorNotEquals(final double doubleValue, final TitanFloat otherValue) {
		otherValue.mustBound("Unbound right operand of float comparison.");

		return otherValue.operatorNotEquals(doubleValue);
	}

	// static isLess
	public static TitanBoolean isLessThan(final double doubleValue, final TitanFloat otherValue) {
		otherValue.mustBound("Unbound right operand of float comparison.");

		return new TitanBoolean(otherValue.isGreaterThan(new TitanFloat(doubleValue)));
	}

	// static isGreaterThan
	public static TitanBoolean isGreaterThan(final double doubleValue, final TitanFloat otherValue) {
		otherValue.mustBound("Unbound right operand of float comparison.");

		return new TitanBoolean(otherValue.isLessThan(new TitanFloat(doubleValue)));
	}

	// static isLessThanOrEqual
	public static TitanBoolean isLessThanOrEqual(final double doubleValue, final TitanFloat otherValue) {
		otherValue.mustBound("Unbound right operand of float comparison.");

		return new TitanBoolean(otherValue.isGreaterThanOrEqual(new TitanFloat(doubleValue)));
	}

	// static isGreaterThanOrEqual
	public static TitanBoolean isGreaterThanOrEqual(final double doubleValue, final TitanFloat otherValue) {
		otherValue.mustBound("Unbound right operand of float comparison.");

		return new TitanBoolean(otherValue.isLessThanOrEqual(new TitanFloat(doubleValue)));
	}
}
