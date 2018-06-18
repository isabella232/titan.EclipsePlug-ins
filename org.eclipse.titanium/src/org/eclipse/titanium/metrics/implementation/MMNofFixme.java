/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titanium.metrics.implementation;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titanium.metrics.MetricData;
import org.eclipse.titanium.metrics.ModuleMetric;

public class MMNofFixme extends BaseModuleMetric {
	public MMNofFixme() {
		super(ModuleMetric.NOF_FIXME);
	}

	@Override
	public Number measure(final MetricData data, final Module module) {
		final IResource res = module.getLocation().getFile();
		int count = 0;
		try {
			count = res.findMarkers(IMarker.TASK, true, IResource.DEPTH_ZERO).length;
		} catch (final CoreException e) {
			ErrorReporter.logExceptionStackTrace("Error while counting markers of " + res.getName(), e);
		}
		return count;
	}
}
