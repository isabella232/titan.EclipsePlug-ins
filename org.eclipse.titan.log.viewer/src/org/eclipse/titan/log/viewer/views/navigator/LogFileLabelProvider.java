/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.views.navigator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.log.viewer.Activator;
import org.eclipse.titan.log.viewer.utils.Constants;
import org.eclipse.ui.model.WorkbenchLabelProvider;

public class LogFileLabelProvider extends LabelProvider {
	@Override
	public String getText(final Object element) {
		String defaultText = WorkbenchLabelProvider.getDecoratingWorkbenchLabelProvider().getText(element);
		if (!(element instanceof IFile)) {
			return defaultText;
		}

		if (isExtractionRunningOn((IFile) element)) {
			return "[Extracting testcases] " + defaultText;
		}
		return defaultText;
	}

	@Override
	public Image getImage(final Object element) {
		Image defaultImage = WorkbenchLabelProvider.getDecoratingWorkbenchLabelProvider().getImage(element);

		if (!(element instanceof IFile)) {
			return defaultImage;
		}

		if (isExtractionRunningOn((IFile) element)) {
			return Activator.getDefault().getIcon(Constants.ICONS_HOUR_GLASS);
		}

		return defaultImage;
	}

	private boolean isExtractionRunningOn(final IFile file) {
		try {
			Object temp = file.getSessionProperty(Constants.EXTRACTION_RUNNING);
			return temp != null && (Boolean) temp;
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace(e);
		}
		return false;
	}
}
