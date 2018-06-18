/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titanium.graph.gui.layouts.algorithms;

import java.util.Collection;

import edu.uci.ics.jung.graph.Graph;

/**
 * This class runs the DAG Layout algorithm on the reverse graph to get a
 * different layout.
 *
 * @author Gobor Daniel
 * @param <V>
 *            node type
 * @param <E>
 *            edge type
 */
public class DAGLayoutReverseAlgorithm<V, E> extends
		DAGLayoutAlgorithm<V, E> {
	public static final String ALG_ID = "TRDAG";

	/**
	 * Initialize the variables.
	 *
	 * @param graph
	 *            The graph whose layout we want to construct
	 */
	public DAGLayoutReverseAlgorithm(final Graph<V, E> graph) {
		super(graph);
	}

	@Override
	protected int getInDegree(final V v) {
		return super.getOutDegree(v);
	}

	@Override
	protected int getOutDegree(final V v) {
		return super.getInDegree(v);
	}

	@Override
	protected Collection<E> getInEdges(final V v) {
		return super.getOutEdges(v);
	}

	@Override
	protected Collection<E> getOutEdges(final V v) {
		return super.getInEdges(v);
	}

	@Override
	protected V getSource(final E e) {
		return super.getDest(e);
	}

	@Override
	protected V getDest(final E e) {
		return super.getSource(e);
	}

}
