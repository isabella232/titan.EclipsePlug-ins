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
import java.util.Set;

import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.ArraySubReference;
import org.eclipse.titan.designer.AST.FieldSubReference;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.IReferenceableElement;
import org.eclipse.titan.designer.AST.ISubReference;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.ITypeWithComponents;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.ParameterisedSubReference;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.Type;
import org.eclipse.titan.designer.AST.ISubReference.Subreference_type;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.attributes.MultipleWithAttributes;
import org.eclipse.titan.designer.AST.TTCN3.attributes.Qualifier;
import org.eclipse.titan.designer.AST.TTCN3.attributes.Qualifiers;
import org.eclipse.titan.designer.AST.TTCN3.attributes.SingleWithAttribute;
import org.eclipse.titan.designer.AST.TTCN3.attributes.WithAttributesPath;
import org.eclipse.titan.designer.AST.TTCN3.attributes.SingleWithAttribute.Attribute_Type;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.ExpressionStruct;
import org.eclipse.titan.designer.compiler.JavaGenData;
import org.eclipse.titan.designer.declarationsearch.Declaration;
import org.eclipse.titan.designer.editors.ProposalCollector;
import org.eclipse.titan.designer.editors.actions.DeclarationCollector;
import org.eclipse.titan.designer.graphics.ImageCache;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * @author Kristof Szabados
 * */
public abstract class TTCN3_Set_Seq_Choice_BaseType extends Type implements ITypeWithComponents, IReferenceableElement {
	protected CompFieldMap compFieldMap;

	private boolean componentInternal;

	public TTCN3_Set_Seq_Choice_BaseType(final CompFieldMap compFieldMap) {
		this.compFieldMap = compFieldMap;
		componentInternal = false;
		compFieldMap.setMyType(this);
		compFieldMap.setFullNameParent(this);
	}

	@Override
	public final void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		compFieldMap.setMyScope(scope);
	}

	/** @return the number of components */
	public final int getNofComponents() {
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
	public final CompField getComponentByIndex(final int index) {
		return compFieldMap.fields.get(index);
	}

	/**
	 * Returns whether a component with the name exists or not..
	 *
	 * @param name the name of the element to check
	 * @return true if there is an element with that name, false otherwise.
	 */
	public final boolean hasComponentWithName(final String name) {
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
	public final CompField getComponentByName(final String name) {
		if (compFieldMap.componentFieldMap == null) {
			return null;
		}

		return compFieldMap.componentFieldMap.get(name);
	}

	/**
	 * Returns the identifier of the element at the specified position.
	 *
	 * @param index index of the element to return
	 * @return the identifier of the element at the specified position in this
	 *         list
	 */
	public final Identifier getComponentIdentifierByIndex(final int index) {
		return compFieldMap.fields.get(index).getIdentifier();
	}

	@Override
	public final IType getFieldType(final CompilationTimeStamp timestamp, final Reference reference, final int actualSubReference,
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
						MessageFormat.format(FieldSubReference.NONEXISTENTSUBREFERENCE, ((FieldSubReference) subreference).getId().getDisplayName(),
								getTypename()));
				return null;
			}

			final IType fieldType = compField.getType();
			if (fieldType == null) {
				return null;
			}

			if (interruptIfOptional && compField.isOptional()) {
				return null;
			}

			final Expected_Value_type internalExpectation =
					expectedIndex == Expected_Value_type.EXPECTED_TEMPLATE ? Expected_Value_type.EXPECTED_DYNAMIC_VALUE : expectedIndex;
			//This is the recursive function call:
			return fieldType.getFieldType(timestamp, reference, actualSubReference + 1, internalExpectation, refChain, interruptIfOptional);
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
						MessageFormat.format(FieldSubReference.NONEXISTENTSUBREFERENCE, ((FieldSubReference) subreference).getId().getDisplayName(),
								getTypename()));
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
					MessageFormat.format(FieldSubReference.INVALIDSUBREFERENCE, ((ParameterisedSubReference) subreference).getId().getDisplayName(),
							getTypename()));
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
	public final boolean isComponentInternal(final CompilationTimeStamp timestamp) {
		check(timestamp);

		return componentInternal;
	}

	@Override
	/** {@inheritDoc} */
	public void parseAttributes(final CompilationTimeStamp timestamp) {
		checkDoneAttribute(timestamp);

		if (!hasVariantAttributes(timestamp)) {
			return;
		}

		/* This will be useful when processing of the variant attributes assigned to the whole type is implemented
		ArrayList<SingleWithAttribute> realAttributes = withAttributesPath.getRealAttributes(timestamp);
		for (int i = 0; i < realAttributes.size(); i++) {
			SingleWithAttribute tempSingle = realAttributes.get(i);
			if (Attribute_Type.Variant_Attribute.equals(tempSingle.getAttributeType())
					&& (tempSingle.getQualifiers() == null || tempSingle.getQualifiers().getNofQualifiers() == 0)) {

			}
		}*/

		final MultipleWithAttributes selfAttributes = withAttributesPath.getAttributes();
		if (selfAttributes == null) {
			return;
		}

		final MultipleWithAttributes newSelfAttributes = new MultipleWithAttributes();
		for (int i = 0; i < selfAttributes.getNofElements(); i++) {
			final SingleWithAttribute temp = selfAttributes.getAttribute(i);
			if (Attribute_Type.Encode_Attribute.equals(temp.getAttributeType())) {
				final SingleWithAttribute newAttribute = new SingleWithAttribute(
						temp.getAttributeType(), temp.hasOverride(), null, temp.getAttributeSpecification());
				newSelfAttributes.addAttribute(newAttribute);
			}
		}

		WithAttributesPath encodeAttributePath = null;
		if (newSelfAttributes.getNofElements() > 0) {
			//at least on "encode" was copied; create a context for them.
			encodeAttributePath = new WithAttributesPath();
			encodeAttributePath.setWithAttributes(newSelfAttributes);
			encodeAttributePath.setAttributeParent(withAttributesPath.getAttributeParent());
		}

		for (int i = 0, size = getNofComponents(); i < size; i++) {
			final CompField componentField = getComponentByIndex(i);
			final IType componentType = componentField.getType();

			componentType.clearWithAttributes();
			if (encodeAttributePath == null) {
				componentType.setAttributeParentPath(withAttributesPath.getAttributeParent());
			} else {
				componentType.setAttributeParentPath(encodeAttributePath);
			}
		}

		// Distribute the attributes with qualifiers to the components
		for (int j = 0; j < selfAttributes.getNofElements(); j++) {
			final SingleWithAttribute tempSingle = selfAttributes.getAttribute(j);
			final Qualifiers tempQualifiers = tempSingle.getQualifiers();
			if (tempQualifiers == null || tempQualifiers.getNofQualifiers() == 0) {
				continue;
			}

			for (int k = 0, kmax = tempQualifiers.getNofQualifiers(); k < kmax; k++) {
				final Qualifier tempQualifier = tempQualifiers.getQualifierByIndex(k);
				if (tempQualifier.getNofSubReferences() == 0) {
					continue;
				}

				final ISubReference tempSubReference = tempQualifier.getSubReferenceByIndex(0);
				boolean componentFound = false;
				for (int i = 0, size = getNofComponents(); i < size; i++) {
					final CompField componentField = getComponentByIndex(i);
					final Identifier componentId = componentField.getIdentifier();

					if (tempSubReference.getReferenceType() == Subreference_type.fieldSubReference
							&& tempSubReference.getId().equals(componentId)) {
						// Found a qualifier whose first identifier matches the component name
						final Qualifiers calculatedQualifiers = new Qualifiers();
						calculatedQualifiers.addQualifier(tempQualifier.getQualifierWithoutFirstSubRef());

						final SingleWithAttribute tempSingle2 = new SingleWithAttribute(
								tempSingle.getAttributeType(), tempSingle.hasOverride(), calculatedQualifiers, tempSingle.getAttributeSpecification());
						tempSingle2.setLocation(new Location(tempSingle.getLocation()));
						final IType componentType = componentField.getType();
						MultipleWithAttributes componentAttributes = componentType.getAttributePath().getAttributes();
						if (componentAttributes == null) {
							componentAttributes = new MultipleWithAttributes();
							componentAttributes.addAttribute(tempSingle2);
							componentType.setWithAttributes(componentAttributes);
						} else {
							componentAttributes.addAttribute(tempSingle2);
						}
						componentFound = true;
					}
				}

				if (!componentFound) {
					if (tempSubReference.getReferenceType() == Subreference_type.arraySubReference) {
						tempQualifier.getLocation().reportSemanticError(Qualifier.INVALID_INDEX_QUALIFIER);
					} else {
						tempQualifier.getLocation().reportSemanticError(MessageFormat.format(
								Qualifier.INVALID_FIELD_QUALIFIER, tempSubReference.getId().getDisplayName()));
					}
				}
			}
		}
	}

	@Override
	public final void check(final CompilationTimeStamp timestamp) {
		if (lastTimeChecked != null && !lastTimeChecked.isLess(timestamp)) {
			return;
		}
		//MarkerHandler.markAllSemanticMarkersForRemoval(this);//TODO: Check its place!!
		lastTimeChecked = timestamp;
		componentInternal = false;
		isErroneous = false;

		parseAttributes(timestamp);

		compFieldMap.check(timestamp);

		for (int i = 0, size = getNofComponents(); i < size; i++) {
			final IType type = getComponentByIndex(i).getType();
			if (type != null && type.isComponentInternal(timestamp)) {
				componentInternal = true;
				break;
			}
		}

		if (constraints != null) {
			constraints.check(timestamp);
		}

		checkSubtypeRestrictions(timestamp);
	}

	@Override
	public final void checkComponentInternal(final CompilationTimeStamp timestamp, final Set<IType> typeSet, final String operation) {
		if (typeSet.contains(this)) {
			return;
		}

		typeSet.add(this);
		for (int i = 0, size = getNofComponents(); i < size; i++) {
			IType type = getComponentByIndex(i).getType();
			if (type != null && type.isComponentInternal(timestamp)) {
				type.checkComponentInternal(timestamp, typeSet, operation);
			}
		}
		typeSet.remove(this);
	}

	@Override
	public final Object[] getOutlineChildren() {
		return compFieldMap.getOutlineChildren();
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
	public final void addProposal(final ProposalCollector propCollector, final int i) {
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
				final List<CompField> compFields = compFieldMap.getComponentsWithPrefix(subreference.getId().getName());
				for (CompField compField : compFields) {
					final String proposalKind = compField.getType().getProposalDescription(new StringBuilder()).toString();
					propCollector.addProposal(compField.getIdentifier(), " - " + proposalKind, ImageCache.getImage(getOutlineIcon()), proposalKind);
					IType type = compField.getType();
					if (type != null && compField.getIdentifier().equals(subreference.getId())) {
						type = type.getTypeRefdLast(CompilationTimeStamp.getBaseTimestamp());
						type.addProposal(propCollector, i + 1);
					}
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
	public final void addDeclaration(final DeclarationCollector declarationCollector, final int i) {
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
				final List<CompField> compFields = compFieldMap.getComponentsWithPrefix(subreference.getId().getName());
				for (CompField compField : compFields) {
					declarationCollector.addDeclaration(compField.getIdentifier().getDisplayName(), compField.getIdentifier().getLocation(), this);
				}
			}
		}
	}

	@Override
	public final void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			lastTimeChecked = null;
			boolean handled = false;

			if (compFieldMap != null
					&& reparser.envelopsDamage(compFieldMap.getLocation())) {
				try {
					compFieldMap.updateSyntax(reparser, true);
				} catch (ReParseException e) {
					e.decreaseDepth();
					throw e;
				}

				reparser.updateLocation(compFieldMap.getLocation());
				handled = true;
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

		reparser.updateLocation(compFieldMap.getLocation());
		compFieldMap.updateSyntax(reparser, false);

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
		compFieldMap.getEnclosingField(offset, rf);
	}

	@Override
	/** {@inheritDoc} */
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		super.findReferences(referenceFinder, foundIdentifiers);
		if (compFieldMap != null) {
			compFieldMap.findReferences(referenceFinder, foundIdentifiers);
		}
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
	public Identifier getComponentIdentifierByName(final Identifier identifier) {
		if(identifier == null){
			return null;
		}
		final CompField cf = getComponentByName(identifier.getName());
		return cf == null ? null : cf.getIdentifier();
	}

	@Override
	public Declaration resolveReference(final Reference reference, final int subRefIdx, final ISubReference lastSubreference) {
		final List<ISubReference> subreferences = reference.getSubreferences();
		int localIndex = subRefIdx;
		while (localIndex < subreferences.size() && subreferences.get(localIndex) instanceof ArraySubReference) {
			++localIndex;
		}

		if (localIndex == subreferences.size()) {
			return null;
		}

		final CompField compField = getComponentByName(subreferences.get(localIndex).getId().getName());
		if (compField == null) {
			return null;
		}
		if (subreferences.get(localIndex) == lastSubreference) {
			return Declaration.createInstance(getDefiningAssignment(), compField.getIdentifier());
		}

		final IType compFieldType = compField.getType().getTypeRefdLast(CompilationTimeStamp.getBaseTimestamp());
		if (compFieldType instanceof IReferenceableElement) {
			final Declaration decl = ((IReferenceableElement) compFieldType).resolveReference(reference, localIndex + 1, lastSubreference);
			return decl != null ? decl : Declaration.createInstance(getDefiningAssignment(), compField.getIdentifier());
		}

		return null;
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
		CompField compField = getComponentByName(fieldId.getName());
		Type nextType = compField.getType();
		boolean nextOptional = !isTemplate && compField.isOptional();
		if (nextOptional) {
			expression.expression.append(MessageFormat.format("if({0}) '{'\n", globalId));
			closingBrackets.insert(0, "}\n");
			String temporalId = aData.getTemporaryVariableName();
			expression.expression.append(MessageFormat.format("Optional<{0}{1}> {2} = {3}.get{4}();\n",
					nextType.getGenNameValue(aData, expression.expression, myScope), isTemplate?"_template":"", temporalId, externalId, FieldSubReference.getJavaGetterName( fieldId.getName())));

			if (subReferenceIndex == subreferences.size()-1) {
				expression.expression.append(MessageFormat.format("switch({0}.getSelection()) '{'\n", temporalId));
				expression.expression.append("case OPTIONAL_UNBOUND:\n");
				expression.expression.append(MessageFormat.format("{0} = false;\n", globalId));
				expression.expression.append("break;\n");
				expression.expression.append("case OPTIONAL_OMIT:\n");
				expression.expression.append(MessageFormat.format("{0} = {1};\n", globalId, isBound?"true":"false"));
				expression.expression.append("break;\n");
				expression.expression.append("default:\n");
				expression.expression.append("{\n");

				String temporalId2 = aData.getTemporaryVariableName();
				expression.expression.append(MessageFormat.format("{0}{1} {2} = {3}.constGet();\n", nextType.getGenNameValue(aData, expression.expression, myScope), isTemplate?"_template":"", temporalId2, temporalId));
				//FIXME handle omit_in_value_list
				expression.expression.append(MessageFormat.format("{0} = {1}.{2}({3});\n", globalId, temporalId2, isBound?"isBound":"isPresent", (!isBound && isTemplate)?"true":""));

				expression.expression.append("break;}\n");
				expression.expression.append("}\n");
				//at the end of the reference chain

				nextType.generateCodeIspresentBound(aData, expression, subreferences, subReferenceIndex + 1, globalId, temporalId2, isTemplate, isBound);
			} else {
				//still more to go
				expression.expression.append(MessageFormat.format("switch({0}.getSelection()) '{'\n", temporalId));
				expression.expression.append("case OPTIONAL_UNBOUND:\n");
				expression.expression.append("case OPTIONAL_OMIT:\n");
				expression.expression.append(MessageFormat.format("{0} = false;\n", globalId));
				expression.expression.append("break;\n");
				expression.expression.append("default:\n");
				expression.expression.append("break;\n");
				expression.expression.append("}\n");

				expression.expression.append(MessageFormat.format("if({0}) '{'\n", globalId));
				closingBrackets.insert(0, "}\n");
				String temporalId2 = aData.getTemporaryVariableName();
				expression.expression.append(MessageFormat.format("{0}{1} {2} = {3}.constGet();\n", nextType.getGenNameValue(aData, expression.expression, myScope), isTemplate?"_template":"", temporalId2, temporalId));
				expression.expression.append(MessageFormat.format("{0} = {1}.isBound();\n", globalId, temporalId2));

				nextType.generateCodeIspresentBound(aData, expression, subreferences, subReferenceIndex + 1, globalId, temporalId2, isTemplate, isBound);
			}
		} else {
			expression.expression.append(MessageFormat.format("if({0}) '{'\n", globalId));
			closingBrackets.insert(0, "}\n");

			String temporalId = aData.getTemporaryVariableName();
			String temporalId2 = aData.getTemporaryVariableName();
			expression.expression.append(MessageFormat.format("{0}{1} {2} = {3};\n", getGenNameValue(aData, expression.expression, myScope), isTemplate?"_template":"", temporalId, externalId));
			expression.expression.append(MessageFormat.format("{0}{1} {2} = {3}.constGet{4}();\n", nextType.getGenNameValue(aData, expression.expression, myScope), isTemplate?"_template":"", temporalId2, temporalId, FieldSubReference.getJavaGetterName( fieldId.getName())));
			//FIXME handle omit_in_value_list
			expression.expression.append(MessageFormat.format("{0} = {1}.{2}({3});\n", globalId, temporalId2, isBound|| (subReferenceIndex!=subreferences.size()-1)?"isBound":"isPresent", (!(isBound || (subReferenceIndex!=subreferences.size()-1)) && isTemplate)?"true":""));

			nextType.generateCodeIspresentBound(aData, expression, subreferences, subReferenceIndex + 1, globalId, temporalId2, isTemplate, isBound);
		}

		expression.expression.append(closingBrackets);
	}
}
