/******************************************************************************
 * Copyright (c) 2000-2019 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.properties.pages;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.Activator;
import org.eclipse.titan.designer.core.TITANJavaBuilder;
import org.eclipse.titan.designer.properties.PropertyNotificationManager;
import org.eclipse.titan.designer.properties.data.ProjectBuildPropertyData;
import org.eclipse.titan.designer.properties.data.ProjectDocumentHandlingUtility;
import org.eclipse.titan.designer.wizards.projectFormat.TITANAutomaticProjectExporter;
import org.eclipse.ui.dialogs.PropertyPage;

/**
 * @author Kristof Szabados
 * */
public class ProjectJavaBuildPropertyPage extends PropertyPage {
	public static final String BUILDER_IS_ENABLED = "This TITAN project has the TITAN Java builder enabled.";
	public static final String BUILDER_IS_NOT_ENABLED = "This TITAN project has the TITAN Java builder disabled.";

	private Composite pageComposite;
	private Label headLabel;
	private TabFolder makefileOperationsTabFolder;

	private InternalJavaCreationTab internalMakefileCreationTab;

	private final PreferenceStore tempStorage;
	private IProject projectResource;

	private ConfigurationManagerControl configurationManager;
	private String firstConfiguration;

	public ProjectJavaBuildPropertyPage() {
		super();
		tempStorage = new PreferenceStore();
	}

	@Override
	public void dispose() {
		headLabel.dispose();
		internalMakefileCreationTab.dispose();
		makefileOperationsTabFolder.dispose();
		pageComposite.dispose();
		super.dispose();
	}

	protected void copyPropertyStore() {
		internalMakefileCreationTab.copyPropertyStore(projectResource, tempStorage);
	}

	protected void copyProjectPersistentProperty(final String propertyName) {
		String temp = null;
		try {
			temp = projectResource.getPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER, propertyName));
		} catch (CoreException ce) {
			ErrorReporter.logExceptionStackTrace(ce);
		}
		if (temp != null) {
			tempStorage.setValue(propertyName, temp);
		}
	}

	/**
	 * Returns true if changes happened to the property given by its name.
	 * 
	 * @param propertyName
	 *                the name of the property to be evaluated.
	 * @return true if the value of the property has changed.
	 */
	protected boolean evaluatePersistentProperty(final String propertyName) {
		String actualValue = null;
		String copyValue = null;
		try {
			actualValue = projectResource.getPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER, propertyName));
		} catch (CoreException ce) {
			ErrorReporter.logExceptionStackTrace(ce);
		}
		copyValue = tempStorage.getString(propertyName);
		return ((actualValue != null && !actualValue.equals(copyValue)) || (actualValue == null && copyValue == null));
	}

	@Override
	public void setVisible(final boolean visible) {
		if (!visible) {
			return;
		}

		if (configurationManager != null) {
			configurationManager.refresh();
		}

		super.setVisible(visible);
	}

	/**
	 * Evaluating PropertyStore changes. If there is a difference between
	 * the temporary storage and the persistent storage the new values will
	 * be saved into the project property file (XML).
	 */
	protected void evaluatePropertyStore() {
		final boolean configurationChanged = !firstConfiguration.equals(configurationManager.getActualSelection());
		if (configurationChanged) {
			PropertyNotificationManager.firePropertyChange(projectResource);
		}
	}

	/**
	 * Handles the change of the active configuration. Sets the new
	 * configuration to be the active one, and loads its settings.
	 * 
	 * @param configuration
	 *                the name of the new configuration.
	 * */
	public void changeConfiguration(final String configuration) {
		configurationManager.changeActualConfiguration();

		//FIXME implement
		copyPropertyStore();
		loadProperties();
		updateContents();
		checkProperties();

		PropertyNotificationManager.firePropertyChange(projectResource);
	}

	@Override
	protected Control createContents(final Composite parent) {
		projectResource = (IProject) getElement();

		pageComposite = new Composite(parent, SWT.NONE);
		final GridLayout pageCompositeLayout = new GridLayout();
		pageCompositeLayout.numColumns = 1;
		pageComposite.setLayout(pageCompositeLayout);
		final GridData pageCompositeGridData = new GridData();
		pageCompositeGridData.horizontalAlignment = GridData.FILL;
		pageCompositeGridData.verticalAlignment = GridData.FILL;
		pageCompositeGridData.grabExcessHorizontalSpace = true;
		pageCompositeGridData.grabExcessVerticalSpace = true;
		pageComposite.setLayoutData(pageCompositeGridData);

		if (TITANJavaBuilder.isBuilderEnabled(projectResource)) {
			headLabel = new Label(pageComposite, SWT.NONE);
			headLabel.setText(BUILDER_IS_ENABLED);
		} else {
			headLabel = new Label(pageComposite, SWT.NONE);
			headLabel.setText(BUILDER_IS_NOT_ENABLED);
		}

		try {
			final String loadLocation = projectResource.getPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER,
					ProjectBuildPropertyData.LOAD_LOCATION));
			if (loadLocation == null) {
				headLabel.setText(headLabel.getText() + "\nWas not yet saved ");
			} else {
				headLabel.setText(headLabel.getText() + "\nWas loaded from " + loadLocation);
			}
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace(e);
		}

		configurationManager = new ConfigurationManagerControl(pageComposite, projectResource);
		configurationManager.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				if (configurationManager.hasConfigurationChanged()) {
					changeConfiguration(configurationManager.getActualSelection());
				}
			}
		});
		firstConfiguration = configurationManager.getActualSelection();

		makefileOperationsTabFolder = new TabFolder(pageComposite, SWT.BORDER);
		
		final GridData makefileOperationsTabFolderGridData = new GridData();
		makefileOperationsTabFolderGridData.horizontalAlignment = GridData.FILL;
		makefileOperationsTabFolderGridData.verticalAlignment = GridData.FILL;
		makefileOperationsTabFolderGridData.grabExcessHorizontalSpace = true;
		makefileOperationsTabFolderGridData.grabExcessVerticalSpace = true;
		makefileOperationsTabFolder.setLayoutData(makefileOperationsTabFolderGridData);

		internalMakefileCreationTab = new InternalJavaCreationTab(projectResource);
		internalMakefileCreationTab.createContents(makefileOperationsTabFolder);

		copyPropertyStore();
		loadProperties();
		updateContents();
		checkProperties();

		return pageComposite;
	}


	protected void updateContents() {


	}

	@Override
	protected void performDefaults() {
		internalMakefileCreationTab.performDefaults();

		configurationManager.saveActualConfiguration();
	}

	@Override
	public boolean performOk() {
		if (!checkProperties()) {
			return false;
		}

		if (!saveProperties()) {
			return false;
		}

		final IPreferenceStore pluginPreferenceStore = Activator.getDefault().getPreferenceStore();
		// setting temporal variables to default for sure
		if (!pluginPreferenceStore.isDefault(MakefileCreationTab.TEMPORAL_TARGET_EXECUTABLE)) {
			pluginPreferenceStore.setToDefault(MakefileCreationTab.TEMPORAL_TARGET_EXECUTABLE);
		}
		if (!pluginPreferenceStore.isDefault(MakeAttributesTab.TEMPORAL_MAKEFILE_SCRIPT)) {
			pluginPreferenceStore.setToDefault(MakeAttributesTab.TEMPORAL_MAKEFILE_SCRIPT);
		}
		if (!pluginPreferenceStore.isDefault(MakeAttributesTab.TEMPORAL_MAKEFILE_FLAGS)) {
			pluginPreferenceStore.setToDefault(MakeAttributesTab.TEMPORAL_MAKEFILE_FLAGS);
		}
		if (!pluginPreferenceStore.isDefault(MakeAttributesTab.TEMPORAL_WORKINGDIRECTORY)) {
			pluginPreferenceStore.setToDefault(MakeAttributesTab.TEMPORAL_WORKINGDIRECTORY);
		}

		configurationManager.saveActualConfiguration();
		ProjectDocumentHandlingUtility.saveDocument(projectResource);

		TITANAutomaticProjectExporter.saveAllAutomatically(projectResource);

		evaluatePropertyStore();
		return true;
	}

	@Override
	public boolean performCancel() {
		loadProperties();
		evaluatePropertyStore();
		configurationManager.clearActualConfiguration();
		return true;
	}

	// Loading persistent property into a GUI element
	public void loadProperty(final String propertyName, final Button button) {
		String temp = "";
		try {
			temp = projectResource.getPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER, propertyName));
		} catch (CoreException ce) {
		}

		button.setSelection(ProjectBuildPropertyData.TRUE_STRING.equals(temp) ? true : false);
	}

	public void loadProperties() {
		internalMakefileCreationTab.loadProperties(projectResource);
	}

	/**
	 * Saving state of GUI element into persistent property.
	 * 
	 * @param propertyName
	 *                the name of the property
	 * @param button
	 *                the button to extract the value from
	 * @return whether the operation was successful or not
	 */
	public boolean saveProperty(final String propertyName, final Button button) {
		final String temp = button.getSelection() ? ProjectBuildPropertyData.TRUE_STRING : ProjectBuildPropertyData.FALSE_STRING;
		try {
			projectResource.setPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER, propertyName), temp);
		} catch (CoreException ce) {
			ErrorReporter.logExceptionStackTrace(ce);
			return false;
		}
		return true;
	}

	/**
	 * Does a general check on the contents of the property pages
	 * 
	 * @return true if no problem was found, false otherwise.
	 * */
	public boolean checkProperties() {
		boolean result = true;
		result &= internalMakefileCreationTab.checkProperties(this);

		return result;
	}

	public boolean saveProperties() {
		boolean success = true;
		// saving properties if checking was successful
		success &= internalMakefileCreationTab.saveProperties(projectResource);

		setErrorMessage(null);
		return success;
	}
}
