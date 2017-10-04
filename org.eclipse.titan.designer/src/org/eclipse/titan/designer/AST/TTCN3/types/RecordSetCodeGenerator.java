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

		/** Java template type name of the field */
		private String mJavaTemplateTypeName;

		/** Field variable name in TTCN-3 and java */
		private String mVarName;
		
		/** The user readable name of the field, typically used in error messages */
		private String mDisplayName;

		private boolean isOptional;

		/** Field variable name in java getter/setter function names and parameters */
		private String mJavaVarName;

		/** Java AST type name (for debug purposes) */
		private String mTTCN3TypeName;

		/**
		 * @param fieldType the string representing the type of this field in the generated code.
		 * @param fieldTemplateType the string representing the template type of this field in the generated code.
		 * @param fieldName the string representing the name of this field in the generated code.
		 * @param displayName The user readable name of the field, typically used in error messages
		 * @param isOptional true if the field is optional.
		 * @param debugName additional text printed out in a comment after the generated local variables.
		 * */
		public FieldInfo( final String fieldType, final String fieldTemplateType, final String fieldName,
						  final String displayName, final boolean isOptional, final String debugName) {
			mJavaTypeName = fieldType;
			mJavaTemplateTypeName = fieldTemplateType;
			mVarName = fieldName;
			mDisplayName = displayName;
			mJavaVarName  = FieldSubReference.getJavaGetterName( mVarName );
			this.isOptional = isOptional;
			mTTCN3TypeName = debugName;
		}
	}

	private RecordSetCodeGenerator() {
		// private to disable instantiation
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
		aData.addBuiltinTypeImport("TitanBoolean");
		aData.addBuiltinTypeImport("TitanInteger");
		aData.addBuiltinTypeImport("TtcnLogger");
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
		generateSizeOf( source, fieldInfos );
		generateLog( source, fieldInfos );
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
	public static void generateTemplateClass(final JavaGenData aData, final StringBuilder source, final String className,
			final String classDisplayName, final List<FieldInfo> fieldInfos, final boolean hasOptional, final boolean isSet) {
		aData.addImport("java.util.List");
		aData.addImport("java.util.ArrayList");
		aData.addImport("java.text.MessageFormat");
		aData.addBuiltinTypeImport("Base_Template");
		aData.addBuiltinTypeImport("TitanBoolean");
		aData.addBuiltinTypeImport("TitanInteger");
		aData.addBuiltinTypeImport("TtcnError");
		aData.addBuiltinTypeImport("Optional");
		source.append( MessageFormat.format( "public static class {0}_template extends Base_Template '{'\n", className ) );

		generateTemplateDeclaration( aData, source, fieldInfos, className );
		generateTemplateGetter( source, fieldInfos, classDisplayName );
		generateTemplateConstructors( source, className, classDisplayName );
		generateTemplateAssign( source, className, classDisplayName );
		generateTemplateCopyTemplate( source, fieldInfos, className, classDisplayName );
		generateTemplateIsPresent( source );
		generateTemplateValueOf( source, fieldInfos, className, classDisplayName );
		generateTemplateListItem( source, className, classDisplayName );
		generateTemplateSetType( source, className, classDisplayName );
		generateTemplateIsBound( source, fieldInfos );
		generateTemplateIsValue( source, fieldInfos );
		generateTemplateMatch( source, fieldInfos, className, classDisplayName );
		generateTemplateSizeOf( source, fieldInfos, classDisplayName );
		generateTemplateLog( source, fieldInfos );

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
				source.append('>');
			} else {
				source.append( fi.mJavaTypeName );
			}
			source.append( ' ' );
			source.append( fi.mVarName );
			source.append( ';' );
			if ( aData.isDebug() ) {
				source.append( " //" );
				source.append( fi.mTTCN3TypeName );
			}
			source.append( '\n' );
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
		if ( aNamesList == null || aNamesList.isEmpty()) {
			// Record type is empty, and parameter list would be also empty, but
			// constructor without parameters is already created, so nothing to do
			return;
		}
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
				aSb.append('>');
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

		source.append( "\t\t\tif ( !aOtherValue.isBound().getValue() ) {\n" +
				"\t\t\t\tthrow new TtcnError( \"Assignment of an unbound value of type " );
		source.append( classReadableName );
		source.append( "\" );\n" +
				"\t\t\t}\n\n" );
		source.append("\t\tif (aOtherValue != this) {\n");
		for ( final FieldInfo fi : aNamesList ) {
			source.append( "\t\t\tif ( aOtherValue.get" );
			source.append( fi.mJavaVarName );
			source.append( "().isBound().getValue() ) {\n" +
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

		source.append('\n');
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
		aSb.append( "\n\t\tpublic TitanBoolean isBound() {\n" );
		//TODO: remove
		//for( int i = 0; i < 80; i++ )
		for ( final FieldInfo fi : aNamesList ) {
			if (fi.isOptional) {
				aSb.append( "\t\t\tif ( optional_sel.OPTIONAL_OMIT.equals(" );
				aSb.append( fi.mVarName );
				aSb.append(".getSelection()) || ");
				aSb.append(fi.mVarName);
				aSb.append( ".isBound().getValue() ) return new TitanBoolean(true);\n" );
			} else {
				aSb.append( "\t\t\tif ( " );
				aSb.append( fi.mVarName );
				aSb.append( ".isBound().getValue() ) return new TitanBoolean(true);\n" );
			}

		}
		aSb.append( "\t\t\treturn new TitanBoolean(false);\n" +
				"\t\t}\n" );
	}

	/**
	 * Generating isPresent() function
	 * @param aSb the output, where the java code is written
	 * @param aNamesList sequence field variable and type names
	 */
	private static void generateIsPresent( final StringBuilder aSb, final List<FieldInfo> aNamesList ) {
		aSb.append( "\n\t\tpublic TitanBoolean isPresent() {\n" );
		aSb.append( "\t\t\t\treturn isBound();\n");
		aSb.append( "\t\t}\n" );
	}

	/**
	 * Generating isValue() function
	 * @param aSb the output, where the java code is written
	 * @param aNamesList sequence field variable and type names
	 */
	private static void generateIsValue( final StringBuilder aSb, final List<FieldInfo> aNamesList ) {
		aSb.append( "\n\t\tpublic TitanBoolean isValue() {\n" );
		if ( aNamesList == null || aNamesList.isEmpty() ) {
			aSb.append( "\t\t\treturn new TitanBoolean(false);\n" +
					"\t\t}\n" );
			return;
		}
		for ( final FieldInfo fi : aNamesList ) {
			if (fi.isOptional) {
				aSb.append( "\t\t\tif ( !optional_sel.OPTIONAL_OMIT.equals(" );
				aSb.append( fi.mVarName );
				aSb.append(".getSelection()) && !");
				aSb.append(fi.mVarName);
				aSb.append( ".isValue().getValue() ) return new TitanBoolean(false);\n" );
			} else {
				aSb.append( "\t\t\tif ( !" );
				aSb.append( fi.mVarName );
				aSb.append( ".isValue().getValue() ) return new TitanBoolean(false);\n" );
			}
		}
		aSb.append( "\t\t\treturn new TitanBoolean(true);\n" +
				"\t\t}\n" );
	}

	/**
	 * Generating sizeOf() function
	 * @param aSb the output, where the java code is written
	 * @param aNamesList sequence field variable and type names
	 */
	private static void generateSizeOf( final StringBuilder aSb, final List<FieldInfo> aNamesList ) {
		aSb.append( "\n\t\tpublic TitanInteger sizeOf() {\n" );
		aSb.append( "\t\t\tint sizeof = 0;\n" );
		//number of non-optional fields
		int size = 0;
		for ( final FieldInfo fi : aNamesList ) {
			if (fi.isOptional) {
				aSb.append( MessageFormat.format( "\t\t\tif ({0}.isPresent().getValue()) '{'\n", fi.mVarName ) );
				aSb.append( "\t\t\t\tsizeof++;\n" );
				aSb.append( "\t\t\t}\n" );
			} else {
				size++;
			}
		}
		aSb.append( MessageFormat.format( "\t\t\tsizeof += {0};\n", size ) );
		aSb.append( "\t\t\treturn new TitanInteger(sizeof);\n" );
		aSb.append( "\t\t}\n" );
	}

	/**
	 * Generating log() function
	 * @param aSb the output, where the java code is written
	 * @param aNamesList sequence field variable and type names
	 */
	private static void generateLog(final StringBuilder aSb, final List<FieldInfo> aNamesList ) {
		aSb.append("public void log() {\n");
		aSb.append("if (!isBound().getValue()) {\n");
		aSb.append("TtcnLogger.log_event_unbound();\n");
		aSb.append("return;\n");
		aSb.append("}\n");
		aSb.append("TtcnLogger.log_char('{');\n");
		for (int i = 0 ; i < aNamesList.size(); i++) {
			FieldInfo fieldInfo = aNamesList.get(i);
			if (i > 0) {
				aSb.append("TtcnLogger.log_char(',');\n");
			}
			aSb.append(MessageFormat.format("TtcnLogger.log_event_str(\" {0} := \");\n", fieldInfo.mVarName));
			aSb.append(MessageFormat.format("{0}.log();\n", fieldInfo.mVarName));
		}
		aSb.append("TtcnLogger.log_event_str(\" }\");\n");
		aSb.append("}\n");
	}

	/**
	 * Generating isBound() function for template
	 * @param aSb the output, where the java code is written
	 * @param aNamesList sequence field variable and type names
	 */
	private static void generateTemplateIsBound( final StringBuilder aSb, final List<FieldInfo> aNamesList ) {
		aSb.append( "\n\t\tpublic TitanBoolean isBound() {\n" );
		aSb.append( "\t\t\tif (templateSelection == template_sel.UNINITIALIZED_TEMPLATE && !is_ifPresent) {\n"
				+ "\t\t\t\treturn new TitanBoolean(false);\n"
				+ "\t\t\t}\n" );
		aSb.append( "\t\t\tif (templateSelection != template_sel.SPECIFIC_VALUE) {\n"
				+ "\t\t\t\treturn new TitanBoolean(true);\n"
				+ "\t\t\t}\n" );
		for ( final FieldInfo fi : aNamesList ) {
			if (fi.isOptional) {
				aSb.append( MessageFormat.format( "\t\t\tif ({0}.isOmit() || {0}.isBound().getValue()) '{'\n"
						+ "\t\t\t\treturn new TitanBoolean(true);\n"
						+ "\t\t\t}\n", fi.mVarName ) );
			} else {
				aSb.append( MessageFormat.format( "\t\t\tif ({0}.isBound().getValue()) '{'\n"
						+ "\t\t\t\treturn new TitanBoolean(true);\n"
						+ "\t\t\t}\n", fi.mVarName ) );
			}
		}
		aSb.append( "\t\t\treturn new TitanBoolean(false);\n" +
				"\t\t}\n" );
	}

	/**
	 * Generating isValue() function for template
	 * @param aSb the output, where the java code is written
	 * @param aNamesList sequence field variable and type names
	 */
	private static void generateTemplateIsValue( final StringBuilder aSb, final List<FieldInfo> aNamesList ) {
		aSb.append( "\n\t\tpublic TitanBoolean isValue() {\n" );
		aSb.append( "\t\t\tif (templateSelection != template_sel.SPECIFIC_VALUE || is_ifPresent) {\n"
				+ "\t\t\t\treturn new TitanBoolean(false);\n"
				+ "\t\t\t}\n" );
		for ( final FieldInfo fi : aNamesList ) {
			if (fi.isOptional) {
				aSb.append( MessageFormat.format( "\t\t\tif (!{0}.isOmit() && !{0}.isValue().getValue()) '{'\n"
						+ "\t\t\t\treturn new TitanBoolean(false);\n"
						+ "\t\t\t}\n", fi.mVarName ) );
			} else {
				aSb.append( MessageFormat.format( "\t\t\tif (!{0}.isValue().getValue()) '{'\n"
						+ "\t\t\t\treturn new TitanBoolean(false);\n"
						+ "\t\t\t}\n", fi.mVarName ) );
			}
		}
		aSb.append( "\t\t\treturn new TitanBoolean(true);\n" +
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

		aSb.append('\n');
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
				aSb.append('>');
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
				aSb.append('>');
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

			//TODO: remove
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
		source.append('\n');

		for ( final FieldInfo fi : aNamesList ) {
			source.append( "\tprivate " );
			source.append( fi.mJavaTypeName );
			source.append( "_template " );
			source.append( fi.mVarName );
			source.append( ';' );
			if ( aData.isDebug() ) {
				source.append( " //" );
				source.append( fi.mTTCN3TypeName );
			}
			source.append( '\n' );
		}

		source.append("\t//originally value_list/list_value\n");
		source.append( MessageFormat.format( "\tList<{0}_template> list_value;\n", className ) );
	}

	/**
	 * Generate getters for template
	 *
	 * @param source the source to be updated
	 * @param aNamesList sequence field variable and type names
	 * @param displayName the user readable name of the type to be generated.
	 */
	private static void generateTemplateGetter( final StringBuilder source, final List<FieldInfo> aNamesList,
			final String displayName ) {
		for ( final FieldInfo fi : aNamesList ) {
			source.append( '\n' );
			source.append( MessageFormat.format( "\tpublic {0}_template get{1}() '{'\n", fi.mJavaTypeName, fi.mJavaVarName ) );
			source.append("\t\tsetSpecific();\n");
			source.append( MessageFormat.format( "\t\treturn {0};\n", fi.mVarName ) );
			source.append("\t}\n");
			
			source.append( '\n' );
			source.append( MessageFormat.format( "\tpublic {0}_template constGet{1}() '{'\n", fi.mJavaTypeName, fi.mJavaVarName ) );
			source.append("\t\tif (templateSelection != template_sel.SPECIFIC_VALUE) {\n");
			source.append( MessageFormat.format( "\t\t\tthrow new TtcnError(\"Accessing field {0} of a non-specific template of type {1}.\");\n", fi.mDisplayName, displayName ) );
			source.append("\t\t}\n");
			source.append( MessageFormat.format( "\t\treturn {0};\n", fi.mVarName ) );
			source.append("\t}\n");
		}

		source.append('\n');
		source.append("\tprivate void setSpecific() {\n");
		source.append("\t\tif (templateSelection != template_sel.SPECIFIC_VALUE) {\n");
		source.append("\t\t\tfinal template_sel old_selection = templateSelection;\n");
		source.append("\t\t\tcleanUp();\n");
		source.append("\t\t\tsetSelection(template_sel.SPECIFIC_VALUE);\n");
		for ( final FieldInfo fi : aNamesList ) {
			source.append( MessageFormat.format( "\t\t\t{0} = new {1}();\n", fi.mVarName, fi.mJavaTemplateTypeName ) );
		}
		source.append("\t\t\tif (old_selection == template_sel.ANY_VALUE || old_selection == template_sel.ANY_OR_OMIT) {\n");
		for ( final FieldInfo fi : aNamesList ) {
			if (fi.isOptional) {
				source.append( MessageFormat.format( "\t\t\t\t{0}.assign(template_sel.ANY_OR_OMIT);\n", fi.mVarName ) );
			} else {
				source.append( MessageFormat.format( "\t\t\t\t{0}.assign(template_sel.ANY_VALUE);\n", fi.mVarName ) );
			}
		}
		source.append("\t\t\t}\n");
		source.append("\t\t}\n");
		source.append("\t}\n");
	}

	/**
	 * Generate constructors for template
	 *
	 * @param source where the source code is to be generated.
	 * @param genName the name of the generated class representing the "record of/set of" type.
	 * @param displayName the user readable name of the type to be generated.
	 */
	private static void generateTemplateConstructors( final StringBuilder source, final String genName, final String displayName ) {
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
		source.append("\t\tcopyValue(otherValue);\n");
		source.append("\t}\n");

		source.append('\n');
		source.append( MessageFormat.format( "\tpublic {0}_template( final {0}_template otherValue ) '{'\n", genName ) );
		source.append("\t\tcopyTemplate( otherValue );\n");
		source.append("\t}\n");

		source.append('\n');
		source.append( MessageFormat.format( "\tpublic {0}_template( final Optional<{0}> other_value ) '{'\n", genName ) );
		source.append("\t\tswitch (other_value.getSelection()) {\n");
		source.append("\t\tcase OPTIONAL_PRESENT:\n");
		source.append("\t\t\tcopyValue(other_value.constGet());\n");
		source.append("\t\t\tbreak;\n");
		source.append("\t\tcase OPTIONAL_OMIT:\n");
		source.append("\t\t\tsetSelection(template_sel.OMIT_VALUE);\n");
		source.append("\t\t\tbreak;\n");
		source.append("\t\tdefault:\n");
		source.append( MessageFormat.format( "\t\t\tthrow new TtcnError(\"Creating a template of type {0} from an unbound optional field.\");\n", displayName ) );
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
		source.append("\t\tcopyValue(other_value);\n");
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
		source.append("\t\t@Override\n");
		source.append( MessageFormat.format("\t\tpublic {0}_template assign(final Base_Type otherValue) '{'\n", genName));
		source.append( MessageFormat.format("\t\t\tif (otherValue instanceof {0}) '{'\n", genName));
		source.append( MessageFormat.format("\t\t\t\treturn assign(({0}) otherValue);\n", genName));
		source.append("\t\t\t}\n\n");
		source.append( MessageFormat.format("\t\t\tthrow new TtcnError(MessageFormat.format(\"Internal Error: value `{0}'' can not be cast to {1}\", otherValue));\n", genName));
		source.append("\t\t}\n");

		source.append('\n');
		source.append("\t\t@Override\n");
		source.append( MessageFormat.format("\t\tpublic {0}_template assign(final Base_Template otherValue) '{'\n", genName));
		source.append( MessageFormat.format("\t\t\tif (otherValue instanceof {0}_template) '{'\n", genName));
		source.append( MessageFormat.format("\t\t\t\treturn assign(({0}_template) otherValue);\n", genName));
		source.append("\t\t\t}\n\n");
		source.append( MessageFormat.format("\t\t\tthrow new TtcnError(MessageFormat.format(\"Internal Error: value `{0}'' can not be cast to {1}_template\", otherValue));\n", genName));
		source.append("\t\t}\n");

		source.append('\n');
		source.append( MessageFormat.format( "\tpublic {0}_template assign( final Optional<{0}> other_value ) '{'\n", genName ) );
		source.append("\t\tcleanUp();\n");
		source.append("\t\tswitch (other_value.getSelection()) {\n");
		source.append("\t\tcase OPTIONAL_PRESENT:\n");
		source.append("\t\t\tcopyValue(other_value.constGet());\n");
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
	 * Generate the copyTemplate function for template
	 *
	 * @param source where the source code is to be generated.
	 * @param aNamesList sequence field variable and type names
	 * @param genName the name of the generated class representing the "record of/set of" type.
	 * @param displayName the user readable name of the type to be generated.
	 */
	private static void generateTemplateCopyTemplate( final StringBuilder source, final List<FieldInfo> aNamesList, final String genName, final String displayName ) {
		source.append('\n');
		source.append( MessageFormat.format( "\tprivate void copyValue(final {0} other_value) '{'\n", genName));
		for ( final FieldInfo fi : aNamesList ) {
			source.append( MessageFormat.format( "\t\tif (other_value.get{0}().isBound().getValue()) '{'\n", fi.mJavaVarName ) );
			if ( fi.isOptional ) {
				source.append( MessageFormat.format( "\t\t\tif (other_value.get{0}().isPresent().getValue()) '{'\n", fi.mJavaVarName ) );
				source.append( MessageFormat.format( "\t\t\t\tget{0}().assign(other_value.get{0}().get());\n", fi.mJavaVarName ) );
				source.append("\t\t\t} else {\n");
				source.append( MessageFormat.format( "\t\t\t\tget{0}().assign(template_sel.OMIT_VALUE);\n", fi.mJavaVarName ) );
				source.append("\t\t\t}\n");
			} else {
				source.append( MessageFormat.format( "\t\t\tget{0}().assign(other_value.get{0}());\n", fi.mJavaVarName ) );
			}
			source.append("\t\t} else {\n");
			source.append( MessageFormat.format( "\t\t\tget{0}().cleanUp();\n", fi.mJavaVarName ) );
			source.append("\t\t}\n");
		}
		source.append("\t\tsetSelection(template_sel.SPECIFIC_VALUE);\n");
		source.append("\t}\n");

		source.append('\n');
		source.append( MessageFormat.format( "\tprivate void copyTemplate(final {0}_template other_value) '{'\n", genName));
		source.append("\t\tswitch (other_value.templateSelection) {\n");
		source.append("\t\tcase SPECIFIC_VALUE:\n");
		for ( final FieldInfo fi : aNamesList ) {
			source.append( MessageFormat.format( "\t\t\tif (template_sel.UNINITIALIZED_TEMPLATE != other_value.get{0}().getSelection()) '{'\n", fi.mJavaVarName ) );
			source.append( MessageFormat.format( "\t\t\t\tget{0}().assign(other_value.get{0}());\n", fi.mJavaVarName ) );
			source.append("\t\t\t} else {\n");
			source.append( MessageFormat.format( "\t\t\t\tget{0}().cleanUp();\n", fi.mJavaVarName ) );
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

	/**
	 * Generating isPresent() function for template
	 * @param aSb the output, where the java code is written
	 */
	private static void generateTemplateIsPresent( final StringBuilder aSb ) {
		aSb.append('\n');
		aSb.append("\t\tpublic TitanBoolean isPresent() {\n");
		aSb.append("\t\t\treturn isPresent(false);\n");
		aSb.append("\t\t}\n");

		aSb.append('\n');
		aSb.append("\t\tpublic TitanBoolean isPresent(boolean legacy) {\n");
		aSb.append("\t\t\treturn new TitanBoolean(isPresent_(legacy));\n");
		aSb.append("\t\t}\n");

		aSb.append('\n');
		aSb.append("\t\tprivate boolean isPresent_(boolean legacy) {\n");
		aSb.append("\t\t\tif (templateSelection==template_sel.UNINITIALIZED_TEMPLATE) {\n");
		aSb.append("\t\t\t\treturn false;\n");
		aSb.append("\t\t\t}\n");
		aSb.append("\t\t\treturn !match_omit_(legacy);\n");
		aSb.append("\t\t}\n");

		aSb.append('\n');
		aSb.append("\t\tpublic TitanBoolean match_omit() {\n");
		aSb.append("\t\t\treturn match_omit(false);\n");
		aSb.append("\t\t}\n");

		aSb.append('\n');
		aSb.append("\t\tpublic TitanBoolean match_omit(boolean legacy) {\n");
		aSb.append("\t\t\treturn new TitanBoolean(match_omit_(legacy));\n");
		aSb.append("\t\t}\n");

		aSb.append('\n');
		aSb.append("\t\tprivate boolean match_omit_(boolean legacy) {\n");
		aSb.append("\t\t\tif (is_ifPresent) {\n");
		aSb.append("\t\t\t\treturn true;\n");
		aSb.append("\t\t\t}\n");
		aSb.append("\t\t\tswitch (templateSelection) {\n");
		aSb.append("\t\t\tcase OMIT_VALUE:\n");
		aSb.append("\t\t\tcase ANY_OR_OMIT:\n");
		aSb.append("\t\t\t\treturn true;\n");
		aSb.append("\t\t\tcase VALUE_LIST:\n");
		aSb.append("\t\t\tcase COMPLEMENTED_LIST:\n");
		aSb.append("\t\t\t\tif (legacy) {\n");
		aSb.append("\t\t\t\t\tfor (int l_idx=0; l_idx<list_value.size(); l_idx++) {\n");
		aSb.append("\t\t\t\t\t\tif (list_value.get(l_idx).match_omit_(legacy)) {\n");
		aSb.append("\t\t\t\t\t\t\treturn templateSelection==template_sel.VALUE_LIST;\n");
		aSb.append("\t\t\t\t\t\t}\n");
		aSb.append("\t\t\t\t\t}\n");
		aSb.append("\t\t\t\t\treturn templateSelection==template_sel.COMPLEMENTED_LIST;\n");
		aSb.append("\t\t\t\t} // else fall through\n");
		aSb.append("\t\t\tdefault:\n");
		aSb.append("\t\t\t\treturn false;\n");
		aSb.append("\t\t\t}\n");
		aSb.append("\t\t}\n");
	}

	/**
	 * Generating valueOf() function for template
	 * @param aSb the output, where the java code is written
	 * @param aNamesList sequence field variable and type names
	 * @param genName the name of the generated class representing the "record of/set of" type.
	 * @param displayName the user readable name of the type to be generated.
	 */
	private static void generateTemplateValueOf( final StringBuilder aSb, final List<FieldInfo> aNamesList, final String genName, final String displayName ) {
		aSb.append('\n');
		aSb.append( MessageFormat.format( "\t\tpublic {0} valueOf() '{'\n", genName ) );
		aSb.append("\t\t\tif (templateSelection != template_sel.SPECIFIC_VALUE || is_ifPresent) {\n");
		aSb.append( MessageFormat.format( "\t\t\t\tthrow new TtcnError(\"Performing a valueof or send operation on a non-specific template of type {0}.\");\n", displayName ) );
		aSb.append("\t\t\t}\n");
		aSb.append( MessageFormat.format( "\t\t\t{0} ret_val = new {0}();\n", genName ) );
		for ( final FieldInfo fi : aNamesList ) {
			if (fi.isOptional) {
				aSb.append( MessageFormat.format( "\t\t\tif ({0}.isOmit()) '{'\n", fi.mVarName )  );
				aSb.append( MessageFormat.format( "\t\t\t\tret_val.{0}.assign(template_sel.OMIT_VALUE);\n", fi.mVarName ) );
				aSb.append("\t\t\t} else ");
			} else {
				aSb.append("\t\t\t ");
			}
			aSb.append( MessageFormat.format( "if ({0}.isBound().getValue()) '{'\n", fi.mVarName )  );
			aSb.append( MessageFormat.format( "\t\t\t\tret_val.{0}.assign({0}.valueOf());\n", fi.mVarName ) );
			aSb.append("\t\t\t}\n");
		}
		aSb.append("\t\t\treturn ret_val;\n");
		aSb.append("\t\t}\n");
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
	 * Generating setType() function for template
	 * @param aSb the output, where the java code is written
	 * @param genName the name of the generated class representing the "record of/set of" type.
	 * @param displayName the user readable name of the type to be generated.
	 */
	private static void generateTemplateSetType( final StringBuilder aSb, final String genName, final String displayName ) {
		aSb.append('\n');
		aSb.append("\t\tpublic void setType(template_sel template_type, int list_length) {\n");
		aSb.append("\t\t\tif (template_type != template_sel.VALUE_LIST && template_type != template_sel.COMPLEMENTED_LIST) {\n");
		aSb.append( MessageFormat.format( "\t\t\t\tthrow new TtcnError(\"Setting an invalid list for a template of type {0}.\");\n", displayName ) );
		aSb.append("\t\t\t}\n");
		aSb.append("\t\t\tcleanUp();\n");
		aSb.append("\t\t\tsetSelection(template_type);\n");
		aSb.append( MessageFormat.format( "\t\t\tlist_value = new ArrayList<{0}_template>(list_length);\n", genName ) );
		aSb.append("\t\t\tfor(int i = 0 ; i < list_length; i++) {\n");
        aSb.append(MessageFormat.format("\t\t\t\tlist_value.add(new {0}_template());\n", genName));
        aSb.append("\t\t\t}\n");
		aSb.append("\t\t}\n");
	}

	/**
	 * Generate the copyTemplate function for template
	 *
	 * @param source where the source code is to be generated.
	 * @param aNamesList sequence field variable and type names
	 * @param genName the name of the generated class representing the "record of/set of" type.
	 * @param displayName the user readable name of the type to be generated.
	 */
	private static void generateTemplateMatch( final StringBuilder source, final List<FieldInfo> aNamesList, final String genName, final String displayName ) {
		source.append('\n');
		source.append( MessageFormat.format( "\t\tpublic TitanBoolean match({0} other_value) '{'\n", genName ) );
		source.append("\t\t\treturn match(other_value, false);\n");
		source.append("\t\t}\n");

		source.append('\n');
		source.append( MessageFormat.format( "\t\tpublic TitanBoolean match({0} other_value, boolean legacy) '{'\n", genName ) );
		source.append("\t\t\treturn new TitanBoolean(match_(other_value, legacy));\n");
		source.append("\t\t}\n");

		source.append('\n');
		source.append( MessageFormat.format( "\t\tprivate boolean match_({0} other_value, boolean legacy) '{'\n", genName ) );
		source.append("\t\t\tif (!other_value.isBound().getValue()) {\n");
		source.append("\t\t\t\treturn false;\n");
		source.append("\t\t\t}\n");
		source.append("\t\t\tswitch (templateSelection) {\n");
		source.append("\t\t\tcase ANY_VALUE:\n");
		source.append("\t\t\tcase ANY_OR_OMIT:\n");
		source.append("\t\t\t\treturn true;\n");
		source.append("\t\t\tcase OMIT_VALUE:\n");
		source.append("\t\t\t\treturn false;\n");
		source.append("\t\t\tcase SPECIFIC_VALUE:\n");
		for ( final FieldInfo fi : aNamesList ) {
			source.append( MessageFormat.format( "\t\t\t\tif(!other_value.get{0}().isBound().getValue()) '{'\n", fi.mJavaVarName )  );
			source.append("\t\t\t\t\treturn false;\n");
			source.append("\t\t\t\t}\n");
			if (fi.isOptional) {
				source.append( MessageFormat.format( "\t\t\t\tif((other_value.get{0}().isPresent().getValue() ? !{1}.match(other_value.get{0}().get(), legacy).getValue() : !{1}.match_omit(legacy).getValue())) '{'\n", fi.mJavaVarName, fi.mVarName ) );
			} else {
				source.append( MessageFormat.format( "\t\t\t\tif(!{1}.match(other_value.get{0}(), legacy).getValue()) '{'\n", fi.mJavaVarName, fi.mVarName )  );
			}
			source.append("\t\t\t\t\treturn false;\n");
			source.append("\t\t\t\t}\n");
		}
		source.append("\t\t\t\treturn true;\n");
		source.append("\t\t\tcase VALUE_LIST:\n");
		source.append("\t\t\tcase COMPLEMENTED_LIST:\n");
		source.append("\t\t\t\tfor (int list_count = 0; list_count < list_value.size(); list_count++)\n");
		source.append("\t\t\t\t\tif (list_value.get(list_count).match(other_value, legacy).getValue()) {\n");
		source.append("\t\t\t\t\t\treturn templateSelection == template_sel.VALUE_LIST;\n");
		source.append("\t\t\t\t\t}\n");
		source.append("\t\t\t\treturn templateSelection == template_sel.COMPLEMENTED_LIST;\n");
		source.append("\t\t\tdefault:\n");
		source.append( MessageFormat.format( "\t\t\t\tthrow new TtcnError(\"Matching an uninitialized/unsupported template of type {0}.\");\n", displayName ) );
		source.append("\t\t\t}\n");
		source.append("\t\t}\n");

		source.append('\n');
		source.append("\t@Override\n");
		source.append( MessageFormat.format( "\tpublic TitanBoolean match(final Base_Type otherValue, final boolean legacy) '{'\n", genName ) );
		source.append( MessageFormat.format( "\tif (otherValue instanceof {0}) '{'\n", genName) );
		source.append( MessageFormat.format( "\t\treturn match(({0})otherValue, legacy);\n", genName) );
		source.append("\t}\n\n");
		source.append( MessageFormat.format( "\t\tthrow new TtcnError(\"Internal Error: The left operand of assignment is not of type {0}.\");\n", genName ) );
		source.append("\t}\n");
	}

	/**
	 * Generating sizeOf() function
	 * @param aSb the output, where the java code is written
	 * @param aNamesList sequence field variable and type names
	 * @param displayName the user readable name of the type to be generated.
	 */
	private static void generateTemplateSizeOf( final StringBuilder aSb, final List<FieldInfo> aNamesList, final String displayName ) {
		aSb.append( "\n\t\tpublic TitanInteger sizeOf() {\n" );
		aSb.append( "\t\t\tif (is_ifPresent) {\n" );
		aSb.append( MessageFormat.format( "\t\t\t\tthrow new TtcnError(\"Performing sizeof() operation on a template of type {0} which has an ifpresent attribute.\");\n", displayName ) );
		aSb.append( "\t\t\t}\n" );
		aSb.append( "\t\t\tswitch (templateSelection) {\n" );
		aSb.append( "\t\t\tcase SPECIFIC_VALUE:\n" );
		aSb.append( "\t\t\t\tint sizeof = 0;\n" );
		//number of non-optional fields
		int size = 0;
		for ( final FieldInfo fi : aNamesList ) {
			if (fi.isOptional) {
				aSb.append( MessageFormat.format( "\t\t\t\tif ({0}.isPresent().getValue()) '{'\n", fi.mVarName ) );
				aSb.append( "\t\t\t\t\tsizeof++;\n" );
				aSb.append( "\t\t\t\t}\n" );
			} else {
				size++;
			}
		}
		aSb.append( MessageFormat.format( "\t\t\t\tsizeof += {0};\n", size ) );
		aSb.append( "\t\t\t\treturn new TitanInteger(sizeof);\n" );
		aSb.append( "\t\t\tcase VALUE_LIST:\n" );
		aSb.append( "\t\t\t\tif (list_value.size() < 1) {\n" );
		aSb.append( MessageFormat.format( "\t\t\t\t\tthrow new TtcnError(\"Internal error: Performing sizeof() operation on a template of type {0} containing an empty list.\");\n", displayName ) );
		aSb.append( "\t\t\t\t}\n" );
		aSb.append( "\t\t\t\tint item_size = list_value.get(0).sizeOf().getInt();\n" );
		aSb.append( "\t\t\t\tfor (int l_idx = 1; l_idx < list_value.size(); l_idx++) {\n" );
		aSb.append( "\t\t\t\t\tif (list_value.get(l_idx).sizeOf().getInt() != item_size) {\n" );
		aSb.append( MessageFormat.format( "\t\t\t\t\t\tthrow new TtcnError(\"Performing sizeof() operation on a template of type {0} containing a value list with different sizes.\");\n", displayName ) );
		aSb.append( "\t\t\t\t\t}\n" );
		aSb.append( "\t\t\t\t}\n" );
		aSb.append( "\t\t\t\treturn new TitanInteger(item_size);\n" );
		aSb.append( "\t\t\tcase OMIT_VALUE:\n" );
		aSb.append( MessageFormat.format( "\t\t\t\tthrow new TtcnError(\"Performing sizeof() operation on a template of type {0} containing omit value.\");\n", displayName ) );
		aSb.append( "\t\t\tcase ANY_VALUE:\n" );
		aSb.append( "\t\t\tcase ANY_OR_OMIT:\n" );
		aSb.append( MessageFormat.format( "\t\t\t\tthrow new TtcnError(\"Performing sizeof() operation on a template of type {0} containing */? value.\");\n", displayName ) );
		aSb.append( "\t\t\tcase COMPLEMENTED_LIST:\n" );
		aSb.append( MessageFormat.format( "\t\t\t\tthrow new TtcnError(\"Performing sizeof() operation on a template of type {0} containing complemented list.\");\n", displayName ) );
		aSb.append( "\t\t\tdefault:\n" );
		aSb.append( MessageFormat.format( "\t\t\t\tthrow new TtcnError(\"Performing sizeof() operation on an uninitialized/unsupported template of type {0}.\");\n", displayName ) );
		aSb.append( "\t\t\t}\n" );
		aSb.append( "\t\t}\n" );
	}

	/**
	 * Generating log() function
	 * @param aSb the output, where the java code is written
	 * @param aNamesList sequence field variable and type names
	 */
	private static void generateTemplateLog(final StringBuilder source, final List<FieldInfo> aNamesList) {
		source.append("public void log() {\n");
		source.append("switch (templateSelection) {\n");
		source.append("case SPECIFIC_VALUE:\n");
		source.append("TtcnLogger.log_char('{');\n");
		for (int i = 0 ; i < aNamesList.size(); i++) {
			FieldInfo fieldInfo = aNamesList.get(i);
			if (i > 0) {
				source.append("TtcnLogger.log_char(',');\n");
			}
			source.append(MessageFormat.format("TtcnLogger.log_event_str(\" {0} := \");\n", fieldInfo.mVarName));
			source.append(MessageFormat.format("{0}.log();\n", fieldInfo.mVarName));
		}
		source.append("TtcnLogger.log_event_str(\" }\");\n");
		source.append("break;\n");
		source.append("case COMPLEMENTED_LIST:\n");
		source.append("TtcnLogger.log_event_str(\"complement \");\n");
		source.append("case VALUE_LIST:\n");
		source.append("TtcnLogger.log_char('(');\n");
		source.append("for (int list_count = 0; list_count < list_value.size(); list_count++) {\n");
		source.append("if (list_count > 0) {\n");
		source.append("TtcnLogger.log_event_str(\", \");\n");
		source.append("}\n");
		source.append("list_value.get(list_count).log();\n");
		source.append("}\n");
		source.append("TtcnLogger.log_char(')');\n");
		source.append("break;\n");
		source.append("default:\n");
		source.append("log_generic();\n");
		source.append("break;\n");
		source.append("}\n");
		source.append("log_ifpresent();\n");
		source.append("}\n");
	}
}
