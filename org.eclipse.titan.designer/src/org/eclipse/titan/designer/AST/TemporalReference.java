/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST;

import java.util.ArrayList;
import java.util.List;

/**
 * This a special reference kind which should only be used temporally, when the sub-references are backed by an other reference.
 * As such it is not setting scope information for the sub-references, to not alter the original information.
 *
 * @author Kristof Szabados
 * */
public final class TemporalReference extends Reference {

	public TemporalReference(final Identifier modid, final List<ISubReference> subReferences) {
		super(modid);
		this.subReferences = new ArrayList<ISubReference>(subReferences);
	}

	@Override
	/** {@inheritDoc} */
	public void setMyScope(final Scope scope) {
		myScope = scope;
	}
}
