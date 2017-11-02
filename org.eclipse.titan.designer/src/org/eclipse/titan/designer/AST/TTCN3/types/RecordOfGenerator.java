/******************************************************************************
 * Copyright (c) 2000-2017 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.types;

import java.text.MessageFormat;

import org.eclipse.titan.designer.compiler.JavaGenData;

/**
 * Utility class for generating the value and template classes for "record of/set of" types.
 *
 * @author Arpad Lovassy
 */
public class RecordOfGenerator {

	private RecordOfGenerator() {
		// private to disable instantiation
	}

	/**
	 * Generate "record of/set of" class
	 * @param aData only used to update imports if needed.
	 * @param source where the source code is to be generated.
	 * @param genName the name of the generated class representing the "record of/set of" type.
	 * @param displayName the user readable name of the type to be generated.
	 * @param ofTypeName type name of the "record of/set of" element
	 * @param isSetOf true: set of, false: record of
	 */
	public static void generateValueClass( final JavaGenData aData,
										   final StringBuilder source,
										   final String genName,
										   final String displayName,
										   final String ofTypeName,
										   final boolean isSetOf ) {
		aData.addImport("java.util.List");
		aData.addBuiltinTypeImport("Base_Type");
		aData.addBuiltinTypeImport("TtcnError");
		aData.addBuiltinTypeImport("TitanInteger");
		aData.addBuiltinTypeImport("TitanNull_Type");
		aData.addBuiltinTypeImport("AdditionalFunctions");
		aData.addBuiltinTypeImport("RecordOfMatch");
		aData.addBuiltinTypeImport("RecordOfMatch.compare_function_t");
		source.append(MessageFormat.format("public static class {0} extends Base_Type '{'\n", genName));

		generateValueDeclaration( source, genName, ofTypeName, isSetOf );
		generateValueConstructors( source, genName, ofTypeName, displayName );
		generateValueCopyList( source, ofTypeName );
		generateValueIsPresent( source );
		generateValueIsBound( source );
		generateValueIsValue(source, ofTypeName);
		generateValueOperatorEquals( source, genName, ofTypeName, displayName, isSetOf );
		generateValueAssign( source, genName, ofTypeName, displayName);
		generateValueConcatenate( source, genName, ofTypeName, displayName );
		generateValueRotate( source, genName, ofTypeName, displayName );
		generateValueCleanup( source );
		generateValueGetterSetters( source, ofTypeName, displayName );
		generateValueGetUnboundElem( source, ofTypeName );
		generateValueToString( source );
		generateValueReplace( source, genName, ofTypeName, displayName );
		generateValueLog( source );

		source.append("}\n");
	}

	/**
	 * Generate "record of/set of" template class
	 * @param aData only used to update imports if needed.
	 * @param source where the source code is to be generated.
	 * @param genName the name of the generated class representing the "record of/set of" type.
	 * @param displayName the user readable name of the type to be generated.
	 * @param ofTypeName type name of the "record of/set of" element
	 * @param isSetOf true: set of, false: record of
	 */
	public static void generateTemplateClass( final JavaGenData aData,
											  final StringBuilder source,
											  final String genName,
											  final String displayName,
											  final String ofTypeName,
											  final boolean isSetOf ) {
		aData.addImport("java.util.List");
		aData.addImport("java.util.ArrayList");
		aData.addImport("java.util.concurrent.atomic.AtomicBoolean");
		aData.addImport("java.util.concurrent.atomic.AtomicInteger");
		aData.addImport("java.text.MessageFormat");
		aData.addBuiltinTypeImport("Base_Template");
		aData.addBuiltinTypeImport("Record_Of_Template");
		aData.addBuiltinTypeImport("TitanInteger");
		aData.addBuiltinTypeImport("TtcnError");
		aData.addBuiltinTypeImport("RecordOfMatch");
		aData.addBuiltinTypeImport("RecordOfMatch.match_function_t");
		aData.addBuiltinTypeImport("Restricted_Length_Template");
		aData.addBuiltinTypeImport("Optional");
		if ( isSetOf ) {
			aData.addBuiltinTypeImport("RecordOfMatch.log_function_t");
		}

		source.append( MessageFormat.format( "public static class {0}_template extends Record_Of_Template '{'\n", genName ) );

		generateTemplateDeclaration( source, genName, ofTypeName );
		if ( isSetOf ) {
			generateTemplateDeclarationSetOf( source, genName, ofTypeName );
		}
		generateTemplateConstructors( source, genName, ofTypeName, displayName );
		generateTemplateCopyTemplate( source, genName, ofTypeName, displayName, isSetOf );
		generateTemplateIsPresent( source );
		generateTemplateMatch( source, genName, displayName, isSetOf );
		generateTemplateMatchOmit( source );
		generateTemplateAssign( source, genName, displayName );
		generateTemplateCleanup( source );
		generateTemplateReplace( source, genName, displayName );
		generateTemplateGetterSetters( source, genName, ofTypeName, displayName );
		if ( isSetOf ) {
			generateTemplateGetterSettersSetOf( source, genName, ofTypeName, displayName );
		}
		generateTemplateConcat( source, genName, ofTypeName, displayName );
		generateTemplateSetSize( source, genName, ofTypeName, displayName, isSetOf );
		generateTemplateNElem( source, genName );
		generateTemplateMatchv( source, genName );
		generateTemplateIsValue( source, genName );
		generateTemplateSetType( source, genName, ofTypeName, displayName, isSetOf );
		generateTemplateListItem( source, genName, displayName );
		generateTemplateGetListItem( source, genName, displayName );
		generateTemplateValueOf( source, genName, displayName );
		generateTemplateSubstr( source, genName );
		generateTemplateLog( source, genName, displayName, isSetOf );
		generateTemplateGetIstemplateKind( source, genName );
		//TODO: use
		//generateTemplateCheckRestriction( source, displayName );
		source.append("}\n");
	}

	/**
	 * Generate member variables
	 *
	 * @param source where the source code is to be generated.
	 * @param genName the name of the generated class representing the "record of/set of" type.
	 * @param ofTypeName type name of the "record of/set of" element
	 * @param isSetOf true: set of, false: record of
	 */
	private static void generateValueDeclaration( final StringBuilder source, final String genName, final String ofTypeName,
												  final boolean isSetOf ) {
		source.append('\n');
		source.append( MessageFormat.format( "\tprivate List<{0}> valueElements;\n", ofTypeName ) );

		if ( isSetOf ) {
			source.append('\n');
			source.append("\tprivate compare_function_t compare_function_set = new compare_function_t() {\n");
			source.append("\t\t@Override\n");
			source.append("\t\tpublic boolean compare(Base_Type left_ptr, int left_index, Base_Type right_ptr, int right_index) {\n");
			source.append( MessageFormat.format( "\t\t\treturn compare_set(({0})left_ptr, left_index, ({0})right_ptr, right_index);\n", genName ) );
			source.append("\t\t}\n");
			source.append("\t};\n");
		}
	}

	/**
	 * Generate constructors
	 *
	 * @param source where the source code is to be generated.
	 * @param genName the name of the generated class representing the "record of/set of" type.
	 * @param ofTypeName type name of the "record of/set of" element
	 * @param displayName the user readable name of the type to be generated.
	 */
	private static void generateValueConstructors( final StringBuilder source, final String genName, final String ofTypeName, final String displayName) {
		source.append('\n');
		source.append( MessageFormat.format( "\tpublic {0}() '{'\n", genName ) );
		source.append("\t}\n");

		source.append('\n');
		source.append( MessageFormat.format( "\tpublic {0}( final {0} otherValue ) '{'\n", genName ) );
		source.append( MessageFormat.format("\t\totherValue.mustBound(\"Copying an unbound value of type {0}.\");\n", displayName ) );
		source.append("\t\tvalueElements = copyList( otherValue.valueElements );\n");
		source.append("\t}\n");

		source.append('\n');
		source.append( MessageFormat.format( "\tpublic {0}(TitanNull_Type nullValue) '{'\n", genName ) );
		source.append( MessageFormat.format( "\t\tvalueElements = new ArrayList<{0}>();\n", ofTypeName ) );
		source.append("\t}\n");
	}

	/**
	 * Generate the copyList function
	 *
	 * @param source where the source code is to be generated.
	 * @param ofTypeName type name of the "record of/set of" element
	 */
	private static void generateValueCopyList( final StringBuilder source, final String ofTypeName ) {
		source.append('\n');
		source.append( MessageFormat.format( "\tprivate static final List<{0}> copyList( final List<{0}> srcList ) '{'\n", ofTypeName ) );
		source.append("\t\tif ( srcList == null ) {\n");
		source.append("\t\t\treturn null;\n");
		source.append("\t\t}\n");
		source.append('\n');
		source.append( MessageFormat.format( "\t\tfinal List<{0}> newList = new ArrayList<{0}>( srcList.size() );\n", ofTypeName ) );
		source.append( MessageFormat.format( "\t\tfor ({0} srcElem : srcList) '{'\n", ofTypeName ) );
		source.append( MessageFormat.format( "\t\t\t{0} newElem = getUnboundElem();\n", ofTypeName ) );
		source.append("\t\t\tif (srcElem.isBound()) {\n");
		source.append("\t\t\t\tnewElem.assign( srcElem );\n");
		source.append("\t\t\t}\n");
		source.append("\t\t\tnewList.add( ( newElem ) );\n");
		source.append("\t\t}\n");
		source.append("\t\treturn newList;\n");
		source.append("\t}\n");
	}

	/**
	 * Generate the isPresent function
	 *
	 * @param source where the source code is to be generated.
	 */
	private static void generateValueIsPresent(final StringBuilder source) {
		source.append('\n');
		source.append("\t@Override\n");
		source.append("\tpublic boolean isPresent() {\n");
		source.append("\t\treturn isBound();\n");
		source.append("\t}\n");
	}

	/**
	 * Generate the isBound function
	 *
	 * @param source where the source code is to be generated.
	 */
	private static void generateValueIsBound(final StringBuilder source) {
		source.append('\n');
		source.append("\t@Override\n");
		source.append("\tpublic boolean isBound() {\n");
		source.append("\t\treturn valueElements != null;\n");
		source.append("\t}\n");
		source.append('\n');
		source.append("\tpublic void mustBound( final String aErrorMessage ) {\n");
		source.append("\t\tif ( !isBound() ) {\n");
		source.append("\t\t\tthrow new TtcnError( aErrorMessage );\n");
		source.append("\t\t}\n");
		source.append("\t}\n");
	}

	/**
	 * Generate the isValue function
	 *
	 * @param source where the source code is to be generated.
	 * @param ofTypeName type name of the "record of/set of" element
	 */
	private static void generateValueIsValue(final StringBuilder source, final String ofTypeName) {
		source.append('\n');
		source.append("\t@Override\n");
		source.append("\tpublic boolean isValue() {\n");
		source.append("\t\tif (valueElements == null) {\n");
		source.append("\t\t\treturn false;\n");
		source.append("\t\t}\n");
		source.append("\t\tfor (int i=0; i < valueElements.size(); i++) {\n");
		source.append( MessageFormat.format( "\t\t\tfinal {0} elem = valueElements.get(i);\n", ofTypeName ) );
		source.append("\t\t\tif (elem == null || !elem.isValue()) {\n");
		source.append("\t\t\t\treturn false;\n");
		source.append("\t\t\t}\n");
		source.append("\t\t}\n");
		source.append("\t\treturn true;\n");
		source.append("\t}\n");
		source.append('\n');
	}

	/**
	 * Generate assignment operators
	 *
	 * @param source where the source code is to be generated.
	 * @param genName the name of the generated class representing the "record of/set of" type.
	 * @param ofTypeName type name of the "record of/set of" element
	 * @param displayName the user readable name of the type to be generated.
	 * @param isSetOf true: set of, false: record of
	 */
	private static void generateValueOperatorEquals( final StringBuilder source, final String genName, final String ofTypeName,
													 final String displayName, final boolean isSetOf ) {
		source.append('\n');
		source.append("\t@Override\n");
		source.append("\tpublic boolean operatorEquals(Base_Type otherValue) {\n");
		source.append( MessageFormat.format( "\t\tif (otherValue instanceof {0}) '{'\n", genName) );
		source.append( MessageFormat.format( "\t\t\treturn operatorEquals(({0})otherValue);\n", genName) );
		source.append("\t\t}\n");
		source.append( MessageFormat.format( "\t\tthrow new TtcnError(\"Internal Error: The left operand of comparison is not of type {0}.\");\n", genName ) );
		source.append("\t}\n");
		source.append('\n');

		source.append("\t//originally operator==\n");
		source.append( MessageFormat.format( "\tpublic boolean operatorEquals( final {0} otherValue ) '{'\n", genName ) );
		source.append( MessageFormat.format( "\t\tmustBound(\"The left operand of comparison is an unbound value of type {0}.\");\n", displayName ) );
		source.append( MessageFormat.format( "\t\totherValue.mustBound(\"The right operand of comparison is an unbound value of type {0}.\");\n", displayName ) );
		source.append('\n');
		if ( isSetOf ) {
			source.append("\t\treturn RecordOfMatch.compare_set_of(otherValue, otherValue.valueElements.size(), this, valueElements.size(), compare_function_set);\n");
		} else {
			source.append("\t\tfinal int size = valueElements.size();\n");
			source.append("\t\tif ( size != otherValue.valueElements.size() ) {\n");
			source.append("\t\t\treturn false;\n");
			source.append("\t\t}\n");
			source.append('\n');
			source.append("\t\tfor ( int i = 0; i < size; i++ ) {\n");
			source.append( MessageFormat.format( "\t\t\tfinal {0} leftElem = valueElements.get( i );\n", ofTypeName ) );
			source.append( MessageFormat.format( "\t\t\tfinal {0} rightElem = otherValue.valueElements.get( i );\n", ofTypeName ) );
			source.append("\t\t\tif (leftElem.isBound()) {\n");
			source.append("\t\t\t\tif (rightElem.isBound()) {\n");
			source.append("\t\t\t\t\tif ( !leftElem.operatorEquals( rightElem ) ) {\n");
			source.append("\t\t\t\t\t\treturn false;\n");
			source.append("\t\t\t\t\t}\n");
			source.append("\t\t\t\t} else {\n");
			source.append("\t\t\t\t\treturn false;\n");
			source.append("\t\t\t\t}\n");
			source.append("\t\t\t} else if (rightElem.isBound()) {\n");
			source.append("\t\t\t\treturn false;\n");
			source.append("\t\t\t}\n");
			source.append("\t\t}\n");
			source.append('\n');
			source.append("\t\treturn true;\n");
		}
		source.append("\t}\n");

		if ( isSetOf ) {
			source.append( MessageFormat.format( "\tprivate boolean compare_set({0} left_ptr, int left_index, {0} right_ptr, int right_index) '{'\n", genName ) );
			source.append("\t\tif (left_ptr.valueElements == null) {\n");
			source.append( MessageFormat.format( "\t\t\tthrow new TtcnError(\"The left operand of comparison is an unbound value of type {0}.\");\n", displayName ) );
			source.append("\t\t}\n");
			source.append("\t\tif (right_ptr.valueElements == null) {\n");
			source.append( MessageFormat.format( "\t\t\tthrow new TtcnError(\"The right operand of comparison is an unbound value of type {0}.\");\n", displayName ) );
			source.append("\t\t}\n");
			source.append("\t\tif (left_ptr.valueElements.get(left_index) != null) {\n");
			source.append("\t\t\tif (right_ptr.valueElements.get(right_index) != null){\n");
			source.append("\t\t\t\treturn left_ptr.valueElements.get(left_index).operatorEquals( right_ptr.valueElements.get(right_index) );\n");
			source.append("\t\t\t} else return false;\n");
			source.append("\t\t} else {\n");
			source.append("\t\t\treturn right_ptr.valueElements.get(right_index) == null;\n");
			source.append("\t\t}\n");
			source.append("\t}\n");
		}
	}

	/**
	 * Generate assign functions
	 *
	 * @param source where the source code is to be generated.
	 * @param genName the name of the generated class representing the "record of/set of" type.
	 * @param ofTypeName type name of the "record of/set of" element
	 * @param displayName the user readable name of the type to be generated.
	 */
	private static void generateValueAssign( final StringBuilder source, final String genName, final String ofTypeName, final String displayName ) {
		source.append('\n');
		source.append("\t@Override\n");
		source.append( MessageFormat.format( "\tpublic {0} assign(final Base_Type otherValue) '{'\n", genName ) );
		source.append( MessageFormat.format( "\tif (otherValue instanceof {0}) '{'\n", genName) );
		source.append( MessageFormat.format( "\t\treturn assign(({0})otherValue);\n", genName) );
		source.append("\t}\n\n");
		source.append( MessageFormat.format( "\t\tthrow new TtcnError(\"Internal Error: The left operand of assignment is not of type {0}.\");\n", genName ) );
		source.append("\t}\n");

		source.append('\n');
		source.append("\t//originally operator=\n");
		source.append( MessageFormat.format( "\tpublic {0} assign( final {0} aOtherValue ) '{'\n", genName ) );
		source.append( MessageFormat.format("\t\taOtherValue.mustBound( \"Assigning an unbound value of type {0}.\" );\n", displayName));
		source.append('\n');
		source.append("\t\tvalueElements = copyList( aOtherValue.valueElements );\n");
		source.append("\t\treturn this;\n");
		source.append("\t}\n");

		source.append('\n');
		source.append( MessageFormat.format( "\tpublic {0} assign(TitanNull_Type nullValue) '{'\n", genName ) );
		source.append( MessageFormat.format( "\t\tvalueElements = new ArrayList<{0}>();\n", ofTypeName ) );
		source.append("\t\treturn this;\n");
		source.append("\t}\n");
	}

	/**
	 * Generate concatenate function
	 *
	 * @param source where the source code is to be generated.
	 * @param genName the name of the generated class representing the "record of/set of" type.
	 * @param ofTypeName type name of the "record of/set of" element
	 * @param displayName the user readable name of the type to be generated.
	 */
	private static void generateValueConcatenate( final StringBuilder source, final String genName, final String ofTypeName, final String displayName ) {
		source.append('\n');
		source.append("\t//originally operator+\n");
		source.append( MessageFormat.format( "\tpublic {0} concatenate(final {0} other_value) '{'\n", genName ) );
		source.append("\t\tif (valueElements == null || other_value.valueElements == null) {\n");
		source.append( MessageFormat.format( "\t\t\tthrow new TtcnError(\"Unbound operand of {0} concatenation.\");\n", displayName ) );
		source.append("\t\t}\n");
		source.append( MessageFormat.format( "\t\t{0} ret_val = new {0}(TitanNull_Type.NULL_VALUE);\n", genName ) );
		source.append("\t\tfor (int i=0; i < valueElements.size(); i++) {\n");
		source.append( MessageFormat.format( "\t\t\tfinal {0} elem = valueElements.get(i);\n", ofTypeName ) );
		source.append("\t\t\tif (elem != null) {\n");
		source.append( MessageFormat.format( "\t\t\t\tret_val.valueElements.add(new {0}(elem));\n", ofTypeName ) );
		source.append("\t\t\t}\n");
		source.append("\t\t}\n");
		source.append("\t\tfor (int i = 0; i < other_value.valueElements.size(); i++) {\n");
		source.append( MessageFormat.format( "\t\t\tfinal {0} elem = other_value.valueElements.get(i);\n", ofTypeName ) );
		source.append("\t\t\tif (elem != null) {\n");
		source.append( MessageFormat.format( "\t\t\t\tret_val.valueElements.add(new {0}(elem));\n", ofTypeName ) );
		source.append("\t\t\t}\n");
		source.append("\t\t}\n");
		source.append("\t\treturn ret_val;\n");
		source.append("\t}\n");

		source.append('\n');
		source.append( MessageFormat.format( "\tpublic {0} concatenate(final TitanNull_Type nullValue) '{'\n", genName ) );
		source.append( MessageFormat.format( "\t\t{0} ret_val = new {0}(this);\n", genName ) );
		source.append("\t\treturn ret_val;\n");
		source.append("\t}\n");
	}

	/**
	 * Generate rotate functions
	 *
	 * @param source where the source code is to be generated.
	 * @param genName the name of the generated class representing the "record of/set of" type.
	 * @param ofTypeName type name of the "record of/set of" element
	 * @param displayName the user readable name of the type to be generated.
	 */
	private static void generateValueRotate( final StringBuilder source, final String genName, final String ofTypeName, final String displayName ) {
		source.append('\n');
		source.append("\t//originally operator<<=\n");
		source.append( MessageFormat.format( "\tpublic {0} rotateLeft(final TitanInteger rotate_count) '{'\n", genName ) );
		source.append("\t\trotate_count.mustBound(\"Unbound integer operand of rotate left operator.\");\n");
		source.append("\t\treturn rotateLeft(rotate_count.getInt());\n");
		source.append("\t}\n");

		source.append('\n');
		source.append("\t//originally operator<<=\n");
		source.append( MessageFormat.format( "\tpublic {0} rotateLeft(final int rotate_count) '{'\n", genName ) );
		source.append("\t\treturn rotateRight(-rotate_count);\n");
		source.append("\t}\n");

		source.append('\n');
		source.append("\t//originally operator>>=\n");
		source.append( MessageFormat.format( "\tpublic {0} rotateRight(final TitanInteger rotate_count) '{'\n", genName ) );
		source.append("\t\trotate_count.mustBound(\"Unbound integer operand of rotate right operator.\");\n");
		source.append("\t\treturn rotateRight(rotate_count.getInt());\n");
		source.append("\t}\n");

		source.append('\n');
		source.append("\t//originally operator>>=\n");
		source.append( MessageFormat.format( "\tpublic {0} rotateRight(final int rotate_count) '{'\n", genName ) );
		source.append("\t\tif (valueElements == null) {\n");
		source.append( MessageFormat.format( "\t\t	throw new TtcnError(\"Performing rotation operation on an unbound value of type {0}.\");\n", displayName ) );
		source.append("\t\t}\n");
		source.append("\t\tfinal int size = valueElements.size();\n");
		source.append("\t\tif (size == 0) {\n");
		source.append( MessageFormat.format( "\t\t\treturn new {0}(TitanNull_Type.NULL_VALUE);\n", genName ) );
		source.append("\t\t}\n");
		source.append("\t\tint rc;\n");
		source.append("\t\tif (rotate_count >= 0) {\n");
		source.append("\t\t\trc = rotate_count % size;\n");
		source.append("\t\t} else {\n");
		source.append("\t\t\trc = size - ((-rotate_count) % size);\n");
		source.append("\t\t}\n");
		source.append("\t\tif (rc == 0) {\n");
		source.append( MessageFormat.format( "\t\t\treturn new {0}(this);\n", genName ) );
		source.append("\t\t}\n");
		source.append( MessageFormat.format( "\t\t{0} ret_val = new {0}(TitanNull_Type.NULL_VALUE);\n", genName ) );
		source.append("\t\tfor (int i = size - rc; i < size; i++) {\n");
		source.append( MessageFormat.format( "\t\t\tfinal {0} elem = valueElements.get(i);\n", ofTypeName ) );
		source.append("\t\t\tif (elem != null) {\n");
		source.append( MessageFormat.format( "\t\t\t\tret_val.valueElements.add(new {0}(elem));\n", ofTypeName ) );
		source.append("\t\t\t}\n");
		source.append("\t\t}\n");
		source.append("\t\tfor (int i = 0; i < size - rc; i++) {\n");
		source.append( MessageFormat.format( "\t\t\tfinal {0} elem = valueElements.get(i);\n", ofTypeName ) );
		source.append("\t\t\tif (elem != null) {\n");
		source.append( MessageFormat.format( "\t\t\t\tret_val.valueElements.add(new {0}(elem));\n", ofTypeName ) );
		source.append("\t\t\t}\n");
		source.append("\t\t}\n");
		source.append("\t\treturn ret_val;\n");
		source.append("\t}\n");
	}

	/**
	 * Generate the clean_up function
	 *
	 * @param source where the source code is to be generated.
	 */
	private static void generateValueCleanup(final StringBuilder source) {
		source.append('\n');
		source.append("\t//originally clean_up\n");
		source.append("\tpublic void cleanUp() {\n");
		source.append("\t\tvalueElements = null;\n");
		source.append("\t}\n");
	}

	/**
	 * Generate getter and setter functions
	 * @param source where the source code is to be generated.
	 * @param ofTypeName type name of the "record of/set of" element
	 * @param displayName the user readable name of the type to be generated.
	 */
	private static void generateValueGetterSetters(final StringBuilder source, final String ofTypeName , final String displayName) {
		source.append('\n');
		source.append("\t//originally get_at(int)\n");
		source.append( MessageFormat.format("\tpublic {0} getAt( final int index_value ) '{'\n", ofTypeName ) );
		source.append("\t\tif (index_value < 0) {\n");
		source.append( MessageFormat.format( "\t\t\tthrow new TtcnError( \"Accessing an element of type {0} using a negative index: \"+index_value+\".\");\n", displayName ) );
		source.append("\t\t}\n");
		source.append('\n');
		source.append("\t\tif (valueElements == null || index_value >= valueElements.size() ) {\n");
		source.append("\t\t\t//increase list size\n");
		source.append("\t\t\tsetSize(index_value + 1);\n");
		source.append("\t\t}\n");
		source.append('\n');
		source.append("\t\tif ( valueElements.get( index_value ) == null ) {\n");
		source.append( MessageFormat.format( "\t\t\t{0} newElem = getUnboundElem();\n", ofTypeName ) );
		source.append("\t\t\tvalueElements.set( index_value, newElem );\n");
		source.append("\t\t}\n");
		source.append("\t\treturn valueElements.get( index_value );\n");
		source.append("\t}\n");

		source.append('\n');
		source.append("\t//originally get_at(const INTEGER&)\n");
		source.append( MessageFormat.format("\tpublic {0} getAt(final TitanInteger index_value) '{'\n", ofTypeName ) );
		source.append( MessageFormat.format( "\t\tindex_value.mustBound( \"Using an unbound integer value for indexing a value of type {0}.\" );\n", displayName ) );
		source.append("\t\treturn getAt( index_value.getInt() );\n");
		source.append("\t}\n");

		source.append('\n');
		source.append("\t//originally get_at(int) const\n");
		source.append( MessageFormat.format("\tpublic {0} constGetAt( final int index_value ) '{'\n", ofTypeName ) );
		source.append("\t\tif ( !isBound() ) {\n");
		source.append( MessageFormat.format( "\t\t\tthrow new TtcnError( \"Accessing an element in an unbound value of type {0}.\" );\n", displayName ) );
		source.append("\t\t}\n");
		source.append("\t\tif (index_value < 0) {\n");
		source.append( MessageFormat.format( "\t\t\tthrow new TtcnError( \"Accessing an element of type {0} using a negative index: \"+index_value+\".\");\n", displayName ) );
		source.append("\t\t}\n");
		source.append("\t\tfinal int nofElements = n_elem().getInt();\n");
		source.append("\t\tif ( index_value >= nofElements ) {\n");
		source.append( MessageFormat.format( "\t\t\tthrow new TtcnError( \"Index overflow in a value of type {0}: The index is \"+index_value+\", but the value has only \"+nofElements+\" elements.\" );\n", displayName ) );
		source.append("\t\t}\n");
		source.append('\n');
		source.append( MessageFormat.format( "\t\tfinal {0} elem = valueElements.get( index_value );\n", ofTypeName ) );
		source.append("\t\treturn ( elem != null ) ? elem : getUnboundElem();\n");
		source.append("\t}\n");

		source.append('\n');
		source.append("\t//originally get_at(const INTEGER&) const\n");
		source.append( MessageFormat.format( "\tpublic {0} constGetAt(final TitanInteger index_value) '{'\n", ofTypeName ) );
		source.append( MessageFormat.format( "\t\tindex_value.mustBound( \"Using an unbound integer value for indexing a value of type {0}.\" );\n", displayName ) );
		source.append("\t\treturn constGetAt( index_value.getInt() );\n");
		source.append("\t}\n");

		source.append('\n');
		source.append("\tpublic TitanInteger sizeOf() {\n");
		source.append( MessageFormat.format( "\t\tmustBound(\"Performing sizeof operation on an unbound value of type {0}.\");\n", displayName ) );
		source.append("\t\treturn new TitanInteger(valueElements.size());\n");
		source.append("\t}\n");

		source.append('\n');
		source.append("\tpublic TitanInteger n_elem() {\n");
		source.append("\t\treturn sizeOf();\n");
		source.append("\t}\n");

		source.append('\n');
		source.append("\tpublic TitanInteger lengthOf() {\n");
		source.append( MessageFormat.format( "\t\tmustBound(\"Performing lengthof operation on an unbound value of type {0}.\");\n", displayName ) );
		source.append("\t\tfor ( int i = valueElements.size() - 1; i >= 0; i-- ) {\n");
		source.append( MessageFormat.format( "\t\t\t{0} elem = valueElements.get( i );\n", ofTypeName ) );
		source.append("\t\t\tif ( elem != null && elem.isBound() ) {\n");
		source.append("\t\t\t\treturn new TitanInteger(i + 1);\n");
		source.append("\t\t\t}\n");
		source.append("\t\t}\n");
		source.append("\t\treturn new TitanInteger(0);\n");
		source.append("\t}\n");

		source.append('\n');
		source.append("\tpublic void setSize(final int newSize) {\n");
		source.append("\t\tif (newSize < 0) {\n");
		source.append( MessageFormat.format( "\t\t\tthrow new TtcnError(\"Internal error: Setting a negative size for a value of type {0}.\");\n", displayName ) );
		source.append("\t\t}\n");
		source.append("\t\tif ( valueElements == null ) {\n");
		source.append( MessageFormat.format( "\t\t\tvalueElements = new ArrayList<{0}>();\n", ofTypeName ) );
		source.append("\t\t}\n");
		source.append("\t\tif (newSize > valueElements.size()) {\n");
		source.append("\t\t\tfor ( int i = valueElements.size(); i < newSize; i++ ) {\n");
		source.append( MessageFormat.format( "\t\t\t\tvalueElements.add( new {0}() );\n", ofTypeName ) );
		source.append("\t\t\t}\n");
		source.append("\t\t} else if (newSize < valueElements.size()) {\n");
		source.append("\t\t\twhile(valueElements.size() > newSize) {\n");
		source.append("\t\t\t\tvalueElements.remove(valueElements.size()-1);\n");
		source.append("\t\t\t}\n");
		source.append("\t\t}\n");
		source.append("\t}\n");
	}

	private static void generateValueGetUnboundElem(final StringBuilder source, final String ofTypeName) {
		source.append('\n');
		source.append( MessageFormat.format( "\tprivate static {0} getUnboundElem() '{'\n", ofTypeName ) );
		source.append( MessageFormat.format( "\t\treturn new {0}();\n", ofTypeName ) );
		source.append("\t}\n");
	}

	private static void generateValueToString(final StringBuilder source) {
		source.append('\n');
		source.append("\t@Override\n");
		source.append("\tpublic String toString() {\n");
		source.append("\t\tif (!isBound()) {\n");
		source.append("\t\t\treturn \"<unbound>\";\n");
		source.append("\t\t}\n");
		source.append("\t\tfinal StringBuilder sb = new StringBuilder();\n");
		source.append("\t\tsb.append('{');\n");
		source.append("\t\tfinal int size = ( valueElements != null ) ? valueElements.size() : 0;\n");
		source.append("\t\tfor (int i = 0; i < size; i++ ) {\n");
		source.append("\t\t\tif ( i > 0 ) {\n");
		source.append("\t\t\t\tsb.append(',');\n");
		source.append("\t\t\t}\n");
		source.append("\t\t\tsb.append(valueElements.get(i).toString());\n");
		source.append("\t\t}\n");
		source.append("\t\tsb.append('}');\n");
		source.append("\t\treturn sb.toString();\n");
		source.append("\t}\n");
	}

	/**
	 * Generate substr() and replace()
	 *
	 * @param source where the source code is to be generated.
	 * @param genName the name of the generated class representing the "record of/set of" type.
	 * @param ofTypeName type name of the "record of/set of" element
	 * @param displayName the user readable name of the type to be generated.
	 */
	private static void generateValueReplace( final StringBuilder source, final String genName, final String ofTypeName, final String displayName) {
		source.append('\n');
		source.append( MessageFormat.format( "\tpublic {0} substr(int index, int returncount) '{'\n", genName ) );
		source.append("\t\tif (valueElements == null) {\n");
		source.append( MessageFormat.format( "\t\t\tthrow new TtcnError(\"The first argument of substr() is an unbound value of type {0}.\");\n", displayName ) );
		source.append("\t\t}\n");
		source.append( MessageFormat.format( "\t\tAdditionalFunctions.check_substr_arguments(valueElements.size(), index, returncount, \"{0}\",\"element\");\n", displayName ) );
		source.append( MessageFormat.format( "\t\t{0} ret_val = new {0}(TitanNull_Type.NULL_VALUE);\n", genName ) );
		source.append("\t\tfor (int i=0; i<returncount; i++) {\n");
		source.append("\t\t\tif (valueElements.get(i+index) != null) {\n");
		source.append( MessageFormat.format( "\t\t\t\tret_val.valueElements.add(new {0}(valueElements.get(i+index)));\n", ofTypeName ) );
		source.append("\t\t\t}\n");
		source.append("\t\t}\n");
		source.append("\t\treturn ret_val;\n");
		source.append("\t}\n");

		source.append('\n');
		source.append( MessageFormat.format( "\tpublic {0} replace(int index, int len, final {0} repl) '{'\n", genName ) );
		source.append("\t\tif (valueElements == null) {\n");
		source.append( MessageFormat.format( "\t\t\tthrow new TtcnError(\"The first argument of replace() is an unbound value of type {0}.\");\n", displayName ) );
		source.append("\t\t}\n");
		source.append("\t\tif (repl.valueElements == null) {\n");
		source.append( MessageFormat.format( "\t\t\tthrow new TtcnError(\"The fourth argument of replace() is an unbound value of type {0}.\");\n", displayName ) );
		source.append("\t\t}\n");
		source.append( MessageFormat.format( "\t\tAdditionalFunctions.check_replace_arguments(valueElements.size(), index, len, \"{0}\",\"element\");\n", displayName ) );
		source.append( MessageFormat.format( "\t\t{0} ret_val = new {0}(TitanNull_Type.NULL_VALUE);\n", genName ) );
		source.append("\t\tfor (int i = 0; i < index; i++) {\n");
		source.append( MessageFormat.format( "\t\t\tfinal {0} elem = valueElements.get(i);\n", ofTypeName ) );
		source.append("\t\t\tif (elem != null) {\n");
		source.append( MessageFormat.format( "\t\t\t\tret_val.valueElements.add(new {0}(elem));\n", ofTypeName ) );
		source.append("\t\t\t}\n");
		source.append("\t\t}\n");
		source.append("\t\tfor (int i = 0; i < repl.valueElements.size(); i++) {\n");
		source.append( MessageFormat.format( "\t\t\tfinal {0} elem = repl.valueElements.get(i);\n", ofTypeName ) );
		source.append("\t\t\tif (elem != null) {\n");
		source.append( MessageFormat.format( "\t\t\t\tret_val.valueElements.add(new {0}(elem));\n", ofTypeName ) );
		source.append("\t\t\t}\n");
		source.append("\t\t}\n");
		source.append("\t\tfor (int i = 0; i < valueElements.size() - index - len; i++) {\n");
		source.append( MessageFormat.format( "\t\t\tfinal {0} elem = valueElements.get(index + i + len);\n", ofTypeName ) );
		source.append("\t\t\tif (elem != null) {\n");
		source.append( MessageFormat.format( "\t\t\t\tret_val.valueElements.add(new {0}(elem));\n", ofTypeName ) );
		source.append("\t\t\t}\n");
		source.append("\t\t}\n");
		source.append("\t\treturn ret_val;\n");
		source.append("\t}\n");

		source.append('\n');
		source.append( MessageFormat.format( "\tpublic {0} replace(int index, int len, final {0}_template repl) '{'\n", genName ) );
		source.append("\t\tif (!repl.isValue()) {\n");
		source.append("\t\t\tthrow new TtcnError(\"The fourth argument of function replace() is a template with non-specific value.\");\n");
		source.append("\t\t}\n");
		source.append("\t\treturn replace(index, len, repl.valueOf());\n");
		source.append("\t}\n");
	}

	/**
	 * Generate log()
	 *
	 * @param source where the source code is to be generated.
	 */
	private static void generateValueLog(final StringBuilder source) {
		source.append("\tpublic void log() {\n");
		source.append("\t\tif (valueElements == null) {\n");
		source.append("\t\t\tTtcnLogger.log_event_unbound();\n");
		source.append("\t\t\treturn;\n");
		source.append("\t\t}\n");
		source.append("\t\tTtcnLogger.log_event_str(\"{ \");\n");
		source.append("\t\tfinal int size = valueElements.size();\n");
		source.append("\t\tfor (int i = 0; i < size; i++ ) {\n");
		source.append("\t\t\tif ( i > 0 ) {\n");
		source.append("\t\t\t\tTtcnLogger.log_event_str(\", \");\n");
		source.append("\t\t\t}\n");
		source.append("\t\tvalueElements.get(i).log();\n");
		source.append("\t\t}\n");
		source.append("\t\tTtcnLogger.log_event_str(\" }\");\n");
		source.append("\t}\n");
	}

	/**
	 * Generate member variables for template
	 *
	 * @param source where the source code is to be generated.
	 * @param genName the name of the generated class representing the "record of/set of" type.
	 * @param ofTypeName type name of the "record of/set of" element
	 */
	private static void generateTemplateDeclaration( final StringBuilder source, final String genName, final String ofTypeName ) {
		source.append('\n');

		source.append("\t//originally single_value/value_elements\n");
		source.append( MessageFormat.format( "\tList<{0}> value_elements;\n", ofTypeName ) );
		source.append('\n');

		source.append("\t//originally value_list/list_value\n");
		source.append( MessageFormat.format( "\tList<{0}_template> list_value;\n", genName ) );

		source.append('\n');
		source.append("\tprivate match_function_t match_function_specific = new match_function_t() {\n");
		source.append("\t\t@Override\n");
		source.append("\t\tpublic boolean match(Base_Type value_ptr, int value_index, Restricted_Length_Template template_ptr, int template_index, boolean legacy) {\n");
		source.append( MessageFormat.format( "\t\t\treturn match_index(({0})value_ptr, value_index, ({0}_template)template_ptr, template_index, legacy);\n", genName ) );
		source.append("\t\t}\n");
		source.append("\t};\n");
	}

	/**
	 * Generate member variables for template
	 * ONLY for set of
	 *
	 * @param source where the source code is to be generated.
	 * @param genName the name of the generated class representing the "record of/set of" type.
	 * @param ofTypeName type name of the "record of/set of" element
	 */
	private static void generateTemplateDeclarationSetOf( final StringBuilder source, final String genName, final String ofTypeName ) {

		source.append('\n');
		source.append("\t//ONLY for set of\n");
		source.append("\t//originally value_set/set_items\n");
		source.append( MessageFormat.format( "\tList<{0}> set_items;\n", ofTypeName ) );

		source.append('\n');
		source.append("\tprivate match_function_t match_function_set = new match_function_t() {\n");
		source.append("\t\t@Override\n");
		source.append("\t\tpublic boolean match(Base_Type value_ptr, int value_index, Restricted_Length_Template template_ptr, int template_index, boolean legacy) {\n");
		source.append( MessageFormat.format( "\t\t\treturn match_set(({0})value_ptr, value_index, ({0}_template)template_ptr, template_index, legacy);\n", genName ) );
		source.append("\t\t}\n");
		source.append("\t};\n");

		source.append('\n');
		source.append("\tprivate log_function_t log_function = new log_function_t() {\n");
		source.append("\t\t@Override\n");
		source.append("\t\tpublic void log(Base_Type value_ptr, Restricted_Length_Template template_ptr, int index_value, int index_template, boolean legacy) {\n");
		source.append("\t\t\tif (value_ptr != null && template_ptr != null) {\n");
		source.append( MessageFormat.format( "\t\t\t(({0}_template)template_ptr).value_elements.get(index_template).log_match((({0})value_ptr).constGetAt(index_value), legacy);\n", genName ) );
		source.append("\t\t\t} else if (value_ptr != null) {\n");
		source.append( MessageFormat.format( "\t\t\t\t(({0})value_ptr).constGetAt(index_value).log();\n", genName ) );
		source.append("\t\t\t} else if (template_ptr != null) {\n");
		source.append( MessageFormat.format( "\t\t\t\t(({0}_template)template_ptr).value_elements.get(index_template).log();\n", genName ) );
		source.append("\t\t\t}\n");
		source.append("\t\t}\n");
		source.append("\t};\n");
	}

	/**
	 * Generate constructors for template
	 *
	 * @param source where the source code is to be generated.
	 * @param genName the name of the generated class representing the "record of/set of" type.
	 * @param ofTypeName type name of the "record of/set of" element
	 * @param displayName the user readable name of the type to be generated.
	 */
	private static void generateTemplateConstructors( final StringBuilder source, final String genName, final String ofTypeName, final String displayName ) {
		source.append('\n');
		source.append( MessageFormat.format( "\tpublic {0}_template() '{'\n", genName ) );
		source.append("\t}\n");

		source.append('\n');
		source.append( MessageFormat.format( "\tpublic {0}_template(final template_sel other_value ) '{'\n", genName));
		source.append("\t\tsuper( other_value );\n");
		source.append("\t\tcheckSingleSelection( other_value );\n");
		source.append("\t}\n");

		source.append('\n');
		source.append( MessageFormat.format( "\tpublic {0}_template( final {0} otherValue ) '{'\n", genName ) );
		source.append("\t\tcopy_value( otherValue );\n");
		source.append("\t}\n");

		source.append('\n');
		source.append( MessageFormat.format( "\tpublic {0}_template( final {0}_template otherValue ) '{'\n", genName ) );
		source.append("\t\tcopyTemplate( otherValue );\n");
		source.append("\t}\n");

		source.append('\n');
		source.append( MessageFormat.format( "\tpublic {0}_template( final Optional<{0}> other_value ) '{'\n", genName ) );
		source.append("\t\tswitch (other_value.getSelection()) {\n");
		source.append("\t\tcase OPTIONAL_PRESENT:\n");
		source.append("\t\t\tcopy_value(other_value.constGet());\n");
		source.append("\t\t\tbreak;\n");
		source.append("\t\tcase OPTIONAL_OMIT:\n");
		source.append("\t\t\tsetSelection(template_sel.OMIT_VALUE);\n");
		source.append("\t\t\tbreak;\n");
		source.append("\t\tdefault:\n");
		source.append( MessageFormat.format( "\t\t\tthrow new TtcnError(\"Creating a template of type {0} from an unbound optional field.\");\n", displayName ) );
		source.append("\t\t}\n");
		source.append("\t}\n");

		source.append('\n');
		source.append( MessageFormat.format( "\tpublic {0}_template( final TitanNull_Type nullValue ) '{'\n", genName ) );
		source.append("\t\tsuper( template_sel.SPECIFIC_VALUE );\n");
		source.append( MessageFormat.format( "\t\tvalue_elements = new ArrayList<{0}>();\n", ofTypeName ) );
		source.append("\t}\n");
	}

	/**
	 * Generate the copyTemplate function for template
	 *
	 * @param source where the source code is to be generated.
	 * @param genName the name of the generated class representing the "record of/set of" type.
	 * @param ofTypeName type name of the "record of/set of" element
	 * @param displayName the user readable name of the type to be generated.
	 * @param isSetOf true: set of, false: record of
	 */
	private static void generateTemplateCopyTemplate( final StringBuilder source, final String genName, final String ofTypeName,
													  final String displayName, final boolean isSetOf ) {

		source.append('\n');
		source.append( MessageFormat.format( "\tprivate void copy_value(final {0} other_value) '{'\n", genName ) );
		source.append("\t\tif (!other_value.isBound()) {\n");
		source.append( MessageFormat.format( "\t\t\tthrow new TtcnError(\"Initialization of a template of type {0} with an unbound value.\");\n", displayName ) );
		source.append("\t\t}\n");
		source.append( MessageFormat.format( "\t\tvalue_elements = new ArrayList<{0}>();\n", ofTypeName ) );
		source.append("\t\tfinal int otherSize = other_value.valueElements.size();\n");
		source.append("\t\tfor (int elem_count = 0; elem_count < otherSize; elem_count++) {\n");
		source.append("\t\t\tif (other_value.constGetAt(elem_count).isBound()) {\n");
		source.append( MessageFormat.format( "\t\t\t\tvalue_elements.add( new {0}(other_value.constGetAt(elem_count)) );\n", ofTypeName ) );
		source.append("\t\t\t} else {\n");
		source.append( MessageFormat.format( "\t\t\t\tvalue_elements.add( new {0}() );\n", ofTypeName ) );
		source.append("\t\t\t}\n");
		source.append("\t\t}\n");
		source.append("\t\tsetSelection(template_sel.SPECIFIC_VALUE);\n");
		source.append("\t}\n");

		source.append('\n');
		source.append( MessageFormat.format( "\tprivate void copyTemplate(final {0}_template other_value) '{'\n", genName));
		source.append("\t\tswitch (other_value.templateSelection) {\n");
		source.append("\t\tcase SPECIFIC_VALUE:\n");
		source.append( MessageFormat.format( "\t\t\tvalue_elements = new ArrayList<{0}>();\n", ofTypeName ) );
		source.append("\t\t\tfinal int otherSize = other_value.value_elements.size();\n");
		source.append("\t\t\tfor (int elem_count = 0; elem_count < otherSize; elem_count++) {\n");
		source.append("\t\t\t\tif (other_value.constGetAt(elem_count).isBound()) {\n");
		source.append( MessageFormat.format( "\t\t\t\t\tvalue_elements.add( new {0}(other_value.constGetAt(elem_count)) );\n", ofTypeName ) );
		source.append("\t\t\t\t} else {\n");
		source.append( MessageFormat.format( "\t\t\t\t\tvalue_elements.add( new {0}() );\n", ofTypeName ) );
		source.append("\t\t\t\t}\n");
		source.append("\t\t\t}\n");
		source.append("\t\t\tbreak;\n");
		source.append("\t\tcase OMIT_VALUE:\n");
		source.append("\t\tcase ANY_VALUE:\n");
		source.append("\t\tcase ANY_OR_OMIT:\n");
		source.append("\t\t\tbreak;\n");
		source.append("\t\tcase VALUE_LIST:\n");
		source.append("\t\tcase COMPLEMENTED_LIST:\n");
		source.append( MessageFormat.format( "\t\t\tlist_value = new ArrayList<{0}_template>(other_value.list_value.size());\n", genName));
		source.append("\t\t\tfor(int i = 0; i < other_value.list_value.size(); i++) {\n");
		source.append( MessageFormat.format( "\t\t\t\tfinal {0}_template temp = new {0}_template(other_value.list_value.get(i));\n", genName));
		source.append("\t\t\t\tlist_value.add(temp);\n");
		source.append("\t\t\t}\n");
		source.append("\t\t\tbreak;\n");
		if ( isSetOf ) {
			source.append("\t\tcase SUPERSET_MATCH:\n");
			source.append("\t\tcase SUBSET_MATCH:\n");
			source.append( MessageFormat.format( "\t\t\tset_items = new ArrayList<{0}>(other_value.set_items.size());\n", ofTypeName ) );
			source.append("\t\t\tfor (int set_count = 0; set_count < other_value.set_items.size(); set_count++) {\n");
			source.append( MessageFormat.format( "\t\t\t\tfinal {0} temp = new {0}(other_value.set_items.get(set_count));\n", ofTypeName ) );
			source.append("\t\t\t\tset_items.add(temp);\n");
			source.append("\t\t\t}\n");
			source.append("\t\t\tbreak;\n");
		}
		source.append("\t\tdefault:\n");
		source.append( MessageFormat.format( "\t\t\tthrow new TtcnError(\"Copying an uninitialized template of type {0}.\");\n", displayName));
		source.append("\t\t}\n");
		source.append("\t\tsetSelection(other_value);\n");
		source.append("\t}\n");
	}

	/**
	 * Generate the isPresent function for template
	 *
	 * @param source where the source code is to be generated.
	 */
	private static void generateTemplateIsPresent(final StringBuilder source) {
		source.append('\n');
		source.append("\tpublic boolean isPresent() {\n");
		source.append("\t\treturn isPresent(false);\n");
		source.append("\t}\n");

		source.append('\n');
		source.append("\tpublic boolean isPresent(final boolean legacy) {\n");
		source.append("\t\tif (templateSelection == template_sel.UNINITIALIZED_TEMPLATE) {\n");
		source.append("\t\t\treturn false;\n");
		source.append("\t\t}\n");
		source.append("\t\treturn !match_omit(legacy);\n");
		source.append("\t}\n");
	}

	/**
	 * Generate the match function
	 *
	 * @param source where the source code is to be generated.
	 * @param genName the name of the generated class representing the "record of/set of" type.
	 * @param displayName the user readable name of the type to be generated.
	 * @param isSetOf true: set of, false: record of
	 */
	private static void generateTemplateMatch( final StringBuilder source, final String genName, final String displayName, final boolean isSetOf ) {
		source.append('\n');
		source.append("\t// originally match\n");
		source.append( MessageFormat.format( "\tpublic boolean match(final {0} other_value) '{'\n", genName ) );
		source.append("\t\treturn match(other_value, false);\n");
		source.append("\t}\n");

		source.append('\n');
		source.append("\t// originally match\n");
		source.append( MessageFormat.format( "\tpublic boolean match(final {0} other_value, final boolean legacy) '{'\n", genName ) );
		source.append("\t\tif(!other_value.isBound()) {\n");
		source.append("\t\t\treturn false;\n");
		source.append("\t\t}\n");
		source.append("\t\tfinal int value_length = other_value.sizeOf().getInt();\n");
		source.append("\t\tif (!match_length(value_length)) {\n");
		source.append("\t\t\treturn false;\n");
		source.append("\t\t}\n");
		source.append("\t\tswitch (templateSelection) {\n");
		source.append("\t\tcase ANY_VALUE:\n");
		source.append("\t\tcase ANY_OR_OMIT:\n");
		source.append("\t\t\treturn true;\n");
		source.append("\t\tcase OMIT_VALUE:\n");
		source.append("\t\t\treturn false;\n");
		source.append("\t\tcase SPECIFIC_VALUE:\n");
		if ( isSetOf ) {
			source.append("\t\t\treturn RecordOfMatch.match_set_of(other_value, value_length, this, value_elements.size(), match_function_specific, legacy);\n");
		} else {
			source.append("\t\t\treturn RecordOfMatch.match_record_of(other_value, value_length, this, value_elements.size(), match_function_specific, legacy);\n");
		}
		source.append("\t\tcase VALUE_LIST:\n");
		source.append("\t\tcase COMPLEMENTED_LIST:\n");
		source.append("\t\t\tfor(int i = 0 ; i < list_value.size(); i++) {\n");
		source.append("\t\t\t\tif(list_value.get(i).match(other_value, legacy)) {\n");
		source.append("\t\t\t\t\treturn templateSelection == template_sel.VALUE_LIST;\n");
		source.append("\t\t\t\t}\n");
		source.append("\t\t\t}\n");
		source.append("\t\t\treturn templateSelection == template_sel.COMPLEMENTED_LIST;\n");
		if ( isSetOf ) {
			source.append("\t\tcase SUPERSET_MATCH:\n");
			source.append("\t\tcase SUBSET_MATCH:\n");
			source.append("\t\t\treturn RecordOfMatch.match_set_of(other_value, value_length, this, set_items.size(), match_function_set, legacy);\n");
		}
		source.append("\t\tdefault:\n");
		source.append( MessageFormat.format( "\t\t\tthrow new TtcnError(\"Matching with an uninitialized/unsupported template of type {0}.\");\n", displayName ) );
		source.append("\t\t}\n");
		source.append("\t}\n");

		source.append('\n');
		source.append( MessageFormat.format( "\tprivate boolean match_index(final {0} value_ptr, int value_index, {0}_template template_ptr, int template_index, boolean legacy) '{'\n", genName ) );
		source.append("\t\tif (value_index >= 0) {\n");
		source.append("\t\t\treturn template_ptr.value_elements.get(template_index).match(value_ptr.valueElements.get(value_index), legacy);\n");
		source.append("\t\t} else {\n");
		source.append("\t\t\treturn template_ptr.value_elements.get(template_index).is_any_or_omit();\n");
		source.append("\t\t}\n");
		source.append("\t}\n");

		if ( isSetOf ) {
			source.append('\n');
			source.append( MessageFormat.format( "\tprivate boolean match_set(final {0} value_ptr, int value_index, {0}_template template_ptr, int template_index, boolean legacy) '{'\n", genName ) );
			source.append("\t\tif (value_index >= 0) {\n");
			source.append("\t\t\treturn template_ptr.set_items.get(template_index).match(value_ptr.valueElements.get(value_index), legacy);\n");
			source.append("\t\t} else {\n");
			source.append("\t\t\treturn template_ptr.set_items.get(template_index).is_any_or_omit();\n");
			source.append("\t\t}\n");
			source.append("\t}\n");
		}

		source.append('\n');
		source.append("\t@Override\n");
		source.append( MessageFormat.format( "\tpublic boolean match(final Base_Type otherValue, final boolean legacy) '{'\n", genName ) );
		source.append( MessageFormat.format( "\t\tif (otherValue instanceof {0}) '{'\n", genName) );
		source.append( MessageFormat.format( "\t\t\treturn match(({0})otherValue, legacy);\n", genName) );
		source.append("\t\t}\n\n");
		source.append( MessageFormat.format( "\t\tthrow new TtcnError(\"Internal Error: The left operand of assignment is not of type {0}.\");\n", genName ) );
		source.append("\t}\n");
	}

	/**
	 * Generate the match_omit function
	 *
	 * @param source: where the source code is to be generated.
	 */
	private static void generateTemplateMatchOmit( final StringBuilder source ) {
		source.append('\n');
		source.append("\tpublic boolean match_omit(final boolean legacy) {\n");
		source.append("\t\tif (is_ifPresent) {\n");
		source.append("\t\t\treturn true;\n");
		source.append("\t\t}\n");
		source.append("\t\tswitch(templateSelection) {\n");
		source.append("\t\tcase OMIT_VALUE:\n");
		source.append("\t\tcase ANY_OR_OMIT:\n");
		source.append("\t\t\treturn true;\n");
		source.append("\t\tcase VALUE_LIST:\n");
		source.append("\t\tcase COMPLEMENTED_LIST:\n");
		source.append("\t\t\tif (legacy) {\n");
		source.append("\t\t\t\tfor (int i = 0 ; i < list_value.size(); i++) {\n");
		source.append("\t\t\t\t\tif (list_value.get(i).match_omit(legacy)) {\n");
		source.append("\t\t\t\t\t\treturn templateSelection == template_sel.VALUE_LIST;\n");
		source.append("\t\t\t\t\t}\n");
		source.append("\t\t\t\t}\n");
		source.append("\t\t\t\treturn templateSelection == template_sel.COMPLEMENTED_LIST;\n");
		source.append("\t\t\t}\n");
		source.append("\t\t\treturn false;\n");
		source.append("\t\tdefault:\n");
		source.append("\t\t\treturn false;\n");
		source.append("\t\t}\n");
		source.append("\t}\n");
	}

	/**
	 * Generate assign functions for template
	 *
	 * @param source where the source code is to be generated.
	 * @param genName the name of the generated class representing the "record of/set of" type.
	 * @param displayName the user readable name of the type to be generated.
	 */
	private static void generateTemplateAssign( final StringBuilder source, final String genName, final String displayName ) {
		source.append('\n');
		source.append("\t//originally operator=\n");
		source.append( MessageFormat.format( "\tpublic {0}_template assign( final template_sel other_value ) '{'\n", genName ) );
		source.append("\t\tcheckSingleSelection(other_value);\n");
		source.append("\t\tcleanUp();\n");
		source.append("\t\tsetSelection(other_value);\n");
		source.append("\t\treturn this;\n");
		source.append("\t}\n");

		source.append('\n');
		source.append("\t//originally operator=\n");
		source.append( MessageFormat.format( "\tpublic {0}_template assign( final {0} other_value ) '{'\n", genName ) );
		source.append("\t\tcleanUp();\n");
		source.append("\t\tcopy_value(other_value);\n");
		source.append("\t\treturn this;\n");
		source.append("\t}\n");

		source.append('\n');
		source.append("\t//originally operator=\n");
		source.append( MessageFormat.format( "\tpublic {0}_template assign( final {0}_template other_value ) '{'\n", genName ) );
		source.append("\t\tif (other_value != this) {\n");
		source.append("\t\t\tcleanUp();\n");
		source.append("\t\t\tcopyTemplate(other_value);\n");
		source.append("\t\t}\n");
		source.append("\t\treturn this;\n");
		source.append("\t}\n");

		source.append('\n');
		source.append("\t@Override\n");
		source.append( MessageFormat.format( "\tpublic {0}_template assign(final Base_Type otherValue) '{'\n", genName ) );
		source.append( MessageFormat.format( "\tif (otherValue instanceof {0}) '{'\n", genName) );
		source.append( MessageFormat.format( "\t\treturn assign(({0})otherValue);\n", genName) );
		source.append("\t}\n\n");
		source.append( MessageFormat.format( "\t\tthrow new TtcnError(\"Internal Error: The left operand of assignment is not of type {0}.\");\n", genName ) );
		source.append("\t}\n");

		source.append('\n');
		source.append("\t@Override\n");
		source.append( MessageFormat.format( "\tpublic {0}_template assign(final Base_Template otherValue) '{'\n", genName ) );
		source.append( MessageFormat.format( "\tif (otherValue instanceof {0}_template) '{'\n", genName) );
		source.append( MessageFormat.format( "\t\treturn assign(({0}_template)otherValue);\n", genName) );
		source.append("\t}\n\n");
		source.append( MessageFormat.format( "\t\tthrow new TtcnError(\"Internal Error: The left operand of assignment is not of type {0}_template.\");\n", genName ) );
		source.append("\t}\n");

		source.append('\n');
		source.append( MessageFormat.format( "\tpublic {0}_template assign( final Optional<{0}> other_value ) '{'\n", genName ) );
		source.append("\t\tcleanUp();\n");
		source.append("\t\tswitch (other_value.getSelection()) {\n");
		source.append("\t\tcase OPTIONAL_PRESENT:\n");
		source.append("\t\t\tcopy_value(other_value.constGet());\n");
		source.append("\t\t\tbreak;\n");
		source.append("\t\tcase OPTIONAL_OMIT:\n");
		source.append("\t\t\tsetSelection(template_sel.OMIT_VALUE);\n");
		source.append("\t\t\tbreak;\n");
		source.append("\t\tdefault:\n");
		source.append( MessageFormat.format( "\t\t\tthrow new TtcnError(\"Assignment of an unbound optional field to a template of type {0}.\");\n", displayName ) );
		source.append("\t\t}\n");
		source.append("\t\treturn this;\n");
		source.append("\t}\n");
	}

	/**
	 * Generate the clean_up function for template
	 *
	 * @param source where the source code is to be generated.
	 */
	private static void generateTemplateCleanup(final StringBuilder source) {
		source.append('\n');
		source.append("\t@Override\n");
		source.append("\tpublic void cleanUp() {\n");
		source.append("\t\tswitch(templateSelection) {\n");
		source.append("\t\tcase SPECIFIC_VALUE:\n");
		source.append("\t\t\tvalue_elements.clear();\n");
		source.append("\t\t\tvalue_elements = null;\n");
		source.append("\t\t\tbreak;\n");
		source.append("\t\tcase VALUE_LIST:\n");
		source.append("\t\tcase COMPLEMENTED_LIST:\n");
		source.append("\t\t\tlist_value.clear();\n");
		source.append("\t\t\tlist_value = null;\n");
		source.append("\t\t\tbreak;\n");
		source.append("\t\tdefault:\n");
		source.append("\t\t\tbreak;\n");
		source.append("\t\t}\n");
		source.append("\t\ttemplateSelection = template_sel.UNINITIALIZED_TEMPLATE;\n");
		source.append("\t}\n");
	}

	/**
	 * Generate replace functions for template
	 *
	 * @param source where the source code is to be generated.
	 * @param genName the name of the generated class representing the "record of/set of" type.
	 * @param displayName the user readable name of the type to be generated.
	 */
	private static void generateTemplateReplace(final StringBuilder source, final String genName, final String displayName) {
 		source.append('\n');
		source.append( MessageFormat.format( "\tpublic {0} replace(int index, int len, final {0}_template repl) '{'\n", genName ) );
		source.append("\t\tif (!isValue()) {\n");
		source.append("\t\t\tthrow new TtcnError(\"The first argument of function replace() is a template with non-specific value.\");\n");
		source.append("\t\t}\n");
		source.append("\t\tif (!repl.isValue()) {\n");
		source.append("\t\t\tthrow new TtcnError(\"The fourth argument of function replace() is a template with non-specific value.\");\n");
		source.append("\t\t}\n");
		source.append("\t\treturn valueOf().replace(index, len, repl.valueOf());\n");
		source.append("\t}\n");

 		source.append('\n');
		source.append( MessageFormat.format( "\tpublic {0} replace(int index, int len, final {0} repl) '{'\n", genName ) );
		source.append("\t\tif (!isValue()) {\n");
		source.append("\t\t\tthrow new TtcnError(\"The first argument of function replace() is a template with non-specific value.\");\n");
		source.append("\t\t}\n");
		source.append("\t\treturn valueOf().replace(index, len, repl);\n");
		source.append("\t}\n");
	}

	/**
	 * Generate getter and setter functions for template
	 *
	 * @param source where the source code is to be generated.
	 * @param genName the name of the generated class representing the "record of/set of" type.
	 * @param ofTypeName type name of the "record of/set of" element
	 * @param displayName the user readable name of the type to be generated.
	 */
	private static void generateTemplateGetterSetters(final StringBuilder source, final String genName, final String ofTypeName, final String displayName) {
		source.append('\n');
		source.append( MessageFormat.format( "\tpublic {0} getAt(int index_value) '{'\n", ofTypeName ) );
		source.append("\t\tif (index_value < 0) {\n");
		source.append("\t\t\tthrow new TtcnError( MessageFormat.format( \"Accessing an element of a template for type "+displayName+" using a negative index: {0}.\", index_value ) );\n");
		source.append("\t\t}\n");
		source.append('\n');
		source.append("\t\tswitch (templateSelection) {\n");
		source.append("\t\tcase SPECIFIC_VALUE:\n");
		source.append("\t\t\tif(index_value < value_elements.size()) break;\n");
		source.append("\t\t\t// no break\n");
		source.append("\t\tcase OMIT_VALUE:\n");
		source.append("\t\tcase ANY_VALUE:\n");
		source.append("\t\tcase ANY_OR_OMIT:\n");
		source.append("\t\tcase UNINITIALIZED_TEMPLATE:\n");
		source.append("\t\t\tsetSize(index_value + 1);\n");
		source.append("\t\t\tbreak;\n");
		source.append("\t\tdefault:\n");
		source.append( MessageFormat.format( "\t\t\tthrow new TtcnError(\"Accessing an element of a non-specific template for type {0}.\");\n", displayName ) );
		source.append("\t\t}\n");
		source.append("\t\treturn value_elements.get(index_value);\n");
		source.append("\t}\n");

		source.append('\n');
		source.append( MessageFormat.format( "\tpublic {0} getAt(final TitanInteger index_value) '{'\n", ofTypeName ) );
		source.append("\t\tif (!index_value.isBound()) {\n");
		source.append( MessageFormat.format( "\t\t\tthrow new TtcnError(\"Using an unbound integer value for indexing a template of type {0}.\");\n", displayName ) );
		source.append("\t\t}\n");
		source.append('\n');
		source.append("\t\treturn getAt(index_value.getInt());\n");
		source.append("\t}\n");

		source.append('\n');
		source.append( MessageFormat.format( "\tpublic {0} constGetAt(int index_value) '{'\n", ofTypeName ) );
		source.append("\t\tif (index_value < 0) {\n");
		source.append("\t\t\tthrow new TtcnError( MessageFormat.format( \"Accessing an element of a template for type "+displayName+" using a negative index: {0}.\", index_value ) );\n");
		source.append("\t\t}\n");
		source.append('\n');
		source.append("\t\tif (templateSelection != template_sel.SPECIFIC_VALUE) {\n");
		source.append( MessageFormat.format( "\t\t\tthrow new TtcnError(\"Accessing an element of a non-specific template for type {0}.\");\n", displayName ) );
		source.append("\t\t}\n");
		source.append('\n');
		source.append("\t\tif (index_value >= value_elements.size()) {\n");
		source.append("\t\t\tthrow new TtcnError( MessageFormat.format( \"Index overflow in a template of type "+displayName+": The index is {0}, but the template has only {1} elements.\", index_value, value_elements.size() ) );\n");
		source.append("\t\t}\n");
		source.append('\n');
		source.append("\t\treturn value_elements.get(index_value);\n");
		source.append("\t}\n");

		source.append('\n');
		source.append( MessageFormat.format( "\tpublic {0} constGetAt(final TitanInteger index_value) '{'\n", ofTypeName ) );
		source.append("\t\tif (!index_value.isBound()) {\n");
		source.append( MessageFormat.format( "\t\t\tthrow new TtcnError(\"Using an unbound integer value for indexing a template of type {0}.\");\n", displayName ) );
		source.append("\t\t}\n");
		source.append('\n');
		source.append("\t\treturn constGetAt(index_value.getInt());\n");
		source.append("\t}\n");
	}

	/**
	 * Generate getter and setter functions for template
	 * ONLY for set of
	 *
	 * @param source where the source code is to be generated.
	 * @param genName the name of the generated class representing the "record of/set of" type.
	 * @param ofTypeName type name of the "record of/set of" element
	 * @param displayName the user readable name of the type to be generated.
	 */
	private static void generateTemplateGetterSettersSetOf(final StringBuilder source, final String genName, final String ofTypeName, final String displayName) {
		source.append('\n');
		source.append( MessageFormat.format( "\tpublic {0} setItem(int set_index) '{'\n", ofTypeName ) );
		source.append("\t\tif (templateSelection != template_sel.SUPERSET_MATCH && templateSelection != template_sel.SUBSET_MATCH) {\n");
		source.append( MessageFormat.format( "\t\t\tthrow new TtcnError(\"Internal error: Accessing a set element of a non-set template of type {0}.\");\n", displayName ) );
		source.append("\t\t}\n");
		source.append("\t\tif (set_index >= set_items.size() ) {\n");
		source.append( MessageFormat.format( "\t\t\tthrow new TtcnError(\"Internal error: Index overflow in a set template of type {0}.\");\n", displayName ) );
		source.append("\t\t}\n");
		source.append("\t\treturn set_items.get(set_index);\n");
		source.append("\t}\n");
	}

	/**
	 * Generate concat functions for template
	 *
	 * @param source where the source code is to be generated.
	 * @param genName the name of the generated class representing the "record of/set of" type.
	 * @param ofTypeName type name of the "record of/set of" element
	 * @param displayName the user readable name of the type to be generated.
	 */
	private static void generateTemplateConcat(final StringBuilder source, final String genName, final String ofTypeName, final String displayName) {
		source.append('\n');
		source.append("\tprivate int get_length_for_concat(AtomicBoolean is_any_value) {\n");
		source.append("\t\tswitch (templateSelection) {\n");
		source.append("\t\tcase SPECIFIC_VALUE:\n");
		source.append("\t\t\treturn value_elements.size();\n");
		source.append("\t\tcase ANY_VALUE:\n");
		source.append("\t\tcase ANY_OR_OMIT:\n");
		source.append("\t\t\tswitch (length_restriction_type) {\n");
		source.append("\t\t\tcase NO_LENGTH_RESTRICTION:\n");
		source.append("\t\t\t\tif (templateSelection == template_sel.ANY_VALUE) {\n");
		source.append("\t\t\t\t\t// ? => { * }\n");
		source.append("\t\t\t\t\tis_any_value.set( true );\n");
		source.append("\t\t\t\t\treturn 1;\n");
		source.append("\t\t\t\t}\n");
		source.append("\t\t\t\tthrow new TtcnError(\"Operand of record of template concatenation is an AnyValueOrNone (*) matching mechanism with no length restriction\");\n");
		source.append("\t\t\tcase RANGE_LENGTH_RESTRICTION:\n");
		source.append("\t\t\t\tif (range_length_max_length == 0 || range_length_max_length != range_length_min_length) {\n");
		source.append("\t\t\t\t\tthrow new TtcnError( MessageFormat.format( \"Operand of record of template concatenation is an {0} matching mechanism with non-fixed length restriction\", templateSelection == template_sel.ANY_VALUE ? \"AnyValue (?)\" : \"AnyValueOrNone (*)\" ) );\n");
		source.append("\t\t\t\t}\n");
		source.append("\t\t\t\t// else fall through (range length restriction is allowed if the minimum\n");
		source.append("\t\t\t\t// and maximum value are the same)\n");
		source.append("\t\t\tcase SINGLE_LENGTH_RESTRICTION:\n");
		source.append("\t\t\t\t// ? length(N) or * length(N) => { ?, ?, ... ? } N times\n");
		source.append("\t\t\t\treturn length_restriction_type == length_restriction_type_t.SINGLE_LENGTH_RESTRICTION ? single_length : range_length_min_length;\n");
		source.append("\t\t\t}\n");
		source.append("\t\tdefault:\n");
		source.append("\t\t\tthrow new TtcnError(\"Operand of record of template concatenation is an uninitialized or unsupported template.\");\n");
		source.append("\t\t}\n");
		source.append("\t}\n");

		source.append('\n');
		source.append( MessageFormat.format( "\tprivate int get_length_for_concat(final {0} operand) '{'\n", genName ) );
		source.append("\t\toperand.mustBound(\"Operand of record of template concatenation is an unbound value.\");\n");
		source.append("\t\treturn operand.valueElements.size();\n");
		source.append("\t}\n");

		source.append('\n');
		source.append("\n\tprivate int get_length_for_concat(template_sel operand) {\n");
		source.append("\t\tif (operand == template_sel.ANY_VALUE) {\n");
		source.append("\t\t\t// ? => { * }\n");
		source.append("\t\t\treturn 1;\n");
		source.append("\t\t}\n");
		source.append("\t\tthrow new TtcnError(\"Operand of record of template concatenation is an uninitialized or unsupported template.\");\n");
		source.append("\t}\n");

		source.append('\n');
		//TODO: simplify, just use value_elements.add()
		source.append("\t\t\t//TODO: simplify, just use value_elements.add()\n");
		source.append( MessageFormat.format( "\tprivate void concat(AtomicInteger pos, final {0}_template operand) '{'\n", genName ) );
		source.append("\t\t// all errors should have already been caught by the operand's\n");
		source.append("\t\t// get_length_for_concat() call;\n");
		source.append("\t\t// the result template (this) should already be set to SPECIFIC_VALUE and\n");
		source.append("\t\t// single_value.value_elements should already be allocated\n");
		source.append("\t\tswitch (operand.templateSelection) {\n");
		source.append("\t\tcase SPECIFIC_VALUE:\n");
		source.append("\t\t\tfor (int i = 0; i < operand.value_elements.size(); ++i) {\n");
		source.append( MessageFormat.format( "\t\t\t\tvalue_elements.set( pos.get() + i, new {0}(operand.value_elements.get(i)) );\n", ofTypeName ) );
		source.append("\t\t\t}\n");
		source.append("\t\t\tpos.addAndGet( operand.value_elements.size() );\n");
		source.append("\t\t\tbreak;\n");
		source.append("\t\tcase ANY_VALUE:\n");
		source.append("\t\tcase ANY_OR_OMIT:\n");
		source.append("\t\t\tswitch (operand.length_restriction_type) {\n");
		source.append("\t\t\tcase NO_LENGTH_RESTRICTION:\n");
		source.append("\t\t\t\t// ? => { * }\n");
		source.append( MessageFormat.format( "\t\t\t\tvalue_elements.set( pos.get(), new {0}( template_sel.ANY_OR_OMIT ) );\n", ofTypeName ) );
		source.append("\t\t\t\tpos.incrementAndGet();\n");
		source.append("\t\t\t\tbreak;\n");
		source.append("\t\t\tcase RANGE_LENGTH_RESTRICTION:\n");
		source.append("\t\t\tcase SINGLE_LENGTH_RESTRICTION: {\n");
		source.append("\t\t\t\t// ? length(N) or * length(N) => { ?, ?, ... ? } N times\n");
		source.append("\t\t\t\tint N = operand.length_restriction_type == length_restriction_type_t.SINGLE_LENGTH_RESTRICTION ? operand.single_length : operand.range_length_min_length;\n");
		source.append("\t\t\t\tfor (int i = 0; i < N; ++i) {\n");
		source.append( MessageFormat.format( "\t\t\t\t\tvalue_elements.set( pos.get() + i, new {0}( template_sel.ANY_VALUE ) );\n", ofTypeName ) );
		source.append("\t\t\t\t}\n");
		source.append("\t\t\t\tpos.addAndGet( N );\n");
		source.append("\t\t\t\tbreak; }\n");
		source.append("\t\t	}\n");
		source.append("\t\tdefault:\n");
		source.append("\t\t	break;\n");
		source.append("\t\t}\n");
		source.append("\t}\n");

		//TODO: implement void concat(int& pos, const Record_Of_Type& operand)
		//TODO: implement void concat(int& pos)
	}

	/**
	 * Generate set_size and sizeof functions for template
	 *
	 * @param source where the source code is to be generated.
	 * @param genName the name of the generated class representing the "record of/set of" type.
	 * @param ofTypeName type name of the "record of/set of" element
	 * @param displayName the user readable name of the type to be generated.
	 * @param isSetOf true: set of, false: record of
	 */
	private static void generateTemplateSetSize( final StringBuilder source, final String genName, final String ofTypeName,
												 final String displayName, final boolean isSetOf ) {
		source.append('\n');
		source.append("\tpublic void setSize(int new_size) {\n");
		source.append("\t\tif (new_size < 0) {\n");
		source.append( MessageFormat.format( "\t\t\tthrow new TtcnError(\"Internal error: Setting a negative size for a template of type {0}.\");\n", displayName ) );
		source.append("\t\t}\n");
		source.append("\t\ttemplate_sel old_selection = templateSelection;\n");
		source.append("\t\tif (old_selection != template_sel.SPECIFIC_VALUE) {\n");
		source.append("\t\t\tcleanUp();\n");
		source.append("\t\t\tsetSelection(template_sel.SPECIFIC_VALUE);\n");
		source.append("\t\t\tvalue_elements = null;\n");
		source.append("\t\t}\n");
		source.append("\t\tif (value_elements == null) {\n");
		source.append( MessageFormat.format( "\t\t\tvalue_elements = new ArrayList<{0}>();\n", ofTypeName ) );
		source.append("\t\t}\n");
		source.append("\t\tif (new_size > value_elements.size()) {\n");
		source.append("\t\t\tif (old_selection == template_sel.ANY_VALUE || old_selection == template_sel.ANY_OR_OMIT) {\n");
		source.append("\t\t\t\tfor (int elem_count = value_elements.size(); elem_count < new_size; elem_count++) {\n");
		source.append( MessageFormat.format( "\t\t\t\t\tvalue_elements.add( new {0}(template_sel.ANY_VALUE) );\n", ofTypeName ) );
		source.append("\t\t\t\t}\n");
		source.append("\t\t\t} else {\n");
		source.append("\t\t\t\tfor (int elem_count = value_elements.size(); elem_count < new_size; elem_count++) {\n");
		source.append( MessageFormat.format( "\t\t\t\t\tvalue_elements.add( new {0}() );\n", ofTypeName ) );
		source.append("\t\t\t\t}\n");
		source.append("\t\t\t}\n");
		source.append("\t\t} else if (new_size < value_elements.size()) {\n");
		source.append("\t\t\tfinal int oldSize = value_elements.size();\n");
		source.append("\t\t\tfor (int elem_count = new_size; elem_count < oldSize; elem_count++) {\n");
		source.append("\t\t\t\tvalue_elements.remove( new_size );\n");
		source.append("\t\t\t}\n");
		source.append("\t\t}\n");
		source.append("\t}\n");

		source.append('\n');
		source.append("\tpublic TitanInteger sizeOf() {\n");
		source.append("\t\treturn sizeOf(true);\n");
		source.append("\t}\n");

		source.append('\n');
		source.append("\tpublic TitanInteger lengthOf() {\n");
		source.append("\t\treturn sizeOf(false);\n");
		source.append("\t}\n");

		source.append('\n');
		source.append("\tpublic TitanInteger sizeOf(boolean is_size) {\n");
		source.append("\t\tfinal String op_name = is_size ? \"size\" : \"length\";\n");
		source.append("\t\tint min_size;\n");
		source.append("\t\tboolean has_any_or_none;\n");
		source.append("\t\tif (is_ifPresent) {\n");
		source.append("\t\t\tthrow new TtcnError( MessageFormat.format( \"Performing {0}of() operation on a template of type "+displayName+" which has an ifpresent attribute.\", op_name ) );\n");
		source.append("\t\t}\n");
		source.append("\t\tswitch (templateSelection)\n");
		source.append("\t\t{\n");
		source.append("\t\tcase SPECIFIC_VALUE: {\n");
		source.append("\t\t\tmin_size = 0;\n");
		source.append("\t\t\thas_any_or_none = false;\n");
		source.append("\t\t\tint elem_count = value_elements.size();\n");
		source.append("\t\t\tif (!is_size) {\n");
		source.append("\t\t\t\twhile (elem_count>0 && !(value_elements.get(elem_count-1)).isBound())\n");
		source.append("\t\t\t\t\telem_count--;\n");
		source.append("\t\t\t}\n");
		source.append("\t\t\tfor (int i=0; i<elem_count; i++)\n");
		source.append("\t\t\t{\n");
		source.append("\t\t\tswitch (value_elements.get(i).getSelection())\n");
		source.append("\t\t\t\t{\n");
		source.append("\t\t\t\tcase OMIT_VALUE:\n");
		source.append("\t\t\t\t\tthrow new TtcnError( MessageFormat.format( \"Performing {0}of() operation on a template of type "+displayName+" containing omit element.\", op_name ) );\n");
		source.append("\t\t\t\tcase ANY_OR_OMIT:\n");
		source.append("\t\t\t\t\thas_any_or_none = true;\n");
		source.append("\t\t\t\t\tbreak;\n");
		source.append("\t\t\t\tdefault:\n");
		source.append("\t\t\t\t\tmin_size++;\n");
		source.append("\t\t\t\t\tbreak;\n");
		source.append("\t\t\t\t}\n");
		source.append("\t\t\t}\n");
		source.append("\t\t} break;\n");
		if ( isSetOf ) {
			source.append("\t\tcase SUPERSET_MATCH:\n");
			source.append("\t\tcase SUBSET_MATCH:\n");
			source.append("\t\t{\n");
			source.append("\t\t	min_size = 0;\n");
			source.append("\t\t	has_any_or_none = false;\n");
			source.append("\t\t	int elem_count = set_items.size();\n");
			source.append("\t\t	if (!is_size) {\n");
			source.append("\t\t		while (elem_count>0 && !set_items.get(elem_count-1).isBound()) {\n");
			source.append("\t\t			elem_count--;\n");
			source.append("\t\t		}\n");
			source.append("\t\t	}\n");
			source.append("\t\t	for (int i=0; i<elem_count; i++) {\n");
			source.append("\t\t		switch (set_items.get(i).getSelection()) {\n");
			source.append("\t\t		case OMIT_VALUE:\n");
			source.append("\t\t			throw new TtcnError( MessageFormat.format( \"Performing {0}of() operation on a template of type "+displayName+" containing omit element.\", op_name ) );\n");
			source.append("\t\t		case ANY_OR_OMIT:\n");
			source.append("\t\t			has_any_or_none = true;\n");
			source.append("\t\t			break;\n");
			source.append("\t\t		default:\n");
			source.append("\t\t			min_size++;\n");
			source.append("\t\t			break;\n");
			source.append("\t\t		}\n");
			source.append("\t\t	}\n");
			source.append("\t\t	if (templateSelection == template_sel.SUPERSET_MATCH) {\n");
			source.append("\t\t		has_any_or_none = true;\n");
			source.append("\t\t	} else {\n");
			source.append("\t\t		int max_size = min_size;\n");
			source.append("\t\t		min_size = 0;\n");
			source.append("\t\t		if (!has_any_or_none) { // [0,max_size]\n");
			source.append("\t\t			switch (length_restriction_type) {\n");
			source.append("\t\t			case NO_LENGTH_RESTRICTION:\n");
			source.append("\t\t				if (max_size==0) {\n");
			source.append("\t\t					return new TitanInteger(0);\n");
			source.append("\t\t				}\n");
			source.append("\t\t				throw new TtcnError( MessageFormat.format( \"Performing {0}of() operation on a template of type "+displayName+" with no exact size.\", op_name ) );\n");
			source.append("\t\t			case SINGLE_LENGTH_RESTRICTION:\n");
			source.append("\t\t				if (single_length <= max_size)\n");
			source.append("\t\t					return new TitanInteger(single_length);\n");
			source.append("\t\t				throw new TtcnError( MessageFormat.format( \"Performing {0}of() operation on an invalid template of type "+displayName+". The maximum size ({1}) contradicts the length restriction ({2}).\", op_name, max_size, single_length ) );\n");
			source.append("\t\t			case RANGE_LENGTH_RESTRICTION:\n");
			source.append("\t\t				if (max_size == range_length_min_length) {\n");
			source.append("\t\t					return new TitanInteger(max_size);\n");
			source.append("\t\t				} else if (max_size > range_length_min_length) {\n");
			source.append("\t\t					throw new TtcnError( MessageFormat.format( \"Performing {0}of() operation on a template of type "+displayName+" with no exact size.\", op_name ) );\n");
			source.append("\t\t				} else\n");
			source.append("\t\t					throw new TtcnError( MessageFormat.format( \"Performing {0}of() operation on an invalid template of type "+displayName+". Maximum size ({1}) contradicts the length restriction ({2}..{3}).\", op_name, max_size, range_length_min_length, range_length_max_length ) );\n");
			source.append("\t\t			default:\n");
			source.append("\t\t				throw new TtcnError(\"Internal error: Template has invalid length restriction type.\");\n");
			source.append("\t\t			}\n");
			source.append("\t\t		}\n");
			source.append("\t\t	}\n");
			source.append("\t\t}\n");
			source.append("\t\tbreak;\n");
		}
		source.append("\t\tcase OMIT_VALUE:\n");
		source.append("\t\t\tthrow new TtcnError( MessageFormat.format( \"Performing {0}of() operation on a template of type "+displayName+" containing omit value.\", op_name ) );\n");
		source.append("\t\tcase ANY_VALUE:\n");
		source.append("\t\tcase ANY_OR_OMIT:\n");
		source.append("\t\t\tmin_size = 0;\n");
		source.append("\t\t\thas_any_or_none = true;\n");
		source.append("\t\t\tbreak;\n");
		source.append("\t\tcase VALUE_LIST:\n");
		source.append("\t\t{\n");
		source.append("\t\t\tif (list_value.size()<1)\n");
		source.append("\t\t\t\tthrow new TtcnError( MessageFormat.format( \"Performing {0}of() operation on a template of type "+displayName+" containing an empty list.\", op_name ) );\n");
		source.append("\t\t\tint item_size = list_value.get(0).sizeOf(is_size).getInt();\n");
		source.append("\t\t\tfor (int i = 1; i < list_value.size(); i++) {\n");
		source.append("\t\t\t\tif (list_value.get(i).sizeOf(is_size).getInt()!=item_size) {\n");
		source.append("\t\t\t\t\tthrow new TtcnError( MessageFormat.format( \"Performing {0}of() operation on a template of type "+displayName+" containing a value list with different sizes.\", op_name ) );\n");
		source.append("\t\t\t\t}\n");
		source.append("\t\t\t}\n");
		source.append("\t\t\tmin_size = item_size;\n");
		source.append("\t\t\thas_any_or_none = false;\n");
		source.append("\t\t\tbreak;\n");
		source.append("\t\t}\n");
		source.append("\t\tcase COMPLEMENTED_LIST:\n");
		source.append("\t\t\tthrow new TtcnError( MessageFormat.format( \"Performing {0}of() operation on a template of type "+displayName+" containing complemented list.\", op_name ) );\n");
		source.append("\t\tdefault:\n");
		source.append("\t\t\tthrow new TtcnError( MessageFormat.format( \"Performing {0}of() operation on an uninitialized/unsupported template of type "+genName+".\", op_name ) );\n");
		source.append("\t\t}\n");
		source.append("\t\treturn new TitanInteger(check_section_is_single(min_size, has_any_or_none, op_name, \"a template of type\", \""+ofTypeName+"\"));\n");
		source.append("\t}\n");
	}

	/**
	 * Generate n_elem function for template
	 *
	 * @param source where the source code is to be generated.
	 * @param genName the name of the generated class representing the "record of/set of" type.
	 */
	private static void generateTemplateNElem(final StringBuilder source, final String genName) {
		source.append('\n');
		source.append("\tpublic TitanInteger nofElements() {\n");
		source.append("\t\tswitch (templateSelection) {\n");
		source.append("\t\tcase SPECIFIC_VALUE:\n");
		source.append("\t\t\treturn new TitanInteger(value_elements.size());\n");
		source.append("\t\tcase COMPLEMENTED_LIST:\n");
		source.append( MessageFormat.format( "\t\t\tthrow new TtcnError(\"Performing n_elem() operation on a template of type {0} containing complemented list.\");\n", genName ) );
		source.append("\t\tcase UNINITIALIZED_TEMPLATE:\n");
		source.append("\t\tcase OMIT_VALUE:\n");
		source.append("\t\tcase ANY_VALUE:\n");
		source.append("\t\tcase ANY_OR_OMIT:\n");
		source.append("\t\tcase VALUE_LIST:\n");
		source.append("\t\tcase VALUE_RANGE:\n");
		source.append("\t\tcase STRING_PATTERN:\n");
		source.append("\t\tcase SUPERSET_MATCH:\n");
		source.append("\t\tcase SUBSET_MATCH:\n");
		source.append("\t\tcase DECODE_MATCH:\n");
		source.append("\t\t\tbreak;\n");
		source.append("\t\t}\n");
		source.append( MessageFormat.format( "\t\tthrow new TtcnError(\"Performing n_elem() operation on an uninitialized/unsupported template of type {0}.\");\n", genName ) );
		source.append("\t}\n");
	}

	/**
	 * Generate matchv function for template
	 *
	 * @param source where the source code is to be generated.
	 * @param genName the name of the generated class representing the "record of/set of" type.
	 */
	private static void generateTemplateMatchv(final StringBuilder source, final String genName) {
		source.append('\n');
		source.append( MessageFormat.format( "\tprivate boolean matchv(final {0} other_value, boolean legacy) '{'\n", genName ) );
		source.append("\t\tif (!other_value.isBound()) {\n");
		source.append("\t\t\treturn false;\n");
		source.append("\t\t}\n");
		source.append("\t\tint value_length = other_value.sizeOf().getInt();\n");
		source.append("\t\tif (!match_length(value_length)) {\n");
		source.append("\t\t\treturn false;\n");
		source.append("\t\t}\n");
		source.append("\t\tswitch (templateSelection) {\n");
		source.append("\t\tcase SPECIFIC_VALUE:\n");
		source.append("\t\t\treturn RecordOfMatch.match_record_of(other_value, value_length, this, value_elements.size(), match_function_specific, legacy);\n");
		source.append("\t\tcase OMIT_VALUE:\n");
		source.append("\t\t\treturn false;\n");
		source.append("\t\tcase ANY_VALUE:\n");
		source.append("\t\tcase ANY_OR_OMIT:\n");
		source.append("\t\t\treturn true;\n");
		source.append("\t\tcase VALUE_LIST:\n");
		source.append("\t\tcase COMPLEMENTED_LIST:\n");
		source.append("\t\t\tfor (int list_count = 0; list_count < list_value.size(); list_count++) {\n");
		source.append("\t\t\t\tif (list_value.get(list_count).matchv(other_value, legacy)) {\n");
		source.append("\t\t\t\t\treturn templateSelection == template_sel.VALUE_LIST;\n");
		source.append("\t\t}\n");
		source.append("\t\t\t}\n");
		source.append("\t\t\treturn templateSelection == template_sel.COMPLEMENTED_LIST;\n");
		source.append("\t\tdefault:\n");
		source.append( MessageFormat.format( "\t\t\tthrow new TtcnError(\"Matching with an uninitialized/unsupported template of type {0}.\");\n", genName ) );
		source.append("\t\t}\n");
		source.append("\t}\n");
	}

	/**
	 * Generate is_value function for template
	 *
	 * @param source where the source code is to be generated.
	 * @param genName the name of the generated class representing the "record of/set of" type.
	 */
	private static void generateTemplateIsValue(final StringBuilder source, final String genName) {
		source.append('\n');
		source.append("\t@Override\n");
		source.append("\tpublic boolean isValue() {\n");
		source.append("\t\tif (templateSelection != template_sel.SPECIFIC_VALUE || is_ifPresent) {\n");
		source.append("\t\t\treturn false;\n");
		source.append("\t\t}\n");
		source.append("\t\tfor (int elem_count = 0; elem_count < value_elements.size(); elem_count++) {\n");
		source.append("\t\t\tif (!value_elements.get(elem_count).isValue()) return false;\n");
		source.append("\t\t}\n");
		source.append("\t\treturn true;\n");
		source.append("\t}\n");
	}

	/**
	 * Generate set_type function for template
	 *
	 * @param source where the source code is to be generated.
	 * @param genName the name of the generated class representing the "record of/set of" type.
	 * @param ofTypeName type name of the "record of/set of" element
	 * @param displayName the user readable name of the type to be generated.
	 * @param isSetOf true: set of, false: record of
	 */
	private static void generateTemplateSetType( final StringBuilder source, final String genName, final String ofTypeName,
												 final String displayName, final boolean isSetOf ) {
		source.append('\n');
		source.append("\tpublic void setType(template_sel template_type, int list_length) {\n");
		source.append("\t\tcleanUp();\n");
		source.append("\t\tswitch (template_type) {\n");
		source.append("\t\tcase VALUE_LIST:\n");
		source.append("\t\tcase COMPLEMENTED_LIST:\n");
		source.append( MessageFormat.format( "\t\t\tlist_value = new ArrayList<{0}_template>( list_length );\n", genName ) );
		source.append("\t\t\tfor (int list_count = 0; list_count < list_length; list_count++) {\n");
		source.append( MessageFormat.format( "\t\t\t\tlist_value.add( new {0}_template() );\n", genName ) );
		source.append("\t\t\t}\n");
		source.append("\t\t\tbreak;\n");
		if ( isSetOf ) {
			source.append("\t\tcase SUPERSET_MATCH:\n");
			source.append("\t\tcase SUBSET_MATCH:\n");
			source.append( MessageFormat.format( "\t\t\tset_items = new ArrayList<{0}>(list_length);\n", ofTypeName ) );
			source.append("\t\t\tfor( int i = 0; i < list_length; i++ ) {\n");
			source.append( MessageFormat.format( "\t\t\t\tset_items.add( new {0}() );\n", ofTypeName ) );
			source.append("\t\t\t}\n");
			source.append("\t\t\tbreak;\n");
		}
		source.append("\t\tdefault:\n");
		source.append( MessageFormat.format( "\t\t\tthrow new TtcnError(\"Internal error: Setting an invalid type for a template of type {0}.\");\n", displayName ) );
		source.append("\t\t}\n");
		source.append("\t\tsetSelection(template_type);\n");
		source.append("\t}\n");
	}

	/**
	 * Generating listItem() function for template
	 * @param aSb the output, where the java code is written
	 * @param genName the name of the generated class representing the "record of/set of" type.
	 * @param displayName the user readable name of the type to be generated.
	 */
	private static void generateTemplateListItem( final StringBuilder aSb, final String genName, final String displayName ) {
		aSb.append('\n');
		aSb.append( MessageFormat.format( "\t\tpublic {0}_template listItem(int list_index) '{'\n", genName ) );
		aSb.append("\t\t\tif (templateSelection != template_sel.VALUE_LIST && templateSelection != template_sel.COMPLEMENTED_LIST) {\n");
		aSb.append( MessageFormat.format( "\t\t\t\tthrow new TtcnError(\"Accessing a list element of a non-list template of type {0}.\");\n", displayName ) );
		aSb.append("\t\t\t}\n");
		aSb.append("\t\t\tif (list_index >= list_value.size()) {\n");
		aSb.append( MessageFormat.format( "\t\t\t\tthrow new TtcnError(\"Index overflow in a value list template of type {0}.\");\n", displayName ) );
		aSb.append("\t\t\t}\n");
		aSb.append("\t\t\treturn list_value.get(list_index);\n");
		aSb.append("\t\t}\n");
	}

	/**
	 * Generate get_list_item function for template
	 *
	 * @param source where the source code is to be generated.
	 * @param genName the name of the generated class representing the "record of/set of" type.
	 * @param displayName the user readable name of the type to be generated.
	 */
	private static void generateTemplateGetListItem(final StringBuilder source, final String genName, final String displayName) {
		source.append('\n');
		source.append( MessageFormat.format( "\tpublic {0}_template get_list_item(int list_index) '{'\n", genName ) );
		source.append("\t\tif (templateSelection != template_sel.VALUE_LIST && templateSelection != template_sel.COMPLEMENTED_LIST) {\n");
		source.append( MessageFormat.format( "\t\t\tthrow new TtcnError(\"Internal error: Accessing a list element of a non-list template of type {0}.\");\n", displayName ) );
		source.append("\t\t}\n");
		source.append("\t\tif (list_index < 0) {\n");
		source.append("\t\t\tthrow new TtcnError( MessageFormat.format( \"Internal error: Accessing a value list template of type "+displayName+" using a negative index ({0}).\", list_index ) );\n");
		source.append("\t\t}\n");
		source.append("\t\tif (list_index >= list_value.size()) {\n");
		source.append( MessageFormat.format( "\t\t\tthrow new TtcnError(\"Internal error: Index overflow in a value list template of type {0}.\");\n", displayName ) );
		source.append("\t\t}\n");
		source.append("\t\treturn list_value.get( list_index );\n");
		source.append("\t}\n");
	}

	/**
	 * Generating valueOf() function for template
	 * @param aSb the output, where the java code is written
	 * @param genName the name of the generated class representing the "record of/set of" type.
	 * @param displayName the user readable name of the type to be generated.
	 */
	private static void generateTemplateValueOf( final StringBuilder aSb, final String genName, final String displayName ) {
		aSb.append('\n');
		aSb.append( MessageFormat.format( "\t\tpublic {0} valueOf() '{'\n", genName ) );
		aSb.append("\t\t\tif (templateSelection != template_sel.SPECIFIC_VALUE || is_ifPresent) {\n");
		aSb.append( MessageFormat.format( "\t\t\t\tthrow new TtcnError(\"Performing a valueof or send operation on a non-specific template of type {0}.\");\n", displayName ) );
		aSb.append("\t\t\t}\n");
		aSb.append( MessageFormat.format( "\t\t\t{0} ret_val = new {0}(TitanNull_Type.NULL_VALUE);\n", genName ) );
		aSb.append("\t\t\tfor (int elem_count = 0; elem_count < value_elements.size(); elem_count++) {\n");
		aSb.append("\t\t\t\tif (value_elements.get(elem_count).isBound()) {\n");
		aSb.append("\t\t\t\t\tret_val.valueElements.add( value_elements.get(elem_count).valueOf() );\n");
		aSb.append("\t\t\t\t}\n");
		aSb.append("\t\t\t}\n");
		aSb.append("\t\t\treturn ret_val;\n");
		aSb.append("\t\t}\n");
	}

	/**
	 * Generating substr() function for template
	 * @param aSb the output, where the java code is written
	 * @param genName the name of the generated class representing the "record of/set of" type.
	 */
	private static void generateTemplateSubstr( final StringBuilder aSb, final String genName ) {
		aSb.append('\n');
		aSb.append( MessageFormat.format( "\t\tpublic {0} substr(int index, int returncount) '{'\n", genName ) );
		aSb.append("\t\t\tif (!isValue()) {\n");
		aSb.append("\t\t\t\tthrow new TtcnError(\"The first argument of function substr() is a template with non-specific value.\");\n");
		aSb.append("\t\t\t}\n");
		aSb.append("\t\t\treturn valueOf().substr(index, returncount);\n");
		aSb.append("\t\t}\n");
	}

	/**
	 * Generating log() function for template
	 * @param aSb the output, where the java code is written
	 * @param genName the name of the generated class representing the "record of/set of" type.
	 * @param displayName the user readable name of the type to be generated.
	 * @param isSetOf true: set of, false: record of
	 */
	private static void generateTemplateLog( final StringBuilder aSb, final String genName, final String displayName, final boolean isSetOf ) {
		aSb.append('\n');
		aSb.append("\t\tpublic void log() {\n");
		aSb.append("\t\t\tswitch (templateSelection) {\n");
		aSb.append("\t\t\tcase SPECIFIC_VALUE:\n");
		aSb.append("\t\t\t\tif (value_elements.size() > 0) {\n");
		aSb.append("\t\t\t\t\tTtcnLogger.log_event_str(\"{ \");\n");
		aSb.append("\t\t\t\t\tfor (int elem_count = 0; elem_count < value_elements.size(); elem_count++) {\n");
		aSb.append("\t\t\t\t\t\tif (elem_count > 0) {\n");
		aSb.append("\t\t\t\t\t\t\tTtcnLogger.log_event_str(\", \");\n");
		aSb.append("\t\t\t\t\t\t}\n");
		if ( !isSetOf ) {
			aSb.append("\t\t\t\t\t\tif (permutation_starts_at(elem_count)) {\n");
			aSb.append("\t\t\t\t\t\t\tTtcnLogger.log_event_str(\"permutation(\");\n");
			aSb.append("\t\t\t\t\t\t}\n");
		}
		aSb.append("\t\t\t\t\t\tvalue_elements.get(elem_count).log();\n");
		if ( !isSetOf ) {
			aSb.append("\t\t\t\t\t\tif (permutation_ends_at(elem_count)) {\n");
			aSb.append("\t\t\t\t\t\t\tTtcnLogger.log_char(')');\n");
			aSb.append("\t\t\t\t\t\t}\n");
		}
		aSb.append("\t\t\t\t\t}\n");
		aSb.append("\t\t\t\t\tTtcnLogger.log_event_str(\" }\");\n");
		aSb.append("\t\t\t\t} else {\n");
		aSb.append("\t\t\t\t\tTtcnLogger.log_event_str(\"{ }\");\n");
		aSb.append("\t\t\t\t}\n");
		aSb.append("\t\t\t\tbreak;\n");
		aSb.append("\t\t\tcase COMPLEMENTED_LIST:\n");
		aSb.append("\t\t\t\tTtcnLogger.log_event_str(\"complement\");\n");
		aSb.append("\t\t\tcase VALUE_LIST:\n");
		aSb.append("\t\t\t\tTtcnLogger.log_char('(');\n");
		aSb.append("\t\t\t\tfor (int list_count = 0; list_count < list_value.size(); list_count++) {\n");
		aSb.append("\t\t\t\t\tif (list_count > 0) TtcnLogger.log_event_str(\", \");\n");
		aSb.append("\t\t\t\t\tlist_value.get(list_count).log();\n");
		aSb.append("\t\t\t\t}\n");
		aSb.append("\t\t\t\tTtcnLogger.log_char(')');\n");
		aSb.append("\t\t\t\tbreak;\n");
		if ( isSetOf ) {
			aSb.append("\t\t\tcase SUPERSET_MATCH:\n");
			aSb.append("\t\t\tcase SUBSET_MATCH:\n");
			aSb.append("\t\t\t\tTtcnLogger.log_event(\"%s(\", templateSelection == template_sel.SUPERSET_MATCH ? \"superset\" : \"subset\");\n");
			aSb.append("\t\t\t\tfor (int set_count = 0; set_count < set_items.size(); set_count++) {\n");
			aSb.append("\t\t\t\t\tif (set_count > 0) {\n");
			aSb.append("\t\t\t\t\t\tTtcnLogger.log_event_str(\", \");\n");
			aSb.append("\t\t\t\t\t}\n");
			aSb.append("\t\t\t\t\tset_items.get(set_count).log();\n");
			aSb.append("\t\t\t\t}\n");
			aSb.append("\t\t\t\tTtcnLogger.log_char(')');\n");
			aSb.append("\t\t\t\tbreak;\n");
		}
		aSb.append("\t\t\tdefault:\n");
		aSb.append("\t\t\t\tlog_generic();\n");
		aSb.append("\t\t\t}\n");
		aSb.append("\t\t\tlog_restricted();\n");
		aSb.append("\t\t\tlog_ifpresent();\n");
		aSb.append("\t\t}\n");

		aSb.append('\n');
		aSb.append(MessageFormat.format("\tpublic void log_match(final {0} match_value) '{'\n", genName ) );
		aSb.append("\t\tlog_match(match_value, false);\n");
		aSb.append("\t}\n");

		aSb.append('\n');
		aSb.append("\t@Override\n");
		aSb.append("\tpublic void log_match(final Base_Type match_value, final boolean legacy) {\n");
		aSb.append(MessageFormat.format("\t\tif (match_value instanceof {0}) '{'\n", genName));
		aSb.append(MessageFormat.format("\t\t\tlog_match(({0})match_value, legacy);\n", genName));
		aSb.append("\t\t\treturn;\n");
		aSb.append("\t\t}\n\n");
		aSb.append(MessageFormat.format("\t\tthrow new TtcnError(\"Internal Error: value can not be cast to {0}.\");\n", displayName));
		aSb.append("\t}\n");

		aSb.append('\n');
		aSb.append(MessageFormat.format("\tpublic void log_match(final {0} match_value, boolean legacy) '{'\n", genName ) );
		if ( isSetOf ) {
			aSb.append("\t\tif ( TtcnLogger.matching_verbosity_t.VERBOSITY_COMPACT == TtcnLogger.get_matching_verbosity() ) {\n");
			aSb.append("\t\t\tif(match(match_value, legacy)) {\n");
			aSb.append("\t\t\t\tTtcnLogger.print_logmatch_buffer();\n");
			aSb.append("\t\t\t\tTtcnLogger.log_event_str(\" matched\");\n");
			aSb.append("\t\t\t} else {\n");
			aSb.append("\t\t\t\tint previous_size = TtcnLogger.get_logmatch_buffer_len();\n");
			aSb.append("\t\t\t\tif (templateSelection == template_sel.SPECIFIC_VALUE) {\n");
			aSb.append("\t\t\t\t\tRecordOfMatch.log_match_heuristics(match_value, match_value.sizeOf().getInt(), this, value_elements.size(), match_function_specific, log_function, legacy);\n");
			aSb.append("\t\t\t\t} else {\n");
			aSb.append("\t\t\t\t\tif(previous_size != 0) {\n");
			aSb.append("\t\t\t\t\t\tTtcnLogger.print_logmatch_buffer();\n");
			aSb.append("\t\t\t\t\t\tTtcnLogger.set_logmatch_buffer_len(previous_size);\n");
			aSb.append("\t\t\t\t\t\tTtcnLogger.log_event_str(\":=\");\n");
			aSb.append("\t\t\t\t\t}\n");
			aSb.append("\t\t\t\t}\n");
			aSb.append("\t\t\t\tmatch_value.log();\n");
			aSb.append("\t\t\t\tTtcnLogger.log_event_str(\" with \");\n");
			aSb.append("\t\t\t\tlog();\n");
			aSb.append("\t\t\t\tTtcnLogger.log_event_str(\" unmatched\");\n");
			aSb.append("\t\t\t}\n");
			aSb.append("\t\t\treturn;\n");
			aSb.append("\t\t}\n");
			aSb.append("\t\tmatch_value.log();\n");
			aSb.append("\t\tTtcnLogger.log_event_str(\" with \");\n");
			aSb.append("\t\tlog();\n");
			aSb.append("\t\tif (match(match_value, legacy)) {\n");
			aSb.append("\t\t\tTtcnLogger.log_event_str(\" matched\");\n");
			aSb.append("\t\t} else {\n");
			aSb.append("\t\t\tTtcnLogger.log_event_str(\" unmatched\");\n");
			aSb.append("\t\t\tif (templateSelection == template_sel.SPECIFIC_VALUE) {\n");
			aSb.append("\t\t\t\tRecordOfMatch.log_match_heuristics(match_value, match_value.sizeOf().getInt(), this, value_elements.size(), match_function_specific, log_function, legacy);\n");
			aSb.append("\t\t\t}\n");
			aSb.append("\t\t}\n");
		} else {
			aSb.append("\t\tif ( TtcnLogger.matching_verbosity_t.VERBOSITY_COMPACT == TtcnLogger.get_matching_verbosity() ) {\n");
			aSb.append("\t\t\tif(match(match_value, legacy)) {\n");
			aSb.append("\t\t\t\tTtcnLogger.print_logmatch_buffer();\n");
			aSb.append("\t\t\t\tTtcnLogger.log_event_str(\" matched\");\n");
			aSb.append("\t\t\t} else {\n");

			aSb.append("\t\t\t\tif (templateSelection == template_sel.SPECIFIC_VALUE && value_elements.size() > 0 && get_number_of_permutations() == 0 && value_elements.size() == match_value.sizeOf().getInt()) {\n");
			aSb.append("\t\t\t\t\tint previous_size = TtcnLogger.get_logmatch_buffer_len();\n");
			aSb.append("\t\t\t\t\tfor (int elem_count = 0; elem_count < value_elements.size(); elem_count++) {\n");
			aSb.append("\t\t\t\t\t\tif ( !value_elements.get(elem_count).match(match_value.constGetAt(elem_count), legacy) ) {\n");
			aSb.append("\t\t\t\t\t\tTtcnLogger.log_logmatch_info(\"[%d]\", elem_count);\n");
			aSb.append("\t\t\t\t\t\t\tvalue_elements.get(elem_count).log_match( match_value.constGetAt(elem_count), legacy );\n");
			aSb.append("\t\t\t\t\t\t\tTtcnLogger.set_logmatch_buffer_len(previous_size);\n");
			aSb.append("\t\t\t\t\t\t}\n");
			aSb.append("\t\t\t\t\t}\n");
			aSb.append("\t\t\t\t\tlog_match_length(value_elements.size());\n");
			aSb.append("\t\t\t\t} else {\n");
			aSb.append("\t\t\t\t\tTtcnLogger.print_logmatch_buffer();\n");
			aSb.append("\t\t\t\t\tmatch_value.log();\n");
			aSb.append("\t\t\t\t\tTtcnLogger.log_event_str(\" with \");\n");
			aSb.append("\t\t\t\t\tlog();\n");
			aSb.append("\t\t\t\t\tTtcnLogger.log_event_str(\" unmatched\");\n");
			aSb.append("\t\t\t\t}\n");
			aSb.append("\t\t\t}\n");
			aSb.append("\t\t\treturn;\n");
			aSb.append("\t\t}\n");
			aSb.append("\t\tif (templateSelection == template_sel.SPECIFIC_VALUE && value_elements.size() > 0 && get_number_of_permutations() == 0 && value_elements.size() == match_value.sizeOf().getInt()) {\n");
			aSb.append("\t\t\tTtcnLogger.log_event_str(\"{ \");\n");
			aSb.append("\t\t\tfor (int elem_count = 0; elem_count < value_elements.size(); elem_count++) {\n");
			aSb.append("\t\t\t\tif (elem_count > 0) {\n");
			aSb.append("\t\t\t\t\tTtcnLogger.log_event_str(\", \");\n");
			aSb.append("\t\t\t\t}\n");
			aSb.append("\t\t\t\tvalue_elements.get(elem_count).log_match( match_value.constGetAt(elem_count), legacy );\n");
			aSb.append("\t\t\t}\n");
			aSb.append("\t\t\tTtcnLogger.log_event_str(\" }\");\n");
			aSb.append("\t\t\tlog_match_length(value_elements.size());\n");
			aSb.append("\t\t} else {\n");
			aSb.append("\t\t\tmatch_value.log();\n");
			aSb.append("\t\t\tTtcnLogger.log_event_str(\" with \");\n");
			aSb.append("\t\t\tlog();\n");
			aSb.append("\t\t\tif ( match(match_value, legacy) ) {\n");
			aSb.append("\t\t\t\tTtcnLogger.log_event_str(\" matched\");\n");
			aSb.append("\t\t\t} else {\n");
			aSb.append("\t\t\t\tTtcnLogger.log_event_str(\" unmatched\");\n");

			aSb.append("\t\t\t}\n");
			aSb.append("\t\t}\n");
		}
		aSb.append("\t}\n");
	}

//TODO: implement void log_matchv(final Base_Type match_value, boolean legacy)
/*
	void log_matchv(final Base_Type match_value, boolean legacy)
	{
		if (TtcnLogger.VERBOSITY_COMPACT == TtcnLogger.get_matching_verbosity()) {
			if (matchv(match_value, legacy)) {
				TtcnLogger.print_logmatch_buffer();
				TtcnLogger.log_event_str(" matched");
			} else {
				final Record_Of_Type recof_value = (Record_Of_Type)(match_value);
				if (templateSelection == SPECIFIC_VALUE &&
						single_value.n_elements > 0 && get_number_of_permutations() == 0 &&
						single_value.n_elements == recof_value.size_of()) {
					size_t previous_size = TtcnLogger.get_logmatch_buffer_len();
					for (int elem_count = 0; elem_count < single_value.n_elements; elem_count++) {
						if(!single_value.value_elements[elem_count].matchv(recof_value.getAt(elem_count), legacy)){
							TtcnLogger.log_logmatch_info("[{0}]", elem_count);
							single_value.value_elements[elem_count].log_matchv(recof_value.getAt(elem_count), legacy);
							TtcnLogger.set_logmatch_buffer_len(previous_size);
						}
					}
					log_match_length(single_value.n_elements);
				} else {
					TtcnLogger.print_logmatch_buffer();
					match_value.log();
					TtcnLogger.log_event_str(" with ");
					log();
					TtcnLogger.log_event_str(" unmatched");
				}
			}
		} else {
			final Record_Of_Type recof_value = static_cast<const Record_Of_Type*>(match_value);
			if (templateSelection == SPECIFIC_VALUE &&
					single_value.n_elements > 0 && get_number_of_permutations() == 0 &&
					single_value.n_elements == recof_value.size_of()) {
				TtcnLogger.log_event_str("{ ");
				for (int elem_count = 0; elem_count < single_value.n_elements; elem_count++) {
					if (elem_count > 0) TtcnLogger.log_event_str(", ");
					single_value.value_elements[elem_count].log_matchv(recof_value.getAt(elem_count), legacy);
				}
				TtcnLogger.log_event_str(" }");
				log_match_length(single_value.n_elements);
			} else {
				match_value.log();
				TtcnLogger.log_event_str(" with ");
				log();
				if (matchv(match_value, legacy)) TtcnLogger.log_event_str(" matched");
				else TtcnLogger.log_event_str(" unmatched");
			}
		}
	}

//TODO
//*/
	//TODO" implement void encode_text(Text_Buf& text_buf)
	//TODO: implement void set_param(Module_Param& param)
	//TODO: implement Module_Param* get_param(Module_Param_Name& param_name) const

	/**
	 * Generate get_istemplate_kind function for template
	 *
	 * @param source where the source code is to be generated.
	 * @param genName the name of the generated class representing the "record of/set of" type.
	 */
	private static void generateTemplateGetIstemplateKind(final StringBuilder source, final String genName) {
		source.append('\n');
		source.append("\t@Override\n");
		source.append("\tprotected boolean get_istemplate_kind(final String type) {\n");
		source.append("\t\tif (\"AnyElement\".equals(type)) {\n");
		source.append("\t\t\tif (templateSelection != template_sel.SPECIFIC_VALUE) {\n");
		source.append("\t\t\treturn false;\n");
		source.append("\t\t\t}\n");
		source.append("\t\t\tfor (int i = 0; i < value_elements.size(); i++) {\n");
		source.append("\t\t\t\tif (value_elements.get(i).getSelection() == template_sel.ANY_VALUE) {\n");
		source.append("\t\t\t\t\treturn true;\n");
		source.append("\t\t\t\t}\n");
		source.append("\t\t\t}\n");
		source.append("\t\t\treturn false;\n");
		source.append("\t\t} else if (\"AnyElementsOrNone\".equals(type)) {\n");
		source.append("\t\t\tif (templateSelection != template_sel.SPECIFIC_VALUE) {\n");
		source.append("\t\t\t\treturn false;\n");
		source.append("\t\t\t}\n");
		source.append("\t\t\tfor (int i = 0; i < value_elements.size(); i++) {\n");
		source.append("\t\t\t\tif (value_elements.get(i).getSelection() == template_sel.ANY_OR_OMIT) {\n");
		source.append("\t\t\t\t\treturn true;\n");
		source.append("\t\t\t\t}\n");
		source.append("\t\t\t}\n");
		source.append("\t\t\treturn false;\n");
		source.append("\t\t} else if (\"permutation\".equals(type)) {\n");
		source.append("\t\t\treturn get_number_of_permutations() != 0;\n");
		source.append("\t\t} else if (\"length\".equals(type)) {\n");
		source.append("\t\t\treturn length_restriction_type != length_restriction_type_t.NO_LENGTH_RESTRICTION;\n");
		source.append("\t\t} else {\n");
		source.append("\t\t\treturn super.get_istemplate_kind(type);\n");
		source.append("\t\t}\n");
		source.append("\t}\n");
	}

	/**
	 * Generate check_restriction function for template
	 *
	 * @param source where the source code is to be generated.
	 * @param displayName the user readable name of the type to be generated.
	 */
	private static void generateTemplateCheckRestriction(final StringBuilder source, final String displayName) {
		source.append('\n');
		source.append("\tpublic void check_restriction(template_res t_res, final String t_name) {\n");
		source.append("\t\tcheck_restriction(t_res, t_name, false);\n");
		source.append("\t}\n");

		source.append('\n');
		source.append("\tpublic void check_restriction(template_res t_res, final String t_name, boolean legacy) {\n");
		source.append("\t\tif (templateSelection==template_sel.UNINITIALIZED_TEMPLATE) {\n");
		source.append("\t\t\treturn;\n");
		source.append("\t\t}\n");
		source.append("\t\tswitch ((t_name != null && (t_res==template_res.TR_VALUE)) ? template_res.TR_OMIT : t_res) {\n");
		source.append("\t\tcase TR_OMIT:\n");
		source.append("\t\t\tif (templateSelection==template_sel.OMIT_VALUE) {\n");
		source.append("\t\t\t\treturn;\n");
		source.append("\t\t\t}\n");
		source.append("\t\t\t// no break\n");
		source.append("\t\tcase TR_VALUE:\n");
		source.append("\t\t\tif (templateSelection!=template_sel.SPECIFIC_VALUE || is_ifPresent) {\n");
		source.append("\t\t\t\tbreak;\n");
		source.append("\t\t\t}\n");
		source.append("\t\t\tfor (int i=0; i<value_elements.size(); i++)\n");
		source.append("\t\t\t\tvalue_elements.get(i).check_restriction(t_res, t_name != null ? t_name : \""+displayName+"\");\n");
		source.append("\t\t\treturn;\n");
		source.append("\t\tcase TR_PRESENT:\n");
		source.append("\t\t\tif (!match_omit(legacy)) return;\n");
		source.append("\t\t\tbreak;\n");
		source.append("\t\tdefault:\n");
		source.append("\t\t\treturn;\n");
		source.append("\t\t}\n");
		source.append("\t\tthrow new TtcnError( MessageFormat.format( \"Restriction `{0}' on template of type {1} violated.\", getResName(t_res), t_name != null ? t_name : \""+displayName+"\" ) );\n");
		source.append("\t}\n");
	}
}
