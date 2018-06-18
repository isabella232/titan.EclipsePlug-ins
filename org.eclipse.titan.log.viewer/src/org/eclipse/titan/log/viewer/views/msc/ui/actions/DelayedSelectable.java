/*******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/

package org.eclipse.titan.log.viewer.views.msc.ui.actions;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;

public interface DelayedSelectable extends ISelectionChangedListener {
	ISelection getDelayedSelection();

	void setDelayedSelection(ISelection delayedSelection);

	void selectionChanged(ISelection selection);

	public void run();
}
