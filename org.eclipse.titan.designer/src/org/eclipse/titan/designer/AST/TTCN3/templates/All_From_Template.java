/******************************************************************************
 * Copyright (c) 2000-2017 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.templates;

import java.text.MessageFormat;
import java.util.List;

import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.ISubReference;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.IType.Type_type;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.IValue.Value_type;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.AST.ParameterisedSubReference;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.ReferenceChain;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.definitions.ActualParameter;
import org.eclipse.titan.designer.AST.TTCN3.definitions.ActualParameterList;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Const;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_ModulePar;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_ModulePar_Template;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Template;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Var;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Var_Template;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Referenced_ActualParameter;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Template_ActualParameter;
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
 * Represents a template that holds a hexstring pattern.
 *
 * @author Kristof Szabados
 * */
public class All_From_Template extends TTCN3Template {
	private static final String SPECIFICVALUEEXPECTED = "After all from a specific value is expected";
	private static final String LISTEXPECTED = "After all from a variable or a template of list type is expected";
	private static final String TYPEMISMATCH = "Type mismatch: `{0}'' was expected in the list";
	private static final String REFERENCEEXPECTED = "Reference to a value was expected";
	private static final String ANYOROMITANDPERMUTATIONPRHOHIBITED = "`all from' can not refer to a template containing permutation or AnyElementsOrNone";

	//TODO: modify: in titan.core this is a SpecificValue_Template
	private final TTCN3Template allFrom;

	public All_From_Template(final TTCN3Template allFrom) {
		this.allFrom = allFrom;

		if (allFrom != null) {
			allFrom.setFullNameParent(this);
		}
	}

	@Override
	/** {@inheritDoc} */
	public Template_type getTemplatetype() {
		return Template_type.ALL_FROM;
	}


	@Override
	/** {@inheritDoc} */
	public String getTemplateTypeName() {
		return "all from ".concat(allFrom.getTemplateTypeName());
	}

	@Override
	/** {@inheritDoc} */
	public String createStringRepresentation() {
		if (allFrom == null) {
			return "<erroneous template>";
		}

		final StringBuilder builder = new StringBuilder();
		builder.append(allFrom.createStringRepresentation());

		if (lengthRestriction != null) {
			builder.append(lengthRestriction.createStringRepresentation());
		}
		if (isIfpresent) {
			builder.append("ifpresent");
		}

		return builder.toString();
	}

	public TTCN3Template getAllFrom() {
		return allFrom;
	}

	@Override
	/** {@inheritDoc} */
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		if (allFrom != null) {
			allFrom.setMyScope(scope);
		}
	}

	@Override
	/** {@inheritDoc} */
	public IType getExpressionGovernor(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue) {
		if (lastTimeChecked != null && !lastTimeChecked.isLess(timestamp)) {
			if (myGovernor != null) {
				return myGovernor;
			}
		}

		if (allFrom != null) {
			allFrom.setMyGovernor(null);
			final ITTCN3Template temp = allFrom.setLoweridToReference(timestamp);
			final IType type = temp.getExpressionGovernor(timestamp, expectedValue);
			if (temp.getIsErroneous(timestamp)) {
				isErroneous = true;
			}
			return type;
		}

		return null;
	}

	@Override
	/** {@inheritDoc} */
	public Type_type getExpressionReturntype(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue) {
		if (getIsErroneous(timestamp) || allFrom == null) {
			return Type_type.TYPE_UNDEFINED;
		}

		allFrom.setLoweridToReference(timestamp);
		return allFrom.getExpressionReturntype(timestamp, expectedValue);
	}

	@Override
	/** {@inheritDoc} */
	public boolean checkExpressionSelfReferenceTemplate(final CompilationTimeStamp timestamp, final Assignment lhs) {
		return allFrom.checkExpressionSelfReferenceTemplate(timestamp, lhs);
	}

	@Override
	/** {@inheritDoc} */
	public void checkSpecificValue(final CompilationTimeStamp timestamp, final boolean allowOmit) {
		getLocation().reportSemanticError("A specific value expected instead of a all from reference");
	}

	@Override
	/** {@inheritDoc} */
	public void checkRecursions(final CompilationTimeStamp timestamp, final IReferenceChain referenceChain) {
		if (referenceChain.add(this) && allFrom != null) {
			allFrom.checkRecursions(timestamp, referenceChain);
		}
	}

	@Override
	/** {@inheritDoc} */
	public boolean checkThisTemplateGeneric(final CompilationTimeStamp timestamp, final IType type, final boolean isModified, final boolean allowOmit,
			final boolean allowAnyOrOmit, final boolean subCheck, final boolean implicitOmit, final Assignment lhs) {

		if (allFrom == null) {
			ErrorReporter.INTERNAL_ERROR();
			return false;
		}

		if (!Template_type.SPECIFIC_VALUE.equals(allFrom.getTemplatetype())) {
			allFrom.getLocation().reportSemanticError(SPECIFICVALUEEXPECTED);
			allFrom.setIsErroneous(true);
			return false;
		}

		if (!((SpecificValue_Template) allFrom).isReference()) {
			allFrom.getLocation().reportSemanticError(REFERENCEEXPECTED);
			allFrom.setIsErroneous(true);
			return false;
		}

		// isReference branch:
		final Reference reference = ((SpecificValue_Template) allFrom).getReference();
		final Assignment assignment = reference.getRefdAssignment(timestamp, true);
		if (assignment == null) {
			allFrom.getLocation().reportSemanticError("Assignment not found");
			allFrom.setIsErroneous(true);
			return false;
		}

		boolean selfReference = lhs == assignment;
		// ES 201 873-1 - V4.7.1 B.1.2.1.a:
		// The type of the template list and the member type of the template in
		// the all from clause shall be
		// compatible.
		final IType assType = assignment.getType(timestamp);

		if (assType != null) {

			final IType atype = assType.getFieldType(timestamp, reference, 1, Expected_Value_type.EXPECTED_DYNAMIC_VALUE, false);
			if (atype == null) {
				allFrom.setIsErroneous(true);
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
					allFrom.getLocation().reportSemanticError(LISTEXPECTED);
					allFrom.setIsErroneous(true);
				}
			}

			if (it != null) {
				if (!it.isCompatible(timestamp, type, null, null, null)) {
					allFrom.getLocation().reportSemanticError(MessageFormat.format(TYPEMISMATCH, type.getTypename()));
					allFrom.setIsErroneous(true);
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
			selfReference |= checkThisTemplateParameterizedReference(reference, lhs);
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
		case A_FUNCTION_RVAL:
		case A_FUNCTION_RTEMP:
		case A_EXT_FUNCTION_RVAL:
		case A_EXT_FUNCTION_RTEMP:
			selfReference |= checkThisTemplateParameterizedReference(reference, lhs);
			break;
		default:
			return selfReference;
		}

		//it is too complex to analyse anyoromit. Perhaps it can be omit

		if (body != null) {

			switch (body.getTemplatetype()) {
			case TEMPLATE_LIST:
				//TODO: if "all from" is in a permutation list it anyoromit and any is permitted
				if (!allowAnyOrOmit && ((Template_List) body).containsAnyornoneOrPermutation()) {
					allFrom.getLocation().reportSemanticError(ANYOROMITANDPERMUTATIONPRHOHIBITED);
					allFrom.setIsErroneous(true);
				}
				break;
			case NAMED_TEMPLATE_LIST:
				((Named_Template_List) body).checkSpecificValue(timestamp, true);
				break;
			case SPECIFIC_VALUE:
				break;
			default:
				allFrom.getLocation().reportSemanticError(LISTEXPECTED);
				allFrom.setIsErroneous(true);
				return selfReference;
			}

		}

		//		if (value != null) {
		//			// TODO
		//			return;
		//		}

		return selfReference;
	}

	private boolean checkThisTemplateParameterizedReference(final Reference reference, final Assignment lhs) {
		final List<ISubReference> subreferences = reference.getSubreferences();
		if (subreferences.isEmpty() || !(subreferences.get(0) instanceof ParameterisedSubReference)) {
			return false;
		}

		final ParameterisedSubReference subReference = (ParameterisedSubReference) subreferences.get(0);
		final ActualParameterList actualParameterList = subReference.getActualParameters();
		if (actualParameterList == null) {
			return false;
		}

		final int nofParameters = actualParameterList.getNofParameters();
		for (int i = 0; i < nofParameters; i++) {
			Reference parameterReference = null;
			final ActualParameter actualParameter = actualParameterList.getParameter(i);
			if (actualParameter instanceof Template_ActualParameter) {
				TemplateInstance templateInstance = ((Template_ActualParameter)actualParameter).getTemplateInstance();
				ITTCN3Template template = templateInstance.getTemplateBody();
				template = template.setLoweridToReference(CompilationTimeStamp.getBaseTimestamp());
				if(template.getTemplatetype() == Template_type.TEMPLATE_REFD) {
					parameterReference = ((Referenced_Template)template).getReference();
				}
			} else if (actualParameter instanceof Referenced_ActualParameter) {
				parameterReference = ((Referenced_ActualParameter) actualParameter).getReference();
			}

			if (parameterReference != null) {
				final Assignment assignment = parameterReference.getRefdAssignment(CompilationTimeStamp.getBaseTimestamp(), false);
				if (assignment == lhs) {
					return true;
				}

				// In case a parameter is another function call / parametrised template
				// check their parameters as well
				switch (assignment.getAssignmentType()) {
				case A_TEMPLATE:
				case A_FUNCTION_RVAL:
				case A_FUNCTION_RTEMP:
				case A_EXT_FUNCTION_RVAL:
				case A_EXT_FUNCTION_RTEMP:
					if (checkThisTemplateParameterizedReference(parameterReference, lhs)) {
						return true;
					}
					break;
				default:
					break;
				}
			}
		}
		return false;
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
		if (allFrom == null) {
			ErrorReporter.INTERNAL_ERROR();
			return result;
		}

		if (!Template_type.SPECIFIC_VALUE.equals(allFrom.getTemplatetype())) {
			allFrom.getLocation().reportSemanticError(REFERENCEEXPECTED);
			allFrom.setIsErroneous(true);
			return result;
		}

		if (!((SpecificValue_Template) allFrom).isReference()) {
			allFrom.getLocation().reportSemanticError(REFERENCEEXPECTED);
			allFrom.setIsErroneous(true);
			return result;
		}

		// isReference branch:
		final Reference reference = ((SpecificValue_Template) allFrom).getReference();
		final Assignment assignment = reference.getRefdAssignment(timestamp, true);
		if (assignment == null) {
			allFrom.getLocation().reportSemanticError("Assignment not found");
			allFrom.setIsErroneous(true);
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
			allFrom.getLocation().reportSemanticError("Template must be a record of or a set of values");
			allFrom.setIsErroneous(true);
			return result;
		}
		result = ((Template_List) body).getNofTemplatesNotAnyornone(timestamp);
		return result;
	}

	@Override
	/** {@inheritDoc} */
	public void reArrangeInitCode(final JavaGenData aData, final StringBuilder source, final Module usageModule) {
		if (allFrom != null) {
			allFrom.reArrangeInitCode(aData, source, usageModule);
		}
	}

	@Override
	/** {@inheritDoc} */
	public void generateCodeInit(final JavaGenData aData, final StringBuilder source, final String name) {
		if (lastTimeBuilt != null && !lastTimeBuilt.isLess(aData.getBuildTimstamp())) {
			return;
		}
		lastTimeBuilt = aData.getBuildTimstamp();

		generateCodeInitAllFrom(aData, source, name);

		if (lengthRestriction != null) {
			if(getCodeSection() == CodeSectionType.CS_POST_INIT) {
				lengthRestriction.reArrangeInitCode(aData, source, myScope.getModuleScope());
			}
			lengthRestriction.generateCodeInit(aData, source, name);
		}

		if (isIfpresent) {
			source.append(name);
			source.append(".set_ifPresent();\n");
		}
	}

	public void generateCodeInitAllFrom(final JavaGenData aData, final StringBuilder source, final String name) {
		final IValue value = ((SpecificValue_Template) allFrom).getValue();
		Reference reference;
		if (value.getValuetype() == Value_type.UNDEFINED_LOWERIDENTIFIER_VALUE) {
			//value.getValueRefdLast(CompilationTimeStamp.getBaseTimestamp(), null);
			reference = ((Undefined_LowerIdentifier_Value) value).getAsReference();
		} else {
			reference = ((Referenced_Value) value).getReference();
		}

		final ExpressionStruct expression = new ExpressionStruct();
		reference.generateCode(aData, expression);

		final Assignment assignment = reference.getRefdAssignment(CompilationTimeStamp.getBaseTimestamp(), false);
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

	public void generateCodeInitAllFrom(final JavaGenData aData, final StringBuilder source, final String name, final StringBuilder referenceCache) {
		source.append(MessageFormat.format("{0}.assign({1}.getAt(i_i));\n", name, referenceCache));
	}
}
