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
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.IType.Type_type;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.ReferenceChain;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.Value;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.definitions.ActualParameterList;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Testcase;
import org.eclipse.titan.designer.AST.TTCN3.definitions.FormalParameterList;
import org.eclipse.titan.designer.AST.TTCN3.templates.ParsedActualParameters;
import org.eclipse.titan.designer.AST.TTCN3.types.Altstep_Type;
import org.eclipse.titan.designer.AST.TTCN3.values.Expression_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Testcase_Reference_Value;
import org.eclipse.titan.designer.compiler.JavaGenData;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * @author Kristof Szabados
 * */
public final class ActivateDereferedExpression extends Expression_Value {
	private static final String ALTSTEPEXPECTED = "A value of type altstep was expected in the argument of `derefers()'' instead of `{0}''";

	private static final String FULLNAMEPART1 = ".<reference>";
	private static final String FULLNAMEPART2 = ".<parameters>";
	private static final String OPERATIONNAME = "activate()";

	private final Value value;
	private final ParsedActualParameters actualParameterList;

	private ActualParameterList actualParameters;

	public ActivateDereferedExpression(final Value value, final ParsedActualParameters actualParameterList) {
		this.value = value;
		this.actualParameterList = actualParameterList;

		if (value != null) {
			value.setFullNameParent(this);
		}
		if (actualParameterList != null) {
			actualParameterList.setFullNameParent(this);
		}
	}

	@Override
	/** {@inheritDoc} */
	public Operation_type getOperationType() {
		return Operation_type.ACTIVATE_REFERENCED_OPERATION;
	}

	@Override
	/** {@inheritDoc} */
	public boolean checkExpressionSelfReference(final CompilationTimeStamp timestamp, final Assignment lhs) {
		if (value != null & value.checkExpressionSelfReferenceValue(timestamp, lhs)) {
			return true;
		}

		return false;
	}

	@Override
	/** {@inheritDoc} */
	public String createStringRepresentation() {
		final StringBuilder builder = new StringBuilder("activate(derefers(");
		if (value != null) {
			builder.append(value.createStringRepresentation());
		}
		builder.append(")(");
		// TODO implement more precise create_stringRepresentation
		builder.append("...");
		builder.append("))");

		return builder.toString();
	}

	@Override
	/** {@inheritDoc} */
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);

		if (value != null) {
			value.setMyScope(scope);
		}
		if (actualParameterList != null) {
			actualParameterList.setMyScope(scope);
		}
	}

	@Override
	/** {@inheritDoc} */
	public void setCodeSection(final CodeSectionType codeSection) {
		super.setCodeSection(codeSection);

		if (value != null) {
			value.setCodeSection(codeSection);
		}
		if (actualParameterList != null) {
			actualParameterList.setCodeSection(codeSection);
		}
	}

	@Override
	/** {@inheritDoc} */
	public StringBuilder getFullName(final INamedNode child) {
		final StringBuilder builder = super.getFullName(child);

		if (value == child) {
			return builder.append(FULLNAMEPART1);
		} else if (actualParameterList == child) {
			return builder.append(FULLNAMEPART2);
		}

		return builder;
	}

	@Override
	/** {@inheritDoc} */
	public Type_type getExpressionReturntype(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue) {
		return Type_type.TYPE_DEFAULT;
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
		value.setLoweridToReference(timestamp);
		IType type = value.getExpressionGovernor(timestamp, Expected_Value_type.EXPECTED_DYNAMIC_VALUE);
		if (type != null) {
			type = type.getTypeRefdLast(timestamp);
		}

		if (type == null || type.getIsErroneous(timestamp)) {
			setIsErroneous(true);
			return;
		}

		if (!Type_type.TYPE_ALTSTEP.equals(type.getTypetype())) {
			value.getLocation().reportSemanticError(MessageFormat.format(ALTSTEPEXPECTED, type.getTypename()));
			setIsErroneous(true);
			return;
		}

		actualParameters = new ActualParameterList();
		final FormalParameterList formalParameterList = ((Altstep_Type) type).getFormalParameters();
		if (formalParameterList.checkActualParameterList(timestamp, actualParameterList, actualParameters)) {
			setIsErroneous(true);
			return;
		}

		actualParameters.setFullNameParent(this);
		actualParameters.setMyScope(getMyScope());
		if (!formalParameterList.checkActivateArgument(timestamp, actualParameters, createStringRepresentation())) {
			setIsErroneous(true);
		}

		checkExpressionDynamicPart(expectedValue, OPERATIONNAME, true, true, false);
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

		if (value == null) {
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

		if (value != null) {
			value.updateSyntax(reparser, false);
			reparser.updateLocation(value.getLocation());
		}
		if (actualParameterList != null) {
			actualParameterList.updateSyntax(reparser, false);
			reparser.updateLocation(actualParameterList.getLocation());
		}
	}

	@Override
	/** {@inheritDoc} */
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (value != null) {
			value.findReferences(referenceFinder, foundIdentifiers);
		}
		if (actualParameterList != null) {
			actualParameterList.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	@Override
	/** {@inheritDoc} */
	protected boolean memberAccept(final ASTVisitor v) {
		if (value != null && !value.accept(v)) {
			return false;
		}
		if (actualParameterList != null && !actualParameterList.accept(v)) {
			return false;
		}
		return true;
	}

	@Override
	/** {@inheritDoc} */
	public boolean canGenerateSingleExpression() {
		return value.canGenerateSingleExpression() && actualParameters.hasSingleExpression();
	}

	@Override
	/** {@inheritDoc} */
	public StringBuilder generateSingleExpression(final JavaGenData aData) {
		aData.addBuiltinTypeImport("TitanDefault");

		final StringBuilder result = new StringBuilder();
		result.append("new TitanDefault(");
		final ExpressionStruct expression = new ExpressionStruct();
		generateCodeExpressionExpression(aData, expression);
		result.append(expression.expression);
		result.append(')');

		return result;
	}

	@Override
	/** {@inheritDoc} */
	public void generateCodeExpressionExpression(final JavaGenData aData, final ExpressionStruct expression) {
		final IReferenceChain chain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
		final IValue last = value.getValueRefdLast(CompilationTimeStamp.getBaseTimestamp(), chain);
		chain.release();

		if (last.getValuetype() == Value_type.ALTSTEP_REFERENCE_VALUE) {
			// the referred testcase is known
			final Def_Testcase testcase = ((Testcase_Reference_Value)last).getReferredTestcase();
			expression.expression.append(MessageFormat.format("{0}(", testcase.getGenNameFromScope(aData, expression.expression, myScope, "activate_")));
		} else {
			// the referred testcase is unknown
			value.generateCodeExpressionMandatory(aData, expression, true);
			expression.expression.append(".activate(");
		}

		actualParameters.generateCodeAlias(aData, expression);
		expression.expression.append(')');
	}
}
