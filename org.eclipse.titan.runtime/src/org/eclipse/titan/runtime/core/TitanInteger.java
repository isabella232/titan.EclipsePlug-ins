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
 * @author Gergo Ujhelyi
 * @author Andrea Palfi
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

	// originally operator-
	public TitanInteger sub() {
		mustBound("Unbound integer operand of unary - operator (negation).");

		if (nativeFlag) {
			final long temp = (long) nativeInt * -1;
			if (temp > Integer.MIN_VALUE && temp < Integer.MAX_VALUE) {
				return new TitanInteger((int) temp);
			} else {
				return new TitanInteger(BigInteger.valueOf(temp));
			}
		} else {
			return new TitanInteger(openSSL.negate());
		}
	}

	// originally operator+
	public TitanInteger add(final int otherValue) {
		return this.add(new TitanInteger(otherValue));
	}

	/**
	 * this + otherValue
	 * originally operator+
	 */
	public TitanInteger add(final TitanInteger otherValue) {
		mustBound("Unbound left operand of integer addition.");
		otherValue.mustBound("Unbound right operand of integer addition.");

		if (nativeFlag) {
			if (otherValue.nativeFlag) {
				final long temp = (long) nativeInt + (long) otherValue.nativeInt;
				if (temp > Integer.MIN_VALUE && temp < Integer.MAX_VALUE) {
					return new TitanInteger((int) temp);
				}
				return new TitanInteger(BigInteger.valueOf(temp));
			} else {
				final BigInteger this_int = BigInteger.valueOf(nativeInt);
				return new TitanInteger(this_int.add(otherValue.openSSL));
			}
		} else {
			if (otherValue.nativeFlag) {
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

	// originally operator-
	public TitanInteger sub(final TitanInteger otherValue) {
		this.mustBound("Unbound left operand of integer addition. ");
		otherValue.mustBound("Unbound right operand of integer addition. ");

		if (nativeFlag) {
			if (otherValue.nativeFlag) {
				final long temp = (long) nativeInt - (long) otherValue.nativeInt;
				if (temp > Integer.MIN_VALUE && temp < Integer.MAX_VALUE) {
					return new TitanInteger((int) temp);
				}
				return new TitanInteger(BigInteger.valueOf(temp));
			} else {
				final BigInteger this_int = BigInteger.valueOf(nativeInt);
				return new TitanInteger(this_int.subtract(otherValue.openSSL));
			}
		} else {
			if (otherValue.nativeFlag) {
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

	// originally operator*
	public TitanInteger mul(final TitanInteger otherValue) {
		mustBound("Unbound left operand of integer multiplication.");

		otherValue.mustBound("Unbound right operand of integer multiplication.");

		if (nativeFlag && nativeInt == 0 || (otherValue.nativeFlag && otherValue.nativeInt == 0)) {
			return new TitanInteger((int) 0);
		}

		if (nativeFlag) {
			if (otherValue.nativeFlag) {
				final long temp = (long) nativeInt * (long) otherValue.nativeInt;
				if (temp > Integer.MIN_VALUE && temp < Integer.MAX_VALUE) {
					return new TitanInteger((int) temp);
				}
				return new TitanInteger(BigInteger.valueOf(temp));
			} else {
				final BigInteger this_int = BigInteger.valueOf(nativeInt);
				return new TitanInteger(this_int.multiply(otherValue.openSSL));
			}
		} else {
			if (otherValue.nativeFlag) {
				final BigInteger other_int = BigInteger.valueOf(otherValue.nativeInt);
				return new TitanInteger(openSSL.multiply(other_int));
			} else {
				return new TitanInteger(openSSL.multiply(otherValue.openSSL));
			}
		}
	}

	//originally operator/
	public TitanInteger div(final int otherValue){
		return div(new TitanInteger(otherValue));
	}

	// originally operator/
	public TitanInteger div(final TitanInteger otherValue) {
		mustBound("Unbound left operand of integer division.");
		otherValue.mustBound("Unbound right operand of integer division.");

		if (otherValue.operatorEquals(0).getValue()) {
			throw new TtcnError("Integer division by zero.");
		}
		if (nativeFlag && nativeInt == 0) {
			return new TitanInteger(0);
		}

		if (nativeFlag) {
			if (otherValue.nativeFlag) {
				final long temp = (long) nativeInt / (long) otherValue.nativeInt;
				if (temp > Integer.MIN_VALUE && temp < Integer.MAX_VALUE) {
					return new TitanInteger(nativeInt / otherValue.nativeInt);
				} else {
					return new TitanInteger(BigInteger.valueOf(temp));
				}
			} else {
				BigInteger this_int = BigInteger.valueOf(nativeInt);
				return new TitanInteger(this_int.divide(otherValue.openSSL));
			}
		} else {
			if (otherValue.nativeFlag) {
				BigInteger other_value_int = BigInteger.valueOf(otherValue.nativeInt);
				return new TitanInteger(openSSL.divide(other_value_int));
			} else {
				return new TitanInteger(openSSL.divide(otherValue.openSSL));
			}
		}
	}

	// originally operator==
	public TitanBoolean operatorEquals(final int otherValue) {
		mustBound("Unbound left operand of integer comparison.");

		if (nativeFlag) {
			return new TitanBoolean(nativeInt == otherValue);
		}

		final BigInteger other_int = BigInteger.valueOf(otherValue);
		return new TitanBoolean(openSSL.equals(other_int));
	}

	// rem with one parameter
	public TitanInteger rem(final int rightValue) {
		if (rightValue == 0) {
			throw new TtcnError("The right operand of rem operator is zero.");
		}

		return rem(this, rightValue);
	}

	public TitanInteger rem(final TitanInteger rightValue) {
		this.mustBound("Unbound left operand of rem operator ");
		rightValue.mustBound("Unbound right operand of rem operator");

		return this.sub(rightValue.mul((this.div(rightValue))));
	}


	// originally operator==
	public TitanBoolean operatorEquals(final TitanInteger otherValue) {
		mustBound("Unbound left operand of integer comparison.");
		otherValue.mustBound("Unbound right operand of integer comparison.");

		if (nativeFlag) {
			if (otherValue.nativeFlag) {
				return new TitanBoolean(nativeInt == otherValue.nativeInt);
			} else {
				final BigInteger this_int = BigInteger.valueOf(nativeInt);
				return new TitanBoolean(this_int.equals(otherValue.openSSL));
			}
		} else {
			if (otherValue.nativeFlag) {
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
			return operatorEquals((TitanInteger) otherValue);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to integer", otherValue));
	}

	// TODO: check the correction
	// originally operator !=
	public TitanBoolean operatorNotEquals(final int otherValue) {
		return operatorEquals(otherValue).not();
	}

	// originally operator!=
	public TitanBoolean operatorNotEquals(final TitanInteger otherValue) {
		return operatorEquals(otherValue).not();
	}

	// originally operator <
	public TitanBoolean isLessThan(final int otherValue) {
		mustBound("Unbound left operand of integer comparison.");

		if (nativeFlag) {
			return new TitanBoolean(nativeInt < otherValue);
		} else {
			final BigInteger other_int = BigInteger.valueOf(otherValue);
			return new TitanBoolean(-1 == openSSL.compareTo(other_int));
		}
	}

	// originally operator <
	public TitanBoolean isLessThan(final TitanInteger otherValue) {
		mustBound("Unbound left operand of integer comparison.");
		otherValue.mustBound("Unbound right operand of integer comparison.");

		if (nativeFlag) {
			if (otherValue.nativeFlag) {
				return new TitanBoolean(nativeInt < otherValue.nativeInt);
			} else {
				final BigInteger this_int = BigInteger.valueOf(nativeInt);
				return new TitanBoolean(-1 == this_int.compareTo(otherValue.openSSL));
			}
		} else {
			if (otherValue.nativeFlag) {
				final BigInteger other_int = BigInteger.valueOf(otherValue.nativeInt);
				return new TitanBoolean(-1 == openSSL.compareTo(other_int));
			} else {
				return new TitanBoolean(-1 == openSSL.compareTo(otherValue.openSSL));
			}
		}
	}

	// originally operator >
	public TitanBoolean isGreaterThan(final int otherValue) {
		mustBound("Unbound left operand of integer comparison.");

		if (nativeFlag) {
			return new TitanBoolean(nativeInt > otherValue);
		} else {
			final BigInteger other_int = BigInteger.valueOf(otherValue);
			return new TitanBoolean(1 == openSSL.compareTo(other_int));

		}
	}

	// originally operator >
	public TitanBoolean isGreaterThan(final TitanInteger otherValue) {
		mustBound("Unbound left operand of integer comparison.");
		otherValue.mustBound("Unbound right operand of integer comparison.");

		if (nativeFlag) {
			if (otherValue.nativeFlag) {
				return new TitanBoolean(nativeInt > otherValue.nativeInt);
			} else {
				final BigInteger this_int = BigInteger.valueOf(nativeInt);
				return new TitanBoolean(1 == this_int.compareTo(otherValue.openSSL));
			}
		} else {
			if (otherValue.nativeFlag) {
				final BigInteger other_int = BigInteger.valueOf(otherValue.nativeInt);
				return new TitanBoolean(1 == openSSL.compareTo(other_int));
			} else {
				return new TitanBoolean(1 == openSSL.compareTo(otherValue.openSSL));
			}
		}
	}

	// originally operator <=
	public TitanBoolean isLessThanOrEqual(final int otherValue) {
		return isGreaterThan(otherValue).not();
	}

	// originally operator <=
	public TitanBoolean isLessThanOrEqual(final TitanInteger otherValue) {
		return isGreaterThan(otherValue).not();
	}

	// originally operator >=
	public TitanBoolean isGreaterThanOrEqual(final int otherValue) {
		return isLessThan(otherValue).not();
	}

	// originally operator >=
	public TitanBoolean isGreaterThanOrEqual(final TitanInteger otherValue) {
		return isLessThan(otherValue).not();
	}

	public boolean isNative() {
		return nativeFlag;
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
		mustBound("Using the value of an unbound integer variable.");

		if (!nativeFlag) {
			throw new TtcnError("Invalid conversion of a large integer value.");
		}

		return nativeInt;
	}

	// originally get_long_long_val
	public BigInteger getBigInteger() {
		mustBound("Using the value of an unbound integer variable.");

		if (nativeFlag) {
			return BigInteger.valueOf(nativeInt);
		}

		return openSSL;
	}

	// static operator+
	public static TitanInteger add(final int intValue, final TitanInteger otherValue) {
		otherValue.mustBound("Unbound right operand of integer addition.");

		if (otherValue.nativeFlag) {
			final long temp = (long) intValue + (long) otherValue.nativeInt;
			if (temp > Integer.MIN_VALUE && temp < Integer.MAX_VALUE) {
				return new TitanInteger((int) temp);
			} else {
				return new TitanInteger(BigInteger.valueOf(temp));
			}
		} else {
			final BigInteger first_int = BigInteger.valueOf(intValue);
			return new TitanInteger(first_int.add(otherValue.openSSL));
		}
	}

	// static operator-
	public static TitanInteger sub(final int intValue, final TitanInteger otherValue) {
		otherValue.mustBound("Unbound right operand of integer subtraction.");

		if (otherValue.nativeFlag) {
			final long temp = (long) intValue - (long) otherValue.nativeInt;
			if (temp > Integer.MIN_VALUE && temp < Integer.MAX_VALUE) {
				return new TitanInteger((int) temp);
			} else {
				return new TitanInteger(BigInteger.valueOf(temp));
			}
		} else {
			final BigInteger first_int = BigInteger.valueOf(intValue);
			return new TitanInteger(first_int.subtract(otherValue.openSSL));
		}
	}

	// static mul
	public static TitanInteger mul(final int intValue, final TitanInteger otherValue) {
		return new TitanInteger(intValue).mul(otherValue);
	}

	// static operator/
	public static TitanInteger div(final int intValue, final TitanInteger otherValue) {
		return new TitanInteger(intValue).div(otherValue);
	}

	// static rem
	public static TitanInteger rem(final int leftValue, final int rightValue) {
		if (rightValue == 0) {
			throw new TtcnError("The right operand of rem operator is zero.");
		}

		return new TitanInteger(leftValue - rightValue * (leftValue / rightValue));
	}

	public static TitanInteger rem(final TitanInteger leftValue, final TitanInteger rightValue) {
		leftValue.mustBound("Unbound left operand of rem operator ");
		rightValue.mustBound("Unbound right operand of rem operator");

		return leftValue.sub(rightValue.mul((leftValue.div(rightValue))));
	}

	public static TitanInteger rem(final TitanInteger leftValue, final int rightValue) {
		return rem(leftValue, new TitanInteger(rightValue));
	}

	public static TitanInteger rem(final int leftValue, final TitanInteger rightValue) {
		return rem(new TitanInteger(leftValue), rightValue);
	}

	// mod
	public static TitanInteger mod(final int leftValue, int rightValue) {
		if (rightValue < 0) {
			rightValue = rightValue * (-1);
		} else if (rightValue == 0) {
			throw new TtcnError("The right operand of mod operator is zero.");
		}
		if (leftValue > 0) {
			return rem(leftValue, rightValue);
		} else {
			int result = rem(leftValue, rightValue).nativeInt;
			if (result == 0) {
				return new TitanInteger(0);
			} else {
				return new TitanInteger(rightValue + result);
			}
		}
	}

	public static TitanInteger mod(final TitanInteger leftValue, final TitanInteger rightValue) {
		leftValue.mustBound("Unbound left operand of mod operator.");
		rightValue.mustBound("Unbound right operand of mod operator");

		TitanInteger rightValueAbs = new TitanInteger(rightValue);
		if (rightValue.isLessThan(0).getValue()) {
			rightValueAbs = rightValueAbs.mul(-1);
		} else if (rightValue.operatorEquals(0).getValue()) {
			throw new TtcnError("The right operand of mod operator is zero");
		}
		if (leftValue.isGreaterThan(0).getValue()) {
			return rem(leftValue, rightValue);
		} else {
			TitanInteger result = rem(leftValue, rightValueAbs);
			if (result.operatorEquals(0).getValue()) {
				return new TitanInteger(0);
			} else {
				return new TitanInteger(rightValueAbs.add(result));
			}
		}
	}

	// static mod
	public static TitanInteger mod(final TitanInteger leftValue, final int rightValue) {
		return mod(leftValue, new TitanInteger(rightValue));
	}

	public static TitanInteger mod(final int leftValue, final TitanInteger rightValue) {
		return mod(new TitanInteger(leftValue), rightValue);
	}

	// mod with one parameter
	public TitanInteger mod(final TitanInteger rightValue) {
		mustBound("Unbound left operand of mod operator.");
		rightValue.mustBound("Unbound right operand of mod operator");

		TitanInteger rightValueAbs = new TitanInteger(rightValue);
		if (rightValue.isLessThan(0).getValue()) {
			rightValueAbs = rightValueAbs.mul(-1);
		} else if (rightValue.operatorEquals(0).getValue()) {
			throw new TtcnError("The right operand of mod operator is zero");
		}
		if (isGreaterThan(0).getValue()) {
			return rem(this, rightValue);
		} else {
			TitanInteger result = rem(this, rightValueAbs);
			if (result.operatorEquals(0).getValue()) {
				return new TitanInteger(0);
			} else {
				return new TitanInteger(rightValueAbs.add(result));
			}
		}
	}

	// mod with one parameter - int
	public TitanInteger mod(final int rightValue) {
		return mod(new TitanInteger(rightValue));
	}

	// static operator==
	public static TitanBoolean operatorEquals(final int intValue, final TitanInteger otherValue) {
		return new TitanInteger(intValue).operatorEquals(otherValue);
	}

	// static operator!=
	public static TitanBoolean operatorNotEquals(final int intValue, final TitanInteger otherValue) {
		return new TitanInteger(intValue).operatorNotEquals(otherValue);
	}

	// static operator <
	public static TitanBoolean isLessThan(final int intValue, final TitanInteger otherValue) {
		return new TitanInteger(intValue).isLessThan(otherValue);
	}

	// static operator >
	public static TitanBoolean isGreaterThan(final int intValue, final TitanInteger otherValue) {
		return new TitanInteger(intValue).isGreaterThan(otherValue);
	}

	// static operator <=
	public static TitanBoolean isLessThanOrEqual(final int intValue, final TitanInteger otherValue) {
		return new TitanInteger(intValue).isLessThanOrEqual(otherValue);
	}

	// static operator >=
	public static TitanBoolean isGreaterThanOrEqual(final int intValue, final TitanInteger otherValue) {
		return new TitanInteger(intValue).isGreaterThanOrEqual(otherValue);
	}
}
