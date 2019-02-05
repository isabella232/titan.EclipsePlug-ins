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
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.atomic.AtomicBoolean;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.CommonTokenFactory;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.UnbufferedCharStream;
import org.eclipse.titan.runtime.core.TtcnError;

/**
 * Syntactic analyzer for CFG files
 * @author Arpad Lovassy
 */
public final class CfgAnalyzer {

	private static final int RECURSION_LIMIT = 20;
	private static final String TEMP_CFG_FILENAME = "temp.cfg";

	private ExecuteSectionHandler executeSectionHandler = null;
	private IncludeSectionHandler orderedIncludeSectionHandler = new IncludeSectionHandler();
	private DefineSectionHandler defineSectionHandler = new DefineSectionHandler();

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
	 * RECURSIVE
	 * Preparse the [INCLUDE] and [ORDERED_INCLUDE] sections of a CFG file, which means that the include file name is replaced
	 * by the content of the include file recursively.
	 * After a successful include preparsing we get one CFG file that will not contain any [INCLUDE] or [ORDERED_INCLUDE] sections.
	 * @param file actual file to preparse
	 * @param out output string buffer, where the resolved content is written
	 * @param modified (out) true, if CFG file was changed during preparsing,
	 *     <br>false otherwise, so when the CFG file did not contain any [INCLUDE] or [ORDERED_INCLUDE] sections
	 * @param listener listener for ANTLR lexer/parser errors
	 * @param recursionDepth counter of the recursion depth
	 */
	private void preparseInclude(final File file, final StringBuilder out, AtomicBoolean modified, final CFGListener listener, final int recursionDepth) {
		if (recursionDepth > RECURSION_LIMIT) {
			// dumb but safe defense against infinite recursion, default value from gcc
			throw new TtcnError("Maximum include recursion depth reached in file: " + file.getName());
		}
		final String dir = file.getParent();
		final Reader reader;
		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF8));
		} catch (FileNotFoundException e) {
			throw new TtcnError(e);
		}
		if ( listener != null ) {
			listener.setFilename(file.getName());
		}
		final RuntimeCfgLexer lexer = createLexer(reader, listener);
		final CommonTokenStream tokenStream = new CommonTokenStream( lexer );
		tokenStream.fill();
		final List<Token> tokens = tokenStream.getTokens();
		final ListIterator<Token> iter = tokens.listIterator();
		while (iter.hasNext()) {
			final Token token = iter.next();
			final int tokenType = token.getType();
			final String tokenText = token.getText();
			switch (tokenType) {
			case RuntimeCfgLexer.INCLUDE_SECTION:
			case RuntimeCfgLexer.ORDERED_INCLUDE_SECTION:
				modified.set(true);
				break;
			case RuntimeCfgLexer.INCLUDE_FILENAME:
			case RuntimeCfgLexer.ORDERED_INCLUDE_FILENAME:
				final String orderedIncludeFilename = tokenText.substring( 1, tokenText.length() - 1 );
				if ( !orderedIncludeSectionHandler.isFileAdded( orderedIncludeFilename ) ) {
					orderedIncludeSectionHandler.addFile( orderedIncludeFilename );
					final File orderedIncludeFile = new File(dir, orderedIncludeFilename);
					preparseInclude(orderedIncludeFile, out, modified, listener, recursionDepth + 1);
					modified.set(true);
				}
				break;

			default:
				out.append(tokenText);
				break;
			}
		}

		IOUtils.closeQuietly(reader);
	}

	/**
	 * Preparse the [DEFINE] section and macro references of a CFG file, which means that the defines are collected,
	 * and the macro references are replaced with their values.
	 * After a successful define preparsing we get a CFG file that will not contain any [DEFINE] sections.
	 * Define preparsing is done after include preparsing, so the result will not contain any
	 * [INCLUDE] or [ORDERED_INCLUDE] sections as well.
	 * @param in cfg file content to preparse
	 * @param modified (in/out) set to true, if the cfg file content is modified during preparsing,
	 *                 otherwise the value is left untouched
	 * @param listener listener for ANTLR lexer/parser errors
	 * @return output string buffer, where the resolved content is written
	 */
	private StringBuilder preparseDefine(final StringBuilder in, final AtomicBoolean modified, final CFGListener listener) {
		// collect defines
		StringReader reader = new StringReader( in.toString() );
		RuntimeCfgLexer lexer = createLexer( reader, listener );
		CommonTokenStream tokenStream = new CommonTokenStream( lexer );
		RuntimeCfgPreParser parser = new RuntimeCfgPreParser( tokenStream );

		// remove ConsoleErrorListener
		parser.removeErrorListeners();
		parser.addErrorListener( listener );

		// parse tree is built by default
		parser.setBuildParseTree(false);
		parser.pr_ConfigFile();
		defineSectionHandler = parser.getDefineSectionHandler();
		parser = null;

		// modified during macro resolving
		AtomicBoolean modifiedMacro = new AtomicBoolean(false);
		// in the 1st round we can use the lexer which was created for the parser
		StringBuilder out = resolveMacros(tokenStream, modifiedMacro);
		reader.close();
		while ( modifiedMacro.get() ) {
			modified.set(true);
			reader = new StringReader( out.toString() );
			lexer = createLexer( reader, listener );
			tokenStream = new CommonTokenStream( lexer );
			tokenStream.fill();
			modifiedMacro = new AtomicBoolean(false);
			out = resolveMacros( tokenStream, modifiedMacro );
			reader.close();
		}
		return out;
	}

	/**
	 * Macro references are replaced with their values
	 * @param tokenStream input tokens
	 * @param modified (in/out) set to true, if the cfg file content is modified during preparsing,
	 *                 otherwise the value is left untouched
	 * @return output string buffer, where the resolved content is written
	 */
	private StringBuilder resolveMacros(final CommonTokenStream tokenStream, final AtomicBoolean modified) {
		final List<Token> tokens = tokenStream.getTokens();
		final ListIterator<Token> iter = tokens.listIterator();
		boolean defineSection = false;
		while (iter.hasNext()) {
			final Token token = iter.next();
			final CommonToken commonToken = (CommonToken)token;
			final int tokenType = token.getType();
			switch (tokenType) {
			case RuntimeCfgLexer.DEFINE_SECTION:
				iter.remove();
				// it is enough to set modified here, because if there is no [DEFINE] section,
				// then macros cannot be resolved, so content is not changed
				modified.set(true);
				defineSection = true;
				break;
			case RuntimeCfgLexer.MAIN_CONTROLLER_SECTION:
			case RuntimeCfgLexer.EXECUTE_SECTION:
			case RuntimeCfgLexer.EXTERNAL_COMMANDS_SECTION:
			case RuntimeCfgLexer.TESTPORT_PARAMETERS_SECTION:
			case RuntimeCfgLexer.GROUPS_SECTION:
			case RuntimeCfgLexer.MODULE_PARAMETERS_SECTION:
			case RuntimeCfgLexer.COMPONENTS_SECTION:
			case RuntimeCfgLexer.LOGGING_SECTION:
			case RuntimeCfgLexer.PROFILER_SECTION:
				defineSection = false;
				break;
			case RuntimeCfgLexer.INCLUDE_SECTION:
			case RuntimeCfgLexer.ORDERED_INCLUDE_SECTION:
				//should not happen in this stage of preparsing
				//TODO: error
				defineSection = false;
				break;
			case RuntimeCfgLexer.MACRO:
				final String macroValue = defineSectionHandler.getMacroValue(token);
				resolveMacro(commonToken, macroValue, iter);
				break;
			case RuntimeCfgLexer.MACRO_BINARY:
			case RuntimeCfgLexer.MACRO_BOOL:
			case RuntimeCfgLexer.MACRO_BSTR:
			case RuntimeCfgLexer.MACRO_EXP_CSTR:
			case RuntimeCfgLexer.MACRO_FLOAT:
			case RuntimeCfgLexer.MACRO_HOSTNAME:
			case RuntimeCfgLexer.MACRO_HSTR:
			case RuntimeCfgLexer.MACRO_ID:
			case RuntimeCfgLexer.MACRO_INT:
			case RuntimeCfgLexer.MACRO_OSTR:
				final String typedMacroValue = defineSectionHandler.getTypedMacroValue(token);
				resolveMacro(commonToken, typedMacroValue, iter);
				break;
			default:
				if (defineSection) {
					iter.remove();
				}
				break;
			}
		}

		final StringBuilder out = new StringBuilder();
		for ( final Token token : tokens ) {
			out.append(token.getText());
		}

		return out;
	}

	/**
	 * Change a macro to its value.
	 * Also handle string concatenation with surrounding STRING tokens if needed.
	 * @param commonToken modifiable lexer token object
	 * @param macroValue new value
	 * @param iter iterator for getting previous and next token
	 */
	private void resolveMacro(CommonToken commonToken, String macroValue, ListIterator<Token> iter) {
		final String stringWithoutQuotes = defineSectionHandler.removeQuotes(macroValue);
		if ( stringWithoutQuotes == null ) {
			// not a string, we don't need to handle it as a special case 
			commonToken.setText(macroValue);
			return;
		}

		final String macroWithoutQuotes = defineSectionHandler.removeMacroQuotes(macroValue);
		if ( macroWithoutQuotes != null ) {
			commonToken.setText(macroWithoutQuotes);
			return;
		}

		String prevText = "";
		if ( iter.hasPrevious() ) {
			Token prevToken = iter.previous();
			if ( iter.hasPrevious() ) {
				prevToken = iter.previous();
				final String prevWithoutQuotes = defineSectionHandler.removeQuotes(prevToken.getText());
				if ( prevToken.getType() == RuntimeCfgLexer.STRING && prevWithoutQuotes != null) {
					prevText = prevWithoutQuotes;
					iter.remove();
				}
				iter.next();
			}
			// go back to the macro token
			iter.next();
		}

		String nextText = "";
		if ( iter.hasNext() ) {
			final Token nextToken = iter.next();
			final String nextWithoutQuotes = defineSectionHandler.removeQuotes(nextToken.getText());
			if ( nextToken.getType() == RuntimeCfgLexer.STRING && nextWithoutQuotes != null) {
				nextText = nextWithoutQuotes;
				iter.remove();
			}
			// go back to the macro token
			iter.previous();
		}

		commonToken.setType(RuntimeCfgLexer.STRING);

		final String newValue = "\"" + prevText + stringWithoutQuotes + nextText + "\"";
		final String macroWithoutQuotes2 = defineSectionHandler.removeMacroQuotes(newValue);
		if ( macroWithoutQuotes2 != null ) {
			commonToken.setText(macroWithoutQuotes2);
			return;
		}

		commonToken.setText(newValue);
	}

	/**
	 * Preparse a CFG file.
	 * It effects the [INCLUDE], [ORDERED_INCLUDE] and [DEFINE] sections.
	 * After a successful preparsing we get one CFG file that will not contain
	 * any [INCLUDE], [ORDERED_INCLUDE] or [DEFINE] sections,
	 * where the content of the include files are copied into the place of the include file names,
	 * macro references are resolved as they are defined in the [DEFINE] section.
	 * @param file actual file to preparse
	 * @param listener listener for ANTLR lexer/parser errors
	 * @return true, if CFG file was changed during preparsing,
	 *     <br>false otherwise, so when the CFG file did not contain any [INCLUDE] or [ORDERED_INCLUDE] sections
	 */
	private boolean preparse(final File file, final CFGListener listener) {
		final StringBuilder outInclude = new StringBuilder();
		final AtomicBoolean modified = new AtomicBoolean(false);
		preparseInclude(file, outInclude, modified, listener, 0);

		if ( listener != null ) {
			listener.setFilename(null);
		}
		final StringBuilder outDefine = preparseDefine(outInclude, modified, listener);
		writeTempCfg(file.getParent(), outDefine);
		return modified.get();
	}

	/**
	 * Create and initialize a new CFG Lexer object
	 * @param reader file reader
	 * @param lexerListener listener for ANTLR lexer/parser errors, it can be null
	 * @return the created lexer object
	 */
	private RuntimeCfgLexer createLexer(final Reader reader, final CFGListener lexerListener) {
		final CharStream charStream = new UnbufferedCharStream(reader);
		final RuntimeCfgLexer lexer = new RuntimeCfgLexer(charStream);
		lexer.setTokenFactory(new CommonTokenFactory(true));
		lexer.removeErrorListeners(); // remove ConsoleErrorListener
		if ( lexerListener != null ) {
			lexer.addErrorListener(lexerListener);
		}
		return lexer;
	}

	/**
	 * Write the content of a string buffer to a file.
	 * This is used for writing the resolved CFG file after preparsing.
	 * @param dir output file directory
	 * @param sb string buffer to write
	 */
	private void writeTempCfg( final String dir, final StringBuilder sb ) {
		final File out = new File(dir, TEMP_CFG_FILENAME);
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(out);
			pw.append(sb);
		} catch (FileNotFoundException e) {
			throw new TtcnError(e);
		} finally {
			if (pw != null) {
				pw.close();
			}
		}
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
		if (null != code) {
			// preparsing is not needed
			reader = new StringReader(code);
		} else if (null != file) {
			try {
				// if the cfg file is modified during the preparsing process, file is updated
				if ( preparse( file, preparseListener ) ) {
					// preparsing modified the cfg file, so use the temp.cfg instead
					file = new File(file.getParent(), TEMP_CFG_FILENAME);
				}
				reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF8));
			} catch (FileNotFoundException e) {
				throw new TtcnError(e);
			}
		} else {
			throw new TtcnError("CfgAnalyzer.directParse(): nothing to parse");
		}

		final CFGListener lexerListener = new CFGListener(fileName);
		final RuntimeCfgLexer lexer = createLexer(reader, lexerListener);

		// 1. Previously it was UnbufferedTokenStream(lexer), but it was changed to BufferedTokenStream, because UnbufferedTokenStream seems to be unusable. It is an ANTLR 4 bug.
		// Read this: https://groups.google.com/forum/#!topic/antlr-discussion/gsAu-6d3pKU
		// pr_PatternChunk[StringBuilder builder, boolean[] uni]:
		//   $builder.append($v.text); <-- exception is thrown here: java.lang.UnsupportedOperationException: interval 85..85 not in token buffer window: 86..341
		// 2. Changed from BufferedTokenStream to CommonTokenStream, otherwise tokens with "-> channel(HIDDEN)" are not filtered out in lexer.
		final CommonTokenStream tokenStream = new CommonTokenStream( lexer );
		final RuntimeCfgParser parser = new RuntimeCfgParser( tokenStream );
		parser.setActualFile( file );

		// remove ConsoleErrorListener
		parser.removeErrorListeners();
		final CFGListener parserListener = new CFGListener(fileName);
		parser.addErrorListener(parserListener);

		// parse tree is built by default
		parser.setBuildParseTree(false);
		parser.pr_ConfigFile();

		executeSectionHandler = parser.getExecuteSectionHandler();
		IOUtils.closeQuietly(reader);

		return preparseListener.encounteredError() || lexerListener.encounteredError() || parserListener.encounteredError();
	}
}
