/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.statements;

import java.util.List;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Timer;
import org.eclipse.titan.designer.AST.TTCN3.types.PortGenerator;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.ExpressionStruct;
import org.eclipse.titan.designer.compiler.JavaGenData;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;
/**
 * @author Kristof Szabados
 * */
public final class Timeout_Statement extends Statement {
	private static final String FULLNAMEPART1 = ".timerreference";
	private static final String FULLNAMEPART2 = ".redirectIndex";
	private static final String STATEMENT_NAME = "timeout";

	private final Reference timerReference;

	private final boolean any_from;
	private final Reference indexRedirection;

	public Timeout_Statement(final Reference timerReference) {
		this.timerReference = timerReference;
		this.any_from = false;
		this.indexRedirection = null;

		if (timerReference != null) {
			timerReference.setFullNameParent(this);
		}
	}

	public Timeout_Statement(final Reference timerReference, final boolean any_from, final Reference index_redirect) {
		this.timerReference = timerReference;
		this.any_from = any_from;
		this.indexRedirection = index_redirect;

		if (timerReference != null) {
			timerReference.setFullNameParent(this);
		}
		if (index_redirect != null) {
			index_redirect.setFullNameParent(this);
		}
	}

	@Override
	/** {@inheritDoc} */
	public Statement_type getType() {
		return Statement_type.S_TIMEOUT;
	}

	@Override
	/** {@inheritDoc} */
	public String getStatementName() {
		return STATEMENT_NAME;
	}

	@Override
	/** {@inheritDoc} */
	public StringBuilder getFullName(final INamedNode child) {
		final StringBuilder builder = super.getFullName(child);

		if (timerReference == child) {
			return builder.append(FULLNAMEPART1);
		} else if (indexRedirection == child) {
			return builder.append(FULLNAMEPART2);
		}

		return builder;
	}

	@Override
	/** {@inheritDoc} */
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		if (timerReference != null) {
			timerReference.setMyScope(scope);
		}
		if (indexRedirection != null) {
			indexRedirection.setMyScope(scope);
		}
	}

	@Override
	/** {@inheritDoc} */
	public boolean hasReceivingStatement() {
		return true;
	}

	@Override
	/** {@inheritDoc} */
	public boolean canRepeat() {
		return false;
	}

	@Override
	/** {@inheritDoc} */
	public void check(final CompilationTimeStamp timestamp) {
		if (lastTimeChecked != null && !lastTimeChecked.isLess(timestamp)) {
			return;
		}

		Start_Timer_Statement.checkTimerReference(timestamp, timerReference, any_from);

		if (timerReference != null && indexRedirection != null) {
			final Assignment assignment = timerReference.getRefdAssignment(timestamp, false);
			checkIndexRedirection(timestamp, indexRedirection, assignment == null ? null : ((Def_Timer)assignment).getDimensions(), any_from, "timer");
		}

		lastTimeChecked = timestamp;
	}

	@Override
	/** {@inheritDoc} */
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}

		if (timerReference != null) {
			timerReference.updateSyntax(reparser, false);
			reparser.updateLocation(timerReference.getLocation());
		}
	}

	@Override
	/** {@inheritDoc} */
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (timerReference == null) {
			return;
		}

		timerReference.findReferences(referenceFinder, foundIdentifiers);
	}

	@Override
	/** {@inheritDoc} */
	protected boolean memberAccept(final ASTVisitor v) {
		if (timerReference != null && !timerReference.accept(v)) {
			return false;
		}
		return true;
	}

	@Override
	/** {@inheritDoc} */
	public void generateCode(final JavaGenData aData, final StringBuilder source) {
		final ExpressionStruct expression = new ExpressionStruct();
		generateCodeExpression(aData, expression, null);

		PortGenerator.generateCodeStandalone(aData, source, expression.expression.toString(), getStatementName(), canRepeat(), getLocation());
	}

	@Override
	/** {@inheritDoc} */
	public void generateCodeExpression(final JavaGenData aData, final ExpressionStruct expression, final String callTimer) {
		aData.addBuiltinTypeImport("TitanTimer");

		if (timerReference == null) {
			expression.expression.append("TitanTimer.anyTimeout()");
		} else {
			timerReference.generateCode(aData, expression);
			expression.expression.append(".timeout(");
			if (indexRedirection == null) {
				expression.expression.append("null");
			} else {
				generateCodeIndexRedirect(aData, expression, indexRedirection, getMyScope());
			}
			expression.expression.append(")");
		}
	}
}
