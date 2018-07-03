/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titanium.refactoring.organize;

import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.Activator;
import org.eclipse.titan.designer.editors.ttcn3editor.TTCN3Editor;
import org.eclipse.titanium.refactoring.Utils;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * This class handles the {@link OrganizeImportsRefactoring} class when the operation is
 * called from the package browser for a single or multiple project(s), folder(s) or file(s).
 * <p>
 * {@link #execute(ExecutionEvent)} is called by the UI (see plugin.xml).
 *
 * @author Farkas Izabella Ingrid
 */
public class OrganizeImportsFromBrowser extends AbstractHandler implements IObjectActionDelegate {
	private ISelection selection;
	
	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		selection = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage().getSelection();
		performOrganizeImport();
		return null;
	}
	
	@Override
	public void run(final IAction action) {
		performOrganizeImport();
	}

	@Override
	public void selectionChanged(final IAction action, final ISelection selection) {
		this.selection = selection;
	}

	@Override
	public void setActivePart(final IAction action, final IWorkbenchPart targetPart) {
		// intentionally empty
	}
	
	private void performOrganizeImport() {
		// getting the active editor
		final TTCN3Editor targetEditor = Utils.getActiveEditor();
		//find selection
		if (!(selection instanceof IStructuredSelection)) {
			return;
		}
		final IStructuredSelection structSelection = (IStructuredSelection)selection;
		final Set<IProject> projsToUpdate = Utils.findAllProjectsInSelection(structSelection);

		//update AST before refactoring
		Utils.updateASTBeforeRefactoring(projsToUpdate, "OrganizeImports");
		Activator.getDefault().pauseHandlingResourceChanges();

		//create refactoring
		final OrganizeImportsRefactoring refactoring = new OrganizeImportsRefactoring(structSelection);
		//open wizard
		final OrganizeImportsWizard wiz = new OrganizeImportsWizard(refactoring);
		final RefactoringWizardOpenOperation operation = new RefactoringWizardOpenOperation(wiz);
		try {
			operation.run(targetEditor == null ? null : targetEditor.getEditorSite().getShell(), "");
		} catch (InterruptedException irex) {
			// operation was cancelled
		} catch (Exception e) {
			ErrorReporter.logError("OrganizeImportsFromBrowser: Error while performing refactoring change! ");
			ErrorReporter.logExceptionStackTrace(e);
		}
		Activator.getDefault().resumeHandlingResourceChanges();

		//update AST after refactoring
		Utils.updateASTAfterRefactoring(wiz, refactoring.getAffectedObjects(), refactoring.getName());
	}

}
