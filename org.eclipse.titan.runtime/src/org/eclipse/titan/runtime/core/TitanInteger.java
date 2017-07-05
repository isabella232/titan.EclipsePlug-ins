/******************************************************************************
 * Copyright (c) 2000-2017 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

import java.math.BigInteger;
import java.text.MessageFormat;

/**
 * TTCN-3 integer
 * @author Arpad Lovassy
 */
public class TitanInteger extends Base_Type {
	private boolean boundFlag;

	private boolean nativeFlag;
	private int nativeInt;
	private BigInteger openSSL;

	public TitanInteger() {
		boundFlag = false;
		nativeFlag = true;
	}

	public TitanInteger( final int otherValue ) {
		boundFlag = true;
		nativeFlag = true;
		nativeInt = otherValue;
	}

	public TitanInteger( final BigInteger otherValue ) {
		openSSL = otherValue;
		boundFlag = true;
		nativeFlag = false;
	}

	public TitanInteger( final TitanInteger otherValue ) {
		otherValue.mustBound( "Copying an unbound integer value." );

		boundFlag = true;
		nativeFlag = otherValue.nativeFlag;
		if(nativeFlag) {
			nativeInt = otherValue.nativeInt;
		} else {
			openSSL = otherValue.openSSL;
		}
	}

	//originally clean_up
	public void cleanUp() {
		if(!nativeFlag) {
			openSSL = null;
		}
		boundFlag = false;
	}

	//originally operator=
	public TitanInteger assign( final int otherValue ) {
		cleanUp();
		boundFlag = true;
		nativeFlag = true;
		nativeInt = otherValue;

		return this;
	}

	//originally operator=
	public TitanInteger assign( final TitanInteger otherValue ) {
		otherValue.mustBound( "Assignment of an unbound integer value." );

		if (otherValue != this) {
			cleanUp();
			boundFlag = true;
			nativeFlag = otherValue.nativeFlag;
			if(nativeFlag) {
				nativeInt = otherValue.nativeInt;
			} else {
				openSSL = otherValue.openSSL;
			}
		}

		return this;
	}

	@Override
	public TitanInteger assign(final Base_Type otherValue) {
		if (otherValue instanceof TitanInteger) {
			return assign((TitanInteger)otherValue);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to integer", otherValue));
	}

	//originally operator+ unary plus
	public TitanInteger add(){
		mustBound("Unbound integer operand of unary + operator.");
		if(nativeFlag){
			return new TitanInteger(nativeInt);
		} else {
			return new TitanInteger(openSSL);
		}
	}

	//originally operator-
	public TitanInteger sub(){
		mustBound("Unbound integer operand of unary - operator (negation).");

		if(nativeFlag){
			final long temp = nativeInt * -1;
			if(temp > Integer.MIN_VALUE && temp < Integer.MAX_VALUE){
				return new TitanInteger((int) temp);
			} else {
				return new TitanInteger(BigInteger.valueOf(temp));
			}
		} else {
			return new TitanInteger(openSSL.negate());
		}
	}
	//originally operator+
	public TitanInteger add(final int otherValue){
		return this.add(new TitanInteger(otherValue));
	}

	/**
	 * this + otherValue
	 * originally operator+
	 */
	public TitanInteger add( final TitanInteger otherValue ) {
		mustBound( "Unbound left operand of integer addition." );
		otherValue.mustBound( "Unbound right operand of integer addition." );

		if (nativeFlag) {
			if(otherValue.nativeFlag) {
				final long temp = nativeInt + otherValue.nativeInt;
				if (temp > Integer.MIN_VALUE && temp < Integer.MAX_VALUE) {
					return new TitanInteger((int)temp);
				}
				return new TitanInteger(BigInteger.valueOf(temp));
			} else {
				final BigInteger this_int = BigInteger.valueOf(nativeInt);
				return new TitanInteger(this_int.add(otherValue.openSSL));
			}
		} else {
			if(otherValue.nativeFlag) {
				final BigInteger other_int = BigInteger.valueOf(otherValue.nativeInt);
				return new TitanInteger(openSSL.add(other_int));
			} else {
				return new TitanInteger(openSSL.add(otherValue.openSSL));
			}
		}
	}

	//originally operator-
	public TitanInteger sub(final int otherValue){
		return this.sub(new TitanInteger(otherValue));
	}

	//originally operator-
	public TitanInteger sub(final TitanInteger otherValue){
		this.mustBound("Unbound left operand of integer addition. ");
		otherValue.mustBound("Unbound right operand of integer addition. ");

		if(nativeFlag){
			if(otherValue.nativeFlag){
				final long temp = this.nativeInt - otherValue.nativeInt;
				if(temp > Integer.MIN_VALUE  && temp < Integer.MAX_VALUE ){
					return new TitanInteger((int) temp);
				}
				return new TitanInteger(BigInteger.valueOf(temp));
			} else {
				final BigInteger this_int = BigInteger.valueOf(this.nativeInt);
				return new TitanInteger(this_int.subtract(otherValue.openSSL));
			}
		} else {
			if(otherValue.nativeFlag){
				final BigInteger other_int = BigInteger.valueOf(otherValue.nativeInt);
				return new TitanInteger(openSSL.subtract(other_int));
			} else {
				return new TitanInteger(openSSL.subtract(otherValue.openSSL));
			}
		}
	}

	//originally operator*
	public TitanInteger mul(final int otherValue) {
		return this.mul(new TitanInteger(otherValue));
	}

	//originally operator*
	public TitanInteger mul( final TitanInteger otherValue) {
		mustBound( "Unbound left operand of integer multiplication." );
		otherValue.mustBound( "Unbound right operand of integer multiplication." );
		
		if (nativeFlag && nativeInt==0 || (otherValue.nativeFlag && otherValue.nativeInt==0) ) {
			return new TitanInteger((int)0);
		}

		if (nativeFlag) {
			if(otherValue.nativeFlag) {
				final long temp = nativeInt * otherValue.nativeInt;
				if (temp > Integer.MIN_VALUE && temp < Integer.MAX_VALUE) {
					return new TitanInteger((int)temp);
				}
				return new TitanInteger(BigInteger.valueOf(temp));
			} else {
				final BigInteger this_int = BigInteger.valueOf(nativeInt);
				return new TitanInteger(this_int.multiply(otherValue.openSSL));
			}
		} else {
			if(otherValue.nativeFlag) {
				final BigInteger other_int = BigInteger.valueOf(otherValue.nativeInt);
				return new TitanInteger(openSSL.multiply(other_int));
			} else {
				return new TitanInteger(openSSL.multiply(otherValue.openSSL));
			}
		}
	}

	//originally operator/
	public TitanInteger div(final int otherValue){
		mustBound("Unbound left operand of integer division.");
		if(otherValue == 0){
			throw new TtcnError("Integer division by zero.");
		}
		
		return this.div(new TitanInteger(otherValue));
	}

	//originally operator/
	public TitanInteger div(final TitanInteger otherValue) {
		mustBound("Unbound left operand of integer division.");
		otherValue.mustBound("Unbound right operand of integer division.");
		if(otherValue.operatorEquals(0).getValue()){
			throw new TtcnError("Integer division by zero.");
		}
		if(nativeFlag && nativeInt == 0){
			return new TitanInteger(0);
		}
		
		if(nativeFlag){
			if(otherValue.nativeFlag){
				final long temp = nativeInt / otherValue.nativeInt;
				if(temp > Integer.MIN_VALUE && temp < Integer.MAX_VALUE){
					return new TitanInteger(nativeInt / otherValue.nativeInt);
				} else {
					return new TitanInteger(BigInteger.valueOf(temp));
				}
			} else {
				BigInteger this_int = BigInteger.valueOf(nativeInt);
				return new TitanInteger(this_int.divide(otherValue.openSSL));
			}
		} else {
			if(otherValue.nativeFlag){
				BigInteger other_value_int = BigInteger.valueOf(otherValue.nativeInt);
				return new TitanInteger(openSSL.divide(other_value_int));
			} else {
				return new TitanInteger(openSSL.divide(otherValue.openSSL));
			}
		}
	}

	//originally operator==
	public TitanBoolean operatorEquals( final int otherValue ) {
		mustBound("Unbound left operand of integer comparison.");

		if(nativeFlag) {
			return new TitanBoolean(nativeInt == otherValue);
		}

		final BigInteger other_int = BigInteger.valueOf(otherValue);
		return new TitanBoolean(openSSL.equals(other_int));
	}

	
	//TODO: mod
	/*
	public TitanInteger mod( final TitanInteger leftValue, final TitanInteger rightValue)
	{
		leftValue.mustBound("Unbound left operand of mod operator.");
		rightValue.mustBound( "Unbound right operand of mod operator" );
		
		
			
			new TitanInteger Math.abs(rightValue); 
			
			return new TitanInteger((int)0);
		
		
		if (nativeFlag) {
			if(rightValue.nativeFlag) {
				final long temp = nativeInt * otherValue.nativeInt;
				if (temp > Integer.MIN_VALUE && temp < Integer.MAX_VALUE) {
					return new TitanInteger((int)temp);
				}
				return new TitanInteger(BigInteger.valueOf(temp));
			} else {
				final BigInteger this_int = BigInteger.valueOf(nativeInt);
				return new TitanInteger(this_int.multiply(otherValue.openSSL));
			}
		} else {
			if(otherValue.nativeFlag) {
				final BigInteger other_int = BigInteger.valueOf(otherValue.nativeInt);
				return new TitanInteger(openSSL.multiply(other_int));
			} else {
				return new TitanInteger(openSSL.multiply(otherValue.openSSL));
			}
		}
	}*/

	//originally operator==
	public TitanBoolean operatorEquals( final TitanInteger otherValue ) {
		mustBound("Unbound left operand of integer comparison.");
		otherValue.mustBound("Unbound right operand of integer comparison.");

		if (nativeFlag) {
			if(otherValue.nativeFlag) {
				return new TitanBoolean(nativeInt == otherValue.nativeInt);
			} else {
				final BigInteger this_int = BigInteger.valueOf(nativeInt);
				return new TitanBoolean(this_int.equals(otherValue.openSSL));
			}
		} else {
			if(otherValue.nativeFlag) {
				final BigInteger other_int = BigInteger.valueOf(otherValue.nativeInt);
				return new TitanBoolean(openSSL.equals(other_int));
			} else {
				return new TitanBoolean(openSSL.equals(otherValue.openSSL));
			}
		}
	}

	@Override
	public TitanBoolean operatorEquals(final Base_Type otherValue) {
		if (otherValue instanceof TitanInteger) {
			return operatorEquals((TitanInteger)otherValue);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to integer", otherValue));
	}

	//TODO: check the correction
	//originally operator !=
	public TitanBoolean operatorNotEquals(final int otherValue){
		return operatorEquals(otherValue).not();
	}

	//originally operator!=
	public TitanBoolean operatorNotEquals( final TitanInteger otherValue ) {
		return operatorEquals( otherValue ).not();
	}

	//originally operator <
	public TitanBoolean isLessThan(final int otherValue) {
		mustBound("Unbound left operand of integer comparison.");

		if (nativeFlag) {
			return new TitanBoolean(nativeInt < otherValue);
		} else {
			final BigInteger other_int = BigInteger.valueOf(otherValue);
			return new TitanBoolean(-1 == openSSL.compareTo(other_int));
		}
	}

	//originally operator <
	public TitanBoolean isLessThan(final TitanInteger otherValue) {
		mustBound("Unbound left operand of integer comparison.");
		otherValue.mustBound("Unbound right operand of integer comparison.");

		if (nativeFlag) {
			if(otherValue.nativeFlag) {
				return new TitanBoolean(nativeInt < otherValue.nativeInt);
			} else {
				final BigInteger this_int = BigInteger.valueOf(nativeInt);
				return new TitanBoolean(-1 == this_int.compareTo(otherValue.openSSL));
			}
		} else {
			if(otherValue.nativeFlag) {
				final BigInteger other_int = BigInteger.valueOf(otherValue.nativeInt);
				return new TitanBoolean(-1 == openSSL.compareTo(other_int));
			} else {
				return new TitanBoolean(-1 == openSSL.compareTo(otherValue.openSSL));
			}
		}
	}

	//originally operator >
	public TitanBoolean isGreaterThan(final int otherValue) {
		mustBound("Unbound left operand of integer comparison.");

		if (nativeFlag) {
			return new TitanBoolean(nativeInt > otherValue);
		} else {
			final BigInteger other_int = BigInteger.valueOf(otherValue);
			return new TitanBoolean(1 == openSSL.compareTo(other_int));

		}
	}

	//originally operator >
	public TitanBoolean isGreaterThan(final TitanInteger otherValue) {
		mustBound("Unbound left operand of integer comparison.");
		otherValue.mustBound("Unbound right operand of integer comparison.");

		if (nativeFlag) {
			if(otherValue.nativeFlag) {
				return new TitanBoolean(nativeInt > otherValue.nativeInt);
			} else {
				final BigInteger this_int = BigInteger.valueOf(nativeInt);
				return new TitanBoolean(1 == this_int.compareTo(otherValue.openSSL));
			}
		} else {
			if(otherValue.nativeFlag) {
				final BigInteger other_int = BigInteger.valueOf(otherValue.nativeInt);
				return new TitanBoolean(1 == openSSL.compareTo(other_int));
			} else {
				return new TitanBoolean(1 == openSSL.compareTo(otherValue.openSSL));
			}
		}
	}

	//originally operator <=
	public TitanBoolean isLessThanOrEqual(final int otherValue) {
		return isGreaterThan(otherValue).not();
	}

	//originally operator <=
	public TitanBoolean isLessThanOrEqual(final TitanInteger otherValue) {
		return isGreaterThan(otherValue).not();
	}

	//originally operator >=
	public TitanBoolean isGreaterThanOrEqual(final int otherValue) {
		return isGreaterThan(otherValue).not();
	}

	//originally operator >=
	public TitanBoolean isGreaterThanOrEqual(final TitanInteger otherValue) {
		return isGreaterThan(otherValue).not();
	}

	public boolean isBound() {
		return boundFlag;
	}

	public boolean isPresent() {
		return isBound();
	};

	public boolean isValue() {
		return boundFlag;
	}

	public void mustBound( final String errorMessage ) {
		if ( !boundFlag ) {
			throw new TtcnError( errorMessage );
		}
	}

	@Override
	public String toString() {
		if ( !boundFlag ) {
			return "<unbound>";
		}
		return getBigInteger().toString();
	}

	// originally int()
	public int getInt() {
		mustBound( "Using the value of an unbound integer variable." );

		if(!nativeFlag) {
			throw new TtcnError( "Invalid conversion of a large integer value." );
		}

		return nativeInt;
	}

	// originally get_long_long_val
	public BigInteger getBigInteger() {
		mustBound( "Using the value of an unbound integer variable." );

		if(nativeFlag) {
			return BigInteger.valueOf(nativeInt);
		}

		return openSSL;
	}


	//static operator+
	public static TitanInteger add(final int intValue, final TitanInteger otherValue){
		otherValue.mustBound("Unbound right operand of integer addition.");

		if(otherValue.nativeFlag){
			final long temp = intValue + otherValue.nativeInt;
			if(temp > Integer.MIN_VALUE && temp < Integer.MAX_VALUE){
				return new TitanInteger((int) temp);
			} else {
				return new TitanInteger(BigInteger.valueOf(temp));
			}
		} else {
			final BigInteger first_int = BigInteger.valueOf(intValue);
			return new TitanInteger(first_int.add(otherValue.openSSL));
		}
	}

	//static operator-
	public static TitanInteger sub(final int intValue, final TitanInteger otherValue){
		otherValue.mustBound("Unbound right operand of integer subtraction.");

		if(otherValue.nativeFlag){
			final long temp = intValue - otherValue.nativeInt;
			if(temp > Integer.MIN_VALUE && temp < Integer.MAX_VALUE){
				return new TitanInteger((int) temp);
			} else {
				return new TitanInteger(BigInteger.valueOf(temp));
			}
		} else {
			final BigInteger first_int = BigInteger.valueOf(intValue);
			return new TitanInteger(first_int.subtract(otherValue.openSSL));
		}
	}

	//static mul

	//static operator/
	public static TitanInteger div(final int intValue, final TitanInteger otherValue){
		otherValue.mustBound("Unbound right operand of integer division.");

		if(otherValue.operatorEquals(0).getValue()){
			throw new TtcnError("Integer division by zero.");
		}

		return new TitanInteger(intValue).div(otherValue);
	}

	//static operator==
	public static TitanBoolean operatorEquals(final int intValue, final TitanInteger otherValue){
		otherValue.mustBound("Unbound right operand of integer comparison.");

		return new TitanInteger(intValue).operatorEquals(otherValue);
	}
	
	//static operator!=
	public static TitanBoolean operatorNotEquals(final int intValue, final TitanInteger otherValue){
		otherValue.mustBound("Unbound right operand of integer comparison.");

		return new TitanInteger(intValue).operatorNotEquals(otherValue);
	}

	//TODO: implement static rem
	//TODO: implement static mod
	//TODO: implement static isLessThan
	//TODO: implement static isGreaterThan
	//TODO: implement static isLEssThanOrEqual
	//TODO: implement static isGreaterThanOrEqual
}
