/******************************************************************************
 * Copyright (c) 2000-2017 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.templates;

import java.text.MessageFormat;

import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.IValue.Value_type;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.ReferenceChain;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Const;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_ModulePar;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_ModulePar_Template;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Template;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Var;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Var_Template;
import org.eclipse.titan.designer.AST.TTCN3.types.SequenceOf_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.SetOf_Type;
import org.eclipse.titan.designer.AST.TTCN3.values.Referenced_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.SequenceOf_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.SetOf_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Undefined_LowerIdentifier_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.ExpressionStruct;
import org.eclipse.titan.designer.compiler.JavaGenData;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * Represents the BNF element "AllElementsFrom" Ref: ttcn3 standard
 * "ETSI ES 201 873-1 V4.6.1 (2014-06)" A.1.6.1.3 Template definitions/127.
 *
 * @author Jeno Balasko
 *
 */
public class AllElementsFrom extends TemplateBody {

	private static final String SPECIFICVALUEEXPECTED = "After all from a specific value is expected";
	private static final String LISTEXPECTED = "After all from a variable or a template of list type is expected";
	private static final String TYPEMISMATCH = "Type mismatch: `{0}'' was expected in the list";
	private static final String REFERENCEEXPECTED = "Reference to a value was expected";
	private static final String ANYOROMITANDPERMUTATIONPRHOHIBITED = "`all from' can not refer to a template containing permutation or AnyElementsOrNone";
	/**
	 * myGovernor is the governor of AllElementsFrom. It is the type/governor of
	 * the elements/items of its templates which shall be a sequence.
	 *
	 */
	private IType myGovernor;

	public AllElementsFrom() {
		super();
	}

	public AllElementsFrom(final TTCN3Template t) {
		template = t;
		// template shall be a specific value & a reference
		// element type => check function
		// These features are checked by checkThisTemplateGeneric()
	}

	@Override
	/** {@inheritDoc} */
	public IType getMyGovernor() {
		return myGovernor;
	}

	@Override
	/** {@inheritDoc} */
	public void setMyGovernor(final IType governor) {
		myGovernor = governor;
	}

	@Override
	/** {@inheritDoc} */
	public boolean checkExpressionSelfReferenceTemplate(final CompilationTimeStamp timestamp, final Assignment lhs) {
		return template.checkExpressionSelfReferenceTemplate(timestamp, lhs);
	}

	@Override
	/** {@inheritDoc} */
	public boolean checkThisTemplateGeneric(final CompilationTimeStamp timestamp, final IType type, final boolean isModified, final boolean allowOmit,
			final boolean allowAnyOrOmit, final boolean subCheck, final boolean implicitOmit, final Assignment lhs) {

		if (template == null) {
			ErrorReporter.INTERNAL_ERROR();
			return false;
		}

		if (!Template_type.SPECIFIC_VALUE.equals(template.getTemplatetype())) {
			template.getLocation().reportSemanticError(SPECIFICVALUEEXPECTED);
			template.setIsErroneous(true);
			return false;
		}

		if (!((SpecificValue_Template) template).isReference()) {
			template.getLocation().reportSemanticError(REFERENCEEXPECTED);
			template.setIsErroneous(true);
			return false;
		}

		// isReference branch:
		final Reference reference = ((SpecificValue_Template) template).getReference();
		final Assignment assignment = reference.getRefdAssignment(timestamp, true);
		if (assignment == null) {
			template.getLocation().reportSemanticError("Assignment not found");
			template.setIsErroneous(true);
			return false;
		}

		// ES 201 873-1 - V4.7.1 B.1.2.1.a:
		// The type of the template list and the member type of the template in
		// the all from clause shall be
		// compatible.
		final IType assType = assignment.getType(timestamp);

		if (assType != null) {

			final IType atype = assType.getFieldType(timestamp, reference, 1, Expected_Value_type.EXPECTED_DYNAMIC_VALUE, false);
			if (atype == null) {
				template.setIsErroneous(true);
				return false;
			}

			final IType referredType = atype.getTypeRefdLast(timestamp);
			IType it = null; // type of the fields of the sequence/set
			if (referredType != null) {
				switch (referredType.getTypetype()) {
				case TYPE_SEQUENCE_OF:
					it = ((SequenceOf_Type) referredType).getOfType();
					break;
				case TYPE_SET_OF:
					it = ((SetOf_Type) referredType).getOfType();
					break;
				case TYPE_TTCN3_SEQUENCE:
					// it = ((TTCN3_Sequence_Type) rt).getFieldType(timestamp,
					// reference, actualSubReference, expectedIndex,
					// interruptIfOptional)
					break;
				default:
					template.getLocation().reportSemanticError(LISTEXPECTED);
					template.setIsErroneous(true);
				}
			}

			if (it != null) {
				if (!it.isCompatible(timestamp, type, null, null, null)) {
					template.getLocation().reportSemanticError(MessageFormat.format(TYPEMISMATCH, type.getTypename()));
					template.setIsErroneous(true);
				}
			}

		}

		// ES 201 873-1 - V4.7.1 B.1.2.1.
		// b) The template in the all from clause as a whole shall not resolve
		// into a matching mechanism (i.e. its
		// elements may contain any of the matching mechanisms or matching
		// attributes with the exception of those
		// described in the following restriction).
		// c) Individual fields of the template in the all from clause shall not
		// resolve to any of the following matching
		// mechanisms: AnyElementsOrNone, permutation
		ITTCN3Template body = null;
		IValue value = null;
		switch (assignment.getAssignmentType()) {
		case A_TEMPLATE:
			body = ((Def_Template) assignment).getTemplate(timestamp);
			break;
		case A_VAR_TEMPLATE:
			body = ((Def_Var_Template) assignment).getInitialValue();
			break;
		case A_CONST:
			break;
		case A_MODULEPAR:
			value = ((Def_ModulePar) assignment).getDefaultValue();
			break;
		case A_MODULEPAR_TEMPLATE:
			body = ((Def_ModulePar_Template) assignment).getDefaultTemplate();
			break;
		case A_VAR:
			value = ((Def_Var) assignment).getInitialValue();
			break;
		default:
			return false;
		}

		//it is too complex to analyse anyoromit. Perhaps it can be omit

		if (body != null) {

			switch (body.getTemplatetype()) {
			case TEMPLATE_LIST:
				//TODO: if "all from" is in a permutation list it anyoromit and any is permitted
				if (!allowAnyOrOmit && ((Template_List) body).containsAnyornoneOrPermutation()) {
					template.getLocation().reportSemanticError(ANYOROMITANDPERMUTATIONPRHOHIBITED);
					template.setIsErroneous(true);
				}
				break;
			case NAMED_TEMPLATE_LIST:
				((Named_Template_List) body).checkSpecificValue(timestamp, true);
				break;
			case SPECIFIC_VALUE:
				break;
			default:
				template.getLocation().reportSemanticError(LISTEXPECTED);
				template.setIsErroneous(true);
				return false;
			}

		}

		//		if (value != null) {
		//			// TODO
		//			return;
		//		}

		return false;
	}

	// @Override
	/** {@inheritDoc} */
	public Template_type getTemplatetype() {
		return Template_type.ALLELEMENTSFROM;
	}

	@Override
	/** {@inheritDoc} */
	public String getTemplateTypeName() {
		return "all from ".concat(template.getTemplateTypeName());
	}

	@Override
	/** {@inheritDoc} */
	public boolean isValue(final CompilationTimeStamp timestamp) {
		return false;
	}

	/**
	 * Gets the number of values If the value is type of SEQUENCEOF_VALUE or
	 * type of SETOF_VALUE then returns their size otherwise returns 1
	 */
	private int getNofValues(final IValue value, final CompilationTimeStamp timestamp) {
		int result = 0;
		if (value == null) {
			return result;
		}

		final IReferenceChain chain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
		final IValue lastValue = value.getValueRefdLast(timestamp, chain);
		chain.release();
		if (lastValue.getIsErroneous(timestamp)) {
			return result;
		}
		if (Value_type.SEQUENCEOF_VALUE.equals(lastValue.getValuetype())) {
			final SequenceOf_Value lvalue = (SequenceOf_Value) lastValue;
			result = lvalue.getNofComponents();
			return result;
		} else if (Value_type.SETOF_VALUE.equals(lastValue.getValuetype())) {
			final SetOf_Value svalue = (SetOf_Value) lastValue;
			result = svalue.getNofComponents();
			return result;
		} else {
			return 1; // this value is calculated as 1 in an all from
		}
	}

	/**
	 * Calculates the number of list members which are not the any or none
	 * symbol.
	 *
	 * @return the number calculated.
	 * */
	public int getNofTemplatesNotAnyornone(final CompilationTimeStamp timestamp) {
		int result = 0;
		if (template == null) {
			ErrorReporter.INTERNAL_ERROR();
			return result;
		}

		if (!Template_type.SPECIFIC_VALUE.equals(template.getTemplatetype())) {
			template.getLocation().reportSemanticError(REFERENCEEXPECTED);
			template.setIsErroneous(true);
			return result;
		}

		if (!((SpecificValue_Template) template).isReference()) {
			template.getLocation().reportSemanticError(REFERENCEEXPECTED);
			template.setIsErroneous(true);
			return result;
		}

		// isReference branch:
		final Reference reference = ((SpecificValue_Template) template).getReference();
		final Assignment assignment = reference.getRefdAssignment(timestamp, true);
		if (assignment == null) {
			template.getLocation().reportSemanticError("Assignment not found");
			template.setIsErroneous(true);
			return result;
		}

		ITTCN3Template body = null;

		switch (assignment.getAssignmentType()) {
		case A_TEMPLATE:
			body = ((Def_Template) assignment).getTemplate(timestamp);
			break;
		case A_VAR_TEMPLATE:
			body = ((Def_Var_Template) assignment).getInitialValue();
			break;
		case A_CONST:
			final IValue value = ((Def_Const) assignment).getValue();
			return getNofValues(value, timestamp);
		case A_MODULEPAR:
			final IValue mvalue = ((Def_ModulePar) assignment).getDefaultValue();
			return getNofValues(mvalue, timestamp);
		case A_MODULEPAR_TEMPLATE:
			body = ((Def_ModulePar_Template) assignment).getDefaultTemplate();
			break;
		default:
			return result;
		}
		if (body == null) {
			ErrorReporter.INTERNAL_ERROR();
			return result;
		}
		if (!Template_type.TEMPLATE_LIST.equals(body.getTemplatetype())) {
			template.getLocation().reportSemanticError("Template must be a record of or a set of values");
			template.setIsErroneous(true);
			return result;
		}
		result = ((Template_List) body).getNofTemplatesNotAnyornone(timestamp);
		return result;
	}


	public void generateCodeInitAllFrom(final JavaGenData aData, final StringBuilder source, final String name) {
		IValue value = ((SpecificValue_Template) template).getValue();
		Reference reference;
		if (value.getValuetype() == Value_type.UNDEFINED_LOWERIDENTIFIER_VALUE) {
			//value.getValueRefdLast(CompilationTimeStamp.getBaseTimestamp(), null);
			reference = ((Undefined_LowerIdentifier_Value) value).getAsReference();
		} else {
			reference = ((Referenced_Value) value).getReference();
		}

		ExpressionStruct expression = new ExpressionStruct();
		reference.generateCode(aData, expression);

		Assignment assignment = reference.getRefdAssignment(CompilationTimeStamp.getBaseTimestamp(), false);
		switch (assignment.getAssignmentType()) {
		case A_CONST:
		case A_EXT_CONST:
		case A_MODULEPAR:
		case A_VAR:
		case A_PAR_VAL:
		case A_PAR_VAL_IN:
		case A_PAR_VAL_OUT:
		case A_PAR_VAL_INOUT:
		case A_FUNCTION_RVAL:
		case A_EXT_FUNCTION_RVAL:
			if (assignment.getType(CompilationTimeStamp.getBaseTimestamp()).fieldIsOptional(reference.getSubreferences())) {
				expression.expression.append(".get()");
			}
			break;
		default:
			break;
		}

		// The caller will have to provide the for cycle with this variable
		source.append(MessageFormat.format("{0}.assign({1}.getAt(i_i));\n", name, expression.expression));
	}
}
