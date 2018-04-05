/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.values.expressions;

import java.util.List;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.AST.IType.Type_type;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.Value;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.TemplateRestriction.Restriction_type;
import org.eclipse.titan.designer.AST.TTCN3.templates.ITTCN3Template;
import org.eclipse.titan.designer.AST.TTCN3.templates.TemplateInstance;
import org.eclipse.titan.designer.AST.TTCN3.values.CharstringExtractor;
import org.eclipse.titan.designer.AST.TTCN3.values.Charstring_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Expression_Value;
import org.eclipse.titan.designer.compiler.JavaGenData;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * Expression type for
 *   istemplatekind (in template any_type invalue, in charstring kind) return boolean
 * @author Arpad Lovassy
 */
public final class IsTemplateKindExpression extends Expression_Value {
	private static final String OPERAND1_ERROR1 = "Cannot determine the type of the 1st operand of the `istemplatekind' operation";
	private static final String OPERAND1_ERROR2 = "The 1st operand of the `istemplatekind' operation cannot be encoded";

	private static final String OPERAND2_ERROR1 = "The 2nd operand of the `istemplatekind' operation should be a charstring value";

	private final TemplateInstance templateInstance;
	private final Value value;

	public IsTemplateKindExpression(final TemplateInstance templateInstance, final Value value) {
		this.templateInstance = templateInstance;
		this.value = value;

		if (templateInstance != null) {
			templateInstance.setFullNameParent(this);
		}
		if (value != null) {
			value.setFullNameParent(this);
		}
	}

	@Override
	/** {@inheritDoc} */
	public Operation_type getOperationType() {
		return Operation_type.ISTEMPLATEKIND_OPERATION;
	}

	@Override
	/** {@inheritDoc} */
	public boolean checkExpressionSelfReference(final CompilationTimeStamp timestamp, final Assignment lhs) {
		if (templateInstance != null && templateInstance.getTemplateBody().checkExpressionSelfReferenceTemplate(timestamp, lhs)) {
			return true;
		}
		if (value != null & value.checkExpressionSelfReferenceValue(timestamp, lhs)) {
			return true;
		}

		return false;
	}

	@Override
	/** {@inheritDoc} */
	public String createStringRepresentation() {
		final StringBuilder builder = new StringBuilder("istemplatekind(");
		builder.append(templateInstance.createStringRepresentation());
		builder.append(", ");
		builder.append(value == null ? "null" : value.createStringRepresentation());
		builder.append(')');
		return builder.toString();
	}

	@Override
	/** {@inheritDoc} */
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		if (templateInstance != null) {
			templateInstance.setMyScope(scope);
		}
		if (value != null) {
			value.setMyScope(scope);
		}
	}

	@Override
	/** {@inheritDoc} */
	public void setCodeSection(final CodeSectionType codeSection) {
		super.setCodeSection(codeSection);

		if (templateInstance != null) {
			templateInstance.setCodeSection(codeSection);
		}
		if (value != null) {
			value.setCodeSection(codeSection);
		}
	}

	@Override
	/** {@inheritDoc} */
	public StringBuilder getFullName(final INamedNode child) {
		final StringBuilder builder = super.getFullName(child);

		if (templateInstance == child) {
			return builder.append(OPERAND1);
		} else if (value == child) {
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
	 */
	private void checkExpressionOperands( final CompilationTimeStamp timestamp,
			final Expected_Value_type expectedValue,
			final IReferenceChain referenceChain) {
		//check template1
		checkExpressionOperand1(timestamp, expectedValue, referenceChain);
		//check value2
		checkExpressionOperand2(timestamp, expectedValue, referenceChain);
	}

	/**
	 * Checks the 1st operand
	 * in template (value) any_type
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle.
	 * @param expectedValue
	 *                the kind of value expected.
	 * @param referenceChain
	 *                a reference chain to detect cyclic references.
	 */
	private void checkExpressionOperand1( final CompilationTimeStamp timestamp,
			final Expected_Value_type expectedValue,
			final IReferenceChain referenceChain ) {
		if (templateInstance == null) {
			setIsErroneous(true);
			return;
		}

		final Expected_Value_type internalExpectation = Expected_Value_type.EXPECTED_DYNAMIC_VALUE.equals(expectedValue) ? Expected_Value_type.EXPECTED_TEMPLATE
				: expectedValue;
		IType type = templateInstance.getExpressionGovernor(timestamp, internalExpectation);
		ITTCN3Template template = templateInstance.getTemplateBody();
		if (type == null) {
			template = template.setLoweridToReference(timestamp);
			type = template.getExpressionGovernor(timestamp, internalExpectation);
		}

		if (type == null) {
			if (!template.getIsErroneous(timestamp)) {
				templateInstance.getLocation().reportSemanticError(OPERAND1_ERROR1);
			}
			setIsErroneous(true);
			return;
		}

		IsValueExpression.checkExpressionTemplateInstance(timestamp, this, templateInstance, type, referenceChain, expectedValue);

		if (getIsErroneous(timestamp)) {
			return;
		}

		template.checkSpecificValue(timestamp, false);

		type = type.getTypeRefdLast(timestamp);
		switch (type.getTypetype()) {
		case TYPE_UNDEFINED:
		case TYPE_NULL:
		case TYPE_REFERENCED:
		case TYPE_VERDICT:
		case TYPE_PORT:
		case TYPE_COMPONENT:
		case TYPE_DEFAULT:
		case TYPE_SIGNATURE:
		case TYPE_FUNCTION:
		case TYPE_ALTSTEP:
		case TYPE_TESTCASE:
			if (!isErroneous) {
				location.reportSemanticError(OPERAND1_ERROR2);
				setIsErroneous(true);
			}
			break;
		default:
			break;
		}
	}

	/**
	 * Checks the 2nd operand
	 * in charstring (optional)
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle.
	 * @param expectedValue
	 *                the kind of value expected.
	 * @param referenceChain
	 *                a reference chain to detect cyclic references.
	 */
	private void checkExpressionOperand2( final CompilationTimeStamp timestamp,
			final Expected_Value_type expectedValue,
			final IReferenceChain referenceChain ) {
		if (value == null) {
			setIsErroneous(true);
			return;
		}

		value.setLoweridToReference(timestamp);
		final Type_type tempType = value.getExpressionReturntype(timestamp, expectedValue);

		switch (tempType) {
		case TYPE_CHARSTRING:
			final IValue last = value.getValueRefdLast(timestamp, expectedValue, referenceChain);
			if (!last.isUnfoldable(timestamp)) {
				final String originalString = ((Charstring_Value) last).getValue();
				final CharstringExtractor cs = new CharstringExtractor( originalString );
				if ( cs.isErrorneous() ) {
					value.getLocation().reportSemanticError( cs.getErrorMessage() );
					setIsErroneous(true);
				}
			}

			break;
		case TYPE_UNDEFINED:
			setIsErroneous(true);
			break;
		default:
			if (!isErroneous) {
				location.reportSemanticError(OPERAND2_ERROR1);
				setIsErroneous(true);
			}
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

		if (templateInstance != null) {
			checkExpressionOperands(timestamp, expectedValue, referenceChain);
		}

		//TODO could be optimized for templates

		return lastValue;
	}

	@Override
	/** {@inheritDoc} */
	public void checkRecursions(final CompilationTimeStamp timestamp, final IReferenceChain referenceChain) {
		if (referenceChain.add(this)) {
			if (templateInstance != null) {
				referenceChain.markState();
				templateInstance.checkRecursions(timestamp, referenceChain);
				referenceChain.previousState();
			}
			if (value != null) {
				referenceChain.markState();
				value.checkRecursions(timestamp, referenceChain);
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

		if (templateInstance != null) {
			templateInstance.updateSyntax(reparser, false);
			reparser.updateLocation(templateInstance.getLocation());
		}

		if (value != null) {
			value.updateSyntax(reparser, false);
			reparser.updateLocation(value.getLocation());
		}
	}

	@Override
	/** {@inheritDoc} */
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (templateInstance != null) {
			templateInstance.findReferences(referenceFinder, foundIdentifiers);
		}
		if (value != null) {
			value.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	@Override
	/** {@inheritDoc} */
	protected boolean memberAccept(final ASTVisitor v) {
		if (templateInstance != null && !templateInstance.accept(v)) {
			return false;
		}
		if (value != null && !value.accept(v)) {
			return false;
		}
		return true;
	}

	@Override
	/** {@inheritDoc} */
	public void reArrangeInitCode(final JavaGenData aData, final StringBuilder source, final Module usageModule) {
		if (templateInstance != null) {
			templateInstance.reArrangeInitCode(aData, source, usageModule);
		}
		if (value != null) {
			value.reArrangeInitCode(aData, source, usageModule);
		}
	}

	@Override
	/** {@inheritDoc} */
	public boolean canGenerateSingleExpression() {
		return value.canGenerateSingleExpression() && templateInstance.hasSingleExpression();
	}

	@Override
	/** {@inheritDoc} */
	public StringBuilder generateCodeInit(final JavaGenData aData, final StringBuilder source, final String name) {
		final ExpressionStruct expression = new ExpressionStruct();
		expression.expression.append(name);
		expression.expression.append(".assign(");
		generateCodeExpressionExpression(aData, expression);
		expression.expression.append(")");

		expression.mergeExpression(source);

		return source;
	}

	@Override
	/** {@inheritDoc} */
	public void generateCodeExpressionExpression(final JavaGenData aData, final ExpressionStruct expression) {
		//TODO actually a bit more complicated
		templateInstance.generateCode(aData, expression, Restriction_type.TR_NONE);
		expression.expression.append( ".get_istemplate_kind( " );
		value.generateCodeExpression(aData, expression, false);
		expression.expression.append( ')' );
	}
}
