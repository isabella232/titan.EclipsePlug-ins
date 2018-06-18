/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titanium.graph.clustering.visualization;

import java.util.Set;

import org.eclipse.titanium.graph.components.NodeColours;
import org.eclipse.titanium.graph.components.NodeDescriptor;

/**
 * This class represents a cluster of the module graph. It is the subclass of
 * {@link NodeDescriptor}
 *
 * @author Gobor Daniel
 */
public class ClusterNode extends NodeDescriptor {

	/**
	 * Constructor
	 *
	 * @param name
	 *            : The cluster's name
	 */
	public ClusterNode(final String name) {
		super(name);
		nodeColour = NodeColours.DARK_GREEN;
	}

	/**
	 * Constructor
	 *
	 * @param name
	 *            : The cluster's name
	 * @param cluster
	 *            : A set of nodes belonging to the represented cluster
	 */
	public ClusterNode(final String name, final Set<NodeDescriptor> cluster) {
		this(name);
		setCluster(cluster);
	}
}
