/******************************************************************************
 * Copyright (c) 2000-2021 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titanium.applications;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titanium.markers.export.XlsProblemExporter;

/**
 * Prototype application for extracting the contents of the problems view into
 * an excel file in headless mode.
 * It will analyze the project provided as parameter in the
 * workspace, and save the reports for into an excel file with the
 * name <project_name>.xls
 *
 * It awaits one single parameter, the folder to place to excel files into.
 * */
public class ExportAllCodeSmellsForProject extends InformationExporter {
	private String projectName;

	@Override
	protected boolean checkParameters(final String[] args) {
		if (args.length < 2 || args.length > 3) {
			System.out.println("This application takes as parameter the location of the resulting .XLS files "
					+ "the name of the project to be checked "
					+ "and optionally the date to be inserted into the file.");
			return false;
		}

		projectName = args[1];

		return true;
	}

	@Override
	protected void exportInformationForProject(final String[] args, final IProject project, final IProgressMonitor monitor) {
		final XlsProblemExporter exporter = new XlsProblemExporter(project);

		try {
			Date date;
			if (args.length == 2) {
				date = Calendar.getInstance().getTime();
			} else {
				date = new SimpleDateFormat("yyyy_MM_dd").parse(args[2]);
			}
			exporter.exportMarkers(monitor, args[0] + project.getName() + ".xls", date);
		} catch (Exception e) {
			ErrorReporter.logExceptionStackTrace("Error while exporting to excel " + args[0] + project.getName() + ".xls",e);
		}
	}

	@Override
	protected List<IProject> getProjectsToHandle() {
		if (projectName == null) {
			return new ArrayList<IProject>();
		}

		final IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		IProject foundProject = null;
		for (final IProject project : projects) {
			if (projectName.equals(project.getName())) {
				foundProject = project;
			}
		}

		if (foundProject == null) {
			System.out.println("There is no project with name `" + projectName + "' in the workspace.");
			return new ArrayList<IProject>();
		}

		if (!foundProject.isAccessible()) {
			System.out.println("There project `" + projectName + "' is not accessible.");
			return new ArrayList<IProject>();
		}

		final ArrayList<IProject> result = new ArrayList<IProject>();
		result.add(foundProject);
		return result;
	}


}
