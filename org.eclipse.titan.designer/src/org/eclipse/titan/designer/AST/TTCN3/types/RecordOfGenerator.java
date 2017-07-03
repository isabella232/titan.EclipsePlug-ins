package org.eclipse.titan.designer.AST.TTCN3.types;

import java.text.MessageFormat;

import org.eclipse.titan.designer.compiler.JavaGenData;

/**
 * Utility class for generating the value and template classes for "record of" types.
 *
 * @author Arpad Lovassy
 */
public class RecordOfGenerator {

	/**
	 * Generate "record of" class
	 * @param aData only used to update imports if needed.
	 * @param source where the source code is to be generated.
	 * @param genName the name of the generated class representing the "record of" type.
	 * @param displayName the user readable name of the type to be generated.
	 * @param ofTypeName type name of the "record of" element 
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
		source.append( MessageFormat.format( "public static class {0} extends Base_Type '{'\n", genName ) );

		generateValueDeclaration( source, genName, ofTypeName );
		generateValueConstructors( source, genName, displayName );
		generateValueCopyList( source, ofTypeName );
		generateValueIsPresent( source );
		generateValueIsBound( source );
		generateValueOperatorEquals( source, genName, ofTypeName );
		generateValueAssign( source, genName );
		generateValueCleanup( source );
		generateValueGetterSetters( source, ofTypeName );
		generateValueGetUnboundElem( source, ofTypeName );

		source.append("}\n");
	}

	/**
	 * Generate member variables
	 *
	 * @param source where the source code is to be generated.
	 * @param genName the name of the generated class representing the "record of" type.
	 * @param ofTypeName type name of the "record of" element 
	 */
	private static void generateValueDeclaration( final StringBuilder source, final String genName, final String ofTypeName ) {
		source.append("\n");
		source.append( MessageFormat.format( "\tList<{0}> valueElements;\n", ofTypeName ) );
	}

	/**
	 * Generate constructors
	 *
	 * @param source where the source code is to be generated.
	 * @param genName the name of the generated class representing the "record of" type.
	 * @param displayName the user readable name of the type to be generated.
	 */
	private static void generateValueConstructors( final StringBuilder source, final String genName, final String displayName ) {
		source.append("\n");
		source.append( MessageFormat.format( "\tpublic {0}() '{'\n", genName ) );
		source.append("\t};\n");
		source.append("\n");
		source.append( MessageFormat.format( "\tpublic {0}( final {0} otherValue ) '{'\n", genName ) );
		source.append( MessageFormat.format( "\t\totherValue.mustBound(\"Copying an unbound {0} value.\");\n", displayName ) );
		source.append("\t\tvalueElements = copyList( otherValue.valueElements );\n");
		source.append("\t};\n");
	}

	/**
	 * Generate the copyList function
	 *
	 * @param source where the source code is to be generated.
	 * @param ofTypeName type name of the "record of" element 
	 */
	private static void generateValueCopyList( final StringBuilder source, final String ofTypeName ) {
		source.append("\n");
		source.append( MessageFormat.format( "\tpublic final List<{0}> copyList( final List<{0}> srcList ) '{'\n", ofTypeName ) );
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
		source.append("\n");
		source.append("\t@Override\n");
		source.append("\tpublic boolean isBound() {\n");
		source.append("\t\treturn valueElements != null;\n");
		source.append("\t}\n");	
		source.append("\n");
		source.append("\tpublic void mustBound( final String aErrorMessage ) {\n");
		source.append("\t\tif ( !isBound() ) {\n");
		source.append("\t\t\tthrow new TtcnError( aErrorMessage );\n");
		source.append("\t\t}\n");
		source.append("\t}\n");
	}

	/**
	 * Generate assignment operators
	 *
	 * @param source where the source code is to be generated.
	 * @param genName the name of the generated class representing the "record of" type.
	 * @param ofTypeName type name of the "record of" element 
	 */
	private static void generateValueOperatorEquals( final StringBuilder source, final String genName, final String ofTypeName ) {
		source.append("\n");
		source.append("\t@Override\n");
		source.append("\tpublic TitanBoolean operatorEquals(Base_Type otherValue) {\n");
		source.append("\n");
		source.append( MessageFormat.format( "\t\treturn operatorEquals( ( {0} ) otherValue );\n", genName ) );
		source.append("\t}\n");
		source.append("\n");
		source.append("\t//originally operator==\n");
		source.append( MessageFormat.format( "\tpublic TitanBoolean operatorEquals( final {0} otherValue ) '{'\n", genName ) );
		source.append( MessageFormat.format( "\t\tmustBound(\"The left operand of comparison is an unbound value of type record of {0}.\");\n", ofTypeName ) );
		source.append( MessageFormat.format( "\t\totherValue.mustBound(\"The right operand of comparison is an unbound value of type {0}.\");\n", ofTypeName ) );
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
	 * @param genName the name of the generated class representing the "record of" type.
	 */
	private static void generateValueAssign( final StringBuilder source, final String genName ) {
		source.append("\n");
		source.append("\t@Override\n");
		source.append( MessageFormat.format( "\tpublic {0} assign(Base_Type otherValue) '{'\n", genName ) );
		source.append( MessageFormat.format( "\t\treturn assign( ( {0} ) otherValue );\n", genName ) );
		source.append("\t}\n");
		source.append("\n");
		source.append("\t//originally operator=\n");
		source.append( MessageFormat.format( "\tpublic {0} assign( final {0} aOtherValue ) '{'\n", genName ) );
		source.append("\t\taOtherValue.mustBound( \"Assignment of an unbound record of value.\" );\n");
		source.append("\n");
		source.append("\t\tvalueElements = aOtherValue.valueElements;\n");
		source.append("\t\treturn this;\n");
		source.append("\t}\n");
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
	 * @param ofTypeName type name of the "record of" element
	 */
	private static void generateValueGetterSetters(StringBuilder source, final String ofTypeName ) {
		source.append("\n");
		source.append("\t//originally get_at(int)\n");
		source.append( MessageFormat.format( "\tpublic {0} getAt( final int index_value ) '{'\n", ofTypeName ) );
		source.append("\t\tif (index_value < 0) {\n");
		source.append( MessageFormat.format( "\t\t\tthrow new TtcnError( \"Accessing an element of type record of {0} using a negative index: \"+index_value+\".\");\n", ofTypeName ) );
		source.append("\t\t}\n");
		source.append("\n");
		source.append("\t\tif (index_value >= valueElements.size() ) {\n");
		source.append("\t\t\t//increase list size\n");
		source.append("\t\t\tfor ( int i = valueElements.size(); i <= index_value; i++ ) {\n");
		source.append("\t\t\t\tvalueElements.set( index_value, null );\n");
		source.append("\t\t\t}\n");
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
		source.append( MessageFormat.format( "\tpublic {0} getAt(final TitanInteger index_value) '{'\n", ofTypeName ) );
		source.append( MessageFormat.format( "\t\tindex_value.mustBound( \"Using an unbound integer value for indexing a value of type {0}.\" );\n", ofTypeName ) );
		source.append("\t\treturn getAt( index_value.getInt() );\n");
		source.append("\t}\n");

		source.append("\n");
		source.append("\t//originally get_at(int) const\n");
		source.append( MessageFormat.format( "\tpublic {0} constGetAt( final int index_value ) '{'\n", ofTypeName ) );
		source.append("\t\tif ( !isBound() ) {\n");
		source.append( MessageFormat.format( "\t\t\tthrow new TtcnError( \"Accessing an element in an unbound value of type record of {0}.\" );\n", ofTypeName ) );
		source.append("\t\t}\n");
		source.append("\t\tif (index_value < 0) {\n");
		source.append( MessageFormat.format( "\t\t\tthrow new TtcnError( \"Accessing an element of type record of {0} using a negative index: \"+index_value+\".\");\n", ofTypeName ) );
		source.append("\t\t}\n");
		source.append("\t\tfinal int nofElements = getNofElements();\n");
		source.append("\t\tif ( index_value >= nofElements ) {\n");
		source.append( MessageFormat.format( "\t\t\tthrow new TtcnError( \"Index overflow in a value of type record of {0}: The index is \"+index_value+\", but the value has only \"+nofElements+\" elements.\" );\n", ofTypeName ) );
		source.append("\t\t}\n");
		source.append("\n");
		source.append( MessageFormat.format( "\t\tfinal {0} elem = valueElements.get( index_value );\n", ofTypeName ) );
		source.append("\t\treturn ( elem != null ) ? elem : getUnboundElem();\n");
		source.append("\t}\n");

		source.append("\n");
		source.append("\t//originally get_at(const INTEGER&) const\n");
		source.append( MessageFormat.format( "\tpublic {0} constGetAt(final TitanInteger index_value) '{'\n", ofTypeName ) );
		source.append( MessageFormat.format( "\t\tindex_value.mustBound( \"Using an unbound integer value for indexing a value of type {0}.\" );\n", ofTypeName ) );
		source.append("\t\treturn constGetAt( index_value.getInt() );\n");
		source.append("\t}\n");

		source.append("\n");
		source.append("\tpublic int getNofElements() {\n");
		source.append("\t\tif ( valueElements == null ) {\n");
		source.append("\t\t\treturn 0;\n");
		source.append("\t\t}\n");
		source.append("\t\treturn valueElements.size();\n");
		source.append("\t}\n");
		
		//TODO: lengthOf: index of the last bound value + 1

		source.append("\n");
		source.append( MessageFormat.format( "\tpublic void add( final {0} aElement ) '{'\n", ofTypeName ) );
		source.append("\t\tif ( valueElements == null ) {\n");
		source.append( MessageFormat.format( "\t\t\tvalueElements = new ArrayList<{0}>();\n", ofTypeName ) );
		source.append("\t\t}\n");
		source.append("\t\tvalueElements.add( aElement );\n");
		source.append("\t}\n");
	}

	private static void generateValueGetUnboundElem(StringBuilder source, String ofTypeName) {
		source.append("\n");
		source.append( MessageFormat.format( "\tpublic {0} getUnboundElem() '{'\n", ofTypeName ) );
		source.append( MessageFormat.format( "\t\treturn new {0}();\n", ofTypeName ) );
		source.append("\t}\n");
	}

}
