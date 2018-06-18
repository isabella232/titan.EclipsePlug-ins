/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

import org.eclipse.titan.runtime.core.Base_Type.TTCN_Typedescriptor;

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
public interface IDecode_Match {
	public boolean match(final TTCN_Buffer buffer);

	public void log();
	/** this returns the decoding result of the last successfully matched value,
	 * which may be used by value and parameter redirect classes for optimization
	 * (so they don't have to decode the same value again)
	 * the function returns a void pointer (since the decoding could result in a
	 * value of any type), which is converted to the required type when used */
	public Object get_dec_res();

	/** this returns the decoded type's descriptor, which may be used by value and
	 * parameter redirect classes to determine whether the redirected value would
	 * be decoded into the same type as the type used in this decmatch template */
	public TTCN_Typedescriptor get_type_descr();
}
