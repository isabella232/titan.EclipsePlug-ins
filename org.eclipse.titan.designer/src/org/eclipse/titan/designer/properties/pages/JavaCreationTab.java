/******************************************************************************
 * Copyright (c) 2000-2021 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.properties.pages;

import java.net.URI;

import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.titan.common.fieldeditors.TITANResourceLocatorFieldEditor;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.common.path.PathUtil;
import org.eclipse.titan.common.path.TITANPathUtilities;
import org.eclipse.titan.common.utils.StringUtils;
import org.eclipse.titan.designer.core.ant.AntLaunchConfigGenerator;
import org.eclipse.titan.designer.core.ant.AntScriptGenerator;
import org.eclipse.titan.designer.preferences.pages.ComboFieldEditor;
import org.eclipse.titan.designer.productUtilities.ProductConstants;
import org.eclipse.titan.designer.properties.data.MakefileCreationData;
import org.eclipse.titan.designer.properties.data.ProjectBuildPropertyData;
import org.eclipse.ui.dialogs.PropertyPage;

/**
 * Tab page for configuring the Java target, i.e. class or executable JAR is produced 
 * at the end of build process 
 * @author Adam Knapp
 * */
public class JavaCreationTab {
	private static final String JAVA_CREATION_TAB_TITLE = "Java target creation attributes";

	private final IProject project;
	private final PropertyPage page;
	private Composite automaticBuildPropertiesComposite;
	private Composite defaultJavaTargetComposite;
	private Composite javaTargetComposite;
	private ComboFieldEditor defaultJavaTarget;
	private TabItem creationAttributesTabItem;
	private TITANResourceLocatorFieldEditor temporalJavaTargetFileFieldEditor;
	public static final String TEMPORAL_JAVA_TARGET = ProductConstants.PRODUCT_ID_DESIGNER + ".temporalJavaTarget";
	private Button generateStartShScript;
	private Button generateStartBatScript;

	public JavaCreationTab(final IProject project, final PropertyPage page) {
		this.project = project;
		this.page = page;
	}

	public JavaCreationTab(final IProject project) {
		this.project = project;
		this.page = null;
	}

	/**
	 * Disposes the SWT resources allocated by this tab page.
	 */
	public void dispose() {

	}

	/**
	 * Creates and returns the SWT control for the customized body of this
	 * TabItem under the given parent TabFolder.
	 * <p>
	 * 
	 * @param tabFolder
	 *                the parent TabFolder
	 * @return the new TabItem
	 */
	protected TabItem createContents(final TabFolder tabFolder) {
		creationAttributesTabItem = new TabItem(tabFolder, SWT.BORDER);
		creationAttributesTabItem.setText(JAVA_CREATION_TAB_TITLE);
		creationAttributesTabItem.setToolTipText("Settings controlling the generation of Java binaries.");

		automaticBuildPropertiesComposite = new Composite(tabFolder, SWT.MULTI);
		automaticBuildPropertiesComposite.setEnabled(true);
		automaticBuildPropertiesComposite.setLayout(new GridLayout());
		//defaultTarget
		defaultJavaTargetComposite = new Composite(automaticBuildPropertiesComposite, SWT.NONE);
		defaultJavaTarget = new ComboFieldEditor(MakefileCreationData.DEFAULT_TARGET_PROPERTY, "Default target:",
				MakefileCreationData.DefaultJavaTarget.getDisplayNamesAndValues(), defaultJavaTargetComposite);

		defaultJavaTarget.setPropertyChangeListener (new IPropertyChangeListener() {
			@Override
			public void propertyChange(final PropertyChangeEvent event) {
				updateDefaultJavaTarget();
			}
		});

		//target JAR
		javaTargetComposite = new Composite(automaticBuildPropertiesComposite, SWT.NONE);
		final GridLayout targetExecutableLayout = new GridLayout();
		javaTargetComposite.setLayout(targetExecutableLayout);
		final GridData targetExecutableData = new GridData(GridData.FILL);
		targetExecutableData.grabExcessHorizontalSpace = true;
		targetExecutableData.horizontalAlignment = SWT.FILL;
		javaTargetComposite.setLayoutData(targetExecutableData);
		temporalJavaTargetFileFieldEditor = new TITANResourceLocatorFieldEditor(TEMPORAL_JAVA_TARGET, 
				"JAR file:", javaTargetComposite, IResource.FILE, project.getLocation().toOSString());
		temporalJavaTargetFileFieldEditor
				.getLabelControl(javaTargetComposite)
				.setToolTipText("The target of the Java build process.\n"
						+ "This field is optional.\n"
						+ "If it is not set, only Java class files will be generated into the working directory.");
		final String[] extensions = new String[] {"*.jar;*.zip", "*.*"};
		temporalJavaTargetFileFieldEditor.setFilterExtensions(extensions);
		if (page != null) {
			temporalJavaTargetFileFieldEditor.setPage(page);
		}
		generateStartShScript = new Button(automaticBuildPropertiesComposite, SWT.CHECK);
		generateStartShScript.setText("Generate ttcn3_start shell script for starting the JAR (Linux)");
		generateStartShScript.setEnabled(false);
		generateStartShScript
				.setToolTipText("If this option is set, also a shell script will be generated to start the JAR");
		generateStartBatScript = new Button(automaticBuildPropertiesComposite, SWT.CHECK);
		generateStartBatScript.setText("Generate ttcn3_start.bat script for starting the JAR (WIN)");
		generateStartBatScript.setEnabled(false);
		generateStartBatScript
				.setToolTipText("If this option is set, also a .bat script will be generated to start the JAR");
		creationAttributesTabItem.setControl(automaticBuildPropertiesComposite);
		return creationAttributesTabItem;
	}

	/**
	 * Checks the properties of this page for errors.
	 * 
	 * @param page
	 *                the property page to report the errors to.
	 * @return true if no error was found, false otherwise.
	 * */
	public boolean checkProperties(final ProjectJavaBuildPropertyPage page) {
		if ((temporalJavaTargetFileFieldEditor.getStringValue().isEmpty() || !temporalJavaTargetFileFieldEditor.isValid())
				&& !isClassSelected()) {
			String errorMessage = temporalJavaTargetFileFieldEditor.getErrorMessage();
			if (errorMessage == null) {
				errorMessage = "The Java target is not set";
			}
			page.setErrorMessage(errorMessage);
			return false;
		}
		return true;
	}

	/**
	 * Copies the actual values into the provided preference storage.
	 * 
	 * @param project
	 *                the actual project (the real preference store).
	 * @param tempStorage
	 *                the temporal store to copy the values to.
	 * */
	public void copyPropertyStore(final IProject project, final PreferenceStore tempStorage) {
		String temp = null;
		try {
			temp = project.getPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER,
					MakefileCreationData.DEFAULT_JAVA_TARGET_PROPERTY));
			if (temp != null) {
				tempStorage.setValue(MakefileCreationData.DEFAULT_JAVA_TARGET_PROPERTY, temp);
			}
			temp = project.getPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER,
					MakefileCreationData.TARGET_EXECUTABLE_PROPERTY));
			if (temp != null) {
				tempStorage.setValue(MakefileCreationData.TARGET_EXECUTABLE_PROPERTY, temp);
			}
			temp = project.getPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER,
					MakefileCreationData.GENERATE_START_SH_SCRIPT_PROPERTY));
			if (temp != null) {
				tempStorage.setValue(MakefileCreationData.GENERATE_START_SH_SCRIPT_PROPERTY, temp);
			}
			temp = project.getPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER,
					MakefileCreationData.GENERATE_START_BAT_SCRIPT_PROPERTY));
			if (temp != null) {
				tempStorage.setValue(MakefileCreationData.GENERATE_START_BAT_SCRIPT_PROPERTY, temp);
			}
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace(e);
		}
	}

	/**
	 * Evaluates the properties on the option page, and compares them with
	 * the saved values.
	 * 
	 * @param project
	 *                the actual project (the real preference store).
	 * @param tempStorage
	 *                the temporal store to copy the values to.
	 * 
	 * @return true if the values in the real and the temporal storage are
	 *         different (they have changed), false otherwise.
	 * */
	public boolean evaluatePropertyStore(final IProject project, final PreferenceStore tempStorage) {
		boolean result = false;
		String actualValue = null;
		String copyValue = null;
		try {
			actualValue = project.getPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER,
					MakefileCreationData.DEFAULT_JAVA_TARGET_PROPERTY));
			copyValue = tempStorage.getString(MakefileCreationData.DEFAULT_JAVA_TARGET_PROPERTY);
			result |= ((actualValue != null && !actualValue.equals(copyValue)) || (actualValue == null && copyValue == null));
			actualValue = project.getPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER,
					MakefileCreationData.TARGET_EXECUTABLE_PROPERTY));
			copyValue = tempStorage.getString(MakefileCreationData.TARGET_EXECUTABLE_PROPERTY);
			result |= ((actualValue != null && !actualValue.equals(copyValue)) || (actualValue == null && copyValue == null));
			actualValue = project.getPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER,
					MakefileCreationData.GENERATE_START_SH_SCRIPT_PROPERTY));
			copyValue = tempStorage.getString(MakefileCreationData.GENERATE_START_SH_SCRIPT_PROPERTY);
			result |= ((actualValue != null && !actualValue.equals(copyValue)) || (actualValue == null && copyValue == null));
			actualValue = project.getPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER,
					MakefileCreationData.GENERATE_START_BAT_SCRIPT_PROPERTY));
			copyValue = tempStorage.getString(MakefileCreationData.GENERATE_START_BAT_SCRIPT_PROPERTY);
			result |= ((actualValue != null && !actualValue.equals(copyValue)) || (actualValue == null && copyValue == null));
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace(e);
		}
		return result;
	}

	/**
	 * Loads the properties from the property storage, into the user
	 * interface elements.
	 * 
	 * @param project
	 *                the project to load the properties from.
	 * */
	public void loadProperties(final IProject project) {
		String temp;
		final boolean useAbsolutePath = false;
		try {
			temp = project.getPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER,
					MakefileCreationData.DEFAULT_JAVA_TARGET_PROPERTY));
			if (StringUtils.isNullOrEmpty(temp)) {
				defaultJavaTarget.setSelectedValue(MakefileCreationData.DefaultJavaTarget.getDefault().toString());
			} else {
				try {
					defaultJavaTarget.setSelectedValue(MakefileCreationData.DefaultJavaTarget.createInstance(temp).toString());
				} catch (final IllegalArgumentException e) {
					ErrorReporter.INTERNAL_ERROR("Unknown default target in Java target creation tab: " + temp);
				}
			}

			temp = project.getPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER,
					MakefileCreationData.TARGET_EXECUTABLE_PROPERTY));
			if (temp == null) {
				temporalJavaTargetFileFieldEditor.setStringValue(MakefileCreationData.getDefaultJavaTargetName(project, useAbsolutePath));
			} else {
				temporalJavaTargetFileFieldEditor.setStringValue(temp);
			}
			temp = project.getPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER,
					MakefileCreationData.GENERATE_START_SH_SCRIPT_PROPERTY));
			generateStartShScript.setSelection(ProjectBuildPropertyData.TRUE_STRING.equals(temp) ? true : false);
			temp = project.getPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER,
					MakefileCreationData.GENERATE_START_BAT_SCRIPT_PROPERTY));
			generateStartBatScript.setSelection(ProjectBuildPropertyData.TRUE_STRING.equals(temp) ? true : false);
			updateDefaultJavaTarget();
		} catch (CoreException e) {
			performDefaults(project);
		}
	}

	/**
	 * Saves the properties to the property storage, from the user interface
	 * elements.
	 * 
	 * @param project
	 *                the project to save the properties to.
	 * @return true if the save was successful, false otherwise.
	 * */
	public boolean saveProperties(final IProject project) {
		try {
			setProperty(project, MakefileCreationData.DEFAULT_JAVA_TARGET_PROPERTY, defaultJavaTarget.getActualValue());
			setProperty(project, MakefileCreationData.GENERATE_START_SH_SCRIPT_PROPERTY,
					String.valueOf(generateStartShScript.getSelection()));
			setProperty(project, MakefileCreationData.GENERATE_START_BAT_SCRIPT_PROPERTY,
					String.valueOf(generateStartBatScript.getSelection()));
			String temp = temporalJavaTargetFileFieldEditor.getStringValue();
			final URI path = URIUtil.toURI(temp);
			final URI resolvedPath = TITANPathUtilities.resolvePathURI(temp, project.getLocation().toOSString());
			if (path.equals(resolvedPath)) {
				temp = PathUtil.getRelativePath(project.getLocation().toOSString(), temp);
			}
			setProperty(project, MakefileCreationData.TARGET_EXECUTABLE_PROPERTY, temp);
			if (isExecutableSelected()) {
				if (AntScriptGenerator.generateAndStoreBuildXML(project)) {
					AntLaunchConfigGenerator.createAntLaunchConfiguration(project);
					AntLaunchConfigGenerator.addAntBuilder(project);
					project.refreshLocal(IResource.DEPTH_ONE, null);
				}
			}
			AntLaunchConfigGenerator.setAntBuilderEnabled(project, isExecutableSelected());
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace(e);
			return false;
		}
		return true;
	}

	public void performDefaults(final IProject project) {
		defaultJavaTarget.setSelectedValue(MakefileCreationData.DefaultJavaTarget.getDefault().toString());
		temporalJavaTargetFileFieldEditor.setStringValue(MakefileCreationData.getDefaultJavaTargetName(project, false));
		generateStartShScript.setSelection(MakefileCreationData.GENERATE_START_SH_SCRIPT_DEFAULT);
		generateStartBatScript.setSelection(MakefileCreationData.GENERATE_START_BAT_SCRIPT_DEFAULT);
		updateDefaultJavaTarget();
	}

	/**
	 * Sets the provided value, on the provided project, for the provided
	 * property.
	 * 
	 * @param project
	 *                the project to work on.
	 * @param name
	 *                the name of the property to change.
	 * @param value
	 *                the value to set.
	 * 
	 * @exception CoreException
	 *                    if this method fails. Reasons include:
	 *                    <ul>
	 *                    <li>This project does not exist.</li>
	 *                    <li>This project is not local.</li>
	 *                    <li>This project is a project that is not open.</li>
	 *                    <li>Resource changes are disallowed during certain
	 *                    types of resource change event notification. See
	 *                    <code>IResourceChangeEvent</code> for more
	 *                    details.</li>
	 *                    </ul>
	 * */
	private void setProperty(final IProject project, final String name, final String value) throws CoreException {
		final QualifiedName qualifiedName = new QualifiedName(ProjectBuildPropertyData.QUALIFIER, name);
		final String oldValue = project.getPersistentProperty(qualifiedName);
		if (value != null && !value.equals(oldValue)) {
			project.setPersistentProperty(qualifiedName, value);
		}
	}

	/**
	 * Enables or disables the textbox of Java target and the checkboxes according to the selected value.
	 * If Class is selected, the textbox is disabled.
	 * If Executable is selected, the checkboxes are enabled.
	 */
	private void updateDefaultJavaTarget() {
		temporalJavaTargetFileFieldEditor.setEnabled(true, javaTargetComposite);
		generateStartShScript.setEnabled(false);
		generateStartBatScript.setEnabled(false);
		if (isClassSelected()) {
			temporalJavaTargetFileFieldEditor.setEnabled(false, javaTargetComposite);
			return;
		}
		if (isExecutableSelected()) {
			generateStartShScript.setEnabled(true);
			generateStartBatScript.setEnabled(true);
		}
	}

	private boolean isClassSelected() {
		return defaultJavaTarget.getActualValue().equals(MakefileCreationData.DefaultJavaTarget.CLASS.toString());
	}

	private boolean isExecutableSelected() {
		return defaultJavaTarget.getActualValue().equals(MakefileCreationData.DefaultJavaTarget.EXECUTABLE.toString());
	}
}
