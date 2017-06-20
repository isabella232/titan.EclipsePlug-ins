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
import java.util.Map;

import org.eclipse.titan.designer.AST.CachedReferenceChain;
import org.eclipse.titan.designer.AST.FieldSubReference;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.ISubReference;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.ReferenceChain;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.Type;
import org.eclipse.titan.designer.AST.TypeCompatibilityInfo;
import org.eclipse.titan.designer.AST.ASN1.types.ASN1_Choice_Type;
import org.eclipse.titan.designer.AST.IValue.Value_type;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.templates.ITTCN3Template;
import org.eclipse.titan.designer.AST.TTCN3.templates.NamedTemplate;
import org.eclipse.titan.designer.AST.TTCN3.templates.Named_Template_List;
import org.eclipse.titan.designer.AST.TTCN3.templates.ITTCN3Template.Completeness_type;
import org.eclipse.titan.designer.AST.TTCN3.templates.ITTCN3Template.Template_type;
import org.eclipse.titan.designer.AST.TTCN3.types.subtypes.SubType;
import org.eclipse.titan.designer.AST.TTCN3.values.Choice_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.ExpressionStruct;
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
	public void checkConstructorName(final String definitionName) {
		if (hasComponentWithName(definitionName)) {
			final CompField field = getComponentByName(definitionName);
			field.getIdentifier().getLocation().reportSemanticError(MessageFormat.format(UNSUPPERTED_FIELDNAME, field.getIdentifier().getDisplayName()));
		}
	}

	@Override
	/** {@inheritDoc} */
	public void checkRecursions(final CompilationTimeStamp timestamp, final IReferenceChain referenceChain) {
		if (!referenceChain.add(this)) {
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
		for (CompField compField : map.values()) {
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
	public void checkThisValue(final CompilationTimeStamp timestamp, final IValue value, final ValueCheckingOptions valueCheckingOptions) {
		if (getIsErroneous(timestamp)) {
			return;
		}

		super.checkThisValue(timestamp, value, valueCheckingOptions);

		IValue last = value.getValueRefdLast(timestamp, valueCheckingOptions.expected_value, null);
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
		case SEQUENCE_VALUE:
			if (value.isAsn()) {
				value.getLocation().reportSemanticError(MessageFormat.format(CHOICEEXPECTED, getFullName()));
				value.setIsErroneous(true);
			} else {
				last = last.setValuetype(timestamp, Value_type.CHOICE_VALUE);
				if (!last.getIsErroneous(timestamp)) {
					checkThisValueChoice(timestamp, (Choice_Value) last, valueCheckingOptions.expected_value,
							valueCheckingOptions.incomplete_allowed, valueCheckingOptions.str_elem);
				}
			}
			break;
		case CHOICE_VALUE:
			checkThisValueChoice(timestamp, (Choice_Value) last, valueCheckingOptions.expected_value,
					valueCheckingOptions.incomplete_allowed, valueCheckingOptions.str_elem);
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
	}

	private void checkThisValueChoice(final CompilationTimeStamp timestamp, final Choice_Value value, final Expected_Value_type expectedValue,
			final boolean incompleteAllowed, final boolean strElem) {
		final Identifier name = value.getName();
		if (!hasComponentWithName(name.getName())) {
			if (value.isAsn()) {
				value.getLocation().reportSemanticError(MessageFormat.format(NONEXISTENTCHOICE, name.getDisplayName(), getFullName()));
				value.setIsErroneous(true);
			} else {
				value.getLocation().reportSemanticError(MessageFormat.format(NONEXISTENTUNION, name.getDisplayName(), getFullName()));
				value.setIsErroneous(true);
			}
			return;
		}

		IValue alternativeValue = value.getValue();
		if (alternativeValue == null) {
			return;
		}

		final Type alternativeType = getComponentByName(name.getName()).getType();
		alternativeValue.setMyGovernor(alternativeType);
		alternativeValue = alternativeType.checkThisValueRef(timestamp, alternativeValue);
		alternativeType.checkThisValue(timestamp, alternativeValue, new ValueCheckingOptions(expectedValue,
				incompleteAllowed, false, true, false, strElem));

		value.setLastTimeChecked(timestamp);
	}

	@Override
	/** {@inheritDoc} */
	public void checkThisTemplate(final CompilationTimeStamp timestamp, final ITTCN3Template template,
			final boolean isModified, final boolean implicitOmit) {
		registerUsage(template);
		template.setMyGovernor(this);

		if (getIsErroneous(timestamp)) {
			return;
		}

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
					namedTemplateTemplate.checkThisTemplateGeneric(
							timestamp, fieldType, Completeness_type.MAY_INCOMPLETE.equals(completeness), false, false, true, implicitOmit);
				}
			}
		} else {
			template.getLocation().reportSemanticError(MessageFormat.format(TEMPLATENOTALLOWED, template.getTemplateTypeName(), getTypename()));
		}

		if (template.getLengthRestriction() != null) {
			template.getLocation().reportSemanticError(MessageFormat.format(LENGTHRESTRICTIONNOTALLOWED, getTypename()));
		}
	}

	@Override
	/** {@inheritDoc} */
	public StringBuilder getProposalDescription(final StringBuilder builder) {
		return builder.append("union");
	}

	@Override
	/** {@inheritDoc} */
	public String getGenNameValue(JavaGenData aData, StringBuilder source, Scope scope) {
		return getGenNameOwn(scope);
	}

	@Override
	/** {@inheritDoc} */
	public void generateCode( final JavaGenData aData, final StringBuilder source ) {
		final String genName = getGenNameOwn();
		final String displayName = getFullName();
		aData.addBuiltinTypeImport("Base_Type");

		for ( final CompField compField : compFieldMap.fields ) {
			StringBuilder tempSource = aData.getCodeForType(compField.getType().getGenNameOwn());
			compField.getType().generateCode(aData, tempSource);
		}

		source.append(MessageFormat.format("public static class {0} extends Base_Type '{'\n", genName));
		source.append("public enum union_selection_type { UNBOUND_VALUE, ");
		for (int i = 0 ; i < compFieldMap.fields.size(); i++) {
			if (i > 0) {
				source.append(", ");
			}
			String tempFieldName = compFieldMap.fields.get(i).getIdentifier().getName();
			String fieldName = FieldSubReference.getJavaGetterName( tempFieldName );
			source.append(MessageFormat.format("ALT_{0}", FieldSubReference.getJavaGetterName( fieldName )));
		}
		source.append("};\n");
		source.append("private union_selection_type union_selection;\n");
		source.append("//originally a union which can not be mapped to Java\n");
		source.append("private Base_Type field;\n");

		source.append(MessageFormat.format("public {0}() '{'\n", genName));
		source.append("union_selection = union_selection_type.UNBOUND_VALUE;\n");
		source.append("};\n");
		source.append(MessageFormat.format("public {0}(final {0} otherValue) '{'\n", genName));
		source.append("copy_value(otherValue);\n");
		source.append("};\n");

		source.append(MessageFormat.format("private void copy_value(final {0} otherValue) '{'\n", genName));
		source.append("switch(otherValue.union_selection){\n");
		for (int i = 0 ; i < compFieldMap.fields.size(); i++) {
			CompField field = compFieldMap.fields.get(i);
			String tempFieldName = field.getIdentifier().getName();
			String fieldName = FieldSubReference.getJavaGetterName( tempFieldName );
			source.append(MessageFormat.format("case ALT_{0}:\n", fieldName));
			source.append(MessageFormat.format("field = new {0}(({0})otherValue.field);\n", field.getType().getGenNameValue(aData, source, myScope)));
		}
		source.append("break;\n");
		source.append("default:\n");
		source.append(MessageFormat.format("throw new TtcnError(\"Assignment of an unbound union value of type {0}.\");", displayName));
		source.append("}\n");
		source.append("union_selection = otherValue.union_selection;\n");
		source.append("}\n");

		source.append("//originally operator=\n");
		source.append(MessageFormat.format("public {0} assign( final {0} otherValue ) '{'\n", genName));
		source.append("if(otherValue.union_selection == union_selection_type.UNBOUND_VALUE) {\n");
		source.append(MessageFormat.format("throw new TtcnError( \"Assignment of an unbound {0} value.\" );\n", displayName));
		source.append("}\n");
		source.append("cleanUp();\n");
		source.append("copy_value(otherValue);\n");
		source.append("return this;\n");
		source.append("}\n");

		source.append("@Override\n");
		source.append(MessageFormat.format("public {0} assign( final Base_Type otherValue ) '{'\n", genName));
		source.append(MessageFormat.format("if (otherValue instanceof {0}) '{'\n", genName));
		source.append(MessageFormat.format("return assign(({0})otherValue);\n", genName));
		source.append("}\n");
		source.append(MessageFormat.format("throw new TtcnError(\"Internal Error: value can not be cast to {0}.\");\n", displayName));
			
		source.append("}\n");
		source.append("//originally clean_up\n");
		source.append("public void cleanUp() {\n");
		source.append("field = null;\n");
		source.append("union_selection = union_selection_type.UNBOUND_VALUE;\n");
		source.append("}\n");

		source.append("public boolean isChosen(final union_selection_type checked_selection) {\n");
		source.append("if(checked_selection == union_selection_type.UNBOUND_VALUE) {\n");
		source.append(MessageFormat.format("throw new TtcnError(\"Internal error: Performing ischosen() operation on an invalid field of union type {0}.\");\n", displayName));
		source.append("}\n");
		source.append("if (union_selection == checked_selection) {\n");
		source.append(MessageFormat.format("throw new TtcnError(\"Performing ischosen() operation on an unbound value of union type {0}.\");\n", displayName));
		source.append("}\n");
		source.append("return union_selection == checked_selection;\n");

		source.append("}\n");
		source.append("public boolean isBound() {\n");
		source.append("return union_selection != union_selection_type.UNBOUND_VALUE;\n");
		source.append("}\n");	
		source.append("public boolean isValue() {\n");
		source.append("switch(union_selection) {\n");
		for (int i = 0 ; i < compFieldMap.fields.size(); i++) {
			CompField field = compFieldMap.fields.get(i);
			String tempFieldName = field.getIdentifier().getName();
			String fieldName = FieldSubReference.getJavaGetterName( tempFieldName );
			source.append(MessageFormat.format("case ALT_{0}:\n", fieldName));
		}

		source.append("return field.isValue();\n");
		source.append("default:\n");
		source.append("throw new TtcnError(\"Invalid selection in union is_bound\");\n");
		source.append("}\n");
		source.append("}\n");

		source.append("public boolean isPresent() {\n");
		source.append("return isBound();\n");
		source.append("}\n");

		
		source.append("//originally operator==\n");
		source.append(MessageFormat.format("public TitanBoolean operatorEquals( final {0} otherValue ) '{'\n", genName));
		source.append("if (union_selection == union_selection_type.UNBOUND_VALUE) {\n");
		source.append(MessageFormat.format("throw new TtcnError( \"The left operand of comparison is an unbound value of union type {0}.\" );\n", displayName));
		source.append("}\n");
		source.append("if (otherValue.union_selection == union_selection_type.UNBOUND_VALUE) {\n");
		source.append(MessageFormat.format("throw new TtcnError( \"The right operand of comparison is an unbound value of union type {0}.\" );\n", displayName));
		source.append("}\n");
		source.append("if (union_selection != otherValue.union_selection) {\n");
		source.append("return new TitanBoolean(false);\n");

		source.append("}\n");
		source.append("switch(union_selection) {\n");
		for (int i = 0 ; i < compFieldMap.fields.size(); i++) {
			CompField field = compFieldMap.fields.get(i);
			String tempFieldName = field.getIdentifier().getName();
			String fieldName = FieldSubReference.getJavaGetterName( tempFieldName );
			source.append(MessageFormat.format("case ALT_{0}:\n", fieldName));
			source.append(MessageFormat.format("return (({0})field).operatorEquals(({0})otherValue.field);\n", field.getType().getGenNameValue(aData, source, myScope)));
		}


		source.append("default:\n");
		source.append("return new TitanBoolean(false);\n");
		source.append("}\n");
		source.append("}\n");

		source.append("@Override\n");
		source.append("public TitanBoolean operatorEquals( final Base_Type otherValue ) {\n");
		source.append(MessageFormat.format("if (otherValue instanceof {0}) '{'\n", genName));
		source.append(MessageFormat.format("return operatorEquals(({0})otherValue);\n", genName));
		source.append("}\n");
		source.append(MessageFormat.format("throw new TtcnError(\"Internal Error: value can not be cast to {0}.\");\n", displayName));
		source.append("}\n");

		for (int i = 0 ; i < compFieldMap.fields.size(); i++) {
			CompField field = compFieldMap.fields.get(i);
			String tempFieldName = field.getIdentifier().getName();
			String fieldName = FieldSubReference.getJavaGetterName( tempFieldName );
			String typeName = field.getType().getGenNameValue(aData, source, myScope);
			source.append(MessageFormat.format("public {0} get{1}() '{'\n", typeName, fieldName));
			source.append(MessageFormat.format("if (union_selection != union_selection_type.ALT_{0}) '{'\n", fieldName));
			source.append("cleanUp();\n");
			source.append(MessageFormat.format("field = new {0}();\n", typeName));
			source.append(MessageFormat.format("union_selection = union_selection_type.ALT_{0};\n", fieldName));
			source.append("}\n");
			source.append(MessageFormat.format("return ({0})field;\n", typeName));
			source.append("}\n");

			source.append(MessageFormat.format("public {0} constGet{1}() '{'\n", typeName, fieldName));
			source.append(MessageFormat.format("if (union_selection != union_selection_type.ALT_{0}) '{'\n", fieldName));
			source.append(MessageFormat.format("throw new TtcnError(\"Using non-selected field field1 in a value of union type {0}.\");\n", displayName));
			source.append("}\n");
			source.append(MessageFormat.format("return ({0})field;\n", typeName));
			source.append("}\n");
		}
		
		//TODO: implement
		source.append( "\t\t//TODO: TTCN3_Choice_Type.generateCode() is not fully implemented!\n" );
		source.append("}\n");
	}

	@Override
	/** {@inheritDoc} */
	public boolean isPresentAnyvalueEmbeddedField(ExpressionStruct expression, List<ISubReference> subreferences, int beginIndex) {
		return false;
	}
}
