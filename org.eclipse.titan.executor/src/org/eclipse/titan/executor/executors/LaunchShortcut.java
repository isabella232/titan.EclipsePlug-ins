/******************************************************************************
 * Copyright (c) 2000-2021 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.executor.executors;

import static org.eclipse.titan.executor.GeneralConstants.EXECUTECONFIGFILEONLAUNCH;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.common.path.PathUtil;
import org.eclipse.titan.designer.properties.data.ProjectFileHandler;
import org.eclipse.titan.executor.tabpages.hostcontrollers.HostControllersTab;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

/**
 * This class lets the user create a launch configuration via a shortcut.
 * By selecting a project and the proper type of launch configuration in the runs on menu, a temporal launch configuration is created.
 * If this is a new launch configuration it is saved with a temporal name, if something with the same selection input can be found it is reused.
 *
 * @author Kristof Szabados
 * @author Adam Knapp
 * */
public abstract class LaunchShortcut implements ILaunchShortcut {
	
	/**
	 * Returns the configuration ID, e.g. 
	 * org.eclipse.titan.executor.executors.single.LaunchConfigurationDelegate
	 * @return Configuration ID
	 */
	protected abstract String getConfigurationId();
	
	/**
	 * Returns the title string for the dialog 
	 * @return Title of dialog
	 */
	protected abstract String getDialogTitle();
	
	/**
	 * Returns the launch configuration type:
	 * Single, Parallel, Parallel-JNI, MC-Java
	 * @return Type of launch configuration
	 */
	protected abstract String getLaunchConfigurationType();

	/**
	 * Initializes the provided launch configuration for execution.
	 *
	 * @param configuration the configuration to initialize.
	 * @param project the project to gain data from.
	 * @param configFilePath the path of the configuration file.
	 * */
	public abstract boolean initLaunchConfiguration(final ILaunchConfigurationWorkingCopy configuration,
			final IProject project, final String configFilePath);

	/**
	 * Creates a working copy of the launch configuration from the available information,
	 * that can be used to initialize the pages of the launch configuration.
	 *
	 *  @param project the project to use.
	 *  @param file the file selected by the user.
	 *  @param mode one of the launch modes defined by the launch manager
	 * */
	protected ILaunchConfigurationWorkingCopy getWorkingCopy(final IProject project, final IFile file, final String mode) {

		try {
			final ILaunchConfigurationType configurationType = LaunchConfigurationUtil.getLaunchManager().getLaunchConfigurationType(getConfigurationId());
			final ILaunchConfiguration[] configurations = LaunchConfigurationUtil.getLaunchManager().getLaunchConfigurations(configurationType);

			final List<ILaunchConfiguration> candidateConfigurations = new ArrayList<ILaunchConfiguration>();
			for (final ILaunchConfiguration configuration : configurations) {
				final IResource[] resources = configuration.getMappedResources();
				if (resources == null || resources.length < 2) {
					continue;
				}
				boolean found = false;
				for (final IResource resource : resources) {
					if (resource.equals(project)) {
						found = true;
						break;
					}
				}
				if (found) {
					found = false;
					for (final IResource resource : resources) {
						if (file.equals(resource)) {
							found = true;
							break;
						}
					}
				}
				if (found) {
					candidateConfigurations.add(configuration);
				}
			}

			if (1 == candidateConfigurations.size()) {
				performLaunch(candidateConfigurations.get(0), mode);
				return null;
			} else if (candidateConfigurations.size() > 1) {
				final ILabelProvider labelProvider = DebugUITools.newDebugModelPresentation();
				final ElementListSelectionDialog dialog = new ElementListSelectionDialog(null, labelProvider);
				dialog.setTitle(getDialogTitle());
				dialog.setMessage("Select existing configuration:");
				dialog.setElements(candidateConfigurations.toArray(new ILaunchConfiguration[candidateConfigurations.size()]));
				if (dialog.open() == Window.OK) {
					final ILaunchConfiguration result = (ILaunchConfiguration) dialog.getFirstResult();
					performLaunch(result, mode);
					labelProvider.dispose();
					return null;
				}

				labelProvider.dispose();
			}
			// size() == 0 case: create new configuration
			final String configurationName = file.getFullPath().toString().substring(1).replace("/", "-") 
					+ "-" + getLaunchConfigurationType();
			ILaunchConfiguration config = LaunchConfigurationUtil.findLaunchConfigurationByName(configurationName, configurationType);
			ILaunchConfigurationWorkingCopy wc = null;
			if (config != null) {
				wc = config.getWorkingCopy();
			} else {
				wc = configurationType.newInstance(null, 
						LaunchConfigurationUtil.getLaunchManager().generateLaunchConfigurationName(configurationName));
			}
			wc.setMappedResources(new IResource[] {project, file});
			wc.setAttribute(EXECUTECONFIGFILEONLAUNCH, true);

			return wc;
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace(e);
			return null;
		}
	}
	
	protected void performLaunch(ILaunchConfiguration configuration, final String mode) throws CoreException {
		if (configuration.isWorkingCopy()) {
			configuration = ((ILaunchConfigurationWorkingCopy)configuration).doSave();
		}
		configuration.launch(mode, null);
	}

	@Override
	/** {@inheritDoc} */
	public final void launch(final IEditorPart editor, final String mode) {
		// Execution from editors is not supported
		ErrorReporter.INTERNAL_ERROR("LaunchShortcut.launch called from an editor even though it is no registered to support such calls.");
	}

	@Override
	/** {@inheritDoc} */
	public final void launch(final ISelection selection, final String mode) {
		if (!(selection instanceof IStructuredSelection)) {
			return;
		}

		final Object[] selections = ((IStructuredSelection) selection).toArray();
		if (1 != selections.length) {
			return;
		}

		IFile cfgFile = null;
		IProject project = null;

		if ((selections[0] instanceof IProject)) {
			// try to find the cfg file:
			project = (IProject) selections[0];
			final List<IFile> cfgFiles = ProjectFileHandler.getCfgFiles(project);
			if (cfgFiles.size() == 1) {
				cfgFile = cfgFiles.get(0);
			} else if (cfgFiles.size() > 1) {
				final ILabelProvider labelProvider = DebugUITools.newDebugModelPresentation();
				final ElementListSelectionDialog dialog = new ElementListSelectionDialog(null, labelProvider);
				dialog.setTitle("Config File Selection");
				dialog.setMessage("Select existing cfg file:");
				dialog.setElements(cfgFiles.toArray(new IFile[cfgFiles.size()]));
				if (dialog.open() == Window.OK) {
					cfgFile = (IFile) dialog.getFirstResult();
				}
			} else {
				ErrorReporter.logError("Config file not found");
				ErrorReporter.parallelErrorDisplayInMessageDialog(
						"Error while creating the default launch configuration for project " + project.getName(),
						"Config file not found in project " + project.getName());
				return;
			}
		} else {
			return;
		}

		if (project == null || cfgFile == null) {
			return;
		}

		final ILaunchConfigurationWorkingCopy wc = getWorkingCopy(project, cfgFile, mode);
		if (wc == null) {
			return; // successful launch happened
		}

		boolean result = initLaunchConfiguration(wc, project, 
				PathUtil.getRelativePath(project.getLocation().toOSString(), cfgFile.getLocation().toOSString()));
		if (result) {
			result = HostControllersTab.initLaunchConfiguration(wc);
		}
		try {
			if (result) {
				wc.setMappedResources(new IResource[] {project, cfgFile});
				wc.setAttribute(EXECUTECONFIGFILEONLAUNCH, true);
				performLaunch(wc, mode);
			}
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace(e);
		}
	}
}
