/******************************************************************************
 * Copyright (c) 2000-2021 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titanium.actions;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.parsers.GlobalParser;
import org.eclipse.titan.designer.parsers.ProjectSourceParser;
import org.eclipse.titanium.actions.ModuleGraphFromEditor.Generator;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 *
 * @author Hoang Le My Anh
 *
 */
public class ModuleGraphFromBrowser extends AbstractHandler implements IObjectActionDelegate {
	private ISelection selection;

	public ModuleGraphFromBrowser() {
		// Do nothing
	}

	@Override
	public void run(final IAction action) {
		doModuleGraphFromBrowser();
	}

	@Override
	public void selectionChanged(final IAction action, final ISelection selection) {
		this.selection = selection;
	}

	@Override
	public void setActivePart(final IAction action, final IWorkbenchPart targetPart) {
		//Do nothing
	}

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		selection = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage().getSelection();

		doModuleGraphFromBrowser();

		return null;
	}

	/**
	 * Do open the module graph view and selected node from browser
	 * */
	private void doModuleGraphFromBrowser() {
		if (!(selection instanceof IStructuredSelection)) {
			return;
		}

		final IStructuredSelection structSelection = (IStructuredSelection) selection;

		for (final Object selectedFile : structSelection.toList()) {
			final IProject project = ((IResource) selectedFile).getProject();
			final ProjectSourceParser projectSourceParser = GlobalParser.getProjectSourceParser(((IResource) selectedFile).getProject());
			final Module actualModule = projectSourceParser.containedModule((IFile) selectedFile);

			final Generator generator = new Generator(project, actualModule);
			generator.schedule();
		}
	}
}
