/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.properties.pages;

import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * @author Kristof Szabados
 * */
public class OptionElementContentProvider implements ITreeContentProvider {

	@Override
	public Object[] getElements(final Object inputElement) {
		return getChildren(inputElement);
	}

	@Override
	public void dispose() {
		// Nothing to do
	}

	@Override
	public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
		// Nothing to do
	}

	@Override
	public Object[] getChildren(final Object parentElement) {
		if (parentElement instanceof OptionElement) {
			List<OptionElement> children = ((OptionElement) parentElement).children;
			if (children == null) {
				return new Object[0];
			}
			return children.toArray();
		}

		return new Object[0];
	}

	@Override
	public Object getParent(final Object element) {
		if (element instanceof OptionElement) {
			return ((OptionElement) element).parent;
		}

		return null;
	}

	@Override
	public boolean hasChildren(final Object element) {
		return getChildren(element).length > 0;
	}

}
