/******************************************************************************
 * Copyright (c) 2000-2020 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
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
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.Identifier.Identifier_type;
import org.eclipse.titan.designer.AST.Location;
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
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.attributes.JsonAST;
import org.eclipse.titan.designer.AST.TTCN3.attributes.RawAST;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Type;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Definition;
import org.eclipse.titan.designer.AST.TTCN3.templates.ITTCN3Template;
import org.eclipse.titan.designer.AST.TTCN3.values.Expression_Value.Operation_type;
import org.eclipse.titan.designer.AST.TTCN3.values.Integer_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.ExpressionStruct;
import org.eclipse.titan.designer.compiler.BuildTimestamp;
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
	public boolean isCompatibleByPort(final CompilationTimeStamp timestamp, final IType otherType) {
		check(timestamp);
		otherType.check(timestamp);

		final IType t1 = getTypeRefdLast(timestamp);
		final IType t2 = otherType.getTypeRefdLast(timestamp);

		if (t1.getIsErroneous(timestamp) || t2.getIsErroneous(timestamp)) {
			return true;
		}

		return t1.isCompatibleByPort(timestamp, otherType);
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

		return refdLast.getTypename();//TODO maybe this should be the name of the current type.
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

		initAttributes(timestamp);

		refdLast = getTypeRefdLast(timestamp);

		if (refdLast != null && !refdLast.getIsErroneous(timestamp)) {
			refdLast.check(timestamp);
			componentInternal = refdLast.isComponentInternal(timestamp);
		}

		if (constraints != null) {
			constraints.check(timestamp);
		}

		final IReferenceChain tempReferenceChain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
		final IType typeParent = getTypeRefd(timestamp, tempReferenceChain);
		tempReferenceChain.release();
		if (!refdLast.getIsErroneous(timestamp) && !typeParent.getIsErroneous(timestamp)) {
			checkSubtypeRestrictions(timestamp, refdLast.getSubtypeType(), typeParent.getSubtype());
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

				if (refd.getMyScope() != null) {
					// opentype or OCFT
					refd.setMyScope(getMyScope());
					refd.setParentType(getParentType());
					refd.setGenName(getGenNameOwn(), "type");
					//FIXME
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

		if (rawAttribute != null) {

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
		}

		if (refd.getJsonAttribute() == null) {
			refd.forceJson(timestamp);
		}
		checkJson(timestamp);

		//TODO add checks for other encodings.

		if (refChain.contains(this)) {
			return;
		}

		refdLast.checkCodingAttributes(timestamp, refChain);
	}

	@Override
	public void checkJsonDefault() {
		final String defaultValue = jsonAttribute.default_value;
		final int length = defaultValue.length();
		int i;
		switch (refdLast.getTypetype()) {
		case TYPE_BOOL:
			if (!defaultValue.matches("true|false")) {
				getLocation().reportSemanticError(MessageFormat.format("Invalid {0} JSON default value", getTypename()));
			}
			break;
		case TYPE_INTEGER:
			if (!defaultValue.matches("-?[0-9]+")) {
				getLocation().reportSemanticError(MessageFormat.format("Invalid {0} JSON default value", getTypename()));
			}
			break;
		case TYPE_REAL:
			if (defaultValue.matches("-?infinity|not_a_number") || defaultValue.length() < 1) {
				// special float values => skip the rest of the check
				return;
			}

			boolean first_digit = false; // first non-zero digit reached
			boolean zero = false; // first zero digit reached
			boolean decimal_point = false; // decimal point (.) reached
			boolean exponent_mark = false; // exponential mark (e or E) reached
			boolean exponent_sign = false; // sign of the exponential (- or +) reached
			boolean error = false;

			i = (defaultValue.charAt(0) == '-') ? 1 : 0;
			while(!error && i < defaultValue.length()) {
				final char value = defaultValue.charAt(i);
				switch (value) {
				case '.':
					if (decimal_point || exponent_mark || (!first_digit && !zero)) {
						error = true;
					}
					decimal_point = true;
					first_digit = false;
					zero = false;
					break;
				case 'e':
				case 'E':
					if (exponent_mark || (!first_digit && !zero)) {
						error = true;
					}
					exponent_mark = true;
					first_digit = false;
					zero = false;
					break;
				case '0':
					if (!first_digit && (exponent_mark || (!decimal_point && zero))) {
						error = true;
					}
					zero = true;
					break;
				case '1':
				case '2':
				case '3':
				case '4':
				case '5':
				case '6':
				case '7':
				case '8':
				case '9':
					if (!first_digit && zero && (!decimal_point || exponent_mark)) {
						error = true;
					}
					first_digit = true;
					break;
				case '-':
				case '+':
					if (exponent_sign || !exponent_mark || zero || first_digit) {
						error = true;
					}
					exponent_sign = true;
					break;
				default:
					error = true;
				}
				++i;
			}

			if (!first_digit && !zero) {
				getLocation().reportSemanticError(MessageFormat.format("Invalid {0} JSON default value", getTypename()));
			}
			break;
		case TYPE_BITSTRING:
			if (!defaultValue.matches("[0-1]+")) {
				getLocation().reportSemanticError(MessageFormat.format("Invalid {0} JSON default value", getTypename()));
			}
			break;
		case TYPE_OCTETSTRING:
			if (defaultValue.length() % 2 != 0 || !defaultValue.matches("[0-9a-fA-F]+")) {
				getLocation().reportSemanticError(MessageFormat.format("Invalid {0} JSON default value", getTypename()));
			}
			break;
		case TYPE_HEXSTRING:
			if (!defaultValue.matches("[0-9a-fA-F]+")) {
				getLocation().reportSemanticError(MessageFormat.format("Invalid {0} JSON default value", getTypename()));
			}
			break;
		case TYPE_CHARSTRING:
			i = 0;
			while (i < length) {
				final char value = defaultValue.charAt(i);
				if ((byte)value < 0) {
					getLocation().reportSemanticError(MessageFormat.format("Invalid {0} JSON default value", getTypename()));
					return;
				}
				if (value == '\\') {
					if (i == length-1) {
						getLocation().reportSemanticError(MessageFormat.format("Invalid {0} JSON default value", getTypename()));
						return;
					}
					
					switch (value) {
					case '\\':
					case '\"':
					case 'n':
					case 't':
					case 'r':
					case 'f':
					case 'b':
					case '/':
						break;
					case 'u':
					{
						if (i + 4 >= length) {
							getLocation().reportSemanticError(MessageFormat.format("Invalid {0} JSON default value", getTypename()));
							return;
						}
						if (defaultValue.charAt(i+1) != '0' || defaultValue.charAt(i+2) != '0' ||
								defaultValue.charAt(i+3) < '0' || defaultValue.charAt(i+3) > '7') {
							getLocation().reportSemanticError(MessageFormat.format("Invalid {0} JSON default value", getTypename()));
							return;
						}
						final char nextChar = defaultValue.charAt(i+4);
						if ((nextChar < '0' || nextChar > '9') &&
								(nextChar < 'a' || nextChar > 'f') &&
								(nextChar < 'A' || nextChar > 'F')) {
							getLocation().reportSemanticError(MessageFormat.format("Invalid {0} JSON default value", getTypename()));
							return;
						}
						i += 4;
						break;
					}
					default:
						getLocation().reportSemanticError(MessageFormat.format("Invalid {0} JSON default value", getTypename()));
						return;
					}
				}
				i++;
			}//while
			break;
		case TYPE_UCHARSTRING:
			i = 0;
			while (i < length) {
				final char value = defaultValue.charAt(i);
				if (value == '\\') {
					if (i == length-1) {
						getLocation().reportSemanticError(MessageFormat.format("Invalid {0} JSON default value", getTypename()));
						return;
					}
					
					switch (value) {
					case '\\':
					case '\"':
					case 'n':
		            case 't':
		            case 'r':
		            case 'f':
		            case 'b':
		            case '/':
						break;
		            case 'u':
		            {
		            	if (i + 4 >= length) {
		            		getLocation().reportSemanticError(MessageFormat.format("Invalid {0} JSON default value", getTypename()));
		            		return;
		            	}
		            	for (int j = 1; j < 5; j++) {
			            	final char nextChar = defaultValue.charAt(i+j);
			            	if ((nextChar < '0' || nextChar > '9') &&
			            			(nextChar < 'a' || nextChar > 'f') &&
			            			(nextChar < 'A' || nextChar > 'F')) {
			            		getLocation().reportSemanticError(MessageFormat.format("Invalid {0} JSON default value", getTypename()));
			            		return;
			            	}
		            	}
		            i += 4;
		            break;
		            }
					default:
						getLocation().reportSemanticError(MessageFormat.format("Invalid {0} JSON default value", getTypename()));
						return;
					}
				}
				i++;
			}//while
			break;
		case TYPE_VERDICT:
			if (!defaultValue.matches("none|pass|inconc|fail|error")) {
				getLocation().reportSemanticError(MessageFormat.format("Invalid {0} JSON default value", getTypename()));
			}
			break;
		case TYPE_SET_OF:
		case TYPE_SEQUENCE_OF:
			if (!jsonAttribute.default_value.matches("\\{\\}")) {
				getLocation().reportSemanticError(MessageFormat.format("Invalid JSON default value for type `{0}''. Only the empty array is allowed.", getTypename()));
			}
			break;
		case TYPE_TTCN3_SET:
		case TYPE_TTCN3_SEQUENCE:
			if (((TTCN3_Set_Seq_Choice_BaseType)refdLast).getNofComponents() != 0) {
				getLocation().reportSemanticError("JSON default values are not available for record/set types with 1 or more fields");
			}
			break;
		case TYPE_TTCN3_ENUMERATED:
			final Identifier identifier = new Identifier(Identifier_type.ID_TTCN, jsonAttribute.default_value);
			if (!((TTCN3_Enumerated_Type)refdLast).hasEnumItemWithName(identifier)) { 
				getLocation().reportSemanticError(MessageFormat.format("Invalid JSON default value for enumerated type `{0}''", getTypename()));
			}
			break;
		default:
			getLocation().reportSemanticError(MessageFormat.format("JSON default values are not available for type `{0}''", getTypename()));
			break;
		}
	}
	
	@Override
	public void checkJson(final CompilationTimeStamp timestamp) {
		if (jsonAttribute == null && !hasEncodeAttribute("JSON")) {
			return;
		}

		if (jsonAttribute == null) {
			return;
		}

		if (jsonAttribute.omit_as_null && !isOptionalField()) {
			getLocation().reportSemanticError("Invalid attribute, 'omit as null' requires optional field of a record or set.");
		}

		if (jsonAttribute.as_value) {
			switch(refdLast.getTypetype()) {
			case TYPE_TTCN3_CHOICE:
			case TYPE_ANYTYPE:
				break; // OK
			case TYPE_TTCN3_SEQUENCE:
			case TYPE_TTCN3_SET:
				if (((TTCN3_Set_Seq_Choice_BaseType)refdLast).getNofComponents() == 1) {
					break; // OK
				}
			default:
				getLocation().reportSemanticError("Invalid attribute, 'as value' is only allowed for unions, the anytype, or records or sets with one field");
				break;
			}
		}

		if (jsonAttribute.alias != null) {
			final IType parent = getParentType();
			if (parent == null) {
				// only report this error when using the new codec handling, otherwise
				// ignore the attribute (since it can also be set by the XML 'name as ...' attribute)
				getLocation().reportSemanticError("Invalid attribute, 'name as ...' requires field of a record, set or union.");
			} else {
				switch (parent.getTypetype()) {
				case TYPE_TTCN3_SEQUENCE:
				case TYPE_TTCN3_SET:
				case TYPE_TTCN3_CHOICE:
				case TYPE_ANYTYPE:
					break;
				default:
					// only report this error when using the new codec handling, otherwise
					// ignore the attribute (since it can also be set by the XML 'name as ...' attribute)
					getLocation().reportSemanticError("Invalid attribute, 'name as ...' requires field of a record, set or union.");
					break;
				}
			}

			if (parent != null && parent.getJsonAttribute() != null && parent.getJsonAttribute().as_value) {
				switch (parent.getTypetype()) {
				case TYPE_TTCN3_CHOICE:
				case TYPE_ANYTYPE:
					// parent_type_name remains null if the 'as value' attribute is set for an invalid type
					getLocation().reportSemanticWarning(MessageFormat.format("Attribute 'name as ...' will be ignored, because parent {0} is encoded without field names.", parent.getTypename()));
					break;
				case TYPE_TTCN3_SEQUENCE:
				case TYPE_TTCN3_SET:
					if (((TTCN3_Set_Seq_Choice_BaseType)parent).getNofComponents() == 1) {
						// parent_type_name remains null if the 'as value' attribute is set for an invalid type
						getLocation().reportSemanticWarning(MessageFormat.format("Attribute 'name as ...' will be ignored, because parent {0} is encoded without field names.", parent.getTypename()));
					}
					break;
				default:
					break;
				}
			}
		}

		if (jsonAttribute.default_value != null) {
			checkJsonDefault();
		}

		//TODO: check schema extensions 

		if (jsonAttribute.metainfo_unbound) {
			if (refdLast.getTypetype() == Type_type.TYPE_TTCN3_SEQUENCE || 
					refdLast.getTypetype() == Type_type.TYPE_TTCN3_SET) {
				final TTCN3_Set_Seq_Choice_BaseType last = (TTCN3_Set_Seq_Choice_BaseType)refdLast;
				final int nofComponents = last.getNofComponents();
				if (jsonAttribute.as_value && nofComponents == 1) {
					getLocation().reportSemanticWarning(MessageFormat.format("Attribute 'metainfo for unbound' will be ignored, because the {0} is encoded without field names.", refdLast.getTypetype() == Type_type.TYPE_TTCN3_SEQUENCE ? "record" : "set"));
				} else {
					for (int i = 0; i < nofComponents; i++) {
						Type componentType = last.getComponentByIndex(i).getType();
						if (componentType.jsonAttribute == null) {
							componentType.jsonAttribute = new JsonAST();
						}
						componentType.jsonAttribute.metainfo_unbound = true;
					}
				}
			} else {
				switch (refdLast.getTypetype()) {
				case TYPE_SEQUENCE_OF:
				case TYPE_SET_OF:
				case TYPE_ARRAY:
					break;
				default:
					if (getParentType() == null || (getParentType().getTypetype() != Type_type.TYPE_TTCN3_SEQUENCE &&
					getParentType().getTypetype() != Type_type.TYPE_TTCN3_SET)) {
						// only allowed if it's an array type or a field of a record/set
						getLocation().reportSemanticError("Invalid attribute 'metainfo for unbound', requires record, set, record of, set of, array or field of a record or set");
					}
					break;
				}
			}
		}

		if (jsonAttribute.as_number) {
			if (refdLast.getTypetypeTtcn3() != Type_type.TYPE_TTCN3_ENUMERATED) { 
				getLocation().reportSemanticError("Invalid attribute, 'as number' is only allowed for enumerated types");
			} else if (jsonAttribute.enum_texts.size() != 0) {
				getLocation().reportSemanticWarning("Attribute 'text ... as ...' will be ignored, because the enumerated values are encoded as numbers");
			}
		}

		//FIXME: check tag_list

		if (jsonAttribute.as_map) {
			if (refdLast.getTypetype() != Type_type.TYPE_SEQUENCE_OF && refdLast.getTypetype() != Type_type.TYPE_SET_OF) {
				getLocation().reportSemanticError("Invalid attribute, 'as map' requires record of or set of");
			} else {  // T_SEQOF && T_SETOF
				final AbstractOfType last = (AbstractOfType) refdLast;
				final IType ofType = last.getOfType();
				final IType ofTypeLast = ofType.getTypeRefdLast(CompilationTimeStamp.getBaseTimestamp());
				if ((ofTypeLast.getTypetype() == Type_type.TYPE_TTCN3_SEQUENCE && ((TTCN3_Sequence_Type) ofTypeLast).getNofComponents() == 2 )) {
		        	final Type keyType = ((TTCN3_Sequence_Type) ofTypeLast).getComponentByIndex(0).getType();
		        	if (keyType.getTypeRefdLast(CompilationTimeStamp.getBaseTimestamp()).getTypetype() != Type_type.TYPE_UCHARSTRING) {
		        		getLocation().reportSemanticError("Invalid attribute, 'as map' requires the element type's first field to be a universal charstring");
		        	}

		        	if (keyType.isOptionalField()) {
		        		getLocation().reportSemanticError("Invalid attribute, 'as map' requires the element type's first field to be mandatory");
		        	}
		        } else if (ofTypeLast.getTypetype() == Type_type.TYPE_TTCN3_SET || ((TTCN3_Set_Type) ofTypeLast).getNofComponents() == 2 ) {
		        	final Type keyType = ((TTCN3_Set_Type) ofTypeLast).getComponentByIndex(0).getType();
		        	if (keyType.getTypeRefdLast(CompilationTimeStamp.getBaseTimestamp()).getTypetype() != Type_type.TYPE_UCHARSTRING) {
		        		getLocation().reportSemanticError("Invalid attribute, 'as map' requires the element type's first field to be a universal charstring");
		        	}

		        	if (keyType.isOptionalField()) {
		        		getLocation().reportSemanticError("Invalid attribute, 'as map' requires the element type's first field to be mandatory");
		        	}
		        } else {
		        	getLocation().reportSemanticError("Invalid attribute, 'as map' requires the element type to be a record or set with 2 fields");
		        }
			}
		}

		if (jsonAttribute.enum_texts.size() > 0) {
			if (refdLast.getTypetypeTtcn3() != Type_type.TYPE_TTCN3_ENUMERATED) {
				getLocation().reportSemanticError("Invalid attribute, 'text ... as ...' requires an enumerated type");
			} else {
				for (int i = 0; i < jsonAttribute.enum_texts.size(); i++) {
					final Identifier identifier = new Identifier(Identifier_type.ID_TTCN, jsonAttribute.enum_texts.get(i).from, NULL_Location.INSTANCE, true);
					if (!((TTCN3_Enumerated_Type)refdLast).hasEnumItemWithName(identifier)) {
						getLocation().reportSemanticError(MessageFormat.format("Invalid JSON default value for enumerated type `{0}''", getTypename()));
					} else {
						final EnumItem enumItem = ((TTCN3_Enumerated_Type)refdLast).getEnumItemWithName(identifier);
						final int index = (int) ((Integer_Value) enumItem.getValue()).getValue();
						jsonAttribute.enum_texts.get(i).index = index;
						for (int j = 0; j < i; j++) {
							if (jsonAttribute.enum_texts.get(j).index == index) {
								getLocation().reportSemanticError(MessageFormat.format("Duplicate attribute 'text ... as ...' for enumerated value `{0}''", jsonAttribute.enum_texts.get(i).from));
							}
						}
					}
				}
			}
		}
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
	public int getRawLength(final BuildTimestamp timestamp) {
		if (refdLast != null && !refdLast.getIsErroneous(CompilationTimeStamp.getBaseTimestamp()) && refdLast != this) {
			return refdLast.getRawLength(timestamp);
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
	public void checkMapParameter(final CompilationTimeStamp timestamp, final IReferenceChain refChain, final Location errorLocation) {
		if (refChain.contains(this)) {
			return;
		}

		refChain.add(this);
		if (refdLast != null) {
			refdLast.checkMapParameter(timestamp, refChain, errorLocation);
		}
	}

	@Override
	/** {@inheritDoc} */
	public void forceRaw(final CompilationTimeStamp timestamp) {
		if (refd != null && !refd.getIsErroneous(CompilationTimeStamp.getBaseTimestamp()) && refdLast != this) {
			refd.forceRaw(timestamp);
		}
	}

	@Override
	/** {@inheritDoc} */
	public void forceJson(final CompilationTimeStamp timestamp) {
		if (refd != null && !refd.getIsErroneous(CompilationTimeStamp.getBaseTimestamp()) && refdLast != this) {
			refd.forceJson(timestamp);
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
	public String getGenNameValue(final JavaGenData aData, final StringBuilder source) {
		if (this == refd || refd == null || refdLast == null) {
			ErrorReporter.INTERNAL_ERROR("Code generator reached erroneous type reference `" + getFullName() + "''");
			return "FATAL_ERROR encountered while processing `" + getFullName() + "''\n";
		}

		return refd.getGenNameValue(aData, source);
	}

	@Override
	/** {@inheritDoc} */
	public String getGenNameTemplate(final JavaGenData aData, final StringBuilder source) {
		if (this == refd || refd == null || refdLast == null) {
			ErrorReporter.INTERNAL_ERROR("Code generator reached erroneous type reference `" + getFullName() + "''");
			return "FATAL_ERROR encountered while processing `" + getFullName() + "''\n";
		}

		return refd.getGenNameTemplate(aData, source);
	}

	@Override
	/** {@inheritDoc} */
	public String getGenNameTypeDescriptor(final JavaGenData aData, final StringBuilder source) {
		if (rawAttribute != null || jsonAttribute != null ||
				hasVariantAttributes(CompilationTimeStamp.getBaseTimestamp())
				|| (!isAsn() && hasEncodeAttribute("JSON"))) {
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

		final IReferenceChain refChain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
		final IType t = ((IReferencingType) this).getTypeRefd(CompilationTimeStamp.getBaseTimestamp(), refChain);
		refChain.release();

		if (t != null && t != this) {
			return t.getGenNameTypeDescriptor(aData, source);
		}

		ErrorReporter.INTERNAL_ERROR("Code generator reached erroneous type reference `" + getFullName() + "''");

		return "FATAL_ERROR encountered while processing `" + getFullName() + "''\n";
	}

	@Override
	/** {@inheritDoc} */
	public boolean needsOwnRawDescriptor(final JavaGenData aData) {
		return rawAttribute != null;
	}

	@Override
	/** {@inheritDoc} */
	public String getGenNameRawDescriptor(final JavaGenData aData, final StringBuilder source) {
		if (this == refd || refd == null || refdLast == null) {
			ErrorReporter.INTERNAL_ERROR("Code generator reached erroneous type reference `" + getFullName() + "''");

			return "FATAL_ERROR encountered while processing `" + getFullName() + "''\n";
		}

		if (rawAttribute != null) {
			if (needsAlias()) {
				return getGenNameOwn(aData) + "." + getGenNameOwn() + "_raw_";
			}

			return getGenNameOwn(aData) + "_raw_";
		}

		return refd.getGenNameRawDescriptor(aData, source);
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
		if (this == refd || refd == null || refdLast == null) {
			ErrorReporter.INTERNAL_ERROR("Code generator reached erroneous type reference `" + getFullName() + "''");

			return "FATAL_ERROR encountered while processing `" + getFullName() + "''\n";
		}

		if (needsOwnJsonDescriptor(aData)) {
			return getGenNameOwn(aData) + "_json_";
		}

		return refd.getGenNameJsonDescriptor(aData, source);
	}

	@Override
	/** {@inheritDoc} */
	public boolean generatesOwnClass(JavaGenData aData, StringBuilder source) {
		return needsAlias();
	}

	@Override
	/** {@inheritDoc} */
	public void generateCode( final JavaGenData aData, final StringBuilder source ) {
		if (lastTimeGenerated != null && !lastTimeGenerated.isLess(aData.getBuildTimstamp())) {
			return;
		}

		lastTimeGenerated = aData.getBuildTimstamp();

		if(myScope.getModuleScopeGen() == refd.getMyScope().getModuleScopeGen()) {
			final StringBuilder tempSource = aData.getCodeForType(refd.getGenNameOwn());
			refd.generateCode(aData, tempSource);
		}

		if(needsAlias()) {
			final String ownName = getGenNameOwn();
			switch (refd.getTypetype()) {
			case TYPE_PORT:
				source.append(MessageFormat.format("\tpublic static class {0} extends {1} '{' '}'\n", ownName, refd.getGenNameValue(aData, source)));
				break;
			case TYPE_SIGNATURE:
				source.append(MessageFormat.format("\tpublic static class {0}_call extends {1}_call '{' '}'\n", genName, refd.getGenNameValue(aData, source)));
				source.append(MessageFormat.format("\tpublic static class {0}_call_redirect extends {1}_call_redirect '{' '}'\n", genName, refd.getGenNameValue(aData, source)));
				if (!((Signature_Type) refd).isNonblocking()) {
					source.append(MessageFormat.format("\tpublic static class {0}_reply extends {1}_reply '{' '}'\n", genName, refd.getGenNameValue(aData, source)));
					source.append(MessageFormat.format("\tpublic static class {0}_reply_redirect extends {1}_reply_redirect '{' '}'\n", genName, refd.getGenNameValue(aData, source)));
				}
				if (((Signature_Type) refd).getSignatureExceptions() != null) {
					source.append(MessageFormat.format("\tpublic static class {0}_template extends {1} '{' '}'\n", genName, refd.getGenNameTemplate(aData, source)));
				}
				break;
			default:
				source.append(MessageFormat.format("\tpublic static class {0} extends {1} '{'\n", ownName, refd.getGenNameValue(aData, source)));

				final StringBuilder descriptor = new StringBuilder();
				generateCodeTypedescriptor(aData, source, descriptor, null);
				generateCodeDefaultCoding(aData, source, descriptor);
				generateCodeForCodingHandlers(aData, source, descriptor);
				source.append(descriptor);

				source.append("\t}\n");

				source.append(MessageFormat.format("\tpublic static class {0}_template extends {1} '{' '}'\n", ownName, refd.getGenNameTemplate(aData, source)));
			}
		} else {
			generateCodeTypedescriptor(aData, source, null, aData.attibute_registry);
			generateCodeDefaultCoding(aData, source, null);
			generateCodeForCodingHandlers(aData, source, null);
		}

		if (!isAsn()) {
			if (hasDoneAttribute()) {
				generateCodeDone(aData, source);
			}
			if (subType != null) {
				subType.generateCode(aData, source);
			}
		}
	}

	@Override
	/** {@inheritDoc} */
	public void generateCodeIsPresentBoundChosen(final JavaGenData aData, final ExpressionStruct expression, final List<ISubReference> subreferences,
			final int subReferenceIndex, final String globalId, final String externalId, final boolean isTemplate, final Operation_type optype, final String field, final Scope targetScope) {
		if (this == refdLast || refdLast == null) {
			ErrorReporter.INTERNAL_ERROR("Code generator reached erroneous type reference `" + getFullName() + "''");
			expression.expression.append("FATAL_ERROR encountered while processing `" + getFullName() + "''\n");
			return;
		}

		refdLast.generateCodeIsPresentBoundChosen(aData, expression, subreferences, subReferenceIndex, globalId, externalId, isTemplate, optype, field, targetScope);
	}

	@Override
	/** {@inheritDoc} */
	public boolean isPresentAnyvalueEmbeddedField(final ExpressionStruct expression, final List<ISubReference> subreferences, final int beginIndex) {
		if (this == refdLast || refdLast == null) {
			ErrorReporter.INTERNAL_ERROR("Code generator reached erroneous type reference `" + getFullName() + "''");
			expression.expression.append("FATAL_ERROR encountered while processing `" + getFullName() + "''\n");
			return false;
		}

		return refdLast.isPresentAnyvalueEmbeddedField(expression, subreferences, beginIndex);
	}

	@Override
	/** {@inheritDoc} */
	public String generateConversion(final JavaGenData aData, final IType fromType, final String fromName, final boolean forValue, final ExpressionStruct expression) {
		if (this == refdLast || refdLast == null) {
			ErrorReporter.INTERNAL_ERROR("Code generator reached erroneous type reference `" + getFullName() + "''");
			expression.expression.append("FATAL_ERROR encountered while processing `" + getFullName() + "''\n");
			return fromName;
		}

		return refdLast.generateConversion(aData, fromType, fromName, forValue, expression);
	}
}
