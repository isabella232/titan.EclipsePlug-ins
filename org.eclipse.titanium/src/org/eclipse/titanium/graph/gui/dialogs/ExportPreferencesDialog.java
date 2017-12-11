/******************************************************************************
 * Copyright (c) 2000-2017 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.graph.gui.dialogs;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titanium.graph.visualization.GraphHandler.ImageExportType;
import org.eclipse.titanium.swt.SWTResourceManager;
//TODO
/**
 * This class implements the export window to be shown on exporting graph to an image file
 * @author Gabor Jenei
 * @author Balazs Maitz
 */
// FIXME misleading class name: we are not exporting preferences
public class ExportPreferencesDialog extends Dialog {

	protected ImageExportType result = ImageExportType.EXPORT_SEEN_GRAPH;
	protected Shell shlExportGraph;
	private Combo mode;

	/**
	 * Create the dialog.
	 * 
	 * @param parent
	 * @param style
	 */
	public ExportPreferencesDialog(final Shell parent) {
		super(parent);
		setText("Export Graph");
	}

	/**
	 * Open the dialog.
	 * 
	 * @return The set preferences in a string array
	 */
	public ImageExportType open() {
		createContents();
		shlExportGraph.open();
		//shlExportGraph.layout();
		final Display display = getParent().getDisplay();
		while (!shlExportGraph.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return result;
	}

	/**
	 * Create contents of the dialog.
	 */
	private void createContents() {
		shlExportGraph = new Shell(getParent(), SWT.BORDER | SWT.TITLE | SWT.PRIMARY_MODAL);
		shlExportGraph.setImage(SWTResourceManager.getImage("resources/icons/sample.gif"));
		shlExportGraph.setLayout(new GridLayout(1, false));
		shlExportGraph.setText("Export Preferences");

		Composite selectWhatToSave = new Composite(shlExportGraph, SWT.NONE);
		selectWhatToSave.setLayout(new FillLayout());
		final Label lblPartToExport = new Label(selectWhatToSave, SWT.NONE);
		lblPartToExport.setText("Part to export:");

		mode = new Combo(selectWhatToSave, SWT.READ_ONLY);
		mode.setItems(new String[] { "Whole graph", "Only the seen part", "The satellite view" });
		mode.select(0);

		final Button btnOk = new Button(shlExportGraph, SWT.PUSH);

		btnOk.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				switch (mode.getSelectionIndex()) {
				case 0:
					result = ImageExportType.EXPORT_WHOLE_GRAPH;
					break;
				case 1:
					result = ImageExportType.EXPORT_SEEN_GRAPH;
					break;
				case 2:
					result = ImageExportType.EXPORT_SATELLITE;
					break;
				default:
					ErrorReporter.logError("unexpected selection index " + mode.getSelectionIndex());
					result = ImageExportType.EXPORT_SEEN_GRAPH;
					break;
				}

				if (!shlExportGraph.isDisposed()) {
					shlExportGraph.close();
				}
			}
		});

		btnOk.setText("OK");
		btnOk.setFocus();
		shlExportGraph.pack();
	}
}
