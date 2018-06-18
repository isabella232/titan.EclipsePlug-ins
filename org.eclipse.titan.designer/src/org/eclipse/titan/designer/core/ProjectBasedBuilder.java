/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.core;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.common.path.TITANPathUtilities;
import org.eclipse.titan.designer.GeneralConstants;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.properties.data.MakeAttributesData;
import org.eclipse.titan.designer.properties.data.MakefileCreationData;
import org.eclipse.titan.designer.properties.data.ProjectBuildPropertyData;

/**
 * @author Kristof Szabados
 * */
public final class ProjectBasedBuilder {
	static final String EMPTY_STRING = "";
	static final String BUILD_FAILED = "Build failed for project ";
	static final String INVALID_WORKINGDIRECTORY1 = "Invalid working directory";
	static final String INVALID_WORKINGDIRECTORY2 = "The working directory and the project's directory must not be the same";

	private static final Map<IProject, ProjectBasedBuilder> BUILDERS = new HashMap<IProject, ProjectBasedBuilder>();
	private static final Map<IProject, Boolean> FORCED_BUILD_SET = new HashMap<IProject, Boolean>();
	private static final Map<IProject, Boolean> FORCED_MAKEFILE_REBUILD_SET = new HashMap<IProject, Boolean>();

	private final IProject project;

	public ProjectBasedBuilder(final IProject project) {
		this.project = project;
	}

	/**
	 * Creates a builder that handles the build related information of the
	 * given project.
	 * 
	 * @param project
	 *                the project to create a builder for
	 * 
	 * @return the builder which handles the build related information of
	 *         the provided project
	 * */
	public static ProjectBasedBuilder getProjectBasedBuilder(final IProject project) {
		ProjectBasedBuilder builder;

		if (BUILDERS.containsKey(project)) {
			builder = BUILDERS.get(project);
		} else {
			builder = new ProjectBasedBuilder(project);
			BUILDERS.put(project, builder);
		}

		return builder;
	}

	/**
	 * Gets the path of the working directory as an URI.
	 * 
	 * @param reportError
	 *                whether to report error if one is found o not.
	 * @return the path of the working directory as an URI, or null if there
	 *         were errors.
	 * */
	public URI getWorkingDirectoryURI(final boolean reportError) {
		if (!project.isAccessible()) {
			return null;
		}

		String workingDirectory;

		try {
			workingDirectory = project.getPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER,
					MakeAttributesData.TEMPORAL_WORKINGDIRECTORY_PROPERTY));
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace(e);
			workingDirectory = null;
		}

		if (workingDirectory == null || EMPTY_STRING.equals(workingDirectory)) {
			if (reportError){
				ErrorReporter.parallelErrorDisplayInMessageDialog(BUILD_FAILED + project.getName(),INVALID_WORKINGDIRECTORY1);
			}
			return null;
		}

		return TITANPathUtilities.resolvePath(workingDirectory, project.getLocationURI());
	}

	/**
	 * Gets the path of the working directory as an IPath.
	 * 
	 * @param reportError
	 *                whether to report error if one is found or not.
	 * @return the path of the working directory as an IPath, or null if
	 *         there were errors.
	 * */
	public IPath getWorkingDirectoryPath(final boolean reportError) {
		final URI uri = getWorkingDirectoryURI(reportError);
		if (uri == null) {
			return null;
		}

		return URIUtil.toPath(uri);
	}

	/**
	 * Calculates the eclipse resources, that can be mapped to the working
	 * directory of the project.
	 * 
	 * @param reportError
	 *                whether to report error if one is found or not.
	 * 
	 * @return the list of container resources that map to the working
	 *         directory, or an empty array if none.
	 * */
	public IContainer[] getWorkingDirectoryResources(final boolean reportError) {
		final URI uri = getWorkingDirectoryURI(reportError);
		if (uri == null) {
			return new IContainer[0];
		}

		final IWorkspaceRoot wroot = ResourcesPlugin.getWorkspace().getRoot();
		return wroot.findContainersForLocationURI(uri);
	}

	public IProject[] getReferencedProjects() {
		if (project == null) {
			return new IProject[] {};
		}

		try {
			if (project.isAccessible()) {
				return project.getReferencedProjects();
			}
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace(e);
		}

		return new IProject[] {};
	}

	/**
	 * @return a list of all projects reachable from the actual project.
	 * */
	public List<IProject> getAllReachableProjects() {
		final List<IProject> referenceChain = new ArrayList<IProject>();
		final List<IProject> knownProjects = new ArrayList<IProject>();

		getAllReachableProjects(referenceChain, project, knownProjects);

		return knownProjects;
	}

	/**
	 * Calculates and returns a list of all reachable projects from the
	 * provided one.
	 * 
	 * @param referenceChain
	 *                a reference chain used to detect project reference
	 *                cycles.
	 * @param actualProject
	 *                the project being checked.
	 * @param knownProjects
	 *                the projects already known, they shall not be added
	 *                any more to the list.
	 * */
	private void getAllReachableProjects(final List<IProject> referenceChain, final IProject actualProject, final List<IProject> knownProjects) {
		if (knownProjects.contains(actualProject)) {
			return;
		}

		if (referenceChain.contains(actualProject)) {
			knownProjects.add(actualProject);
			return;
		}

		final IProject[] referencedProjects = ProjectBasedBuilder.getProjectBasedBuilder(actualProject).getReferencedProjects();

		if (referencedProjects.length == 0) {
			knownProjects.add(actualProject);
		}

		final int size = referenceChain.size();
		referenceChain.add(actualProject);
		for (IProject temporalProject : referencedProjects) {
			getAllReachableProjects(referenceChain, temporalProject, knownProjects);
		}

		referenceChain.remove(size);

		if (!knownProjects.contains(actualProject)) {
			knownProjects.add(actualProject);
		}
	}

	/**
	 * @return a list of all projects referencing transitively the actual project.
	 * */
	public List<IProject> getAllReferencingProjects() {
		final List<IProject> referenceChain = new ArrayList<IProject>();
		final List<IProject> knownProjects = new ArrayList<IProject>();

		getAllReferencingProjects(referenceChain, project, knownProjects);

		return knownProjects;
	}

	/**
	 * Calculates and returns a list of all projects referencing transitively the provided one.
	 * 
	 * @param referenceChain
	 *                a reference chain used to detect project reference
	 *                cycles.
	 * @param actualProject
	 *                the project being checked.
	 * @param knownProjects
	 *                the projects already known, they shall not be added
	 *                any more to the list.
	 * */
	private void getAllReferencingProjects(final List<IProject> referenceChain, final IProject actualProject, final List<IProject> knownProjects) {
		if (knownProjects.contains(actualProject)) {
			return;
		}

		if (referenceChain.contains(actualProject)) {
			knownProjects.add(actualProject);
			return;
		}

		final IProject[] referencingProjects = ProjectBasedBuilder.getProjectBasedBuilder(actualProject).getReferencingProjects();

		if (referencingProjects.length == 0) {
			knownProjects.add(actualProject);
		}

		final int size = referenceChain.size();
		referenceChain.add(actualProject);
		for (IProject temporalProject : referencingProjects) {
			getAllReferencingProjects(referenceChain, temporalProject, knownProjects);
		}

		referenceChain.remove(size);

		if (!knownProjects.contains(actualProject)) {
			knownProjects.add(actualProject);
		}
	}

	public IProject[] getReferencingProjects() {
		return project.getReferencingProjects();
	}

	/**
	 * Sets for project that it should be forced to build itself.
	 * 
	 * @param project
	 *                the project to set this attribute for.
	 * */
	public static void setForcedBuild(final IProject project) {
		FORCED_BUILD_SET.put(project, Boolean.TRUE);
	}

	public static Boolean getForcedBuild(final IProject project) {
		if (FORCED_BUILD_SET.containsKey(project)) {
			return FORCED_BUILD_SET.get(project);
		}

		return Boolean.FALSE;
	}

	public static void clearForcedBuild(final IProject project) {
		FORCED_BUILD_SET.remove(project);
	}

	/**
	 * Sets for project that it should regenerate its makefile.
	 * 
	 * @param project
	 *                the project to set this attribute for.
	 * */
	public static void setForcedMakefileRebuild(final IProject project) {
		FORCED_MAKEFILE_REBUILD_SET.put(project, Boolean.TRUE);
	}

	public static Boolean getForcedMakefileRebuild(final IProject project) {
		if (FORCED_MAKEFILE_REBUILD_SET.containsKey(project)) {
			return FORCED_MAKEFILE_REBUILD_SET.get(project);
		}

		return Boolean.FALSE;
	}

	public static void clearForcedMakefileRebuild(final IProject project) {
		FORCED_MAKEFILE_REBUILD_SET.remove(project);
	}

	/**
	 * This method returns a resource visitor that has visited the project
	 * resource.
	 * 
	 * @return the TITANBuilderResourceVisitor that has visited the project
	 *         resource
	 */
	public TITANBuilderResourceVisitor getResourceVisitor() {
		final TITANBuilderResourceVisitor visitor = new TITANBuilderResourceVisitor(getWorkingDirectoryResources(false));
		try {
			if (project.isAccessible()) {
				project.accept(visitor);
			}
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace(e);
		}

		return visitor;
	}

	public Map<String, IFile> getFilesofReferencedProjects() {
		final Set<IProject> projectSet = new HashSet<IProject>();
		projectSet.add(project);

		return getFilesofReferencedProjects(projectSet);
	}

	private Map<String, IFile> getFilesofReferencedProjects(final Set<IProject> visitedProjects) {
		final IProject[] projects = getReferencedProjects();
		final Map<String, IFile> files = new HashMap<String, IFile>();

		for (IProject tempProject : projects) {
			try {
				if (tempProject.isAccessible() && !visitedProjects.contains(tempProject)) {
					final IContainer[] workingDirectories = getProjectBasedBuilder(tempProject).getWorkingDirectoryResources(false);
					final IPath workingDir = ProjectBasedBuilder.getProjectBasedBuilder(tempProject).getWorkingDirectoryPath(false);
					ReferencedProjectResourceVisitor visitor = new ReferencedProjectResourceVisitor(workingDirectories,
							workingDir);

					visitedProjects.add(tempProject);
					final Map<String, IFile> tempFiles = getProjectBasedBuilder(tempProject).getFilesofReferencedProjects(
							visitedProjects);
					files.putAll(tempFiles);

					tempProject.accept(visitor);
					files.putAll(visitor.getFiles());
				}
				visitedProjects.remove(tempProject);
			} catch (CoreException e) {
				ErrorReporter.logExceptionStackTrace("While gathering the files of project " + tempProject.getName(), e);
			}
		}

		return files;
	}
	/**
	 * Checks whether the code splitting modes are the same in the referred projects as splitting mode in the current project.
	 * If difference was found, compilation error marker is placed on the current project
	 * @return true if the splitting modes are the same, otherwise false
	 */
	public boolean checkCodeSplittingEquality(){
		final Location location = new Location(project);
		String codeSplitting = "";
		boolean errorFound = false;
		StringBuilder wrongProjects = new StringBuilder();
		wrongProjects.append("Code splitting setting failure in project(s): ");
		try {
			codeSplitting = project.getPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER,
					MakefileCreationData.CODE_SPLITTING_PROPERTY));
			if(codeSplitting == null) {
				ErrorReporter.logError("Code splitting value is not set for "+ project.getName());
				return false;
			}
			String tempCodeSplitting;
			final IProject[] projects = getReferencedProjects();
			for (IProject tempProject : projects) { 
				if(tempProject.isAccessible()) {
					tempCodeSplitting = tempProject.getPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER,
							MakefileCreationData.CODE_SPLITTING_PROPERTY));
					if(!codeSplitting.equals(tempCodeSplitting)) {
						ErrorReporter.logError(
								"Code splitting error found in project " + tempProject.getName() + "; Project "
										+project.getName() + " expected " + codeSplitting + ", got " + tempCodeSplitting);
						if (errorFound!=false ){
							wrongProjects.append(',');
						};
						wrongProjects.append(tempProject.getName());
						errorFound = true;
					}
				} else {
					ErrorReporter.logError("This referenced project is not accessible:" + tempProject);
				}
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
		if(errorFound){
			wrongProjects.append("; Project ").append(project.getName()).append(" expected ").append(codeSplitting);
			location.reportExternalProblem(wrongProjects.toString(), IMarker.SEVERITY_ERROR, GeneralConstants.COMPILER_ERRORMARKER);
			return false;
		} else {
			return true;
		}
	}
}
