/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titanium.metrics.implementation;

import org.eclipse.titanium.metrics.IMetric;
import org.eclipse.titanium.metrics.IMetricEnum;
import org.eclipse.titanium.metrics.MetricData;

/**
 * A base implementation of {@link IMetric}.
 *
 * @author poroszd
 *
 */
abstract class BaseMetric<ENTITY, METRIC extends IMetricEnum> implements IMetric<ENTITY, METRIC> {
	private final METRIC metric;

	BaseMetric(final METRIC metric) {
		this.metric = metric;
	}

	@Override
	public final METRIC getMetric() {
		return metric;
	}

	@Override
	public void init(final MetricData data) {
		//intentionally left empty
	}
}
