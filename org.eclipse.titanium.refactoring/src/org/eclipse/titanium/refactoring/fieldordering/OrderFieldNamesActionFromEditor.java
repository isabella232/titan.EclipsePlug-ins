/******************************************************************************
 * Copyright (c) 2000-2021 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titanium.refactoring.fieldordering;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.Activator;
import org.eclipse.titan.designer.editors.ttcn3editor.TTCN3Editor;
import org.eclipse.titan.designer.parsers.GlobalParser;

import org.eclipse.titanium.refactoring.Utils;

/**
 * This class handles the {@link OrderFieldNamesRefactoring} class when the operation is
 * called from the editor for a single module.
 * <p>
 * {@link #execute(ExecutionEvent)} is called by the UI (see plugin.xml).
 *
 * @author Zsolt Tabi
 */
public class OrderFieldNamesActionFromEditor extends AbstractHandler {

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {

		//update AST
		Utils.updateASTForProjectActiveInEditor("OrderFieldNames");
		Activator.getDefault().pauseHandlingResourceChanges();

		// getting the active editor
		final TTCN3Editor targetEditor = Utils.getActiveEditor();
		if (targetEditor == null) {
			return null;
		}
		//getting selected file
		final IFile selectedFile = Utils.getSelectedFileInEditor("OrderFieldNames");
		if (selectedFile == null) {
			return null;
		}

		final IStructuredSelection structSelection = new StructuredSelection(selectedFile);
		final OrderFieldNamesRefactoring refactoring = new OrderFieldNamesRefactoring(structSelection);

		//open wizard
		final OrderFieldNamesWizard wiz = new OrderFieldNamesWizard(refactoring);
		final RefactoringWizardOpenOperation operation = new RefactoringWizardOpenOperation(wiz);
		try {
			operation.run(targetEditor.getEditorSite().getShell(), "");
		} catch (InterruptedException irex) {
			// operation was cancelled
		} catch (Exception e) {
			ErrorReporter.logError("OrderFieldNamesActionFromEditor: Error while performing refactoring change! ");
			ErrorReporter.logExceptionStackTrace(e);
		}

		//update AST again
		Activator.getDefault().resumeHandlingResourceChanges();

		final IProject project = selectedFile.getProject();
		GlobalParser.getProjectSourceParser(project).reportOutdating(selectedFile);
		GlobalParser.getProjectSourceParser(project).analyzeAll();

		return null;
	}

}
