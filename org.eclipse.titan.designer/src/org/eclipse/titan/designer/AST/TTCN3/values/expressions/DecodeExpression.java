/******************************************************************************
 * Copyright (c) 2000-2017 Ericsson Telecom AB
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
import org.eclipse.titan.designer.AST.BridgingNamedNode;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.ISubReference;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.IType.Type_type;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.AST.ParameterisedSubReference;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.definitions.ActualParameterList;
import org.eclipse.titan.designer.AST.TTCN3.templates.ITTCN3Template;
import org.eclipse.titan.designer.AST.TTCN3.templates.ITTCN3Template.Template_type;
import org.eclipse.titan.designer.AST.TTCN3.templates.Referenced_Template;
import org.eclipse.titan.designer.AST.TTCN3.values.Expression_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Referenced_Value;
import org.eclipse.titan.designer.compiler.JavaGenData;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * @author Arpad Lovassy
 * @author Kristof Szabados
 * */
public final class DecodeExpression extends Expression_Value {
	private static final String OPERANDERROR1 = "The first operand of the `decvalue' operation should be a bitstring value";
	private static final String OPERANDERROR2 = "The second operand of the `decvalue' operation is unable to hold a decoded value";

	private final Reference reference1;
	private final Reference reference2;
	//FIXME missing support for third and forth parameter

	public DecodeExpression(final Reference reference1, final Reference reference2) {
		this.reference1 = reference1;
		this.reference2 = reference2;

		if (reference1 != null) {
			reference1.setFullNameParent(this);
		}
		if (reference2 != null) {
			reference2.setFullNameParent(this);
		}
	}

	@Override
	/** {@inheritDoc} */
	public Operation_type getOperationType() {
		return Operation_type.DECODE_OPERATION;
	}

	@Override
	/** {@inheritDoc} */
	public boolean checkExpressionSelfReference(final CompilationTimeStamp timestamp, final Assignment lhs) {
		if (lhs == reference1.getRefdAssignment(timestamp, false)) {
			return true;
		}
		if (lhs == reference2.getRefdAssignment(timestamp, false)) {
			return true;
		}

		return false;
	}

	@Override
	/** {@inheritDoc} */
	public String createStringRepresentation() {
		final StringBuilder builder = new StringBuilder("decvalue");
		builder.append('(').append(reference1.getDisplayName());
		builder.append(", ");
		builder.append(reference2.getDisplayName());
		builder.append(')');
		return builder.toString();
	}

	@Override
	/** {@inheritDoc} */
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		if (reference1 != null) {
			reference1.setMyScope(scope);
		}
		if (reference2 != null) {
			reference2.setMyScope(scope);
		}
	}

	@Override
	/** {@inheritDoc} */
	public void setCodeSection(final CodeSectionType codeSection) {
		super.setCodeSection(codeSection);

		if (reference1 != null) {
			reference1.setCodeSection(codeSection);
		}
		if (reference2 != null) {
			reference2.setCodeSection(codeSection);
		}
	}

	@Override
	/** {@inheritDoc} */
	public StringBuilder getFullName(final INamedNode child) {
		final StringBuilder builder = super.getFullName(child);

		if (reference1 == child || reference2 == child) {
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
		return true;
	}


	private void checkFirstExpressionOperand(final CompilationTimeStamp timestamp){
		final Assignment temporalAssignment = reference1.getRefdAssignment(timestamp, true);

		if (temporalAssignment == null) {
			setIsErroneous(true);
			return;
		}

		switch (temporalAssignment.getAssignmentType()) {
		case A_CONST:
		case A_EXT_CONST:
		case A_MODULEPAR:
		case A_TEMPLATE:
			reference1.getLocation().reportSemanticError(
					MessageFormat.format("Reference to `{0}'' cannot be used as the first operand of the `decvalue'' operation",
							temporalAssignment.getAssignmentName()));
			setIsErroneous(true);
			break;
		case A_VAR:
		case A_PAR_VAL:
		case A_PAR_VAL_IN:
		case A_PAR_VAL_OUT:
		case A_PAR_VAL_INOUT:
			break;
		case A_VAR_TEMPLATE:
		case A_PAR_TEMP_IN:
		case A_PAR_TEMP_OUT:
		case A_PAR_TEMP_INOUT: {
			final Referenced_Template template = new Referenced_Template(reference1);
			template.setMyScope(getMyScope());
			template.setFullNameParent(new BridgingNamedNode(this, ".<operand>"));
			final ITTCN3Template last = template.getTemplateReferencedLast(timestamp);
			if (!Template_type.SPECIFIC_VALUE.equals(last.getTemplatetype()) && last != template) {
				reference1.getLocation().reportSemanticError(
						MessageFormat.format("Specific value template was expected instead of `{0}''",
								last.getTemplateTypeName()));
				setIsErroneous(true);
				return;
			}
			break;
		}
		default:
			reference1.getLocation().reportSemanticError(
					MessageFormat.format("Reference to `{0}'' cannot be used as the first operand of the `decvalue' operation",
							temporalAssignment.getAssignmentName()));
			setIsErroneous(true);
			return;
		}

		final IType temporalType = temporalAssignment.getType(timestamp).getFieldType(timestamp, reference1, 1,
				Expected_Value_type.EXPECTED_DYNAMIC_VALUE, false);
		if (temporalType == null) {
			setIsErroneous(true);
			return;
		}
		if (temporalType.getTypeRefdLast(timestamp).getTypetype() != Type_type.TYPE_BITSTRING) {
			if (!isErroneous) {
				reference1.getLocation().reportSemanticError(OPERANDERROR1);
				setIsErroneous(true);
			}
			return;
		}
	}

	private void checkSecondExpressionOperand(final CompilationTimeStamp timestamp){
		final Assignment temporalAssignment = reference2.getRefdAssignment(timestamp, true);

		if (temporalAssignment == null) {
			setIsErroneous(true);
			return;
		}
		IType temporalType = temporalAssignment.getType(timestamp).getFieldType(timestamp, reference2, 1,
				Expected_Value_type.EXPECTED_DYNAMIC_VALUE, false);
		if (temporalType == null) {
			setIsErroneous(true);
			return;
		}
		temporalType = temporalType.getTypeRefdLast(timestamp);
		switch (temporalType.getTypetype()) {
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
			// if (!isErroneous) {
			reference2.getLocation().reportSemanticError(OPERANDERROR2);
			setIsErroneous(true);
			// }
			break;
		default:
			break;
		}
	}

	/**
	 * Checks the parameters of the expression and if they are valid in
	 * their position in the expression or not.
	 *
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle.
	 * @param referenceChain
	 *                a reference chain to detect cyclic references.
	 * */
	private void checkExpressionOperands(final CompilationTimeStamp timestamp, final IReferenceChain referenceChain) {
		if (reference1 == null || reference2 == null) {
			return;
		}
		checkFirstExpressionOperand(timestamp);
		checkSecondExpressionOperand(timestamp);
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

		checkExpressionOperands(timestamp, referenceChain);

		return lastValue;
	}

	/**
	 * Helper function checking if a provided reference is in a recursive
	 * reference chain or not.
	 *
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle.
	 * @param reference
	 *                the reference to check for recursion.
	 * @param referenceChain
	 *                the ReferenceChain used to detect circular references,
	 *                must not be null.
	 * */
	private void checkRecursionHelper(final CompilationTimeStamp timestamp, final Reference reference, final IReferenceChain referenceChain) {
		final Assignment assignment = reference.getRefdAssignment(timestamp, true);
		if (assignment == null) {
			setIsErroneous(true);
			return;
		}

		switch (assignment.getAssignmentType()) {
		case A_CONST:
		case A_EXT_CONST:
		case A_MODULEPAR:
		case A_VAR:
		case A_PAR_VAL:
		case A_PAR_VAL_IN:
		case A_PAR_VAL_OUT:
		case A_PAR_VAL_INOUT: {
			final Referenced_Value value = new Referenced_Value(reference);
			value.setMyScope(getMyScope());
			value.setFullNameParent(this);

			referenceChain.markState();
			value.checkRecursions(timestamp, referenceChain);
			referenceChain.previousState();
			break;
		}
		case A_TEMPLATE:
		case A_VAR_TEMPLATE:
		case A_PAR_TEMP_IN:
		case A_PAR_TEMP_OUT:
		case A_PAR_TEMP_INOUT: {
			final Referenced_Template template = new Referenced_Template(reference1);
			template.setMyScope(getMyScope());
			template.setFullNameParent(this);

			referenceChain.markState();
			template.checkRecursions(timestamp, referenceChain);
			referenceChain.previousState();
			break;
		}
		default:
			// remain silent, the error was already detected and
			// reported
			break;
		}
	}

	@Override
	/** {@inheritDoc} */
	public void checkRecursions(final CompilationTimeStamp timestamp, final IReferenceChain referenceChain) {
		if (referenceChain.add(this)) {
			checkRecursionHelper(timestamp, reference1, referenceChain);
			checkRecursionHelper(timestamp, reference2, referenceChain);
		}
	}

	@Override
	/** {@inheritDoc} */
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}

		if (reference1 != null) {
			reference1.updateSyntax(reparser, false);
			reparser.updateLocation(reference1.getLocation());
		}
		if (reference2 != null) {
			reference2.updateSyntax(reparser, false);
			reparser.updateLocation(reference2.getLocation());
		}
	}

	@Override
	/** {@inheritDoc} */
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (reference1 != null) {
			reference1.findReferences(referenceFinder, foundIdentifiers);
		}
		if (reference2 != null) {
			reference2.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	@Override
	/** {@inheritDoc} */
	protected boolean memberAccept(final ASTVisitor v) {
		if (reference1 != null && !reference1.accept(v)) {
			return false;
		}
		if (reference2 != null && !reference2.accept(v)) {
			return false;
		}
		return true;
	}

	@Override
	/** {@inheritDoc} */
	public void reArrangeInitCode(final JavaGenData aData, final StringBuilder source, final Module usageModule) {
		if (reference1 != null) {
			final List<ISubReference> subreferences = reference1.getSubreferences();
			if (subreferences != null && subreferences.size() > 0 && subreferences.get(0) instanceof ParameterisedSubReference) {
				final ActualParameterList actualParameterList = ((ParameterisedSubReference)subreferences.get(0)).getActualParameters();
				if (actualParameterList != null) {
					actualParameterList.reArrangeInitCode(aData, source, usageModule);
				}
			}
		}
		if (reference2 != null) {
			final List<ISubReference> subreferences = reference2.getSubreferences();
			if (subreferences != null && subreferences.size() > 0 && subreferences.get(0) instanceof ParameterisedSubReference) {
				final ActualParameterList actualParameterList = ((ParameterisedSubReference)subreferences.get(0)).getActualParameters();
				if (actualParameterList != null) {
					actualParameterList.reArrangeInitCode(aData, source, usageModule);
				}
			}
		}
	}

	@Override
	/** {@inheritDoc} */
	public void generateCodeExpressionExpression(final JavaGenData aData, final ExpressionStruct expression) {
		aData.addBuiltinTypeImport("TitanOctetString");
		aData.addCommonLibraryImport("TTCN_EncDec");
		aData.addCommonLibraryImport("AdditionalFunctions");

		final ExpressionStruct expression1 = new ExpressionStruct();
		final ExpressionStruct expression2 = new ExpressionStruct();

		reference1.generateCode(aData, expression1);
		reference2.generateCode(aData, expression2);
		final Assignment tempAssignment = reference2.getRefdAssignment(CompilationTimeStamp.getBaseTimestamp(), false);
		final IType type = tempAssignment.getType(CompilationTimeStamp.getBaseTimestamp());
		final IType fieldType = type.getFieldType(CompilationTimeStamp.getBaseTimestamp(), reference2, 1, Expected_Value_type.EXPECTED_DYNAMIC_VALUE, false);

		if (expression1.preamble.length() > 0) {
			expression.preamble.append(expression1.preamble);
		}
		if (expression2.preamble.length() > 0) {
			expression.preamble.append(expression2.preamble);
		}

		final Scope scope = reference2.getMyScope();
		final boolean isOptional = fieldType.fieldIsOptional(reference2.getSubreferences());

		final ExpressionStruct expression3 = new ExpressionStruct();
		//TODO add support for 5th parameter
		expression3.expression.append(MessageFormat.format("{0}_default_coding", fieldType.getGenNameDefaultCoding(aData, expression.expression, scope)));
		final String bufferID = aData.getTemporaryVariableName();
		final String returnValueID = aData.getTemporaryVariableName();
		// TOOD add handling for non-built-in encoding
		expression.preamble.append("TTCN_EncDec.set_error_behavior(TTCN_EncDec.error_type.ET_ALL, TTCN_EncDec.error_behavior_type.EB_WARNING);\n");
		expression.preamble.append("TTCN_EncDec.clear_error();\n");
		expression.preamble.append(MessageFormat.format("TitanOctetString {0} = new TitanOctetString(AdditionalFunctions.bit2oct({1}));\n", bufferID, expression1.expression));
		expression.preamble.append(MessageFormat.format("TitanInteger {0} = new TitanInteger({1}_decoder({2}, {3}{4}, {5}));\n", returnValueID, fieldType.getGenNameCoder(aData, expression.expression, scope), bufferID, expression2.expression, isOptional? ".get()":"", expression3.expression));
		expression.preamble.append(MessageFormat.format("if ({0}.operatorEquals(0)) '{'\n", returnValueID));
		expression.preamble.append(MessageFormat.format("{0} = AdditionalFunctions.oct2bit({1});\n", expression1.expression, bufferID));
		expression.preamble.append("}\n");
		// TOOD add handling for non-built-in encoding
		expression.preamble.append("TTCN_EncDec.set_error_behavior(TTCN_EncDec.error_type.ET_ALL, TTCN_EncDec.error_behavior_type.EB_DEFAULT);\n");
		expression.preamble.append("TTCN_EncDec.clear_error();\n");

		expression.expression.append(returnValueID);
		if (expression1.postamble.length() > 0) {
			expression.postamble.append(expression1.postamble);
		}
		if (expression2.postamble.length() > 0) {
			expression.postamble.append(expression2.postamble);
		}
		if (expression3.postamble.length() > 0) {
			expression.postamble.append(expression3.postamble);
		}
	}
}
