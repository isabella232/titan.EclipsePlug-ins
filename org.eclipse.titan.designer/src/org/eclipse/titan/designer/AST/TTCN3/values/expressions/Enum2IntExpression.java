/******************************************************************************
 * Copyright (c) 2000-2021 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.values.expressions;

import java.util.List;

import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.IType.Type_type;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.Value;
import org.eclipse.titan.designer.AST.ASN1.types.ASN1_Enumerated_Type;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.types.EnumItem;
import org.eclipse.titan.designer.AST.TTCN3.types.TTCN3_Enumerated_Type;
import org.eclipse.titan.designer.AST.TTCN3.values.Enumerated_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Expression_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Integer_Value;
import org.eclipse.titan.designer.compiler.JavaGenData;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * @author Kristof Szabados
 * */
public final class Enum2IntExpression extends Expression_Value {
	private static final String OPERANDERROR1 = "The operand of the `enum2int' operation should be an enumerated value";

	private final Value value;

	public Enum2IntExpression(final Value value) {
		this.value = value;

		if (value != null) {
			value.setFullNameParent(this);
		}
	}

	@Override
	/** {@inheritDoc} */
	public Operation_type getOperationType() {
		return Operation_type.ENUM2INT_OPERATION;
	}

	@Override
	/** {@inheritDoc} */
	public boolean checkExpressionSelfReference(final CompilationTimeStamp timestamp, final Assignment lhs) {
		return value != null && value.checkExpressionSelfReferenceValue(timestamp, lhs);
	}

	@Override
	/** {@inheritDoc} */
	public String createStringRepresentation() {
		final StringBuilder builder = new StringBuilder();
		builder.append("enum2int(").append(value.createStringRepresentation()).append(')');
		return builder.toString();
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
	public void setCodeSection(final CodeSectionType codeSection) {
		super.setCodeSection(codeSection);

		if (value != null) {
			value.setCodeSection(codeSection);
		}
	}

	@Override
	/** {@inheritDoc} */
	public StringBuilder getFullName(final INamedNode child) {
		final StringBuilder builder = super.getFullName(child);

		if (value == child) {
			return builder.append(OPERAND);
		}

		return builder;
	}

	@Override
	/** {@inheritDoc} */
	public Type_type getExpressionReturntype(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue) {
		return Type_type.TYPE_INTEGER;
	}

	@Override
	/** {@inheritDoc} */
	public boolean isUnfoldable(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue,
			final IReferenceChain referenceChain) {
		if (value == null) {
			return true;
		}

		return value.isUnfoldable(timestamp, expectedValue, referenceChain);

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
		if (value == null) {
			return;
		}

		value.setLoweridToReference(timestamp);
		final IType tempType = value.getExpressionGovernor(timestamp, expectedValue);
		if (tempType == null) {
			value.getLocation().reportSemanticError(
					"Please use a reference to an enumerated value as the first operand of operation `enum2int()'");
			setIsErroneous(true);
			return;
		}

		final IType last = tempType.getTypeRefdLast(timestamp);
		switch (last.getTypetype()) {
		case TYPE_ASN1_ENUMERATED:
		case TYPE_TTCN3_ENUMERATED: {
			final IValue lastValue = value.getValueRefdLast(timestamp, expectedValue, referenceChain);
			if (Value_type.OMIT_VALUE.equals(lastValue.getValuetype())) {
				value.getLocation().reportSemanticError("The operand of operation `enum2int' cannot be omit");
				setIsErroneous(true);
			}
			return;
		}
		case TYPE_UNDEFINED:
			setIsErroneous(true);
			break;
		default:
			if (!isErroneous) {
				value.getLocation().reportSemanticError(OPERANDERROR1);
				setIsErroneous(true);
			}
			return;
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

		if (value == null) {
			return lastValue;
		}

		checkExpressionOperands(timestamp, expectedValue, referenceChain);

		if (getIsErroneous(timestamp)) {
			return lastValue;
		}

		if (isUnfoldable(timestamp, referenceChain)) {
			return lastValue;
		}

		final IValue last = value.getValueRefdLast(timestamp, referenceChain);
		if (last == null || last.getIsErroneous(timestamp)) {
			setIsErroneous(true);
			return lastValue;
		}

		IType type = last.getExpressionGovernor(timestamp, expectedValue);
		type = type.getTypeRefdLast(timestamp);

		switch (type.getTypetype()) {
		case TYPE_ASN1_ENUMERATED: {
			final EnumItem item = ((ASN1_Enumerated_Type) type).getEnumItemWithName(((Enumerated_Value) last).getValue());
			lastValue = new Integer_Value(((Integer_Value) item.getValue()).getValue());
			lastValue.copyGeneralProperties(this);
			break;
		}
		case TYPE_TTCN3_ENUMERATED: {
			final EnumItem item = ((TTCN3_Enumerated_Type) type).getEnumItemWithName(((Enumerated_Value) last).getValue());
			lastValue = new Integer_Value(((Integer_Value) item.getValue()).getValue());
			lastValue.copyGeneralProperties(this);
			break;
		}
		default:
			setIsErroneous(true);
			break;
		}

		return lastValue;
	}

	@Override
	/** {@inheritDoc} */
	public void checkRecursions(final CompilationTimeStamp timestamp, final IReferenceChain referenceChain) {
		if (referenceChain.add(this) && value != null) {
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

		if (value != null) {
			value.updateSyntax(reparser, false);
			reparser.updateLocation(value.getLocation());
		}
	}

	@Override
	/** {@inheritDoc} */
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (value == null) {
			return;
		}

		value.findReferences(referenceFinder, foundIdentifiers);
	}

	@Override
	/** {@inheritDoc} */
	protected boolean memberAccept(final ASTVisitor v) {
		if (value != null && !value.accept(v)) {
			return false;
		}
		return true;
	}

	@Override
	/** {@inheritDoc} */
	public void reArrangeInitCode(final JavaGenData aData, final StringBuilder source, final Module usageModule) {
		if (value != null) {
			value.reArrangeInitCode(aData, source, usageModule);
		}
	}

	@Override
	/** {@inheritDoc} */
	public boolean canGenerateSingleExpression() {
		return value.canGenerateSingleExpression();
	}

	@Override
	/** {@inheritDoc} */
	public boolean returnsNative() {
		return true;
	}

	@Override
	/** {@inheritDoc} */
	public void generateCodeExpressionExpression(final JavaGenData aData, final ExpressionStruct expression) {
		IType governor = value.getMyGovernor();
		if (governor == null) {
			governor = value.getExpressionGovernor(CompilationTimeStamp.getBaseTimestamp(), Expected_Value_type.EXPECTED_TEMPLATE);
		}
		if (governor == null) {
			governor = myLastSetGovernor;
		}
		if (governor == null ) {
			ErrorReporter.INTERNAL_ERROR("FATAL ERROR while generating code for expression `" + getFullName() + "''");
			return;
		}

		final String name = governor.getGenNameValue(aData, expression.expression);

		expression.expression.append(name);
		expression.expression.append(".enum2int(");
		value.generateCodeExpressionMandatory(aData, expression, false);
		expression.expression.append(")");
	}
}
