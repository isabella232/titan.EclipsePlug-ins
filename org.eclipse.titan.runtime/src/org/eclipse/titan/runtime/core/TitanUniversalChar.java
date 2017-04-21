package org.eclipse.titan.runtime.core;

/**
 * originally universal_char
 * Represents UTF-32 character
 * @author Arpad Lovassy
 */
public class TitanUniversalChar {
	private char uc_group;
	private char uc_plane;
	private char uc_row;
	private char uc_cell;

	public TitanUniversalChar( char uc_group, char uc_plane, char uc_row, char uc_cell ) {
		this.uc_group = uc_group;
		this.uc_plane = uc_plane;
		this.uc_row = uc_row;
		this.uc_cell = uc_cell;
	}

	public TitanUniversalChar( TitanUniversalChar uc ) {
		this.uc_group = uc.uc_group;
		this.uc_plane = uc.uc_plane;
		this.uc_row = uc.uc_row;
		this.uc_cell = uc.uc_cell;
	}

	public boolean is_char() {
		return getUc_group() == 0 && getUc_plane() == 0 && getUc_row() == 0 && getUc_cell() < 128;
	}

	//originally boolean operator==(const universal_char& left_value, const universal_char& right_value)
	public static boolean operatorEquals( final TitanUniversalChar left_value, final TitanUniversalChar right_value ) {
		return left_value.getUc_group() == right_value.getUc_group() &&
			   left_value.getUc_plane() == right_value.getUc_plane() &&
			   left_value.getUc_row() == right_value.getUc_row() &&
			   left_value.getUc_cell() == right_value.getUc_cell();
	}

	public boolean operatorEquals( final TitanUniversalChar right_value ) {
		return operatorEquals( this, right_value );
	}

	//originally boolean operator<(const universal_char& left_value, const universal_char& right_value)
	public static boolean lessThan( final TitanUniversalChar left_value, final TitanUniversalChar right_value ) {
		if (left_value.getUc_group() < right_value.getUc_group()) return true;
		else if (left_value.getUc_group() == right_value.getUc_group()) {
			if (left_value.getUc_plane() < right_value.getUc_plane()) return true;
			else if (left_value.getUc_plane() == right_value.getUc_plane()) {
				if (left_value.getUc_row() < right_value.getUc_row()) return true;
				else if (left_value.getUc_row() == right_value.getUc_row()) {
					if (left_value.getUc_cell() < right_value.getUc_cell()) return true;
					else return false;
				} else return false;
			} else return false;
		} else return false;
	}

	public boolean lessThan( final TitanUniversalChar right_value ) {
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
}
