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
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.Value;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.TemplateRestriction.Restriction_type;
import org.eclipse.titan.designer.AST.TTCN3.templates.ITTCN3Template;
import org.eclipse.titan.designer.AST.TTCN3.templates.TemplateInstance;
import org.eclipse.titan.designer.AST.TTCN3.values.Charstring_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Expression_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.UniversalCharstring_Value;
import org.eclipse.titan.designer.compiler.JavaGenData;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * @author Kristof Szabados
 * */
public final class EncodeExpression extends Expression_Value {
	private static final String OPERANDERROR1 = "Cannot determine the argument type of `encvalue' operation";
	private static final String OPERANDERROR2 = "The operand of the `encvalue' operation cannot be encoded";
	private static final String SECONDOPERANDERROR = "The second operand of the `encvalue' operation should be a universal charstring value";
	private static final String THIRDOPERANDERROR = "The third operand of the `encvalue' operation should be a universal charstring value";

	private final TemplateInstance templateInstance;
	private final Value encodingInfo;
	private final Value dynamicEncoding;

	public EncodeExpression(final TemplateInstance templateInstance, final Value encodingInfo, final Value dynamicEncoding) {
		this.templateInstance = templateInstance;
		this.encodingInfo = encodingInfo;
		this.dynamicEncoding = dynamicEncoding;

		if (templateInstance != null) {
			templateInstance.setFullNameParent(this);
		}
		if (encodingInfo != null) {
			encodingInfo.setFullNameParent(this);
		}
		if (dynamicEncoding != null) {
			dynamicEncoding.setFullNameParent(this);
		}
	}

	@Override
	/** {@inheritDoc} */
	public Operation_type getOperationType() {
		return Operation_type.ENCODE_OPERATION;
	}

	@Override
	/** {@inheritDoc} */
	public boolean checkExpressionSelfReference(final CompilationTimeStamp timestamp, final Assignment lhs) {
		if (templateInstance!= null && templateInstance.getTemplateBody().checkExpressionSelfReferenceTemplate(timestamp, lhs)) {
			return true;
		}

		return false;
	}

	@Override
	/** {@inheritDoc} */
	public String createStringRepresentation() {
		if (templateInstance == null) {
			return "<erroneous value>";
		}

		final StringBuilder builder = new StringBuilder();
		builder.append("encvalue(");
		builder.append(templateInstance.createStringRepresentation());
		if (encodingInfo != null) {
			builder.append(", ").append(encodingInfo.createStringRepresentation());
		}
		if (dynamicEncoding != null) {
			builder.append(", ").append(dynamicEncoding.createStringRepresentation());
		}
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
		if (encodingInfo != null) {
			encodingInfo.setMyScope(scope);
		}
		if (dynamicEncoding != null) {
			dynamicEncoding.setMyScope(scope);
		}
	}

	@Override
	/** {@inheritDoc} */
	public void setCodeSection(final CodeSectionType codeSection) {
		super.setCodeSection(codeSection);

		if (templateInstance != null) {
			templateInstance.setCodeSection(codeSection);
		}
		if (encodingInfo != null) {
			encodingInfo.setCodeSection(codeSection);
		}
		if (dynamicEncoding != null) {
			dynamicEncoding.setCodeSection(codeSection);
		}
	}

	@Override
	/** {@inheritDoc} */
	public StringBuilder getFullName(final INamedNode child) {
		final StringBuilder builder = super.getFullName(child);

		if (templateInstance == child) {
			return builder.append(OPERAND1);
		} else if (encodingInfo != null) {
			return builder.append(OPERAND2);
		} else if (dynamicEncoding != null) {
			return builder.append(OPERAND3);
		}

		return builder;
	}

	@Override
	/** {@inheritDoc} */
	public Type_type getExpressionReturntype(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue) {
		return Type_type.TYPE_BITSTRING;
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
	 *                the kind of the value to be expected.
	 * @param referenceChain
	 *                a reference chain to detect cyclic references.
	 * */
	private void checkExpressionOperands(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue,
			final IReferenceChain referenceChain) {
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
				templateInstance.getLocation().reportSemanticError(OPERANDERROR1);
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
				location.reportSemanticError(OPERANDERROR2);
				setIsErroneous(true);
			}
			break;
		default:
			break;
		}

		if (encodingInfo != null) {
			encodingInfo.setLoweridToReference(timestamp);
			final Type_type tempType = encodingInfo.getExpressionReturntype(timestamp, expectedValue);

			switch (tempType) {
			case TYPE_UCHARSTRING:
				encodingInfo.getValueRefdLast(timestamp, expectedValue, referenceChain);
				break;
			case TYPE_UNDEFINED:
				setIsErroneous(true);
				break;
			default:
				location.reportSemanticError(SECONDOPERANDERROR);
				setIsErroneous(true);
				break;
			}
		}

		if (dynamicEncoding != null) {
			dynamicEncoding.setLoweridToReference(timestamp);
			final Type_type tempType = dynamicEncoding.getExpressionReturntype(timestamp, expectedValue);

			switch (tempType) {
			case TYPE_UCHARSTRING: {
				final IValue lastValue = dynamicEncoding.getValueRefdLast(timestamp, expectedValue, referenceChain);
				if (!dynamicEncoding.isUnfoldable(timestamp)) {
					boolean errorFound = false;
					if (Value_type.UNIVERSALCHARSTRING_VALUE.equals(lastValue.getValuetype())) {
						errorFound = ((UniversalCharstring_Value)lastValue).checkDynamicEncodingString(timestamp, type);
					} else if (Value_type.CHARSTRING_VALUE.equals(lastValue.getValuetype())) {
						errorFound = ((Charstring_Value)lastValue).checkDynamicEncodingString(timestamp, type);
					}
					if (errorFound) {
						dynamicEncoding.getLocation().reportSemanticError(MessageFormat.format("The encoding string does not match any encodings of type `{0}''", type.getTypename()));
					}
				}
				break;
			}
			case TYPE_UNDEFINED:
				setIsErroneous(true);
				break;
			default:
				location.reportSemanticError(THIRDOPERANDERROR);
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

		if (templateInstance != null) {
			checkExpressionOperands(timestamp, expectedValue, referenceChain);
		}

		return lastValue;
	}

	@Override
	/** {@inheritDoc} */
	public void checkRecursions(final CompilationTimeStamp timestamp, final IReferenceChain referenceChain) {
		if (referenceChain.add(this) && templateInstance != null) {
			referenceChain.markState();
			templateInstance.checkRecursions(timestamp, referenceChain);
			referenceChain.previousState();
			if (encodingInfo != null) {
				referenceChain.markState();
				encodingInfo.checkRecursions(timestamp, referenceChain);
				referenceChain.previousState();
			}
			if (dynamicEncoding != null) {
				referenceChain.markState();
				dynamicEncoding.checkRecursions(timestamp, referenceChain);
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
		if (encodingInfo != null) {
			encodingInfo.updateSyntax(reparser, false);
			reparser.updateLocation(encodingInfo.getLocation());
		}
		if (dynamicEncoding != null) {
			dynamicEncoding.updateSyntax(reparser, false);
			reparser.updateLocation(dynamicEncoding.getLocation());
		}
	}

	@Override
	/** {@inheritDoc} */
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (templateInstance != null) {
			templateInstance.findReferences(referenceFinder, foundIdentifiers);
		}
		if (encodingInfo != null) {
			encodingInfo.findReferences(referenceFinder, foundIdentifiers);
		}
		if (dynamicEncoding != null) {
			dynamicEncoding.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	@Override
	/** {@inheritDoc} */
	protected boolean memberAccept(final ASTVisitor v) {
		if (templateInstance != null && !templateInstance.accept(v)) {
			return false;
		}
		if (encodingInfo != null && !encodingInfo.accept(v)) {
			return false;
		}
		if (dynamicEncoding != null && !dynamicEncoding.accept(v)) {
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
		if (encodingInfo != null) {
			encodingInfo.reArrangeInitCode(aData, source, usageModule);
		}
		if (dynamicEncoding != null) {
			dynamicEncoding.reArrangeInitCode(aData, source, usageModule);
		}
	}

	@Override
	/** {@inheritDoc} */
	public void generateCodeExpressionExpression(final JavaGenData aData, final ExpressionStruct expression) {
		aData.addBuiltinTypeImport("TitanOctetString");
		aData.addCommonLibraryImport("AdditionalFunctions");

		final boolean isValue = templateInstance.getTemplateBody().isValue(CompilationTimeStamp.getBaseTimestamp());

		final ExpressionStruct expression2 = new ExpressionStruct();
		if (isValue) {
			templateInstance.getTemplateBody().getValue().generateCodeExpressionMandatory(aData, expression2, true);
		} else {
			templateInstance.generateCode(aData, expression2, Restriction_type.TR_NONE);
		}

		final Scope scope = templateInstance.getTemplateBody().getMyScope();
		final IType governor = templateInstance.getTemplateBody().getMyGovernor();
		if (expression2.preamble.length() > 0) {
			expression.postamble.append(expression2.preamble);
		}

		final ExpressionStruct expression3 = new ExpressionStruct();
		//FIXME generate code for second and third parameter
		expression3.expression.append(MessageFormat.format("{0}_default_coding", governor.getGenNameDefaultCoding(aData, expression.expression, scope)));

		final String tempID = aData.getTemporaryVariableName();
		expression.preamble.append(MessageFormat.format("TitanOctetString {0} = new TitanOctetString();\n", tempID));
		expression.preamble.append(MessageFormat.format("{0}_encoder({1}{2}, {3}, {4});\n", governor.getGenNameCoder(aData, expression.expression, scope), expression2.expression, isValue?"":".valueOf()", tempID, expression3.expression));
		expression.expression.append(MessageFormat.format("AdditionalFunctions.oct2bit({0})", tempID));
		if (expression2.postamble.length() > 0) {
			expression.postamble.append(expression2.postamble);
		}
		if (expression3.postamble.length() > 0) {
			expression.postamble.append(expression3.postamble);
		}
	}
}
