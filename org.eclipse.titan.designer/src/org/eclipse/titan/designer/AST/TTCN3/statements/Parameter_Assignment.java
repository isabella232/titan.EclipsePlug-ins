/******************************************************************************
 * Copyright (c) 2000-2019 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.statements;

import java.util.List;

import org.eclipse.titan.designer.AST.ASTNode;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.GovernedSimple.CodeSectionType;
import org.eclipse.titan.designer.AST.ILocateableNode;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.NULL_Location;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.Value;
import org.eclipse.titan.designer.AST.TTCN3.IIncrementallyUpdateable;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * Represents a parameter assignment in a parameter redirection.
 *
 * @author Kristof Szabados
 * */
public final class Parameter_Assignment extends ASTNode implements ILocateableNode, IIncrementallyUpdateable {
	private final Reference reference;
	private final Identifier identifier;
	private final Value encoding;
	private boolean is_decoded ;

	/**
	 * pointer to the type the redirected field or element is decoded into
	 * (only used if subrefs is not null and decoded is true), not owned
	 */
	private IType declarationType;

	private Location location = NULL_Location.INSTANCE;

	public Parameter_Assignment(final Reference reference, final Identifier identifier) {
		this.reference = reference;
		this.identifier = identifier;
		this.encoding = null;
		this.declarationType = null;

		if (reference != null) {
			reference.setFullNameParent(this);
		}
	}
	
	public Parameter_Assignment(final Reference reference, final Identifier identifier, final Value string_encoding) {
		this.reference = reference;
		this.identifier = identifier;
		this.encoding = string_encoding;
		this.is_decoded = true;
		this.declarationType = null;

		if (reference != null) {
			reference.setFullNameParent(this);
		}
		if (encoding != null) {
			encoding.setFullNameParent(this);
		}
	}

	public Identifier getIdentifier() {
		return identifier;
	}

	public Reference getReference() {
		return reference;
	}

	public boolean isDecoded() {
		return is_decoded;
	}

	public Value getStringEncoding() {
		return encoding;
	}

	public void setDeclarationType(final IType declarationType) {
		this.declarationType = declarationType;
	}

	public IType getDeclarationType() {
		return declarationType;
	}

	@Override
	/** {@inheritDoc} */
	public void setLocation(final Location location) {
		this.location = location;
	}

	@Override
	/** {@inheritDoc} */
	public Location getLocation() {
		return location;
	}

	@Override
	/** {@inheritDoc} */
	public StringBuilder getFullName(final INamedNode child) {
		final StringBuilder builder = super.getFullName(child);

		if (encoding == child) {
			return builder.append(".<string_encoding>");
		}

		return builder;
	}

	@Override
	/** {@inheritDoc} */
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		if (reference != null) {
			reference.setMyScope(scope);
		}
		if (encoding != null) {
			encoding.setMyScope(scope);
		}
	}

	/**
	 * Sets the code_section attribute for the statements in this parameter assignment to the provided value.
	 *
	 * @param codeSection the code section where these statements should be generated.
	 * */
	public void setCodeSection(final CodeSectionType codeSection) {
		if (reference != null) {
			reference.setCodeSection(codeSection);
		}
		if (encoding != null) {
			encoding.setCodeSection(codeSection);
		}
	}

	@Override
	/** {@inheritDoc} */
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}

		reference.updateSyntax(reparser, isDamaged);
		reparser.updateLocation(reference.getLocation());

		if (encoding != null) {
			encoding.updateSyntax(reparser, isDamaged);
			reparser.updateLocation(encoding.getLocation());
		}

		reparser.updateLocation(identifier.getLocation());
	}

	@Override
	/** {@inheritDoc} */
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (reference != null) {
			reference.findReferences(referenceFinder, foundIdentifiers);
		}
		if (identifier != null) {
			// TODO
		}
		if (encoding != null) {
			encoding.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	@Override
	/** {@inheritDoc} */
	protected boolean memberAccept(final ASTVisitor v) {
		if (reference != null && !reference.accept(v)) {
			return false;
		}
		if (encoding != null && !encoding.accept(v)) {
			return false;
		}
		if (identifier != null && !identifier.accept(v)) {
			return false;
		}
		return true;
	}
}
