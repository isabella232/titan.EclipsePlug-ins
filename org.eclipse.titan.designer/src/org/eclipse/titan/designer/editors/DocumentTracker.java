/******************************************************************************
 * Copyright (c) 2000-2019 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.IDocument;

/**
 * This fully static class is used to track which files are open in a document at
 * a given time.
 * <p>
 * We assume that one file can be open in one document only at a time.
 *
 * @author Kristof Szabados
 * */
public final class DocumentTracker {

	private static final Map<IFile, IDocument> FILE_DOCUMENT_MAP = new ConcurrentHashMap<IFile, IDocument>();

	/** private constructor to disable instantiation */
	private DocumentTracker() {
		// intentionally empty
	}

	/**
	 * Stores the information that the provided file is opened in the provided document.
	 *
	 * @param file the provided file
	 * @param document the provided document
	 * */
	public static void put(final IFile file, final IDocument document) {
		FILE_DOCUMENT_MAP.put(file, document);
	}

	/**
	 * Checks if the provided file is open in a document and returns the document.
	 *
	 * @param file the file to check for
	 * @return the document the file is open in, or null if none
	 * */
	public static IDocument get(final IFile file) {
		return FILE_DOCUMENT_MAP.get(file);
	}
}
