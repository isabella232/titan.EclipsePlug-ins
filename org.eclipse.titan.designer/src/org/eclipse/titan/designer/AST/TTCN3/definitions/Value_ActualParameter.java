/******************************************************************************
 * Copyright (c) 2000-2017 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.definitions;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.TTCN3.IIncrementallyUpdateable;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.ExpressionStruct;
import org.eclipse.titan.designer.compiler.JavaGenData;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * Represents an actual parameter that has a Value as its actual value.
 *
 * @author Kristof Szabados
 * */
public final class Value_ActualParameter extends ActualParameter {

	private final IValue value;

	public Value_ActualParameter(final IValue value) {
		this.value = value;
	}

	public IValue getValue() {
		return value;
	}

	@Override
	/** {@inheritDoc} */
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);

		if (value != null) {
			value.setMyScope(scope);
		}
	}

	@Override
	/** {@inheritDoc} */
	public void checkRecursions(final CompilationTimeStamp timestamp, final IReferenceChain referenceChain) {
		if (value != null) {
			referenceChain.markState();
			value.checkRecursions(timestamp, referenceChain);
			referenceChain.previousState();
		}
	}

	@Override
	/** {@inheritDoc} */
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}

		if (value instanceof IIncrementallyUpdateable) {
			((IIncrementallyUpdateable) value).updateSyntax(reparser, false);
			reparser.updateLocation(value.getLocation());
		} else if (value != null) {
			throw new ReParseException();
		}
	}

	@Override
	/** {@inheritDoc} */
	protected boolean memberAccept(final ASTVisitor v) {
		if (value != null) {
			if (!value.accept(v)) {
				return false;
			}
		}
		return true;
	}

	@Override
	/** {@inheritDoc} */
	public void generateCode( final JavaGenData aData, final ExpressionStruct expression) {
		//TODO not complete implementation pl. copy_needed, formal parameter missing
		if (value != null ) {
			StringBuilder expressionExpression = new StringBuilder();
			ExpressionStruct valueExpression = new ExpressionStruct();
			value.generateCodeExpression(aData, valueExpression);
			if(valueExpression.preamble.length() > 0) {
				expression.preamble.append(valueExpression.preamble);
			}
			if(valueExpression.postamble.length() == 0) {
				expressionExpression.append(valueExpression.expression);
			} else {
				// make sure the postambles of the parameters are executed before the
				// function call itself (needed if the value contains function calls
				// with lazy or fuzzy parameters)
				String tempId = aData.getTemporaryVariableName();
				value.getMyGovernor().getGenNameValue(aData, expression.preamble, myScope);
				expression.preamble.append(" ");
				expression.preamble.append(tempId);
				expression.preamble.append("(");
				expression.preamble.append(valueExpression.expression);
				expression.preamble.append(")");

				expression.preamble.append(valueExpression.postamble);
				expressionExpression.append(tempId);
			}

			//TODO copy might be needed here
			expression.expression.append(expressionExpression);
		}
	}
}
