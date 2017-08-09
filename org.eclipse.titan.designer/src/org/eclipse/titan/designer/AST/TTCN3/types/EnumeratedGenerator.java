/******************************************************************************
 * Copyright (c) 2000-2017 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.types;

import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.titan.designer.AST.TTCN3.values.Integer_Value;
import org.eclipse.titan.designer.compiler.JavaGenData;

/**
 * @author Farkas Izabella Ingrid
 * */
public class EnumeratedGenerator {
	
	private static final String UNKNOWN_VALUE = "UNKNOWN_VALUE";
	private static final String UNBOUND_VALUE ="UNBOUND_VALUE";
	
	public static class Enum_Defs {
		private EnumerationItems items;
		private String name;
		private String displayName;
		private String templateName;
		private Long firstUnused = -1L;  //first unused value for thsi enum type
		private Long secondUnused = -1L; //second unused value for thsi enum type
		
		public Enum_Defs(final EnumerationItems aItems, final String aName, final String aDisplayName, final String aTemplateName){
			items = aItems;
			name = aName;
			displayName = aDisplayName;
			templateName = aTemplateName;
			calculateFirstAndSecondUnusedValues();
		}
		
		//This function supposes that the enum class is already checked and error free
		private void calculateFirstAndSecondUnusedValues() {
			if( firstUnused != -1 ) {
				return; //function already have been called
			}
			final Map<Long, EnumItem> valueMap = new HashMap<Long, EnumItem>(items.getItems().size());
			final List<EnumItem> enumItems = items.getItems();
			for( int i = 0, size = enumItems.size(); i < size; i++) {
				final EnumItem item = enumItems.get(i);
				valueMap.put( ((Integer_Value) item.getValue()).getValue(), item);
			}

			Long firstUnused = Long.valueOf(0);
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
	
	public static void generateValueClass(final JavaGenData aData, final StringBuilder source, final Enum_Defs e_defs ) {
//		if(needsAlias()) { ???
		
			source.append(MessageFormat.format("public static class {0} extends Base_Type '{' \n", e_defs.name));
			//== enum_type ==
			source.append("public enum enum_type {\n");
			
			DecimalFormat formatter = new DecimalFormat("#");
			int size = e_defs.items.getItems().size();
			EnumItem item = null;
			for ( int i=0; i<size; i++) {
				item = e_defs.items.getItems().get(i);
				source.append(MessageFormat.format("{0}", item.getId().getTtcnName()));
				if (item.getValue() instanceof Integer_Value) {
					String valueWithoutCommas = formatter.format( ((Integer_Value) item.getValue()).getValue());
					source.append(MessageFormat.format("({0}),\n", valueWithoutCommas));
				} else {
					//TODO: impossible ?
					//throw new TtcnError("Invalid item value");
				}
			}
			
			//e_defs.calculateFirstAndSecondUnusedValues();
			source.append(MessageFormat.format("{0}({1}),\n", UNKNOWN_VALUE, e_defs.firstUnused));
			source.append(MessageFormat.format("{0}({1});\n", UNBOUND_VALUE, e_defs.secondUnused));
			source.append("\n private int enum_num;\n");
			//== constructors for enum_type ==
			
			source.append("enum_type(int num) {\n");
			source.append("this.enum_num = num;\n");
			source.append("}\n");
			
			source.append("private int getInt() {\n");
			source.append("return enum_num;\n");
			source.append("}\n");
			
			source.append("}\n\n");
			// end of enum_type
			
			//== enum_value ==
			source.append("public enum_type enum_value;\n");
			
			source.append("//===Constructors===;\n");
			generateValueConstructors(source,e_defs.name);

			//== functions ==
			source.append("//===Methods===;\n");
			generateValueAssign(source, e_defs.name); 
			generateValueOperatorEquals(source, e_defs.name);
			generateValueOperatorNotEquals(source, e_defs.name);
			generateValueIsLessThan(source, e_defs.name);
			generateValueIsLessThanOrEqual(source, e_defs.name);
			generateValueIsGreaterThan(source, e_defs.name);
			generateValueIsGreaterThanOrEqual(source, e_defs.name);
			//TODO: enum2int
			//TODO: static const char *enum_to_str(enum_type enum_par);
//					static enum_type str_to_enum(const char *str_par);
//					static boolean is_valid_enum(int int_par);
			//TODO: int as_int() const { return enum2int(enum_value); }
//					void from_int(int p_val) { *this = p_val; }
//					void int2enum(int int_val);
//					operator enum_type() const;
			generateValueIsPresent(source);
			generateValueIsBound(source);
			generateMustBound(source);
			generateValueIsValue(source);
			generateValueCleanUp(source);
			generateValueIsValidEnum(source, e_defs.name);
			source.append("\t}\n");
//		}
	}
	
	//TODO: log() const;
	//TODO: set_param(Module_Param& param);
	//TODO: encode_text(Text_Buf& text_buf) const;
	//TODO: decode_text(Text_Buf& text_buf);
	
	public static void generateTemplateClass(final JavaGenData aData, final StringBuilder source, final Enum_Defs e_defs){
		source.append(MessageFormat.format("\tpublic static class {0}_template extends Base_Template '{'\n", e_defs.name, e_defs.templateName));
		
		//TODO: generate this, and others:
		//generateTemplateAssign(source,e_defs.name);
		generateTemplateSetType(source);
		
		source.append("\t}\n");
	}
	
	//===

	private static void generateValueConstructors(final StringBuilder source,final String name) {
		//empty
		source.append(MessageFormat.format("public {0}() '{'\n", name));
		source.append(MessageFormat.format("enum_value = enum_type.{0};\n", UNBOUND_VALUE));
		source.append("}\n\n");

		// own type
		source.append(MessageFormat.format("public {0}({0} other_value) '{'\n", name));
		source.append(MessageFormat.format("enum_value = other_value.enum_value;\n", name));
		source.append("}\n\n");

		// enum_type
		source.append(MessageFormat.format("public {0}({0}.enum_type other_value ) '{'\n", name));
		source.append("enum_value = other_value;\n");
		source.append("}\n\n");
		
		//TODO: arg int
		source.append(MessageFormat.format("public {0}(int otherValue) '{'\n", name));
		source.append("if (!isValidEnum(otherValue)) {\n");
		source.append(MessageFormat.format("throw new TtcnError(MessageFormat.format(\"Initializing a variable of enumerated type `{0}'' with invalid numeric value {1} .\", otherValue));\n", name));
		source.append("}\n\n");
		source.append(MessageFormat.format("enum_value.enum_num =  otherValue;\n", name));
		source.append("}\n\n");
	}
	
	private static void generateValueIsValidEnum(final StringBuilder source,final String name) {
		source.append("public boolean isValidEnum(int otherValue) {\n");
		source.append("return otherValue < enum_type.UNKNOWN_VALUE.getInt();\n");
		source.append("}\n\n");
	}
	
	private static void generateValueIsPresent(final StringBuilder source) {
		source.append("public boolean isPresent() {\n");
		source.append("return isBound();\n");
		source.append("}\n\n");
	}

	private static void generateValueIsBound(final StringBuilder source){
		source.append("public boolean isBound() {\n");
		source.append("return (enum_value != enum_type.UNBOUND_VALUE);\n");
		source.append("}\n\n");
	}

	private static void generateValueIsValue(final StringBuilder source){
		source.append("public boolean isValue() {\n");
		source.append("return (enum_value != enum_type.UNBOUND_VALUE);\n");
		source.append("}\n\n");
	}

	private static void generateValueOperatorEquals(final StringBuilder source,final String aName) {
		//Arg type: own type
		source.append(MessageFormat.format("public TitanBoolean operatorEquals(final {0} otherValue)'{'\n", aName));
		source.append(MessageFormat.format("return (new TitanBoolean( enum_value == otherValue.enum_value));\n", aName)); 
		source.append("}\n\n");
		
		//Arg: Base_Type
		source.append("public TitanBoolean operatorEquals(final Base_Type otherValue){\n");
		source.append(MessageFormat.format("if (otherValue instanceof {0}) '{'\n", aName));
		source.append(MessageFormat.format("return this.operatorEquals( ({0}) otherValue);\n", aName)); 
		source.append("} else {\n");
		source.append("//TODO:TtcnError message\n");
		source.append("return new TitanBoolean(false);\n");
		source.append("}\n");
		source.append("}\n\n");
		
		//Arg: enum_type
		source.append(MessageFormat.format("public TitanBoolean operatorEquals(final {0}.enum_type otherValue)'{'\n",aName));
		source.append(MessageFormat.format("return (new TitanBoolean( enum_value == otherValue));\n", aName)); 
		source.append("}\n\n");
	}
	
	private static void generateValueOperatorNotEquals(final StringBuilder source,final String aName) {
		//Arg type: own type
		source.append(MessageFormat.format("public TitanBoolean operatorNotEquals(final {0} otherValue)'{'\n", aName));
		source.append(MessageFormat.format("return operatorEquals(otherValue).not();\n", aName)); 
		source.append("}\n\n");
		
		//Arg: Base_Type
		source.append("public TitanBoolean operatorNotEquals(final Base_Type otherValue){\n");
		source.append(MessageFormat.format("return operatorEquals(otherValue).not();\n", aName)); 
		source.append("}\n\n");
		
		//Arg: enum_type
		source.append(MessageFormat.format("public TitanBoolean operatorNotEquals(final {0}.enum_type otherValue)'{'\n",aName));
		source.append(MessageFormat.format("return operatorEquals(otherValue).not();\n", aName)); 
		source.append("}\n\n");
	}

	private static void generateMustBound(final StringBuilder source ) {
		source.append("public void mustBound( String errorMessage) {\n");
		source.append("if ( !isBound() ) {\n");
		source.append("throw new TtcnError( errorMessage );\n");
		source.append("}\n");
		source.append("}\n\n");		
	}
	
	private static void generateValueAssign(final StringBuilder source, final String name) {
		//Arg type: own type
		source.append(MessageFormat.format("public {0} assign(final {0} otherValue)'{'\n", name));
		source.append("otherValue.mustBound(\"Assignment of an unbound enumerated value\");\n\n");
		source.append( "if (otherValue != this) {\n");
		source.append(MessageFormat.format("this.enum_value = otherValue.enum_value;\n",  name));
		source.append("}\n\n");
		source.append("return this;\n");
		source.append("}\n\n");
		
		//Arg: Base_Type
		source.append(MessageFormat.format("public {0} assign(final Base_Type otherValue)'{'\n", name));
		source.append(MessageFormat.format("if( otherValue instanceof {0} ) '{'\n", name));
		source.append(MessageFormat.format("return assign(({0}) otherValue);\n", name));
		source.append("}\n\n");
		source.append(MessageFormat.format("throw new TtcnError(MessageFormat.format(\"Internal Error: value `{0}'' can not be cast to {1}\", otherValue));\n", name));
		source.append("}\n\n");
		//Arg: enum_type
		source.append(MessageFormat.format("public {0} assign(final {0}.enum_type otherValue)'{'\n", name));
		source.append(MessageFormat.format("return assign( new {0}(otherValue) );\n",name));
		source.append("}\n\n");
	}
	 
	private static void generateValueIsLessThan(final StringBuilder source, final String name) {
		// originally operator<(enum_type other_value) const
		source.append(MessageFormat.format("public boolean isLessThan(final {0}.enum_type otherValue)'{'\n", name));
		source.append("if (this.enum_value == enum_type.UNBOUND_VALUE) {\n");
		source.append("throw new TtcnError(\"The left operand of comparison is an unbound value of enumerated type "+ name +". \");\n");
		source.append("}\n");
		source.append("return enum_value.enum_num < otherValue.enum_num;\n");
		source.append("}\n\n");

		// originally operator<(const myenum1& other_value) const
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
		// originally operator<=(enum_type other_value) const
		source.append(MessageFormat.format("public boolean isLessThanOrEqual(final {0}.enum_type otherValue)'{'\n", name));
		source.append("if (this.enum_value == enum_type.UNBOUND_VALUE) {\n");
		source.append("throw new TtcnError(\"The left operand of comparison is an unbound value of enumerated type "+ name +". \");\n");
		source.append("}\n");
		source.append("return enum_value.enum_num <= otherValue.enum_num;\n");
		source.append("}\n\n");

		// originally operator<=(const myenum1& other_value) const
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
		// originally operator>(enum_type other_value) const
		source.append(MessageFormat.format("public boolean isGreaterThan(final {0}.enum_type otherValue)'{'\n", name));
		source.append("if (this.enum_value == enum_type.UNBOUND_VALUE) {\n");
		source.append("throw new TtcnError(\"The left operand of comparison is an unbound value of enumerated type "+ name +". \");\n");
		source.append("}\n");
		source.append("return enum_value.enum_num > otherValue.enum_num;\n");
		source.append("}\n\n");

		// originally operator>(const myenum1& other_value) const
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
		// originally operator>=(enum_type other_value) const
		source.append(MessageFormat.format("public boolean isGreaterThanOrEqual(final {0}.enum_type otherValue)'{'\n", name));
		source.append("if (this.enum_value == enum_type.UNBOUND_VALUE) {\n");
		source.append("throw new TtcnError(\"The left operand of comparison is an unbound value of enumerated type "+ name +". \");\n");
		source.append("}\n");
		source.append("return enum_value.enum_num >= otherValue.enum_num;\n");
		source.append("}\n\n");

		// originally operator>=(const myenum1& other_value) const
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
	
	//TODO: enum_template
	
	private static void generateTemplateSetType(final StringBuilder source){
		source.append("\t\tpublic void setType(template_sel valueList, int i) {\n");
		source.append("\t\t\t//TODO: setType is not implemented yet\n");
		source.append("\t\t}\n");
	}
	
	private static void generateTemplateAssign(final StringBuilder source, final String name) {
		//Arg type: own type
		source.append(MessageFormat.format("public {0}_template assign(final {0}_template other_value)'{'\n", name));
		source.append("other_value.mustBound(\"Assignment of an unbound enumerated value\");\n\n");
		source.append( "if (other_value != this) {\n");
		source.append(MessageFormat.format("this.enum_value = other_value.enum_value;\n",  name));
		source.append("}\n\n");
		source.append("return this;\n");
		source.append("}\n\n");
		
		//Arg: Base_Type
		source.append(MessageFormat.format("public {0}_template assign(final Base_Type other_value) '{' \n", name));
		source.append(MessageFormat.format("if( other_value instanceof {0}_template ) '{'\n", name));
		source.append(MessageFormat.format("return assign(({0}_template) other_value);\n", name));
		source.append("}\n\n");
		source.append(MessageFormat.format("throw new TtcnError(MessageFormat.format(\"Internal Error: value `{0}'' can not be cast to {1}\", other_value));\n", name));
		source.append("}\n\n");
		//Arg: enum_type
		source.append(MessageFormat.format("public {0}_template assign(final {0}_template.enum_type other_value)'{'\n", name));
		source.append(MessageFormat.format("return assign( new {0}_template(other_value) );\n",name));
		source.append("}\n\n");
	}	

}
