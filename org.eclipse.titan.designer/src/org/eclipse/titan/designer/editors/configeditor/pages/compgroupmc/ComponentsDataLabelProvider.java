/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors.configeditor.pages.compgroupmc;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.titan.common.parsers.cfg.ConfigTreeNodeUtilities;
import org.eclipse.titan.common.parsers.cfg.indices.ComponentSectionHandler.Component;

/**
 * @author Kristof Szabados
 * */
public final class ComponentsDataLabelProvider extends LabelProvider implements ITableLabelProvider {

	@Override
	public Image getColumnImage(final Object element, final int columnIndex) {
		return null;
	}

	@Override
	public String getColumnText(final Object element, final int columnIndex) {
		if (element != null && element instanceof Component) {
			Component parameter = (Component) element;
			String text;
			switch (columnIndex) {
			case 0:
				if (parameter.getComponentName() == null) {
					return "";
				}

				text = parameter.getComponentName().getText();
				if (text == null || text.length() == 0) {
					text = ConfigTreeNodeUtilities.toString(parameter.getComponentName());
				}

				return text;
			case 1:
				if (parameter.getHostName() == null) {
					return "";
				}

				text = parameter.getHostName().getText();
				if (text == null || text.length() == 0) {
					text = ConfigTreeNodeUtilities.toString(parameter.getHostName());
				}

				return text;
			default:
				return "";
			}
		}

		return "";
	}
}
