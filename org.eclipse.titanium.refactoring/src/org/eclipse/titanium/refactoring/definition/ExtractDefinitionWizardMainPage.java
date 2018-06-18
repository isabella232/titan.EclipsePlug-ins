/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titanium.refactoring.definition;

import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;

/**
 * Wizard page #1: edit the name of the new project.
 *
 * @author Viktor Varga
 */
public class ExtractDefinitionWizardMainPage extends WizardNewProjectCreationPage {

	public ExtractDefinitionWizardMainPage(final String pageName) {
		super(pageName);
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

}
