/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titanium.graph.gui.layouts.algorithms;

import java.util.Map;
import java.util.Set;

/**
 * This interface provides a base for all hierarchically ordered layout algorithms
 * @author Gabor Jenei
 * @param <V> The node type
 */
public interface HierarcicalLayoutAlgorithm<V> {

	/**
	 * @return A map that tells the level for each node
	 */
	public Map<V, Integer> getLevels();

	/**
	 * @return The number of hierarchical levels
	 */
	public int getNumberOfLevels();

	/**
	 * @return An array that tells how many nodes are there on a certain level
	 */
	public int[] getNumberOfNodesPerLevel();

	/**
	 * @return The set of isolate nodes
	 */
	public Set<V> getIsolateNodes();

	/**
	 * This method runs the actual calculations needed to perform the ordering
	 */
	public void run();
}