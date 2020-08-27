/******************************************************************************
 * Copyright (c) 2000-2020 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

import java.nio.ByteBuffer;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.titan.runtime.core.BER.ASN_BERdescriptor;
import org.eclipse.titan.runtime.core.BER.ASN_Tag;
import org.eclipse.titan.runtime.core.BER.ASN_TagClass;
import org.eclipse.titan.runtime.core.JSON.TTCN_JSONdescriptor;
import org.eclipse.titan.runtime.core.JSON.json_string_escaping;
import org.eclipse.titan.runtime.core.JSON_Tokenizer.json_token_t;
import org.eclipse.titan.runtime.core.Param_Types.Module_Param_Float;
import org.eclipse.titan.runtime.core.Param_Types.Module_Param_Name;
import org.eclipse.titan.runtime.core.Param_Types.Module_Param_Unbound;
import org.eclipse.titan.runtime.core.Param_Types.Module_Parameter;
import org.eclipse.titan.runtime.core.Param_Types.Module_Parameter.basic_check_bits_t;
import org.eclipse.titan.runtime.core.RAW.RAW_Force_Omit;
import org.eclipse.titan.runtime.core.RAW.RAW_coding_par;
import org.eclipse.titan.runtime.core.RAW.RAW_enc_tr_pos;
import org.eclipse.titan.runtime.core.RAW.RAW_enc_tree;
import org.eclipse.titan.runtime.core.RAW.TTCN_RAWdescriptor;
import org.eclipse.titan.runtime.core.RAW.ext_bit_t;
import org.eclipse.titan.runtime.core.RAW.raw_sign_t;
import org.eclipse.titan.runtime.core.RAW.top_bit_order_t;
import org.eclipse.titan.runtime.core.TTCN_EncDec.coding_type;
import org.eclipse.titan.runtime.core.TTCN_EncDec.error_type;
import org.eclipse.titan.runtime.core.TTCN_EncDec.raw_order_t;
import org.eclipse.titan.runtime.core.TitanCharString.CharCoding;

/**
 * TTCN-3 float
 * @author Arpad Lovassy
 * @author Farkas Izabella Ingrid
 * @author Andrea Palfi
 */
public class TitanFloat extends Base_Type {
	private static final ASN_Tag TitanReal_tag_[] = new ASN_Tag[] {new ASN_Tag(ASN_TagClass.ASN_TAG_UNIV, 9)};
	public static final ASN_BERdescriptor TitanFloat_Ber_ = new ASN_BERdescriptor(1, TitanReal_tag_);
	public static final TTCN_RAWdescriptor TitanFloat_raw_ = new TTCN_RAWdescriptor(64, raw_sign_t.SG_NO, raw_order_t.ORDER_LSB, raw_order_t.ORDER_LSB, raw_order_t.ORDER_LSB, raw_order_t.ORDER_LSB, ext_bit_t.EXT_BIT_NO, raw_order_t.ORDER_LSB, raw_order_t.ORDER_LSB, top_bit_order_t.TOP_BIT_INHERITED, 0, 0, 0, 8, 0, null, -1, CharCoding.UNKNOWN, null, false);
	public static final TTCN_JSONdescriptor TitanFloat_json_ = new TTCN_JSONdescriptor(false, null, false, null, null, false, false, false, 0, null, false, json_string_escaping.ESCAPE_AS_SHORT);

	public static final TTCN_Typedescriptor TitanFloat_descr_ = new TTCN_Typedescriptor("REAL", TitanFloat_Ber_, TitanFloat_raw_, TitanFloat_json_, null);

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

	/**
	 * Initializes to unbound value.
	 * */
	public TitanFloat() {
		super();
	}

	/**
	 * Initializes to a given value.
	 *
	 * @param otherValue
	 *                the value to initialize to.
	 * */
	public TitanFloat(final double otherValue) {
		float_value = new Ttcn3Float(otherValue);
	}

	/**
	 * Initializes to a given value.
	 *
	 * @param otherValue
	 *                the value to initialize to.
	 * */
	public TitanFloat(final Ttcn3Float otherValue) {
		float_value = otherValue;
	}

	/**
	 * Initializes to a given value.
	 *
	 * @param otherValue
	 *                the value to initialize to.
	 * */
	public TitanFloat(final TitanFloat otherValue) {
		otherValue.must_bound("Copying an unbound float value.");

		float_value = otherValue.float_value;
	}

	/**
	 * Returns the value of this float.
	 *
	 * operator double() in the core
	 *
	 * @return the float value as double.
	 * */
	public Double get_value() {
		must_bound("Using the value of an unbound float variable.");
		return float_value.getValue();
	}

	/**
	 * Assigns the other value to this value.
	 * Overwriting the current content in the process.
	 *<p>
	 * operator= in the core.
	 *
	 * @param otherValue
	 *                the other value to assign.
	 * @return the new value object.
	 */
	public TitanFloat operator_assign(final double otherValue) {
		float_value = new Ttcn3Float(otherValue);

		return this;
	}

	/**
	 * Assigns the other value to this value.
	 * Overwriting the current content in the process.
	 *<p>
	 * operator= in the core.
	 *
	 * @param otherValue
	 *                the other value to assign.
	 * @return the new value object.
	 */
	public TitanFloat operator_assign(final Ttcn3Float otherValue) {
		float_value = otherValue;

		return this;
	}

	/**
	 * Assigns the other value to this value.
	 * Overwriting the current content in the process.
	 *<p>
	 * operator= in the core.
	 *
	 * @param otherValue
	 *                the other value to assign.
	 * @return the new value object.
	 */
	public TitanFloat operator_assign(final TitanFloat otherValue) {
		otherValue.must_bound("Assignment of an unbound float value.");

		if (otherValue != this) {
			float_value = otherValue.float_value;
		}

		return this;
	}

	@Override
	public TitanFloat operator_assign(final Base_Type otherValue) {
		if (otherValue instanceof TitanFloat) {
			return operator_assign((TitanFloat) otherValue);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to float", otherValue));
	}

	@Override
	public boolean is_bound() {
		return float_value != null;
	}

	@Override
	public boolean is_present() {
		return is_bound();
	};

	@Override
	public boolean is_value() {
		return float_value != null;
	}

	/**
	 * Checks if the provided float value is plus infinity, minus infinity
	 * or NaN.
	 *
	 * @param other_value
	 *                the value to check.
	 * @return {@code true} if it is plus infinity, minus infinity or NaN,
	 *         {@code false} otherwise.
	 * */
	public static TitanBoolean is_special(final double other_value) {
		return new TitanBoolean(other_value == PLUS_INFINITY || other_value == MINUS_INFINITY || Double.isNaN(other_value));
	}

	/**
	 * Checks if the provided value is a numeric value or not.
	 *
	 * @param float_value
	 *                the value to check.
	 * @param error_msg
	 *                the error message to log if the value is not numeric.
	 * */
	public static void check_numeric(final double float_value, final String error_msg) {
		if (is_special(float_value).get_value()) {
			throw new TtcnError(MessageFormat.format("{0} must be a numeric value instead of {1}", error_msg, float_value));
		}
	}

	/**
	 * Represents the unary operator+.
	 * Creates a copy of the current value.
	 *
	 * operator+ in the core
	 *
	 * @return a copy of the current value.
	 */
	public TitanFloat add() {
		must_bound("Unbound float operand of unary + operator.");

		return new TitanFloat(float_value);
	}

	/**
	 * Returns a float whose value is this + other_value.
	 *
	 * operator+ in the core
	 *
	 * @param other_value
	 *                the other value to use.
	 * @return this + other_value
	 */
	public TitanFloat add(final double other_value) {
		must_bound("Unbound left operand of float addition.");

		return new TitanFloat(float_value.add(other_value));
	}

	/**
	 * Returns a float whose value is this + other_value.
	 *
	 * operator+ in the core
	 *
	 * @param other_value
	 *                the other value to use.
	 * @return this + other_value
	 */
	public TitanFloat add(final Ttcn3Float other_value) {
		must_bound("Unbound left operand of float addition.");

		return new TitanFloat(float_value.add(other_value.getValue()));
	}

	/**
	 * Returns a float whose value is this + other_value.
	 *
	 * operator+ in the core
	 *
	 * @param other_value
	 *                the other value to use.
	 * @return this + other_value
	 */
	public TitanFloat add(final TitanFloat other_value) {
		must_bound("Unbound left operand of float addition.");
		other_value.must_bound("Unbound right operand of float addition.");

		return new TitanFloat(float_value.add(other_value.float_value.getValue()));
	}

	/**
	 * Negates the current value.
	 *
	 * operator- in the core
	 *
	 * @return the negated value.
	 */
	public TitanFloat sub() {
		must_bound("Unbound float operand of unary - operator (negation).");

		return new TitanFloat(-float_value.getValue());
	}

	/**
	 * Returns a float whose value is this - other_value.
	 *
	 * operator- in the core
	 *
	 * @param other_value
	 *                the other value to use.
	 * @return this - other_value
	 */
	public TitanFloat sub(final double other_value) {
		must_bound("Unbound left operand of float subtraction.");

		return new TitanFloat(float_value.sub(other_value));
	}

	/**
	 * Returns a float whose value is this - other_value.
	 *
	 * operator- in the core
	 *
	 * @param other_value
	 *                the other value to use.
	 * @return this - other_value
	 */
	public TitanFloat sub(final Ttcn3Float other_value) {
		must_bound("Unbound left operand of float subtraction.");

		return new TitanFloat(float_value.sub(other_value.getValue()));
	}

	/**
	 * Returns a float whose value is this - other_value.
	 *
	 * operator- in the core
	 *
	 * @param other_value
	 *                the other value to use.
	 * @return this - other_value
	 */
	public TitanFloat sub(final TitanFloat other_value) {
		must_bound("Unbound left operand of float subtraction.");
		other_value.must_bound("Unbound right operand of float subtraction.");

		return new TitanFloat(float_value.sub(other_value.float_value.getValue()));
	}

	/**
	 * Returns a float whose value is this * other_value.
	 *
	 * operator* in the core
	 *
	 * @param other_value
	 *                the other value to use.
	 * @return this * other_value
	 */
	public TitanFloat mul(final double other_value) {
		must_bound("Unbound left operand of float multiplication.");

		return new TitanFloat(float_value.mul(other_value));
	}

	/**
	 * Returns a float whose value is this * other_value.
	 *
	 * operator* in the core
	 *
	 * @param other_value
	 *                the other value to use.
	 * @return this * other_value
	 */
	public TitanFloat mul(final Ttcn3Float other_value) {
		must_bound("Unbound left operand of float multiplication.");

		return new TitanFloat(float_value.mul(other_value.getValue()));
	}

	/**
	 * Returns a float whose value is this * other_value.
	 *
	 * operator* in the core
	 *
	 * @param other_value
	 *                the other value to use.
	 * @return this * other_value
	 */
	public TitanFloat mul(final TitanFloat other_value) {
		must_bound("Unbound left operand of float multiplication.");
		other_value.must_bound("Unbound right operand of float multiplication.");

		return new TitanFloat(float_value.mul(other_value.float_value.getValue()));
	}

	/**
	 * Returns a float whose value is this / other_value.
	 *
	 * operator/ in the core
	 *
	 * @param other_value
	 *                the other value to use.
	 * @return this / other_value
	 */
	public TitanFloat div(final double other_value) {
		must_bound("Unbound left operand of float division.");

		if (other_value == 0.0) {
			throw new TtcnError("Float division by zero.");
		}

		return new TitanFloat(float_value.div(other_value));
	}

	/**
	 * Returns a float whose value is this / other_value.
	 *
	 * operator/ in the core
	 *
	 * @param other_value
	 *                the other value to use.
	 * @return this / other_value
	 */
	public TitanFloat div(final Ttcn3Float other_value) {
		must_bound("Unbound left operand of float division.");

		if (other_value.getValue() == 0.0) {
			throw new TtcnError("Float division by zero.");
		}

		return new TitanFloat(float_value.div(other_value.getValue()));
	}

	/**
	 * Returns a float whose value is this / other_value.
	 *
	 * operator/ in the core
	 *
	 * @param other_value
	 *                the other value to use.
	 * @return this / other_value
	 */
	public TitanFloat div(final TitanFloat other_value) {
		must_bound("Unbound left operand of float division.");
		other_value.must_bound("Unbound right operand of float division.");

		final double otherValue = other_value.float_value.getValue();
		if (otherValue == 0.0) {
			throw new TtcnError("Float division by zero.");
		}

		return new TitanFloat(float_value.div(otherValue));
	}

	/**
	 * Checks if the current value is equivalent to the provided one.
	 *
	 * operator== in the core
	 *
	 * @param otherValue
	 *                the other value to check against.
	 * @return {@code true} if the values are equivalent.
	 */
	public boolean operator_equals(final double otherValue) {
		must_bound("Unbound left operand of float comparison.");

		return float_value.operator_equals(otherValue);
	}

	/**
	 * Checks if the current value is equivalent to the provided one.
	 *
	 * operator== in the core
	 *
	 * @param otherValue
	 *                the other value to check against.
	 * @return {@code true} if the values are equivalent.
	 */
	public boolean operator_equals(final Ttcn3Float otherValue) {
		must_bound("Unbound left operand of float comparison.");

		return float_value.operator_equals(otherValue.getValue());
	}

	/**
	 * Checks if the current value is equivalent to the provided one.
	 *
	 * operator== in the core
	 *
	 * @param otherValue
	 *                the other value to check against.
	 * @return {@code true} if the values are equivalent.
	 */
	public boolean operator_equals(final TitanFloat otherValue) {
		must_bound("Unbound left operand of float comparison.");
		otherValue.must_bound("Unbound right operand of float comparison.");

		return float_value.operator_equals(otherValue.float_value.getValue());
	}

	@Override
	public boolean operator_equals(final Base_Type otherValue) {
		if (otherValue instanceof TitanFloat) {
			return operator_equals((TitanFloat) otherValue);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to charstring", otherValue));
	}

	/**
	 * Checks if the current value is not equivalent to the provided one.
	 *
	 * operator!= in the core
	 *
	 * @param otherValue
	 *                the other value to check against.
	 * @return {@code true} if the values are not equivalent.
	 */
	public boolean operator_not_equals(final double otherValue) {
		return !operator_equals(otherValue);
	}

	/**
	 * Checks if the current value is not equivalent to the provided one.
	 *
	 * operator!= in the core
	 *
	 * @param otherValue
	 *                the other value to check against.
	 * @return {@code true} if the values are not equivalent.
	 */
	public boolean operator_not_equals(final Ttcn3Float otherValue) {
		return !operator_equals(otherValue);
	}

	/**
	 * Checks if the current value is not equivalent to the provided one.
	 *
	 * operator!= in the core
	 *
	 * @param otherValue
	 *                the other value to check against.
	 * @return {@code true} if the values are not equivalent.
	 */
	public boolean operator_not_equals(final TitanFloat otherValue) {
		return !operator_equals(otherValue);
	}

	/**
	 * Checks if the current value is less than the provided one.
	 *
	 * operator< in the core
	 *
	 * @param otherValue
	 *                the other value to check against.
	 * @return {@code true} if the value is less than the provided.
	 */
	public boolean is_less_than(final double otherValue) {
		must_bound("Unbound left operand of float comparison.");

		return float_value.is_less_than(otherValue);
	}

	/**
	 * Checks if the current value is less than the provided one.
	 *
	 * operator< in the core
	 *
	 * @param otherValue
	 *                the other value to check against.
	 * @return {@code true} if the value is less than the provided.
	 */
	public boolean is_less_than(final Ttcn3Float otherValue) {
		must_bound("Unbound left operand of float comparison.");

		return float_value.is_less_than(otherValue.getValue());
	}

	/**
	 * Checks if the current value is less than the provided one.
	 *
	 * operator< in the core
	 *
	 * @param otherValue
	 *                the other value to check against.
	 * @return {@code true} if the value is less than the provided.
	 */
	public boolean is_less_than(final TitanFloat otherValue) {
		must_bound("Unbound left operand of float comparison.");
		otherValue.must_bound("Unbound right operand of float comparison.");

		return float_value.is_less_than(otherValue.float_value.getValue());
	}

	/**
	 * Checks if the current value is greater than the provided one.
	 *
	 * operator> in the core
	 *
	 * @param otherValue
	 *                the other value to check against.
	 * @return {@code true} if the value is greater than the provided.
	 */
	public boolean is_greater_than(final double otherValue) {
		must_bound("Unbound left operand of float comparison.");

		return float_value.is_greater_than(otherValue);
	}

	/**
	 * Checks if the current value is greater than the provided one.
	 *
	 * operator> in the core
	 *
	 * @param otherValue
	 *                the other value to check against.
	 * @return {@code true} if the value is greater than the provided.
	 */
	public boolean is_greater_than(final Ttcn3Float otherValue) {
		must_bound("Unbound left operand of float comparison.");

		return float_value.is_greater_than(otherValue.getValue());
	}

	/**
	 * Checks if the current value is greater than the provided one.
	 *
	 * operator> in the core
	 *
	 * @param otherValue
	 *                the other value to check against.
	 * @return {@code true} if the value is greater than the provided.
	 */
	public boolean is_greater_than(final TitanFloat otherValue) {
		must_bound("Unbound left operand of float comparison.");
		otherValue.must_bound("Unbound right operand of float comparison.");

		return float_value.is_greater_than(otherValue.float_value.getValue());
	}

	/**
	 * Checks if the current value is less than or equivalent to the provided one.
	 *
	 * operator<= in the core
	 *
	 * @param otherValue
	 *                the other value to check against.
	 * @return {@code true} if the value is less than or equivalent to the provided.
	 */
	public boolean is_less_than_or_equal(final double otherValue) {
		return !is_greater_than(otherValue);
	}

	/**
	 * Checks if the current value is less than or equivalent to the provided one.
	 *
	 * operator<= in the core
	 *
	 * @param otherValue
	 *                the other value to check against.
	 * @return {@code true} if the value is less than or equivalent to the provided.
	 */
	public boolean is_less_than_or_equal(final Ttcn3Float otherValue) {
		return !is_greater_than(otherValue);
	}

	/**
	 * Checks if the current value is less than or equivalent to the provided one.
	 *
	 * operator<= in the core
	 *
	 * @param otherValue
	 *                the other value to check against.
	 * @return {@code true} if the value is less than or equivalent to the provided.
	 */
	public boolean is_less_than_or_equal(final TitanFloat otherValue) {
		return !is_greater_than(otherValue);
	}

	/**
	 * Checks if the current value is greater than or equivalent to the
	 * provided one.
	 *
	 * operator>= in the core
	 *
	 * @param otherValue
	 *                the other value to check against.
	 * @return {@code true} if the value is greater than or equivalent to
	 *         the provided.
	 */
	public boolean is_greater_than_or_equal(final double otherValue) {
		return !is_less_than(otherValue);
	}

	/**
	 * Checks if the current value is greater than or equivalent to the
	 * provided one.
	 *
	 * operator>= in the core
	 *
	 * @param otherValue
	 *                the other value to check against.
	 * @return {@code true} if the value is greater than or equivalent to
	 *         the provided.
	 */
	public boolean is_greater_than_or_equal(final Ttcn3Float otherValue) {
		return !is_less_than(otherValue);
	}

	/**
	 * Checks if the current value is greater than or equivalent to the
	 * provided one.
	 *
	 * operator>= in the core
	 *
	 * @param otherValue
	 *                the other value to check against.
	 * @return {@code true} if the value is greater than or equivalent to
	 *         the provided.
	 */
	public boolean is_greater_than_or_equal(final TitanFloat otherValue) {
		return !is_less_than(otherValue);
	}

	@Override
	public void log() {
		if (float_value != null) {
			log_float(float_value.getValue());
		} else {
			TTCN_Logger.log_event_unbound();
		}
	}

	@Override
	public void clean_up() {
		float_value = null;
	}

	static void log_float(final double float_val) {
		if ((float_val > -TitanFloat.MAX_DECIMAL_FLOAT && float_val <= -TitanFloat.MIN_DECIMAL_FLOAT)
				|| (float_val >= MIN_DECIMAL_FLOAT && float_val < TitanFloat.MAX_DECIMAL_FLOAT) || (float_val == 0.0)) {
			TTCN_Logger.log_event("%f", float_val);
		} else if (float_val == PLUS_INFINITY) {
			TTCN_Logger.log_event_str("infinity");
		} else if (float_val == MINUS_INFINITY) {
			TTCN_Logger.log_event_str("-infinity");
		} else if (float_val != float_val) {
			TTCN_Logger.log_event_str("not_a_number");
		} else {
			TTCN_Logger.log_event("%e", float_val);
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
		if (float_value == null) {
			return "<unbound>";
		}

		return float_value.toString();
	}

	@Override
	/** {@inheritDoc} */
	public void encode_text(final Text_Buf text_buf) {
		must_bound("Text encoder: Encoding an unbound float value.");

		text_buf.push_double(float_value.getValue());
	}

	@Override
	/** {@inheritDoc} */
	public void decode_text(final Text_Buf text_buf) {
		operator_assign(text_buf.pull_double());
	}

	/**
	 * Returns a float whose value is double_value + other_value.
	 *
	 * static operator+ in the core
	 *
	 * @param double_value
	 *                the first value to use.
	 * @param other_value
	 *                the other value to use.
	 * @return double_value + other_value
	 */
	public static TitanFloat add(final double double_value, final TitanFloat other_value) {
		other_value.must_bound("Unbound right operand of float addition.");

		return new TitanFloat(other_value.add(double_value));
	}

	/**
	 * Returns a float whose value is double_value - other_value.
	 *
	 * static operator- in the core
	 *
	 * @param double_value
	 *                the first value to use.
	 * @param other_value
	 *                the other value to use.
	 * @return double_value - other_value
	 */
	public static TitanFloat sub(final double double_value, final TitanFloat other_value) {
		other_value.must_bound("Unbound right operand of float subtraction.");

		return new TitanFloat(double_value - other_value.get_value());
	}

	/**
	 * Returns a float whose value is double_value * other_value.
	 *
	 * static operator* in the core
	 *
	 * @param double_value
	 *                the first value to use.
	 * @param other_value
	 *                the other value to use.
	 * @return double_value * other_value
	 */
	public static TitanFloat mul(final double double_value, final TitanFloat other_value) {
		other_value.must_bound("Unbound right operand of float multiplication.");

		return new TitanFloat(other_value.mul(double_value));
	}

	/**
	 * Returns a float whose value is double_value / other_value.
	 *
	 * static operator/ in the core
	 *
	 * @param double_value
	 *                the first value to use.
	 * @param other_value
	 *                the other value to use.
	 * @return double_value / other_value
	 */
	public static TitanFloat div(final double double_value, final TitanFloat other_value) {
		other_value.must_bound("Unbound right operand of float division.");

		final double value = other_value.float_value.getValue();
		if (value == 0.0) {
			throw new TtcnError("Float division by zero.");
		}

		return new TitanFloat(double_value / other_value.get_value());
	}

	/**
	 * Checks if the first value is equivalent to the second one.
	 *
	 * static operator== in the core
	 *
	 * @param doubleValue
	 *                the first value.
	 * @param otherValue
	 *                the second value to check against.
	 * @return {@code true} if the values are equivalent.
	 */
	public static boolean operator_equals(final double doubleValue, final TitanFloat otherValue) {
		otherValue.must_bound("Unbound right operand of float comparison.");

		return otherValue.operator_equals(doubleValue);
	}

	/**
	 * Checks if the first value is not equivalent to the second one.
	 *
	 * static operator!= in the core
	 *
	 * @param doubleValue
	 *                the first value.
	 * @param otherValue
	 *                the second value to check against.
	 * @return {@code true} if the values are not equivalent.
	 */
	public static boolean operator_not_equals(final double doubleValue, final TitanFloat otherValue) {
		otherValue.must_bound("Unbound right operand of float comparison.");

		return otherValue.operator_not_equals(doubleValue);
	}

	/**
	 * Checks if the first value is less than the second one.
	 *
	 * static operator< in the core
	 *
	 * @param doubleValue
	 *                the first value.
	 * @param otherValue
	 *                the second value to check against.
	 * @return {@code true} if the first value is less than the second.
	 */
	public static boolean is_less_than(final double doubleValue, final TitanFloat otherValue) {
		otherValue.must_bound("Unbound right operand of float comparison.");

		return otherValue.is_greater_than(doubleValue);
	}

	/**
	 * Checks if the first value is greater than the second one.
	 *
	 * static operator> in the core
	 *
	 * @param doubleValue
	 *                the first value.
	 * @param otherValue
	 *                the second value to check against.
	 * @return {@code true} if the first value is greater than the second.
	 */
	public static boolean is_greater_than(final double doubleValue, final TitanFloat otherValue) {
		otherValue.must_bound("Unbound right operand of float comparison.");

		return otherValue.is_less_than(doubleValue);
	}

	/**
	 * Checks if the first value is less than or equal to the second one.
	 *
	 * static operator<= in the core
	 *
	 * @param doubleValue
	 *                the first value.
	 * @param otherValue
	 *                the second value to check against.
	 * @return {@code true} if the first value is less than or equal to the
	 *         second.
	 */
	public static TitanBoolean is_less_than_or_equal(final double doubleValue, final TitanFloat otherValue) {
		otherValue.must_bound("Unbound right operand of float comparison.");

		return new TitanBoolean(otherValue.is_greater_than_or_equal(new TitanFloat(doubleValue)));
	}

	/**
	 * Checks if the first value is greater than or equal to the second one.
	 *
	 * static operator>= in the core
	 *
	 * @param intValue
	 *                the first value.
	 * @param otherValue
	 *                the second value to check against.
	 * @return {@code true} if the first value is greater than or equal the
	 *         second.
	 */
	public static TitanBoolean is_greater_than_or_equal(final double doubleValue, final TitanFloat otherValue) {
		otherValue.must_bound("Unbound right operand of float comparison.");

		return new TitanBoolean(otherValue.is_less_than_or_equal(new TitanFloat(doubleValue)));
	}

	@Override
	/** {@inheritDoc} */
	public void encode(final TTCN_Typedescriptor p_td, final TTCN_Buffer p_buf, final coding_type p_coding, final int flavour) {
		switch (p_coding) {
		case CT_RAW: {
			final TTCN_EncDec_ErrorContext errorContext = new TTCN_EncDec_ErrorContext("While RAW-encoding type '%s': ", p_td.name);
			try {
				if (p_td.raw == null) {
					TTCN_EncDec_ErrorContext.error_internal("No RAW descriptor available for type '%s'.", p_td.name);
				}

				final RAW_enc_tr_pos tree_position = new RAW_enc_tr_pos(0, null);
				final RAW_enc_tree root = new RAW_enc_tree(true, null, tree_position, 1, p_td.raw);
				RAW_encode(p_td, root);
				root.put_to_buf(p_buf);
			} finally {
				errorContext.leave_context();
			}
			break;
		}
		case CT_JSON: {
			final TTCN_EncDec_ErrorContext errorContext = new TTCN_EncDec_ErrorContext("While JSON-encoding type '%s': ", p_td.name);
			try {
				if(p_td.json == null) {
					TTCN_EncDec_ErrorContext.error_internal("No JSON descriptor available for type '%s'.", p_td.name);
				}

				final JSON_Tokenizer tok = new JSON_Tokenizer(flavour != 0);
				JSON_encode(p_td, tok);
				final StringBuilder temp = tok.get_buffer();
				for (int i = 0; i < temp.length(); i++) {
					final int temp2 = temp.charAt(i);
					p_buf.put_c((byte)temp2);
				}
			} finally {
				errorContext.leave_context();
			}
			break;
		}
		default:
			throw new TtcnError(MessageFormat.format("Unknown coding method requested to encode type `{0}''", p_td.name));
		}
	}

	@Override
	/** {@inheritDoc} */
	public void decode(final TTCN_Typedescriptor p_td, final TTCN_Buffer p_buf, final coding_type p_coding, final int flavour) {
		switch (p_coding) {
		case CT_RAW: {
			final TTCN_EncDec_ErrorContext errorContext = new TTCN_EncDec_ErrorContext("While RAW-decoding type '%s': ", p_td.name);
			try {
				if (p_td.raw == null) {
					TTCN_EncDec_ErrorContext.error_internal("No RAW descriptor available for type '%s'.", p_td.name);
				}

				final raw_order_t order = p_td.raw.top_bit_order == top_bit_order_t.TOP_BIT_LEFT ? raw_order_t.ORDER_LSB : raw_order_t.ORDER_MSB;
				if (RAW_decode(p_td, p_buf, p_buf.get_len() * 8, order) < 0) {
					TTCN_EncDec_ErrorContext.error(error_type.ET_INCOMPL_ANY, "Can not decode type '%s', because invalid or incomplete message was received", p_td.name);
				}
			} finally {
				errorContext.leave_context();
			}
			break;
		}
		case CT_JSON: {
			final TTCN_EncDec_ErrorContext errorContext = new TTCN_EncDec_ErrorContext("While JSON-decoding type '%s': ", p_td.name);
			try {
				if(p_td.json == null) {
					TTCN_EncDec_ErrorContext.error_internal("No JSON descriptor available for type '%s'.", p_td.name);
				}

				final byte[] data = p_buf.get_data();
				final char[] temp = new char[data.length];
				for (int i = 0; i < data.length; i++) {
					temp[i] = (char)data[i];
				}
				final JSON_Tokenizer tok = new JSON_Tokenizer(new String(temp), p_buf.get_len());
				if(JSON_decode(p_td, tok, false) < 0) {
					TTCN_EncDec_ErrorContext.error(TTCN_EncDec.error_type.ET_INCOMPL_MSG,
							"Can not decode type '%s', because invalid or incomplete message was received", p_td.name);
				}
				p_buf.set_pos(tok.get_buf_pos());
			} finally {
				errorContext.leave_context();
			}
			break;
		}
		default:
			throw new TtcnError(MessageFormat.format("Unknown coding method requested to decode type `{0}''", p_td.name));
		}
	}

	@Override
	/** {@inheritDoc} */
	public int RAW_encode(final TTCN_Typedescriptor p_td, final RAW_enc_tree myleaf) {
		byte[] bc;
		final int length = p_td.raw.fieldlength / 8;
		double tmp = float_value.getValue();
		if (!is_bound()) {
			TTCN_EncDec_ErrorContext.error(error_type.ET_UNBOUND, "Encoding an unbound value.");
			tmp = 0.0;
		}
		if (Double.isNaN(tmp)) {
			TTCN_EncDec_ErrorContext.error_internal("Value is NaN.");
		}
		if (length > RAW.RAW_INT_ENC_LENGTH) {
			myleaf.data_array = bc = new byte[length];
		} else {
			bc = myleaf.data_array;
		}
		if (length == 8) {
			final byte[] dv = new byte[8];
			ByteBuffer.wrap(dv).putDouble(tmp);
			for (int i = 0, k = 7; i < 8; i++, k--) {
				bc[i] = dv[k];
			}
		} else if (length == 4) {
			if (tmp == 0.0) {
				for (int i = 0; i < 4; i++) {
					bc[i] = 0;
				}
			} else if (tmp == -0.0) {
				for (int i = 0; i < 4; i++) {
					bc[i] = 0;
				}
				bc[0] |= 0x80;
			} else {
				int index = 0;
				final int adj = 1;

				final byte[] dv = new byte[8];
				ByteBuffer.wrap(dv).putDouble(tmp);
				bc[0] = (byte) (dv[index] & 0x80);
				int exponent = dv[index] & 0x7F;
				exponent <<= 4;
				index += adj;
				exponent += (dv[index] & 0xF0) >> 4;
				exponent -= 1023;

				if (exponent > 127) {
					TTCN_EncDec_ErrorContext.error(error_type.ET_LEN_ERR,"The float value %f is out of the range of the single precision: %s", float_value.getValue(), p_td.name);
					tmp = 0.0;
					exponent = 0;
				} else if (exponent < -127) {
					TTCN_EncDec_ErrorContext.error(error_type.ET_FLOAT_TR, "The float value %f is too small to represent it in single precision: %s", float_value.getValue(), p_td.name);
					tmp = 0.0;
					exponent = 0;
				} else {
					exponent += 127;
				}
				bc[0] |= (exponent >> 1) & 0x7F;
				bc[1] = (byte) (((exponent << 7) & 0x80) | ((dv[index] & 0x0F) << 3) | ((dv[index + adj] & 0xE0) >> 5));
				index += adj;
				bc[2] = (byte) (((dv[index] & 0x1F) << 3) | ((dv[index + adj] & 0xE0) >> 5));
				index += adj;
				bc[3] = (byte) (((dv[index] & 0x1F) << 3) | ((dv[index + adj] & 0xE0) >> 5));
			}
		} else {
			TTCN_EncDec_ErrorContext.error_internal("Invalid FLOAT length {0}", length);
		}

		myleaf.coding_par.csn1lh = p_td.raw.csn1lh;

		return myleaf.length = p_td.raw.fieldlength;
	}

	@Override
	/** {@inheritDoc} */
	public int RAW_decode(final TTCN_Typedescriptor p_td, final TTCN_Buffer buff, int limit, final raw_order_t top_bit_ord, final boolean no_err, final int sel_field, final boolean first_call, final RAW_Force_Omit force_omit) {
		final int prepaddlength = buff.increase_pos_padd(p_td.raw.prepadding);
		limit -= prepaddlength;
		int decode_length = p_td.raw.fieldlength;
		final TTCN_EncDec_ErrorContext errorcontext = new TTCN_EncDec_ErrorContext();
		try {
			if (p_td.raw.fieldlength > limit || p_td.raw.fieldlength > buff.unread_len_bit()) {
				if (no_err) {
					return -1;
				}
				TTCN_EncDec_ErrorContext.error(error_type.ET_LEN_ERR, "There is not enough bits in the buffer to decode type %s.", p_td.name);
				decode_length = limit > (int) buff.unread_len_bit() ? buff.unread_len_bit() : limit;
				float_value = new Ttcn3Float(0.0);
				decode_length += buff.increase_pos_padd(p_td.raw.padding);
				return decode_length + prepaddlength;
			}

			double tmp = 0.0;
			final byte[] data = new byte[16];
			final RAW_coding_par cp = new RAW_coding_par();
			boolean orders = p_td.raw.bitorderinoctet == raw_order_t.ORDER_MSB;
			if (p_td.raw.bitorderinfield == raw_order_t.ORDER_MSB) {
				orders = !orders;
			}
			cp.bitorder = orders ? raw_order_t.ORDER_MSB : raw_order_t.ORDER_LSB;
			orders = p_td.raw.byteorder == raw_order_t.ORDER_MSB;
			if (p_td.raw.bitorderinfield == raw_order_t.ORDER_MSB) {
				orders = !orders;
			}
			cp.byteorder = orders ? raw_order_t.ORDER_MSB : raw_order_t.ORDER_LSB;
			cp.fieldorder = p_td.raw.fieldorder;
			cp.hexorder = raw_order_t.ORDER_LSB;
			cp.csn1lh = p_td.raw.csn1lh;
			buff.get_b(decode_length, data, cp, top_bit_ord);
			if (decode_length == 64) {
				final byte[] dv = new byte[8];
				for (int i = 0, k = 7; i < 8; i++, k--) {
					dv[i] = data[k];
				}
				tmp = ByteBuffer.wrap(dv).getDouble();
				if (Double.isNaN(tmp)) {
					if (no_err) {
						return -1;
					}
					TTCN_EncDec_ErrorContext.error(error_type.ET_LEN_ERR, "Not a Number received for type %s.", p_td.name);
					tmp = 0.0;
				}
			} else if (decode_length == 32) {
				final int sign = (data[0] & 0x80) >> 7;
				int exponent = ((data[0] & 0x7F) << 1) | ((data[1] & 0x80) >> 7);
				int fraction = ((data[1] & 0x7F) << 1) | ((data[2] & 0x80) >> 7);
				fraction <<= 8;
				fraction += ((data[2] & 0x7F) << 1) | ((data[3] & 0x80) >> 7);
				fraction <<= 7;
				fraction += data[3] & 0x7F;
				if (exponent == 0 && fraction == 0) {
					tmp = sign != 0 ? -0.0 : 0.0;
				} else if (exponent == 0xFF && fraction != 0) {
					if (no_err) {
						return -1;
					}
					TTCN_EncDec_ErrorContext.error(error_type.ET_LEN_ERR, "Not a Number received for type %s.", p_td.name);
					tmp = 0.0;
				}  else if (exponent == 0 && fraction != 0) {
					final double sign_v = sign != 0 ? -1.0 : 1.0;
					tmp = sign_v * ((double)(fraction) / 8388608.0) * Math.pow(2.0, -126.0);
				} else {
					final double sign_v = sign != 0 ? -1.0 : 1.0;
					exponent -= 127;
					tmp = sign_v * (1.0 + (double)(fraction) / 8388608.0) * Math.pow(2.0,(double)(exponent));
				}
			}
			decode_length += buff.increase_pos_padd(p_td.raw.padding);
			float_value = new Ttcn3Float(tmp);
		} finally {
			errorcontext.leave_context();
		}

		return decode_length + prepaddlength;
	}

	@Override
	/** {@inheritDoc} */
	public void set_param(Module_Parameter param) {
		param.basic_check(basic_check_bits_t.BC_VALUE.getValue(), "float value");

		// Originally RT2
		if (param.get_type() == Module_Parameter.type_t.MP_Reference) {
			param = param.get_referenced_param().get();
		}

		switch (param.get_type()) {
		case MP_Float:
			operator_assign(param.get_float());
			break;
		case MP_Expression:
			switch (param.get_expr_type()) {
			case EXPR_NEGATE: {
				final TitanFloat operand = new TitanFloat();
				operand.set_param(param.get_operand1());
				operator_assign(operand.sub());
				break; }
			case EXPR_ADD: {
				final TitanFloat operand1 = new TitanFloat();
				final TitanFloat operand2 = new TitanFloat();
				operand1.set_param(param.get_operand1());
				operand2.set_param(param.get_operand2());
				operator_assign(operand1.add(operand2));
				break;
			}
			case EXPR_SUBTRACT: {
				final TitanFloat operand1 = new TitanFloat();
				final TitanFloat operand2 = new TitanFloat();
				operand1.set_param(param.get_operand1());
				operand2.set_param(param.get_operand2());
				operator_assign(operand1.sub(operand2));
				break;
			}
			case EXPR_MULTIPLY: {
				final TitanFloat operand1 = new TitanFloat();
				final TitanFloat operand2 = new TitanFloat();
				operand1.set_param(param.get_operand1());
				operand2.set_param(param.get_operand2());
				operator_assign(operand1.mul(operand2));
				break;
			}
			case EXPR_DIVIDE: {
				final TitanFloat operand1 = new TitanFloat();
				final TitanFloat operand2 = new TitanFloat();
				operand1.set_param(param.get_operand1());
				operand2.set_param(param.get_operand2());
				if (operand2.operator_equals(0)) {
					param.error("Floating point division by zero.");
				}
				operator_assign(operand1.div(operand2));
				break; }
			default:
				param.expr_type_error("a float");
				break;
			}
			break;
		default:
			param.type_error("float value");
			break;
		}
	}

	@Override
	/** {@inheritDoc} */
	public Module_Parameter get_param(final Module_Param_Name param_name) {
		if (!is_bound()) {
			return new Module_Param_Unbound();
		}
		return new Module_Param_Float(float_value.getValue());
	}

	private static final String POS_INF_STR = "\"infinity\"";
	private static final String NEG_INF_STR = "\"-infinity\"";
	private static final String NAN_STR = "\"not_a_number\"";
	private static final String POS_INF_STR_DEFAULT = "infinity";
	private static final String NEG_INF_STR_DEFAULT = "-infinity";
	private static final String NAN_STR_DEFAULT = "not_a_number";

	@Override
	public int JSON_encode(final TTCN_Typedescriptor cborFloatDescr, final JSON_Tokenizer p_tok, final boolean p_parent_is_map) {
		if (!is_bound()) {
			TTCN_EncDec_ErrorContext.error(TTCN_EncDec.error_type.ET_UNBOUND,
					"Encoding an unbound float value.");
			return -1;
		}

		final double value = float_value.getValue();
		if (Double.POSITIVE_INFINITY == value) {
			return p_tok.put_next_token(json_token_t.JSON_TOKEN_STRING, POS_INF_STR);
		}
		if (Double.NEGATIVE_INFINITY == value) {
			return p_tok.put_next_token(json_token_t.JSON_TOKEN_STRING, NEG_INF_STR);
		}
		if (Double.isNaN(value)) {
			return p_tok.put_next_token(json_token_t.JSON_TOKEN_STRING, NAN_STR);
		}

		// true if decimal representation possible (use %f format)
		final boolean decimal_repr = (value == 0.0)
				|| (value > -MAX_DECIMAL_FLOAT && value <= -MIN_DECIMAL_FLOAT)
				|| (value >= MIN_DECIMAL_FLOAT && value < MAX_DECIMAL_FLOAT);

		final String tmp_str = String.format(Locale.US, decimal_repr ? "%f" : "%e", value);
		final int enc_len = p_tok.put_next_token(json_token_t.JSON_TOKEN_NUMBER, tmp_str);
		return enc_len;
	}

	@Override
	public int JSON_decode(final TTCN_Typedescriptor p_td, final JSON_Tokenizer p_tok, final boolean p_silent, final boolean p_parent_is_map, final int p_chosen_field) {
		final AtomicReference<json_token_t> token = new AtomicReference<json_token_t>(json_token_t.JSON_TOKEN_NONE);
		final StringBuilder value = new StringBuilder();
		final AtomicInteger value_len = new AtomicInteger(0);
		int dec_len = 0;
		boolean use_default = false;
		if (p_td.json.getActualDefaultValue() != null && 0 == p_tok.get_buffer_length()) {
			operator_assign(p_td.json.getActualDefaultValue());

			return dec_len;
		} else if (p_td.json.getDefault_value() != null && 0 == p_tok.get_buffer_length()) {
			// No JSON data in the buffer -> use default value
			value.append(p_td.json.getDefault_value());
			value_len.set(value.length());
			use_default = true;
		} else {
			dec_len = p_tok.get_next_token(token, value, value_len);
		}

		final String valueStr = value.toString();
		if (json_token_t.JSON_TOKEN_ERROR == token.get()) {
			if(!p_silent) {
				TTCN_EncDec_ErrorContext.error(TTCN_EncDec.error_type.ET_INVAL_MSG, JSON.JSON_DEC_BAD_TOKEN_ERROR, "");
			}
			return JSON.JSON_ERROR_FATAL;
		} else if (json_token_t.JSON_TOKEN_STRING == token.get() || use_default) {
			if ( (use_default ? POS_INF_STR_DEFAULT : POS_INF_STR).equals(valueStr) ) {
				float_value = new Ttcn3Float(Double.POSITIVE_INFINITY);
			} else if ( (use_default ? NEG_INF_STR_DEFAULT : NEG_INF_STR).equals(valueStr) ) {
				float_value = new Ttcn3Float(Double.NEGATIVE_INFINITY);
			} else if ( (use_default ? NAN_STR_DEFAULT : NAN_STR).equals(valueStr) ) {
				float_value = new Ttcn3Float(Double.NaN);
			} else if (!use_default) {
				final String spec_val = MessageFormat.format("float ({0}, {1} or {2})", POS_INF_STR, NEG_INF_STR, NAN_STR);
				if(!p_silent) {
					TTCN_EncDec_ErrorContext.error(TTCN_EncDec.error_type.ET_INVAL_MSG, JSON.JSON_DEC_FORMAT_ERROR, "string", spec_val);
				}
				float_value = null;
				return JSON.JSON_ERROR_FATAL;
			}
		} else if (json_token_t.JSON_TOKEN_NUMBER == token.get()) {
			float_value = new Ttcn3Float(Double.parseDouble(valueStr));
		} else {
			return JSON.JSON_ERROR_INVALID_TOKEN;
		}
		if (!is_bound() && use_default) {
			// Already checked the default value for the string possibilities, now
			// check for a valid number
			float_value = new Ttcn3Float(Double.parseDouble(valueStr));
		}
		return dec_len;
	}
}
