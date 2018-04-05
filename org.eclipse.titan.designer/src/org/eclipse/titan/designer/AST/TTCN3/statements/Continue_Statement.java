/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
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
public final class Continue_Statement extends Statement {
	private static final String INCORRECTUSAGE = "Continue statement cannot be used outside loops";

	private static final String STATEMENT_NAME = "continue";

	private Statement loop_stmt;
	private AltGuards altGuards;

	@Override
	/** {@inheritDoc} */
	public Statement_type getType() {
		return Statement_type.S_CONTINUE;
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
		if (myStatementBlock == null || !myStatementBlock.hasEnclosingLoop()) {
			location.reportSemanticError(INCORRECTUSAGE);
		}

		if (loop_stmt != null) {
			// FIXME:
			// loop_stmt->loop.has_cnt=true;
			if (altGuards != null) {
				//FIXME:
				// loop_stmt->loop.has_cnt_in_ags=true;
			}
		} else {
			location.reportSemanticError(INCORRECTUSAGE);
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
		if (loop_stmt != null) {
			//FIXME: implement case of interlive
			//if (altGuards != null && iterate_once && is_ilt)
			//	source.append("break;\n");
			//} else
			if (loop_stmt instanceof For_Statement) {
				For_Statement forStatment = (For_Statement) loop_stmt;
				forStatment.generateCodeStepAssigment(aData, source);
				source.append("continue;\n");
			} else if (loop_stmt instanceof DoWhile_Statement) {
				DoWhile_Statement doWhileStatement = (DoWhile_Statement) loop_stmt;
				doWhileStatement.generateCodeConditional(aData, source);
				source.append("continue;\n");
			} else {
				source.append("continue;\n");
			}
		} else {
			// FIXME: FATAL_ERROR("Statement::generate_code_continue()");
		}
	}
}
