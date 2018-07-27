package org.eclipse.titan.designer.AST.TTCN3.types;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.titan.designer.AST.FieldSubReference;
import org.eclipse.titan.designer.AST.TTCN3.attributes.RawASTStruct;
import org.eclipse.titan.designer.AST.TTCN3.attributes.RawASTStruct.rawAST_coding_field_list;
import org.eclipse.titan.designer.AST.TTCN3.attributes.RawASTStruct.rawAST_coding_field_type;
import org.eclipse.titan.designer.AST.TTCN3.attributes.RawASTStruct.rawAST_coding_fields;
import org.eclipse.titan.designer.AST.TTCN3.attributes.RawASTStruct.rawAST_coding_taglist;
import org.eclipse.titan.designer.compiler.JavaGenData;

/**
 * Utility class for generating the value and template classes for union/choice types.
 *
 * The code generated for union/choice types only differs in matching and encoding.
 *
 * @author Kristof Szabados
 * */
public class UnionGenerator {
	/**
	 * Data structure to store sequence field variable and type names.
	 * Used for java code generation.
	 */
	public static class FieldInfo {

		/** Java type name of the field */
		private String mJavaTypeName;

		private String mJavaTemplateName;

		/** Field variable name in TTCN-3 and java */
		private String mVarName;

		/** The user readable name of the field, typically used in error messages */
		private String mDisplayName;

		/** Field variable name in java getter/setter function names and parameters */
		private String mJavaVarName;

		private String mTypeDescriptorName;

		/**
		 * @param fieldType the string representing the value type of this field in the generated code.
		 * @param fieldTemplate the string representing the template type of this field in the generated code.
		 * @param fieldName the string representing the name of this field in the generated code.
		 * @param displayName the string representing the name of this field in the error messages and logs in the generated code.
		 * @param typeDescriptorName the name of the type descriptor.
		 * */
		public FieldInfo(final String fieldType, final String fieldTemplate, final String fieldName, final String displayName, final String typeDescriptorName) {
			mJavaTypeName = fieldType;
			mJavaTemplateName = fieldTemplate;
			mVarName = fieldName;
			mJavaVarName = FieldSubReference.getJavaGetterName( mVarName );
			mDisplayName = displayName;
			mTypeDescriptorName = typeDescriptorName;
		}
	}

	private static class TemporalVariable {
		public String type;
		public String typedescriptor;
		int start_pos;
		int use_counter;
		int decoded_for_element;
	}

	private UnionGenerator() {
		// private to disable instantiation
	}

	/**
	 * This function can be used to generate the value class of union/choice types
	 *
	 * defUnionClass in compiler2/union.{h,c}
	 *
	 * @param aData only used to update imports if needed.
	 * @param source where the source code is to be generated.
	 * @param genName the name of the generated class representing the union/choice type.
	 * @param displayName the user readable name of the type to be generated.
	 * @param fieldInfos the list of information about the fields.
	 * @param hasOptional true if the type has an optional field.
	 * @param hasRaw true it the type has raw attributes.
	 * @param raw the raw coding related settings if applicable.
	 * */
	public static void generateValueClass(final JavaGenData aData, final StringBuilder source, final String genName, final String displayName,
			final List<FieldInfo> fieldInfos, final boolean hasOptional, final boolean hasRaw, final RawASTStruct raw) {
		aData.addImport("java.text.MessageFormat");
		aData.addBuiltinTypeImport("Base_Type");
		aData.addBuiltinTypeImport("Text_Buf");
		aData.addBuiltinTypeImport("TtcnError");
		aData.addBuiltinTypeImport("TtcnLogger");
		aData.addBuiltinTypeImport("TTCN_Buffer");
		aData.addBuiltinTypeImport("TTCN_EncDec.error_type");
		aData.addBuiltinTypeImport("TTCN_EncDec.raw_order_t");
		aData.addBuiltinTypeImport("TTCN_EncDec.coding_type");
		aData.addBuiltinTypeImport("RAW.RAW_enc_tr_pos");
		aData.addBuiltinTypeImport("RAW.RAW_enc_tree");
		aData.addBuiltinTypeImport("RAW.RAW_Force_Omit");
		aData.addBuiltinTypeImport("TTCN_EncDec_ErrorContext");

		final boolean rawNeeded = hasRaw; //TODO can be forced optionally if needed

		source.append(MessageFormat.format("public static class {0} extends Base_Type '{'\n", genName));
		generateValueDeclaration(source, genName, fieldInfos);
		generateValueConstructors(source, genName, fieldInfos);
		generateValueCopyValue(source, genName, displayName, fieldInfos);
		generateValueAssign(source, genName, displayName, fieldInfos);
		generateValueCleanup(source);
		generateValueIsChosen(source, displayName);
		generateValueIsBound(source);
		generateValueIsValue(source, fieldInfos);
		generateValueIsPresent(source);
		generateValueOperatorEquals(source, genName, displayName, fieldInfos);
		generateValueNotEquals(source, genName);
		generateValueGetterSetters(source, genName, displayName, fieldInfos);
		generateValueGetSelection(source);
		generateValueLog(source, fieldInfos);
		if (fieldInfos.size() > 0) {
			generateValueSetImplicitOmit(source, fieldInfos);
		}
		generateValueEncodeDecodeText(source, genName, displayName, fieldInfos);
		generateValueEncodeDecode(source, genName, displayName, fieldInfos, rawNeeded, hasRaw, raw);
		//FIXME implement set_param
		source.append( "\t\t//TODO: implement set_param !\n" );
		source.append("}\n");
	}

	/**
	 * This function can be used to generate the template class of union/choice types
	 *
	 * defUnionClass in compiler2/union.{h,c}
	 *
	 * @param aData only used to update imports if needed.
	 * @param source where the source code is to be generated.
	 * @param genName the name of the generated class representing the union/choice type.
	 * @param displayName the user readable name of the type to be generated.
	 * @param fieldInfos the list of information about the fields.
	 * @param hasOptional true if the type has an optional field.
	 * */
	public static void generateTemplateClass(final JavaGenData aData, final StringBuilder source, final String genName, final String displayName,
			final List<FieldInfo> fieldInfos, final boolean hasOptional) {
		aData.addBuiltinTypeImport("Base_Template");
		aData.addBuiltinTypeImport("Text_Buf");
		aData.addImport("java.util.ArrayList");

		source.append(MessageFormat.format("public static class {0}_template extends Base_Template '{'\n", genName));
		generateTemplateDeclaration(source, genName);
		generatetemplateCopyValue(aData, source, genName, displayName, fieldInfos);
		generateTemplateConstructors(source, genName);
		generateTemplateCleanup(source, fieldInfos);
		generateTemplateAssign(source, genName);
		generateTemplateMatch(source, genName, displayName, fieldInfos);
		generateTemplateIsChosen(source, genName, displayName);
		generateTemplateIsValue(source, displayName, fieldInfos);
		generateTemplateValueOf(source, genName, displayName, fieldInfos);
		generateTemplateSetType(source, genName, displayName);
		generateTemplateListItem(source, genName, displayName);
		generateTemplateIsPresent(source);
		generateTemplateMatchOmit(source);
		generateTemplateGetterSetters(source, genName, displayName, fieldInfos);
		generateTemplateLog(source);
		generateTemplateLogMatch(source, genName, displayName, fieldInfos);
		generateTemplateEncodeDecodeText(source, genName, displayName, fieldInfos);

		//FIXME implement set_param
		//FIXME implement check_restriction
		source.append( "\t\t//TODO: implement set_param, check_restriction !\n" );
		source.append("}\n");
	}

	/**
	 * Generate member variables
	 *
	 * @param source where the source code is to be generated.
	 * @param genName the name of the generated class representing the union/choice type.
	 * @param fieldInfos the list of information about the fields.
	 * */
	private static void generateValueDeclaration(final StringBuilder source, final String genName, final List<FieldInfo> fieldInfos) {
		source.append("public enum union_selection_type { UNBOUND_VALUE");
		for (int i = 0 ; i < fieldInfos.size(); i++) {
			source.append(", ");
			source.append(MessageFormat.format(" ALT_{0}", fieldInfos.get(i).mJavaVarName));
		}
		source.append(" };\n");
		source.append("private union_selection_type union_selection;\n");
		source.append("//originally a union which can not be mapped to Java\n");
		source.append("private Base_Type field;\n");
	}

	/**
	 * Generate constructors
	 *
	 * @param source where the source code is to be generated.
	 * @param genName the name of the generated class representing the union/choice type.
	 * @param fieldInfos the list of information about the fields.
	 * */
	private static void generateValueConstructors( final StringBuilder source, final String genName, final List<FieldInfo> fieldInfos){
		source.append(MessageFormat.format("public {0}() '{'\n", genName));
		source.append("union_selection = union_selection_type.UNBOUND_VALUE;\n");
		source.append("};\n");
		source.append(MessageFormat.format("public {0}(final {0} otherValue) '{'\n", genName));
		source.append("copy_value(otherValue);\n");
		source.append("};\n\n");
	}

	/**
	 * Generate the copy_value function
	 *
	 * @param source where the source code is to be generated.
	 * @param genName the name of the generated class representing the union/choice type.
	 * @param displayName the user readable name of the type to be generated.
	 * @param fieldInfos the list of information about the fields.
	 * */
	private static void generateValueCopyValue(final StringBuilder source, final String genName, final String displayName, final List<FieldInfo> fieldInfos) {
		source.append(MessageFormat.format("private void copy_value(final {0} otherValue) '{'\n", genName));
		if (!fieldInfos.isEmpty()) {
			source.append("switch(otherValue.union_selection){\n");
			for (int i = 0 ; i < fieldInfos.size(); i++) {
				final FieldInfo fieldInfo = fieldInfos.get(i);
				source.append(MessageFormat.format("case ALT_{0}:\n", fieldInfo.mJavaVarName));
				source.append(MessageFormat.format("field = new {0}(({0})otherValue.field);\n", fieldInfo.mJavaTypeName));
				source.append("break;\n");
			}
			source.append("default:\n");
			source.append(MessageFormat.format("throw new TtcnError(\"Assignment of an unbound union value of type {0}.\");\n", displayName));
			source.append("}\n");
		}
		source.append("union_selection = otherValue.union_selection;\n");
		source.append("}\n\n");
	}

	/**
	 * Generate assign functions
	 *
	 * @param source where the source code is to be generated.
	 * @param genName the name of the generated class representing the union/choice type.
	 * @param displayName the user readable name of the type to be generated.
	 * @param fieldInfos the list of information about the fields.
	 * */
	private static void generateValueAssign(final StringBuilder source, final String genName, final String displayName, final List<FieldInfo> fieldInfos) {
		source.append("//originally operator=\n");
		source.append(MessageFormat.format("public {0} assign( final {0} otherValue ) '{'\n", genName));
		source.append("if (otherValue != this) {\n");
		source.append("cleanUp();\n");
		source.append("copy_value(otherValue);\n");
		source.append("}\n\n");
		source.append("return this;\n");
		source.append("}\n");

		source.append("@Override\n");
		source.append(MessageFormat.format("public {0} assign( final Base_Type otherValue ) '{'\n", genName));
		source.append(MessageFormat.format("if (otherValue instanceof {0}) '{'\n", genName));
		source.append(MessageFormat.format("return assign(({0})otherValue);\n", genName));
		source.append("}\n");
		source.append(MessageFormat.format("throw new TtcnError(\"Internal Error: value can not be cast to {0}.\");\n", displayName));

		source.append("}\n\n");
	}

	/**
	 * Generate the clean_up function
	 *
	 * @param source where the source code is to be generated.
	 * */
	private static void generateValueCleanup(final StringBuilder source) {
		source.append("//originally clean_up\n");
		source.append("public void cleanUp() {\n");
		source.append("field = null;\n");
		source.append("union_selection = union_selection_type.UNBOUND_VALUE;\n");
		source.append("}\n\n");
	}

	/**
	 * Generate the isChosen function
	 *
	 * @param source where the source code is to be generated.
	 * @param displayName the user readable name of the type to be generated.
	 * */
	private static void generateValueIsChosen(final StringBuilder source, final String displayName) {
		source.append("public boolean isChosen(final union_selection_type checked_selection) {\n");
		source.append("if(checked_selection == union_selection_type.UNBOUND_VALUE) {\n");
		source.append(MessageFormat.format("throw new TtcnError(\"Internal error: Performing ischosen() operation on an invalid field of union type {0}.\");\n", displayName));
		source.append("}\n");
		source.append("return union_selection == checked_selection;\n");
		source.append("}\n\n");
	}

	/**
	 * Generate the isBound function
	 *
	 * @param source where the source code is to be generated.
	 * */
	private static void generateValueIsBound(final StringBuilder source) {
		source.append("public boolean isBound() {\n");
		source.append("return union_selection != union_selection_type.UNBOUND_VALUE;\n");
		source.append("}\n\n");
	}

	/**
	 * Generate the isValue function
	 *
	 * @param source where the source code is to be generated.
	 * @param fieldInfos the list of information about the fields.
	 * */
	private static void generateValueIsValue(final StringBuilder source, final List<FieldInfo> fieldInfos) {
		source.append("public boolean isValue() {\n");
		source.append("switch(union_selection) {\n");
		source.append("case UNBOUND_VALUE:\n");
		source.append("return false;\n");
		for (int i = 0 ; i < fieldInfos.size(); i++) {
			final FieldInfo fieldInfo = fieldInfos.get(i);
			source.append(MessageFormat.format("case ALT_{0}:\n", fieldInfo.mJavaVarName));
			source.append("return field.isValue();\n");
		}

		source.append("default:\n");
		source.append("throw new TtcnError(\"Invalid selection in union is_bound\");\n");
		source.append("}\n");
		source.append("}\n\n");
	}

	/**
	 * Generate the isPresent function
	 *
	 * @param source where the source code is to be generated.
	 * */
	private static void generateValueIsPresent(final StringBuilder source) {
		source.append("public boolean isPresent() {\n");
		source.append("return isBound();\n");
		source.append("}\n\n");
	}

	/**
	 * Generate equals operators (originally ==)
	 *
	 * @param source where the source code is to be generated.
	 * @param genName the name of the generated class representing the union/choice type.
	 * @param displayName the user readable name of the type to be generated.
	 * @param fieldInfos the list of information about the fields.
	 * */
	private static void generateValueOperatorEquals(final StringBuilder source, final String genName, final String displayName,
			final List<FieldInfo> fieldInfos) {
		source.append("//originally operator==\n");
		source.append(MessageFormat.format("public boolean operatorEquals( final {0} otherValue ) '{'\n", genName));
		source.append("if (union_selection == union_selection_type.UNBOUND_VALUE) {\n");
		source.append(MessageFormat.format("throw new TtcnError( \"The left operand of comparison is an unbound value of union type {0}.\" );\n", displayName));
		source.append("}\n");
		source.append("if (otherValue.union_selection == union_selection_type.UNBOUND_VALUE) {\n");
		source.append(MessageFormat.format("throw new TtcnError( \"The right operand of comparison is an unbound value of union type {0}.\" );\n", displayName));
		source.append("}\n");
		source.append("if (union_selection != otherValue.union_selection) {\n");
		source.append("return false;\n");

		source.append("}\n");
		source.append("switch(union_selection) {\n");
		for (int i = 0 ; i < fieldInfos.size(); i++) {
			final FieldInfo fieldInfo = fieldInfos.get(i);
			source.append(MessageFormat.format("case ALT_{0}:\n", fieldInfo.mJavaVarName));
			source.append(MessageFormat.format("return (({0})field).operatorEquals(({0})otherValue.field);\n", fieldInfo.mJavaTypeName));
		}


		source.append("default:\n");
		source.append("return false;\n");
		source.append("}\n");
		source.append("}\n");

		source.append("@Override\n");
		source.append("public boolean operatorEquals( final Base_Type otherValue ) {\n");
		source.append(MessageFormat.format("if (otherValue instanceof {0}) '{'\n", genName));
		source.append(MessageFormat.format("return operatorEquals(({0})otherValue);\n", genName));
		source.append("}\n");
		source.append(MessageFormat.format("throw new TtcnError(\"Internal Error: value can not be cast to {0}.\");\n", displayName));
		source.append("}\n\n");
	}

	/**
	 * Generate not equals operators (originally !=)
	 *
	 * @param source where the source code is to be generated.
	 * @param genName the name of the generated class representing the union/choice type.
	 * */
	private static void generateValueNotEquals(final StringBuilder source, final String genName) {
		source.append("//originally operator!=\n");
		source.append(MessageFormat.format("public boolean operatorNotEquals( final {0} otherValue ) '{'\n", genName));
		source.append("return !operatorEquals(otherValue);\n");
		source.append("}\n\n");
	}

	/**
	 * Generate getters/setters
	 *
	 * @param source where the source code is to be generated.
	 * @param genName the name of the generated class representing the union/choice type.
	 * @param displayName the user readable name of the type to be generated.
	 * @param fieldInfos the list of information about the fields.
	 * */
	private static void generateValueGetterSetters(final StringBuilder source, final String genName, final String displayName,
			final List<FieldInfo> fieldInfos) {
		for (int i = 0 ; i < fieldInfos.size(); i++) {
			final FieldInfo fieldInfo = fieldInfos.get(i);
			source.append(MessageFormat.format("public {0} get{1}() '{'\n", fieldInfo.mJavaTypeName, fieldInfo.mJavaVarName));
			source.append(MessageFormat.format("if (union_selection != union_selection_type.ALT_{0}) '{'\n", fieldInfo.mJavaVarName));
			source.append("cleanUp();\n");
			source.append(MessageFormat.format("field = new {0}();\n", fieldInfo.mJavaTypeName));
			source.append(MessageFormat.format("union_selection = union_selection_type.ALT_{0};\n", fieldInfo.mJavaVarName));
			source.append("}\n");
			source.append(MessageFormat.format("return ({0})field;\n", fieldInfo.mJavaTypeName));
			source.append("}\n\n");

			source.append(MessageFormat.format("public {0} constGet{1}() '{'\n", fieldInfo.mJavaTypeName, fieldInfo.mJavaVarName));
			source.append(MessageFormat.format("if (union_selection != union_selection_type.ALT_{0}) '{'\n", fieldInfo.mJavaVarName));
			source.append(MessageFormat.format("throw new TtcnError(\"Using non-selected field {0} in a value of union type {1}.\");\n", fieldInfo.mDisplayName ,displayName));
			source.append("}\n");
			source.append(MessageFormat.format("return ({0})field;\n", fieldInfo.mJavaTypeName));
			source.append("}\n\n");
		}
	}

	/**
	 * Generate the get_selection function
	 *
	 * @param source where the source code is to be generated.
	 * */
	private static void generateValueGetSelection(final StringBuilder source) {
		source.append("public union_selection_type get_selection() {\n");
		source.append("return union_selection;\n");
		source.append("}\n");
	}

	/**
	 * Generate log
	 *
	 * @param source where the source code is to be generated.
	 * @param fieldInfos the list of information about the fields.
	 * */
	private static void generateValueLog(final StringBuilder source, final List<FieldInfo> fieldInfos) {
		source.append("public void log() {\n");
		source.append("switch (union_selection) {\n");
		for (int i = 0 ; i < fieldInfos.size(); i++) {
			final FieldInfo fieldInfo = fieldInfos.get(i);
			source.append(MessageFormat.format("case ALT_{0}:\n", fieldInfo.mJavaVarName));
			source.append(MessageFormat.format("TtcnLogger.log_event_str(\"'{' {0} := \");\n", fieldInfo.mDisplayName));
			source.append("field.log();\n");
			source.append("TtcnLogger.log_event_str(\" }\");\n");
			source.append("break;\n");
		}

		source.append("default:\n");
		source.append("TtcnLogger.log_event_unbound();\n");
		source.append("break;\n");
		source.append("}\n");
		source.append("}\n");
	}

	/**
	 * Generate set_implicit_omit.
	 *
	 * @param source where the source code is to be generated.
	 * @param fieldInfos the list of information about the fields.
	 */
	private static void generateValueSetImplicitOmit(final StringBuilder source, final List<FieldInfo> fieldInfos) {
		source.append("@Override\n");
		source.append("public void set_implicit_omit() {\n");
		source.append("switch (union_selection) {\n");
		for (int i = 0 ; i < fieldInfos.size(); i++) {
			final FieldInfo fieldInfo = fieldInfos.get(i);

			source.append(MessageFormat.format("case ALT_{0}:\n", fieldInfo.mJavaVarName));
		}
		source.append("field.set_implicit_omit();\n");
		source.append("break;\n");

		source.append("default:\n");
		source.append("break;\n");
		source.append("}\n");
		source.append("}\n\n");
	}

	/**
	 * Generate encode_text/decode_text
	 *
	 * @param source where the source code is to be generated.
	 * @param genName the name of the generated class representing the union/choice type.
	 * @param displayName the user readable name of the type to be generated.
	 * @param fieldInfos the list of information about the fields.
	 * */
	private static void generateValueEncodeDecodeText(final StringBuilder source, final String genName, final String displayName, final List<FieldInfo> fieldInfos) {
		source.append("@Override\n");
		source.append("public void encode_text(final Text_Buf text_buf) {\n");
		source.append("switch(union_selection) {\n");
		for (int i = 0 ; i < fieldInfos.size(); i++) {
			final FieldInfo fieldInfo = fieldInfos.get(i);
			source.append(MessageFormat.format("case ALT_{0}:\n", fieldInfo.mJavaVarName));
			source.append(MessageFormat.format("text_buf.push_int({0});\n", i));
			source.append("break;\n");
		}

		source.append("default:\n");
		source.append(MessageFormat.format("throw new TtcnError(\"Text encoder: Encoding an unbound value of union type {0}.\");\n", displayName));
		source.append("}\n");
		if (fieldInfos.size() > 0) {
			source.append("field.encode_text(text_buf);\n");
		}
		source.append("}\n");

		source.append("@Override\n");
		source.append("public void decode_text(final Text_Buf text_buf) {\n");
		source.append("final int temp = text_buf.pull_int().getInt();\n");
		source.append("switch(temp) {\n");
		for (int i = 0 ; i < fieldInfos.size(); i++) {
			final FieldInfo fieldInfo = fieldInfos.get(i);
			source.append(MessageFormat.format("case {0}:\n", i));
			source.append(MessageFormat.format("get{0}().decode_text(text_buf);\n", fieldInfo.mJavaVarName));
			source.append("break;\n");
		}

		source.append("default:\n");
		source.append(MessageFormat.format("throw new TtcnError(\"Text decoder: Unrecognized union selector was received for type {0}.\");\n", displayName));
		source.append("}\n");
		source.append("}\n");
	}

	/**
	 * Generate encode/decode
	 *
	 * @param source where the source code is to be generated.
	 * @param genName the name of the generated class representing the union/choice type.
	 * @param displayName the user readable name of the type to be generated.
	 * @param fieldInfos the list of information about the fields.
	 * @param rawNeeded true if encoding/decoding for RAW is to be generated.
	 * @param hasRaw true if the union has raw attributes.
	 * @param raw the raw attributes or null.
	 * */
	private static void generateValueEncodeDecode(final StringBuilder source, final String genName, final String displayName, final List<FieldInfo> fieldInfos, final boolean rawNeeded, final boolean hasRaw, final RawASTStruct raw) {
		source.append("@Override\n");
		source.append("public void encode(final TTCN_Typedescriptor p_td, final TTCN_Buffer p_buf, final coding_type p_coding, final int flavour) {\n");
		source.append("switch (p_coding) {\n");
		source.append("case CT_RAW: {\n");
		source.append("final TTCN_EncDec_ErrorContext errorContext = new TTCN_EncDec_ErrorContext(\"While RAW-encoding type '%s': \", p_td.name);\n");
		source.append("if (p_td.raw == null) {\n");
		source.append("TTCN_EncDec_ErrorContext.error_internal(\"No RAW descriptor available for type '%s'.\", p_td.name);\n");
		source.append("}\n");
		source.append("final RAW_enc_tr_pos rp = new RAW_enc_tr_pos(0, null);\n");
		source.append("final RAW_enc_tree root = new RAW_enc_tree(true, null, rp, 1, p_td.raw);\n");
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

			source.append("@Override\n");
			source.append("public int RAW_encode(final TTCN_Typedescriptor p_td, final RAW_enc_tree myleaf) {\n");
			source.append("int encoded_length = 0;\n");
			source.append("myleaf.isleaf = false;\n");
			source.append(MessageFormat.format("myleaf.num_of_nodes = {0};\n", fieldInfos.size()));
			source.append(MessageFormat.format("myleaf.nodes = new RAW_enc_tree[{0}];\n", fieldInfos.size()));
			source.append("switch (union_selection) {\n");
			for (int i = 0 ; i < fieldInfos.size(); i++) {
				final FieldInfo fieldInfo = fieldInfos.get(i);
				source.append(MessageFormat.format("case ALT_{0}:\n", fieldInfo.mJavaVarName));
				source.append(MessageFormat.format("myleaf.nodes[{0}] = new RAW_enc_tree(true, myleaf, myleaf.curr_pos, {0}, {1}_descr_.raw);\n", i, fieldInfo.mTypeDescriptorName));
				source.append(MessageFormat.format("encoded_length = field.RAW_encode({0}_descr_, myleaf.nodes[{1}]);\n", fieldInfo.mTypeDescriptorName, i));
				source.append(MessageFormat.format("myleaf.nodes[{0}].coding_descr = {1}_descr_;\n", i, fieldInfo.mTypeDescriptorName));

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
			source.append("return encoded_length;\n");
			source.append("}\n\n");

			source.append("@Override\n");
			source.append("public int RAW_decode(final TTCN_Typedescriptor p_td, final TTCN_Buffer buff, int limit, final raw_order_t top_bit_ord, final boolean no_err, final int sel_field, final boolean first_call, final RAW_Force_Omit force_omit) {\n");
			source.append("final int prepaddlength = buff.increase_pos_padd(p_td.raw.prepadding);\n");
			source.append("limit -= prepaddlength;\n");
			source.append("int decoded_length = 0;\n");
			source.append("final int starting_pos = buff.get_pos_bit();\n");
			source.append("if (sel_field != -1) {\n");
			source.append("switch (sel_field) {\n");
			for (int i = 0 ; i < fieldInfos.size(); i++) {
				final FieldInfo fieldInfo = fieldInfos.get(i);
				source.append(MessageFormat.format("case {0}: '{'\n", i));
				source.append(MessageFormat.format("RAW_Force_Omit field_force_omit = new RAW_Force_Omit({0}, force_omit, {1}_descr_.raw.forceomit);\n", i, fieldInfo.mTypeDescriptorName));
				source.append(MessageFormat.format("decoded_length = get{0}().RAW_decode({1}_descr_, buff, limit, top_bit_ord, no_err, -1, true, field_force_omit);\n", fieldInfo.mJavaVarName, fieldInfo.mTypeDescriptorName));
				source.append("break;\n");
				source.append("}\n");
			}
			source.append("default:\n");
			source.append("break;\n");
			source.append("}\n");
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
					source.append(MessageFormat.format("{0} temporal_{1} = new {0}();\n", tempVariable.type, i));
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
								source.append(MessageFormat.format("{0} temporal_{1} = new {0}();\n", tempVariable.type, variableIndex));
								source.append(MessageFormat.format("int decoded_{0}_length;\n", variableIndex));
							}
							if (tempVariable.decoded_for_element == -1) {
								source.append(MessageFormat.format("buff.set_pos_bit(starting_pos + {0});\n", cur_field_list.start_pos));
								source.append(MessageFormat.format("decoded_{0}_length = temporal_{0}.RAW_decode({1}_descr_, buff, limit, top_bit_ord, true, -1, true, null);\n", variableIndex, tempVariable.typedescriptor));
							}
							tempVariable.decoded_for_element = i;
							source.append(MessageFormat.format("if (decoded_{0}_length > 0) '{'\n", variableIndex));
							source.append(MessageFormat.format("if (temporal_{0}.operatorEquals({1})", variableIndex, cur_field_list.expression.expression));
							for (int k = j + 1; k < cur_choice.fields.size(); k++) {
								final rawAST_coding_field_list tempFieldList = cur_choice.fields.get(k);
								if (tempFieldList.temporal_variable_index == variableIndex) {
									source.append(MessageFormat.format(" || temporal_{0}.operatorEquals({1})", variableIndex, tempFieldList.expression.expression));
								}
							}
							source.append(") {\n");
							source.append("buff.set_pos_bit(starting_pos);\n");
							source.append(MessageFormat.format("RAW_Force_Omit field_force_omit = new RAW_Force_Omit({0}, force_omit, {1}_descr_.raw.forceomit);\n", i, fieldInfo.mTypeDescriptorName));
							source.append(MessageFormat.format("decoded_length = get{0}().RAW_decode({1}_descr_, buff, limit, top_bit_ord, true, -1, true, field_force_omit);\n", fieldInfo.mJavaVarName, fieldInfo.mTypeDescriptorName));
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
							source.append(MessageFormat.format("RAW_Force_Omit field_force_omit = new RAW_Force_Omit({0}, force_omit, {1}_descr_.raw.forceomit);\n", i, fieldInfo.mTypeDescriptorName));
							source.append(MessageFormat.format("decoded_length = get{0}().RAW_decode({1}_descr_, buff, limit, top_bit_ord, true, -1, true, field_force_omit);\n", fieldInfo.mJavaVarName, fieldInfo.mTypeDescriptorName));
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
					source.append(MessageFormat.format("RAW_Force_Omit field_{0}_force_omit = new RAW_Force_Omit({0}, force_omit, {1}_descr_.raw.forceomit);\n", i, fieldInfo.mTypeDescriptorName));
					source.append(MessageFormat.format("decoded_length = get{0}().RAW_decode({1}_descr_, buff, limit, top_bit_ord, true, -1, true, field_{2}_force_omit);\n", fieldInfo.mJavaVarName, fieldInfo.mTypeDescriptorName, i));
					source.append("if (decoded_length >= 0) {\n");
					source.append("if (");
					genRawFieldChecker(source, cur_choice, true);
					source.append(") {\n");
					source.append("return decoded_length + buff.increase_pos_padd(p_td.raw.padding) + prepaddlength;\n");
					source.append("}\n");
					source.append("}\n");
				}
			}
			for (int i = 0 ; i < fieldInfos.size(); i++) {
				if (tag_type[i] == 0) {
					final FieldInfo fieldInfo = fieldInfos.get(i);
					source.append("buff.set_pos_bit(starting_pos);\n");
					source.append(MessageFormat.format("RAW_Force_Omit field_{0}_force_omit = new RAW_Force_Omit({0}, force_omit, {1}_descr_.raw.forceomit);\n", i, fieldInfo.mTypeDescriptorName));
					source.append(MessageFormat.format("decoded_length = get{0}().RAW_decode({1}_descr_, buff, limit, top_bit_ord, true, -1, true, field_{2}_force_omit);\n", fieldInfo.mJavaVarName, fieldInfo.mTypeDescriptorName, i));
					source.append("if (decoded_length >= 0) {\n");
					source.append("return decoded_length + buff.increase_pos_padd(p_td.raw.padding) + prepaddlength;\n");
					source.append("}\n");
				}
			}

			source.append("}\n");
			source.append("cleanUp();\n");
			source.append("return -1;\n");
			source.append("}\n\n");
		}
	}

	/**
	 * Generate member variables
	 *
	 * @param source where the source code is to be generated.
	 * @param genName the name of the generated class representing the union/choice type.
	 * */
	private static void generateTemplateDeclaration(final StringBuilder source, final String genName) {
		source.append("//if single value which value?\n");
		source.append(MessageFormat.format("private {0}.union_selection_type single_value_union_selection;\n", genName));
		source.append("//originally a union which can not be mapped to Java\n");
		source.append("private Base_Template single_value;\n");
		source.append("// value_list part\n");
		source.append(MessageFormat.format("private ArrayList<{0}_template> value_list;\n\n", genName));
	}

	/**
	 * Generate constructors
	 *
	 * @param source where the source code is to be generated.
	 * @param genName the name of the generated class representing the union/choice type.
	 * */
	private static void generateTemplateConstructors( final StringBuilder source, final String genName){
		source.append(MessageFormat.format("public {0}_template() '{'\n", genName));
		source.append("}\n");
		source.append(MessageFormat.format("public {0}_template(final template_sel other_value) '{'\n", genName));
		source.append("super(other_value);\n");
		source.append("checkSingleSelection(other_value);\n");
		source.append("}\n");
		source.append(MessageFormat.format("public {0}_template(final {0} other_value) '{'\n", genName));
		source.append("copy_value(other_value);\n");
		source.append("}\n");
		source.append(MessageFormat.format("public {0}_template(final {0}_template other_value) '{'\n", genName));
		source.append("copy_template(other_value);\n");
		source.append("}\n\n");
	}

	/**
	 * Generate the copy_value and copy_template functions
	 *
	 * @param aData only used to update imports if needed.
	 * @param source where the source code is to be generated.
	 * @param genName the name of the generated class representing the union/choice type.
	 * @param displayName the user readable name of the type to be generated.
	 * @param fieldInfos the list of information about the fields.
	 * */
	private static void generatetemplateCopyValue(final JavaGenData aData, final StringBuilder source, final String genName, final String displayName, final List<FieldInfo> fieldInfos) {
		source.append(MessageFormat.format("private void copy_value(final {0} other_value) '{'\n", genName));
		source.append("single_value_union_selection = other_value.union_selection;\n");
		if (!fieldInfos.isEmpty()) {
			source.append("switch (other_value.union_selection) {\n");
			for (int i = 0 ; i < fieldInfos.size(); i++) {
				final FieldInfo fieldInfo = fieldInfos.get(i);
				source.append(MessageFormat.format("case ALT_{0}:\n", fieldInfo.mJavaVarName));
				source.append(MessageFormat.format("single_value = new {0}(({1})other_value.field);\n", fieldInfo.mJavaTemplateName, fieldInfo.mJavaTypeName));
				source.append("break;\n");
			}
			source.append("default:\n");
			source.append(MessageFormat.format("throw new TtcnError(\"Initializing a template with an unbound value of type {0}.\");\n", displayName));
			source.append("}\n");
		}
		source.append("set_selection(template_sel.SPECIFIC_VALUE);\n");
		source.append("}\n");

		source.append(MessageFormat.format("private void copy_template(final {0}_template other_value) '{'\n", genName));
		source.append("switch (other_value.templateSelection) {\n");
		source.append("case SPECIFIC_VALUE:\n");
		source.append("single_value_union_selection = other_value.single_value_union_selection;\n");
		if (!fieldInfos.isEmpty()) {
			source.append("switch (single_value_union_selection) {\n");
			for (int i = 0 ; i < fieldInfos.size(); i++) {
				final FieldInfo fieldInfo = fieldInfos.get(i);
				source.append(MessageFormat.format("case ALT_{0}:\n", fieldInfo.mJavaVarName));
				source.append(MessageFormat.format("single_value = new {0}(other_value.get{1}());\n", fieldInfo.mJavaTemplateName, fieldInfo.mJavaVarName));
				source.append("break;\n");
			}
			source.append("default:\n");
			source.append(MessageFormat.format("throw new TtcnError(\"Internal error: Invalid union selector in a specific value when copying a template of type {0}.\");\n", displayName));
			source.append("}\n");
		}
		source.append("break;\n");
		source.append("case OMIT_VALUE:\n");
		source.append("case ANY_VALUE:\n");
		source.append("case ANY_OR_OMIT:\n");
		source.append("break;\n");
		source.append("case VALUE_LIST:\n");
		source.append("case COMPLEMENTED_LIST:\n");
		source.append(MessageFormat.format("value_list = new ArrayList<{0}_template>(other_value.value_list.size());\n", genName));
		source.append("for(int i = 0; i < other_value.value_list.size(); i++) {\n");
		source.append(MessageFormat.format("final {0}_template temp = new {0}_template(other_value.value_list.get(i));\n", genName));
		source.append("value_list.add(temp);\n");
		source.append("}\n");
		source.append("break;\n");
		source.append("default:\n");
		source.append(MessageFormat.format("throw new TtcnError(\"Copying an uninitialized template of union type {0}.\");\n", displayName));
		source.append("}\n");
		source.append("set_selection(other_value);\n");
		source.append("}\n\n");
	}

	/**
	 * Generate the clean_up function
	 *
	 * @param source where the source code is to be generated.
	 * @param fieldInfos the list of information about the fields.
	 * */
	private static void generateTemplateCleanup(final StringBuilder source, final List<FieldInfo> fieldInfos) {
		source.append("@Override\n");
		source.append("public void cleanUp() {\n");
		source.append("switch(templateSelection) {\n");
		source.append("case SPECIFIC_VALUE:\n");
		if (!fieldInfos.isEmpty()) {
			source.append("switch(single_value_union_selection) {\n");
			for (int i = 0 ; i < fieldInfos.size(); i++) {
				final FieldInfo fieldInfo = fieldInfos.get(i);
				source.append(MessageFormat.format("case ALT_{0}:\n", fieldInfo.mJavaVarName));
				source.append(MessageFormat.format("(({0})single_value).cleanUp();\n", fieldInfo.mJavaTemplateName));
				source.append("break;\n");
			}
			source.append("default:\n");
			source.append("break;\n");
			source.append("}\n");
		}
		source.append("single_value = null;\n");
		source.append("break;\n");
		source.append("case VALUE_LIST:\n");
		source.append("case COMPLEMENTED_LIST:\n");
		source.append("value_list.clear();\n");
		source.append("value_list = null;\n");
		source.append("break;\n");
		source.append("default:\n");
		source.append("break;\n");
		source.append("}\n");
		source.append("templateSelection = template_sel.UNINITIALIZED_TEMPLATE;\n");
		source.append("}\n\n");
	}

	/**
	 * Generate assign functions
	 *
	 * @param source where the source code is to be generated.
	 * @param genName the name of the generated class representing the union/choice type.
	 * */
	private static void generateTemplateAssign(final StringBuilder source, final String genName) {
		source.append("//originally operator=\n");
		source.append(MessageFormat.format("public {0}_template assign( final template_sel other_value ) '{'\n", genName));
		source.append("checkSingleSelection(other_value);\n");
		source.append("cleanUp();\n");
		source.append("set_selection(other_value);\n");
		source.append("return this;\n");
		source.append("}\n\n");

		source.append("//originally operator=\n");
		source.append(MessageFormat.format("public {0}_template assign( final {0} other_value ) '{'\n", genName));
		source.append("cleanUp();\n");
		source.append("copy_value(other_value);\n");
		source.append("return this;\n");
		source.append("}\n\n");

		source.append("//originally operator=\n");
		source.append(MessageFormat.format("public {0}_template assign( final {0}_template other_value ) '{'\n", genName));
		source.append("if (other_value != this) {\n");
		source.append("cleanUp();\n");
		source.append("copy_template(other_value);\n");
		source.append("}\n");
		source.append("return this;\n");
		source.append("}\n\n");

		source.append("@Override\n");
		source.append(MessageFormat.format("public {0}_template assign( final Base_Type otherValue ) '{'\n", genName));
		source.append(MessageFormat.format("if (otherValue instanceof {0}) '{'\n", genName));
		source.append(MessageFormat.format("return assign(({0})otherValue);\n", genName));
		source.append("}\n");
		source.append(MessageFormat.format("throw new TtcnError(\"Internal Error: value can not be cast to {0}.\");\n", genName));
		source.append("}\n\n");

		source.append("@Override\n");
		source.append(MessageFormat.format("public {0}_template assign( final Base_Template otherValue ) '{'\n", genName));
		source.append(MessageFormat.format("if (otherValue instanceof {0}_template) '{'\n", genName));
		source.append(MessageFormat.format("return assign(({0}_template)otherValue);\n", genName));
		source.append("}\n");
		source.append(MessageFormat.format("throw new TtcnError(\"Internal Error: value can not be cast to {0}_template.\");\n", genName));
		source.append("}\n\n");

		//FIXME implement optional parameter version
	}

	/**
	 * Generate the match function
	 *
	 * @param source where the source code is to be generated.
	 * @param genName the name of the generated class representing the union/choice type.
	 * @param displayName the user readable name of the type to be generated.
	 * @param fieldInfos the list of information about the fields.
	 * */
	private static void generateTemplateMatch(final StringBuilder source, final String genName, final String displayName, final List<FieldInfo> fieldInfos) {
		source.append("// originally match\n");
		source.append(MessageFormat.format("public boolean match(final {0} other_value) '{'\n", genName));
		source.append("return match(other_value, false);\n");
		source.append("}\n\n");

		source.append("// originally match\n");
		source.append(MessageFormat.format("public boolean match(final {0} other_value, final boolean legacy) '{'\n", genName));
		source.append("if(!other_value.isBound()) {\n");
		source.append("return false;\n");
		source.append("}\n");

		source.append("switch (templateSelection) {\n");
		source.append("case ANY_VALUE:\n");
		source.append("case ANY_OR_OMIT:\n");
		source.append("return true;\n");
		source.append("case OMIT_VALUE:\n");
		source.append("return false;\n");
		source.append("case SPECIFIC_VALUE:\n");
		source.append(MessageFormat.format("final {0}.union_selection_type value_selection = other_value.get_selection();\n", genName));
		source.append(MessageFormat.format("if (value_selection == {0}.union_selection_type.UNBOUND_VALUE) '{'\n", genName));
		source.append("return false;\n");
		source.append("}\n");
		source.append("if (value_selection != single_value_union_selection) {\n");
		source.append("return false;\n");
		source.append("}\n");
		source.append("switch(value_selection) {\n");
		for (int i = 0 ; i < fieldInfos.size(); i++) {
			final FieldInfo fieldInfo = fieldInfos.get(i);
			source.append(MessageFormat.format("case ALT_{0}:\n", fieldInfo.mJavaVarName));
			source.append(MessageFormat.format("return (({0})single_value).match(other_value.get{1}(), legacy);\n", fieldInfo.mJavaTemplateName, fieldInfo.mJavaVarName));
		}

		source.append("default:\n");
		source.append(MessageFormat.format("throw new TtcnError(\"Internal error: Invalid selector in a specific value when matching a template of union type {0}.\");\n", displayName));
		source.append("}\n");
		source.append("case VALUE_LIST:\n");
		source.append("case COMPLEMENTED_LIST:\n");
		source.append("for(int i = 0 ; i < value_list.size(); i++) {\n");
		source.append("if(value_list.get(i).match(other_value, legacy)) {\n");
		source.append("return templateSelection == template_sel.VALUE_LIST;\n");
		source.append("}\n");
		source.append("}\n");
		source.append("return templateSelection == template_sel.COMPLEMENTED_LIST;\n");
		source.append("default:\n");
		source.append("throw new TtcnError(\"Matching with an uninitialized/unsupported integer template.\");\n");
		source.append("}\n");
		source.append("}\n");

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
	 * Generate the isChosen function
	 *
	 * @param source where the source code is to be generated.
	 * @param genName the name of the generated class representing the union/choice type.
	 * @param displayName the user readable name of the type to be generated.
	 * */
	private static void generateTemplateIsChosen(final StringBuilder source, final String genName, final String displayName) {
		source.append(MessageFormat.format("public boolean isChosen(final {0}.union_selection_type checked_selection) '{'\n", genName));
		source.append(MessageFormat.format("if(checked_selection == {0}.union_selection_type.UNBOUND_VALUE) '{'\n", genName));
		source.append(MessageFormat.format("throw new TtcnError(\"Internal error: Performing ischosen() operation on an invalid field of union type {0}.\");\n", displayName));
		source.append("}\n");
		source.append("switch(templateSelection) {\n");
		source.append("case SPECIFIC_VALUE:\n");
		source.append(MessageFormat.format("if (single_value_union_selection == {0}.union_selection_type.UNBOUND_VALUE) '{'\n", genName));
		source.append(MessageFormat.format("throw new TtcnError(\"Internal error: Invalid selector in a specific value when performing ischosen() operation on a template of union type {0}.\");\n", displayName));
		source.append("}\n");
		source.append("return single_value_union_selection == checked_selection;\n");
		source.append("case VALUE_LIST:\n");
		source.append("if (value_list.isEmpty()) {\n");
		source.append(MessageFormat.format("throw new TtcnError(\"Internal error: Performing ischosen() operation on a template of union type {0} containing an empty list.\");\n", displayName));
		source.append("}\n");
		source.append("for (int i = 0; i < value_list.size(); i++) {\n");
		source.append("if(!value_list.get(i).isChosen(checked_selection)) {\n");
						//FIXME this is incorrect in the compiler
		source.append("return false;\n");
		source.append("}\n");
		source.append("}\n");
		source.append("return true;\n");
		source.append("default:\n");
		source.append("return false;\n");
		source.append("}\n");
		source.append("}\n\n");
	}

	/**
	 * Generate the isValue function
	 *
	 * @param source where the source code is to be generated.
	 * @param displayName the user readable name of the type to be generated.
	 * @param fieldInfos the list of information about the fields.
	 * */
	private static void generateTemplateIsValue(final StringBuilder source, final String displayName, final List<FieldInfo> fieldInfos) {
		source.append("@Override\n");
		source.append("public boolean isValue() {\n");
		source.append("if (templateSelection != template_sel.SPECIFIC_VALUE || is_ifPresent) {\n");
		source.append("return false;\n");
		source.append("}\n");
		source.append("switch(single_value_union_selection) {\n");
		for (int i = 0 ; i < fieldInfos.size(); i++) {
			final FieldInfo fieldInfo = fieldInfos.get(i);
			source.append(MessageFormat.format("case ALT_{0}:\n", fieldInfo.mJavaVarName));
			source.append(MessageFormat.format("return (({0})single_value).isValue();\n", fieldInfo.mJavaTemplateName));
		}
		source.append("default:\n");
		source.append(MessageFormat.format("throw new TtcnError(\"Internal error: Invalid selector in a specific value when performing is_value operation on a template of union type {0}.\");\n", displayName));
		source.append("}\n");
		source.append("}\n\n");
	}

	/**
	 * Generate the valueOf function
	 *
	 * @param source where the source code is to be generated.
	 * @param genName the name of the generated class representing the union/choice type.
	 * @param displayName the user readable name of the type to be generated.
	 * @param fieldInfos the list of information about the fields.
	 * */
	private static void generateTemplateValueOf(final StringBuilder source, final String genName, final String displayName, final List<FieldInfo> fieldInfos) {
		source.append(MessageFormat.format("public {0} valueOf() '{'\n", genName));
		source.append("if (templateSelection != template_sel.SPECIFIC_VALUE || is_ifPresent) {\n");
		source.append(MessageFormat.format("throw new TtcnError(\"Performing a valueof or send operation on a non-specific template of union type {0}.\");\n", displayName));
		source.append("}\n");
		if (!fieldInfos.isEmpty()) {
			source.append(MessageFormat.format("final {0} ret_val = new {0}();\n", genName));
		}
		source.append("switch(single_value_union_selection) {\n");
		for (int i = 0 ; i < fieldInfos.size(); i++) {
			final FieldInfo fieldInfo = fieldInfos.get(i);
			source.append(MessageFormat.format("case ALT_{0}:\n", fieldInfo.mJavaVarName));
			source.append(MessageFormat.format("ret_val.get{0}().assign((({1})single_value).valueOf());\n", fieldInfo.mJavaVarName, fieldInfo.mJavaTemplateName));
			source.append("break;\n");
		}
		source.append("default:\n");
		source.append(MessageFormat.format("throw new TtcnError(\"Internal error: Invalid selector in a specific value when performing valueof operation on a template of union type {0}.\");\n", displayName));
		source.append("}\n");
		if (!fieldInfos.isEmpty()) {
			source.append("return ret_val;\n");
		}
		source.append("}\n\n");
	}

	/**
	 * Generate the setType function
	 *
	 * @param source where the source code is to be generated.
	 * @param genName the name of the generated class representing the union/choice type.
	 * @param displayName the user readable name of the type to be generated.
	 * */
	private static void generateTemplateSetType(final StringBuilder source, final String genName, final String displayName) {
		source.append("public void setType(final template_sel template_type, final int list_length) {\n");
		source.append("if (template_type != template_sel.VALUE_LIST && template_type != template_sel.COMPLEMENTED_LIST) {\n");
		source.append(MessageFormat.format("throw new TtcnError(\"Internal error: Setting an invalid list for a template of union type {0}.\");\n", displayName));
		source.append("}\n");

		source.append("cleanUp();\n");
		source.append("set_selection(template_type);\n");
		source.append(MessageFormat.format("value_list = new ArrayList<{0}_template>(list_length);\n", genName));
		source.append("for(int i = 0 ; i < list_length; i++) {\n");
		source.append(MessageFormat.format("value_list.add(new {0}_template());\n", genName));
		source.append("}\n");
		source.append("}\n\n");
	}

	/**
	 * Generate the listItem function
	 *
	 * @param source where the source code is to be generated.
	 * @param genName the name of the generated class representing the union/choice type.
	 * @param displayName the user readable name of the type to be generated.
	 * */
	private static void generateTemplateListItem(final StringBuilder source, final String genName, final String displayName) {
		source.append(MessageFormat.format("public {0}_template listItem(final int list_index)  '{'\n", genName));
		source.append("if (templateSelection != template_sel.VALUE_LIST && templateSelection != template_sel.COMPLEMENTED_LIST) {\n");
		source.append(MessageFormat.format("throw new TtcnError(\"Internal error: Accessing a list element of a non-list template of union type {0}.\");\n", displayName));
		source.append("}\n");

		source.append("if (list_index < 0) {\n");
		source.append(MessageFormat.format("throw new TtcnError(\"Internal error: Index underflow in a value list template of union type {0}.\");\n", displayName));
		source.append("}\n");
		source.append("if(list_index >= value_list.size()) {\n");
		source.append(MessageFormat.format("throw new TtcnError(\"Internal error: Index overflow in a value list template of union type {0}.\");\n", displayName));
		source.append("}\n");
		source.append("return value_list.get(list_index);\n");
		source.append("}\n\n");
	}

	/**
	 * Generate the isPresent function
	 *
	 * @param source where the source code is to be generated.
	 * */
	private static void generateTemplateIsPresent(final StringBuilder source) {
		source.append("public boolean isPresent() {\n");
		source.append("return isPresent(false);\n");
		source.append("}\n\n");

		source.append("public boolean isPresent(final boolean legacy) {\n");
		source.append("if (templateSelection == template_sel.UNINITIALIZED_TEMPLATE) {\n");
		source.append("return false;\n");
		source.append("}\n");
		source.append("return !match_omit(legacy);\n");
		source.append("}\n\n");
	}

	/**
	 * Generate the match_omit function
	 *
	 * @param source where the source code is to be generated.
	 * */
	private static void generateTemplateMatchOmit(final StringBuilder source) {
		source.append("public boolean match_omit() {\n");
		source.append("return match_omit(false);\n");
		source.append("}\n\n");

		source.append("public boolean match_omit(final boolean legacy) {\n");
		source.append("if (is_ifPresent) {\n");
		source.append("return true;\n");
		source.append("}\n");
		source.append("switch(templateSelection) {\n");
		source.append("case OMIT_VALUE:\n");
		source.append("case ANY_OR_OMIT:\n");
		source.append("return true;\n");
		source.append("case VALUE_LIST:\n");
		source.append("case COMPLEMENTED_LIST:\n");
		source.append("if (legacy) {\n");
		source.append("for (int i = 0 ; i < value_list.size(); i++) {\n");
		source.append("if (value_list.get(i).match_omit(legacy)) {\n");
		source.append("return templateSelection == template_sel.VALUE_LIST;\n");
		source.append("}\n");
		source.append("}\n");
		source.append("return templateSelection == template_sel.COMPLEMENTED_LIST;\n");
		source.append("}\n");
		source.append("return false;\n");
		source.append("default:\n");
		source.append("return false;\n");
		source.append("}\n");
		source.append("}\n\n");
	}

	/**
	 * Generate getters/setters
	 *
	 * @param source where the source code is to be generated.
	 * @param genName the name of the generated class representing the union/choice type.
	 * @param displayName the user readable name of the type to be generated.
	 * @param fieldInfos the list of information about the fields.
	 * */
	private static void generateTemplateGetterSetters(final StringBuilder source, final String genName, final String displayName,
			final List<FieldInfo> fieldInfos) {
		for (int i = 0 ; i < fieldInfos.size(); i++) {
			final FieldInfo fieldInfo = fieldInfos.get(i);
			source.append(MessageFormat.format("public {0} get{1}() '{'\n", fieldInfo.mJavaTemplateName, fieldInfo.mJavaVarName));
			source.append(MessageFormat.format("if (templateSelection != template_sel.SPECIFIC_VALUE || single_value_union_selection != {0}.union_selection_type.ALT_{1}) '{'\n", genName, fieldInfo.mJavaVarName));
			source.append("final template_sel old_selection = templateSelection;\n");
			source.append("cleanUp();\n");
			source.append("if (old_selection == template_sel.ANY_VALUE || old_selection == template_sel.ANY_OR_OMIT) {\n");
			source.append(MessageFormat.format("single_value = new {0}(template_sel.ANY_VALUE);\n", fieldInfo.mJavaTemplateName));
			source.append("} else {\n");
			source.append(MessageFormat.format("single_value = new {0}();\n", fieldInfo.mJavaTemplateName));
			source.append("}\n");
			source.append(MessageFormat.format("single_value_union_selection = {0}.union_selection_type.ALT_{1};\n", genName, fieldInfo.mJavaVarName));
			source.append("set_selection(template_sel.SPECIFIC_VALUE);\n");
			source.append("}\n");
			source.append(MessageFormat.format("return ({0})single_value;\n", fieldInfo.mJavaTemplateName));
			source.append("}\n\n");

			source.append(MessageFormat.format("public {0} constGet{1}() '{'\n", fieldInfo.mJavaTemplateName, fieldInfo.mJavaVarName));
			source.append("if (templateSelection != template_sel.SPECIFIC_VALUE) {\n");
			source.append(MessageFormat.format("throw new TtcnError(\"Accessing field {0} in a non-specific template of union type {1}.\");\n", fieldInfo.mDisplayName, displayName));
			source.append("}\n");
			source.append(MessageFormat.format("if (single_value_union_selection != {0}.union_selection_type.ALT_{1}) '{'\n", genName, fieldInfo.mJavaVarName));
			source.append(MessageFormat.format("throw new TtcnError(\"Accessing non-selected field {0} in a template of union type {1}.\");\n", fieldInfo.mDisplayName, displayName));
			source.append("}\n");
			source.append(MessageFormat.format("return ({0})single_value;\n", fieldInfo.mJavaTemplateName));
			source.append("}\n\n");
		}
	}

	/**
	 * Generate log
	 *
	 * @param source where the source code is to be generated.
	 * */
	private static void generateTemplateLog(final StringBuilder source) {
		source.append("public void log() {\n");
		source.append("switch (templateSelection) {\n");
		source.append("case SPECIFIC_VALUE:\n");
		source.append("single_value.log();\n");
		source.append("break;\n");
		source.append("case COMPLEMENTED_LIST:\n");
		source.append("TtcnLogger.log_event_str(\"complement\");\n");
		source.append("case VALUE_LIST:\n");
		source.append("TtcnLogger.log_char('(');\n");
		source.append("for (int list_count = 0; list_count < value_list.size(); list_count++) {\n");
		source.append("if (list_count > 0) {\n");
		source.append("TtcnLogger.log_event_str(\", \");\n");
		source.append("}\n");
		source.append("value_list.get(list_count).log();\n");
		source.append("}\n");
		source.append("TtcnLogger.log_char(')');\n");
		source.append("break;\n");
		source.append("default:\n");
		source.append("log_generic();\n");
		source.append("break;\n");
		source.append("}\n");
		source.append("log_ifpresent();\n");
		source.append("}\n\n");
	}

	/**
	 * Generate log_match
	 *
	 * @param source where the source code is to be generated.
	 * @param genName the name of the generated class representing the union/choice type.
	 * @param displayName the user readable name of the type to be generated.
	 * @param fieldInfos the list of information about the fields.
	 * */
	private static void generateTemplateLogMatch(final StringBuilder source, final String genName, final String displayName, final List<FieldInfo> fieldInfos) {
		source.append("@Override\n");
		source.append("public void log_match(final Base_Type match_value, final boolean legacy) {\n");
		source.append(MessageFormat.format("if (match_value instanceof {0}) '{'\n", genName));
		source.append(MessageFormat.format("log_match(({0})match_value, legacy);\n", genName));
		source.append("\t\t\treturn;\n");
		source.append("}\n\n");
		source.append(MessageFormat.format("throw new TtcnError(\"Internal Error: value can not be cast to {0}.\");\n", displayName));
		source.append("}\n\n");

		source.append(MessageFormat.format("public void log_match(final {0} match_value, final boolean legacy) '{'\n", genName));
		source.append("if (TtcnLogger.matching_verbosity_t.VERBOSITY_COMPACT == TtcnLogger.get_matching_verbosity() && match(match_value, legacy)) {\n");
		source.append("TtcnLogger.print_logmatch_buffer();\n");
		source.append("TtcnLogger.log_event_str(\" matched\");\n");
		source.append("return;\n");
		source.append("}\n");
		source.append("if (templateSelection == template_sel.SPECIFIC_VALUE && single_value_union_selection == match_value.get_selection()) {\n");
		source.append("switch(single_value_union_selection) {\n");
		for (int i = 0 ; i < fieldInfos.size(); i++) {
			final FieldInfo fieldInfo = fieldInfos.get(i);
			source.append(MessageFormat.format("case ALT_{0}:\n", fieldInfo.mJavaVarName));
			source.append("if (TtcnLogger.matching_verbosity_t.VERBOSITY_COMPACT == TtcnLogger.get_matching_verbosity()) {\n");
			source.append(MessageFormat.format("TtcnLogger.log_logmatch_info(\".{0}\");\n", fieldInfo.mDisplayName));

			source.append(MessageFormat.format("single_value.log_match(match_value.get{0}(), legacy);\n", fieldInfo.mJavaVarName));
			source.append("} else {\n");
			source.append(MessageFormat.format("TtcnLogger.log_logmatch_info(\"'{' {0} := \");\n", fieldInfo.mDisplayName));
			source.append(MessageFormat.format("single_value.log_match(match_value.get{0}(), legacy);\n", fieldInfo.mJavaVarName));
			source.append("TtcnLogger.log_event_str(\" }\");\n");
			source.append("}\n");
			source.append("break;\n");
		}
		source.append("default:\n");
		source.append("TtcnLogger.print_logmatch_buffer();\n");
		source.append("TtcnLogger.log_event_str(\"<invalid selector>\");\n");
		source.append("}\n");
		source.append("} else {\n");
		source.append("TtcnLogger.print_logmatch_buffer();\n");
		source.append("match_value.log();\n");
		source.append("TtcnLogger.log_event_str(\" with \");\n");
		source.append("log();\n");
		source.append("if (match(match_value, legacy)) {\n");
		source.append("TtcnLogger.log_event_str(\" matched\");\n");
		source.append("} else {\n");
		source.append("TtcnLogger.log_event_str(\" unmatched\");\n");
		source.append("}\n");
		source.append("}\n");
		source.append("}\n\n");
	}

	/**
	 * Generate encode_text/decode_text
	 *
	 * @param source where the source code is to be generated.
	 * @param genName the name of the generated class representing the union/choice type.
	 * @param displayName the user readable name of the type to be generated.
	 * @param fieldInfos the list of information about the fields.
	 * */
	private static void generateTemplateEncodeDecodeText(final StringBuilder source, final String genName, final String displayName, final List<FieldInfo> fieldInfos) {
		source.append("@Override\n");
		source.append("public void encode_text(final Text_Buf text_buf) {\n");
		source.append("encode_text_base(text_buf);\n");
		source.append("switch(templateSelection) {\n");
		source.append("case OMIT_VALUE:\n");
		source.append("case ANY_VALUE:\n");
		source.append("case ANY_OR_OMIT:\n");
		source.append("break;\n");
		source.append("case SPECIFIC_VALUE:\n");
		source.append("text_buf.push_int(single_value_union_selection.ordinal());\n");
		source.append("single_value.encode_text(text_buf);\n");
		source.append("break;\n");
		source.append("case VALUE_LIST:\n");
		source.append("case COMPLEMENTED_LIST:\n");
		source.append("text_buf.push_int(value_list.size());\n");
		source.append("for (int i = 0; i < value_list.size(); i++) {\n");
		source.append("value_list.get(i).encode_text(text_buf);\n");
		source.append("}\n");
		source.append("break;\n");
		source.append("default:\n");
		source.append(MessageFormat.format("throw new TtcnError(\"Text encoder: Encoding an uninitialized template of type {0}.\");\n", displayName));
		source.append("}\n");
		source.append("}\n");

		source.append("@Override\n");
		source.append("public void decode_text(final Text_Buf text_buf) {\n");
		source.append("cleanUp();\n");
		source.append("decode_text_base(text_buf);\n");
		source.append("switch(templateSelection) {\n");
		source.append("case OMIT_VALUE:\n");
		source.append("case ANY_VALUE:\n");
		source.append("case ANY_OR_OMIT:\n");
		source.append("break;\n");
		source.append("case SPECIFIC_VALUE:{\n");
		source.append("final int temp = text_buf.pull_int().getInt();\n");
		source.append("switch(temp) {\n");
		for (int i = 0 ; i < fieldInfos.size(); i++) {
			final FieldInfo fieldInfo = fieldInfos.get(i);

			source.append(MessageFormat.format("case {0}:\n", i));
			source.append(MessageFormat.format("single_value = new {0}();\n", fieldInfo.mJavaTemplateName));
			source.append("single_value.decode_text(text_buf);\n");
			source.append("break;\n");
		}
		source.append("}\n");
		source.append("}\n");
		source.append("case VALUE_LIST:\n");
		source.append("case COMPLEMENTED_LIST: {\n");
		source.append("final int size = text_buf.pull_int().getInt();\n");
		source.append(MessageFormat.format("value_list = new ArrayList<{0}_template>(size);\n", genName));
		source.append("for (int i = 0; i < size; i++) {\n");
		source.append(MessageFormat.format("final {0}_template temp2 = new {0}_template();\n", genName));
		source.append("temp2.decode_text(text_buf);\n");
		source.append("value_list.add(temp2);\n");
		source.append("}\n");
		source.append("break;\n");
		source.append("}\n");
		source.append("default:\n");
		source.append(MessageFormat.format("throw new TtcnError(\"Text decoder: Unrecognized selector was received in a template of type {0}.\");\n", displayName));
		source.append("}\n");
		source.append("}\n");
	}

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
					fieldName = MessageFormat.format("{0}.get{1}()", fieldName, FieldSubReference.getJavaGetterName( field.nthfieldname ));

				}

				if (j < fields.fields.size() && field.fieldtype == rawAST_coding_field_type.OPTIONAL_FIELD) {
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

}
