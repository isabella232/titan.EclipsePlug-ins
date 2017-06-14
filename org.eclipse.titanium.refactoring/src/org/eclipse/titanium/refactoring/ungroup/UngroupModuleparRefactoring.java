/******************************************************************************
 * Copyright (c) 2000-2017 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.refactoring.ungroup;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

/**
 * This class represents the 'Ungroup module parameters' refactoring operation.
 * <p>
 * This refactoring operation ungroup all grouped modulepar in the given
 *   files/folders/projects, which are contained in a {@link IStructuredSelection} object.
 *
 * @author Nagy Mátyás
 */
public class UngroupModuleparRefactoring extends Refactoring {
	
	private final IStructuredSelection selection;
	private final Set<IProject> projects = new HashSet<IProject>();

	private Object[] affectedObjects;		//the list of objects affected by the change
	
	public UngroupModuleparRefactoring(final IStructuredSelection selection) {
		this.selection = selection;

		final Iterator<?> it = selection.iterator();
		while (it.hasNext()) {
			final Object o = it.next();
			if (o instanceof IResource) {
				final IProject temp = ((IResource) o).getProject();
				projects.add(temp);
			}
		}
	}

	public Object[] getAffectedObjects() {
		return affectedObjects;
	}

	//METHODS FROM REFACTORING


	@Override
	public String getName() {
		return "Ungroup module parameters";
	}

	@Override
	public RefactoringStatus checkInitialConditions(final IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		return new RefactoringStatus();
	}

	@Override
	public RefactoringStatus checkFinalConditions(final IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		return new RefactoringStatus();
	}
	
	@Override
	public Change createChange(final IProgressMonitor pm) throws CoreException, OperationCanceledException {
		if (selection == null) {
			return null;
		}
		final CompositeChange cchange = new CompositeChange("UngroupModuleparRefactoring");
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
			this.change = new CompositeChange("UngroupModuleparRefactoring");;
		}

		private CompositeChange getChange() {
			return change;
		}

		@Override
		public boolean visit(final IResource resource) throws CoreException {
			if (resource instanceof IFile) {
				final ChangeCreator chCreator = new ChangeCreator((IFile)resource);
				chCreator.perform();
				final Change ch = chCreator.getChange();
				if (ch != null) {
					change.add(ch);
				}
				//SKIP
				return false;
			}
			//CONTINUE
			return true;
		}

	}
}
