/******************************************************************************
 * Copyright (c) 2000-2017 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.types;

import java.text.MessageFormat;
import java.util.ArrayList;

import org.eclipse.titan.designer.compiler.JavaGenData;

/**
 * Utility class for generating the value and template classes for function/altstep/testcase types.
 *
 * starting/activating/executing is not yet supported
 * 
 * @author Kristof Szabados
 * */
public class FunctionReferenceGenerator {

	enum fatType {FUNCTION, ALTSTEP, TESTCASE};
	
	public static class FunctionReferenceDefinition {
		private String genName;
		private String displayName;
		public String returnType;
		public fatType type;
		public boolean runsOnSelf;
		public boolean isStartable;
		public String formalParList;
		public String actualParList;
		public ArrayList<String> parameters;

		public FunctionReferenceDefinition(final String genName, final String displayName) {
			this.genName = genName;
			this.displayName = displayName;
		}
	}

	/**
	 * This function can be used to generate the value class of function/altstep/tetscase types
	 *
	 * defFunctionrefClass in compiler2/functionref.{h,c}
	 *
	 * @param aData only used to update imports if needed.
	 * @param source where the source code is to be generated.
	 * @param def the function definition to generate code for.
	 * */
	public static void generateValueClass(final JavaGenData aData, final StringBuilder source, final FunctionReferenceDefinition def) {
		aData.addBuiltinTypeImport("TitanBoolean");
		aData.addBuiltinTypeImport("Base_Type");
		aData.addCommonLibraryImport("TtcnError");

		source.append(MessageFormat.format("public static class {0} extends Base_Type '{'\n", def.genName));
		switch (def.type) {
		case FUNCTION:
			source.append("public interface function_pointer {\n");
			source.append("String getId();\n");
			if (def.returnType == null) {
				source.append("void");
			} else {
				source.append(def.returnType);
			}
			source.append(MessageFormat.format(" invoke({0});\n", def.formalParList));
			source.append("}\n");
			break;
		case ALTSTEP:
			source.append("public interface function_pointer {\n");
			source.append("String getId();\n");
			if (def.returnType == null) {
				source.append("void");
			} else {
				source.append(def.returnType);
			}
			source.append(MessageFormat.format(" invoke_standalone({0});\n", def.formalParList));
			source.append(MessageFormat.format("Default_Base activate({0});\n", def.formalParList));
			source.append(MessageFormat.format("TitanAlt_Status invoke({0});\n", def.formalParList));
			source.append("}\n");
			break;
		case TESTCASE:
			source.append("public interface function_pointer {\n");
			source.append("String getId();\n");
			source.append(MessageFormat.format("TitanVerdictType execute({0});\n", def.formalParList));
			source.append("}\n");
			break;
		}
		source.append("private function_pointer referred_function;\n");

		source.append(MessageFormat.format("public {0}() '{'\n", def.genName));
		source.append("referred_function = null;\n");
		source.append("}\n");
		
		//TODO check if this kind of constructor is a good idea or not!!!
		source.append(MessageFormat.format("public {0}(final function_pointer otherValue) '{'\n", def.genName));
		source.append("referred_function = otherValue;\n");
		source.append("}\n");
		
		source.append(MessageFormat.format("public {0}(final {0} otherValue) '{'\n", def.genName));
		source.append(MessageFormat.format("otherValue.mustBound(\"Copying an unbound {0}.\");\n\n", def.displayName));
		source.append("referred_function = otherValue.referred_function;\n");
		source.append("}\n");

		source.append(MessageFormat.format("public {0} assign(final function_pointer otherValue) '{'\n", def.genName));
		source.append("referred_function = otherValue;\n");
		source.append("return this;\n");
		source.append("}\n");

		source.append(MessageFormat.format("public {0} assign(final {0} otherValue) '{'\n", def.genName));
		source.append(MessageFormat.format("otherValue.mustBound(\"Assignment of an unbound {0}.\");\n\n", def.displayName));
		source.append("referred_function = otherValue.referred_function;\n");
		source.append("return this;\n");
		source.append("}\n");

		source.append("@Override\n");
		source.append("public Base_Type assign(Base_Type otherValue) {\n");
		source.append(MessageFormat.format("if (otherValue instanceof {0}) '{'\n", def.genName));
		source.append(MessageFormat.format("return assign(({0})otherValue);\n", def.genName));
		source.append("}\n");
		source.append(MessageFormat.format("throw new TtcnError(\"Internal Error: The left operand of assignment is not of type {0}.\");\n", def.displayName));
		source.append("}\n");

		source.append("public TitanBoolean operatorEquals(final function_pointer otherValue) {\n");
		source.append(MessageFormat.format("mustBound(\"Unbound left operand of {0} comparison.\");\n\n", def.displayName));
		source.append("return new TitanBoolean(referred_function.getId().equals(otherValue.getId()));\n");
		source.append("}\n");
		
		source.append(MessageFormat.format("public TitanBoolean operatorEquals(final {0} otherValue) '{'\n", def.genName));
		source.append(MessageFormat.format("mustBound(\"Unbound left operand of {0} comparison.\");\n", def.displayName));
		source.append(MessageFormat.format("otherValue.mustBound(\"Unbound right operand of {0} comparison.\");\n\n", def.displayName));
		source.append("return new TitanBoolean(referred_function.getId().equals(otherValue.referred_function.getId()));\n");
		source.append("}\n");

		source.append("@Override\n");
		source.append("public TitanBoolean operatorEquals(Base_Type otherValue) {\n");
		source.append(MessageFormat.format("if (otherValue instanceof {0}) '{'\n", def.genName));
		source.append(MessageFormat.format("return operatorEquals(({0})otherValue);\n", def.genName));
		source.append("}\n");
		source.append(MessageFormat.format("throw new TtcnError(\"Internal Error: The left operand of comparison is not of type {0}.\");\n", def.displayName));
		source.append("}\n");
			
		source.append("public TitanBoolean operatorNotEquals(final function_pointer otherValue) {\n");
		source.append("return operatorEquals(otherValue).not();\n");
		source.append("}\n");
		source.append(MessageFormat.format("public TitanBoolean operatorNotEquals(final {0} otherValue) '{'\n", def.genName));
		source.append("return operatorEquals(otherValue).not();\n");
		source.append("}\n");

		switch (def.type) {
		case FUNCTION:
			source.append("public ");
			if (def.returnType == null) {
				source.append("void");
			} else {
				source.append(def.returnType);
			}
			source.append(MessageFormat.format(" invoke({0}) '{'\n", def.formalParList));
			source.append("mustBound(\"Call of unbound function.\");\n");
					//check for null
			if (def.returnType != null) {
				source.append("return ");
			}
			source.append("referred_function.invoke(");
			source.append(def.actualParList);
			source.append(");\n");
			source.append("}\n");
			break;
		case ALTSTEP:
			source.append(MessageFormat.format("public void invoke_standalone({0}) '{'\n", def.formalParList));
			source.append("mustBound(\"Call of unbound altstep.\");\n");
			source.append("referred_function.invoke_standalone(");
			source.append(def.actualParList);
			source.append(");\n");
			source.append("}\n");
			source.append(MessageFormat.format("public Default_Base activate({0}) '{'\n", def.formalParList));
			source.append("mustBound(\"Activation of unbound altstep.\");\n");
			source.append("return referred_function.activate(");
			source.append(def.actualParList);
			source.append(");\n");
			source.append("}\n");
			source.append(MessageFormat.format("public TitanAlt_Status invoke({0}) '{'\n", def.formalParList));
			source.append("mustBound(\"Call of unbound altstep.\");\n");
			source.append("return referred_function.invoke(");
			source.append(def.actualParList);
			source.append(");\n");
			source.append("}\n");
			break;
		case TESTCASE:
			source.append(MessageFormat.format("public TitanVerdictType execute({0}) '{'\n", def.formalParList));
			source.append("mustBound(\"Call of unbound testcase.\");\n");
			source.append("if (referred_function == null) {\n");
			source.append("throw new TtcnError(\"null reference cannot be executed.\");\n");
			source.append("}\n");
			source.append(MessageFormat.format("return referred_function.execute({0});", def.actualParList));
			source.append("}\n");
			break;
		}

		source.append("public boolean isBound() {\n");
		source.append("return referred_function != null;\n");
		source.append("}\n");
		source.append("public boolean isValue() {\n");
		source.append("return referred_function != null;\n");
		source.append("}\n");
		source.append("public boolean isPresent() {\n");
		source.append("return isBound();\n");
		source.append("}\n");
		source.append("public void cleanUp() {\n");
		source.append("referred_function = null;\n");
		source.append("}\n");
		source.append("public void mustBound( final String aErrorMessage ) {\n");
		source.append("if ( !isBound() ) {\n");
		source.append("throw new TtcnError( aErrorMessage );\n");
		source.append("}\n");
		source.append("}\n");
		source.append("}\n\n");
	}

	/**
	 * This function can be used to generate the template class of function/altstep/tetscase types
	 *
	 * defFunctionrefTemplate in compiler2/functionref.{h,c}
	 *
	 * @param aData only used to update imports if needed.
	 * @param source where the source code is to be generated.
	 * @param def the function definition to generate code for.
	 * */
	public static void generateTemplateClass(final JavaGenData aData, final StringBuilder source, final FunctionReferenceDefinition def) {
		aData.addBuiltinTypeImport("Base_Template");
		aData.addCommonLibraryImport("TtcnError");

		source.append(MessageFormat.format("public static class {0}_template extends Base_Template '{'\n", def.genName));
		source.append("//the single value\n");
		source.append(MessageFormat.format("private {0}.function_pointer single_value;\n", def.genName));
		source.append("// value_list part\n");
		source.append(MessageFormat.format("private ArrayList<{0}_template> value_list;\n", def.genName));

		source.append(MessageFormat.format("private void copy_template(final {0}_template other_value) '{'\n", def.genName));
		source.append("switch (other_value.templateSelection) {\n");
		source.append("case SPECIFIC_VALUE:\n");
		source.append("single_value = other_value.single_value;\n");
		source.append("break;\n");
		source.append("case OMIT_VALUE:\n");
		source.append("case ANY_VALUE:\n");
		source.append("case ANY_OR_OMIT:\n");
		source.append("break;\n");
		source.append("case VALUE_LIST:\n");
		source.append("case COMPLEMENTED_LIST:\n");
		source.append(MessageFormat.format("value_list = new ArrayList<{0}_template>(other_value.value_list.size());\n", def.genName));
		source.append("for(int i = 0; i < other_value.value_list.size(); i++) {\n");
		source.append(MessageFormat.format("final {0}_template temp = new {0}_template(other_value.value_list.get(i));\n", def.genName));
		source.append("value_list.add(temp);\n");
		source.append("}\n");
		source.append("break;\n");
		source.append("default:\n");
		source.append(MessageFormat.format("throw new TtcnError(\"Copying an uninitialized template of type {0}.\");\n", def.displayName));
		source.append("}\n");
		source.append("setSelection(other_value);\n");
		source.append("}\n");

		source.append(MessageFormat.format("public {0}_template() '{'\n", def.genName));
		source.append("}\n");
		source.append(MessageFormat.format("public {0}_template(final template_sel otherValue) '{'\n", def.genName));
		source.append("super(otherValue);\n");
		source.append("checkSingleSelection(otherValue);\n");
		source.append("}\n");

		source.append(MessageFormat.format("public {0}_template(final {0}.function_pointer otherValue) '{'\n", def.genName));
		source.append("super(template_sel.SPECIFIC_VALUE);\n");
		source.append("single_value = otherValue;\n");
		source.append("}\n");

		source.append(MessageFormat.format("public {0}_template(final {0} otherValue) '{'\n", def.genName));
		source.append("super(template_sel.SPECIFIC_VALUE);\n");
		source.append(MessageFormat.format("otherValue.mustBound(\"Creating a template from an unbound {0} value.\");\n", def.displayName));
		source.append("single_value = otherValue.referred_function;\n");
		source.append("}\n");

		source.append(MessageFormat.format("public {0}_template(final {0}_template otherValue) '{'\n", def.genName));
		source.append("copy_template(otherValue);\n");
		source.append("}\n");

		source.append("public void cleanUp(){\n");
		source.append("switch(templateSelection) {\n");
		source.append("case SPECIFIC_VALUE:\n");
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
		source.append("}\n");

		source.append("//originally operator=\n");
		source.append(MessageFormat.format("public {0}_template assign( final template_sel other_value ) '{'\n", def.genName));
		source.append("checkSingleSelection(other_value);\n");
		source.append("cleanUp();\n");
		source.append("setSelection(other_value);\n");
		source.append("return this;\n");
		source.append("}\n");

		source.append("//originally operator=\n");
		source.append(MessageFormat.format("public {0}_template assign( final {0}.function_pointer other_value ) '{'\n", def.genName));
		source.append("cleanUp();\n");
		source.append("setSelection(template_sel.SPECIFIC_VALUE);\n");
		source.append("single_value = other_value;\n");
		source.append("return this;\n");
		source.append("}\n");

		source.append("//originally operator=\n");
		source.append(MessageFormat.format("public {0}_template assign( final {0} other_value ) '{'\n", def.genName));
		source.append(MessageFormat.format("other_value.mustBound(\"Assignment of an unbound {0} value to a template.\");\n", def.displayName));
		source.append("cleanUp();\n");
		source.append("setSelection(template_sel.SPECIFIC_VALUE);\n");
		source.append("single_value = other_value.referred_function;\n");
		source.append("return this;\n");
		source.append("}\n");

		source.append("//originally operator=\n");
		source.append(MessageFormat.format("public {0}_template assign( final {0}_template other_value ) '{'\n", def.genName));
		source.append("if (other_value != this) {\n");
		source.append("cleanUp();\n");
		source.append("copy_template(other_value);\n");
		source.append("}\n");
		source.append("return this;\n");
		source.append("}\n");

		source.append("// originally match\n");
		source.append(MessageFormat.format("public TitanBoolean match(final {0}.function_pointer other_value) '{'\n", def.genName));
		source.append("return match(other_value, false);\n");
		source.append("}\n");

		source.append("// originally match\n");
		source.append(MessageFormat.format("public TitanBoolean match(final {0}.function_pointer other_value, final boolean legacy) '{'\n", def.genName));
		source.append("switch (templateSelection) {\n");
		source.append("case ANY_VALUE:\n");
		source.append("case ANY_OR_OMIT:\n");
		source.append("return new TitanBoolean(true);\n");
		source.append("case OMIT_VALUE:\n");
		source.append("return new TitanBoolean(false);\n");
		source.append("case SPECIFIC_VALUE:\n");
		source.append("return new TitanBoolean(single_value.getId().equals(other_value.getId()));\n");
		source.append("case VALUE_LIST:\n");
		source.append("case COMPLEMENTED_LIST:\n");
		source.append("for(int i = 0 ; i < value_list.size(); i++) {\n");
		source.append("if(value_list.get(i).match(other_value, legacy).getValue()) {\n");
		source.append("return new TitanBoolean(templateSelection == template_sel.VALUE_LIST);\n");
		source.append("}\n");
		source.append("}\n");
		source.append("return new TitanBoolean(templateSelection == template_sel.COMPLEMENTED_LIST);\n");
		source.append("default:\n");
		source.append(MessageFormat.format("throw new TtcnError(\"Matching with an uninitialized/unsupported {0} template.\");\n", def.displayName));
		source.append("}\n");
		source.append("}\n");

		source.append("// originally match\n");
		source.append(MessageFormat.format("public TitanBoolean match(final {0} other_value) '{'\n", def.genName));
		source.append("return match(other_value, false);\n");
		source.append("}\n");

		source.append("// originally match\n");
		source.append(MessageFormat.format("public TitanBoolean match(final {0} other_value, final boolean legacy) '{'\n", def.genName));
		source.append("if (!other_value.isBound()) {\n");
		source.append("return new TitanBoolean(false);\n");
		source.append("}\n");
		source.append("return match(other_value.referred_function);\n");
		source.append("}\n");

		source.append(MessageFormat.format("public {0} valueOf() '{'\n", def.genName));
		source.append("if (!templateSelection.equals(template_sel.SPECIFIC_VALUE) || is_ifPresent) {\n");
		source.append(MessageFormat.format("throw new TtcnError(\"Performing a valueof or send operation on a non-specific {0} template.\");\n", def.displayName));
		source.append("}\n");
		source.append(MessageFormat.format("return new {0}(single_value);\n", def.genName));
		source.append("}\n");

		source.append("// originally set_type\n");
		source.append("public void setType(final template_sel templateType) {\n");
		source.append("setType(templateType, 0);\n");
		source.append("}\n");

		source.append("// originally set_type\n");
		source.append("public void setType(final template_sel templateType, final int listLength) {\n");
		source.append("if (!template_sel.VALUE_LIST.equals(templateSelection) &&\n");
		source.append("!template_sel.COMPLEMENTED_LIST.equals(templateSelection)) {\n");
		source.append(MessageFormat.format("throw new TtcnError(\"Setting an invalid type for an {0} template.\");\n", def.displayName));
		source.append("}\n");
		source.append("cleanUp();\n");
		source.append("setSelection(templateType);\n");
		source.append(MessageFormat.format("value_list = new ArrayList<{0}_template>(listLength);\n", def.genName));
		source.append("for(int i = 0; i < listLength; i++) {\n");
		source.append(MessageFormat.format("value_list.add(new {0}_template());\n", def.genName));
		source.append("}\n");
		source.append("}\n");

		source.append("// originally list_iem\n");
		source.append(MessageFormat.format("public {0}_template listItem(final int listIndex) '{'\n", def.genName));
		source.append("if (!template_sel.VALUE_LIST.equals(templateSelection) &&\n");
		source.append("!template_sel.COMPLEMENTED_LIST.equals(templateSelection)) {\n");
		source.append(MessageFormat.format("throw new TtcnError(\"Accessing a list element of a non-list template of type {0}.\");\n", def.displayName));
		source.append("}\n");
		source.append("if (listIndex > value_list.size()) {\n");
		source.append(MessageFormat.format("throw new TtcnError(\"Index overflow in a value list template of type {0}.\");\n", def.displayName));
		source.append("}\n");
		source.append("return value_list.get(listIndex);\n");
		source.append("}\n");

		source.append("public boolean isPresent(final boolean legacy) {\n");
		source.append("if (templateSelection == template_sel.UNINITIALIZED_TEMPLATE) {\n");
		source.append("return false;\n");
		source.append("}\n");
		source.append("return !match_omit(legacy);\n");
		source.append("}\n");

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
		source.append("}\n");
		source.append("}\n");
	}
}
