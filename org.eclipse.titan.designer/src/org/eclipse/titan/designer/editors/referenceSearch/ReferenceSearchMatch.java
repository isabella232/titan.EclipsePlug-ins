/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors.referenceSearch;

import org.eclipse.search.ui.text.Match;
import org.eclipse.titan.designer.AST.Identifier;

/**
 * @author Szabolcs Beres
 * */
public class ReferenceSearchMatch extends Match {

	private Identifier id;

	public ReferenceSearchMatch(final Identifier id) {
		super(id.getLocation().getFile(), id.getLocation().getOffset(), id.getLocation().getEndOffset() - id.getLocation().getOffset());
		this.id = id;
	}

	public Identifier getId() {
		return id;
	}
}
