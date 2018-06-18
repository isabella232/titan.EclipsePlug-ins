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
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.Value;
import org.eclipse.titan.designer.AST.TTCN3.types.CompField;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * ExtensionAdditionGroup.
 *
 * @author Kristof Szabados
 */
public final class ExtensionAdditionGroup extends ExtensionAddition {
	/** right now can only be null as not present */
	private final Value versionNumber;

	private final ComponentTypeList componentTypes;

	public ExtensionAdditionGroup(final Value versionNumber, final ComponentTypeList componentTypes) {
		this.versionNumber = versionNumber;
		this.componentTypes = componentTypes;

		if (null != versionNumber) {
			versionNumber.setFullNameParent(this);
		}
		componentTypes.setFullNameParent(this);
	}

	@Override
	/** {@inheritDoc} */
	public StringBuilder getFullName(final INamedNode child) {
		final StringBuilder builder = super.getFullName(child);

		if (versionNumber == child) {
			builder.append(".<versionnumber>");
		}

		return builder;
	}

	@Override
	/** {@inheritDoc} */
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		if (null != versionNumber) {
			versionNumber.setMyScope(scope);
		}
		componentTypes.setMyScope(scope);
	}

	@Override
	/** {@inheritDoc} */
	public int getNofComps() {
		return componentTypes.getNofComps();
	}

	@Override
	/** {@inheritDoc} */
	public CompField getCompByIndex(final int index) {
		return componentTypes.getCompByIndex(index);
	}

	@Override
	/** {@inheritDoc} */
	public boolean hasCompWithName(final Identifier identifier) {
		return componentTypes.hasCompWithName(identifier);
	}

	@Override
	/** {@inheritDoc} */
	public CompField getCompByName(final Identifier identifier) {
		return componentTypes.getCompByName(identifier);
	}

	@Override
	/** {@inheritDoc} */
	public void trCompsof(final CompilationTimeStamp timestamp, final IReferenceChain referenceChain, final boolean isSet) {
		componentTypes.trCompsof(timestamp, referenceChain, isSet);
	}

	@Override
	/** {@inheritDoc} */
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (versionNumber != null) {
			versionNumber.findReferences(referenceFinder, foundIdentifiers);
		}
		if (componentTypes != null) {
			componentTypes.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	@Override
	/** {@inheritDoc} */
	protected boolean memberAccept(final ASTVisitor v) {
		if (versionNumber != null && !versionNumber.accept(v)) {
			return false;
		}

		if (componentTypes != null && !componentTypes.accept(v)) {
			return false;
		}

		return true;
	}
}
