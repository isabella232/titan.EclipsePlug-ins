/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.eclipse.titan.designer.AST.Type.CompatibilityLevel;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.attributes.MultipleWithAttributes;
import org.eclipse.titan.designer.AST.TTCN3.attributes.RawAST;
import org.eclipse.titan.designer.AST.TTCN3.attributes.SingleWithAttribute;
import org.eclipse.titan.designer.AST.TTCN3.attributes.WithAttributesPath;
import org.eclipse.titan.designer.AST.TTCN3.attributes.SingleWithAttribute.Attribute_Modifier_type;
import org.eclipse.titan.designer.AST.TTCN3.templates.ITTCN3Template;
import org.eclipse.titan.designer.AST.TTCN3.types.subtypes.ParsedSubType;
import org.eclipse.titan.designer.AST.TTCN3.types.subtypes.SubType;
import org.eclipse.titan.designer.AST.TTCN3.values.Expression_Value.Operation_type;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.ExpressionStruct;
import org.eclipse.titan.designer.compiler.BuildTimestamp;
import org.eclipse.titan.designer.compiler.JavaGenData;
import org.eclipse.titan.designer.editors.ProposalCollector;
import org.eclipse.titan.designer.editors.actions.DeclarationCollector;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * @author Kristof Szabados
 * */
public interface IType extends IGovernor, IIdentifierContainer, IVisitableNode, IReferenceChainElement {

	public enum Type_type {
		// special values never instantiated
		/** undefined. */
		TYPE_UNDEFINED,

		// common types (they reside among the TTCN-3 type package)
		/** boolean. */
		TYPE_BOOL,
		/** bitstring. */
		TYPE_BITSTRING,
		/** octetstring. */
		TYPE_OCTETSTRING,
		/** integer. */
		TYPE_INTEGER,
		/** real / float. */
		TYPE_REAL,
		/** object identifier. */
		TYPE_OBJECTID,
		/** referenced. */
		TYPE_REFERENCED,
		/** sequence of. */
		TYPE_SEQUENCE_OF,
		/** set of. */
		TYPE_SET_OF,

		// TTCN-3 types
		/** character string (TTCN-3). */
		TYPE_CHARSTRING,
		/** hexadecimal string (TTCN-3). */
		TYPE_HEXSTRING,
		/** unversal charstring (TTCN-3). */
		TYPE_UCHARSTRING,
		/** verdict type (TTCN-3). */
		TYPE_VERDICT,
		/** address (TTCN-3). */
		TYPE_ADDRESS,
		/** default (TTCN-3). */
		TYPE_DEFAULT,
		/** Sequence (TTCN-3). */
		TYPE_TTCN3_SEQUENCE,
		/** Set (TTCN-3). */
		TYPE_TTCN3_SET,
		/** Union (TTCN-3). */
		TYPE_TTCN3_CHOICE,
		/** enumeration (TTCN-3). */
		TYPE_TTCN3_ENUMERATED,
		/** function (TTCN-3). */
		TYPE_FUNCTION,
		/** altstep (TTCN-3). */
		TYPE_ALTSTEP,
		/** testcase (TTCN-3). */
		TYPE_TESTCASE,
		/** array (TTCN-3). */
		TYPE_ARRAY,
		/** signature (TTCN-3). */
		TYPE_SIGNATURE,
		/** component (TTCN-3). */
		TYPE_COMPONENT,
		/** port (TTCN-3). */
		TYPE_PORT,
		/** anytype (TTCN-3). */
		TYPE_ANYTYPE,

		// ASN.1 types
		/** null type (ASN.1). */
		TYPE_NULL,
		/** Integer type (ASN.1). */
		TYPE_INTEGER_A,
		/** enumeration (ASN.1). */
		TYPE_ASN1_ENUMERATED,
		/** bitstring (ASN.1). */
		TYPE_BITSTRING_A,
		/** UTF8String (ASN.1). */
		TYPE_UTF8STRING,
		/** numericString (ASN.1). */
		TYPE_NUMERICSTRING,
		/** printablestring (ASN.1). */
		TYPE_PRINTABLESTRING,
		/** teletexstring (ASN.1). */
		TYPE_TELETEXSTRING,
		/** videotexstring (ASN.1). */
		TYPE_VIDEOTEXSTRING,
		/** IA5String (ASN.1). */
		TYPE_IA5STRING,
		/** graphicstring (ASN.1). */
		TYPE_GRAPHICSTRING,
		/** visiblestring (ASN.1). */
		TYPE_VISIBLESTRING,
		/** generalString (ASN.1). */
		TYPE_GENERALSTRING,
		/** universalString (ASN.1). */
		TYPE_UNIVERSALSTRING,
		/** bmpString (ASN.1). */
		TYPE_BMPSTRING,
		/** unrestrictedString (ASN.1). */
		TYPE_UNRESTRICTEDSTRING,
		/** UTCtime (ASN.1). */
		TYPE_UTCTIME,
		/** generalised time (ASN.1). */
		TYPE_GENERALIZEDTIME,
		/** objectdescriptor. */
		TYPE_OBJECTDESCRIPTOR,
		/** relative object identifier (ASN.1). */
		TYPE_ROID,
		/** choice (ASN.1). */
		TYPE_ASN1_CHOICE,
		/** sequence (ASN.1). */
		TYPE_ASN1_SEQUENCE,
		/** set (ASN.1). */
		TYPE_ASN1_SET,
		/** ObjectClassField (ASN.1). */
		TYPE_OBJECTCLASSFIELDTYPE,
		/** opentype (ASN.1). */
		TYPE_OPENTYPE,
		/** ANY (ASN.1). */
		TYPE_ANY,
		/** external (ASN.1). */
		TYPE_EXTERNAL,
		/** embedded_pdv (ASN.1). */
		TYPE_EMBEDDED_PDV,
		/** selection (ASN.1). */
		TYPE_SELECTION
	}

	/**
	 * Encoding types.
	 * 
	 * TODO only RAW is supported for now
	 * */
	public enum MessageEncoding_type {
		/** not yet defined */
		UNDEFINED("<unknown encoding>"),
		/** ASN.1 BER encoding (built-in) */
		BER("BER"),
		/** ASN.1 PER encoding (through user defined coder functions) */
		PER("PER"),
		/** ASN.1 OER (built-in) */
		OER("OER"),
		/** raw encoding (built-in) */
		RAW("RAW"),
		/** text encoding (built-in) */
		TEXT("TEXT"),
		/** xer encoding (built-in) */
		XER("XER"),
		/** json encoding (built-in) */
		JSON("JSON"),
		/** custom encoding (through user defined coder functions) */
		CUSTOM("CUSTOM");

		private String name;

		MessageEncoding_type(final String name) {
			this.name = name;
		}

		public String getEncodingName() {
			return name;
		}
	}

	/**
	 *  Enumeration to represent the owner of the type.
	 */
	enum TypeOwner_type {
		OT_UNKNOWN,
		/** ASN.1 type assignment (Ass_T) */
		OT_TYPE_ASS,
		/** ASN.1 variable assignment (Ass_V) */
		OT_VAR_ASS,
		/** ASN.1 value set assignment (Ass_VS) */
		OT_VSET_ASS,
		/** ASN.1 TypeFieldSpec (FieldSpec_T) */
		OT_TYPE_FLD,
		/** ASN.1 FixedTypeValueFieldSpec (FieldSpec_V_FT) */
		OT_FT_V_FLD,
		/** TTCN-3 TypeMapping */
		OT_TYPE_MAP,
		/** TTCN-3 TypeMappingTarget */
		OT_TYPE_MAP_TARGET,
		/** TTCN-3 type definition (Def_Type) */
		OT_TYPE_DEF,
		/** TTCN-3 constant definition (DefConst, Def_ExtCOnst) */
		OT_CONST_DEF,
		/** TTCN-3 module parameter definition (Def_Modulepar) */
		OT_MODPAR_DEF,
		/** TTCN-3 variable definition (Def_Var) */
		OT_VAR_DEF,
		/** TTCN-3 var template definition (Def_Var_Template) */
		OT_VARTMPL_DEF,
		/** TTCN-3 function (Def_Function, Def_ExtFunction) */
		OT_FUNCTION_DEF,
		/** TTCN-3 template definition (Def_Template) */
		OT_TEMPLATE_DEF,
		/** another Type: TTCN-3 array(T_ARRAY) */
		OT_ARRAY,
		/** another Type (T_SEQOF, T_SETOF), ASN.1 or TTCN-3 */
		OT_RECORD_OF,
		/** another Type: TTCN-3 function (T_FUNCTION) */
		OT_FUNCTION,
		/** another Type: TTCN-3 signature (T_SIGNATURE) */
		OT_SIGNATURE,
		/** another Type (T_REFD) */
		OT_REF,
		/** another Type (T_REFDSPEC) */
		OT_REF_SPEC,
		/** a field of a record/set/union (CompField) */
		OT_COMP_FIELD,
		/** ASN.1 "COMPONENTS OF" (CT_CompsOf) */
		OT_COMPS_OF,
		/** formal parameter (FormalPar), TTCN-3 */
		OT_FORMAL_PAR,
		/** TypeList for a 'with "extension anytype t1,t2..." ' */
		OT_TYPE_LIST,
		/** ASN.1 FieldSetting_Type */
		OT_FIELDSETTING,
		/** another Type (T_SELTYPE), ASN.1 selection type */
		OT_SELTYPE,
		/** another Type (T_OCFT), ASN.1 obj.class field type */
		OT_OCFT,
		/** a TemplateInstance (TTCN-3) */
		OT_TEMPLATE_INST,
		/** a RunsOnScope (TTCN-3) */
		OT_RUNSON_SCOPE,
		/** a port scope */
		OT_PORT_SCOPE,
		/** exception Specification (ExcSpec) */
		OT_EXC_SPEC,
		/** signature parameter (SignatureParam) */
		OT_SIG_PAR//,
		// OT_POOL no pool type is used here
	};

	/** Stores information about the custom encoder or decoder function of a type */
	public static class CoderFunction_Type {
		/** definition of the encoder or decoder function */
		Assignment functionDefinition;
		/** indicates whether there are multiple encoder/decoder functions for this type and codec */
		boolean conflict;
	}

	/**
	 * Stores information related to an encoding type (codec), when using new codec handling.
	 * */
	public static class Coding_Type {
		/** built-in or user defined codec */
		public boolean builtIn;
		/** the 'encode' attribute's modifier */
		public Attribute_Modifier_type modifier;
		/** built-in codec ,when builtIn is true */
		public MessageEncoding_type builtInCoding;

		/** custom codec fields, when builtIn is false */
		public static class CustomCoding_type {
			/** name of the user defined codec (the string in the 'encode' attribute) */
			public String name;

			/** the map of encoder functions per type */
			public HashMap<IType, CoderFunction_Type> encoders;
			/** the map of decoder functions per type */
			public HashMap<IType, CoderFunction_Type> decoders;
		}

		public CustomCoding_type customCoding;
	}

	/**
	 * Represents the options that drive the value checking mechanisms. </p>
	 * All members have to be final, as it is not allowed to change the
	 * options during analysis. If this would be needed for a branch of the
	 * analysis, a copy should be made.
	 * */
	public static class ValueCheckingOptions {
		/** The kind of the value to be expected */
		public final Expected_Value_type expected_value;
		/**
		 * true if an incomplete value can be accepted at the given
		 * location, false otherwise
		 */
		public final boolean incomplete_allowed;
		/**
		 * true if the omit value can be accepted at the given location,
		 * false otherwise
		 */
		public final boolean omit_allowed;
		/** true if the subtypes should also be checked */
		public final boolean sub_check;
		/**
		 * true if the implicit omit optional attribute was set for the
		 * value, false otherwise
		 */
		public final boolean implicit_omit;
		/** true if the value to be checked is an element of a string */
		public final boolean str_elem;

		public ValueCheckingOptions(final Expected_Value_type expectedValue, final boolean incompleteAllowed, final boolean omitAllowed,
				final boolean subCheck, final boolean implicitOmit, final boolean strElem) {
			this.expected_value = expectedValue;
			this.incomplete_allowed = incompleteAllowed;
			this.omit_allowed = omitAllowed;
			this.sub_check = subCheck;
			this.implicit_omit = implicitOmit;
			this.str_elem = strElem;
		}
	}

	/** @return the internal type of the type */
	Type_type getTypetype();

	/** @return the parent type of the actual type */
	IType getParentType();

	/**
	 * Sets the parent type of the actual type.
	 *
	 * @param type
	 *                the type to set.
	 * */
	void setParentType(final IType type);

	/**
	 * @return the with attribute path element of this type. If it did not
	 *         exist it will be created.
	 * */
	WithAttributesPath getAttributePath();

	/**
	 * Sets the parent path for the with attribute path element of this
	 * type. Also, creates the with attribute path node if it did not exist
	 * before.
	 *
	 * @param parent
	 *                the parent to be set.
	 * */
	void setAttributeParentPath(final WithAttributesPath parent);

	/**
	 * Clears the with attributes assigned to this type.
	 * <p>
	 * Should only be used on component fields.
	 * */
	void clearWithAttributes();

	/**
	 * Sets the with attributes for this type.
	 *
	 * @param attributes
	 *                the attributes to set.
	 * */
	void setWithAttributes(final MultipleWithAttributes attributes);

	/** @return true if the done extension was assigned to this type */
	boolean hasDoneAttribute();

	/** @return true if this type has a constraint attached to it */
	boolean isConstrained();

	/**
	 * Add constraints to this type.
	 *
	 * @param constraints
	 *                the constraints to be added.
	 * */
	void addConstraints(final Constraints constraints);

	/**
	 * @return the constraints set on this type, null if none.
	 * */
	Constraints getConstraints();

	/**
	 * @return the sub-type restriction of the actual type, or null if it
	 *         does not have one.
	 */
	SubType getSubtype();

	/**
	 * Sets the type restrictions as they were parsed.
	 *
	 * @param parsedRestrictions
	 *                the restrictions to set on this type.
	 * */
	void setParsedRestrictions(final List<ParsedSubType> parsedRestrictions);

	/**
	 * Returns the type referred last in case of a referred type, or itself
	 * in any other case.
	 *
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 *
	 * @return the actual or the last referred type
	 * */
	IType getTypeRefdLast(final CompilationTimeStamp timestamp);

	/**
	 * Returns the referenced field type for structured types, or itself in
	 * any other case.
	 *
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 * @param reference
	 *                the reference used to select the field.
	 * @param actualSubReference
	 *                the index used to tell, which element of the reference
	 *                to use as the field selector.
	 * @param expectedIndex
	 *                the expected kind of the index value
	 * @param interruptIfOptional
	 *                if true then returns null when reaching an optional
	 *                field
	 * @return the type of the field, or self. In case of error a null
	 *         reference is returned.
	 * */
	IType getFieldType(final CompilationTimeStamp timestamp, final Reference reference, final int actualSubReference,
			final Expected_Value_type expectedIndex, final boolean interruptIfOptional);

	/**
	 * Returns the referenced field type for structured types, or itself in
	 * any other case.
	 *
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 * @param reference
	 *                the reference used to select the field.
	 * @param actualSubReference
	 *                the index used to tell, which element of the reference
	 *                to use as the field selector.
	 * @param expectedIndex
	 *                the expected kind of the index value
	 * @param refChain
	 *                a chain of references used to detect circular
	 *                references.
	 * @param interruptIfOptional
	 *                if true then returns null when reaching an optional
	 *                field
	 * @return the type of the field, or self.
	 * */
	IType getFieldType(final CompilationTimeStamp timestamp, final Reference reference, final int actualSubReference,
			final Expected_Value_type expectedIndex, final IReferenceChain refChain, final boolean interruptIfOptional);

	/**
	 * Calculates the list of field types traversed, in type_array and their
	 * local indices in subrefsArray parameters. Must be used only after
	 * getFieldType() was already successfully invoked. It can be used only
	 * when all array indexes are foldable, otherwise it returns false.
	 *
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 * @param reference
	 *                the reference used to select the field.
	 * @param actualSubReference
	 *                the index used to tell, which element of the reference
	 *                to use as the field selector.
	 * @param subrefsArray
	 *                the list of field indices the searched fields were
	 *                found at.
	 * @param typeArray
	 *                the list of types found while traversing the fields.
	 * @return true in case the type of the referenced field could be
	 *         evaluated, false otherwise.
	 */
	boolean getSubrefsAsArray(final CompilationTimeStamp timestamp, final Reference reference, final int actualSubReference,
			List<Integer> subrefsArray, List<IType> typeArray);

	/**
	 * Returns whether the last field referenced by subReferences is an optional
	 * record/SEQUENCE or set/SET field.
	 * It can be used only after a successful
	 * semantic check (e.g. during code generation) or the behaviour will be
	 * unpredictable.
	 *
	 * @param subReferences the subreferences to walk
	 * */
	boolean fieldIsOptional(final List<ISubReference> subReferences);

	/**
	 * @return the raw attribute of the type.
	 * */
	RawAST getRawAttribute();

	/**
	 * Returns the default field length of this type (in bits).
	 * integer: 8
	 * boolean: 0
	 * float: 64
	 * other: 0
	 * 
	 * @return the default field length of the type.
	 * */
	public int getDefaultRawFieldLength();

	/**
	 * TODO might need some optimization later.
	 * @param timestamp
	 *                the time stamp of the actual build cycle.
	 * @return the length of the raw field length of this type
	 * */
	public int getRawLength(final BuildTimestamp timestamp);

	/**
	 * Returns the default field length of this type (in bits).
	 * hexstring: 4
	 * octetstring, charstring, universalcharstring: 8
	 * 
	 * other: 1
	 * 
	 * @return the default field length of the type.
	 * */
	public int getLengthMultiplier();

	/**
	 * Calculates the list of field types traversed. Does not check if the
	 * index values are valid. Does not give error messages, it just returns
	 * with false.
	 *
	 * @param reference
	 *                the reference used to select the field.
	 * @param actualSubReference
	 *                the index used to tell, which element of the reference
	 *                to use as the field selector.
	 * @param typeArray
	 *                the list of types found while traversing the fields.
	 * @return true in case the type of the referenced field could be
	 *         evaluated, false otherwise.
	 */
	boolean getFieldTypesAsArray(final Reference reference, final int actualSubReference, List<IType> typeArray);

	/**
	 * Checks if there is a variant attribute among the ones reaching to
	 * this type.
	 *
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 *
	 * @return true if there was a variant attribute found, false otherwise.
	 * */
	boolean hasVariantAttributes(final CompilationTimeStamp timestamp);

	/**
	 * Checks if a given type has the done extension attribute assigned to
	 * it, or not.
	 * */
	void checkDoneAttribute(final CompilationTimeStamp timestamp);

	/**
	 * Parses the attributes assigned to this type, and creates the needed
	 * semantic structures to store the gained data.
	 *
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 * FIXME this belongs to legacy mode parsing, needs to be removed
	 * */
	void parseAttributes(final CompilationTimeStamp timestamp);

	/**
	 * Fills the list parameter with the types that have an empty coding
	 * table. The types considered are the type itself and its field and
	 * element types. Recursive.
	 *
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 * @param typeList the list to fill with the found types
	 * @param only_own_table
	 *                if true, then only the type's own coding table is
	 *                checked, otherwise inherited coding tables are also
	 *                checked
	 */
	public void getTypesWithNoCodingTable(final CompilationTimeStamp timestamp, final ArrayList<IType> typeList, final boolean onlyOwnTable);

	//TODO check if we need to separate global or not
	/**
	 * Parses the specified variant attribute and checks its validity (when
	 * using the new codec handling).
	 *
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 * @param singleWithAttribute
	 *                the with attribute to parse.
	 * @param global
	 *                is the attribute a global one?
	 * */
	void checkThisVariant(final CompilationTimeStamp timestamp, final SingleWithAttribute singleWithAttribute, final boolean global);

	/**
	 * Checks the coding instructions set by the parsed variant attributes.
	 * The check is performed recursively on the type's fields and elements.
	 *
	 * Checks the raw and other coding attributes in one place.
	 * (the compiler has chk_raw separately)
	 * May also create such internal attributes based on restrictions.
	 *
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 * @param refChain
	 *                a chain of references used to detect circular
	 *                references.
	 * */
	void checkCodingAttributes(final CompilationTimeStamp timestamp, IReferenceChain refChain);

	/**
	 * If the type does not have its raw attribute, generate and check a default one.
	 *
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 * */
	void forceRaw(final CompilationTimeStamp timestamp);

	/**
	 * Set the raw attribute of a type from outside.
	 * Should be used only when raw attribute checking requires it
	 *
	 * @param newAttributes the new attributes to set.
	 * */
	void setRawAttributes(final RawAST newAttributes);

	/**
	 * Adds a coding to the type.
	 *
	 * @param timestamp the time stamp of the actual semantic check cycle.
	 * @param name the name of the coding to add.
	 * @param modifier the modifier of the coding to add.
	 * @param silent stay silent if the coding can not be applied to a given type.
	 * */
	void addCoding(final CompilationTimeStamp timestamp, final String name, final Attribute_Modifier_type modifier, final boolean silent);

	/**
	 * Sets the encoding function for a type.
	 *
	 * @param codingName the name of the coding to set it for
	 * @param functionDefinition the function definition to set
	 * */
	void setEncodingFunction(final String codingName, final Assignment functionDefinition);

	/**
	 * Sets the decoding function for a type.
	 *
	 * @param codingName the name of the coding to set it for
	 * @param functionDefinition the function definition to set
	 * */
	void setDecodingFunction(final String codingName, final Assignment functionDefinition);

	/**
	 * Checks for the type that has a coding table.
	 *
	 * @param timestamp the time stamp of the actual semantic check cycle.
	 * @param ignoreLocal ignore the local coding table.
	 * @return the first type in the searched chain with a coding table.
	 * */
	IType getTypeWithCodingTable(final CompilationTimeStamp timestamp, final boolean ignoreLocal);

	/**
	 * Checks if the type can have the given encoding.
	 *
	 * @param timestamp the time stamp of the actual semantic check cycle.
	 * @param coding the coding to check for.
	 * @param refChain a reference chain to disable recursive looping.
	 *
	 * @return true if the type has the given encoding.
	 * */
	boolean canHaveCoding(final CompilationTimeStamp timestamp, final MessageEncoding_type coding, final IReferenceChain refChain);

	/**
	 * Checks if the type has the given encoding.
	 *
	 * @param timestamp the time stamp of the actual semantic check cycle.
	 * @param coding the coding to check for.
	 *
	 * @return true if the type has the given encoding.
	 * */
	boolean hasEncoding(final CompilationTimeStamp timestamp, final MessageEncoding_type coding, final String customEncoding);

	/**
	 * @return the coding table of this type
	 * */
	List<Coding_Type> getCodingTable();

	/**
	 * Checks if the complex type has a field whose name is exactly the same
	 * as the name of the definition defining the type.
	 *
	 * @param definitionName
	 *                the name of the definition.
	 **/
	void checkConstructorName(final String definitionName);

	/**
	 * The type of sub-type that belongs to this type or ST_NONE if this
	 * type cannot have sub-type, every type that can have a sub-type must
	 * override this function
	 */
	SubType.SubType_type getSubtypeType();

	/**
	 * Checks for circular references within embedded types.
	 *
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 * @param referenceChain
	 *                the ReferenceChain used to detect circular references,
	 *                must not be null.
	 **/
	void checkRecursions(final CompilationTimeStamp timestamp, final IReferenceChain referenceChain);

	/**
	 * Checks if the values of this type can be used only in `self'. Some
	 * types (default, function reference with `runs on self') are invalid
	 * outside of the component they were created in, they should not be
	 * sent/received in ports or used in compref.start. All structured types
	 * that may contain such internal types are also internal.
	 *
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 * @return true if component internal, false otherwise.
	 * */
	boolean isComponentInternal(final CompilationTimeStamp timestamp);

	/**
	 * Checks the types which should be component internal, if they have
	 * left the component. Should be called only if is_component_internal()
	 * returned true.
	 *
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 * @param typeSet
	 *                is used to escape infinite recursion, by maintaining
	 *                the set of types used to call this function.
	 * @param operation
	 *                the name of the operation to be included in the error
	 *                message.
	 * */
	void checkComponentInternal(final CompilationTimeStamp timestamp, final Set<IType> typeSet, final String operation);

	/**
	 * Checks whether the type can be a component of another type definition
	 * (e.g. field of a structured type, parameter/return type/ exception of
	 * a signature). Ports and Signatures are not allowed, Default only
	 * within structured types.
	 *
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 * @param errorLocation
	 *                the location to report the errors to.
	 * @param defaultAllowed
	 *                whether default should be allowed or not.
	 * @param errorMessage
	 *                the part of the error message to be reported.
	 * */
	void checkEmbedded(final CompilationTimeStamp timestamp, final Location errorLocation, final boolean defaultAllowed, final String errorMessage);

	/**
	 * If the value is an undefined lowerid, then this member decides
	 * whether it is a reference or a lowerid value (e.g., enum, named
	 * number).
	 *
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 * @param value
	 *                the value to be checked
	 *
	 * @return the converted value so that it can replace the original, or
	 *         the original if no conversion was needed
	 * */
	IValue checkThisValueRef(final CompilationTimeStamp timestamp, final IValue value);

	/**
	 * Checks if a given value is valid according to this type.
	 * <p>
	 * The default / base implementation checks referenced, expression and
	 * macro values, as they must be unfolded (if possible) before gaining
	 * access to the real / final value and that values kind.
	 *
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 * @param value
	 *                the value to be checked
	 * @param lhs
	 *                the assignment to check against
	 * @param valueCheckingOptions
	 *                the options according to which the given value should
	 *                be evaluated
	 * @return true if the value contains a reference to lhs
	 * */
	boolean checkThisValue(final CompilationTimeStamp timestamp, final IValue value, final Assignment lhs, final ValueCheckingOptions valueCheckingOptions);

	/**
	 * Checks whether the provided template is a specific value and the
	 * embedded value is a referenced one.
	 *
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 * @param template
	 *                the template to check.
	 *
	 * @return the checked template, might be different from the one passed
	 *         as parameter.
	 * */
	ITTCN3Template checkThisTemplateRef(final CompilationTimeStamp timestamp, final ITTCN3Template template);

	/**
	 * Checks whether the provided template is a specific value and the
	 * embedded value is a referenced one.
	 * Additionally checks whether the template has the expected value type and avoids circular references.
	 *
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 * @param template
	 *                the template to check.
	 *
	 * @return the checked template, might be different from the one passed
	 *         as parameter.
	 * */
	ITTCN3Template checkThisTemplateRef(final CompilationTimeStamp timestamp, final ITTCN3Template template,
			final Expected_Value_type expectedValue, final IReferenceChain referenceChain);

	/**
	 * Does the semantic checking of the provided template according to the
	 * a specific type.
	 *
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 * @param template
	 *                the template to be checked by the type.
	 * @param isModified
	 *                true if the template is a modified template
	 * @param implicitOmit
	 *                true if the implicit omit optional attribute was set
	 *                for the template, false otherwise
	 * @param lhs
	 *                the assignment to check against
	 * @return true if the value contains a reference to lhs
	 * */
	boolean checkThisTemplate(final CompilationTimeStamp timestamp, final ITTCN3Template template, final boolean isModified,
			final boolean implicitOmit, final Assignment lhs);

	/**
	 * Does the semantic checking of the provided template according to the
	 * sub-type of the actual type.
	 *
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 * @param template
	 *                the template to be checked by the sub-type.
	 * */
	void checkThisTemplateSubtype(final CompilationTimeStamp timestamp, final ITTCN3Template template);

	/**
	 * Returns whether this type is compatible with type.
	 * <p>
	 * Note: The compatibility relation is asymmetric. The function returns
	 * true if the set of possible values in type is a subset of possible
	 * values in this.
	 *
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 * @param otherType
	 *                the type to check against.
	 * @param info
	 *                the type compatibility information.
	 * @param leftChain
	 *                to detect type recursion on the left side.
	 * @param rightChain
	 *                to detect type recursion on the right side.
	 * @return true if they are compatible, false otherwise.
	 * */
	boolean isCompatible(final CompilationTimeStamp timestamp, final IType otherType, TypeCompatibilityInfo info,
			final TypeCompatibilityInfo.Chain leftChain, final TypeCompatibilityInfo.Chain rightChain);

	/**
	 * Returns whether this type is strongly compatible with type that is exactly has the same type and they are both base types
	 * <p>
	 *
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 * @param otherType
	 *                the type to check against.
	 * @param info
	 *                the type compatibility information.
	 * @param leftChain
	 *                to detect type recursion on the left side.
	 * @param rightChain
	 *                to detect type recursion on the right side.
	 * @return true if they are compatible, false otherwise.
	 * */
	boolean isStronglyCompatible(final CompilationTimeStamp timestamp, final IType otherType, TypeCompatibilityInfo info,
			final TypeCompatibilityInfo.Chain leftChain, final TypeCompatibilityInfo.Chain rightChain);

	/**
	 * Returns whether this type and it's sub-type are compatible to the
	 * other type and it's sub-type.
	 */
	CompatibilityLevel getCompatibility(final CompilationTimeStamp timestamp, final IType type, final TypeCompatibilityInfo info,
			final TypeCompatibilityInfo.Chain leftChain, final TypeCompatibilityInfo.Chain rightChain);

	/**
	 * Returns whether this type is identical to the parameter type.
	 *
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 * @param type
	 *                the type to check against
	 * @return true if they are identical, false otherwise
	 * */
	boolean isIdentical(final CompilationTimeStamp timestamp, final IType type);

	/**
	 * Returns the name of this type for the user to identify the type.
	 *
	 * For simple primitive types (like integer, float, bitstring) this is their name.
	 * For other types this is the full name of the definition of the type.
	 *
	 * @return the name of the type.
	 * */
	String getTypename();

	/**
	 * @return the TTCN-3 equivalent of this type's type, or undefined if
	 *         none
	 */
	Type_type getTypetypeTtcn3();

	/**
	 * @return the assignment/definition that defines this type.
	 * */
	Assignment getDefiningAssignment();

	// TODO declaration and proposal collecting should not belong here
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
	void addDeclaration(final DeclarationCollector declarationCollector, final int i);

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
	void addProposal(final ProposalCollector propCollector, final int i);

	/**
	 * Creates the description part of this type to be displayed, in the
	 * code completion.
	 *
	 * @param the
	 *                part of the description to be extended
	 *
	 * @return the description extended with this type's description.
	 * */
	StringBuilder getProposalDescription(final StringBuilder builder);

	/**
	 * This function is used by user interface functions to determine which
	 * field of a type is enclosing the provided offset. For example this is
	 * used to find all references to a field.
	 *
	 * @param offset
	 *                the offset of the location in the file to search for.
	 * @param rf
	 *                the reference finder object that will store the type
	 *                and field informations when the offset is inside a
	 *                field of this type.
	 * */
	void getEnclosingField(final int offset, final ReferenceFinder rf);

	/**
	 * Returns the name of this type as it can be generated into the code.
	 *
	 * @return The name of the Java type in the generated code.
	 */
	public String getGenNameOwn();

	/**
	 * Returns the name of the Java value class that represents this at runtime.
	 * The class is either pre-defined (written manually in the Base
	 * Library) or generated by the compiler.
	 * The reference is valid in the module that \a p_scope belongs to.
	 *
	 * get_genname_value in titan.core
	 *
	 * @param aData only used to update imports if needed
	 * @param source the source code generated
	 * @param scope the scope into which the name needs to be generated
	 * @return The name of the Java value class in the generated code.
	 */
	public String getGenNameValue(final JavaGenData aData, final StringBuilder source, final Scope scope);

	/**
	 * Returns the name of the Java template class that represents this at runtime.
	 * The class is either pre-defined (written manually in the Base
	 * Library) or generated by the compiler.
	 * The reference is valid in the module that \a p_scope belongs to.
	 *
	 * get_genname_template in titan.core
	 *
	 * @param aData only used to update imports if needed
	 * @param source the source code generated
	 * @param scope the scope into which the name needs to be generated
	 * @return The name of the Java value class in the generated code.
	 */
	public String getGenNameTemplate(final JavaGenData aData, final StringBuilder source, final Scope scope);

	/**
	 * Returns the name of the type descriptor (- the _descr_ postfix).
	 *
	 * get_genname_typedescriptor in titan.core
	 *
	 * @param aData only used to update imports if needed
	 * @param source the source code generated
	 * @param scope the scope into which the name needs to be generated
	 * @return The name of the Java variable in the generated code.
	 */
	public String getGenNameTypeDescriptor(final JavaGenData aData, final StringBuilder source, final Scope scope);

	/**
	 * Returns the name prefix of the coder function related to this type.
	 *
	 * get_genname_coder in titan.core
	 *
	 * @param aData only used to update imports if needed
	 * @param source the source code generated
	 * @param scope the scope to generate to.
	 * @return The name prefix of the Java function in the generated code.
	 */
	public String getGenNameCoder(final JavaGenData aData, final StringBuilder source, final Scope scope);

	/**
	 * Returns the name prefix of the runtime variable holding the name of the default coder's name related to this type.
	 *
	 * get_genname_default_coding in titan.core
	 *
	 * @param aData only used to update imports if needed
	 * @param source the source code generated
	 * @param scope the scope to generate to.
	 * @return The name prefix of the Java function in the generated code.
	 */
	public String getGenNameDefaultCoding(final JavaGenData aData, final StringBuilder source, final Scope scope);

	/**
	 * Returns the name of the RAW type descriptor (- the _descr_ postfix).
	 *
	 * get_genname_rawdescriptor in titan.core
	 *
	 * @param aData only used to update imports if needed
	 * @param source the source code generated
	 * @return The name of the Java variable in the generated code.
	 */
	public String getGenNameRawDescriptor(final JavaGenData aData, final StringBuilder source);

	/**
	 * Returns the name prefix of type descriptors, etc. that belong to the
	 * equivalent Java class referenced from the module of scope \a p_scope.
	 * It differs from \a get_genname() only in case of special ASN.1 types
	 * like RELATIVE-OID or various string types.
	 *
	 * get_genname_value in titan.core
	 *
	 * @param aData only used to update imports if needed
	 * @param source the source code generated
	 * @param scope the scope into which the name needs to be generated
	 * @return The name of the Java value class in the generated code.
	 */
	public String getGenNameTypeName(final JavaGenData aData, final StringBuilder source, final Scope scope);

	/**
	 * Returns whether this type can be encoded according to rules
	 * encoding.
	 *
	 * originally get_gen_coder_functions
	 *
	 * @param encodingType the encoding type to check
	 * @return true if the type has the provided encoding, false otherwise
	 * */
	public boolean getGenerateCoderFunctions(final MessageEncoding_type encodingType);

	/**
	 * Indicates for the type and it's field types, that they need to generate coder functions for the provided coding type.
	 * @param timestamp the time stamp of the actual semantic check cycle.
	 * @param encodingType the encoding type to use.
	 * */
	public void setGenerateCoderFunctions(final CompilationTimeStamp timestamp, final MessageEncoding_type encodingType);

	/**
	 * Add generated java code on this level.
	 * @param aData only used to update imports if needed
	 * @param source the source code generated
	 */
	public void generateCode( final JavaGenData aData, final StringBuilder source );

	/**
	 * Generates type specific call for the reference used in isbound call
	 * into argument expression. Argument \a subrefs holds the reference path
	 * that needs to be checked. Argument \a module is the actual module of
	 * the reference and is used to gain access to temporal identifiers.
	 *
	 * generate_code_ispresentbound in the compiler
	 *
	 * @param aData only used to update imports if needed
	 * @param expression the expression for code generation
	 * @param subreferences the subreference to process
	 * @param subReferenceIndex the index telling which part of the subreference to process
	 * @param globalId is the name of the bool variable where the result
	 * of the isbound check is calculated.
	 * @param externalId is the name
	 * of the assignment where the call chain starts.
	 * @param isTemplate is_template tells if the assignment is a template or not.
	 * @param optype tells if the function is isbound or ispresent.
	 * @param field TODO
	 * */
	public void generateCodeIsPresentBoundChosen(final JavaGenData aData, final ExpressionStruct expression, final List<ISubReference> subreferences, final int subReferenceIndex, final String globalId, final String externalId, final boolean isTemplate, final Operation_type optype, final String field);

	/**
	 * Helper function used in generateCodeIspresentbound() for the
	 * ispresent() function in case of template parameter.
	 *
	 * @return true if the referenced field which is embedded into a "?" is always present,
	 * otherwise returns false.
	 * */
	public boolean isPresentAnyvalueEmbeddedField(final ExpressionStruct expression, final List<ISubReference> subreferences, final int beginIndex);

	/** Set the owner and its type type */
	public void setOwnertype(final TypeOwner_type ownerType, final INamedNode owner);

	TypeOwner_type getOwnertype();
	INamedNode getOwner();
}
