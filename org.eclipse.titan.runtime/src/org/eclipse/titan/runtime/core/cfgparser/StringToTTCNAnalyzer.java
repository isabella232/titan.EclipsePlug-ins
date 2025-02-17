/******************************************************************************
 * Copyright (c) 2000-2021 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core.cfgparser;

import java.io.Reader;
import java.io.StringReader;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenFactory;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.UnbufferedCharStream;
import org.eclipse.titan.runtime.core.TtcnError;
import org.eclipse.titan.runtime.core.Param_Types.Module_Parameter;

/**
 * Analyzer for string2ttcn statement
 *
 * @author Gergo Ujhelyi
 */
public class StringToTTCNAnalyzer {

	// need a generated RuntimeCFGLexer
	public static final int LEXER_MODE = RuntimeCfgLexer.MODULE_PARAMETERS_SECTION_MODE;
	public static final String UNKNOWN_FILE = "<unknown file>";

	private Module_Parameter parsed_module_param;

	public Module_Parameter getParsedModuleParam() {
		return parsed_module_param;
	}

	/**
	 * Convert a string to a TTCN-3 module parameter structure.
	 *
	 * @param mp_str
	 *                the string to convert.
	 * @param is_component
	 *                {@code true} if the value is to be assigned to a
	 *                component variable or template.
	 * @return the parsed module parameter structure or {@code null} in case
	 *         of problems
	 * */
	public static Module_Parameter process_config_string2ttcn(final String mp_str, final boolean is_component) {
		// TODO check processing of is_component.
		final StringToTTCNAnalyzer analyzer = new StringToTTCNAnalyzer();
		analyzer.parse(mp_str);
		if (analyzer.getParsedModuleParam() != null) {
			return analyzer.getParsedModuleParam();
		} else {
			throw new TtcnError("Internal error: could not parse TTCN string.");
		}
	}

	/**
	 * Parses a string.
	 *
	 * @param code
	 *                the value in the string2ttcn statement
	 *
	 * @return {@code true} if there were errors in the string,
	 *         {@code false} otherwise
	 */
	public boolean parse(final String code) {
		if (code == null || code.isEmpty()) {
			throw new TtcnError("StringToTTCNAnalyzer.parse(): nothing to parse");
		}
		final Reader reader = new StringReader(code);
		final CFGListener lexerListener = new CFGListener(UNKNOWN_FILE);
		final CommonTokenStream tokenStream = createTokeStream(reader, lexerListener);
		final RuntimeCfgParser parser = new RuntimeCfgParser(tokenStream);

		parser.setBuildParseTree(true);
		try {
			parsed_module_param = parser.pr_ParameterValue().moduleparameter;
		} catch (Exception e) {
			throw new TtcnError(e.getMessage());
		}
		IOUtils.closeQuietly(reader);
		return lexerListener.encounteredError();
	}

	/**
	 * Create and initialize a new CFG Lexer object
	 *
	 * @param reader
	 *                file reader
	 * @param lexerListener
	 *                listener for ANTLR lexer/parser errors, it can be null
	 * @return the created lexer object
	 */
	private RuntimeCfgLexer createLexer(final Reader reader, final CFGListener lexerListener) {
		final CharStream charStream = new UnbufferedCharStream(reader);
		final RuntimeCfgLexer lexer = new RuntimeCfgLexer(charStream);
		lexer.setTokenFactory(new CommonTokenFactory(true));
		// set module parameter mode
		lexer.mode(LEXER_MODE);
		if (lexerListener != null) {
			lexer.removeErrorListeners();
			lexer.addErrorListener(lexerListener);
		}
		return lexer;
	}

	private CommonTokenStream createTokeStream(final Reader reader, final CFGListener lexerListener) {
		final RuntimeCfgLexer lexer = createLexer(reader, lexerListener);
		return new CommonTokenStream(lexer);
	}
}
