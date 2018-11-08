/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core.cfgparser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenFactory;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.UnbufferedCharStream;
import org.eclipse.titan.runtime.core.TtcnError;

/**
 * Syntactic analyzer for CFG files
 * @author Arpad Lovassy
 */
public final class CfgAnalyzer {

	ExecuteSectionHandler executeSectionHandler = null;

	public ExecuteSectionHandler getExecuteSectionHandler() {
		return executeSectionHandler;
	}

	/**
	 * Parses the provided elements.
	 * If the contents of an editor are to be parsed, than the file parameter is only used to report the errors to.
	 *
	 * @param file the file to parse, and report the errors to
	 * @param code the contents of an editor, or null.
	 */
	public void parse(final File file, final String code) {
		String fileName = "<unknown file>";
		if ( file != null ) {
			fileName = file.getName();
		}
		directParse(file, fileName, code);
	}

	/**
	 * Parses the provided elements.
	 * If the contents of an editor are to be parsed, than the file parameter is only used to report the errors to.
	 *
	 * @param file the file to parse
	 * @param fileName the name of the file, to refer to.
	 * @param code the contents of an editor, or null.
	 *
	 * @return {@code true} if there were errors in the file, {@code false} otherwise
	 */
	public boolean directParse(final File file, final String fileName, final String code) {
		final Reader reader;
		if (null != code) {
			reader = new StringReader(code);
		} else if (null != file) {
			try {
				reader = new BufferedReader(new InputStreamReader( new FileInputStream(file), StandardCharsets.UTF8));
			} catch (FileNotFoundException e) {
				//TODO
				//ErrorReporter.logExceptionStackTrace("Could not get the contents of `" + fileName + "'", e);
				throw new TtcnError(e);
			}
		} else {
			//TODO
			//ErrorReporter.INTERNAL_ERROR("CfgAnalyzer.directParse(): nothing to parse");
			return true;
		}

		final CharStream charStream = new UnbufferedCharStream(reader);
		final RuntimeCfgLexer lexer = new RuntimeCfgLexer(charStream);
		lexer.setTokenFactory(new CommonTokenFactory(true));
		lexer.removeErrorListeners(); // remove ConsoleErrorListener
		CFGListener lexerListener = new CFGListener(fileName);
		lexer.addErrorListener(lexerListener);

		// 1. Previously it was UnbufferedTokenStream(lexer), but it was changed to BufferedTokenStream, because UnbufferedTokenStream seems to be unusable. It is an ANTLR 4 bug.
		// Read this: https://groups.google.com/forum/#!topic/antlr-discussion/gsAu-6d3pKU
		// pr_PatternChunk[StringBuilder builder, boolean[] uni]:
		//   $builder.append($v.text); <-- exception is thrown here: java.lang.UnsupportedOperationException: interval 85..85 not in token buffer window: 86..341
		// 2. Changed from BufferedTokenStream to CommonTokenStream, otherwise tokens with "-> channel(HIDDEN)" are not filtered out in lexer.
		final CommonTokenStream tokenStream = new CommonTokenStream( lexer );
		final RuntimeCfgParser parser = new RuntimeCfgParser( tokenStream );
		parser.setActualFile( file );
		// parse tree is built by default
		parser.setBuildParseTree(false);
		// remove ConsoleErrorListener
		parser.removeErrorListeners();
		CFGListener parserListener = new CFGListener(fileName);
		parser.addErrorListener(parserListener);

		parser.pr_ConfigFile();
		
		executeSectionHandler = parser.getExecuteSectionHandler();

		try {
			reader.close();
		} catch (IOException e) {
		}

		return lexerListener.encounteredError() || parserListener.encounteredError();
	}
}
