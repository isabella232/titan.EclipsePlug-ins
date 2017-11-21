/******************************************************************************
 * Copyright (c) 2000-2017 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.templates;

import java.text.MessageFormat;
import java.util.ArrayList;

import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.IType.Type_type;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.IValue.Value_type;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.TemplateRestriction;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.ExpressionStruct;
import org.eclipse.titan.designer.compiler.JavaGenData;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * Represents a template that matches everything but the elements in a list.
 *
 * @author Kristof Szabados
 * */
public final class ComplementedList_Template extends CompositeTemplate {
	private static final String ANYOROMITWARNING = "`*'' in complemented list. This template will not match anything.";

	public ComplementedList_Template(final ListOfTemplates templates) {
		super(templates);
	}

	@Override
	/** {@inheritDoc} */
	public Template_type getTemplatetype() {
		return Template_type.COMPLEMENTED_LIST;
	}

	@Override
	/** {@inheritDoc} */
	public String getTemplateTypeName() {
		if (isErroneous) {
			return "erroneous complemented list match";
		}

		return "complemented list match";
	}

	@Override
	/** {@inheritDoc} */
	public IType getExpressionGovernor(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue) {
		if (myGovernor != null) {
			return myGovernor;
		}

		for (int i = 0, size = templates.getNofTemplates(); i < size; i++) {
			final IType type = templates.getTemplateByIndex(i).getExpressionGovernor(timestamp, expectedValue);
			if (type != null) {
				return type;
			}
		}

		return null;
	}

	@Override
	/** {@inheritDoc} */
	public Type_type getExpressionReturntype(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue) {
		if (getIsErroneous(timestamp)) {
			return Type_type.TYPE_UNDEFINED;
		}

		for (int i = 0, size = templates.getNofTemplates(); i < size; i++) {
			final Type_type type = templates.getTemplateByIndex(i).getExpressionReturntype(timestamp, expectedValue);
			if (!Type_type.TYPE_UNDEFINED.equals(type)) {
				return type;
			}
		}

		return Type_type.TYPE_UNDEFINED;
	}

	@Override
	/** {@inheritDoc} */
	public boolean checkExpressionSelfReferenceTemplate(final CompilationTimeStamp timestamp, final Assignment lhs) {
		for (int i = 0, size = templates.getNofTemplates(); i < size; i++) {
			if(templates.getTemplateByIndex(i).checkExpressionSelfReferenceTemplate(timestamp, lhs)) {
				return true;
			}
		}

		return false;
	}

	@Override
	/** {@inheritDoc} */
	public void checkSpecificValue(final CompilationTimeStamp timestamp, final boolean allowOmit) {
		getLocation().reportSemanticError("A specific value expected instead of a complemented list match");
	}

	@Override
	/** {@inheritDoc} */
	public boolean checkThisTemplateGeneric(final CompilationTimeStamp timestamp, final IType type, final boolean isModified,
			final boolean allowOmit, final boolean allowAnyOrOmit, final boolean subCheck, final boolean implicitOmit, final Assignment lhs) {

		if(type == null){
			return false;
		}

		final boolean allowOmitInValueList = TTCN3Template.allowOmitInValueList(getLocation(), allowOmit);

		boolean selfReference = false;
		for (int i = 0, size = templates.getNofTemplates(); i < size; i++) {
			final TTCN3Template component = templates.getTemplateByIndex(i);
			component.setMyGovernor(type);
			final ITTCN3Template temporalComponent = type.checkThisTemplateRef(timestamp, component);
			selfReference |= temporalComponent.checkThisTemplateGeneric(timestamp, type, false, allowOmitInValueList, true, subCheck, implicitOmit, lhs);

			if (Template_type.ANY_OR_OMIT.equals(temporalComponent.getTemplatetype())) {
				component.getLocation().reportSemanticWarning(ANYOROMITWARNING);
			}
		}

		checkLengthRestriction(timestamp, type);
		if (!allowOmit && isIfpresent) {
			location.reportSemanticError("`ifpresent' is not allowed here");
		}
		if (subCheck) {
			type.checkThisTemplateSubtype(timestamp, this);
		}

		return selfReference;
	}

	/**
	 * If ALLOW_OMIT_IN_VALUELIST_TEMPLATE_PROPERTY is switched on
	 * and has AnyOrOmit (=AnyOrNone) or omit in the list then accepted, otherwise not
	 */
	@Override
	public boolean checkPresentRestriction(final CompilationTimeStamp timestamp, final String definitionName, final Location usageLocation) {
		checkRestrictionCommon(timestamp, definitionName, TemplateRestriction.Restriction_type.TR_PRESENT, usageLocation);

		final boolean allowOmitInValueList = TTCN3Template.allowOmitInValueList(getLocation(), true);
		if(allowOmitInValueList) {
			boolean hasAnyOrOmit = false;
			for (int i = 0, size = templates.getNofTemplates(); i < size; i++) {
				final TTCN3Template component = templates.getTemplateByIndex(i);

				// === if OMIT_VALUE then hasOmitValue=true and break ====
				// componentType == ITTCN3Template.Template_type.OMIT_VALUE does not work
				// TODO: put this if-block to a higher level
				//TODO: avoid NPE (?)
				if(Template_type.SPECIFIC_VALUE.equals(component.getTemplatetype())){
					final IValue value = ((SpecificValue_Template) component).getSpecificValue();
					if( Value_type.OMIT_VALUE.equals(value.getValuetype())){
						hasAnyOrOmit = true;
						break;
					}
				}

				final TTCN3Template.Template_type componentType =  component.getTemplatetype();
				if (ITTCN3Template.Template_type.ANY_OR_OMIT.equals(componentType)) {
					hasAnyOrOmit = true;
					break;
				}

			}
			if (!hasAnyOrOmit) {
				location.reportSemanticError(MessageFormat.format(PRESENTRESTRICTIONERROR+" without omit or AnyValueOrNone in the list", definitionName, getTemplateTypeName()));
				return false;
			}
		}
		// some basic check was performed, always needs runtime check
		return true;
	}

	@Override
	/** {@inheritDoc} */
	protected String getNameForStringRep() {
		return "complement";
	}

	@Override
	/** {@inheritDoc} */
	public boolean needsTemporaryReference() {
		return true;
	}

	@Override
	/** {@inheritDoc} */
	public boolean hasSingleExpression() {
		return false;
	}

	@Override
	/** {@inheritDoc} */
	public StringBuilder getSingleExpression(final JavaGenData aData, final boolean castIsNeeded) {
		StringBuilder result = new StringBuilder();

		result.append( "\t//TODO: fatal error while generating " );
		result.append( getClass().getSimpleName() );
		result.append( ".getSingleExpression() !\n" );
		// TODO: fatal error
		return result;
	}

	@Override
	/** {@inheritDoc} */
	public void reArrangeInitCode(final JavaGenData aData, final StringBuilder source, final Module usageModule) {
		for (int i = 0; i < templates.getNofTemplates(); i++) {
			templates.getTemplateByIndex(i).reArrangeInitCode(aData, source, usageModule);
		}

		if (lengthRestriction != null) {
			lengthRestriction.reArrangeInitCode(aData, source, usageModule);
		}
	}

	@Override
	/** {@inheritDoc} */
	public void generateCodeInit(final JavaGenData aData, final StringBuilder source, final String name) {
		if (lastTimeBuilt != null && !lastTimeBuilt.isLess(aData.getBuildTimstamp())) {
			return;
		}
		lastTimeBuilt = aData.getBuildTimstamp();

		aData.addBuiltinTypeImport( "Base_Template.template_sel" );

		final ArrayList<Integer> variables = new ArrayList<Integer>();
		long fixedPart = 0;
		for (int i = 0; i < templates.getNofTemplates(); i++) {
			final TTCN3Template templateListItem = templates.getTemplateByIndex(i);
			if (templateListItem.getTemplatetype() == Template_type.ALL_FROM) {
				variables.add(i);
			} else {
				fixedPart++;
			}
		}

		final String typeName = myGovernor.getGenNameTemplate(aData, source, myScope);

		if (variables.size() > 0) {
			final StringBuilder preamble = new StringBuilder();
			final StringBuilder setType = new StringBuilder();
			final StringBuilder variableReferences[] = new StringBuilder[templates.getNofTemplates()];

			setType.append(MessageFormat.format("{0}.setType(template_sel.COMPLEMENTED_LIST, {1}", name, fixedPart));

			for (int v = 0; v < variables.size(); v++) {
				TTCN3Template template = templates.getTemplateByIndex(variables.get(v));
				// the template must be all from
				if ( template instanceof All_From_Template ) {
					template = ((All_From_Template)template).getAllFrom();
				}

				final Reference reference = ((SpecificValue_Template) template).getReference();
				final Assignment assignment = reference.getRefdAssignment(CompilationTimeStamp.getBaseTimestamp(), false);

				setType.append(" + ");

				final ExpressionStruct expression = new ExpressionStruct();
				reference.generateCode(aData, expression);
				if (expression.preamble.length() > 0) {
					preamble.append(expression.preamble);
				}

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
						setType.append(".get()");
					}
					break;
				default:
					break;
				}

				variableReferences[variables.get(v)] = expression.expression;
				setType.append(expression.expression);
				setType.append(".n_elem().getInt()");
			}

			source.append(preamble);
			source.append(setType);
			source.append(");\n");

			final StringBuilder shifty = new StringBuilder();
			for (int i = 0; i < templates.getNofTemplates(); i++) {
				final TTCN3Template template = templates.getTemplateByIndex(i);

				switch (template.getTemplatetype()) {
				case ALL_FROM: {
					// the template must be all from

					final StringBuilder storedExpression = variableReferences[i];
					source.append(MessageFormat.format("for (int i_i = 0, i_lim = {0}.n_elem().getInt(); i_i < i_lim; ++i_i ) '{'\n", storedExpression));

					String embeddedName = MessageFormat.format("{0}.listItem({1}{2} + i_i)", name, i, shifty);
					((All_From_Template) template).generateCodeInitAllFrom(aData, source, embeddedName, storedExpression);
					source.append("}\n");
					shifty.append(MessageFormat.format("-1 + {0}.n_elem().getInt()", storedExpression));
					break;
				}
				default:
					if (template.needsTemporaryReference()) {
						final String tempId = aData.getTemporaryVariableName();
						source.append("{\n");
						source.append(MessageFormat.format("{0} {1} = {2}.listItem({3}{4});\n", typeName, tempId, name, i, shifty));
						generateCodeInit(aData, source, tempId);
						source.append("}\n");
					} else {
						final String embeddedName = MessageFormat.format("{0}.listItem({1}{2})", name, i, shifty);
						template.generateCodeInit(aData, source, embeddedName);
					}
					break;
				}
			}
		} else {
			source.append(MessageFormat.format("{0}.setType(template_sel.COMPLEMENTED_LIST, {1});\n", name, templates.getNofTemplates()));
			for (int i = 0; i < templates.getNofTemplates(); i++) {
				final TTCN3Template template = templates.getTemplateByIndex(i);
				if (template.needsTemporaryReference()) {
					final String tempId = aData.getTemporaryVariableName();
					source.append("{\n");
					source.append(MessageFormat.format("{0} {1} = {2}.listItem({3});\n", typeName, tempId, name, i));
					template.generateCodeInit(aData, source, tempId);
					source.append("}\n");
				} else {
					final String embeddedName = MessageFormat.format("{0}.listItem({1})", name, i);
					template.generateCodeInit(aData, source, embeddedName);
				}
			}
		}

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
}
