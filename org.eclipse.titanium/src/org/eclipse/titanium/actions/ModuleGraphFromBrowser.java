/******************************************************************************
 * Copyright (c) 2000-2019 Ericsson Telecom AB
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
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.parsers.GlobalParser;
import org.eclipse.titan.designer.parsers.ProjectSourceParser;
import org.eclipse.titanium.error.ErrorHandler;
import org.eclipse.titanium.error.GUIErrorHandler;
import org.eclipse.titanium.graph.components.NodeDescriptor;
import org.eclipse.titanium.graph.gui.windows.ModuleGraphEditor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.part.FileEditorInput;

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

		for (Object selectedFile : structSelection.toList()) {
			final IProject project = ((IResource) selectedFile).getProject();
			final ProjectSourceParser projectSourceParser = GlobalParser.getProjectSourceParser(((IResource) selectedFile).getProject());
			final Module actualModule = projectSourceParser.containedModule((IFile) selectedFile);

			final Generator generator = new Generator(project, actualModule);
			generator.schedule();
		}
	}

	/**
	 * Generate graph and color node.
	 * */
	private static class Generator extends Job {
		private final IProject project;
		private Module actualModule;

		// Constructor
		Generator(final IProject project, Module actualModule) {
			super("Generator");
			this.project = project;
			this.actualModule = actualModule;
		}

		@Override
		protected IStatus run(final IProgressMonitor monitor) {
			monitor.beginTask("Parsing project", 30);
			IFile input = null;
			try {
				IResource[] members = project.members();
				for (final IResource res : members) {
					if (res.getType() == IResource.FILE) {
						input = (IFile) res;
						break;
					}
					if (res.getType() == IResource.FOLDER) {
						members = ((IFolder) res).members();
					}
				}
			} catch (CoreException ce) {
				final ErrorHandler errorHandler = new GUIErrorHandler();
				errorHandler.reportException("Error while parsing the project", ce);
			}

			final IFile finalInput = input;

			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					try {
						// Get a selected file in the project
						final IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
						final FileEditorInput editorInput = new FileEditorInput(finalInput);
						IEditorPart editor = page.findEditor(editorInput);

						if (editor == null) {
							// Generate the graph
							editor = page.openEditor(editorInput, ModuleGraphEditor.ID, true, IWorkbenchPage.MATCH_ID
									| IWorkbenchPage.MATCH_INPUT);

							// Get the selected node and color it
							final ModuleGraphEditor actualEditor = (ModuleGraphEditor) editor;

							for (final NodeDescriptor node : actualEditor.getGraph().getVertices()) {
								if (node.getName().equals(actualModule.getName().toString())) {

									Display.getDefault().asyncExec(new Runnable() {
										@Override
										public void run() {
											try {
												// Set a color for the selected node
												actualEditor.elemChosen(node);
											} catch (Exception exc) {
												final ErrorHandler errorHandler = new GUIErrorHandler();
												errorHandler.reportException("Error while setting color node", exc);
											}
										}
									});
								}
							}
						}

					} catch (Exception exc) {
						final ErrorHandler errorHandler = new GUIErrorHandler();
						errorHandler.reportException("Error while selecting the node", exc);
					}
				}
			});
			monitor.done();
			return Status.OK_STATUS;
		}
	}
}
