/******************************************************************************
 * Copyright (c) 2000-2021 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.executor.executors.jni;

import org.eclipse.core.resources.IProject;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.titan.executor.tabpages.maincontroller.JNIMainControllerTab;
import org.eclipse.titan.executor.views.executormonitor.ExecutorMonitorView;

/**
 * @author Kristof Szabados
 * */
public final class LaunchShortcutConfig extends org.eclipse.titan.executor.executors.LaunchShortcutConfig {
	@Override
	/** {@inheritDoc} */
	protected String getConfigurationId() {
		return ExecutorMonitorView.JNI_MODE_LAUNCHCONFIGURATION_ID;
	}

	@Override
	/** {@inheritDoc} */
	protected String getDialogTitle() {
		return "Select jni mode execution";
	}
	
	@Override
	/** {@inheritDoc} */
	protected String getLaunchConfigurationType() {
		return "Parallel-JNI";
	}

	@Override
	/** {@inheritDoc} */
	public boolean initLaunchConfiguration(final ILaunchConfigurationWorkingCopy configuration, final IProject project, final String configFilePath) {
		return JNIMainControllerTab.initLaunchConfiguration(configuration, project, configFilePath);
	}
}
