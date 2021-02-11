/******************************************************************************
 * Copyright (c) 2000-2021 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.actions.runconfiggenerator;

import static org.eclipse.titan.common.utils.StringUtils.isNullOrEmpty;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

public abstract class GenerateRunConfigBase extends AbstractHandler implements IObjectActionDelegate {
	
	/**
	 * Template for single mode run configuration as Java application.
	 * To be filled:
	 * <ol>
	 * <li>Project name</li>
	 * <li>Titan config file name</li>
	 * </ol>
	 */
	public static final String SINGLE_RUNCONFIG_TEMPLATE = 
			  "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n"
			+ "<launchConfiguration type=\"org.eclipse.jdt.launching.localJavaApplication\">\n"
			+ "    <listAttribute key=\"org.eclipse.debug.core.MAPPED_RESOURCE_PATHS\">\n"
			+ "        <listEntry value=\"/{0}/java_src/org/eclipse/titan/{0}/generated/Single_main.java\"/>\n"
			+ "    </listAttribute>\n"
			+ "    <listAttribute key=\"org.eclipse.debug.core.MAPPED_RESOURCE_TYPES\">\n"
			+ "        <listEntry value=\"1\"/>\n"
			+ "    </listAttribute>\n"
			+ "    <listAttribute key=\"org.eclipse.debug.ui.favoriteGroups\">\n"
			+ "        <listEntry value=\"org.eclipse.debug.ui.launchGroup.run\"/>\n"
			+ "    </listAttribute>\n"
			+ "    <booleanAttribute key=\"org.eclipse.jdt.launching.ATTR_ATTR_USE_ARGFILE\" value=\"false\"/>\n"
			+ "    <booleanAttribute key=\"org.eclipse.jdt.launching.ATTR_SHOW_CODEDETAILS_IN_EXCEPTION_MESSAGES\" value=\"true\"/>\n"
			+ "    <booleanAttribute key=\"org.eclipse.jdt.launching.ATTR_USE_CLASSPATH_ONLY_JAR\" value=\"false\"/>\n"
			+ "    <booleanAttribute key=\"org.eclipse.jdt.launching.ATTR_USE_START_ON_FIRST_THREAD\" value=\"true\"/>\n"
			+ "    <stringAttribute key=\"org.eclipse.jdt.launching.MAIN_TYPE\" value=\"org.eclipse.titan.{0}.generated.Single_main\"/>\n"
			+ "    <stringAttribute key=\"org.eclipse.jdt.launching.MODULE_NAME\" value=\"{0}\"/>\n"
			+ "    <stringAttribute key=\"org.eclipse.jdt.launching.PROGRAM_ARGUMENTS\" value=\"{1}\"/>\n"
			+ "    <stringAttribute key=\"org.eclipse.jdt.launching.PROJECT_ATTR\" value=\"{0}\"/>\n"
			+ "</launchConfiguration>\r\n";
	
	/**
	 * Template for <b>HC</b> (parallel mode run configuration as Java application).
	 * To be filled:
	 * <ol>
	 * <li>Project name</li>
	 * <li>Host from Titan config file</li>
	 * <li>Port from Titan config file</li>
	 * </ol>
	 */
	public static final String PARALLEL_RUNCONFIG_TEMPLATE = 
			  "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n"
			+ "<launchConfiguration type=\"org.eclipse.jdt.launching.localJavaApplication\">\n"
			+ "    <listAttribute key=\"org.eclipse.debug.core.MAPPED_RESOURCE_PATHS\">\n"
			+ "        <listEntry value=\"/{0}/java_src/org/eclipse/titan/{0}/generated/Parallel_main.java\"/>\n"
			+ "    </listAttribute>\n"
			+ "    <listAttribute key=\"org.eclipse.debug.core.MAPPED_RESOURCE_TYPES\">\n"
			+ "        <listEntry value=\"1\"/>\n"
			+ "    </listAttribute>\n"
			+ "    <booleanAttribute key=\"org.eclipse.jdt.launching.ATTR_ATTR_USE_ARGFILE\" value=\"false\"/>\n"
			+ "    <booleanAttribute key=\"org.eclipse.jdt.launching.ATTR_SHOW_CODEDETAILS_IN_EXCEPTION_MESSAGES\" value=\"true\"/>\n"
			+ "    <booleanAttribute key=\"org.eclipse.jdt.launching.ATTR_USE_CLASSPATH_ONLY_JAR\" value=\"false\"/>\n"
			+ "    <booleanAttribute key=\"org.eclipse.jdt.launching.ATTR_USE_START_ON_FIRST_THREAD\" value=\"true\"/>\n"
			+ "    <stringAttribute key=\"org.eclipse.jdt.launching.MAIN_TYPE\" value=\"org.eclipse.titan.{0}.generated.Parallel_main\"/>\n"
			+ "    <stringAttribute key=\"org.eclipse.jdt.launching.MODULE_NAME\" value=\"{0}\"/>\n"
			+ "    <stringAttribute key=\"org.eclipse.jdt.launching.PROGRAM_ARGUMENTS\" value=\"{1} {2,number,#}\"/>\n"
			+ "    <stringAttribute key=\"org.eclipse.jdt.launching.PROJECT_ATTR\" value=\"{0}\"/>\n"
			+ "</launchConfiguration>\n";
	
	/**
	 * Template for <b>MC</b> (parallel mode run configuration as TITAN Java Native launcher).
	 * To be filled:
	 * <ol>
	 * <li>Project name</li>
	 * <li>Titan config file name</li>
	 * </ol>
	 */
	public static final String TITAN_RUNCONFIG_TEMPLATE = 
			  "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\r\n"
			+ "<launchConfiguration type=\"org.eclipse.titan.executor.executors.java_mctr.LaunchConfigurationDelegate\">\r\n"
			+ "    <stringAttribute key=\"bad_container_name\" value=\"\\consume\"/>\r\n"
			+ "    <stringAttribute key=\"lastTimeSelection\" value=\"configuration file\"/>\r\n"
			+ "    <intAttribute key=\"lastTimeSelectionTime\" value=\"1\"/>\r\n"
			+ "    <intAttribute key=\"lastTimeSelectionType\" value=\"4\"/>\r\n"
			+ "    <listAttribute key=\"org.eclipse.debug.core.MAPPED_RESOURCE_PATHS\">\r\n"
			+ "        <listEntry value=\"/{0}\"/>\r\n"
			+ "    </listAttribute>\r\n"
			+ "    <listAttribute key=\"org.eclipse.debug.core.MAPPED_RESOURCE_TYPES\">\r\n"
			+ "        <listEntry value=\"4\"/>\r\n"
			+ "    </listAttribute>\r\n"
			+ "    <intAttribute key=\"org.eclipse.titan.executor.MainControllerStateRefreshTimeout\" value=\"5\"/>\r\n"
			+ "    <stringAttribute key=\"org.eclipse.titan.executor.configurationFilePath\" value=\"{1}\"/>\r\n"
			+ "    <booleanAttribute key=\"org.eclipse.titan.executor.consoleLogging\" value=\"true\"/>\r\n"
			+ "    <stringAttribute key=\"org.eclipse.titan.executor.executableFilePath\" value=\"\"/>\r\n"
			+ "    <booleanAttribute key=\"org.eclipse.titan.executor.executeConfigurationFileOnLaunch\" value=\"true\"/>\r\n"
			+ "    <booleanAttribute key=\"org.eclipse.titan.executor.keepTemporarilyGeneratedConfigurationFiles\" value=\"true\"/>\r\n"
			+ "    <intAttribute key=\"org.eclipse.titan.executor.maximumNotificationLineCount\" value=\"1000\"/>\r\n"
			+ "    <stringAttribute key=\"org.eclipse.titan.executor.projectName\" value=\"{0}\"/>\r\n"
			+ "    <booleanAttribute key=\"org.eclipse.titan.executor.severityLevelExtraction\" value=\"true\"/>\r\n"
			+ "    <booleanAttribute key=\"org.eclipse.titan.executor.testcaseRefreshOnStart\" value=\"true\"/>\r\n"
			+ "    <booleanAttribute key=\"org.eclipse.titan.executor.verdictExtraction\" value=\"true\"/>\r\n"
			+ "    <stringAttribute key=\"org.eclipse.titan.executor.workingdirectoryPath\" value=\"\"/>\r\n"
			+ "</launchConfiguration>\r\n";
	
	/**
	 * Template for automatic start of MC and HC as Launch Group.
	 * To be filled:
	 * <ol>
	 * <li>Run configuration of MC</li>
	 * <li>Run configuration of HC</li>
	 * </ol>
	 */
	public static final String LAUNCH_GROUP_CONFIG_TEMPLATE = 
			  "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\r\n"
			+ "<launchConfiguration type=\"org.eclipse.debug.core.groups.GroupLaunchConfigurationType\">\r\n"
			+ "    <stringAttribute key=\"org.eclipse.debug.core.launchGroup.0.action\" value=\"NONE\"/>\r\n"
			+ "    <booleanAttribute key=\"org.eclipse.debug.core.launchGroup.0.adoptIfRunning\" value=\"false\"/>\r\n"
			+ "    <booleanAttribute key=\"org.eclipse.debug.core.launchGroup.0.enabled\" value=\"true\"/>\r\n"
			+ "    <stringAttribute key=\"org.eclipse.debug.core.launchGroup.0.mode\" value=\"inherit\"/>\r\n"
			+ "    <stringAttribute key=\"org.eclipse.debug.core.launchGroup.0.name\" value=\"{0}\"/>\r\n"
			+ "    <stringAttribute key=\"org.eclipse.debug.core.launchGroup.1.action\" value=\"OUTPUT_REGEXP\"/>\r\n"
			+ "    <stringAttribute key=\"org.eclipse.debug.core.launchGroup.1.actionParam\" value=\"Listening on IP address\"/>"
			+ "    <booleanAttribute key=\"org.eclipse.debug.core.launchGroup.1.adoptIfRunning\" value=\"false\"/>\r\n"
			+ "    <booleanAttribute key=\"org.eclipse.debug.core.launchGroup.1.enabled\" value=\"true\"/>\r\n"
			+ "    <stringAttribute key=\"org.eclipse.debug.core.launchGroup.1.mode\" value=\"inherit\"/>\r\n"
			+ "    <stringAttribute key=\"org.eclipse.debug.core.launchGroup.1.name\" value=\"{1}\"/>\r\n"
			+ "    <listAttribute key=\"org.eclipse.debug.ui.favoriteGroups\">\r\n"
			+ "        <listEntry value=\"org.eclipse.debug.ui.launchGroup.run\"/>\r\n"
			+ "    </listAttribute>\r\n"
			+ "</launchConfiguration>";
	
	/**
	 * Folder where the run configurations are stored (inside the project)
	 */
	public static final String RUN_CONFIG_FOLDER = "launch";
	
	private ISelection selection;
	
	/**
	 * Invokes the abstract run configuration generation method
	 */
	private void invokeGeneration() {
		final IStructuredSelection structSelection = (IStructuredSelection) selection;

		for (final Object selected : structSelection.toList()) {
			if (selected instanceof IFile) {
				final IFile tempFile = (IFile) selected;
				try {
					generateRunConfig(tempFile);
				} catch (CoreException e) {
					ErrorReporter.logExceptionStackTrace(e);
				}
			}
		}
	}
	
	/**
	 * Generates the run configuration file for Eclipse.
	 * It simply fills the necessary parameters in the template launch config.
	 * 
	 * @param file The selected Titan configuration file
	 * @throws CoreException
	 */
	abstract protected void generateRunConfig(final IFile file) throws CoreException;

	@Override
	/** {@inheritDoc} */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		selection = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage().getSelection();
		if (!(selection instanceof IStructuredSelection)) {
			return null;
		}

		invokeGeneration();
		
		return null;
	}
	
	@Override
	/** {@inheritDoc} */
	public void run(IAction action) {
		if (!(selection instanceof IStructuredSelection)) {
			return;
		}

		invokeGeneration();
	}

	@Override
	/** {@inheritDoc} */
	public void selectionChanged(IAction action, ISelection selection) {
		this.selection = selection;		
	}

	@Override
	/** {@inheritDoc} */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		//Do nothing		
	}
	
	/**
	 * Checks the file name, project name and project location 
	 * properties of the selected file. 
	 * 
	 * @param file The selected file to checks
	 * @return Whether the properties of the file is okay or not
	 * @throws CoreException
	 */
	public boolean validateFile(final IFile file) throws CoreException {
		final String configFileName = file.getLocation().toOSString();		
		if (isNullOrEmpty(configFileName)) {
			ErrorReporter.logError("Error during processing the Titan configuration file");
			return false;
		}
		
		final String projectName = file.getProject().getName();
		if (isNullOrEmpty(projectName)) {
			ErrorReporter.logError("Error during processing the project name");
			return false;
		}
		
		final String projectLocation = file.getProject().getLocation().toOSString();
		if (isNullOrEmpty(projectLocation)) {
			ErrorReporter.logError("Error during processing the project location");
			return false;
		}
		
		return true;
	}
	
}
