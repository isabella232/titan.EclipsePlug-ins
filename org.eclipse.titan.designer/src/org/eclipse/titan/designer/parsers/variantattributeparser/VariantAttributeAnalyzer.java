/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.parsers.variantattributeparser;

import java.io.StringReader;
import java.util.concurrent.atomic.AtomicBoolean;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenFactory;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.UnbufferedCharStream;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.titan.common.parsers.TitanListener;
import org.eclipse.titan.designer.GeneralConstants;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.MarkerHandler;
import org.eclipse.titan.designer.AST.TTCN3.attributes.AttributeSpecification;
import org.eclipse.titan.designer.AST.TTCN3.attributes.RawAST;
import org.eclipse.titan.designer.parsers.ParserMarkerSupport;

/**
 * Variant attribute parser analyzer
 *
 * FIXME currently does only syntactic checking, no data structure is extracted
 *
 * @author Kristof Szabados
 *
 */
public class VariantAttributeAnalyzer {

	public void parse(final RawAST rawAST, final AttributeSpecification specification, final int lengthMultiplier, final AtomicBoolean raw_found) {
		VariantAttributeLexer lexer;
		Location location = specification.getLocation();

		StringReader reader = new StringReader(specification.getSpecification());
		CharStream charStream = new UnbufferedCharStream(reader);
		lexer = new VariantAttributeLexer(charStream);
		lexer.setTokenFactory(new CommonTokenFactory(true));
		TitanListener lexerListener = new TitanListener();
		lexer.removeErrorListeners();
		lexer.addErrorListener(lexerListener);

		final CommonTokenStream tokenStream = new CommonTokenStream( lexer );

		VariantAttributeParser parser = new VariantAttributeParser( tokenStream );
		parser.setBuildParseTree(false);

		TitanListener parserListener = new TitanListener();
		parser.removeErrorListeners();
		parser.addErrorListener(parserListener);

		parser.setActualFile((IFile)location.getFile());
		parser.setLine(location.getLine());
		parser.setOffset(location.getOffset() + 1);

		MarkerHandler.markMarkersForRemoval(GeneralConstants.ONTHEFLY_SYNTACTIC_MARKER, location.getFile(), location.getOffset(),
				location.getEndOffset());

		parser.setRawAST(rawAST);
		parser.setLengthMultiplier(lengthMultiplier);
		parser.pr_AttribSpec();

		if (!lexerListener.getErrorsStored().isEmpty()) {
			for (int i = 0; i < lexerListener.getErrorsStored().size(); i++) {
				Location temp = new Location(location);
				temp.setOffset(temp.getOffset() + 1);
				ParserMarkerSupport.createOnTheFlyMixedMarker((IFile) location.getFile(), lexerListener.getErrorsStored().get(i), IMarker.SEVERITY_ERROR, temp);
			}
		}
		if (!parserListener.getErrorsStored().isEmpty()) {
			for (int i = 0; i < parserListener.getErrorsStored().size(); i++) {
				Location temp = new Location(location);
				temp.setOffset(temp.getOffset() + 1);
				ParserMarkerSupport.createOnTheFlyMixedMarker((IFile) location.getFile(), parserListener.getErrorsStored().get(i), IMarker.SEVERITY_ERROR, temp);
			}
		}

		if (!raw_found.get()) {
			raw_found.set(parser.getRawFound());
		}
	}
}
