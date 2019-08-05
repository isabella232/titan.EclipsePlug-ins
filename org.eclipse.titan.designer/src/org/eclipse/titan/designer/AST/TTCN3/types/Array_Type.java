/******************************************************************************
 * Copyright (c) 2000-2019 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.types;

import java.math.BigInteger;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.text.templates.Template;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.ArraySubReference;
import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.FieldSubReference;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.IReferenceableElement;
import org.eclipse.titan.designer.AST.ISubReference;
import org.eclipse.titan.designer.AST.ISubReference.Subreference_type;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.IValue.Value_type;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.ParameterisedSubReference;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.ReferenceChain;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.Type;
import org.eclipse.titan.designer.AST.TypeCompatibilityInfo;
import org.eclipse.titan.designer.AST.Value;
import org.eclipse.titan.designer.AST.ASN1.types.ASN1_Sequence_Type;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.templates.ITTCN3Template;
import org.eclipse.titan.designer.AST.TTCN3.templates.ITTCN3Template.Template_type;
import org.eclipse.titan.designer.AST.TTCN3.templates.IndexedTemplate;
import org.eclipse.titan.designer.AST.TTCN3.templates.Indexed_Template_List;
import org.eclipse.titan.designer.AST.TTCN3.templates.PermutationMatch_Template;
import org.eclipse.titan.designer.AST.TTCN3.templates.Template_List;
import org.eclipse.titan.designer.AST.TTCN3.types.subtypes.SubType;
import org.eclipse.titan.designer.AST.TTCN3.values.ArrayDimension;
import org.eclipse.titan.designer.AST.TTCN3.values.Array_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Expression_Value.Operation_type;
import org.eclipse.titan.designer.AST.TTCN3.values.Integer_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.ExpressionStruct;
import org.eclipse.titan.designer.compiler.BuildTimestamp;
import org.eclipse.titan.designer.compiler.JavaGenData;
import org.eclipse.titan.designer.declarationsearch.Declaration;
import org.eclipse.titan.designer.editors.ProposalCollector;
import org.eclipse.titan.designer.editors.actions.DeclarationCollector;
import org.eclipse.titan.designer.editors.ttcn3editor.TTCN3CodeSkeletons;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * @author Kristof Szabados
 * @author Gergo Ujhelyi
 * */
public final class Array_Type extends Type implements IReferenceableElement {
	private static final String ARRAYVALUEEXPECTED = "Array value was expected";
	private static final String TOOMANYEXPECTED = "Too many elements in the array value: {0} was expected instead of {1}";
	private static final String TOOFEWEXPECTED = "Too few elements in the array value: {0} was expected instead of {1}";
	private static final String TEMPLATENOTALLOWED = "{0} cannot be used for type `{1}''";
	private static final String REDUNDANTLENGTHRESTRICTION = "Redundant usage of length restriction with `omit''";
	private static final String TOOMANYTEMPLATEELEMENTS = "Too many elements in the array template: {0} was expected instead of {1}";
	private static final String TOOFEWTEMPLATEELEMENTS = "Too few elements in the array template: {0} was expected instead of {1}";
	private static final String NOTUSEDNOTALLOWED = "Not used symbol `-'' is not allowed in this context";

	private static final String FULLNAMEPART1 = ".<elementType>";
	private static final String FULLNAMEPART2 = ".<dimension>";

	private static final String BADARRAYDIMENSION = "Array types should have the same dimension";
	private static final String NOFFIELDSDONTMATCH =
			"The number of fields in record/SEQUENCE types ({0}) and the size of the array ({1}) must be the same";
	private static final String NOTCOMPATIBLESETSETOF = "set/SET and set of/SET OF types are compatible only with other set/SET and set of/SET OF types";
	private static final String NOTCOMPATIBLEUNIONANYTYPE = "union/CHOICE/anytype types are compatible only with other union/CHOICE/anytype types";

	private final Type elementType;
	private final ArrayDimension dimension;
	// used only in code generation
	private final boolean inTypeDefinition;

	private boolean componentInternal;

	private BuildTimestamp lastBuildTimestamp;
	private String lastGenName;

	private boolean insideCanHaveCoding = false;

	public Array_Type(final Type elementType, final ArrayDimension dimension, final boolean inTypeDefinition) {
		this.elementType = elementType;
		this.dimension = dimension;
		this.inTypeDefinition = inTypeDefinition;
		componentInternal = false;

		if (elementType != null) {
			elementType.setOwnertype(TypeOwner_type.OT_ARRAY, this);
			elementType.setFullNameParent(this);
		}
		if (dimension != null) {
			dimension.setFullNameParent(this);
		}
	}

	@Override
	/** {@inheritDoc} */
	public Type_type getTypetype() {
		return Type_type.TYPE_ARRAY;
	}

	public ArrayDimension getDimension() {
		return dimension;
	}

	@Override
	/** {@inheritDoc} */
	public StringBuilder getFullName(final INamedNode child) {
		final StringBuilder builder = super.getFullName(child);

		if (elementType == child) {
			return builder.append(FULLNAMEPART1);
		} else if (dimension == child) {
			return builder.append(FULLNAMEPART2);
		}

		return builder;
	}

	@Override
	/** {@inheritDoc} */
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		if (elementType != null) {
			elementType.setMyScope(scope);
		}
		if (dimension != null) {
			dimension.setMyScope(scope);
		}
	}

	@Override
	/** {@inheritDoc} */
	public boolean isCompatible(final CompilationTimeStamp timestamp, final IType otherType, final TypeCompatibilityInfo info,
			final TypeCompatibilityInfo.Chain leftChain, final TypeCompatibilityInfo.Chain rightChain) {
		check(timestamp);
		otherType.check(timestamp);
		final IType temp = otherType.getTypeRefdLast(timestamp);

		if (getIsErroneous(timestamp) || temp.getIsErroneous(timestamp) || this == temp) {
			return true;
		}

		if (info == null || noStructuredTypeCompatibility) {
			return this == temp;
		}

		switch (temp.getTypetype()) {
		case TYPE_ASN1_SEQUENCE: {
			final ASN1_Sequence_Type tempType = (ASN1_Sequence_Type) temp;
			final int tempTypeNofComps = tempType.getNofComponents();
			if (tempTypeNofComps == 0) {
				return false;
			}

			final long thisNofComps = getDimension().getSize();
			if (thisNofComps != tempTypeNofComps) {
				info.setErrorStr(MessageFormat.format(NOFFIELDSDONTMATCH, thisNofComps, tempTypeNofComps));
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
				final IType tempElementType = getElementType().getTypeRefdLast(timestamp);
				lChain.markState();
				rChain.markState();
				lChain.add(tempElementType);
				rChain.add(tempTypeCfType);
				final TypeCompatibilityInfo infoTemp = new TypeCompatibilityInfo(tempElementType, tempTypeCfType, false);
				if (!tempElementType.equals(tempTypeCfType)
						&& !(lChain.hasRecursion() && rChain.hasRecursion())
						&& !tempElementType.isCompatible(timestamp, tempTypeCfType, infoTemp, lChain, rChain)) {
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
			final TTCN3_Sequence_Type tempType = (TTCN3_Sequence_Type) temp;
			final int tempTypeNofComps = tempType.getNofComponents();
			if (tempTypeNofComps == 0) {
				return false;
			}

			final long nofComps = getDimension().getSize();
			if (nofComps != tempTypeNofComps) {
				info.setErrorStr(MessageFormat.format(NOFFIELDSDONTMATCH, nofComps, tempTypeNofComps));
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
				final IType tempElementType = getElementType().getTypeRefdLast(timestamp);
				lChain.markState();
				rChain.markState();
				lChain.add(tempElementType);
				rChain.add(tempTypeCfType);
				final TypeCompatibilityInfo infoTemp = new TypeCompatibilityInfo(tempElementType, tempTypeCfType, false);
				if (!tempElementType.equals(tempTypeCfType)
						&& !(lChain.hasRecursion() && rChain.hasRecursion())
						&& !tempElementType.isCompatible(timestamp, tempTypeCfType, infoTemp, lChain, rChain)) {
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
			final SequenceOf_Type tempType = (SequenceOf_Type) temp;
			if (!tempType.isSubtypeCompatible(timestamp, this)) {
				info.setErrorStr("Incompatible record of/SEQUENCE OF subtypes");
				return false;
			}

			final IType tempTypeOfType = tempType.getOfType().getTypeRefdLast(timestamp);
			final IType tempElementType = getElementType().getTypeRefdLast(timestamp);
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
			lChain.add(tempElementType);
			rChain.add(tempTypeOfType);
			final TypeCompatibilityInfo infoTemp = new TypeCompatibilityInfo(tempElementType, tempTypeOfType, false);
			if (!tempElementType.equals(tempTypeOfType)
					&& !(lChain.hasRecursion() && rChain.hasRecursion())
					&& !tempElementType.isCompatible(timestamp, tempTypeOfType, infoTemp, lChain, rChain)) {
				info.appendOp1Ref(infoTemp.getOp1RefStr());
				if (infoTemp.getOp2RefStr().length() > 0) {
					info.appendOp2Ref("[]");
				}
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
			final Array_Type tempType = (Array_Type) temp;
			if (this == tempType) {
				return true;
			}
			if (dimension != null && tempType.dimension != null && !dimension.isIdentical(timestamp, tempType.dimension)) {
				info.setErrorStr(BADARRAYDIMENSION);
				return false;
			}

			final IType tempElementType = getElementType().getTypeRefdLast(timestamp);
			final IType tempTypeElementType = tempType.getElementType().getTypeRefdLast(timestamp);
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
			lChain.add(tempElementType);
			rChain.add(tempTypeElementType);
			final TypeCompatibilityInfo infoTemp = new TypeCompatibilityInfo(tempElementType, tempTypeElementType, false);
			if (!tempElementType.equals(tempTypeElementType)
					&& !(lChain.hasRecursion() && rChain.hasRecursion())
					&& !tempElementType.isCompatible(timestamp, tempTypeElementType, infoTemp, lChain, rChain)) {
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
	public boolean isIdentical(final CompilationTimeStamp timestamp, final IType type) {
		check(timestamp);
		type.check(timestamp);
		final IType temp = type.getTypeRefdLast(timestamp);

		if (getIsErroneous(timestamp) || temp.getIsErroneous(timestamp)) {
			return true;
		}

		if (!Type_type.TYPE_ARRAY.equals(temp.getTypetype())) {
			return false;
		}

		final Array_Type other = (Array_Type) temp;
		final boolean result = elementType != null && other.elementType != null && elementType.isIdentical(timestamp, other.elementType);
		return result && dimension != null && other.dimension != null && dimension.isIdentical(timestamp, other.dimension);
	}

	@Override
	/** {@inheritDoc} */
	public boolean isComponentInternal(final CompilationTimeStamp timestamp) {
		check(timestamp);

		return componentInternal;
	}

	@Override
	/** {@inheritDoc} */
	public void checkRecursions(final CompilationTimeStamp timestamp, final IReferenceChain referenceChain) {
		if (elementType != null && referenceChain.add(this)) {
			referenceChain.markState();
			elementType.checkRecursions(timestamp, referenceChain);
			referenceChain.previousState();
		}
	}

	@Override
	/** {@inheritDoc} */
	public void check(final CompilationTimeStamp timestamp) {
		if (lastTimeChecked != null && !lastTimeChecked.isLess(timestamp)) {
			return;
		}

		lastTimeChecked = timestamp;
		componentInternal = false;
		isErroneous = false;

		initAttributes(timestamp);

		if (elementType != null) {
			elementType.setGenName(getGenNameOwn(), "0");
			elementType.setParentType(this);
			elementType.check(timestamp);
			elementType.checkEmbedded(timestamp, elementType.getLocation(), true, "embedded into an array type");
			componentInternal = elementType.isComponentInternal(timestamp);
		}

		if (dimension != null) {
			dimension.check(timestamp);
		}

		if (myScope != null) {
			checkEncode(timestamp);
			checkVariants(timestamp);
		}
	}

	@Override
	/** {@inheritDoc} */
	public void checkComponentInternal(final CompilationTimeStamp timestamp, final Set<IType> typeSet, final String operation) {
		if (typeSet.contains(this)) {
			return;
		}

		if (elementType != null && elementType.isComponentInternal(timestamp)) {
			typeSet.add(this);
			elementType.checkComponentInternal(timestamp, typeSet, operation);
			typeSet.remove(this);
		}
	}

	@Override
	/** {@inheritDoc} */
	public boolean checkThisValue(final CompilationTimeStamp timestamp, final IValue value, final Assignment lhs, final ValueCheckingOptions valueCheckingOptions) {
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

		switch (last.getValuetype()) {
		case SEQUENCEOF_VALUE:
			last = last.setValuetype(timestamp, Value_type.ARRAY_VALUE);
			selfReference = checkThisValueArray(timestamp, value, lhs, (Array_Value) last, valueCheckingOptions.expected_value,
					valueCheckingOptions.incomplete_allowed, valueCheckingOptions.implicit_omit, valueCheckingOptions.str_elem);
			break;
		case ARRAY_VALUE:
			selfReference = checkThisValueArray(timestamp, value, lhs, (Array_Value) last, valueCheckingOptions.expected_value,
					valueCheckingOptions.incomplete_allowed, valueCheckingOptions.implicit_omit, valueCheckingOptions.str_elem);
			break;
		case EXPRESSION_VALUE:
		case MACRO_VALUE:
			// already checked
			break;
		default:
			value.getLocation().reportSemanticError(ARRAYVALUEEXPECTED);
			value.setIsErroneous(true);
		}

		value.setLastTimeChecked(timestamp);

		return selfReference;
	}

	private boolean checkThisValueArray(final CompilationTimeStamp timestamp, final IValue originalValue, final Assignment lhs,
			final Array_Value lastValue, final Expected_Value_type expectedValue,
			final boolean incompleteAllowed, final boolean implicitOmit, final boolean strElem) {
		if (dimension == null) {
			return false;
		}

		boolean selfReference = false;
		final int nofValues = lastValue.getNofComponents();

		if (!dimension.getIsErroneous(timestamp) && dimension.getSize() < nofValues) {
			originalValue.getLocation().reportSemanticError(MessageFormat.format(TOOMANYEXPECTED, dimension.getSize(), nofValues));
			originalValue.setIsErroneous(true);
		}

		if (lastValue.isIndexed()) {
			boolean checkHoles = !dimension.getIsErroneous(timestamp) && Expected_Value_type.EXPECTED_CONSTANT.equals(expectedValue);
			final long arraySize = dimension.getSize();
			BigInteger maxIndex = BigInteger.valueOf(-1);
			final Map<BigInteger, Integer> indexMap = new HashMap<BigInteger, Integer>(lastValue.getNofComponents());
			for (int i = 0, size = lastValue.getNofComponents(); i < size; i++) {
				final IValue component = lastValue.getValueByIndex(i);
				final Value index = lastValue.getIndexByIndex(i);
				dimension.checkIndex(timestamp, index, expectedValue);

				final IReferenceChain referenceChain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
				final IValue indexLast = index.getValueRefdLast(timestamp, referenceChain);
				referenceChain.release();

				if (indexLast.getIsErroneous(timestamp) || !Value_type.INTEGER_VALUE.equals(indexLast.getValuetype())) {
					checkHoles = false;
				} else {
					final BigInteger tempIndex = ((Integer_Value) indexLast).getValueValue();
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
						indexMap.put(tempIndex, Integer.valueOf(i + 1));
						if (maxIndex.compareTo(tempIndex) == -1) {
							maxIndex = tempIndex;
						}
					}
				}

				component.setMyGovernor(elementType);
				final IValue tempValue2 = elementType.checkThisValueRef(timestamp, component);
				selfReference |= elementType.checkThisValue(timestamp, tempValue2, lhs,
						new ValueCheckingOptions(expectedValue, incompleteAllowed, false, true, implicitOmit, strElem));
			}
			if (checkHoles) {
				if (indexMap.size() < arraySize) {
					lastValue.getLocation().reportSemanticError("It's not allowed to create hole(s) in constant values");
					originalValue.setIsErroneous(true);
				}
			}
		} else {
			if (!dimension.getIsErroneous(timestamp)) {
				final long arraySize = dimension.getSize();
				if (arraySize > nofValues) {
					originalValue.getLocation().reportSemanticError(MessageFormat.format(
							"Too few elements in the array value: {0} was expected instead of {1}", arraySize, nofValues));
					originalValue.setIsErroneous(true);
				} else if (arraySize < nofValues) {
					originalValue.getLocation().reportSemanticError(MessageFormat.format(
							"Too many elements in the array value: {0} was expected instead of {1}", arraySize, nofValues));
					originalValue.setIsErroneous(true);
				}
			}

			for (int i = 0, size = lastValue.getNofComponents(); i < size; i++) {
				final IValue component = lastValue.getValueByIndex(i);
				component.setMyGovernor(elementType);
				if (Value_type.NOTUSED_VALUE.equals(component.getValuetype())) {
					if (!incompleteAllowed) {
						component.getLocation().reportSemanticError(AbstractOfType.INCOMPLETEPRESENTERROR);
					}
				} else {
					final IValue tempValue2 = elementType.checkThisValueRef(timestamp, component);
					selfReference |= elementType.checkThisValue(timestamp, tempValue2, lhs,
							new ValueCheckingOptions(expectedValue, incompleteAllowed, false, true, implicitOmit, strElem));
				}
			}
		}

		return selfReference;
	}

	public IType getElementType() {
		return elementType;
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
		case PERMUTATION_MATCH: {
			final int nofComponents = ((PermutationMatch_Template) template).getNofTemplates();
			for (int i = 0; i < nofComponents; i++) {
				ITTCN3Template templateComponent = ((PermutationMatch_Template) template).getTemplateByIndex(i);
				templateComponent.setMyGovernor(elementType);
				templateComponent = elementType.checkThisTemplateRef(timestamp, templateComponent);
				selfReference |= templateComponent.checkThisTemplateGeneric(timestamp, elementType, isModified, false, true, true, false, lhs);
			}
			break;
		}
		case TEMPLATE_LIST: {
			final Template_List listTemplate = (Template_List) template;
			ITTCN3Template baseTemplate = listTemplate.getBaseTemplate();
			int nofBaseComponents = 0;
			if (baseTemplate != null) {
				baseTemplate = baseTemplate.getTemplateReferencedLast(timestamp, null);
				if (Template_type.TEMPLATE_LIST.equals(baseTemplate.getTemplatetype())) {
					nofBaseComponents = ((Template_List) baseTemplate).getNofTemplates();
				} else {
					baseTemplate = null;
				}
			}

			if (!dimension.getIsErroneous(timestamp)) {
				final long arraySize = dimension.getSize();
				final int nofComponents = listTemplate.getNofTemplates();
				boolean fixedSize = true;
				int templateSize = 0;
				for (int i = 0; i < nofComponents && fixedSize; i++) {
					final ITTCN3Template templateComponent = listTemplate.getTemplateByIndex(i);
					switch (templateComponent.getTemplatetype()) {
					case PERMUTATION_MATCH: {
						final PermutationMatch_Template permutationTemplate = (PermutationMatch_Template) templateComponent;
						if(permutationTemplate.containsAnyornoneOrPermutation(timestamp)) {
							fixedSize = false;
						} else {
							templateSize += permutationTemplate.getNofTemplatesNotAnyornone(timestamp);
						}
						break;
					}
					default:
						templateSize++;
						break;
					}
				}

				if (fixedSize) {
					if (arraySize < templateSize) {
						listTemplate.getLocation().reportSemanticError(MessageFormat.format(TOOMANYTEMPLATEELEMENTS, arraySize, templateSize));
					} else if (arraySize > templateSize) {
						listTemplate.getLocation().reportSemanticError(MessageFormat.format(TOOFEWTEMPLATEELEMENTS, arraySize, templateSize));
					}
				}
			}

			final int nofComponents = ((Template_List) template).getNofTemplates();
			for (int i = 0; i < nofComponents; i++) {
				ITTCN3Template templateComponent = listTemplate.getTemplateByIndex(i);
				templateComponent.setMyGovernor(elementType);
				if (baseTemplate != null && i < nofBaseComponents) {
					templateComponent.setBaseTemplate(((Template_List) baseTemplate).getTemplateByIndex(i));
				}
				templateComponent = elementType.checkThisTemplateRef(timestamp, templateComponent);
				switch (templateComponent.getTemplatetype()) {
				case PERMUTATION_MATCH:
					selfReference |= templateComponent.checkThisTemplateGeneric(timestamp, this, isModified, false, true, true, implicitOmit, lhs);
					break;
				case TEMPLATE_NOTUSED:
					if (!isModified) {
						templateComponent.getLocation().reportSemanticWarning(NOTUSEDNOTALLOWED);
					}
					break;
				default:
					selfReference |= templateComponent.checkThisTemplateGeneric(timestamp, elementType, isModified, false, true, true, implicitOmit, lhs);
					break;
				}
			}
			break;
		}
		case INDEXED_TEMPLATE_LIST:	{
			final Map<Long, Integer> indexMap = new HashMap<Long, Integer>();
			final Indexed_Template_List indexedTemplateList = (Indexed_Template_List) template;
			for (int i = 0; i < indexedTemplateList.getNofTemplates(); i++) {
				final IndexedTemplate indexedTemplate = indexedTemplateList.getIndexedTemplateByIndex(i);
				final Value indexValue = indexedTemplate.getIndex().getValue();

				dimension.checkIndex(timestamp, indexValue, Expected_Value_type.EXPECTED_DYNAMIC_VALUE);
				ITTCN3Template templateComponent = indexedTemplate.getTemplate();

				final IReferenceChain chain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
				final IValue lastValue = indexValue.getValueRefdLast(timestamp, chain);
				chain.release();
				if (Value_type.INTEGER_VALUE.equals(lastValue.getValuetype())) {
					final long index = ((Integer_Value) lastValue).getValue();
					if (index > Integer.MAX_VALUE) {
						indexValue.getLocation().reportSemanticError(MessageFormat.format(
								"An integer value less than `{0}'' was expected for indexing type `{1}'' instead of `{2}''",
								Integer.MAX_VALUE, getTypename(), index));
						indexValue.setIsErroneous(true);
					} else {
						if (indexMap.containsKey(index)) {
							indexValue.getLocation().reportSemanticError(MessageFormat.format(
									"Duplicate index value `{0}'' for component `{1}'' and `{2}''", index, i + 1, indexMap.get(index)));
							indexValue.setIsErroneous(true);
						} else {
							indexMap.put(index, i);
						}
					}
				}

				templateComponent.setMyGovernor(elementType);
				templateComponent = elementType.checkThisTemplateRef(timestamp, templateComponent);
				selfReference |= templateComponent.checkThisTemplateGeneric(timestamp, elementType, isModified, false, true, true, implicitOmit, lhs);
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
	public boolean canHaveCoding(final CompilationTimeStamp timestamp, final MessageEncoding_type coding) {
		if (insideCanHaveCoding) {
			insideCanHaveCoding = false;
			return true;
		}
		insideCanHaveCoding = true;

		if (coding != MessageEncoding_type.JSON) {
			insideCanHaveCoding = false;
			return false;
		}

		final boolean result = elementType.getTypeRefdLast(timestamp).canHaveCoding(timestamp, coding);

		insideCanHaveCoding = false;
		return result;
	}

	@Override
	/** {@inheritDoc} */
	public void setGenerateCoderFunctions(final CompilationTimeStamp timestamp, final MessageEncoding_type encodingType) {
		switch(encodingType) {
		case RAW:
		case JSON:
		//FIXME: add other encoding type
			break;
		default:
			return;
		}

		if (getGenerateCoderFunctions(encodingType)) {
			//already set
			return;
		}

		codersToGenerate.add(encodingType);
		elementType.getTypeRefdLast(timestamp).setGenerateCoderFunctions(timestamp, encodingType);
	}

	@Override
	/** {@inheritDoc} */
	public void checkCodingAttributes(final CompilationTimeStamp timestamp, final IReferenceChain refChain) {
		//TODO add checks for other encodings.

		if (refChain.contains(this)) {
			return;
		}

		refChain.add(this);
		refChain.markState();

		elementType.checkCodingAttributes(timestamp, refChain);

		refChain.previousState();
	}

	@Override
	/** {@inheritDoc} */
	public void getTypesWithNoCodingTable(final CompilationTimeStamp timestamp, final ArrayList<IType> typeList, final boolean onlyOwnTable) {
		if (typeList.contains(this)) {
			return;
		}

		if ((onlyOwnTable && codingTable.isEmpty()) || (!onlyOwnTable && getTypeWithCodingTable(timestamp, false) == null)) {
			typeList.add(this);
		}

		elementType.getTypesWithNoCodingTable(timestamp, typeList, onlyOwnTable);
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
		if (isErroneous || elementType == null || this == elementType) {
			return "Erroneous type";
		}

		final StringBuilder builder = new StringBuilder();
		builder.append(dimension.createStringRepresentation());
		IType temp = elementType;
		while (temp != null && Type_type.TYPE_ARRAY.equals(temp.getTypetype())) {
			final Array_Type tempArray = (Array_Type) temp;
			builder.append(tempArray.dimension.createStringRepresentation());
			temp = tempArray.elementType;
		}
		if (temp != null) {
			builder.insert(0, temp.getTypename());
		}

		return builder.toString();
	}

	@Override
	/** {@inheritDoc} */
	public String getOutlineIcon() {
		return "array.gif";
	}

	@Override
	/** {@inheritDoc} */
	public IType getFieldType(final CompilationTimeStamp timestamp, final Reference reference, final int actualSubReference,
			final Expected_Value_type expectedIndex,
			final IReferenceChain refChain, final boolean interruptIfOptional) {
		final List<ISubReference> subreferences = reference.getSubreferences();
		if (subreferences.size() <= actualSubReference) {
			return this;
		}

		final Expected_Value_type internalExpectation = (expectedIndex == Expected_Value_type.EXPECTED_TEMPLATE) ? Expected_Value_type.EXPECTED_DYNAMIC_VALUE
				: expectedIndex;
		final ISubReference subreference = subreferences.get(actualSubReference);
		switch (subreference.getReferenceType()) {
		case arraySubReference:
			final Value indexValue = ((ArraySubReference) subreference).getValue();
			indexValue.setLoweridToReference(timestamp);

			IType indexingType = indexValue.getExpressionGovernor(timestamp, expectedIndex);
			if (indexingType == null) {
				//an error was already reported.
				return null;
			}

			indexingType = indexingType.getTypeRefdLast(timestamp);
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

				IType embeddedType = elementType.getTypeRefdLast(timestamp);
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

				return embeddedType;
			} else {
				if (dimension != null) {
					dimension.checkIndex(timestamp, indexValue, expectedIndex);
				}

				if (elementType != null) {
					return elementType.getFieldType(timestamp, reference, actualSubReference + 1, internalExpectation, refChain, interruptIfOptional);
				}
			}

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
	public void checkMapParameter(final CompilationTimeStamp timestamp, final IReferenceChain refChain, final Location errorLocation) {
		if (refChain.contains(this)) {
			return;
		}

		refChain.add(this);
		if (elementType != null) {
			elementType.checkMapParameter(timestamp, refChain, errorLocation);
		}
	}

	@Override
	/** {@inheritDoc} */
	public StringBuilder getProposalDescription(final StringBuilder builder) {
		builder.append("array of ");
		if (elementType != null) {
			elementType.getProposalDescription(builder);
		}
		return builder;
	}

	@Override
	/** {@inheritDoc} */
	public void addProposal(final ProposalCollector propCollector, final int i) {
		final List<ISubReference> subreferences = propCollector.getReference().getSubreferences();
		if (subreferences.size() < i) {
			return;
		} else if (subreferences.size() == i) {
			final ISubReference subreference = subreferences.get(i - 1);
			if (Subreference_type.fieldSubReference.equals(subreference.getReferenceType())) {
				final String candidate = ((FieldSubReference) subreference).getId().getDisplayName();
				propCollector.addTemplateProposal(candidate,
						new Template(candidate + "[index]", candidate + " with index", propCollector.getContextIdentifier(),
								candidate + "[${index}]", false), TTCN3CodeSkeletons.SKELETON_IMAGE);
			}
			return;
		}

		final ISubReference subreference = subreferences.get(i);
		if (Subreference_type.arraySubReference.equals(subreference.getReferenceType())) {
			if (subreferences.size() > i + 1 && elementType != null) {
				elementType.addProposal(propCollector, i + 1);
			}
		}
	}

	@Override
	/** {@inheritDoc} */
	public void addDeclaration(final DeclarationCollector declarationCollector, final int i) {
		final List<ISubReference> subreferences = declarationCollector.getReference().getSubreferences();
		if (subreferences.size() <= i) {
			return;
		}

		final ISubReference subreference = subreferences.get(i);
		if (Subreference_type.arraySubReference.equals(subreference.getReferenceType())) {
			if (subreferences.size() > i + 1 && elementType != null) {
				elementType.addDeclaration(declarationCollector, i + 1);
			}
		}
	}

	@Override
	/** {@inheritDoc} */
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}

		if (elementType != null) {
			elementType.updateSyntax(reparser, false);
			reparser.updateLocation(elementType.getLocation());
		}

		if (dimension != null) {
			dimension.updateSyntax(reparser, false);
			reparser.updateLocation(dimension.getLocation());
		}

		if (subType != null) {
			subType.updateSyntax(reparser, false);
		}

		if (withAttributesPath != null) {
			withAttributesPath.updateSyntax(reparser, false);
			reparser.updateLocation(withAttributesPath.getLocation());
		}
	}

	@Override
	/** {@inheritDoc} */
	public void getEnclosingField(final int offset, final ReferenceFinder rf) {
		if (elementType == null) {
			return;
		}

		elementType.getEnclosingField(offset, rf);
	}

	@Override
	/** {@inheritDoc} */
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		super.findReferences(referenceFinder, foundIdentifiers);
		if (elementType != null) {
			elementType.findReferences(referenceFinder, foundIdentifiers);
		}
		if (dimension != null) {
			dimension.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	@Override
	/** {@inheritDoc} */
	protected boolean memberAccept(final ASTVisitor v) {
		if (!super.memberAccept(v)) {
			return false;
		}
		if (elementType!=null && !elementType.accept(v)) {
			return false;
		}
		if (dimension!=null && !dimension.accept(v)) {
			return false;
		}
		return true;
	}

	@Override
	/** {@inheritDoc} */
	public Declaration resolveReference(final Reference reference, final int subRefIdx, final ISubReference lastSubreference) {
		if (elementType == null) {
			return null;
		}

		final IType refdLastOfType = elementType.getTypeRefdLast(CompilationTimeStamp.getBaseTimestamp());
		if (refdLastOfType instanceof IReferenceableElement) {
			return ((IReferenceableElement) refdLastOfType).resolveReference(reference, subRefIdx + 1, lastSubreference);
		}

		return null;
	}

	@Override
	/** {@inheritDoc} */
	public boolean isPresentAnyvalueEmbeddedField(final ExpressionStruct expression, final List<ISubReference> subreferences, final int beginIndex) {
		if (subreferences == null || getIsErroneous(CompilationTimeStamp.getBaseTimestamp())) {
			return true;
		}

		if (beginIndex >= subreferences.size()) {
			return true;
		}

		final ISubReference subReference = subreferences.get(beginIndex);
		if (!(subReference instanceof ArraySubReference)) {
			ErrorReporter.INTERNAL_ERROR("Code generator reached erroneous type reference `" + getFullName() + "''");
			expression.expression.append("FATAL_ERROR encountered while processing `" + getFullName() + "''\n");
			return true;
		}

		if (elementType == null) {
			return true;
		}

		return elementType.isPresentAnyvalueEmbeddedField(expression, subreferences, beginIndex + 1);
	}

	@Override
	/** {@inheritDoc} */
	public String getGenNameValue(final JavaGenData aData, final StringBuilder source) {
		if (inTypeDefinition) {
			lastBuildTimestamp = aData.getBuildTimstamp();
			lastGenName = getGenNameOwn(aData);
		} else if(lastBuildTimestamp == null || lastBuildTimestamp.isLess(aData.getBuildTimstamp())) {
			lastBuildTimestamp = aData.getBuildTimstamp();
			lastGenName = aData.getTemporaryVariableName();
		}

		return lastGenName;
	}

	@Override
	/** {@inheritDoc} */
	public String getGenNameTemplate(final JavaGenData aData, final StringBuilder source) {
		if (inTypeDefinition) {
			lastBuildTimestamp = aData.getBuildTimstamp();
			lastGenName = getGenNameOwn(aData);
		} else if(lastBuildTimestamp == null || lastBuildTimestamp.isLess(aData.getBuildTimstamp())) {
			lastBuildTimestamp = aData.getBuildTimstamp();
			lastGenName = aData.getTemporaryVariableName();
		}

		return lastGenName + "_template";
	}

	@Override
	/** {@inheritDoc} */
	public String getGenNameJsonDescriptor(final JavaGenData aData, final StringBuilder source) {
		generateCodeJsonDescriptor(aData, source);

		return getGenNameOwn(aData) + "_json_";
	}

	@Override
	/** {@inheritDoc} */
	public void generateCode(final JavaGenData aData, final StringBuilder source) {
		if (lastTimeGenerated != null && !lastTimeGenerated.isLess(aData.getBuildTimstamp())) {
			return;
		}

		lastTimeGenerated = aData.getBuildTimstamp();

		if (!inTypeDefinition) {
			return;
		}

		aData.addBuiltinTypeImport("Base_Template");

		elementType.generateCode(aData, source);

		final String ownName = getGenNameValue(aData, source);
		final String valueName = dimension.getValueType(aData, source, elementType, myScope);
		final String templateName = dimension.getTemplateType(aData, source, elementType, myScope);
		final String elementName = elementType.getGenNameValue(aData, source);

		generateCodeTypedescriptor(aData, source);

		source.append(MessageFormat.format("public static class {0} extends {1} '{' \n", ownName, valueName));
		if (aData.isDebug()) {
			source.append("/**\n");
			source.append(" * Initializes to unbound value.\n");
			source.append(" * */\n");
		}
		source.append(MessageFormat.format("public {0}() '{'\n", ownName));
		source.append(MessageFormat.format("super({0}.class, {1,number,#},{2,number,#});\n",elementName,dimension.getSize(), dimension.getOffset()));
		source.append("}\n\n");

		if (aData.isDebug()) {
			source.append("/**\n");
			source.append(" * Initializes to a given value.\n");
			source.append(" *\n");
			source.append(" * @param otherValue\n");
			source.append(" *                the value to initialize to.\n");
			source.append(" * */\n");
		}
		source.append(MessageFormat.format("public {0}({0} otherValue) '{'\n", ownName));
		source.append("super(otherValue);\n");
		source.append("}\n");
		source.append("}\n\n");

		source.append(MessageFormat.format("public static class {0}_template extends {1} '{'\n", ownName, templateName));
		if (aData.isDebug()) {
			source.append("/**\n");
			source.append(" * Initializes to unbound/uninitialized template.\n");
			source.append(" * */\n");
		}
		source.append(MessageFormat.format("public {0}_template() '{'\n",ownName));
		source.append(MessageFormat.format("super({0}.class, {0}_template.class, {1,number,#}, {2,number,#});\n",elementName,dimension.getSize(), dimension.getOffset()));
		source.append("}\n\n");

		if (aData.isDebug()) {
			source.append("/**\n");
			source.append(" * Initializes to a given template.\n");
			source.append(" *\n");
			source.append(" * @param otherValue\n");
			source.append(" *                the template to initialize to.\n");
			source.append(" * */\n");
		}
		source.append(MessageFormat.format("public {0}_template({0}_template otherValue) '{'\n",ownName));
		source.append("super(otherValue);\n");
		source.append("}\n\n");

		if (aData.isDebug()) {
			source.append("/**\n");
			source.append(" * Initializes to a given value.\n");
			source.append(" * Copies all of the fields and the template becomes a specific template.\n");
			source.append(" *\n");
			source.append(" * @param otherValue\n");
			source.append(" *                the value to initialize to.\n");
			source.append(" * */\n");
		}
		source.append(MessageFormat.format("public {0}_template({0} otherValue) '{'\n",ownName));
		source.append(MessageFormat.format("super({0}_template.class, otherValue);\n", elementName));
		source.append("}\n\n");

		if (aData.isDebug()) {
			source.append("/**\n");
			source.append(" * Initializes to a given template kind.\n");
			source.append(" *\n");
			source.append(" * @param otherValue\n");
			source.append(" *                the template kind to initialize to.\n");
			source.append(" * */\n");
		}
		source.append(MessageFormat.format("public {0}_template(final Base_Template.template_sel otherValue) '{'\n",ownName));
		source.append(MessageFormat.format("super({0}.class, {0}_template.class, otherValue);\n",elementName));
		source.append("}\n\n");

		source.append("@Override\n");
		source.append(MessageFormat.format("public {0} valueof() '{'\n", ownName));
		source.append(MessageFormat.format("return ({0})super.valueof();\n", ownName));
		source.append("}\n");

		source.append("@Override\n");
		source.append("public void set_type(final template_sel templateType, final int length) {\n");
		source.append("clean_up();\n");
		source.append("switch (templateType) {\n");
		source.append("case VALUE_LIST:\n");
		source.append("case COMPLEMENTED_LIST:\n");
		source.append("listSize = length;\n");
		source.append(MessageFormat.format("value_list = new {0}_template[listSize];\n", ownName));
		source.append("for (int i = 0; i < length; ++i) {\n");
		source.append(MessageFormat.format("value_list[i] = new {0}_template();\n", ownName));
		source.append("}\n");
		source.append("\n");
		source.append("break;\n");
		source.append("default:\n");
		source.append("throw new TtcnError(\"Internal error: Setting an invalid type for an array template.\");\n");
		source.append("}\n");
		source.append("set_selection(templateType);\n");
		source.append("}\n");

		source.append("@Override\n");
		source.append(MessageFormat.format("public {0}_template list_item(final int index) '{'\n", ownName));
		source.append(MessageFormat.format("return ({0}_template)super.list_item(index);\n", ownName));
		source.append("}\n");
		source.append("}\n\n");

		if (hasDoneAttribute()) {
			generateCodeDone(aData, source);
		}
		if (subType != null) {
			subType.generateCode(aData, source);
		}

		generateCodeForCodingHandlers(aData, source);
	}

	/**
	 * Generate the value class to represent an array.
	 * (Also generates the value classes of the of type if it is an array)
	 *
	 * @param aData only used to update imports if needed
	 * @param source where the source code should be generated
	 */
	public void generateCodeValue( final JavaGenData aData, final StringBuilder source) {
		final String className = getGenNameValue(aData, source);

		final IType elementType = getElementType();
		final String ofType = elementType.getGenNameValue( aData, source );
		if ( elementType.getTypetype() == Type_type.TYPE_ARRAY ) {
			((Array_Type)elementType).generateCodeValue( aData, source);
		}

		final ArrayDimension dim = getDimension();

		aData.addBuiltinTypeImport("TitanValue_Array");

		source.append(MessageFormat.format("public static class {0} extends TitanValue_Array<{1}> '{'\n", className, ofType));
		source.append(MessageFormat.format("public {0}() '{'\n", className));
		source.append(MessageFormat.format("super({0}.class, {1,number,#} , {2,number,#});\n", ofType, dim.getSize(), dim.getOffset()));
		source.append("}\n");
		source.append(MessageFormat.format("public {0}({0} otherValue) '{'\n", className));
		source.append("super(otherValue);\n");
		source.append("}\n");
		source.append(MessageFormat.format("public {0}(final TitanValue_Array<{1}> otherValue) '{'\n", className, ofType));
		source.append("super(otherValue);\n");
		source.append("}\n");
		source.append("}\n\n");
	}

	/**
	 * Generate the template class to represent an array.
	 * (Also generates the template classes of the of type if it is an array)
	 *
	 * @param aData only used to update imports if needed
	 * @param source where the source code should be generated
	 */
	public void generateCodeTemplate( final JavaGenData aData, final StringBuilder source) {
		final String className = getGenNameValue(aData, source);
		final String classTemplateName = getGenNameTemplate(aData, source);

		final IType elementType = getElementType();
		final String ofValueType = elementType.getGenNameValue(aData, source);
		final String ofTemplateType = elementType.getGenNameTemplate(aData, source);

		if(elementType.getTypetype() == Type_type.TYPE_ARRAY) {
			((Array_Type)elementType).generateCodeTemplate(aData, source);
		}

		final ArrayDimension dim = getDimension();

		aData.addBuiltinTypeImport("TitanTemplate_Array");
		aData.addBuiltinTypeImport("Base_Template.template_sel");
		aData.addBuiltinTypeImport("Optional");

		source.append(MessageFormat.format("public static class {0} extends TitanTemplate_Array<{1}, {2}> '{'\n", classTemplateName, ofValueType, ofTemplateType));
		source.append(MessageFormat.format("public {0}() '{'\n", classTemplateName));
		source.append(MessageFormat.format("super({0}.class, {1}.class, {2,number,#}, {3,number,#});\n", ofValueType, ofTemplateType, dim.getSize(), dim.getOffset()));
		source.append("}\n");

		source.append(MessageFormat.format("public {0}(final Class<{2}> classTemplate, final TitanValue_Array<{1}> otherValue) '{'\n", classTemplateName, ofValueType, ofTemplateType));
		source.append("super(classTemplate, otherValue);\n");
		source.append("}\n");

		source.append(MessageFormat.format("public {0}(final Optional<{1}> otherValue) '{'\n", classTemplateName, className));
		source.append(MessageFormat.format("super({0}.class, {1}.class, {2,number,#}, {3,number,#});\n", ofValueType, ofTemplateType, dim.getSize(), dim.getOffset()));
		source.append("switch (otherValue.get_selection()) {\n");
		source.append("case OPTIONAL_PRESENT:\n");
		source.append("set_selection(template_sel.SPECIFIC_VALUE);\n");
		source.append("copy_value(otherValue.constGet());\n");
		source.append("break;\n");
		source.append("case OPTIONAL_OMIT:\n");
		source.append("set_selection(template_sel.OMIT_VALUE);\n");
		source.append("break;\n");
		source.append("case OPTIONAL_UNBOUND:\n");
		source.append("throw new TtcnError(\"Creating an array template from an unbound optional field.\");\n");
		source.append("}\n");
		source.append("}\n\n");

		source.append("@Override\n");
		source.append(MessageFormat.format("public {0} valueof() '{'\n", className));
		source.append(MessageFormat.format("return ({0})super.valueof();\n", className));
		source.append("}\n");
		source.append("}\n");
	}

	@Override
	/** {@inheritDoc} */
	public void generateCodeIsPresentBoundChosen(final JavaGenData aData, final ExpressionStruct expression, final List<ISubReference> subreferences,
			final int subReferenceIndex, final String globalId, final String externalId, final boolean isTemplate, final Operation_type optype, final String field, final Scope targetScope) {
		if (subreferences == null || getIsErroneous(CompilationTimeStamp.getBaseTimestamp())) {
			return;
		}

		if (subReferenceIndex >= subreferences.size()) {
			return;
		}

		final StringBuilder closingBrackets = new StringBuilder();
		if(isTemplate) {
			boolean anyvalueReturnValue = true;
			if (optype == Operation_type.ISPRESENT_OPERATION) {
				anyvalueReturnValue = isPresentAnyvalueEmbeddedField(expression, subreferences, subReferenceIndex);
			} else if (optype == Operation_type.ISCHOOSEN_OPERATION || optype == Operation_type.ISVALUE_OPERATION) {
				anyvalueReturnValue = false;
			}

			expression.expression.append(MessageFormat.format("if({0}) '{'\n", globalId));
			expression.expression.append(MessageFormat.format("switch({0}.get_selection()) '{'\n", externalId));
			expression.expression.append("case UNINITIALIZED_TEMPLATE:\n");
			expression.expression.append(MessageFormat.format("{0} = false;\n", globalId));
			expression.expression.append("break;\n");
			expression.expression.append("case ANY_VALUE:\n");
			expression.expression.append(MessageFormat.format("{0} = {1};\n", globalId, anyvalueReturnValue?"true":"false"));
			expression.expression.append("break;\n");
			expression.expression.append("case SPECIFIC_VALUE:{\n");

			closingBrackets.append("break;}\n");
			closingBrackets.append("default:\n");
			closingBrackets.append(MessageFormat.format("{0} = false;\n", globalId));
			closingBrackets.append("break;\n");
			closingBrackets.append("}\n");
			closingBrackets.append("}\n");
		}

		final ISubReference subReference = subreferences.get(subReferenceIndex);
		if (!(subReference instanceof ArraySubReference)) {
			ErrorReporter.INTERNAL_ERROR("Code generator reached erroneous type reference `" + getFullName() + "''");
			expression.expression.append("FATAL_ERROR encountered while processing `" + getFullName() + "''\n");
			return;
		}

		final IType nextType = elementType;
		final Value indexValue = ((ArraySubReference) subReference).getValue();
		final IReferenceChain referenceChain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
		final IValue last = indexValue.getValueRefdLast(CompilationTimeStamp.getBaseTimestamp(), referenceChain);
		referenceChain.release();

		expression.expression.append(MessageFormat.format("if({0}) '{'\n", globalId));
		closingBrackets.insert(0, "}\n");

		final String temporalIndexId = aData.getTemporaryVariableName();
		expression.expression.append(MessageFormat.format("final TitanInteger {0} = ", temporalIndexId));
		last.generateCodeExpressionMandatory(aData, expression, true);
		expression.expression.append(";\n");
		expression.expression.append(MessageFormat.format("{0} = {1}.is_greater_than_or_equal(0) && {1}.is_less_than({2}.{3});\n",
				globalId, temporalIndexId, externalId, isTemplate?"n_elem()":"size_of()"));

		expression.expression.append(MessageFormat.format("if({0}) '{'\n", globalId));
		closingBrackets.insert(0, "}\n");

		final String temporalId = aData.getTemporaryVariableName();
		final String nextTypeGenName = isTemplate ? nextType.getGenNameTemplate(aData, expression.expression) : nextType.getGenNameValue(aData, expression.expression);
		expression.expression.append(MessageFormat.format("final {0} {1} = {2}.constGet_at({3});\n", nextTypeGenName,
				temporalId, externalId, temporalIndexId));

		final boolean isLast = subReferenceIndex == (subreferences.size() - 1);
		if (optype == Operation_type.ISBOUND_OPERATION) {
			expression.expression.append(MessageFormat.format("{0} = {1}.is_bound();\n", globalId, temporalId));
		} else if (optype == Operation_type.ISVALUE_OPERATION) {
			expression.expression.append(MessageFormat.format("{0} = {1}.is_value();\n", globalId, temporalId));
		} else if (optype == Operation_type.ISPRESENT_OPERATION) {
			expression.expression.append(MessageFormat.format("{0} = {1}.{2}({3});\n", globalId,  temporalId, !isLast?"is_bound":"is_present", isLast && isTemplate && aData.getAllowOmitInValueList()?"true":""));
		} else if (optype == Operation_type.ISCHOOSEN_OPERATION) {
			expression.expression.append(MessageFormat.format("{0} = {1}.is_bound();\n", globalId, temporalId));
			if (subReferenceIndex==subreferences.size()-1) {
				expression.expression.append(MessageFormat.format("if ({0}) '{'\n", globalId));
				expression.expression.append(MessageFormat.format("{0} = {1}.ischosen({2});\n", globalId, temporalId, field));
				expression.expression.append("}\n");
			}
		}

		nextType.generateCodeIsPresentBoundChosen(aData, expression, subreferences, subReferenceIndex + 1, globalId, temporalId, isTemplate, optype, field, targetScope);

		expression.expression.append(closingBrackets);
	}

	@Override
	/** {@inheritDoc} */
	public String generateConversion(final JavaGenData aData, final IType fromType, final String fromName, final ExpressionStruct expression) {
		final IType refdType = fromType.getTypeRefdLast(CompilationTimeStamp.getBaseTimestamp());
		if (refdType == null || this == refdType) {
			//no need to convert
			return fromName;
		}

		switch(refdType.getTypetype()) {
		//TODO TTCN and ASN sequence branches can be unified if getNofComponents can be the same in both.
		case TYPE_TTCN3_SEQUENCE: {
			//heavy conversion is needed
			final TTCN3_Sequence_Type realFromType = (TTCN3_Sequence_Type) refdType;
			return generateConversionTTCNSeqToArray(aData, realFromType, fromName, expression);
		}
		case TYPE_ASN1_SEQUENCE: {
			//heavy conversion is needed
			final ASN1_Sequence_Type realFromType = (ASN1_Sequence_Type) refdType;
			return generateConversionASNSeqToArray(aData, realFromType, fromName, expression);
		}
		case TYPE_SEQUENCE_OF: {
			final IType fromOfType = ((SequenceOf_Type)refdType).getOfType();
			return generateConversionSeqOfToArray(aData, (SequenceOf_Type)refdType, fromName, fromOfType, expression);
		}
		case TYPE_ARRAY: {
			return generateConversionArrayToArray(aData, (Array_Type)refdType, fromName, expression);
		}
		default:
			return "FATAL ERROR during converting to type " + getTypename();
		}
	}

	private String generateConversionSeqOfToArray(final JavaGenData aData, final SequenceOf_Type fromType, final String fromName, final IType fromOfType, final ExpressionStruct expression) {
		//heavy conversion is needed
		final String tempId = aData.getTemporaryVariableName();

		final String name = getGenNameValue(aData, expression.preamble);
		expression.preamble.append(MessageFormat.format("final {0} {1} = new {0}();\n", name, tempId));
		final String ConversionFunctionName = Type.getConversionFunction(aData, fromType, this, expression.preamble);
		expression.preamble.append(MessageFormat.format("if(!{0}({1}, {2})) '{'\n", ConversionFunctionName, tempId, fromName));
		expression.preamble.append(MessageFormat.format("throw new TtcnError(\"Values or templates of type `{0}'' and `{1}'' are not compatible at run-time\");\n", getTypename(), fromType.getTypename()));
		expression.preamble.append("}\n");

		if (!aData.hasTypeConversion(ConversionFunctionName)) {
			long to_offset = getDimension().getOffset();
			final StringBuilder conversionFunctionBody = new StringBuilder();
			conversionFunctionBody.append(MessageFormat.format("\tpublic static boolean {0}(final {1} to, final {2} from) '{'\n", ConversionFunctionName, name, fromType.getGenNameValue( aData, conversionFunctionBody )));
			conversionFunctionBody.append(MessageFormat.format("\t\tif(!from.is_bound() || from.size_of().get_int() != {0}) '{'\n", getDimension().getSize()));
			conversionFunctionBody.append("\t\t\treturn false;\n");
			conversionFunctionBody.append("\t\t}\n\n");

			for (int i = 0; i < getDimension().getSize(); i++) {
				final String tempId2 = aData.getTemporaryVariableName();
				conversionFunctionBody.append(MessageFormat.format("\t\tfinal {0} {1} = from.constGet_at({2});\n", fromOfType.getGenNameValue(aData, conversionFunctionBody), tempId2, i));
				conversionFunctionBody.append(MessageFormat.format("\t\tif({0}.is_bound()) '{'\n", tempId2));

				final ExpressionStruct tempExpression = new ExpressionStruct();
				final String tempId3 = elementType.generateConversion(aData, fromOfType, tempId2, tempExpression);
				tempExpression.openMergeExpression(conversionFunctionBody);

				conversionFunctionBody.append(MessageFormat.format("\t\t\tto.get_at({0}).operator_assign({1});\n", to_offset + i, tempId3));
				conversionFunctionBody.append("\t\t}\n");
			}

			conversionFunctionBody.append("\t\treturn true;\n");
			conversionFunctionBody.append("\t}\n\n");
			aData.addTypeConversion(ConversionFunctionName, conversionFunctionBody.toString());
		}

		return tempId;
	}

	private String generateConversionArrayToArray(final JavaGenData aData, final Array_Type fromType, final String fromName, final ExpressionStruct expression) {
		//heavy conversion is needed
		final String tempId = aData.getTemporaryVariableName();

		final String name = getGenNameValue(aData, expression.preamble);
		expression.preamble.append(MessageFormat.format("final {0} {1} = new {0}();\n", name, tempId));
		final String ConversionFunctionName = Type.getConversionFunction(aData, fromType, this, expression.preamble);
		expression.preamble.append(MessageFormat.format("if(!{0}({1}, {2})) '{'\n", ConversionFunctionName, tempId, fromName));
		expression.preamble.append(MessageFormat.format("throw new TtcnError(\"Values or templates of type `{0}'' and `{1}'' are not compatible at run-time\");\n", getTypename(), fromType.getTypename()));
		expression.preamble.append("}\n");

		if (!aData.hasTypeConversion(ConversionFunctionName)) {
			final IType fromOfType = fromType.getElementType();
			long from_offset = fromType.getDimension().getOffset();
			long to_offset = getDimension().getOffset();
			final StringBuilder conversionFunctionBody = new StringBuilder();
			conversionFunctionBody.append(MessageFormat.format("\tpublic static boolean {0}(final {1} to, final {2} from) '{'\n", ConversionFunctionName, name, fromType.getGenNameValue( aData, conversionFunctionBody )));

			for (int i = 0; i < getDimension().getSize(); i++) {
				final String tempId2 = aData.getTemporaryVariableName();
				conversionFunctionBody.append(MessageFormat.format("\t\tfinal {0} {1} = from.constGet_at({2});\n", fromOfType.getGenNameValue(aData, conversionFunctionBody), tempId2, from_offset + i));
				conversionFunctionBody.append(MessageFormat.format("\t\tif({0}.is_bound()) '{'\n", tempId2));

				final ExpressionStruct tempExpression = new ExpressionStruct();
				final String tempId3 = elementType.generateConversion(aData, fromOfType, tempId2, tempExpression);
				tempExpression.openMergeExpression(conversionFunctionBody);

				conversionFunctionBody.append(MessageFormat.format("\t\t\tto.get_at({0}).operator_assign({1});\n", to_offset + i, tempId3));
				conversionFunctionBody.append("\t\t}\n");
			}

			conversionFunctionBody.append("\t\treturn true;\n");
			conversionFunctionBody.append("\t}\n\n");
			aData.addTypeConversion(ConversionFunctionName, conversionFunctionBody.toString());
		}

		return tempId;
	}

	private String generateConversionTTCNSeqToArray(final JavaGenData aData, final TTCN3_Sequence_Type fromType, final String fromName, final ExpressionStruct expression) {
		//heavy conversion is needed
		final String tempId = aData.getTemporaryVariableName();

		final String name = getGenNameValue(aData, expression.preamble);
		expression.preamble.append(MessageFormat.format("final {0} {1} = new {0}();\n", name, tempId));
		final String ConversionFunctionName = Type.getConversionFunction(aData, fromType, this, expression.preamble);
		expression.preamble.append(MessageFormat.format("if(!{0}({1}, {2})) '{'\n", ConversionFunctionName, tempId, fromName));
		expression.preamble.append(MessageFormat.format("throw new TtcnError(\"Values or templates of type `{0}'' and `{1}'' are not compatible at run-time\");\n", getTypename(), fromType.getTypename()));
		expression.preamble.append("}\n");

		if (!aData.hasTypeConversion(ConversionFunctionName)) {
			final int fromComponentCount = fromType.getNofComponents();
			long to_offset = getDimension().getOffset();
			final StringBuilder conversionFunctionBody = new StringBuilder();
			conversionFunctionBody.append(MessageFormat.format("\tpublic static boolean {0}(final {1} to, final {2} from) '{'\n", ConversionFunctionName, name, fromType.getGenNameValue( aData, conversionFunctionBody )));
			conversionFunctionBody.append(MessageFormat.format("\t\tint index = {0};\n", to_offset));

			for (int i = 0; i < fromComponentCount; i++) {
				final CompField fromComp = fromType.getComponentByIndex(i);
				final Identifier fromFieldName = fromComp.getIdentifier();
				final IType fromFieldType = fromComp.getType().getTypeRefdLast(CompilationTimeStamp.getBaseTimestamp());

				final String tempId2 = aData.getTemporaryVariableName();
				conversionFunctionBody.append(MessageFormat.format("\t\tfinal {0} {1} = from.constGet_field_{2}();\n", fromFieldType.getGenNameValue(aData, conversionFunctionBody), tempId2, FieldSubReference.getJavaGetterName( fromFieldName.getName() )));
				conversionFunctionBody.append(MessageFormat.format("\t\tif({0}.is_bound()) '{'\n", tempId2));

				final ExpressionStruct tempExpression = new ExpressionStruct();
				final String tempId3 = elementType.generateConversion(aData, fromFieldType, tempId2, tempExpression);
				tempExpression.openMergeExpression(conversionFunctionBody);

				conversionFunctionBody.append(MessageFormat.format("\t\t\tto.get_at(index).operator_assign({0});\n", tempId3));
				conversionFunctionBody.append("\t\t}\n");
				conversionFunctionBody.append("\t\tindex++;\n");
			}

			conversionFunctionBody.append("\t\treturn true;\n");
			conversionFunctionBody.append("\t}\n\n");
			aData.addTypeConversion(ConversionFunctionName, conversionFunctionBody.toString());
		}

		return tempId;
	}

	private String generateConversionASNSeqToArray(final JavaGenData aData, final ASN1_Sequence_Type fromType, final String fromName, final ExpressionStruct expression) {
		//heavy conversion is needed
		final String tempId = aData.getTemporaryVariableName();

		final String name = getGenNameValue(aData, expression.preamble);
		expression.preamble.append(MessageFormat.format("final {0} {1} = new {0}();\n", name, tempId));
		final String ConversionFunctionName = Type.getConversionFunction(aData, fromType, this, expression.preamble);
		expression.preamble.append(MessageFormat.format("if(!{0}({1}, {2})) '{'\n", ConversionFunctionName, tempId, fromName));
		expression.preamble.append(MessageFormat.format("throw new TtcnError(\"Values or templates of type `{0}'' and `{1}'' are not compatible at run-time\");\n", getTypename(), fromType.getTypename()));
		expression.preamble.append("}\n");

		if (!aData.hasTypeConversion(ConversionFunctionName)) {
			final int fromComponentCount = fromType.getNofComponents();
			long to_offset = getDimension().getOffset();
			final StringBuilder conversionFunctionBody = new StringBuilder();
			conversionFunctionBody.append(MessageFormat.format("\tpublic static boolean {0}(final {1} to, final {2} from) '{'\n", ConversionFunctionName, name, fromType.getGenNameValue( aData, conversionFunctionBody )));
			conversionFunctionBody.append(MessageFormat.format("\t\tint index = {0};\n", to_offset));

			for (int i = 0; i < fromComponentCount; i++) {
				final CompField fromComp = fromType.getComponentByIndex(i);
				final Identifier fromFieldName = fromComp.getIdentifier();
				final IType fromFieldType = fromComp.getType().getTypeRefdLast(CompilationTimeStamp.getBaseTimestamp());

				final String tempId2 = aData.getTemporaryVariableName();
				conversionFunctionBody.append(MessageFormat.format("\t\tfinal {0} {1} = from.constGet_field_{2}();\n", fromFieldType.getGenNameValue(aData, conversionFunctionBody), tempId2, FieldSubReference.getJavaGetterName( fromFieldName.getName() )));
				conversionFunctionBody.append(MessageFormat.format("\t\tif({0}.is_bound()) '{'\n", tempId2));

				final ExpressionStruct tempExpression = new ExpressionStruct();
				final String tempId3 = elementType.generateConversion(aData, fromFieldType, tempId2, tempExpression);
				tempExpression.openMergeExpression(conversionFunctionBody);

				conversionFunctionBody.append(MessageFormat.format("\t\t\tto.get_at(index).operator_assign({0});\n", tempId3));
				conversionFunctionBody.append("\t\t}\n");
				conversionFunctionBody.append("\t\tindex++;\n");
			}

			conversionFunctionBody.append("\t\treturn true;\n");
			conversionFunctionBody.append("\t}\n\n");
			aData.addTypeConversion(ConversionFunctionName, conversionFunctionBody.toString());
		}

		return tempId;
	}
}
