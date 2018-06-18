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
import org.antlr.v4.runtime.IntStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenFactory;
import org.antlr.v4.runtime.TokenSource;

public class BlockTokenSource implements TokenSource {
	private List<Token> tokenList;
	int index;

	BlockTokenSource(final List<Token> tokenList) {
		this.tokenList = tokenList;
		this.index = 0;
	}

	@Override
	public int getCharPositionInLine() {
		return -1;
	}

	@Override
	public CharStream getInputStream() {
		return null;
	}

	@Override
	public int getLine() {
		return 0;
	}

	@Override
	public String getSourceName() {
		return IntStream.UNKNOWN_SOURCE_NAME;
	}

	@Override
	public TokenFactory<?> getTokenFactory() {
		return null;
	}

	@Override
	public Token nextToken() {
		return tokenList.get(index++);
	}

	@Override
	public void setTokenFactory(final TokenFactory<?> arg0) {
		//Do nothing
	}

}

