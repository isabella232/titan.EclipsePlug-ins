/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.IDocument;
import org.eclipse.titan.designer.AST.Reference;

/**
 * Provides a general interface for reference parsers.
 *
 * @author Kristof Szabados
 * */
public interface IReferenceParser {

	void setErrorReporting(boolean reportErrors);

	Reference findReferenceForCompletion(IFile file, int offset, IDocument document);

	/**
	 * @return the parsed reference or null if the text can not form a reference
	 * */
	Reference findReferenceForOpening(IFile file, int offset, IDocument document);
}
