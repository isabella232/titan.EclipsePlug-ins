/******************************************************************************
 * Copyright (c) 2000-2020 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.ASN1.types;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.common.parsers.SyntacticErrorStorage;
import org.eclipse.titan.designer.AST.ArraySubReference;
import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.FieldSubReference;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.ISubReference;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.IValue.Value_type;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.AST.ParameterisedSubReference;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.Type;
import org.eclipse.titan.designer.AST.TypeCompatibilityInfo;
import org.eclipse.titan.designer.AST.ASN1.Block;
import org.eclipse.titan.designer.AST.ASN1.IASN1Type;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.attributes.JsonAST;
import org.eclipse.titan.designer.AST.TTCN3.templates.ITTCN3Template;
import org.eclipse.titan.designer.AST.TTCN3.templates.ITTCN3Template.Completeness_type;
import org.eclipse.titan.designer.AST.TTCN3.templates.ITTCN3Template.Template_type;
import org.eclipse.titan.designer.AST.TTCN3.templates.NamedTemplate;
import org.eclipse.titan.designer.AST.TTCN3.templates.Named_Template_List;
import org.eclipse.titan.designer.AST.TTCN3.types.CompField;
import org.eclipse.titan.designer.AST.TTCN3.types.TTCN3_Choice_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.TTCN3_Set_Seq_Choice_BaseType;
import org.eclipse.titan.designer.AST.TTCN3.types.UnionGenerator;
import org.eclipse.titan.designer.AST.TTCN3.types.UnionGenerator.FieldInfo;
import org.eclipse.titan.designer.AST.TTCN3.values.Choice_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Expression_Value.Operation_type;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.ExpressionStruct;
import org.eclipse.titan.designer.compiler.JavaGenData;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ParserMarkerSupport;
import org.eclipse.titan.designer.parsers.asn1parser.Asn1Parser;
import org.eclipse.titan.designer.parsers.asn1parser.BlockLevelTokenStreamTracker;

/**
 * @author Kristof Szabados
 * @author Arpad Lovassy
 * */
public final class ASN1_Choice_Type extends ASN1_Set_Seq_Choice_BaseType {
	private static final String MISSINGALTERNATIVE = "CHOICE type must have at least one alternative";
	private static final String TEMPLATENOTALLOWED = "{0} cannot be used for union type `{1}''";
	private static final String LENGTHRESTRICTIONNOTALLOWED = "Length restriction is not allowed for union type `{0}''";
	private static final String ONEFIELDEXPECTED = "A template for union type must contain exactly one selected field";
	private static final String REFERENCETONONEXISTENTFIELD = "Reference to non-existent field `{0}'' in union template for type `{1}''";
	private static final String CHOICEEXPECTED = "CHOICE value was expected for type `{0}''";
	private static final String UNIONEXPECTED = "Union value was expected for type `{0}''";
	private static final String NONEXISTENTCHOICE = "Reference to a non-existent alternative `{0}'' in CHOICE value for type `{1}''";
	private static final String NONEXISTENTUNION = "Reference to a non-existent field `{0}'' in union value for type `{1}''";

	private static final String NOCOMPATIBLEFIELD = "union/CHOICE type `{0}'' doesn''t have any field compatible with `{1}''";
	private static final String NOTCOMPATIBLEUNION = "union/CHOICE types are compatible only with other union/CHOICE types";

	public ASN1_Choice_Type(final Block aBlock) {
		this.mBlock = aBlock;
		setLocation(new Location(aBlock.getLocation()));
	}

	public IASN1Type newInstance() {
		return new ASN1_Choice_Type(mBlock);
	}

	@Override
	/** {@inheritDoc} */
	public Type_type getTypetype() {
		return Type_type.TYPE_ASN1_CHOICE;
	}

	@Override
	/** {@inheritDoc} */
	public Type_type getTypetypeTtcn3() {
		return Type_type.TYPE_TTCN3_CHOICE;
	}

	@Override
	/** {@inheritDoc} */
	public int getNofComponents() {
		if (null == components) {
			parseBlockChoice();
		}

		return components.getNofComps();
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

		if (null == info || noStructuredTypeCompatibility) {
			return this == temp;
		}

		switch (temp.getTypetype()) {
		case TYPE_ASN1_CHOICE: {
			final ASN1_Choice_Type temporalType = (ASN1_Choice_Type) temp;
			if (this == temporalType) {
				return true;
			}

			TypeCompatibilityInfo.Chain lChain = leftChain;
			TypeCompatibilityInfo.Chain rChain = rightChain;
			if (null == lChain) {
				lChain = info.getChain();
				lChain.add(this);
			}
			if (null == rChain) {
				rChain = info.getChain();
				rChain.add(temporalType);
			}
			for (int i = 0, size = getNofComponents(); i < size; i++) {
				final CompField cf = getComponentByIndex(i);
				final IType cfType = cf.getType().getTypeRefdLast(timestamp);
				for (int j = 0, size2 = temporalType.getNofComponents(); j < size2; j++) {
					final CompField temporalCompField = temporalType.getComponentByIndex(j);
					final IType tempTypeCompFieldType = temporalCompField.getType().getTypeRefdLast(timestamp);
					if (!cf.getIdentifier().getDisplayName().equals(temporalCompField.getIdentifier().getDisplayName())) {
						continue;
					}
					lChain.markState();
					rChain.markState();
					lChain.add(cfType);
					rChain.add(tempTypeCompFieldType);
					if (cfType.equals(tempTypeCompFieldType) || (lChain.hasRecursion() && rChain.hasRecursion())
							|| cfType.isCompatible(timestamp, tempTypeCompFieldType, info, lChain, rChain)) {
						info.setNeedsConversion(true);
						lChain.previousState();
						rChain.previousState();
						return true;
					}
					lChain.previousState();
					rChain.previousState();
				}
			}
			info.setErrorStr(MessageFormat.format(NOCOMPATIBLEFIELD, temp.getTypename(), getTypename()));
			return false;
		}
		case TYPE_TTCN3_CHOICE: {
			final TTCN3_Choice_Type temporalType = (TTCN3_Choice_Type) temp;
			TypeCompatibilityInfo.Chain lChain = leftChain;
			TypeCompatibilityInfo.Chain rChain = rightChain;
			if (lChain == null) {
				lChain = info.getChain();
				lChain.add(this);
			}
			if (rChain == null) {
				rChain = info.getChain();
				rChain.add(temporalType);
			}
			for (int i = 0, size = getNofComponents(); i < size; i++) {
				final CompField cf = getComponentByIndex(i);
				final IType compFieldType = cf.getType().getTypeRefdLast(timestamp);
				for (int j = 0, size2 = temporalType.getNofComponents(); j < size2; j++) {
					final CompField temporalCompField = temporalType.getComponentByIndex(j);
					final IType tempTypeCompFieldType = temporalCompField.getType().getTypeRefdLast(timestamp);
					if (!cf.getIdentifier().getDisplayName().equals(temporalCompField.getIdentifier().getDisplayName())) {
						continue;
					}
					lChain.markState();
					rChain.markState();
					lChain.add(compFieldType);
					rChain.add(tempTypeCompFieldType);
					if (compFieldType.equals(tempTypeCompFieldType) || (lChain.hasRecursion() && rChain.hasRecursion())
							|| compFieldType.isCompatible(timestamp, tempTypeCompFieldType, info, lChain, rChain)) {
						info.setNeedsConversion(true);
						lChain.previousState();
						rChain.previousState();
						return true;
					}
					lChain.previousState();
					rChain.previousState();
				}
			}
			info.setErrorStr(MessageFormat.format(NOCOMPATIBLEFIELD, temp.getTypename(), getTypename()));
			return false;
		}
		case TYPE_ASN1_SEQUENCE:
		case TYPE_TTCN3_SEQUENCE:
		case TYPE_SEQUENCE_OF:
		case TYPE_ARRAY:
		case TYPE_ASN1_SET:
		case TYPE_TTCN3_SET:
		case TYPE_SET_OF:
		case TYPE_ANYTYPE:
			info.setErrorStr(NOTCOMPATIBLEUNION);
			return false;
		default:
			return false;
		}
	}

	@Override
	/** {@inheritDoc} */
	public String getOutlineIcon() {
		return "asn1_choice.gif";
	}

	@Override
	/** {@inheritDoc} */
	public StringBuilder getProposalDescription(final StringBuilder builder) {
		return builder.append("choice");
	}

	@Override
	/** {@inheritDoc} */
	public void checkRecursions(final CompilationTimeStamp timestamp, final IReferenceChain referenceChain) {
		if (components == null) {
			return;
		}

		if (referenceChain.add(this) && components.getNofComps() == 1) {
			final IType type = components.getCompByIndex(0).getType();
			if (type != null) {
				referenceChain.markState();
				type.checkRecursions(timestamp, referenceChain);
				referenceChain.previousState();
			}
		}
	}

	@Override
	/** {@inheritDoc} */
	public void check(final CompilationTimeStamp timestamp) {
		if (lastTimeChecked != null && !lastTimeChecked.isLess(timestamp)) {
			return;
		}

		lastTimeChecked = timestamp;
		if (components != null && myScope != null) {
			final Module module = myScope.getModuleScope();
			if (module != null) {
				if (module.getSkippedFromSemanticChecking()) {
					return;
				}
			}
		}
		isErroneous = false;

		if (components == null) {
			parseBlockChoice();
		}

		if (isErroneous || components == null) {
			return;
		}

		components.check(timestamp);
		if (components.getNofComps() <= 0 && location != null) {
			location.reportSemanticError(MISSINGALTERNATIVE);
			setIsErroneous(true);
		}

		if (constraints != null) {
			constraints.check(timestamp);
		}

		if (myScope != null) {
			checkEncode(timestamp);
			checkVariants(timestamp);
		}
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

		switch (last.getValuetype()) {
		case SEQUENCE_VALUE:
			if (value.isAsn()) {
				value.getLocation().reportSemanticError(MessageFormat.format(CHOICEEXPECTED, getFullName()));
				value.setIsErroneous(true);
			} else {
				last = last.setValuetype(timestamp, Value_type.CHOICE_VALUE);
				if (!last.getIsErroneous(timestamp)) {
					selfReference = checkThisValueChoice(timestamp, (Choice_Value) last, lhs, valueCheckingOptions.expected_value,
							valueCheckingOptions.incomplete_allowed);
				}
			}
			break;
		case CHOICE_VALUE:
			selfReference = checkThisValueChoice(timestamp, (Choice_Value) last, lhs, valueCheckingOptions.expected_value,
					valueCheckingOptions.incomplete_allowed);
			break;
		case EXPRESSION_VALUE:
		case MACRO_VALUE:
			// already checked
			break;
		default:
			if (value.isAsn()) {
				value.getLocation().reportSemanticError(MessageFormat.format(CHOICEEXPECTED, getFullName()));
				value.setIsErroneous(true);
			} else {
				value.getLocation().reportSemanticError(MessageFormat.format(UNIONEXPECTED, getFullName()));
				value.setIsErroneous(true);
			}
			value.setIsErroneous(true);
		}

		value.setLastTimeChecked(timestamp);

		return selfReference;
	}

	private boolean checkThisValueChoice(final CompilationTimeStamp timestamp, final Choice_Value value, final Assignment lhs, final Expected_Value_type expectedValue,
			final boolean incompleteAllowed) {
		boolean selfReference = false;
		final Identifier name = value.getName();
		if (!hasComponentWithName(name)) {
			if (value.isAsn()) {
				value.getLocation()
				.reportSemanticError(MessageFormat.format(NONEXISTENTCHOICE, name.getDisplayName(), getFullName()));
				value.setIsErroneous(true);
			} else {
				value.getLocation().reportSemanticError(MessageFormat.format(NONEXISTENTUNION, name.getDisplayName(), getFullName()));
				value.setIsErroneous(true);
			}

			value.setLastTimeChecked(timestamp);
			return selfReference;
		}

		IValue alternativeValue = value.getValue();
		if (alternativeValue == null) {
			return selfReference;
		}

		final Type alternativeType = getComponentByName(name).getType();
		alternativeValue.setMyGovernor(alternativeType);
		alternativeValue = alternativeType.checkThisValueRef(timestamp, alternativeValue);
		selfReference = alternativeType.checkThisValue(timestamp, alternativeValue, lhs, new ValueCheckingOptions(expectedValue, incompleteAllowed, false, true,
				false, false));

		value.setLastTimeChecked(timestamp);

		return selfReference;
	}

	@Override
	/** {@inheritDoc} */
	public boolean checkThisTemplate(final CompilationTimeStamp timestamp, final ITTCN3Template template, final boolean isModified,
			final boolean implicitOmit, final Assignment lhs) {
		registerUsage(template);
		template.setMyGovernor(this);

		if (getIsErroneous(timestamp)) {
			return false;
		}

		boolean selfReference = false;
		if (Template_type.NAMED_TEMPLATE_LIST.equals(template.getTemplatetype())) {
			final Named_Template_List namedTemplateList = (Named_Template_List) template;
			final int nofTemplates = namedTemplateList.getNofTemplates();
			if (nofTemplates != 1) {
				template.getLocation().reportSemanticError(ONEFIELDEXPECTED);
				template.setIsErroneous(true);
			}

			for (int i = 0; i < nofTemplates; i++) {
				final NamedTemplate namedTemplate = namedTemplateList.getTemplateByIndex(i);
				final Identifier name = namedTemplate.getName();

				final CompField field = components.getCompByName(name);
				if (field == null) {
					namedTemplate.getLocation().reportSemanticError(
							MessageFormat.format(REFERENCETONONEXISTENTFIELD, name.getDisplayName(), getFullName()));
				} else {
					final Type fieldType = field.getType();
					if (fieldType != null && !fieldType.getIsErroneous(timestamp)) {
						ITTCN3Template namedTemplateTemplate = namedTemplate.getTemplate();

						namedTemplateTemplate.setMyGovernor(fieldType);
						namedTemplateTemplate = fieldType.checkThisTemplateRef(timestamp, namedTemplateTemplate);
						final Completeness_type completeness = namedTemplateList.getCompletenessConditionChoice(timestamp,
								isModified, name);
						selfReference |= namedTemplateTemplate.checkThisTemplateGeneric(timestamp, fieldType,
								Completeness_type.MAY_INCOMPLETE.equals(completeness), false, false, true,
								implicitOmit, lhs);
					}
				}
			}
		} else {
			template.getLocation().reportSemanticError(
					MessageFormat.format(TEMPLATENOTALLOWED, template.getTemplateTypeName(), getTypename()));
			template.setIsErroneous(true);
		}

		if (template.getLengthRestriction() != null) {
			template.getLocation().reportSemanticError(MessageFormat.format(LENGTHRESTRICTIONNOTALLOWED, getTypename()));
			template.setIsErroneous(true);
		}

		return selfReference;
	}

	/** Parses the block as if it were the block of a choice. */
	public void parseBlockChoice() {
		if (null == mBlock) {
			return;
		}

		final Asn1Parser parser = BlockLevelTokenStreamTracker.getASN1ParserForBlock(mBlock);
		if (null == parser) {
			return;
		}

		components = parser.pr_special_AlternativeTypeLists().list;
		final List<SyntacticErrorStorage> errors = parser.getErrorStorage();
		if (null != errors && !errors.isEmpty()) {
			isErroneous = true;
			components = null;
			for (int i = 0; i < errors.size(); i++) {
				ParserMarkerSupport.createOnTheFlyMixedMarker((IFile) mBlock.getLocation().getFile(), errors.get(i),
						IMarker.SEVERITY_ERROR);
			}
		}

		if (components == null) {
			isErroneous = true;
			return;
		}

		components.setFullNameParent(this);
		components.setMyScope(getMyScope());
		components.setMyType(this);
	}

	@Override
	/** {@inheritDoc} */
	public void checkCodingAttributes(final CompilationTimeStamp timestamp, final IReferenceChain refChain) {
		checkJson(timestamp);
		//TODO add checks for other encodings.

		if (refChain.contains(this)) {
			return;
		}

		refChain.add(this);
		refChain.markState();
		for (int i = 0; i < getNofComponents(); i++) {
			final CompField cf = getComponentByIndex(i);

			cf.getType().checkCodingAttributes(timestamp, refChain);
		}
		refChain.previousState();
	}

	@Override
	/** {@inheritDoc} */
	public void forceJson(final CompilationTimeStamp timestamp) {
		if (jsonAttribute == null) {
			jsonAttribute = new JsonAST();
		}
	}

	@Override
	public void checkJson(final CompilationTimeStamp timestamp) {
		if (jsonAttribute == null && !hasEncodeAttribute("JSON")) {
			return;
		}

		for (int i = 0; i < getNofComponents(); i++) {
			final Type fieldType = getComponentByIndex(i).getType();
			fieldType.forceJson(timestamp);
		}

		if (jsonAttribute == null) {
			return;
		}

		if (jsonAttribute.omit_as_null && !isOptionalField()) {
			getLocation().reportSemanticError("Invalid attribute, 'omit as null' requires optional field of a record or set.");
		}

		if (jsonAttribute.as_value) {
			getLocation().reportSemanticError("Invalid attribute, 'as value' is only allowed for unions, the anytype, or records or sets with one field");
		}

		if (jsonAttribute.alias != null) {
			final IType parent = getParentType();
			if (parent == null) {
				// only report this error when using the new codec handling, otherwise
				// ignore the attribute (since it can also be set by the XML 'name as ...' attribute)
				getLocation().reportSemanticError("Invalid attribute, 'name as ...' requires field of a record, set or union.");
			} else {
				switch (parent.getTypetype()) {
				case TYPE_TTCN3_SEQUENCE:
				case TYPE_TTCN3_SET:
				case TYPE_TTCN3_CHOICE:
					break;
				default:
					// only report this error when using the new codec handling, otherwise
					// ignore the attribute (since it can also be set by the XML 'name as ...' attribute)
					getLocation().reportSemanticError("Invalid attribute, 'name as ...' requires field of a record, set or union.");
					break;
				}
			}

			if (parent != null && parent.getJsonAttribute() != null && parent.getJsonAttribute().as_value) {
				switch (parent.getTypetype()) {
				case TYPE_TTCN3_CHOICE:
				case TYPE_ANYTYPE:
					// parent_type_name remains null if the 'as value' attribute is set for an invalid type
					getLocation().reportSemanticWarning(MessageFormat.format("Attribute 'name as ...' will be ignored, because parent {0} is encoded without field names.", parent.getTypename()));
					break;
				case TYPE_TTCN3_SEQUENCE:
				case TYPE_TTCN3_SET:
					if (((TTCN3_Set_Seq_Choice_BaseType)parent).getNofComponents() == 1) {
						// parent_type_name remains null if the 'as value' attribute is set for an invalid type
						getLocation().reportSemanticWarning(MessageFormat.format("Attribute 'name as ...' will be ignored, because parent {0} is encoded without field names.", parent.getTypename()));
					}
					break;
				default:
					break;
				}
			}
		}

		if (jsonAttribute.default_value != null) {
			checkJsonDefault();
		}

		//TODO: check schema extensions 

		if (jsonAttribute.metainfo_unbound) {
			if (getParentType() == null || (getParentType().getTypetype() != Type_type.TYPE_TTCN3_SEQUENCE &&
					getParentType().getTypetype() != Type_type.TYPE_TTCN3_SET)) {
				// only allowed if it's an array type or a field of a record/set
				getLocation().reportSemanticError("Invalid attribute 'metainfo for unbound', requires record, set, record of, set of, array or field of a record or set");
			}
		}

		if (jsonAttribute.as_number) {
			getLocation().reportSemanticError("Invalid attribute, 'as number' is only allowed for enumerated types");
		}

		//FIXME: check tag_list

		if (jsonAttribute.as_map) {
			getLocation().reportSemanticError("Invalid attribute, 'as map' requires record of or set of");
		}

		if (jsonAttribute.enum_texts.size() > 0) {
			getLocation().reportSemanticError("Invalid attribute, 'text ... as ...' requires an enumerated type");
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
			final Identifier id = subreference.getId();
			final CompField compField = components.getCompByName(id);
			if (compField == null) {
				subreference.getLocation().reportSemanticError(
						MessageFormat.format(FieldSubReference.NONEXISTENTSUBREFERENCE, ((FieldSubReference) subreference)
								.getId().getDisplayName(), getTypename()));
				return null;
			}

			final Expected_Value_type internalExpectation = (expectedIndex == Expected_Value_type.EXPECTED_TEMPLATE) ? Expected_Value_type.EXPECTED_DYNAMIC_VALUE
					: expectedIndex;

			return compField.getType().getFieldType(timestamp, reference, actualSubReference + 1, internalExpectation, refChain,
					interruptIfOptional);
		case parameterisedSubReference:
			subreference.getLocation().reportSemanticError(
					MessageFormat.format(FieldSubReference.INVALIDSUBREFERENCE, ((ParameterisedSubReference) subreference)
							.getId().getDisplayName(), getTypename()));
			return null;
		default:
			subreference.getLocation().reportSemanticError(ISubReference.INVALIDSUBREFERENCE);
			return null;
		}
	}

	@Override
	/** {@inheritDoc} */
	public String getGenNameValue(final JavaGenData aData, final StringBuilder source) {
		return getGenNameOwn(aData);
	}

	@Override
	/** {@inheritDoc} */
	public String getGenNameTemplate(final JavaGenData aData, final StringBuilder source) {
		return getGenNameOwn(aData).concat("_template");
	}

	@Override
	/** {@inheritDoc} */
	public String getGenNameTypeDescriptor(final JavaGenData aData, final StringBuilder source) {
		final String baseName = getGenNameTypeName(aData, source);
		return baseName + "." + getGenNameOwn();
	}

	@Override
	/** {@inheritDoc} */
	public String getGenNameRawDescriptor(final JavaGenData aData, final StringBuilder source) {
		return getGenNameOwn(aData) + "." + getGenNameOwn() + "_raw_";
	}

	@Override
	/** {@inheritDoc} */
	public String getGenNameJsonDescriptor(final JavaGenData aData, final StringBuilder source) {
		return getGenNameOwn(aData) + "." + getGenNameOwn() + "_json_";
	}

	@Override
	/** {@inheritDoc} */
	public boolean generatesOwnClass(final JavaGenData aData, final StringBuilder source) {
		return true;
	}

	@Override
	/** {@inheritDoc} */
	public void generateCode( final JavaGenData aData, final StringBuilder source ) {
		if (lastTimeGenerated != null && !lastTimeGenerated.isLess(aData.getBuildTimstamp())) {
			return;
		}

		lastTimeGenerated = aData.getBuildTimstamp();

		if (components == null) {
			return;
		}

		final String genName = getGenNameOwn();
		final String displayName = getFullName();

		final StringBuilder localTypeDescriptor = new StringBuilder();
		final HashMap<String, String> attributeRegistry = new HashMap<String, String>();
		generateCodeTypedescriptor(aData, source, localTypeDescriptor, attributeRegistry);
		generateCodeDefaultCoding(aData, source, localTypeDescriptor);
		final StringBuilder localCodingHandler = new StringBuilder();
		generateCodeForCodingHandlers(aData, source, localCodingHandler);

		final boolean hasJson = getGenerateCoderFunctions(MessageEncoding_type.JSON);
		final List<FieldInfo> fieldInfos =  new ArrayList<FieldInfo>();
		boolean hasOptional = false;
		for ( int i = 0 ; i < components.getNofComps(); i++ ) {
			final CompField compField = components.getCompByIndex(i);
			final IType cfType = compField.getType();

			switch (cfType.getTypetype()) {
			case TYPE_ASN1_CHOICE:
			case TYPE_ASN1_ENUMERATED:
			case TYPE_ASN1_SEQUENCE:
			case TYPE_ASN1_SET:
			case TYPE_SEQUENCE_OF:
			case TYPE_SET_OF:
				if (!cfType.generatesOwnClass(aData, source)) {
					cfType.generateCodeTypedescriptor(aData, source, localTypeDescriptor, attributeRegistry);
					cfType.generateCodeDefaultCoding(aData, source, localTypeDescriptor);
					cfType.generateCodeForCodingHandlers(aData, source, localCodingHandler);
				}
				break;
			default:
				cfType.generateCodeTypedescriptor(aData, source, localTypeDescriptor, attributeRegistry);
				cfType.generateCodeDefaultCoding(aData, source, localTypeDescriptor);
				cfType.generateCodeForCodingHandlers(aData, source, localCodingHandler);
				break;
			}

			final String jsonAlias = cfType.getJsonAttribute() != null ? cfType.getJsonAttribute().alias : null;
			final int JsonValueType = hasJson ? cfType.getJsonValueType() : 0;
			final FieldInfo fi = new FieldInfo(cfType.getGenNameValue( aData, source ),
					cfType.getGenNameTemplate(aData, source),
					compField.getIdentifier().getName(), compField.getIdentifier().getDisplayName(),
					cfType.getGenNameTypeDescriptor(aData, source), jsonAlias, JsonValueType);
			hasOptional |= compField.isOptional();
			fieldInfos.add( fi );
		}

		for ( int i = 0 ; i < components.getNofComps(); i++ ) {
			final CompField compField = components.getCompByIndex(i);
			final StringBuilder tempSource = aData.getCodeForType(compField.getType().getGenNameOwn());
			compField.getType().generateCode(aData, tempSource);
		}

		final boolean jsonAsValue = jsonAttribute != null ? jsonAttribute.as_value : false;
		final boolean hasRaw = getGenerateCoderFunctions(MessageEncoding_type.RAW);
		UnionGenerator.generateValueClass(aData, source, genName, displayName, fieldInfos, hasOptional, hasRaw, null, hasJson, false, jsonAsValue, localTypeDescriptor, localCodingHandler);
		UnionGenerator.generateTemplateClass(aData, source, genName, displayName, fieldInfos, hasOptional);
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

		return false;
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
		if (!(subReference instanceof FieldSubReference)) {
			ErrorReporter.INTERNAL_ERROR("Code generator reached erroneous type reference `" + getFullName() + "''");
			expression.expression.append("FATAL_ERROR encountered while processing `" + getFullName() + "''\n");
			return;
		}

		final String valueTypeGenName = getGenNameValue(aData, expression.expression);
		final String currentTypeGenName = isTemplate ? getGenNameTemplate(aData, expression.expression) : valueTypeGenName;
		final Identifier fieldId = ((FieldSubReference) subReference).getId();
		expression.expression.append(MessageFormat.format("if({0}) '{'\n", globalId));
		expression.expression.append(MessageFormat.format("{0} = {1}.ischosen({2}.union_selection_type.ALT_{3});\n", globalId, externalId, valueTypeGenName, FieldSubReference.getJavaGetterName( fieldId.getName())));
		expression.expression.append("}\n");

		final CompField compField = getComponentByName(fieldId);
		final Type nextType = compField.getType();
		final String nextTypeGenName = isTemplate ? nextType.getGenNameTemplate(aData, expression.expression) : nextType.getGenNameValue(aData, expression.expression);

		expression.expression.append(MessageFormat.format("if({0}) '{'\n", globalId));
		closingBrackets.insert(0, "}\n");

		final String temporalId = aData.getTemporaryVariableName();
		final String temporalId2 = aData.getTemporaryVariableName();
		expression.expression.append(MessageFormat.format("final {0} {1} = new {0}({2});\n", currentTypeGenName, temporalId, externalId));
		expression.expression.append(MessageFormat.format("final {0} {1} = {2}.get_field_{3}();\n", nextTypeGenName, temporalId2, temporalId, FieldSubReference.getJavaGetterName( fieldId.getName())));

		if (optype == Operation_type.ISBOUND_OPERATION) {
			expression.expression.append(MessageFormat.format("{0} = {1}.is_bound();\n", globalId, temporalId2));
		} else if (optype == Operation_type.ISVALUE_OPERATION) {
			expression.expression.append(MessageFormat.format("{0} = {1}.is_value();\n", globalId, temporalId2));
		} else if (optype == Operation_type.ISPRESENT_OPERATION) {
			expression.expression.append(MessageFormat.format("{0} = {1}.is_present({2});\n", globalId, temporalId2, isTemplate && aData.getAllowOmitInValueList()?"true":""));
		} else if (optype == Operation_type.ISCHOOSEN_OPERATION) {
			expression.expression.append(MessageFormat.format("{0} = {1}.is_bound();\n", globalId, temporalId2));
			if (subReferenceIndex==subreferences.size()-1) {
				expression.expression.append(MessageFormat.format("if ({0}) '{'\n", globalId));
				expression.expression.append(MessageFormat.format("{0} = {1}.ischosen({2});\n", globalId, temporalId2, field));
				expression.expression.append("}\n");
			}
		}

		nextType.generateCodeIsPresentBoundChosen(aData, expression, subreferences, subReferenceIndex + 1, globalId, temporalId2, isTemplate, optype, field, targetScope);

		expression.expression.append(closingBrackets);
	}

	@Override
	public String generateConversion(final JavaGenData aData, final IType fromType, final String fromName, final boolean forValue, final ExpressionStruct expression) {
		final IType refdType = fromType.getTypeRefdLast(CompilationTimeStamp.getBaseTimestamp());
		if (refdType == null || this == refdType) {
			//no need to convert
			return fromName;
		}

		switch (refdType.getTypetype()) {
		case TYPE_TTCN3_CHOICE:{
			//heavy conversion is needed
			final TTCN3_Choice_Type realFromType = (TTCN3_Choice_Type) refdType;
			return generateConversionTTCNChoiceToASNChoice(aData, realFromType, fromName, forValue, expression);
		}
		case TYPE_ASN1_CHOICE:{
			//heavy conversion is needed
			final ASN1_Choice_Type realFromType = (ASN1_Choice_Type) refdType;
			return generateConversionASNChoiceToASNChoice(aData, realFromType, fromName, forValue, expression);
		}
		default:
			expression.expression.append(MessageFormat.format("//FIXME conversion from {0} to {1} is not needed or nor supported yet\n", fromType.getTypename(), getTypename()));
			break;
		}

		// the default implementation does nothing
		return fromName;
	}

	protected String generateConversionTTCNChoiceToASNChoice(final JavaGenData aData, final TTCN3_Choice_Type fromType, final String fromName, final boolean forValue, final ExpressionStruct expression) {
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
			conversionFunctionBody.append("\t\tif(!from.is_bound()) {\n");
			conversionFunctionBody.append("\t\t\treturn false;\n");
			conversionFunctionBody.append("\t\t}\n");
			conversionFunctionBody.append("\t\tswitch (from.get_selection()) {\n");

			for (int i = 0; i < fromType.getNofComponents(); i++) {
				final CompField fromComp = fromType.getComponentByIndex(i);
				final Identifier fromFieldName = fromComp.getIdentifier();
				final IType fromFieldType = fromComp.getType().getTypeRefdLast(CompilationTimeStamp.getBaseTimestamp());
				for (int j = 0; j < getNofComponents(); j++) {
					final CompField toComp = getComponentByIndex(j);
					final Identifier toFieldName = toComp.getIdentifier();
					final IType toFieldType = toComp.getType().getTypeRefdLast(CompilationTimeStamp.getBaseTimestamp());
					if (fromFieldName.equals(toFieldName) && fromFieldType.isCompatible(CompilationTimeStamp.getBaseTimestamp(), toFieldType, null, null, null)) {
						conversionFunctionBody.append(MessageFormat.format("\t\tcase ALT_{0}: '{'\n", fromFieldName.getName()));
						final String tempId2 = aData.getTemporaryVariableName();
						final String fromFieldTypeName = forValue ? fromFieldType.getGenNameValue(aData, conversionFunctionBody): fromFieldType.getGenNameTemplate(aData, conversionFunctionBody);
						conversionFunctionBody.append(MessageFormat.format("\t\t\tfinal {0} {1} = from.constGet_field_{2}();\n", fromFieldTypeName, tempId2, fromFieldName.getName()));
						conversionFunctionBody.append(MessageFormat.format("\t\t\tif ({0}.is_bound()) '{'\n", tempId2));

						final ExpressionStruct tempExpression = new ExpressionStruct();
						final String tempId3 = toFieldType.generateConversion(aData, fromFieldType, tempId2, forValue, tempExpression);
						tempExpression.openMergeExpression(conversionFunctionBody);

						conversionFunctionBody.append(MessageFormat.format("\t\t\t\tto.get_field_{0}().operator_assign({1});\n", toFieldName.getName() , tempId3));

						conversionFunctionBody.append("\t\t\t}\n");
						conversionFunctionBody.append("\t\t\tbreak;\n");
						conversionFunctionBody.append("\t\t}\n");
					}
				}
			}

			conversionFunctionBody.append("\t\tdefault:\n");
			conversionFunctionBody.append("\t\t\treturn false;\n");
			conversionFunctionBody.append("\t\t}\n");
			conversionFunctionBody.append("\t\treturn true;\n");
			conversionFunctionBody.append("\t}\n\n");

			aData.addTypeConversion(ConversionFunctionName, conversionFunctionBody.toString());
		}

		return tempId;
	}

	protected String generateConversionASNChoiceToASNChoice(final JavaGenData aData, final ASN1_Choice_Type fromType, final String fromName, final boolean forValue, final ExpressionStruct expression) {
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
			conversionFunctionBody.append("\t\tif(!from.is_bound()) {\n");
			conversionFunctionBody.append("\t\t\treturn false;\n");
			conversionFunctionBody.append("\t\t}\n");
			conversionFunctionBody.append("\t\tswitch (from.get_selection()) {\n");

			for (int i = 0; i < fromType.getNofComponents(); i++) {
				final CompField fromComp = fromType.getComponentByIndex(i);
				final Identifier fromFieldName = fromComp.getIdentifier();
				final IType fromFieldType = fromComp.getType().getTypeRefdLast(CompilationTimeStamp.getBaseTimestamp());
				for (int j = 0; j < getNofComponents(); j++) {
					final CompField toComp = getComponentByIndex(j);
					final Identifier toFieldName = toComp.getIdentifier();
					final IType toFieldType = toComp.getType().getTypeRefdLast(CompilationTimeStamp.getBaseTimestamp());
					if (fromFieldName.equals(toFieldName) && fromFieldType.isCompatible(CompilationTimeStamp.getBaseTimestamp(), toFieldType, null, null, null)) {
						conversionFunctionBody.append(MessageFormat.format("\t\tcase ALT_{0}: '{'\n", fromFieldName.getName()));
						final String tempId2 = aData.getTemporaryVariableName();
						final String fromFieldTypeName = forValue ? fromFieldType.getGenNameValue(aData, conversionFunctionBody): fromFieldType.getGenNameTemplate(aData, conversionFunctionBody);
						conversionFunctionBody.append(MessageFormat.format("\t\t\tfinal {0} {1} = from.constGet_field_{2}();\n", fromFieldTypeName, tempId2, fromFieldName.getName()));
						conversionFunctionBody.append(MessageFormat.format("\t\t\tif ({0}.is_bound()) '{'\n", tempId2));

						final ExpressionStruct tempExpression = new ExpressionStruct();
						final String tempId3 = toFieldType.generateConversion(aData, fromFieldType, tempId2, forValue, tempExpression);
						tempExpression.openMergeExpression(conversionFunctionBody);

						conversionFunctionBody.append(MessageFormat.format("\t\t\t\tto.get_field_{0}().operator_assign({1});\n", toFieldName.getName() , tempId3));

						conversionFunctionBody.append("\t\t\t}\n");
						conversionFunctionBody.append("\t\t\tbreak;\n");
						conversionFunctionBody.append("\t\t}\n");
					}
				}
			}

			conversionFunctionBody.append("\t\tdefault:\n");
			conversionFunctionBody.append("\t\t\treturn false;\n");
			conversionFunctionBody.append("\t\t}\n");
			conversionFunctionBody.append("\t\treturn true;\n");
			conversionFunctionBody.append("\t}\n\n");

			aData.addTypeConversion(ConversionFunctionName, conversionFunctionBody.toString());
		}

		return tempId;
	}
}
