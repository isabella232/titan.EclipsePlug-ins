/******************************************************************************
 * Copyright (c) 2000-2019 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors.ttcn3editor.actions;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

/**
 * <p>
 * This class define the "Open Call Hierarchy" action in the TTCN3 editors right click menu.<br>
 * The class show the necessary view and run the reference search when the user trigger the action.
 * </p>
 * @see CallHierarchy
 * @see CallHierarchyView
 * @see CallHierarchyNode
 * @see org.eclipse.ui.IEditorActionDelegate
 * @see org.eclipse.core.commands.AbstractHandler
 * @author Sándor Bálazs
 * */
public class CallHierarchyAction extends AbstractHandler implements IEditorActionDelegate {

	@Override
	public void run(IAction action) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		// TODO Auto-generated method stub
		
	}

}
