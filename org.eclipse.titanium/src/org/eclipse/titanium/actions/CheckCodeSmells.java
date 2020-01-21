/******************************************************************************
 * Copyright (c) 2000-2019 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titanium.actions;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.titanium.Activator;
import org.eclipse.titanium.markers.handler.MarkerHandler;
import org.eclipse.titanium.markers.utils.Analyzer;
import org.eclipse.titanium.markers.utils.AnalyzerCache;
import org.eclipse.titanium.preferences.PreferenceConstants;
import org.eclipse.titanium.utils.ProjectAnalyzerJob;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

/**
 * Action delegate of code smell searching.
 * <p>
 * This action works on the current structured selections, most notably when the
 * active view is the Project Explorer.
 * <p>
 * If a project is selected, an analyzer job is scheduled for the project as a
 * whole, while for single files, only those files are analyzed.
 *
 * @author poroszd
 * @author Kristof Szabados
 *
 */
public class CheckCodeSmells extends AbstractHandler {
	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		final IWorkbenchPage iwPage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		final ISelection selection = iwPage.getSelection();

		final List<IResource> res = org.eclipse.titan.common.utils.SelectionUtils.getResourcesFromSelection(selection);

		final List<IProject> projects = new ArrayList<IProject>();
		collectResourcesToBeAnalyzed(new LinkedList<IResource>(res), projects);

		final String titaniumId = Activator.PLUGIN_ID;
		final String onTheFlyPref = PreferenceConstants.ON_THE_FLY_SMELLS;
		final boolean onTheFlyEnabled = Platform.getPreferencesService().getBoolean(titaniumId, onTheFlyPref, false, null);
		final Analyzer analyzer = AnalyzerCache.withPreference();
		// check projects
		checkProjects(projects, onTheFlyEnabled, analyzer);

		return null;
	}

	/**
	 * @param res the resources that have to be checked for potential files and projects
	 * @param projects the projects found for analyzes
	 */
	private void collectResourcesToBeAnalyzed(final Deque<IResource> res, final List<IProject> projects) {
		while (!res.isEmpty()) {
			final IResource resource = res.pollFirst();
			if (resource instanceof IProject) {
				final IProject project = (IProject) resource;
				projects.add(project);
			}
		}
	}

	/**
	 * @param projects the projects to be analyzed
	 * @param onTheFlyEnabled whether on-the-fly analysis is enabled or not
	 * @param analyzer the analyzer to be used to analyze the projects
	 */
	private void checkProjects(final List<IProject> projects, final boolean onTheFlyEnabled, final Analyzer analyzer) {
		for (final IProject project : projects) {

			new ProjectAnalyzerJob("Check " + project.getName() + " for code smells") {
				@Override
				public IStatus doPostWork(final IProgressMonitor monitor) {
					final SubMonitor progress = SubMonitor.convert(monitor, 100);
					if (!onTheFlyEnabled) {
						MarkerHandler handler;
						synchronized (project) {
							handler = analyzer.analyzeProject(progress.newChild(100), project);
						}
						handler.showAll(project);
					}
					return Status.OK_STATUS;
				}
			}.quickSchedule(project);
		}
	}
}
