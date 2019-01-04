/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.types;

import java.text.MessageFormat;
import java.util.ArrayList;

import org.eclipse.titan.designer.compiler.JavaGenData;

/**
 * Utility class for generating the value and template classes for
 * function/altstep/testcase types.
 *
 * starting/activating/executing is not yet supported
 *
 * @author Kristof Szabados
 * */
public final class FunctionReferenceGenerator {

	enum fatType {FUNCTION, ALTSTEP, TESTCASE};

	public static class FunctionReferenceDefinition {
		private final String genName;
		private final String displayName;
		public String returnType;
		public fatType type;
		public boolean runsOnSelf;
		public boolean isStartable;
		public String formalParList;
		public String actualParList;
		public ArrayList<String> parameterTypeNames;
		public ArrayList<String> parameterNames;

		public FunctionReferenceDefinition(final String genName, final String displayName) {
			this.genName = genName;
			this.displayName = displayName;
		}
	}

	private FunctionReferenceGenerator() {
		// private to disable instantiation
	}

	/**
	 * This function can be used to generate the value class of
	 * function/altstep/tetscase types
	 *
	 * defFunctionrefClass in compiler2/functionref.{h,c}
	 *
	 * @param aData
	 *                used to access build settings.
	 * @param source
	 *                where the source code is to be generated.
	 * @param def
	 *                the function definition to generate code for.
	 * */
	public static void generateValueClass(final JavaGenData aData, final StringBuilder source, final FunctionReferenceDefinition def) {
		aData.addBuiltinTypeImport("Base_Type");
		aData.addCommonLibraryImport("TtcnError");
		aData.addCommonLibraryImport("Module_List");
		aData.addImport("java.lang.reflect.Method");
		aData.addImport("java.lang.reflect.InvocationTargetException");

		source.append(MessageFormat.format("\tpublic static class {0} extends Base_Type '{'\n", def.genName));
		switch (def.type) {
		case FUNCTION:
			source.append("\t\tpublic interface function_pointer {\n");
			source.append("\t\t\tString getModuleName();\n");
			source.append("\t\t\tString getDefinitionName();\n");
			source.append(MessageFormat.format("\t\t\t{0} invoke({1});\n", def.returnType == null? "void" : def.returnType, def.formalParList));
			if (def.isStartable) {
				source.append("\t\t\tvoid start(final TitanComponent component_reference");
				if (def.formalParList != null && def.formalParList.length() > 0) {
					source.append(", ");
					source.append(def.formalParList);
				}
				source.append(");\n");
			}
			source.append("\t\t}\n");
			break;
		case ALTSTEP:
			aData.addBuiltinTypeImport("Default_Base");

			source.append("\t\tpublic interface function_pointer {\n");
			source.append("\t\t\tString getModuleName();\n");
			source.append("\t\t\tString getDefinitionName();\n");
			source.append(MessageFormat.format("\t\t\t{0} invoke_standalone({1});\n", def.returnType == null? "void" : def.returnType, def.formalParList));
			source.append(MessageFormat.format("\t\t\tDefault_Base activate({0});\n", def.formalParList));
			source.append(MessageFormat.format("\t\t\tTitanAlt_Status invoke({0});\n", def.formalParList));
			source.append("\t\t}\n");
			break;
		case TESTCASE:
			source.append("\t\tpublic interface function_pointer {\n");
			source.append("\t\t\tString getModuleName();\n");
			source.append("\t\t\tString getDefinitionName();\n");
			source.append(MessageFormat.format("\t\t\tTitanVerdictType execute({0});\n", def.formalParList));
			source.append("\t\t}\n");
			break;
		}
		source.append("\t\tprivate function_pointer referred_function;\n");

		source.append("\t\tpublic static final function_pointer nullValue = new function_pointer() {\n");
		source.append("\t\t\t@Override\n");
		source.append("\t\t\tpublic String getModuleName() {\n");
		source.append("\t\t\t\treturn \"null\";\n");
		source.append("\t\t\t}\n");
		source.append("\t\t\t@Override\n");
		source.append("\t\t\tpublic String getDefinitionName() {\n");
		source.append("\t\t\t\treturn \"null\";\n");
		source.append("\t\t\t}\n");
		switch (def.type) {
		case FUNCTION:
			source.append("\t\t\t@Override\n");
			source.append(MessageFormat.format("\t\t\tpublic {0} invoke({1}) '{'\n", def.returnType == null? "void" : def.returnType, def.formalParList));
			source.append("\t\t\t\tthrow new TtcnError(\"null reference cannot be invoked.\");\n");
			source.append("\t\t\t}\n");
			if (def.isStartable) {
				source.append("\t\t\tpublic void start(final TitanComponent component_reference");
				if (def.formalParList != null && def.formalParList.length() > 0) {
					source.append(", ");
					source.append(def.formalParList);
				}
				source.append(") {\n");
				source.append("\t\t\t\tthrow new TtcnError(\"null reference cannot be started.\");\n");
				source.append("\t\t\t}\n");
			}
			break;
		case ALTSTEP:
			source.append(MessageFormat.format("\t\t\tpublic {0} invoke_standalone({1}) '{'\n", def.returnType == null? "void" : def.returnType, def.formalParList));
			source.append("\t\t\t\tthrow new TtcnError(\"null reference cannot be invoked.\");\n");
			source.append("\t\t\t}\n");
			source.append(MessageFormat.format("\t\t\tpublic Default_Base activate({0}) '{'\n", def.formalParList));
			source.append("\t\t\t\tthrow new TtcnError(\"null reference cannot be activated.\");\n");
			source.append("\t\t\t}\n");
			source.append(MessageFormat.format("\t\t\tpublic TitanAlt_Status invoke({0}) '{'\n", def.formalParList));
			source.append("\t\t\t\tthrow new TtcnError(\"null reference cannot be invoked.\");\n");
			source.append("\t\t}\n");
			break;
		case TESTCASE:
			source.append("\t\t\t@Override\n");
			source.append(MessageFormat.format("\t\tpublic TitanVerdictType execute({0}) '{'\n", def.formalParList));
			source.append("\t\t\t\tthrow new TtcnError(\"null reference cannot be executed.\");\n");
			source.append("\t\t\t}\n");
			break;
		}
		source.append("\t\t};\n");

		if ( aData.isDebug() ) {
			source.append( "\t\t/**\n" );
			source.append( "\t\t * Initializes to unbound value.\n" );
			source.append( "\t\t * */\n" );
		}
		source.append(MessageFormat.format("\t\tpublic {0}() '{'\n", def.genName));
		source.append("\t\t\treferred_function = null;\n");
		source.append("\t\t}\n\n");

		//TODO check if this kind of constructor is a good idea or not!!!
		if ( aData.isDebug() ) {
			source.append( "\t\t/**\n" );
			source.append( "\t\t * Initializes to a given value.\n" );
			source.append( "\t\t *\n" );
			source.append( "\t\t * @param otherValue\n" );
			source.append( "\t\t *                the value to initialize to.\n" );
			source.append( "\t\t * */\n" );
		}
		source.append(MessageFormat.format("\t\tpublic {0}(final function_pointer otherValue) '{'\n", def.genName));
		source.append("\t\t\treferred_function = otherValue;\n");
		source.append("\t\t}\n\n");

		if ( aData.isDebug() ) {
			source.append( "\t\t/**\n" );
			source.append( "\t\t * Initializes to a given value.\n" );
			source.append( "\t\t *\n" );
			source.append( "\t\t * @param otherValue\n" );
			source.append( "\t\t *                the value to initialize to.\n" );
			source.append( "\t\t * */\n" );
		}
		source.append(MessageFormat.format("\t\tpublic {0}(final {0} otherValue) '{'\n", def.genName));
		source.append(MessageFormat.format("\t\t\totherValue.must_bound(\"Copying an unbound {0}.\");\n\n", def.displayName));
		source.append("\t\t\treferred_function = otherValue.referred_function;\n");
		source.append("\t\t}\n\n");

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
		source.append(MessageFormat.format("\t\tpublic {0} operator_assign(final function_pointer otherValue) '{'\n", def.genName));
		source.append("\t\t\treferred_function = otherValue;\n");
		source.append("\t\t\treturn this;\n");
		source.append("\t\t}\n\n");

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
		source.append(MessageFormat.format("\t\tpublic {0} operator_assign(final {0} otherValue) '{'\n", def.genName));
		source.append(MessageFormat.format("\t\t\totherValue.must_bound(\"Assignment of an unbound {0}.\");\n\n", def.displayName));
		source.append("\t\t\treferred_function = otherValue.referred_function;\n");
		source.append("\t\t\treturn this;\n");
		source.append("\t\t}\n");

		source.append("\t\t@Override\n");
		source.append(MessageFormat.format("\t\tpublic {0} operator_assign(Base_Type otherValue) '{'\n", def.genName));
		source.append(MessageFormat.format("\t\t\tif (otherValue instanceof {0}) '{'\n", def.genName));
		source.append(MessageFormat.format("\t\t\t\treturn operator_assign(({0})otherValue);\n", def.genName));
		source.append("\t\t\t}\n");
		source.append(MessageFormat.format("\t\t\tthrow new TtcnError(\"Internal Error: The left operand of assignment is not of type {0}.\");\n", def.displayName));
		source.append("\t\t}\n");

		if (aData.isDebug()) {
			source.append("\t\t/**\n");
			source.append("\t\t * Checks if the current value is equivalent to the provided one.\n");
			source.append("\t\t *\n");
			source.append("\t\t * operator== in the core\n");
			source.append("\t\t *\n");
			source.append("\t\t * @param otherValue\n");
			source.append("\t\t *                the other value to check against.\n");
			source.append("\t\t * @return true if the values are equivalent.\n");
			source.append("\t\t */\n");
		}
		source.append("\t\tpublic boolean operator_equals(final function_pointer otherValue) {\n");
		source.append(MessageFormat.format("\t\t\tmust_bound(\"Unbound left operand of {0} comparison.\");\n\n", def.displayName));
		source.append("\t\t\treturn referred_function.getModuleName().equals(otherValue.getModuleName()) && referred_function.getDefinitionName().equals(otherValue.getDefinitionName());\n");
		source.append("\t\t}\n");

		if (aData.isDebug()) {
			source.append("\t\t/**\n");
			source.append("\t\t * Checks if the current value is equivalent to the provided one.\n");
			source.append("\t\t *\n");
			source.append("\t\t * operator== in the core\n");
			source.append("\t\t *\n");
			source.append("\t\t * @param otherValue\n");
			source.append("\t\t *                the other value to check against.\n");
			source.append("\t\t * @return true if the values are equivalent.\n");
			source.append("\t\t */\n");
		}
		source.append(MessageFormat.format("\t\tpublic boolean operator_equals(final {0} otherValue) '{'\n", def.genName));
		source.append(MessageFormat.format("\t\t\tmust_bound(\"Unbound left operand of {0} comparison.\");\n", def.displayName));
		source.append(MessageFormat.format("\t\t\totherValue.must_bound(\"Unbound right operand of {0} comparison.\");\n\n", def.displayName));
		source.append("\t\t\treturn referred_function.getModuleName().equals(otherValue.referred_function.getModuleName()) && referred_function.getDefinitionName().equals(otherValue.referred_function.getDefinitionName());\n");
		source.append("\t\t}\n");

		source.append("\t\t@Override\n");
		source.append("\t\tpublic boolean operator_equals(final Base_Type otherValue) {\n");
		source.append(MessageFormat.format("\t\t\tif (otherValue instanceof {0}) '{'\n", def.genName));
		source.append(MessageFormat.format("\t\t\t\treturn operator_equals(({0})otherValue);\n", def.genName));
		source.append("\t\t\t}\n");
		source.append(MessageFormat.format("\t\t\tthrow new TtcnError(\"Internal Error: The left operand of comparison is not of type {0}.\");\n", def.displayName));
		source.append("\t\t}\n");

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
		source.append("\t\tpublic boolean operator_not_equals(final function_pointer otherValue) {\n");
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
			source.append("\t\t * @return {@code true} if the values are not equivalent.\n");
			source.append("\t\t */\n");
		}
		source.append(MessageFormat.format("\t\tpublic boolean operator_not_equals(final {0} otherValue) '{'\n", def.genName));
		source.append("\t\t\treturn !operator_equals(otherValue);\n");
		source.append("\t\t}\n");

		switch (def.type) {
		case FUNCTION:
			source.append("\t\tpublic ");
			if (def.returnType == null) {
				source.append("void");
			} else {
				source.append(def.returnType);
			}
			source.append(MessageFormat.format(" invoke({0}) '{'\n", def.formalParList));
			source.append("\t\t\tmust_bound(\"Call of unbound function.\");\n");
			source.append("\t\t\t");
			if (def.returnType != null) {
				source.append("return ");
			}
			source.append("referred_function.invoke(");
			source.append(def.actualParList);
			source.append(");\n");
			source.append("\t\t}\n");

			if (def.isStartable) {
				source.append("\t\tpublic void start(final TitanComponent component_reference");
				if (def.formalParList != null && def.formalParList.length() > 0) {
					source.append(", ");
					source.append(def.formalParList);
				}
				source.append(") {\n");
				source.append("\t\t\tmust_bound(\"Start of unbound function.\");\n");
				source.append("\t\t\treferred_function.start(component_reference");
				if (def.actualParList != null && def.actualParList.length() > 0) {
					source.append(", ");
					source.append(def.actualParList);
				}
				source.append(");\n");
				source.append("\t\t}\n");
			}
			break;
		case ALTSTEP:
			source.append(MessageFormat.format("\t\tpublic void invoke_standalone({0}) '{'\n", def.formalParList));
			source.append("\t\t\tmust_bound(\"Call of unbound altstep.\");\n");
			source.append("\t\t\treferred_function.invoke_standalone(");
			source.append(def.actualParList);
			source.append(");\n");
			source.append("\t\t}\n");
			source.append(MessageFormat.format("\t\tpublic Default_Base activate({0}) '{'\n", def.formalParList));
			source.append("\t\t\tmust_bound(\"Activation of unbound altstep.\");\n");
			source.append("\t\t\treturn referred_function.activate(");
			source.append(def.actualParList);
			source.append(");\n");
			source.append("\t\t}\n");
			source.append(MessageFormat.format("\t\tpublic TitanAlt_Status invoke({0}) '{'\n", def.formalParList));
			source.append("\t\t\tmust_bound(\"Call of unbound altstep.\");\n");
			source.append("\t\t\treturn referred_function.invoke(");
			source.append(def.actualParList);
			source.append(");\n");
			source.append("\t\t}\n");
			break;
		case TESTCASE:
			source.append(MessageFormat.format("\t\tpublic TitanVerdictType execute({0}) '{'\n", def.formalParList));
			source.append("\t\t\tmust_bound(\"Call of unbound testcase.\");\n");
			source.append("\t\t\tif (referred_function == null) {\n");
			source.append("\t\t\t\tthrow new TtcnError(\"null reference cannot be executed.\");\n");
			source.append("\t\t\t}\n");
			source.append(MessageFormat.format("\t\t\treturn referred_function.execute({0});", def.actualParList));
			source.append("\t\t}\n");
			break;
		}

		source.append("\t\t@Override\n");
		source.append("\t\tpublic boolean is_bound() {\n");
		source.append("\t\t\treturn referred_function != null;\n");
		source.append("\t\t}\n");
		source.append("\t\t@Override\n");
		source.append("\t\tpublic boolean is_value() {\n");
		source.append("\t\t\treturn referred_function != null;\n");
		source.append("\t\t}\n");
		source.append("\t\t@Override\n");
		source.append("\t\tpublic boolean is_present() {\n");
		source.append("\t\t\treturn is_bound();\n");
		source.append("\t\t}\n");

		source.append("\t\t@Override\n");
		source.append("\t\tpublic void clean_up() {\n");
		source.append("\t\t\treferred_function = null;\n");
		source.append("\t\t}\n\n");

		source.append("\t\t@Override\n");
		source.append("\t\tpublic void log() {\n");
		source.append("\t\t\tif(referred_function == null) {\n");
		source.append("\t\t\t\tTTCN_Logger.log_event_unbound();\n");
		source.append("\t\t\t} else {\n");
		source.append("\t\t\t\tTTCN_Logger.log_event(\"refers(%s)\", referred_function.getDefinitionName());\n");
		source.append("\t\t\t}\n");
		source.append("\t\t}\n\n");

		source.append("\t\t@Override\n");
		source.append("\t\tpublic void set_param(final Module_Parameter param) {\n");
		source.append("\t\t\tparam.error(\"Not supported.\");\n");
		source.append("\t\t}\n\n");

		source.append("\t\t@Override\n");
		source.append("\t\tpublic void encode_text(final Text_Buf text_buf) {\n");
		switch (def.type) {
		case FUNCTION:
			source.append("\t\t\tmust_bound(\"Text encoder: Encoding an unbound function reference.\");\n");
			break;
		case ALTSTEP:
			source.append("\t\t\tmust_bound(\"Text encoder: Encoding an unbound altstep reference.\");\n");
			break;
		case TESTCASE:
			source.append("\t\t\tmust_bound(\"Text encoder: Encoding an unbound testcase reference.\");\n");
			break;
		}
		source.append("\t\t\tif (referred_function == nullValue) {\n");
		source.append("\t\t\t\ttext_buf.push_string(\"\");\n");
		source.append("\t\t\t} else {\n");
		source.append("\t\t\t\tfinal String moduleName = referred_function.getModuleName();\n");
		source.append("\t\t\t\tfinal String definitionName = referred_function.getDefinitionName();\n");
		source.append("\t\t\t\ttext_buf.push_string(moduleName);\n");
		source.append("\t\t\t\ttext_buf.push_string(definitionName);\n");
		source.append("\t\t\t}\n");
		source.append("\t\t}\n\n");

		source.append("\t\t@Override\n");
		source.append("\t\tpublic void decode_text(final Text_Buf text_buf) {\n");
		generateDecodeTextInternal(aData, source, def, "referred_function");
		source.append("\t\t}\n\n");

		source.append("\t}\n\n");
	}

	private static void generateDecodeTextInternal(final JavaGenData aData, final StringBuilder source, final FunctionReferenceDefinition def, final String memberName) {
		source.append("\t\t\tfinal String moduleName = text_buf.pull_string();\n");
		source.append("\t\t\tif (moduleName == \"\") {\n");
		source.append(MessageFormat.format("\t\t\t\t{0} = {1}.nullValue;\n", memberName, def.genName));
		source.append("\t\t\t\treturn;\n");
		source.append("\t\t\t}\n");

		source.append("\t\t\tfinal TTCN_Module module = Module_List.lookup_module(moduleName);\n");
		source.append("\t\t\tif (module == null) {\n");
		source.append("\t\t\t\tthrow new TtcnError(MessageFormat.format(\"Text decoder: Module {0} does not exist when trying to decode a function reference.\", moduleName));\n");
		source.append("\t\t\t}\n");
		source.append("\t\t\tfinal String definitionName = text_buf.pull_string();\n");

		switch (def.type) {
		case FUNCTION:
			source.append("\t\t\ttry{\n");
			source.append("\t\t\t\tfinal Method m = module.getClass().getDeclaredMethod(definitionName");
			for(final String name : def.parameterTypeNames) {
				source.append(MessageFormat.format(", {0}.class", name));
			}
			source.append(");\n");
			source.append(MessageFormat.format("\t\t\t\t{0} = new {1}.function_pointer() '{'\n", memberName, def.genName));
			source.append("\t\t\t\t\t@Override\n");
			source.append("\t\t\t\t\tpublic String getModuleName() {\n");
			source.append("\t\t\t\t\t\treturn moduleName;\n");
			source.append("\t\t\t\t\t}\n");
			source.append("\t\t\t\t\t@Override\n");
			source.append("\t\t\t\t\tpublic String getDefinitionName() {\n");
			source.append("\t\t\t\t\t\treturn definitionName;\n");
			source.append("\t\t\t\t\t}\n");
			source.append("\t\t\t\t\tpublic ");
			if (def.returnType == null) {
				source.append("void");
			} else {
				source.append(def.returnType);
			}
			source.append(MessageFormat.format(" invoke({0}) '{'\n", def.formalParList));
			source.append("\t\t\t\t\t\ttry{\n");
			source.append("\t\t\t\t\t\t\t");
			if (def.returnType != null) {
				source.append(MessageFormat.format("return ({0})", def.returnType));
			}
			source.append("m.invoke(null");
			for(final String name : def.parameterNames) {
				source.append(MessageFormat.format(", {0}", name));
			}
			source.append(");\n");
			source.append("\t\t\t\t\t\t} catch(IllegalAccessException e) {\n");
			source.append("\t\t\t\t\t\t\tthrow new TtcnError(MessageFormat.format(\"Text decoder: Could not invoke function {0}.{1}.\", moduleName, definitionName));\n");
			source.append("\t\t\t\t\t\t} catch (InvocationTargetException e) {\n");
			source.append("\t\t\t\t\t\t\tthrow new TtcnError(MessageFormat.format(\"Text decoder: Could not invoke function {0}.{1}.\", moduleName, definitionName));\n");
			source.append("\t\t\t\t\t\t}\n");
			source.append("\t\t\t\t\t}\n");

			if (def.isStartable) {
				source.append("\t\t\t\t\tpublic void start(final TitanComponent component_reference");
				if (def.formalParList != null && def.formalParList.length() > 0) {
					source.append(", ");
					source.append(def.formalParList);
				}
				source.append(") {\n");
				source.append("\t\t\t\t\t\tthrow new TtcnError(\"FIXME Not yet implemented.\");\n");
				source.append("\t\t\t\t\t}\n");
			}

			source.append("\t\t\t\t};\n");
			source.append("\t\t\t} catch (NoSuchMethodException e) {\n");
			source.append("\t\t\t\tthrow new TtcnError(MessageFormat.format(\"Text decoder: Reference to non-existent function {0}.{1} was received.\", moduleName, definitionName));\n");
			source.append("\t\t\t}\n");
			break;
		case ALTSTEP:
			source.append(MessageFormat.format("\t\t\t{0} = new {1}.function_pointer() '{'\n", memberName, def.genName));
			source.append("\t\t\t\t@Override\n");
			source.append("\t\t\t\tpublic String getModuleName() {\n");
			source.append("\t\t\t\t\treturn moduleName;\n");
			source.append("\t\t\t\t}\n");
			source.append("\t\t\t\t@Override\n");
			source.append("\t\t\t\tpublic String getDefinitionName() {\n");
			source.append("\t\t\t\t\treturn definitionName;\n");
			source.append("\t\t\t\t}\n");
			source.append("\t\t\t\t@Override\n");
			source.append(MessageFormat.format("\t\t\t\tpublic void invoke_standalone({0}) '{'\n", def.formalParList));
			source.append("\t\t\t\t\ttry{\n");
			source.append("\t\t\t\t\t\tfinal Method m = module.getClass().getDeclaredMethod(definitionName");
			for(final String name : def.parameterTypeNames) {
				source.append(MessageFormat.format(", {0}.class", name));
			}
			source.append(");\n");
			source.append("\t\t\t\t\t\tm.invoke(null");
			for(final String name : def.parameterNames) {
				source.append(MessageFormat.format(", {0}", name));
			}
			source.append(");\n");
			source.append("\t\t\t\t\t} catch (NoSuchMethodException e) {\n");
			source.append("\t\t\t\t\t\tthrow new TtcnError(MessageFormat.format(\"Text decoder: Reference to non-existent altstep {0}.{1} was received.\", moduleName, definitionName));\n");
			source.append("\t\t\t\t\t} catch(IllegalAccessException e) {\n");
			source.append("\t\t\t\t\t\tthrow new TtcnError(MessageFormat.format(\"Text decoder: Could not invoke altstep {0}.{1}.\", moduleName, definitionName));\n");
			source.append("\t\t\t\t\t} catch (InvocationTargetException e) {\n");
			source.append("\t\t\t\t\t\tthrow new TtcnError(MessageFormat.format(\"Text decoder: Could not invoke altstep {0}.{1}.\", moduleName, definitionName));\n");
			source.append("\t\t\t\t\t}\n");
			source.append("\t\t\t\t}\n");

			source.append("\t\t\t\t@Override\n");
			source.append(MessageFormat.format("\t\t\t\tpublic Default_Base activate({0}) '{'\n", def.formalParList));
			source.append("\t\t\t\t\ttry{\n");
			source.append("\t\t\t\t\t\tfinal Method m = module.getClass().getDeclaredMethod(\"activate_\" + definitionName");
			for(final String name : def.parameterTypeNames) {
				source.append(MessageFormat.format(", {0}.class", name));
			}
			source.append(");\n");
			source.append("\t\t\t\t\t\treturn (Default_Base)m.invoke(null");
			for(final String name : def.parameterNames) {
				source.append(MessageFormat.format(", {0}", name));
			}
			source.append(");\n");
			source.append("\t\t\t\t\t} catch (NoSuchMethodException e) {\n");
			source.append("\t\t\t\t\t\tthrow new TtcnError(MessageFormat.format(\"Text decoder: Reference to non-existent altstep {0}.{1} was received.\", moduleName, definitionName));\n");
			source.append("\t\t\t\t\t} catch(IllegalAccessException e) {\n");
			source.append("\t\t\t\t\t\tthrow new TtcnError(MessageFormat.format(\"Text decoder: Could not invoke altstep {0}.{1}.\", moduleName, definitionName));\n");
			source.append("\t\t\t\t\t} catch (InvocationTargetException e) {\n");
			source.append("\t\t\t\t\t\tthrow new TtcnError(MessageFormat.format(\"Text decoder: Could not invoke altstep {0}.{1}.\", moduleName, definitionName));\n");
			source.append("\t\t\t\t\t}\n");
			source.append("\t\t\t\t}\n");

			source.append("\t\t\t\t@Override\n");
			source.append(MessageFormat.format("\t\t\t\tpublic TitanAlt_Status invoke({0}) '{'\n", def.formalParList));
			source.append("\t\t\t\t\ttry{\n");
			source.append("\t\t\t\t\t\tfinal Method m = module.getClass().getDeclaredMethod(definitionName + \"_instance\"");
			for(final String name : def.parameterTypeNames) {
				source.append(MessageFormat.format(", {0}.class", name));
			}
			source.append(");\n");
			source.append("\t\t\t\t\t\treturn (TitanAlt_Status)m.invoke(null");
			for(final String name : def.parameterNames) {
				source.append(MessageFormat.format(", {0}", name));
			}
			source.append(");\n");
			source.append("\t\t\t\t\t} catch (NoSuchMethodException e) {\n");
			source.append("\t\t\t\t\t\tthrow new TtcnError(MessageFormat.format(\"Text decoder: Reference to non-existent altstep {0}.{1} was received.\", moduleName, definitionName));\n");
			source.append("\t\t\t\t\t} catch(IllegalAccessException e) {\n");
			source.append("\t\t\t\t\t\tthrow new TtcnError(MessageFormat.format(\"Text decoder: Could not invoke altstep {0}.{1}.\", moduleName, definitionName));\n");
			source.append("\t\t\t\t\t} catch (InvocationTargetException e) {\n");
			source.append("\t\t\t\t\t\tthrow new TtcnError(MessageFormat.format(\"Text decoder: Could not invoke altstep {0}.{1}.\", moduleName, definitionName));\n");
			source.append("\t\t\t\t\t}\n");
			source.append("\t\t\t\t}\n");
			source.append("\t\t\t};\n");
			break;
		case TESTCASE:
			source.append("\t\t\ttry{\n");
			source.append("\t\t\t\tfinal Method m = module.getClass().getDeclaredMethod(\"testcase_\" + definitionName");
			for(final String name : def.parameterTypeNames) {
				source.append(MessageFormat.format(", {0}.class", name));
			}
			source.append(", boolean.class, TitanFloat.class);\n");
			source.append(MessageFormat.format("\t\t\t\t{0} = new {1}.function_pointer() '{'\n", memberName, def.genName));
			source.append("\t\t\t\t\t@Override\n");
			source.append("\t\t\t\t\tpublic String getModuleName() {\n");
			source.append("\t\t\t\t\t\treturn moduleName;\n");
			source.append("\t\t\t\t\t}\n");
			source.append("\t\t\t\t\t@Override\n");
			source.append("\t\t\t\t\tpublic String getDefinitionName() {\n");
			source.append("\t\t\t\t\t\treturn definitionName;\n");
			source.append("\t\t\t\t\t}\n");
			source.append("\t\t\t\t\t@Override\n");
			source.append("\t\t\t\t\tpublic TitanVerdictType execute(");
			source.append(def.formalParList);
			source.append(") {\n");
			source.append("\t\t\t\t\t\ttry{\n");
			source.append("\t\t\t\t\t\t\treturn (TitanVerdictType)");
			source.append("m.invoke(null");
			for(final String name : def.parameterNames) {
				source.append(MessageFormat.format(", {0}", name));
			}
			source.append(", has_timer, timer_value");
			source.append(");\n");
			source.append("\t\t\t\t\t\t} catch(IllegalAccessException e) {\n");
			source.append("\t\t\t\t\t\t\tthrow new TtcnError(MessageFormat.format(\"Text decoder: Could not execute testcase {0}.{1}.\", moduleName, definitionName));\n");
			source.append("\t\t\t\t\t\t} catch (InvocationTargetException e) {\n");
			source.append("\t\t\t\t\t\t\tthrow new TtcnError(MessageFormat.format(\"Text decoder: Could not execute testcase {0}.{1}.\", moduleName, definitionName));\n");
			source.append("\t\t\t\t\t\t}\n");
			source.append("\t\t\t\t\t}\n");
			source.append("\t\t\t\t};\n");
			source.append("\t\t\t} catch (NoSuchMethodException e) {\n");
			source.append("\t\t\t\tthrow new TtcnError(MessageFormat.format(\"Text decoder: Reference to non-existent testcase {0}.{1} was received.\", moduleName, definitionName));\n");
			source.append("\t\t\t}\n");
			break;
		}
	}

	/**
	 * This function can be used to generate the template class of
	 * function/altstep/tetscase types
	 *
	 * defFunctionrefTemplate in compiler2/functionref.{h,c}
	 *
	 * @param aData
	 *                used to access build settings.
	 * @param source
	 *                where the source code is to be generated.
	 * @param def
	 *                the function definition to generate code for.
	 * */
	public static void generateTemplateClass(final JavaGenData aData, final StringBuilder source, final FunctionReferenceDefinition def) {
		aData.addBuiltinTypeImport("Base_Template");
		aData.addCommonLibraryImport("TtcnError");
		aData.addImport("java.util.ArrayList");

		source.append(MessageFormat.format("\tpublic static class {0}_template extends Base_Template '{'\n", def.genName));
		source.append("\t\t//the single value\n");
		source.append(MessageFormat.format("\t\tprivate {0}.function_pointer single_value;\n", def.genName));
		source.append("\t\t// value_list part\n");
		source.append(MessageFormat.format("\t\tprivate ArrayList<{0}_template> value_list;\n", def.genName));

		source.append(MessageFormat.format("\t\tprivate void copy_template(final {0}_template other_value) '{'\n", def.genName));
		source.append("\t\t\tswitch (other_value.template_selection) {\n");
		source.append("\t\t\tcase SPECIFIC_VALUE:\n");
		source.append("\t\t\t\tsingle_value = other_value.single_value;\n");
		source.append("\t\t\t\tbreak;\n");
		source.append("\t\t\tcase OMIT_VALUE:\n");
		source.append("\t\t\tcase ANY_VALUE:\n");
		source.append("\t\t\tcase ANY_OR_OMIT:\n");
		source.append("\t\t\t\tbreak;\n");
		source.append("\t\t\tcase VALUE_LIST:\n");
		source.append("\t\t\tcase COMPLEMENTED_LIST:\n");
		source.append(MessageFormat.format("\t\t\t\tvalue_list = new ArrayList<{0}_template>(other_value.value_list.size());\n", def.genName));
		source.append("\t\t\t\tfor(int i = 0; i < other_value.value_list.size(); i++) {\n");
		source.append(MessageFormat.format("\t\t\t\t\tfinal {0}_template temp = new {0}_template(other_value.value_list.get(i));\n", def.genName));
		source.append("\t\t\t\t\tvalue_list.add(temp);\n");
		source.append("\t\t\t\t}\n");
		source.append("\t\t\t\tbreak;\n");
		source.append("\t\t\tdefault:\n");
		source.append(MessageFormat.format("\t\t\t\tthrow new TtcnError(\"Copying an uninitialized template of type {0}.\");\n", def.displayName));
		source.append("\t\t\t}\n");
		source.append("\t\t\tset_selection(other_value);\n");
		source.append("\t\t}\n");

		if (aData.isDebug()) {
			source.append("\t\t/**\n");
			source.append("\t\t * Initializes to unbound/uninitialized template.\n");
			source.append("\t\t * */\n");
		}
		source.append(MessageFormat.format("\t\tpublic {0}_template() '{'\n", def.genName));
		source.append("\t\t\t// do nothing\n");
		source.append("\t\t}\n");
		source.append(MessageFormat.format("\t\tpublic {0}_template(final template_sel otherValue) '{'\n", def.genName));
		source.append("\t\t\tsuper(otherValue);\n");
		source.append("\t\t\tcheck_single_selection(otherValue);\n");
		source.append("\t\t}\n");

		if (aData.isDebug()) {
			source.append("\t\t/**\n");
			source.append("\t\t * Initializes to a given value.\n");
			source.append("\t\t * The template becomes a specific template with the provided value.\n");
			source.append("\t\t *\n");
			source.append("\t\t * @param otherValue\n");
			source.append("\t\t *                the value to initialize to.\n");
			source.append("\t\t * */\n");
		}
		source.append(MessageFormat.format("\t\tpublic {0}_template(final {0}.function_pointer otherValue) '{'\n", def.genName));
		source.append("\t\t\tsuper(template_sel.SPECIFIC_VALUE);\n");
		source.append("\t\t\tsingle_value = otherValue;\n");
		source.append("\t\t}\n");

		if (aData.isDebug()) {
			source.append("\t\t/**\n");
			source.append("\t\t * Initializes to a given value.\n");
			source.append("\t\t * The template becomes a specific template with the provided value.\n");
			source.append("\t\t *\n");
			source.append("\t\t * @param otherValue\n");
			source.append("\t\t *                the value to initialize to.\n");
			source.append("\t\t * */\n");
		}
		source.append(MessageFormat.format("\t\tpublic {0}_template(final {0} otherValue) '{'\n", def.genName));
		source.append("\t\t\tsuper(template_sel.SPECIFIC_VALUE);\n");
		source.append(MessageFormat.format("\t\t\totherValue.must_bound(\"Creating a template from an unbound {0} value.\");\n", def.displayName));
		source.append("\t\t\tsingle_value = otherValue.referred_function;\n");
		source.append("\t\t}\n");

		if (aData.isDebug()) {
			source.append("\t\t/**\n");
			source.append("\t\t * Initializes to a given value.\n");
			source.append("\t\t * The template becomes a specific template with the provided value.\n");
			source.append("\t\t *\n");
			source.append("\t\t * @param otherValue\n");
			source.append("\t\t *                the value to initialize to.\n");
			source.append("\t\t * */\n");
		}
		source.append(MessageFormat.format("\t\tpublic {0}_template(final {0}_template otherValue) '{'\n", def.genName));
		source.append("\t\t\tcopy_template(otherValue);\n");
		source.append("\t\t}\n");

		source.append("\t\t@Override\n");
		source.append("\t\tpublic void clean_up(){\n");
		source.append("\t\t\tswitch (template_selection) {\n");
		source.append("\t\t\tcase SPECIFIC_VALUE:\n");
		source.append("\t\t\t\tsingle_value = null;\n");
		source.append("\t\t\t\tbreak;\n");
		source.append("\t\t\tcase VALUE_LIST:\n");
		source.append("\t\t\tcase COMPLEMENTED_LIST:\n");
		source.append("\t\t\t\tvalue_list.clear();\n");
		source.append("\t\t\t\tvalue_list = null;\n");
		source.append("\t\t\t\tbreak;\n");
		source.append("\t\t\tdefault:\n");
		source.append("\t\t\t\tbreak;\n");
		source.append("\t\t\t}\n");
		source.append("\t\t\ttemplate_selection = template_sel.UNINITIALIZED_TEMPLATE;\n");
		source.append("\t\t}\n");

		source.append("\t\t@Override\n");
		source.append(MessageFormat.format("\t\tpublic {0}_template operator_assign( final template_sel otherValue ) '{'\n", def.genName));
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
		source.append(MessageFormat.format("\t\tpublic {0}_template operator_assign( final {0}.function_pointer otherValue ) '{'\n", def.genName));
		source.append("\t\t\tclean_up();\n");
		source.append("\t\t\tset_selection(template_sel.SPECIFIC_VALUE);\n");
		source.append("\t\t\tsingle_value = otherValue;\n");
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
		source.append(MessageFormat.format("\t\tpublic {0}_template operator_assign( final {0} otherValue ) '{'\n", def.genName));
		source.append(MessageFormat.format("\t\t\totherValue.must_bound(\"Assignment of an unbound {0} value to a template.\");\n", def.displayName));
		source.append("\t\t\tclean_up();\n");
		source.append("\t\t\tset_selection(template_sel.SPECIFIC_VALUE);\n");
		source.append("\t\t\tsingle_value = otherValue.referred_function;\n");
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
		source.append(MessageFormat.format("\t\tpublic {0}_template operator_assign( final {0}_template otherValue ) '{'\n", def.genName));
		source.append("\t\t\tif (otherValue != this) {\n");
		source.append("\t\t\t\tclean_up();\n");
		source.append("\t\t\t\tcopy_template(otherValue);\n");
		source.append("\t\t\t}\n");
		source.append("\t\t\treturn this;\n");
		source.append("\t\t}\n");

		source.append("\t\t@Override\n");
		source.append(MessageFormat.format("\t\tpublic {0}_template operator_assign(Base_Type otherValue) '{'\n", def.genName));
		source.append(MessageFormat.format("\t\t\tif (otherValue instanceof {0}) '{'\n", def.genName));
		source.append(MessageFormat.format("\t\t\t\treturn operator_assign(({0})otherValue);\n", def.genName));
		source.append("\t\t\t}\n");
		source.append(MessageFormat.format("\t\t\tthrow new TtcnError(\"Internal Error: The left operand of assignment is not of type {0}.\");\n", def.displayName));
		source.append("\t\t}\n");

		source.append("\t\t@Override\n");
		source.append(MessageFormat.format("\t\tpublic {0}_template operator_assign(Base_Template otherValue) '{'\n", def.genName));
		source.append(MessageFormat.format("\t\t\tif (otherValue instanceof {0}_template) '{'\n", def.genName));
		source.append(MessageFormat.format("\t\t\t\treturn operator_assign(({0}_template)otherValue);\n", def.genName));
		source.append("\t\t\t}\n");
		source.append(MessageFormat.format("\t\t\tthrow new TtcnError(\"Internal Error: The left operand of assignment is not of type {0}_template.\");\n", def.displayName));
		source.append("\t\t}\n");

		source.append("\t\t@Override\n");
		source.append(MessageFormat.format("\t\tpublic boolean match(Base_Type otherValue, final boolean legacy) '{'\n", def.genName));
		source.append(MessageFormat.format("\t\t\tif (otherValue instanceof {0}) '{'\n", def.genName));
		source.append(MessageFormat.format("\t\t\t\treturn match(({0})otherValue, legacy);\n", def.genName));
		source.append("\t\t\t}\n");
		source.append(MessageFormat.format("\t\t\tthrow new TtcnError(\"Internal Error: The left operand of assignment is not of type {0}.\");\n", def.displayName));
		source.append("\t\t}\n");

		if (aData.isDebug()) {
			source.append("\t\t/**\n");
			source.append("\t\t * Matches the provided value against this template.\n");
			source.append("\t\t *\n");
			source.append("\t\t * @param other_value the value to be matched.\n");
			source.append("\t\t * */\n");
		}
		source.append(MessageFormat.format("\t\tpublic boolean match(final {0}.function_pointer other_value) '{'\n", def.genName));
		source.append("\t\t\treturn match(other_value, false);\n");
		source.append("\t\t}\n");

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
		source.append(MessageFormat.format("\t\tpublic boolean match(final {0}.function_pointer other_value, final boolean legacy) '{'\n", def.genName));
		source.append("\t\t\tswitch (template_selection) {\n");
		source.append("\t\t\tcase ANY_VALUE:\n");
		source.append("\t\t\tcase ANY_OR_OMIT:\n");
		source.append("\t\t\t\treturn true;\n");
		source.append("\t\t\tcase OMIT_VALUE:\n");
		source.append("\t\t\t\treturn false;\n");
		source.append("\t\t\tcase SPECIFIC_VALUE:\n");
		source.append("\t\t\t\treturn single_value.getDefinitionName().equals(other_value.getDefinitionName());\n");
		source.append("\t\t\tcase VALUE_LIST:\n");
		source.append("\t\t\tcase COMPLEMENTED_LIST:\n");
		source.append("\t\t\t\tfor(int i = 0 ; i < value_list.size(); i++) {\n");
		source.append("\t\t\t\t\tif(value_list.get(i).match(other_value, legacy)) {\n");
		source.append("\t\t\t\t\t\treturn template_selection == template_sel.VALUE_LIST;\n");
		source.append("\t\t\t\t\t}\n");
		source.append("\t\t\t\t}\n");
		source.append("\t\t\t\treturn template_selection == template_sel.COMPLEMENTED_LIST;\n");
		source.append("\t\t\tdefault:\n");
		source.append(MessageFormat.format("\t\t\t\tthrow new TtcnError(\"Matching with an uninitialized/unsupported {0} template.\");\n", def.displayName));
		source.append("\t\t\t}\n");
		source.append("\t\t}\n");

		if (aData.isDebug()) {
			source.append("\t\t/**\n");
			source.append("\t\t * Matches the provided value against this template.\n");
			source.append("\t\t *\n");
			source.append("\t\t * @param other_value the value to be matched.\n");
			source.append("\t\t * */\n");
		}
		source.append(MessageFormat.format("\t\tpublic boolean match(final {0} other_value) '{'\n", def.genName));
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
		source.append(MessageFormat.format("\t\tpublic boolean match(final {0} other_value, final boolean legacy) '{'\n", def.genName));
		source.append("\t\t\tif (!other_value.is_bound()) {\n");
		source.append("\t\t\t\treturn false;\n");
		source.append("\t\t\t}\n");
		source.append("\t\t\treturn match(other_value.referred_function);\n");
		source.append("\t\t}\n\n");

		source.append("\t\t@Override\n");
		source.append(MessageFormat.format("\t\tpublic {0} valueof() '{'\n", def.genName));
		source.append("\t\t\tif (!template_selection.equals(template_sel.SPECIFIC_VALUE) || is_ifPresent) {\n");
		source.append(MessageFormat.format("\t\t\t\tthrow new TtcnError(\"Performing a valueof or send operation on a non-specific {0} template.\");\n", def.displayName));
		source.append("\t\t\t}\n");
		source.append(MessageFormat.format("\t\t\treturn new {0}(single_value);\n", def.genName));
		source.append("\t\t}\n");

		source.append("\t\t@Override\n");
		source.append("\t\tpublic void set_type(final template_sel templateType, final int listLength) {\n");
		source.append("\t\t\tif (!template_sel.VALUE_LIST.equals(templateType) &&\n");
		source.append("\t\t\t\t!template_sel.COMPLEMENTED_LIST.equals(templateType)) {\n");
		source.append(MessageFormat.format("\t\t\t\tthrow new TtcnError(\"Setting an invalid type for an {0} template.\");\n", def.displayName));
		source.append("\t\t\t}\n");
		source.append("\t\t\tclean_up();\n");
		source.append("\t\t\tset_selection(templateType);\n");
		source.append(MessageFormat.format("\t\t\tvalue_list = new ArrayList<{0}_template>(listLength);\n", def.genName));
		source.append("\t\t\tfor(int i = 0; i < listLength; i++) {\n");
		source.append(MessageFormat.format("\t\t\t\tvalue_list.add(new {0}_template());\n", def.genName));
		source.append("\t\t\t}\n");
		source.append("\t\t}\n\n");

		source.append("\t\t@Override\n");
		source.append(MessageFormat.format("\t\tpublic {0}_template list_item(final int listIndex) '{'\n", def.genName));
		source.append("\t\t\tif (!template_sel.VALUE_LIST.equals(template_selection) &&\n");
		source.append("\t\t\t\t!template_sel.COMPLEMENTED_LIST.equals(template_selection)) {\n");
		source.append(MessageFormat.format("\t\t\t\tthrow new TtcnError(\"Accessing a list element of a non-list template of type {0}.\");\n", def.displayName));
		source.append("\t\t\t}\n");
		source.append("\t\t\tif (listIndex < 0) {\n");
		source.append(MessageFormat.format("\t\t\t\tthrow new TtcnError(MessageFormat.format(\"Internal error: Accessing a value list template of type {0} using a negative index ('{'0'}').\", listIndex));\n", def.displayName));
		source.append("\t\t\t} else if (listIndex > value_list.size()) {\n");
		source.append(MessageFormat.format("\t\t\t\tthrow new TtcnError(\"Index overflow in a value list template of type {0}.\");\n", def.displayName));
		source.append("\t\t\t}\n");
		source.append("\t\t\treturn value_list.get(listIndex);\n");
		source.append("\t\t}\n\n");

		source.append("\t\t@Override\n");
		source.append("\t\tpublic boolean match_omit(final boolean legacy) {\n");
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
		source.append("\t\t\t\t\tfor (int i = 0 ; i < value_list.size(); i++) {\n");
		source.append("\t\t\t\t\t\tif (value_list.get(i).match_omit(legacy)) {\n");
		source.append("\t\t\t\t\t\t\treturn template_selection == template_sel.VALUE_LIST;\n");
		source.append("\t\t\t\t\t\t}\n");
		source.append("\t\t\t\t\t}\n");
		source.append("\t\t\t\t\treturn template_selection == template_sel.COMPLEMENTED_LIST;\n");
		source.append("\t\t\t\t}\n");
		source.append("\t\t\t\treturn false;\n");
		source.append("\t\t\tdefault:\n");
		source.append("\t\t\t\treturn false;\n");
		source.append("\t\t\t}\n");
		source.append("\t\t}\n");

		source.append("\t\t@Override\n");
		source.append("\t\tpublic void log() {\n");
		source.append("\t\t\tswitch (template_selection) {\n");
		source.append("\t\t\tcase SPECIFIC_VALUE:\n");
		source.append("\t\t\t\tif(single_value == null) {\n");
		source.append("\t\t\t\t\tTTCN_Logger.log_event_unbound();\n");
		source.append("\t\t\t\t} else {\n");
		source.append("\t\t\t\t\tTTCN_Logger.log_event(\"refers(%s)\", single_value.getDefinitionName());\n");
		source.append("\t\t\t\t}\n");
		source.append("\t\t\t\tbreak;\n");
		source.append("\t\t\tcase COMPLEMENTED_LIST:\n");
		source.append("\t\t\t\tTTCN_Logger.log_event_str(\"complement\");\n");
		source.append("\t\t\tcase VALUE_LIST:\n");
		source.append("\t\t\t\tTTCN_Logger.log_char('(');\n");
		source.append("\t\t\t\tfor (int list_count = 0; list_count < value_list.size(); list_count++) {\n");
		source.append("\t\t\t\t\tif (list_count > 0) {\n");
		source.append("\t\t\t\t\t\tTTCN_Logger.log_event_str(\", \");\n");
		source.append("\t\t\t\t\t}\n");
		source.append("\t\t\t\t\tvalue_list.get(list_count).log();\n");
		source.append("\t\t\t\t}\n");
		source.append("\t\t\t\tTTCN_Logger.log_char(')');\n");
		source.append("\t\t\t\tbreak;\n");
		source.append("\t\t\tdefault:\n");
		source.append("\t\t\t\tlog_generic();\n");
		source.append("\t\t\t\tbreak;\n");
		source.append("\t\t\t}\n");
		source.append("\t\t\tlog_ifpresent();\n");
		source.append("\t\t}\n");

		source.append("\t\t@Override\n");
		source.append("\t\tpublic void log_match(final Base_Type match_value, final boolean legacy) {\n");
		source.append(MessageFormat.format("\t\t\tif (match_value instanceof {0}) '{'\n", def.genName));
		source.append(MessageFormat.format("\t\t\t\tlog_match(({0})match_value, legacy);\n", def.genName));
		source.append("\t\t\t\treturn;\n");
		source.append("\t\t\t}\n\n");
		source.append(MessageFormat.format("\t\t\tthrow new TtcnError(\"Internal Error: value can not be cast to {0}.\");\n", def.displayName));
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
		source.append(MessageFormat.format("\t\tpublic void log_match(final {0} match_value, final boolean legacy) '{'\n", def.genName));
		source.append("\t\t\tmatch_value.log();\n");
		source.append("\t\t\tTTCN_Logger.log_event_str(\" with \");\n");
		source.append("\t\t\tlog();\n");
		source.append("\t\t\tif (match(match_value, legacy)) {\n");
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
		source.append("\t\t\t\tbreak;\n");
		source.append("\t\t\tcase SPECIFIC_VALUE:\n");
		source.append("\t\t\t\tif (single_value == null) {\n");
		switch (def.type) {
		case FUNCTION:
			source.append("\t\t\t\t\tthrow new TtcnError(\"Text encoder: Encoding an unbound function reference.\");\n");
			break;
		case ALTSTEP:
			source.append("\t\t\t\t\tthrow new TtcnError(\"Text encoder: Encoding an unbound altstep reference.\");\n");
			break;
		case TESTCASE:
			source.append("\t\t\t\t\tthrow new TtcnError(\"Text encoder: Encoding an unbound testcase reference.\");\n");
			break;
		}
		source.append("\t\t\t\t}\n");
		source.append(MessageFormat.format("\t\t\t\tif (single_value == {0}.nullValue) '{'\n", def.genName));
		source.append("\t\t\t\t\ttext_buf.push_string(\"\");\n");
		source.append("\t\t\t\t} else {\n");
		source.append("\t\t\t\t\tfinal String moduleName = single_value.getModuleName();\n");
		source.append("\t\t\t\t\tfinal String definitionName = single_value.getDefinitionName();\n");
		source.append("\t\t\t\t\ttext_buf.push_string(moduleName);\n");
		source.append("\t\t\t\t\ttext_buf.push_string(definitionName);\n");
		source.append("\t\t\t\t}\n");
		source.append("\t\t\t\tbreak;\n");
		source.append("\t\t\tcase VALUE_LIST:\n");
		source.append("\t\t\tcase COMPLEMENTED_LIST:\n");
		source.append("\t\t\t\ttext_buf.push_int(value_list.size());\n");
		source.append("\t\t\t\tfor (int i = 0; i < value_list.size(); i++) {\n");
		source.append("\t\t\t\t\tvalue_list.get(i).encode_text(text_buf);\n");
		source.append("\t\t\t\t}\n");
		source.append("\t\t\t\tbreak;\n");
		source.append("\t\t\tdefault:\n");
		source.append( MessageFormat.format( "\t\t\t\tthrow new TtcnError(\"Text encoder: Encoding an uninitialized/unsupported template of type {0}.\");\n", def.displayName));
		source.append("\t\t\t}\n");
		source.append("\t\t}\n\n");

		source.append("\t\t@Override\n");
		source.append("\t\tpublic void decode_text(final Text_Buf text_buf) {\n");
		source.append("\t\t\tdecode_text_base(text_buf);\n");
		source.append("\t\t\tswitch (template_selection) {\n");
		source.append("\t\t\tcase OMIT_VALUE:\n");
		source.append("\t\t\tcase ANY_VALUE:\n");
		source.append("\t\t\tcase ANY_OR_OMIT:\n");
		source.append("\t\t\t\tbreak;\n");
		source.append("\t\t\tcase SPECIFIC_VALUE: {\n");
		generateDecodeTextInternal(aData, source, def, "single_value");
		source.append("\t\t\t\tbreak;\n");
		source.append("\t\t\t}\n");
		source.append("\t\t\tcase VALUE_LIST:\n");
		source.append("\t\t\tcase COMPLEMENTED_LIST: {\n");
		source.append("\t\t\t\tfinal int size = text_buf.pull_int().get_int();\n");
		source.append( MessageFormat.format( "\t\t\t\tvalue_list = new ArrayList<{0}_template>(size);\n", def.genName));
		source.append("\t\t\t\tfor (int i = 0; i < size; i++) {\n");
		source.append( MessageFormat.format( "\t\t\t\t\tfinal {0}_template temp = new {0}_template();\n", def.genName));
		source.append("\t\t\t\t\ttemp.decode_text(text_buf);\n");
		source.append("\t\t\t\t\tvalue_list.add(temp);\n");
		source.append("\t\t\t\t}\n");
		source.append("\t\t\t\tbreak;\n");
		source.append("\t\t\t}\n");
		source.append("\t\t\tdefault:\n");
		source.append( MessageFormat.format( "\t\t\t\tthrow new TtcnError(\"Text decoder: An unknown/unsupported selection was received in a template of type {0}.\");\n", def.displayName));
		source.append("\t\t\t}\n");
		source.append("\t\t}\n\n");

		source.append("\t\t@Override\n");
		source.append("\t\tpublic void set_param(final Module_Parameter param) {\n");
		source.append("\t\t\tparam.error(\"Not supported.\");\n");
		source.append("\t\t}\n\n");

		source.append("\t\t@Override\n");
		source.append("\t\tpublic void check_restriction(final template_res restriction, final String name, final boolean legacy) {\n");
		source.append("\t\t\tif (template_selection == template_sel.UNINITIALIZED_TEMPLATE) {\n");
		source.append("\t\t\t\treturn;\n");
		source.append("\t\t\t}\n");
		source.append("\t\t\tswitch ((name != null && restriction == template_res.TR_VALUE) ? template_res.TR_OMIT : restriction) {\n");
		source.append("\t\t\tcase TR_VALUE:\n");
		source.append("\t\t\t\tif (!is_ifPresent && template_selection == template_sel.SPECIFIC_VALUE) {\n");
		source.append("\t\t\t\t\treturn;\n");
		source.append("\t\t\t\t}\n");
		source.append("\t\t\t\tbreak;\n");
		source.append("\t\t\tcase TR_OMIT:\n");
		source.append("\t\t\t\tif (!is_ifPresent && (template_selection == template_sel.OMIT_VALUE || template_selection == template_sel.SPECIFIC_VALUE)) {\n");
		source.append("\t\t\t\t\treturn;\n");
		source.append("\t\t\t\t}\n");
		source.append("\t\t\t\tbreak;\n");
		source.append("\t\t\tcase TR_PRESENT:\n");
		source.append("\t\t\t\tif (!match_omit(legacy)) {\n");
		source.append("\t\t\t\t\treturn;\n");
		source.append("\t\t\t\t}\n");
		source.append("\t\t\t\tbreak;\n");
		source.append("\t\t\tdefault:\n");
		source.append("\t\t\t\treturn;\n");
		source.append("\t\t\t}\n");
		source.append(MessageFormat.format("\t\t\tthrow new TtcnError(MessageFormat.format(\"Restriction `'{'0'}''''' on template of type '{'1'}' violated.\", get_res_name(restriction), name == null ? \"{0}\" : name));\n", def.displayName));
		source.append("\t\t}\n");

		source.append("\t}\n\n");
	}
}
