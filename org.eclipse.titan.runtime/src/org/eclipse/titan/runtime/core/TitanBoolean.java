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
 * @author Gergo Ujhelyi
 * @author Andrea Pálfi
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

		if (aOtherValue != this) {
			boolean_value = aOtherValue.boolean_value;
		}

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
	public boolean or( final boolean aOtherValue ) {
		mustBound( "The left operand of or operator is an unbound boolean value." );

		return boolean_value || aOtherValue;
	}

	/**
	 * this or aOtherValue
	 * originally operator or
	 */
	public boolean or( final TitanBoolean aOtherValue ) {
		mustBound( "The left operand of or operator is an unbound boolean value." );
		aOtherValue.mustBound( "The right operand of or operator is an unbound boolean value." );

		return boolean_value || aOtherValue.boolean_value;
	}

	/**
	 * this and aOtherValue
	 * originally operator and
	 */
	public boolean and( final boolean aOtherValue ) {
		mustBound( "The left operand of and operator is an unbound boolean value." );

		return boolean_value && aOtherValue;
	}

	/**
	 * this and aOtherValue
	 * originally operator and
	 */
	public boolean and( final TitanBoolean aOtherValue ) {
		mustBound( "The left operand of and operator is an unbound boolean value." );
		aOtherValue.mustBound( "The right operand of and operator is an unbound boolean value." );

		return boolean_value && aOtherValue.boolean_value;
	}

	/**
	 * this xor aOtherValue
	 * originally operator ^
	 */
	public boolean xor(final boolean aOtherValue) {
		mustBound("The left operand of xor operator is an unbound boolean value.");

		return boolean_value.booleanValue() != aOtherValue;
	}

	/**
	 * this xor aOtherValue
	 * originally operator ^
	 */
	public boolean xor(final TitanBoolean aOtherValue) {
		mustBound("The left operand of xor operator is an unbound boolean value.");
		aOtherValue.mustBound("The right operand of xor operator is an unbound boolean value.");

		return boolean_value.booleanValue() != aOtherValue.boolean_value.booleanValue();
	}

	/**
	 * not this
	 * originally operator not
	 */
	public boolean not() {
		mustBound( "The operand of not operator is an unbound boolean value." );

		return !boolean_value;
	}

	//originally operator==
	public boolean operatorEquals( final TitanBoolean aOtherValue ) {
		mustBound("The left operand of comparison is an unbound boolean value.");
		aOtherValue.mustBound("The right operand of comparison is an unbound boolean value.");

		return boolean_value.equals(aOtherValue.boolean_value);
	}

	//originally operator==
	public boolean operatorEquals(final boolean otherValue){
		mustBound("The left operand of comparison is an unbound boolean value.");

		return boolean_value == otherValue;
	}


	@Override
	public boolean operatorEquals(final Base_Type otherValue) {
		if (otherValue instanceof TitanBoolean) {
			return operatorEquals((TitanBoolean)otherValue);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to boolean", otherValue));
	}

	//originally operator !=
	public boolean operatorNotEquals(final boolean otherValue){
		mustBound("The left operand of comparison is an unbound boolean value.");

		return !operatorEquals(otherValue);
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

	//log()
	public void log(){
		if(boolean_value != null){
			TtcnLogger.log_event_str(boolean_value.toString());
		}else{
			TtcnLogger.log_event_unbound();
		}
	}

	@Override
	/** {@inheritDoc} */
	public void encode_text(final Text_Buf text_buf) {
		mustBound("Text encoder: Encoding an unbound boolean value.");

		text_buf.push_int(boolean_value ? 1 : 0);
	}

	@Override
	/** {@inheritDoc} */
	public void decode_text(final Text_Buf text_buf) {
		final int int_value = text_buf.pull_int().getInt();
		switch(int_value) {
		case 0:
			boolean_value = false;
			break;
		case 1:
			boolean_value = true;
			break;
		default:
			throw new TtcnError(MessageFormat.format("Text decoder: An invalid boolean value ({0}) was received.", int_value));
		}
	}

	public static boolean getNative(final boolean value) {
		return value;
	}

	public static boolean getNative(final TitanBoolean otherValue) {
		return otherValue.getValue();
	}

	//static and
	public static boolean and(final boolean boolValue, final TitanBoolean otherValue) {
		if(!boolValue){
			return false;
		}
		otherValue.mustBound("The right operand of and operator is an unbound boolean value.");

		return otherValue.boolean_value;
	}

	//static or
	public static boolean or(final boolean boolValue, final TitanBoolean otherValue){
		if(boolValue){
			return true;
		}
		otherValue.mustBound("The right operand of or operator is an unbound boolean value.");

		return otherValue.boolean_value;
	}

	//static xor
	public static boolean xor(final boolean boolValue, final TitanBoolean otherValue){
		otherValue.mustBound("The right operand of xor operator is an unbound boolean value.");

		return boolValue != otherValue.boolean_value;
	}

	//static equals
	public static boolean operatorEquals(final boolean boolValue, final TitanBoolean otherValue){
		otherValue.mustBound("The right operand of comparison is an unbound boolean value.");

		return boolValue == otherValue.boolean_value;
	}

	//static notEquals
	public static boolean opeatorNotEquals(final boolean boolValue, final TitanBoolean otherValue){
		otherValue.mustBound("The right operand of comparison is an unbound boolean value.");

		return new TitanBoolean(boolValue).operatorNotEquals(otherValue.boolean_value);
	}
}
