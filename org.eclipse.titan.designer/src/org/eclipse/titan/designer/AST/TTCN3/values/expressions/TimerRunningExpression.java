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
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Timer;
import org.eclipse.titan.designer.AST.TTCN3.statements.Statement;
import org.eclipse.titan.designer.AST.TTCN3.values.ArrayDimensions;
import org.eclipse.titan.designer.AST.TTCN3.values.Expression_Value;
import org.eclipse.titan.designer.compiler.JavaGenData;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * @author Kristof Szabados
 * */
public final class TimerRunningExpression extends Expression_Value {
	private static final String OPERANDERROR = "The operand of operation `timer running'':"
			+ " Reference to a single timer `{0}'' cannot have field or array sub-references";
	private static final String OPERANDERROR2 = "The operand of operation `timer running'' should be a timer instead of `{0}''";
	private static final String OPERATIONNAME = "timer running";

	private final Reference reference;

	private final boolean any_from;
	private final Reference indexRedirection;

	public TimerRunningExpression(final Reference reference) {
		this.reference = reference;
		this.any_from = false;
		this.indexRedirection = null;

		if (reference != null) {
			reference.setFullNameParent(this);
		}
	}

	public TimerRunningExpression(final Reference reference, final boolean any_from, final Reference index_redirect) {
		this.reference = reference;
		this.any_from = any_from;
		this.indexRedirection = index_redirect;

		if (reference != null) {
			reference.setFullNameParent(this);
		}
		if (index_redirect != null) {
			index_redirect.setFullNameParent(this);
		}
	}

	@Override
	/** {@inheritDoc} */
	public Operation_type getOperationType() {
		return Operation_type.TIMER_RUNNING_OPERATION;
	}

	@Override
	/** {@inheritDoc} */
	public boolean checkExpressionSelfReference(final CompilationTimeStamp timestamp, final Assignment lhs) {
		//assume no self-ref
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
		if (indexRedirection != null) {
			indexRedirection.setMyScope(scope);
		}
	}

	@Override
	/** {@inheritDoc} */
	public void setCodeSection(final CodeSectionType codeSection) {
		super.setCodeSection(codeSection);

		if (reference != null) {
			reference.setCodeSection(codeSection);
		}
		if (indexRedirection != null) {
			indexRedirection.setCodeSection(codeSection);
		}
	}

	@Override
	/** {@inheritDoc} */
	public StringBuilder getFullName(final INamedNode child) {
		final StringBuilder builder = super.getFullName(child);

		if (reference == child) {
			return builder.append(OPERAND);
		} else if (indexRedirection == child) {
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
		if (reference == null) {
			return;
		}

		final Assignment assignment = reference.getRefdAssignment(timestamp, true);
		if (assignment == null) {
			setIsErroneous(true);
			return;
		}

		switch (assignment.getAssignmentType()) {
		case A_TIMER: {
			final ArrayDimensions dimensions = ((Def_Timer) assignment).getDimensions();
			if (dimensions != null) {
				dimensions.checkIndices(timestamp, reference, "timer", false, Expected_Value_type.EXPECTED_DYNAMIC_VALUE, any_from);
			} else if (reference.getSubreferences().size() > 1) {
				reference.getLocation().reportSemanticError(
						MessageFormat.format(OPERANDERROR, assignment.getIdentifier().getDisplayName()));
			}
			break;
		}
		case A_PAR_TIMER:
			if (reference.getSubreferences().size() > 1) {
				reference.getLocation().reportSemanticError(
						MessageFormat.format(OPERANDERROR, assignment.getIdentifier().getDisplayName()));
			}
			break;
		default:
			reference.getLocation().reportSemanticError(MessageFormat.format(OPERANDERROR2, assignment.getDescription()));
			setIsErroneous(true);
			break;
		}

		checkExpressionDynamicPart(expectedValue, OPERATIONNAME, true, true, false);

		if (indexRedirection != null) {
			Statement.checkIndexRedirection(timestamp, indexRedirection, assignment == null ? null : ((Def_Timer)assignment).getDimensions(), any_from, "timer");
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
		if (indexRedirection != null) {
			indexRedirection.updateSyntax(reparser, false);
			reparser.updateLocation(indexRedirection.getLocation());
		}
	}

	@Override
	/** {@inheritDoc} */
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (reference != null) {
			reference.findReferences(referenceFinder, foundIdentifiers);
		}

		if (indexRedirection != null) {
			indexRedirection.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	@Override
	/** {@inheritDoc} */
	protected boolean memberAccept(final ASTVisitor v) {
		if (reference != null && !reference.accept(v)) {
			return false;
		}

		if (indexRedirection != null && !indexRedirection.accept(v)) {
			return false;
		}

		return true;
	}

	@Override
	/** {@inheritDoc} */
	public boolean canGenerateSingleExpression() {
		if (indexRedirection != null) {
			return false;
		}

		return reference != null && reference.hasSingleExpression();
	}

	@Override
	/** {@inheritDoc} */
	public boolean returnsNative() {
		return true;
	}

	@Override
	/** {@inheritDoc} */
	public void generateCodeExpressionExpression(final JavaGenData aData, final ExpressionStruct expression) {
		reference.generateCode(aData, expression);
		generateCodeExpressionOptionalFieldReference(aData, expression, reference);
		expression.expression.append(".running(");
		if (indexRedirection == null) {
			expression.expression.append("null");
		} else {
			Statement.generateCodeIndexRedirect(aData, expression, indexRedirection, getMyScope());
		}
		expression.expression.append(')');
	}
}
