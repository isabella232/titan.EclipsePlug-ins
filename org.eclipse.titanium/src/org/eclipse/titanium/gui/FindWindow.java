/******************************************************************************
 * Copyright (c) 2000-2017 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.gui;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.titanium.error.GUIErrorHandler;
import org.eclipse.titanium.swt.SWTResourceManager;

/**
 * This class implements a node searching Eclipse window, it is inherited from
 * {@link Dialog}
 * 
 * @author Gabor Jenei
 * @author Balazs Maitz
 */
@SuppressWarnings("rawtypes")
public class FindWindow<T extends Comparable> extends Dialog {

	protected Shell shlFind;
	private static final Dimension FIND_DIALOG_DEFAULT_SIZE = new Dimension(490, 265); // min size
	private final Searchable<T> view;
	private final Collection<T> totalSet;
	private Label lblResults;
	private Tree tree;
	private final SortedSet<T> treeItems;
	protected final GUIErrorHandler errorHandler = new GUIErrorHandler(); 

	/**
	 * Create the dialog.
	 * 
	 * @param parent
	 *            : The parent shell
	 * @param view
	 *            : reference to the opener editor window
	 * @param totalList
	 * 			  : The whole set to search on
	 * @exception On illegal arguments
	 */
	public FindWindow(final Shell parent, final Searchable<T> view, final Collection<T> totalList) throws IllegalArgumentException {
		super(parent);
		if (view==null) {
			throw new IllegalArgumentException("The totalList parameter of FindWindow's constructor mustn't be null!");
		}
		if (totalList==null) {
			throw new IllegalArgumentException("The totalList parameter of FindWindow's constructor mustn't be null!");
		}
		
		this.view = view;
		treeItems = new TreeSet<T>();
		this.totalSet = totalList;
		setText("Find");
	}

	/**
	 * Open the dialog.
	 */
	public void open() {
		createContents();
		shlFind.open();
		
		final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		shlFind.setLocation(new Point(screenSize.width / 2, 20));

		final Display display = getParent().getDisplay();
		while (!shlFind.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		view.clearResults();
		shlFind.pack();
	}

	/**
	 * Closing the dialog
	 */
	public void close() {
		view.clearResults();
		if (!shlFind.isDisposed()) {
			shlFind.close();
		}
	}

	/**
	 * Create contents of the dialog.
	 */
	private void createContents() {
		
		shlFind = new Shell(getParent(), SWT.CLOSE | SWT.TITLE | SWT.RESIZE);
		shlFind.setModified(true);
		shlFind.setImage(SWTResourceManager.getImage("resources/icons/search_src.gif"));
		shlFind.setSize(FIND_DIALOG_DEFAULT_SIZE.width, FIND_DIALOG_DEFAULT_SIZE.height);
		shlFind.setText("Find");
		shlFind.setMaximized(true);
		shlFind.setLayout(new GridLayout(1,false));
		
		shlFind.setMinimumSize(FIND_DIALOG_DEFAULT_SIZE.width,FIND_DIALOG_DEFAULT_SIZE.height);
		
		final Label nameLabel = new Label(shlFind, SWT.NONE);
		nameLabel.setText("Name: ");
		
		final Text text = new Text(shlFind, SWT.BORDER);
		text.setLayoutData(new GridData(SWT.FILL,SWT.NONE, false, false));
		text.setSize(120, 10);
		
		FillLayout fl = new FillLayout();
		
		Composite buttonsAndCheckBoxes = new Composite(shlFind, SWT.NONE);
		buttonsAndCheckBoxes.setLayout(fl);
		buttonsAndCheckBoxes.setLayoutData(new GridData(SWT.FILL,SWT.NONE, true, false));		
		
		Composite checkBoxes = new Composite(buttonsAndCheckBoxes, SWT.NONE);
	    GridLayout cbLayout = new GridLayout(1, false);
	    cbLayout.verticalSpacing = 15;
		Composite buttons = new Composite(buttonsAndCheckBoxes, SWT.RIGHT_TO_LEFT);
	    GridLayout btLayout = new GridLayout(1, false);
	    btLayout.verticalSpacing = 5;
	    
		checkBoxes.setLayout(cbLayout);	
		buttons.setLayout(btLayout);

		final Button btnExactMatch = new Button(checkBoxes, SWT.CHECK);
		btnExactMatch.setText("Exact match");
		
		final Button btnCaseSensitive = new Button(checkBoxes, SWT.CHECK);
		btnCaseSensitive.setText("Case sensitive");
		
		final Button btnFind = new Button(buttons, SWT.PUSH);
		btnFind.setText("Find");

		final Button btnClearResult = new Button(buttons, SWT.PUSH);
		btnClearResult.setText("Clear result");
		lblResults = new Label(shlFind, SWT.NONE);
		lblResults.setText("Results:");
		
		tree = new Tree(shlFind, SWT.BORDER);
		tree.setLayoutData(new GridData(SWT.FILL,SWT.FILL, true, true));
		
		tree.addSelectionListener(new SelectionAdapter() {
			@SuppressWarnings("unchecked")
			@Override
			public void widgetSelected(final SelectionEvent e) {
				view.elemChosen((T) tree.getSelection()[0].getData());
			}
		});
		
		btnFind.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				
				String name = text.getText();
				final boolean exactMatch = btnExactMatch.getSelection();
				final boolean caseSensitive = btnCaseSensitive.getSelection();
				boolean noResult = true;
				
				if (!caseSensitive) {
					name = name.toLowerCase();
				}

				for (final T actElem : totalSet) {
					String elemName = actElem.toString();
					if (!caseSensitive) {
						elemName = elemName.toLowerCase();
					}
					
					if (!exactMatch && elemName.contains(name)) {
						treeItems.add(actElem);
					} else if (exactMatch && elemName.equals(name)) {
						treeItems.add(actElem);
					}
				}
				
				for (final T actElem : treeItems) {
					final TreeItem item = new TreeItem(tree, SWT.NONE);
					item.setText(actElem.toString());
					item.setData(actElem);
					noResult = false;
				}

				if (noResult) {
					errorHandler.reportInformation("The search hasn't found such node!");
				}
			}
		});

		btnClearResult.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				tree.removeAll();
				treeItems.clear();
				view.clearResults();
			}
		});
		
		shlFind.pack();
	}
}
