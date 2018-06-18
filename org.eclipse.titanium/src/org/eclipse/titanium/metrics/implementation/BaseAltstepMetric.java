/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titanium.metrics.implementation;

import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Altstep;
import org.eclipse.titanium.metrics.AltstepMetric;

/**
 * Specific interface for metrics working on altsteps.
 *
 * @author poroszd
 *
 */
abstract class BaseAltstepMetric extends BaseMetric<Def_Altstep, AltstepMetric> {
	BaseAltstepMetric(final AltstepMetric metric) {
		super(metric);
	}
}
