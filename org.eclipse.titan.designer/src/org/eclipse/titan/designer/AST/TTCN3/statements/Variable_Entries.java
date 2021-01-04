/******************************************************************************
 * Copyright (c) 2000-2021 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.statements;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.titan.designer.AST.ASTNode;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.GovernedSimple.CodeSectionType;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.TTCN3.IIncrementallyUpdateable;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * @author Kristof Szabados
 * */
public final class Variable_Entries extends ASTNode implements IIncrementallyUpdateable {
	private static final String FULLNAMEPART = ".ve_";

	private final List<Variable_Entry> entries;

	public Variable_Entries() {
		super();
		entries = new ArrayList<Variable_Entry>();
	}

	@Override
	/** {@inheritDoc} */
	public StringBuilder getFullName(final INamedNode child) {
		final StringBuilder builder = super.getFullName(child);

		for (int i = 0; i < entries.size(); i++) {
			if (entries.get(i) == child) {
				return builder.append(FULLNAMEPART).append(Integer.toString(i + 1));
			}
		}

		return builder;
	}

	@Override
	/** {@inheritDoc} */
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);

		for (int i = 0; i < entries.size(); i++) {
			entries.get(i).setMyScope(scope);
		}
	}

	/**
	 * Sets the code_section attribute for the statements in these variable entries to the provided value.
	 *
	 * @param codeSection the code section where these statements should be generated.
	 * */
	public void setCodeSection(final CodeSectionType codeSection) {
		for (int i = 0; i < entries.size(); i++) {
			entries.get(i).setCodeSection(codeSection);
		}
	}

	public void add(final Variable_Entry entry) {
		if (entry != null) {
			entries.add(entry);
			entry.setFullNameParent(this);
		}
	}

	public int getNofEntries() {
		return entries.size();
	}

	public Variable_Entry getEntryByIndex(final int index) {
		return entries.get(index);
	}

	@Override
	/** {@inheritDoc} */
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}

		for (final Variable_Entry entry : entries) {
			entry.updateSyntax(reparser, isDamaged);
			reparser.updateLocation(entry.getLocation());
		}
	}

	@Override
	/** {@inheritDoc} */
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (entries == null) {
			return;
		}

		for (final Variable_Entry ve : entries) {
			ve.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	@Override
	/** {@inheritDoc} */
	protected boolean memberAccept(final ASTVisitor v) {
		if (entries != null) {
			for (final Variable_Entry ve : entries) {
				if (!ve.accept(v)) {
					return false;
				}
			}
		}
		return true;
	}
}
