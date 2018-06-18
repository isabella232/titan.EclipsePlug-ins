/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.common.parsers;

import org.antlr.v4.runtime.Lexer;

/**
 * USED FOR LOGGING PURPOSES.<br>
 * Utility class to get the token name from token index for logging purpose.<br>
 * Token name can be calculated in 2 ways:
 * <ol>
 * <li> Generate ILexerLogUtil for each lexer from ...Lexer.java,
 *      see <titan.EclipsePlug-ins root>/Tools/antlr4_generate_logutil.pl<br>
 *      This is preferred, it is accurate
 * <li> Get token name like this: aTokenType == -1 ? "EOF" : aLexer.getRuleNames()[ aTokenType - 1 ]<br>
 *      There are 2 problems with it:
 *      <ol>
 *      <li> fragments are not tokens, but they are listed in ruleNames[], for example: CfgLexer FR_NUMBER1
 *      <li> tokens, which are defined, but has no rule are not listed in ruleNames[], for example: Ttcn3Lexer MACRO
 *      </ol>
 *      so after these cases the lexer rule names are in wrong position, it is a bug in ANTLR 4.4<br>
 *      But it can be still used in general cases if no ILexerLogUtil is generated
 * </ol>
 * @author Arpad Lovassy
 */
public class TokenNameResolver {

	/**
	 * token name resolver for case 1
	 */
	private ILexerLogUtil mLexerLogUtil = null;

	/**
	 * token name resolver for case 2
	 */
	private Lexer mLexer = null;

	/**
	 * Default constructor, in this case no resolving is made, token index is displayed
	 */
	public TokenNameResolver() {
	}

	/**
	 * Constructor for case 1
	 * @param aLexerLogUtil token name resolver for case 1
	 */
	public TokenNameResolver( final ILexerLogUtil aLexerLogUtil ) {
		mLexerLogUtil = aLexerLogUtil;
	}

	/**
	 * Constructor for case 2
	 * @param aLexerLogUtil token name resolver for case 2
	 */
	public TokenNameResolver( final Lexer aLexer ) {
		mLexer = aLexer;
	}

	/**
	 * @param aTokenType token type, possible values: -1 (EOF), otherwise token types start from 1
	 * @return resolved token name
	 */
	public String getTokenName( final int aTokenType ) {
		if ( mLexerLogUtil != null ) {
			return mLexerLogUtil.getTokenName( aTokenType );
		} else if ( mLexer != null ) {
			return aTokenType == -1 ? "EOF" : mLexer.getRuleNames()[ aTokenType - 1 ];
		} else {
			return "" + aTokenType;
		}
	}
}
