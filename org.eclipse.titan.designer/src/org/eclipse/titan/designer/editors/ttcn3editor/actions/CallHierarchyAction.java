/******************************************************************************
 * Copyright (c) 2000-2019 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors.ttcn3editor.actions;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.titan.designer.graphics.ImageCache;
import org.eclipse.titan.designer.editors.ttcn3editor.CallHierarchy;
import org.eclipse.titan.designer.editors.ttcn3editor.CallHierarchyNode;
import org.eclipse.titan.designer.editors.ttcn3editor.CallHierarchyView;

/**
 * <p>
 * This class define the "Open Call Hierarchy" action in the TTCN3 editors right click menu.<br>
 * The class show the necessary view and run the reference search when the user trigger the action.
 * </p>
 * 
 * @see CallHierarchy
 * @see CallHierarchyView
 * @see CallHierarchyNode
 * @see org.eclipse.ui.IEditorActionDelegate
 * @see org.eclipse.core.commands.AbstractHandler
 * @author Sandor Balazs
 * */
public final class CallHierarchyAction extends AbstractHandler implements IEditorActionDelegate {
	/**
	 * Store the current selection from the TTCN3 Editor at the time of the right click action.<br>
	 * Updated in the {@link #selectionChanged(IAction, ISelection)}.
	 */
	private ISelection selection;

	/**
	 * The current editor.<br>
	 * Updated in the {@link #setActiveEditor(IAction, IEditorPart)}<br>
	 * and in the {@link #CallHierarchyAction()}.
	 */
	private IEditorPart targetEditor;

	private static final String SHOW_VIEW_ERROR 				= "The Call Hierarchy view cannot be displayed.";
	private static final String REFERENCE_SEARCH_FAILED 		= "The Call Hierarchy search failed.";
	private static final String STATUS_LINE_MESSAGE_ICON 		= "titan.gif";
	private static final String STATUS_LINE_ERROR_ICON 			= "compiler_error_fresh.gif";
	private static final int    STATUS_LINE_LEVEL_MESSAGE 		= 0;
	private static final int    STATUS_LINE_LEVEL_ERROR 		= 1;
	private static final int 	STATUS_LINE_CLEAR 				= -1;

	/**
	 * The <code>CallHierarchyAction</code> class constructor.<br>
	 * Set the current selection empty and set the target editor.
	 */
	public CallHierarchyAction() {
		selection 		= TextSelection.emptySelection();
		targetEditor 	= PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
	}

	/**
	 * The run method react for the triggered action and call the processing() method.
	 *
	 * @see #processing()
	 * @param action
	 *                The current triggered action.
	 */
	@Override
	public void run(final IAction action) {
		processing(selection);
	}

	/**
	 * Always record the current selection.
	 *
	 * @param action
	 * 			The current triggered action.
	 * @param selection
	 * 			The new selection from the TTCN3 Editor.
	 */
	@Override
	public void selectionChanged(final IAction action, final ISelection currentSelection) {
		selection = currentSelection;
	}

	/**
	 * Set the active targetEditor in this action ant the {@link CallHierarchyView} too.
	 *
	 * @param action
	 * 			The current triggered action.
	 * @param selection
	 * 			The new selection from the TTCN3 Editor.
	 */
	@Override
	public void setActiveEditor(final IAction action, final IEditorPart activeEditor) {
		targetEditor = activeEditor;
	}

	/**
	 * The execute method react for the triggered event and call the processing() method.
	 *
	 * @see #processing()
	 * @param event
	 * 			The execute triggering event.
	 */
	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		processing(selection);
		return null;
	}

	/**
	 * The real processing method.<br>
	 * Show the {@link CallHierarchyView} and run the {@link CallHierarchy#functionCallFinder(ISelection)} reference search method.<br>
	 * Then create the call hierarchy graph root and add the search results to the root and then update the {@link CallHierarchyView}.
	 * Then the method add the current function to the {@link CallHierarchy#addToSearchLog()}.
	 * 
	 * @param currentSelection
	 * 			The selection for the search target from the editor
	 */
	private void processing(final ISelection currentSelection) {
		clearStatusLineMessage();

		final CallHierarchyView callHierarchyView = CallHierarchyView.showView();
		if(callHierarchyView == null) {
			showStatusLineMessage(SHOW_VIEW_ERROR, STATUS_LINE_LEVEL_ERROR);
			return;
		}
		final CallHierarchy callHierarchy = callHierarchyView.getCallHierarchy();
		
		final CallHierarchyNode selectedNode = callHierarchy.functionCallFinder(currentSelection);
		if(selectedNode == null) {
			showStatusLineMessage(REFERENCE_SEARCH_FAILED, STATUS_LINE_LEVEL_ERROR);
			return;
		}
		callHierarchy.addToSearchLog(selectedNode);
		final CallHierarchyNode root = new CallHierarchyNode();
		root.addChild(selectedNode);
		callHierarchyView.setAction(this);
		callHierarchyView.setInput(root);
	}
	
	/**
	 * The processing method for node reprocessing.<br>
	 * Show the {@link CallHierarchyView} and run the {@link CallHierarchy#functionCallFinder()} reference search method.<br>
	 * Then create the call hierarchy graph root and add the search results to the root and then update the {@link CallHierarchyView}.
	 * Then the method add the current function to the {@link CallHierarchy#addToSearchLog()} and set the current selected node in the {@link CallHierarchy#setCurrentNode()}.
	 * 
	 * @param searchabledNode
	 * 			The selection for the search target from the editor
	 */
	public void processing(final CallHierarchyNode searchabledNode) {
		searchabledNode.clearNode();
		clearStatusLineMessage();

		final CallHierarchyView callHierarchyView = CallHierarchyView.showView();
		if(callHierarchyView == null) {
			showStatusLineMessage(SHOW_VIEW_ERROR, STATUS_LINE_LEVEL_ERROR);
			return;
		}
		final CallHierarchy callHierarchy = callHierarchyView.getCallHierarchy();
		
		final CallHierarchyNode selectedNode = callHierarchy.functionCallFinder(searchabledNode);
		if(selectedNode == null) {
			showStatusLineMessage(REFERENCE_SEARCH_FAILED, STATUS_LINE_LEVEL_ERROR);
			return;
		}
		callHierarchy.setCurrentNode(selectedNode);
		callHierarchy.addToSearchLog(selectedNode);
		
		final CallHierarchyNode root = new CallHierarchyNode();
		root.addChild(selectedNode);
		callHierarchyView.setAction(this);
		callHierarchyView.setInput(root);
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
	 * The message level possible ERROR OR MESSAGE. The level define by the level parameter.<br>
	 * With the STATUS_LINE_CLEAR parameter, the method clear the status line.
	 * 
	 * @param message
	 * 			The string of the message.
	 * @param level
	 * 			The level of message.<br>
	 * 			Possible: STATUS_LINE_LEVEL_MESSAGE or STATUS_LINE_LEVEL_ERROR or STATUS_LINE_CLEAR
	 */
	public void showStatusLineMessage(final String message, final int level) {
		if(targetEditor == null) {
			targetEditor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		}

		final IStatusLineManager statusLineManager = targetEditor.getEditorSite().getActionBars().getStatusLineManager();
		if(statusLineManager == null) {
			return;
		}

		statusLineManager.setMessage(null);
		statusLineManager.setErrorMessage(null);

		if(level == STATUS_LINE_LEVEL_MESSAGE) {
			statusLineManager.setMessage(ImageCache.getImage(STATUS_LINE_MESSAGE_ICON), message);
		}

		if(level == STATUS_LINE_LEVEL_ERROR) {
			statusLineManager.setErrorMessage(ImageCache.getImage(STATUS_LINE_ERROR_ICON), message);
		}
	}

	/**
	 * Clear the target editors status bar.<br>
	 * Use: {@link #showStatusLineMessage()}
	 * @see {@link #showStatusLineMessage()}
	 */
	public void clearStatusLineMessage() {
		showStatusLineMessage("", STATUS_LINE_CLEAR);
	}
}