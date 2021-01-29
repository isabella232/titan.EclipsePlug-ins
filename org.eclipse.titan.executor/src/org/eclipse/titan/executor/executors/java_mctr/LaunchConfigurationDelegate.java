/******************************************************************************
 * Copyright (c) 2000-2021 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.executor.executors.java_mctr;

import static org.eclipse.titan.executor.GeneralConstants.SINGLEMODEJAVAEXECUTOR;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.titan.executor.Activator;
import org.eclipse.titan.executor.executors.LaunchConfigurationUtil;
import org.eclipse.titan.executor.executors.TitanLaunchConfigurationDelegate;

/**
 * @author Kristof Szabados
 * @author Arpad Lovassy
 */
public final class LaunchConfigurationDelegate extends TitanLaunchConfigurationDelegate {

	private static final String MISSING_LINKED_LAUNCH_CONFIG = "Unable to locate the launch configuration for single mode";
	
	@Override
	public void launch(final ILaunchConfiguration arg0, final String arg1, final ILaunch arg2, final IProgressMonitor arg3) throws CoreException {
		showExecutionPerspective();
		final boolean singleMode = arg0.getAttribute(SINGLEMODEJAVAEXECUTOR, false);
		if (singleMode) {
			List<String> list = LaunchConfigurationUtil.getLinkedLaunchConfigurations(arg0);
			if (list == null || list.size() != 1) {
				final IStatus status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, IStatus.OK, MISSING_LINKED_LAUNCH_CONFIG, null);
				throw new CoreException(status);
			}
			ILaunchConfiguration singleConfig = LaunchConfigurationUtil.findJavaAppLaunchConfigurationByName(list.get(0));
			if (singleConfig == null) {
				final IStatus status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, IStatus.OK, MISSING_LINKED_LAUNCH_CONFIG, null);
				throw new CoreException(status);
			}
			singleConfig.launch(arg1, null);
			return;
		}
		final NativeJavaExecutor executor = new NativeJavaExecutor(arg0);
		executor.startSession(arg2);
	}

}
