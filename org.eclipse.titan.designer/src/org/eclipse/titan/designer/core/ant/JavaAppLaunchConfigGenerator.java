/******************************************************************************
 * Copyright (c) 2000-2021 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.core.ant;

import java.io.File;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.titan.common.utils.FileUtils;
import org.eclipse.titan.designer.GeneralConstants;
import org.eclipse.titan.designer.compiler.ProjectSourceCompiler;

/**
 * Utility class for generating temporary Java app launch configuration.
 * @author Adam Knapp
 */
public final class JavaAppLaunchConfigGenerator {

	private static final String TEMPORARY_LAUNCH_CONFIGURATION_NAME = "tempJavaAppLaunchConfig";

	/**
	 * Creates a temporary Java app launch configuration.
	 * @param project Project to which the temporary Java app launch configuration is related to
	 * @return The temporary Java app launch configuration. It returns {@code null}, if the specified project is {@code null}.
	 * @throws CoreException
	 */
	public static ILaunchConfiguration createTemporaryJavaAppLaunchConfiguration(final IProject project) throws CoreException {
		if (project == null) {
			return null;
		}
		ILaunchConfiguration tempConfig = null;
		ILaunchConfigurationWorkingCopy wc = null;
		final ILaunchConfigurationType type = getJavaAppLaunchConfigurationType();
		final ILaunchConfiguration[] configs = getLaunchManager().getLaunchConfigurations(type);
		for (ILaunchConfiguration config : configs) {
			if (config.getName().equals(TEMPORARY_LAUNCH_CONFIGURATION_NAME)) {
				tempConfig = config;
				break;
			}
		}
		if (tempConfig == null) {
			IFolder tempFolder = project.getFolder(new Path(GeneralConstants.JAVA_TEMP_DIR));
			FileUtils.createDir(tempFolder);
			wc = getJavaAppLaunchConfigurationType().newInstance(tempFolder, 
					getLaunchManager().generateLaunchConfigurationName(TEMPORARY_LAUNCH_CONFIGURATION_NAME));
		} else {
			wc = tempConfig.getWorkingCopy();
		}

		final String mainType = ProjectSourceCompiler.getPackageGeneratedRoot(project) + ".Parallel_main";
		wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, mainType);
		wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, project.getName());
		final String mainFilePath = ProjectSourceCompiler.getGeneratedRoot(project) + File.separator + "Parallel_main";
		final IFile mainFile = project.getFile(new Path(mainFilePath));
		wc.setMappedResources(new IResource[] {mainFile});

		tempConfig = wc.doSave();
		return tempConfig;
	}

	/**
	 * Deletes the temporary Java app launch configuration.
	 * @param project Project to which the temporary Java app launch configuration is related to
	 * @throws CoreException
	 */
	public static void deleteTemporaryJavaAppLaunchConfiguration(final IProject project) throws CoreException {
		if (project == null) {
			return;
		}
		IFile tempConfigFile = project.getFile(new Path(GeneralConstants.JAVA_TEMP_DIR + File.separator 
				+ TEMPORARY_LAUNCH_CONFIGURATION_NAME + ".launch"));
		if (tempConfigFile.exists()) {
			tempConfigFile.delete(true, null);
		}
	}

	/**
	 * Searches for a launch configuration of Java application type that is related to the specified project.
	 * The launch configuration is used to get the class paths. 
	 * @param project Project where the launch configuration is looked for
	 * @return The launch configuration of Java application type
	 * @throws CoreException
	 */
	public static ILaunchConfiguration findLaunchConfiguration(final IProject project) throws CoreException {
		if (project == null) {
			return null;
		}
		final ILaunchConfigurationType type = getJavaAppLaunchConfigurationType();
		final ILaunchConfiguration[] configs = getLaunchManager().getLaunchConfigurations(type);
		for (ILaunchConfiguration config : configs) {
			if (project.getName().equals(config.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, ""))) {
				return config;
			}
		}
		return createTemporaryJavaAppLaunchConfiguration(project);
	}

	/**
	 * Returns the launch configuration type extension for Java app launch configurations.
	 * @return The launch configuration type extension for Java app launch configurations
	 * @see org.eclipse.debug.core.ILaunchManager#getLaunchConfigurationType
	 */
	public static ILaunchConfigurationType getJavaAppLaunchConfigurationType() {
		return getLaunchManager().getLaunchConfigurationType(IJavaLaunchConfigurationConstants.ID_JAVA_APPLICATION);
	}

	/**
	 * Returns the singleton launch manager.
	 * @return launch manager
	 */
	private static ILaunchManager getLaunchManager() {
		return DebugPlugin.getDefault().getLaunchManager();
	}
}
