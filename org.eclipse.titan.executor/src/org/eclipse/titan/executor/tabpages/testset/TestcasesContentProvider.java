/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.executor.tabpages.testset;

import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.titan.executor.executors.ITreeBranch;
import org.eclipse.titan.executor.executors.ITreeLeaf;

/**
 * @author Kristof Szabados
 * */
public final class TestcasesContentProvider implements ITreeContentProvider {

	@Override
	public void dispose() {
		// Do nothing
	}

	@Override
	public ITreeLeaf[] getChildren(final Object parentElement) {
		if (parentElement instanceof TestsetTreeElement) {
			List<ITreeLeaf> temp = ((TestsetTreeElement) parentElement).children();
			return temp.toArray(new ITreeLeaf[temp.size()]);
		}
		return new ITreeLeaf[] {};
	}

	@Override
	public ITreeLeaf[] getElements(final Object inputElement) {
		return getChildren(inputElement);
	}

	@Override
	public ITreeBranch getParent(final Object element) {
		if (element instanceof TestCaseTreeElement) {
			return ((TestCaseTreeElement) element).parent();
		}
		return null;
	}

	@Override
	public boolean hasChildren(final Object element) {
		return element instanceof TestsetTreeElement
				&& !((TestsetTreeElement) element).children().isEmpty();
	}

	@Override
	public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
		// Do nothing
	}

}
