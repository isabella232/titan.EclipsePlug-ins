/******************************************************************************
 * Copyright (c) 2000-2020 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.types;

import java.text.MessageFormat;

import org.eclipse.titan.designer.AST.TTCN3.attributes.RawASTStruct;
import org.eclipse.titan.designer.compiler.JavaGenData;

/**
 * Utility class for generating the value and template classes for
 * "record of/set of" types.
 *
 * @author Arpad Lovassy
 */
public final class RecordOfGenerator {

	private RecordOfGenerator() {
		// private to disable instantiation
	}

	/**
	 * Generate "record of/set of" class
	 *
	 * @param aData
	 *                used to access build settings.
	 * @param source
	 *                where the source code is to be generated.
	 * @param genName
	 *                the name of the generated class representing the
	 *                "record of/set of" type.
	 * @param displayName
	 *                the user readable name of the type to be generated.
	 * @param ofTypeName
	 *                type name of the "record of/set of" element
	 * @param isSetOf
	 *                {@code true}: set of, {@code false}: record of
	 * @param hasRaw
	 *                {@code true}: if the type has RAW coding attributes.
	 * @param forceGenSeof
	 *                {@code true}: if code generation is forced.
	 * @param extension_bit
	 *                the raw extension bit to be used if RAW coding
	 *                is to be generated.
	 * @param hasJson
	 *                {@code true}: if the type has JSON coding attributes.
	 * @param localTypeDescriptor
	 *                the code to be generated into the class representing
	 *                the type and coding descriptors of the type.
	 */
	public static void generateValueClass( final JavaGenData aData,
										   final StringBuilder source,
										   final String genName,
										   final String displayName,
										   final String ofTypeName,
										   final boolean isSetOf,
										   final boolean hasRaw,
										   final boolean forceGenSeof,
										   final int extension_bit,
										   final boolean hasJson,
										   final StringBuilder localTypeDescriptor) {
		aData.addImport("java.text.MessageFormat");
		aData.addImport("java.util.List");
		aData.addBuiltinTypeImport("Base_Type");
		aData.addBuiltinTypeImport("JSON_Tokenizer");
		aData.addBuiltinTypeImport("Text_Buf");
		aData.addBuiltinTypeImport("TtcnError");
		aData.addBuiltinTypeImport("TitanInteger");
		aData.addBuiltinTypeImport("TitanNull_Type");
		aData.addBuiltinTypeImport("AdditionalFunctions");
		aData.addBuiltinTypeImport("RecordOf_Match");
		aData.addBuiltinTypeImport("Record_Of_Type");
		aData.addBuiltinTypeImport("TTCN_Logger");
		aData.addBuiltinTypeImport("RAW.RAW_enc_tr_pos");
		aData.addBuiltinTypeImport("RAW.RAW_enc_tree");
		aData.addBuiltinTypeImport("RAW.top_bit_order_t");
		aData.addBuiltinTypeImport("TTCN_Buffer");
		aData.addBuiltinTypeImport("TTCN_EncDec_ErrorContext");
		aData.addBuiltinTypeImport("TTCN_EncDec.coding_type");
		aData.addBuiltinTypeImport("TTCN_EncDec.error_type");
		aData.addBuiltinTypeImport("TTCN_EncDec.raw_order_t");
		aData.addBuiltinTypeImport("Param_Types.Module_Parameter");
		if ( isSetOf ) {
			aData.addBuiltinTypeImport("RecordOf_Match.compare_function_t");
		}

		final boolean rawNeeded = forceGenSeof || hasRaw; //TODO can be forced optionally if needed
		final boolean jsonNeeded = forceGenSeof || hasJson; //TODO can be forced optionally if needed
		if (rawNeeded) {
			aData.addBuiltinTypeImport("RAW.ext_bit_t");
			aData.addBuiltinTypeImport("RAW.RAW_Force_Omit");
		}

		if  (jsonNeeded) {
			aData.addImport("java.util.concurrent.atomic.AtomicInteger");
			aData.addImport("java.util.concurrent.atomic.AtomicReference");
			aData.addBuiltinTypeImport("JSON");
			aData.addBuiltinTypeImport("JSON_Tokenizer.json_token_t");
		}

		source.append(MessageFormat.format("\tpublic static class {0} extends Record_Of_Type '{'\n", genName));

		source.append(localTypeDescriptor);

		generateValueDeclaration( source, genName, ofTypeName, isSetOf );
		generateValueConstructors( aData, source, genName, ofTypeName, displayName );
		generateValueCopyList( source, ofTypeName );
		generateValueIsPresent( source );
		generateValueIsBound( aData, source );
		generateValueIsValue(source, ofTypeName);
		generateValueoperator_equals( aData, source, genName, ofTypeName, displayName, isSetOf );
		generateValueoperator_assign(aData, source, genName, ofTypeName, displayName);
		generateValueConcatenate(aData, source, genName, ofTypeName, displayName );
		generateValueRotate(aData, source, genName, ofTypeName, displayName );
		generateValueCleanup( source );
		generateValueGetterSetters( aData, source, ofTypeName, displayName );
		generateValueGetUnboundElem( source, ofTypeName );
		generateValueToString( source );
		generateValueReplace( aData, source, genName, ofTypeName, displayName );
		generateValueLog( source );
		generateValueSetParam(source, displayName, isSetOf);
		generateValueSetImplicitOmit(source, ofTypeName);
		generateValueEncodeDecodeText(source, ofTypeName, displayName);
		generateValueEncodeDecode(source, ofTypeName, displayName, rawNeeded, forceGenSeof, extension_bit, jsonNeeded);

		source.append("\t}\n");
	}

	/**
	 * Generate "record of/set of" template class
	 *
	 * @param aData
	 *                used to access build settings.
	 * @param source
	 *                where the source code is to be generated.
	 * @param genName
	 *                the name of the generated class representing the
	 *                "record of/set of" type.
	 * @param displayName
	 *                the user readable name of the type to be generated.
	 * @param ofTypeName
	 *                type name of the "record of/set of" element
	 * @param isSetOf
	 *                {@code true}: set of, {@code false}: record of
	 */
	public static void generateTemplateClass( final JavaGenData aData,
											  final StringBuilder source,
											  final String genName,
											  final String displayName,
											  final String ofTypeName,
											  final boolean isSetOf ) {
		aData.addImport("java.util.List");
		aData.addImport("java.util.ArrayList");
		aData.addImport("java.text.MessageFormat");
		aData.addBuiltinTypeImport("Base_Template");
		aData.addBuiltinTypeImport("Text_Buf");
		aData.addBuiltinTypeImport("TitanInteger");
		aData.addBuiltinTypeImport("TtcnError");
		aData.addBuiltinTypeImport("RecordOf_Match");
		aData.addBuiltinTypeImport("RecordOf_Match.match_function_t");
		aData.addBuiltinTypeImport("Restricted_Length_Template");
		aData.addBuiltinTypeImport("Optional");
		aData.addBuiltinTypeImport("TTCN_Logger");
		if ( isSetOf ) {
			aData.addBuiltinTypeImport("Set_Of_Template");
			aData.addBuiltinTypeImport("RecordOf_Match.log_function_t");
		} else {
			aData.addBuiltinTypeImport("Record_Of_Template");
		}

		source.append( MessageFormat.format( "\tpublic static class {0}_template extends {1}_Of_Template '{'\n", genName, isSetOf ? "Set" : "Record" ) );

		generateTemplateDeclaration( source, genName, ofTypeName );
		if ( isSetOf ) {
			generateTemplateDeclarationSetOf( source, genName, ofTypeName );
		}
		generateTemplateConstructors( aData, source, genName, ofTypeName, displayName, isSetOf );
		generateTemplateCopyTemplate( aData, source, genName, ofTypeName, displayName, isSetOf );
		generateTemplateMatch( aData, source, genName, displayName, isSetOf );
		generateTemplateMatchOmit( source );
		generateTemplateoperator_assign(aData, source, genName, ofTypeName, displayName, isSetOf );
		generateTemplateCleanup( source );
		generateTemplateReplace( aData, source, genName, displayName );
		generateTemplateGetterSetters( aData, source, genName, ofTypeName, displayName );
		if ( isSetOf ) {
			generateTemplateGetterSettersSetOf( aData, source, genName, ofTypeName, displayName );
		}

		//TODO only need to be generated in runtime2 or to support template concatenation
		//aData.addImport("java.util.concurrent.atomic.AtomicBoolean");
		//aData.addImport("java.util.concurrent.atomic.AtomicInteger");
		//generateTemplateConcat( source, genName, ofTypeName, displayName );
		//generateTemplateMatchv( source, genName );

		generateTemplateSetSize( aData, source, genName, ofTypeName, displayName, isSetOf );
		generateTemplateNElem( aData, source, genName );
		generateTemplateIsValue( source, genName );
		generateTemplateSetType( source, genName, ofTypeName, displayName, isSetOf );
		generateTemplateListItem( source, genName, displayName );
		generateTemplateGetListItem( aData, source, genName, displayName );
		generateTemplateValueOf( source, genName, ofTypeName, displayName );
		generateTemplateSubstr( aData, source, genName );
		generateTemplateLog( aData, source, genName, displayName, isSetOf );
		generateTemplateEncodeDecodeText(source, genName, displayName, ofTypeName, isSetOf);
		generateTemplateSetParam(source, displayName, isSetOf);
		generateTemplateGetIstemplateKind( source, genName, isSetOf );
		generateTemplateCheckRestriction(source, displayName);

		source.append("\t}\n");
	}

	/**
	 * Generate member variables
	 *
	 * @param source
	 *                where the source code is to be generated.
	 * @param genName
	 *                the name of the generated class representing the
	 *                "record of/set of" type.
	 * @param ofTypeName
	 *                type name of the "record of/set of" element
	 * @param isSetOf
	 *                {@code true}: set of, {@code false}: record of
	 */
	private static void generateValueDeclaration( final StringBuilder source, final String genName, final String ofTypeName,
												  final boolean isSetOf ) {
		source.append('\n');
		source.append( MessageFormat.format( "\t\tprotected List<{0}> valueElements;\n", ofTypeName ) );

		if ( isSetOf ) {
			source.append('\n');
			source.append("\t\tprivate final compare_function_t compare_function_set = new compare_function_t() {\n");
			source.append("\t\t\t@Override\n");
			source.append("\t\t\tpublic boolean compare(final Base_Type left_ptr, final int left_index, final Base_Type right_ptr, final int right_index) {\n");
			source.append( MessageFormat.format( "\t\t\t\treturn compare_set(({0})left_ptr, left_index, ({0})right_ptr, right_index);\n", genName ) );
			source.append("\t\t\t}\n");
			source.append("\t\t};\n");
		}
	}

	/**
	 * Generate constructors
	 *
	 * @param aData
	 *                only used to update imports if needed
	 * @param source
	 *                where the source code is to be generated.
	 * @param genName
	 *                the name of the generated class representing the
	 *                "record of/set of" type.
	 * @param ofTypeName
	 *                type name of the "record of/set of" element
	 * @param displayName
	 *                the user readable name of the type to be generated.
	 */
	private static void generateValueConstructors( final JavaGenData aData, final StringBuilder source, final String genName, final String ofTypeName, final String displayName) {
		source.append('\n');
		if ( aData.isDebug() ) {
			source.append( "\t\t/**\n" );
			source.append( "\t\t * Initializes to unbound value.\n" );
			source.append( "\t\t * */\n" );
		}
		source.append( MessageFormat.format( "\t\tpublic {0}() '{'\n", genName ) );
		source.append("\t\t\t// do nothing\n");
		source.append("\t\t}\n\n");

		if ( aData.isDebug() ) {
			source.append( "\t\t/**\n" );
			source.append( "\t\t * Initializes to a given value.\n" );
			source.append( "\t\t *\n" );
			source.append( "\t\t * @param otherValue\n" );
			source.append( "\t\t *                the value to initialize to.\n" );
			source.append( "\t\t * */\n" );
		}
		source.append( MessageFormat.format("\t\tpublic {0}( final {0} otherValue ) '{'\n", genName ) );
		source.append( MessageFormat.format("\t\t\totherValue.must_bound(\"Copying an unbound value of type {0}.\");\n", displayName ) );
		source.append("\t\t\tvalueElements = copy_list( otherValue.valueElements );\n");
		source.append("\t\t}\n\n");

		if ( aData.isDebug() ) {
			source.append( "\t\t/**\n" );
			source.append( "\t\t * Initializes to a given value.\n" );
			source.append( "\t\t *\n" );
			source.append( "\t\t * @param otherValue\n" );
			source.append( "\t\t *                the value to initialize to.\n" );
			source.append( "\t\t * */\n" );
		}
		source.append( MessageFormat.format("\t\tpublic {0}( final Record_Of_Type otherValue ) '{'\n", genName ) );
		source.append( MessageFormat.format("\t\t\totherValue.must_bound(\"Copying an unbound value of type {0}.\");\n", displayName ) );
		source.append("\t\t\tvalueElements = copy_list( otherValue );\n");
		source.append("\t\t}\n\n");

		if ( aData.isDebug() ) {
			source.append( "\t\t/**\n" );
			source.append( "\t\t * Initializes to a given value.\n" );
			source.append( "\t\t *\n" );
			source.append( "\t\t * @param otherValue\n" );
			source.append( "\t\t *                the value to initialize to.\n" );
			source.append( "\t\t * */\n" );
		}
		source.append( MessageFormat.format( "\t\tpublic {0}(final TitanNull_Type nullValue) '{'\n", genName ) );
		source.append( MessageFormat.format( "\t\t\tvalueElements = new ArrayList<{0}>();\n", ofTypeName ) );
		source.append("\t\t}\n");
	}

	/**
	 * Generate the copyList function
	 *
	 * @param source
	 *                where the source code is to be generated.
	 * @param ofTypeName
	 *                type name of the "record of/set of" element
	 */
	private static void generateValueCopyList( final StringBuilder source, final String ofTypeName ) {
		source.append('\n');
		source.append( MessageFormat.format( "\t\tprivate static final List<{0}> copy_list( final List<{0}> srcList ) '{'\n", ofTypeName ) );
		source.append("\t\t\tif ( srcList == null ) {\n");
		source.append("\t\t\t\treturn null;\n");
		source.append("\t\t\t}\n");
		source.append('\n');
		source.append( MessageFormat.format( "\t\t\tfinal List<{0}> newList = new ArrayList<{0}>( srcList.size() );\n", ofTypeName ) );
		source.append( MessageFormat.format( "\t\t\tfor (final {0} srcElem : srcList) '{'\n", ofTypeName ) );
		source.append( MessageFormat.format( "\t\t\t\tfinal {0} newElem = get_unbound_elem();\n", ofTypeName ) );
		source.append("\t\t\t\tif (srcElem.is_bound()) {\n");
		source.append("\t\t\t\t\tnewElem.operator_assign( srcElem );\n");
		source.append("\t\t\t\t}\n");
		source.append("\t\t\t\tnewList.add( newElem );\n");
		source.append("\t\t\t}\n");
		source.append("\t\t\treturn newList;\n");
		source.append("\t\t}\n\n");

		source.append( MessageFormat.format( "\t\tprivate static final List<{0}> copy_list( final Record_Of_Type otherValue ) '{'\n", ofTypeName ) );
		source.append( MessageFormat.format( "\t\t\tfinal List<{0}> newList = new ArrayList<{0}>( otherValue.n_elem() );\n", ofTypeName ) );
		source.append("\t\t\tfor (int i = 0; i < otherValue.n_elem(); i++) {\n");
		source.append("\t\t\t\tfinal Base_Type srcElem = otherValue.constGet_at(i);\n");
		source.append( MessageFormat.format( "\t\t\t\tfinal {0} newElem = get_unbound_elem();\n", ofTypeName ) );
		source.append("\t\t\t\tif (srcElem.is_bound()) {\n");
		source.append("\t\t\t\t\tnewElem.operator_assign( srcElem );\n");
		source.append("\t\t\t\t}\n");
		source.append("\t\t\t\tnewList.add( newElem );\n");
		source.append("\t\t\t}\n");
		source.append("\t\t\treturn newList;\n");
		source.append("\t\t}\n\n");
	}

	/**
	 * Generate the isPresent function
	 *
	 * @param source
	 *                where the source code is to be generated.
	 */
	private static void generateValueIsPresent(final StringBuilder source) {
		source.append('\n');
		source.append("\t\t@Override\n");
		source.append("\t\tpublic boolean is_present() {\n");
		source.append("\t\t\treturn is_bound();\n");
		source.append("\t\t}\n");
	}

	/**
	 * Generate the isBound function
	 *
	 *@param aData
	 *                only used to update imports if needed
	 * @param source
	 *                where the source code is to be generated.
	 */
	private static void generateValueIsBound(final JavaGenData aData, final StringBuilder source) {
		source.append('\n');
		source.append("\t\t@Override\n");
		source.append("\t\tpublic boolean is_bound() {\n");
		source.append("\t\t\treturn valueElements != null;\n");
		source.append("\t\t}\n");
		source.append('\n');
	}

	/**
	 * Generate the isValue function
	 *
	 * @param source
	 *                where the source code is to be generated.
	 * @param ofTypeName
	 *                type name of the "record of/set of" element
	 */
	private static void generateValueIsValue(final StringBuilder source, final String ofTypeName) {
		source.append('\n');
		source.append("\t\t@Override\n");
		source.append("\t\tpublic boolean is_value() {\n");
		source.append("\t\t\tif (valueElements == null) {\n");
		source.append("\t\t\t\treturn false;\n");
		source.append("\t\t\t}\n");
		source.append("\t\t\tfinal int elements_size = valueElements.size();\n");
		source.append("\t\t\tfor (int i=0; i < elements_size; i++) {\n");
		source.append( MessageFormat.format( "\t\t\t\tfinal {0} elem = valueElements.get(i);\n", ofTypeName ) );
		source.append("\t\t\t\tif (elem == null || !elem.is_value()) {\n");
		source.append("\t\t\t\t\treturn false;\n");
		source.append("\t\t\t\t}\n");
		source.append("\t\t\t}\n");
		source.append("\t\t\treturn true;\n");
		source.append("\t\t}\n");
		source.append('\n');
	}

	/**
	 * Generate assignment operators
	 *
	 * @param aData
	 *                only used to update imports if needed
	 * @param source
	 *                where the source code is to be generated.
	 * @param genName
	 *                the name of the generated class representing the
	 *                "record of/set of" type.
	 * @param ofTypeName
	 *                type name of the "record of/set of" element
	 * @param displayName
	 *                the user readable name of the type to be generated.
	 * @param isSetOf
	 *                {@code true}: set of, {@code false}: record of
	 */
	private static void generateValueoperator_equals( final JavaGenData aData, final StringBuilder source, final String genName, final String ofTypeName,
													 final String displayName, final boolean isSetOf ) {
		source.append('\n');
		source.append("\t\t@Override\n");
		source.append("\t\tpublic boolean operator_equals(final Base_Type other_value) {\n");
		source.append( MessageFormat.format( "\t\t\tif (other_value instanceof {0}) '{'\n", genName) );
		source.append( MessageFormat.format( "\t\t\t\treturn operator_equals(({0})other_value);\n", genName) );
		source.append("\t\t\t}\n");
		source.append("\t\t\tif (other_value instanceof Record_Of_Type) {\n");
		source.append("\t\t\t\treturn operator_equals((Record_Of_Type)other_value);\n");
		source.append("\t\t\t}\n");
		source.append( MessageFormat.format( "\t\t\tthrow new TtcnError(\"Internal Error: The left operand of comparison is not of type {0}.\");\n", genName ) );
		source.append("\t\t}\n");
		source.append('\n');
//Record_Of_Type
		if (aData.isDebug()) {
			source.append("\t\t/**\n");
			source.append("\t\t * Checks if the current value is equivalent to the provided one.\n");
			source.append("\t\t *\n");
			source.append("\t\t * operator== in the core\n");
			source.append("\t\t *\n");
			source.append("\t\t * @param otherValue\n");
			source.append("\t\t *                the other value to check against.\n");
			source.append("\t\t * @return true if the values are equivalent.\n");
			source.append("\t\t */\n");
		}
		source.append("\t\tpublic boolean operator_equals( final TitanNull_Type nullValue) {\n");
		source.append( MessageFormat.format( "\t\t\tmust_bound(\"The left operand of comparison is an unbound value of type {0}.\");\n", displayName ) );
		source.append("\t\t\treturn valueElements.isEmpty();\n");
		source.append("\t\t}\n\n");

		if (aData.isDebug()) {
			source.append("\t\t/**\n");
			source.append("\t\t * Checks if the current value is equivalent to the provided one.\n");
			source.append("\t\t *\n");
			source.append("\t\t * operator== in the core\n");
			source.append("\t\t *\n");
			source.append("\t\t * @param other_value\n");
			source.append("\t\t *                the other value to check against.\n");
			source.append("\t\t * @return true if the values are equivalent.\n");
			source.append("\t\t */\n");
		}
		source.append( MessageFormat.format( "\t\tpublic boolean operator_equals( final {0} other_value ) '{'\n", genName ) );
		source.append( MessageFormat.format( "\t\t\tmust_bound(\"The left operand of comparison is an unbound value of type {0}.\");\n", displayName ) );
		source.append( MessageFormat.format( "\t\t\tother_value.must_bound(\"The right operand of comparison is an unbound value of type {0}.\");\n", displayName ) );
		source.append("\t\t\tif (this == other_value) {\n");
		source.append("\t\t\t\treturn true;\n");
		source.append("\t\t\t}\n");
		source.append('\n');
		if ( isSetOf ) {
			source.append("\t\t\treturn RecordOf_Match.compare_set_of(this, valueElements.size(), other_value, other_value.valueElements.size(), compare_function_set);\n");
		} else {
			source.append("\t\t\tfinal int size = valueElements.size();\n");
			source.append("\t\t\tif ( size != other_value.valueElements.size() ) {\n");
			source.append("\t\t\t\treturn false;\n");
			source.append("\t\t\t}\n");
			source.append('\n');
			source.append("\t\t\tfor ( int i = 0; i < size; i++ ) {\n");
			source.append( MessageFormat.format( "\t\t\t\tfinal {0} leftElem = valueElements.get( i );\n", ofTypeName ) );
			source.append( MessageFormat.format( "\t\t\t\tfinal {0} rightElem = other_value.valueElements.get( i );\n", ofTypeName ) );
			source.append("\t\t\t\tif (leftElem.is_bound()) {\n");
			source.append("\t\t\t\t\tif (rightElem.is_bound()) {\n");
			source.append("\t\t\t\t\t\tif ( !leftElem.operator_equals( rightElem ) ) {\n");
			source.append("\t\t\t\t\t\t\treturn false;\n");
			source.append("\t\t\t\t\t\t}\n");
			source.append("\t\t\t\t\t} else {\n");
			source.append("\t\t\t\t\t\treturn false;\n");
			source.append("\t\t\t\t\t}\n");
			source.append("\t\t\t\t} else if (rightElem.is_bound()) {\n");
			source.append("\t\t\t\t\treturn false;\n");
			source.append("\t\t\t\t}\n");
			source.append("\t\t\t}\n");
			source.append('\n');
			source.append("\t\t\treturn true;\n");
		}
		source.append("\t\t}\n\n");

		source.append("\t\t@Override\n");
		source.append( "\t\tpublic boolean operator_equals( final Record_Of_Type other_value ) {\n" );
		source.append( MessageFormat.format( "\t\t\tmust_bound(\"The left operand of comparison is an unbound value of type {0}.\");\n", displayName ) );
		source.append( "\t\t\tother_value.must_bound(\"The right operand of comparison is an unbound value.\");\n" );
		source.append("\t\t\tif (this == other_value) {\n");
		source.append("\t\t\t\treturn true;\n");
		source.append("\t\t\t}\n\n");
		if ( isSetOf ) {
			source.append("\t\t\treturn RecordOf_Match.compare_set_of(this, valueElements.size(), other_value, other_value.n_elem(), compare_function_set);\n");
		} else {
			source.append("\t\t\tfinal int size = valueElements.size();\n");
			source.append("\t\t\tif ( size != other_value.n_elem() ) {\n");
			source.append("\t\t\t\treturn false;\n");
			source.append("\t\t\t}\n");
			source.append('\n');
			source.append("\t\t\tfor ( int i = 0; i < size; i++ ) {\n");
			source.append( MessageFormat.format( "\t\t\t\tfinal {0} leftElem = valueElements.get( i );\n", ofTypeName ) );
			source.append( "\t\t\t\tfinal Base_Type rightElem = other_value.constGet_at(i);\n" );
			source.append("\t\t\t\tif (leftElem.is_bound()) {\n");
			source.append("\t\t\t\t\tif (rightElem.is_bound()) {\n");
			source.append("\t\t\t\t\t\tif ( !leftElem.operator_equals( rightElem ) ) {\n");
			source.append("\t\t\t\t\t\t\treturn false;\n");
			source.append("\t\t\t\t\t\t}\n");
			source.append("\t\t\t\t\t} else {\n");
			source.append("\t\t\t\t\t\treturn false;\n");
			source.append("\t\t\t\t\t}\n");
			source.append("\t\t\t\t} else if (rightElem.is_bound()) {\n");
			source.append("\t\t\t\t\treturn false;\n");
			source.append("\t\t\t\t}\n");
			source.append("\t\t\t}\n");
			source.append('\n');
			source.append("\t\t\treturn true;\n");
		}
		source.append("\t\t}\n\n");

		if ( isSetOf ) {
			source.append( MessageFormat.format( "\t\tprivate boolean compare_set(final {0} left_ptr, final int left_index, final {0} right_ptr, final int right_index) '{'\n", genName ) );
			source.append("\t\t\tif (left_ptr.valueElements == null) {\n");
			source.append( MessageFormat.format( "\t\t\t\tthrow new TtcnError( \"The left operand of comparison is an unbound value of type {0}.\");\n", displayName ) );
			source.append("\t\t\t}\n");
			source.append("\t\t\tif (right_ptr.valueElements == null) {\n");
			source.append( MessageFormat.format( "\t\t\t\tthrow new TtcnError( \"The right operand of comparison is an unbound value of type {0}.\");\n", displayName ) );
			source.append("\t\t\t}\n");
			source.append( MessageFormat.format( "\t\t\tfinal {0} temp_left = left_ptr.valueElements.get(left_index);\n", ofTypeName ) );
			source.append( MessageFormat.format( "\t\t\tfinal {0} temp_right = right_ptr.valueElements.get(right_index);\n", ofTypeName ) );
			source.append("\t\t\tif (temp_left.is_bound()) {\n");
			source.append("\t\t\t\tif (temp_right.is_bound()){\n");
			source.append("\t\t\t\t\treturn temp_left.operator_equals( temp_right );\n");
			source.append("\t\t\t\t} else  {\n");
			source.append("\t\t\t\t\treturn false;\n");
			source.append("\t\t\t\t}\n");
			source.append("\t\t\t} else {\n");
			source.append("\t\t\t\treturn !temp_right.is_bound();\n");
			source.append("\t\t\t}\n");
			source.append("\t\t}\n\n");
		}

		if (aData.isDebug()) {
			source.append("\t\t/**\n");
			source.append("\t\t * Checks if the current value is not equivalent to the provided one.\n");
			source.append("\t\t *\n");
			source.append("\t\t * operator!= in the core\n");
			source.append("\t\t *\n");
			source.append("\t\t * @param otherValue\n");
			source.append("\t\t *                the other value to check against.\n");
			source.append("\t\t * @return true if the values are not equivalent.\n");
			source.append("\t\t */\n");
		}
		source.append("\t\tpublic boolean operator_not_equals( final TitanNull_Type nullValue) {\n");
		source.append("\t\t\treturn !operator_equals(nullValue);\n");
		source.append("\t\t}\n\n");

		if (aData.isDebug()) {
			source.append("\t\t/**\n");
			source.append("\t\t * Checks if the current value is not equivalent to the provided one.\n");
			source.append("\t\t *\n");
			source.append("\t\t * operator!= in the core\n");
			source.append("\t\t *\n");
			source.append("\t\t * @param otherValue\n");
			source.append("\t\t *                the other value to check against.\n");
			source.append("\t\t * @return true if the values are not equivalent.\n");
			source.append("\t\t */\n");
		}
		source.append( MessageFormat.format( "\t\tpublic boolean operator_not_equals( final {0} otherValue ) '{'\n", genName ) );
		source.append("\t\t\treturn !operator_equals(otherValue);\n");
		source.append("\t\t}\n\n");

		source.append("\t\tpublic boolean operator_not_equals( final Record_Of_Type otherValue ) {\n" );
		source.append("\t\t\treturn !operator_equals(otherValue);\n");
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
	 *                "record of/set of" type.
	 * @param ofTypeName
	 *                type name of the "record of/set of" element
	 * @param displayName
	 *                the user readable name of the type to be generated.
	 */
	private static void generateValueoperator_assign(final JavaGenData aData, final StringBuilder source, final String genName, final String ofTypeName, final String displayName ) {
		source.append('\n');
		source.append("\t\t@Override\n");
		source.append( MessageFormat.format( "\t\tpublic {0} operator_assign(final Base_Type other_value) '{'\n", genName ) );
		source.append( MessageFormat.format( "\t\t\tif (other_value instanceof {0}) '{'\n", genName) );
		source.append( MessageFormat.format( "\t\t\t\treturn operator_assign(({0})other_value);\n", genName) );
		source.append("\t\t\t}\n\n");
		source.append("\t\t\tif (other_value instanceof Record_Of_Type) {\n");
		source.append("\t\t\t\treturn operator_assign((Record_Of_Type)other_value);\n");
		source.append("\t\t\t}\n\n");
		source.append( MessageFormat.format( "\t\t\tthrow new TtcnError(\"Internal Error: The left operand of assignment is not of type {0}.\");\n", genName ) );
		source.append("\t\t}\n\n");

		source.append("\t\t@Override\n");
		source.append( MessageFormat.format( "\t\tpublic {0} operator_assign( final Record_Of_Type other_value ) '{'\n", genName ) );
		source.append( MessageFormat.format("\t\t\tother_value.must_bound( \"Assigning an unbound value of type {0}.\" );\n", displayName));
		source.append("\t\t\tif (this == other_value) {\n");
		source.append("\t\t\t\treturn this;\n");
		source.append("\t\t\t}\n");
		source.append('\n');
		source.append("\t\t\tvalueElements = copy_list( other_value );\n");
		source.append("\t\t\treturn this;\n");
		source.append("\t\t}\n\n");

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
		source.append( MessageFormat.format( "\t\tpublic {0} operator_assign( final {0} otherValue ) '{'\n", genName ) );
		source.append( MessageFormat.format("\t\t\totherValue.must_bound( \"Assigning an unbound value of type {0}.\" );\n", displayName));
		source.append("\t\t\tif (this == otherValue) {\n");
		source.append("\t\t\t\treturn this;\n");
		source.append("\t\t\t}\n");
		source.append('\n');
		source.append("\t\t\tvalueElements = copy_list( otherValue.valueElements );\n");
		source.append("\t\t\treturn this;\n");
		source.append("\t\t}\n\n");

		if ( aData.isDebug() ) {
			source.append("\t\t/**\n");
			source.append("\t\t * Sets the current value to unbound.\n");
			source.append("\t\t * Overwriting the current content in the process.\n");
			source.append("\t\t *<p>\n");
			source.append("\t\t * operator= in the core.\n");
			source.append("\t\t *\n");
			source.append("\t\t * @param nullValue\n");
			source.append("\t\t *                the null value.\n");
			source.append("\t\t * @return the new value object.\n");
			source.append("\t\t */\n");
		}
		source.append( MessageFormat.format( "\t\tpublic {0} operator_assign(final TitanNull_Type nullValue) '{'\n", genName ) );
		source.append( MessageFormat.format( "\t\t\tvalueElements = new ArrayList<{0}>();\n", ofTypeName ) );
		source.append("\t\t\treturn this;\n");
		source.append("\t\t}\n\n");
	}

	/**
	 * Generate concatenate function
	 *
	 * @param aData
	 *                used to access build settings.
	 * @param source
	 *                where the source code is to be generated.
	 * @param genName
	 *                the name of the generated class representing the
	 *                "record of/set of" type.
	 * @param ofTypeName
	 *                type name of the "record of/set of" element
	 * @param displayName
	 *                the user readable name of the type to be generated.
	 */
	private static void generateValueConcatenate(final JavaGenData aData, final StringBuilder source, final String genName, final String ofTypeName, final String displayName ) {
		if (aData.isDebug()) {
			source.append("\t\t/**\n");
			source.append("\t\t * Concatenates the current record/set of with the other received as a\n");
			source.append("\t\t * parameter.\n");
			source.append("\t\t *\n");
			source.append("\t\t * operator+ in the core.\n");
			source.append("\t\t *\n");
			source.append("\t\t * @param other_value\n");
			source.append("\t\t *                the other value to concatenate with.\n");
			source.append("\t\t * @return the new record/set of representing the concatenated value.\n");
			source.append("\t\t * */\n");
		}
		source.append( MessageFormat.format( "\t\tpublic {0} operator_concatenate(final {0} other_value) '{'\n", genName ) );
		source.append("\t\t\tif (valueElements == null || other_value.valueElements == null) {\n");
		source.append( MessageFormat.format( "\t\t\t\tthrow new TtcnError(\"Unbound operand of {0} concatenation.\");\n", displayName ) );
		source.append("\t\t\t}\n");
		source.append( MessageFormat.format( "\t\t\tfinal {0} ret_val = new {0}(TitanNull_Type.NULL_VALUE);\n", genName ) );
		source.append("\t\t\tfinal int elements_size = valueElements.size();\n");
		source.append("\t\t\tfor (int i=0; i < elements_size; i++) {\n");
		source.append( MessageFormat.format( "\t\t\t\tfinal {0} elem = valueElements.get(i);\n", ofTypeName ) );
		source.append("\t\t\t\tif (elem != null) {\n");
		source.append( MessageFormat.format( "\t\t\t\t\tret_val.valueElements.add(new {0}(elem));\n", ofTypeName ) );
		source.append("\t\t\t\t}\n");
		source.append("\t\t\t}\n");
		source.append("\t\t\tfinal int other_elements_size = other_value.valueElements.size();\n");
		source.append("\t\t\tfor (int i = 0; i < other_elements_size; i++) {\n");
		source.append( MessageFormat.format( "\t\t\t\tfinal {0} elem = other_value.valueElements.get(i);\n", ofTypeName ) );
		source.append("\t\t\t\tif (elem != null) {\n");
		source.append( MessageFormat.format( "\t\t\t\t\tret_val.valueElements.add(new {0}(elem));\n", ofTypeName ) );
		source.append("\t\t\t\t}\n");
		source.append("\t\t\t}\n");
		source.append("\t\t\treturn ret_val;\n");
		source.append("\t\t}\n\n");

		if (aData.isDebug()) {
			source.append("\t\t/**\n");
			source.append("\t\t * Concatenates the current record/set of with a zero length list.\n");
			source.append("\t\t * Effectively creates a copy of the actual record/set of value.\n");
			source.append("\t\t *\n");
			source.append("\t\t * operator+ in the core.\n");
			source.append("\t\t *\n");
			source.append("\t\t * @param null_value\n");
			source.append("\t\t *                used only to indicate concatenation with an empty list.\n");
			source.append("\t\t * @return the new record/set of representing the concatenated value.\n");
			source.append("\t\t * */\n");
		}
		source.append( MessageFormat.format( "\t\tpublic {0} operator_concatenate(final TitanNull_Type null_value) '{'\n", genName ) );
		source.append( MessageFormat.format( "\t\t\treturn new {0}(this);\n", genName ) );
		source.append("\t\t}\n");
	}

	/**
	 * Generate rotate functions
	 *
	 * @param aData
	 *                used to access build settings.
	 * @param source
	 *                where the source code is to be generated.
	 * @param genName
	 *                the name of the generated class representing the
	 *                "record of/set of" type.
	 * @param ofTypeName
	 *                type name of the "record of/set of" element
	 * @param displayName
	 *                the user readable name of the type to be generated.
	 */
	private static void generateValueRotate(final JavaGenData aData, final StringBuilder source, final String genName, final String ofTypeName, final String displayName ) {
		source.append('\n');
		if (aData.isDebug()) {
			source.append("\t\t/**\n");
			source.append( MessageFormat.format( "\t\t * Creates a new {0}, that is the equivalent of the\n", displayName ) );
			source.append("\t\t * current one with its elements rotated to the left with the provided\n");
			source.append("\t\t * amount.\n");
			source.append("\t\t *\n");
			source.append("\t\t * operator<<= in the core.\n");
			source.append("\t\t *\n");
			source.append("\t\t * @param rotate_count\n");
			source.append("\t\t *                the number of characters to rotate left.\n");
			source.append( MessageFormat.format( "\t\t * @return the new {0}.\n", displayName ) );
			source.append("\t\t * */\n");
		}
		source.append( MessageFormat.format( "\t\tpublic {0} rotate_left(final TitanInteger rotate_count) '{'\n", genName ) );
		source.append("\t\t\trotate_count.must_bound(\"Unbound integer operand of rotate left operator.\");\n");
		source.append("\t\t\treturn rotate_left(rotate_count.get_int());\n");
		source.append("\t\t}\n");

		source.append('\n');
		if (aData.isDebug()) {
			source.append("\t\t/**\n");
			source.append( MessageFormat.format( "\t\t * Creates a new {0}, that is the equivalent of the\n", displayName ) );
			source.append("\t\t * current one with its elements rotated to the left with the provided\n");
			source.append("\t\t * amount.\n");
			source.append("\t\t *\n");
			source.append("\t\t * operator<<= in the core.\n");
			source.append("\t\t *\n");
			source.append("\t\t * @param rotate_count\n");
			source.append("\t\t *                the number of characters to rotate left.\n");
			source.append( MessageFormat.format( "\t\t * @return the new {0}.\n", displayName ) );
			source.append("\t\t * */\n");
		}
		source.append( MessageFormat.format( "\t\tpublic {0} rotate_left(final int rotate_count) '{'\n", genName ) );
		source.append("\t\t\treturn rotate_right(-rotate_count);\n");
		source.append("\t\t}\n");

		source.append('\n');
		if (aData.isDebug()) {
			source.append("\t\t/**\n");
			source.append( MessageFormat.format( "\t\t * Creates a new {0}, that is the equivalent of the\n", displayName ) );
			source.append("\t\t * current one with its elements rotated to the right with the provided\n");
			source.append("\t\t * amount.\n");
			source.append("\t\t *\n");
			source.append("\t\t * operator<<= in the core.\n");
			source.append("\t\t *\n");
			source.append("\t\t * @param rotate_count\n");
			source.append("\t\t *                the number of characters to rotate right.\n");
			source.append( MessageFormat.format( "\t\t * @return the new {0}.\n", displayName ) );
			source.append("\t\t * */\n");
		}
		source.append( MessageFormat.format( "\t\tpublic {0} rotate_right(final TitanInteger rotate_count) '{'\n", genName ) );
		source.append("\t\t\trotate_count.must_bound(\"Unbound integer operand of rotate right operator.\");\n");
		source.append("\t\t\treturn rotate_right(rotate_count.get_int());\n");
		source.append("\t\t}\n");

		source.append('\n');
		if (aData.isDebug()) {
			source.append("\t\t/**\n");
			source.append( MessageFormat.format( "\t\t * Creates a new {0}, that is the equivalent of the\n", displayName ) );
			source.append("\t\t * current one with its elements rotated to the right with the provided\n");
			source.append("\t\t * amount.\n");
			source.append("\t\t *\n");
			source.append("\t\t * operator<<= in the core.\n");
			source.append("\t\t *\n");
			source.append("\t\t * @param rotate_count\n");
			source.append("\t\t *                the number of characters to rotate right.\n");
			source.append( MessageFormat.format( "\t\t * @return the new {0}.\n", displayName ) );
			source.append("\t\t * */\n");
		}
		source.append( MessageFormat.format( "\t\tpublic {0} rotate_right(final int rotate_count) '{'\n", genName ) );
		source.append( MessageFormat.format( "\t\t\tmust_bound(\"Performing rotation operation on an unbound value of type {0}.\");\n", displayName ) );
		source.append("\t\t\tfinal int size = valueElements.size();\n");
		source.append("\t\t\tif (size == 0) {\n");
		source.append( MessageFormat.format( "\t\t\t\treturn new {0}(TitanNull_Type.NULL_VALUE);\n", genName ) );
		source.append("\t\t\t}\n");
		source.append("\t\t\tint rc;\n");
		source.append("\t\t\tif (rotate_count >= 0) {\n");
		source.append("\t\t\t\trc = rotate_count % size;\n");
		source.append("\t\t\t} else {\n");
		source.append("\t\t\t\trc = size - ((-rotate_count) % size);\n");
		source.append("\t\t\t}\n");
		source.append("\t\t\tif (rc == 0) {\n");
		source.append( MessageFormat.format( "\t\t\t\treturn new {0}(this);\n", genName ) );
		source.append("\t\t\t}\n");
		source.append( MessageFormat.format( "\t\t\tfinal {0} ret_val = new {0}(TitanNull_Type.NULL_VALUE);\n", genName ) );
		source.append("\t\t\tfor (int i = size - rc; i < size; i++) {\n");
		source.append( MessageFormat.format( "\t\t\t\tfinal {0} elem = valueElements.get(i);\n", ofTypeName ) );
		source.append("\t\t\t\tif (elem != null) {\n");
		source.append( MessageFormat.format( "\t\t\t\t\tret_val.valueElements.add(new {0}(elem));\n", ofTypeName ) );
		source.append("\t\t\t\t}\n");
		source.append("\t\t\t}\n");
		source.append("\t\t\tfor (int i = 0; i < size - rc; i++) {\n");
		source.append( MessageFormat.format( "\t\t\t\tfinal {0} elem = valueElements.get(i);\n", ofTypeName ) );
		source.append("\t\t\t\tif (elem != null) {\n");
		source.append( MessageFormat.format( "\t\t\t\t\tret_val.valueElements.add(new {0}(elem));\n", ofTypeName ) );
		source.append("\t\t\t\t}\n");
		source.append("\t\t\t}\n");
		source.append("\t\t\treturn ret_val;\n");
		source.append("\t\t}\n");
	}

	/**
	 * Generate the clean_up function
	 *
	 * @param source
	 *                where the source code is to be generated.
	 */
	private static void generateValueCleanup(final StringBuilder source) {
		source.append('\n');
		source.append("\t\t@Override\n");
		source.append("\t\tpublic void clean_up() {\n");
		source.append("\t\t\tvalueElements = null;\n");
		source.append("\t\t}\n\n");
	}

	/**
	 * Generate getter and setter functions
	 *
	 * @param aData
	 *                only used to update imports if needed
	 * @param source
	 *                where the source code is to be generated.
	 * @param ofTypeName
	 *                type name of the "record of/set of" element
	 * @param displayName
	 *                the user readable name of the type to be generated.
	 */
	private static void generateValueGetterSetters(final JavaGenData aData, final StringBuilder source, final String ofTypeName , final String displayName) {
		source.append("\t\t@Override\n");
		source.append( MessageFormat.format("\t\tpublic {0} get_at( final int index_value ) '{'\n", ofTypeName ) );
		source.append("\t\t\tif (index_value < 0) {\n");
		source.append( MessageFormat.format( "\t\t\t\tthrow new TtcnError( \"Accessing an element of type {0} using a negative index: \"+index_value+\".\");\n", displayName ) );
		source.append("\t\t\t}\n");
		source.append('\n');
		source.append("\t\t\tif (valueElements == null || index_value >= valueElements.size() ) {\n");
		source.append("\t\t\t\t//increase list size\n");
		source.append("\t\t\t\tset_size(index_value + 1);\n");
		source.append("\t\t\t}\n");
		source.append('\n');
		source.append( MessageFormat.format("\t\t\t{0} temp = valueElements.get( index_value );\n", ofTypeName ) );
		source.append("\t\t\tif ( temp == null ) {\n");
		source.append("\t\t\t\ttemp = get_unbound_elem();\n");
		source.append("\t\t\t\tvalueElements.set( index_value, temp );\n");
		source.append("\t\t\t}\n");
		source.append("\t\t\treturn temp;\n");
		source.append("\t\t}\n\n");

		source.append("\t\t@Override\n");
		source.append( MessageFormat.format("\t\tpublic {0} get_at(final TitanInteger index_value) '{'\n", ofTypeName ) );
		source.append( MessageFormat.format( "\t\t\tindex_value.must_bound( \"Using an unbound integer value for indexing a value of type {0}.\" );\n", displayName ) );
		source.append("\t\t\treturn get_at( index_value.get_int() );\n");
		source.append("\t\t}\n\n");

		source.append("\t\t@Override\n");
		source.append( MessageFormat.format("\t\tpublic {0} constGet_at( final int index_value ) '{'\n", ofTypeName ) );
		source.append( MessageFormat.format( "\t\t\tmust_bound( \"Accessing an element in an unbound value of type {0}.\" );\n", displayName ) );
		source.append("\t\t\tif (index_value < 0) {\n");
		source.append( MessageFormat.format( "\t\t\t\tthrow new TtcnError( \"Accessing an element of type {0} using a negative index: \"+index_value+\".\");\n", displayName ) );
		source.append("\t\t\t}\n");
		source.append("\t\t\tfinal int nofElements = valueElements.size();\n");
		source.append("\t\t\tif ( index_value >= nofElements ) {\n");
		source.append( MessageFormat.format( "\t\t\t\tthrow new TtcnError( \"Index overflow in a value of type {0}: The index is \"+index_value+\", but the value has only \"+nofElements+\" elements.\" );\n", displayName ) );
		source.append("\t\t\t}\n");
		source.append('\n');
		source.append( MessageFormat.format( "\t\t\tfinal {0} elem = valueElements.get( index_value );\n", ofTypeName ) );
		source.append("\t\t\treturn ( elem == null ) ? get_unbound_elem(): elem ;\n");
		source.append("\t\t}\n\n");

		source.append("\t\t@Override\n");
		source.append( MessageFormat.format( "\t\tpublic {0} constGet_at(final TitanInteger index_value) '{'\n", ofTypeName ) );
		source.append( MessageFormat.format( "\t\t\tindex_value.must_bound( \"Using an unbound integer value for indexing a value of type {0}.\" );\n", displayName ) );
		source.append("\t\t\treturn constGet_at( index_value.get_int() );\n");
		source.append("\t\t}\n\n");

		if (aData.isDebug()) {
			source.append("\t\t/**\n");
			source.append("\t\t * Returns the number of elements.\n");
			source.append("\t\t * The value to be returned is the maximum of the minimal length restriction value of the type,\n");
			source.append("\t\t *  or 0 for types with no minimal length restriction,\n");
			source.append("\t\t *  and the index of the last initialized element plus 1.\n");
			source.append("\t\t *\n");
			source.append("\t\t * size_of in the core.\n");
			source.append("\t\t * deprecated by the standard.\n");
			source.append("\t\t *\n");
			source.append("\t\t * @return the number of elements.\n");
			source.append("\t\t * */\n");
		}
		source.append("\t\tpublic TitanInteger size_of() {\n");
		source.append( MessageFormat.format( "\t\t\tmust_bound(\"Performing sizeof operation on an unbound value of type {0}.\");\n", displayName ) );
		source.append("\t\t\treturn new TitanInteger(valueElements.size());\n");
		source.append("\t\t}\n\n");

		source.append("\t\t@Override\n");
		source.append("\t\tpublic int n_elem() {\n");
		source.append("\t\t\treturn size_of().get_int();\n");
		source.append("\t\t}\n\n");

		if (aData.isDebug()) {
			source.append("\t\t/**\n");
			source.append("\t\t * Returns the number of elements, that is, the largest used index plus\n");
			source.append("\t\t * one and zero for the empty value.\n");
			source.append("\t\t *\n");
			source.append("\t\t * lengthof in the core\n");
			source.append("\t\t *\n");
			source.append("\t\t * @return the number of elements.\n");
			source.append("\t\t * */\n");
		}
		source.append("\t\tpublic TitanInteger lengthof() {\n");
		source.append( MessageFormat.format( "\t\t\tmust_bound(\"Performing lengthof operation on an unbound value of type {0}.\");\n", displayName ) );
		source.append("\t\t\tfor ( int i = valueElements.size() - 1; i >= 0; i-- ) {\n");
		source.append( MessageFormat.format( "\t\t\t\tfinal {0} elem = valueElements.get( i );\n", ofTypeName ) );
		source.append("\t\t\t\tif ( elem != null && elem.is_bound() ) {\n");
		source.append("\t\t\t\t\treturn new TitanInteger(i + 1);\n");
		source.append("\t\t\t\t}\n");
		source.append("\t\t\t}\n");
		source.append("\t\t\treturn new TitanInteger(0);\n");
		source.append("\t\t}\n\n");

		if (aData.isDebug()) {
			source.append("\t\t/**\n");
			source.append("\t\t * Sets the new size of the value.\n");
			source.append("\t\t * If the new size is bigger than actual, unbound elements are added to the end.\n");
			source.append("\t\t * If the new size is smaller than actual, excess elements are removed.\n");
			source.append("\t\t *\n");
			source.append("\t\t * @param newSize the new size to be used.\n");
			source.append("\t\t * */\n");
		}
		source.append("\t\tpublic void set_size(final int newSize) {\n");
		source.append("\t\t\tif (newSize < 0) {\n");
		source.append( MessageFormat.format( "\t\t\t\tthrow new TtcnError(\"Internal error: Setting a negative size for a value of type {0}.\");\n", displayName ) );
		source.append("\t\t\t}\n");
		source.append("\t\t\tif ( valueElements == null ) {\n");
		source.append( MessageFormat.format( "\t\t\t\tvalueElements = new ArrayList<{0}>(newSize);\n", ofTypeName ) );
		source.append("\t\t\t}\n");
		source.append("\t\t\tfinal int elements_size = valueElements.size();\n");
		source.append("\t\t\tif (newSize > elements_size) {\n");
		source.append("\t\t\t\tfor ( int i = elements_size; i < newSize; i++ ) {\n");
		source.append( MessageFormat.format( "\t\t\t\t\tvalueElements.add( new {0}() );\n", ofTypeName ) );
		source.append("\t\t\t\t}\n");
		source.append("\t\t\t} else if (newSize < elements_size) {\n");
		source.append("\t\t\t\twhile(valueElements.size() > newSize) {\n");
		source.append("\t\t\t\t\tvalueElements.remove(valueElements.size()-1);\n");
		source.append("\t\t\t\t}\n");
		source.append("\t\t\t}\n");
		source.append("\t\t}\n");
	}

	private static void generateValueGetUnboundElem(final StringBuilder source, final String ofTypeName) {
		source.append('\n');
		source.append( MessageFormat.format( "\t\tprivate static {0} get_unbound_elem() '{'\n", ofTypeName ) );
		source.append( MessageFormat.format( "\t\t\treturn new {0}();\n", ofTypeName ) );
		source.append("\t\t}\n");
	}

	private static void generateValueToString(final StringBuilder source) {
		source.append('\n');
		source.append("\t\t/**\n");
		source.append("\t\t * Do not use this function!<br>\n");
		source.append("\t\t * It is provided by Java and currently used for debugging.\n");
		source.append("\t\t * But it is not part of the intentionally provided interface,\n");
		source.append("\t\t *   and so can be changed without notice.\n");
		source.append("\t\t * <p>\n");
		source.append("\t\t * JAVA DESCRIPTION:\n");
		source.append("\t\t * <p>\n");
		source.append("\t\t * {@inheritDoc}\n");
		source.append("\t\t *  */\n");
		source.append("\t\t@Override\n");
		source.append("\t\tpublic String toString() {\n");
		source.append("\t\t\tif (!is_bound()) {\n");
		source.append("\t\t\t\treturn \"<unbound>\";\n");
		source.append("\t\t\t}\n");
		source.append("\t\t\tfinal StringBuilder sb = new StringBuilder();\n");
		source.append("\t\t\tsb.append('{');\n");
		source.append("\t\t\tfinal int size = ( valueElements == null ) ? 0 : valueElements.size();\n");
		source.append("\t\t\tfor (int i = 0; i < size; i++ ) {\n");
		source.append("\t\t\t\tif ( i > 0 ) {\n");
		source.append("\t\t\t\t\tsb.append(',');\n");
		source.append("\t\t\t\t}\n");
		source.append("\t\t\t\tsb.append(valueElements.get(i).toString());\n");
		source.append("\t\t\t}\n");
		source.append("\t\t\tsb.append('}');\n");
		source.append("\t\t\treturn sb.toString();\n");
		source.append("\t\t}\n\n");
	}

	/**
	 * Generate substr() and replace()
	 *
	 * @param aData
	 *                used to access build settings.
	 * @param source
	 *                where the source code is to be generated.
	 * @param genName
	 *                the name of the generated class representing the
	 *                "record of/set of" type.
	 * @param ofTypeName
	 *                type name of the "record of/set of" element
	 * @param displayName
	 *                the user readable name of the type to be generated.
	 */
	private static void generateValueReplace( final JavaGenData aData, final StringBuilder source, final String genName, final String ofTypeName, final String displayName) {
		if (aData.isDebug()) {
			source.append("\t\t/**\n");
			source.append("\t\t * Creates a new record/set of value from the current value,\n");
			source.append("\t\t * with the elements from the provided index at the provided length.\n");
			source.append("\t\t *\n");
			source.append("\t\t * @param index\n");
			source.append("\t\t *                the index to start at.\n");
			source.append("\t\t * @param returncount\n");
			source.append("\t\t *                the number of elements to copy.\n");
			source.append("\t\t * @return the new value.\n");
			source.append("\t\t * */\n");
		}
		source.append( MessageFormat.format( "\t\tpublic {0} substr(final int index, final int returncount) '{'\n", genName ) );
		source.append( MessageFormat.format( "\t\t\tmust_bound(\"The first argument of substr() is an unbound value of type {0}.\");\n", displayName ) );
		source.append( MessageFormat.format( "\t\t\tAdditionalFunctions.check_substr_arguments(valueElements.size(), index, returncount, \"{0}\",\"element\");\n", displayName ) );
		source.append( MessageFormat.format( "\t\t\tfinal {0} ret_val = new {0}(TitanNull_Type.NULL_VALUE);\n", genName ) );
		source.append("\t\t\tfor (int i=0; i<returncount; i++) {\n");
		source.append("\t\t\t\tif (valueElements.get(i+index) != null) {\n");
		source.append( MessageFormat.format( "\t\t\t\t\tret_val.valueElements.add(new {0}(valueElements.get(i+index)));\n", ofTypeName ) );
		source.append("\t\t\t\t}\n");
		source.append("\t\t\t}\n");
		source.append("\t\t\treturn ret_val;\n");
		source.append("\t\t}\n\n");

		if (aData.isDebug()) {
			source.append("\t\t/**\n");
			source.append("\t\t * Creates a new record/set of value from the current value,\n");
			source.append("\t\t * with the parts from the provided index at the provided length\n");
			source.append("\t\t * being replaced by the provided values.\n");
			source.append("\t\t *\n");
			source.append("\t\t * @param index\n");
			source.append("\t\t *                the index to start replacing at.\n");
			source.append("\t\t * @param len\n");
			source.append("\t\t *                the number of elements to replace.\n");
			source.append("\t\t * @param repl\n");
			source.append("\t\t *                the values to insert.\n");
			source.append("\t\t * @return the new value.\n");
			source.append("\t\t * */\n");
		}
		source.append( MessageFormat.format( "\t\tpublic {0} replace(final int index, final int len, final {0} repl) '{'\n", genName ) );
		source.append( MessageFormat.format( "\t\t\tmust_bound(\"The first argument of replace() is an unbound value of type {0}.\");\n", displayName ) );
		source.append( MessageFormat.format( "\t\t\trepl.must_bound(\"The fourth argument of replace() is an unbound value of type {0}.\");\n", displayName ) );
		source.append( MessageFormat.format( "\t\t\tAdditionalFunctions.check_replace_arguments(valueElements.size(), index, len, \"{0}\",\"element\");\n", displayName ) );
		source.append( MessageFormat.format( "\t\t\tfinal {0} ret_val = new {0}(TitanNull_Type.NULL_VALUE);\n", genName ) );
		source.append("\t\t\tfor (int i = 0; i < index; i++) {\n");
		source.append( MessageFormat.format( "\t\t\t\tfinal {0} elem = valueElements.get(i);\n", ofTypeName ) );
		source.append("\t\t\t\tif (elem != null) {\n");
		source.append( MessageFormat.format( "\t\t\t\t\tret_val.valueElements.add(new {0}(elem));\n", ofTypeName ) );
		source.append("\t\t\t\t}\n");
		source.append("\t\t\t}\n");
		source.append("\t\t\tfor (int i = 0; i < repl.valueElements.size(); i++) {\n");
		source.append( MessageFormat.format( "\t\t\t\tfinal {0} elem = repl.valueElements.get(i);\n", ofTypeName ) );
		source.append("\t\t\t\tif (elem != null) {\n");
		source.append( MessageFormat.format( "\t\t\t\t\tret_val.valueElements.add(new {0}(elem));\n", ofTypeName ) );
		source.append("\t\t\t\t}\n");
		source.append("\t\t\t}\n");
		source.append("\t\t\tfinal int elements_size = valueElements.size();\n");
		source.append("\t\t\tfor (int i = 0; i < elements_size - index - len; i++) {\n");
		source.append( MessageFormat.format( "\t\t\t\tfinal {0} elem = valueElements.get(index + i + len);\n", ofTypeName ) );
		source.append("\t\t\t\tif (elem != null) {\n");
		source.append( MessageFormat.format( "\t\t\t\t\tret_val.valueElements.add(new {0}(elem));\n", ofTypeName ) );
		source.append("\t\t\t\t}\n");
		source.append("\t\t\t}\n");
		source.append("\t\t\treturn ret_val;\n");
		source.append("\t\t}\n\n");

		//int index,int len:
		if (aData.isDebug()) {
			source.append("\t\t/**\n");
			source.append("\t\t * Creates a new record/set of value from the current value,\n");
			source.append("\t\t * with the parts from the provided index at the provided length\n");
			source.append("\t\t * being replaced by the provided values.\n");
			source.append("\t\t *\n");
			source.append("\t\t * @param index\n");
			source.append("\t\t *                the index to start replacing at.\n");
			source.append("\t\t * @param len\n");
			source.append("\t\t *                the number of elements to replace.\n");
			source.append("\t\t * @param repl\n");
			source.append("\t\t *                the values to insert.\n");
			source.append("\t\t * @return the new value.\n");
			source.append("\t\t * */\n");
		}
		source.append( MessageFormat.format( "\t\tpublic {0} replace(final int index, final int len, final {0}_template repl) '{'\n", genName ) );
		source.append("\t\t\tif (!repl.is_value()) {\n");
		source.append("\t\t\t\tthrow new TtcnError(\"The fourth argument of function replace() is a template with non-specific value.\");\n");
		source.append("\t\t\t}\n");
		source.append("\t\t\treturn replace(index, len, repl.valueof());\n");
		source.append("\t\t}\n\n");
		//int,TitanInteger
		if (aData.isDebug()) {
			source.append("\t\t/**\n");
			source.append("\t\t * Creates a new record/set of value from the current value,\n");
			source.append("\t\t * with the parts from the provided index at the provided length\n");
			source.append("\t\t * being replaced by the provided values.\n");
			source.append("\t\t *\n");
			source.append("\t\t * @param index\n");
			source.append("\t\t *                the index to start replacing at.\n");
			source.append("\t\t * @param len\n");
			source.append("\t\t *                the number of elements to replace.\n");
			source.append("\t\t * @param repl\n");
			source.append("\t\t *                the values to insert.\n");
			source.append("\t\t * @return the new value.\n");
			source.append("\t\t * */\n");
		}
		source.append( MessageFormat.format( "\t\tpublic {0} replace(final int index, final TitanInteger len, final {0}_template repl) '{'\n", genName ) );
		source.append("\t\t\tif (!repl.is_value()) {\n");
		source.append("\t\t\t\tthrow new TtcnError(\"The fourth argument of function replace() is a template with non-specific value.\");\n");
		source.append("\t\t\t}\n");
		source.append("\t\t\treturn replace(index, len.get_int(), repl.valueof());\n");
		source.append("\t\t}\n\n");
		//TitanInteger,int
		if (aData.isDebug()) {
			source.append("\t\t/**\n");
			source.append("\t\t * Creates a new record/set of value from the current value,\n");
			source.append("\t\t * with the parts from the provided index at the provided length\n");
			source.append("\t\t * being replaced by the provided values.\n");
			source.append("\t\t *\n");
			source.append("\t\t * @param index\n");
			source.append("\t\t *                the index to start replacing at.\n");
			source.append("\t\t * @param len\n");
			source.append("\t\t *                the number of elements to replace.\n");
			source.append("\t\t * @param repl\n");
			source.append("\t\t *                the values to insert.\n");
			source.append("\t\t * @return the new value.\n");
			source.append("\t\t * */\n");
		}
		source.append( MessageFormat.format( "\t\tpublic {0} replace(final TitanInteger index, final int len, final {0}_template repl) '{'\n", genName ) );
		source.append("\t\t\tif (!repl.is_value()) {\n");
		source.append("\t\t\t\tthrow new TtcnError(\"The fourth argument of function replace() is a template with non-specific value.\");\n");
		source.append("\t\t\t}\n");
		source.append("\t\t\treturn replace(index.get_int(), len, repl.valueof());\n");
		source.append("\t\t}\n\n");
		//TitanInteger,TitanInteger
		if (aData.isDebug()) {
			source.append("\t\t/**\n");
			source.append("\t\t * Creates a new record/set of value from the current value,\n");
			source.append("\t\t * with the parts from the provided index at the provided length\n");
			source.append("\t\t * being replaced by the provided values.\n");
			source.append("\t\t *\n");
			source.append("\t\t * @param index\n");
			source.append("\t\t *                the index to start replacing at.\n");
			source.append("\t\t * @param len\n");
			source.append("\t\t *                the number of elements to replace.\n");
			source.append("\t\t * @param repl\n");
			source.append("\t\t *                the values to insert.\n");
			source.append("\t\t * @return the new value.\n");
			source.append("\t\t * */\n");
		}
		source.append( MessageFormat.format( "\t\tpublic {0} replace(final TitanInteger index, final TitanInteger len, final {0}_template repl) '{'\n", genName ) );
		source.append("\t\t\tif (!repl.is_value()) {\n");
		source.append("\t\t\t\tthrow new TtcnError(\"The fourth argument of function replace() is a template with non-specific value.\");\n");
		source.append("\t\t\t}\n");
		source.append("\t\t\treturn replace(index.get_int(), len.get_int(), repl.valueof());\n");
		source.append("\t\t}\n");
		//===
	}

	/**
	 * Generate log()
	 *
	 * @param source
	 *                where the source code is to be generated.
	 */
	private static void generateValueLog(final StringBuilder source) {
		source.append("\t\t@Override\n");
		source.append("\t\tpublic void log() {\n");
		source.append("\t\t\tif (valueElements == null) {\n");
		source.append("\t\t\t\tTTCN_Logger.log_event_unbound();\n");
		source.append("\t\t\t\treturn;\n");
		source.append("\t\t\t}\n");
		source.append("\t\t\tTTCN_Logger.log_event_str(\"{ \");\n");
		source.append("\t\t\tfinal int size = valueElements.size();\n");
		source.append("\t\t\tfor (int i = 0; i < size; i++ ) {\n");
		source.append("\t\t\t\tif ( i > 0 ) {\n");
		source.append("\t\t\t\t\tTTCN_Logger.log_event_str(\", \");\n");
		source.append("\t\t\t\t}\n");
		//Temporal fix for empty record in a record
		source.append("\t\t\t\tif (valueElements.get(i) != null) {\n");
		source.append("\t\t\t\t\tvalueElements.get(i).log();\n");
		source.append("\t\t\t\t}\n");
		source.append("\t\t\t}\n");
		source.append("\t\t\tTTCN_Logger.log_event_str(\" }\");\n");
		source.append("\t\t}\n\n");
	}

	/**
	 * Generate set_param
	 *
	 * @param source
	 *                where the source code is to be generated.
	 * @param displayName
	 *                the user readable name of the type to be generated.
	 * @param isSetOf
	 *                {@code true}: set of, {@code false}: record of
	 */
	private static void generateValueSetParam(final StringBuilder source, final String displayName, final boolean isSetOf) {
		source.append("\t\t@Override\n");
		source.append("\t\tpublic void set_param(final Module_Parameter param) {\n");
		source.append(MessageFormat.format("\t\t\tparam.basic_check(Module_Parameter.basic_check_bits_t.BC_VALUE.getValue() | Module_Parameter.basic_check_bits_t.BC_LIST.getValue(), \"{0} of value\");\n", isSetOf ? "set" : "record"));
		source.append("\t\t\tswitch (param.get_operation_type()) {\n");
		source.append("\t\t\tcase OT_ASSIGN:\n");
		source.append("\t\t\t\tif (param.get_type() == Module_Parameter.type_t.MP_Value_List && param.get_size() == 0) {\n");
		source.append("\t\t\t\t\toperator_assign(TitanNull_Type.NULL_VALUE);\n");
		source.append("\t\t\t\t\treturn;\n");
		source.append("\t\t\t\t}\n");
		source.append("\t\t\t\tswitch (param.get_type()) {\n");
		source.append("\t\t\t\tcase MP_Value_List:\n");
		source.append("\t\t\t\t\tset_size(param.get_size());\n");
		source.append("\t\t\t\t\tfor (int i = 0; i < param.get_size(); i++) {\n");
		source.append("\t\t\t\t\t\tfinal Module_Parameter current = param.get_elem(i);\n");
		source.append("\t\t\t\t\t\tif (current.get_type() != Module_Parameter.type_t.MP_NotUsed) {\n");
		source.append("\t\t\t\t\t\t\tget_at(i).set_param(current);\n");
		source.append("\t\t\t\t\t\t\tif (!constGet_at(i).is_bound()) {\n");
		source.append("\t\t\t\t\t\t\t\tvalueElements.set(i, null);\n");
		source.append("\t\t\t\t\t\t\t}\n");
		source.append("\t\t\t\t\t\t}\n");
		source.append("\t\t\t\t\t}\n");
		source.append("\t\t\t\t\tbreak;\n");
		source.append("\t\t\t\tcase MP_Indexed_List:\n");
		source.append("\t\t\t\t\tfor (int i = 0; i < param.get_size(); i++) {\n");
		source.append("\t\t\t\t\t\tfinal Module_Parameter current = param.get_elem(i);\n");
		source.append("\t\t\t\t\t\tget_at(current.get_id().get_index()).set_param(current);\n");
		source.append("\t\t\t\t\t\tif (!constGet_at(current.get_id().get_index()).is_bound()) {\n");
		source.append("\t\t\t\t\t\t\tvalueElements.set(i, null);\n");
		source.append("\t\t\t\t\t\t}\n");
		source.append("\t\t\t\t\t}\n");
		source.append("\t\t\t\t\tbreak;\n");
		source.append("\t\t\t\tdefault:\n");
		source.append( MessageFormat.format( "\t\t\t\t\tparam.type_error(\"{0} of value\", \"{1}\");\n", isSetOf ? "set" : "record", displayName));
		source.append("\t\t\t\t\tbreak;\n");
		source.append("\t\t\t\t}\n");
		source.append("\t\t\t\tbreak;\n");
		source.append("\t\t\tcase OT_CONCAT:\n");
		source.append("\t\t\t\tswitch (param.get_type()) {\n");
		source.append("\t\t\t\tcase MP_Value_List: {\n");
		source.append("\t\t\t\t\tif (!is_bound()) {\n");
		source.append("\t\t\t\t\t\toperator_assign(TitanNull_Type.NULL_VALUE);\n");
		source.append("\t\t\t\t\t}\n");
		source.append("\t\t\t\t\tfinal int start_idx = lengthof().get_int();\n");
		source.append("\t\t\t\t\tfor (int i = 0; i < param.get_size(); i++) {\n");
		source.append("\t\t\t\t\t\tfinal Module_Parameter current = param.get_elem(i);\n");
		source.append("\t\t\t\t\t\tif (current.get_type() != Module_Parameter.type_t.MP_NotUsed) {\n");
		source.append("\t\t\t\t\t\t\tget_at(start_idx + i).set_param(current);\n");
		source.append("\t\t\t\t\t\t}\n");
		source.append("\t\t\t\t\t}\n");
		source.append("\t\t\t\t\tbreak;\n");
		source.append("\t\t\t\t}\n");
		source.append("\t\t\t\tcase MP_Indexed_List:\n");
		source.append("\t\t\t\t\tparam.error(\"Cannot concatenate an indexed value list\");\n");
		source.append("\t\t\t\t\tbreak;\n");
		source.append("\t\t\t\tdefault:\n");
		source.append( MessageFormat.format( "\t\t\t\t\tparam.type_error(\"{0} of value\", \"{1}\");\n", isSetOf ? "set" : "record", displayName));
		source.append("\t\t\t\t\tbreak;\n");
		source.append("\t\t\t\t}\n");
		source.append("\t\t\t\tbreak;\n");
		source.append("\t\t\tdefault:\n");
		source.append("\t\t\t\tthrow new TtcnError(\"Internal error: Unknown operation type\");\n");
		source.append("\t\t\t}\n");
		source.append("\t\t}\n\n");
	}

	/**
	 * Generate set_implicit_omit.
	 *
	 * @param source
	 *                where the source code is to be generated.
	 * @param ofTypeName
	 *                type name of the "record of/set of" element
	 */
	private static void generateValueSetImplicitOmit(final StringBuilder source, final String ofTypeName) {
		source.append("\t\t@Override\n");
		source.append("\t\tpublic void set_implicit_omit() {\n");
		source.append("\t\t\tif(valueElements == null) {\n");
		source.append("\t\t\t\treturn;\n");
		source.append("\t\t\t}\n");

		source.append("\t\t\tfinal int elements_size = valueElements.size();\n");
		source.append("\t\t\tfor (int i = 0; i < elements_size; i++) {\n");
		source.append( MessageFormat.format( "\t\t\t\tfinal {0} temp = valueElements.get(i);\n", ofTypeName));
		source.append("\t\t\t\tif (temp != null && temp.is_bound()) {\n");
		source.append("\t\t\t\t\ttemp.set_implicit_omit();\n");
		source.append("\t\t\t\t}\n");
		source.append("\t\t\t}\n");
		source.append("\t\t}\n\n");
	}

	/**
	 * Generate encode_text/decode_text
	 *
	 * @param source
	 *                where the source code is to be generated.
	 * @param ofTypeName
	 *                type name of the "record of/set of" element
	 * @param displayName
	 *                the user readable name of the type to be generated.
	 */
	private static void generateValueEncodeDecodeText(final StringBuilder source, final String ofTypeName, final String displayName) {
		source.append("\t\t@Override\n");
		source.append("\t\tpublic void encode_text(final Text_Buf text_buf) {\n");
		source.append( MessageFormat.format( "\t\t\tmust_bound(\"Text encoder: Encoding an unbound value of type {0}.\");\n", displayName));
		source.append("\t\t\tfinal int elements_size = valueElements.size();\n");
		source.append("\t\t\ttext_buf.push_int(elements_size);\n");
		source.append("\t\t\tfor (int i = 0; i < elements_size; i++) {\n");
		source.append("\t\t\t\tvalueElements.get(i).encode_text(text_buf);\n");
		source.append("\t\t\t}\n");
		source.append("\t\t}\n\n");

		source.append("\t\t@Override\n");
		source.append("\t\tpublic void decode_text(final Text_Buf text_buf) {\n");
		source.append("\t\t\tclean_up();\n");
		source.append("\t\t\tfinal int temp = text_buf.pull_int().get_int();\n");
		source.append( MessageFormat.format( "\t\t\tvalueElements = new ArrayList<{0}>(temp);\n", ofTypeName));
		source.append("\t\t\tfor (int i = 0; i < temp; i++) {\n");
		source.append( MessageFormat.format( "\t\t\t\tfinal {0} temp2 = new {0}();\n", ofTypeName));
		source.append("\t\t\t\ttemp2.decode_text(text_buf);\n");
		source.append("\t\t\t\tvalueElements.add(temp2);\n");
		source.append("\t\t\t}\n");
		source.append("\t\t}\n\n");
	}

	/**
	 * Generate encode/decode
	 *
	 * @param source
	 *                where the source code is to be generated.
	 * @param ofTypeName
	 *                type name of the "record of/set of" element
	 * @param displayName
	 *                the user readable name of the type to be generated.
	 * @param rawNeeded
	 *                true if encoding/decoding for RAW is to be generated
	 * @param forceGenSeof
	 *                {@code true}: if code generation is forced.
	 * @param extension_bit
	 *                the raw extension bit to be used if RAW coding is to
	 *                be generated.
	 * @param jsonNeeded
	 *                true if encoding/decoding for JSON is to be generated
	 */
	private static void generateValueEncodeDecode(final StringBuilder source, final String ofTypeName, final String displayName,
			final boolean rawNeeded, final boolean forceGenSeof, final int extension_bit,
			final boolean jsonNeeded) {
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
		source.append("\t\t\t\t\tfinal RAW_enc_tree root = new RAW_enc_tree(false, null, tree_position, 1, p_td.raw);\n");
		source.append("\t\t\t\t\tRAW_encode(p_td, root);\n");
		source.append("\t\t\t\t\troot.put_to_buf(p_buf);\n");
		source.append("\t\t\t\t} finally {\n");
		source.append("\t\t\t\t\terrorContext.leave_context();\n");
		source.append("\t\t\t\t}\n");
		source.append("\t\t\t\tbreak;\n");
		source.append("\t\t\t}\n");

		source.append("\t\t\tcase CT_JSON: {\n");
		source.append("\t\t\t\tif(p_td.json == null) {\n");
		source.append("\t\t\t\t\tTTCN_EncDec_ErrorContext.error_internal(\"No JSON descriptor available for type '%s'.\", p_td.name);\n");
		source.append("\t\t\t\t}\n");
		source.append("\t\t\t\tfinal JSON_Tokenizer tok = new JSON_Tokenizer(flavour != 0);\n");
		source.append("\t\t\t\tJSON_encode(p_td, tok);\n");
		source.append("\t\t\t\tp_buf.put_s(tok.get_buffer().toString().getBytes());\n");
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
		source.append("\t\t\t\tif(p_td.json == null) {\n");
		source.append("\t\t\t\t\tTTCN_EncDec_ErrorContext.error_internal(\"No JSON descriptor available for type '%s'.\", p_td.name);\n");
		source.append("\t\t\t\t}\n");
		source.append("\t\t\t\tfinal JSON_Tokenizer tok = new JSON_Tokenizer(new String(p_buf.get_data()), p_buf.get_len());\n");
		source.append("\t\t\t\tif(JSON_decode(p_td, tok, false) < 0) {\n");
		source.append("\t\t\t\t\tTTCN_EncDec_ErrorContext.error(error_type.ET_INCOMPL_MSG, \"Can not decode type '%s', because invalid or incomplete message was received\", p_td.name);\n");
		source.append("\t\t\t\t}\n");
		source.append("\t\t\t\tp_buf.set_pos(tok.get_buf_pos());\n");
		source.append("\t\t\t\tbreak;\n");
		source.append("\t\t\t}\n");

		source.append("\t\t\tdefault:\n");
		source.append("\t\t\t\tthrow new TtcnError(MessageFormat.format(\"Unknown coding method requested to decode type `{0}''\", p_td.name));\n");
		source.append("\t\t\t}\n");
		source.append("\t\t}\n\n");

		if (rawNeeded) {
			source.append("\t\t@Override\n");
			source.append("\t\t/** {@inheritDoc} */\n");
			source.append("\t\tpublic int RAW_encode(final TTCN_Typedescriptor p_td, final RAW_enc_tree myleaf) {\n");
			source.append("\t\t\tint encoded_length = 0;\n");
			source.append("\t\t\tfinal int encoded_num_of_records = p_td.raw.fieldlength == 0 ? valueElements.size() : Math.min(valueElements.size(), p_td.raw.fieldlength);\n");
			source.append("\t\t\tmyleaf.isleaf = false;\n");
			source.append("\t\t\tmyleaf.rec_of = true;\n");
			source.append("\t\t\tmyleaf.num_of_nodes = encoded_num_of_records;\n");
			source.append("\t\t\tmyleaf.nodes = new RAW_enc_tree[encoded_num_of_records];\n"); //init_nodes_of_enc_tree
			source.append("\t\t\tfor (int a = 0; a < encoded_num_of_records; a++) {\n");
			source.append("\t\t\t\tmyleaf.nodes[a] = new RAW_enc_tree(true, myleaf, myleaf.curr_pos, a, p_td.oftype_descr.raw);\n");
			source.append("\t\t\t\tencoded_length += valueElements.get(a).RAW_encode(p_td.oftype_descr, myleaf.nodes[a]);\n");
			source.append("\t\t\t}\n");
			source.append("\t\t\treturn myleaf.length = encoded_length;\n");
			source.append("\t\t}\n\n");

			source.append("\t\t@Override\n");
			source.append("\t\t/** {@inheritDoc} */\n");
			source.append("\t\tpublic int RAW_decode(final TTCN_Typedescriptor p_td, final TTCN_Buffer buff, final int limit, final raw_order_t top_bit_ord) {\n");
			source.append("\t\t\treturn RAW_decode(p_td, buff, limit, top_bit_ord, false, -1, true, null);\n");
			source.append("\t\t}\n\n");

			source.append("\t\t@Override\n");
			source.append("\t\t/** {@inheritDoc} */\n");
			source.append("\t\tpublic int RAW_decode(final TTCN_Typedescriptor p_td, final TTCN_Buffer buff, int limit, final raw_order_t top_bit_ord, final boolean no_err, int sel_field, final boolean first_call, final RAW_Force_Omit force_omit) {\n");
			source.append("\t\t\tfinal int prepaddlength = buff.increase_pos_padd(p_td.raw.prepadding);\n");
			source.append("\t\t\tlimit -= prepaddlength;\n");
			source.append("\t\t\tint decoded_length = 0;\n");
			source.append("\t\t\tint decoded_field_length = 0;\n");
			source.append("\t\t\tint start_of_field = 0;\n");
			source.append("\t\t\tif (first_call) {\n");
			source.append("\t\t\t\tclean_up();\n");
			source.append(MessageFormat.format("\t\t\t\tvalueElements = new ArrayList<{0}>();\n", ofTypeName));
			source.append("\t\t\t}\n");
			source.append("\t\t\tfinal int start_field = valueElements.size();\n");
			source.append("\t\t\tif (p_td.raw.fieldlength > 0 || sel_field != -1) {\n");
			source.append("\t\t\t\tint a = 0;\n");
			source.append("\t\t\t\tif (sel_field == -1) {\n");
			source.append("\t\t\t\t\tsel_field = p_td.raw.fieldlength;\n");
			source.append("\t\t\t\t}\n");
			source.append("\t\t\t\tfor (a = 0; a < sel_field; a++) {\n");
			source.append("\t\t\t\t\tdecoded_field_length = get_at(a + start_field).RAW_decode(p_td.oftype_descr, buff, limit, top_bit_ord, true, -1, true, null);\n");
			source.append("\t\t\t\t\tif (decoded_field_length < 0) {\n");
			source.append("\t\t\t\t\t\treturn decoded_field_length;\n");
			source.append("\t\t\t\t\t}\n");
			source.append("\t\t\t\t\tdecoded_length += decoded_field_length;\n");
			source.append("\t\t\t\t\tlimit -= decoded_field_length;\n");
			source.append("\t\t\t\t}\n");
			source.append("\t\t\t\tif (a == 0) {\n");
			source.append("\t\t\t\t\tvalueElements.clear();\n");
			source.append("\t\t\t\t}\n");
			source.append("\t\t\t} else {\n");
			source.append("\t\t\t\tif (limit == 0) {\n");
			source.append("\t\t\t\t\tif (!first_call) {\n");
			source.append("\t\t\t\t\t\treturn -1;\n");
			source.append("\t\t\t\t\t}\n");
			source.append("\t\t\t\t\tvalueElements.clear();\n");
			source.append("\t\t\t\t\treturn decoded_length + buff.increase_pos_padd(p_td.raw.padding) + prepaddlength;\n");
			source.append("\t\t\t\t}\n");
			source.append("\t\t\t\tint a = start_field;\n");
			source.append("\t\t\t\twhile (limit > 0) {\n");
			source.append("\t\t\t\t\tstart_of_field = buff.get_pos_bit();\n");
			source.append("\t\t\t\t\tdecoded_field_length = get_at(a).RAW_decode(p_td.oftype_descr, buff, limit, top_bit_ord, true, -1, true, null);\n");
			source.append("\t\t\t\t\tif (decoded_field_length < 0) {\n");
			source.append("\t\t\t\t\t\tvalueElements.remove(a);\n");
			source.append("\t\t\t\t\t\tbuff.set_pos_bit(start_of_field);\n");
			source.append("\t\t\t\t\t\tif (a > start_field) {\n");
			source.append("\t\t\t\t\t\t\treturn decoded_length + buff.increase_pos_padd(p_td.raw.padding) + prepaddlength;\n");
			source.append("\t\t\t\t\t\t} else {\n");
			source.append("\t\t\t\t\t\t\treturn -1;\n");
			source.append("\t\t\t\t\t\t}\n");
			source.append("\t\t\t\t\t}\n");
			source.append("\t\t\t\t\tdecoded_length += decoded_field_length;\n");
			source.append("\t\t\t\t\tlimit -= decoded_field_length;\n");
			source.append("\t\t\t\t\ta++;\n");
			if (forceGenSeof) {
				source.append("\t\t\t\t\tif (ext_bit_t.EXT_BIT_NO != p_td.raw.extension_bit && ((ext_bit_t.EXT_BIT_YES != p_td.raw.extension_bit) ^ buff.get_last_bit())) {\n");
				source.append("\t\t\t\t\t\treturn decoded_length + buff.increase_pos_padd(p_td.raw.padding) + prepaddlength;\n");
				source.append("\t\t\t\t\t}\n");
			} else if (extension_bit != RawASTStruct.XDEFNO && extension_bit != RawASTStruct.XDEFDEFAULT){
				source.append(MessageFormat.format("\t\t\t\t\tif ( {0}buff.get_last_bit()) '{'\n", extension_bit == RawASTStruct.XDEFYES ? "" : "!"));
				source.append("\t\t\t\t\t\treturn decoded_length + buff.increase_pos_padd(p_td.raw.padding) + prepaddlength;\n");
				source.append("\t\t\t\t\t}\n");
			}
			source.append("\t\t\t\t}\n");
			source.append("\t\t\t}\n");
			source.append("\t\t\treturn decoded_length + buff.increase_pos_padd(p_td.raw.padding) + prepaddlength;\n");
			source.append("\t\t}\n\n");
		}
		if (jsonNeeded) {
			// JSON encode, RT1
			source.append("\t\t@Override\n");
			source.append("\t\t/** {@inheritDoc} */\n");
			source.append("\t\tpublic int JSON_encode(final TTCN_Typedescriptor p_td, final JSON_Tokenizer p_tok, final boolean p_parent_is_map) {\n");
			source.append("\t\t\tif (!is_bound()) {\n");
			source.append("\t\t\t\tTTCN_EncDec_ErrorContext.error(error_type.ET_UNBOUND,\n");
			source.append(MessageFormat.format("\t\t\t\t\t\"Encoding an unbound value of type {0}.\");\n", displayName));
			source.append("\t\t\t\treturn -1;\n");
			source.append("\t\t\t}\n\n");
			source.append("\t\t\tint enc_len = p_tok.put_next_token(p_td.json.isAs_map() ? json_token_t.JSON_TOKEN_OBJECT_START : json_token_t.JSON_TOKEN_ARRAY_START, null);\n");
			source.append("\t\t\tfor (int i = 0; i < valueElements.size(); ++i) {\n");
			source.append("\t\t\t\tif (p_td.json.isMetainfo_unbound() && !(get_at(i).is_bound())) {\n");
			// unbound elements are encoded as { "metainfo []" : "unbound" }
			source.append("\t\t\t\t\tenc_len += p_tok.put_next_token(json_token_t.JSON_TOKEN_OBJECT_START, null);\n");
			source.append("\t\t\t\t\tenc_len += p_tok.put_next_token(json_token_t.JSON_TOKEN_NAME, \"metainfo []\");\n");
			source.append("\t\t\t\t\tenc_len += p_tok.put_next_token(json_token_t.JSON_TOKEN_STRING, \"\\\"unbound\\\"\");\n");
			source.append("\t\t\t\t\tenc_len += p_tok.put_next_token(json_token_t.JSON_TOKEN_OBJECT_END, null);\n");
			source.append("\t\t\t\t}\n");
			source.append("\t\t\t\telse {\n");
			source.append("\t\t\t\t\tfinal int ret_val = get_at(i).JSON_encode(p_td.oftype_descr, p_tok, p_td.json.isAs_map());\n");
			source.append("\t\t\t\t\tif (0 > ret_val) break;\n");
			source.append("\t\t\t\t\tenc_len += ret_val;\n");
			source.append("\t\t\t\t}\n");
			source.append("\t\t\t}\n");
			source.append("\t\t\tenc_len += p_tok.put_next_token(p_td.json.isAs_map() ? json_token_t.JSON_TOKEN_OBJECT_END : json_token_t.JSON_TOKEN_ARRAY_END, null);\n");
			source.append("\t\t\treturn enc_len;\n");
			source.append("\t\t}\n\n");

			// JSON decode, RT1
			source.append("\t\t@Override\n");
			source.append("\t\t/** {@inheritDoc} */\n");
			source.append("\t\tpublic int JSON_decode(final TTCN_Typedescriptor p_td, final JSON_Tokenizer p_tok, final boolean p_silent, final boolean p_parent_is_map, final int p_chosen_field) {\n");
			source.append("\t\t\tif (null != p_td.json.getDefault_value() && 0 == p_tok.get_buffer_length()) {\n");
			// use the default value (currently only the empty array can be set as
			// default value for this type)
			source.append("\t\t\t\tset_size(0);\n");
			source.append("\t\t\t\treturn p_td.json.getDefault_value().length();\n");
			source.append("\t\t\t}\n");
			source.append("\t\t\tfinal AtomicReference<json_token_t> token = new AtomicReference<json_token_t>(json_token_t.JSON_TOKEN_NONE);\n");
			source.append("\t\t\tint dec_len = p_tok.get_next_token(token, null, null);\n");
			source.append("\t\t\tif (json_token_t.JSON_TOKEN_ERROR == token.get()) {\n");
			source.append("\t\t\t\tif (!p_silent) {\n");
			source.append("\t\t\t\t\tTTCN_EncDec_ErrorContext.error(error_type.ET_INVAL_MSG, JSON.JSON_DEC_BAD_TOKEN_ERROR, \"\");\n");
			source.append("\t\t\t\t}\n");
			source.append("\t\t\t\treturn JSON.JSON_ERROR_FATAL;\n");
			source.append("\t\t\t}\n");
			source.append("\t\t\telse if ((!p_td.json.isAs_map() && json_token_t.JSON_TOKEN_ARRAY_START != token.get()) ||\n");
			source.append("\t\t\t\t\t(p_td.json.isAs_map() && json_token_t.JSON_TOKEN_OBJECT_START != token.get())) {\n");
			source.append("\t\t\t\treturn JSON.JSON_ERROR_INVALID_TOKEN;\n");
			source.append("\t\t\t}\n\n");
			source.append("\t\t\tset_size(0);\n");
			source.append("\t\t\twhile (true) {\n");
			source.append("\t\t\t\tfinal int buf_pos = p_tok.get_buf_pos();\n");
			source.append("\t\t\t\tint ret_val;\n");
			source.append("\t\t\t\tif (p_td.json.isMetainfo_unbound()) {\n");
			// check for metainfo object
			source.append("\t\t\t\t\tret_val = p_tok.get_next_token(token, null, null);\n");
			source.append("\t\t\t\t\tif (json_token_t.JSON_TOKEN_OBJECT_START == token.get()) {\n");
			source.append("\t\t\t\t\t\tfinal StringBuilder value = new StringBuilder();\n");
			source.append("\t\t\t\t\t\tfinal AtomicInteger value_len = new AtomicInteger(0);\n");
			source.append("\t\t\t\t\t\tret_val += p_tok.get_next_token(token, value, value_len);\n");
			source.append("\t\t\t\t\t\tif (json_token_t.JSON_TOKEN_NAME == token.get() && 11 == value_len.get() && \"metainfo []\".equals(value.toString())) {\n");
			source.append("\t\t\t\t\t\t\tret_val += p_tok.get_next_token(token, value, value_len);\n");
			source.append("\t\t\t\t\t\t\tif (json_token_t.JSON_TOKEN_STRING == token.get() && 9 == value_len.get() && \"\\\"unbound\\\"\".equals(value.toString())) {\n");
			source.append("\t\t\t\t\t\t\t\tret_val = p_tok.get_next_token(token, null, null);\n");
			source.append("\t\t\t\t\t\t\t\tif (json_token_t.JSON_TOKEN_OBJECT_END == token.get()) {\n");
			source.append("\t\t\t\t\t\t\t\t\tdec_len += ret_val;\n");
			source.append("\t\t\t\t\t\t\t\t\tcontinue;\n");
			source.append("\t\t\t\t\t\t\t\t}\n");
			source.append("\t\t\t\t\t\t\t}\n");
			source.append("\t\t\t\t\t\t}\n");
			source.append("\t\t\t\t\t}\n");
			// metainfo object not found, jump back and let the element type decode it
			source.append("\t\t\t\t\tp_tok.set_buf_pos(buf_pos);\n");
			source.append("\t\t\t\t}\n");
			source.append(MessageFormat.format("\t\t\t\tfinal {0} val = new {0}();\n", ofTypeName));
			source.append("\t\t\t\tfinal int ret_val2 = val.JSON_decode(p_td.oftype_descr, p_tok, p_silent, p_td.json.isAs_map(), JSON.CHOSEN_FIELD_UNSET);\n");
			source.append("\t\t\t\tif (JSON.JSON_ERROR_INVALID_TOKEN == ret_val2) {\n");
			source.append("\t\t\t\t\tp_tok.set_buf_pos(buf_pos);\n");
			source.append("\t\t\t\t\tbreak;\n");
			source.append("\t\t\t\t}\n");
			source.append("\t\t\t\telse if (JSON.JSON_ERROR_FATAL == ret_val2) {\n");
			source.append("\t\t\t\t\tif (p_silent) {\n");
			source.append("\t\t\t\t\t\tclean_up();\n");
			source.append("\t\t\t\t\t}\n");
			source.append("\t\t\t\t\treturn JSON.JSON_ERROR_FATAL;\n");
			source.append("\t\t\t\t}\n");
			source.append("\t\t\t\tvalueElements.add(val);\n");
			source.append("\t\t\t\tdec_len += ret_val2;\n");
			source.append("\t\t\t}\n\n");
			source.append("\t\t\tdec_len += p_tok.get_next_token(token, null, null);\n");
			source.append("\t\t\tif ((!p_td.json.isAs_map() && json_token_t.JSON_TOKEN_ARRAY_END != token.get()) ||\n");
			source.append("\t\t\t\t\t(p_td.json.isAs_map() && json_token_t.JSON_TOKEN_OBJECT_END != token.get())) {\n");
			source.append("\t\t\t\tif (!p_silent) {\n");
			source.append("\t\t\t\t\tTTCN_EncDec_ErrorContext.error(error_type.ET_INVAL_MSG, JSON.JSON_DEC_REC_OF_END_TOKEN_ERROR, \"\");\n");
			source.append("\t\t\t\t}\n");
			source.append("\t\t\t\tif (p_silent) {\n");
			source.append("\t\t\t\t\tclean_up();\n");
			source.append("\t\t\t\t}\n");
			source.append("\t\t\t\treturn JSON.JSON_ERROR_FATAL;\n");
			source.append("\t\t\t}\n\n");
			source.append("\t\t\treturn dec_len;\n");
			source.append("\t\t}\n\n");
		}
	}

	/**
	 * Generate member variables for template
	 *
	 * @param source
	 *                where the source code is to be generated.
	 * @param genName
	 *                the name of the generated class representing the
	 *                "record of/set of" type.
	 * @param ofTypeName
	 *                type name of the "record of/set of" element
	 */
	private static void generateTemplateDeclaration( final StringBuilder source, final String genName, final String ofTypeName ) {
		source.append('\n');

		source.append("\t\t//originally single_value/value_elements\n");
		source.append( MessageFormat.format( "\t\tprotected List<{0}> value_elements;\n", ofTypeName ) );
		source.append('\n');

		source.append("\t\t//originally value_list/list_value\n");
		source.append( MessageFormat.format( "\t\tprotected List<{0}_template> list_value;\n", genName ) );

		source.append('\n');
		source.append("\t\tprivate final match_function_t match_function_specific = new match_function_t() {\n");
		source.append("\t\t\t@Override\n");
		source.append("\t\t\tpublic boolean match(final Base_Type value_ptr, final int value_index, final Restricted_Length_Template template_ptr, final int template_index, final boolean legacy) {\n");
		source.append( MessageFormat.format( "\t\t\t\treturn match_index(({0})value_ptr, value_index, ({0}_template)template_ptr, template_index, legacy);\n", genName ) );
		source.append("\t\t\t}\n");
		source.append("\t\t};\n");
	}

	/**
	 * Generate member variables for template ONLY for set of
	 *
	 * @param source
	 *                where the source code is to be generated.
	 * @param genName
	 *                the name of the generated class representing the
	 *                "record of/set of" type.
	 * @param ofTypeName
	 *                type name of the "record of/set of" element
	 */
	private static void generateTemplateDeclarationSetOf( final StringBuilder source, final String genName, final String ofTypeName ) {
		source.append('\n');
		source.append("\t\t//ONLY for set of\n");
		source.append("\t\t//originally value_set/set_items\n");
		source.append( MessageFormat.format( "\t\tprotected List<{0}> set_items;\n", ofTypeName ) );

		source.append('\n');
		source.append("\t\tprivate final match_function_t match_function_set = new match_function_t() {\n");
		source.append("\t\t\t@Override\n");
		source.append("\t\t\tpublic boolean match(final Base_Type value_ptr, final int value_index, final Restricted_Length_Template template_ptr, final int template_index, final boolean legacy) {\n");
		source.append( MessageFormat.format( "\t\t\t\treturn match_set(({0})value_ptr, value_index, ({0}_template)template_ptr, template_index, legacy);\n", genName ) );
		source.append("\t\t\t}\n");
		source.append("\t\t};\n");

		source.append('\n');
		source.append("\t\tprivate final log_function_t log_function = new log_function_t() {\n");
		source.append("\t\t\t@Override\n");
		source.append("\t\t\tpublic void log(final Base_Type value_ptr, final Restricted_Length_Template template_ptr, final int index_value, final int index_template, final boolean legacy) {\n");
		source.append("\t\t\t\tif (value_ptr == null) {\n");
		source.append("\t\t\t\t\tif (template_ptr != null) {\n");
		source.append( MessageFormat.format( "\t\t\t\t\t\t(({0}_template)template_ptr).value_elements.get(index_template).log();\n", genName ) );
		source.append("\t\t\t\t\t}\n");
		source.append("\t\t\t\t} else if (template_ptr == null) {\n");
		source.append( MessageFormat.format( "\t\t\t\t\t(({0})value_ptr).constGet_at(index_value).log();\n", genName ) );
		source.append("\t\t\t\t} else {\n");
		source.append( MessageFormat.format( "\t\t\t\t\t(({0}_template)template_ptr).value_elements.get(index_template).log_match((({0})value_ptr).constGet_at(index_value), legacy);\n", genName ) );
		source.append("\t\t\t\t}\n");
		source.append("\t\t\t}\n");
		source.append("\t\t};\n");
	}

	/**
	 * Generate constructors for template
	 *
	 * @param aData
	 *                used to access build settings.
	 * @param source
	 *                where the source code is to be generated.
	 * @param genName
	 *                the name of the generated class representing the
	 *                "record of/set of" type.
	 * @param ofTypeName
	 *                type name of the "record of/set of" element
	 * @param displayName
	 *                the user readable name of the type to be generated.
	 * @param isSetOf
	 *                {@code true}: set of, {@code false}: record of
	 */
	private static void generateTemplateConstructors( final JavaGenData aData, final StringBuilder source, final String genName, final String ofTypeName, final String displayName, final boolean isSetOf) {
		source.append('\n');
		if (aData.isDebug()) {
			source.append("\t\t/**\n");
			source.append("\t\t * Initializes to unbound/uninitialized template.\n");
			source.append("\t\t * */\n");
		}
		source.append( MessageFormat.format( "\t\tpublic {0}_template() '{'\n", genName ) );
		source.append("\t\t\t// do nothing\n");
		source.append("\t\t}\n");

		source.append('\n');
		if (aData.isDebug()) {
			source.append("\t\t/**\n");
			source.append("\t\t * Initializes to a given template kind.\n");
			source.append("\t\t *\n");
			source.append("\t\t * @param otherValue\n");
			source.append("\t\t *                the template kind to initialize to.\n");
			source.append("\t\t * */\n");
		}
		source.append( MessageFormat.format( "\t\tpublic {0}_template(final template_sel otherValue ) '{'\n", genName));
		source.append("\t\t\tsuper( otherValue );\n");
		source.append("\t\t\tcheck_single_selection( otherValue );\n");
		source.append("\t\t}\n");

		source.append('\n');
		if (aData.isDebug()) {
			source.append("\t\t/**\n");
			source.append("\t\t * Initializes to a given value.\n");
			source.append("\t\t * The template becomes a specific template.\n");
			source.append("\t\t * The elements of the provided value are copied.\n");
			source.append("\t\t *\n");
			source.append("\t\t * @param otherValue\n");
			source.append("\t\t *                the value to initialize to.\n");
			source.append("\t\t * */\n");
		}
		source.append( MessageFormat.format( "\t\tpublic {0}_template( final {0} otherValue ) '{'\n", genName ) );
		source.append("\t\t\tcopy_value( otherValue );\n");
		source.append("\t\t}\n");

		source.append('\n');
		if (aData.isDebug()) {
			source.append("\t\t/**\n");
			source.append("\t\t * Initializes to a given template.\n");
			source.append("\t\t * The elements of the provided template are copied.\n");
			source.append("\t\t *\n");
			source.append("\t\t * @param otherValue\n");
			source.append("\t\t *                the value to initialize to.\n");
			source.append("\t\t * */\n");
		}
		source.append( MessageFormat.format( "\t\tpublic {0}_template( final {0}_template otherValue ) '{'\n", genName ) );
		source.append("\t\t\tcopy_template( otherValue );\n");
		source.append("\t\t}\n\n");

		source.append( MessageFormat.format( "\t\tpublic {0}_template( final {1}_Of_Template otherValue ) '{'\n", genName, isSetOf ? "Set" : "Record" ) );
		source.append("\t\t\tcopy_template( otherValue );\n");
		source.append("\t\t}\n\n");

		if (aData.isDebug()) {
			source.append("\t\t/**\n");
			source.append("\t\t * Initializes to a given value.\n");
			source.append("\t\t * The template becomes a specific template with the provided value.\n");
			source.append("\t\t * Causes a dynamic testcase error if the value is neither present nor optional.\n");
			source.append("\t\t *\n");
			source.append("\t\t * @param otherValue\n");
			source.append("\t\t *                the value to initialize to.\n");
			source.append("\t\t * */\n");
		}
		source.append( MessageFormat.format( "\t\tpublic {0}_template( final Optional<{0}> otherValue ) '{'\n", genName ) );
		source.append("\t\t\tswitch (otherValue.get_selection()) {\n");
		source.append("\t\t\tcase OPTIONAL_PRESENT:\n");
		source.append("\t\t\t\tcopy_value(otherValue.constGet());\n");
		source.append("\t\t\t\tbreak;\n");
		source.append("\t\t\tcase OPTIONAL_OMIT:\n");
		source.append("\t\t\t\tset_selection(template_sel.OMIT_VALUE);\n");
		source.append("\t\t\t\tbreak;\n");
		source.append("\t\t\tdefault:\n");
		source.append( MessageFormat.format( "\t\t\t\tthrow new TtcnError(\"Creating a template of type {0} from an unbound optional field.\");\n", displayName ) );
		source.append("\t\t\t}\n");
		source.append("\t\t}\n");

		source.append('\n');
		if (aData.isDebug()) {
			source.append("\t\t/**\n");
			source.append("\t\t * Initializes to an empty specific value template.\n");
			source.append("\t\t *\n");
			source.append("\t\t * @param nullValue\n");
			source.append("\t\t *                the null value.\n");
			source.append("\t\t * */\n");
		}
		source.append( MessageFormat.format( "\t\tpublic {0}_template( final TitanNull_Type nullValue ) '{'\n", genName ) );
		source.append("\t\t\tsuper( template_sel.SPECIFIC_VALUE );\n");
		source.append( MessageFormat.format( "\t\t\tvalue_elements = new ArrayList<{0}>();\n", ofTypeName ) );
		source.append("\t\t}\n\n");
	}

	/**
	 * Generate the copyTemplate function for template
	 *
	 * @param aData
	 *                used to access build settings.
	 * @param source
	 *                where the source code is to be generated.
	 * @param genName
	 *                the name of the generated class representing the
	 *                "record of/set of" type.
	 * @param ofTypeName
	 *                type name of the "record of/set of" element
	 * @param displayName
	 *                the user readable name of the type to be generated.
	 * @param isSetOf
	 *                {@code true}: set of, {@code false}: record of
	 */
	private static void generateTemplateCopyTemplate(final JavaGenData aData, final StringBuilder source, final String genName, final String ofTypeName,
													  final String displayName, final boolean isSetOf ) {
		if (aData.isDebug()) {
			source.append("\t\t/**\n");
			source.append("\t\t * Internal function to copy the provided value into this template.\n");
			source.append("\t\t * The template becomes a specific value template.\n");
			source.append("\t\t * The already existing content is overwritten.\n");
			source.append("\t\t *\n");
			source.append("\t\t * @param other_value the value to be copied.\n");
			source.append("\t\t * */\n");
		}
		source.append( MessageFormat.format( "\t\tprotected void copy_value(final {0} other_value) '{'\n", genName ) );
		source.append( MessageFormat.format( "\t\t\tother_value.must_bound(\"Initialization of a template of type {0} with an unbound value.\");\n", displayName ) );
		source.append( MessageFormat.format( "\t\t\tvalue_elements = new ArrayList<{0}>();\n", ofTypeName ) );
		source.append("\t\t\tfinal int otherSize = other_value.valueElements.size();\n");
		source.append("\t\t\tfor (int elem_count = 0; elem_count < otherSize; elem_count++) {\n");
		source.append("\t\t\t\tif (other_value.constGet_at(elem_count).is_bound()) {\n");
		source.append( MessageFormat.format( "\t\t\t\t\tvalue_elements.add( new {0}(other_value.constGet_at(elem_count)) );\n", ofTypeName ) );
		source.append("\t\t\t\t} else {\n");
		source.append( MessageFormat.format( "\t\t\t\t\tvalue_elements.add( new {0}() );\n", ofTypeName ) );
		source.append("\t\t\t\t}\n");
		source.append("\t\t\t}\n");
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
		source.append( MessageFormat.format( "\t\tprivate void copy_template(final {0}_template other_value) '{'\n", genName));
		source.append("\t\t\tswitch (other_value.template_selection) {\n");
		source.append("\t\t\tcase SPECIFIC_VALUE:\n");
		source.append( MessageFormat.format( "\t\t\t\tvalue_elements = new ArrayList<{0}>();\n", ofTypeName ) );
		source.append("\t\t\t\tfinal int otherSize = other_value.value_elements.size();\n");
		source.append("\t\t\t\tfor (int elem_count = 0; elem_count < otherSize; elem_count++) {\n");
		source.append("\t\t\t\t\tif (other_value.constGet_at(elem_count).is_bound()) {\n");
		source.append( MessageFormat.format( "\t\t\t\t\t\tvalue_elements.add( new {0}(other_value.constGet_at(elem_count)) );\n", ofTypeName ) );
		source.append("\t\t\t\t\t} else {\n");
		source.append( MessageFormat.format( "\t\t\t\t\t\tvalue_elements.add( new {0}() );\n", ofTypeName ) );
		source.append("\t\t\t\t\t}\n");
		source.append("\t\t\t\t}\n");
		source.append("\t\t\t\tbreak;\n");
		source.append("\t\t\tcase OMIT_VALUE:\n");
		source.append("\t\t\tcase ANY_VALUE:\n");
		source.append("\t\t\tcase ANY_OR_OMIT:\n");
		source.append("\t\t\t\tbreak;\n");
		source.append("\t\t\tcase VALUE_LIST:\n");
		source.append("\t\t\tcase COMPLEMENTED_LIST:\n");
		source.append( MessageFormat.format( "\t\t\t\tlist_value = new ArrayList<{0}_template>(other_value.list_value.size());\n", genName));
		source.append("\t\t\t\tfor(int i = 0; i < other_value.list_value.size(); i++) {\n");
		source.append( MessageFormat.format( "\t\t\t\t\tfinal {0}_template temp = new {0}_template(other_value.list_value.get(i));\n", genName));
		source.append("\t\t\t\t\tlist_value.add(temp);\n");
		source.append("\t\t\t\t}\n");
		source.append("\t\t\t\tbreak;\n");
		if ( isSetOf ) {
			source.append("\t\t\tcase SUPERSET_MATCH:\n");
			source.append("\t\t\tcase SUBSET_MATCH:\n");
			source.append( MessageFormat.format( "\t\t\t\tset_items = new ArrayList<{0}>(other_value.set_items.size());\n", ofTypeName ) );
			source.append("\t\t\t\tfor (int set_count = 0; set_count < other_value.set_items.size(); set_count++) {\n");
			source.append( MessageFormat.format( "\t\t\t\t\tfinal {0} temp = new {0}(other_value.set_items.get(set_count));\n", ofTypeName ) );
			source.append("\t\t\t\t\tset_items.add(temp);\n");
			source.append("\t\t\t\t}\n");
			source.append("\t\t\t\tbreak;\n");
		}
		source.append("\t\t\tdefault:\n");
		source.append( MessageFormat.format( "\t\t\t\tthrow new TtcnError(\"Copying an uninitialized template of type {0}.\");\n", displayName));
		source.append("\t\t\t}\n");
		source.append("\t\t\tset_selection(other_value);\n");
		source.append("\t\t}\n\n");

		source.append(MessageFormat.format("\t\tprivate void copy_template(final {0}_Of_Template other_value) '{'\n", isSetOf ? "Set" : "Record"));
		source.append("\t\t\tswitch (other_value.get_selection()) {\n");
		source.append("\t\t\tcase SPECIFIC_VALUE:\n");
		source.append( MessageFormat.format( "\t\t\t\tvalue_elements = new ArrayList<{0}>();\n", ofTypeName ) );
		source.append("\t\t\t\tfinal int otherSize = other_value.n_elem();\n");
		source.append("\t\t\t\tfor (int elem_count = 0; elem_count < otherSize; elem_count++) {\n");
		source.append( MessageFormat.format( "\t\t\t\t\tfinal {0} temp = new {0}();\n", ofTypeName ) );
		source.append("\t\t\t\t\tif (other_value.constGet_at(elem_count).is_bound()) {\n");
		source.append("\t\t\t\t\t\ttemp.operator_assign(other_value.constGet_at(elem_count));\n");
		//source.append( MessageFormat.format( "\t\t\t\t\t\tvalue_elements.add( new {0}(other_value.constGet_at(elem_count)) );\n", ofTypeName ) );
		//source.append("\t\t\t\t\t} else {\n");
		//source.append( MessageFormat.format( "\t\t\t\t\t\tvalue_elements.add( new {0}() );\n", ofTypeName ) );
		source.append("\t\t\t\t\t}\n");
		source.append("\t\t\t\t\tvalue_elements.add( temp );\n");
		source.append("\t\t\t\t}\n");
		source.append("\t\t\t\tbreak;\n");
		source.append("\t\t\tcase OMIT_VALUE:\n");
		source.append("\t\t\tcase ANY_VALUE:\n");
		source.append("\t\t\tcase ANY_OR_OMIT:\n");
		source.append("\t\t\t\tbreak;\n");
		source.append("\t\t\tcase VALUE_LIST:\n");
		source.append("\t\t\tcase COMPLEMENTED_LIST:\n");
		source.append("\t\t\t\tfinal int otherListSize = other_value.n_list_elem();\n");
		source.append( MessageFormat.format( "\t\t\t\tlist_value = new ArrayList<{0}_template>(otherListSize);\n", genName));
		source.append("\t\t\t\tfor(int i = 0; i < otherListSize; i++) {\n");
		source.append( MessageFormat.format( "\t\t\t\t\tfinal {0}_template temp = new {0}_template();\n", genName));
		source.append("\t\t\t\t\ttemp.operator_assign(other_value.list_item(i));\n");
		source.append("\t\t\t\t\tlist_value.add(temp);\n");
		source.append("\t\t\t\t}\n");
		source.append("\t\t\t\tbreak;\n");
		if ( isSetOf ) {
			source.append("\t\t\tcase SUPERSET_MATCH:\n");
			source.append("\t\t\tcase SUBSET_MATCH:\n");
			source.append("\t\t\t\tfinal int otherSetSize = other_value.n_set_items();\n");
			source.append( MessageFormat.format( "\t\t\t\tset_items = new ArrayList<{0}>(otherSetSize);\n", ofTypeName ) );
			source.append("\t\t\t\tfor (int set_count = 0; set_count < otherSetSize; set_count++) {\n");
			source.append( MessageFormat.format( "\t\t\t\t\tfinal {0} temp = new {0}();\n", ofTypeName ) );
			source.append("\t\t\t\t\ttemp.operator_assign(other_value.set_item(set_count));\n");
			source.append("\t\t\t\t\tset_items.add(temp);\n");
			source.append("\t\t\t\t}\n");
			source.append("\t\t\t\tbreak;\n");
		}
		source.append("\t\t\tdefault:\n");
		source.append( MessageFormat.format( "\t\t\t\tthrow new TtcnError(\"Copying an uninitialized template of type {0}.\");\n", displayName));
		source.append("\t\t\t}\n");
		source.append("\t\t\tset_selection(other_value);\n");
		source.append("\t\t}\n");
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
	 *                "record of/set of" type.
	 * @param displayName
	 *                the user readable name of the type to be generated.
	 * @param isSetOf
	 *                {@code true}: set of, {@code false}: record of
	 */
	private static void generateTemplateMatch( final JavaGenData aData, final StringBuilder source, final String genName, final String displayName, final boolean isSetOf ) {
		source.append('\n');
		if (aData.isDebug()) {
			source.append("\t\t/**\n");
			source.append("\t\t * Matches the provided value against this template.\n");
			source.append("\t\t *\n");
			source.append("\t\t * @param other_value the value to be matched.\n");
			source.append("\t\t * */\n");
		}
		source.append( MessageFormat.format( "\t\tpublic boolean match(final {0} other_value) '{'\n", genName ) );
		source.append("\t\t\treturn match(other_value, false);\n");
		source.append("\t\t}\n");

		source.append('\n');
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
		source.append( MessageFormat.format( "\t\tpublic boolean match(final {0} other_value, final boolean legacy) '{'\n", genName ) );
		source.append("\t\t\tif(!other_value.is_bound()) {\n");
		source.append("\t\t\t\treturn false;\n");
		source.append("\t\t\t}\n");
		source.append("\t\t\tfinal int value_length = other_value.size_of().get_int();\n");
		source.append("\t\t\tif (!match_length(value_length)) {\n");
		source.append("\t\t\t\treturn false;\n");
		source.append("\t\t\t}\n");
		source.append("\t\t\tswitch (template_selection) {\n");
		source.append("\t\t\tcase ANY_VALUE:\n");
		source.append("\t\t\tcase ANY_OR_OMIT:\n");
		source.append("\t\t\t\treturn true;\n");
		source.append("\t\t\tcase OMIT_VALUE:\n");
		source.append("\t\t\t\treturn false;\n");
		source.append("\t\t\tcase SPECIFIC_VALUE:\n");
		if ( isSetOf ) {
			source.append("\t\t\t\treturn RecordOf_Match.match_set_of(other_value, value_length, this, value_elements.size(), match_function_specific, legacy);\n");
		} else {
			source.append("\t\t\t\treturn RecordOf_Match.match_record_of(other_value, value_length, this, value_elements.size(), match_function_specific, legacy);\n");
		}
		source.append("\t\t\tcase VALUE_LIST:\n");
		source.append("\t\t\tcase COMPLEMENTED_LIST: {\n");
		source.append("\t\t\t\tfinal int list_size = list_value.size();\n");
		source.append("\t\t\t\tfor(int i = 0 ; i < list_size; i++) {\n");
		source.append("\t\t\t\t\tif(list_value.get(i).match(other_value, legacy)) {\n");
		source.append("\t\t\t\t\t\treturn template_selection == template_sel.VALUE_LIST;\n");
		source.append("\t\t\t\t\t}\n");
		source.append("\t\t\t\t}\n");
		source.append("\t\t\t\treturn template_selection == template_sel.COMPLEMENTED_LIST;\n");
		source.append("\t\t\t}\n");
		if ( isSetOf ) {
			source.append("\t\t\tcase SUPERSET_MATCH:\n");
			source.append("\t\t\tcase SUBSET_MATCH:\n");
			source.append("\t\t\t\treturn RecordOf_Match.match_set_of(other_value, value_length, this, set_items.size(), match_function_set, legacy);\n");
		}
		source.append("\t\t\tdefault:\n");
		source.append( MessageFormat.format( "\t\t\t\tthrow new TtcnError(\"Matching with an uninitialized/unsupported template of type {0}.\");\n", displayName ) );
		source.append("\t\t\t}\n");
		source.append("\t\t}\n");

		source.append('\n');
		source.append( MessageFormat.format( "\t\tprivate boolean match_index(final {0} value_ptr, final int value_index, final {0}_template template_ptr, final int template_index, final boolean legacy) '{'\n", genName ) );
		source.append("\t\t\tif (value_index >= 0) {\n");
		source.append("\t\t\t\treturn template_ptr.value_elements.get(template_index).match(value_ptr.valueElements.get(value_index), legacy);\n");
		source.append("\t\t\t} else {\n");
		source.append("\t\t\t\treturn template_ptr.value_elements.get(template_index).is_any_or_omit();\n");
		source.append("\t\t\t}\n");
		source.append("\t\t}\n");

		if ( isSetOf ) {
			source.append('\n');
			source.append( MessageFormat.format( "\t\tprivate boolean match_set(final {0} value_ptr, final int value_index, final {0}_template template_ptr, final int template_index, final boolean legacy) '{'\n", genName ) );
			source.append("\t\t\tif (value_index >= 0) {\n");
			source.append("\t\t\t\treturn template_ptr.set_items.get(template_index).match(value_ptr.valueElements.get(value_index), legacy);\n");
			source.append("\t\t\t} else {\n");
			source.append("\t\t\t\treturn template_ptr.set_items.get(template_index).is_any_or_omit();\n");
			source.append("\t\t\t}\n");
			source.append("\t\t}\n");
		}

		source.append('\n');
		source.append("\t\t@Override\n");
		source.append( MessageFormat.format( "\t\tpublic boolean match(final Base_Type otherValue, final boolean legacy) '{'\n", genName ) );
		source.append( MessageFormat.format( "\t\t\tif (otherValue instanceof {0}) '{'\n", genName) );
		source.append( MessageFormat.format( "\t\t\t\treturn match(({0})otherValue, legacy);\n", genName) );
		source.append("\t\t\t}\n\n");
		source.append( MessageFormat.format( "\t\t\tthrow new TtcnError(\"Internal Error: The left operand of assignment is not of type {0}.\");\n", genName ) );
		source.append("\t\t}\n");
	}

	/**
	 * Generate the match_omit function
	 *
	 * @param source
	 *                where the source code is to be generated.
	 */
	private static void generateTemplateMatchOmit( final StringBuilder source ) {
		source.append('\n');
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
		source.append("\t\t\t\t\tfinal int list_size = list_value.size();\n");
		source.append("\t\t\t\t\tfor (int i = 0 ; i < list_size; i++) {\n");
		source.append("\t\t\t\t\t\tif (list_value.get(i).match_omit(legacy)) {\n");
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
	 * Generate assign functions for template
	 *
	 * @param aData
	 *                used to access build settings.
	 * @param source
	 *                where the source code is to be generated.
	 * @param genName
	 *                the name of the generated class representing the
	 *                "record of/set of" type.
	 * @param ofTypeName
	 *                type name of the "record of/set of" element
	 * @param displayName
	 *                the user readable name of the type to be generated.
	 * @param isSetOf
	 *                {@code true}: set of, {@code false}: record of
	 */
	private static void generateTemplateoperator_assign(final JavaGenData aData, final StringBuilder source, final String genName, final String ofTypeName, final String displayName, final boolean isSetOf) {
		source.append("\t\t@Override\n");
		source.append( MessageFormat.format( "\t\tpublic {0}_template operator_assign( final template_sel otherValue ) '{'\n", genName ) );
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
		source.append( MessageFormat.format( "\t\tpublic {0}_template operator_assign( final {0} otherValue ) '{'\n", genName ) );
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
		source.append( MessageFormat.format( "\t\tpublic {0}_template operator_assign( final {0}_template otherValue ) '{'\n", genName ) );
		source.append("\t\t\tif (otherValue != this) {\n");
		source.append("\t\t\t\tclean_up();\n");
		source.append("\t\t\t\tcopy_template(otherValue);\n");
		source.append("\t\t\t}\n");
		source.append("\t\t\treturn this;\n");
		source.append("\t\t}\n\n");

		source.append( MessageFormat.format( "\t\tpublic {0}_template operator_assign( final {1}_Of_Template otherValue ) '{'\n", genName, isSetOf ? "Set" : "Record" ) );
		source.append("\t\t\tif (otherValue != this) {\n");
		source.append("\t\t\t\tclean_up();\n");
		source.append("\t\t\t\tcopy_template(otherValue);\n");
		source.append("\t\t\t}\n");
		source.append("\t\t\treturn this;\n");
		source.append("\t\t}\n");

		source.append('\n');
		source.append("\t\t@Override\n");
		source.append( MessageFormat.format( "\t\tpublic {0}_template operator_assign(final Base_Type otherValue) '{'\n", genName ) );
		source.append( MessageFormat.format( "\t\t\tif (otherValue instanceof {0}) '{'\n", genName) );
		source.append( MessageFormat.format( "\t\t\t\treturn operator_assign(({0})otherValue);\n", genName) );
		source.append("\t\t\t}\n\n");
		source.append( MessageFormat.format( "\t\t\tthrow new TtcnError(\"Internal Error: The left operand of assignment is not of type {0}.\");\n", genName ) );
		source.append("\t\t}\n");

		source.append('\n');
		source.append("\t\t@Override\n");
		source.append( MessageFormat.format( "\t\tpublic {0}_template operator_assign(final Base_Template otherValue) '{'\n", genName ) );
		source.append( MessageFormat.format( "\t\t\tif (otherValue instanceof {0}_template) '{'\n", genName) );
		source.append( MessageFormat.format( "\t\t\t\treturn operator_assign(({0}_template)otherValue);\n", genName) );
		source.append("\t\t\t}\n\n");
		source.append(MessageFormat.format("\t\t\tif (otherValue instanceof {0}_Of_Template) '{'\n", isSetOf ? "Set" : "Record"));
		source.append(MessageFormat.format("\t\t\t\treturn operator_assign(({0}_Of_Template)otherValue);\n", isSetOf ? "Set" : "Record"));
		source.append("\t\t\t}\n\n");
		source.append( MessageFormat.format( "\t\t\tthrow new TtcnError(\"Internal Error: The left operand of assignment is not of type {0}_template.\");\n", genName ) );
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
		source.append( MessageFormat.format( "\t\tpublic {0}_template operator_assign( final Optional<{0}> other_value ) '{'\n", genName ) );
		source.append("\t\t\tclean_up();\n");
		source.append("\t\t\tswitch (other_value.get_selection()) {\n");
		source.append("\t\t\tcase OPTIONAL_PRESENT:\n");
		source.append("\t\t\t\tcopy_value(other_value.constGet());\n");
		source.append("\t\t\t\tbreak;\n");
		source.append("\t\t\tcase OPTIONAL_OMIT:\n");
		source.append("\t\t\t\tset_selection(template_sel.OMIT_VALUE);\n");
		source.append("\t\t\t\tbreak;\n");
		source.append("\t\t\tdefault:\n");
		source.append( MessageFormat.format( "\t\t\t\tthrow new TtcnError(\"Assignment of an unbound optional field to a template of type {0}.\");\n", displayName ) );
		source.append("\t\t\t}\n");
		source.append("\t\t\treturn this;\n");
		source.append("\t\t}\n");

		if ( aData.isDebug() ) {
			source.append("\t\t/**\n");
			source.append("\t\t * Sets the current template to empty.\n");
			source.append("\t\t * Overwriting the current content in the process.\n");
			source.append("\t\t *<p>\n");
			source.append("\t\t * operator= in the core.\n");
			source.append("\t\t *\n");
			source.append("\t\t * @param nullValue\n");
			source.append("\t\t *                the null value.\n");
			source.append("\t\t * @return the new template object.\n");
			source.append("\t\t */\n");
		}
		source.append( MessageFormat.format( "\t\tpublic {0}_template operator_assign(final TitanNull_Type nullValue) '{'\n", genName ) );
		source.append("\t\t\tclean_up();\n");
		source.append("\t\t\tset_selection(template_sel.SPECIFIC_VALUE);\n");
		source.append( MessageFormat.format( "\t\t\tvalue_elements = new ArrayList<{0}>();\n", ofTypeName ) );
		source.append("\t\t\treturn this;\n");
		source.append("\t\t}\n\n");
	}

	/**
	 * Generate the clean_up function for template
	 *
	 * @param source
	 *                where the source code is to be generated.
	 */
	private static void generateTemplateCleanup(final StringBuilder source) {
		source.append('\n');
		source.append("\t\t@Override\n");
		source.append("\t\tpublic void clean_up() {\n");
		source.append("\t\t\tswitch (template_selection) {\n");
		source.append("\t\t\tcase SPECIFIC_VALUE:\n");
		source.append("\t\t\t\tvalue_elements.clear();\n");
		source.append("\t\t\t\tvalue_elements = null;\n");
		source.append("\t\t\t\tbreak;\n");
		source.append("\t\t\tcase VALUE_LIST:\n");
		source.append("\t\t\tcase COMPLEMENTED_LIST:\n");
		source.append("\t\t\t\tlist_value.clear();\n");
		source.append("\t\t\t\tlist_value = null;\n");
		source.append("\t\t\t\tbreak;\n");
		source.append("\t\t\tdefault:\n");
		source.append("\t\t\t\tbreak;\n");
		source.append("\t\t\t}\n");
		source.append("\t\t\ttemplate_selection = template_sel.UNINITIALIZED_TEMPLATE;\n");
		source.append("\t\t}\n\n");
	}

	/**
	 * Generate replace functions for template
	 *
	 * @param aData
	 *                only used to update imports if needed
	 * @param source
	 *                where the source code is to be generated.
	 * @param genName
	 *                the name of the generated class representing the
	 *                "record of/set of" type.
	 * @param displayName
	 *                the user readable name of the type to be generated.
	 */
	private static void generateTemplateReplace(final JavaGenData aData, final StringBuilder source, final String genName, final String displayName) {
 		//int,int
		if (aData.isDebug()) {
			source.append("\t\t/**\n");
			source.append("\t\t * Creates a new record/set of value from the current value,\n");
			source.append("\t\t * with the parts from the provided index at the provided length\n");
			source.append("\t\t * being replaced by the provided values.\n");
			source.append("\t\t *\n");
			source.append("\t\t * @param index\n");
			source.append("\t\t *                the index to start replacing at.\n");
			source.append("\t\t * @param len\n");
			source.append("\t\t *                the number of elements to replace.\n");
			source.append("\t\t * @param repl\n");
			source.append("\t\t *                the values to insert.\n");
			source.append("\t\t * @return the new value.\n");
			source.append("\t\t * */\n");
		}
		source.append( MessageFormat.format( "\t\tpublic {0} replace(final int index, final int len, final {0}_template repl) '{'\n", genName ) );
		source.append("\t\t\tif (!is_value()) {\n");
		source.append("\t\t\t\tthrow new TtcnError(\"The first argument of function replace() is a template with non-specific value.\");\n");
		source.append("\t\t\t}\n");
		source.append("\t\t\tif (!repl.is_value()) {\n");
		source.append("\t\t\t\tthrow new TtcnError(\"The fourth argument of function replace() is a template with non-specific value.\");\n");
		source.append("\t\t\t}\n");
		source.append("\t\t\treturn valueof().replace(index, len, repl.valueof());\n");
		source.append("\t\t}\n\n");
		//TitanInteger, TitanInteger
		if (aData.isDebug()) {
			source.append("\t\t/**\n");
			source.append("\t\t * Creates a new record/set of value from the current value,\n");
			source.append("\t\t * with the parts from the provided index at the provided length\n");
			source.append("\t\t * being replaced by the provided values.\n");
			source.append("\t\t *\n");
			source.append("\t\t * @param index\n");
			source.append("\t\t *                the index to start replacing at.\n");
			source.append("\t\t * @param len\n");
			source.append("\t\t *                the number of elements to replace.\n");
			source.append("\t\t * @param repl\n");
			source.append("\t\t *                the values to insert.\n");
			source.append("\t\t * @return the new value.\n");
			source.append("\t\t * */\n");
		}
		source.append( MessageFormat.format( "\t\tpublic {0} replace(final TitanInteger index, final TitanInteger len, final {0}_template repl) '{'\n", genName ) );
		source.append("\t\t\tif (!is_value()) {\n");
		source.append("\t\t\t\tthrow new TtcnError(\"The first argument of function replace() is a template with non-specific value.\");\n");
		source.append("\t\t\t}\n");
		source.append("\t\t\tif (!repl.is_value()) {\n");
		source.append("\t\t\t\tthrow new TtcnError(\"The fourth argument of function replace() is a template with non-specific value.\");\n");
		source.append("\t\t\t}\n");
		source.append("\t\t\treturn valueof().replace(index.get_int(), len.get_int(), repl.valueof());\n");
		source.append("\t\t}\n\n");

		//int,int
		if (aData.isDebug()) {
			source.append("\t\t/**\n");
			source.append("\t\t * Creates a new record/set of value from the current value,\n");
			source.append("\t\t * with the parts from the provided index at the provided length\n");
			source.append("\t\t * being replaced by the provided values.\n");
			source.append("\t\t *\n");
			source.append("\t\t * @param index\n");
			source.append("\t\t *                the index to start replacing at.\n");
			source.append("\t\t * @param len\n");
			source.append("\t\t *                the number of elements to replace.\n");
			source.append("\t\t * @param repl\n");
			source.append("\t\t *                the values to insert.\n");
			source.append("\t\t * @return the new value.\n");
			source.append("\t\t * */\n");
		}
		source.append( MessageFormat.format( "\t\tpublic {0} replace(final int index, final int len, final {0} repl) '{'\n", genName ) );
		source.append("\t\t\tif (!is_value()) {\n");
		source.append("\t\t\t\tthrow new TtcnError(\"The first argument of function replace() is a template with non-specific value.\");\n");
		source.append("\t\t\t}\n");
		source.append("\t\t\treturn valueof().replace(index, len, repl);\n");
		source.append("\t\t}\n\n");
		//TitanInteger, TitanInteger
		if (aData.isDebug()) {
			source.append("\t\t/**\n");
			source.append("\t\t * Creates a new record/set of value from the current value,\n");
			source.append("\t\t * with the parts from the provided index at the provided length\n");
			source.append("\t\t * being replaced by the provided values.\n");
			source.append("\t\t *\n");
			source.append("\t\t * @param index\n");
			source.append("\t\t *                the index to start replacing at.\n");
			source.append("\t\t * @param len\n");
			source.append("\t\t *                the number of elements to replace.\n");
			source.append("\t\t * @param repl\n");
			source.append("\t\t *                the values to insert.\n");
			source.append("\t\t * @return the new value.\n");
			source.append("\t\t * */\n");
		}
		source.append( MessageFormat.format( "\t\tpublic {0} replace(final TitanInteger index, final TitanInteger len, final {0} repl) '{'\n", genName ) );
		source.append("\t\t\tif (!is_value()) {\n");
		source.append("\t\t\t\tthrow new TtcnError(\"The first argument of function replace() is a template with non-specific value.\");\n");
		source.append("\t\t\t}\n");
		source.append("\t\t\treturn valueof().replace(index.get_int(), len.get_int(), repl);\n");
		source.append("\t\t}\n\n");

		//TODO: perhaps one case is enough, if it is rethought
	}

	/**
	 * Generate getter and setter functions for template
	 *
	 * @param aData
	 *                only used to update imports if needed
	 * @param source
	 *                where the source code is to be generated.
	 * @param genName
	 *                the name of the generated class representing the
	 *                "record of/set of" type.
	 * @param ofTypeName
	 *                type name of the "record of/set of" element
	 * @param displayName
	 *                the user readable name of the type to be generated.
	 */
	private static void generateTemplateGetterSetters(final JavaGenData aData, final StringBuilder source, final String genName, final String ofTypeName, final String displayName) {
		source.append("\t\t@Override\n");
		source.append( MessageFormat.format( "\t\tpublic {0} get_at(final int index_value) '{'\n", ofTypeName ) );
		source.append("\t\t\tif (index_value < 0) {\n");
		source.append("\t\t\t\tthrow new TtcnError( MessageFormat.format( \"Accessing an element of a template for type "+displayName+" using a negative index: {0}.\", index_value ) );\n");
		source.append("\t\t\t}\n");
		source.append('\n');
		source.append("\t\t\tswitch (template_selection) {\n");
		source.append("\t\t\tcase SPECIFIC_VALUE:\n");
		source.append("\t\t\t\tif(index_value < value_elements.size()) {\n\n");
		source.append("\t\t\t\t\tbreak;\n");
		source.append("\t\t\t\t}\n");
		source.append("\t\t\t\t// no break\n");
		source.append("\t\t\tcase OMIT_VALUE:\n");
		source.append("\t\t\tcase ANY_VALUE:\n");
		source.append("\t\t\tcase ANY_OR_OMIT:\n");
		source.append("\t\t\tcase UNINITIALIZED_TEMPLATE:\n");
		source.append("\t\t\t\tset_size(index_value + 1);\n");
		source.append("\t\t\t\tbreak;\n");
		source.append("\t\t\tdefault:\n");
		source.append( MessageFormat.format( "\t\t\t\tthrow new TtcnError(\"Accessing an element of a non-specific template for type {0}.\");\n", displayName ) );
		source.append("\t\t\t}\n");
		source.append("\t\t\treturn value_elements.get(index_value);\n");
		source.append("\t\t}\n\n");

		source.append("\t\t@Override\n");
		source.append( MessageFormat.format( "\t\tpublic {0} get_at(final TitanInteger index_value) '{'\n", ofTypeName ) );
		source.append( MessageFormat.format( "\t\t\tindex_value.must_bound(\"Using an unbound integer value for indexing a template of type {0}.\");\n", displayName ) );
		source.append('\n');
		source.append("\t\t\treturn get_at(index_value.get_int());\n");
		source.append("\t\t}\n\n");

		source.append("\t\t@Override\n");
		source.append( MessageFormat.format( "\t\tpublic {0} constGet_at(final int index_value) '{'\n", ofTypeName ) );
		source.append("\t\t\tif (index_value < 0) {\n");
		source.append("\t\t\t\tthrow new TtcnError( MessageFormat.format( \"Accessing an element of a template for type "+displayName+" using a negative index: {0}.\", index_value ) );\n");
		source.append("\t\t\t}\n");
		source.append('\n');
		source.append("\t\t\tif (template_selection != template_sel.SPECIFIC_VALUE) {\n");
		source.append( MessageFormat.format( "\t\t\t\tthrow new TtcnError(\"Accessing an element of a non-specific template for type {0}.\");\n", displayName ) );
		source.append("\t\t\t}\n");
		source.append('\n');
		source.append("\t\t\tif (index_value >= value_elements.size()) {\n");
		source.append("\t\t\t\tthrow new TtcnError( MessageFormat.format( \"Index overflow in a template of type "+displayName+": The index is {0}, but the template has only {1} elements.\", index_value, value_elements.size() ) );\n");
		source.append("\t\t\t}\n");
		source.append('\n');
		source.append("\t\t\treturn value_elements.get(index_value);\n");
		source.append("\t\t}\n\n");

		source.append("\t\t@Override\n");
		source.append( MessageFormat.format( "\t\tpublic {0} constGet_at(final TitanInteger index_value) '{'\n", ofTypeName ) );
		source.append( MessageFormat.format( "\t\t\tindex_value.must_bound(\"Using an unbound integer value for indexing a template of type {0}.\");\n", displayName ) );
		source.append('\n');
		source.append("\t\t\treturn constGet_at(index_value.get_int());\n");
		source.append("\t\t}\n\n");
	}

	/**
	 * Generate getter and setter functions for template ONLY for set of
	 *
	 * @param aData
	 *                only used to update imports if needed
	 * @param source
	 *                where the source code is to be generated.
	 * @param genName
	 *                the name of the generated class representing the
	 *                "record of/set of" type.
	 * @param ofTypeName
	 *                type name of the "record of/set of" element
	 * @param displayName
	 *                the user readable name of the type to be generated.
	 */
	private static void generateTemplateGetterSettersSetOf(final JavaGenData aData, final StringBuilder source, final String genName, final String ofTypeName, final String displayName) {
		source.append("\t\tpublic int n_set_items() {\n");
		source.append("\t\t\tif (template_selection != template_sel.SUPERSET_MATCH && template_selection != template_sel.SUBSET_MATCH) {\n");
		source.append( MessageFormat.format( "\t\t\t\tthrow new TtcnError(\"Internal error: Accessing a set element of a non-set template of type {0}.\");\n", displayName ) );
		source.append("\t\t\t}\n");
		source.append("\t\t\treturn set_items.size();\n");
		source.append("\t\t}\n");

		source.append("\t\t@Override\n");
		source.append( MessageFormat.format( "\t\tpublic {0} set_item(final int set_index) '{'\n", ofTypeName ) );
		source.append("\t\t\tif (template_selection != template_sel.SUPERSET_MATCH && template_selection != template_sel.SUBSET_MATCH) {\n");
		source.append( MessageFormat.format( "\t\t\t\tthrow new TtcnError(\"Internal error: Accessing a set element of a non-set template of type {0}.\");\n", displayName ) );
		source.append("\t\t\t}\n");
		source.append("\t\t\tif (set_index >= set_items.size() ) {\n");
		source.append( MessageFormat.format( "\t\t\t\tthrow new TtcnError(\"Internal error: Index overflow in a set template of type {0}.\");\n", displayName ) );
		source.append("\t\t\t}\n");
		source.append("\t\t\treturn set_items.get(set_index);\n");
		source.append("\t\t}\n\n");
	}

	/**
	 * Generate concat functions for template
	 *
	 * @param source
	 *                where the source code is to be generated.
	 * @param genName
	 *                the name of the generated class representing the
	 *                "record of/set of" type.
	 * @param ofTypeName
	 *                type name of the "record of/set of" element
	 * @param displayName
	 *                the user readable name of the type to be generated.
	 */
	private static void generateTemplateConcat(final StringBuilder source, final String genName, final String ofTypeName, final String displayName) {
		source.append('\n');
		source.append("\t\tprotected int get_length_for_concat(final AtomicBoolean is_any_value) {\n");
		source.append("\t\t\tswitch (template_selection) {\n");
		source.append("\t\t\tcase SPECIFIC_VALUE:\n");
		source.append("\t\t\t\treturn value_elements.size();\n");
		source.append("\t\t\tcase ANY_VALUE:\n");
		source.append("\t\t\tcase ANY_OR_OMIT:\n");
		source.append("\t\t\t\tswitch (length_restriction_type) {\n");
		source.append("\t\t\t\tcase NO_LENGTH_RESTRICTION:\n");
		source.append("\t\t\t\t\tif (template_selection == template_sel.ANY_VALUE) {\n");
		source.append("\t\t\t\t\t\t// ? => { * }\n");
		source.append("\t\t\t\t\t\tis_any_value.set( true );\n");
		source.append("\t\t\t\t\t\treturn 1;\n");
		source.append("\t\t\t\t\t}\n");
		source.append("\t\t\t\t\tthrow new TtcnError(\"Operand of record of template concatenation is an AnyValueOrNone (*) matching mechanism with no length restriction\");\n");
		source.append("\t\t\t\tcase RANGE_LENGTH_RESTRICTION:\n");
		source.append("\t\t\t\t\tif (range_length_max_length == 0 || range_length_max_length != range_length_min_length) {\n");
		source.append("\t\t\t\t\t\tthrow new TtcnError( MessageFormat.format( \"Operand of record of template concatenation is an {0} matching mechanism with non-fixed length restriction\", template_selection == template_sel.ANY_VALUE ? \"AnyValue (?)\" : \"AnyValueOrNone (*)\" ) );\n");
		source.append("\t\t\t\t\t}\n");
		source.append("\t\t\t\t\t// else fall through (range length restriction is allowed if the minimum\n");
		source.append("\t\t\t\t\t// and maximum value are the same)\n");
		source.append("\t\t\t\tcase SINGLE_LENGTH_RESTRICTION:\n");
		source.append("\t\t\t\t\t// ? length(N) or * length(N) => { ?, ?, ... ? } N times\n");
		source.append("\t\t\t\t\treturn length_restriction_type == length_restriction_type_t.SINGLE_LENGTH_RESTRICTION ? single_length : range_length_min_length;\n");
		source.append("\t\t\t\t}\n");
		source.append("\t\t\tdefault:\n");
		source.append("\t\t\t\tthrow new TtcnError(\"Operand of record of template concatenation is an uninitialized or unsupported template.\");\n");
		source.append("\t\t\t}\n");
		source.append("\t\t}\n");

		source.append('\n');
		source.append( MessageFormat.format( "\t\tprotected int get_length_for_concat(final {0} operand) '{'\n", genName ) );
		source.append("\t\t\toperand.must_bound(\"Operand of record of template concatenation is an unbound value.\");\n");
		source.append("\t\t\treturn operand.valueElements.size();\n");
		source.append("\t\t}\n");

		source.append('\n');
		source.append("\t\tprotected int get_length_for_concat(final template_sel operand) {\n");
		source.append("\t\t\tif (operand == template_sel.ANY_VALUE) {\n");
		source.append("\t\t\t\t// ? => { * }\n");
		source.append("\t\t\t\treturn 1;\n");
		source.append("\t\t\t}\n");
		source.append("\t\t\tthrow new TtcnError(\"Operand of record of template concatenation is an uninitialized or unsupported template.\");\n");
		source.append("\t\t}\n");

		source.append('\n');
		//TODO: simplify, just use value_elements.add()
		source.append("\t\t//TODO: simplify, just use value_elements.add()\n");
		source.append( MessageFormat.format( "\t\tpublic void concat(final AtomicInteger pos, final {0}_template operand) '{'\n", genName ) );
		source.append("\t\t\t// all errors should have already been caught by the operand's\n");
		source.append("\t\t\t// get_length_for_concat() call;\n");
		source.append("\t\t\t// the result template (this) should already be set to SPECIFIC_VALUE and\n");
		source.append("\t\t\t// single_value.value_elements should already be allocated\n");
		source.append("\t\t\tswitch (operand.template_selection) {\n");
		source.append("\t\t\tcase SPECIFIC_VALUE:\n");
		source.append("\t\t\t\tfor (int i = 0; i < operand.value_elements.size(); ++i) {\n");
		source.append( MessageFormat.format( "\t\t\t\t\tvalue_elements.set( pos.get() + i, new {0}(operand.value_elements.get(i)) );\n", ofTypeName ) );
		source.append("\t\t\t\t}\n");
		source.append("\t\t\t\tpos.addAndGet( operand.value_elements.size() );\n");
		source.append("\t\t\t\tbreak;\n");
		source.append("\t\t\tcase ANY_VALUE:\n");
		source.append("\t\t\tcase ANY_OR_OMIT:\n");
		source.append("\t\t\t\tswitch (operand.length_restriction_type) {\n");
		source.append("\t\t\t\tcase NO_LENGTH_RESTRICTION:\n");
		source.append("\t\t\t\t\t// ? => { * }\n");
		source.append( MessageFormat.format( "\t\t\t\t\tvalue_elements.set( pos.get(), new {0}( template_sel.ANY_OR_OMIT ) );\n", ofTypeName ) );
		source.append("\t\t\t\t\tpos.incrementAndGet();\n");
		source.append("\t\t\t\t\tbreak;\n");
		source.append("\t\t\t\tcase RANGE_LENGTH_RESTRICTION:\n");
		source.append("\t\t\t\tcase SINGLE_LENGTH_RESTRICTION: {\n");
		source.append("\t\t\t\t\t// ? length(N) or * length(N) => { ?, ?, ... ? } N times\n");
		source.append("\t\t\t\t\tfinal int N = operand.length_restriction_type == length_restriction_type_t.SINGLE_LENGTH_RESTRICTION ? operand.single_length : operand.range_length_min_length;\n");
		source.append("\t\t\t\t\tfor (int i = 0; i < N; ++i) {\n");
		source.append( MessageFormat.format( "\t\t\t\t\t\tvalue_elements.set( pos.get() + i, new {0}( template_sel.ANY_VALUE ) );\n", ofTypeName ) );
		source.append("\t\t\t\t\t}\n");
		source.append("\t\t\t\t\tpos.addAndGet( N );\n");
		source.append("\t\t\t\t\tbreak; }\n");
		source.append("\t\t\t	}\n");
		source.append("\t\t\tdefault:\n");
		source.append("\t\t\t	break;\n");
		source.append("\t\t\t}\n");
		source.append("\t\t}\n\n");

		//TODO: implement void concat(int& pos, const Record_Of_Type& operand)
		//TODO: implement void concat(int& pos)
	}

	/**
	 * Generate set_size and sizeof functions for template
	 *
	 * @param aData
	 *                used to access build settings.
	 * @param source
	 *                where the source code is to be generated.
	 * @param genName
	 *                the name of the generated class representing the
	 *                "record of/set of" type.
	 * @param ofTypeName
	 *                type name of the "record of/set of" element
	 * @param displayName
	 *                the user readable name of the type to be generated.
	 * @param isSetOf
	 *                {@code true}: set of, {@code false}: record of
	 */
	private static void generateTemplateSetSize(final JavaGenData aData, final StringBuilder source, final String genName,
			final String ofTypeName, final String displayName, final boolean isSetOf) {
		if (aData.isDebug()) {
			source.append("\t\t/**\n");
			source.append("\t\t * Sets the new size of the template.\n");
			source.append("\t\t * Also makes turns it into a specific value template if not already.\n");
			source.append("\t\t * If the new size is bigger than actual, unbound elements are added to the end.\n");
			source.append("\t\t * If the new size is smaller than actual, excess elements are removed.\n");
			source.append("\t\t *\n");
			source.append("\t\t * @param newSize the new size to be used.\n");
			source.append("\t\t * */\n");
		}
		source.append("\t\tpublic void set_size(final int new_size) {\n");
		source.append("\t\t\tif (new_size < 0) {\n");
		source.append( MessageFormat.format( "\t\t\t\tthrow new TtcnError(\"Internal error: Setting a negative size for a template of type {0}.\");\n", displayName ) );
		source.append("\t\t\t}\n");
		source.append("\t\t\tfinal template_sel old_selection = template_selection;\n");
		source.append("\t\t\tif (old_selection != template_sel.SPECIFIC_VALUE) {\n");
		source.append("\t\t\t\tclean_up();\n");
		source.append("\t\t\t\tset_selection(template_sel.SPECIFIC_VALUE);\n");
		source.append("\t\t\t\tvalue_elements = null;\n");
		source.append("\t\t\t}\n");
		source.append("\t\t\tif (value_elements == null) {\n");
		source.append( MessageFormat.format( "\t\t\t\tvalue_elements = new ArrayList<{0}>(new_size);\n", ofTypeName ) );
		source.append("\t\t\t}\n");
		source.append("\t\t\tif (new_size > value_elements.size()) {\n");
		source.append("\t\t\t\tif (old_selection == template_sel.ANY_VALUE || old_selection == template_sel.ANY_OR_OMIT) {\n");
		source.append("\t\t\t\t\tfor (int elem_count = value_elements.size(); elem_count < new_size; elem_count++) {\n");
		source.append( MessageFormat.format( "\t\t\t\t\t\tvalue_elements.add( new {0}(template_sel.ANY_VALUE) );\n", ofTypeName ) );
		source.append("\t\t\t\t\t}\n");
		source.append("\t\t\t\t} else {\n");
		source.append("\t\t\t\t\tfor (int elem_count = value_elements.size(); elem_count < new_size; elem_count++) {\n");
		source.append( MessageFormat.format( "\t\t\t\t\t\tvalue_elements.add( new {0}() );\n", ofTypeName ) );
		source.append("\t\t\t\t\t}\n");
		source.append("\t\t\t\t}\n");
		source.append("\t\t\t} else if (new_size < value_elements.size()) {\n");
		source.append("\t\t\t\twhile(value_elements.size() > new_size) {\n");
		source.append("\t\t\t\t\tvalue_elements.remove(value_elements.size()-1);\n");
		source.append("\t\t\t\t}\n");
		source.append("\t\t\t}\n");
		source.append("\t\t}\n\n");

		if (aData.isDebug()) {
			source.append("\t\t/**\n");
			source.append("\t\t * Returns the number of elements.\n");
			source.append("\t\t * The value to be returned is the maximum of the minimal length restriction value of the type,\n");
			source.append("\t\t *  or 0 for types with no minimal length restriction,\n");
			source.append("\t\t *  and the index of the last initialized element plus 1.\n");
			source.append("\t\t *\n");
			source.append("\t\t * size_of in the core.\n");
			source.append("\t\t * deprecated by the standard.\n");
			source.append("\t\t *\n");
			source.append("\t\t * @return the number of elements.\n");
			source.append("\t\t * */\n");
		}
		source.append("\t\tpublic TitanInteger size_of() {\n");
		source.append("\t\t\treturn sizeOf(true);\n");
		source.append("\t\t}\n\n");

		if (aData.isDebug()) {
			source.append("\t\t/**\n");
			source.append("\t\t * Returns the number of elements, that is, the largest used index plus\n");
			source.append("\t\t * one and zero for the empty value.\n");
			source.append("\t\t *\n");
			source.append("\t\t * lengthof in the core\n");
			source.append("\t\t *\n");
			source.append("\t\t * @return the number of elements.\n");
			source.append("\t\t * */\n");
		}
		source.append("\t\tpublic TitanInteger lengthof() {\n");
		source.append("\t\t\treturn sizeOf(false);\n");
		source.append("\t\t}\n\n");

		if (aData.isDebug()) {
			source.append("\t\t/**\n");
			source.append("\t\t * A helper function to reduce code. Based on the parameter it\n");
			source.append("\t\t * can operate as size_of or lengthof.\n");
			source.append("\t\t *\n");
			source.append("\t\t * @param is_size\n");
			source.append("\t\t *                {@code true} to operate as size_of,\n");
			source.append("\t\t *                {@code false} otherwise.\n");
			source.append("\t\t * @return the appriopriate number based on the operation mode\n");
			source.append("\t\t *         selected.\n");
			source.append("\t\t * */\n");
		}
		source.append("\t\tpublic TitanInteger sizeOf(final boolean is_size) {\n");
		source.append("\t\t\tfinal String op_name = is_size ? \"size\" : \"length\";\n");
		source.append("\t\t\tif (is_ifPresent) {\n");
		source.append("\t\t\t\tthrow new TtcnError( MessageFormat.format( \"Performing {0}of() operation on a template of type "+displayName+" which has an ifpresent attribute.\", op_name ) );\n");
		source.append("\t\t\t}\n");
		source.append("\t\t\tint min_size;\n");
		source.append("\t\t\tboolean has_any_or_none;\n");
		source.append("\t\t\tswitch (template_selection)\n");
		source.append("\t\t\t{\n");
		source.append("\t\t\tcase SPECIFIC_VALUE: {\n");
		source.append("\t\t\t\tmin_size = 0;\n");
		source.append("\t\t\t\thas_any_or_none = false;\n");
		source.append("\t\t\t\tint elem_count = value_elements.size();\n");
		source.append("\t\t\t\tif (!is_size) {\n");
		source.append("\t\t\t\t\twhile (elem_count>0 && !(value_elements.get(elem_count-1)).is_bound()) {\n");
		source.append("\t\t\t\t\t\telem_count--;\n");
		source.append("\t\t\t\t\t}\n");
		source.append("\t\t\t\t}\n");
		source.append("\t\t\t\tfor (int i=0; i<elem_count; i++)\n");
		source.append("\t\t\t\t{\n");
		source.append("\t\t\t\t\tswitch (value_elements.get(i).get_selection())\n");
		source.append("\t\t\t\t\t{\n");
		source.append("\t\t\t\t\tcase OMIT_VALUE:\n");
		source.append("\t\t\t\t\t\tthrow new TtcnError( MessageFormat.format( \"Performing {0}of() operation on a template of type "+displayName+" containing omit element.\", op_name ) );\n");
		source.append("\t\t\t\t\tcase ANY_OR_OMIT:\n");
		source.append("\t\t\t\t\t\thas_any_or_none = true;\n");
		source.append("\t\t\t\t\t\tbreak;\n");
		source.append("\t\t\t\t\tdefault:\n");
		source.append("\t\t\t\t\t\tmin_size++;\n");
		source.append("\t\t\t\t\t\tbreak;\n");
		source.append("\t\t\t\t\t}\n");
		source.append("\t\t\t\t}\n");
		source.append("\t\t\t} break;\n");
		if ( isSetOf ) {
			source.append("\t\t\tcase SUPERSET_MATCH:\n");
			source.append("\t\t\tcase SUBSET_MATCH:\n");
			source.append("\t\t\t{\n");
			source.append("\t\t\t	min_size = 0;\n");
			source.append("\t\t\t	has_any_or_none = false;\n");
			source.append("\t\t\t	int elem_count = set_items.size();\n");
			source.append("\t\t\t	if (!is_size) {\n");
			source.append("\t\t\t		while (elem_count>0 && !set_items.get(elem_count-1).is_bound()) {\n");
			source.append("\t\t\t			elem_count--;\n");
			source.append("\t\t\t		}\n");
			source.append("\t\t\t	}\n");
			source.append("\t\t\t	for (int i=0; i<elem_count; i++) {\n");
			source.append("\t\t\t		switch (set_items.get(i).get_selection()) {\n");
			source.append("\t\t\t		case OMIT_VALUE:\n");
			source.append("\t\t\t			throw new TtcnError( MessageFormat.format( \"Performing {0}of() operation on a template of type "+displayName+" containing omit element.\", op_name ) );\n");
			source.append("\t\t\t		case ANY_OR_OMIT:\n");
			source.append("\t\t\t			has_any_or_none = true;\n");
			source.append("\t\t\t			break;\n");
			source.append("\t\t\t		default:\n");
			source.append("\t\t\t			min_size++;\n");
			source.append("\t\t\t			break;\n");
			source.append("\t\t\t		}\n");
			source.append("\t\t\t	}\n");
			source.append("\t\t\t	if (template_selection == template_sel.SUPERSET_MATCH) {\n");
			source.append("\t\t\t		has_any_or_none = true;\n");
			source.append("\t\t\t	} else {\n");
			source.append("\t\t\t		final int max_size = min_size;\n");
			source.append("\t\t\t		min_size = 0;\n");
			source.append("\t\t\t		if (!has_any_or_none) { // [0,max_size]\n");
			source.append("\t\t\t			switch (length_restriction_type) {\n");
			source.append("\t\t\t			case NO_LENGTH_RESTRICTION:\n");
			source.append("\t\t\t				if (max_size==0) {\n");
			source.append("\t\t\t					return new TitanInteger(0);\n");
			source.append("\t\t\t				}\n");
			source.append("\t\t\t				throw new TtcnError( MessageFormat.format( \"Performing {0}of() operation on a template of type "+displayName+" with no exact size.\", op_name ) );\n");
			source.append("\t\t\t			case SINGLE_LENGTH_RESTRICTION:\n");
			source.append("\t\t\t				if (single_length <= max_size) {\n");
			source.append("\t\t\t					return new TitanInteger(single_length);\n");
			source.append("\t\t\t				}\n");
			source.append("\t\t\t				throw new TtcnError( MessageFormat.format( \"Performing {0}of() operation on an invalid template of type "+displayName+". The maximum size ({1}) contradicts the length restriction ({2}).\", op_name, max_size, single_length ) );\n");
			source.append("\t\t\t			case RANGE_LENGTH_RESTRICTION:\n");
			source.append("\t\t\t				if (max_size == range_length_min_length) {\n");
			source.append("\t\t\t					return new TitanInteger(max_size);\n");
			source.append("\t\t\t				} else if (max_size > range_length_min_length) {\n");
			source.append("\t\t\t					throw new TtcnError( MessageFormat.format( \"Performing {0}of() operation on a template of type "+displayName+" with no exact size.\", op_name ) );\n");
			source.append("\t\t\t				} else {\n");
			source.append("\t\t\t					throw new TtcnError( MessageFormat.format( \"Performing {0}of() operation on an invalid template of type "+displayName+". Maximum size ({1}) contradicts the length restriction ({2}..{3}).\", op_name, max_size, range_length_min_length, range_length_max_length ) );\n");
			source.append("\t\t\t				}\n");
			source.append("\t\t\t			default:\n");
			source.append("\t\t\t				throw new TtcnError(\"Internal error: Template has invalid length restriction type.\");\n");
			source.append("\t\t\t			}\n");
			source.append("\t\t\t		}\n");
			source.append("\t\t\t	}\n");
			source.append("\t\t\t}\n");
			source.append("\t\t\tbreak;\n");
		}
		source.append("\t\t\tcase OMIT_VALUE:\n");
		source.append("\t\t\t\tthrow new TtcnError( MessageFormat.format( \"Performing {0}of() operation on a template of type "+displayName+" containing omit value.\", op_name ) );\n");
		source.append("\t\t\tcase ANY_VALUE:\n");
		source.append("\t\t\tcase ANY_OR_OMIT:\n");
		source.append("\t\t\t\tmin_size = 0;\n");
		source.append("\t\t\t\thas_any_or_none = true;\n");
		source.append("\t\t\t\tbreak;\n");
		source.append("\t\t\tcase VALUE_LIST:\n");
		source.append("\t\t\t{\n");
		source.append("\t\t\t\tif (list_value.isEmpty()) {\n");
		source.append("\t\t\t\t\tthrow new TtcnError( MessageFormat.format( \"Performing {0}of() operation on a template of type "+displayName+" containing an empty list.\", op_name ) );\n");
		source.append("\t\t\t\t}\n");
		source.append("\t\t\t\tfinal int item_size = list_value.get(0).sizeOf(is_size).get_int();\n");
		source.append("\t\t\t\tfinal int list_size = list_value.size();\n");
		source.append("\t\t\t\tfor (int i = 1; i < list_size; i++) {\n");
		source.append("\t\t\t\t\tif (list_value.get(i).sizeOf(is_size).get_int()!=item_size) {\n");
		source.append("\t\t\t\t\t\tthrow new TtcnError( MessageFormat.format( \"Performing {0}of() operation on a template of type "+displayName+" containing a value list with different sizes.\", op_name ) );\n");
		source.append("\t\t\t\t\t}\n");
		source.append("\t\t\t\t}\n");
		source.append("\t\t\t\tmin_size = item_size;\n");
		source.append("\t\t\t\thas_any_or_none = false;\n");
		source.append("\t\t\t\tbreak;\n");
		source.append("\t\t\t}\n");
		source.append("\t\t\tcase COMPLEMENTED_LIST:\n");
		source.append("\t\t\t\tthrow new TtcnError( MessageFormat.format( \"Performing {0}of() operation on a template of type "+displayName+" containing complemented list.\", op_name ) );\n");
		source.append("\t\t\tdefault:\n");
		source.append("\t\t\t\tthrow new TtcnError( MessageFormat.format( \"Performing {0}of() operation on an uninitialized/unsupported template of type "+genName+".\", op_name ) );\n");
		source.append("\t\t\t}\n");
		source.append("\t\t\treturn new TitanInteger(check_section_is_single(min_size, has_any_or_none, op_name, \"a template of type\", \""+ofTypeName+"\"));\n");
		source.append("\t\t}\n\n");
	}

	/**
	 * Generate n_elem function for template
	 *
	 * @param aData
	 *                used to access build settings.
	 * @param source
	 *                where the source code is to be generated.
	 * @param genName
	 *                the name of the generated class representing the
	 *                "record of/set of" type.
	 */
	private static void generateTemplateNElem(final JavaGenData aData,final StringBuilder source, final String genName) {
		if (aData.isDebug()) {
			source.append("\t\t/**\n");
			source.append("\t\t * Returns the number of elements, that is, the largest used index plus\n");
			source.append("\t\t * one and zero for the empty value.\n");
			source.append("\t\t *\n");
			source.append("\t\t * n_elem in the core\n");
			source.append("\t\t *\n");
			source.append("\t\t * @return the number of elements.\n");
			source.append("\t\t * */\n");
		}
		source.append("\t\tpublic int n_elem() {\n");
		source.append("\t\t\tswitch (template_selection) {\n");
		source.append("\t\t\tcase SPECIFIC_VALUE:\n");
		source.append("\t\t\t\treturn value_elements.size();\n");
		source.append("\t\t\tcase COMPLEMENTED_LIST:\n");
		source.append( MessageFormat.format( "\t\t\t\tthrow new TtcnError(\"Performing n_elem() operation on a template of type {0} containing complemented list.\");\n", genName ) );
		source.append("\t\t\tcase UNINITIALIZED_TEMPLATE:\n");
		source.append("\t\t\tcase OMIT_VALUE:\n");
		source.append("\t\t\tcase ANY_VALUE:\n");
		source.append("\t\t\tcase ANY_OR_OMIT:\n");
		source.append("\t\t\tcase VALUE_LIST:\n");
		source.append("\t\t\tcase VALUE_RANGE:\n");
		source.append("\t\t\tcase STRING_PATTERN:\n");
		source.append("\t\t\tcase SUPERSET_MATCH:\n");
		source.append("\t\t\tcase SUBSET_MATCH:\n");
		source.append("\t\t\tcase DECODE_MATCH:\n");
		source.append("\t\t\t\tbreak;\n");
		source.append("\t\t\t}\n");
		source.append( MessageFormat.format( "\t\t\tthrow new TtcnError(\"Performing n_elem() operation on an uninitialized/unsupported template of type {0}.\");\n", genName ) );
		source.append("\t\t}\n");
	}

	/**
	 * Generate matchv function for template
	 *
	 * @param source
	 *                where the source code is to be generated.
	 * @param genName
	 *                the name of the generated class representing the
	 *                "record of/set of" type.
	 */
	private static void generateTemplateMatchv(final StringBuilder source, final String genName) {
		source.append('\n');
		source.append( MessageFormat.format( "\t\tpublic boolean matchv(final {0} other_value, final boolean legacy) '{'\n", genName ) );
		source.append("\t\t\tif (!other_value.is_bound()) {\n");
		source.append("\t\t\t\treturn false;\n");
		source.append("\t\t\t}\n");
		source.append("\t\t\tfinal int value_length = other_value.size_of().get_int();\n");
		source.append("\t\t\tif (!match_length(value_length)) {\n");
		source.append("\t\t\t\treturn false;\n");
		source.append("\t\t\t}\n");
		source.append("\t\t\tswitch (template_selection) {\n");
		source.append("\t\t\tcase SPECIFIC_VALUE:\n");
		source.append("\t\t\t\treturn RecordOf_Match.match_record_of(other_value, value_length, this, value_elements.size(), match_function_specific, legacy);\n");
		source.append("\t\t\tcase OMIT_VALUE:\n");
		source.append("\t\t\t\treturn false;\n");
		source.append("\t\t\tcase ANY_VALUE:\n");
		source.append("\t\t\tcase ANY_OR_OMIT:\n");
		source.append("\t\t\t\treturn true;\n");
		source.append("\t\t\tcase VALUE_LIST:\n");
		source.append("\t\t\tcase COMPLEMENTED_LIST: {\n");
		source.append("\t\t\t\tfinal int list_size = list_value.size();\n");
		source.append("\t\t\t\tfor (int list_count = 0; list_count < list_size; list_count++) {\n");
		source.append("\t\t\t\t\tif (list_value.get(list_count).matchv(other_value, legacy)) {\n");
		source.append("\t\t\t\t\t\treturn template_selection == template_sel.VALUE_LIST;\n");
		source.append("\t\t\t\t\t}\n");
		source.append("\t\t\t\t}\n");
		source.append("\t\t\t\treturn template_selection == template_sel.COMPLEMENTED_LIST;\n");
		source.append("\t\t\t}\n");
		source.append("\t\t\tdefault:\n");
		source.append( MessageFormat.format( "\t\t\t\tthrow new TtcnError(\"Matching with an uninitialized/unsupported template of type {0}.\");\n", genName ) );
		source.append("\t\t\t}\n");
		source.append("\t\t}\n");
	}

	/**
	 * Generate is_value function for template
	 *
	 * @param source
	 *                where the source code is to be generated.
	 * @param genName
	 *                the name of the generated class representing the
	 *                "record of/set of" type.
	 */
	private static void generateTemplateIsValue(final StringBuilder source, final String genName) {
		source.append('\n');
		source.append("\t\t@Override\n");
		source.append("\t\tpublic boolean is_value() {\n");
		source.append("\t\t\tif (template_selection != template_sel.SPECIFIC_VALUE || is_ifPresent) {\n");
		source.append("\t\t\t\treturn false;\n");
		source.append("\t\t\t}\n");
		source.append("\t\t\tfinal int elements_size = value_elements.size();\n");
		source.append("\t\t\tfor (int elem_count = 0; elem_count < elements_size; elem_count++) {\n");
		source.append("\t\t\t\tif (!value_elements.get(elem_count).is_value()) {\n");
		source.append("\t\t\t\t\treturn false;\n");
		source.append("\t\t\t\t}\n");
		source.append("\t\t\t}\n");
		source.append("\t\t\treturn true;\n");
		source.append("\t\t}\n");
	}

	/**
	 * Generate set_type function for template
	 *
	 * @param source
	 *                where the source code is to be generated.
	 * @param genName
	 *                the name of the generated class representing the
	 *                "record of/set of" type.
	 * @param ofTypeName
	 *                type name of the "record of/set of" element
	 * @param displayName
	 *                the user readable name of the type to be generated.
	 * @param isSetOf
	 *                {@code true}: set of, {@code false}: record of
	 */
	private static void generateTemplateSetType( final StringBuilder source, final String genName, final String ofTypeName,
												 final String displayName, final boolean isSetOf ) {
		source.append('\n');
		source.append("\t\t@Override\n");
		source.append("\t\tpublic void set_type(final template_sel template_type, final int list_length) {\n");
		source.append("\t\t\tclean_up();\n");
		source.append("\t\t\tswitch (template_type) {\n");
		source.append("\t\t\tcase VALUE_LIST:\n");
		source.append("\t\t\tcase COMPLEMENTED_LIST:\n");
		source.append( MessageFormat.format( "\t\t\t\tlist_value = new ArrayList<{0}_template>( list_length );\n", genName ) );
		source.append("\t\t\t\tfor (int list_count = 0; list_count < list_length; list_count++) {\n");
		source.append( MessageFormat.format( "\t\t\t\t\tlist_value.add( new {0}_template() );\n", genName ) );
		source.append("\t\t\t\t}\n");
		source.append("\t\t\t\tbreak;\n");
		if ( isSetOf ) {
			source.append("\t\t\tcase SUPERSET_MATCH:\n");
			source.append("\t\t\tcase SUBSET_MATCH:\n");
			source.append( MessageFormat.format( "\t\t\t\tset_items = new ArrayList<{0}>(list_length);\n", ofTypeName ) );
			source.append("\t\t\t\tfor( int i = 0; i < list_length; i++ ) {\n");
			source.append( MessageFormat.format( "\t\t\t\t\tset_items.add( new {0}() );\n", ofTypeName ) );
			source.append("\t\t\t\t}\n");
			source.append("\t\t\t\tbreak;\n");
		}
		source.append("\t\t\tdefault:\n");
		source.append( MessageFormat.format( "\t\t\t\tthrow new TtcnError(\"Internal error: Setting an invalid type for a template of type {0}.\");\n", displayName ) );
		source.append("\t\t\t}\n");
		source.append("\t\t\tset_selection(template_type);\n");
		source.append("\t\t}\n");
	}

	/**
	 * Generating list_item() function for template
	 *
	 * @param aSb
	 *                the output, where the java code is written
	 * @param genName
	 *                the name of the generated class representing the
	 *                "record of/set of" type.
	 * @param displayName
	 *                the user readable name of the type to be generated.
	 */
	private static void generateTemplateListItem( final StringBuilder aSb, final String genName, final String displayName ) {
		aSb.append('\n');
		aSb.append("\t\t@Override\n");
		aSb.append("\t\tpublic int n_list_elem() {\n");
		aSb.append("\t\t\tif (template_selection != template_sel.VALUE_LIST && template_selection != template_sel.COMPLEMENTED_LIST) {\n");
		aSb.append(MessageFormat.format( "\t\t\t\tthrow new TtcnError(\"Internal error: Accessing a list element of a non-list template of union type {0}.\");\n", displayName ) );
		aSb.append("\t\t\t}\n");
		aSb.append("\t\t\treturn list_value.size();\n");
		aSb.append("\t\t}\n");

		aSb.append("\t\t@Override\n");
		aSb.append( MessageFormat.format( "\t\tpublic {0}_template list_item(final int list_index) '{'\n", genName ) );
		aSb.append("\t\t\tif (template_selection != template_sel.VALUE_LIST && template_selection != template_sel.COMPLEMENTED_LIST) {\n");
		aSb.append( MessageFormat.format( "\t\t\t\tthrow new TtcnError(\"Accessing a list element of a non-list template of type {0}.\");\n", displayName ) );
		aSb.append("\t\t\t}\n");
		aSb.append("\t\t\tif (list_index < 0) {\n");
		aSb.append(MessageFormat.format("\t\t\t\tthrow new TtcnError(MessageFormat.format(\"Internal error: Accessing a value list template of type {0} using a negative index ('{'0'}').\", list_index));\n", displayName));
		aSb.append("\t\t\t} else if (list_index >= list_value.size()) {\n");
		aSb.append( MessageFormat.format( "\t\t\t\tthrow new TtcnError(\"Index overflow in a value list template of type {0}.\");\n", displayName ) );
		aSb.append("\t\t\t}\n");
		aSb.append("\t\t\treturn list_value.get(list_index);\n");
		aSb.append("\t\t}\n\n");
	}

	/**
	 * Generate get_list_item function for template
	 *
	 * @param aData
	 *                used to access build settings.
	 * @param source
	 *                where the source code is to be generated.
	 * @param genName
	 *                the name of the generated class representing the
	 *                "record of/set of" type.
	 * @param displayName
	 *                the user readable name of the type to be generated.
	 */
	private static void generateTemplateGetListItem(final JavaGenData aData, final StringBuilder source, final String genName, final String displayName) {
		if (aData.isDebug()) {
			source.append("\t\t/**\n");
			source.append("\t\t * Accessor for list items of value list and complemented list\n");
			source.append("\t\t * templates.\n");
			source.append("\t\t *\n");
			source.append("\t\t * Underflow and overflow results in dynamic testcase\n");
			source.append("\t\t * error. list_item in the core.\n");
			source.append("\t\t *\n");
			source.append("\t\t * @param list_index\n");
			source.append("\t\t *                the index of the list item.\n");
			source.append("\t\t * @return the list item at the provided index.\n");
			source.append("\t\t * */\n");
		}
		source.append( MessageFormat.format( "\t\tpublic {0}_template get_list_item(final int list_index) '{'\n", genName ) );
		source.append("\t\t\tif (template_selection != template_sel.VALUE_LIST && template_selection != template_sel.COMPLEMENTED_LIST) {\n");
		source.append( MessageFormat.format( "\t\t\t\tthrow new TtcnError(\"Internal error: Accessing a list element of a non-list template of type {0}.\");\n", displayName ) );
		source.append("\t\t\t}\n");
		source.append("\t\t\tif (list_index < 0) {\n");
		source.append(MessageFormat.format("\t\t\t\tthrow new TtcnError(MessageFormat.format(\"Internal error: Accessing a value list template of type {0} using a negative index ('{'0'}').\", list_index));\n", displayName));
		source.append("\t\t\t} else if (list_index >= list_value.size()) {\n");
		source.append( MessageFormat.format( "\t\t\t\tthrow new TtcnError(\"Internal error: Index overflow in a value list template of type {0}.\");\n", displayName ) );
		source.append("\t\t\t}\n");
		source.append("\t\t\treturn list_value.get( list_index );\n");
		source.append("\t\t}\n");
	}

	/**
	 * Generating valueof() function for template
	 *
	 * @param aSb
	 *                the output, where the java code is written
	 * @param genName
	 *                the name of the generated class representing the
	 *                "record of/set of" type.
	 * @param ofTypeName
	 *                type name of the "record of/set of" element
	 * @param displayName
	 *                the user readable name of the type to be generated.
	 */
	private static void generateTemplateValueOf( final StringBuilder aSb, final String genName, final String ofTypeName, final String displayName ) {
		aSb.append('\n');
		aSb.append("\t\t@Override\n");
		aSb.append( MessageFormat.format( "\t\tpublic {0} valueof() '{'\n", genName ) );
		aSb.append("\t\t\tif (template_selection != template_sel.SPECIFIC_VALUE || is_ifPresent) {\n");
		aSb.append( MessageFormat.format( "\t\t\t\tthrow new TtcnError(\"Performing a valueof or send operation on a non-specific template of type {0}.\");\n", displayName ) );
		aSb.append("\t\t\t}\n");
		aSb.append( MessageFormat.format( "\t\t\tfinal {0} ret_val = new {0}(TitanNull_Type.NULL_VALUE);\n", genName ) );
		aSb.append("\t\t\tfinal int elements_size = value_elements.size();\n");
		aSb.append("\t\t\tfor (int elem_count = 0; elem_count < elements_size; elem_count++) {\n");
		aSb.append(MessageFormat.format("\t\t\t\tfinal {0} temp = value_elements.get(elem_count);\n", ofTypeName));
		aSb.append("\t\t\t\tif (temp.is_bound()) {\n");
		aSb.append("\t\t\t\t\tret_val.valueElements.add( temp.valueof() );\n");
		aSb.append("\t\t\t\t}\n");
		aSb.append("\t\t\t}\n");
		aSb.append("\t\t\treturn ret_val;\n");
		aSb.append("\t\t}\n\n");
	}

	/**
	 * Generating substr() function for template
	 *
	 * @param aData
	 *                used to access build settings.
	 * @param aSb
	 *                the output, where the java code is written
	 * @param genName
	 *                the name of the generated class representing the
	 *                "record of/set of" type.
	 */
	private static void generateTemplateSubstr(final JavaGenData aData, final StringBuilder aSb, final String genName ) {
		if (aData.isDebug()) {
			aSb.append("\t\t/**\n");
			aSb.append("\t\t * Creates a new record/set of value from the current value,\n");
			aSb.append("\t\t * with the elements from the provided index at the provided length.\n");
			aSb.append("\t\t *\n");
			aSb.append("\t\t * @param index\n");
			aSb.append("\t\t *                the index to start at.\n");
			aSb.append("\t\t * @param returncount\n");
			aSb.append("\t\t *                the number of elements to copy.\n");
			aSb.append("\t\t * @return the new value.\n");
			aSb.append("\t\t * */\n");
		}
		aSb.append( MessageFormat.format( "\t\tpublic {0} substr(final int index, final int returncount) '{'\n", genName ) );
		aSb.append("\t\t\tif (!is_value()) {\n");
		aSb.append("\t\t\t\tthrow new TtcnError(\"The first argument of function substr() is a template with non-specific value.\");\n");
		aSb.append("\t\t\t}\n");
		aSb.append("\t\t\treturn valueof().substr(index, returncount);\n");
		aSb.append("\t\t}\n");
	}

	/**
	 * Generating log() function for template
	 *
	 * @param aData
	 *                used to access build settings.
	 * @param aSb
	 *                the output, where the java code is written
	 * @param genName
	 *                the name of the generated class representing the
	 *                "record of/set of" type.
	 * @param displayName
	 *                the user readable name of the type to be generated.
	 * @param isSetOf
	 *                {@code true}: set of, {@code false}: record of
	 */
	private static void generateTemplateLog(final JavaGenData aData, final StringBuilder aSb, final String genName, final String displayName, final boolean isSetOf ) {
		aSb.append('\n');
		aSb.append("\t\t@Override\n");
		aSb.append("\t\tpublic void log() {\n");
		aSb.append("\t\t\tswitch (template_selection) {\n");
		aSb.append("\t\t\tcase SPECIFIC_VALUE:\n");
		aSb.append("\t\t\t\tif (value_elements.isEmpty()) {\n");
		aSb.append("\t\t\t\t\tTTCN_Logger.log_event_str(\"{ }\");\n");
		aSb.append("\t\t\t\t} else {\n");
		aSb.append("\t\t\t\t\tTTCN_Logger.log_event_str(\"{ \");\n");
		aSb.append("\t\t\t\t\tfinal int elements_size = value_elements.size();\n");
		aSb.append("\t\t\t\t\tfor (int elem_count = 0; elem_count < elements_size; elem_count++) {\n");
		aSb.append("\t\t\t\t\t\tif (elem_count > 0) {\n");
		aSb.append("\t\t\t\t\t\t\tTTCN_Logger.log_event_str(\", \");\n");
		aSb.append("\t\t\t\t\t\t}\n");
		if ( !isSetOf ) {
			aSb.append("\t\t\t\t\t\tif (permutation_starts_at(elem_count)) {\n");
			aSb.append("\t\t\t\t\t\t\tTTCN_Logger.log_event_str(\"permutation(\");\n");
			aSb.append("\t\t\t\t\t\t}\n");
		}
		aSb.append("\t\t\t\t\t\tvalue_elements.get(elem_count).log();\n");
		if ( !isSetOf ) {
			aSb.append("\t\t\t\t\t\tif (permutation_ends_at(elem_count)) {\n");
			aSb.append("\t\t\t\t\t\t\tTTCN_Logger.log_char(')');\n");
			aSb.append("\t\t\t\t\t\t}\n");
		}
		aSb.append("\t\t\t\t\t}\n");
		aSb.append("\t\t\t\t\tTTCN_Logger.log_event_str(\" }\");\n");
		aSb.append("\t\t\t\t}\n");
		aSb.append("\t\t\t\tbreak;\n");
		aSb.append("\t\t\tcase COMPLEMENTED_LIST:\n");
		aSb.append("\t\t\t\tTTCN_Logger.log_event_str(\"complement\");\n");
		aSb.append("\t\t\tcase VALUE_LIST: {\n");
		aSb.append("\t\t\t\tTTCN_Logger.log_char('(');\n");
		aSb.append("\t\t\t\tfinal int list_size = list_value.size();\n");
		aSb.append("\t\t\t\tfor (int list_count = 0; list_count < list_size; list_count++) {\n");
		aSb.append("\t\t\t\t\tif (list_count > 0) {\n");
		aSb.append("\t\t\t\t\t\tTTCN_Logger.log_event_str(\", \");\n");
		aSb.append("\t\t\t\t\t}\n");
		aSb.append("\t\t\t\t\tlist_value.get(list_count).log();\n");
		aSb.append("\t\t\t\t}\n");
		aSb.append("\t\t\t\tTTCN_Logger.log_char(')');\n");
		aSb.append("\t\t\t\tbreak;\n");
		aSb.append("\t\t\t}\n");
		if ( isSetOf ) {
			aSb.append("\t\t\tcase SUPERSET_MATCH:\n");
			aSb.append("\t\t\tcase SUBSET_MATCH:\n");
			aSb.append("\t\t\t\tTTCN_Logger.log_event(\"%s(\", template_selection == template_sel.SUPERSET_MATCH ? \"superset\" : \"subset\");\n");
			aSb.append("\t\t\t\tfor (int set_count = 0; set_count < set_items.size(); set_count++) {\n");
			aSb.append("\t\t\t\t\tif (set_count > 0) {\n");
			aSb.append("\t\t\t\t\t\tTTCN_Logger.log_event_str(\", \");\n");
			aSb.append("\t\t\t\t\t}\n");
			aSb.append("\t\t\t\t\tset_items.get(set_count).log();\n");
			aSb.append("\t\t\t\t}\n");
			aSb.append("\t\t\t\tTTCN_Logger.log_char(')');\n");
			aSb.append("\t\t\t\tbreak;\n");
		}
		aSb.append("\t\t\tdefault:\n");
		aSb.append("\t\t\t\tlog_generic();\n");
		aSb.append("\t\t\t}\n");
		aSb.append("\t\t\tlog_restricted();\n");
		aSb.append("\t\t\tlog_ifpresent();\n");
		aSb.append("\t\t}\n");

		aSb.append('\n');
		if (aData.isDebug()) {
			aSb.append("\t\t/**\n");
			aSb.append("\t\t * Logs the matching of the provided value to this template, to help\n");
			aSb.append("\t\t * identify the reason for mismatch.\n");
			aSb.append("\t\t *\n");
			aSb.append("\t\t * @param match_value\n");
			aSb.append("\t\t *                the value to be matched.\n");
			aSb.append("\t\t * */\n");
		}
		aSb.append(MessageFormat.format("\t\tpublic void log_match(final {0} match_value) '{'\n", genName ) );
		aSb.append("\t\t\tlog_match(match_value, false);\n");
		aSb.append("\t\t}\n");

		aSb.append('\n');
		aSb.append("\t\t@Override\n");
		aSb.append("\t\tpublic void log_match(final Base_Type match_value, final boolean legacy) {\n");
		aSb.append(MessageFormat.format("\t\t\tif (match_value instanceof {0}) '{'\n", genName));
		aSb.append(MessageFormat.format("\t\t\t\tlog_match(({0})match_value, legacy);\n", genName));
		aSb.append("\t\t\t\treturn;\n");
		aSb.append("\t\t\t}\n\n");
		aSb.append(MessageFormat.format("\t\t\tthrow new TtcnError(\"Internal Error: value can not be cast to {0}.\");\n", displayName));
		aSb.append("\t\t}\n");

		aSb.append('\n');
		if (aData.isDebug()) {
			aSb.append("\t\t/**\n");
			aSb.append("\t\t * Logs the matching of the provided value to this template, to help\n");
			aSb.append("\t\t * identify the reason for mismatch. In legacy mode omitted value fields\n");
			aSb.append("\t\t * are not matched against the template field.\n");
			aSb.append("\t\t *\n");
			aSb.append("\t\t * @param match_value\n");
			aSb.append("\t\t *                the value to be matched.\n");
			aSb.append("\t\t * @param legacy\n");
			aSb.append("\t\t *                use legacy mode.\n");
			aSb.append("\t\t * */\n");
		}
		aSb.append(MessageFormat.format("\t\tpublic void log_match(final {0} match_value, final boolean legacy) '{'\n", genName ) );
		if ( isSetOf ) {
			aSb.append("\t\t\tif ( TTCN_Logger.matching_verbosity_t.VERBOSITY_COMPACT == TTCN_Logger.get_matching_verbosity() ) {\n");
			aSb.append("\t\t\t\tif(match(match_value, legacy)) {\n");
			aSb.append("\t\t\t\t\tTTCN_Logger.print_logmatch_buffer();\n");
			aSb.append("\t\t\t\t\tTTCN_Logger.log_event_str(\" matched\");\n");
			aSb.append("\t\t\t\t} else {\n");
			aSb.append("\t\t\t\t\tfinal int previous_size = TTCN_Logger.get_logmatch_buffer_len();\n");
			aSb.append("\t\t\t\t\tif (template_selection == template_sel.SPECIFIC_VALUE) {\n");
			aSb.append("\t\t\t\t\t\tRecordOf_Match.log_match_heuristics(match_value, match_value.size_of().get_int(), this, value_elements.size(), match_function_specific, log_function, legacy);\n");
			aSb.append("\t\t\t\t\t} else {\n");
			aSb.append("\t\t\t\t\t\tif(previous_size != 0) {\n");
			aSb.append("\t\t\t\t\t\t\tTTCN_Logger.print_logmatch_buffer();\n");
			aSb.append("\t\t\t\t\t\t\tTTCN_Logger.set_logmatch_buffer_len(previous_size);\n");
			aSb.append("\t\t\t\t\t\t\tTTCN_Logger.log_event_str(\":=\");\n");
			aSb.append("\t\t\t\t\t\t}\n");
			aSb.append("\t\t\t\t\t}\n");
			aSb.append("\t\t\t\t\tmatch_value.log();\n");
			aSb.append("\t\t\t\t\tTTCN_Logger.log_event_str(\" with \");\n");
			aSb.append("\t\t\t\t\tlog();\n");
			aSb.append("\t\t\t\t\tTTCN_Logger.log_event_str(\" unmatched\");\n");
			aSb.append("\t\t\t\t}\n");
			aSb.append("\t\t\t\treturn;\n");
			aSb.append("\t\t\t}\n");
			aSb.append("\t\t\tmatch_value.log();\n");
			aSb.append("\t\t\tTTCN_Logger.log_event_str(\" with \");\n");
			aSb.append("\t\t\tlog();\n");
			aSb.append("\t\t\tif (match(match_value, legacy)) {\n");
			aSb.append("\t\t\t\tTTCN_Logger.log_event_str(\" matched\");\n");
			aSb.append("\t\t\t} else {\n");
			aSb.append("\t\t\t\tTTCN_Logger.log_event_str(\" unmatched\");\n");
			aSb.append("\t\t\t\tif (template_selection == template_sel.SPECIFIC_VALUE) {\n");
			aSb.append("\t\t\t\t\tRecordOf_Match.log_match_heuristics(match_value, match_value.size_of().get_int(), this, value_elements.size(), match_function_specific, log_function, legacy);\n");
			aSb.append("\t\t\t\t}\n");
			aSb.append("\t\t\t}\n");
		} else {
			aSb.append("\t\t\tif ( TTCN_Logger.matching_verbosity_t.VERBOSITY_COMPACT == TTCN_Logger.get_matching_verbosity() ) {\n");
			aSb.append("\t\t\t\tif(match(match_value, legacy)) {\n");
			aSb.append("\t\t\t\t\tTTCN_Logger.print_logmatch_buffer();\n");
			aSb.append("\t\t\t\t\tTTCN_Logger.log_event_str(\" matched\");\n");
			aSb.append("\t\t\t\t} else {\n");

			aSb.append("\t\t\t\t\tif (template_selection == template_sel.SPECIFIC_VALUE && !value_elements.isEmpty() && get_number_of_permutations() == 0 && value_elements.size() == match_value.size_of().get_int()) {\n");
			aSb.append("\t\t\t\t\t\tfinal int previous_size = TTCN_Logger.get_logmatch_buffer_len();\n");
			aSb.append("\t\t\t\t\t\tfinal int elements_size = value_elements.size();\n");
			aSb.append("\t\t\t\t\t\tfor (int elem_count = 0; elem_count < elements_size; elem_count++) {\n");
			aSb.append("\t\t\t\t\t\t\tif ( !value_elements.get(elem_count).match(match_value.constGet_at(elem_count), legacy) ) {\n");
			aSb.append("\t\t\t\t\t\t\t\tTTCN_Logger.log_logmatch_info(\"[%d]\", elem_count);\n");
			aSb.append("\t\t\t\t\t\t\t\tvalue_elements.get(elem_count).log_match( match_value.constGet_at(elem_count), legacy );\n");
			aSb.append("\t\t\t\t\t\t\t\tTTCN_Logger.set_logmatch_buffer_len(previous_size);\n");
			aSb.append("\t\t\t\t\t\t\t}\n");
			aSb.append("\t\t\t\t\t\t}\n");
			aSb.append("\t\t\t\t\t\tlog_match_length(elements_size);\n");
			aSb.append("\t\t\t\t\t} else {\n");
			aSb.append("\t\t\t\t\t\tTTCN_Logger.print_logmatch_buffer();\n");
			aSb.append("\t\t\t\t\t\tmatch_value.log();\n");
			aSb.append("\t\t\t\t\t\tTTCN_Logger.log_event_str(\" with \");\n");
			aSb.append("\t\t\t\t\t\tlog();\n");
			aSb.append("\t\t\t\t\t\tTTCN_Logger.log_event_str(\" unmatched\");\n");
			aSb.append("\t\t\t\t\t}\n");
			aSb.append("\t\t\t\t}\n");
			aSb.append("\t\t\t\treturn;\n");
			aSb.append("\t\t\t}\n");
			aSb.append("\t\t\tif (template_selection == template_sel.SPECIFIC_VALUE && !value_elements.isEmpty() && get_number_of_permutations() == 0 && value_elements.size() == match_value.size_of().get_int()) {\n");
			aSb.append("\t\t\t\tTTCN_Logger.log_event_str(\"{ \");\n");
			aSb.append("\t\t\t\tfinal int elements_size = value_elements.size();\n");
			aSb.append("\t\t\t\tfor (int elem_count = 0; elem_count < elements_size; elem_count++) {\n");
			aSb.append("\t\t\t\t\tif (elem_count > 0) {\n");
			aSb.append("\t\t\t\t\t\tTTCN_Logger.log_event_str(\", \");\n");
			aSb.append("\t\t\t\t\t}\n");
			aSb.append("\t\t\t\t\tvalue_elements.get(elem_count).log_match( match_value.constGet_at(elem_count), legacy );\n");
			aSb.append("\t\t\t\t}\n");
			aSb.append("\t\t\t\tTTCN_Logger.log_event_str(\" }\");\n");
			aSb.append("\t\t\t\tlog_match_length(elements_size);\n");
			aSb.append("\t\t\t} else {\n");
			aSb.append("\t\t\t\tmatch_value.log();\n");
			aSb.append("\t\t\t\tTTCN_Logger.log_event_str(\" with \");\n");
			aSb.append("\t\t\t\tlog();\n");
			aSb.append("\t\t\t\tif ( match(match_value, legacy) ) {\n");
			aSb.append("\t\t\t\t\tTTCN_Logger.log_event_str(\" matched\");\n");
			aSb.append("\t\t\t\t} else {\n");
			aSb.append("\t\t\t\t\tTTCN_Logger.log_event_str(\" unmatched\");\n");

			aSb.append("\t\t\t\t}\n");
			aSb.append("\t\t\t}\n");
		}
		aSb.append("\t\t}\n\n");
	}

	/**
	 * Generate encode_text/decode_text
	 *
	 * @param source
	 *                where the source code is to be generated.
	 * @param genName
	 *                the name of the generated class representing the
	 *                "record of/set of" type.
	 * @param ofTypeName
	 *                type name of the "record of/set of" element
	 * @param displayName
	 *                the user readable name of the type to be generated.
	 * @param isSetOf
	 *                {@code true}: set of, {@code false}: record of
	 */
	private static void generateTemplateEncodeDecodeText( final StringBuilder aSb, final String genName, final String displayName, final String ofTypeName, final boolean isSetOf) {
		aSb.append("\t\t@Override\n");
		aSb.append("\t\tpublic void encode_text(final Text_Buf text_buf) {\n");
		aSb.append(MessageFormat.format("\t\t\tencode_text_{0}(text_buf);\n", isSetOf ? "restricted" : "permutation"));
		aSb.append("\t\t\tswitch (template_selection) {\n");
		aSb.append("\t\t\tcase OMIT_VALUE:\n");
		aSb.append("\t\t\tcase ANY_VALUE:\n");
		aSb.append("\t\t\tcase ANY_OR_OMIT:\n");
		aSb.append("\t\t\t\tbreak;\n");
		aSb.append("\t\t\tcase SPECIFIC_VALUE: {\n");
		aSb.append("\t\t\t\tfinal int elements_size = value_elements.size();\n");
		aSb.append("\t\t\t\ttext_buf.push_int(elements_size);\n");
		aSb.append("\t\t\t\tfor (int i = 0; i < elements_size; i++) {\n");
		aSb.append("\t\t\t\t\tvalue_elements.get(i).encode_text(text_buf);\n");
		aSb.append("\t\t\t\t}\n");
		aSb.append("\t\t\t\tbreak;\n");
		aSb.append("\t\t\t}\n");
		aSb.append("\t\t\tcase VALUE_LIST:\n");
		aSb.append("\t\t\tcase COMPLEMENTED_LIST:{\n");
		aSb.append("\t\t\t\tfinal int list_size = value_elements.size();\n");
		aSb.append("\t\t\t\ttext_buf.push_int(list_size);\n");
		aSb.append("\t\t\t\tfor (int i = 0; i < list_size; i++) {\n");
		aSb.append("\t\t\t\t\tlist_value.get(i).encode_text(text_buf);\n");
		aSb.append("\t\t\t\t}\n");
		aSb.append("\t\t\t\tbreak;\n");
		aSb.append("\t\t\t}\n");
		aSb.append("\t\t\tdefault:\n");
		aSb.append(MessageFormat.format("\t\t\t\tthrow new TtcnError(\"Text encoder: Encoding an uninitialized/unsupported template of type {0}.\");\n", displayName));
		aSb.append("\t\t\t}\n");
		aSb.append("\t\t}\n\n");

		aSb.append("\t\t@Override\n");
		aSb.append("\t\tpublic void decode_text(final Text_Buf text_buf) {\n");
		aSb.append("\t\t\tclean_up();\n");
		aSb.append(MessageFormat.format("\t\t\tdecode_text_{0}(text_buf);\n", isSetOf ? "restricted" : "permutation"));
		aSb.append("\t\t\tswitch (template_selection) {\n");
		aSb.append("\t\t\tcase OMIT_VALUE:\n");
		aSb.append("\t\t\tcase ANY_VALUE:\n");
		aSb.append("\t\t\tcase ANY_OR_OMIT:\n");
		aSb.append("\t\t\t\tbreak;\n");
		aSb.append("\t\t\tcase SPECIFIC_VALUE: {\n");
		aSb.append("\t\t\t\tfinal int temp = text_buf.pull_int().get_int();\n");
		aSb.append("\t\t\t\tif (temp < 0) {\n");
		aSb.append(MessageFormat.format("\t\t\t\t\tthrow new TtcnError(\"Text decoder: Negative size was received for a template of type {0}.\");\n", displayName));
		aSb.append("\t\t\t\t}\n");
		aSb.append(MessageFormat.format("\t\t\t\tvalue_elements = new ArrayList<{0}>(temp);\n", ofTypeName));
		aSb.append("\t\t\t\tfor (int i = 0; i < temp; i++) {\n");
		aSb.append(MessageFormat.format("\t\t\t\t\tfinal {0} temp2 = new {0}();\n", ofTypeName));
		aSb.append("\t\t\t\t\ttemp2.decode_text(text_buf);\n");
		aSb.append("\t\t\t\t\tvalue_elements.add(temp2);\n");
		aSb.append("\t\t\t\t}\n");
		aSb.append("\t\t\t\tbreak;\n");
		aSb.append("\t\t\t}\n");
		aSb.append("\t\t\tcase VALUE_LIST:\n");
		aSb.append("\t\t\tcase COMPLEMENTED_LIST: {\n");
		aSb.append("\t\t\t\tfinal int size = text_buf.pull_int().get_int();\n");
		aSb.append(MessageFormat.format("\t\t\t\tlist_value = new ArrayList<{0}_template>(size);\n", genName));
		aSb.append("\t\t\t\tfor (int i = 0; i < size; i++) {\n");
		aSb.append(MessageFormat.format("\t\t\t\t\tfinal {0}_template temp2 = new {0}_template();\n", genName));
		aSb.append("\t\t\t\t\ttemp2.decode_text(text_buf);\n");
		aSb.append("\t\t\t\t\tlist_value.add(temp2);\n");
		aSb.append("\t\t\t\t}\n");
		aSb.append("\t\t\t\tbreak;\n");
		aSb.append("\t\t\t}\n");
		aSb.append("\t\t\tdefault:\n");
		aSb.append(MessageFormat.format("\t\t\t\tthrow new TtcnError(\"Text decoder: An unknown/unsupported selection was received for a template of type {0}.\");\n", displayName));
		aSb.append("\t\t\t}\n");
		aSb.append("\t\t}\n\n");
	}

	/**
	 * Generate set_param
	 *
	 * @param source
	 *                where the source code is to be generated.
	 * @param ofTypeName
	 *                type name of the "record of/set of" element
	 * @param isSetOf
	 *                {@code true}: set of, {@code false}: record of
	 */
	private static void generateTemplateSetParam( final StringBuilder aSb, final String displayName, final boolean isSetOf) {
		aSb.append("\t\t@Override\n");
		aSb.append("\t\tpublic void set_param(final Module_Parameter param) {\n");
		aSb.append(MessageFormat.format("\t\t\tparam.basic_check(Module_Parameter.basic_check_bits_t.BC_TEMPLATE.getValue() | Module_Parameter.basic_check_bits_t.BC_LIST.getValue(), \"{0} of template\");\n", isSetOf ? "set" : "record"));
		aSb.append("\t\t\tswitch (param.get_type()) {\n");
		aSb.append("\t\t\tcase MP_Omit:\n");
		aSb.append("\t\t\t\toperator_assign(template_sel.OMIT_VALUE);\n");
		aSb.append("\t\t\t\tbreak;\n");
		aSb.append("\t\t\tcase MP_Any:\n");
		aSb.append("\t\t\t\toperator_assign(template_sel.ANY_VALUE);\n");
		aSb.append("\t\t\t\tbreak;\n");
		aSb.append("\t\t\tcase MP_AnyOrNone:\n");
		aSb.append("\t\t\t\toperator_assign(template_sel.ANY_OR_OMIT);\n");
		aSb.append("\t\t\t\tbreak;\n");
		aSb.append("\t\t\tcase MP_List_Template:\n");
		aSb.append("\t\t\tcase MP_ComplementList_Template: {\n");
		aSb.append("\t\t\t\tfinal int size = param.get_size();\n");
		aSb.append("\t\t\t\tset_type(param.get_type() == Module_Parameter.type_t.MP_List_Template ? template_sel.VALUE_LIST : template_sel.COMPLEMENTED_LIST, size);\n");
		aSb.append("\t\t\t\tfor (int i = 0; i < size; i++) {\n");
		aSb.append("\t\t\t\t\tlist_item(i).set_param(param.get_elem(i));\n");
		aSb.append("\t\t\t\t}\n");
		aSb.append("\t\t\t\tbreak;\n");
		aSb.append("\t\t\t}\n");
		aSb.append("\t\t\tcase MP_Indexed_List:\n");
		aSb.append("\t\t\t\tif (template_selection != template_sel.SPECIFIC_VALUE) {\n");
		aSb.append("\t\t\t\t\tset_size(0);\n");
		aSb.append("\t\t\t\t}\n");
		aSb.append("\t\t\t\tfor (int i = 0; i < param.get_size(); i++) {\n");
		aSb.append("\t\t\t\t\tget_at(param.get_elem(i).get_id().get_index()).set_param(param.get_elem(i));\n");
		aSb.append("\t\t\t\t}\n");
		aSb.append("\t\t\t\tbreak;\n");
		if (isSetOf) {
			aSb.append("\t\t\tcase MP_Value_List: {\n");
			aSb.append("\t\t\t\tset_size(param.get_size());\n");
			aSb.append("\t\t\t\tfor (int i = 0; i < param.get_size(); i++) {\n");
			aSb.append("\t\t\t\t\tif (param.get_elem(i).get_type() != Module_Parameter.type_t.MP_NotUsed) {\n");
			aSb.append("\t\t\t\t\t\tget_at(i).set_param(param.get_elem(i));\n");
			aSb.append("\t\t\t\t\t}\n");
			aSb.append("\t\t\t\t}\n");
			aSb.append("\t\t\t\tbreak;\n");
			aSb.append("\t\t\t}\n");
			aSb.append("\t\t\tcase MP_Superset_Template:\n");
			aSb.append("\t\t\tcase MP_Subset_Template:\n");
			aSb.append("\t\t\t\tset_type(param.get_type() == Module_Parameter.type_t.MP_Superset_Template ? template_sel.SUPERSET_MATCH : template_sel.SUBSET_MATCH, param.get_size());\n");
			aSb.append("\t\t\t\tfor (int i = 0; i < param.get_size(); i++) {\n");
			aSb.append("\t\t\t\t\tset_item(i).set_param(param.get_elem(i));\n");
			aSb.append("\t\t\t\t}\n");
			aSb.append("\t\t\t\tbreak;\n");
		} else {
			aSb.append("\t\t\tcase MP_Value_List: {\n");
			aSb.append("\t\t\t\tremove_all_permutations();\n");
			aSb.append("\t\t\t\tset_size(param.get_size());\n");
			aSb.append("\t\t\t\tint current_index = 0;\n");
			aSb.append("\t\t\t\tfor (int i = 0; i < param.get_size(); i++) {\n");
			aSb.append("\t\t\t\t\tswitch (param.get_elem(i).get_type()) {\n");
			aSb.append("\t\t\t\t\tcase MP_NotUsed:\n");
			aSb.append("\t\t\t\t\t\tcurrent_index++;\n");
			aSb.append("\t\t\t\t\t\tbreak;\n");
			aSb.append("\t\t\t\t\tcase MP_Permutation_Template: {\n");
			aSb.append("\t\t\t\t\t\tfinal int permutation_start_index = current_index;\n");
			aSb.append("\t\t\t\t\t\tfinal Module_Parameter param_i = param.get_elem(i);\n");
			aSb.append("\t\t\t\t\t\tfor (int perm_i = 0; perm_i < param_i.get_size(); perm_i++) {\n");
			aSb.append("\t\t\t\t\t\t\tget_at(current_index).set_param(param_i.get_elem(perm_i));\n");
			aSb.append("\t\t\t\t\t\t\tcurrent_index++;\n");
			aSb.append("\t\t\t\t\t\t}\n");
			aSb.append("\t\t\t\t\t\tfinal int permutation_end_index = current_index - 1;\n");
			aSb.append("\t\t\t\t\t\tadd_permutation(permutation_start_index, permutation_end_index);\n");
			aSb.append("\t\t\t\t\t\tbreak;\n");
			aSb.append("\t\t\t\t\t}\n");
			aSb.append("\t\t\t\t\tdefault:\n");
			aSb.append("\t\t\t\t\t\tget_at(current_index).set_param(param.get_elem(i));\n");
			aSb.append("\t\t\t\t\t\tcurrent_index++;\n");
			aSb.append("\t\t\t\t\t\tbreak;\n");
			aSb.append("\t\t\t\t\t}\n");
			aSb.append("\t\t\t\t}\n");
			aSb.append("\t\t\t\tbreak;\n");
			aSb.append("\t\t\t}\n");
		}
		aSb.append("\t\t\tdefault:\n");
		aSb.append(MessageFormat.format("\t\t\t\tparam.type_error(\"{0} of template\", \"{1}\");\n", isSetOf ? "set" : "record", displayName));
		aSb.append("\t\t\t\tbreak;\n");
		aSb.append("\t\t\t}\n");
		aSb.append("\t\t\tis_ifPresent = param.get_ifpresent();\n");
		aSb.append("\t\t\tset_length_range(param);\n");
		aSb.append("\t\t}\n");
	}

//TODO: implement void log_matchv(final Base_Type match_value, final boolean legacy)
/*
	void log_matchv(final Base_Type match_value, final boolean legacy)
	{
		if (TTCN_Logger.VERBOSITY_COMPACT == TTCN_Logger.get_matching_verbosity()) {
			if (matchv(match_value, legacy)) {
				TTCN_Logger.print_logmatch_buffer();
				TTCN_Logger.log_event_str(" matched");
			} else {
				final Record_Of_Type recof_value = (Record_Of_Type)(match_value);
				if (template_selection == SPECIFIC_VALUE &&
						single_value.n_elements > 0 && get_number_of_permutations() == 0 &&
						single_value.n_elements == recof_value.size_of()) {
					size_t previous_size = TTCN_Logger.get_logmatch_buffer_len();
					for (int elem_count = 0; elem_count < single_value.n_elements; elem_count++) {
						if(!single_value.value_elements[elem_count].matchv(recof_value.get_at(elem_count), legacy)){
							TTCN_Logger.log_logmatch_info("[{0}]", elem_count);
							single_value.value_elements[elem_count].log_matchv(recof_value.get_at(elem_count), legacy);
							TTCN_Logger.set_logmatch_buffer_len(previous_size);
						}
					}
					log_match_length(single_value.n_elements);
				} else {
					TTCN_Logger.print_logmatch_buffer();
					match_value.log();
					TTCN_Logger.log_event_str(" with ");
					log();
					TTCN_Logger.log_event_str(" unmatched");
				}
			}
		} else {
			final Record_Of_Type recof_value = static_cast<const Record_Of_Type*>(match_value);
			if (template_selection == SPECIFIC_VALUE &&
					single_value.n_elements > 0 && get_number_of_permutations() == 0 &&
					single_value.n_elements == recof_value.size_of()) {
				TTCN_Logger.log_event_str("{ ");
				for (int elem_count = 0; elem_count < single_value.n_elements; elem_count++) {
					if (elem_count > 0) TTCN_Logger.log_event_str(", ");
					single_value.value_elements[elem_count].log_matchv(recof_value.get_at(elem_count), legacy);
				}
				TTCN_Logger.log_event_str(" }");
				log_match_length(single_value.n_elements);
			} else {
				match_value.log();
				TTCN_Logger.log_event_str(" with ");
				log();
				if (matchv(match_value, legacy)) TTCN_Logger.log_event_str(" matched");
				else TTCN_Logger.log_event_str(" unmatched");
			}
		}
	}
*/
	//TODO: implement Module_Param* get_param(Module_Param_Name& param_name) const

	/**
	 * Generate get_istemplate_kind function for template
	 *
	 * @param source
	 *                where the source code is to be generated.
	 * @param genName
	 *                the name of the generated class representing the
	 *                "record of/set of" type.
	 * @param isSetOf
	 *                {@code true}: set of, {@code false}: record of
	 */
	private static void generateTemplateGetIstemplateKind(final StringBuilder source, final String genName, final boolean isSetOf) {
		source.append('\n');
		source.append("\t\t@Override\n");
		source.append("\t\tpublic boolean get_istemplate_kind(final String type) {\n");
		source.append("\t\t\tif (\"AnyElement\".equals(type)) {\n");
		source.append("\t\t\t\tif (template_selection != template_sel.SPECIFIC_VALUE) {\n");
		source.append("\t\t\t\t\treturn false;\n");
		source.append("\t\t\t\t}\n");
		source.append("\t\t\t\tfinal int elements_size = value_elements.size();\n");
		source.append("\t\t\t\tfor (int i = 0; i < elements_size; i++) {\n");
		source.append("\t\t\t\t\tif (value_elements.get(i).get_selection() == template_sel.ANY_VALUE) {\n");
		source.append("\t\t\t\t\t\treturn true;\n");
		source.append("\t\t\t\t\t}\n");
		source.append("\t\t\t\t}\n");
		source.append("\t\t\t\treturn false;\n");
		source.append("\t\t\t} else if (\"AnyElementsOrNone\".equals(type)) {\n");
		source.append("\t\t\t\tif (template_selection != template_sel.SPECIFIC_VALUE) {\n");
		source.append("\t\t\t\t\treturn false;\n");
		source.append("\t\t\t\t}\n");
		source.append("\t\t\t\tfinal int elements_size = value_elements.size();\n");
		source.append("\t\t\t\tfor (int i = 0; i < elements_size; i++) {\n");
		source.append("\t\t\t\t\tif (value_elements.get(i).get_selection() == template_sel.ANY_OR_OMIT) {\n");
		source.append("\t\t\t\t\t\treturn true;\n");
		source.append("\t\t\t\t\t}\n");
		source.append("\t\t\t\t}\n");
		source.append("\t\t\t\treturn false;\n");
		source.append("\t\t\t} else if (\"permutation\".equals(type)) {\n");
		if (isSetOf) {
			source.append("\t\t\t\treturn false;\n");
		} else {
			source.append("\t\t\t\treturn get_number_of_permutations() != 0;\n");
		}
		source.append("\t\t\t} else if (\"length\".equals(type)) {\n");
		source.append("\t\t\t\treturn length_restriction_type != length_restriction_type_t.NO_LENGTH_RESTRICTION;\n");
		source.append("\t\t\t} else {\n");
		source.append("\t\t\t\treturn super.get_istemplate_kind(type);\n");
		source.append("\t\t\t}\n");
		source.append("\t\t}\n\n");
	}

	/**
	 * Generate check_restriction function for template
	 *
	 * @param source
	 *                where the source code is to be generated.
	 * @param displayName
	 *                the user readable name of the type to be generated.
	 */
	private static void generateTemplateCheckRestriction(final StringBuilder source, final String displayName) {
		source.append("\t\t@Override\n");
		source.append("\t\tpublic void check_restriction(final template_res restriction, final String name, final boolean legacy) {\n");
		source.append("\t\t\tif (template_selection==template_sel.UNINITIALIZED_TEMPLATE) {\n");
		source.append("\t\t\t\treturn;\n");
		source.append("\t\t\t}\n");
		source.append("\t\t\tswitch ((name != null && (restriction==template_res.TR_VALUE)) ? template_res.TR_OMIT : restriction) {\n");
		source.append("\t\t\tcase TR_OMIT:\n");
		source.append("\t\t\t\tif (template_selection==template_sel.OMIT_VALUE) {\n");
		source.append("\t\t\t\t\treturn;\n");
		source.append("\t\t\t\t}\n");
		source.append("\t\t\t\t// no break\n");
		source.append("\t\t\tcase TR_VALUE: {\n");
		source.append("\t\t\t\tif (template_selection!=template_sel.SPECIFIC_VALUE || is_ifPresent) {\n");
		source.append("\t\t\t\t\tbreak;\n");
		source.append("\t\t\t\t}\n");
		source.append("\t\t\t\tfinal int elements_size = value_elements.size();\n");
		source.append("\t\t\t\tfor (int i = 0; i < elements_size; i++) {\n");
		source.append("\t\t\t\t\tvalue_elements.get(i).check_restriction(restriction, name == null ? \""+displayName+"\" : name, false);\n");
		source.append("\t\t\t\t}\n");
		source.append("\t\t\t\treturn;\n");
		source.append("\t\t\t}\n");
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

	public static String getPreGenBasedNameValue(final JavaGenData aData, final StringBuilder source, final String ofTypeName, final boolean isSetOf,
			final boolean optimized_memalloc) {
		aData.addBuiltinTypeImport("PreGenRecordOf");

		return MessageFormat.format("PreGenRecordOf.PREGEN__{0}__OF__{1}{2}", isSetOf ? "SET" : "RECORD", ofTypeName, optimized_memalloc ? "__OPTIMIZED" : "");
	}

	public static String getPreGenBasedNameTemplate(final JavaGenData aData, final StringBuilder source, final String ofTypeName, final boolean isSetOf,
			final boolean optimized_memalloc) {
		aData.addBuiltinTypeImport("PreGenRecordOf");

		return MessageFormat.format("PreGenRecordOf.PREGEN__{0}__OF__{1}{2}_template", isSetOf ? "SET" : "RECORD", ofTypeName, optimized_memalloc ? "__OPTIMIZED" : "");
	}

	/**
	 * Generate "record of/set of" class
	 *
	 * @param aData
	 *                used to access build settings.
	 * @param source
	 *                where the source code is to be generated.
	 * @param genName
	 *                the name of the generated class representing the
	 *                "record of/set of" type.
	 * @param displayName
	 *                the user readable name of the type to be generated.
	 * @param ofTypeName
	 *                type name of the "record of/set of" element
	 * @param ofTypeGenName
	 *                type generated name of the "record of/set of" element
	 * @param isSetOf
	 *                {@code true}: set of, {@code false}: record of
	 * @param optimized_memalloc
	 *                {@code true}: build on the optimized class,
	 *                {@code false}: use the base version
	 */
	public static void generatePreGenBasedValueClass( final JavaGenData aData,
								final StringBuilder source,
								final String genName,
								final String displayName,
								final String ofTypeName,
								final String ofTypeGenName,
								final boolean isSetOf,
								final boolean optimized_memalloc) {
		aData.addBuiltinTypeImport("PreGenRecordOf");
		aData.addBuiltinTypeImport("TitanNull_Type");
		aData.addBuiltinTypeImport("Optional");

		source.append(MessageFormat.format("\tpublic static class {0} extends PreGenRecordOf.PREGEN__{1}__OF__{2}{3} '{'\n", genName, isSetOf ? "SET" : "RECORD", ofTypeName, optimized_memalloc ? "__OPTIMIZED" : ""));
		source.append(MessageFormat.format("\t\tpublic {0}() '{'\n", genName));
		source.append("\t\t\tsuper();\n");
		source.append("\t\t}\n");
		source.append(MessageFormat.format("\t\tpublic {0}(final {0} other_value) '{'\n", genName));
		source.append("\t\t\tsuper(other_value);\n");
		source.append("\t\t}\n");
		source.append(MessageFormat.format("\t\tpublic {0}(final TitanNull_Type other_value) '{'\n", genName));
		source.append("\t\t\tsuper(other_value);\n");
		source.append("\t\t}\n");
		source.append(MessageFormat.format("\t\tpublic {0}(final PreGenRecordOf.PREGEN__{1}__OF__{2}{3} other_value) '{'\n", genName, isSetOf ? "SET" : "RECORD", ofTypeName, optimized_memalloc ? "__OPTIMIZED" : ""));
		source.append("\t\t\tsuper(other_value);\n");
		source.append("\t\t}\n");
		source.append("\t\t@Override\n");
		source.append(MessageFormat.format("\t\tpublic {0} operator_concatenate(PreGenRecordOf.PREGEN__{1}__OF__{2}{3} other_value) '{'\n", genName, isSetOf ? "SET" : "RECORD", ofTypeName, optimized_memalloc ? "__OPTIMIZED" : ""));
		source.append(MessageFormat.format("\t\t\tmust_bound(\"Unbound operand of {0} concatenation.\");\n", displayName));
		source.append(MessageFormat.format("\t\t\tother_value.must_bound(\"Unbound operand of {0} concatenation.\");\n", displayName));
		source.append(MessageFormat.format("\t\t\tfinal {0} ret_val = new {0}(TitanNull_Type.NULL_VALUE);\n", genName));
		source.append("\t\t\tfinal int elements_size = valueElements.size();\n");
		source.append("\t\t\tfor (int i=0; i < elements_size; i++) {\n");
		source.append(MessageFormat.format("\t\t\t\tfinal {0} elem = valueElements.get(i);\n", ofTypeGenName));
		source.append("\t\t\t\tif (elem != null) {\n");
		source.append(MessageFormat.format("\t\t\t\t\tret_val.valueElements.add(new {0}(elem));\n", ofTypeGenName));
		source.append("\t\t\t\t}\n");
		source.append("\t\t\t}\n");
		source.append("\t\t\tfinal int other_size = other_value.lengthof().get_int();\n");
		source.append("\t\t\tfor (int i = 0; i < other_size; i++) {\n");
		source.append(MessageFormat.format("\t\t\t\tfinal {0} elem = other_value.get_at(i);\n", ofTypeGenName));
		source.append("\t\t\t\tif (elem != null) {\n");
		source.append(MessageFormat.format("\t\t\t\t\tret_val.valueElements.add(new {0}(elem));\n", ofTypeGenName));
		source.append("\t\t\t\t}\n");
		source.append("\t\t\t}\n");
		source.append("\t\t\treturn ret_val;\n");
		source.append("\t\t}\n");
		source.append("\t}\n");
	}

	/**
	 * Generate "record of/set of" template class
	 *
	 * @param aData
	 *                used to access build settings.
	 * @param source
	 *                where the source code is to be generated.
	 * @param genName
	 *                the name of the generated class representing the
	 *                "record of/set of" type.
	 * @param displayName
	 *                the user readable name of the type to be generated.
	 * @param ofTypeName
	 *                type name of the "record of/set of" element
	 * @param ofTypeGenName
	 *                type generated name of the "record of/set of" element
	 * @param isSetOf
	 *                {@code true}: set of, {@code false}: record of
	 * @param optimized_memalloc
	 *                {@code true}: build on the optimized class,
	 *                {@code false}: use the base version
	 */
	public static void generatePreGenBasedTemplateClass( final JavaGenData aData,
								final StringBuilder source,
								final String genName,
								final String displayName,
								final String ofTypeName,
								final String ofTypeGenName,
								final boolean isSetOf,
								final boolean optimized_memalloc) {
		aData.addImport("java.util.ArrayList");

		aData.addBuiltinTypeImport("PreGenRecordOf");
		aData.addBuiltinTypeImport("TitanNull_Type");
		aData.addBuiltinTypeImport("Optional");
		aData.addBuiltinTypeImport("TtcnError");

		source.append(MessageFormat.format("\tpublic static class {0}_template extends PreGenRecordOf.PREGEN__{1}__OF__{2}{3}_template '{'\n", genName, isSetOf ? "SET" : "RECORD", ofTypeName, optimized_memalloc ? "__OPTIMIZED" : ""));
		source.append(MessageFormat.format("\t\tpublic {0}_template() '{'\n", genName));
		source.append("\t\t\tsuper();\n");
		source.append("\t\t}\n");
		source.append(MessageFormat.format("\t\tpublic {0}_template(final {0} other_value) '{'\n", genName));
		source.append("\t\t\tsuper(other_value);\n");
		source.append("\t\t}\n");
		source.append(MessageFormat.format("\t\tpublic {0}_template(final {0}_template other_template) '{'\n", genName));
		source.append("\t\t\tsuper(other_template);\n");
		source.append("\t\t}\n");
		source.append(MessageFormat.format("\t\tpublic {0}_template(final template_sel other_template) '{'\n", genName));
		source.append("\t\t\tsuper(other_template);\n");
		source.append("\t\t}\n");
		source.append(MessageFormat.format("\t\tpublic {0}_template(final TitanNull_Type other_template) '{'\n", genName));
		source.append("\t\t\tsuper(other_template);\n");
		source.append("\t\t}\n");
		source.append(MessageFormat.format("\t\tpublic {0}_template(final PreGenRecordOf.PREGEN__{1}__OF__{2}{3}_template other_template) '{'\n", genName, isSetOf ? "SET" : "RECORD", ofTypeName, optimized_memalloc ? "__OPTIMIZED" : ""));
		source.append("\t\t\tsuper(other_template);\n");
		source.append("\t\t}\n");

		source.append(MessageFormat.format("\t\tpublic {0}_template( final Optional<{0}> other_value ) '{'\n", genName));
		source.append("\t\t\tswitch (other_value.get_selection()) {\n");
		source.append("\t\t\tcase OPTIONAL_PRESENT:\n");
		source.append("\t\t\t\tcopy_value(other_value.constGet());\n");
		source.append("\t\t\t\tbreak;\n");
		source.append("\t\t\tcase OPTIONAL_OMIT:\n");
		source.append("\t\t\t\tset_selection(template_sel.OMIT_VALUE);\n");
		source.append("\t\t\t\tbreak;\n");
		source.append("\t\t\tdefault:\n");
		source.append(MessageFormat.format("\t\t\t\tthrow new TtcnError(\"Creating a template of type {0} from an unbound optional field.\");\n", displayName));
		source.append("\t\t\t}\n");
		source.append("\t\t}\n\n");

		source.append("\t\t@Override\n");
		source.append("\t\tpublic void set_type(final template_sel template_type, final int list_length) {\n");
		source.append("\t\t\tclean_up();\n");
		source.append("\t\t\tswitch (template_type) {\n");
		source.append("\t\t\tcase VALUE_LIST:\n");
		source.append("\t\t\tcase COMPLEMENTED_LIST:\n");
		source.append(MessageFormat.format("\t\t\t\tlist_value = new ArrayList<PreGenRecordOf.PREGEN__{0}__OF__{1}{2}_template>( list_length );\n", isSetOf ? "SET" : "RECORD", ofTypeName, optimized_memalloc ? "__OPTIMIZED" : ""));
		source.append("\t\t\t\tfor (int list_count = 0; list_count < list_length; list_count++) {\n");
		source.append(MessageFormat.format("\t\t\t\t\tlist_value.add( new {0}_template() );\n", genName));
		source.append("\t\t\t\t}\n");
		source.append("\t\t\t\tbreak;\n");
		if ( isSetOf ) {
			source.append("\t\t\tcase SUPERSET_MATCH:\n");
			source.append("\t\t\tcase SUBSET_MATCH:\n");
			source.append( MessageFormat.format( "\t\t\t\tset_items = new ArrayList<{0}>(list_length);\n", ofTypeGenName ) );
			source.append("\t\t\t\tfor( int i = 0; i < list_length; i++ ) {\n");
			source.append( MessageFormat.format( "\t\t\t\t\tset_items.add( new {0}() );\n", ofTypeGenName ) );
			source.append("\t\t\t\t}\n");
			source.append("\t\t\t\tbreak;\n");
		}
		source.append("\t\t\tdefault:\n");
		source.append(MessageFormat.format("\t\t\t\tthrow new TtcnError(\"Internal error: Setting an invalid type for a template of type {0}.\");\n", displayName));
		source.append("\t\t\t}\n");
		source.append("\t\t\tset_selection(template_type);\n");
		source.append("\t\t}\n");

		source.append("\t\t@Override\n");
		source.append(MessageFormat.format("\t\tpublic {0}_template list_item(final int list_index) '{'\n", genName));
		source.append(MessageFormat.format("\t\t\treturn ({0}_template)super.list_item(list_index);\n", genName));
		source.append("\t\t}\n");

		source.append("\t\t@Override\n");
		source.append(MessageFormat.format("\t\tpublic {0} valueof() '{'\n", genName));
		source.append("\t\t\tif (template_selection != template_sel.SPECIFIC_VALUE || is_ifPresent) {\n");
		source.append(MessageFormat.format("\t\t\t\tthrow new TtcnError(\"Performing a valueof or send operation on a non-specific template of type {0}.\");\n", displayName));
		source.append("\t\t\t}\n");
		source.append(MessageFormat.format("\t\t\tfinal {0} ret_val = new {0}(TitanNull_Type.NULL_VALUE);\n", genName));
		source.append("\t\t\tint i = 0;\n");
		source.append("\t\t\tfinal int elements_size = value_elements.size();\n");
		source.append("\t\t\tfor (int elem_count = 0; elem_count < elements_size; elem_count++) {\n");
		source.append(MessageFormat.format("\t\t\t\tfinal {0} temp = value_elements.get(elem_count);\n", ofTypeName));
		source.append("\t\t\t\tif (temp.is_bound()) {\n");
		source.append("\t\t\t\t\tret_val.get_at(i).operator_assign(temp.valueof());\n");
		source.append("\t\t\t\t\ti++;\n");
		source.append("\t\t\t\t}\n");
		source.append("\t\t\t}\n");
		source.append("\t\t\treturn ret_val;\n");
		source.append("\t\t\t}\n");
		source.append("\t\t}\n");
	}
}
