/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.values.expressions;

import java.text.MessageFormat;
import java.util.List;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.IType.Type_type;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.values.Expression_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Referenced_Value;
import org.eclipse.titan.designer.compiler.JavaGenData;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * @author Kristof Szabados
 * */
public final class UndefRunningExpression extends Expression_Value {
	private final Reference reference;

	private Expression_Value realExpression;
	private final boolean anyfrom;
	private final Reference index_redirection;

	public UndefRunningExpression(final Reference reference, final Reference index_redirection, final boolean anyFrom) {
		this.reference = reference;
		this.index_redirection = index_redirection;
		this.anyfrom = anyFrom;

		if (reference != null) {
			reference.setFullNameParent(this);
		}
		if (index_redirection != null) {
			index_redirection.setFullNameParent(this);
		}
	}

	@Override
	/** {@inheritDoc} */
	public Operation_type getOperationType() {
		return Operation_type.UNDEFINED_RUNNING_OPERATION;
	}

	@Override
	/** {@inheritDoc} */
	public boolean checkExpressionSelfReference(final CompilationTimeStamp timestamp, final Assignment lhs) {
		if (lhs == reference.getRefdAssignment(timestamp, false)) {
			return true;
		}
		if (lhs == index_redirection.getRefdAssignment(timestamp, false)) {
			return true;
		}

		return false;
	}

	@Override
	/** {@inheritDoc} */
	public String createStringRepresentation() {
		final StringBuilder builder = new StringBuilder();
		builder.append(reference.getDisplayName()).append(".running");
		return builder.toString();
	}

	@Override
	/** {@inheritDoc} */
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);

		if (reference != null) {
			reference.setMyScope(scope);
		}
		if (index_redirection != null) {
			index_redirection.setMyScope(scope);
		}
	}

	@Override
	/** {@inheritDoc} */
	public void setCodeSection(final CodeSectionType codeSection) {
		super.setCodeSection(codeSection);

		if (reference != null) {
			reference.setCodeSection(codeSection);
		}
		if (realExpression != null) {
			realExpression.setCodeSection(codeSection);
		}
		if (index_redirection != null) {
			index_redirection.setCodeSection(codeSection);
		}
	}

	@Override
	/** {@inheritDoc} */
	public StringBuilder getFullName(final INamedNode child) {
		final StringBuilder builder = super.getFullName(child);

		if (reference == child) {
			return builder.append(OPERAND);
		} else if (index_redirection == child) {
			return builder.append(REDIRECTINDEX);
		}

		return builder;
	}

	@Override
	/** {@inheritDoc} */
	public Type_type getExpressionReturntype(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue) {
		return Type_type.TYPE_BOOL;
	}

	@Override
	/** {@inheritDoc} */
	public boolean isUnfoldable(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue,
			final IReferenceChain referenceChain) {
		return true;
	}

	/**
	 * Checks the parameters of the expression and if they are valid in
	 * their position in the expression or not.
	 *
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle.
	 * @param expectedValue
	 *                the kind of value expected.
	 * @param referenceChain
	 *                a reference chain to detect cyclic references.
	 * */
	private void checkExpressionOperands(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue,
			final IReferenceChain referenceChain) {
		setIsErroneous(false);

		if (reference == null) {
			return;
		}

		final Assignment assignment = reference.getRefdAssignment(timestamp, true);
		if (assignment == null) {
			setIsErroneous(true);
			return;
		}

		switch (assignment.getAssignmentType()) {
		case A_TIMER:
		case A_PAR_TIMER: {
			realExpression = new TimerRunningExpression(reference, anyfrom, index_redirection);
			realExpression.setMyScope(getMyScope());
			realExpression.setFullNameParent(this);
			realExpression.setLocation(getLocation());
			realExpression.evaluateValue(timestamp, expectedValue, referenceChain);
			break;
		}
		case A_CONST:
		case A_EXT_CONST:
		case A_MODULEPAR:
		case A_VAR:
		case A_FUNCTION_RVAL:
		case A_EXT_FUNCTION_RVAL:
		case A_PAR_VAL:
		case A_PAR_VAL_IN:
		case A_PAR_VAL_OUT:
		case A_PAR_VAL_INOUT: {
			final Referenced_Value value = new Referenced_Value(reference);
			value.setMyScope(getMyScope());
			value.setFullNameParent(this);
			value.getValueRefdLast(timestamp, referenceChain);
			realExpression = new ComponentRunningExpression(value, index_redirection, anyfrom);
			realExpression.setMyScope(getMyScope());
			realExpression.setFullNameParent(this);
			realExpression.setLocation(getLocation());
			realExpression.evaluateValue(timestamp, expectedValue, referenceChain);
			break;
		}
		default:
			reference.getLocation()
			.reportSemanticError(
					MessageFormat.format(
							"First operand of operation `<timer or component> running'' should be timer or component reference instead of {0}",
							assignment.getDescription()));
			setIsErroneous(true);
			break;
		}
	}

	@Override
	/** {@inheritDoc} */
	public IValue evaluateValue(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue,
			final IReferenceChain referenceChain) {
		if (lastTimeChecked != null && !lastTimeChecked.isLess(timestamp)) {
			return lastValue;
		}

		isErroneous = false;
		lastTimeChecked = timestamp;
		lastValue = this;

		if (reference == null) {
			return lastValue;
		}

		checkExpressionOperands(timestamp, expectedValue, referenceChain);

		return lastValue;
	}

	@Override
	/** {@inheritDoc} */
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}

		if (reference != null) {
			reference.updateSyntax(reparser, false);
			reparser.updateLocation(reference.getLocation());
		}
		if (index_redirection != null) {
			index_redirection.updateSyntax(reparser, false);
			reparser.updateLocation(index_redirection.getLocation());
		}
	}

	@Override
	/** {@inheritDoc} */
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (reference != null) {
			reference.findReferences(referenceFinder, foundIdentifiers);
		}

		if (index_redirection != null) {
			index_redirection.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	@Override
	/** {@inheritDoc} */
	protected boolean memberAccept(final ASTVisitor v) {
		if (reference != null && !reference.accept(v)) {
			return false;
		}
		if (index_redirection != null && !index_redirection.accept(v)) {
			return false;
		}
		return true;
	}

	@Override
	/** {@inheritDoc} */
	public boolean canGenerateSingleExpression() {
		if (realExpression != null) {
			return realExpression.canGenerateSingleExpression();
		}

		return false;
	}

	@Override
	/** {@inheritDoc} */
	public boolean returnsNative() {
		if (realExpression != null && realExpression != this) {
			return realExpression.returnsNative();
		}

		return false;
	}

	@Override
	/** {@inheritDoc} */
	public StringBuilder generateSingleExpression(final JavaGenData aData) {
		if (realExpression != null) {
			return realExpression.generateSingleExpression(aData);
		}

		return new StringBuilder();
	}

	@Override
	/** {@inheritDoc} */
	public void generateCodeExpressionExpression(final JavaGenData aData, final ExpressionStruct expression) {
		if (realExpression != null) {
			realExpression.generateCodeExpressionExpression(aData, expression);
		}
	}
}
