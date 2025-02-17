/******************************************************************************
 * Copyright (c) 2000-2021 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.types;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.ArraySubReference;
import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.FieldSubReference;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.ISubReference;
import org.eclipse.titan.designer.AST.ISubReference.Subreference_type;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.IValue.Value_type;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.Identifier.Identifier_type;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.ParameterisedSubReference;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.Type;
import org.eclipse.titan.designer.AST.TypeCompatibilityInfo;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.attributes.AnytypeAttribute;
import org.eclipse.titan.designer.AST.TTCN3.attributes.AttributeSpecification;
import org.eclipse.titan.designer.AST.TTCN3.attributes.ExtensionAttribute;
import org.eclipse.titan.designer.AST.TTCN3.attributes.ExtensionAttribute.ExtensionAttribute_type;
import org.eclipse.titan.designer.AST.TTCN3.attributes.JsonAST;
import org.eclipse.titan.designer.AST.TTCN3.attributes.Qualifiers;
import org.eclipse.titan.designer.AST.TTCN3.attributes.SingleWithAttribute;
import org.eclipse.titan.designer.AST.TTCN3.attributes.SingleWithAttribute.Attribute_Type;
import org.eclipse.titan.designer.AST.TTCN3.attributes.WithAttributesPath;
import org.eclipse.titan.designer.AST.TTCN3.definitions.TTCN3Module;
import org.eclipse.titan.designer.AST.TTCN3.templates.ITTCN3Template;
import org.eclipse.titan.designer.AST.TTCN3.templates.ITTCN3Template.Completeness_type;
import org.eclipse.titan.designer.AST.TTCN3.templates.ITTCN3Template.Template_type;
import org.eclipse.titan.designer.AST.TTCN3.templates.NamedTemplate;
import org.eclipse.titan.designer.AST.TTCN3.templates.Named_Template_List;
import org.eclipse.titan.designer.AST.TTCN3.types.UnionGenerator.FieldInfo;
import org.eclipse.titan.designer.AST.TTCN3.types.subtypes.SubType;
import org.eclipse.titan.designer.AST.TTCN3.values.Anytype_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Expression_Value.Operation_type;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.ExpressionStruct;
import org.eclipse.titan.designer.compiler.JavaGenData;
import org.eclipse.titan.designer.editors.ProposalCollector;
import org.eclipse.titan.designer.editors.actions.DeclarationCollector;
import org.eclipse.titan.designer.graphics.ImageCache;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.extensionattributeparser.ExtensionAttributeAnalyzer;

/**
 * anytype type.
 *
 * @author Kristof Szabados
 * */
public final class Anytype_Type extends Type {
	private static final String TEMPLATENOTALLOWED = "{0} cannot be used for anytype type `{1}''";
	private static final String LENGTHRESTRICTIONNOTALLOWED = "Length restriction is not allowed for anytype type `{0}''";
	private static final String ONEFIELDEXPECTED = "A template for anytype type must contain exactly one selected field";
	private static final String ANYTYPEEXPECTED = "Anytype value was expected for type `{0}''";
	private static final String NONEXISTENTUNION = "Reference to a non-existent field `{0}'' in anytype value for type `{1}''";

	private static final String NOCOMPATIBLEFIELD = "Type anytype `{0}'' doesn''t have any field compatible with `{1}''";
	private static final String NOTCOMPATIBLEANYTYPE = "Type anytype is compatible only with other anytype types";

	private CompFieldMap compFieldMap;

	private boolean insideCanHaveCoding = false;

	public Anytype_Type() {
		clear();
	}

	@Override
	/** {@inheritDoc} */
	public Type_type getTypetype() {
		return Type_type.TYPE_ANYTYPE;
	}

	@Override
	/** {@inheritDoc} */
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		compFieldMap.setMyScope(scope);
	}

	/** @return the number of components */
	public int getNofComponents() {
		if (compFieldMap == null) {
			return 0;
		}

		return compFieldMap.fields.size();
	}

	/**
	 * Returns the element at the specified position.
	 *
	 * @param index index of the element to return
	 * @return the element at the specified position in this list
	 */
	public CompField getComponentByIndex(final int index) {
		return compFieldMap.fields.get(index);
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
		case TYPE_ANYTYPE: {
			final Anytype_Type tempType = (Anytype_Type) temp;
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
			for (int i = 0; i < getNofComponents(); i++) {
				final CompField cf = getComponentByIndex(i);
				final IType cfType = cf.getType().getTypeRefdLast(timestamp);
				for (int j = 0; j < tempType.getNofComponents(); j++) {
					final CompField tempComponentField = tempType.getComponentByIndex(j);
					final IType tempTypeCompFieldType = tempComponentField.getType().getTypeRefdLast(timestamp);
					if (!cf.getIdentifier().getDisplayName().equals(tempComponentField.getIdentifier().getDisplayName())
							/*|| !cfType.getMyScope().getModuleScope().equals(tempTypeCompFieldType.getMyScope().getModuleScope())*/) {
						continue;
					}
					lChain.markState();
					rChain.markState();
					lChain.add(cfType);
					rChain.add(tempTypeCompFieldType);
					if (cfType == tempTypeCompFieldType
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
		case TYPE_ASN1_CHOICE:
		case TYPE_TTCN3_CHOICE:
			info.setErrorStr(NOTCOMPATIBLEANYTYPE);
			return false;
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
		return getFullName();
	}

	@Override
	/** {@inheritDoc} */
	public String getOutlineIcon() {
		return "anytype.gif";
	}

	/**
	 * Clears the fields of this anytype.
	 * <p>
	 * The fields of the anytype have to be recollected every time the module is checked,
	 * as definitions might have changed.
	 * */
	private void clear() {
		compFieldMap = new CompFieldMap();
		compFieldMap.setMyType(this);
		compFieldMap.setFullNameParent(this);
	}

	/**
	 * Adds a component to the list of components.
	 *
	 * @param field the component to be added.
	 * */
	public void addComp(final CompField field) {
		compFieldMap.addComp(field);
	}

	/**
	 * Returns whether a component with the name exists or not..
	 *
	 * @param name the name of the element to check
	 * @return true if there is an element with that name, false otherwise.
	 */
	public boolean hasComponentWithName(final String name) {
		if (compFieldMap.componentFieldMap == null) {
			return false;
		}

		return compFieldMap.componentFieldMap.containsKey(name);
	}

	/**
	 * Returns the element with the specified name.
	 *
	 * @param name the name of the element to return
	 * @return the element with the specified name in this list, or null if none was found
	 */
	public CompField getComponentByName(final String name) {
		if (compFieldMap.componentFieldMap == null) {
			return null;
		}

		return compFieldMap.componentFieldMap.get(name);
	}

	@Override
	/** {@inheritDoc} */
	public SubType.SubType_type getSubtypeType() {
		return SubType.SubType_type.ST_UNION;
	}

	@Override
	/** {@inheritDoc} */
	public void check(final CompilationTimeStamp timestamp) {
		if (lastTimeChecked != null && !lastTimeChecked.isLess(timestamp)) {
			return;
		}

		lastTimeChecked = timestamp;
		isErroneous = false;

		analyzeExtensionAttributes(timestamp);

		initAttributes(timestamp);

		compFieldMap.check(timestamp);

		if (myScope != null) {
			checkEncode(timestamp);
			checkVariants(timestamp);
		}
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

		for ( final CompField compField : compFieldMap.fields ) {
			compField.getType().getTypesWithNoCodingTable(timestamp, typeList, onlyOwnTable);
		}
	}

	/**
	 * Convert and check the anytype attributes applied to the module of this type.
	 *
	 * @param timestamp
	 *                the timestamp of the actual build cycle.
	 * */
	private void analyzeExtensionAttributes(final CompilationTimeStamp timestamp) {
		clear();

		final TTCN3Module myModule = (TTCN3Module) getMyScope().getModuleScope();
		final WithAttributesPath moduleAttributePath = myModule.getAttributePath();

		if (moduleAttributePath == null) {
			return;
		}

		final List<SingleWithAttribute> realAttributes = moduleAttributePath.getRealAttributes(timestamp);

		SingleWithAttribute attribute;
		List<AttributeSpecification> specifications = null;
		for (int i = 0; i < realAttributes.size(); i++) {
			attribute = realAttributes.get(i);
			if (Attribute_Type.Extension_Attribute.equals(attribute.getAttributeType())) {
				final Qualifiers qualifiers = attribute.getQualifiers();
				if (qualifiers == null || qualifiers.getNofQualifiers() == 0) {
					if (specifications == null) {
						specifications = new ArrayList<AttributeSpecification>();
					}
					specifications.add(attribute.getAttributeSpecification());
				}
			}
		}

		if (specifications == null) {
			return;
		}

		final List<ExtensionAttribute> attributes = new ArrayList<ExtensionAttribute>();
		AttributeSpecification specification;
		for (int i = 0; i < specifications.size(); i++) {
			specification = specifications.get(i);
			final ExtensionAttributeAnalyzer analyzer = new ExtensionAttributeAnalyzer();
			analyzer.parse(specification);
			final List<ExtensionAttribute> temp = analyzer.getAttributes();
			if (temp != null) {
				attributes.addAll(temp);
			}
		}

		final Scope definitionsScope = myModule.getDefinitions();
		ExtensionAttribute extensionAttribute;
		for (int i = 0; i < attributes.size(); i++) {
			extensionAttribute = attributes.get(i);

			if(ExtensionAttribute_type.ANYTYPE.equals(extensionAttribute.getAttributeType())) {
				final AnytypeAttribute anytypeAttribute = (AnytypeAttribute) extensionAttribute;

				for (int j = 0; j < anytypeAttribute.getNofTypes(); j++) {
					final Type tempType = anytypeAttribute.getType(j);

					String fieldName;
					Identifier identifier = null;
					if (Type_type.TYPE_REFERENCED.equals(tempType.getTypetype())) {
						final Reference reference = ((Referenced_Type) tempType).getReference();
						identifier = reference.getId();
						fieldName = identifier.getTtcnName();
					} else {
						fieldName = tempType.getTypename();
						identifier = new Identifier(Identifier_type.ID_TTCN, fieldName);
					}

					tempType.setMyScope(definitionsScope);
					addComp(new CompField(identifier, tempType, false, null));
				}
			}
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
			last = last.setValuetype(timestamp, Value_type.ANYTYPE_VALUE);
			if (!last.getIsErroneous(timestamp)) {
				selfReference= checkThisValueAnytype(timestamp, (Anytype_Value) last, lhs, valueCheckingOptions.expected_value,
						valueCheckingOptions.incomplete_allowed, valueCheckingOptions.str_elem);
			}
			break;
		case ANYTYPE_VALUE:
			selfReference = checkThisValueAnytype(timestamp, (Anytype_Value) last, lhs, valueCheckingOptions.expected_value,
					valueCheckingOptions.incomplete_allowed, valueCheckingOptions.str_elem);
			break;
		case EXPRESSION_VALUE:
		case MACRO_VALUE:
			// already checked
			break;
		default:
			value.getLocation().reportSemanticError(MessageFormat.format(ANYTYPEEXPECTED, getFullName()));
			value.setIsErroneous(true);
			break;
		}

		value.setLastTimeChecked(timestamp);

		return selfReference;
	}

	private boolean checkThisValueAnytype(final CompilationTimeStamp timestamp, final Anytype_Value value, final Assignment lhs, final Expected_Value_type expectedValue,
			final boolean incompleteAllowed, final boolean strElem) {
		boolean selfReference = false;

		final Identifier name = value.getName();
		if (!hasComponentWithName(name.getName())) {
			value.getLocation().reportSemanticError(MessageFormat.format(NONEXISTENTUNION, name.getDisplayName(), getFullName()));
			setIsErroneous(true);
			return selfReference;
		}

		final Type alternativeType = getComponentByName(name.getName()).getType();
		IValue alternativeValue = value.getValue();
		alternativeValue.setMyGovernor(alternativeType);
		alternativeValue = alternativeType.checkThisValueRef(timestamp, alternativeValue);
		selfReference = alternativeType.checkThisValue(timestamp, alternativeValue, lhs, new ValueCheckingOptions(expectedValue, incompleteAllowed, false, true, false, strElem));

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
				if (field != null) {
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
	public boolean canHaveCoding(final CompilationTimeStamp timestamp, final MessageEncoding_type coding) {
		if (insideCanHaveCoding) {
			insideCanHaveCoding = false;
			return true;
		}
		insideCanHaveCoding = true;

		if (coding == MessageEncoding_type.BER) {
			insideCanHaveCoding = false;
			return hasEncoding(timestamp, MessageEncoding_type.BER, null);
		}

		for ( final CompField compField : compFieldMap.fields ) {
			if (!compField.getType().getTypeRefdLast(timestamp).canHaveCoding(timestamp, coding)) {
				insideCanHaveCoding = false;
				return false;
			}
		}

		insideCanHaveCoding = false;
		return true;
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
		for ( final CompField compField : compFieldMap.fields ) {

			compField.getType().checkCodingAttributes(timestamp, refChain);
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
	/** {@inheritDoc} */
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
				case TYPE_ANYTYPE:
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

		if (jsonAttribute.parsed_default_value != null) {
			checkJsonDefault(timestamp);
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
			final CompField compField = compFieldMap.getCompWithName(id);
			if (compField == null) {
				subreference.getLocation().reportSemanticError(
						MessageFormat.format(FieldSubReference.NONEXISTENTSUBREFERENCE, ((FieldSubReference) subreference).getId().getDisplayName(), getTypename()));
				return null;
			}

			final IType fieldType = compField.getType();
			if (fieldType == null) {
				return null;
			}

			final Expected_Value_type internalExpectation =
					(expectedIndex == Expected_Value_type.EXPECTED_TEMPLATE) ? Expected_Value_type.EXPECTED_DYNAMIC_VALUE : expectedIndex;

			return fieldType.getFieldType(timestamp, reference, actualSubReference + 1, internalExpectation, refChain, false);
		case parameterisedSubReference:
			subreference.getLocation().reportSemanticError(
					MessageFormat.format(FieldSubReference.INVALIDSUBREFERENCE, ((ParameterisedSubReference) subreference).getId().getDisplayName(),	getTypename()));
			return null;
		default:
			subreference.getLocation().reportSemanticError(ISubReference.INVALIDSUBREFERENCE);
			return null;
		}
	}

	@Override
	/** {@inheritDoc} */
	public boolean getSubrefsAsArray(final CompilationTimeStamp timestamp, final Reference reference, final int actualSubReference,
			final List<Integer> subrefsArray, final List<IType> typeArray) {
		final List<ISubReference> subreferences = reference.getSubreferences();
		if (subreferences.size() <= actualSubReference) {
			return true;
		}

		final ISubReference subreference = subreferences.get(actualSubReference);
		switch (subreference.getReferenceType()) {
		case arraySubReference:
			subreference.getLocation().reportSemanticError(MessageFormat.format(ArraySubReference.INVALIDSUBREFERENCE, getTypename()));
			return false;
		case fieldSubReference:
			final Identifier id = subreference.getId();
			final CompField compField = compFieldMap.getCompWithName(id);
			if (compField == null) {
				subreference.getLocation().reportSemanticError(
						MessageFormat.format(FieldSubReference.NONEXISTENTSUBREFERENCE, ((FieldSubReference) subreference).getId().getDisplayName(), getTypename()));
				return false;
			}

			final IType fieldType = compField.getType();
			if (fieldType == null) {
				return false;
			}

			final int fieldIndex = compFieldMap.fields.indexOf(compField);
			subrefsArray.add(fieldIndex);
			typeArray.add(this);
			return fieldType.getSubrefsAsArray(timestamp, reference, actualSubReference + 1, subrefsArray, typeArray);
		case parameterisedSubReference:
			subreference.getLocation().reportSemanticError(
					MessageFormat.format(FieldSubReference.INVALIDSUBREFERENCE, ((ParameterisedSubReference) subreference).getId().getDisplayName(),	getTypename()));
			return false;
		default:
			subreference.getLocation().reportSemanticError(ISubReference.INVALIDSUBREFERENCE);
			return false;
		}
	}

	@Override
	/** {@inheritDoc} */
	public boolean getFieldTypesAsArray(final Reference reference, final int actualSubReference, final List<IType> typeArray) {
		final List<ISubReference> subreferences = reference.getSubreferences();
		if (subreferences.size() <= actualSubReference) {
			return true;
		}

		final ISubReference subreference = subreferences.get(actualSubReference);
		switch (subreference.getReferenceType()) {
		case arraySubReference:
			return false;
		case fieldSubReference:
			if (compFieldMap == null) {
				return false;
			}

			final Identifier id = subreference.getId();
			final CompField compField = compFieldMap.getCompWithName(id);
			if (compField == null) {
				return false;
			}

			final IType fieldType = compField.getType();
			if (fieldType == null) {
				return false;
			}
			typeArray.add(this);
			return fieldType.getFieldTypesAsArray(reference, actualSubReference + 1, typeArray);
		case parameterisedSubReference:
			return false;
		default:
			return false;
		}
	}

	@Override
	/** {@inheritDoc} */
	public void checkMapParameter(final CompilationTimeStamp timestamp, final IReferenceChain refChain, final Location errorLocation) {
		if (refChain.contains(this)) {
			return;
		}

		refChain.add(this);
		for (int i = 0, size = getNofComponents(); i < size; i++) {
			final IType type = getComponentByIndex(i).getType();
			type.checkMapParameter(timestamp, refChain, errorLocation);
		}
	}

	@Override
	/** {@inheritDoc} */
	public StringBuilder getProposalDescription(final StringBuilder builder) {
		return builder.append("anytype");
	}

	/**
	 * Searches and adds a completion proposal to the provided collector if a
	 * valid one is found.
	 * <p>
	 * In case of structural types, the member fields are checked if they could
	 * complete the proposal.
	 *
	 * @param propCollector the proposal collector to add the proposal to, and
	 *            used to get more information
	 * @param i index, used to identify which element of the reference (used by
	 *            the proposal collector) should be checked for completions.
	 * */
	@Override
	/** {@inheritDoc} */
	public void addProposal(final ProposalCollector propCollector, final int i) {
		final List<ISubReference> subreferences = propCollector.getReference().getSubreferences();
		if (subreferences.size() <= i) {
			return;
		}

		final ISubReference subreference = subreferences.get(i);
		if (Subreference_type.fieldSubReference.equals(subreference.getReferenceType())) {
			if (subreferences.size() > i + 1) {
				// the reference might go on
				final CompField compField = compFieldMap.getCompWithName(subreference.getId());
				if (compField == null) {
					return;
				}

				final IType type = compField.getType();
				if (type != null) {
					type.addProposal(propCollector, i + 1);
				}
			} else {
				// final part of the reference
				final List<CompField> compFields = compFieldMap.getComponentsWithPrefixCaseInsensitive(subreference.getId().getName());
				for (final CompField compField : compFields) {
					final String proposalKind = compField.getType().getProposalDescription(new StringBuilder()).toString();
					propCollector.addProposal(compField.getIdentifier(), " - " + proposalKind, ImageCache.getImage(getOutlineIcon()), proposalKind);
				}
			}
		}
	}

	/**
	 * Searches and adds a declaration proposal to the provided collector if a
	 * valid one is found.
	 * <p>
	 * In case of structural types, the member fields are checked if they could
	 * be the declaration searched for.
	 *
	 * @param declarationCollector the declaration collector to add the
	 *            declaration to, and used to get more information.
	 * @param i index, used to identify which element of the reference (used by
	 *            the declaration collector) should be checked.
	 * */
	@Override
	/** {@inheritDoc} */
	public void addDeclaration(final DeclarationCollector declarationCollector, final int i) {
		final List<ISubReference> subreferences = declarationCollector.getReference().getSubreferences();
		if (subreferences.size() <= i) {
			return;
		}

		final ISubReference subreference = subreferences.get(i);
		if (Subreference_type.fieldSubReference.equals(subreference.getReferenceType())) {
			if (subreferences.size() > i + 1) {
				// the reference might go on
				final CompField compField = compFieldMap.getCompWithName(subreference.getId());
				if (compField == null) {
					return;
				}

				final IType type = compField.getType();
				if (type != null) {
					type.addDeclaration(declarationCollector, i + 1);
				}
			} else {
				// final part of the reference
				final List<CompField> compFields = compFieldMap.getComponentsWithPrefixCaseInsensitive(subreference.getId().getName());
				for (final CompField compField : compFields) {
					declarationCollector.addDeclaration(compField.getIdentifier().getDisplayName(), compField.getIdentifier().getLocation(), this);
				}
			}
		}
	}

	@Override
	/** {@inheritDoc} */
	public void getEnclosingField(final int offset, final ReferenceFinder rf) {
		compFieldMap.getEnclosingField(offset, rf);
	}

	@Override
	/** {@inheritDoc} */
	protected boolean memberAccept(final ASTVisitor v) {
		if (!super.memberAccept(v)) {
			return false;
		}
		if (compFieldMap!=null && !compFieldMap.accept(v)) {
			return false;
		}
		return true;
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
	public boolean needsOwnRawDescriptor(final JavaGenData aData) {
		return true;
	}

	@Override
	/** {@inheritDoc} */
	public String getGenNameRawDescriptor(final JavaGenData aData, final StringBuilder source) {
		return getGenNameOwn(aData) + "." + getGenNameOwn() + "_raw_";
	}

	@Override
	/** {@inheritDoc} */
	public boolean needsOwnJsonDescriptor(final JavaGenData aData) {
		return true;
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

		final String genName = getGenNameOwn();
		final String displayName = getFullName();

		final StringBuilder localTypeDescriptor = new StringBuilder();
		generateCodeTypedescriptor(aData, source, localTypeDescriptor, null);
		generateCodeDefaultCoding(aData, source, localTypeDescriptor);
		final StringBuilder localCodingHandler = new StringBuilder();
		generateCodeForCodingHandlers(aData, source, localCodingHandler);

		final boolean hasJson = getGenerateCoderFunctions(MessageEncoding_type.JSON);
		final List<FieldInfo> fieldInfos =  new ArrayList<FieldInfo>();
		boolean hasOptional = false;
		for ( final CompField compField : compFieldMap.fields ) {
			final IType cfType = compField.getType();
			final String jsonAlias = cfType.getJsonAttribute() != null ? cfType.getJsonAttribute().alias : null; 
			final int JsonValueType = hasJson ? cfType.getJsonValueType() : 0;
			final FieldInfo fi = new FieldInfo(cfType.getGenNameValue( aData, source ),
					cfType.getGenNameTemplate(aData, source),
					compField.getIdentifier().getName(), compField.getIdentifier().getDisplayName(),
					cfType.getGenNameTypeDescriptor(aData, source), jsonAlias, JsonValueType);
			hasOptional |= compField.isOptional();
			fieldInfos.add( fi );
		}

		for ( final CompField compField : compFieldMap.fields ) {
			final StringBuilder tempSource = aData.getCodeForType(compField.getType().getGenNameOwn());
			compField.getType().generateCode(aData, tempSource);
		}

		final boolean jsonAsValue = jsonAttribute != null ? jsonAttribute.as_value : false;
		final boolean hasRaw = getGenerateCoderFunctions(MessageEncoding_type.RAW);
		//FIXME can have raw attributes
		UnionGenerator.generateValueClass(aData, source, genName, displayName, fieldInfos, hasOptional, hasRaw, null, hasJson, true, jsonAsValue, localTypeDescriptor, localCodingHandler);
		UnionGenerator.generateTemplateClass(aData, source, genName, displayName, fieldInfos, hasOptional);

		if (hasDoneAttribute()) {
			generateCodeDone(aData, source);
		}
		if (subType != null) {
			subType.generateCode(aData, source);
		}
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

		final Identifier fieldId = ((FieldSubReference) subReference).getId();
		final CompField compField = getComponentByName(fieldId.getName());
		final Type nextType = compField.getType();
		final String nextTypeGenName = isTemplate ? nextType.getGenNameTemplate(aData, expression.expression) : nextType.getGenNameValue(aData, expression.expression);
		final boolean nextOptional = !isTemplate && compField.isOptional();
		if (nextOptional) {
			expression.expression.append(MessageFormat.format("if({0}) '{'\n", globalId));
			closingBrackets.insert(0, "}\n");
			final String temporalId = aData.getTemporaryVariableName();
			aData.addBuiltinTypeImport("Optional");
			expression.expression.append(MessageFormat.format("final Optional<{0}> {1} = {2}.get_field_{3}();\n",
					nextTypeGenName, temporalId, externalId, FieldSubReference.getJavaGetterName( fieldId.getName())));

			if (subReferenceIndex == subreferences.size()-1) {
				expression.expression.append(MessageFormat.format("switch({0}.get_selection()) '{'\n", temporalId));
				expression.expression.append("case OPTIONAL_UNBOUND:\n");
				expression.expression.append(MessageFormat.format("{0} = false;\n", globalId));
				expression.expression.append("break;\n");
				expression.expression.append("case OPTIONAL_OMIT:\n");
				expression.expression.append(MessageFormat.format("{0} = {1};\n", globalId, optype == Operation_type.ISBOUND_OPERATION?"true":"false"));
				expression.expression.append("break;\n");
				expression.expression.append("default:\n");
				expression.expression.append("{\n");

				final String temporalId2 = aData.getTemporaryVariableName();
				expression.expression.append(MessageFormat.format("final {0} {1} = {2}.constGet();\n", nextTypeGenName,  temporalId2, temporalId));

				if (optype == Operation_type.ISBOUND_OPERATION) {
					expression.expression.append(MessageFormat.format("{0} = {1}.is_bound();\n", globalId, temporalId2));
				} else if (optype == Operation_type.ISVALUE_OPERATION) {
					expression.expression.append(MessageFormat.format("{0} = {1}.is_value();\n", globalId, temporalId2));
				} else if (optype == Operation_type.ISPRESENT_OPERATION) {
					expression.expression.append(MessageFormat.format("{0} = {1}.is_present({2});\n", globalId, temporalId2, isTemplate && aData.getAllowOmitInValueList()?"true":""));
				} else if (optype == Operation_type.ISCHOOSEN_OPERATION) {
					expression.expression.append(MessageFormat.format("{0} = {1}.ischosen({2});\n", globalId, temporalId2, field));
				}

				expression.expression.append("break;}\n");
				expression.expression.append("}\n");
				//at the end of the reference chain

				nextType.generateCodeIsPresentBoundChosen(aData, expression, subreferences, subReferenceIndex + 1, globalId, temporalId2, isTemplate, optype, field, targetScope);
			} else {
				//still more to go
				expression.expression.append(MessageFormat.format("switch({0}.get_selection()) '{'\n", temporalId));
				expression.expression.append("case OPTIONAL_UNBOUND:\n");
				expression.expression.append("case OPTIONAL_OMIT:\n");
				expression.expression.append(MessageFormat.format("{0} = false;\n", globalId));
				expression.expression.append("break;\n");
				expression.expression.append("default:\n");
				expression.expression.append("break;\n");
				expression.expression.append("}\n");

				expression.expression.append(MessageFormat.format("if({0}) '{'\n", globalId));
				closingBrackets.insert(0, "}\n");
				final String temporalId2 = aData.getTemporaryVariableName();
				expression.expression.append(MessageFormat.format("final {0} {1} = {2}.constGet();\n", nextTypeGenName, temporalId2, temporalId));
				expression.expression.append(MessageFormat.format("{0} = {1}.is_bound();\n", globalId, temporalId2));

				nextType.generateCodeIsPresentBoundChosen(aData, expression, subreferences, subReferenceIndex + 1, globalId, temporalId2, isTemplate, optype, field, targetScope);
			}
		} else {
			expression.expression.append(MessageFormat.format("if({0}) '{'\n", globalId));
			closingBrackets.insert(0, "}\n");

			final String temporalId = aData.getTemporaryVariableName();
			final String temporalId2 = aData.getTemporaryVariableName();
			final String currentTypeGenName = isTemplate ? getGenNameTemplate(aData, expression.expression) : getGenNameValue(aData, expression.expression);
			expression.expression.append(MessageFormat.format("final {0} {1} = {2};\n", currentTypeGenName, temporalId, externalId));
			expression.expression.append(MessageFormat.format("final {0} {1} = {2}.get_field_{3}();\n", nextTypeGenName, temporalId2, temporalId, FieldSubReference.getJavaGetterName( fieldId.getName())));

			if (optype == Operation_type.ISBOUND_OPERATION) {
				expression.expression.append(MessageFormat.format("{0} = {1}.is_bound();\n", globalId, temporalId2));
			} else if (optype == Operation_type.ISVALUE_OPERATION) {
				expression.expression.append(MessageFormat.format("{0} = {1}.is_value();\n", globalId, temporalId2));
			} else if (optype == Operation_type.ISPRESENT_OPERATION) {
				expression.expression.append(MessageFormat.format("{0} = {1}.{2}({3});\n", globalId, temporalId2, subReferenceIndex!=subreferences.size()-1?"is_bound":"is_present", subReferenceIndex==subreferences.size()-1 && isTemplate && aData.getAllowOmitInValueList()?"true":""));
			} else if (optype == Operation_type.ISCHOOSEN_OPERATION) {
				expression.expression.append(MessageFormat.format("{0} = {1}.is_bound();\n", globalId, temporalId2));
				if (subReferenceIndex==subreferences.size()-1) {
					expression.expression.append(MessageFormat.format("if ({0}) '{'\n", globalId));
					expression.expression.append(MessageFormat.format("{0} = {1}.ischosen({2});\n", globalId, temporalId2, field));
					expression.expression.append("}\n");
				}
			}

			nextType.generateCodeIsPresentBoundChosen(aData, expression, subreferences, subReferenceIndex + 1, globalId, temporalId2, isTemplate, optype, field, targetScope);
		}

		expression.expression.append(closingBrackets);
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
	public String generateConversion(final JavaGenData aData, final IType fromType, final String fromName, final boolean forValue, final ExpressionStruct expression) {
		final IType refdType = fromType.getTypeRefdLast(CompilationTimeStamp.getBaseTimestamp());
		if (refdType == null || this == refdType) {
			//no need to convert
			return fromName;
		}

		switch (refdType.getTypetype()) {
		case TYPE_ANYTYPE:{
			//heavy conversion is needed
			final Anytype_Type realFromType = (Anytype_Type) refdType;
			return generateConversionAnytypeToAnytype(aData, realFromType, fromName, forValue, expression);
		}
		default:
			expression.expression.append(MessageFormat.format("//FIXME conversion from {0} to {1} is not needed or nor supported yet\n", fromType.getTypename(), getTypename()));
			break;
		}

		// the default implementation does nothing
		return fromName;
	}

	protected String generateConversionAnytypeToAnytype(final JavaGenData aData, final Anytype_Type fromType, final String fromName, final boolean forValue, final ExpressionStruct expression) {
		final String tempId = aData.getTemporaryVariableName();
		final String name = getGenNameValue(aData, expression.preamble);
		expression.preamble.append(MessageFormat.format("final {0} {1} = new {0}();\n", name, tempId));
		final String ConversionFunctionName = Type.getConversionFunction(aData, fromType, this, forValue, expression.preamble);
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
