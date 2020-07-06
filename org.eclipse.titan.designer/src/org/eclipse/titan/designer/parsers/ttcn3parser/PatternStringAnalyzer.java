/******************************************************************************
 * Copyright (c) 2000-2020 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.parsers.ttcn3parser;

import java.io.Reader;
import java.io.StringReader;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenFactory;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.UnbufferedCharStream;
import org.eclipse.core.resources.IFile;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.TTCN3.templates.PatternString;

/**
 * Analyzer for the TTCN patterns
 * 
 * @author Gergo Ujhelyi
 */
public class PatternStringAnalyzer {
	
	private PatternString analyzedPatternString;
	
	public PatternString getAnalyzedPatternString() {
		return analyzedPatternString;
	}
	
	public static PatternString parse_pattern(final String p_str, final Location p_loc, final Token startToken, final Token endToken)	{
		final Reader reader = new StringReader(p_str);

		final CharStream charStream = new UnbufferedCharStream(reader);
		final PatternStringLexer lexer = new PatternStringLexer(charStream);
		lexer.setStartToken(startToken);
		lexer.setTokenFactory(new CommonTokenFactory(true));
		lexer.setOffset(p_loc.getOffset());
		lexer.setLine(p_loc.getLine());
		lexer.setCharPositionInLine(0);
		lexer.setActualFile((IFile)p_loc.getFile());
		lexer.setTokenString(p_str);
		lexer.setActualLocation(p_loc);

		while(lexer.nextToken().getType()!=Token.EOF) {}

		final PatternString retVal = lexer.getPatternString();
		return retVal; 
	}
}
