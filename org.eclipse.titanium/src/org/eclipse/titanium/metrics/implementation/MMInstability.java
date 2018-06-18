/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titanium.metrics.implementation;

import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titanium.metrics.MetricData;
import org.eclipse.titanium.metrics.ModuleMetric;

public class MMInstability extends BaseModuleMetric {
	public MMInstability() {
		super(ModuleMetric.INSTABILITY);
	}

	@Override
	public Number measure(final MetricData data, final Module module) {
		final int eff = data.get(ModuleMetric.EFFERENT_COUPLING, module).intValue();
		final int aff = data.get(ModuleMetric.AFFERENT_COUPLING, module).intValue();
		double instability;
		if (eff == 0 && aff == 0) {
			instability = 0;
		} else {
			instability = ((double) eff) / (eff + aff);
		}
		return new Double(instability);
	}
}
