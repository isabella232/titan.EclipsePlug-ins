/******************************************************************************
 * Copyright (c) 2000-2017 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.refactoring.insertfield;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.ui.refactoring.RefactoringWizard;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Definition;

/**
 * Wizard for the 'Insert field modifiers' refactoring operation.
 *
 * @author Bianka Bekefi
 */

public class InsertFieldWizard extends RefactoringWizard implements IExecutableExtension {

	private static final String WIZ_WINDOWTITLE = "Insert field modifiers";

	private final InsertFieldRefactoring refactoring;

	private final Definition selection;

	InsertFieldWizard(final Refactoring refactoring, final Definition selection) {
		super(refactoring, RefactoringWizard.DIALOG_BASED_USER_INTERFACE);
		this.refactoring = (InsertFieldRefactoring) refactoring;
		this.selection = selection;
	}

	@Override
	public void setInitializationData(final IConfigurationElement config, final String propertyName, final Object data) throws CoreException {

	}

	@Override
	protected void addUserInputPages() {
		setDefaultPageTitle(WIZ_WINDOWTITLE);
		final InsertFieldWizardInputPage optionsPage = new InsertFieldWizardInputPage(WIZ_WINDOWTITLE, refactoring.getSettings(), selection);
		addPage(optionsPage);
	}

}
