/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.actions;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.titan.log.viewer.parsers.data.TestCase;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.ICommonActionConstants;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;

public class TestCaseActionProvider extends CommonActionProvider {

	private OpenMSCViewAction openTestCaseAction;

	@Override
	public void init(final ICommonActionExtensionSite site) {
		openTestCaseAction = new OpenMSCViewAction();
	}

	@Override
	public void fillActionBars(final IActionBars actionBars) {

		final IStructuredSelection selection = (IStructuredSelection) getContext().getSelection();
		if (selection.size() == 1 && selection.getFirstElement() instanceof TestCase) {
			openTestCaseAction.selectionChanged(null, selection);
			actionBars.setGlobalActionHandler(ICommonActionConstants.OPEN, openTestCaseAction);
		}
	}
}
