package org.eclipse.titan.designer.AST.TTCN3.types;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.titan.designer.AST.FieldSubReference;
import org.eclipse.titan.designer.AST.TTCN3.attributes.RawAST;
import org.eclipse.titan.designer.AST.TTCN3.attributes.RawASTStruct;
import org.eclipse.titan.designer.AST.TTCN3.attributes.RawASTStruct.rawAST_coding_ext_group;
import org.eclipse.titan.designer.AST.TTCN3.attributes.RawASTStruct.rawAST_coding_field_list;
import org.eclipse.titan.designer.AST.TTCN3.attributes.RawASTStruct.rawAST_coding_field_type;
import org.eclipse.titan.designer.AST.TTCN3.attributes.RawASTStruct.rawAST_coding_fields;
import org.eclipse.titan.designer.AST.TTCN3.attributes.RawASTStruct.rawAST_coding_taglist;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.ExpressionStruct;
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

		private boolean ofType;//TODO check if this is really neded here

		/** Field variable name in java getter/setter function names and parameters */
		private String mJavaVarName;

		/** Java AST type name (for debug purposes) */
		private String mTTCN3TypeName;
	
		private String mTypeDescriptorName;

		public boolean hasRaw;
		public RawASTStruct raw;

		/**
		 * @param fieldType the string representing the type of this field in the generated code.
		 * @param fieldTemplateType the string representing the template type of this field in the generated code.
		 * @param fieldName the string representing the name of this field in the generated code.
		 * @param displayName The user readable name of the field, typically used in error messages
		 * @param isOptional true if the field is optional.
		 * @param debugName additional text printed out in a comment after the generated local variables.
		 * @param typeDescriptorName the name of the type descriptor.
		 * */
		public FieldInfo( final String fieldType, final String fieldTemplateType, final String fieldName,
						  final String displayName, final boolean isOptional, final boolean ofType, final String debugName, final String typeDescriptorName) {
			mJavaTypeName = fieldType;
			mJavaTemplateTypeName = fieldTemplateType;
			mVarName = fieldName;
			mDisplayName = displayName;
			mJavaVarName  = FieldSubReference.getJavaGetterName( mVarName );
			this.isOptional = isOptional;
			this.ofType = ofType;
			mTTCN3TypeName = debugName;
			mTypeDescriptorName = typeDescriptorName;
		}
	}

	private static class raw_option_struct {
		public boolean lengthto;
		public int lengthof;
		public ArrayList<Integer> lengthofField;
		public boolean pointerto;
		public int pointerof;
		public boolean ptrbase;
		public int extbitgroup;
		public int tag_type;
		public boolean delayedDecode;
		public ArrayList<Integer> dependentFields;
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
	 * @param hasRaw true it the type has raw attributes.
	 * @param raw the raw coding related settings if applicable.
	 */
	public static void generateValueClass(final JavaGenData aData, final StringBuilder source, final String className, final String classDisplayname,
			final List<FieldInfo> fieldInfos, final boolean hasOptional, final boolean isSet, final boolean hasRaw, final RawASTStruct raw) {
		aData.addBuiltinTypeImport("Base_Type");
		aData.addBuiltinTypeImport("Text_Buf");
		aData.addImport("java.text.MessageFormat");
		aData.addBuiltinTypeImport("TtcnLogger");
		aData.addBuiltinTypeImport("RAW.RAW_enc_tr_pos");
		aData.addBuiltinTypeImport("RAW.RAW_enc_tree");
		aData.addBuiltinTypeImport("TTCN_Buffer");
		aData.addBuiltinTypeImport("TTCN_EncDec.coding_type");
		aData.addBuiltinTypeImport("TTCN_EncDec.error_type");
		aData.addBuiltinTypeImport("TTCN_EncDec.raw_order_t");
		aData.addBuiltinTypeImport("TTCN_EncDec_ErrorContext");
		if(hasOptional) {
			aData.addBuiltinTypeImport("Optional");
			aData.addBuiltinTypeImport("Optional.optional_sel");
			aData.addBuiltinTypeImport("Base_Template.template_sel");
		}

		final boolean rawNeeded = hasRaw; //TODO can be forced optionally if needed

		if (fieldInfos.isEmpty()) {
			generateEmptyValueClass(aData, source, className, classDisplayname, rawNeeded);
			return;
		}

		aData.addBuiltinTypeImport("TitanInteger");

		source.append( "\tpublic static class " );
		source.append( className );
		source.append(" extends Base_Type");
		source.append( " {\n" );
		generateDeclaration( aData, source, fieldInfos );
		generateConstructor( source, fieldInfos, className );
		generateConstructorManyParams( source, fieldInfos, className );
		generateConstructorCopy( source, fieldInfos, className, classDisplayname );
		generateAssign( aData, source, fieldInfos, className, classDisplayname );
		generateCleanUp( source, fieldInfos );
		generateIsBound( source, fieldInfos );
		generateIsPresent( source, fieldInfos );
		generateIsValue( source, fieldInfos );
		generateOperatorEquals( source, fieldInfos, className, classDisplayname);
		generateGettersSetters( source, fieldInfos );
		generateSizeOf( source, fieldInfos );
		generateLog( source, fieldInfos );
		generateValueEncodeDecodeText(source, fieldInfos);
		generateValueEncodeDecode(aData, source, className, classDisplayname, fieldInfos, isSet, rawNeeded, raw);

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
		aData.addBuiltinTypeImport("Text_Buf");
		aData.addBuiltinTypeImport("TtcnError");
		aData.addBuiltinTypeImport("Optional");
		aData.addBuiltinTypeImport("TtcnLogger");

		if (fieldInfos.isEmpty()) {
			generateEmptyTemplateClass(aData, source, className, classDisplayName, fieldInfos, hasOptional, isSet);
			return;
		}

		aData.addBuiltinTypeImport("TitanInteger");

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
		generateTemplateLog( source, fieldInfos, className, classDisplayName );
		generateTemplateEncodeDecodeText(source, fieldInfos, className, classDisplayName);

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
				aData.addCommonLibraryImport("Optional");
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
	 * @param aNamesList sequence field variable and type names
	 * @param aClassName the class name of the record/set class
	 * @param displayName the user readable name of the type to be generated.
	 */
	private static void generateConstructorCopy( final StringBuilder aSb, final List<FieldInfo> aNamesList, final String aClassName, final String displayName ) {
		aSb.append( "\n\t\tpublic " );
		aSb.append( aClassName );
		aSb.append( "( final " );
		aSb.append( aClassName );
		aSb.append( " aOtherValue ) {\n" );
		aSb.append( "if(!aOtherValue.isBound()) {\n" );
		aSb.append( MessageFormat.format( "\t\t\tthrow new TtcnError(\"Copying of an unbound value of type {0}.\");\n", displayName ) );
		aSb.append( "}\n" );
		for ( final FieldInfo fi : aNamesList ) {
			if (fi.isOptional) {
				aSb.append(MessageFormat.format("\t\t\t{0} = new Optional<{1}>({1}.class);\n", fi.mVarName, fi.mJavaTypeName));
			} else {
				aSb.append(MessageFormat.format("\t\t\t{0} = new {1}();\n", fi.mVarName, fi.mJavaTypeName));
			}

		}
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

		source.append(MessageFormat.format("\t\tpublic {0} assign(final {0} aOtherValue ) '{'\n", aClassName));
		source.append("\t\t\tif ( !aOtherValue.isBound() ) {\n");
		source.append(MessageFormat.format("\t\t\t\tthrow new TtcnError( \"Assignment of an unbound value of type {0}\");\n", classReadableName));
		source.append("\t\t\t}\n\n");
		source.append("\t\t\tif (aOtherValue != this) {\n");
		for ( final FieldInfo fi : aNamesList ) {
			source.append(MessageFormat.format("\t\t\t\tif ( aOtherValue.get{0}().isBound() ) '{'\n", fi.mJavaVarName));
			source.append(MessageFormat.format("\t\t\t\t\tthis.{0}.assign( aOtherValue.get{1}() );\n", fi.mVarName, fi.mJavaVarName));
			source.append("\t\t\t\t} else {\n");
			source.append(MessageFormat.format("\t\t\t\t\tthis.{0}.cleanUp();\n", fi.mVarName));
			source.append("\t\t\t\t}\n");
		}
		source.append( "\t\t\t}\n\n" );
		source.append( "\t\t\treturn this;\n");
		source.append("\t\t}\n");

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
		aSb.append( "\n\t\tpublic boolean isBound() {\n" );
		for ( final FieldInfo fi : aNamesList ) {
			if (fi.isOptional) {
				aSb.append( "\t\t\tif ( optional_sel.OPTIONAL_OMIT.equals(" );
				aSb.append( fi.mVarName );
				aSb.append(".get_selection()) || ");
				aSb.append(fi.mVarName);
				aSb.append( ".isBound() ) { return true; }\n" );
			} else {
				aSb.append( "\t\t\tif ( " );
				aSb.append( fi.mVarName );
				aSb.append( ".isBound() ) { return true; }\n" );
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
		if ( aNamesList == null || aNamesList.isEmpty() ) {
			aSb.append( "\t\t\treturn false;\n" +
					"\t\t}\n" );
			return;
		}
		for ( final FieldInfo fi : aNamesList ) {
			if (fi.isOptional) {
				aSb.append( "\t\t\tif ( !optional_sel.OPTIONAL_OMIT.equals(" );
				aSb.append( fi.mVarName );
				aSb.append(".get_selection()) && !");
				aSb.append(fi.mVarName);
				aSb.append( ".isValue() ) { return false; }\n" );
			} else {
				aSb.append( "\t\t\tif ( !" );
				aSb.append( fi.mVarName );
				aSb.append( ".isValue() ) { return false; }\n" );
			}
		}
		aSb.append( "\t\t\treturn true;\n" +
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
				aSb.append( MessageFormat.format( "\t\t\tif ({0}.isPresent()) '{'\n", fi.mVarName ) );
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
		aSb.append("\t\tpublic void log() {\n");
		aSb.append("\t\t\tif (!isBound()) {\n");
		aSb.append("\t\t\t\tTtcnLogger.log_event_unbound();\n");
		aSb.append("\t\t\t\treturn;\n");
		aSb.append("\t\t\t}\n");
		aSb.append("\t\t\tTtcnLogger.log_char('{');\n");
		for (int i = 0 ; i < aNamesList.size(); i++) {
			final FieldInfo fieldInfo = aNamesList.get(i);

			if (i > 0) {
				aSb.append("\t\t\tTtcnLogger.log_char(',');\n");
			}
			aSb.append(MessageFormat.format("\t\t\tTtcnLogger.log_event_str(\" {0} := \");\n", fieldInfo.mDisplayName));
			aSb.append(MessageFormat.format("\t\t\t{0}.log();\n", fieldInfo.mVarName));
		}
		aSb.append("\t\t\tTtcnLogger.log_event_str(\" }\");\n");
		aSb.append("\t\t}\n");
	}

	/**
	 * Generating encode_text/decode_text
	 *
	 * @param aSb the output, where the java code is written
	 * @param aNamesList sequence field variable and type names
	 */
	private static void generateValueEncodeDecodeText(final StringBuilder aSb, final List<FieldInfo> aNamesList) {
		aSb.append("\t\t@Override\n");
		aSb.append("\t\tpublic void encode_text(final Text_Buf text_buf) {\n");
		for (int i = 0 ; i < aNamesList.size(); i++) {
			final FieldInfo fieldInfo = aNamesList.get(i);

			aSb.append(MessageFormat.format("\t\t\t{0}.encode_text(text_buf);\n", fieldInfo.mVarName));
		}
		aSb.append("\t\t}\n");

		aSb.append("\t\t@Override\n");
		aSb.append("\t\tpublic void decode_text(final Text_Buf text_buf) {\n");
		for (int i = 0 ; i < aNamesList.size(); i++) {
			final FieldInfo fieldInfo = aNamesList.get(i);

			aSb.append(MessageFormat.format("\t\t\t{0}.decode_text(text_buf);\n", fieldInfo.mVarName));
		}
		aSb.append("\t\t}\n");
	}

	/**
	 * Generate encode/decode
	 *
	 * @param aData only used to update imports if needed.
	 * @param source: where the source code is to be generated.
	 * @param genName: the name of the generated class representing the union/choice type.
	 * @param displayName: the user readable name of the type to be generated.
	 * @param fieldInfos: the list of information about the fields.
	 * @param rawNeeded true if encoding/decoding for RAW is to be generated.
	 * @param raw the raw coding related settings if applicable.
	 * */
	private static void generateValueEncodeDecode(final JavaGenData aData, final StringBuilder source, final String genName, final String displayName, final List<FieldInfo> fieldInfos, final boolean isSet, final boolean rawNeeded, final RawASTStruct raw) {
		source.append("@Override\n");
		source.append("public void encode(final TTCN_Typedescriptor p_td, final TTCN_Buffer p_buf, final coding_type p_coding, final int flavour) {\n");
		source.append("switch (p_coding) {\n");
		source.append("case CT_RAW: {\n");
		source.append("final TTCN_EncDec_ErrorContext errorContext = new TTCN_EncDec_ErrorContext(\"While RAW-encoding type '%s': \", p_td.name);\n");
		source.append("if (p_td.raw == null) {\n");
		source.append("TTCN_EncDec_ErrorContext.error_internal(\"No RAW descriptor available for type '%s'.\", p_td.name);\n");
		source.append("}\n");
		source.append("final RAW_enc_tr_pos rp = new RAW_enc_tr_pos(0, null);\n");
		source.append("final RAW_enc_tree root = new RAW_enc_tree(false, null, rp, 1, p_td.raw);\n");
		source.append("RAW_encode(p_td, root);\n");
		source.append("root.put_to_buf(p_buf);\n");
		source.append("errorContext.leaveContext();\n");
		source.append("break;\n");
		source.append("}\n");
		source.append("default:\n");
		source.append("throw new TtcnError(MessageFormat.format(\"Unknown coding method requested to encode type `{0}''\", p_td.name));\n");
		source.append("}\n");
		source.append("}\n\n");

		source.append("@Override\n");
		source.append("public void decode(final TTCN_Typedescriptor p_td, final TTCN_Buffer p_buf, final coding_type p_coding, final int flavour) {\n");
		source.append("switch (p_coding) {\n");
		source.append("case CT_RAW: {\n");
		source.append("final TTCN_EncDec_ErrorContext errorContext = new TTCN_EncDec_ErrorContext(\"While RAW-decoding type '%s': \", p_td.name);\n");
		source.append("if (p_td.raw == null) {\n");
		source.append("TTCN_EncDec_ErrorContext.error_internal(\"No RAW descriptor available for type '%s'.\", p_td.name);\n");
		source.append("}\n");
		source.append("raw_order_t order;\n");
		source.append("switch (p_td.raw.top_bit_order) {\n");
		source.append("case TOP_BIT_LEFT:\n");
		source.append("order = raw_order_t.ORDER_LSB;\n");
		source.append("break;\n");
		source.append("case TOP_BIT_RIGHT:\n");
		source.append("default:\n");
		source.append("order = raw_order_t.ORDER_MSB;\n");
		source.append("break;\n");
		source.append("}\n");
		source.append("int rawr = RAW_decode(p_td, p_buf, p_buf.get_len() * 8, order);\n");
		source.append("if (rawr < 0) {\n");
		source.append("error_type temp = error_type.values()[-rawr];\n");
		source.append("switch(temp) {\n");
		source.append("case ET_INCOMPL_MSG:\n");
		source.append("case ET_LEN_ERR:\n");
		source.append("TTCN_EncDec_ErrorContext.error(temp, \"Can not decode type '%s', because invalid or incomplete message was received\", p_td.name);\n");
		source.append("break;\n");
		source.append("case ET_UNBOUND:\n");
		source.append("default:\n");
		source.append("TTCN_EncDec_ErrorContext.error(error_type.ET_INVAL_MSG, \"Can not decode type '%s', because invalid or incomplete message was received\", p_td.name);\n");
		source.append("break;\n");
		source.append("}\n");
		source.append("}\n");
		source.append("errorContext.leaveContext();\n");
		source.append("break;\n");
		source.append("}\n");
		source.append("default:\n");
		source.append("throw new TtcnError(MessageFormat.format(\"Unknown coding method requested to decode type `{0}''\", p_td.name));\n");
		source.append("}\n");
		source.append("}\n\n");

		if (rawNeeded) {
			final ArrayList<raw_option_struct> raw_options = new ArrayList<RecordSetCodeGenerator.raw_option_struct>(fieldInfos.size());
			final AtomicBoolean hasLengthto = new AtomicBoolean();
			final AtomicBoolean hasPointer = new AtomicBoolean();
			final AtomicBoolean hasCrosstag = new AtomicBoolean();
			final AtomicBoolean has_ext_bit = new AtomicBoolean();
			set_raw_options(isSet, fieldInfos, raw != null, raw, raw_options, hasLengthto, hasPointer, hasCrosstag, has_ext_bit);

			source.append("@Override\n");
			source.append("public int RAW_encode(final TTCN_Typedescriptor p_td, final RAW_enc_tree myleaf) {\n");
			source.append("if (!isBound()) {\n");
			source.append("TTCN_EncDec_ErrorContext.error(error_type.ET_UNBOUND, \"Encoding an unbound value.\", \"\");\n");
			source.append("}\n");

			source.append("int encoded_length = 0;\n");
			source.append("myleaf.isleaf = false;\n");
			source.append(MessageFormat.format("myleaf.num_of_nodes = {0};\n", fieldInfos.size()));
			source.append(MessageFormat.format("myleaf.nodes = new RAW_enc_tree[{0}];\n", fieldInfos.size()));
			for (int i = 0 ; i < fieldInfos.size(); i++) {
				final FieldInfo fieldInfo = fieldInfos.get(i);
	
				if (fieldInfo.isOptional) {
					source.append(MessageFormat.format("if ({0}.isPresent()) '{'\n", fieldInfo.mVarName));
					source.append(MessageFormat.format("myleaf.nodes[{0}] = new RAW_enc_tree(true, myleaf, myleaf.curr_pos, {0}, {1}_descr_.raw);\n", i, fieldInfo.mTypeDescriptorName));
					source.append("} else {\n");
					source.append(MessageFormat.format("myleaf.nodes[{0}] = null;\n", i));
					source.append("}\n");
				} else {
					source.append(MessageFormat.format("myleaf.nodes[{0}] = new RAW_enc_tree(true, myleaf, myleaf.curr_pos, {0}, {1}_descr_.raw);\n", i, fieldInfo.mTypeDescriptorName));
				}
			}
			final int ext_bit_group_length = raw == null || raw.ext_bit_groups == null ? 0 : raw.ext_bit_groups.size();
			for (int i = 0; i < ext_bit_group_length; i++) {
				final rawAST_coding_ext_group tempGroup = raw.ext_bit_groups.get(i);
				if (tempGroup.ext_bit != RawASTStruct.XDEFNO) {
					source.append("{\n");
					source.append(MessageFormat.format("int node_idx = {0};\n", tempGroup.from));
					source.append(MessageFormat.format("while (node_idx <= {0} && myleaf.nodes[node_idx] == null) '{'\n", tempGroup.to));
					source.append("node_idx++;\n");
					source.append("}\n");
					source.append("if (myleaf.nodes[node_idx] != null) {\n");
					source.append("myleaf.nodes[node_idx].ext_bit_handling = 1;\n");
					source.append(MessageFormat.format("myleaf.nodes[node_idx].ext_bit = ext_bit_t.{0};\n", tempGroup.ext_bit == RawASTStruct.XDEFYES? "EXT_BIT_YES" : "EXT_BIT_REVERSE"));
					source.append("}\n");
					source.append(MessageFormat.format("node_idx = {0};\n", tempGroup.to));
					source.append(MessageFormat.format("while (node_idx >= {0} && myleaf.nodes[node_idx] == null) '{'\n", tempGroup.from));
					source.append("node_idx--;\n");
					source.append("}\n");
					source.append("if (myleaf.nodes[node_idx] != null) {\n");
					source.append("myleaf.nodes[node_idx].ext_bit_handling += 2;;\n");
					source.append("}\n");
					source.append("}\n");
				}
			}
			for (int i = 0 ; i < fieldInfos.size(); i++) {
				final FieldInfo fieldInfo = fieldInfos.get(i);

				if (fieldInfo.isOptional) {
					source.append(MessageFormat.format("if ({0}.isPresent()) '{'\n", fieldInfo.mVarName));
				}

				if (raw_options.get(i).lengthto && fieldInfo.raw.lengthindex == null && fieldInfo.raw.union_member_num == 0) {
					aData.addBuiltinTypeImport("RAW.calc_type");
					aData.addBuiltinTypeImport("RAW.RAW_enc_lengthto");

					source.append(MessageFormat.format("encoded_length += {0};\n", fieldInfo.raw.fieldlength));
					source.append(MessageFormat.format("myleaf.nodes[{0}].calc = calc_type.CALC_LENGTH;\n", i));
					source.append(MessageFormat.format("myleaf.nodes[{0}].coding_descr = {1}_descr_;\n", i, fieldInfo.mTypeDescriptorName));

					final int lengthtoSize = fieldInfo.raw.lengthto == null ? 0 : fieldInfo.raw.lengthto.size();
					source.append(MessageFormat.format("myleaf.nodes[{0}].length = {1};\n", i, fieldInfo.raw.fieldlength));
					source.append(MessageFormat.format("myleaf.nodes[{0}].lengthto = new RAW_enc_lengthto({1}, new RAW_enc_tr_pos[{1}], {2}, {3});\n", i, lengthtoSize, fieldInfo.raw.unit, fieldInfo.raw.lengthto_offset));
					for (int a = 0; a < lengthtoSize; a++) {
						if (fieldInfos.get(fieldInfo.raw.lengthto.get(a)).isOptional) {
							source.append(MessageFormat.format("if ({0}.isPresent()) '{'\n", fieldInfos.get(fieldInfo.raw.lengthto.get(a)).mVarName));
						}
						source.append(MessageFormat.format("myleaf.nodes[{0}].lengthto.fields[{1}] = new RAW_enc_tr_pos(myleaf.nodes[{2}].curr_pos.level, myleaf.nodes[{2}].curr_pos.pos);\n", i, a, fieldInfo.raw.lengthto.get(a)));
						if (fieldInfos.get(fieldInfo.raw.lengthto.get(a)).isOptional) {
							source.append("} else {\n");
							source.append(MessageFormat.format("myleaf.nodes[{0}].lengthto.fields[{1}] = new RAW_enc_tr_pos(0, null);\n", i, a));
							source.append("}\n");
						}
					}
				} else if (raw_options.get(i).pointerto) {
					aData.addBuiltinTypeImport("RAW.calc_type");
					aData.addBuiltinTypeImport("RAW.RAW_enc_pointer");

					if (fieldInfos.get(fieldInfo.raw.pointerto).isOptional) {
						source.append(MessageFormat.format("if ({0}.isPresent()) '{'\n", fieldInfos.get(fieldInfo.raw.pointerto).mVarName));
					}
					source.append(MessageFormat.format("encoded_length += {0};\n", fieldInfo.raw.fieldlength));
					source.append(MessageFormat.format("myleaf.nodes[{0}].calc = calc_type.CALC_POINTER;\n", i));
					source.append(MessageFormat.format("myleaf.nodes[{0}].coding_descr = {1}_descr_;\n", i, fieldInfo.mTypeDescriptorName));

					source.append(MessageFormat.format("myleaf.nodes[{0}].pointerto = new RAW_enc_pointer(new RAW_enc_tr_pos(myleaf.nodes[{1}].curr_pos.level, myleaf.nodes[{1}].curr_pos.pos), {2}, {3}, {4});\n", i, fieldInfo.raw.pointerto, fieldInfo.raw.ptroffset, fieldInfo.raw.unit, fieldInfo.raw.pointerbase));
					source.append(MessageFormat.format("myleaf.nodes[{0}].length = {1};\n", i, fieldInfo.raw.fieldlength));
					if (fieldInfos.get(fieldInfo.raw.pointerto).isOptional) {
						source.append("} else {\n");
						source.append("TitanInteger atm = new TitanInteger(0);\n");
						source.append(MessageFormat.format("encoded_length += atm.RAW_encode({0}_descr_, myleaf.nodes[{1}]);\n", fieldInfo.mTypeDescriptorName, i));
						source.append("}\n");
					}
				} else {
					source.append(MessageFormat.format("encoded_length += {0}{1}.RAW_encode({2}_descr_, myleaf.nodes[{3}]);\n", fieldInfo.mVarName, fieldInfo.isOptional? ".get()" : "", fieldInfo.mTypeDescriptorName, i));
				}
				if (fieldInfo.isOptional) {
					source.append("}\n");
				}
			}
			for (int i = 0; i < fieldInfos.size(); i++) {
				final FieldInfo fieldInfo = fieldInfos.get(i);

				if (raw_options.get(i).lengthto && fieldInfo.raw.lengthindex != null) {
					aData.addBuiltinTypeImport("RAW.calc_type");
					aData.addBuiltinTypeImport("RAW.RAW_enc_lengthto");

					if (fieldInfo.isOptional) {
						source.append(MessageFormat.format("if ({0}.isPresent()) '{'\n", fieldInfo.mVarName));
					}

					source.append(MessageFormat.format("if (myleaf.nodes[{0}].nodes[{1}] != null) '{'\n", i, fieldInfo.raw.lengthindex.nthfield));
					source.append(MessageFormat.format("myleaf.nodes[{0}].nodes[{1}].calc = calc_type.CALC_LENGTH;\n", i, fieldInfo.raw.lengthindex.nthfield));
					source.append(MessageFormat.format("myleaf.nodes[{0}].nodes[{1}].coding_descr = {2}_descr_;\n", i, fieldInfo.raw.lengthindex.nthfield, fieldInfo.raw.lengthindex.typedesc));

					final int lengthtoSize = fieldInfo.raw.lengthto == null ? 0 : fieldInfo.raw.lengthto.size();
					source.append(MessageFormat.format("myleaf.nodes[{0}].nodes[{1}].lengthto = new RAW_enc_lengthto({2}, new RAW_enc_tr_pos[{2}], {3}, {4});\n", i, fieldInfo.raw.lengthindex.nthfield, lengthtoSize, fieldInfo.raw.unit, fieldInfo.raw.lengthto_offset));
					for (int a = 0; a < lengthtoSize; a++) {
						if (fieldInfos.get(fieldInfo.raw.lengthto.get(a)).isOptional) {
							source.append(MessageFormat.format("if ({0}.isPresent()) '{'\n", fieldInfos.get(fieldInfo.raw.lengthto.get(a)).mVarName));
						}
						source.append(MessageFormat.format("myleaf.nodes[{0}].nodes[{1}].lengthto.fields[{2}] = new RAW_enc_tr_pos(myleaf.nodes[{3}].curr_pos.level, myleaf.nodes[{3}].curr_pos.pos);\n", i, fieldInfo.raw.lengthindex.nthfield, a, fieldInfo.raw.lengthto.get(a)));
						if (fieldInfos.get(fieldInfo.raw.lengthto.get(a)).isOptional) {
							source.append("} else {\n");
							source.append(MessageFormat.format("myleaf.nodes[{0}].nodes[{1}].lengthto.fields[{2}] = new RAW_enc_tr_pos(0, null);\n", i, fieldInfo.raw.lengthindex.nthfield, a));
							source.append("}\n");
						}
					}
					source.append("}\n");

					if (fieldInfo.isOptional) {
						source.append("}\n");
					}
				}
				if (raw_options.get(i).lengthto && fieldInfo.raw.union_member_num > 0) {
					aData.addBuiltinTypeImport("RAW.calc_type");
					aData.addBuiltinTypeImport("RAW.RAW_enc_lengthto");

					if (fieldInfo.isOptional) {
						source.append(MessageFormat.format("if ({0}.isPresent()) ", fieldInfo.mVarName));
					}
					source.append("{\n");

					source.append("int sel_field = 0;\n");
					source.append(MessageFormat.format("while (myleaf.nodes[{0}].nodes[sel_field] == null) '{'\n", i));
					source.append("sel_field++;\n");
					source.append("}\n");

					source.append(MessageFormat.format("myleaf.nodes[{0}].nodes[sel_field].calc = calc_type.CALC_LENGTH;\n", i));
					source.append(MessageFormat.format("myleaf.nodes[{0}].nodes[sel_field].coding_descr = {1}_descr_;\n", i, fieldInfo.raw.lengthindex.typedesc));

					final int lengthtoSize = fieldInfo.raw.lengthto == null ? 0 : fieldInfo.raw.lengthto.size();
					source.append(MessageFormat.format("myleaf.nodes[{0}].nodes[sel_field].length = {1};\n", i, fieldInfo.raw.fieldlength));
					source.append(MessageFormat.format("myleaf.nodes[{0}].nodes[sel_field].lengthto = new RAW_enc_lengthto({1}, new RAW_enc_tr_pos[{1}], {2}, {3});\n", i, lengthtoSize, fieldInfo.raw.unit, fieldInfo.raw.lengthto_offset));
					for (int a = 0; a < lengthtoSize; a++) {
						if (fieldInfos.get(fieldInfo.raw.lengthto.get(a)).isOptional) {
							source.append(MessageFormat.format("if ({0}.isPresent()) '{'\n", fieldInfos.get(fieldInfo.raw.lengthto.get(a)).mVarName));
						}
						source.append(MessageFormat.format("myleaf.nodes[{0}].nodes[sel_field].lengthto.fields[{1}] = new RAW_enc_tr_pos(myleaf.nodes[{2}].curr_pos.level, myleaf.nodes[{2}].curr_pos.pos);\n", i, a, fieldInfo.raw.lengthto.get(a)));
						if (fieldInfos.get(fieldInfo.raw.lengthto.get(a)).isOptional) {
							source.append("} else {\n");
							source.append(MessageFormat.format("myleaf.nodes[{0}].nodes[sel_field].lengthto.fields[{1}] = new RAW_enc_tr_pos(0, null);\n", i, a));
							source.append("}\n");
						}
					}
					source.append("}\n");

					if (fieldInfo.isOptional) {
						source.append("}\n");
					}
				}
				final int tag_type = raw_options.get(i).tag_type;
				if ( tag_type > 0 && raw.taglist.list.get(tag_type -1).fields != null && raw.taglist.list.get(tag_type - 1).fields.size() > 0) {
					final rawAST_coding_taglist cur_choice = raw.taglist.list.get(tag_type -1);
					source.append("if (");
					if (fieldInfo.isOptional) {
						source.append(MessageFormat.format("{0}.isPresent() && (", fieldInfo.mVarName));
					}
					genRawFieldChecker(source, cur_choice, false);
					if (fieldInfo.isOptional) {
						source.append(")");
					}
					source.append(") {\n");
					genRawTagChecker(source, cur_choice);
					source.append("}\n");
				}
				final int presenceLength = fieldInfo.raw == null || fieldInfo.raw.presence == null || fieldInfo.raw.presence.fields == null ? 0 : fieldInfo.raw.presence.fields.size();
				if (fieldInfo.hasRaw && presenceLength > 0) {
					source.append("if (");
					if (fieldInfo.isOptional) {
						source.append(MessageFormat.format("{0}.isPresent() && (", fieldInfo.mVarName));
					}
					genRawFieldChecker(source, fieldInfo.raw.presence, false);
					if (fieldInfo.isOptional) {
						source.append(")");
					}
					source.append(") {\n");
					genRawTagChecker(source, fieldInfo.raw.presence);
					source.append("}\n");
				}
				final int crosstagLength = fieldInfo.raw == null || fieldInfo.raw.crosstaglist == null || fieldInfo.raw.crosstaglist.list == null ? 0 : fieldInfo.raw.crosstaglist.list.size();
				if (fieldInfo.hasRaw && crosstagLength > 0) {
					if (fieldInfo.isOptional) {
						source.append(MessageFormat.format("if ({0}.isPresent()) '{'\n", fieldInfo.mVarName));
					}
					source.append(MessageFormat.format("switch ({0}{1}.get_selection()) '{'\n", fieldInfo.mVarName, fieldInfo.isOptional ? ".get()":""));
					for (int a = 0; a < crosstagLength; a++) {
						final rawAST_coding_taglist cur_choice = fieldInfo.raw.crosstaglist.list.get(a);
						final int curSize = cur_choice == null || cur_choice.fields == null ? 0 : cur_choice.fields.size();
						if(curSize > 0) {
							source.append(MessageFormat.format("case ALT_{0}:\n", cur_choice.varName));
							source.append("if (");
							genRawFieldChecker(source, cur_choice, false);
							source.append(") {\n");
							if (cur_choice.fields.get(0).isOmitValue) {
								if (cur_choice.fields.get(0).fields.size() != 1) {
									//FIXME report error "omit value with multiple fields in CROSSTAG"
								}
								source.append(MessageFormat.format("encoded_length -= myleaf.nodes[{0}].length;\n", cur_choice.fields.get(0).fields.get(0).nthfield));
								source.append(MessageFormat.format("myleaf.nodes[{0}] = null;\n", cur_choice.fields.get(0).fields.get(0).nthfield));
								
							} else {
								source.append(MessageFormat.format("RAW_enc_tr_pos pr_pos = new RAW_enc_tr_pos(myleaf.curr_pos.level + {0}, new int[] '{'", cur_choice.fields.get(0).fields.size()));
								for (int ll = 0 ; ll < cur_choice.fields.get(0).fields.size(); ll++) {
									if (ll > 0) {
										source.append(',');
									}
									source.append(cur_choice.fields.get(0).fields.get(ll).nthfield);
								}
								source.append("});\n");
								source.append("RAW_enc_tree temp_leaf = myleaf.get_node(pr_pos);\n");
								source.append("if (temp_leaf != null) {\n");
								source.append(MessageFormat.format("{0}.RAW_encode({1}_descr_, temp_leaf);\n", cur_choice.fields.get(0).expression.expression, cur_choice.fields.get(0).fields.get(cur_choice.fields.get(0).fields.size()-1).typedesc));
								source.append("} else {\n");
								source.append("TTCN_EncDec_ErrorContext.error(error_type.ET_OMITTED_TAG, \"Encoding a tagged, but omitted value.\", \"\");\n");
								source.append("}\n");
							}
							source.append("}\n");
							source.append("break;\n");
						}
					}
					source.append("default:\n");
					source.append("break;\n");
					source.append("}\n");
					if (fieldInfo.isOptional) {
						source.append("}\n");
					}
				}
			}

			// presence
			final int presenceLength = raw == null || raw.presence == null || raw.presence.fields == null ? 0 : raw.presence.fields.size();
			if (presenceLength > 0) {
				source.append(" if (");
				genRawFieldChecker(source, raw.presence, false);
				source.append(" ) {\n");
				genRawTagChecker(source, raw.presence);
				source.append("}\n");
			}

			source.append("myleaf.length = encoded_length;\n");
			source.append("return encoded_length;\n");
			source.append("}\n\n");

			source.append("@Override\n");
			source.append("public int RAW_decode(final TTCN_Typedescriptor p_td, final TTCN_Buffer buff, int limit, final raw_order_t top_bit_ord, final boolean no_err, final int sel_field, final boolean first_call) {\n");
			if (isSet) {
				int mand_num = 0;
				for (int i = 0; i < fieldInfos.size(); i++) {
					if (!fieldInfos.get(i).isOptional) {
						mand_num++;
					}
				}
				source.append("int prepaddlength = buff.increase_pos_padd(p_td.raw.prepadding);\n");
				source.append("limit -= prepaddlength;\n");
				source.append("int decoded_length = 0;\n");
				source.append("int field_map[] = new int[]{");
				for (int i = 0 ; i < fieldInfos.size(); i++) {
					if (i != 0) {
						source.append(',');
					}
					source.append('0');
				}
				source.append("};\n");
				if (mand_num > 0) {
					source.append("int nof_mand_fields = 0;\n");
				}
				for (int i = 0 ; i < fieldInfos.size(); i++) {
					final FieldInfo fieldInfo = fieldInfos.get(i);

					if (fieldInfo.isOptional) {
						source.append(MessageFormat.format("{0}.assign(template_sel.OMIT_VALUE);\n", fieldInfo.mVarName));
					}
				}
				source.append("raw_order_t local_top_order;\n");
				source.append("if (p_td.raw.top_bit_order == top_bit_order_t.TOP_BIT_INHERITED) {\n");
				source.append("local_top_order = top_bit_ord;\n");
				source.append("} else if (p_td.raw.top_bit_order == top_bit_order_t.TOP_BIT_RIGHT) {\n");
				source.append("local_top_order = raw_order_t.ORDER_MSB;\n");
				source.append("} else {\n");
				source.append("local_top_order = raw_order_t.ORDER_LSB;\n");
				source.append("}\n");

				source.append("while (limit > 0) {\n");
				source.append("int fl_start_pos = buff.get_pos_bit();\n");
				for (int i = 0 ; i < fieldInfos.size(); i++) {
					// tagged fields
					final FieldInfo fieldInfo = fieldInfos.get(i);
					final int tag_type = raw_options.get(i).tag_type;

					if (tag_type > 0 && raw.taglist.list.get(tag_type - 1).fields.size() > 0) {
						final rawAST_coding_taglist cur_choice = raw.taglist.list.get(raw_options.get(i).tag_type - 1);
						boolean has_fixed = false;
						boolean has_variable = false;
						boolean flag_needed = false;
						for (int j = 0; j < cur_choice.fields.size(); j++) {
							if (cur_choice.fields.get(j).start_pos >= 0) {
								if (has_fixed || has_variable) {
									flag_needed = true;
								}
								has_fixed = true;
							} else {
								if (has_fixed) {
									flag_needed = true;
								}
								has_variable = true;
							}
							if (has_fixed && has_variable) {
								break;
							}
						}

						source.append(MessageFormat.format("if (field_map[{0}] == 0) '{'\n", i));
						if (flag_needed) {
							source.append("boolean already_failed = true;\n");
						}
						if (has_fixed) {
							boolean first_fixed= true;
							source.append("raw_order_t temporal_top_order;\n");
							source.append("int temporal_decoded_length;\n");
							for (int j = 0; j < cur_choice.fields.size(); j++) {
								final rawAST_coding_field_list cur_field_list = cur_choice.fields.get(j);
								if (cur_field_list.start_pos < 0) {
									continue;
								}
								if (!first_fixed) {
									source.append("if (!already_failed) {\n");
								}
								for (int k = cur_field_list.fields.size() - 1; k > 0; k--) {
									source.append(MessageFormat.format("if ({0}_descr_.raw.top_bit_order == top_bit_order_t.TOP_BIT_RIGHT) '{'\n", cur_field_list.fields.get(k - 1).typedesc));
									source.append("temporal_top_order = raw_order_t.ORDER_MSB;\n");
									source.append(MessageFormat.format("} else if ({0}_descr_.raw.top_bit_order == top_bit_order_t.TOP_BIT_LEFT) '{'\n", cur_field_list.fields.get(k - 1).typedesc));
									source.append("temporal_top_order = raw_order_t.ORDER_LSB;\n");
									source.append("} else ");
								}
								source.append("{\n");
								source.append("temporal_top_order = top_bit_ord;\n");
								source.append("}\n");
								source.append(MessageFormat.format("{0} temporal_{1} = new {0}();\n", cur_field_list.fields.get(cur_field_list.fields.size() - 1).type, j));
								source.append(MessageFormat.format("buff.set_pos_bit(fl_start_pos + {0});\n", cur_field_list.start_pos));
								source.append(MessageFormat.format("temporal_decoded_length = temporal_{0}.RAW_decode({1}_descr_, buff, limit, temporal_top_order, true, -1, true);\n", j, cur_field_list.fields.get(cur_field_list.fields.size() - 1).typedesc));
								source.append("buff.set_pos_bit(fl_start_pos);\n");
								source.append(MessageFormat.format("if (temporal_decoded_length > 0 && temporal_{0}.operatorEquals({1})) '{'\n", j, cur_field_list.expression.expression));
								source.append(MessageFormat.format("int decoded_field_length = {0}{1}.RAW_decode({2}_descr_, buff, limit, local_top_order, true, -1, true);\n", fieldInfo.mVarName, fieldInfo.isOptional? ".get()" : "", fieldInfo.mTypeDescriptorName));
								source.append(MessageFormat.format("if (decoded_field_length {0} 0 && (", fieldInfo.isOptional ? ">" : ">="));
								genRawFieldChecker(source, cur_choice, true);
								source.append(")) {\n");
								source.append("decoded_length += decoded_field_length;\n");
								source.append("limit -= decoded_field_length;\n");
								if (!fieldInfo.isOptional) {
									source.append("nof_mand_fields++;\n");
								}
								source.append(MessageFormat.format("field_map[{0}] = 1;", i));
								source.append("continue;\n");
								source.append("} else {\n");
								source.append("buff.set_pos_bit(fl_start_pos);\n");
								if (fieldInfo.isOptional) {
									source.append(MessageFormat.format("{0}.assign(template_sel.OMIT_VALUE);\n", fieldInfo.mVarName));
								}
								if (flag_needed) {
									source.append("already_failed = true;\n");
								}
								source.append("}\n");
								source.append("}\n");
								if (first_fixed) {
									first_fixed = false;
								} else {
									source.append("}\n");
								}
							}
						}

						if (has_variable) {
							if (flag_needed) {
								source.append("if (!already_failed) {\n");
							}
							source.append(MessageFormat.format("int decoded_field_length = {0}{1}.RAW_decode({2}_descr_, buff, limit, local_top_order, true, -1, true);\n", fieldInfo.mVarName, fieldInfo.isOptional? ".get()" : "", fieldInfo.mTypeDescriptorName));
							source.append(MessageFormat.format("if (decoded_field_length {0} 0 && (", fieldInfo.isOptional ? ">" : ">="));
							genRawFieldChecker(source, cur_choice, true);
							source.append(")) {\n");
							source.append("decoded_length += decoded_field_length;\n");
							source.append("limit -= decoded_field_length;\n");
							if (!fieldInfo.isOptional) {
								source.append("nof_mand_fields++;\n");
							}
							source.append(MessageFormat.format("field_map[{0}] = 1;", i));
							source.append("continue;\n");
							source.append("} else {\n");
							source.append("buff.set_pos_bit(fl_start_pos);\n");
							if (fieldInfo.isOptional) {
								source.append(MessageFormat.format("{0}.assign(template_sel.OMIT_VALUE);\n", fieldInfo.mVarName));
							}
							source.append("}\n");
							if (flag_needed) {
								source.append("}\n");
							}
						}
						source.append("}\n");
					}
				}
				for (int i = 0 ; i < fieldInfos.size(); i++) {
					// untagged fields
					final FieldInfo fieldInfo = fieldInfos.get(i);

					if (raw_options.get(i).tag_type == 0) {
						boolean repeatable;
						if (fieldInfo.ofType && fieldInfo.raw != null && fieldInfo.raw.repeatable == RawAST.XDEFYES) {
							repeatable = true;
						} else {
							repeatable = false;
							source.append(MessageFormat.format("if (field_map[{0}] == 0) ", i));
						}

						source.append("{\n");
						source.append(MessageFormat.format("int decoded_field_length = {0}{1}.RAW_decode({2}_descr_, buff, limit, top_bit_ord, true, -1, ", fieldInfo.mVarName, fieldInfo.isOptional ? ".get()":"", fieldInfo.mTypeDescriptorName));
						if (repeatable) {
							source.append(MessageFormat.format("field_map[{0}]);\n", i));
						} else {
							source.append("true);\n");
						}

						source.append(MessageFormat.format("if (decoded_field_length {0} 0) '{'\n", fieldInfo.isOptional ? ">" : ">="));
						source.append("decoded_length += decoded_field_length;\n");
						source.append("limit -= decoded_field_length;\n");
						if (repeatable) {
							if (!fieldInfo.isOptional) {
								source.append(MessageFormat.format("if (field_map[{0}] == 0 ) '{'\n", i));
								source.append("nof_mand_fields++;\n");
								source.append("}\n");
							}
							source.append(MessageFormat.format("field_map[{0}]++;\n", i));
						} else {
							if (!fieldInfo.isOptional) {
								source.append("nof_mand_fields++;\n");
							}
							source.append(MessageFormat.format("field_map[{0}] = 1;\n", i));
						}

						source.append("continue;\n");
						source.append("} else {\n");
						source.append("buff.set_pos_bit(fl_start_pos);\n");
						if (fieldInfo.isOptional) {
							if (repeatable) {
								source.append(MessageFormat.format("if (field_map[{0}] == 0) ", i));
							}
							source.append(MessageFormat.format("{0}.assign(template_sel.OMIT_VALUE);\n", fieldInfo.mVarName));
						}

						source.append("}\n");
						source.append("}\n");
					}
				}
				for (int i = 0 ; i < fieldInfos.size(); i++) {
					// tag OTHERWISE
					final FieldInfo fieldInfo = fieldInfos.get(i);
					final int tag_type = raw_options.get(i).tag_type;

					if (tag_type > 0 && raw.taglist.list.get(tag_type - 1).fields.size() == 0) {
						source.append(MessageFormat.format("if (field_map[{0}] == 0) ", i));
						source.append(MessageFormat.format("int decoded_field_length = {0}{1}.RAW_decode({2}_descr_, buff, limit, top_bit_ord, true, -1, ", fieldInfo.mVarName, fieldInfo.isOptional ? ".get()":"", fieldInfo.mTypeDescriptorName));
						source.append(MessageFormat.format("if (decoded_field_length {0} 0) '{'\n", fieldInfo.isOptional ? ">" : ">="));
						source.append("decoded_length += decoded_field_length;\n");
						source.append("limit -= decoded_field_length;\n");
						if (!fieldInfo.isOptional) {
							source.append("nof_mand_fields++;\n");
						}

						source.append(MessageFormat.format("field_map[{0}] = 1;\n", i));
						source.append("continue;\n");
						source.append("} else {\n");
						source.append("buff.set_pos_bit(fl_start_pos);\n");
						if (fieldInfo.isOptional) {
							source.append(MessageFormat.format("{0}.assign(template_sel.OMIT_VALUE);\n", fieldInfo.mVarName));
						}

						source.append("}\n");
						source.append("}\n");
					}
				}

				source.append("break;\n");
				source.append("}\n");

				if (mand_num > 0) {
					source.append(MessageFormat.format("if (nof_mand_fields != {0}) '{'\n", fieldInfos.size()));
					source.append("return limit > 0 ? -1 : TTCN_EncDec.error_type.ET_INCOMPL_MSG.ordinal();\n");
					source.append("}\n");
				}
				source.append("return decoded_length + prepaddlength + buff.increase_pos_padd(p_td.raw.padding);\n");
			} else {
				source.append("int prepaddlength = buff.increase_pos_padd(p_td.raw.prepadding);\n");
				source.append("limit -= prepaddlength;\n");
				source.append("int last_decoded_pos = buff.get_pos_bit();\n");
				source.append("int decoded_length = 0;\n");
				source.append("int decoded_field_length = 0;\n");
				source.append("raw_order_t local_top_order;\n");

				if (hasCrosstag.get()) {
					source.append("int selected_field = -1;\n");
				}
				if (raw != null && raw.ext_bit_groups != null && raw.ext_bit_groups.size() > 0) {
					source.append("int group_limit = 0;\n");
				}
				source.append("if (p_td.raw.top_bit_order == top_bit_order_t.TOP_BIT_INHERITED) {\n");
				source.append("local_top_order = top_bit_ord;\n");
				source.append("} else if (p_td.raw.top_bit_order == top_bit_order_t.TOP_BIT_RIGHT) {\n");
				source.append("local_top_order = raw_order_t.ORDER_MSB;\n");
				source.append("} else {\n");
				source.append("local_top_order = raw_order_t.ORDER_LSB;\n");
				source.append("}\n");

				if (has_ext_bit.get()) {
					source.append("{\n");
					source.append("char data[] = buff.get_read_data();\n");
					source.append("int count = 1;\n");
					source.append("int mask = 1 << (local_top_order == raw_order_t.ORDER_LSB ? 0 : 7);\n");
					source.append("if (p_td.raw.extension_bit == ext_bit_t.EXT_BIT_YES) {\n");
					source.append("while ((data[count - 1] & mask) == 0 && count * 8 < limit) {\n");
					source.append("count++;\n");
					source.append("}\n");
					source.append("} else {\n");
					source.append("while ((data[count - 1] & mask) != 0 && count * 8 < limit) {\n");
					source.append("count++;\n");
					source.append("}\n");
					source.append("}\n");
					source.append("if (limit > 0) {\n");
					source.append("limit = count * 8;\n");
					source.append("}\n");
					source.append("}\n");
				}
				if (hasPointer.get()) {
					source.append("int end_of_available_data = last_decoded_pos + limit;\n");
				}
				for (int i = 0; i < fieldInfos.size(); i++) {
					if (raw_options.get(i).pointerof > 0) {
						source.append(MessageFormat.format("int start_of_field{0} = -1;\n", i));
					}
					if (raw_options.get(i).ptrbase) {
						source.append(MessageFormat.format("int start_pos_of_field{0} = -1;\n", i));
					}
					if (raw_options.get(i).lengthto) {
						source.append(MessageFormat.format("int value_of_length_field{0} = 0;\n", i));
					}
				}

				final AtomicInteger prev_ext_group = new AtomicInteger(0);
				for (int i = 0 ; i < fieldInfos.size(); i++) {
					final FieldInfo fieldInfo = fieldInfos.get(i);
					final raw_option_struct tempRawOption = raw_options.get(i);

					if (tempRawOption.delayedDecode) {
						final ExpressionStruct expression = new ExpressionStruct();
						genRawFieldDecodeLimit(aData, expression, fieldInfos, i, raw, raw_options);
						if (expression.preamble.length() > 0) {
							source.append(expression.preamble);
						}
						source.append("if (");
						source.append(expression.expression);
						source.append(MessageFormat.format(" < {0}) '{'\n", fieldInfo.raw.length));
						source.append("return -1 * TTCN_EncDec.error_type.ET_LEN_ERR.ordinal();\n");
						source.append("}\n");
						source.append(MessageFormat.format("int start_of_field{0} = buff.get_pos_bit();\n", i));
						source.append(MessageFormat.format("buff.set_pos_bit(start_of_field{0} + {1});\n", i, fieldInfo.raw.length));
						source.append(MessageFormat.format("decoded_length += {0};\n", fieldInfo.raw.length));
						source.append(MessageFormat.format("last_decoded_pos += {0};\n", fieldInfo.raw.length));
						source.append(MessageFormat.format("limit -= {0};\n", fieldInfo.raw.length));
						for (int j = 0; j < tempRawOption.lengthof; j++) {
							source.append(MessageFormat.format("value_of_length_field{0} -= {1};\n", tempRawOption.lengthofField.get(j), fieldInfo.raw.length));
						}
					} else {
						genRawDecodeRecordField(aData, source, fieldInfos, i, raw, raw_options, false, prev_ext_group);
						
						if (tempRawOption.dependentFields != null && tempRawOption.dependentFields.size() > 0) {
							for (int j = 0; j < tempRawOption.dependentFields.size(); j++) {
								final int dependent_field_index = tempRawOption.dependentFields.get(j);
								source.append(MessageFormat.format("buff.set_pos_bit(start_of_field{0});\n", dependent_field_index));
								genRawDecodeRecordField(aData, source, fieldInfos, dependent_field_index, raw, raw_options, true, prev_ext_group);
							}
							if (i < fieldInfos.size() - 1) {
								/* seek back if there are more regular fields to decode */
								source.append("buff.set_pos_bit(last_decoded_pos);\n");
							}
						}
					}
				}
	
				
				if (raw != null && raw.presence != null && raw.presence.fields != null && raw.presence.fields.size() > 0) {
					source.append("if (");
					genRawFieldChecker(source, raw.presence, false);
					source.append(") {\n");
					source.append("return -1;");
					source.append("}\n");
				}
				source.append("buff.set_pos_bit(last_decoded_pos);\n");
				source.append("return decoded_length + prepaddlength + buff.increase_pos_padd(p_td.raw.padding);\n");
			}

			source.append("}\n\n");
		}
	}

	//FIXME comment
	private static void set_raw_options(final boolean isSet, final List<FieldInfo> fieldInfos, final boolean hasRaw, final RawASTStruct raw, final ArrayList<raw_option_struct> raw_options, final AtomicBoolean hasLengthto, final AtomicBoolean hasPointer, final AtomicBoolean hasCrosstag, final AtomicBoolean has_ext_bit) {
		for (int i = 0; i < fieldInfos.size(); i++) {
			final raw_option_struct tempRawOption = new raw_option_struct();
			raw_options.add(tempRawOption);

			tempRawOption.lengthto = false;
			tempRawOption.lengthof = 0;
			tempRawOption.lengthofField = null;
			tempRawOption.pointerto = false;
			tempRawOption.pointerof = 0;
			tempRawOption.ptrbase = false;
			tempRawOption.extbitgroup = 0;
			tempRawOption.tag_type = 0;
			tempRawOption.delayedDecode = false;
			tempRawOption.dependentFields = null;
		}
		hasLengthto.set(false);
		hasPointer.set(false);
		hasCrosstag.set(false);
		has_ext_bit.set(hasRaw && raw.extension_bit != RawASTStruct.XDEFNO && raw.extension_bit != RawASTStruct.XDEFDEFAULT);
		for (int i = 0; i < fieldInfos.size(); i++) {
			if (fieldInfos.get(i).hasRaw && fieldInfos.get(i).raw.crosstaglist != null) {
				hasCrosstag.set(true);
			}
		}
		if (hasRaw) {
			final int taglistSize = raw.taglist == null || raw.taglist.list == null ? 0 : raw.taglist.list.size();
			for (int i = 0; i < taglistSize; i++) {
				raw_options.get(raw.taglist.list.get(i).fieldnum).tag_type = i + 1;
			}
			final int extBitGroupsSize = raw.ext_bit_groups == null ? 0 : raw.ext_bit_groups.size();
			for (int i = 0; i < extBitGroupsSize; i++) {
				final rawAST_coding_ext_group tempExtGroup = raw.ext_bit_groups.get(i);
				for (int k = tempExtGroup.from; k <= tempExtGroup.to; k++) {
					raw_options.get(k).extbitgroup = i + 1;
				}
			}
		}
		for (int i = 0; i < fieldInfos.size(); i++) {
			final FieldInfo tempFieldInfo = fieldInfos.get(i);
			final int lengthSize = tempFieldInfo.raw == null || tempFieldInfo.raw.lengthto == null ? 0 : tempFieldInfo.raw.lengthto.size();
			if (tempFieldInfo.hasRaw && lengthSize > 0) {
				hasLengthto.set(true);
				raw_options.get(i).lengthto = true;
				for (int j = 0; j < lengthSize; j++) {
					final int fieldIndex = tempFieldInfo.raw.lengthto.get(j);
					final raw_option_struct tempOptions = raw_options.get(fieldIndex);
					if (tempOptions.lengthofField == null) {
						tempOptions.lengthofField = new ArrayList<Integer>();
					}
					tempOptions.lengthofField.add(i);
					tempOptions.lengthof++;
				}
			}
			if (tempFieldInfo.hasRaw && tempFieldInfo.raw.pointerto != -1) {
				raw_options.get(i).pointerto = true;
				raw_options.get(fieldInfos.get(i).raw.pointerto).pointerof = i + 1;
				hasPointer.set(true);
				raw_options.get(fieldInfos.get(i).raw.pointerbase).ptrbase = true;
			}
		}
		if (!isSet && hasCrosstag.get()) {
			for (int i = 0; i < fieldInfos.size(); i++) {
				final FieldInfo tempFieldInfo = fieldInfos.get(i);
				int maxIndex = i;
				if (!tempFieldInfo.hasRaw) {
					continue;
				}
				final int crosstagSize = tempFieldInfo.raw.crosstaglist == null || tempFieldInfo.raw.crosstaglist.list == null ? 0: tempFieldInfo.raw.crosstaglist.list.size();
				for (int j = 0; j < crosstagSize; j++) {
					final rawAST_coding_taglist crosstag = tempFieldInfo.raw.crosstaglist.list.get(j);
					final int fieldsSize = crosstag == null || crosstag.fields == null ? 0 : crosstag.fields.size(); 
					for (int k = 0; k < fieldsSize; k++) {
						final rawAST_coding_field_list keyid = crosstag.fields.get(k);
						if (keyid.fields.size() >= 1) {
							final int fieldIndex = keyid.fields.get(0).nthfield;
							if (fieldIndex > maxIndex) {
								maxIndex = fieldIndex;
							}
						}
					}
				}
				if (maxIndex > i) {
					raw_options.get(i).delayedDecode = true;
					if (raw_options.get(maxIndex).dependentFields == null) {
						raw_options.get(maxIndex).dependentFields = new ArrayList<Integer>();
					}
					raw_options.get(maxIndex).dependentFields.add(i);
				}
			}
		}
	}

	/**
	 * Generating isBound() function for template
	 * @param aSb the output, where the java code is written
	 * @param aNamesList sequence field variable and type names
	 */
	private static void generateTemplateIsBound( final StringBuilder aSb, final List<FieldInfo> aNamesList ) {
		aSb.append( "\n\t\tpublic boolean isBound() {\n" );
		aSb.append( "\t\t\tif (templateSelection == template_sel.UNINITIALIZED_TEMPLATE && !is_ifPresent) {\n"
				+ "\t\t\t\treturn false;\n"
				+ "\t\t\t}\n" );
		aSb.append( "\t\t\tif (templateSelection != template_sel.SPECIFIC_VALUE) {\n"
				+ "\t\t\t\treturn true;\n"
				+ "\t\t\t}\n" );
		for ( final FieldInfo fi : aNamesList ) {
			if (fi.isOptional) {
				aSb.append( MessageFormat.format( "\t\t\tif ({0}.isOmit() || {0}.isBound()) '{'\n"
						+ "\t\t\t\treturn true;\n"
						+ "\t\t\t}\n", fi.mVarName ) );
			} else {
				aSb.append( MessageFormat.format( "\t\t\tif ({0}.isBound()) '{'\n"
						+ "\t\t\t\treturn true;\n"
						+ "\t\t\t}\n", fi.mVarName ) );
			}
		}
		aSb.append( "\t\t\treturn false;\n" +
				"\t\t}\n" );
	}

	/**
	 * Generating isValue() function for template
	 * @param aSb the output, where the java code is written
	 * @param aNamesList sequence field variable and type names
	 */
	private static void generateTemplateIsValue( final StringBuilder aSb, final List<FieldInfo> aNamesList ) {
		aSb.append( "\n\t\tpublic boolean isValue() {\n" );
		aSb.append( "\t\t\tif (templateSelection != template_sel.SPECIFIC_VALUE || is_ifPresent) {\n"
				+ "\t\t\t\treturn false;\n"
				+ "\t\t\t}\n" );
		for ( final FieldInfo fi : aNamesList ) {
			if (fi.isOptional) {
				aSb.append( MessageFormat.format( "\t\t\tif (!{0}.isOmit() && !{0}.isValue()) '{'\n"
						+ "\t\t\t\treturn false;\n"
						+ "\t\t\t}\n", fi.mVarName ) );
			} else {
				aSb.append( MessageFormat.format( "\t\t\tif (!{0}.isValue()) '{'\n"
						+ "\t\t\t\treturn false;\n"
						+ "\t\t\t}\n", fi.mVarName ) );
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
		aSb.append( "\n\t\tpublic boolean operatorEquals( final " );
		aSb.append( aClassName );
		aSb.append( " aOtherValue ) {\n" );
		for ( final FieldInfo fi : aNamesList ) {
			aSb.append( "\t\t\tif ( !this." );
			aSb.append( fi.mVarName );
			aSb.append( ".operatorEquals( aOtherValue." );
			aSb.append( fi.mVarName );
			aSb.append( " ) ) { return false; }\n" );
		}
		aSb.append( "\t\t\treturn true;\n" +
				"\t\t}\n" );

		aSb.append('\n');
		aSb.append("\t\t@Override\n");
		aSb.append("\t\tpublic boolean operatorEquals(final Base_Type otherValue) {\n");
		aSb.append("\t\t\tif (otherValue instanceof ").append(aClassName).append(" ) {\n");
		aSb.append("\t\t\t\treturn operatorEquals((").append( aClassName ).append(") otherValue);\n");
		aSb.append("\t\t\t}\n\n");
		aSb.append("\t\t\tthrow new TtcnError(MessageFormat.format(\"Internal Error: value `{0}'' can not be cast to ").append(classReadableName).append("\", otherValue));\n");
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
		source.append("\t\t\tset_selection(template_sel.SPECIFIC_VALUE);\n");
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
	 * @param genName the name of the generated class representing the record/set type.
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
		source.append("\t\tswitch (other_value.get_selection()) {\n");
		source.append("\t\tcase OPTIONAL_PRESENT:\n");
		source.append("\t\t\tcopyValue(other_value.constGet());\n");
		source.append("\t\t\tbreak;\n");
		source.append("\t\tcase OPTIONAL_OMIT:\n");
		source.append("\t\t\tset_selection(template_sel.OMIT_VALUE);\n");
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
	 * @param genName the name of the generated class representing the record/set type.
	 * @param displayName the user readable name of the type to be generated.
	 */
	private static void generateTemplateAssign( final StringBuilder source, final String genName, final String displayName ) {
		source.append('\n');
		source.append("\t//originally operator=\n");
		source.append( MessageFormat.format( "\tpublic {0}_template assign( final template_sel other_value ) '{'\n", genName ) );
		source.append("\t\tcheckSingleSelection(other_value);\n");
		source.append("\t\tcleanUp();\n");
		source.append("\t\tset_selection(other_value);\n");
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
		source.append("\t\tswitch (other_value.get_selection()) {\n");
		source.append("\t\tcase OPTIONAL_PRESENT:\n");
		source.append("\t\t\tcopyValue(other_value.constGet());\n");
		source.append("\t\t\tbreak;\n");
		source.append("\t\tcase OPTIONAL_OMIT:\n");
		source.append("\t\t\tset_selection(template_sel.OMIT_VALUE);\n");
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
	 * @param genName the name of the generated class representing the record/set type.
	 * @param displayName the user readable name of the type to be generated.
	 */
	private static void generateTemplateCopyTemplate( final StringBuilder source, final List<FieldInfo> aNamesList, final String genName, final String displayName ) {
		source.append('\n');
		source.append( MessageFormat.format( "\tprivate void copyValue(final {0} other_value) '{'\n", genName));
		for ( final FieldInfo fi : aNamesList ) {
			source.append( MessageFormat.format( "\t\tif (other_value.get{0}().isBound()) '{'\n", fi.mJavaVarName ) );
			if ( fi.isOptional ) {
				source.append( MessageFormat.format( "\t\t\tif (other_value.get{0}().isPresent()) '{'\n", fi.mJavaVarName ) );
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
		source.append("\t\tset_selection(template_sel.SPECIFIC_VALUE);\n");
		source.append("\t}\n");

		source.append('\n');
		source.append( MessageFormat.format( "\tprivate void copyTemplate(final {0}_template other_value) '{'\n", genName));
		source.append("\t\tswitch (other_value.templateSelection) {\n");
		source.append("\t\tcase SPECIFIC_VALUE:\n");
		for ( final FieldInfo fi : aNamesList ) {
			source.append( MessageFormat.format( "\t\t\tif (template_sel.UNINITIALIZED_TEMPLATE == other_value.get{0}().get_selection()) '{'\n", fi.mJavaVarName ) );
			source.append( MessageFormat.format( "\t\t\t\tget{0}().cleanUp();\n", fi.mJavaVarName ) );
			source.append("\t\t\t} else {\n");
			source.append( MessageFormat.format( "\t\t\t\tget{0}().assign(other_value.get{0}());\n", fi.mJavaVarName ) );
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
		source.append("\t\tset_selection(other_value);\n");
		source.append("\t}\n");
	}

	/**
	 * Generating isPresent() function for template
	 * @param aSb the output, where the java code is written
	 */
	private static void generateTemplateIsPresent( final StringBuilder aSb ) {
		aSb.append('\n');
		aSb.append("\t\tpublic boolean isPresent() {\n");
		aSb.append("\t\t\treturn isPresent(false);\n");
		aSb.append("\t\t}\n");

		aSb.append('\n');
		aSb.append("\t\tpublic boolean isPresent(final boolean legacy) {\n");
		aSb.append("\t\t\treturn isPresent_(legacy);\n");
		aSb.append("\t\t}\n");

		aSb.append('\n');
		aSb.append("\t\tprivate boolean isPresent_(final boolean legacy) {\n");
		aSb.append("\t\t\tif (templateSelection==template_sel.UNINITIALIZED_TEMPLATE) {\n");
		aSb.append("\t\t\t\treturn false;\n");
		aSb.append("\t\t\t}\n");
		aSb.append("\t\t\treturn !match_omit_(legacy);\n");
		aSb.append("\t\t}\n");

		aSb.append('\n');
		aSb.append("\t\tpublic boolean match_omit() {\n");
		aSb.append("\t\t\treturn match_omit(false);\n");
		aSb.append("\t\t}\n");

		aSb.append('\n');
		aSb.append("\t\tpublic boolean match_omit(final boolean legacy) {\n");
		aSb.append("\t\t\treturn match_omit_(legacy);\n");
		aSb.append("\t\t}\n");

		aSb.append('\n');
		aSb.append("\t\tprivate boolean match_omit_(final boolean legacy) {\n");
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
	 * @param genName the name of the generated class representing the record/set type.
	 * @param displayName the user readable name of the type to be generated.
	 */
	private static void generateTemplateValueOf( final StringBuilder aSb, final List<FieldInfo> aNamesList, final String genName, final String displayName ) {
		aSb.append('\n');
		aSb.append( MessageFormat.format( "\t\tpublic {0} valueOf() '{'\n", genName ) );
		aSb.append("\t\t\tif (templateSelection != template_sel.SPECIFIC_VALUE || is_ifPresent) {\n");
		aSb.append( MessageFormat.format( "\t\t\t\tthrow new TtcnError(\"Performing a valueof or send operation on a non-specific template of type {0}.\");\n", displayName ) );
		aSb.append("\t\t\t}\n");
		aSb.append( MessageFormat.format( "\t\t\tfinal {0} ret_val = new {0}();\n", genName ) );
		for ( final FieldInfo fi : aNamesList ) {
			if (fi.isOptional) {
				aSb.append( MessageFormat.format( "\t\t\tif ({0}.isOmit()) '{'\n", fi.mVarName )  );
				aSb.append( MessageFormat.format( "\t\t\t\tret_val.{0}.assign(template_sel.OMIT_VALUE);\n", fi.mVarName ) );
				aSb.append("\t\t\t} else ");
			} else {
				aSb.append("\t\t\t ");
			}
			aSb.append( MessageFormat.format( "if ({0}.isBound()) '{'\n", fi.mVarName )  );
			aSb.append( MessageFormat.format( "\t\t\t\tret_val.{0}.assign({0}.valueOf());\n", fi.mVarName ) );
			aSb.append("\t\t\t}\n");
		}
		aSb.append("\t\t\treturn ret_val;\n");
		aSb.append("\t\t}\n");
	}

	/**
	 * Generating listItem() function for template
	 * @param aSb the output, where the java code is written
	 * @param genName the name of the generated class representing the record/set type.
	 * @param displayName the user readable name of the type to be generated.
	 */
	private static void generateTemplateListItem( final StringBuilder aSb, final String genName, final String displayName ) {
		aSb.append('\n');
		aSb.append( MessageFormat.format( "\t\tpublic {0}_template listItem(final int list_index) '{'\n", genName ) );
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
	 * @param genName the name of the generated class representing the record/set type.
	 * @param displayName the user readable name of the type to be generated.
	 */
	private static void generateTemplateSetType( final StringBuilder aSb, final String genName, final String displayName ) {
		aSb.append('\n');
		aSb.append("\t\tpublic void setType(final template_sel template_type, final int list_length) {\n");
		aSb.append("\t\t\tif (template_type != template_sel.VALUE_LIST && template_type != template_sel.COMPLEMENTED_LIST) {\n");
		aSb.append( MessageFormat.format( "\t\t\t\tthrow new TtcnError(\"Setting an invalid list for a template of type {0}.\");\n", displayName ) );
		aSb.append("\t\t\t}\n");
		aSb.append("\t\t\tcleanUp();\n");
		aSb.append("\t\t\tset_selection(template_type);\n");
		aSb.append( MessageFormat.format( "\t\t\tlist_value = new ArrayList<{0}_template>(list_length);\n", genName ) );
		aSb.append("\t\t\tfor(int i = 0 ; i < list_length; i++) {\n");
        aSb.append(MessageFormat.format("\t\t\t\tlist_value.add(new {0}_template());\n", genName));
        aSb.append("\t\t\t}\n");
		aSb.append("\t\t}\n");
	}

	/**
	 * Generate the match function for template
	 *
	 * @param source where the source code is to be generated.
	 * @param aNamesList sequence field variable and type names
	 * @param genName the name of the generated class representing the record/set type.
	 * @param displayName the user readable name of the type to be generated.
	 */
	private static void generateTemplateMatch( final StringBuilder source, final List<FieldInfo> aNamesList, final String genName, final String displayName ) {
		source.append('\n');
		source.append( MessageFormat.format( "\t\tpublic boolean match(final {0} other_value) '{'\n", genName ) );
		source.append("\t\t\treturn match(other_value, false);\n");
		source.append("\t\t}\n");

		source.append('\n');
		source.append( MessageFormat.format( "\t\tpublic boolean match(final {0} other_value, final boolean legacy) '{'\n", genName ) );
		source.append("\t\t\tif (!other_value.isBound()) {\n");
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
			source.append( MessageFormat.format( "\t\t\t\tif(!other_value.get{0}().isBound()) '{'\n", fi.mJavaVarName )  );
			source.append("\t\t\t\t\treturn false;\n");
			source.append("\t\t\t\t}\n");
			if (fi.isOptional) {
				source.append( MessageFormat.format( "\t\t\t\tif((other_value.get{0}().isPresent() ? !{1}.match(other_value.get{0}().get(), legacy) : !{1}.match_omit(legacy))) '{'\n", fi.mJavaVarName, fi.mVarName ) );
			} else {
				source.append( MessageFormat.format( "\t\t\t\tif(!{1}.match(other_value.get{0}(), legacy)) '{'\n", fi.mJavaVarName, fi.mVarName )  );
			}
			source.append("\t\t\t\t\treturn false;\n");
			source.append("\t\t\t\t}\n");
		}
		source.append("\t\t\t\treturn true;\n");
		source.append("\t\t\tcase VALUE_LIST:\n");
		source.append("\t\t\tcase COMPLEMENTED_LIST:\n");
		source.append("\t\t\t\tfor (int list_count = 0; list_count < list_value.size(); list_count++) {\n");
		source.append("\t\t\t\t\tif (list_value.get(list_count).match(other_value, legacy)) {\n");
		source.append("\t\t\t\t\t\treturn templateSelection == template_sel.VALUE_LIST;\n");
		source.append("\t\t\t\t\t}\n");
		source.append("\t\t\t\t}\n");
		source.append("\t\t\t\treturn templateSelection == template_sel.COMPLEMENTED_LIST;\n");
		source.append("\t\t\tdefault:\n");
		source.append( MessageFormat.format( "\t\t\t\tthrow new TtcnError(\"Matching an uninitialized/unsupported template of type {0}.\");\n", displayName ) );
		source.append("\t\t\t}\n");
		source.append("\t\t}\n");

		source.append('\n');
		source.append("\t@Override\n");
		source.append( MessageFormat.format( "\tpublic boolean match(final Base_Type otherValue, final boolean legacy) '{'\n", genName ) );
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
				aSb.append( MessageFormat.format( "\t\t\t\tif ({0}.isPresent()) '{'\n", fi.mVarName ) );
				aSb.append( "\t\t\t\t\tsizeof++;\n" );
				aSb.append( "\t\t\t\t}\n" );
			} else {
				size++;
			}
		}
		aSb.append( MessageFormat.format( "\t\t\t\tsizeof += {0};\n", size ) );
		aSb.append( "\t\t\t\treturn new TitanInteger(sizeof);\n" );
		aSb.append( "\t\t\tcase VALUE_LIST:\n" );
		aSb.append( "\t\t\t\tif (list_value.isEmpty()) {\n" );
		aSb.append( MessageFormat.format( "\t\t\t\t\tthrow new TtcnError(\"Internal error: Performing sizeof() operation on a template of type {0} containing an empty list.\");\n", displayName ) );
		aSb.append( "\t\t\t\t}\n" );
		aSb.append( "\t\t\t\tfinal int item_size = list_value.get(0).sizeOf().getInt();\n" );
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
	 * @param genName the name of the generated class representing the record/set type.
	 * @param displayName the user readable name of the type to be generated.
	 */
	private static void generateTemplateLog(final StringBuilder source, final List<FieldInfo> aNamesList, final String genName, final String displayName) {
		source.append('\n');
		source.append("\t\tpublic void log() {\n");
		source.append("\t\t\tswitch (templateSelection) {\n");
		source.append("\t\t\tcase SPECIFIC_VALUE:\n");
		source.append("\t\t\t\tTtcnLogger.log_char('{');\n");
		for (int i = 0 ; i < aNamesList.size(); i++) {
			final FieldInfo fieldInfo = aNamesList.get(i);

			if (i > 0) {
				source.append("\t\t\t\tTtcnLogger.log_char(',');\n");
			}
			source.append(MessageFormat.format("\t\t\t\tTtcnLogger.log_event_str(\" {0} := \");\n", fieldInfo.mDisplayName));
			source.append(MessageFormat.format("\t\t\t\t{0}.log();\n", fieldInfo.mVarName));
		}
		source.append("\t\t\t\tTtcnLogger.log_event_str(\" }\");\n");
		source.append("\t\t\t\tbreak;\n");
		source.append("\t\t\tcase COMPLEMENTED_LIST:\n");
		source.append("\t\t\t\tTtcnLogger.log_event_str(\"complement\");\n");
		source.append("\t\t\tcase VALUE_LIST:\n");
		source.append("\t\t\t\tTtcnLogger.log_char('(');\n");
		source.append("\t\t\t\tfor (int list_count = 0; list_count < list_value.size(); list_count++) {\n");
		source.append("\t\t\t\t\tif (list_count > 0) {\n");
		source.append("\t\t\t\t\t\tTtcnLogger.log_event_str(\", \");\n");
		source.append("\t\t\t\t\t}\n");
		source.append("\t\t\t\t\tlist_value.get(list_count).log();\n");
		source.append("\t\t\t\t}\n");
		source.append("\t\t\t\tTtcnLogger.log_char(')');\n");
		source.append("\t\t\t\tbreak;\n");
		source.append("\t\t\tdefault:\n");
		source.append("\t\t\t\tlog_generic();\n");
		source.append("\t\t\t\tbreak;\n");
		source.append("\t\t\t}\n");
		source.append("\t\t\tlog_ifpresent();\n");
		source.append("\t\t}\n");

		source.append('\n');
		source.append(MessageFormat.format("\t\tpublic void log_match(final {0} match_value) '{'\n", genName ) );
		source.append("\t\t\tlog_match(match_value, false);\n");
		source.append("\t\t}\n");

		source.append('\n');
		source.append("\t\t@Override\n");
		source.append("\t\tpublic void log_match(final Base_Type match_value, final boolean legacy) {\n");
		source.append(MessageFormat.format("\t\t\tif (match_value instanceof {0}) '{'\n", genName));
		source.append(MessageFormat.format("\t\t\t\tlog_match(({0})match_value, legacy);\n", genName));
		source.append("\t\t\t\treturn;\n");
		source.append("\t\t\t}\n\n");
		source.append(MessageFormat.format("\t\t\tthrow new TtcnError(\"Internal Error: value can not be cast to {0}.\");\n", displayName));
		source.append("\t\t}\n");

		source.append('\n');
		source.append(MessageFormat.format("\t\tpublic void log_match(final {0} match_value, final boolean legacy) '{'\n", genName ) );
		source.append("\t\t\tif ( TtcnLogger.matching_verbosity_t.VERBOSITY_COMPACT == TtcnLogger.get_matching_verbosity() ) {\n");
		source.append("\t\t\t\tif(match(match_value, legacy)) {\n");
		source.append("\t\t\t\t\tTtcnLogger.print_logmatch_buffer();\n");
		source.append("\t\t\t\t\tTtcnLogger.log_event_str(\" matched\");\n");
		source.append("\t\t\t\t} else {\n");
		source.append("\t\t\t\t\tif (templateSelection == template_sel.SPECIFIC_VALUE) {\n");
		source.append("\t\t\t\t\t\tfinal int previous_size = TtcnLogger.get_logmatch_buffer_len();\n");
		for (int i = 0 ; i < aNamesList.size(); i++) {
			final FieldInfo fi = aNamesList.get(i);

			if (fi.isOptional) {
				source.append(MessageFormat.format("\t\t\t\t\t\tif (match_value.constGet{0}().isPresent()) '{'\n", fi.mJavaVarName ) );
				source.append(MessageFormat.format("\t\t\t\t\t\t\tif( !{0}.match(match_value.constGet{1}().get(), legacy) ) '{'\n", fi.mVarName, fi.mJavaVarName ) );
				source.append(MessageFormat.format("\t\t\t\t\t\t\t\tTtcnLogger.log_logmatch_info(\".{0}\");\n", fi.mDisplayName ) );
				source.append(MessageFormat.format("\t\t\t\t\t\t\t\t{0}.log_match(match_value.constGet{1}().get(), legacy);\n", fi.mVarName, fi.mJavaVarName ) );
				source.append("\t\t\t\t\t\t\t\tTtcnLogger.set_logmatch_buffer_len(previous_size);\n");
				source.append("\t\t\t\t\t\t\t}\n");
				source.append("\t\t\t\t\t\t} else {\n");
				source.append(MessageFormat.format("\t\t\t\t\t\t\tif (!{0}.match_omit(legacy)) '{'\n", fi.mVarName) );
				source.append(MessageFormat.format("\t\t\t\t\t\t\t\tTtcnLogger.log_logmatch_info(\".{0} := omit with \");\n", fi.mDisplayName ) );
				source.append("\t\t\t\t\t\t\tTtcnLogger.print_logmatch_buffer();\n");
				source.append(MessageFormat.format("\t\t\t\t\t\t\t\t{0}.log();\n", fi.mVarName) );
				source.append("\t\t\t\t\t\t\t\tTtcnLogger.log_event_str(\" unmatched\");\n");
				source.append("\t\t\t\t\t\t\t\tTtcnLogger.set_logmatch_buffer_len(previous_size);\n");
				source.append("\t\t\t\t\t\t\t}\n");
				source.append("\t\t\t\t\t\t}\n");
			} else {
				source.append(MessageFormat.format("\t\t\t\t\t\tif( !{0}.match(match_value.constGet{1}(), legacy) ) '{'\n", fi.mVarName, fi.mJavaVarName ) );
				source.append(MessageFormat.format("\t\t\t\t\t\t\tTtcnLogger.log_logmatch_info(\".{0}\");\n", fi.mDisplayName ) );
				source.append(MessageFormat.format("\t\t\t\t\t\t\t{0}.log_match(match_value.constGet{1}(), legacy);\n", fi.mVarName, fi.mJavaVarName ) );
				source.append("\t\t\t\t\t\t\tTtcnLogger.set_logmatch_buffer_len(previous_size);\n");
				source.append("\t\t\t\t\t\t}\n");
			}
		}
		source.append("\t\t\t\t\t} else {\n");
		source.append("\t\t\t\t\t\tTtcnLogger.print_logmatch_buffer();\n");
		source.append("\t\t\t\t\t\tmatch_value.log();\n");
		source.append("\t\t\t\t\t\tTtcnLogger.log_event_str(\" with \");\n");
		source.append("\t\t\t\t\t\tlog();\n");
		source.append("\t\t\t\t\t\tTtcnLogger.log_event_str(\" unmatched\");\n");
		source.append("\t\t\t\t\t}\n");
		source.append("\t\t\t\t}\n");
		source.append("\t\t\t\treturn;\n");
		source.append("\t\t\t}\n");
		source.append("\t\t\tif (templateSelection == template_sel.SPECIFIC_VALUE) {\n");
		for (int i = 0 ; i < aNamesList.size(); i++) {
			final FieldInfo fi = aNamesList.get(i);

			source.append(MessageFormat.format("\t\t\t\tTtcnLogger.log_event_str(\"'{' {0} := \");\n", fi.mDisplayName ) );
			source.append(MessageFormat.format("\t\t\t\t{0}.log_match(match_value.constGet{1}(), legacy);\n", fi.mVarName, fi.mJavaVarName ) );
		}
		source.append("\t\t\t\tTtcnLogger.log_event_str(\" }\");\n");
		source.append("\t\t\t} else {\n");
		source.append("\t\t\t\tmatch_value.log();\n");
		source.append("\t\t\t\tTtcnLogger.log_event_str(\" with \");\n");
		source.append("\t\t\t\tlog();\n");
		source.append("\t\t\t\tif ( match(match_value, legacy) ) {\n");
		source.append("\t\t\t\t\tTtcnLogger.log_event_str(\" matched\");\n");
		source.append("\t\t\t\t} else {\n");
		source.append("\t\t\t\t\tTtcnLogger.log_event_str(\" unmatched\");\n");
		source.append("\t\t\t\t}\n");
		source.append("\t\t\t}\n");
		source.append("\t\t}\n");
	}

	/**
	 * Generate encode_text/decode_text
	 *
	 * @param source where the source code is to be generated.
	 * @param aNamesList sequence field variable and type names
	 * @param genName the name of the generated class representing the record/set type.
	 * @param displayName the user readable name of the type to be generated.
	 */
	private static void generateTemplateEncodeDecodeText(final StringBuilder source, final List<FieldInfo> aNamesList, final String genName, final String displayName) {
		source.append("\t\t@Override\n");
		source.append("\t\tpublic void encode_text(final Text_Buf text_buf) {\n");
		source.append("\t\t\tencode_text_base(text_buf);\n");
		source.append("\t\t\tswitch (templateSelection) {\n");
		source.append("\t\t\tcase OMIT_VALUE:\n");
		source.append("\t\t\tcase ANY_VALUE:\n");
		source.append("\t\t\tcase ANY_OR_OMIT:\n");
		source.append("\t\t\t\tbreak;\n");
		source.append("\t\t\tcase SPECIFIC_VALUE:\n");
		for (int i = 0 ; i < aNamesList.size(); i++) {
			final FieldInfo fi = aNamesList.get(i);

			source.append(MessageFormat.format("\t\t\t\t{0}.encode_text(text_buf);\n", fi.mVarName ) );
		}
		source.append("\t\t\t\tbreak;\n");
		source.append("\t\t\tcase VALUE_LIST:\n");
		source.append("\t\t\tcase COMPLEMENTED_LIST:\n");
		source.append("\t\t\t\ttext_buf.push_int(list_value.size());\n");
		source.append("\t\t\t\tfor (int i = 0; i < list_value.size(); i++) {\n");
		source.append("\t\t\t\t\tlist_value.get(i).encode_text(text_buf);\n");
		source.append("\t\t\t\t}\n");
		source.append("\t\t\t\tbreak;\n");
		source.append("\t\t\tdefault:\n");
		source.append(MessageFormat.format("\t\t\t\tthrow new TtcnError(\"Text encoder: Encoding an uninitialized/unsupported template of type {0}.\");\n", displayName));
		source.append("\t\t\t}\n");
		source.append("\t\t}\n");

		source.append("\t\t@Override\n");
		source.append("\t\tpublic void decode_text(final Text_Buf text_buf) {\n");
		source.append("\t\t\tcleanUp();\n");
		source.append("\t\t\tdecode_text_base(text_buf);\n");
		source.append("\t\t\tswitch (templateSelection) {\n");
		source.append("\t\t\tcase OMIT_VALUE:\n");
		source.append("\t\t\tcase ANY_VALUE:\n");
		source.append("\t\t\tcase ANY_OR_OMIT:\n");
		source.append("\t\t\t\tbreak;\n");
		source.append("\t\t\tcase SPECIFIC_VALUE:\n");
		for (int i = 0 ; i < aNamesList.size(); i++) {
			final FieldInfo fi = aNamesList.get(i);

			source.append(MessageFormat.format("\t\t\t\t{0} = new {1}();\n", fi.mVarName, fi.mJavaTemplateTypeName ) );
			source.append(MessageFormat.format("\t\t\t\t{0}.decode_text(text_buf);\n", fi.mVarName ) );
		}
		source.append("\t\t\t\tbreak;\n");
		source.append("\t\t\tcase VALUE_LIST:\n");
		source.append("\t\t\tcase COMPLEMENTED_LIST:\n");
		source.append(MessageFormat.format("\t\t\t\tlist_value = new ArrayList<{0}_template>(text_buf.pull_int().getInt());\n", genName));
		source.append("\t\t\t\tfor(int i = 0; i < list_value.size(); i++) {\n");
		source.append(MessageFormat.format("\t\t\t\t\tfinal {0}_template temp = new {0}_template();\n", genName));
		source.append("\t\t\t\t\ttemp.decode_text(text_buf);\n");
		source.append("\t\t\t\t\tlist_value.add(temp);\n");
		source.append("\t\t\t\t}\n");
		source.append("\t\t\t\tbreak;\n");
		source.append("\t\t\tdefault:\n");
		source.append(MessageFormat.format("\t\t\t\tthrow new TtcnError(\"Text decoder: An unknown/unsupported selection was received in a template of type {0}.\");\n", displayName));
		source.append("\t\t\t}\n");
		source.append("\t\t}\n");
	}

	/**
	 * This function can be used to generate the value class of record and set types
	 *
	 * defEmptyRecordClass in compilers/record.c
	 *
	 * @param aData only used to update imports if needed.
	 * @param source where the source code is to be generated.
	 * @param className the name of the generated class representing the record/set type.
	 * @param classDisplayName the user readable name of the type to be generated.
	 * @param rawNeeded true if encoding/decoding for RAW is to be generated.
	 */
	public static void generateEmptyValueClass(final JavaGenData aData, final StringBuilder source, final String className, final String classDisplayname, final boolean rawNeeded) {
		aData.addBuiltinTypeImport("TitanNull_Type");

		source.append(MessageFormat.format("public static class {0} extends Base_Type '{'\n", className));
		source.append("private boolean bound_flag;\n\n");

		source.append(MessageFormat.format("public {0}() '{'\n", className));
		source.append("bound_flag = false;\n");
		source.append("}\n\n");

		source.append(MessageFormat.format("public {0}( final TitanNull_Type otherValue ) '{'\n", className));
		source.append("bound_flag = true;\n");
		source.append("}\n\n");

		source.append(MessageFormat.format("public {0}( final {0} otherValue ) '{'\n", className));
		source.append("if ( !otherValue.isBound() ) {\n");
		source.append(MessageFormat.format("\t\tthrow new TtcnError(\"Copying of an unbound value of type {0}.\");\n", classDisplayname));
		source.append("}\n");
		source.append("bound_flag = true;\n");
		source.append("}\n\n");

		source.append("//originally operator=\n");
		source.append(MessageFormat.format("public {0} assign( final TitanNull_Type otherValue ) '{'\n", className));
		source.append("bound_flag = true;\n");
		source.append("return this;\n");
		source.append("}\n\n");

		source.append("//originally operator=\n");
		source.append(MessageFormat.format("public {0} assign( final {0} otherValue ) '{'\n", className));
		source.append("if ( !otherValue.isBound() ) {\n");
		source.append(MessageFormat.format("\t\tthrow new TtcnError(\"Assignment of an unbound value of type {0}.\");\n", classDisplayname));
		source.append("}\n");
		source.append("bound_flag = true;\n");
		source.append("return this;\n");
		source.append("}\n\n");

		source.append("@Override\n");
		source.append(MessageFormat.format("public {0} assign( final Base_Type otherValue ) '{'\n", className));
		source.append(MessageFormat.format("if (otherValue instanceof {0}) '{'\n", className));
		source.append(MessageFormat.format("return assign(({0})otherValue);\n", className));
		source.append("}\n");
		source.append(MessageFormat.format("throw new TtcnError(\"Internal Error: value can not be cast to {0}.\");\n", className));
		source.append("}\n\n");

		source.append("//originally clean_up\n");
		source.append("public void cleanUp() {\n");
		source.append("bound_flag = false;\n");
		source.append("}\n\n");

		source.append("//originally is_bound\n");
		source.append("public boolean isBound() {\n");
		source.append("return bound_flag;\n");
		source.append("}\n\n");

		source.append("//originally is_present\n");
		source.append("public boolean isPresent() {\n");
		source.append("return isBound();\n");
		source.append("}\n\n");

		source.append("//originally is_value\n");
		source.append("public boolean isValue() {\n");
		source.append("return bound_flag;\n");
		source.append("}\n\n");

		source.append("public void mustBound( final String aErrorMessage ) {\n");
		source.append("if ( !bound_flag ) {\n");
		source.append("throw new TtcnError( aErrorMessage );\n");
		source.append("}\n");
		source.append("}\n\n");

		source.append("//originally operator==\n");
		source.append("public boolean operatorEquals( final TitanNull_Type otherValue ) {\n");
		source.append("if (!isBound()) {\n");
		source.append(MessageFormat.format("throw new TtcnError(\"Comparison of an unbound value of type {0}.\");\n", classDisplayname));
		source.append("}\n");
		source.append("return true;\n");
		source.append("}\n\n");

		source.append("//originally operator==\n");
		source.append(MessageFormat.format("public boolean operatorEquals( final {0} otherValue ) '{'\n", className));
		source.append("if (!isBound()) {\n");
		source.append(MessageFormat.format("throw new TtcnError(\"Comparison of an unbound value of type {0}.\");\n", classDisplayname));
		source.append("}\n");
		source.append("if (!otherValue.isBound()) {\n");
		source.append(MessageFormat.format("throw new TtcnError(\"Comparison of an unbound value of type {0}.\");\n", classDisplayname));
		source.append("}\n");
		source.append("return true;\n");
		source.append("}\n\n");

		source.append("@Override\n");
		source.append("public boolean operatorEquals( final Base_Type otherValue ) {\n");
		source.append(MessageFormat.format("if (otherValue instanceof {0}) '{'\n", className));
		source.append(MessageFormat.format("return operatorEquals(({0})otherValue);\n", className));
		source.append("}\n");
		source.append(MessageFormat.format("throw new TtcnError(\"Internal Error: value can not be cast to {0}.\");\n", classDisplayname));
		source.append("}\n\n");

		source.append("//originally operator!=\n");
		source.append("public boolean operatorNotEquals( final TitanNull_Type otherValue ) {\n");
		source.append("return !operatorEquals(otherValue);\n");
		source.append("}\n\n");

		source.append("//originally operator!=\n");
		source.append(MessageFormat.format("public boolean operatorNotEquals( final {0} otherValue ) '{'\n", className));
		source.append("return !operatorEquals(otherValue);\n");
		source.append("}\n\n");

		source.append("public boolean operatorNotEquals( final Base_Type otherValue ) {\n");
		source.append("return !operatorEquals(otherValue);\n");
		source.append("}\n\n");

		source.append("public void log() {\n");
		source.append("if (bound_flag) {\n");
		source.append("TtcnLogger.log_event_str(\"{ }\");\n");
		source.append("return;\n");
		source.append("}\n");
		source.append("TtcnLogger.log_event_unbound();\n");
		source.append("}\n");

		source.append("@Override\n");
		source.append("public void encode_text(final Text_Buf text_buf) {\n");
		source.append(MessageFormat.format("mustBound(\"Text encoder: Encoding an unbound value of type {0}.\");\n", classDisplayname));
		source.append("}\n");

		source.append("@Override\n");
		source.append("public void decode_text(final Text_Buf text_buf) {\n");
		source.append("bound_flag = true;\n");
		source.append("}\n");

		source.append("@Override\n");
		source.append("public void encode(final TTCN_Typedescriptor p_td, final TTCN_Buffer p_buf, final coding_type p_coding, final int flavour) {\n");
		source.append("switch (p_coding) {\n");
		source.append("case CT_RAW: {\n");
		source.append("final TTCN_EncDec_ErrorContext errorContext = new TTCN_EncDec_ErrorContext(\"While RAW-encoding type '%s': \", p_td.name);\n");
		source.append("if (p_td.raw == null) {\n");
		source.append("TTCN_EncDec_ErrorContext.error_internal(\"No RAW descriptor available for type '%s'.\", p_td.name);\n");
		source.append("}\n");
		source.append("final RAW_enc_tr_pos rp = new RAW_enc_tr_pos(0, null);\n");
		source.append("final RAW_enc_tree root = new RAW_enc_tree(false, null, rp, 1, p_td.raw);\n");
		source.append("RAW_encode(p_td, root);\n");
		source.append("root.put_to_buf(p_buf);\n");
		source.append("errorContext.leaveContext();\n");
		source.append("break;\n");
		source.append("}\n");
		source.append("default:\n");
		source.append("throw new TtcnError(MessageFormat.format(\"Unknown coding method requested to encode type `{0}''\", p_td.name));\n");
		source.append("}\n");
		source.append("}\n\n");

		source.append("@Override\n");
		source.append("public void decode(final TTCN_Typedescriptor p_td, final TTCN_Buffer p_buf, final coding_type p_coding, final int flavour) {\n");
		source.append("switch (p_coding) {\n");
		source.append("case CT_RAW: {\n");
		source.append("final TTCN_EncDec_ErrorContext errorContext = new TTCN_EncDec_ErrorContext(\"While RAW-decoding type '%s': \", p_td.name);\n");
		source.append("if (p_td.raw == null) {\n");
		source.append("TTCN_EncDec_ErrorContext.error_internal(\"No RAW descriptor available for type '%s'.\", p_td.name);\n");
		source.append("}\n");
		source.append("raw_order_t order;\n");
		source.append("switch (p_td.raw.top_bit_order) {\n");
		source.append("case TOP_BIT_LEFT:\n");
		source.append("order = raw_order_t.ORDER_LSB;\n");
		source.append("break;\n");
		source.append("case TOP_BIT_RIGHT:\n");
		source.append("default:\n");
		source.append("order = raw_order_t.ORDER_MSB;\n");
		source.append("break;\n");
		source.append("}\n");
		source.append("int rawr = RAW_decode(p_td, p_buf, p_buf.get_len() * 8, order);\n");
		source.append("if (rawr < 0) {\n");
		source.append("error_type temp = error_type.values()[-rawr];\n");
		source.append("switch(temp) {\n");
		source.append("case ET_INCOMPL_MSG:\n");
		source.append("case ET_LEN_ERR:\n");
		source.append("TTCN_EncDec_ErrorContext.error(temp, \"Can not decode type '%s', because invalid or incomplete message was received\", p_td.name);\n");
		source.append("break;\n");
		source.append("case ET_UNBOUND:\n");
		source.append("default:\n");
		source.append("TTCN_EncDec_ErrorContext.error(error_type.ET_INVAL_MSG, \"Can not decode type '%s', because invalid or incomplete message was received\", p_td.name);\n");
		source.append("break;\n");
		source.append("}\n");
		source.append("}\n");
		source.append("errorContext.leaveContext();\n");
		source.append("break;\n");
		source.append("}\n");
		source.append("default:\n");
		source.append("throw new TtcnError(MessageFormat.format(\"Unknown coding method requested to decode type `{0}''\", p_td.name));\n");
		source.append("}\n");
		source.append("}\n\n");

		if (rawNeeded) {
			source.append("@Override\n");
			source.append("public int RAW_encode(final TTCN_Typedescriptor p_td, final RAW_enc_tree myleaf) {\n");
			source.append("if (!isBound()) {\n");
			source.append("TTCN_EncDec_ErrorContext.error(error_type.ET_UNBOUND, \"Encoding an unbound value.\", \"\");\n");
			source.append("}\n");
			source.append("return 0;\n");
			source.append("}\n");

			source.append("@Override\n");
			source.append("public int RAW_decode(final TTCN_Typedescriptor p_td, final TTCN_Buffer buff, int limit, final raw_order_t top_bit_ord, final boolean no_err, final int sel_field, final boolean first_call) {\n");
			source.append("bound_flag = true;");
			source.append("return buff.increase_pos_padd(p_td.raw.prepadding) + buff.increase_pos_padd(p_td.raw.padding);\n");
			source.append("}\n");
		}

		source.append("}\n\n");
	}

	/**
	 * This function can be used to generate the template class of record and set types
	 *
	 * defEmptyRecordTemplate in compilers/record.c
	 *
	 * @param aData only used to update imports if needed.
	 * @param source where the source code is to be generated.
	 * @param className the name of the generated class representing the record/set type.
	 * @param classDisplayName the user readable name of the type to be generated.
	 * @param fieldInfos the list of information about the fields.
	 * @param hasOptional true if the type has an optional field.
	 * @param isSet true if generating code for a set, false if generating code for a record.
	 */
	public static void generateEmptyTemplateClass(final JavaGenData aData, final StringBuilder source, final String className,
			final String classDisplayName, final List<FieldInfo> fieldInfos, final boolean hasOptional, final boolean isSet) {
		aData.addBuiltinTypeImport("TitanNull_Type");

		source.append(MessageFormat.format("public static class {0}_template extends Base_Template '{'\n", className));

		source.append("//originally value_list/list_value\n");
		source.append(MessageFormat.format("List<{0}_template> list_value;\n", className));

		source.append(MessageFormat.format("public {0}_template() '{'\n", className));
		source.append("}\n\n");

		source.append(MessageFormat.format("public {0}_template(final template_sel other_value) '{'\n", className));
		source.append("super( other_value );\n");
		source.append("checkSingleSelection( other_value );\n");
		source.append("}\n\n");

		source.append(MessageFormat.format("public {0}_template(final TitanNull_Type other_value) '{'\n", className));
		source.append("super(template_sel.SPECIFIC_VALUE);\n");
		source.append("}\n\n");

		source.append(MessageFormat.format("public {0}_template(final {0} other_value) '{'\n", className));
		source.append("super(template_sel.SPECIFIC_VALUE);\n");
		source.append("if (!other_value.isBound()) {\n");
		source.append(MessageFormat.format("throw new TtcnError(\"Creating a template from an unbound value of type {0}.\");\n", classDisplayName));
		source.append("}\n");
		source.append("}\n\n");

		source.append(MessageFormat.format("public {0}_template(final {0}_template other_value) '{'\n", className));
		source.append("copyTemplate( other_value );\n");
		source.append("}\n\n");

		source.append(MessageFormat.format("public {0}_template(final Optional<{0}> other_value) '{'\n", className));
		source.append("switch (other_value.get_selection()) {\n");
		source.append("case OPTIONAL_PRESENT:\n");
		source.append("set_selection(template_sel.SPECIFIC_VALUE);\n");
		source.append("break;\n");
		source.append("case OPTIONAL_OMIT:\n");
		source.append("set_selection(template_sel.OMIT_VALUE);\n");
		source.append("break;\n");
		source.append("default:\n");
		source.append(MessageFormat.format("throw new TtcnError(\"Creating a template of type {0} from an unbound optional field.\");\n", classDisplayName));
		source.append("}\n");
		source.append("}\n\n");

		source.append("//originally operator=\n");
		source.append(MessageFormat.format("public {0}_template assign(final template_sel other_value) '{'\n", className));
		source.append("checkSingleSelection(other_value);\n");
		source.append("cleanUp();\n");
		source.append("set_selection(other_value);\n");
		source.append("return this;\n");
		source.append("}\n\n");

		source.append("//originally operator=\n");
		source.append(MessageFormat.format("public {0}_template assign(final TitanNull_Type other_value) '{'\n", className));
		source.append("cleanUp();\n");
		source.append("set_selection(template_sel.SPECIFIC_VALUE);\n");
		source.append("return this;\n");
		source.append("}\n\n");

		source.append("//originally operator=\n");
		source.append(MessageFormat.format("public {0}_template assign(final {0} other_value) '{'\n", className));
		source.append("if (!other_value.isBound()) {\n");
		source.append(MessageFormat.format("throw new TtcnError(\"Assignment of an unbound value of type {0} to a template.\");\n", classDisplayName));
		source.append("}\n");
		source.append("cleanUp();\n");
		source.append("set_selection(template_sel.SPECIFIC_VALUE);\n");
		source.append("return this;\n");
		source.append("}\n\n");

		source.append("//originally operator=\n");
		source.append(MessageFormat.format("public {0}_template assign(final {0}_template other_value) '{'\n", className));
		source.append("if (other_value != this) {\n");
		source.append("cleanUp();\n");
		source.append("copyTemplate(other_value);\n");
		source.append("}\n");
		source.append("return this;\n");
		source.append("}\n\n");

		source.append("@Override\n");
		source.append(MessageFormat.format("public {0}_template assign(final Base_Type other_value) '{'\n", className));
		source.append(MessageFormat.format("if (other_value instanceof {0}) '{'\n", className));
		source.append(MessageFormat.format("return assign(({0}) other_value);\n", className));
		source.append("}\n");
		source.append( MessageFormat.format("\t\t\tthrow new TtcnError(MessageFormat.format(\"Internal Error: value `{0}'' can not be cast to {1}\", other_value));\n", className));
		source.append("}\n\n");

		source.append("@Override\n");
		source.append(MessageFormat.format("public {0}_template assign(final Base_Template other_value) '{'\n", className));
		source.append(MessageFormat.format("if (other_value instanceof {0}_template) '{'\n", className));
		source.append(MessageFormat.format("return assign(({0}_template) other_value);\n", className));
		source.append("}\n");
		source.append( MessageFormat.format("\t\t\tthrow new TtcnError(MessageFormat.format(\"Internal Error: value `{0}'' can not be cast to {1}_template\", other_value));\n", className));
		source.append("}\n\n");

		source.append(MessageFormat.format("public {0}_template assign(final Optional<{0}> other_value) '{'\n", className));
		source.append("cleanUp();\n");
		source.append("switch (other_value.get_selection()) {\n");
		source.append("case OPTIONAL_PRESENT:\n");
		source.append("set_selection(template_sel.SPECIFIC_VALUE);\n");
		source.append("break;\n");
		source.append("case OPTIONAL_OMIT:\n");
		source.append("set_selection(template_sel.OMIT_VALUE);\n");
		source.append("break;\n");
		source.append("default:\n");
		source.append(MessageFormat.format("throw new TtcnError(\"Assignment of an unbound optional field to a template of type {0} .\");\n", classDisplayName));
		source.append("}\n");
		source.append("return this;\n");
		source.append("}\n\n");

		source.append(MessageFormat.format("public void copyTemplate(final {0}_template other_value) '{'\n", className));
		source.append("switch (other_value.templateSelection) {\n");
		source.append("case SPECIFIC_VALUE:\n");
		source.append("case OMIT_VALUE:\n");
		source.append("case ANY_VALUE:\n");
		source.append("case ANY_OR_OMIT:\n");
		source.append("break;\n");
		source.append("case VALUE_LIST:\n");
		source.append("case COMPLEMENTED_LIST:\n");
		source.append( MessageFormat.format( "\t\t\tlist_value = new ArrayList<{0}_template>(other_value.list_value.size());\n", className));
		source.append("\t\t\tfor(int i = 0; i < other_value.list_value.size(); i++) {\n");
		source.append( MessageFormat.format( "\t\t\t\tfinal {0}_template temp = new {0}_template(other_value.list_value.get(i));\n", className));
		source.append("\t\t\t\tlist_value.add(temp);\n");
		source.append("\t\t\t}\n");
		source.append("break;\n");
		source.append("default:\n");
		source.append(MessageFormat.format("throw new TtcnError(\"Copying an uninitialized template of type {0}.\");\n", classDisplayName));
		source.append("}\n");
		source.append("set_selection(other_value);\n");
		source.append("}\n\n");

		source.append("public boolean isPresent() {\n");
		source.append("return isPresent(false);\n");
		source.append("}\n\n");

		source.append("public boolean isPresent(final boolean legacy) {\n");
		source.append("return isPresent_(legacy);\n");
		source.append("}\n\n");

		source.append("private boolean isPresent_(final boolean legacy) {\n");
		source.append("if (templateSelection==template_sel.UNINITIALIZED_TEMPLATE) {\n");
		source.append("return false;\n");
		source.append("}\n");
		source.append("return !match_omit_(legacy);\n");
		source.append("}\n\n");

		source.append("public boolean match_omit() {\n");
		source.append("return match_omit(false);\n");
		source.append("}\n\n");

		source.append("public boolean match_omit(final boolean legacy) {\n");
		source.append("return match_omit_(legacy);\n");
		source.append("}\n\n");

		source.append("private boolean match_omit_(final boolean legacy) {\n");
		source.append("if (is_ifPresent) {\n");
		source.append("return true;\n");
		source.append("}\n");
		source.append("switch (templateSelection) {\n");
		source.append("case OMIT_VALUE:\n");
		source.append("case ANY_OR_OMIT:\n");
		source.append("return true;\n");
		source.append("case VALUE_LIST:\n");
		source.append("case COMPLEMENTED_LIST:\n");
		source.append("if (legacy) {\n");
		source.append("for (int l_idx=0; l_idx<list_value.size(); l_idx++) {\n");
		source.append("if (list_value.get(l_idx).match_omit_(legacy)) {\n");
		source.append("return templateSelection==template_sel.VALUE_LIST;\n");
		source.append("}\n");
		source.append("}\n");
		source.append("return templateSelection==template_sel.COMPLEMENTED_LIST;\n");
		source.append("} // else fall through\n");
		source.append("default:\n");
		source.append("return false;\n");
		source.append("}\n");
		source.append("}\n\n");

		source.append(MessageFormat.format("public {0} valueOf() '{'\n", className));
		source.append("if (templateSelection != template_sel.SPECIFIC_VALUE || is_ifPresent) {\n");
		source.append(MessageFormat.format("throw new TtcnError(\"Performing a valueof or send operation on a non-specific template of type {0}.\");\n", classDisplayName));
		source.append("}\n");
		source.append(MessageFormat.format("final {0} ret_val = new {0}(TitanNull_Type.NULL_VALUE);\n", className));
		source.append("return ret_val;\n");
		source.append("}\n\n");

		source.append( MessageFormat.format( "\t\tpublic {0}_template listItem(final int list_index) '{'\n", className ) );
		source.append("\t\t\tif (templateSelection != template_sel.VALUE_LIST && templateSelection != template_sel.COMPLEMENTED_LIST) {\n");
		source.append( MessageFormat.format( "\t\t\t\tthrow new TtcnError(\"Accessing a list element of a non-list template of type {0}.\");\n", classDisplayName ) );
		source.append("\t\t\t}\n");
		source.append("\t\t\tif (list_index >= list_value.size()) {\n");
		source.append( MessageFormat.format( "\t\t\t\tthrow new TtcnError(\"Index overflow in a value list template of type {0}.\");\n", classDisplayName ) );
		source.append("\t\t\t}\n");
		source.append("\t\t\treturn list_value.get(list_index);\n");
		source.append("\t\t}\n\n");

		source.append("\t\tpublic void setType(final template_sel template_type, final int list_length) {\n");
		source.append("\t\t\tif (template_type != template_sel.VALUE_LIST && template_type != template_sel.COMPLEMENTED_LIST) {\n");
		source.append( MessageFormat.format( "\t\t\t\tthrow new TtcnError(\"Setting an invalid list for a template of type {0}.\");\n", classDisplayName ) );
		source.append("\t\t\t}\n");
		source.append("\t\t\tcleanUp();\n");
		source.append("\t\t\tset_selection(template_type);\n");
		source.append( MessageFormat.format( "\t\t\tlist_value = new ArrayList<{0}_template>(list_length);\n", className ) );
		source.append("\t\t\tfor(int i = 0 ; i < list_length; i++) {\n");
		source.append(MessageFormat.format("\t\t\t\tlist_value.add(new {0}_template());\n", className));
		source.append("\t\t\t}\n");
		source.append("\t\t}\n\n");

		source.append( MessageFormat.format( "public boolean match(final {0} other_value) '{'\n", className ) );
		source.append("return match(other_value, false);\n");
		source.append("}\n\n");

		source.append( MessageFormat.format( "public boolean match(final {0} other_value, final boolean legacy) '{'\n", className ) );
		source.append("if (!other_value.isBound()) {\n");
		source.append("return false;\n");
		source.append("}\n");
		source.append("return match(TitanNull_Type.NULL_VALUE, legacy);\n");
		source.append("}\n\n");

		source.append("private boolean match(final TitanNull_Type other_value, final boolean legacy) {\n");
		source.append("switch (templateSelection) {\n");
		source.append("case ANY_VALUE:\n");
		source.append("case ANY_OR_OMIT:\n");
		source.append("return true;\n");
		source.append("case OMIT_VALUE:\n");
		source.append("return false;\n");
		source.append("case SPECIFIC_VALUE:\n");
		source.append("return true;\n");
		source.append("case VALUE_LIST:\n");
		source.append("case COMPLEMENTED_LIST:\n");
		source.append("for (int list_count = 0; list_count < list_value.size(); list_count++) {\n");
		source.append("if (list_value.get(list_count).match(other_value, legacy)) {\n");
		source.append("return templateSelection == template_sel.VALUE_LIST;\n");
		source.append("}\n");
		source.append("}\n");
		source.append("return templateSelection == template_sel.COMPLEMENTED_LIST;\n");
		source.append("default:\n");
		source.append( MessageFormat.format( "throw new TtcnError(\"Matching an uninitialized/unsupported template of type {0}.\");\n", classDisplayName ) );
		source.append("}\n");
		source.append("}\n\n");

		source.append("@Override\n");
		source.append("public boolean match(final Base_Type other_value, final boolean legacy) {\n");
		source.append( MessageFormat.format( "if (other_value instanceof {0}) '{'\n", className ) );
		source.append( MessageFormat.format( "return match(({0})other_value, legacy);\n", className ) );
		source.append("}\n");
		source.append( MessageFormat.format( "throw new TtcnError(\"Internal Error: The left operand of assignment is not of type {0}.\");\n", classDisplayName ) );
		source.append("}\n\n");

		source.append("public void log() {\n");
		source.append("switch (templateSelection) {\n");
		source.append("case SPECIFIC_VALUE:\n");
		source.append("TtcnLogger.log_event_str(\"{ }\");\n");
		source.append("break;\n");
		source.append("case COMPLEMENTED_LIST:\n");
		source.append("TtcnLogger.log_event_str(\"complement\");\n");
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
		source.append("}\n\n");

		source.append( MessageFormat.format( "public void log_match(final {0} match_value) '{'\n", className ) );
		source.append("log_match(match_value, false);\n");
		source.append("}\n\n");

		source.append("@Override\n");
		source.append("public void log_match(final Base_Type match_value, final boolean legacy) {\n");
		source.append( MessageFormat.format( "if (match_value instanceof {0}) '{'\n", className ) );
		source.append( MessageFormat.format( "log_match(({0})match_value, legacy);\n", className ) );
		source.append("return;\n");
		source.append("}\n");
		source.append( MessageFormat.format( "throw new TtcnError(\"Internal Error: value can not be cast to {0}.\");\n", classDisplayName ) );
		source.append("}\n\n");

		source.append( MessageFormat.format( "public void log_match(final {0} match_value, final boolean legacy) '{'\n", className ) );
		source.append("match_value.log();\n");
		source.append("TtcnLogger.log_event_str(\" with \");\n");
		source.append("log();\n");
		source.append("if ( match(match_value, legacy) ) {\n");
		source.append("TtcnLogger.log_event_str(\" matched\");\n");
		source.append("} else {\n");
		source.append("TtcnLogger.log_event_str(\" unmatched\");\n");
		source.append("}\n");
		source.append("}\n\n");

		source.append("@Override\n");
		source.append("public void encode_text(final Text_Buf text_buf) {\n");
		source.append("encode_text_base(text_buf);\n");
		source.append("switch (templateSelection) {\n");
		source.append("case OMIT_VALUE:\n");
		source.append("case ANY_VALUE:\n");
		source.append("case ANY_OR_OMIT:\n");
		source.append("case SPECIFIC_VALUE:\n");
		source.append("break;\n");
		source.append("case VALUE_LIST:\n");
		source.append("case COMPLEMENTED_LIST:\n");
		source.append("text_buf.push_int(list_value.size());\n");
		source.append("for (int i = 0; i < list_value.size(); i++) {\n");
		source.append("list_value.get(i).encode_text(text_buf);\n");
		source.append("}\n");
		source.append("break;\n");
		source.append("default:\n");
		source.append( MessageFormat.format( "throw new TtcnError(\"Text encoder: Encoding an uninitialized/unsupported template of type {0}.\");\n", classDisplayName));
		source.append("}\n");
		source.append("}\n\n");

		source.append("@Override\n");
		source.append("public void decode_text(final Text_Buf text_buf) {\n");
		source.append("decode_text_base(text_buf);\n");
		source.append("switch (templateSelection) {\n");
		source.append("case OMIT_VALUE:\n");
		source.append("case ANY_VALUE:\n");
		source.append("case ANY_OR_OMIT:\n");
		source.append("case SPECIFIC_VALUE:\n");
		source.append("break;\n");
		source.append("case VALUE_LIST:\n");
		source.append("case COMPLEMENTED_LIST:\n");
		source.append( MessageFormat.format( "list_value = new ArrayList<{0}_template>(text_buf.pull_int().getInt());\n", className));
		source.append("for(int i = 0; i < list_value.size(); i++) {\n");
		source.append( MessageFormat.format( "final {0}_template temp = new {0}_template();\n", className));
		source.append("temp.decode_text(text_buf);\n");
		source.append("list_value.add(temp);\n");
		source.append("}\n");
		source.append("break;\n");
		source.append("default:\n");
		source.append( MessageFormat.format( "throw new TtcnError(\"Text decoder: An unknown/unsupported selection was received in a template of type {0}.\");\n", classDisplayName));
		source.append("}\n");
		source.append("}\n\n");

		source.append("}\n\n");
	}

	/**
	 * This function can be used to generate a raw field checker.
	 *
	 * used to generate into conditionals.
	 *
	 * @param source where the source code is to be generated.
	 * @param taglist the taglist as data.
	 * @param is_equal will it be used in equals style check?
	 */
	private static void genRawFieldChecker(final StringBuilder source, final rawAST_coding_taglist taglist, final boolean is_equal) {
		for (int i = 0; i < taglist.fields.size(); i++) {
			final rawAST_coding_field_list fields = taglist.fields.get(i);
			String fieldName = null;
			boolean firstExpr = true;
			if (i > 0) {
				source.append(is_equal ? " || " : " && ");
			}
			for (int j = 0; j < fields.fields.size(); j++) {
				final rawAST_coding_fields field = fields.fields.get(j);
				if (j == 0) {
					/* this is the first field reference */
					fieldName = MessageFormat.format("{0}", field.nthfieldname);
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
					fieldName = MessageFormat.format("{0}.get{1}()", fieldName, FieldSubReference.getJavaGetterName( field.nthfieldname ));

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
					source.append(MessageFormat.format("{0}.isPresent()", fieldName));
					fieldName = MessageFormat.format("{0}.get()", fieldName);
				}
			}

			if (!firstExpr) {
				source.append(is_equal ? " && " : " || ");
			}
			if (is_equal) {
				source.append(MessageFormat.format("{0}.operatorEquals({1})", fieldName, fields.expression.expression));
			} else {
				source.append(MessageFormat.format("!{0}.operatorEquals({1})", fieldName, fields.expression.expression));
			}

			if (!firstExpr && taglist.fields.size() > 1) {
				source.append(')');
			}

		}
	}

	/**
	 * This function can be used to generate a raw tag checker.
	 * 
	 * used to generate encoding code for the tags.
	 *
	 * @param source where the source code is to be generated.
	 * @param taglist the taglist as data.
	 */
	private static void genRawTagChecker(final StringBuilder source, final rawAST_coding_taglist taglist) {
		source.append("RAW_enc_tree temp_leaf;\n");
		for (int temp_tag = 0; temp_tag < taglist.fields.size(); temp_tag++) {
			final rawAST_coding_field_list tempField = taglist.fields.get(temp_tag);
			source.append("{\n");
			source.append(MessageFormat.format("int new_pos{0}[] = new int[myleaf.curr_pos.level + {1}];\n", temp_tag, tempField.fields.size()));
			source.append(MessageFormat.format("System.arraycopy(myleaf.curr_pos.pos, 0, new_pos{0}, 0, myleaf.curr_pos.level);\n", temp_tag));
			for (int l = 0; l < tempField.fields.size(); l++) {
				source.append(MessageFormat.format("new_pos{0}[myleaf.curr_pos.level + {1}] = {2};\n", temp_tag, l, tempField.fields.get(l).nthfield));
			}
			source.append(MessageFormat.format("RAW_enc_tr_pos pr_pos{0} = new RAW_enc_tr_pos(myleaf.curr_pos.level + {1}, new_pos{2});\n", temp_tag, tempField.fields.size(), temp_tag));
			source.append(MessageFormat.format("temp_leaf = myleaf.get_node(pr_pos{0});\n", temp_tag));
			source.append("if (temp_leaf != null) {\n");
			source.append(MessageFormat.format("{0}.RAW_encode({1}_descr_, temp_leaf);\n", tempField.expression.expression, tempField.fields.get(tempField.fields.size() - 1).type));
			source.append(" } else ");
		}

		source.append(" {\n");
		source.append("TTCN_EncDec_ErrorContext.error(error_type.ET_OMITTED_TAG, \"Encoding a tagged, but omitted value.\", \"\");\n");
		source.append(" }\n");
		for (int temp_tag = taglist.fields.size() - 1; temp_tag >= 0; temp_tag--) {
			source.append("}\n");
		}
	}

	/**
	 * This function generates the conditional check decoding length limit.
	 *
	 * @param aData only used to update imports if needed.
	 * @param source where the source code is to be generated.
	 * @param fieldInfos the list of field informations.
	 * @param i the index of the field to generate for.
	 * @param raw the raw attribute of the record/set.
	 * @param raw_options the pre-calculated raw options.
	 */
	private static void genRawFieldDecodeLimit(final JavaGenData aData, final ExpressionStruct expression, final List<FieldInfo> fieldInfos, final int i, final RawASTStruct raw, final ArrayList<raw_option_struct> raw_options) {
		int nof_args = 1;
		final raw_option_struct tempRawOption = raw_options.get(i);
		for (int j = 0; j < tempRawOption.lengthof; j++) {
			final int field_index = tempRawOption.lengthofField.get(j);
			if ( i > field_index && fieldInfos.get(field_index).raw.unit != -1) {
				nof_args++;
			}
		}
		if (tempRawOption.extbitgroup > 0 && raw.ext_bit_groups.get(tempRawOption.extbitgroup - 1).ext_bit != RawAST.XDEFNO) {
			nof_args++;
		}
		if (nof_args > 1) {
			final String tempvar = aData.getTemporaryVariableName();
			expression.preamble.append(MessageFormat.format("int {0} = limit;\n", tempvar));
			for (int j = 0; j < tempRawOption.lengthof; j++) {
				final int field_index = tempRawOption.lengthofField.get(j);
				if (i > field_index && fieldInfos.get(field_index).raw.unit != -1) {
					expression.preamble.append(MessageFormat.format("{0} = {0} < Math.abs(value_of_length_field{1}) ? {0} : Math.abs(value_of_length_field{1});\n", tempvar, field_index));
				}
			}
			if (tempRawOption.extbitgroup > 0 && raw.ext_bit_groups.get(tempRawOption.extbitgroup - 1).ext_bit != RawAST.XDEFNO) {
				expression.preamble.append(MessageFormat.format("{0} = {0} < group_limit ? {0} : group_limit;\n", tempvar));
			}
			expression.expression.append(tempvar);
		} else {
			expression.expression.append("limit");
		}
	}

	/**
	 * This function generates the code for decoding a record field.
	 *
	 * @param aData only used to update imports if needed.
	 * @param source where the source code is to be generated.
	 * @param fieldInfos the list of field informations.
	 * @param i the index of the field to generate for.
	 * @param raw the raw attribute of the record/set.
	 * @param raw_options the pre-calculated raw options.
	 * @param delayed_decode true to generated for delayed decoding.
	 * @param prev_ext_group the index of the previous extension group.
	 */
	private static void genRawDecodeRecordField(final JavaGenData aData, final StringBuilder source, final List<FieldInfo> fieldInfos, final int i, final RawASTStruct raw, final ArrayList<raw_option_struct> raw_options, final boolean delayed_decode, final AtomicInteger prev_ext_group) {
		final raw_option_struct tempRawOption = raw_options.get(i);

		if (tempRawOption.ptrbase) {
			source.append(MessageFormat.format("start_pos_of_field{0} = buff.get_pos_bit();\n", i));
		}
		if (prev_ext_group.get() != tempRawOption.extbitgroup) {
			prev_ext_group.set(tempRawOption.extbitgroup);
			if (prev_ext_group.get() > 0 && raw.ext_bit_groups.get(tempRawOption.extbitgroup - 1).ext_bit != RawAST.XDEFNO) {
				if (tempRawOption.pointerof > 0) {
					final FieldInfo pointedField = fieldInfos.get(tempRawOption.pointerof - 1);

					source.append("{\n");
					source.append("int old_pos = buff.get_pos_bit();\n");
					source.append(MessageFormat.format("if (start_of_field{0} != -1 && start_pos_of_field{1} != -1) '{'\n", i, pointedField.raw.pointerbase));
					source.append(MessageFormat.format("start_of_field{0} = start_pos_of_field{1} + get{2}(){3}.getInt() * {4} + {5};\n", i, pointedField.raw.pointerbase, pointedField.mVarName, pointedField.isOptional ? ".get()" : "", pointedField.raw.unit, pointedField.raw.ptroffset));
					source.append(MessageFormat.format("buff.set_pos_bit(start_of_field{0});\n", i));
					source.append(MessageFormat.format("limit = end_of_available_data - start_of_field{0};\n", i));
				}

				source.append("{\n");
				source.append("char[] data = buff.get_read_data();\n");
				source.append("int count = 1;\n");
				source.append("int rot = local_top_order == raw_order_t.ORDER_LSB ? 0: 7;\n");
				source.append(MessageFormat.format("while (((data[count - 1] >> rot) & 0x01) == {0} && count * 8 < limit) '{'\n", raw.ext_bit_groups.get(tempRawOption.extbitgroup - 1).ext_bit == RawAST.XDEFYES ? 0: 1));
				source.append("count++;\n");
				source.append("}\n");
				source.append(" if (limit > 0) {\n");
				source.append("group_limit = count * 8;\n");
				source.append("}\n");
				source.append("}\n");

				if (tempRawOption.pointerof > 0) {
					source.append(" } else {\n");
					source.append("group_limit = 0;\n");
					source.append("}\n");
					source.append("buff.set_pos_bit(old_pos);\n");
					source.append("limit = end_of_available_data - old_pos;\n");
					source.append("}\n");
				}
			}
		}

		final FieldInfo fieldInfo = fieldInfos.get(i);
		final int crosstagsize = fieldInfo.raw == null || fieldInfo.raw.crosstaglist == null || fieldInfo.raw.crosstaglist.list == null ? 0 : fieldInfo.raw.crosstaglist.list.size();
		if (crosstagsize > 0) {
			int other = -1;
			boolean first_value = true;
			for (int j = 0; j < crosstagsize; j++) {
				final rawAST_coding_taglist cur_choice = fieldInfo.raw.crosstaglist.list.get(j);
				if (cur_choice.fields != null && cur_choice.fields.size() > 0) {
					if (first_value) {
						source.append("if (");
						first_value = false;
					} else {
						source.append(" else if (");
					}
					genRawFieldChecker(source, cur_choice, true);
					source.append(") {\n");
					source.append(MessageFormat.format("selected_field = {0};\n", cur_choice.fieldnum));
					source.append("}");
				} else {
					other = cur_choice.fieldnum;
				}
			}
			source.append(" else {\n");
			source.append(MessageFormat.format("selected_field = {0};\n", other));
			source.append("}\n");
		}
		/* check the presence of optional field*/
		if (fieldInfo.isOptional) {
			/* check if enough bits to decode the field*/
			source.append("if ( limit > 0");
			for (int a = 0; a < tempRawOption.lengthof; a++) {
				final int field_index = tempRawOption.lengthofField.get(a);
				if (i > field_index) {
					source.append(MessageFormat.format(" && value_of_length_field{0} > 0", field_index));
				}
			}
			if (tempRawOption.extbitgroup > 0 && raw.ext_bit_groups.get(tempRawOption.extbitgroup - 1).ext_bit != RawAST.XDEFNO) {
				source.append(" && group_limit > 0");
			}
			if (tempRawOption.pointerof > 0) {
				source.append(MessageFormat.format(" && start_of_field{0} != -1 && start_pos_of_field{1} != -1", i, fieldInfos.get(tempRawOption.pointerof - 1).raw.pointerbase));
			}

			final int presenceSize = fieldInfo.raw == null || fieldInfo.raw.presence == null || fieldInfo.raw.presence.fields == null ? 0 : fieldInfo.raw.presence.fields.size();
			if (presenceSize > 0) {
				source.append(" && ");
				if (presenceSize > 1) {
					source.append('(');
				}
				genRawFieldChecker(source, fieldInfo.raw.presence, true);
				if (presenceSize > 1) {
					source.append(')');
				}
			}
			if (crosstagsize > 0) {
				source.append("&& selected_field != -1");
			}
			source.append(") {\n");
		}
		if (tempRawOption.pointerof > 0) {
			final FieldInfo tempPointed = fieldInfos.get(tempRawOption.pointerof - 1);
			source.append(MessageFormat.format("start_of_field{0} = start_pos_of_field{1} + get{2}(){3}.getInt() * {4} + {5};\n", i, tempPointed.raw.pointerbase, tempPointed.mJavaVarName, tempPointed.isOptional ? ".get()":"", tempPointed.raw.unit, tempPointed.raw.ptroffset));
			source.append(MessageFormat.format("buff.set_pos_bit(start_of_field{0});\n", i));
			source.append(MessageFormat.format("limit = end_of_available_data - start_of_field{0};\n", i));
		}
		if (fieldInfo.isOptional) {
			source.append("int fl_start_pos = buff.get_pos_bit();\n");
		}

		final ExpressionStruct expression = new ExpressionStruct();
		if (delayed_decode) {
			/* the fixed field length is used as limit in case of delayed decoding */
			expression.expression.append(fieldInfo.raw.length);
		} else {
			genRawFieldDecodeLimit(aData, expression, fieldInfos, i, raw, raw_options);
		}
		if (expression.preamble.length() > 0) {
			source.append(expression.preamble);
		}
		source.append(MessageFormat.format("decoded_field_length = get{0}(){1}.RAW_decode({2}_descr_, buff, ", fieldInfo.mJavaVarName, fieldInfo.isOptional ? ".get()":"", fieldInfo.mTypeDescriptorName));
		source.append(expression.expression);
		source.append(MessageFormat.format(", local_top_order, {0}", fieldInfo.isOptional ? "true": "no_err"));
		if (crosstagsize > 0) {
			source.append(", selected_field");
		} else {
			source.append(", -1");
		}
		boolean found = false;
		for (int a = 0; a < tempRawOption.lengthof && !found; a++) {
			final int field_index = tempRawOption.lengthofField.get(a);
			if (fieldInfos.get(field_index).raw.unit == -1) {
				source.append(MessageFormat.format(", value_of_length_field{0}", field_index));
				found = true;
			}
		}
		if (!found) {
			source.append(", true");
		}
		source.append(");\n");

		if (delayed_decode) {
			source.append(MessageFormat.format("if ( decoded_field_length != {0}) '{'\n", fieldInfo.raw.length));
			source.append("return -1;\n");
			source.append("}\n");
		} else if(fieldInfo.isOptional) {
			source.append("if (decoded_field_length < 1) {\n");
			source.append(MessageFormat.format("{0}.assign(template_sel.OMIT_VALUE);\n", fieldInfo.mVarName));
			source.append("buff.set_pos_bit(fl_start_pos);\n");
			source.append(" } else {\n");
		} else {
			source.append("if (decoded_field_length < 0) {\n");
			source.append("return decoded_field_length;\n");
			source.append("}\n");
		}
		if (tempRawOption.tag_type > 0 && raw.taglist.list.get(tempRawOption.tag_type - 1).fields.size() > 0) {
			final rawAST_coding_taglist cur_choice = raw.taglist.list.get(tempRawOption.tag_type - 1);

			source.append("if (");
			genRawFieldChecker(source, cur_choice, false);
			source.append(") {\n");
			if (fieldInfo.isOptional) {
				source.append(MessageFormat.format("{0}.assign(template_sel.OMIT_VALUE);\n", fieldInfo.mVarName));
				source.append("buff.set_pos_bit(fl_start_pos);\n");
				source.append(" } else {");
			} else {
				source.append("return -1;\n");
				source.append("}\n");
			}
		}
		if (!delayed_decode) {
			source.append("decoded_length += decoded_field_length;\n");
			source.append("limit -= decoded_field_length;\n");
			source.append("last_decoded_pos = Math.max(last_decoded_pos, buff.get_pos_bit());\n");
		}
		if (tempRawOption.extbitgroup > 0 && raw.ext_bit_groups.get(tempRawOption.extbitgroup - 1).ext_bit != RawAST.XDEFNO) {
			source.append("group_limit -= decoded_field_length;\n");
		}
		if (tempRawOption.lengthto) {
			if (fieldInfo.raw.lengthindex != null) {
				if (fieldInfo.raw.lengthindex.fieldtype == rawAST_coding_field_type.OPTIONAL_FIELD) {
					source.append(MessageFormat.format("if ({0}{1}.get{2}().isPresent()) '{'\n", fieldInfo.mVarName, fieldInfo.isOptional? ".get()":"", FieldSubReference.getJavaGetterName(fieldInfo.raw.lengthindex.nthfieldname)));
				}
				if (fieldInfo.raw.lengthto_offset != 0) {
					source.append(MessageFormat.format("{0}{1}.get{2}(){3}.assign({0}{1}.get{2}(){3} - {4});\n",
							fieldInfo.mVarName, fieldInfo.isOptional ? ".get()" : "", FieldSubReference.getJavaGetterName(fieldInfo.raw.lengthindex.nthfieldname), fieldInfo.raw.lengthindex.fieldtype == rawAST_coding_field_type.OPTIONAL_FIELD ? ".get()" : "", fieldInfo.raw.lengthto_offset));
				}
				source.append(MessageFormat.format("value_of_length_field{0} += {1}{2}.get{3}(){4}.getLong() * {5};\n",
						i, fieldInfo.mVarName, fieldInfo.isOptional ? ".get()" : "", FieldSubReference.getJavaGetterName(fieldInfo.raw.lengthindex.nthfieldname), fieldInfo.raw.lengthindex.fieldtype == rawAST_coding_field_type.OPTIONAL_FIELD ? ".get()" : "", fieldInfo.raw.unit == -1 ? 1 : fieldInfo.raw.unit));
				if (fieldInfo.raw.lengthindex.fieldtype == rawAST_coding_field_type.OPTIONAL_FIELD) {
					source.append("}\n");
				}
			} else if (fieldInfo.raw.union_member_num > 0) {
				source.append(MessageFormat.format("switch ({0}{1}.get_selection()) '{'\n", fieldInfo.mVarName, fieldInfo.isOptional ? ".get()":""));
				for (int m = 0; m < fieldInfo.raw.member_name.size(); m++) {
					source.append(MessageFormat.format("case {0}.{1}", fieldInfo.raw.member_name.get(0), fieldInfo.raw.member_name.get(m)));
					if (fieldInfo.raw.lengthto_offset != 0) {
						source.append(MessageFormat.format("{0}{1}.get{2}().assign({0}{1}.get{2}() - {3});\n", fieldInfo.mVarName, fieldInfo.isOptional ? ".get()" : "", fieldInfo.raw.member_name.get(m), fieldInfo.raw.lengthto_offset));
					}
					source.append(MessageFormat.format("value_of_length_field{0} += {1}{2}.get{3}().getLong() * {4};\n", i, fieldInfo.mVarName, fieldInfo.isOptional ? ".get()" : "", fieldInfo.raw.member_name.get(m), fieldInfo.raw.unit == -1 ? 1 : fieldInfo.raw.unit));
					source.append("break;\n");
				}
				source.append("default:\n");
				source.append(MessageFormat.format("value_of_length_field{0} = 0;\n", i));
				source.append("}\n");
			} else {
				if (fieldInfo.raw.lengthto_offset != 0) {
					source.append(MessageFormat.format("{0}{1}.assign({0}{1}.getInt() - {2});\n", fieldInfo.mVarName, fieldInfo.isOptional? ".get()":"", fieldInfo.raw.lengthto_offset));
				}
				source.append(MessageFormat.format("value_of_length_field{0} += {1}{2}.getLong() * {3};\n", i, fieldInfo.mVarName, fieldInfo.isOptional ? ".get()" : "", fieldInfo.raw.unit == -1 ? 1 : fieldInfo.raw.unit));
			}
		}
		if (tempRawOption.pointerto) {
			source.append(MessageFormat.format("start_of_field{0} = {1}{2}.getInt() {3};\n ", fieldInfo.raw.pointerto, fieldInfo.mVarName, fieldInfo.isOptional? ".get()":"", fieldInfo.raw.ptroffset > 0? " + 1": "- 1"));
		}
		if (!delayed_decode) {
			/* mark the used bits in length area*/
			for (int a = 0; a < tempRawOption.lengthof; a++) {
				final int field_index = tempRawOption.lengthofField.get(a);
				source.append(MessageFormat.format("value_of_length_field{0} -= decoded_field_length;\n", field_index));
				if (i == field_index) {
					source.append(MessageFormat.format("if (value_of_length_field{0} < 0) '{'\n", field_index));
					source.append("return -1;\n");
					source.append("}\n");
				}
			}
		}
		if (fieldInfo.isOptional) {
			source.append("}\n");
			source.append("}");
			if (tempRawOption.tag_type > 0) {
				source.append("\n}");
			}
			source.append(" else {\n");
			source.append(MessageFormat.format("{0}.assign(template_sel.OMIT_VALUE);\n", fieldInfo.mVarName));
			source.append("}\n");
		}
	}
}
