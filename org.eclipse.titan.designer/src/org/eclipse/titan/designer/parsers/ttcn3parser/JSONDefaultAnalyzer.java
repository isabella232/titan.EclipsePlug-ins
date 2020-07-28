/******************************************************************************
 * Copyright (c) 2000-2020 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.parsers.ttcn3parser;

import java.io.Reader;
import java.io.StringReader;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenFactory;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.UnbufferedCharStream;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.titan.common.parsers.TitanListener;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.parsers.ParserMarkerSupport;
import org.eclipse.titan.designer.parsers.ParserUtilities;
import org.eclipse.titan.designer.parsers.ttcn3parser.Ttcn3Parser.Pr_CharStringValueContext;
import org.eclipse.titan.designer.parsers.ttcn3parser.Ttcn3Parser.Pr_PrimaryContext;

/**
 * This is helper class, letting us parse JSON default attribute values as TTCN-3 values.
 * 
 * FIXME most probably not the final location for this function, but a good starting location.
 * 
 * @author Kristof Szabados
 */
public class JSONDefaultAnalyzer {
	
	public IValue parseJSONDefaultValue(final String defaultString, final Location location) {
		final Reader reader = new StringReader( defaultString );
		final CharStream charStream = new UnbufferedCharStream( reader );
		final Ttcn3Lexer lexer = new Ttcn3Lexer( charStream );
		lexer.setTokenFactory( new CommonTokenFactory( true ) );
		lexer.initRootInterval( defaultString.length() );
		lexer.removeErrorListeners();

		final CommonTokenStream tokenStream = new CommonTokenStream( lexer );
		final Ttcn3Parser parser = new Ttcn3Parser( tokenStream );
		ParserUtilities.setBuildParseTree( parser );

		lexer.setActualFile((IFile)location.getFile());
		parser.setActualFile((IFile)location.getFile());
		parser.setProject(location.getFile().getProject());
		parser.setLine(location.getLine());
		parser.setOffset(location.getOffset());
		final TitanListener lexerListener = new TitanListener();
		// remove ConsoleErrorListener
		lexer.removeErrorListeners();
		lexer.addErrorListener(lexerListener);
		parser.removeErrorListeners();
		final TitanListener parserListener = new TitanListener();
		parser.addErrorListener( parserListener );

		final Pr_PrimaryContext root = parser.pr_Primary();
		parser.pr_EndOfFile();

		if (!lexerListener.getErrorsStored().isEmpty() || !parserListener.getErrorsStored().isEmpty()) {
			//Lets assume it is a string of some type
			final String newString = "\"" + defaultString + "\"";
			final Reader reader2 = new StringReader( newString );
			final CharStream charStream2 = new UnbufferedCharStream( reader2 );
			final Ttcn3Lexer lexer2 = new Ttcn3Lexer( charStream2 );
			lexer2.setTokenFactory( new CommonTokenFactory( true ) );
			lexer2.initRootInterval( newString.length() );
			lexer2.removeErrorListeners();

			final CommonTokenStream tokenStream2 = new CommonTokenStream( lexer2 );
			final Ttcn3Parser parser2 = new Ttcn3Parser( tokenStream2 );
			ParserUtilities.setBuildParseTree( parser2 );

			lexer2.setActualFile((IFile)location.getFile());
			parser2.setActualFile((IFile)location.getFile());
			parser2.setProject(location.getFile().getProject());
			parser2.setLine(location.getLine());
			parser2.setOffset(location.getOffset());
			final TitanListener lexerListener2 = new TitanListener();
			// remove ConsoleErrorListener
			lexer2.removeErrorListeners();
			lexer2.addErrorListener(lexerListener2);
			parser2.removeErrorListeners();
			final TitanListener parserListener2 = new TitanListener();
			parser2.addErrorListener( parserListener2 );

			final Pr_CharStringValueContext root2 = parser2.pr_CharStringValue();
			parser2.pr_EndOfFile();

			if (lexerListener2.getErrorsStored() != null) {
				for (int i = 0; i < lexerListener2.getErrorsStored().size(); i++) {
					final Location temp = new Location(location);
					temp.setOffset(temp.getOffset() + 1);
					ParserMarkerSupport.createOnTheFlyMixedMarker((IFile) location.getFile(), lexerListener2.getErrorsStored().get(i), IMarker.SEVERITY_ERROR, temp);
				}
			}
			if (parserListener2.getErrorsStored() != null) {
				for (int i = 0; i < parserListener2.getErrorsStored().size(); i++) {
					final Location temp = new Location(location);
					temp.setOffset(temp.getOffset() + 1);
					ParserMarkerSupport.createOnTheFlyMixedMarker((IFile) location.getFile(), parserListener2.getErrorsStored().get(i), IMarker.SEVERITY_ERROR, temp);
				}
			}

			return root2.value;
		}

		//no syntax errors found
		return root.value;
	}
}
