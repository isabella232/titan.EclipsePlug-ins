/******************************************************************************
 * Copyright (c) 2000-2017 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

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

	public TitanUniversalChar( final char uc_group, final char uc_plane, final char uc_row, final char uc_cell ) {
		this.uc_group = uc_group;
		this.uc_plane = uc_plane;
		this.uc_row = uc_row;
		this.uc_cell = uc_cell;
	}

	public TitanUniversalChar( final TitanUniversalChar uc ) {
		this.uc_group = uc.uc_group;
		this.uc_plane = uc.uc_plane;
		this.uc_row = uc.uc_row;
		this.uc_cell = uc.uc_cell;
	}

	public boolean is_char() {
		return getUc_group() == 0 && getUc_plane() == 0 && getUc_row() == 0 && getUc_cell() < 128;
	}

	//originally boolean operator==(const universal_char& left_value, const universal_char& right_value)
	public static TitanBoolean operatorEquals( final TitanUniversalChar left_value, final TitanUniversalChar right_value ) {
		return new TitanBoolean(left_value.getUc_group() == right_value.getUc_group() &&
				left_value.getUc_plane() == right_value.getUc_plane() &&
				left_value.getUc_row() == right_value.getUc_row() &&
				left_value.getUc_cell() == right_value.getUc_cell());
	}

	public TitanBoolean operatorEquals( final TitanUniversalChar right_value ) {
		return operatorEquals( this, right_value );
	}

	// originally inline boolean operator!=(const universal_char& uchar_value, const universal_char& other_value)
	public static TitanBoolean operatorNotEquals(TitanUniversalChar left_value, TitanUniversalChar right_value) {
		return operatorEquals(left_value, right_value).not();
	}

	//originally boolean operator<(const universal_char& left_value, const universal_char& right_value)
	public static TitanBoolean lessThan( final TitanUniversalChar left_value, final TitanUniversalChar right_value ) {
		if (left_value.getUc_group() < right_value.getUc_group()) {
			return new TitanBoolean(true);
		} else if (left_value.getUc_group() == right_value.getUc_group()) {
			if (left_value.getUc_plane() < right_value.getUc_plane()) {
				return new TitanBoolean(true);
			} else if (left_value.getUc_plane() == right_value.getUc_plane()) {
				if (left_value.getUc_row() < right_value.getUc_row()) {
					return new TitanBoolean(true);
				} else if (left_value.getUc_row() == right_value.getUc_row()) {
					if (left_value.getUc_cell() < right_value.getUc_cell()) {
						return new TitanBoolean(true);
					} else {
						return new TitanBoolean(false);
					}
				} else {
					return new TitanBoolean(false);
				}
			} else {
				return new TitanBoolean(false);
			}
		} else {
			return new TitanBoolean(false);
		}
	}

	public TitanBoolean lessThan( final TitanUniversalChar right_value ) {
		return lessThan( this, right_value );
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

	@Override
	public String toString() {
		return new StringBuilder("(").append(uc_group).append(",").append(uc_plane)
				.append(",").append(uc_row).append(",").append(uc_cell).append(")").toString();
	}
}
