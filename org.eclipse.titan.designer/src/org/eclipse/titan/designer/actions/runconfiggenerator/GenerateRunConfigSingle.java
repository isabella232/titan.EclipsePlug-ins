/******************************************************************************
 * Copyright (c) 2000-2021 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.actions.runconfiggenerator;

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
import org.eclipse.titan.common.utils.FileUtils;
import org.eclipse.titan.common.utils.IOUtils;
import org.eclipse.titan.designer.consoles.TITANConsole;
import org.eclipse.titan.designer.core.TITANBuilder;
import org.eclipse.titan.designer.core.TITANJavaBuilder;

/**
 * Executed by "Generate single run configuration for Eclipse" command/menu item.
 * It handles the generation of an Eclipse based run configuration file.
 * 
 * @author Adam Knapp
 *
 */
public class GenerateRunConfigSingle extends GenerateRunConfigBase {
	
	public GenerateRunConfigSingle() {
	}
	
	@Override
	/** {@inheritDoc} */
	protected void generateRunConfig(final IFile file) throws CoreException {
		if (TITANBuilder.isBuilderEnabled(file.getProject())) {
			generateRunConfigSingleTitan(file);
		} else if (TITANJavaBuilder.isBuilderEnabled(file.getProject())) {
			generateRunConfigSingleTitanJava(file);
		}
		
		return;
	}
	
	/**
	 * Generates the run configuration file for Eclipse.
	 * It simply fills the necessary parameters in the template launch config.
	 * 
	 * @param file The selected Titan configuration file
	 * @throws CoreException
	 */
	private void generateRunConfigSingleTitan(final IFile file) throws CoreException {
		ErrorReporter.parallelErrorDisplayInMessageDialog(
				"Error while generating the default launch configuration for project "
						+ file.getProject().getName(),
						"This feature is only available for Titan Java projects!");
	}
	
	/**
	 * Generates the run configuration file for Eclipse.
	 * It simply fills the necessary parameters in the template launch config.
	 * 
	 * @param file The selected Titan configuration file
	 * @throws CoreException
	 */
	private void generateRunConfigSingleTitanJava(final IFile file) throws CoreException {
		if (!validateFile(file)) {
			return;
		}
		
		final String projectName = file.getProject().getName();
		final String projectLocation = file.getProject().getLocation().toOSString();
		
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
		final URI filename = URIUtil.toURI(projectLocation + File.separator + 
				RUN_CONFIG_FOLDER + File.separator + filenamePrefix + "-Single.launch");
		final URI configFileNameUri = URIUtil.toURI(file.getProject().getLocation()).relativize(URIUtil.toURI(file.getLocation()));
		final String content = MessageFormat.format(SINGLE_RUNCONFIG_TEMPLATE, projectName, configFileNameUri.getPath());
		
		try {
			final File runConfigFile = new File(filename);
			if (overwrite) {
				FileUtils.deleteQuietly(runConfigFile);
			}
			if (runConfigFile.createNewFile()) {
				IOUtils.writeStringToFile(runConfigFile, content);
				TITANConsole.println("Run configuration generated: " + runConfigFile.getAbsolutePath());
				file.getProject().refreshLocal(IResource.DEPTH_INFINITE, null);
			} else {
				TITANConsole.println("File generation error. Existing file was not overwritten.");
			}
		} catch (IOException e) {
			TITANConsole.println("An error occurred.");
		}
	}

}
