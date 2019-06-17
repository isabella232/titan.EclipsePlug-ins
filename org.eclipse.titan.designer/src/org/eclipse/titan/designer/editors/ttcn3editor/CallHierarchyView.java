/******************************************************************************
 * Copyright (c) 2000-2019 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors.ttcn3editor;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.graphics.ImageCache;
import org.eclipse.titan.designer.productUtilities.ProductConstants;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Event;

/**
 * <p>
 * The <code>CallHierarchyView</code> class represent the view for the call hierarchy data visualization.
 * <p>
 * 
 * @see ViewPart
 * @see ISelectionChangedListener
 * @author Sándor Bálazs
 */
public final class CallHierarchyView extends ViewPart implements ISelectionChangedListener {
	/**
	 * The <code>treeViewer</code> visualize the call hierarchy graph state
	 * and the call hierarchy search results.
	 * @see TreeViewer
	 */
	private TreeViewer treeViewer;

	/**
	 * The <code>tableViewer</code> visualize the selected call hierarchy node references.
	 * @see TableViewer
	 */
	private TableViewer tableViewer;

	/**
	 * Used by {@link #tableViewer}.
	 * @see TableViewer
	 */
	private Table table;

	/**
	 * The content provider for the {@link #treeViewer}.
	 * @see CallHierarchyContentProvider
	 */
	private CallHierarchyContentProvider contentProvider;

	/**
	 * The label provider for the {@link #treeViewer} and the {@link #tableViewer}.
	 * @see CallHierarchyLabelProvider
	 */
	private CallHierarchyLabelProvider labelProvider;

	/**
	 * The <code>statusLineManager</code> is part of the view.<br>
	 * Setting in: {@link #createPartControl(Composite)}
	 * @see #createPartControl(Composite)
	 * @see IStatusLineManager
	 */
	private IStatusLineManager statusLineManager;

	/**
	 * The <code>messageLabel</code> is a Label for show status informations.
	 * Use in: {@link #setMessage(String)}
	 * @see #setMessage(String)
	 */
	private Label messageLabel;

	/**
	 * The {@link CallHierarchy} contains the in the call hierarchy view used search algorithms implementation.
	 * @see CallHierarchy
	 */
	private CallHierarchy callHierarchy;

	/**
	 * The current selected {@link CallHierarchyNode} in the {@link #treeViewer}.
	 * @see #treeViewer
	 * @see CallHierarchyNode
	 */
	private CallHierarchyNode treeViewerSelectedNode;

	/**
	 * The currently focused view.<br>
	 * Possible values: </code>TREE_VIEWER<code> or <code>TABLE_VIEWEVR</code>.
	 */
	private int focused;

	/**
	 * The new focused view.<br>
	 * Possible values: </code>TREE_VIEWER<code> or <code>TABLE_VIEWEVR</code>.
	 */
	private int inFocus;

	/**
	 * The {@link #tableViewer}'s headers.
	 */
	private final String columnHeaders[] = {"", "Line", "Call"};

	/**
	 * The {@link #tableViewer}'s column layouts.
	 */
	private final ColumnLayoutData columnLayouts[] = {
			new ColumnPixelData(18, false, true),
			new ColumnWeightData(60),
			new ColumnWeightData(300)
	};

	/**
	 * The <code>CallHierarchyView</code>'s view id.<br>
	 * Usage: {@link #showView()}
	 */
	public static final String  viewID = "org.eclipse.titan.designer.editors.ttcn3editor.CallHierarchyView";

	private static final String INITIAL_MESSAGE 			= "To display the call hierarchy, select one function or testcase\n"
			+ "and choose the 'Open Call Hierarchy' menu option or press Ctrl+Alt+H.";
	private static final String CALLING_IN_PROJECT			= "\"{0}\" calls in project: \"{1}\"."; 
	private static final String EDITOR_OPEN_ERROR			= "The new editor can not open!";
	private static final int    TREE_VIEWER					= 0;
	private static final int    TABLE_VIEWEVR				= 1;
	private static final int    STATUS_LINE_LEVEL_MESSAGE 	= 0;
	private static final int    STATUS_LINE_LEVEL_ERROR 	= 1;

	/**
	 * The <code>CallHierarchyView</code>'s constructor.<br>
	 * Initializations:<br>
	 *    - {@link CallHierarchy}<br>
	 *    - {@link CallHierarchyContentProvider}<br>
	 *    - {@link CallHierarchyLabelProvider}<br>
	 */
	public CallHierarchyView() {
		callHierarchy 	= new CallHierarchy();
		contentProvider = new CallHierarchyContentProvider(this.callHierarchy);
		labelProvider 	= new CallHierarchyLabelProvider();
		inFocus 		= TREE_VIEWER;
		focused 		= TREE_VIEWER;
	}

	/**
	 * This static method can show a {@link CallHierarchyView}.<br>
	 * @return
	 * 			Return the opened {@link CallHierarchyView}<br>
	 * 			Return <b>NULL</b> if showing the view is not success.
	 */
	public static CallHierarchyView showView() {
		IViewPart viewPart = null;

		try {
			viewPart = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(viewID);
		} catch (PartInitException e) {
			return null;
		}

		if(viewPart == null) {
			return null;
		}

		if(!(viewPart instanceof CallHierarchyView)) {
			return null;
		}

		final CallHierarchyView view = (CallHierarchyView) viewPart;

		return view;
	}

	/**
	 * Set the {@link #treeViewer} input.<br>
	 * Initialize the {@link #tableViewer} to empty.<br>
	 * Set the {@link #treeViewerSelectedNode} to the root.<br>
	 * Set the focus to the {@link #treeViewer}.
	 * @param node
	 * 			The new root {@link CallHierarchyNode} for the {@link #treeViewer}.
	 */
	public void setInput(final CallHierarchyNode node) {
		treeViewer.setInput(node);
		treeViewer.refresh();

		final Object[] EMPTY = {};
		tableViewer.setInput(EMPTY);

		treeViewerSelectedNode = node;
		setMessage(MessageFormat.format(CALLING_IN_PROJECT, callHierarchy.getSelectedAssignment().getFullName(), callHierarchy.getCurrentProject().getName()));
		treeViewer.getControl().setFocus();
	}

	/**
	 * This method set up the {@link CallHierarchyView}.<br>
	 * Set the focus to the {@link #treeViewer}.
	 * @param parent
	 * 			The parent {@link Composite} of the new {@link CallHierarchyView}.
	 */
	@Override
	public void createPartControl(final Composite parent) {
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		gridLayout.makeColumnsEqualWidth = true;
		parent.setLayout(gridLayout);

		final GridData gridDataForLabel = new GridData();
		gridDataForLabel.horizontalAlignment = GridData.FILL;
		gridDataForLabel.grabExcessHorizontalSpace = true;
		gridDataForLabel.minimumWidth = 100;

		messageLabel = new Label(parent, SWT.WRAP);
		messageLabel.setLayoutData(gridDataForLabel);
		messageLabel.setAlignment(SWT.LEFT);
		messageLabel.setText(INITIAL_MESSAGE);

		final GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessVerticalSpace = true;

		final SashForm splitter = new SashForm(parent, SWT.NONE);
		splitter.setLayoutData(gridData);

		setUpTreeViewer(splitter, gridData);

		table = new Table(splitter, SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER);
		setUpTableViewer(table);

		statusLineManager = getViewSite().getActionBars().getStatusLineManager();
		callHierarchy.setStatusLineManager(statusLineManager);

		treeViewer.getControl().setFocus();
	}

	/**
	 * Set up the {@link #treeViewer}.
	 *
	 * @param parent
	 * 			The {@link #treeViewer}'s parent {@link Composite}.
	 * @param gridData
	 * 			The {@link GridData} for the {@link #treeViewer}.
	 */
	private void setUpTreeViewer(final Composite parent, final GridData gridData) {
		treeViewer = new TreeViewer(parent, SWT.BORDER | SWT.MULTI);
		treeViewer.getTree().setLayoutData(gridData);
		treeViewer.setContentProvider(contentProvider);
		treeViewer.setLabelProvider(labelProvider);
		treeViewer.setAutoExpandLevel(2);
		treeViewer.addSelectionChangedListener(this);
	}

	/**
	 * Set up the {@link #tableViewer}.
	 *
	 * @param table
	 * 		The {@link Table} for the {@link #tableViewer}.
	 */
	private void setUpTableViewer(final Table table) {
		tableViewer = new TableViewer(table);
		tableViewer.setContentProvider(new ArrayContentProvider());
		tableViewer.setLabelProvider(new CallHierarchyLabelProvider());
		tableViewer.addSelectionChangedListener(this);

		final TableLayout tableLayout = new TableLayout();
		tableViewer.getTable().setLayout(tableLayout);
		tableViewer.getTable().setHeaderVisible(true);

		final ArrayList<TableColumn> tableColumns = new ArrayList<TableColumn>();
		for (int i = 0; i < columnHeaders.length; i++) {
			tableLayout.addColumnData(columnLayouts[i]);
			final TableColumn tc = new TableColumn(table, SWT.NONE,i);
			tc.setResizable(columnLayouts[i].resizable);
			tc.setText(columnHeaders[i]);
			tableColumns.add(tc);
		}

		final Listener sortListener = new Listener() {
			public void handleEvent(final Event e) {
				int sortDirection = table.getSortDirection();

				Object[] references = treeViewerSelectedNode.getReferences();
				if(references == null)  {
					return;
				}

				if(sortDirection == SWT.UP) {
					sortDirection = SWT.DOWN;
					Arrays.sort(references, getReferenceComparator.reversed());
				} else {
					sortDirection = SWT.UP;
					Arrays.sort(references, getReferenceComparator);
				}

				table.setSortDirection(sortDirection);
				tableViewer.setInput(references);
			}
		};

		tableColumns.get(1).addListener(SWT.Selection, sortListener);
		table.setSortColumn(tableColumns.get(1));
		table.setSortDirection(SWT.UP);
	}

	/**
	 * The {@link CallHierarchyView}'s focus handler.<br>
	 * Use:<br>
	 * - {@link #focused}<br>
	 * - {@link #inFocus}
	 */
	@Override
	public void setFocus() {
		if(focused == inFocus) {
			return;
		}
		if(inFocus == TREE_VIEWER) {
			focused = TREE_VIEWER;
			treeViewer.getControl().setFocus();
		}
		if(inFocus == TABLE_VIEWEVR) {
			focused = TABLE_VIEWEVR;
			tableViewer.getControl().setFocus();
		}
	}

	/**
	 * This is a {@link Comparator} for the {@link Reference} type.
	 *
	 * @return
	 * 		The reference comparator.
	 */
	public final Comparator<Object> getReferenceComparator = new Comparator<Object>() {
		@Override
		public int compare(final Object object1, final Object object2) {
			final Reference reference1 = (Reference) object1;
			final Reference reference2 = (Reference) object2;
			if (reference1.getLocation().getLine() < reference2.getLocation().getLine()) {
				return -1;
			}
			if (reference1.getLocation().getLine() > reference2.getLocation().getLine()) {
				return 1;
			}

			return 0;
		}
	}; 

	/**
	 * The selectionChanged method for the {@link ISelectionChangedListener}.
	 *
	 * @param event
	 * 		The {@link SelectionChangedEvent}.
	 */
	@Override
	public void selectionChanged(final SelectionChangedEvent event) {
		final Location location = getEventLocation(event);
		if (location == null) {
			return;
		}

		final IFile selectedFile = (IFile) location.getFile().getAdapter(IFile.class);
		boolean isFileAlredyOpened = false;
		IEditorReference openedEditor = null;

		final IEditorReference[] editorReferences = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getEditorReferences();
		for (int i = 0; i < editorReferences.length; i++) {
			if(editorReferences[i].getName().equals(selectedFile.getName())) {
				isFileAlredyOpened = true;
				openedEditor = editorReferences[i];
				break;
			}
		}

		if(isFileAlredyOpened) {
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().bringToTop(openedEditor.getPart(true));
		} else {
			final IEditorInput input = new FileEditorInput(selectedFile);
			try {
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(input, ProductConstants.PRODUCT_ID_DESIGNER + ".editors.ttcn3editor.TTCN3Editor");
			} catch (PartInitException e) {
				showStatusLineMessage(EDITOR_OPEN_ERROR, STATUS_LINE_LEVEL_ERROR);
				return;
			}
			showView();
		}

		selectLocation(location);
	}

	/**
	 * Select a {@link Location} in the current opened editor!<br>
	 * <b>Important:</b> The {@link Location} must be in the current opened editor.
	 * @param location
	 * 			The selected {@link Location}.
	 */
	private void selectLocation(final Location location)  {
		final IEditorPart targetEditor 	= PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		final ITextEditor editor 			= (ITextEditor) targetEditor;
		editor.selectAndReveal(location.getOffset(), location.getEndOffset() - location.getOffset());
	}

	/**
	 * Return {@link Location} of a {@link CallHierarchyNode} or a {@link Reference} from a {@link SelectionChangedEvent}.
	 * @param event
	 * 			A {@link SelectionChangedEvent} with a {@link CallHierarchyNode} or a {@link Reference}.
	 * @return
	 * 			A {@link Location}.
	 */
	private Location getEventLocation(final SelectionChangedEvent event) {
		final ISelection selection = event.getSelection();
		if (selection.isEmpty()) {
			return null;
		}

		final Object selectedElement = ((IStructuredSelection) selection).getFirstElement();

		Location location = null;
		if (selectedElement instanceof CallHierarchyNode) {
			treeViewerSelectedNode = (CallHierarchyNode) selectedElement;

			location = treeViewerSelectedNode.getNodeDefinition().getLocation();

			final Object[] references = treeViewerSelectedNode.getReferences();

			final int sortDirection = table.getSortDirection();
			if(sortDirection == SWT.UP) {
				Arrays.sort(references, getReferenceComparator);
			} else {
				Arrays.sort(references, getReferenceComparator.reversed());
			}

			tableViewer.setInput(references);

			inFocus = TREE_VIEWER;
		}

		if (selectedElement instanceof Reference) {
			final Reference reference = (Reference) selectedElement;
			location = reference.getLocation();
			inFocus = TABLE_VIEWEVR;
		}

		return location;
	}

	/**
	 * Get the used {@link CallHierarchy} object.
	 * @return
	 * 			The used {@link CallHierarchy} object.
	 */
	public CallHierarchy getCallHierarchy() {
		return this.callHierarchy;
	}

	/**
	 * Set the {@link #messageLabel}'s text.
	 * @param message
	 * 			The {@link #messageLabel}'s text.
	 */
	public void setMessage(final String message) {
		messageLabel.setText(message);
		messageLabel.getParent().layout();
	}

	/**
	 * Set the {@link #messageLabel}'s visible.
	 * @param visible
	 * 			The {@link #messageLabel}'s visible.
	 */
	public void setMessageVisible(final boolean visible) {
		messageLabel.setVisible(visible);
		messageLabel.getParent().layout();
	}

	/**
	 * Show message on the target editors status bar.<br>
	 * The message level is automatically STATUS_LINE_LEVEL_MESSAGE.
	 * 
	 * @see #showStatusLineMessage(String, int)
	 * @param message
	 * 			The string of the message.
	 */
	public void showStatusLineMessage(final String message) {
		showStatusLineMessage(message, STATUS_LINE_LEVEL_MESSAGE);
	}

	/**
	 * Show message on the target editors status bar.<br>
	 * The message level possible ERROR OR MESSAGE. The level define by the level parameter.
	 * 
	 * @param message
	 * 			The string of the message.
	 * @param level
	 * 			The level of message.<br>
	 * 			Possible: STATUS_LINE_LEVEL_MESSAGE or STATUS_LINE_LEVEL_ERROR
	 */
	public void showStatusLineMessage(final String message, final int level) {
		if(statusLineManager == null) {
			return;
		}
		statusLineManager.setErrorMessage(null);

		if(level == STATUS_LINE_LEVEL_MESSAGE) {
			statusLineManager.setMessage(ImageCache.getImage("titan.gif"), message);
		}

		if(level == STATUS_LINE_LEVEL_ERROR) {
			statusLineManager.setMessage(ImageCache.getImage("compiler_error_fresh.gif"), message);
		}
	}
}