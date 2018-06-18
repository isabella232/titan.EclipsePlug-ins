/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titanium.metrics.view;

import org.eclipse.titanium.metrics.MetricData;
import org.eclipse.titanium.metrics.utils.RiskLevel;

/**
 * A node, which is not a root node of any tree, meaning it is part of a
 * metric-subtree (so it is sensible to query the risk corresponding this node).
 *
 * @author poroszd
 *
 */
interface IContentNode extends INode {
	RiskLevel getRiskLevel(MetricData data);

	double risk(MetricData data);
}
