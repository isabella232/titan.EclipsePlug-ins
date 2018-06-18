/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.declarationsearch;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;

/**
 * @author Szabolcs Beres
 * */
class DefinitionDeclaration extends Declaration {

	private Assignment ass;

	public DefinitionDeclaration(final Assignment ass) {
		this.ass = ass;
	}

	@Override
	public List<Hit> getReferences(final Module module) {
		try {
			final ReferenceFinder referenceFinder = new ReferenceFinder(ass);
			final List<Hit> result = referenceFinder.findReferencesInModule(module);
			if (ass.getMyScope().getModuleScope() == module) {
				result.add(new Hit(ass.getIdentifier()));
			}

			return result;
		} catch (final IllegalArgumentException e) {
			return new ArrayList<ReferenceFinder.Hit>();
		}
	}

	@Override
	public ReferenceFinder getReferenceFinder(final Module module) {
		try {
			final ReferenceFinder referenceFinder = new ReferenceFinder(ass);
			return referenceFinder;
		} catch (final IllegalArgumentException e) {
			return null;
		}
	}

	@Override
	public boolean shouldMarkOccurrences() {
		return ass.shouldMarkOccurrences();
	}

	@Override
	public Identifier getIdentifier() {
		return ass.getIdentifier();
	}

	@Override
	public Assignment getAssignment() {
		return ass;
	}
}
