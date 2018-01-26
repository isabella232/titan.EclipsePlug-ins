/******************************************************************************
 * Copyright (c) 2000-2017 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.types;

import java.text.MessageFormat;
import java.util.List;

import org.eclipse.titan.designer.AST.ArraySubReference;
import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.FieldSubReference;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.ISubReference;
import org.eclipse.titan.designer.AST.ISubReference.Subreference_type;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.IValue.Value_type;
import org.eclipse.titan.designer.AST.ParameterisedSubReference;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.Type;
import org.eclipse.titan.designer.AST.TypeCompatibilityInfo;
import org.eclipse.titan.designer.AST.Value;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.templates.ITTCN3Template;
import org.eclipse.titan.designer.AST.TTCN3.templates.ValueRange;
import org.eclipse.titan.designer.AST.TTCN3.templates.Value_Range_Template;
import org.eclipse.titan.designer.AST.TTCN3.types.subtypes.SubType;
import org.eclipse.titan.designer.AST.TTCN3.values.Charstring_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Expression_Value.Operation_type;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.ExpressionStruct;
import org.eclipse.titan.designer.compiler.JavaGenData;
import org.eclipse.titan.designer.editors.actions.DeclarationCollector;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * character string type (TTCN-3).
 *
 * @author Kristof Szabados
 * @author Arpad Lovassy
 */
public final class CharString_Type extends Type {
	private static final String CHARSTRING = "charstring";
	public static final String CHARSTRINGVALUEEXPECTED = "Character string value was expected";
	private static final String TEMPLATENOTALLOWED = "{0} cannot be used for type `{1}''";
	private static final String INCORRECTBOUNDARIES = "The lower boundary is higher than the upper boundary";
	private static final String INFINITEBOUNDARYERROR = "The {0} boundary must be a charstring value";
	private static final String TOOLONGBOUNDARYERROR = "The {0} boundary must be a charstring value containing a single character.";

	public static enum CharCoding {
		UNKNOWN,
		ASCII,
		UTF_8,
		UTF16,
		UTF16BE,
		UTF16LE,
		UTF32,
		UTF32BE,
		UTF32LE
	}

	@Override
	/** {@inheritDoc} */
	public Type_type getTypetype() {
		return Type_type.TYPE_CHARSTRING;
	}

	@Override
	/** {@inheritDoc} */
	public StringBuilder getProposalDescription(final StringBuilder builder) {
		return builder.append(CHARSTRING);
	}

	@Override
	/** {@inheritDoc} */
	public String getOutlineIcon() {
		return "charstring.gif";
	}

	@Override
	/** {@inheritDoc} */
	public boolean isCompatible(final CompilationTimeStamp timestamp, final IType otherType, final TypeCompatibilityInfo info,
			final TypeCompatibilityInfo.Chain leftChain, final TypeCompatibilityInfo.Chain rightChain) {
		check(timestamp);
		otherType.check(timestamp);
		final IType temp = otherType.getTypeRefdLast(timestamp);
		if (getIsErroneous(timestamp) || temp.getIsErroneous(timestamp)) {
			return true;
		}

		switch (temp.getTypetype()) {
		case TYPE_CHARSTRING:
		case TYPE_NUMERICSTRING:
		case TYPE_PRINTABLESTRING:
		case TYPE_IA5STRING:
		case TYPE_VISIBLESTRING:
		case TYPE_UTCTIME:
		case TYPE_GENERALIZEDTIME:
			return true;
		default:
			return false;
		}
	}

	@Override
	/** {@inheritDoc} */
	public Type_type getTypetypeTtcn3() {
		if (isErroneous) {
			return Type_type.TYPE_UNDEFINED;
		}

		return getTypetype();
	}

	@Override
	/** {@inheritDoc} */
	public String getTypename() {
		return CHARSTRING;
	}

	@Override
	/** {@inheritDoc} */
	public SubType.SubType_type getSubtypeType() {
		return SubType.SubType_type.ST_CHARSTRING;
	}

	@Override
	/** {@inheritDoc} */
	public void check(final CompilationTimeStamp timestamp) {
		if (lastTimeChecked != null && !lastTimeChecked.isLess(timestamp)) {
			return;
		}

		lastTimeChecked = timestamp;

		parseAttributes(timestamp);

		if (constraints != null) {
			constraints.check(timestamp);
		}

		checkSubtypeRestrictions(timestamp);

		if (myScope != null) {
			checkEncode(timestamp);
			checkVariants(timestamp);
		}
	}

	@Override
	/** {@inheritDoc} */
	public boolean checkThisValue(final CompilationTimeStamp timestamp, final IValue value, final Assignment lhs, final ValueCheckingOptions valueCheckingOptions) {
		final boolean selfReference = super.checkThisValue(timestamp, value, lhs, valueCheckingOptions);

		IValue last = value.getValueRefdLast(timestamp, valueCheckingOptions.expected_value, null);
		if (last == null || last.getIsErroneous(timestamp)) {
			return selfReference;
		}

		// already handled ones
		switch (value.getValuetype()) {
		case OMIT_VALUE:
		case REFERENCED_VALUE:
			return selfReference;
		case UNDEFINED_LOWERIDENTIFIER_VALUE:
			if (Value_type.REFERENCED_VALUE.equals(last.getValuetype())) {
				return selfReference;
			}
			break;
		default:
			break;
		}

		switch (last.getValuetype()) {
		case UNDEFINED_BLOCK:
			last = last.setValuetype(timestamp, Value_type.CHARSYMBOLS_VALUE);
			if (last.getIsErroneous(timestamp)) {
				return selfReference;
			}

			last.setValuetype(timestamp, Value_type.CHARSTRING_VALUE);
			break;
		case CHARSYMBOLS_VALUE:
		case UNIVERSALCHARSTRING_VALUE:
			last.setValuetype(timestamp, Value_type.CHARSTRING_VALUE);
			break;
		case CHARSTRING_VALUE:
		case ISO2022STRING_VALUE:
			break;
		case EXPRESSION_VALUE:
		case MACRO_VALUE:
			// already checked
			break;
		default:
			value.getLocation().reportSemanticError(CHARSTRINGVALUEEXPECTED);
			value.setIsErroneous(true);
		}

		if (valueCheckingOptions.sub_check) {
			//there is no parent type to check
			if (subType != null) {
				subType.checkThisValue(timestamp, last);
			}
		}

		value.setLastTimeChecked(timestamp);

		return selfReference;
	}

	@Override
	/** {@inheritDoc} */
	public boolean checkThisTemplate(final CompilationTimeStamp timestamp, final ITTCN3Template template,
			final boolean isModified, final boolean implicitOmit, final Assignment lhs) {
		registerUsage(template);
		checkThisTemplateString(timestamp, this, template, isModified);

		return false;
	}

	/**
	 * Checks if the provided template is valid for the provided type.
	 * <p>
	 * The type must be equivalent with the TTCN-3 charstring type
	 *
	 * @param timestamp the time stamp of the actual semantic check cycle.
	 * @param type the charstring type used for the check.
	 * @param template the template to be checked by the type.
	 * @param isModified true if the template is a modified template
	 * */
	public static void checkThisTemplateString(final CompilationTimeStamp timestamp, final Type type,
			final ITTCN3Template template, final boolean isModified) {
		switch (template.getTemplatetype()) {
		case VALUE_RANGE: {
			final ValueRange range = ((Value_Range_Template) template).getValueRange();
			final IValue lower = checkBoundary(timestamp, type, range.getMin(), template, "lower");
			final IValue upper = checkBoundary(timestamp, type, range.getMax(), template, "upper");
			range.setTypeType(type.getTypetypeTtcn3());

			if (lower != null && upper != null) {
				if (((Charstring_Value) lower).getValue().compareTo(((Charstring_Value) upper).getValue()) > 0) {
					template.getLocation().reportSemanticError(INCORRECTBOUNDARIES);
				}
			}
			break;
		}
		case CSTR_PATTERN:
			// TODO implement later once patterns become supported
			break;
		default:
			template.getLocation().reportSemanticError(
					MessageFormat.format(TEMPLATENOTALLOWED, template.getTemplateTypeName(), type.getTypename()));
			break;
		}
	}

	private static IValue checkBoundary(final CompilationTimeStamp timestamp, final Type type, final Value value,
			final ITTCN3Template template, final String which) {
		if (value == null) {
			template.getLocation().reportSemanticError(MessageFormat.format(INFINITEBOUNDARYERROR, which));
			return null;
		}

		value.setMyGovernor(type);
		IValue temp = type.checkThisValueRef(timestamp, value);
		type.checkThisValue(timestamp, temp, null, new ValueCheckingOptions(Expected_Value_type.EXPECTED_DYNAMIC_VALUE, false, false, true, false, false));
		temp = temp.getValueRefdLast(timestamp, Expected_Value_type.EXPECTED_DYNAMIC_VALUE, null);
		switch (temp.getValuetype()) {
		case CHARSTRING_VALUE:
			if (((Charstring_Value) temp).getValueLength() != 1) {
				value.getLocation().reportSemanticError(MessageFormat.format(TOOLONGBOUNDARYERROR, which));
			}
			break;
		default:
			temp = null;
			break;
		}

		return temp;
	}

	@Override
	/** {@inheritDoc} */
	public IType getFieldType(final CompilationTimeStamp timestamp, final Reference reference, final int actualSubReference,
			final Expected_Value_type expectedIndex, final IReferenceChain refChain, final boolean interruptIfOptional) {
		final List<ISubReference> subreferences = reference.getSubreferences();
		if (subreferences.size() <= actualSubReference) {
			return this;
		}

		final ISubReference subreference = subreferences.get(actualSubReference);
		switch (subreference.getReferenceType()) {
		case arraySubReference:
			if (subreferences.size() > actualSubReference + 1) {
				subreference.getLocation().reportSemanticError(ArraySubReference.INVALIDSTRINGELEMENTINDEX);
				return null;
			} else if (subreferences.size() == actualSubReference + 1) {
				reference.setStringElementReferencing();
			}

			final Value indexValue = ((ArraySubReference) subreference).getValue();
			checkStringIndex(timestamp, indexValue, expectedIndex, refChain);

			return this;
		case fieldSubReference:
			subreference.getLocation().reportSemanticError(
					MessageFormat.format(FieldSubReference.INVALIDSUBREFERENCE, ((FieldSubReference) subreference).getId().getDisplayName(),
							getTypename()));
			return null;
		case parameterisedSubReference:
			subreference.getLocation().reportSemanticError(
					MessageFormat.format(FieldSubReference.INVALIDSUBREFERENCE, ((ParameterisedSubReference) subreference).getId().getDisplayName(),
							getTypename()));
			return null;
		default:
			subreference.getLocation().reportSemanticError(ISubReference.INVALIDSUBREFERENCE);
			return null;
		}
	}

	@Override
	/** {@inheritDoc} */
	public int getLengthMultiplier() {
		return 8;
	}

	@Override
	/** {@inheritDoc} */
	public void addDeclaration(final DeclarationCollector declarationCollector, final int i) {
		final List<ISubReference> subreferences = declarationCollector.getReference().getSubreferences();
		if (subreferences.size() <= i) {
			return;
		}

		final ISubReference subreference = subreferences.get(i);
		if (Subreference_type.arraySubReference.equals(subreference.getReferenceType())
				&& subreferences.size() == i + 1) {
			declarationCollector.addDeclaration(CHARSTRING, location, this);

		}
	}

	@Override
	/** {@inheritDoc} */
	public void generateCode( final JavaGenData aData, final StringBuilder source ) {
		generateCodeTypedescriptor(aData, source);
		if(needsAlias()) {
			final String ownName = getGenNameOwn();
			source.append(MessageFormat.format("\tpublic static class {0} extends {1} '{' '}'\n", ownName, getGenNameValue(aData, source, myScope)));
			source.append(MessageFormat.format("\tpublic static class {0}_template extends {1} '{' '}'\n", ownName, getGenNameTemplate(aData, source, myScope)));
		}

		if (hasDoneAttribute()) {
			generateCodeDone(aData, source);
		}
		if (subType != null) {
			subType.generateCode(aData, source);
		}
	}

	@Override
	/** {@inheritDoc} */
	public String getGenNameValue(final JavaGenData aData, final StringBuilder source, final Scope scope) {
		aData.addBuiltinTypeImport( "TitanCharString" );

		return "TitanCharString";
	}

	@Override
	/** {@inheritDoc} */
	public String getGenNameTemplate(final JavaGenData aData, final StringBuilder source, final Scope scope) {
		aData.addBuiltinTypeImport( "TitanCharString_template" );

		return "TitanCharString_template";
	}

	@Override
	/** {@inheritDoc} */
	public void generateCodeIsPresentBoundChosen(final JavaGenData aData, final ExpressionStruct expression, final List<ISubReference> subreferences,
			final int subReferenceIndex, final String globalId, final String externalId, final boolean isTemplate, final Operation_type optype, final String field) {
		generateCodeIspresentBound_forStrings(aData, expression, subreferences, subReferenceIndex, globalId, externalId, isTemplate, optype, field);
	}

	@Override
	/** {@inheritDoc} */
	public String internalGetGenNameTypeDescriptor(final JavaGenData aData, final StringBuilder source, final Scope scope) {
		aData.addBuiltinTypeImport( "Base_Type" );
		return "Base_Type.TitanCharString";
	}
}
