/******************************************************************************
 * Copyright (c) 2000-2017 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.refactoring.ungroup;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.titan.common.logging.ErrorReporter;

/**
 * @author Nagy Mátyás
 * */
public class UngroupModuleparHeadless {

	private final IStructuredSelection selection;

	public UngroupModuleparHeadless(final IStructuredSelection selection) {
		this.selection = selection;
	}

	public void run() {
		final UngroupModuleparRefactoring refactoring = new UngroupModuleparRefactoring(selection);
		try {
			final Change change = refactoring.createChange(null);
			change.perform(new NullProgressMonitor());
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace(e);
		}
	}



}
