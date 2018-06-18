/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.templates;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.titan.designer.AST.ASTNode;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.ILocateableNode;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.NULL_Location;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.GovernedSimple.CodeSectionType;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.TTCN3.IIncrementallyUpdateable;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * Represents the list of named actual parameters. For example in a function
 * call.
 *
 * @author Kristof Szabados
 * */
public final class NamedParameters extends ASTNode implements ILocateableNode, IIncrementallyUpdateable {
	private final List<NamedParameter> namedParams;

	private Location location;

	public NamedParameters() {
		super();
		namedParams = new ArrayList<NamedParameter>();
		location = NULL_Location.INSTANCE;
	}

	@Override
	/** {@inheritDoc} */
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);

		for (int i = 0, size = namedParams.size(); i < size; i++) {
			namedParams.get(i).setMyScope(scope);
		}
	}

	public void setCodeSection(final CodeSectionType codeSection) {
		for (int i = 0, size = namedParams.size(); i < size; i++) {
			namedParams.get(i).getInstance().setCodeSection(codeSection);
		}
	}

	@Override
	/** {@inheritDoc} */
	public StringBuilder getFullName(final INamedNode child) {
		final StringBuilder builder = super.getFullName(child);

		for (int i = 0, size = namedParams.size(); i < size; i++) {
			if (namedParams.get(i) == child) {
				return builder.append(INamedNode.SQUAREOPEN).append(String.valueOf(i + 1)).append(INamedNode.SQUARECLOSE);
			}
		}

		return builder;
	}

	public String createStringRepresentation() {
		final StringBuilder sb = new StringBuilder();
		for (NamedParameter n : namedParams) {
			sb.append(n.createStringRepresentation()).append(", ");
		}
		if (!namedParams.isEmpty()) {
			sb.setLength(sb.length() - 2);
		}
		return sb.toString();
	}

	/**
	 * Adds a new parameter to the list.
	 *
	 * @param parameter
	 *                the named parameter to be added.
	 * */
	public void addParameter(final NamedParameter parameter) {
		if (parameter != null && parameter.getName() != null) {
			namedParams.add(parameter);
			parameter.setFullNameParent(this);
		}
	}

	/** @return the number of parameters in the list */
	public int getNofParams() {
		return namedParams.size();
	}

	/**
	 * @param index
	 *                the index of the element to return.
	 *
	 * @return the parameter on the indexed position.
	 * */
	public NamedParameter getParamByIndex(final int index) {
		return namedParams.get(index);
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

	/**
	 * Handles the incremental parsing of this list of named parameters.
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

		for (int i = 0, size = namedParams.size(); i < size; i++) {
			final NamedParameter parameter = namedParams.get(i);

			parameter.updateSyntax(reparser, false);
			reparser.updateLocation(parameter.getLocation());
		}
	}

	@Override
	/** {@inheritDoc} */
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (namedParams == null) {
			return;
		}

		for (NamedParameter namedParam : namedParams) {
			namedParam.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	@Override
	/** {@inheritDoc} */
	protected boolean memberAccept(final ASTVisitor v) {
		if (namedParams != null) {
			for (NamedParameter np : namedParams) {
				if (!np.accept(v)) {
					return false;
				}
			}
		}
		return true;
	}
}
