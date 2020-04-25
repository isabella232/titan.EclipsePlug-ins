/******************************************************************************
 * Copyright (c) 2000-2020 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

import java.math.BigInteger;
import java.text.MessageFormat;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.titan.runtime.core.BER.ASN_BERdescriptor;
import org.eclipse.titan.runtime.core.BER.ASN_Tag;
import org.eclipse.titan.runtime.core.BER.ASN_TagClass;
import org.eclipse.titan.runtime.core.JSON.TTCN_JSONdescriptor;
import org.eclipse.titan.runtime.core.JSON.json_string_escaping;
import org.eclipse.titan.runtime.core.JSON_Tokenizer.json_token_t;
import org.eclipse.titan.runtime.core.Param_Types.Module_Param_Integer;
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
 * TTCN-3 integer
 * @author Arpad Lovassy
 * @author Gergo Ujhelyi
 * @author Andrea Palfi
 */
public class TitanInteger extends Base_Type {
	private static final ASN_Tag TitanInteger_tag_[] = new ASN_Tag[] {new ASN_Tag(ASN_TagClass.ASN_TAG_UNIV, 2)};
	public static final ASN_BERdescriptor TitanInteger_Ber_ = new ASN_BERdescriptor(1, TitanInteger_tag_);
	public static final TTCN_RAWdescriptor TitanInteger_raw_ = new TTCN_RAWdescriptor(8, raw_sign_t.SG_NO, raw_order_t.ORDER_LSB, raw_order_t.ORDER_LSB, raw_order_t.ORDER_LSB, raw_order_t.ORDER_LSB, ext_bit_t.EXT_BIT_NO, raw_order_t.ORDER_LSB, raw_order_t.ORDER_LSB, top_bit_order_t.TOP_BIT_INHERITED, 0, 0, 0, 8, 0, null, -1, CharCoding.UNKNOWN, null, false);
	public static final TTCN_JSONdescriptor TitanInteger_json_ = new TTCN_JSONdescriptor(false, null, false, null, false, false, false, 0, null, false, json_string_escaping.ESCAPE_AS_SHORT);
	public static final TTCN_Typedescriptor TitanInteger_descr_ = new TTCN_Typedescriptor("INTEGER", TitanInteger_Ber_, TitanInteger_raw_, TitanInteger_json_, null);

	private boolean boundFlag;

	private boolean nativeFlag;
	private int nativeInt;
	private BigInteger openSSL;

	/**
	 * Initializes to unbound value.
	 * */
	public TitanInteger() {
		boundFlag = false;
		nativeFlag = true;
	}

	/**
	 * Initializes to a given value.
	 *
	 * @param otherValue
	 *                the value to initialize to.
	 * */
	public TitanInteger(final int otherValue) {
		boundFlag = true;
		if( otherValue == Integer.MIN_VALUE) {
			//special case: -MIN_VALUE = MAX_VALUE + 1, so the negated value doesn't fit into int
			nativeFlag = false;
			openSSL = BigInteger.valueOf(otherValue);
		} else {
			nativeFlag = true;
			nativeInt = otherValue;
		}
	}

	/**
	 * Initializes to a given value.
	 *
	 * @param otherValue
	 *                the value to initialize to.
	 * */
	public TitanInteger(final BigInteger otherValue) {
		openSSL = otherValue;
		boundFlag = true;
		nativeFlag = false;
	}

	/**
	 * Initializes to a given value.
	 *
	 * @param otherValue
	 *                the value to initialize to.
	 * */
	public TitanInteger(final TitanInteger otherValue) {
		otherValue.must_bound("Copying an unbound integer value.");

		boundFlag = true;
		nativeFlag = otherValue.nativeFlag;
		if (nativeFlag) {
			nativeInt = otherValue.nativeInt;
		} else {
			openSSL = otherValue.openSSL;
		}
	}

	private boolean from_string(final String otherValue) {
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

	/**
	 * Initializes to a given value.
	 *
	 * @param otherValue
	 *                the value to initialize to.
	 * */
	public TitanInteger(final String otherValue) {
		if (otherValue == null) {
			throw new TtcnError(MessageFormat.format("Unexpected error when converting `{0}'' to integer", otherValue));
		}
		boundFlag = true;
		if (!from_string(otherValue)) {
			throw new TtcnError(MessageFormat.format("Unexpected error when converting `{0}'' to integer", otherValue));
		}
	}

	@Override
	public void clean_up() {
		if (!nativeFlag) {
			openSSL = null;
		}
		boundFlag = false;
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
	public TitanInteger operator_assign(final int otherValue) {
		clean_up();
		boundFlag = true;
		if( otherValue == Integer.MIN_VALUE) {
			//special case: -MIN_VALUE = MAX_VALUE + 1, so the negated value doesn't fit into int
			nativeFlag = false;
			openSSL = BigInteger.valueOf(otherValue);
		} else {
			nativeFlag = true;
			nativeInt = otherValue;
		}

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
	public TitanInteger operator_assign(final BigInteger otherValue) {
		clean_up();
		boundFlag = true;
		nativeFlag = false;
		openSSL = otherValue;

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
	public TitanInteger operator_assign(final TitanInteger otherValue) {
		otherValue.must_bound("Assignment of an unbound integer value.");

		if (otherValue != this) {
			clean_up();
			boundFlag = true;
			nativeFlag = otherValue.nativeFlag;
			if (nativeFlag) {
				nativeInt = otherValue.nativeInt;
			} else {
				openSSL = otherValue.openSSL;
			}
		}

		return this;
	}

	@Override
	public TitanInteger operator_assign(final Base_Type otherValue) {
		if (otherValue instanceof TitanInteger) {
			return operator_assign((TitanInteger)otherValue);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to integer", otherValue));
	}

	/**
	 * Represents the unary operator+.
	 * Creates a copy of the current value.
	 *
	 * operator+ in the core
	 *
	 * @return a copy of the current value.
	 */
	public TitanInteger add() {
		must_bound("Unbound integer operand of unary + operator.");

		if (nativeFlag) {
			return new TitanInteger(nativeInt);
		} else {
			return new TitanInteger(openSSL);
		}
	}

	/**
	 * Negates the current value.
	 *
	 * operator- in the core
	 *
	 * @return the negated value.
	 */
	public TitanInteger sub() {
		must_bound("Unbound integer operand of unary - operator (negation).");

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

	/**
	 * Returns an integer whose value is this + other_value.
	 *
	 * operator+ in the core
	 *
	 * @param other_value
	 *                the other value to use.
	 * @return this + other_value
	 */
	public TitanInteger add(final int other_value) {
		must_bound("Unbound left operand of integer addition.");

		if (nativeFlag) {
			final long temp = (long) nativeInt + (long) other_value;
			if (temp > Integer.MIN_VALUE && temp < Integer.MAX_VALUE) {
				return new TitanInteger((int) temp);
			}
			return new TitanInteger(BigInteger.valueOf(temp));
		} else {
			final BigInteger other_int = BigInteger.valueOf(other_value);
			return new TitanInteger(openSSL.add(other_int));
		}
	}

	/**
	 * Returns an integer whose value is this + other_value.
	 *
	 * operator+ in the core
	 *
	 * @param other_value
	 *                the other value to use.
	 * @return this + other_value
	 */
	public TitanInteger add(final BigInteger other_value) {
		must_bound("Unbound left operand of integer addition.");

		if (nativeFlag) {
			final BigInteger this_int = BigInteger.valueOf(nativeInt);
			return new TitanInteger(this_int.add(other_value));
		} else {
			return new TitanInteger(openSSL.add(other_value));
		}
	}

	/**
	 * Returns an integer whose value is this + other_value.
	 *
	 * operator+ in the core
	 *
	 * @param other_value
	 *                the other value to use.
	 * @return this + other_value
	 */
	public TitanInteger add(final TitanInteger other_value) {
		must_bound("Unbound left operand of integer addition.");
		other_value.must_bound("Unbound right operand of integer addition.");

		if (nativeFlag) {
			if (other_value.nativeFlag) {
				final long temp = (long) nativeInt + (long) other_value.nativeInt;
				if (temp > Integer.MIN_VALUE && temp < Integer.MAX_VALUE) {
					return new TitanInteger((int) temp);
				}
				return new TitanInteger(BigInteger.valueOf(temp));
			} else {
				final BigInteger this_int = BigInteger.valueOf(nativeInt);
				return new TitanInteger(this_int.add(other_value.openSSL));
			}
		} else {
			if (other_value.nativeFlag) {
				final BigInteger other_int = BigInteger.valueOf(other_value.nativeInt);
				return new TitanInteger(openSSL.add(other_int));
			} else {
				return new TitanInteger(openSSL.add(other_value.openSSL));
			}
		}
	}

	/**
	 * Returns an integer whose value is this - other_value.
	 *
	 * operator- in the core
	 *
	 * @param other_value
	 *                the other value to use.
	 * @return this - other_value
	 */
	public TitanInteger sub(final int other_value) {
		this.must_bound("Unbound left operand of integer subtraction. ");

		if (nativeFlag) {
			final long temp = (long) nativeInt - (long) other_value;
			if (temp > Integer.MIN_VALUE && temp < Integer.MAX_VALUE) {
				return new TitanInteger((int) temp);
			}
			return new TitanInteger(BigInteger.valueOf(temp));
		} else {
			final BigInteger other_int = BigInteger.valueOf(other_value);
			return new TitanInteger(openSSL.subtract(other_int));
		}
	}

	/**
	 * Returns an integer whose value is this - other_value.
	 *
	 * operator- in the core
	 *
	 * @param other_value
	 *                the other value to use.
	 * @return this - other_value
	 */
	public TitanInteger sub(final BigInteger other_value) {
		this.must_bound("Unbound left operand of integer subtraction. ");

		if (nativeFlag) {
			final BigInteger this_int = BigInteger.valueOf(nativeInt);
			return new TitanInteger(this_int.subtract(other_value));
		} else {
			return new TitanInteger(openSSL.subtract(other_value));
		}
	}

	/**
	 * Returns an integer whose value is this - other_value.
	 *
	 * operator- in the core
	 *
	 * @param other_value
	 *                the other value to use.
	 * @return this - other_value
	 */
	public TitanInteger sub(final TitanInteger other_value) {
		this.must_bound("Unbound left operand of integer subtraction. ");
		other_value.must_bound("Unbound right operand of integer subtraction. ");

		if (nativeFlag) {
			if (other_value.nativeFlag) {
				final long temp = (long) nativeInt - (long) other_value.nativeInt;
				if (temp > Integer.MIN_VALUE && temp < Integer.MAX_VALUE) {
					return new TitanInteger((int) temp);
				}
				return new TitanInteger(BigInteger.valueOf(temp));
			} else {
				final BigInteger this_int = BigInteger.valueOf(nativeInt);
				return new TitanInteger(this_int.subtract(other_value.openSSL));
			}
		} else {
			if (other_value.nativeFlag) {
				final BigInteger other_int = BigInteger.valueOf(other_value.nativeInt);
				return new TitanInteger(openSSL.subtract(other_int));
			} else {
				return new TitanInteger(openSSL.subtract(other_value.openSSL));
			}
		}
	}

	/**
	 * Returns an integer whose value is this * other_value.
	 *
	 * operator* in the core
	 *
	 * @param other_value
	 *                the other value to use.
	 * @return this * other_value
	 */
	public TitanInteger mul(final int other_value) {
		must_bound("Unbound left operand of integer multiplication.");

		if (nativeFlag && nativeInt == 0 || other_value == 0) {
			return new TitanInteger((int) 0);
		}

		if (nativeFlag) {
			final long temp = (long) nativeInt * (long) other_value;
			if (temp > Integer.MIN_VALUE && temp < Integer.MAX_VALUE) {
				return new TitanInteger((int) temp);
			}
			return new TitanInteger(BigInteger.valueOf(temp));
		} else {
			final BigInteger other_int = BigInteger.valueOf(other_value);
			return new TitanInteger(openSSL.multiply(other_int));
		}
	}

	/**
	 * Returns an integer whose value is this * other_value.
	 *
	 * operator* in the core
	 *
	 * @param other_value
	 *                the other value to use.
	 * @return this * other_value
	 */
	public TitanInteger mul(final BigInteger other_value) {
		must_bound("Unbound left operand of integer multiplication.");

		if (nativeFlag && nativeInt == 0) {
			return new TitanInteger((int) 0);
		}

		if (nativeFlag) {
			final BigInteger this_int = BigInteger.valueOf(nativeInt);
			return new TitanInteger(this_int.multiply(other_value));
		} else {
			return new TitanInteger(openSSL.multiply(other_value));
		}
	}

	/**
	 * Returns an integer whose value is this * other_value.
	 *
	 * operator* in the core
	 *
	 * @param other_value
	 *                the other value to use.
	 * @return this * other_value
	 */
	public TitanInteger mul(final TitanInteger other_value) {
		must_bound("Unbound left operand of integer multiplication.");
		other_value.must_bound("Unbound right operand of integer multiplication.");

		if (nativeFlag && nativeInt == 0 || (other_value.nativeFlag && other_value.nativeInt == 0)) {
			return new TitanInteger((int) 0);
		}

		if (nativeFlag) {
			if (other_value.nativeFlag) {
				final long temp = (long) nativeInt * (long) other_value.nativeInt;
				if (temp > Integer.MIN_VALUE && temp < Integer.MAX_VALUE) {
					return new TitanInteger((int) temp);
				}
				return new TitanInteger(BigInteger.valueOf(temp));
			} else {
				final BigInteger this_int = BigInteger.valueOf(nativeInt);
				return new TitanInteger(this_int.multiply(other_value.openSSL));
			}
		} else {
			if (other_value.nativeFlag) {
				final BigInteger other_int = BigInteger.valueOf(other_value.nativeInt);
				return new TitanInteger(openSSL.multiply(other_int));
			} else {
				return new TitanInteger(openSSL.multiply(other_value.openSSL));
			}
		}
	}

	/**
	 * Returns an integer whose value is this / other_value.
	 *
	 * operator/ in the core
	 *
	 * @param other_value
	 *                the other value to use.
	 * @return this / other_value
	 */
	public TitanInteger div(final int other_value) {
		must_bound("Unbound left operand of integer division.");

		if (other_value == 0) {
			throw new TtcnError("Integer division by zero.");
		}
		if (nativeFlag && nativeInt == 0) {
			return new TitanInteger(0);
		}

		if (nativeFlag) {
			final long temp = (long) nativeInt / (long) other_value;
			if (temp > Integer.MIN_VALUE && temp < Integer.MAX_VALUE) {
				return new TitanInteger((int) temp);
			} else {
				return new TitanInteger(BigInteger.valueOf(temp));
			}
		} else {
			final BigInteger other_value_int = BigInteger.valueOf(other_value);
			return new TitanInteger(openSSL.divide(other_value_int));
		}
	}

	/**
	 * Returns an integer whose value is this / other_value.
	 *
	 * operator/ in the core
	 *
	 * @param other_value
	 *                the other value to use.
	 * @return this / other_value
	 */
	public TitanInteger div(final BigInteger other_value) {
		must_bound("Unbound left operand of integer division.");

		if (other_value.equals(BigInteger.ZERO)) {
			throw new TtcnError("Integer division by zero.");
		}
		if (nativeFlag && nativeInt == 0) {
			return new TitanInteger(0);
		}

		if (nativeFlag) {
			final BigInteger this_int = BigInteger.valueOf(nativeInt);
			return new TitanInteger(this_int.divide(other_value));
		} else {
			return new TitanInteger(openSSL.divide(other_value));
		}
	}

	/**
	 * Returns an integer whose value is this / other_value.
	 *
	 * operator/ in the core
	 *
	 * @param other_value
	 *                the other value to use.
	 * @return this / other_value
	 */
	public TitanInteger div(final TitanInteger other_value) {
		must_bound("Unbound left operand of integer division.");
		other_value.must_bound("Unbound right operand of integer division.");

		if (other_value.operator_equals(0)) {
			throw new TtcnError("Integer division by zero.");
		}
		if (nativeFlag && nativeInt == 0) {
			return new TitanInteger(0);
		}

		if (nativeFlag) {
			if (other_value.nativeFlag) {
				final long temp = (long) nativeInt / (long) other_value.nativeInt;
				if (temp > Integer.MIN_VALUE && temp < Integer.MAX_VALUE) {
					return new TitanInteger((int) temp);
				} else {
					return new TitanInteger(BigInteger.valueOf(temp));
				}
			} else {
				final BigInteger this_int = BigInteger.valueOf(nativeInt);
				return new TitanInteger(this_int.divide(other_value.openSSL));
			}
		} else {
			if (other_value.nativeFlag) {
				final BigInteger other_value_int = BigInteger.valueOf(other_value.nativeInt);
				return new TitanInteger(openSSL.divide(other_value_int));
			} else {
				return new TitanInteger(openSSL.divide(other_value.openSSL));
			}
		}
	}

	/**
	 * Returns the remainder after the division this / other_value.
	 *
	 * rem in the core
	 *
	 * @param other_value
	 *                the other value to use.
	 * @return the remainder
	 */
	public TitanInteger rem(final int other_value) {
		must_bound("Unbound left operand of rem operator ");

		if (other_value == 0) {
			throw new TtcnError("The right operand of rem operator is zero.");
		}

		if (nativeFlag) {
			return new TitanInteger(nativeInt - other_value * (nativeInt / other_value));
		} else {
			return new TitanInteger(openSSL.remainder(BigInteger.valueOf(other_value)));
		}
	}

	/**
	 * Returns the remainder after the division this / other_value.
	 *
	 * rem in the core
	 *
	 * @param other_value
	 *                the other value to use.
	 * @return the remainder
	 */
	public TitanInteger rem(final BigInteger other_value) {
		must_bound("Unbound left operand of rem operator ");

		if (other_value.equals(BigInteger.ZERO)) {
			throw new TtcnError("The right operand of rem operator is zero.");
		}

		if (nativeFlag) {
			return new TitanInteger(BigInteger.valueOf(nativeInt).remainder(other_value));
		} else {
			return new TitanInteger(openSSL.remainder(other_value));
		}
	}

	/**
	 * Returns the remainder after the division this / other_value.
	 *
	 * rem in the core
	 *
	 * @param other_value
	 *                the other value to use.
	 * @return the remainder
	 */
	public TitanInteger rem(final TitanInteger other_value) {
		must_bound("Unbound left operand of rem operator ");
		other_value.must_bound("Unbound right operand of rem operator");

		return this.sub(other_value.mul(this.div(other_value)));
	}


	/**
	 * Returns the modulo after the division this / other_value.
	 *
	 * @param other_value
	 *                the other value to use.
	 * @return the modulo
	 */
	public TitanInteger mod(final int other_value) {
		must_bound("Unbound left operand of mod operator.");

		int rightValueAbs = other_value;
		if (other_value < 0) {
			rightValueAbs = -1 * other_value;
		} else if (other_value == 0) {
			throw new TtcnError("The right operand of mod operator is zero");
		}
		if (is_greater_than(0)) {
			return rem(other_value);
		} else {
			final TitanInteger result = rem(rightValueAbs);
			if (result.operator_equals(0)) {
				return result;
			} else {
				return result.add(rightValueAbs);
			}
		}
	}

	/**
	 * Returns the modulo after the division this / other_value.
	 *
	 * @param other_value
	 *                the other value to use.
	 * @return the modulo
	 */
	public TitanInteger mod(final BigInteger other_value) {
		must_bound("Unbound left operand of mod operator.");

		BigInteger rightValueAbs = other_value;
		final int comparision = other_value.compareTo(BigInteger.ZERO);
		if (comparision == -1) {
			rightValueAbs = rightValueAbs.negate();
		} else if (comparision == 0) {
			throw new TtcnError("The right operand of mod operator is zero");
		}
		if (is_greater_than(0)) {
			return rem(other_value);
		} else {
			final TitanInteger result = rem(rightValueAbs);
			if (result.operator_equals(0)) {
				return result;
			} else {
				return result.add(rightValueAbs);
			}
		}
	}

	/**
	 * Returns the modulo after the division this / other_value.
	 *
	 * @param other_value
	 *                the other value to use.
	 * @return the modulo
	 */
	public TitanInteger mod(final TitanInteger other_value) {
		must_bound("Unbound left operand of mod operator.");
		other_value.must_bound("Unbound right operand of mod operator");

		TitanInteger rightValueAbs = new TitanInteger(other_value);
		if (other_value.is_less_than(0)) {
			rightValueAbs = rightValueAbs.mul(-1);
		} else if (other_value.operator_equals(0)) {
			throw new TtcnError("The right operand of mod operator is zero");
		}
		if (is_greater_than(0)) {
			return rem(other_value);
		} else {
			final TitanInteger result = rem(rightValueAbs);
			if (result.operator_equals(0)) {
				return new TitanInteger(0);
			} else {
				return new TitanInteger(rightValueAbs.add(result));
			}
		}
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
	public boolean operator_equals(final int otherValue) {
		must_bound("Unbound left operand of integer comparison.");

		if (nativeFlag) {
			return nativeInt == otherValue;
		}

		final BigInteger other_int = BigInteger.valueOf(otherValue);
		return openSSL.equals(other_int);
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
	public boolean operator_equals(final BigInteger otherValue) {
		must_bound("Unbound left operand of integer comparison.");

		if (!nativeFlag) {
			return openSSL.equals(otherValue);
		}

		final BigInteger local_int = BigInteger.valueOf(nativeInt);
		return local_int.equals(otherValue);
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
	public boolean operator_equals(final TitanInteger otherValue) {
		must_bound("Unbound left operand of integer comparison.");
		otherValue.must_bound("Unbound right operand of integer comparison.");

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
	public boolean operator_equals(final Base_Type otherValue) {
		if (otherValue instanceof TitanInteger) {
			return operator_equals((TitanInteger) otherValue);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to integer", otherValue));
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
	public boolean operator_not_equals(final int otherValue) {
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
	public boolean operator_not_equals(final BigInteger otherValue) {
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
	public boolean operator_not_equals(final TitanInteger otherValue) {
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
	public boolean is_less_than(final int otherValue) {
		must_bound("Unbound left operand of integer comparison.");

		if (nativeFlag) {
			return nativeInt < otherValue;
		} else {
			final BigInteger other_int = BigInteger.valueOf(otherValue);
			return -1 == openSSL.compareTo(other_int);
		}
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
	public boolean is_less_than(final BigInteger otherValue) {
		must_bound("Unbound left operand of integer comparison.");

		if (nativeFlag) {
			final BigInteger this_int = BigInteger.valueOf(nativeInt);
			return -1 == this_int.compareTo(otherValue);
		} else {
			return -1 == openSSL.compareTo(otherValue);
		}
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
	public boolean is_less_than(final TitanInteger otherValue) {
		must_bound("Unbound left operand of integer comparison.");
		otherValue.must_bound("Unbound right operand of integer comparison.");

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

	/**
	 * Checks if the current value is greater than the provided one.
	 *
	 * operator> in the core
	 *
	 * @param otherValue
	 *                the other value to check against.
	 * @return {@code true} if the value is greater than the provided.
	 */
	public boolean is_greater_than(final int otherValue) {
		must_bound("Unbound left operand of integer comparison.");

		if (nativeFlag) {
			return nativeInt > otherValue;
		} else {
			final BigInteger other_int = BigInteger.valueOf(otherValue);
			return 1 == openSSL.compareTo(other_int);

		}
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
	public boolean is_greater_than(final BigInteger otherValue) {
		must_bound("Unbound left operand of integer comparison.");

		if (nativeFlag) {
			final BigInteger this_int = BigInteger.valueOf(nativeInt);
			return 1 == this_int.compareTo(otherValue);
		} else {
			return 1 == openSSL.compareTo(otherValue);

		}
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
	public boolean is_greater_than(final TitanInteger otherValue) {
		must_bound("Unbound left operand of integer comparison.");
		otherValue.must_bound("Unbound right operand of integer comparison.");

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

	/**
	 * Checks if the current value is less than or equivalent to the provided one.
	 *
	 * operator<= in the core
	 *
	 * @param otherValue
	 *                the other value to check against.
	 * @return {@code true} if the value is less than or equivalent to the provided.
	 */
	public boolean is_less_than_or_equal(final int otherValue) {
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
	public boolean is_less_than_or_equal(final BigInteger otherValue) {
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
	public boolean is_less_than_or_equal(final TitanInteger otherValue) {
		return !is_greater_than(otherValue);
	}

	/**
	 * Checks if the current value is greater than or equivalent to the provided one.
	 *
	 * operator>= in the core
	 *
	 * @param otherValue
	 *                the other value to check against.
	 * @return {@code true} if the value is greater than or equivalent to the provided.
	 */
	public boolean is_greater_than_or_equal(final int otherValue) {
		return !is_less_than(otherValue);
	}

	/**
	 * Checks if the current value is greater than or equivalent to the provided one.
	 *
	 * operator>= in the core
	 *
	 * @param otherValue
	 *                the other value to check against.
	 * @return {@code true} if the value is greater than or equivalent to the provided.
	 */
	public boolean is_greater_than_or_equal(final BigInteger otherValue) {
		return !is_less_than(otherValue);
	}

	/**
	 * Checks if the current value is greater than or equivalent to the provided one.
	 *
	 * operator>= in the core
	 *
	 * @param otherValue
	 *                the other value to check against.
	 * @return {@code true} if the value is greater than or equivalent to the provided.
	 */
	public boolean is_greater_than_or_equal(final TitanInteger otherValue) {
		return !is_less_than(otherValue);
	}

	/**
	 * Checks if the integer fits into the native range.
	 *
	 * @return {@code true} if it can be stored in int, {@code false}
	 *         otherwise.
	 * */
	public boolean is_native() {
		return nativeFlag;
	}

	@Override
	public boolean is_bound() {
		return boundFlag;
	}

	@Override
	public boolean is_present() {
		return is_bound();
	}

	@Override
	public boolean is_value() {
		return boundFlag;
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
		if (!boundFlag) {
			return "<unbound>";
		}
		return get_BigInteger().toString();
	}

	@Override
	public void log() {
		if (boundFlag) {
			if (nativeFlag) {
				TTCN_Logger.log_event("%d", nativeInt);
			} else {
				TTCN_Logger.log_event("%s", openSSL.toString());
			}
		} else {
			TTCN_Logger.log_event_unbound();
		}
	}

	/**
	 * Returns the value as an int.
	 * <p>
	 * A current value larger than the native int range results in dynamic
	 * testcase error.
	 *
	 * int() in the core.
	 *
	 * @return the native int value.
	 * */
	public int get_int() {
		must_bound("Using the value of an unbound integer variable.");

		if (!nativeFlag) {
			throw new TtcnError("Invalid conversion of a large integer value.");
		}

		return nativeInt;
	}

	/**
	 * Returns the value as a long.
	 * <p>
	 * A current value larger than the native int range results in dynamic
	 * testcase error.
	 *
	 * get_long_long_val() in the core.
	 *
	 * @return the native long value.
	 * */
	public long get_long() {
		must_bound("Using the value of an unbound integer variable.");

		if (nativeFlag) {
			return nativeInt;
		}

		return openSSL.longValue();
	}

	/**
	 * Returns the value as a BigInteger.
	 *
	 * get_long_long_val() in the core.
	 *
	 * @return the value as a BigInteger.
	 * */
	public BigInteger get_BigInteger() {
		must_bound("Using the value of an unbound integer variable.");

		if (nativeFlag) {
			return BigInteger.valueOf(nativeInt);
		}

		return openSSL;
	}

	@Override
	/** {@inheritDoc} */
	public void encode_text(final Text_Buf text_buf) {
		must_bound("Text encoder: Encoding an unbound integer value.");

		text_buf.push_int(this);
	}

	@Override
	/** {@inheritDoc} */
	public void decode_text(final Text_Buf text_buf) {
		operator_assign(text_buf.pull_int());
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
				JSON_Tokenizer tok = new JSON_Tokenizer(flavour != 0);
				JSON_encode(p_td, tok);
				p_buf.put_s(tok.get_buffer().toString().getBytes());
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
				JSON_Tokenizer tok = new JSON_Tokenizer(new String(p_buf.get_data()), p_buf.get_len());
				if(JSON_decode(p_td, tok, false) < 0) {
					TTCN_EncDec_ErrorContext.error(error_type.ET_INCOMPL_MSG,
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
	public int JSON_encode(final TTCN_Typedescriptor p_td, final JSON_Tokenizer p_tok, final boolean p_parent_is_map) {
		if (!is_bound()) {
			TTCN_EncDec_ErrorContext.error(TTCN_EncDec.error_type.ET_UNBOUND,"Encoding an unbound integer value.");
			return -1;
		}

		final StringBuilder tmp_str = new StringBuilder();
		if (nativeFlag) {
			tmp_str.append(nativeInt);
		} else {
			tmp_str.append(openSSL.toString());
		}

		final int enc_len = p_tok.put_next_token(json_token_t.JSON_TOKEN_NUMBER, tmp_str.toString());
		return enc_len;
	}

	@Override
	/** {@inheritDoc} */
	public int JSON_decode(final TTCN_Typedescriptor p_td, final JSON_Tokenizer p_tok, final boolean p_silent, final boolean p_parent_is_map, final int p_chosen_field) {
		final AtomicReference<json_token_t> token = new AtomicReference<json_token_t>(json_token_t.JSON_TOKEN_NONE);
		final StringBuilder value = new StringBuilder();
		final AtomicInteger value_len = new AtomicInteger(0);
		int dec_len = 0;
		boolean use_default = p_td.json.getDefault_value() != null && 0 == p_tok.get_buffer_length();
		if (use_default) {
			// No JSON data in the buffer -> use default value
			value.setLength(0);
			value.append( p_td.json.getDefault_value() );
			value_len.set(value.length());
		} else {
			dec_len = p_tok.get_next_token(token, value, value_len);
		}
		if (json_token_t.JSON_TOKEN_ERROR == token.get()) {
			if(!p_silent) {
				TTCN_EncDec_ErrorContext.error(TTCN_EncDec.error_type.ET_INVAL_MSG, JSON.JSON_DEC_BAD_TOKEN_ERROR, "");
			}
			return JSON.JSON_ERROR_FATAL;
		} else if (json_token_t.JSON_TOKEN_NUMBER == token.get() || use_default) {
			if (from_string(value.substring(0,value_len.get())) && value_len.get() == get_nof_digits() + ('-' == value.charAt(0) ? 1 : 0)) {
				boundFlag = true;
			} else {
				if(!p_silent) {
					TTCN_EncDec_ErrorContext.error(TTCN_EncDec.error_type.ET_INVAL_MSG, JSON.JSON_DEC_FORMAT_ERROR, "number", "integer");
				}
				boundFlag = false;
				dec_len = JSON.JSON_ERROR_FATAL;
			}
		} else {
			boundFlag = false;
			return JSON.JSON_ERROR_INVALID_TOKEN;
		}
		return dec_len;
	}

	private int get_nof_digits() {
		int digits = 0;
		if (nativeFlag) {
			int x = nativeInt;
			if (x == 0) {
				return 1;
			}
			if (x < 0) {
				x = -x;
			}
			while (x > 0) {
				++digits;
				x /= 10;
			}
		} else {
			BigInteger x = openSSL;
			if (x.compareTo(BigInteger.ZERO) == 0) {
				return 1;
			}
			if (x.compareTo(BigInteger.ZERO) < 0) {
				x = x.negate();
			}
			while (x.compareTo(BigInteger.ZERO) > 0) {
				++digits;
				x = x.divide(BigInteger.TEN);
			}
		}
		return digits;
	}

	/**
	 * Returns an integer whose value is int_value + other_value.
	 *
	 * static operator+ in the core
	 *
	 * @param int_value
	 *                the first value to use.
	 * @param other_value
	 *                the other value to use.
	 * @return int_value + other_value
	 */
	public static TitanInteger add(final int int_value, final TitanInteger other_value) {
		other_value.must_bound("Unbound right operand of integer addition.");

		if (other_value.nativeFlag) {
			final long temp = (long) int_value + (long) other_value.nativeInt;
			if (temp > Integer.MIN_VALUE && temp < Integer.MAX_VALUE) {
				return new TitanInteger((int) temp);
			} else {
				return new TitanInteger(BigInteger.valueOf(temp));
			}
		} else {
			final BigInteger first_int = BigInteger.valueOf(int_value);
			return new TitanInteger(first_int.add(other_value.openSSL));
		}
	}

	/**
	 * Returns an integer whose value is int_value - other_value.
	 *
	 * static operator- in the core
	 *
	 * @param int_value
	 *                the first value to use.
	 * @param other_value
	 *                the other value to use.
	 * @return int_value - other_value
	 */
	public static TitanInteger sub(final int int_value, final TitanInteger other_value) {
		other_value.must_bound("Unbound right operand of integer subtraction.");

		if (other_value.nativeFlag) {
			final long temp = (long) int_value - (long) other_value.nativeInt;
			if (temp > Integer.MIN_VALUE && temp < Integer.MAX_VALUE) {
				return new TitanInteger((int) temp);
			} else {
				return new TitanInteger(BigInteger.valueOf(temp));
			}
		} else {
			final BigInteger first_int = BigInteger.valueOf(int_value);
			return new TitanInteger(first_int.subtract(other_value.openSSL));
		}
	}

	/**
	 * Returns an integer whose value is int_value * other_value.
	 *
	 * static operator* in the core
	 *
	 * @param int_value
	 *                the first value to use.
	 * @param other_value
	 *                the other value to use.
	 * @return int_value * other_value
	 */
	public static TitanInteger mul(final int int_value, final TitanInteger other_value) {
		return new TitanInteger(int_value).mul(other_value);
	}

	/**
	 * Returns an integer whose value is int_value / other_value.
	 *
	 * static operator/ in the core
	 *
	 * @param int_value
	 *                the first value to use.
	 * @param other_value
	 *                the other value to use.
	 * @return int_value / other_value
	 */
	public static TitanInteger div(final int int_value, final TitanInteger other_value) {
		return new TitanInteger(int_value).div(other_value);
	}

	/**
	 * Returns an integer whose value is the remainder of left_value / right_value.
	 *
	 * static rem in the core
	 *
	 * @param left_value
	 *                the first value to use.
	 * @param right_value
	 *                the other value to use.
	 * @return the remainder of left_value / right_value
	 */
	public static TitanInteger rem(final int left_value, final int right_value) {
		if (right_value == 0) {
			throw new TtcnError("The right operand of rem operator is zero.");
		}

		return new TitanInteger(left_value - right_value * (left_value / right_value));
	}

	/**
	 * Returns an integer whose value is the remainder of left_value / right_value.
	 *
	 * static rem in the core
	 *
	 * @param left_value
	 *                the first value to use.
	 * @param right_value
	 *                the other value to use.
	 * @return the remainder of left_value / right_value
	 */
	public static TitanInteger rem(final TitanInteger left_value, final TitanInteger right_value) {
		left_value.must_bound("Unbound left operand of rem operator ");
		right_value.must_bound("Unbound right operand of rem operator");

		return left_value.sub(right_value.mul((left_value.div(right_value))));
	}

	/**
	 * Returns an integer whose value is the remainder of left_value / right_value.
	 *
	 * static rem in the core
	 *
	 * @param left_value
	 *                the first value to use.
	 * @param right_value
	 *                the other value to use.
	 * @return the remainder of left_value / right_value
	 */
	public static TitanInteger rem(final TitanInteger left_value, final int right_value) {
		return left_value.rem(right_value);
	}

	/**
	 * Returns an integer whose value is the remainder of left_value / right_value.
	 *
	 * static rem in the core
	 *
	 * @param left_value
	 *                the first value to use.
	 * @param right_value
	 *                the other value to use.
	 * @return the remainder of left_value / right_value
	 */
	public static TitanInteger rem(final int left_value, final TitanInteger right_value) {
		return rem(new TitanInteger(left_value), right_value);
	}

	/**
	 * Returns an integer whose value is the modulo of left_value / right_value.
	 *
	 * static mod in the core
	 *
	 * @param left_value
	 *                the first value to use.
	 * @param right_value
	 *                the other value to use.
	 * @return the modulo of left_value / right_value
	 */
	public static TitanInteger mod(final int left_value, int right_value) {
		if (right_value < 0) {
			right_value = right_value * (-1);
		} else if (right_value == 0) {
			throw new TtcnError("The right operand of mod operator is zero.");
		}
		if (left_value > 0) {
			return rem(left_value, right_value);
		} else {
			final int result = rem(left_value, right_value).nativeInt;
			if (result == 0) {
				return new TitanInteger(0);
			} else {
				return new TitanInteger(right_value + result);
			}
		}
	}

	/**
	 * Returns an integer whose value is the modulo of left_value / right_value.
	 *
	 * static mod in the core
	 *
	 * @param left_value
	 *                the first value to use.
	 * @param right_value
	 *                the other value to use.
	 * @return the modulo of left_value / right_value
	 */
	public static TitanInteger mod(final TitanInteger left_value, final TitanInteger right_value) {
		left_value.must_bound("Unbound left operand of mod operator.");
		right_value.must_bound("Unbound right operand of mod operator");

		TitanInteger rightValueAbs = new TitanInteger(right_value);
		if (right_value.is_less_than(0)) {
			rightValueAbs = rightValueAbs.mul(-1);
		} else if (right_value.operator_equals(0)) {
			throw new TtcnError("The right operand of mod operator is zero");
		}
		if (left_value.is_greater_than(0)) {
			return rem(left_value, right_value);
		} else {
			final TitanInteger result = rem(left_value, rightValueAbs);
			if (result.operator_equals(0)) {
				return new TitanInteger(0);
			} else {
				return rightValueAbs.add(result);
			}
		}
	}

	/**
	 * Returns an integer whose value is the modulo of left_value / right_value.
	 *
	 * static mod in the core
	 *
	 * @param left_value
	 *                the first value to use.
	 * @param right_value
	 *                the other value to use.
	 * @return the modulo of left_value / right_value
	 */
	public static TitanInteger mod(final TitanInteger left_value, final int right_value) {
		return left_value.mod(right_value);
	}

	/**
	 * Returns an integer whose value is the modulo of left_value / right_value.
	 *
	 * static mod in the core
	 *
	 * @param left_value
	 *                the first value to use.
	 * @param right_value
	 *                the other value to use.
	 * @return the modulo of left_value / right_value
	 */
	public static TitanInteger mod(final int left_value, final TitanInteger right_value) {
		return mod(new TitanInteger(left_value), right_value);
	}

	/**
	 * Checks if the first value is equivalent to the second one.
	 *
	 * static operator== in the core
	 *
	 * @param intValue
	 *                the first value.
	 * @param otherValue
	 *                the second value to check against.
	 * @return {@code true} if the values are equivalent.
	 */
	public static boolean operator_equals(final int intValue, final TitanInteger otherValue) {
		return new TitanInteger(intValue).operator_equals(otherValue);
	}

	/**
	 * Checks if the first value is not equivalent to the second one.
	 *
	 * static operator!= in the core
	 *
	 * @param intValue
	 *                the first value.
	 * @param otherValue
	 *                the second value to check against.
	 * @return {@code true} if the values are not equivalent.
	 */
	public static boolean operator_not_equals(final int intValue, final TitanInteger otherValue) {
		return new TitanInteger(intValue).operator_not_equals(otherValue);
	}

	/**
	 * Checks if the first value is less than the second one.
	 *
	 * static operator< in the core
	 *
	 * @param intValue
	 *                the first value.
	 * @param otherValue
	 *                the second value to check against.
	 * @return {@code true} if the first value is less than the second.
	 */
	public static boolean is_less_than(final int intValue, final TitanInteger otherValue) {
		return new TitanInteger(intValue).is_less_than(otherValue);
	}

	/**
	 * Checks if the first value is greater than the second one.
	 *
	 * static operator> in the core
	 *
	 * @param intValue
	 *                the first value.
	 * @param otherValue
	 *                the second value to check against.
	 * @return {@code true} if the first value is greater than the second.
	 */
	public static boolean is_greater_than(final int intValue, final TitanInteger otherValue) {
		return new TitanInteger(intValue).is_greater_than(otherValue);
	}

	/**
	 * Checks if the first value is less than or equal to the second one.
	 *
	 * static operator<= in the core
	 *
	 * @param intValue
	 *                the first value.
	 * @param otherValue
	 *                the second value to check against.
	 * @return {@code true} if the first value is less than or equal to the
	 *         second.
	 */
	public static boolean is_less_than_or_equal(final int intValue, final TitanInteger otherValue) {
		return new TitanInteger(intValue).is_less_than_or_equal(otherValue);
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
	public static boolean is_greater_than_or_equal(final int intValue, final TitanInteger otherValue) {
		return new TitanInteger(intValue).is_greater_than_or_equal(otherValue);
	}

	/** Encodes the value of the variable according to the
	 * TTCN_Typedescriptor_t.  It must be public because called by
	 * another types during encoding.  Returns the length of encoded data.  */

	public static final int INTX_MASKS[] = { 0 /* dummy */, 0x01, 0x03, 0x07, 0x0F, 0x1F, 0x3F, 0x7F, 0xFF };

	@Override
	/** {@inheritDoc} */
	public int RAW_encode(final TTCN_Typedescriptor p_td, final RAW_enc_tree myleaf) {
		if (!nativeFlag) {
			return RAW_encode_openssl(p_td, myleaf);
		}

		final TTCN_EncDec_ErrorContext errorContext = new TTCN_EncDec_ErrorContext();
		try {
			int value = get_int();
			boolean neg_sgbit = (value < 0) && (p_td.raw.comp == raw_sign_t.SG_SG_BIT);
			if (!is_bound()) {
				TTCN_EncDec_ErrorContext.error(TTCN_EncDec.error_type.ET_UNBOUND, "Encoding an unbound value.");
				value = 0;
				neg_sgbit = false;
			}

			if (value == Integer.MIN_VALUE) {
				final TitanInteger big_value = new TitanInteger(BigInteger.valueOf(value));

				return big_value.RAW_encode_openssl(p_td, myleaf);
			}

			if ((value < 0) && (p_td.raw.comp == raw_sign_t.SG_NO)) {
				TTCN_EncDec_ErrorContext.error(TTCN_EncDec.error_type.ET_SIGN_ERR, "Unsigned encoding of a negative number: %s", p_td.name);
				value = -value;
			}
			if (neg_sgbit) {
				value = -value;
			}

			int length; // total length, in bytes
			int val_bits = 0; // only for IntX
			int len_bits = 0; // only for IntX
			if (p_td.raw.fieldlength == RAW.RAW_INTX) { // IntX (variable length)
				val_bits = (p_td.raw.comp != raw_sign_t.SG_NO) ? 1 : 0; // bits needed to store the value
				int v2 = value;
				if (v2 < 0 && p_td.raw.comp == raw_sign_t.SG_2COMPL) {
					v2 = ~v2;
				}
				do {
					v2 >>= 1;
				++val_bits;
				} while (v2 != 0);
				len_bits = 1 + val_bits / 8; // bits needed to store the length
				if (val_bits % 8 + len_bits % 8 > 8) {
					// the remainder of the value bits and the length bits do not fit into
					// an octet => an extra octet is needed and the length must be increased
					++len_bits;
				}
				length = (len_bits + val_bits + 7) / 8;
				if (len_bits % 8 == 0 && val_bits % 8 != 0) {
					// special case: the value can be stored on 8k - 1 octets plus the partial octet
					// - len_bits = 8k is not enough, since there's no partial octet in that case
					// and the length would then be followed by 8k octets (and it only indicates
					// 8k - 1 further octets)
					// - len_bits = 8k + 1 is too much, since there are only 8k - 1 octets
					// following the partial octet (and 8k are indicated)
					// solution: len_bits = 8k + 1 and insert an extra empty octet
					++len_bits;
					++length;
				}
			} else { // not IntX, use the field length
				length = (p_td.raw.fieldlength + 7) / 8;
				int min_bits = RAW.min_bits(value);
				if(p_td.raw.comp == raw_sign_t.SG_SG_BIT) {
					min_bits++;
				}
				if (min_bits > p_td.raw.fieldlength) {
					TTCN_EncDec_ErrorContext.error(error_type.ET_LEN_ERR, "There are insufficient bits to encode '%s': ", p_td.name);
					value = 0; // substitute with zero
				}
			}

			final byte bc[] = new byte[length];
			myleaf.data_array = bc;
			if (p_td.raw.fieldlength == RAW.RAW_INTX) {
				int i = 0;
				// treat the empty space between the value and the length as if it was part
				// of the value, too
				val_bits = length * 8 - len_bits;
				// first, encode the value
				do {
					bc[i] = (byte) (value & INTX_MASKS[val_bits > 8 ? 8 : val_bits]);
					++i;
					value >>= 8;
					val_bits -= 8;
				} while (val_bits > 0);
				if (neg_sgbit) {
					// the sign bit is the first bit after the length
					final char mask = (char)(0x80 >> len_bits % 8);
					bc[i - 1] |= mask;
				}
				// second, encode the length (ignore the last zero)
				--len_bits;
				if (val_bits != 0) {
					// the remainder of the length is in the same octet as the remainder of the
					// value => step back onto it
					--i;
				} else {
					// the remainder of the length is in a separate octet
					bc[i] = 0;
				}
				// insert the length's partial octet
				int mask = 0x80;
				for (int j = 0; j < len_bits % 8; ++j) {
					bc[i] |= mask;
					mask >>= 1;
				}
				if (len_bits % 8 > 0 || val_bits != 0) {
					// there was a partial octet => step onto the first full octet
					++i;
				}
				// insert the length's full octets
				while (len_bits >= 8) {
					// octets containing only ones in the length
					bc[i] = (byte)0xFF;
					++i;
					len_bits -= 8;
				}
				myleaf.length = length * 8;
			} else {
				for (int a = 0; a < length; a++) {
					bc[a] = (byte)(value & 0xFF);
					value >>= 8;
				}
				if (neg_sgbit) {
					final int mask = 0x01 << (p_td.raw.fieldlength - 1) % 8;
					bc[length - 1] |= mask;
				}
				myleaf.length = p_td.raw.fieldlength;
			}

			myleaf.coding_par.csn1lh = p_td.raw.csn1lh;
		} finally {
			errorContext.leave_context();
		}

		return myleaf.length;
	}

	//TODO actually big integer
	public int RAW_encode_openssl(final TTCN_Typedescriptor p_td, final RAW_enc_tree myleaf) {
		byte[] bc = null;
		int length = 0; // total length, in bytes
		int val_bits = 0, len_bits = 0; // only for IntX
		BigInteger D = new BigInteger(openSSL.toString());
		final TTCN_EncDec_ErrorContext errorContext = new TTCN_EncDec_ErrorContext();
		try {
			boolean neg_sgbit = (D.signum() == -1) && (p_td.raw.comp == raw_sign_t.SG_SG_BIT);
			if (!is_bound()) {
				TTCN_EncDec_ErrorContext.error(error_type.ET_UNBOUND, "Encoding an unbound value.");
				neg_sgbit = false;
			}
			if ((D.signum() == -1) && (p_td.raw.comp == raw_sign_t.SG_NO)) {
				TTCN_EncDec_ErrorContext.error(error_type.ET_SIGN_ERR, "Unsigned encoding of a negative number: %s", p_td.name);
				D = D.negate();
				neg_sgbit = false;
			}
			// `if (neg_sgbit) tmp->neg = tmp->neg == 0;' is not needed, because the
			// sign is stored separately from the number.  Default encoding of negative
			// values in 2's complement form.
			if (p_td.raw.fieldlength == RAW.RAW_INTX) {
				val_bits = D.bitLength(); // bits needed to store the value
				len_bits = 1 + val_bits / 8; // bits needed to store the length
				if (val_bits % 8 + len_bits % 8 > 8) {
					// the remainder of the value bits and the length bits do not fit into
					// an octet => an extra octet is needed and the length must be increased
					++len_bits;
				}
				length = (len_bits + val_bits + 7) / 8;
				if (len_bits % 8 == 0 && val_bits % 8 != 0) {
					// special case: the value can be stored on 8k - 1 octets plus the partial octet
					// - len_bits = 8k is not enough, since there's no partial octet in that case
					// and the length would then be followed by 8k octets (and it only indicates
					// 8k - 1 further octets)
					// - len_bits = 8k + 1 is too much, since there are only 8k - 1 octets
					// following the partial octet (and 8k are indicated)
					// solution: len_bits = 8k + 1 and insert an extra empty octet
					++len_bits;
					++length;
				}
			} else {
				length = (p_td.raw.fieldlength + 7) / 8;
				final int min_bits = RAW.min_bits(D);
				if (min_bits > p_td.raw.fieldlength) {
					TTCN_EncDec_ErrorContext.error(error_type.ET_LEN_ERR, "There are insufficient bits to encode '%s':", p_td.name);
					// `tmp = -((-tmp) & BitMaskTable[min_bits(tmp)]);' doesn't make any sense
					// at all for negative values.  Just simply clear the value.
					neg_sgbit = false;
				}
			}
			if (length > RAW.RAW_INT_ENC_LENGTH) {
				myleaf.data_array = bc = new byte[length];
			} else {
				bc = myleaf.data_array;
			}

			final boolean twos_compl = (D.signum() == -1) && !neg_sgbit;

			if (p_td.raw.fieldlength == RAW.RAW_INTX) {
				int i = 0;
				// treat the empty space between the value and the length as if it was part
				// of the value, too
				val_bits = length * 8 - len_bits;
				// first, encode the value
				final byte[] tmp = neg_sgbit ? D.abs().toByteArray() : D.toByteArray();
				final int num_bytes = tmp.length;
				do {
					bc[i] = (byte) (((num_bytes - i > 0 ? tmp[num_bytes - (i + 1)] : (twos_compl ? 0xFF : 0)) & INTX_MASKS[val_bits > 8 ? 8 : val_bits]) & 0xFF);
					++i;
					val_bits -= 8;
				} while (val_bits > 0);
				if (neg_sgbit) {
					// the sign bit is the first bit after the length
					final int mask = 0x80 >> len_bits % 8;
					bc[i - 1] |= mask;
				}
				// second, encode the length (ignore the last zero)
				--len_bits;
				if (val_bits != 0) {
					// the remainder of the length is in the same octet as the remainder of the
					// value => step back onto it
					--i;
				} else {
					// the remainder of the length is in a separate octet
					bc[i] = 0;
				}
				// insert the length's partial octet
				int mask = 0x80;
				for (int j = 0; j < len_bits % 8; ++j) {
					bc[i] |= mask;
					mask >>= 1;
				}
				if (len_bits % 8 > 0 || val_bits != 0) {
					// there was a partial octet => step onto the first full octet
					++i;
				}
				// insert the length's full octets
				while (len_bits >= 8) {
					// octets containing only ones in the length
					bc[i] = (byte)0xFF;
					++i;
					len_bits -= 8;
				}
				myleaf.length = length * 8;
			} else {
				final byte[] tmp;
				if (twos_compl) {
					tmp = D.toByteArray();
				} else {
					tmp = D.abs().toByteArray();
				}
				final int num_bytes = tmp.length;
				for (int a = 0; a < length; a++) {
					if (twos_compl && num_bytes - 1 < a) {
						bc[a] = (byte)0xff;
					} else {
						bc[a] = (byte) ((num_bytes - a > 0 ? tmp[num_bytes - (a + 1)] : 0) & 0xff);
					}
				}
				if (neg_sgbit) {
					final int mask = 0x01 << (p_td.raw.fieldlength - 1) % 8;
					bc[length - 1] |= mask;
				}
				myleaf.length = p_td.raw.fieldlength;
			}

			myleaf.coding_par.csn1lh = p_td.raw.csn1lh;
		} finally {
			errorContext.leave_context();
		}

		return myleaf.length;
	}

	@Override
	/** {@inheritDoc} */
	public int RAW_decode(final TTCN_Typedescriptor p_td, final TTCN_Buffer buff, final int limit, final raw_order_t top_bit_ord) {
		return RAW_decode(p_td, buff, limit, top_bit_ord, false, -1, true, null);
	}

	@Override
	/** {@inheritDoc} */
	public int RAW_decode(final TTCN_Typedescriptor p_td, final TTCN_Buffer buff, int limit, final raw_order_t top_bit_ord, final boolean no_err, final int sel_field, final boolean first_call, final RAW_Force_Omit force_omit) {
		final TTCN_EncDec_ErrorContext errorContext = new TTCN_EncDec_ErrorContext();
		try {
			boundFlag = false;
			final int prepaddlength = buff.increase_pos_padd(p_td.raw.prepadding);
			limit -= prepaddlength;
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
			int decode_length = 0;
			int len_bits = 0; // only for IntX (amount of bits used to store the length)
			byte len_data = 0; // only for IntX (an octet used to store the length)
			int partial_octet_bits = 0; // only for IntX (amount of value bits in the partial octet)
			final byte[] tmp_len_data = new byte[1];
			if (p_td.raw.fieldlength == RAW.RAW_INTX) {
				// extract the length
				do {
					// check if at least 8 bits are available in the buffer
					if (8 > limit) {
						if (!no_err) {
							TTCN_EncDec_ErrorContext.error(error_type.ET_LEN_ERR, "There are not enough bits in the buffer to decode the length of IntX type %s (needed: %d, found: %d).", p_td.name, len_bits + 8, len_bits + limit);
						}

						return -error_type.ET_LEN_ERR.ordinal();
					} else {
						limit -= 8;
					}
					final int nof_unread_bits = buff.unread_len_bit();
					if (nof_unread_bits < 8) {
						if (!no_err) {
							TTCN_EncDec_ErrorContext.error(error_type.ET_INCOMPL_MSG, "There are not enough bits in the buffer to decode the length of IntX type %s (needed: %d, found: %d).", p_td.name, len_bits + 8, len_bits + nof_unread_bits);
						}

						return -error_type.ET_INCOMPL_MSG.ordinal();
					}

					// extract the next length octet (or partial length octet)
					buff.get_b(8, tmp_len_data, cp, top_bit_ord);
					int mask = 0x80;
					len_data = tmp_len_data[0];
					do {
						++len_bits;
						if ((tmp_len_data[0] & mask) != 0) {
							mask >>= 1;
						} else {
							// the first zero signals the end of the length
							// the rest of the bits in the octet are part of the value
							partial_octet_bits = (8 - len_bits % 8) % 8;

							// decode_length only stores the amount of bits in full octets needed
							// by the value, the bits in the partial octet are stored by len_data
							decode_length = 8 * (len_bits - 1);
							break;
						}
					} while (len_bits % 8 != 0);
				} while (decode_length == 0 && partial_octet_bits == 0);
			} else {
				// not IntX, use the static field length
				decode_length = p_td.raw.fieldlength;
			}
			if (decode_length > limit) {
				if (!no_err) {
					TTCN_EncDec_ErrorContext.error(error_type.ET_LEN_ERR,
							"There are not enough bits in the buffer to decode%s type %s (needed: %d, found: %d).", p_td.raw.fieldlength == RAW.RAW_INTX ? " the value of IntX" : "", p_td.name, decode_length, limit);
				}
				if (no_err || p_td.raw.fieldlength == RAW.RAW_INTX) {
					return -error_type.ET_LEN_ERR.ordinal();
				}
				decode_length = limit;
			}

			final int nof_unread_bits = buff.unread_len_bit();
			if (decode_length > nof_unread_bits) {
				if (!no_err) {
					TTCN_EncDec_ErrorContext.error(error_type.ET_INCOMPL_MSG,
							"There are not enough bits in the buffer to decode %s type %s (needed: %d, found: %d).", p_td.raw.fieldlength == RAW.RAW_INTX ? " the value of IntX" : "", p_td.name, decode_length, nof_unread_bits);
				}
				if (no_err || p_td.raw.fieldlength == RAW.RAW_INTX) {
					return error_type.ET_INCOMPL_MSG.ordinal();
				}
				decode_length = nof_unread_bits;
			}
			clean_up();
			if (decode_length < 0) {
				return -1;
			} else if (decode_length == 0 && partial_octet_bits == 0) {
				boundFlag = true;
				nativeFlag = true;
				nativeInt = 0;
			} else {
				int tmp = 0;
				int twos_compl = 0;
				byte[] data = new byte[ (decode_length + partial_octet_bits + 7) / 8];
				buff.get_b(decode_length, data, cp, top_bit_ord);
				if (partial_octet_bits != 0) {
					// in case there are value bits in the last length octet (only for IntX),
					// these need to be appended to the extracted data
					data[decode_length / 8] = len_data;
					decode_length += partial_octet_bits;
				}
				int end_pos = decode_length;
				int idx = (end_pos - 1) / 8;
				boolean negativ_num = false;
				switch (p_td.raw.comp) {
				case SG_2COMPL:
					if ((data[idx] >> ((end_pos - 1) % 8) & 0x01) != 0) {
						tmp = -1;
						twos_compl = 1;
					}
					break;
				case SG_NO:
					break;
				case SG_SG_BIT:
					negativ_num = ((data[idx] >> ((end_pos - 1) % 8)) & 0x01) != 0;
					end_pos--;
					break;
				default:
					break;
				}
				if (end_pos < 9) {
					tmp <<= end_pos;
					tmp |= data[0] & RAW.BitMaskTable[end_pos];
				} else {
					idx = (end_pos - 1) / 8;
					tmp <<= (end_pos - 1) % 8 + 1;
					tmp |= data[idx--] & RAW.BitMaskTable[(end_pos - 1) % 8 + 1];
					if (decode_length >  32 - 1) {
						BigInteger D = BigInteger.valueOf(tmp);
						int pad = tmp == 0 ? 1 : 0;
						for (; idx >= 0; idx--) {
							if (pad != 0 && data[idx] != 0) {
								D = BigInteger.valueOf(data[idx] & 0xFF);
								pad = 0;
								continue;
							}
							if (pad != 0) {
								continue;
							}

							D = D.shiftLeft(8);
							D = D.add(BigInteger.valueOf(data[idx] & 0xFF));
						}
						if (twos_compl != 0) {
							final BigInteger D_tmp = BigInteger.ZERO;
							D = D.subtract(D_tmp);
						} else if (negativ_num) {
							D = D.negate();
						}
						if (D.bitLength() > 31) {
							boundFlag = true;
							nativeFlag = false;
							openSSL = D;
						} else {
							boundFlag = true;
							nativeFlag = true;
							nativeInt = D.intValue();
						}
						decode_length += buff.increase_pos_padd(p_td.raw.padding);
						boundFlag = true;

						return decode_length + prepaddlength + len_bits;
					} else {
						for (; idx >= 0; idx--) {
							tmp <<= 8;
							tmp |= data[idx] & 0xff;
						}
					}
				}
				boundFlag = true;
				nativeFlag = true;
				nativeInt = negativ_num ? -tmp : tmp;
			}
			decode_length += buff.increase_pos_padd(p_td.raw.padding);
			boundFlag = true;

			return decode_length + prepaddlength + len_bits;
		} finally {
			errorContext.leave_context();
		}
	}

	@Override
	/** {@inheritDoc} */
	public void set_param(Module_Parameter param) {
		param.basic_check(basic_check_bits_t.BC_VALUE.getValue(), "integer value");

		// Originally RT2
		if (param.get_type() == Module_Parameter.type_t.MP_Reference) {
			param = param.get_referenced_param().get();
		}

		switch (param.get_type()) {
		case MP_Integer:
			operator_assign(param.get_integer());
			break;
		case MP_Expression:
			switch (param.get_expr_type()) {
			case EXPR_NEGATE: {
				final TitanInteger operand = new TitanInteger();
				operand.set_param(param.get_operand1());
				operator_assign(operand.sub());
				break; }
			case EXPR_ADD: {
				final TitanInteger operand1 = new TitanInteger();
				final TitanInteger operand2 = new TitanInteger();
				operand1.set_param(param.get_operand1());
				operand2.set_param(param.get_operand2());
				operator_assign(operand1.add(operand2));
				break;
			}
			case EXPR_SUBTRACT: {
				final TitanInteger operand1 = new TitanInteger();
				final TitanInteger operand2 = new TitanInteger();
				operand1.set_param(param.get_operand1());
				operand2.set_param(param.get_operand2());
				operator_assign(operand1.sub(operand2));
				break;
			}
			case EXPR_MULTIPLY: {
				final TitanInteger operand1 = new TitanInteger();
				final TitanInteger operand2 = new TitanInteger();
				operand1.set_param(param.get_operand1());
				operand2.set_param(param.get_operand2());
				operator_assign(operand1.mul(operand2));
				break;
			}
			case EXPR_DIVIDE: {
				final TitanInteger operand1 = new TitanInteger();
				final TitanInteger operand2 = new TitanInteger();
				operand1.set_param(param.get_operand1());
				operand2.set_param(param.get_operand2());
				if (operand2.operator_equals(0)) {
					param.error("Integer division by zero.");
				}
				operator_assign(operand1.div(operand2));
				break; }
			default:
				param.expr_type_error("an integer");
				break;
			}
			break;
		default:
			param.type_error("integer value");
			break;
		}
	}

	@Override
	/** {@inheritDoc} */
	public Module_Parameter get_param(final Module_Param_Name param_name) {
		if (!boundFlag) {
			return new Module_Param_Unbound();
		}
		return new Module_Param_Integer(this);
	}
}
