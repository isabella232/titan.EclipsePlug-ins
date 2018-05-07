/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
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
import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.Assignment.Assignment_type;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.IReferencingType;
import org.eclipse.titan.designer.AST.ISetting;
import org.eclipse.titan.designer.AST.ISubReference;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.IValue.Value_type;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.AST.NULL_Location;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.ReferenceChain;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.Type;
import org.eclipse.titan.designer.AST.TypeCompatibilityInfo;
import org.eclipse.titan.designer.AST.ASN1.ASN1Type;
import org.eclipse.titan.designer.AST.ASN1.IASN1Type;
import org.eclipse.titan.designer.AST.ASN1.Type_Assignment;
import org.eclipse.titan.designer.AST.ASN1.Undefined_Assignment;
import org.eclipse.titan.designer.AST.ASN1.definitions.SpecialASN1Module;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.attributes.RawAST;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Type;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Definition;
import org.eclipse.titan.designer.AST.TTCN3.templates.ITTCN3Template;
import org.eclipse.titan.designer.AST.TTCN3.values.Expression_Value.Operation_type;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.ExpressionStruct;
import org.eclipse.titan.designer.compiler.JavaGenData;
import org.eclipse.titan.designer.editors.ProposalCollector;
import org.eclipse.titan.designer.editors.actions.DeclarationCollector;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * @author Kristof Szabados
 * */
public final class Referenced_Type extends ASN1Type implements IReferencingType {

	private final Reference reference;
	private IType refd;
	private IType refdLast;

	private boolean componentInternal;

	public Referenced_Type(final Reference reference) {
		this.reference = reference;
		componentInternal = false;

		if (reference != null) {
			reference.setFullNameParent(this);
			setLocation(reference.getLocation());
			setMyScope(reference.getMyScope());
		}
	}

	@Override
	/** {@inheritDoc} */
	public Type_type getTypetype() {
		return Type_type.TYPE_REFERENCED;
	}

	public Reference getReference() {
		return reference;
	}

	@Override
	// Location is optimized not to store an object that it is not needed
	public Location getLocation() {
		if (reference != null && reference.getLocation() != null) {
			return new Location(reference.getLocation());
		}

		return NULL_Location.INSTANCE;
	}

	@Override
	/** {@inheritDoc} */
	public void setLocation(final Location location) {
		//Do nothing
	}

	@Override
	/** {@inheritDoc} */
	public IASN1Type newInstance() {
		return new Referenced_Type(reference);
	}

	@Override
	/** {@inheritDoc} */
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		if (reference != null) {
			reference.setMyScope(scope);
		}
	}

	@Override
	/** {@inheritDoc} */
	public String chainedDescription() {
		return "type reference: " + reference;
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
	public boolean isIdentical(final CompilationTimeStamp timestamp, final IType type) {
		check(timestamp);
		type.check(timestamp);
		final IType t1 = getTypeRefdLast(timestamp);
		final IType t2 = type.getTypeRefdLast(timestamp);

		if (t1.getIsErroneous(timestamp) || t2.getIsErroneous(timestamp)) {
			return true;
		}

		return t1.isIdentical(timestamp, t2);
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
		if (isErroneous || refdLast == null || refdLast == this) {
			return "Referenced type";
		}

		return refdLast.getTypename();
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

		if (refdLast != null && this != refdLast) {
			final Expected_Value_type internalExpectation =
					expectedIndex == Expected_Value_type.EXPECTED_TEMPLATE ? Expected_Value_type.EXPECTED_DYNAMIC_VALUE : expectedIndex;
			final IType temp = refdLast.getFieldType(timestamp, reference, actualSubReference, internalExpectation, refChain, interruptIfOptional);
			if (reference.getIsErroneous(timestamp)) {
				setIsErroneous(true);
			}
			return temp;
		}

		return this;
	}

	@Override
	/** {@inheritDoc} */
	public boolean getSubrefsAsArray(final CompilationTimeStamp timestamp, final Reference reference, final int actualSubReference,
			final List<Integer> subrefsArray, final List<IType> typeArray) {
		if (reference.getSubreferences().size() == 1) {
			return true;
		}

		if (this == refdLast) {
			return false;
		}

		return refdLast.getSubrefsAsArray(timestamp, reference, actualSubReference, subrefsArray, typeArray);
	}

	@Override
	/** {@inheritDoc} */
	public boolean getFieldTypesAsArray(final Reference reference, final int actualSubReference, final List<IType> typeArray) {
		if (reference.getSubreferences().size() == 1) {
			return true;
		}
		if (this == refdLast || refdLast == null) {
			return false;
		}
		return refdLast.getFieldTypesAsArray(reference, actualSubReference, typeArray);
	}

	@Override
	/** {@inheritDoc} */
	public StringBuilder getProposalDescription(final StringBuilder builder) {
		Assignment ass;
		if (lastTimeChecked == null) {
			ass = reference.getRefdAssignment(CompilationTimeStamp.getBaseTimestamp(), true);
		} else {
			ass = reference.getRefdAssignment(lastTimeChecked, true);
		}

		if (ass != null && Assignment_type.A_TYPE.semanticallyEquals(ass.getAssignmentType())) {
			if (ass instanceof Def_Type) {
				final Def_Type defType = (Def_Type) ass;
				return builder.append(defType.getIdentifier().getDisplayName());
			} else if (ass instanceof Type_Assignment) {
				return builder.append(((Type_Assignment) ass).getIdentifier().getDisplayName());
			}
		}

		return builder.append("unknown_referred_type");
	}

	@Override
	/** {@inheritDoc} */
	public boolean isComponentInternal(final CompilationTimeStamp timestamp) {
		check(timestamp);

		return componentInternal;
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
		refd = null;
		refdLast = null; //Do not remove! Intentionally set for null to avoid checking circle.

		parseAttributes(timestamp);

		refdLast = getTypeRefdLast(timestamp);

		if (refdLast != null && !refdLast.getIsErroneous(timestamp)) {
			refdLast.check(timestamp);
			componentInternal = refdLast.isComponentInternal(timestamp);
		}

		if (constraints != null) {
			constraints.check(timestamp);
		}

		final IType typeLast = getTypeRefdLast(timestamp);
		final IReferenceChain tempReferenceChain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
		final IType typeParent = getTypeRefd(timestamp, tempReferenceChain);
		tempReferenceChain.release();
		if (!typeLast.getIsErroneous(timestamp) && !typeParent.getIsErroneous(timestamp)) {
			checkSubtypeRestrictions(timestamp, typeLast.getSubtypeType(), typeParent.getSubtype());
		}

		if (myScope != null) {
			checkEncode(timestamp);
			checkVariants(timestamp);
		}
	}

	@Override
	/** {@inheritDoc} */
	public void checkComponentInternal(final CompilationTimeStamp timestamp, final Set<IType> typeSet, final String operation) {
		final IType last = getTypeRefdLast(timestamp);

		if (last != null && !last.getIsErroneous(timestamp) && last != this) {
			last.checkComponentInternal(timestamp, typeSet, operation);
		}
	}

	@Override
	/** {@inheritDoc} */
	public void checkEmbedded(final CompilationTimeStamp timestamp, final Location errorLocation, final boolean defaultAllowed,
			final String errorMessage) {
		final IType last = getTypeRefdLast(timestamp);

		if (last != null && !last.getIsErroneous(timestamp) && last != this) {
			last.checkEmbedded(timestamp, errorLocation, defaultAllowed, errorMessage);
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
					valueCheckingOptions.incomplete_allowed, valueCheckingOptions.omit_allowed, false, valueCheckingOptions.implicit_omit,
					valueCheckingOptions.str_elem));
			final Definition def = value.getDefiningAssignment();
			if (def != null) {
				final String referingModuleName = getMyScope().getModuleScope().getName();
				if (!def.referingHere.contains(referingModuleName)) {
					def.referingHere.add(referingModuleName);
				}
			}
		}

		if (valueCheckingOptions.sub_check && (subType != null)) {
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
		if (refChain.add(this) && reference != null && !getIsErroneous(timestamp)) {
			if (refd != null) {
				return refd;
			}

			Assignment ass = reference.getRefdAssignment(timestamp, true, refChain);

			if (ass != null && Assignment_type.A_UNDEF.semanticallyEquals(ass.getAssignmentType())) {
				ass = ((Undefined_Assignment) ass).getRealAssignment(timestamp);
			}

			if (ass == null || ass.getIsErroneous()) {
				// The referenced assignment was not found, or is erroneous
				isErroneous = true;
				lastTimeChecked = timestamp;
				return this;
			}

			switch (ass.getAssignmentType()) {
			case A_TYPE: {
				IType tempType = ass.getType(timestamp);
				if (tempType != null) {
					if (!tempType.getIsErroneous(timestamp)) {
						tempType.check(timestamp);
						tempType = tempType.getFieldType(timestamp, reference, 1, Expected_Value_type.EXPECTED_DYNAMIC_VALUE, refChain, false);
						if (tempType == null) {
							setIsErroneous(true);
							return this;
						}

						refd = tempType;
						return refd;
					}
				}
				break;
			}
			case A_VS: {
				final IType tempType = ass.getType(timestamp);
				if (tempType == null) {
					isErroneous = true;
					lastTimeChecked = timestamp;
					return this;
				}

				refd = tempType;
				return refd;
			}
			case A_OC:
			case A_OBJECT:
			case A_OS:
				final ISetting setting = reference.getRefdSetting(timestamp);
				if (setting == null || setting.getIsErroneous(timestamp)) {
					isErroneous = true;
					lastTimeChecked = timestamp;
					return this;
				}

				if (!Setting_type.S_T.equals(setting.getSettingtype())) {
					reference.getLocation().reportSemanticError(MessageFormat.format(TYPEREFERENCEEXPECTED, reference.getDisplayName()));
					isErroneous = true;
					lastTimeChecked = timestamp;
					return this;
				}

				refd = (Type) setting;

				if (refd.getOwnertype() == TypeOwner_type.OT_UNKNOWN) {
					refd.setOwnertype(TypeOwner_type.OT_REF, this);
				}

				return refd;
			default:
				reference.getLocation().reportSemanticError(MessageFormat.format(TYPEREFERENCEEXPECTED, reference.getDisplayName()));
				break;
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
	public void checkRecursions(final CompilationTimeStamp timestamp, final IReferenceChain referenceChain) {
		if (referenceChain.add(this)) {
			final IReferenceChain tempReferenceChain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
			final IType t = getTypeRefd(timestamp, tempReferenceChain);
			tempReferenceChain.release();

			if (t != null && !t.getIsErroneous(timestamp) && !this.equals(t)) {
				t.checkRecursions(timestamp, referenceChain);
			}
		}
	}

	@Override
	/** {@inheritDoc} */
	public void checkCodingAttributes(final CompilationTimeStamp timestamp, final IReferenceChain refChain) {
		if (refdLast == null || refdLast.getIsErroneous(CompilationTimeStamp.getBaseTimestamp()) || refdLast == this) {
			return;
		}

		//check raw attributes
		if (subType != null) {
			final int restrictionLength = subType.get_length_restriction();
			if (restrictionLength != -1) {
				if (rawAttribute == null) {
					rawAttribute = new RawAST(getDefaultRawFieldLength());
				}
				if (rawAttribute.fieldlength == 0) {
					rawAttribute.fieldlength = restrictionLength;
					rawAttribute.length_restriction = -1;
				} else {
					rawAttribute.length_restriction = restrictionLength;
				}
			}
		}

		if (rawAttribute == null) {
			return;
		}

		refd.forceRaw(timestamp);
		if (rawAttribute.fieldlength == 0 && rawAttribute.length_restriction != -1) {
			switch (refdLast.getTypetype()) {
			case TYPE_BITSTRING:
				rawAttribute.fieldlength = rawAttribute.length_restriction;
				rawAttribute.length_restriction = -1;
				break;
			case TYPE_HEXSTRING:
				rawAttribute.fieldlength = rawAttribute.length_restriction * 4;
				rawAttribute.length_restriction = -1;
				break;
			case TYPE_OCTETSTRING:
				rawAttribute.fieldlength = rawAttribute.length_restriction * 8;
				rawAttribute.length_restriction = -1;
				break;
			case TYPE_CHARSTRING:
			case TYPE_UCHARSTRING:
				rawAttribute.fieldlength = rawAttribute.length_restriction * 8;
				rawAttribute.length_restriction = -1;
				break;
			case TYPE_SEQUENCE_OF:
			case TYPE_SET_OF:
				rawAttribute.fieldlength = rawAttribute.length_restriction;
				rawAttribute.length_restriction = -1;
				break;
			default:
				break;
			}
		}
		//TODO add checks for other encodings.
		

		if (refChain.contains(this)) {
			return;
		}

		refdLast.checkCodingAttributes(timestamp, refChain);
	}

	@Override
	/** {@inheritDoc} */
	public int getDefaultRawFieldLength() {
		if (refdLast != null && !refdLast.getIsErroneous(CompilationTimeStamp.getBaseTimestamp()) && refdLast != this) {
			return refdLast.getDefaultRawFieldLength();
		}

		return 0;
	}

	@Override
	/** {@inheritDoc} */
	public int getRawLength() {
		if (refdLast != null && !refdLast.getIsErroneous(CompilationTimeStamp.getBaseTimestamp()) && refdLast != this) {
			return refdLast.getRawLength();
		}

		return -1;
	}

	@Override
	/** {@inheritDoc} */
	public int getLengthMultiplier() {
		if (refdLast != null && !refdLast.getIsErroneous(CompilationTimeStamp.getBaseTimestamp()) && refdLast != this) {
			return refdLast.getLengthMultiplier();
		}

		return 1;
	}

	@Override
	/** {@inheritDoc} */
	public void forceRaw(final CompilationTimeStamp timestamp) {
		if (refd != null && !refd.getIsErroneous(CompilationTimeStamp.getBaseTimestamp()) && refdLast != this) {
			refd.forceRaw(timestamp);
		}
	}

	/**
	 * Searches and adds a completion proposal to the provided collector if a
	 * valid one is found.
	 * <p>
	 * The type referred last is identified, and the job of adding a proposal is
	 * delegated to it.
	 *
	 * @param propCollector the proposal collector to add the proposal to, and
	 *            used to get more information
	 * @param i index, used to identify which element of the reference (used by
	 *            the proposal collector) should be checked for completions.
	 * */
	@Override
	/** {@inheritDoc} */
	public void addProposal(final ProposalCollector propCollector, final int i) {
		if (lastTimeChecked == null) {
			check(CompilationTimeStamp.getBaseTimestamp());
		}

		if (refdLast != null && !this.equals(refdLast)) {
			refdLast.addProposal(propCollector, i);
		}
	}

	/**
	 * Searches and adds a declaration proposal to the provided collector if a
	 * valid one is found.
	 * <p>
	 * The type referred last is identified, and the job of adding a declaration
	 * is delegated to it.
	 *
	 * @param declarationCollector the declaration collector to add the
	 *            declaration to, and used to get more information.
	 * @param i index, used to identify which element of the reference (used by
	 *            the declaration collector) should be checked.
	 * */
	@Override
	/** {@inheritDoc} */
	public void addDeclaration(final DeclarationCollector declarationCollector, final int i) {
		if (lastTimeChecked == null) {
			check(CompilationTimeStamp.getBaseTimestamp());
		}

		if (refdLast != null && !this.equals(refdLast)) {
			refdLast.addDeclaration(declarationCollector, i);
		}
	}

	@Override
	/** {@inheritDoc} */
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}

		reference.updateSyntax(reparser, false);
		reparser.updateLocation(reference.getLocation());

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
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		super.findReferences(referenceFinder, foundIdentifiers);
		if (reference != null) {
			reference.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	@Override
	/** {@inheritDoc} */
	protected boolean memberAccept(final ASTVisitor v) {
		if (!super.memberAccept(v)) {
			return false;
		}
		if (reference!=null && !reference.accept(v)) {
			return false;
		}
		return true;
	}

	@Override
	/** {@inheritDoc} */
	public String getGenNameValue(final JavaGenData aData, final StringBuilder source, final Scope scope) {
		if (this == refd || refd == null || refdLast == null) {
			ErrorReporter.INTERNAL_ERROR("Code generator reached erroneous type reference `" + getFullName() + "''");
			return "FATAL_ERROR encountered";
		}

		final Module refdModule = refdLast.getMyScope().getModuleScope();
		if (refdModule != scope.getModuleScope() && !SpecialASN1Module.isSpecAsss(refdModule)) {
			aData.addInterModuleImport(refdModule.getName());
		}
		return refd.getGenNameValue(aData, source, scope);
	}

	@Override
	/** {@inheritDoc} */
	public String getGenNameTemplate(final JavaGenData aData, final StringBuilder source, final Scope scope) {
		if (this == refd || refd == null || refdLast == null) {
			ErrorReporter.INTERNAL_ERROR("Code generator reached erroneous type reference `" + getFullName() + "''");
			return "FATAL_ERROR encountered";
		}

		final Module refdModule = refdLast.getMyScope().getModuleScope();
		if (refdModule != scope.getModuleScope() && !SpecialASN1Module.isSpecAsss(refdModule)) {
			aData.addInterModuleImport(refdModule.getName());
		}
		return refd.getGenNameTemplate(aData, source, scope);
	}

	@Override
	/** {@inheritDoc} */
	public String getGenNameRawDescriptor(final JavaGenData aData, final StringBuilder source) {
		if (this == refd || refd == null || refdLast == null) {
			ErrorReporter.INTERNAL_ERROR("Code generator reached erroneous type reference `" + getFullName() + "''");

			return "FATAL_ERROR encountered";
		}

		if (rawAttribute != null) {
			generateCodeRawDescriptor(aData, source);

			return getGenNameOwn(myScope) + "_raw_";
		}

		return refd.getGenNameRawDescriptor(aData, source);
	}

	@Override
	/** {@inheritDoc} */
	public void generateCode( final JavaGenData aData, final StringBuilder source ) {
		generateCodeTypedescriptor(aData, source);
		if(needsAlias()) {
			final String ownName = getGenNameOwn();
			final IType last = getTypeRefdLast(CompilationTimeStamp.getBaseTimestamp());
			switch (last.getTypetype()) {
			case TYPE_PORT:
				source.append(MessageFormat.format("\tpublic static class {0} extends {1} '{' '}'\n", ownName, getGenNameValue(aData, source, myScope)));
				break;
			case TYPE_SIGNATURE:
				source.append(MessageFormat.format("\tpublic static class {0}_call extends {1}_call '{' '}'\n", genName, getGenNameValue(aData, source, myScope)));
				if (!((Signature_Type) last).isNonblocking()) {
					source.append(MessageFormat.format("\tpublic static class {0}_reply extends {1}_reply '{' '}'\n", genName, getGenNameValue(aData, source, myScope)));
				}
				if (((Signature_Type) last).getSignatureExceptions() != null) {
					source.append(MessageFormat.format("\tpublic static class {0}_template extends {1} '{' '}'\n", genName, getGenNameTemplate(aData, source, myScope)));
				}
				//FIXME *-redirect -s are missing
				break;
			default:
				source.append(MessageFormat.format("\tpublic static class {0} extends {1} '{' '}'\n", ownName, getGenNameValue(aData, source, myScope)));
				source.append(MessageFormat.format("\tpublic static class {0}_template extends {1} '{' '}'\n", ownName, getGenNameTemplate(aData, source, myScope)));
			}

			//TODO: implement: package of the imported class is unknown
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
	public void generateCodeTypedescriptor(final JavaGenData aData, final StringBuilder source) {
		// FIXME needs to care for other coding attributes too.
		if (rawAttribute != null) {
			super.generateCodeTypedescriptor(aData, source);
		}
	}

	@Override
	/** {@inheritDoc} */
	public void generateCodeIsPresentBoundChosen(final JavaGenData aData, final ExpressionStruct expression, final List<ISubReference> subreferences,
			final int subReferenceIndex, final String globalId, final String externalId, final boolean isTemplate, final Operation_type optype, final String field) {
		if (this == refdLast || refdLast == null) {
			ErrorReporter.INTERNAL_ERROR("Code generator reached erroneous type reference `" + getFullName() + "''");
			expression.expression.append("FATAL_ERROR encountered");
			return;
		}

		refdLast.generateCodeIsPresentBoundChosen(aData, expression, subreferences, subReferenceIndex, globalId, externalId, isTemplate, optype, field);
	}

	@Override
	/** {@inheritDoc} */
	public boolean isPresentAnyvalueEmbeddedField(final ExpressionStruct expression, final List<ISubReference> subreferences, final int beginIndex) {
		if (this == refdLast || refdLast == null) {
			ErrorReporter.INTERNAL_ERROR("Code generator reached erroneous type reference `" + getFullName() + "''");
			expression.expression.append("FATAL_ERROR encountered");
			return false;
		}

		return refdLast.isPresentAnyvalueEmbeddedField(expression, subreferences, beginIndex);
	}
}
