/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.definitions;

import java.util.List;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.IType.TypeOwner_type;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.TTCN3.TTCN3Scope;
import org.eclipse.titan.designer.AST.TTCN3.types.Port_Type;
import org.eclipse.titan.designer.editors.ProposalCollector;
import org.eclipse.titan.designer.editors.actions.DeclarationCollector;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * The PortScope class represents the TTCN3 specific 'port' scope, which is
 * a link to the contents of the port type used by functions running on translation ports.
 *
 * @author Kristof Szabados
 * */
public class PortScope extends TTCN3Scope {

	private final Port_Type portType;
	private final Definitions variableDefinitions;

	public PortScope(final Port_Type portType, final Scope parentScope) {
		this.portType = portType;
		if (portType == null) {
			variableDefinitions = null;
		} else {
			portType.setOwnertype(TypeOwner_type.OT_PORT_SCOPE, this);
			variableDefinitions = portType.getPortBody().getVariableDefinitions();
		}
		setParentScope(parentScope);
	}

	@Override
	/** {@inheritDoc} */
	public PortScope getScopePort() {
		return this;
	}

	/**
	 * @return the port type this scope was created from.
	 * */
	public Port_Type getPortType() {
		return portType;
	}

	@Override
	/** {@inheritDoc} */
	public boolean hasAssignmentWithId(final CompilationTimeStamp timestamp, final Identifier identifier) {
		if (variableDefinitions != null && variableDefinitions.hasAssignmentWithId(timestamp, identifier)) {
			return true;
		}

		return super.hasAssignmentWithId(timestamp, identifier);
	}

	@Override
	/** {@inheritDoc} */
	public Assignment getAssBySRef(final CompilationTimeStamp timestamp, final Reference reference) {
		return getAssBySRef(timestamp, reference, null);
	}

	@Override
	/** {@inheritDoc} */
	public Assignment getAssBySRef(final CompilationTimeStamp timestamp, final Reference reference, final IReferenceChain refChain) {
		if (variableDefinitions != null && variableDefinitions.hasLocalAssignmentWithID(timestamp, reference.getId())) {
			return variableDefinitions.getLocalAssignmentByID(timestamp, reference.getId());
		}
		if (parentScope != null) {
			return parentScope.getAssBySRef(timestamp, reference);
		}

		return null;
	}

	@Override
	/** {@inheritDoc} */
	public void addProposal(final ProposalCollector propCollector) {
		if (propCollector.getReference().getModuleIdentifier() == null) {
			if (variableDefinitions != null) {
				variableDefinitions.addProposal(propCollector);
			}
		}
		super.addProposal(propCollector);
	}

	@Override
	/** {@inheritDoc} */
	public void addDeclaration(final DeclarationCollector declarationCollector) {
		if (declarationCollector.getReference().getModuleIdentifier() == null) {
			if (variableDefinitions != null
					&& variableDefinitions.hasLocalAssignmentWithID(CompilationTimeStamp.getBaseTimestamp(), declarationCollector.getReference().getId())) {
				final Definition def = variableDefinitions.getLocalAssignmentByID(CompilationTimeStamp.getBaseTimestamp(), declarationCollector.getReference().getId());
				declarationCollector.addDeclaration(def);
			}
		}
		super.addDeclaration(declarationCollector);
	}

	@Override
	/** {@inheritDoc} */
	public Assignment getEnclosingAssignment(final int offset) {
		return null;
	}

	@Override
	/** {@inheritDoc} */
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (portType != null) {
			portType.findReferences(referenceFinder, foundIdentifiers);
		}
		if (variableDefinitions != null) {
			variableDefinitions.findReferences(referenceFinder, foundIdentifiers);
		}
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
		if (portType != null) {
			if (!portType.accept(v)) {
				return false;
			}
		}
		if (variableDefinitions != null) {
			if (!variableDefinitions.accept(v)) {
				return false;
			}
		}
		if (v.leave(this) == ASTVisitor.V_ABORT) {
			return false;
		}
		return true;
	}
}
