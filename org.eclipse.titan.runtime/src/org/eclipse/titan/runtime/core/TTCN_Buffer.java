/******************************************************************************
 * Copyright (c) 2000-2017 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

/**
 * Buffer used by the different encoders/decoders.
 * 
 * FIXME the current implementation is only a placeholder to mark the architectural borders.
 * */
public class TTCN_Buffer {
	//FIXME a lot to implement here
	 /** 
	  * Stores the current contents of the buffer to variable p_os.
	  * 
	  * @param p_os the variable to store the contents of the buffer into.
	  * */
	public void get_string(final TitanOctetString p_os) {
		throw new TtcnError("get_string() for TTCN_Buffer is not implemented!");
	}
}
