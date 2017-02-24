/******************************************************************************
 * Copyright (c) 2000-2017 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

/**
 * TTCN-3 float
 * @author Arpad Lovassy
 */
public class TitanFloat {

	//TODO: implement ANY and/or OMIT

	/**
	 * float value.
	 * It can be null if ANY or OMIT
	 */
	private Double float_value;

	public TitanFloat() {
		super();
	}

	public TitanFloat( final double aOtherValue ) {
		float_value = aOtherValue;
	}

	public TitanFloat( final TitanFloat aOtherValue ) {
		aOtherValue.mustBound( "Copying an unbound float value." );
		float_value = aOtherValue.float_value;
	}

	public Double getValue() {
		return float_value;
	}

	public void setValue( final double aOtherValue ) {
		float_value = aOtherValue;
	}

	//originally operator=
	public TitanFloat assign( final TitanFloat aOtherValue ) {
		aOtherValue.mustBound( "Assignment of an unbound float value." );
		float_value = aOtherValue.float_value;

		return this;
	}

	public boolean isBound() {
		return float_value != null;
	}

	public boolean isValue() {
		return float_value != null;
	}

	public void mustBound( final String aErrorMessage ) {
		if ( float_value == null ) {
			throw new TtcnError( aErrorMessage );
		}
	}

	/**
	 * this + aOtherValue
	 * originally operator&
	 */
	public TitanFloat append( final TitanFloat aOtherValue ) {
		mustBound( "Unbound left operand of float addition." );
		aOtherValue.mustBound( "Unbound right operand of float addition." );

		return new TitanFloat( float_value + aOtherValue.float_value );
	}

	//originally operator==
	public boolean operatorEquals( final TitanFloat aOtherValue ) {
		mustBound("Unbound left operand of float comparison.");
		aOtherValue.mustBound("Unbound right operand of float comparison.");

		return float_value.equals(aOtherValue.float_value);
	}

	//originally operator!=
	public boolean operatorNotEquals( final TitanFloat aOtherValue ) {
		return !operatorEquals( aOtherValue );
	}

	//originally operator <
	public boolean isLessThan(final TitanFloat otherValue) {
		mustBound("Unbound left operand of float comparison.");
		otherValue.mustBound("Unbound right operand of float comparison.");
		return float_value < otherValue.float_value;
	}

	//originally operator >
	public boolean isGreaterThan(final TitanFloat otherValue) {
		mustBound("Unbound left operand of float comparison.");
		otherValue.mustBound("Unbound right operand of float comparison.");
		return float_value > otherValue.float_value;
	}

	//originally operator <=
	public boolean isLessThanOrEqual(final TitanFloat otherValue) {
		return !isGreaterThan(otherValue);
	}

	//originally operator >=
	public boolean isGreaterThanOrEqual(final TitanFloat otherValue) {
		return !isGreaterThan(otherValue);
	}

	public void cleanUp() {
		float_value = null;
	}

	@Override
	public String toString() {
		if ( float_value == null ) {
			return "<unbound>";
		}
		return float_value.toString();
	}
}
