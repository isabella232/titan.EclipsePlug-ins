/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.executor.executors;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.titan.executor.graphics.ImageCache;

/**
 * @author Kristof Szabados
 * */
public final class ExecuteDialogLabelProvider extends LabelProvider {

	@Override
	public Image getImage(final Object element) {
		if (element instanceof TreeBranch) {
			return ImageCache.getImage("testset.gif");
		} else if (element instanceof TreeLeaf) {
			return ImageCache.getImage("testcase.gif");
		}
		return super.getImage(element);
	}

	@Override
	public String getText(final Object element) {
		if (element instanceof TreeLeaf) {
			return ((TreeLeaf) element).name();
		}
		return super.getText(element);
	}

}
