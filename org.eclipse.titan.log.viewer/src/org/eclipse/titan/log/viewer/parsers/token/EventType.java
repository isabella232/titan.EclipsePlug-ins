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
 * Sets the type, the possible following token types and delimiters for a EventType
 */
public class EventType extends Token {

	/**
	 * Constructor
	 * @param token the token
	 */
	public EventType(final String token) {
		super(token);

		setTokenList(Constants.SOURCE_INFORMATION | Constants.MESSAGE);
		setDelimiterList(Constants.WHITE_SPACE | Constants.END_OF_RECORD);
	}

	@Override
	public int getType() {
		return Constants.EVENT_TYPE;
	}

}
