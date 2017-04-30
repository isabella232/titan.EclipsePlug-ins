/******************************************************************************
 * Copyright (c) 2000-2017 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

/**
 *
 * @author Arpad Lovassy
 */
public class TitanUniversalCharString_Element {
	boolean bound_flag;
	TitanUniversalCharString str_val;
	int char_pos;

	public TitanUniversalCharString_Element( final boolean par_bound_flag, final TitanUniversalCharString par_str_val, final int par_char_pos ) {
		bound_flag = par_bound_flag;
		str_val = par_str_val;
		char_pos = par_char_pos;
	}

	public boolean isBound() {
		return bound_flag;
	}

	public boolean isValue() {
		return bound_flag;
	}

	public void mustBound( final String aErrorMessage ) {
		if ( !bound_flag ) {
			throw new TtcnError( aErrorMessage );
		}
	}

	//originally operator=
	public TitanUniversalCharString_Element assign( final TitanUniversalCharString_Element other_value ) {
		other_value.mustBound("Assignment of an unbound charstring element.");
		bound_flag = true;
		str_val.setCharAt( char_pos, other_value.str_val.charAt( other_value.char_pos ) );
		return this;
	}

	//originally operator=
	public TitanUniversalCharString_Element assign( final TitanUniversalCharString other_value ) {
		other_value.mustBound("Assignment of unbound charstring value.");
		if (other_value.getValue().size() != 1) {
			throw new TtcnError( "Assignment of a charstring value with length other than 1 to a charstring element." );
		}
		bound_flag = true;
		str_val.setCharAt( char_pos, other_value.charAt(0) );
		return this;
	}

	//originally operator==
	public boolean equalsTo( final TitanUniversalCharString_Element other_value ) {
		mustBound("Unbound left operand of charstring element comparison.");
		other_value.mustBound("Unbound right operand of charstring comparison.");
		return get_char() == other_value.str_val.charAt( other_value.char_pos );
	}

	//originally operator==
	public boolean equalsTo( final TitanUniversalCharString other_value ) {
		mustBound("Unbound left operand of charstring element comparison.");
		other_value.mustBound("Unbound right operand of charstring element comparison.");
		if (other_value.getValue().size() != 1) {
			return false;
		}
		return get_char() == other_value.charAt(0);
	}

	public TitanUniversalChar get_char() {
		return str_val.charAt( char_pos );
	}

}
