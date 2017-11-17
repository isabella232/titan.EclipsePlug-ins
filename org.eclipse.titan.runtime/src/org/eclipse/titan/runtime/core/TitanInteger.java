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

	private boolean fromString(final String otherValue) {
		try {
			final BigInteger temp = new BigInteger(otherValue);
			if (temp.abs().compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) == -1) {
				nativeFlag = true;
				nativeInt = temp.intValue();
			} else {
				nativeFlag = false;
				openSSL = temp;
			}
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
		
	}

	public TitanInteger(final String otherValue) {
		if (otherValue == null) {
			throw new TtcnError(MessageFormat.format("Unexpected error when converting `{0}'' to integer", otherValue));
		}
		boundFlag = true;
		if (!fromString(otherValue)) {
			throw new TtcnError(MessageFormat.format("Unexpected error when converting `{0}'' to integer", otherValue));
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
	public TitanInteger assign( final BigInteger otherValue ) {
		cleanUp();
		boundFlag = true;
		nativeFlag = false;
		openSSL = otherValue;

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

	// originally operator+
	public TitanInteger add(final BigInteger otherValue) {
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

	//originally operator-
	public TitanInteger sub(final BigInteger otherValue){
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

	//originally operator*
	public TitanInteger mul(final BigInteger otherValue) {
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

	//originally operator/
	public TitanInteger div(final BigInteger otherValue){
		return div(new TitanInteger(otherValue));
	}

	// originally operator/
	public TitanInteger div(final TitanInteger otherValue) {
		mustBound("Unbound left operand of integer division.");
		otherValue.mustBound("Unbound right operand of integer division.");

		if (otherValue.operatorEquals(0)) {
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
				final BigInteger this_int = BigInteger.valueOf(nativeInt);
				return new TitanInteger(this_int.divide(otherValue.openSSL));
			}
		} else {
			if (otherValue.nativeFlag) {
				final BigInteger other_value_int = BigInteger.valueOf(otherValue.nativeInt);
				return new TitanInteger(openSSL.divide(other_value_int));
			} else {
				return new TitanInteger(openSSL.divide(otherValue.openSSL));
			}
		}
	}

	// rem with one parameter
	public TitanInteger rem(final int rightValue) {
		if (rightValue == 0) {
			throw new TtcnError("The right operand of rem operator is zero.");
		}

		return rem(this, rightValue);
	}

	// rem with one parameter
	public TitanInteger rem(final BigInteger rightValue) {
		if (rightValue.equals(BigInteger.ZERO)) {
			throw new TtcnError("The right operand of rem operator is zero.");
		}

		return rem(this, new TitanInteger(rightValue));
	}

	public TitanInteger rem(final TitanInteger rightValue) {
		this.mustBound("Unbound left operand of rem operator ");
		rightValue.mustBound("Unbound right operand of rem operator");

		return this.sub(rightValue.mul((this.div(rightValue))));
	}

	// originally operator==
	public boolean operatorEquals(final int otherValue) {
		mustBound("Unbound left operand of integer comparison.");

		if (nativeFlag) {
			return nativeInt == otherValue;
		}

		final BigInteger other_int = BigInteger.valueOf(otherValue);
		return openSSL.equals(other_int);
	}

	// originally operator==
	public boolean operatorEquals(final BigInteger otherValue) {
		mustBound("Unbound left operand of integer comparison.");

		if (!nativeFlag) {
			return openSSL.equals(otherValue);
		}

		final BigInteger local_int = BigInteger.valueOf(nativeInt);
		return local_int.equals(otherValue);
	}

	// originally operator==
	public boolean operatorEquals(final TitanInteger otherValue) {
		mustBound("Unbound left operand of integer comparison.");
		otherValue.mustBound("Unbound right operand of integer comparison.");

		if (nativeFlag) {
			if (otherValue.nativeFlag) {
				return nativeInt == otherValue.nativeInt;
			} else {
				final BigInteger this_int = BigInteger.valueOf(nativeInt);
				return this_int.equals(otherValue.openSSL);
			}
		} else {
			if (otherValue.nativeFlag) {
				final BigInteger other_int = BigInteger.valueOf(otherValue.nativeInt);
				return openSSL.equals(other_int);
			} else {
				return openSSL.equals(otherValue.openSSL);
			}
		}
	}

	@Override
	public boolean operatorEquals(final Base_Type otherValue) {
		if (otherValue instanceof TitanInteger) {
			return operatorEquals((TitanInteger) otherValue);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to integer", otherValue));
	}

	// originally operator !=
	public boolean operatorNotEquals(final int otherValue) {
		return !operatorEquals(otherValue);
	}

	// originally operator !=
	public boolean operatorNotEquals(final BigInteger otherValue) {
		return !operatorEquals(otherValue);
	}

	// originally operator!=
	public boolean operatorNotEquals(final TitanInteger otherValue) {
		return !operatorEquals(otherValue);
	}

	// originally operator <
	public boolean isLessThan(final int otherValue) {
		mustBound("Unbound left operand of integer comparison.");

		if (nativeFlag) {
			return nativeInt < otherValue;
		} else {
			final BigInteger other_int = BigInteger.valueOf(otherValue);
			return -1 == openSSL.compareTo(other_int);
		}
	}

	// originally operator <
	public boolean isLessThan(final BigInteger otherValue) {
		mustBound("Unbound left operand of integer comparison.");

		if (nativeFlag) {
			final BigInteger this_int = BigInteger.valueOf(nativeInt);
			return -1 == this_int.compareTo(otherValue);
		} else {
			return -1 == openSSL.compareTo(otherValue);
		}
	}

	// originally operator <
	public boolean isLessThan(final TitanInteger otherValue) {
		mustBound("Unbound left operand of integer comparison.");
		otherValue.mustBound("Unbound right operand of integer comparison.");

		if (nativeFlag) {
			if (otherValue.nativeFlag) {
				return nativeInt < otherValue.nativeInt;
			} else {
				final BigInteger this_int = BigInteger.valueOf(nativeInt);
				return -1 == this_int.compareTo(otherValue.openSSL);
			}
		} else {
			if (otherValue.nativeFlag) {
				final BigInteger other_int = BigInteger.valueOf(otherValue.nativeInt);
				return -1 == openSSL.compareTo(other_int);
			} else {
				return -1 == openSSL.compareTo(otherValue.openSSL);
			}
		}
	}

	// originally operator >
	public boolean isGreaterThan(final int otherValue) {
		mustBound("Unbound left operand of integer comparison.");

		if (nativeFlag) {
			return nativeInt > otherValue;
		} else {
			final BigInteger other_int = BigInteger.valueOf(otherValue);
			return 1 == openSSL.compareTo(other_int);

		}
	}

	// originally operator >
	public boolean isGreaterThan(final BigInteger otherValue) {
		mustBound("Unbound left operand of integer comparison.");

		if (nativeFlag) {
			final BigInteger this_int = BigInteger.valueOf(nativeInt);
			return 1 == this_int.compareTo(otherValue);
		} else {
			return 1 == openSSL.compareTo(otherValue);

		}
	}

	// originally operator >
	public boolean isGreaterThan(final TitanInteger otherValue) {
		mustBound("Unbound left operand of integer comparison.");
		otherValue.mustBound("Unbound right operand of integer comparison.");

		if (nativeFlag) {
			if (otherValue.nativeFlag) {
				return nativeInt > otherValue.nativeInt;
			} else {
				final BigInteger this_int = BigInteger.valueOf(nativeInt);
				return 1 == this_int.compareTo(otherValue.openSSL);
			}
		} else {
			if (otherValue.nativeFlag) {
				final BigInteger other_int = BigInteger.valueOf(otherValue.nativeInt);
				return 1 == openSSL.compareTo(other_int);
			} else {
				return 1 == openSSL.compareTo(otherValue.openSSL);
			}
		}
	}

	// originally operator <=
	public boolean isLessThanOrEqual(final int otherValue) {
		return !isGreaterThan(otherValue);
	}

	// originally operator <=
	public boolean isLessThanOrEqual(final BigInteger otherValue) {
		return !isGreaterThan(otherValue);
	}

	// originally operator <=
	public boolean isLessThanOrEqual(final TitanInteger otherValue) {
		return !isGreaterThan(otherValue);
	}

	// originally operator >=
	public boolean isGreaterThanOrEqual(final int otherValue) {
		return !isLessThan(otherValue);
	}

	// originally operator >=
	public boolean isGreaterThanOrEqual(final BigInteger otherValue) {
		return !isLessThan(otherValue);
	}

	// originally operator >=
	public boolean isGreaterThanOrEqual(final TitanInteger otherValue) {
		return !isLessThan(otherValue);
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

	public void log() {
		if (boundFlag) {
			if (nativeFlag) {
				TtcnLogger.log_event("%d", nativeInt);
			} else {
				TtcnLogger.log_event("%s", openSSL.toString());
			}
		} else {
			TtcnLogger.log_event_unbound();
		}
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

	@Override
	/** {@inheritDoc} */
	public void encode_text(final Text_Buf text_buf) {
		mustBound("Text encoder: Encoding an unbound integer value.");

		text_buf.push_int(this);
	}

	@Override
	/** {@inheritDoc} */
	public void decode_text(final Text_Buf text_buf) {
		assign(text_buf.pull_int());
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
			final int result = rem(leftValue, rightValue).nativeInt;
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
		if (rightValue.isLessThan(0)) {
			rightValueAbs = rightValueAbs.mul(-1);
		} else if (rightValue.operatorEquals(0)) {
			throw new TtcnError("The right operand of mod operator is zero");
		}
		if (leftValue.isGreaterThan(0)) {
			return rem(leftValue, rightValue);
		} else {
			final TitanInteger result = rem(leftValue, rightValueAbs);
			if (result.operatorEquals(0)) {
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

	// mod with one parameter - int
	public TitanInteger mod(final int rightValue) {
		return mod(new TitanInteger(rightValue));
	}

	// mod with one parameter - int
	public TitanInteger mod(final BigInteger rightValue) {
		return mod(new TitanInteger(rightValue));
	}

	// mod with one parameter
	public TitanInteger mod(final TitanInteger rightValue) {
		mustBound("Unbound left operand of mod operator.");
		rightValue.mustBound("Unbound right operand of mod operator");

		TitanInteger rightValueAbs = new TitanInteger(rightValue);
		if (rightValue.isLessThan(0)) {
			rightValueAbs = rightValueAbs.mul(-1);
		} else if (rightValue.operatorEquals(0)) {
			throw new TtcnError("The right operand of mod operator is zero");
		}
		if (isGreaterThan(0)) {
			return rem(this, rightValue);
		} else {
			final TitanInteger result = rem(this, rightValueAbs);
			if (result.operatorEquals(0)) {
				return new TitanInteger(0);
			} else {
				return new TitanInteger(rightValueAbs.add(result));
			}
		}
	}

	// static operator==
	public static boolean operatorEquals(final int intValue, final TitanInteger otherValue) {
		return new TitanInteger(intValue).operatorEquals(otherValue);
	}

	// static operator!=
	public static boolean operatorNotEquals(final int intValue, final TitanInteger otherValue) {
		return new TitanInteger(intValue).operatorNotEquals(otherValue);
	}

	// static operator <
	public static boolean isLessThan(final int intValue, final TitanInteger otherValue) {
		return new TitanInteger(intValue).isLessThan(otherValue);
	}

	// static operator >
	public static boolean isGreaterThan(final int intValue, final TitanInteger otherValue) {
		return new TitanInteger(intValue).isGreaterThan(otherValue);
	}

	// static operator <=
	public static boolean isLessThanOrEqual(final int intValue, final TitanInteger otherValue) {
		return new TitanInteger(intValue).isLessThanOrEqual(otherValue);
	}

	// static operator >=
	public static boolean isGreaterThanOrEqual(final int intValue, final TitanInteger otherValue) {
		return new TitanInteger(intValue).isGreaterThanOrEqual(otherValue);
	}
}
