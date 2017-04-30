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
 * TTCN-3 boolean
 * @author Arpad Lovassy
 */
public class TitanBoolean extends Base_Type {

	/**
	 * boolean_value in core.
	 * Unbound if null
	 */
	private Boolean boolean_value;

	public TitanBoolean() {
		super();
	}

	public TitanBoolean( final Boolean aOtherValue ) {
		boolean_value = aOtherValue;
	}

	public TitanBoolean( final TitanBoolean aOtherValue ) {
		aOtherValue.mustBound( "Copying an unbound boolean value." );
		boolean_value = aOtherValue.boolean_value;
	}

	public Boolean getValue() {
		return boolean_value;
	}

	public void setValue( final Boolean aOtherValue ) {
		boolean_value = aOtherValue;
	}

	//originally operator=
	public TitanBoolean assign( final boolean aOtherValue ) {
		boolean_value = aOtherValue;

		return this;
	}

	//originally operator=
	public TitanBoolean assign( final TitanBoolean aOtherValue ) {
		aOtherValue.mustBound( "Assignment of an unbound boolean value." );
		boolean_value = aOtherValue.boolean_value;

		return this;
	}

	@Override
	public TitanBoolean assign(final Base_Type otherValue) {
		if (otherValue instanceof TitanBoolean) {
			return assign((TitanBoolean)otherValue);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to boolean", otherValue));
	}

	public boolean isBound() {
		return boolean_value != null;
	}

	public boolean isPresent() {
		return isBound();
	};

	public boolean isValue() {
		return boolean_value != null;
	}

	public void mustBound( final String aErrorMessage ) {
		if ( boolean_value == null ) {
			throw new TtcnError( aErrorMessage );
		}
	}

	/**
	 * this or aOtherValue
	 * originally operator or
	 */
	public TitanBoolean or( final boolean aOtherValue ) {
		mustBound( "Unbound left operand of boolean or operation." );

		return new TitanBoolean( boolean_value || aOtherValue );
	}

	/**
	 * this or aOtherValue
	 * originally operator or
	 */
	public TitanBoolean or( final TitanBoolean aOtherValue ) {
		mustBound( "Unbound left operand of boolean or operation." );
		aOtherValue.mustBound( "Unbound right operand of boolean or operation." );

		return new TitanBoolean( boolean_value || aOtherValue.boolean_value );
	}

	/**
	 * this and aOtherValue
	 * originally operator and
	 */
	public TitanBoolean and( final boolean aOtherValue ) {
		mustBound( "Unbound left operand of boolean and operation." );

		return new TitanBoolean( boolean_value && aOtherValue );
	}

	/**
	 * this and aOtherValue
	 * originally operator and
	 */
	public TitanBoolean and( final TitanBoolean aOtherValue ) {
		mustBound( "Unbound left operand of boolean and operation." );
		aOtherValue.mustBound( "Unbound right operand of boolean and operation." );

		return new TitanBoolean( boolean_value && aOtherValue.boolean_value );
	}

	/**
	 * this xor aOtherValue
	 * originally operator ^
	 */
	public TitanBoolean xor(final boolean aOtherValue) {
		mustBound("The left operand of xor operator is an unbound boolean value.");

		return new TitanBoolean( boolean_value.booleanValue() != aOtherValue);
	}

	/**
	 * this xor aOtherValue
	 * originally operator ^
	 */
	public TitanBoolean xor(final TitanBoolean aOtherValue) {
		mustBound("The left operand of xor operator is an unbound boolean value.");
		aOtherValue.mustBound("The right operand of xor operator is an unbound boolean value.");

		return new TitanBoolean( boolean_value.booleanValue() != aOtherValue.boolean_value.booleanValue());
	}

	/**
	 * this == aOtherValue
	 * originally operator ==
	 */
	public TitanBoolean equalsTo( final boolean aOtherValue ) {
		mustBound( "Unbound left operand of boolean equals operation." );

		return new TitanBoolean( boolean_value == aOtherValue );
	}

	/**
	 * this == aOtherValue
	 * originally operator ==
	 */
	public TitanBoolean equalsTo( final TitanBoolean aOtherValue ) {
		mustBound( "Unbound left operand of boolean equals operation." );
		aOtherValue.mustBound( "Unbound right operand of boolean equals operation." );

		return new TitanBoolean( boolean_value == aOtherValue.boolean_value );
	}

	/**
	 * not this
	 * originally operator not
	 */
	public TitanBoolean not() {
		mustBound( "Unbound left operand of boolean and operation." );

		return new TitanBoolean( !boolean_value );
	}

	//originally operator==
	public boolean operatorEquals( final TitanBoolean aOtherValue ) {
		mustBound("Unbound left operand of boolean comparison.");
		aOtherValue.mustBound("Unbound right operand of boolean comparison.");

		return boolean_value.equals(aOtherValue.boolean_value);
	}

	@Override
	public boolean operatorEquals(final Base_Type otherValue) {
		if (otherValue instanceof TitanBoolean) {
			return operatorEquals((TitanBoolean)otherValue);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to boolean", otherValue));
	}

	//originally operator!=
	public boolean operatorNotEquals( final TitanBoolean aOtherValue ) {
		return !operatorEquals( aOtherValue );
	}

	public void cleanUp() {
		boolean_value = null;
	}

	@Override
	public String toString() {
		if ( boolean_value == null ) {
			return "<unbound>";
		}
		return boolean_value.toString();
	}

	public static boolean getNative(final boolean value) {
		return value;
	}

	public static boolean getNative(final TitanBoolean otherValue) {
		return otherValue.getValue();
	}
}
