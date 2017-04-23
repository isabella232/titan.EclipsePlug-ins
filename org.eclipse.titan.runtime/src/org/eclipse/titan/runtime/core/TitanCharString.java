/******************************************************************************
 * Copyright (c) 2000-2017 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

import java.text.MessageFormat;


/**
 * TTCN-3 charstring
 * @author Arpad Lovassy
 */
public class TitanCharString extends Base_Type {

	/**
	 * charstring value.
	 */
	private StringBuilder val_ptr;

	public TitanCharString() {
		super();
	}

	public TitanCharString( final StringBuilder aOtherValue ) {
		val_ptr = new StringBuilder( aOtherValue );
	}

	public TitanCharString( final String aOtherValue ) {
		copyValue( aOtherValue );
	}

	public TitanCharString( final TitanCharString aOtherValue ) {
		aOtherValue.mustBound( "Copying an unbound charstring value." );
		val_ptr = aOtherValue.val_ptr;
	}

	//originally char*()
	public StringBuilder getValue() {
		mustBound("Getting an unbound charstring value as string.");
		return val_ptr;
	}

	private void copyValue( final String aOtherValue ) {
		val_ptr = new StringBuilder( aOtherValue );
	}

	private void copyValue( final StringBuilder aOtherValue ) {
		val_ptr = new StringBuilder( aOtherValue );
	}

	//originally operator=
	public TitanCharString assign( final TitanCharString aOtherValue ) {
		aOtherValue.mustBound( "Assignment of an unbound charstring value." );
		copyValue( aOtherValue.val_ptr );

		return this;
	}
	
	@Override
	public TitanCharString assign(final Base_Type otherValue) {
		if (otherValue instanceof TitanCharString) {
			return assign((TitanCharString)otherValue);
		}
		
		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to charstring", otherValue));
	}

	public boolean isBound() {
		return val_ptr != null;
	}

	public boolean isPresent() {
		return isBound();
	};

	public boolean isValue() {
		return val_ptr != null;
	}

	public void mustBound( final String aErrorMessage ) {
		if ( val_ptr == null ) {
			throw new TtcnError( aErrorMessage );
		}
	}

	/**
	 * this + aOtherValue
	 * originally operator&
	 */
	public TitanCharString append( final TitanCharString aOtherValue ) {
		mustBound( "Unbound left operand of charstring addition." );
		aOtherValue.mustBound( "Unbound right operand of charstring addition." );

		return new TitanCharString( val_ptr.append( aOtherValue.val_ptr ) );
	}

	//originally operator==
	public boolean operatorEquals( final TitanCharString aOtherValue ) {
		mustBound("Unbound left operand of charstring comparison.");
		aOtherValue.mustBound("Unbound right operand of charstring comparison.");

		return val_ptr.equals(aOtherValue.val_ptr);
	}
	
	@Override
	public boolean operatorEquals(final Base_Type otherValue) {
		if (otherValue instanceof TitanCharString) {
			return operatorEquals((TitanCharString)otherValue);
		}
		
		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to charstring", otherValue));
	}

	//originally operator!=
	public boolean operatorNotEquals( final TitanCharString aOtherValue ) {
		return !operatorEquals( aOtherValue );
	}

	public void cleanUp() {
		val_ptr = null;
	}

	//originally operator[](int)
	public TitanCharString_Element getAt(int index_value) {
		if (val_ptr == null && index_value == 0) {
			val_ptr = new StringBuilder();
			return new TitanCharString_Element(false, this, 0);
		} else {
			mustBound("Accessing an element of an unbound charstring value.");
			if (index_value < 0) {
				throw new TtcnError("Accessing an charstring element using a negative index (" + index_value + ").");
			}

			int n_nibbles = val_ptr.length();
			if (index_value > n_nibbles) {
				throw new TtcnError("Index overflow when accessing a charstring element: The index is " + index_value +
					", but the string has only " + n_nibbles + " characters.");
			}
			if (index_value == n_nibbles) {
				return new TitanCharString_Element( false, this, index_value );
			} else {
				return new TitanCharString_Element( true, this, index_value );
			}
		}
	}

	//originally operator[](const INTEGER&)
	public TitanCharString_Element getAt(final TitanInteger index_value) {
		index_value.mustBound("Indexing a charstring value with an unbound integer value.");
		return getAt( index_value.getInt() );
	}

	//originally operator[](int) const
	public TitanCharString_Element constGetAt( final int index_value ) {
		mustBound("Accessing an element of an unbound charstring value.");
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

	//originally operator[](const INTEGER&) const
	public TitanCharString_Element constGetAt( final TitanInteger index_value ) {
		index_value.mustBound("Indexing a charstring value with an unbound integer value.");
		return constGetAt( index_value.getInt() );
	}

	@Override
	public String toString() {
		if ( val_ptr == null ) {
			return "<unbound>";
		}
		return val_ptr.toString();
	}
}
