/******************************************************************************
 * Copyright (c) 2000-2019 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.types;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.CachedReferenceChain;
import org.eclipse.titan.designer.AST.FieldSubReference;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.ISubReference;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.IValue.Value_type;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.ReferenceChain;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.Type;
import org.eclipse.titan.designer.AST.TypeCompatibilityInfo;
import org.eclipse.titan.designer.AST.Value;
import org.eclipse.titan.designer.AST.ASN1.types.ASN1_Choice_Type;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.attributes.RawAST;
import org.eclipse.titan.designer.AST.TTCN3.attributes.RawAST.rawAST_single_tag;
import org.eclipse.titan.designer.AST.TTCN3.attributes.RawAST.rawAST_tag_field_value;
import org.eclipse.titan.designer.AST.TTCN3.attributes.RawASTStruct;
import org.eclipse.titan.designer.AST.TTCN3.attributes.RawASTStruct.rawAST_coding_field_type;
import org.eclipse.titan.designer.AST.TTCN3.attributes.RawASTStruct.rawAST_coding_fields;
import org.eclipse.titan.designer.AST.TTCN3.attributes.RawASTStruct.rawAST_coding_taglist;
import org.eclipse.titan.designer.AST.TTCN3.templates.ITTCN3Template;
import org.eclipse.titan.designer.AST.TTCN3.templates.ITTCN3Template.Completeness_type;
import org.eclipse.titan.designer.AST.TTCN3.templates.ITTCN3Template.Template_type;
import org.eclipse.titan.designer.AST.TTCN3.templates.NamedTemplate;
import org.eclipse.titan.designer.AST.TTCN3.templates.Named_Template_List;
import org.eclipse.titan.designer.AST.TTCN3.types.UnionGenerator.FieldInfo;
import org.eclipse.titan.designer.AST.TTCN3.types.subtypes.SubType;
import org.eclipse.titan.designer.AST.TTCN3.values.Choice_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Expression_Value.Operation_type;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.ExpressionStruct;
import org.eclipse.titan.designer.compiler.BuildTimestamp;
import org.eclipse.titan.designer.compiler.JavaGenData;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * @author Kristof Szabados
 * */
public final class TTCN3_Choice_Type extends TTCN3_Set_Seq_Choice_BaseType {
	private static final String UNSUPPERTED_FIELDNAME =
			"Sorry, but it is not supported for sequence types to have a field with a name (`{0}'') which exactly matches the name of the type definition.";
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

	public TTCN3_Choice_Type(final CompFieldMap compFieldMap) {
		super(compFieldMap);
	}

	@Override
	/** {@inheritDoc} */
	public Type_type getTypetype() {
		return Type_type.TYPE_TTCN3_CHOICE;
	}

	@Override
	/** {@inheritDoc} */
	public SubType.SubType_type getSubtypeType() {
		return SubType.SubType_type.ST_UNION;
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
		case TYPE_ASN1_CHOICE: {
			final ASN1_Choice_Type tempType = (ASN1_Choice_Type) temp;
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
			for (int i = 0, size = getNofComponents(); i < size; i++) {
				final CompField cf = getComponentByIndex(i);
				final IType cfType = cf.getType().getTypeRefdLast(timestamp);
				for (int j = 0, size2 = tempType.getNofComponents(timestamp); j < size2; j++) {
					final CompField tempComponentField = tempType.getComponentByIndex(j);
					final IType tempTypeCompFieldType = tempComponentField.getType().getTypeRefdLast(timestamp);
					if (!cf.getIdentifier().getDisplayName().equals(tempComponentField.getIdentifier().getDisplayName())) {
						continue;
					}
					lChain.markState();
					rChain.markState();
					lChain.add(cfType);
					rChain.add(tempTypeCompFieldType);
					if (cfType.equals(tempTypeCompFieldType)
							|| (lChain.hasRecursion() && rChain.hasRecursion())
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
			final TTCN3_Choice_Type tempType = (TTCN3_Choice_Type) temp;
			if (this == tempType) {
				return true;
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
			for (int i = 0, size = getNofComponents(); i < size; i++) {
				final CompField cf = getComponentByIndex(i);
				final IType cfType = cf.getType().getTypeRefdLast(timestamp);
				for (int j = 0, size2 = tempType.getNofComponents(); j < size2; j++) {
					final CompField tempComponentField = tempType.getComponentByIndex(j);
					final IType tempTypeCompFieldType = tempComponentField.getType().getTypeRefdLast(timestamp);
					if (!cf.getIdentifier().getDisplayName().equals(tempComponentField.getIdentifier().getDisplayName())) {
						continue;
					}
					lChain.markState();
					rChain.markState();
					lChain.add(cfType);
					rChain.add(tempTypeCompFieldType);
					if (cfType.equals(tempTypeCompFieldType)
							|| (lChain.hasRecursion() && rChain.hasRecursion())
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
	public boolean isIdentical(final CompilationTimeStamp timestamp, final IType type) {
		check(timestamp);
		type.check(timestamp);
		final IType temp = type.getTypeRefdLast(timestamp);

		if (getIsErroneous(timestamp) || temp.getIsErroneous(timestamp)) {
			return true;
		}

		return this == temp;
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
		return getFullName();
	}

	@Override
	/** {@inheritDoc} */
	public String getOutlineIcon() {
		return "asn1_choice.gif";
	}

	@Override
	/** {@inheritDoc} */
	public void checkConstructorName(final String definitionName) {
		if (hasComponentWithName(definitionName)) {
			final CompField field = getComponentByName(definitionName);
			field.getIdentifier().getLocation().reportSemanticError(MessageFormat.format(UNSUPPERTED_FIELDNAME, field.getIdentifier().getDisplayName()));
		}
	}

	@Override
	/** {@inheritDoc} */
	public void checkRecursions(final CompilationTimeStamp timestamp, final IReferenceChain referenceChain) {
		if (!referenceChain.add(this) || compFieldMap.isEmpty()) {
			return;
		}

		// FIXME there should be a better way than checking for all possible IReferenceChain implementation
		CachedReferenceChain cachedChain;
		if (referenceChain instanceof CachedReferenceChain) {
			cachedChain = (CachedReferenceChain) referenceChain;
		} else {
			if (!(referenceChain instanceof ReferenceChain)) {
				return;
			}
			cachedChain = ((ReferenceChain) referenceChain).toCachedReferenceChain();
		}

		final Map<String, CompField> map = compFieldMap.getComponentFieldMap(timestamp);
		cachedChain.markErrorState();
		int i = 1;
		for (final CompField compField : map.values()) {
			final IType type = compField.getType();
			if (type != null) {
				cachedChain.markState();
				type.checkRecursions(timestamp, cachedChain);
				cachedChain.previousState();
			}

			if (cachedChain.getNofErrors() < i) {
				break;
			}
			++i;
		}

		if (cachedChain.getNofErrors() == map.size()) {
			cachedChain.reportAllTheErrors();
		}
		cachedChain.prevErrorState();
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
							valueCheckingOptions.incomplete_allowed, valueCheckingOptions.implicit_omit, valueCheckingOptions.str_elem);
				}
			}
			break;
		case CHOICE_VALUE:
			selfReference = checkThisValueChoice(timestamp, (Choice_Value) last, lhs, valueCheckingOptions.expected_value,
					valueCheckingOptions.incomplete_allowed, valueCheckingOptions.implicit_omit, valueCheckingOptions.str_elem);
			break;
		case EXPRESSION_VALUE:
		case MACRO_VALUE:
			// already checked
			break;
		default:
			if (value.isAsn()) {
				value.getLocation().reportSemanticError(MessageFormat.format(CHOICEEXPECTED, getFullName()));
			} else {
				value.getLocation().reportSemanticError(MessageFormat.format(UNIONEXPECTED, getFullName()));
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

	private boolean checkThisValueChoice(final CompilationTimeStamp timestamp, final Choice_Value value, final Assignment lhs, final Expected_Value_type expectedValue,
			final boolean incompleteAllowed, final boolean implicit_omit, final boolean strElem) {
		boolean selfReference = false;
		final Identifier name = value.getName();
		if (!hasComponentWithName(name.getName())) {
			if (value.isAsn()) {
				value.getLocation().reportSemanticError(MessageFormat.format(NONEXISTENTCHOICE, name.getDisplayName(), getFullName()));
				value.setIsErroneous(true);
			} else {
				value.getLocation().reportSemanticError(MessageFormat.format(NONEXISTENTUNION, name.getDisplayName(), getFullName()));
				value.setIsErroneous(true);
			}
			return selfReference;
		}

		IValue alternativeValue = value.getValue();
		if (alternativeValue == null) {
			return selfReference;
		}

		final Type alternativeType = getComponentByName(name.getName()).getType();
		alternativeValue.setMyGovernor(alternativeType);
		alternativeValue = alternativeType.checkThisValueRef(timestamp, alternativeValue);
		selfReference = alternativeType.checkThisValue(timestamp, alternativeValue, lhs, new ValueCheckingOptions(expectedValue,
				incompleteAllowed, false, true, implicit_omit, strElem));

		value.setLastTimeChecked(timestamp);

		return selfReference;
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
		if (Template_type.NAMED_TEMPLATE_LIST.equals(template.getTemplatetype())) {
			final Named_Template_List namedTemplateList = (Named_Template_List) template;
			final int nofTemplates = namedTemplateList.getNofTemplates();
			if (nofTemplates != 1) {
				template.getLocation().reportSemanticError(ONEFIELDEXPECTED);
			}

			for (int i = 0; i < nofTemplates; i++) {
				final NamedTemplate namedTemplate = namedTemplateList.getTemplateByIndex(i);
				final Identifier name = namedTemplate.getName();

				final CompField field = compFieldMap.getCompWithName(name);
				if (field == null) {
					namedTemplate.getLocation().reportSemanticError(MessageFormat.format(REFERENCETONONEXISTENTFIELD, name.getDisplayName(), getFullName()));
				} else {
					final Type fieldType = field.getType();
					ITTCN3Template namedTemplateTemplate = namedTemplate.getTemplate();

					namedTemplateTemplate.setMyGovernor(fieldType);
					namedTemplateTemplate = fieldType.checkThisTemplateRef(timestamp, namedTemplateTemplate);
					final Completeness_type completeness = namedTemplateList.getCompletenessConditionChoice(timestamp, isModified, name);
					selfReference |= namedTemplateTemplate.checkThisTemplateGeneric(
							timestamp, fieldType, Completeness_type.MAY_INCOMPLETE.equals(completeness), false, false, true, implicitOmit, lhs);
				}
			}
		} else {
			template.getLocation().reportSemanticError(MessageFormat.format(TEMPLATENOTALLOWED, template.getTemplateTypeName(), getTypename()));
		}

		if (template.getLengthRestriction() != null) {
			template.getLocation().reportSemanticError(MessageFormat.format(LENGTHRESTRICTIONNOTALLOWED, getTypename()));
		}

		return selfReference;
	}

	@Override
	/** {@inheritDoc} */
	public void checkCodingAttributes(final CompilationTimeStamp timestamp, final IReferenceChain refChain) {
		//check raw attributes
		//TODO can unions have length restriction?
		if (rawAttribute != null) {
			//TODO force_raw()
			if (rawAttribute.taglist != null) {
				for (int c = 0; c < rawAttribute.taglist.size(); c++) {
					final rawAST_single_tag singleTag = rawAttribute.taglist.get(c);
					final Identifier fieldname = singleTag.fieldName;
					if (!hasComponentWithName(fieldname.getName())) {
						fieldname.getLocation().reportSemanticError(MessageFormat.format("Invalid field name `{0}'' in RAW parameter TAG for type `{1}''", fieldname.getDisplayName(), getTypename()));
						continue;
					}
	
					if (singleTag.keyList != null) {
						for (int a = 0; a < singleTag.keyList.size(); a++) {
							final Reference reference = new Reference(null);
							reference.addSubReference(new FieldSubReference(fieldname));
							for (int b = 0; b < singleTag.keyList.get(a).keyField.names.size(); b++) {
								reference.addSubReference(new FieldSubReference(singleTag.keyList.get(a).keyField.names.get(b)));
							}

							final IType t = getFieldType(timestamp, reference, 0, Expected_Value_type.EXPECTED_DYNAMIC_VALUE, false);
							if (t != null) {
								final Value v = singleTag.keyList.get(a).v_value;
								if (v != null) {
									v.setMyScope(getMyScope());
									v.setMyGovernor(t);
									final IValue tempValue = t.checkThisValueRef(timestamp, v);
									t.checkThisValue(timestamp, tempValue, null, new ValueCheckingOptions(Expected_Value_type.EXPECTED_CONSTANT, false, false, false, false, false));
								}
							}
						}
					}
				}
			}
		}
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
	public void forceRaw(final CompilationTimeStamp timestamp) {
		if (rawAttribute == null) {
			rawAttribute = new RawAST(getDefaultRawFieldLength());
		}
	}

	@Override
	/** {@inheritDoc} */
	public int getRawLength(final BuildTimestamp timestamp) {
		if (rawLengthCalculated != null && !rawLengthCalculated.isLess(timestamp)) {
			return rawLength;
		}

		rawLengthCalculated = timestamp;
		rawLength = 0;
		for (int i = 0; i < getNofComponents(); i++) {
			final CompField cf = getComponentByIndex(i);

			final Type t = cf.getType();
			final int l = t.getTypeRefdLast(CompilationTimeStamp.getBaseTimestamp()).getRawLength(timestamp);
			if (l == -1) {
				rawLength = -1;
				return rawLength;
			}

			if (i == 0) {
				rawLength = l;
			} else {
				if (rawLength != l) {
					rawLength = -1;
					return rawLength;
				}
			}
		}

		return rawLength;
	}

	@Override
	/** {@inheritDoc} */
	public StringBuilder getProposalDescription(final StringBuilder builder) {
		return builder.append("union");
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
	public void generateCode( final JavaGenData aData, final StringBuilder source ) {
		if (lastTimeGenerated != null && !lastTimeGenerated.isLess(aData.getBuildTimstamp())) {
			return;
		}

		lastTimeGenerated = aData.getBuildTimstamp();

		final String genName = getGenNameOwn();
		final String displayName = getFullName();

		generateCodeTypedescriptor(aData, source);

		final List<FieldInfo> fieldInfos =  new ArrayList<FieldInfo>();
		boolean hasOptional = false;
		for ( final CompField compField : compFieldMap.fields ) {
			final IType cfType = compField.getType();
			final FieldInfo fi = new FieldInfo(cfType.getGenNameValue( aData, source ),
					cfType.getGenNameTemplate(aData, source),
					compField.getIdentifier().getName(), compField.getIdentifier().getDisplayName(),
					cfType.getGenNameTypeDescriptor(aData, source));
			hasOptional |= compField.isOptional();
			fieldInfos.add( fi );
		}

		for ( final CompField compField : compFieldMap.fields ) {
			final StringBuilder tempSource = aData.getCodeForType(compField.getType().getGenNameOwn());
			compField.getType().generateCode(aData, tempSource);
		}

		final boolean hasRaw = getGenerateCoderFunctions(MessageEncoding_type.RAW);
		RawASTStruct raw = null;
		if (hasRaw) {
			RawAST dummy_raw;
			if (rawAttribute == null) {
				dummy_raw = new RawAST(getDefaultRawFieldLength());
			} else {
				dummy_raw = rawAttribute;
			}
			raw = new RawASTStruct(dummy_raw, true);

			// building taglist
			final int taglistSize = dummy_raw.taglist == null ? 0 : dummy_raw.taglist.size();
			for (int c = 0; c < taglistSize; c++) {
				final rawAST_single_tag singleTag = dummy_raw.taglist.get(c);
				final rawAST_coding_taglist codingSingleTag = raw.taglist.list.get(c);
				if (singleTag.keyList != null) {
					codingSingleTag.fields = new ArrayList<RawASTStruct.rawAST_coding_field_list>(singleTag.keyList.size());
				}
				codingSingleTag.fieldname = singleTag.fieldName.getName();
				codingSingleTag.varName = FieldSubReference.getJavaGetterName(codingSingleTag.fieldname);
				final Identifier idf = singleTag.fieldName;
				codingSingleTag.fieldnum = getComponentIndexByName(idf);

				final int keyListSize = singleTag.keyList == null ? 0 : singleTag.keyList.size();
				for (int a = 0; a < keyListSize; a++) {
					final rawAST_tag_field_value key = singleTag.keyList.get(a);
					final RawASTStruct.rawAST_coding_field_list codingKey = new RawASTStruct.rawAST_coding_field_list();
					codingSingleTag.fields.add(codingKey);

					codingKey.fields = new ArrayList<RawASTStruct.rawAST_coding_fields>(key.keyField.names.size());
					//codingKey.value = key.value;
					final ExpressionStruct expression = new ExpressionStruct();
					key.v_value.generateCodeExpression(aData, expression, true);
					codingKey.expression = expression;
					final ExpressionStruct nativeExpression = new ExpressionStruct();
					key.v_value.generateCodeExpression(aData, nativeExpression, false);
					codingKey.nativeExpression = nativeExpression;
					codingKey.isOmitValue = key.v_value.getValuetype() == Value_type.OMIT_VALUE;
					codingKey.start_pos = 0;
					final CompField cf = getComponentByIndex(codingSingleTag.fieldnum);
					IType t = cf.getType().getTypeRefdLast(CompilationTimeStamp.getBaseTimestamp());

					final RawASTStruct.rawAST_coding_fields tempField = new rawAST_coding_fields();
					tempField.nthfield = codingSingleTag.fieldnum;
					tempField.nthfieldname = singleTag.fieldName.getName();
					tempField.fieldtype = rawAST_coding_field_type.UNION_FIELD;
					tempField.type = t.getGenNameValue(aData, source);
					tempField.typedesc = t.getGenNameTypeDescriptor(aData, source);
					tempField.unionType = getGenNameValue(aData, source);
					codingKey.fields.add(tempField);

					for (int b = 0; b < key.keyField.names.size(); b++) {
						final RawASTStruct.rawAST_coding_fields newField = new rawAST_coding_fields();
						codingKey.fields.add(newField);

						final Identifier idf2 = key.keyField.names.get(b);
						int comp_index = 0;
						CompField cf2;
						switch (t.getTypetype()) {
						case TYPE_TTCN3_CHOICE:
							comp_index = ((TTCN3_Choice_Type)t).getComponentIndexByName(idf2);
							cf2 = ((TTCN3_Choice_Type)t).getComponentByIndex(comp_index);
							newField.nthfield = comp_index;
							newField.nthfieldname = idf2.getName();
							newField.fieldtype = rawAST_coding_field_type.UNION_FIELD;
							newField.unionType = t.getGenNameValue(aData, source);
							break;
						case TYPE_TTCN3_SEQUENCE:
						case TYPE_TTCN3_SET:
							comp_index = ((TTCN3_Set_Seq_Choice_BaseType)t).getComponentIndexByName(idf2);
							cf2 = ((TTCN3_Set_Seq_Choice_BaseType)t).getComponentByIndex(comp_index);
							newField.nthfield = comp_index;
							newField.nthfieldname = idf2.getName();
							if (cf2.isOptional()) {
								newField.fieldtype = rawAST_coding_field_type.OPTIONAL_FIELD;
							} else {
								newField.fieldtype = rawAST_coding_field_type.MANDATORY_FIELD;
							}
							break;
						default:
							//internal error
							return;
						}

						final IType field_type = cf2.getType();
						newField.type = field_type.getGenNameValue(aData, source);
						newField.typedesc = field_type.getGenNameTypeDescriptor(aData, source);
						if (field_type.getTypetype() == Type_type.TYPE_TTCN3_SEQUENCE && ((TTCN3_Sequence_Type)field_type).rawAttribute != null
								&& (((TTCN3_Sequence_Type)field_type).rawAttribute.pointerto == null || ((TTCN3_Sequence_Type)field_type).rawAttribute.lengthto != null)) {
							codingKey.start_pos = -1;
						}

						if (t.getTypetype() == Type_type.TYPE_TTCN3_SEQUENCE) {
							IType t2;
							for (int i = 0; i < comp_index && codingKey.start_pos >= 0; i++) {
								t2 = ((TTCN3_Sequence_Type)t).getComponentByIndex(i).getType();
								if (t2.getRawLength(aData.getBuildTimstamp()) >= 0) {
									if (((Type)t2).rawAttribute != null) {
										codingKey.start_pos += ((Type)t2).rawAttribute.padding;
									}
									codingKey.start_pos += ((Type)t2).getRawLength(aData.getBuildTimstamp());
								} else {
									codingKey.start_pos = -1;
								}
							}

						}

						t = field_type.getTypeRefdLast(CompilationTimeStamp.getBaseTimestamp());
					}
				}
			}
		}

		UnionGenerator.generateValueClass(aData, source, genName, displayName, fieldInfos, hasOptional, hasRaw, raw);
		UnionGenerator.generateTemplateClass(aData, source, genName, displayName, fieldInfos, hasOptional);

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

		final CompField compField = getComponentByName(fieldId.getName());
		final Type nextType = compField.getType();

		expression.expression.append(MessageFormat.format("if({0}) '{'\n", globalId));
		closingBrackets.insert(0, "}\n");

		final String temporalId = aData.getTemporaryVariableName();
		final String temporalId2 = aData.getTemporaryVariableName();
		final String nextTypeGenName = isTemplate ? nextType.getGenNameTemplate(aData, expression.expression) : nextType.getGenNameValue(aData, expression.expression);
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
	public String generateConversion(final JavaGenData aData, final IType fromType, final String fromName, final ExpressionStruct expression) {
		final IType refdType = fromType.getTypeRefdLast(CompilationTimeStamp.getBaseTimestamp());
		if (refdType == null || this == refdType) {
			//no need to convert
			return fromName;
		}

		switch (refdType.getTypetype()) {
		case TYPE_TTCN3_CHOICE:{
			//heavy conversion is needed
			final TTCN3_Choice_Type realFromType = (TTCN3_Choice_Type) refdType;
			return generateConversionTTCNChoiceToTTCNChoice(aData, realFromType, fromName, expression);
		}
		case TYPE_ASN1_CHOICE:{
			//heavy conversion is needed
			final ASN1_Choice_Type realFromType = (ASN1_Choice_Type) refdType;
			return generateConversionASNChoiceToTTCNChoice(aData, realFromType, fromName, expression);
		}
		default:
			expression.expression.append(MessageFormat.format("//FIXME conversion from {0} to {1} is not needed or nor supported yet\n", fromType.getTypename(), getTypename()));
			break;
		}

		// the default implementation does nothing
		return fromName;
	}

	protected String generateConversionTTCNChoiceToTTCNChoice(final JavaGenData aData, final TTCN3_Choice_Type fromType, final String fromName, final ExpressionStruct expression) {
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
						conversionFunctionBody.append(MessageFormat.format("\t\t\tfinal {0} {1} = from.constGet_field_{2}();\n", fromFieldType.getGenNameValue(aData, conversionFunctionBody), tempId2, fromFieldName.getName()));
						conversionFunctionBody.append(MessageFormat.format("\t\t\tif ({0}.is_bound()) '{'\n", tempId2));
						
						final ExpressionStruct tempExpression = new ExpressionStruct();
						final String tempId3 = toFieldType.generateConversion(aData, fromFieldType, tempId2, tempExpression);
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

	protected String generateConversionASNChoiceToTTCNChoice(final JavaGenData aData, final ASN1_Choice_Type fromType, final String fromName, final ExpressionStruct expression) {
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
			conversionFunctionBody.append("\t\tif(!from.is_bound()) {\n");
			conversionFunctionBody.append("\t\t\treturn false;\n");
			conversionFunctionBody.append("\t\t}\n");
			conversionFunctionBody.append("\t\tswitch (from.get_selection()) {\n");

			for (int i = 0; i < fromType.getNofComponents(CompilationTimeStamp.getBaseTimestamp()); i++) {
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
						conversionFunctionBody.append(MessageFormat.format("\t\t\tfinal {0} {1} = from.constGet_field_{2}();\n", fromFieldType.getGenNameValue(aData, conversionFunctionBody), tempId2, fromFieldName.getName()));
						conversionFunctionBody.append(MessageFormat.format("\t\t\tif ({0}.is_bound()) '{'\n", tempId2));
						
						final ExpressionStruct tempExpression = new ExpressionStruct();
						final String tempId3 = toFieldType.generateConversion(aData, fromFieldType, tempId2, tempExpression);
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
