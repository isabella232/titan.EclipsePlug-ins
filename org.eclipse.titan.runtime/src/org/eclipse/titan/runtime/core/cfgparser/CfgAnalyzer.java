/******************************************************************************
 * Copyright (c) 2000-2020 Ericsson Telecom AB
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
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;

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

	static final String TEMP_CFG_FILENAME = "temp.cfg";

	private ExecuteSectionHandler executeSectionHandler = null;
	private MCSectionHandler mcSectionHandler = null;

	public ExecuteSectionHandler getExecuteSectionHandler() {
		return executeSectionHandler;
	}

	/**
	 * Parses a file.
	 *
	 * @param file the file to parse.
	 * @return {@code true} if there were errors in the file, {@code false} otherwise
	 */
	public boolean parse(final File file) {
		String fileName = "<unknown file>";
		if ( file != null ) {
			fileName = file.getName();
		}
		return directParse(file, fileName, null);
	}

	/**
	 * Parses a string.
	 *
	 * @param code the source code to parse.
	 * @return {@code true} if there were errors in the file, {@code false} otherwise
	 */
	public boolean parse(final String code) {
		final String fileName = "<unknown file>";
		return directParse(null, fileName, code);
	}

	/**
	 * Create and initialize a new CFG Lexer object
	 * @param reader file reader
	 * @param lexerListener listener for ANTLR lexer/parser errors, it can be null
	 * @return the created lexer object
	 */
	static RuntimeCfgLexer createLexer(final Reader reader, final CFGListener lexerListener) {
		final CharStream charStream = new UnbufferedCharStream(reader);
		final RuntimeCfgLexer lexer = new RuntimeCfgLexer(charStream);
		lexer.setTokenFactory(new CommonTokenFactory(true));
		if ( lexerListener != null ) {
			lexer.removeErrorListeners(); // remove ConsoleErrorListener
			lexer.addErrorListener(lexerListener);
		}
		return lexer;
	}

	/**
	 * Create and initialize a new CFG Lexer object
	 * @param reader file reader
	 * @param lexerListener listener for ANTLR lexer/parser errors, it can be null
	 * @return the created lexer object
	 */
	static CommonTokenStream createTokenStream(final Reader reader, final CFGListener lexerListener) {
		final RuntimeCfgLexer lexer = createLexer(reader, lexerListener);
		// 1. Previously it was UnbufferedTokenStream(lexer), but it was changed to BufferedTokenStream, because UnbufferedTokenStream seems to be unusable. It is an ANTLR 4 bug.
		// Read this: https://groups.google.com/forum/#!topic/antlr-discussion/gsAu-6d3pKU
		// pr_PatternChunk[StringBuilder builder, boolean[] uni]:
		//   $builder.append($v.text); <-- exception is thrown here: java.lang.UnsupportedOperationException: interval 85..85 not in token buffer window: 86..341
		// 2. Changed from BufferedTokenStream to CommonTokenStream, otherwise tokens with "-> channel(HIDDEN)" are not filtered out in lexer.
		final CommonTokenStream tokenStream = new CommonTokenStream(lexer);
		return tokenStream;
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
	private boolean directParse(File file, final String fileName, final String code) {
		final Reader reader;
		final CFGListener preparseListener = new CFGListener(fileName);
		boolean config_preproc_error = false;
		if (null != code) {
			// preparsing is not needed
			reader = new StringReader(code);
		} else if (null != file) {
			try {
				final File preparsedFile = new File(file.getParent(), TEMP_CFG_FILENAME);
				final CfgPreProcessor preprocessor = new CfgPreProcessor();
				if ( preprocessor.preparse( file, preparsedFile, preparseListener ) ) {
					// if the cfg file is modified during the preparsing process, file is updated,
					// preparsing modified the cfg file, so use the temp.cfg instead
					file = preparsedFile;
				}

				config_preproc_error = preprocessor.get_error_flag();
				final ConfigCharsetDetector detector = new ConfigCharsetDetector(file);
				final String detectedCharset = detector.detectCharSet();
				if (detectedCharset == null) {
					reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "ISO-8859-1"));
				} else {
					reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), detectedCharset));
				}
				
			} catch (FileNotFoundException e) {
				throw new TtcnError(e);
			} catch (UnsupportedEncodingException e) {
				throw new TtcnError(e);
			}
		} else {
			throw new TtcnError("CfgAnalyzer.directParse(): nothing to parse");
		}

		if ( preparseListener.encounteredError() || config_preproc_error ) {
			return true;
		}

		final CFGListener lexerListener = new CFGListener(fileName);
		final CommonTokenStream tokenStream = createTokenStream(reader, lexerListener);
		final RuntimeCfgParser parser = new RuntimeCfgParser( tokenStream );
		RuntimeCfgParser.reset_configuration_options();
		parser.setActualFile( file );

		// remove ConsoleErrorListener
		parser.removeErrorListeners();
		final CFGListener parserListener = new CFGListener(fileName);
		parser.addErrorListener(parserListener);

		// parse tree is built by default
		parser.setBuildParseTree(false);
		parser.pr_ConfigFile();

		executeSectionHandler = parser.getExecuteSectionHandler();
		mcSectionHandler = parser.getMcSectionHandler();
		IOUtils.closeQuietly(reader);
		final boolean config_process_error = parser.get_error_flag();
		return lexerListener.encounteredError() || parserListener.encounteredError() || config_process_error;
	}

	public MCSectionHandler getMcSectionHandler() {
		return mcSectionHandler;
	}
}
