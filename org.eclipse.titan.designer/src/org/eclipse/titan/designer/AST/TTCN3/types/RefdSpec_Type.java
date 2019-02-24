/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.types;

import java.text.MessageFormat;
import java.util.List;

import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.IReferencingType;
import org.eclipse.titan.designer.AST.ISubReference;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.IValue.Value_type;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.ReferenceChain;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.TypeCompatibilityInfo;
import org.eclipse.titan.designer.AST.ASN1.ASN1Type;
import org.eclipse.titan.designer.AST.ASN1.IASN1Type;
import org.eclipse.titan.designer.AST.ASN1.definitions.SpecialASN1Module;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Definition;
import org.eclipse.titan.designer.AST.TTCN3.templates.ITTCN3Template;
import org.eclipse.titan.designer.AST.TTCN3.values.Expression_Value.Operation_type;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.ExpressionStruct;
import org.eclipse.titan.designer.compiler.JavaGenData;
import org.eclipse.titan.designer.editors.ProposalCollector;
import org.eclipse.titan.designer.editors.actions.DeclarationCollector;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * @author Kristof Szabados
 * */
public class RefdSpec_Type extends ASN1Type implements IReferencingType {

	private final IType refdType;
	private boolean componentInternal;

	public RefdSpec_Type(final IType type) {
		this.refdType = type;
		type.setOwnertype(TypeOwner_type.OT_REF_SPEC, this);
		componentInternal = false;
	}

	@Override
	/** {@inheritDoc} */
	public Type_type getTypetype() {
		return Type_type.TYPE_REFD_SPEC;
	}

	@Override
	/** {@inheritDoc} */
	public IASN1Type newInstance() {
		return new RefdSpec_Type(refdType);
	}
	
	@Override
	/** {@inheritDoc} */
	public boolean isCompatible(final CompilationTimeStamp timestamp, final IType otherType, final TypeCompatibilityInfo info,
			final TypeCompatibilityInfo.Chain leftChain, final TypeCompatibilityInfo.Chain rightChain) {
		check(timestamp);
		otherType.check(timestamp);
		final IType t1 = getTypeRefdLast(timestamp);
		final IType t2 = otherType.getTypeRefdLast(timestamp);

		if (t1.getIsErroneous(timestamp) || t2.getIsErroneous(timestamp)) {
			return true;
		}

		return t1.isCompatible(timestamp, t2, info, null, null);
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
		if (isErroneous || refdType == null || refdType == this) {
			return "Referenced type";
		}

		return refdType.getTypename();
	}

	@Override
	/** {@inheritDoc} */
	public String getOutlineIcon() {
		return "referenced.gif";
	}

	@Override
	/** {@inheritDoc} */
	public IType getFieldType(final CompilationTimeStamp timestamp, final Reference reference, final int actualSubReference,
			final Expected_Value_type expectedIndex, final IReferenceChain refChain, final boolean interruptIfOptional) {

		check(timestamp);
		if (reference.getSubreferences().size() == 1) {
			return this;
		}

		if (refdType != null && this != refdType) {
			final Expected_Value_type internalExpectation =
					expectedIndex == Expected_Value_type.EXPECTED_TEMPLATE ? Expected_Value_type.EXPECTED_DYNAMIC_VALUE : expectedIndex;
			final IType temp = refdType.getFieldType(timestamp, reference, actualSubReference, internalExpectation, refChain, interruptIfOptional);
			if (reference.getIsErroneous(timestamp)) {
				setIsErroneous(true);
			}
			return temp;
		}

		return this;
	}

	@Override
	/** {@inheritDoc} */
	public void check(final CompilationTimeStamp timestamp) {
		check(timestamp, null);
	}

	@Override
	/** {@inheritDoc} */
	public void check(final CompilationTimeStamp timestamp, final IReferenceChain refChain) {
		if (lastTimeChecked != null && !lastTimeChecked.isLess(timestamp)) {
			return;
		}

		lastTimeChecked = timestamp;
		componentInternal = false;
		isErroneous = false;

		refdType.check(timestamp);
		componentInternal = refdType.isComponentInternal(timestamp);

		if (myScope != null) {
			checkEncode(timestamp);
			checkVariants(timestamp);
		}
	}

	@Override
	/** {@inheritDoc} */
	public IValue checkThisValueRef(final CompilationTimeStamp timestamp, final IValue value) {
		if (Value_type.UNDEFINED_LOWERIDENTIFIER_VALUE.equals(value.getValuetype())) {
			final IReferenceChain tempReferenceChain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
			final IType refd = getTypeRefd(timestamp, tempReferenceChain);
			tempReferenceChain.release();

			if (refd == null || this.equals(refd)) {
				return value;
			}

			return refd.checkThisValueRef(timestamp, value);
		}

		return value;
	}

	@Override
	/** {@inheritDoc} */
	public boolean checkThisValue(final CompilationTimeStamp timestamp, final IValue value, final Assignment lhs, final ValueCheckingOptions valueCheckingOptions) {
		if (getIsErroneous(timestamp)) {
			return false;
		}

		boolean selfReference = false;
		final IType tempType = getTypeRefdLast(timestamp);
		if (tempType != this) {
			selfReference = tempType.checkThisValue(timestamp, value, lhs, new ValueCheckingOptions(valueCheckingOptions.expected_value,
					valueCheckingOptions.incomplete_allowed, valueCheckingOptions.omit_allowed, false, false,
					valueCheckingOptions.str_elem));
			final Definition def = value.getDefiningAssignment();
			if (def != null) {
				final String referingModuleName = getMyScope().getModuleScope().getName();
				if (!def.referingHere.contains(referingModuleName)) {
					def.referingHere.add(referingModuleName);
				}
			}
		}

		if (valueCheckingOptions.sub_check && subType != null) {
			subType.checkThisValue(timestamp, value);
		}

		value.setLastTimeChecked(timestamp);

		return selfReference;
	}

	@Override
	/** {@inheritDoc} */
	public boolean checkThisTemplate(final CompilationTimeStamp timestamp, final ITTCN3Template template, final boolean isModified,
			final boolean implicitOmit, final Assignment lhs) {
		if (getIsErroneous(timestamp)) {
			return false;
		}

		registerUsage(template);
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
			if (refdType != null) {
				return refdType;
			}
		}

		isErroneous = true;
		lastTimeChecked = timestamp;
		return this;
	}

	@Override
	/** {@inheritDoc} */
	public IType getTypeRefdLast(final CompilationTimeStamp timestamp, final IReferenceChain referenceChain) {
		final boolean newChain = null == referenceChain;
		IReferenceChain tempReferenceChain;
		if (newChain) {
			tempReferenceChain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
		} else {
			tempReferenceChain = referenceChain;
		}

		IType t = this;
		while (t != null && t instanceof IReferencingType && !t.getIsErroneous(timestamp)) {
			t = ((IReferencingType) t).getTypeRefd(timestamp, tempReferenceChain);
		}

		if (newChain) {
			tempReferenceChain.release();
		}

		if (t != null && t.getIsErroneous(timestamp)) {
			setIsErroneous(true);
		}

		return t;
	}

	@Override
	/** {@inheritDoc} */
	public void addProposal(final ProposalCollector propCollector, final int i) {
		if (lastTimeChecked == null) {
			check(CompilationTimeStamp.getBaseTimestamp());
		}

		if (refdType != null && !this.equals(refdType)) {
			refdType.addProposal(propCollector, i);
		}
	}

	@Override
	/** {@inheritDoc} */
	public void addDeclaration(final DeclarationCollector declarationCollector, final int i) {
		if (lastTimeChecked == null) {
			check(CompilationTimeStamp.getBaseTimestamp());
		}

		if (refdType != null && !this.equals(refdType)) {
			refdType.addDeclaration(declarationCollector, i);
		}
	}

	@Override
	/** {@inheritDoc} */
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		super.findReferences(referenceFinder, foundIdentifiers);
		if (refdType != null) {
			refdType.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	@Override
	/** {@inheritDoc} */
	public String getGenNameValue(final JavaGenData aData, final StringBuilder source) {
		if (this == refdType || refdType == null) {
			ErrorReporter.INTERNAL_ERROR("Code generator reached erroneous type reference `" + getFullName() + "''");
			return "FATAL_ERROR encountered while processing `" + getFullName() + "''\n";
		}

		return refdType.getGenNameValue(aData, source);
	}

	@Override
	/** {@inheritDoc} */
	public String getGenNameTemplate(final JavaGenData aData, final StringBuilder source) {
		if (this == refdType || refdType == null) {
			ErrorReporter.INTERNAL_ERROR("Code generator reached erroneous type reference `" + getFullName() + "''");
			return "FATAL_ERROR encountered while processing `" + getFullName() + "''\n";
		}

		return refdType.getGenNameTemplate(aData, source);
	}

	@Override
	/** {@inheritDoc} */
	public String getGenNameRawDescriptor(final JavaGenData aData, final StringBuilder source) {
		if (this == refdType || refdType == null) {
			ErrorReporter.INTERNAL_ERROR("Code generator reached erroneous type reference `" + getFullName() + "''");

			return "FATAL_ERROR encountered while processing `" + getFullName() + "''\n";
		}

		if (rawAttribute != null) {
			generateCodeRawDescriptor(aData, source);

			return getGenNameOwn(aData) + "_raw_";
		}

		return refdType.getGenNameRawDescriptor(aData, source);
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
			last.generateCode(aData, tempSource);
		}

		generateCodeTypedescriptor(aData, source);
		if(needsAlias()) {
			final String ownName = getGenNameOwn();
			switch (last.getTypetype()) {
			case TYPE_PORT:
				source.append(MessageFormat.format("\tpublic static class {0} extends {1} '{' '}'\n", ownName, last.getGenNameValue(aData, source)));
				break;
			case TYPE_SIGNATURE:
				source.append(MessageFormat.format("\tpublic static class {0}_call extends {1}_call '{' '}'\n", genName, last.getGenNameValue(aData, source)));
				source.append(MessageFormat.format("\tpublic static class {0}_call_redirect extends {1}_call_redirect '{' '}'\n", genName, last.getGenNameValue(aData, source)));
				if (!((Signature_Type) last).isNonblocking()) {
					source.append(MessageFormat.format("\tpublic static class {0}_reply extends {1}_reply '{' '}'\n", genName, last.getGenNameValue(aData, source)));
					source.append(MessageFormat.format("\tpublic static class {0}_reply_redirect extends {1}_reply_redirect '{' '}'\n", genName, last.getGenNameValue(aData, source)));
				}
				if (((Signature_Type) last).getSignatureExceptions() != null) {
					source.append(MessageFormat.format("\tpublic static class {0}_template extends {1} '{' '}'\n", genName, last.getGenNameTemplate(aData, source)));
				}
				break;
			default:
				source.append(MessageFormat.format("\tpublic static class {0} extends {1} '{' '}'\n", ownName, last.getGenNameValue(aData, source)));
				source.append(MessageFormat.format("\tpublic static class {0}_template extends {1} '{' '}'\n", ownName, last.getGenNameTemplate(aData, source)));
			}
		}

		if (!isAsn()) {
			if (hasDoneAttribute()) {
				generateCodeDone(aData, source);
			}
			if (subType != null) {
				subType.generateCode(aData, source);
			}
		}

		generateCodeForCodingHandlers(aData, source);
	}

	@Override
	/** {@inheritDoc} */
	public void generateCodeIsPresentBoundChosen(final JavaGenData aData, final ExpressionStruct expression, final List<ISubReference> subreferences,
			final int subReferenceIndex, final String globalId, final String externalId, final boolean isTemplate, final Operation_type optype, final String field, final Scope targetScope) {
		if (this == refdType || refdType == null) {
			ErrorReporter.INTERNAL_ERROR("Code generator reached erroneous type reference `" + getFullName() + "''");
			expression.expression.append("FATAL_ERROR encountered while processing `" + getFullName() + "''\n");
			return;
		}

		refdType.generateCodeIsPresentBoundChosen(aData, expression, subreferences, subReferenceIndex, globalId, externalId, isTemplate, optype, field, targetScope);
	}
}
