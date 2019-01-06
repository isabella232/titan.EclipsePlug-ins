/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors.referenceSearch;

import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.ISearchResult;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.productUtilities.ProductConstants;

/**
 * @author Szabolcs Beres
 * */
public class ReferenceSearchQuery implements ISearchQuery {

	private final ReferenceSearchResult result;

	private final Module module;
	private final IProject project;
	private final ReferenceFinder referenceFinder;

	public ReferenceSearchQuery(final ReferenceFinder rf, final Module module, final IProject project) {
		this.result = new ReferenceSearchResult(this);
		this.module = module;
		this.project = project;
		this.referenceFinder = rf;

	}

	@Override
	public IStatus run(final IProgressMonitor monitor) {
		result.removeAll();

		final Map<Module, List<Hit>> map = referenceFinder.findAllReferences(module, project, monitor, false);
		for (final Map.Entry<Module, List<Hit>> entry : map.entrySet()) {
			final IResource resource = entry.getKey().getLocation().getFile();
			if (!(resource instanceof IFile)) {
				continue;
			}

			for (final Hit hit : entry.getValue()) {
				result.addMatch(new ReferenceSearchMatch(hit.identifier));
			}
		}

		monitor.done();
		return new Status(IStatus.OK, ProductConstants.PRODUCT_ID_DESIGNER, IStatus.OK, "Search done.", null);
	}

	@Override
	public String getLabel() {
		return "TITAN Reference Search";
	}

	@Override
	public boolean canRerun() {
		return true;
	}

	@Override
	public boolean canRunInBackground() {
		return true;
	}

	@Override
	public ISearchResult getSearchResult() {
		return result;
	}

	public String getSearchPattern() {
		return referenceFinder.getSearchName();
	}
}
