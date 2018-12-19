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
 * Utility class for generating the value and template classes for record and
 * set types.
 *
 * The code generated for record and set types only differs in matching and
 * encoding.
 *
 * @author Kristof Szabados
 * @author Arpad Lovassy
 */
public final class RecordSetCodeGenerator {
	/**
	 * Data structure to store sequence field variable and type names.
	 * Used for java code generation.
	 */
	public static class FieldInfo {

		/** Java type name of the field */
		private final String mJavaTypeName;

		/** Java template type name of the field */
		private final String mJavaTemplateTypeName;

		/** Field variable name in TTCN-3 and java */
		private final String mVarName;

		/** The user readable name of the field, typically used in error messages */
		private final String mDisplayName;

		private final boolean isOptional;

		private final boolean ofType;//TODO check if this is really neded here

		/** Field variable name in java getter/setter function names and parameters */
		private final String mJavaVarName;

		/** Java AST type name (for debug purposes) */
		private final String mTTCN3TypeName;
	
		private final String mTypeDescriptorName;

		public boolean hasRaw;
		public RawASTStruct raw;

		/**
		 * @param fieldType
		 *                the string representing the type of this field
		 *                in the generated code.
		 * @param fieldTemplateType
		 *                the string representing the template type of
		 *                this field in the generated code.
		 * @param fieldName
		 *                the string representing the name of this field
		 *                in the generated code.
		 * @param displayName
		 *                The user readable name of the field, typically
		 *                used in error messages
		 * @param isOptional
		 *                {@code true} if the field is optional.
		 * @param debugName
		 *                additional text printed out in a comment after
		 *                the generated local variables.
		 * @param typeDescriptorName
		 *                the name of the type descriptor.
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
	 * This function can be used to generate the value class of record and
	 * set types
	 *
	 * defRecordClass in compilers/record.{h,c}
	 *
	 * @param aData
	 *                used to access build settings.
	 * @param source
	 *                where the source code is to be generated.
	 * @param className
	 *                the name of the generated class representing the
	 *                record/set type.
	 * @param classDisplayName
	 *                the user readable name of the type to be generated.
	 * @param fieldInfos
	 *                the list of information about the fields.
	 * @param hasOptional
	 *                {@code true} if the type has an optional field.
	 * @param isSet
	 *                {@code true} if generating code for a set, {@code false} if generating
	 *                code for a record.
	 * @param hasRaw
	 *                {@code true} if the type has raw attributes.
	 * @param raw
	 *                the raw coding related settings if applicable.
	 */
	public static void generateValueClass(final JavaGenData aData, final StringBuilder source, final String className, final String classDisplayname,
			final List<FieldInfo> fieldInfos, final boolean hasOptional, final boolean isSet, final boolean hasRaw, final RawASTStruct raw) {
		aData.addBuiltinTypeImport("Base_Type");
		aData.addBuiltinTypeImport("Text_Buf");
		aData.addImport("java.text.MessageFormat");
		aData.addBuiltinTypeImport("TTCN_Logger");
		aData.addBuiltinTypeImport("RAW.RAW_enc_tr_pos");
		aData.addBuiltinTypeImport("RAW.RAW_enc_tree");
		aData.addBuiltinTypeImport("TTCN_Buffer");
		aData.addBuiltinTypeImport("TTCN_EncDec.coding_type");
		aData.addBuiltinTypeImport("TTCN_EncDec.error_type");
		aData.addBuiltinTypeImport("TTCN_EncDec.raw_order_t");
		aData.addBuiltinTypeImport("TTCN_EncDec_ErrorContext");
		aData.addBuiltinTypeImport("Param_Types.Module_Parameter");
		if(hasOptional) {
			aData.addBuiltinTypeImport("Optional");
			aData.addBuiltinTypeImport("Optional.optional_sel");
			aData.addBuiltinTypeImport("Base_Template.template_sel");
		}

		final boolean rawNeeded = hasRaw; //TODO can be forced optionally if needed
		if (rawNeeded) {
			aData.addBuiltinTypeImport("RAW.RAW_Force_Omit");
		}

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
		generateConstructor( aData, source, fieldInfos, className );
		generateConstructorManyParams( aData, source, fieldInfos, className );
		generateConstructorCopy( aData, source, fieldInfos, className, classDisplayname );
		generateoperator_assign( aData, source, fieldInfos, className, classDisplayname );
		generateCleanUp( source, fieldInfos );
		generateIsBound( source, fieldInfos );
		generateIsPresent( source, fieldInfos );
		generateIsValue( source, fieldInfos );
		generateoperator_equals( aData, source, fieldInfos, className, classDisplayname);
		generateGettersSetters( aData, source, fieldInfos );
		generateSizeOf( aData, source, fieldInfos );
		generateLog( source, fieldInfos );
		generateValueSetParam(source, classDisplayname, fieldInfos, isSet);
		generateValueSetImplicitOmit(source, fieldInfos);
		generateValueEncodeDecodeText(source, fieldInfos);
		generateValueEncodeDecode(aData, source, className, classDisplayname, fieldInfos, isSet, rawNeeded, raw);

		source.append( "\t}\n" );
	}

	/**
	 * This function can be used to generate the template class of record
	 * and set types
	 *
	 * defRecordClass in compilers/record.{h,c}
	 *
	 * @param aData
	 *                used to access build settings.
	 * @param source
	 *                where the source code is to be generated.
	 * @param className
	 *                the name of the generated class representing the
	 *                record/set type.
	 * @param classDisplayName
	 *                the user readable name of the type to be generated.
	 * @param fieldInfos
	 *                the list of information about the fields.
	 * @param hasOptional
	 *                {@code true} if the type has an optional field.
	 * @param isSet
	 *                {@code true} if generating code for a set, false if generating
	 *                code for a record.
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
		aData.addBuiltinTypeImport("TTCN_Logger");

		if (fieldInfos.isEmpty()) {
			generateEmptyTemplateClass(aData, source, className, classDisplayName, fieldInfos, hasOptional, isSet);
			return;
		}

		aData.addBuiltinTypeImport("TitanInteger");

		source.append( MessageFormat.format( "\tpublic static class {0}_template extends Base_Template '{'\n", className ) );
		generateTemplateDeclaration( aData, source, fieldInfos, className );
		generateTemplateConstructors( aData, source, className, classDisplayName );
		generateTemplateoperator_assign( aData, source, className, classDisplayName );
		generateTemplateCopyTemplate( source, fieldInfos, className, classDisplayName );
		generateTemplateSetType( source, className, classDisplayName );
		generateTemplateIsBound( source, fieldInfos );
		generateTemplateIsPresent( source );
		generateTemplateIsValue( source, fieldInfos );
		generateTemplateGetter( aData, source, fieldInfos, classDisplayName );
		generateTemplateMatch( aData, source, fieldInfos, className, classDisplayName );
		generateTemplateValueOf( source, fieldInfos, className, classDisplayName );
		generateTemplateSizeOf( aData, source, fieldInfos, classDisplayName );
		generateTemplateListItem( source, className, classDisplayName );
		generateTemplateLog( aData, source, fieldInfos, className, classDisplayName );
		generateTemplateEncodeDecodeText(source, fieldInfos, className, classDisplayName);
		generateTemplateSetParam(source, classDisplayName, fieldInfos, isSet);
		generateTemplateCheckRestriction(source, classDisplayName, fieldInfos, isSet);

		source.append("\t}\n");
	}

	/**
	 * Generating declaration of the member variables
	 * 
	 * @param aData
	 *                used to access build settings.
	 * @param source
	 *                the source to be updated
	 * @param aNamesList
	 *                sequence field variable and type names
	 */
	private static void generateDeclaration( final JavaGenData aData, final StringBuilder source, final List<FieldInfo> aNamesList ) {
		for ( final FieldInfo fi : aNamesList ) {
			if (fi.isOptional) {
				aData.addCommonLibraryImport("Optional");

				source.append( MessageFormat.format( "\t\tprivate final Optional<{0}> {1};", fi.mJavaTypeName, fi.mVarName ) );
			} else {
				source.append( MessageFormat.format( "\t\tprivate final {0} {1};", fi.mJavaTypeName, fi.mVarName ) );
			}

			if ( aData.isDebug() ) {
				source.append( " //" );
				source.append( fi.mTTCN3TypeName );
			}
			source.append( '\n' );
		}
	}

	/**
	 * Generating constructor without parameter
	 *
	 * @param aData
	 *                only used to update imports if needed
	 * @param aSb
	 *                the output, where the java code is written
	 * @param aNamesList
	 *                sequence field variable and type names
	 * @param aClassName
	 *                the class name of the record/set class
	 */
	private static void generateConstructor( final JavaGenData aData, final StringBuilder aSb, final List<FieldInfo> aNamesList,
			final String aClassName ) {
		aSb.append( '\n' );
		if ( aData.isDebug() ) {
			aSb.append( "\t\t/**\n" );
			aSb.append( "\t\t * Initializes to unbound value.\n" );
			aSb.append( "\t\t * */\n" );
		}
		aSb.append( MessageFormat.format( "\t\tpublic {0}() '{'\n", aClassName ) );
		for ( final FieldInfo fi : aNamesList ) {
			if (fi.isOptional) {
				aSb.append( MessageFormat.format( "\t\t\tthis.{0} = new Optional<{1}>({1}.class);\n", fi.mVarName, fi.mJavaTypeName ) );
			} else {
				aSb.append( MessageFormat.format( "\t\t\tthis.{0} = new {1}();\n", fi.mVarName, fi.mJavaTypeName ) );
			}
		}

		aSb.append( "\t\t}\n" );
	}

	/**
	 * Generating constructor with many parameters (one for each record/set
	 * field)
	 *
	 * @param aData
	 *                only used to update imports if needed
	 * @param aSb
	 *                the output, where the java code is written
	 * @param aNamesList
	 *                sequence field variable and type names
	 * @param aClassName
	 *                the class name of the record/set class
	 */
	private static void generateConstructorManyParams( final JavaGenData aData, final StringBuilder aSb, final List<FieldInfo> aNamesList,
			final String aClassName ) {
		if ( aNamesList == null || aNamesList.isEmpty()) {
			// Record type is empty, and parameter list would be also empty, but
			// constructor without parameters is already created, so nothing to do
			return;
		}

		aSb.append( '\n' );
		if (aData.isDebug()) {
			aSb.append("\t\t/**\n");
			aSb.append("\t\t * Initializes from given field values. The number of arguments equals\n");
			aSb.append("\t\t * to the number of fields.\n");
			aSb.append("\t\t *\n");
			for ( final FieldInfo fi : aNamesList ) {
				aSb.append(MessageFormat.format("\t\t * @param {0}\n", fi.mVarName));
				aSb.append(MessageFormat.format("\t\t *                the value of field {0}\n", fi.mDisplayName));
			}

			aSb.append("\t\t * */\n");
		}
		aSb.append( MessageFormat.format( "\t\tpublic {0}(", aClassName ) );
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
			aSb.append( ' ' );
			aSb.append( fi.mJavaVarName );
		}
		aSb.append( " ) {\n" );
		for ( final FieldInfo fi : aNamesList ) {
			if (fi.isOptional) {
				aSb.append( MessageFormat.format( "\t\t\tthis.{0} = new Optional<{1}>({1}.class);\n", fi.mVarName, fi.mJavaTypeName ) );
				aSb.append( MessageFormat.format( "\t\t\tthis.{0}.operator_assign( {1} );\n", fi.mVarName, fi.mJavaVarName ) );
			} else {
				aSb.append( MessageFormat.format( "\t\t\tthis.{0} = new {1}( {2} );\n", fi.mVarName, fi.mJavaTypeName, fi.mJavaVarName ) );
			}
		}
		aSb.append( "\t\t}\n" );
	}

	/**
	 * Generating constructor with 1 parameter (copy constructor)
	 *
	 * @param aData
	 *                only used to update imports if needed
	 * @param aSb
	 *                the output, where the java code is written
	 * @param aNamesList
	 *                sequence field variable and type names
	 * @param aClassName
	 *                the class name of the record/set class
	 * @param displayName
	 *                the user readable name of the type to be generated.
	 */
	private static void generateConstructorCopy( final JavaGenData aData, final StringBuilder aSb, final List<FieldInfo> aNamesList, final String aClassName, final String displayName ) {
		aSb.append( '\n' );
		if ( aData.isDebug() ) {
			aSb.append( "\t\t/**\n" );
			aSb.append( "\t\t * Initializes to a given value.\n" );
			aSb.append( "\t\t *\n" );
			aSb.append( "\t\t * @param otherValue\n" );
			aSb.append( "\t\t *                the value to initialize to.\n" );
			aSb.append( "\t\t * */\n" );
		}
		aSb.append( MessageFormat.format( "\t\tpublic {0}( final {0} otherValue) '{'\n", aClassName ) );
		aSb.append( MessageFormat.format( "\t\t\t\totherValue.must_bound(\"Copying of an unbound value of type {0}.\");\n", displayName ) );
		for ( final FieldInfo fi : aNamesList ) {
			if (fi.isOptional) {
				aSb.append(MessageFormat.format("\t\t\t{0} = new Optional<{1}>({1}.class);\n", fi.mVarName, fi.mJavaTypeName));
			} else {
				aSb.append(MessageFormat.format("\t\t\t{0} = new {1}();\n", fi.mVarName, fi.mJavaTypeName));
			}

		}

		aSb.append( "\t\t\toperator_assign( otherValue );\n" );
		aSb.append( "\t\t}\n\n" );
	}

	/**
	 * Generating operator_assign() function
	 * 
	 * @param aData
	 *                only used to update imports if needed
	 * @param source
	 *                the source code generated
	 * @param aNamesList
	 *                sequence field variable and type names
	 * @param aClassName
	 *                the class name of the record/set class
	 * @param classReadableName
	 *                the readable name of the class
	 */
	private static void generateoperator_assign( final JavaGenData aData, final StringBuilder source, final List<FieldInfo> aNamesList,
			final String aClassName, final String classReadableName ) {
		aData.addCommonLibraryImport( "TtcnError" );

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
		source.append(MessageFormat.format("\t\tpublic {0} operator_assign(final {0} otherValue ) '{'\n", aClassName));
		source.append(MessageFormat.format("\t\t\t\totherValue.must_bound( \"Assignment of an unbound value of type {0}\");\n", classReadableName));
		source.append("\t\t\tif (otherValue != this) {\n");
		for ( final FieldInfo fi : aNamesList ) {
			source.append(MessageFormat.format("\t\t\t\tif ( otherValue.get_field_{0}().is_bound() ) '{'\n", fi.mJavaVarName));
			source.append(MessageFormat.format("\t\t\t\t\tthis.{0}.operator_assign( otherValue.get_field_{1}() );\n", fi.mVarName, fi.mJavaVarName));
			source.append("\t\t\t\t} else {\n");
			source.append(MessageFormat.format("\t\t\t\t\tthis.{0}.clean_up();\n", fi.mVarName));
			source.append("\t\t\t\t}\n");
		}
		source.append( "\t\t\t}\n\n" );
		source.append( "\t\t\treturn this;\n");
		source.append("\t\t}\n");

		source.append('\n');
		source.append("\t\t@Override\n");
		source.append("\t\tpublic ").append( aClassName ).append(" operator_assign(final Base_Type otherValue) {\n");
		source.append("\t\t\tif (otherValue instanceof ").append(aClassName).append(" ) {\n");
		source.append("\t\t\t\treturn operator_assign((").append( aClassName ).append(") otherValue);\n");
		source.append("\t\t\t}\n\n");
		source.append("\t\t\tthrow new TtcnError(MessageFormat.format(\"Internal Error: value `{0}'' can not be cast to ").append(classReadableName).append("\", otherValue));\n");
		source.append("\t\t}\n");

	}

	/**
	 * Generating clean_up() function
	 * 
	 * @param aSb
	 *                the output, where the java code is written
	 * @param aNamesList
	 *                sequence field variable and type names
	 */
	private static void generateCleanUp( final StringBuilder aSb, final List<FieldInfo> aNamesList ) {
		aSb.append("\n\t\t@Override\n");
		aSb.append( "\t\tpublic void clean_up() {\n" );
		for ( final FieldInfo fi : aNamesList ) {
			aSb.append( "\t\t\t" );
			aSb.append( fi.mVarName );
			aSb.append( ".clean_up();\n" );
		}
		aSb.append( "\t\t}\n" );
	}

	/**
	 * Generating is_bound() function
	 * 
	 * @param aSb
	 *                the output, where the java code is written
	 * @param aNamesList
	 *                sequence field variable and type names
	 */
	private static void generateIsBound( final StringBuilder aSb, final List<FieldInfo> aNamesList ) {
		aSb.append( "\n\t\t@Override\n");
		aSb.append( "\t\tpublic boolean is_bound() {\n" );
		aSb.append( "\t\t\treturn " );
		for (int i = 0; i < aNamesList.size(); i++) {
			final FieldInfo fi = aNamesList.get(i);

			if (i != 0) {
				aSb.append( "\n\t\t\t\t\t|| " );
			}
			if (fi.isOptional) {
				aSb.append( MessageFormat.format( "optional_sel.OPTIONAL_OMIT.equals({0}.get_selection()) || {0}.is_bound()", fi.mVarName ) );
			} else {
				aSb.append( MessageFormat.format( "{0}.is_bound()", fi.mVarName ) );
			}
		}
		aSb.append(";\n");
		aSb.append("\t\t}\n");
	}

	/**
	 * Generating is_present() function
	 * 
	 * @param aSb
	 *                the output, where the java code is written
	 * @param aNamesList
	 *                sequence field variable and type names
	 */
	private static void generateIsPresent( final StringBuilder aSb, final List<FieldInfo> aNamesList ) {
		aSb.append( "\n\t\t@Override\n");
		aSb.append( "\t\tpublic boolean is_present() {\n" );
		aSb.append( "\t\t\treturn is_bound();\n");
		aSb.append( "\t\t}\n" );
	}

	/**
	 * Generating is_value() function
	 * 
	 * @param aSb
	 *                the output, where the java code is written
	 * @param aNamesList
	 *                sequence field variable and type names
	 */
	private static void generateIsValue( final StringBuilder aSb, final List<FieldInfo> aNamesList ) {
		aSb.append( "\n\t\t@Override\n");
		aSb.append( "\t\tpublic boolean is_value() {\n" );
		if ( aNamesList == null || aNamesList.isEmpty() ) {
			aSb.append( "\t\t\treturn false;\n" +
					"\t\t}\n" );
			return;
		}

		aSb.append( "\t\t\treturn " );
		for (int i = 0; i < aNamesList.size(); i++) {
			final FieldInfo fi = aNamesList.get(i);

			if (i != 0) {
				aSb.append( "\n\t\t\t\t\t&& " );
			}
			if (fi.isOptional) {
				aSb.append( MessageFormat.format( "(optional_sel.OPTIONAL_OMIT.equals({0}.get_selection()) || {0}.is_value())", fi.mVarName ) );
			} else {
				aSb.append( MessageFormat.format( "{0}.is_value()", fi.mVarName ) );
			}
		}
		aSb.append(";\n");
		aSb.append("\t\t}\n");
	}

	/**
	 * Generating size_of() function
	 * 
	 * @param aData
	 *                used to access build settings.
	 * @param aSb
	 *                the output, where the java code is written
	 * @param aNamesList
	 *                sequence field variable and type names
	 */
	private static void generateSizeOf( final JavaGenData aData, final StringBuilder aSb, final List<FieldInfo> aNamesList ) {
		if (aData.isDebug()) {
			aSb.append("\t\t/**\n");
			aSb.append("\t\t * Returns the size (number of fields).\n");
			aSb.append("\t\t *\n");
			aSb.append("\t\t * size_of in the core\n");
			aSb.append("\t\t *\n");
			aSb.append("\t\t * @return the size of the structure.\n");
			aSb.append("\t\t * */\n");
		}
		aSb.append( "\t\tpublic TitanInteger size_of() {\n" );
		//number of non-optional fields
		int size = 0;
		for ( final FieldInfo fi : aNamesList ) {
			if (!fi.isOptional) {
				size++;
			}
		}

		if (size == aNamesList.size()) {
			aSb.append( MessageFormat.format( "\t\t\treturn new TitanInteger({0});\n", size ) );
		} else {
			aSb.append( MessageFormat.format( "\t\t\tint sizeof = {0};\n", size ) );
			for ( final FieldInfo fi : aNamesList ) {
				if (fi.isOptional) {
					aSb.append( MessageFormat.format( "\t\t\tif ({0}.ispresent()) '{'\n", fi.mVarName ) );
					aSb.append( "\t\t\t\tsizeof++;\n" );
					aSb.append( "\t\t\t}\n" );
				}
			}

			aSb.append( "\t\t\treturn new TitanInteger(sizeof);\n" );
		}

		aSb.append( "\t\t}\n\n" );
	}

	/**
	 * Generating log() function
	 * 
	 * @param aSb
	 *                the output, where the java code is written
	 * @param aNamesList
	 *                sequence field variable and type names
	 */
	private static void generateLog(final StringBuilder aSb, final List<FieldInfo> aNamesList ) {
		aSb.append("\t\t@Override\n");
		aSb.append("\t\tpublic void log() {\n");
		aSb.append("\t\t\tif (!is_bound()) {\n");
		aSb.append("\t\t\t\tTTCN_Logger.log_event_unbound();\n");
		aSb.append("\t\t\t\treturn;\n");
		aSb.append("\t\t\t}\n");
		aSb.append("\t\t\tTTCN_Logger.log_char('{');\n");
		for (int i = 0 ; i < aNamesList.size(); i++) {
			final FieldInfo fieldInfo = aNamesList.get(i);

			if (i > 0) {
				aSb.append("\t\t\tTTCN_Logger.log_char(',');\n");
			}
			aSb.append(MessageFormat.format("\t\t\tTTCN_Logger.log_event_str(\" {0} := \");\n", fieldInfo.mDisplayName));
			aSb.append(MessageFormat.format("\t\t\t{0}.log();\n", fieldInfo.mVarName));
		}
		aSb.append("\t\t\tTTCN_Logger.log_event_str(\" }\");\n");
		aSb.append("\t\t}\n\n");
	}

	/**
	 * Generate set_param.
	 *
	 * @param aSb
	 *                the output, where the java code is written
	 * @param classReadableName
	 *                the readable name of the class
	 * @param fieldInfos
	 *                sequence field variable and type names
	 * @param isSet
	 *                {@code true} if a set, {@code false} if a record
	 */
	private static void generateValueSetParam(final StringBuilder aSb, final String classReadableName, final List<FieldInfo> fieldInfos, final boolean isSet) {
		aSb.append("\t\t@Override\n");
		aSb.append("\t\tpublic void set_param(final Module_Parameter param) {\n");
		aSb.append(MessageFormat.format("\t\t\tparam.basic_check(Module_Parameter.basic_check_bits_t.BC_VALUE.getValue(), \"{0} value\");\n", isSet ? "set" : "record"));
		aSb.append("\t\t\tswitch (param.get_type()) {\n");
		aSb.append("\t\t\tcase MP_Value_List:\n");
		aSb.append(MessageFormat.format("\t\t\t\tif (param.get_size() > {0}) '{'\n", fieldInfos.size()));
		aSb.append(MessageFormat.format("\t\t\t\t\tparam.error(MessageFormat.format(\"{0} value of type {1} has {2} fields but list value has '{'0'}' fields.\", param.get_size()));\n", isSet ? "set" : "record", classReadableName, fieldInfos.size()));
		aSb.append("\t\t\t\t}\n");
		for (int i = 0 ; i < fieldInfos.size(); i++) {
			final FieldInfo fieldInfo = fieldInfos.get(i);

			aSb.append(MessageFormat.format("\t\t\t\tif (param.get_size() > {0} && param.get_elem({0}).get_type() != Module_Parameter.type_t.MP_NotUsed) '{'\n", i));
			aSb.append(MessageFormat.format("\t\t\t\t\tget_field_{0}().set_param(param.get_elem({1}));\n", fieldInfo.mJavaVarName, i));
			aSb.append("\t\t\t\t}\n");
		}
		aSb.append("\t\t\t\tbreak;\n");
		aSb.append("\t\t\tcase MP_Assignment_List: {\n");
		aSb.append("\t\t\t\tfinal boolean value_used[] = new boolean[param.get_size()];\n");
		for (int i = 0 ; i < fieldInfos.size(); i++) {
			final FieldInfo fieldInfo = fieldInfos.get(i);

			aSb.append("\t\t\t\tfor (int val_idx = 0; val_idx < param.get_size(); val_idx++) {\n");
			aSb.append("\t\t\t\t\tfinal Module_Parameter curr_param = param.get_elem(val_idx);\n");
			aSb.append(MessageFormat.format("\t\t\t\t\tif (\"{0}\".equals(curr_param.get_id().get_name())) '{'\n", fieldInfo.mDisplayName));

			aSb.append("\t\t\t\t\t\tif (curr_param.get_type() != Module_Parameter.type_t.MP_NotUsed) {\n");
			aSb.append(MessageFormat.format("\t\t\t\t\t\t\tget_field_{0}().set_param(curr_param);\n", fieldInfo.mJavaVarName));
			aSb.append("\t\t\t\t\t\t}\n");
			aSb.append("\t\t\t\t\t\tvalue_used[val_idx] = true;\n");
			aSb.append("\t\t\t\t\t}\n");
			aSb.append("\t\t\t\t}\n");
		}

		aSb.append("\t\t\t\tfor (int val_idx = 0; val_idx < param.get_size(); val_idx++) {\n");
		aSb.append("\t\t\t\t\tif (!value_used[val_idx]) {\n");
		aSb.append("\t\t\t\t\t\tfinal Module_Parameter curr_param = param.get_elem(val_idx);\n");
		aSb.append(MessageFormat.format("\t\t\t\t\t\tcurr_param.error(MessageFormat.format(\"Non existent field name in type {0}: '{'0'}'\", curr_param.get_id().get_name()));\n", classReadableName));
		aSb.append("\t\t\t\t\t\tbreak;\n");
		aSb.append("\t\t\t\t\t}\n");
		aSb.append("\t\t\t\t}\n");
		aSb.append("\t\t\t\tbreak;\n");
		aSb.append("\t\t\t}\n");
		aSb.append("\t\t\tdefault:\n");
		aSb.append(MessageFormat.format("\t\t\t\tparam.type_error(\"{0} value\", \"{1}\");\n", isSet ? "set" : "record", classReadableName));
		aSb.append("\t\t\t}\n");
		aSb.append("\t\t}\n\n");
	}

	/**
	 * Generate set_implicit_omit.
	 *
	 * @param aSb
	 *                the output, where the java code is written
	 * @param aNamesList
	 *                sequence field variable and type names
	 */
	private static void generateValueSetImplicitOmit(final StringBuilder aSb, final List<FieldInfo> aNamesList) {
		aSb.append("\t\t@Override\n");
		aSb.append("\t\tpublic void set_implicit_omit() {\n");

		for (int i = 0 ; i < aNamesList.size(); i++) {
			final FieldInfo fieldInfo = aNamesList.get(i);

			if (fieldInfo.isOptional) {
				aSb.append( MessageFormat.format( "\t\t\tif ({0}.is_bound()) '{'\n", fieldInfo.mVarName ) );
				aSb.append( MessageFormat.format( "\t\t\t\t{0}.set_implicit_omit();\n", fieldInfo.mVarName ) );
				aSb.append("\t\t\t} else {\n");
				aSb.append( MessageFormat.format( "\t\t\t\t{0}.operator_assign(template_sel.OMIT_VALUE);\n", fieldInfo.mVarName ) );
				aSb.append("\t\t\t}\n");
			} else {
				aSb.append( MessageFormat.format( "\t\t\tif ({0}.is_bound()) '{'\n", fieldInfo.mVarName ) );
				aSb.append( MessageFormat.format( "\t\t\t\t{0}.set_implicit_omit();\n", fieldInfo.mVarName ) );
				aSb.append("\t\t\t}\n");
			}
		}
		aSb.append("\t\t}\n\n");
	}

	/**
	 * Generating encode_text/decode_text
	 *
	 * @param aSb
	 *                the output, where the java code is written
	 * @param aNamesList
	 *                sequence field variable and type names
	 */
	private static void generateValueEncodeDecodeText(final StringBuilder aSb, final List<FieldInfo> aNamesList) {
		aSb.append("\t\t@Override\n");
		aSb.append("\t\tpublic void encode_text(final Text_Buf text_buf) {\n");
		for (int i = 0 ; i < aNamesList.size(); i++) {
			final FieldInfo fieldInfo = aNamesList.get(i);

			aSb.append(MessageFormat.format("\t\t\t{0}.encode_text(text_buf);\n", fieldInfo.mVarName));
		}
		aSb.append("\t\t}\n\n");

		aSb.append("\t\t@Override\n");
		aSb.append("\t\tpublic void decode_text(final Text_Buf text_buf) {\n");
		for (int i = 0 ; i < aNamesList.size(); i++) {
			final FieldInfo fieldInfo = aNamesList.get(i);

			aSb.append(MessageFormat.format("\t\t\t{0}.decode_text(text_buf);\n", fieldInfo.mVarName));
		}
		aSb.append("\t\t}\n\n");
	}

	/**
	 * Generate encode/decode
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
	 * @param isSet
	 *                {@code true} if a set, {@code false} if a record
	 * @param rawNeeded
	 *                {@code true} if encoding/decoding for RAW is to be generated.
	 * @param raw
	 *                the raw coding related settings if applicable.
	 * */
	private static void generateValueEncodeDecode(final JavaGenData aData, final StringBuilder source, final String genName, final String displayName, final List<FieldInfo> fieldInfos, final boolean isSet, final boolean rawNeeded, final RawASTStruct raw) {
		source.append("\t\t@Override\n");
		source.append("\t\tpublic void encode(final TTCN_Typedescriptor p_td, final TTCN_Buffer p_buf, final coding_type p_coding, final int flavour) {\n");
		source.append("\t\t\tswitch (p_coding) {\n");
		source.append("\t\t\tcase CT_RAW: {\n");
		source.append("\t\t\t\tfinal TTCN_EncDec_ErrorContext errorContext = new TTCN_EncDec_ErrorContext(\"While RAW-encoding type '%s': \", p_td.name);\n");
		source.append("\t\t\t\tif (p_td.raw == null) {\n");
		source.append("\t\t\t\t\tTTCN_EncDec_ErrorContext.error_internal(\"No RAW descriptor available for type '%s'.\", p_td.name);\n");
		source.append("\t\t\t\t}\n");
		source.append("\t\t\t\tfinal RAW_enc_tr_pos tree_position = new RAW_enc_tr_pos(0, null);\n");
		source.append("\t\t\t\tfinal RAW_enc_tree root = new RAW_enc_tree(false, null, tree_position, 1, p_td.raw);\n");
		source.append("\t\t\t\tRAW_encode(p_td, root);\n");
		source.append("\t\t\t\troot.put_to_buf(p_buf);\n");
		source.append("\t\t\t\terrorContext.leave_context();\n");
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
		source.append("\t\t\t\tif (p_td.raw == null) {\n");
		source.append("\t\t\t\t\tTTCN_EncDec_ErrorContext.error_internal(\"No RAW descriptor available for type '%s'.\", p_td.name);\n");
		source.append("\t\t\t\t}\n");
		source.append("\t\t\t\traw_order_t order;\n");
		source.append("\t\t\t\tswitch (p_td.raw.top_bit_order) {\n");
		source.append("\t\t\t\tcase TOP_BIT_LEFT:\n");
		source.append("\t\t\t\t\torder = raw_order_t.ORDER_LSB;\n");
		source.append("\t\t\t\t\tbreak;\n");
		source.append("\t\t\t\tcase TOP_BIT_RIGHT:\n");
		source.append("\t\t\t\tdefault:\n");
		source.append("\t\t\t\t\torder = raw_order_t.ORDER_MSB;\n");
		source.append("\t\t\t\t\tbreak;\n");
		source.append("\t\t\t\t}\n");
		source.append("\t\t\t\tfinal int rawr = RAW_decode(p_td, p_buf, p_buf.get_len() * 8, order);\n");
		source.append("\t\t\t\tif (rawr < 0) {\n");
		source.append("\t\t\t\t\tfinal error_type temp = error_type.values()[-rawr];\n");
		source.append("\t\t\t\t\tswitch (temp) {\n");
		source.append("\t\t\t\t\tcase ET_INCOMPL_MSG:\n");
		source.append("\t\t\t\t\tcase ET_LEN_ERR:\n");
		source.append("\t\t\t\t\t\tTTCN_EncDec_ErrorContext.error(temp, \"Can not decode type '%s', because invalid or incomplete message was received\", p_td.name);\n");
		source.append("\t\t\t\t\t\tbreak;\n");
		source.append("\t\t\t\t\tcase ET_UNBOUND:\n");
		source.append("\t\t\t\t\tdefault:\n");
		source.append("\t\t\t\t\t\tTTCN_EncDec_ErrorContext.error(error_type.ET_INVAL_MSG, \"Can not decode type '%s', because invalid or incomplete message was received\", p_td.name);\n");
		source.append("\t\t\t\t\t\tbreak;\n");
		source.append("\t\t\t\t\t}\n");
		source.append("\t\t\t\t}\n");
		source.append("\t\t\t\terrorContext.leave_context();\n");
		source.append("\t\t\t\tbreak;\n");
		source.append("\t\t\t}\n");
		source.append("\t\t\tdefault:\n");
		source.append("\t\t\t\tthrow new TtcnError(MessageFormat.format(\"Unknown coding method requested to decode type `{0}''\", p_td.name));\n");
		source.append("\t\t\t}\n");
		source.append("\t\t}\n\n");

		if (rawNeeded) {
			final ArrayList<raw_option_struct> raw_options = new ArrayList<RecordSetCodeGenerator.raw_option_struct>(fieldInfos.size());
			final AtomicBoolean hasLengthto = new AtomicBoolean();
			final AtomicBoolean hasPointer = new AtomicBoolean();
			final AtomicBoolean hasCrosstag = new AtomicBoolean();
			final AtomicBoolean has_ext_bit = new AtomicBoolean();
			set_raw_options(isSet, fieldInfos, raw != null, raw, raw_options, hasLengthto, hasPointer, hasCrosstag, has_ext_bit);

			source.append("@Override\n");
			source.append("/** {@inheritDoc} */\n");
			source.append("public int RAW_encode(final TTCN_Typedescriptor p_td, final RAW_enc_tree myleaf) {\n");
			source.append("if (!is_bound()) {\n");
			source.append("TTCN_EncDec_ErrorContext.error(error_type.ET_UNBOUND, \"Encoding an unbound value.\", \"\");\n");
			source.append("}\n");

			source.append("int encoded_length = 0;\n");
			source.append("myleaf.isleaf = false;\n");
			source.append(MessageFormat.format("myleaf.num_of_nodes = {0};\n", fieldInfos.size()));
			source.append(MessageFormat.format("myleaf.nodes = new RAW_enc_tree[{0}];\n", fieldInfos.size()));
			for (int i = 0 ; i < fieldInfos.size(); i++) {
				final FieldInfo fieldInfo = fieldInfos.get(i);
	
				if (fieldInfo.isOptional) {
					source.append(MessageFormat.format("if ({0}.is_present()) '{'\n", fieldInfo.mVarName));
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
					source.append(MessageFormat.format("if ({0}.is_present()) '{'\n", fieldInfo.mVarName));
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
							source.append(MessageFormat.format("if ({0}.is_present()) '{'\n", fieldInfos.get(fieldInfo.raw.lengthto.get(a)).mVarName));
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
						source.append(MessageFormat.format("if ({0}.is_present()) '{'\n", fieldInfos.get(fieldInfo.raw.pointerto).mVarName));
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
						source.append(MessageFormat.format("if ({0}.is_present()) '{'\n", fieldInfo.mVarName));
					}

					source.append(MessageFormat.format("if (myleaf.nodes[{0}].nodes[{1}] != null) '{'\n", i, fieldInfo.raw.lengthindex.nthfield));
					source.append(MessageFormat.format("myleaf.nodes[{0}].nodes[{1}].calc = calc_type.CALC_LENGTH;\n", i, fieldInfo.raw.lengthindex.nthfield));
					source.append(MessageFormat.format("myleaf.nodes[{0}].nodes[{1}].coding_descr = {2}_descr_;\n", i, fieldInfo.raw.lengthindex.nthfield, fieldInfo.raw.lengthindex.typedesc));

					final int lengthtoSize = fieldInfo.raw.lengthto == null ? 0 : fieldInfo.raw.lengthto.size();
					source.append(MessageFormat.format("myleaf.nodes[{0}].nodes[{1}].lengthto = new RAW_enc_lengthto({2}, new RAW_enc_tr_pos[{2}], {3}, {4});\n", i, fieldInfo.raw.lengthindex.nthfield, lengthtoSize, fieldInfo.raw.unit, fieldInfo.raw.lengthto_offset));
					for (int a = 0; a < lengthtoSize; a++) {
						if (fieldInfos.get(fieldInfo.raw.lengthto.get(a)).isOptional) {
							source.append(MessageFormat.format("if ({0}.is_present()) '{'\n", fieldInfos.get(fieldInfo.raw.lengthto.get(a)).mVarName));
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
						source.append(MessageFormat.format("if ({0}.is_present()) ", fieldInfo.mVarName));
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
							source.append(MessageFormat.format("if ({0}.is_present()) '{'\n", fieldInfos.get(fieldInfo.raw.lengthto.get(a)).mVarName));
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
						source.append(MessageFormat.format("{0}.is_present() && (", fieldInfo.mVarName));
					}
					genRawFieldChecker(source, cur_choice, false);
					if (fieldInfo.isOptional) {
						source.append(')');
					}
					source.append(") {\n");
					genRawTagChecker(source, cur_choice);
					source.append("}\n");
				}
				final int presenceLength = fieldInfo.raw == null || fieldInfo.raw.presence == null || fieldInfo.raw.presence.fields == null ? 0 : fieldInfo.raw.presence.fields.size();
				if (fieldInfo.hasRaw && presenceLength > 0) {
					source.append("if (");
					if (fieldInfo.isOptional) {
						source.append(MessageFormat.format("{0}.is_present() && (", fieldInfo.mVarName));
					}
					genRawFieldChecker(source, fieldInfo.raw.presence, false);
					if (fieldInfo.isOptional) {
						source.append(')');
					}
					source.append(") {\n");
					genRawTagChecker(source, fieldInfo.raw.presence);
					source.append("}\n");
				}
				final int crosstagLength = fieldInfo.raw == null || fieldInfo.raw.crosstaglist == null || fieldInfo.raw.crosstaglist.list == null ? 0 : fieldInfo.raw.crosstaglist.list.size();
				if (fieldInfo.hasRaw && crosstagLength > 0) {
					if (fieldInfo.isOptional) {
						source.append(MessageFormat.format("if ({0}.is_present()) '{'\n", fieldInfo.mVarName));
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
								source.append(MessageFormat.format("encoded_length -= myleaf.nodes[{0}].length;\n", cur_choice.fields.get(0).fields.get(0).nthfield));
								source.append(MessageFormat.format("myleaf.nodes[{0}] = null;\n", cur_choice.fields.get(0).fields.get(0).nthfield));
								
							} else {
								source.append(MessageFormat.format("final RAW_enc_tr_pos pr_pos = new RAW_enc_tr_pos(myleaf.curr_pos.level + {0}, new int[] '{'", cur_choice.fields.get(0).fields.size()));
								for (int ll = 0 ; ll < cur_choice.fields.get(0).fields.size(); ll++) {
									if (ll > 0) {
										source.append(',');
									}
									source.append(cur_choice.fields.get(0).fields.get(ll).nthfield);
								}
								source.append("});\n");
								source.append("final RAW_enc_tree temp_leaf = myleaf.get_node(pr_pos);\n");
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
			source.append("/** {@inheritDoc} */\n");
			source.append("public int RAW_decode(final TTCN_Typedescriptor p_td, final TTCN_Buffer buff, int limit, final raw_order_t top_bit_ord, final boolean no_err, final int sel_field, final boolean first_call, final RAW_Force_Omit force_omit) {\n");
			if (isSet) {
				int mand_num = 0;
				for (int i = 0; i < fieldInfos.size(); i++) {
					if (!fieldInfos.get(i).isOptional) {
						mand_num++;
					}
				}
				source.append("final int prepaddlength = buff.increase_pos_padd(p_td.raw.prepadding);\n");
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
						source.append(MessageFormat.format("{0}.operator_assign(template_sel.OMIT_VALUE);\n", fieldInfo.mVarName));
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
				source.append("final int fl_start_pos = buff.get_pos_bit();\n");
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

						source.append(MessageFormat.format("if (field_map[{0}] == 0", i));
						if (fieldInfo.isOptional) {
							source.append(MessageFormat.format(" && (force_omit == null || !force_omit.shouldOmit({0}))", i));
						}
						source.append(") {\n");
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
								source.append(MessageFormat.format("final {0} temporal_{1} = new {0}();\n", cur_field_list.fields.get(cur_field_list.fields.size() - 1).type, j));
								source.append(MessageFormat.format("buff.set_pos_bit(fl_start_pos + {0});\n", cur_field_list.start_pos));
								source.append(MessageFormat.format("temporal_decoded_length = temporal_{0}.RAW_decode({1}_descr_, buff, limit, temporal_top_order, true, -1, true, null);\n", j, cur_field_list.fields.get(cur_field_list.fields.size() - 1).typedesc));
								source.append("buff.set_pos_bit(fl_start_pos);\n");
								source.append(MessageFormat.format("if (temporal_decoded_length > 0 && temporal_{0}.operator_equals({1})) '{'\n", j, cur_field_list.expression.expression));
								source.append(MessageFormat.format("final RAW_Force_Omit field_{0}_force_omit = new RAW_Force_Omit({0}, force_omit, {1}_descr_.raw.forceomit);\n", i, fieldInfo.mTypeDescriptorName));
								source.append(MessageFormat.format("final int decoded_field_length = {0}{1}.RAW_decode({2}_descr_, buff, limit, local_top_order, true, -1, true, field_{3}_force_omit);\n", fieldInfo.mVarName, fieldInfo.isOptional? ".get()" : "", fieldInfo.mTypeDescriptorName, i));
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
									source.append(MessageFormat.format("{0}.operator_assign(template_sel.OMIT_VALUE);\n", fieldInfo.mVarName));
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
							source.append(MessageFormat.format("final RAW_Force_Omit field_{0}_force_omit = new RAW_Force_Omit({0}, force_omit, {1}_descr_.raw.forceomit);\n", i, fieldInfo.mTypeDescriptorName));
							source.append(MessageFormat.format("final int decoded_field_length = {0}{1}.RAW_decode({2}_descr_, buff, limit, local_top_order, true, -1, true, field_{3}_force_omit);\n", fieldInfo.mVarName, fieldInfo.isOptional? ".get()" : "", fieldInfo.mTypeDescriptorName, 3));
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
								source.append(MessageFormat.format("{0}.operator_assign(template_sel.OMIT_VALUE);\n", fieldInfo.mVarName));
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
							if (fieldInfo.isOptional) {
								source.append(MessageFormat.format("if (force_omit == null || !force_omit.shouldOmit({0}))", i));
							}
						} else {
							repeatable = false;
							source.append(MessageFormat.format("if (field_map[{0}] == 0 ", i));
							if (fieldInfo.isOptional) {
								source.append(MessageFormat.format("&& (force_omit == null || !force_omit.shouldOmit({0}))", i));
							}
							source.append(')');
						}

						source.append("{\n");
						source.append(MessageFormat.format("final RAW_Force_Omit field_{0}_force_omit = new RAW_Force_Omit({0}, force_omit, {1}_descr_.raw.forceomit);\n", i, fieldInfo.mTypeDescriptorName));
						source.append(MessageFormat.format("final int decoded_field_length = {0}{1}.RAW_decode({2}_descr_, buff, limit, local_top_order, true, -1, ", fieldInfo.mVarName, fieldInfo.isOptional ? ".get()":"", fieldInfo.mTypeDescriptorName));
						if (repeatable) {
							source.append(MessageFormat.format("field_map[{0}] == 0", i));
						} else {
							source.append("true");
						}
						source.append(MessageFormat.format(", field_{0}_force_omit);\n", i));

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
							source.append(MessageFormat.format("{0}.operator_assign(template_sel.OMIT_VALUE);\n", fieldInfo.mVarName));
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
						source.append(MessageFormat.format("if (field_map[{0}] == 0 ", i));
						if (fieldInfo.isOptional) {
							source.append(MessageFormat.format("&& (force_omit == null || !force_omit.shouldOmit({0}))", i));
						}
						source.append(") {\n");
						source.append(MessageFormat.format("final RAW_Force_Omit field_{0}_force_omit = new RAW_Force_Omit({0}, force_omit, {1}_descr_.raw.forceomit);\n", i, fieldInfo.mTypeDescriptorName));
						source.append(MessageFormat.format("final int decoded_field_length = {0}{1}.RAW_decode({2}_descr_, buff, limit, top_bit_ord, true, -1, true, field_{3}_force_omit);\n", fieldInfo.mVarName, fieldInfo.isOptional ? ".get()":"", fieldInfo.mTypeDescriptorName, i));
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
							source.append(MessageFormat.format("{0}.operator_assign(template_sel.OMIT_VALUE);\n", fieldInfo.mVarName));
						}

						source.append("}\n");
						source.append("}\n");
					}
				}

				source.append("break;\n");
				source.append("}\n");

				if (mand_num > 0) {
					source.append(MessageFormat.format("if (nof_mand_fields != {0}) '{'\n", mand_num));
					source.append("return limit > 0 ? -1 : -TTCN_EncDec.error_type.ET_INCOMPL_MSG.ordinal();\n");
					source.append("}\n");
				}
				source.append("return decoded_length + prepaddlength + buff.increase_pos_padd(p_td.raw.padding);\n");
			} else {
				source.append("final int prepaddlength = buff.increase_pos_padd(p_td.raw.prepadding);\n");
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
					source.append("final int end_of_available_data = last_decoded_pos + limit;\n");
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
	 * Generating is_bound() function for template
	 * 
	 * @param aSb
	 *                the output, where the java code is written
	 * @param aNamesList
	 *                sequence field variable and type names
	 */
	private static void generateTemplateIsBound( final StringBuilder aSb, final List<FieldInfo> aNamesList ) {
		aSb.append( "\n\t\t@Override\n");
		aSb.append( "\t\tpublic boolean is_bound() {\n" );
		aSb.append( "\t\t\tif (template_selection == template_sel.UNINITIALIZED_TEMPLATE && !is_ifPresent) {\n"
				+ "\t\t\t\treturn false;\n"
				+ "\t\t\t}\n" );
		aSb.append( "\t\t\tif (template_selection != template_sel.SPECIFIC_VALUE) {\n"
				+ "\t\t\t\treturn true;\n"
				+ "\t\t\t}\n" );

		aSb.append( "\t\t\treturn " );
		for (int i = 0; i < aNamesList.size(); i++) {
			final FieldInfo fi = aNamesList.get(i);

			if (i != 0) {
				aSb.append( "\n\t\t\t\t\t|| " );
			}
			if (fi.isOptional) {
				aSb.append( MessageFormat.format( "{0}.is_omit() || {0}.is_bound()", fi.mVarName ) );
			} else {
				aSb.append( MessageFormat.format( "{0}.is_bound()", fi.mVarName ) );
			}
		}
		aSb.append(";\n");
		aSb.append("\t\t}\n");
	}

	/**
	 * Generating is_value() function for template
	 * 
	 * @param aSb
	 *                the output, where the java code is written
	 * @param aNamesList
	 *                sequence field variable and type names
	 */
	private static void generateTemplateIsValue( final StringBuilder aSb, final List<FieldInfo> aNamesList ) {
		aSb.append( "\n\t\t@Override\n");
		aSb.append( "\t\tpublic boolean is_value() {\n" );
		aSb.append( "\t\t\tif (template_selection != template_sel.SPECIFIC_VALUE || is_ifPresent) {\n"
				+ "\t\t\t\treturn false;\n"
				+ "\t\t\t}\n" );

		aSb.append( "\t\t\treturn " );
		for (int i = 0; i < aNamesList.size(); i++) {
			final FieldInfo fi = aNamesList.get(i);

			if (i != 0) {
				aSb.append( "\n\t\t\t\t\t&& " );
			}
			if (fi.isOptional) {
				aSb.append( MessageFormat.format( "({0}.is_omit() || {0}.is_value())", fi.mVarName ) );
			} else {
				aSb.append( MessageFormat.format( "{0}.is_value()", fi.mVarName ) );
			}
		}
		aSb.append(";\n");
		aSb.append("\t\t}\n");
	}

	/**
	 * Generating operator_equals() function
	 *
	 * @param aData
	 *                only used to update imports if needed
	 * @param aSb
	 *                the output, where the java code is written
	 * @param aNamesList
	 *                sequence field variable and type names
	 * @param aClassName
	 *                the class name of the record/set class
	 * @param classReadableName
	 *                the readable name of the class
	 */
	private static void generateoperator_equals( final JavaGenData aData, final StringBuilder aSb, final List<FieldInfo> aNamesList,
			final String aClassName, final String classReadableName ) {
		aSb.append( '\n' );
		if (aData.isDebug()) {
			aSb.append("\t\t/**\n");
			aSb.append("\t\t * Checks if the current value is equivalent to the provided one.\n");
			aSb.append("\t\t *\n");
			aSb.append("\t\t * operator== in the core\n");
			aSb.append("\t\t *\n");
			aSb.append("\t\t * @param otherValue\n");
			aSb.append("\t\t *                the other value to check against.\n");
			aSb.append("\t\t * @return {@code true} if all fields are equivalent, {@code false} otherwise.\n");
			aSb.append("\t\t */\n");
		}
		aSb.append( MessageFormat.format( "\t\tpublic boolean operator_equals( final {0} otherValue) '{'\n", aClassName ) );
		aSb.append( "\t\t\treturn " );
		for (int i = 0; i < aNamesList.size(); i++) {
			final FieldInfo fi = aNamesList.get(i);

			if (i != 0) {
				aSb.append( "\n\t\t\t\t\t&& " );
			}

			aSb.append( MessageFormat.format( "this.{0}.operator_equals( otherValue.{0} )", fi.mVarName ) );
		}
		aSb.append(";\n");
		aSb.append("\t\t}\n");

		aSb.append('\n');
		aSb.append("\t\t@Override\n");
		aSb.append("\t\tpublic boolean operator_equals(final Base_Type otherValue) {\n");
		aSb.append("\t\t\tif (otherValue instanceof ").append(aClassName).append(" ) {\n");
		aSb.append("\t\t\t\treturn operator_equals((").append( aClassName ).append(") otherValue);\n");
		aSb.append("\t\t\t}\n\n");
		aSb.append("\t\t\tthrow new TtcnError(MessageFormat.format(\"Internal Error: value `{0}'' can not be cast to ").append(classReadableName).append("\", otherValue));\n");
		aSb.append("\t\t}\n\n");
	}

	/**
	 * Generating getters/setters for the member variables
	 *
	 * @param aData
	 *                only used to update imports if needed
	 * @param aSb
	 *                the output, where the java code is written
	 * @param aNamesList
	 *                sequence field variable and type names
	 */
	private static void generateGettersSetters( final JavaGenData aData, final StringBuilder aSb, final List<FieldInfo> aNamesList ) {
		for ( final FieldInfo fi : aNamesList ) {
			if (aData.isDebug()) {
				aSb.append("\t\t/**\n");
				aSb.append(MessageFormat.format("\t\t * Gives access to the field {0}.\n", fi.mDisplayName));
				aSb.append("\t\t *\n");
				aSb.append(MessageFormat.format("\t\t * @return the field {0}.\n", fi.mDisplayName));
				aSb.append("\t\t * */\n");
			}
			aSb.append( "\t\tpublic " );
			if (fi.isOptional) {
				aSb.append("Optional<");
				aSb.append( fi.mJavaTypeName );
				aSb.append('>');
			} else {
				aSb.append( fi.mJavaTypeName );
			}
			aSb.append( " get_field_" );
			aSb.append( fi.mJavaVarName );
			aSb.append( "() {\n" +
					"\t\t\treturn " );
			aSb.append( fi.mVarName );
			aSb.append( ";\n" +
					"\t\t}\n\n" );

			if (aData.isDebug()) {
				aSb.append("\t\t/**\n");
				aSb.append(MessageFormat.format("\t\t * Gives read-only access to the field {0}.\n", fi.mDisplayName));
				aSb.append("\t\t *\n");
				aSb.append(MessageFormat.format("\t\t * @return the field {0}.\n", fi.mDisplayName));
				aSb.append("\t\t * */\n");
			}
			aSb.append( "\t\tpublic " );
			if (fi.isOptional) {
				aSb.append("Optional<");
				aSb.append( fi.mJavaTypeName );
				aSb.append('>');
			} else {
				aSb.append( fi.mJavaTypeName );
			}
			aSb.append( " constGet_field_" );
			aSb.append( fi.mJavaVarName );
			aSb.append( "() {\n" +
					"\t\t\treturn " );
			aSb.append( fi.mVarName );
			aSb.append( ";\n" +
					"\t\t}\n\n" );
		}
	}

	/**
	 * Generate member variables for template
	 *
	 * @param aData
	 *                used to access build settings.
	 * @param source
	 *                the source to be updated
	 * @param aNamesList
	 *                sequence field variable and type names
	 * @param className
	 *                the name of the generated class representing the
	 *                record/set type.
	 */
	private static void generateTemplateDeclaration( final JavaGenData aData, final StringBuilder source, final List<FieldInfo> aNamesList,
			final String className ) {
		for ( final FieldInfo fi : aNamesList ) {
			source.append( "\t\tprivate " );
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

		source.append("\t\t//originally value_list/list_value\n");
		source.append( MessageFormat.format( "\t\tprivate List<{0}_template> list_value;\n\n", className ) );
	}

	/**
	 * Generate getters for template
	 *
	 * @param aData
	 *                used to access build settings.
	 * @param source
	 *                the source to be updated
	 * @param aNamesList
	 *                sequence field variable and type names
	 * @param displayName
	 *                the user readable name of the type to be generated.
	 */
	private static void generateTemplateGetter( final JavaGenData aData, final StringBuilder source, final List<FieldInfo> aNamesList,
			final String displayName ) {
		for ( final FieldInfo fi : aNamesList ) {
			if (aData.isDebug()) {
				source.append("\t\t/**\n");
				source.append(MessageFormat.format("\t\t * Gives access to the field {0}.\n", fi.mDisplayName));
				source.append("\t\t * Turning the template into a specific value template if needed.\n");
				source.append("\t\t *\n");
				source.append(MessageFormat.format("\t\t * @return the field {0}.\n", fi.mDisplayName));
				source.append("\t\t * */\n");
			}
			source.append( MessageFormat.format( "\t\tpublic {0}_template get_field_{1}() '{'\n", fi.mJavaTypeName, fi.mJavaVarName ) );
			source.append("\t\t\tset_specific();\n");
			source.append( MessageFormat.format( "\t\t\treturn {0};\n", fi.mVarName ) );
			source.append("\t\t}\n\n");

			if (aData.isDebug()) {
				source.append("\t\t/**\n");
				source.append(MessageFormat.format("\t\t * Gives read-only access to the field {0}.\n", fi.mDisplayName));
				source.append("\t\t * Being called on a non specific value template causes dynamic test case error.\n");
				source.append("\t\t *\n");
				source.append(MessageFormat.format("\t\t * @return the field {0}.\n", fi.mDisplayName));
				source.append("\t\t * */\n");
			}
			source.append( MessageFormat.format( "\t\tpublic {0}_template constGet_field_{1}() '{'\n", fi.mJavaTypeName, fi.mJavaVarName ) );
			source.append("\t\t\tif (template_selection != template_sel.SPECIFIC_VALUE) {\n");
			source.append( MessageFormat.format( "\t\t\t\tthrow new TtcnError(\"Accessing field {0} of a non-specific template of type {1}.\");\n", fi.mDisplayName, displayName ) );
			source.append("\t\t\t}\n");
			source.append( MessageFormat.format( "\t\t\treturn {0};\n", fi.mVarName ) );
			source.append("\t\t}\n\n");
		}

		source.append("\t\tprivate void set_specific() {\n");
		source.append("\t\t\tif (template_selection != template_sel.SPECIFIC_VALUE) {\n");
		source.append("\t\t\t\tfinal template_sel old_selection = template_selection;\n");
		source.append("\t\t\t\tclean_up();\n");
		source.append("\t\t\t\tset_selection(template_sel.SPECIFIC_VALUE);\n");
		
		source.append("\t\t\t\tif (old_selection == template_sel.ANY_VALUE || old_selection == template_sel.ANY_OR_OMIT) {\n");
		for ( final FieldInfo fi : aNamesList ) {
			if (fi.isOptional) {
				source.append( MessageFormat.format( "\t\t\t\t\t{0} = new {1}(template_sel.ANY_OR_OMIT);\n", fi.mVarName, fi.mJavaTemplateTypeName ) );
			} else {
				source.append( MessageFormat.format( "\t\t\t\t\t{0} = new {1}(template_sel.ANY_VALUE);\n", fi.mVarName, fi.mJavaTemplateTypeName ) );
			}
		}
		source.append("\t\t\t\t} else {\n");
		for ( final FieldInfo fi : aNamesList ) {
			source.append( MessageFormat.format( "\t\t\t\t\t{0} = new {1}();\n", fi.mVarName, fi.mJavaTemplateTypeName ) );
		}
		source.append("\t\t\t\t}\n");
		source.append("\t\t\t}\n");
		source.append("\t\t}\n");
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
	 *                record/set type.
	 * @param displayName
	 *                the user readable name of the type to be generated.
	 */
	private static void generateTemplateConstructors( final JavaGenData aData, final StringBuilder source, final String genName, final String displayName ) {
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
		source.append("\t\t\tcopy_value(otherValue);\n");
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
		source.append("\t\t}\n");

		source.append('\n');
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
	 *                record/set type.
	 * @param displayName
	 *                the user readable name of the type to be generated.
	 */
	private static void generateTemplateoperator_assign(final JavaGenData aData,  final StringBuilder source, final String genName, final String displayName ) {
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
		source.append("\t\t}\n");

		source.append('\n');
		source.append("\t\t@Override\n");
		source.append( MessageFormat.format("\t\tpublic {0}_template operator_assign(final Base_Type otherValue) '{'\n", genName));
		source.append( MessageFormat.format("\t\t\tif (otherValue instanceof {0}) '{'\n", genName));
		source.append( MessageFormat.format("\t\t\t\treturn operator_assign(({0}) otherValue);\n", genName));
		source.append("\t\t\t}\n\n");
		source.append( MessageFormat.format("\t\t\tthrow new TtcnError(MessageFormat.format(\"Internal Error: value `{0}'' can not be cast to {1}\", otherValue));\n", genName));
		source.append("\t\t}\n");

		source.append('\n');
		source.append("\t\t@Override\n");
		source.append( MessageFormat.format("\t\tpublic {0}_template operator_assign(final Base_Template otherValue) '{'\n", genName));
		source.append( MessageFormat.format("\t\t\tif (otherValue instanceof {0}_template) '{'\n", genName));
		source.append( MessageFormat.format("\t\t\t\treturn operator_assign(({0}_template) otherValue);\n", genName));
		source.append("\t\t\t}\n\n");
		source.append( MessageFormat.format("\t\t\tthrow new TtcnError(MessageFormat.format(\"Internal Error: value `{0}'' can not be cast to {1}_template\", otherValue));\n", genName));
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
		source.append( MessageFormat.format( "\t\tpublic {0}_template operator_assign( final Optional<{0}> otherValue ) '{'\n", genName ) );
		source.append("\t\t\tclean_up();\n");
		source.append("\t\t\tswitch (otherValue.get_selection()) {\n");
		source.append("\t\t\tcase OPTIONAL_PRESENT:\n");
		source.append("\t\t\t\tcopy_value(otherValue.constGet());\n");
		source.append("\t\t\t\tbreak;\n");
		source.append("\t\t\tcase OPTIONAL_OMIT:\n");
		source.append("\t\t\t\tset_selection(template_sel.OMIT_VALUE);\n");
		source.append("\t\t\t\tbreak;\n");
		source.append("\t\t\tdefault:\n");
		source.append( MessageFormat.format( "\t\t\t\tthrow new TtcnError(\"Assignment of an unbound optional field to a template of type {0}.\");\n", displayName ) );
		source.append("\t\t\t}\n");
		source.append("\t\t\treturn this;\n");
		source.append("\t\t}\n");
	}

	/**
	 * Generate the copyTemplate function for template
	 *
	 * @param source
	 *                where the source code is to be generated.
	 * @param aNamesList
	 *                sequence field variable and type names
	 * @param genName
	 *                the name of the generated class representing the
	 *                record/set type.
	 * @param displayName
	 *                the user readable name of the type to be generated.
	 */
	private static void generateTemplateCopyTemplate( final StringBuilder source, final List<FieldInfo> aNamesList, final String genName, final String displayName ) {
		source.append('\n');
		source.append( MessageFormat.format( "\t\tprivate void copy_value(final {0} other_value) '{'\n", genName));
		for ( final FieldInfo fi : aNamesList ) {
			source.append( MessageFormat.format( "\t\t\tif (other_value.get_field_{0}().is_bound()) '{'\n", fi.mJavaVarName ) );
			if ( fi.isOptional ) {
				source.append( MessageFormat.format( "\t\t\t\tif (other_value.get_field_{0}().ispresent()) '{'\n", fi.mJavaVarName ) );
				source.append( MessageFormat.format( "\t\t\t\t\tget_field_{0}().operator_assign(other_value.get_field_{0}().get());\n", fi.mJavaVarName ) );
				source.append("\t\t\t\t} else {\n");
				source.append( MessageFormat.format( "\t\t\t\t\tget_field_{0}().operator_assign(template_sel.OMIT_VALUE);\n", fi.mJavaVarName ) );
				source.append("\t\t\t\t}\n");
			} else {
				source.append( MessageFormat.format( "\t\t\t\tget_field_{0}().operator_assign(other_value.get_field_{0}());\n", fi.mJavaVarName ) );
			}
			source.append("\t\t\t} else {\n");
			source.append( MessageFormat.format( "\t\t\t\tget_field_{0}().clean_up();\n", fi.mJavaVarName ) );
			source.append("\t\t\t}\n");
		}
		source.append("\t\t\tset_selection(template_sel.SPECIFIC_VALUE);\n");
		source.append("\t\t}\n");

		source.append('\n');
		source.append( MessageFormat.format( "\t\tprivate void copy_template(final {0}_template other_value) '{'\n", genName));
		source.append("\t\t\tswitch (other_value.template_selection) {\n");
		source.append("\t\t\tcase SPECIFIC_VALUE:\n");
		for ( final FieldInfo fi : aNamesList ) {
			source.append( MessageFormat.format( "\t\t\t\tif (template_sel.UNINITIALIZED_TEMPLATE == other_value.get_field_{0}().get_selection()) '{'\n", fi.mJavaVarName ) );
			source.append( MessageFormat.format( "\t\t\t\t\tget_field_{0}().clean_up();\n", fi.mJavaVarName ) );
			source.append("\t\t\t\t} else {\n");
			source.append( MessageFormat.format( "\t\t\t\t\tget_field_{0}().operator_assign(other_value.get_field_{0}());\n", fi.mJavaVarName ) );
			source.append("\t\t\t\t}\n");
		}
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
		source.append("\t\t\tdefault:\n");
		source.append( MessageFormat.format( "\t\t\t\tthrow new TtcnError(\"Copying an uninitialized template of type {0}.\");\n", displayName));
		source.append("\t\t\t}\n");
		source.append("\t\t\tset_selection(other_value);\n");
		source.append("\t\t}\n");
	}

	/**
	 * Generating is_present() function for template
	 * 
	 * @param aSb
	 *                the output, where the java code is written
	 */
	private static void generateTemplateIsPresent( final StringBuilder aSb ) {
		aSb.append('\n');
		aSb.append("\t\t@Override\n");
		aSb.append("\t\tpublic boolean is_present(final boolean legacy) {\n");
		aSb.append("\t\t\treturn is_present_(legacy);\n");
		aSb.append("\t\t}\n");

		aSb.append('\n');
		aSb.append("\t\tprivate boolean is_present_(final boolean legacy) {\n");
		aSb.append("\t\t\tif (template_selection==template_sel.UNINITIALIZED_TEMPLATE) {\n");
		aSb.append("\t\t\t\treturn false;\n");
		aSb.append("\t\t\t}\n");
		aSb.append("\t\t\treturn !match_omit_(legacy);\n");
		aSb.append("\t\t}\n");

		aSb.append('\n');
		aSb.append("\t\t@Override\n");
		aSb.append("\t\tpublic boolean match_omit(final boolean legacy) {\n");
		aSb.append("\t\t\treturn match_omit_(legacy);\n");
		aSb.append("\t\t}\n");

		aSb.append('\n');
		aSb.append("\t\tprivate boolean match_omit_(final boolean legacy) {\n");
		aSb.append("\t\t\tif (is_ifPresent) {\n");
		aSb.append("\t\t\t\treturn true;\n");
		aSb.append("\t\t\t}\n");
		aSb.append("\t\t\tswitch (template_selection) {\n");
		aSb.append("\t\t\tcase OMIT_VALUE:\n");
		aSb.append("\t\t\tcase ANY_OR_OMIT:\n");
		aSb.append("\t\t\t\treturn true;\n");
		aSb.append("\t\t\tcase VALUE_LIST:\n");
		aSb.append("\t\t\tcase COMPLEMENTED_LIST:\n");
		aSb.append("\t\t\t\tif (legacy) {\n");
		aSb.append("\t\t\t\t\tfor (int l_idx=0; l_idx<list_value.size(); l_idx++) {\n");
		aSb.append("\t\t\t\t\t\tif (list_value.get(l_idx).match_omit_(legacy)) {\n");
		aSb.append("\t\t\t\t\t\t\treturn template_selection==template_sel.VALUE_LIST;\n");
		aSb.append("\t\t\t\t\t\t}\n");
		aSb.append("\t\t\t\t\t}\n");
		aSb.append("\t\t\t\t\treturn template_selection==template_sel.COMPLEMENTED_LIST;\n");
		aSb.append("\t\t\t\t} // else fall through\n");
		aSb.append("\t\t\tdefault:\n");
		aSb.append("\t\t\t\treturn false;\n");
		aSb.append("\t\t\t}\n");
		aSb.append("\t\t}\n");
	}

	/**
	 * Generating valueof() function for template
	 * 
	 * @param aSb
	 *                the output, where the java code is written
	 * @param aNamesList
	 *                sequence field variable and type names
	 * @param genName
	 *                the name of the generated class representing the
	 *                record/set type.
	 * @param displayName
	 *                the user readable name of the type to be generated.
	 */
	private static void generateTemplateValueOf( final StringBuilder aSb, final List<FieldInfo> aNamesList, final String genName, final String displayName ) {
		aSb.append('\n');
		aSb.append("\t\t@Override\n");
		aSb.append( MessageFormat.format( "\t\tpublic {0} valueof() '{'\n", genName ) );
		aSb.append("\t\t\tif (template_selection != template_sel.SPECIFIC_VALUE || is_ifPresent) {\n");
		aSb.append( MessageFormat.format( "\t\t\t\tthrow new TtcnError(\"Performing a valueof or send operation on a non-specific template of type {0}.\");\n", displayName ) );
		aSb.append("\t\t\t}\n");
		aSb.append( MessageFormat.format( "\t\t\tfinal {0} ret_val = new {0}();\n", genName ) );
		for ( final FieldInfo fi : aNamesList ) {
			if (fi.isOptional) {
				aSb.append( MessageFormat.format( "\t\t\tif ({0}.is_omit()) '{'\n", fi.mVarName )  );
				aSb.append( MessageFormat.format( "\t\t\t\tret_val.get_field_{0}().operator_assign(template_sel.OMIT_VALUE);\n", fi.mJavaVarName ) );
				aSb.append("\t\t\t} else ");
			} else {
				aSb.append("\t\t\t");
			}
			aSb.append( MessageFormat.format( "if ({0}.is_bound()) '{'\n", fi.mVarName )  );
			aSb.append( MessageFormat.format( "\t\t\t\tret_val.get_field_{0}().operator_assign({0}.valueof());\n", fi.mVarName ) );
			aSb.append("\t\t\t}\n");
		}
		aSb.append("\t\t\treturn ret_val;\n");
		aSb.append("\t\t}\n\n");
	}

	/**
	 * Generating list_item() function for template
	 * 
	 * @param aSb
	 *                the output, where the java code is written
	 * @param genName
	 *                the name of the generated class representing the
	 *                record/set type.
	 * @param displayName
	 *                the user readable name of the type to be generated.
	 */
	private static void generateTemplateListItem( final StringBuilder aSb, final String genName, final String displayName ) {
		aSb.append('\n');
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
		aSb.append("\t\t}\n");
	}

	/**
	 * Generating set_type() function for template
	 * 
	 * @param aSb
	 *                the output, where the java code is written
	 * @param genName
	 *                the name of the generated class representing the
	 *                record/set type.
	 * @param displayName
	 *                the user readable name of the type to be generated.
	 */
	private static void generateTemplateSetType( final StringBuilder aSb, final String genName, final String displayName ) {
		aSb.append('\n');
		aSb.append("\t\t@Override\n");
		aSb.append("\t\tpublic void set_type(final template_sel template_type, final int list_length) {\n");
		aSb.append("\t\t\tif (template_type != template_sel.VALUE_LIST && template_type != template_sel.COMPLEMENTED_LIST) {\n");
		aSb.append( MessageFormat.format( "\t\t\t\tthrow new TtcnError(\"Setting an invalid list for a template of type {0}.\");\n", displayName ) );
		aSb.append("\t\t\t}\n");
		aSb.append("\t\t\tclean_up();\n");
		aSb.append("\t\t\tset_selection(template_type);\n");
		aSb.append( MessageFormat.format( "\t\t\tlist_value = new ArrayList<{0}_template>(list_length);\n", genName ) );
		aSb.append("\t\t\tfor(int i = 0 ; i < list_length; i++) {\n");
		aSb.append(MessageFormat.format("\t\t\t\tlist_value.add(new {0}_template());\n", genName));
		aSb.append("\t\t\t}\n");
		aSb.append("\t\t}\n\n");
	}

	/**
	 * Generate the match function for template
	 *
	 * @param aData
	 *                used to access build settings.
	 * @param source
	 *                where the source code is to be generated.
	 * @param aNamesList
	 *                sequence field variable and type names
	 * @param genName
	 *                the name of the generated class representing the
	 *                record/set type.
	 * @param displayName
	 *                the user readable name of the type to be generated.
	 */
	private static void generateTemplateMatch( final JavaGenData aData, final StringBuilder source, final List<FieldInfo> aNamesList, final String genName, final String displayName ) {
		if (aData.isDebug()) {
			source.append("\t\t/**\n");
			source.append("\t\t * Matches the provided value against this template.\n");
			source.append("\t\t *\n");
			source.append("\t\t * @param other_value the value to be matched.\n");
			source.append("\t\t * */\n");
		}
		source.append( MessageFormat.format( "\t\tpublic boolean match(final {0} other_value) '{'\n", genName ) );
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
		source.append( MessageFormat.format( "\t\tpublic boolean match(final {0} other_value, final boolean legacy) '{'\n", genName ) );
		source.append("\t\t\tif (!other_value.is_bound()) {\n");
		source.append("\t\t\t\treturn false;\n");
		source.append("\t\t\t}\n");
		source.append("\t\t\tswitch (template_selection) {\n");
		source.append("\t\t\tcase ANY_VALUE:\n");
		source.append("\t\t\tcase ANY_OR_OMIT:\n");
		source.append("\t\t\t\treturn true;\n");
		source.append("\t\t\tcase OMIT_VALUE:\n");
		source.append("\t\t\t\treturn false;\n");
		source.append("\t\t\tcase SPECIFIC_VALUE:\n");
		for ( final FieldInfo fi : aNamesList ) {
			source.append( MessageFormat.format( "\t\t\t\tif(!other_value.get_field_{0}().is_bound()) '{'\n", fi.mJavaVarName )  );
			source.append("\t\t\t\t\treturn false;\n");
			source.append("\t\t\t\t}\n");
			if (fi.isOptional) {
				source.append( MessageFormat.format( "\t\t\t\tif((other_value.get_field_{0}().ispresent() ? !{1}.match(other_value.get_field_{0}().get(), legacy) : !{1}.match_omit(legacy))) '{'\n", fi.mJavaVarName, fi.mVarName ) );
			} else {
				source.append( MessageFormat.format( "\t\t\t\tif(!{1}.match(other_value.get_field_{0}(), legacy)) '{'\n", fi.mJavaVarName, fi.mVarName )  );
			}
			source.append("\t\t\t\t\treturn false;\n");
			source.append("\t\t\t\t}\n");
		}
		source.append("\t\t\t\treturn true;\n");
		source.append("\t\t\tcase VALUE_LIST:\n");
		source.append("\t\t\tcase COMPLEMENTED_LIST:\n");
		source.append("\t\t\t\tfor (int list_count = 0; list_count < list_value.size(); list_count++) {\n");
		source.append("\t\t\t\t\tif (list_value.get(list_count).match(other_value, legacy)) {\n");
		source.append("\t\t\t\t\t\treturn template_selection == template_sel.VALUE_LIST;\n");
		source.append("\t\t\t\t\t}\n");
		source.append("\t\t\t\t}\n");
		source.append("\t\t\t\treturn template_selection == template_sel.COMPLEMENTED_LIST;\n");
		source.append("\t\t\tdefault:\n");
		source.append( MessageFormat.format( "\t\t\t\tthrow new TtcnError(\"Matching an uninitialized/unsupported template of type {0}.\");\n", displayName ) );
		source.append("\t\t\t}\n");
		source.append("\t\t}\n\n");

		source.append('\n');
		source.append("\t\t@Override\n");
		source.append( MessageFormat.format( "\t\tpublic boolean match(final Base_Type otherValue, final boolean legacy) '{'\n", genName ) );
		source.append( MessageFormat.format( "\t\t\tif (otherValue instanceof {0}) '{'\n", genName) );
		source.append( MessageFormat.format( "\t\t\t\treturn match(({0})otherValue, legacy);\n", genName) );
		source.append("\t\t\t}\n\n");
		source.append( MessageFormat.format( "\t\t\tthrow new TtcnError(\"Internal Error: The left operand of assignment is not of type {0}.\");\n", genName ) );
		source.append("\t\t}\n\n");
	}

	/**
	 * Generating size_of() function
	 * 
	 * @param aData
	 *                used to access build settings.
	 * @param aSb
	 *                the output, where the java code is written
	 * @param aNamesList
	 *                sequence field variable and type names
	 * @param displayName
	 *                the user readable name of the type to be generated.
	 */
	private static void generateTemplateSizeOf( final JavaGenData aData, final StringBuilder aSb, final List<FieldInfo> aNamesList, final String displayName ) {
		if (aData.isDebug()) {
			aSb.append("\t\t/**\n");
			aSb.append("\t\t * Returns the size (number of fields).\n");
			aSb.append("\t\t *\n");
			aSb.append("\t\t * size_of in the core\n");
			aSb.append("\t\t *\n");
			aSb.append("\t\t * @return the size of the structure.\n");
			aSb.append("\t\t * */\n");
		}
		aSb.append( "\t\tpublic TitanInteger size_of() {\n" );
		aSb.append( "\t\t\tif (is_ifPresent) {\n" );
		aSb.append( MessageFormat.format( "\t\t\t\tthrow new TtcnError(\"Performing sizeof() operation on a template of type {0} which has an ifpresent attribute.\");\n", displayName ) );
		aSb.append( "\t\t\t}\n" );
		aSb.append( "\t\t\tswitch (template_selection) {\n" );
		aSb.append( "\t\t\tcase SPECIFIC_VALUE:\n" );
		//number of non-optional fields
		int size = 0;
		for ( final FieldInfo fi : aNamesList ) {
			if (!fi.isOptional) {
				size++;
			}
		}

		if (size == aNamesList.size()) {
			aSb.append( MessageFormat.format( "\t\t\t\treturn new TitanInteger({0});\n", size ) );
		} else {
			aSb.append( MessageFormat.format( "\t\t\t\tint sizeof = {0};\n", size ) );
			for ( final FieldInfo fi : aNamesList ) {
				if (fi.isOptional) {
					aSb.append( MessageFormat.format( "\t\t\t\tif ({0}.is_present()) '{'\n", fi.mVarName ) );
					aSb.append( "\t\t\t\t\tsizeof++;\n" );
					aSb.append( "\t\t\t\t}\n" );
				}
			}

			aSb.append( "\t\t\t\treturn new TitanInteger(sizeof);\n" );
		}

		aSb.append( "\t\t\tcase VALUE_LIST:\n" );
		aSb.append( "\t\t\t\tif (list_value.isEmpty()) {\n" );
		aSb.append( MessageFormat.format( "\t\t\t\t\tthrow new TtcnError(\"Internal error: Performing sizeof() operation on a template of type {0} containing an empty list.\");\n", displayName ) );
		aSb.append( "\t\t\t\t}\n" );
		aSb.append( "\t\t\t\tfinal int item_size = list_value.get(0).size_of().get_int();\n" );
		aSb.append( "\t\t\t\tfor (int l_idx = 1; l_idx < list_value.size(); l_idx++) {\n" );
		aSb.append( "\t\t\t\t\tif (list_value.get(l_idx).size_of().get_int() != item_size) {\n" );
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
	 *
	 * @param aData
	 *                used to access build settings.
	 * @param aSb
	 *                the output, where the java code is written
	 * @param aNamesList
	 *                sequence field variable and type names
	 * @param genName
	 *                the name of the generated class representing the
	 *                record/set type.
	 * @param displayName
	 *                the user readable name of the type to be generated.
	 */
	private static void generateTemplateLog(final JavaGenData aData, final StringBuilder source, final List<FieldInfo> aNamesList, final String genName, final String displayName) {
		source.append('\n');
		source.append("\t\t@Override\n");
		source.append("\t\tpublic void log() {\n");
		source.append("\t\t\tswitch (template_selection) {\n");
		source.append("\t\t\tcase SPECIFIC_VALUE:\n");
		source.append("\t\t\t\tTTCN_Logger.log_char('{');\n");
		for (int i = 0 ; i < aNamesList.size(); i++) {
			final FieldInfo fieldInfo = aNamesList.get(i);

			if (i > 0) {
				source.append("\t\t\t\tTTCN_Logger.log_char(',');\n");
			}
			source.append(MessageFormat.format("\t\t\t\tTTCN_Logger.log_event_str(\" {0} := \");\n", fieldInfo.mDisplayName));
			source.append(MessageFormat.format("\t\t\t\t{0}.log();\n", fieldInfo.mVarName));
		}
		source.append("\t\t\t\tTTCN_Logger.log_event_str(\" }\");\n");
		source.append("\t\t\t\tbreak;\n");
		source.append("\t\t\tcase COMPLEMENTED_LIST:\n");
		source.append("\t\t\t\tTTCN_Logger.log_event_str(\"complement\");\n");
		source.append("\t\t\tcase VALUE_LIST:\n");
		source.append("\t\t\t\tTTCN_Logger.log_char('(');\n");
		source.append("\t\t\t\tfor (int list_count = 0; list_count < list_value.size(); list_count++) {\n");
		source.append("\t\t\t\t\tif (list_count > 0) {\n");
		source.append("\t\t\t\t\t\tTTCN_Logger.log_event_str(\", \");\n");
		source.append("\t\t\t\t\t}\n");
		source.append("\t\t\t\t\tlist_value.get(list_count).log();\n");
		source.append("\t\t\t\t}\n");
		source.append("\t\t\t\tTTCN_Logger.log_char(')');\n");
		source.append("\t\t\t\tbreak;\n");
		source.append("\t\t\tdefault:\n");
		source.append("\t\t\t\tlog_generic();\n");
		source.append("\t\t\t\tbreak;\n");
		source.append("\t\t\t}\n");
		source.append("\t\t\tlog_ifpresent();\n");
		source.append("\t\t}\n");

		source.append('\n');
		if (aData.isDebug()) {
			source.append("\t\t/**\n");
			source.append("\t\t * Logs the matching of the provided value to this template, to help\n");
			source.append("\t\t * identify the reason for mismatch.\n");
			source.append("\t\t *\n");
			source.append("\t\t * @param match_value\n");
			source.append("\t\t *                the value to be matched.\n");
			source.append("\t\t * */\n");
		}
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
		source.append(MessageFormat.format("\t\tpublic void log_match(final {0} match_value, final boolean legacy) '{'\n", genName ) );
		source.append("\t\t\tif ( TTCN_Logger.matching_verbosity_t.VERBOSITY_COMPACT == TTCN_Logger.get_matching_verbosity() ) {\n");
		source.append("\t\t\t\tif(match(match_value, legacy)) {\n");
		source.append("\t\t\t\t\tTTCN_Logger.print_logmatch_buffer();\n");
		source.append("\t\t\t\t\tTTCN_Logger.log_event_str(\" matched\");\n");
		source.append("\t\t\t\t} else {\n");
		source.append("\t\t\t\t\tif (template_selection == template_sel.SPECIFIC_VALUE) {\n");
		source.append("\t\t\t\t\t\tfinal int previous_size = TTCN_Logger.get_logmatch_buffer_len();\n");
		for (int i = 0 ; i < aNamesList.size(); i++) {
			final FieldInfo fi = aNamesList.get(i);

			if (fi.isOptional) {
				source.append(MessageFormat.format("\t\t\t\t\t\tif (match_value.constGet_field_{0}().ispresent()) '{'\n", fi.mJavaVarName ) );
				source.append(MessageFormat.format("\t\t\t\t\t\t\tif( !{0}.match(match_value.constGet_field_{1}().get(), legacy) ) '{'\n", fi.mVarName, fi.mJavaVarName ) );
				source.append(MessageFormat.format("\t\t\t\t\t\t\t\tTTCN_Logger.log_logmatch_info(\".{0}\");\n", fi.mDisplayName ) );
				source.append(MessageFormat.format("\t\t\t\t\t\t\t\t{0}.log_match(match_value.constGet_field_{1}().get(), legacy);\n", fi.mVarName, fi.mJavaVarName ) );
				source.append("\t\t\t\t\t\t\t\tTTCN_Logger.set_logmatch_buffer_len(previous_size);\n");
				source.append("\t\t\t\t\t\t\t}\n");
				source.append("\t\t\t\t\t\t} else {\n");
				source.append(MessageFormat.format("\t\t\t\t\t\t\tif (!{0}.match_omit(legacy)) '{'\n", fi.mVarName) );
				source.append(MessageFormat.format("\t\t\t\t\t\t\t\tTTCN_Logger.log_logmatch_info(\".{0} := omit with \");\n", fi.mDisplayName ) );
				source.append("\t\t\t\t\t\t\t\tTTCN_Logger.print_logmatch_buffer();\n");
				source.append(MessageFormat.format("\t\t\t\t\t\t\t\t{0}.log();\n", fi.mVarName) );
				source.append("\t\t\t\t\t\t\t\tTTCN_Logger.log_event_str(\" unmatched\");\n");
				source.append("\t\t\t\t\t\t\t\tTTCN_Logger.set_logmatch_buffer_len(previous_size);\n");
				source.append("\t\t\t\t\t\t\t}\n");
				source.append("\t\t\t\t\t\t}\n");
			} else {
				source.append(MessageFormat.format("\t\t\t\t\t\tif( !{0}.match(match_value.constGet_field_{1}(), legacy) ) '{'\n", fi.mVarName, fi.mJavaVarName ) );
				source.append(MessageFormat.format("\t\t\t\t\t\t\tTTCN_Logger.log_logmatch_info(\".{0}\");\n", fi.mDisplayName ) );
				source.append(MessageFormat.format("\t\t\t\t\t\t\t{0}.log_match(match_value.constGet_field_{1}(), legacy);\n", fi.mVarName, fi.mJavaVarName ) );
				source.append("\t\t\t\t\t\t\tTTCN_Logger.set_logmatch_buffer_len(previous_size);\n");
				source.append("\t\t\t\t\t\t}\n");
			}
		}
		source.append("\t\t\t\t\t} else {\n");
		source.append("\t\t\t\t\t\tTTCN_Logger.print_logmatch_buffer();\n");
		source.append("\t\t\t\t\t\tmatch_value.log();\n");
		source.append("\t\t\t\t\t\tTTCN_Logger.log_event_str(\" with \");\n");
		source.append("\t\t\t\t\t\tlog();\n");
		source.append("\t\t\t\t\t\tTTCN_Logger.log_event_str(\" unmatched\");\n");
		source.append("\t\t\t\t\t}\n");
		source.append("\t\t\t\t}\n");
		source.append("\t\t\t\treturn;\n");
		source.append("\t\t\t}\n");
		source.append("\t\t\tif (template_selection == template_sel.SPECIFIC_VALUE) {\n");
		for (int i = 0 ; i < aNamesList.size(); i++) {
			final FieldInfo fi = aNamesList.get(i);

			source.append(MessageFormat.format("\t\t\t\tTTCN_Logger.log_event_str(\"'{' {0} := \");\n", fi.mDisplayName ) );
			source.append(MessageFormat.format("\t\t\t\t{0}.log_match(match_value.constGet_field_{1}(), legacy);\n", fi.mVarName, fi.mJavaVarName ) );
		}
		source.append("\t\t\t\tTTCN_Logger.log_event_str(\" }\");\n");
		source.append("\t\t\t} else {\n");
		source.append("\t\t\t\tmatch_value.log();\n");
		source.append("\t\t\t\tTTCN_Logger.log_event_str(\" with \");\n");
		source.append("\t\t\t\tlog();\n");
		source.append("\t\t\t\tif ( match(match_value, legacy) ) {\n");
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
	 * @param aNamesList
	 *                sequence field variable and type names
	 * @param genName
	 *                the name of the generated class representing the
	 *                record/set type.
	 * @param displayName
	 *                the user readable name of the type to be generated.
	 */
	private static void generateTemplateEncodeDecodeText(final StringBuilder source, final List<FieldInfo> aNamesList, final String genName, final String displayName) {
		source.append("\t\t@Override\n");
		source.append("\t\tpublic void encode_text(final Text_Buf text_buf) {\n");
		source.append("\t\t\tencode_text_base(text_buf);\n");
		source.append("\t\t\tswitch (template_selection) {\n");
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
		source.append("\t\t\tcase SPECIFIC_VALUE:\n");
		for (int i = 0 ; i < aNamesList.size(); i++) {
			final FieldInfo fi = aNamesList.get(i);

			source.append(MessageFormat.format("\t\t\t\t{0} = new {1}();\n", fi.mVarName, fi.mJavaTemplateTypeName ) );
			source.append(MessageFormat.format("\t\t\t\t{0}.decode_text(text_buf);\n", fi.mVarName ) );
		}
		source.append("\t\t\t\tbreak;\n");
		source.append("\t\t\tcase VALUE_LIST:\n");
		source.append("\t\t\tcase COMPLEMENTED_LIST: {\n");
		source.append("\t\t\t\tfinal int size = text_buf.pull_int().get_int();\n");
		source.append(MessageFormat.format("\t\t\t\tlist_value = new ArrayList<{0}_template>(size);\n", genName));
		source.append("\t\t\t\tfor (int i = 0; i < size; i++) {\n");
		source.append(MessageFormat.format("\t\t\t\t\tfinal {0}_template temp = new {0}_template();\n", genName));
		source.append("\t\t\t\t\ttemp.decode_text(text_buf);\n");
		source.append("\t\t\t\t\tlist_value.add(temp);\n");
		source.append("\t\t\t\t}\n");
		source.append("\t\t\t\tbreak;\n");
		source.append("\t\t\t}\n");
		source.append("\t\t\tdefault:\n");
		source.append(MessageFormat.format("\t\t\t\tthrow new TtcnError(\"Text decoder: An unknown/unsupported selection was received in a template of type {0}.\");\n", displayName));
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
	 * @param isSet
	 *                {@code true} if a set, {@code false} if a record
	 * */
	private static void generateTemplateSetParam(final StringBuilder source, final String displayName, final List<FieldInfo> fieldInfos, final boolean isSet) {
		source.append("\t\t@Override\n");
		source.append("\t\tpublic void set_param(final Module_Parameter param) {\n");
		source.append(MessageFormat.format("\t\t\tparam.basic_check(Module_Parameter.basic_check_bits_t.BC_TEMPLATE.getValue(), \"{0} template\");\n", isSet ? "set" : "record"));
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
		source.append(MessageFormat.format("\t\t\t\tif (param.get_size() > {0}) '{'\n", fieldInfos.size()));
		source.append(MessageFormat.format("\t\t\t\t\tparam.error(MessageFormat.format(\"{0} template of type {1} has {2} fields but list value has '{'0'}' fields.\", param.get_size()));\n", isSet ? "set" : "record", displayName, fieldInfos.size()));
		source.append("\t\t\t\t}\n");
		for (int i = 0 ; i < fieldInfos.size(); i++) {
			final FieldInfo fieldInfo = fieldInfos.get(i);

			source.append(MessageFormat.format("\t\t\t\tif (param.get_size() > {0} && param.get_elem({0}).get_type() != Module_Parameter.type_t.MP_NotUsed) '{'\n", i));
			source.append(MessageFormat.format("\t\t\t\t\tget_field_{0}().set_param(param.get_elem({1}));\n", fieldInfo.mJavaVarName, i));
			source.append("\t\t\t\t}\n");
		}
		source.append("\t\t\t\tbreak;\n");
		source.append("\t\t\tcase MP_Assignment_List: {\n");
		source.append("\t\t\t\tfinal boolean value_used[] = new boolean[param.get_size()];\n");
		for (int i = 0 ; i < fieldInfos.size(); i++) {
			final FieldInfo fieldInfo = fieldInfos.get(i);

			source.append("\t\t\t\tfor (int val_idx = 0; val_idx < param.get_size(); val_idx++) {\n");
			source.append("\t\t\t\t\tfinal Module_Parameter curr_param = param.get_elem(val_idx);\n");
			source.append(MessageFormat.format("\t\t\t\t\tif (\"{0}\".equals(curr_param.get_id().get_name())) '{'\n", fieldInfo.mDisplayName));
			source.append("\t\t\t\t\t\tif (curr_param.get_type() != Module_Parameter.type_t.MP_NotUsed) {\n");
			source.append(MessageFormat.format("\t\t\t\t\t\t\tget_field_{0}().set_param(curr_param);\n", fieldInfo.mJavaVarName));
			source.append("\t\t\t\t\t\t}\n");
			source.append("\t\t\t\t\t\tvalue_used[val_idx] = true;\n");
			source.append("\t\t\t\t\t}\n");
			source.append("\t\t\t\t}\n");
		}

		source.append("\t\t\t\tfor (int val_idx = 0; val_idx < param.get_size(); val_idx++) {\n");
		source.append("\t\t\t\t\tif (!value_used[val_idx]) {\n");
		source.append("\t\t\t\t\t\tfinal Module_Parameter curr_param = param.get_elem(val_idx);\n");
		source.append(MessageFormat.format("\t\t\t\t\t\tcurr_param.error(MessageFormat.format(\"Non existent field name in type {0}: '{'0'}'\", curr_param.get_id().get_name()));\n", displayName));
		source.append("\t\t\t\t\t\tbreak;\n");
		source.append("\t\t\t\t\t}\n");
		source.append("\t\t\t\t}\n");
		source.append("\t\t\t\tbreak;\n");
		source.append("\t\t\t}\n");
		source.append("\t\t\tdefault:\n");
		source.append(MessageFormat.format("\t\t\t\tparam.type_error(\"{0} template\", \"{1}\");\n", isSet ? "set" : "record", displayName));
		source.append("\t\t\t}\n");
		source.append("\t\t\tis_ifPresent = param.get_ifpresent();\n");
		source.append("\t\t}\n\n");
	}

	/**
	 * Generate check_restriction
	 *
	 * @param source
	 *                where the source code is to be generated.
	 * @param displayName
	 *                the user readable name of the type to be generated.
	 * @param fieldInfos
	 *                the list of information about the fields.
	 * @param isSet
	 *                {@code true} if a set, {@code false} if a record
	 * */
	private static void generateTemplateCheckRestriction(final StringBuilder source, final String displayName, final List<FieldInfo> fieldInfos, final boolean isSet) {
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
		for (int i = 0 ; i < fieldInfos.size(); i++) {
			final FieldInfo fieldInfo = fieldInfos.get(i);

			source.append(MessageFormat.format("\t\t\t\tthis.{0}.check_restriction(restriction, name == null ? \"{1}\" : name, legacy);\n", fieldInfo.mJavaVarName, displayName));
		}
		source.append("\t\t\t\treturn;\n");
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

	/**
	 * This function can be used to generate the value class of record and
	 * set types
	 *
	 * defEmptyRecordClass in compilers/record.c
	 *
	 * @param aData
	 *                used to access build settings.
	 * @param source
	 *                where the source code is to be generated.
	 * @param className
	 *                the name of the generated class representing the
	 *                record/set type.
	 * @param classDisplayName
	 *                the user readable name of the type to be generated.
	 * @param rawNeeded
	 *                {@code true} if encoding/decoding for RAW is to be
	 *                generated.
	 */
	public static void generateEmptyValueClass(final JavaGenData aData, final StringBuilder source, final String className, final String classDisplayname, final boolean rawNeeded) {
		aData.addBuiltinTypeImport("TitanNull_Type");

		source.append(MessageFormat.format("\tpublic static class {0} extends Base_Type '{'\n", className));
		source.append("\t\tprivate boolean bound_flag;\n\n");

		source.append(MessageFormat.format("\t\tpublic {0}() '{'\n", className));
		source.append("\t\t\tbound_flag = false;\n");
		source.append("\t\t}\n\n");

		source.append(MessageFormat.format("\t\tpublic {0}( final TitanNull_Type otherValue ) '{'\n", className));
		source.append("\t\t\tbound_flag = true;\n");
		source.append("\t\t}\n\n");

		source.append(MessageFormat.format("\t\tpublic {0}( final {0} otherValue ) '{'\n", className));
		source.append(MessageFormat.format("\t\t\totherValue.must_bound(\"Copying of an unbound value of type {0}.\");\n", classDisplayname));
		source.append("\t\t\tbound_flag = true;\n");
		source.append("\t\t}\n\n");

		source.append("\t\t//originally operator=\n");
		source.append(MessageFormat.format("\t\tpublic {0} operator_assign( final TitanNull_Type otherValue ) '{'\n", className));
		source.append("\t\t\tbound_flag = true;\n");
		source.append("\t\t\treturn this;\n");
		source.append("\t\t}\n\n");

		source.append("\t\t//originally operator=\n");
		source.append(MessageFormat.format("\t\tpublic {0} operator_assign( final {0} otherValue ) '{'\n", className));
		source.append(MessageFormat.format("\t\t\totherValue.must_bound(\"Assignment of an unbound value of type {0}.\");\n", classDisplayname));
		source.append("\t\t\tbound_flag = true;\n");
		source.append("\t\t\treturn this;\n");
		source.append("\t\t}\n\n");

		source.append("\t\t@Override\n");
		source.append(MessageFormat.format("\t\tpublic {0} operator_assign( final Base_Type otherValue ) '{'\n", className));
		source.append(MessageFormat.format("\t\t\tif (otherValue instanceof {0}) '{'\n", className));
		source.append(MessageFormat.format("\t\t\t\treturn operator_assign(({0})otherValue);\n", className));
		source.append("\t\t\t}\n");
		source.append(MessageFormat.format("\t\t\tthrow new TtcnError(\"Internal Error: value can not be cast to {0}.\");\n", className));
		source.append("\t\t}\n\n");

		source.append("\t\t@Override\n");
		source.append("\t\tpublic void clean_up() {\n");
		source.append("\t\t\tbound_flag = false;\n");
		source.append("\t\t}\n\n");

		source.append("\t\t@Override\n");
		source.append("\t\tpublic boolean is_bound() {\n");
		source.append("\t\t\treturn bound_flag;\n");
		source.append("\t\t}\n\n");

		source.append("\t\t@Override\n");
		source.append("\t\tpublic boolean is_present() {\n");
		source.append("\t\t\treturn is_bound();\n");
		source.append("\t\t}\n\n");

		source.append("\t\t@Override\n");
		source.append("\t\tpublic boolean is_value() {\n");
		source.append("\t\t\treturn bound_flag;\n");
		source.append("\t\t}\n\n");

		if (aData.isDebug()) {
			source.append("\t\t/**\n");
			source.append("\t\t * Checks if the current value is equivalent to the provided one.\n");
			source.append("\t\t *\n");
			source.append("\t\t * operator== in the core\n");
			source.append("\t\t *\n");
			source.append("\t\t * @param otherValue\n");
			source.append("\t\t *                the other value to check against.\n");
			source.append("\t\t * @return {@code true} if the values are equivalent.\n");
			source.append("\t\t */\n");
		}
		source.append("\t\tpublic boolean operator_equals( final TitanNull_Type otherValue ) {\n");
		source.append(MessageFormat.format("\t\t\tmust_bound(\"Comparison of an unbound value of type {0}.\");\n", classDisplayname));
		source.append("\t\t\treturn true;\n");
		source.append("\t\t}\n\n");

		if (aData.isDebug()) {
			source.append("\t\t/**\n");
			source.append("\t\t * Checks if the current value is equivalent to the provided one.\n");
			source.append("\t\t *\n");
			source.append("\t\t * operator== in the core\n");
			source.append("\t\t *\n");
			source.append("\t\t * @param otherValue\n");
			source.append("\t\t *                the other value to check against.\n");
			source.append("\t\t * @return {@code true} if the values are equivalent.\n");
			source.append("\t\t */\n");
		}
		source.append(MessageFormat.format("\t\tpublic boolean operator_equals( final {0} otherValue ) '{'\n", className));
		source.append(MessageFormat.format("\t\t\tmust_bound(\"Comparison of an unbound value of type {0}.\");\n", classDisplayname));
		source.append(MessageFormat.format("\t\t\totherValue.must_bound(\"Comparison of an unbound value of type {0}.\");\n", classDisplayname));
		source.append("\t\t\treturn true;\n");
		source.append("\t\t}\n\n");

		source.append("\t\t@Override\n");
		source.append("\t\tpublic boolean operator_equals( final Base_Type otherValue ) {\n");
		source.append(MessageFormat.format("\t\t\tif (otherValue instanceof {0}) '{'\n", className));
		source.append(MessageFormat.format("\t\t\t\treturn operator_equals(({0})otherValue);\n", className));
		source.append("\t\t\t}\n");
		source.append(MessageFormat.format("\t\t\tthrow new TtcnError(\"Internal Error: value can not be cast to {0}.\");\n", classDisplayname));
		source.append("\t\t}\n\n");

		if (aData.isDebug()) {
			source.append("\t\t/**\n");
			source.append("\t\t * Checks if the current value is not equivalent to the provided one.\n");
			source.append("\t\t *\n");
			source.append("\t\t * operator!= in the core\n");
			source.append("\t\t *\n");
			source.append("\t\t * @param otherValue\n");
			source.append("\t\t *                the other value to check against.\n");
			source.append("\t\t * @return {@code true} if the values are not equivalent.\n");
			source.append("\t\t */\n");
		}
		source.append("\t\tpublic boolean operator_not_equals( final TitanNull_Type otherValue ) {\n");
		source.append("\t\t\treturn !operator_equals(otherValue);\n");
		source.append("\t\t}\n\n");

		if (aData.isDebug()) {
			source.append("\t\t/**\n");
			source.append("\t\t * Checks if the current value is not equivalent to the provided one.\n");
			source.append("\t\t *\n");
			source.append("\t\t * operator!= in the core\n");
			source.append("\t\t *\n");
			source.append("\t\t * @param otherValue\n");
			source.append("\t\t *                the other value to check against.\n");
			source.append("\t\t * @return {@code true} if not all fields are equivalent, {@code false} otherwise.\n");
			source.append("\t\t */\n");
		}
		source.append(MessageFormat.format("\t\tpublic boolean operator_not_equals( final {0} otherValue ) '{'\n", className));
		source.append("\t\t\treturn !operator_equals(otherValue);\n");
		source.append("\t\t}\n\n");

		if (aData.isDebug()) {
			source.append("\t\t/**\n");
			source.append("\t\t * Checks if the current value is not equivalent to the provided one.\n");
			source.append("\t\t *\n");
			source.append("\t\t * operator!= in the core\n");
			source.append("\t\t *\n");
			source.append("\t\t * @param otherValue\n");
			source.append("\t\t *                the other value to check against.\n");
			source.append("\t\t * @return {@code true} if not all fields are equivalent, {@code false} otherwise.\n");
			source.append("\t\t */\n");
		}
		source.append("\t\tpublic boolean operator_not_equals( final Base_Type otherValue ) {\n");
		source.append("\t\t\treturn !operator_equals(otherValue);\n");
		source.append("\t\t}\n\n");

		source.append("\t\t@Override\n");
		source.append("\t\tpublic void log() {\n");
		source.append("\t\t\tif (bound_flag) {\n");
		source.append("\t\t\t\tTTCN_Logger.log_event_str(\"{ }\");\n");
		source.append("\t\t\t\treturn;\n");
		source.append("\t\t\t}\n");
		source.append("\t\t\tTTCN_Logger.log_event_unbound();\n");
		source.append("\t\t}\n\n");

		source.append("\t\t@Override\n");
		source.append("\t\tpublic void set_param(final Module_Parameter param) {\n");
		source.append("\t\t\tparam.basic_check(Module_Parameter.basic_check_bits_t.BC_VALUE.getValue(), \"empty record/set value (i.e. { })\");\n");
		source.append("\t\t\tif (param.get_type() != Module_Parameter.type_t.MP_Value_List || param.get_size() > 0) {\n");
		source.append(MessageFormat.format("\t\t\t\tparam.type_error(\"empty record/set value (i.e. '{' '}')\", \"{0}\");\n", classDisplayname));
		source.append("\t\t\t}\n");
		source.append("\t\t\tbound_flag = true;\n");
		source.append("\t\t}\n\n");

		source.append("\t\t@Override\n");
		source.append("\t\tpublic void encode_text(final Text_Buf text_buf) {\n");
		source.append(MessageFormat.format("\t\t\tmust_bound(\"Text encoder: Encoding an unbound value of type {0}.\");\n", classDisplayname));
		source.append("\t\t}\n\n");

		source.append("\t\t@Override\n");
		source.append("\t\tpublic void decode_text(final Text_Buf text_buf) {\n");
		source.append("\t\t\tbound_flag = true;\n");
		source.append("\t\t}\n\n");

		source.append("\t\t@Override\n");
		source.append("\t\tpublic void encode(final TTCN_Typedescriptor p_td, final TTCN_Buffer p_buf, final coding_type p_coding, final int flavour) {\n");
		source.append("\t\t\tswitch (p_coding) {\n");
		source.append("\t\t\tcase CT_RAW: {\n");
		source.append("\t\t\t\tfinal TTCN_EncDec_ErrorContext errorContext = new TTCN_EncDec_ErrorContext(\"While RAW-encoding type '%s': \", p_td.name);\n");
		source.append("\t\t\t\tif (p_td.raw == null) {\n");
		source.append("\t\t\t\t\tTTCN_EncDec_ErrorContext.error_internal(\"No RAW descriptor available for type '%s'.\", p_td.name);\n");
		source.append("\t\t\t\t}\n");
		source.append("\t\t\t\tfinal RAW_enc_tr_pos tree_position = new RAW_enc_tr_pos(0, null);\n");
		source.append("\t\t\t\tfinal RAW_enc_tree root = new RAW_enc_tree(false, null, tree_position, 1, p_td.raw);\n");
		source.append("\t\t\t\tRAW_encode(p_td, root);\n");
		source.append("\t\t\t\troot.put_to_buf(p_buf);\n");
		source.append("\t\t\t\terrorContext.leave_context();\n");
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
		source.append("\t\t\t\tif (p_td.raw == null) {\n");
		source.append("\t\t\t\t\tTTCN_EncDec_ErrorContext.error_internal(\"No RAW descriptor available for type '%s'.\", p_td.name);\n");
		source.append("\t\t\t\t}\n");
		source.append("\t\t\t\traw_order_t order;\n");
		source.append("\t\t\t\tswitch (p_td.raw.top_bit_order) {\n");
		source.append("\t\t\t\tcase TOP_BIT_LEFT:\n");
		source.append("\t\t\t\t\torder = raw_order_t.ORDER_LSB;\n");
		source.append("\t\t\t\t\tbreak;\n");
		source.append("\t\t\t\tcase TOP_BIT_RIGHT:\n");
		source.append("\t\t\t\tdefault:\n");
		source.append("\t\t\t\t\torder = raw_order_t.ORDER_MSB;\n");
		source.append("\t\t\t\t\tbreak;\n");
		source.append("\t\t\t\t}\n");
		source.append("\t\t\t\tfinal int rawr = RAW_decode(p_td, p_buf, p_buf.get_len() * 8, order);\n");
		source.append("\t\t\t\tif (rawr < 0) {\n");
		source.append("\t\t\t\t\tfinal error_type temp = error_type.values()[-rawr];\n");
		source.append("\t\t\t\t\tswitch (temp) {\n");
		source.append("\t\t\t\t\tcase ET_INCOMPL_MSG:\n");
		source.append("\t\t\t\t\tcase ET_LEN_ERR:\n");
		source.append("\t\t\t\t\t\tTTCN_EncDec_ErrorContext.error(temp, \"Can not decode type '%s', because invalid or incomplete message was received\", p_td.name);\n");
		source.append("\t\t\t\t\t\tbreak;\n");
		source.append("\t\t\t\t\tcase ET_UNBOUND:\n");
		source.append("\t\t\t\t\tdefault:\n");
		source.append("\t\t\t\t\t\tTTCN_EncDec_ErrorContext.error(error_type.ET_INVAL_MSG, \"Can not decode type '%s', because invalid or incomplete message was received\", p_td.name);\n");
		source.append("\t\t\t\t\t\tbreak;\n");
		source.append("\t\t\t\t\t}\n");
		source.append("\t\t\t\t}\n");
		source.append("\t\t\t\terrorContext.leave_context();\n");
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
			source.append("\t\t\tif (!is_bound()) {\n");
			source.append("\t\t\t\tTTCN_EncDec_ErrorContext.error(error_type.ET_UNBOUND, \"Encoding an unbound value.\", \"\");\n");
			source.append("\t\t\t}\n");
			source.append("\t\t\treturn 0;\n");
			source.append("\t\t}\n");

			source.append("\t\t@Override\n");
			source.append("\t\t/** {@inheritDoc} */\n");
			source.append("\t\tpublic int RAW_decode(final TTCN_Typedescriptor p_td, final TTCN_Buffer buff, int limit, final raw_order_t top_bit_ord, final boolean no_err, final int sel_field, final boolean first_call, final RAW_Force_Omit force_omit) {\n");
			source.append("\t\t\tbound_flag = true;");
			source.append("\t\t\treturn buff.increase_pos_padd(p_td.raw.prepadding) + buff.increase_pos_padd(p_td.raw.padding);\n");
			source.append("\t\t}\n");
		}

		source.append("\t}\n\n");
	}

	/**
	 * This function can be used to generate the template class of record
	 * and set types
	 *
	 * defEmptyRecordTemplate in compilers/record.c
	 *
	 * @param aData
	 *                used to access build settings.
	 * @param source
	 *                where the source code is to be generated.
	 * @param className
	 *                the name of the generated class representing the
	 *                record/set type.
	 * @param classDisplayName
	 *                the user readable name of the type to be generated.
	 * @param fieldInfos
	 *                the list of information about the fields.
	 * @param hasOptional
	 *                {@code true} if the type has an optional field.
	 * @param isSet
	 *                {@code true} if generating code for a set,
	 *                {@code false} if generating code for a record.
	 */
	public static void generateEmptyTemplateClass(final JavaGenData aData, final StringBuilder source, final String className,
			final String classDisplayName, final List<FieldInfo> fieldInfos, final boolean hasOptional, final boolean isSet) {
		aData.addBuiltinTypeImport("TitanNull_Type");

		source.append(MessageFormat.format("\tpublic static class {0}_template extends Base_Template '{'\n", className));

		source.append("\t\t//originally value_list/list_value\n");
		source.append(MessageFormat.format("\t\tprivate List<{0}_template> list_value;\n", className));

		if (aData.isDebug()) {
			source.append("\t\t/**\n");
			source.append("\t\t * Initializes to unbound/uninitialized template.\n");
			source.append("\t\t * */\n");
		}
		source.append(MessageFormat.format("\t\tpublic {0}_template() '{'\n", className));
		source.append("\t\t}\n\n");

		if (aData.isDebug()) {
			source.append("\t\t/**\n");
			source.append("\t\t * Initializes to a given template kind.\n");
			source.append("\t\t *\n");
			source.append("\t\t * @param other_value\n");
			source.append("\t\t *                the template kind to initialize to.\n");
			source.append("\t\t * */\n");
		}
		source.append(MessageFormat.format("\t\tpublic {0}_template(final template_sel other_value) '{'\n", className));
		source.append("\t\t\tsuper( other_value );\n");
		source.append("\t\t\tcheck_single_selection( other_value );\n");
		source.append("\t\t}\n\n");

		if (aData.isDebug()) {
			source.append("\t\t/**\n");
			source.append("\t\t * Initializes to an empty specific value template.\n");
			source.append("\t\t *\n");
			source.append("\t\t * @param null_value\n");
			source.append("\t\t *                the null value.\n");
			source.append("\t\t * */\n");
		}
		source.append(MessageFormat.format("\t\tpublic {0}_template(final TitanNull_Type other_value) '{'\n", className));
		source.append("\t\t\tsuper(template_sel.SPECIFIC_VALUE);\n");
		source.append("\t\t}\n\n");

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
		source.append(MessageFormat.format("\t\tpublic {0}_template(final {0} other_value) '{'\n", className));
		source.append("\t\t\tsuper(template_sel.SPECIFIC_VALUE);\n");
		source.append(MessageFormat.format("\t\t\tother_value.must_bound(\"Creating a template from an unbound value of type {0}.\");\n", classDisplayName));
		source.append("\t\t}\n\n");

		if (aData.isDebug()) {
			source.append("\t\t/**\n");
			source.append("\t\t * Initializes to a given template.\n");
			source.append("\t\t * The elements of the provided template are copied.\n");
			source.append("\t\t *\n");
			source.append("\t\t * @param other_value\n");
			source.append("\t\t *                the value to initialize to.\n");
			source.append("\t\t * */\n");
		}
		source.append(MessageFormat.format("\t\tpublic {0}_template(final {0}_template other_value) '{'\n", className));
		source.append("\t\t\tcopy_template( other_value );\n");
		source.append("\t\t}\n\n");

		if (aData.isDebug()) {
			source.append("\t\t/**\n");
			source.append("\t\t * Initializes to a given value.\n");
			source.append("\t\t * The template becomes a specific template with the provided value.\n");
			source.append("\t\t * Causes a dynamic testcase error if the value is neither present nor optional.\n");
			source.append("\t\t *\n");
			source.append("\t\t * @param other_value\n");
			source.append("\t\t *                the value to initialize to.\n");
			source.append("\t\t * */\n");
		}
		source.append(MessageFormat.format("\t\tpublic {0}_template(final Optional<{0}> other_value) '{'\n", className));
		source.append("\t\t\tswitch (other_value.get_selection()) {\n");
		source.append("\t\t\tcase OPTIONAL_PRESENT:\n");
		source.append("\t\t\t\tset_selection(template_sel.SPECIFIC_VALUE);\n");
		source.append("\t\t\t\tbreak;\n");
		source.append("\t\t\tcase OPTIONAL_OMIT:\n");
		source.append("\t\t\t\tset_selection(template_sel.OMIT_VALUE);\n");
		source.append("\t\t\t\tbreak;\n");
		source.append("\t\t\tdefault:\n");
		source.append(MessageFormat.format("\t\t\t\tthrow new TtcnError(\"Creating a template of type {0} from an unbound optional field.\");\n", classDisplayName));
		source.append("\t\t\t}\n");
		source.append("\t\t}\n\n");

		source.append("\t\t//originally operator=\n");
		source.append(MessageFormat.format("\t\tpublic {0}_template operator_assign(final template_sel other_value) '{'\n", className));
		source.append("\t\t\tcheck_single_selection(other_value);\n");
		source.append("\t\t\tclean_up();\n");
		source.append("\t\t\tset_selection(other_value);\n");
		source.append("\t\t\treturn this;\n");
		source.append("\t\t}\n\n");

		source.append("\t\t//originally operator=\n");
		source.append(MessageFormat.format("\t\tpublic {0}_template operator_assign(final TitanNull_Type other_value) '{'\n", className));
		source.append("\t\t\tclean_up();\n");
		source.append("\t\t\tset_selection(template_sel.SPECIFIC_VALUE);\n");
		source.append("\t\t\treturn this;\n");
		source.append("\t\t}\n\n");

		source.append("\t\t//originally operator=\n");
		source.append(MessageFormat.format("\t\tpublic {0}_template operator_assign(final {0} other_value) '{'\n", className));
		source.append(MessageFormat.format("\t\t\tother_value.must_bound(\"Assignment of an unbound value of type {0} to a template.\");\n", classDisplayName));
		source.append("\t\t\tclean_up();\n");
		source.append("\t\t\tset_selection(template_sel.SPECIFIC_VALUE);\n");
		source.append("\t\t\treturn this;\n");
		source.append("\t\t}\n\n");

		source.append("\t\t//originally operator=\n");
		source.append(MessageFormat.format("\t\tpublic {0}_template operator_assign(final {0}_template other_value) '{'\n", className));
		source.append("\t\t\tif (other_value != this) {\n");
		source.append("\t\t\t\tclean_up();\n");
		source.append("\t\t\t\tcopy_template(other_value);\n");
		source.append("\t\t\t}\n");
		source.append("\t\t\treturn this;\n");
		source.append("\t\t}\n\n");

		source.append("\t\t@Override\n");
		source.append(MessageFormat.format("\t\tpublic {0}_template operator_assign(final Base_Type other_value) '{'\n", className));
		source.append(MessageFormat.format("\t\t\tif (other_value instanceof {0}) '{'\n", className));
		source.append(MessageFormat.format("\t\t\t\treturn operator_assign(({0}) other_value);\n", className));
		source.append("\t\t\t}\n");
		source.append( MessageFormat.format("\t\t\tthrow new TtcnError(MessageFormat.format(\"Internal Error: value `{0}'' can not be cast to {1}\", other_value));\n", className));
		source.append("\t\t}\n\n");

		source.append("\t\t@Override\n");
		source.append(MessageFormat.format("\t\tpublic {0}_template operator_assign(final Base_Template other_value) '{'\n", className));
		source.append(MessageFormat.format("\t\t\tif (other_value instanceof {0}_template) '{'\n", className));
		source.append(MessageFormat.format("\t\t\t\treturn operator_assign(({0}_template) other_value);\n", className));
		source.append("\t\t\t}\n");
		source.append( MessageFormat.format("\t\t\tthrow new TtcnError(MessageFormat.format(\"Internal Error: value `{0}'' can not be cast to {1}_template\", other_value));\n", className));
		source.append("\t\t}\n\n");

		source.append(MessageFormat.format("\t\tpublic {0}_template operator_assign(final Optional<{0}> other_value) '{'\n", className));
		source.append("\t\t\tclean_up();\n");
		source.append("\t\t\tswitch (other_value.get_selection()) {\n");
		source.append("\t\t\tcase OPTIONAL_PRESENT:\n");
		source.append("\t\t\t\tset_selection(template_sel.SPECIFIC_VALUE);\n");
		source.append("\t\t\t\tbreak;\n");
		source.append("\t\t\tcase OPTIONAL_OMIT:\n");
		source.append("\t\t\t\tset_selection(template_sel.OMIT_VALUE);\n");
		source.append("\t\t\t\tbreak;\n");
		source.append("\t\t\tdefault:\n");
		source.append(MessageFormat.format("\t\t\t\tthrow new TtcnError(\"Assignment of an unbound optional field to a template of type {0} .\");\n", classDisplayName));
		source.append("\t\t\t}\n");
		source.append("\t\t\treturn this;\n");
		source.append("\t\t}\n\n");

		source.append(MessageFormat.format("\t\tpublic void copy_template(final {0}_template other_value) '{'\n", className));
		source.append("\t\t\tswitch (other_value.template_selection) {\n");
		source.append("\t\t\tcase SPECIFIC_VALUE:\n");
		source.append("\t\t\tcase OMIT_VALUE:\n");
		source.append("\t\t\tcase ANY_VALUE:\n");
		source.append("\t\t\tcase ANY_OR_OMIT:\n");
		source.append("\t\t\t\tbreak;\n");
		source.append("\t\t\tcase VALUE_LIST:\n");
		source.append("\t\t\tcase COMPLEMENTED_LIST:\n");
		source.append( MessageFormat.format( "\t\t\t\tlist_value = new ArrayList<{0}_template>(other_value.list_value.size());\n", className));
		source.append("\t\t\t\tfor(int i = 0; i < other_value.list_value.size(); i++) {\n");
		source.append( MessageFormat.format( "\t\t\t\t\tfinal {0}_template temp = new {0}_template(other_value.list_value.get(i));\n", className));
		source.append("\t\t\t\t\tlist_value.add(temp);\n");
		source.append("\t\t\t\t}\n");
		source.append("\t\t\t\tbreak;\n");
		source.append("\t\t\tdefault:\n");
		source.append(MessageFormat.format("\t\t\t\tthrow new TtcnError(\"Copying an uninitialized template of type {0}.\");\n", classDisplayName));
		source.append("\t\t\t}\n");
		source.append("\t\t\tset_selection(other_value);\n");
		source.append("\t\t}\n\n");

		source.append("\t\t@Override\n");
		source.append("\t\tpublic boolean is_present(final boolean legacy) {\n");
		source.append("\t\t\treturn is_present_(legacy);\n");
		source.append("\t\t}\n\n");
//TODO check the underscore versions if they are needed.
		source.append("\t\tprivate boolean is_present_(final boolean legacy) {\n");
		source.append("\t\t\tif (template_selection==template_sel.UNINITIALIZED_TEMPLATE) {\n");
		source.append("\t\t\t\treturn false;\n");
		source.append("\t\t\t}\n");
		source.append("\t\t\treturn !match_omit_(legacy);\n");
		source.append("\t\t}\n\n");

		source.append("\t\t@Override\n");
		source.append("\t\tpublic boolean match_omit(final boolean legacy) {\n");
		source.append("\t\t\treturn match_omit_(legacy);\n");
		source.append("\t\t}\n\n");

		source.append("\t\tprivate boolean match_omit_(final boolean legacy) {\n");
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
		source.append("\t\t\t\t\tfor (int l_idx=0; l_idx<list_value.size(); l_idx++) {\n");
		source.append("\t\t\t\t\t\tif (list_value.get(l_idx).match_omit_(legacy)) {\n");
		source.append("\t\t\t\t\t\t\treturn template_selection==template_sel.VALUE_LIST;\n");
		source.append("\t\t\t\t\t\t}\n");
		source.append("\t\t\t\t\t}\n");
		source.append("\t\t\t\t\treturn template_selection==template_sel.COMPLEMENTED_LIST;\n");
		source.append("\t\t\t\t} // else fall through\n");
		source.append("\t\t\tdefault:\n");
		source.append("\t\t\t\treturn false;\n");
		source.append("\t\t\t}\n");
		source.append("\t\t}\n\n");

		source.append("\t\t@Override\n");
		source.append(MessageFormat.format("\t\tpublic {0} valueof() '{'\n", className));
		source.append("\t\t\tif (template_selection != template_sel.SPECIFIC_VALUE || is_ifPresent) {\n");
		source.append(MessageFormat.format("\t\t\t\tthrow new TtcnError(\"Performing a valueof or send operation on a non-specific template of type {0}.\");\n", classDisplayName));
		source.append("\t\t\t}\n");
		source.append(MessageFormat.format("\t\t\treturn new {0}(TitanNull_Type.NULL_VALUE);\n", className));
		source.append("\t\t}\n\n");

		source.append("\t\t@Override\n");
		source.append( MessageFormat.format( "\t\tpublic {0}_template list_item(final int list_index) '{'\n", className ) );
		source.append("\t\t\tif (template_selection != template_sel.VALUE_LIST && template_selection != template_sel.COMPLEMENTED_LIST) {\n");
		source.append( MessageFormat.format( "\t\t\t\tthrow new TtcnError(\"Accessing a list element of a non-list template of type {0}.\");\n", classDisplayName ) );
		source.append("\t\t\t}\n");
		source.append("\t\t\tif (list_index < 0) {\n");
		source.append(MessageFormat.format("\t\t\t\tthrow new TtcnError(MessageFormat.format(\"Internal error: Accessing a value list template of type {0} using a negative index ('{'0'}').\", list_index));\n", classDisplayName));
		source.append("\t\t\t} else if (list_index >= list_value.size()) {\n");
		source.append( MessageFormat.format( "\t\t\t\tthrow new TtcnError(\"Index overflow in a value list template of type {0}.\");\n", classDisplayName ) );
		source.append("\t\t\t}\n");
		source.append("\t\t\treturn list_value.get(list_index);\n");
		source.append("\t\t}\n\n");

		source.append("\t\t@Override\n");
		source.append("\t\tpublic void set_type(final template_sel template_type, final int list_length) {\n");
		source.append("\t\t\tif (template_type != template_sel.VALUE_LIST && template_type != template_sel.COMPLEMENTED_LIST) {\n");
		source.append( MessageFormat.format( "\t\t\t\tthrow new TtcnError(\"Setting an invalid list for a template of type {0}.\");\n", classDisplayName ) );
		source.append("\t\t\t}\n");
		source.append("\t\t\tclean_up();\n");
		source.append("\t\t\tset_selection(template_type);\n");
		source.append( MessageFormat.format( "\t\t\tlist_value = new ArrayList<{0}_template>(list_length);\n", className ) );
		source.append("\t\t\tfor(int i = 0 ; i < list_length; i++) {\n");
		source.append(MessageFormat.format("\t\t\t\tlist_value.add(new {0}_template());\n", className));
		source.append("\t\t\t}\n");
		source.append("\t\t}\n\n");

		if (aData.isDebug()) {
			source.append("\t\t/**\n");
			source.append("\t\t * Matches the provided value against this template.\n");
			source.append("\t\t *\n");
			source.append("\t\t * @param other_value the value to be matched.\n");
			source.append("\t\t * */\n");
		}
		source.append( MessageFormat.format( "\t\tpublic boolean match(final {0} other_value) '{'\n", className ) );
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
		source.append( MessageFormat.format( "\t\tpublic boolean match(final {0} other_value, final boolean legacy) '{'\n", className ) );
		source.append("\t\t\tif (!other_value.is_bound()) {\n");
		source.append("\t\t\t\treturn false;\n");
		source.append("\t\t\t}\n");
		source.append("\t\t\treturn match(TitanNull_Type.NULL_VALUE, legacy);\n");
		source.append("\t\t}\n\n");

		source.append("\t\tprivate boolean match(final TitanNull_Type other_value, final boolean legacy) {\n");
		source.append("\t\t\tswitch (template_selection) {\n");
		source.append("\t\t\tcase ANY_VALUE:\n");
		source.append("\t\t\tcase ANY_OR_OMIT:\n");
		source.append("\t\t\t\treturn true;\n");
		source.append("\t\t\tcase OMIT_VALUE:\n");
		source.append("\t\t\t\treturn false;\n");
		source.append("\t\t\tcase SPECIFIC_VALUE:\n");
		source.append("\t\t\t\treturn true;\n");
		source.append("\t\t\tcase VALUE_LIST:\n");
		source.append("\t\t\tcase COMPLEMENTED_LIST:\n");
		source.append("\t\t\t\tfor (int list_count = 0; list_count < list_value.size(); list_count++) {\n");
		source.append("\t\t\t\t\tif (list_value.get(list_count).match(other_value, legacy)) {\n");
		source.append("\t\t\t\t\t\treturn template_selection == template_sel.VALUE_LIST;\n");
		source.append("\t\t\t\t\t}\n");
		source.append("\t\t\t\t}\n");
		source.append("\t\t\t\treturn template_selection == template_sel.COMPLEMENTED_LIST;\n");
		source.append("\t\t\tdefault:\n");
		source.append( MessageFormat.format( "\t\t\t\tthrow new TtcnError(\"Matching an uninitialized/unsupported template of type {0}.\");\n", classDisplayName ) );
		source.append("\t\t\t}\n");
		source.append("\t\t}\n\n");

		source.append("\t\t@Override\n");
		source.append("\t\tpublic boolean match(final Base_Type other_value, final boolean legacy) {\n");
		source.append( MessageFormat.format( "\t\t\tif (other_value instanceof {0}) '{'\n", className ) );
		source.append( MessageFormat.format( "\t\t\t\treturn match(({0})other_value, legacy);\n", className ) );
		source.append("\t\t\t}\n");
		source.append( MessageFormat.format( "\t\t\tthrow new TtcnError(\"Internal Error: The left operand of assignment is not of type {0}.\");\n", classDisplayName ) );
		source.append("\t\t}\n\n");

		source.append("\t\t@Override\n");
		source.append("\t\tpublic void log() {\n");
		source.append("\t\t\tswitch (template_selection) {\n");
		source.append("\t\t\tcase SPECIFIC_VALUE:\n");
		source.append("\t\t\t\tTTCN_Logger.log_event_str(\"{ }\");\n");
		source.append("\t\t\t\tbreak;\n");
		source.append("\t\t\tcase COMPLEMENTED_LIST:\n");
		source.append("\t\t\t\tTTCN_Logger.log_event_str(\"complement\");\n");
		source.append("\t\t\tcase VALUE_LIST:\n");
		source.append("\t\t\t\tTTCN_Logger.log_char('(');\n");
		source.append("\t\t\t\tfor (int list_count = 0; list_count < list_value.size(); list_count++) {\n");
		source.append("\t\t\t\t\tif (list_count > 0) {\n");
		source.append("\t\t\t\t\t\tTTCN_Logger.log_event_str(\", \");\n");
		source.append("\t\t\t\t\t}\n");
		source.append("\t\t\t\t\tlist_value.get(list_count).log();\n");
		source.append("\t\t\t\t}\n");
		source.append("\t\t\t\tTTCN_Logger.log_char(')');\n");
		source.append("\t\t\t\tbreak;\n");
		source.append("\t\t\tdefault:\n");
		source.append("\t\t\t\tlog_generic();\n");
		source.append("\t\t\t\tbreak;\n");
		source.append("\t\t\t}\n");
		source.append("\t\t\tlog_ifpresent();\n");
		source.append("\t\t}\n\n");

		if (aData.isDebug()) {
			source.append("\t\t/**\n");
			source.append("\t\t * Logs the matching of the provided value to this template, to help\n");
			source.append("\t\t * identify the reason for mismatch.\n");
			source.append("\t\t *\n");
			source.append("\t\t * @param match_value\n");
			source.append("\t\t *                the value to be matched.\n");
			source.append("\t\t * */\n");
		}
		source.append( MessageFormat.format( "\t\tpublic void log_match(final {0} match_value) '{'\n", className ) );
		source.append("\t\t\tlog_match(match_value, false);\n");
		source.append("\t\t}\n\n");

		source.append("\t\t@Override\n");
		source.append("\t\tpublic void log_match(final Base_Type match_value, final boolean legacy) {\n");
		source.append( MessageFormat.format( "\t\t\tif (match_value instanceof {0}) '{'\n", className ) );
		source.append( MessageFormat.format( "\t\t\t\tlog_match(({0})match_value, legacy);\n", className ) );
		source.append("\t\t\t\treturn;\n");
		source.append("\t\t\t}\n");
		source.append( MessageFormat.format( "\t\t\tthrow new TtcnError(\"Internal Error: value can not be cast to {0}.\");\n", classDisplayName ) );
		source.append("\t\t}\n\n");

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
		source.append( MessageFormat.format( "\t\tpublic void log_match(final {0} match_value, final boolean legacy) '{'\n", className ) );
		source.append("\t\t\tmatch_value.log();\n");
		source.append("\t\t\tTTCN_Logger.log_event_str(\" with \");\n");
		source.append("\t\t\tlog();\n");
		source.append("\t\t\tif ( match(match_value, legacy) ) {\n");
		source.append("\t\t\t\tTTCN_Logger.log_event_str(\" matched\");\n");
		source.append("\t\t\t} else {\n");
		source.append("\t\t\t\tTTCN_Logger.log_event_str(\" unmatched\");\n");
		source.append("\t\t\t}\n");
		source.append("\t\t}\n\n");

		source.append("\t\t@Override\n");
		source.append("\t\tpublic void encode_text(final Text_Buf text_buf) {\n");
		source.append("\t\t\tencode_text_base(text_buf);\n");
		source.append("\t\t\tswitch (template_selection) {\n");
		source.append("\t\t\tcase OMIT_VALUE:\n");
		source.append("\t\t\tcase ANY_VALUE:\n");
		source.append("\t\t\tcase ANY_OR_OMIT:\n");
		source.append("\t\t\tcase SPECIFIC_VALUE:\n");
		source.append("\t\t\t\tbreak;\n");
		source.append("\t\t\tcase VALUE_LIST:\n");
		source.append("\t\t\tcase COMPLEMENTED_LIST:\n");
		source.append("\t\t\t\ttext_buf.push_int(list_value.size());\n");
		source.append("\t\t\t\tfor (int i = 0; i < list_value.size(); i++) {\n");
		source.append("\t\t\t\t\tlist_value.get(i).encode_text(text_buf);\n");
		source.append("\t\t\t\t}\n");
		source.append("\t\t\t\tbreak;\n");
		source.append("\t\t\tdefault:\n");
		source.append( MessageFormat.format( "\t\t\t\tthrow new TtcnError(\"Text encoder: Encoding an uninitialized/unsupported template of type {0}.\");\n", classDisplayName));
		source.append("\t\t\t}\n");
		source.append("\t\t}\n\n");

		source.append("\t\t@Override\n");
		source.append("\t\tpublic void decode_text(final Text_Buf text_buf) {\n");
		source.append("\t\t\tdecode_text_base(text_buf);\n");
		source.append("\t\t\tswitch (template_selection) {\n");
		source.append("\t\t\tcase OMIT_VALUE:\n");
		source.append("\t\t\tcase ANY_VALUE:\n");
		source.append("\t\t\tcase ANY_OR_OMIT:\n");
		source.append("\t\t\tcase SPECIFIC_VALUE:\n");
		source.append("\t\t\t\tbreak;\n");
		source.append("\t\t\tcase VALUE_LIST:\n");
		source.append("\t\t\tcase COMPLEMENTED_LIST: {\n");
		source.append("\t\t\t\tfinal int size = text_buf.pull_int().get_int();\n");
		source.append( MessageFormat.format( "\t\t\t\tlist_value = new ArrayList<{0}_template>(size);\n", className));
		source.append("\t\t\t\tfor (int i = 0; i < size; i++) {\n");
		source.append( MessageFormat.format( "\t\t\t\t\tfinal {0}_template temp = new {0}_template();\n", className));
		source.append("\t\t\t\t\ttemp.decode_text(text_buf);\n");
		source.append("\t\t\t\t\tlist_value.add(temp);\n");
		source.append("\t\t\t\t}\n");
		source.append("\t\t\t\tbreak;\n");
		source.append("\t\t\t}\n");
		source.append("\t\t\tdefault:\n");
		source.append( MessageFormat.format( "\t\t\t\tthrow new TtcnError(\"Text decoder: An unknown/unsupported selection was received in a template of type {0}.\");\n", classDisplayName));
		source.append("\t\t\t}\n");
		source.append("\t\t}\n\n");

		source.append("\t\t@Override\n");
		source.append("\t\tpublic void set_param(final Module_Parameter param) {\n");
		source.append("\t\t\tparam.basic_check(Module_Parameter.basic_check_bits_t.BC_TEMPLATE.getValue(), \"empty record/set template\");\n");
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
		source.append(MessageFormat.format("\t\t\t\tif (param.get_size() > {0}) '{'\n", fieldInfos.size()));
		source.append(MessageFormat.format("\t\t\t\t\tparam.type_error(\"empty record/set template\", \"{0}\");\n", classDisplayName));
		source.append("\t\t\t\t}\n");
		source.append("\t\t\t\toperator_assign(TitanNull_Type.NULL_VALUE);\n");
		source.append("\t\t\t\tbreak;\n");
		source.append("\t\t\tdefault:\n");
		source.append(MessageFormat.format("\t\t\t\tparam.type_error(\"empty record/set template\", \"{0}\");\n", classDisplayName));
		source.append("\t\t\t}\n");
		source.append("\t\t\tis_ifPresent = param.get_ifpresent();\n");
		source.append("\t\t}\n\n");

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
		source.append("\t\t\t\treturn;\n");
		source.append("\t\t\tcase TR_PRESENT:\n");
		source.append("\t\t\t\tif (!match_omit(legacy)) {\n");
		source.append("\t\t\t\t\treturn;\n");
		source.append("\t\t\t\t}\n");
		source.append("\t\t\t\tbreak;\n");
		source.append("\t\t\tdefault:\n");
		source.append("\t\t\t\treturn;\n");
		source.append("\t\t\t}\n");
		source.append(MessageFormat.format("\t\t\t\tthrow new TtcnError(MessageFormat.format(\"Restriction `'{'0'}''''' on template of type '{'1'}' violated.\", get_res_name(restriction), name == null ? \"{0}\" : name));\n", classDisplayName));
		source.append("\t\t\t}\n");
		source.append("\t\t}\n\n");
	}

	/**
	 * This function can be used to generate a raw field checker.
	 *
	 * used to generate into conditionals.
	 *
	 * @param source
	 *                where the source code is to be generated.
	 * @param taglist
	 *                the taglist as data.
	 * @param is_equal
	 *                will it be used in equals style check?
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

			if (!firstExpr) {
				source.append(is_equal ? " && " : " || ");
			}
			if (is_equal) {
				source.append(MessageFormat.format("{0}.operator_equals({1})", fieldName, fields.expression.expression));
			} else {
				source.append(MessageFormat.format("!{0}.operator_equals({1})", fieldName, fields.expression.expression));
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
	 * @param source
	 *                where the source code is to be generated.
	 * @param taglist
	 *                the taglist as data.
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
			source.append(MessageFormat.format("final RAW_enc_tr_pos pr_pos{0} = new RAW_enc_tr_pos(myleaf.curr_pos.level + {1}, new_pos{2});\n", temp_tag, tempField.fields.size(), temp_tag));
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
	 * @param aData
	 *                used to access build settings.
	 * @param source
	 *                where the source code is to be generated.
	 * @param fieldInfos
	 *                the list of field informations.
	 * @param i
	 *                the index of the field to generate for.
	 * @param raw
	 *                the raw attribute of the record/set.
	 * @param raw_options
	 *                the pre-calculated raw options.
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
	 * @param aData
	 *                used to access build settings.
	 * @param source
	 *                where the source code is to be generated.
	 * @param fieldInfos
	 *                the list of field informations.
	 * @param i
	 *                the index of the field to generate for.
	 * @param raw
	 *                the raw attribute of the record/set.
	 * @param raw_options
	 *                the pre-calculated raw options.
	 * @param delayed_decode
	 *                {@code true} to generated for delayed decoding.
	 * @param prev_ext_group
	 *                the index of the previous extension group.
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
					source.append(MessageFormat.format("start_of_field{0} = start_pos_of_field{1} + get_field_{2}(){3}.get_int() * {4} + {5};\n", i, pointedField.raw.pointerbase, pointedField.mVarName, pointedField.isOptional ? ".get()" : "", pointedField.raw.unit, pointedField.raw.ptroffset));
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
					source.append('}');
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
			source.append(MessageFormat.format("start_of_field{0} = start_pos_of_field{1} + get_field_{2}(){3}.get_int() * {4} + {5};\n", i, tempPointed.raw.pointerbase, tempPointed.mJavaVarName, tempPointed.isOptional ? ".get()":"", tempPointed.raw.unit, tempPointed.raw.ptroffset));
			source.append(MessageFormat.format("buff.set_pos_bit(start_of_field{0});\n", i));
			source.append(MessageFormat.format("limit = end_of_available_data - start_of_field{0};\n", i));
		}
		if (fieldInfo.isOptional) {
			source.append(MessageFormat.format("if (force_omit != null && force_omit.shouldOmit({0})) '{'\n", i));
			source.append(MessageFormat.format("get_field_{0}().operator_assign(template_sel.OMIT_VALUE);\n", fieldInfo.mJavaVarName));
			source.append("} else {\n");
			source.append("final int fl_start_pos = buff.get_pos_bit();\n");
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
		source.append(MessageFormat.format("final RAW_Force_Omit field_{0}_force_omit = new RAW_Force_Omit({0}, force_omit, {1}_descr_.raw.forceomit);\n", i, fieldInfo.mTypeDescriptorName));
		source.append(MessageFormat.format("decoded_field_length = get_field_{0}(){1}.RAW_decode({2}_descr_, buff, ", fieldInfo.mJavaVarName, fieldInfo.isOptional ? ".get()":"", fieldInfo.mTypeDescriptorName));
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
				source.append(MessageFormat.format(", value_of_length_field{0} != 0", field_index));
				found = true;
			}
		}
		if (!found) {
			source.append(", true");
		}
		source.append(MessageFormat.format(", field_{0}_force_omit);\n", i));

		if (delayed_decode) {
			source.append(MessageFormat.format("if ( decoded_field_length != {0}) '{'\n", fieldInfo.raw.length));
			source.append("return -1;\n");
			source.append("}\n");
		} else if(fieldInfo.isOptional) {
			source.append("if (decoded_field_length < 1) {\n");
			source.append(MessageFormat.format("{0}.operator_assign(template_sel.OMIT_VALUE);\n", fieldInfo.mVarName));
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
				source.append(MessageFormat.format("{0}.operator_assign(template_sel.OMIT_VALUE);\n", fieldInfo.mVarName));
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
					source.append(MessageFormat.format("if ({0}{1}.get_field_{2}().is_present()) '{'\n", fieldInfo.mVarName, fieldInfo.isOptional? ".get()":"", FieldSubReference.getJavaGetterName(fieldInfo.raw.lengthindex.nthfieldname)));
				}
				if (fieldInfo.raw.lengthto_offset != 0) {
					source.append(MessageFormat.format("{0}{1}.get_field_{2}(){3}.operator_assign({0}{1}.get_field_{2}(){3} - {4});\n",
							fieldInfo.mVarName, fieldInfo.isOptional ? ".get()" : "", FieldSubReference.getJavaGetterName(fieldInfo.raw.lengthindex.nthfieldname), fieldInfo.raw.lengthindex.fieldtype == rawAST_coding_field_type.OPTIONAL_FIELD ? ".get()" : "", fieldInfo.raw.lengthto_offset));
				}
				source.append(MessageFormat.format("value_of_length_field{0} += {1}{2}.get_field_{3}(){4}.get_long() * {5};\n",
						i, fieldInfo.mVarName, fieldInfo.isOptional ? ".get()" : "", FieldSubReference.getJavaGetterName(fieldInfo.raw.lengthindex.nthfieldname), fieldInfo.raw.lengthindex.fieldtype == rawAST_coding_field_type.OPTIONAL_FIELD ? ".get()" : "", fieldInfo.raw.unit == -1 ? 1 : fieldInfo.raw.unit));
				if (fieldInfo.raw.lengthindex.fieldtype == rawAST_coding_field_type.OPTIONAL_FIELD) {
					source.append("}\n");
				}
			} else if (fieldInfo.raw.union_member_num > 0) {
				source.append(MessageFormat.format("switch ({0}{1}.get_selection()) '{'\n", fieldInfo.mVarName, fieldInfo.isOptional ? ".get()":""));
				for (int m = 0; m < fieldInfo.raw.member_name.size(); m++) {
					source.append(MessageFormat.format("case {0}.{1}", fieldInfo.raw.member_name.get(0), fieldInfo.raw.member_name.get(m)));
					if (fieldInfo.raw.lengthto_offset != 0) {
						source.append(MessageFormat.format("{0}{1}.get_field_{2}().operator_assign({0}{1}.get_field_{2}() - {3});\n", fieldInfo.mVarName, fieldInfo.isOptional ? ".get()" : "", fieldInfo.raw.member_name.get(m), fieldInfo.raw.lengthto_offset));
					}
					source.append(MessageFormat.format("value_of_length_field{0} += {1}{2}.get_field_{3}().get_long() * {4};\n", i, fieldInfo.mVarName, fieldInfo.isOptional ? ".get()" : "", fieldInfo.raw.member_name.get(m), fieldInfo.raw.unit == -1 ? 1 : fieldInfo.raw.unit));
					source.append("break;\n");
				}
				source.append("default:\n");
				source.append(MessageFormat.format("value_of_length_field{0} = 0;\n", i));
				source.append("}\n");
			} else {
				if (fieldInfo.raw.lengthto_offset != 0) {
					source.append(MessageFormat.format("{0}{1}.operator_assign({0}{1}.get_int() - {2});\n", fieldInfo.mVarName, fieldInfo.isOptional? ".get()":"", fieldInfo.raw.lengthto_offset));
				}
				source.append(MessageFormat.format("value_of_length_field{0} += {1}{2}.get_long() * {3};\n", i, fieldInfo.mVarName, fieldInfo.isOptional ? ".get()" : "", fieldInfo.raw.unit == -1 ? 1 : fieldInfo.raw.unit));
			}
		}
		if (tempRawOption.pointerto) {
			source.append(MessageFormat.format("start_of_field{0} = {1}{2}.get_int() {3};\n ", fieldInfo.raw.pointerto, fieldInfo.mVarName, fieldInfo.isOptional? ".get()":"", fieldInfo.raw.ptroffset > 0? " + 1": "- 1"));
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
			source.append("}\n");
			source.append('}');
			if (tempRawOption.tag_type > 0) {
				source.append("\n}");
			}
			source.append(" else {\n");
			source.append(MessageFormat.format("{0}.operator_assign(template_sel.OMIT_VALUE);\n", fieldInfo.mVarName));
			source.append("}\n");
		}
	}
}
