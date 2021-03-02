/******************************************************************************
 * Copyright (c) 2000-2021 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.common.utils;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.titan.common.logging.ErrorReporter;

public final class FileUtils {

	private FileUtils() {
		// Hide constructor
	}

	/**
	 * Deletes a file.
	 * @param file the file to delete
	 * @throws IOException if the file cannot be deleted
	 */
	public static void delete(final File file) throws IOException {
		if (deleteIfExists(file)) {
			throw new IOException("The file cannot be deleted: " + file.getAbsolutePath());
		}
	}

	/**
	 * Deletes a file. Logs the error if fails.
	 *
	 * @param file the file to delete
	 */
	public static boolean deleteQuietly(final File file) {
		final boolean failed = deleteIfExists(file);
		if (failed) {
			ErrorReporter.logError("Cannot delete file: " + file.getPath());
		}

		return !failed;
	}

	private static boolean deleteIfExists(final File file) {
		return file.exists() && !file.delete();
	}

	/**
	 * RECURSIVE
	 * Creates full directory path
	 * @param aFolder directory to create
	 * @throws CoreException
	 */
	public static void createDir( final IFolder aFolder ) throws CoreException {
		createDir(aFolder, null);
	}

	/**
	 * RECURSIVE
	 * Creates full directory path
	 * @param aFolder directory to create
	 * @param monitor a progress monitor, or {@code null} if progress reporting is not desired
	 * @throws CoreException
	 */
	public static void createDir( final IFolder aFolder, final IProgressMonitor monitor ) throws CoreException {
		if (!aFolder.exists()) {
			final IContainer parent = aFolder.getParent();
			if (parent instanceof IFolder) {
				createDir( (IFolder) parent );
			}
			aFolder.create( true, true, monitor );
		}
	}
}
