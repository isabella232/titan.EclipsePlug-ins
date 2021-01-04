/******************************************************************************
 * Copyright (c) 2000-2021 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;

/**
 * originally universal_char
 * Represents UTF-32 character
 *
 * @author Arpad Lovassy
 */
public class TitanUniversalChar {
	private char uc_group;
	private char uc_plane;
	private char uc_row;
	private char uc_cell;

	/**
	 * Initializes to the provided value.
	 *
	 * @param uc_group
	 *                the group value.
	 * @param uc_plane
	 *                the plane value.
	 * @param uc_row
	 *                the row value.
	 * @param uc_cell
	 *                the cell value.
	 * */
	public TitanUniversalChar(final char uc_group, final char uc_plane, final char uc_row, final char uc_cell) {
		this.uc_group = uc_group;
		this.uc_plane = uc_plane;
		this.uc_row = uc_row;
		this.uc_cell = uc_cell;
	}

	/**
	 * Initializes to the provided value.
	 *
	 * @param uc
	 *                the other universal character string character to
	 *                copy.
	 * */
	public TitanUniversalChar(final TitanUniversalChar uc) {
		this.uc_group = uc.uc_group;
		this.uc_plane = uc.uc_plane;
		this.uc_row = uc.uc_row;
		this.uc_cell = uc.uc_cell;
	}

	/**
	 * Initializes to unbound value.
	 * */
	public TitanUniversalChar() {
		//intentionally left empty
	}

	/**
	 * Checks if this universal charstring character is an ASCII character.
	 *
	 * @return {@code true} if it is an ASCII character, {@code false}
	 *         otherwise
	 * */
	public boolean is_char() {
		return getUc_group() == 0 && getUc_plane() == 0 && getUc_row() == 0 && getUc_cell() < 128;
	}

	/**
	 * Checks if the first value is equivalent to the second one.
	 *
	 * static operator== in the core
	 *
	 * @param left_value
	 *                the first value.
	 * @param right_value
	 *                the other value to check against.
	 * @return {@code true} if the values are equivalent.
	 */
	public static boolean operator_equals(final TitanUniversalChar left_value, final TitanUniversalChar right_value) {
		return left_value.getUc_group() == right_value.getUc_group() &&
				left_value.getUc_plane() == right_value.getUc_plane() &&
				left_value.getUc_row() == right_value.getUc_row() &&
				left_value.getUc_cell() == right_value.getUc_cell();
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
	public boolean operator_equals(final TitanUniversalChar right_value) {
		return operator_equals(this, right_value);
	}

	/**
	 * Checks if the first value is not equivalent to the second one.
	 *
	 * static operator!= in the core
	 *
	 * @param left_value
	 *                the first value.
	 * @param right_value
	 *                the other value to check against.
	 * @return {@code true} if the values are equivalent.
	 */
	public static boolean operator_not_equals(final TitanUniversalChar left_value, final TitanUniversalChar right_value) {
		return !operator_equals(left_value, right_value);
	}

	/**
	 * Checks if the first value is less than the second one.
	 *
	 * static operator< in the core
	 *
	 * @param left_value
	 *                the first value.
	 * @param right_value
	 *                the second value to check against.
	 * @return {@code true} if the first value is less than the second.
	 */
	public static boolean is_less_than(final TitanUniversalChar left_value, final TitanUniversalChar right_value) {
		if (left_value.getUc_group() < right_value.getUc_group()) {
			return true;
		} else if (left_value.getUc_group() == right_value.getUc_group()) {
			if (left_value.getUc_plane() < right_value.getUc_plane()) {
				return true;
			} else if (left_value.getUc_plane() == right_value.getUc_plane()) {
				if (left_value.getUc_row() < right_value.getUc_row()) {
					return true;
				} else if (left_value.getUc_row() == right_value.getUc_row()) {
					if (left_value.getUc_cell() < right_value.getUc_cell()) {
						return true;
					} else {
						return false;
					}
				} else {
					return false;
				}
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	/**
	 * Checks if the current value is less than the provided one.
	 *
	 * operator< in the core
	 *
	 * @param right_value
	 *                the other value to check against.
	 * @return {@code true} if the value is less than the provided.
	 */
	public boolean is_less_than(final TitanUniversalChar right_value) {
		return is_less_than(this, right_value);
	}

	public char getUc_group() {
		return uc_group;
	}

	public char getUc_plane() {
		return uc_plane;
	}

	public char getUc_row() {
		return uc_row;
	}

	public char getUc_cell() {
		return uc_cell;
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
		return new StringBuilder("(").append((int) uc_group).append(",").append((int) uc_plane)
				.append(",").append((int) uc_row).append(",").append((int) uc_cell).append(")").toString();
	}

	/**
	 * @return decoded quadruple as unicode string
	 */
	public String to_utf(final boolean is_alone) {
		byte[] arr = new byte[4];
		arr[0] = (byte)(uc_group & 0xFF);
		arr[1] = (byte)(uc_plane & 0xFF);
		arr[2] = (byte)(uc_row & 0xFF);
		arr[3] = (byte)(uc_cell & 0xFF);
		try {
			if (is_alone) {
				//special cases: last two bytes are a BOM
				if (arr[0] == 0 && arr[1] == 0 && arr[2] == -2 && arr[3]== -1) {
					return new String(arr, "UTF-32LE");
				}
			}
			return new String(arr, "UTF-32");
		} catch (UnsupportedEncodingException e) {
			throw new TtcnError(MessageFormat.format("Cannot decode quadruple: {0}, {1}, {2}, {3}", uc_group, uc_plane, uc_row, uc_cell));
		}
	}
}
