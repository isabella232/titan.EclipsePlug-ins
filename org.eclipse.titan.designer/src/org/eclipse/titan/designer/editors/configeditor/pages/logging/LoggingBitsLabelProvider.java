/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors.configeditor.pages.logging;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.titan.common.parsers.cfg.indices.LoggingBit;

/**
 * @author Kristof Szabados
 * */
public final class LoggingBitsLabelProvider extends LabelProvider {

	@Override
	public String getText(final Object element) {
		if (element != null && element instanceof LoggingBit) {
			LoggingBit bit = (LoggingBit) element;
			return (bit).getName();
		}

		return super.getText(element);
	}

}
