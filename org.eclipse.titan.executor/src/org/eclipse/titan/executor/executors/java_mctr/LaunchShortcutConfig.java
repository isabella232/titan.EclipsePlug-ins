/******************************************************************************
 * Copyright (c) 2000-2021 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.executor.executors.java_mctr;

import static org.eclipse.titan.executor.GeneralConstants.SINGLEMODEJAVAEXECUTOR;

import java.util.ArrayList;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.titan.executor.executors.LaunchConfigurationUtil;
import org.eclipse.titan.executor.tabpages.maincontroller.NativeJavaMainControllerTab;
import org.eclipse.titan.executor.views.executormonitor.ExecutorMonitorView;

/**
 * @author Kristof Szabados
 * @author Adam Knapp
 * */
public final class LaunchShortcutConfig extends org.eclipse.titan.executor.executors.LaunchShortcutConfig {
	@Override
	/** {@inheritDoc} */
	protected String getConfigurationId() {
		return ExecutorMonitorView.NATIVE_JAVA_LAUNCHCONFIGURATION_ID;
	}

	@Override
	/** {@inheritDoc} */
	protected String getDialogTitle() {
		return "Select native Java mode execution";
	}

	@Override
	/** {@inheritDoc} */
	protected String getLaunchConfigurationType() {
		return "MC-Java";
	}

	@Override
	/** {@inheritDoc} */
	protected void performLaunch(ILaunchConfiguration configuration, final String mode) throws CoreException {
		ILaunchConfigurationWorkingCopy wc = null;
		if (configuration.isWorkingCopy()) {
			wc = (ILaunchConfigurationWorkingCopy)configuration;
		} else {
			wc = configuration.getWorkingCopy();
		}
		final boolean singleMode = wc.getAttribute(SINGLEMODEJAVAEXECUTOR, false);
		ArrayList<String> list = new ArrayList<String>(2);
		if (singleMode) {
			final ILaunchConfiguration confSingle = LaunchConfigurationUtil.createJavaAppLaunchConfiguration(wc);
			if (confSingle == null) {
				return;
			}
			list.add(confSingle.getName());
			LaunchConfigurationUtil.setLinkedLaunchConfigurations(wc, list);
			configuration = wc.doSave();
			configuration.launch(mode, null);
			return;
		}
		final ILaunchConfiguration confHC = LaunchConfigurationUtil.createJavaAppLaunchConfiguration(wc);
		final ILaunchConfiguration confGroup = LaunchConfigurationUtil.createGroupLaunchConfiguration(wc, confHC);
		if (confHC == null || confGroup == null) {
			return;
		}
		list.add(confHC.getName());
		list.add(confGroup.getName());
		LaunchConfigurationUtil.setLinkedLaunchConfigurations(wc, list);
		configuration = wc.doSave();
		//confGroup.launch(mode, null);
	}

	@Override
	/** {@inheritDoc} */
	public boolean initLaunchConfiguration(final ILaunchConfigurationWorkingCopy configuration, final IProject project, final String configFilePath) {
		return NativeJavaMainControllerTab.initLaunchConfiguration(configuration, project, configFilePath);
	}

}
