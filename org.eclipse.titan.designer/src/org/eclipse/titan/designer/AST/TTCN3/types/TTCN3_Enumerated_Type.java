/******************************************************************************
 * Copyright (c) 2000-2017 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.types;

import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.ArraySubReference;
import org.eclipse.titan.designer.AST.FieldSubReference;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.ISubReference;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.ITypeWithComponents;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.ParameterisedSubReference;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.Type;
import org.eclipse.titan.designer.AST.TypeCompatibilityInfo;
import org.eclipse.titan.designer.AST.Value;
import org.eclipse.titan.designer.AST.ISubReference.Subreference_type;
import org.eclipse.titan.designer.AST.IValue.Value_type;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.templates.ITTCN3Template;
import org.eclipse.titan.designer.AST.TTCN3.templates.ITTCN3Template.Template_type;
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
	
	private static final String UNKNOWN_VALUE = "UNKNOWN_VALUE";
	private static final String UNBOUND_VALUE ="UNBOUND_VALUE";
	private final EnumerationItems items;
	private Long firstUnused = -1L;  //first unused value for thsi enum type
	private Long secondUnused = -1L; //second unused value for thsi enum type

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
	public void checkThisValue(final CompilationTimeStamp timestamp, final IValue value, final ValueCheckingOptions valueCheckingOptions) {
		if (getIsErroneous(timestamp)) {
			return;
		}

		super.checkThisValue(timestamp, value, valueCheckingOptions);

		final IValue last = value.getValueRefdLast(timestamp, valueCheckingOptions.expected_value, null);
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
	}

	@Override
	/** {@inheritDoc} */
	public void checkThisTemplate(final CompilationTimeStamp timestamp, final ITTCN3Template template,
			final boolean isModified, final boolean implicitOmit) {
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

		for (EnumItem enumItem : items.getItems()) {
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
	public Identifier getComponentIdentifierByName(final Identifier identifier) {
		final EnumItem enumItem = getEnumItemWithName(identifier);
		return enumItem == null ? null : enumItem.getId();
	}

	//=== Code generation ===
	
	private void generateValueClass(final JavaGenData aData, final StringBuilder source, final String ownName) {
		if(needsAlias()) {
			source.append(MessageFormat.format("\tpublic static class {0} extends Base_Type '{' \n", ownName));
			//== enum_type ==
			source.append("\t\tpublic enum enum_type {\n");
			
			DecimalFormat formatter = new DecimalFormat("#");
			int size = items.getItems().size();
			EnumItem item = null;
			for( int i=0; i<size; i++){
				item = items.getItems().get(i);
				source.append(MessageFormat.format("\t\t\t{0}", item.getId().getTtcnName()));
				if(item.getValue() instanceof Integer_Value) {
					String valueWithoutCommas = formatter.format( ((Integer_Value) item.getValue()).getValue());
					source.append(MessageFormat.format("({0}),\n", valueWithoutCommas));
				} else {
					//TODO: impossible ?
				}
			};
			calculateFirstAndSecondUnusedValues();
			source.append(MessageFormat.format("\t\t\t{0}({1}),\n",UNKNOWN_VALUE,firstUnused));
			source.append(MessageFormat.format("\t\t\t{0}({1});\n",UNBOUND_VALUE,secondUnused));
			source.append("\n\t\t\tprivate int enum_num;\n");
			//== constructors for enum_type ==
			
			source.append("\t\t\tenum_type(int num) {\n");
			source.append("\t\t\t\tthis.enum_num = num;\n");
			source.append("\t\t\t}\n");
			
			source.append("\t\t\tprivate int getInt(){\n");
			source.append("\t\t\t\treturn enum_num;\n");
			source.append("\t\t\t}\n");
			
			source.append("\t\t}\n");
			// end of enum_type
			
			//== enum_value ==
			source.append("\t\tpublic enum_type enum_value;\n");
			
			//== Constructors ==
			source.append("\t\t//===Constructors===;\n");
			source.append(MessageFormat.format("\t\t{0}()'{'\n",ownName));
			source.append(MessageFormat.format("\t\t\tenum_value = enum_type.{0};\n", UNBOUND_VALUE));
			source.append("\t\t};\n");
			
//TODO: arg int
//			source.append(MessageFormat.format("\t\t{0}(int other_value)'{'\n",ownName));
//			source.append(MessageFormat.format("\t\t\tenum_value = enum_type.{0};\n", UNBOUND_VALUE));
//			source.append("\t\t};\n");
			
			//empty
			source.append(MessageFormat.format("\t\t{0}( {0} other_value)'{'\n", ownName));
			source.append(MessageFormat.format("\t\t\tenum_value = other_value.enum_value;\n",ownName));
			source.append("\t\t};\n");
			
			source.append(MessageFormat.format("\t\t{0}( {0}.enum_type other_value )'{'\n", ownName));
			source.append("\t\t\tenum_value = other_value;\n");
			source.append("\t\t};\n");

			//== functions ==
			source.append("\t\t//===Methods===;\n");
			//TODO: enum2int
			generateValueIsPresent(source);
			generateValueIsBound(source);
			generateMustBound(source,ownName);
			generateValueOperatorEquals(source, ownName);
			generateValueAssign(source, ownName); 
			source.append("\t}\n");
		}
	}
	
	private void generateTemplateClass(final JavaGenData aData, final StringBuilder source, final String ownName){
		source.append(MessageFormat.format("\tpublic static class {0}_template extends Base_Template '{'\n", ownName, getGenNameTemplate(aData, source, myScope)));
		
		//TODO: generate this, and others:
		generateTemplateSetType(source);
		source.append("\t}\n");
	}
	
	//===

	private void generateValueIsPresent(final StringBuilder source){
		source.append("\t\tpublic boolean isPresent(){ return isBound(); }\n");
	}

	private void generateValueIsBound(final StringBuilder source){
		source.append("\t\tpublic boolean isBound(){\n");
		source.append("\t\t\treturn (enum_value != enum_type.UNBOUND_VALUE);\n");
		source.append("\t\t}\n");
	}

	private void generateValueOperatorEquals(final StringBuilder source,final String ownName) {
		//Arg type: own type
		
		source.append(MessageFormat.format("\t\tpublic TitanBoolean operatorEquals(final {0} other_value)'{'\n", ownName));
		source.append(MessageFormat.format("\t\t\t\treturn (new TitanBoolean( enum_value == other_value.enum_value));\n", ownName)); 
		source.append("\t\t}\n");
		
		//Arg: Base_Type
		source.append("\t\tpublic TitanBoolean operatorEquals(final Base_Type other_value){\n");
		source.append(MessageFormat.format("\t\t\tif( other_value instanceof {0} ) '{'\n", ownName));
		source.append(MessageFormat.format("\t\t\t\treturn this.operatorEquals( ({0}) other_value);\n", ownName)); 
		source.append("\t\t\t} else {\n");
		source.append("\t\t\t//TODO:TtcnError message\n");
		source.append("\t\t\treturn (new TitanBoolean(false));\n");
		source.append("\t\t\t}\n");
		source.append("\t\t}\n");
		
		//Arg: enum_type
		source.append(MessageFormat.format("\t\tpublic TitanBoolean operatorEquals(final {0}.enum_type other_value)'{'\n",ownName));
		source.append(MessageFormat.format("\t\t\t\treturn (new TitanBoolean( enum_value == other_value));\n", ownName)); 
		source.append("\t\t}\n");
	}

	private void generateMustBound(final StringBuilder source, final String ownName) {
		source.append("\t\tpublic void mustBound( String errorMessage) {\n");
		source.append("\t\t\tif( !isBound() ) {\n");
		source.append("\t\t\t\tthrow new TtcnError( errorMessage );\n");
		source.append("\t\t\t};\n");
		source.append("\t\t};\n");
		
	}
	private void generateValueAssign(final StringBuilder source, final String ownName) {
		//Arg type: own type
		source.append(MessageFormat.format("\t\tpublic {0} assign(final {0} other_value)'{'\n",ownName));
		source.append("\t\t\t\tother_value.mustBound(\"Assignment of an unbound enumerated value\");\n");
		source.append(MessageFormat.format("\t\t\t\tthis.enum_value = other_value.enum_value;\n", ownName));
		source.append("\t\t\treturn this;\n");
		source.append("\t\t}\n");
		
		//Arg: Base_Type
		source.append("\t\tpublic Base_Type assign(final Base_Type other_value){\n");
		source.append(MessageFormat.format("\t\t\tif( other_value instanceof {0} ) '{'\n", ownName));
		source.append(MessageFormat.format("\t\t\t\t return assign(({0}) other_value);\n", ownName));
		source.append("\t\t\t}\n");
		source.append(MessageFormat.format("\t\t\tthrow new TtcnError(MessageFormat.format(\"Internal Error: value `{0}'' can not be cast to {1}\", other_value));\n",ownName));
		source.append("\t\t}\n");
		//Arg: enum_type
		source.append(MessageFormat.format("\t\tpublic {0} assign(final {0}.enum_type other_value)'{'\n",ownName));
		source.append(MessageFormat.format("\t\t\treturn assign( new {0}(other_value) );\n",ownName));
		source.append("\t\t}\n");
	}
	
	private void generateTemplateSetType(final StringBuilder source){
		source.append("\t\tpublic void setType(template_sel valueList, int i) {\n");
		source.append("\t\t\t//TODO: setType is not implemented yet\n");
		source.append("\t\t}\n");
	}
	
	//This function supposes that the enum class is alredy checked and error free
	private void calculateFirstAndSecondUnusedValues() {
		if( firstUnused != -1 ) {
			return; //function already have been called
		}
		final Map<Long, EnumItem> valueMap = new HashMap<Long, EnumItem>(items.getItems().size());
		final List<EnumItem> enumItems = items.getItems();
		for( int i = 0, size = enumItems.size(); i < size; i++) {
			final EnumItem item = enumItems.get(i);
			valueMap.put( ((Integer_Value) item.getValue()).getValue(), item);
		}

		Long firstUnused = Long.valueOf(0);
		while (valueMap.containsKey(firstUnused)) {
			firstUnused++;
		}
		
		this.firstUnused = firstUnused;
		firstUnused++;
		while (valueMap.containsKey(firstUnused)) {
			firstUnused++;
		}
		secondUnused = firstUnused;
		valueMap.clear();
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
		aData.addBuiltinTypeImport( "Base_Type" );
		aData.addBuiltinTypeImport( "TitanBoolean" );
		aData.addBuiltinTypeImport( "Base_Template" );
		aData.addImport( "java.text.MessageFormat" );
		generateValueClass(aData,source,ownName);
		generateTemplateClass(aData, source,ownName);
	}

	@Override
	/** {@inheritDoc} */
	public String getGenNameValue(JavaGenData aData, final StringBuilder source, final Scope scope) {
		//TODO: ???
		return getGenNameOwn(scope);
	}

	@Override
	public String getGenNameTemplate(JavaGenData aData, StringBuilder source, Scope scope) {

		return  getGenNameOwn(scope).concat("_template");
	}
}
