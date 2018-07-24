/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core.cfgparser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenFactory;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.UnbufferedCharStream;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;

/**
 * 
 * @author Arpad Lovassy
 */
public final class CfgAnalyzer {

	/**
	 * Parses the provided elements.
	 * If the contents of an editor are to be parsed, than the file parameter is only used to report the errors to.
	 *
	 * @param file the file to parse, and report the errors to
	 * @param code the contents of an editor, or null.
	 */
	public void parse(final IFile file, final String code) {
		String fileName = "<unknown file>";
		if ( file != null ) {
			fileName = file.getFullPath().toOSString();
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
	 */
	public void directParse(final IFile file, final String fileName, final String code) {
		final Reader reader;
		if (null != code) {
			reader = new StringReader(code);
		} else if (null != file) {
			try {
				reader = new BufferedReader(new InputStreamReader(file.getContents(), StandardCharsets.UTF8));
			} catch (CoreException e) {
				//TODO
				//ErrorReporter.logExceptionStackTrace("Could not get the contents of `" + fileName + "'", e);
				return;
			}
		} else {
			//TODO
			//ErrorReporter.INTERNAL_ERROR("CfgAnalyzer.directParse(): nothing to parse");
			return;
		}

		final CharStream charStream = new UnbufferedCharStream(reader);
		final RuntimeCfgLexer lexer = new RuntimeCfgLexer(charStream);
		lexer.setTokenFactory(new CommonTokenFactory(true));
		lexer.removeErrorListeners(); // remove ConsoleErrorListener

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
		parser.pr_ConfigFile();

		try {
			reader.close();
		} catch (IOException e) {
		}
	}

	public static boolean process_config_file(IFile config_file) {
		final CfgAnalyzer cfgAnalyzer = new CfgAnalyzer();
		cfgAnalyzer.directParse(config_file, config_file.getName(), null);
		return false;
	}
}
