/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

import java.util.List;

/**
 * This class represents the Param_Types.hh/cc file containing module parameter related structures.
 * 
 * TODO: for now all class are in this class, to delay architectural decision until we know more detail.
 * (most probably will be turned into a package later.)
 * 
 * @author Kristof Szabados
 * */
public final class Param_Types {

	/**
	 * Base class representing a module parameter as read from the configuration file.
	 *
	 * FIXME a lot to implement here
	 * Right now this is just a placeholder so that some could start working on module parameters.
	 */
	public static abstract class Module_Parameter {
		
		// list of all derived classes that can be instantiated
		public enum type_t {
			MP_NotUsed,
			MP_Omit,
			MP_Integer,
			MP_Float,
			MP_Boolean,
			MP_Verdict,
			MP_Objid,
			MP_Bitstring,
			MP_Hexstring,
			MP_Octetstring,
			MP_Charstring,
			MP_Universal_Charstring,
			MP_Enumerated,
			MP_Ttcn_Null,
			MP_Ttcn_mtc,
			MP_Ttcn_system,
			MP_Asn_Null,
			MP_Any,
			MP_AnyOrNone,
			MP_IntRange,
			MP_FloatRange,
			MP_StringRange,
			MP_Pattern,
			MP_Bitstring_Template,
			MP_Hexstring_Template,
			MP_Octetstring_Template,
			MP_Assignment_List,
			MP_Value_List,
			MP_Indexed_List,
			MP_List_Template,
			MP_ComplementList_Template,
			MP_Superset_Template,
			MP_Subset_Template,
			MP_Permutation_Template,
			MP_Reference,
			MP_Unbound,
			MP_Expression
		};

		public enum operation_type_t { OT_ASSIGN, OT_CONCAT };

		public enum basic_check_bits_t { // used to parametrize basic_check()
			BC_VALUE(0x00), // non-list values
			BC_LIST(0x01), // list values and templates
			BC_TEMPLATE(0x02);  // templates

			private final int value;

			private basic_check_bits_t(int value) {
				this.value = value;
			}

			public int getValue() {
				return value;
			}
		};

		// expression types for MP_Expression
		public enum expression_operand_t {
			EXPR_ERROR, // for reporting errors
			EXPR_ADD,
			EXPR_SUBTRACT,
			EXPR_MULTIPLY,
			EXPR_DIVIDE,
			EXPR_CONCATENATE,
			EXPR_NEGATE // only operand1 is used
		};

		protected operation_type_t operation_type;
		protected Module_Param_Id id;
		protected Module_Parameter parent; // null if no parent
		protected boolean has_ifpresent; // true if 'ifpresent' was used
		protected Module_Param_Length_Restriction length_restriction; // NULL if no length restriction

		public Module_Parameter() {
			operation_type = operation_type_t.OT_ASSIGN;
			id = null;
			parent = null;
			has_ifpresent = false;
			length_restriction = null;
		}

		public void set_parent(final Module_Parameter p_parent) {
			parent = p_parent;
		}

		public void set_id(final Module_Param_Id p_id) {
			if (id == null) {
				throw new TtcnError("Internal error: Module_Param.set_id()");
			}
			id = p_id;
		}

		/**
		 * @return the Id or error, never returns NULL (because every module parameter should have either an explicit or an implicit id when this is called)
		 * */
		public Module_Param_Id get_id() {
			return id;
		}

		public void set_ifpresent() {
			has_ifpresent = true;
		}

		public boolean get_ifpresent() {
			return has_ifpresent;
		}

		public void set_length_restriction(Module_Param_Length_Restriction p_length_restriction) {
			if (length_restriction != null) {
				throw new TtcnError("Internal error: Module_Param.set_length_restriction()");
			}
			length_restriction = p_length_restriction;
		}

		public Module_Param_Length_Restriction get_length_restriction() {
			return length_restriction;
		}

		public operation_type_t get_operation_type() {
			return operation_type;
		}

		public void set_operation_type(final operation_type_t p_optype) {
			operation_type = p_optype;
		}

		public String get_operation_type_str() {
			switch (operation_type) {
			case OT_ASSIGN:
				return "assignment";
			case OT_CONCAT:
				return "concatenation";
			default:
				return "<unknown operation>";
			}
		}

		public String get_operation_type_sign_str() {
			switch (operation_type) {
			case OT_ASSIGN:
				return ":=";
			case OT_CONCAT:
				return "&=";
			default:
				return "<unknown operation>";
			}
		}

		public void log(final boolean log_id) {
			//TODO: implement missing functions first
		}

		public abstract void log_value();

		//TODO: implement get_param_context()

	}

	/**
	 * Module parameter expression
	 * Contains an unprocessed module parameter expression with one or two operands.
	 * Expression types:
	 * with 2 operands: +, -, *, /, &
	 * with 1 operand: - (unary + is handled by the parser).
	 */
	public static class Module_Param_Expression extends Module_Parameter {

		private expression_operand_t expr_type;
		private Module_Parameter operand1;
		private Module_Parameter operand2;

		public Module_Param_Expression(expression_operand_t p_type, Module_Parameter p_op1, Module_Parameter p_op2) {
			expr_type = p_type;
			operand1 = p_op1;
			operand2 = p_op2;
			if (operand1 == null || operand2 == null) {
				throw new TtcnError("Internal error: Module_Param_Expression::Module_Param_Expression()");
			}
			operand1.set_parent(this);
			operand2.set_parent(this);
		}

		public Module_Param_Expression(Module_Parameter p_op) {
			expr_type = expression_operand_t.EXPR_NEGATE;
			operand1 = p_op;
			operand2 = null;
			if (operand1 == null) {
				throw new TtcnError("Internal error: Module_Param_Expression::Module_Param_Expression()");
			}
			operand1.set_parent(this);
		}

		public expression_operand_t get_expr_type() {
			return expr_type;
		}

		public String get_expr_type_str() {
			switch (expr_type) {
			case EXPR_ADD:
				return "Adding (+)";
			case EXPR_SUBTRACT:
				return "Subtracting (-)";
			case EXPR_MULTIPLY:
				return "Multiplying (*)";
			case EXPR_DIVIDE:
				return "Dividing (/)";
			case EXPR_NEGATE:
				return "Negating (-)";
			case EXPR_CONCATENATE:
				return "Concatenating (&)";
			default:
				return null;
			}
		}

		public Module_Parameter get_operand1() {
			return operand1;
		}

		public Module_Parameter get_operand2() {
			return operand2;
		}

		public type_t get_type() {
			return type_t.MP_Expression;
		}

		public String get_type_str() {
			return "expression";
		}

		public void log_value() {
			if (expr_type == expression_operand_t.EXPR_NEGATE) {
				TTCN_Logger.log_event_str("- ");
			}
			operand1.log_value();
			switch (expr_type) {
			case EXPR_ADD:
				TTCN_Logger.log_event_str(" + ");
				break;
			case EXPR_SUBTRACT:
				TTCN_Logger.log_event_str(" - ");
				break;
			case EXPR_MULTIPLY:
				TTCN_Logger.log_event_str(" * ");
				break;
			case EXPR_DIVIDE:
				TTCN_Logger.log_event_str(" / ");
				break;
			case EXPR_CONCATENATE:
				TTCN_Logger.log_event_str(" & ");
				break;
			default:
				break;
			}
			if (expr_type != expression_operand_t.EXPR_NEGATE) {
				operand2.log_value();
			}
		}
	}

	public static class Module_Param_Integer extends Module_Parameter {

		private TitanInteger integer_value;

		public type_t get_type() {
			return type_t.MP_Integer;
		}

		public Module_Param_Integer(final TitanInteger p) {
			integer_value = p;
			if (integer_value == null) {
				throw new TtcnError("Internal error: Module_Param_Integer::Module_Param_Integer()");
			}
		}

		public TitanInteger get_integer() {
			return integer_value;
		}

		public String get_type_str() {
			return "integer";
		}

		@Override
		public void log_value() {
			integer_value.log();
		}
	}

	public static class Module_Param_Float extends Module_Parameter {

		private double float_value;

		public type_t get_type() {
			return type_t.MP_Float; }

		public Module_Param_Float(final double p) {
			float_value = p;
		}

		public double get_float() {
			return float_value;
		}

		public String get_type_str() {
			return "float";
		}

		@Override
		public void log_value() {
			TitanFloat.log_float(float_value);
		}
	}

	public static class Module_Param_Boolean extends Module_Parameter {

		private boolean boolean_value;

		public type_t get_type() {
			return type_t.MP_Boolean;
		}

		public Module_Param_Boolean(final boolean p) {
			boolean_value = p;
		}

		public boolean get_boolean() {
			return boolean_value;
		}

		public String get_type_str() {
			return "boolean";
		}

		@Override
		public void log_value() {
			new TitanBoolean(boolean_value).log();
		}
	}

	public static class Module_Param_Enumerated extends Module_Parameter {

		private String enum_value;

		public type_t get_type() {
			return type_t.MP_Enumerated;
		}

		public Module_Param_Enumerated(final String p_e) {
			enum_value = p_e;
		}

		public String get_enumerated() {
			return enum_value;
		}

		public String get_type_str() {
			return "enumerated";
		}

		@Override
		public void log_value() {
			TTCN_Logger.log_event_str(enum_value);
		}
	}

	public static class Module_Param_Verdict extends Module_Parameter {

		private TitanVerdictType verdict_value;

		public type_t get_type() {
			return type_t.MP_Verdict;
		}

		public Module_Param_Verdict(TitanVerdictType p) {
			verdict_value = p;
		}

		public TitanVerdictType get_verdict() {
			return verdict_value;
		}

		public String get_type_str() {
			return "verdict";
		}

		@Override
		public void log_value() {
			verdict_value.log();
		}
	}

	public static class Module_Param_Objid extends Module_Parameter {
		// special string of integers :)

		private int n_chars;
		private TitanInteger[] chars_ptr;

		public type_t get_type() {
			return type_t.MP_Objid; 
		}

		public Module_Param_Objid(final int p_n, final TitanInteger[] p_c) {
			n_chars = p_n;
			chars_ptr = p_c;
		}

		public String get_type_str() {
			return "object identifier";
		}

		@Override
		public void log_value() {
			new TitanObjectid(n_chars, chars_ptr).log();
		}
	}

	public static class Module_Param_Bitstring extends Module_Parameter {

		private TitanBitString bstr;

		public type_t get_type() {
			return type_t.MP_Bitstring;
		}

		public Module_Param_Bitstring(final String str) {
			bstr = new TitanBitString(str);
		}

		public String get_type_str() {
			return "bitstring";
		}

		@Override
		public void log_value() {
			bstr.log();
		}
	}

	public static class Module_Param_Hexstring extends Module_Parameter {

		private TitanHexString hstr;

		public type_t get_type() {
			return type_t.MP_Hexstring;
		}

		public Module_Param_Hexstring(final String str) {
			hstr = new TitanHexString(str);
		}

		public String get_type_str() {
			return "hexstring";
		}

		public void log_value() {
			hstr.log();
		}
	}

	public static class Module_Param_Octetstring extends Module_Parameter {

		private TitanHexString ostr;

		public type_t get_type() {
			return type_t.MP_Octetstring;
		}

		public Module_Param_Octetstring(final String str) {
			ostr = new TitanHexString(str);
		}

		public String get_type_str() {
			return "octetstring";
		}

		public void log_value() {
			ostr.log();
		}
	}

	public static class Module_Param_Charstring extends Module_Parameter {

		private TitanCharString cstr;

		public type_t get_type() {
			return type_t.MP_Charstring;
		}

		public Module_Param_Charstring(final String str) {
			cstr = new TitanCharString(str);
		}

		public String get_type_str() {
			return "charstring";
		}

		public void log_value() {
			cstr.log();
		}
	}

	public static class Module_Param_Universal_Charstring extends Module_Parameter {

		private TitanUniversalCharString ucstr;

		public type_t get_type() {
			return type_t.MP_Universal_Charstring;
		}

		public Module_Param_Universal_Charstring(final String str) {
			ucstr = new TitanUniversalCharString(str);
		}

		public String get_type_str() {
			return "universal charstring";
		}

		@Override
		public void log_value() {
			ucstr.log();
		}
	}

	public static class Module_Param_Omit extends Module_Parameter {

		public type_t get_type() {
			return type_t.MP_Omit;
		}

		public String get_type_str() {
			return "omit";
		}

		@Override
		public void log_value() {
			TTCN_Logger.log_event_str("omit");
		}
	}

	public static class Module_Param_Id {

		public boolean is_explicit() {
			return false;
		}

		public boolean is_index() {
			return false;
		}

		public boolean is_custom() {
			return false;
		}

		public int get_index() {
			throw new TtcnError("Internal error: Module_Param_Id.get_index()");
		}

		public String get_name() {
			throw new TtcnError("Internal error: Module_Param_Id.get_name()");
		}

		public String get_current_name() {
			throw new TtcnError("Internal error: Module_Param_Id.get_current_name()");
		}

		public boolean next_name() {
			throw new TtcnError("Internal error: Module_Param_Id.next_name()");
		}

		public void reset() {
			throw new TtcnError("Internal error: Module_Param_Id.reset()");
		}

		public int get_nof_names() {
			throw new TtcnError("Internal error: Module_Param_Id.get_nof_names()");
		}

		public String get_str() {
			return "";
		}
	}

	public static class Module_Param_Name extends Module_Param_Id {
		/** The first elements are the module name (if any) and the module parameter name,
		 * followed by record/set field names and array (or record of/set of) indexes.
		 * Since the names of modules, module parameters and fields cannot start with
		 * numbers, the indexes are easily distinguishable from these elements. */

		private List<String> names;
		private int pos;
		
		public Module_Param_Name(final List<String> p) {
			names = p;
			pos = 0;
		}
		
		@Override
		public boolean is_explicit() {
			return true;
		}

		@Override
		public String get_current_name() {
			return names.get(pos);
		}
		
		@Override
		public boolean next_name() {
			if (pos + 1 >= names.size()) {
				return false;
			}
			++pos;
			return true;
		}
		
		@Override
		public void reset() {
			pos = 0;
		}
		
		@Override
		public int get_nof_names() {
			return names.size();
		}
		
		@Override
		public String get_str() {
			StringBuilder result = new StringBuilder();
			for (int i = 0; i < names.size(); i++) {
				boolean index = names.get(i).charAt(0) >= '0' && names.get(i).charAt(0) <= '9';
				if (i > 0 && !index) {
					result.append('.');
				}
				if (index) {
					result.append('[');
				}
				result.append(names.get(i));
				if (index) {
					result.append(']');
				}
			}
			return result.toString();
		}
	}

	public static class Module_Param_FieldName extends Module_Param_Id {

		private String name;

		public Module_Param_FieldName(final String p) {
			name = p;
		}

		@Override
		public String get_name() {
			return name;
		}

		@Override
		public boolean is_explicit() {
			return true;
		}

		@Override
		public String get_str() {
			return name;
		}
	}

	public static class Module_Param_Index extends Module_Param_Id {

		private int index;
		private boolean is_expl;

		public Module_Param_Index(final int p_index, final boolean p_is_expl) {
			index = p_index;
			is_expl = p_is_expl;
		}

		@Override
		public int get_index() {
			return index;
		}

		@Override
		public boolean is_index() {
			return true;
		}

		@Override
		public boolean is_explicit() {
			return is_expl;
		}

		@Override
		public String get_str() {
			return '[' + String.valueOf(index) + ']';
		}
	}

	/** Custom module parameter name class, used in Module_Param instances that aren't
	 * actual module parameters (length boundaries, array indexes and character codes in
	 * quadruples use temporary Module_Param instances to allow the use of expressions
	 * and references to module parameters).
	 * Errors reported in these cases will contain the custom text set in this class,
	 * instead of the regular error message header. */
	public static class Module_Param_CustomName extends Module_Param_Id {
		
		private String name;
		
		public Module_Param_CustomName(final String p) {
			name = p;
		}
		
		@Override
		public String get_name() {
			return name;
		}
		
		@Override
		public boolean is_explicit() {
			return true;
		}
		
		@Override
		public String get_str() {
			return name;
		}
		
		@Override
		public boolean is_custom() {
			return true;
		}
	}
}
