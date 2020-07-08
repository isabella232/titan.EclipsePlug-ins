/******************************************************************************
 * Copyright (c) 2000-2020 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core.cfgparser;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Stores temporary config editor data of the include section
 * @author Kristof Szabados
 * @author Arpad Lovassy
 */
public final class IncludeSectionHandler {

	/** include files */
	private final Set<String> mFiles = new LinkedHashSet<String>();

	public Set<String> getFiles() {
		return mFiles;
	}

	public void addFile( final String aIncludeFile ) {
		mFiles.add( aIncludeFile );
	}

	public boolean isFileAdded( final String aIncludeFile ) {
		return mFiles.contains(aIncludeFile);
	}
}
