/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.views.msc.ui.actions;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;

/**
 * This class merely servers as the locator of file with a given name.
 * */
public class FileFinder implements IResourceVisitor {
	private IFile targetFile = null;
	private String fileName;

	public FileFinder(final String fileName) {
		this.fileName = fileName;
	}

	/**
	 * @return the found file or null if none
	 * */
	public IFile getTargetFile() {
		return targetFile;
	}

	/**
	 * Visits the provided resource and all of it's children to find the specified file.
	 *
	 * @param resource the resource to start searching at
	 * */
	@Override
	public boolean visit(final IResource resource) {
		if (resource.getType() == IResource.FILE) {
			IFile file = (IFile) resource;
			if (file.getName().equals(fileName)) {
				targetFile = file;
			}
		} else {
			return true;
		}
		return false;
	}

}
