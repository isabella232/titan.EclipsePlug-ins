/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.types;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.Activator;
import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.FieldSubReference;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.ISubReference;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.IValue.Value_type;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.Identifier.Identifier_type;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.Type;
import org.eclipse.titan.designer.AST.TypeCompatibilityInfo;
import org.eclipse.titan.designer.AST.ASN1.types.ASN1_Set_Type;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.attributes.RawAST;
import org.eclipse.titan.designer.AST.TTCN3.attributes.RawASTStruct;
import org.eclipse.titan.designer.AST.TTCN3.templates.ITTCN3Template;
import org.eclipse.titan.designer.AST.TTCN3.templates.ITTCN3Template.Template_type;
import org.eclipse.titan.designer.AST.TTCN3.templates.NamedTemplate;
import org.eclipse.titan.designer.AST.TTCN3.templates.Named_Template_List;
import org.eclipse.titan.designer.AST.TTCN3.templates.OmitValue_Template;
import org.eclipse.titan.designer.AST.TTCN3.templates.Template_List;
import org.eclipse.titan.designer.AST.TTCN3.types.RecordSetCodeGenerator.FieldInfo;
import org.eclipse.titan.designer.AST.TTCN3.types.subtypes.SubType;
import org.eclipse.titan.designer.AST.TTCN3.values.NamedValue;
import org.eclipse.titan.designer.AST.TTCN3.values.Omit_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.SequenceOf_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Set_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.ExpressionStruct;
import org.eclipse.titan.designer.compiler.BuildTimestamp;
import org.eclipse.titan.designer.compiler.JavaGenData;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.preferences.PreferenceConstants;
import org.eclipse.titan.designer.productUtilities.ProductConstants;

/**
 * @author Kristof Szabados
 * */
public final class TTCN3_Set_Type extends TTCN3_Set_Seq_Choice_BaseType {
	public static final String INCOMPLETEPRESENTERROR = "Not used symbol `-' is not allowed in this context";
	private static final String UNSUPPORTED_FIELDNAME =
			"Sorry, but it is not supported for set types to have a field with a name (`{0}'') "
					+ "which exactly matches the name of the type definition.";
	private static final String NONEMPTYEXPECTED = "A non-empty value was expected for type `{0}''";

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
	private static final String TEMPLATENOTALLOWED = "{0} cannot be used for set type `{1}''";
	private static final String LENGTHRESTRICTIONNOTALLOWED = "Length restriction is not allowed for set type `{0}''";
	private static final String DUPLICATETEMPLATEFIELDFIRST = "Duplicate field `{0}'' in template";
	private static final String DUPLICATETEMPLATEFIELDAGAIN = "Field `{0}'' is already given here";
	private static final String NONEXISTENTTEMPLATEFIELDREFERENCE = "Reference to non-existing field `{0}'' in set template for type `{1}''";
	private static final String MISSINGTEMPLATEFIELD = "Field `{0}'' is missing from template for set type `{1}''";

	private static final String NOFFIELDSDONTMATCH = "The number of fields in set/SET types must be the same";
	private static final String BADOPTIONALITY = "The optionality of fields in set/SET types must be the same";
	private static final String NOTCOMPATIBLESETSETOF = "set/SET and set of/SET OF types are compatible only with other set/SET and set of/SET OF types";
	private static final String NOTCOMPATIBLEUNIONANYTYPE = "union/CHOICE/anytype types are compatible only with other union/CHOICE/anytype types";

	// The actual value of the severity level to report stricter constant checking on.
	private static boolean strictConstantCheckingSeverity;

	static {
		final IPreferencesService ps = Platform.getPreferencesService();
		if ( ps != null ) {
			strictConstantCheckingSeverity = ps.getBoolean(
					ProductConstants.PRODUCT_ID_DESIGNER, PreferenceConstants.REPORT_STRICT_CONSTANTS, false, null);

			final Activator activator = Activator.getDefault();
			if (activator != null) {
				activator.getPreferenceStore().addPropertyChangeListener(new IPropertyChangeListener() {

					@Override
					public void propertyChange(final PropertyChangeEvent event) {
						final String property = event.getProperty();
						if (PreferenceConstants.REPORT_STRICT_CONSTANTS.equals(property)) {
							strictConstantCheckingSeverity = ps.getBoolean(ProductConstants.PRODUCT_ID_DESIGNER,
									PreferenceConstants.REPORT_STRICT_CONSTANTS, false, null);
						}
					}
				});
			}
		}
	}

	public TTCN3_Set_Type(final CompFieldMap compFieldMap) {
		super(compFieldMap);
	}

	@Override
	/** {@inheritDoc} */
	public Type_type getTypetype() {
		return Type_type.TYPE_TTCN3_SET;
	}

	@Override
	/** {@inheritDoc} */
	public SubType.SubType_type getSubtypeType() {
		return SubType.SubType_type.ST_SET;
	}

	@Override
	/** {@inheritDoc} */
	public boolean isCompatible(final CompilationTimeStamp timestamp, final IType otherType,
			final TypeCompatibilityInfo info, final TypeCompatibilityInfo.Chain leftChain, final TypeCompatibilityInfo.Chain rightChain) {
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
			if (getNofComponents() != tempType.getNofComponents(timestamp)) {
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
				final CompField tempTypeCf = tempType.getComponentByIndex(i);
				final IType cfType = cf.getType().getTypeRefdLast(timestamp);
				final IType tempTypeCfType = tempTypeCf.getType().getTypeRefdLast(timestamp);
				if (cf.isOptional() != tempTypeCf.isOptional()) {
					final String cfName = cf.getIdentifier().getDisplayName();
					final String tempTypeCfName = tempTypeCf.getIdentifier().getDisplayName();
					info.appendOp1Ref("." + cfName);
					info.appendOp2Ref("." + tempTypeCfName);
					info.setOp1Type(cfType);
					info.setOp2Type(tempTypeCfType);
					info.setErrorStr(BADOPTIONALITY);
					return false;
				}
				lChain.markState();
				rChain.markState();
				lChain.add(cfType);
				rChain.add(tempTypeCfType);
				final TypeCompatibilityInfo infoTemp = new TypeCompatibilityInfo(cfType, tempTypeCfType, false);
				if (!cfType.equals(tempTypeCfType)
						&& !(lChain.hasRecursion() && rChain.hasRecursion())
						&& !cfType.isCompatible(timestamp, tempTypeCfType, infoTemp, lChain, rChain)) {
					final String cfName = cf.getIdentifier().getDisplayName();
					final String tempTypeCfName = tempTypeCf.getIdentifier().getDisplayName();
					info.appendOp1Ref("." + cfName + infoTemp.getOp1RefStr());
					info.appendOp2Ref("." + tempTypeCfName + infoTemp.getOp2RefStr());
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
				final CompField tempTypeCf = tempType.getComponentByIndex(i);
				final IType cfType = cf.getType().getTypeRefdLast(timestamp);
				final IType tempTypeCfType = tempTypeCf.getType().getTypeRefdLast(timestamp);
				if (cf.isOptional() != tempTypeCf.isOptional()) {
					final String cfName = cf.getIdentifier().getDisplayName();
					final String tempTypeCompFieldName = tempTypeCf.getIdentifier().getDisplayName();
					info.appendOp1Ref("." + cfName);
					info.appendOp2Ref("." + tempTypeCompFieldName);
					info.setOp1Type(cfType);
					info.setOp2Type(tempTypeCfType);
					info.setErrorStr(BADOPTIONALITY);
					return false;
				}

				final TypeCompatibilityInfo infoTemp = new TypeCompatibilityInfo(cfType, tempTypeCfType, false);
				lChain.markState();
				rChain.markState();
				lChain.add(cfType);
				rChain.add(tempTypeCfType);
				if (!cfType.equals(tempTypeCfType)
						&& !(lChain.hasRecursion() && rChain.hasRecursion())
						&& !cfType.isCompatible(timestamp, tempTypeCfType, infoTemp, lChain, rChain)) {
					final String cfName = cf.getIdentifier().getDisplayName();
					final String tempTypeCfName = tempTypeCf.getIdentifier().getDisplayName();
					info.appendOp1Ref("." + cfName + infoTemp.getOp1RefStr());
					info.appendOp2Ref("." + tempTypeCfName + infoTemp.getOp2RefStr());
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
				final CompField cf = getComponentByIndex(i);
				final IType cfType = cf.getType().getTypeRefdLast(timestamp);
				final IType tempTypeOfType = tempType.getOfType().getTypeRefdLast(timestamp);
				final TypeCompatibilityInfo infoTemp = new TypeCompatibilityInfo(cfType, tempTypeOfType, false);
				lChain.markState();
				rChain.markState();
				lChain.add(cfType);
				rChain.add(tempTypeOfType);
				if (!cfType.equals(tempTypeOfType)
						&& !(lChain.hasRecursion() && rChain.hasRecursion())
						&& !cfType.isCompatible(timestamp, tempTypeOfType, infoTemp, lChain, rChain)) {
					info.appendOp1Ref("." + cf.getIdentifier().getDisplayName() + infoTemp.getOp1RefStr());
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
			}
			info.setNeedsConversion(true);
			lChain.previousState();
			rChain.previousState();
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
	public boolean isIdentical(final CompilationTimeStamp timestamp, final IType type) {
		check(timestamp);
		type.check(timestamp);
		final IType temp = type.getTypeRefdLast(timestamp);

		if (getIsErroneous(timestamp) || temp.getIsErroneous(timestamp)) {
			return true;
		}

		return this == temp;
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
		return getFullName();
	}

	@Override
	/** {@inheritDoc} */
	public String getOutlineIcon() {
		return "set.gif";
	}

	@Override
	/** {@inheritDoc} */
	public void checkConstructorName(final String definitionName) {
		if (hasComponentWithName(definitionName)) {
			final CompField field = getComponentByName(definitionName);
			field.getIdentifier().getLocation().reportSemanticError(MessageFormat.format(UNSUPPORTED_FIELDNAME, field.getIdentifier().getDisplayName()));
		}
	}

	@Override
	/** {@inheritDoc} */
	public void checkRecursions(final CompilationTimeStamp timestamp, final IReferenceChain referenceChain) {
		if (referenceChain.add(this)) {
			CompField field;
			IType type;
			for (int i = 0, size = getNofComponents(); i < size; i++) {
				field = getComponentByIndex(i);
				type = field.getType();
				if (!field.isOptional() && type != null) {
					referenceChain.markState();
					type.checkRecursions(timestamp, referenceChain);
					referenceChain.previousState();
				}
			}
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
				selfReference = checkThisValueSet(timestamp, (Set_Value) last, lhs, valueCheckingOptions.expected_value,
						false, valueCheckingOptions.implicit_omit, valueCheckingOptions.str_elem);
			} else {
				selfReference = checkThisValueSet(timestamp, (Set_Value) last, lhs, valueCheckingOptions.expected_value,
						valueCheckingOptions.incomplete_allowed, valueCheckingOptions.implicit_omit, valueCheckingOptions.str_elem);
			}
			break;
		case SEQUENCEOF_VALUE:
			if (((SequenceOf_Value) last).isIndexed()) {
				value.getLocation().reportSemanticError(MessageFormat.format("Indexed assignment notation cannot be used for set type `{0}''", getFullName()));
				value.setIsErroneous(true);
			} else {
				final SequenceOf_Value tempValue = (SequenceOf_Value) last;
				if (tempValue.getNofComponents() == 0) {
					if (compFieldMap != null && compFieldMap.getComponentFieldMap(timestamp).isEmpty()) {
						last = last.setValuetype(timestamp, Value_type.SET_VALUE);
					} else {
						value.getLocation().reportSemanticError(MessageFormat.format(NONEMPTYEXPECTED, getFullName()));
						value.setIsErroneous(true);
					}
				} else {
					value.getLocation().reportSemanticError(
							MessageFormat.format(last.isAsn() ? VALUELISTNOTATIONERRORASN1 : VALUELISTNOTATIONERRORTTCN3, getFullName()));
					value.setIsErroneous(true);
				}
			}
			break;
		case SET_VALUE:
			if (last.isAsn()) {
				selfReference = checkThisValueSet(timestamp, (Set_Value) last, lhs, valueCheckingOptions.expected_value,
						false, valueCheckingOptions.implicit_omit, valueCheckingOptions.str_elem);
			} else {
				selfReference = checkThisValueSet(timestamp, (Set_Value) last, lhs, valueCheckingOptions.expected_value,
						valueCheckingOptions.incomplete_allowed, valueCheckingOptions.implicit_omit, valueCheckingOptions.str_elem);
			}
			break;
		case UNDEFINED_BLOCK:
			last = last.setValuetype(timestamp, Value_type.SET_VALUE);
			selfReference = checkThisValueSet(timestamp, (Set_Value) last, lhs,
					valueCheckingOptions.expected_value, false, valueCheckingOptions.implicit_omit, valueCheckingOptions.str_elem);
			break;
		case EXPRESSION_VALUE:
		case MACRO_VALUE:
			// already checked
			break;
		default:
			value.getLocation().reportSemanticError(MessageFormat.format(last.isAsn() ? SETVALUEXPECTEDASN1 : SETVALUEXPECTEDTTCN3, getFullName()));
			value.setIsErroneous(true);
		}

		if (valueCheckingOptions.sub_check) {
			//there is no parent type to check
			if (subType != null) {
				subType.checkThisValue(timestamp, last);
			}
		}

		value.setLastTimeChecked(timestamp);

		return selfReference;
	}

	/**
	 * Checks the Set_Value kind value against this type.
	 * <p>
	 * Please note, that this function can only be called once we know for sure
	 * that the value is of set type.
	 *
	 * @param timestamp the timestamp of the actual semantic check cycle.
	 * @param value the value to be checked
	 * @param expectedValue the kind of value we expect to find.
	 * @param incompleteAllowed wheather incomplete value is allowed or not.
	 * @param impliciOmit true if the implicit omit optional attribute was set
	 *            for the value, false otherwise
	 * */
	private boolean checkThisValueSet(final CompilationTimeStamp timestamp, final Set_Value value, final Assignment lhs, final Expected_Value_type expectedValue,
			final boolean incompleteAllowed, final boolean impliciOmit, final boolean strElem) {
		value.removeGeneratedValues();

		boolean selfReference = false;
		final Map<String, NamedValue> componentMap = new HashMap<String, NamedValue>();
		final Map<String, CompField> realComponents = compFieldMap.getComponentFieldMap(timestamp);

		final boolean isAsn = value.isAsn();
		final int nofValueComponents = value.getNofComponents();
		for (int i = 0; i < nofValueComponents; i++) {
			final NamedValue namedValue = value.getSequenceValueByIndex(i);
			final Identifier valueId = namedValue.getName();
			if (!realComponents.containsKey(valueId.getName())) {
				namedValue.getLocation().reportSemanticError(
						MessageFormat.format(isAsn ? NONEXISTENTFIELDASN1 : NONEXISTENTFIELDTTCN3, namedValue.getName().getDisplayName(),
								getTypename()));
			} else {
				if (componentMap.containsKey(valueId.getName())) {
					namedValue.getLocation().reportSemanticError(
							MessageFormat.format(isAsn ? DUPLICATEFIELDAGAINASN1 : DUPLICATEFIELDAGAINTTCN3, valueId.getDisplayName()));
					componentMap.get(valueId.getName()).getLocation().reportSingularSemanticError(
							MessageFormat.format(isAsn ? DUPLICATEFIELDFIRSTASN1 : DUPLICATEFIELDFIRSTTTCN3, valueId.getDisplayName()));
				} else {
					componentMap.put(valueId.getName(), namedValue);
				}

				final CompField componentField = realComponents.get(valueId.getName());
				final Type type = componentField.getType();
				final IValue componentValue = namedValue.getValue();

				if (componentValue != null) {
					componentValue.setMyGovernor(type);
					if (Value_type.NOTUSED_VALUE.equals(componentValue.getValuetype())) {
						if (!incompleteAllowed) {
							componentValue.getLocation().reportSemanticError(INCOMPLETEPRESENTERROR);
						}
					} else {
						final IValue tempValue = type.checkThisValueRef(timestamp, componentValue);
						selfReference |= type.checkThisValue(timestamp, tempValue, lhs,
								new ValueCheckingOptions(expectedValue, incompleteAllowed, componentField.isOptional(), true, impliciOmit, strElem));
					}
				}
			}
		}

		if (!incompleteAllowed || strictConstantCheckingSeverity) {
			final int nofTypeComponents = realComponents.size();
			CompField field;
			for (int i = 0; i < nofTypeComponents; i++) {
				field = compFieldMap.fields.get(i);
				final Identifier id = field.getIdentifier();
				if (!componentMap.containsKey(id.getName())) {
					if (field.isOptional() && impliciOmit) {
						value.addNamedValue(new NamedValue(new Identifier(Identifier_type.ID_TTCN, id.getDisplayName()), new Omit_Value(), false));
					} else {
						value.getLocation().reportSemanticError(
								MessageFormat.format(isAsn ? MISSINGFIELDASN1 : MISSINGFIELDTTCN3, id.getDisplayName()));
					}
				}
			}
		}

		value.setLastTimeChecked(timestamp);

		return selfReference;
	}

	@Override
	/** {@inheritDoc} */
	public boolean checkThisTemplate(final CompilationTimeStamp timestamp, final ITTCN3Template template,
			final boolean isModified, final boolean implicitOmit, final Assignment lhs) {
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
			template.getLocation().reportSemanticError(MessageFormat.format(TEMPLATENOTALLOWED, template.getTemplateTypeName()));
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
	 * @param timestamp the timestamp of the actual semantic check cycle.
	 * @param templateList the template list to check
	 * @param isModified is the template modified or not ?
	 * @param implicitOmit indicates whether the template has the implicit omit attribute set or not.
	 * */
	private boolean checkThisNamedTemplateList(final CompilationTimeStamp timestamp, final Named_Template_List templateList, final boolean isModified,
			final boolean implicitOmit, final Assignment lhs) {
		templateList.removeGeneratedValues();

		boolean selfReference = false;
		final Map<String, NamedTemplate> componentMap = new HashMap<String, NamedTemplate>();
		final int nofTypeComponents = getNofComponents();
		final int nofTemplateComponents = templateList.getNofTemplates();

		final Map<String, CompField> realComponents = compFieldMap.getComponentFieldMap(timestamp);
		for (int i = 0; i < nofTemplateComponents; i++) {
			final NamedTemplate namedTemplate = templateList.getTemplateByIndex(i);
			final Identifier identifier = namedTemplate.getName();
			final String templateName = identifier.getName();

			if (realComponents.containsKey(templateName)) {
				if (componentMap.containsKey(templateName)) {
					namedTemplate.getLocation().reportSemanticError(MessageFormat.format(DUPLICATETEMPLATEFIELDFIRST, identifier.getDisplayName()));
					componentMap.get(templateName).getLocation().reportSemanticError(
							MessageFormat.format(DUPLICATETEMPLATEFIELDAGAIN, identifier.getDisplayName()));
				} else {
					componentMap.put(templateName, namedTemplate);
				}

				final CompField componentField = getComponentByName(identifier.getName());

				final Type type = componentField.getType();
				ITTCN3Template componentTemplate = namedTemplate.getTemplate();
				componentTemplate.setMyGovernor(type);
				componentTemplate = type.checkThisTemplateRef(timestamp, componentTemplate);
				final boolean isOptional = componentField.isOptional();
				selfReference |= componentTemplate.checkThisTemplateGeneric(timestamp, type, isModified, isOptional, isOptional, true, implicitOmit, lhs);
			} else {
				namedTemplate.getLocation().reportSemanticError(
						MessageFormat.format(NONEXISTENTTEMPLATEFIELDREFERENCE, identifier.getDisplayName(), getTypename()));
			}
		}

		if (!isModified && strictConstantCheckingSeverity) {
			// check missing fields
			for (int i = 0; i < nofTypeComponents; i++) {
				final Identifier identifier = getComponentIdentifierByIndex(i);
				if (!componentMap.containsKey(identifier.getName())) {
					if (getComponentByIndex(i).isOptional() && implicitOmit) {
						templateList.addNamedValue(new NamedTemplate(new Identifier(Identifier_type.ID_TTCN, identifier.getDisplayName()),
								new OmitValue_Template(), false));
					} else {
						templateList.getLocation().reportSemanticError(
								MessageFormat.format(MISSINGTEMPLATEFIELD, identifier.getDisplayName(), getTypename()));
					}
				}
			}
		}

		return selfReference;
	}

	@Override
	/** {@inheritDoc} */
	public void checkCodingAttributes(final CompilationTimeStamp timestamp, final IReferenceChain refChain) {
		checkSetSeqRawCodingAttributes(timestamp);
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
	public void forceRaw(final CompilationTimeStamp timestamp) {
		if (rawAttribute == null) {
			rawAttribute = new RawAST(getDefaultRawFieldLength());
		}
	}

	@Override
	/** {@inheritDoc} */
	public int getRawLength(final BuildTimestamp timestamp) {
		if (rawLengthCalculated != null && !rawLengthCalculated.isLess(timestamp)) {
			return rawLength;
		}

		rawLengthCalculated = timestamp;
		rawLength = 0;
		for (int i = 0; i < getNofComponents(); i++) {
			final CompField cf = getComponentByIndex(i);
			if (cf.isOptional()) {
				rawLength = -1;
				return rawLength;
			}

			final Type t = cf.getType();
			final int l = t.getTypeRefdLast(CompilationTimeStamp.getBaseTimestamp()).getRawLength(timestamp);
			if (l == -1) {
				rawLength = -1;
				return rawLength;
			}

			if (t.rawAttribute != null && (t.rawAttribute.pointerto != null || t.rawAttribute.lengthto != null)) {
				rawLength = -1;
				return rawLength;
			}
			rawLength += l;
		}

		return rawLength;
	}

	@Override
	/** {@inheritDoc} */
	public StringBuilder getProposalDescription(final StringBuilder builder) {
		return builder.append("set");
	}

	@Override
	/** {@inheritDoc} */
	public String getGenNameValue(final JavaGenData aData, final StringBuilder source, final Scope scope) {
		return getGenNameOwn(scope);
	}

	@Override
	/** {@inheritDoc} */
	public String getGenNameTemplate(final JavaGenData aData, final StringBuilder source, final Scope scope) {
		return getGenNameOwn(scope).concat("_template");
	}

	@Override
	/** {@inheritDoc} */
	public void generateCode( final JavaGenData aData, final StringBuilder source ) {
		final String className = getGenNameOwn();
		final String classReadableName = getFullName();

		generateCodeTypedescriptor(aData, source);

		final List<FieldInfo> namesList =  new ArrayList<FieldInfo>();
		boolean hasOptional = false;
		for ( final CompField compField : compFieldMap.fields ) {
			final IType cfType = compField.getType();
			final FieldInfo fi = new FieldInfo(cfType.getGenNameValue( aData, source, getMyScope() ),
					cfType.getGenNameTemplate( aData, source, getMyScope() ),
					compField.getIdentifier().getName(), compField.getIdentifier().getDisplayName(), compField.isOptional(),
					false, cfType.getClass().getSimpleName(), cfType.getGenNameTypeDescriptor(aData, source, myScope));
			hasOptional |= compField.isOptional();
			namesList.add( fi );
		}

		for ( final CompField compField : compFieldMap.fields ) {
			final StringBuilder tempSource = aData.getCodeForType(compField.getType().getGenNameOwn());
			compField.getType().generateCode(aData, tempSource);
		}

		final boolean hasRaw = getGenerateCoderFunctions(MessageEncoding_type.RAW);
		final RawASTStruct raw = convertRAWCodingAttributes(aData, source, hasRaw, namesList);

		RecordSetCodeGenerator.generateValueClass(aData, source, className, classReadableName, namesList, hasOptional, true, hasRaw, raw);
		RecordSetCodeGenerator.generateTemplateClass(aData, source, className, classReadableName, namesList, hasOptional, true);

		if (hasDoneAttribute()) {
			generateCodeDone(aData, source);
		}
		if (subType != null) {
			subType.generateCode(aData, source);
		}

		generateCodeForCodingHandlers(aData, source);
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
			expression.expression.append("FATAL_ERROR encountered");
			return true;
		}

		final Identifier fieldId = ((FieldSubReference) subReference).getId();
		final CompField compField = getComponentByName(fieldId.getName());
		if (compField.isOptional()) {
			return false;
		}

		return compField.getType().isPresentAnyvalueEmbeddedField(expression, subreferences, beginIndex + 1);
	}
}
