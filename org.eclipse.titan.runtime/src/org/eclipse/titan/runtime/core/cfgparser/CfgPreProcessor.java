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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.eclipse.titan.runtime.core.TTCN_Logger;
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

	private boolean error_flag = false;

	/**
	 * Pairs of definition name and their value (represented by a token list) collected from the [DEFINE] sections
	 */
	Map<String, List<Token>> definitions = null;
	
	/**
	 * Pairs of definition name and their resolved value.
	 * The definitions are filled by getDefinitionValue() when a new definition value is calculated
	 */
	final Map<String, String> resolvedDefinitions = new LinkedHashMap<String, String>();

	private void config_preproc_error(String error_str, final File actualFile, final Token token) {
		TTCN_Logger.begin_event(TTCN_Logger.Severity.ERROR_UNQUALIFIED);
		TTCN_Logger.log_event("Parse error while pre-processing");
		if ( actualFile != null ) {
			TTCN_Logger.log_event(" configuration file `%s'", actualFile);
		}
		if ( token != null ) {
			TTCN_Logger.log_event(" in line %d", token.getLine() );
		}
		TTCN_Logger.log_event(": ");
		TTCN_Logger.log_event_va_list(error_str);
		TTCN_Logger.end_event();
		error_flag = true;
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
	private void preparseInclude(final File file, final StringBuilder out, AtomicBoolean modified, final CFGListener listener,
										final ChainElement<File> includeChain, final int recursionDepth) {
		if (recursionDepth > RECURSION_LIMIT) {
			// dumb but safe defense against infinite recursion
			config_preproc_error("Maximum include recursion depth reached", file, null);
			return;
		}

		final ChainElement<File> includeChain2 = new ChainElement<File>(includeChain, file);
		final String dir = file.getParent();
		final Reader reader;
		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF8));
		} catch (FileNotFoundException e) {
			config_preproc_error(e.toString(), file, null);
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
		final List<File> includeFiles = new ArrayList<File>();
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
				final File includeFile = new File( dir, includeFilename );
				if ( !includeFiles.contains( includeFile ) ) {
					if ( includeChain2.contains( includeFile ) ) {
						config_preproc_error("Circular import chain detected: " + includeChain2.dump(), file, token);
					} else {
						// include file will be processed when EOF is reached
						includeFiles.add( includeFile );
						modified.set(true);
					}
				}
				break;
			case RuntimeCfgLexer.ORDERED_INCLUDE_FILENAME:
				final String orderedIncludeFilename = tokenText.substring( 1, tokenText.length() - 1 );
				final File orderedIncludeFile = new File(dir, orderedIncludeFilename);
				if ( includeChain2.contains( orderedIncludeFile ) ) {
					config_preproc_error("Circular import chain detected: " + includeChain2.dump(), file, token);
				} else {
					preparseInclude(orderedIncludeFile, out, modified, listener, includeChain2, recursionDepth + 1);
					modified.set(true);
				}
				break;

			default:
				out.append(tokenText);
				break;
			}
		}

		for ( final File includeFile : includeFiles ) {
			preparseInclude(includeFile, out, modified, listener, includeChain2, recursionDepth + 1);
		}

		IOUtils.closeQuietly(reader);
	}

	/**
	 * Preparse the [DEFINE] section and macro references of a CFG file, which means that the defines are collected,
	 * and the macro references are replaced with their values.
	 * After a successful define preparsing we get a CFG file that will not contain any [DEFINE] sections.
	 * Define preparsing is done after include preparsing, so the result will not contain any
	 * [INCLUDE] or [ORDERED_INCLUDE] sections.
	 * @param in cfg file content to preparse
	 * @param modified (in/out) set to true, if the cfg file content is modified during preparsing,
	 *                 otherwise the value is left untouched
	 * @param listener listener for ANTLR lexer/parser errors
	 * @return output string buffer, where the resolved content is written
	 */
	private StringBuilder preparseDefine(final StringBuilder in, final AtomicBoolean modified, final CFGListener listener) {
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
		definitions = defineSectionHandler.getDefinitions();
		parser = null;

		checkCircularReferences();
		if ( error_flag ) {
			return null;
		}

		// we can use the lexer which was created for the parser
		StringBuilder out = resolveMacros(tokenStream, modified);
		reader.close();
		return out;
	}

	private void checkCircularReferences() {
		for (final Map.Entry<String, List<Token>> entry : definitions.entrySet()) {
			final String defName = entry.getKey();
			checkCircularReferences(defName, defName, definitions);
		}
	}

	private void checkCircularReferences(final String first, final String defName, final Map<String, List<Token>> defs) {
		final List<Token> defValue = defs.get(defName);
		if (defValue == null) {
			config_preproc_error("Unknown define "+defName, null, null);
			return;
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
					config_preproc_error("Invalid macro name: "+tokenText, null, token);
				}
				if (first.equals(macroName)) {
					config_preproc_error("Circular reference in define "+first, null, token);
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
	 * @return output string buffer, where the resolved content is written
	 */
	private StringBuilder resolveMacros(final CommonTokenStream tokenStream, final AtomicBoolean modified) {
		final List<Token> tokens = tokenStream.getTokens();
		final ListIterator<Token> iter = tokens.listIterator();
		boolean defineSection = false;
		final StringBuilder out = new StringBuilder();
		while (iter.hasNext()) {
			final Token token = iter.next();
			final CommonToken commonToken = (CommonToken)token;
			final int tokenType = token.getType();
			final String tokenText = token.getText();
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
				out.append(tokenText);
				break;
			case RuntimeCfgLexer.INCLUDE_SECTION:
			case RuntimeCfgLexer.ORDERED_INCLUDE_SECTION:
				//should not happen in this stage of preparsing
				//TODO: error
				defineSection = false;
				out.append(tokenText);
				break;
			default:
				if (defineSection) {
					iter.remove();
				} else {
					if ( resolveToken( out, commonToken ) ) {
						modified.set(true);
					}
				}
			}
		}

		return out;
	}

	/**
	 * Resolves a token, which means, that in case of macro it's changed to its value, otherwise it's left untouched.
	 * @param out output string buffer, where the resolved content is written
	 * @param token lexer token object
	 * @return true, if the cfg file content is modified during preparsing,
	 *         false otherwise
	 */
	private boolean resolveToken(final StringBuilder out, final Token token) {
		boolean modified = false;
		final String tokenText = token.getText();
		final int tokenType = token.getType();
		switch (tokenType) {
		case RuntimeCfgLexer.MACRO:
			final String macroName = DefineSectionHandler.getMacroName(tokenText);
			final String macroValue = getDefinitionValue( macroName );
			out.append(macroValue);
			modified = true;
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
			final String typedMacroName = DefineSectionHandler.getTypedMacroName(tokenText);
			final String typedMacroValue = getDefinitionValue( typedMacroName );
			out.append(typedMacroValue);
			modified = true;
			break;
		default:
			out.append(tokenText);
			break;
		}
		return modified;
	}

	/**
	 * Gets the value of a macro or an environment variable
	 * @param definition macro or environment variable
	 * @return macro or environment variable value, or null if there is no such definition
	 */
	public String getDefinitionValue(final String definition) {
		if ( resolvedDefinitions.containsKey(definition) ) {
			// definition value is already calculated
			return resolvedDefinitions.get(definition);
		}
		if ( definitions == null || !definitions.containsKey( definition ) ) {
			config_preproc_error( "Macro definition not found: " + definition, null, null );
			return null;
		}
		final List<Token> tokenList = definitions.get( definition );
		// true if macro is structured (starts with "{"). In this case the STRING keeps its beginning and ending quotes,
		// in simple case beginning and ending quotes are removed
		final boolean structured = tokenList.size() > 0 && tokenList.get(0).getType() == RuntimeCfgLexer.BEGINCHAR;
		final StringBuilder out = new StringBuilder();
		for (final Token token : tokenList) {
			final int tokenType = token.getType();
			switch (tokenType) {
			case RuntimeCfgLexer.STRING: {
				final CharstringExtractor cse = new CharstringExtractor( token.getText(), !structured );
				final String text = cse.getExtractedString();
				if ( cse.isErroneous() ) {
					config_preproc_error( cse.getErrorMessage(), null, token );
				}
				out.append(text);
				break;
			}
			case RuntimeCfgLexer.FSTRING: {
				final CharstringExtractor cse = new CharstringExtractor( token.getText(), false );
				final String text = cse.getExtractedString();
				if ( cse.isErroneous() ) {
					config_preproc_error( cse.getErrorMessage(), null, token );
				}
				out.append(text);
				break;
			}
			default:
				resolveToken( out, token );
				break;
			}
		}
		final String definitionValue = out.toString();
		resolvedDefinitions.put( definition, definitionValue );
		return definitionValue;
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
	public boolean preparse(final File file, final File resultFile, final CFGListener listener) {
		final StringBuilder outInclude = new StringBuilder();
		final AtomicBoolean modified = new AtomicBoolean(false);
		preparseInclude(file, outInclude, modified, listener, null, 0);

		if ( listener != null ) {
			listener.setFilename(null);
		}
		final StringBuilder outDefine = preparseDefine(outInclude, modified, listener);
		if ( outDefine != null ) {
			writeToFile(resultFile, outDefine);
		}
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

	public boolean get_error_flag() {
		return error_flag;
	}
}
