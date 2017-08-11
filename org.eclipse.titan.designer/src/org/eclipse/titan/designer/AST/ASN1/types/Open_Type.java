/******************************************************************************
 * Copyright (c) 2000-2017 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.ASN1.types;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
import org.eclipse.titan.designer.AST.ParameterisedSubReference;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.Type;
import org.eclipse.titan.designer.AST.TypeCompatibilityInfo;
import org.eclipse.titan.designer.AST.ASN1.ASN1Type;
import org.eclipse.titan.designer.AST.ASN1.IASN1Type;
import org.eclipse.titan.designer.AST.ASN1.TableConstraint;
import org.eclipse.titan.designer.AST.ASN1.Object.ObjectClass_Definition;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.templates.ITTCN3Template;
import org.eclipse.titan.designer.AST.TTCN3.templates.ITTCN3Template.Completeness_type;
import org.eclipse.titan.designer.AST.TTCN3.templates.ITTCN3Template.Template_type;
import org.eclipse.titan.designer.AST.TTCN3.templates.NamedTemplate;
import org.eclipse.titan.designer.AST.TTCN3.templates.Named_Template_List;
import org.eclipse.titan.designer.AST.TTCN3.types.CompField;
import org.eclipse.titan.designer.AST.TTCN3.types.CompFieldMap;
import org.eclipse.titan.designer.AST.TTCN3.types.UnionGenerator;
import org.eclipse.titan.designer.AST.TTCN3.types.UnionGenerator.FieldInfo;
import org.eclipse.titan.designer.AST.TTCN3.values.Choice_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.ExpressionStruct;
import org.eclipse.titan.designer.compiler.JavaGenData;
import org.eclipse.titan.designer.editors.ProposalCollector;
import org.eclipse.titan.designer.editors.actions.DeclarationCollector;
import org.eclipse.titan.designer.graphics.ImageCache;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * Represents a hole type.
 *
 * Please note, that all instances of Open_Type type are always erroneous to
 * stop some error messages, which are produced as we are not yet able to fully
 * handle the constraints.
 *
 * @author Kristof Szabados
 * */
public final class Open_Type extends ASN1Type {
	private static final String TEMPLATENOTALLOWED = "{0} cannot be used for union type `{1}''";
	private static final String LENGTHRESTRICTIONNOTALLOWED = "Length restriction is not allowed for union type `{0}''";
	private static final String ONEFIELDEXPECTED = "A template for union type must contain exactly one selected field";
	private static final String REFERENCETONONEXISTENTFIELD = "Reference to non-existent field `{0}'' in template of type `{1}''";
	private static final String CHOICEEXPECTED = "CHOICE value was expected for type `{0}''";
	private static final String UNIONEXPECTED = "Union value was expected for type `{0}''";
	private static final String NONEXISTENTCHOICE = "Reference to a non-existent alternative `{0}'' in CHOICE value for type `{1}''";
	private static final String NONEXISTENTUNION = "Reference to a non-existent field `{0}'' in union value for type `{1}''";

	private CompFieldMap compFieldMap;
	private final ObjectClass_Definition objectClass;
	private final Identifier fieldName;

	private TableConstraint myTableConstraint;

	public Open_Type(final ObjectClass_Definition objectClass, final Identifier identifier) {
		compFieldMap = new CompFieldMap();
		this.objectClass = objectClass;
		fieldName = identifier;

		compFieldMap.setMyType(this);
		compFieldMap.setFullNameParent(this);
		if (null != objectClass) {
			objectClass.setFullNameParent(this);
		}

		isErroneous = false;
	}

	@Override
	/** {@inheritDoc} */
	public Type_type getTypetype() {
		return Type_type.TYPE_OPENTYPE;
	}

	@Override
	/** {@inheritDoc} */
	public IASN1Type newInstance() {
		return new Open_Type(objectClass, fieldName);
	}

	@Override
	/** {@inheritDoc} */
	public Type_type getTypetypeTtcn3() {
		return Type_type.TYPE_TTCN3_CHOICE;
	}

	@Override
	/** {@inheritDoc} */
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		if (null != compFieldMap) {
			compFieldMap.setMyScope(scope);
		}
	}

	public ObjectClass_Definition getMyObjectClass() {
		return objectClass;
	}

	public Identifier getObjectClassFieldName() {
		return fieldName;
	}

	public void setMyTableConstraint(final TableConstraint constraint) {
		myTableConstraint = constraint;
	}

	//Stores the possible name->type mappings
	//This gives a weak checking
	public void addComponent(final CompField field) {
		if (null != field && null != compFieldMap) {
			compFieldMap.addComp(field);
			lastTimeChecked = null;
		}
	}

	//don't use it before get
	public boolean hasComponentWithName(final Identifier identifier) {
		return  null != getComponentByName(identifier);
	}

	public CompField getComponentByName(final Identifier identifier) {
		//convert the first letter to upper case:
		String name = identifier.getName();
		name = name.substring(0,1).toLowerCase()+name.substring(1);
		if (null != compFieldMap) {
			return compFieldMap.getCompWithName(name);
		}
		return null;
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

		return this == otherType;
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
	public String getTypename() {
		return "open type";
	}

	@Override
	/** {@inheritDoc} */
	public String getFullName() {
		if (null == getNameParent()) {
			return getTypename();
		}

		return super.getFullName();
	}

	@Override
	/** {@inheritDoc} */
	public String getOutlineIcon() {
		return "asn1_opentype.gif";
	}

	@Override
	/** {@inheritDoc} */
	public void checkRecursions(final CompilationTimeStamp timestamp, final IReferenceChain referenceChain) {
		final Map<String, CompField> map = compFieldMap.getComponentFieldMap(timestamp);

		if (referenceChain.add(this) && 1 == map.size()) {
			for (CompField compField : map.values()) {
				final IType type = compField.getType();
				if (null != type) {
					referenceChain.markState();
					type.checkRecursions(timestamp, referenceChain);
					referenceChain.previousState();
				}
			}
		}
	}

	public void clear() {
		lastTimeChecked = null;
		compFieldMap = new CompFieldMap();
		compFieldMap.setMyType(this);
		compFieldMap.setFullNameParent(this);
	}

	@Override
	/** {@inheritDoc} */
	public void check(final CompilationTimeStamp timestamp) {
		if (null != lastTimeChecked && !lastTimeChecked.isLess(timestamp)) {
			return;
		}
		compFieldMap.check(timestamp);
		if( compFieldMap.isEmpty()){
			return; //too early check
		}
		if (null != constraints) {
			constraints.check(timestamp);
		}
		lastTimeChecked = timestamp;
	}

	// FIXME add tests
	@Override
	/** {@inheritDoc} */
	public boolean checkThisValue(final CompilationTimeStamp timestamp, final IValue value, final Assignment lhs, final ValueCheckingOptions valueCheckingOptions) {
		if (getIsErroneous(timestamp)) {
			return false;
		}

		boolean selfReference = super.checkThisValue(timestamp, value, lhs, valueCheckingOptions);

		IValue last = value.getValueRefdLast(timestamp, valueCheckingOptions.expected_value, null);
		if (null == last || last.getIsErroneous(timestamp)) {
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

		this.check(timestamp);

		switch (last.getValuetype()) {
		case SEQUENCE_VALUE:
			if (value.isAsn()) {
				value.getLocation().reportSemanticError(MessageFormat.format(CHOICEEXPECTED, getFullName()));
				value.setIsErroneous(true);
			} else {
				last = last.setValuetype(timestamp, Value_type.CHOICE_VALUE);
				if (!last.getIsErroneous(timestamp)) {
					selfReference = checkThisValueChoice(timestamp, (Choice_Value) last, lhs, valueCheckingOptions.expected_value,
							valueCheckingOptions.incomplete_allowed, valueCheckingOptions.str_elem);
				}
			}
			break;
		case CHOICE_VALUE:
			selfReference = checkThisValueChoice(timestamp, (Choice_Value) last, lhs, valueCheckingOptions.expected_value,
					valueCheckingOptions.incomplete_allowed, valueCheckingOptions.str_elem);
			break;
		default:
			if (value.isAsn()) {
				value.getLocation().reportSemanticError(MessageFormat.format(CHOICEEXPECTED, getFullName()));
			} else {
				value.getLocation().reportSemanticError(MessageFormat.format(UNIONEXPECTED, getFullName()));
			}
			value.setIsErroneous(true);
		}

		value.setLastTimeChecked(timestamp);

		return selfReference;
	}

	private boolean checkThisValueChoice(final CompilationTimeStamp timestamp, final Choice_Value value, final Assignment lhs, final Expected_Value_type expectedValue,
			final boolean incompleteAllowed, final boolean strElem) {
		boolean selfReference = false;

		final Identifier name = value.getName();
		final CompField field = getComponentByName(name);
		if (field == null) {
			if (value.isAsn()) {
				value.getLocation().reportSemanticError(
						MessageFormat.format(NONEXISTENTCHOICE, name.getDisplayName(), getFullName()));
			} else {
				value.getLocation().reportSemanticError(MessageFormat.format(NONEXISTENTUNION, name.getDisplayName(), getFullName()));
			}
		} else {
			IValue alternativeValue = value.getValue();
			if (null == alternativeValue) {
				return selfReference;
			}
			final Type alternativeType = field.getType();
			alternativeValue.setMyGovernor(alternativeType);
			alternativeValue = alternativeType.checkThisValueRef(timestamp, alternativeValue);
			selfReference = alternativeType.checkThisValue(timestamp, alternativeValue, lhs, new ValueCheckingOptions(expectedValue, incompleteAllowed,
					false, true, false, strElem));
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

				final CompField field = getComponentByName(name);
				if (field == null) {
					namedTemplate.getLocation().reportSemanticError(MessageFormat.format(REFERENCETONONEXISTENTFIELD,
							name.getDisplayName(),
							getFullName()));
				} else {
					final Type fieldType = field.getType();
					if (fieldType != null && !fieldType.getIsErroneous(timestamp)) {
						ITTCN3Template namedTemplateTemplate = namedTemplate.getTemplate();

						namedTemplateTemplate.setMyGovernor(fieldType);
						namedTemplateTemplate = fieldType.checkThisTemplateRef(timestamp, namedTemplateTemplate);
						final Completeness_type completeness = namedTemplateList.getCompletenessConditionChoice(timestamp,
								isModified, name);
						selfReference = namedTemplateTemplate.checkThisTemplateGeneric(timestamp, fieldType,
								Completeness_type.MAY_INCOMPLETE.equals(completeness), false, false, true,
								implicitOmit, lhs);
					}
				}
			}
		} else {
			template.getLocation().reportSemanticError(
					MessageFormat.format(TEMPLATENOTALLOWED, template.getTemplateTypeName(), getTypename()));
		}

		if (template.getLengthRestriction() != null) {
			template.getLocation().reportSemanticError(MessageFormat.format(LENGTHRESTRICTIONNOTALLOWED, getTypename()));
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

		final ISubReference subreference = subreferences.get(actualSubReference);
		switch (subreference.getReferenceType()) {
		case arraySubReference:
			subreference.getLocation().reportSemanticError(MessageFormat.format(ArraySubReference.INVALIDSUBREFERENCE, getTypename()));
			return null;
		case fieldSubReference:
			check(timestamp);
			final Identifier id = subreference.getId();
			final CompField compField = getComponentByName(id);
			if (compField == null) {
				if (compFieldMap.getComponentFieldMap(timestamp).isEmpty()) {
					return null; //too early analysis
				}
				reference.getLocation().reportSemanticError(MessageFormat.format(FieldSubReference.NONEXISTENTSUBREFERENCE,
						id.getDisplayName(), getFullName()));
				reference.setIsErroneous(true);
				return this;
			}

			if (interruptIfOptional && compField.isOptional()) {
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
	public StringBuilder getProposalDescription(final StringBuilder builder) {
		return builder.append("open type");
	}

	/**
	 * Searches and adds a completion proposal to the provided collector if
	 * a valid one is found.
	 * <p>
	 * In case of structural types, the member fields are checked if they
	 * could complete the proposal.
	 *
	 * @param propCollector
	 *                the proposal collector to add the proposal to, and
	 *                used to get more information
	 * @param i
	 *                index, used to identify which element of the reference
	 *                (used by the proposal collector) should be checked for
	 *                completions.
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
				final CompField compField = getComponentByName(subreference.getId());
				if (compField == null) {
					return;
				}

				final IType type = compField.getType();
				if (type != null) {
					type.addProposal(propCollector, i + 1);
				}
			} else {
				// final part of the reference
				final List<CompField> compFields = compFieldMap.getComponentsWithPrefix(subreference.getId().getName());
				for (CompField compField : compFields) {
					final String proposalKind = compField.getType().getProposalDescription(new StringBuilder()).toString();
					propCollector.addProposal(compField.getIdentifier(), " - " + proposalKind,
							ImageCache.getImage(compField.getOutlineIcon()), proposalKind);
				}
			}
		}
	}

	/**
	 * Searches and adds a declaration proposal to the provided collector if
	 * a valid one is found.
	 * <p>
	 * In case of structural types, the member fields are checked if they
	 * could be the declaration searched for.
	 *
	 * @param declarationCollector
	 *                the declaration collector to add the declaration to,
	 *                and used to get more information.
	 * @param i
	 *                index, used to identify which element of the reference
	 *                (used by the declaration collector) should be checked.
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
				final CompField compField = getComponentByName(subreference.getId());
				if (compField == null) {
					return;
				}

				final IType type = compField.getType();
				if (type != null) {
					type.addDeclaration(declarationCollector, i + 1);
				}
			} else {
				// final part of the reference
				final List<CompField> compFields = compFieldMap.getComponentsWithPrefix(subreference.getId().getName());
				for (CompField compField : compFields) {
					declarationCollector.addDeclaration(compField.getIdentifier().getDisplayName(), compField.getIdentifier()
							.getLocation(), this);
				}
			}
		}
	}

	@Override
	/** {@inheritDoc} */
	public void getEnclosingField(final int offset, final ReferenceFinder rf) {
		if (compFieldMap == null) {
			return;
		}

		compFieldMap.getEnclosingField(offset, rf);
	}

	@Override
	/** {@inheritDoc} */
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		super.findReferences(referenceFinder, foundIdentifiers);
		if (compFieldMap != null) {
			compFieldMap.findReferences(referenceFinder, foundIdentifiers);
		}
		if (objectClass != null) {
			// TODO
		}
		if (myTableConstraint != null) {
			// TODO
		}
	}

	@Override
	/** {@inheritDoc} */
	protected boolean memberAccept(final ASTVisitor v) {
		if (!super.memberAccept(v)) {
			return false;
		}
		if (compFieldMap != null && !compFieldMap.accept(v)) {
			return false;
		}
		if (objectClass != null && !objectClass.accept(v)) {
			return false;
		}
		if (fieldName != null && !fieldName.accept(v)) {
			return false;
		}
		if (myTableConstraint != null && !myTableConstraint.accept(v)) {
			return false;
		}
		return true;
	}

	@Override
	/** {@inheritDoc} */
	public String getGenNameValue(final JavaGenData aData, final StringBuilder source, final Scope scope) {
		return getGenNameOwn(scope);
	}

	@Override
	/** {@inheritDoc} */
	public void generateCode( final JavaGenData aData, final StringBuilder source ) {
		final String genName = getGenNameOwn();
		final String displayName = getFullName();

		final List<FieldInfo> fieldInfos =  new ArrayList<FieldInfo>();
		boolean hasOptional = false;
		final Map<String, CompField> map = compFieldMap.getComponentFieldMap(CompilationTimeStamp.getBaseTimestamp());
		for ( final CompField compField : map.values() ) {
			final FieldInfo fi = new FieldInfo(compField.getType().getGenNameValue( aData, source, getMyScope() ),
					compField.getType().getGenNameTemplate(aData, source, getMyScope()),
					compField.getIdentifier().getName());
			hasOptional |= compField.isOptional();
			fieldInfos.add( fi );
		}

		for ( final CompField compField : map.values() ) {
			StringBuilder tempSource = aData.getCodeForType(compField.getType().getGenNameOwn());
			compField.getType().generateCode(aData, tempSource);
		}

		UnionGenerator.generateValueClass(aData, source, genName, displayName, fieldInfos, hasOptional);
		UnionGenerator.generateTemplateClass(aData, source, genName, displayName, fieldInfos, hasOptional);
		//TODO: implement
		source.append( "\t\t//TODO: Open_Type.generateCode() is not fully implemented!\n" );
	}

	@Override
	/** {@inheritDoc} */
	public boolean isPresentAnyvalueEmbeddedField(final ExpressionStruct expression, final List<ISubReference> subreferences, final int beginIndex) {
		return false;
	}

	@Override
	/** {@inheritDoc} */
	public void generateCodeIspresentBound(final JavaGenData aData, final ExpressionStruct expression, final List<ISubReference> subreferences,
			final int subReferenceIndex, final String globalId, final String externalId, final boolean isTemplate, final boolean isBound) {
		if (subreferences == null || getIsErroneous(CompilationTimeStamp.getBaseTimestamp())) {
			return;
		}

		if (subReferenceIndex >= subreferences.size()) {
			return;
		}

		StringBuilder closingBrackets = new StringBuilder();
		if(isTemplate) {
			boolean anyvalueReturnValue = true;
			if (!isBound) {
				anyvalueReturnValue = isPresentAnyvalueEmbeddedField(expression, subreferences, subReferenceIndex);
			}

			expression.expression.append(MessageFormat.format("if({0}) '{'\n", globalId));
			expression.expression.append(MessageFormat.format("switch({0}.getSelection()) '{'\n", externalId));
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

		ISubReference subReference = subreferences.get(subReferenceIndex);
		if (!(subReference instanceof FieldSubReference)) {
			ErrorReporter.INTERNAL_ERROR("Code generator reached erroneous type reference `" + getFullName() + "''");
			expression.expression.append("FATAL_ERROR encountered");
			return;
		}

		Identifier fieldId = ((FieldSubReference) subReference).getId();
		expression.expression.append(MessageFormat.format("if({0}) '{'\n", globalId));
		expression.expression.append(MessageFormat.format("{0} = {1}.isChosen({2}.union_selection_type.ALT_{3});\n", globalId, externalId, getGenNameValue(aData, expression.expression, myScope), FieldSubReference.getJavaGetterName( fieldId.getName())));
		expression.expression.append("}\n");

		CompField compField = getComponentByName(fieldId);
		Type nextType = compField.getType();

		expression.expression.append(MessageFormat.format("if({0}) '{'\n", globalId));
		closingBrackets.insert(0, "}\n");

		String temporalId = aData.getTemporaryVariableName();
		String temporalId2 = aData.getTemporaryVariableName();
		expression.expression.append(MessageFormat.format("{0}{1} {2} = {3};\n", getGenNameValue(aData, expression.expression, myScope), isTemplate?"_template":"", temporalId, externalId));
		expression.expression.append(MessageFormat.format("{0}{1} {2} = {3}.constGet{4}();\n", nextType.getGenNameValue(aData, expression.expression, myScope), isTemplate?"_template":"", temporalId2, temporalId, FieldSubReference.getJavaGetterName( fieldId.getName())));
		//FIXME handle omit_in_value_list
		expression.expression.append(MessageFormat.format("{0} = {1}.{2}({3});\n", globalId, temporalId2, isBound|| (subReferenceIndex!=subreferences.size()-1)?"isBound":"isPresent", (!(isBound || (subReferenceIndex!=subreferences.size()-1)) && isTemplate)?"true":""));

		nextType.generateCodeIspresentBound(aData, expression, subreferences, subReferenceIndex + 1, globalId, temporalId2, isTemplate, isBound);

		expression.expression.append(closingBrackets);
	}
}
