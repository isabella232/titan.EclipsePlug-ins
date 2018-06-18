/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.parsers.token;

/**
 * Abstract class for representing a Token
 */
public abstract class Token {

	private final String token;
	private int tokenList;
	private int delimiterList;

	/**
	 * Constructor
	 * @param token the token
	 */
	public Token(final String token) {
		this.token = token;
	}

	/**
	 * Gets the type
	 * @return the type
	 */
	public abstract int getType();

	/**
	 * Gets the content
	 * @return the content
	 */
	public String getContent() {
		return this.token;
	}

	/**
	 * Gets the token list
	 * @return the token list
	 */
	public int getTokenList() {
		return this.tokenList;
	}

	/**
	 * Set and returns a new token list
	 * @param tokenList the new token list to set
	 * @return the token list
	 */
	public int setTokenList(final int tokenList) {
		this.tokenList = tokenList;
		return this.tokenList;
	}

	/**
	 * Gets the delimiter list
	 * @return the delimiter list
	 */
	public int getDelimiterList() {
		return this.delimiterList;
	}

	/**
	 * Sets a new delimiter list
	 * @param delimiterList the new delimiter list
	 */
	public void setDelimiterList(final int delimiterList) {
		this.delimiterList = delimiterList;
	}

}
