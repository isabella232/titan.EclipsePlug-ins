/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titanium.refactoring.logging.context;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titanium.refactoring.logging.ContextLoggingRefactoring.Settings;

/**
 * Context class representing uninteresting nodes.
 * All the nodes from which no log arguments are created are represented by this class.
 *
 * @author Viktor Varga
 */
class NullContext extends Context {

	NullContext(final IVisitableNode node, final Settings settings) {
		super(node, settings);
	}

	@Override
	protected void process_internal() {}

	@Override
	protected List<String> createLogParts_internal(final Set<String> idsAlreadyHandled) {
		return new ArrayList<String>();
	}

}
