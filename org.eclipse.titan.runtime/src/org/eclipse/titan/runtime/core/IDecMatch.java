/******************************************************************************
 * Copyright (c) 2000-2017 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

/**
 * Interface/base class for decoded content matching
 * <br>
 * For every decmatch template the compiler generates a new class that inherits
 * this one and implements its virtual functions. An instance of the new class
 * is stored in the template object, which calls the appropriate virtual
 * functions when the template object's match() or log() functions are called.
 * 
 * originally Dec_Match_Interface
 * @author Arpad Lovassy
 */
interface IDecMatch {
	//TODO
	//public boolean match(TTCN_Buffer aBuffer);

	public void log();
	/** this returns the decoding result of the last successfully matched value,
	 * which may be used by value and parameter redirect classes for optimization
	 * (so they don't have to decode the same value again)
	 * the function returns a void pointer (since the decoding could result in a
	 * value of any type), which is converted to the required type when used */
	public void get_dec_res();

	/** this returns the decoded type's descriptor, which may be used by value and
	 * parameter redirect classes to determine whether the redirected value would
	 * be decoded into the same type as the type used in this decmatch template */
	//TODO
	//public final TTCN_Typedescriptor_t get_type_descr();
};
