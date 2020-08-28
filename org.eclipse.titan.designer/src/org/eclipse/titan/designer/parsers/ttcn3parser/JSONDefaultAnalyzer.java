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
import org.eclipse.titan.designer.parsers.ttcn3parser.Ttcn3Parser.Pr_SingleExpressionContext;

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

		final Pr_SingleExpressionContext root = parser.pr_SingleExpression();
		parser.pr_EndOfFile();

		if (lexerListener.getErrorsStored() != null) {
			for (int i = 0; i < lexerListener.getErrorsStored().size(); i++) {
				final Location temp = new Location(location);
				temp.setOffset(temp.getOffset() + 1);
				ParserMarkerSupport.createOnTheFlyMixedMarker((IFile) location.getFile(), lexerListener.getErrorsStored().get(i), IMarker.SEVERITY_ERROR, temp);
			}
		}
		if (parserListener.getErrorsStored() != null) {
			for (int i = 0; i < parserListener.getErrorsStored().size(); i++) {
				final Location temp = new Location(location);
				temp.setOffset(temp.getOffset() + 1);
				ParserMarkerSupport.createOnTheFlyMixedMarker((IFile) location.getFile(), parserListener.getErrorsStored().get(i), IMarker.SEVERITY_ERROR, temp);
			}
		}

		//no syntax errors found
		return root.value;
	}
}
