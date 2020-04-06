/******************************************************************************
 * Copyright (c) 2000-2020 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.types;

import java.math.BigInteger;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.titan.designer.AST.ArraySubReference;
import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.FieldSubReference;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.IReferenceableElement;
import org.eclipse.titan.designer.AST.ISubReference;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.IValue.Value_type;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.ParameterisedSubReference;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.ReferenceChain;
import org.eclipse.titan.designer.AST.Type;
import org.eclipse.titan.designer.AST.TypeCompatibilityInfo;
import org.eclipse.titan.designer.AST.Value;
import org.eclipse.titan.designer.AST.ASN1.ASN1Type;
import org.eclipse.titan.designer.AST.ASN1.IASN1Type;
import org.eclipse.titan.designer.AST.ASN1.types.ASN1_Sequence_Type;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.attributes.JsonAST;
import org.eclipse.titan.designer.AST.TTCN3.attributes.RawAST;
import org.eclipse.titan.designer.AST.TTCN3.attributes.RawASTStruct;
import org.eclipse.titan.designer.AST.TTCN3.templates.ITTCN3Template;
import org.eclipse.titan.designer.AST.TTCN3.templates.ITTCN3Template.Completeness_type;
import org.eclipse.titan.designer.AST.TTCN3.templates.IndexedTemplate;
import org.eclipse.titan.designer.AST.TTCN3.templates.Indexed_Template_List;
import org.eclipse.titan.designer.AST.TTCN3.templates.PermutationMatch_Template;
import org.eclipse.titan.designer.AST.TTCN3.templates.SubsetMatch_Template;
import org.eclipse.titan.designer.AST.TTCN3.templates.SupersetMatch_Template;
import org.eclipse.titan.designer.AST.TTCN3.templates.Template_List;
import org.eclipse.titan.designer.AST.TTCN3.types.subtypes.SubType;
import org.eclipse.titan.designer.AST.TTCN3.values.ArrayDimension;
import org.eclipse.titan.designer.AST.TTCN3.values.Integer_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.SequenceOf_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.SetOf_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.ExpressionStruct;
import org.eclipse.titan.designer.compiler.JavaGenData;
import org.eclipse.titan.designer.declarationsearch.Declaration;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * @author Kristof Szabados
 * @author Arpad Lovassy
 */
public final class SequenceOf_Type extends AbstractOfType implements IReferenceableElement {
	public static final String SEQOFVALUEEXPECTED = "SEQUENCE OF value was expected";
	public static final String RECORDOFVALUEEXPECTED = "record of value was expected";
	private static final String TEMPLATENOTALLOWED = "{0} cannot be used for recordof type `{1}''";
	private static final String REDUNDANTLENGTHRESTRICTION = "Redundant usage of length restriction with `omit''";
	public static final String NOTUSEDNOTALLOWED1 = "Not used symbol `-' is not allowed in this context";
	public static final String NOTUSEDNOTALLOWED2 = "Not used symbol `-' cannot be used here"
			+ " because there is no corresponding element in the base template";
	public static final String TOOBIGINDEXTEMPLATE = "An integer value less than `{0}'' was expected for indexing type `{1}''"
			+ " instead of `{2}''";
	public static final String NONNEGATIVEINDEXEXPECTEDTEMPLATE = "A non-negative integer value was expected for indexing type `{0}''"
			+ " instead of `{1}''";
	public static final String DUPLICATEINDEX = "Duplicate index value `{0}'' for component `{1}'' and `{2}''";
	public static final String NONNEGATIVINDEXEXPECTED = "A non-negative integer value was expected as index instead of `{0}''";
	public static final String TOOBIGINDEX = "Integer value `{0}'' is too big for indexing type `{1}''";
	public static final String INTEGERINDEXEXPECTED = "The index should be an integer value";

	private static final String NOTCOMPATIBLESETSETOF = "set/SET and set of/SET OF types are compatible only"
			+ " with other set/SET and set of/SET OF types";
	private static final String NOTCOMPATIBLEUNIONANYTYPE = "union/CHOICE/anytype types are compatible only"
			+ " with other union/CHOICE/anytype types";

	public SequenceOf_Type(final IType ofType) {
		super(ofType);
	}

	@Override
	/** {@inheritDoc} */
	public Type_type getTypetype() {
		return Type_type.TYPE_SEQUENCE_OF;
	}

	@Override
	/** {@inheritDoc} */
	public IASN1Type newInstance() {
		if (getOfType() instanceof ASN1Type) {
			return new SequenceOf_Type(((IASN1Type) getOfType()).newInstance());
		}

		return this;
	}

	@Override
	/** {@inheritDoc} */
	public boolean isCompatible(final CompilationTimeStamp timestamp, final IType otherType, final TypeCompatibilityInfo info,
			final TypeCompatibilityInfo.Chain leftChain, final TypeCompatibilityInfo.Chain rightChain) {
		check(timestamp);
		otherType.check(timestamp);
		final IType lastOtherType = otherType.getTypeRefdLast(timestamp);

		if (getIsErroneous(timestamp) || lastOtherType.getIsErroneous(timestamp) || this == lastOtherType) {
			return true;
		}

		if (info == null || noStructuredTypeCompatibility) {
			//There is another chance to be compatible:
			//If records of/sets of are strongly compatible, then the records of/sets of are compatible
			final IType last = getTypeRefdLast(timestamp);
			return last.isStronglyCompatible(timestamp, lastOtherType, info, leftChain, rightChain);
		}

		switch (lastOtherType.getTypetype()) {
		case TYPE_ASN1_SEQUENCE: {
			if (!isSubtypeCompatible(timestamp, lastOtherType)) {
				info.setErrorStr("Incompatible record of/SEQUENCE OF subtypes");
				return false;
			}

			final ASN1_Sequence_Type tempType = (ASN1_Sequence_Type) lastOtherType;
			final int tempTypeNofComps = tempType.getNofComponents();
			if (tempTypeNofComps == 0) {
				return false;
			}
			TypeCompatibilityInfo.Chain lChain = leftChain;
			TypeCompatibilityInfo.Chain rChain = rightChain;
			if (lChain == null) {
				lChain = info.getChain();
				lChain.add(this);
			}
			if (rChain == null) {
				rChain = info.getChain();
				rChain.add(tempType);
			}
			for (int i = 0; i < tempTypeNofComps; i++) {
				final CompField tempTypeCf = tempType.getComponentByIndex(i);
				final IType tempTypeCfType = tempTypeCf.getType().getTypeRefdLast(timestamp);
				final IType ofType = getOfType().getTypeRefdLast(timestamp);
				final TypeCompatibilityInfo infoTemp = new TypeCompatibilityInfo(ofType, tempTypeCfType, false);
				lChain.markState();
				rChain.markState();
				lChain.add(ofType);
				rChain.add(tempTypeCfType);
				if (!ofType.equals(tempTypeCfType)
						&& !(lChain.hasRecursion() && rChain.hasRecursion())
						&& !ofType.isCompatible(timestamp, tempTypeCfType, infoTemp, lChain, rChain)) {
					if (infoTemp.getOp1RefStr().length() > 0) {
						info.appendOp1Ref("[]");
					}
					info.appendOp1Ref(infoTemp.getOp1RefStr());
					info.appendOp2Ref("." + tempTypeCf.getIdentifier().getDisplayName() + infoTemp.getOp2RefStr());
					info.setOp1Type(infoTemp.getOp1Type());
					info.setOp2Type(infoTemp.getOp2Type());
					info.setErrorStr(infoTemp.getErrorStr());
					lChain.previousState();
					rChain.previousState();
					return false;
				}
				lChain.previousState();
				rChain.previousState();
			}
			info.setNeedsConversion(true);
			return true;
		}
		case TYPE_TTCN3_SEQUENCE: {
			if (!isSubtypeCompatible(timestamp, lastOtherType)) {
				info.setErrorStr("Incompatible record of/SEQUENCE OF subtypes");
				return false;
			}

			final TTCN3_Sequence_Type tempType = (TTCN3_Sequence_Type) lastOtherType;
			final int tempTypeNofComps = tempType.getNofComponents();
			if (tempTypeNofComps == 0) {
				return false;
			}
			TypeCompatibilityInfo.Chain lChain = leftChain;
			TypeCompatibilityInfo.Chain rChain = rightChain;
			if (lChain == null) {
				lChain = info.getChain();
				lChain.add(this);
			}
			if (rChain == null) {
				rChain = info.getChain();
				rChain.add(tempType);
			}
			for (int i = 0; i < tempTypeNofComps; i++) {
				final CompField tempTypeCf = tempType.getComponentByIndex(i);
				final IType tempTypeCfType = tempTypeCf.getType().getTypeRefdLast(timestamp);
				final IType ofType = getOfType().getTypeRefdLast(timestamp);
				final TypeCompatibilityInfo infoTemp = new TypeCompatibilityInfo(ofType, tempTypeCfType, false);
				lChain.markState();
				rChain.markState();
				lChain.add(ofType);
				rChain.add(tempTypeCfType);
				if (!ofType.equals(tempTypeCfType)
						&& !(lChain.hasRecursion() && rChain.hasRecursion())
						&& !ofType.isCompatible(timestamp, tempTypeCfType, infoTemp, lChain, rChain)) {
					if (infoTemp.getOp1RefStr().length() > 0) {
						info.appendOp1Ref("[]");
					}
					info.appendOp1Ref(infoTemp.getOp1RefStr());
					info.appendOp2Ref("." + tempTypeCf.getIdentifier().getDisplayName() + infoTemp.getOp2RefStr());
					info.setOp1Type(infoTemp.getOp1Type());
					info.setOp2Type(infoTemp.getOp2Type());
					info.setErrorStr(infoTemp.getErrorStr());
					lChain.previousState();
					rChain.previousState();
					return false;
				}
				lChain.previousState();
				rChain.previousState();
			}
			info.setNeedsConversion(true);
			return true;
		}
		case TYPE_SEQUENCE_OF: {
			if (!isSubtypeCompatible(timestamp, lastOtherType)) {
				info.setErrorStr("Incompatible record of/SEQUENCE OF subtypes");
				return false;
			}

			final SequenceOf_Type tempType = (SequenceOf_Type) lastOtherType;
			if (this == tempType) {
				return true;
			}

			final IType tempTypeOfType = tempType.getOfType().getTypeRefdLast(timestamp);
			final IType ofType = getOfType().getTypeRefdLast(timestamp);
			TypeCompatibilityInfo.Chain lChain = leftChain;
			TypeCompatibilityInfo.Chain rChain = rightChain;
			if (lChain == null) {
				lChain = info.getChain();
				lChain.add(this);
			}
			if (rChain == null) {
				rChain = info.getChain();
				rChain.add(tempType);
			}
			lChain.markState();
			rChain.markState();
			lChain.add(ofType);
			rChain.add(tempTypeOfType);
			final TypeCompatibilityInfo infoTemp = new TypeCompatibilityInfo(ofType, tempTypeOfType, false);
			if (!ofType.equals(tempTypeOfType)
					&& !(lChain.hasRecursion() && rChain.hasRecursion())
					&& !ofType.isCompatible(timestamp, tempTypeOfType, infoTemp, lChain, rChain)) {
				// Record of types can't do anything to check if they're
				// compatible with other record of types in compile-time since
				// we don't have length restrictions here.  No compile-time
				// checks, only add the "[]" to indicate that it's a record of
				// type.
				if (info.getOp1RefStr().length() > 0) {
					info.appendOp1Ref("[]");
				}
				if (info.getOp2RefStr().length() > 0) {
					info.appendOp2Ref("[]");
				}
				info.appendOp1Ref(infoTemp.getOp1RefStr());
				info.appendOp2Ref(infoTemp.getOp2RefStr());
				info.setOp1Type(infoTemp.getOp1Type());
				info.setOp2Type(infoTemp.getOp2Type());
				info.setErrorStr(infoTemp.getErrorStr());
				lChain.previousState();
				rChain.previousState();
				return false;
			}
			info.setNeedsConversion(true);
			lChain.previousState();
			rChain.previousState();
			return true;
		}
		case TYPE_ARRAY: {
			if (!isSubtypeCompatible(timestamp, lastOtherType)) {
				info.setErrorStr("Incompatible record of/SEQUENCE OF subtypes");
				return false;
			}

			final Array_Type tempType = (Array_Type) lastOtherType;
			final IType tempTypeElementType = tempType.getElementType().getTypeRefdLast(timestamp);
			final IType ofType = getOfType().getTypeRefdLast(timestamp);
			TypeCompatibilityInfo.Chain lChain = leftChain;
			TypeCompatibilityInfo.Chain rChain = rightChain;
			if (lChain == null) {
				lChain = info.getChain();
				lChain.add(this);
			}
			if (rChain == null) {
				rChain = info.getChain();
				rChain.add(tempType);
			}
			lChain.markState();
			rChain.markState();
			lChain.add(ofType);
			rChain.add(tempTypeElementType);
			final TypeCompatibilityInfo infoTemp = new TypeCompatibilityInfo(ofType, tempTypeElementType, false);
			if (!ofType.equals(tempTypeElementType)
					&& !(lChain.hasRecursion() && rChain.hasRecursion())
					&& !ofType.isCompatible(timestamp, tempTypeElementType, infoTemp, lChain, rChain)) {
				if (infoTemp.getOp1RefStr().length() > 0) {
					info.appendOp1Ref("[]");
				}
				info.appendOp1Ref(infoTemp.getOp1RefStr());
				info.appendOp2Ref(infoTemp.getOp2RefStr());
				info.setOp1Type(infoTemp.getOp1Type());
				info.setOp2Type(infoTemp.getOp2Type());
				info.setErrorStr(infoTemp.getErrorStr());
				lChain.previousState();
				rChain.previousState();
				return false;
			}
			info.setNeedsConversion(true);
			lChain.previousState();
			rChain.previousState();
			return true;
		}
		case TYPE_ASN1_CHOICE:
		case TYPE_TTCN3_CHOICE:
		case TYPE_ANYTYPE:
			info.setErrorStr(NOTCOMPATIBLEUNIONANYTYPE);
			return false;
		case TYPE_ASN1_SET:
		case TYPE_TTCN3_SET:
		case TYPE_SET_OF:
			info.setErrorStr(NOTCOMPATIBLESETSETOF);
			return false;
		default:
			return false;
		}
	}

	@Override
	/** {@inheritDoc} */
	public boolean isStronglyCompatible(final CompilationTimeStamp timestamp, final IType otherType, final TypeCompatibilityInfo info,
			final TypeCompatibilityInfo.Chain leftChain, final TypeCompatibilityInfo.Chain rightChain) {

		final IType lastOtherType = otherType.getTypeRefdLast(timestamp);
		if (Type_type.TYPE_SEQUENCE_OF.equals(lastOtherType.getTypetype())) {
			final IType oftOther = ((SequenceOf_Type) lastOtherType).getOfType();
			final IType oft = getOfType().getTypeRefdLast(timestamp); // type of the
			// fields
			if (oft != null && oftOther != null) {
				// For basic types pre-generated seq/set of is applied in titan:
				switch (oft.getTypetype()) {
				case TYPE_BOOL:
				case TYPE_BITSTRING:
				case TYPE_OCTETSTRING:
				case TYPE_INTEGER:
				case TYPE_REAL:
				case TYPE_CHARSTRING:
				case TYPE_HEXSTRING:
				case TYPE_UCHARSTRING:
				case TYPE_INTEGER_A:
				case TYPE_ASN1_ENUMERATED:
				case TYPE_BITSTRING_A:
				case TYPE_UTF8STRING:
				case TYPE_NUMERICSTRING:
				case TYPE_PRINTABLESTRING:
				case TYPE_TELETEXSTRING:
				case TYPE_VIDEOTEXSTRING:
				case TYPE_IA5STRING:
				case TYPE_GRAPHICSTRING:
				case TYPE_VISIBLESTRING:
				case TYPE_GENERALSTRING:
				case TYPE_UNIVERSALSTRING:
				case TYPE_BMPSTRING:
				case TYPE_UNRESTRICTEDSTRING:
				case TYPE_UTCTIME:
				case TYPE_GENERALIZEDTIME:
				case TYPE_OBJECTDESCRIPTOR:
					if (oft.isStronglyCompatible(timestamp, oftOther, info, leftChain, rightChain)) {
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
	public String getOutlineIcon() {
		return "record_of.gif";
	}

	@Override
	/** {@inheritDoc} */
	public SubType.SubType_type getSubtypeType() {
		return SubType.SubType_type.ST_RECORDOF;
	}

	@Override
	/** {@inheritDoc} */
	public boolean checkThisValue(final CompilationTimeStamp timestamp, final IValue value, final Assignment lhs, final ValueCheckingOptions valueCheckingOptions) {
		if (getIsErroneous(timestamp)) {
			return false;
		}

		boolean selfReference = super.checkThisValue(timestamp, value, lhs, valueCheckingOptions);

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

		if (Value_type.UNDEFINED_BLOCK.equals(last.getValuetype())) {
			last = last.setValuetype(timestamp, Value_type.SEQUENCEOF_VALUE);
		}
		if (last.getIsErroneous(timestamp)) {
			return selfReference;
		}

		switch (last.getValuetype()) {
		case SEQUENCEOF_VALUE: {
			selfReference = checkThisValueSequenceOf(timestamp, (SequenceOf_Value) last, lhs, valueCheckingOptions.expected_value,
					valueCheckingOptions.incomplete_allowed, valueCheckingOptions.implicit_omit, valueCheckingOptions.str_elem);
			break;
		}
		case SETOF_VALUE: {
			selfReference = checkThisValueSetOf(timestamp, (SetOf_Value) last, lhs, valueCheckingOptions.expected_value,
					valueCheckingOptions.incomplete_allowed, valueCheckingOptions.implicit_omit, valueCheckingOptions.str_elem);
			break;
		}
		case EXPRESSION_VALUE:
		case MACRO_VALUE:
			// already checked
			break;
		default:
			if (value.isAsn()) {
				value.getLocation().reportSemanticError(SEQOFVALUEEXPECTED);
			} else {
				value.getLocation().reportSemanticError(RECORDOFVALUEEXPECTED);
			}

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
	 * Checks the SequenceOf_value kind value against this type.
	 * <p>
	 * Please note, that this function can only be called once we know for sure
	 * that the value is of set-of type.
	 *
	 * @param timestamp the timestamp of the actual semantic check cycle.
	 * @param value the value to be checked
	 * @param expectedValue the kind of value expected here.
	 * @param incompleteAllowed wheather incomplete value is allowed or not.
	 * @param implicitOmit true if the implicit omit optional attribute was set
	 *            for the value, false otherwise
	 * */
	public boolean checkThisValueSequenceOf(final CompilationTimeStamp timestamp, final SequenceOf_Value value, final Assignment lhs,
			final Expected_Value_type expectedValue, final boolean incompleteAllowed , final boolean implicitOmit, final boolean strElem) {
		boolean selfReference = false;

		if (value.isIndexed()) {
			boolean checkHoles = Expected_Value_type.EXPECTED_CONSTANT.equals(expectedValue);
			BigInteger maxIndex = BigInteger.valueOf(-1);
			final Map<BigInteger, Integer> indexMap = new HashMap<BigInteger, Integer>(value.getNofComponents());
			for (int i = 0, size = value.getNofComponents(); i < size; i++) {
				final IValue component = value.getValueByIndex(i);
				final IValue index = value.getIndexByIndex(i);
				final IReferenceChain referenceChain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
				final IValue indexLast = index.getValueRefdLast(timestamp, referenceChain);
				referenceChain.release();

				final IType tempType = TypeFactory.createType(Type_type.TYPE_INTEGER);
				tempType.check(timestamp);
				indexLast.setMyGovernor(tempType);
				final IValue temporalValue = tempType.checkThisValueRef(timestamp, indexLast);
				tempType.checkThisValue(timestamp, temporalValue, lhs, new ValueCheckingOptions(Expected_Value_type.EXPECTED_DYNAMIC_VALUE,
						true, false, true, false, false));

				if (indexLast.getIsErroneous(timestamp) || !Value_type.INTEGER_VALUE.equals(temporalValue.getValuetype())) {
					checkHoles = false;
				} else {
					final BigInteger tempIndex = ((Integer_Value) temporalValue).getValueValue();
					if (tempIndex.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) == 1) {
						index.getLocation().reportSemanticError(MessageFormat.format(
								"A integer value less than `{0}'' was expected for indexing type `{1}'' instead of `{2}''",
								Integer.MAX_VALUE, getTypename(), tempIndex));
						checkHoles = false;
					} else if (tempIndex.compareTo(BigInteger.ZERO) == -1) {
						index.getLocation().reportSemanticError(MessageFormat.format(
								"A non-negative integer value was expected for indexing type `{0}'' instead of `{1}''", getTypename(), tempIndex));
						checkHoles = false;
					} else if (indexMap.containsKey(tempIndex)) {
						index.getLocation().reportSemanticError(MessageFormat.format(
								"Duplicate index value `{0}'' for components {1} and {2}", tempIndex, indexMap.get(tempIndex),  i + 1));
						checkHoles = false;
					} else {
						indexMap.put(tempIndex,  Integer.valueOf(i + 1));
						if (maxIndex.compareTo(tempIndex) == -1) {
							maxIndex = tempIndex;
						}
					}
				}

				component.setMyGovernor(getOfType());
				final IValue tempValue2 = getOfType().checkThisValueRef(timestamp, component);
				selfReference |= getOfType().checkThisValue(timestamp, tempValue2, lhs,
						new ValueCheckingOptions(expectedValue, incompleteAllowed, false, true, implicitOmit, strElem));
			}
			if (checkHoles) {
				if (maxIndex.compareTo(BigInteger.valueOf(indexMap.size() - 1)) != 0) {
					value.getLocation().reportSemanticError("It's not allowed to create hole(s) in constant values");
				}
			}
		} else {
			for (int i = 0, size = value.getNofComponents(); i < size; i++) {
				final IValue component = value.getValueByIndex(i);
				component.setMyGovernor(getOfType());
				if (Value_type.NOTUSED_VALUE.equals(component.getValuetype())) {
					if (!incompleteAllowed) {
						component.getLocation().reportSemanticError(INCOMPLETEPRESENTERROR);
					}
				} else {
					final IValue tempValue2 = getOfType().checkThisValueRef(timestamp, component);
					selfReference |= getOfType().checkThisValue(timestamp, tempValue2, lhs,
							new ValueCheckingOptions(expectedValue, incompleteAllowed, false, true, implicitOmit, strElem));
				}
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
		template.setMyGovernor(this);

		boolean selfReference = false;
		switch (template.getTemplatetype()) {
		case OMIT_VALUE:
			if (template.getLengthRestriction() != null) {
				template.getLocation().reportSemanticWarning(REDUNDANTLENGTHRESTRICTION);
			}
			break;
		case ANY_VALUE:
		case ANY_OR_OMIT:
			break;
		case PERMUTATION_MATCH: {
			final PermutationMatch_Template permutationTemplate = (PermutationMatch_Template) template;
			final int nofComponents = permutationTemplate.getNofTemplates();
			for (int i = 0; i < nofComponents; i++) {
				ITTCN3Template templateComponent = permutationTemplate.getTemplateByIndex(i);
				templateComponent.setMyGovernor(getOfType());
				templateComponent = getOfType().checkThisTemplateRef(timestamp, templateComponent); //It does not do anything for AllElementsFrom, it is ok
				selfReference |= templateComponent.checkThisTemplateGeneric(timestamp, getOfType(), false, false, true, true, implicitOmit, lhs); //it is a special for AllElementsFrom, it is the usual for TemplateBody
			}
			break;
		}
		case SUPERSET_MATCH: {
			final SupersetMatch_Template supersetTemplate = (SupersetMatch_Template) template;
			final int nofComponents = supersetTemplate.getNofTemplates();
			for (int i = 0; i < nofComponents; i++) {
				ITTCN3Template templateComponent = supersetTemplate.getTemplateByIndex(i);
				templateComponent.setMyGovernor(getOfType());
				templateComponent = getOfType().checkThisTemplateRef(timestamp, templateComponent); //It does not do anything for AllElementsFrom, it is ok
				selfReference |= templateComponent.checkThisTemplateGeneric(timestamp, getOfType(), false, false, true, true, implicitOmit, lhs); //it is a special for AllElementsFrom, it is the usual for TemplateBody
			}
			break;
		}
		case SUBSET_MATCH: {
			final SubsetMatch_Template subsetTemplate = (SubsetMatch_Template) template;
			final int nofComponents = subsetTemplate.getNofTemplates();
			for (int i = 0; i < nofComponents; i++) {
				ITTCN3Template templateComponent = subsetTemplate.getTemplateByIndex(i);
				templateComponent.setMyGovernor(getOfType());
				templateComponent = getOfType().checkThisTemplateRef(timestamp, templateComponent); //It does not do anything for AllElementsFrom, it is ok
				selfReference |= templateComponent.checkThisTemplateGeneric(timestamp, getOfType(), false, false, true, true, implicitOmit, lhs); //it is a special for AllElementsFrom, it is the usual for TemplateBody
			}
			break;
		}
		case TEMPLATE_LIST: {
			final Completeness_type completeness = template.getCompletenessConditionSeof(timestamp, isModified);
			Template_List base = null;
			int nofBaseComps = 0;
			if (Completeness_type.PARTIAL.equals(completeness)) {
				ITTCN3Template tempBase = template.getBaseTemplate();
				if (tempBase != null) {
					tempBase = tempBase.getTemplateReferencedLast(timestamp);
				}

				if (tempBase == null) {
					setIsErroneous(true);
					return selfReference;
				}

				base = (Template_List) tempBase;
				nofBaseComps = base.getNofTemplates();
			}

			final Template_List templateList = (Template_List) template;
			final int nofComponents = templateList.getNofTemplates();
			for (int i = 0; i < nofComponents; i++) {
				ITTCN3Template component = templateList.getTemplateByIndex(i);
				component.setMyGovernor(getOfType());
				if (base != null && nofBaseComps > i) {
					component.setBaseTemplate(base.getTemplateByIndex(i));
				} else {
					component.setBaseTemplate(null);
				}

				component = getOfType().checkThisTemplateRef(timestamp, component);

				switch (component.getTemplatetype()) {
				case PERMUTATION_MATCH:
					selfReference |= component.checkThisTemplateGeneric(timestamp, this, isModified, false, true, true, implicitOmit, lhs);
					break;
				case SUPERSET_MATCH:
				case SUBSET_MATCH:
					//FIXME: for Complement??? case COMPLEMENTED_LIST: ???
					// the elements of permutation has to be checked by u.seof.ofType
					// the templates within the permutation always have to be complete
					selfReference |= component.checkThisTemplateGeneric(timestamp, this, false, false, true, true, implicitOmit, lhs);
					break;
				case TEMPLATE_NOTUSED:
					if (Completeness_type.MUST_COMPLETE.equals(completeness)) {
						component.getLocation().reportSemanticWarning(NOTUSEDNOTALLOWED1);
					} else if (Completeness_type.PARTIAL.equals(completeness) && i >= nofBaseComps) {
						component.getLocation().reportSemanticError(NOTUSEDNOTALLOWED2);
					}
					break;
				default:
					final boolean embeddedModified = (completeness == Completeness_type.MAY_INCOMPLETE)
					|| (completeness == Completeness_type.PARTIAL && i < nofBaseComps);
					selfReference |= component.checkThisTemplateGeneric(timestamp, getOfType(), embeddedModified, false, true, true, implicitOmit, lhs);
					break;
				}
			}
			break;
		}
		case INDEXED_TEMPLATE_LIST: {
			final Map<Long, Integer> indexMap = new HashMap<Long, Integer>();
			final Indexed_Template_List indexedTemplateList = (Indexed_Template_List) template;
			for (int i = 0, size = indexedTemplateList.getNofTemplates(); i < size; i++) {
				final IndexedTemplate indexedTemplate = indexedTemplateList.getIndexedTemplateByIndex(i);
				final Value indexValue = indexedTemplate.getIndex().getValue();
				ITTCN3Template templateComponent = indexedTemplate.getTemplate();

				final IReferenceChain chain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
				final IValue lastValue = indexValue.getValueRefdLast(timestamp, chain);
				chain.release();

				final IType tempType = TypeFactory.createType(Type_type.TYPE_INTEGER);
				tempType.check(timestamp);
				lastValue.setMyGovernor(tempType);
				final IValue temporalValue = tempType.checkThisValueRef(timestamp, lastValue);
				tempType.checkThisValue(timestamp, temporalValue, null, new ValueCheckingOptions(Expected_Value_type.EXPECTED_DYNAMIC_VALUE,
						true, false, true, false, false));

				if (!temporalValue.getIsErroneous(timestamp) && Value_type.INTEGER_VALUE.equals(temporalValue.getValuetype())) {
					final long index = ((Integer_Value) lastValue).getValue();
					if (index > Integer.MAX_VALUE) {
						indexValue.getLocation().reportSemanticError(
								MessageFormat.format(TOOBIGINDEXTEMPLATE, Integer.MAX_VALUE, getTypename(), index));
						indexValue.setIsErroneous(true);
					} else if (index < 0) {
						indexValue.getLocation().reportSemanticError(MessageFormat.format(NONNEGATIVEINDEXEXPECTEDTEMPLATE, getTypename(), index));
						indexValue.setIsErroneous(true);
					} else {
						if (indexMap.containsKey(index)) {
							indexValue.getLocation().reportSemanticError(MessageFormat.format(DUPLICATEINDEX, index, i + 1, indexMap.get(index)));
							indexValue.setIsErroneous(true);
						} else {
							indexMap.put(index, i);
						}
					}
				}

				templateComponent.setMyGovernor(getOfType());
				templateComponent = getOfType().checkThisTemplateRef(timestamp, templateComponent);
				selfReference |= templateComponent.checkThisTemplateGeneric(timestamp, getOfType(), true, false, true, true, implicitOmit, lhs);
			}
			break;
		}
		default:
			template.getLocation().reportSemanticError(MessageFormat.format(TEMPLATENOTALLOWED, template.getTemplateTypeName(), getTypename()));
			break;
		}

		return selfReference;
	}

	@Override
	/** {@inheritDoc} */
	public IType getFieldType(final CompilationTimeStamp timestamp, final Reference reference, final int actualSubReference,
			final Expected_Value_type expectedIndex, final IReferenceChain refChain, final boolean interruptIfOptional) {
		final List<ISubReference> subreferences = reference.getSubreferences();
		if (subreferences.size() <= actualSubReference) {
			return this;
		}

		final Expected_Value_type internalExpectation = expectedIndex == Expected_Value_type.EXPECTED_TEMPLATE ? Expected_Value_type.EXPECTED_DYNAMIC_VALUE
				: expectedIndex;

		final ISubReference subreference = subreferences.get(actualSubReference);
		switch (subreference.getReferenceType()) {
		case arraySubReference: {
			final Value indexValue = ((ArraySubReference) subreference).getValue();
			if (indexValue != null) {
				indexValue.setLoweridToReference(timestamp);
				final Type_type tempType = indexValue.getExpressionReturntype(timestamp, expectedIndex);
				if (tempType == Type_type.TYPE_UNDEFINED) {
					if (getOfType() != null) {
						return getOfType().getFieldType(timestamp, reference, actualSubReference + 1, internalExpectation, refChain, interruptIfOptional);
					}

					return null;
				}

				IType indexingType = indexValue.getExpressionGovernor(timestamp, expectedIndex);
				if (indexingType != null) {
					indexingType = indexingType.getTypeRefdLast(timestamp);
				}

				if (indexingType != null && (indexingType.getTypetype() == Type_type.TYPE_ARRAY || indexingType.getTypetype() == Type_type.TYPE_SEQUENCE_OF)) {
					// The indexer type must be of type integer
					long length = 0;
					if (indexingType.getTypetype() == Type_type.TYPE_ARRAY) {
						final Array_Type indexingArray = (Array_Type)indexingType;
						if (indexingArray.getElementType().getTypetype() != Type_type.TYPE_INTEGER) {
							subreference.getLocation().reportSemanticError("Only fixed length array or record of integer types are allowed for short-hand notation for nested indexes.");
							return null;
						}

						length = indexingArray.getDimension().getSize();
					} else if (indexingType.getTypetype() == Type_type.TYPE_SEQUENCE_OF) {
						final SequenceOf_Type indexingSequenceOf = (SequenceOf_Type)indexingType;
						if (indexingSequenceOf.getOfType().getTypetype() != Type_type.TYPE_INTEGER) {
							subreference.getLocation().reportSemanticError("Only fixed length array or record of integer types are allowed for short-hand notation for nested indexes.");
							return null;
						}

						final SubType subType = indexingSequenceOf.getSubtype();
						if (subType == null) {
							subreference.getLocation().reportSemanticError(MessageFormat.format("The type `{0}'' must have single size length restriction when used as a short-hand notation for nested indexes.", indexingSequenceOf.getTypename()));
							return null;
						}

						length = subType.get_length_restriction();
						if (length == -1) {
							subreference.getLocation().reportSemanticError(MessageFormat.format("The type `{0}'' must have single size length restriction when used as a short-hand notation for nested indexes.", indexingSequenceOf.getTypename()));
							return null;
						}
					}

					IType embeddedType = getOfType().getTypeRefdLast(timestamp);
					int j = 0;
					while (j < length - 1) {
						switch(embeddedType.getTypetype()) {
						case TYPE_ARRAY:
							embeddedType = ((Array_Type)embeddedType).getElementType();
							break;
						case TYPE_SEQUENCE_OF:
							embeddedType = ((SequenceOf_Type)embeddedType).getOfType();
							break;
						case TYPE_SET_OF:
							embeddedType = ((SetOf_Type)embeddedType).getOfType();
							break;
						default:
							subreference.getLocation().reportSemanticError(MessageFormat.format("The type `{0}'' contains too many indexes ({1}) in the short-hand notation for nested indexes.", indexingType.getTypename(), length));
							return null;
						}
						j++;
					}

					return embeddedType.getFieldType(timestamp, reference, actualSubReference + 1, internalExpectation, refChain, interruptIfOptional);
				} else if(indexingType != null && indexingType.getTypetypeTtcn3() == Type_type.TYPE_INTEGER) {
					final IValue last = indexValue.getValueRefdLast(timestamp, expectedIndex, refChain);
					if (Value_type.INTEGER_VALUE.equals(last.getValuetype())) {
						final Integer_Value lastInteger = (Integer_Value) last;
						if (lastInteger.isNative()) {
							final long temp = lastInteger.getValue();
							if (temp < 0) {
								indexValue.getLocation().reportSemanticError(MessageFormat.format(NONNEGATIVINDEXEXPECTED, temp));
								indexValue.setIsErroneous(true);
							}
						} else {
							indexValue.getLocation().reportSemanticError(MessageFormat.format(TOOBIGINDEX, lastInteger.getValueValue(), getTypename()));
							indexValue.setIsErroneous(true);
						}
					}

					if (getOfType() != null) {
						return getOfType().getFieldType(timestamp, reference, actualSubReference + 1, internalExpectation, refChain, interruptIfOptional);
					}
				} else {
					indexValue.getLocation().reportSemanticError(INTEGERINDEXEXPECTED);
					indexValue.setIsErroneous(true);
				}
			}

			return null;
		}
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
	public void checkMapParameter(final CompilationTimeStamp timestamp, final IReferenceChain refChain, final Location errorLocation) {
		if (refChain.contains(this)) {
			return;
		}

		refChain.add(this);
		getOfType().checkMapParameter(timestamp, refChain, errorLocation);
	}

	@Override
	/** {@inheritDoc} */
	public void forceRaw(final CompilationTimeStamp timestamp) {
		if (rawAttribute == null) {
			rawAttribute = new RawAST(getDefaultRawFieldLength());
		}
	}

	@Override
	/** {@inheritDoc} */
	public void forceJson(final CompilationTimeStamp timestamp) {
		if (jsonAttribute == null) {
			jsonAttribute = new JsonAST();
		}
	}

	@Override
	/** {@inheritDoc} */
	public StringBuilder getProposalDescription(final StringBuilder builder) {
		builder.append("sequence of ");
		if (getOfType() != null) {
			getOfType().getProposalDescription(builder);
		}
		return builder;
	}

	@Override
	/** {@inheritDoc} */
	public Declaration resolveReference(final Reference reference, final int subRefIdx, final ISubReference lastSubreference) {
		if (getOfType() == null) {
			return null;
		}

		final IType refdLastOfType = getOfType().getTypeRefdLast(CompilationTimeStamp.getBaseTimestamp());
		if (refdLastOfType != this && refdLastOfType instanceof IReferenceableElement) {
			return ((IReferenceableElement) refdLastOfType).resolveReference(reference, subRefIdx + 1, lastSubreference);
		}

		return null;
	}

	@Override
	/** {@inheritDoc} */
	public boolean generatesOwnClass(JavaGenData aData, StringBuilder source) {
		final boolean force_gen_seof = aData.getForceGenSeof();
		if (force_gen_seof) {
			return true;
		} else {
			final IType ofType = getOfType();
			switch (ofType.getTypetype()) {
			case TYPE_BOOL:
			case TYPE_BITSTRING:
			case TYPE_BITSTRING_A:
			case TYPE_HEXSTRING:
			case TYPE_OCTETSTRING:
			case TYPE_CHARSTRING:
			case TYPE_UCHARSTRING:
			case TYPE_UTF8STRING:
			case TYPE_TELETEXSTRING:
			case TYPE_VIDEOTEXSTRING:
			case TYPE_GRAPHICSTRING:
			case TYPE_GENERALSTRING:
			case TYPE_UNIVERSALSTRING:
			case TYPE_BMPSTRING:
			case TYPE_OBJECTDESCRIPTOR:
			case TYPE_INTEGER:
			case TYPE_INTEGER_A:
			case TYPE_REAL:
				return false;
			default:
				return true;
			}
		}
	}

	@Override
	/** {@inheritDoc} */
	public void generateCode( final JavaGenData aData, final StringBuilder source ) {
		if (lastTimeGenerated != null && !lastTimeGenerated.isLess(aData.getBuildTimstamp())) {
			return;
		}

		lastTimeGenerated = aData.getBuildTimstamp();

		final String genName = getGenNameOwn();
		final String displayName = getFullName();
		final IType ofType = getOfType();
		final boolean force_gen_seof = aData.getForceGenSeof();

		if (force_gen_seof) {
			final String ofTypeGenName = ofType.getGenNameValue( aData, source );
			final String ofTemplateTypeName = ofType.getGenNameTemplate( aData, source );

			final boolean hasRaw = getGenerateCoderFunctions(MessageEncoding_type.RAW);
			final boolean hasJson = getGenerateCoderFunctions(MessageEncoding_type.JSON);
			int extension_bit = RawASTStruct.XDEFDEFAULT;
			if (hasRaw) {
				RawAST dummy_raw;
				if (rawAttribute == null) {
					dummy_raw = new RawAST(getDefaultRawFieldLength());
				} else {
					dummy_raw = rawAttribute;
				}

				extension_bit = dummy_raw.extension_bit;
			}

			final StringBuilder localTypeDescriptor = new StringBuilder();
			generateCodeTypedescriptor(aData, source, localTypeDescriptor);
			generateCodeDefaultCoding(aData, source, localTypeDescriptor);
			if (!ofType.generatesOwnClass(aData, source)) {
				ofType.generateCodeTypedescriptor(aData, source, localTypeDescriptor);
				ofType.generateCodeDefaultCoding(aData, source, localTypeDescriptor);
			}

			RecordOfGenerator.generateValueClass( aData, source, genName, displayName, ofTypeGenName, false, hasRaw, true, extension_bit, hasJson, localTypeDescriptor);
			RecordOfGenerator.generateTemplateClass( aData, source, genName, displayName, ofTemplateTypeName, false );
		} else {
			final String ofTypeGenName = ofType.getGenNameValue( aData, source );
			switch (ofType.getTypetype()) {
			case TYPE_BOOL:
			case TYPE_BITSTRING:
			case TYPE_BITSTRING_A:
			case TYPE_HEXSTRING:
			case TYPE_OCTETSTRING:
			case TYPE_CHARSTRING:
			case TYPE_UCHARSTRING:
			case TYPE_UTF8STRING:
			case TYPE_TELETEXSTRING:
			case TYPE_VIDEOTEXSTRING:
			case TYPE_GRAPHICSTRING:
			case TYPE_GENERALSTRING:
			case TYPE_UNIVERSALSTRING:
			case TYPE_BMPSTRING:
			case TYPE_OBJECTDESCRIPTOR:
			case TYPE_INTEGER:
			case TYPE_INTEGER_A:
			case TYPE_REAL: {
				generateCodeTypedescriptor(aData, source, null);
				generateCodeDefaultCoding(aData, source, null);
				if (!ofType.generatesOwnClass(aData, source)) {
					ofType.generateCodeTypedescriptor(aData, source, null);
					ofType.generateCodeDefaultCoding(aData, source, null);
				}

				final String ownName = getGenNameOwn(aData);
				final String valueName = getGenNameValue(aData, source);
				source.append(MessageFormat.format("\t// code for type {0} is not generated, {1} is used instead\n", ownName, valueName));
				break;
			}
			case TYPE_REFERENCED: {
				final String ofTemplateTypeName = ofType.getGenNameTemplate( aData, source );

				final boolean hasRaw = getGenerateCoderFunctions(MessageEncoding_type.RAW);
				final boolean hasJson = getGenerateCoderFunctions(MessageEncoding_type.JSON);
				int extension_bit = RawASTStruct.XDEFDEFAULT;
				if (hasRaw) {
					RawAST dummy_raw;
					if (rawAttribute == null) {
						dummy_raw = new RawAST(getDefaultRawFieldLength());
					} else {
						dummy_raw = rawAttribute;
					}

					extension_bit = dummy_raw.extension_bit;
				}

				final StringBuilder localTypeDescriptor = new StringBuilder();
				generateCodeTypedescriptor(aData, source, localTypeDescriptor);
				generateCodeDefaultCoding(aData, source, localTypeDescriptor);
				if (!ofType.generatesOwnClass(aData, source)) {
					ofType.generateCodeTypedescriptor(aData, source, localTypeDescriptor);
					ofType.generateCodeDefaultCoding(aData, source, localTypeDescriptor);
				}

				RecordOfGenerator.generateValueClass( aData, source, genName, displayName, ofTypeGenName, false, hasRaw, false, extension_bit, hasJson, localTypeDescriptor);
				RecordOfGenerator.generateTemplateClass( aData, source, genName, displayName, ofTemplateTypeName, false );
				break;
			}
			default: {
				final String ofTemplateTypeName = ofType.getGenNameTemplate( aData, source );

				final boolean hasRaw = getGenerateCoderFunctions(MessageEncoding_type.RAW);
				final boolean hasJson = getGenerateCoderFunctions(MessageEncoding_type.JSON);
				int extension_bit = RawASTStruct.XDEFDEFAULT;
				if (hasRaw) {
					RawAST dummy_raw;
					if (rawAttribute == null) {
						dummy_raw = new RawAST(getDefaultRawFieldLength());
					} else {
						dummy_raw = rawAttribute;
					}

					extension_bit = dummy_raw.extension_bit;
				}

				final StringBuilder localTypeDescriptor = new StringBuilder();
				generateCodeTypedescriptor(aData, source, localTypeDescriptor);
				generateCodeDefaultCoding(aData, source, localTypeDescriptor);
				if (!ofType.generatesOwnClass(aData, source)) {
					ofType.generateCodeTypedescriptor(aData, source, localTypeDescriptor);
					ofType.generateCodeDefaultCoding(aData, source, localTypeDescriptor);
				}

				RecordOfGenerator.generateValueClass( aData, source, genName, displayName, ofTypeGenName, false, hasRaw, false, extension_bit, hasJson, localTypeDescriptor);
				RecordOfGenerator.generateTemplateClass( aData, source, genName, displayName, ofTemplateTypeName, false );
				break;
			}
			}
		}

		final StringBuilder tempSource = aData.getCodeForType(ofType.getGenNameOwn());
		ofType.generateCode(aData, tempSource);

		if (!isAsn()) {
			if (hasDoneAttribute()) {
				generateCodeDone(aData, source);
			}
			if (subType != null) {
				subType.generateCode(aData, source);
			}
		}

		generateCodeForCodingHandlers(aData, source);
	}

	@Override
	/** {@inheritDoc} */
	public String getGenNameValue( final JavaGenData aData, final StringBuilder source ) {
		final boolean force_gen_seof = aData.getForceGenSeof();
		if (force_gen_seof) {
			return getGenNameOwn(aData);
		} else {
			final boolean optimized_memalloc = false;//TODO add support for optimized memalloc
			final IType ofType = getOfType();
			switch (ofType.getTypetype()) {
			case TYPE_BOOL:
				return RecordOfGenerator.getPreGenBasedNameValue(aData, source, "BOOLEAN", false, optimized_memalloc);
			case TYPE_BITSTRING:
			case TYPE_BITSTRING_A:
				return RecordOfGenerator.getPreGenBasedNameValue(aData, source, "BITSTRING", false, optimized_memalloc);
			case TYPE_HEXSTRING:
				return RecordOfGenerator.getPreGenBasedNameValue(aData, source, "HEXSTRING", false, optimized_memalloc);
			case TYPE_OCTETSTRING:
				return RecordOfGenerator.getPreGenBasedNameValue(aData, source, "OCTETSTRING", false, optimized_memalloc);
			case TYPE_CHARSTRING:
				return RecordOfGenerator.getPreGenBasedNameValue(aData, source, "CHARSTRING", false, optimized_memalloc);
			case TYPE_UCHARSTRING:
			case TYPE_UTF8STRING:
			case TYPE_TELETEXSTRING:
			case TYPE_VIDEOTEXSTRING:
			case TYPE_GRAPHICSTRING:
			case TYPE_GENERALSTRING:
			case TYPE_UNIVERSALSTRING:
			case TYPE_BMPSTRING:
			case TYPE_OBJECTDESCRIPTOR:
				return RecordOfGenerator.getPreGenBasedNameValue(aData, source, "UNIVERSAL__CHARSTRING", false, optimized_memalloc);
			case TYPE_INTEGER:
			case TYPE_INTEGER_A:
				return RecordOfGenerator.getPreGenBasedNameValue(aData, source, "INTEGER", false, optimized_memalloc);
			case TYPE_REAL:
				return RecordOfGenerator.getPreGenBasedNameValue(aData, source, "FLOAT", false, optimized_memalloc);
			default:
				return getGenNameOwn(aData);
			}
		}
	}

	@Override
	/** {@inheritDoc} */
	public String getGenNameTemplate(final JavaGenData aData, final StringBuilder source) {
		return getGenNameValue(aData, source).concat("_template");
	}

	@Override
	/** {@inheritDoc} */
	public String getGenNameTypeDescriptor(final JavaGenData aData, final StringBuilder source) {
		final boolean force_gen_seof = aData.getForceGenSeof();
		if (force_gen_seof) {
			String baseName = getGenNameTypeName(aData, source);
			return baseName + "." + getGenNameOwn();
		} else {
			final IType ofType = getOfType();
			switch (ofType.getTypetype()) {
			case TYPE_BOOL:
			case TYPE_BITSTRING:
			case TYPE_BITSTRING_A:
			case TYPE_HEXSTRING:
			case TYPE_OCTETSTRING:
			case TYPE_CHARSTRING:
			case TYPE_UCHARSTRING:
			case TYPE_UTF8STRING:
			case TYPE_TELETEXSTRING:
			case TYPE_VIDEOTEXSTRING:
			case TYPE_GRAPHICSTRING:
			case TYPE_GENERALSTRING:
			case TYPE_UNIVERSALSTRING:
			case TYPE_BMPSTRING:
			case TYPE_OBJECTDESCRIPTOR:
			case TYPE_INTEGER:
			case TYPE_INTEGER_A:
			case TYPE_REAL:
				if (getParentType() != null) {
					final IType parentType = getParentType();
					if (parentType.generatesOwnClass(aData, source)) {
						return parentType.getGenNameOwn(aData) + "." + getGenNameOwn();
					}
				}

				return getGenNameOwn(aData);
			default:
				String baseName = getGenNameTypeName(aData, source);
				return baseName + "." + getGenNameOwn();
			}
		}
	}

	@Override
	/** {@inheritDoc} */
	public boolean needsOwnRawDescriptor(final JavaGenData aData) {
		return true;
	}

	@Override
	/** {@inheritDoc} */
	public String getGenNameRawDescriptor(final JavaGenData aData, final StringBuilder source) {
		final boolean force_gen_seof = aData.getForceGenSeof();
		if (force_gen_seof) {
			return getGenNameOwn(aData) + "." + getGenNameOwn() + "_raw_";
		} else {
			final IType ofType = getOfType();
			switch (ofType.getTypetype()) {
			case TYPE_BOOL:
			case TYPE_BITSTRING:
			case TYPE_BITSTRING_A:
			case TYPE_HEXSTRING:
			case TYPE_OCTETSTRING:
			case TYPE_CHARSTRING:
			case TYPE_UCHARSTRING:
			case TYPE_UTF8STRING:
			case TYPE_TELETEXSTRING:
			case TYPE_VIDEOTEXSTRING:
			case TYPE_GRAPHICSTRING:
			case TYPE_GENERALSTRING:
			case TYPE_UNIVERSALSTRING:
			case TYPE_BMPSTRING:
			case TYPE_OBJECTDESCRIPTOR:
			case TYPE_INTEGER:
			case TYPE_INTEGER_A:
			case TYPE_REAL:
				if (getParentType() != null) {
					final IType parentType = getParentType();
					if (parentType.generatesOwnClass(aData, source)) {
						return parentType.getGenNameOwn(aData) + "." + getGenNameOwn() + "_raw_";
					}
				}

				return getGenNameOwn(aData) + "_raw_";
			default:
				return getGenNameOwn(aData) + "." + getGenNameOwn() + "_raw_";
			}
		}
	}

	@Override
	/** {@inheritDoc} */
	public boolean needsOwnJsonDescriptor(final JavaGenData aData) {
		return true;
	}

	@Override
	/** {@inheritDoc} */
	public String getGenNameJsonDescriptor(final JavaGenData aData, final StringBuilder source) {
		final boolean force_gen_seof = aData.getForceGenSeof();
		if (force_gen_seof) {
			return getGenNameOwn(aData) + "." + getGenNameOwn() + "_json_";
		} else {
			final IType ofType = getOfType();
			switch (ofType.getTypetype()) {
			case TYPE_BOOL:
			case TYPE_BITSTRING:
			case TYPE_BITSTRING_A:
			case TYPE_HEXSTRING:
			case TYPE_OCTETSTRING:
			case TYPE_CHARSTRING:
			case TYPE_UCHARSTRING:
			case TYPE_UTF8STRING:
			case TYPE_TELETEXSTRING:
			case TYPE_VIDEOTEXSTRING:
			case TYPE_GRAPHICSTRING:
			case TYPE_GENERALSTRING:
			case TYPE_UNIVERSALSTRING:
			case TYPE_BMPSTRING:
			case TYPE_OBJECTDESCRIPTOR:
			case TYPE_INTEGER:
			case TYPE_INTEGER_A:
			case TYPE_REAL:
				if (getParentType() != null) {
					final IType parentType = getParentType();
					if (parentType.generatesOwnClass(aData, source)) {
						return parentType.getGenNameOwn(aData) + "." + getGenNameOwn() + "_json_";
					}
				}

				return getGenNameOwn(aData) + "_json_";
			default:
				return getGenNameOwn(aData) + "." + getGenNameOwn() + "_json_";
			}
		}
	}

	@Override
	/** {@inheritDoc} */
	public String generateConversion(final JavaGenData aData, final IType fromType, final String fromName, final boolean forValue, final ExpressionStruct expression) {
		final IType refdType = fromType.getTypeRefdLast(CompilationTimeStamp.getBaseTimestamp());
		if (refdType == null || this == refdType) {
			//no need to convert
			return fromName;
		}

		boolean simpleOfType;
		final IType ofType = getOfType();
		switch (ofType.getTypetype()) {
		case TYPE_BOOL:
		case TYPE_BITSTRING:
		case TYPE_BITSTRING_A:
		case TYPE_HEXSTRING:
		case TYPE_OCTETSTRING:
		case TYPE_CHARSTRING:
		case TYPE_UCHARSTRING:
		case TYPE_UTF8STRING:
		case TYPE_TELETEXSTRING:
		case TYPE_VIDEOTEXSTRING:
		case TYPE_GRAPHICSTRING:
		case TYPE_GENERALSTRING:
		case TYPE_UNIVERSALSTRING:
		case TYPE_BMPSTRING:
		case TYPE_OBJECTDESCRIPTOR:
		case TYPE_INTEGER:
		case TYPE_INTEGER_A:
		case TYPE_REAL:
			simpleOfType = true;
			break;
		default:
			simpleOfType = false;
			break;
		}

		switch(refdType.getTypetype()) {
		case TYPE_SEQUENCE_OF: {
			if (!aData.getForceGenSeof() && simpleOfType) {
				// happens to map to the same type
				return fromName;
			}

			final IType fromOfType = ((SequenceOf_Type)refdType).getOfType();
			return generateConversionSetSeqOfToSetSeqOf(aData, fromType, fromName, ofType, fromOfType, forValue, expression);
		}
		case TYPE_TTCN3_SEQUENCE: {
			final TTCN3_Sequence_Type refdFromType = (TTCN3_Sequence_Type)refdType;
			return generateConversionSeqToSeqOf(aData, refdFromType, fromName, ofType, forValue, expression);
		}
		case TYPE_ARRAY: {
			return generateConversionArrayToSetSeqOf(aData, (Array_Type)fromType, fromName, ofType, forValue, expression);
		}
		default:
			return "FATAL ERROR during converting to type " + getTypename();
		}
	}

	private String generateConversionSetSeqOfToSetSeqOf(final JavaGenData aData, final IType fromType, final String fromName, final IType toOfType, final IType fromOfType, final boolean forValue, final ExpressionStruct expression) {
		//heavy conversion is needed
		final String tempId = aData.getTemporaryVariableName();

		final String name = forValue ? getGenNameValue(aData, expression.preamble) : getGenNameTemplate(aData, expression.preamble);
		expression.preamble.append(MessageFormat.format("final {0} {1} = new {0}();\n", name, tempId));
		final String ConversionFunctionName = Type.getConversionFunction(aData, fromType, this, forValue, expression.preamble);
		expression.preamble.append(MessageFormat.format("if(!{0}({1}, {2})) '{'\n", ConversionFunctionName, tempId, fromName));
		expression.preamble.append(MessageFormat.format("throw new TtcnError(\"Values or templates of type `{0}'' and `{1}'' are not compatible at run-time\");\n", getTypename(), fromType.getTypename()));
		expression.preamble.append("}\n");

		if (!aData.hasTypeConversion(ConversionFunctionName)) {
			final StringBuilder conversionFunctionBody = new StringBuilder();
			final String fromTypeName = forValue ? fromType.getGenNameValue( aData, conversionFunctionBody ): fromType.getGenNameTemplate(aData, conversionFunctionBody);
			conversionFunctionBody.append(MessageFormat.format("\tpublic static boolean {0}(final {1} to, final {2} from) '{'\n", ConversionFunctionName, name, fromTypeName));
			conversionFunctionBody.append("\t\tto.set_size(from.n_elem());\n");

			conversionFunctionBody.append("\t\tfor (int i = 0; i < from.n_elem(); i++) {\n");

			final String tempId2 = aData.getTemporaryVariableName();
			final String fromOfTypeName = forValue ? fromOfType.getGenNameValue(aData, conversionFunctionBody): fromOfType.getGenNameTemplate(aData, conversionFunctionBody);
			conversionFunctionBody.append(MessageFormat.format("\t\t\tfinal {0} {1} = from.constGet_at(i);\n", fromOfTypeName, tempId2));
			conversionFunctionBody.append(MessageFormat.format("\t\t\tif({0}.is_bound()) '{'\n", tempId2));

			final ExpressionStruct tempExpression = new ExpressionStruct();
			final String tempId3 = toOfType.generateConversion(aData, fromOfType, tempId2, forValue, tempExpression);
			tempExpression.openMergeExpression(conversionFunctionBody);

			conversionFunctionBody.append(MessageFormat.format("\t\t\t\tto.get_at(i).operator_assign({0});\n", tempId3));
			conversionFunctionBody.append("\t\t\t}\n");
			conversionFunctionBody.append("\t\t}\n");

			conversionFunctionBody.append("\t\treturn true;\n");
			conversionFunctionBody.append("\t}\n\n");
			aData.addTypeConversion(ConversionFunctionName, conversionFunctionBody.toString());
		}

		return tempId;
	}

	private String generateConversionSeqToSeqOf(final JavaGenData aData, final TTCN3_Sequence_Type fromType, final String fromName, final IType toOfType, final boolean forValue, final ExpressionStruct expression) {
		//heavy conversion is needed
		final String tempId = aData.getTemporaryVariableName();

		final String name = forValue ? getGenNameValue(aData, expression.preamble) : getGenNameTemplate(aData, expression.preamble);
		expression.preamble.append(MessageFormat.format("final {0} {1} = new {0}();\n", name, tempId));
		final String ConversionFunctionName = Type.getConversionFunction(aData, fromType, this, forValue, expression.preamble);
		expression.preamble.append(MessageFormat.format("if(!{0}({1}, {2})) '{'\n", ConversionFunctionName, tempId, fromName));
		expression.preamble.append(MessageFormat.format("throw new TtcnError(\"Values or templates of type `{0}'' and `{1}'' are not compatible at run-time\");\n", getTypename(), fromType.getTypename()));
		expression.preamble.append("}\n");

		if (!aData.hasTypeConversion(ConversionFunctionName)) {
			final StringBuilder conversionFunctionBody = new StringBuilder();
			final String fromTypeName = forValue ? fromType.getGenNameValue( aData, conversionFunctionBody ): fromType.getGenNameTemplate(aData, conversionFunctionBody);
			conversionFunctionBody.append(MessageFormat.format("\tpublic static boolean {0}(final {1} to, final {2} from) '{'\n", ConversionFunctionName, name, fromTypeName));

			final int fromComponentCount = fromType.getNofComponents();
			conversionFunctionBody.append(MessageFormat.format("\t\tto.set_size({0});\n", fromComponentCount));
			for (int i = 0; i < fromComponentCount; i++) {
				final CompField fromComp = fromType.getComponentByIndex(i);
				final Identifier fromFieldName = fromComp.getIdentifier();
				final IType fromFieldType = fromComp.getType().getTypeRefdLast(CompilationTimeStamp.getBaseTimestamp());

				final String tempId2 = aData.getTemporaryVariableName();
				final String fromFieldTypeName = forValue ? fromFieldType.getGenNameValue(aData, conversionFunctionBody): fromFieldType.getGenNameTemplate(aData, conversionFunctionBody);
				conversionFunctionBody.append(MessageFormat.format("\t\tfinal {0} {1} = from.constGet_field_{2}(){3};\n", fromFieldTypeName, tempId2, FieldSubReference.getJavaGetterName( fromFieldName.getName() ), forValue && fromComp.isOptional()? ".constGet()": ""));
				conversionFunctionBody.append(MessageFormat.format("\t\t\tif({0}.is_bound()) '{'\n", tempId2));

				final ExpressionStruct tempExpression = new ExpressionStruct();
				final String tempId3 = toOfType.generateConversion(aData, fromFieldType, tempId2, forValue, tempExpression);
				tempExpression.openMergeExpression(conversionFunctionBody);

				conversionFunctionBody.append(MessageFormat.format("\t\t\t\tto.get_at({0}).operator_assign({1});\n", i, tempId3));

				conversionFunctionBody.append("\t\t}\n");
			}

			conversionFunctionBody.append("\t\treturn true;\n");
			conversionFunctionBody.append("\t}\n\n");
			aData.addTypeConversion(ConversionFunctionName, conversionFunctionBody.toString());
		}

		return tempId;
	}

	private String generateConversionArrayToSetSeqOf(final JavaGenData aData, final Array_Type fromType, final String fromName, final IType toOfType, final boolean forValue, final ExpressionStruct expression) {
		//heavy conversion is needed
		final String tempId = aData.getTemporaryVariableName();
		final IType fromOfType = fromType.getElementType();
		final ArrayDimension arrayDimension = fromType.getDimension();
		final long startIndex = arrayDimension.getOffset();

		final String name = forValue ? getGenNameValue(aData, expression.preamble) : getGenNameTemplate(aData, expression.preamble);
		expression.preamble.append(MessageFormat.format("final {0} {1} = new {0}();\n", name, tempId));
		final String ConversionFunctionName = Type.getConversionFunction(aData, fromType, this, forValue, expression.preamble);
		expression.preamble.append(MessageFormat.format("if(!{0}({1}, {2})) '{'\n", ConversionFunctionName, tempId, fromName));
		expression.preamble.append(MessageFormat.format("throw new TtcnError(\"Values or templates of type `{0}'' and `{1}'' are not compatible at run-time\");\n", getTypename(), fromType.getTypename()));
		expression.preamble.append("}\n");

		if (!aData.hasTypeConversion(ConversionFunctionName)) {
			final StringBuilder conversionFunctionBody = new StringBuilder();
			final String fromTypeName = forValue ? fromType.getGenNameValue( aData, conversionFunctionBody ): fromType.getGenNameTemplate(aData, conversionFunctionBody);
			conversionFunctionBody.append(MessageFormat.format("\tpublic static boolean {0}(final {1} to, final {2} from) '{'\n", ConversionFunctionName, name, fromTypeName));
			conversionFunctionBody.append(MessageFormat.format("\t\tto.set_size({0});\n", arrayDimension.getSize()));

			conversionFunctionBody.append(MessageFormat.format("\t\tfor (int i = 0; i < {0}; i++) '{'\n", arrayDimension.getSize()));

			final String tempId2 = aData.getTemporaryVariableName();
			final String fromOfTypeName = forValue ? fromOfType.getGenNameValue(aData, conversionFunctionBody): fromOfType.getGenNameTemplate(aData, conversionFunctionBody);
			conversionFunctionBody.append(MessageFormat.format("\t\t\tfinal {0} {1} = from.constGet_at(i + {2});\n", fromOfTypeName, tempId2, startIndex));
			conversionFunctionBody.append(MessageFormat.format("\t\t\tif({0}.is_bound()) '{'\n", tempId2));

			final ExpressionStruct tempExpression = new ExpressionStruct();
			final String tempId3 = toOfType.generateConversion(aData, fromOfType, tempId2, forValue, tempExpression);
			tempExpression.openMergeExpression(conversionFunctionBody);

			conversionFunctionBody.append(MessageFormat.format("\t\t\t\tto.get_at(i).operator_assign({0});\n", tempId3));
			conversionFunctionBody.append("\t\t\t}\n");
			conversionFunctionBody.append("\t\t}\n");

			conversionFunctionBody.append("\t\treturn true;\n");
			conversionFunctionBody.append("\t}\n\n");
			aData.addTypeConversion(ConversionFunctionName, conversionFunctionBody.toString());
		}

		return tempId;
	}
}
