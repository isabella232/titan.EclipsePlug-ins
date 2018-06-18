/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.refactoring;

import org.eclipse.ltk.ui.refactoring.RefactoringWizard;

/**
 * @author Adam Delic
 * */
public class RenameRefactoringWizard extends RefactoringWizard {
	public RenameRefactoringWizard(final RenameRefactoring r) {
		super(r, DIALOG_BASED_USER_INTERFACE);
	}

	@Override
	protected void addUserInputPages() {
		setDefaultPageTitle(getRefactoring().getName());
		final RenameRefactoringInputPage page = new RenameRefactoringInputPage(getRefactoring().getName());
		addPage(page);
	}
}
