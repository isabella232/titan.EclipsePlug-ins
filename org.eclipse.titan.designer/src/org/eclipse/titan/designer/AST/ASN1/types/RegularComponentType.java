/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.ASN1.types;

import java.util.List;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.TTCN3.types.CompField;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * ComponentType/regular (Contains only a Component).
 *
 * @author Kristof Szabados
 */
public final class RegularComponentType extends ComponentType {

	private final CompField componentField;

	public RegularComponentType(final CompField componentField) {
		this.componentField = componentField;

		componentField.setFullNameParent(this);
	}

	@Override
	/** {@inheritDoc} */
	public void setMyScope(final Scope scope) {
		componentField.setMyScope(scope);
	}

	@Override
	/** {@inheritDoc} */
	public int getNofComps() {
		return 1;
	}

	@Override
	/** {@inheritDoc} */
	public CompField getCompByIndex(final int index) {
		if (0 == index) {
			return componentField;
		}

		// FATAL_ERROR
		return null;
	}

	@Override
	/** {@inheritDoc} */
	public boolean hasCompWithName(final Identifier identifier) {
		if (null == identifier) {
			return false;
		}

		return identifier.getName().equals(componentField.getIdentifier().getName());
	}

	@Override
	/** {@inheritDoc} */
	public CompField getCompByName(final Identifier identifier) {
		if (null == identifier) {
			return null;
		}

		if (identifier.getName().equals(componentField.getIdentifier().getName())) {
			return componentField;
		}

		// FATAL_ERROR
		return null;
	}

	@Override
	/** {@inheritDoc} */
	public void trCompsof(final CompilationTimeStamp timestamp, final IReferenceChain referenceChain, final boolean isSet) {
		//Do nothing
	}

	@Override
	/** {@inheritDoc} */
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (componentField != null) {
			componentField.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	@Override
	/** {@inheritDoc} */
	protected boolean memberAccept(final ASTVisitor v) {
		if (componentField != null && !componentField.accept(v)) {
			return false;
		}
		return true;
	}
}
