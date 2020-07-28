/******************************************************************************
 * Copyright (c) 2000-2020 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST;

import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.Activator;
import org.eclipse.titan.designer.GeneralConstants;
import org.eclipse.titan.designer.AST.ISubReference.Subreference_type;
import org.eclipse.titan.designer.AST.IValue.Value_type;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.ASN1.Value_Assignment;
import org.eclipse.titan.designer.AST.ASN1.types.ASN1_Choice_Type;
import org.eclipse.titan.designer.AST.ASN1.types.ASN1_Sequence_Type;
import org.eclipse.titan.designer.AST.ASN1.types.ASN1_Set_Seq_Choice_BaseType;
import org.eclipse.titan.designer.AST.ASN1.types.ASN1_Set_Type;
import org.eclipse.titan.designer.AST.ASN1.types.Open_Type;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.IIncrementallyUpdateable;
import org.eclipse.titan.designer.AST.TTCN3.attributes.JsonAST;
import org.eclipse.titan.designer.AST.TTCN3.attributes.JsonAST.JsonEnumText;
import org.eclipse.titan.designer.AST.TTCN3.attributes.MultipleWithAttributes;
import org.eclipse.titan.designer.AST.TTCN3.attributes.Qualifier;
import org.eclipse.titan.designer.AST.TTCN3.attributes.Qualifiers;
import org.eclipse.titan.designer.AST.TTCN3.attributes.RawAST;
import org.eclipse.titan.designer.AST.TTCN3.attributes.RawAST.rawAST_field_list;
import org.eclipse.titan.designer.AST.TTCN3.attributes.SingleWithAttribute;
import org.eclipse.titan.designer.AST.TTCN3.attributes.SingleWithAttribute.Attribute_Modifier_type;
import org.eclipse.titan.designer.AST.TTCN3.attributes.SingleWithAttribute.Attribute_Type;
import org.eclipse.titan.designer.AST.TTCN3.attributes.WithAttributesPath;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Const;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Type;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Var_Template;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Definition;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Group;
import org.eclipse.titan.designer.AST.TTCN3.definitions.TTCN3Module;
import org.eclipse.titan.designer.AST.TTCN3.templates.ITTCN3Template;
import org.eclipse.titan.designer.AST.TTCN3.templates.ITTCN3Template.Template_type;
import org.eclipse.titan.designer.AST.TTCN3.templates.SpecificValue_Template;
import org.eclipse.titan.designer.AST.TTCN3.types.AbstractOfType;
import org.eclipse.titan.designer.AST.TTCN3.types.Anytype_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.Array_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.BitString_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.CharString_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.CharString_Type.CharCoding;
import org.eclipse.titan.designer.AST.TTCN3.types.CompField;
import org.eclipse.titan.designer.AST.TTCN3.types.Function_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.OctetString_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.Referenced_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.SequenceOf_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.SetOf_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.TTCN3_Set_Seq_Choice_BaseType;
import org.eclipse.titan.designer.AST.TTCN3.types.subtypes.ParsedSubType;
import org.eclipse.titan.designer.AST.TTCN3.types.subtypes.SubType;
import org.eclipse.titan.designer.AST.TTCN3.values.Expression_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Expression_Value.Operation_type;
import org.eclipse.titan.designer.AST.TTCN3.values.Integer_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Referenced_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.Bit2OctExpression;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.ExpressionStruct;
import org.eclipse.titan.designer.compiler.BuildTimestamp;
import org.eclipse.titan.designer.compiler.JavaGenData;
import org.eclipse.titan.designer.editors.ProposalCollector;
import org.eclipse.titan.designer.editors.actions.DeclarationCollector;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;
import org.eclipse.titan.designer.parsers.variantattributeparser.VariantAttributeAnalyzer;
import org.eclipse.titan.designer.preferences.PreferenceConstants;
import org.eclipse.titan.designer.productUtilities.ProductConstants;

/**
 * The Type class is the base class for types.
 *
 * @author Kristof Szabados
 * @author Arpad Lovassy
 * */
public abstract class Type extends Governor implements IType, IIncrementallyUpdateable, IOutlineElement {
	private static final String INCOMPATIBLEVALUE = "Incompatible value: `{0}'' was expected";
	public static final String REFTOVALUEEXPECTED = "Reference to a value was expected instead of {0}";
	public static final String REFTOVALUEEXPECTED_INSTEADOFCALL = "Reference to a value was expected instead of a call of {0}, which return a template";
	private static final String TYPECOMPATWARNING = "Type compatibility between `{0}'' and `{1}''";

	public final static int JSON_NONE       = 0x00; // no value type set (default)
	public final static int JSON_NUMBER     = 0x01; // integer and float
	public final static int JSON_STRING     = 0x02; // all string types, the objid type, the verdict type and enumerated values
	public final static int JSON_BOOLEAN    = 0x04; // boolean (true or false)
	public final static int JSON_OBJECT     = 0x08; // records, sets, unions and the anytype
	public final static int JSON_ARRAY      = 0x10; // record of, set of and array
	public final static int JSON_NULL       = 0x20; // ASN.1 null type
	public final static int JSON_ANY_VALUE  = 0x3F; // unions with the "as value" coding instruction

	/** the parent type of this type */
	private IType parentType;

	/** The constraints assigned to this type. */
	protected Constraints constraints = null;

	/** The with attributes assigned to the definition of this type */
	// TODO as this is only used for TTCN-3 types maybe we could save some
	// memory, by moving it ... but than we waste runtime.
	protected WithAttributesPath withAttributesPath = null;
	public ArrayList<MessageEncoding_type> codersToGenerate = new ArrayList<IType.MessageEncoding_type>();
	public RawAST rawAttribute = null;
	public JsonAST jsonAttribute = null;

	private boolean hasDone = false;
	/** Indicates that the component array version (used with the help of the
	 * 'any from' clause) of the 'done' function needs to be generated for
	 *  this type. */
	boolean needs_any_from_done = false;

	/** The list of parsed sub-type restrictions before they are converted */
	protected List<ParsedSubType> parsedRestrictions = null;

	/** The sub-type restriction created from the parsed restrictions */
	protected SubType subType = null;

	protected ArrayList<Coding_Type> codingTable = new ArrayList<IType.Coding_Type>();

	/** What kind of AST element owns the type.
	 *  It may not be known at creation type, so it's initially OT_UNKNOWN.
	 *  We want this information so we don't have to bother with XER
	 *  if the type is an ASN.1 construct, or it's the type in a "runs on" scope,
	 *  the type of a variable declaration/module par/const, etc. */
	protected TypeOwner_type ownerType = TypeOwner_type.OT_UNKNOWN;
	protected INamedNode owner = null;//TODO needs to check if this is the tightest interface once structure is ready to reveal all usage.

	/** the time when code for this type was generated. */
	protected BuildTimestamp lastTimeGenerated = null;

	//FIXME these should be only temporary fields
	/** the time when code for the type descriptor of this type was generated. */
	private BuildTimestamp lastTimeTypeDescriptorGenerated = null;

	/**
	 * The actual value of the severity level to report type compatibility
	 * on.
	 */
	private static String typeCompatibilitySeverity;

	/**
	 * if typeCompatibilitySeverity is set to Error this is true, in this
	 * case structured types must be nominally compatible
	 */
	protected static boolean noStructuredTypeCompatibility;

	static {
		final IPreferencesService ps = Platform.getPreferencesService();
		if ( ps != null ) {
			typeCompatibilitySeverity = ps.getString(ProductConstants.PRODUCT_ID_DESIGNER,
					PreferenceConstants.REPORTTYPECOMPATIBILITY, GeneralConstants.WARNING, null);
			noStructuredTypeCompatibility = GeneralConstants.ERROR.equals(typeCompatibilitySeverity);

			final Activator activator = Activator.getDefault();
			if (activator != null) {
				activator.getPreferenceStore().addPropertyChangeListener(new IPropertyChangeListener() {

					@Override
					public void propertyChange(final PropertyChangeEvent event) {
						final String property = event.getProperty();
						if (PreferenceConstants.REPORTTYPECOMPATIBILITY.equals(property)) {
							typeCompatibilitySeverity = ps.getString(ProductConstants.PRODUCT_ID_DESIGNER,
									PreferenceConstants.REPORTTYPECOMPATIBILITY, GeneralConstants.WARNING, null);
							noStructuredTypeCompatibility = GeneralConstants.ERROR.equals(typeCompatibilitySeverity);
						}
					}
				});
			}
		}
	}

	public Type() {
		parentType = null;
	}

	@Override
	/** {@inheritDoc} */
	public Setting_type getSettingtype() {
		return Setting_type.S_T;
	}

	@Override
	/** {@inheritDoc} */
	public abstract Type_type getTypetype();

	@Override
	/** {@inheritDoc} */
	public final IType getParentType() {
		return parentType;
	}

	@Override
	/** {@inheritDoc} */
	public final void setParentType(final IType type) {
		parentType = type;
	}

	@Override
	/** {@inheritDoc} */
	public final WithAttributesPath getAttributePath() {
		if (withAttributesPath == null) {
			withAttributesPath = new WithAttributesPath();
		}

		return withAttributesPath;
	}

	@Override
	/** {@inheritDoc} */
	public void setAttributeParentPath(final WithAttributesPath parent) {
		if (withAttributesPath == null) {
			withAttributesPath = new WithAttributesPath();
		}

		withAttributesPath.setAttributeParent(parent);
	}

	@Override
	/** {@inheritDoc} */
	public final void clearWithAttributes() {
		if (withAttributesPath != null) {
			withAttributesPath.setWithAttributes(null);
		}
	}

	@Override
	/** {@inheritDoc} */
	public final void setWithAttributes(final MultipleWithAttributes attributes) {
		if (withAttributesPath == null) {
			withAttributesPath = new WithAttributesPath();
		}

		withAttributesPath.setWithAttributes(attributes);
	}

	/**
	 * Returns a new type representing the encoded stream of the given
	 * encoding type.
	 *
	 * @param encodingType
	 *                the encoding type.
	 * @param streamVariant
	 *                the variant of the stream.
	 * @return the temporary type.
	 * */
	public static Type getStreamType(final MessageEncoding_type encodingType, final int streamVariant) {
		if (streamVariant == 0) {
			return new BitString_Type();
		}
		switch (encodingType) {
		case BER:
		case PER:
		case RAW:
		case XER:
		case JSON:
		case OER:
			return new OctetString_Type();
		case TEXT:
			if (streamVariant == 1) {
				return new CharString_Type();
			} else {
				return new OctetString_Type();
			}
		default:
			return null;
		}
	}

	@Override
	/** {@inheritDoc} */
	public final boolean hasDoneAttribute() {
		return hasDone;
	}

	@Override
	/** {@inheritDoc} */
	public boolean isTagged() {
		//FIXME implement support for tags.
		return false;
	}

	@Override
	/** {@inheritDoc} */
	public final boolean isConstrained() {
		return constraints != null;
	}

	@Override
	/** {@inheritDoc} */
	public final void addConstraints(final Constraints constraints) {
		if (constraints == null) {
			return;
		}

		this.constraints = constraints;
		constraints.setMyType(this);
		constraints.setFullNameParent(this);
	}

	@Override
	/** {@inheritDoc} */
	public final Constraints getConstraints() {
		return constraints;
	}

	@Override
	/** {@inheritDoc} */
	public final SubType getSubtype() {
		return subType;
	}

	@Override
	/** {@inheritDoc} */
	public final void setParsedRestrictions(final List<ParsedSubType> parsedRestrictions) {
		this.parsedRestrictions = parsedRestrictions;
	}

	@Override
	/** {@inheritDoc} */
	public String chainedDescription() {
		return "type reference: " + getFullName();
	}

	@Override
	/** {@inheritDoc} */
	public final Location getChainLocation() {
		return getLocation();
	}

	@Override
	/** {@inheritDoc} */
	public IType getTypeRefdLast(final CompilationTimeStamp timestamp) {
		final IReferenceChain referenceChain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
		final IType result = getTypeRefdLast(timestamp, referenceChain);
		referenceChain.release();

		return result;
	}

	/**
	 * Returns the type referred last in case of a referred type, or itself
	 * in any other case.
	 *
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 * @param referenceChain
	 *                the ReferenceChain used to detect circular references
	 *
	 * @return the actual or the last referred type
	 * */
	public IType getTypeRefdLast(final CompilationTimeStamp timestamp, final IReferenceChain referenceChain) {
		return this;
	}

	@Override
	/** {@inheritDoc} */
	public IType getFieldType(final CompilationTimeStamp timestamp, final Reference reference, final int actualSubReference,
			final Expected_Value_type expectedIndex, final boolean interruptIfOptional) {
		final IReferenceChain chain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
		final IType temp = getFieldType(timestamp, reference, actualSubReference, expectedIndex, chain, interruptIfOptional);
		chain.release();

		return temp;
	}

	@Override
	/** {@inheritDoc} */
	public abstract IType getFieldType(final CompilationTimeStamp timestamp, final Reference reference, final int actualSubReference,
			final Expected_Value_type expectedIndex, final IReferenceChain refChain, final boolean interruptIfOptional);

	@Override
	/** {@inheritDoc} */
	public boolean getSubrefsAsArray(final CompilationTimeStamp timestamp, final Reference reference, final int actualSubReference,
			final List<Integer> subrefsArray, final List<IType> typeArray) {
		final List<ISubReference> subreferences = reference.getSubreferences();
		if (subreferences.size() <= actualSubReference) {
			return true;
		}
		ErrorReporter.INTERNAL_ERROR("Type " + getTypename() + " has no fields.");
		return false;
	}

	@Override
	/** {@inheritDoc} */
	public boolean getFieldTypesAsArray(final Reference reference, final int actualSubReference, final List<IType> typeArray) {
		final List<ISubReference> subreferences = reference.getSubreferences();
		if (subreferences.size() <= actualSubReference) {
			return true;
		}
		return false;
	}

	//FIXME comment
	public boolean isOptionalField() {
		if (getOwnertype() == TypeOwner_type.OT_COMP_FIELD) {
			final CompField myOwner = (CompField) getOwner();
			return myOwner != null && myOwner.isOptional();
		}

		return false;
	}

	@Override
	/** {@inheritDoc} */
	public boolean fieldIsOptional(final List<ISubReference> subReferences) {
		if (subReferences == null || subReferences.isEmpty()) {
			return false;
		}

		final ISubReference lastSubReference = subReferences.get(subReferences.size() - 1);
		if (!(lastSubReference instanceof FieldSubReference)) {
			return false;
		}

		IType type = this;
		CompField compField = null;
		for ( int i = 1; i < subReferences.size(); i++) {
			type = type.getTypeRefdLast(CompilationTimeStamp.getBaseTimestamp());

			final ISubReference subreference = subReferences.get(i);
			if(Subreference_type.fieldSubReference.equals(subreference.getReferenceType())) {
				final Identifier id = ((FieldSubReference) subreference).getId();
				switch(type.getTypetype()) {
				case TYPE_TTCN3_CHOICE:
				case TYPE_TTCN3_SEQUENCE:
				case TYPE_TTCN3_SET:
					compField = ((TTCN3_Set_Seq_Choice_BaseType)type).getComponentByName(id.getName());
					break;
				case TYPE_ANYTYPE:
					compField = ((Anytype_Type)type).getComponentByName(id.getName());
					break;
				case TYPE_OPENTYPE:
					compField = ((Open_Type)type).getComponentByName(id);
					break;
				case TYPE_ASN1_SEQUENCE:
					compField = ((ASN1_Sequence_Type)type).getComponentByName(id);
					break;
				case TYPE_ASN1_SET:
					compField = ((ASN1_Set_Type)type).getComponentByName(id);
					break;
				case TYPE_ASN1_CHOICE:
					compField = ((ASN1_Choice_Type)type).getComponentByName(id);
					break;
				default:
					return false;
				}

				if (compField == null) {
					return false;
				}

				type = compField.getType();
			} else if(Subreference_type.arraySubReference.equals(subreference.getReferenceType())) {
				switch(type.getTypetype()) {
				case TYPE_SEQUENCE_OF:
				case TYPE_SET_OF:
					type = ((AbstractOfType) type).getOfType();
					break;
				case TYPE_ARRAY:
					type = ((Array_Type) type).getElementType();
					break;
				default:
					type = null;
					break;
				}
			}

			if (type == null) {
				return false;
			}
		}

		return compField != null && compField.isOptional();
	}

	@Override
	/** {@inheritDoc} */
	public RawAST getRawAttribute() {
		return rawAttribute;
	}

	@Override
	/** {@inheritDoc} */
	public int getDefaultRawFieldLength() {
		return 0;
	}

	@Override
	/** {@inheritDoc} */
	public int getRawLength(final BuildTimestamp timestamp) {
		return -1;
	}

	@Override
	/** {@inheritDoc} */
	public JsonAST getJsonAttribute() {
		return jsonAttribute;
	}

	@Override
	/** {@inheritDoc} */
	public int getLengthMultiplier() {
		return 1;
	}

	@Override
	/** {@inheritDoc} */
	public final boolean hasVariantAttributes(final CompilationTimeStamp timestamp) {
		if (withAttributesPath == null) {
			return false;
		}

		final List<SingleWithAttribute> realAttributes = withAttributesPath.getRealAttributes(timestamp);
		for (int i = 0; i < realAttributes.size(); i++) {
			if (SingleWithAttribute.Attribute_Type.Variant_Attribute.equals(realAttributes.get(i).getAttributeType())) {
				return true;
			}
		}

		final MultipleWithAttributes localAttributes = withAttributesPath.getAttributes();
		if (localAttributes == null) {
			return false;
		}

		for (int i = 0; i < localAttributes.getNofElements(); i++) {
			final SingleWithAttribute tempSingle = localAttributes.getAttribute(i);
			if (Attribute_Type.Variant_Attribute.equals(tempSingle.getAttributeType())) {
				return true;
			}
		}

		return false;
	}

	@Override
	/** {@inheritDoc} */
	public void initAttributes(final CompilationTimeStamp timestamp) {
//		codingTable.clear();
		hasDone = false;

		checkDoneAttribute(timestamp);
	}

	@Override
	/** {@inheritDoc} */
	public final void checkDoneAttribute(final CompilationTimeStamp timestamp) {
		hasDone = false;

		if (withAttributesPath == null) {
			return;
		}

		final List<SingleWithAttribute> realAttributes = withAttributesPath.getRealAttributes(timestamp);
		for (final SingleWithAttribute singleAttribute : realAttributes) {
			if (Attribute_Type.Extension_Attribute.equals(singleAttribute.getAttributeType())
					&& "done".equals(singleAttribute.getAttributeSpecification().getSpecification())) {
				hasDone = true;
			}
		}
	}

	/**
	 * Does the semantic checking of the type.
	 *
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 * */
	// FIXME could be made abstract
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
	}

	/**
	 * Checks the encodings supported by the type (when using new codec handling).
	 * TTCN-3 types need to have an 'encode' attribute to support an encoding.
	 * ASN.1 types automatically support BER, PER and JSON encodings, and XER
	 * encoding, if set by the compiler option.
	 *
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 * */
	public final void checkEncode(final CompilationTimeStamp timestamp) {
		rawAttribute = null;
		jsonAttribute = null; //FIXME: check this value

		switch (getTypeRefdLast(timestamp).getTypetypeTtcn3()) {
		case TYPE_NULL:
		case TYPE_BOOL:
		case TYPE_INTEGER:
		case TYPE_REAL:
		case TYPE_TTCN3_ENUMERATED:
		case TYPE_BITSTRING:
		case TYPE_HEXSTRING:
		case TYPE_OCTETSTRING:
		case TYPE_CHARSTRING:
		case TYPE_UCHARSTRING:
		case TYPE_OBJECTID:
		case TYPE_TTCN3_CHOICE:
		case TYPE_SEQUENCE_OF:
		case TYPE_SET_OF:
		case TYPE_TTCN3_SEQUENCE:
		case TYPE_TTCN3_SET:
		case TYPE_VERDICT:
		case TYPE_ARRAY:
		case TYPE_ANYTYPE:
			if (!isAsn()) {
				final WithAttributesPath attributePath = getAttributePath();
				if (attributePath != null) {
					final MultipleWithAttributes multipleWithAttributes = attributePath.getAttributes();
					if (multipleWithAttributes != null) {
						for (int i = 0; i < multipleWithAttributes.getNofElements(); i++) {
							final SingleWithAttribute singleWithAttribute = multipleWithAttributes.getAttribute(i);
							if (singleWithAttribute.getAttributeType() == Attribute_Type.Encode_Attribute) {
								final Attribute_Modifier_type mod = singleWithAttribute.getModifier();
								final Qualifiers qualifiers = singleWithAttribute.getQualifiers();
								if (qualifiers != null && qualifiers.getNofQualifiers() > 0) {
									for (int j = 0; j < qualifiers.getNofQualifiers(); j++) {
										final Qualifier qualifier = qualifiers.getQualifierByIndex(j);
										final List<ISubReference> fieldsOrArrays = new ArrayList<ISubReference>();
										for (int k = 0; k < qualifier.getNofSubReferences(); k++) {
											fieldsOrArrays.add(qualifier.getSubReferenceByIndex(k));
										}
										final Reference reference = new Reference(null, fieldsOrArrays);
										final IType type = getFieldType(timestamp, reference, 0, Expected_Value_type.EXPECTED_CONSTANT, false);
										if (type != null) {
											if (type.getMyScope() != myScope) {
												qualifier.getLocation().reportSemanticWarning("Encode attribute is ignored, because it refers to a type from a different type definition");
											} else {
												type.addCoding(timestamp, singleWithAttribute.getAttributeSpecification().getSpecification(), mod, false);
											}
										}
									}
								} else {
									addCoding(timestamp, singleWithAttribute.getAttributeSpecification().getSpecification(), mod, false);
								}
							}
						}
					}

					if(ownerType != TypeOwner_type.OT_TYPE_DEF) {
						return;
					}

					WithAttributesPath globalAttributesPath;
					final Def_Type def = (Def_Type)owner;
					final Group nearest_group = def.getParentGroup();
					if (nearest_group == null) {
						// no group, use the module
						final Module myModule = myScope.getModuleScope();
						globalAttributesPath = ((TTCN3Module)myModule).getAttributePath();
					} else {
						globalAttributesPath = nearest_group.getAttributePath();
					}

					if (globalAttributesPath != null) {
						boolean hasGlobalOverride = false;
						boolean modifierConflict = false;
						Attribute_Modifier_type firstModifier = Attribute_Modifier_type.MOD_NONE;
						final List<SingleWithAttribute> realAttributes = globalAttributesPath.getRealAttributes(timestamp);
						for (int i = 0; i < realAttributes.size(); i++) {
							final SingleWithAttribute singleWithAttribute = realAttributes.get(i);
							if (singleWithAttribute.getAttributeType() == Attribute_Type.Encode_Attribute) {
								final Attribute_Modifier_type modifier = singleWithAttribute.getModifier();
								if (i == 0) {
									firstModifier = modifier;
								} else if (!modifierConflict && modifier != firstModifier) {
									modifierConflict = true;
									singleWithAttribute.getLocation().reportSemanticError("All 'encode' attributes of a group or module must have the same modifier ('override', '@local' or none)");
								}
								if (modifier == Attribute_Modifier_type.MOD_OVERRIDE) {
									hasGlobalOverride = true;
								}
								if (hasGlobalOverride && modifierConflict) {
									break;
								}
							}
						}
						// make a list of the type and its field and element types that inherit
						// the global 'encode' attributes
						// overriding global attributes are inherited by types with no coding
						// table (no 'encode' attributes) of their own
						// non-overriding global attributes are inherited by types that have
						// no coding table of their own and cannot use the coding table of any
						// other type
						final ArrayList<IType> typeList = new ArrayList<IType>();
						getTypesWithNoCodingTable(timestamp, typeList, hasGlobalOverride);
						if (!typeList.isEmpty()) {
							for (int i = 0; i < realAttributes.size(); i++) {
								final SingleWithAttribute singleWithAttribute = realAttributes.get(i);
								if (singleWithAttribute.getAttributeType() == Attribute_Type.Encode_Attribute) {
									for (int j = typeList.size() - 1; j >= 0; j--) {
										typeList.get(j).addCoding(timestamp, singleWithAttribute.getAttributeSpecification().getSpecification(), Attribute_Modifier_type.MOD_NONE, true);
									}
								}
							}
							typeList.clear();
						}
					}
				}
			} else {
				// ASN.1 types automatically have BER, PER, XER, OER and JSON encoding
				switch(ownerType) {
				case OT_TYPE_ASS:
				case OT_RECORD_OF:
				case OT_COMP_FIELD:
				case OT_SELTYPE:
				case OT_FIELDSETTING:
					//FIXME implement once PER, OER or XER gets supported
					addCoding(timestamp, "JSON", Attribute_Modifier_type.MOD_NONE, true);
					break;
				default:
					break;
				}
			}
			break;
		default:
			// the rest of the types can't have 'encode' attributes
			break;
		}
	}

	/**
	 * Checks the type's variant attributes (when using the new codec handling).
	 * */
	public void checkVariants(final CompilationTimeStamp timestamp) {
		if (isAsn() || ownerType != TypeOwner_type.OT_TYPE_DEF) {
			return;
		}

		WithAttributesPath globalAttributesPath;
		final Def_Type def = (Def_Type)owner;
		final Group nearest_group = def.getParentGroup();
		if (nearest_group == null) {
			// no group, use the module
			final Module myModule = myScope.getModuleScope();
			globalAttributesPath = ((TTCN3Module)myModule).getAttributePath();
		} else {
			globalAttributesPath = nearest_group.getAttributePath();
		}

		if (globalAttributesPath != null) {
			// process all global variants, not just the closest group
			final List<SingleWithAttribute> realAttributes = globalAttributesPath.getRealAttributes(timestamp);
			for (int i = 0; i < realAttributes.size(); i++) {
				final SingleWithAttribute singleWithAttribute = realAttributes.get(i);
				if (singleWithAttribute.getAttributeType() == Attribute_Type.Variant_Attribute) {
					checkThisVariant(timestamp, singleWithAttribute, true);
				}
			}
		}

		// check local variant attributes second, so they overwrite global ones if they
		// conflict with each other
		final WithAttributesPath attributePath = getAttributePath();
		if (attributePath != null) {
			final MultipleWithAttributes multipleWithAttributes = attributePath.getAttributes();
			if (multipleWithAttributes != null) {
				for (int i = 0; i < multipleWithAttributes.getNofElements(); i++) {
					final SingleWithAttribute singleWithAttribute = multipleWithAttributes.getAttribute(i);
					if (singleWithAttribute.getAttributeType() == Attribute_Type.Variant_Attribute) {
						final Qualifiers qualifiers = singleWithAttribute.getQualifiers();
						if (qualifiers != null && qualifiers.getNofQualifiers() > 0) {
							for (int j = 0; j < qualifiers.getNofQualifiers(); j++) {
								final Qualifier qualifier = qualifiers.getQualifierByIndex(j);
								final List<ISubReference> fieldsOrArrays = new ArrayList<ISubReference>();
								for (int k = 0; k < qualifier.getNofSubReferences(); k++) {
									fieldsOrArrays.add(qualifier.getSubReferenceByIndex(k));
								}
								final Reference reference = new Reference(null, fieldsOrArrays);
								final IType type = getFieldType(timestamp, reference, 0, Expected_Value_type.EXPECTED_CONSTANT, false);//?
								if (type != null) {
									type.checkThisVariant(timestamp, singleWithAttribute, false);
								}
							}
						} else {
							checkThisVariant(timestamp, singleWithAttribute, false);
						}
					}
				}
			}
		}

		// check the coding attributes set by the variants
		final IReferenceChain chain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
		checkCodingAttributes(timestamp, chain);
		chain.release();
	}

	@Override
	/** {@inheritDoc} */
	public void checkThisVariant(final CompilationTimeStamp timestamp, final SingleWithAttribute singleWithAttribute, final boolean global) {
		final IType type = getTypeWithCodingTable(timestamp, false);
		if (type == null) {
			//FIXME as only RAW is supported for now, we can not report this error
//			if (!global) {
//				singleWithAttribute.getLocation().reportSemanticError(MessageFormat.format("No encoding rules defined for type `{0}''", getTypename()));
//			}
		} else {
			final List<String> codingStrings = singleWithAttribute.getAttributeSpecification().getEncodings();
			// gather the built-in codecs referred to by the variant's encoding strings
			final ArrayList<MessageEncoding_type> codings = new ArrayList<IType.MessageEncoding_type>();
			boolean erroneous = false;
			if (codingStrings == null) {
				if (type.getCodingTable().size() > 1) {
					if (!global) {
						singleWithAttribute.getLocation().reportSemanticError(MessageFormat.format("The encoding reference is mandatory for variant attributes of type  `{0}''", getTypename()));
					}
					erroneous = true;
				} else if (type.getCodingTable().get(0).builtIn) {
					codings.add(type.getCodingTable().get(0).builtInCoding);
				} else { // PER or custom encoding
					final MessageEncoding_type coding = "PER".equals(type.getCodingTable().get(0).customCoding.name) ? MessageEncoding_type.PER: MessageEncoding_type.CUSTOM;
					singleWithAttribute.getLocation().reportSemanticWarning(MessageFormat.format("Variant attributes related to `{0}'' encoding are ignored", coding.getEncodingName()));
				}
			} else {
				for (int i = 0; i < codingStrings.size(); i++) {
					final String encodingString = codingStrings.get(i);
					final MessageEncoding_type coding = getEncodingType(encodingString);
					if (!hasEncoding(timestamp, coding, encodingString)) {
						erroneous = true;
						//FIXME RAW, JSON restriction only exists because that is the only supported encoding right now
						if (!global && (coding == MessageEncoding_type.RAW || coding == MessageEncoding_type.JSON)) {
							if (coding == MessageEncoding_type.CUSTOM) {
								singleWithAttribute.getLocation().reportSemanticError(MessageFormat.format("Type `{0}'' does not support {1} encoding", getTypename(), coding.getEncodingName()));
							} else {
								singleWithAttribute.getLocation().reportSemanticError(MessageFormat.format("Type `{0}'' does not support custom encoding `{1}''", getTypename(), encodingString));
							}
						}
					} else if (coding != MessageEncoding_type.PER && coding != MessageEncoding_type.CUSTOM) {
						codings.add(coding);
					} else { // PER or custom encoding
						singleWithAttribute.getLocation().reportSemanticWarning(MessageFormat.format("Variant attributes related to {0} encoding are ignored", coding.getEncodingName()));
					}
				}
			}
			//FIXME implement checks
			//TODO raw, json data is extracted
			final VariantAttributeAnalyzer analyzer = new VariantAttributeAnalyzer();
			boolean newRaw = false;
			boolean newJson = false;
			final AtomicBoolean rawFound = new AtomicBoolean(false);
			final AtomicBoolean jsonFound = new AtomicBoolean(false);
			if (rawAttribute == null) {
				IType t_refd = this;
				while (!t_refd.getIsErroneous(timestamp) && t_refd.getRawAttribute() == null && t_refd instanceof Referenced_Type) {
					final IReferenceChain referenceChain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
					t_refd = ((Referenced_Type)t_refd).getTypeRefd(timestamp, referenceChain);
					referenceChain.release();
				}
				rawAttribute = new RawAST(t_refd.getRawAttribute(), getDefaultRawFieldLength());
				newRaw = true;
			}

			if (jsonAttribute == null) {
				IType t_refd = this;
				while (!t_refd.getIsErroneous(timestamp) && t_refd.getJsonAttribute() == null && t_refd instanceof Referenced_Type) {
					final IReferenceChain referenceChain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
					t_refd = ((Referenced_Type)t_refd).getTypeRefd(timestamp, referenceChain);
					referenceChain.release();
				}
				jsonAttribute = new JsonAST(t_refd.getJsonAttribute());
				newJson = true;
			}

			analyzer.parse(rawAttribute, jsonAttribute, singleWithAttribute.getAttributeSpecification(), getLengthMultiplier(),
					rawFound, jsonFound);
			//FIME: check variant attribute is related to ... encoding
			if (!rawFound.get() && newRaw) {
				rawAttribute = null;
			}

			if (!jsonFound.get() && newJson) {
				jsonAttribute = null;
			}
		}
		if (global) {
			// send global variant attributes to field/element types
			switch (getTypetype()) {
			case TYPE_TTCN3_CHOICE:
			case TYPE_TTCN3_SEQUENCE:
			case TYPE_TTCN3_SET:
				for (int i = 0; i < ((TTCN3_Set_Seq_Choice_BaseType) this).getNofComponents(); i++) {
					((TTCN3_Set_Seq_Choice_BaseType) this).getComponentByIndex(i).getType().checkThisVariant(timestamp, singleWithAttribute, global);
				}
				break;
			case TYPE_ASN1_CHOICE:
			case TYPE_ASN1_SEQUENCE:
			case TYPE_ASN1_SET:
				for (int i = 0; i < ((ASN1_Set_Seq_Choice_BaseType) this).getNofComponents(); i++) {
					((ASN1_Set_Seq_Choice_BaseType) this).getComponentByIndex(i).getType().checkThisVariant(timestamp, singleWithAttribute, global);
				}
				break;
			case TYPE_ANYTYPE:
				for (int i = 0; i < ((Anytype_Type) this).getNofComponents(); i++) {
					((Anytype_Type) this).getComponentByIndex(i).getType().checkThisVariant(timestamp, singleWithAttribute, global);
				}
				break;
			case TYPE_OPENTYPE:
				for (int i = 0; i < ((Open_Type) this).getNofComponents(); i++) {
					((Open_Type) this).getComponentByIndex(i).getType().checkThisVariant(timestamp, singleWithAttribute, global);
				}
				break;
			case TYPE_ARRAY:
				((Array_Type)this).getElementType().checkThisVariant(timestamp, singleWithAttribute, global);
				break;
			case TYPE_SEQUENCE_OF:
				((SequenceOf_Type)this).getOfType().checkThisVariant(timestamp, singleWithAttribute, global);
				break;
			case TYPE_SET_OF:
				((SetOf_Type)this).getOfType().checkThisVariant(timestamp, singleWithAttribute, global);
				break;
			default:
				break;
			}
		}
	}

	@Override
	/** {@inheritDoc} */
	public void checkCodingAttributes(final CompilationTimeStamp timestamp, final IReferenceChain refChain) {
		//FIXME implement default behaviour
	}

	@Override
	/** {@inheritDoc} */
	public void checkCoding(final CompilationTimeStamp timestamp, final boolean encode, final Module usageModule, final boolean delayed, final Location errorLocation) {
		final IType type = getTypeWithCodingTable(timestamp, false);
		if (type == null) {
			errorLocation.reportSemanticError(MessageFormat.format("No coding rule specified for type `{0}''", getTypename()));
			return;
		}

		final List<Coding_Type> tempCodingTable = type.getCodingTable();
		for (int i = 0; i < tempCodingTable.size(); i++) {
			final Coding_Type tempCoding = tempCodingTable.get(i);
			if (!tempCoding.builtIn) {
				if (!delayed) {
					//FIXME delay support
					return;
				}

				final HashMap<IType, CoderFunction_Type> tempCoders = encode ? tempCoding.customCoding.encoders: tempCoding.customCoding.decoders;
				final CoderFunction_Type codingFunction = tempCoders.get(this);
				if (codingFunction == null) {
					if (!type.isAsn()) {
						errorLocation.reportSemanticWarning(MessageFormat.format("No `{0}'' {1}coder function defined for type `{2}''", tempCoding.customCoding.name, encode ? "en" : "de", getTypename()));
					}
				} else if (codingFunction.conflict){
					errorLocation.reportSemanticWarning(MessageFormat.format("Multiple `{0}'' {1}coder functions defined for type `{2}''", tempCoding.customCoding.name, encode ? "en" : "de", getTypename()));
				}
			}
		}
	}

	@Override
	/** {@inheritDoc} */
	public void forceRaw(final CompilationTimeStamp timestamp) {
		//empty by default
	}

	@Override
	/** {@inheritDoc} */
	public void setRawAttributes(final RawAST newAttributes) {
		rawAttribute = newAttributes;
	}

	@Override
	/** {@inheritDoc} */
	public void setJsonAttributes(final JsonAST newAttributes) {
		jsonAttribute = newAttributes;
	}

	@Override
	/** {@inheritDoc} */
	public void checkJson(final CompilationTimeStamp timestamp) {
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

		if (jsonAttribute.default_value != null) {
			checkJsonDefault(timestamp);
		}

		//TODO: check schema extensions 

		if (jsonAttribute.metainfo_unbound) {
			if (getParentType() == null || (getParentType().getTypetype() != Type_type.TYPE_TTCN3_SEQUENCE &&
					getParentType().getTypetype() != Type_type.TYPE_TTCN3_SET)) {
				// only allowed if it's an array type or a field of a record/set
				getLocation().reportSemanticError("Invalid attribute 'metainfo for unbound', requires record, set, record of, set of, array or field of a record or set");
			}

			final IType last = getTypeRefdLast(timestamp);
			final IType parent = getParentType();
			if (Type_type.TYPE_TTCN3_SEQUENCE == last.getTypetype() || Type_type.TYPE_TTCN3_SET == last.getTypetype()) {
				// if it's set for the record/set, pass it onto its fields
				final int nof_comps = ((TTCN3_Set_Seq_Choice_BaseType)last).getNofComponents();
				if (jsonAttribute.as_value && nof_comps == 1) {
					getLocation().reportSemanticWarning(MessageFormat.format("Attribute 'metainfo for unbound' will be ignored, because the {0} is encoded without field names.",
							Type_type.TYPE_TTCN3_SEQUENCE == last.getTypetype() ? "record" : "set"));
				}
				else {
					for (int i = 0; i < nof_comps; i++) {
						final Type comp_type = ((TTCN3_Set_Seq_Choice_BaseType)last).getComponentByIndex(i).getType();
						if (null == comp_type.jsonAttribute) {
							comp_type.jsonAttribute = new JsonAST();
						}
						comp_type.jsonAttribute.metainfo_unbound = true;
					}
				}
			}
			else if (Type_type.TYPE_SEQUENCE_OF != last.getTypetype() && Type_type.TYPE_SET_OF != last.getTypetype() &&
					Type_type.TYPE_ARRAY != last.getTypetype() && (null == parent ||
					(Type_type.TYPE_TTCN3_SEQUENCE != parent.getTypetype() && Type_type.TYPE_TTCN3_SET != parent.getTypetype()))) {
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

	@Override
	/** {@inheritDoc} */
	public void checkJsonDefault(final CompilationTimeStamp timestamp) {
		getLocation().reportSemanticError(MessageFormat.format("JSON default values are not available for type `{0}''", getTypename()));
	}

	@Override
	/** {@inheritDoc} */
	public void forceJson(final CompilationTimeStamp timestamp) {
		//empty by default
	}

	@Override
	/** {@inheritDoc} */
	public void addCoding(final CompilationTimeStamp timestamp, final String name, final Attribute_Modifier_type modifier, final boolean silent) {
		boolean encodeAttributeModifierConflict = false;
		final MessageEncoding_type builtInCoding = getEncodingType(name);

		for (int i = 0; i < codingTable.size(); i++) {
			final Coding_Type tempCodingType = codingTable.get(i);
			if (!encodeAttributeModifierConflict && modifier != tempCodingType.modifier) {
				encodeAttributeModifierConflict = true;
				getLocation().reportSemanticError("All 'encode' attributes of a type must have the same modifier ('override', '@local' or none)");
			}

			final String codingName = tempCodingType.builtIn ? tempCodingType.builtInCoding.getEncodingName() : tempCodingType.customCoding.name;
			final String currentName = tempCodingType.builtIn ? builtInCoding.getEncodingName() : name;
			if (currentName.equals(codingName)) {
				return; // coding already added
			}
		}

		switch (builtInCoding) {
		case CUSTOM:
		case PER: {
			final Coding_Type newCoding = new Coding_Type();
			newCoding.builtIn = false;
			newCoding.modifier = modifier;
			newCoding.customCoding = new Coding_Type.CustomCoding_type();
			newCoding.customCoding.name = name;
			newCoding.customCoding.encoders = new HashMap<IType, IType.CoderFunction_Type>();
			newCoding.customCoding.decoders = new HashMap<IType, IType.CoderFunction_Type>();
			codingTable.add(newCoding);
			break;
		}
		default:{
			final IType refdLast = getTypeRefdLast(timestamp);
			final boolean canHaveCoding = refdLast.canHaveCoding(timestamp, builtInCoding);
			if (canHaveCoding) {
				final Coding_Type newCoding = new Coding_Type();
				newCoding.builtIn = true;
				newCoding.modifier = modifier;
				newCoding.builtInCoding = builtInCoding;
				codingTable.add(newCoding);
				refdLast.setGenerateCoderFunctions(timestamp, builtInCoding);
			} else if (!silent){
				getLocation().reportSemanticWarning(MessageFormat.format("Type `{0}'' cannot have {1} encoding. Encode attribute ignored.", getTypename(), name));
			}
			break;
		}
		}
	}

	@Override
	/** {@inheritDoc} */
	public void setEncodingFunction(final String codingName, final Assignment functionDefinition) {
		final IType t = getTypeWithCodingTable(CompilationTimeStamp.getBaseTimestamp(), false);
		if (t == null) {
			return;
		}

		final List<Coding_Type> tempCodingTable = t.getCodingTable();
		for (int i = 0; i < tempCodingTable.size(); i++) {
			final Coding_Type tempCoding = tempCodingTable.get(i);
			if (!tempCoding.builtIn && tempCoding.customCoding.name.equals(codingName)) {
				final HashMap<IType, CoderFunction_Type> tempCoders = tempCoding.customCoding.encoders;
				if (tempCoders.containsKey(this) && tempCoders.get(this).functionDefinition != functionDefinition) {
					tempCoders.get(this).conflict = true;
				} else {
					final CoderFunction_Type newCoder = new CoderFunction_Type();
					newCoder.functionDefinition = functionDefinition;
					newCoder.conflict = false;
					tempCoders.put(this, newCoder);
					//phantom imports can be handled in code generator
				}

				return;
			}
		}
	}

	@Override
	/** {@inheritDoc} */
	public void setDecodingFunction(final String codingName, final Assignment functionDefinition) {
		final IType t = getTypeWithCodingTable(CompilationTimeStamp.getBaseTimestamp(), false);
		if (t == null) {
			return;
		}

		final List<Coding_Type> tempCodingTable = t.getCodingTable();
		for (int i = 0; i < tempCodingTable.size(); i++) {
			final Coding_Type tempCoding = tempCodingTable.get(i);
			if (!tempCoding.builtIn && tempCoding.customCoding.name.equals(codingName)) {
				final HashMap<IType, CoderFunction_Type> tempCoders = tempCoding.customCoding.decoders;
				if (tempCoders.containsKey(this) && tempCoders.get(this).functionDefinition != functionDefinition) {
					tempCoders.get(this).conflict = true;
				} else {
					final CoderFunction_Type newCoder = new CoderFunction_Type();
					newCoder.functionDefinition = functionDefinition;
					newCoder.conflict = false;
					tempCoders.put(this, newCoder);
					//phantom imports can be handled in code generator
				}

				return;
			}
		}
	}

	@Override
	/** {@inheritDoc} */
	public IType getTypeWithCodingTable(final CompilationTimeStamp timestamp, final boolean ignoreLocal) {
		// 1st priority: if local attributes are not ignored, and if the type
		// has its own 'encode' attributes (its coding table is not
		// empty), then
		// return the type
		if (!ignoreLocal && !codingTable.isEmpty()) {
			return this;
		}

		// 2nd priority: if this is a field or element type, and one of its parents
		// has an 'encode' attribute with the 'override' modifier, then return the parent type
		IType parent = null;
		if (parentType != null && (ownerType ==TypeOwner_type.OT_COMP_FIELD || ownerType == TypeOwner_type.OT_RECORD_OF || ownerType == TypeOwner_type.OT_ARRAY)) {
			// note: if one of the parent types has an overriding 'encode' attribute,
			// then this returns the farthest parent with an overriding 'encode';
			// if none of the 'encode' attributes are overriding, then the nearest
			// parent with at least one 'encode' attribute is returned
			parent = parentType.getTypeWithCodingTable(timestamp, true);
		}
		if (parent != null) {
			final List<Coding_Type> tempCodingTable = parent.getCodingTable();
			for (int i = 0; i < tempCodingTable.size(); i++){
				if (tempCodingTable.get(i).modifier == Attribute_Modifier_type.MOD_OVERRIDE) {
					return parent;
				}
			}
		}

		// 3rd priority: if local attributes are ignored, and if the type has its
		// own (non-local) 'encode' attributes, then return the type
		if (ignoreLocal && !codingTable.isEmpty()) {
			boolean local = false;
			for (int i = 0; i < codingTable.size(); i++) {
				if (codingTable.get(i).modifier == Attribute_Modifier_type.MOD_LOCAL) {
					local = true;
					break;
				}
			}
			if (!local) {
				return this;
			}
		}

		// 4th priority, if a referenced type has an 'encode' attribute, then return
		// the referenced type
		if (this instanceof Referenced_Type) {
			final IReferenceChain chain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
			IType tempType = ((Referenced_Type)this).getTypeRefd(timestamp, chain);
			chain.release();
			if (tempType == null || tempType.getIsErroneous(timestamp) ) {
				return parent;
			}

			tempType = tempType.getTypeWithCodingTable(timestamp, false);
			if (tempType != null) {
				return tempType;
			}
		}

		// otherwise return the parent type pointer (whether it's null or not)
		return parent;
	}

	@Override
	/** {@inheritDoc} */
	public boolean canHaveCoding(final CompilationTimeStamp timestamp, final MessageEncoding_type coding) {
		//TODO base type behaviour

		return false;
	}

	@Override
	/** {@inheritDoc} */
	public boolean hasEncoding(final CompilationTimeStamp timestamp, final MessageEncoding_type coding, final String customEncoding) {
		if (coding == MessageEncoding_type.UNDEFINED || (coding == MessageEncoding_type.CUSTOM && customEncoding == null)) {
			// FATAL error
			return false;
		}

		switch (coding) {
		case BER:
		case PER:
		case OER:
		case TEXT:
		case XER:
			//FIXME not yet supported
			return true;
		default:{
			final IType t = getTypeWithCodingTable(timestamp, false);
			if (t != null) {
				final boolean builtIn = coding != MessageEncoding_type.CUSTOM;
				final String encodingName = builtIn ? coding.getEncodingName() : customEncoding;
				final List<Coding_Type> codingTable = t.getCodingTable();
				for (int i = 0; i < codingTable.size(); i++) {
					final Coding_Type tempCodingType = codingTable.get(i);
					if (builtIn == tempCodingType.builtIn
							&& ((builtIn && tempCodingType.builtInCoding == coding) || (!builtIn && tempCodingType.customCoding.name
									.equals(encodingName)))) {
						return true;
					}
				}
			}

			final IType lastType = getTypeRefdLast(timestamp);
			if (coding == MessageEncoding_type.CUSTOM) {
				//  all types need an 'encode' attribute for user-defined codecs
				return false;
			}
			switch (getTypetype()) {
			case TYPE_ASN1_SEQUENCE:
			case TYPE_TTCN3_SEQUENCE:
			case TYPE_ASN1_SET:
			case TYPE_TTCN3_SET:
			case TYPE_SEQUENCE_OF:
			case TYPE_SET_OF:
			case TYPE_ARRAY:
			case TYPE_ASN1_CHOICE:
			case TYPE_TTCN3_CHOICE:
			case TYPE_ANYTYPE:
			case TYPE_ASN1_ENUMERATED:
			case TYPE_TTCN3_ENUMERATED:
				// these types need an 'encode' attribute for built-in codecs,
				return false;
			default:
				break;
			}

			final boolean canHave = lastType.canHaveCoding(timestamp, coding);

			return canHave;
		}
		}
	}

	@Override
	/** {@inheritDoc} */
	public List<Coding_Type> getCodingTable() {
		return codingTable;
	}

	@Override
	/** {@inheritDoc} */
	public void checkConstructorName(final String definitionName) {
		// nothing to be done by default
	}

	@Override
	/** {@inheritDoc} */
	public SubType.SubType_type getSubtypeType() {
		return SubType.SubType_type.ST_NONE;
	}

	protected void checkSubtypeRestrictions(final CompilationTimeStamp timestamp) {
		checkSubtypeRestrictions(timestamp, getSubtypeType(), null);
	}

	/** create and check subtype, called by the check function of the type */
	protected void checkSubtypeRestrictions(final CompilationTimeStamp timestamp, final SubType.SubType_type subtypeType,
			final SubType parentSubtype) {

		if (getIsErroneous(timestamp)) {
			return;
		}

		// if there is no own or parent sub-type then there's nothing to
		// do
		if ((parsedRestrictions == null) && (parentSubtype == null)) {
			return;
		}

		// if the type has no subtype type
		if (subtypeType == SubType.SubType_type.ST_NONE) {
			getLocation().reportSemanticError(
					MessageFormat.format("TTCN-3 subtype constraints are not applicable to type `{0}''", getTypename()));
			setIsErroneous(true);
			return;
		}

		subType = new SubType(subtypeType, this, parsedRestrictions, parentSubtype);

		subType.check(timestamp);
	}

	@Override
	/** {@inheritDoc} */
	public void checkRecursions(final CompilationTimeStamp timestamp, final IReferenceChain referenceChain) {
	}

	@Override
	/** {@inheritDoc} */
	public boolean isComponentInternal(final CompilationTimeStamp timestamp) {
		return false;
	}

	@Override
	/** {@inheritDoc} */
	public void checkComponentInternal(final CompilationTimeStamp timestamp, final Set<IType> typeSet, final String operation) {
		// nothing to be done by default
	}

	@Override
	/** {@inheritDoc} */
	public void checkEmbedded(final CompilationTimeStamp timestamp, final Location errorLocation, final boolean defaultAllowed,
			final String errorMessage) {
		// nothing to be done by default
	}

	@Override
	/** {@inheritDoc} */
	public IValue checkThisValueRef(final CompilationTimeStamp timestamp, final IValue value) {
		if (Value_type.UNDEFINED_LOWERIDENTIFIER_VALUE.equals(value.getValuetype())) {
			return value.setValuetype(timestamp, Value_type.REFERENCED_VALUE);
		}

		return value;
	}

	@Override
	/** {@inheritDoc} */
	public boolean checkThisValue(final CompilationTimeStamp timestamp, final IValue value, final Assignment lhs, final ValueCheckingOptions valueCheckingOptions) {
		value.setIsErroneous(false);

		final Assignment assignment = getDefiningAssignment();
		if (assignment != null && assignment instanceof Definition) {
			final Scope scope = value.getMyScope();
			if (scope != null) {
				final Module module = scope.getModuleScope();
				if (module != null) {
					final String referingModuleName = module.getName();
					if (!((Definition)assignment).referingHere.contains(referingModuleName)) {
						((Definition)assignment).referingHere.add(referingModuleName);
					}
				} else {
					ErrorReporter.logError("The value `" + value.getFullName() + "' does not appear to be in a module");
					value.setIsErroneous(true);
				}
			} else {
				ErrorReporter.logError("The value `" + value.getFullName() + "' does not appear to be in a scope");
				value.setIsErroneous(true);
			}
		}

		check(timestamp);
		final IValue last = value.getValueRefdLast(timestamp, valueCheckingOptions.expected_value, null);
		if (last == null || last.getIsErroneous(timestamp) || getIsErroneous(timestamp)) {
			return false;
		}

		if (Value_type.OMIT_VALUE.equals(last.getValuetype()) && !valueCheckingOptions.omit_allowed) {
			value.getLocation().reportSemanticError("`omit' value is not allowed in this context");
			value.setIsErroneous(true);
			return false;
		}

		boolean selfReference = false;
		switch (value.getValuetype()) {
		case UNDEFINED_LOWERIDENTIFIER_VALUE:
			if (Value_type.REFERENCED_VALUE.equals(last.getValuetype())) {
				final IReferenceChain chain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
				selfReference = checkThisReferencedValue(timestamp, last, lhs, valueCheckingOptions.expected_value, chain, valueCheckingOptions.sub_check,
						valueCheckingOptions.str_elem);
				chain.release();
				return selfReference;
			}
			return false;
		case REFERENCED_VALUE: {
			final IReferenceChain chain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
			selfReference = checkThisReferencedValue(timestamp, value, lhs, valueCheckingOptions.expected_value, chain, valueCheckingOptions.sub_check,
					valueCheckingOptions.str_elem);
			chain.release();
			return selfReference;
		}
		case EXPRESSION_VALUE:
			selfReference = value.checkExpressionSelfReference(timestamp, lhs);
			if (value.isUnfoldable(timestamp, null)) {
				final Type_type temporalType = value.getExpressionReturntype(timestamp, valueCheckingOptions.expected_value);
				if (!Type_type.TYPE_UNDEFINED.equals(temporalType)
						&& !isCompatible(timestamp, this.getTypetype(), temporalType, false, value.isAsn())) {
					value.getLocation().reportSemanticError(MessageFormat.format(INCOMPATIBLEVALUE, getTypename()));
					value.setIsErroneous(true);
				}
			}
			return selfReference;
		case MACRO_VALUE:
			selfReference = value.checkExpressionSelfReference(timestamp, lhs);
			if (value.isUnfoldable(timestamp, null)) {
				final Type_type temporalType = value.getExpressionReturntype(timestamp, valueCheckingOptions.expected_value);
				if (!Type_type.TYPE_UNDEFINED.equals(temporalType)
						&& !isCompatible(timestamp, this.getTypetype(), temporalType, false, value.isAsn())) {
					value.getLocation().reportSemanticError(MessageFormat.format(INCOMPATIBLEVALUE, getTypename()));
					value.setIsErroneous(true);
				}
				return selfReference;
			}
			break;
		default:
			break;
		}

		return selfReference;
	}

	/**
	 * Checks the provided referenced value.
	 *
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle.
	 * @param value
	 *                the referenced value to be checked.
	 * @param lhs
	 *                the assignment to check against
	 * @param expectedValue
	 *                the expectations we have for the value.
	 * @param referenceChain
	 *                the reference chain to detect circular references.
	 * @param strElem
	 *                true if the value to be checked is an element of a
	 *                string
	 * @return true if the value contains a reference to lhs
	 * */
	private boolean checkThisReferencedValue(final CompilationTimeStamp timestamp, final IValue value, final Assignment lhs, final Expected_Value_type expectedValue,
			final IReferenceChain referenceChain, final boolean subCheck, final boolean strElem) {
		final Reference reference = ((Referenced_Value) value).getReference();
		final Assignment assignment = reference.getRefdAssignment(timestamp, true, referenceChain);

		if (assignment == null) {
			value.setIsErroneous(true);
			return false;
		}

		final Assignment myAssignment = getDefiningAssignment();
		if (myAssignment != null && myAssignment instanceof Definition) {
			final String referingModuleName = value.getMyScope().getModuleScope().getName();
			if (!((Definition)myAssignment).referingHere.contains(referingModuleName)) {
				((Definition)myAssignment).referingHere.add(referingModuleName);
			}
		}

		assignment.check(timestamp);
		final boolean selfReference = assignment == lhs;

		boolean isConst = false;
		boolean errorFlag = false;
		boolean checkRunsOn = false;
		IType governor = null;
		if (assignment.getIsErroneous()) {
			value.setIsErroneous(true);
		} else {
			switch (assignment.getAssignmentType()) {
			case A_CONST:
				isConst = true;
				break;
			case A_OBJECT:
			case A_OS:
				final ISetting setting = reference.getRefdSetting(timestamp);
				if (setting == null || setting.getIsErroneous(timestamp)) {
					value.setIsErroneous(true);
					return selfReference;
				}

				if (!Setting_type.S_V.equals(setting.getSettingtype())) {
					reference.getLocation().reportSemanticError(
							MessageFormat.format("This InformationFromObjects construct does not refer to a value: {0}",
									value.getFullName()));
					value.setIsErroneous(true);
					return selfReference;
				}

				governor = ((Value) setting).getMyGovernor();
				if (governor != null) {
					isConst = true;
				}
				break;
			case A_EXT_CONST:
			case A_MODULEPAR:
				if (Expected_Value_type.EXPECTED_CONSTANT.equals(expectedValue)) {
					value.getLocation().reportSemanticError(
							MessageFormat.format(
									"Reference to an (evaluatable) constant value was expected instead of {0}",
									assignment.getDescription()));
					errorFlag = true;
				}
				break;
			case A_VAR:
			case A_PAR_VAL:
			case A_PAR_VAL_IN:
			case A_PAR_VAL_OUT:
			case A_PAR_VAL_INOUT:
				switch (expectedValue) {
				case EXPECTED_CONSTANT:
					value.getLocation().reportSemanticError(
							MessageFormat.format("Reference to a constant value was expected instead of {0}",
									assignment.getDescription()));
					errorFlag = true;
					break;
				case EXPECTED_STATIC_VALUE:
					value.getLocation().reportSemanticError(
							MessageFormat.format("Reference to a static value was expected instead of {0}",
									assignment.getDescription()));
					errorFlag = true;
					break;
				default:
					break;
				}
				break;
			case A_TEMPLATE:
			case A_MODULEPAR_TEMPLATE:
			case A_VAR_TEMPLATE:
			case A_PAR_TEMP_IN:
			case A_PAR_TEMP_OUT:
			case A_PAR_TEMP_INOUT:
				if (!Expected_Value_type.EXPECTED_TEMPLATE.equals(expectedValue)) {
					value.getLocation().reportSemanticError(
							MessageFormat.format(REFTOVALUEEXPECTED,
									assignment.getDescription()));
					errorFlag = true;
				}
				break;
			case A_FUNCTION_RVAL:
				checkRunsOn = true;
				switch (expectedValue) {
				case EXPECTED_CONSTANT: {
					final String message = MessageFormat.format(
							"Reference to a constant value was expected instead of the return value of {0}",
							assignment.getDescription());
					value.getLocation().reportSemanticError(message);
					errorFlag = true;
				}
				break;
				case EXPECTED_STATIC_VALUE: {
					final String message = MessageFormat.format(
							"Reference to a static value was expected instead of the return value of {0}",
							assignment.getDescription());
					value.getLocation().reportSemanticError(message);
					errorFlag = true;
				}
				break;
				default:
					break;
				}
				break;
			case A_EXT_FUNCTION_RVAL:
				switch (expectedValue) {
				case EXPECTED_CONSTANT: {
					final String message = MessageFormat.format(
							"Reference to a constant value was expected instead of the return value of {0}",
							assignment.getDescription());
					value.getLocation().reportSemanticError(message);
					errorFlag = true;
				}
				break;
				case EXPECTED_STATIC_VALUE: {
					final String message = MessageFormat.format(
							"Reference to a static value was expected instead of the return value of {0}",
							assignment.getDescription());
					value.getLocation().reportSemanticError(message);
					errorFlag = true;
				}
				break;
				default:
					break;
				}
				break;
			case A_FUNCTION_RTEMP:
				checkRunsOn = true;
				if (!Expected_Value_type.EXPECTED_TEMPLATE.equals(expectedValue)) {
					value.getLocation()
					.reportSemanticError(
							MessageFormat.format(
									REFTOVALUEEXPECTED_INSTEADOFCALL,
									assignment.getDescription()));
					errorFlag = true;
				}
				break;
			case A_EXT_FUNCTION_RTEMP:
				if (!Expected_Value_type.EXPECTED_TEMPLATE.equals(expectedValue)) {
					value.getLocation()
					.reportSemanticError(
							MessageFormat.format(
									REFTOVALUEEXPECTED_INSTEADOFCALL,
									assignment.getDescription()));
					errorFlag = true;
				}
				break;
			case A_FUNCTION:
			case A_EXT_FUNCTION:
				value.getLocation()
				.reportSemanticError(
						MessageFormat.format(
								"Reference to a {0} was expected instead of a call of {1}, which does not have a return type",
								Expected_Value_type.EXPECTED_TEMPLATE.equals(expectedValue) ? "value or template"
										: "value", assignment.getDescription()));
				value.setIsErroneous(true);
				return selfReference;
			default:
				value.getLocation().reportSemanticError(
						MessageFormat.format("Reference to a {0} was expected instead of {1}",
								Expected_Value_type.EXPECTED_TEMPLATE.equals(expectedValue) ? "value or template"
										: "value", assignment.getDescription()));
				value.setIsErroneous(true);
				return selfReference;
			}
		}

		if (checkRunsOn) {
			reference.getMyScope().checkRunsOnScope(timestamp, assignment, reference, "call");
		}
		if (governor == null) {
			final IType type = assignment.getType(timestamp);
			if (type != null) {
				governor = type.getFieldType(timestamp, reference, 1, expectedValue, referenceChain, false);
			}
		}
		if (governor == null) {
			value.setIsErroneous(true);
			return selfReference;
		}

		final TypeCompatibilityInfo info = new TypeCompatibilityInfo(this, governor, true);
		info.setStr1Elem(strElem);
		info.setStr2Elem(reference.refersToStringElement());
		final CompatibilityLevel compatibilityLevel = getCompatibility(timestamp, governor, info, null, null);
		if (compatibilityLevel != CompatibilityLevel.COMPATIBLE) {
			// Port or signature values do not exist at all. These
			// errors are already
			// reported at those definitions. Extra errors should
			// not be reported
			// here.
			final IType type = getTypeRefdLast(timestamp, null);
			switch (type.getTypetype()) {
			case TYPE_PORT:
				// neither port values nor templates exist
				break;
			case TYPE_SIGNATURE:
				if (Expected_Value_type.EXPECTED_TEMPLATE.equals(expectedValue)) {
					final String message = MessageFormat.format(
							"Type mismatch: a signature template of type `{0}'' was expected instead of `{1}''",
							getTypename(), governor.getTypename());
					value.getLocation().reportSemanticError(message);
				}
				break;
			case TYPE_SEQUENCE_OF:
			case TYPE_ASN1_SEQUENCE:
			case TYPE_TTCN3_SEQUENCE:
			case TYPE_ARRAY:
			case TYPE_ASN1_SET:
			case TYPE_TTCN3_SET:
			case TYPE_SET_OF:
			case TYPE_ASN1_CHOICE:
			case TYPE_TTCN3_CHOICE:
			case TYPE_ANYTYPE:
				if (compatibilityLevel == CompatibilityLevel.INCOMPATIBLE_SUBTYPE) {
					value.getLocation().reportSemanticError(info.getSubtypeError());
				} else {
					value.getLocation().reportSemanticError(info.getErrorStringString());
				}
				break;
			default:
				if (compatibilityLevel == CompatibilityLevel.INCOMPATIBLE_SUBTYPE) {
					value.getLocation().reportSemanticError(info.getSubtypeError());
				} else {
					final String message = MessageFormat.format(
							"Type mismatch: a {0} of type `{1}'' was expected instead of `{2}''",
							Expected_Value_type.EXPECTED_TEMPLATE.equals(expectedValue) ? "value or template" : "value",
									getTypename(), governor.getTypename());
					value.getLocation().reportSemanticError(message);
				}
				break;
			}
			errorFlag = true;
		} else {
			if (GeneralConstants.WARNING.equals(typeCompatibilitySeverity)) {
				if (info.getNeedsConversion()) {
					value.getLocation().reportSemanticWarning(
							MessageFormat.format(TYPECOMPATWARNING, this.getTypename(), governor.getTypename()));
				}
			}
			if (info.getNeedsConversion()) {
				value.set_needs_conversion();
			}
		}

		if (errorFlag) {
			value.setIsErroneous(true);
			return selfReference;
		}

		// checking for circular references
		final IValue last = value.getValueRefdLast(timestamp, expectedValue, referenceChain);
		if (isConst && !last.getIsErroneous(timestamp)) {
			if (subCheck && (subType != null)) {
				subType.checkThisValue(timestamp, value);
			}
		}

		return selfReference;
	}

	@Override
	/** {@inheritDoc} */
	public ITTCN3Template checkThisTemplateRef(final CompilationTimeStamp timestamp, final ITTCN3Template t, final Expected_Value_type expectedValue,
			final IReferenceChain referenceChain) {
		switch( t.getTemplatetype() ){
		case SUPERSET_MATCH:
		case SUBSET_MATCH:
			final IType it1 = getTypeRefdLast(timestamp);
			final Type_type tt = it1.getTypetype();
			if(Type_type.TYPE_SEQUENCE_OF.equals(tt) || Type_type.TYPE_SET_OF.equals(tt) ) {
				return t;
			} else {
				t.getLocation().reportSemanticError(
						MessageFormat.format("{0} cannot be used for type {1}",t.getTemplateTypeName(), getTypename()));
				t.setIsErroneous(true);
				return t;
			}

		case SPECIFIC_VALUE:
			break; //cont below
		default:
			return t;
		}

		//Case of specific value:

		final ITTCN3Template template = t;
		IValue value = ((SpecificValue_Template) template).getSpecificValue();
		if (value == null) {
			return template;
		}

		value = checkThisValueRef(timestamp, value);

		switch (value.getValuetype()) {
		case REFERENCED_VALUE:
			final Assignment assignment = ((Referenced_Value) value).getReference().getRefdAssignment(timestamp, false, referenceChain); //FIXME: referenceChain or null?
			if (assignment == null) {
				template.setIsErroneous(true);
			} else {
				switch (assignment.getAssignmentType()) {
				case A_VAR_TEMPLATE:
					if(!Expected_Value_type.EXPECTED_TEMPLATE.equals(expectedValue)){
						template.getLocation().reportSemanticError(
								MessageFormat.format(REFTOVALUEEXPECTED,
										assignment.getDescription()));
						template.setIsErroneous(true);
					}

					final IType type = ((Def_Var_Template) assignment).getType(timestamp);
					switch (type.getTypetype()) {
					case TYPE_BITSTRING:
					case TYPE_BITSTRING_A:
					case TYPE_HEXSTRING:
					case TYPE_OCTETSTRING:
					case TYPE_CHARSTRING:
					case TYPE_UCHARSTRING:
					case TYPE_UTF8STRING:
					case TYPE_NUMERICSTRING:
					case TYPE_PRINTABLESTRING:
					case TYPE_TELETEXSTRING:
					case TYPE_VIDEOTEXSTRING:
					case TYPE_IA5STRING:
					case TYPE_GRAPHICSTRING:
					case TYPE_VISIBLESTRING:
					case TYPE_GENERALSTRING:
					case TYPE_UNIVERSALSTRING:
					case TYPE_BMPSTRING:
					case TYPE_UTCTIME:
					case TYPE_GENERALIZEDTIME:
					case TYPE_OBJECTDESCRIPTOR: {
						final List<ISubReference> subReferences = ((Referenced_Value) value).getReference().getSubreferences();
						final int nofSubreferences = subReferences.size();
						if (nofSubreferences > 1) {
							final ISubReference subreference = subReferences.get(nofSubreferences - 1);
							if (subreference instanceof ArraySubReference) {
								template.getLocation().reportSemanticError(
										MessageFormat.format("Reference to {0} can not be indexed",
												assignment.getDescription()));
								template.setIsErroneous(true);
								return template;
							}
						}
						break;
					}
					default:
						break;
					}
					return template.setTemplatetype(timestamp, Template_type.TEMPLATE_REFD);
				case A_CONST:
					IType type1;
					if( assignment instanceof Value_Assignment){
						type1 = ((Value_Assignment) assignment).getType(timestamp);
					} else {
						type1 = ((Def_Const) assignment).getType(timestamp);
					}
					switch (type1.getTypetype()) {
					case TYPE_BITSTRING:
					case TYPE_BITSTRING_A:
					case TYPE_HEXSTRING:
					case TYPE_OCTETSTRING:
					case TYPE_CHARSTRING:
					case TYPE_UCHARSTRING:
					case TYPE_UTF8STRING:
					case TYPE_NUMERICSTRING:
					case TYPE_PRINTABLESTRING:
					case TYPE_TELETEXSTRING:
					case TYPE_VIDEOTEXSTRING:
					case TYPE_IA5STRING:
					case TYPE_GRAPHICSTRING:
					case TYPE_VISIBLESTRING:
					case TYPE_GENERALSTRING:
					case TYPE_UNIVERSALSTRING:
					case TYPE_BMPSTRING:
					case TYPE_UTCTIME:
					case TYPE_GENERALIZEDTIME:
					case TYPE_OBJECTDESCRIPTOR: {
						final List<ISubReference> subReferences = ((Referenced_Value) value).getReference().getSubreferences();
						final int nofSubreferences = subReferences.size();
						if (nofSubreferences > 1) {
							final ISubReference subreference = subReferences.get(nofSubreferences - 1);
							if (subreference instanceof ArraySubReference) {
								template.getLocation().reportSemanticError(
										MessageFormat.format("Reference to {0} can not be indexed",
												assignment.getDescription()));
								template.setIsErroneous(true);
								return template;
							}
						}
						break;
					}
					default:
						break;
					}
					break;
				case A_TEMPLATE:
				case A_MODULEPAR_TEMPLATE:
				case A_PAR_TEMP_IN:
				case A_PAR_TEMP_OUT:
				case A_PAR_TEMP_INOUT:
				case A_FUNCTION_RTEMP:
				case A_EXT_FUNCTION_RTEMP:
					if(!Expected_Value_type.EXPECTED_TEMPLATE.equals(expectedValue)){
						template.getLocation().reportSemanticError(
								MessageFormat.format(REFTOVALUEEXPECTED,
										assignment.getDescription()));
						template.setIsErroneous(true);
					}
					return template.setTemplatetype(timestamp, Template_type.TEMPLATE_REFD);
				default:
					break;
				}
			}
			break;
		case EXPRESSION_VALUE: {
			final Expression_Value expression = (Expression_Value) value;
			if (Operation_type.APPLY_OPERATION.equals(expression.getOperationType())) {
				IType type = expression.getExpressionGovernor(timestamp, Expected_Value_type.EXPECTED_TEMPLATE);
				if (type == null) {
					break;
				}

				type = type.getTypeRefdLast(timestamp);
				if (type != null && Type_type.TYPE_FUNCTION.equals(type.getTypetype()) && ((Function_Type) type).returnsTemplate()) {
					return template.setTemplatetype(timestamp, Template_type.TEMPLATE_INVOKE);
				}
			}
			break;
		}
		default:
			break;
		}

		return template;

	}

	//TODO: This function is obsolete, use the general function everywhere instead!
	@Override
	/** {@inheritDoc} */
	public ITTCN3Template checkThisTemplateRef(final CompilationTimeStamp timestamp, final ITTCN3Template t) {
		return checkThisTemplateRef(timestamp,t,Expected_Value_type.EXPECTED_TEMPLATE, null);
	}

	/**
	 * Checks the provided index as a part of a string.
	 *
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 * @param indexValue
	 *                the index value to be checked.
	 * @param expectedIndex
	 *                the expected kind of the index value.
	 * @param refChain
	 *                a chain of references used to detect circular
	 *                references.
	 * */
	protected void checkStringIndex(final CompilationTimeStamp timestamp, final Value indexValue, final Expected_Value_type expectedIndex, final IReferenceChain refChain) {
		if (indexValue != null) {
			indexValue.setLoweridToReference(timestamp);
			final Type_type tempType = indexValue.getExpressionReturntype(timestamp, expectedIndex);
			switch (tempType) {
			case TYPE_INTEGER:
				final IValue last = indexValue.getValueRefdLast(timestamp, expectedIndex, refChain);
				if (Value_type.INTEGER_VALUE.equals(last.getValuetype())) {
					final Integer_Value lastInteger = (Integer_Value) last;
					if (lastInteger.isNative()) {
						final long temp = lastInteger.getValue();
						if (temp < 0) {
							indexValue.getLocation().reportSemanticError(MessageFormat.format(SequenceOf_Type.NONNEGATIVINDEXEXPECTED, temp));
							indexValue.setIsErroneous(true);
						}
					} else {
						indexValue.getLocation().reportSemanticError(MessageFormat.format(SequenceOf_Type.TOOBIGINDEX, lastInteger.getValueValue(), getTypename()));
						indexValue.setIsErroneous(true);
					}
				}
				break;
			case TYPE_UNDEFINED:
				indexValue.setIsErroneous(true);
				break;
			default:
				indexValue.getLocation().reportSemanticError(SequenceOf_Type.INTEGERINDEXEXPECTED);
				indexValue.setIsErroneous(true);
				break;
			}
		}
	}

	/**
	 * Register the usage of this type in the provided template.
	 *
	 * @param template
	 *                the template to use.
	 * */
	protected void registerUsage(final ITTCN3Template template) {
		final Assignment assignment = getDefiningAssignment();
		if (assignment != null && assignment instanceof Definition) {
			final String referingModuleName = template.getMyScope().getModuleScope().getName();
			if (!((Definition)assignment).referingHere.contains(referingModuleName)) {
				((Definition)assignment).referingHere.add(referingModuleName);
			}
		}
	}

	@Override
	/** {@inheritDoc} */
	public abstract boolean checkThisTemplate(final CompilationTimeStamp timestamp, final ITTCN3Template template, final boolean isModified,
			final boolean implicitOmit, final Assignment lhs);

	@Override
	/** {@inheritDoc} */
	public final void checkThisTemplateSubtype(final CompilationTimeStamp timestamp, final ITTCN3Template template) {
		if (ITTCN3Template.Template_type.PERMUTATION_MATCH.equals(template.getTemplatetype())) {
			// a permutation is just a fragment, in itself it has no type
			return;
		}

		if (subType != null) {
			subType.checkThisTemplateGeneric(timestamp, template);
		}
	}

	@Override
	/** {@inheritDoc} */
	public abstract boolean isCompatible(final CompilationTimeStamp timestamp, final IType otherType, TypeCompatibilityInfo info,
			final TypeCompatibilityInfo.Chain leftChain, final TypeCompatibilityInfo.Chain rightChain);

	@Override
	/** {@inheritDoc} */
	public boolean isStronglyCompatible(final CompilationTimeStamp timestamp, final IType otherType, final TypeCompatibilityInfo info,
			final TypeCompatibilityInfo.Chain leftChain, final TypeCompatibilityInfo.Chain rightChain) {

		check(timestamp);
		otherType.check(timestamp);
		final IType thisTypeLast = this.getTypeRefdLast(timestamp);
		final IType otherTypeLast = otherType.getTypeRefdLast(timestamp);

		if (thisTypeLast == null || otherTypeLast == null || thisTypeLast.getIsErroneous(timestamp)
				|| otherTypeLast.getIsErroneous(timestamp)) {
			return true;
		}

		return thisTypeLast.getTypetype().equals(otherTypeLast.getTypetype());
	}

	public enum CompatibilityLevel {
		INCOMPATIBLE_TYPE, INCOMPATIBLE_SUBTYPE, COMPATIBLE
	}

	@Override
	/** {@inheritDoc} */
	public CompatibilityLevel getCompatibility(final CompilationTimeStamp timestamp, final IType type, final TypeCompatibilityInfo info,
			final TypeCompatibilityInfo.Chain leftChain, final TypeCompatibilityInfo.Chain rightChain) {
		if (info == null) {
			ErrorReporter.INTERNAL_ERROR("info==null");
		}

		if (!isCompatible(timestamp, type, info, leftChain, rightChain)) {
			return CompatibilityLevel.INCOMPATIBLE_TYPE;
		}

		// if there is noStructuredTypeCompatibility and isCompatible then it should be strong compatibility:
		if( noStructuredTypeCompatibility ) {
			return CompatibilityLevel.COMPATIBLE;
		}

		final SubType otherSubType = type.getSubtype();
		if ((info != null) && (subType != null) && (otherSubType != null)) {
			if (info.getStr1Elem()) {
				if (info.getStr2Elem()) {
					// both are string elements -> nothing
					// to do
				} else {
					// char <-> string
					if (!otherSubType.isCompatibleWithElem(timestamp)) {
						info.setSubtypeError("Subtype mismatch: string element has no common value with subtype "
								+ otherSubType.toString());
						return CompatibilityLevel.INCOMPATIBLE_SUBTYPE;
					}
				}
			} else {
				if (info.getStr2Elem()) {
					// string <-> char
					if (!subType.isCompatibleWithElem(timestamp)) {
						info.setSubtypeError("Subtype mismatch: subtype " + subType.toString()
								+ " has no common value with string element");
						return CompatibilityLevel.INCOMPATIBLE_SUBTYPE;
					}
				} else {
					// string <-> string
					if (!subType.isCompatible(timestamp, otherSubType)) {
						info.setSubtypeError("Subtype mismatch: subtype " + subType.toString()
								+ " has no common value with subtype " + otherSubType.toString());
						return CompatibilityLevel.INCOMPATIBLE_SUBTYPE;
					}
				}
			}
		}

		return CompatibilityLevel.COMPATIBLE;
	}

	@Override
	/** {@inheritDoc} */
	public boolean isCompatibleByPort(final CompilationTimeStamp timestamp, final IType otherType) {
		check(timestamp);
		otherType.check(timestamp);

		return false;
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

		return getTypetypeTtcn3().equals(temp.getTypetypeTtcn3());
	}

	@Override
	/** {@inheritDoc} */
	public void checkMapParameter(final CompilationTimeStamp timestamp, final IReferenceChain refChain, final Location errorLocation) {
		// the default implementation is empty as it is allowed
	}

	/**
	 * Return the encoding belonging to the provided name.
	 *
	 * @param encoding
	 *                the name of the encoding to identify
	 * @return the encoding identified by the name or undefined otherwise.
	 * */
	public static MessageEncoding_type getEncodingType(final String encoding) {
		if ("RAW".equals(encoding)) {
			return MessageEncoding_type.RAW;
		} else if ("TEXT".equals(encoding)) {
			return MessageEncoding_type.TEXT;
		} else if ("JSON".equals(encoding)) {
			return MessageEncoding_type.JSON;
		} else if ("BER:2002".equals(encoding) || "CER:2002".equals(encoding) || "DER:2002".equals(encoding)) {
			return MessageEncoding_type.BER;
		} else if ("XML".equals(encoding) || "XER".equals(encoding)) {
			return MessageEncoding_type.XER;
		} else if ("PER".equals(encoding)) {
			return MessageEncoding_type.PER;
		} else if ("OER".equals(encoding)) {
			return MessageEncoding_type.OER;
		} else {
			return MessageEncoding_type.CUSTOM;
		}
	}

	@Override
	/** {@inheritDoc} */
	public abstract String getTypename();

	@Override
	/** {@inheritDoc} */
	public abstract Type_type getTypetypeTtcn3();

	/**
	 * Creates and returns the description of this type, used to describe it
	 * as a completion proposal.
	 *
	 * @param builder
	 *                the StringBuilder used to create the description.
	 *
	 * @return the description of this type.
	 * */
	@Override
	/** {@inheritDoc} */
	public StringBuilder getProposalDescription(final StringBuilder builder) {
		return builder;
	}

	@Override
	/** {@inheritDoc} */
	public Identifier getIdentifier() {
		return null;
	}

	@Override
	/** {@inheritDoc} */
	public Object[] getOutlineChildren() {
		return new Object[] {};
	}

	@Override
	/** {@inheritDoc} */
	public String getOutlineText() {
		return "";
	}

	@Override
	/** {@inheritDoc} */
	public int category() {
		return getTypetype().ordinal();
	}

	/**
	 * Searches and adds a completion proposal to the provided collector if
	 * a valid one is found.
	 * <p>
	 * If this type is a simple type, it can never complete any proposals.
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
	}

	/**
	 * Searches and adds a declaration proposal to the provided collector if
	 * a valid one is found.
	 * <p>
	 * Simple types can not be used as declarations.
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
	}

	/**
	 * Returns whether this type is compatible with type. Used if the other
	 * value is unfoldable, but we can determine its expression return type
	 * <p>
	 * Note: The compatibility relation is asymmetric. The function returns
	 * true if the set of possible values in type is a subset of possible
	 * values in this.
	 *
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 * @param typeType1
	 *                the type of the first type.
	 * @param typeType2
	 *                the type of the second type.
	 * @param isAsn11
	 *                true if the first type is from ASN.1
	 * @param isAsn12
	 *                true if the second type is from ASN.1
	 *
	 * @return true if the first type is compatible with the second,
	 *         otherwise false.
	 * */
	public static final boolean isCompatible(final CompilationTimeStamp timestamp, final Type_type typeType1, final Type_type typeType2,
			final boolean isAsn11, final boolean isAsn12) {
		if (Type_type.TYPE_UNDEFINED.equals(typeType1) || Type_type.TYPE_UNDEFINED.equals(typeType2)) {
			return true;
		}

		switch (typeType1) {
		case TYPE_NULL:
		case TYPE_BOOL:
		case TYPE_REAL:
		case TYPE_HEXSTRING:
		case TYPE_SEQUENCE_OF:
		case TYPE_SET_OF:
		case TYPE_VERDICT:
		case TYPE_DEFAULT:
		case TYPE_COMPONENT:
		case TYPE_SIGNATURE:
		case TYPE_PORT:
		case TYPE_ARRAY:
		case TYPE_FUNCTION:
		case TYPE_ALTSTEP:
		case TYPE_TESTCASE:
			return typeType1.equals(typeType2);
		case TYPE_OCTETSTRING:
			return Type_type.TYPE_OCTETSTRING.equals(typeType2) || (!isAsn11 && Type_type.TYPE_ANY.equals(typeType2));
		case TYPE_UCHARSTRING:
			switch (typeType2) {
			case TYPE_UCHARSTRING:
			case TYPE_UTF8STRING:
			case TYPE_BMPSTRING:
			case TYPE_UNIVERSALSTRING:
			case TYPE_TELETEXSTRING:
			case TYPE_VIDEOTEXSTRING:
			case TYPE_GRAPHICSTRING:
			case TYPE_OBJECTDESCRIPTOR:
			case TYPE_GENERALSTRING:
			case TYPE_CHARSTRING:
			case TYPE_NUMERICSTRING:
			case TYPE_PRINTABLESTRING:
			case TYPE_IA5STRING:
			case TYPE_VISIBLESTRING:
			case TYPE_UTCTIME:
			case TYPE_GENERALIZEDTIME:
				return true;
			default:
				return false;
			}
		case TYPE_UTF8STRING:
		case TYPE_BMPSTRING:
		case TYPE_UNIVERSALSTRING:
			switch (typeType2) {
			case TYPE_UCHARSTRING:
			case TYPE_UTF8STRING:
			case TYPE_BMPSTRING:
			case TYPE_UNIVERSALSTRING:
			case TYPE_CHARSTRING:
			case TYPE_NUMERICSTRING:
			case TYPE_PRINTABLESTRING:
			case TYPE_IA5STRING:
			case TYPE_VISIBLESTRING:
			case TYPE_UTCTIME:
			case TYPE_GENERALIZEDTIME:
				return true;
			default:
				return false;
			}
		case TYPE_TELETEXSTRING:
		case TYPE_VIDEOTEXSTRING:
		case TYPE_GRAPHICSTRING:
		case TYPE_OBJECTDESCRIPTOR:
		case TYPE_GENERALSTRING:
			switch (typeType2) {
			case TYPE_TELETEXSTRING:
			case TYPE_VIDEOTEXSTRING:
			case TYPE_GRAPHICSTRING:
			case TYPE_OBJECTDESCRIPTOR:
			case TYPE_GENERALSTRING:
			case TYPE_CHARSTRING:
			case TYPE_NUMERICSTRING:
			case TYPE_PRINTABLESTRING:
			case TYPE_IA5STRING:
			case TYPE_VISIBLESTRING:
			case TYPE_UTCTIME:
			case TYPE_GENERALIZEDTIME:
			case TYPE_UCHARSTRING:
				return true;
			default:
				return false;
			}
		case TYPE_CHARSTRING:
		case TYPE_NUMERICSTRING:
		case TYPE_PRINTABLESTRING:
		case TYPE_IA5STRING:
		case TYPE_VISIBLESTRING:
		case TYPE_UTCTIME:
		case TYPE_GENERALIZEDTIME:
			switch (typeType2) {
			case TYPE_CHARSTRING:
			case TYPE_NUMERICSTRING:
			case TYPE_PRINTABLESTRING:
			case TYPE_IA5STRING:
			case TYPE_VISIBLESTRING:
			case TYPE_UTCTIME:
			case TYPE_GENERALIZEDTIME:
				return true;
			default:
				return false;
			}
		case TYPE_BITSTRING:
		case TYPE_BITSTRING_A:
			return Type_type.TYPE_BITSTRING.equals(typeType2) || Type_type.TYPE_BITSTRING_A.equals(typeType2);
		case TYPE_INTEGER:
		case TYPE_INTEGER_A:
			return Type_type.TYPE_INTEGER.equals(typeType2) || Type_type.TYPE_INTEGER_A.equals(typeType2);
		case TYPE_OBJECTID:
			return Type_type.TYPE_OBJECTID.equals(typeType2) || (!isAsn11 && Type_type.TYPE_ROID.equals(typeType2));
		case TYPE_ROID:
			return Type_type.TYPE_ROID.equals(typeType2) || (!isAsn12 && Type_type.TYPE_OBJECTID.equals(typeType2));
		case TYPE_TTCN3_ENUMERATED:
		case TYPE_ASN1_ENUMERATED:
			return Type_type.TYPE_TTCN3_ENUMERATED.equals(typeType2) || Type_type.TYPE_ASN1_ENUMERATED.equals(typeType2);
		case TYPE_TTCN3_CHOICE:
		case TYPE_ASN1_CHOICE:
		case TYPE_OPENTYPE:
			return Type_type.TYPE_TTCN3_CHOICE.equals(typeType2) || Type_type.TYPE_ASN1_CHOICE.equals(typeType2)
					|| Type_type.TYPE_OPENTYPE.equals(typeType2);
		case TYPE_TTCN3_SEQUENCE:
		case TYPE_ASN1_SEQUENCE:
			return Type_type.TYPE_TTCN3_SEQUENCE.equals(typeType2) || Type_type.TYPE_ASN1_SEQUENCE.equals(typeType2);
		case TYPE_TTCN3_SET:
		case TYPE_ASN1_SET:
			return Type_type.TYPE_TTCN3_SET.equals(typeType2) || Type_type.TYPE_ASN1_SET.equals(typeType2);
		case TYPE_ANY:
			return Type_type.TYPE_ANY.equals(typeType2) || Type_type.TYPE_OCTETSTRING.equals(typeType2);
		case TYPE_REFERENCED:
		case TYPE_OBJECTCLASSFIELDTYPE:
		case TYPE_ADDRESS:
			return false;
		default:
			return false;
		}
	}

	/**
	 * Handles the incremental parsing of this type.
	 *
	 * @param reparser
	 *                the parser doing the incremental parsing.
	 * @param isDamaged
	 *                true if the location contains the damaged area, false
	 *                if only its' location needs to be updated.
	 * */
	@Override
	/** {@inheritDoc} */
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}

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
	public Assignment getDefiningAssignment() {
		if(getMyScope() == null) {
			return null;
		}

		final Module module = getMyScope().getModuleScope();
		return module.getEnclosingAssignment(getLocation().getOffset());
	}

	@Override
	/** {@inheritDoc} */
	public void getEnclosingField(final int offset, final ReferenceFinder rf) {
	}

	@Override
	/** {@inheritDoc} */
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (constraints != null) {
			constraints.findReferences(referenceFinder, foundIdentifiers);
		}
		if (withAttributesPath != null) {
			withAttributesPath.findReferences(referenceFinder, foundIdentifiers);
		}
		if (parsedRestrictions != null) {
			for (final ParsedSubType parsedSubType : parsedRestrictions) {
				parsedSubType.findReferences(referenceFinder, foundIdentifiers);
			}
		}
	}

	@Override
	/** {@inheritDoc} */
	protected boolean memberAccept(final ASTVisitor v) {
		if (constraints != null && !constraints.accept(v)) {
			return false;
		}
		if (withAttributesPath != null && !withAttributesPath.accept(v)) {
			return false;
		}
		if (parsedRestrictions != null) {
			for (final ParsedSubType pst : parsedRestrictions) {
				if (!pst.accept(v)) {
					return false;
				}
			}
		}
		return true;
	}

	@Override
	/** {@inheritDoc} */
	public boolean getGenerateCoderFunctions(final MessageEncoding_type encodingType) {
		for (int i = 0; i < codersToGenerate.size(); i++) {
			if (encodingType == codersToGenerate.get(i)) {
				return true;
			}
		}

		switch (getTypetype()) {
		case TYPE_ASN1_SEQUENCE:
		case TYPE_TTCN3_SEQUENCE:
		case TYPE_ASN1_SET:
		case TYPE_TTCN3_SET:
		case TYPE_ASN1_CHOICE:
		case TYPE_TTCN3_CHOICE:
		case TYPE_ANYTYPE:
		case TYPE_OPENTYPE:
		case TYPE_SEQUENCE_OF:
		case TYPE_SET_OF:
		case TYPE_ARRAY:
		case TYPE_ASN1_ENUMERATED:
		case TYPE_TTCN3_ENUMERATED:
			// no 'encode' attribute for this type or any types that reference it, so
			// don't generate coder functions
			return false;
		default:
			// no need to generate coder functions for basic types, but this function
			// is also used to determine codec-specific descriptor generation
			final boolean canHave = canHaveCoding(CompilationTimeStamp.getBaseTimestamp(), encodingType);

			return canHave;
		}
	}

	@Override
	/** {@inheritDoc} */
	public void setGenerateCoderFunctions(final CompilationTimeStamp timestamp, final MessageEncoding_type encodingType) {
		//FIXME not supported other encoding
		switch(encodingType) {
		case RAW:
		case JSON:
	 // case BER:
	 // case XER:
	 // case TEXT:
	 // case OER:
			break;
		default:
			return;
		}

		if (getGenerateCoderFunctions(encodingType)) {
			//already set
			return;
		}

		codersToGenerate.add(encodingType);
	}

	/**
	 * Indicates if this type will generate its own class, or not.
	 *
	 * @param aData
	 *                only used to update imports if needed
	 * @param source
	 *                the source code to report error to.
	 * @return {@code true} if during code generation a class will be
	 *         generated for this class, {@code false otherwise}
	 * */
	public abstract boolean generatesOwnClass(final JavaGenData aData, final StringBuilder source );

	/**
	 * Add generated java code on this level.
	 * 
	 * @param aData
	 *                only used to update imports if needed
	 * @param source
	 *                the source code generated
	 */
	public abstract void generateCode( final JavaGenData aData, final StringBuilder source );

	/**
	 * Generates the type descriptor for this type.
	 *
	 * generate_code_typedescriptor in the compiler
	 *
	 * @param aData
	 *                only used to update imports if needed
	 * @param source
	 *                the source code generated
	 * @param localTarget
	 *                {@code null} if the code to be generated is to be
	 *                added to module level, {@code otherwise} the type
	 *                descriptors will be added to this StringBuilder.
	 * @param attributeRegistry
	 *                A hashmap (value-name pair) used to compress final
	 *                static coding attributes of complex types and their
	 *                fields.
	 * */
	public void generateCodeTypedescriptor(final JavaGenData aData, final StringBuilder source, final StringBuilder localTarget, final HashMap<String, String> attributeRegistry) {
		if (lastTimeTypeDescriptorGenerated != null && !lastTimeTypeDescriptorGenerated.isLess(aData.getBuildTimstamp())) {
			return;
		}

		lastTimeTypeDescriptorGenerated = aData.getBuildTimstamp();

		//FIXME implement: actually more complicated
		final String genname = getGenNameOwn();
		final String gennameTypeDescriptor = getGenNameTypeDescriptor(aData, source);
		/* genname{type,ber,raw,text,xer,json,oer}descriptor == gennameown is true if
		 * the type needs its own {type,ber,raw,text,xer,json}descriptor
		 * and can't use the descriptor of one of the built-in types.
		 */

		final IType last = getTypeRefdLast(CompilationTimeStamp.getBaseTimestamp());
		int codingsSupported = 0;
		//check and generate the needed type descriptors
		//FIXME implement: right now we assume RAW is allowed and needed for all types, just to create interfaces so that work on both sides can happen in parallel.
		final boolean generate_raw = aData.getEnableRaw() && last.getGenerateCoderFunctions(MessageEncoding_type.RAW);
		String gennameRawDescriptor;
		if (generate_raw && needsOwnRawDescriptor(aData)) {
			generateCodeRawDescriptor(aData, source, localTarget, attributeRegistry);
		}
		if (generate_raw) {
			gennameRawDescriptor = getGenNameRawDescriptor(aData, source);
			codingsSupported++;
		} else {
			gennameRawDescriptor = "null";
		}

		final boolean generate_json = aData.getEnableJson() && last.getGenerateCoderFunctions(MessageEncoding_type.JSON);
		String gennameJsonDescriptor;
		if (generate_json && needsOwnJsonDescriptor(aData)) {
			generateCodeJsonDescriptor(aData, source, localTarget, attributeRegistry);
		}
		if (generate_json) {
			gennameJsonDescriptor = getGenNameJsonDescriptor(aData, source);
			codingsSupported++;
		} else {
			gennameJsonDescriptor = "null";
		}

		aData.addBuiltinTypeImport("Base_Type.TTCN_Typedescriptor");

		final String descriptorName = MessageFormat.format("{0}_descr_", genname);
		if (localTarget == null && aData.hasGlobalVariable(descriptorName)) {
			return;
		}

		final StringBuilder globalVariable = new StringBuilder();
		globalVariable.append(MessageFormat.format("\tpublic static final TTCN_Typedescriptor {0}_descr_ = new TTCN_Typedescriptor(\"{1}\"", genname, getFullName()));
		//FIXME ASN BER has very limited support for now
		if (codingsSupported > 1) {
			globalVariable.append(", null");
		}

		if (generate_raw) {
			globalVariable.append(MessageFormat.format(", {0}", gennameRawDescriptor));
		} else if (codingsSupported > 1) {
			globalVariable.append(", null");
		}

		if (generate_json) {
			globalVariable.append(MessageFormat.format(", {0}", gennameJsonDescriptor));
		} else if (codingsSupported > 1) {
			globalVariable.append(", null");
		}

		switch (last.getTypetype()) {
		case TYPE_SEQUENCE_OF: {
			final StringBuilder preInit = aData.getPreInit();
			preInit.append(MessageFormat.format("{0}_descr_.oftype_descr = {1}_descr_;\n", gennameTypeDescriptor, ((SequenceOf_Type)last).getOfType().getGenNameTypeDescriptor(aData, source)));
			break;}
		case TYPE_SET_OF:{
			final StringBuilder preInit = aData.getPreInit();
			preInit.append(MessageFormat.format("{0}_descr_.oftype_descr = {1}_descr_;\n", gennameTypeDescriptor, ((SetOf_Type)last).getOfType().getGenNameTypeDescriptor(aData, source)));
			break;}
		default:
			break;
		}

		globalVariable.append(");\n");
		if (localTarget == null) {
			aData.addGlobalVariable(descriptorName, globalVariable.toString());
		} else {
			localTarget.append(globalVariable);
		}
	}

	/**
	 * Generates the raw descriptor for this type if it exists.
	 *
	 * generate_code_rawdescriptor in the compiler
	 *
	 * @param aData
	 *                only used to update imports if needed
	 * @param source
	 *                the source code generated
	 * @param localTarget
	 *                {@code null} if the code to be generated is to be
	 *                added to module level, {@code otherwise} the RAW
	 *                descriptors will be added to this StringBuilder.
	 * @param attributeRegistry
	 *                A hashmap (value-name pair) used to compress final
	 *                static coding attributes of complex types and their
	 *                fields.
	 * */
	private void generateCodeRawDescriptor(final JavaGenData aData, final StringBuilder source, final StringBuilder localTarget, final HashMap<String, String> attributeRegistry) {
		aData.addBuiltinTypeImport("RAW.TTCN_RAWdescriptor");
		aData.addBuiltinTypeImport("RAW.ext_bit_t");
		aData.addBuiltinTypeImport("RAW.raw_sign_t");
		aData.addBuiltinTypeImport("RAW.top_bit_order_t");
		aData.addBuiltinTypeImport("TTCN_EncDec.raw_order_t");
		aData.addBuiltinTypeImport("TitanCharString.CharCoding");

		final String genname = getGenNameOwn();
		final String descriptorName = MessageFormat.format("{0}_raw_", genname);
		final StringBuilder RAW_value = new StringBuilder();
		RAW_value.append(MessageFormat.format("new TTCN_RAWdescriptor(", genname));

		final boolean dummyRaw = rawAttribute == null;
		if (dummyRaw) {
			rawAttribute = new RawAST(getDefaultRawFieldLength());
		}
		if (rawAttribute.intX) {
			aData.addBuiltinTypeImport("RAW");

			RAW_value.append("RAW.RAW_INTX,");
		} else {
			RAW_value.append(rawAttribute.fieldlength).append(',');
		}
		if (rawAttribute.comp == RawAST.XDEFCOMPL) {
			RAW_value.append("raw_sign_t.SG_2COMPL,");
		} else if (rawAttribute.comp == RawAST.XDEFSIGNBIT) {
			RAW_value.append("raw_sign_t.SG_SG_BIT,");
		} else {
			RAW_value.append("raw_sign_t.SG_NO,");
		}
		if (rawAttribute.byteorder == RawAST.XDEFLAST) {
			RAW_value.append("raw_order_t.ORDER_MSB,");
		} else {
			RAW_value.append("raw_order_t.ORDER_LSB,");
		}
		if (getTypeRefdLast(CompilationTimeStamp.getBaseTimestamp()).getTypetype() == Type_type.TYPE_OCTETSTRING) {
			// the default alignment for octetstrings is 'left'
			if (rawAttribute.align == RawAST.XDEFRIGHT) {
				RAW_value.append("raw_order_t.ORDER_LSB,");
			} else {
				RAW_value.append("raw_order_t.ORDER_MSB,");
			}
		} else {
			// the default alignment for all other type is 'right'
			if (rawAttribute.align == RawAST.XDEFLEFT) {
				RAW_value.append("raw_order_t.ORDER_MSB,");
			} else {
				RAW_value.append("raw_order_t.ORDER_LSB,");
			}
		}
		if (rawAttribute.bitorderinfield == RawAST.XDEFMSB) {
			RAW_value.append("raw_order_t.ORDER_MSB,");
		} else {
			RAW_value.append("raw_order_t.ORDER_LSB,");
		}
		if (rawAttribute.bitorderinoctet == RawAST.XDEFMSB) {
			RAW_value.append("raw_order_t.ORDER_MSB,");
		} else {
			RAW_value.append("raw_order_t.ORDER_LSB,");
		}
		if (rawAttribute.extension_bit == RawAST.XDEFYES) {
			RAW_value.append("ext_bit_t.EXT_BIT_YES,");
		} else if (rawAttribute.extension_bit == RawAST.XDEFREVERSE) {
			RAW_value.append("ext_bit_t.EXT_BIT_REVERSE,");
		} else {
			RAW_value.append("ext_bit_t.EXT_BIT_NO,");
		}
		if (rawAttribute.hexorder == RawAST.XDEFHIGH) {
			RAW_value.append("raw_order_t.ORDER_MSB,");
		} else {
			RAW_value.append("raw_order_t.ORDER_LSB,");
		}
		if (rawAttribute.fieldorder == RawAST.XDEFMSB) {
			RAW_value.append("raw_order_t.ORDER_MSB,");
		} else {
			RAW_value.append("raw_order_t.ORDER_LSB,");
		}
		if (rawAttribute.toplevelind > 0) {
			if (rawAttribute.toplevel.bitorder == RawAST.XDEFLSB) {
				RAW_value.append("top_bit_order_t.TOP_BIT_LEFT,");
			} else {
				RAW_value.append("top_bit_order_t.TOP_BIT_RIGHT,");
			}
		} else {
			RAW_value.append("top_bit_order_t.TOP_BIT_INHERITED,");
		}
		RAW_value.append(rawAttribute.padding).append(',');
		RAW_value.append(rawAttribute.prepadding).append(',');
		RAW_value.append(rawAttribute.ptroffset).append(',');
		RAW_value.append(rawAttribute.unit).append(',');
		RAW_value.append(rawAttribute.padding_pattern_length).append(',');
		if (rawAttribute.padding_pattern_length > 0 && rawAttribute.padding_pattern != null) {
			RAW_value.append("new byte[] {");
			final String temp = Bit2OctExpression.bit2oct(rawAttribute.padding_pattern);
			boolean first = true;
			for (int i = temp.length() - 1; i > 0; i-=2) {
				if (first) {
					first = false;
				} else {
					RAW_value.append(", ");
				}
				RAW_value.append("(byte)0x").append(temp.charAt(i - 1)).append(temp.charAt(i));
			}
			RAW_value.append("},");
		} else {
			RAW_value.append("null,");
		}
		RAW_value.append(rawAttribute.length_restriction).append(',');
		if (rawAttribute.stringformat == CharCoding.UTF_8) {
			RAW_value.append("CharCoding.UTF_8");
		} else if (rawAttribute.stringformat == CharCoding.UTF16) {
			RAW_value.append("CharCoding.UTF16");
		} else {
			RAW_value.append("CharCoding.UNKNOWN");
		}

		RAW_value.append(',');
		if (rawAttribute.forceOmit.lists.size() == 0) {
			RAW_value.append(" null");
		} else {
			aData.addBuiltinTypeImport("RAW");

			final String force_omit_name = MessageFormat.format("{0}_raw_force_omit", genname);
			final StringBuilder globalVariables = new StringBuilder();

			globalVariables.append(MessageFormat.format("\tstatic final ArrayList<RAW.RAW_Field_List> {0}_lists = new ArrayList<RAW.RAW_Field_List>();\n", force_omit_name));
			globalVariables.append(MessageFormat.format("\tstatic final RAW.RAW_Force_Omit {0} = new RAW.RAW_Force_Omit({1}, {2}_raw_force_omit_lists);\n", force_omit_name, rawAttribute.forceOmit.lists.size(), genname));

			aData.addGlobalVariable(force_omit_name, globalVariables.toString());

			final StringBuilder preInit = aData.getPreInit();
			for (int i = 0; i < rawAttribute.forceOmit.lists.size(); i++) {
				final rawAST_field_list fieldList = rawAttribute.forceOmit.lists.get(i);
				IType t = getTypeRefdLast(CompilationTimeStamp.getBaseTimestamp());
				preInit.append(MessageFormat.format("{0}_lists.add(", force_omit_name));
				for (int j = 0; j < fieldList.names.size(); j++) {
					final Identifier name = fieldList.names.get(j);

					switch (t.getTypetype()) {
					case TYPE_TTCN3_CHOICE:
					case TYPE_TTCN3_SEQUENCE:
					case TYPE_TTCN3_SET: {
						final int index = ((TTCN3_Set_Seq_Choice_BaseType)t).getComponentIndexByName(name);
						t = ((TTCN3_Set_Seq_Choice_BaseType)t).getComponentByName(name.getName()).getType().getTypeRefdLast(CompilationTimeStamp.getBaseTimestamp());
						preInit.append(MessageFormat.format("new RAW.RAW_Field_List({0} ,", index));
						break;
					}
					case TYPE_ASN1_CHOICE:
					case TYPE_ASN1_SEQUENCE:
					case TYPE_ASN1_SET: {
						final int index = ((ASN1_Set_Seq_Choice_BaseType)t).getComponentIndexByName(name);
						t = ((ASN1_Set_Seq_Choice_BaseType)t).getComponentByName(name).getType().getTypeRefdLast(CompilationTimeStamp.getBaseTimestamp());
						preInit.append(MessageFormat.format("new RAW.RAW_Field_List({0} ,", index));
						break;
					}
					default:
						break;
					}
				}
				preInit.append("null");
				for (int j = 0; j < fieldList.names.size(); j++) {
					preInit.append(')');
				}
				preInit.append(");\n");
			}

			RAW_value.append(force_omit_name);
		}
		RAW_value.append(", ");
		RAW_value.append(rawAttribute.csn1lh ? "true" : "false");
		RAW_value.append(')');

		final StringBuilder globalVariable = new StringBuilder();
		final String raw_value_string = RAW_value.toString();
		if (attributeRegistry != null) {
			if (attributeRegistry.containsKey(raw_value_string)) {
				final String previousName = attributeRegistry.get(raw_value_string);
				globalVariable.append(MessageFormat.format("\tpublic static final TTCN_RAWdescriptor {0}_raw_ = {1};\n", genname, previousName));
			} else {
				attributeRegistry.put(raw_value_string, MessageFormat.format("{0}_raw_", genname));
				globalVariable.append(MessageFormat.format("\tpublic static final TTCN_RAWdescriptor {0}_raw_ = {1};\n", genname, raw_value_string));
			}
		} else {
			globalVariable.append(MessageFormat.format("\tpublic static final TTCN_RAWdescriptor {0}_raw_ = {1};\n", genname, raw_value_string));
		}

		if (localTarget == null) {
			aData.addGlobalVariable(descriptorName, globalVariable.toString());
		} else {
			localTarget.append(globalVariable);
		}

		if (dummyRaw) {
			rawAttribute = null;
		}
	}

	/**
	 * Generates the json descriptor for this type if it exists.
	 *
	 * generate_code_jsondescriptor in the compiler
	 *
	 * @param aData
	 *                only used to update imports if needed
	 * @param source
	 *                the source code generated
	 * @param localTarget
	 *                {@code null} if the code to be generated is to be
	 *                added to module level, {@code otherwise} the JSON
	 *                descriptors will be added to this StringBuilder.
	 * @param attributeRegistry
	 *                A hashmap (value-name pair) used to compress final
	 *                static coding attributes of complex types and their
	 *                fields.
	 * */
	protected void generateCodeJsonDescriptor(final JavaGenData aData, final StringBuilder source, final StringBuilder localTarget, final HashMap<String, String> attributeRegistry) {
		aData.addBuiltinTypeImport("JSON.TTCN_JSONdescriptor");
		aData.addBuiltinTypeImport("TitanCharString.CharCoding");

		final String genname = getGenNameOwn();
		final String descriptorName = MessageFormat.format("{0}_json_", genname);
		final StringBuilder JSON_value = new StringBuilder();

		JSON_value.append(MessageFormat.format("new TTCN_JSONdescriptor(", genname));

		final boolean as_map = (jsonAttribute != null && jsonAttribute.as_map) || 
				(ownerType == TypeOwner_type.OT_RECORD_OF && parentType.getJsonAttribute() != null && parentType.getJsonAttribute().as_map);

		if (jsonAttribute == null) { 
			JSON_value.append(MessageFormat.format("false, null, false, null, false, false, {0}, 0, null)", as_map ? "true": "false"));
		} else {
			String enum_texts_name = null;
			JSON_value.append(jsonAttribute.omit_as_null).append(',');
			if (jsonAttribute.alias != null) {
				JSON_value.append('\"').append(jsonAttribute.alias).append('\"').append(',');
			} else {
				JSON_value.append("null").append(',');
			}
			// FIXME: || jsonattrib->tag_list != NULL
			JSON_value.append(jsonAttribute.as_value).append(',');
			if (jsonAttribute.default_value == null) {
				JSON_value.append("null").append(',');
			} else {
				try {
					final byte[] bytes = jsonAttribute.default_value.getBytes("UTF-8");
					final StringBuilder utf8encoded = new StringBuilder();
					for (byte b : bytes) {
						utf8encoded.append((char)b);
					}
					JSON_value.append('\"').append(utf8encoded).append('\"');
				} catch (UnsupportedEncodingException e) {
					ErrorReporter.INTERNAL_ERROR("generateCodeJsonDescriptor(): unsupported charset");
					JSON_value.append("null");
				}

				if (jsonAttribute.actualDefaultValue != null) {
					if (jsonAttribute.actualDefaultValue.canGenerateSingleExpression() ) {
						final ExpressionStruct expression = new ExpressionStruct();
						jsonAttribute.actualDefaultValue.generateCodeExpressionMandatory(aData, expression, true);
						if (expression.preamble.length() > 0 || expression.postamble.length() > 0) {
							//FIXME hiba?
						} else {
							JSON_value.append("/*").append( expression.expression).append("*/");
						}
					} else {
						//FIXME hiba?
					}
				}

				JSON_value.append(',');
			}

			JSON_value.append(jsonAttribute.metainfo_unbound).append(',');
			JSON_value.append(jsonAttribute.as_number).append(',');
			JSON_value.append(as_map).append(',');
			JSON_value.append(jsonAttribute.enum_texts.size()).append(',');

			if (jsonAttribute.enum_texts.size() != 0) {
				aData.addBuiltinTypeImport("JSON.JsonEnumText");
				enum_texts_name = MessageFormat.format("{0}_json_enum_texts", genname);
				final StringBuilder enum_texts_value = new StringBuilder();
				enum_texts_value.append(MessageFormat.format("\t\tfinal static List<JsonEnumText> {0} = new ArrayList<JsonEnumText>();\n", enum_texts_name));
				enum_texts_value.append("\t\tstatic {\n");
				for (int i = 0; i < jsonAttribute.enum_texts.size(); i++) {
					final JsonEnumText element = jsonAttribute.enum_texts.get(i);
					enum_texts_value.append(MessageFormat.format("\t\t\t{0}.add(new JsonEnumText({1}, \"{2}\"));\n", enum_texts_name, element.index, element.to));
				}
				enum_texts_value.append("\t\t}\n");
				if (localTarget == null) {
					aData.addGlobalVariable(enum_texts_name, enum_texts_value.toString());
				} else {
					localTarget.append(enum_texts_value);
				}
			}
			JSON_value.append(enum_texts_name).append(")");
		}

		final StringBuilder globalVariable = new StringBuilder();
		if (attributeRegistry != null) {
			if (attributeRegistry.containsKey(JSON_value.toString())) {
				final String previousName = attributeRegistry.get(JSON_value.toString());
				globalVariable.append(MessageFormat.format("\tpublic static final TTCN_JSONdescriptor {0}_json_ = {1};\n", genname, previousName));
			} else {
				attributeRegistry.put(JSON_value.toString(), MessageFormat.format("{0}_json_", genname));
				globalVariable.append(MessageFormat.format("\tpublic static final TTCN_JSONdescriptor {0}_json_ = {1};\n", genname, JSON_value.toString()));
			}
		} else {
			globalVariable.append(MessageFormat.format("\tpublic static final TTCN_JSONdescriptor {0}_json_ = {1};\n", genname, JSON_value.toString()));
		}
		if (localTarget == null) {
			aData.addGlobalVariable(descriptorName, globalVariable.toString());
		} else {
			localTarget.append(globalVariable);
		}
	}

	/**
	 * Generates the default coding descriptor for the types
	 *
	 * part of generate_code_rawdescriptor in the compiler
	 *
	 * @param aData
	 *                only used to update imports if needed
	 * @param source
	 *                the source code generated
	 * @param localTarget
	 *                {@code null} if the code to be generated is to be
	 *                added to module level, {@code otherwise} the JSON
	 *                descriptors will be added to this StringBuilder.
	 * */
	public void generateCodeDefaultCoding(final JavaGenData aData, final StringBuilder source, final StringBuilder localTarget) {
		final String genname = getGenNameOwn();

		final IType t = getTypeWithCodingTable(CompilationTimeStamp.getBaseTimestamp(), false);
		if (t == null /*|| !genname.equals(getGenNameDefaultCoding(aData, source, myScope))*/) {
			return;
		}

		String defaultCoding = "";
		final List<Coding_Type> tempCodingTable = t.getCodingTable();
		if (tempCodingTable.size() == 0) {
			return;
		}

		if (tempCodingTable.size() == 1) {
			final Coding_Type tempCodingType = tempCodingTable.get(0);
			if (tempCodingType.builtIn) {
				defaultCoding = tempCodingType.builtInCoding == MessageEncoding_type.BER ? "BER:2002" : tempCodingType.builtInCoding.getEncodingName();
			} else {
				defaultCoding = tempCodingType.customCoding.name;
			}
		}

		aData.addBuiltinTypeImport("TitanUniversalCharString");

		final String globalVariable = MessageFormat.format("\tpublic static final TitanUniversalCharString {0}_default_coding = new TitanUniversalCharString(\"{1}\");\n", genname, defaultCoding);
		if (localTarget == null) {
			aData.addGlobalVariable(MessageFormat.format("{0}_default_coding", genname), globalVariable);
		} else {
			localTarget.append(globalVariable);
		}
	}

	/**
	 * Generates the coding handler functions for the types
	 *
	 * generate_code_rawdescriptor in the compiler
	 *
	 * @param aData
	 *                only used to update imports if needed
	 * @param source
	 *                the source code generated
	 * @param localTarget
	 *                {@code null} if the code to be generated is to be
	 *                added to module level, {@code otherwise} the coding
	 *                handlers will be added to this StringBuilder.
	 * */
	public void generateCodeForCodingHandlers(final JavaGenData aData, final StringBuilder source, final StringBuilder localTarget) {
		final String genname = getGenNameOwn();
		final IType t = getTypeWithCodingTable(CompilationTimeStamp.getBaseTimestamp(), false);
		if (t == null) {
			return;
		}

		final List<Coding_Type> tempCodingTable = t.getCodingTable();
		if (tempCodingTable.size() == 0) {
			return;
		}

		if (getGenNameCoder(aData, source, myScope).isEmpty() ) {
			return;
		}

		// encoder and decoder functions
		aData.addBuiltinTypeImport("TitanInteger");
		aData.addBuiltinTypeImport("TitanOctetString");
		aData.addBuiltinTypeImport("TitanUniversalCharString");
		aData.addCommonLibraryImport("TtcnError");
		aData.addCommonLibraryImport("TTCN_Logger");
		aData.addImport("java.text.MessageFormat");

		final StringBuilder encoderString = new StringBuilder();
		encoderString.append("\t/**\n");
		encoderString.append(MessageFormat.format("\t * The encoder function for type {0}.\n", getTypename()));
		encoderString.append("\t *\n");
		encoderString.append("\t * @param input_value\n");
		encoderString.append("\t *                the input value to encode.\n");
		encoderString.append("\t * @param output_stream\n");
		encoderString.append("\t *                the octetstring to be extend with the result of the\n");
		encoderString.append("\t *                encoding.\n");
		encoderString.append("\t * @param coding_name\n");
		encoderString.append("\t *                the name of the coding to use.\n");
		encoderString.append("\t * */\n");
		encoderString.append(MessageFormat.format("\tpublic static void {0}_encoder(final {1} input_value, final TitanOctetString output_stream, final TitanUniversalCharString coding_name) '{'\n", genname, getGenNameValue(aData, source)));

		final StringBuilder decoderString = new StringBuilder();
		decoderString.append("\t/**\n");
		decoderString.append(MessageFormat.format("\t * The decoder function for type {0}. In case\n", getTypename()));
		decoderString.append("\t * of successful decoding the bits used for decoding are removed from\n");
		decoderString.append("\t * the beginning of the input_stream.\n");
		decoderString.append("\t *\n");
		decoderString.append("\t * @param input_stream\n");
		decoderString.append("\t *                the octetstring starting with the value to be decoded.\n");
		decoderString.append("\t * @param output_value\n");
		decoderString.append("\t *                the decoded value if the decoding was successful.\n");
		decoderString.append("\t * @param coding_name\n");
		decoderString.append("\t *                the name of the coding to use.\n");
		decoderString.append("\t * @return 0 if nothing could be decoded, 1 in case of success, 2 in\n");
		decoderString.append("\t *         case of error (incomplete message or length)\n");
		decoderString.append("\t * */\n");
		decoderString.append(MessageFormat.format("\tpublic static int {0}_decoder(final TitanOctetString input_stream, final {1} output_value, final TitanUniversalCharString coding_name) '{'\n", genname, getGenNameValue(aData, source)));

		// user defined codecs
		for (int i = 0; i < tempCodingTable.size(); i++) {
			final Coding_Type tempCodingType = tempCodingTable.get(i);
			if (!tempCodingType.builtIn) {
				//encoder
				encoderString.append(MessageFormat.format("\t\tif (coding_name.operator_equals(\"{0}\")) '{'\n", tempCodingType.customCoding.name));
				final CoderFunction_Type encoderFunction = tempCodingType.customCoding.encoders.get(this);
				if (encoderFunction == null) {
					encoderString.append(MessageFormat.format("\t\t\tthrow new TtcnError(\"No `{0}'' encoding function defined for type `{1}''\");\n", tempCodingType.customCoding.name, getTypename()));
				} else {
					if (encoderFunction.conflict) {
						encoderString.append(MessageFormat.format("\t\t\tthrow new TtcnError(\"Multiple `{0}'' encoding function defined for type `{1}''\");\n", tempCodingType.customCoding.name, getTypename()));
					} else {
						aData.addCommonLibraryImport("AdditionalFunctions");

						encoderString.append(MessageFormat.format("\t\t\toutput_stream.operator_assign(AdditionalFunctions.bit2oct({0}(input_value)));\n", encoderFunction.functionDefinition.getGenNameFromScope(aData, source, "")));
					}
				}
				encoderString.append("\t\t}\n");

				// decoder
				decoderString.append(MessageFormat.format("\t\tif (coding_name.operator_equals(\"{0}\")) '{'\n", tempCodingType.customCoding.name));
				final CoderFunction_Type decoderFunction = tempCodingType.customCoding.decoders.get(this);
				if (decoderFunction == null) {
					decoderString.append(MessageFormat.format("\t\t\tthrow new TtcnError(\"No `{0}'' decoding function defined for type `{1}''\");\n", tempCodingType.customCoding.name, getTypename()));
				} else {
					if (decoderFunction.conflict) {
						decoderString.append(MessageFormat.format("\t\t\tthrow new TtcnError(\"Multiple `{0}'' decoding function defined for type `{1}''\");\n", tempCodingType.customCoding.name, getTypename()));
					} else {
						aData.addCommonLibraryImport("AdditionalFunctions");
						aData.addBuiltinTypeImport("TitanBitString");

						decoderString.append("\t\t\tfinal TitanBitString bit_stream = new TitanBitString(AdditionalFunctions.oct2bit(input_stream));\n");
						decoderString.append(MessageFormat.format("\t\t\tfinal TitanInteger ret_val = {0}(bit_stream, output_value);\n", decoderFunction.functionDefinition.getGenNameFromScope(aData, source, "")));
						decoderString.append("\t\t\tinput_stream.operator_assign(AdditionalFunctions.bit2oct(bit_stream));\n");
						decoderString.append("\t\t\treturn ret_val.get_int();\n");
					}
				}
				decoderString.append("\t\t}\n");
			}
		}

		// built-in codecs
		final StringBuilder checkString = new StringBuilder();
		for (int i = 0; i < tempCodingTable.size(); i++) {
			final Coding_Type tempCoding = tempCodingTable.get(i);
			if (tempCoding.builtIn) {
				if (checkString.length() > 0) {
					checkString.append(" && ");
				}
				checkString.append(MessageFormat.format("codingType != TTCN_EncDec.coding_type.CT_{0}", tempCoding.builtInCoding.getEncodingName()));
			}
		}

		if (checkString.length() > 0) {
			aData.addCommonLibraryImport("TTCN_EncDec");
			aData.addImport("java.util.concurrent.atomic.AtomicInteger");

			encoderString.append("\t\tfinal AtomicInteger extra_options = new AtomicInteger(0);\n");
			encoderString.append("\t\tfinal TTCN_EncDec.coding_type codingType = TTCN_EncDec.get_coding_from_str(coding_name, extra_options, true);\n");
			encoderString.append(MessageFormat.format("\t\tif ({0}) '{'\n", checkString));
		}
		encoderString.append("\t\tTTCN_Logger.begin_event_log2str();\n");
		encoderString.append("\t\tcoding_name.log();\n");
		encoderString.append(MessageFormat.format("\t\tthrow new TtcnError(MessageFormat.format(\"Type `{0}'' does not support '{'0'}' encoding\", TTCN_Logger.end_event_log2str()));\n", getTypename()));
		if (checkString.length() > 0) {
			aData.addCommonLibraryImport("TTCN_Buffer");

			encoderString.append("\t\t}\n");
			encoderString.append("\t\tfinal TTCN_Buffer ttcnBuffer = new TTCN_Buffer();\n");
			encoderString.append(MessageFormat.format("\t\tinput_value.encode({0}_descr_, ttcnBuffer, codingType, extra_options.get());\n", getGenNameTypeDescriptor(aData, source)));
			encoderString.append("\t\tttcnBuffer.get_string(output_stream);\n");
		}
		encoderString.append("\t}\n\n");

		if (checkString.length() > 0) {
			decoderString.append("\t\tfinal AtomicInteger extra_options = new AtomicInteger(0);\n");
			decoderString.append("\t\tfinal TTCN_EncDec.coding_type codingType = TTCN_EncDec.get_coding_from_str(coding_name, extra_options, false);\n");
			decoderString.append(MessageFormat.format("\t\tif ({0}) '{'\n", checkString));
		}
		decoderString.append("\t\tTTCN_Logger.begin_event_log2str();\n");
		decoderString.append("\t\tcoding_name.log();\n");
		decoderString.append(MessageFormat.format("\t\tthrow new TtcnError(MessageFormat.format(\"Type `{0}'' does not support '{'0'}' encoding\", TTCN_Logger.end_event_log2str()));\n", getTypename()));
		if (checkString.length() > 0) {
			decoderString.append("\t\t}\n");
			decoderString.append("\t\tfinal TTCN_Buffer ttcnBuffer = new TTCN_Buffer(input_stream);\n");
			decoderString.append(MessageFormat.format("\t\toutput_value.decode({0}_descr_, ttcnBuffer, codingType, extra_options.get());\n", getGenNameTypeDescriptor(aData, source)));
			decoderString.append("\t\tswitch (TTCN_EncDec.get_last_error_type()) {\n");
			decoderString.append("\t\tcase ET_NONE:\n");
			decoderString.append("\t\t\tttcnBuffer.cut();\n");
			decoderString.append("\t\t\tttcnBuffer.get_string(input_stream);\n");
			decoderString.append("\t\t\treturn 0;\n");
			decoderString.append("\t\tcase ET_INCOMPL_MSG:\n");
			decoderString.append("\t\tcase ET_LEN_ERR:\n");
			decoderString.append("\t\t\treturn 2;\n");
			decoderString.append("\t\tdefault:\n");
			decoderString.append("\t\t\treturn 1;\n");
			decoderString.append("\t\t}\n");
		}
		decoderString.append("\t}\n\n");

		if (localTarget == null) {
			source.append(encoderString);
			source.append(decoderString);
		} else {
			localTarget.append(encoderString);
			localTarget.append(decoderString);
		}
	}

	/**
	 * Returns true if the type supports at least one built-in encoding.
	 * Only used with new codec handling.
	 *
	 * @return true if the type supports at least one built-in encoding.
	 * */
	public boolean hasBuiltInEncoding() {
		final IType t = getTypeWithCodingTable(CompilationTimeStamp.getBaseTimestamp(), false);
		if (t == null) {
			return false;
		}

		final List<Coding_Type> codingTable = t.getCodingTable();
		for (int i = 0; i < codingTable.size(); i++) {
			if (codingTable.get(i).builtIn) {
				return true;
			}
		}

		return false;
	}

	/**
	 * @return whether the type needs an explicit Java alias and/or
	 * an alias to a type descriptor of another type. It returns true for those
	 * types that are defined in module-level type definitions hence are
	 * directly accessible by the users of Java API (in test ports, external
	 * functions)
	 * */
	protected boolean needsAlias() {
		//TODO find a way without using fullname and change in synch with compiler
		final String name = getFullName();
		final int firstDot = name.indexOf('.');
		if(firstDot == -1 || name.indexOf('.', firstDot + 1) == -1) {
			return true;
		}

		return false;
	}

	/**
	 * Creates and returns a string representation of this type, that can be
	 * used to calculate the alternative name of this type in an open type.
	 *
	 * create_stringRepr on the C side.
	 *
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 * @return the base name to start calculating from.
	 * */
	public String createStringRep_for_OpenType_AltName(final CompilationTimeStamp timestamp) {
		return getGenNameOwn();
	}

	/**
	 * Returns the name of the Java value class that represents this at runtime.
	 * The class is either pre-defined (written manually in the Base
	 * Library) or generated by the compiler.
	 * The reference is valid in the module that scope belongs to.
	 *
	 * get_genname_value in titan.core
	 *
	 * @param aData
	 *                only used to update imports if needed
	 * @param source
	 *                the source code generated
	 * @return The name of the Java value class in the generated code.
	 */
	public abstract String getGenNameValue(final JavaGenData aData, final StringBuilder source);

	/**
	 * Returns the name of the Java template class that represents this at runtime.
	 * The class is either pre-defined (written manually in the
	 * Base Library) or generated by the compiler. The reference is valid in
	 * the module that \a p_scope belongs to.
	 *
	 * get_genname_template in titan.core
	 *
	 * @param aData
	 *                only used to update imports if needed
	 * @param source
	 *                the source code generated
	 * @return The name of the Java value class in the generated code.
	 */
	public abstract String getGenNameTemplate(final JavaGenData aData, final StringBuilder source);

	/** Returns whether the type has the encoding attribute specified by
	 * the parameter. The function also checks the qualified attributes of
	 * parent types. Always returns true for ASN.1 types, when checking for a
	 * JSON encoding attribute.
	 */
	public boolean hasEncodeAttribute(final String encoding_name) {
		if ("JSON".equals(encoding_name) &&  isAsn()) {
			// ASN.1 types automatically support JSON encoding
			return true;
		}

		final WithAttributesPath attributePath = getAttributePath();
		if (attributePath != null) {
			final CompilationTimeStamp timestamp = CompilationTimeStamp.getBaseTimestamp() ;
			final List<SingleWithAttribute> realAttributes = attributePath.getRealAttributes(timestamp);
			for (int i = 0; i < realAttributes.size(); i++) {
				final SingleWithAttribute singleWithAttribute = realAttributes.get(i);
				if (SingleWithAttribute.Attribute_Type.Encode_Attribute.equals(singleWithAttribute.getAttributeType())) {
					if (singleWithAttribute.getAttributeSpecification().getSpecification().equals(encoding_name)) {
						return true;
					}
				}
			}
		}

		if ( (ownerType == TypeOwner_type.OT_COMP_FIELD || ownerType == TypeOwner_type.OT_RECORD_OF ||ownerType == TypeOwner_type.OT_ARRAY) &&
				parentType != null && parentType.getAttributePath() != null &&
				parentType.hasEncodeAttributeForType(this, encoding_name)) {
			return true;
		}

		return false;
	}

	/** Helper function for hasEncodeAttribute. Checks this type's qualified encoding
	 * attributes that refer to the specified type (target_type) and returns
	 * true if any of them match the specified encoding (encoding_name).
	 * Recursive function (calls the parent type's hasEncodeAttrForType function
	 * if no matching attributes are found).
	 */
	@Override
	public boolean hasEncodeAttributeForType(final Type type, final String encoding_name) {
		// if this type has an encode attribute, that also extends to its
		// fields/elements
		if (hasEncodeAttribute(encoding_name)) {
			return true;
		}
		// otherwise search this type's qualified attributes
		final MultipleWithAttributes multiWithAttributes = getAttributePath().getAttributes();
		if (multiWithAttributes != null) {
			for (int i = 0; i < multiWithAttributes.getNofElements(); i++) {
				final SingleWithAttribute singleWithAttribute = multiWithAttributes.getAttribute(i);
				if (SingleWithAttribute.Attribute_Type.Encode_Attribute.equals(singleWithAttribute.getAttributeType())
						&& singleWithAttribute.getAttributeSpecification().getSpecification().equals(encoding_name)) {
					// search the attribute's qualifiers for one that refers to the
					// target type
					final Qualifiers qualifiers = singleWithAttribute.getQualifiers();
					if (qualifiers != null) {
						for (int j = 0; j < qualifiers.getNofQualifiers(); j++) {
							final Qualifier qualifier = qualifiers.getQualifierByIndex(j);
							final List<ISubReference> fieldsOrArrays = new ArrayList<ISubReference>();
							for (int k = 0; k < qualifier.getNofSubReferences(); k++) {
								fieldsOrArrays.add(qualifier.getSubReferenceByIndex(k));
							}
							final Reference reference = new Reference(null, fieldsOrArrays);
							final IType typeQualifier = getFieldType(CompilationTimeStamp.getBaseTimestamp(), reference, 0, Expected_Value_type.EXPECTED_CONSTANT, false);
							if (typeQualifier == type) {
								return true;
							}
						}
					}

				}
			}
		}

		if ( (ownerType == TypeOwner_type.OT_COMP_FIELD || ownerType == TypeOwner_type.OT_RECORD_OF ||ownerType == TypeOwner_type.OT_ARRAY) &&
				parentType != null && parentType.getAttributePath() != null) {
			return parentType.hasEncodeAttributeForType(type, encoding_name);
		}

		return false;
	}

	/**
	 * Returns the name of the type descriptor (- the _descr_ postfix).
	 *
	 * get_genname_typedescriptor in titan.core
	 *
	 * @param aData
	 *                only used to update imports if needed
	 * @param source
	 *                the source code generated
	 * @return The name of the Java variable in the generated code.
	 */
	public abstract String getGenNameTypeDescriptor(final JavaGenData aData, final StringBuilder source);

	@Override
	/** {@inheritDoc} */
	public String getGenNameCoder(final JavaGenData aData, final StringBuilder source, final Scope scope) {
		final IType t_ct = getTypeWithCodingTable(CompilationTimeStamp.getBaseTimestamp(), false);
		if (t_ct == null) {
			return "";
		}

		// if the type has an 'encode' or 'variant' attribute, then it needs its own coder functions
		//TODO add support for more coders
		if (codingTable.size() > 0 || rawAttribute != null || jsonAttribute != null) {
			if (generatesOwnClass(aData, source)) {
				return getGenNameOwn(aData) + "." + getGenNameOwn();
			} else if (getParentType() != null) {
				final IType parentType = getParentType();
				if (parentType.generatesOwnClass(aData, source)) {
					return parentType.getGenNameOwn(aData) + "." + getGenNameOwn();
				}

				return getGenNameOwn(aData);
			}

			return getGenNameOwn(aData);
		}

		// if it has its own custom encoder or decoder functions set, then it needs its own coder functions
		final ArrayList<Coding_Type> ct_codingTable = ((Type)t_ct).codingTable;;
		for (int i = 0; i < ct_codingTable.size(); i++) {
			final Coding_Type tempCodingType = ct_codingTable.get(i);
			if (!tempCodingType.builtIn && (tempCodingType.customCoding.encoders.containsKey(this) ||
					tempCodingType.customCoding.decoders.containsKey(this))) {
				if (generatesOwnClass(aData, source)) {
					return getGenNameOwn(aData) + "." + getGenNameOwn();
				} else if (getParentType() != null) {
					final IType parentType = getParentType();
					if (parentType.generatesOwnClass(aData, source)) {
						return parentType.getGenNameOwn(aData) + "." + getGenNameOwn();
					}

					return getGenNameOwn(aData);
				}

				return getGenNameOwn(aData);
			}
		}

		if (this instanceof IReferencingType) {
			final IReferenceChain refChain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
			final IType t = ((IReferencingType) this).getTypeRefd(CompilationTimeStamp.getBaseTimestamp(), refChain);
			refChain.release();

			if (t != null && t != this) {
				return t.getGenNameCoder(aData, source, scope);
			}
		}

		return "";
	}

	@Override
	/** {@inheritDoc} */
	public String getGenNameDefaultCoding(final JavaGenData aData, final StringBuilder source, final Scope scope) {
		return getGenNameTypeDescriptor(aData, source);
		/*switch (ownerType) {
		case OT_TYPE_ASS:
		case OT_TYPE_DEF:
		case OT_ARRAY:
		case OT_COMP_FIELD:
		case OT_RECORD_OF:
			// types defined in TTCN-3 or ASN.1 code and their field and element types
			// have their own default coding variables
			return getGenNameOwn(aData);
		default:
			break;
		}

		if (this instanceof IReferencingType) {
			final IReferenceChain refChain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
			final IType t = ((IReferencingType) this).getTypeRefd(CompilationTimeStamp.getBaseTimestamp(), refChain);
			refChain.release();

			if (t != null && t != this) {
				return t.getGenNameDefaultCoding(aData, source, scope);
			}
		}

		return "";*/
	}

	/**
	 * Checks if the type needs its own RAW descriptor.
	 *
	 * @param aData
	 *                only used to update imports if needed
	 * @return {@code true} if the type needs its own RAW descriptor,
	 *         {@code false} otherwise.
	 */
	public boolean needsOwnRawDescriptor(final JavaGenData aData) {
		ErrorReporter.INTERNAL_ERROR("Trying to generate RAW for type `" + getFullName() + "'' that has no raw attributes");

		return false;
	}

	/**
	 * Returns the name of the RAW descriptor (- the _raw_ postfix).
	 *
	 * get_genname_rawdescriptor in titan.core
	 *
	 * @param aData
	 *                only used to update imports if needed
	 * @param source
	 *                the source code generated
	 * @return The name of the Java variable in the generated code.
	 */
	public String getGenNameRawDescriptor(final JavaGenData aData, final StringBuilder source) {
		ErrorReporter.INTERNAL_ERROR("Trying to generate RAW for type `" + getFullName() + "'' that has no raw attributes");

		return "FATAL_ERROR encountered while processing `" + getFullName() + "''\n";
	}

	/**
	 * Checks if the type needs its own JSON descriptor.
	 *
	 * @param aData
	 *                only used to update imports if needed
	 * @return {@code true} if the type needs its own JSON descriptor,
	 *         {@code false} otherwise.
	 */
	public boolean needsOwnJsonDescriptor(final JavaGenData aData) {
		//FIXME: temporally version
		//ErrorReporter.INTERNAL_ERROR("Trying to generate JSON for type `" + getFullName() + "'' that has no JSON attributes");

		return false;
	}

	public String getGenNameJsonDescriptor(final JavaGenData aData, final StringBuilder source) {
		//FIXME: temporally version
		//ErrorReporter.INTERNAL_ERROR("Trying to generate JSON for type `" + getFullName() + "'' that has no json attributes");

		return null;//"FATAL_ERROR encountered while processing `" + getFullName() + "''\n";
	}

	/**
	 * Returns the name prefix of type descriptors, etc. that belong to the
	 * equivalent Java class referenced from the module of scope \a p_scope.
	 * It differs from \a get_genname() only in case of special ASN.1 types
	 * like RELATIVE-OID or various string types.
	 *
	 * get_genname_value in titan.core
	 *
	 * @param aData
	 *                only used to update imports if needed
	 * @param source
	 *                the source code generated
	 * @return The name of the Java value class in the generated code.
	 */
	public String getGenNameTypeName(final JavaGenData aData, final StringBuilder source) {
		//FIXME implement everywhere
		return getGenNameValue(aData, source);
	}

	/**
	 * Generates the module level done statement for a type with the "done" attribute.
	 *
	 * generate_code_done in titan.core
	 *
	 * @param aData
	 *                only used to update imports if needed
	 * @param source
	 *                the source code generated
	 * */
	public void generateCodeDone(final JavaGenData aData, final StringBuilder source) {
		final String genName = getGenNameValue(aData, source);
		final String displayName = getTypename();

		aData.addImport("java.util.concurrent.atomic.AtomicReference");
		aData.addBuiltinTypeImport( "Base_Template.template_sel" );
		aData.addBuiltinTypeImport("TitanAlt_Status");
		aData.addCommonLibraryImport("Text_Buf");
		aData.addCommonLibraryImport("Index_Redirect");
		aData.addCommonLibraryImport("TTCN_Logger");
		aData.addCommonLibraryImport("TTCN_Runtime");
		aData.addBuiltinTypeImport("Value_Redirect_Interface");

		source.append(MessageFormat.format("\tpublic static final TitanAlt_Status done(final TitanComponent component_reference, final {0}_template value_template, final Value_Redirect_Interface value_redirect, final Index_Redirect index_redirect) '{'\n", genName));
		source.append("\t\tif (!component_reference.is_bound()) {\n");
		source.append("\t\t\tthrow new TtcnError(\"Performing a done operation on an unbound component reference.\");\n");
		source.append("\t\t}\n");
		source.append("\t\tif (value_template.get_selection() == template_sel.ANY_OR_OMIT) {\n");
		source.append("\t\t\tthrow new TtcnError(\"Done operation using '*' as matching template\");\n");
		source.append("\t\t}\n");

		source.append("\t\tfinal Text_Buf text_buf = new Text_Buf();\n");
		source.append("\t\tfinal AtomicReference<Text_Buf> text_buf_ref = new AtomicReference<Text_Buf>(text_buf);\n");
		source.append(MessageFormat.format("\t\tfinal TitanAlt_Status ret_val = TTCN_Runtime.component_done(component_reference.get_component(), \"{0}\", text_buf_ref);\n", displayName));
		source.append("\t\tif (ret_val == TitanAlt_Status.ALT_YES) {\n");
		source.append(MessageFormat.format("\t\t\tfinal {0} return_value = new {0}();\n", genName));
		source.append("\t\t\treturn_value.decode_text(text_buf_ref.get());\n");
		source.append("\t\t\tif (value_template.match(return_value)) {\n");
		source.append("\t\t\t\tif (value_redirect != null) {\n");
		source.append("\t\t\t\t\tvalue_redirect.set_values(return_value);\n");
		source.append("\t\t\t\t}\n");
		source.append("\t\t\t\tTTCN_Logger.begin_event(TTCN_Logger.Severity.PARALLEL_PTC);\n");
		source.append("\t\t\t\tTTCN_Logger.log_event_str(\"PTC with component reference \");\n");
		source.append("\t\t\t\tcomponent_reference.log();\n");
		source.append(MessageFormat.format("\t\t\t\tTTCN_Logger.log_event_str(\" is done. Return value: {0} : \");\n", displayName));
		source.append("\t\t\t\treturn_value.log();\n");
		source.append("\t\t\t\tTTCN_Logger.end_event();\n");
		source.append("\t\t\t\treturn TitanAlt_Status.ALT_YES;\n");
		source.append("\t\t\t} else {\n");
		source.append("\t\t\t\tif (TTCN_Logger.log_this_event(TTCN_Logger.Severity.MATCHING_DONE)) {\n");
		source.append("\t\t\t\t\tTTCN_Logger.begin_event(TTCN_Logger.Severity.MATCHING_DONE);\n");
		source.append(MessageFormat.format("\t\t\t\t\tTTCN_Logger.log_event_str(\"Done operation with type {0} on component reference \");\n", displayName));
		source.append("\t\t\t\t\tcomponent_reference.log();\n");
		source.append("\t\t\t\t\tTTCN_Logger.log_event_str(\" failed: Return value does not match the template: \");\n");
		source.append("\t\t\t\t\tvalue_template.log_match(return_value, true);\n");
		source.append("\t\t\t\t\tTTCN_Logger.end_event();\n");
		source.append("\t\t\t\t}\n");
		source.append("\t\t\t\treturn TitanAlt_Status.ALT_NO;\n");
		source.append("\t\t\t}\n");
		source.append("\t\t} else {\n");
		source.append("\t\t\treturn ret_val;\n");
		source.append("\t\t}\n");
		source.append("\t}\n");

		if (needs_any_from_done) {
			source.append(MessageFormat.format("\tpublic static TitanAlt_Status done(final TitanValue_Array<TitanComponent> component_array, final {0}_template value_template, final Value_Redirect_Interface value_redirect, final Index_Redirect index_redirect) '{'\n", genName));
			source.append("\t\tif (index_redirect != null) {\n");
			source.append("\t\t\tindex_redirect.incr_pos();\n");
			source.append("\t\t}\n");

			source.append("\t\tTitanAlt_Status result = TitanAlt_Status.ALT_NO;\n");
			source.append("\t\tfor (int i = 0; i < component_array.n_elem(); i++) {\n");
			source.append("\t\t\tfinal TitanAlt_Status ret_val = done((TitanComponent)component_array.get_at(i), value_template, value_redirect, index_redirect);\n");
			source.append("\t\t\tif (ret_val == TitanAlt_Status.ALT_YES) {\n");
			source.append("\t\t\t\tif (index_redirect != null) {\n");
			source.append("\t\t\t\t\tindex_redirect.add_index(i + component_array.get_offset());\n");
			source.append("\t\t\t\t}\n");
			source.append("\t\t\t\tresult = ret_val;\n");
			source.append("\t\t\t\tbreak;\n");
			source.append("\t\t\t} else if (ret_val == TitanAlt_Status.ALT_REPEAT || (ret_val == TitanAlt_Status.ALT_MAYBE && result == TitanAlt_Status.ALT_NO)) {\n");
			source.append("\t\t\t\tresult = ret_val;\n");
			source.append("\t\t\t}\n");
			source.append("\t\t}\n");
			source.append("\t\tif (index_redirect != null) {\n");
			source.append("\t\t\tindex_redirect.decr_pos();\n");
			source.append("\t\t}\n");

			source.append("\t\treturn result;\n");
			source.append("\t}\n");
		}
	}

	@Override
	/** {@inheritDoc} */
	public int getJsonValueType() {
		final IType t = getTypeRefdLast(CompilationTimeStamp.getBaseTimestamp());
		final Type_type tt = t.getTypetype();
		switch(tt) {
		case TYPE_INTEGER:
		case TYPE_INTEGER_A:
			return JSON_NUMBER;
		case TYPE_REAL:
			return JSON_NUMBER | JSON_STRING;
		case TYPE_BOOL:
			return JSON_BOOLEAN;
		case TYPE_NULL:
			return JSON_NULL;
		case TYPE_BITSTRING:
		case TYPE_BITSTRING_A:
		case TYPE_HEXSTRING:
		case TYPE_OCTETSTRING:
		case TYPE_CHARSTRING:
		case TYPE_UCHARSTRING:
		case TYPE_UTF8STRING:
		case TYPE_NUMERICSTRING:
		case TYPE_PRINTABLESTRING:
		case TYPE_TELETEXSTRING:
		case TYPE_VIDEOTEXSTRING:
		case TYPE_IA5STRING:
		case TYPE_GRAPHICSTRING:
		case TYPE_VISIBLESTRING:
		case TYPE_GENERALSTRING:  
		case TYPE_UNIVERSALSTRING:
		case TYPE_BMPSTRING:
		case TYPE_VERDICT:
		case TYPE_TTCN3_ENUMERATED:
		case TYPE_ASN1_ENUMERATED:
		case TYPE_OBJECTID:
		case TYPE_ROID:
		case TYPE_ANY:
			return JSON_STRING;
		case TYPE_TTCN3_SEQUENCE:
		case TYPE_TTCN3_SET:
			if (t instanceof TTCN3_Set_Seq_Choice_BaseType) {
				final TTCN3_Set_Seq_Choice_BaseType bt = (TTCN3_Set_Seq_Choice_BaseType)t;
				if (bt.getNofComponents() > 1) {
					return JSON_OBJECT;
				} else {
					return JSON_ANY_VALUE;
				}
			} else {
				ErrorReporter.INTERNAL_ERROR("getJsonValueType(): invalid TTCN3 set or sequence type " + tt.name());
			}
		case TYPE_ASN1_SEQUENCE:
		case TYPE_ASN1_SET:
			if (t instanceof ASN1_Set_Seq_Choice_BaseType) {
				final ASN1_Set_Seq_Choice_BaseType bt = (ASN1_Set_Seq_Choice_BaseType)t;
				if (bt.getNofComponents() > 1) {
					return JSON_OBJECT;
				} else {
					return JSON_ANY_VALUE;
				}
			} else {
				ErrorReporter.INTERNAL_ERROR("getJsonValueType(): invalid ASN1 set or sequence type " + tt.name());
			}
		case TYPE_TTCN3_CHOICE:
		case TYPE_ASN1_CHOICE:
		case TYPE_ANYTYPE:
		case TYPE_OPENTYPE:
			return JSON_ANY_VALUE;
		case TYPE_SEQUENCE_OF:
		case TYPE_SET_OF:
		case TYPE_ARRAY:
			return JSON_ARRAY | JSON_OBJECT;
		default:
			ErrorReporter.INTERNAL_ERROR("getJsonValueType(): invalid field type " + tt.name());
		}
		return 0;
	}

	/**
	 * Generates type specific call for the reference used in isbound call
	 * into argument expression. Argument \a subrefs holds the reference
	 * path that needs to be checked. Argument \a module is the actual
	 * module of the reference and is used to gain access to temporal
	 * identifiers.
	 *
	 * generate_code_ispresentbound in the compiler
	 *
	 * @param aData
	 *                only used to update imports if needed
	 * @param expression
	 *                the expression for code generation
	 * @param subreferences
	 *                the subreference to process
	 * @param subReferenceIndex
	 *                the index telling which part of the subreference to
	 *                process
	 * @param globalId
	 *                is the name of the bool variable where the result of
	 *                the isbound check is calculated.
	 * @param externalId
	 *                is the name of the assignment where the call chain
	 *                starts.
	 * @param isTemplate
	 *                is_template tells if the assignment is a template or
	 *                not.
	 * @param optype
	 *                tells if the function is isbound or ispresent.
	 * @param field
	 *                the expression to be used to reach the last union's
	 *                field, or null.
	 * */
	public void generateCodeIsPresentBoundChosen(final JavaGenData aData, final ExpressionStruct expression, final List<ISubReference> subreferences, final int subReferenceIndex, final String globalId, final String externalId, final boolean isTemplate, final Operation_type optype, final String field, final Scope targetScope) {
		if (subreferences == null || getIsErroneous(CompilationTimeStamp.getBaseTimestamp())) {
			return;
		}

		if (subReferenceIndex >= subreferences.size()) {
			return;
		}

		//FIXME implement
		expression.expression.append( "//TODO: " );
		expression.expression.append( getClass().getSimpleName() );
		expression.expression.append( ".generateCodeIspresentBound() is not be implemented yet!\n" );
	}

	/**
	 * Generates type specific call for the reference used in isbound call
	 * into argument expression. Argument \a subrefs holds the reference
	 * path that needs to be checked. Argument \a module is the actual
	 * module of the reference and is used to gain access to temporal
	 * identifiers.
	 *
	 * This version should only be called from string types. It only serves
	 * to save from copy-paste -ing the same code to every string type
	 * class. generate_code_ispresentbound in the compiler
	 *
	 * @param aData
	 *                only used to update imports if needed
	 * @param expression
	 *                the expression for code generation
	 * @param subreferences
	 *                the subreference to process
	 * @param subReferenceIndex
	 *                the index telling which part of the subreference to
	 *                process
	 * @param globalId
	 *                is the name of the bool variable where the result of
	 *                the isbound check is calculated.
	 * @param externalId
	 *                is the name of the assignment where the call chain
	 *                starts.
	 * @param isTemplate
	 *                is_template tells if the assignment is a template or
	 *                not.
	 * @param field
	 *                the expression to be used to reach the last union's
	 *                field, or null.
	 * @param targetScope
	 *                the scope to generate the code for.
	 * @param isBound
	 *                tells if the function is isbound or ispresent.
	 * */
	protected void generateCodeIspresentBound_forStrings(final JavaGenData aData, final ExpressionStruct expression, final List<ISubReference> subreferences, final int subReferenceIndex, final String globalId, final String externalId, final boolean isTemplate, final Operation_type optype, final String field, final Scope targetScope) {
		if (subreferences == null || getIsErroneous(CompilationTimeStamp.getBaseTimestamp())) {
			return;
		}

		if (subReferenceIndex >= subreferences.size()) {
			return;
		}

		if(isTemplate) {
			//FIXME handle template
			expression.expression.append( "//TODO: template handling in" );
			expression.expression.append( getClass().getSimpleName() );
			expression.expression.append( ".generateCodeIspresentBound() is not be implemented yet!\n" );
		}

		final ISubReference subReference = subreferences.get(subReferenceIndex);
		if (!(subReference instanceof ArraySubReference)) {
			ErrorReporter.INTERNAL_ERROR("Code generator reached erroneous type reference `" + getFullName() + "''");
			expression.expression.append("FATAL_ERROR encountered while processing `" + getFullName() + "''\n");
			return;
		}

		final Value indexValue = ((ArraySubReference) subReference).getValue();
		final IReferenceChain referenceChain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
		final IValue last = indexValue.getValueRefdLast(CompilationTimeStamp.getBaseTimestamp(), referenceChain);
		referenceChain.release();

		final String temporalIndexId = aData.getTemporaryVariableName();
		expression.expression.append(MessageFormat.format("if({0}) '{'\n", globalId));
		expression.expression.append(MessageFormat.format("final TitanInteger {0} = ", temporalIndexId));
		last.generateCodeExpressionMandatory(aData, expression, true);
		expression.expression.append(";\n");
		expression.expression.append(MessageFormat.format("{0} = {1}.is_greater_than_or_equal(0) && {1}.is_less_than({2}.lengthof());\n",
				globalId, temporalIndexId, externalId));

		final String temporalId = aData.getTemporaryVariableName();
		final boolean isLast = subReferenceIndex == (subreferences.size() - 1);
		expression.expression.append(MessageFormat.format("if({0}) '{'\n", globalId));

		if (optype == Operation_type.ISBOUND_OPERATION) {
			expression.expression.append(MessageFormat.format("{0} = {1}.constGet_at({2}).is_bound();\n",
					globalId, externalId, temporalIndexId));
		} else if (optype == Operation_type.ISPRESENT_OPERATION) {
			expression.expression.append(MessageFormat.format("{0} = {1}.constGet_at({2}).{3}({4});\n",
					globalId, externalId, temporalIndexId, (!isLast)?"is_bound":"is_present", isLast && isTemplate && aData.getAllowOmitInValueList()?"true":""));
		}

		generateCodeIsPresentBoundChosen(aData, expression, subreferences, subReferenceIndex + 1, globalId, temporalId, isTemplate, optype, field, targetScope);

		expression.expression.append("}\n}\n");
	}

	/**
	 * Helper function used in generateCodeIspresentbound() for the
	 * ispresent() function in case of template parameter.
	 *
	 * @param expression
	 *                the expression to generate code to (error messages if
	 *                needed).
	 * @param subreferences
	 *                the subreferences to check.
	 * @param beginIndex
	 *                the index at which index the checking of subreferences
	 *                should start.
	 * @return true if the referenced field which is embedded into a "?" is
	 *         always present, otherwise returns false.
	 * */
	public boolean isPresentAnyvalueEmbeddedField(final ExpressionStruct expression, final List<ISubReference> subreferences, final int beginIndex) {
		return true;
	}

	/** Set the owner and its type type */
	public void setOwnertype(final TypeOwner_type ownerType, final INamedNode owner) {
		this.ownerType = ownerType;
		this.owner = owner;
	}

	public TypeOwner_type getOwnertype() {
		return ownerType;
	}

	public INamedNode getOwner() {
		return owner;
	}

	/**
	 * Indicates that the type needs to have any from done support.
	 * */
	public void set_needs_any_from_done() {
		needs_any_from_done = true;
	}

	/**
	 * Calculate and return the name of the function that would be generated
	 * when converting from one type to the other.
	 *
	 * @param aData
	 *                used to access the build context.
	 * @param from
	 *                the type to convert from.
	 * @param to
	 *                the type to convert to.
	 * @param forValue
	 *                generate name for value conversion.
	 * @param source
	 *                the StringBuilder where errors can be reported to.
	 * @return the name of the function.
	 * */
	public static String getConversionFunction(final JavaGenData aData, final IType from, final IType to, final boolean forValue, final StringBuilder source) {
		final String fromName = from.getGenNameValue( aData, source );
		final String toName = to.getGenNameValue( aData, source );
		final StringBuilder returnValue = new StringBuilder();
		returnValue.append("conv_").append(fromName.replace('.', '_')).append('_').append(toName.replace('.', '_'));
		if (!forValue) {
			returnValue.append("_t");
		}

		return returnValue.toString();
	}

	@Override
	public String generateConversion(final JavaGenData aData, final IType fromType, final String fromName, final boolean forValue, final ExpressionStruct expression) {
		// the default implementation does nothing
		return fromName;
	}
}
