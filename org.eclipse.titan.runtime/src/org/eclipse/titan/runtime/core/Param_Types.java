/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;


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
	public static class Module_Parameter {
		
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

		public void set_parent(Module_Parameter p_parent) {
			parent = p_parent;
		}

		public void set_id(Module_Param_Id p_id) {
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

		public void set_operation_type(operation_type_t p_optype) {
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

		public void log(boolean log_id) {
			//TODO: implement missing functions first
		}

		//TODO: implement get_param_context()

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
}
