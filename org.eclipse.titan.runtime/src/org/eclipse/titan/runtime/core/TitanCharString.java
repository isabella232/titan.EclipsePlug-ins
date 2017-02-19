/******************************************************************************
 * Copyright (c) 2000-2017 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;


/**
 * TTCN-3 charstring
 * @author Arpad Lovassy
 */
public class TitanCharString {

	//TODO: implement ANY and/or OMIT

	/**
	 * charstring value.
	 * It can be null if ANY or OMIT
	 */
	private String val_ptr;

	public TitanCharString() {
		super();
	}

	public TitanCharString( final String aOtherValue ) {
		val_ptr = aOtherValue;
	}

	public TitanCharString( final TitanCharString aOtherValue ) {
		aOtherValue.mustBound( "Copying an unbound charstring value." );
		val_ptr = aOtherValue.val_ptr;
	}

	//originally char*()
	public String getValue() {
		return val_ptr;
	}

	public void setValue( final String aOtherValue ) {
		val_ptr = aOtherValue;
	}
	
	//originally operator=
	public TitanCharString assign( final TitanCharString aOtherValue ) {
		aOtherValue.mustBound( "Assignment of an unbound charstring value." );
		val_ptr = aOtherValue.val_ptr;

		return this;
	}

	public void mustBound( final String aErrorMessage ) {
		if ( val_ptr == null ) {
			throw new TtcnError( aErrorMessage );
		}
	}
	
	/**
	 * this + aOther
	 * originally operator+
	 */
	public TitanCharString add( final TitanCharString aOtherValue ) {
		mustBound( "Unbound left operand of charstring addition." );
		aOtherValue.mustBound( "Unbound right operand of charstring addition." );

		return new TitanCharString( val_ptr + aOtherValue.val_ptr );
	}

	//originally operator==
	public boolean operatorEquals( final TitanCharString aOtherValue ) {
		mustBound("Unbound left operand of charstring comparison.");
		aOtherValue.mustBound("Unbound right operand of charstring comparison.");

		return val_ptr.equals(aOtherValue.val_ptr);
	}

	//originally operator!=
	public boolean operatorNotEquals( final TitanCharString aOtherValue ) {
		return !operatorEquals( aOtherValue );
	}

	public void cleanUp() {
		val_ptr = null;
	}
	
	@Override
	public String toString() {
		if ( val_ptr == null ) {
			return "<unbound>";
		}
		return val_ptr;
	}
}
