/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core.cfgparser;

import java.util.ArrayList;
import java.util.List;

/**
 * Stores temporary config editor data of the include section
 * @author Kristof Szabados
 * @author Arpad Lovassy
 */
public final class IncludeSectionHandler {

	/** list of include files, which are stored as ParseTree nodes */
	private List<String> mFiles = new ArrayList<String>();

	public List<String> getFiles() {
		return mFiles;
	}

	public void addFile( final String aIncludeFile ) {
		mFiles.add( aIncludeFile );
	}
}
