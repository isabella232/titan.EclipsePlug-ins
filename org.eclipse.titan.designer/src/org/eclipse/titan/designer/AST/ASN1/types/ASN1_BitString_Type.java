/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.ASN1.types;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.titan.common.parsers.SyntacticErrorStorage;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.ArraySubReference;
import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.Assignments;
import org.eclipse.titan.designer.AST.FieldSubReference;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.IReferencingType;
import org.eclipse.titan.designer.AST.ISubReference;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.IValue.Value_type;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.AST.ParameterisedSubReference;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.ReferenceChain;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.TypeCompatibilityInfo;
import org.eclipse.titan.designer.AST.Value;
import org.eclipse.titan.designer.AST.ASN1.ASN1Type;
import org.eclipse.titan.designer.AST.ASN1.Block;
import org.eclipse.titan.designer.AST.ASN1.IASN1Type;
import org.eclipse.titan.designer.AST.ASN1.values.Named_Bits;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.templates.ITTCN3Template;
import org.eclipse.titan.designer.AST.TTCN3.types.BitString_Type;
import org.eclipse.titan.designer.AST.TTCN3.values.Bitstring_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Expression_Value.Operation_type;
import org.eclipse.titan.designer.AST.TTCN3.values.Integer_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.NamedValue;
import org.eclipse.titan.designer.AST.TTCN3.values.NamedValues;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.ExpressionStruct;
import org.eclipse.titan.designer.compiler.JavaGenData;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ParserMarkerSupport;
import org.eclipse.titan.designer.parsers.asn1parser.Asn1Parser;
import org.eclipse.titan.designer.parsers.asn1parser.BlockLevelTokenStreamTracker;

/**
 * @author Kristof Szabados
 * @author Arpad Lovassy
 */
public final class ASN1_BitString_Type extends ASN1Type {
	private static final String BITSTRINGVALUEEXPECTED1 = "BIT STRING value was expected";
	private static final String BITSTRINGVALUEEXPECTED2 = "bitstring value was expected";

	private final Block mBlock;
	private NamedValues namedValues;

	public ASN1_BitString_Type(final Block aBlock) {
		this.mBlock = aBlock;
		if (null != aBlock) {
			aBlock.setFullNameParent(this);
		}
	}

	public IASN1Type newInstance() {
		return new ASN1_BitString_Type(mBlock);
	}

	@Override
	/** {@inheritDoc} */
	public Type_type getTypetype() {
		return Type_type.TYPE_BITSTRING_A;
	}

	@Override
	/** {@inheritDoc} */
	public Type_type getTypetypeTtcn3() {
		return Type_type.TYPE_BITSTRING;
	}

	@Override
	/** {@inheritDoc} */
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		if (null != namedValues) {
			namedValues.setMyScope(scope);
		}
	}

	@Override
	/** {@inheritDoc} */
	public StringBuilder getFullName(final INamedNode child) {
		final StringBuilder builder = super.getFullName(child);

		if (child == namedValues) {
			builder.append(".<namedvalues>");
		}

		return builder;
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

		return Type_type.TYPE_BITSTRING.equals(temp.getTypetype()) || Type_type.TYPE_BITSTRING_A.equals(temp.getTypetype());
	}

	@Override
	/** {@inheritDoc} */
	public String getTypename() {
		return "bitstring";
	}

	@Override
	/** {@inheritDoc} */
	public String getOutlineIcon() {
		return "bitstring.gif";
	}

	@Override
	/** {@inheritDoc} */
	public StringBuilder getProposalDescription(final StringBuilder builder) {
		return builder.append("bit string");
	}

	@Override
	/** {@inheritDoc} */
	public void check(final CompilationTimeStamp timestamp) {
		if (null != lastTimeChecked && !lastTimeChecked.isLess(timestamp)) {
			return;
		}

		lastTimeChecked = timestamp;
		if (null != myScope) {
			final Module module = myScope.getModuleScope();
			if (null != module) {
				if (module.getSkippedFromSemanticChecking()) {
					return;
				}
			}
		}
		isErroneous = false;

		if (null == namedValues) {
			parseBlockBitstring();
		}

		if (isErroneous || null == namedValues) {
			return;
		}

		/* check named bits */

		final Map<String, Identifier> nameMap = new HashMap<String, Identifier>();
		for (int i = 0, size = namedValues.getSize(); i < size; i++) {
			final NamedValue namedValue = namedValues.getNamedValueByIndex(i);
			final Identifier identifier = namedValue.getName();
			if (nameMap.containsKey(identifier.getName())) {
				final Location tempLocation = nameMap.get(identifier.getName()).getLocation();
				tempLocation.reportSingularSemanticError(MessageFormat.format(Assignments.DUPLICATEDEFINITIONFIRST,
						identifier.getDisplayName()));
				identifier.getLocation().reportSemanticError(
						MessageFormat.format(Assignments.DUPLICATEDEFINITIONREPEATED, identifier.getDisplayName()));
			} else {
				nameMap.put(identifier.getName(), identifier);
			}
		}

		final Map<Integer, NamedValue> valueMap = new HashMap<Integer, NamedValue>();

		for (int i = 0, size = namedValues.getSize(); i < size; i++) {
			final NamedValue namedValue = namedValues.getNamedValueByIndex(i);
			final IValue value = namedValue.getValue();

			final IReferenceChain referenceChain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
			final IValue last = value.getValueRefdLast(timestamp, referenceChain);
			referenceChain.release();

			if (last.getIsErroneous(timestamp)) {
				continue;
			}

			switch (last.getValuetype()) {
			case INTEGER_VALUE: {
				final Integer_Value integerValue = (Integer_Value) last;
				if (integerValue.isNative()) {
					final int intValue = integerValue.intValue();
					final Integer intValueObject = Integer.valueOf(intValue);
					if (intValue < 0) {
						value.getLocation().reportSemanticError(
								MessageFormat.format(
										"A non-negative INTEGER value was expected for named bit `{0}'' instead of {1}",
										namedValue.getName().getDisplayName(), intValueObject));
						value.setIsErroneous(true);
						continue;
					}
					if (valueMap.containsKey(intValueObject)) {
						value.getLocation().reportSemanticError(
								MessageFormat.format("Duplicate value {0} for named bit `{1}''", intValueObject,
										namedValue.getName().getDisplayName()));
						value.setIsErroneous(true);
						final NamedValue temp = valueMap.get(intValueObject);
						temp.getLocation().reportSemanticError(
								MessageFormat.format("Bit {0} is already assigned to name `{1}''", intValueObject,
										temp.getName().getDisplayName()));
					} else {
						valueMap.put(intValueObject, namedValue);
					}
				} else {
					value.getLocation().reportSemanticError(
							MessageFormat.format("INTEGER value `{0}'' is too big to be used as a named bit",
									integerValue.getValueValue()));
					value.setIsErroneous(true);
				}
				break;
			}
			default:
				namedValue.getLocation().reportSemanticError(
						MessageFormat.format("INTEGER value was expected for named bit `{0}''", namedValue.getName()
								.getDisplayName()));
				break;
			}
		}

		nameMap.clear();

		if (null != constraints) {
			constraints.check(timestamp);
		}

		if (myScope != null) {
			checkEncode(timestamp);
			checkVariants(timestamp);
		}
	}

	@Override
	/** {@inheritDoc} */
	public boolean checkThisValue(final CompilationTimeStamp timestamp, final IValue value, final Assignment lhs, final ValueCheckingOptions valueCheckingOptions) {
		final boolean selfReference = super.checkThisValue(timestamp, value, lhs, valueCheckingOptions);

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

		if (last.isAsn()) {
			if (last instanceof IReferencingType) {
				final IType type = last.getMyGovernor().getTypeRefdLast(timestamp);
				switch (type.getTypetype()) {
				case TYPE_BITSTRING:
				case TYPE_BITSTRING_A:
					break;
				default:
					value.getLocation().reportSemanticError("(reference to) BIT STRING value was expected");
					value.setIsErroneous(true);
					value.setLastTimeChecked(timestamp);
					return selfReference;
				}
			}
			switch (last.getValuetype()) {
			case BITSTRING_VALUE:
				break;
			case HEXSTRING_VALUE:
				last.setValuetype(timestamp, Value_type.BITSTRING_VALUE);
				break;
			case UNDEFINED_BLOCK: {
				last = last.setValuetype(timestamp, Value_type.NAMED_BITS);
				if (namedValues == null) {
					value.getLocation().reportSemanticError(
							MessageFormat.format("No named bits are defined in type `{0}''", getTypename()));
					value.setIsErroneous(true);
					value.setLastTimeChecked(timestamp);
					return selfReference;
				}
				final Named_Bits namedBits = (Named_Bits) last;
				final StringBuilder builder = new StringBuilder(namedBits.getNofIds());
				for (int i = 0; i < namedBits.getNofIds(); i++) {
					final Identifier id = namedBits.getIdByIndex(i);
					if (!namedValues.hasNamedValueWithName(id)) {
						id.getLocation().reportSemanticError(
								MessageFormat.format("No named bit with name `{0}'' is defined in type `{1}''",
										id.getDisplayName(), getTypename()));
						value.setIsErroneous(true);
						value.setLastTimeChecked(timestamp);
						return selfReference;
					}

					IValue tempValue = namedValues.getNamedValueByName(id).getValue();
					final ReferenceChain referenceChain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
					tempValue = tempValue.getValueRefdLast(timestamp, referenceChain);
					referenceChain.release();
					if (!tempValue.getIsErroneous(timestamp) && Value_type.INTEGER_VALUE.equals(tempValue.getValuetype())) {
						final int bitIndex = ((Integer_Value) tempValue).intValue();
						while (builder.length() <= bitIndex) {
							builder.append('0');
						}
						builder.setCharAt(bitIndex, '1');
					} else {
						// FIXME Most probably we were not able to build the semantic structure for something, because it is not yet supported, like referenced values in sets
					}
				}

				last = new Bitstring_Value(builder.toString());
				last.copyGeneralProperties(value);
				namedBits.setRealValue((Bitstring_Value) last);
				break;
			}
			default:
				value.getLocation().reportSemanticError(BITSTRINGVALUEEXPECTED1);
				value.setIsErroneous(true);
				value.setLastTimeChecked(timestamp);
				return selfReference;
			}
		} else {
			switch (last.getValuetype()) {
			case BITSTRING_VALUE:
				break;
			case EXPRESSION_VALUE:
			case MACRO_VALUE:
				// already checked
				break;
			default:
				value.getLocation().reportSemanticError(BITSTRINGVALUEEXPECTED2);
				value.setIsErroneous(true);
			}
		}

		if (valueCheckingOptions.sub_check) {
			// there is no parent type to check
			if (subType != null) {
				subType.checkThisValue(timestamp, last);
			}
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

		switch (template.getTemplatetype()) {
		case BSTR_PATTERN:
			break;
		default:
			template.getLocation().reportSemanticError(
					MessageFormat.format(BitString_Type.TEMPLATENOTALLOWED, template.getTemplateTypeName()));
			break;
		}

		return false;
	}

	private void parseBlockBitstring() {
		if (null == mBlock) {
			return;
		}

		final Asn1Parser parser = BlockLevelTokenStreamTracker.getASN1ParserForBlock(mBlock);
		if (null == parser) {
			return;
		}

		namedValues = parser.pr_special_NamedBitList().namedValues;
		final List<SyntacticErrorStorage> errors = parser.getErrorStorage();
		if (null != errors && !errors.isEmpty()) {
			isErroneous = true;
			namedValues = null;
			for (int i = 0; i < errors.size(); i++) {
				ParserMarkerSupport.createOnTheFlyMixedMarker((IFile) mBlock.getLocation().getFile(), errors.get(i),
						IMarker.SEVERITY_ERROR);
			}
		}

		if (namedValues != null) {
			namedValues.setFullNameParent(this);
			namedValues.setMyScope(getMyScope());
		}
	}

	@Override
	/** {@inheritDoc} */
	public boolean canHaveCoding(final CompilationTimeStamp timestamp, final MessageEncoding_type coding, final IReferenceChain refChain) {
		if (coding == MessageEncoding_type.BER) {
			return hasEncoding(timestamp, MessageEncoding_type.BER, null);
		}

		switch (coding) {
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
			if (subreferences.size() > actualSubReference + 1) {
				subreference.getLocation().reportSemanticError(ArraySubReference.INVALIDSTRINGELEMENTINDEX);
				return null;
			} else if (subreferences.size() == actualSubReference + 1) {
				reference.setStringElementReferencing();
			}

			final Value indexValue = ((ArraySubReference) subreference).getValue();
			checkStringIndex(timestamp, indexValue, expectedIndex, refChain);

			return this;
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
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		super.findReferences(referenceFinder, foundIdentifiers);
		if (namedValues != null) {
			namedValues.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	@Override
	/** {@inheritDoc} */
	protected boolean memberAccept(final ASTVisitor v) {
		if (!super.memberAccept(v)) {
			return false;
		}
		if (namedValues != null && !namedValues.accept(v)) {
			return false;
		}
		return true;
	}

	@Override
	/** {@inheritDoc} */
	public void generateCode( final JavaGenData aData, final StringBuilder source ) {
		generateCodeTypedescriptor(aData, source);
		if(needsAlias()) {
			final String ownName = getGenNameOwn();
			source.append(MessageFormat.format("\tpublic static class {0} extends {1} '{' '}'\n", ownName, getGenNameValue(aData, source, myScope)));
			source.append(MessageFormat.format("\tpublic static class {0}_template extends {1} '{' '}'\n", ownName, getGenNameTemplate(aData, source, myScope)));
		}

		generateCodeForCodingHandlers(aData, source);
	}

	@Override
	/** {@inheritDoc} */
	public String getGenNameValue(final JavaGenData aData, final StringBuilder source, final Scope scope) {
		aData.addBuiltinTypeImport( "TitanBitString" );

		return "TitanBitString";
	}

	@Override
	/** {@inheritDoc} */
	public String getGenNameTemplate(final JavaGenData aData, final StringBuilder source, final Scope scope) {
		aData.addBuiltinTypeImport( "TitanBitString_template" );

		return "TitanBitString_template";
	}

	@Override
	/** {@inheritDoc} */
	public void generateCodeIsPresentBoundChosen(final JavaGenData aData, final ExpressionStruct expression, final List<ISubReference> subreferences,
			final int subReferenceIndex, final String globalId, final String externalId, final boolean isTemplate, final Operation_type optype, final String field) {
		generateCodeIspresentBound_forStrings(aData, expression, subreferences, subReferenceIndex, globalId, externalId, isTemplate, optype, field);
	}

	@Override
	/** {@inheritDoc} */
	public String internalGetGenNameTypeDescriptor(final JavaGenData aData, final StringBuilder source, final Scope scope) {
		aData.addBuiltinTypeImport( "Base_Type" );
		return "Base_Type.TitanBitString";
	}
}
