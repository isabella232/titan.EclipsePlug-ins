/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titanium.metrics.risk;

import org.eclipse.titanium.metrics.utils.RiskLevel;

/**
 * Classifier for risk.
 * <p>
 * Implementations are responsible for telling what does a value measured by a
 * metric means for the project quality.
 *
 * @author poroszd
 */
public interface IRisk {
	/**
	 * The risk on a [0.0 - 3.0) scale.
	 *
	 * @param value
	 *            the value measured by a metric
	 *
	 * @return the normalized risk
	 */
	double getRiskValue(final Number value);

	/**
	 * The discrete risk.
	 *
	 * @param value
	 *            the value measured by a metric
	 *
	 * @return the discretized risk
	 */
	RiskLevel getRiskLevel(final Number value);
}
