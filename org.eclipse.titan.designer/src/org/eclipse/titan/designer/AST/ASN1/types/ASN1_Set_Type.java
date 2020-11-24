/******************************************************************************
 * Copyright (c) 2000-2020 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.ASN1.types;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.common.parsers.SyntacticErrorStorage;
import org.eclipse.titan.designer.Activator;
import org.eclipse.titan.designer.AST.ArraySubReference;
import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.FieldSubReference;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.ISubReference;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.IValue.Value_type;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.Identifier.Identifier_type;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.AST.ParameterisedSubReference;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.ReferenceChain;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.Type;
import org.eclipse.titan.designer.AST.TypeCompatibilityInfo;
import org.eclipse.titan.designer.AST.Value;
import org.eclipse.titan.designer.AST.ASN1.Block;
import org.eclipse.titan.designer.AST.ASN1.IASN1Type;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.attributes.JsonAST;
import org.eclipse.titan.designer.AST.TTCN3.templates.ITTCN3Template;
import org.eclipse.titan.designer.AST.TTCN3.templates.ITTCN3Template.Template_type;
import org.eclipse.titan.designer.AST.TTCN3.templates.NamedTemplate;
import org.eclipse.titan.designer.AST.TTCN3.templates.Named_Template_List;
import org.eclipse.titan.designer.AST.TTCN3.templates.OmitValue_Template;
import org.eclipse.titan.designer.AST.TTCN3.templates.Template_List;
import org.eclipse.titan.designer.AST.TTCN3.types.Array_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.CompField;
import org.eclipse.titan.designer.AST.TTCN3.types.RecordSetCodeGenerator;
import org.eclipse.titan.designer.AST.TTCN3.types.RecordSetCodeGenerator.FieldInfo;
import org.eclipse.titan.designer.AST.TTCN3.types.SetOf_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.TTCN3_Set_Seq_Choice_BaseType;
import org.eclipse.titan.designer.AST.TTCN3.types.TTCN3_Set_Type;
import org.eclipse.titan.designer.AST.TTCN3.values.NamedValue;
import org.eclipse.titan.designer.AST.TTCN3.values.Omit_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.SequenceOf_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Set_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.ExpressionStruct;
import org.eclipse.titan.designer.compiler.JavaGenData;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ParserMarkerSupport;
import org.eclipse.titan.designer.parsers.asn1parser.Asn1Parser;
import org.eclipse.titan.designer.parsers.asn1parser.BlockLevelTokenStreamTracker;
import org.eclipse.titan.designer.preferences.PreferenceConstants;
import org.eclipse.titan.designer.productUtilities.ProductConstants;

/**
 * @author Kristof Szabados
 * @author Arpad Lovassy
 */
public final class ASN1_Set_Type extends ASN1_Set_Seq_Choice_BaseType {
	private static final String NONEMPTYEXPECTED = "A non-empty value was expected for type `{0}''";

	// TODO these are duplicates, try to find a way to remove them without too much pain.
	private static final String VALUELISTNOTATIONERRORASN1 = "Value list notation cannot be used for SET type `{0}''";
	private static final String SETVALUEXPECTEDASN1 = "SET value was expected for type `{0}''";
	private static final String NONEXISTENTFIELDASN1 = "Reference to a non-existent component `{0}'' of SET type `{1}''";
	private static final String DUPLICATEFIELDFIRSTASN1 = "Component `{0}'' is already given here";
	private static final String DUPLICATEFIELDAGAINASN1 = "Duplicated SET component `{0}''";
	private static final String MISSINGFIELDASN1 = "Mandatory component `{0}'' is missing from SET value";

	private static final String VALUELISTNOTATIONERRORTTCN3 = "Value list notation cannot be used for set type `{0}''";
	private static final String SETVALUEXPECTEDTTCN3 = "set value was expected for type `{0}''";
	private static final String NONEXISTENTFIELDTTCN3 = "Reference to a non-existent field `{0}'' in set value for type `{1}''";
	private static final String DUPLICATEFIELDFIRSTTTCN3 = "Field `{0}'' is already given here";
	private static final String DUPLICATEFIELDAGAINTTCN3 = "Duplicated set field `{0}''";
	private static final String MISSINGFIELDTTCN3 = "Field `{0}'' is missing from set value";

	private static final String VALUELISTNOTATIONNOTALLOWED = "Value list notation is not allowed for set type `{0}''";
	private static final String NONEMPTYSETTEMPLATEEXPECTED = "A non-empty set template was expected for type `{0}''";
	private static final String TEMPLATENOTALLOWED = "{0} cannot be used for record type `{1}''";
	private static final String LENGTHRESTRICTIONNOTALLOWED = "Length restriction is not allowed for record type `{0}''";
	private static final String DUPLICATETEMPLATEFIELDFIRST = "Duplicate field `{0}'' in template";
	private static final String DUPLICATETEMPLATEFIELDAGAIN = "Field `{0}'' is already given here";
	private static final String NONEXISTENTTEMPLATEFIELDREFERENCE = "Reference to non-existing field `{0}'' in record template for type `{1}''";
	private static final String MISSINGTEMPLATEFIELD = "Field `{0}'' is missing from template for record type `{1}''";

	private static final String NOFFIELDSDONTMATCH = "The number of fields in set/SET types must be the same";
	private static final String BADOPTIONALITY = "The optionality of fields in set/SET types must be the same";
	private static final String NOTCOMPATIBLESETSETOF = "set/SET and set of/SET OF types are compatible only with other set/SET and set of/SET OF types";
	private static final String NOTCOMPATIBLEUNIONANYTYPE = "union/CHOICE/anytype types are compatible only with other union/CHOICE/anytype types";

	private CompilationTimeStamp trCompsofTimestamp;

	// The actual value of having the default as optional setting on..
	private static boolean defaultAsOptional;

	static {
		defaultAsOptional = Platform.getPreferencesService().getBoolean(ProductConstants.PRODUCT_ID_DESIGNER,
				PreferenceConstants.DEFAULTASOPTIONAL, false, null);

		final Activator activator = Activator.getDefault();
		if (activator != null) {
			activator.getPreferenceStore().addPropertyChangeListener(new IPropertyChangeListener() {

				@Override
				public void propertyChange(final PropertyChangeEvent event) {
					final String property = event.getProperty();
					if (PreferenceConstants.DEFAULTASOPTIONAL.equals(property)) {
						defaultAsOptional = Platform.getPreferencesService().getBoolean(ProductConstants.PRODUCT_ID_DESIGNER,
								PreferenceConstants.DEFAULTASOPTIONAL, false, null);
					}
				}
			});
		}
	}

	// The actual value of the severity level to report stricter constant
	// checking on.
	private static boolean strictConstantCheckingSeverity;

	static {
		strictConstantCheckingSeverity = Platform.getPreferencesService().getBoolean(ProductConstants.PRODUCT_ID_DESIGNER,
				PreferenceConstants.REPORT_STRICT_CONSTANTS, false, null);

		final Activator activator = Activator.getDefault();
		if (activator != null) {
			activator.getPreferenceStore().addPropertyChangeListener(new IPropertyChangeListener() {

				@Override
				public void propertyChange(final PropertyChangeEvent event) {
					final String property = event.getProperty();
					if (PreferenceConstants.REPORT_STRICT_CONSTANTS.equals(property)) {
						strictConstantCheckingSeverity = Platform.getPreferencesService().getBoolean(
								ProductConstants.PRODUCT_ID_DESIGNER, PreferenceConstants.REPORT_STRICT_CONSTANTS,
								false, null);
					}
				}
			});
		}
	}

	public ASN1_Set_Type(final Block aBlock) {
		this.mBlock = aBlock;
	}

	public IASN1Type newInstance() {
		return new ASN1_Set_Type(mBlock);
	}

	@Override
	/** {@inheritDoc} */
	public Type_type getTypetype() {
		return Type_type.TYPE_ASN1_SET;
	}

	@Override
	/** {@inheritDoc} */
	public Type_type getTypetypeTtcn3() {
		return Type_type.TYPE_TTCN3_SET;
	}

	@Override
	/** {@inheritDoc} */
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		if (null != components) {
			components.setMyScope(scope);
		}
	}

	@Override
	/** {@inheritDoc} */
	public int getNofComponents() {
		if (null == components) {
			parseBlockSet();
		}

		return components.getNofComps();
	}

	@Override
	/** {@inheritDoc} */
	public boolean isCompatible(final CompilationTimeStamp timestamp, final IType otherType, final TypeCompatibilityInfo info,
			final TypeCompatibilityInfo.Chain leftChain, final TypeCompatibilityInfo.Chain rightChain) {
		check(timestamp);
		otherType.check(timestamp);
		final IType temp = otherType.getTypeRefdLast(timestamp);

		if (getIsErroneous(timestamp) || temp.getIsErroneous(timestamp) || this == temp) {
			return true;
		}

		if (info == null || noStructuredTypeCompatibility) {
			return this == temp;
		}

		switch (temp.getTypetype()) {
		case TYPE_ASN1_SET: {
			final ASN1_Set_Type tempType = (ASN1_Set_Type) temp;
			if (this == tempType) {
				return true;
			}
			if (getNofComponents() != tempType.getNofComponents()) {
				info.setErrorStr(NOFFIELDSDONTMATCH);
				return false;
			}
			TypeCompatibilityInfo.Chain lChain = leftChain;
			TypeCompatibilityInfo.Chain rChain = rightChain;
			if (lChain == null) {
				lChain = info.getChain();
				lChain.add(this);
			}
			if (rChain == null) {
				rChain = info.getChain();
				rChain.add(tempType);
			}
			for (int i = 0, size = getNofComponents(); i < size; i++) {
				final CompField cf = getComponentByIndex(i);
				final CompField tempTypeCompField = tempType.getComponentByIndex(i);
				final IType compFieldType = cf.getType().getTypeRefdLast(timestamp);
				final IType tempTypeCompFieldType = tempTypeCompField.getType().getTypeRefdLast(timestamp);
				if (cf.isOptional() != tempTypeCompField.isOptional()) {
					final String compFieldName = cf.getIdentifier().getDisplayName();
					final String tempTypeCompFieldName = tempTypeCompField.getIdentifier().getDisplayName();
					info.appendOp1Ref("." + compFieldName);
					info.appendOp2Ref("." + tempTypeCompFieldName);
					info.setOp1Type(compFieldType);
					info.setOp2Type(tempTypeCompFieldType);
					info.setErrorStr(BADOPTIONALITY);
					return false;
				}
				lChain.markState();
				rChain.markState();
				lChain.add(compFieldType);
				rChain.add(tempTypeCompFieldType);
				final TypeCompatibilityInfo infoTemp = new TypeCompatibilityInfo(compFieldType, tempTypeCompFieldType, false);
				if (!compFieldType.equals(tempTypeCompFieldType) && !(lChain.hasRecursion() && rChain.hasRecursion())
						&& !compFieldType.isCompatible(timestamp, tempTypeCompFieldType, infoTemp, lChain, rChain)) {
					final String compFieldName = cf.getIdentifier().getDisplayName();
					final String tempTypeCompFieldName = tempTypeCompField.getIdentifier().getDisplayName();
					info.appendOp1Ref("." + compFieldName + infoTemp.getOp1RefStr());
					info.appendOp2Ref("." + tempTypeCompFieldName + infoTemp.getOp2RefStr());
					info.setOp1Type(infoTemp.getOp1Type());
					info.setOp2Type(infoTemp.getOp2Type());
					info.setErrorStr(infoTemp.getErrorStr());
					lChain.previousState();
					rChain.previousState();
					return false;
				}
				lChain.previousState();
				rChain.previousState();
			}
			info.setNeedsConversion(true);
			return true;
		}
		case TYPE_TTCN3_SET: {
			final TTCN3_Set_Type tempType = (TTCN3_Set_Type) temp;
			if (getNofComponents() != tempType.getNofComponents()) {
				info.setErrorStr(NOFFIELDSDONTMATCH);
				return false;
			}
			TypeCompatibilityInfo.Chain lChain = leftChain;
			TypeCompatibilityInfo.Chain rChain = rightChain;
			if (lChain == null) {
				lChain = info.getChain();
				lChain.add(this);
			}
			if (rChain == null) {
				rChain = info.getChain();
				rChain.add(tempType);
			}
			for (int i = 0, size = getNofComponents(); i < size; i++) {
				final CompField compField = getComponentByIndex(i);
				final CompField tempTypeCompField = tempType.getComponentByIndex(i);
				final IType compFieldType = compField.getType().getTypeRefdLast(timestamp);
				final IType tempTypeCompFieldType = tempTypeCompField.getType().getTypeRefdLast(timestamp);
				if (compField.isOptional() != tempTypeCompField.isOptional()) {
					final String compFieldName = compField.getIdentifier().getDisplayName();
					final String tempTypeCompFieldName = tempTypeCompField.getIdentifier().getDisplayName();
					info.appendOp1Ref("." + compFieldName);
					info.appendOp2Ref("." + tempTypeCompFieldName);
					info.setOp1Type(compFieldType);
					info.setOp2Type(tempTypeCompFieldType);
					info.setErrorStr(BADOPTIONALITY);
					return false;
				}
				lChain.markState();
				rChain.markState();
				lChain.add(compFieldType);
				rChain.add(tempTypeCompFieldType);
				final TypeCompatibilityInfo infoTemp = new TypeCompatibilityInfo(compFieldType, tempTypeCompFieldType, false);
				if (!compFieldType.equals(tempTypeCompFieldType) && !(lChain.hasRecursion() && rChain.hasRecursion())
						&& !compFieldType.isCompatible(timestamp, tempTypeCompFieldType, infoTemp, lChain, rChain)) {
					final String compFieldName = compField.getIdentifier().getDisplayName();
					final String tempTypeCompFieldName = tempTypeCompField.getIdentifier().getDisplayName();
					info.appendOp1Ref("." + compFieldName + infoTemp.getOp1RefStr());
					info.appendOp2Ref("." + tempTypeCompFieldName + infoTemp.getOp2RefStr());
					info.setOp1Type(infoTemp.getOp1Type());
					info.setOp2Type(infoTemp.getOp2Type());
					info.setErrorStr(infoTemp.getErrorStr());
					lChain.previousState();
					rChain.previousState();
					return false;
				}
				lChain.previousState();
				rChain.previousState();
			}
			info.setNeedsConversion(true);
			return true;
		}
		case TYPE_SET_OF: {
			final SetOf_Type tempType = (SetOf_Type) temp;
			if (!tempType.isSubtypeCompatible(timestamp, this)) {
				info.setErrorStr("Incompatible set of/SET OF subtypes");
				return false;
			}

			final int nofComps = getNofComponents();
			if (nofComps == 0) {
				return false;
			}
			TypeCompatibilityInfo.Chain lChain = leftChain;
			TypeCompatibilityInfo.Chain rChain = rightChain;
			if (lChain == null) {
				lChain = info.getChain();
				lChain.add(this);
			}
			if (rChain == null) {
				rChain = info.getChain();
				rChain.add(tempType);
			}
			for (int i = 0; i < nofComps; i++) {
				final CompField compField = getComponentByIndex(i);
				final IType compFieldType = compField.getType().getTypeRefdLast(timestamp);
				final IType temporalTypeOfType = tempType.getOfType().getTypeRefdLast(timestamp);
				lChain.markState();
				rChain.markState();
				lChain.add(compFieldType);
				rChain.add(temporalTypeOfType);
				final TypeCompatibilityInfo infoTemp = new TypeCompatibilityInfo(compFieldType, temporalTypeOfType, false);
				if (!compFieldType.equals(temporalTypeOfType) && !(lChain.hasRecursion() && rChain.hasRecursion())
						&& !compFieldType.isCompatible(timestamp, temporalTypeOfType, infoTemp, lChain, rChain)) {
					info.appendOp1Ref("." + compField.getIdentifier().getDisplayName() + infoTemp.getOp1RefStr());
					if (infoTemp.getOp2RefStr().length() > 0) {
						info.appendOp2Ref("[]");
					}
					info.appendOp2Ref(infoTemp.getOp2RefStr());
					info.setOp1Type(infoTemp.getOp1Type());
					info.setOp2Type(infoTemp.getOp2Type());
					info.setErrorStr(infoTemp.getErrorStr());
					lChain.previousState();
					rChain.previousState();
					return false;
				}
				lChain.previousState();
				rChain.previousState();
			}
			info.setNeedsConversion(true);
			return true;
		}
		case TYPE_ASN1_CHOICE:
		case TYPE_TTCN3_CHOICE:
		case TYPE_ANYTYPE:
			info.setErrorStr(NOTCOMPATIBLEUNIONANYTYPE);
			return false;
		case TYPE_ASN1_SEQUENCE:
		case TYPE_TTCN3_SEQUENCE:
		case TYPE_SEQUENCE_OF:
		case TYPE_ARRAY:
			info.setErrorStr(NOTCOMPATIBLESETSETOF);
			return false;
		default:
			return false;
		}
	}

	@Override
	/** {@inheritDoc} */
	public String getOutlineIcon() {
		return "set.gif";
	}

	@Override
	/** {@inheritDoc} */
	public StringBuilder getProposalDescription(final StringBuilder builder) {
		return builder.append("set");
	}

	@Override
	/** {@inheritDoc} */
	public void checkRecursions(final CompilationTimeStamp timestamp, final IReferenceChain referenceChain) {
		if (components == null) {
			return;
		}

		if (referenceChain.add(this)) {
			CompField field;
			IType t;
			for (int i = 0; i < components.getNofComps(); i++) {
				field = components.getCompByIndex(i);
				t = field.getType();
				if (!field.isOptional() && t != null) {
					referenceChain.markState();
					t.checkRecursions(timestamp, referenceChain);
					referenceChain.previousState();
				}
			}
		}
	}

	@Override
	/** {@inheritDoc} */
	public void check(final CompilationTimeStamp timestamp) {
		if (lastTimeChecked != null && !lastTimeChecked.isLess(timestamp)) {
			return;
		}

		lastTimeChecked = timestamp;
		if (components != null && myScope != null) {
			final Module module = myScope.getModuleScope();
			if (module != null) {
				if (module.getSkippedFromSemanticChecking()) {
					lastTimeChecked = timestamp;
					return;
				}
			}
		}
		isErroneous = false;

		if (components == null) {
			parseBlockSet();
		}

		if (isErroneous || components == null) {
			return;
		}

		trCompsof(timestamp, null);
		components.check(timestamp);
		// ctss.chk_tags()

		if (constraints != null) {
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
		if (getIsErroneous(timestamp)) {
			return false;
		}

		boolean selfReference = super.checkThisValue(timestamp, value, lhs, valueCheckingOptions);

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

		switch (last.getValuetype()) {
		case SEQUENCE_VALUE:
			last = last.setValuetype(timestamp, Value_type.SET_VALUE);
			if (last.isAsn()) {
				selfReference = checkThisValueSet_A(timestamp, (Set_Value) last, lhs, valueCheckingOptions.expected_value,
						valueCheckingOptions.implicit_omit, valueCheckingOptions.str_elem);
			} else {
				selfReference = checkThisValueSet_T(timestamp, (Set_Value) last, lhs, valueCheckingOptions.expected_value,
						valueCheckingOptions.incomplete_allowed, valueCheckingOptions.implicit_omit,
						valueCheckingOptions.str_elem);
			}
			break;
		case SEQUENCEOF_VALUE:
			if (((SequenceOf_Value) last).isIndexed()) {
				value.getLocation().reportSemanticError(
						MessageFormat.format(
								"Indexed assignment notation cannot be used for SET type `{0}''",
								getFullName()));
				value.setIsErroneous(true);
			} else {
				final SequenceOf_Value temporalValue = (SequenceOf_Value) last;
				if (temporalValue.getNofComponents() == 0) {
					if (getNofComponents() == 0) {
						last = last.setValuetype(timestamp, Value_type.SET_VALUE);
					} else {
						value.getLocation().reportSemanticError(MessageFormat.format(NONEMPTYEXPECTED, getFullName()));
						value.setIsErroneous(true);
					}
				} else {
					value.getLocation().reportSemanticError(
							MessageFormat.format(last.isAsn() ? VALUELISTNOTATIONERRORASN1 : VALUELISTNOTATIONERRORTTCN3,
									getFullName()));
					value.setIsErroneous(true);
				}
			}
			break;
		case SET_VALUE:
			if (last.isAsn()) {
				selfReference = checkThisValueSet_A(timestamp, (Set_Value) last, lhs, valueCheckingOptions.expected_value,
						valueCheckingOptions.implicit_omit, valueCheckingOptions.str_elem);
			} else {
				selfReference = checkThisValueSet_T(timestamp, (Set_Value) last, lhs, valueCheckingOptions.expected_value,
						valueCheckingOptions.incomplete_allowed, valueCheckingOptions.implicit_omit,
						valueCheckingOptions.str_elem);
			}
			break;
		case UNDEFINED_BLOCK:
			last = last.setValuetype(timestamp, Value_type.SET_VALUE);
			selfReference = checkThisValueSet_A(timestamp, (Set_Value) last, lhs, valueCheckingOptions.expected_value,
					valueCheckingOptions.implicit_omit, valueCheckingOptions.str_elem);
			break;
		case EXPRESSION_VALUE:
		case MACRO_VALUE:
			// already checked
			break;
		default:
			value.getLocation().reportSemanticError(
					MessageFormat.format(last.isAsn() ? SETVALUEXPECTEDASN1 : SETVALUEXPECTEDTTCN3, getFullName()));
			value.setIsErroneous(true);
			break;
		}

		value.setLastTimeChecked(timestamp);

		return selfReference;
	}

	/**
	 * Checks the TTCN-3 Set_Value kind value against this type.
	 * <p>
	 * Please note, that this function can only be called once we know for
	 * sure that the value is of set type.
	 *
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle.
	 * @param value
	 *                the value to be checked
	 * @param expectedValue
	 *                the kind of value expected here.
	 * @param incompleteAllowed
	 *                whether incomplete value is allowed or not.
	 * @param implicitOmit
	 *                true if the implicit omit optional attribute was set
	 *                for the value, false otherwise
	 * */
	private boolean checkThisValueSet_T(final CompilationTimeStamp timestamp, final Set_Value value, final Assignment lhs, final Expected_Value_type expectedValue,
			final boolean incompleteAllowed, final boolean implicitOmit, final boolean strElem) {
		boolean selfReference = false;
		final Map<String, NamedValue> componentMap = new HashMap<String, NamedValue>();

		value.removeGeneratedValues();

		final int nofValueComponents = value.getNofComponents();
		for (int i = 0; i < nofValueComponents; i++) {
			final NamedValue namedValue = value.getSequenceValueByIndex(i);
			final Identifier valueId = namedValue.getName();
			if (!hasComponentWithName(valueId)) {
				namedValue.getLocation().reportSemanticError(
						MessageFormat.format(NONEXISTENTFIELDTTCN3, namedValue.getName().getDisplayName(), getTypename()));
			} else {
				if (componentMap.containsKey(valueId.getName())) {
					namedValue.getLocation().reportSemanticError(
							MessageFormat.format(DUPLICATEFIELDAGAINTTCN3, valueId.getDisplayName()));
					componentMap.get(valueId.getName()).getLocation().reportSingularSemanticError(
							MessageFormat.format(DUPLICATEFIELDFIRSTTTCN3, valueId.getDisplayName()));
				} else {
					componentMap.put(valueId.getName(), namedValue);
				}

				final CompField componentField = getComponentByName(valueId);
				final Type type = componentField.getType();
				final IValue componentValue = namedValue.getValue();

				if (componentValue != null) {
					componentValue.setMyGovernor(type);
					final IValue temporalValue = type.checkThisValueRef(timestamp, componentValue);
					boolean isOptional = componentField.isOptional();
					if (!isOptional && componentField.hasDefault() && defaultAsOptional) {
						isOptional = true;
					}
					selfReference |= type.checkThisValue(timestamp, temporalValue, lhs, new ValueCheckingOptions(expectedValue, incompleteAllowed,
							isOptional, true, implicitOmit, strElem));
				}
			}
		}

		if (!incompleteAllowed || implicitOmit || strictConstantCheckingSeverity) {
			final int nofTypeComponents = getNofComponents();
			for (int i = 0; i < nofTypeComponents; i++) {
				final CompField field = getComponentByIndex(i);
				final Identifier id = field.getIdentifier();
				if (!componentMap.containsKey(id.getName())) {
					if (field.isOptional() && implicitOmit) {
						value.addNamedValue(new NamedValue(new Identifier(Identifier_type.ID_ASN, id.getDisplayName()),
								new Omit_Value(), false));
					} else if (strictConstantCheckingSeverity) {
						value.getLocation().reportSemanticError(MessageFormat.format(MISSINGFIELDTTCN3, id.getDisplayName()));
					}
				}
			}
		}

		value.setLastTimeChecked(timestamp);

		return selfReference;
	}

	/**
	 * Checks the ASN.1 Set_Value kind value against this type.
	 * <p>
	 * Please note, that this function can only be called once we know for
	 * sure that the value is of set type.
	 *
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle.
	 * @param value
	 *                the value to be checked
	 * @param expectedValue
	 *                the kind of value expected here.
	 * @param implicitOmit
	 *                true if the implicit omit optional attribute was set
	 *                for the value, false otherwise
	 * */
	private boolean checkThisValueSet_A(final CompilationTimeStamp timestamp, final Set_Value value, final Assignment lhs, final Expected_Value_type expectedValue,
			final boolean implicitOmit, final boolean strElem) {
		boolean selfReference = false;
		final Map<String, NamedValue> componentMap = new HashMap<String, NamedValue>();

		value.removeGeneratedValues();

		final int nofValueComponents = value.getNofComponents();
		for (int i = 0; i < nofValueComponents; i++) {
			final NamedValue namedValue = value.getSequenceValueByIndex(i);
			final Identifier valueId = namedValue.getName();
			if (!hasComponentWithName(valueId)) {
				namedValue.getLocation().reportSemanticError(
						MessageFormat.format(NONEXISTENTFIELDASN1, namedValue.getName()
								.getDisplayName(), getTypename()));
			} else {
				if (componentMap.containsKey(valueId.getName())) {
					namedValue.getLocation().reportSemanticError(
							MessageFormat.format(DUPLICATEFIELDAGAINASN1,
									valueId.getDisplayName()));
					componentMap.get(valueId.getName()).getLocation().reportSingularSemanticError(
							MessageFormat.format(DUPLICATEFIELDFIRSTASN1, valueId.getDisplayName()));
				} else {
					componentMap.put(valueId.getName(), namedValue);
				}

				final CompField componentField = getComponentByName(valueId);
				final Type type = componentField.getType();
				final IValue componentValue = namedValue.getValue();

				if (componentValue != null) {
					componentValue.setMyGovernor(type);
					final IValue temporalValue = type.checkThisValueRef(timestamp, componentValue);
					selfReference |= type.checkThisValue(timestamp, temporalValue, lhs, new ValueCheckingOptions(expectedValue, false,
							false, true, implicitOmit, strElem));
				}
			}
		}

		final int nofTypeComponents = getNofComponents();
		for (int i = 0; i < nofTypeComponents; i++) {
			final CompField field = getComponentByIndex(i);
			final Identifier id = field.getIdentifier();
			if (!componentMap.containsKey(id.getName())) {
				if (field.isOptional() && implicitOmit) {
					value.addNamedValue(new NamedValue(new Identifier(Identifier_type.ID_ASN, id.getDisplayName()),
							new Omit_Value(), false));
				} else if (!field.isOptional() && !field.hasDefault()) {
					value.getLocation().reportSemanticError(MessageFormat.format(MISSINGFIELDASN1, id.getDisplayName()));
				}
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

		boolean selfReference = false;
		switch (template.getTemplatetype()) {
		case TEMPLATE_LIST:
			if (((Template_List) template).getNofTemplates() > 0) {
				template.getLocation().reportSemanticError(MessageFormat.format(VALUELISTNOTATIONNOTALLOWED, getFullName()));
				break;
			} else if (getNofComponents() > 0) {
				template.getLocation().reportSemanticError(MessageFormat.format(NONEMPTYSETTEMPLATEEXPECTED, getFullName()));
			} else {
				final ITTCN3Template transformed = template.setTemplatetype(timestamp, Template_type.NAMED_TEMPLATE_LIST);
				selfReference = checkThisNamedTemplateList(timestamp, (Named_Template_List) transformed, isModified, implicitOmit, lhs);
			}
			break;
		case NAMED_TEMPLATE_LIST:
			selfReference = checkThisNamedTemplateList(timestamp, (Named_Template_List) template, isModified, implicitOmit, lhs);
			break;
		default:
			template.getLocation().reportSemanticError(
					MessageFormat.format(TEMPLATENOTALLOWED, template.getTemplateTypeName(), getTypename()));
			break;
		}

		if (template.getLengthRestriction() != null) {
			template.getLocation().reportSemanticError(LENGTHRESTRICTIONNOTALLOWED);
		}

		return selfReference;
	}

	/**
	 * Checks the provided named template list against this type.
	 *
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle.
	 * @param templateList
	 *                the template list to check
	 * @param isModified
	 *                true if the template is modified otherwise false.
	 * @param implicitOmit
	 *                true it the template has implicit omit attribute set,
	 *                false otherwise.
	 * @param lhs
	 *                the assignment to check against
	 * @return true if the value contains a reference to lhs
	 * */
	private boolean checkThisNamedTemplateList(final CompilationTimeStamp timestamp, final Named_Template_List templateList,
			final boolean isModified, final boolean implicitOmit, final Assignment lhs) {
		templateList.removeGeneratedValues();

		boolean selfReference = false;
		final Map<String, NamedTemplate> componentMap = new HashMap<String, NamedTemplate>();
		final int nofTypeComponents = getNofComponents();
		final int nofTemplateComponents = templateList.getNofTemplates();

		for (int i = 0; i < nofTemplateComponents; i++) {
			final NamedTemplate namedTemplate = templateList.getTemplateByIndex(i);
			final Identifier identifier = namedTemplate.getName();
			final String templateName = identifier.getName();

			if (hasComponentWithName(identifier)) {
				if (componentMap.containsKey(templateName)) {
					namedTemplate.getLocation().reportSemanticError(
							MessageFormat.format(DUPLICATETEMPLATEFIELDFIRST, identifier.getDisplayName()));
					componentMap.get(templateName).getLocation().reportSemanticError(
							MessageFormat.format(DUPLICATETEMPLATEFIELDAGAIN, identifier.getDisplayName()));
				} else {
					componentMap.put(templateName, namedTemplate);
				}

				final CompField componentField = getComponentByName(identifier);
				final Type type = componentField.getType();
				if (type != null && !type.getIsErroneous(timestamp)) {
					ITTCN3Template componentTemplate = namedTemplate.getTemplate();
					componentTemplate.setMyGovernor(type);
					componentTemplate = type.checkThisTemplateRef(timestamp, componentTemplate);
					boolean isOptional = componentField.isOptional();
					if (!isOptional && componentField.hasDefault() && defaultAsOptional) {
						isOptional = true;
					}
					selfReference |= componentTemplate.checkThisTemplateGeneric(timestamp, type, isModified, isOptional, isOptional, true,
							implicitOmit, lhs);
				}
			} else {
				namedTemplate.getLocation().reportSemanticError(
						MessageFormat.format(NONEXISTENTTEMPLATEFIELDREFERENCE, identifier.getDisplayName(), getTypename()));
			}
		}

		if (!isModified || implicitOmit || strictConstantCheckingSeverity) {
			// check missing fields
			for (int i = 0; i < nofTypeComponents; i++) {
				final Identifier identifier = getComponentIdentifierByIndex(i);
				if (!componentMap.containsKey(identifier.getName())) {
					if (getComponentByIndex(i).isOptional() && implicitOmit) {
						templateList.addNamedValue(new NamedTemplate(new Identifier(Identifier_type.ID_ASN, identifier
								.getDisplayName()), new OmitValue_Template(), false));
					} else if (!isModified || strictConstantCheckingSeverity) {
						templateList.getLocation().reportSemanticError(
								MessageFormat.format(MISSINGTEMPLATEFIELD,
										identifier.getDisplayName(), getTypename()));
					}
				}
			}
		}

		return selfReference;
	}

	/** Parses the block as if it were the block of a set. */
	private void parseBlockSet() {
		if (null == mBlock) {
			return;
		}

		final Asn1Parser parser = BlockLevelTokenStreamTracker.getASN1ParserForBlock(mBlock);
		if (null == parser) {
			return;
		}

		components = parser.pr_special_ComponentTypeLists().list;
		final List<SyntacticErrorStorage> errors = parser.getErrorStorage();
		if (null != errors && !errors.isEmpty()) {
			isErroneous = true;
			components = null;
			for (int i = 0; i < errors.size(); i++) {
				ParserMarkerSupport.createOnTheFlyMixedMarker((IFile) mBlock.getLocation().getFile(), errors.get(i),
						IMarker.SEVERITY_ERROR);
			}
		}

		if (components == null) {
			isErroneous = true;
			return;
		}

		components.setFullNameParent(this);
		components.setMyScope(getMyScope());
		components.setMyType(this);
	}

	/**
	 * Check the components of member to reveal possible recursive
	 * referencing.
	 *
	 * @param timestamp
	 *                the actual compilation cycle.
	 * @param referenceChain
	 *                the reference chain used to detect recursive
	 *                referencing
	 * */
	public void trCompsof(final CompilationTimeStamp timestamp, final IReferenceChain referenceChain) {
		if (trCompsofTimestamp != null && !trCompsofTimestamp.isLess(timestamp)) {
			return;
		}

		if (referenceChain != null) {
			components.trCompsof(timestamp, referenceChain, false);
		} else {
			final IReferenceChain temporalReferenceChain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);

			components.trCompsof(timestamp, temporalReferenceChain, false);

			temporalReferenceChain.release();
		}

		trCompsofTimestamp = timestamp;
		components.trCompsof(timestamp, null, true);
	}

	@Override
	/** {@inheritDoc} */
	public void checkCodingAttributes(final CompilationTimeStamp timestamp, final IReferenceChain refChain) {
		checkJson(timestamp);
		//TODO add checks for other encodings.

		if (refChain.contains(this)) {
			return;
		}

		refChain.add(this);
		refChain.markState();
		for (int i = 0; i < getNofComponents(); i++) {
			final CompField cf = getComponentByIndex(i);

			cf.getType().checkCodingAttributes(timestamp, refChain);
		}
		refChain.previousState();
	}

	@Override
	/** {@inheritDoc} */
	public void forceJson(final CompilationTimeStamp timestamp) {
		if (jsonAttribute == null) {
			jsonAttribute = new JsonAST();
		}
	}

	@Override
	/** {@inheritDoc} */
	public void checkJson(final CompilationTimeStamp timestamp) {
		if (jsonAttribute == null && !hasEncodeAttribute("JSON")) {
			return;
		}

		for (int i = 0; i < getNofComponents(); i++) {
			final Type fieldType = getComponentByIndex(i).getType();
			fieldType.forceJson(timestamp);
		}

		if (jsonAttribute == null) {
			return;
		}

		if (jsonAttribute.omit_as_null && !isOptionalField()) {
			getLocation().reportSemanticError("Invalid attribute, 'omit as null' requires optional field of a record or set.");
		}

		if (jsonAttribute.as_value) {
			getLocation().reportSemanticError("Invalid attribute, 'as value' is only allowed for unions, the anytype, or records or sets with one field");
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

		if (jsonAttribute.parsed_default_value != null) {
			checkJsonDefault(timestamp);
		}

		//TODO: check schema extensions

		if (jsonAttribute.metainfo_unbound) {
			if (getParentType() == null || (getParentType().getTypetype() != Type_type.TYPE_TTCN3_SEQUENCE &&
					getParentType().getTypetype() != Type_type.TYPE_TTCN3_SET)) {
				// only allowed if it's an array type or a field of a record/set
				getLocation().reportSemanticError("Invalid attribute 'metainfo for unbound', requires record, set, record of, set of, array or field of a record or set");
			}
		}

		if (jsonAttribute.as_number) {
			getLocation().reportSemanticError("Invalid attribute, 'as number' is only allowed for enumerated types");
		}

		//FIXME: check tag_list

		if (jsonAttribute.as_map) {
			getLocation().reportSemanticError("Invalid attribute, 'as map' requires record of or set of");
		}

		if (jsonAttribute.enum_texts.size() > 0) {
			getLocation().reportSemanticError("Invalid attribute, 'text ... as ...' requires an enumerated type");
		}
	}

	// This is the same as in ASN1_Sequence_Type
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
			final Identifier id = subreference.getId();
			final CompField compField = components.getCompByName(id);
			if (compField == null) {
				subreference.getLocation().reportSemanticError(
						MessageFormat.format(FieldSubReference.NONEXISTENTSUBREFERENCE, ((FieldSubReference) subreference)
								.getId().getDisplayName(), getTypename()));
				return null;
			}

			if (interruptIfOptional && compField.isOptional()) {
				return null;
			}

			final Expected_Value_type internalExpectation = (expectedIndex == Expected_Value_type.EXPECTED_TEMPLATE) ? Expected_Value_type.EXPECTED_DYNAMIC_VALUE
					: expectedIndex;

			return compField.getType().getFieldType(timestamp, reference, actualSubReference + 1, internalExpectation, refChain,
					interruptIfOptional);
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

	// This is the same as in ASN1_Sequence_type
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
		case fieldSubReference: {
			final Identifier id = subreference.getId();
			final CompField compField = components.getCompByName(id);
			if (compField == null) {
				subreference.getLocation().reportSemanticError(
						MessageFormat.format(FieldSubReference.NONEXISTENTSUBREFERENCE, ((FieldSubReference) subreference)
								.getId().getDisplayName(), getTypename()));
				return false;
			}

			final IType fieldType = compField.getType();
			if (fieldType == null) {
				return false;
			}

			final int fieldIndex = components.indexOf(compField);
			subrefsArray.add(fieldIndex);
			typeArray.add(this);
			return fieldType.getSubrefsAsArray(timestamp, reference, actualSubReference + 1, subrefsArray, typeArray);
		}
		case parameterisedSubReference:
			subreference.getLocation().reportSemanticError(
					MessageFormat.format(FieldSubReference.INVALIDSUBREFERENCE, ((ParameterisedSubReference) subreference)
							.getId().getDisplayName(), getTypename()));
			return false;
		default:
			subreference.getLocation().reportSemanticError(ISubReference.INVALIDSUBREFERENCE);
			return false;
		}
	}

	@Override
	/** {@inheritDoc} */
	public String getGenNameValue(final JavaGenData aData, final StringBuilder source) {
		return getGenNameOwn(aData);
	}

	@Override
	/** {@inheritDoc} */
	public String getGenNameTemplate(final JavaGenData aData, final StringBuilder source) {
		return getGenNameOwn(aData).concat("_template");
	}

	@Override
	/** {@inheritDoc} */
	public String getGenNameTypeDescriptor(final JavaGenData aData, final StringBuilder source) {
		final String baseName = getGenNameTypeName(aData, source);
		return baseName + "." + getGenNameOwn();
	}

	@Override
	/** {@inheritDoc} */
	public boolean generatesOwnClass(final JavaGenData aData, final StringBuilder source) {
		return true;
	}

	@Override
	/** {@inheritDoc} */
	public String getGenNameJsonDescriptor(final JavaGenData aData, final StringBuilder source) {
		return getGenNameOwn(aData) + "." + getGenNameOwn() + "_json_";
	}

	@Override
	/** {@inheritDoc} */
	public void generateCode( final JavaGenData aData, final StringBuilder source ) {
		if (lastTimeGenerated != null && !lastTimeGenerated.isLess(aData.getBuildTimstamp())) {
			return;
		}

		lastTimeGenerated = aData.getBuildTimstamp();

		final String className = getGenNameOwn();
		final String classReadableName = getFullName();

		final StringBuilder localTypeDescriptor = new StringBuilder();
		final HashMap<String, String> attributeRegistry = new HashMap<String, String>();
		generateCodeTypedescriptor(aData, source, localTypeDescriptor, attributeRegistry);
		generateCodeDefaultCoding(aData, source, localTypeDescriptor);
		final StringBuilder localCodingHandler = new StringBuilder();
		generateCodeForCodingHandlers(aData, source, localCodingHandler);

		final List<FieldInfo> namesList =  new ArrayList<FieldInfo>();
		boolean hasOptional = false;
		for ( int i = 0; i < components.getNofComps(); i++) {
			final CompField compField = components.getCompByIndex(i);
			final IType cfType = compField.getType();
			final IType lastType = cfType.getTypeRefdLast(CompilationTimeStamp.getBaseTimestamp());
			boolean ofType;
			switch (lastType.getTypetype()) {
			case TYPE_SEQUENCE_OF:
			case TYPE_SET_OF:
				ofType = true;
				break;
			default:
				ofType = false;
				break;
			}

			switch (cfType.getTypetype()) {
			case TYPE_ASN1_CHOICE:
			case TYPE_ASN1_ENUMERATED:
			case TYPE_ASN1_SEQUENCE:
			case TYPE_ASN1_SET:
			case TYPE_SEQUENCE_OF:
			case TYPE_SET_OF:
				if (!cfType.generatesOwnClass(aData, source)) {
					cfType.generateCodeTypedescriptor(aData, source, localTypeDescriptor, attributeRegistry);
					cfType.generateCodeDefaultCoding(aData, source, localTypeDescriptor);
					cfType.generateCodeForCodingHandlers(aData, source, localCodingHandler);
				}
				break;
			default:
				cfType.generateCodeTypedescriptor(aData, source, localTypeDescriptor, attributeRegistry);
				cfType.generateCodeDefaultCoding(aData, source, localTypeDescriptor);
				cfType.generateCodeForCodingHandlers(aData, source, localCodingHandler);
				break;
			}

			final JsonAST jsonAttribute = cfType.getJsonAttribute();
			final FieldInfo fi = new FieldInfo(cfType.getGenNameValue( aData, source ),
					cfType.getGenNameTemplate( aData, source ),
					compField.getIdentifier().getName(), compField.getIdentifier().getDisplayName(), compField.isOptional(),
					ofType, compField.getType().getClass().getSimpleName(), cfType.getGenNameTypeDescriptor(aData, source),
					jsonAttribute != null ? jsonAttribute.metainfo_unbound : false,
					jsonAttribute != null ? jsonAttribute.parsed_default_value : null,
					null,
					jsonAttribute != null ? jsonAttribute.alias : null,
					jsonAttribute != null ? jsonAttribute.omit_as_null : false);
			hasOptional |= compField.isOptional();
			namesList.add( fi );
		}

		for ( int i = 0; i < components.getNofComps(); i++) {
			final CompField compField = components.getCompByIndex(i);
			final StringBuilder tempSource = aData.getCodeForType(compField.getType().getGenNameOwn());
			compField.getType().generateCode(aData, tempSource);

			if (compField.hasDefault()) {
				final Value defaultValue = compField.getDefault();
				final StringBuilder defaultValueSource = new StringBuilder();
				final Type type = compField.getType();
				final String typeGeneratedName = type.getGenNameValue( aData, defaultValueSource );
				if (type.getTypetype().equals(Type_type.TYPE_ARRAY)) {
					final Array_Type arrayType = (Array_Type) type;
					final StringBuilder temp_sb = aData.getCodeForType(arrayType.getGenNameOwn());
					arrayType.generateCodeValue(aData, temp_sb);
				}

				defaultValueSource.append(MessageFormat.format("\tpublic static final {0} {1} = new {0}();\n", typeGeneratedName, defaultValue.get_lhs_name()));
				defaultValue.generateCodeInit( aData, aData.getPreInit(), defaultValue.getGenNameOwn() );

				aData.addGlobalVariable(typeGeneratedName, defaultValueSource.toString());
			}
		}

		final boolean hasRaw = getGenerateCoderFunctions(MessageEncoding_type.RAW);
		final boolean hasJson = getGenerateCoderFunctions(MessageEncoding_type.JSON);
		final boolean jsonAsValue = jsonAttribute != null ? jsonAttribute.as_value : false;
		final boolean jsonAsMapPossible = jsonAttribute != null ? jsonAttribute.as_map : false;

		RecordSetCodeGenerator.generateValueClass(aData, source, className, classReadableName, namesList, hasOptional, true, hasRaw, null, hasJson, jsonAsValue, jsonAsMapPossible, localTypeDescriptor, localCodingHandler);
		RecordSetCodeGenerator.generateTemplateClass(aData, source, className, classReadableName, namesList, hasOptional, true);
	}

	@Override
	/** {@inheritDoc} */
	public boolean isPresentAnyvalueEmbeddedField(final ExpressionStruct expression, final List<ISubReference> subreferences, final int beginIndex) {
		if (subreferences == null || getIsErroneous(CompilationTimeStamp.getBaseTimestamp())) {
			return true;
		}

		if (beginIndex >= subreferences.size()) {
			return true;
		}

		final ISubReference subReference = subreferences.get(beginIndex);
		if (!(subReference instanceof FieldSubReference)) {
			ErrorReporter.INTERNAL_ERROR("Code generator reached erroneous type reference `" + getFullName() + "''");
			expression.expression.append("FATAL_ERROR encountered while processing `" + getFullName() + "''\n");
			return true;
		}

		final Identifier fieldId = ((FieldSubReference) subReference).getId();
		final CompField compField = getComponentByName(fieldId);
		if (compField.isOptional()) {
			return false;
		}

		return compField.getType().isPresentAnyvalueEmbeddedField(expression, subreferences, beginIndex + 1);
	}

	@Override
	public String generateConversion(final JavaGenData aData, final IType fromType, final String fromName, final boolean forValue, final ExpressionStruct expression) {
		final IType refdType = fromType.getTypeRefdLast(CompilationTimeStamp.getBaseTimestamp());
		if (refdType == null || this == refdType) {
			//no need to convert
			return fromName;
		}

		switch (refdType.getTypetype()) {
		case TYPE_TTCN3_SET: {
			//heavy conversion is needed
			final TTCN3_Set_Seq_Choice_BaseType realFromType = (TTCN3_Set_Seq_Choice_BaseType) refdType;
			return generateConversionTTCNSetSeqToASNSetSeq(aData, realFromType, fromName, forValue, expression);
		}
		case TYPE_ASN1_SET: {
			//heavy conversion is needed
			final ASN1_Set_Seq_Choice_BaseType realFromType = (ASN1_Set_Seq_Choice_BaseType) refdType;
			return generateConversionASNSetSeqToASNSetSeq(aData, realFromType, fromName, forValue, expression);
		}
		default:
			expression.expression.append(MessageFormat.format("//FIXME conversion from {0} to {1} is not needed or nor supported yet\n", fromType.getTypename(), getTypename()));
			break;
		}

		// the default implementation does nothing
		return fromName;
	}
}
