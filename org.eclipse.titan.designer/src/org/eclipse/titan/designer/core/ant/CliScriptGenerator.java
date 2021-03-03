/******************************************************************************
 * Copyright (c) 2000-2021 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.core.ant;

import java.io.File;
import java.io.IOException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.common.utils.CommentUtils;
import org.eclipse.titan.common.utils.IOUtils;
import org.eclipse.titan.common.utils.StringUtils;
import org.eclipse.titan.designer.GeneralConstants;
import org.eclipse.titan.designer.properties.data.MakefileCreationData;
import org.eclipse.titan.designer.properties.data.ProjectBuildPropertyData;

/**
 * Utility class for generating Linux shell and Windows command scripts.
 * @author Adam Knapp
 */
public final class CliScriptGenerator {

	public static final String SCRIPT_NAME = "titan_java_start";
	public static final String CMD_SCRIPT_NAME = SCRIPT_NAME + ".cmd";
	public static final String SHELL_SCRIPT_NAME = SCRIPT_NAME + ".sh";

	private static final String SHELL_SCRIPT_HEADER = "#!/bin/sh\n";

	
	public static String createCmdScriptContent(final IProject project) {
		if (project == null) {
			return "";
		}
		final StringBuilder builder = new StringBuilder();
		builder.append(CommentUtils.getHeaderCommentsWithCopyright(":: ", GeneralConstants.VERSION_STRING));
		return builder.toString();
	}

	
	public static String createShellScriptContent(final IProject project) {
		if (project == null) {
			return "";
		}
		final StringBuilder builder = new StringBuilder();
		builder.append(SHELL_SCRIPT_HEADER)
		.append(CommentUtils.getHeaderCommentsWithCopyright("# ", GeneralConstants.VERSION_STRING));
		return builder.toString();
	}

	public static boolean existCmdScript(final IProject project) throws CoreException {
		if (project == null) {
			return false;
		}
		final String jarFolder = getJarFolder(project);
		if (StringUtils.isNullOrEmpty(jarFolder)) {
			return false;
		}
		final File scriptFile = new File(jarFolder + File.separator + CMD_SCRIPT_NAME);
		return scriptFile.exists();
	}

	
	public static boolean existShellScript(final IProject project) throws CoreException {
		if (project == null) {
			return false;
		}
		final String jarFolder = getJarFolder(project);
		if (StringUtils.isNullOrEmpty(jarFolder)) {
			return false;
		}
		final File scriptFile = new File(jarFolder + File.separator + SHELL_SCRIPT_NAME);
		return scriptFile.exists();
	}

	
	public static void generateAndStoreScripts(final IProject project) {
		if (project == null) {
			return;
		}
		try {
			String temp = project.getPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER,
					MakefileCreationData.GENERATE_START_SH_SCRIPT_PROPERTY));
			final boolean shellScriptNeeded = ProjectBuildPropertyData.TRUE_STRING.equals(temp) ? true : false;
			temp = project.getPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER,
					MakefileCreationData.GENERATE_START_BAT_SCRIPT_PROPERTY));
			final boolean cmdScriptNeeded = ProjectBuildPropertyData.TRUE_STRING.equals(temp) ? true : false;

			String shellScriptContent = null;
			String cmdScriptContent = null;
			if (shellScriptNeeded) {
				if (!existShellScript(project)) {
					shellScriptContent = createShellScriptContent(project);
				}
			}
			if (cmdScriptNeeded) {
				if (!existCmdScript(project)) {
					cmdScriptContent = createCmdScriptContent(project);
				}
			}
			storeScripts(project, shellScriptContent, cmdScriptContent);
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace(e);
		} catch (IOException e) {
			ErrorReporter.logExceptionStackTrace(e);
		}
	}

	
	private static String getJarFolder(final IProject project) throws CoreException {
		final String jarPathString = project.getPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER,
				MakefileCreationData.TARGET_EXECUTABLE_PROPERTY));
		if (StringUtils.isNullOrEmpty(jarPathString)) {
			ErrorReporter.INTERNAL_ERROR("Jar file is null or empty");
			return null;
		}
		File jarFile = new File(jarPathString);
		if (!jarFile.isAbsolute()) {
			jarFile = new File(project.getLocation().toOSString() + File.separator + jarPathString);
		}
		return jarFile.getParent();
	}
	
	
	public static void storeScripts(final IProject project, final String shellScriptContent,
			final String cmdScriptContent) throws CoreException, IOException {
		if (project == null || (shellScriptContent == null && cmdScriptContent == null)) {
			return;
		}
		final String jarFolderString = getJarFolder(project);
		if (shellScriptContent != null) {
			File shellScriptFile = new File(jarFolderString + File.separator + SHELL_SCRIPT_NAME);
			IOUtils.writeStringToFile(shellScriptFile, shellScriptContent);
		}
		if (cmdScriptContent != null) {
			File cmdScriptFile = new File(jarFolderString + File.separator + CMD_SCRIPT_NAME);
			IOUtils.writeStringToFile(cmdScriptFile, cmdScriptContent);
		}
	}
}
