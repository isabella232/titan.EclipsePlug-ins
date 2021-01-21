/******************************************************************************
 * Copyright (c) 2000-2021 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.actions.runconfiggenerator;

import java.text.MessageFormat;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.titan.common.path.PathUtil;

public class FileNameDialog extends TitleAreaDialog {
	
	private static String SHELL_TEXT = "Save run configuration for Eclipse Titan";
	private static String TITLE_TEXT = "You can save the run configration for {0} mode";
	private static String HELP_TEXT = "Give name for the run configuration";
	private static String ERROR_TEXT = "Invalid file name";
	private static String LABEL_TEXT = "File name: ";
	private static String OVERWRITE_TEXT = "Overwrite if exists?";
	
	private Text fileNameText;
	private Button overwriteButton;
	
	private String selectedFileName = "";
	private String defaultFileName = "";
	private boolean isOverwite = false;
	/**
	 * False -> Single mode
	 * True -> Parallel mode
	 */
	private boolean mode = false;

	public FileNameDialog(Shell parent) {
		super(parent);
		setHelpAvailable(false);
	}
	
	@Override
	/** {@inheritDoc} */
    public void create() {
        super.create();
        setTitle(MessageFormat.format(TITLE_TEXT, mode ? "parallel" : "single"));
        setMessage(HELP_TEXT, IMessageProvider.INFORMATION);
    }
	
	@Override
	/** {@inheritDoc} */
    protected Control createDialogArea(Composite parent) {
        Composite area = (Composite) super.createDialogArea(parent);
        Composite container = new Composite(area, SWT.NONE);
        container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        GridLayout layout = new GridLayout(1, false);
        container.setLayout(layout);

        createFileNameText(container);
        createOverwriteCheckBox(container);

        return area;
    }
	
	@Override
	/** {@inheritDoc} */
    protected Point getInitialSize() {
        return new Point(500, 300);
    }
	
	@Override
	/** {@inheritDoc} */
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(SHELL_TEXT);
    }
	
	private void createFileNameText(Composite container) {
        Label fileNameLabel = new Label(container, SWT.NONE);
        fileNameLabel.setText(LABEL_TEXT);

        GridData dataFileName = new GridData();
        dataFileName.grabExcessHorizontalSpace = true;
        dataFileName.horizontalAlignment = GridData.FILL;

        fileNameText = new Text(container, SWT.BORDER);
        fileNameText.setLayoutData(dataFileName);
        fileNameText.setText(defaultFileName);
        fileNameText.addListener(SWT.Modify, new Listener() {
            public void handleEvent(Event e) {
                String string = fileNameText.getText();
                Button okButton = getButton(IDialogConstants.OK_ID);
                if (!PathUtil.isValidFileName(string)) {
                	setMessage(ERROR_TEXT, IMessageProvider.ERROR);
                	if (okButton != null)
                		okButton.setEnabled(false);
                } else {
                	setMessage(HELP_TEXT, IMessageProvider.INFORMATION);
                	if (okButton != null)
                		okButton.setEnabled(true);
                }
              }
            });
	}

	private void createOverwriteCheckBox(Composite container) {
		overwriteButton = new Button(container, SWT.CHECK);
		overwriteButton.setText(OVERWRITE_TEXT);
	}

	@Override
	/** {@inheritDoc} */
	protected boolean isResizable() {
		return true;
	}

	/**
	 * Sets the default file name to show
	 * 
	 * @param defaultFileName default file name
	 */
	public void setDefaultFileName(String defaultFileName) {
		this.defaultFileName = defaultFileName;
	}

	/**
	 * Returns the default file name
	 * 
	 * @return default file name
	 */
	public String getDefaultFileName() {
		return defaultFileName;
	}
	
	private void saveInput() {
		selectedFileName = fileNameText.getText();
        isOverwite = overwriteButton.getSelection();
    }

	@Override
	/** {@inheritDoc} */
    protected void okPressed() {
        saveInput();
        super.okPressed();
    }

	/**
	 * Returns the filled file name
	 * 
	 * @return the filled file name
	 */
    public String getFirstName() {
        return selectedFileName;
    }

    /**
     * Returns whether the user wants to overwrite
     * the existing run configuration or not 
     * 
     * @return overwrite or not
     */
    public boolean getOverwrite() {
        return isOverwite;
    }

	/**
	 * Returns the mode of the run configuration.
	 * <li> False -> Single mode
	 * <li> True -> Parallel mode
	 * 
	 * @return the mode
	 */
	public boolean getMode() {
		return mode;
	}

	/**
	 * Sets the mode of the run configuration.
	 * <li> False -> Single mode
	 * <li> True -> Parallel mode
	 * 
	 * @param mode the mode to set
	 */
	public void setMode(boolean mode) {
		this.mode = mode;
	}

}
