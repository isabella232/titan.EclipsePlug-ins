/******************************************************************************
 * Copyright (c) 2000-2020 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.executor.tabpages.maincontroller;

import static org.eclipse.titan.executor.GeneralConstants.CONFIGFILEPATH;
import static org.eclipse.titan.executor.GeneralConstants.EXECUTABLEFILEPATH;
import static org.eclipse.titan.executor.GeneralConstants.EXECUTECONFIGFILEONLAUNCH;
import static org.eclipse.titan.executor.GeneralConstants.PROJECTNAME;
import static org.eclipse.titan.executor.GeneralConstants.WORKINGDIRECTORYPATH;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.debug.ui.ILaunchConfigurationTabGroup;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.eclipse.titan.common.fieldeditors.TITANResourceLocator;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.common.parsers.cfg.ConfigFileHandler;
import org.eclipse.titan.common.path.TITANPathUtilities;
import org.eclipse.titan.designer.productUtilities.ProductConstants;
import org.eclipse.titan.executor.designerconnection.DesignerHelper;
import org.eclipse.titan.executor.designerconnection.DynamicLinkingHelper;
import org.eclipse.titan.executor.designerconnection.EnvironmentHelper;
import org.eclipse.titan.executor.graphics.ImageCache;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * @author Kristof Szabados
 * */
public class NativeJavaMainControllerTab extends AbstractLaunchConfigurationTab {
	protected static final String EMPTY = "";
	private static final String NAME = "Basic Main Controller options";
	private static final String PROJECT = "Project (REQUIRED):";
	private static final String PROJECT_TOOLTIP =
			"This field is required.\n" +
					"When an existing project is selected and the Designer plug-in is also present the working directory " +
					"and executable fields are filled out automatically\n  with the values set as project properties.";
	private static final String CONFIGFILE = "Configuration file (REQUIRED):";
	private static final String CONFIGFILE_TOOLTIP = "This field is required.\n" +
			"The runtime configuration file used to describe the runtime behaviour of the executable test program.";
	private static final String BROWSE_WORKSPACE = "Browse Workspace..";

	public static final String BUILDER_ID = ProductConstants.PRODUCT_ID_DESIGNER + ".core.TITANJavaBuilder";

	private final class BasicProjectSelectorListener extends SelectionAdapter implements ModifyListener {

		@Override
		public void modifyText(final ModifyEvent e) {
			final Object source = e.getSource();
			if (null == source) {
				return;
			}
			if (source.equals(projectNameText)) {
				handleProjectNameModified();
			} else if (source.equals(configurationFileText.getTextControl(configFileGroup))) {
				handleConfigurationModified();
			}
			updateLaunchConfigurationDialog();
		}

		@Override
		public void widgetSelected(final SelectionEvent e) {
			final Object source = e.getSource();
			if (null == source) {
				return;
			}
			if (source.equals(projectSelectionButton)) {
				handleProjectButtonSelected();
			}

			updateLaunchConfigurationDialog();
		}
	}

	private final BasicProjectSelectorListener generalListener;
	private final ILaunchConfigurationTabGroup tabGroup;
	private ILaunchConfigurationWorkingCopy lastConfiguration;

	protected Text projectNameText;
	protected TITANResourceLocator configurationFileText;
	private Group configFileGroup;
	private Button projectSelectionButton;
	private Button automaticExecuteSectionExecution;
	protected boolean projectIsValid;
	private boolean projectIsJava;
	protected boolean configurationFileIsValid;

	protected List<Throwable> exceptions = new ArrayList<Throwable>();

	public NativeJavaMainControllerTab(final ILaunchConfigurationTabGroup tabGroup) {
		this.tabGroup = tabGroup;
		generalListener = new BasicProjectSelectorListener();
		projectIsValid = true;
		projectIsJava = true;
		configurationFileIsValid = false;
	}

	@Override
	public final void createControl(final Composite parent) {
		final Composite pageComposite = new Composite(parent, SWT.NONE);
		final GridLayout pageCompositeLayout = new GridLayout();
		pageCompositeLayout.numColumns = 1;
		pageComposite.setLayout(pageCompositeLayout);
		final GridData pageCompositeGridData = new GridData();
		pageCompositeGridData.horizontalAlignment = GridData.FILL;
		pageCompositeGridData.grabExcessHorizontalSpace = true;
		pageCompositeGridData.grabExcessVerticalSpace = true;
		pageComposite.setLayoutData(pageCompositeGridData);

		createProjectEditor(pageComposite);
		createConfigurationEditor(pageComposite);
		setControl(pageComposite);
	}

	@Override
	public final String getName() {
		return NAME;
	}

	@Override
	public final Image getImage() {
		return ImageCache.getImage("titan.gif");
	}

	@Override
	public final void initializeFrom(final ILaunchConfiguration configuration) {
		try {
			lastConfiguration = configuration.getWorkingCopy();
			String temp = configuration.getAttribute(PROJECTNAME, EMPTY);
			if (!temp.equals(projectNameText.getText())) {
				projectNameText.setText(temp);
			}

			temp = configuration.getAttribute(CONFIGFILEPATH, EMPTY);
			if (!temp.equals(configurationFileText.getStringValue())) {
				configurationFileText.setStringValue(temp);
			}

			final boolean tempBoolean = configuration.getAttribute(EXECUTECONFIGFILEONLAUNCH, false);
			if (tempBoolean != automaticExecuteSectionExecution.getSelection()) {
				automaticExecuteSectionExecution.setSelection(tempBoolean);
			}

			final IProject project = getProject();
			if (project == null) {
				return;
			}

			final String projectPath = project.getLocation().toOSString(); //TODO should use URI based addresses
			configurationFileText.setRootPath(projectPath);
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace(e);
		}
	}

	@Override
	public final void performApply(final ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(PROJECTNAME, projectNameText.getText());
		configuration.setAttribute(CONFIGFILEPATH, configurationFileText.getStringValue());
		configuration.setAttribute(EXECUTECONFIGFILEONLAUNCH, automaticExecuteSectionExecution.getSelection());

		final IProject project = getProject();
		configuration.setMappedResources(new IResource[] {project});
	}

	@Override
	public final void setDefaults(final ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(PROJECTNAME, EMPTY);
		configuration.setAttribute(WORKINGDIRECTORYPATH, EMPTY);
		configuration.setAttribute(EXECUTABLEFILEPATH, EMPTY);
		configuration.setAttribute(CONFIGFILEPATH, EMPTY);
		configuration.setAttribute(EXECUTECONFIGFILEONLAUNCH, false);
		configuration.setMappedResources(new IResource[0]);
	}

	protected final void createProjectEditor(final Composite parent) {
		final Font font = parent.getFont();
		final Group group = new Group(parent, SWT.NONE);
		group.setText(PROJECT);
		group.setToolTipText(PROJECT_TOOLTIP);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		group.setLayoutData(gd);
		final GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		group.setLayout(layout);
		group.setFont(font);
		projectNameText = new Text(group, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		projectNameText.setLayoutData(gd);
		projectNameText.setFont(font);
		projectNameText.addModifyListener(generalListener);
		projectSelectionButton = createPushButton(group, BROWSE_WORKSPACE, null);
		projectSelectionButton.addSelectionListener(generalListener);
	}

	protected final void createConfigurationEditor(final Composite parent) {
		final Font font = parent.getFont();
		configFileGroup = new Group(parent, SWT.NONE);
		configFileGroup.setText(CONFIGFILE);
		configFileGroup.setToolTipText(CONFIGFILE_TOOLTIP);
		final GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		configFileGroup.setLayoutData(gd);
		final GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		configFileGroup.setLayout(layout);
		configFileGroup.setFont(font);

		final IProject project = getProject();
		if (project == null) {
			configurationFileText = new TITANResourceLocator(CONFIGFILE, configFileGroup, IResource.FILE, "");
		} else {
			configurationFileText = new TITANResourceLocator(CONFIGFILE, configFileGroup, IResource.FILE, getProject().getLocation().toOSString());
		}
		configurationFileText.getLabelControl(configFileGroup).setToolTipText(CONFIGFILE_TOOLTIP);
		configurationFileText.getTextControl(configFileGroup).addModifyListener(generalListener);

		automaticExecuteSectionExecution = new Button(configFileGroup, SWT.CHECK);
		automaticExecuteSectionExecution.setText("Execute automatically");
		automaticExecuteSectionExecution.setToolTipText("Execute the `EXECUTE' section of the configuration file automatically when launched");
		automaticExecuteSectionExecution.setSelection(false);
		automaticExecuteSectionExecution.addSelectionListener(generalListener);
		automaticExecuteSectionExecution.setEnabled(true);
	}

	/**
	 * Show a dialog that lets the user select a project. This in turn provides context for the main type, allowing the user to key a main type name,
	 * or constraining the search for main types to the specified project.
	 */
	protected final void handleProjectButtonSelected() {
		final ILabelProvider labelProvider = new WorkbenchLabelProvider();
		final ElementListSelectionDialog dialog = new ElementListSelectionDialog(getShell(), labelProvider);
		dialog.setTitle("Project selection");
		dialog.setMessage("Select a project to constrain your search.");
		final IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		final List<IProject> availableProjects = new ArrayList<IProject>(projects.length);
		for (final IProject project : projects) {
			try {
				if (project.isAccessible() && project.hasNature(DesignerHelper.NATURE_ID)) {
					availableProjects.add(project);
				}
			} catch (CoreException e) {
				ErrorReporter.logExceptionStackTrace(e);
			}
		}

		dialog.setElements(availableProjects.toArray(new IProject[availableProjects.size()]));
		if (dialog.open() == Window.OK) {
			final String projectName = ((IProject) dialog.getFirstResult()).getName();
			if (!projectName.equals(projectNameText.getText())) {
				projectNameText.setText(projectName);
			}
		}
	}

	/**
	 * @return the project selected or null if none.
	 * */
	public final IProject getProject() {
		if (projectNameText == null) {
			return null;
		}

		final String projectName = projectNameText.getText();
		final IProject projectWithTitanNature = DynamicLinkingHelper.getProject(projectName);
		if (projectWithTitanNature != null) {
			projectIsValid = true;
		}

		return projectWithTitanNature;
	}

	private void handleProjectNameModified() {
		final IProject project = getProject();
		if (project == null) {
			projectIsValid = false;
			return;
		}

		projectIsValid = true;

		checkJavaBuilder(project);
	}

	private boolean checkJavaBuilder(final IProject project) {
		projectIsJava = false;

		if (!project.isAccessible()) {
			return false;
		}

		IProjectDescription description;
		try {
			description = project.getDescription();
		} catch (CoreException e) {
			return false;
		}

		final ICommand[] cmds = description.getBuildSpec();
		for (int i = 0; i < cmds.length; i++) {
			if (BUILDER_ID.equals(cmds[i].getBuilderName())) {
				projectIsJava = true;
				return true;
			}
		}

		return false;
	}

	protected final void handleConfigurationModified() {
		if (EMPTY.equals(configurationFileText.getStringValue())) {
			automaticExecuteSectionExecution.setEnabled(false);
			return;
		}

		final IProject project = getProject();
		URI uri;
		if (project == null) {
			uri = URIUtil.toURI(configurationFileText.getStringValue());
		} else {
			uri = TITANPathUtilities.resolvePathURI(configurationFileText.getStringValue(), getProject().getLocation().toOSString());
		}

		final File file = new File(uri);
		if (file.exists() && file.isFile()) {
			exceptions.clear();
			final ConfigFileHandler configHandler = new ConfigFileHandler();
			configHandler.readFromFile(file.getPath());
			if (configHandler.parseExceptions().isEmpty()) {
				final Map<String, String> env = new HashMap<String, String>(System.getenv());
				Map<String, String> tempEnvironmentalVariables;

				try {
					tempEnvironmentalVariables = lastConfiguration.getAttribute(ILaunchManager.ATTR_ENVIRONMENT_VARIABLES, new HashMap<String, String>());
					EnvironmentHelper.resolveVariables(env, tempEnvironmentalVariables);
					configHandler.setEnvMap(env);
					configHandler.processASTs();
				} catch (CoreException e) {
					exceptions.add(e);
					configurationFileIsValid = false;
				}
			}
			exceptions.addAll(configHandler.parseExceptions());

			if (exceptions.isEmpty()) {
				configurationFileIsValid = true;
				automaticExecuteSectionExecution.setEnabled(true);
				return;
			}
		}
		configurationFileIsValid = false;
		exceptions.clear();
		exceptions.add(new Exception("The path `" + URIUtil.toPath(uri) + "' does not seem to be correct."));
		automaticExecuteSectionExecution.setEnabled(false);
	}

	/**
	 * Calculates the working directory of the provided project.
	 *
	 * @param project the project to use.
	 *
	 * @return the working directory.
	 * */
	public static String getRAWWorkingDirectoryForProject(final IProject project) {
		try {
			final String workingDirectory = project.getPersistentProperty(
					new QualifiedName(DesignerHelper.PROJECT_BUILD_PROPERTYPAGE_QUALIFIER,	DesignerHelper.WORKINGDIR_PROPERTY));
			return workingDirectory;
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace(e);
		}

		return "bin";
	}

	/**
	 * Calculates the executable of the provided project.
	 *
	 * @param project the project to use.
	 *
	 * @return the executable.
	 * */
	public static String getExecutableForProject(final IProject project) {
		try {
			final String executable = project.getPersistentProperty(
					new QualifiedName(DesignerHelper.PROJECT_BUILD_PROPERTYPAGE_QUALIFIER,	DesignerHelper.EXECUTABLE_PROPERTY));
			if (executable != null) {
				return executable;
			}
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace(e);
		}

		return "";
	}

	@Override
	public boolean canSave() {
		if (!EMPTY.equals(projectNameText.getText()) && !projectIsValid) {
			return false;
		}

		if (!projectIsJava) {
			return false;
		}

		return super.canSave();
	}

	@Override
	public boolean isValid(final ILaunchConfiguration launchConfig) {
		if (!EMPTY.equals(projectNameText.getText()) && !projectIsValid) {
			setErrorMessage("The name of the project is not valid.");
			return false;
		}

		if (!projectIsJava) {
			setErrorMessage("The project must be a Titan Java project.");
			return false;
		}

		if(EMPTY.equals(configurationFileText.getStringValue())) {
			setErrorMessage("The configuration file must be set."); //<<<<== This should be set to "setErrorMessage(null);"
					//if the cfg file is not mandatory !
			return false; // <<<<== This can be set to true if the cfg file is not mandatory !
		} else if (!configurationFileIsValid) {
			if (null != exceptions && !exceptions.isEmpty()) {
				setErrorMessage("Problem in config file: " + exceptions.get(0).getMessage());
			} else {
				setErrorMessage("The configuration file is not valid.");
			}
			return false;
		}
		setErrorMessage(null);
		return true;
	}

	/**
	 * Initializes the provided launch configuration for JNI mode execution.
	 *
	 * @param configuration the configuration to initialize.
	 * @param project the project to gain data from.
	 * @param configFilePath the path of the configuration file.
	 * */
	public static boolean initLaunchConfiguration(final ILaunchConfigurationWorkingCopy configuration,
			final IProject project, final String configFilePath) {
		configuration.setAttribute(PROJECTNAME, project.getName());
		configuration.setAttribute(CONFIGFILEPATH, configFilePath);
		configuration.setAttribute(EXECUTECONFIGFILEONLAUNCH, !"".equals(configFilePath));

		return true;
	}
}
