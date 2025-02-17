/******************************************************************************
 * Copyright (c) 2000-2021 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

/**
 * A class which behaves almost, but not quite, entirely unlike
 * a floating-point value.
 *
 * TODO consider merging into TitanFloat later, to reduce complexity
 *
 * @author Arpad Lovassy
 */
public class Ttcn3Float {

	/**
	 * representation of negative 0, used by isNegativeZero() for comparison
	 */
	private static final long NEGATIVE_ZERO = Double.doubleToLongBits( -0.0 );

	/** the floating-point value */
	private double value;

	/**
	 * Initializes to the provided value.
	 *
	 * @param d the double value to use.
	 * */
	public Ttcn3Float( final double d ) {
		value = d;
	}

	/**
	 * operator double() in the core
	 *
	 * @return the double value.
	 * */
	double getValue() {
		return value;
	}

	/**
	 * Assigns the other value to this value.
	 * Overwriting the current content in the process.
	 *<p>
	 * operator= in the core.
	 *
	 * @param d
	 *                the other value to assign.
	 * @return the new value object.
	 */
	Ttcn3Float operator_assign(final double d){
		value = d;

		return this;
	}

	/**
	 * Assigns the other value to this value.
	 * Overwriting the current content in the process.
	 *<p>
	 * operator= in the core.
	 *
	 * @param d
	 *                the other value to assign.
	 * @return the new value object.
	 */
	Ttcn3Float operator_assign(final Ttcn3Float d){
		this.value = d.value;
		return this;
	}


	// originally operator+
	// this + d
	Ttcn3Float add( final double d ) {
		return new Ttcn3Float( value + d );
	}

	// originally operator-
	Ttcn3Float sub( final double d ) {
		return new Ttcn3Float( value - d );
	}

	// originally operator*
	Ttcn3Float mul( final double d ) {
		return new Ttcn3Float( value * d );
	}

	// originally operator/=
	Ttcn3Float div( final double d ) {
		return new Ttcn3Float( value / d );
	}

	/**
	 * Checks if the current value is less than the provided one.
	 *
	 * operator< in the core
	 *
	 * @param d
	 *                the other value to check against.
	 * @return {@code true} if the value is less than the provided.
	 */
	boolean is_less_than( final double d ) {
		if ( Double.isNaN( value ) ) {
			return false; // TTCN-3 special: NaN is bigger than anything except NaN
		} else if ( Double.isNaN( d ) ) {
			return true; // TTCN-3 special: NaN is bigger than anything except NaN
		} else if ( value == 0.0 && d == 0.0 ) { // does not distinguish -0.0
			return is_negative_zero(value) && !is_negative_zero(d); // value negative, d non-negative
		} else { // finally, the sensible behavior
			return value < d;
		}
	}

	/**
	 * Checks if the current value is greater than the provided one.
	 *
	 * operator> in the core
	 *
	 * @param d
	 *                the other value to check against.
	 * @return {@code true} if the value is greater than the provided.
	 */
	boolean is_greater_than( final double d ) {
		if ( Double.isNaN( value ) ) {
			return !Double.isNaN( d ); // TTCN-3 special: NaN is bigger than anything except NaN
		} else if ( Double.isNaN( d ) ) {
			return false; // TTCN-3 special: NaN is bigger than anything except NaN
		} else if ( value == 0.0 && d == 0.0 ) { // does not distinguish -0.0
			return !is_negative_zero(value) && is_negative_zero(d); // value non-negative, d negative
		} else { // finally, the sensible behavior
			return value > d;
		}
	}

	/**
	 * Checks if the current value is equivalent to the provided one.
	 *
	 * operator== in the core
	 *
	 * @param d
	 *                the other value to check against.
	 * @return {@code true} if the values are equivalent.
	 */
	boolean operator_equals( final double d ) {
		if ( Double.isNaN( value ) ) {
			return Double.isNaN( d ); // TTCN-3 special: NaN is bigger than anything except NaN
		} else if ( Double.isNaN( d ) ) {
			return false;
		} else if ( value == 0.0 && d == 0.0 ) { // does not distinguish -0.0
			return is_negative_zero( value ) == is_negative_zero( d );
		} else { // finally, the sensible behavior
			return value == d;
		}
	}

	/**
	 * Checks if the provided value is negative zero or not.
	 * <p>
	 * TTCN-3 arithmetic handles 0.0 and -0.0 as the same.
	 * But they might have different representations
	 *  in some encodings/decodings.
	 * <p>
	 * signbit in the core
	 *
	 * @param d the value to check.
	 * @return {@code true} if it is -0.0, {@code false} otherwise.
	 * */
	private boolean is_negative_zero( final double d ) {
		return Double.doubleToLongBits( d ) == NEGATIVE_ZERO;
	}

	public String create_java_string_representation() {
		if (Double.isNaN(value)){
			return "Double.NaN";
		} else if (Double.isInfinite(value)) {
			if( Double.compare(value,0)>0) {
				return "Double.POSITIVE_INFINITY";
			} else {
				return "-Double.NEGATIVE_INFINITY";
			}
		} else {
			return Double.toString(value);
		}
	}
	/**
	 * Converts the value to ttcn representation
	 * It is useful in the logging, for example
	 * @return the converted string
	 */
	public String create_ttcn3_string_representation() {
		if (Double.isNaN(value)){
			return "not_a_number";
		} else if (Double.isInfinite(value)) {
			if( Double.compare(value,0)>0) {
				return "infinity";
			} else {
				return "-infinity";
			}
		} else {
			return Double.toString(value);
		}
	}

	/**
	 * Do not use this function!<br>
	 * It is provided by Java and currently used for debugging.
	 * But it is not part of the intentionally provided interface,
	 *   and so can be changed without notice.
	 * <p>
	 * JAVA DESCRIPTION:
	 * <p>
	 * {@inheritDoc}
	 *  */
	@Override
	public String toString() {
		return create_ttcn3_string_representation();
	}
}
