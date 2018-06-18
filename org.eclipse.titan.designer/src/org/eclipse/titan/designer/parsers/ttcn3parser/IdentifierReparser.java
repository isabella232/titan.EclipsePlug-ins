/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.parsers.ttcn3parser;

import org.antlr.v4.runtime.tree.ParseTree;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.parsers.ParserUtilities;
import org.eclipse.titan.designer.parsers.ttcn3parser.Ttcn3Reparser.Pr_IdentifierContext;

/**
 * Reparser for getting pr_Identifier
 * @author Arpad Lovassy
 */
public class IdentifierReparser implements IIdentifierReparser {

	private final TTCN3ReparseUpdater mReparser;

	private Identifier mIdentifier;

	public IdentifierReparser( final TTCN3ReparseUpdater aReparser ) {
		mReparser = aReparser;
	}

	@Override
	public int parse() {
		return mReparser.parse(new ITTCN3ReparseBase() {
			@Override
			public void reparse(final Ttcn3Reparser parser) {
				final Pr_IdentifierContext root = parser.pr_Identifier();
				final Identifier identifier2 = root.identifier;
				ParserUtilities.logParseTree( root, parser );

				final ParseTree rootEof = parser.pr_EndOfFile();
				ParserUtilities.logParseTree( rootEof, parser );
				if ( parser.isErrorListEmpty() ) {
					mIdentifier = identifier2;
				}
			}
		});
	}

	@Override
	public int parseAndSetNameChanged() {
		return mReparser.parse(new ITTCN3ReparseBase() {
			@Override
			public void reparse(final Ttcn3Reparser parser) {
				final Pr_IdentifierContext root = parser.pr_Identifier();
				final Identifier identifier2 = root.identifier;
				ParserUtilities.logParseTree( root, parser );

				final ParseTree rootEof = parser.pr_EndOfFile();
				ParserUtilities.logParseTree( rootEof, parser );
				if ( parser.isErrorListEmpty() ) {
					mIdentifier = identifier2;
					// default behaviour
					mReparser.setNameChanged(true);
				}
			}
		});
	}

	@Override
	public final Identifier getIdentifier() {
		return mIdentifier;
	}

}
