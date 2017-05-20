/******************************************************************************
 * Copyright (c) 2000-2017 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.graph.utils;

import java.util.Collection;
import java.util.List;


import edu.uci.ics.jung.graph.Graph;

/**
 * This class checks for dependent nodes in a Jung graph.
 * 
 * @author Bianka Bekefi
 * @param <V>
 *            node type
 * @param <E>
 *            edge type
 */
public class CheckDependentNodes<V, E> {
	protected Graph<V, E> graph;
	
	public CheckDependentNodes(final Graph<V, E> graph) {
		this.graph = graph;
	}
	
	public boolean isDependent(final V node, final List<V> neighbors) {
		Collection<E> inEdges = graph.getInEdges(node);
		for (E edge : inEdges) {
			V source = graph.getSource(edge);
			if (!neighbors.contains(source)) {
				return false;
			}
		}
		return true;
	}
	
}
