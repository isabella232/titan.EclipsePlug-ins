/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.ASN1.Object;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.ASN1.ASN1Object;
import org.eclipse.titan.designer.AST.ASN1.ObjectClass;
import org.eclipse.titan.designer.AST.ASN1.ObjectSetElement_Visitor;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * ObjectSetElement Visitor, checker.
 *
 * @author Kristof Szabados
 */
public final class ObjectSetElementVisitor_checker extends ObjectSetElement_Visitor {

	private final ObjectClass governor;
	private final CompilationTimeStamp timestamp;

	public ObjectSetElementVisitor_checker(final CompilationTimeStamp timestamp, final Location location, final ObjectClass governor) {
		super(location);
		this.timestamp = timestamp;
		this.governor = governor;
	}

	@Override
	/** {@inheritDoc} */
	public void visitObject(final ASN1Object p) {
		p.setMyGovernor(governor);
		p.check(timestamp);
	}

	@Override
	/** {@inheritDoc} */
	public void visitObjectSetReferenced(final Referenced_ObjectSet p) {
		p.setMyGovernor(governor);
		p.check(timestamp);
	}

	@Override
	/** {@inheritDoc} */
	protected boolean memberAccept(final ASTVisitor v) {
		// TODO
		return true;
	}
}
