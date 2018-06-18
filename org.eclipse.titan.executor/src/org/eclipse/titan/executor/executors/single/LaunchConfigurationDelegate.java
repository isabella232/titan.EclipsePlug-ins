/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.executor.executors.single;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.titan.executor.executors.TitanLaunchConfigurationDelegate;

/**
 * @author Kristof Szabados
 * */
public final class LaunchConfigurationDelegate extends
TitanLaunchConfigurationDelegate {

	@Override
	public void launch(final ILaunchConfiguration arg0, final String arg1, final ILaunch arg2,
			final IProgressMonitor arg3) throws CoreException {
		showExecutionPerspective();
		SingleExecutor executor = new SingleExecutor(arg0);
		executor.startSession(arg2);
	}
}
