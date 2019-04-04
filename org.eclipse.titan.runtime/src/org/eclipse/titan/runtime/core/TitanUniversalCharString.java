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

import org.eclipse.titan.runtime.core.Param_Types.Module_Param_Expression;
import org.eclipse.titan.runtime.core.Param_Types.Module_Parameter;
import org.eclipse.titan.runtime.core.Param_Types.Module_Parameter.basic_check_bits_t;
import org.eclipse.titan.runtime.core.Param_Types.Module_Parameter.expression_operand_t;
import org.eclipse.titan.runtime.core.Param_Types.Module_Parameter.operation_type_t;
import org.eclipse.titan.runtime.core.Param_Types.Module_Parameter.type_t;
import org.eclipse.titan.runtime.core.RAW.RAW_Force_Omit;
import org.eclipse.titan.runtime.core.RAW.RAW_enc_tr_pos;
import org.eclipse.titan.runtime.core.RAW.RAW_enc_tree;
import org.eclipse.titan.runtime.core.TTCN_EncDec.coding_type;
import org.eclipse.titan.runtime.core.TTCN_EncDec.error_type;
import org.eclipse.titan.runtime.core.TTCN_EncDec.raw_order_t;
import org.eclipse.titan.runtime.core.TitanCharString.CharCoding;

/**
 * TTCN-3 Universal_charstring
 * @author Arpad Lovassy
 * @author Farkas Izabella Ingrid
 * @author Andrea Palfi
 */
public class TitanUniversalCharString extends Base_Type {

	/** Internal data */
	List<TitanUniversalChar> val_ptr;

	/** Character string values are stored in an optimal way */
	StringBuilder cstr;

	/**
	 * true, if cstr is used <br>
	 * false, if val_ptr is used
	 */
	boolean charstring;

	/**
	 * Initializes to unbound value.
	 * */
	public TitanUniversalCharString() {
		super();
	}

	/**
	 * Initializes to a given value.
	 *
	 * @param otherValue
	 *                the value to initialize to.
	 * */
	public TitanUniversalCharString(final TitanUniversalChar otherValue) {
		if (!otherValue.is_char()) {
			val_ptr = new ArrayList<TitanUniversalChar>();
			val_ptr.add(otherValue);
			charstring = false;
		} else {
			cstr = new StringBuilder();
			cstr.append(otherValue.getUc_cell());
			charstring = true;
		}
	}

	public TitanUniversalCharString(final char uc_group, final char uc_plane, final char uc_row,  final char uc_cell) {
		final TitanUniversalChar uc = new TitanUniversalChar(uc_group, uc_plane, uc_row, uc_cell);
		if (uc.is_char()) {
			cstr = new StringBuilder();
			cstr.append(uc_cell);
			charstring = true;
		} else {
			val_ptr = new ArrayList<TitanUniversalChar>();
			val_ptr.add(uc);
			charstring = false;
		}
	}

	/**
	 * Initializes to a given value.
	 *
	 * @param otherValue
	 *                the value to initialize to.
	 * */
	public TitanUniversalCharString(final List<TitanUniversalChar> otherValue) {
		val_ptr = copy_list(otherValue);
		charstring = false;
	}

	public TitanUniversalCharString(final TitanUniversalChar[] otherValue) {
		val_ptr = new ArrayList<TitanUniversalChar>(otherValue.length);
		for (int i = 0; i < otherValue.length; i++) {
			val_ptr.add(otherValue[i]);
		}

		charstring = false;
	}

	/**
	 * Initializes to a given value.
	 *
	 * @param otherValue
	 *                the value to initialize to.
	 * */
	public TitanUniversalCharString(final String otherValue) {
		cstr = new StringBuilder(otherValue);
		charstring = true;
	}

	/**
	 * Initializes to a given value.
	 *
	 * @param otherValue
	 *                the value to initialize to.
	 * */
	public TitanUniversalCharString(final StringBuilder otherValue) {
		cstr = new StringBuilder(otherValue);
		charstring = true;
	}

	/**
	 * Initializes to a given value.
	 *
	 * @param otherValue
	 *                the value to initialize to.
	 * */
	public TitanUniversalCharString(final TitanCharString otherValue) {
		otherValue.must_bound("Copying an unbound charstring value.");

		cstr = new StringBuilder(otherValue.get_value());
		charstring = true;
	}

	/**
	 * Initializes to a given value.
	 *
	 * @param otherValue
	 *                the value to initialize to.
	 * */
	public TitanUniversalCharString(final TitanUniversalCharString otherValue) {
		otherValue.must_bound("Copying an unbound universal charstring value.");

		charstring = otherValue.charstring;
		if (charstring) {
			cstr = new StringBuilder(otherValue.cstr);
		} else {
			val_ptr = copy_list(otherValue.val_ptr);
		}
	}

	/**
	 * Initializes to a given value.
	 *
	 * @param otherValue
	 *                the value to initialize to.
	 * */
	public TitanUniversalCharString(final TitanUniversalCharString_Element otherValue) {
		otherValue.must_bound("Initialization of a universal charstring with an unbound universal charstring element.");

		if (otherValue.get_char().is_char()) {
			cstr = new StringBuilder();
			cstr.append(otherValue.get_char().getUc_cell());
			charstring = true;
		} else {
			val_ptr = new ArrayList<TitanUniversalChar>();
			val_ptr.add(otherValue.get_char());
			charstring = false;
		}
	}

	private static List<TitanUniversalChar> copy_list(final List<TitanUniversalChar> uList) {
		if (uList == null) {
			return null;
		}

		final List<TitanUniversalChar> clonedList = new ArrayList<TitanUniversalChar>(uList.size());
		for (final TitanUniversalChar uc : uList) {
			clonedList.add(new TitanUniversalChar(uc));
		}

		return clonedList;
	}

	/**
	 * Returns the internal data storage of this universal charstring.
	 * <p>
	 * Please note, this code is for internal use only.
	 * Users are not recommended to use this function.
	 * As such it is also not part of the public API
	 *  and might change without notice.
	 *
	 * <p>
	 * operator universal char*() in the core
	 *
	 * @return the internal representation of the universal chartstring.
	 * */
	public List<TitanUniversalChar> get_value() {
		must_bound("Casting an unbound universal charstring value to const universal_char*.");

		if (charstring) {
			convert_cstr_to_uni();
		}

		return val_ptr;
	}

	/**
	 * Overwrites the internal data storage of this universal chartstring.
	 * Takes ownership of the provided data.
	 * <p>
	 * Please note, this code is for internal use only.
	 * Users are not recommended to use this function.
	 * As such it is also not part of the public API
	 *  and might change without notice.
	 *
	 * <p>
	 * operator universal char*() in the core
	 *
	 * @param other_value the internal representation of the universal chartstring.
	 * */
	public void set_value(final List<TitanUniversalChar> other_value) {
		if (other_value != null) {
			val_ptr = other_value;
			cstr = null;
			charstring = false;
		}
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
	public TitanUniversalCharString operator_assign(final TitanUniversalCharString otherValue) {
		otherValue.must_bound("Assignment of an unbound universal charstring value.");

		if (otherValue != this) {
			val_ptr = otherValue.val_ptr;
			cstr = otherValue.cstr;
			charstring = otherValue.charstring;
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
	public TitanUniversalCharString operator_assign(final TitanUniversalCharString_Element otherValue) {
		otherValue.must_bound("Assignment of an unbound universal charstring element to a universal charstring.");

		if (otherValue.get_char().is_char()) {
			cstr = new StringBuilder();
			cstr.append(otherValue.get_char().getUc_cell());
			val_ptr = null;
			charstring = true;
		} else {
			val_ptr = new ArrayList<TitanUniversalChar>();
			val_ptr.add(otherValue.get_char());
			cstr = null;
			charstring = false;
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
	public TitanUniversalCharString operator_assign(final TitanCharString otherValue) {
		otherValue.must_bound("Assignment of an unbound charstring value.");

		if (!charstring) {
			clean_up();
			charstring = true;
		}
		cstr = new StringBuilder(otherValue.get_value());

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
	public TitanUniversalCharString operator_assign(final TitanCharString_Element otherValue) {
		otherValue.must_bound("Assignment of an unbound charstring element to a charstring.");

		if (!charstring) {
			clean_up();
			charstring = true;
		}
		cstr = new StringBuilder();
		cstr.append(otherValue.get_char());

		return this;
	}

	// originally operator=
	@Override
	public TitanUniversalCharString operator_assign(final Base_Type otherValue) {
		if (otherValue instanceof TitanUniversalCharString) {
			return operator_assign((TitanUniversalCharString) otherValue);
		} else if (otherValue instanceof TitanCharString) {
			return operator_assign((TitanCharString) otherValue);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to universal charstring", otherValue));
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
	public TitanUniversalCharString operator_assign(final TitanUniversalChar otherValue) {
		clean_up();
		if (otherValue.is_char()) {
			charstring = true;
			cstr = new StringBuilder();
			cstr.append(otherValue.getUc_cell());
		} else {
			charstring = false;
			val_ptr = new ArrayList<TitanUniversalChar>();
			val_ptr.add(otherValue);
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
	public TitanUniversalCharString operator_assign(final char[] otherValue) {
		charstring = true;
		cstr = new StringBuilder();
		for (int i = 0; i < otherValue.length; ++i) {
			cstr.append(otherValue[i]);
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
	public TitanUniversalCharString operator_assign(final String otherValue) {
		charstring = true;
		cstr = new StringBuilder();
		for (int i = 0; i < otherValue.length(); ++i) {
			cstr.append(otherValue.charAt(i));
		}

		return this;
	}

	@Override
	public boolean is_bound() {
		return charstring ? cstr != null : val_ptr != null;
	}

	@Override
	public boolean is_present() {
		return is_bound();
	};

	@Override
	public boolean is_value() {
		return is_bound();
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
		must_bound("Performing lengthof operation on an unbound universal charstring value.");

		if (charstring) {
			return new TitanInteger(cstr.length());
		}

		return new TitanInteger(val_ptr.size());
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
		must_bound("The left operand of comparison is an unbound universal charstring value.");
		otherValue.must_bound("The right operand of comparison is an unbound universal charstring value.");

		if (charstring) {
			if (otherValue.charstring) {
				return cstr.toString().equals(otherValue.cstr.toString());
			} else {
				if (cstr.length() != otherValue.val_ptr.size()) {
					return false;
				}

				for (int i = 0; i < otherValue.val_ptr.size(); ++i) {
					if (!otherValue.val_ptr.get(i).is_char() || otherValue.val_ptr.get(i).getUc_cell() != cstr.charAt(i)){
						return false;
					}
				}

				return true;
			}

		}
		if (val_ptr.size() != otherValue.val_ptr.size()) {
			return false;
		}

		for (int i = 0; i < val_ptr.size(); ++i) {
			if (!val_ptr.get(i).operator_equals(otherValue.val_ptr.get(i))) {
				return false;
			}
		}

		return true;
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
		must_bound("The left operand of comparison is an unbound universal charstring value.");
		otherValue.must_bound("The right operand of comparison is an unbound universal charstring element.");

		if (charstring) {
			return otherValue.get_char().is_char() && otherValue.get_char().getUc_cell() == cstr.charAt(0);
		}
		if (val_ptr.size() != 1) {
			return false;
		}

		return val_ptr.get(0).operator_equals(otherValue.get_char());
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
		must_bound("The left operand of comparison is an unbound universal charstring value.");
		otherValue.must_bound("The right operand of comparison is an unbound charstring value.");

		if (charstring) {
			return otherValue.get_value().toString().equals(cstr.toString());
		}
		if (val_ptr.size() != otherValue.lengthof().get_int()) {
			return false;
		}

		for (int i = 0; i < val_ptr.size(); ++i) {
			if (val_ptr.get(i).getUc_group() != 0 || val_ptr.get(i).getUc_plane() != 0 || val_ptr.get(i).getUc_row() != 0
					|| val_ptr.get(i).getUc_cell() != otherValue.get_value().charAt(i)) {
				return false;
			}
		}

		return true;
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
		must_bound("The left operand of comparison is an unbound universal charstring value.");
		otherValue.must_bound("The right operand of comparison is an unbound charstring element.");

		if (charstring) {
			return cstr.charAt(0) == otherValue.get_char();
		}
		if (val_ptr.size() != 1) {
			return false;
		}

		return val_ptr.get(0).is_char() && val_ptr.get(0).getUc_cell() == otherValue.get_char();
	}

	@Override
	public boolean operator_equals(final Base_Type otherValue) {
		if (otherValue instanceof TitanUniversalCharString) {
			return operator_equals((TitanUniversalCharString) otherValue);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to universal charstring", otherValue));
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
	public boolean operator_equals(final TitanUniversalChar otherValue) {
		must_bound("The left operand of comparison is an unbound universal charstring value.");

		if (charstring) {
			return cstr.length() == 1 && otherValue.getUc_group() == 0 &&
					otherValue.getUc_plane() == 0 && otherValue.getUc_row() == 0 &&
					cstr.charAt(0) == otherValue.getUc_cell();
		}
		if (val_ptr.size() != 1) {
			return false;
		}

		return val_ptr.get(0).operator_equals(otherValue);
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
		must_bound("The left operand of comparison is an unbound universal charstring value.");

		if (charstring) {
			return cstr.toString().equals(otherValue);
		}
		if (val_ptr.size() != otherValue.length()) {
			return false;
		}
		for (int i = 0; i < val_ptr.size(); ++i) {
			if (val_ptr.get(i).getUc_group() != 0 || val_ptr.get(i).getUc_plane() != 0 || val_ptr.get(i).getUc_row() != 0
					|| val_ptr.get(i).getUc_cell() != otherValue.charAt(i)) {
				return false;
			}
		}

		return true;
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
	public boolean operator_not_equals(final TitanUniversalCharString otherValue) {
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
	public boolean operator_not_equals(final TitanUniversalCharString_Element otherValue) {
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
	public boolean operator_not_equals(final TitanCharString otherValue) {
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
	public boolean operator_not_equals(final TitanCharString_Element otherValue) {
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
	public boolean operator_not_equals(final Base_Type otherValue) {
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
	public boolean operator_not_equals(final TitanUniversalChar otherValue) {
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
	public boolean operator_not_equals(final String otherValue) {
		return !operator_equals(otherValue);
	}

	/**
	 * Concatenates the current universal charstring with the universal
	 * charstring received as a parameter.
	 *
	 * operator+ in the core.
	 *
	 * @param other_value
	 *                the other value to concatenate with.
	 * @return the new universal charstring representing the concatenated
	 *         value.
	 * */
	public TitanUniversalCharString operator_concatenate(final TitanUniversalChar other_value) {
		must_bound("The left operand of concatenation is an unbound universal charstring value.");

		if (charstring) {
			if (other_value.is_char()) {
				return new TitanUniversalCharString(new StringBuilder(cstr).append(other_value.getUc_cell()));
			} else {
				final List<TitanUniversalChar> ulist = new ArrayList<TitanUniversalChar>();
				for (int i = 0; i < cstr.length(); ++i) {
					final TitanUniversalChar uc = new TitanUniversalChar((char) 0, (char) 0, (char) 0, cstr.charAt(i));
					ulist.add(uc);
				}
				ulist.add(other_value);
				final TitanUniversalCharString ret_val = new TitanUniversalCharString();
				ret_val.set_value(ulist);
				return ret_val;
			}
		}

		final TitanUniversalCharString ret_val = new TitanUniversalCharString(val_ptr);
		ret_val.val_ptr.add(other_value);
		return ret_val;
	}

	/**
	 * Concatenates the current universal charstring with the string 
	 * eceived as a parameter.
	 *
	 * operator+ in the core.
	 *
	 * @param other_value
	 *                the other value to concatenate with.
	 * @return the new universal charstring representing the concatenated
	 *         value.
	 * */
	public TitanUniversalCharString operator_concatenate(final String other_value) {
		must_bound("The left operand of concatenation is an unbound universal charstring value.");

		int other_len;
		if (other_value == null) {
			other_len = 0;
		} else {
			other_len = other_value.length();
		}
		if (other_len == 0) {
			return new TitanUniversalCharString(this);
		}
		if (charstring) {
			return new TitanUniversalCharString(new StringBuilder(cstr).append(other_value));
		}
		final TitanUniversalCharString ret_val = new TitanUniversalCharString(val_ptr);
		for (int i = 0; i < other_len; i++) {
			final TitanUniversalChar uc = new TitanUniversalChar((char) 0, (char) 0, (char) 0, other_value.charAt(i));
			ret_val.val_ptr.add(uc);
		}
		return ret_val;
	}

	/**
	 * Concatenates the current universal charstring with the charstring
	 * received as a parameter.
	 *
	 * operator+ in the core.
	 *
	 * @param other_value
	 *                the other value to concatenate with.
	 * @return the new universal charstring representing the concatenated
	 *         value.
	 * */
	public TitanUniversalCharString operator_concatenate(final TitanCharString other_value) {
		must_bound("The left operand of concatenation is an unbound universal charstring value.");
		other_value.must_bound("The right operand of concatenation is an unbound charstring value.");

		return operator_concatenate(other_value.get_value().toString());
	}

	/**
	 * Concatenates the current universal charstring with the charstring
	 * received as a parameter.
	 *
	 * operator+ in the core.
	 *
	 * @param other_value
	 *                the other value to concatenate with.
	 * @return the new universal charstring representing the concatenated
	 *         value.
	 * */
	public TitanUniversalCharString operator_concatenate(final TitanCharString_Element other_value) {
		must_bound("The left operand of concatenation is an unbound universal charstring value.");
		other_value.must_bound("The right operand of concatenation is an unbound charstring element.");

		if (charstring) {
			return new TitanUniversalCharString(new StringBuilder(cstr).append(other_value.get_char()));
		}

		final TitanUniversalCharString ret_val = new TitanUniversalCharString(val_ptr);
		final TitanUniversalChar uc = new TitanUniversalChar((char) 0, (char) 0, (char) 0, other_value.get_char());
		ret_val.val_ptr.add(uc);
		return ret_val;
	}


	/**
	 * Concatenates the current universal charstring with the universal
	 * charstring received as a parameter.
	 *
	 * operator+ in the core.
	 *
	 * @param other_value
	 *                the other value to concatenate with.
	 * @return the new universal charstring representing the concatenated
	 *         value.
	 * */
	public TitanUniversalCharString operator_concatenate(final TitanUniversalCharString other_value) {
		must_bound("The left operand of concatenation is an unbound universal charstring value.");
		other_value.must_bound("The right operand of concatenation is an unbound universal charstring value.");

		if (charstring) {
			if (cstr.length() == 0) {
				return new TitanUniversalCharString(other_value);
			}
			if (other_value.charstring) {
				if (other_value.cstr.length() == 0) {
					return new TitanUniversalCharString(this);
				}

				return new TitanUniversalCharString(new StringBuilder(cstr).append(other_value.cstr));
			} else {
				if (other_value.val_ptr.isEmpty()) {
					return new TitanUniversalCharString(this);
				}
				final List<TitanUniversalChar> ulist = new ArrayList<TitanUniversalChar>();
				for (int i = 0; i < cstr.length(); i++) {
					final TitanUniversalChar uc = new TitanUniversalChar((char) 0, (char) 0, (char) 0, cstr.charAt(i));
					ulist.add(uc);
				}
				ulist.addAll(other_value.val_ptr);
				final TitanUniversalCharString ret_val = new TitanUniversalCharString();
				ret_val.set_value(ulist);
				return ret_val;
			}
		} else {
			if (other_value.charstring) {
				final TitanUniversalCharString ret_val = new TitanUniversalCharString(val_ptr);
				final StringBuilder cs = other_value.cstr;
				final int cslen = cs.length();
				for (int i = 0; i < cslen; i++) {
					final TitanUniversalChar uc = new TitanUniversalChar((char) 0, (char) 0, (char) 0, cs.charAt(i));
					ret_val.get_value().add(uc);
				}
				return ret_val;
			} else {
				if (val_ptr.isEmpty()) {
					return new TitanUniversalCharString(other_value);
				}
				if (other_value.val_ptr.isEmpty()) {
					return new TitanUniversalCharString(this);
				}
				final TitanUniversalCharString ret_val = new TitanUniversalCharString(val_ptr);
				ret_val.get_value().addAll(other_value.val_ptr);
				return ret_val;
			}
		}
	}

	/**
	 * Concatenates the current universal charstring with the universal
	 * charstring received as a parameter.
	 *
	 * operator+ in the core.
	 *
	 * @param other_value
	 *                the other value to concatenate with.
	 * @return the new universal charstring representing the concatenated
	 *         value.
	 * */
	public TitanUniversalCharString operator_concatenate(final TitanUniversalCharString_Element other_value) {
		must_bound("The left operand of concatenation is an unbound universal charstring value.");
		other_value.must_bound("The right operand of concatenation is an unbound universal charstring element.");

		if (charstring) {
			return new TitanUniversalCharString(new StringBuilder(cstr).append(other_value.get_char().getUc_cell()));
		}

		final TitanUniversalCharString ret_val = new TitanUniversalCharString(val_ptr);
		ret_val.val_ptr.add(other_value.get_char());
		return ret_val;
	}

	@Override
	public void clean_up() {
		val_ptr = null;
		cstr = null;
	}

	/**
	 * @return number of digits
	 */
	private int size() {
		return charstring ? cstr.length() : val_ptr.size();
	}

	/**
	 * Gives access to the given element. Indexing begins from zero.
	 * Over-indexing by 1 extends the universal charstring.
	 *
	 * operator[] in the core.
	 *
	 * @param index_value
	 *            the index of the element to return.
	 * @return the element at the specified position in this universal charstring
	 * */
	public TitanUniversalCharString_Element get_at(final int index_value) {
		if (!is_bound() && index_value == 0) {
			if (charstring) {
				cstr = new StringBuilder();
			} else {
				val_ptr = new ArrayList<TitanUniversalChar>();
			}
			return new TitanUniversalCharString_Element(false, this, 0);
		} else {
			must_bound("Accessing an element of an unbound universal charstring value.");

			if (index_value < 0) {
				throw new TtcnError("Accessing an universal charstring element using a negative index (" + index_value + ").");
			}

			final int n_nibbles = size();
			if (index_value > n_nibbles) {
				throw new TtcnError("Index overflow when accessing a universal charstring element: The index is " + index_value +
						", but the string has only " + n_nibbles + " characters.");
			}
			if (index_value == n_nibbles) {
				if (charstring) {
					cstr.append((char) 0);
				} else {
					val_ptr.add(new TitanUniversalChar((char) 0, (char) 0, (char) 0, (char) 0));
				}
				return new TitanUniversalCharString_Element(false, this, index_value);
			} else {
				return new TitanUniversalCharString_Element(true, this, index_value);
			}
		}
	}

	/**
	 * Gives access to the given element. Indexing begins from zero.
	 * Over-indexing by 1 extends the universal charstring.
	 *
	 * operator[] in the core.
	 *
	 * @param index_value
	 *            the index of the element to return.
	 * @return the element at the specified position in this universal charstring
	 * */
	public TitanUniversalCharString_Element get_at(final TitanInteger index_value) {
		index_value.must_bound("Indexing a universal charstring value with an unbound integer value.");

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
	 * @return the element at the specified position in this universal charstring
	 * */
	public TitanUniversalCharString_Element constGet_at(final int index_value) {
		must_bound("Accessing an element of an unbound universal charstring value.");

		if (index_value < 0) {
			throw new TtcnError("Accessing an universal charstring element using a negative index (" + index_value + ").");
		}

		final int n_nibbles = charstring ? cstr.length() : val_ptr.size();
		if (index_value >= n_nibbles) {
			throw new TtcnError("Index overflow when accessing a universal charstring element: The index is " + index_value +
					", but the string has only " + n_nibbles + " characters.");
		}

		return new TitanUniversalCharString_Element(true, this, index_value);
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
	 * @return the element at the specified position in this universal charstring
	 * */
	public TitanUniversalCharString_Element constGet_at(final TitanInteger index_value) {
		index_value.must_bound("Indexing a universal charstring value with an unbound integer value.");

		return constGet_at(index_value.get_int());
	}

	public static boolean is_printable(final TitanUniversalChar uchar) {
		return uchar.getUc_group() == 0 && uchar.getUc_plane() == 0 && uchar.getUc_row() == 0 && TTCN_Logger.is_printable(uchar.getUc_cell());
	}

	private static enum States {
		INIT, PCHAR, UCHAR;
	}

	@Override
	public void log(){
		if (charstring) {
			new TitanCharString(cstr).log();
			return;
		}
		if (val_ptr != null) {
			States state = States.INIT;
			final StringBuilder buffer = new StringBuilder();
			for (int i = 0; i < val_ptr.size(); i++) {
				final TitanUniversalChar uchar = val_ptr.get(i);
				if (is_printable(uchar)) {
					switch (state) {
					case UCHAR:
						buffer.append(" & ");
					case INIT:
						buffer.append('\"');
					case PCHAR:
						TTCN_Logger.log_char_escaped(uchar.getUc_cell(), buffer);
						break;
					}
					state = States.PCHAR;
				} else {
					switch (state) {
					case PCHAR:
						buffer.append('\"');
					case UCHAR:
						buffer.append(" & ");
					case INIT:
						buffer.append(MessageFormat.format("char({0}, {1}, {2}, {3})", (int)uchar.getUc_group(), (int)uchar.getUc_plane(), (int)uchar.getUc_row(), (int)uchar.getUc_cell()));
						break;
					}
					state = States.UCHAR;
				}
			}
			switch (state) {
			case INIT:
				buffer.append("\"\"");
				break;
			case PCHAR:
				buffer.append('\"');
				break;
			default:
				break;
			}
			TTCN_Logger.log_event_str(buffer.toString());

		} else {
			TTCN_Logger.log_event_unbound();
		}

	}

	protected static TitanUniversalCharString from_UTF8_buffer(final TTCN_Buffer p_buff) {
		final TitanOctetString os = new TitanOctetString();
		p_buff.get_string(os);
		if (new TitanCharString("UTF-8").equals(AdditionalFunctions.get_stringencoding(os))) {
			final TitanUniversalCharString ret = new TitanUniversalCharString();
			ret.decode_utf8(p_buff.get_data(), CharCoding.UTF_8, false);
			return ret;
		} else {
			if (p_buff.get_data() != null) {
				return new TitanUniversalCharString(String.valueOf(p_buff.get_data()));
			} else {
				return new TitanUniversalCharString("");
			}
		}
	}

	@Override
	public void set_param(final Module_Parameter param) {
		set_param_internal(param, false);
	}

	/** An extended version of set_param(), which also accepts string patterns if
	 * the second parameter is set (needed by TitanUniversalCharString_template to
	 * concatenate string patterns). 
	 * @return true, if the module parameter was a string pattern, otherwise false */
	public boolean set_param_internal(final Module_Parameter param, final boolean allow_pattern) {
		return set_param_internal(param, allow_pattern, false);
	}

	public boolean set_param_internal(final Module_Parameter param, final boolean allow_pattern, final boolean is_nocase_pattern) {
		boolean is_pattern = false;
		param.basic_check(basic_check_bits_t.BC_VALUE.getValue()|basic_check_bits_t.BC_LIST.getValue(), "universal charstring value");
		switch (param.get_type()) {
		case MP_Charstring:
			switch (param.get_operation_type()) {
			case OT_ASSIGN:
				clean_up();
				//no break
			case OT_CONCAT:
				final TTCN_Buffer buff = new TTCN_Buffer();
				buff.put_s(((String)param.get_string_data()).toCharArray());
				if (is_bound()) {
					this.operator_assign(this.operator_concatenate(from_UTF8_buffer(buff)));
				} else {
					this.operator_assign(from_UTF8_buffer(buff));
				}
				break;
			default:
				throw new TtcnError("Internal error: TitanUniversalCharString.set_param()");
			}
			break;
		case MP_Universal_Charstring:
			switch (param.get_operation_type()) {
			case OT_ASSIGN:
				clean_up();
				//no break
			case OT_CONCAT:
				if (is_bound()) {
					this.operator_assign(this.operator_concatenate((TitanUniversalCharString)param.get_string_data()));
				} else {
					this.operator_assign((TitanUniversalCharString)param.get_string_data());
				}
				break;
			default:
				throw new TtcnError("Internal error: TitanUniversalCharString.set_param()");
			}
			break;
		case MP_Expression:
			if (param.get_expr_type() == expression_operand_t.EXPR_CONCATENATE) {
				final TitanUniversalCharString operand1 = new TitanUniversalCharString();
				final TitanUniversalCharString operand2 = new TitanUniversalCharString();
				is_pattern = operand1.set_param_internal(param.get_operand1(), allow_pattern, is_nocase_pattern);
				operand2.set_param(param.get_operand2());
				if (param.get_operation_type() == operation_type_t.OT_CONCAT) {
					this.operator_assign(this.operator_concatenate(operand1).operator_concatenate(operand2));
				} else {
					this.operator_assign(operand1.operator_concatenate(operand2));
				}
			} else {
				param.expr_type_error("a universal charstring");
			}
			break;
		case MP_Pattern:
			if (allow_pattern) {
				//TODO: need to check later
				this.operator_assign(new TitanCharString(param.get_pattern()));
				is_pattern = true;
				if (param.parent.get_type() == type_t.MP_Expression) {
					((Module_Param_Expression)(param.parent)).set_nocase(param.get_nocase());
				}
				break;
			}
			// else fall through
		default:
			param.type_error("universal charstring value");
		}
		return is_pattern;
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
		if (!is_bound()) {
			return "<unbound>";
		}

		if (charstring) {
			return cstr.toString();
		} else {
			final StringBuilder str = new StringBuilder();

			for (int i = 0; i < val_ptr.size(); ++i) {
				str.append(val_ptr.get(i).toString());
			}

			return str.toString();
		}
	}

	/**
	 * @return unicode string representation
	 */
	public String to_utf() {
		must_bound("Accessing an element of an unbound universal charstring value.");

		if (charstring) {
			return cstr.toString();
		} else {
			final StringBuilder str = new StringBuilder();

			for (int i = 0; i < val_ptr.size(); ++i) {
				str.append(val_ptr.get(i).to_utf(val_ptr.size() == 1 ? true : false));
			}

			return str.toString();
		}
	}

	@Override
	/** {@inheritDoc} */
	public void encode_text(final Text_Buf text_buf) {
		must_bound("Text encoder: Encoding an unbound universal charstring value.");

		if (charstring) {
			convert_cstr_to_uni();
		}

		final int n_chars = val_ptr.size();
		text_buf.push_int(n_chars);
		for (int i = 0; i < n_chars; i++) {
			final TitanUniversalChar tempChar = val_ptr.get(i);
			byte buf[] = new byte[4];
			buf[0] = (byte)tempChar.getUc_group();
			buf[1] = (byte)tempChar.getUc_plane();
			buf[2] = (byte)tempChar.getUc_row();
			buf[3] = (byte)tempChar.getUc_cell();
			text_buf.pull_raw(4, buf);
		}
	}

	@Override
	/** {@inheritDoc} */
	public void decode_text(final Text_Buf text_buf) {
		clean_up();

		final int n_uchars = text_buf.pull_int().get_int();
		if (n_uchars < 0) {
			throw new TtcnError("Text decoder: Invalid length was received for an universal charstring.");
		}

		charstring = false;
		val_ptr = new ArrayList<TitanUniversalChar>(n_uchars);
		if (n_uchars > 0) {
			for (int i = 0; i < n_uchars; i++) {
				final byte buf[] = new byte[4];
				text_buf.pull_raw(4, buf);
				final TitanUniversalChar temp = new TitanUniversalChar((char)buf[0], (char)buf[1], (char)buf[2], (char)buf[3]);
				val_ptr.add(temp);
			}
		}
	}

	// intentionally package public
	final TitanUniversalChar char_at(final int i) {
		if (charstring) {
			return new TitanUniversalChar((char) 0, (char) 0, (char) 0, cstr.charAt(i));
		}

		return val_ptr.get(i);
	}

	// intentionally package public
	final void set_char_at(final int i, final TitanUniversalChar c) {
		if (charstring) {
			convert_cstr_to_uni();
		}

		val_ptr.set(i, c);
	}

	final void set_char_at(final int i, final char c) {
		if (charstring) {
			cstr.setCharAt(i, c);
		} else {
			val_ptr.set(i, new TitanUniversalChar((char) 0, (char) 0, (char) 0, c));
		}
	}

	/**
	 * Creates a new universal charstring, that is the equivalent of the
	 * current one with its elements rotated to the left with the provided
	 * amount.
	 *
	 * operator<<= in the core.
	 *
	 * @param rotate_count
	 *                the number of characters to rotate left.
	 * @return the new universal charstring.
	 * */
	public TitanUniversalCharString rotate_left(int rotate_count) {
		must_bound("The left operand of rotate left operator is an unbound universal charstring value.");

		if (charstring) {
			return new TitanUniversalCharString(new TitanCharString(cstr).rotate_left(rotate_count));
		}
		if (val_ptr.isEmpty()) {
			return new TitanUniversalCharString(this);
		}
		if (rotate_count >= 0) {
			rotate_count = rotate_count % val_ptr.size();
			if (rotate_count == 0) {
				return new TitanUniversalCharString(this);
			}

			final TitanUniversalCharString result = new TitanUniversalCharString();
			result.val_ptr = new ArrayList<TitanUniversalChar>();
			for (int i = 0; i < val_ptr.size() - rotate_count; i++) {
				result.val_ptr.add(i, val_ptr.get(i + rotate_count));
			}
			for (int i = val_ptr.size() - rotate_count; i < val_ptr.size(); i++) {
				result.val_ptr.add(i, val_ptr.get(i + rotate_count - val_ptr.size()));
			}

			return result;
		} else {
			return rotate_right(-rotate_count);
		}
	}

	/**
	 * Creates a new universal charstring, that is the equivalent of the
	 * current one with its elements rotated to the left with the provided
	 * amount.
	 *
	 * operator<<= in the core.
	 *
	 * @param rotate_count
	 *                the number of characters to rotate left.
	 * @return the new universal charstring.
	 * */
	public TitanUniversalCharString rotate_left(final TitanInteger rotate_count) {
		rotate_count.must_bound("Unbound right operand of octetstring rotate left operator.");

		return rotate_left(rotate_count.get_int());
	}

	/**
	 * Creates a new universal charstring, that is the equivalent of the
	 * current one with its elements rotated to the right with the provided
	 * amount.
	 *
	 * operator>>= in the core.
	 *
	 * @param rotate_count
	 *                the number of characters to rotate right.
	 * @return the new universal charstring.
	 * */
	public TitanUniversalCharString rotate_right(int rotate_count) {
		must_bound("The left operand of rotate right operator is an unbound universal charstring value.");

		if (charstring) {
			return new TitanUniversalCharString(new TitanCharString(cstr).rotate_right(rotate_count));
		}
		if (val_ptr.isEmpty()) {
			return new TitanUniversalCharString(this);
		}
		if (rotate_count >= 0) {
			rotate_count = rotate_count % val_ptr.size();
			if (rotate_count == 0) {
				return new TitanUniversalCharString(this);
			}

			final TitanUniversalCharString result = new TitanUniversalCharString();
			result.val_ptr = new ArrayList<TitanUniversalChar>();
			if (rotate_count > val_ptr.size()) {
				rotate_count = val_ptr.size();
			}
			for (int i = 0; i < rotate_count; i++) {
				result.val_ptr.add(i, val_ptr.get(i - rotate_count + val_ptr.size()));
			}
			for (int i = rotate_count; i < val_ptr.size(); i++) {
				result.val_ptr.add(i, val_ptr.get(i - rotate_count));
			}

			return result;
		} else {
			return rotate_left(-rotate_count);
		}
	}

	/**
	 * Creates a new universal charstring, that is the equivalent of the
	 * current one with its elements rotated to the left with the provided
	 * amount.
	 *
	 * operator>>= in the core.
	 *
	 * @param rotate_count
	 *                the number of characters to rotate left.
	 * @return the new universal charstring.
	 * */
	public TitanUniversalCharString rotate_right(final TitanInteger rotate_count) {
		rotate_count.must_bound("Unbound right operand of octetstring rotate left operator.");

		return rotate_right(rotate_count.get_int());
	}

	public TitanCharString get_stringRepr_for_pattern() {
		this.must_bound("Performing pattern conversion operation on an unbound universal charstring value.");
		final StringBuilder ret_val = new StringBuilder();
		if (charstring)
			for (int i = 0; i < cstr.length(); i++) {
				final char chr = cstr.charAt(i);
				if (TTCN_Logger.is_printable(chr)) {
					ret_val.append(chr);
				} else {
					ret_val.append("\\q{0,0,0,");
					ret_val.append((int)chr);
					ret_val.append('}');
				}
			} else {
				for (int i = 0; i < val_ptr.size(); i++) {
					final TitanUniversalChar uchar = val_ptr.get(i);
					if (uchar.is_char()) {
						ret_val.append(uchar.getUc_cell());
					} else {
						ret_val.append("\\q{");
						ret_val.append((int)uchar.getUc_group());
						ret_val.append(',');
						ret_val.append((int)uchar.getUc_plane());
						ret_val.append(',');
						ret_val.append((int)uchar.getUc_row());
						ret_val.append(',');
						ret_val.append((int)uchar.getUc_cell());
						ret_val.append('}');
					}
				}	
			}
		return new TitanCharString(ret_val.toString());
	}

	public void convert_cstr_to_uni() {
		val_ptr = new ArrayList<TitanUniversalChar>(cstr.length());
		for (int i = 0; i < cstr.length(); ++i) {
			val_ptr.add(i, new TitanUniversalChar((char) 0, (char) 0, (char) 0, cstr.charAt(i)));
		}
		charstring = false;
		cstr = null;
	}

	// static function
	/**
	 * Checks if the first value is equivalent to the second one.
	 *
	 * static operator== in the core
	 *
	 * @param ucharValue
	 *                the first value.
	 * @param otherValue
	 *                the other value to check against.
	 * @return {@code true} if the values are equivalent.
	 */
	public static boolean operator_equals(final TitanUniversalChar ucharValue, final TitanUniversalCharString otherValue) {
		otherValue.must_bound("The right operand of comparison is an unbound universal charstring value.");

		if (otherValue.charstring) {
			if (otherValue.cstr.length() != 1) {
				return false;
			}
			return ucharValue.is_char() && ucharValue.getUc_cell() == otherValue.cstr.charAt(0);
		}
		if (otherValue.val_ptr.size() != 1) {
			return false;
		}

		return ucharValue.operator_equals(otherValue.val_ptr.get(0));
	}

	/**
	 * Checks if the first value is not equivalent to the second one.
	 *
	 * static operator!= in the core
	 *
	 * @param ucharValue
	 *                the first value.
	 * @param otherValue
	 *                the other value to check against.
	 * @return {@code true} if the values are equivalent.
	 */
	public static boolean operator_not_equals(final TitanUniversalChar ucharValue, final TitanUniversalCharString otherValue) {
		return !operator_equals(ucharValue, otherValue);
	}

	/**
	 * Concatenates the first universal charstring with the second universal
	 * charstring received as a parameter.
	 *
	 * static operator+ in the core.
	 *
	 * @param ucharValue the first universal charstring to concatenate.
	 * @param other_value
	 *                the other value to concatenate with.
	 * @return the new universal charstring representing the concatenated
	 *         value.
	 * */
	public static TitanUniversalCharString operator_concatenate(final TitanUniversalChar ucharValue, final TitanUniversalCharString other_value) {
		other_value.must_bound("The right operand of concatenation is an unbound universal charstring value.");

		if (other_value.charstring) {
			if (ucharValue.is_char()) {
				return new TitanUniversalCharString(new StringBuilder(ucharValue.getUc_cell()).append(other_value.cstr));
			} else {
				final List<TitanUniversalChar> ulist = new ArrayList<TitanUniversalChar>();
				ulist.add(ucharValue);
				for (int i = 0; i < other_value.cstr.length(); ++i) {
					final TitanUniversalChar uc = new TitanUniversalChar((char) 0, (char) 0, (char) 0, other_value.cstr.charAt(i));
					ulist.add(uc);
				}
				final TitanUniversalCharString ret_val = new TitanUniversalCharString();
				ret_val.set_value(ulist);
				return ret_val;
			}
		}

		final TitanUniversalCharString ret_val = new TitanUniversalCharString(ucharValue);
		ret_val.val_ptr.addAll(other_value.val_ptr);
		return ret_val;
	}

	/**
	 * Checks if the first value is equivalent to the second one.
	 *
	 * static operator== in the core
	 *
	 * @param otherValue
	 *                the first value.
	 * @param rightValue
	 *                the other value to check against.
	 * @return {@code true} if the values are equivalent.
	 */
	public static boolean operator_equals(final String otherValue, final TitanUniversalCharString rightValue) {
		rightValue.must_bound("The left operand of comparison is an unbound universal charstring value.");

		if (rightValue.charstring) {
			return rightValue.cstr.toString().equals(otherValue);
		}
		if (rightValue.val_ptr.size() != otherValue.length()) {
			return false;
		}
		for (int i = 0; i < rightValue.val_ptr.size(); ++i) {
			if (rightValue.val_ptr.get(i).getUc_group() != 0 || rightValue.val_ptr.get(i).getUc_plane() != 0
					|| rightValue.val_ptr.get(i).getUc_row() != 0
					|| rightValue.val_ptr.get(i).getUc_cell() != otherValue.charAt(i)) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Concatenates the first universal charstring with the second universal
	 * charstring received as a parameter.
	 *
	 * static operator+ in the core.
	 *
	 * @param stringValue the first universal charstring to concatenate.
	 * @param other_value
	 *                the other value to concatenate with.
	 * @return the new universal charstring representing the concatenated
	 *         value.
	 * */
	public static TitanUniversalCharString operator_concatenate(final String stringValue, final TitanUniversalCharString other_value) {
		other_value.must_bound("The left operand of concatenation is an unbound universal charstring value.");

		int other_len;
		if (stringValue == null) {
			other_len = 0;
		} else {
			other_len = stringValue.length();
		}
		if (other_len == 0) {
			return new TitanUniversalCharString(other_value);
		}
		if (other_value.charstring) {
			return new TitanUniversalCharString(new StringBuilder(stringValue).append(other_value.cstr));
		}
		final TitanUniversalCharString ret_val = new TitanUniversalCharString();
		ret_val.val_ptr = new ArrayList<TitanUniversalChar>();
		for (int i = 0; i < other_len; i++) {
			final TitanUniversalChar uc = new TitanUniversalChar((char) 0, (char) 0, (char) 0, stringValue.charAt(i));
			ret_val.val_ptr.add(uc);
		}
		ret_val.val_ptr.addAll(other_value.val_ptr);
		return ret_val;
	}

	// decode

	public void decode_utf8(final char[] valueStr, final CharCoding code, final boolean checkBOM) {
		// approximate the number of characters
		final int lenghtOctets = valueStr.length;
		int lenghtUnichars = 0;
		for (int i = 0; i < lenghtOctets; i++) {
			// count all octets except the continuing octets (10xxxxxx)
			if ((valueStr[i] & 0xC0) != 0x80) {
				lenghtUnichars++;
			}
		}
		// allocate enough memory, start from clean state
		clean_up();
		charstring = false;
		val_ptr = new ArrayList<TitanUniversalChar>(lenghtUnichars);
		for (int i = 0; i < lenghtUnichars; i++) {
			val_ptr.add(new TitanUniversalChar());
		}
		lenghtUnichars = 0;

		final int start = checkBOM ? check_BOM(CharCoding.UTF_8, valueStr) : 0;
		for (int i = start; i < lenghtOctets; ) {
			// perform the decoding character by character
			if (valueStr[i] <= 0x7F)  {
				// character encoded on a single octet: 0xxxxxxx (7 useful bits)
				val_ptr.add(lenghtUnichars, new TitanUniversalChar((char)0,(char) 0,(char) 0, valueStr[i]));
				i++;
				lenghtUnichars++;
			} else if (valueStr[i] <= 0xBF)  {
				// continuing octet (10xxxxxx) without leading octet ==> malformed
				TTCN_EncDec_ErrorContext.error(TTCN_EncDec.error_type.ET_DEC_UCSTR, MessageFormat.format(
						"Malformed: At character position {0}, octet position {1}: continuing octet {0} without leading octet.",
						lenghtUnichars, i, valueStr[i]));
				i++;
			} else if (valueStr[i] <= 0xDF)  {
				// character encoded on 2 octets: 110xxxxx 10xxxxxx (11 useful bits)
				char[] octets = new char[2];
				octets[0] = (char) (valueStr[i] & 0x1F);

				fill_continuing_octets(1, octets, lenghtOctets, valueStr, i + 1, lenghtUnichars);

				val_ptr.set(lenghtUnichars, new TitanUniversalChar((char) 0, (char) 0,(char) (octets[0] >> 2), (char) ((octets[0] << 6) & 0xFF | octets[1])));

				if (val_ptr.get(lenghtUnichars).getUc_row() == 0x00 && 
						val_ptr.get(lenghtUnichars).getUc_cell() < 0x80) {
					TTCN_EncDec_ErrorContext.error(TTCN_EncDec.error_type.ET_DEC_UCSTR, MessageFormat.format(
							"Overlong: At character position {0}, octet position {1}: 2-octet encoding for quadruple (0, 0, 0, {2}).", 
							lenghtUnichars, i, val_ptr.get(lenghtUnichars).getUc_cell()));
				}
				i += 2;
				lenghtUnichars++;
			} else if (valueStr[i] <= 0xEF) {
				// character encoded on 3 octets: 1110xxxx 10xxxxxx 10xxxxxx
				// (16 useful bits)
				char[] octets = new char[3];
				octets[0] = (char) (valueStr[i] & 0x0F);
				fill_continuing_octets(2, octets /*+ 1*/, lenghtOctets, valueStr, i + 1, lenghtUnichars);

				val_ptr.set(lenghtUnichars, new TitanUniversalChar((char) 0, (char) 0,(char) ((octets[0] << 4) & 0xFF | octets[1] >> 2), (char) ((octets[1] << 6) & 0xFF | octets[2])));

				if (val_ptr.get(lenghtUnichars).getUc_row() < 0x08) {
					TTCN_EncDec_ErrorContext.error(TTCN_EncDec.error_type.ET_DEC_UCSTR,
							MessageFormat.format("Overlong: At character position {0}, octet position {1}: 3-octet encoding for quadruple (0, 0, {2}, {3}).", 
									lenghtUnichars, i, val_ptr.get(lenghtUnichars).getUc_row(), val_ptr.get(lenghtUnichars).getUc_cell()));
				}
				i += 3;
				lenghtUnichars++;
			} else if (valueStr[i] <= 0xF7) {
				// character encoded on 4 octets: 11110xxx 10xxxxxx 10xxxxxx 10xxxxxx
				// (21 useful bits)
				char[] octets = new char[4];
				octets[0] = (char) (valueStr[i] & 0x07);
				fill_continuing_octets(3, octets /*+ 1*/, lenghtOctets, valueStr, i + 1, lenghtUnichars);

				val_ptr.set(lenghtUnichars, new TitanUniversalChar((char) 0, (char) ((octets[0] << 2) & 0xFF | octets[1] >> 4),(char) ((octets[1] << 4) & 0xFF | octets[2] >> 2), (char) ((octets[2] << 6) & 0xFF | octets[3])));

				if (val_ptr.get(lenghtUnichars).getUc_plane() == 0x00) {
					TTCN_EncDec_ErrorContext.error(TTCN_EncDec.error_type.ET_DEC_UCSTR,
							MessageFormat.format("Overlong: At character position {0}, octet position {1}: 4-octet encoding for quadruple (0, 0, {2}, {3}).", 
									lenghtUnichars, i, val_ptr.get(lenghtUnichars).getUc_row(), val_ptr.get(lenghtUnichars).getUc_cell()));
				}
				i += 4;
				lenghtUnichars++;
			} else if (valueStr[i] <= 0xFB) {
				// character encoded on 5 octets: 111110xx 10xxxxxx 10xxxxxx 10xxxxxx
				// 10xxxxxx (26 useful bits)

				char[] octets = new char[5];
				octets[0] = (char) (valueStr[i] & 0x03);
				fill_continuing_octets(4, octets /*+ 1*/, lenghtOctets, valueStr, i + 1, lenghtUnichars);

				val_ptr.set(lenghtUnichars, new TitanUniversalChar((char) octets[0], (char) ((octets[1] << 2) & 0xFF | octets[2] >> 4),(char) ((octets[2] << 4) & 0xFF | octets[3] >> 2), (char) ((octets[3] << 6) & 0xFF | octets[4])));

				if (val_ptr.get(lenghtUnichars).getUc_group() == 0x00 && val_ptr.get(lenghtUnichars).getUc_plane() < 0x20) {
					TTCN_EncDec_ErrorContext.error(TTCN_EncDec.error_type.ET_DEC_UCSTR,
							MessageFormat.format("Overlong: At character position {0}, octet position {1}: 5-octet encoding for quadruple (0, {4}, {2}, {3}).", 
									lenghtUnichars, i, val_ptr.get(lenghtUnichars).getUc_row(), val_ptr.get(lenghtUnichars).getUc_cell(),  val_ptr.get(lenghtUnichars).getUc_plane()));
				}
				i += 5;
				lenghtUnichars++;
			} else if (valueStr[i] <= 0xFD) {
				// character encoded on 6 octets: 1111110x 10xxxxxx 10xxxxxx 10xxxxxx
				// 10xxxxxx 10xxxxxx (31 useful bits)

				char[] octets = new char[6];
				octets[0] = (char) (valueStr[i] & 0x01);
				fill_continuing_octets(5, octets, lenghtOctets, valueStr, i + 1, lenghtUnichars);

				val_ptr.set(lenghtUnichars, new TitanUniversalChar((char) ((octets[0] << 6) & 0xFF | octets[1]), (char) ((octets[2] << 2) & 0xFF | octets[3] >> 4),(char) ((octets[3] << 4) & 0xFF | octets[4] >> 2), (char) ((octets[4] << 6) & 0xFF | octets[5])));

				if (val_ptr.get(lenghtUnichars).getUc_group() < 0x04) {
					TTCN_EncDec_ErrorContext.error(TTCN_EncDec.error_type.ET_DEC_UCSTR,
							MessageFormat.format("Overlong: At character position {0}, octet position {1}: 6-octet encoding for quadruple {2}.", 
									lenghtUnichars, i, val_ptr.get(lenghtUnichars).toString()));
				}
				i += 6;
				lenghtUnichars++;
			} else {
				// not used code points: FE and FF => malformed
				TTCN_EncDec_ErrorContext.error(TTCN_EncDec.error_type.ET_DEC_UCSTR,
						MessageFormat.format("Malformed: At character position {0}, octet position {1}: unused/reserved octet {2}.", lenghtUnichars, i, valueStr[i]));
				i++;
			}
		}

		if (val_ptr.size() != lenghtUnichars) {
			// truncate the memory and set the correct size in case of decoding errors
			// (e.g. skipped octets)

			if (lenghtUnichars > 0) {
				final List<TitanUniversalChar> helper = new ArrayList<TitanUniversalChar>(lenghtUnichars);
				for (int i = 0; i < lenghtUnichars && i < val_ptr.size(); i++) {
					helper.add(val_ptr.get(i));
				}
				val_ptr = helper;
			} else {
				clean_up();
			}
		}
	}

	public void decode_utf16(final int n_octets, final char[] octets_ptr, final CharCoding expected_coding) {
		if (n_octets % 2 != 0 || 0 > n_octets) {
			TTCN_EncDec_ErrorContext.error(error_type.ET_DEC_UCSTR, "Wrong UTF-16 string. The number of bytes (%d) in octetstring shall be non negative and divisible by 2", n_octets);
		}

		final int start = check_BOM(expected_coding, octets_ptr);
		int n_uchars = n_octets / 2;
		clean_up();
		val_ptr = new ArrayList<TitanUniversalChar>(n_uchars);
		n_uchars = 0;
		boolean isBig = true;
		switch (expected_coding) {
		case UTF16:
		case UTF16BE:
			isBig = true;
			break;
		case UTF16LE:
			isBig = false;
			break;
		default:
			TTCN_EncDec_ErrorContext.error(error_type.ET_DEC_UCSTR, "Unexpected coding type for UTF-16 encoding");
			break;
		}
		for (int i = start; i < n_octets; i+= 2 ) {
			final int first  = isBig ? i : i + 1;
			final int second = isBig ? i + 1 : i;
			final int third  = isBig ? i + 2 : i + 3;
			final int fourth = isBig ? i + 3 : i + 2;

			final int W1 = octets_ptr[first] << 8 | octets_ptr[second];
			final int W2 = (i + 3 < n_octets) ? octets_ptr[third] << 8 | octets_ptr[fourth] : 0;

			if (0xD800 > W1 || 0xDFFF < W1) {
				//if W1 < 0xD800 or W1 > 0xDFFF, the character value is the value of W1
				val_ptr.add(new TitanUniversalChar((char)0,(char) 0, (char) (octets_ptr[first] & 0xFF) , (char) (octets_ptr[second] & 0xFF)));
				++n_uchars;
			} else if (0xD800 > W1 || 0xDBFF < W1) {
				//Determine if W1 is between 0xD800 and 0xDBFF. If not, the sequence
				//is in error and no valid character can be obtained using W1.
				TTCN_EncDec_ErrorContext.error(error_type.ET_DEC_UCSTR, "The word (0x%04X) shall be between 0xD800 and 0xDBFF", W1);
			} else if (0 == W2 || (0xDC00 > W2 || 0xDFFF < W2)) {
				//If there is no W2 (that is, the sequence ends with W1), or if W2
				//is not between 0xDC00 and 0xDFFF, the sequence is in error.
				if(W2 != 0) {
					TTCN_EncDec_ErrorContext.error(error_type.ET_DEC_UCSTR, "Wrong UTF-16 string. The word (0x%04X) shall be between 0xDC00 and 0xDFFF", W2);
				} else {
					TTCN_EncDec_ErrorContext.error(error_type.ET_DEC_UCSTR, "Wrong UTF-16 string. The decoding algorythm does not expect 0x00 or EOL");
				}
			} else {
				//Construct a 20-bit unsigned integer, taking the 10 low-order bits of W1 as its 10 high-
				//order bits and the 10 low-order bits of W2 as its 10 low-order bits.
				final int mask10bitlow = 0x3FF;
				int DW = (W1 & mask10bitlow) << 10;
				DW |= (W2 & mask10bitlow);
				DW += 0x10000;
				val_ptr.add(new TitanUniversalChar((char) 0,(char) ((DW >> 16) & 0xFF), (char) (DW >> 8 & 0xFF),(char) (DW & 0xFF)));
				++n_uchars;
				i+=2; // jump over w2 in octetstring
			}
		}
		if (val_ptr.size() != n_uchars) {
			// truncate the memory and set the correct size in case of decoding errors
			// (e.g. skipped octets)
			if (n_uchars > 0) {
				val_ptr = new ArrayList<TitanUniversalChar>(n_uchars);
			} else {
				clean_up();
			}
		}
	}

	public void decode_utf32(final int n_octets, final char[] octets_ptr, final CharCoding expected_coding) {
		if (n_octets % 4 != 0 || 0 > n_octets) {
			TTCN_EncDec_ErrorContext.error(error_type.ET_DEC_UCSTR, "Wrong UTF-32 string. The number of bytes (%d) in octetstring shall be non negative and divisible by 4", n_octets);
		}

		final int start = check_BOM(expected_coding, octets_ptr);
		int n_uchars = n_octets / 4;
		val_ptr = new ArrayList<TitanUniversalChar>(n_uchars);
		n_uchars = 0;
		boolean isBig = true;
		switch (expected_coding) {
		case UTF32:
		case UTF32BE:
			isBig = true;
			break;
		case UTF32LE:
			isBig = false;
			break;
		default:
			TTCN_EncDec_ErrorContext.error(error_type.ET_DEC_UCSTR, "Unexpected coding type for UTF-32 encoding");
			break;
		}

		for (int i = start; i < n_octets; i += 4 ) {
			final int first  = isBig ? i : i + 3;
			final int second = isBig ? i + 1 : i + 2;
			final int third  = isBig ? i + 2 : i + 1;
			final int fourth = isBig ? i + 3 : i;
			int DW = octets_ptr[first] << 8 | octets_ptr[second];
			DW <<= 8;
			DW |= octets_ptr[third];
			DW <<= 8;
			DW |= octets_ptr[fourth];
			if (0x0000D800 <= DW && 0x0000DFFF >= DW) {
				TTCN_EncDec_ErrorContext.error(error_type.ET_DEC_UCSTR, "Any UTF-32 code (0x%08X) between 0x0000D800 and 0x0000DFFF is ill-formed", DW);
			} else if (0x0010FFFF < DW) {
				TTCN_EncDec_ErrorContext.error(error_type.ET_DEC_UCSTR, "Any UTF-32 code (0x%08X) greater than 0x0010FFFF is ill-formed", DW);
			} else {
				val_ptr.add(new TitanUniversalChar(octets_ptr[first], octets_ptr[second], octets_ptr[third], octets_ptr[fourth]));
				++n_uchars;
			}
		}
		if(val_ptr.size() != n_uchars) {
			// truncate the memory and set the correct size in case of decoding errors
			// (e.g. skipped octets)
			if (n_uchars > 0 ) {
				val_ptr = new ArrayList<TitanUniversalChar>(n_uchars);
			} else {
				clean_up();
			}
		}
	}

	public int check_BOM(final CharCoding code, final char[] ostr) {
		String coding_str;
		//BOM indicates that the byte order is determined by a byte order mark, 
		//if present at the beginning the length of BOM is returned.
		final int length = ostr.length;
		switch (code) {
		case UTF32BE:
		case UTF32:
			if (4 <= length && 0x00 == ostr[0] && 0x00 == ostr[1] &&
			0xFE == ostr[2] && 0xFF == ostr[3]) {
				return 4;
			}
			coding_str = "UTF-32BE";
			break;
		case UTF32LE:
			if (4 <= length && 0xFF == ostr[0] && 0xFE == ostr[1] &&
			0x00 == ostr[2] && 0x00 == ostr[3]) {
				return 4;
			}
			coding_str = "UTF-32LE";
			break;
		case UTF16BE:
		case UTF16:
			if (2 <= length && 0xFE == ostr[0] && 0xFF == ostr[1]) {
				return 2;
			}
			coding_str = "UTF-16BE";
			break;
		case UTF16LE:
			if (2 <= length && 0xFF == ostr[0] && 0xFE == ostr[1]) {
				return 2;
			}
			coding_str = "UTF-16LE";
			break;
		case UTF_8:
			if (3 <= ostr.length && 0xEF == ostr[0] && 0xBB == ostr[1] && 0xBF == ostr[2]) {
				return 3;
			}
			coding_str = "UTF-8";
			break;
		default:
			throw new TtcnError(MessageFormat.format("Internal error: invalid expected coding ({0})", code));
		}

		if (TTCN_Logger.log_this_event(TTCN_Logger.Severity.DEBUG_UNQUALIFIED)) {
			TTCN_Logger.begin_event(TTCN_Logger.Severity.DEBUG_UNQUALIFIED);
			TTCN_Logger.log_event_str("Warning: No ");
			TTCN_Logger.log_event_str(coding_str);
			TTCN_Logger.log_event_str(" Byte Order Mark(BOM) detected. It may result decoding errors");
			TTCN_Logger.end_event();
		}
		return 0;
	}

	public static void fill_continuing_octets(final int n_continuing, final char[] continuing_ptr, final int n_octets,
			final char[] octets_ptr, final int start_pos, final int uchar_pos) {
		for (int i = 0; i < n_continuing; i++) {
			if (start_pos + i < n_octets) {
				final char octet = octets_ptr[start_pos + i];
				if ((octet & 0xC0) != 0x80) {
					TTCN_EncDec_ErrorContext.error(TTCN_EncDec.error_type.ET_DEC_UCSTR,
							MessageFormat.format("Malformed: At character position {0}, octet position {1}: {2} is not a valid continuing octet.", uchar_pos, start_pos + i, octet));
				}
				continuing_ptr[i + 1] = (char) (octet & 0x3F);
			} else {
				if (start_pos + i == n_octets) {
					if (i > 0) {
						// only a part of octets is missing
						TTCN_EncDec_ErrorContext.error(TTCN_EncDec.error_type.ET_DEC_UCSTR,
								MessageFormat.format("Incomplete: At character position {0}, octet position {1}: {2} out of {3} continuing octets {4} missing from the end of the stream.",
										uchar_pos, start_pos + i, n_continuing - i, n_continuing, n_continuing - i > 1 ? "are" : "is"));
					} else {
						// all octets are missing
						TTCN_EncDec_ErrorContext.error(TTCN_EncDec.error_type.ET_DEC_UCSTR,
								MessageFormat.format("Incomplete: At character position {0}, octet position {1}: {2} continuing octet{3} missing from the end of the stream.",
										uchar_pos, start_pos, n_continuing, n_continuing > 1 ? "s are" : " is"));
					}
				}
				continuing_ptr[i + 1] = 0;
			}
		}
	}

	// encode

	public void encode_utf8(final TTCN_Buffer text_buf) {
		encode_utf8(text_buf, false);
	}

	public void encode_utf8(final TTCN_Buffer buf, final boolean addBOM) {
		// Add BOM
		if (addBOM) {
			buf.put_c((char)0xEF);
			buf.put_c((char)0xBB);
			buf.put_c((char)0xBF);
		}

		if (charstring) {
			final char[] bstr = new char[cstr.length()];
			for (int i = 0; i < cstr.length(); i++) {
				bstr[i] =  cstr.charAt(i);
			}
			buf.put_s(bstr);
			// put_s avoids the check for boundness in put_cs
		} else {
			for (int i = 0; i < val_ptr.size(); i++) {
				final char g = val_ptr.get(i).getUc_group();
				final char p = val_ptr.get(i).getUc_plane();
				final char r = val_ptr.get(i).getUc_row();
				final char c = val_ptr.get(i).getUc_cell();
				if (g == 0x00 && p <= 0x1F) {
					if (p == 0x00) {
						if (r == 0x00 && c <= 0x7F) {
							// 1 octet
							buf.put_c(c);
						} // r
						// 2 octets
						else if (r <= 0x07) {
							buf.put_c((char) (0xC0 | r << 2 | c >> 6));
							buf.put_c((char) (0x80 | (c & 0x3F)));
						} // r
						// 3 octets
						else {
							buf.put_c((char) (0xE0 | r >> 4));
							buf.put_c((char) (0x80 | (r << 2 & 0x3C) | c >> 6));
							buf.put_c((char) (0x80 | (c & 0x3F)));
						} // r
					} // p
					// 4 octets
					else {
						buf.put_c((char) (0xF0 | p >> 2));
						buf.put_c((char) (0x80 | (p << 4 & 0x30) | r >> 4));
						buf.put_c((char) (0x80 | (r << 2 & 0x3C) | c >> 6));
						buf.put_c((char) (0x80 | (c & 0x3F)));
					} // p
				} //g
				// 5 octets
				else if (g <= 0x03) {
					buf.put_c((char) (0xF8 | g));
					buf.put_c((char) (0x80 | p >> 2));
					buf.put_c((char) (0x80 | (p << 4 & 0x30) | r >> 4));
					buf.put_c((char) (0x80 | (r << 2 & 0x3C) | c >> 6));
					buf.put_c((char) (0x80 | (c & 0x3F)));
				} // g
				// 6 octets
				else {
					buf.put_c((char) (0xFC | g >> 6));
					buf.put_c((char) (0x80 | (g & 0x3F)));
					buf.put_c((char) (0x80 | p >> 2));
					buf.put_c((char) (0x80 | (p << 4 & 0x30) | r >> 4));
					buf.put_c((char) (0x80 | (r << 2 & 0x3C) | c >> 6));
					buf.put_c((char) (0x80 | (c & 0x3F)));
				}
			} // for i
		}
	}

	public void encode_utf16(final TTCN_Buffer buf, final CharCoding expected_coding) {
		//add BOM
		boolean isBig = true;
		final TTCN_EncDec_ErrorContext error = new TTCN_EncDec_ErrorContext();
		try {
			switch (expected_coding) {
			case UTF16:
			case UTF16BE:
				isBig = true;
				break;
			case UTF16LE:
				isBig = false;
				break;
			default:
				TTCN_EncDec_ErrorContext.error(error_type.ET_DEC_UCSTR, "Unexpected coding type for UTF-16 encoding");
				break;
			}
			buf.put_c((char) (isBig ? 0xFE : 0xFF));
			buf.put_c((char) (isBig ? 0xFF : 0xFE));

			if(charstring) {
				for (int i = 0; i < cstr.length(); ++i) {
					buf.put_c(isBig ? 0 : cstr.charAt(i));
					buf.put_c(isBig ? cstr.charAt(i) : 0);
				}
			} else {
				for (int i = 0; i < val_ptr.size(); i++) {
					final char g = val_ptr.get(i).getUc_group();
					final char p = val_ptr.get(i).getUc_plane();
					final char r = val_ptr.get(i).getUc_row();
					final char c = val_ptr.get(i).getUc_cell();
					if (g != 0 || (0x10 < p)) {
						TTCN_EncDec_ErrorContext.error(error_type.ET_DEC_UCSTR, "Any UCS code (0x%02X%02X%02X%02X) to be encoded into UTF-16 shall not be greater than 0x10FFFF", g, p, r, c);
					} else if (0x00 == g && 0x00 ==p && 0xD8 <= r && 0xDF >= r) {
						// Values between 0xD800 and 0xDFFF are specifically reserved for use with UTF-16,
						// and don't have any characters assigned to them.
						TTCN_EncDec_ErrorContext.error(error_type.ET_DEC_UCSTR, "Any UCS code (0x%02X%02X) between 0xD800 and 0xDFFF is ill-formed", r, c);
					} else if (0x00 == g && 0x00 == p) {
						buf.put_c((char) ((isBig ? r : c) & 0xFF) );
						buf.put_c((char) ((isBig ? c : r) & 0xFF) );
					} else if (g != 0 || p != 0) {
						int univc = g;
						univc <<= 24;
						int temp = p;
						temp <<= 16;
						univc |= temp;
						temp = r;
						temp <<= 8;
						univc |= temp;
						univc |= c; // universal char filled in univc 
						int W1 = 0xD800;
						int W2 = 0xDC00;
						final int univcmod = univc - 0x10000;
						final int WH = univcmod >> 10;
						final int WL = univcmod & 0x3ff;
						W1 |= WH;
						W2 |= WL;
						char uc;
						uc = (char) (isBig ? W1 >> 8 : W1);
						buf.put_c((char) (uc & 0xFF));
						uc = (char) (isBig ? W1 : W1 >> 8);
						buf.put_c((char) (uc & 0xFF));
						uc = (char) (isBig ? W2 >> 8 : W2);
						buf.put_c((char) (uc & 0xFF));
						uc = (char) (isBig ? W2 : W2 >> 8);
						buf.put_c((char) (uc & 0xFF));
					}
				}
			}
		} finally {
			error.leave_context();
		}
	}

	public void encode_utf32(final TTCN_Buffer buf, final CharCoding expected_coding) {
		boolean isBig = true;
		final TTCN_EncDec_ErrorContext error = new TTCN_EncDec_ErrorContext();
		try {
			switch (expected_coding) {
			case UTF32:
			case UTF32BE:
				isBig = true;
				break;
			case UTF32LE:
				isBig = false;
				break;
			default:
				TTCN_EncDec_ErrorContext.error(error_type.ET_DEC_UCSTR, "Unexpected coding type for UTF-32 encoding");
				break;
			}
			//add BOM
			buf.put_c((char) (isBig ? 0x00 : 0xFF));
			buf.put_c((char) (isBig ? 0x00 : 0xFE));
			buf.put_c((char) (isBig ? 0xFE : 0x00));
			buf.put_c((char) (isBig ? 0xFF : 0x00));
			if (charstring) {
				for (int i = 0; i < cstr.length(); i++) {
					buf.put_c(isBig ? 0 : cstr.charAt(i));
					buf.put_c((char) 0);
					buf.put_c((char) 0);
					buf.put_c(isBig ? cstr.charAt(i) : 0);
				}
			} else {
				for (int i = 0; i < val_ptr.size(); i++) {
					final char g = val_ptr.get(i).getUc_group();
					final char p = val_ptr.get(i).getUc_plane();
					final char r = val_ptr.get(i).getUc_row();
					final char c = val_ptr.get(i).getUc_cell();
					int DW = g << 8 | p;
					DW <<= 8;
					DW |= r;
					DW <<= 8;
					DW |= c;
					if (0x0000D800 <= DW && 0x0000DFFF >= DW) {
						TTCN_EncDec_ErrorContext.error(error_type.ET_DEC_UCSTR, "Any UCS code (0x%08X) between 0x0000D800 and 0x0000DFFF is ill-formed", DW);
					} else if (0x0010FFFF < DW) {
						TTCN_EncDec_ErrorContext.error(error_type.ET_DEC_UCSTR, "Any UCS code (0x%08X) greater than 0x0010FFFF is ill-formed", DW);
					} else {
						buf.put_c(isBig ? g : c);
						buf.put_c(isBig ? p : r);
						buf.put_c(isBig ? r : p);
						buf.put_c(isBig ? c : g);
					}
				}
			}
		} finally {
			error.leave_context();
		}
	}

	@Override
	/** {@inheritDoc} */
	public void encode(final TTCN_Typedescriptor p_td, final TTCN_Buffer p_buf, final coding_type p_coding, final int flavour) {
		switch (p_coding) {
		case CT_RAW:
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
			try {
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
			} finally {
				errorContext.leave_context();
			}
			break;

		default:
			throw new TtcnError(MessageFormat.format("Unknown coding method requested to decode type `{0}''", p_td.name));
		}
	}

	@Override
	/** {@inheritDoc} */
	public int RAW_encode(final TTCN_Typedescriptor p_td, final RAW_enc_tree myleaf) {
		if (!is_bound()) {
			TTCN_EncDec_ErrorContext.error(error_type.ET_UNBOUND, "Encoding an unbound value.");
		}
		if (charstring) {
			return new TitanCharString(cstr).RAW_encode(p_td, myleaf);
		}

		final TTCN_Buffer buf = new TTCN_Buffer();
		switch (p_td.raw.stringformat) {
		case UNKNOWN: // default is UTF-8
		case UTF_8:
			encode_utf8(buf);
			break;
		case UTF16:
			encode_utf16(buf, CharCoding.UTF16);
			break;
		default:
			TTCN_EncDec_ErrorContext.error(error_type.ET_INTERNAL, "Invalid string serialization type.");
			break;
		}
		if (p_td.raw.fieldlength < 0 ) {
			// NULL terminated string
			buf.put_c((char) 0);
		}

		final int buff_len = buf.get_len();
		int bl = buff_len * 8; // bit length
		int align_length = p_td.raw.fieldlength > 0 ? p_td.raw.fieldlength - bl : 0;
		if (align_length < 0) {
			TTCN_EncDec_ErrorContext.error(error_type.ET_LEN_ERR, "There are insufficient bits to encode '%s': ", p_td.name);
			bl = p_td.raw.fieldlength;
			align_length = 0;
		}
		myleaf.data_array = new char[buff_len];
		System.arraycopy(buf.get_data(), 0, myleaf.data_array, 0, buff_len);
		if (p_td.raw.endianness == raw_order_t.ORDER_MSB) {
			myleaf.align = -align_length;
		} else {
			myleaf.align = align_length;
		}
		return myleaf.length = bl + align_length;
	}

	@Override
	/** {@inheritDoc} */
	public int RAW_decode(final TTCN_Typedescriptor p_td, final TTCN_Buffer buff, final int limit, final raw_order_t top_bit_ord) {
		return RAW_decode(p_td, buff, limit, top_bit_ord, false, -1, true, null);
	}

	@Override
	/** {@inheritDoc} */
	public int RAW_decode(final TTCN_Typedescriptor p_td, final TTCN_Buffer buff, final int limit, final raw_order_t top_bit_ord, final boolean no_err, final int sel_field, final boolean first_call, final RAW_Force_Omit force_omit) {
		final TitanCharString buff_str = new TitanCharString();
		final TTCN_EncDec_ErrorContext errorcontext = new TTCN_EncDec_ErrorContext();
		try {
			final int dec_len = buff_str.RAW_decode(p_td, buff, limit, top_bit_ord);
			final char[] tmp_val_ptr = buff_str.get_value().toString().toCharArray();
			if(buff_str.is_bound()) {
				charstring = true;
				for (int i = 0; i < buff_str.lengthof().get_int(); ++i) {
					if(buff_str.get_value().charAt(i) > 127) {
						charstring = false;
						break;
					}
				}

				switch (p_td.raw.stringformat) {
				case UNKNOWN: //default is UTF-8
				case UTF_8:
					if(charstring) {
						cstr = buff_str.get_value();
					} else {
						decode_utf8(tmp_val_ptr, CharCoding.UTF_8 , false);
					}
					break;
				case UTF16:
					if(!charstring) {
						decode_utf16(tmp_val_ptr.length, tmp_val_ptr, CharCoding.UTF16);
					} else {
						TTCN_EncDec_ErrorContext.error(error_type.ET_INVAL_MSG, "Invalid string format. Buffer contains only ASCII characters.");
					}
					break;
				default:
					TTCN_EncDec_ErrorContext.error(error_type.ET_INTERNAL, "Invalid string serialization type.");
					break;
				}
			}
			return dec_len;
		} finally {
			errorcontext.leave_context();
		}
	}

	public static CharCoding get_character_coding(final String codingString, final String contextString) {
		CharCoding newCoding = CharCoding.UTF_8;
		if (codingString != null && !codingString.equals("UTF-8")) {
			if ("UTF-16".equals(codingString)) {
				newCoding = CharCoding.UTF16;
			} else if("UTF-16LE".equals(codingString)) {
				newCoding = CharCoding.UTF16LE;
			} else if("UTF-16BE".equals(codingString)) {
				newCoding = CharCoding.UTF16BE;
			} else if("UTF-32".equals(codingString)) {
				newCoding = CharCoding.UTF32;
			} else if("UTF-32LE".equals(codingString)) {
				newCoding = CharCoding.UTF32LE;
			} else if("UTF-32BE".equals(codingString)) {
				newCoding = CharCoding.UTF32BE;
			} else {
				throw new TtcnError(MessageFormat.format("Invalid string serialization for {0}.", contextString));
			}
		}

		return newCoding;
	}

	/**
	 * This static function is used to help with implicit conversion, where
	 * there is no better to way.
	 * <p>
	 * This particular function can be easily optimized away in during
	 * execution.
	 *
	 * @param otherValue
	 *                the other value to convert into a TitanUniversalCharString.
	 * @return the converted value.
	 * */
	public static TitanUniversalCharString convert_to_UniversalCharString(final TitanUniversalCharString otherValue) {
		return otherValue;
	}

	/**
	 * This static function is used to help with implicit conversion, where
	 * there is no better to way.
	 *
	 * @param otherValue
	 *                the other value to convert into a TitanUniversalCharString.
	 * @return the converted value.
	 * */
	public static TitanUniversalCharString convert_to_UniversalCharString(final TitanUniversalCharString_Element otherValue) {
		return new TitanUniversalCharString(otherValue);
	}

	/**
	 * This static function is used to help with implicit conversion, where
	 * there is no better to way.
	 *
	 * @param otherValue
	 *                the other value to convert into a TitanUniversalCharString.
	 * @return the converted value.
	 * */
	public static TitanUniversalCharString convert_to_UniversalCharString(final TitanCharString otherValue) {
		return new TitanUniversalCharString(otherValue);
	}

	/**
	 * This static function is used to help with implicit conversion, where
	 * there is no better to way.
	 *
	 * @param otherValue
	 *                the other value to convert into a TitanUniversalCharString.
	 * @return the converted value.
	 * */
	public static TitanUniversalCharString convert_to_UniversalCharString(final TitanCharString_Element otherValue) {
		return new TitanUniversalCharString(new TitanCharString(otherValue));
	}
}
