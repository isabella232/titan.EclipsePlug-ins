/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titanium.refactoring.function;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.ui.refactoring.RefactoringWizard;

/**
 * Wizard for the 'Extract to function' operation.
 *
 * @author Viktor Varga
 */
public class ExtractToFunctionWizard extends RefactoringWizard implements IExecutableExtension {

	private static final String WIZ_WINDOWTITLE = "Extract selected code into new function";

	private final ExtractToFunctionRefactoring refactoring;

	public ExtractToFunctionWizard(final Refactoring refactoring) {
		super(refactoring, DIALOG_BASED_USER_INTERFACE);
		this.refactoring = (ExtractToFunctionRefactoring)refactoring;
	}


	@Override
	public void setInitializationData(final IConfigurationElement config,
			final String propertyName, final Object data) throws CoreException {
	}

	@Override
	protected void addUserInputPages() {
		setDefaultPageTitle(WIZ_WINDOWTITLE);
		final ExtractToFunctionWizardFuncNamePage funcNamePage = new ExtractToFunctionWizardFuncNamePage(WIZ_WINDOWTITLE);
		addPage(funcNamePage);
		final ExtractToFunctionWizardParamsPage paramsPage = new ExtractToFunctionWizardParamsPage(WIZ_WINDOWTITLE, refactoring.getWizardModelProvider());
		addPage(paramsPage);
	}
}
