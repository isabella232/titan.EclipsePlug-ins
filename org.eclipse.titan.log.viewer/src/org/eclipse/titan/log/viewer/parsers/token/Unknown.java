/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.parsers.token;

import org.eclipse.titan.log.viewer.parsers.Constants;

/**
 * Sets the type for an unknown token
 */
public class Unknown extends Token {

	/**
	 * Constructor
	 * @param token the token
	 */
	public Unknown(final String token) {
		super(token);
	}

	@Override
	public int getType() {
		return Constants.UNKNOWN;
	}

}
