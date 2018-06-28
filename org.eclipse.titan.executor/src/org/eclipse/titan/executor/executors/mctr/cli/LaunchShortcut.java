/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.executor.executors.mctr.cli;

import org.eclipse.core.resources.IProject;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.titan.executor.tabpages.maincontroller.MctrCliMainControllerTab;

/**
 * @author Kristof Szabados
 * */
public final class LaunchShortcut extends org.eclipse.titan.executor.executors.LaunchShortcut {
	@Override
	/** {@inheritDoc} */
	protected String getConfigurationId() {
		return "org.eclipse.titan.executor.executors.mctr.cli.LaunchConfigurationDelegate";
	}

	@Override
	/** {@inheritDoc} */
	protected String getDialogTitle() {
		return "Select (parallel) mctr_cli mode execution configuration";
	}

	@Override
	/** {@inheritDoc} */
	public boolean initLaunchConfiguration(final ILaunchConfigurationWorkingCopy configuration, final IProject project, final String configFilePath) {
		return MctrCliMainControllerTab.initLaunchConfiguration(configuration, project, configFilePath);
	}
}
