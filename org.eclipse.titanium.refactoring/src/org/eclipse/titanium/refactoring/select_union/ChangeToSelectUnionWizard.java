package org.eclipse.titanium.refactoring.select_union;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.ui.refactoring.RefactoringWizard;

/**
 * Wizard for the 'Change union to select union' refactoring operation.
 *
 * @author Mate Kovacs
 */
public class ChangeToSelectUnionWizard extends RefactoringWizard implements
		IExecutableExtension {

	private static final String WIZ_WINDOWTITLE = "Change union to select union";

	ChangeToSelectUnionWizard(final Refactoring refactoring) {
		super(refactoring, DIALOG_BASED_USER_INTERFACE);
	}

	@Override
	public void setInitializationData(final IConfigurationElement config, final String propertyName, final Object data) throws CoreException {
	}

	@Override
	protected void addUserInputPages() {
		setDefaultPageTitle(WIZ_WINDOWTITLE);
	}

}
