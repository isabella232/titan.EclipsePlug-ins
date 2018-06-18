/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.executor.tabpages.testset;

import java.util.Iterator;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;

/**
 * @author Kristof Szabados
 * */
public final class KnownTestcasesDragSourceListener extends DragSourceAdapter {
	private final TableViewer viewer;

	public KnownTestcasesDragSourceListener(final TableViewer viewer) {
		this.viewer = viewer;
	}

	@Override
	public void dragSetData(final DragSourceEvent event) {
		if (TestcaseTransfer.getInstance().isSupportedType(event.dataType)) {
			final IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();

			if (!selection.isEmpty()) {
				final TestCaseTreeElement[] testcases = new TestCaseTreeElement[selection.size()];
				int i = 0;
				for (Iterator<?> it = selection.iterator(); it.hasNext();) {
					testcases[i] = (TestCaseTreeElement) it.next();
					i++;
				}
				event.data = testcases;
			}
		}
	}

	@Override
	public void dragStart(final DragSourceEvent event) {
		event.doit = !viewer.getSelection().isEmpty();
	}
}
