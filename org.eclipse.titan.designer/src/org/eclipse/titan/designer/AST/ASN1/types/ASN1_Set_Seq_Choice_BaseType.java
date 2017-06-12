/******************************************************************************
 * Copyright (c) 2000-2017 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.ASN1.types;

import java.text.MessageFormat;
import java.util.List;

import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.ArraySubReference;
import org.eclipse.titan.designer.AST.FieldSubReference;
import org.eclipse.titan.designer.AST.IReferenceableElement;
import org.eclipse.titan.designer.AST.ISubReference;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.ITypeWithComponents;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.Type;
import org.eclipse.titan.designer.AST.ASN1.ASN1Type;
import org.eclipse.titan.designer.AST.ASN1.Block;
import org.eclipse.titan.designer.AST.ISubReference.Subreference_type;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.TTCN3.types.CompField;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.ExpressionStruct;
import org.eclipse.titan.designer.compiler.JavaGenData;
import org.eclipse.titan.designer.declarationsearch.Declaration;
import org.eclipse.titan.designer.editors.ProposalCollector;
import org.eclipse.titan.designer.editors.actions.DeclarationCollector;
import org.eclipse.titan.designer.graphics.ImageCache;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * @author Kristof Szabados
 * */
public abstract class ASN1_Set_Seq_Choice_BaseType extends ASN1Type implements ITypeWithComponents, IReferenceableElement {

	protected Block mBlock;
	protected CTs_EE_CTs components;

	@Override
	/** {@inheritDoc} */
	public String getTypename() {
		return getFullName();
	}

	/**
	 * Returns the element with the specified name.
	 *
	 * @param identifier
	 *                the name of the element to return
	 * @return the element with the specified name in this list, or null if
	 *         none exists.
	 */
	public CompField getComponentByName(final Identifier identifier) {
		if (null == components) {
			return null;
		}

		return components.getCompByName(identifier);
	}

	/** @return the number of components */
	public int getNofComponents(final CompilationTimeStamp timestamp) {
		if (null == components || lastTimeChecked == null) {
			check(timestamp);
		}

		return components.getNofComps();
	}

	/**
	 * Returns whether an element is stored with the specified name.
	 *
	 * @param identifier
	 *                the name of the element to return
	 * @return true if an element with the provided name exists in the list,
	 *         false otherwise
	 */
	public boolean hasComponentWithName(final Identifier identifier) {
		if (null == components) {
			return false;
		}

		return components.hasCompWithName(identifier);
	}

	/**
	 * Returns the element at the specified position.
	 *
	 * @param index
	 *                index of the element to return
	 * @return the element at the specified position in this list
	 */
	public CompField getComponentByIndex(final int index) {
		return components.getCompByIndex(index);
	}

	/**
	 * Returns the identifier of the element at the specified position.
	 *
	 * @param index
	 *                index of the element to return
	 * @return the identifier of the element at the specified position in
	 *         this list
	 */
	public Identifier getComponentIdentifierByIndex(final int index) {
		return components.getCompByIndex(index).getIdentifier();
	}

	@Override
	/** {@inheritDoc} */
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		if (null != components) {
			components.setMyScope(scope);
		}
	}

	// TODO: remove this when the location is properly set
	@Override
	public Location getLikelyLocation() {
		if (mBlock != null) {
			return mBlock.getLocation();
		} else {
			return location;
		}
	}

	@Override
	/** {@inheritDoc} */
	public void getEnclosingField(final int offset, final ReferenceFinder rf) {
		if (components == null) {
			return;
		}

		components.getEnclosingField(offset, rf);
	}

	@Override
	/** {@inheritDoc} */
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		super.findReferences(referenceFinder, foundIdentifiers);
		if (components != null) {
			components.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	@Override
	/** {@inheritDoc} */
	protected boolean memberAccept(final ASTVisitor v) {
		if (!super.memberAccept(v)) {
			return false;
		}
		if (components != null && !components.accept(v)) {
			return false;
		}
		return true;
	}

	@Override
	public Identifier getComponentIdentifierByName(final Identifier identifier) {
		final CompField cf = getComponentByName(identifier);
		return cf == null ? null : cf.getIdentifier();
	}

	@Override
	public Declaration resolveReference(final Reference reference, final int subRefIdx, final ISubReference lastSubreference) {
		final List<ISubReference> subreferences = reference.getSubreferences();
		int actualIndex = subRefIdx;
		while (actualIndex < subreferences.size() && subreferences.get(actualIndex) instanceof ArraySubReference) {
			++actualIndex;
		}

		if (actualIndex == subreferences.size()) {
			return null;
		}

		final Identifier fieldID = subreferences.get(actualIndex).getId();
		if (subreferences.get(actualIndex) == lastSubreference) {
			return Declaration.createInstance(getDefiningAssignment(), fieldID);
		}

		final CompField compField = getComponentByName(fieldID);
		final IType compFieldType = compField.getType().getTypeRefdLast(CompilationTimeStamp.getBaseTimestamp());
		if (compFieldType instanceof IReferenceableElement) {
			final Declaration decl = ((IReferenceableElement) compFieldType).resolveReference(reference, actualIndex + 1, lastSubreference);
			return decl != null ? decl : Declaration.createInstance(getDefiningAssignment(), compField.getIdentifier());
		}

		return null;
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
				final CompField compField = components.getCompByName(subreference.getId());
				if (compField == null) {
					return;
				}

				final IType type = compField.getType();
				if (type != null) {
					type.addDeclaration(declarationCollector, i + 1);
				}
			} else {
				// final part of the reference
				final List<CompField> compFields = components.getComponentsWithPrefix(subreference.getId().getName());
				for (CompField compField : compFields) {
					declarationCollector.addDeclaration(compField.getIdentifier().getDisplayName(),
							compField.getIdentifier().getLocation(), this);
				}
			}
		}
	}

	@Override
	public Object[] getOutlineChildren() {
		if (components == null) {
			return new Object[] {};
		}

		return components.getOutlineChildren();
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
		if (subreferences.size() <= i || components == null) {
			return;
		}

		final ISubReference subreference = subreferences.get(i);
		if (Subreference_type.fieldSubReference.equals(subreference.getReferenceType())) {
			if (subreferences.size() > i + 1) {
				// the reference might go on

				final CompField compField = components.getCompByName(subreference.getId());
				if (compField == null) {
					return;
				}

				final IType type = compField.getType();
				if (type != null) {
					type.addProposal(propCollector, i + 1);
				}
			} else {
				// final part of the reference
				final List<CompField> compFields = components.getComponentsWithPrefix(subreference.getId().getName());
				for (CompField compField : compFields) {
					final String proposalKind = compField.getType().getProposalDescription(new StringBuilder()).toString();
					propCollector.addProposal(compField.getIdentifier(), " - " + proposalKind,
							ImageCache.getImage(getOutlineIcon()), proposalKind);
				}
			}
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
		case fieldSubReference: {
			final Identifier id = subreference.getId();
			final CompField compField = components.getCompByName(id);
			if (compField == null) {
				return false;
			}

			final IType fieldType = compField.getType();
			if (fieldType == null) {
				return false;
			}
			typeArray.add(this);
			return fieldType.getFieldTypesAsArray(reference, actualSubReference + 1, typeArray);
		}
		case parameterisedSubReference:
			return false;
		default:
			return false;
		}
	}

	@Override
	/** {@inheritDoc} */
	public void generateCodeIspresentBound(JavaGenData aData, ExpressionStruct expression, List<ISubReference> subreferences,
			int subReferenceIndex, String globalId, String externalId, boolean isTemplate, boolean isBound) {
		if (subreferences == null || getIsErroneous(CompilationTimeStamp.getBaseTimestamp())) {
			return;
		}

		if (subReferenceIndex >= subreferences.size()) {
			return;
		}

		//FIXME handle template

		ISubReference subReference = subreferences.get(subReferenceIndex);
		if (!(subReference instanceof FieldSubReference)) {
			ErrorReporter.INTERNAL_ERROR("Code generator reached erroneous type reference `" + getFullName() + "''");
			expression.expression.append("FATAL_ERROR encountered");
			return;
		}

		StringBuilder closingBrackets = new StringBuilder();
		Identifier fieldId = ((FieldSubReference) subReference).getId();
		CompField compField = getComponentByName(fieldId);
		Type nextType = compField.getType();
		boolean nextOptional = !isTemplate && compField.isOptional();
		if (nextOptional) {
			expression.expression.append(MessageFormat.format("if({0}) '{'\n", globalId));
			closingBrackets.append("}\n");
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
				closingBrackets.append("}\n");
				String temporalId2 = aData.getTemporaryVariableName();
				expression.expression.append(MessageFormat.format("{0}{1} {2} = {3}.constGet();\n", nextType.getGenNameValue(aData, expression.expression, myScope), isTemplate?"_template":"", temporalId2, temporalId));
				expression.expression.append(MessageFormat.format("{0} = {1}.isBound();\n", globalId, temporalId2));

				nextType.generateCodeIspresentBound(aData, expression, subreferences, subReferenceIndex + 1, globalId, temporalId2, isTemplate, isBound);
			}
		} else {
			expression.expression.append(MessageFormat.format("if({0}) '{'\n", globalId));
			closingBrackets.append("}\n");

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
