/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titanium.applications;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.Activator;

public class ClearAllProjects implements IApplication {

	@Override
	public Object start(final IApplicationContext context) throws Exception {
		Platform.getBundle("org.eclipse.titan.designer").start();

		final String[] projectFiles = (String[]) context.getArguments().get(IApplicationContext.APPLICATION_ARGS);

		if (projectFiles != null && projectFiles.length != 0) {
			System.out.println("This application does not have parameters.");
			System.out.println("number of parameters: " + projectFiles.length);

			try {
				ResourcesPlugin.getWorkspace().save(true, null);
			} catch (CoreException e) {
				ErrorReporter.logExceptionStackTrace("Error while closing workspace",e);
			}

			return Integer.valueOf(-1);
		}

		if (Activator.getDefault() != null) {
			Activator.getDefault().pauseHandlingResourceChanges();
		}
		boolean result = true;

		final IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		for (final IProject project : projects) {
			try {
				if (project.isLinked()) {
					project.delete(false, false, null);
				} else {
					project.delete(true, true, null);
				}
			} catch (CoreException e) {
				ErrorReporter.logExceptionStackTrace("Error while deleting project " + project.getName(), e);
				result = false;
			}
		}

		try {
			ResourcesPlugin.getWorkspace().save(true, null);
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace("Error while closing workspace",e);
			result = false;
		}

		if (result) {
			return EXIT_OK;
		}

		return Integer.valueOf(-1);
	}

	@Override
	public void stop() {
		// nothing to do
	}
}
