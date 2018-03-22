/******************************************************************************
 * Copyright (c) 2000-2017 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.types;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.titan.designer.compiler.JavaGenData;

/**
 * @author Farkas Izabella Ingrid
 * */
public class EnumeratedGenerator {

	private static final String UNKNOWN_VALUE = "UNKNOWN_VALUE";
	private static final String UNBOUND_VALUE ="UNBOUND_VALUE";

	public static class Enum_field {
		private String name;
		private String displayName;

		private long value;

		public Enum_field(final String aName, final String aDisplayName, final long aValue) {
			name = aName;
			displayName = aDisplayName;
			value = aValue;
		}
	}

	public static class Enum_Defs {
		private List<Enum_field> items;
		private String name;
		private String displayName;
		private String templateName;
		private boolean hasRaw;
		private long firstUnused = -1;  //first unused value for this enum type
		private long secondUnused = -1; //second unused value for this enum type

		public Enum_Defs(final List<Enum_field> aItems, final String aName, final String aDisplayName, final String aTemplateName, final boolean aHasRaw){
			items = aItems;
			name = aName;
			displayName = aDisplayName;
			templateName = aTemplateName;
			hasRaw = aHasRaw;
			calculateFirstAndSecondUnusedValues();
		}

		//This function supposes that the enum class is already checked and error free
		private void calculateFirstAndSecondUnusedValues() {
			if( firstUnused != -1 ) {
				return; //function already have been called
			}
			final Map<Long, Enum_field> valueMap = new HashMap<Long, Enum_field>(items.size());

			for( int i = 0, size = items.size(); i < size; i++) {
				final Enum_field item = items.get(i);
				valueMap.put(item.value, item);
			}

			long firstUnused = Long.valueOf(0);
			while (valueMap.containsKey(firstUnused)) {
				firstUnused++;
			}

			this.firstUnused = firstUnused;
			firstUnused++;
			while (valueMap.containsKey(firstUnused)) {
				firstUnused++;
			}
			secondUnused = firstUnused;
			valueMap.clear();
		}
	}

	private EnumeratedGenerator() {
		// private to disable instantiation
	}

	public static void generateValueClass(final JavaGenData aData, final StringBuilder source, final Enum_Defs e_defs ) {
		aData.addBuiltinTypeImport("TitanInteger");
		aData.addBuiltinTypeImport( "Base_Type" );
		aData.addBuiltinTypeImport( "Base_Template" );
		aData.addBuiltinTypeImport("Text_Buf");
		aData.addBuiltinTypeImport("TtcnError");
		aData.addImport( "java.text.MessageFormat" );

		//		if(needsAlias()) { ???
		source.append(MessageFormat.format("public static class {0} extends Base_Type '{'\n", e_defs.name));
		//== enum_type ==
		source.append("public enum enum_type {\n");
		final StringBuilder helper = new StringBuilder();
		final int size = e_defs.items.size();
		Enum_field item = null;
		for ( int i=0; i<size; i++) {
			item = e_defs.items.get(i);
			source.append(MessageFormat.format("{0}", item.name));
			source.append(MessageFormat.format("({0}),\n", item.value));
			helper.append("case ").append(MessageFormat.format("{0}", item.value)).append(": ");
			helper.append(" return ").append(MessageFormat.format("{0}", item.name)).append(";\n");
		}

		source.append(MessageFormat.format("{0}({1}),\n", UNKNOWN_VALUE, e_defs.firstUnused));
		source.append(MessageFormat.format("{0}({1});\n", UNBOUND_VALUE, e_defs.secondUnused));
		helper.append("case ").append(MessageFormat.format("{0}", e_defs.firstUnused)).append(": ");
		helper.append(" return ").append("UNKNOWN_VALUE").append(";\n");
		helper.append("case ").append(MessageFormat.format("{0}", e_defs.secondUnused)).append(": ");
		helper.append(" return ").append("UNBOUND_VALUE").append(";\n");

		source.append("\n private int enum_num;\n");

		//== constructors for enum_type ==

		source.append("enum_type(final int num) {\n");
		source.append("this.enum_num = num;\n");
		source.append("}\n\n");

		source.append("private int getInt() {\n");
		source.append("return enum_num;\n");
		source.append("}\n\n");
		generateValueEnumGetValue(source, helper);
		source.append("}\n\n");
		// end of enum_type

		//== enum_value ==
		source.append("public enum_type enum_value;\n");

		source.append("//===Constructors===;\n");
		generateValueConstructors(source,e_defs.name);

		//== functions ==
		source.append("//===Methods===;\n");
		generateValueAssign(source, e_defs.name);
		generateValueOperatorEquals(source, e_defs.name, e_defs.displayName);
		generateValueOperatorNotEquals(source, e_defs.name);
		generateValueIsLessThan(source, e_defs.name);
		generateValueIsLessThanOrEqual(source, e_defs.name);
		generateValueIsGreaterThan(source, e_defs.name);
		generateValueIsGreaterThanOrEqual(source, e_defs.name);
		generateValueIsPresent(source);
		generateValueIsBound(source);
		generateMustBound(source);
		generateValueIsValue(source);
		generateValueCleanUp(source);
		generateValueIsValidEnum(source, e_defs.name);
		generateValueIntToEnum(source);
		generateValueEnumToInt(source, e_defs.name);
		generateValueStrToEnum(source);
		generateValueEnumToStr(source);
		generateValueAsInt(source);
		generateValueFromInt(source);
		generateValueToString(source);
		generateLog(source);
		generateValueEncodeDecodeText(source, e_defs.displayName);
		source.append("}\n");
	}

	//TODO: set_param(Module_Param& param);

	public static void generateTemplateClass(final JavaGenData aData, final StringBuilder source, final Enum_Defs e_defs){
		aData.addBuiltinTypeImport("TitanInteger");
		aData.addBuiltinTypeImport( "Base_Type" );
		aData.addBuiltinTypeImport( "Base_Template" );
		aData.addBuiltinTypeImport("Text_Buf");
		aData.addBuiltinTypeImport("TtcnError");
		aData.addCommonLibraryImport("TtcnLogger");
		aData.addImport( "java.text.MessageFormat" );
		aData.addImport("java.util.ArrayList");

		source.append(MessageFormat.format("public static class {0}_template extends Base_Template '{'\n", e_defs.name, e_defs.templateName));

		generateTemplateDeclaration(source, e_defs.name);
		generatetemplateCopyTemplate(source, e_defs.name);
		generateTemplateConstructors(source, e_defs.name);
		generateTemplateCleanUp(source);
		generateTemplateIsBound(source);
		generateTemplateIsValue(source, e_defs.name);
		generateTemplateAssign(source,e_defs.name);
		generateTemplateMatch(source,  e_defs.name);
		generateTemplateValueOf(source, e_defs.name);
		generateTemplateSetType(source,  e_defs.name);
		generateTemplateListItem(source, e_defs.name);
		generateTemplateIsPresent(source);
		generateTemplateMatchOmit(source);
		generateTemplateLog(source, e_defs.name);
		generateTemplateLogMatch(source, e_defs.name, e_defs.displayName);
		generateTemplateEncodeDecodeText(source, e_defs.name, e_defs.displayName);

		//FIXME implement encode
		//FIXME implement decode
		//FIXME implement set_param
		//FIXME implement check_restriction
		source.append("}\n");
	}

	//===

	private static void generateValueToString(final StringBuilder source) {
		source.append("public String toString() {\n");
		source.append("return enum_value.name() + \"(\"+enum_value.enum_num+\")\";\n");
		source.append("}\n\n");
	}

	private static void generateValueEnumGetValue(final StringBuilder source, final StringBuilder helper) {
		source.append("public static enum_type getValue(final int index) {\n");
		source.append("switch (index) {\n");
		source.append(helper);
		source.append("default:\n");
		source.append("return null;\n");
		source.append("}\n");
		source.append("}\n\n");
	}

	private static void generateValueConstructors(final StringBuilder source,final String name) {
		//empty
		source.append(MessageFormat.format("public {0}() '{'\n", name));
		source.append(MessageFormat.format("enum_value = enum_type.{0};\n", UNBOUND_VALUE));
		source.append("}\n\n");

		// own type
		source.append(MessageFormat.format("public {0}(final {0} otherValue) '{'\n", name));
		source.append(MessageFormat.format("enum_value = otherValue.enum_value;\n", name));
		source.append("}\n\n");

		// enum_type
		source.append(MessageFormat.format("public {0}(final {0}.enum_type otherValue ) '{'\n", name));
		source.append("enum_value = otherValue;\n");
		source.append("}\n\n");

		//arg int
		source.append(MessageFormat.format("public {0}(final int otherValue) '{'\n", name));
		source.append("if (!isValidEnum(otherValue)) {\n");
		source.append(MessageFormat.format("throw new TtcnError(MessageFormat.format(\"Initializing a variable of enumerated type `{0}'' with invalid numeric value {1} .\", otherValue));\n", name));
		source.append("}\n\n");
		source.append(MessageFormat.format("enum_value =  enum_type.getValue(otherValue);\n", name));
		source.append("}\n\n");
	}

	private static void generateValueIsValidEnum(final StringBuilder source, final String name) {
		source.append("public static boolean isValidEnum(final int otherValue) {\n");
		source.append("final enum_type helper =  enum_type.getValue(otherValue);\n");
		source.append("return helper != null && helper != enum_type.UNKNOWN_VALUE && helper != enum_type.UNBOUND_VALUE ;\n");
		source.append("}\n\n");
	}

	private static void generateValueEnumToStr(final StringBuilder source) {
		source.append("public static String enum2str(final enum_type enumPar) {\n");
		source.append("	return enumPar.name();\n");
		source.append("}\n\n");
	}

	private static void generateLog(final StringBuilder source) {
		source.append("public void log() {\n");
		source.append("if (enum_value != enum_type.UNBOUND_VALUE) {\n");
		source.append("TtcnLogger.log_event_enum(enum2str(enum_value), enum2int(enum_value));\n");
		source.append("} else {\n");
		source.append("TtcnLogger.log_event_unbound();\n");
		source.append("}\n");
		source.append("}\n\n");
	}

	private static void generateValueEncodeDecodeText(final StringBuilder source, final String name) {
		source.append("public void encode_text(final Text_Buf text_buf) {\n");
		source.append("if (enum_value == enum_type.UNBOUND_VALUE) {\n");
		source.append(MessageFormat.format("throw new TtcnError(\"Text encoder: Encoding an unbound value of enumerated type {0}.\");\n", name));
		source.append("}\n");
		source.append("text_buf.push_int(enum_value.enum_num);\n");
		source.append("}\n\n");

		source.append("public void decode_text(final Text_Buf text_buf) {\n");
		source.append("final int temp = text_buf.pull_int().getInt();\n");
		source.append("if (!isValidEnum(temp)) {\n");
		source.append(MessageFormat.format("throw new TtcnError(MessageFormat.format(\"Text decoder: Unknown numeric value '{'0'}' was received for enumerated type {0}.\", temp));\n", name));
		source.append("}\n");
		source.append("int2enum(temp);\n");
		source.append("}\n\n");
	}

	private static void generateValueStrToEnum(final StringBuilder source) {
		source.append("public static enum_type str2enum(final String strPar) {\n");
		source.append("enum_type helper;");
		source.append("try {\n");
		source.append("helper = enum_type.valueOf(strPar);\n");
		source.append("} catch(IllegalArgumentException e) {\n");
		source.append("helper = enum_type.UNBOUND_VALUE;\n");
		source.append("}\n");
		source.append("	return helper;\n");
		source.append("}\n\n");
	}

	private static void generateValueAsInt(final StringBuilder source) {
		source.append("//originally int as_int()\n");
		source.append("public int asInt() {\n");
		source.append("return enum2int(enum_value);\n");
		source.append("}\n\n");
	}

	private static void generateValueFromInt(final StringBuilder source) {
		source.append("public void fromInt(final int otherValue) {\n");
		source.append("enum_value = enum_type.getValue(otherValue);\n");
		source.append("}\n\n");
	}

	private static void generateValueIntToEnum(final StringBuilder source) {
		//arg: int
		source.append("public void int2enum(final int intValue) {\n");
		source.append("if (!isValidEnum(intValue)) {\n");
		source.append("throw new TtcnError(\"Assigning invalid numeric value \"+intValue+\" to a variable of enumerated type {}.\");\n");
		source.append("	}\n");
		source.append("enum_value = enum_type.getValue(intValue);\n");
		source.append("}\n\n");

		//arg: TitanInteger
		source.append("public void int2enum(final TitanInteger intValue) {\n");
		source.append("if (!isValidEnum(intValue.getInt())) {\n");
		source.append("throw new TtcnError(\"Assigning invalid numeric value \"+intValue.getInt()+\" to a variable of enumerated type {}.\");\n");
		source.append("	}\n");
		source.append("enum_value = enum_type.getValue(intValue.getInt());\n");
		source.append("}\n\n");
	}

	private static void generateValueEnumToInt(final StringBuilder source, final String name) {
		// arg: enum_type
		source.append(MessageFormat.format("public static int enum2int(final {0}.enum_type enumPar) '{'\n", name));
		source.append("if (enumPar == enum_type.UNBOUND_VALUE || enumPar == enum_type.UNKNOWN_VALUE) {\n");
		source.append("throw new TtcnError(\"The argument of function enum2int() is an \"+ (enumPar==enum_type.UNBOUND_VALUE ? \"unbound\":\"invalid\") +\" value of enumerated type {0}.\");\n");
		source.append("}\n");
		source.append("return enumPar.enum_num;\n");
		source.append("}\n\n");

		// own type
		source.append(MessageFormat.format("public static int enum2int({0} enumPar) '{'\n", name));
		source.append("if (enumPar.enum_value == enum_type.UNBOUND_VALUE || enumPar.enum_value == enum_type.UNKNOWN_VALUE) {\n");
		source.append("throw new TtcnError(\"The argument of function enum2int() is an \"+ (enumPar.enum_value==enum_type.UNBOUND_VALUE ? \"unbound\":\"invalid\") +\" value of enumerated type {0}.\");\n");
		source.append("}\n");
		source.append("return enumPar.enum_value.enum_num;\n");
		source.append("}\n\n");

	}

	private static void generateValueIsPresent(final StringBuilder source) {
		source.append("public boolean isPresent() {\n");
		source.append("return isBound();\n");
		source.append("}\n\n");
	}

	private static void generateValueIsBound(final StringBuilder source){
		source.append("public boolean isBound() {\n");
		source.append("return enum_value != enum_type.UNBOUND_VALUE;\n");
		source.append("}\n\n");
	}

	private static void generateValueIsValue(final StringBuilder source){
		source.append("public boolean isValue() {\n");
		source.append("return enum_value != enum_type.UNBOUND_VALUE;\n");
		source.append("}\n\n");
	}

	private static void generateValueOperatorEquals(final StringBuilder source, final String aName, final String displayName) {
		//Arg type: own type
		source.append("//originally operator==\n");
		source.append(MessageFormat.format("public boolean operatorEquals(final {0} otherValue)'{'\n", aName));
		source.append(MessageFormat.format("return enum_value == otherValue.enum_value;\n", aName));
		source.append("}\n\n");

		//Arg: Base_Type
		source.append("//originally operator==\n");
		source.append("public boolean operatorEquals(final Base_Type otherValue){\n");
		source.append(MessageFormat.format("if (otherValue instanceof {0}) '{'\n", aName));
		source.append(MessageFormat.format("return operatorEquals( ({0}) otherValue);\n", aName));
		source.append("} else {\n");
		source.append(MessageFormat.format("throw new TtcnError(\"Internal Error: value can not be cast to {0}.\");\n", displayName));
		source.append("}\n");
		source.append("}\n\n");

		//Arg: enum_type
		source.append("//originally operator==\n");
		source.append(MessageFormat.format("public boolean operatorEquals(final {0}.enum_type otherValue)'{'\n",aName));
		source.append(MessageFormat.format("return enum_value == otherValue;\n", aName));
		source.append("}\n\n");
	}

	private static void generateValueOperatorNotEquals(final StringBuilder source,final String aName) {
		//Arg type: own type
		source.append("//originally operator!=\n");
		source.append(MessageFormat.format("public boolean operatorNotEquals(final {0} otherValue)'{'\n", aName));
		source.append(MessageFormat.format("return !operatorEquals(otherValue);\n", aName));
		source.append("}\n\n");

		//Arg: Base_Type
		source.append("//originally operator!=\n");
		source.append("public boolean operatorNotEquals(final Base_Type otherValue){\n");
		source.append(MessageFormat.format("return !operatorEquals(otherValue);\n", aName));
		source.append("}\n\n");

		//Arg: enum_type
		source.append("//originally operator!=\n");
		source.append(MessageFormat.format("public boolean operatorNotEquals(final {0}.enum_type otherValue)'{'\n",aName));
		source.append(MessageFormat.format("return !operatorEquals(otherValue);\n", aName));
		source.append("}\n\n");
	}

	private static void generateMustBound(final StringBuilder source ) {
		source.append("public void mustBound(final String errorMessage) {\n");
		source.append("if ( !isBound() ) {\n");
		source.append("throw new TtcnError( errorMessage );\n");
		source.append("}\n");
		source.append("}\n\n");
	}

	private static void generateValueAssign(final StringBuilder source, final String name) {
		//Arg type: own type
		source.append("//originally operator=\n");
		source.append(MessageFormat.format("public {0} assign(final {0} otherValue)'{'\n", name));
		source.append("otherValue.mustBound(\"Assignment of an unbound enumerated value\");\n\n");
		source.append( "if (otherValue != this) {\n");
		source.append(MessageFormat.format("this.enum_value = otherValue.enum_value;\n",  name));
		source.append("}\n\n");
		source.append("return this;\n");
		source.append("}\n\n");

		//Arg: Base_Type
		source.append("@Override\n");
		source.append(MessageFormat.format("public {0} assign(final Base_Type otherValue)'{'\n", name));
		source.append(MessageFormat.format("if( otherValue instanceof {0} ) '{'\n", name));
		source.append(MessageFormat.format("return assign(({0}) otherValue);\n", name));
		source.append("}\n\n");
		source.append(MessageFormat.format("throw new TtcnError(MessageFormat.format(\"Internal Error: value `{0}'' can not be cast to {1}\", otherValue));\n", name));
		source.append("}\n\n");

		//Arg: enum_type
		source.append("//originally operator=\n");
		source.append(MessageFormat.format("public {0} assign(final {0}.enum_type otherValue)'{'\n", name));
		source.append(MessageFormat.format("return assign( new {0}(otherValue) );\n",name));
		source.append("}\n\n");

		//Arg: int
		source.append("//originally operator=\n");
		source.append(MessageFormat.format("public {0} assign(final int otherValue)'{'\n", name));
		source.append("if (!isValidEnum(otherValue)) {\n");
		source.append(MessageFormat.format("throw new TtcnError(MessageFormat.format(\"Assigning unknown numeric value {1} to a variable of enumerated type `{0}''.\", otherValue));\n", name));
		source.append("}\n\n");
		source.append(MessageFormat.format("enum_value =  enum_type.getValue(otherValue);\n", name));
		source.append("return this;\n");
		source.append("}\n\n");
	}

	private static void generateValueIsLessThan(final StringBuilder source, final String name) {
		// arg: enum_type
		source.append("// originally operator<\n");
		source.append(MessageFormat.format("public boolean isLessThan(final {0}.enum_type otherValue)'{'\n", name));
		source.append("if (this.enum_value == enum_type.UNBOUND_VALUE) {\n");
		source.append("throw new TtcnError(\"The left operand of comparison is an unbound value of enumerated type "+ name +". \");\n");
		source.append("}\n");
		source.append("return enum_value.enum_num < otherValue.enum_num;\n");
		source.append("}\n\n");

		//arg: own type
		source.append("// originally operator<\n");
		source.append(MessageFormat.format("public boolean isLessThan(final {0} otherValue)'{'\n", name));
		source.append("if (this.enum_value == enum_type.UNBOUND_VALUE) {\n");
		source.append("throw new TtcnError(\"The left operand of comparison is an unbound value of enumerated type "+ name +". \");\n");
		source.append("}\n");
		source.append("if (otherValue.enum_value == enum_type.UNBOUND_VALUE) {\n");
		source.append("throw new TtcnError(\"The right operand of comparison is an unbound value of enumerated type "+ name +". \");\n");
		source.append("}\n");
		source.append("return  enum_value.enum_num < otherValue.enum_value.enum_num ;\n");
		source.append("}\n\n");
	}

	private static void generateValueIsLessThanOrEqual(final StringBuilder source, final String name) {
		// arg: enum_type
		source.append("// originally operator<=\n");
		source.append(MessageFormat.format("public boolean isLessThanOrEqual(final {0}.enum_type otherValue)'{'\n", name));
		source.append("if (this.enum_value == enum_type.UNBOUND_VALUE) {\n");
		source.append("throw new TtcnError(\"The left operand of comparison is an unbound value of enumerated type "+ name +". \");\n");
		source.append("}\n");
		source.append("return enum_value.enum_num <= otherValue.enum_num;\n");
		source.append("}\n\n");

		// own type
		source.append("// originally operator<=\n");
		source.append(MessageFormat.format("public boolean isLessThanOrEqual(final {0} otherValue)'{'\n", name));
		source.append("if (this.enum_value == enum_type.UNBOUND_VALUE) {\n");
		source.append("throw new TtcnError(\"The left operand of comparison is an unbound value of enumerated type "+ name +". \");\n");
		source.append("}\n");
		source.append("if (otherValue.enum_value == enum_type.UNBOUND_VALUE) {\n");
		source.append("throw new TtcnError(\"The right operand of comparison is an unbound value of enumerated type "+ name +". \");\n");
		source.append("}\n");
		source.append("return  enum_value.enum_num <= otherValue.enum_value.enum_num ;\n");
		source.append("}\n\n");
	}

	private static void generateValueIsGreaterThan(final StringBuilder source, final String name) {
		// arg: enum_type
		source.append("// originally operator>\n");
		source.append(MessageFormat.format("public boolean isGreaterThan(final {0}.enum_type otherValue)'{'\n", name));
		source.append("if (this.enum_value == enum_type.UNBOUND_VALUE) {\n");
		source.append("throw new TtcnError(\"The left operand of comparison is an unbound value of enumerated type "+ name +". \");\n");
		source.append("}\n");
		source.append("return enum_value.enum_num > otherValue.enum_num;\n");
		source.append("}\n\n");

		// own type
		source.append("// originally operator>\n");
		source.append(MessageFormat.format("public boolean isGreaterThan(final {0} otherValue)'{'\n", name));
		source.append("if (this.enum_value == enum_type.UNBOUND_VALUE) {\n");
		source.append("throw new TtcnError(\"The left operand of comparison is an unbound value of enumerated type "+ name +". \");\n");
		source.append("}\n");
		source.append("if (otherValue.enum_value == enum_type.UNBOUND_VALUE) {\n");
		source.append("throw new TtcnError(\"The right operand of comparison is an unbound value of enumerated type "+ name +". \");\n");
		source.append("}\n");
		source.append("return  enum_value.enum_num > otherValue.enum_value.enum_num ;\n");
		source.append("}\n\n");
	}

	private static void generateValueIsGreaterThanOrEqual(final StringBuilder source, final String name) {
		// arg: enum_type
		source.append("// originally operator>=\n");
		source.append(MessageFormat.format("public boolean isGreaterThanOrEqual(final {0}.enum_type otherValue)'{'\n", name));
		source.append("if (this.enum_value == enum_type.UNBOUND_VALUE) {\n");
		source.append("throw new TtcnError(\"The left operand of comparison is an unbound value of enumerated type "+ name +". \");\n");
		source.append("}\n");
		source.append("return enum_value.enum_num >= otherValue.enum_num;\n");
		source.append("}\n\n");

		// arg: own type
		source.append("// originally operator>=\n");
		source.append(MessageFormat.format("public boolean isGreaterThanOrEqual(final {0} otherValue)'{'\n", name));
		source.append("if (this.enum_value == enum_type.UNBOUND_VALUE) {\n");
		source.append("throw new TtcnError(\"The left operand of comparison is an unbound value of enumerated type "+ name +". \");\n");
		source.append("}\n");
		source.append("if (otherValue.enum_value == enum_type.UNBOUND_VALUE) {\n");
		source.append("throw new TtcnError(\"The right operand of comparison is an unbound value of enumerated type "+ name +". \");\n");
		source.append("}\n");
		source.append("return  enum_value.enum_num >= otherValue.enum_value.enum_num ;\n");
		source.append("}\n\n");
	}

	private static void generateValueCleanUp(final StringBuilder source) {
		source.append("public void cleanUp() {\n");
		source.append("enum_value = enum_type.UNBOUND_VALUE;\n");
		source.append("}\n\n");
	}

	private static void generateTemplateDeclaration(final StringBuilder source, final String name) {
		source.append("// single_value\n");
		source.append(MessageFormat.format("private {0}.enum_type single_value;\n",name));
		source.append("// value_list part\n");
		source.append(MessageFormat.format("private ArrayList<{0}_template> value_list;\n\n", name));
	}

	private static void generateTemplateConstructors( final StringBuilder source, final String name){
		// empty
		source.append(MessageFormat.format("public {0}_template() '{'\n", name));
		source.append("}\n\n");

		// template_sel
		source.append(MessageFormat.format("public {0}_template(final template_sel otherValue) '{'\n", name));
		source.append("super(otherValue);\n");
		source.append("checkSingleSelection(otherValue);\n");
		source.append("}\n\n");

		// int
		source.append(MessageFormat.format("public {0}_template(final int otherValue) '{'\n", name));
		source.append("super(template_sel.SPECIFIC_VALUE);\n");
		source.append(MessageFormat.format("if (!{0}.isValidEnum(otherValue)) '{'\n", name));
		source.append(MessageFormat.format("throw new TtcnError(\"Initializing a template of enumerated type {0} with unknown numeric value \"+ otherValue +\".\");\n", name));
		source.append("}\n");
		source.append(MessageFormat.format("single_value = {0}.enum_type.getValue(otherValue);\n", name));
		source.append("}\n\n");

		// name type
		source.append(MessageFormat.format("public {0}_template(final {0} otherValue) '{'\n", name));
		source.append("super(template_sel.SPECIFIC_VALUE);\n");
		source.append(MessageFormat.format("if (otherValue.enum_value == {0}.enum_type.UNBOUND_VALUE) '{'\n", name));
		source.append("throw new TtcnError(\"Creating a template from an unbound value of enumerated type "+ name +". \");\n");
		source.append("}\n");
		source.append("single_value = otherValue.enum_value;\n");
		source.append("}\n\n");

		// own type
		source.append(MessageFormat.format("public {0}_template(final {0}_template otherValue) '{'\n", name));
		source.append("copy_template(otherValue);\n");
		source.append("}\n\n");

		// name.enum_type
		source.append(MessageFormat.format("public {0}_template(final {0}.enum_type otherValue) '{'\n", name));
		source.append("super(template_sel.SPECIFIC_VALUE);\n");
		source.append("single_value = otherValue;\n");
		source.append("}\n\n");

		//FIXME implement optional parameter version
	}

	private static void generatetemplateCopyTemplate(final StringBuilder source, final String name) {
		source.append(MessageFormat.format("private void copy_template(final {0}_template otherValue) '{'\n", name));
		source.append("setSelection(otherValue);");
		source.append("switch (otherValue.templateSelection) {\n");
		source.append("case SPECIFIC_VALUE:\n");
		source.append("single_value = otherValue.single_value;\n");
		source.append("break;\n");
		source.append("case OMIT_VALUE:\n");
		source.append("case ANY_VALUE:\n");
		source.append("case ANY_OR_OMIT:\n");
		source.append("break;\n");
		source.append("case VALUE_LIST:\n");
		source.append("case COMPLEMENTED_LIST:\n");
		source.append(MessageFormat.format("value_list = new ArrayList<{0}_template>(otherValue.value_list.size());\n", name));
		source.append("for(int i = 0; i < otherValue.value_list.size(); i++) {\n");
		source.append(MessageFormat.format("final {0}_template temp = new {0}_template(otherValue.value_list.get(i));\n", name));
		source.append("value_list.add(temp);\n");
		source.append("}\n");
		source.append("break;\n");
		source.append("default:\n");
		source.append(MessageFormat.format("throw new TtcnError(\"Copying an uninitialized/unsupported template of enumerated type {0}.\");\n", name));
		source.append("}\n");
		source.append("}\n\n");
	}

	private static void generateTemplateAssign(final StringBuilder source, final String name) {
		// arg: template_sel
		source.append("//originally operator=\n");
		source.append(MessageFormat.format("public {0}_template assign(final template_sel otherValue) '{'\n", name));
		source.append("checkSingleSelection(otherValue);\n");
		source.append("cleanUp();\n");
		source.append("setSelection(otherValue);\n");
		source.append("return this;\n");
		source.append("}\n\n");

		// arg: int
		source.append("//originally operator=\n");
		source.append(MessageFormat.format("public {0}_template assign(final int otherValue) '{'\n", name));
		source.append(MessageFormat.format("if (!{0}.isValidEnum(otherValue)) '{'\n", name));
		source.append(MessageFormat.format("throw new TtcnError(\"Assigning unknown numeric value \" + otherValue + \" to a template of enumerated type {0}.\");\n", name));
		source.append("}\n");
		source.append("cleanUp();\n");
		source.append("setSelection(template_sel.SPECIFIC_VALUE);\n");
		source.append("return this;\n");
		source.append("}\n\n");

		// arg: name.enum_type
		source.append("//originally operator=\n");
		source.append(MessageFormat.format("public {0}_template assign(final {0}.enum_type otherValue)'{'\n", name));
		source.append("cleanUp();\n");
		source.append("setSelection(template_sel.SPECIFIC_VALUE);\n");
		source.append("single_value = otherValue;\n");
		source.append("return this;\n");
		source.append("}\n\n");

		// arg : own type
		source.append("//originally operator=\n");
		source.append(MessageFormat.format("public {0}_template assign(final {0}_template otherValue)'{'\n", name));
		source.append("// otherValue.mustBound(\"Assignment of an unbound enumerated value\");\n\n");
		source.append( "if (otherValue != this) {\n");
		source.append("cleanUp();\n");
		source.append("copy_template(otherValue);\n");
		source.append("}\n");
		source.append("return this;\n");
		source.append("}\n\n");

		// arg: name type
		source.append("//originally operator=\n");
		source.append(MessageFormat.format("public {0}_template assign(final {0} otherValue)'{'\n", name));
		source.append(MessageFormat.format("if (otherValue.enum_value == {0}.enum_type.UNBOUND_VALUE) '{'\n", name));
		source.append("throw new TtcnError(\"Assignment of an unbound value of enumerated type "+ name +" to a template. \");\n");
		source.append("}\n");
		source.append("cleanUp();\n");
		source.append("setSelection(template_sel.SPECIFIC_VALUE);\n");
		source.append("single_value = otherValue.enum_value;\n");
		source.append("return this;\n");
		source.append("}\n\n");

		//Arg: Base_Type
		source.append("@Override\n");
		source.append(MessageFormat.format("public {0}_template assign(final Base_Type otherValue)'{'\n", name));
		source.append(MessageFormat.format("if( otherValue instanceof {0} ) '{'\n", name));
		source.append(MessageFormat.format("return assign(({0}) otherValue);\n", name));
		source.append("}\n\n");
		source.append(MessageFormat.format("throw new TtcnError(MessageFormat.format(\"Internal Error: value `{0}'' can not be cast to {1}\", otherValue));\n", name));
		source.append("}\n\n");

		//Arg: Base_Template
		source.append("@Override\n");
		source.append(MessageFormat.format("public {0}_template assign(final Base_Template otherValue)'{'\n", name));
		source.append(MessageFormat.format("if( otherValue instanceof {0}_template ) '{'\n", name));
		source.append(MessageFormat.format("return assign(({0}_template) otherValue);\n", name));
		source.append("}\n\n");
		source.append(MessageFormat.format("throw new TtcnError(MessageFormat.format(\"Internal Error: value `{0}''_template can not be cast to {1}\", otherValue));\n", name));
		source.append("}\n\n");
		/*@Override
		public myenum1_template assign(Base_Type otherValue) {
			if( otherValue instanceof myenum1 ) {
				return assign((myenum1) otherValue);
			}

			throw new TtcnError(MessageFormat.format("Internal Error: value `myenum1' can not be cast to {1}", otherValue));
		}

		@Override
		public myenum1_template assign(Base_Template otherValue) {
			if( otherValue instanceof myenum1_template ) {
				return assign((myenum1_template) otherValue);
			}

			throw new TtcnError(MessageFormat.format("Internal Error: value `myenum1' can not be cast to {1}", otherValue));
		}*/
		//FIXME implement optional parameter version
	}

	private static void generateTemplateSetType(final StringBuilder source, final String name){
		source.append("public void setType(final template_sel templateType, final int list_length) {\n");
		source.append("if (templateType != template_sel.VALUE_LIST && templateType != template_sel.COMPLEMENTED_LIST) {\n");
		source.append(MessageFormat.format("throw new TtcnError(\"Setting an invalid list type for a template of enumerated type {0}.\");\n", name));
		source.append("}\n");
		source.append("cleanUp();\n");
		source.append("setSelection(templateType);\n");
		source.append(MessageFormat.format("value_list = new ArrayList<{0}_template>();\n", name));
		source.append("for(int i = 0 ; i < list_length; i++) {\n");
		source.append(MessageFormat.format("value_list.add(new {0}_template());\n", name));
		source.append("}\n");
		source.append("}\n\n");
	}

	private static void generateTemplateIsBound(final StringBuilder source) {
		source.append("public boolean isBound() {\n");
		source.append("if (templateSelection != template_sel.UNINITIALIZED_TEMPLATE && !is_ifPresent) {\n");
		source.append("return false;\n");
		source.append("}\n");
		source.append("return true;\n");
		source.append("}\n\n");
	}

	private static void generateTemplateIsValue(final StringBuilder source, final String name) {
		source.append("public boolean isValue() {\n");
		source.append("if (templateSelection != template_sel.SPECIFIC_VALUE || is_ifPresent) {\n");
		source.append("return false;\n");
		source.append("}\n");
		source.append(MessageFormat.format("return single_value != {0}.enum_type.UNBOUND_VALUE;\n", name));
		source.append("}\n\n");
	}

	private static void generateTemplateCleanUp(final StringBuilder source) {
		source.append("public void cleanUp() {\n");
		source.append("if (templateSelection == template_sel.VALUE_LIST || templateSelection == template_sel.COMPLEMENTED_LIST) {\n");
		source.append("value_list.clear();\n");
		source.append("value_list = null;\n");
		source.append("}\n");
		source.append("if (templateSelection == template_sel.SPECIFIC_VALUE) {\n");
		source.append("single_value = null;\n");
		source.append("}\n");
		source.append("templateSelection = template_sel.UNINITIALIZED_TEMPLATE;\n");
		source.append("}\n\n");
	}

	private static void generateTemplateMatch(final StringBuilder source, final String name) {
		// name.enum_type
		source.append("// originally match\n");
		source.append(MessageFormat.format("public boolean match(final {0}.enum_type otherValue) '{'\n", name));
		source.append("return match(otherValue, false);\n");
		source.append("}\n\n");

		source.append("// originally match\n");
		source.append(MessageFormat.format("public boolean match(final {0}.enum_type otherValue, final boolean legacy) '{'\n", name));
		source.append("switch (templateSelection) {\n");
		source.append("case SPECIFIC_VALUE:\n");
		source.append("return single_value == otherValue;\n");
		source.append("case OMIT_VALUE:\n");
		source.append("return false;\n");
		source.append("case ANY_VALUE:\n");
		source.append("case ANY_OR_OMIT:\n");
		source.append("return true;\n");
		source.append("case VALUE_LIST:\n");
		source.append("case COMPLEMENTED_LIST:\n");
		source.append("for(int i = 0 ; i < value_list.size(); i++) {\n");
		source.append("if(value_list.get(i).match(otherValue)) {\n");
		source.append("return templateSelection == template_sel.VALUE_LIST;\n");
		source.append("}\n");
		source.append("}\n");
		source.append("return templateSelection == template_sel.COMPLEMENTED_LIST;\n");
		source.append("default:\n");
		source.append("throw new TtcnError(\"Matching with an uninitialized/unsupported template of enumerated type "+ name +".\");\n");
		source.append("}\n");
		source.append("}\n\n");

		// name type
		source.append("// originally match\n");
		source.append(MessageFormat.format("public boolean match(final {0} otherValue) '{'\n", name));
		source.append("return match(otherValue.enum_value, false);\n");
		source.append("}\n\n");

		source.append("// originally match\n");
		source.append(MessageFormat.format("public boolean match(final {0} otherValue, final boolean legacy) '{'\n", name));
		source.append("return match(otherValue.enum_value, false);\n");
		source.append("}\n\n");

		source.append("@Override\n");
		source.append(MessageFormat.format("public boolean match(final Base_Type otherValue, final boolean legacy)'{'\n", name));
		source.append(MessageFormat.format("if( otherValue instanceof {0} ) '{'\n", name));
		source.append(MessageFormat.format("return match(({0}) otherValue, legacy);\n", name));
		source.append("}\n\n");
		source.append(MessageFormat.format("throw new TtcnError(MessageFormat.format(\"Internal Error: value `{0}'' can not be cast to {1}\", otherValue));\n", name));
		source.append("}\n\n");
	}

	private static void generateTemplateValueOf(final StringBuilder source, final String name) {
		source.append(MessageFormat.format("public {0} valueOf() '{'\n", name));
		source.append("if (templateSelection != template_sel.SPECIFIC_VALUE || is_ifPresent) {\n");
		source.append(MessageFormat.format("throw new TtcnError(\"Performing a valueof or send operation on a non-specific template of enumerated type {0}.\");\n", name));
		source.append("}\n");
		source.append(MessageFormat.format("return new {0}(single_value);\n", name));
		source.append("}\n\n");
	}

	private static void generateTemplateListItem(final StringBuilder source, final String name) {
		source.append(MessageFormat.format("public {0}_template listItem(final int list_index) '{'\n", name));
		source.append("if (templateSelection != template_sel.VALUE_LIST && templateSelection != template_sel.COMPLEMENTED_LIST) {\n");
		source.append(MessageFormat.format("throw new TtcnError(\"Accessing a list element of a non-list template of enumerated type {0}.\");\n", name));
		source.append("}\n");

		source.append("if (list_index < 0) {\n");
		source.append(MessageFormat.format("throw new TtcnError(\"Index underflow in a value list template of enumerated type {0}.\");\n", name));
		source.append("}\n");
		source.append("if(list_index >= value_list.size()) {\n");
		source.append(MessageFormat.format("throw new TtcnError(\"Index overflow in a value list template of enumerated type {0}.\");\n", name));
		source.append("}\n");
		source.append("return value_list.get(list_index);\n");
		source.append("}\n\n");
	}

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
		source.append("if (value_list.get(i).match_omit()) {\n");
		source.append("return templateSelection == template_sel.VALUE_LIST;\n");
		source.append("}\n");
		source.append("}\n");
		source.append("return templateSelection == template_sel.COMPLEMENTED_LIST;\n");
		source.append("}\n");
		source.append("default:\n");
		source.append("return false;\n");
		source.append("}\n");
		source.append("}\n\n");
	}

	private static void generateTemplateLog(final StringBuilder source, final String name) {
		source.append("public void log() {\n");
		source.append("switch (templateSelection) {\n");
		source.append("case SPECIFIC_VALUE:\n");
		source.append(MessageFormat.format("TtcnLogger.log_event_enum({0}.enum2str(single_value), {0}.enum2int(single_value));\n", name));
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
		source.append("}\n");
	}

	private static void generateTemplateLogMatch(final StringBuilder source, final String name, final String displayName ){
		source.append("@Override\n");
		source.append("public void log_match(final Base_Type match_value, final boolean legacy) {\n");
		source.append(MessageFormat.format("if (match_value instanceof {0}) '{'\n", name));
		source.append(MessageFormat.format("log_match(({0})match_value, legacy);\n", name));
		source.append("return;\n");
		source.append("}\n\n");
		source.append(MessageFormat.format("\t\tthrow new TtcnError(\"Internal Error: value can not be cast to {0}.\");\n", displayName));
		source.append("}\n");

		source.append(MessageFormat.format("public void log_match(final {0} match_value, final boolean legacy)'{'\n",name));
		source.append("match_value.log();\n");
		source.append("TtcnLogger.log_event_str(\" with \");\n");
		source.append("log();\n");
		source.append("if (match(match_value, legacy)) {\n");
		source.append("TtcnLogger.log_event_str(\" matched\");\n");
		source.append("} else {\n");
		source.append("TtcnLogger.log_event_str(\" unmatched\");\n");
		source.append("}\n");
		source.append("}\n");
	}

	private static void generateTemplateEncodeDecodeText(final StringBuilder source, final String name, final String displayName) {
		source.append("@Override\n");
		source.append("public void encode_text(final Text_Buf text_buf) {\n");
		source.append("encode_text_base(text_buf);\n");
		source.append("switch (templateSelection) {\n");
		source.append("case OMIT_VALUE:\n");
		source.append("case ANY_VALUE:\n");
		source.append("case ANY_OR_OMIT:\n");
		source.append("break;\n");
		source.append("case SPECIFIC_VALUE:\n");
		source.append("text_buf.push_int(single_value.getInt());\n");
		source.append("break;\n");
		source.append("case VALUE_LIST:\n");
		source.append("case COMPLEMENTED_LIST:\n");
		source.append("text_buf.push_int(value_list.size());\n");
		source.append("for (int i = 0; i < value_list.size(); i++) {\n");
		source.append("value_list.get(i).encode_text(text_buf);\n");
		source.append("}\n");
		source.append("break;\n");
		source.append("default:\n");
		source.append(MessageFormat.format("throw new TtcnError(\"Text encoder: Encoding an uninitialized/unsupported template of enumerated type {0}.\");\n", displayName));
		source.append("}\n");
		source.append("}\n");

		source.append("@Override\n");
		source.append("public void decode_text(final Text_Buf text_buf) {\n");
		source.append("cleanUp();\n");
		source.append("decode_text_base(text_buf);\n");
		source.append("switch (templateSelection) {\n");
		source.append("case OMIT_VALUE:\n");
		source.append("case ANY_VALUE:\n");
		source.append("case ANY_OR_OMIT:\n");
		source.append("break;\n");
		source.append("case SPECIFIC_VALUE:{\n");
		source.append("final int temp = text_buf.pull_int().getInt();\n");
		source.append(MessageFormat.format("if (!{0}.isValidEnum(temp)) '{'\n", name));
		source.append(MessageFormat.format("throw new TtcnError(MessageFormat.format(\"Text decoder: Unknown numeric value '{'0'}' was received for enumerated type {0}.\", temp));\n", displayName));
		source.append("}\n");
		source.append(MessageFormat.format("single_value = {0}.enum_type.values()[temp];\n", name));
		source.append("break;\n");
		source.append("}\n");
		source.append("case VALUE_LIST:\n");
		source.append("case COMPLEMENTED_LIST:\n");
		source.append(MessageFormat.format("value_list = new ArrayList<{0}_template>(text_buf.pull_int().getInt());\n", name));
		source.append("for(int i = 0; i < value_list.size(); i++) {\n");
		source.append(MessageFormat.format("final {0}_template temp = new {0}_template();\n", name));
		source.append("temp.decode_text(text_buf);\n");
		source.append("value_list.add(temp);\n");
		source.append("}\n");
		source.append("break;\n");
		source.append("default:\n");
		source.append(MessageFormat.format("throw new TtcnError(\"Text decoder: An unknown/unsupported selection was received for a template of enumerated type {0}.\");\n", displayName));
		source.append("}\n");
		source.append("}\n");
	}
}
