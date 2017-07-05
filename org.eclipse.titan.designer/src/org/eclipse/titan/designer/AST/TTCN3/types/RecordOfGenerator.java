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
		source.append(MessageFormat.format("public static class {0} extends Base_Type '{'\n", genName));

		generateValueDeclaration( source, genName, ofTypeName );
		generateValueConstructors( source, genName , displayName);
		generateValueCopyList( source, ofTypeName );
		generateValueIsPresent( source );
		generateValueIsBound( source );
		generateValueOperatorEquals( source, genName, ofTypeName, displayName);
		generateValueAssign( source, genName, displayName);
		generateValueCleanup( source );
		generateValueGetterSetters( source, ofTypeName, displayName );
		generateValueGetUnboundElem( source, ofTypeName );

		source.append("}\n");
	}

	/**
	 * Generate "record of" template class
	 * @param aData only used to update imports if needed.
	 * @param source where the source code is to be generated.
	 * @param genName the name of the generated class representing the "record of" type.
	 * @param displayName the user readable name of the type to be generated.
	 * @param ofTypeName type name of the "record of" element 
	 */
	public static void generateTemplateClass( final JavaGenData aData,
											  final StringBuilder source,
											  final String genName,
											  final String displayName,
											  final String ofTypeName ) {
		aData.addImport("java.util.List");
		aData.addBuiltinTypeImport("Base_Template");
		aData.addBuiltinTypeImport("TitanBoolean");
		aData.addBuiltinTypeImport("TtcnError");
		source.append( MessageFormat.format( "public static class {0}_template extends Base_Template '{'\n", genName ) );

		generateTemplateDeclaration( source, genName, ofTypeName );
		generateTemplateConstructors( source, genName, displayName );
		generateTemplateCopyTemplate( source, genName, ofTypeName, displayName );
		generateTemplateIsPresent( source );
		generateTemplateMatch( source, genName, displayName );
		generateTemplateMatchOmit( source );
		generateTemplateAssign( source, genName, displayName );
		generateTemplateCleanup( source );
		generateTemplateGetterSetters( source, ofTypeName, displayName );

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
	private static void generateValueConstructors( final StringBuilder source, final String genName, final String displayName) {
		source.append("\n");
		source.append( MessageFormat.format( "\tpublic {0}() '{'\n", genName ) );
		source.append("\t};\n");
		source.append("\n");
		source.append( MessageFormat.format( "\tpublic {0}( final {0} otherValue ) '{'\n", genName ) );
		source.append( MessageFormat.format("\t\totherValue.mustBound(\"Copying an unbound value of type {0}.\");\n", displayName ) );
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
	 * @param genName the name of the generated class representing the "record of" type.
	 * @param displayName the user readable name of the type to be generated.
	 */
	private static void generateValueAssign( final StringBuilder source, final String genName, final String displayName ) {
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
		source.append("\t\tif (index_value >= valueElements.size() ) {\n");
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
		source.append("\t\tif ( !isBound() ) {\n");
		source.append( MessageFormat.format( "\t\t\tthrow new TtcnError( \"Accessing an element in an unbound value of type {0}.\" );\n", displayName ) );
		source.append("\t\t}\n");
		source.append("\t\tif (index_value < 0) {\n");
		source.append( MessageFormat.format( "\t\t\tthrow new TtcnError( \"Accessing an element of type {0} using a negative index: \"+index_value+\".\");\n", displayName ) );
		source.append("\t\t}\n");
		source.append("\t\tfinal int nofElements = getNofElements();\n");
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
		source.append("\tpublic int getNofElements() {\n");
		source.append( MessageFormat.format( "\t\tmustBound(\"Performing sizeof operation on an unbound value of type {0}.\");\n", displayName ) );
		source.append("\t\treturn valueElements.size();\n");
		source.append("\t}\n");

		source.append("\n");
		source.append("\tpublic int lengthOf() {\n");
		source.append("\t\tif ( valueElements == null ) {\n");
		source.append("\t\t\treturn 0;\n");
		source.append("\t\t}\n");
		source.append("\t\tfor ( int i = valueElements.size() - 1; i >= 0; i-- ) {\n");
		source.append( MessageFormat.format( "\t\t\t{0} elem = valueElements.get( i );\n", ofTypeName ) );
		source.append("\t\t\tif ( elem != null && elem.isBound() ) {\n");
		source.append("\t\t\t\treturn i + 1;\n");
		source.append("\t\t\t}\n");
		source.append("\t\t}\n");
		source.append("\t\treturn 0;\n");
		source.append("\t}\n");

		source.append("\n");
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
		source.append("\t\tif (newSize > valueElements.size()) {\n");
		source.append("\t\t\tfor ( int i = valueElements.size(); i <= newSize; i++ ) {\n");
		source.append("\t\t\t\tvalueElements.set( i, null );\n");
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

	/**
	 * Generate member variables for template
	 *
	 * @param source where the source code is to be generated.
	 * @param genName the name of the generated class representing the "record of" type.
	 * @param ofTypeName type name of the "record of" element 
	 */
	private static void generateTemplateDeclaration( final StringBuilder source, final String genName, final String ofTypeName ) {
		source.append("\n");
		source.append( MessageFormat.format( "\t{0} single_value;\n", genName ) );
		source.append( MessageFormat.format( "\tList<{0}_template> value_list;\n", genName ) );
	}

	/**
	 * Generate constructors for template
	 *
	 * @param source where the source code is to be generated.
	 * @param genName the name of the generated class representing the "record of" type.
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
	 * @param genName the name of the generated class representing the "record of" type.
	 * @param ofTypeName type name of the "record of" element 
	 * @param displayName the user readable name of the type to be generated.
	 */
	private static void generateTemplateCopyTemplate( final StringBuilder source, final String genName, final String ofTypeName, final String displayName ) {
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
		source.append( MessageFormat.format( "\t\t\tvalue_list = new ArrayList<{0}_template>(other_value.value_list.size());\n", genName));
		source.append("\t\t\tfor(int i = 0; i < other_value.value_list.size(); i++) {\n");
		source.append( MessageFormat.format( "\t\t\t\tfinal {0}_template temp = new {0}_template(other_value.value_list.get(i));\n", genName));
		source.append("\t\t\t\tvalue_list.add(temp);\n");	
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
	 * @param genName the name of the generated class representing the "record of" type.
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
		source.append("\t\tif(!other_value.isBound()) {\n");
		source.append("\t\t\treturn new TitanBoolean(false);\n");
		source.append("\t\t}\n");
		source.append("\t\tswitch (templateSelection) {\n");
		source.append("\t\tcase ANY_VALUE:\n");
		source.append("\t\tcase ANY_OR_OMIT:\n");
		source.append("\t\t\treturn new TitanBoolean(true);\n");
		source.append("\t\tcase OMIT_VALUE:\n");
		source.append("\t\t\treturn new TitanBoolean(false);\n");

		source.append("\t\tcase SPECIFIC_VALUE:\n");
		//TODO: implement generated <genName>.match_function_specific() and Struct_of.cc: match_record_of()/match_set_of()
		/*
		case SPECIFIC_VALUE:
		return match_record_of(&other_value, value_length, this, single_value.n_elements, match_function_specific, legacy);
		*/
		source.append("\t\t\t//TODO: implement  generated <genName>.match_function_specific() and Struct_of.cc: match_record_of()/match_set_of()\n");
		source.append("\t\t\treturn new TitanBoolean(false);\n");

		source.append("\t\tcase VALUE_LIST:\n");
		source.append("\t\tcase COMPLEMENTED_LIST:\n");
		source.append("\t\t\tfor(int i = 0 ; i < value_list.size(); i++) {\n");
		source.append("\t\t\t\tif(value_list.get(i).match(other_value, legacy).getValue()) {\n");
		source.append("\t\t\t\t\treturn new TitanBoolean(templateSelection == template_sel.VALUE_LIST);\n");
		source.append("\t\t\t\t}\n");
		source.append("\t\t\t}\n");
		source.append("\t\t\treturn new TitanBoolean(templateSelection == template_sel.COMPLEMENTED_LIST);\n");
		source.append("\t\tdefault:\n");
		source.append( MessageFormat.format( "\t\t\tthrow new TtcnError(\"Matching with an uninitialized/unsupported template of type {0}.\");\n", displayName ) );
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
		source.append("\t\t\t\tfor (int i = 0 ; i < value_list.size(); i++) {\n");
		source.append("\t\t\t\t\tif (value_list.get(i).match_omit(legacy)) {\n");
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
	 * @param genName the name of the generated class representing the "record of" type.
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
		source.append("\t\t\tvalue_list.clear();\n");
		source.append("\t\t\tvalue_list = null;\n");
		source.append("\t\t\tbreak;\n");
		source.append("\t\tdefault:\n");
		source.append("\t\t\tbreak;\n");
		source.append("\t\t}\n");
		source.append("\t\ttemplateSelection = template_sel.UNINITIALIZED_TEMPLATE;\n");
		source.append("\t}\n");
	}

	/**
	 * Generate getter and setter functions for template 
	 * @param source where the source code is to be generated.
	 * @param ofTypeName type name of the "record of" element
	 * @param displayName the user readable name of the type to be generated.
	 */
	private static void generateTemplateGetterSetters(StringBuilder source, final String ofTypeName , final String displayName) {
		//TODO: implement
		//TODO: n_elem()
		
	}
}
