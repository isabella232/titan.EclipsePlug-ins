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

	/**
	 * Generate "record of/set of" class
	 * @param aData only used to update imports if needed.
	 * @param source where the source code is to be generated.
	 * @param genName the name of the generated class representing the "record of/set of" type.
	 * @param displayName the user readable name of the type to be generated.
	 * @param ofTypeName type name of the "record of/set of" element 
	 */
	public static void generateValueClass( final JavaGenData aData,
										   final StringBuilder source,
										   final String genName,
										   final String displayName,
										   final String ofTypeName ) {
		aData.addImport("java.util.List");
		aData.addBuiltinTypeImport("Base_Type");
		aData.addBuiltinTypeImport("TitanBoolean");
		aData.addBuiltinTypeImport("TtcnError");
		aData.addBuiltinTypeImport("TitanInteger");
		aData.addBuiltinTypeImport("TitanNull_Type");
		aData.addBuiltinTypeImport("AdditionalFunctions");
		source.append(MessageFormat.format("public static class {0} extends Base_Type '{'\n", genName));

		generateValueDeclaration( source, genName, ofTypeName );
		generateValueConstructors( source, genName, ofTypeName, displayName );
		generateValueCopyList( source, ofTypeName );
		generateValueIsPresent( source );
		generateValueIsBound( source );
		generateValueOperatorEquals( source, genName, ofTypeName , displayName);
		generateValueAssign( source, genName, ofTypeName, displayName);
		generateValueCleanup( source );
		generateValueGetterSetters( source, ofTypeName, displayName );
		generateValueGetUnboundElem( source, ofTypeName );
		generateValueToString( source );
		generateValueReplace( source, genName, ofTypeName, displayName );

		source.append("}\n");
	}

	/**
	 * Generate "record of/set of" template class
	 * @param aData only used to update imports if needed.
	 * @param source where the source code is to be generated.
	 * @param genName the name of the generated class representing the "record of/set of" type.
	 * @param displayName the user readable name of the type to be generated.
	 * @param ofTypeName type name of the "record of/set of" element 
	 */
	public static void generateTemplateClass( final JavaGenData aData,
											  final StringBuilder source,
											  final String genName,
											  final String displayName,
											  final String ofTypeName ) {
		aData.addImport("java.util.List");
		aData.addImport("java.util.concurrent.atomic.AtomicBoolean");
		aData.addImport("java.util.concurrent.atomic.AtomicInteger");
		aData.addImport("java.text.MessageFormat");
		aData.addBuiltinTypeImport("Base_Template");
		aData.addBuiltinTypeImport("Record_Of_Template");
		aData.addBuiltinTypeImport("TitanBoolean");
		aData.addBuiltinTypeImport("TitanInteger");
		aData.addBuiltinTypeImport("TtcnError");
		aData.addBuiltinTypeImport("RecordOfMatch");
		aData.addBuiltinTypeImport("RecordOfMatch.match_function_t");
		aData.addBuiltinTypeImport("Restricted_Length_Template");
		source.append( MessageFormat.format( "public static class {0}_template extends Record_Of_Template '{'\n", genName ) );

		generateTemplateDeclaration( source, genName, ofTypeName );
		generateTemplateConstructors( source, genName, displayName );
		generateTemplateCopyTemplate( source, genName, displayName );
		generateTemplateIsPresent( source );
		generateTemplateMatch( source, genName, displayName );
		generateTemplateMatchOmit( source );
		generateTemplateAssign( source, genName, displayName );
		generateTemplateCleanup( source );
		generateTemplateReplace( source, genName, displayName );
		generateTemplateGetterSetters( source, genName, ofTypeName, displayName );
		generateTemplateConcat( source, genName, ofTypeName, displayName );
		generateTemplateSetSize( source, genName, ofTypeName, displayName );
		generateTemplateNElem( source, genName );
		generateTemplateMatchv( source, genName );
		generateTemplateIsValue( source, genName );
		generateTemplateSetType( source, genName );
		generateTemplateListItem( source, genName, displayName );
		generateTemplateGetListItem( source, genName, displayName );
		generateTemplateValueOf( source, genName, displayName );
		generateTemplateGetIstemplateKind( source, genName );
		//TODO: use
		//generateTemplateCheckRestriction( source, genName );
		source.append("}\n");
	}

	/**
	 * Generate member variables
	 *
	 * @param source where the source code is to be generated.
	 * @param genName the name of the generated class representing the "record of/set of" type.
	 * @param ofTypeName type name of the "record of/set of" element 
	 */
	private static void generateValueDeclaration( final StringBuilder source, final String genName, final String ofTypeName ) {
		source.append("\n");
		source.append( MessageFormat.format( "\tList<{0}> valueElements;\n", ofTypeName ) );
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
		source.append("\n");
		source.append( MessageFormat.format( "\tpublic {0}() '{'\n", genName ) );
		source.append("\t}\n");

		source.append("\n");
		source.append( MessageFormat.format( "\tpublic {0}( final {0} otherValue ) '{'\n", genName ) );
		source.append( MessageFormat.format("\t\totherValue.mustBound(\"Copying an unbound value of type {0}.\");\n", displayName ) );
		source.append("\t\tvalueElements = copyList( otherValue.valueElements );\n");
		source.append("\t}\n");

		source.append("\n");
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
		source.append("\n");
		source.append( MessageFormat.format( "\tprivate static final List<{0}> copyList( final List<{0}> srcList ) '{'\n", ofTypeName ) );
		source.append("\t\tif ( srcList == null ) {\n");
		source.append("\t\t\treturn null;\n");
		source.append("\t\t}\n");
		source.append("\n");
		source.append( MessageFormat.format( "\t\tfinal List<{0}> newList = new ArrayList<{0}>( srcList.size() );\n", ofTypeName ) );
		source.append( MessageFormat.format( "\t\tfor ({0} srcElem : srcList) '{'\n", ofTypeName ) );
		source.append( MessageFormat.format( "\t\t\t{0} newElem = getUnboundElem();\n", ofTypeName ) );
		source.append("\t\t\tnewElem.assign( srcElem );\n");
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
		source.append("\n");
		source.append("\t@Override\n");
		source.append("\tpublic TitanBoolean isPresent() {\n");
		source.append("\t\treturn isBound();\n");
		source.append("\t}\n");
	}

	/**
	 * Generate the isBound function
	 *
	 * @param source where the source code is to be generated.
	 */
	private static void generateValueIsBound(final StringBuilder source) {
		source.append("\n");
		source.append("\t@Override\n");
		source.append("\tpublic TitanBoolean isBound() {\n");
		source.append("\t\treturn new TitanBoolean(valueElements != null);\n");
		source.append("\t}\n");	
		source.append("\n");
		source.append("\tpublic void mustBound( final String aErrorMessage ) {\n");
		source.append("\t\tif ( !isBound().getValue() ) {\n");
		source.append("\t\t\tthrow new TtcnError( aErrorMessage );\n");
		source.append("\t\t}\n");
		source.append("\t}\n");
	}

	/**
	 * Generate assignment operators
	 *
	 * @param source where the source code is to be generated.
	 * @param genName the name of the generated class representing the "record of/set of" type.
	 * @param ofTypeName type name of the "record of/set of" element
	 * @param displayName the user readable name of the type to be generated.
	 */
	private static void generateValueOperatorEquals( final StringBuilder source, final String genName, final String ofTypeName, final String displayName ) {
		source.append("\n");
		source.append("\t@Override\n");
		source.append("\tpublic TitanBoolean operatorEquals(Base_Type otherValue) {\n");
		source.append("\n");
		source.append( MessageFormat.format( "\tif (otherValue instanceof {0}) '{'\n", genName) );
		source.append( MessageFormat.format( "\t\treturn operatorEquals(({0})otherValue);\n", genName) );
		source.append("\t}\n\n");
		source.append( MessageFormat.format( "\t\tthrow new TtcnError(\"Internal Error: The left operand of comparison is not of type {0}.\");\n", genName ) );
		source.append("\t}\n");
		source.append("\n");
		source.append("\t//originally operator==\n");
		source.append( MessageFormat.format( "\tpublic TitanBoolean operatorEquals( final {0} otherValue ) '{'\n", genName ) );
		source.append( MessageFormat.format( "\t\tmustBound(\"The left operand of comparison is an unbound value of type {0}.\");\n", displayName ) );
		source.append( MessageFormat.format( "\t\totherValue.mustBound(\"The right operand of comparison is an unbound value of type {0}.\");\n", displayName ) );
		source.append("\n");
		source.append("\t\tfinal int size = valueElements.size();\n"); 
		source.append("\t\tif ( size != otherValue.valueElements.size() ) {\n");
		source.append("\t\t\treturn new TitanBoolean( false );\n");
		source.append("\t\t}\n");
		source.append("\n");
		source.append("\t\tfor ( int i = 0; i < size; i++ ) {\n");
		source.append( MessageFormat.format( "\t\t\tfinal {0} leftElem = valueElements.get( i );\n", ofTypeName ) );
		source.append( MessageFormat.format( "\t\t\tfinal {0} rightElem = otherValue.valueElements.get( i );\n", ofTypeName ) );
		source.append("\t\t\tif ( leftElem.operatorEquals( rightElem ).not().getValue() ) {\n");
		source.append("\t\t\t\treturn new TitanBoolean( false );\n");
		source.append("\t\t\t}\n");
		source.append("\t\t}\n");
		source.append("\n");
		source.append("\t\treturn new TitanBoolean( true );\n");
		source.append("\t}\n");
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
		source.append("\n");
		source.append("\t@Override\n");
		source.append( MessageFormat.format( "\tpublic {0} assign(final Base_Type otherValue) '{'\n", genName ) );
		source.append( MessageFormat.format( "\tif (otherValue instanceof {0}) '{'\n", genName) );
		source.append( MessageFormat.format( "\t\treturn assign(({0})otherValue);\n", genName) );
		source.append("\t}\n\n");
		source.append( MessageFormat.format( "\t\tthrow new TtcnError(\"Internal Error: The left operand of assignment is not of type {0}.\");\n", genName ) );
		source.append("\t}\n");

		source.append("\n");
		source.append("\t//originally operator=\n");
		source.append( MessageFormat.format( "\tpublic {0} assign( final {0} aOtherValue ) '{'\n", genName ) );
		source.append( MessageFormat.format("\t\taOtherValue.mustBound( \"Assigning an unbound value of type {0}.\" );\n", displayName));
		source.append("\n");
		source.append("\t\tvalueElements = copyList( aOtherValue.valueElements );\n");
		source.append("\t\treturn this;\n");
		source.append("\t}\n");

		source.append("\n");
		source.append( MessageFormat.format( "\tpublic {0} assign(TitanNull_Type nullValue) '{'\n", genName ) );
		source.append( MessageFormat.format( "\t\tvalueElements = new ArrayList<{0}>();\n", ofTypeName ) );
		source.append("\t\treturn this;\n");
		source.append("\t};\n");
	}

	/**
	 * Generate the clean_up function
	 *
	 * @param source where the source code is to be generated.
	 */
	private static void generateValueCleanup(final StringBuilder source) {
		source.append("\n");
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
	private static void generateValueGetterSetters(StringBuilder source, final String ofTypeName , final String displayName) {
		source.append("\n");
		source.append("\t//originally get_at(int)\n");
		source.append( MessageFormat.format("\tpublic {0} getAt( final int index_value ) '{'\n", ofTypeName ) );
		source.append("\t\tif (index_value < 0) {\n");
		source.append( MessageFormat.format( "\t\t\tthrow new TtcnError( \"Accessing an element of type {0} using a negative index: \"+index_value+\".\");\n", displayName ) );
		source.append("\t\t}\n");
		source.append("\n");
		source.append("\t\tif (valueElements == null || index_value >= valueElements.size() ) {\n");
		source.append("\t\t\t//increase list size\n");
		source.append("\t\t\tsetSize(index_value + 1);\n");
		source.append("\t\t}\n");
		source.append("\n");
		source.append("\t\tif ( valueElements.get( index_value ) == null ) {\n");
		source.append( MessageFormat.format( "\t\t\t{0} newElem = getUnboundElem();\n", ofTypeName ) );
		source.append("\t\t\tvalueElements.set( index_value, newElem );\n");
		source.append("\t\t}\n");
		source.append("\t\treturn valueElements.get( index_value );\n");
		source.append("\t}\n");

		source.append("\n");
		source.append("\t//originally get_at(const INTEGER&)\n");
		source.append( MessageFormat.format("\tpublic {0} getAt(final TitanInteger index_value) '{'\n", ofTypeName ) );
		source.append( MessageFormat.format( "\t\tindex_value.mustBound( \"Using an unbound integer value for indexing a value of type {0}.\" );\n", displayName ) );
		source.append("\t\treturn getAt( index_value.getInt() );\n");
		source.append("\t}\n");

		source.append("\n");
		source.append("\t//originally get_at(int) const\n");
		source.append( MessageFormat.format("\tpublic {0} constGetAt( final int index_value ) '{'\n", ofTypeName ) );
		source.append("\t\tif ( !isBound().getValue() ) {\n");
		source.append( MessageFormat.format( "\t\t\tthrow new TtcnError( \"Accessing an element in an unbound value of type {0}.\" );\n", displayName ) );
		source.append("\t\t}\n");
		source.append("\t\tif (index_value < 0) {\n");
		source.append( MessageFormat.format( "\t\t\tthrow new TtcnError( \"Accessing an element of type {0} using a negative index: \"+index_value+\".\");\n", displayName ) );
		source.append("\t\t}\n");
		source.append("\t\tfinal int nofElements = getNofElements().getInt();\n");
		source.append("\t\tif ( index_value >= nofElements ) {\n");
		source.append( MessageFormat.format( "\t\t\tthrow new TtcnError( \"Index overflow in a value of type {0}: The index is \"+index_value+\", but the value has only \"+nofElements+\" elements.\" );\n", displayName ) );
		source.append("\t\t}\n");
		source.append("\n");
		source.append( MessageFormat.format( "\t\tfinal {0} elem = valueElements.get( index_value );\n", ofTypeName ) );
		source.append("\t\treturn ( elem != null ) ? elem : getUnboundElem();\n");
		source.append("\t}\n");

		source.append("\n");
		source.append("\t//originally get_at(const INTEGER&) const\n");
		source.append( MessageFormat.format( "\tpublic {0} constGetAt(final TitanInteger index_value) '{'\n", ofTypeName ) );
		source.append( MessageFormat.format( "\t\tindex_value.mustBound( \"Using an unbound integer value for indexing a value of type {0}.\" );\n", displayName ) );
		source.append("\t\treturn constGetAt( index_value.getInt() );\n");
		source.append("\t}\n");

		source.append("\n");
		source.append("\tpublic TitanInteger sizeOf() {\n");
		source.append( MessageFormat.format( "\t\tmustBound(\"Performing sizeof operation on an unbound value of type {0}.\");\n", displayName ) );
		source.append("\t\treturn new TitanInteger(valueElements.size());\n");
		source.append("\t}\n");

		source.append("\n");
		source.append("\tpublic TitanInteger getNofElements() {\n");
		source.append("\t\treturn sizeOf();\n");
		source.append("\t}\n");

		source.append("\n");
		source.append("\tpublic TitanInteger lengthOf() {\n");
		source.append( MessageFormat.format( "\t\tmustBound(\"Performing lengthof operation on an unbound value of type {0}.\");\n", displayName ) );
		source.append("\t\tfor ( int i = valueElements.size() - 1; i >= 0; i-- ) {\n");
		source.append( MessageFormat.format( "\t\t\t{0} elem = valueElements.get( i );\n", ofTypeName ) );
		source.append("\t\t\tif ( elem != null && elem.isBound().getValue() ) {\n");
		source.append("\t\t\t\treturn new TitanInteger(i + 1);\n");
		source.append("\t\t\t}\n");
		source.append("\t\t}\n");
		source.append("\t\treturn new TitanInteger(0);\n");
		source.append("\t}\n");

		source.append("\n");
		//FIXME eddig nem volt ilyen API -nk
		source.append( MessageFormat.format( "\tpublic void add( final {0} aElement ) '{'\n", ofTypeName ) );
		source.append("\t\tif ( valueElements == null ) {\n");
		source.append( MessageFormat.format( "\t\t\tvalueElements = new ArrayList<{0}>();\n", ofTypeName ) );
		source.append("\t\t}\n");
		source.append("\t\tvalueElements.add( aElement );\n");
		source.append("\t}\n");

		source.append("\n");
		source.append("\tpublic void setSize(final int newSize) {\n");
		source.append("\t\tif (newSize < 0) {\n");
		source.append( MessageFormat.format( "\t\t\tthrow new TtcnError(\"Internal error: Setting a negative size for a value of type {0}.\");\n", displayName ) );
		source.append("\t\t}\n");
		source.append("\t\tif ( valueElements == null ) {\n");
		source.append( MessageFormat.format( "\t\t\tvalueElements = new ArrayList<{0}>();\n", ofTypeName ) );
		source.append("\t\t}\n");
		source.append("\t\tif (newSize > valueElements.size()) {\n");
		source.append("\t\t\tfor ( int i = valueElements.size(); i < newSize; i++ ) {\n");
		source.append("\t\t\t\tvalueElements.add( null );\n");
		source.append("\t\t\t}\n");
		source.append("\t\t} else if (newSize < valueElements.size()) {\n");
		source.append("\t\t\twhile(valueElements.size() > newSize) {\n");
		source.append("\t\t\t\tvalueElements.remove(valueElements.size()-1);\n");
		source.append("\t\t\t}\n");
		source.append("\t\t}\n");
		source.append("\t}\n");
	}

	private static void generateValueGetUnboundElem(StringBuilder source, String ofTypeName) {
		source.append("\n");
		source.append( MessageFormat.format( "\tprivate static {0} getUnboundElem() '{'\n", ofTypeName ) );
		source.append( MessageFormat.format( "\t\treturn new {0}();\n", ofTypeName ) );
		source.append("\t}\n");
	}

	private static void generateValueToString(StringBuilder source) {
		source.append("\n");
		source.append("\t@Override\n");
		source.append("\tpublic String toString() {\n");
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
		source.append("\n");
		source.append( MessageFormat.format( "\tpublic {0} substr(int index, int returncount) '{'\n", genName ) );
		source.append("\t\tif (valueElements == null) {\n");
		source.append( MessageFormat.format( "\t\t\tthrow new TtcnError(\"The first argument of substr() is an unbound value of type {0}.\");\n", displayName ) );
		source.append("\t\t}\n");
		source.append( MessageFormat.format( "\t\tAdditionalFunctions.check_substr_arguments(valueElements.size(), index, returncount, \"{0}\",\"element\");\n", displayName ) );
		source.append( MessageFormat.format( "\t\t{0} ret_val = new {0}();\n", genName ) );
		source.append("\t\tret_val.setSize(returncount);\n");
		source.append("\t\tfor (int i=0; i<returncount; i++) {\n");
		source.append("\t\t\tif (valueElements.get(i+index) != null) {\n");
		source.append( MessageFormat.format( "\t\t\t\tret_val.valueElements.set(i, new {0}(valueElements.get(i+index)));\n", ofTypeName ) );
		source.append("\t\t\t}\n");
		source.append("\t\t}\n");
		source.append("\t\treturn ret_val;\n");
		source.append("\t}\n");

		source.append("\n");
		source.append( MessageFormat.format( "\tpublic {0} replace(int index, int len, final {0} repl) '{'\n", genName ) );
		source.append("\t\tif (valueElements == null) {\n");
		source.append( MessageFormat.format( "\t\t\tthrow new TtcnError(\"The first argument of replace() is an unbound value of type {0}.\");\n", displayName ) );
		source.append("\t\t}\n");
		source.append("\t\tif (repl.valueElements == null) {\n");
		source.append( MessageFormat.format( "\t\t\tthrow new TtcnError(\"The fourth argument of replace() is an unbound value of type {0}.\");\n", displayName ) );
		source.append("\t\t}\n");
		source.append( MessageFormat.format( "\t\tAdditionalFunctions.check_replace_arguments(valueElements.size(), index, len, \"{0}\",\"element\");\n", displayName ) );
		source.append( MessageFormat.format( "\t\t{0} ret_val = new {0}();\n", genName ) );
		source.append("\t\tret_val.setSize(valueElements.size() + repl.valueElements.size() - len);\n");
		source.append("\t\tfor (int i = 0; i < index; i++) {\n");
		source.append("\t\t\tif (valueElements.get(i) != null) {\n");
		source.append( MessageFormat.format( "\t\t\t\tret_val.valueElements.set(i, new {0}(valueElements.get(i)));\n", ofTypeName ) );
		source.append("\t\t\t}\n");
		source.append("\t\t}\n");
		source.append("\t\tfor (int i = 0; i < repl.valueElements.size(); i++) {\n");
		source.append("\t\t\tif (repl.valueElements.get(i) != null) {\n");
		source.append( MessageFormat.format( "\t\t\t\tret_val.valueElements.set(i+index, new {0}(repl.valueElements.get(i)));\n", ofTypeName ) );
		source.append("\t\t\t}\n");
		source.append("\t\t}\n");
		source.append("\t\tfor (int i = 0; i < valueElements.size() - index - len; i++) {\n");
		source.append("\t\t\tif (valueElements.get(index+i+len) != null) {\n");
		source.append( MessageFormat.format( "\t\t\t\tret_val.valueElements.set(index+i+repl.valueElements.size(), new {0}(valueElements.get(index+i+len)));\n", ofTypeName ) );
		source.append("\t\t\t}\n");
		source.append("\t\t}\n");
		source.append("\t\treturn ret_val;\n");
		source.append("\t}\n");

		source.append("\n");
		source.append( MessageFormat.format( "\tpublic {0} replace(int index, int len, final {0}_template repl) '{'\n", genName ) );
		source.append("\t\tif (!repl.isValue().getValue()) {\n");
		source.append("\t\t\tthrow new TtcnError(\"The fourth argument of function replace() is a template with non-specific value.\");\n");
		source.append("\t\t}\n");
		source.append("\t\treturn replace(index, len, repl.valueOf());\n");
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
		source.append("\n");
		//TODO: remove
		source.append( MessageFormat.format( "\t{0} single_value;\n", genName ) );

		source.append("\t//originally single_value/value_elements\n");
		source.append( MessageFormat.format( "\tList<{0}> value_elements;\n", ofTypeName ) );
		source.append("\n");

		source.append("\t//originally value_list/list_value\n");
		source.append( MessageFormat.format( "\tList<{0}_template> list_value;\n", genName ) );
		
		source.append("\n");
		source.append("\tprivate match_function_t match_function_specific = new match_function_t() {\n");
		source.append("\t\t@Override\n");
		source.append("\t\tpublic boolean match(Base_Type value_ptr, int value_index, Restricted_Length_Template template_ptr, int template_index, boolean legacy) {\n");
		source.append( MessageFormat.format( "\t\t\treturn match_index(({0})value_ptr, value_index, ({0}_template)template_ptr, template_index, legacy);\n", genName ) );
		source.append("\t\t}\n");
		source.append("\t};\n");
	}

	/**
	 * Generate constructors for template
	 *
	 * @param source where the source code is to be generated.
	 * @param genName the name of the generated class representing the "record of/set of" type.
	 * @param displayName the user readable name of the type to be generated.
	 */
	private static void generateTemplateConstructors( final StringBuilder source, final String genName, final String displayName ) {
		source.append("\n");
		source.append( MessageFormat.format( "\tpublic {0}_template() '{'\n", genName ) );
		source.append("\t};\n");

		source.append("\n");
		source.append( MessageFormat.format( "\tpublic {0}_template(final template_sel other_value ) '{'\n", genName));
		source.append("\t\tsuper( other_value );\n");
		source.append("\t\tcheckSingleSelection( other_value );\n");
		source.append("\t}\n");

		source.append("\n");
		source.append( MessageFormat.format( "\tpublic {0}_template( final {0} otherValue ) '{'\n", genName ) );
		source.append( MessageFormat.format( "\t\totherValue.mustBound(\"Copying an unbound value of type {0}.\");\n", displayName ) );
		source.append( MessageFormat.format( "\t\tsingle_value = new {0}( otherValue );\n", genName ) );
		source.append("\t\tsetSelection(template_sel.SPECIFIC_VALUE);\n");
		source.append("\t};\n");

		source.append("\n");
		source.append( MessageFormat.format( "\tpublic {0}_template( final {0}_template otherValue ) '{'\n", genName ) );
		source.append("\t\tcopyTemplate( otherValue );\n");
		source.append("\t};\n");
	}

	/**
	 * Generate the copyTemplate function for template
	 *
	 * @param source where the source code is to be generated.
	 * @param genName the name of the generated class representing the "record of/set of" type.
	 * @param displayName the user readable name of the type to be generated.
	 */
	private static void generateTemplateCopyTemplate( final StringBuilder source, final String genName, final String displayName ) {
		source.append("\n");
		source.append( MessageFormat.format( "\tprivate void copyTemplate(final {0}_template other_value) '{'\n", genName));
		source.append("\t\tswitch (other_value.templateSelection) {\n");
		source.append("\t\tcase SPECIFIC_VALUE:\n");
		source.append( MessageFormat.format( "\t\t\tsingle_value = new {0}( other_value.single_value );\n", genName ) );
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
		source.append("\n");
		source.append("\tpublic TitanBoolean isPresent() {\n");
		source.append("\t\treturn isPresent(false);\n");
		source.append("\t}\n");

		source.append("\n");
		source.append("\tpublic TitanBoolean isPresent(final boolean legacy) {\n");
		source.append("\t\tif (templateSelection == template_sel.UNINITIALIZED_TEMPLATE) {\n");
		source.append("\t\t\treturn new TitanBoolean(false);\n");
		source.append("\t\t}\n");
		source.append("\t\treturn new TitanBoolean(!match_omit(legacy).getValue());\n");
		source.append("\t}\n");
	}

	/**
	 * Generate the match function
	 *
	 * @param source where the source code is to be generated.
	 * @param genName the name of the generated class representing the "record of/set of" type.
	 * @param displayName the user readable name of the type to be generated.
	 */
	private static void generateTemplateMatch( final StringBuilder source, final String genName, final String displayName ) {
		source.append("\n");
		source.append("\t// originally match\n");
		source.append( MessageFormat.format( "\tpublic TitanBoolean match(final {0} other_value) '{'\n", genName ) );
		source.append("\t\treturn match(other_value, false);\n");
		source.append("\t}\n");

		source.append("\n");
		source.append("\t// originally match\n");
		source.append( MessageFormat.format( "\tpublic TitanBoolean match(final {0} other_value, final boolean legacy) '{'\n", genName ) );
		source.append("\t\tif(!other_value.isBound().getValue()) {\n");
		source.append("\t\t\treturn new TitanBoolean(false);\n");
		source.append("\t\t}\n");
		source.append("\t\tswitch (templateSelection) {\n");
		source.append("\t\tcase ANY_VALUE:\n");
		source.append("\t\tcase ANY_OR_OMIT:\n");
		source.append("\t\t\treturn new TitanBoolean(true);\n");
		source.append("\t\tcase OMIT_VALUE:\n");
		source.append("\t\t\treturn new TitanBoolean(false);\n");
		source.append("\t\tcase SPECIFIC_VALUE:\n");
		source.append("\t\t\treturn new TitanBoolean( RecordOfMatch.match_record_of(other_value, other_value.sizeOf().getInt(), this, value_elements.size(), match_function_specific, legacy) );\n");
		source.append("\t\tcase VALUE_LIST:\n");
		source.append("\t\tcase COMPLEMENTED_LIST:\n");
		source.append("\t\t\tfor(int i = 0 ; i < list_value.size(); i++) {\n");
		source.append("\t\t\t\tif(list_value.get(i).match(other_value, legacy).getValue()) {\n");
		source.append("\t\t\t\t\treturn new TitanBoolean(templateSelection == template_sel.VALUE_LIST);\n");
		source.append("\t\t\t\t}\n");
		source.append("\t\t\t}\n");
		source.append("\t\t\treturn new TitanBoolean(templateSelection == template_sel.COMPLEMENTED_LIST);\n");
		source.append("\t\tdefault:\n");
		source.append( MessageFormat.format( "\t\t\tthrow new TtcnError(\"Matching with an uninitialized/unsupported template of type {0}.\");\n", displayName ) );
		source.append("\t\t}\n");
		source.append("\t}\n");
		
		source.append("\n");
		source.append( MessageFormat.format( "\tprivate boolean match_index(final {0} value_ptr, int value_index, {0}_template template_ptr, int template_index, boolean legacy) '{'\n", genName ) );
		source.append("\t\tif (value_index >= 0) {\n");
		source.append("\t\t\treturn template_ptr.value_elements.get(template_index).match(value_ptr.valueElements.get(value_index), legacy).getValue();\n");
		source.append("\t\t} else {\n");
		source.append("\t\t\treturn template_ptr.value_elements.get(template_index).is_any_or_omit();\n");
		source.append("\t\t}\n");
		source.append("\t}\n");
	}

	/**
	 * Generate the match_omit function
	 *
	 * @param source: where the source code is to be generated.
	 */
	private static void generateTemplateMatchOmit( final StringBuilder source ) {
		source.append("\n");
		source.append("\tpublic TitanBoolean match_omit(final boolean legacy) {\n");
		source.append("\t\tif (is_ifPresent) {\n");
		source.append("\t\t\treturn new TitanBoolean(true);\n");
		source.append("\t\t}\n");
		source.append("\t\tswitch(templateSelection) {\n");
		source.append("\t\tcase OMIT_VALUE:\n");
		source.append("\t\tcase ANY_OR_OMIT:\n");
		source.append("\t\t\treturn new TitanBoolean(true);\n");
		source.append("\t\tcase VALUE_LIST:\n");
		source.append("\t\tcase COMPLEMENTED_LIST:\n");
		source.append("\t\t\tif (legacy) {\n");
		source.append("\t\t\t\tfor (int i = 0 ; i < list_value.size(); i++) {\n");
		source.append("\t\t\t\t\tif (list_value.get(i).match_omit(legacy).getValue()) {\n");
		source.append("\t\t\t\t\t\treturn new TitanBoolean(templateSelection == template_sel.VALUE_LIST);\n");
		source.append("\t\t\t\t\t}\n");
		source.append("\t\t\t\t}\n");
		source.append("\t\t\t\treturn new TitanBoolean(templateSelection == template_sel.COMPLEMENTED_LIST);\n");
		source.append("\t\t\t}\n");
		source.append("\t\t\treturn new TitanBoolean(false);\n");
		source.append("\t\tdefault:\n");
		source.append("\t\t\treturn new TitanBoolean(false);\n");
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
		source.append("\n");
		source.append("\t//originally operator=\n");
		source.append( MessageFormat.format( "\tpublic {0}_template assign( final template_sel other_value ) '{'\n", genName ) );
		source.append("\t\tcheckSingleSelection(other_value);\n");
		source.append("\t\tcleanUp();\n");
		source.append("\t\tsetSelection(other_value);\n");
		source.append("\t\treturn this;\n");
		source.append("\t}\n");

		source.append("\n");
		source.append("\t//originally operator=\n");
		source.append( MessageFormat.format( "\tpublic {0}_template assign( final {0} other_value ) '{'\n", genName ) );
		source.append("\t\tcleanUp();\n");
		source.append( MessageFormat.format( "\t\tsingle_value = new {0}( other_value );\n", genName ) );
		source.append("\t\tsetSelection(template_sel.SPECIFIC_VALUE);\n");
		source.append("\t\treturn this;\n");
		source.append("\t}\n");

		source.append("\n");
		source.append("\t//originally operator=\n");
		source.append( MessageFormat.format( "\tpublic {0}_template assign( final {0}_template other_value ) '{'\n", genName ) );
		source.append("\t\tif (other_value != this) {\n");
		source.append("\t\t\tcleanUp();\n");
		source.append("\t\t\tcopyTemplate(other_value);\n");
		source.append("\t\t}\n");
		source.append("\t\treturn this;\n");
		source.append("\t}\n");

		source.append("\n");
		source.append("\t@Override\n");
		source.append( MessageFormat.format( "\tpublic {0}_template assign(final Base_Type otherValue) '{'\n", genName ) );
		source.append( MessageFormat.format( "\tif (otherValue instanceof {0}) '{'\n", genName) );
		source.append( MessageFormat.format( "\t\treturn assign(({0})otherValue);\n", genName) );
		source.append("\t}\n\n");
		source.append( MessageFormat.format( "\t\tthrow new TtcnError(\"Internal Error: The left operand of assignment is not of type {0}.\");\n", genName ) );
		source.append("\t}\n");

		source.append("\n");
		source.append("\t@Override\n");
		source.append( MessageFormat.format( "\tpublic {0}_template assign(final Base_Template otherValue) '{'\n", genName ) );
		source.append( MessageFormat.format( "\tif (otherValue instanceof {0}_template) '{'\n", genName) );
		source.append( MessageFormat.format( "\t\treturn assign(({0}_template)otherValue);\n", genName) );
		source.append("\t}\n\n");
		source.append( MessageFormat.format( "\t\tthrow new TtcnError(\"Internal Error: The left operand of assignment is not of type {0}_template.\");\n", genName ) );
		source.append("\t}\n");
		
		//TODO: implement optional parameter version
	}

	/**
	 * Generate the clean_up function for template
	 *
	 * @param source where the source code is to be generated.
	 */
	private static void generateTemplateCleanup(final StringBuilder source) {
		source.append("\n");
		source.append("\t@Override\n");
		source.append("\tpublic void cleanUp() {\n");
		source.append("\t\tswitch(templateSelection) {\n");
		source.append("\t\tcase SPECIFIC_VALUE:\n");
		source.append("\t\t\tsingle_value = null;\n");
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
	private static void generateTemplateReplace(StringBuilder source, final String genName, final String displayName) {
 		source.append("\n");
		source.append( MessageFormat.format( "\tpublic {0} replace(int index, int len, final {0}_template repl) '{'\n", genName ) );
		source.append("\t\tif (!isValue().getValue()) {\n");
		source.append("\t\t\tthrow new TtcnError(\"The first argument of function replace() is a template with non-specific value.\");\n");
		source.append("\t\t}\n");
		source.append("\t\tif (!repl.isValue().getValue()) {\n");
		source.append("\t\t\tthrow new TtcnError(\"The fourth argument of function replace() is a template with non-specific value.\");\n");
		source.append("\t\t}\n");
		source.append("\t\treturn valueOf().replace(index, len, repl.valueOf());\n");
		source.append("\t}\n");

 		source.append("\n");
		source.append( MessageFormat.format( "\tpublic {0} replace(int index, int len, final {0} repl) '{'\n", genName ) );
		source.append("\t\tif (!isValue().getValue()) {\n");
		source.append("\t\t\tthrow new TtcnError(\"The first argument of function replace() is a template with non-specific value.\");\n");
		source.append("\t\t}\n");
		source.append("\t\treturn valueOf().replace(index, len, repl);\n");
		source.append("\t}\n");

//TODO: remove
/*
 		source.append("\n");
		source.append( MessageFormat.format( "\tprivate void substr_(int index, int returncount, {0} rec_of) '{'\n", genName ) );
		source.append("\t\tif (!isValue().getValue()) {\n");
		source.append( MessageFormat.format( "\t\t\tthrow new TtcnError(\"The first argument of function substr() is a template of type {0} with non-specific value.\");\n", displayName ) );
		source.append("\t\t}\n");
		source.append("\n");
		source.append("\t\trec_of.valueElements = null;\n");
		source.append( MessageFormat.format( "\t\t{0}_template this_value = new {0}_template();\n", genName ) );
		source.append("\t\tvalueofv(this_value);\n");
		source.append("\t\tthis_value.substr_(index, returncount, rec_of);\n");
		source.append("\t}\n");

		source.append("\n");
		source.append( MessageFormat.format( "\tprivate void replace_(int index, int len, final {0}_template repl, {0} rec_of) '{'\n", genName ) );
		source.append("\t\tif (!isValue().getValue()) {\n");
		//TODO: fix: "third" instead of "first", also in titan.core
		source.append( MessageFormat.format( "\t\t\tthrow new TtcnError(\"The first argument of function replace() is a template of type {0} with non-specific value.\");\n", displayName ) );
		source.append("\t\t}\n");
		source.append("\n");
		source.append("\t\tif (!repl.isValue().getValue()) {\n");
		source.append( MessageFormat.format( "\t\t\tthrow new TtcnError(\"The fourth argument of function replace() is a template of type {0} with non-specific value.\");\n", displayName ) );
		source.append("\t\t}\n");
		source.append("\n");
		source.append("\t\trec_of.valueElements = null;\n");
		source.append( MessageFormat.format( "\t\t{0}_template this_value = new {0}_template();\n", genName ) );
		source.append("\t\tvalueofv(this_value);\n");
		source.append( MessageFormat.format( "\t\t{0}_template repl_value = new {0}_template();\n", genName ) );
		source.append("\t\trepl.valueofv(repl_value);\n");
		source.append("\t\t// call the replace() function of the value class instance\n");
		source.append("\t\tthis_value.replace_(index, len, repl_value, rec_of);\n");
		source.append("\t}\n");

		source.append("\n");
		source.append( MessageFormat.format( "\tprivate void replace_(int index, int len, final {0} repl, {0} rec_of) '{'\n", genName ) );
		source.append("\t\tif (!isValue().getValue()) {\n");
		//TODO: fix: "third" instead of "first", also in titan.core
		source.append( MessageFormat.format( "\t\t\tthrow new TtcnError(\"The first argument of function replace() is a template of type {0} with non-specific value.\");\n", displayName ) );
		source.append("\t\t}\n");
		source.append("\n");
		source.append("\t\trec_of.valueElements = null;\n");
		source.append( MessageFormat.format( "\t\t{0}_template this_value = new {0}_template();\n", genName ) );
		source.append("\t\tvalueofv(this_value);\n");
		source.append("\t\t// call the replace() function of the value class instance\n");
		source.append("\t\tthis_value.replace_(index, len, repl, rec_of);\n");
		source.append("\t}\n");
		
		source.append("\n");
		source.append( MessageFormat.format( "\tprivate void valueofv(final {0}_template value) '{'\n", genName ) );
		source.append("\t\tif (templateSelection != template_sel.SPECIFIC_VALUE || is_ifPresent) {\n");
		source.append( MessageFormat.format( "\t\t\tthrow new TtcnError(\"Performing a valueof or send operation on a non-specific template of type {0}.\");\n", displayName ) );  
		source.append("\t\t}\n");
		source.append("\t\tvalue.setSize(value_elements.size());\n");
		source.append("\t\tfor (int elem_count = 0; elem_count < value_elements.size(); elem_count++) {\n");
		source.append("\t\t\tvalue_elements.get(elem_count).valueofv(value.getAt(elem_count));\n");
		source.append("\t\t}\n");
		source.append("\t\tvalue.set_err_descr(err_descr);\n");
		source.append("\t}\n");
*/
	}

	/**
	 * Generate getter and setter functions for template 
	 *  
	 * @param source where the source code is to be generated.
	 * @param genName the name of the generated class representing the "record of/set of" type.
	 * @param ofTypeName type name of the "record of/set of" element
	 * @param displayName the user readable name of the type to be generated.
	 */
	private static void generateTemplateGetterSetters(StringBuilder source, final String genName, final String ofTypeName, final String displayName) {
		source.append("\n");
		source.append( MessageFormat.format( "\tprivate {0} getAt(int index_value) '{'\n", ofTypeName ) );
		source.append("\t\tif (index_value < 0) {\n");
		source.append("\t\t\tthrow new TtcnError( MessageFormat.format( \"Accessing an element of a template for type "+displayName+" using a negative index: {0}.\", index_value ) );\n");
		source.append("\t\t}\n");
		source.append("\n");
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
		
		source.append("\n");
		source.append( MessageFormat.format( "\tprivate {0} getAt(final TitanInteger index_value) '{'\n", ofTypeName ) );
		source.append("\t\tif (!index_value.isBound().getValue()) {\n");
		source.append( MessageFormat.format( "\t\t\tthrow new TtcnError(\"Using an unbound integer value for indexing a template of type {0}.\");\n", displayName ) );
		source.append("\t\t}\n");
		source.append("\n");
		source.append("\t\treturn getAt(index_value.getInt());\n");
		source.append("\t}\n");
		
		source.append("\n");
		source.append( MessageFormat.format( "\tprivate {0} constGetAt(int index_value) '{'\n", ofTypeName ) );
		source.append("\t\tif (index_value < 0) {\n");
		source.append("\t\t\tthrow new TtcnError( MessageFormat.format( \"Accessing an element of a template for type "+displayName+" using a negative index: {0}.\", index_value ) );\n");
		source.append("\t\t}\n");
		source.append("\n");
		source.append("\t\tif (templateSelection != template_sel.SPECIFIC_VALUE) {\n");
		source.append( MessageFormat.format( "\t\t\tthrow new TtcnError(\"Accessing an element of a non-specific template for type {0}.\");\n", displayName ) );
		source.append("\t\t}\n");
		source.append("\n");
		source.append("\t\tif (index_value >= value_elements.size()) {\n");
		source.append("\t\t\tthrow new TtcnError( MessageFormat.format( \"Index overflow in a template of type "+displayName+": The index is {0}, but the template has only {1} elements.\", index_value, value_elements.size() ) );\n");
		source.append("\t\t}\n");
		source.append("\n");
		source.append("\t\treturn value_elements.get(index_value);\n");
		source.append("\t}\n");
		
		source.append("\n");
		source.append( MessageFormat.format( "\tprivate {0} constGetAt(final TitanInteger index_value) '{'\n", ofTypeName ) );
		source.append("\t\tif (!index_value.isBound().getValue()) {\n");
		source.append( MessageFormat.format( "\t\t\tthrow new TtcnError(\"Using an unbound integer value for indexing a template of type {0}.\");\n", displayName ) );
		source.append("\t\t}\n");
		source.append("\n");
		source.append("\t\treturn constGetAt(index_value.getInt());\n");
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
	private static void generateTemplateConcat(StringBuilder source, final String genName, final String ofTypeName, final String displayName) {
		source.append("\n");
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
		
		source.append("\n");
		source.append( MessageFormat.format( "\tprivate int get_length_for_concat(final {0} operand) '{'\n", genName ) );
		source.append("\t\toperand.mustBound(\"Operand of record of template concatenation is an unbound value.\");\n");
		source.append("\t\treturn operand.valueElements.size();\n");
		source.append("\t}\n");

		source.append("\n");
		source.append("\n\tprivate int get_length_for_concat(template_sel operand) {");
		source.append("\t\tif (operand == template_sel.ANY_VALUE) {\n");
		source.append("\t\t\t// ? => { * }\n");
		source.append("\t\t\treturn 1;\n");
		source.append("\t\t}\n");
		source.append("\t\tthrow new TtcnError(\"Operand of record of template concatenation is an uninitialized or unsupported template.\");\n");
		source.append("\t}\n");

		source.append("\n");
		//TODO: simplify, just use value_elements.add()
		source.append("\t\t//TODO: simplify, just use value_elements.add()\n");
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
	 */
	private static void generateTemplateSetSize(StringBuilder source, final String genName, final String ofTypeName, final String displayName) {
		source.append("\n");
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
		
		source.append("\n");
		source.append("\tpublic TitanInteger sizeOf() {\n");
		source.append("\t\treturn sizeOf(true);\n");
		source.append("\t}\n");

		source.append("\n");
		source.append("\tpublic TitanInteger lengthOf() {\n");
		source.append("\t\treturn sizeOf(false);\n");
		source.append("\t}\n");

		source.append("\n");
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
		source.append("\t\t\t\twhile (elem_count>0 && !(value_elements.get(elem_count-1)).isBound().getValue())\n");
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
	private static void generateTemplateNElem(StringBuilder source, final String genName) {
		source.append("\n");
		source.append("\tpublic TitanInteger nofElements() {\n");
		source.append("\t\tswitch (templateSelection) {\n");
		source.append("\t\tcase SPECIFIC_VALUE:\n");
		source.append("\t\t\treturn single_value.getNofElements();\n");
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
	private static void generateTemplateMatchv(StringBuilder source, final String genName) {
		source.append("\n");
		source.append( MessageFormat.format( "\tprivate boolean matchv(final {0} other_value, boolean legacy) '{'\n", genName ) );
		source.append("\t\tif (!other_value.isBound().getValue()) {\n");
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
	private static void generateTemplateIsValue(StringBuilder source, final String genName) {
		source.append("\n");
		source.append("\t@Override\n");
		source.append("\tpublic TitanBoolean isValue() {\n");
		source.append("\t\tif (templateSelection != template_sel.SPECIFIC_VALUE || is_ifPresent) {\n");
		source.append("\t\t\treturn new TitanBoolean(false);\n");
		source.append("\t\t}\n");
		source.append("\t\tfor (int elem_count = 0; elem_count < value_elements.size(); elem_count++) {\n");
		source.append("\t\t\tif (!value_elements.get(elem_count).isValue().getValue()) return new TitanBoolean(false);\n");
		source.append("\t\t}\n");
		source.append("\t\treturn new TitanBoolean(true);\n");
		source.append("\t}\n");
	}

	/**
	 * Generate set_type function for template
	 *  
	 * @param source where the source code is to be generated.
	 * @param genName the name of the generated class representing the "record of/set of" type.
	 */
	private static void generateTemplateSetType(StringBuilder source, final String genName) {
		source.append("\n");
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
		source.append("\t\tdefault:\n");
		source.append( MessageFormat.format( "\t\t\tthrow new TtcnError(\"Internal error: Setting an invalid type for a template of type {0}.\");\n", genName ) );
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
		aSb.append("\n");
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
	private static void generateTemplateGetListItem(StringBuilder source, final String genName, final String displayName) {
		source.append("\n");
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
		aSb.append("\n");
		aSb.append( MessageFormat.format( "\t\tpublic {0} valueOf() '{'\n", genName ) );
		aSb.append("\t\t\tif (templateSelection != template_sel.SPECIFIC_VALUE || is_ifPresent) {\n");
		aSb.append( MessageFormat.format( "\t\t\t\tthrow new TtcnError(\"Performing a valueof or send operation on a non-specific template of type {0}.\");\n", displayName ) );
		aSb.append("\t\t\t}\n");
		aSb.append( MessageFormat.format( "\t\t\t{0} ret_val = new {0}();\n", genName ) );
		aSb.append("\t\t\tfor (int elem_count = 0; elem_count < value_elements.size(); elem_count++) {\n");
		aSb.append("\t\t\t\tif (value_elements.get(elem_count).isBound().getValue()) {\n");
		aSb.append("\t\t\t\t\tret_val.add( value_elements.get(elem_count).valueOf() );\n");
		aSb.append("\t\t\t\t}\n");
		aSb.append("\t\t\t}\n");
		aSb.append("\t\t\treturn ret_val;\n");
		aSb.append("\t\t}\n");
	}

//TODO: implement log()
/*
	void log() {
		switch (templateSelection) {
		case SPECIFIC_VALUE:
			if (value_elements.size() > 0) {
				TtcnLogger.log_event_str("{ ");
				for (int elem_count = 0; elem_count < value_elements.size(); elem_count++) {
					if (elem_count > 0) TtcnLogger.log_event_str(", ");
					if (permutation_starts_at(elem_count)) TtcnLogger.log_event_str("permutation(");
					value_elements.get(elem_count).log();
					if (permutation_ends_at(elem_count)) TtcnLogger.log_char(')');
				}
				TtcnLogger.log_event_str(" }");
			} else TtcnLogger.log_event_str("{ }");
			break;
		case COMPLEMENTED_LIST:
			TtcnLogger.log_event_str("complement ");
			// no break
		case VALUE_LIST:
			TtcnLogger.log_char('(');
			for (int list_count = 0; list_count < list_value.size(); list_count++) {
				if (list_count > 0) TtcnLogger.log_event_str(", ");
				list_value.get(list_count).log();
			}
			TtcnLogger.log_char(')');
			break;
		default:
			log_generic();
			break;
		}
		log_restricted();
		log_ifpresent();
		if (err_descr) err_descr.log();
	}
//TODO
//*/

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
	private static void generateTemplateGetIstemplateKind(StringBuilder source, final String genName) {
		source.append("\n");
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
	 * @param genName the name of the generated class representing the "record of/set of" type.
	 */
	private static void generateTemplateCheckRestriction(StringBuilder source, final String genName) {
		source.append("\n");
		source.append("\tvoid check_restriction(template_res t_res, final String t_name, boolean legacy) {\n");
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
		source.append("\t\t\t\tvalue_elements.get(i).check_restriction(t_res, t_name != null ? t_name : \""+genName+"\");\n");
		source.append("\t\t\treturn;\n");
		source.append("\t\tcase TR_PRESENT:\n");
		source.append("\t\t\tif (!match_omit(legacy).getValue()) return;\n");
		source.append("\t\t\tbreak;\n");
		source.append("\t\tdefault:\n");
		source.append("\t\t\treturn;\n");
		source.append("\t\t}\n");
		source.append("\t\tthrow new TtcnError( MessageFormat.format( \"Restriction `{0}' on template of type {1} violated.\", get_res_name(t_res), t_name != null ? t_name : \""+genName+"\" ) );\n");
		source.append("\t}\n");
	}
}
