/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titanium.graph.clustering;

import org.eclipse.titanium.graph.components.EdgeDescriptor;
import org.eclipse.titanium.graph.components.NodeDescriptor;

import edu.uci.ics.jung.graph.DirectedSparseGraph;

/**
 * This version of the module name cluster only shows the non empty clustres as nodes in the cluster graph.
 *
 * @author Gobor Daniel
 *
 */
public class SparseModuleNameCluster extends FullModuleNameCluster {

	public SparseModuleNameCluster(final DirectedSparseGraph<NodeDescriptor, EdgeDescriptor> graph) {
		super(graph);
	}

	@Override
	protected void check(final String name) {
		if (!mapNameCluster.get(name).isEmpty()) {
			super.check(name);
		}
	}
}
