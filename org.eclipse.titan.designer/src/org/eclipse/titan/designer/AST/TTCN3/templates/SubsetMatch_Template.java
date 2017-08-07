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
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.types.Array_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.SequenceOf_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.SetOf_Type;
import org.eclipse.titan.designer.AST.TTCN3.values.Referenced_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Undefined_LowerIdentifier_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.ExpressionStruct;
import org.eclipse.titan.designer.compiler.JavaGenData;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * Represents a template for the subset matching mechanism.
 *
 * <p>
 * Example 1:
 * <p>
 * type set of integer SoI;
 * <p>
 * template SoI t_1 := subset ( 1,2,? );
 *
 * <p>
 * Example 2:
 * <p>
 * type set of integer SoI;
 * <p>
 * template SoI t_SoI1 := {1, 2, (6..9)};
 * <p>
 * template subset(all from t_SOI1) length(2);
 *
 * @author Kristof Szabados
 * */
public final class SubsetMatch_Template extends CompositeTemplate {

	public SubsetMatch_Template(final ListOfTemplates templates) {
		super(templates);
	}

	@Override
	/** {@inheritDoc} */
	public Template_type getTemplatetype() {
		return Template_type.SUBSET_MATCH;
	}

	@Override
	/** {@inheritDoc} */
	public String getTemplateTypeName() {
		if (isErroneous) {
			return "erroneous subset match";
		}

		return "subset match";
	}

	@Override
	/** {@inheritDoc} */
	public Type_type getExpressionReturntype(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue) {
		if (getIsErroneous(timestamp)) {
			return Type_type.TYPE_UNDEFINED;
		}

		return Type_type.TYPE_SET_OF;
	}

	@Override
	/** {@inheritDoc} */
	public void checkSpecificValue(final CompilationTimeStamp timestamp, final boolean allowOmit) {
		getLocation().reportSemanticError("A specific value expected instead of a subset match");
	}

	@Override
	/** {@inheritDoc} */
	protected void checkTemplateSpecificLengthRestriction(final CompilationTimeStamp timestamp, final Type_type typeType) {
		if (Type_type.TYPE_SET_OF.equals(typeType)) {
			final boolean hasAnyOrNone = templateContainsAnyornone();
			lengthRestriction.checkNofElements(timestamp, getNofTemplatesNotAnyornone(timestamp), hasAnyOrNone, true, hasAnyOrNone, this);
		}
	}

	@Override
	/** {@inheritDoc} */
	protected String getNameForStringRep() {
		return "subset";
	}

	@Override
	/** {@inheritDoc} */
	public boolean needsTemporaryReference() {
		return true;
	}

	@Override
	/** {@inheritDoc} */
	public boolean hasSingleExpression() {
		if (lengthRestriction != null || isIfpresent /*TODO: || get_needs_conversion () */) {
			return false;
		}

		return false;
	}

	@Override
	/** {@inheritDoc} */
	public void generateCodeExpression(final JavaGenData aData, final ExpressionStruct expression) {
		IType governor = myGovernor;
		if (governor == null) {
			governor = getExpressionGovernor(CompilationTimeStamp.getBaseTimestamp(), Expected_Value_type.EXPECTED_TEMPLATE);
		}
		if (governor == null) {
			return;
		}
		String genName = governor.getGenNameTemplate(aData, expression.expression, myScope);
		String tempId = aData.getTemporaryVariableName();

		expression.preamble.append(MessageFormat.format("{0} {1} = new {0}();\n", genName, tempId));
		setGenNameRecursive(tempId);
		generateCodeInit(aData, expression.preamble, tempId);
		// TODO handle template restriction
		expression.expression.append(tempId);
	}

	@Override
	/** {@inheritDoc} */
	public void generateCodeInit(final JavaGenData aData, final StringBuilder source, final String name) {
		String ofTypeName;
		switch (myGovernor.getTypetype()) {
		case TYPE_SEQUENCE_OF:
			ofTypeName = ((SequenceOf_Type) myGovernor).getOfType().getGenNameTemplate(aData, source, myScope);
			break;
		case TYPE_SET_OF:
			ofTypeName = ((SetOf_Type) myGovernor).getOfType().getGenNameTemplate(aData, source, myScope);
			break;
		case TYPE_ARRAY:
			ofTypeName = ((Array_Type) myGovernor).getElementType().getGenNameTemplate(aData, source, myScope);
			break;
		default:
			// TODO FATAL error
			return;
		}

		ArrayList<Integer> variables = new ArrayList<Integer>();
		long fixedPart = 0;
		for (int i = 0; i < templates.getNofTemplates(); i++) {
			ITemplateListItem templateListItem = templates.getTemplateByIndex(i);
			if (templateListItem.getTemplatetype() == Template_type.ALLELEMENTSFROM) {
				variables.add(i);
			} else {
				fixedPart++;
			}
		}

		if (variables.size() > 0) {
			StringBuilder preamble = new StringBuilder();
			StringBuilder setType = new StringBuilder();
			setType.append(MessageFormat.format("{0}.setType(template_sel.SUBSET_MATCH, {1}", name, fixedPart));

			for (int v = 0; v < variables.size(); v++) {
				ITemplateListItem templateListItem = templates.getTemplateByIndex(variables.get(v));
				TTCN3Template template = ((TemplateBody) templateListItem).getTemplate();
				// the template must be all from
				IValue value = ((SpecificValue_Template) template).getValue();
				Reference reference;
				if (value.getValuetype() == Value_type.UNDEFINED_LOWERIDENTIFIER_VALUE) {
					reference = ((Undefined_LowerIdentifier_Value) value).getAsReference();
				} else {
					reference = ((Referenced_Value) value).getReference();
				}
				Assignment assignment = reference.getRefdAssignment(CompilationTimeStamp.getBaseTimestamp(), false);

				setType.append(" + ");

				ExpressionStruct expression = new ExpressionStruct();
				reference.generateCode(aData, expression);
				if (expression.preamble.length() > 0) {
					preamble.append(expression.preamble);
				}
				setType.append(expression.expression);

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

				setType.append(".n_elem()");
			}

			source.append(preamble);
			source.append(setType);
			source.append(");\n");

			StringBuilder shifty = new StringBuilder();
			for (int i = 0; i < templates.getNofTemplates(); i++) {
				ITemplateListItem templateListItem = templates.getTemplateByIndex(i);
				TTCN3Template template = ((TemplateBody) templateListItem).getTemplate();

				switch (templateListItem.getTemplatetype()) {
				case ALLELEMENTSFROM: {
					// the template must be all from
					template.setLoweridToReference(CompilationTimeStamp.getBaseTimestamp());
					IValue value = ((SpecificValue_Template) template).getValue();
					Reference reference;
					if (value.getValuetype() == Value_type.UNDEFINED_LOWERIDENTIFIER_VALUE) {
						//value.getValueRefdLast(CompilationTimeStamp.getBaseTimestamp(), null);
						reference = ((Undefined_LowerIdentifier_Value) value).getAsReference();
					} else {
						reference = ((Referenced_Value) value).getReference();
					}
					Assignment assignment = reference.getRefdAssignment(CompilationTimeStamp.getBaseTimestamp(), false);

					ExpressionStruct expression = new ExpressionStruct();
					reference.generateCode(aData, expression);

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

					source.append(MessageFormat.format("for (int i_i = 0, i_lim = {0}.n_elem(); i_i < i_lim; ++i_i ) '{'\n", expression.expression));

					String embeddedName = MessageFormat.format("{0}.setItem({1}{2} + i_i)", name, i, shifty);
					((AllElementsFrom) templateListItem).generateCodeInitAllFrom(aData, source, embeddedName);
					source.append("}\n");
					shifty.append(MessageFormat.format("-1 + {0}.n_elem()", expression.expression));
					break;
				}
				default:
					if (template.needsTemporaryReference()) {
						String tempId = aData.getTemporaryVariableName();
						source.append("{\n");
						source.append(MessageFormat.format("{0} {1} = {2}.setItem({3}{4});\n", ofTypeName, tempId, name, i, shifty));
						generateCodeInit(aData, source, tempId);
						source.append("}\n");
					} else {
						String embeddedName = MessageFormat.format("{0}.setItem({1}{2})", name, i, shifty);
						template.generateCodeInit(aData, source, embeddedName);
					}
					break;
				}
			}
		} else {
			source.append(MessageFormat.format("{0}.setType(template_sel.SUBSET_MATCH, {1});\n", name, templates.getNofTemplates()));
			for (int i = 0; i < templates.getNofTemplates(); i++) {
				TTCN3Template template = templates.getTemplateByIndex(i).getTemplate();
				if (template.needsTemporaryReference()) {
					String tempId = aData.getTemporaryVariableName();
					source.append("{\n");
					source.append(MessageFormat.format("{0} {1} = {2}.setItem({3});\n", ofTypeName, tempId, name, i));
					template.generateCodeInit(aData, source, tempId);
					source.append("}\n");
				} else {
					String embeddedName = MessageFormat.format("{0}.setItem({1})", name, i);
					template.generateCodeInit(aData, source, embeddedName);
				}
			}
		}
	}
}
