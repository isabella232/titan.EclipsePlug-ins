/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titanium.refactoring.modulepar;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;

/**
 * Wizard page #1: edit the name of the new project.
 *
 * @author Viktor Varga
 */
public class ExtractModuleParWizardMainPage extends WizardNewProjectCreationPage {

	private boolean saveModuleParsOption = false;

	public ExtractModuleParWizardMainPage(final String pageName) {
		super(pageName);
	}

	public boolean getSaveModuleParsOption() {
		return saveModuleParsOption;
	}


	@Override
	public void createControl(final Composite parent) {
		super.createControl(parent);
		final Control control = getControl();
		if (!(control instanceof Composite)) {
			ErrorReporter.logError("ExtractModuleParWizardMainPage: Control is not of Composite type. ");
			return;
		}

		final Composite composite = (Composite)control;
		final Button chb_saveModulePars = new Button(composite, SWT.CHECK);
		chb_saveModulePars.setText("Save a list of the module parameters into a text file");
		chb_saveModulePars.addSelectionListener(new CHBSelectionListener());
	}

	@Override
	protected boolean validatePage() {
		if (!super.validatePage()) {
			return false;
		}

		final String projectName = getProjectName();
		if (!projectName.matches("[a-zA-Z0-9[_-]]*")) {
			setErrorMessage("Invalid project name");
			return false;
		}

		setErrorMessage(null);
		return true;
	}


	/**
	 * Listens to the 'Save module parameters to a text file' checkbox.
	 * */
	private class CHBSelectionListener implements SelectionListener {

		@Override
		public void widgetSelected(final SelectionEvent e) {
			if (!(e.getSource() instanceof Button)) {
				return;
			}

			final Button checkBox = (Button)e.getSource();
			saveModuleParsOption = checkBox.getSelection();
		}

		@Override
		public void widgetDefaultSelected(final SelectionEvent e) {

		}

	}

}
