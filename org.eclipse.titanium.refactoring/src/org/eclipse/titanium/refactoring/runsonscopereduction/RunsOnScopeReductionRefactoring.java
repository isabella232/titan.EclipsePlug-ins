/******************************************************************************
 * Copyright (c) 2000-2019 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titanium.refactoring.runsonscopereduction;

import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Definition;
import org.eclipse.titan.designer.parsers.GlobalParser;
import org.eclipse.titan.designer.preferences.PreferenceConstants;
import org.eclipse.titan.designer.productUtilities.ProductConstants;

/**
 * This class represents the 'Runs on scope reduction' refactoring operation.
 *
 * @author Farkas Izabella Ingrid
 *
 */
public class RunsOnScopeReductionRefactoring extends Refactoring{

	private static final String ONTHEFLYANALAYSISDISABLED = "The on-the-fly analysis is disabled, there is semantic information present to work on";
	public static final String PROJECTCONTAINSERRORS = "The project `{0}'' contains errors, which might corrupt the result of the refactoring";
	private static final String PROJECTCONTAINSTTCNPPFILES = "The project `{0}'' contains .ttcnpp files, which might corrupt the result of the refactoring";

	private Object[] affectedObjects; // look at creatChange
	private final IStructuredSelection selection;
	private final Set<IProject> projects = new HashSet<IProject>();
	private final Definition defSelection;

	public RunsOnScopeReductionRefactoring(final IStructuredSelection selection) {
		this.selection = selection;
		this.defSelection = null;

		final Iterator<?> it = selection.iterator();
		while (it.hasNext()) {
			final Object o = it.next();
			if (o instanceof IResource) {
				final IProject temp = ((IResource) o).getProject();
				projects.add(temp);
			}
		}
	}

	public RunsOnScopeReductionRefactoring(final Definition selection) {
		this.defSelection = selection;
		this.selection = null;
	}

	public Object[] getAffectedObjects() {
		return affectedObjects;
	}

	///METHODS FROM REFACTORING

	@Override
	public String getName() {
		return "Runs on scope reduction modifiers";
	}

	@Override
	public RefactoringStatus checkInitialConditions(final IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		final RefactoringStatus result = new RefactoringStatus();
		try{
			pm.beginTask("Checking preconditions...", 2);

			final IPreferencesService prefs = Platform.getPreferencesService();//PreferenceConstants.USEONTHEFLYPARSING
			if (! prefs.getBoolean(ProductConstants.PRODUCT_ID_DESIGNER, PreferenceConstants.USEONTHEFLYPARSING, false, null)) {
				result.addError(ONTHEFLYANALAYSISDISABLED);
			}

			// check that there are no ttcnpp files in the project
			for (final IProject project : projects) {
				if (GlobalParser.hasTtcnppFiles(project)) {
					result.addError(MessageFormat.format(PROJECTCONTAINSTTCNPPFILES, project));
				}
			}
			pm.worked(1);

			// check that there are no error markers in the project
			for (final IProject project : projects) {
				final IMarker[] markers = project.findMarkers(null, true, IResource.DEPTH_INFINITE);
				for (final IMarker marker : markers) {
					if (IMarker.SEVERITY_ERROR == marker.getAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR)) {
						result.addError(MessageFormat.format(PROJECTCONTAINSERRORS, project));
						break;
					}
				}
			}
			pm.worked(1);

		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace(e);
			result.addFatalError(e.getMessage());
		} finally {
			pm.done();
		}
		return result;
	}

	@Override
	public RefactoringStatus checkFinalConditions(final IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		return new RefactoringStatus();
	}

	@Override
	public Change createChange(final IProgressMonitor pm) throws CoreException, OperationCanceledException {
		if (selection != null) {
			final CompositeChange cchange = new CompositeChange("RunsOnScopeRefactoring");
			final Iterator<?> it = selection.iterator();
			while (it.hasNext()) {
				final Object o = it.next();
				if (!(o instanceof IResource)) {
					continue;
				}
				final IResource res = (IResource)o;
				final ResourceVisitor vis = new ResourceVisitor();
				res.accept(vis);
				cchange.add(vis.getChange());
			}
			affectedObjects = cchange.getAffectedObjects();
			return cchange;
		} else {
			// a single definition selected
			final CompositeChange cchange = new CompositeChange("RunsOnScopeRefactoring");
			final IResource file = defSelection.getLocation().getFile();
			if (!(file instanceof IFile)) {
				ErrorReporter.logError("RunsOnScopeReductionRefactoring.createChange(): File container of defSelection is not an IFile! ");
			}

			final ChangeCreator chCreator = new ChangeCreator((IFile)file, defSelection);
			chCreator.perform();
			final Change ch = chCreator.getChange();
			if (ch != null) {
				cchange.add(ch);
				this.affectedObjects = ch.getAffectedObjects();
			} else {
				this.affectedObjects = new Object[]{};
			}
			return cchange;
		}
	}

	//METHODS FROM REFACTORING END

	/**
	 * Visits all the files of a folder or project (any {@link IResource}).
	 * Creates the {@link Change} for all files and then merges them into a single
	 * {@link CompositeChange}.
	 * <p>
	 * Call on any {@link IResource} object.
	 *  */
	private class ResourceVisitor implements IResourceVisitor {

		private final CompositeChange change;

		public ResourceVisitor() {
			this.change = new CompositeChange("RunsOnScopeRefactoring");
		}

		private CompositeChange getChange() {
			return change;
		}

		@Override
		public boolean visit(final IResource resource) throws CoreException {
			if (resource instanceof IFile) {
				final ChangeCreator chCreator = new ChangeCreator((IFile)resource);
				chCreator.perform(); //this work
				final Change ch = chCreator.getChange();
				if (ch != null) {
					change.add(ch);
				}
				//SKIP //TODO: see this option is validate
				//return false;
			}
			//CONTINUE
			return true;
		}

	}
}
