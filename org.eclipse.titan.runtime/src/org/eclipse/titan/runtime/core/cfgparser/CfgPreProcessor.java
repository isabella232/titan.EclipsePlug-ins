/******************************************************************************
 * Copyright (c) 2000-2019 Ericsson Telecom AB
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
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.eclipse.titan.runtime.core.TtcnError;

/**
 * Runtime CFG preparsing.
 * It effects the [INCLUDE], [ORDERED_INCLUDE] and [DEFINE] sections.
 * After a successful preparsing we get one CFG file that will not contain
 * any [INCLUDE], [ORDERED_INCLUDE] or [DEFINE] sections,
 * where the content of the include files are copied into the place of the include file names,
 * macro references are resolved as they are defined in the [DEFINE] section.
 * @author Arpad Lovassy
 */
public class CfgPreProcessor {

	private static final int RECURSION_LIMIT = 100;

	private CfgPreProcessor() {
		// Hide constructor
	}

	private static void config_preproc_error(String errorMsg) {
		//TODO: implement
		System.err.println(errorMsg);
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
	 * @param includeChain chained list element of the previous file that included this one
	 *                     to keep track the included files to avoid infinite recursion,
	 *                     null in case of the root element
	 * @param recursionDepth counter of the recursion depth
	 */
	private static void preparseInclude(final File file, final StringBuilder out, AtomicBoolean modified, final CFGListener listener,
										final ChainElement<File> includeChain, final int recursionDepth) {
		if (recursionDepth > RECURSION_LIMIT) {
			// dumb but safe defense against infinite recursion, default value from gcc
			config_preproc_error("Maximum include recursion depth reached in file: " + file.getName());
			return;
		}

		if ( includeChain != null && includeChain.contains(file) ) {
			config_preproc_error("Circular import chain detected: " + includeChain.dump());
			return;
		}

		final ChainElement<File> includeChain2 = new ChainElement<File>(includeChain, file);
		final String dir = file.getParent();
		final Reader reader;
		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF8));
		} catch (FileNotFoundException e) {
			config_preproc_error(e.toString());
			return;
		}
		if ( listener != null ) {
			listener.setFilename(file.getName());
		}
		final CommonTokenStream tokenStream = CfgAnalyzer.createTokenStream(reader, listener);
		tokenStream.fill();
		final List<Token> tokens = tokenStream.getTokens();
		final ListIterator<Token> iter = tokens.listIterator();
		// file names collected from [INCLUDE] sections
		final List<String> includeFilenames = new ArrayList<String>();
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
				final String includeFilename = tokenText.substring( 1, tokenText.length() - 1 );
				if ( !includeFilenames.contains( includeFilename ) ) {
					// include file will be processed when EOF is reached
					includeFilenames.add(includeFilename);
					modified.set(true);
				}
				break;
			case RuntimeCfgLexer.ORDERED_INCLUDE_FILENAME:
				final String orderedIncludeFilename = tokenText.substring( 1, tokenText.length() - 1 );
				final File orderedIncludeFile = new File(dir, orderedIncludeFilename);
				preparseInclude(orderedIncludeFile, out, modified, listener, includeChain2, recursionDepth + 1);
				modified.set(true);
				break;

			default:
				out.append(tokenText);
				break;
			}
		}

		for ( final String includeFilename : includeFilenames ) {
			final File includeFile = new File(dir, includeFilename);
			preparseInclude(includeFile, out, modified, listener, includeChain2, recursionDepth + 1);
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
	private static StringBuilder preparseDefine(final StringBuilder in, final AtomicBoolean modified, final CFGListener listener) {
		// collect defines
		StringReader reader = new StringReader( in.toString() );
		CommonTokenStream tokenStream = CfgAnalyzer.createTokenStream(reader, listener);
		RuntimeCfgPreParser parser = new RuntimeCfgPreParser( tokenStream );

		if ( listener != null ) {
			// remove ConsoleErrorListener
			parser.removeErrorListeners();
			parser.addErrorListener( listener );
		}

		// parse tree is built by default
		parser.setBuildParseTree(false);
		parser.pr_ConfigFile();
		final DefineSectionHandler defineSectionHandler = parser.getDefineSectionHandler();
		final Map<String, List<Token>> defs = defineSectionHandler.getDefinitions();
		parser = null;

		checkCircularReferences(defs);

		// modified during macro resolving
		AtomicBoolean modifiedMacro = new AtomicBoolean(false);
		// in the 1st round we can use the lexer which was created for the parser
		StringBuilder out = resolveMacros(tokenStream, modifiedMacro, defineSectionHandler);
		reader.close();
		while ( modifiedMacro.get() ) {
			modified.set(true);
			reader = new StringReader( out.toString() );
			tokenStream = CfgAnalyzer.createTokenStream(reader, listener);
			tokenStream.fill();
			modifiedMacro = new AtomicBoolean(false);
			out = resolveMacros(tokenStream, modifiedMacro, defineSectionHandler);
			reader.close();
		}
		return out;
	}

	private static void checkCircularReferences(final Map<String, List<Token>> defs) {
		for (final Map.Entry<String, List<Token>> entry : defs.entrySet()) {
			final String defName = entry.getKey();
			checkCircularReferences(defName, defName, defs);
		}
	}

	private static void checkCircularReferences(final String first, final String defName, final Map<String, List<Token>> defs) {
		final List<Token> defValue = defs.get(defName);
		if (defValue == null) {
			throw new TtcnError("Unknown define "+defName);
		}
		for ( final Token token : defValue ) {
			final int tokenType = token.getType();
			switch (tokenType) {
			case RuntimeCfgLexer.MACRO:
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
				final String tokenText = token.getText();
				// get macro name: ${MACRO_1_0} -> MACRO_1_0
				String macroName = DefineSectionHandler.getMacroName(tokenText);
				if (macroName == null) {
					macroName = DefineSectionHandler.getTypedMacroName(tokenText);
				}
				if (macroName == null) {
					throw new TtcnError("Invalid macro name: "+tokenText);
				}
				if (first.equals(macroName)) {
					throw new TtcnError("Circular reference in define "+first);
				}
				checkCircularReferences(first, macroName, defs);
				break;
			default:
				break;
			}
		}
	}

	/**
	 * Macro references are replaced with their values
	 * @param tokenStream input tokens
	 * @param modified (in/out) set to true, if the cfg file content is modified during preparsing,
	 *                 otherwise the value is left untouched
	 * @param defineSectionHandler define handler for getting collection of definition name value pairs
	 * @return output string buffer, where the resolved content is written
	 */
	private static StringBuilder resolveMacros(final CommonTokenStream tokenStream, final AtomicBoolean modified, final DefineSectionHandler defineSectionHandler) {
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
				if (defineSection) {
					iter.remove();
				} else {
					final String macroValue = defineSectionHandler.getMacroValue(token);
					resolveMacro(commonToken, macroValue, iter);
				}
				modified.set(true);
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
				if (defineSection) {
					iter.remove();
				} else {
					final String typedMacroValue = defineSectionHandler.getTypedMacroValue(token);
					resolveMacro(commonToken, typedMacroValue, iter);
				}
				modified.set(true);
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
	private static void resolveMacro(CommonToken commonToken, String macroValue, ListIterator<Token> iter) {
		final String stringWithoutQuotes = DefineSectionHandler.removeQuotes(macroValue);
		if ( stringWithoutQuotes == null ) {
			// not a string, we don't need to handle it as a special case
			commonToken.setText(macroValue);
			return;
		}

		// macro reference in quotes, remove the quotes
		final String macroWithoutQuotes = DefineSectionHandler.removeMacroQuotes(macroValue);
		if ( macroWithoutQuotes != null ) {
			commonToken.setText(macroWithoutQuotes);
			return;
		}

		String prevText = "";
		if ( iter.hasPrevious() ) {
			Token prevToken = iter.previous();
			if ( iter.hasPrevious() ) {
				prevToken = iter.previous();
				final String prevWithoutQuotes = DefineSectionHandler.removeQuotes(prevToken.getText());
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
			final String nextWithoutQuotes = DefineSectionHandler.removeQuotes(nextToken.getText());
			if ( nextToken.getType() == RuntimeCfgLexer.STRING && nextWithoutQuotes != null) {
				nextText = nextWithoutQuotes;
				iter.remove();
			}
			// go back to the macro token
			iter.previous();
		}

		commonToken.setType(RuntimeCfgLexer.STRING);

		final String newValue = "\"" + prevText + stringWithoutQuotes + nextText + "\"";
		final String macroWithoutQuotes2 = DefineSectionHandler.removeMacroQuotes(newValue);
		if ( macroWithoutQuotes2 != null ) {
			// The result is a macro reference in quotes, remove the quotes
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
	 * @param resultFile result file
	 * @param listener listener for ANTLR lexer/parser errors
	 * @return <code>true</code>, if CFG file was changed during preparsing,
	 *     <br><code>false</code> otherwise, so when the CFG file did not contain
	 *         any [INCLUDE], [ORDERED_INCLUDE] or [DEFINE] sections
	 */
	static boolean preparse(final File file, final File resultFile, final CFGListener listener) {
		final StringBuilder outInclude = new StringBuilder();
		final AtomicBoolean modified = new AtomicBoolean(false);
		preparseInclude(file, outInclude, modified, listener, null, 0);

		if ( listener != null ) {
			listener.setFilename(null);
		}
		final StringBuilder outDefine = preparseDefine(outInclude, modified, listener);
		writeToFile(resultFile, outDefine);
		return modified.get();
	}

	/**
	 * Write the content of a string buffer to a file.
	 * This is used for writing the resolved CFG file after preparsing.
	 * @param resultFile result file
	 * @param sb string buffer to write
	 */
	static void writeToFile( final File resultFile, final StringBuilder sb ) {
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(resultFile);
			pw.append(sb);
		} catch (FileNotFoundException e) {
			throw new TtcnError(e);
		} finally {
			if (pw != null) {
				pw.close();
			}
		}
	}
}
