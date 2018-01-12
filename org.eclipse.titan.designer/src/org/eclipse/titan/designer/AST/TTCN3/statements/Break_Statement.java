/******************************************************************************
 * Copyright (c) 2000-2017 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.statements;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.compiler.JavaGenData;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * @author Kristof Szabados
 * @author Farkas Izabella Ingrid
 * */
public final class Break_Statement extends Statement {
	private static final String INCORRECTUSAGE = "Break statement cannot be used outside loops,"
			+ " alt or interleave statements, altsteps or response and exception handling part of call operations";
	private static final String STATEMENT_NAME = "break";

	private Statement loop_stmt;
	private AltGuards altGuards;

	@Override
	/** {@inheritDoc} */
	public Statement_type getType() {
		return Statement_type.S_BREAK;
	}

	@Override
	/** {@inheritDoc} */
	public String getStatementName() {
		return STATEMENT_NAME;
	}

	@Override
	/** {@inheritDoc} */
	public boolean isTerminating(final CompilationTimeStamp timestamp) {
		return true;
	}

	@Override
	/** {@inheritDoc} */
	protected void setMyLaicStmt(final AltGuards pAltGuards, final Statement pLoopStmt) {
		if (pLoopStmt != null) {
			loop_stmt = pLoopStmt;
		}
		altGuards = pAltGuards;
	}

	@Override
	/** {@inheritDoc} */
	public void check(final CompilationTimeStamp timestamp) {
		if (lastTimeChecked != null && !lastTimeChecked.isLess(timestamp)) {
			return;
		}
		if (myStatementBlock == null || !myStatementBlock.hasEnclosingLoopOrAltguard()) {
			location.reportSemanticError(INCORRECTUSAGE);
		}

		if(loop_stmt == null && altGuards == null) {
			location.reportSemanticError(INCORRECTUSAGE);
		}

		if (loop_stmt != null) {
			//FIXME:
			// brk_cnt.loop_stmt->loop.has_brk=true;
		}
		lastTimeChecked = timestamp;
	}

	@Override
	/** {@inheritDoc} */
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}
	}

	@Override
	/** {@inheritDoc} */
	protected boolean memberAccept(final ASTVisitor v) {
		// no members
		return true;
	}

	@Override
	/** {@inheritDoc} */
	public void generateCode( final JavaGenData aData, final StringBuilder source ) {
		if (altGuards != null && altGuards.getIsAltstep()) {
			source.append("return TitanAlt_Status.ALT_BREAK;\n");
		} else {
			source.append("if (true) { break; }\n"); //simple break causes java semantic error
		}
	}
}
