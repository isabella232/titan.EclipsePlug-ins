package org.eclipse.titan.designer.AST.TTCN3.types;

import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.titan.designer.AST.FieldSubReference;
import org.eclipse.titan.designer.AST.TTCN3.values.Integer_Value;
import org.eclipse.titan.designer.compiler.JavaGenData;

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
			source.append(MessageFormat.format("\tpublic static class {0} extends Base_Type '{' \n", e_defs.name));
			//== enum_type ==
			source.append("\t\tpublic enum enum_type {\n");
			
			DecimalFormat formatter = new DecimalFormat("#");
			int size = e_defs.items.getItems().size();
			EnumItem item = null;
			for( int i=0; i<size; i++){
				item = e_defs.items.getItems().get(i);
				source.append(MessageFormat.format("\t\t\t{0}", item.getId().getTtcnName()));
				if(item.getValue() instanceof Integer_Value) {
					String valueWithoutCommas = formatter.format( ((Integer_Value) item.getValue()).getValue());
					source.append(MessageFormat.format("({0}),\n", valueWithoutCommas));
				} else {
					//TODO: impossible ?
				}
			};
//			calculateFirstAndSecondUnusedValues();
			source.append(MessageFormat.format("\t\t\t{0}({1}),\n", UNKNOWN_VALUE, e_defs.firstUnused));
			source.append(MessageFormat.format("\t\t\t{0}({1});\n", UNBOUND_VALUE, e_defs.secondUnused));
			source.append("\n\t\t\tprivate int enum_num;\n");
			//== constructors for enum_type ==
			
			source.append("\t\t\tenum_type(int num) {\n");
			source.append("\t\t\t\tthis.enum_num = num;\n");
			source.append("\t\t\t}\n");
			
			source.append("\t\t\tprivate int getInt(){\n");
			source.append("\t\t\t\treturn enum_num;\n");
			source.append("\t\t\t}\n");
			
			source.append("\t\t}\n");
			// end of enum_type
			
			//== enum_value ==
			source.append("\t\tpublic enum_type enum_value;\n");
			
			//== Constructors ==
			source.append("\t\t//===Constructors===;\n");
			source.append(MessageFormat.format("\t\t{0}()'{'\n",e_defs.name));
			source.append(MessageFormat.format("\t\t\tenum_value = enum_type.{0};\n", UNBOUND_VALUE));
			source.append("\t\t};\n");
			
//TODO: arg int
//			source.append(MessageFormat.format("\t\t{0}(int other_value)'{'\n",e_defs.name));
//			source.append(MessageFormat.format("\t\t\tenum_value = enum_type.{0};\n", UNBOUND_VALUE));
//			source.append("\t\t};\n");
			
			//empty
			source.append(MessageFormat.format("\t\t{0}( {0} other_value)'{'\n", e_defs.name));
			source.append(MessageFormat.format("\t\t\tenum_value = other_value.enum_value;\n",e_defs.name));
			source.append("\t\t};\n");
			
			source.append(MessageFormat.format("\t\t{0}( {0}.enum_type other_value )'{'\n", e_defs.name));
			source.append("\t\t\tenum_value = other_value;\n");
			source.append("\t\t};\n");

			//== functions ==
			source.append("\t\t//===Methods===;\n");
			//TODO: enum2int
			generateValueIsPresent(source);
			generateValueIsBound(source);
			generateMustBound(source);
			generateValueOperatorEquals(source, e_defs.name);
			generateValueAssign(source, e_defs.name); 
			source.append("\t}\n");
//		}
	}
	
	public static void generateTemplateClass(final JavaGenData aData, final StringBuilder source, final Enum_Defs e_defs){
		source.append(MessageFormat.format("\tpublic static class {0}_template extends Base_Template '{'\n", e_defs.name, e_defs.templateName));
		
		//TODO: generate this, and others:
		generateTemplateSetType(source);
		source.append("\t}\n");
	}
	
	//===

	private static void generateValueIsPresent(final StringBuilder source){
		source.append("\t\tpublic boolean isPresent(){ return isBound(); }\n");
	}

	private static void generateValueIsBound(final StringBuilder source){
		source.append("\t\tpublic boolean isBound(){\n");
		source.append("\t\t\treturn (enum_value != enum_type.UNBOUND_VALUE);\n");
		source.append("\t\t}\n");
	}

	private static void generateValueOperatorEquals(final StringBuilder source,final String aName) {
		//Arg type: own type
		
		source.append(MessageFormat.format("\t\tpublic TitanBoolean operatorEquals(final {0} other_value)'{'\n", aName));
		source.append(MessageFormat.format("\t\t\t\treturn (new TitanBoolean( enum_value == other_value.enum_value));\n", aName)); 
		source.append("\t\t}\n");
		
		//Arg: Base_Type
		source.append("\t\tpublic TitanBoolean operatorEquals(final Base_Type other_value){\n");
		source.append(MessageFormat.format("\t\t\tif( other_value instanceof {0} ) '{'\n", aName));
		source.append(MessageFormat.format("\t\t\t\treturn this.operatorEquals( ({0}) other_value);\n", aName)); 
		source.append("\t\t\t} else {\n");
		source.append("\t\t\t//TODO:TtcnError message\n");
		source.append("\t\t\treturn (new TitanBoolean(false));\n");
		source.append("\t\t\t}\n");
		source.append("\t\t}\n");
		
		//Arg: enum_type
		source.append(MessageFormat.format("\t\tpublic TitanBoolean operatorEquals(final {0}.enum_type other_value)'{'\n",aName));
		source.append(MessageFormat.format("\t\t\t\treturn (new TitanBoolean( enum_value == other_value));\n", aName)); 
		source.append("\t\t}\n");
	}

	private static void generateMustBound(final StringBuilder source ) {
		source.append("\t\tpublic void mustBound( String errorMessage) {\n");
		source.append("\t\t\tif( !isBound() ) {\n");
		source.append("\t\t\t\tthrow new TtcnError( errorMessage );\n");
		source.append("\t\t\t};\n");
		source.append("\t\t};\n");		
	}
	
	private static void generateValueAssign(final StringBuilder source, final String name) {
		//Arg type: own type
		source.append(MessageFormat.format("\t\tpublic {0} assign(final {0} other_value)'{'\n", name));
		source.append("\t\t\t\tother_value.mustBound(\"Assignment of an unbound enumerated value\");\n");
		source.append(MessageFormat.format("\t\t\t\tthis.enum_value = other_value.enum_value;\n",  name));
		source.append("\t\t\treturn this;\n");
		source.append("\t\t}\n");
		
		//Arg: Base_Type
		source.append("\t\tpublic Base_Type assign(final Base_Type other_value){\n");
		source.append(MessageFormat.format("\t\t\tif( other_value instanceof {0} ) '{'\n", name));
		source.append(MessageFormat.format("\t\t\t\t return assign(({0}) other_value);\n", name));
		source.append("\t\t\t}\n");
		source.append(MessageFormat.format("\t\t\tthrow new TtcnError(MessageFormat.format(\"Internal Error: value `{0}'' can not be cast to {1}\", other_value));\n", name));
		source.append("\t\t}\n");
		//Arg: enum_type
		source.append(MessageFormat.format("\t\tpublic {0} assign(final {0}.enum_type other_value)'{'\n", name));
		source.append(MessageFormat.format("\t\t\treturn assign( new {0}(other_value) );\n",name));
		source.append("\t\t}\n");
	}
	
	private static void generateTemplateSetType(final StringBuilder source){
		source.append("\t\tpublic void setType(template_sel valueList, int i) {\n");
		source.append("\t\t\t//TODO: setType is not implemented yet\n");
		source.append("\t\t}\n");
	}
	


}
