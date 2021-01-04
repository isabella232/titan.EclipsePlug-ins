/******************************************************************************
 * Copyright (c) 2000-2021 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors.configeditor.pages.execute;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Text;
import org.eclipse.titan.common.path.PathConverter;
import org.eclipse.titan.designer.consoles.TITANDebugConsole;
import org.eclipse.titan.designer.preferences.PreferenceConstants;
import org.eclipse.titan.designer.productUtilities.ProductConstants;

/**
 * @author Kristof Szabados
 * */
public final class ExternalCommandBrowser implements SelectionListener {

	private final Text field;
	private final IFile actualFile;

	public ExternalCommandBrowser(final Text field, final IFile actualFile) {
		this.field = field;
		this.actualFile = actualFile;
	}

	@Override
	public void widgetDefaultSelected(final SelectionEvent e) {
		//Do nothing
	}

	@Override
	public void widgetSelected(final SelectionEvent e) {
		final boolean reportDebugInformation = Platform.getPreferencesService().getBoolean(ProductConstants.PRODUCT_ID_DESIGNER,
				PreferenceConstants.DISPLAYDEBUGINFORMATION, false, null);

		final FileDialog dialog = new FileDialog(field.getShell());
		String file = field.getText();
		if (file != null && !"".equals(file)) {
			file = file.substring(1, file.length() - 1);

			final String filename = PathConverter.getAbsolutePath(actualFile.getLocation().toOSString(), file);
			dialog.setFilterPath(filename);
			dialog.setFileName(filename);
		}

		file = dialog.open();
		if (file != null) {
			final StringBuilder output = new StringBuilder();
			final String filename = PathConverter.convert(file, reportDebugInformation, output);
			TITANDebugConsole.println(output);
			field.setText("\"" + filename + "\"");
		}
	}

}
