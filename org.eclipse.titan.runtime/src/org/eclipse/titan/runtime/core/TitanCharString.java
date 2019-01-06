/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.titan.runtime.core.Param_Types.Module_Parameter;
import org.eclipse.titan.runtime.core.Param_Types.Module_Parameter.basic_check_bits_t;
import org.eclipse.titan.runtime.core.Param_Types.Module_Parameter.expression_operand_t;
import org.eclipse.titan.runtime.core.Param_Types.Module_Parameter.operation_type_t;
import org.eclipse.titan.runtime.core.RAW.RAW_Force_Omit;
import org.eclipse.titan.runtime.core.RAW.RAW_coding_par;
import org.eclipse.titan.runtime.core.RAW.RAW_enc_tr_pos;
import org.eclipse.titan.runtime.core.RAW.RAW_enc_tree;
import org.eclipse.titan.runtime.core.TTCN_EncDec.coding_type;
import org.eclipse.titan.runtime.core.TTCN_EncDec.error_type;
import org.eclipse.titan.runtime.core.TTCN_EncDec.raw_order_t;


/**
 * TTCN-3 charstring
 * @author Arpad Lovassy
 * @author Andrea Palfi
 */
public class TitanCharString extends Base_Type {

	/**
	 * charstring value.
	 */
	private StringBuilder val_ptr;

	/**
	 * Initializes to unbound value.
	 * */
	public TitanCharString() {
		super();
	}

	/**
	 * Initializes to a given value.
	 *
	 * @param otherValue
	 *                the value to initialize to.
	 * */
	public TitanCharString(final StringBuilder otherValue) {
		val_ptr = new StringBuilder(otherValue);
	}

	/**
	 * Initializes to a given value.
	 *
	 * @param otherValue
	 *                the value to initialize to.
	 * */
	public TitanCharString(final String otherValue) {
		copy_value(otherValue);
	}

	/**
	 * Initializes to a given value.
	 *
	 * @param otherValue
	 *                the value to initialize to.
	 * */
	public TitanCharString(final TitanCharString otherValue) {
		otherValue.must_bound("Copying an unbound charstring value.");

		val_ptr = new StringBuilder(otherValue.val_ptr);
	}

	/**
	 * Initializes to a given value.
	 *
	 * @param otherValue
	 *                the value to initialize to.
	 * */
	public TitanCharString(final TitanUniversalCharString otherValue) {
		otherValue.must_bound("Copying an unbound universal charstring to a charstring.");

		operator_assign(otherValue);
	}

	/**
	 * Returns the internal data storage of this charstring.
	 * <p>
	 * Please note, this code is for internal use only.
	 * Users are not recommended to use this function.
	 * As such it is also not part of the public API
	 *  and might change without notice.
	 *
	 * <p>
	 * char*() in the core
	 *
	 * @return the internal representation of the charstring.
	 * */
	public StringBuilder get_value() {
		must_bound("Getting an unbound charstring value as string.");

		return val_ptr;
	}

	private void copy_value(final String aOtherValue) {
		val_ptr = new StringBuilder(aOtherValue);
	}

	private void copy_value(final StringBuilder aOtherValue) {
		val_ptr = new StringBuilder(aOtherValue);
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
	public TitanCharString operator_assign(final String otherValue) {
		clean_up();

		if (otherValue == null) {
			val_ptr = new StringBuilder();
		} else {
			val_ptr = new StringBuilder(otherValue);
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
	public TitanCharString operator_assign(final TitanCharString otherValue) {
		otherValue.must_bound("Assignment of an unbound charstring value.");

		if (otherValue != this) {
			copy_value(otherValue.val_ptr);
		}

		return this;
	}

	@Override
	public TitanCharString operator_assign(final Base_Type otherValue) {
		if (otherValue instanceof TitanCharString) {
			return operator_assign((TitanCharString)otherValue);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to charstring", otherValue));
	}

	@Override
	public boolean is_bound() {
		return val_ptr != null;
	}

	@Override
	public boolean is_present() {
		return is_bound();
	}

	@Override
	public boolean is_value() {
		return val_ptr != null;
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
	public TitanCharString operator_assign(final TitanCharString_Element otherValue) {
		otherValue.must_bound("Assignment of an unbound charstring element to a charstring.");

		clean_up();
		val_ptr = new StringBuilder(1);
		val_ptr.append(otherValue.get_char());

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
	public TitanCharString operator_assign(final TitanUniversalCharString otherValue) {
		otherValue.must_bound("Assignment of an unbound universal charstring to a charstring.");

		if (otherValue.charstring) {
			return operator_assign(otherValue.cstr.toString());
		} else {
			clean_up();
			final StringBuilder sb = new StringBuilder();
			for (int i = 0; i < otherValue.val_ptr.size(); ++i) {
				final TitanUniversalChar uc = otherValue.val_ptr.get(i);
				if (uc.getUc_group() != 0 || uc.getUc_plane() != 0 || uc.getUc_row() != 0) {
					throw new TtcnError(MessageFormat.format("Multiple-byte characters cannot be assigned to a charstring, invalid character char({0},{1}, {2}, {3}) at index {4}.", otherValue));
				} else {
					sb.append(uc.getUc_cell());
				}
			}
			//if every char was ok, it can be pass to the charstring
			val_ptr = sb;
		}

		return this;
	}

	/**
	 * Returns the number of elements, that is, the largest used index plus
	 * one and zero for the empty value.
	 *
	 * lengthof in the core
	 *
	 * @return the number of elements.
	 * */
	public TitanInteger lengthof() {
		must_bound("Performing lengthof operation on an unbound charstring value.");

		return new TitanInteger(val_ptr.length());
	}

	public static enum CharCoding {
		UNKNOWN,
		ASCII,
		UTF_8,
		UTF16,
		UTF16BE,
		UTF16LE,
		UTF32,
		UTF32BE,
		UTF32LE
	}

	private static enum States {
		INIT, PCHAR, NPCHAR;
	}

	@Override
	public void log() {
		if (val_ptr != null) {
			States state = States.INIT;
			final StringBuilder buffer = new StringBuilder();
			for (int i = 0; i < val_ptr.length(); i++) {
				final char c = val_ptr.charAt(i);
				if (TTCN_Logger.is_printable(c)) {
					switch (state) {
					case NPCHAR:
						buffer.append(" & ");
					case INIT:
						buffer.append("\"");
					case PCHAR:
						TTCN_Logger.log_char_escaped(c, buffer);
						break;
					}
					state = States.PCHAR;
				} else {
					switch (state) {
					case PCHAR:
						buffer.append("\"");
					case NPCHAR:
						buffer.append(" & ");
					case INIT:
						buffer.append(MessageFormat.format("char(0, 0, 0, {0})", (int) c));
						break;
					}
					state = States.NPCHAR;
				}
			}
			switch (state) {
			case INIT:
				buffer.append("\"\"");
				break;
			case PCHAR:
				buffer.append("\"");
				break;
			default:
				break;
			}
			TTCN_Logger.log_event_str(buffer.toString());

		} else {
			TTCN_Logger.log_event_unbound();
		}
	}

	@Override
	/** {@inheritDoc} */
	public void encode_text(final Text_Buf text_buf) {
		must_bound("Text encoder: Encoding an unbound charstring value.");

		final int n_chars = val_ptr.length();
		text_buf.push_int(n_chars);
		if (n_chars > 0) {
			byte[] temp = new byte[n_chars];
			for (int i = 0; i < n_chars; i++) {
				temp[i] = (byte)val_ptr.charAt(i);
			}
			text_buf.push_raw(n_chars, temp);
		}
	}

	@Override
	/** {@inheritDoc} */
	public void decode_text(final Text_Buf text_buf) {
		clean_up();

		final int n_chars = text_buf.pull_int().get_int();
		if (n_chars < 0) {
			throw new TtcnError("Text decoder: Invalid length was received for a charstring.");
		}

		val_ptr = new StringBuilder(n_chars);
		if (n_chars > 0) {
			final byte[] temp = new byte[n_chars];
			text_buf.pull_raw(n_chars, temp);
			for (int i = 0; i < n_chars; i++) {
				val_ptr.append((char)temp[i]);
			}
		}
	}

	/**
	 * Concatenates the current charstring with the charstring received as a
	 * parameter.
	 *
	 * operator+ in the core.
	 *
	 * @param other_value
	 *                the other value to concatenate with.
	 * @return the new charstring representing the concatenated value.
	 * */
	public TitanCharString operator_concatenate(final TitanCharString other_value) {
		must_bound("Unbound left operand of charstring addition.");
		other_value.must_bound("Unbound right operand of charstring addition.");

		final TitanCharString result = new TitanCharString(val_ptr);
		result.val_ptr.append(other_value.val_ptr);

		return result;
	}

	/**
	 * Concatenates the current charstring with the charstring received as a
	 * parameter.
	 *
	 * operator+ in the core.
	 *
	 * @param other_value
	 *                the other value to concatenate with.
	 * @return the new charstring representing the concatenated value.
	 * */
	public TitanCharString operator_concatenate(final String other_value) {
		must_bound("Unbound operand of charstring concatenation.");

		final TitanCharString ret_val = new TitanCharString(val_ptr);
		if (other_value != null && other_value.length() > 0) {
			ret_val.val_ptr.append(other_value);
		}

		return ret_val;
	}

	//originally operator+=
	//append for String
	public TitanCharString append(final String aOtherValue) {
		must_bound(" Appending a string literal to an unbound charstring value.");

		if (aOtherValue != null && aOtherValue.length() > 0) {
			val_ptr.append(aOtherValue);
		}

		return this;
	}

	//originally operator+=
	// append for charstring_element
	public TitanCharString append(final TitanCharString_Element aOtherValue) {
		must_bound("Appending a charstring value to an unbound charstring value.");
		aOtherValue.must_bound("Appending an unbound charstring value to another charstring value.");

		val_ptr.append(aOtherValue.get_char());

		return this;
	}

	/**
	 * Concatenates the current charstring with the charstring received as a
	 * parameter.
	 *
	 * operator+ in the core.
	 *
	 * @param other_value
	 *                the other value to concatenate with.
	 * @return the new charstring representing the concatenated value.
	 * */
	public TitanCharString operator_concatenate(final TitanCharString_Element other_value) {
		must_bound("Unbound operand of charstring concatenation.");
		other_value.must_bound("Unbound operand of charstring element concatenation.");

		final TitanCharString ret_val = new TitanCharString(this);
		ret_val.val_ptr.append(other_value.get_char());

		return ret_val;
	}

	/**
	 * Concatenates the current charstring with the universal charstring
	 * received as a parameter.
	 *
	 * operator+ in the core.
	 *
	 * @param other_value
	 *                the other value to concatenate with.
	 * @return the new universal charstring representing the concatenated
	 *         value.
	 * */
	public TitanUniversalCharString operator_concatenate(final TitanUniversalCharString other_value) {
		must_bound("The left operand of concatenation is an unbound charstring value.");
		other_value.must_bound("The right operand of concatenation is an unbound universal charstring value.");

		if (val_ptr.length() == 0) {
			return new TitanUniversalCharString(other_value);
		}
		if (other_value.charstring) {
			final StringBuilder ret_val = new StringBuilder(get_value());
			ret_val.append(other_value.cstr.toString());

			return new TitanUniversalCharString(ret_val);
		} else {
			final List<TitanUniversalChar> ret_val = new ArrayList<TitanUniversalChar>();
			for (int i = 0; i < val_ptr.length(); i++) {
				ret_val.add(new TitanUniversalChar((char) 0, (char) 0, (char) 0, val_ptr.charAt(i)));
			}
			ret_val.addAll(other_value.get_value());

			return new TitanUniversalCharString(ret_val);
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
	public boolean operator_equals(final TitanCharString otherValue) {
		must_bound("Unbound left operand of charstring comparison.");
		otherValue.must_bound("Unbound right operand of charstring comparison.");

		return val_ptr.toString().equals(otherValue.val_ptr.toString());
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
	public boolean operator_equals(final TitanUniversalCharString otherValue) {
		must_bound("The left operand of comparison is an unbound charstring value.");
		otherValue.must_bound("The right operand of comparison is an unbound universal charstring value.");

		if (otherValue.charstring) {
			return val_ptr.toString().equals(otherValue.cstr.toString());
		}
		if (val_ptr.length() != otherValue.val_ptr.size()) {
			return false;
		}
		for (int i = 0; i < val_ptr.length(); i++) {
			final char tempLeft = val_ptr.charAt(i);
			final TitanUniversalChar tempRight = otherValue.val_ptr.get(i);
			if (tempRight.getUc_group() != 0 || tempRight.getUc_plane() != 0 || tempRight.getUc_row() != 0
					|| tempRight.getUc_cell() != tempLeft) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean operator_equals(final Base_Type otherValue) {
		if (otherValue instanceof TitanCharString) {
			return operator_equals((TitanCharString)otherValue);
		} else if (otherValue instanceof TitanUniversalCharString) {
			return operator_equals((TitanUniversalCharString)otherValue);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to charstring", otherValue));
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
	public boolean operator_equals(final String otherValue) {
		must_bound("Unbound operand of charstring comparison.");

		if (otherValue == null) {
			return val_ptr.length() == 0;
		}
		return this.val_ptr.toString().equals(otherValue);
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
	public boolean operator_equals(final TitanCharString_Element otherValue) {
		otherValue.must_bound("Unbound operand of charstring element comparison.");
		must_bound("Unbound operand of charstring comparison.");

		if (val_ptr.length() != 1) {
			return false;
		}

		return val_ptr.charAt(0) == otherValue.get_char();

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
	public boolean operator_equals(final TitanUniversalCharString_Element otherValue) {
		must_bound("The left operand of comparison is an unbound charstring value.");
		otherValue.must_bound("The right operand of comparison is an unbound universal charstring value");

		if (val_ptr.length() != 1) {
			return false;
		}

		final TitanUniversalChar uc = otherValue.get_char();
		return uc.getUc_group() == 0 && uc.getUc_plane() == 0 && uc.getUc_row() == 0 && uc.getUc_cell() == val_ptr.charAt(0);
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
	public boolean operator_not_equals(final TitanCharString otherValue) {
		return !operator_equals(otherValue);
	}

	@Override
	public void clean_up() {
		val_ptr = null;
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
	public boolean operator_not_equals(final TitanCharString_Element otherValue) {
		otherValue.must_bound("Unbound operand of charstring element comparison.");
		must_bound("Unbound operand of charstring comparison.");

		if (val_ptr.length() == 1) {
			return false;
		}
		return val_ptr.charAt(0) != otherValue.get_char();
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
	public boolean operator_not_equals(final String otherValue) {
		must_bound("Unbound operand of charstring comparison.");

		if (otherValue != null) {
			return val_ptr.length() == 0;
		}

		return this.val_ptr.toString().equals(otherValue);
	}

	/**
	 * Creates a new charstring, that is the equivalent of the
	 * current one with its elements rotated to the left with the provided
	 * amount.
	 *
	 * operator<<= in the core.
	 *
	 * @param rotate_count
	 *                the number of characters to rotate left.
	 * @return the new charstring.
	 * */
	public TitanCharString rotate_left(int rotate_count) {
		must_bound("Unbound charstring operand of rotate left operator.");

		if (val_ptr.length() == 0) {
			return this;
		}
		if (rotate_count >= 0) {
			rotate_count %= val_ptr.length();
			if (rotate_count == 0) {
				return this;
			}
			final StringBuilder rValue = new StringBuilder(val_ptr.length());
			for (int i = 0; i < val_ptr.length(); i++) {
				rValue.append(val_ptr.charAt((i + rotate_count) % val_ptr.length()));
			}
			return new TitanCharString(rValue);
		}

		return rotate_right(-rotate_count);
	}

	/**
	 * Creates a new charstring, that is the equivalent of the
	 * current one with its elements rotated to the left with the provided
	 * amount.
	 *
	 * operator<<= in the core.
	 *
	 * @param rotate_count
	 *                the number of characters to rotate left.
	 * @return the new charstring.
	 * */
	public TitanCharString rotate_left(final TitanInteger rotate_count) {
		rotate_count.must_bound("Unbound integer operand of rotate left operator.");

		return rotate_left(rotate_count.get_int());
	}

	/**
	 * Creates a new charstring, that is the equivalent of the
	 * current one with its elements rotated to the right with the provided
	 * amount.
	 *
	 * operator>>= in the core.
	 *
	 * @param rotate_count
	 *                the number of characters to rotate right.
	 * @return the new charstring.
	 * */
	public TitanCharString rotate_right(int rotate_count) {
		must_bound("Unbound charstring operand of rotate right operator.");

		if (val_ptr.length() == 0) {
			return this;
		}
		if (rotate_count >= 0) {
			rotate_count %= val_ptr.length();
			if (rotate_count == 0) {
				return this;
			}
			final StringBuilder rValue = new StringBuilder(val_ptr.length());

			for (int i = 0; i < rotate_count; i++) {
				rValue.append(val_ptr.charAt(i + val_ptr.length() - rotate_count));
			}
			for (int i = rotate_count; i < val_ptr.length(); i++) {
				rValue.append(val_ptr.charAt(i - rotate_count));
			}
			return new TitanCharString(rValue);
		}
		return rotate_left(-rotate_count);
	}

	/**
	 * Creates a new charstring, that is the equivalent of the
	 * current one with its elements rotated to the right with the provided
	 * amount.
	 *
	 * operator>>= in the core.
	 *
	 * @param rotate_count
	 *                the number of characters to rotate right.
	 * @return the new charstring.
	 * */
	public TitanCharString rotate_right(final TitanInteger rotate_count) {
		rotate_count.must_bound("Unbound integer operand of rotate right operator.");

		return rotate_right(rotate_count.get_int());
	}

	/**
	 * Gives access to the given element. Indexing begins from zero.
	 * Over-indexing by 1 extends the charstring.
	 *
	 * operator[] in the core.
	 *
	 * @param index_value
	 *            the index of the element to return.
	 * @return the element at the specified position in this bitstring
	 * */
	public TitanCharString_Element get_at(final int index_value) {
		if (val_ptr == null && index_value == 0) {
			val_ptr = new StringBuilder();
			return new TitanCharString_Element(false, this, 0);
		} else {
			must_bound("Accessing an element of an unbound charstring value.");

			if (index_value < 0) {
				throw new TtcnError("Accessing an charstring element using a negative index (" + index_value + ").");
			}

			final int n_nibbles = val_ptr.length();
			if (index_value > n_nibbles) {
				throw new TtcnError("Index overflow when accessing a charstring element: The index is " + index_value +
						", but the string has only " + n_nibbles + " characters.");
			}
			if (index_value == n_nibbles) {
				val_ptr.setLength(index_value + 1);
				return new TitanCharString_Element(false, this, index_value);
			} else {
				return new TitanCharString_Element(true, this, index_value);
			}
		}
	}

	/**
	 * Gives access to the given element. Indexing begins from zero.
	 * Over-indexing by 1 extends the charstring.
	 *
	 * operator[] in the core.
	 *
	 * @param index_value
	 *            the index of the element to return.
	 * @return the element at the specified position in this charstring
	 * */
	public TitanCharString_Element get_at(final TitanInteger index_value) {
		index_value.must_bound("Indexing a charstring value with an unbound integer value.");

		return get_at(index_value.get_int());
	}

	/**
	 * Gives read-only access to the given element.
	 *
	 * Index underflow and overflow causes dynamic test case error.
	 *
	 * const operator[] const in the core.
	 *
	 * @param index
	 *            the index of the element to return.
	 * @return the element at the specified position in this charstring
	 * */
	public TitanCharString_Element constGet_at(final int index_value) {
		must_bound("Accessing an element of an unbound charstring value.");

		if (index_value < 0) {
			throw new TtcnError("Accessing an charstring element using a negative index (" + index_value + ").");
		}

		final int n_nibbles = val_ptr.length();
		if (index_value >= n_nibbles) {
			throw new TtcnError("Index overflow when accessing a charstring element: The index is " + index_value +
					", but the string has only " + n_nibbles + " characters.");
		}

		return new TitanCharString_Element(true, this, index_value);
	}

	/**
	 * Gives read-only access to the given element.
	 *
	 * Index underflow and overflow causes dynamic test case error.
	 *
	 * const operator[] const in the core.
	 *
	 * @param index
	 *            the index of the element to return.
	 * @return the element at the specified position in this charstring
	 * */
	public TitanCharString_Element constGet_at(final TitanInteger index_value) {
		index_value.must_bound("Indexing a charstring value with an unbound integer value.");

		return constGet_at(index_value.get_int());
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
		if (val_ptr == null) {
			return "<unbound>";
		}

		return val_ptr.toString();
	}

	/**
	 * Checks if the first value is equivalent to the second one.
	 *
	 * static operator== in the core
	 *
	 * @param stringValue
	 *                the first value.
	 * @param otherValue
	 *                the other value to check against.
	 * @return {@code true} if the values are equivalent.
	 */
	public static boolean operator_equals(final String stringValue, final TitanCharString otherValue) {
		otherValue.must_bound("Unbound operand of charstring comparison.");

		if (stringValue == null) {
			return otherValue.val_ptr.length() == 0;
		}

		return otherValue.val_ptr.toString().equals(stringValue);
	}

	/**
	 * Checks if the first value is equivalent to the second one.
	 *
	 * static operator== in the core
	 *
	 * @param stringValue
	 *                the first value.
	 * @param otherValue
	 *                the other value to check against.
	 * @return {@code true} if the values are equivalent.
	 */
	public static boolean operator_equals(final String stringValue, final TitanCharString_Element otherValue) {
		otherValue.must_bound("Unbound operand of charstring element comparison.");

		if (stringValue.length() != 1) {
			return false;
		}

		return stringValue.charAt(0) == otherValue.get_char();
	}

	/**
	 * Checks if the first value is not equivalent to the second one.
	 *
	 * static operator!= in the core
	 *
	 * @param stringValue
	 *                the first value.
	 * @param otherValue
	 *                the other value to check against.
	 * @return {@code true} if the values are equivalent.
	 */
	public static boolean operator_not_equals(final String stringValue, final TitanCharString otherValue) {
		return !operator_equals(stringValue, otherValue);
	}

	/**
	 * Checks if the first value is not equivalent to the second one.
	 *
	 * static operator!= in the core
	 *
	 * @param stringValue
	 *                the first value.
	 * @param otherValue
	 *                the other value to check against.
	 * @return {@code true} if the values are equivalent.
	 */
	public static boolean operator_not_equals(final String stringValue, final TitanCharString_Element otherValue) {
		return !operator_equals(stringValue, otherValue);
	}

	/**
	 * Concatenates the first charstring parameter with the second
	 * charstring received as a parameter.
	 *
	 * operator+ in the core.
	 *
	 * @param stringValue
	 *                the first parameter.
	 * @param other_value
	 *                the other value to concatenate with.
	 * @return the new charstring representing the concatenated value.
	 * */
	public static TitanCharString operator_concatenate(final String stringValue, final TitanCharString other_value) {
		other_value.must_bound("Unbound operand of charstring concatenation.");

		final TitanCharString ret_val = new TitanCharString(stringValue);
		return ret_val.operator_concatenate(other_value);
	}

	/**
	 * Concatenates the first charstring parameter with the second
	 * charstring received as a parameter.
	 *
	 * operator+ in the core.
	 *
	 * @param stringValue
	 *                the first parameter.
	 * @param other_value
	 *                the other value to concatenate with.
	 * @return the new charstring representing the concatenated value.
	 * */
	public static TitanCharString operator_concatenate(final String stringValue, final TitanCharString_Element other_value) {
		other_value.must_bound("Unbound operand of charstring element concatenation.");

		final TitanCharString ret_val = new TitanCharString(stringValue);
		return ret_val.operator_concatenate(other_value);
	}

	@Override
	/** {@inheritDoc} */
	public void encode(final TTCN_Typedescriptor p_td, final TTCN_Buffer p_buf, final coding_type p_coding, final int flavour) {
		switch (p_coding) {
		case CT_RAW:
			final TTCN_EncDec_ErrorContext errorContext = new TTCN_EncDec_ErrorContext("While RAW-encoding type '%s': ", p_td.name);
			if (p_td.raw == null) {
				TTCN_EncDec_ErrorContext.error_internal("No RAW descriptor available for type '%s'.", p_td.name);
			}

			final RAW_enc_tr_pos tree_position = new RAW_enc_tr_pos(0, null);
			final RAW_enc_tree root = new RAW_enc_tree(true, null, tree_position, 1, p_td.raw);
			RAW_encode(p_td, root);
			root.put_to_buf(p_buf);

			errorContext.leave_context();
			break;

		default:
			throw new TtcnError(MessageFormat.format("Unknown coding method requested to encode type `{0}''", p_td.name));
		}
	}

	@Override
	/** {@inheritDoc} */
	public void decode(final TTCN_Typedescriptor p_td, final TTCN_Buffer p_buf, final coding_type p_coding, final int flavour) {
		switch (p_coding) {
		case CT_RAW:
			final TTCN_EncDec_ErrorContext errorContext = new TTCN_EncDec_ErrorContext("While RAW-decoding type '%s': ", p_td.name);
			if (p_td.raw == null) {
				TTCN_EncDec_ErrorContext.error_internal("No RAW descriptor available for type '%s'.", p_td.name);
			}
			raw_order_t order;
			switch (p_td.raw.top_bit_order) {
			case TOP_BIT_LEFT:
				order = raw_order_t.ORDER_LSB;
				break;
			case TOP_BIT_RIGHT:
			default:
				order = raw_order_t.ORDER_MSB;
				break;
			}
			if (RAW_decode(p_td, p_buf, p_buf.get_len() * 8, order) < 0) {
				TTCN_EncDec_ErrorContext.error(TTCN_EncDec.error_type.ET_INCOMPL_MSG, "Can not decode type '%s', because invalid or incomplete message was received", p_td.name);
			}

			errorContext.leave_context();
			break;

		default:
			throw new TtcnError(MessageFormat.format("Unknown coding method requested to decode type `{0}''", p_td.name));
		}
	}

	@Override
	/** {@inheritDoc} */
	public int RAW_encode(final TTCN_Typedescriptor p_td, final RAW_enc_tree myleaf) {
		int bl = val_ptr.length() * 8; // bit length
		int align_length = p_td.raw.fieldlength > 0 ? p_td.raw.fieldlength - bl : 0;
		final TTCN_EncDec_ErrorContext errorContext = new TTCN_EncDec_ErrorContext();
		if (!is_bound()) {
			TTCN_EncDec_ErrorContext.error(error_type.ET_UNBOUND, "Encoding an unbound value.");
		}
		if ((bl + align_length) < val_ptr.length() * 8) {
			TTCN_EncDec_ErrorContext.error(error_type.ET_LEN_ERR, "There is no sufficient bits to encode: '%s'", p_td.name);
			bl = p_td.raw.fieldlength;
			align_length = 0;
		}
		if (p_td.raw.fieldlength >= 0) {
			myleaf.data_array = val_ptr.toString().toCharArray();
		} else {
			// NULL terminated
			myleaf.data_array = new char[val_ptr.length() + 1];
			for (int i = 0; i < val_ptr.length(); i++) {
				myleaf.data_array[i] = val_ptr.charAt(i);
			}
			myleaf.data_array[val_ptr.length()] = '0';
			bl += 8;
		}
		if (p_td.raw.endianness == raw_order_t.ORDER_MSB) {
			myleaf.align = -align_length;
		} else {
			myleaf.align = align_length;
		}
		errorContext.leave_context();

		return myleaf.length = bl + align_length;
	}

	@Override
	/** {@inheritDoc} */
	public int RAW_decode(final TTCN_Typedescriptor p_td, final TTCN_Buffer buff, final int limit, final raw_order_t top_bit_ord) {
		return RAW_decode(p_td, buff, limit, top_bit_ord, false, -1, true, null);
	}

	@Override
	/** {@inheritDoc} */
	public int RAW_decode(final TTCN_Typedescriptor p_td, final TTCN_Buffer buff, int limit, final raw_order_t top_bit_ord, final boolean no_err, final int sel_field, final boolean first_call, final RAW_Force_Omit force_omit) {
		final int prepaddlength = buff.increase_pos_padd(p_td.raw.prepadding);
		limit -= prepaddlength;
		int decode_length = p_td.raw.fieldlength <= 0 ? (limit / 8) * 8 : p_td.raw.fieldlength;
		final TTCN_EncDec_ErrorContext errorContext = new TTCN_EncDec_ErrorContext();
		if (decode_length > limit || decode_length > buff.unread_len_bit()) {
			if (no_err) {
				errorContext.leave_context();
				return -TTCN_EncDec.error_type.ET_LEN_ERR.ordinal();
			}
			TTCN_EncDec_ErrorContext.error(error_type.ET_LEN_ERR, "There is not enough bits in the buffer to decode type '%s.'", p_td.name);
			decode_length = ((limit > buff.unread_len_bit() ? buff.unread_len_bit() : limit) / 8) * 8;
		}

		final RAW_coding_par cp = new RAW_coding_par();
		boolean orders = false;
		if (p_td.raw.bitorderinoctet == raw_order_t.ORDER_MSB) {
			orders = true;
		}
		if (p_td.raw.bitorderinfield == raw_order_t.ORDER_MSB) {
			orders = !orders;
		}
		cp.bitorder = orders ? raw_order_t.ORDER_MSB : raw_order_t.ORDER_LSB;
		orders = false;
		if (p_td.raw.byteorder == raw_order_t.ORDER_MSB) {
			orders = true;
		}
		if (p_td.raw.bitorderinfield == raw_order_t.ORDER_MSB) {
			orders = !orders;
		}
		cp.byteorder = orders ? raw_order_t.ORDER_MSB : raw_order_t.ORDER_LSB;
		cp.fieldorder = p_td.raw.fieldorder;
		cp.hexorder = raw_order_t.ORDER_LSB;
		if (p_td.raw.fieldlength >= 0) {
			clean_up();
			val_ptr = new StringBuilder(decode_length / 8);
			final char[] val_tmp = new char[decode_length / 8];
			buff.get_b(decode_length, val_tmp, cp, top_bit_ord);
			val_ptr.append(val_tmp);
		} else {
			// NULL terminated
			final TTCN_Buffer temp_buff = new TTCN_Buffer();
			//TODO: can be more simple
			char[] ch = new char[1];
			ch[0] = 0;
			int str_len = 0;
			int null_found = 0;
			while (str_len < decode_length) {
				buff.get_b(8, ch, cp, top_bit_ord);
				if (ch[0] == 0) {
					null_found = 1;
					break;
				}
				temp_buff.put_c(ch[0]);
				str_len += 8;
			}
			if (null_found == 0) {
				return -1;
			}
			temp_buff.get_string(this);
			decode_length = str_len + 8;
		}
		if (p_td.raw.length_restrition != -1 && decode_length > p_td.raw.length_restrition) {
			if (p_td.raw.endianness == raw_order_t.ORDER_MSB) {
				val_ptr.insert(0, val_ptr.toString().toCharArray(), 0, val_ptr.length());
			}
		}
		decode_length += buff.increase_pos_padd(p_td.raw.padding);
		errorContext.leave_context();

		return decode_length + prepaddlength;
	}

	protected boolean set_param_internal(final Module_Parameter param, final boolean allow_pattern, final AtomicBoolean is_nocase_pattern) {
		boolean is_pattern = false;
		param.basic_check(basic_check_bits_t.BC_VALUE.getValue()|basic_check_bits_t.BC_LIST.getValue(), "charstring value");
		final Module_Parameter mp = param;
		switch (mp.get_type()) {
		case MP_Universal_Charstring:
		case MP_Charstring:
			switch (param.get_operation_type()) {
			case OT_ASSIGN:
				clean_up();
				// no break
			case OT_CONCAT: {
				// The universal charstring will decode the string value if it is UTF-8 encoded
				final TitanUniversalCharString ucs = new TitanUniversalCharString();
				ucs.set_param(mp);
				if (ucs.charstring) {
					// No special characters were found
					if (is_bound()) {
						operator_concatenate(ucs);
					} else {
						operator_assign(ucs);
					}
				} else {
					// Special characters found -> check if the UTF-8 decoding resulted in any multi-byte characters
					for (int i = 0; i < ucs.val_ptr.size(); ++i) {
						final TitanUniversalChar uc = ucs.val_ptr.get(i);
						if (0 != uc.getUc_group() ||
								0 != uc.getUc_plane() ||
								0 != uc.getUc_row()) {
							param.error("Type mismatch: a charstring value without multi-octet characters was expected.");
						}
					}
					final TitanCharString new_cs = new TitanCharString(ucs);
					if (is_bound()) {
						operator_concatenate(new_cs);
					} else {
						operator_assign(new_cs);
					}
				}
				break; }
			default:
				throw new TtcnError("Internal error: TitanCharString.set_param()");
			}
			break;
		case MP_Expression:
			if (mp.get_expr_type() == expression_operand_t.EXPR_CONCATENATE) {
				// only allow string patterns for the first operand
				final TitanCharString operand1 = new TitanCharString();
				final TitanCharString operand2 = new TitanCharString();
				is_pattern = operand1.set_param_internal(mp.get_operand1(), allow_pattern,
						is_nocase_pattern);
				operand2.set_param(mp.get_operand2());
				if (param.get_operation_type() == operation_type_t.OT_CONCAT) {
					operator_concatenate(operand1);
					operator_concatenate(operand2);
				}
				else {
					operator_assign(operand1);
					operator_concatenate(operand2);
				}
			}
			else {
				param.expr_type_error("a charstring");
			}
			break;
		case MP_Pattern:
			if (allow_pattern) {
				operator_assign(new TitanCharString(mp.get_pattern()));
				is_pattern = true;
				if (is_nocase_pattern != null) {
					is_nocase_pattern.set(mp.get_nocase());
				}
				break;
			}
			// else fall through
		default:
			param.type_error("charstring value");
			break;
		}
		return is_pattern;
	}

	protected boolean set_param_internal(final Module_Parameter param, final boolean allow_pattern) {
		return set_param_internal(param, allow_pattern, null);
	}

	@Override
	/** {@inheritDoc} */
	public void set_param(final Module_Parameter param) {
		set_param_internal(param, false);
	}
}