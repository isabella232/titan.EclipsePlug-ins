/******************************************************************************
 * Copyright (c) 2000-2021 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.templates;

import java.util.List;

import org.eclipse.titan.designer.AST.ASTNode;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.ILocateableNode;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.NULL_Location;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.TTCN3.IIncrementallyUpdateable;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * Represents a named actual parameter. For example in a function call.
 *
 * @author Kristof Szabados
 * */
public final class NamedParameter extends ASTNode implements ILocateableNode, IIncrementallyUpdateable {

	private final Identifier name;
	private final TemplateInstance instance;

	private Location location;

	public NamedParameter(final Identifier name, final TemplateInstance instance) {
		super();
		this.name = name;
		this.instance = instance;
		location = NULL_Location.INSTANCE;

		if (instance != null) {
			instance.setFullNameParent(this);
		}
	}

	@Override
	/** {@inheritDoc} */
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		if (instance != null) {
			instance.setMyScope(scope);
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

	public Identifier getName() {
		return name;
	}

	public String createStringRepresentation() {
		if (name == null || instance == null) {
			return "<unknown named parameter>";
		} else {
			final StringBuilder sb = new StringBuilder();
			sb.append(name.getName());
			sb.append(" := ");
			sb.append(instance.createStringRepresentation());
			return sb.toString();
		}
	}

	public TemplateInstance getInstance() {
		return instance;
	}

	/**
	 * Handles the incremental parsing of this named parameter.
	 *
	 * @param reparser
	 *                the parser doing the incremental parsing.
	 * @param isDamaged
	 *                true if the location contains the damaged area, false
	 *                if only its' location needs to be updated.
	 * */
	@Override
	/** {@inheritDoc} */
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}

		reparser.updateLocation(name.getLocation());

		if (instance != null) {
			instance.updateSyntax(reparser, false);
			reparser.updateLocation(instance.getLocation());
		}
	}

	@Override
	/** {@inheritDoc} */
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (instance != null) {
			instance.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	@Override
	/** {@inheritDoc} */
	protected boolean memberAccept(final ASTVisitor v) {
		if (name != null && !name.accept(v)) {
			return false;
		}
		if (instance != null && !instance.accept(v)) {
			return false;
		}
		return true;
	}
}
