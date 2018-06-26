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
import org.eclipse.titan.designer.AST.ASN1.values.Named_Integer_Value;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.templates.ITTCN3Template;
import org.eclipse.titan.designer.AST.TTCN3.templates.ITTCN3Template.Template_type;
import org.eclipse.titan.designer.AST.TTCN3.templates.ValueRange;
import org.eclipse.titan.designer.AST.TTCN3.templates.Value_Range_Template;
import org.eclipse.titan.designer.AST.TTCN3.values.Integer_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.NamedValue;
import org.eclipse.titan.designer.AST.TTCN3.values.NamedValues;
import org.eclipse.titan.designer.AST.TTCN3.values.Real_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Undefined_LowerIdentifier_Value;
import org.eclipse.titan.designer.compiler.JavaGenData;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ParserMarkerSupport;
import org.eclipse.titan.designer.parsers.asn1parser.Asn1Parser;
import org.eclipse.titan.designer.parsers.asn1parser.BlockLevelTokenStreamTracker;

/**
 * @author Kristof Szabados
 * @author Arpad Lovassy
 */
public final class ASN1_Integer_Type extends ASN1Type {
	private static final String INTEGERVALUEEXPECTED = "INTEGER value was expected";
	private static final String TEMPLATENOTALLOWED = "{0} cannot be used for type `integer''";
	private static final String LENGTHRESTRICTIONNOTALLOWED = "Length restriction is not allowed for type `integer''";
	private static final String INCORRECTBOUNDARIES = "The lower boundary is higher than the upper boundary";
	private static final String INCORRECTLOWERBOUNDARY = "The lower boundary cannot be infinity";
	private static final String INCORRECTUPPERBOUNDARY = "The upper boundary cannot be -infinity";

	private final Block mBlock;
	private NamedValues namedNumbers;

	private static enum BOUNDARY_TYPE {
		LOWER, UPPER
	}

	public ASN1_Integer_Type() {
		this.mBlock = null;
	}

	public ASN1_Integer_Type(final Block aBlock) {
		this.mBlock = aBlock;
	}

	public IASN1Type newInstance() {
		return new ASN1_Integer_Type(mBlock);
	}

	@Override
	/** {@inheritDoc} */
	public Type_type getTypetype() {
		return Type_type.TYPE_INTEGER_A;
	}

	@Override
	/** {@inheritDoc} */
	public Type_type getTypetypeTtcn3() {
		return Type_type.TYPE_INTEGER;
	}

	@Override
	/** {@inheritDoc} */
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		if (null != namedNumbers) {
			namedNumbers.setMyScope(scope);
		}
	}

	@Override
	/** {@inheritDoc} */
	public StringBuilder getFullName(final INamedNode child) {
		final StringBuilder builder = super.getFullName(child);

		if (child == namedNumbers) {
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

		return Type_type.TYPE_INTEGER.equals(temp.getTypetype()) || Type_type.TYPE_INTEGER_A.equals(temp.getTypetype());
	}

	@Override
	/** {@inheritDoc} */
	public String getTypename() {
		return "integer";
	}

	@Override
	/** {@inheritDoc} */
	public String getOutlineIcon() {
		return "integer.gif";
	}

	@Override
	/** {@inheritDoc} */
	public StringBuilder getProposalDescription(final StringBuilder builder) {
		return builder.append("integer");
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

		if (null == namedNumbers) {
			parseBlockInt();
		}

		if (isErroneous || null == namedNumbers) {
			return;
		}

		/* check named numbers */

		final Map<String, Identifier> nameMap = new HashMap<String, Identifier>();
		for (int i = 0, size = namedNumbers.getSize(); i < size; i++) {
			final NamedValue namedValue = namedNumbers.getNamedValueByIndex(i);
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

		for (int i = 0, size = namedNumbers.getSize(); i < size; i++) {
			final NamedValue namedValue = namedNumbers.getNamedValueByIndex(i);
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
					final Integer intValue = Integer.valueOf(integerValue.intValue());
					if (valueMap.containsKey(intValue)) {
						value.getLocation().reportSemanticError(
								MessageFormat.format("Duplicate number {0} for name `{1}''", intValue, namedValue
										.getName().getDisplayName()));
						final NamedValue temp = valueMap.get(intValue);
						temp.getLocation().reportSemanticError(
								MessageFormat.format("Number {0} is already assigned to name `{1}''", intValue, temp
										.getName().getDisplayName()));
					} else {
						valueMap.put(intValue, namedValue);
					}
				} else {
					value.getLocation().reportSemanticError(
							MessageFormat.format("Integer value `{0}'' is too big to be used as a named number",
									integerValue.getValueValue()));
					value.setIsErroneous(true);
				}
				break;
			}
			default:
				namedValue.getLocation().reportSemanticError(
						MessageFormat.format("INTEGER value was expected for named number `{0}''", namedValue.getName()
								.getDisplayName()));
				value.setIsErroneous(true);
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
	public IValue checkThisValueRef(final CompilationTimeStamp timestamp, final IValue value) {
		if (Value_type.UNDEFINED_LOWERIDENTIFIER_VALUE.equals(value.getValuetype())) {
			if (value.isAsn()) {
				if (null == lastTimeChecked) {
					check(timestamp);
				}

				if (null != namedNumbers
						&& namedNumbers.hasNamedValueWithName(((Undefined_LowerIdentifier_Value) value).getIdentifier())) {
					final IValue tempValue = value.setValuetype(timestamp, Value_type.NAMED_INTEGER_VALUE);
					tempValue.setMyGovernor(this);
					return tempValue;
				}
			}
		}

		return super.checkThisValueRef(timestamp, value);
	}

	@Override
	/** {@inheritDoc} */
	public boolean checkThisValue(final CompilationTimeStamp timestamp, final IValue value, final Assignment lhs, final ValueCheckingOptions valueCheckingOptions) {
		final boolean selfReference = super.checkThisValue(timestamp, value, lhs, valueCheckingOptions);

		final IValue last = value.getValueRefdLast(timestamp, valueCheckingOptions.expected_value, null);
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

		switch (last.getValuetype()) {
		case INTEGER_VALUE:
			break;
		case NAMED_INTEGER_VALUE:
			if (null != namedNumbers) {
				// convert it into an integer value
				final Identifier name = ((Named_Integer_Value) last).getIdentifier();
				final NamedValue namedValue = namedNumbers.getNamedValueByName(name);
				IValue tempValue = namedValue.getValue();
				final ReferenceChain referenceChain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
				tempValue = tempValue.getValueRefdLast(timestamp, referenceChain);
				referenceChain.release();
				if (!tempValue.getIsErroneous(timestamp) && Value_type.INTEGER_VALUE.equals(tempValue.getValuetype())) {
					final int temp = ((Integer_Value) tempValue).intValue();
					final Integer_Value converted = new Integer_Value(temp);
					converted.copyGeneralProperties(value);
					((Named_Integer_Value) last).setCalculatedValue(converted);
				} else {
					// FIXME Most probably we were not able to build the semantic structure for something, because it is not yet supported, like referenced values in sets
				}
			}
			break;
		case EXPRESSION_VALUE:
		case MACRO_VALUE:
			// already checked
			break;
		default:
			value.getLocation().reportSemanticError(INTEGERVALUEEXPECTED);
			value.setIsErroneous(true);
		}

		value.setLastTimeChecked(timestamp);

		return selfReference;
	}

	//this method accepts REAL_VALUE
	public void checkThisValueLimit(final CompilationTimeStamp timestamp, final IValue value, final ValueCheckingOptions valueCheckingOptions) {
		super.checkThisValue(timestamp, value, null, valueCheckingOptions);

		final IValue last = value.getValueRefdLast(timestamp, valueCheckingOptions.expected_value, null);
		if (null == last || last.getIsErroneous(timestamp)) {
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
		case INTEGER_VALUE:
		case REAL_VALUE:
			break;
		case NAMED_INTEGER_VALUE:
			if (null != namedNumbers) {
				// convert it into an integer value
				final Identifier name = ((Named_Integer_Value) last).getIdentifier();
				final NamedValue namedValue = namedNumbers.getNamedValueByName(name);
				IValue tempValue = namedValue.getValue();
				final ReferenceChain referenceChain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
				tempValue = tempValue.getValueRefdLast(timestamp, referenceChain);
				referenceChain.release();
				if (!tempValue.getIsErroneous(timestamp) && Value_type.INTEGER_VALUE.equals(tempValue.getValuetype())) {
					final int temp = ((Integer_Value) tempValue).intValue();
					final Integer_Value converted = new Integer_Value(temp);
					converted.copyGeneralProperties(value);
					((Named_Integer_Value) last).setCalculatedValue(converted);
				} else {
					// FIXME Most probably we were not able to build the semantic structure for something, because it is not yet supported, like referenced values in sets
				}
			}
			break;
		case EXPRESSION_VALUE:
		case MACRO_VALUE:
			// already checked
			break;
		default:
			value.getLocation().reportSemanticError(INTEGERVALUEEXPECTED);
			value.setIsErroneous(true);
		}
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

		if (Template_type.VALUE_RANGE.equals(template.getTemplatetype())) {
			final ValueRange range = ((Value_Range_Template) template).getValueRange();
			final IValue lower = checkBoundary(timestamp, range.getMin(), BOUNDARY_TYPE.LOWER);
			final IValue upper = checkBoundary(timestamp, range.getMax(), BOUNDARY_TYPE.UPPER);
			range.setTypeType(getTypetypeTtcn3());

			if (lower != null && Value.Value_type.INTEGER_VALUE.equals(lower.getValuetype()) && upper != null
					&& Value.Value_type.INTEGER_VALUE.equals(upper.getValuetype())) {
				if (!getIsErroneous(timestamp) && ((Integer_Value) lower).getValue() > ((Integer_Value) upper).getValue()) {
					template.getLocation().reportSemanticError(INCORRECTBOUNDARIES);
				}
			}
			if (lower != null && range.getMax() == null) {
				checkBoundaryInfinity(timestamp, lower, true);
			}
			if (range.getMin() == null && upper != null) {
				checkBoundaryInfinity(timestamp, upper, false);
			}
			if (range.getMin() == null && range.isMinExclusive()) {
				template.getLocation().reportSemanticError("invalid lower boundary, -infinity cannot be excluded from an integer template range");
				template.setIsErroneous(true);
			}
			if (range.getMax() == null && range.isMaxExclusive()) {
				template.getLocation().reportSemanticError("invalid upper boundary, infinity cannot be excluded from an integer template range");
				template.setIsErroneous(true);
			}
		} else {
			template.getLocation().reportSemanticError(MessageFormat.format(TEMPLATENOTALLOWED, template.getTemplateTypeName()));
		}

		if (template.getLengthRestriction() != null) {
			template.getLocation().reportSemanticError(LENGTHRESTRICTIONNOTALLOWED);
		}

		return false;
	}

	private IValue checkBoundary(final CompilationTimeStamp timestamp, final Value value, final BOUNDARY_TYPE btype) {
		if (value == null) {
			return null;
		}
		value.setMyGovernor(this);
		IValue temp = checkThisValueRef(timestamp, value);
		checkThisValueLimit(timestamp, temp,
				new ValueCheckingOptions(Expected_Value_type.EXPECTED_DYNAMIC_VALUE, false, false, true, false, false));

		temp = temp.getValueRefdLast(timestamp, Expected_Value_type.EXPECTED_DYNAMIC_VALUE, null);
		if(Value_type.REAL_VALUE.equals(temp.getValuetype())) {
			if( ((Real_Value) temp).isNegativeInfinity() ) {
				if( BOUNDARY_TYPE.UPPER.equals(btype)) {
					value.getLocation().reportSemanticError(INCORRECTUPPERBOUNDARY);
					value.setIsErroneous(true);
				}
				return temp;
			} else if( ((Real_Value) temp).isPositiveInfinity() ) {
				if( BOUNDARY_TYPE.LOWER.equals(btype)) {
					value.getLocation().reportSemanticError(INCORRECTLOWERBOUNDARY);
					value.setIsErroneous(true);
				}
				return temp;
			} else {
				value.getLocation().reportSemanticError(INTEGERVALUEEXPECTED);
				value.setIsErroneous(true);
				return null;
			}
		}

		switch (temp.getValuetype()) {
		case INTEGER_VALUE:
			break;
		default:
			temp = null;
			break;
		}

		return temp;
	}

	private void checkBoundaryInfinity(final CompilationTimeStamp timestamp, final IValue value, final boolean isUpper) {
		if (value == null) {
			return;
		}

		value.setMyGovernor(this);
		IValue temp = checkThisValueRef(timestamp, value);
		checkThisValue(timestamp, temp, null, new ValueCheckingOptions(Expected_Value_type.EXPECTED_STATIC_VALUE, false, false, true, false, false));
		temp = temp.getValueRefdLast(timestamp, Expected_Value_type.EXPECTED_STATIC_VALUE, null);
		if (temp.getValuetype() == Value_type.OMIT_VALUE) {
			value.getLocation().reportSemanticError("`omit' value is not allowed in this context");
			value.setIsErroneous(true);
			return;
		}
		if (subType != null) {
			//FIXME implement subtype check
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

	private void parseBlockInt() {
		if (null == mBlock) {
			return;
		}

		final Asn1Parser parser = BlockLevelTokenStreamTracker.getASN1ParserForBlock(mBlock);
		if (null == parser) {
			return;
		}

		namedNumbers = parser.pr_special_NamedNumberList().namedValues;
		final List<SyntacticErrorStorage> errors = parser.getErrorStorage();
		if (null != errors && !errors.isEmpty()) {
			isErroneous = true;
			namedNumbers = null;
			for (int i = 0; i < errors.size(); i++) {
				ParserMarkerSupport.createOnTheFlyMixedMarker((IFile) mBlock.getLocation().getFile(), errors.get(i),
						IMarker.SEVERITY_ERROR);
			}
		}

		if (namedNumbers != null) {
			namedNumbers.setFullNameParent(this);
			namedNumbers.setMyScope(getMyScope());
		}
	}

	@Override
	/** {@inheritDoc} */
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		super.findReferences(referenceFinder, foundIdentifiers);
		if (namedNumbers != null) {
			namedNumbers.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	@Override
	/** {@inheritDoc} */
	protected boolean memberAccept(final ASTVisitor v) {
		if (!super.memberAccept(v)) {
			return false;
		}
		if (namedNumbers != null && !namedNumbers.accept(v)) {
			return false;
		}
		return true;
	}

	@Override
	/** {@inheritDoc} */
	public void generateCode( final JavaGenData aData, final StringBuilder source  ) {
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
	public String getGenNameValue(final JavaGenData aData, final StringBuilder source , final Scope scope) {
		aData.addBuiltinTypeImport( "TitanInteger" );
		return "TitanInteger";
	}

	@Override
	/** {@inheritDoc} */
	public String getGenNameTemplate(final JavaGenData aData, final StringBuilder source, final Scope scope) {
		aData.addBuiltinTypeImport( "TitanInteger_template" );
		return "TitanInteger_template";
	}

	@Override
	/** {@inheritDoc} */
	public String internalGetGenNameTypeDescriptor(final JavaGenData aData, final StringBuilder source, final Scope scope) {
		aData.addBuiltinTypeImport( "Base_Type" );
		return "Base_Type.TitanInteger";
	}
}
