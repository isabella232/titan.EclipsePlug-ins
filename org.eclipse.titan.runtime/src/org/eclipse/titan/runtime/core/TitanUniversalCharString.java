/******************************************************************************
 * Copyright (c) 2000-2017 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;


/**
 * TTCN-3 Universal_charstring
 * @author Arpad Lovassy
 */
public class TitanUniversalCharString extends Base_Type {

	/** Internal data */
	private List<TitanUniversalChar> val_ptr;

	/** Character string values are stored in an optimal way */
	private StringBuilder cstr;

	/**
	 * true, if cstr is used <br>
	 * false, if val_ptr is used
	 */
	private boolean charstring;

	public TitanUniversalCharString() {
		super();
	}

	public TitanUniversalCharString( final TitanUniversalChar aOtherValue ) {
		val_ptr = new ArrayList<TitanUniversalChar>();
		val_ptr.add( aOtherValue );
		charstring = false;
	}

	public TitanUniversalCharString( final List<TitanUniversalChar> aOtherValue ) {
		val_ptr = copyList( aOtherValue );
		charstring = false;
	}

	public TitanUniversalCharString(final TitanUniversalChar[] aOther) {
		val_ptr = new ArrayList<TitanUniversalChar>();
		for(int i = 0; i < aOther.length; i++) {
			val_ptr.add( aOther[i] );
		}

		charstring = false;
	}

	public TitanUniversalCharString( final String aOtherValue ) {
		cstr = new StringBuilder( aOtherValue );
		charstring = true;
	}

	public TitanUniversalCharString( final StringBuilder aOtherValue ) {
		cstr = new StringBuilder( aOtherValue );
		charstring = true;
	}

	public TitanUniversalCharString( final TitanCharString aOtherValue ) {
		aOtherValue.mustBound( "Copying an unbound charstring value." );
		cstr = new StringBuilder( aOtherValue.getValue() );
		charstring = true;
	}

	public TitanUniversalCharString( final TitanUniversalCharString aOtherValue ) {
		aOtherValue.mustBound( "Copying an unbound universal charstring value." );
		val_ptr = copyList( aOtherValue.val_ptr );
		cstr = aOtherValue.cstr;
		charstring = aOtherValue.charstring;
	}

	private static List<TitanUniversalChar> copyList(final List<TitanUniversalChar> uList) {
		if ( uList == null ) {
			return null;
		}
		final List<TitanUniversalChar> clonedList = new ArrayList<TitanUniversalChar>( uList.size() );
		for (TitanUniversalChar uc : uList) {
			clonedList.add( new TitanUniversalChar( uc ) );
		}
		return clonedList;
	}

	public List<TitanUniversalChar> getValue() {
		return val_ptr;
	}

	//takes ownership of aOtherValue
	public void setValue( final List<TitanUniversalChar> aOtherValue ) {
		if ( aOtherValue != null) {
			val_ptr = aOtherValue;
			cstr = null;
			charstring = false;
		}
	}

	//originally operator=
	public TitanUniversalCharString assign( final TitanUniversalCharString aOtherValue ) {
		aOtherValue.mustBound( "Assignment of an unbound universal charstring value." );
		val_ptr = aOtherValue.val_ptr;

		return this;
	}
	
	@Override
	public TitanUniversalCharString assign(final Base_Type otherValue) {
		if (otherValue instanceof TitanUniversalCharString) {
			return assign((TitanUniversalCharString)otherValue);
		}
		
		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to universal charstring", otherValue));
	}

	public boolean isBound() {
		return charstring ? cstr != null : val_ptr != null;
	}

	public boolean isPresent() {
		return isBound();
	};

	public boolean isValue() {
		return isBound();
	}

	public void mustBound( final String aErrorMessage ) {
		if ( val_ptr == null ) {
			throw new TtcnError( aErrorMessage );
		}
	}

	//originally operator==
	public boolean operatorEquals( final TitanUniversalCharString aOtherValue ) {
		mustBound("Unbound left operand of universal charstring comparison.");
		aOtherValue.mustBound("Unbound right operand of universal charstring comparison.");

		return val_ptr.equals(aOtherValue.val_ptr);
	}
	
	@Override
	public boolean operatorEquals(final Base_Type otherValue) {
		if (otherValue instanceof TitanUniversalCharString) {
			return operatorEquals((TitanUniversalCharString)otherValue);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to universal charstring", otherValue));
	}

	//originally operator!=
	public boolean operatorNotEquals( final TitanUniversalCharString aOtherValue ) {
		return !operatorEquals( aOtherValue );
	}

	/**
	 * this + aOtherValue
	 * originally UNIVERSAL_CHARSTRING UNIVERSAL_CHARSTRING::operator+ (const universal_char& other_value) const
	 */
	public TitanUniversalCharString append( final TitanUniversalChar other_value ) {
		mustBound( "The left operand of concatenation is an unbound universal charstring value." );
		if (charstring) {
			if (other_value.is_char()) {
				final TitanUniversalCharString ret_val = new TitanUniversalCharString( cstr.append( other_value.getUc_cell() ) );
				return ret_val;
			} else {
				final List<TitanUniversalChar> ulist = new ArrayList<TitanUniversalChar>();
				for (int i = 0; i < cstr.length(); ++i) {
					final TitanUniversalChar uc = new TitanUniversalChar( (char)0, (char)0, (char)0, cstr.charAt( i ) );
					ulist.add( uc );
				}
				ulist.add( other_value );
				final TitanUniversalCharString ret_val = new TitanUniversalCharString();
				ret_val.setValue( ulist );
				return ret_val;
			}
		}

		final TitanUniversalCharString ret_val = new TitanUniversalCharString( val_ptr );
		ret_val.val_ptr.add( other_value );
		return ret_val;
	}

	/**
	 * this + aOtherValue
	 * originally UNIVERSAL_CHARSTRING UNIVERSAL_CHARSTRING::operator+ (const char* other_value) const
	 */
	public TitanUniversalCharString append( final String other_value ) {
		mustBound("The left operand of concatenation is an unbound universal charstring value.");
		int other_len;
		if (other_value == null) other_len = 0;
		else other_len = other_value.length();
		if (other_len == 0) return this;
		if ( charstring ) {
			return new TitanUniversalCharString( cstr.append( other_value ) );
		}
		final TitanUniversalCharString ret_val = new TitanUniversalCharString( val_ptr );
		for (int i = 0; i < other_len; i++) {
			final TitanUniversalChar uc = new TitanUniversalChar( (char)0, (char)0, (char)0, other_value.charAt( i ));
			ret_val.append( uc );
		}
		return ret_val;
	}

	/**
	 * this + aOtherValue
	 * originally UNIVERSAL_CHARSTRING UNIVERSAL_CHARSTRING::operator+ (const CHARSTRING& other_value) const
	 */
	public TitanUniversalCharString append( final TitanCharString other_value ) {
		mustBound("The left operand of concatenation is an unbound universal charstring value.");
		other_value.mustBound("The right operand of concatenation is an unbound charstring value.");
		return append( other_value.getValue().toString() );
	}

	/**
	 * this + aOtherValue
	 * originally UNIVERSAL_CHARSTRING UNIVERSAL_CHARSTRING::operator+ (const CHARSTRING_ELEMENT& other_value) const
	 */
	public TitanUniversalCharString append( final TitanCharString_Element other_value ) {
		mustBound("The left operand of concatenation is an unbound universal charstring value.");
		other_value.mustBound("The right operand of concatenation is an unbound charstring element.");
		if ( charstring ) {
			return new TitanUniversalCharString( cstr.append( other_value.get_char() ) );
		}
		final TitanUniversalCharString ret_val = new TitanUniversalCharString( val_ptr );
		final TitanUniversalChar uc = new TitanUniversalChar( (char)0, (char)0, (char)0, other_value.get_char());
		ret_val.append( uc );
		return ret_val;
	}

	
	/**
	 * this + aOtherValue
	 * originally UNIVERSAL_CHARSTRING UNIVERSAL_CHARSTRING::operator+ (const UNIVERSAL_CHARSTRING& other_value) const
	 */
	public TitanUniversalCharString append( final TitanUniversalCharString other_value ) {
		mustBound("The left operand of concatenation is an unbound universal charstring value.");
		other_value.mustBound("The right operand of concatenation is an unbound universal charstring value.");
		if (charstring) {
			if (cstr.length() == 0)
				return other_value;
			if (other_value.charstring) {
				if (other_value.cstr.length() == 0)
					return this;
				final TitanUniversalCharString ret_val = new TitanUniversalCharString( cstr.append( other_value.cstr ) );
				return ret_val;
			} else {
				if (other_value.val_ptr.size() == 0)
					return this;
				final List<TitanUniversalChar> ulist = new ArrayList<TitanUniversalChar>();
				for (int i = 0; i < cstr.length(); i++) {
					final TitanUniversalChar uc = new TitanUniversalChar( (char)0, (char)0, (char)0, cstr.charAt( i ) );
					ulist.add( uc );
				}
				ulist.addAll( other_value.val_ptr );
				final TitanUniversalCharString ret_val = new TitanUniversalCharString();
				ret_val.setValue( ulist );
				return ret_val;
			}
		} else {
			if (other_value.charstring) {
				final TitanUniversalCharString ret_val = new TitanUniversalCharString( val_ptr );
				final StringBuilder cs = other_value.cstr;
				final int cslen = cs.length();
				for (int i = 0; i < cslen; i++) {
					final TitanUniversalChar uc = new TitanUniversalChar( (char)0, (char)0, (char)0, cs.charAt( i ) );
					ret_val.getValue().add( uc );
				}
				return ret_val;
			} else {
				if (val_ptr.size() == 0) return other_value;
				if (other_value.val_ptr.size() == 0) return this;
				final TitanUniversalCharString ret_val = new TitanUniversalCharString( val_ptr );
				ret_val.getValue().addAll( other_value.val_ptr );
				return ret_val;
			}
		}
	}

	/**
	 * this + aOtherValue
	 * originally UNIVERSAL_CHARSTRING UNIVERSAL_CHARSTRING::operator+ (const UNIVERSAL_CHARSTRING_ELEMENT& other_value) const
	 */
	public TitanUniversalCharString append( final TitanUniversalCharString_Element other_value ) {
		mustBound("The left operand of concatenation is an unbound universal charstring value.");
		other_value.mustBound("The right operand of concatenation is an unbound universal charstring element.");
		if ( charstring ) {
			return new TitanUniversalCharString( cstr.append( other_value.get_char() ) );
		}
		final TitanUniversalCharString ret_val = new TitanUniversalCharString( val_ptr );
		ret_val.append( other_value.get_char() );
		return ret_val;
	}

	public void cleanUp() {
		val_ptr = null;
		cstr = null;
	}

	/**
	 * @return number of digits 
	 */
	private int size() {
		return charstring ? cstr.length() : val_ptr.size();
	}

	//originally operator[](int)
	public TitanUniversalCharString_Element getAt(final int index_value) {
		if ( !isBound() && index_value == 0 ) {
			if ( charstring ) {
				cstr = new StringBuilder();
			} else {
				val_ptr = new ArrayList<TitanUniversalChar>();
			}
			return new TitanUniversalCharString_Element(false, this, 0);
		} else {
			mustBound("Accessing an element of an unbound universal charstring value.");
			if (index_value < 0) {
				throw new TtcnError("Accessing an universal charstring element using a negative index (" + index_value + ").");
			}

			final int n_nibbles = size();
			if (index_value > n_nibbles) {
				throw new TtcnError("Index overflow when accessing a universal charstring element: The index is " + index_value +
					", but the string has only " + n_nibbles + " characters.");
			}
			if (index_value == n_nibbles) {
				if ( charstring ) {
					cstr.append( (char)0 );
				} else {
					val_ptr.add( new TitanUniversalChar( (char)0, (char)0, (char)0, (char)0 ) );
				}
				return new TitanUniversalCharString_Element( false, this, index_value );
			} else {
				return new TitanUniversalCharString_Element( true, this, index_value );
			}
		}
	}

	//originally operator[](const INTEGER&)
	public TitanUniversalCharString_Element getAt(final TitanInteger index_value) {
		index_value.mustBound("Indexing a universal charstring value with an unbound integer value.");
		return getAt( index_value.getInt() );
	}

	//originally operator[](int) const
	public TitanUniversalCharString_Element constGetAt( final int index_value ) {
		mustBound("Accessing an element of an unbound universal charstring value.");
		if (index_value < 0) {
			throw new TtcnError("Accessing an universal charstring element using a negative index (" + index_value + ").");
		}
		final int n_nibbles = val_ptr.size();
		if (index_value >= n_nibbles) {
			throw new TtcnError("Index overflow when accessing a universal charstring element: The index is " + index_value +
					", but the string has only " + n_nibbles + " characters.");
		}
		return new TitanUniversalCharString_Element(true, this, index_value);
	}

	//originally operator[](const INTEGER&) const
	public TitanUniversalCharString_Element constGetAt( final TitanInteger index_value ) {
		index_value.mustBound("Indexing a universal charstring value with an unbound integer value.");
		return constGetAt( index_value.getInt() );
	}

	@Override
	public String toString() {
		if ( !isBound() ) {
			return "<unbound>";
		}
		if ( charstring ) {
			return cstr.toString();
		} else {
			return val_ptr.toString();
		}
	}

	final TitanUniversalChar charAt( final int i ) {
		//TODO, handle charstring case also if needed
		return val_ptr.get( i );
	}

	final void setCharAt( final int i, final TitanUniversalChar c ) {
		//TODO, handle charstring case also if needed
		val_ptr.set( i, c );
	}
}
