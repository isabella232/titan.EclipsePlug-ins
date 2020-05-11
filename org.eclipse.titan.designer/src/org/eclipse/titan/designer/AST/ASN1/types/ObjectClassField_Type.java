/******************************************************************************
 * Copyright (c) 2000-2020 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.ASN1.types;

import java.text.MessageFormat;
import java.util.List;

import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.ArraySubReference;
import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.FieldSubReference;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.IReferencingType;
import org.eclipse.titan.designer.AST.ISubReference;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.IValue.Value_type;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.ParameterisedSubReference;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.ReferenceChain;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.TypeCompatibilityInfo;
import org.eclipse.titan.designer.AST.ASN1.ASN1Type;
import org.eclipse.titan.designer.AST.ASN1.IASN1Type;
import org.eclipse.titan.designer.AST.ASN1.Object.ObjectClass_Definition;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.templates.ITTCN3Template;
import org.eclipse.titan.designer.compiler.JavaGenData;
import org.eclipse.titan.designer.editors.ProposalCollector;
import org.eclipse.titan.designer.editors.actions.DeclarationCollector;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * @author Kristof Szabados
 * */
public final class ObjectClassField_Type extends ASN1Type implements IReferencingType {

	private final IASN1Type referred_type;
	private final ObjectClass_Definition objectClass;
	private final Identifier fieldName;

	public ObjectClassField_Type(final IASN1Type referredType, final ObjectClass_Definition objectClass, final Identifier identifier) {
		this.referred_type = referredType;
		this.objectClass = objectClass;
		fieldName = identifier;

		if (referredType != null) {
			referredType.setOwnertype(TypeOwner_type.OT_OCFT, this);
		}
	}

	@Override
	/** {@inheritDoc} */
	public Type_type getTypetype() {
		return Type_type.TYPE_OBJECTCLASSFIELDTYPE;
	}

	@Override
	/** {@inheritDoc} */
	public IASN1Type newInstance() {
		return new ObjectClassField_Type(referred_type, objectClass, fieldName);
	}

	@Override
	/** {@inheritDoc} */
	public String chainedDescription() {
		return "type ObjectClassFieldType: " + referred_type.getFullName();
	}

	public ObjectClass_Definition getMyObjectClass() {
		return objectClass;
	}

	public Identifier getObjectClassFieldName() {
		return fieldName;
	}

	@Override
	/** {@inheritDoc} */
	public boolean isCompatible(final CompilationTimeStamp timestamp, final IType otherType, final TypeCompatibilityInfo info,
			final TypeCompatibilityInfo.Chain leftChain, final TypeCompatibilityInfo.Chain rightChain) {
		check(timestamp);
		otherType.check(timestamp);

		if (null == referred_type) {
			return false;
		}

		final IType t1 = referred_type.getTypeRefdLast(timestamp);
		final IType t2 = otherType.getTypeRefdLast(timestamp);

		if (t1.getIsErroneous(timestamp) || t2.getIsErroneous(timestamp)) {
			return true;
		}

		return t1.isCompatible(timestamp, t2, null, null, null);
	}

	@Override
	/** {@inheritDoc} */
	public Type_type getTypetypeTtcn3() {
		return Type_type.TYPE_UNDEFINED;
	}

	@Override
	/** {@inheritDoc} */
	public String getTypename() {
		if (isErroneous || null == referred_type || this == referred_type) {
			return "Object class field type";
		}

		return referred_type.getTypename();
	}

	@Override
	/** {@inheritDoc} */
	public String getOutlineIcon() {
		return "titan.gif";
	}

	@Override
	/** {@inheritDoc} */
	public void check(final CompilationTimeStamp timestamp) {
		if (null != lastTimeChecked && !lastTimeChecked.isLess(timestamp)) {
			return;
		}

		lastTimeChecked = timestamp;
		isErroneous = false;

		if (null != referred_type) {
			referred_type.check(timestamp);
		}

		if (null != constraints) {
			constraints.check(timestamp);
		}
	}

	@Override
	/** {@inheritDoc} */
	public void checkRecursions(final CompilationTimeStamp timestamp, final IReferenceChain referenceChain) {
		if (referenceChain.add(this)) {
			final IReferenceChain tempReferenceChain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
			final IType type = getTypeRefd(timestamp, tempReferenceChain);
			tempReferenceChain.release();

			if (null != type && !type.getIsErroneous(timestamp) && !this.equals(type)) {
				type.checkRecursions(timestamp, referenceChain);
			}
		}
	}

	@Override
	/** {@inheritDoc} */
	public IValue checkThisValueRef(final CompilationTimeStamp timestamp, final IValue value) {
		if (Value_type.UNDEFINED_LOWERIDENTIFIER_VALUE.equals(value.getValuetype())) {
			final IReferenceChain tempReferenceChain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
			final IType refd = getTypeRefd(timestamp, tempReferenceChain);
			tempReferenceChain.release();

			if (null == refd) {
				return value;
			}

			return refd.checkThisValueRef(timestamp, value);
		}

		return value;
	}

	@Override
	/** {@inheritDoc} */
	public boolean checkThisValue(final CompilationTimeStamp timestamp, final IValue value, final Assignment lhs, final ValueCheckingOptions valueCheckingOptions) {
		final IReferenceChain tempReferenceChain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
		final IType last = getTypeRefd(timestamp, tempReferenceChain);
		tempReferenceChain.release();

		boolean selfReference = false;
		if (null != last && last != this) {
			selfReference = last.checkThisValue(timestamp, value, lhs, valueCheckingOptions);
		}

		value.setLastTimeChecked(timestamp);

		return selfReference;
	}

	@Override
	/** {@inheritDoc} */
	public boolean checkThisTemplate(final CompilationTimeStamp timestamp, final ITTCN3Template template, final boolean isModified,
			final boolean implicitOmit, final Assignment lhs) {
		registerUsage(template);

		if (getIsErroneous(timestamp)) {
			return false;
		}

		boolean selfReference = false;
		final IType tempType = getTypeRefdLast(timestamp);
		if (tempType != this) {
			selfReference = tempType.checkThisTemplate(timestamp, template, isModified, implicitOmit, lhs);
		}

		return selfReference;
	}

	@Override
	/** {@inheritDoc} */
	public IType getTypeRefd(final CompilationTimeStamp timestamp, final IReferenceChain refChain) {
		if (refChain.add(this) && !getIsErroneous(timestamp)) {
			return referred_type;
		}

		isErroneous = true;
		lastTimeChecked = timestamp;
		return this;
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
					MessageFormat.format(FieldSubReference.INVALIDSUBREFERENCE, ((FieldSubReference) subreference).getId()
							.getDisplayName(), getTypename()));
			return null;
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
	public void checkMapParameter(final CompilationTimeStamp timestamp, final IReferenceChain refChain, final Location errorLocation) {
		if (refChain.contains(this)) {
			return;
		}

		refChain.add(this);
		final IType referencedLast = getTypeRefdLast(timestamp);
		if (referencedLast != null) {
			referencedLast.checkMapParameter(timestamp, refChain, errorLocation);
		}
	}

	@Override
	/** {@inheritDoc} */
	public StringBuilder getProposalDescription(final StringBuilder builder) {
		if (null != referred_type) {
			return referred_type.getProposalDescription(builder);
		}

		return builder.append("unknown_referred_type");
	}

	/**
	 * Searches and adds a completion proposal to the provided collector if
	 * a valid one is found.
	 * <p>
	 * The type referred last is identified, and the job of adding a
	 * proposal is delegated to it.
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

		if (null != referred_type && !this.equals(referred_type)) {
			referred_type.addProposal(propCollector, i);
		}
	}

	/**
	 * Searches and adds a declaration proposal to the provided collector if
	 * a valid one is found.
	 * <p>
	 * The type referred last is identified, and the job of adding a
	 * declaration is delegated to it.
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
		if (null != referred_type && !this.equals(referred_type)) {
			referred_type.addDeclaration(declarationCollector, i);
		}
	}

	@Override
	/** {@inheritDoc} */
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		super.findReferences(referenceFinder, foundIdentifiers);
		// TODO
	}

	@Override
	/** {@inheritDoc} */
	protected boolean memberAccept(final ASTVisitor v) {
		if (!super.memberAccept(v)) {
			return false;
		}
		if (referred_type != null && !referred_type.accept(v)) {
			return false;
		}
		if (objectClass != null && !objectClass.accept(v)) {
			return false;
		}
		if (fieldName != null && !fieldName.accept(v)) {
			return false;
		}
		return true;
	}

	@Override
	/** {@inheritDoc} */
	public String getGenNameValue(final JavaGenData aData, final StringBuilder source) {
		if (this == referred_type || referred_type == null) {
			ErrorReporter.INTERNAL_ERROR("Code generator reached erroneous object class field type `" + getFullName() + "''");
			return "FATAL_ERROR encountered while processing `" + getFullName() + "''\n";
		}

		return referred_type.getGenNameValue(aData, source);
	}

	@Override
	/** {@inheritDoc} */
	public String getGenNameTemplate(final JavaGenData aData, final StringBuilder source) {
		if (this == referred_type || referred_type == null) {
			ErrorReporter.INTERNAL_ERROR("Code generator reached erroneous object class field type `" + getFullName() + "''");
			return "FATAL_ERROR encountered while processing `" + getFullName() + "''\n";
		}

		return referred_type.getGenNameTemplate(aData, source);
	}

	@Override
	/** {@inheritDoc} */
	public String getGenNameTypeDescriptor(final JavaGenData aData, final StringBuilder source) {
		if (rawAttribute != null || jsonAttribute != null ||
				hasVariantAttributes(CompilationTimeStamp.getBaseTimestamp())) {
			if (needsAlias()) {
				String baseName = getGenNameOwn(aData);
				return baseName + "." + getGenNameOwn();
			} else if (getParentType() != null) {
				final IType parentType = getParentType();
				if (parentType.generatesOwnClass(aData, source)) {
					return parentType.getGenNameOwn(aData) + "." + getGenNameOwn();
				}

				return getGenNameOwn(aData);
			}

			return getGenNameOwn(aData);
		}

		if (needsAlias()) {
			String baseName = getGenNameOwn(aData);
			return baseName + "." + getGenNameOwn();
		}

		return referred_type.getGenNameTypeDescriptor(aData, source);
	}

	@Override
	/** {@inheritDoc} */
	public boolean needsOwnJsonDescriptor(final JavaGenData aData) {
		return !((jsonAttribute == null || jsonAttribute.empty()) && (getOwnertype() != TypeOwner_type.OT_RECORD_OF || getParentType().getJsonAttribute() == null
				|| !getParentType().getJsonAttribute().as_map));
	}

	@Override
	/** {@inheritDoc} */
	public String getGenNameJsonDescriptor(final JavaGenData aData, final StringBuilder source) {
		if (this == referred_type || referred_type == null) {
			ErrorReporter.INTERNAL_ERROR("Code generator reached erroneous type reference `" + getFullName() + "''");

			return "FATAL_ERROR encountered while processing `" + getFullName() + "''\n";
		}

		if (needsOwnJsonDescriptor(aData)) {
			return getGenNameOwn(aData) + "_json_";
		}

		return referred_type.getGenNameJsonDescriptor(aData, source);
	}

	@Override
	/** {@inheritDoc} */
	public boolean generatesOwnClass(final JavaGenData aData, final StringBuilder source) {
		return needsAlias();
	}

	@Override
	/** {@inheritDoc} */
	public void generateCode( final JavaGenData aData, final StringBuilder source ) {
		if (lastTimeGenerated != null && !lastTimeGenerated.isLess(aData.getBuildTimstamp())) {
			return;
		}

		lastTimeGenerated = aData.getBuildTimstamp();

		final IType last = getTypeRefdLast(CompilationTimeStamp.getBaseTimestamp());
		if(myScope.getModuleScopeGen() == last.getMyScope().getModuleScopeGen()) {
			final StringBuilder tempSource = aData.getCodeForType(last.getGenNameOwn());
			if (tempSource.length() == 0) {
				last.generateCode(aData, tempSource);
			}
		}

		if(needsAlias()) {
			final String ownName = getGenNameOwn();
			last.generateCode(aData, source);

			source.append(MessageFormat.format("\tpublic static class {0} extends {1} '{'\n", ownName, referred_type.getGenNameValue(aData, source)));

			final StringBuilder descriptor = new StringBuilder();
			generateCodeTypedescriptor(aData, source, descriptor, null);
			generateCodeDefaultCoding(aData, source, descriptor);
			generateCodeForCodingHandlers(aData, source, descriptor);
			source.append(descriptor);

			source.append("\t}\n");

			source.append(MessageFormat.format("\tpublic static class {0}_template extends {1} '{' '}'\n", ownName, referred_type.getGenNameTemplate(aData, source)));
		} else {
			generateCodeTypedescriptor(aData, source, null, aData.attibute_registry);
			generateCodeDefaultCoding(aData, source, null);
			generateCodeForCodingHandlers(aData, source, null);
		}
	}
}
