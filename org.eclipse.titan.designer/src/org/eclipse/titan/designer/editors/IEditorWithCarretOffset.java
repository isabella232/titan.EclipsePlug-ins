/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors;

import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Interface for a text editor that use a carret, whose offset can be retrieved.
 *
 * @author Kristof Szabados
 * */
public interface IEditorWithCarretOffset extends ITextEditor {

	/** @return the offset the carret is currently on. */
	int getCarretOffset();
}
