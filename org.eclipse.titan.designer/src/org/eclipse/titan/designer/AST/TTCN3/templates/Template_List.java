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
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.IType.Type_type;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.IValue.Value_type;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.TemplateRestriction;
import org.eclipse.titan.designer.AST.TTCN3.TemplateRestriction.Restriction_type;
import org.eclipse.titan.designer.AST.TTCN3.types.Array_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.SequenceOf_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.SetOf_Type;
import org.eclipse.titan.designer.AST.TTCN3.values.ArrayDimension;
import org.eclipse.titan.designer.AST.TTCN3.values.Integer_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.SequenceOf_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Values;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.ExpressionStruct;
import org.eclipse.titan.designer.compiler.JavaGenData;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * Represents a list of templates.
 *
 * @author Kristof Szabados
 * */
public final class Template_List extends CompositeTemplate {
	/** Indicates whether the embedded templates contain PERMUTATION_MATCH. */
	private boolean hasPermutation = false;

	// cache storing the value form of this list of templates if already
	// created, or null
	private SequenceOf_Value asValue = null;

	// if assigned to a record/set the semantic checking will create a converted value.
	private TTCN3Template converted = null;

	public Template_List(final ListOfTemplates templates) {
		super(templates);

		for (int i = 0, size = templates.getNofTemplates(); i < size; i++) {
			if (Template_type.PERMUTATION_MATCH.equals(templates.getTemplateByIndex(i).getTemplatetype())) {
				hasPermutation = true;
			}
		}
	}

	@Override
	/** {@inheritDoc} */
	public Template_type getTemplatetype() {
		return Template_type.TEMPLATE_LIST;
	}

	@Override
	/** {@inheritDoc} */
	public String getTemplateTypeName() {
		if (isErroneous) {
			return "erroneous value list notation";
		}

		return "value list notation";
	}

	@Override
	/** {@inheritDoc} */
	public String createStringRepresentation() {
		final StringBuilder builder = new StringBuilder();
		builder.append("{ ");
		for (int i = 0, size = templates.getNofTemplates(); i < size; i++) {
			if (i > 0) {
				builder.append(", ");
			}

			final ITTCN3Template template = templates.getTemplateByIndex(i);
			builder.append(template.createStringRepresentation());
		}
		builder.append(" }");

		if (lengthRestriction != null) {
			builder.append(lengthRestriction.createStringRepresentation());
		}
		if (isIfpresent) {
			builder.append("ifpresent");
		}

		return builder.toString();
	}

	public boolean hasAllFrom() {
		for (int i = 0, size = templates.getNofTemplates(); i < size; i++) {
			if (Template_type.ALL_FROM.equals(templates.getTemplateByIndex(i).getTemplatetype())) {
				return true;
			}
		}

		return false;
	}

	@Override
	/** {@inheritDoc} */
	public TTCN3Template setTemplatetype(final CompilationTimeStamp timestamp, final Template_type newType) {
		switch (newType) {
		case NAMED_TEMPLATE_LIST:
			converted =  Named_Template_List.convert(timestamp, this);
			return converted;
		default:
			return super.setTemplatetype(timestamp, newType);
		}
	}

	@Override
	/** {@inheritDoc} */
	protected ITTCN3Template getReferencedArrayTemplate(final CompilationTimeStamp timestamp, final IValue arrayIndex,
			final IReferenceChain referenceChain) {
		IValue indexValue = arrayIndex.setLoweridToReference(timestamp);
		indexValue = indexValue.getValueRefdLast(timestamp, referenceChain);
		if (indexValue.getIsErroneous(timestamp)) {
			return null;
		}

		long index = 0;
		if (!indexValue.isUnfoldable(timestamp)) {
			if (Value_type.INTEGER_VALUE.equals(indexValue.getValuetype())) {
				index = ((Integer_Value) indexValue).getValue();
			} else {
				arrayIndex.getLocation().reportSemanticError("An integer value was expected as index");
				return null;
			}
		} else {
			return null;
		}

		final IType tempType = myGovernor.getTypeRefdLast(timestamp);
		if (tempType.getIsErroneous(timestamp)) {
			return null;
		}

		switch (tempType.getTypetype()) {
		case TYPE_SEQUENCE_OF: {
			if (index < 0) {
				final String message = MessageFormat
						.format("A non-negative integer value was expected instead of {0} for indexing a template of `sequence of'' type `{1}''",
								index, tempType.getTypename());
				arrayIndex.getLocation().reportSemanticError(message);
				return null;
			}

			final int nofElements = getNofTemplates();
			if (!(index < nofElements)) {
				final String message = MessageFormat
						.format("Index overflow in a template of `sequence of'' type `{0}'': the index is {1}, but the template has only {2} elements",
								tempType.getTypename(), index, nofElements);
				arrayIndex.getLocation().reportSemanticError(message		);
				return null;
			}
			break;
		}
		case TYPE_SET_OF: {
			if (index < 0) {
				final String message = MessageFormat
						.format("A non-negative integer value was expected instead of {0} for indexing a template of `set of'' type `{1}''",
								index, tempType.getTypename());
				arrayIndex.getLocation().reportSemanticError(message);
				return null;
			}

			final int nofElements = getNofTemplates();
			if (!(index < nofElements)) {
				final String message = MessageFormat
						.format("Index overflow in a template of `set of'' type `{0}'': the index is {1}, but the template has only {2} elements",
								tempType.getTypename(), index, nofElements);
				arrayIndex.getLocation().reportSemanticError(message);
				return null;
			}
			break;
		}
		case TYPE_ARRAY: {
			final ArrayDimension dimension = ((Array_Type) tempType).getDimension();
			dimension.checkIndex(timestamp, indexValue, Expected_Value_type.EXPECTED_DYNAMIC_VALUE);
			if (!dimension.getIsErroneous(timestamp)) {
				// re-base the index
				index -= dimension.getOffset();
				if (index < 0 || !(index < getNofTemplates()) ) {
					arrayIndex.getLocation().reportSemanticError(
							MessageFormat.format("The index value {0} is outside the array indexable range", index
									+ dimension.getOffset()));
					return null;
				}
			} else {
				return null;
			}
			break;
		}
		default:{
			final String message = MessageFormat.format("Invalid array element reference: type `{0}'' cannot be indexed",
					tempType.getTypename());
			arrayIndex.getLocation().reportSemanticError(message);
			return null;
		}
		}

		final ITTCN3Template returnValue = getTemplateByIndex((int) index);
		if (Template_type.TEMPLATE_NOTUSED.equals(returnValue.getTemplatetype())) {
			if (baseTemplate != null) {
				return baseTemplate.getTemplateReferencedLast(timestamp, referenceChain).getReferencedArrayTemplate(timestamp,
						indexValue, referenceChain);
			}

			return null;
		}

		return returnValue;
	}

	@Override
	/** {@inheritDoc} */
	public boolean isValue(final CompilationTimeStamp timestamp) {
		if (lengthRestriction != null || isIfpresent || getIsErroneous(timestamp)) {
			return false;
		}

		for (int i = 0, size = getNofTemplates(); i < size; i++) {
			if (!templates.getTemplateByIndex(i).isValue(timestamp)) {
				return false;
			}
		}

		return true;
	}

	@Override
	/** {@inheritDoc} */
	public IValue getValue() {
		if (asValue != null) {
			return asValue;
		}

		if (converted != null) {
			return converted.getValue();
		}

		final Values values = new Values(false);
		for (int i = 0, size = getNofTemplates(); i < size; i++) {
			values.addValue(templates.getTemplateByIndex(i).getValue());
		}

		asValue = new SequenceOf_Value(values);
		asValue.setLocation(getLocation());
		asValue.setMyScope(getMyScope());
		asValue.setFullNameParent(getNameParent());
		asValue.setMyGovernor(getMyGovernor());

		return asValue;
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
		for (int i = 0, size = templates.getNofTemplates(); i < size; i++) {
			templates.getTemplateByIndex(i).checkSpecificValue(timestamp, true);
		}
	}

	@Override
	/** {@inheritDoc} */
	protected void checkTemplateSpecificLengthRestriction(final CompilationTimeStamp timestamp, final Type_type typeType) {
		if (Type_type.TYPE_SEQUENCE_OF.equals(typeType) || Type_type.TYPE_SET_OF.equals(typeType)) {
			final int nofTemplatesGood = getNofTemplatesNotAnyornone(timestamp); //at least !

			final boolean hasAnyOrNone = templateContainsAnyornone();

			lengthRestriction.checkNofElements(timestamp, nofTemplatesGood, hasAnyOrNone, false, hasAnyOrNone, this);
		}
	}

	@Override
	/** {@inheritDoc} */
	public boolean checkValueomitRestriction(final CompilationTimeStamp timestamp, final String definitionName, final boolean omitAllowed, final Location usageLocation) {
		if (omitAllowed) {
			checkRestrictionCommon(timestamp, definitionName, TemplateRestriction.Restriction_type.TR_OMIT, usageLocation);
		} else {
			checkRestrictionCommon(timestamp, definitionName, TemplateRestriction.Restriction_type.TR_VALUE, usageLocation);
		}

		boolean needsRuntimeCheck = false;
		for (int i = 0, size = templates.getNofTemplates(); i < size; i++) {
			if (templates.getTemplateByIndex(i).checkValueomitRestriction(timestamp, definitionName, true, usageLocation)) {
				needsRuntimeCheck = true;
			}
		}
		return needsRuntimeCheck;
	}

	@Override
	/** {@inheritDoc} */
	public ITTCN3Template getReferencedSetSequenceFieldTemplate(final CompilationTimeStamp timestamp, final Identifier fieldIdentifier,
			final Reference reference, final IReferenceChain referenceChain) {
		if (converted != null) {
			return converted.getReferencedSetSequenceFieldTemplate(timestamp, fieldIdentifier, reference, referenceChain);
		}

		return null;
	}

	@Override
	/** {@inheritDoc} */
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (lengthRestriction != null) {
			lengthRestriction.findReferences(referenceFinder, foundIdentifiers);
		}

		if (asValue != null) {
			asValue.findReferences(referenceFinder, foundIdentifiers);
		} else if (templates != null) {
			templates.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	@Override
	/** {@inheritDoc} */
	protected boolean memberAccept(final ASTVisitor v) {
		if (!super.memberAccept(v)) {
			return false;
		}
		if (asValue != null) {
			if (!asValue.accept(v)) {
				return false;
			}
		}
		return true;
	}

	@Override
	/** {@inheritDoc} */
	public void setGenNamePrefix(final String prefix) {
		super.setGenNamePrefix(prefix);

		if (converted != null) {
			converted.setGenNamePrefix(prefix);
			return;
		}

		for (int i = 0; i < templates.getNofTemplates(); i++) {
			templates.getTemplateByIndex(i).setGenNamePrefix(prefix);
		}
	}

	@Override
	/** {@inheritDoc} */
	public void setGenNameRecursive(final String parameterGenName) {
		super.setGenNameRecursive(parameterGenName);

		if (converted != null) {
			converted.setGenNameRecursive(parameterGenName);
			return;
		}

		if(myGovernor == null) {
			return;
		}

		final IType type = myGovernor.getTypeRefdLast(CompilationTimeStamp.getBaseTimestamp());
		long offset = 0;
		if(Type_type.TYPE_ARRAY.equals(type.getTypetype())) {
			offset = ((Array_Type) type).getDimension().getOffset();
		}
		for (int i = 0; i < templates.getNofTemplates(); i++) {
			final StringBuilder embeddedName = new StringBuilder(parameterGenName);
			embeddedName.append('[').append(offset + i).append(']');
			templates.getTemplateByIndex(i).setGenNameRecursive(embeddedName.toString());
		}
	}

	@Override
	/** {@inheritDoc} */
	protected String getNameForStringRep() {
		return "";
	}

	@Override
	/** {@inheritDoc} */
	public boolean needsTemporaryReference() {
		if (converted != null) {
			return converted.needsTemporaryReference();
		}

		if (lengthRestriction != null || isIfpresent) {
			return true;
		}

		// temporary reference is needed if the template has at least one
		// element (excluding not used symbols)
		for (int i = 0; i < templates.getNofTemplates(); i++) {
			final TTCN3Template template = templates.getTemplateByIndex(i);
			if (template.getTemplatetype() != Template_type.TEMPLATE_NOTUSED) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Returns whether the template can be represented by an in-line
	 *  Java expression.
	 * */
	public boolean hasSingleExpression() {
		if (converted != null) {
			return converted.hasSingleExpression();
		}

		if (lengthRestriction != null || isIfpresent /* TODO:  || get_needs_conversion()*/) {
			return false;
		}

		return false;
	}

	@Override
	/** {@inheritDoc} */
	public StringBuilder getSingleExpression(final JavaGenData aData, final boolean castIsNeeded) {
		if (converted != null) {
			return converted.getSingleExpression(aData, castIsNeeded);
		}

		ErrorReporter.INTERNAL_ERROR("INTERNAL ERROR: Can not generate single expression for template list `" + getFullName() + "''");

		return new StringBuilder("FATAL_ERROR encountered");
	}

	@Override
	/** {@inheritDoc} */
	public void generateCodeExpression(final JavaGenData aData, final ExpressionStruct expression, final TemplateRestriction.Restriction_type templateRestriction) {
		if (asValue != null) {
			asValue.generateCodeExpression(aData, expression, true);
			return;
		}

		if (converted != null) {
			converted.generateCodeExpression(aData, expression, templateRestriction);
			return;
		}

		IType governor = myGovernor;
		if (governor == null) {
			governor = getExpressionGovernor(CompilationTimeStamp.getBaseTimestamp(), Expected_Value_type.EXPECTED_TEMPLATE);
		}
		if (governor == null) {
			return;
		}

		final String genName = governor.getGenNameTemplate(aData, expression.expression, myScope);
		final String tempId = aData.getTemporaryVariableName();

		expression.preamble.append(MessageFormat.format("{0} {1} = new {0}();\n", genName, tempId));
		setGenNameRecursive(tempId);
		generateCodeInit(aData, expression.preamble, tempId);

		if (templateRestriction != Restriction_type.TR_NONE) {
			TemplateRestriction.generateRestrictionCheckCode(aData, expression.expression, location, tempId, templateRestriction);
		}

		expression.expression.append(tempId);
	}

	@Override
	/** {@inheritDoc} */
	public void reArrangeInitCode(final JavaGenData aData, final StringBuilder source, final Module usageModule) {
		if (asValue != null) {
			asValue.reArrangeInitCode(aData, source, usageModule);
			return;
		}

		if (converted != null) {
			converted.reArrangeInitCode(aData, source, usageModule);
			return;
		}

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

		if (asValue != null) {
			asValue.generateCodeInit(aData, source, name);
			return;
		}

		if (converted != null) {
			converted.generateCodeInit(aData, source, name);
			return;
		}

		if (myGovernor == null) {
			return;
		}

		// TODO special case for empty list
		final IType typeLast = myGovernor.getTypeRefdLast(CompilationTimeStamp.getBaseTimestamp());
		long indexOffset = 0;
		if (typeLast.getTypetype().equals(Type_type.TYPE_ARRAY)) {
			indexOffset = ((Array_Type) typeLast).getDimension().getOffset();
		}

		String ofTypeName;
		switch(typeLast.getTypetype()) {
		case TYPE_SEQUENCE_OF:
			ofTypeName = ((SequenceOf_Type) typeLast).getOfType().getGenNameTemplate(aData, source, myScope);
			break;
		case TYPE_SET_OF:
			ofTypeName = ((SetOf_Type) typeLast).getOfType().getGenNameTemplate(aData, source, myScope);
			break;
		case TYPE_ARRAY:
			ofTypeName = ((Array_Type) typeLast).getElementType().getGenNameTemplate(aData, source, myScope);
			break;
		default:
			//TODO FATAL error
			return;
		}

		if (hasPermutation || hasAllFrom()) {
			long fixedPart = 0;
			final StringBuilder preamble = new StringBuilder();
			final StringBuilder setSize = new StringBuilder();
			final StringBuilder body = new StringBuilder();

			final String counter = aData.getTemporaryVariableName();
			body.append(MessageFormat.format("int {0} = 0;\n", counter));

			for (int i = 0, size = templates.getNofTemplates(); i < size; i++) {
				final TTCN3Template template = templates.getTemplateByIndex(i);
				if (template.getTemplatetype() == Template_type.ALL_FROM) {
					final TTCN3Template subTemplate = ((All_From_Template) template).getAllFrom();
					final Reference reference = ((SpecificValue_Template) subTemplate).getReference();
					final Assignment assignment = reference.getRefdAssignment(CompilationTimeStamp.getBaseTimestamp(), false);

					setSize.append(" + ");
					final ExpressionStruct sizeExpression = new ExpressionStruct();
					final ExpressionStruct bodyExpression = new ExpressionStruct();
					reference.generateCode(aData, sizeExpression);
					reference.generateCode(aData, bodyExpression);
					setSize.append(sizeExpression.expression);
					if (sizeExpression.preamble.length() > 0) {
						preamble.append(sizeExpression.preamble);
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
							setSize.append(".get()");
						}
						break;
					default:
						break;
					}

					setSize.append(".n_elem().getInt()");
					body.append(MessageFormat.format("for (int i_i = 0, i_lim = {0}.n_elem().getInt(); i_i < i_lim; ++i_i ) '{'\n", bodyExpression.expression));

					final String embeddedName = MessageFormat.format("{0}.setItem({1} + i_i)", name, counter);
					((All_From_Template) template).generateCodeInitAllFrom(aData, body, embeddedName);
					body.append("}\n");
					body.append(MessageFormat.format("{0} += {1}.n_elem().getInt();\n", counter, bodyExpression.expression));
				} else if (template.getTemplatetype() == Template_type.PERMUTATION_MATCH) {
					final int numPermutations = ((PermutationMatch_Template) template).getNofTemplates();
					final String permutationStart = aData.getTemporaryVariableName();
					body.append(MessageFormat.format("int {0} = {1};\n", permutationStart, counter));
					for (int j = 0; j < numPermutations; j++) {
						final TTCN3Template template2 = ((PermutationMatch_Template) template).getTemplateByIndex(j);
						if (template2.getTemplatetype() == Template_type.ALL_FROM) {
							final TTCN3Template subTemplate = ((All_From_Template) template2).getAllFrom();
							final Reference reference = ((SpecificValue_Template) subTemplate).getReference();
							final Assignment assignment = reference.getRefdAssignment(CompilationTimeStamp.getBaseTimestamp(), false);

							setSize.append(" + ");
							final ExpressionStruct sizeExpression = new ExpressionStruct();
							final ExpressionStruct bodyExpression = new ExpressionStruct();
							reference.generateCode(aData, sizeExpression);
							reference.generateCode(aData, bodyExpression);
							setSize.append(sizeExpression.expression);
							if (sizeExpression.preamble.length() > 0) {
								preamble.append(sizeExpression.preamble);
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
									setSize.append(".get()");
								}
								break;
							default:
								break;
							}

							setSize.append(".n_elem().getInt()");
							body.append(MessageFormat.format("for (int i_i = 0, i_lim = {0}.n_elem().getInt(); i_i < i_lim; ++i_i ) '{'\n", bodyExpression.expression));

							final String embeddedName = MessageFormat.format("{0}.setItem({1} + i_i)", name, counter);
							((All_From_Template) template2).generateCodeInitAllFrom(aData, body, embeddedName);
							body.append("}\n");

							body.append(MessageFormat.format("{0} += {1}.n_elem().getInt();\n", counter, bodyExpression.expression));
							template2.lastTimeBuilt = aData.buildTimestamp;
						} else {
							fixedPart++;
							template2.generateCodeInitSeofElement(aData, body, name, counter, ofTypeName);
							body.append(MessageFormat.format("{0}++;\n", counter));
						}
					}

					// do not consider index_offset in case of permutation indicators
					body.append(MessageFormat.format("{0}.add_permutation({1}, {2} - 1);\n", name, permutationStart, counter));
					template.lastTimeBuilt = aData.buildTimestamp;
				} else {
					fixedPart++;
					template.generateCodeInitSeofElement(aData, body, name, counter, ofTypeName);
					body.append(MessageFormat.format("{0}++;\n", counter));
				}
			}

			source.append(preamble);

			source.append(MessageFormat.format("{0}.setSize({1}", name, fixedPart));
			source.append(setSize);
			source.append(");\n");
			source.append(body);
		} else {
			//source.append(MessageFormat.format("{0}.setSize({1});\n", name, getNofTemplates()));

			int index = 0;
			for (int i = 0, size = templates.getNofTemplates(); i < size; i++) {
				final TTCN3Template template = templates.getTemplateByIndex(i);
				switch (template.getTemplatetype()) {
				case PERMUTATION_MATCH:
					final PermutationMatch_Template actualTemplate = (PermutationMatch_Template) template;
					final int nofPermutatedTemplates = actualTemplate.getNofTemplates();
					for (int j = 0; j < nofPermutatedTemplates; j++) {
						final long ix = indexOffset + index + j;
						final TTCN3Template template2 = actualTemplate.getTemplateByIndex(j);
						template2.generateCodeInitSeofElement(aData, source, name, Long.toString(ix), ofTypeName);
					}
					// do not consider index_offset in case of permutation indicators
					source.append(MessageFormat.format("{0}.add_permutation({1}, {2});\n", name, index, index + nofPermutatedTemplates - 1));
					template.lastTimeBuilt = aData.buildTimestamp;
					index += nofPermutatedTemplates;
					break;
				case ALL_FROM:
				case TEMPLATE_NOTUSED:
					index++;
					break;
				default:
					template.generateCodeInitSeofElement(aData, source, name, Long.toString(indexOffset + index), ofTypeName);
					index++;
					break;
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
