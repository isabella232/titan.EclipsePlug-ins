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

	private static final int RECURSION_LIMIT = 20;

	private IncludeSectionHandler orderedIncludeSectionHandler = new IncludeSectionHandler();
	private DefineSectionHandler defineSectionHandler = new DefineSectionHandler();

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
		final CommonTokenStream tokenStream = CfgAnalyzer.createTokenStream(reader, listener);
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
			tokenStream = CfgAnalyzer.createTokenStream(reader, listener);
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
	private void resolveMacro(CommonToken commonToken, String macroValue, ListIterator<Token> iter) {
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
	 * @param resultFile result file name
	 * @param listener listener for ANTLR lexer/parser errors
	 * @return <code>true</code>, if CFG file was changed during preparsing,
	 *     <br><code>false</code> otherwise, so when the CFG file did not contain any [INCLUDE] or [ORDERED_INCLUDE] sections
	 */
	boolean preparse(final File file, final File resultFile, final CFGListener listener) {
		final StringBuilder outInclude = new StringBuilder();
		final AtomicBoolean modified = new AtomicBoolean(false);
		preparseInclude(file, outInclude, modified, listener, 0);

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
	private static void writeToFile( final File resultFile, final StringBuilder sb ) {
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
