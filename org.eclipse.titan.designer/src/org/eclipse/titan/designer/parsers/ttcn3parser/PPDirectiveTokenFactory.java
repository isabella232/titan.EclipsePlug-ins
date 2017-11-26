/******************************************************************************
 * Copyright (c) 2000-2017 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.parsers.ttcn3parser;

/**
 * @author Laszlo Baji
 * */
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenFactory;
import org.antlr.v4.runtime.TokenSource;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.misc.Pair;

public class PPDirectiveTokenFactory implements TokenFactory<CommonToken>{
	public static final TokenFactory<CommonToken> DEFAULT = new PPDirectiveTokenFactory();
	protected final boolean copyText;
	Token token;

	public PPDirectiveTokenFactory() {
		this.copyText = false;
		this.token = new CommonToken(Token.EOF);
	}

	public PPDirectiveTokenFactory (final boolean copyText, final Token token) {
		this.copyText = copyText;
		this.token = token;
	}

	@Override
	public CommonToken create(final int type, final String text) {
		return new CommonToken(type, text);
	}

	@Override
	public CommonToken create(final Pair<TokenSource, CharStream> source, final int type,
			final String text, final int channel, final int start, final int stop, final int line, final int charPositionInLine) {
		CommonToken t = new CommonToken(source, type, channel, start, stop);
		t.setLine(line);
		t.setStartIndex(start + token.getStartIndex());
		t.setStopIndex(stop + token.getStartIndex() + 1);
		t.setCharPositionInLine(charPositionInLine);
		if ( text!=null ) {
			t.setText(text);
		} else if ( copyText && source.b != null ) {
			t.setText(source.b.getText(Interval.of(start,stop)));
		}

		return t;
	}

}
