/******************************************************************************
 * Copyright (c) 2000-2017 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

import java.math.BigInteger;

import org.eclipse.titan.runtime.core.TtcnError;

/**
 * TTCN-3 integer
 * @author Arpad Lovassy
 */
public class TitanInteger {
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
		cleanUp();
		boundFlag = true;
		nativeFlag = otherValue.nativeFlag;
		if(nativeFlag) {
			nativeInt = otherValue.nativeInt;
		} else {
			openSSL = otherValue.openSSL;
		}

		return this;
	}

	public boolean isBound() {
		return boundFlag;
	}

	public boolean isValue() {
		return boundFlag;
	}
	
	public void mustBound( final String errorMessage ) {
		if ( !boundFlag ) {
			throw new TtcnError( errorMessage );
		}
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
				long temp = nativeInt + otherValue.nativeInt;
				if (temp > Integer.MIN_VALUE && temp < Integer.MAX_VALUE) {
					return new TitanInteger((int)temp);
				}
				return new TitanInteger(BigInteger.valueOf(temp));
			} else {
				BigInteger this_int = BigInteger.valueOf(nativeInt);
				return new TitanInteger(this_int.add(otherValue.openSSL));
			}
		} else {
			if(otherValue.nativeFlag) {
				BigInteger other_int = BigInteger.valueOf(otherValue.nativeInt);
				return new TitanInteger(openSSL.add(other_int));
			} else {
				return new TitanInteger(openSSL.add(otherValue.openSSL));
			}
		}
	}

	//originally operator==
	public boolean operatorEquals( final int otherValue ) {
		mustBound("Unbound left operand of integer comparison.");
		if(nativeFlag) {
			return nativeInt == otherValue;
		}

		BigInteger other_int = BigInteger.valueOf(otherValue);
		return openSSL.equals(other_int);
	}
	
	//originally operator==
	public boolean operatorEquals( final TitanInteger otherValue ) {
		mustBound("Unbound left operand of integer comparison.");
		otherValue.mustBound("Unbound right operand of integer comparison.");

		if (nativeFlag) {
			if(otherValue.nativeFlag) {
				return nativeInt == otherValue.nativeInt;
			} else {
				BigInteger this_int = BigInteger.valueOf(nativeInt);
				return this_int.equals(otherValue.openSSL);
			}
		} else {
			if(otherValue.nativeFlag) {
				BigInteger other_int = BigInteger.valueOf(otherValue.nativeInt);
				return openSSL.equals(other_int);
			} else {
				return openSSL.equals(otherValue.openSSL);
			}
		}
	}

	//originally operator!=
	public boolean operatorNotEquals( final TitanInteger otherValue ) {
		return !operatorEquals( otherValue );
	}
	
	//originally operator <
	public boolean isLessThan(final int otherValue) {
		mustBound("Unbound left operand of integer comparison.");
		if (nativeFlag) {
			return nativeInt < otherValue;
		} else {
			BigInteger other_int = BigInteger.valueOf(otherValue);
			return -1 == openSSL.compareTo(other_int);
		}
	}
	
	//originally operator <
	public boolean isLessThan(final TitanInteger otherValue) {
		mustBound("Unbound left operand of integer comparison.");
		otherValue.mustBound("Unbound right operand of integer comparison.");
		if (nativeFlag) {
			if(otherValue.nativeFlag) {
				return nativeInt < otherValue.nativeInt;
			} else {
				BigInteger this_int = BigInteger.valueOf(nativeInt);
				return -1 == this_int.compareTo(otherValue.openSSL);
			}
		} else {
			if(otherValue.nativeFlag) {
				BigInteger other_int = BigInteger.valueOf(otherValue.nativeInt);
				return -1 == openSSL.compareTo(other_int);
			} else {
				return -1 == openSSL.compareTo(otherValue.openSSL);
			}
		}
	}
	
	//originally operator >
	public boolean isGreaterThan(final int otherValue) {
		mustBound("Unbound left operand of integer comparison.");
		if (nativeFlag) {
			return nativeInt > otherValue;
		} else {
			BigInteger other_int = BigInteger.valueOf(otherValue);
			return 1 == openSSL.compareTo(other_int);

		}
	}

	//originally operator >
	public boolean isGreaterThan(final TitanInteger otherValue) {
		mustBound("Unbound left operand of integer comparison.");
		otherValue.mustBound("Unbound right operand of integer comparison.");
		if (nativeFlag) {
			if(otherValue.nativeFlag) {
				return nativeInt > otherValue.nativeInt;
			} else {
				BigInteger this_int = BigInteger.valueOf(nativeInt);
				return 1 == this_int.compareTo(otherValue.openSSL);
			}
		} else {
			if(otherValue.nativeFlag) {
				BigInteger other_int = BigInteger.valueOf(otherValue.nativeInt);
				return 1 == openSSL.compareTo(other_int);
			} else {
				return 1 == openSSL.compareTo(otherValue.openSSL);
			}
		}
	}
	
	//originally operator <=
	public boolean isLessThanOrEqual(final int otherValue) {
		return !isGreaterThan(otherValue);
	}

	//originally operator <=
	public boolean isLessThanOrEqual(final TitanInteger otherValue) {
		return !isGreaterThan(otherValue);
	}

	//originally operator >=
	public boolean isGreaterThanOrEqual(final int otherValue) {
		return !isGreaterThan(otherValue);
	}

	//originally operator >=
	public boolean isGreaterThanOrEqual(final TitanInteger otherValue) {
		return !isGreaterThan(otherValue);
	}

	//originally clean_up
	public void cleanUp() {
		if(!nativeFlag) {
			openSSL = null;
		}
		boundFlag = false;
	}

	@Override
	public String toString() {
		if ( !boundFlag ) {
			return "<unbound>";
		}
		return getBigInteger().toString();
	}
}
