/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.ASN1;

import java.util.List;

import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.ISetting;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.compiler.JavaGenData;
import org.eclipse.titan.designer.editors.ProposalCollector;
import org.eclipse.titan.designer.editors.actions.DeclarationCollector;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * An undefined assignment.
 *
 * @author Kristof Szabados
 * */
public abstract class Undefined_Assignment extends ASN1Assignment {
	protected static final String CIRCULARASSIGNMENTCHAIN = "Circular assignment chain: {0}";
	protected static final String UNRECOGNISABLEASSIGNMENT = "Cannot recognise this assignment";

	/** the scope of the right side of this assignment. */
	protected Scope rightScope;
	/** the classified assignment. */
	protected ASN1Assignment realAssignment;

	public Undefined_Assignment(final Identifier id, final Ass_pard assPard) {
		super(id, assPard);
	}

	@Override
	/** {@inheritDoc} */
	public final Assignment_type getAssignmentType() {
		if (null != realAssignment) {
			return realAssignment.getAssignmentType();
		}

		return Assignment_type.A_UNDEF;
	}

	@Override
	/** {@inheritDoc} */
	public final String getAssignmentName() {
		if (null != realAssignment) {
			return realAssignment.getAssignmentName();
		}

		return "<undefined assignment>";
	}

	public final ASN1Assignment getRealAssignment(final CompilationTimeStamp timestamp) {
		check(timestamp);

		return realAssignment;
	}

	@Override
	/** {@inheritDoc} */
	public final void setRightScope(final Scope rightScope) {
		if (null != realAssignment) {
			realAssignment.setRightScope(rightScope);
		}
		this.rightScope = rightScope;
	}

	@Override
	/** {@inheritDoc} */
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		if (null != realAssignment) {
			realAssignment.setMyScope(scope);
		}
		rightScope = scope;
	}

	@Override
	/** {@inheritDoc} */
	public final ISetting getSetting(final CompilationTimeStamp timestamp) {
		check(timestamp);

		if (null != realAssignment) {
			return realAssignment.getSetting(timestamp);
		}

		return null;
	}

	@Override
	/** {@inheritDoc} */
	public final IType getType(final CompilationTimeStamp timestamp) {
		check(timestamp);

		if (null != realAssignment) {
			return realAssignment.getType(timestamp);
		}

		return null;
	}

	@Override
	/** {@inheritDoc} */
	public void check(final CompilationTimeStamp timestamp) {
		check(timestamp, null);
	}

	@Override
	/** {@inheritDoc} */
	public void check(final CompilationTimeStamp timestamp, final IReferenceChain refChain) {
		if (null != lastTimeChecked && !lastTimeChecked.isLess(timestamp)) {
			return;
		}

		if (null != myScope && null != lastTimeChecked) {
			final Module module = myScope.getModuleScope();
			if (null != module) {
				if (module.getSkippedFromSemanticChecking()) {
					lastTimeChecked = timestamp;
					return;
				}
			}
		}

		lastTimeChecked = timestamp;

		if (null != assPard) {
			assPard.check(timestamp);
			return;
		}

		classifyAssignment(timestamp, null);
		if (null != realAssignment) {
			realAssignment.check(timestamp);
		}
	}

	@Override
	/** {@inheritDoc} */
	public final boolean isAssignmentType(final CompilationTimeStamp timestamp, final Assignment_type assignmentType,
			final IReferenceChain referenceChain) {
		check(timestamp);

		if (null == realAssignment) {
			return false;
		}

		return getIsErroneous() ? false : realAssignment.isAssignmentType(timestamp, assignmentType, referenceChain);
	}

	/**
	 * Classifies the actually unknown assignment.
	 *
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle
	 * @param referenceChain
	 *                this reference chain is used to detect recursive
	 *                references if needed
	 * */
	protected abstract void classifyAssignment(final CompilationTimeStamp timestamp, final IReferenceChain referenceChain);

	@Override
	/** {@inheritDoc} */
	public final Object[] getOutlineChildren() {
		if (null == realAssignment) {
			return super.getOutlineChildren();
		}

		return realAssignment.getOutlineChildren();
	}

	@Override
	/** {@inheritDoc} */
	public final String getOutlineIcon() {
		if (null != realAssignment) {
			return realAssignment.getOutlineIcon();
		}
		return "titan.gif";
	}

	@Override
	/** {@inheritDoc} */
	public final void addDeclaration(final DeclarationCollector declarationCollector, final int i) {
		if (null != realAssignment) {
			realAssignment.addDeclaration(declarationCollector, i);
		}
	}

	// TODO: remove when location is fixed
	@Override
	/** {@inheritDoc} */
	public Location getLikelyLocation() {
		if (realAssignment != null) {
			return realAssignment.getLikelyLocation();
		}

		return super.getLikelyLocation();
	}

	@Override
	/** {@inheritDoc} */
	public final void addProposal(final ProposalCollector propCollector, final int i) {
		if (null != realAssignment) {
			realAssignment.addProposal(propCollector, i);
		}
	}

	@Override
	/** {@inheritDoc} */
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		super.findReferences(referenceFinder, foundIdentifiers);
		if (realAssignment != null) {
			realAssignment.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	@Override
	/** {@inheritDoc} */
	protected boolean memberAccept(final ASTVisitor v) {
		if (!super.memberAccept(v)) {
			return false;
		}
		if (realAssignment != null && !realAssignment.accept(v)) {
			return false;
		}
		return true;
	}

	@Override
	public void generateCode(JavaGenData aData, boolean cleanUp) {
		if (realAssignment != null) {
			realAssignment.generateCode(aData, cleanUp);
			return;
		}

		ErrorReporter.INTERNAL_ERROR("Code generator reached erroneous undefined assignment `" + getFullName() + "''");
		aData.getSrc().append("FATAL_ERROR encountered");
	}
}
