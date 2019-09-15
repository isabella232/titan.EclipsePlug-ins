/******************************************************************************
 * Copyright (c) 2000-2019 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.types;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.FieldSubReference;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.Type;
import org.eclipse.titan.designer.AST.IValue.Value_type;
import org.eclipse.titan.designer.AST.ReferenceChain;
import org.eclipse.titan.designer.AST.TypeCompatibilityInfo;
import org.eclipse.titan.designer.AST.Value;
import org.eclipse.titan.designer.AST.ASN1.ASN1Type;
import org.eclipse.titan.designer.AST.ASN1.IASN1Type;
import org.eclipse.titan.designer.AST.ASN1.types.ASN1_Set_Type;
import org.eclipse.titan.designer.AST.TTCN3.attributes.JsonAST;
import org.eclipse.titan.designer.AST.TTCN3.attributes.RawAST;
import org.eclipse.titan.designer.AST.TTCN3.attributes.RawASTStruct;
import org.eclipse.titan.designer.AST.TTCN3.templates.ITTCN3Template;
import org.eclipse.titan.designer.AST.TTCN3.templates.ITTCN3Template.Completeness_type;
import org.eclipse.titan.designer.AST.TTCN3.templates.ITTCN3Template.Template_type;
import org.eclipse.titan.designer.AST.TTCN3.templates.IndexedTemplate;
import org.eclipse.titan.designer.AST.TTCN3.templates.Indexed_Template_List;
import org.eclipse.titan.designer.AST.TTCN3.templates.SubsetMatch_Template;
import org.eclipse.titan.designer.AST.TTCN3.templates.SupersetMatch_Template;
import org.eclipse.titan.designer.AST.TTCN3.templates.Template_List;
import org.eclipse.titan.designer.AST.TTCN3.types.subtypes.SubType;
import org.eclipse.titan.designer.AST.TTCN3.values.Integer_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.SetOf_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.ExpressionStruct;
import org.eclipse.titan.designer.compiler.JavaGenData;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * @author Kristof Szabados
 * @author Arpad Lovassy
 */
public final class SetOf_Type extends AbstractOfType {
	public static final String SETOFVALUEEXPECTED1 = "SET OF value was expected";
	public static final String SETOFVALUEEXPECTED2 = "set of value was expected";
	private static final String TEMPLATENOTALLOWED = "{0} cannot be used for setof type `{1}''";
	private static final String REDUNDANTLENGTHRESTRICTION = "Redundant usage of length restriction with `omit''";
	private static final String ANYOROMITINSUBSET = "`*'' in subset. This template will match everything";
	private static final String ANYOROMITINSUPERSET = "`*'' in superset has no effect during matching";

	private static final String NOTCOMPATIBLESETSETOF = "set/SET and set of/SET OF types are compatible only with other set/SET and set of/SET OF types";
	private static final String NOTCOMPATIBLEUNIONANYTYPE = "union/CHOICE/anytype types are compatible only with other union/CHOICE/anytype types";

	public SetOf_Type(final IType ofType) {
		super(ofType);
	}

	@Override
	/** {@inheritDoc} */
	public Type_type getTypetype() {
		return Type_type.TYPE_SET_OF;
	}

	@Override
	/** {@inheritDoc} */
	public IASN1Type newInstance() {
		if (getOfType() instanceof ASN1Type) {
			return new SetOf_Type(((IASN1Type) getOfType()).newInstance());
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
		case TYPE_ASN1_SET: {
			if (!isSubtypeCompatible(timestamp, lastOtherType)) {
				info.setErrorStr("Incompatible set of/SET OF subtypes");
				return false;
			}

			final ASN1_Set_Type tempType = (ASN1_Set_Type) lastOtherType;
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
				final IType ofType = getOfType().getTypeRefdLast(timestamp);
				final IType tempTypeCfType = tempTypeCf.getType().getTypeRefdLast(timestamp);
				final TypeCompatibilityInfo infoTemp = new TypeCompatibilityInfo(ofType, tempTypeCfType, false);
				lChain.markState();
				rChain.markState();
				lChain.add(ofType);
				rChain.add(tempTypeCfType);
				if (!ofType.equals(tempTypeCfType) && !(lChain.hasRecursion() && rChain.hasRecursion())
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
		case TYPE_TTCN3_SET: {
			if (!isSubtypeCompatible(timestamp, lastOtherType)) {
				info.setErrorStr("Incompatible set of/SET OF subtypes");
				return false;
			}

			final TTCN3_Set_Type tempType = (TTCN3_Set_Type) lastOtherType;
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
				final IType ofType = getOfType().getTypeRefdLast(timestamp);
				final IType tempTypeCfType = tempTypeCf.getType().getTypeRefdLast(timestamp);
				final TypeCompatibilityInfo infoTemp = new TypeCompatibilityInfo(ofType, tempTypeCfType, false);
				lChain.markState();
				rChain.markState();
				lChain.add(ofType);
				rChain.add(tempTypeCfType);
				if (!ofType.equals(tempTypeCfType) && !(lChain.hasRecursion() && rChain.hasRecursion())
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
		case TYPE_SET_OF: {
			if (!isSubtypeCompatible(timestamp, lastOtherType)) {
				info.setErrorStr("Incompatible set of/SET OF subtypes");
				return false;
			}

			final SetOf_Type tempType = (SetOf_Type) lastOtherType;
			if (this == tempType) {
				return true;
			}

			final IType ofType = getOfType().getTypeRefdLast(timestamp);
			final IType tempTypeOfType = tempType.getOfType().getTypeRefdLast(timestamp);
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
			if (!ofType.equals(tempTypeOfType) && !(lChain.hasRecursion() && rChain.hasRecursion())
					&& !ofType.isCompatible(timestamp, tempTypeOfType, infoTemp, lChain, rChain)) {
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
		case TYPE_ASN1_CHOICE:
		case TYPE_TTCN3_CHOICE:
		case TYPE_ANYTYPE:
			info.setErrorStr(NOTCOMPATIBLEUNIONANYTYPE);
			return false;
		case TYPE_ASN1_SEQUENCE:
		case TYPE_TTCN3_SEQUENCE:
		case TYPE_SEQUENCE_OF:
		case TYPE_ARRAY:
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
		if (Type_type.TYPE_SET_OF.equals(lastOtherType.getTypetype())) {
			final IType oftOther = ((SetOf_Type) lastOtherType).getOfType();
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
		return "set_of.gif";
	}

	@Override
	/** {@inheritDoc} */
	public SubType.SubType_type getSubtypeType() {
		return SubType.SubType_type.ST_SETOF;
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
			last = last.setValuetype(timestamp, Value_type.SETOF_VALUE);
		}
		if (last.getIsErroneous(timestamp)) {
			return selfReference;
		}

		switch (last.getValuetype()) {
		case SEQUENCEOF_VALUE:
			last = last.setValuetype(timestamp, Value_type.SETOF_VALUE);
			selfReference = checkThisValueSetOf(timestamp, (SetOf_Value) last, lhs, valueCheckingOptions.expected_value,
					valueCheckingOptions.incomplete_allowed, valueCheckingOptions.implicit_omit, valueCheckingOptions.str_elem);
			break;
		case SETOF_VALUE:
			selfReference = checkThisValueSetOf(timestamp, (SetOf_Value) last, lhs, valueCheckingOptions.expected_value,
					valueCheckingOptions.incomplete_allowed, valueCheckingOptions.implicit_omit, valueCheckingOptions.str_elem);
			break;
		case EXPRESSION_VALUE:
		case MACRO_VALUE:
			// already checked
			break;
		default:
			if (value.isAsn()) {
				value.getLocation().reportSemanticError(SETOFVALUEEXPECTED1);
			} else {
				value.getLocation().reportSemanticError(SETOFVALUEEXPECTED2);
			}
			value.setIsErroneous(true);
		}

		if (valueCheckingOptions.sub_check) {
			// there is no parent type to check
			if (subType != null) {
				subType.checkThisValue(timestamp, last);
			}
		}

		value.setLastTimeChecked(timestamp);

		return selfReference;
	}

	@Override
	/** {@inheritDoc} */
	public boolean checkThisTemplate(final CompilationTimeStamp timestamp, final ITTCN3Template template, final boolean isModified,
			final boolean implicitOmit, final Assignment lhs) {
		registerUsage(template);
		template.setMyGovernor(this);

		boolean selfReference = false;
		switch (template.getTemplatetype()) {
		case OMIT_VALUE:
			if (template.getLengthRestriction() != null) {
				template.getLocation().reportSemanticWarning(REDUNDANTLENGTHRESTRICTION);
			}
			break;
		case SUBSET_MATCH: {
			final SubsetMatch_Template subsetTemplate = (SubsetMatch_Template) template;
			final int nofComponents = subsetTemplate.getNofTemplates();
			for (int i = 0; i < nofComponents; i++) {
				ITTCN3Template templateComponent = subsetTemplate.getTemplateByIndex(i);
				templateComponent.setMyGovernor(getOfType());
				templateComponent = getOfType().checkThisTemplateRef(timestamp, templateComponent);
				selfReference |= templateComponent.checkThisTemplateGeneric(timestamp, getOfType(), false, false, true, true, implicitOmit, lhs);
				if (Template_type.ANY_OR_OMIT.equals(templateComponent.getTemplateReferencedLast(timestamp, null).getTemplatetype())) {
					templateComponent.getLocation().reportSemanticWarning(ANYOROMITINSUBSET);
				}
			}
			break;
		}
		case SUPERSET_MATCH: {
			final SupersetMatch_Template supersetTemplate = (SupersetMatch_Template) template;
			final int nofComponents = supersetTemplate.getNofTemplates();
			for (int i = 0; i < nofComponents; i++) {
				ITTCN3Template templateComponent = supersetTemplate.getTemplateByIndex(i);
				templateComponent.setMyGovernor(getOfType());
				templateComponent = getOfType().checkThisTemplateRef(timestamp, templateComponent);
				selfReference |= templateComponent.checkThisTemplateGeneric(timestamp, getOfType(), false, false, true, true, implicitOmit, lhs);
				if (Template_type.ANY_OR_OMIT.equals(templateComponent.getTemplateReferencedLast(timestamp, null).getTemplatetype())) {
					templateComponent.getLocation().reportSemanticWarning(ANYOROMITINSUPERSET);
				}
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
					component.getLocation().reportSemanticError(
							MessageFormat.format("{0} cannot be used for `set of' type `{1}''",
									component.getTemplateTypeName(), getTypename()));
					break;
				case TEMPLATE_NOTUSED:
					if (Completeness_type.MUST_COMPLETE.equals(completeness)) {
						component.getLocation().reportSemanticError(SequenceOf_Type.NOTUSEDNOTALLOWED2);
					} else if (Completeness_type.PARTIAL.equals(completeness) && i >= nofBaseComps) {
						component.getLocation().reportSemanticError(SequenceOf_Type.NOTUSEDNOTALLOWED2);
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
			for (int i = 0; i < indexedTemplateList.getNofTemplates(); i++) {
				final IndexedTemplate indexedTemplate = indexedTemplateList.getIndexedTemplateByIndex(i);
				final Value indexValue = indexedTemplate.getIndex().getValue();
				ITTCN3Template templateComponent = indexedTemplate.getTemplate();

				final IReferenceChain chain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
				final IValue lastValue = indexValue.getValueRefdLast(timestamp, chain);
				chain.release();
				if(!indexValue.isUnfoldable(timestamp)) {
					if (Value_type.INTEGER_VALUE.equals(lastValue.getValuetype())) {
						final long index = ((Integer_Value) lastValue).getValue();
						if (index > Integer.MAX_VALUE) {
							indexValue.getLocation().reportSemanticError(
									MessageFormat.format(SequenceOf_Type.TOOBIGINDEXTEMPLATE, Integer.MAX_VALUE,
											getTypename(), index));
							indexValue.setIsErroneous(true);
						} else if (index < 0) {
							indexValue.getLocation().reportSemanticError(
									MessageFormat.format(SequenceOf_Type.NONNEGATIVEINDEXEXPECTEDTEMPLATE, getTypename(),
											index));
							indexValue.setIsErroneous(true);
						} else {
							if (indexMap.containsKey(index)) {
								indexValue.getLocation().reportSemanticError(
										MessageFormat.format(SequenceOf_Type.DUPLICATEINDEX, index, i + 1,
												indexMap.get(index)));
								indexValue.setIsErroneous(true);
							} else {
								indexMap.put(index, i);
							}
						}
					} else {
						indexValue.getLocation().reportSemanticError(SequenceOf_Type.INTEGERINDEXEXPECTED);
						indexValue.setIsErroneous(true);
					}
				}

				templateComponent.setMyGovernor(getOfType());
				templateComponent = getOfType().checkThisTemplateRef(timestamp, templateComponent);
				selfReference |= templateComponent.checkThisTemplateGeneric(timestamp, getOfType(), true, false, true, true, implicitOmit, lhs);
			}
			break;
		}
		default:
			template.getLocation().reportSemanticError(
					MessageFormat.format(TEMPLATENOTALLOWED, template.getTemplateTypeName(), getTypename()));
			break;
		}

		return selfReference;
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
		builder.append("set of ");
		if (getOfType() != null) {
			getOfType().getProposalDescription(builder);
		}
		return builder;
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

		generateCodeTypedescriptor(aData, source);

		if (force_gen_seof) {
			final String ofTypeGenName = ofType.getGenNameValue( aData, source );
			final String ofTemplateTypeName = ofType.getGenNameTemplate( aData, source );

			final boolean hasRaw = getGenerateCoderFunctions(MessageEncoding_type.RAW);
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

			RecordOfGenerator.generateValueClass( aData, source, genName, displayName, ofTypeGenName, true, hasRaw, true, extension_bit);
			RecordOfGenerator.generateTemplateClass( aData, source, genName, displayName, ofTemplateTypeName, true );
		} else {
			final String ofTypeGenName = ofType.getGenNameValue( aData, source );
			final String ofTemplateTypeName = ofType.getGenNameTemplate( aData, source );
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
				String ownName = getGenNameOwn(aData);
				String valueName = getGenNameValue(aData, source);
				source.append(MessageFormat.format("\t// code for type {0} is not generated, {1} is used instead\n", ownName, valueName));
				break;
			}
			default: {
				final boolean hasRaw = getGenerateCoderFunctions(MessageEncoding_type.RAW);
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

				RecordOfGenerator.generateValueClass( aData, source, genName, displayName, ofTypeGenName, true, hasRaw, false, extension_bit);
				RecordOfGenerator.generateTemplateClass( aData, source, genName, displayName, ofTemplateTypeName, true );
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
				return RecordOfGenerator.getPreGenBasedNameValue(aData, source, "BOOLEAN", true, optimized_memalloc);
			case TYPE_BITSTRING:
			case TYPE_BITSTRING_A:
				return RecordOfGenerator.getPreGenBasedNameValue(aData, source, "BITSTRING", true, optimized_memalloc);
			case TYPE_HEXSTRING:
				return RecordOfGenerator.getPreGenBasedNameValue(aData, source, "HEXSTRING", true, optimized_memalloc);
			case TYPE_OCTETSTRING:
				return RecordOfGenerator.getPreGenBasedNameValue(aData, source, "OCTETSTRING", true, optimized_memalloc);
			case TYPE_CHARSTRING:
				return RecordOfGenerator.getPreGenBasedNameValue(aData, source, "CHARSTRING", true, optimized_memalloc);
			case TYPE_UCHARSTRING:
			case TYPE_UTF8STRING:
			case TYPE_TELETEXSTRING:
			case TYPE_VIDEOTEXSTRING:
			case TYPE_GRAPHICSTRING:
			case TYPE_GENERALSTRING:
			case TYPE_UNIVERSALSTRING:
			case TYPE_BMPSTRING:
			case TYPE_OBJECTDESCRIPTOR:
				return RecordOfGenerator.getPreGenBasedNameValue(aData, source, "UNIVERSAL__CHARSTRING", true, optimized_memalloc);
			case TYPE_INTEGER:
			case TYPE_INTEGER_A:
				return RecordOfGenerator.getPreGenBasedNameValue(aData, source, "INTEGER", true, optimized_memalloc);
			case TYPE_REAL:
				return RecordOfGenerator.getPreGenBasedNameValue(aData, source, "FLOAT", true, optimized_memalloc);
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
	public String getGenNameRawDescriptor(final JavaGenData aData, final StringBuilder source) {
		generateCodeRawDescriptor(aData, source);

		return getGenNameOwn(aData) + "_raw_";
	}

	@Override
	/** {@inheritDoc} */
	public String getGenNameJsonDescriptor(final JavaGenData aData, final StringBuilder source) {
		generateCodeJsonDescriptor(aData, source);

		return getGenNameOwn(aData) + "_json_";
	}

	@Override
	/** {@inheritDoc} */
	public String generateConversion(final JavaGenData aData, final IType fromType, final String fromName, final ExpressionStruct expression) {
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
		case TYPE_SET_OF: {
			if (!aData.getForceGenSeof() && simpleOfType) {
				// happens to map to the same type
				return fromName;
			}

			final IType fromOfType = ((SetOf_Type)refdType).getOfType();
			return generateConversionSetSeqOfToSetSeqOf(aData, fromType, fromName, ofType, fromOfType, expression);
		}
		case TYPE_TTCN3_SET: {
			final TTCN3_Set_Type refdFromType = (TTCN3_Set_Type)refdType;
			return generateConversionSetToSetOf(aData, refdFromType, fromName, ofType, expression);
		}
		default:
			return "FATAL ERROR during converting to type " + getTypename();
		}
	}

	private String generateConversionSetSeqOfToSetSeqOf(final JavaGenData aData, final IType fromType, final String fromName, final IType toOfType, final IType fromOfType, final ExpressionStruct expression) {
		//heavy conversion is needed
		final String tempId = aData.getTemporaryVariableName();

		final String name = getGenNameValue(aData, expression.preamble);
		expression.preamble.append(MessageFormat.format("final {0} {1} = new {0}();\n", name, tempId));
		final String ConversionFunctionName = Type.getConversionFunction(aData, fromType, this, expression.preamble);
		expression.preamble.append(MessageFormat.format("if(!{0}({1}, {2})) '{'\n", ConversionFunctionName, tempId, fromName));
		expression.preamble.append(MessageFormat.format("throw new TtcnError(\"Values or templates of type `{0}'' and `{1}'' are not compatible at run-time\");\n", getTypename(), fromType.getTypename()));
		expression.preamble.append("}\n");

		if (!aData.hasTypeConversion(ConversionFunctionName)) {
			final StringBuilder conversionFunctionBody = new StringBuilder();
			conversionFunctionBody.append(MessageFormat.format("\tpublic static boolean {0}(final {1} to, final {2} from) '{'\n", ConversionFunctionName, name, fromType.getGenNameValue( aData, conversionFunctionBody )));
			conversionFunctionBody.append("\t\tto.set_size(from.n_elem());\n");
			conversionFunctionBody.append("\t\tfor (int i = 0; i < from.n_elem(); i++) {\n");

			final String tempId2 = aData.getTemporaryVariableName();
			conversionFunctionBody.append(MessageFormat.format("\t\t\tfinal {0} {1} = from.constGet_at(i);\n", fromOfType.getGenNameValue(aData, conversionFunctionBody), tempId2));
			conversionFunctionBody.append(MessageFormat.format("\t\t\tif({0}.is_bound()) '{'\n", tempId2));

			final ExpressionStruct tempExpression = new ExpressionStruct();
			final String tempId3 = toOfType.generateConversion(aData, fromOfType, tempId2, tempExpression);
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

	private String generateConversionSetToSetOf(final JavaGenData aData, final TTCN3_Set_Type fromType, final String fromName, final IType toOfType, final ExpressionStruct expression) {
		//heavy conversion is needed
		final String tempId = aData.getTemporaryVariableName();

		final String name = getGenNameValue(aData, expression.preamble);
		expression.preamble.append(MessageFormat.format("final {0} {1} = new {0}();\n", name, tempId));
		final String ConversionFunctionName = Type.getConversionFunction(aData, fromType, this, expression.preamble);
		expression.preamble.append(MessageFormat.format("if(!{0}({1}, {2})) '{'\n", ConversionFunctionName, tempId, fromName));
		expression.preamble.append(MessageFormat.format("throw new TtcnError(\"Values or templates of type `{0}'' and `{1}'' are not compatible at run-time\");\n", getTypename(), fromType.getTypename()));
		expression.preamble.append("}\n");

		if (!aData.hasTypeConversion(ConversionFunctionName)) {
			final StringBuilder conversionFunctionBody = new StringBuilder();
			conversionFunctionBody.append(MessageFormat.format("\tpublic static boolean {0}(final {1} to, final {2} from) '{'\n", ConversionFunctionName, name, fromType.getGenNameValue( aData, conversionFunctionBody )));

			final int fromComponentCount = fromType.getNofComponents();
			conversionFunctionBody.append(MessageFormat.format("\t\tto.set_size({0});\n", fromComponentCount));
			for (int i = 0; i < fromComponentCount; i++) {
				final CompField fromComp = fromType.getComponentByIndex(i);
				final Identifier fromFieldName = fromComp.getIdentifier();
				final IType fromFieldType = fromComp.getType().getTypeRefdLast(CompilationTimeStamp.getBaseTimestamp());

				final String tempId2 = aData.getTemporaryVariableName();
				conversionFunctionBody.append(MessageFormat.format("\t\tfinal {0} {1} = from.constGet_field_{2}();\n", fromFieldType.getGenNameValue(aData, conversionFunctionBody), tempId2, FieldSubReference.getJavaGetterName( fromFieldName.getName() )));
				conversionFunctionBody.append(MessageFormat.format("\t\t\tif({0}.is_bound()) '{'\n", tempId2));

				final ExpressionStruct tempExpression = new ExpressionStruct();
				final String tempId3 = toOfType.generateConversion(aData, fromFieldType, tempId2, tempExpression);
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
}
