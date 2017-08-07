/******************************************************************************
 * Copyright (c) 2000-2017 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.TTCN3.IIncrementallyUpdateable;
import org.eclipse.titan.designer.AST.TTCN3.TemplateRestriction;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.ExpressionStruct;
import org.eclipse.titan.designer.compiler.JavaGenData;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * Represents a list of template instances.
 *
 * @author Kristof Szabados
 */
public final class TemplateInstances extends ASTNode implements ILocateableNode, IIncrementallyUpdateable {

	private final List<TemplateInstance> instances;

	/** The location of the template instances. */
	private Location location;

	public TemplateInstances() {
		super();
		instances = new ArrayList<TemplateInstance>();
		location = NULL_Location.INSTANCE;
	}

	public TemplateInstances(final TemplateInstances other) {
		super();
		instances = new ArrayList<TemplateInstance>(other.instances.size());
		for (int i = 0, size = other.instances.size(); i < size; i++) {
			instances.add(other.instances.get(i));
		}
		location = other.location;
	}

	@Override
	/** {@inheritDoc} */
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);

		for (int i = 0, size = instances.size(); i < size; i++) {
			instances.get(i).setMyScope(scope);
		}
	}

	@Override
	/** {@inheritDoc} */
	public StringBuilder getFullName(final INamedNode child) {
		final StringBuilder builder = super.getFullName(child);

		for (int i = 0, size = instances.size(); i < size; i++) {
			if (instances.get(i) == child) {
				return builder.append(INamedNode.SQUAREOPEN).append(String.valueOf(i + 1)).append(INamedNode.SQUARECLOSE);
			}
		}

		return builder;
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
	 * Adds a new template instance to the list.
	 *
	 * @param instance
	 *                the template instance to add.
	 * */
	public void addTemplateInstance(final TemplateInstance instance) {
		if (instance != null) {
			instances.add(instance);
			instance.setFullNameParent(this);
		}
	}

	/** @return the number of template instances in the list */
	public int getNofTis() {
		return instances.size();
	}

	/**
	 * @param index
	 *                the index of the element to return.
	 *
	 * @return the template instance on the indexed position.
	 * */
	public TemplateInstance getInstanceByIndex(final int index) {
		return instances.get(index);
	}

	/**
	 * Handles the incremental parsing of this list of template instances.
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

		for (int i = 0, size = instances.size(); i < size; i++) {
			final TemplateInstance instance = instances.get(i);

			instance.updateSyntax(reparser, false);
			reparser.updateLocation(instance.getLocation());
		}
	}

	@Override
	/** {@inheritDoc} */
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (instances == null) {
			return;
		}

		for (TemplateInstance ti : instances) {
			ti.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	@Override
	/** {@inheritDoc} */
	protected boolean memberAccept(final ASTVisitor v) {
		if (instances != null) {
			for (TemplateInstance ti : instances) {
				if (!ti.accept(v)) {
					return false;
				}
			}
		}
		return true;
	}

	public String createStringRepresentation() {
		final StringBuilder sb = new StringBuilder();
		for (TemplateInstance ti : instances) {
			sb.append(ti.createStringRepresentation()).append(", ");
		}
		if (!instances.isEmpty()) {
			sb.setLength(sb.length() - 2);
		}
		return sb.toString();
	}

	/**
	 * Add generated java code on this level.
	 * @param aData the structure to put imports into and get temporal variable names from.
	 * @param expression the expression for code generated
	 * @param templateRestriction the template restriction to check in runtime
	 */
	public void generateCode( final JavaGenData aData, final ExpressionStruct expression, final TemplateRestriction.Restriction_type templateRestriction ) {
		if ( instances == null ) {
			return;
		}
		final int size = instances.size();
		if ( size > 0 ) {
			expression.expression.append( " " );
			for ( int i = 0; i < size; i++ ) {
				if ( i > 0 ) {
					expression.expression.append( ", " );
				}
				instances.get( i ).generateCode( aData, expression, templateRestriction );
			}
			expression.expression.append( " " );
		}
	}
}
