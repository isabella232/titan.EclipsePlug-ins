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
 * Represent a variable or a not used sign in port redirect "scope".
 *
 * @author Kristof Szabados
 * */
public final class Variable_Entry extends ASTNode implements ILocateableNode, IIncrementallyUpdateable {
	private static final String FULLNAMEPART = ".reference";
	// variable reference or null in case of notused
	private final Reference reference;

	private final boolean decoded;

	private final Value stringEncoding;

	private final IType declarationType;

	private Location location = NULL_Location.INSTANCE;

	public Variable_Entry() {
		reference = null;
		decoded = false;
		stringEncoding = null;
		declarationType = null;
	}

	public Variable_Entry(final Reference reference) {
		this.reference = reference;
		decoded = false;
		stringEncoding = null;
		declarationType = null;

		if (reference != null) {
			reference.setFullNameParent(this);
		}
	}

	public Variable_Entry(final Reference reference, final boolean decoded, final Value encodingString, final IType declarationType) {
		this.reference = reference;
		this.decoded = decoded;
		this.stringEncoding = encodingString;
		this.declarationType = declarationType;

		if (reference != null) {
			reference.setFullNameParent(this);
		}
		if (encodingString != null) {
			encodingString.setFullNameParent(this);
		}
	}

	public Reference getReference() {
		return reference;
	}

	public boolean isDecoded() {
		return decoded;
	}

	public Value getStringEncoding() {
		return stringEncoding;
	}

	public IType getDeclarationType() {
		return declarationType;
	}

	@Override
	/** {@inheritDoc} */
	public StringBuilder getFullName(final INamedNode child) {
		final StringBuilder builder = super.getFullName(child);

		if (reference == child) {
			return builder.append(FULLNAMEPART);
		} else if (stringEncoding == child) {
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
		if (stringEncoding != null) {
			stringEncoding.setMyScope(scope);
		}
	}

	/**
	 * Sets the code_section attribute for the statements in this variable entry to the provided value.
	 *
	 * @param codeSection the code section where these statements should be generated.
	 * */
	public void setCodeSection(final CodeSectionType codeSection) {
		if (reference != null) {
			reference.setCodeSection(codeSection);
		}
		if (stringEncoding != null) {
			stringEncoding.setCodeSection(codeSection);
		}
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
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}

		reference.updateSyntax(reparser, isDamaged);
		reparser.updateLocation(reference.getLocation());

		if (stringEncoding != null) {
			stringEncoding.updateSyntax(reparser, isDamaged);
			reparser.updateLocation(stringEncoding.getLocation());
		}
	}

	@Override
	/** {@inheritDoc} */
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (reference == null) {
			return;
		}

		reference.findReferences(referenceFinder, foundIdentifiers);

		if (stringEncoding != null) {
			stringEncoding.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	@Override
	/** {@inheritDoc} */
	protected boolean memberAccept(final ASTVisitor v) {
		if (reference != null && !reference.accept(v)) {
			return false;
		}
		if (stringEncoding != null && !stringEncoding.accept(v)) {
			return false;
		}

		return true;
	}
}
