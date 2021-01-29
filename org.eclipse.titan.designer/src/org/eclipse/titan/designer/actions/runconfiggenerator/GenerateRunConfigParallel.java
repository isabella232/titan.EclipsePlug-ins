/******************************************************************************
 * Copyright (c) 2000-2021 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.actions.runconfiggenerator;

import static org.eclipse.titan.common.utils.StringUtils.isNullOrEmpty;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.text.MessageFormat;

import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.common.parsers.cfg.ConfigFileHandler;
import org.eclipse.titan.common.utils.FileUtils;
import org.eclipse.titan.common.utils.IOUtils;
import org.eclipse.titan.designer.consoles.TITANConsole;
import org.eclipse.titan.designer.core.TITANBuilder;
import org.eclipse.titan.designer.core.TITANJavaBuilder;

/**
 * Executed by "Generate parallel run configuration for Eclipse" command/menu item.
 * It handles the generation of 3 Eclipse based run configuration files:
 * <li> Run config for the host controller
 * <li> Run config for the main controller
 * <li> Run config for launch group that invokes the previous two
 * 
 * @author Adam Knapp
 *
 */
public class GenerateRunConfigParallel extends GenerateRunConfigBase {
	
	public GenerateRunConfigParallel() {
	}

	@Override
	/** {@inheritDoc} */
	protected void generateRunConfig(final IFile file) throws CoreException {
		if (TITANBuilder.isBuilderEnabled(file.getProject())) {
			generateRunConfigParallelTitan(file);
		} else if (TITANJavaBuilder.isBuilderEnabled(file.getProject())) {
			generateRunConfigParallelTitanJava(file);
		}
		
		return;
	}
	
	/**
	 * Generates the parallel mode run configuration files for Eclipse.
	 * It simply fills the project name, the Titan config file name and
	 * the host + port in the template launch configurations.
	 * 
	 * @param file
	 * @throws CoreException
	 */
	private void generateRunConfigParallelTitan(final IFile file) throws CoreException {
		ErrorReporter.parallelErrorDisplayInMessageDialog(
				"Error while generating the default launch configuration for project "
						+ file.getProject().getName(),
						"This feature is only available for Titan Java projects!");
	}

	/**
	 * Generates the parallel mode run configuration files for Eclipse.
	 * It simply fills the project name, the Titan config file name and
	 * the host + port in the template launch configurations.
	 * 
	 * @param file
	 * @throws CoreException
	 */
	private void generateRunConfigParallelTitanJava(final IFile file) throws CoreException {
		if (!validateFile(file)) {
			return;
		}
		
		final String projectName = file.getProject().getName();
		final String projectLocation = file.getProject().getLocation().toOSString();
		final String configFileName = file.getLocation().toOSString();
		
		final ConfigFileHandler configHandler = new ConfigFileHandler();
		configHandler.readFromFile(configFileName);
		
		String localAddress = configHandler.getLocalAddress();
		if (isNullOrEmpty(localAddress)) {
			localAddress = "127.0.0.1";
			ErrorReporter.parallelWarningDisplayInMessageDialog(
					"Error while generating the default launch configuration for project " + projectName, 
					"\"LocalAddress\" parameter is missing from configuration file: " + file.getFullPath().toOSString() +
					"\nUsing default: " + localAddress);
			TITANConsole.println("\"LocalAddress\" parameter is missing from configuration file: " + file.getFullPath().toOSString());
			TITANConsole.println("Using default: " + localAddress);
			
		}
		
		final int tcpPort = configHandler.getTcpPort();
		if (tcpPort == 0) {
			ErrorReporter.logError("\"TCPPort\" parameter is missing from configuration file: " 
					+ file.getFullPath().toOSString());
			ErrorReporter.parallelErrorDisplayInMessageDialog(
					"Error while generating the default launch configuration for project " + projectName, 
					"\"TCPPort\" parameter is missing from configuration file: " + file.getFullPath().toOSString() +
					"\nCannot continue");
			TITANConsole.println("\"TCPPort\" parameter is missing from configuration file: " 
					+ file.getFullPath().toOSString());
			TITANConsole.println("Cannot continue");
			return;
		}
		
		String filenamePrefix = projectName;
		boolean overwrite = false;
		final FileNameDialog dialog = new FileNameDialog(new Shell());
		dialog.setDefaultFileName(filenamePrefix);
		dialog.setMode(false);
		dialog.create();
		if (dialog.open() == Window.OK) {
			filenamePrefix = dialog.getFirstName();
			overwrite = dialog.getOverwrite();
		} else {
			return;
		}
		
		FileUtils.createDir(file.getProject().getFolder(RUN_CONFIG_FOLDER));
		final String parallelRunConfigShortFilename = filenamePrefix + "-HC";
		final URI parallelRunConfigFilename = URIUtil.toURI(projectLocation + File.separator + 
				RUN_CONFIG_FOLDER + File.separator + parallelRunConfigShortFilename + ".launch");
		final String titanRunConfigShortFilename = filenamePrefix + "-MC";
		final URI titanRunConfigFilename = URIUtil.toURI(projectLocation + File.separator + 
				RUN_CONFIG_FOLDER + File.separator + titanRunConfigShortFilename + ".launch");
		final URI launchGroupConfigFilename = URIUtil.toURI(projectLocation + File.separator + 
				RUN_CONFIG_FOLDER + File.separator + filenamePrefix + "-Parallel.launch");
		
		final URI configFileNameUri = URIUtil.toURI(file.getProject().getLocation()).relativize(URIUtil.toURI(file.getLocation()));
		final String parallelRunConfigContent = 
				MessageFormat.format(PARALLEL_RUNCONFIG_TEMPLATE, projectName, localAddress, tcpPort);
		final String titanRunConfigContent = 
				MessageFormat.format(TITAN_RUNCONFIG_TEMPLATE, projectName, configFileNameUri.getPath());
		final String launchGroupConfigContent = 
				MessageFormat.format(LAUNCH_GROUP_CONFIG_TEMPLATE, titanRunConfigShortFilename, parallelRunConfigShortFilename);
		
		try {
			File parallelRunConfigFile = new File(parallelRunConfigFilename);
			File titanRunConfigFile = new File(titanRunConfigFilename);
			File launchGroupConfigFile = new File(launchGroupConfigFilename);
			
			if (overwrite) {
				FileUtils.deleteQuietly(parallelRunConfigFile);
				FileUtils.deleteQuietly(titanRunConfigFile);
				FileUtils.deleteQuietly(launchGroupConfigFile);
			}
			
			if (parallelRunConfigFile.createNewFile() && titanRunConfigFile.createNewFile() &&
					launchGroupConfigFile.createNewFile()) {
				IOUtils.writeStringToFile(parallelRunConfigFile, parallelRunConfigContent);
				TITANConsole.println("Run configuration generated: " + parallelRunConfigFile.getAbsolutePath());
				
				IOUtils.writeStringToFile(titanRunConfigFile, titanRunConfigContent);
				TITANConsole.println("Run configuration generated: " + titanRunConfigFile.getAbsolutePath());
				
				IOUtils.writeStringToFile(launchGroupConfigFile, launchGroupConfigContent);
				TITANConsole.println("Run configuration generated: " + launchGroupConfigFile.getAbsolutePath());
				
				file.getProject().refreshLocal(IResource.DEPTH_INFINITE, null);
			} else {
				TITANConsole.println("File generation error. Existing files were not overwritten.");
			}
		} catch (IOException e) {
			TITANConsole.println("An error occurred.");
		}
	}
	
}
