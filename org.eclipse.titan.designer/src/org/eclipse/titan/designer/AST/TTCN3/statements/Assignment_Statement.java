/******************************************************************************
 * Copyright (c) 2000-2017 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.statements;

import java.text.MessageFormat;
import java.util.List;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.GovernedSimple.CodeSectionType;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.ISubReference;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.IType.Type_type;
import org.eclipse.titan.designer.AST.IType.ValueCheckingOptions;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.IValue.Value_type;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.ReferenceChain;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.TemporalReference;
import org.eclipse.titan.designer.AST.ASN1.types.ASN1_Sequence_Type;
import org.eclipse.titan.designer.AST.ASN1.types.ASN1_Set_Type;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.TemplateRestriction;
import org.eclipse.titan.designer.AST.TTCN3.TemplateRestriction.Restriction_type;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Const;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_ExternalConst;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Function;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_ModulePar;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_ModulePar_Template;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Template;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Var;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Var_Template;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Definition;
import org.eclipse.titan.designer.AST.TTCN3.definitions.FormalParameter;
import org.eclipse.titan.designer.AST.TTCN3.templates.ITTCN3Template;
import org.eclipse.titan.designer.AST.TTCN3.templates.ITTCN3Template.Template_type;
import org.eclipse.titan.designer.AST.TTCN3.templates.TTCN3Template;
import org.eclipse.titan.designer.AST.TTCN3.templates.ValueList_Template;
import org.eclipse.titan.designer.AST.TTCN3.types.CompField;
import org.eclipse.titan.designer.AST.TTCN3.types.TTCN3_Sequence_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.TTCN3_Set_Type;
import org.eclipse.titan.designer.AST.TTCN3.values.Bitstring_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Charstring_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Hexstring_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Octetstring_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Referenced_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.UniversalCharstring_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.ExpressionStruct;
import org.eclipse.titan.designer.compiler.JavaGenData;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReparseUtilities;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * @author Kristof Szabados
 * */
public final class Assignment_Statement extends Statement {
	private static final String FULLNAMEPART = ".assignment";
	private static final String TEMPLATEASSIGNMENTTOVALUE = "A template body with matching symbols cannot be assigned to a variable";
	private static final String VARIABLEREFERENCEEXPECTED = "Reference to a variable or template variable was expected instead of `{0}''";
	private static final String OMITTOMANDATORYASSIGNMENT1 = "Omit value can only be assigned to an optional field of a record or set value";
	private static final String OMITTOMANDATORYASSIGNMENT2 = "Assignment of `omit'' to mandatory field `{0}'' of type `{1}''";
	private static final String STATEMENT_NAME = "assignment";

	private final Reference reference;
	private final TTCN3Template template;

	private boolean selfReference = false;
	TemplateRestriction.Restriction_type templateRestriction = Restriction_type.TR_NONE;
	private boolean generateRestrictionCheck = false;

	public Assignment_Statement(final Reference reference, final TTCN3Template template) {
		this.reference = reference;
		this.template = template;

		if (reference != null) {
			reference.setFullNameParent(this);
		}
		if (template != null) {
			template.setFullNameParent(this);
		}
	}

	@Override
	/** {@inheritDoc} */
	public Statement_type getType() {
		return Statement_type.S_ASSIGNMENT;
	}

	@Override
	/** {@inheritDoc} */
	public String getStatementName() {
		return STATEMENT_NAME;
	}

	@Override
	/** {@inheritDoc} */
	public StringBuilder getFullName(final INamedNode child) {
		final StringBuilder builder = super.getFullName(child);

		if (reference == child) {
			return builder.append(FULLNAMEPART);
		} else if (template == child) {
			return builder.append(FULLNAMEPART);
		}

		return builder;
	}

	@Override
	/** {@inheritDoc} */
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		if (reference != null) {
			reference.setMyScope(scope);
		}
		if (template != null) {
			template.setMyScope(scope);
		}
	}

	@Override
	/** {@inheritDoc} */
	public void setCodeSection(final CodeSectionType codeSection) {
		if (template != null) {
			template.setCodeSection(codeSection);
		}
	}

	@Override
	/** {@inheritDoc} */
	public void check(final CompilationTimeStamp timestamp) {
		if (lastTimeChecked != null && !lastTimeChecked.isLess(timestamp)) {
			return;
		}

		lastTimeChecked = timestamp;
		isErroneous = false;
		selfReference = false;
		templateRestriction = Restriction_type.TR_NONE;
		generateRestrictionCheck = false;

		if (reference == null) {
			return;
		}

		reference.setUsedOnLeftHandSide();
		final Assignment assignment = reference.getRefdAssignment(timestamp, true);
		if (assignment == null || assignment.getIsErroneous()) {
			isErroneous = true;
			return;
		}

		if (template == null) {
			return;
		}

		switch (assignment.getAssignmentType()) {
		case A_PAR_VAL_IN:
			((FormalParameter) assignment).useAsLValue(reference);
			if (template.isValue(timestamp)) { //TODO: isValue should be checked within the previous line! This is double check!
				final IValue temporalValue = template.getValue();
				checkVarAssignment(timestamp, assignment, temporalValue);
				template.setMyGovernor(temporalValue.getMyGovernor());
				break;
			} else if( Template_type.VALUE_LIST.equals(template.getTemplatetype())
					&& ((ValueList_Template) template).getNofTemplates() == 1) {
				//TODO: convert (x) to x to compilation!
				break;
			} else {
				template.getLocation().reportSemanticError(TEMPLATEASSIGNMENTTOVALUE);
				template.setIsErroneous(true);
				return;
			}
		case A_PAR_VAL_OUT:
		case A_PAR_VAL_INOUT:
		case A_PAR_VAL:
			((FormalParameter) assignment).setWritten();
			if (template.isValue(timestamp)) { //TODO: isValue should be checked within the previous line! This is double check!
				final IValue temporalValue = template.getValue();
				checkVarAssignment(timestamp, assignment, temporalValue);
				template.setMyGovernor(temporalValue.getMyGovernor());
				break;
			} else if( Template_type.VALUE_LIST.equals(template.getTemplatetype())
					&& ((ValueList_Template) template).getNofTemplates() == 1) {
				//TODO: convert (x) to x to compilation!
				break;
			} else {
				template.getLocation().reportSemanticError(TEMPLATEASSIGNMENTTOVALUE);
				template.setIsErroneous(true);
				return;
			}
			//break
		case A_VAR:
			((Def_Var) assignment).setWritten();
			if (template.getIsErroneous(timestamp) ) {
				return;
			}
			final IValue temporalValue = template.getValue();
			if (temporalValue != null) {
				checkVarAssignment(timestamp, assignment, temporalValue);
				template.setMyGovernor(temporalValue.getMyGovernor());
				break;
			} else if ( Template_type.VALUE_LIST.equals(template.getTemplatetype())
					&& ((ValueList_Template) template).getNofTemplates() == 1) {
				break;
			} else {
				template.getLocation().reportSemanticError(TEMPLATEASSIGNMENTTOVALUE);
				template.setIsErroneous(true);
				return;
			}
		case A_PAR_TEMP_IN:
			((FormalParameter) assignment).useAsLValue(reference);
			checkTemplateAssignment(timestamp, assignment,Expected_Value_type.EXPECTED_TEMPLATE,null);
			break;
		case A_PAR_TEMP_OUT:
		case A_PAR_TEMP_INOUT:
			((FormalParameter) assignment).setWritten();
			checkTemplateAssignment(timestamp, assignment,Expected_Value_type.EXPECTED_TEMPLATE,null);
			break;
		case A_VAR_TEMPLATE:
			((Def_Var_Template) assignment).setWritten();
			checkTemplateAssignment(timestamp, assignment,Expected_Value_type.EXPECTED_TEMPLATE,null);
			break;
		default:
			reference.getLocation().reportSemanticError(MessageFormat.format(VARIABLEREFERENCEEXPECTED, assignment.getAssignmentName()));
			reference.setIsErroneous(true);
			isErroneous = true;
		}
	}

	private void checkVarAssignment(final CompilationTimeStamp timestamp, final Assignment assignment, final IValue value) {
		final IType varType = getType(timestamp, assignment);

		if (varType == null || value == null) {
			isErroneous = true;
			return;
		}

		final IType type = varType.getFieldType(timestamp, reference, 1, Expected_Value_type.EXPECTED_DYNAMIC_VALUE, false);
		if (type == null) {
			isErroneous = true;
			return;
		}

		value.setMyGovernor(type);
		IValue lastValue = type.checkThisValueRef(timestamp, value);

		final IReferenceChain referenceChain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
		lastValue = lastValue.getValueRefdLast(timestamp, referenceChain);
		referenceChain.release();

		if (Value_type.OMIT_VALUE.equals(lastValue.getValuetype())) {
			final ISubReference lastReference = reference.removeLastSubReference();

			if (lastReference == null || lastReference.getId() == null) {
				value.getLocation().reportSemanticError(OMITTOMANDATORYASSIGNMENT1);
				isErroneous = true;
				reference.addSubReference(lastReference);
				return;
			}
			final Identifier lastField = lastReference.getId();

			final List<ISubReference> baseReference = reference.getSubreferences(0, reference.getSubreferences().size() - 1);
			reference.addSubReference(lastReference);

			final Reference newReference = new TemporalReference(null, baseReference);
			newReference.clearStringElementReferencing();
			IType baseType = varType.getFieldType(timestamp, newReference, 1, Expected_Value_type.EXPECTED_DYNAMIC_VALUE, false);
			if (baseType == null) {
				isErroneous = true;
				return;
			}

			baseType = baseType.getTypeRefdLast(timestamp);
			if (baseType.getIsErroneous(timestamp)) {
				isErroneous = true;
				return;
			}

			CompField componentField;
			switch (baseType.getTypetype()) {
			case TYPE_TTCN3_SEQUENCE:
				componentField = ((TTCN3_Sequence_Type) baseType).getComponentByName(lastField.getName());
				if (componentField != null && !componentField.isOptional()) {
					value.getLocation().reportSemanticError(
							MessageFormat.format(OMITTOMANDATORYASSIGNMENT2, lastField.getDisplayName(),
									baseType.getTypename()));
					value.setIsErroneous(true);
				}
				break;
			case TYPE_ASN1_SEQUENCE:
				componentField = ((ASN1_Sequence_Type) baseType).getComponentByName(lastField);
				if (componentField != null && !componentField.isOptional()) {
					value.getLocation().reportSemanticError(
							MessageFormat.format(OMITTOMANDATORYASSIGNMENT2, lastField.getDisplayName(),
									baseType.getTypename()));
					value.setIsErroneous(true);
				}
				break;
			case TYPE_TTCN3_SET:
				componentField = ((TTCN3_Set_Type) baseType).getComponentByName(lastField.getName());
				if (componentField != null && !componentField.isOptional()) {
					value.getLocation().reportSemanticError(
							MessageFormat.format(OMITTOMANDATORYASSIGNMENT2, lastField.getDisplayName(),
									baseType.getTypename()));
					value.setIsErroneous(true);
				}
				break;
			case TYPE_ASN1_SET:
				componentField = ((ASN1_Set_Type) baseType).getComponentByName(lastField);
				if (componentField != null && !componentField.isOptional()) {
					value.getLocation().reportSemanticError(
							MessageFormat.format(OMITTOMANDATORYASSIGNMENT2, lastField.getDisplayName(),
									baseType.getTypename()));
					value.setIsErroneous(true);
				}
				break;
			default:
				value.getLocation().reportSemanticError(OMITTOMANDATORYASSIGNMENT1);//TODO:check this!!!
				value.setIsErroneous(true);
				isErroneous = true;
				break;
			}
		} else {
			final boolean isStringElement = reference.refersToStringElement();
			selfReference = type.checkThisValue(timestamp, value, assignment, new ValueCheckingOptions(Expected_Value_type.EXPECTED_DYNAMIC_VALUE, true, false,
					!isStringElement, false, isStringElement));

			if (isStringElement) {
				// The length of the right hand side should be 1
				final IType lastType = type.getTypeRefdLast(timestamp);
				int stringLength = 1;
				switch (lastType.getTypetype()) {
				case TYPE_BITSTRING:
				case TYPE_BITSTRING_A:
					if (!Value_type.BITSTRING_VALUE.equals(lastValue.getValuetype())) {
						return;
					}

					stringLength = ((Bitstring_Value) lastValue).getValueLength();
					break;
				case TYPE_HEXSTRING:
					if (!Value_type.HEXSTRING_VALUE.equals(lastValue.getValuetype())) {
						lastValue = null;
					} else {
						stringLength = ((Hexstring_Value) lastValue).getValueLength();
					}
					break;
				case TYPE_OCTETSTRING:
					if (!Value_type.OCTETSTRING_VALUE.equals(lastValue.getValuetype())) {
						return;
					}

					stringLength = ((Octetstring_Value) lastValue).getValueLength();
					break;
				case TYPE_CHARSTRING:
				case TYPE_NUMERICSTRING:
				case TYPE_PRINTABLESTRING:
				case TYPE_IA5STRING:
				case TYPE_VISIBLESTRING:
				case TYPE_UTCTIME:
				case TYPE_GENERALIZEDTIME:
					if (!Value_type.CHARSTRING_VALUE.equals(lastValue.getValuetype())) {
						return;
					}

					stringLength = ((Charstring_Value) lastValue).getValueLength();
					break;
				case TYPE_UCHARSTRING:
				case TYPE_UTF8STRING:
				case TYPE_TELETEXSTRING:
				case TYPE_VIDEOTEXSTRING:
				case TYPE_GRAPHICSTRING:
				case TYPE_GENERALSTRING:
				case TYPE_UNIVERSALSTRING:
				case TYPE_BMPSTRING:
				case TYPE_OBJECTDESCRIPTOR:
					if (Value_type.UNIVERSALCHARSTRING_VALUE.equals(lastValue.getValuetype())) {
						stringLength = ((UniversalCharstring_Value) lastValue).getValueLength();
					} else if (Value_type.CHARSTRING_VALUE.equals(lastValue.getValuetype())) {
						stringLength = ((Charstring_Value) lastValue).getValueLength();
					} else {
						return;
					}
					break;
				default:
					lastValue = null;
					return;
				}

				if (stringLength != 1) {
					final String message = MessageFormat
							.format("The length of the string to be assigned to a string element of type `{0}'' should be 1 instead of {1}",
									type.getTypename(), stringLength);
					value.getLocation().reportSemanticError(message);
					value.setIsErroneous(true);
				}
			}
		}
	}

	private void checkTemplateAssignment(final CompilationTimeStamp timestamp, final Assignment assignment,
			final Expected_Value_type expectedValue, final IReferenceChain referenceChain) {
		IType type = getType(timestamp, assignment);

		if (type == null) {
			isErroneous = true;
			return;
		}

		type.check(timestamp); //temp

		type = type.getFieldType(timestamp, reference, 1, expectedValue, false);
		if (type == null) {
			isErroneous = true;
			return;
		}

		template.setMyGovernor(type);
		final ITTCN3Template temporalTemplate = type.checkThisTemplateRef(timestamp, template, expectedValue,referenceChain);
		selfReference = temporalTemplate.checkThisTemplateGeneric(timestamp, type, true, true, true, true, false, assignment);
		checkTemplateRestriction(timestamp);

		if (reference.refersToStringElement()) {
			if (!template.isValue(timestamp)) {
				template.getLocation().reportSemanticError(TEMPLATEASSIGNMENTTOVALUE);
				template.setIsErroneous(true);
				//isErroneous = true; //????
				return;
			}
		}
	}

	/**
	 * Checks the template restriction on the assignment referenced on the left hand side.
	 *
	 * @param timestamp the time stamp of the actual semantic check cycle.
	 */
	private void checkTemplateRestriction(final CompilationTimeStamp timestamp) {
		final Assignment ass = reference.getRefdAssignment(timestamp, true);
		if (ass == null) {
			return;
		}

		switch(ass.getAssignmentType()) {
		case A_VAR_TEMPLATE:
			templateRestriction =  ((Def_Var_Template) ass).getTemplateRestriction();
			break;
		case A_PAR_TEMP_IN:
		case A_PAR_TEMP_OUT:
		case A_PAR_TEMP_INOUT:
			templateRestriction = ((FormalParameter) ass).getTemplateRestriction();
			break;
		default:
			templateRestriction = TemplateRestriction.Restriction_type.TR_NONE;
			break;
		}

		templateRestriction = TemplateRestriction.getSubRestriction(templateRestriction, timestamp, reference);
		generateRestrictionCheck = TemplateRestriction.check(timestamp, (Definition)ass, template, reference);
	}

	/**
	 * Calculates the type of this assignment.
	 *
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 *
	 * @return the type of the assignment if it has one, otherwise null
	 * */
	private IType getType(final CompilationTimeStamp timestamp, final Assignment assignment) {
		switch (assignment.getAssignmentType()) {
		case A_CONST:
			return ((Def_Const) assignment).getType(timestamp);
		case A_EXT_CONST:
			return ((Def_ExternalConst) assignment).getType(timestamp);
		case A_VAR:
			return ((Def_Var) assignment).getType(timestamp);
		case A_VAR_TEMPLATE:
			return ((Def_Var_Template) assignment).getType(timestamp);
		case A_TEMPLATE:
			return ((Def_Template) assignment).getType(timestamp);
		case A_MODULEPAR:
			return ((Def_ModulePar) assignment).getType(timestamp);
		case A_MODULEPAR_TEMPLATE:
			return ((Def_ModulePar_Template) assignment).getType(timestamp);
		case A_EXT_FUNCTION_RVAL:
		case A_EXT_FUNCTION_RTEMP:
			return ((Def_ExternalConst) assignment).getType(timestamp);
		case A_FUNCTION_RVAL:
		case A_FUNCTION_RTEMP:
			return ((Def_Function) assignment).getType(timestamp);
		case A_PAR_VAL:
		case A_PAR_VAL_IN:
		case A_PAR_VAL_OUT:
		case A_PAR_VAL_INOUT:
		case A_PAR_TEMP_IN:
		case A_PAR_TEMP_OUT:
		case A_PAR_TEMP_INOUT:
		case A_PAR_PORT:
		case A_PAR_TIMER:
			return ((FormalParameter) assignment).getType(timestamp);
		default:
			return null;
		}
	}

	@Override
	/** {@inheritDoc} */
	public List<Integer> getPossibleExtensionStarterTokens() {
		return ReparseUtilities.getAllValidTokenTypes();
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

		if (template != null) {
			template.updateSyntax(reparser, false);
			reparser.updateLocation(template.getLocation());
		}
	}

	@Override
	/** {@inheritDoc} */
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (reference != null) {
			reference.findReferences(referenceFinder, foundIdentifiers);
		}
		if (template != null) {
			template.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	@Override
	/** {@inheritDoc} */
	protected boolean memberAccept(final ASTVisitor v) {
		if (reference != null && !reference.accept(v)) {
			return false;
		}
		if (template != null && !template.accept(v)) {
			return false;
		}
		return true;
	}

	public Reference getReference() {
		return reference;
	}

	public TTCN3Template getTemplate() {
		return template;
	}

	@Override
	/** {@inheritDoc} */
	public void generateCode( final JavaGenData aData, final StringBuilder source ) {
		if (reference == null || template == null) {
			return;
		}

		final boolean rhsCopied = selfReference;
		//TODO this is actually much more complicated
		final Assignment assignment = reference.getRefdAssignment(CompilationTimeStamp.getBaseTimestamp(), true);
		if ( assignment == null ) {
			//TODO: handle null
			return;
		}
		boolean isValue;
		switch (assignment.getAssignmentType()) {
		case A_PAR_VAL_IN:
		case A_PAR_VAL_OUT:
		case A_PAR_VAL_INOUT:
		case A_PAR_VAL:
		case A_VAR:
			isValue = true;
			break;
		default:
			isValue = false;
			break;
		}

		boolean isOptional = false;
		if (assignment.getType(CompilationTimeStamp.getBaseTimestamp()).fieldIsOptional(reference.getSubreferences())) {
			isOptional = true;
		}

		if (isValue) {
			final String rhsCopy = aData.getTemporaryVariableName();
			String rhsRef = rhsCopy;
			if(rhsCopied) {
				source.append("{\n");
				if (isOptional) {
					source.append(MessageFormat.format("Optional<{0}> {1} = new Optional<{0}>({0}.class);\n", template.getMyGovernor().getGenNameValue(aData, source, myScope), rhsCopy));
					rhsRef += ".get()";
				} else if(!reference.hasSingleExpression()) {
					source.append(MessageFormat.format("{0} {1} = new {0}();\n", template.getMyGovernor().getGenNameValue(aData, source, myScope), rhsCopy));
				}
			}

			final IValue value = template.getValue();
			// TODO handle needs_conv
			if (reference.getSubreferences().size() > 1) {
				if(value.canGenerateSingleExpression()) {
					final ExpressionStruct expression = new ExpressionStruct();
					reference.generateCode(aData, expression);
					source.append(expression.preamble);

					String temp;
					IType type = getType(CompilationTimeStamp.getBaseTimestamp(), assignment);
					type = type.getFieldType(CompilationTimeStamp.getBaseTimestamp(), reference, 1, Expected_Value_type.EXPECTED_TEMPLATE, false);
					
					//if (value.getValuetype() != Value_type.OMIT_VALUE && (isOptional || type.getTypetypeTtcn3() != value.getExpressionReturntype(CompilationTimeStamp.getBaseTimestamp(), Expected_Value_type.EXPECTED_TEMPLATE))) {
					if (value.getValuetype() != Value_type.OMIT_VALUE && value.getValuetype() != Value_type.REFERENCED_VALUE && (isOptional || type.getTypetypeTtcn3() != value.getExpressionReturntype(CompilationTimeStamp.getBaseTimestamp(), Expected_Value_type.EXPECTED_TEMPLATE))) {
						temp = MessageFormat.format("new {0}({1})", value.getMyGovernor().getGenNameValue(aData, source, myScope), value.generateSingleExpression(aData));
					} else {
						temp = value.generateSingleExpression(aData).toString();
					}

					if (rhsCopied) {
						source.append(MessageFormat.format("{0}.assign({1});\n", rhsCopy, temp));
						expression.expression.append(MessageFormat.format(".assign({0});\n", rhsCopy));
					} else {
						expression.expression.append(MessageFormat.format(".assign({0});\n", temp));
					}

					source.append(expression.expression);
					source.append(expression.postamble);
				} else {
					final String tempID = aData.getTemporaryVariableName();
					final String typeGenname = value.getMyGovernor().getGenNameValue(aData, source, myScope);
					final ExpressionStruct leftExpression = new ExpressionStruct();
					reference.generateCode(aData, leftExpression);

					if (rhsCopied) {
						//TODO handle needs conversion case
						value.generateCodeInit(aData, source, rhsRef);
					} else if (isOptional) {
						leftExpression.expression.append(".get()");
					}

					source.append("{\n");
					source.append(leftExpression.preamble);
					if (reference.refersToStringElement()) {
						//LHS is a string element
						aData.addBuiltinTypeImport(typeGenname + "_Element");
						source.append(MessageFormat.format("{0}_Element {1} = {2};\n", typeGenname, tempID, leftExpression.expression));
					} else {
						source.append(MessageFormat.format("{0} {1} = {2};\n", typeGenname, tempID, leftExpression.expression));
					}
					source.append(leftExpression.postamble);
					if (rhsCopied) {
						source.append(MessageFormat.format("{0}.assign({1});\n", tempID, rhsCopy));
					} else {
						//TODO handle needs conversion
						value.generateCodeInit(aData, source, tempID);
					}

					source.append("}\n");
				}
			} else {
				// left hand side is a single assignment
				final String name = assignment.getGenNameFromScope(aData, source, myScope, null);
				if (!isOptional && value.getValuetype() == Value_type.REFERENCED_VALUE) {
					final Reference rightReference = ((Referenced_Value)value).getReference();
					final Assignment rightAssignment = rightReference.getRefdAssignment(CompilationTimeStamp.getBaseTimestamp(), true);
					if (rightAssignment.getType(CompilationTimeStamp.getBaseTimestamp()).fieldIsOptional(rightReference.getSubreferences())) {
						value.generateCodeInitMandatory(aData, source, name);
					} else {
						value.generateCodeInit(aData, source, name);
					}
				} else {
					value.generateCodeInit(aData, source, name);
				}
			}
			if(rhsCopied) {
				source.append("}\n");
			}
		} else {
			final String rhsCopy = aData.getTemporaryVariableName();
			if(rhsCopied) {
				source.append("{\n");
				source.append(MessageFormat.format("{0} {1} = new {0}();\n", template.getMyGovernor().getGenNameTemplate(aData, source, myScope), rhsCopy));
			}
			// TODO handle needs_conv
			if (reference.getSubreferences().size() > 1) {
				if((templateRestriction != Restriction_type.TR_NONE || !generateRestrictionCheck) && template.hasSingleExpression()) {
					final ExpressionStruct expression = new ExpressionStruct();
					reference.generateCode(aData, expression);
					source.append(expression.preamble);
					if (rhsCopied) {
						source.append(MessageFormat.format("{0}.assign({1});\n", rhsCopy, template.getSingleExpression(aData, false)));
						expression.expression.append(MessageFormat.format(".assign({0});\n", rhsCopy));
					} else {
						expression.expression.append(MessageFormat.format(".assign({0});\n", template.getSingleExpression(aData, false)));
					}

					expression.mergeExpression(source);
				} else {
					final String tempID = aData.getTemporaryVariableName();
					final ExpressionStruct expression = new ExpressionStruct();
					reference.generateCode(aData, expression);

					if (rhsCopied) {
						//TODO handle needs conversion case
						template.generateCodeInit(aData, source, rhsCopy);
					}

					source.append("{\n");
					source.append(expression.preamble);
					final IType governor = template.getMyGovernor();
					source.append(MessageFormat.format("{0} {1} = {2};\n", governor.getGenNameTemplate(aData, source, template.getMyScope()), tempID, expression.expression));
					source.append(expression.postamble);
					if (rhsCopied) {
						source.append(MessageFormat.format("{0}.assign({1});\n", tempID, rhsCopy));
					} else {
						//TODO handle needs conversion case
						if (Type_type.TYPE_SEQUENCE_OF.equals(governor.getTypetype()) || Type_type.TYPE_ARRAY.equals(governor.getTypetype())) {
							source.append(MessageFormat.format("{0}.removeAllPermutations();\n", tempID));
						}
						template.generateCodeInit(aData, source, tempID);
					}

					if (templateRestriction != Restriction_type.TR_NONE && generateRestrictionCheck) {
						TemplateRestriction.generateRestrictionCheckCode(aData, source, location, tempID, templateRestriction);
					}

					source.append("}\n");
				}
			} else {
				// left hand side is a single assignment
				final String rhsName = reference.getRefdAssignment(CompilationTimeStamp.getBaseTimestamp(), false).getGenNameFromScope(aData, source, myScope, "");
				final IType governor = template.getMyGovernor();
				if (Type_type.TYPE_SEQUENCE_OF.equals(governor.getTypetype()) || Type_type.TYPE_ARRAY.equals(governor.getTypetype())) {
					source.append(MessageFormat.format("{0}.removeAllPermutations();\n", rhsCopied?rhsCopy:rhsName));
				}
				template.generateCodeInit(aData, source, rhsCopied?rhsCopy:rhsName);
				if (rhsCopied) {
					source.append(MessageFormat.format("{0}.assign({1});\n", rhsName, rhsCopy));
				}

				if (templateRestriction != Restriction_type.TR_NONE && generateRestrictionCheck) {
					TemplateRestriction.generateRestrictionCheckCode(aData, source, location, rhsName, templateRestriction);
				}
			}
			if(rhsCopied) {
				source.append("}\n");
			}
		}
	}
}
