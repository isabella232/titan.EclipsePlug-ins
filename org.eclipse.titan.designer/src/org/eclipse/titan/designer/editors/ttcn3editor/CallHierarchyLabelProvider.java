/******************************************************************************
 * Copyright (c) 2000-2019 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors.ttcn3editor;

import org.eclipse.swt.graphics.Image;
import org.eclipse.jface.viewers.LabelProvider;
import java.text.MessageFormat;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.graphics.ImageCache;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.editors.ttcn3editor.CallHierarchyNode;

/**
 * <p>
 * Label provider for the {@link CallHierarchyView}'s TreeViewer and TableViewer.<br>
 * The label provider work on {@link CallHierarchyNode}s and references.
 * </p>
 * @see CallHierarchyView
 * @see CallHierarchyNode
 * 
 * @author Sándor Bálazs
 */
public class CallHierarchyLabelProvider extends LabelProvider implements ITableLabelProvider {
	
	private static final int COLUMN_ICON = 0;
	private static final int COLUMN_LINE = 1;
	private static final int COLUMN_INFO = 2;
	
	/**
	 * Give an icon for the input element by the {@link CallHierarchyNode}'s definition type.
	 * @param element
	 * 			The current element which the method give the appropriate icon.<br>
	 * 			Type: {@link CallHierarchyNode}.
	 */
	@Override
	public Image getImage(final Object element) {
		String iconName = "titan.gif";
		
		if (!(element instanceof CallHierarchyNode)) {
			return ImageCache.getImage(iconName);
		}
		
		CallHierarchyNode node = (CallHierarchyNode) element;
		
		if(node.getNodeDefinition().getAssignmentName().equals("function")) {
			iconName = "function.gif";
		}
		
		if(node.getNodeDefinition().getAssignmentName().equals("testcase")) {
			iconName = "testcase.gif";
		}
		
		return ImageCache.getImage(iconName);
	}
	
	/**
	 * <p>
	 * Give a text for the input element by the {@link CallHierarchyNode}'s definition
	 * name when the input element is {@link CallHierarchyNode}.<br>
	 * This names use in the {@link CallHierarchyView}'s TreeViewer.
	 * </p>
	 * @param element
	 *			The current element which the method give the appropriate text.<br>
	 * 			Required type: {@link CallHierarchyNode}.
	 */
	@Override
	public String getText(final Object element) {
		if (!(element instanceof CallHierarchyNode)) {
			return "error";
		}
		CallHierarchyNode node = (CallHierarchyNode) element;
		final int matches = node.getReferencesNumber();
		if(matches > 0) {
			final String text = "{0}   -   ({1} matches)";
			return MessageFormat.format(text, node.getName(), matches);
		}
		return node.getName();
	}

	@Override
	public Image getColumnImage(final Object element, final int columnIndex) {
		if(columnIndex != COLUMN_ICON) {
			return null;
		}
		String iconName = "match.gif";
		return ImageCache.getImage(iconName);
	}

	/**
	 * <p>
	 * When the input element is Reference, the method use the the reference location or the reference name by the column index.<br>
	 * This names use in the {@link CallHierarchyView}'s TableViewer.
	 * </p>
	 * @param element
	 *			The current element which the method give the appropriate text.<br>
	 * 			Required type: Reference.
	 * @param columnIndex
	 * 			Define the current table column.
	 */
	@Override
	public String getColumnText(final Object element, final int columnIndex) {
		if(!(element instanceof Reference)){
			return null;
		}
		
		Reference reference = (Reference) element;
		
		switch (columnIndex) {
			case COLUMN_LINE:
				return String.valueOf(reference.getLocation().getLine());
			case COLUMN_INFO:
				final Assignment referedAssignment = reference.getRefdAssignment(CompilationTimeStamp.getBaseTimestamp(), false);
				return referedAssignment.getFullName();
		}
		return null;
	}
}
