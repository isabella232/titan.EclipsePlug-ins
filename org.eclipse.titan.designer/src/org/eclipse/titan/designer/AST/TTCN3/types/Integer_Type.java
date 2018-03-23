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
import org.eclipse.titan.designer.AST.TTCN3.attributes.RawAST;
import org.eclipse.titan.designer.AST.TTCN3.templates.ITTCN3Template;
import org.eclipse.titan.designer.AST.TTCN3.templates.TTCN3Template;
import org.eclipse.titan.designer.AST.TTCN3.templates.ValueList_Template;
import org.eclipse.titan.designer.AST.TTCN3.templates.ValueRange;
import org.eclipse.titan.designer.AST.TTCN3.templates.Value_Range_Template;
import org.eclipse.titan.designer.AST.TTCN3.types.subtypes.SubType;
import org.eclipse.titan.designer.AST.TTCN3.values.Integer_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Real_Value;
import org.eclipse.titan.designer.compiler.JavaGenData;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * @author Kristof Szabados
 * @author Arpad Lovassy
 */
public final class Integer_Type extends Type {
	public static final String INTEGERVALUEEXPECTED = "integer value was expected";
	private static final String TEMPLATENOTALLOWED = "{0} cannot be used for type `integer''";
	private static final String LENGTHRESTRICTIONNOTALLOWED = "Length restriction is not allowed for type `integer''";
	private static final String INCORRECTBOUNDARIES = "The lower boundary is higher than the upper boundary";
	private static final String INCORRECTLOWERBOUNDARY = "The lower boundary cannot be infinity";
	private static final String INCORRECTUPPERBOUNDARY = "The upper boundary cannot be -infinity";

	private static enum BOUNDARY_TYPE {
		LOWER, UPPER
	}

	@Override
	/** {@inheritDoc} */
	public Type_type getTypetype() {
		return Type_type.TYPE_INTEGER;
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

		return Type_type.TYPE_INTEGER.equals(temp.getTypetype()) || Type_type.TYPE_INTEGER_A.equals(temp.getTypetype());
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
		return "integer";
	}

	@Override
	/** {@inheritDoc} */
	public String getOutlineIcon() {
		return "integer.gif";
	}

	@Override
	/** {@inheritDoc} */
	public SubType.SubType_type getSubtypeType() {
		return SubType.SubType_type.ST_INTEGER;
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

		final IValue last = value.getValueRefdLast(timestamp, valueCheckingOptions.expected_value, null);
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
		case INTEGER_VALUE:
			break;
		case EXPRESSION_VALUE:
		case MACRO_VALUE:
			// already checked
			break;
		default:
			value.getLocation().reportSemanticError(INTEGERVALUEEXPECTED);
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

	/**
	 * Checks if a given value is a valid integer limit.
	 * <p>
	 * The special float values infinity and -infinity are valid integer range limits too.
	 *
	 * @param timestamp the time stamp of the actual semantic check cycle.
	 * @param value the value to be checked
	 * @param expectedValue the kind of the value to be expected
	 * @param incompleteAllowed true if an incomplete value can be accepted at the given location, false otherwise
	 * @param omitAllowed true if the omit value can be accepted at the given location, false otherwise
	 * @param subCheck true if the subtypes should also be checked.
	 * @param implicitOmit true if the implicit omit optional attribute was set for the value, false otherwise
	 * */
	public void checkThisValueLimit(final CompilationTimeStamp timestamp, final IValue value, final Expected_Value_type expectedValue,
			final boolean incompleteAllowed, final boolean omitAllowed, final boolean subCheck, final boolean implicitOmit) {
		super.checkThisValue(timestamp, value, null, new ValueCheckingOptions(expectedValue, incompleteAllowed, omitAllowed, subCheck, implicitOmit, false));

		final IValue last = value.getValueRefdLast(timestamp, expectedValue, null);
		if (last == null || last.getIsErroneous(timestamp)) {
			return;
		}

		// already handled ones
		switch (value.getValuetype()) {
		case OMIT_VALUE:
		case REFERENCED_VALUE:
			return;
		case UNDEFINED_LOWERIDENTIFIER_VALUE:
			if (Value_type.REFERENCED_VALUE.equals(last.getValuetype())) {
				return;
			}
			break;
		default:
			break;
		}

		switch (last.getValuetype()) {
		case INTEGER_VALUE:
			break;
		case REAL_VALUE: {
			final Real_Value real = (Real_Value) last;
			if (!real.isNegativeInfinity() && !real.isPositiveInfinity()) {
				value.getLocation().reportSemanticError(INTEGERVALUEEXPECTED);
				value.setIsErroneous(true);
			}
			break;
		}
		case EXPRESSION_VALUE:
		case MACRO_VALUE:
			// already checked
			break;
		default:
			value.getLocation().reportSemanticError(INTEGERVALUEEXPECTED);
			value.setIsErroneous(true);
		}

		if (subCheck) {
			//there is no parent type to check
			if (subType != null) {
				subType.checkThisValue(timestamp, last);
			}
		}
	}

	@Override
	/** {@inheritDoc} */
	public boolean checkThisTemplate(final CompilationTimeStamp timestamp, final ITTCN3Template template,
			final boolean isModified, final boolean implicitOmit, final Assignment lhs) {
		registerUsage(template);
		template.setMyGovernor(this);

		if (getIsErroneous(timestamp)) {
			return false;
		}

		boolean selfReference = false;
		switch (template.getTemplatetype()) {
		case VALUE_RANGE:
			final ValueRange range = ((Value_Range_Template) template).getValueRange();
			final IValue lower = checkBoundary(timestamp, range.getMin(),BOUNDARY_TYPE.LOWER);
			final IValue upper = checkBoundary(timestamp, range.getMax(),BOUNDARY_TYPE.UPPER);
			range.setTypeType(getTypetypeTtcn3());

			// Template references are not checked.
			if (lower != null && Value.Value_type.INTEGER_VALUE.equals(lower.getValuetype()) && upper != null
					&& Value.Value_type.INTEGER_VALUE.equals(upper.getValuetype())) {
				if (!getIsErroneous(timestamp) && ((Integer_Value) lower).getValue() > ((Integer_Value) upper).getValue()) {
					template.getLocation().reportSemanticError(INCORRECTBOUNDARIES);
					template.setIsErroneous(true);
				}
			}
			//TODO some checks are still missing
			break;
		case VALUE_LIST:
			final ValueList_Template temp = (ValueList_Template) template;
			for (int i = 0; i < temp.getNofTemplates(); i++){
				final TTCN3Template tmp = temp.getTemplateByIndex(i);
				selfReference |= checkThisTemplate(timestamp,tmp,isModified,implicitOmit, lhs);
			}
			break;
		case ANY_OR_OMIT:
		case ANY_VALUE:
			//Allowed
			break;
		default:
			template.getLocation().reportSemanticError(MessageFormat.format(TEMPLATENOTALLOWED, template.getTemplateTypeName()));
			template.setIsErroneous(true);
		}

		if (template.getLengthRestriction() != null) {
			template.getLocation().reportSemanticError(LENGTHRESTRICTIONNOTALLOWED);
			template.setIsErroneous(true);
		}

		return selfReference;
	}

	private IValue checkBoundary(final CompilationTimeStamp timestamp, final Value value, final BOUNDARY_TYPE btype ) {
		if (value == null) {
			return null;
		}

		value.setMyGovernor(this);
		IValue temp = checkThisValueRef(timestamp, value);
		checkThisValueLimit(timestamp, temp, Expected_Value_type.EXPECTED_DYNAMIC_VALUE, false, false, true, false);
		temp = temp.getValueRefdLast(timestamp, Expected_Value_type.EXPECTED_DYNAMIC_VALUE, null);

		if(Value_type.REAL_VALUE.equals(temp.getValuetype())) {
			if( ((Real_Value) temp).isNegativeInfinity() ) {
				if( BOUNDARY_TYPE.UPPER.equals(btype)) {
					value.getLocation().reportSemanticError(INCORRECTUPPERBOUNDARY);
					value.setIsErroneous(true);
				}
				return temp;
			} else if( ((Real_Value) temp).isPositiveInfinity() ) {
				if( BOUNDARY_TYPE.LOWER.equals(btype)) {
					value.getLocation().reportSemanticError(INCORRECTLOWERBOUNDARY);
					value.setIsErroneous(true);
				}
				return temp;
			} else {
				value.getLocation().reportSemanticError(INTEGERVALUEEXPECTED);
				value.setIsErroneous(true);
				return null;
			}
		}

		switch (temp.getValuetype()) {
		case INTEGER_VALUE:
			break;
		default:
			temp = null;
			break;
		}

		return temp;
	}

	@Override
	/** {@inheritDoc} */
	public void checkCodingAttributes(final CompilationTimeStamp timestamp, final IReferenceChain refChain) {
		//check raw attributes
		if (subType != null) {
			final int restrictionLength = subType.get_length_restriction();
			if (restrictionLength != -1) {
				if (rawAttribute == null) {
					rawAttribute = new RawAST(getDefaultRawFieldLength());
				}

				rawAttribute.length_restriction = restrictionLength;
			}
		}
		if (rawAttribute != null) {
			if (rawAttribute.intX) {
				rawAttribute.bitorderinfield = RawAST.XDEFMSB;
				rawAttribute.bitorderinoctet = RawAST.XDEFMSB;
				rawAttribute.byteorder = RawAST.XDEFMSB;
			}
		}
		//TODO add checks for other encodings.
	}

	@Override
	/** {@inheritDoc} */
	public boolean canHaveCoding(final CompilationTimeStamp timestamp, final MessageEncoding_type coding, final IReferenceChain refChain) {
		if (coding == MessageEncoding_type.BER) {
			return hasEncoding(timestamp, MessageEncoding_type.BER, null);
		}

		switch (coding) {
		case RAW:
		case TEXT:
		case JSON:
		case XER:
			return true;
		default:
			return false;
		}
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
			subreference.getLocation().reportSemanticError(MessageFormat.format(ArraySubReference.INVALIDSUBREFERENCE, getTypename()));
			return null;
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
	public int getDefaultRawFieldLength() {
		return 8;
	}

	@Override
	/** {@inheritDoc} */
	public int getRawLength() {
		if (rawAttribute != null) {
			return rawAttribute.fieldlength;
		}

		return getDefaultRawFieldLength();
	}

	@Override
	/** {@inheritDoc} */
	public StringBuilder getProposalDescription(final StringBuilder builder) {
		return builder.append("integer");
	}

	@Override
	/** {@inheritDoc} */
	public void generateCode( final JavaGenData aData, final StringBuilder source  ) {
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

		generateCodeForCodingHandlers(aData, source);
	}

	@Override
	/** {@inheritDoc} */
	public String getGenNameValue(final JavaGenData aData, final StringBuilder source , final Scope scope) {
		aData.addBuiltinTypeImport( "TitanInteger" );
		return "TitanInteger";
	}

	@Override
	/** {@inheritDoc} */
	public String getGenNameTemplate(final JavaGenData aData, final StringBuilder source, final Scope scope) {
		aData.addBuiltinTypeImport( "TitanInteger_template" );
		return "TitanInteger_template";
	}

	@Override
	/** {@inheritDoc} */
	public String internalGetGenNameTypeDescriptor(final JavaGenData aData, final StringBuilder source, final Scope scope) {
		aData.addBuiltinTypeImport( "Base_Type" );
		return "Base_Type.TitanInteger";
	}

	@Override
	/** {@inheritDoc} */
	public String getGenNameRawDescriptor(final JavaGenData aData, final StringBuilder source) {
		if (rawAttribute == null) {
			aData.addBuiltinTypeImport( "RAW" );

			return "RAW.TitanInteger_raw_";
		} else {
			generateCodeRawDescriptor(aData, source);

			return getGenNameOwn(myScope) + "_raw_";
		}
	}
}
