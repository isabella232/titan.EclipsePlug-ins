/******************************************************************************
 * Copyright (c) 2000-2020 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.types;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.titan.designer.AST.FieldSubReference;
import org.eclipse.titan.designer.AST.Type;
import org.eclipse.titan.designer.AST.TTCN3.attributes.RawASTStruct;
import org.eclipse.titan.designer.AST.TTCN3.attributes.RawASTStruct.rawAST_coding_field_list;
import org.eclipse.titan.designer.AST.TTCN3.attributes.RawASTStruct.rawAST_coding_field_type;
import org.eclipse.titan.designer.AST.TTCN3.attributes.RawASTStruct.rawAST_coding_fields;
import org.eclipse.titan.designer.AST.TTCN3.attributes.RawASTStruct.rawAST_coding_taglist;
import org.eclipse.titan.designer.compiler.JavaGenData;

/**
 * Utility class for generating the value and template classes for union/choice
 * types.
 *
 * The code generated for union/choice types only differs in matching and
 * encoding.
 *
 * @author Kristof Szabados
 * @author Arpad Lovassy
 * */
public final class UnionGenerator {

	/**
	 * Data structure to store sequence field variable and type names.
	 * Used for java code generation.
	 */
	public static class FieldInfo {

		/** Java type name of the field */
		private final String mJavaTypeName;

		private final String mJavaTemplateName;

		/** Field variable name in TTCN-3 and java */
		private final String mVarName;

		/** The user readable name of the field, typically used in error messages */
		private final String mDisplayName;

		/** Field variable name in java getter/setter function names and parameters */
		private final String mJavaVarName;

		private final String mTypeDescriptorName;

		private final String jsonAlias;

		private final int jsonValueType;

		/**
		 * @param fieldType
		 *                the string representing the value type of this
		 *                field in the generated code.
		 * @param fieldTemplate
		 *                the string representing the template type of
		 *                this field in the generated code.
		 * @param fieldName
		 *                the string representing the name of this field
		 *                in the generated code.
		 * @param displayName
		 *                the string representing the name of this field
		 *                in the error messages and logs in the
		 *                generated code.
		 * @param typeDescriptorName
		 *                the name of the type descriptor.
		 * */
		public FieldInfo(final String fieldType, final String fieldTemplate, final String fieldName, final String displayName,
						 final String typeDescriptorName, final String jsonAlias, final int jsonValueType) {
			mJavaTypeName = fieldType;
			mJavaTemplateName = fieldTemplate;
			mVarName = fieldName;
			mJavaVarName = FieldSubReference.getJavaGetterName( mVarName );
			mDisplayName = displayName;
			mTypeDescriptorName = typeDescriptorName;
			this.jsonAlias = jsonAlias;
			this.jsonValueType = jsonValueType;
		}
	}

	private static class TemporalVariable {
		public String type;
		public String typedescriptor;
		int start_pos;
		int use_counter;
		int decoded_for_element;
	}

	/*
	 * Should the union have more than 200 fields, we will generate helper functions.
	 * Each of which will handle 200 fields on its own.
	 * This is done as in the case of Diameter a union with 1666 fields
	 *  would generate too much code into a single function.
	 **/
	private static final int maxFieldsLength = 200;

	private UnionGenerator() {
		// private to disable instantiation
	}

	/**
	 * This function can be used to generate the value class of union/choice
	 * types
	 *
	 * defUnionClass in compiler2/union.{h,c}
	 *
	 * @param aData
	 *                used to access build settings.
	 * @param source
	 *                where the source code is to be generated.
	 * @param genName
	 *                the name of the generated class representing the
	 *                union/choice type.
	 * @param displayName
	 *                the user readable name of the type to be generated.
	 * @param fieldInfos
	 *                the list of information about the fields.
	 * @param hasOptional
	 *                {@code true} if the type has an optional field.
	 * @param hasRaw
	 *                {@code true} if the type has raw attributes.
	 * @param raw
	 *                the raw coding related settings if applicable.
	 * @param hasJson 
	 *                {@code true} if the type has JSON attributes.
	 * @param isAnytypeKind
	 *                true if anytype kind
	 * @param jsonAsValue
	 *                true if this type is a field of a union with the "as value" coding instruction
	 * @param localTypeDescriptor
	 *                the code to be generated into the class representing
	 *                the type and coding descriptors of the type.
	 * @param localCodingHandler
	 *                the coding handlers to be generated into the class.
	 * */
	public static void generateValueClass(final JavaGenData aData, final StringBuilder source, final String genName, final String displayName,
			final List<FieldInfo> fieldInfos, final boolean hasOptional, final boolean hasRaw, final RawASTStruct raw, final boolean hasJson,
			final boolean isAnytypeKind, final boolean jsonAsValue, final StringBuilder localTypeDescriptor, final StringBuilder localCodingHandler) {
		aData.addImport("java.text.MessageFormat");
		aData.addBuiltinTypeImport("Base_Type");
		aData.addBuiltinTypeImport("JSON_Tokenizer");
		aData.addBuiltinTypeImport("Text_Buf");
		aData.addBuiltinTypeImport("TtcnError");
		aData.addBuiltinTypeImport("TTCN_Logger");
		aData.addBuiltinTypeImport("TTCN_Buffer");
		aData.addBuiltinTypeImport("TTCN_EncDec.error_type");
		aData.addBuiltinTypeImport("TTCN_EncDec.raw_order_t");
		aData.addBuiltinTypeImport("TTCN_EncDec.coding_type");
		aData.addBuiltinTypeImport("RAW.RAW_enc_tr_pos");
		aData.addBuiltinTypeImport("RAW.RAW_enc_tree");
		aData.addBuiltinTypeImport("RAW.top_bit_order_t");
		aData.addBuiltinTypeImport("TTCN_EncDec_ErrorContext");
		aData.addBuiltinTypeImport("Param_Types.Module_Parameter");
		aData.addBuiltinTypeImport("Param_Types.Module_Param_Id");
		aData.addBuiltinTypeImport("Param_Types.Module_Param_Name");

		final boolean rawNeeded = hasRaw; //TODO can be forced optionally if needed
		final boolean jsonNeeded = hasJson; //TODO can be forced optionally if needed
		if (rawNeeded) {
			aData.addBuiltinTypeImport("RAW.RAW_Force_Omit");
		}

		source.append(MessageFormat.format("\tpublic static class {0} extends Base_Type '{'\n", genName));

		source.append(localTypeDescriptor);

		generateValueDeclaration(aData, source, genName, fieldInfos);
		generateValueConstructors(aData, source, genName, fieldInfos);
		generateValueCopyValue(aData, source, genName, displayName, fieldInfos);
		generateValueoperator_assign(aData, source, genName, displayName, fieldInfos);
		generateValueCleanup(source, fieldInfos);
		generateValueIsChosen(aData, source, displayName);
		generateValueIsBound(source);
		generateValueIsValue(source, fieldInfos);
		generateValueIsPresent(source);
		generateValueoperator_equals(aData, source, genName, displayName, fieldInfos);
		generateValueNotEquals(aData, source, genName);
		generateValueGetterSetters(aData, source, genName, displayName, fieldInfos);
		generateValueGetSelection(aData, source, genName, fieldInfos);
		generateValueLog(source, fieldInfos);
		generateValueSetParam(source, displayName, fieldInfos);
		generateValueGetParam(source, displayName, fieldInfos);
		if (!fieldInfos.isEmpty()) {
			generateValueSetImplicitOmit(source, fieldInfos);
		}
		generateValueEncodeDecodeText(source, genName, displayName, fieldInfos);
		generateValueEncodeDecode(source, genName, displayName, fieldInfos, rawNeeded, hasRaw, raw);
		if (jsonNeeded && fieldInfos.size() > 0) {
			aData.addImport("java.util.concurrent.atomic.AtomicInteger");
			aData.addImport("java.util.concurrent.atomic.AtomicReference");
			aData.addBuiltinTypeImport("JSON");
			aData.addBuiltinTypeImport("JSON_Tokenizer.json_token_t");
			generateValueJsonEncodeDecode(source, genName, displayName, fieldInfos, jsonAsValue);
		}

		source.append(localCodingHandler);

		source.append("\t}\n");
	}

	/**
	 * This function can be used to generate the template class of
	 * union/choice types
	 *
	 * defUnionClass in compiler2/union.{h,c}
	 *
	 * @param aData
	 *                used to access build settings.
	 * @param source
	 *                where the source code is to be generated.
	 * @param genName
	 *                the name of the generated class representing the
	 *                union/choice type.
	 * @param displayName
	 *                the user readable name of the type to be generated.
	 * @param fieldInfos
	 *                the list of information about the fields.
	 * @param hasOptional
	 *                {@code true} if the type has an optional field.
	 * */
	public static void generateTemplateClass(final JavaGenData aData, final StringBuilder source, final String genName, final String displayName,
			final List<FieldInfo> fieldInfos, final boolean hasOptional) {
		aData.addBuiltinTypeImport("Base_Template");
		aData.addBuiltinTypeImport("Text_Buf");
		aData.addBuiltinTypeImport("Param_Types.Module_Param_Any");
		aData.addBuiltinTypeImport("Param_Types.Module_Param_AnyOrNone");
		aData.addBuiltinTypeImport("Param_Types.Module_Param_Assignment_List");
		aData.addBuiltinTypeImport("Param_Types.Module_Param_ComplementList_Template");
		aData.addBuiltinTypeImport("Param_Types.Module_Param_List_Template");
		aData.addBuiltinTypeImport("Param_Types.Module_Param_Name");
		aData.addBuiltinTypeImport("Param_Types.Module_Param_Omit");
		aData.addBuiltinTypeImport("Param_Types.Module_Param_Unbound");
		aData.addImport("java.util.ArrayList");

		if (!fieldInfos.isEmpty()) {
			aData.addBuiltinTypeImport("Param_Types.Module_Param_FieldName");
		}

		source.append(MessageFormat.format("\tpublic static class {0}_template extends Base_Template '{'\n", genName));
		generateTemplateDeclaration(source, genName, fieldInfos);
		generatetemplateCopyValue(aData, source, genName, displayName, fieldInfos);
		generateTemplateConstructors(aData, source, genName);
		generateTemplateCleanup(source, fieldInfos);
		generateTemplateoperator_assign(aData, source, genName);
		generateTemplateMatch(aData, source, genName, displayName, fieldInfos);
		generateTemplateIsChosen(aData, source, genName, displayName);
		generateTemplateIsValue(source, displayName, fieldInfos);
		generateTemplateValueOf(source, genName, displayName, fieldInfos);
		generateTemplateSetType(source, genName, displayName);
		generateTemplateListItem(source, genName, displayName);
		generateTemplateMatchOmit(source);
		generateTemplateGetterSetters(aData, source, genName, displayName, fieldInfos);
		generateTemplateLog(source, fieldInfos);
		generateTemplateLogMatch(aData, source, genName, displayName, fieldInfos);
		generateTemplateEncodeDecodeText(source, genName, displayName, fieldInfos);
		generateTemplateSetParam(source, displayName, fieldInfos);
		generateTemplateGetParam(source, genName, displayName, fieldInfos);
		generateTemplateCheckSelection(source, displayName, fieldInfos);

		source.append("\t}\n");
	}

	/**
	 * Generate member variables
	 *
	 * @param aData
	 *                used to access build settings.
	 * @param source
	 *                where the source code is to be generated.
	 * @param genName
	 *                the name of the generated class representing the
	 *                union/choice type.
	 * @param fieldInfos
	 *                the list of information about the fields.
	 * */
	private static void generateValueDeclaration(final JavaGenData aData, final StringBuilder source, final String genName, final List<FieldInfo> fieldInfos) {
		if ( aData.isDebug() ) {
			source.append("\t\t/**\n");
			source.append("\t\t * Indicates the state/selection of this union kind.\n");
			source.append("\t\t * When union_selection is UNBOUND_VALUE, the union is unbound.\n");
			source.append("\t\t * When union_selection is any other enumeration,\n");
			source.append("\t\t * the appropriate field is selected.\n");
			source.append("\t\t * */\n");
		}
		source.append("\t\tpublic enum union_selection_type { UNBOUND_VALUE");
		for (int i = 0 ; i < fieldInfos.size(); i++) {
			source.append(", ");
			source.append(MessageFormat.format(" ALT_{0}", fieldInfos.get(i).mJavaVarName));
		}
		source.append(" };\n");
		source.append("\t\tprivate union_selection_type union_selection;\n");
		if (!fieldInfos.isEmpty()) {
			source.append("\t\t//originally a union which can not be mapped to Java\n");
			source.append("\t\tprivate Base_Type field;\n");
		}
		source.append('\n');
	}

	/**
	 * Generate constructors
	 *
	 * @param aData
	 *                used to access build settings.
	 * @param source
	 *                where the source code is to be generated.
	 * @param genName
	 *                the name of the generated class representing the
	 *                union/choice type.
	 * @param fieldInfos
	 *                the list of information about the fields.
	 * */
	private static void generateValueConstructors(final JavaGenData aData, final StringBuilder source, final String genName, final List<FieldInfo> fieldInfos){
		if ( aData.isDebug() ) {
			source.append( "\t\t/**\n" );
			source.append( "\t\t * Initializes to unbound value.\n" );
			source.append( "\t\t * */\n" );
		}
		source.append(MessageFormat.format("\t\tpublic {0}() '{'\n", genName));
		source.append("\t\t\tunion_selection = union_selection_type.UNBOUND_VALUE;\n");
		source.append("\t\t};\n\n");

		if ( aData.isDebug() ) {
			source.append( "\t\t/**\n" );
			source.append( "\t\t * Initializes to a given value.\n" );
			source.append( "\t\t *\n" );
			source.append( "\t\t * @param otherValue\n" );
			source.append( "\t\t *                the value to initialize to.\n" );
			source.append( "\t\t * */\n" );
		}
		source.append(MessageFormat.format("\t\tpublic {0}(final {0} otherValue) '{'\n", genName));
		source.append("\t\t\tcopy_value(otherValue);\n");
		source.append("\t\t};\n\n");
	}

	/**
	 * Generate the copy_value function
	 *
	 * @param aData
	 *                used to access build settings.
	 * @param source
	 *                where the source code is to be generated.
	 * @param genName
	 *                the name of the generated class representing the
	 *                union/choice type.
	 * @param displayName
	 *                the user readable name of the type to be generated.
	 * @param fieldInfos
	 *                the list of information about the fields.
	 * */
	private static void generateValueCopyValue(final JavaGenData aData,final StringBuilder source, final String genName, final String displayName, final List<FieldInfo> fieldInfos) {
		if (aData.isDebug()) {
			source.append("\t\t/**\n");
			source.append("\t\t * Internal function to copy the provided value into this template.\n");
			source.append("\t\t * The template becomes a specific value template.\n");
			source.append("\t\t * The already existing content is overwritten.\n");
			source.append("\t\t *\n");
			source.append("\t\t * @param other_value the value to be copied.\n");
			source.append("\t\t * */\n");
		}
		source.append(MessageFormat.format("\t\tprivate void copy_value(final {0} other_value) '{'\n", genName));
		if (!fieldInfos.isEmpty()) {
			source.append("\t\t\tswitch (other_value.union_selection){\n");
			for (int i = 0 ; i < fieldInfos.size(); i++) {
				final FieldInfo fieldInfo = fieldInfos.get(i);
				source.append(MessageFormat.format("\t\t\tcase ALT_{0}:\n", fieldInfo.mJavaVarName));
				source.append(MessageFormat.format("\t\t\t\tfield = new {0}(({0})other_value.field);\n", fieldInfo.mJavaTypeName));
				source.append("\t\t\t\tbreak;\n");
			}
			source.append("\t\t\tdefault:\n");
			source.append(MessageFormat.format("\t\t\t\tthrow new TtcnError(\"Assignment of an unbound union value of type {0}.\");\n", displayName));
			source.append("\t\t\t}\n");
		}
		source.append("\t\t\tunion_selection = other_value.union_selection;\n");
		source.append("\t\t}\n\n");
	}

	/**
	 * Generate assign functions
	 *
	 * @param aData
	 *                used to access build settings.
	 * @param source
	 *                where the source code is to be generated.
	 * @param genName
	 *                the name of the generated class representing the
	 *                union/choice type.
	 * @param displayName
	 *                the user readable name of the type to be generated.
	 * @param fieldInfos
	 *                the list of information about the fields.
	 * */
	private static void generateValueoperator_assign(final JavaGenData aData, final StringBuilder source, final String genName, final String displayName, final List<FieldInfo> fieldInfos) {
		if ( aData.isDebug() ) {
			source.append("\t\t/**\n");
			source.append("\t\t * Assigns the other value to this value.\n");
			source.append("\t\t * Overwriting the current content in the process.\n");
			source.append("\t\t *<p>\n");
			source.append("\t\t * operator= in the core.\n");
			source.append("\t\t *\n");
			source.append("\t\t * @param otherValue\n");
			source.append("\t\t *                the other value to assign.\n");
			source.append("\t\t * @return the new value object.\n");
			source.append("\t\t */\n");
		}
		source.append(MessageFormat.format("\t\tpublic {0} operator_assign( final {0} otherValue ) '{'\n", genName));
		source.append("\t\t\tif (otherValue != this) {\n");
		source.append("\t\t\t\tclean_up();\n");
		source.append("\t\t\t\tcopy_value(otherValue);\n");
		source.append("\t\t\t}\n\n");
		source.append("\t\t\treturn this;\n");
		source.append("\t\t}\n\n");

		source.append("\t\t@Override\n");
		source.append(MessageFormat.format("\t\tpublic {0} operator_assign( final Base_Type otherValue ) '{'\n", genName));
		source.append(MessageFormat.format("\t\t\tif (otherValue instanceof {0}) '{'\n", genName));
		source.append(MessageFormat.format("\t\t\t\treturn operator_assign(({0})otherValue);\n", genName));
		source.append("\t\t\t}\n");
		source.append(MessageFormat.format("\t\t\tthrow new TtcnError(\"Internal Error: value can not be cast to {0}.\");\n", displayName));

		source.append("\t\t}\n\n");
	}

	/**
	 * Generate the clean_up function
	 *
	 * @param source
	 *                where the source code is to be generated.
	 * @param fieldInfos
	 *                the list of information about the fields.
	 * */
	private static void generateValueCleanup(final StringBuilder source, final List<FieldInfo> fieldInfos) {
		source.append("\t\t@Override\n");
		source.append("\t\tpublic void clean_up() {\n");
		if (!fieldInfos.isEmpty()) {
			source.append("\t\t\tfield = null;\n");
		}
		source.append("\t\t\tunion_selection = union_selection_type.UNBOUND_VALUE;\n");
		source.append("\t\t}\n\n");
	}

	/**
	 * Generate the ischosen function
	 *
	 * @param aData
	 *                used to access build settings.
	 * @param source
	 *                where the source code is to be generated.
	 * @param displayName
	 *                the user readable name of the type to be generated.
	 * */
	private static void generateValueIsChosen(final JavaGenData aData, final StringBuilder source, final String displayName) {
		if (aData.isDebug()) {
			source.append("\t\t/**\n");
			source.append("\t\t * Checks and reports whether the union has the provided alternative active or not.\n");
			source.append("\t\t *\n");
			source.append("\t\t * ischosen in the core.\n");
			source.append("\t\t *\n");
			source.append("\t\t * @param checked_selection the selection to check for.\n");
			source.append("\t\t *\n");
			source.append("\t\t * @return {@code true} if the unions has the provided selection active.\n");
			source.append("\t\t */\n");
		}
		source.append("\t\tpublic boolean ischosen(final union_selection_type checked_selection) {\n");
		source.append("\t\t\tif(checked_selection == union_selection_type.UNBOUND_VALUE) {\n");
		source.append(MessageFormat.format("\t\t\t\tthrow new TtcnError(\"Internal error: Performing ischosen() operation on an invalid field of union type {0}.\");\n", displayName));
		source.append("\t\t\t}\n");
		source.append("\t\t\treturn union_selection == checked_selection;\n");
		source.append("\t\t}\n\n");
	}

	/**
	 * Generate the isBound function
	 *
	 * @param source
	 *                where the source code is to be generated.
	 * */
	private static void generateValueIsBound(final StringBuilder source) {
		source.append("\t\t@Override\n");
		source.append("\t\tpublic boolean is_bound() {\n");
		source.append("\t\t\treturn union_selection != union_selection_type.UNBOUND_VALUE;\n");
		source.append("\t\t}\n\n");
	}

	/**
	 * Generate the isValue function
	 *
	 * @param source
	 *                where the source code is to be generated.
	 * @param fieldInfos
	 *                the list of information about the fields.
	 * */
	private static void generateValueIsValue(final StringBuilder source, final List<FieldInfo> fieldInfos) {
		source.append("\t\t@Override\n");
		source.append("\t\tpublic boolean is_value() {\n");
		source.append("\t\t\tswitch (union_selection) {\n");
		source.append("\t\t\tcase UNBOUND_VALUE:\n");
		source.append("\t\t\t\treturn false;\n");
		//TODO could this be optimized?
		for (int i = 0 ; i < fieldInfos.size(); i++) {
			final FieldInfo fieldInfo = fieldInfos.get(i);
			source.append(MessageFormat.format("\t\t\tcase ALT_{0}:\n", fieldInfo.mJavaVarName));
			source.append("\t\t\t\treturn field.is_value();\n");
		}

		source.append("\t\t\tdefault:\n");
		source.append("\t\t\t\tthrow new TtcnError(\"Invalid selection in union is_bound\");\n");
		source.append("\t\t\t}\n");
		source.append("\t\t}\n\n");
	}

	/**
	 * Generate the isPresent function
	 *
	 * @param source
	 *                where the source code is to be generated.
	 * */
	private static void generateValueIsPresent(final StringBuilder source) {
		source.append("\t\t@Override\n");
		source.append("\t\tpublic boolean is_present() {\n");
		source.append("\t\t\treturn is_bound();\n");
		source.append("\t\t}\n\n");
	}

	/**
	 * Generate equals operators (originally ==)
	 *
	 * @param aData
	 *                used to access build settings.
	 * @param source
	 *                where the source code is to be generated.
	 * @param genName
	 *                the name of the generated class representing the
	 *                union/choice type.
	 * @param displayName
	 *                the user readable name of the type to be generated.
	 * @param fieldInfos
	 *                the list of information about the fields.
	 * */
	private static void generateValueoperator_equals(final JavaGenData aData, final StringBuilder source, final String genName, final String displayName,
			final List<FieldInfo> fieldInfos) {
		if (aData.isDebug()) {
			source.append("\t\t/**\n");
			source.append("\t\t * Checks if the current value is equivalent to the provided one.\n");
			source.append("\t\t *\n");
			source.append("\t\t * operator== in the core\n");
			source.append("\t\t *\n");
			source.append("\t\t * @param otherValue\n");
			source.append("\t\t *                the other value to check against.\n");
			source.append("\t\t * @return {@code true} if the selections and field values are equivalent.\n");
			source.append("\t\t */\n");
		}
		source.append(MessageFormat.format("\t\tpublic boolean operator_equals( final {0} otherValue ) '{'\n", genName));
		source.append(MessageFormat.format("\t\t\tmust_bound( \"The left operand of comparison is an unbound value of union type {0}.\" );\n", displayName));
		source.append(MessageFormat.format("\t\t\totherValue.must_bound( \"The right operand of comparison is an unbound value of union type {0}.\" );\n", displayName));
		source.append("\t\t\tif (union_selection != otherValue.union_selection) {\n");
		source.append("\t\t\t\treturn false;\n");

		source.append("\t\t\t}\n");
		source.append("\t\t\tswitch (union_selection) {\n");
		//TODO could this be optimized?
		for (int i = 0 ; i < fieldInfos.size(); i++) {
			final FieldInfo fieldInfo = fieldInfos.get(i);
			source.append(MessageFormat.format("\t\t\tcase ALT_{0}:\n", fieldInfo.mJavaVarName));
			source.append(MessageFormat.format("\t\t\t\treturn (({0})field).operator_equals(({0})otherValue.field);\n", fieldInfo.mJavaTypeName));
		}


		source.append("\t\t\tdefault:\n");
		source.append("\t\t\t\treturn false;\n");
		source.append("\t\t\t}\n");
		source.append("\t\t}\n\n");

		source.append("\t\t@Override\n");
		source.append("\t\tpublic boolean operator_equals( final Base_Type otherValue ) {\n");
		source.append(MessageFormat.format("\t\t\tif (otherValue instanceof {0}) '{'\n", genName));
		source.append(MessageFormat.format("\t\t\t\treturn operator_equals(({0})otherValue);\n", genName));
		source.append("\t\t\t}\n");
		source.append(MessageFormat.format("\t\t\tthrow new TtcnError(\"Internal Error: value can not be cast to {0}.\");\n", displayName));
		source.append("\t\t}\n\n");
	}

	/**
	 * Generate not equals operators (originally !=)
	 *
	 * @param aData
	 *                used to access build settings.
	 * @param source
	 *                where the source code is to be generated.
	 * @param genName
	 *                the name of the generated class representing the
	 *                union/choice type.
	 * */
	private static void generateValueNotEquals(final JavaGenData aData, final StringBuilder source, final String genName) {
		if (aData.isDebug()) {
			source.append("\t\t/**\n");
			source.append("\t\t * Checks if the current value is not equivalent to the provided one.\n");
			source.append("\t\t *\n");
			source.append("\t\t * operator!= in the core\n");
			source.append("\t\t *\n");
			source.append("\t\t * @param otherValue\n");
			source.append("\t\t *                the other value to check against.\n");
			source.append("\t\t * @return {@code true} if either the selections or the field\n");
			source.append("\t\t *         values are not equivalent.\n");
			source.append("\t\t */\n");
		}
		source.append(MessageFormat.format("\t\tpublic boolean operator_not_equals( final {0} otherValue ) '{'\n", genName));
		source.append("\t\t\treturn !operator_equals(otherValue);\n");
		source.append("\t\t}\n\n");
	}

	/**
	 * Generate getters/setters
	 *
	 * @param aData
	 *                used to access build settings.
	 * @param source
	 *                where the source code is to be generated.
	 * @param genName
	 *                the name of the generated class representing the
	 *                union/choice type.
	 * @param displayName
	 *                the user readable name of the type to be generated.
	 * @param fieldInfos
	 *                the list of information about the fields.
	 * */
	private static void generateValueGetterSetters(final JavaGenData aData, final StringBuilder source, final String genName, final String displayName,
			final List<FieldInfo> fieldInfos) {
		for (int i = 0 ; i < fieldInfos.size(); i++) {
			final FieldInfo fieldInfo = fieldInfos.get(i);

			if (aData.isDebug()) {
				source.append("\t\t/**\n");
				source.append(MessageFormat.format("\t\t * Selects and gives access to field {0}.\n", fieldInfo.mDisplayName));
				source.append("\t\t * If other field was previously selected, its value will be destroyed.\n");
				source.append("\t\t *\n");
				source.append(MessageFormat.format("\t\t * @return field {0}.\n", fieldInfo.mDisplayName));
				source.append("\t\t * */\n");
			}
			source.append(MessageFormat.format("\t\tpublic {0} get_field_{1}() '{'\n", fieldInfo.mJavaTypeName, fieldInfo.mJavaVarName));
			source.append(MessageFormat.format("\t\t\tif (union_selection != union_selection_type.ALT_{0}) '{'\n", fieldInfo.mJavaVarName));
			source.append("\t\t\t\t//clean_up();\n");
			source.append(MessageFormat.format("\t\t\t\tfield = new {0}();\n", fieldInfo.mJavaTypeName));
			source.append(MessageFormat.format("\t\t\t\tunion_selection = union_selection_type.ALT_{0};\n", fieldInfo.mJavaVarName));
			source.append("\t\t\t}\n");
			source.append(MessageFormat.format("\t\t\treturn ({0})field;\n", fieldInfo.mJavaTypeName));
			source.append("\t\t}\n\n");

			if (aData.isDebug()) {
				source.append("\t\t/**\n");
				source.append(MessageFormat.format("\t\t * Gives read-only access to field {0}.\n", fieldInfo.mDisplayName));
				source.append(MessageFormat.format("\t\t * If field {0} is not selected,\n", fieldInfo.mDisplayName));
				source.append("\t\t * this function will cause a dynamic test case error.\n");
				source.append("\t\t *\n");
				source.append(MessageFormat.format("\t\t * @return field {0}.\n", fieldInfo.mDisplayName));
				source.append("\t\t * */\n");
			}
			source.append(MessageFormat.format("\t\tpublic {0} constGet_field_{1}() '{'\n", fieldInfo.mJavaTypeName, fieldInfo.mJavaVarName));
			source.append(MessageFormat.format("\t\t\tif (union_selection != union_selection_type.ALT_{0}) '{'\n", fieldInfo.mJavaVarName));
			source.append(MessageFormat.format("\t\t\t\tthrow new TtcnError(\"Using non-selected field {0} in a value of union type {1}.\");\n", fieldInfo.mDisplayName, displayName));
			source.append("\t\t\t}\n");
			source.append(MessageFormat.format("\t\t\treturn ({0})field;\n", fieldInfo.mJavaTypeName));
			source.append("\t\t}\n\n");
		}
	}

	/**
	 * Generate the get_selection function
	 *
	 * @param aData
	 *                used to access build settings.
	 * @param source
	 *                where the source code is to be generated.
	 * @param genName
	 *                the name of the generated class representing the
	 *                union/choice type.
	 * @param fieldInfos
	 *                the list of information about the fields.
	 * */
	private static void generateValueGetSelection(final JavaGenData aData, final StringBuilder source, final String genName,
			final List<FieldInfo> fieldInfos) {
		if (aData.isDebug()) {
			source.append("\t\t/**\n");
			source.append("\t\t * Returns the current selection.\n");
			source.append(MessageFormat.format("\t\t * It will return {0}.union_selection_type.UNBOUND_VALUE if the value is unbound,\n", genName));
			if (!fieldInfos.isEmpty()) {
				source.append(MessageFormat.format("\t\t * {0}.union_selection_type.ALT_{1} if the first field was selected, and so on.\n", genName, fieldInfos.get(0).mJavaVarName));
			}
			source.append("\t\t *\n");
			source.append("\t\t * @return the current selection.\n");
			source.append("\t\t * */\n");
		}
		source.append("\t\tpublic union_selection_type get_selection() {\n");
		source.append("\t\t\treturn union_selection;\n");
		source.append("\t\t}\n\n");
	}

	/**
	 * Generate log
	 *
	 * @param source
	 *                where the source code is to be generated.
	 * @param fieldInfos
	 *                the list of information about the fields.
	 * */
	private static void generateValueLog(final StringBuilder source, final List<FieldInfo> fieldInfos) {
		source.append("\t\t@Override\n");
		source.append("\t\tpublic void log() {\n");
		source.append("\t\t\tswitch (union_selection) {\n");
		for (int i = 0 ; i < fieldInfos.size(); i++) {
			//TODO could this be optimized?
			final FieldInfo fieldInfo = fieldInfos.get(i);
			source.append(MessageFormat.format("\t\t\tcase ALT_{0}:\n", fieldInfo.mJavaVarName));
			source.append(MessageFormat.format("\t\t\t\tTTCN_Logger.log_event_str(\"'{' {0} := \");\n", fieldInfo.mDisplayName));
			source.append("\t\t\t\tfield.log();\n");
			source.append("\t\t\t\tTTCN_Logger.log_event_str(\" }\");\n");
			source.append("\t\t\t\tbreak;\n");
		}

		source.append("\t\t\tdefault:\n");
		source.append("\t\t\t\tTTCN_Logger.log_event_unbound();\n");
		source.append("\t\t\t\tbreak;\n");
		source.append("\t\t\t}\n");
		source.append("\t\t}\n\n");
	}

	/**
	 * Generate set_param.
	 *
	 * @param source
	 *                where the source code is to be generated.
	 * @param displayName
	 *                the user readable name of the type to be generated.
	 * @param fieldInfos
	 *                the list of information about the fields.
	 */
	private static void generateValueSetParam(final StringBuilder source, final String displayName, final List<FieldInfo> fieldInfos) {
		if (fieldInfos.size() > maxFieldsLength) {
			final int fullSize = fieldInfos.size();
			final int iterations = fullSize / maxFieldsLength;
			for (int iteration = 0; iteration <= iterations; iteration++) {
				final int start = iteration * maxFieldsLength ;
				final int end = Math.min((iteration + 1) * maxFieldsLength - 1, fullSize - 1);
				source.append("\t\t// Internal helper function.\n");
				source.append(MessageFormat.format("\t\tprivate boolean value_set_param_single_helper_{0,number,#}_{1,number,#}(final String name, final Module_Parameter param) '{'\n", start, end));
				for (int i = start ; i <= end; i++) {
					final FieldInfo fieldInfo = fieldInfos.get(i);

					source.append(MessageFormat.format("\t\t\tif(\"{0}\".equals(name)) '{'\n", fieldInfo.mDisplayName));
					source.append(MessageFormat.format("\t\t\t\tget_field_{0}().set_param(param);\n", fieldInfo.mJavaVarName));
					//source.append("\t\t\t\tsingle_value.set_param(param);\n");
					source.append("\t\t\t\treturn true;\n");
					source.append("\t\t\t}\n");
				}
				source.append("\t\t\treturn false;\n");
				source.append("\t\t}\n\n");

				source.append("\t\t// Internal helper function.\n");
				source.append(MessageFormat.format("\t\tprivate boolean value_set_param_list_helper_{0,number,#}_{1,number,#}(final String last_name, final Module_Parameter mp_last) '{'\n", start, end));
				for (int i = start ; i <= end; i++) {
					final FieldInfo fieldInfo = fieldInfos.get(i);

					source.append(MessageFormat.format("\t\t\tif(\"{0}\".equals(last_name)) '{'\n", fieldInfo.mDisplayName));
					source.append(MessageFormat.format("\t\t\t\tget_field_{0}().set_param(mp_last);\n", fieldInfo.mJavaVarName));
					source.append("\t\t\t\treturn true;\n");
					source.append("\t\t\t}\n");
				}
				source.append("\t\t\treturn false;\n");
				source.append("\t\t}\n\n");
			}
		}

		source.append("\t\t@Override\n");
		source.append("\t\tpublic void set_param(Module_Parameter param) {\n");

		// Originally RT2
		source.append("\t\t\tfinal Module_Param_Id param_id = param.get_id();\n");
		source.append("\t\t\tif (param.get_id() != null && param.get_id().next_name()) {\n");
		// Haven't reached the end of the module parameter name
		// => the name refers to one of the fields, not to the whole union
		source.append("\t\t\t\tfinal String param_field = param.get_id().get_current_name();\n");
		source.append("\t\t\t\tfinal char first_char = param_field.charAt(0);\n");
		source.append("\t\t\t\tif (first_char >= '0' && first_char <= '9') {\n");
		source.append(MessageFormat.format("\t\t\t\t\tparam.error(\"Unexpected array index in module parameter, expected a valid field name for union type `{0}''\");\n", displayName ));
		source.append("\t\t\t\t}\n");
		if (fieldInfos.size() > maxFieldsLength) {
			final int fullSize = fieldInfos.size();
			final int iterations = fullSize / maxFieldsLength;
			for (int iteration = 0; iteration <= iterations; iteration++) {
				final int start = iteration * maxFieldsLength;
				final int end = Math.min((iteration + 1) * maxFieldsLength - 1, fullSize - 1);

				source.append(MessageFormat.format("\t\t\t\tif(value_set_param_single_helper_{0,number,#}_{1,number,#}(param_field, param)) '{'\n", start, end));
				source.append("\t\t\t\t\treturn;\n");
				source.append("\t\t\t\t}\n");
			}

			source.append(MessageFormat.format("\t\t\t\tparam.error(MessageFormat.format(\"Field `'{'0'}''' not found in union template type `{0}''\", param_field));\n", displayName));
		} else {
			for (int i = 0 ; i < fieldInfos.size(); i++) {
				final FieldInfo fieldInfo = fieldInfos.get(i);

				if (i == 0) {
					source.append("\t\t\t\t");
				} else {
					source.append(" else ");
				}
				source.append(MessageFormat.format("if(\"{0}\".equals(param_field)) '{'\n", fieldInfo.mDisplayName));
				source.append(MessageFormat.format("\t\t\t\t\tget_field_{0}().set_param(param);\n", fieldInfo.mJavaVarName));
				source.append("\t\t\t\t\treturn;\n");
				source.append("\t\t\t\t}");
			}

			source.append(" else {\n");
			source.append(MessageFormat.format("\t\t\t\t\tparam.error(MessageFormat.format(\"Field `'{'0'}''' not found in union template type `{0}''\", param_field));\n", displayName));
			source.append("\t\t\t\t}\n");
		}
		source.append("\t\t\t}\n");

		source.append("\t\t\tparam.basic_check(Module_Parameter.basic_check_bits_t.BC_VALUE.getValue(), \"union value\");\n");

		// Originally RT2
		source.append("\t\t\tif (param.get_type() == Module_Parameter.type_t.MP_Reference) {\n");
		source.append("\t\t\t\tparam = param.get_referenced_param().get();\n");
		source.append("\t\t\t}\n");

		source.append("\t\t\tif(param.get_type() == Module_Parameter.type_t.MP_Value_List && param.get_size() == 0) {\n");
		source.append("\t\t\t\treturn;\n");
		source.append("\t\t\t}\n");
		source.append("\t\t\tif (param.get_type() != Module_Parameter.type_t.MP_Assignment_List) {\n");
		source.append("\t\t\t\tparam.error(\"union value with field name was expected\");\n");
		source.append("\t\t\t}\n");
		source.append("\t\t\tfinal Module_Parameter mp_last = param.get_elem(param.get_size() - 1);\n");
		source.append("\t\t\tfinal String last_name = mp_last.get_id().get_name();\n");
		if (fieldInfos.size() > maxFieldsLength) {
			final int fullSize = fieldInfos.size();
			final int iterations = fullSize / maxFieldsLength;
			for (int iteration = 0; iteration <= iterations; iteration++) {
				final int start = iteration * maxFieldsLength;
				final int end = Math.min((iteration + 1) * maxFieldsLength - 1, fullSize - 1);
				source.append(MessageFormat.format("\t\t\tif(value_set_param_list_helper_{0,number,#}_{1,number,#}(last_name, mp_last)) '{'\n", start, end));
				source.append("\t\t\t\tif (!field.is_bound()) {\n");
				source.append("\t\t\t\t\tclean_up();\n");
				source.append("\t\t\t\t}\n");
				source.append("\t\t\t\treturn;\n");
				source.append("\t\t\t}\n");
			}
		} else {
			for (int i = 0 ; i < fieldInfos.size(); i++) {
				final FieldInfo fieldInfo = fieldInfos.get(i);

				source.append(MessageFormat.format("\t\t\tif (\"{0}\".equals(last_name)) '{'\n", fieldInfo.mDisplayName));
				source.append(MessageFormat.format("\t\t\t\tget_field_{0}().set_param(mp_last);\n", fieldInfo.mJavaVarName));
				source.append("\t\t\t\tif (!field.is_bound()) {\n");
				source.append("\t\t\t\t\tclean_up();\n");
				source.append("\t\t\t\t}\n");
				source.append("\t\t\t\treturn;\n");
				source.append("\t\t\t}\n");
			}
		}
		source.append(MessageFormat.format("\t\t\tmp_last.error(MessageFormat.format(\"Field '{'0'}' does not exist in type {0}.\", last_name));\n", displayName));
		source.append("\t\t}\n\n");
	}

	// Originally RT2
	/**
	 * Generate get_param.
	 *
	 * @param source
	 *                where the source code is to be generated.
	 * @param displayName
	 *                the user readable name of the type to be generated.
	 * @param fieldInfos
	 *                the list of information about the fields.
	 */
	private static void generateValueGetParam(final StringBuilder source, final String displayName, final List<FieldInfo> fieldInfos) {
		if (fieldInfos.size() > maxFieldsLength) {
			final int fullSize = fieldInfos.size();
			final int iterations = fullSize / maxFieldsLength;
			for (int iteration = 0; iteration <= iterations; iteration++) {
				final int start = iteration * maxFieldsLength ;
				final int end = Math.min((iteration + 1) * maxFieldsLength - 1, fullSize - 1);
				source.append("\t\t// Internal helper function.\n");
				source.append(MessageFormat.format("\t\tprivate Module_Parameter value_get_param_single_helper_{0,number,#}_{1,number,#}(final String param_field, final Module_Param_Name param_name) '{'\n", start, end));
				source.append("\t\t\t");
				for (int i = start ; i <= end; i++) {
					final FieldInfo fieldInfo = fieldInfos.get(i);

					source.append(MessageFormat.format("if (\"{0}\".equals(param_field)) '{'\n", fieldInfo.mDisplayName));
					source.append(MessageFormat.format("\t\t\t\treturn get_field_{0}().get_param(param_name);\n", fieldInfo.mJavaVarName));
					source.append("\t\t\t} else ");
				}
				source.append("{\n");
				source.append("\t\t\t\treturn null;\n");
				source.append("\t\t\t}\n");
				source.append("\t\t}\n\n");

				source.append(MessageFormat.format("\t\tprivate Module_Parameter value_get_param_specific_helper_{0,number,#}_{1,number,#}(final Module_Param_Name param_name) '{'\n", start, end));
				source.append("\t\t\tModule_Parameter mp_field = null;\n");
				source.append("\t\t\tswitch (union_selection) {\n");
				for (int i = start ; i <= end; i++) {
					final FieldInfo fieldInfo = fieldInfos.get(i);

					source.append(MessageFormat.format("\t\t\tcase ALT_{0}:\n", fieldInfo.mJavaVarName));
					source.append(MessageFormat.format("\t\t\t\tmp_field = get_field_{0}().get_param(param_name);\n", fieldInfo.mJavaVarName));
					source.append(MessageFormat.format("\t\t\t\tmp_field.set_id(new Module_Param_FieldName(\"{0}\"));\n", fieldInfo.mDisplayName));
					source.append("\t\t\t\treturn mp_field;\n");
				}
				source.append("\t\t\tdefault:\n");
				source.append("\t\t\t\treturn null;\n");
				source.append("\t\t\t}\n");
				source.append("\t\t}\n\n");
			}
		}

		source.append("\t\t@Override\n");
		source.append("\t\tpublic Module_Parameter get_param(final Module_Param_Name param_name) {\n");
		source.append("\t\t\tif (!is_bound()) {\n");
		source.append("\t\t\t\treturn new Module_Param_Unbound();\n");
		source.append("\t\t\t}\n");
		source.append("\t\t\tif (param_name.next_name()) {\n");
		// Haven't reached the end of the module parameter name
		// => the name refers to one of the fields, not to the whole union
		source.append("\t\t\t\tfinal String param_field = param_name.get_current_name();\n");
		source.append("\t\t\t\tif (param_field.charAt(0) >= '0' && param_field.charAt(0) <= '9') {\n");
		source.append(MessageFormat.format("\t\t\t\t\tthrow new TtcnError(\"Unexpected array index in module parameter, expected a valid field name for union type `{0}''\");\n", displayName ));
		source.append("\t\t\t\t}\n");
		
		if (fieldInfos.size() > maxFieldsLength) {
			source.append("\t\t\t\tModule_Parameter temp_parameter = null;\n");
			
			final int fullSize = fieldInfos.size();
			final int iterations = fullSize / maxFieldsLength;
			for (int iteration = 0; iteration <= iterations; iteration++) {
				final int start = iteration * maxFieldsLength;
				final int end = Math.min((iteration + 1) * maxFieldsLength - 1, fullSize - 1);

				source.append(MessageFormat.format("\t\t\t\ttemp_parameter = value_get_param_single_helper_{0,number,#}_{1,number,#}(param_field, param_name);\n", start, end));
				source.append("\t\t\t\tif(temp_parameter != null) {\n");
				source.append("\t\t\t\t\treturn temp_parameter;\n");
				source.append("\t\t\t\t}\n");
			}
		} else {
			source.append("\t\t\t\t");
			for (int i = 0 ; i < fieldInfos.size(); i++) {
				final FieldInfo fieldInfo = fieldInfos.get(i);
				source.append(MessageFormat.format("if (\"{0}\".equals(param_field)) '{'\n", fieldInfo.mDisplayName));
				source.append(MessageFormat.format("\t\t\t\t\treturn get_field_{0}().get_param(param_name);\n", fieldInfo.mJavaVarName));
				source.append("\t\t\t\t} else ");
			}
		}
		source.append("{\n");
		source.append(MessageFormat.format("\t\t\t\t\tthrow new TtcnError(MessageFormat.format(\"Field `'{'0'}''' not found in union type `{0}''\", param_field));\n", displayName));
		source.append("\t\t\t\t}\n");
		source.append("\t\t\t}\n");
		source.append("\t\t\tModule_Parameter mp_field;\n");
		if (fieldInfos.size() > maxFieldsLength) {
			final int fullSize = fieldInfos.size();
			source.append("\t\t\tfinal int temp = union_selection.ordinal();\n");
			source.append(MessageFormat.format("\t\t\tswitch (temp / {0,number,#}) '{'\n", maxFieldsLength));
			final int iterations = fullSize / maxFieldsLength;
			for (int iteration = 0; iteration <= iterations; iteration++) {
				final int start = iteration * maxFieldsLength;
				final int end = Math.min((iteration + 1) * maxFieldsLength - 1, fullSize - 1);
				source.append(MessageFormat.format("\t\t\tcase {0,number,#}:\n", iteration));
				if (iteration == 0) {
					source.append(MessageFormat.format("\t\t\t\tmp_field = temp == 0 ? mp_field = null : value_get_param_specific_helper_{0,number,#}_{1,number,#}(param_name);\n", start, end));
				} else if (iteration == iterations) {
					source.append(MessageFormat.format("\t\t\t\tmp_field = temp > {0,number,#} ? mp_field = null : value_get_param_specific_helper_{1,number,#}_{2,number,#}(param_name);\n", fullSize, start, end));
				} else {
					source.append(MessageFormat.format("\t\t\t\tmp_field = value_get_param_specific_helper_{0,number,#}_{1,number,#}(param_name);\n", start, end));
				}
				source.append("\t\t\t\tbreak;\n");
			}
			source.append("\t\t\tdefault:\n");
			source.append("\t\t\t\tmp_field = null;\n");
			source.append("\t\t\t}\n");
		} else {
			source.append("\t\t\tswitch(union_selection) {\n");
			for (int i = 0 ; i < fieldInfos.size(); i++) {
				final FieldInfo fieldInfo = fieldInfos.get(i);
				source.append(MessageFormat.format("\t\t\tcase ALT_{0}:\n", fieldInfo.mJavaVarName));
				source.append(MessageFormat.format("\t\t\t\tmp_field = get_field_{0}().get_param(param_name);\n", fieldInfo.mJavaVarName));
				source.append(MessageFormat.format("\t\t\t\tmp_field.set_id(new Module_Param_FieldName(\"{0}\"));\n", fieldInfo.mDisplayName));
				source.append("\t\t\t\tbreak;\n");
			}
			source.append("\t\t\tdefault:\n");
			source.append("\t\t\t\tmp_field = null;\n");
			source.append("\t\t\t}\n");
		}
		source.append("\t\t\tfinal Module_Param_Assignment_List mp = new Module_Param_Assignment_List();\n");
		source.append("\t\t\tmp.add_elem(mp_field);\n");
		source.append("\t\t\treturn mp;\n");
		source.append("\t\t}\n\n");
	}

	/**
	 * Generate set_implicit_omit.
	 *
	 * @param source
	 *                where the source code is to be generated.
	 * @param fieldInfos
	 *                the list of information about the fields.
	 */
	private static void generateValueSetImplicitOmit(final StringBuilder source, final List<FieldInfo> fieldInfos) {
		source.append("\t\t@Override\n");
		source.append("\t\tpublic void set_implicit_omit() {\n");
		source.append("\t\t\tswitch (union_selection) {\n");
		for (int i = 0 ; i < fieldInfos.size(); i++) {
			final FieldInfo fieldInfo = fieldInfos.get(i);

			source.append(MessageFormat.format("\t\t\tcase ALT_{0}:\n", fieldInfo.mJavaVarName));
		}
		source.append("\t\t\t\tfield.set_implicit_omit();\n");
		source.append("\t\t\t\tbreak;\n");

		source.append("\t\t\tdefault:\n");
		source.append("\t\t\t\tbreak;\n");
		source.append("\t\t\t}\n");
		source.append("\t\t}\n\n");
	}

	/**
	 * Generate encode_text/decode_text
	 *
	 * @param source
	 *                where the source code is to be generated.
	 * @param genName
	 *                the name of the generated class representing the
	 *                union/choice type.
	 * @param displayName
	 *                the user readable name of the type to be generated.
	 * @param fieldInfos
	 *                the list of information about the fields.
	 * */
	private static void generateValueEncodeDecodeText(final StringBuilder source, final String genName, final String displayName, final List<FieldInfo> fieldInfos) {
		source.append("\t\t@Override\n");
		source.append("\t\tpublic void encode_text(final Text_Buf text_buf) {\n");
		source.append("\t\t\tswitch (union_selection) {\n");
		for (int i = 0 ; i < fieldInfos.size(); i++) {
			//TODO could this be optimized?
			final FieldInfo fieldInfo = fieldInfos.get(i);
			source.append(MessageFormat.format("\t\t\tcase ALT_{0}:\n", fieldInfo.mJavaVarName));
			source.append(MessageFormat.format("\t\t\t\ttext_buf.push_int({0,number,#});\n", i + 1));
			source.append("\t\t\t\tbreak;\n");
		}

		source.append("\t\t\tdefault:\n");
		source.append(MessageFormat.format("\t\t\t\tthrow new TtcnError(\"Text encoder: Encoding an unbound value of union type {0}.\");\n", displayName));
		source.append("\t\t\t}\n");
		if (!fieldInfos.isEmpty()) {
			source.append("\t\t\tfield.encode_text(text_buf);\n");
		}
		source.append("\t\t}\n\n");

		source.append("\t\t@Override\n");
		source.append("\t\tpublic void decode_text(final Text_Buf text_buf) {\n");
		source.append("\t\t\tfinal int temp = text_buf.pull_int().get_int();\n");
		source.append("\t\t\tswitch (temp) {\n");
		for (int i = 0 ; i < fieldInfos.size(); i++) {
			final FieldInfo fieldInfo = fieldInfos.get(i);
			source.append(MessageFormat.format("\t\t\tcase {0,number,#}:\n", i + 1));
			source.append(MessageFormat.format("\t\t\t\tget_field_{0}().decode_text(text_buf);\n", fieldInfo.mJavaVarName));
			source.append("\t\t\t\tbreak;\n");
		}

		source.append("\t\t\tdefault:\n");
		source.append(MessageFormat.format("\t\t\t\tthrow new TtcnError(\"Text decoder: Unrecognized union selector was received for type {0}.\");\n", displayName));
		source.append("\t\t\t}\n");
		source.append("\t\t}\n\n");
	}

	/**
	 * Generate encode/decode
	 *
	 * @param source
	 *                where the source code is to be generated.
	 * @param genName
	 *                the name of the generated class representing the
	 *                union/choice type.
	 * @param displayName
	 *                the user readable name of the type to be generated.
	 * @param fieldInfos
	 *                the list of information about the fields.
	 * @param rawNeeded
	 *                {@code true} if encoding/decoding for RAW is to be
	 *                generated.
	 * @param hasRaw
	 *                {@code true} if the union has raw attributes.
	 * @param raw
	 *                the raw attributes or null.
	 * */
	private static void generateValueEncodeDecode(final StringBuilder source, final String genName, final String displayName,
			final List<FieldInfo> fieldInfos, final boolean rawNeeded, final boolean hasRaw, final RawASTStruct raw) {
		source.append("\t\t@Override\n");
		source.append("\t\tpublic void encode(final TTCN_Typedescriptor p_td, final TTCN_Buffer p_buf, final coding_type p_coding, final int flavour) {\n");
		source.append("\t\t\tswitch (p_coding) {\n");
		source.append("\t\t\tcase CT_RAW: {\n");
		source.append("\t\t\t\tfinal TTCN_EncDec_ErrorContext errorContext = new TTCN_EncDec_ErrorContext(\"While RAW-encoding type '%s': \", p_td.name);\n");
		source.append("\t\t\t\ttry{\n");
		source.append("\t\t\t\t\tif (p_td.raw == null) {\n");
		source.append("\t\t\t\t\t\tTTCN_EncDec_ErrorContext.error_internal(\"No RAW descriptor available for type '%s'.\", p_td.name);\n");
		source.append("\t\t\t\t\t}\n");
		source.append("\t\t\t\t\tfinal RAW_enc_tr_pos tree_position = new RAW_enc_tr_pos(0, null);\n");
		source.append("\t\t\t\t\tfinal RAW_enc_tree root = new RAW_enc_tree(true, null, tree_position, 1, p_td.raw);\n");
		source.append("\t\t\t\t\tRAW_encode(p_td, root);\n");
		source.append("\t\t\t\t\troot.put_to_buf(p_buf);\n");
		source.append("\t\t\t\t} finally {\n");
		source.append("\t\t\t\t\terrorContext.leave_context();\n");
		source.append("\t\t\t\t}\n");
		source.append("\t\t\t\tbreak;\n");
		source.append("\t\t\t}\n");

		source.append("\t\t\tcase CT_JSON: {\n");
		source.append("\t\t\t\tfinal TTCN_EncDec_ErrorContext errorContext = new TTCN_EncDec_ErrorContext(\"While JSON-encoding type '%s': \", p_td.name);\n");
		source.append("\t\t\t\ttry{\n");
		source.append("\t\t\t\t\tif(p_td.json == null) {\n");
		source.append("\t\t\t\t\t\tTTCN_EncDec_ErrorContext.error_internal(\"No JSON descriptor available for type '%s'.\", p_td.name);\n");
		source.append("\t\t\t\t\t}\n");
		source.append("\t\t\t\t\tfinal JSON_Tokenizer tok = new JSON_Tokenizer(flavour != 0);\n");
		source.append("\t\t\t\t\tJSON_encode(p_td, tok);\n");
		source.append("\t\t\t\t\tfinal StringBuilder temp = tok.get_buffer();\n");
		source.append("\t\t\t\t\tfor (int i = 0; i < temp.length(); i++) {\n");
		source.append("\t\t\t\t\t\tfinal int temp2 = temp.charAt(i);\n");
		source.append("\t\t\t\t\t\tp_buf.put_c((byte)temp2);\n");
		source.append("\t\t\t\t\t}\n");
		source.append("\t\t\t\t} finally {\n");
		source.append("\t\t\t\t\terrorContext.leave_context();\n");
		source.append("\t\t\t\t}\n");
		source.append("\t\t\t\tbreak;\n");
		source.append("\t\t\t}\n");

		source.append("\t\t\tdefault:\n");
		source.append("\t\t\t\tthrow new TtcnError(MessageFormat.format(\"Unknown coding method requested to encode type `{0}''\", p_td.name));\n");
		source.append("\t\t\t}\n");
		source.append("\t\t}\n\n");

		source.append("\t\t@Override\n");
		source.append("\t\tpublic void decode(final TTCN_Typedescriptor p_td, final TTCN_Buffer p_buf, final coding_type p_coding, final int flavour) {\n");
		source.append("\t\t\tswitch (p_coding) {\n");
		source.append("\t\t\tcase CT_RAW: {\n");
		source.append("\t\t\t\tfinal TTCN_EncDec_ErrorContext errorContext = new TTCN_EncDec_ErrorContext(\"While RAW-decoding type '%s': \", p_td.name);\n");
		source.append("\t\t\t\ttry{\n");
		source.append("\t\t\t\t\tif (p_td.raw == null) {\n");
		source.append("\t\t\t\t\t\tTTCN_EncDec_ErrorContext.error_internal(\"No RAW descriptor available for type '%s'.\", p_td.name);\n");
		source.append("\t\t\t\t\t}\n");
		source.append("\t\t\t\t\tfinal raw_order_t order = p_td.raw.top_bit_order == top_bit_order_t.TOP_BIT_LEFT ? raw_order_t.ORDER_LSB : raw_order_t.ORDER_MSB;\n");
		source.append("\t\t\t\t\tfinal int rawr = RAW_decode(p_td, p_buf, p_buf.get_len() * 8, order);\n");
		source.append("\t\t\t\t\tif (rawr < 0) {\n");
		source.append("\t\t\t\t\t\tfinal error_type temp = error_type.values()[-rawr];\n");
		source.append("\t\t\t\t\t\tswitch (temp) {\n");
		source.append("\t\t\t\t\t\tcase ET_INCOMPL_MSG:\n");
		source.append("\t\t\t\t\t\tcase ET_LEN_ERR:\n");
		source.append("\t\t\t\t\t\t\tTTCN_EncDec_ErrorContext.error(temp, \"Can not decode type '%s', because invalid or incomplete message was received\", p_td.name);\n");
		source.append("\t\t\t\t\t\t\tbreak;\n");
		source.append("\t\t\t\t\t\tcase ET_UNBOUND:\n");
		source.append("\t\t\t\t\t\tdefault:\n");
		source.append("\t\t\t\t\t\t\tTTCN_EncDec_ErrorContext.error(error_type.ET_INVAL_MSG, \"Can not decode type '%s', because invalid or incomplete message was received\", p_td.name);\n");
		source.append("\t\t\t\t\t\t\tbreak;\n");
		source.append("\t\t\t\t\t\t}\n");
		source.append("\t\t\t\t\t}\n");
		source.append("\t\t\t\t} finally {\n");
		source.append("\t\t\t\t\terrorContext.leave_context();\n");
		source.append("\t\t\t\t}\n");
		source.append("\t\t\t\tbreak;\n");
		source.append("\t\t\t}\n");

		source.append("\t\t\tcase CT_JSON: {\n");
		source.append("\t\t\t\tfinal TTCN_EncDec_ErrorContext errorContext = new TTCN_EncDec_ErrorContext(\"While JSON-decoding type '%s': \", p_td.name);\n");
		source.append("\t\t\t\ttry{\n");
		source.append("\t\t\t\t\tif(p_td.json == null) {\n");
		source.append("\t\t\t\t\t\tTTCN_EncDec_ErrorContext.error_internal(\"No JSON descriptor available for type '%s'.\", p_td.name);\n");
		source.append("\t\t\t\t\t}\n");
		source.append("\t\t\t\t\tfinal byte[] data = p_buf.get_data();\n");
		source.append("\t\t\t\t\tfinal char[] temp = new char[data.length];\n");
		source.append("\t\t\t\t\tfor (int i = 0; i < data.length; i++) {\n");
		source.append("\t\t\t\t\t\ttemp[i] = (char)data[i];\n");
		source.append("\t\t\t\t\t}\n");
		source.append("\t\t\t\t\tfinal JSON_Tokenizer tok = new JSON_Tokenizer(new String(temp), p_buf.get_len());\n");
		source.append("\t\t\t\t\tif(JSON_decode(p_td, tok, false) < 0) {\n");
		source.append("\t\t\t\t\t\tTTCN_EncDec_ErrorContext.error(error_type.ET_INCOMPL_MSG, \"Can not decode type '%s', because invalid or incomplete message was received\", p_td.name);\n");
		source.append("\t\t\t\t\t}\n");
		source.append("\t\t\t\t\tp_buf.set_pos(tok.get_buf_pos());\n");
		source.append("\t\t\t\t} finally {\n");
		source.append("\t\t\t\t\terrorContext.leave_context();\n");
		source.append("\t\t\t\t}\n");
		source.append("\t\t\t\tbreak;\n");
		source.append("\t\t\t}\n");

		source.append("\t\t\tdefault:\n");
		source.append("\t\t\t\tthrow new TtcnError(MessageFormat.format(\"Unknown coding method requested to decode type `{0}''\", p_td.name));\n");
		source.append("\t\t\t}\n");
		source.append("\t\t}\n\n");

		if (rawNeeded) {
			final int tag_type[] = new int[fieldInfos.size()];
			Arrays.fill(tag_type, 0);
			if (hasRaw && raw != null && raw.taglist != null && raw.taglist.list != null) { // fill tag_type. 0-No tag, >0 index of the tag + 1
				for (int i = 0; i < raw.taglist.list.size(); i++) {
					final rawAST_coding_taglist tempTaglist = raw.taglist.list.get(i);
					if (tempTaglist.fields != null && tempTaglist.fields.size() > 0) {
						boolean found = false;
						for (int v = 0; v < tempTaglist.fields.size(); v++) {
							if (tempTaglist.fields.get(v).start_pos >= 0) {
								found = true;
								break;
							}
						}
						if (found) {
							tag_type[tempTaglist.fieldnum] = i + 1;
						} else {
							tag_type[tempTaglist.fieldnum] = -i + 1;
						}
					}
				}
			}

			if (fieldInfos.size() > maxFieldsLength) {
				final int fullSize = fieldInfos.size();
				final int iterations = fullSize / maxFieldsLength;
				for (int iteration = 0; iteration <= iterations; iteration++) {
					final int start = iteration * maxFieldsLength ;
					final int end = Math.min((iteration + 1) * maxFieldsLength - 1, fullSize - 1);
					source.append("\t\t// Internal helper function.\n");
					source.append(MessageFormat.format("\t\tprivate int RAW_encode_helper_{0,number,#}_{1,number,#}(final RAW_enc_tree myleaf) '{'\n", start, end));
					source.append("\t\t\tint encoded_length = 0;\n");
					source.append("\t\t\tswitch (union_selection) {\n");
					for (int i = start ; i <= end; i++) {
						final FieldInfo fieldInfo = fieldInfos.get(i);

						source.append(MessageFormat.format("\t\t\tcase ALT_{0}:\n", fieldInfo.mJavaVarName));
						source.append(MessageFormat.format("\t\t\t\tmyleaf.nodes[{0,number,#}] = new RAW_enc_tree(true, myleaf, myleaf.curr_pos, {0,number,#}, {1}_descr_);\n", i, fieldInfo.mTypeDescriptorName));
						source.append(MessageFormat.format("\t\t\t\tencoded_length = field.RAW_encode({0}_descr_, myleaf.nodes[{1,number,#}]);\n", fieldInfo.mTypeDescriptorName, i));

						final int t_type = tag_type[i] > 0 ? tag_type[i] : -tag_type[i];
						if (t_type > 0 && raw.taglist.list.get(t_type - 1).fields.size() > 0) {
							final rawAST_coding_taglist cur_choice = raw.taglist.list.get(t_type - 1);
							source.append(" if (");
							genRawFieldChecker(source, cur_choice, false);
							source.append(" ) {\n");
							genRawTagChecker(source, cur_choice);
							source.append("}\n");
						}

						source.append("\t\t\t\tbreak;\n");
					}
					source.append("\t\t\tdefault:\n");
					source.append("\t\t\t\tbreak;\n");
					source.append("\t\t\t}\n");
					source.append("\t\t\treturn encoded_length;\n");
					source.append("\t\t}\n\n");
				}
			}


			source.append("@Override\n");
			source.append("/** {@inheritDoc} */\n");
			source.append("public int RAW_encode(final TTCN_Typedescriptor p_td, final RAW_enc_tree myleaf) {\n");
			source.append("int encoded_length = 0;\n");
			source.append("myleaf.isleaf = false;\n");
			source.append(MessageFormat.format("myleaf.num_of_nodes = {0,number,#};\n", fieldInfos.size()));
			source.append(MessageFormat.format("myleaf.nodes = new RAW_enc_tree[{0,number,#}];\n", fieldInfos.size()));
			if (fieldInfos.size() > maxFieldsLength) {
				final int fullSize = fieldInfos.size();
				source.append("final int temp = union_selection.ordinal();\n");
				source.append(MessageFormat.format("if (temp == 0 || temp > {0,number,#}) '{'\n", fullSize)); //1689 meg jo, fullSize
				source.append("TTCN_EncDec_ErrorContext.error(error_type.ET_UNBOUND, \"Encoding an unbound value.\", \"\");\n");
				source.append("}\n");
				source.append(MessageFormat.format("switch (temp / {0,number,#}) '{'\n", maxFieldsLength));
				final int iterations = fullSize / maxFieldsLength;
				for (int iteration = 0; iteration <= iterations; iteration++) {
					final int start = iteration * maxFieldsLength;
					final int end = Math.min((iteration + 1) * maxFieldsLength - 1, fullSize - 1);
					source.append(MessageFormat.format("case {0,number,#}:\n", iteration));
					source.append(MessageFormat.format("return RAW_encode_helper_{0,number,#}_{1,number,#}(myleaf);\n", start, end));
				}
				source.append("}\n");
			} else {
				source.append("switch (union_selection) {\n");
				for (int i = 0 ; i < fieldInfos.size(); i++) {
					final FieldInfo fieldInfo = fieldInfos.get(i);
					source.append(MessageFormat.format("case ALT_{0}:\n", fieldInfo.mJavaVarName));
					source.append(MessageFormat.format("myleaf.nodes[{0,number,#}] = new RAW_enc_tree(true, myleaf, myleaf.curr_pos, {0,number,#}, {1}_descr_.raw);\n", i, fieldInfo.mTypeDescriptorName));
					source.append(MessageFormat.format("encoded_length = field.RAW_encode({0}_descr_, myleaf.nodes[{1,number,#}]);\n", fieldInfo.mTypeDescriptorName, i));
					source.append(MessageFormat.format("myleaf.nodes[{0,number,#}].coding_descr = {1}_descr_;\n", i, fieldInfo.mTypeDescriptorName));

					final int t_type = tag_type[i] > 0 ? tag_type[i] : -tag_type[i];
					if (t_type > 0 && raw.taglist.list.get(t_type - 1).fields.size() > 0) {
						final rawAST_coding_taglist cur_choice = raw.taglist.list.get(t_type - 1);
						source.append(" if (");
						genRawFieldChecker(source, cur_choice, false);
						source.append(" ) {\n");
						genRawTagChecker(source, cur_choice);
						source.append("}\n");
					}

					source.append("break;\n");
				}
				source.append("default:\n");
				source.append("TTCN_EncDec_ErrorContext.error(error_type.ET_UNBOUND, \"Encoding an unbound value.\", \"\");\n");
				source.append("}\n");
			}

			source.append("return encoded_length;\n");
			source.append("}\n\n");

			if (fieldInfos.size() > maxFieldsLength) {
				final int fullSize = fieldInfos.size();
				final int iterations = fullSize / maxFieldsLength;
				for (int iteration = 0; iteration <= iterations; iteration++) {
					final int start = iteration * maxFieldsLength ;
					final int end = Math.min((iteration + 1) * maxFieldsLength - 1, fullSize - 1);
					source.append("\t\t// Internal helper function.\n");
					source.append(MessageFormat.format("\t\tprivate int RAW_decode_helper_{0,number,#}_{1,number,#}(final TTCN_Buffer buff, final int limit, final raw_order_t top_bit_ord, final boolean no_err, final int sel_field, final boolean first_call, final RAW_Force_Omit force_omit) '{'\n", start, end));
					source.append("\t\t\tint decoded_length = 0;\n");
					source.append("\t\t\tswitch (sel_field) {\n");
					for (int i = start ; i <= end; i++) {
						final FieldInfo fieldInfo = fieldInfos.get(i);

						source.append(MessageFormat.format("\t\t\tcase {0,number,#}: '{'\n", i));
						source.append(MessageFormat.format("\t\t\t\tfinal RAW_Force_Omit field_force_omit = new RAW_Force_Omit({0,number,#}, force_omit, {1}_descr_.raw.forceomit);\n", i, fieldInfo.mTypeDescriptorName));
						source.append(MessageFormat.format("\t\t\t\tdecoded_length = get_field_{0}().RAW_decode({1}_descr_, buff, limit, top_bit_ord, no_err, -1, true, field_force_omit);\n", fieldInfo.mJavaVarName, fieldInfo.mTypeDescriptorName));
						source.append("\t\t\t\tbreak;\n");
						source.append("\t\t\t}\n");
					}
					source.append("\t\t\tdefault:\n");
					source.append("\t\t\t\tbreak;\n");
					source.append("\t\t\t}\n");
					source.append("\t\t\treturn decoded_length;\n");
					source.append("\t\t}\n\n");

					source.append("\t\t// Internal helper function.\n");
					source.append(MessageFormat.format("\t\tprivate int RAW_decode_helper2_{0,number,#}_{1,number,#}(final TTCN_Buffer buff, final int limit, final raw_order_t top_bit_ord, final boolean no_err, final int sel_field, final boolean first_call, final RAW_Force_Omit force_omit, final int starting_pos) '{'\n", start, end));
					source.append("\t\t\tint decoded_length = 0;\n");
					for (int i = start ; i <= end; i++) {
						if (tag_type[i] == 0) {
							final FieldInfo fieldInfo = fieldInfos.get(i);
							source.append("\t\t\tbuff.set_pos_bit(starting_pos);\n");
							source.append(MessageFormat.format("\t\t\tfinal RAW_Force_Omit field_{0,number,#}_force_omit = new RAW_Force_Omit({0,number,#}, force_omit, {1}_descr_.raw.forceomit);\n", i, fieldInfo.mTypeDescriptorName));
							source.append(MessageFormat.format("\t\t\tdecoded_length = get_field_{0}().RAW_decode({1}_descr_, buff, limit, top_bit_ord, true, -1, true, field_{2,number,#}_force_omit);\n", fieldInfo.mJavaVarName, fieldInfo.mTypeDescriptorName, i));
							source.append("\t\t\tif (decoded_length >= 0) {\n");
							source.append("\t\t\t\treturn decoded_length;\n");
							source.append("\t\t\t}\n");
						}
					}

					source.append("\t\t\treturn -1;\n");
					source.append("\t\t}\n\n");
				}
			}

			source.append("@Override\n");
			source.append("/** {@inheritDoc} */\n");
			source.append("public int RAW_decode(final TTCN_Typedescriptor p_td, final TTCN_Buffer buff, int limit, final raw_order_t top_bit_ord, final boolean no_err, final int sel_field, final boolean first_call, final RAW_Force_Omit force_omit) {\n");
			source.append("final int prepaddlength = buff.increase_pos_padd(p_td.raw.prepadding);\n");
			source.append("limit -= prepaddlength;\n");
			source.append("int decoded_length = 0;\n");
			source.append("final int starting_pos = buff.get_pos_bit();\n");
			source.append("if (sel_field != -1) {\n");
			if (fieldInfos.size() > maxFieldsLength) {
				source.append(MessageFormat.format("switch (sel_field / {0,number,#}) '{'\n", maxFieldsLength));
				final int fullSize = fieldInfos.size();
				final int iterations = fullSize / maxFieldsLength;
				for (int iteration = 0; iteration <= iterations; iteration++) {
					final int start = iteration * maxFieldsLength;
					final int end = Math.min((iteration + 1) * maxFieldsLength - 1, fullSize - 1);
					source.append(MessageFormat.format("case {0,number,#}:\n", iteration));
					source.append(MessageFormat.format("decoded_length = RAW_decode_helper_{0,number,#}_{1,number,#}(buff, limit, top_bit_ord, no_err, sel_field, first_call, force_omit);\n", start, end));
					source.append("break;\n");
				}
				source.append("}\n");
			} else {
				source.append("switch (sel_field) {\n");
				for (int i = 0 ; i < fieldInfos.size(); i++) {
					final FieldInfo fieldInfo = fieldInfos.get(i);
					source.append(MessageFormat.format("case {0,number,#}: '{'\n", i));
					source.append(MessageFormat.format("final RAW_Force_Omit field_force_omit = new RAW_Force_Omit({0,number,#}, force_omit, {1}_descr_.raw.forceomit);\n", i, fieldInfo.mTypeDescriptorName));
					source.append(MessageFormat.format("decoded_length = get_field_{0}().RAW_decode({1}_descr_, buff, limit, top_bit_ord, no_err, -1, true, field_force_omit);\n", fieldInfo.mJavaVarName, fieldInfo.mTypeDescriptorName));
					source.append("break;\n");
					source.append("}\n");
				}
				source.append("default:\n");
				source.append("break;\n");
				source.append("}\n");
			}
			source.append("return decoded_length + buff.increase_pos_padd(p_td.raw.padding) + prepaddlength;\n");
			source.append("} else {\n");
			for(int i = 0; i < fieldInfos.size(); i++) {
				if (tag_type[i] > 0 && raw.taglist.list.get(tag_type[i] - 1).fields.size() > 0) {
					source.append("boolean already_failed = false;\n");
					break;
				}
			}

			/* pre-calculate what we know about the temporal variables*/
			final ArrayList<TemporalVariable> tempVariableList = new ArrayList<UnionGenerator.TemporalVariable>();
			for(int i = 0; i < fieldInfos.size(); i++) {
				if (tag_type[i] > 0 && raw.taglist.list.get(tag_type[i] - 1).fields.size() > 0) {
					final rawAST_coding_taglist cur_choice = raw.taglist.list.get(tag_type[i] - 1);
					for (int j = 0; j < cur_choice.fields.size(); j++) {
						final rawAST_coding_field_list fieldlist = cur_choice.fields.get(j);
						if (fieldlist.start_pos >= 0) {
							final rawAST_coding_fields lastCodingFields = fieldlist.fields.get(fieldlist.fields.size() - 1);
							boolean found = false;
							for (int k = 0; k < tempVariableList.size(); k++) {
								final TemporalVariable temporalVariable = tempVariableList.get(k);
								if (temporalVariable.start_pos == fieldlist.start_pos && temporalVariable.typedescriptor.equals(lastCodingFields.typedesc)) {
									temporalVariable.use_counter++;
									fieldlist.temporal_variable_index = k;
									found = true;
									break;
								}
							}
							if (!found) {
								final TemporalVariable temp = new TemporalVariable();
								temp.type = lastCodingFields.type;
								temp.typedescriptor = lastCodingFields.typedesc;
								temp.start_pos = fieldlist.start_pos;
								temp.use_counter = 1;
								temp.decoded_for_element = -1;
								fieldlist.temporal_variable_index = tempVariableList.size();
								tempVariableList.add(temp);
							}
						}
					}
				}
			}

			for (int i = 0; i < tempVariableList.size(); i++) {
				final TemporalVariable tempVariable = tempVariableList.get(i);
				if (tempVariable.use_counter > 1) {
					source.append(MessageFormat.format("final {0} temporal_{1} = new {0}();\n", tempVariable.type, i));
					source.append(MessageFormat.format("int decoded_{0}_length = 0;\n", i));
				}
			}

			for (int i = 0 ; i < fieldInfos.size(); i++) { /* fields with tag */
				if (tag_type[i] > 0 && raw.taglist.list.get(tag_type[i] - 1).fields.size() > 0) {
					final FieldInfo fieldInfo = fieldInfos.get(i);
					final rawAST_coding_taglist cur_choice = raw.taglist.list.get(tag_type[i] - 1);

					//TODO already_failed handling could be optimized!
					source.append("already_failed = false;\n");
					/* try to decode those key variables whose position we know
					 * this way we might be able to step over bad values faster
					 */
					for (int j = 0; j < cur_choice.fields.size(); j++) {
						final rawAST_coding_field_list cur_field_list = cur_choice.fields.get(j);
						if (cur_field_list.start_pos >= 0) {
							final int variableIndex = cur_field_list.temporal_variable_index;
							final TemporalVariable tempVariable = tempVariableList.get(variableIndex);
							if (tempVariable.decoded_for_element == i) {
								continue;
							}

							source.append("if (!already_failed) {\n");
							if (tempVariable.use_counter == 1) {
								source.append(MessageFormat.format("final {0} temporal_{1} = new {0}();\n", tempVariable.type, variableIndex));
								source.append(MessageFormat.format("int decoded_{0}_length;\n", variableIndex));
							}
							if (tempVariable.decoded_for_element == -1) {
								source.append(MessageFormat.format("buff.set_pos_bit(starting_pos + {0});\n", cur_field_list.start_pos));
								source.append(MessageFormat.format("decoded_{0}_length = temporal_{0}.RAW_decode({1}_descr_, buff, limit, top_bit_ord, true, -1, true, null);\n", variableIndex, tempVariable.typedescriptor));
							}
							tempVariable.decoded_for_element = i;
							source.append(MessageFormat.format("if (decoded_{0}_length > 0) '{'\n", variableIndex));
							source.append(MessageFormat.format("if (temporal_{0}.operator_equals({1})", variableIndex, cur_field_list.nativeExpression.expression));
							for (int k = j + 1; k < cur_choice.fields.size(); k++) {
								final rawAST_coding_field_list tempFieldList = cur_choice.fields.get(k);
								if (tempFieldList.temporal_variable_index == variableIndex) {
									source.append(MessageFormat.format(" || temporal_{0}.operator_equals({1})", variableIndex, tempFieldList.nativeExpression.expression));
								}
							}
							source.append(") {\n");
							source.append("buff.set_pos_bit(starting_pos);\n");
							source.append(MessageFormat.format("final RAW_Force_Omit field_force_omit = new RAW_Force_Omit({0}, force_omit, {1}_descr_.raw.forceomit);\n", i, fieldInfo.mTypeDescriptorName));
							source.append(MessageFormat.format("decoded_length = get_field_{0}().RAW_decode({1}_descr_, buff, limit, top_bit_ord, true, -1, true, field_force_omit);\n", fieldInfo.mJavaVarName, fieldInfo.mTypeDescriptorName));
							source.append("if (decoded_length > 0) {\n");
							source.append("if (");
							genRawFieldChecker(source, cur_choice, true);
							source.append(") {\n");
							source.append("return decoded_length + buff.increase_pos_padd(p_td.raw.padding) + prepaddlength;\n");
							source.append("} else {\n");
							source.append("already_failed = true;\n");
							source.append("}\n");
							source.append("}\n");
							source.append("}\n");
							source.append("}\n");
							source.append("}\n");
						}
					}
					/* if there is one tag key whose position we don't know
					 * and we couldn't decide yet if the element can be decoded or not
					 * than we have to decode it.
					 * note that this is not actually a cycle because of the break
					 */
					for (int j = 0; j < cur_choice.fields.size(); j++) {
						if (cur_choice.fields.get(j).start_pos < 0) {
							source.append("if (already_failed) {\n");
							source.append("buff.set_pos_bit(starting_pos);\n");
							source.append(MessageFormat.format("final RAW_Force_Omit field_force_omit = new RAW_Force_Omit({0}, force_omit, {1}_descr_.raw.forceomit);\n", i, fieldInfo.mTypeDescriptorName));
							source.append(MessageFormat.format("decoded_length = get_field_{0}().RAW_decode({1}_descr_, buff, limit, top_bit_ord, true, -1, true, field_force_omit);\n", fieldInfo.mJavaVarName, fieldInfo.mTypeDescriptorName));
							source.append("if (decoded_length > 0) {\n");
							source.append("if (");
							genRawFieldChecker(source, cur_choice, true);
							source.append(") {\n");
							source.append("return decoded_length + buff.increase_pos_padd(p_td.raw.padding) + prepaddlength;\n");
							source.append("}\n");
							source.append("}\n");
							source.append("}\n");

							break;
						}
					}
				}
			}

			for (int i = 0 ; i < fieldInfos.size(); i++) {
				if (tag_type[i] < 0 && raw.taglist.list.get(-1 * tag_type[i] - 1).fields.size() > 0) {
					final FieldInfo fieldInfo = fieldInfos.get(i);
					final rawAST_coding_taglist cur_choice = raw.taglist.list.get(-1 * tag_type[i] - 1);

					source.append("buff.set_pos_bit(starting_pos);\n");
					source.append(MessageFormat.format("final RAW_Force_Omit field_{0,number,#}_force_omit = new RAW_Force_Omit({0,number,#}, force_omit, {1}_descr_.raw.forceomit);\n", i, fieldInfo.mTypeDescriptorName));
					source.append(MessageFormat.format("decoded_length = get_field_{0}().RAW_decode({1}_descr_, buff, limit, top_bit_ord, true, -1, true, field_{2,number,#}_force_omit);\n", fieldInfo.mJavaVarName, fieldInfo.mTypeDescriptorName, i));
					source.append("if (decoded_length >= 0) {\n");
					source.append("if (");
					genRawFieldChecker(source, cur_choice, true);
					source.append(") {\n");
					source.append("return decoded_length + buff.increase_pos_padd(p_td.raw.padding) + prepaddlength;\n");
					source.append("}\n");
					source.append("}\n");
				}
			}
			if (fieldInfos.size() > maxFieldsLength) {
				final int fullSize = fieldInfos.size();
				final int iterations = fullSize / maxFieldsLength;
				for (int iteration = 0; iteration <= iterations; iteration++) {
					final int start = iteration * maxFieldsLength;
					final int end = Math.min((iteration + 1) * maxFieldsLength - 1, fullSize - 1);
					source.append(MessageFormat.format("\t\t\t\tdecoded_length = RAW_decode_helper2_{0,number,#}_{1,number,#}(buff, limit, top_bit_ord, no_err, sel_field, first_call, force_omit, starting_pos);\n", start, end));
					source.append("\t\t\t\tif (decoded_length >= 0) {\n");
					source.append("\t\t\t\t\treturn decoded_length + buff.increase_pos_padd(p_td.raw.padding) + prepaddlength;\n");
					source.append("\t\t\t\t}\n");
				}
			} else {
				for (int i = 0 ; i < fieldInfos.size(); i++) {
					if (tag_type[i] == 0) {
						final FieldInfo fieldInfo = fieldInfos.get(i);
						source.append("buff.set_pos_bit(starting_pos);\n");
						source.append(MessageFormat.format("final RAW_Force_Omit field_{0,number,#}_force_omit = new RAW_Force_Omit({0,number,#}, force_omit, {1}_descr_.raw.forceomit);\n", i, fieldInfo.mTypeDescriptorName));
						source.append(MessageFormat.format("decoded_length = get_field_{0}().RAW_decode({1}_descr_, buff, limit, top_bit_ord, true, -1, true, field_{2,number,#}_force_omit);\n", fieldInfo.mJavaVarName, fieldInfo.mTypeDescriptorName, i));
						source.append("if (decoded_length >= 0) {\n");
						source.append("return decoded_length + buff.increase_pos_padd(p_td.raw.padding) + prepaddlength;\n");
						source.append("}\n");
					}
				}
			}

			source.append("}\n");
			source.append("clean_up();\n");
			source.append("return -1;\n");
			source.append("}\n\n");
		}
	}

	/**
	 * Generate JSON encode/decode
	 *
	 * @param source
	 *                where the source code is to be generated.
	 * @param genName
	 *                the name of the generated class representing the
	 *                union/choice type.
	 * @param displayName
	 *                the user readable name of the type to be generated.
	 * @param fieldInfos
	 *                the list of information about the fields.
	 * @param jsonAsValue
	 *                true if this type is a field of a union with the "as value" coding instruction
	 */
	private static void generateValueJsonEncodeDecode(final StringBuilder source, final String genName, final String displayName,
			final List<FieldInfo> fieldInfos, final boolean jsonAsValue) {

		//TODO: implement RT2
		final boolean use_runtime_2 = false;
		
		// JSON encode
		source.append("\t\t@Override\n");
		source.append("\t\t/** {@inheritDoc} *"+"/\n");
		source.append("\t\tpublic int JSON_encode(final TTCN_Typedescriptor p_td, final JSON_Tokenizer p_tok, final boolean p_parent_is_map) {\n");
		if ( fieldInfos.size() > 0 ) {
			if (use_runtime_2) {
				source.append("\t\t\tif (err_descr) {\n");
				source.append("\t\t\t\treturn JSON_encode_negtest(err_descr, p_td, p_tok);\n");
				source.append("\t\t\t}\n");
			}
			if (!jsonAsValue) {
				// 'as value' is not set for the base type, but it might still be set in
				// the type descriptor
				source.append("\t\t\tfinal boolean as_value = p_td.json.isAs_value();\n");
				source.append("\t\t\tint enc_len = as_value ? 0 : p_tok.put_next_token(json_token_t.JSON_TOKEN_OBJECT_START, null);\n");
			} else {
				source.append("\t\t\tint enc_len = 0;\n");
			}
			source.append("\t\t\tswitch(union_selection) {\n");

			for (int i = 0; i < fieldInfos.size(); i++) {
				final FieldInfo fieldInfo = fieldInfos.get(i);
				source.append(MessageFormat.format("\t\t\tcase ALT_{0}:\n", fieldInfo.mJavaVarName));
				if (!jsonAsValue) {
					source.append("\t\t\t\tif (!as_value) {\n");
					source.append(MessageFormat.format("\t\t\t\t\tenc_len += p_tok.put_next_token(json_token_t.JSON_TOKEN_NAME, \"{0}\");\n", fieldInfo.jsonAlias != null ? fieldInfo.jsonAlias : fieldInfo.mDisplayName));
					source.append("\t\t\t\t}\n");

				}
				source.append(MessageFormat.format("\t\t\t\tenc_len += get_field_{0}().JSON_encode({1}_descr_, p_tok);\n", fieldInfo.mJavaVarName, fieldInfo.mTypeDescriptorName));
				source.append("\t\t\t\tbreak;\n");
			}
			source.append("\t\t\tdefault:\n");
			source.append(MessageFormat.format("\t\t\t\tTTCN_EncDec_ErrorContext.error(error_type.ET_UNBOUND, \"Encoding an unbound value of type {0}.\");\n", displayName));
			source.append("\t\t\t\treturn -1;\n");
			source.append("\t\t\t}\n\n");

			if (!jsonAsValue) {
				source.append("\t\t\tif (!as_value) {\n");
				source.append("\t\t\t\tenc_len += p_tok.put_next_token(json_token_t.JSON_TOKEN_OBJECT_END, null);\n");
				source.append("\t\t\t}\n");
			}
			source.append("\t\t\treturn enc_len;\n");
			source.append("}\n\n");
		}
		else {
			source.append("\t\t\tTTCN_EncDec_ErrorContext.error(error_type.ET_UNBOUND, \n");
			source.append(MessageFormat.format("\t\t\t\t\"Cannot encode union of type {0}, because it has zero alternatives.\");\n", displayName));
			source.append("\t\t\treturn -1;\n");
			source.append("}\n\n");   
		}

		if (use_runtime_2) {
			// JSON encode for negative testing
			source.append("\t\t@Override\n");
			source.append("\t\t/** {@inheritDoc} *"+"/\n");
			source.append("\t\tpublic int JSON_encode_negtest(final Erroneous_descriptor ed, final TTCN_Typedescriptor td, final JSON_Tokenizer jt) {\n");
			if (fieldInfos.size() > 0) {
				if (!jsonAsValue) {
					// 'as value' is not set for the base type, but it might still be set in
					// the type descriptor
					source.append("\t\t\tfinal boolean as_value = p_td.json.as_value;\n");
					source.append("\t\t\tint enc_len = as_value ? 0 : p_tok.put_next_token(json_token_t.JSON_TOKEN_OBJECT_START, null);\n");
				} else {
					source.append("\t\t\tint enc_len = 0;\n\n");
				}
				source.append("\t\t\tconst Erroneous_values_t* err_vals = null;\n");
				source.append("\t\t\tconst Erroneous_descriptor_t* emb_descr = null;\n");
				source.append("\t\t\tswitch(union_selection) {\n");

				for (int i = 0; i < fieldInfos.size(); i++) {
					final FieldInfo fieldInfo = fieldInfos.get(i);
					source.append(MessageFormat.format("\t\t\tcase ALT_{0}:\n", fieldInfo.mJavaVarName));
					source.append(MessageFormat.format("\t\t\t\terr_vals = p_err_descr.get_field_err_values({0});\n", i));
					source.append(MessageFormat.format("\t\t\t\temb_descr = p_err_descr.get_field_emb_descr({0});\n", i));
					source.append("\t\t\t\tif (null != err_vals && null != err_vals.value) {\n");
					source.append("\t\t\t\t\tif (null != err_vals.value.errval) {\n");
					source.append("\t\t\t\t\t\tif(err_vals.value.raw){\n");
					source.append("\t\t\t\t\t\t\tenc_len += err_vals.value.errval.JSON_encode_negtest_raw(p_tok);\n");
					source.append("\t\t\t\t\t\t} else {\n");
					source.append("\t\t\t\t\t\t\tif (null == err_vals.value.type_descr) {\n");
					source.append("\t\t\t\t\t\t\t\tTTCN_error(\"internal error: erroneous value typedescriptor missing\");\n");
					source.append("\t\t\t\t\t\t\t}\n");
					if (!jsonAsValue) {
						source.append("\t\t\t\t\t\t\tif (!as_value) {\n");
						source.append(MessageFormat.format("\t\t\t\t\t\t\t\tenc_len += p_tok.put_next_token(json_token_t.JSON_TOKEN_NAME, \"{0}\");\n", fieldInfo.jsonAlias != null ? fieldInfo.jsonAlias : fieldInfo.mDisplayName));
						source.append("\t\t\t\t\t\t\t}\n");
					}
					source.append("\t\t\t\t\t\t\tenc_len += err_vals.value.errval.JSON_encode(*err_vals.value.type_descr, p_tok);\n");
					source.append("\t\t\t\t\t\t}\n");
					source.append("\t\t\t\t\t}\n");
					source.append("\t\t\t\t} else {\n");
					if (!jsonAsValue) {
						source.append("\t\t\t\t\tif (!as_value) {\n");
						source.append(MessageFormat.format("\t\t\t\t\t\tenc_len += p_tok.put_next_token(json_token_t.JSON_TOKEN_NAME, \"{0}\");\n", fieldInfo.jsonAlias != null ? fieldInfo.jsonAlias : fieldInfo.mDisplayName));
						source.append("\t\t\t\t\t}\n");
					}
					source.append("\t\t\t\t\tif (null != emb_descr) {\n");
					source.append(MessageFormat.format("\t\t\t\t\t\tenc_len += get_field_{0}().JSON_encode_negtest(emb_descr, {1}_descr_, p_tok);\n", fieldInfo.mJavaVarName, fieldInfo.mTypeDescriptorName));
					source.append("\t\t\t\t\t} else {\n");
					source.append(MessageFormat.format("\t\t\t\t\t\tenc_len += get_field_{0}().JSON_encode({1}_descr_, p_tok);\n", fieldInfo.mJavaVarName, fieldInfo.mTypeDescriptorName));
					source.append("\t\t\t\t\t}\n");
					source.append("\t\t\t\t}\n");
					source.append("\t\t\t\tbreak;\n");
				}
				source.append("\t\t\tdefault:\n");
				source.append("\t\t\t\tTTCN_EncDec_ErrorContext.error(error_type.ET_UNBOUND, \n");
				source.append(MessageFormat.format("\t\t\t\t\t\"Encoding an unbound value of type {0}.\");\n", displayName));
				source.append("\t\t\t\treturn -1;\n");
				source.append("\t\t\t}\n\n");
				if (!jsonAsValue) {
					source.append("\t\t\tif (!as_value) {\n");
					source.append("\t\t\t\tenc_len += p_tok.put_next_token(json_token_t.JSON_TOKEN_OBJECT_END, null);\n");
					source.append("}\n");
				}
				source.append("\t\t\treturn enc_len;\n");
				source.append("}\n\n");
			}
			else {
				source.append("\t\t\tTTCN_EncDec_ErrorContext.error(error_type.ET_UNBOUND, \n");
				source.append(MessageFormat.format("\t\t\t\t\"Cannot encode union of type {0}, because it has zero alternatives.\");\n", displayName));
				source.append("\t\t\treturn -1;\n");
				source.append("\t\t}\n\n");
			}
		}

		// JSON decode
		source.append("\t\t@Override\n");
		source.append("\t\t/** {@inheritDoc} *"+"/\n");
		source.append("\t\tpublic int JSON_decode(final TTCN_Typedescriptor p_td, final JSON_Tokenizer p_tok, final boolean p_silent, final boolean p_parent_is_map, final int p_chosen_field) {\n");
		if (fieldInfos.size() > 0) {
			source.append(MessageFormat.format("\t\t\tif (0 <= p_chosen_field && {0,number,#} > p_chosen_field) '{'\n", fieldInfos.size()));
			source.append("\t\t\t\tswitch (p_chosen_field) {\n");

			for (int i = 0; i < fieldInfos.size(); i++) {
				final FieldInfo fieldInfo = fieldInfos.get(i);
				source.append(MessageFormat.format("\t\t\t\tcase {0,number,#}:\n", i));
				source.append(MessageFormat.format("\t\t\t\t\treturn get_field_{0}().JSON_decode({1}_descr_, p_tok, true);\n", fieldInfo.mJavaVarName, fieldInfo.mTypeDescriptorName));
			}
			source.append("\t\t\t\t}\n");
			source.append("\t\t\t}\n");
			source.append("\t\t\tfinal AtomicReference<json_token_t> j_token = new AtomicReference<json_token_t>(json_token_t.JSON_TOKEN_NONE);\n");
			if (!jsonAsValue) {
				source.append("\t\t\tif (p_td.json.isAs_value()) {\n");
			}
			source.append("\t\t\t\tfinal int buf_pos = p_tok.get_buf_pos();\n");
			source.append("\t\t\t\tp_tok.get_next_token(j_token, null, null);\n");
			source.append("\t\t\t\tint ret_val = 0;\n");
			source.append("\t\t\t\tswitch(j_token.get()) {\n");
			source.append("\t\t\t\tcase JSON_TOKEN_NUMBER: {\n");
			for (int i = 0; i < fieldInfos.size(); i++) {
				final FieldInfo fieldInfo = fieldInfos.get(i);
				if ((Type.JSON_NUMBER & fieldInfo.jsonValueType) != 0) {
					source.append("\t\t\t\t\tp_tok.set_buf_pos(buf_pos);\n");
					source.append(MessageFormat.format("\t\t\t\t\tret_val = get_field_{0}().JSON_decode({1}_descr_, p_tok, true);\n", fieldInfo.mJavaVarName, fieldInfo.mTypeDescriptorName));
					source.append("\t\t\t\t\tif (0 <= ret_val) {\n");
					source.append("\t\t\t\t\t\treturn ret_val;\n");
					source.append("\t\t\t\t\t}\n");
				}
			}
			source.append("\t\t\t\t\tif (!p_silent) {\n");
			source.append("\t\t\t\t\t\tTTCN_EncDec_ErrorContext.error(error_type.ET_INVAL_MSG, JSON.JSON_DEC_AS_VALUE_ERROR, \"number\");\n");
			source.append("\t\t\t\t\t}\n");
			source.append("\t\t\t\t\tclean_up();\n");
			source.append("\t\t\t\t\treturn JSON.JSON_ERROR_FATAL;\n");
			source.append("\t\t\t\t}\n");
			source.append("\t\t\t\tcase JSON_TOKEN_STRING: {\n");
			for (int i = 0; i < fieldInfos.size(); i++) {
				final FieldInfo fieldInfo = fieldInfos.get(i);
				if ((Type.JSON_STRING & fieldInfo.jsonValueType) != 0) {
					source.append("\t\t\t\t\tp_tok.set_buf_pos(buf_pos);\n");
					source.append(MessageFormat.format("\t\t\t\t\tret_val = get_field_{0}().JSON_decode({1}_descr_, p_tok, true);\n", fieldInfo.mJavaVarName, fieldInfo.mTypeDescriptorName));
					source.append("\t\t\t\t\tif (0 <= ret_val) {\n");
					source.append("\t\t\t\t\t\treturn ret_val;\n");
					source.append("\t\t\t\t\t}\n");
				}
			}
			source.append("\t\t\t\t\tif (!p_silent) {\n");
			source.append("\t\t\t\t\t\tTTCN_EncDec_ErrorContext.error(error_type.ET_INVAL_MSG, JSON.JSON_DEC_AS_VALUE_ERROR, \"string\");\n");
			source.append("\t\t\t\t\t}\n");
			source.append("\t\t\t\t\tclean_up();\n");
			source.append("\t\t\t\t\treturn JSON.JSON_ERROR_FATAL;\n");
			source.append("\t\t\t\t}\n");
			source.append("\t\t\t\tcase JSON_TOKEN_LITERAL_TRUE:\n");
			source.append("\t\t\t\tcase JSON_TOKEN_LITERAL_FALSE: {\n");
			for (int i = 0; i < fieldInfos.size(); i++) {
				final FieldInfo fieldInfo = fieldInfos.get(i);
				if ((Type.JSON_BOOLEAN & fieldInfo.jsonValueType) != 0) {
					source.append("\t\t\t\t\tp_tok.set_buf_pos(buf_pos);\n");
					source.append(MessageFormat.format("\t\t\t\tret_val = get_field_{0}().JSON_decode({1}_descr_, p_tok, true);\n", fieldInfo.mJavaVarName, fieldInfo.mTypeDescriptorName));
					source.append("\t\t\t\t\tif (0 <= ret_val) {\n");
					source.append("\t\t\t\t\t\treturn ret_val;\n");
					source.append("\t\t\t\t\t}\n");
				}
			}
			source.append("\t\t\t\t\tfinal String literal_str = \"literal (\" + ((json_token_t.JSON_TOKEN_LITERAL_TRUE == j_token.get()) ? \"true\" : \"false\") + \")\";\n");
			source.append("\t\t\t\t\tif (!p_silent) {\n");
			source.append("\t\t\t\t\t\tTTCN_EncDec_ErrorContext.error(error_type.ET_INVAL_MSG, JSON.JSON_DEC_AS_VALUE_ERROR, literal_str);\n");
			source.append("\t\t\t\t\t}\n");
			source.append("\t\t\t\t\tclean_up();\n");
			source.append("\t\t\t\t\treturn JSON.JSON_ERROR_FATAL;\n");
			source.append("\t\t\t\t}\n");
			source.append("\t\t\t\tcase JSON_TOKEN_ARRAY_START: {\n");
			for (int i = 0; i < fieldInfos.size(); i++) {
				final FieldInfo fieldInfo = fieldInfos.get(i);
				if ((Type.JSON_ARRAY & fieldInfo.jsonValueType) != 0) {
					source.append("\t\t\t\t\tp_tok.set_buf_pos(buf_pos);\n");
					source.append(MessageFormat.format("\t\t\t\tret_val = get_field_{0}().JSON_decode({1}_descr_, p_tok, true);\n", fieldInfo.mJavaVarName, fieldInfo.mTypeDescriptorName));
					source.append("\t\t\t\t\tif (0 <= ret_val) {\n");
					source.append("\t\t\t\t\t\treturn ret_val;\n");
					source.append("\t\t\t\t\t}\n");
				}
			}
			source.append("\t\t\t\t\tif (!p_silent) {\n");
			source.append("\t\t\t\t\t\tTTCN_EncDec_ErrorContext.error(error_type.ET_INVAL_MSG, JSON.JSON_DEC_AS_VALUE_ERROR, \"array\");\n");
			source.append("\t\t\t\t\t}\n");
			source.append("\t\t\t\t\tclean_up();\n");
			source.append("\t\t\t\t\treturn JSON.JSON_ERROR_FATAL;\n");
			source.append("\t\t\t\t}\n");
			source.append("\t\t\t\tcase JSON_TOKEN_OBJECT_START: {\n");
			for (int i = 0; i < fieldInfos.size(); i++) {
				final FieldInfo fieldInfo = fieldInfos.get(i);
				if ((Type.JSON_OBJECT & fieldInfo.jsonValueType) != 0) {
					source.append("\t\t\t\t\tp_tok.set_buf_pos(buf_pos);\n");
					source.append(MessageFormat.format("\t\t\t\t\tret_val = get_field_{0}().JSON_decode({1}_descr_, p_tok, true);\n", fieldInfo.mJavaVarName, fieldInfo.mTypeDescriptorName));
					source.append("\t\t\t\t\tif (0 <= ret_val) {\n");
					source.append("\t\t\t\t\t\treturn ret_val;\n");
					source.append("\t\t\t\t\t}\n");
				}
			}
			source.append("\t\t\t\t\tif (!p_silent) {\n");
			source.append("\t\t\t\t\t\tTTCN_EncDec_ErrorContext.error(error_type.ET_INVAL_MSG, JSON.JSON_DEC_AS_VALUE_ERROR, \"object\");\n");
			source.append("\t\t\t\t\t}\n");
			source.append("\t\t\t\t\tclean_up();\n");
			source.append("\t\t\t\t\treturn JSON.JSON_ERROR_FATAL;\n");
			source.append("\t\t\t\t}\n");
			source.append("\t\t\t\tcase JSON_TOKEN_LITERAL_NULL: {\n");
			for (int i = 0; i < fieldInfos.size(); i++) {
				final FieldInfo fieldInfo = fieldInfos.get(i);
				if ((Type.JSON_NULL & fieldInfo.jsonValueType) != 0) {
					source.append("\t\t\t\t\tp_tok.set_buf_pos(buf_pos);\n");
					source.append(MessageFormat.format("\t\t\t\t\tret_val = get_field_{0}().JSON_decode({1}_descr_, p_tok, true);\n", fieldInfo.mJavaVarName, fieldInfo.mTypeDescriptorName));
					source.append("\t\t\t\t\tif (0 <= ret_val) {\n");
					source.append("\t\t\t\t\t\treturn ret_val;\n");
					source.append("\t\t\t\t\t}\n");
				}
			}
			source.append("\t\t\t\t\tclean_up();\n");
			// the caller might be able to decode the null value if it's an optional field
			// only return an invalid token error, not a fatal error
			source.append("\t\t\t\t\treturn JSON.JSON_ERROR_INVALID_TOKEN;\n");
			source.append("\t\t\t\t}\n");
			source.append("\t\t\t\tcase JSON_TOKEN_ERROR:\n");
			source.append("\t\t\t\t\tTTCN_EncDec_ErrorContext.error(error_type.ET_INVAL_MSG, JSON.JSON_DEC_BAD_TOKEN_ERROR, \"\");\n");
			source.append("\t\t\t\t\treturn JSON.JSON_ERROR_FATAL;\n");
			source.append("\t\t\t\tdefault:\n");
			source.append("\t\t\t\t\treturn JSON.JSON_ERROR_INVALID_TOKEN;\n");
			source.append("\t\t\t\t}\n");
			if (!jsonAsValue) {
				source.append("\t\t\t\t}\n");
				source.append("\t\t\t\telse {\n"); // if there is no 'as value' set in the type descriptor
				source.append("\t\t\t\tint dec_len = p_tok.get_next_token(j_token, null, null);\n");
				source.append("\t\t\t\tif (json_token_t.JSON_TOKEN_ERROR == j_token.get()) {\n");
				source.append("\t\t\t\t\tTTCN_EncDec_ErrorContext.error(error_type.ET_INVAL_MSG, JSON.JSON_DEC_BAD_TOKEN_ERROR, \"\");\n");
				source.append("\t\t\t\t\treturn JSON.JSON_ERROR_FATAL;\n");
				source.append("\t\t\t\t}\n");
				source.append("\t\t\t\telse if (json_token_t.JSON_TOKEN_OBJECT_START != j_token.get()) {\n");
				source.append("\t\t\t\t\treturn JSON.JSON_ERROR_INVALID_TOKEN;\n");
				source.append("\t\t\t\t}\n\n");
				source.append("\t\t\t\tfinal StringBuilder fld_name = new StringBuilder();\n");
				source.append("\t\t\t\tfinal AtomicInteger name_len = new AtomicInteger(0);\n");
				source.append("\t\t\t\tdec_len += p_tok.get_next_token(j_token, fld_name, name_len);\n");
				source.append("\t\t\t\tif (json_token_t.JSON_TOKEN_NAME != j_token.get()) {\n");
				source.append("\t\t\t\t\tTTCN_EncDec_ErrorContext.error(error_type.ET_INVAL_MSG, JSON.JSON_DEC_NAME_TOKEN_ERROR);\n");
				source.append("\t\t\t\t\treturn JSON.JSON_ERROR_FATAL;\n");
				source.append("\t\t\t\t} else {\n");
				source.append("\t\t\t\t\t");
				for (int i = 0; i < fieldInfos.size(); i++) {
					final FieldInfo fieldInfo = fieldInfos.get(i);
					final String fieldName = fieldInfo.jsonAlias != null ? fieldInfo.jsonAlias : fieldInfo.mDisplayName; 
					source.append(MessageFormat.format("if ({0} == name_len.get() && \"{1}\".equals(fld_name.substring(0,name_len.get()))) '{'\n",
							fieldName.length(), fieldName));
					source.append(MessageFormat.format("\t\t\t\t\t\tfinal int ret_val = get_field_{0}().JSON_decode({1}_descr_, p_tok, p_silent);\n", fieldInfo.mJavaVarName, fieldInfo.mTypeDescriptorName));
					source.append("\t\t\t\t\t\tif (0 > ret_val) {\n");
					source.append("\t\t\t\t\t\t\tif (JSON.JSON_ERROR_INVALID_TOKEN == ret_val) {\n");
					source.append(MessageFormat.format("\t\t\t\t\t\t\t\tTTCN_EncDec_ErrorContext.error(error_type.ET_INVAL_MSG, JSON.JSON_DEC_FIELD_TOKEN_ERROR, \"{0}\");\n",
							fieldInfo.mDisplayName));
					source.append("\t\t\t\t\t\t\t}\n");
					source.append("\t\t\t\t\t\t\treturn JSON.JSON_ERROR_FATAL;\n");
					source.append("\t\t\t\t\t\t} else {\n");
					source.append("\t\t\t\t\t\t\tdec_len += ret_val;\n");
					source.append("\t\t\t\t\t\t}\n");
					source.append("\t\t\t\t\t} else ");
				}
				source.append("{\n");
				source.append("\t\t\t\t\t\tTTCN_EncDec_ErrorContext.error(error_type.ET_INVAL_MSG, JSON.JSON_DEC_INVALID_NAME_ERROR, fld_name);\n");
				source.append("\t\t\t\t\t\treturn JSON.JSON_ERROR_FATAL;\n");
				source.append("\t\t\t\t\t}\n");
				source.append("\t\t\t\t}\n\n");
				source.append("\t\t\t\tdec_len += p_tok.get_next_token(j_token, null, null);\n");
				source.append("\t\t\t\tif (json_token_t.JSON_TOKEN_OBJECT_END != j_token.get()) {\n");
				source.append("\t\t\t\t\tif (!p_silent) {\n");
				source.append("\t\t\t\t\t\tTTCN_EncDec_ErrorContext.error(error_type.ET_INVAL_MSG, JSON.JSON_DEC_STATIC_OBJECT_END_TOKEN_ERROR, \"\");\n");
				source.append("\t\t\t\t\t}\n");
				source.append("\t\t\t\t\treturn JSON.JSON_ERROR_FATAL;\n");
				source.append("\t\t\t\t}\n\n");
				source.append("\t\t\t\treturn dec_len;\n");
				source.append("\t\t\t}\n");
			}
			source.append("\t\t}\n\n");
		}
		else { // no fields
			source.append("\t\t\tif (!p_silent) {\n");
			source.append("\t\t\t\tTTCN_EncDec_ErrorContext.error(error_type.ET_INVAL_MSG, \n");
			source.append(MessageFormat.format("\t\t\t\t\"Cannot decode union of type {0}, because it has zero alternatives.\");\n", displayName));
			source.append("\t\t\t}\n");
			source.append("\t\t\treturn JSON.JSON_ERROR_FATAL;\n");
			source.append("\t\t}\n\n");
		}
	}

	/**
	 * Generate member variables
	 *
	 * @param source
	 *                where the source code is to be generated.
	 * @param genName
	 *                the name of the generated class representing the
	 *                union/choice type.
	 * @param fieldInfos
	 *                the list of information about the fields.
	 * */
	private static void generateTemplateDeclaration(final StringBuilder source, final String genName, final List<FieldInfo> fieldInfos) {
		source.append("\t\t//if single value which value?\n");
		source.append(MessageFormat.format("\t\tprivate {0}.union_selection_type single_value_union_selection;\n", genName));
		if (!fieldInfos.isEmpty()) {
			source.append("\t\t//originally a union which can not be mapped to Java\n");
			source.append("\t\tprivate Base_Template single_value;\n");
		}
		source.append("\t\t// value_list part\n");
		source.append(MessageFormat.format("\t\tprivate ArrayList<{0}_template> value_list;\n\n", genName));
	}

	/**
	 * Generate constructors
	 *
	 * @param aData
	 *                used to access build settings.
	 * @param source
	 *                where the source code is to be generated.
	 * @param genName
	 *                the name of the generated class representing the
	 *                union/choice type.
	 * */
	private static void generateTemplateConstructors( final JavaGenData aData, final StringBuilder source, final String genName){
		if (aData.isDebug()) {
			source.append("\t\t/**\n");
			source.append("\t\t * Initializes to unbound/uninitialized template.\n");
			source.append("\t\t * */\n");
		}
		source.append(MessageFormat.format("\t\tpublic {0}_template() '{'\n", genName));
		source.append("\t\t\t// do nothing\n");
		source.append("\t\t}\n");

		if (aData.isDebug()) {
			source.append("\t\t/**\n");
			source.append("\t\t * Initializes to a given template kind.\n");
			source.append("\t\t *\n");
			source.append("\t\t * @param other_value\n");
			source.append("\t\t *                the template kind to initialize to.\n");
			source.append("\t\t * */\n");
		}
		source.append(MessageFormat.format("\t\tpublic {0}_template(final template_sel other_value) '{'\n", genName));
		source.append("\t\t\tsuper(other_value);\n");
		source.append("\t\t\tcheck_single_selection(other_value);\n");
		source.append("\t\t}\n");

		if (aData.isDebug()) {
			source.append("\t\t/**\n");
			source.append("\t\t * Initializes to a given value.\n");
			source.append("\t\t * The template becomes a specific template.\n");
			source.append("\t\t * The elements of the provided value are copied.\n");
			source.append("\t\t *\n");
			source.append("\t\t * @param other_value\n");
			source.append("\t\t *                the value to initialize to.\n");
			source.append("\t\t * */\n");
		}
		source.append(MessageFormat.format("\t\tpublic {0}_template(final {0} other_value) '{'\n", genName));
		source.append("\t\t\tcopy_value(other_value);\n");
		source.append("\t\t}\n");

		if (aData.isDebug()) {
			source.append("\t\t/**\n");
			source.append("\t\t * Initializes to a given template.\n");
			source.append("\t\t * The elements of the provided template are copied.\n");
			source.append("\t\t *\n");
			source.append("\t\t * @param other_value\n");
			source.append("\t\t *                the value to initialize to.\n");
			source.append("\t\t * */\n");
		}
		source.append(MessageFormat.format("\t\tpublic {0}_template(final {0}_template other_value) '{'\n", genName));
		source.append("\t\t\tcopy_template(other_value);\n");
		source.append("\t\t}\n\n");
	}

	/**
	 * Generate the copy_value and copy_template functions
	 *
	 * @param aData
	 *                used to access build settings.
	 * @param source
	 *                where the source code is to be generated.
	 * @param genName
	 *                the name of the generated class representing the
	 *                union/choice type.
	 * @param displayName
	 *                the user readable name of the type to be generated.
	 * @param fieldInfos
	 *                the list of information about the fields.
	 * */
	private static void generatetemplateCopyValue(final JavaGenData aData, final StringBuilder source, final String genName, final String displayName, final List<FieldInfo> fieldInfos) {
		if (aData.isDebug()) {
			source.append("\t\t/**\n");
			source.append("\t\t * Internal function to copy the provided value into this template.\n");
			source.append("\t\t * The template becomes a specific value template.\n");
			source.append("\t\t * The already existing content is overwritten.\n");
			source.append("\t\t *\n");
			source.append("\t\t * @param other_value the value to be copied.\n");
			source.append("\t\t * */\n");
		}
		source.append(MessageFormat.format("\t\tprivate void copy_value(final {0} other_value) '{'\n", genName));
		source.append("\t\t\tsingle_value_union_selection = other_value.get_selection();\n");
		if (!fieldInfos.isEmpty()) {
			source.append("\t\t\tswitch (other_value.get_selection()) {\n");
			for (int i = 0 ; i < fieldInfos.size(); i++) {
				final FieldInfo fieldInfo = fieldInfos.get(i);
				source.append(MessageFormat.format("\t\t\tcase ALT_{0}:\n", fieldInfo.mJavaVarName));
				source.append(MessageFormat.format("\t\t\t\tsingle_value = new {0}(other_value.constGet_field_{1}());\n", fieldInfo.mJavaTemplateName, fieldInfo.mJavaVarName));
				source.append("\t\t\t\tbreak;\n");
			}
			source.append("\t\t\tdefault:\n");
			source.append(MessageFormat.format("\t\t\t\tthrow new TtcnError(\"Initializing a template with an unbound value of type {0}.\");\n", displayName));
			source.append("\t\t\t}\n");
		}
		source.append("\t\t\tset_selection(template_sel.SPECIFIC_VALUE);\n");
		source.append("\t\t}\n\n");

		if (aData.isDebug()) {
			source.append("\t\t/**\n");
			source.append("\t\t * Internal function to copy the provided template into this template.\n");
			source.append("\t\t * The already existing content is overwritten.\n");
			source.append("\t\t *\n");
			source.append("\t\t * @param other_value the value to be copied.\n");
			source.append("\t\t * */\n");
		}
		source.append(MessageFormat.format("\t\tprivate void copy_template(final {0}_template other_value) '{'\n", genName));
		source.append("\t\t\tswitch (other_value.template_selection) {\n");
		source.append("\t\t\tcase SPECIFIC_VALUE:\n");
		source.append("\t\t\t\tsingle_value_union_selection = other_value.single_value_union_selection;\n");
		if (!fieldInfos.isEmpty()) {
			source.append("\t\t\t\tswitch (single_value_union_selection) {\n");
			for (int i = 0 ; i < fieldInfos.size(); i++) {
				final FieldInfo fieldInfo = fieldInfos.get(i);
				source.append(MessageFormat.format("\t\t\t\tcase ALT_{0}:\n", fieldInfo.mJavaVarName));
				source.append(MessageFormat.format("\t\t\t\t\tsingle_value = new {0}(other_value.constGet_field_{1}());\n", fieldInfo.mJavaTemplateName, fieldInfo.mJavaVarName));
				source.append("\t\t\t\t\tbreak;\n");
			}
			source.append("\t\t\t\tdefault:\n");
			source.append(MessageFormat.format("\t\t\t\t\tthrow new TtcnError(\"Internal error: Invalid union selector in a specific value when copying a template of type {0}.\");\n", displayName));
			source.append("\t\t\t\t}\n");
		}
		source.append("\t\t\t\tbreak;\n");
		source.append("\t\t\tcase OMIT_VALUE:\n");
		source.append("\t\t\tcase ANY_VALUE:\n");
		source.append("\t\t\tcase ANY_OR_OMIT:\n");
		source.append("\t\t\t\tbreak;\n");
		source.append("\t\t\tcase VALUE_LIST:\n");
		source.append("\t\t\tcase COMPLEMENTED_LIST:\n");
		source.append(MessageFormat.format("\t\t\t\tvalue_list = new ArrayList<{0}_template>(other_value.value_list.size());\n", genName));
		source.append("\t\t\t\tfor(int i = 0; i < other_value.value_list.size(); i++) {\n");
		source.append(MessageFormat.format("\t\t\t\t\tfinal {0}_template temp = new {0}_template(other_value.value_list.get(i));\n", genName));
		source.append("\t\t\t\t\tvalue_list.add(temp);\n");
		source.append("\t\t\t\t}\n");
		source.append("\t\t\t\tbreak;\n");
		source.append("\t\t\tdefault:\n");
		source.append(MessageFormat.format("\t\t\t\tthrow new TtcnError(\"Copying an uninitialized template of union type {0}.\");\n", displayName));
		source.append("\t\t\t}\n");
		source.append("\t\t\tset_selection(other_value);\n");
		source.append("\t\t}\n\n");
	}

	/**
	 * Generate the clean_up function
	 *
	 * @param source
	 *                where the source code is to be generated.
	 * @param fieldInfos
	 *                the list of information about the fields.
	 * */
	private static void generateTemplateCleanup(final StringBuilder source, final List<FieldInfo> fieldInfos) {
		source.append("\t\t@Override\n");
		source.append("\t\tpublic void clean_up() {\n");
		source.append("\t\t\tswitch (template_selection) {\n");
		source.append("\t\t\tcase SPECIFIC_VALUE:\n");
		if (!fieldInfos.isEmpty()) {
			source.append("\t\t\t\tswitch (single_value_union_selection) {\n");
			for (int i = 0 ; i < fieldInfos.size(); i++) {
				//TODO could this be optimized?
				final FieldInfo fieldInfo = fieldInfos.get(i);
				source.append(MessageFormat.format("\t\t\t\tcase ALT_{0}:\n", fieldInfo.mJavaVarName));
				source.append(MessageFormat.format("\t\t\t\t\t(({0})single_value).clean_up();\n", fieldInfo.mJavaTemplateName));
				source.append("\t\t\t\t\tbreak;\n");
			}
			source.append("\t\t\t\tdefault:\n");
			source.append("\t\t\t\t\tbreak;\n");
			source.append("\t\t\t\t}\n");
			source.append("\t\t\t\tsingle_value = null;\n");
		}
		source.append("\t\t\t\tbreak;\n");
		source.append("\t\t\tcase VALUE_LIST:\n");
		source.append("\t\t\tcase COMPLEMENTED_LIST:\n");
		source.append("\t\t\t\tvalue_list.clear();\n");
		source.append("\t\t\t\tvalue_list = null;\n");
		source.append("\t\t\t\tbreak;\n");
		source.append("\t\t\tdefault:\n");
		source.append("\t\t\t\tbreak;\n");
		source.append("\t\t\t}\n");
		source.append("\t\t\ttemplate_selection = template_sel.UNINITIALIZED_TEMPLATE;\n");
		source.append("\t\t}\n\n");
	}

	/**
	 * Generate assign functions
	 *
	 * @param aData
	 *                used to access build settings.
	 * @param source
	 *                where the source code is to be generated.
	 * @param genName
	 *                the name of the generated class representing the
	 *                union/choice type.
	 * */
	private static void generateTemplateoperator_assign(final JavaGenData aData, final StringBuilder source, final String genName) {
		source.append("\t\t@Override\n");
		source.append(MessageFormat.format("\t\tpublic {0}_template operator_assign(final template_sel otherValue ) '{'\n", genName));
		source.append("\t\t\tcheck_single_selection(otherValue);\n");
		source.append("\t\t\tclean_up();\n");
		source.append("\t\t\tset_selection(otherValue);\n");
		source.append("\t\t\treturn this;\n");
		source.append("\t\t}\n\n");

		if ( aData.isDebug() ) {
			source.append("\t\t/**\n");
			source.append("\t\t * Assigns the other value to this template.\n");
			source.append("\t\t * Overwriting the current content in the process.\n");
			source.append("\t\t *<p>\n");
			source.append("\t\t * operator= in the core.\n");
			source.append("\t\t *\n");
			source.append("\t\t * @param otherValue\n");
			source.append("\t\t *                the other value to assign.\n");
			source.append("\t\t * @return the new template object.\n");
			source.append("\t\t */\n");
		}
		source.append(MessageFormat.format("\t\tpublic {0}_template operator_assign(final {0} otherValue ) '{'\n", genName));
		source.append("\t\t\tclean_up();\n");
		source.append("\t\t\tcopy_value(otherValue);\n");
		source.append("\t\t\treturn this;\n");
		source.append("\t\t}\n\n");

		if ( aData.isDebug() ) {
			source.append("\t\t/**\n");
			source.append("\t\t * Assigns the other template to this template.\n");
			source.append("\t\t * Overwriting the current content in the process.\n");
			source.append("\t\t *<p>\n");
			source.append("\t\t * operator= in the core.\n");
			source.append("\t\t *\n");
			source.append("\t\t * @param otherValue\n");
			source.append("\t\t *                the other value to assign.\n");
			source.append("\t\t * @return the new template object.\n");
			source.append("\t\t */\n");
		}
		source.append(MessageFormat.format("\t\tpublic {0}_template operator_assign(final {0}_template otherValue ) '{'\n", genName));
		source.append("\t\t\tif (otherValue != this) {\n");
		source.append("\t\t\t\tclean_up();\n");
		source.append("\t\t\t\tcopy_template(otherValue);\n");
		source.append("\t\t\t}\n");
		source.append("\t\t\treturn this;\n");
		source.append("\t\t}\n\n");

		source.append("\t\t@Override\n");
		source.append(MessageFormat.format("\t\tpublic {0}_template operator_assign(final Base_Type otherValue ) '{'\n", genName));
		source.append(MessageFormat.format("\t\t\tif (otherValue instanceof {0}) '{'\n", genName));
		source.append(MessageFormat.format("\t\t\t\treturn operator_assign(({0})otherValue);\n", genName));
		source.append("\t\t\t}\n");
		source.append(MessageFormat.format("\t\t\tthrow new TtcnError(\"Internal Error: value can not be cast to {0}.\");\n", genName));
		source.append("\t\t}\n\n");

		source.append("\t\t@Override\n");
		source.append(MessageFormat.format("\t\tpublic {0}_template operator_assign(final Base_Template otherValue ) '{'\n", genName));
		source.append(MessageFormat.format("\t\t\tif (otherValue instanceof {0}_template) '{'\n", genName));
		source.append(MessageFormat.format("\t\t\t\treturn operator_assign(({0}_template)otherValue);\n", genName));
		source.append("\t\t\t}\n");
		source.append(MessageFormat.format("\t\t\tthrow new TtcnError(\"Internal Error: value can not be cast to {0}_template.\");\n", genName));
		source.append("\t\t}\n\n");

		//FIXME implement optional parameter version
	}

	/**
	 * Generate the match function
	 *
	 * @param aData
	 *                used to access build settings.
	 * @param source
	 *                where the source code is to be generated.
	 * @param genName
	 *                the name of the generated class representing the
	 *                union/choice type.
	 * @param displayName
	 *                the user readable name of the type to be generated.
	 * @param fieldInfos
	 *                the list of information about the fields.
	 * */
	private static void generateTemplateMatch(final JavaGenData aData, final StringBuilder source, final String genName, final String displayName, final List<FieldInfo> fieldInfos) {
		if (aData.isDebug()) {
			source.append("\t\t/**\n");
			source.append("\t\t * Matches the provided value against this template.\n");
			source.append("\t\t *\n");
			source.append("\t\t * @param other_value the value to be matched.\n");
			source.append("\t\t * */\n");
		}
		source.append(MessageFormat.format("\t\tpublic boolean match(final {0} other_value) '{'\n", genName));
		source.append("\t\t\treturn match(other_value, false);\n");
		source.append("\t\t}\n\n");

		if (aData.isDebug()) {
			source.append("\t\t/**\n");
			source.append("\t\t * Matches the provided value against this template. In legacy mode\n");
			source.append("\t\t * omitted value fields are not matched against the template field.\n");
			source.append("\t\t *\n");
			source.append("\t\t * @param other_value\n");
			source.append("\t\t *                the value to be matched.\n");
			source.append("\t\t * @param legacy\n");
			source.append("\t\t *                use legacy mode.\n");
			source.append("\t\t * */\n");
		}
		source.append(MessageFormat.format("\t\tpublic boolean match(final {0} other_value, final boolean legacy) '{'\n", genName));
		source.append("\t\t\tif(!other_value.is_bound()) {\n");
		source.append("\t\t\t\treturn false;\n");
		source.append("\t\t\t}\n");

		source.append("\t\t\tswitch (template_selection) {\n");
		source.append("\t\t\tcase ANY_VALUE:\n");
		source.append("\t\t\tcase ANY_OR_OMIT:\n");
		source.append("\t\t\t\treturn true;\n");
		source.append("\t\t\tcase OMIT_VALUE:\n");
		source.append("\t\t\t\treturn false;\n");
		source.append("\t\t\tcase SPECIFIC_VALUE:\n");
		source.append(MessageFormat.format("\t\t\t\tfinal {0}.union_selection_type value_selection = other_value.get_selection();\n", genName));
		source.append(MessageFormat.format("\t\t\t\tif (value_selection == {0}.union_selection_type.UNBOUND_VALUE) '{'\n", genName));
		source.append("\t\t\t\t\treturn false;\n");
		source.append("\t\t\t\t}\n");
		source.append("\t\t\t\tif (value_selection != single_value_union_selection) {\n");
		source.append("\t\t\t\t\treturn false;\n");
		source.append("\t\t\t\t}\n");
		source.append("\t\t\t\tswitch (value_selection) {\n");
		for (int i = 0 ; i < fieldInfos.size(); i++) {
			final FieldInfo fieldInfo = fieldInfos.get(i);
			source.append(MessageFormat.format("\t\t\t\tcase ALT_{0}:\n", fieldInfo.mJavaVarName));
			source.append(MessageFormat.format("\t\t\t\t\treturn (({0})single_value).match(other_value.get_field_{1}(), legacy);\n", fieldInfo.mJavaTemplateName, fieldInfo.mJavaVarName));
		}

		source.append("\t\t\t\tdefault:\n");
		source.append(MessageFormat.format("\t\t\t\t\tthrow new TtcnError(\"Internal error: Invalid selector in a specific value when matching a template of union type {0}.\");\n", displayName));
		source.append("\t\t\t\t}\n");
		source.append("\t\t\tcase VALUE_LIST:\n");
		source.append("\t\t\tcase COMPLEMENTED_LIST: {\n");
		source.append("\t\t\t\tfinal int list_size = value_list.size();\n");
		source.append("\t\t\t\tfor(int i = 0 ; i < list_size; i++) {\n");
		source.append("\t\t\t\t\tif(value_list.get(i).match(other_value, legacy)) {\n");
		source.append("\t\t\t\t\t\treturn template_selection == template_sel.VALUE_LIST;\n");
		source.append("\t\t\t\t\t}\n");
		source.append("\t\t\t\t}\n");
		source.append("\t\t\t\treturn template_selection == template_sel.COMPLEMENTED_LIST;\n");
		source.append("\t\t\t}\n");
		source.append("\t\t\tdefault:\n");
		source.append("\t\t\t\tthrow new TtcnError(\"Matching with an uninitialized/unsupported integer template.\");\n");
		source.append("\t\t\t}\n");
		source.append("\t\t}\n\n");

		source.append("\t\t@Override\n");
		source.append( MessageFormat.format( "\t\tpublic boolean match(final Base_Type otherValue, final boolean legacy) '{'\n", genName ) );
		source.append( MessageFormat.format( "\t\t\tif (otherValue instanceof {0}) '{'\n", genName) );
		source.append( MessageFormat.format( "\t\t\t\treturn match(({0})otherValue, legacy);\n", genName) );
		source.append("\t\t\t}\n\n");
		source.append( MessageFormat.format( "\t\t\tthrow new TtcnError(\"Internal Error: The left operand of assignment is not of type {0}.\");\n", genName ) );
		source.append("\t\t}\n\n");
	}

	/**
	 * Generate the ischosen function
	 *
	 * @param aData
	 *                used to access build settings.
	 * @param source
	 *                where the source code is to be generated.
	 * @param genName
	 *                the name of the generated class representing the
	 *                union/choice type.
	 * @param displayName
	 *                the user readable name of the type to be generated.
	 * */
	private static void generateTemplateIsChosen(final JavaGenData aData, final StringBuilder source, final String genName, final String displayName) {
		if (aData.isDebug()) {
			source.append("\t\t/**\n");
			source.append("\t\t * Checks and reports whether the union has the provided alternative active or not.\n");
			source.append("\t\t *\n");
			source.append("\t\t * ischosen in the core.\n");
			source.append("\t\t *\n");
			source.append("\t\t * @param checked_selection the selection to check for.\n");
			source.append("\t\t *\n");
			source.append("\t\t * @return {@code true} if the unions has the provided selection active.\n");
			source.append("\t\t */\n");
		}
		source.append(MessageFormat.format("\t\tpublic boolean ischosen(final {0}.union_selection_type checked_selection) '{'\n", genName));
		source.append(MessageFormat.format("\t\t\tif(checked_selection == {0}.union_selection_type.UNBOUND_VALUE) '{'\n", genName));
		source.append(MessageFormat.format("\t\t\t\tthrow new TtcnError(\"Internal error: Performing ischosen() operation on an invalid field of union type {0}.\");\n", displayName));
		source.append("\t\t\t}\n");
		source.append("\t\t\tswitch (template_selection) {\n");
		source.append("\t\t\tcase SPECIFIC_VALUE:\n");
		source.append(MessageFormat.format("\t\t\t\tif (single_value_union_selection == {0}.union_selection_type.UNBOUND_VALUE) '{'\n", genName));
		source.append(MessageFormat.format("\t\t\t\t\tthrow new TtcnError(\"Internal error: Invalid selector in a specific value when performing ischosen() operation on a template of union type {0}.\");\n", displayName));
		source.append("\t\t\t\t}\n");
		source.append("\t\t\t\treturn single_value_union_selection == checked_selection;\n");
		source.append("\t\t\tcase VALUE_LIST: {\n");
		source.append("\t\t\t\tif (value_list.isEmpty()) {\n");
		source.append(MessageFormat.format("\t\t\t\t\tthrow new TtcnError(\"Internal error: Performing ischosen() operation on a template of union type {0} containing an empty list.\");\n", displayName));
		source.append("\t\t\t\t}\n");
		source.append("\t\t\t\tfinal int list_size = value_list.size();\n");
		source.append("\t\t\t\tfor (int i = 0; i < list_size; i++) {\n");
		source.append("\t\t\t\t\tif(!value_list.get(i).ischosen(checked_selection)) {\n");
						//FIXME this is incorrect in the compiler
		source.append("\t\t\t\t\t\treturn false;\n");
		source.append("\t\t\t\t\t}\n");
		source.append("\t\t\t\t}\n");
		source.append("\t\t\t\treturn true;\n");
		source.append("\t\t\t}\n");
		source.append("\t\t\tdefault:\n");
		source.append("\t\t\t\treturn false;\n");
		source.append("\t\t\t}\n");
		source.append("\t\t}\n\n");
	}

	/**
	 * Generate the isValue function
	 *
	 * @param source
	 *                where the source code is to be generated.
	 * @param displayName
	 *                the user readable name of the type to be generated.
	 * @param fieldInfos
	 *                the list of information about the fields.
	 * */
	private static void generateTemplateIsValue(final StringBuilder source, final String displayName, final List<FieldInfo> fieldInfos) {
		source.append("\t\t@Override\n");
		source.append("\t\tpublic boolean is_value() {\n");
		source.append("\t\t\tif (template_selection != template_sel.SPECIFIC_VALUE || is_ifPresent) {\n");
		source.append("\t\t\t\treturn false;\n");
		source.append("\t\t\t}\n");
		source.append("\t\t\tswitch (single_value_union_selection) {\n");
		for (int i = 0 ; i < fieldInfos.size(); i++) {
			//TODO could this be optimized?
			final FieldInfo fieldInfo = fieldInfos.get(i);
			source.append(MessageFormat.format("\t\t\tcase ALT_{0}:\n", fieldInfo.mJavaVarName));
			source.append(MessageFormat.format("\t\t\t\treturn (({0})single_value).is_value();\n", fieldInfo.mJavaTemplateName));
		}
		source.append("\t\t\tdefault:\n");
		source.append(MessageFormat.format("\t\t\t\tthrow new TtcnError(\"Internal error: Invalid selector in a specific value when performing is_value operation on a template of union type {0}.\");\n", displayName));
		source.append("\t\t\t}\n");
		source.append("\t\t}\n\n");
	}

	/**
	 * Generate the valueOf function
	 *
	 * @param source
	 *                where the source code is to be generated.
	 * @param genName
	 *                the name of the generated class representing the
	 *                union/choice type.
	 * @param displayName
	 *                the user readable name of the type to be generated.
	 * @param fieldInfos
	 *                the list of information about the fields.
	 * */
	private static void generateTemplateValueOf(final StringBuilder source, final String genName, final String displayName, final List<FieldInfo> fieldInfos) {
		source.append("\t\t@Override\n");
		source.append(MessageFormat.format("\t\tpublic {0} valueof() '{'\n", genName));
		source.append("\t\t\tif (template_selection != template_sel.SPECIFIC_VALUE || is_ifPresent) {\n");
		source.append(MessageFormat.format("\t\t\t\tthrow new TtcnError(\"Performing a valueof or send operation on a non-specific template of union type {0}.\");\n", displayName));
		source.append("\t\t\t}\n");
		if (!fieldInfos.isEmpty()) {
			source.append(MessageFormat.format("\t\t\tfinal {0} ret_val = new {0}();\n", genName));
		}
		source.append("\t\t\tswitch (single_value_union_selection) {\n");
		for (int i = 0 ; i < fieldInfos.size(); i++) {
			final FieldInfo fieldInfo = fieldInfos.get(i);
			source.append(MessageFormat.format("\t\t\tcase ALT_{0}:\n", fieldInfo.mJavaVarName));
			source.append(MessageFormat.format("\t\t\t\tret_val.get_field_{0}().operator_assign((({1})single_value).valueof());\n", fieldInfo.mJavaVarName, fieldInfo.mJavaTemplateName));
			source.append("\t\t\t\tbreak;\n");
		}
		source.append("\t\t\tdefault:\n");
		source.append(MessageFormat.format("\t\t\t\tthrow new TtcnError(\"Internal error: Invalid selector in a specific value when performing valueof operation on a template of union type {0}.\");\n", displayName));
		source.append("\t\t\t}\n");
		if (!fieldInfos.isEmpty()) {
			source.append("\t\t\treturn ret_val;\n");
		}
		source.append("\t\t}\n\n");
	}

	/**
	 * Generate the set_type function
	 *
	 * @param source
	 *                where the source code is to be generated.
	 * @param genName
	 *                the name of the generated class representing the
	 *                union/choice type.
	 * @param displayName
	 *                the user readable name of the type to be generated.
	 * */
	private static void generateTemplateSetType(final StringBuilder source, final String genName, final String displayName) {
		source.append("\t\t@Override\n");
		source.append("\t\tpublic void set_type(final template_sel template_type, final int list_length) {\n");
		source.append("\t\t\tif (template_type != template_sel.VALUE_LIST && template_type != template_sel.COMPLEMENTED_LIST) {\n");
		source.append(MessageFormat.format("\t\t\t\tthrow new TtcnError(\"Internal error: Setting an invalid list for a template of union type {0}.\");\n", displayName));
		source.append("\t\t\t}\n");

		source.append("\t\t\tclean_up();\n");
		source.append("\t\t\tset_selection(template_type);\n");
		source.append(MessageFormat.format("\t\t\tvalue_list = new ArrayList<{0}_template>(list_length);\n", genName));
		source.append("\t\t\tfor(int i = 0 ; i < list_length; i++) {\n");
		source.append(MessageFormat.format("\t\t\t\tvalue_list.add(new {0}_template());\n", genName));
		source.append("\t\t\t}\n");
		source.append("\t\t}\n\n");
	}

	/**
	 * Generate the list_item function
	 *
	 * @param source
	 *                where the source code is to be generated.
	 * @param genName
	 *                the name of the generated class representing the
	 *                union/choice type.
	 * @param displayName
	 *                the user readable name of the type to be generated.
	 * */
	private static void generateTemplateListItem(final StringBuilder source, final String genName, final String displayName) {
		source.append("\t\t@Override\n");
		source.append("\t\tpublic int n_list_elem() {\n");
		source.append("\t\t\tif (template_selection != template_sel.VALUE_LIST && template_selection != template_sel.COMPLEMENTED_LIST) {\n");
		source.append(MessageFormat.format( "\t\t\t\tthrow new TtcnError(\"Internal error: Accessing a list element of a non-list template of union type {0}.\");\n", displayName ) );
		source.append("\t\t\t}\n");
		source.append("\t\t\treturn value_list.size();\n");
		source.append("\t\t}\n\n");

		source.append("\t\t@Override\n");
		source.append(MessageFormat.format("\t\tpublic {0}_template list_item(final int list_index)  '{'\n", genName));
		source.append("\t\t\tif (template_selection != template_sel.VALUE_LIST && template_selection != template_sel.COMPLEMENTED_LIST) {\n");
		source.append(MessageFormat.format("\t\t\t\tthrow new TtcnError(\"Internal error: Accessing a list element of a non-list template of union type {0}.\");\n", displayName));
		source.append("\t\t\t}\n");

		source.append("\t\t\tif (list_index < 0) {\n");
		source.append(MessageFormat.format("\t\t\t\tthrow new TtcnError(MessageFormat.format(\"Internal error: Accessing a value list template of type {0} using a negative index ('{'0'}').\", list_index));\n", displayName));
		source.append("\t\t\t} else if(list_index >= value_list.size()) {\n");
		source.append(MessageFormat.format("\t\t\t\tthrow new TtcnError(\"Internal error: Index overflow in a value list template of union type {0}.\");\n", displayName));
		source.append("\t\t\t}\n");
		source.append("\t\t\treturn value_list.get(list_index);\n");
		source.append("\t\t}\n\n");
	}

	/**
	 * Generate the match_omit function
	 *
	 * @param source
	 *                where the source code is to be generated.
	 * */
	private static void generateTemplateMatchOmit(final StringBuilder source) {
		source.append("\t\t@Override\n");
		source.append("\t\tpublic boolean match_omit(final boolean legacy) {\n");
		source.append("\t\t\tif (is_ifPresent) {\n");
		source.append("\t\t\t\treturn true;\n");
		source.append("\t\t\t}\n");
		source.append("\t\t\tswitch (template_selection) {\n");
		source.append("\t\t\tcase OMIT_VALUE:\n");
		source.append("\t\t\tcase ANY_OR_OMIT:\n");
		source.append("\t\t\t\treturn true;\n");
		source.append("\t\t\tcase VALUE_LIST:\n");
		source.append("\t\t\tcase COMPLEMENTED_LIST:\n");
		source.append("\t\t\t\tif (legacy) {\n");
		source.append("\t\t\t\t\tfinal int list_size = value_list.size();\n");
		source.append("\t\t\t\t\tfor (int i = 0 ; i < list_size; i++) {\n");
		source.append("\t\t\t\t\t\tif (value_list.get(i).match_omit(legacy)) {\n");
		source.append("\t\t\t\t\t\t\treturn template_selection == template_sel.VALUE_LIST;\n");
		source.append("\t\t\t\t\t\t}\n");
		source.append("\t\t\t\t\t}\n");
		source.append("\t\t\t\t\treturn template_selection == template_sel.COMPLEMENTED_LIST;\n");
		source.append("\t\t\t\t}\n");
		source.append("\t\t\t\treturn false;\n");
		source.append("\t\t\tdefault:\n");
		source.append("\t\t\t\treturn false;\n");
		source.append("\t\t\t}\n");
		source.append("\t\t}\n\n");
	}

	/**
	 * Generate getters/setters
	 *
	 * @param aData
	 *                used to access build settings.
	 * @param source
	 *                where the source code is to be generated.
	 * @param genName
	 *                the name of the generated class representing the
	 *                union/choice type.
	 * @param displayName
	 *                the user readable name of the type to be generated.
	 * @param fieldInfos
	 *                the list of information about the fields.
	 * */
	private static void generateTemplateGetterSetters(final JavaGenData aData, final StringBuilder source, final String genName, final String displayName,
			final List<FieldInfo> fieldInfos) {
		for (int i = 0 ; i < fieldInfos.size(); i++) {
			final FieldInfo fieldInfo = fieldInfos.get(i);

			if (aData.isDebug()) {
				source.append("\t\t/**\n");
				source.append(MessageFormat.format("\t\t * Selects and gives access to field {0}.\n", fieldInfo.mDisplayName));
				source.append(MessageFormat.format("\t\t * If field {0} was previously selected,\n", fieldInfo.mDisplayName));
				source.append("\t\t * its value will be destroyed.\n");
				source.append("\t\t *\n");
				source.append(MessageFormat.format("\t\t * @return field {0}.\n", fieldInfo.mDisplayName));
				source.append("\t\t * */\n");
			}
			source.append(MessageFormat.format("\t\tpublic {0} get_field_{1}() '{'\n", fieldInfo.mJavaTemplateName, fieldInfo.mJavaVarName));
			source.append(MessageFormat.format("\t\t\tif (template_selection != template_sel.SPECIFIC_VALUE || single_value_union_selection != {0}.union_selection_type.ALT_{1}) '{'\n", genName, fieldInfo.mJavaVarName));
			source.append("\t\t\t\tfinal template_sel old_selection = template_selection;\n");
			source.append("\t\t\t\tclean_up();\n");
			source.append("\t\t\t\tif (old_selection == template_sel.ANY_VALUE || old_selection == template_sel.ANY_OR_OMIT) {\n");
			source.append(MessageFormat.format("\t\t\t\t\tsingle_value = new {0}(template_sel.ANY_VALUE);\n", fieldInfo.mJavaTemplateName));
			source.append("\t\t\t\t} else {\n");
			source.append(MessageFormat.format("\t\t\t\t\tsingle_value = new {0}();\n", fieldInfo.mJavaTemplateName));
			source.append("\t\t\t\t}\n");
			source.append(MessageFormat.format("\t\t\t\tsingle_value_union_selection = {0}.union_selection_type.ALT_{1};\n", genName, fieldInfo.mJavaVarName));
			source.append("\t\t\t\tset_selection(template_sel.SPECIFIC_VALUE);\n");
			source.append("\t\t\t}\n");
			source.append(MessageFormat.format("\t\t\treturn ({0})single_value;\n", fieldInfo.mJavaTemplateName));
			source.append("\t\t}\n\n");

			if (aData.isDebug()) {
				source.append("\t\t/**\n");
				source.append(MessageFormat.format("\t\t * Gives read-only access to field {0}.\n", fieldInfo.mDisplayName));
				source.append(MessageFormat.format("\t\t * If field {0} is not selected,\n", fieldInfo.mDisplayName));
				source.append("\t\t * this function will cause a dynamic test case error.\n");
				source.append("\t\t *\n");
				source.append(MessageFormat.format("\t\t * @return field {0}.\n", fieldInfo.mDisplayName));
				source.append("\t\t * */\n");
			}
			source.append(MessageFormat.format("\t\tpublic {0} constGet_field_{1}() '{'\n", fieldInfo.mJavaTemplateName, fieldInfo.mJavaVarName));
			source.append("\t\t\tif (template_selection != template_sel.SPECIFIC_VALUE) {\n");
			source.append(MessageFormat.format("\t\t\t\tthrow new TtcnError(\"Accessing field {0} in a non-specific template of union type {1}.\");\n", fieldInfo.mDisplayName, displayName));
			source.append("\t\t\t}\n");
			source.append(MessageFormat.format("\t\t\tif (single_value_union_selection != {0}.union_selection_type.ALT_{1}) '{'\n", genName, fieldInfo.mJavaVarName));
			source.append(MessageFormat.format("\t\t\t\tthrow new TtcnError(\"Accessing non-selected field {0} in a template of union type {1}.\");\n", fieldInfo.mDisplayName, displayName));
			source.append("\t\t\t}\n");
			source.append(MessageFormat.format("\t\t\treturn ({0})single_value;\n", fieldInfo.mJavaTemplateName));
			source.append("\t\t}\n\n");
		}
	}

	/**
	 * Generate log
	 *
	 * @param source
	 *                where the source code is to be generated.
	 * @param fieldInfos
	 *                the list of information about the fields.
	 * */
	private static void generateTemplateLog(final StringBuilder source, final List<FieldInfo> fieldInfos) {
		source.append("\t\t@Override\n");
		source.append("\t\tpublic void log() {\n");
		source.append("\t\t\tswitch (template_selection) {\n");
		source.append("\t\t\tcase SPECIFIC_VALUE:\n");
		if (!fieldInfos.isEmpty()) {
			source.append("\t\t\t\tswitch (single_value_union_selection) {\n");
			for (int i = 0; i < fieldInfos.size(); i++) {
				source.append(MessageFormat.format("\t\t\t\tcase ALT_{0}:\n", fieldInfos.get(i).mJavaVarName));
				source.append(MessageFormat.format("\t\t\t\t\tTTCN_Logger.log_event_str(\"'{' {0} := \");\n", fieldInfos.get(i).mJavaVarName));
				source.append("\t\t\t\t\tsingle_value.log();\n");
				source.append("\t\t\t\t\tTTCN_Logger.log_event_str(\" }\");\n");
				source.append("\t\t\t\t\tbreak;\n");
			}
			source.append("\t\t\t\tdefault:\n");
			source.append("\t\t\t\t\tTTCN_Logger.log_event_unbound();\n");
			source.append("\t\t\t}\n");
		}
		source.append("\t\t\t\tbreak;\n");
		source.append("\t\t\tcase COMPLEMENTED_LIST:\n");
		source.append("\t\t\t\tTTCN_Logger.log_event_str(\"complement\");\n");
		source.append("\t\t\tcase VALUE_LIST: {\n");
		source.append("\t\t\t\tTTCN_Logger.log_char('(');\n");
		source.append("\t\t\t\tfinal int list_size = value_list.size();\n");
		source.append("\t\t\t\tfor (int list_count = 0; list_count < list_size; list_count++) {\n");
		source.append("\t\t\t\t\tif (list_count > 0) {\n");
		source.append("\t\t\t\t\t\tTTCN_Logger.log_event_str(\", \");\n");
		source.append("\t\t\t\t\t}\n");
		source.append("\t\t\t\t\tvalue_list.get(list_count).log();\n");
		source.append("\t\t\t\t}\n");
		source.append("\t\t\t\tTTCN_Logger.log_char(')');\n");
		source.append("\t\t\t\tbreak;\n");
		source.append("\t\t\t}\n");
		source.append("\t\t\tdefault:\n");
		source.append("\t\t\t\tlog_generic();\n");
		source.append("\t\t\t\tbreak;\n");
		source.append("\t\t\t}\n");
		source.append("\t\t\tlog_ifpresent();\n");
		source.append("\t\t}\n\n");
	}

	/**
	 * Generate log_match
	 *
	 * @param aData
	 *                used to access build settings.
	 * @param source
	 *                where the source code is to be generated.
	 * @param genName
	 *                the name of the generated class representing the
	 *                union/choice type.
	 * @param displayName
	 *                the user readable name of the type to be generated.
	 * @param fieldInfos
	 *                the list of information about the fields.
	 * */
	private static void generateTemplateLogMatch(final JavaGenData aData, final StringBuilder source, final String genName, final String displayName, final List<FieldInfo> fieldInfos) {
		source.append("\t\t@Override\n");
		source.append("\t\tpublic void log_match(final Base_Type match_value, final boolean legacy) {\n");
		source.append(MessageFormat.format("\t\t\tif (match_value instanceof {0}) '{'\n", genName));
		source.append(MessageFormat.format("\t\t\t\tlog_match(({0})match_value, legacy);\n", genName));
		source.append("\t\t\t\treturn;\n");
		source.append("\t\t\t}\n\n");
		source.append(MessageFormat.format("\t\t\tthrow new TtcnError(\"Internal Error: value can not be cast to {0}.\");\n", displayName));
		source.append("\t\t}\n\n");

		if (fieldInfos.size() > maxFieldsLength) {
			final int fullSize = fieldInfos.size();
			final int iterations = fullSize / maxFieldsLength;
			for (int iteration = 0; iteration <= iterations; iteration++) {
				final int start = iteration * maxFieldsLength ;
				final int end = Math.min((iteration + 1) * maxFieldsLength - 1, fullSize - 1);
				source.append("\t\t// Internal helper function.\n");
				source.append(MessageFormat.format("\t\tprivate void log_match_helper_{0,number,#}_{1,number,#}(final {2} match_value, final boolean legacy, final boolean isCompact) '{'\n", start, end, genName));
				source.append("\t\t\t\tswitch (single_value_union_selection) {\n");
				for (int i = start ; i <= end; i++) {
					final FieldInfo fieldInfo = fieldInfos.get(i);
					source.append(MessageFormat.format("\t\t\t\tcase ALT_{0}:\n", fieldInfo.mJavaVarName));
					source.append("\t\t\t\t\tif (isCompact) {\n");
					source.append(MessageFormat.format("\t\t\t\t\t\tTTCN_Logger.log_logmatch_info(\".{0}\");\n", fieldInfo.mDisplayName));
					source.append("\t\t\t\t\t} else {\n");
					source.append(MessageFormat.format("\t\t\t\t\t\tTTCN_Logger.log_logmatch_info(\"'{' {0} := \");\n", fieldInfo.mDisplayName));
					source.append("\t\t\t\t\t}\n");
					source.append(MessageFormat.format("\t\t\t\t\tsingle_value.log_match(match_value.get_field_{0}(), legacy);\n", fieldInfo.mJavaVarName));
					source.append("\t\t\t\t\tif (!isCompact) {\n");
					source.append("\t\t\t\t\t\tTTCN_Logger.log_event_str(\" }\");\n");
					source.append("\t\t\t\t\t}\n");
					source.append("\t\t\t\t\tbreak;\n");
				}
				source.append("\t\t\t\tdefault:\n");
				source.append("\t\t\t\t\tbreak;\n");
				source.append("\t\t\t\t}\n");
				source.append("\t\t}\n\n");
			}
		}

		if (aData.isDebug()) {
			source.append("\t\t/**\n");
			source.append("\t\t * Logs the matching of the provided value to this template, to help\n");
			source.append("\t\t * identify the reason for mismatch. In legacy mode omitted value fields\n");
			source.append("\t\t * are not matched against the template field.\n");
			source.append("\t\t *\n");
			source.append("\t\t * @param match_value\n");
			source.append("\t\t *                the value to be matched.\n");
			source.append("\t\t * @param legacy\n");
			source.append("\t\t *                use legacy mode.\n");
			source.append("\t\t * */\n");
		}
		source.append(MessageFormat.format("\t\tpublic void log_match(final {0} match_value, final boolean legacy) '{'\n", genName));
		source.append("\t\t\t\tfinal boolean isCompact = TTCN_Logger.matching_verbosity_t.VERBOSITY_COMPACT == TTCN_Logger.get_matching_verbosity();\n");
		source.append("\t\t\tif (isCompact && match(match_value, legacy)) {\n");
		source.append("\t\t\t\tTTCN_Logger.print_logmatch_buffer();\n");
		source.append("\t\t\t\tTTCN_Logger.log_event_str(\" matched\");\n");
		source.append("\t\t\t\treturn;\n");
		source.append("\t\t\t}\n");
		source.append("\t\t\tif (template_selection == template_sel.SPECIFIC_VALUE && single_value_union_selection == match_value.get_selection()) {\n");

		if (fieldInfos.size() > maxFieldsLength) {
			final int fullSize = fieldInfos.size();
			source.append("\t\t\tfinal int temp = single_value_union_selection.ordinal();\n");
			source.append(MessageFormat.format("\t\t\tswitch (temp / {0,number,#}) '{'\n", maxFieldsLength));
			final int iterations = fullSize / maxFieldsLength;
			for (int iteration = 0; iteration <= iterations; iteration++) {
				final int start = iteration * maxFieldsLength;
				final int end = Math.min((iteration + 1) * maxFieldsLength - 1, fullSize - 1);
				source.append(MessageFormat.format("\t\t\tcase {0,number,#}:\n", iteration));
				if (iteration == 0) {
					source.append("\t\t\t\tif (temp == 0) {\n");
					source.append("\t\t\t\t\tTTCN_Logger.print_logmatch_buffer();\n");
					source.append("\t\t\t\t\tTTCN_Logger.log_event_str(\"<invalid selector>\");\n");
					source.append("\t\t\t\t} else {\n");
					source.append(MessageFormat.format("\t\t\t\t\tlog_match_helper_{0,number,#}_{1,number,#}(match_value, legacy, isCompact);\n", start, end));
					source.append("\t\t\t\t}\n");
				} else if (iteration == iterations) {
					source.append(MessageFormat.format("\t\t\t\tif (temp > {0,number,#}) '{'\n", fullSize)); //1689 meg jo, fullSize
					source.append("\t\t\t\t\tTTCN_Logger.print_logmatch_buffer();\n");
					source.append("\t\t\t\t\tTTCN_Logger.log_event_str(\"<invalid selector>\");\n");
					source.append("\t\t\t\t} else {\n");
					source.append(MessageFormat.format("\t\t\t\t\tlog_match_helper_{0,number,#}_{1,number,#}(match_value, legacy, isCompact);\n", start, end));
					source.append("\t\t\t\t}\n");
				} else {
					source.append(MessageFormat.format("\t\t\t\tlog_match_helper_{0,number,#}_{1,number,#}(match_value, legacy, isCompact);\n", start, end));
				}
				source.append("\t\t\t\tbreak;\n");
			}
			source.append("\t\t\t\t}\n");
		} else {
			source.append("\t\t\t\tswitch (single_value_union_selection) {\n");
			for (int i = 0 ; i < fieldInfos.size(); i++) {
				final FieldInfo fieldInfo = fieldInfos.get(i);
				source.append(MessageFormat.format("\t\t\t\tcase ALT_{0}:\n", fieldInfo.mJavaVarName));
				source.append("\t\t\t\t\tif (isCompact) {\n");
				source.append(MessageFormat.format("\t\t\t\t\t\tTTCN_Logger.log_logmatch_info(\".{0}\");\n", fieldInfo.mDisplayName));
				source.append(MessageFormat.format("\t\t\t\t\t\tsingle_value.log_match(match_value.get_field_{0}(), legacy);\n", fieldInfo.mJavaVarName));
				source.append("\t\t\t\t\t} else {\n");
				source.append(MessageFormat.format("\t\t\t\t\t\tTTCN_Logger.log_logmatch_info(\"'{' {0} := \");\n", fieldInfo.mDisplayName));
				source.append(MessageFormat.format("\t\t\t\t\t\tsingle_value.log_match(match_value.get_field_{0}(), legacy);\n", fieldInfo.mJavaVarName));
				source.append("\t\t\t\t\t\tTTCN_Logger.log_event_str(\" }\");\n");
				source.append("\t\t\t\t\t}\n");
				source.append("\t\t\t\t\tbreak;\n");
			}
			source.append("\t\t\t\tdefault:\n");
			source.append("\t\t\t\t\tTTCN_Logger.print_logmatch_buffer();\n");
			source.append("\t\t\t\t\tTTCN_Logger.log_event_str(\"<invalid selector>\");\n");
			source.append("\t\t\t\t\tbreak;\n");
			source.append("\t\t\t\t}\n");
		}
		source.append("\t\t\t} else {\n");
		source.append("\t\t\t\tTTCN_Logger.print_logmatch_buffer();\n");
		source.append("\t\t\t\tmatch_value.log();\n");
		source.append("\t\t\t\tTTCN_Logger.log_event_str(\" with \");\n");
		source.append("\t\t\t\tlog();\n");
		source.append("\t\t\t\tif (match(match_value, legacy)) {\n");
		source.append("\t\t\t\t\tTTCN_Logger.log_event_str(\" matched\");\n");
		source.append("\t\t\t\t} else {\n");
		source.append("\t\t\t\t\tTTCN_Logger.log_event_str(\" unmatched\");\n");
		source.append("\t\t\t\t}\n");
		source.append("\t\t\t}\n");
		source.append("\t\t}\n\n");
	}

	/**
	 * Generate encode_text/decode_text
	 *
	 * @param source
	 *                where the source code is to be generated.
	 * @param genName
	 *                the name of the generated class representing the
	 *                union/choice type.
	 * @param displayName
	 *                the user readable name of the type to be generated.
	 * @param fieldInfos
	 *                the list of information about the fields.
	 * */
	private static void generateTemplateEncodeDecodeText(final StringBuilder source, final String genName, final String displayName, final List<FieldInfo> fieldInfos) {
		source.append("\t\t@Override\n");
		source.append("\t\tpublic void encode_text(final Text_Buf text_buf) {\n");
		source.append("\t\t\tencode_text_base(text_buf);\n");
		source.append("\t\t\tswitch (template_selection) {\n");
		source.append("\t\t\tcase OMIT_VALUE:\n");
		source.append("\t\t\tcase ANY_VALUE:\n");
		source.append("\t\t\tcase ANY_OR_OMIT:\n");
		source.append("\t\t\t\tbreak;\n");
		source.append("\t\t\tcase SPECIFIC_VALUE:\n");
		source.append("\t\t\t\ttext_buf.push_int(single_value_union_selection.ordinal());\n");
		if (!fieldInfos.isEmpty()) {
			source.append("\t\t\t\tsingle_value.encode_text(text_buf);\n");
		}
		source.append("\t\t\t\tbreak;\n");
		source.append("\t\t\tcase VALUE_LIST:\n");
		source.append("\t\t\tcase COMPLEMENTED_LIST: {\n");
		source.append("\t\t\t\tfinal int list_size = value_list.size();\n");
		source.append("\t\t\t\ttext_buf.push_int(list_size);\n");
		source.append("\t\t\t\tfor (int i = 0; i < list_size; i++) {\n");
		source.append("\t\t\t\t\tvalue_list.get(i).encode_text(text_buf);\n");
		source.append("\t\t\t\t}\n");
		source.append("\t\t\t\tbreak;\n");
		source.append("\t\t\t}\n");
		source.append("\t\t\tdefault:\n");
		source.append(MessageFormat.format("\t\t\t\tthrow new TtcnError(\"Text encoder: Encoding an uninitialized template of type {0}.\");\n", displayName));
		source.append("\t\t\t}\n");
		source.append("\t\t}\n\n");

		source.append("\t\t@Override\n");
		source.append("\t\tpublic void decode_text(final Text_Buf text_buf) {\n");
		source.append("\t\t\tclean_up();\n");
		source.append("\t\t\tdecode_text_base(text_buf);\n");
		source.append("\t\t\tswitch (template_selection) {\n");
		source.append("\t\t\tcase OMIT_VALUE:\n");
		source.append("\t\t\tcase ANY_VALUE:\n");
		source.append("\t\t\tcase ANY_OR_OMIT:\n");
		source.append("\t\t\t\tbreak;\n");
		source.append("\t\t\tcase SPECIFIC_VALUE:{\n");
		source.append("\t\t\t\tfinal int temp = text_buf.pull_int().get_int();\n");
		source.append(MessageFormat.format("\t\t\t\tsingle_value_union_selection = {0}.union_selection_type.values()[temp];\n", genName));
		source.append("\t\t\t\tswitch (single_value_union_selection) {\n");
		source.append("\t\t\t\tcase UNBOUND_VALUE:\n");
		source.append(MessageFormat.format("\t\t\t\t\tthrow new TtcnError(\"Text decoder: Unrecognized union selector was received for a template of type {0}.\");\n", displayName));

		for (int i = 0 ; i < fieldInfos.size(); i++) {
			final FieldInfo fieldInfo = fieldInfos.get(i);

			source.append(MessageFormat.format("\t\t\t\tcase ALT_{0}:\n", fieldInfo.mJavaVarName));
			source.append(MessageFormat.format("\t\t\t\t\tsingle_value = new {0}();\n", fieldInfo.mJavaTemplateName));
			source.append("\t\t\t\t\tsingle_value.decode_text(text_buf);\n");
			source.append("\t\t\t\t\tbreak;\n");
		}
		source.append("\t\t\t\t}\n");
		source.append("\t\t\t\tbreak;\n");
		source.append("\t\t\t}\n");
		source.append("\t\t\tcase VALUE_LIST:\n");
		source.append("\t\t\tcase COMPLEMENTED_LIST: {\n");
		source.append("\t\t\t\tfinal int size = text_buf.pull_int().get_int();\n");
		source.append(MessageFormat.format("\t\t\t\tvalue_list = new ArrayList<{0}_template>(size);\n", genName));
		source.append("\t\t\t\tfor (int i = 0; i < size; i++) {\n");
		source.append(MessageFormat.format("\t\t\t\t\tfinal {0}_template temp2 = new {0}_template();\n", genName));
		source.append("\t\t\t\t\ttemp2.decode_text(text_buf);\n");
		source.append("\t\t\t\t\tvalue_list.add(temp2);\n");
		source.append("\t\t\t\t}\n");
		source.append("\t\t\t\tbreak;\n");
		source.append("\t\t\t}\n");
		source.append("\t\t\tdefault:\n");
		source.append(MessageFormat.format("\t\t\t\tthrow new TtcnError(\"Text decoder: Unrecognized selector was received in a template of type {0}.\");\n", displayName));
		source.append("\t\t\t}\n");
		source.append("\t\t}\n\n");
	}

	/**
	 * Generate set_param
	 *
	 * @param source
	 *                where the source code is to be generated.
	 * @param displayName
	 *                the user readable name of the type to be generated.
	 * @param fieldInfos
	 *                the list of information about the fields.
	 * */
	private static void generateTemplateSetParam(final StringBuilder source, final String displayName, final List<FieldInfo> fieldInfos) {
		if (fieldInfos.size() > maxFieldsLength) {
			final int fullSize = fieldInfos.size();
			final int iterations = fullSize / maxFieldsLength;
			for (int iteration = 0; iteration <= iterations; iteration++) {
				final int start = iteration * maxFieldsLength ;
				final int end = Math.min((iteration + 1) * maxFieldsLength - 1, fullSize - 1);
				source.append("\t\t// Internal helper function.\n");
				source.append(MessageFormat.format("\t\tprivate boolean template_set_param_single_helper_{0,number,#}_{1,number,#}(final String name, final Module_Parameter param) '{'\n", start, end));
				for (int i = start ; i <= end; i++) {
					final FieldInfo fieldInfo = fieldInfos.get(i);

					source.append(MessageFormat.format("\t\t\tif(\"{0}\".equals(name)) '{'\n", fieldInfo.mDisplayName));
					source.append("\t\t\t\tsingle_value.set_param(param);\n");
					source.append("\t\t\t\treturn true;\n");
					source.append("\t\t\t}\n");
				}
				source.append("\t\t\treturn false;\n");
				source.append("\t\t}\n\n");

				source.append("\t\t// Internal helper function.\n");
				source.append(MessageFormat.format("\t\tprivate boolean template_set_param_list_helper_{0,number,#}_{1,number,#}(final String last_name, final Module_Parameter mp_last) '{'\n", start, end));
				for (int i = start ; i <= end; i++) {
					final FieldInfo fieldInfo = fieldInfos.get(i);

					source.append(MessageFormat.format("\t\t\tif(\"{0}\".equals(last_name)) '{'\n", fieldInfo.mDisplayName));
					source.append(MessageFormat.format("\t\t\t\tget_field_{0}().set_param(mp_last);\n", fieldInfo.mJavaVarName));
					source.append("\t\t\t\treturn true;\n");
					source.append("\t\t\t}\n");
				}
				source.append("\t\t\treturn false;\n");
				source.append("\t\t}\n\n");
			}
		}

		source.append("\t\t@Override\n");
		source.append("\t\tpublic void set_param(Module_Parameter param) {\n");
		source.append("\t\t\tfinal Module_Param_Id param_id = param.get_id();\n");
		source.append("\t\t\tif((param.get_id() instanceof Module_Param_Name) && param.get_id().next_name()) {\n");
		source.append("\t\t\t\tfinal String param_field = param.get_id().get_current_name();\n");
		source.append("\t\t\t\tfinal char first_char = param_field.charAt(0);\n");
		source.append("\t\t\t\tif (first_char >= '0' && first_char <= '9') {\n");
		source.append(MessageFormat.format("\t\t\t\t\tparam.error(\"Unexpected array index in module parameter, expected a valid field name for union template type `{0}''\");\n", displayName));
		source.append("\t\t\t\t}\n");
		if (fieldInfos.size() > maxFieldsLength) {
			final int fullSize = fieldInfos.size();
			final int iterations = fullSize / maxFieldsLength;
			for (int iteration = 0; iteration <= iterations; iteration++) {
				final int start = iteration * maxFieldsLength;
				final int end = Math.min((iteration + 1) * maxFieldsLength - 1, fullSize - 1);

				source.append(MessageFormat.format("\t\t\t\tif(template_set_param_single_helper_{0,number,#}_{1,number,#}(param_field, param)) '{'\n", start, end));
				source.append("\t\t\t\t\treturn;\n");
				source.append("\t\t\t\t}\n");
			}

			source.append(MessageFormat.format("\t\t\t\tparam.error(MessageFormat.format(\"Field `'{'0'}''' not found in union template type `{0}''\", param_field));\n", displayName));
		} else {
			for (int i = 0 ; i < fieldInfos.size(); i++) {
				final FieldInfo fieldInfo = fieldInfos.get(i);

				if (i == 0) {
					source.append("\t\t\t\t");
				} else {
					source.append(" else ");
				}
				source.append(MessageFormat.format("if(\"{0}\".equals(param_field)) '{'\n", fieldInfo.mDisplayName));
				source.append("\t\t\t\t\tsingle_value.set_param(param);\n");
				source.append("\t\t\t\t\treturn;\n");
				source.append("\t\t\t\t}");
			}

			source.append(" else {\n");
			source.append(MessageFormat.format("\t\t\t\t\tparam.error(MessageFormat.format(\"Field `'{'0'}''' not found in union template type `{0}''\", param_field));\n", displayName));
			source.append("\t\t\t\t}\n");
		}

		source.append("\t\t\t}\n");

		source.append("\t\t\tparam.basic_check(Module_Parameter.basic_check_bits_t.BC_TEMPLATE.getValue(), \"union template\");\n");

		// Originally RT2
		source.append("\t\t\tif (param.get_type() == Module_Parameter.type_t.MP_Reference) {\n");
		source.append("\t\t\t\tparam = param.get_referenced_param().get();\n");
		source.append("\t\t\t}\n");

		source.append("\t\t\tswitch (param.get_type()) {\n");
		source.append("\t\t\tcase MP_Omit:\n");
		source.append("\t\t\t\toperator_assign(template_sel.OMIT_VALUE);\n");
		source.append("\t\t\t\tbreak;\n");
		source.append("\t\t\tcase MP_Any:\n");
		source.append("\t\t\t\toperator_assign(template_sel.ANY_VALUE);\n");
		source.append("\t\t\t\tbreak;\n");
		source.append("\t\t\tcase MP_AnyOrNone:\n");
		source.append("\t\t\t\toperator_assign(template_sel.ANY_OR_OMIT);\n");
		source.append("\t\t\t\tbreak;\n");
		source.append("\t\t\tcase MP_List_Template:\n");
		source.append("\t\t\tcase MP_ComplementList_Template: {\n");
		source.append("\t\t\t\tfinal int size = param.get_size();\n");
		source.append("\t\t\t\tset_type(param.get_type() == Module_Parameter.type_t.MP_List_Template ? template_sel.VALUE_LIST : template_sel.COMPLEMENTED_LIST, size);\n");
		source.append("\t\t\t\tfor (int i = 0; i < size; i++) {\n");
		source.append("\t\t\t\t\tlist_item(i).set_param(param.get_elem(i));\n");
		source.append("\t\t\t\t}\n");
		source.append("\t\t\t\tbreak;\n");
		source.append("\t\t\t}\n");
		source.append("\t\t\tcase MP_Value_List:\n");
		source.append("\t\t\t\tif (param.get_size() == 0) {\n");
		source.append("\t\t\t\t\tbreak;\n");
		source.append("\t\t\t\t}\n");
		source.append(MessageFormat.format("\t\t\t\tparam.type_error(\"union template\", \"{0}\");\n", displayName));
		source.append("\t\t\t\tbreak;\n");
		source.append("\t\t\tcase MP_Assignment_List: {\n");
		source.append("\t\t\t\tfinal Module_Parameter mp_last = param.get_elem(param.get_size() - 1);\n");
		source.append("\t\t\t\tfinal String last_name = mp_last.get_id().get_name();\n");
		if (fieldInfos.size() > maxFieldsLength) {
			final int fullSize = fieldInfos.size();
			final int iterations = fullSize / maxFieldsLength;
			for (int iteration = 0; iteration <= iterations; iteration++) {
				final int start = iteration * maxFieldsLength;
				final int end = Math.min((iteration + 1) * maxFieldsLength - 1, fullSize - 1);
				source.append(MessageFormat.format("\t\t\t\tif(template_set_param_list_helper_{0,number,#}_{1,number,#}(last_name, mp_last)) '{'\n", start, end));
				source.append("\t\t\t\t\tbreak;\n");
				source.append("\t\t\t\t}\n");
			}
		} else {
			for (int i = 0 ; i < fieldInfos.size(); i++) {
				final FieldInfo fieldInfo = fieldInfos.get(i);

				source.append(MessageFormat.format("\t\t\t\tif(\"{0}\".equals(last_name)) '{'\n", fieldInfo.mDisplayName));
				source.append(MessageFormat.format("\t\t\t\t\tget_field_{0}().set_param(mp_last);\n", fieldInfo.mJavaVarName));
				source.append("\t\t\t\t\tbreak;\n");
				source.append("\t\t\t\t}\n");
			}
		}
		source.append(MessageFormat.format("\t\t\t\tmp_last.error(MessageFormat.format(\"Field '{'0'}' does not exist in type {0}.\", last_name));\n", displayName));
		source.append("\t\t\t\tbreak;\n");
		source.append("\t\t\t}\n");
		source.append("\t\t\tdefault:\n");
		source.append(MessageFormat.format("\t\t\t\tparam.type_error(\"union template\", \"{0}\");\n", displayName));
		source.append("\t\t\t}\n");
		source.append("\t\t\tis_ifPresent = param.get_ifpresent();\n");
		source.append("\t\t}\n\n");
	}

	// Originally RT2
	/**
	 * Generate get_param
	 *
	 * @param source
	 *                where the source code is to be generated.
	 * @param displayName
	 *                the user readable name of the type to be generated.
	 * @param displayName
	 *                the user readable name of the type to be generated.
	 * @param fieldInfos
	 *                the list of information about the fields.
	 */
	private static void generateTemplateGetParam(final StringBuilder source, final String name, final String displayName, final List<FieldInfo> fieldInfos) {
		if (fieldInfos.size() > maxFieldsLength) {
			final int fullSize = fieldInfos.size();
			final int iterations = fullSize / maxFieldsLength;
			for (int iteration = 0; iteration <= iterations; iteration++) {
				final int start = iteration * maxFieldsLength ;
				final int end = Math.min((iteration + 1) * maxFieldsLength - 1, fullSize - 1);
				source.append("\t\t// Internal helper function.\n");
				source.append(MessageFormat.format("\t\tprivate Module_Parameter template_get_param_single_helper_{0,number,#}_{1,number,#}(final String param_field, final Module_Param_Name param_name) '{'\n", start, end));
				source.append("\t\t\t");
				for (int i = start ; i <= end; i++) {
					final FieldInfo fieldInfo = fieldInfos.get(i);

					source.append(MessageFormat.format("if (\"{0}\".equals(param_field)) '{'\n", fieldInfo.mDisplayName));
					source.append(MessageFormat.format("\t\t\t\treturn get_field_{0}().get_param(param_name);\n", fieldInfo.mJavaVarName));
					source.append("\t\t\t} else ");
				}
				source.append("{\n");
				source.append("\t\t\t\treturn null;\n");
				source.append("\t\t\t}\n");
				source.append("\t\t}\n\n");

				source.append(MessageFormat.format("\t\tprivate Module_Parameter template_get_param_specific_helper_{0,number,#}_{1,number,#}(final Module_Param_Name param_name) '{'\n", start, end));
				source.append("\t\t\tModule_Parameter mp_field = null;\n");
				source.append("\t\t\tswitch (single_value_union_selection) {\n");
				for (int i = start ; i <= end; i++) {
					final FieldInfo fieldInfo = fieldInfos.get(i);

					source.append(MessageFormat.format("\t\t\tcase ALT_{0}:\n", fieldInfo.mJavaVarName));
					source.append(MessageFormat.format("\t\t\t\tmp_field = get_field_{0}().get_param(param_name);\n", fieldInfo.mJavaVarName));
					source.append(MessageFormat.format("\t\t\t\tmp_field.set_id(new Module_Param_FieldName(\"{0}\"));\n", fieldInfo.mDisplayName));
					source.append("\t\t\t\treturn mp_field;\n");
				}
				source.append("\t\t\tdefault:\n");
				source.append("\t\t\t\treturn null;\n");
				source.append("\t\t\t}\n");
				source.append("\t\t}\n\n");
			}
		}

		source.append("\t\t@Override\n");
		source.append("\t\tpublic Module_Parameter get_param(final Module_Param_Name param_name) {\n");
		source.append("\t\t\tif (param_name.next_name()) {\n");
		// Haven't reached the end of the module parameter name
		// => the name refers to one of the fields, not to the whole union
		source.append("\t\t\t\tfinal String param_field = param_name.get_current_name();\n"); 
		source.append("\t\t\t\tif (param_field.charAt(0) >= '0' && param_field.charAt(0) <= '9') {\n"); 
		source.append(MessageFormat.format("\t\t\t\t\tthrow new TtcnError(\"Unexpected array index in module parameter reference, expected a valid field name for union template type `{0}''\");\n", displayName));
		source.append("\t\t\t\t}\n"); 

		if (fieldInfos.size() > maxFieldsLength) {
			source.append("\t\t\t\tModule_Parameter temp_parameter = null;\n");
			
			final int fullSize = fieldInfos.size();
			final int iterations = fullSize / maxFieldsLength;
			for (int iteration = 0; iteration <= iterations; iteration++) {
				final int start = iteration * maxFieldsLength;
				final int end = Math.min((iteration + 1) * maxFieldsLength - 1, fullSize - 1);

				source.append(MessageFormat.format("\t\t\t\ttemp_parameter = template_get_param_single_helper_{0,number,#}_{1,number,#}(param_field, param_name);\n", start, end));
				source.append("\t\t\t\tif(temp_parameter != null) {\n");
				source.append("\t\t\t\t\treturn temp_parameter;\n");
				source.append("\t\t\t\t}\n");
			}
		} else {
			source.append("\t\t\t\t");
			for (int i = 0 ; i < fieldInfos.size(); i++) {
				final FieldInfo fieldInfo = fieldInfos.get(i);
				source.append(MessageFormat.format("if (\"{0}\".equals(param_field)) '{'\n", fieldInfo.mDisplayName));
				source.append(MessageFormat.format("\t\t\t\t\treturn get_field_{0}().get_param(param_name);\n", fieldInfo.mJavaVarName));
				source.append("\t\t\t\t} else ");
			}
		}

		source.append("{\n");
		source.append(MessageFormat.format("\t\t\t\t\tthrow new TtcnError(MessageFormat.format(\"Field `'{'0'}''' not found in union type `{0}''\", param_field));\n", displayName));
		source.append("\t\t\t\t}\n");
		source.append("\t\t\t}\n");
		source.append("\t\t\tModule_Parameter mp = null;\n");
		source.append("\t\t\tswitch (template_selection) {\n");
		source.append("\t\t\tcase UNINITIALIZED_TEMPLATE:\n");
		source.append("\t\t\t\tmp = new Module_Param_Unbound();\n");
		source.append("\t\t\t\tbreak;\n");
		source.append("\t\t\tcase OMIT_VALUE:\n");
		source.append("\t\t\t\tmp = new Module_Param_Omit();\n");
		source.append("\t\t\t\tbreak;\n");
		source.append("\t\t\tcase ANY_VALUE:\n");
		source.append("\t\t\t\tmp = new Module_Param_Any();\n");
		source.append("\t\t\t\tbreak;\n");
		source.append("\t\t\tcase ANY_OR_OMIT:\n");
		source.append("\t\t\t\tmp = new Module_Param_AnyOrNone();\n");
		source.append("\t\t\t\tbreak;\n");
		source.append("\t\t\tcase SPECIFIC_VALUE: {\n");
		if (fieldInfos.isEmpty()) {
			source.append("\t\t\t\tmp = new Module_Param_Assignment_List();\n");
		} else {
			source.append("\t\t\t\tModule_Parameter mp_field = null;\n");
	
			if (fieldInfos.size() > maxFieldsLength) {
				final int fullSize = fieldInfos.size();
				source.append("\t\t\t\tfinal int temp = single_value_union_selection.ordinal();\n");
				source.append(MessageFormat.format("\t\t\t\tswitch (temp / {0,number,#}) '{'\n", maxFieldsLength));
				final int iterations = fullSize / maxFieldsLength;
				for (int iteration = 0; iteration <= iterations; iteration++) {
					final int start = iteration * maxFieldsLength;
					final int end = Math.min((iteration + 1) * maxFieldsLength - 1, fullSize - 1);
					source.append(MessageFormat.format("\t\t\t\tcase {0,number,#}:\n", iteration));
					if (iteration == 0) {
						source.append("\t\t\t\t\tif (temp == 0) {\n");
						source.append("\t\t\t\t\t\tbreak;\n");
						source.append("\t\t\t\t\t} else {\n");
						source.append(MessageFormat.format("\t\t\t\t\t\tmp_field = template_get_param_specific_helper_{0,number,#}_{1,number,#}(param_name);\n", start, end));
						source.append("\t\t\t\t\t}\n");
					} else if (iteration == iterations) {
						source.append(MessageFormat.format("\t\t\t\t\tif (temp > {0,number,#}) '{'\n", fullSize));
						source.append("\t\t\t\t\t\tbreak;\n");
						source.append("\t\t\t\t\t} else {\n");
						source.append(MessageFormat.format("\t\t\t\t\t\tmp_field = template_get_param_specific_helper_{0,number,#}_{1,number,#}(param_name);\n", start, end));
						source.append("\t\t\t\t\t}\n");
					} else {
						source.append(MessageFormat.format("\t\t\t\t\tmp_field = template_get_param_specific_helper_{0,number,#}_{1,number,#}(param_name);\n", start, end));
					}
					source.append("\t\t\t\t\tbreak;\n");
				}
				source.append("\t\t\t\tdefault:\n");
				source.append("\t\t\t\t\tmp_field = null;\n");
				source.append("\t\t\t\t}\n");
			} else {
				source.append("\t\t\t\tswitch(single_value_union_selection) {\n");
				for (int i = 0 ; i < fieldInfos.size(); i++) {
					final FieldInfo fieldInfo = fieldInfos.get(i);
					source.append(MessageFormat.format("\t\t\t\tcase ALT_{0}:\n", fieldInfo.mJavaVarName));
					source.append(MessageFormat.format("\t\t\t\t\tmp_field = get_field_{0}().get_param(param_name);\n", fieldInfo.mJavaVarName));
					source.append(MessageFormat.format("\t\t\t\t\tmp_field.set_id(new Module_Param_FieldName(\"{0}\"));\n", fieldInfo.mDisplayName));
					source.append("\t\t\t\t\tbreak;\n");
				}
				source.append("\t\t\t\tdefault:\n");
				source.append("\t\t\t\t\tbreak;\n");
				source.append("\t\t\t\t}\n");
			}
			source.append("\t\t\t\tmp = new Module_Param_Assignment_List();\n");
			source.append("\t\t\t\tmp.add_elem(mp_field);\n");
		}
		source.append("\t\t\t\tbreak;\n");
		source.append("\t\t\t}\n");
		source.append("\t\t\tcase VALUE_LIST:\n");
		source.append("\t\t\tcase COMPLEMENTED_LIST: {\n");
		source.append("\t\t\t\tif (template_selection == template_sel.VALUE_LIST) {\n");
		source.append("\t\t\t\t\tmp = new Module_Param_List_Template();\n");
		source.append("\t\t\t\t} else {\n");
		source.append("\t\t\t\t\tmp = new Module_Param_ComplementList_Template();\n");
		source.append("\t\t\t\t}\n");
		source.append("\t\t\t\tfor (int i_i = 0; i_i < value_list.size(); ++i_i) {\n");
		source.append("\t\t\t\t\tmp.add_elem(value_list.get(i_i).get_param(param_name));\n");
		source.append("\t\t\t\t}\n");
		source.append("\t\t\t\tbreak;\n");
		source.append("\t\t\t}\n");
		source.append("\t\t\tdefault:\n");
		source.append("\t\t\t\tbreak;\n");
		source.append("\t\t\t}\n");
		source.append("\t\t\tif (is_ifPresent) {\n");
		source.append("\t\t\t\tmp.set_ifpresent();\n");
		source.append("\t\t\t}\n");
		source.append("\t\t\treturn mp;\n");
		source.append("\t\t}\n\n");
	}

	/**
	 * Generate check_selection
	 *
	 * @param source
	 *                where the source code is to be generated.
	 * @param displayName
	 *                the user readable name of the type to be generated.
	 * @param fieldInfos
	 *                the list of information about the fields.
	 * */
	private static void generateTemplateCheckSelection(final StringBuilder source, final String displayName, final List<FieldInfo> fieldInfos) {
		source.append("\t\t@Override\n");
		source.append("\t\tpublic void check_restriction(final template_res restriction, final String name, final boolean legacy) {\n");
		source.append("\t\t\tif (template_selection == template_sel.UNINITIALIZED_TEMPLATE) {\n");
		source.append("\t\t\t\treturn;\n");
		source.append("\t\t\t}\n");
		source.append("\t\t\tswitch ((name != null && restriction == template_res.TR_VALUE) ? template_res.TR_OMIT : restriction) {\n");
		source.append("\t\t\tcase TR_OMIT:\n");
		source.append("\t\t\t\tif (template_selection == template_sel.OMIT_VALUE) {\n");
		source.append("\t\t\t\t\treturn;\n");
		source.append("\t\t\t\t}\n");
		source.append("\t\t\tcase TR_VALUE:\n");
		source.append("\t\t\t\tif (template_selection != template_sel.SPECIFIC_VALUE || is_ifPresent) {\n");
		source.append("\t\t\t\t\tbreak;\n");
		source.append("\t\t\t\t}\n");
		source.append("\t\t\t\tswitch (single_value_union_selection) {\n");
		for (int i = 0 ; i < fieldInfos.size(); i++) {
			final FieldInfo fieldInfo = fieldInfos.get(i);
			source.append(MessageFormat.format("\t\t\t\tcase ALT_{0}:\n", fieldInfo.mJavaVarName));

			source.append(MessageFormat.format("\t\t\t\t\t(({0})single_value).check_restriction(restriction, name == null ? \"{1}\" : name, legacy);\n", fieldInfo.mJavaTemplateName, displayName));
			source.append("\t\t\t\t\treturn;\n");
		}
		source.append("\t\t\t\tdefault:\n");
		source.append(MessageFormat.format("\t\t\t\t\tthrow new TtcnError(\"Internal error: Invalid selector in a specific value when performing check_restriction operation on a template of union type {0}.\");\n", displayName));
		source.append("\t\t\t\t}\n");
		source.append("\t\t\tcase TR_PRESENT:\n");
		source.append("\t\t\t\tif (!match_omit(legacy)) {\n");
		source.append("\t\t\t\t\treturn;\n");
		source.append("\t\t\t\t}\n");
		source.append("\t\t\t\tbreak;\n");
		source.append("\t\t\tdefault:\n");
		source.append("\t\t\t\treturn;\n");
		source.append("\t\t\t}\n");
		source.append(MessageFormat.format("\t\t\tthrow new TtcnError(MessageFormat.format(\"Restriction `'{'0'}''''' on template of type '{'1'}' violated.\", get_res_name(restriction), name == null ? \"{0}\" : name));\n", displayName));
		source.append("\t\t}\n");
	}

	private static void genRawFieldChecker(final StringBuilder source, final rawAST_coding_taglist taglist, final boolean is_equal) {
		for (int i = 0; i < taglist.fields.size(); i++) {
			final rawAST_coding_field_list fields = taglist.fields.get(i);
			String fieldName = null;
			boolean firstExpr = true;
			boolean optional = false;
			if (i > 0) {
				source.append(is_equal ? " || " : " && ");
			}
			for (int j = 0; j < fields.fields.size(); j++) {
				final rawAST_coding_fields field = fields.fields.get(j);
				if (j == 0) {
					/* this is the first field reference */
					fieldName = MessageFormat.format("(({0})field)", field.type);
				} else {
					/* this is not the first field reference */
					if (field.fieldtype == rawAST_coding_field_type.UNION_FIELD) {
						if (firstExpr) {
							if (taglist.fields.size() > 1) {
								source.append('(');
							}
							firstExpr = false;
						} else {
							source.append(is_equal ? " && " : " || ");
						}
						source.append(MessageFormat.format("{0}.get_selection() {1} union_selection_type.ALT_{2}", fieldName, is_equal ? "==" : "!=", field.nthfieldname));
					}
					fieldName = MessageFormat.format("{0}.get_field_{1}()", fieldName, FieldSubReference.getJavaGetterName( field.nthfieldname ));

				}

				if (j < fields.fields.size() - 1 && field.fieldtype == rawAST_coding_field_type.OPTIONAL_FIELD) {
					if (firstExpr) {
						if (taglist.fields.size() > 1) {
							source.append('(');
						}
						firstExpr = false;
					} else {
						source.append(is_equal ? " && " : " || ");
					}
					if (!is_equal) {
						source.append('!');
					}
					source.append(MessageFormat.format("{0}.is_present()", fieldName));
					fieldName = MessageFormat.format("{0}.get()", fieldName);
				}
			}
			if (fields.fields.get(fields.fields.size() - 1).fieldtype == rawAST_coding_field_type.OPTIONAL_FIELD) {
				optional = true;
			}

			if (!firstExpr) {
				source.append(is_equal ? " && " : " || ");
			}

			final StringBuilder expression = optional ? fields.expression.expression : fields.nativeExpression.expression;
			if (is_equal) {
				source.append(MessageFormat.format("{0}.operator_equals({1})", fieldName, expression));
			} else {
				source.append(MessageFormat.format("!{0}.operator_equals({1})", fieldName, expression));
			}

			if (!firstExpr && taglist.fields.size() > 1) {
				source.append(')');
			}

		}
	}

	private static void genRawTagChecker(final StringBuilder source, final rawAST_coding_taglist taglist) {
		boolean canBeSimple = taglist.fields.size() > 0;
		if (taglist.fields.size() > 1) {
			final rawAST_coding_field_list firstField = taglist.fields.get(0);
			final int firstFieldSize = firstField.fields.size();
			for (int i = 1; i < taglist.fields.size() && canBeSimple; i++) {
				final rawAST_coding_field_list tempField = taglist.fields.get(i);
				if (firstFieldSize != tempField.fields.size()) {
					canBeSimple = false;
				}
				for (int j = 0; j < firstFieldSize && canBeSimple; j++) {
					if (firstField.fields.get(j).nthfield != tempField.fields.get(j).nthfield) {
						canBeSimple = false;
					}
				}
			}
		}
		if (canBeSimple) {
			final rawAST_coding_field_list tempField = taglist.fields.get(0);
			final int tempFieldSize = tempField.fields.size();
			source.append("{\n");
			source.append(MessageFormat.format("final int new_pos{0}[] = new int[myleaf.curr_pos.level + {1}];\n", 0, tempFieldSize));
			source.append(MessageFormat.format("System.arraycopy(myleaf.curr_pos.pos, 0, new_pos{0}, 0, myleaf.curr_pos.level);\n", 0));
			for (int l = 0; l < tempFieldSize; l++) {
				source.append(MessageFormat.format("new_pos{0}[myleaf.curr_pos.level + {1}] = {2};\n", 0, l, tempField.fields.get(l).nthfield));
			}
			source.append(MessageFormat.format("final RAW_enc_tr_pos pr_pos{0} = new RAW_enc_tr_pos(myleaf.curr_pos.level + {1}, new_pos{0});\n", 0, tempFieldSize));
			source.append(MessageFormat.format("final RAW_enc_tree temp_leaf = myleaf.get_node(pr_pos{0});\n", 0));
			source.append("if (temp_leaf != null) {\n");
			source.append(MessageFormat.format("{0}.RAW_encode({1}_descr_, temp_leaf);\n", tempField.expression.expression, tempField.fields.get(tempFieldSize - 1).typedesc));
			source.append(" } else ");
		} else {
			source.append("RAW_enc_tree temp_leaf;\n");
			for (int temp_tag = 0; temp_tag < taglist.fields.size(); temp_tag++) {
				final rawAST_coding_field_list tempField = taglist.fields.get(temp_tag);
				final int tempFieldSize = tempField.fields.size();
				source.append("{\n");
				source.append(MessageFormat.format("int new_pos{0}[] = new int[myleaf.curr_pos.level + {1}];\n", temp_tag, tempFieldSize));
				source.append(MessageFormat.format("System.arraycopy(myleaf.curr_pos.pos, 0, new_pos{0}, 0, myleaf.curr_pos.level);\n", temp_tag));
				for (int l = 0; l < tempFieldSize; l++) {
					source.append(MessageFormat.format("new_pos{0}[myleaf.curr_pos.level + {1}] = {2};\n", temp_tag, l, tempField.fields.get(l).nthfield));
				}
				source.append(MessageFormat.format("final RAW_enc_tr_pos pr_pos{0} = new RAW_enc_tr_pos(myleaf.curr_pos.level + {1}, new_pos{0});\n", temp_tag, tempFieldSize));
				source.append(MessageFormat.format("temp_leaf = myleaf.get_node(pr_pos{0});\n", temp_tag));
				source.append("if (temp_leaf != null) {\n");
				source.append(MessageFormat.format("{0}.RAW_encode({1}_descr_, temp_leaf);\n", tempField.expression.expression, tempField.fields.get(tempFieldSize - 1).typedesc));
				source.append(" } else ");
			}
		}

		source.append(" {\n");
		source.append("TTCN_EncDec_ErrorContext.error(error_type.ET_OMITTED_TAG, \"Encoding a tagged, but omitted value.\", \"\");\n");
		source.append(" }\n");
		if (canBeSimple) {
			source.append("}\n");
		} else {
			for (int temp_tag = taglist.fields.size() - 1; temp_tag >= 0; temp_tag--) {
				source.append("}\n");
			}
		}
	}
}
