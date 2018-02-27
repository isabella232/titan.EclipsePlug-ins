/******************************************************************************
 * Copyright (c) 2000-2017 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.types;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.ArraySubReference;
import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.FieldSubReference;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.ISubReference;
import org.eclipse.titan.designer.AST.ISubReference.Subreference_type;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.ITypeWithComponents;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.IValue.Value_type;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.ParameterisedSubReference;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.Type;
import org.eclipse.titan.designer.AST.TypeCompatibilityInfo;
import org.eclipse.titan.designer.AST.Value;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.attributes.RawAST;
import org.eclipse.titan.designer.AST.TTCN3.templates.ITTCN3Template;
import org.eclipse.titan.designer.AST.TTCN3.templates.ITTCN3Template.Template_type;
import org.eclipse.titan.designer.AST.TTCN3.types.EnumeratedGenerator.Enum_Defs;
import org.eclipse.titan.designer.AST.TTCN3.types.subtypes.SubType;
import org.eclipse.titan.designer.AST.TTCN3.values.Integer_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Undefined_LowerIdentifier_Value;
import org.eclipse.titan.designer.compiler.JavaGenData;
import org.eclipse.titan.designer.editors.ProposalCollector;
import org.eclipse.titan.designer.editors.actions.DeclarationCollector;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * @author Kristof Szabados
 * */
public final class TTCN3_Enumerated_Type extends Type implements ITypeWithComponents {
	public static final String DUPLICATEENUMERATIONIDENTIFIERFIRST = "Duplicate enumeration identifier `{0}'' was first declared here";
	public static final String DUPLICATEENUMERATIONIDENTIFIERREPEATED = "Duplicate enumeration identifier `{0}'' was declared here again";
	public static final String DUPLICATEDENUMERATIONVALUEFIRST = "Value {0} is already assigned to `{1}''";
	public static final String DUPLICATEDENUMERATIONVALUEREPEATED = "Duplicate numeric value {0} for enumeration `{1}''";
	private static final String TTCN3ENUMERATEDVALUEEXPECTED = "Enumerated value was expected";
	private static final String ASN1ENUMERATEDVALUEEXPECTED = "ENUMERATED value was expected";
	private static final String TEMPLATENOTALLOWED = "{0} cannot be used for enumerated type";
	private static final String LENGTHRESTRICTIONNOTALLOWED = "Length restriction is not allowed for enumerated type";
	private static final String LARGEINTEGERERROR = "Using a large integer value ({0}) as an ENUMERATED/enumerated value is not supported";
	private final EnumerationItems items;


	// minor cache
	private Map<String, EnumItem> nameMap;

	public TTCN3_Enumerated_Type(final EnumerationItems items) {
		this.items = items;

		if (items != null) {
			items.setFullNameParent(this);
		}
	}

	@Override
	/** {@inheritDoc} */
	public Type_type getTypetype() {
		return Type_type.TYPE_TTCN3_ENUMERATED;
	}

	@Override
	/** {@inheritDoc} */
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		if (items != null) {
			items.setMyScope(scope);
		}
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

		return this == temp;
	}

	@Override
	/** {@inheritDoc} */
	public boolean isIdentical(final CompilationTimeStamp timestamp, final IType type) {
		return isCompatible(timestamp, type, null, null, null);
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
		return "enumeration.gif";
	}

	/**
	 * Check if an enumeration item exists with the provided name.
	 *
	 * @param identifier the name to look for
	 *
	 * @return true it there is an item with that name, false otherwise.
	 * */
	public boolean hasEnumItemWithName(final Identifier identifier) {
		if (lastTimeChecked == null) {
			check(CompilationTimeStamp.getBaseTimestamp());
		}

		return nameMap.containsKey(identifier.getName());
	}

	/**
	 * Returns an enumeration item with the provided name.
	 *
	 * @param identifier the name to look for
	 *
	 * @return the enumeration item with the provided name, or null.
	 * */
	public EnumItem getEnumItemWithName(final Identifier identifier) {
		if (lastTimeChecked == null) {
			check(CompilationTimeStamp.getBaseTimestamp());
		}

		return nameMap.get(identifier.getName());
	}

	/**
	 * Does the semantic checking of the enumerations.
	 *
	 * @param timestamp the timestamp of the actual semantic check cycle.
	 * */
	@Override
	/** {@inheritDoc} */
	public void check(final CompilationTimeStamp timestamp) {
		if (lastTimeChecked != null && !lastTimeChecked.isLess(timestamp)) {
			return;
		}

		parseAttributes(timestamp);

		nameMap = new HashMap<String, EnumItem>(items.getItems().size());
		final Map<Long, EnumItem> valueMap = new HashMap<Long, EnumItem>(items.getItems().size());

		final List<EnumItem> enumItems = items.getItems();
		// check duplicated names and values
		for (int i = 0, size = enumItems.size(); i < size; i++) {
			final EnumItem item = enumItems.get(i);
			final Identifier id = item.getId();
			final String fieldName = id.getName();
			if (nameMap.containsKey(fieldName)) {
				nameMap.get(fieldName).getId().getLocation().reportSingularSemanticError(
						MessageFormat.format(DUPLICATEENUMERATIONIDENTIFIERFIRST, id.getDisplayName()));
				id.getLocation().reportSemanticError(MessageFormat.format(DUPLICATEENUMERATIONIDENTIFIERREPEATED, id.getDisplayName()));
			} else {
				nameMap.put(fieldName, item);
			}

			final Value value = item.getValue();
			if (value != null && item.isOriginal()) {
				if (value.getIsErroneous(timestamp) || !Value_type.INTEGER_VALUE.equals(value.getValuetype())) {
					value.getLocation().reportSemanticError(MessageFormat.format("INTEGER value was expected for enumeration `{0}''", id.getDisplayName()));
					setIsErroneous(true);
				} else {
					final Integer_Value enumValue = (Integer_Value) value;
					if (!enumValue.isNative()) {
						enumValue.getLocation().reportSemanticError(MessageFormat.format(LARGEINTEGERERROR, enumValue.getValueValue()));
						setIsErroneous(true);
					} else {
						final Long enumLong = enumValue.getValue();
						if (valueMap.containsKey(enumLong)) {
							valueMap.get(enumLong).getLocation().reportSingularSemanticError(
									MessageFormat.format(DUPLICATEDENUMERATIONVALUEFIRST, enumLong, valueMap.get(enumLong).getId().getDisplayName()));
							value.getLocation().reportSemanticError(
									MessageFormat.format(DUPLICATEDENUMERATIONVALUEREPEATED, enumLong, id.getDisplayName()));
							setIsErroneous(true);
						} else {
							valueMap.put(enumLong, item);
						}
					}
				}
			}
		}

		// Assign default values
		if (!getIsErroneous(timestamp) && lastTimeChecked == null) {
			Long firstUnused = Long.valueOf(0);
			while (valueMap.containsKey(firstUnused)) {
				firstUnused++;
			}

			for (int i = 0, size = enumItems.size(); i < size; i++) {
				final EnumItem item = enumItems.get(i);
				if (!item.isOriginal()) {
					//optimization: if the same value was already assigned, there is no need to create it again.
					final IValue value = item.getValue();
					if (value == null || ((Integer_Value) value).getValue() != firstUnused) {
						final Integer_Value tempValue = new Integer_Value(firstUnused.longValue());
						tempValue.setLocation(item.getLocation());
						item.setValue(tempValue);
					}

					valueMap.put(firstUnused, item);
					firstUnused = Long.valueOf(firstUnused.longValue() + 1);

					while (valueMap.containsKey(firstUnused)) {
						firstUnused++;
					}
				}
			}
		}

		valueMap.clear();

		lastTimeChecked = timestamp;

		checkSubtypeRestrictions(timestamp);
	}

	@Override
	/** {@inheritDoc} */
	public SubType.SubType_type getSubtypeType() {
		return SubType.SubType_type.ST_ENUM;
	}

	@Override
	/** {@inheritDoc} */
	public IValue checkThisValueRef(final CompilationTimeStamp timestamp, final IValue value) {
		if (Value_type.UNDEFINED_LOWERIDENTIFIER_VALUE.equals(value.getValuetype())) {
			if (hasEnumItemWithName(((Undefined_LowerIdentifier_Value) value).getIdentifier())) {
				final IValue temp = value.setValuetype(timestamp, Value_type.ENUMERATED_VALUE);
				temp.setMyGovernor(this);
				return temp;
			}
		}

		return super.checkThisValueRef(timestamp, value);
	}

	@Override
	/** {@inheritDoc} */
	public boolean checkThisValue(final CompilationTimeStamp timestamp, final IValue value, final Assignment lhs, final ValueCheckingOptions valueCheckingOptions) {
		if (getIsErroneous(timestamp)) {
			return false;
		}

		final boolean selfReference = super.checkThisValue(timestamp, value, lhs,  valueCheckingOptions);

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
		case ENUMERATED_VALUE:
			// if it is an enumerated value, then it was already checked to be categorized.
			break;
		case EXPRESSION_VALUE:
		case MACRO_VALUE:
			break;
		default:
			value.getLocation().reportSemanticError(value.isAsn() ? ASN1ENUMERATEDVALUEEXPECTED : TTCN3ENUMERATEDVALUEEXPECTED);
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
		template.setMyGovernor(this);

		if (!Template_type.SPECIFIC_VALUE.equals(template.getTemplatetype()) ) {
			template.getLocation().reportSemanticError(MessageFormat.format(TEMPLATENOTALLOWED, template.getTemplateTypeName()));
			template.setIsErroneous(true);
		}
		if (template.getLengthRestriction() != null) {
			template.getLocation().reportSemanticError(LENGTHRESTRICTIONNOTALLOWED);
			template.setIsErroneous(true);
		}

		return false;
	}

	@Override
	/** {@inheritDoc} */
	public void checkCodingAttributes(final CompilationTimeStamp timestamp) {
		//check raw attributes
		if (subType != null) {
			int restrictionLength = subType.get_length_restriction();
			if (restrictionLength != -1) {
				if (rawAttribute == null) {
					rawAttribute = new RawAST(getDefaultRawFieldLength());
				}

				rawAttribute.length_restriction = restrictionLength;
			}
		}
		//TODO add checks for other encodings.
	}

	@Override
	/** {@inheritDoc} */
	public boolean canHaveCoding(final MessageEncoding_type coding, final IReferenceChain refChain) {
		if (coding == MessageEncoding_type.BER) {
			return hasEncoding(MessageEncoding_type.BER, null);
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
	public StringBuilder getProposalDescription(final StringBuilder builder) {
		return builder.append("enumerated");
	}

	/**
	 * Searches and adds a completion proposal to the provided collector if a
	 * valid one is found.
	 * <p>
	 * The enumerated elements are checked if they can complete the provided
	 * proposal.
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
		if (subreferences.size() != 1 || propCollector.getReference().getModuleIdentifier() != null) {
			return;
		}

		final ISubReference subreference = subreferences.get(i);
		if (Subreference_type.fieldSubReference.equals(subreference.getReferenceType()) && items != null) {
			items.addProposal(propCollector);
		}
	}

	/**
	 * Searches and adds a declaration proposal to the provided collector if a
	 * valid one is found.
	 * <p>
	 * The enumerated elements are checked if they can be the declaration
	 * searched for.
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
		if (i != 0 || subreferences.size() != 1 || declarationCollector.getReference().getModuleIdentifier() != null) {
			return;
		}

		final ISubReference subreference = subreferences.get(i);
		if (Subreference_type.fieldSubReference.equals(subreference.getReferenceType()) && items != null) {
			items.addDeclaration(declarationCollector, i);
		}
	}

	public void addDeclaration(final DeclarationCollector declarationCollector, final int i, final Location commentLocation) {
		final List<ISubReference> subreferences = declarationCollector.getReference().getSubreferences();
		if (i != 0 || subreferences.size() != 1 || declarationCollector.getReference().getModuleIdentifier() != null) {
			return;
		}

		final ISubReference subreference = subreferences.get(i);
		if (Subreference_type.fieldSubReference.equals(subreference.getReferenceType()) && items != null) {

			if (commentLocation != null) {
				items.addDeclaration(declarationCollector, i, commentLocation);
			}
		}
	}

	@Override
	/** {@inheritDoc} */
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			lastTimeChecked = null;
			boolean handled = false;
			if (items != null) {
				if (reparser.envelopsDamage(items.getLocation())) {
					items.updateSyntax(reparser, true);
					reparser.updateLocation(items.getLocation());
					handled = true;
				}
			}

			if (subType != null) {
				subType.updateSyntax(reparser, false);
				handled = true;
			}

			if (handled) {
				return;
			}

			throw new ReParseException();
		}

		if (items != null) {
			items.updateSyntax(reparser, false);
			reparser.updateLocation(items.getLocation());
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
		if (items == null) {
			return;
		}

		for (final EnumItem enumItem : items.getItems()) {
			if (enumItem.getLocation().containsOffset(offset)) {
				rf.type = this;
				rf.fieldId = enumItem.getId();
				return;
			}
		}
	}

	@Override
	/** {@inheritDoc} */
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		super.findReferences(referenceFinder, foundIdentifiers);
		if (items != null) {
			items.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	@Override
	/** {@inheritDoc} */
	protected boolean memberAccept(final ASTVisitor v) {
		if (!super.memberAccept(v)) {
			return false;
		}
		if (items!=null && !items.accept(v)) {
			return false;
		}
		return true;
	}

	@Override
	/** {@inheritDoc} */
	public Identifier getComponentIdentifierByName(final Identifier identifier) {
		final EnumItem enumItem = getEnumItemWithName(identifier);
		return enumItem == null ? null : enumItem.getId();
	}

	/**
	 * Add generated java code on this level.
	 * @param aData only used to update imports if needed
	 * @param source the source code generated
	 */
	@Override
	/** {@inheritDoc} */
	public void generateCode( final JavaGenData aData, final StringBuilder source ) {
		final String ownName = getGenNameOwn();
		final String displayName = getFullName();

		generateCodeTypedescriptor(aData, source);

		final Enum_Defs e_defs = new Enum_Defs( items.getItems(), ownName, displayName, getGenNameTemplate(aData, source, myScope));
		EnumeratedGenerator.generateValueClass( aData, source, e_defs );
		EnumeratedGenerator.generateTemplateClass( aData, source, e_defs);

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
	public String getGenNameValue(final JavaGenData aData, final StringBuilder source, final Scope scope) {
		//TODO: ???
		return getGenNameOwn(scope);
	}

	@Override
	/** {@inheritDoc} */
	public String getGenNameTemplate(final JavaGenData aData, final StringBuilder source, final Scope scope) {
		return  getGenNameOwn(scope).concat("_template");
	}

	@Override
	/** {@inheritDoc} */
	public String getGenNameRawDescriptor(final JavaGenData aData, final StringBuilder source) {
		generateCodeRawDescriptor(aData, source);

		return getGenNameOwn(myScope) + "_raw_";
	}
}
