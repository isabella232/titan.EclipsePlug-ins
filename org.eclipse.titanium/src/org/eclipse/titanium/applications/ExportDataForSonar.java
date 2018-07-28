/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titanium.applications;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.GeneralConstants;
import org.eclipse.titanium.utils.ProjectAnalyzerJob;
import org.eclipse.titanium.utils.SonarDataExporter;

public class ExportDataForSonar implements IApplication {

	private List<IProject> projectsToExport = new ArrayList<IProject>();

	private boolean checkParameters(final String[] args) {
		// Use Apache CLI if more functionality is needed
		if (args.length == 0) {
			projectsToExport = getAllAccessibleProjects();
			return true;
		}

		if (args.length != 2) {
			printUsage();
			return false;
		}

		if (!("-p".equals(args[0]) || !"--projects".equals(args[0]))) {
			printUsage();
			return false;
		}

		final List<String> projectNames = Arrays.asList(args[1].split(","));
		final IWorkspaceRoot wsRoot = ResourcesPlugin.getWorkspace().getRoot();
		for (final String name : projectNames) {
			final IProject project = wsRoot.getProject(name);
			if (!project.isAccessible()) {
				System.out.println("Project '" + name + "' is not accessible.");
				return false;
			}
			projectsToExport.add(project);
		}
		return true;
	}

	private void printUsage() {
		final String applicationName = ExportDataForSonar.class.getCanonicalName();
		System.out.println("Usage: ./eclipse " + applicationName + " [-p project1,project2,...,projectN]");
	}

	private void exportInformationForProject(final String[] args, final IProject project, final IProgressMonitor monitor) {
		final SonarDataExporter exporter = new SonarDataExporter(project);
		try {
			exporter.exportDataForProject();
		} catch (IOException e) {
			ErrorReporter.logExceptionStackTrace("Error while exporting data for project " + project.getName(), e);
		}
	}

	private List<IProject> getAllAccessibleProjects() {
		final IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		final List<IProject> existingProjects = new ArrayList<IProject>();
		for (final IProject project : projects) {
			if (project.isAccessible()) {
				existingProjects.add(project);
			}
		}
		return existingProjects;
	}

	@Override
	public Object start(final IApplicationContext context) throws Exception {
		if (!GeneralConstants.DEBUG) {
			ErrorReporter.INTERNAL_ERROR("Experimental functionaility for the Titanium project");
		}

		Platform.getBundle("org.eclipse.titan.designer").start();
		final String[] args = (String[]) context.getArguments().get(IApplicationContext.APPLICATION_ARGS);
		if(!checkParameters(args)) {
			return Integer.valueOf(-1);
		}

		for (final IProject project : projectsToExport) {
			final  ProjectAnalyzerJob job = new ProjectAnalyzerJob("Exporting information for project " + project.getName()) {
				@Override
				public IStatus doPostWork(final IProgressMonitor monitor) {
					System.out.println("Exporting information for " + getProject().getName());
					exportInformationForProject(args, getProject(), monitor);
					return Status.OK_STATUS;
				}
			}.quickSchedule(project);
			job.join();
		}

		boolean result = true;

		try {
			ResourcesPlugin.getWorkspace().save(true, null);
		} catch (Exception e) {
			ErrorReporter.logExceptionStackTrace("Error while closing workspace",e);
			result = false;
		}

		if (result) {
			System.out.println("All information is succesfully exported.");

			return EXIT_OK;
		} else {
			System.err.println("The export wasn't successfull, see zour workspace1s errorlog for details");
			return Integer.valueOf(-1);
		}
	}

	@Override
	public void stop() {
		// nothing to be done
	}
}
