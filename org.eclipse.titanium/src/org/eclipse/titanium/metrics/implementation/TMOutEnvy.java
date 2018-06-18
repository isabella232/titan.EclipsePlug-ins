/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titanium.metrics.implementation;

import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Testcase;
import org.eclipse.titanium.metrics.MetricData;
import org.eclipse.titanium.metrics.TestcaseMetric;
import org.eclipse.titanium.metrics.visitors.Counter;
import org.eclipse.titanium.metrics.visitors.ExternalFeatureEnvyDetector;

public class TMOutEnvy extends BaseTestcaseMetric {
	public TMOutEnvy() {
		super(TestcaseMetric.OUT_ENVY);
	}

	@Override
	public Number measure(final MetricData data, final Def_Testcase testcase) {
		final Counter count = new Counter(0);
		final Module myModule = testcase.getMyScope().getModuleScope();
		testcase.accept(new ExternalFeatureEnvyDetector(myModule, count));
		return count.val();
	}
}
