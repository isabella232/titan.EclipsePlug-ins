/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.types;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.ArraySubReference;
import org.eclipse.titan.designer.AST.FieldSubReference;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.IReferenceableElement;
import org.eclipse.titan.designer.AST.ISubReference;
import org.eclipse.titan.designer.AST.ISubReference.Subreference_type;
import org.eclipse.titan.designer.AST.IValue.Value_type;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.ITypeWithComponents;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.ParameterisedSubReference;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.ReferenceChain;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.Type;
import org.eclipse.titan.designer.AST.Value;
import org.eclipse.titan.designer.AST.ASN1.types.ASN1_Choice_Type;
import org.eclipse.titan.designer.AST.ASN1.types.ASN1_Set_Seq_Choice_BaseType;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.attributes.RawAST;
import org.eclipse.titan.designer.AST.TTCN3.attributes.RawAST.rawAST_ext_bit_group;
import org.eclipse.titan.designer.AST.TTCN3.attributes.RawAST.rawAST_single_tag;
import org.eclipse.titan.designer.AST.TTCN3.attributes.RawAST.rawAST_tag_field_value;
import org.eclipse.titan.designer.AST.TTCN3.attributes.RawASTStruct;
import org.eclipse.titan.designer.AST.TTCN3.attributes.RawASTStruct.rawAST_coding_ext_group;
import org.eclipse.titan.designer.AST.TTCN3.attributes.RawASTStruct.rawAST_coding_field_list;
import org.eclipse.titan.designer.AST.TTCN3.attributes.RawASTStruct.rawAST_coding_field_type;
import org.eclipse.titan.designer.AST.TTCN3.attributes.RawASTStruct.rawAST_coding_fields;
import org.eclipse.titan.designer.AST.TTCN3.attributes.RawASTStruct.rawAST_coding_taglist;
import org.eclipse.titan.designer.AST.TTCN3.types.RecordSetCodeGenerator.FieldInfo;
import org.eclipse.titan.designer.AST.TTCN3.values.Expression_Value.Operation_type;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.ExpressionStruct;
import org.eclipse.titan.designer.compiler.BuildTimestamp;
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
	protected BuildTimestamp rawLengthCalculated;
	protected int rawLength;

	public TTCN3_Set_Seq_Choice_BaseType(final CompFieldMap compFieldMap) {
		this.compFieldMap = compFieldMap;
		componentInternal = false;
		compFieldMap.setMyType(this);
		compFieldMap.setFullNameParent(this);
	}

	@Override
	/** {@inheritDoc} */
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
	 * Returns the index of the element with the specified name.
	 *
	 * @param identifier
	 *                the name of the element to return
	 * @return the index of an element with the provided name,
	 *         -1 if there is no such element.
	 */
	public int getComponentIndexByName(final Identifier identifier) {
		for (int i = 0; i < compFieldMap.fields.size(); i++) {
			if (compFieldMap.fields.get(i).getIdentifier().equals(identifier)) {
				return i;
			}
		}

		return -1;
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
	/** {@inheritDoc} */
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
	/** {@inheritDoc} */
	public final boolean isComponentInternal(final CompilationTimeStamp timestamp) {
		check(timestamp);

		return componentInternal;
	}

	@Override
	/** {@inheritDoc} */
	public void parseAttributes(final CompilationTimeStamp timestamp) {
		checkDoneAttribute(timestamp);
	}

	@Override
	/** {@inheritDoc} */
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

		if (myScope != null) {
			checkEncode(timestamp);
			checkVariants(timestamp);
		}
	}

	@Override
	/** {@inheritDoc} */
	public final void checkComponentInternal(final CompilationTimeStamp timestamp, final Set<IType> typeSet, final String operation) {
		if (typeSet.contains(this)) {
			return;
		}

		typeSet.add(this);
		for (int i = 0, size = getNofComponents(); i < size; i++) {
			final IType type = getComponentByIndex(i).getType();
			if (type != null && type.isComponentInternal(timestamp)) {
				type.checkComponentInternal(timestamp, typeSet, operation);
			}
		}
		typeSet.remove(this);
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
		for (int i = 0, size = getNofComponents(); i < size; i++) {
			final IType type = getComponentByIndex(i).getType();
			type.getTypesWithNoCodingTable(timestamp, typeList, onlyOwnTable);
		}
	}

	@Override
	/** {@inheritDoc} */
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
				for (final CompField compField : compFields) {
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
				for (final CompField compField : compFields) {
					declarationCollector.addDeclaration(compField.getIdentifier().getDisplayName(), compField.getIdentifier().getLocation(), this);
				}
			}
		}
	}

	@Override
	/** {@inheritDoc} */
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
	/** {@inheritDoc} */
	public Identifier getComponentIdentifierByName(final Identifier identifier) {
		if(identifier == null){
			return null;
		}
		final CompField cf = getComponentByName(identifier.getName());
		return cf == null ? null : cf.getIdentifier();
	}

	@Override
	/** {@inheritDoc} */
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
	public boolean canHaveCoding(final CompilationTimeStamp timestamp, final MessageEncoding_type coding, final IReferenceChain refChain) {
		if (refChain.contains(this)) {
			return true;
		}
		refChain.add(this);

		for (int i = 0; i < codingTable.size(); i++) {
			final Coding_Type tempCodingType = codingTable.get(i);

			if (tempCodingType.builtIn && tempCodingType.builtInCoding.equals(coding)) {
				return true; // coding already added
			}
		}

		if (coding == MessageEncoding_type.BER) {
			return hasEncoding(timestamp, MessageEncoding_type.BER, null);
		}

		for ( final CompField compField : compFieldMap.fields ) {
			refChain.markState();
			if (!compField.getType().getTypeRefdLast(timestamp).canHaveCoding(timestamp, coding, refChain)) {
				return false;
			}
			refChain.previousState();
		}

		return true;
	}

	/**
	 * Check the raw coding attributes of TTCN3 record and set types.
	 *
	 * @param timestamp the timestamp of the actual semantic check cycle.
	 * */
	protected void checkSetSeqRawCodingAttributes(final CompilationTimeStamp timestamp) {
		//check raw attributes
		if (rawAttribute != null) {
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
			if (rawAttribute.ext_bit_groups != null) {
				for (int a = 0; a < rawAttribute.ext_bit_groups.size(); a++) {
					final rawAST_ext_bit_group tempExtBitGroup = rawAttribute.ext_bit_groups.get(a);
					final Identifier fromIdentifier = tempExtBitGroup.from;
					final Identifier toIdentifier = tempExtBitGroup.to;
					boolean foundError = false;

					if (!hasComponentWithName(fromIdentifier.getName())) {
						fromIdentifier.getLocation().reportSemanticError(MessageFormat.format("Invalid field name `{0}'' in RAW parameter EXTENSION_BIT_GROUP for type `{1}''", fromIdentifier.getDisplayName(), getTypename()));
						foundError = true;
					}
					if (!hasComponentWithName(toIdentifier.getName())) {
						toIdentifier.getLocation().reportSemanticError(MessageFormat.format("Invalid field name `{0}'' in RAW parameter EXTENSION_BIT_GROUP for type `{1}''", toIdentifier.getDisplayName(), getTypename()));
						foundError = true;
					}
					if (!foundError) {
						boolean foundStart = false;
						for (int i = 0; i < getNofComponents(); i++) {
							final Identifier tempId = getComponentByIndex(i).getIdentifier();
							if (tempId.equals(fromIdentifier)) {
								foundStart = true;
							} else if (tempId.equals(toIdentifier)) {
								if (!foundStart) {
									getLocation().reportSemanticError(MessageFormat.format("Invalid field order in RAW parameter EXTENSION_BIT_GROUP for type `{0}'': `{1}'', `{2}''", getTypename(), fromIdentifier.getDisplayName(), toIdentifier.getDisplayName()));
								}
								break;
							}
						}
					}
				}
			}
			if (rawAttribute.paddall != RawAST.XDEFDEFAULT) {
				for (int i = 0; i < getNofComponents(); i++) {
					final CompField cField = getComponentByIndex(i);
					final Type fieldType = cField.getType();
					RawAST fieldRawAttribute = fieldType.rawAttribute;
					if (fieldRawAttribute == null) {
						IType t = fieldType;
						if (t instanceof Referenced_Type) {
							final IReferenceChain referenceChain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
							t = ((Referenced_Type)t).getTypeRefd(timestamp, referenceChain);
							referenceChain.release();
						}
						while (t.getRawAttribute() == null && t instanceof Referenced_Type) {
							final IReferenceChain referenceChain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
							t = ((Referenced_Type)t).getTypeRefd(timestamp, referenceChain);
							referenceChain.release();
						}
						fieldRawAttribute = new RawAST(t.getRawAttribute(), fieldType.getDefaultRawFieldLength());
						fieldType.setRawAttributes(fieldRawAttribute);
					}
					if (fieldRawAttribute.padding == 0) {
						fieldRawAttribute.padding = rawAttribute.padding;
					}
					if (fieldRawAttribute.prepadding == 0) {
						fieldRawAttribute.prepadding = rawAttribute.prepadding;
					}
					if (fieldRawAttribute.padding_pattern_length == 0 && rawAttribute.padding_pattern_length > 0) {
						fieldRawAttribute.padding_pattern = rawAttribute.padding_pattern;
						fieldRawAttribute.padding_pattern_length = rawAttribute.padding_pattern_length;
					}
				}
			}
			if (rawAttribute.fieldorder != RawAST.XDEFDEFAULT) {
				for (int i = 0; i < getNofComponents(); i++) {
					final CompField cField = getComponentByIndex(i);
					final Type fieldType = cField.getType();
					RawAST fieldRawAttribute = fieldType.rawAttribute;
					if (fieldRawAttribute == null) {
						fieldRawAttribute = new RawAST(fieldType.getDefaultRawFieldLength());
						fieldType.setRawAttributes(fieldRawAttribute);
					}
					if (fieldRawAttribute.fieldorder == RawAST.XDEFDEFAULT) {
						fieldRawAttribute.fieldorder = rawAttribute.fieldorder;
					}
				}
			}
		}
		if (rawAttribute != null && rawAttribute.presence != null) {
			if (rawAttribute.presence.keyList != null) {
				for (int a = 0; a < rawAttribute.presence.keyList.size(); a++) {
					final rawAST_tag_field_value tempTagFieldValue = rawAttribute.presence.keyList.get(a);
					final Reference reference = new Reference(null);
					reference.addSubReference(new FieldSubReference(tempTagFieldValue.keyField.names.get(0)));
					for (int b = 1; b < tempTagFieldValue.keyField.names.size(); b++) {
						reference.addSubReference(new FieldSubReference(tempTagFieldValue.keyField.names.get(b)));
					}

					final IType t = getFieldType(timestamp, reference, 0, Expected_Value_type.EXPECTED_DYNAMIC_VALUE, false);
					if (t != null) {
						final Value v = tempTagFieldValue.v_value;
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
		int usedBits = 0; // number of bits used to store all previous fields
		for (int i = 0; i < getNofComponents(); i++) {
			final CompField cField = getComponentByIndex(i);
			final Type fieldType = cField.getType();
			fieldType.forceRaw(timestamp);
			final RawAST rawPar = fieldType.rawAttribute;
			if (rawPar != null) {
				final Identifier fieldId = cField.getIdentifier();
				final IType fieldTypeLast = fieldType.getTypeRefdLast(timestamp);
				if (rawPar.prepadding != 0) {
					usedBits = (usedBits + rawPar.prepadding - 1) / rawPar.prepadding * rawPar.prepadding;
				}
				if (rawPar.intX && fieldTypeLast.getTypetype() == Type_type.TYPE_INTEGER) {
					if (usedBits % 8 != 0 && (rawAttribute == null || rawAttribute.fieldorder != RawAST.XDEFMSB)) {
						getLocation().reportSemanticError(MessageFormat.format("Using RAW parameter IntX in a record/set with FIELDORDER set to 'lsb' is only supported if the IntX field starts at the beginning of a new octet. There are {0} unused bits in the last octet before field {1}.", 8 - (usedBits % 8), fieldId.getDisplayName()));
					}
				} else if (rawPar.fieldlength > 0) {
					usedBits += rawPar.fieldlength;
				}
				if (rawPar.padding != 0) {
					usedBits = (usedBits + rawPar.padding - 1) / rawPar.padding * rawPar.padding;
				}
				if (rawPar.lengthto != null) {
					for (int j = 0; j < rawPar.lengthto.size(); j++) {
						final Identifier id = rawPar.lengthto.get(j);
						if (!hasComponentWithName(id.getName())) {
							id.getLocation().reportSemanticError(MessageFormat.format("Invalid fieldname in RAW parameter LENGTHTO for field {0}: {1}", fieldId.getDisplayName(), id.getDisplayName()));
						}
					}
				}
				if (rawPar.lengthto != null) {
					switch (fieldTypeLast.getTypetype()) {
					case TYPE_INTEGER:
					case TYPE_INTEGER_A:
						break;
					case TYPE_TTCN3_CHOICE:
						for (int fi = 0; fi < ((TTCN3_Choice_Type)fieldTypeLast).getNofComponents(); fi++) {
							final Type_type tt = ((TTCN3_Choice_Type)fieldTypeLast).getComponentByIndex(fi).getType().getTypetype();
							if (tt != Type_type.TYPE_INTEGER && tt != Type_type.TYPE_INTEGER_A) {
								getLocation().reportSemanticError("The union type LENGTHTO field must contain only integer fields");
							}
						}
						break;
					case TYPE_ASN1_CHOICE:
						for (int fi = 0; fi < ((ASN1_Choice_Type)fieldTypeLast).getNofComponents(timestamp); fi++) {
							final Type_type tt = ((ASN1_Choice_Type)fieldTypeLast).getComponentByIndex(fi).getType().getTypetype();
							if (tt != Type_type.TYPE_INTEGER && tt != Type_type.TYPE_INTEGER_A) {
								getLocation().reportSemanticError("The union type LENGTHTO field must contain only integer fields");
							}
						}
						break;
					case TYPE_ANYTYPE:
					case TYPE_OPENTYPE:
					case TYPE_TTCN3_SEQUENCE:
					case TYPE_ASN1_SEQUENCE:
					case TYPE_TTCN3_SET:
					case TYPE_ASN1_SET:
						if (rawPar.lengthindex != null) {
							// will be checked in the next step
							break;
						}
					default:
						getLocation().reportSemanticError(MessageFormat.format("The LENGTHTO field must be an integer or union type instead of `{0}''", fieldTypeLast.getTypename()));
						break;
					}
				}
				if (rawPar.lengthto != null && rawPar.lengthindex != null) {
					final Identifier id = rawPar.lengthindex.names.get(0);
					switch (fieldTypeLast.getTypetype()) {
					case TYPE_TTCN3_CHOICE:
					case TYPE_TTCN3_SEQUENCE:
					case TYPE_TTCN3_SET:
						if(!((TTCN3_Set_Seq_Choice_BaseType)fieldTypeLast).hasComponentWithName(id.getName())) {
							id.getLocation().reportSemanticError(MessageFormat.format("Invalid fieldname in RAW parameter LENGTHINDEX for field {0}: {1}", fieldId.getDisplayName(), id.getDisplayName()));
						}
						break;
					case TYPE_ASN1_CHOICE:
					case TYPE_ASN1_SEQUENCE:
					case TYPE_ASN1_SET:
						if(!((ASN1_Set_Seq_Choice_BaseType)fieldTypeLast).hasComponentWithName(id)) {
							id.getLocation().reportSemanticError(MessageFormat.format("Invalid fieldname in RAW parameter LENGTHINDEX for field {0}: {1}", fieldId.getDisplayName(), id.getDisplayName()));
						}
						break;
					default:
						fieldId.getLocation().reportSemanticError(MessageFormat.format("Invalid fieldmember type in RAW parameter LENGTHINDEX for field {0}.", fieldId.getDisplayName()));
						break;
					}
				}
				if (rawPar.pointerto != null) {
					final Identifier id = rawPar.pointerto;
					boolean errorFound = false;
					int pointed = 0;
					if (!hasComponentWithName(id.getName())) {
						id.getLocation().reportSemanticError(MessageFormat.format("Invalid fieldname in RAW parameter POINTERTO for field {0}: {1}", fieldId.getDisplayName(), id.getDisplayName()));
						errorFound = true;
					}
					if (!errorFound) {
						pointed = getComponentIndexByName(id);
						if (pointed <= i) {
							id.getLocation().reportSemanticError(MessageFormat.format("Pointer must precede the pointed field. Incorrect field name `{0}'' in RAW parameter POINTERTO for field `{1}''", id.getDisplayName(), fieldId.getDisplayName()));
							errorFound = true;
						}
					}
					if (!errorFound && rawPar.ptrbase != null) {
						final Identifier idf2 = rawPar.ptrbase;
						if (!hasComponentWithName(idf2.getName())) {
							idf2.getLocation().reportSemanticError(MessageFormat.format("Invalid field name `{0}'' in RAW parameter PTROFFSET for field `{1}''", idf2.getDisplayName(), fieldId.getDisplayName()));
							errorFound = true;
						}
						if (!errorFound && getComponentIndexByName(idf2) > pointed) {
							idf2.getLocation().reportSemanticError(MessageFormat.format("Pointer base must precede the pointed field. Incorrect field name `{0}'' in RAW parameter PTROFFSET for field `{1}''", idf2.getDisplayName(), fieldId.getDisplayName()));
						}
					}
				}
				if (rawPar.presence != null && rawPar.presence.keyList != null) {
					for (int a = 0; a < rawPar.presence.keyList.size(); a++) {
						final rawAST_tag_field_value tempTagFieldValue = rawPar.presence.keyList.get(a);
						final Reference reference = new Reference(null);
						reference.addSubReference(new FieldSubReference(tempTagFieldValue.keyField.names.get(0)));
						for (int b = 1; b < tempTagFieldValue.keyField.names.size(); b++) {
							reference.addSubReference(new FieldSubReference(tempTagFieldValue.keyField.names.get(b)));
						}

						final IType t = getFieldType(timestamp, reference, 0, Expected_Value_type.EXPECTED_DYNAMIC_VALUE, false);
						if (t != null) {
							final Value v = tempTagFieldValue.v_value;
							if (v != null) {
								v.setMyScope(getMyScope());
								v.setMyGovernor(t);
								final IValue tempValue = t.checkThisValueRef(timestamp, v);
								t.checkThisValue(timestamp, tempValue, null, new ValueCheckingOptions(Expected_Value_type.EXPECTED_CONSTANT, false, false, false, false, false));
							}
						}
					}
				}
				if (rawPar.crosstaglist != null) {
					boolean errorFound = false;
					for (int c = 0; c < rawPar.crosstaglist.size(); c++) {
						final rawAST_single_tag singleTag = rawPar.crosstaglist.get(c);
						final Identifier idf = singleTag.fieldName;
						switch (fieldTypeLast.getTypetype()) {
						case TYPE_TTCN3_CHOICE:
						case TYPE_TTCN3_SEQUENCE:
						case TYPE_TTCN3_SET:
							if (idf == null) {
								getLocation().reportSemanticError("Field member in RAW parameter CROSSTAG cannot be 'omit'");
								errorFound = true;
							} else if(!((TTCN3_Set_Seq_Choice_BaseType)fieldTypeLast).hasComponentWithName(idf.getName())) {
								idf.getLocation().reportSemanticError(MessageFormat.format("Invalid fieldname in RAW parameter CROSSTAG for field {0}: {1}", fieldId.getDisplayName(), idf.getDisplayName()));
								errorFound = true;
							}
							break;
						case TYPE_ASN1_CHOICE:
						case TYPE_ASN1_SEQUENCE:
						case TYPE_ASN1_SET:
							if (idf == null) {
								getLocation().reportSemanticError("Field member in RAW parameter CROSSTAG cannot be 'omit'");
								errorFound = true;
							} else if(!((ASN1_Set_Seq_Choice_BaseType)fieldTypeLast).hasComponentWithName(idf)) {
								idf.getLocation().reportSemanticError(MessageFormat.format("Invalid fieldname in RAW parameter CROSSTAG for field {0}: {1}", fieldId.getDisplayName(), idf.getDisplayName()));
								errorFound = true;
							}
							break;
						default:
							fieldId.getLocation().reportSemanticError(MessageFormat.format("Invalid fieldmember type in RAW parameter CROSSTAG for field {0}.", fieldId.getDisplayName()));
							errorFound = true;
							break;
						}


						if (!errorFound && singleTag.keyList != null) {
							for (int a = 0; a < singleTag.keyList.size(); a++) {
								IType t2 = this;
								boolean errorFound2 = false;
								boolean allow_omit = false;
								final rawAST_tag_field_value tagField = singleTag.keyList.get(a);
								for (int b = 0; b < tagField.keyField.names.size() && !errorFound2; b++) {
									final Identifier idf2 = tagField.keyField.names.get(b);
									CompField cf2 = null;
									switch (t2.getTypetype()) {
									case TYPE_TTCN3_CHOICE:
									case TYPE_TTCN3_SEQUENCE:
									case TYPE_TTCN3_SET:
										if(!((TTCN3_Set_Seq_Choice_BaseType)t2).hasComponentWithName(idf2.getName())) {
											idf2.getLocation().reportSemanticError(MessageFormat.format("Invalid fieldname in RAW parameter CROSSTAG for field {0}: {1}", fieldId.getDisplayName(), idf2.getDisplayName()));
											errorFound2 = true;
										} else {
											cf2 = ((TTCN3_Set_Seq_Choice_BaseType)t2).getComponentByName(idf2.getName());
										}
										break;
									case TYPE_ASN1_CHOICE:
									case TYPE_ASN1_SEQUENCE:
									case TYPE_ASN1_SET:
										if(!((ASN1_Set_Seq_Choice_BaseType)t2).hasComponentWithName(idf2)) {
											idf2.getLocation().reportSemanticError(MessageFormat.format("Invalid fieldname in RAW parameter CROSSTAG for field {0}: {1}", fieldId.getDisplayName(), idf2.getDisplayName()));
											errorFound2 = true;
										} else {
											cf2 = ((ASN1_Set_Seq_Choice_BaseType)t2).getComponentByName(idf2);
										}
										break;
									default:
										fieldId.getLocation().reportSemanticError(MessageFormat.format("Invalid fieldmember type in RAW parameter CROSSTAG for field {0}.", fieldId.getDisplayName()));
										errorFound2 = true;
										break;
									}
									if (b == 0) {
										final int fieldIndex = getComponentIndexByName(idf2);
										if (fieldIndex == i) {
											idf2.getLocation().reportSemanticError(MessageFormat.format("RAW parameter CROSSTAG for field `{0}'' cannot refer to the field itself", idf2.getDisplayName()));
											errorFound2 = true;
										} else if (fieldIndex > i) {
											if (cField.isOptional()) {//TODO || fieldType.getRawLength() < 0 
												idf2.getLocation().reportSemanticError(MessageFormat.format("Field `{0}'' that CROSSTAG refers to must precede field `{1}'' or field `{1}'' must be mandatory with fixed length", idf2.getDisplayName(), fieldId.getDisplayName()));
												errorFound2 = true;
											}
										}
									}
									if (!errorFound2) {
										t2 = cf2.getType().getTypeRefdLast(timestamp);
										if (b == tagField.keyField.names.size() - 1 && cf2.isOptional()) {
											allow_omit = true;
										}
									}
								}
								if (!errorFound2) {
									final Value v = singleTag.keyList.get(a).v_value;
									if (v != null) {
										v.setMyScope(getMyScope());
										v.setMyGovernor(t2);
										final IValue tempValue = t2.checkThisValueRef(timestamp, v);
										t2.checkThisValue(timestamp, tempValue, null, new ValueCheckingOptions(Expected_Value_type.EXPECTED_CONSTANT, false, allow_omit, false, false, false));
									}
								}
							}
						}
					}
				}
			}
		}
	}

	//FIXME comment
	protected RawASTStruct convertRAWCodingAttributes(final JavaGenData aData, final StringBuilder source, final boolean hasRaw, final List<FieldInfo> namesList) {
		RawASTStruct raw = null;
		if (hasRaw) {
			RawAST dummy_raw;
			if (rawAttribute == null) {
				dummy_raw = new RawAST(getDefaultRawFieldLength());
			} else {
				dummy_raw = rawAttribute;
			}
			raw = new RawASTStruct(dummy_raw);

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
					codingKey.isOmitValue = key.v_value.getValuetype() == Value_type.OMIT_VALUE;
					codingKey.start_pos = 0;
					final CompField cf = getComponentByIndex(codingSingleTag.fieldnum);
					IType t = cf.getType().getTypeRefdLast(CompilationTimeStamp.getBaseTimestamp());

					final RawASTStruct.rawAST_coding_fields tempField = new rawAST_coding_fields();
					tempField.nthfield = codingSingleTag.fieldnum;
					tempField.nthfieldname = singleTag.fieldName.getName();
					tempField.fieldtype = rawAST_coding_field_type.UNION_FIELD;
					tempField.type = t.getGenNameValue(aData, source, myScope);
					tempField.typedesc = t.getGenNameTypeDescriptor(aData, source, myScope);
					if (cf.isOptional()) {
						tempField.fieldtype = rawAST_coding_field_type.OPTIONAL_FIELD;
					} else {
						tempField.fieldtype = rawAST_coding_field_type.MANDATORY_FIELD;
					}
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
							return null;
						}

						final IType field_type = cf2.getType();
						newField.type = field_type.getGenNameValue(aData, source, myScope);
						newField.typedesc = field_type.getGenNameTypeDescriptor(aData, source, myScope);
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
			// building presence list
			final int presenceListSize = dummy_raw.presence == null || dummy_raw.presence.keyList == null ? 0 : dummy_raw.presence.keyList.size();
			for (int a = 0; a < presenceListSize; a++) {
				final rawAST_tag_field_value fieldValue = dummy_raw.presence.keyList.get(a);
				final rawAST_coding_field_list presences = new rawAST_coding_field_list();
				raw.presence.fields.add(presences);

				final ExpressionStruct expression = new ExpressionStruct();
				fieldValue.v_value.generateCodeExpression(aData, expression, true);
				presences.expression = expression;
				presences.isOmitValue = fieldValue.v_value.getValuetype() == Value_type.OMIT_VALUE;
				final int keySize = fieldValue.keyField == null || fieldValue.keyField.names == null ? 0 : fieldValue.keyField.names.size();
				presences.fields = new ArrayList<RawASTStruct.rawAST_coding_fields>(keySize);
				IType t = this;
				for (int b = 0; b < keySize; b++) {
					final RawASTStruct.rawAST_coding_fields newField = new rawAST_coding_fields();
					presences.fields.add(newField);

					final Identifier idf2 = fieldValue.keyField.names.get(b);
					int comp_index = 0;
					CompField cf2;
					switch (t.getTypetype()) {
					case TYPE_TTCN3_CHOICE:
						comp_index = ((TTCN3_Choice_Type)t).getComponentIndexByName(idf2);
						cf2 = ((TTCN3_Choice_Type)t).getComponentByIndex(comp_index);
						newField.nthfield = comp_index;
						newField.nthfieldname = idf2.getName();
						newField.fieldtype = rawAST_coding_field_type.UNION_FIELD;
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
						return null;
					}

					final IType field_type = cf2.getType();
					newField.type = field_type.getGenNameValue(aData, source, myScope);
					newField.typedesc = field_type.getGenNameTypeDescriptor(aData, source, myScope);

					t = field_type.getTypeRefdLast(CompilationTimeStamp.getBaseTimestamp());
				}
			}
			final int extBiGroupSize = dummy_raw.ext_bit_groups == null ? 0 : dummy_raw.ext_bit_groups.size();
			for (int c = 0; c < extBiGroupSize; c++) {
				final rawAST_ext_bit_group tempGroup = dummy_raw.ext_bit_groups.get(c);
				final Identifier idf = tempGroup.from;
				final Identifier idf2 = tempGroup.to;

				final rawAST_coding_ext_group codingGroup = new rawAST_coding_ext_group();
				raw.ext_bit_groups.add(codingGroup);
				codingGroup.ext_bit = tempGroup.ext_bit;
				codingGroup.from = getComponentIndexByName(idf);
				codingGroup.to = getComponentIndexByName(idf2);
			}
			for (int i = 0; i < getNofComponents(); i++) {
				final FieldInfo element_i = namesList.get(i);
				final CompField cf = getComponentByIndex(i);
				final IType t_field = cf.getType();
				final IType t_field_last = t_field.getTypeRefdLast(CompilationTimeStamp.getBaseTimestamp());
				final RawAST rawpar = t_field.getRawAttribute();
				if (rawpar != null) {
					element_i.raw = new RawASTStruct(rawpar);
					final int lengthtoNum = rawpar.lengthto == null ? 0 : rawpar.lengthto.size();
					for (int j = 0; j < lengthtoNum; j++) {
						final Identifier idf = rawpar.lengthto.get(j);
						element_i.raw.lengthto.add(getComponentIndexByName(idf));
					}
					if (lengthtoNum > 0 && rawpar.lengthindex != null) {
						final Identifier idf = rawpar.lengthindex.names.get(0);
						int comp_index = 0;
						CompField cf2;
						switch (t_field_last.getTypetype()) {
						case TYPE_TTCN3_CHOICE:
							comp_index = ((TTCN3_Choice_Type)t_field_last).getComponentIndexByName(idf);
							cf2 = ((TTCN3_Choice_Type)t_field_last).getComponentByIndex(comp_index);
							element_i.raw.lengthindex.nthfield = comp_index;
							element_i.raw.lengthindex.nthfieldname = idf.getName();
							break;
						case TYPE_TTCN3_SEQUENCE:
						case TYPE_TTCN3_SET:
							comp_index = ((TTCN3_Set_Seq_Choice_BaseType)t_field_last).getComponentIndexByName(idf);
							cf2 = ((TTCN3_Set_Seq_Choice_BaseType)t_field_last).getComponentByIndex(comp_index);
							element_i.raw.lengthindex.nthfield = comp_index;
							element_i.raw.lengthindex.nthfieldname = idf.getName();
							break;
						default:
							//internal error
							return null;
						}
						final Type t_field2 = cf2.getType();
						if (t_field2.getTypetype() == Type_type.TYPE_TTCN3_CHOICE) {
							element_i.raw.lengthindex.fieldtype = rawAST_coding_field_type.UNION_FIELD;
						} else if (cf2.isOptional()) {
							element_i.raw.lengthindex.fieldtype = rawAST_coding_field_type.OPTIONAL_FIELD;
						} else {
							element_i.raw.lengthindex.fieldtype = rawAST_coding_field_type.MANDATORY_FIELD;
						}

						element_i.raw.lengthindex.type = t_field2.getGenNameValue(aData, source, myScope);
						element_i.raw.lengthindex.typedesc = t_field2.getGenNameTypeDescriptor(aData, source, myScope);
					}
					if (lengthtoNum > 0 && rawpar.lengthindex == null) {
						switch (t_field_last.getTypetype()) {
						case TYPE_TTCN3_CHOICE:
						case TYPE_TTCN3_SEQUENCE:
						case TYPE_TTCN3_SET:
							final int componentsNumber = ((TTCN3_Set_Seq_Choice_BaseType)t_field_last).getNofComponents();
							element_i.raw.union_member_num = componentsNumber;
							element_i.raw.member_name = new ArrayList<String>(componentsNumber + 1);
							element_i.raw.member_name.add(t_field_last.getGenNameValue(aData, source, myScope));
							for (int m = 1; m < componentsNumber + 1; m++){
								final CompField compf = ((TTCN3_Set_Seq_Choice_BaseType)t_field_last).getComponentByIndex(m - 1);
								element_i.raw.member_name.add(compf.getIdentifier().getName());
							}
							break;
						default:
							break;
						}
					}
					if (rawpar.pointerto != null) {
						final Identifier idf = rawpar.pointerto;
						element_i.raw.pointerto = getComponentIndexByName(idf);
						if (rawpar.ptrbase != null) {
							final Identifier idf2 = rawpar.ptrbase;
							element_i.raw.pointerbase = getComponentIndexByName(idf2);
						} else {
							element_i.raw.pointerbase = i;
						}
					}
					// building presence list
					final int parPresenceListSize = rawpar.presence == null || rawpar.presence.keyList == null ? 0 : rawpar.presence.keyList.size();
					for (int a = 0; a < parPresenceListSize; a++) {
						final rawAST_coding_field_list presences = new rawAST_coding_field_list();
						element_i.raw.presence.fields.add(presences);

						final rawAST_tag_field_value fieldValue = rawpar.presence.keyList.get(a);
						final ExpressionStruct expression = new ExpressionStruct();
						fieldValue.v_value.generateCodeExpression(aData, expression, true);
						presences.expression = expression;
						presences.isOmitValue = fieldValue.v_value.getValuetype() == Value_type.OMIT_VALUE;
						presences.fields = new ArrayList<RawASTStruct.rawAST_coding_fields>(fieldValue.keyField.names.size());
						IType t = this;
						for (int b = 0; b < fieldValue.keyField.names.size(); b++) {
							final RawASTStruct.rawAST_coding_fields newField = new rawAST_coding_fields();
							presences.fields.add(newField);

							final Identifier idf2 = fieldValue.keyField.names.get(b);
							int comp_index = 0;
							CompField cf2;
							switch (t.getTypetype()) {
							case TYPE_TTCN3_CHOICE:
								comp_index = ((TTCN3_Choice_Type)t).getComponentIndexByName(idf2);
								cf2 = ((TTCN3_Choice_Type)t).getComponentByIndex(comp_index);
								newField.nthfield = comp_index;
								newField.nthfieldname = idf2.getName();
								newField.fieldtype = rawAST_coding_field_type.UNION_FIELD;
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
								return null;
							}

							final IType field_type = cf2.getType();
							newField.type = field_type.getGenNameValue(aData, source, myScope);
							newField.typedesc = field_type.getGenNameTypeDescriptor(aData, source, myScope);

							t = field_type.getTypeRefdLast(CompilationTimeStamp.getBaseTimestamp());
						}
					}
					// building crosstaglist
					final int crossTaglistSize = rawpar.crosstaglist == null ? 0 : rawpar.crosstaglist.size();
					for (int c = 0; c < crossTaglistSize; c++) {
						final rawAST_single_tag singleTag = rawpar.crosstaglist.get(c);
						final rawAST_coding_taglist codingSingleTag = element_i.raw.crosstaglist.list.get(c);
						if (singleTag.keyList != null) {
							codingSingleTag.fields = new ArrayList<RawASTStruct.rawAST_coding_field_list>(singleTag.keyList.size());
						}
						codingSingleTag.fieldname = singleTag.fieldName.getName();
						codingSingleTag.varName = FieldSubReference.getJavaGetterName(codingSingleTag.fieldname);
						final Identifier idf = singleTag.fieldName;
						switch (t_field_last.getTypetype()) {
						case TYPE_TTCN3_CHOICE:
						case TYPE_TTCN3_SEQUENCE:
						case TYPE_TTCN3_SET:
							codingSingleTag.fieldnum = ((TTCN3_Set_Seq_Choice_BaseType)t_field_last).getComponentIndexByName(idf);
							break;
						case TYPE_ASN1_CHOICE:
							codingSingleTag.fieldnum = ((ASN1_Set_Seq_Choice_BaseType)t_field_last).getComponentIndexByName(idf);
							break;
						default:
							codingSingleTag.fieldnum  = -1;
							break;
						}

						final int keyListSize = singleTag.keyList == null ? 0 : singleTag.keyList.size();
						for (int a = 0; a < keyListSize; a++) {
							final rawAST_tag_field_value key = singleTag.keyList.get(a);
							final RawASTStruct.rawAST_coding_field_list codingKey = new RawASTStruct.rawAST_coding_field_list();
							codingSingleTag.fields.add(codingKey);

							codingKey.fields = new ArrayList<RawASTStruct.rawAST_coding_fields>(key.keyField.names.size());
							final ExpressionStruct expression = new ExpressionStruct();
							key.v_value.generateCodeExpression(aData, expression, true);
							codingKey.expression = expression;
							codingKey.isOmitValue = key.v_value.getValuetype() == Value_type.OMIT_VALUE;
							if (codingKey.isOmitValue && key.keyField.names.size() != 1) {
								getLocation().reportSemanticError("omit value with multiple fields in CROSSTAG");
								break;
							}

							IType t = this;
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
									return null;
								}

								final IType field_type = cf2.getType();
								newField.type = field_type.getGenNameValue(aData, source, myScope);
								newField.typedesc = field_type.getGenNameTypeDescriptor(aData, source, myScope);

								t = field_type.getTypeRefdLast(CompilationTimeStamp.getBaseTimestamp());
							}
						}
					}
					
					element_i.raw.length = t_field.getRawLength(aData.getBuildTimstamp());
					element_i.hasRaw = true;
				} else {
					element_i.hasRaw = false;
				}
			}
		}

		return raw;
	}

	@Override
	/** {@inheritDoc} */
	public void setGenerateCoderFunctions(final CompilationTimeStamp timestamp, final MessageEncoding_type encodingType) {
		switch(encodingType) {
		case RAW:
			break;
		default:
			return;
		}

		if (getGenerateCoderFunctions(encodingType)) {
			//already set
			return;
		}

		codersToGenerate.add(encodingType);

		for ( final CompField compField : compFieldMap.fields ) {
			compField.getType().getTypeRefdLast(timestamp).setGenerateCoderFunctions(timestamp, encodingType);
		}
	}

	@Override
	/** {@inheritDoc} */
	public String getGenNameRawDescriptor(final JavaGenData aData, final StringBuilder source) {
		generateCodeRawDescriptor(aData, source);

		return getGenNameOwn(myScope) + "_raw_";
	}

	@Override
	/** {@inheritDoc} */
	public void generateCodeIsPresentBoundChosen(final JavaGenData aData, final ExpressionStruct expression, final List<ISubReference> subreferences,
			final int subReferenceIndex, final String globalId, final String externalId, final boolean isTemplate, final Operation_type optype, final String field) {
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
			} else if (optype == Operation_type.ISCHOOSEN_OPERATION) {
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
			expression.expression.append("FATAL_ERROR encountered");
			return;
		}

		final Identifier fieldId = ((FieldSubReference) subReference).getId();
		final CompField compField = getComponentByName(fieldId.getName());
		final Type nextType = compField.getType();
		final boolean nextOptional = !isTemplate && compField.isOptional();
		if (nextOptional) {
			expression.expression.append(MessageFormat.format("if({0}) '{'\n", globalId));
			closingBrackets.insert(0, "}\n");
			final String temporalId = aData.getTemporaryVariableName();
			aData.addBuiltinTypeImport("Optional");
			expression.expression.append(MessageFormat.format("final Optional<{0}{1}> {2} = {3}.get{4}();\n",
					nextType.getGenNameValue(aData, expression.expression, myScope), isTemplate?"_template":"", temporalId, externalId, FieldSubReference.getJavaGetterName( fieldId.getName())));

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
				expression.expression.append(MessageFormat.format("{0}{1} {2} = {3}.constGet();\n", nextType.getGenNameValue(aData, expression.expression, myScope), isTemplate?"_template":"", temporalId2, temporalId));

				if (optype == Operation_type.ISBOUND_OPERATION) {
					expression.expression.append(MessageFormat.format("{0} = {1}.isBound();\n", globalId, temporalId2));
				} else if (optype == Operation_type.ISPRESENT_OPERATION) {
					expression.expression.append(MessageFormat.format("{0} = {1}.isPresent({2});\n", globalId, temporalId2, isTemplate && aData.getAllowOmitInValueList()?"true":""));
				} else if (optype == Operation_type.ISCHOOSEN_OPERATION) {
					expression.expression.append(MessageFormat.format("{0} = {1}.isChosen({2});\n", globalId, temporalId2, field));
				}

				expression.expression.append("break;}\n");
				expression.expression.append("}\n");
				//at the end of the reference chain

				nextType.generateCodeIsPresentBoundChosen(aData, expression, subreferences, subReferenceIndex + 1, globalId, temporalId2, isTemplate, optype, field);
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
				expression.expression.append(MessageFormat.format("{0}{1} {2} = {3}.constGet();\n", nextType.getGenNameValue(aData, expression.expression, myScope), isTemplate?"_template":"", temporalId2, temporalId));
				expression.expression.append(MessageFormat.format("{0} = {1}.isBound();\n", globalId, temporalId2));

				nextType.generateCodeIsPresentBoundChosen(aData, expression, subreferences, subReferenceIndex + 1, globalId, temporalId2, isTemplate, optype, field);
			}
		} else {
			expression.expression.append(MessageFormat.format("if({0}) '{'\n", globalId));
			closingBrackets.insert(0, "}\n");

			final String temporalId = aData.getTemporaryVariableName();
			final String temporalId2 = aData.getTemporaryVariableName();
			expression.expression.append(MessageFormat.format("{0}{1} {2} = new {0}{1}({3});\n", getGenNameValue(aData, expression.expression, myScope), isTemplate?"_template":"", temporalId, externalId));
			expression.expression.append(MessageFormat.format("{0}{1} {2} = {3}.get{4}();\n", nextType.getGenNameValue(aData, expression.expression, myScope), isTemplate?"_template":"", temporalId2, temporalId, FieldSubReference.getJavaGetterName( fieldId.getName())));

			if (optype == Operation_type.ISBOUND_OPERATION) {
				expression.expression.append(MessageFormat.format("{0} = {1}.isBound();\n", globalId, temporalId2));
			} else if (optype == Operation_type.ISPRESENT_OPERATION) {
				expression.expression.append(MessageFormat.format("{0} = {1}.{2}({3});\n", globalId, temporalId2, subReferenceIndex!=subreferences.size()-1?"isBound":"isPresent", subReferenceIndex==subreferences.size()-1 && isTemplate && aData.getAllowOmitInValueList()?"true":""));
			} else if (optype == Operation_type.ISCHOOSEN_OPERATION) {
				expression.expression.append(MessageFormat.format("{0} = {1}.isBound();\n", globalId, temporalId2));
				if (subReferenceIndex==subreferences.size()-1) {
					expression.expression.append(MessageFormat.format("if ({0}) '{'\n", globalId));
					expression.expression.append(MessageFormat.format("{0} = {1}.isChosen({2});\n", globalId, temporalId2, field));
					expression.expression.append("}\n");
				}
			}

			nextType.generateCodeIsPresentBoundChosen(aData, expression, subreferences, subReferenceIndex + 1, globalId, temporalId2, isTemplate, optype, field);
		}

		expression.expression.append(closingBrackets);
	}
}
