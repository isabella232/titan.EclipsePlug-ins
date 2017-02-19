/******************************************************************************
 * Copyright (c) 2000-2017 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.wizards;
/*
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.core.TITANNature;
import org.eclipse.titan.designer.productUtilities.ProductConstants;
import org.eclipse.titan.designer.properties.data.MakeAttributesData;
import org.eclipse.titan.designer.properties.data.ProjectBuildPropertyData;
import org.eclipse.titan.designer.properties.data.ProjectDocumentHandlingUtility;
import org.eclipse.titan.designer.properties.data.ProjectFileHandler;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.wizards.newresource.BasicNewProjectResourceWizard;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;
*/

/**
 * This is temporal/experimental code for a new project wizard for a new feature.
 * It is INTENTIONALLY COMMENTED OUT:
 *   CQ request is on the way for the new dependencies.
 *   Users should not be able to activate this feature before it is ready and stable.
 * */
public class NewTITANJavaProjectWizard /*extends BasicNewResourceWizard implements IExecutableExtension*/ {
/*	private static final String NEWPROJECT_WINDOWTITLE = "New TITAN Java Project";
	private static final String NEWPROJECT_TITLE = "Create a TITAN Java Project";
	private static final String NEWPROJECT_DESCRIPTION = "Create a new TITAN Java project in the workspace or in an external location";
	private static final String CREATING_PROJECT = "creating project";
	private static final String CREATION_FAILED = "Project creation failed";

	private NewTITANProjectCreationPage mainPage;
	
	private IConfigurationElement config;
	private IProject newProject;
	private boolean isCreated = false;
	
	@Override
	public void addPages() {
		super.addPages();

		mainPage = new NewTITANProjectCreationPage(NEWPROJECT_WINDOWTITLE);
		mainPage.setTitle(NEWPROJECT_TITLE);
		mainPage.setDescription(NEWPROJECT_DESCRIPTION);
		addPage(mainPage);
	}

	@Override
	public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
		this.config = config;
	}

	@Override
	public void init(final IWorkbench workbench, final IStructuredSelection currentSelection) {
		super.init(workbench, currentSelection);
		setNeedsProgressMonitor(true);
		setWindowTitle(NEWPROJECT_WINDOWTITLE);
	}

	//TODO comment
	private IProject createNewProject() {
		final IProject tempProjectHandle = mainPage.getProjectHandle();

		URI location = null;
		if (!mainPage.useDefaults()) {
			location = mainPage.getLocationURI();
		}

		final IWorkspace workspace = ResourcesPlugin.getWorkspace();
		final String name = tempProjectHandle.getName();

		final IProject newProjectHandle = ResourcesPlugin.getWorkspace().getRoot().getProject(name);

		final IProjectDescription description = workspace.newProjectDescription(name);
		description.setLocationURI(location);
		description.setNatureIds(new String[] {TITANNature.NATURE_ID, JavaCore.NATURE_ID, "org.eclipse.pde.PluginNature"});

		final WorkspaceModifyOperation op = new WorkspaceModifyOperation() {
			@Override
			protected void execute(final IProgressMonitor monitor) throws CoreException {
				createProject(description, newProjectHandle, monitor);

				IFolder folder = newProjectHandle.getFolder("src");
				if (!folder.exists()) {
					try {
						folder.create(true, true, null);
					} catch (CoreException e) {
						ErrorReporter.logExceptionStackTrace(e);
					}
				}
				
				folder = newProjectHandle.getFolder("java_src");
				if (!folder.exists()) {
					try {
						folder.create(true, true, null);
					} catch (CoreException e) {
						ErrorReporter.logExceptionStackTrace(e);
					}
				}
				
				folder = newProjectHandle.getFolder("java_bin");
				if (!folder.exists()) {
					try {
						folder.create(true, true, null);
					} catch (CoreException e) {
						ErrorReporter.logExceptionStackTrace(e);
					}
				}
			}
		};

		try {
			getContainer().run(true, true, op);
		} catch (InterruptedException e) {
			return null;
		} catch (final InvocationTargetException e) {
			final Throwable t = e.getTargetException();
			if (t != null) {
				ErrorReporter.parallelErrorDisplayInMessageDialog(CREATION_FAILED, t.getMessage());
			}
			return null;
		}

		newProject = newProjectHandle;

		return newProject;
	}
	
	//TODO comment
	protected void createProject(final IProjectDescription description, final IProject projectHandle, final IProgressMonitor monitor)
			throws CoreException {
		final SubMonitor progress = SubMonitor.convert(monitor, 101);
		try {
			progress.setTaskName(CREATING_PROJECT);

			projectHandle.create(description, progress.newChild(50));

			if (progress.isCanceled()) {
				throw new OperationCanceledException();
			}

			projectHandle.open(IResource.BACKGROUND_REFRESH, progress.newChild(50));

			projectHandle.refreshLocal(IResource.DEPTH_ONE, progress.newChild(1));
			isCreated = true;
		} finally {
			progress.done();
		}
	}

	private void createBuildProperties() throws CoreException {
		final StringBuilder content = new StringBuilder();
		content.append("source.. = java_src/\n");
		content.append("output.. = java_bin/\n");
		content.append("bin.includes = META-INF/,\\\n");
		content.append("               .\n");
		
		final IFile properties = newProject.getFile(new Path("build.properties"));
		final InputStream stream = new ByteArrayInputStream(content.toString().getBytes());
		if(properties.exists()) {
			properties.setContents(stream, true, true, null);
		} else {
			properties.create(stream, true, null);
		}
	}
	
	private void createManifest() throws CoreException {
		final StringBuilder content = new StringBuilder("Manifest-Version: 1.0\n");
		content.append("Bundle-ManifestVersion: 2\n");
		content.append("Bundle-Name: " + newProject.getName() + "\n");
		content.append("Bundle-SymbolicName: " + newProject.getName() + "; singleton:=true\n");
		content.append("Bundle-Version: 1.0.0\n");
		content.append("Require-Bundle: org.eclipse.titan.runtime;bundle-version=\"1.0.0\"\n");
		content.append("Bundle-RequiredExecutionEnvironment: JavaSE-1.6\n");
		content.append("Bundle-ActivationPolicy: lazy\n");
		
		final IFolder metaInf = newProject.getFolder("META-INF");
		metaInf.create(false, true, null);

		final IFile properties = metaInf.getFile(new Path("MANIFEST.MF"));
		final InputStream stream = new ByteArrayInputStream(content.toString().getBytes());
		if(properties.exists()) {
			properties.setContents(stream, true, true, null);
		} else {
			properties.create(stream, true, null);
		}
	}

	@Override
	public boolean performFinish() {
		if(!isCreated) {
			createNewProject();
		}

		try {
			newProject.setPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER,
					MakeAttributesData.TEMPORAL_WORKINGDIRECTORY_PROPERTY), "Java_src");

			final IProjectDescription description = newProject.getDescription();

			final IJavaProject javaProject = JavaCore.create(newProject);
			final List<IClasspathEntry> classpathEntries = new ArrayList<IClasspathEntry>();
			classpathEntries.add(JavaCore.newContainerEntry(new Path("org.eclipse.jdt.launching.JRE_CONTAINER/org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType/J2SE-1.5")));
			classpathEntries.add(JavaCore.newContainerEntry(new Path("org.eclipse.pde.core.requiredPlugins")));
			classpathEntries.add(JavaCore.newSourceEntry(new Path("/" + newProject.getName() +"/java_src")));
			javaProject.setRawClasspath(classpathEntries.toArray(new IClasspathEntry[classpathEntries.size()]), null);
			javaProject.setOutputLocation(new Path("/" + newProject.getName() + "/java_bin"), null);
			
			createBuildProperties();
			createManifest();
			
			final ICommand titanCommand = description.newCommand();
			titanCommand.setBuilderName(ProductConstants.PRODUCT_ID_DESIGNER + ".core.TITANJavaBuilder");

			final ICommand javaCommand = description.newCommand();
			javaCommand.setBuilderName(JavaCore.BUILDER_ID);

			final ICommand manifestCommand = description.newCommand();
			manifestCommand.setBuilderName("org.eclipse.pde.ManifestBuilder");
			
			final ICommand schemaCommand = description.newCommand();
			schemaCommand.setBuilderName("org.eclipse.pde.SchemaBuilder");

			description.setBuildSpec(new ICommand[]{titanCommand, javaCommand, manifestCommand, schemaCommand});
			newProject.setDescription(description, null);
		} catch (CoreException exception) {
			ErrorReporter.logExceptionStackTrace(exception);
			return false;
		}

		ProjectDocumentHandlingUtility.createDocument(newProject);
		ProjectFileHandler pfHandler;
		pfHandler = new ProjectFileHandler(newProject);
		final WorkspaceJob job = pfHandler.saveProjectSettingsJob();

		try {
			job.join();
		} catch (InterruptedException e) {
			ErrorReporter.logExceptionStackTrace(e);
		}

		try {
			newProject.refreshLocal(IResource.DEPTH_INFINITE, null);
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace(e);
		}


		BasicNewProjectResourceWizard.updatePerspective(config);
		selectAndReveal(newProject);

		return true;
	}
	*/
}
