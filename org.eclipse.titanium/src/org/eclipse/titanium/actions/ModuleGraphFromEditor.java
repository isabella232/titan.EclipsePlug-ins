/******************************************************************************
 * Copyright (c) 2000-2020 Ericsson Telecom AB
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
import org.eclipse.swt.widgets.Display;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.editors.ttcn3editor.TTCN3Editor;
import org.eclipse.titan.designer.editors.ttcnppeditor.TTCNPPEditor;
import org.eclipse.titan.designer.parsers.GlobalParser;
import org.eclipse.titan.designer.parsers.ProjectSourceParser;
import org.eclipse.titanium.error.ErrorHandler;
import org.eclipse.titanium.error.GUIErrorHandler;
import org.eclipse.titanium.graph.components.NodeDescriptor;
import org.eclipse.titanium.graph.gui.windows.ModuleGraphEditor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;

/**
 *
 * @author Hoang Le My Anh
 *
 */
public final class ModuleGraphFromEditor extends AbstractHandler{

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		final IEditorPart editor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();

		if (editor == null || !(editor instanceof TTCN3Editor || editor instanceof TTCNPPEditor)) {
			ErrorReporter.logError("The editor is not found or not a Titan TTCN-3 editor");
			return null;
		}

		final IFile file = (IFile) editor.getEditorInput().getAdapter(IFile.class);
		final ProjectSourceParser projectSourceParser = GlobalParser.getProjectSourceParser(file.getProject());
		final Module actualModule = projectSourceParser.containedModule(file);
		final IProject project = file.getProject();

		final Generator generator = new Generator(project, actualModule);
		generator.schedule();

		return null;
	}


	/**
	 * Generate graph and color node.
	 * */
	public static class Generator extends Job {
		private final IProject project;
		private Module actualModule;

		// Constructor
		Generator(final IProject project, final Module actualModule) {
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
						}

						if (editor == null) {
							//an external editor was opened
							return;
						}

						// Get the selected node and color it
						final ModuleGraphEditor actualEditor = (ModuleGraphEditor) editor;
						NodeDescriptor foundNode = null;

						for (final NodeDescriptor node : actualEditor.getGraph().getVertices()) {
							if (node.getName().equals(actualModule.getName().toString())) {
								foundNode = node;
							}
						}

						if (foundNode != null) {
							final NodeDescriptor tempNode = foundNode;
							Display.getDefault().asyncExec(new Runnable() {
								@Override
								public void run() {
									try {
										// Set a color for the selected node
										actualEditor.elemChosen(tempNode);
									} catch (Exception exc) {
										final ErrorHandler errorHandler = new GUIErrorHandler();
										errorHandler.reportException("Error while setting color node", exc);
									}
								}
							});
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
