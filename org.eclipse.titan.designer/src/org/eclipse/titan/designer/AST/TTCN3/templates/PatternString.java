/******************************************************************************
 * Copyright (c) 2000-2019 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.templates;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.IASTNode;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.IType.Type_type;
import org.eclipse.titan.designer.AST.IType.ValueCheckingOptions;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.IValue.Value_type;
import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.Type;
import org.eclipse.titan.designer.AST.Value;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Template;
import org.eclipse.titan.designer.AST.TTCN3.templates.PatternString.ps_elem_t.kind_t;
import org.eclipse.titan.designer.AST.TTCN3.types.CharString_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.UniversalCharstring_Type;
import org.eclipse.titan.designer.AST.TTCN3.values.Charstring_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Referenced_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.UniversalCharstring;
import org.eclipse.titan.designer.AST.TTCN3.values.UniversalCharstring_Value;
import org.eclipse.titan.designer.AST.GovernedSimple.CodeSectionType;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

// FIXME: implement
/**
 * @author Balazs Andor Zalanyi
 * @author Arpad Lovassy
 */
public final class PatternString implements IVisitableNode, INamedNode, IASTNode {

	public enum PatternType {
		CHARSTRING_PATTERN, UNIVCHARSTRING_PATTERN
	}

	private PatternType patterntype;

	/**
	 * The string content of the pattern
	 */
	private String content;

	/** the scope of the declaration of this node. */
	protected Scope myScope;
	/** the naming parent of the node. */
	private INamedNode nameParent;

	private boolean nocase = false;

	private Location location = null;

	/** String elements for the PatternStringLexer */
	private List<ps_elem_t> elems = new ArrayList<ps_elem_t>();

	public PatternString() {
		patterntype = PatternType.CHARSTRING_PATTERN;
	}

	public PatternString(final PatternType pt) {
		patterntype = pt;
	}

	public ps_elem_t get_last_elem() {
		if (elems.isEmpty()) {
			return null;
		}
		ps_elem_t last_elem = elems.get(elems.size()-1);
		if (last_elem.kind == kind_t.PSE_STR) {
			return last_elem;
		} else {
			return null;
		}
	}

	public void addChar(final char c) {
		ps_elem_t last_elem = get_last_elem();
		if (last_elem != null) {
			last_elem.str += c;
		}
		else {
			elems.add(new ps_elem_t(kind_t.PSE_STR, String.valueOf(c)));
		}
	}

	public void addString(final String p_str) {
		ps_elem_t last_elem = get_last_elem();
		if (last_elem != null) {
			last_elem.str += p_str;
		}
		else {
			elems.add(new ps_elem_t(kind_t.PSE_STR, p_str));
		}
	}

	public void addStringUSI(final List<String> usi_str) {
		UniversalCharstring s = new UniversalCharstring(usi_str, null);
		ps_elem_t last_elem = get_last_elem();
		if (last_elem != null) {
			last_elem.str += s.getStringRepresentationForPattern();
		} else {
			elems.add(new ps_elem_t(kind_t.PSE_STR, s.getStringRepresentationForPattern()));
		}
	}

	public void addRef(final Reference p_ref, final boolean N) {
		elems.add(new ps_elem_t(kind_t.PSE_REF, p_ref, N));
	}

	public PatternType getPatterntype() {
		return patterntype;
	}

	public void setPatterntype(final PatternType pt) {
		patterntype = pt;
	}

	public void setContent(final String s) {
		content = s;
	}

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public String getContent() {
		return content;
	}

	public String getFullString() {
		return content;
	}

	@Override
	/** {@inheritDoc} */
	public String getFullName() {
		return getFullName(this).toString();
	}

	@Override
	/** {@inheritDoc} */
	public StringBuilder getFullName(final INamedNode child) {
		if (null != nameParent) {
			return nameParent.getFullName(this);
		}

		return new StringBuilder();
	}

	@Override
	/** {@inheritDoc} */
	public final void setFullNameParent(final INamedNode nameParent) {
		this.nameParent = nameParent;
	}

	@Override
	/** {@inheritDoc} */
	public INamedNode getNameParent() {
		return nameParent;
	}

	@Override
	/** {@inheritDoc} */
	public void setMyScope(final Scope scope) {
		myScope = scope;
	}

	@Override
	/** {@inheritDoc} */
	public final Scope getMyScope() {
		return myScope;
	}

	/**
	 * Sets the code_section attribute of this pattern to the provided value.
	 *
	 * @param codeSection the code section where this pattern should be generated.
	 * */
	public void setCodeSection(final CodeSectionType codeSection) {
		//FIXME implement
	}

	/**
	 * Checks for circular references within embedded templates.
	 *
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 * @param referenceChain
	 *                the ReferenceChain used to detect circular references,
	 *                must not be null.
	 **/
	public void checkRecursions(final CompilationTimeStamp timestamp, final IReferenceChain referenceChain) {
		//Do nothing
	}

	@Override
	/** {@inheritDoc} */
	public boolean accept(final ASTVisitor v) {
		switch (v.visit(this)) {
		case ASTVisitor.V_ABORT:
			return false;
		case ASTVisitor.V_SKIP:
			return true;
		}
		// no members
		if (v.leave(this) == ASTVisitor.V_ABORT) {
			return false;
		}
		return true;
	}

	//FIXME comment
	public String create_charstring_literals(final Module module, final StringBuilder preamble) {
		//FIXME implement correctly

		return content;
	}

	/** Called by Value::get_value_refd_last() */
	public Value get_value() {
		if (content == null) {
			return null;
		} else {
			return new Charstring_Value(content);
		}
	}

	public void set_nocase(final boolean p_nocase) {
		nocase = p_nocase;
	}

	public boolean get_nocase() {
		return nocase;
	}

	// =================================
	// ===== PatternString.ps_elem_t
	// =================================
	public static class ps_elem_t {

		public static enum kind_t {PSE_STR, PSE_REF, PSE_REFDSET };

		private kind_t kind;
		private String str;
		private Reference ref;
		private Type t; // The type of the reference in the case of PSE_REFDSET
		private boolean with_N; // If the reference was given as \N{ref} in the pattern
		private boolean is_charstring; // \N{charstring}
		private boolean is_universal_charstring; // \N{universal charstring}

		public ps_elem_t(final kind_t p_kind, final String p_str) {
			kind = p_kind;
			str = p_str;
			ref = null;
			t = null;
			with_N = false;
			is_charstring = false;
			is_universal_charstring = false;
		}

		public ps_elem_t(final kind_t p_kind, final Reference p_ref, final boolean N) {
			kind = p_kind;
			str = null;
			with_N = N;
			is_charstring = false;
			is_universal_charstring = false;
			if (p_ref == null) {
				System.err.println("PatternString::ps_elem_t.ps_elem_t()");
			} else {
				ref = p_ref;
			}
		}

		public ps_elem_t(final ps_elem_t p) {
			kind = p.kind;
			str = null;
			ref = null;
			t = null;
			with_N = false;
			is_charstring = false;
			is_universal_charstring = false;
			switch (kind) {
			case PSE_STR:
				str = p.str;
				break;
			case PSE_REF:
				ref = p.ref;
				break;
			case PSE_REFDSET:
				System.err.println("PatternString::ps_elem_t::ps_elem_t");
			default:
				break;
			}
		}

		//use clean_up instead of ~ps_elem_t()
		public void clean_up() {
			switch (kind) {
			case PSE_STR:
				str = null;
				// fall through
			case PSE_REF:
			case PSE_REFDSET:
				ref = null;
				// do not delete t
				break;
			default:
				break;
			}
		}

		public void setFullName(final INamedNode parent_name) {
			switch (kind) {
			case PSE_REF:
			case PSE_REFDSET:
				ref.setFullNameParent(parent_name);
				break;
			default:
				break;
			}
		}

		public void setScope(final Scope p_scope) {
			switch (kind) {
			case PSE_REF:
			case PSE_REFDSET:
				ref.setMyScope(p_scope);
				break;
			default:
				break;
			}
		}

		public void checkRef(final PatternType pstr_type, final Expected_Value_type expected_value, final CompilationTimeStamp timestamp) {
			if (kind != kind_t.PSE_REF) {
				System.err.println("PatternString::ps_elem_t::chk_ref()");
				return;
			}

			IValue v = null;
			IValue v_last = null;
			if (ref.getId().getName() == "CHARSTRING") {
				is_charstring = true;
				return;
			} else if (ref.getId().getName() == "UNIVERSAL_CHARSTRING") {
				is_universal_charstring = true;
				return;
			}

			Assignment ass = ref.getRefdAssignment(timestamp, false);
			if (ass == null) {
				return;
			}
			IType ref_type = ass.getType(timestamp).getTypeRefdLast(timestamp).getFieldType(timestamp, ref, 1, expected_value, null, false);
			Type_type tt;
			switch (pstr_type) {
			case CHARSTRING_PATTERN:
				tt = Type_type.TYPE_CHARSTRING;
				if (ref_type.getTypetype() != Type_type.TYPE_CHARSTRING) {
					//FIXME: initial implement
					ref.getLocation().reportSemanticError("Type of the referenced %s '%s' should be 'charstring'");
				}
				break;
			case UNIVCHARSTRING_PATTERN:
				tt = ref_type.getTypetype();
				if (tt != Type_type.TYPE_CHARSTRING && tt != Type_type.TYPE_UCHARSTRING) {
					ref.getLocation().reportSemanticError("Type of the referenced %s '%s' should be either 'charstring' or 'universal charstring'");
				}
				break;
			default:
				System.err.println("Unknown pattern string type");
				return;
			}
			IType refcheckertype = null;
			if (tt == Type_type.TYPE_CHARSTRING) {
				refcheckertype = new CharString_Type();
			} else if (tt == Type_type.TYPE_UCHARSTRING) {
				refcheckertype = new UniversalCharstring_Type();
			}
			switch (ass.getAssignmentType()) {
			case A_TYPE:
				kind = kind_t.PSE_REFDSET;
				t = (Type) ass.getType(timestamp);
				break;
			case A_MODULEPAR_TEMPLATE:
			case A_VAR_TEMPLATE:
			case A_PAR_TEMP_IN:
			case A_PAR_TEMP_OUT:
			case A_PAR_TEMP_INOUT:
				// error reporting moved up
				break;
			case A_TEMPLATE:
				ITTCN3Template templ = null;
				templ = ((Def_Template) ass).getTemplate(timestamp);
				refcheckertype.checkThisTemplateRef(timestamp, templ);
				switch (templ.getTemplatetype()) {
				case SPECIFIC_VALUE:
					v_last = templ.getValue();
					break;
					//TODO: template concat in RT2
				case CSTR_PATTERN:
					if (!with_N) {
						PatternString ps = ((CharString_Pattern_Template)templ).getPatternstring();
						//TODO: has_refs()
						v_last = ps.get_value();
						break;
					}
				default:
					//TODO:error report
					System.err.println("Unable to resolve referenced '%s' to character string type. '%s' template cannot be used.");
					break;
				}
				break;
			default:
				Reference t_ref = ref;
				t_ref.setLocation(ref.getLocation());
				v = new Referenced_Value(t_ref);
				v.setMyGovernor(refcheckertype);
				v.setMyScope(ref.getMyScope());
				v.setLocation(ref.getLocation());
				refcheckertype.checkThisValue(timestamp, v, null, new ValueCheckingOptions(expected_value, false, false, true, false, false));
			}
			if (v_last != null && (v_last.getValuetype() == Value_type.CHARSTRING_VALUE || v_last.getValuetype() == Value_type.UNIVERSALCHARSTRING_VALUE)) {
				// the reference points to a constant substitute the reference with the known value
				if (v_last.getValuetype() == Value_type.CHARSTRING_VALUE) {
					if (with_N && ((Charstring_Value)v_last).getValue().length() != 1) {
						ref.getLocation().reportSemanticError("The length of the charstring must be of length one, when it is being referenced in a pattern with \\N{ref}");
					}
					str = ((Charstring_Value)v_last).getValue();
				} else {
					if (with_N && ((UniversalCharstring_Value)v_last).getValue().length() != 1) {
						ref.getLocation().reportSemanticError("The length of the universal charstring must be of length one, when it is being referenced in a pattern with \\N{ref}");
					}
					str = ((UniversalCharstring_Value)v_last).getValue().getStringRepresentation();
				}
				kind = kind_t.PSE_STR;
			}
			v = null;
		}

		public void setCodeSection(final CodeSectionType codeSection) {
			switch (kind) {
			case PSE_REF:
			case PSE_REFDSET:
				ref.setCodeSection(codeSection);
				break;
			default:
				break;
			}
		}
		@Override
		public String toString() {
			switch (kind) {
			case PSE_STR:
				return str;
			case PSE_REF:
			case PSE_REFDSET:
				return ref.getDisplayName();
			default:
				return "null";
			}
		}
		
	}
}

