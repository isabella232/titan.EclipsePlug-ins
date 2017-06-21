package org.eclipse.titan.designer.AST.TTCN3.types;

import java.text.MessageFormat;
import java.util.List;

import org.eclipse.titan.designer.AST.FieldSubReference;
import org.eclipse.titan.designer.compiler.JavaGenData;

/**
 * Utility class for generating the value and template classes for union/choice types.
 *
 * The code generated for union/choice types only differs in matching and encoding.
 * */
public class UnionGenerator {
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
		 * @param fieldType: the string representing the type of this field in the generated code.
		 * @param fieldName: the string representing the name of this field in the generated code.
		 * @param isOptional: true if the field is optional.
		 * @param debugName: additional text printed out in a comment after the generated local variables.
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
	 * This function can be used to generate the value class of union/choice types
	 *
	 * defUnionClass in compiler2/union.{h,c}
	 *
	 * @param aData: only used to update imports if needed.
	 * @param source: where the source code is to be generated.
	 * @param genName: the name of the generated class representing the union/choice type.
	 * @param displayName: the user readable name of the type to be generated.
	 * @param fieldInfos: the list of information about the fields.
	 * @param hasOptional: true if the type has an optional field.
	 * */
	public static void generateValueClass(final JavaGenData aData, final StringBuilder source, final String genName, final String displayName,
			final List<FieldInfo> fieldInfos, final boolean hasOptional) {
		aData.addBuiltinTypeImport("Base_Type");
		aData.addBuiltinTypeImport("TitanBoolean");

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
		generateValueGetterSetters(source, genName, displayName, fieldInfos);

		//FIXME implement rest
		source.append("}\n");
	}

	/**
	 * This function can be used to generate the template class of union/choice types
	 *
	 * defUnionClass in compiler2/union.{h,c}
	 *
	 * @param aData: only used to update imports if needed.
	 * @param source: where the source code is to be generated.
	 * @param genName: the name of the generated class representing the union/choice type.
	 * @param displayName: the user readable name of the type to be generated.
	 * @param fieldInfos: the list of information about the fields.
	 * @param hasOptional: true if the type has an optional field.
	 * */
	public static void generateTemplateClass(final JavaGenData aData, final StringBuilder source, final String genName, final String displayName,
			final List<FieldInfo> fieldInfos, final boolean hasOptional) {
		aData.addBuiltinTypeImport("Base_Template");
		aData.addBuiltinTypeImport("TitanBoolean");
		aData.addImport("java.util.ArrayList");

		source.append(MessageFormat.format("public static class {0}_template extends Base_Template '{'\n", genName));
		generateTemplateDeclaration(source, genName);
		generatetemplateCopyValue(source, genName, displayName, fieldInfos);
		generateTemplateConstructors(source, genName);
		generateTemplateCleanup(source, fieldInfos);
		generateTemplateAssign(source, genName);
		generateTemplateIsChosen(source, genName, displayName);
		generateTemplateIsValue(source, displayName, fieldInfos);
		generateTemplateIsPresent(source);
		generateTemplateMatchOmit(source);
		generateTemplateGetterSetters(source, genName, displayName, fieldInfos);

		//FIXME implement rest
		source.append("}\n");	
	}

	/**
	 * Generate member variables
	 *
	 * @param source: where the source code is to be generated.
	 * @param genName: the name of the generated class representing the union/choice type.
	 * @param fieldInfos: the list of information about the fields.
	 * */
	private static void generateValueDeclaration(final StringBuilder source, final String genName, final List<FieldInfo> fieldInfos) {
		source.append("public enum union_selection_type { UNBOUND_VALUE, ");
		for (int i = 0 ; i < fieldInfos.size(); i++) {
			if (i > 0) {
				source.append(", ");
			}
			source.append(MessageFormat.format("ALT_{0}", fieldInfos.get(i).mJavaVarName));
		}
		source.append("};\n");
		source.append("private union_selection_type union_selection;\n");
		source.append("//originally a union which can not be mapped to Java\n");
		source.append("private Base_Type field;\n");
	}

	/**
	 * Generate constructors
	 *
	 * @param source: where the source code is to be generated.
	 * @param genName: the name of the generated class representing the union/choice type.
	 * @param fieldInfos: the list of information about the fields.
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
	 * @param source: where the source code is to be generated.
	 * @param genName: the name of the generated class representing the union/choice type.
	 * @param displayName: the user readable name of the type to be generated.
	 * @param fieldInfos: the list of information about the fields.
	 * */
	private static void generateValueCopyValue(final StringBuilder source, final String genName, final String displayName, final List<FieldInfo> fieldInfos) {
		source.append(MessageFormat.format("private void copy_value(final {0} otherValue) '{'\n", genName));
		source.append("switch(otherValue.union_selection){\n");
		for (int i = 0 ; i < fieldInfos.size(); i++) {
			FieldInfo fieldInfo = fieldInfos.get(i);
			source.append(MessageFormat.format("case ALT_{0}:\n", fieldInfo.mJavaVarName));
			source.append(MessageFormat.format("field = new {0}(({0})otherValue.field);\n", fieldInfo.mJavaTypeName));
			source.append("break;\n");
		}
		source.append("default:\n");
		source.append(MessageFormat.format("throw new TtcnError(\"Assignment of an unbound union value of type {0}.\");", displayName));
		source.append("}\n");
		source.append("union_selection = otherValue.union_selection;\n");
		source.append("}\n\n");
	}

	/**
	 * Generate assign functions
	 *
	 * @param source: where the source code is to be generated.
	 * @param genName: the name of the generated class representing the union/choice type.
	 * @param displayName: the user readable name of the type to be generated.
	 * @param fieldInfos: the list of information about the fields.
	 * */
	private static void generateValueAssign(final StringBuilder source, final String genName, final String displayName, final List<FieldInfo> fieldInfos) {
		source.append("//originally operator=\n");
		source.append(MessageFormat.format("public {0} assign( final {0} otherValue ) '{'\n", genName));
		source.append("if(otherValue.union_selection == union_selection_type.UNBOUND_VALUE) {\n");
		source.append(MessageFormat.format("throw new TtcnError( \"Assignment of an unbound {0} value.\" );\n", displayName));
		source.append("}\n");
		source.append("cleanUp();\n");
		source.append("copy_value(otherValue);\n");
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
	 * @param source: where the source code is to be generated.
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
	 * @param source: where the source code is to be generated.
	 * @param displayName: the user readable name of the type to be generated.
	 * */
	private static void generateValueIsChosen(final StringBuilder source, final String displayName) {
		source.append("public boolean isChosen(final union_selection_type checked_selection) {\n");
		source.append("if(checked_selection == union_selection_type.UNBOUND_VALUE) {\n");
		source.append(MessageFormat.format("throw new TtcnError(\"Internal error: Performing ischosen() operation on an invalid field of union type {0}.\");\n", displayName));
		source.append("}\n");
		source.append("if (union_selection == checked_selection) {\n");
		source.append(MessageFormat.format("throw new TtcnError(\"Performing ischosen() operation on an unbound value of union type {0}.\");\n", displayName));
		source.append("}\n");
		source.append("return union_selection == checked_selection;\n");
		source.append("}\n\n");
	}

	/**
	 * Generate the isBound function
	 *
	 * @param source: where the source code is to be generated.
	 * */
	private static void generateValueIsBound(final StringBuilder source) {
		source.append("public boolean isBound() {\n");
		source.append("return union_selection != union_selection_type.UNBOUND_VALUE;\n");
		source.append("}\n\n");	
	}

	/**
	 * Generate the isValue function
	 *
	 * @param source: where the source code is to be generated.
	 * @param fieldInfos: the list of information about the fields.
	 * */
	private static void generateValueIsValue(final StringBuilder source, final List<FieldInfo> fieldInfos) {
		source.append("public boolean isValue() {\n");
		source.append("switch(union_selection) {\n");
		for (int i = 0 ; i < fieldInfos.size(); i++) {
			FieldInfo fieldInfo = fieldInfos.get(i);
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
	 * @param source: where the source code is to be generated.
	 * */
	private static void generateValueIsPresent(final StringBuilder source) {
		source.append("public boolean isPresent() {\n");
		source.append("return isBound();\n");
		source.append("}\n\n");
	}

	/**
	 * Generate assignment operators
	 *
	 * @param source: where the source code is to be generated.
	 * @param genName: the name of the generated class representing the union/choice type.
	 * @param displayName: the user readable name of the type to be generated.
	 * @param fieldInfos: the list of information about the fields.
	 * */
	private static void generateValueOperatorEquals(final StringBuilder source, final String genName, final String displayName,
			final List<FieldInfo> fieldInfos) {
		source.append("//originally operator==\n");
		source.append(MessageFormat.format("public TitanBoolean operatorEquals( final {0} otherValue ) '{'\n", genName));
		source.append("if (union_selection == union_selection_type.UNBOUND_VALUE) {\n");
		source.append(MessageFormat.format("throw new TtcnError( \"The left operand of comparison is an unbound value of union type {0}.\" );\n", displayName));
		source.append("}\n");
		source.append("if (otherValue.union_selection == union_selection_type.UNBOUND_VALUE) {\n");
		source.append(MessageFormat.format("throw new TtcnError( \"The right operand of comparison is an unbound value of union type {0}.\" );\n", displayName));
		source.append("}\n");
		source.append("if (union_selection != otherValue.union_selection) {\n");
		source.append("return new TitanBoolean(false);\n");

		source.append("}\n");
		source.append("switch(union_selection) {\n");
		for (int i = 0 ; i < fieldInfos.size(); i++) {
			FieldInfo fieldInfo = fieldInfos.get(i);
			source.append(MessageFormat.format("case ALT_{0}:\n", fieldInfo.mJavaVarName));
			source.append(MessageFormat.format("return (({0})field).operatorEquals(({0})otherValue.field);\n", fieldInfo.mJavaTypeName));
		}


		source.append("default:\n");
		source.append("return new TitanBoolean(false);\n");
		source.append("}\n");
		source.append("}\n");

		source.append("@Override\n");
		source.append("public TitanBoolean operatorEquals( final Base_Type otherValue ) {\n");
		source.append(MessageFormat.format("if (otherValue instanceof {0}) '{'\n", genName));
		source.append(MessageFormat.format("return operatorEquals(({0})otherValue);\n", genName));
		source.append("}\n");
		source.append(MessageFormat.format("throw new TtcnError(\"Internal Error: value can not be cast to {0}.\");\n", displayName));
		source.append("}\n\n");
	}

	/**
	 * Generate getters/setters
	 *
	 * @param source: where the source code is to be generated.
	 * @param genName: the name of the generated class representing the union/choice type.
	 * @param displayName: the user readable name of the type to be generated.
	 * @param fieldInfos: the list of information about the fields.
	 * */
	private static void generateValueGetterSetters(final StringBuilder source, final String genName, final String displayName,
			final List<FieldInfo> fieldInfos) {
		for (int i = 0 ; i < fieldInfos.size(); i++) {
			FieldInfo fieldInfo = fieldInfos.get(i);
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
			source.append(MessageFormat.format("throw new TtcnError(\"Using non-selected field field1 in a value of union type {0}.\");\n", displayName));
			source.append("}\n");
			source.append(MessageFormat.format("return ({0})field;\n", fieldInfo.mJavaTypeName));
			source.append("}\n\n");
		}
	}

	/**
	 * Generate member variables
	 *
	 * @param source: where the source code is to be generated.
	 * @param genName: the name of the generated class representing the union/choice type.
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
	 * @param source: where the source code is to be generated.
	 * @param genName: the name of the generated class representing the union/choice type.
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
	 * @param source: where the source code is to be generated.
	 * @param genName: the name of the generated class representing the union/choice type.
	 * @param displayName: the user readable name of the type to be generated.
	 * @param fieldInfos: the list of information about the fields.
	 * */
	private static void generatetemplateCopyValue(final StringBuilder source, final String genName, final String displayName, final List<FieldInfo> fieldInfos) {
		source.append(MessageFormat.format("private void copy_value(final {0} other_value) '{'\n", genName));
		source.append("single_value_union_selection = other_value.union_selection;\n");
		source.append("switch (other_value.union_selection) {\n");
		for (int i = 0 ; i < fieldInfos.size(); i++) {
			FieldInfo fieldInfo = fieldInfos.get(i);
			source.append(MessageFormat.format("case ALT_{0}:\n", fieldInfo.mJavaVarName));
			source.append(MessageFormat.format("single_value = new {0}_template(({0})other_value.field);\n", fieldInfo.mJavaTypeName));
			source.append("break;\n");
		}
		source.append("default:\n");
		source.append(MessageFormat.format("throw new TtcnError(\"Initializing a template with an unbound value of type {0}.\");", displayName));	
		source.append("}\n");
		source.append("setSelection(template_sel.SPECIFIC_VALUE);\n");
		source.append("}\n");

		source.append(MessageFormat.format("private void copy_template(final {0}_template other_value) '{'\n", genName));
		source.append("switch (other_value.templateSelection) {\n");
		source.append("case SPECIFIC_VALUE:\n");
		source.append("single_value_union_selection = other_value.single_value_union_selection;\n");
		source.append("switch (single_value_union_selection) {\n");
		for (int i = 0 ; i < fieldInfos.size(); i++) {
			FieldInfo fieldInfo = fieldInfos.get(i);
			source.append(MessageFormat.format("case ALT_{0}:\n", fieldInfo.mJavaVarName));
			source.append(MessageFormat.format("single_value = new {0}_template(other_value.get{1}());\n", fieldInfo.mJavaTypeName, fieldInfo.mJavaVarName));
			source.append("break;\n");
		}
		source.append("default:\n");
		source.append(MessageFormat.format("throw new TtcnError(\"Internal error: Invalid union selector in a specific value when copying a template of type {0}.\");", displayName));	
		source.append("}\n");		
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
		source.append(MessageFormat.format("throw new TtcnError(\"Copying an uninitialized template of union type {0}.\");", displayName));
		source.append("}\n");
		source.append("setSelection(other_value);\n");
		source.append("}\n\n");
	}

	/**
	 * Generate the clean_up function
	 *
	 * @param source: where the source code is to be generated.
	 * @param fieldInfos: the list of information about the fields.
	 * */
	private static void generateTemplateCleanup(final StringBuilder source, final List<FieldInfo> fieldInfos) {
		source.append("@Override\n");
		source.append("public void cleanUp() {\n");
		source.append("switch(templateSelection) {\n");
		source.append("case SPECIFIC_VALUE:\n");
		source.append("switch(single_value_union_selection) {\n");
		for (int i = 0 ; i < fieldInfos.size(); i++) {
			FieldInfo fieldInfo = fieldInfos.get(i);
			source.append(MessageFormat.format("case ALT_{0}:\n", fieldInfo.mJavaVarName));
			source.append(MessageFormat.format("(({0}_template)single_value).cleanUp();\n", fieldInfo.mJavaTypeName, fieldInfo.mJavaVarName));
			source.append("break;\n");
		}
		source.append("default:\n");
		source.append("break;\n");
		source.append("}\n");
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
	 * @param source: where the source code is to be generated.
	 * @param genName: the name of the generated class representing the union/choice type.
	 * */
	private static void generateTemplateAssign(final StringBuilder source, final String genName) {
		source.append("//originally operator=\n");
		source.append(MessageFormat.format("public {0}_template assign( final template_sel other_value ) '{'\n", genName));
		source.append("checkSingleSelection(other_value);\n");
		source.append("cleanUp();\n");
		source.append("setSelection(other_value);\n");
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
	}

	/**
	 * Generate the isChosen function
	 *
	 * @param source: where the source code is to be generated.
	 * @param genName: the name of the generated class representing the union/choice type.
	 * @param displayName: the user readable name of the type to be generated.
	 * */
	private static void generateTemplateIsChosen(final StringBuilder source, final String genName, final String displayName) {
		source.append(MessageFormat.format("public boolean isChosen(final {0}.union_selection_type checked_selection) '{'\n", genName));
		source.append(MessageFormat.format("if(checked_selection == {0}.union_selection_type.UNBOUND_VALUE) '{'\n", genName));
		source.append(MessageFormat.format("throw new TtcnError(\"Internal error: Performing ischosen() operation on an invalid field of union type {0}.\");", displayName));
		source.append("}\n");
		source.append("switch(templateSelection) {\n");
		source.append("case SPECIFIC_VALUE:\n");
		source.append(MessageFormat.format("if (single_value_union_selection == {0}.union_selection_type.UNBOUND_VALUE) '{'\n", genName));
		source.append(MessageFormat.format("throw new TtcnError(\"Internal error: Invalid selector in a specific value when performing ischosen() operation on a template of union type {0}.\");", displayName));
		source.append("}\n");
		source.append("return single_value_union_selection == checked_selection;\n");
		source.append("case VALUE_LIST:\n");
		source.append("if (value_list.size() < 1) {\n");
		source.append(MessageFormat.format("throw new TtcnError(\"Internal error: Performing ischosen() operation on a template of union type {0} containing an empty list.\");\n", displayName));
		source.append("}\n");
		source.append("for (int i = 0; i < value_list.size(); i++) {\n");
		source.append("if(!value_list.get(i).isChosen(checked_selection)) {\n");
						//FIXME this is incorrect in the compiler
		source.append("return false;\n");
		source.append("}\n");
		source.append("}\n");
		source.append("return true;\n");
		source.append("case ANY_VALUE:\n");
		source.append("case ANY_OR_OMIT:\n");
		source.append("case OMIT_VALUE:\n");
		source.append("case COMPLEMENTED_LIST:\n");
		source.append(MessageFormat.format("throw new TtcnError(\"Performing ischosen() operation on a template of union type {0}, which does not determine unambiguously the chosen field of the matching values.\");", displayName));
		source.append("default:\n");
		source.append(MessageFormat.format("throw new TtcnError(\"Performing ischosen() operation on an uninitialized template of union type {0}.\");", displayName));
		source.append("}\n");
		source.append("}\n\n");
	}

	/**
	 * Generate the isValue function
	 *
	 * @param source: where the source code is to be generated.
	 * @param displayName: the user readable name of the type to be generated.
	 * @param fieldInfos: the list of information about the fields.
	 * */
	private static void generateTemplateIsValue(final StringBuilder source, final String displayName, final List<FieldInfo> fieldInfos) {
		source.append("@Override\n");
		source.append("public boolean isValue() {\n");
		source.append("if (templateSelection != template_sel.SPECIFIC_VALUE || is_ifPresent) {\n");
		source.append("return false;\n");
		source.append("}\n");
		source.append("switch(single_value_union_selection) {\n");
		for (int i = 0 ; i < fieldInfos.size(); i++) {
			FieldInfo fieldInfo = fieldInfos.get(i);
			source.append(MessageFormat.format("case ALT_{0}:\n", fieldInfo.mJavaVarName));
			source.append(MessageFormat.format("return (({0}_template)single_value).isValue();\n", fieldInfo.mJavaTypeName, fieldInfo.mJavaVarName));
		}
		source.append("default:\n");
		source.append(MessageFormat.format("throw new TtcnError(\"Internal error: Invalid selector in a specific value when performing is_value operation on a template of union type {0}.\");", displayName));
		source.append("}\n");
		source.append("}\n\n");
	}

	/**
	 * Generate the isPresent function
	 *
	 * @param source: where the source code is to be generated.
	 * */
	private static void generateTemplateIsPresent(final StringBuilder source) {
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
	 * @param source: where the source code is to be generated.
	 * */
	private static void generateTemplateMatchOmit(final StringBuilder source) {
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
	 * @param source: where the source code is to be generated.
	 * @param genName: the name of the generated class representing the union/choice type.
	 * @param displayName: the user readable name of the type to be generated.
	 * @param fieldInfos: the list of information about the fields.
	 * */
	private static void generateTemplateGetterSetters(final StringBuilder source, final String genName, final String displayName,
			final List<FieldInfo> fieldInfos) {
		for (int i = 0 ; i < fieldInfos.size(); i++) {
			FieldInfo fieldInfo = fieldInfos.get(i);
			source.append(MessageFormat.format("public {0}_template get{1}() '{'\n", fieldInfo.mJavaTypeName, fieldInfo.mJavaVarName));
			source.append(MessageFormat.format("if (templateSelection != template_sel.SPECIFIC_VALUE || single_value_union_selection != {0}.union_selection_type.ALT_{1}) '{'\n", genName, fieldInfo.mJavaVarName));
			source.append("template_sel old_selection = templateSelection;\n");
			source.append("cleanUp();\n");
			source.append("if (old_selection == template_sel.ANY_VALUE || old_selection == template_sel.ANY_OR_OMIT) {\n");
			source.append(MessageFormat.format("single_value = new {0}_template(template_sel.ANY_VALUE);\n", fieldInfo.mJavaTypeName));
			source.append("} else {\n");
			source.append(MessageFormat.format("single_value_union_selection = {0}.union_selection_type.ALT_{1};\n", genName, fieldInfo.mJavaVarName));
			source.append("}\n");
			source.append("setSelection(template_sel.SPECIFIC_VALUE);\n");
			source.append("}\n");
			source.append(MessageFormat.format("return ({0}_template)single_value;\n", fieldInfo.mJavaTypeName));
			source.append("}\n\n");

			source.append(MessageFormat.format("public {0}_template constGet{1}() '{'\n", fieldInfo.mJavaTypeName, fieldInfo.mJavaVarName));
			source.append("if (templateSelection != template_sel.SPECIFIC_VALUE) {\n");
			source.append(MessageFormat.format("throw new TtcnError(\"Accessing field field1 in a non-specific template of union type {0}.\");", displayName));
			source.append("}\n");
			source.append(MessageFormat.format("if (single_value_union_selection != {0}.union_selection_type.ALT_{1}) '{'\n", genName, fieldInfo.mJavaVarName));
			source.append(MessageFormat.format("throw new TtcnError(\"Accessing non-selected field field1 in a template of union type {0}.\");", displayName));
			source.append("}\n");
			source.append(MessageFormat.format("return ({0}_template)single_value;\n", fieldInfo.mJavaTypeName));
			source.append("}\n\n");
		}
	}
}
