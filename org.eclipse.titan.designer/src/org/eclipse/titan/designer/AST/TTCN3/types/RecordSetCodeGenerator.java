package org.eclipse.titan.designer.AST.TTCN3.types;

import java.text.MessageFormat;
import java.util.List;

import org.eclipse.titan.designer.AST.FieldSubReference;
import org.eclipse.titan.designer.compiler.JavaGenData;

/**
 * Utility class for generating the value and template classes for record and set types.
 *
 * The code generated for record and set types only differs in matching and encoding.
 * 
 * @author Kristof Szabados
 * @author Arpad Lovassy
 */
public class RecordSetCodeGenerator {

	/**
	 * Data structure to store sequence field variable and type names.
	 * Used for java code generation.
	 */
	public static class FieldInfo {

		/** Java type name of the field */
		private String mJavaTypeName;

		/** Field variable name in TTCN-3 and java */
		private String mVarName;

		private boolean isOptional;

		/** Field variable name in java getter/setter function names and parameters */
		private String mJavaVarName;

		/** Java AST type name (for debug purposes) */
		private String mTTCN3TypeName;

		/**
		 * @param fieldType the string representing the type of this field in the generated code.
		 * @param fieldName the string representing the name of this field in the generated code.
		 * @param isOptional true if the field is optional.
		 * @param debugName additional text printed out in a comment after the generated local variables.
		 * */
		public FieldInfo(final String fieldType, final String fieldName, final boolean isOptional, final String debugName) {
			mJavaTypeName = fieldType;
			mVarName = fieldName;
			mJavaVarName  = FieldSubReference.getJavaGetterName( mVarName );
			this.isOptional = isOptional;
			mTTCN3TypeName = debugName;
		}
	}

	/**
	 * This function can be used to generate the value class of record and set types
	 *
	 * defRecordClass in compilers/record.{h,c}
	 *
	 * @param aData only used to update imports if needed.
	 * @param source where the source code is to be generated.
	 * @param className the name of the generated class representing the record/set type.
	 * @param classDisplayName the user readable name of the type to be generated.
	 * @param fieldInfos the list of information about the fields.
	 * @param hasOptional true if the type has an optional field.
	 * @param isSet true if generating code for a set, false if generating code for a record.
	 */
	public static void generateValueClass(final JavaGenData aData, final StringBuilder source, final String className, final String classDisplayname,
			final List<FieldInfo> fieldInfos, final boolean hasOptional, final boolean isSet) {
		aData.addBuiltinTypeImport("Base_Type");
		aData.addImport("java.text.MessageFormat");
		if(hasOptional) {
			aData.addBuiltinTypeImport("Optional");
			aData.addBuiltinTypeImport("Optional.optional_sel");
		}

		source.append( "\tpublic static class " );
		source.append( className );
		source.append(" extends Base_Type");
		source.append( " {\n" );
		generateDeclaration( aData, source, fieldInfos );
		generateConstructor( source, fieldInfos, className );
		generateConstructorManyParams( source, fieldInfos, className );
		generateConstructorCopy( source, className );
		generateAssign( aData, source, fieldInfos, className, classDisplayname );
		generateCleanUp( source, fieldInfos );
		generateIsBound( source, fieldInfos );
		generateIsPresent( source, fieldInfos );
		generateIsValue( source, fieldInfos );
		generateOperatorEquals( source, fieldInfos, className, classDisplayname);
		generateGettersSetters( source, fieldInfos );
		source.append( "\t}\n" );
	}

	/**
	 * This function can be used to generate the template class of record and set types
	 *
	 * defRecordClass in compilers/record.{h,c}
	 *
	 * @param aData only used to update imports if needed.
	 * @param source where the source code is to be generated.
	 * @param className the name of the generated class representing the record/set type.
	 * @param classDisplayName the user readable name of the type to be generated.
	 * @param fieldInfos the list of information about the fields.
	 * @param hasOptional true if the type has an optional field.
	 * @param isSet true if generating code for a set, false if generating code for a record.
	 */
	public static void generateTemplateClass(JavaGenData aData, StringBuilder source, String className,
			String classDisplayName, List<FieldInfo> fieldInfos, boolean hasOptional, boolean isSet) {
		aData.addImport("java.util.List");
		aData.addImport("java.text.MessageFormat");
		aData.addBuiltinTypeImport("Base_Template");
		aData.addBuiltinTypeImport("TitanBoolean");
		aData.addBuiltinTypeImport("TtcnError");
		source.append( MessageFormat.format( "public static class {0}_template extends Base_Template '{'\n", className ) );

		generateTemplateDeclaration( aData, source, fieldInfos, className );
		generateTemplateConstructors( source, className, classDisplayName );
		generateTemplateAssign( source, className, classDisplayName );
		generateTemplateCopyTemplate( aData,source, fieldInfos, className, classDisplayName );
		
		// TODO
		source.append("}\n");
	}

	/**
	 * Generating declaration of the member variables
	 * @param aData the generated java code with other info
	 * @param source the source to be updated
	 * @param aNamesList sequence field variable and type names
	 */
	private static void generateDeclaration( final JavaGenData aData, final StringBuilder source, final List<FieldInfo> aNamesList ) {
		for ( final FieldInfo fi : aNamesList ) {
			source.append( "\t\tprivate " );
			if (fi.isOptional) {
				source.append("Optional<");
				source.append( fi.mJavaTypeName );
				source.append(">");
			} else {
				source.append( fi.mJavaTypeName );
			}
			source.append( " " );
			source.append( fi.mVarName );
			source.append( ";" );
			if ( aData.isDebug() ) {
				source.append( " //" );
				source.append( fi.mTTCN3TypeName );
			}
			source.append( "\n" );
		}
	}

	/**
	 * Generating constructor without parameter
	 * @param aSb the output, where the java code is written
	 * @param aNamesList sequence field variable and type names
	 * @param aClassName the class name of the record/set class
	 */
	private static void generateConstructor( final StringBuilder aSb, final List<FieldInfo> aNamesList,
			final String aClassName ) {
		aSb.append( "\n\t\tpublic " );
		aSb.append( aClassName );
		aSb.append( "() {\n" );
		for ( final FieldInfo fi : aNamesList ) {
			aSb.append( "\t\t\t" );
			aSb.append( fi.mVarName );
			aSb.append( " = new " );
			if (fi.isOptional) {
				aSb.append("Optional<");
				aSb.append( fi.mJavaTypeName );
				aSb.append(">(");
				aSb.append( fi.mJavaTypeName );
				aSb.append( ".class);\n" );
			} else {
				aSb.append( fi.mJavaTypeName );
				aSb.append( "();\n" );
			}

		}
		aSb.append( "\t\t}\n" );
	}

	/**
	 * Generating constructor with many parameters (one for each record/set field)
	 *
	 * @param aSb the output, where the java code is written
	 * @param aNamesList sequence field variable and type names
	 * @param aClassName the class name of the record/set class
	 */
	private static void generateConstructorManyParams( final StringBuilder aSb, final List<FieldInfo> aNamesList,
			final String aClassName ) {
		aSb.append( "\n\t\tpublic " );
		aSb.append( aClassName );
		aSb.append( "( " );
		boolean first = true;
		for ( final FieldInfo fi : aNamesList ) {
			if ( first ) {
				first = false;
			} else {
				aSb.append( ", " );
			}
			aSb.append( "final " );
			if (fi.isOptional) {
				aSb.append("Optional<");
				aSb.append( fi.mJavaTypeName );
				aSb.append(">");
			} else {
				aSb.append( fi.mJavaTypeName );
			}
			aSb.append( " a" );
			aSb.append( fi.mJavaVarName );
		}
		aSb.append( " ) {\n" );
		for ( final FieldInfo fi : aNamesList ) {
			aSb.append( "\t\t\t" );
			aSb.append( fi.mVarName );
			aSb.append( " = new " );
			if (fi.isOptional) {
				aSb.append("Optional<");
				aSb.append( fi.mJavaTypeName );
				aSb.append(">(");
				aSb.append( fi.mJavaTypeName );
				aSb.append( ".class);\n" );
				aSb.append( "\t\t\tthis." );
				aSb.append( fi.mVarName );
				aSb.append( ".assign( a" );
				aSb.append( fi.mJavaVarName );
				aSb.append( " );\n" );
			} else {
				aSb.append( fi.mJavaTypeName );
				aSb.append( "( a");
				aSb.append( fi.mJavaVarName );
				aSb.append(" );\n" );
			}
		}
		aSb.append( "\t\t}\n" );
	}

	/**
	 * Generating constructor with 1 parameter (copy constructor)
	 * @param aSb the output, where the java code is written
	 * @param aClassName the class name of the record/set class
	 */
	private static void generateConstructorCopy( final StringBuilder aSb, final String aClassName ) {
		aSb.append( "\n\t\tpublic " );
		aSb.append( aClassName );
		aSb.append( "( final " );
		aSb.append( aClassName );
		aSb.append( " aOtherValue ) {\n" );
		aSb.append( "\t\t\tthis();\n" );
		aSb.append( "\t\t\tassign( aOtherValue );\n" );
		aSb.append( "\t\t}\n" );
	}

	/**
	 * Generating assign() function
	 * @param aData only used to update imports if needed
	 * @param source the source code generated
	 * @param aNamesList sequence field variable and type names
	 * @param aClassName the class name of the record/set class
	 * @param classReadableName the readable name of the class
	 */
	private static void generateAssign( final JavaGenData aData, final StringBuilder source, final List<FieldInfo> aNamesList,
			final String aClassName, final String classReadableName ) {
		aData.addCommonLibraryImport( "TtcnError" );
		source.append( "\n\t\tpublic " );
		source.append( aClassName );
		source.append( " assign( final " );
		source.append( aClassName );
		source.append( " aOtherValue ) {\n" );

		source.append( "\t\t\tif ( !aOtherValue.isBound() ) {\n" +
				"\t\t\t\tthrow new TtcnError( \"Assignment of an unbound value of type " );
		source.append( classReadableName );
		source.append( "\" );\n" +
				"\t\t\t}\n\n" );
		source.append("\t\tif (aOtherValue != this) {\n");
		for ( final FieldInfo fi : aNamesList ) {
			source.append( "\t\t\tif ( aOtherValue.get" );
			source.append( fi.mJavaVarName );
			source.append( "().isBound() ) {\n" +
					"\t\t\t\tthis." );
			source.append( fi.mVarName );
			source.append( ".assign( aOtherValue.get" );
			source.append( fi.mJavaVarName );
			source.append( "() );\n" +
					"\t\t\t} else {\n" +
					"\t\t\t\tthis." );
			source.append( fi.mVarName );
			source.append( ".cleanUp();\n" +
					"\t\t\t}\n" );
		}
		source.append( "\t\t}\n\n" );
		source.append( "\n\t\t\treturn this;\n" +
				"\t\t}\n" );

		source.append("\n");
		source.append("\t\t@Override\n");
		source.append("\t\tpublic ").append( aClassName ).append(" assign(final Base_Type otherValue) {\n");
		source.append("\t\t\tif (otherValue instanceof ").append(aClassName).append(" ) {\n");
		source.append("\t\t\t\treturn assign((").append( aClassName ).append(") otherValue);\n");
		source.append("\t\t\t}\n\n");
		source.append("\t\t\tthrow new TtcnError(MessageFormat.format(\"Internal Error: value `{0}'' can not be cast to ").append(classReadableName).append("\", otherValue));\n");
		source.append("\t\t}\n");

	}

	/**
	 * Generating cleanUp() function
	 * @param aSb the output, where the java code is written
	 * @param aNamesList sequence field variable and type names
	 */
	private static void generateCleanUp( final StringBuilder aSb, final List<FieldInfo> aNamesList ) {
		aSb.append( "\n\t\tpublic void cleanUp() {\n" );
		for ( final FieldInfo fi : aNamesList ) {
			aSb.append( "\t\t\t" );
			aSb.append( fi.mVarName );
			aSb.append( ".cleanUp();\n" );
		}
		aSb.append( "\t\t}\n" );
	}

	/**
	 * Generating isBound() function
	 * @param aSb the output, where the java code is written
	 * @param aNamesList sequence field variable and type names
	 */
	private static void generateIsBound( final StringBuilder aSb, final List<FieldInfo> aNamesList ) {
		aSb.append( "\n\t\tpublic boolean isBound() {\n" );
		//TODO: remove
		//for( int i = 0; i < 80; i++ )
		for ( final FieldInfo fi : aNamesList ) {
			if (fi.isOptional) {
				aSb.append( "\t\t\tif ( optional_sel.OPTIONAL_OMIT.equals(" );
				aSb.append( fi.mVarName );
				aSb.append(".getSelection()) || ");
				aSb.append(fi.mVarName);
				aSb.append( ".isBound() ) return true;\n" );
			} else {
				aSb.append( "\t\t\tif ( " );
				aSb.append( fi.mVarName );
				aSb.append( ".isBound() ) return true;\n" );
			}

		}
		aSb.append( "\t\t\treturn false;\n" +
				"\t\t}\n" );
	}

	/**
	 * Generating isPresent() function
	 * @param aSb the output, where the java code is written
	 * @param aNamesList sequence field variable and type names
	 */
	private static void generateIsPresent( final StringBuilder aSb, final List<FieldInfo> aNamesList ) {
		aSb.append( "\n\t\tpublic boolean isPresent() {\n" );
		aSb.append( "\t\t\t\treturn isBound();\n");
		aSb.append( "\t\t}\n" );
	}

	/**
	 * Generating isValue() function
	 * @param aSb the output, where the java code is written
	 * @param aNamesList sequence field variable and type names
	 */
	private static void generateIsValue( final StringBuilder aSb, final List<FieldInfo> aNamesList ) {
		aSb.append( "\n\t\tpublic boolean isValue() {\n" );
		for ( final FieldInfo fi : aNamesList ) {
			if (fi.isOptional) {
				aSb.append( "\t\t\tif ( !optional_sel.OPTIONAL_OMIT.equals(" );
				aSb.append( fi.mVarName );
				aSb.append(".getSelection()) && !");
				aSb.append(fi.mVarName);
				aSb.append( ".isValue() ) return true;\n" );
			} else {
				aSb.append( "\t\t\tif ( " );
				aSb.append( fi.mVarName );
				aSb.append( ".isValue() ) return true;\n" );
			}
		}
		aSb.append( "\t\t\treturn true;\n" +
				"\t\t}\n" );
	}

	/**
	 * Generating operatorEquals() function
	 * @param aSb the output, where the java code is written
	 * @param aNamesList sequence field variable and type names
	 * @param aClassName the class name of the record/set class
	 * @param classReadableName the readable name of the class
	 */
	private static void generateOperatorEquals( final StringBuilder aSb, final List<FieldInfo> aNamesList,
			final String aClassName, final String classReadableName ) {
		aSb.append( "\n\t\tpublic TitanBoolean operatorEquals( final " );
		aSb.append( aClassName );
		aSb.append( " aOtherValue ) {\n" );
		for ( final FieldInfo fi : aNamesList ) {
			aSb.append( "\t\t\tif ( !TitanBoolean.getNative( this." );
			aSb.append( fi.mVarName );
			aSb.append( ".operatorEquals( aOtherValue." );
			aSb.append( fi.mVarName );
			aSb.append( " )) ) return new TitanBoolean(false);\n" );
		}
		aSb.append( "\t\t\treturn new TitanBoolean(true);\n" +
				"\t\t}\n" );

		aSb.append("\n");
		aSb.append("\t\t@Override\n");
		aSb.append("\t\tpublic TitanBoolean operatorEquals(final Base_Type otherValue) {\n");
		aSb.append("\t\t\tif (otherValue instanceof ").append(aClassName).append(" ) {\n");
		aSb.append("\t\t\t\treturn operatorEquals((").append( aClassName ).append(") otherValue);\n");
		aSb.append("\t\t\t}\n\n");
		aSb.append("\t\t\tthrow new TtcnError(MessageFormat.format(\"Internal Error: value `{0}'' can not be cast to ").append(classReadableName).append("\", otherValue));");
		aSb.append("\t\t}\n");
	}

	/**
	 * Generating getters/setters for the member variables
	 * @param aSb the output, where the java code is written
	 * @param aNamesList sequence field variable and type names
	 */
	private static void generateGettersSetters( final StringBuilder aSb, final List<FieldInfo> aNamesList ) {
		for ( final FieldInfo fi : aNamesList ) {
			aSb.append( "\n\t\tpublic " );
			if (fi.isOptional) {
				aSb.append("Optional<");
				aSb.append( fi.mJavaTypeName );
				aSb.append(">");
			} else {
				aSb.append( fi.mJavaTypeName );
			}
			aSb.append( " get" );
			aSb.append( fi.mJavaVarName );
			aSb.append( "() {\n" +
					"\t\t\treturn " );
			aSb.append( fi.mVarName );
			aSb.append( ";\n" +
					"\t\t}\n" );

			aSb.append( "\n\t\tpublic " );
			if (fi.isOptional) {
				aSb.append("Optional<");
				aSb.append( fi.mJavaTypeName );
				aSb.append(">");
			} else {
				aSb.append( fi.mJavaTypeName );
			}
			aSb.append( " constGet" );
			aSb.append( fi.mJavaVarName );
			aSb.append( "() {\n" +
					"\t\t\treturn " );
			aSb.append( fi.mVarName );
			aSb.append( ";\n" +
					"\t\t}\n" );

			//			aSb.append( "\n\t\tpublic void set" );
			//			aSb.append( fi.mJavaVarName );
			//			aSb.append( "( final " );
			//			if (fi.isOptional) {
			//				aSb.append("Optional<");
			//				aSb.append( fi.mJavaTypeName );
			//				aSb.append(">");
			//			} else {
			//				aSb.append( fi.mJavaTypeName );
			//			}
			//			aSb.append( " a" );
			//			aSb.append( fi.mJavaVarName );
			//			aSb.append( " ) {\n" +
			//						"\t\t\tthis." );
			//			aSb.append( fi.mVarName );
			//			aSb.append( " = a" );
			//			aSb.append( fi.mJavaVarName );
			//			aSb.append( ";\n" +
			//						"\t\t}\n" );
		}
	}

	/**
	 * Generate member variables for template
	 *
	 * @param aData the generated java code with other info
	 * @param source the source to be updated
	 * @param aNamesList sequence field variable and type names
	 * @param className the name of the generated class representing the record/set type.
	 */
	private static void generateTemplateDeclaration( final JavaGenData aData, final StringBuilder source, final List<FieldInfo> aNamesList,
			final String className ) {
		source.append("\n");

		source.append("\tprivate static class single_value_struct {\n");
		for ( final FieldInfo fi : aNamesList ) {
			aData.addBuiltinTypeImport(fi.mJavaTypeName + "_template");
			source.append( "\t\tprivate " );
			source.append( fi.mJavaTypeName );
			source.append( "_template " );
			source.append( fi.mVarName );
			source.append( ";" );
			if ( aData.isDebug() ) {
				source.append( " //" );
				source.append( fi.mTTCN3TypeName );
			}
			source.append( "\n" );
		}
		for ( final FieldInfo fi : aNamesList ) {
			source.append( MessageFormat.format( "\t\tpublic {0}_template get{1}() '{'\n", fi.mJavaTypeName, fi.mJavaVarName ) );
			source.append( MessageFormat.format( "\t\t\treturn {0};\n", fi.mVarName ) );
			source.append("\t\t}\n");
			source.append( "\n" );

			source.append( MessageFormat.format( "\t\tpublic void set{1}( final {0}_template a{1}) '{'\n", fi.mJavaTypeName, fi.mJavaVarName ) );
			source.append( MessageFormat.format( "\t\t\tthis.{0} = a{1};\n", fi.mVarName, fi.mJavaVarName ) );
			source.append("\t\t}\n");
			source.append( "\n" );
		}
		source.append("\t}\n");
		source.append("\tsingle_value_struct single_value;\n");
		source.append("\n");

		source.append("\t//originally value_list/list_value\n");
		source.append( MessageFormat.format( "\tList<{0}_template> list_value;\n", className ) );

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
		source.append("\t\tcopyValue(otherValue);\n");
		source.append("\t};\n");

		source.append("\n");
		source.append( MessageFormat.format( "\tpublic {0}_template( final {0}_template otherValue ) '{'\n", genName ) );
		source.append("\t\tcopyTemplate( otherValue );\n");
		source.append("\t};\n");

		//TODO: implement optional parameter version
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
		source.append("\t\tcopyValue(other_value);\n");
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
	 * Generate the copyTemplate function for template
	 *
	 * @param aData the generated java code with other info
	 * @param source where the source code is to be generated.
	 * @param aNamesList sequence field variable and type names
	 * @param genName the name of the generated class representing the "record of/set of" type.
	 * @param displayName the user readable name of the type to be generated.
	 */
	private static void generateTemplateCopyTemplate( final JavaGenData aData, final StringBuilder source, final List<FieldInfo> aNamesList, final String genName, final String displayName ) {
		source.append("\n");
		source.append( MessageFormat.format( "\tprivate void copyValue(final {0} other_value) '{'\n", genName));
		source.append("\t\tsingle_value = new single_value_struct();\n");
		for ( final FieldInfo fi : aNamesList ) {
			source.append( MessageFormat.format( "\t\tif (other_value.get{0}().isBound()) '{'\n", fi.mJavaVarName ) );
			if ( fi.isOptional ) {
				source.append( MessageFormat.format( "\t\t\tif (other_value.get{0}().isPresent()) '{'\n", fi.mJavaVarName ) );
				source.append( MessageFormat.format( "\t\t\t\tsingle_value.get{0}().assign(other_value.get{0}().get());\n", fi.mJavaVarName ) );
				source.append("\t\t\t} else {\n");
				source.append( MessageFormat.format( "\t\t\t\tsingle_value.get{0}().assign(template_sel.OMIT_VALUE);\n", fi.mJavaVarName ) );
				source.append("\t\t\t}\n");
			} else {
				source.append( MessageFormat.format( "\t\t\tsingle_value.get{0}().assign(other_value.get{0}());\n", fi.mJavaVarName ) );
			}
			source.append("\t\t} else {\n");
			source.append( MessageFormat.format( "\t\t\tsingle_value.get{0}().cleanUp();\n", fi.mJavaVarName ) );
			source.append("\t\t}\n");
		}
		source.append("\t\tsetSelection(template_sel.SPECIFIC_VALUE);\n");
		source.append("\t}\n");

		source.append("\n");
		source.append( MessageFormat.format( "\tprivate void copyTemplate(final {0}_template other_value) '{'\n", genName));
		source.append("\t\tswitch (other_value.templateSelection) {\n");
		source.append("\t\tcase SPECIFIC_VALUE:\n");
		source.append("\t\t\tsingle_value = new single_value_struct();\n");
		for ( final FieldInfo fi : aNamesList ) {
			source.append( MessageFormat.format( "\t\t\tif (template_sel.UNINITIALIZED_TEMPLATE != other_value.single_value.get{0}().getSelection()) '{'\n", fi.mJavaVarName ) );
			source.append( MessageFormat.format( "\t\t\t\tsingle_value.get{0}().assign(other_value.single_value.get{0}());\n", fi.mJavaVarName ) );
			source.append("\t\t\t} else {\n");
			source.append( MessageFormat.format( "\t\t\t\tsingle_value.get{0}().cleanUp();\n", fi.mJavaVarName ) );
			source.append("\t\t\t}\n");
		}
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
}
