/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.parsers.asn1parser;

import java.util.List;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenFactory;
import org.antlr.v4.runtime.IntStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenFactory;
import org.antlr.v4.runtime.TokenSource;

public class BlockTokenSource implements TokenSource {
	private List<Token> tokenList;
	int index;

	/** How to create token objects */
	protected TokenFactory<?> _factory = CommonTokenFactory.DEFAULT;

	BlockTokenSource(final List<Token> tokenList) {
		this.tokenList = tokenList;
		this.index = 0;
	}

	@Override
	/** {@inheritDoc} */
	public int getCharPositionInLine() {
		return -1;
	}

	@Override
	/** {@inheritDoc} */
	public CharStream getInputStream() {
		return null;
	}

	@Override
	/** {@inheritDoc} */
	public int getLine() {
		return 0;
	}

	@Override
	/** {@inheritDoc} */
	public String getSourceName() {
		return IntStream.UNKNOWN_SOURCE_NAME;
	}

	@Override
	/** {@inheritDoc} */
	public TokenFactory<?> getTokenFactory() {
		return _factory;
	}

	@Override
	/** {@inheritDoc} */
	public Token nextToken() {
		return tokenList.get(index++);
	}

	@Override
	/** {@inheritDoc} */
	public void setTokenFactory(final TokenFactory<?> factory) {
		this._factory = factory;
	}

}

