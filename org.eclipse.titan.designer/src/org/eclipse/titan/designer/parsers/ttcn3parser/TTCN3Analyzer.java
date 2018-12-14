/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.parsers.ttcn3parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenFactory;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.UnbufferedCharStream;
import org.antlr.v4.runtime.atn.ParserATNSimulator;
import org.antlr.v4.runtime.atn.PredictionContextCache;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.tree.ParseTree;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.common.parsers.Interval;
import org.eclipse.titan.common.parsers.SyntacticErrorStorage;
import org.eclipse.titan.common.parsers.TITANMarker;
import org.eclipse.titan.common.parsers.TitanListener;
import org.eclipse.titan.designer.AST.TTCN3.definitions.TTCN3Module;
import org.eclipse.titan.designer.parsers.GlobalParser;
import org.eclipse.titan.designer.parsers.ISourceAnalyzer;
import org.eclipse.titan.designer.parsers.ParserUtilities;
import org.eclipse.titan.designer.preferences.PreferenceConstants;
import org.eclipse.titan.designer.productUtilities.ProductConstants;
import org.eclipse.titan.designer.properties.data.PreprocessorSymbolsOptionsData;

/**
 * TTCN3Analyzer
 * @author Arpad Lovassy
 */
public class TTCN3Analyzer implements ISourceAnalyzer {

	private List<TITANMarker> warningsAndErrors;
	private List<TITANMarker> unsupportedConstructs;
	private Interval rootInterval;
	private TTCN3Module actualTtc3Module;
	private byte[] digest = null;

	/**
	 * The list of markers (ERROR and WARNING) created during parsing
	 * NOTE: used from ANTLR v4
	 */
	private List<SyntacticErrorStorage> mErrorsStored = null;

	@Override
	public List<SyntacticErrorStorage> getErrorStorage() {
		return mErrorsStored;
	}

	@Override
	public List<TITANMarker> getWarnings() {
		return warningsAndErrors;
	}

	@Override
	public List<TITANMarker> getUnsupportedConstructs() {
		return unsupportedConstructs;
	}

	@Override
	public TTCN3Module getModule() {
		return actualTtc3Module;
	}

	@Override
	public Interval getRootInterval() {
		return rootInterval;
	}

	/**
	 * Parse TTCN-3 file using ANTLR v4
	 * @param aFile TTCN-3 file to parse, It cannot be null
	 * @param aCode TTCN-3 code to parse in string format
	 *              It can be null, in this case code is read from file
	 */
	public void parse( final IFile aFile, final String aCode ) {
		Reader reader;
		Reader reader2;
		int rootInt;
		if ( aCode != null ) {
			reader = new StringReader( aCode );
			reader2 = new StringReader( aCode );
			rootInt = aCode.length();
		} else if (aFile != null) {
			try {
				InputStreamReader temp = new InputStreamReader(aFile.getContents());
				if (!aFile.getCharset().equals(temp.getEncoding())) {
					try {
						temp.close();
					} catch (IOException e) {
						ErrorReporter.logWarningExceptionStackTrace(e);
					}
					temp = new InputStreamReader(aFile.getContents(), aFile.getCharset());
				}

				reader = new BufferedReader(temp);
				InputStreamReader temp2 = new InputStreamReader(aFile.getContents());
				if (!aFile.getCharset().equals(temp.getEncoding())) {
					try {
						temp2.close();
					} catch (IOException e) {
						ErrorReporter.logWarningExceptionStackTrace(e);
					}
					temp2 = new InputStreamReader(aFile.getContents(), aFile.getCharset());
				}

				reader2 = new BufferedReader(temp2);
			} catch (CoreException e) {
				ErrorReporter.logExceptionStackTrace(e);
				return;
			} catch (UnsupportedEncodingException e) {
				ErrorReporter.logExceptionStackTrace(e);
				return;
			}

			IFileStore store;
			try {
				store = EFS.getStore(aFile.getLocationURI());
			} catch (CoreException e) {
				ErrorReporter.logExceptionStackTrace(e);
				return;
			}

			final IFileInfo fileInfo = store.fetchInfo();
			rootInt = (int) fileInfo.getLength();
		} else {
			return;
		}

		parse( reader, rootInt, aFile );
		md5_processing(reader2);
	}

	/**
	 * Parse TTCN-3 file using ANTLR v4
	 * Eclipse independent version
	 * @param aFile TTCN-3 file to parse, It cannot be null
	 */
	public void parse(final File aFile ) {
		BufferedReader bufferedReader;
		BufferedReader bufferedReader2;

		try {
			bufferedReader = new BufferedReader( new FileReader( aFile ) );
			bufferedReader2 = new BufferedReader( new FileReader( aFile ) );
		} catch ( FileNotFoundException e ) {
			//TODO: handle error
			return;
		}

		final int fileLength = (int)aFile.length();
		parse( bufferedReader, fileLength, null );
		md5_processing(bufferedReader2);
	}

	/**
	 * Parse TTCN-3 file using ANTLR v4
	 * @param aReader file to parse (cannot be null, closes aReader)
	 * @param aFileLength file length
	 * @param aEclipseFile Eclipse dependent resource file
	 */
	private void parse( final Reader aReader, final int aFileLength, final IFile aEclipseFile ) {
		final IPreferencesService prefs = Platform.getPreferencesService();
		final boolean realtimeEnabled = prefs.getBoolean(ProductConstants.PRODUCT_ID_DESIGNER, PreferenceConstants.ENABLEREALTIMEEXTENSION, false, null);

		final CharStream charStream = new UnbufferedCharStream( aReader );
		final Ttcn3Lexer lexer = new Ttcn3Lexer( charStream );
		lexer.setCommentTodo( true );
		lexer.setTokenFactory( new CommonTokenFactory( true ) );
		lexer.initRootInterval( aFileLength );
		if (realtimeEnabled) {
			lexer.enableRealtime();
		}

		final TitanListener lexerListener = new TitanListener();
		// remove ConsoleErrorListener
		lexer.removeErrorListeners();
		lexer.addErrorListener(lexerListener);

		// 1. Previously it was UnbufferedTokenStream(lexer), but it was changed to BufferedTokenStream, because UnbufferedTokenStream seems to be unusable. It is an ANTLR 4 bug.
		// Read this: https://groups.google.com/forum/#!topic/antlr-discussion/gsAu-6d3pKU
		// pr_PatternChunk[StringBuilder builder, boolean[] uni]:
		//   $builder.append($v.text); <-- exception is thrown here: java.lang.UnsupportedOperationException: interval 85..85 not in token buffer window: 86..341
		// 2. Changed from BufferedTokenStream to CommonTokenStream, otherwise tokens with "-> channel(HIDDEN)" are not filtered out in lexer.
		final CommonTokenStream tokenStream = new CommonTokenStream( lexer );

		Ttcn3Parser parser = new Ttcn3Parser( tokenStream );
		ParserUtilities.setBuildParseTree( parser );
		PreprocessedTokenStream preprocessor = null;

		if ( aEclipseFile != null && GlobalParser.TTCNPP_EXTENSION.equals( aEclipseFile.getFileExtension() ) ) {
			lexer.setTTCNPP();
			preprocessor = new PreprocessedTokenStream(lexer);
			preprocessor.setActualFile(aEclipseFile);
			if ( aEclipseFile.getProject() != null ) {
				preprocessor.setMacros( PreprocessorSymbolsOptionsData.getTTCN3PreprocessorDefines( aEclipseFile.getProject() ) );
			}
			parser = new Ttcn3Parser( preprocessor );
			ParserUtilities.setBuildParseTree( parser );
			preprocessor.setActualLexer(lexer);
			preprocessor.setParser(parser);
		}

		if ( aEclipseFile != null ) {
			lexer.setActualFile( aEclipseFile );
			parser.setActualFile( aEclipseFile );
			parser.setProject( aEclipseFile.getProject() );
		}

		// remove ConsoleErrorListener
		parser.removeErrorListeners();
		final TitanListener parserListener = new TitanListener();
		parser.addErrorListener( parserListener );

		// This is added because of the following ANTLR 4 bug:
		// Memory Leak in PredictionContextCache #499
		// https://github.com/antlr/antlr4/issues/499
		final DFA[] decisionToDFA = parser.getInterpreter().decisionToDFA;
		parser.setInterpreter(new ParserATNSimulator(parser, parser.getATN(), decisionToDFA, new PredictionContextCache()));

		//try SLL mode
		try {
			parser.getInterpreter().setPredictionMode(PredictionMode.SLL);
			final ParseTree root = parser.pr_TTCN3File();
			ParserUtilities.logParseTree( root, parser );
			warningsAndErrors = parser.getWarningsAndErrors();
			mErrorsStored = lexerListener.getErrorsStored();
			mErrorsStored.addAll( parserListener.getErrorsStored() );
		} catch (RecognitionException e) {
			// quit
		}

		if (!warningsAndErrors.isEmpty() || !mErrorsStored.isEmpty()) {
			//SLL mode might have failed, try LL mode
			try {
				final CharStream charStream2 = new UnbufferedCharStream( aReader );
				lexer.setInputStream(charStream2);
				//lexer.reset();
				parser.reset();
				parserListener.reset();
				parser.getInterpreter().setPredictionMode(PredictionMode.LL);
				final ParseTree root = parser.pr_TTCN3File();
				ParserUtilities.logParseTree( root, parser );
				warningsAndErrors = parser.getWarningsAndErrors();
				mErrorsStored = lexerListener.getErrorsStored();
				mErrorsStored.addAll( parserListener.getErrorsStored() );
			} catch(RecognitionException e) {

			}
		}

		unsupportedConstructs = parser.getUnsupportedConstructs();
		rootInterval = lexer.getRootInterval();
		actualTtc3Module = parser.getModule();
		if ( preprocessor != null ) {
			// if the file was preprocessed
			mErrorsStored.addAll(preprocessor.getErrorStorage());
			warningsAndErrors.addAll( preprocessor.getWarnings() );
			unsupportedConstructs.addAll( preprocessor.getUnsupportedConstructs() );
			if ( actualTtc3Module != null ) {
				actualTtc3Module.setIncludedFiles( preprocessor.getIncludedFiles() );
				actualTtc3Module.setInactiveCodeLocations( preprocessor.getInactiveCodeLocations() );
			}
		}
		//TODO: empty mErrorsStored not to store errors from the previous parse round in case of exception

		try {
			aReader.close();
		} catch (IOException e) {
		}
	}

	private void md5_processing(final Reader aReader) {
		final IPreferencesService prefs = Platform.getPreferencesService();
		final boolean realtimeEnabled = prefs.getBoolean(ProductConstants.PRODUCT_ID_DESIGNER, PreferenceConstants.ENABLEREALTIMEEXTENSION, false, null);;

		final CharStream charStream = new UnbufferedCharStream( aReader );
		final Ttcn3Lexer lexer = new Ttcn3Lexer( charStream );
		lexer.setCommentTodo( true );
		lexer.setTokenFactory( new CommonTokenFactory( true ) );
		if (realtimeEnabled) {
			lexer.enableRealtime();
		}

		final TitanListener lexerListener = new TitanListener();
		lexer.removeErrorListeners();
		lexer.addErrorListener(lexerListener);

		MessageDigest md5 = null;
		try {
			md5 = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			ErrorReporter.logExceptionStackTrace(e);
		}

		List<? extends Token> tokens = lexer.getAllTokens();
		for (Token token: tokens) {
			if (token.getChannel() != Token.HIDDEN_CHANNEL) {
				final String text = token.getText();
				md5.update(text.getBytes());
				md5.update(" ".getBytes());
			}
		}
		if (md5 != null) {
			digest = md5.digest();
			  if (actualTtc3Module != null) {
				  actualTtc3Module.addMD5Digest(digest);
			  }
		}
	}
}
