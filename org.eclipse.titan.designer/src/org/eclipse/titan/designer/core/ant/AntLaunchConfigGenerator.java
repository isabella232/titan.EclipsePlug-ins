/******************************************************************************
 * Copyright (c) 2000-2021 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.core.ant;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.ant.launching.IAntLaunchConstants;
import org.eclipse.core.externaltools.internal.IExternalToolConstants;
import org.eclipse.core.externaltools.internal.model.ExternalToolBuilder;
import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.RefreshUtil;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;

/**
 * Utility class for generating Ant launch configuration.
 * @author Adam Knapp
 */
@SuppressWarnings("restriction")
public final class AntLaunchConfigGenerator {
	private static final String ANT_LAUNCH_CONFIGURATION_NAME = "JarBuilder";
	private static final String ANT_LAUNCH_CONFIGURATION_EXTENSION = ".launch";
	private static final String ANT_LAUNCH_CONFIGURATION_FOLDER = ".externalToolBuilders";
	private static final String ANT_BUILDER_ARG1_KEY = "LaunchConfigHandle";
	private static final String ANT_BUILDER_ARG1_VALUE = 
			"<project>/" + ANT_LAUNCH_CONFIGURATION_FOLDER + "/" 
					+ ANT_LAUNCH_CONFIGURATION_NAME + ANT_LAUNCH_CONFIGURATION_EXTENSION; 

	/**
	 * Adds the ANT builder to the project
	 * @param project Project where automated JAR export is required
	 */
	public static void addAntBuilder(final IProject project) throws CoreException {
		final IProjectDescription description = project.getDescription();
		final List<ICommand> commands = new ArrayList<ICommand>(Arrays.asList(description.getBuildSpec()));
		for(ICommand command : commands) {
			if (command.getBuilderName().equals(ExternalToolBuilder.ID)) {
				return;
			}
		}
		final ICommand antCommand = description.newCommand();
		final Map<String, String> args = new HashMap<String, String>(1);
		args.put(ANT_BUILDER_ARG1_KEY, ANT_BUILDER_ARG1_VALUE);
		antCommand.setBuilderName(ExternalToolBuilder.ID);
		antCommand.setBuilding(IncrementalProjectBuilder.CLEAN_BUILD, false);
		antCommand.setArguments(args);
		commands.add(antCommand);
		description.setBuildSpec(commands.toArray(new ICommand[commands.size()]));
		project.setDescription(description, null);
		project.refreshLocal(IResource.DEPTH_INFINITE, null);
	}

	/**
	 * Creates a new or overwrites the existing ANT launch configuration for automated JAR export.
	 * @param project Project where automated JAR export is required
	 * @return The newly created or the overwritten ANT launch configuration. It returns {@code null}, 
	 * if the project is {@code null} or not properly set up.
	 * @throws CoreException
	 */
	public static ILaunchConfiguration createAntLaunchConfiguration(final IProject project) throws CoreException {
		if (project == null) {
			return null;
		}
		if (existAntLaunchConfiguration(project)) {
			return getAntLaunchConfiguration(project);
		}
		final IFolder antLaunchConfigFolder = project.getFolder(new Path(ANT_LAUNCH_CONFIGURATION_FOLDER));
		if (!antLaunchConfigFolder.exists()) {
			antLaunchConfigFolder.create(false, true, null);
		}
		ILaunchConfigurationWorkingCopy wc = getAntLaunchConfigurationType().newInstance(antLaunchConfigFolder, ANT_LAUNCH_CONFIGURATION_NAME);
		wc.setAttribute(IAntLaunchConstants.ATTR_ANT_MANUAL_TARGETS, "jar,");
		wc.setAttribute(IAntLaunchConstants.ATTR_TARGETS_UPDATED, true);
		wc.setAttribute(IAntLaunchConstants.ATTR_DEFAULT_VM_INSTALL, false);
		wc.setAttribute(RefreshUtil.ATTR_REFRESH_SCOPE, "${project}");
		wc.setAttribute(IDebugUIConstants.ATTR_LAUNCH_IN_BACKGROUND, false);
		wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_CLASSPATH_PROVIDER, "org.eclipse.ant.ui.AntClasspathProvider");
		wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_DEFAULT_CLASSPATH, true);
		wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, project.getName());
		wc.setAttribute(IExternalToolConstants.ATTR_BUILDER_ENABLED, true);
		final String location = "${workspace_loc:/" + project.getName() + "/" + AntScriptGenerator.BUILD_XML_NAME + "}";
		wc.setAttribute(IExternalToolConstants.ATTR_LOCATION, location);
		wc.setAttribute(IExternalToolConstants.ATTR_RUN_BUILD_KINDS, "incremental");
		wc.setAttribute(IExternalToolConstants.ATTR_TRIGGERS_CONFIGURED, true);
		wc.setAttribute(IExternalToolConstants.ATTR_WORKING_DIRECTORY, "${workspace_loc:/" + project.getName() + "}");
		return wc.doSave();
	}

	/**
	 * Checks whether the ANT launch configuration is already exist
	 * @param project Project where automated JAR export is required
	 * @return Return whether the ANT launch configuration is already exist
	 */
	public static boolean existAntLaunchConfiguration(final IProject project) {
		if (project == null) {
			return false;
		}
		final IFile antLaunchConfigFile = project.getFile(
				new Path(ANT_LAUNCH_CONFIGURATION_FOLDER + File.separator 
						+ ANT_LAUNCH_CONFIGURATION_NAME + ANT_LAUNCH_CONFIGURATION_EXTENSION));
		return antLaunchConfigFile.exists();
	}

	/**
	 * Looks for the ANT launch configuration in the specified project.
	 * @param project Project with ANT launch configuration 
	 * @return Launch configuration. It returns {@code null} if the {@code project}
	 * is {@code null} or no proper launch configuration was found in the project.
	 * @throws CoreException
	 */
	public static ILaunchConfiguration getAntLaunchConfiguration(final IProject project) throws CoreException {
		if (project == null) {
			return null;
		}
		final IFile antLaunchConfigFile = project.getFile(
				new Path(ANT_LAUNCH_CONFIGURATION_FOLDER + File.separator 
						+ ANT_LAUNCH_CONFIGURATION_NAME + ANT_LAUNCH_CONFIGURATION_EXTENSION));
		if (antLaunchConfigFile.exists()) {
			return getLaunchManager().getLaunchConfiguration(antLaunchConfigFile);
		}
		return null;
	}

	/**
	 * Returns the launch configuration type extension for ANT launch configurations.
	 * @return The launch configuration type extension for ANT launch configurations
	 * @see org.eclipse.debug.core.ILaunchManager#getLaunchConfigurationType
	 */
	public static ILaunchConfigurationType getAntLaunchConfigurationType() {
		return getLaunchManager().getLaunchConfigurationType(IAntLaunchConstants.ID_ANT_BUILDER_LAUNCH_CONFIGURATION_TYPE);
	}

	/**
	 * Returns the singleton launch manager.
	 * @return launch manager
	 */
	private static ILaunchManager getLaunchManager() {
		return DebugPlugin.getDefault().getLaunchManager();
	}

	/**
	 * Returns whether or not the ANT builder for the specified project is enabled. 
	 * @param project Project with ANT builder
	 * @return The enabled state.
	 */
	public static boolean isAntBuilderEnabled(final IProject project) throws CoreException {
		if (project == null) {
			return false;
		}
		final ILaunchConfiguration config = getAntLaunchConfiguration(project);
		if (config == null) {
			return false;
		}
		return config.getAttribute(IExternalToolConstants.ATTR_BUILDER_ENABLED, false);
	}

	/**
	 * Set whether or not the ANT builder for the specified project is enabled.
	 * @param project Project with ANT builder
	 * @param enabled The enabled state.
	 */
	public static void setAntBuilderEnabled(final IProject project, final boolean enabled) throws CoreException {
		if (project == null) {
			return;
		}
		final ILaunchConfiguration config = getAntLaunchConfiguration(project);
		if (config == null) {
			return;
		}
		final ILaunchConfigurationWorkingCopy wc = config.getWorkingCopy();
		wc.setAttribute(IExternalToolConstants.ATTR_BUILDER_ENABLED, enabled);
		wc.doSave();
	}
}
