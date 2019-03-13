/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.definitions;

import java.text.MessageFormat;

import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.Assignment.Assignment_type;
import org.eclipse.titan.designer.AST.GovernedSimple.CodeSectionType;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.Value;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.ExpressionStruct;
import org.eclipse.titan.designer.compiler.JavaGenData;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * Represents an actual parameter that has a reference as its actual value.
 *
 * @author Kristof Szabados
 * */
public final class Referenced_ActualParameter extends ActualParameter {

	private final Reference reference;

	public Referenced_ActualParameter(final Reference reference) {
		this.reference = reference;
	}

	@Override
	/** {@inheritDoc} */
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);

		if (reference != null) {
			reference.setMyScope(scope);
		}
	}

	@Override
	/** {@inheritDoc} */
	public boolean hasSingleExpression(final FormalParameter formalParameter) {
//		if(genRestrictionCheck != Restriction_type.TR_NONE) {
			// TODO needs t check post restriction check generation
//			return true;
//		}

		if (reference != null) {
			if (formalParameter != null && formalParameter.getAssignmentType() != Assignment_type.A_PAR_TIMER
					&& formalParameter.getAssignmentType() != Assignment_type.A_PAR_PORT) {
				boolean isTemplateParamater = false;
				if (formalParameter.getAssignmentType() == Assignment_type.A_PAR_TEMP_INOUT ||
						formalParameter.getAssignmentType() == Assignment_type.A_PAR_TEMP_OUT) {
					isTemplateParamater = true;
				}

				final Assignment assignment = reference.getRefdAssignment(CompilationTimeStamp.getBaseTimestamp(), false);
				final IType type = assignment.getType(CompilationTimeStamp.getBaseTimestamp());
				final IType fieldType = type.getFieldType(CompilationTimeStamp.getBaseTimestamp(), reference, 1, isTemplateParamater ? Expected_Value_type.EXPECTED_TEMPLATE : Expected_Value_type.EXPECTED_DYNAMIC_VALUE, false);
				final IType actualParType = fieldType.getTypeRefdLast(CompilationTimeStamp.getBaseTimestamp());
				final IType formalParType = formalParameter.getType(CompilationTimeStamp.getBaseTimestamp()).getTypeRefdLast(CompilationTimeStamp.getBaseTimestamp());
				//FIXME actually check for the need of conversion
				if (!actualParType.isIdentical(CompilationTimeStamp.getBaseTimestamp(), formalParType)) {
					return false;
				}
			}

			return reference.hasSingleExpression();
		}

		return false;
	}

	@Override
	/** {@inheritDoc} */
	public void setCodeSection(final CodeSectionType codeSection) {
		if (reference != null) {
			reference.setCodeSection(codeSection);
		}
	}

	public Reference getReference() {
		return reference;
	}

	@Override
	/** {@inheritDoc} */
	public void checkRecursions(final CompilationTimeStamp timestamp, final IReferenceChain referenceChain) {
		// nothing to be done here
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
	}

	@Override
	/** {@inheritDoc} */
	protected boolean memberAccept(final ASTVisitor v) {
		if (reference != null) {
			if (!reference.accept(v)) {
				return false;
			}
		}
		return true;
	}

	@Override
	/** {@inheritDoc} */
	public void generateCode( final JavaGenData aData, final ExpressionStruct expression, final FormalParameter formalParameter) {
		if (reference != null) {
			boolean needsConversion = false;
			IType formalParType = null;
			IType actualParType = null;
			if (formalParameter != null && formalParameter.getAssignmentType() != Assignment_type.A_PAR_TIMER
					&& formalParameter.getAssignmentType() != Assignment_type.A_PAR_PORT) {
				boolean isTemplateParamater = false;
				if (formalParameter.getAssignmentType() == Assignment_type.A_PAR_TEMP_INOUT ||
						formalParameter.getAssignmentType() == Assignment_type.A_PAR_TEMP_OUT) {
					isTemplateParamater = true;
				}

				final Assignment assignment = reference.getRefdAssignment(CompilationTimeStamp.getBaseTimestamp(), false);
				final IType type = assignment.getType(CompilationTimeStamp.getBaseTimestamp());
				final IType fieldType = type.getFieldType(CompilationTimeStamp.getBaseTimestamp(), reference, 1, isTemplateParamater ? Expected_Value_type.EXPECTED_TEMPLATE : Expected_Value_type.EXPECTED_DYNAMIC_VALUE, false);
				actualParType = fieldType.getTypeRefdLast(CompilationTimeStamp.getBaseTimestamp());
				formalParType = formalParameter.getType(CompilationTimeStamp.getBaseTimestamp()).getTypeRefdLast(CompilationTimeStamp.getBaseTimestamp());
				//FIXME actually check for the need of conversion
				if (!actualParType.isIdentical(CompilationTimeStamp.getBaseTimestamp(), formalParType)) {
					needsConversion = true;
				}
			}

			StringBuilder expressionExpression = new StringBuilder();
			final String tempId;
			// FIXME handle conversion case
			final ExpressionStruct valueExpression = new ExpressionStruct();
			reference.generateCode(aData, valueExpression);
			Value.generateCodeExpressionOptionalFieldReference(aData, valueExpression, reference);
			if(valueExpression.preamble.length() > 0) {
				expression.preamble.append(valueExpression.preamble);
			}
			if(valueExpression.postamble.length() == 0) {
				expressionExpression.append(valueExpression.expression);
			} else {
				// make sure the postambles of the parameters are executed before the
				// function call itself (needed if the value contains function calls
				// with lazy or fuzzy parameters)
				tempId = aData.getTemporaryVariableName();
				expression.preamble.append(MessageFormat.format(" {0}({1})", tempId, valueExpression.expression));
				expression.preamble.append(valueExpression.postamble);
				expressionExpression.append(tempId);
			}

			if (needsConversion) {
				final String tempId2 = aData.getTemporaryVariableName();
				final String formalParTypeName = formalParType.getGenNameValue(aData, expression.preamble);
				StringBuilder oldExpressionExpression = expressionExpression;
				StringBuilder convertedExpression = formalParType.generateConversion(aData, actualParType, expressionExpression);
				final String finalExpression = MessageFormat.format("final {0} {1} = {2};\n", formalParTypeName, tempId2, convertedExpression.toString());
				//TODO copy might be needed here
				expression.preamble.append(finalExpression);
				expression.expression.append(tempId2);

				expressionExpression = new StringBuilder(tempId2);
				convertedExpression = actualParType.generateConversion(aData, formalParType, expressionExpression);
				expression.postamble.append(MessageFormat.format("{0}.operator_assign({1});\n", oldExpressionExpression, convertedExpression));
			} else {
				//TODO copy might be needed here
				expression.expression.append(expressionExpression);
			}
		}
	}

	@Override
	/** {@inheritDoc} */
	public void reArrangeInitCode(final JavaGenData aData, final StringBuilder source, final Module usageModule) {
		ErrorReporter.INTERNAL_ERROR("Code generator reached erroneous actual parameter `" + getFullName() + "''");
	}
}
