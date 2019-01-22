/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.templates;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.IASTNode;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.AST.Scope;
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

	public PatternString() {
		patterntype = PatternType.CHARSTRING_PATTERN;
	}

	public PatternString(final PatternType pt) {
		patterntype = pt;
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

}
