/******************************************************************************
 * Copyright (c) 2000-2017 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

//TODO: maybe change it to exception
public class TtcnError extends Error {

	public TtcnError( final String aErrorMessage ) {
		//FIXME implement
		super( aErrorMessage );

	}

	// FIXME this function is not ok here.
	// As soon as we have implemented all functions from the old core it should be moved.
	// Till then similarity with the old structure rules.
	public static void TtcnWarning(final String aWarningMessage) {
		//FIXME implement logging of warnings
		System.out.println("warning: " + aWarningMessage);
	}
}
