/******************************************************************************
 * Copyright (c) 2000-2017 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.values.expressions;

import java.util.List;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.Value;
import org.eclipse.titan.designer.AST.IType.Type_type;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.values.Boolean_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Expression_Value;
import org.eclipse.titan.designer.compiler.JavaGenData;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * @author Kristof Szabados
 * */
public final class OrExpression extends Expression_Value {
	private static final String FIRSTOPERANDERROR = "The first operand of the `or' operation should be a boolean value";
	private static final String SECONDOPERANDERROR = "The second operand of the `or' operation should be a boolean value";

	private final Value value1;
	private final Value value2;

	public OrExpression(final Value value1, final Value value2) {
		this.value1 = value1;
		this.value2 = value2;

		if (value1 != null) {
			value1.setFullNameParent(this);
		}
		if (value2 != null) {
			value2.setFullNameParent(this);
		}
	}

	@Override
	/** {@inheritDoc} */
	public Operation_type getOperationType() {
		return Operation_type.OR_OPERATION;
	}

	@Override
	/** {@inheritDoc} */
	public String createStringRepresentation() {
		final StringBuilder builder = new StringBuilder();
		builder.append('(').append(value1.createStringRepresentation());
		builder.append(" or ");
		builder.append(value2.createStringRepresentation()).append(')');
		return builder.toString();
	}

	@Override
	/** {@inheritDoc} */
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		if (value1 != null) {
			value1.setMyScope(scope);
		}
		if (value2 != null) {
			value2.setMyScope(scope);
		}
	}

	@Override
	/** {@inheritDoc} */
	public StringBuilder getFullName(final INamedNode child) {
		final StringBuilder builder = super.getFullName(child);

		if (value1 == child) {
			return builder.append(OPERAND1);
		} else if (value2 == child) {
			return builder.append(OPERAND2);
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
		if (value1 == null || value2 == null || getIsErroneous(timestamp)) {
			return true;
		}

		final IValue last = value1.getValueRefdLast(timestamp, expectedValue, referenceChain);
		if (last.getIsErroneous(timestamp)) {
			return true;
		}

		return value1.isUnfoldable(timestamp, expectedValue, referenceChain)
				|| (!((Boolean_Value) last).getValue() && value2.isUnfoldable(timestamp, expectedValue, referenceChain));
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
		Type_type tempType1 = null;
		Type_type tempType2 = null;

		if (value1 != null) {
			value1.setLoweridToReference(timestamp);
			tempType1 = value1.getExpressionReturntype(timestamp, expectedValue);

			switch (tempType1) {
			case TYPE_BOOL:
				value1.getValueRefdLast(timestamp, expectedValue, referenceChain);
				break;
			case TYPE_UNDEFINED:
				setIsErroneous(true);
				break;
			default:
				value1.getLocation().reportSemanticError(FIRSTOPERANDERROR);
				setIsErroneous(true);
				break;
			}
		}

		if (value2 != null) {
			value2.setLoweridToReference(timestamp);
			tempType2 = value2.getExpressionReturntype(timestamp, expectedValue);

			switch (tempType2) {
			case TYPE_BOOL:
				value2.getValueRefdLast(timestamp, expectedValue, referenceChain);
				break;
			case TYPE_UNDEFINED:
				setIsErroneous(true);
				break;
			default:
				value2.getLocation().reportSemanticError(SECONDOPERANDERROR);
				setIsErroneous(true);
				break;
			}
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

		if (value1 == null || value2 == null) {
			return lastValue;
		}

		checkExpressionOperands(timestamp, expectedValue, referenceChain);

		if (getIsErroneous(timestamp) || isUnfoldable(timestamp, referenceChain)) {
			return lastValue;
		}

		final IValue last1 = value1.getValueRefdLast(timestamp, referenceChain);
		if (Value_type.BOOLEAN_VALUE.equals(last1.getValuetype())) {
			if (((Boolean_Value) last1).getValue()) {
				lastValue = new Boolean_Value(true);
				lastValue.copyGeneralProperties(this);
			} else {
				final Boolean_Value temp = (Boolean_Value) value2.getValueRefdLast(timestamp, referenceChain);
				lastValue = new Boolean_Value(temp.getValue());
				lastValue.copyGeneralProperties(this);
			}
		} else {
			// we must keep the left operand because of the
			// potential side effects
			// the right operand can only be eliminated if it is a
			// literal "false"
			final IValue last2 = value2.getValueRefdLast(timestamp, referenceChain);
			if (Value_type.BOOLEAN_VALUE.equals(last2.getValuetype()) && !((Boolean_Value) last2).getValue()) {
				final Boolean_Value temp = (Boolean_Value) value1.getValueRefdLast(timestamp, referenceChain);
				lastValue = new Boolean_Value(temp.getValue());
				lastValue.copyGeneralProperties(this);
			}
		}

		return lastValue;
	}

	@Override
	/** {@inheritDoc} */
	public void checkRecursions(final CompilationTimeStamp timestamp, final IReferenceChain referenceChain) {
		if (referenceChain.add(this)) {
			if (value1 != null) {
				referenceChain.markState();
				value1.checkRecursions(timestamp, referenceChain);
				referenceChain.previousState();
			}
			if (value2 != null) {
				referenceChain.markState();
				value2.checkRecursions(timestamp, referenceChain);
				referenceChain.previousState();
			}
		}
	}

	@Override
	/** {@inheritDoc} */
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}

		if (value1 != null) {
			value1.updateSyntax(reparser, false);
			reparser.updateLocation(value1.getLocation());
		}

		if (value2 != null) {
			value2.updateSyntax(reparser, false);
			reparser.updateLocation(value2.getLocation());
		}
	}

	@Override
	/** {@inheritDoc} */
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (value1 != null) {
			value1.findReferences(referenceFinder, foundIdentifiers);
		}
		if (value2 != null) {
			value2.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	@Override
	/** {@inheritDoc} */
	protected boolean memberAccept(final ASTVisitor v) {
		if (value1 != null && !value1.accept(v)) {
			return false;
		}
		if (value2 != null && !value2.accept(v)) {
			return false;
		}
		return true;
	}

	@Override
	/** {@inheritDoc} */
	public void generateCodeExpressionExpression(final JavaGenData aData, final ExpressionStruct expression) {
		if (value2.needsShortCircuit()) {
			final String tempId = aData.getTemporaryVariableName();
			expression.preamble.append("TitanBoolean ");
			aData.addBuiltinTypeImport( "TitanBoolean" );
			expression.preamble.append(tempId);
			expression.preamble.append(" = new TitanBoolean();\n");

			ExpressionStruct expression2 = new ExpressionStruct();
			expression2.expression.append(tempId);
			expression2.expression.append(".assign(");
			value1.generateCodeExpressionMandatory(aData, expression2);
			expression2.expression.append(")");
			expression2.mergeExpression(expression.preamble);

			expression.preamble.append("if (!TitanBoolean.getNative(");
			expression.preamble.append(tempId);
			expression.preamble.append(")) ");

			expression2 = new ExpressionStruct();
			expression2.expression.append(tempId);
			expression2.expression.append(".assign(");
			value2.generateCodeExpressionMandatory(aData, expression2);
			expression2.expression.append(")");
			expression2.mergeExpression(expression.preamble);

			expression.expression.append(tempId);
		} else {
			//TODO actually a bit more complicated
			value1.generateCodeExpressionMandatory(aData, expression);
			expression.expression.append( ".and( " );
			value2.generateCodeExpressionMandatory(aData, expression);
			expression.expression.append( " )" );
		}
	}

	@Override
	/** {@inheritDoc} */
	public StringBuilder generateCodeTmp(final JavaGenData aData, final StringBuilder source, final StringBuilder init) {
		final ExpressionStruct expression = new ExpressionStruct();

		//TODO actually only the mandatory part is needed
		generateCodeExpression(aData, expression);

		if(expression.preamble.length() > 0 || expression.postamble.length() > 0) {
			if(expression.preamble.length() > 0) {
				init.append(expression.preamble);
			}
			if(expression.postamble.length() > 0) {
				init.append(expression.postamble);
			}
			source.append(expression.expression);
		} else {
			source.append(expression.expression);
		}

		return source;
	}
}
