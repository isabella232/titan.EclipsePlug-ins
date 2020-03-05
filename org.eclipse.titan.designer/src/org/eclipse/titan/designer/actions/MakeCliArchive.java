/******************************************************************************
 * Copyright (c) 2000-2020 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.actions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.consoles.TITANConsole;
import org.eclipse.titan.designer.consoles.TITANDebugConsole;
import org.eclipse.titan.designer.core.makefile.InternalMakefileGenerator;
import org.eclipse.titan.designer.core.makefile.ModuleStruct;
import org.eclipse.titan.designer.core.makefile.OtherFileStruct;
import org.eclipse.titan.designer.core.makefile.UserStruct;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Plug-in action to archive the contents of TITAN projects, without using the command line.
 */
public class MakeCliArchive extends AbstractHandler implements IObjectActionDelegate {

	private static final String BIN_SUBPATH = File.separator + "bin";
	private static final String BACKUP_SUBPATH = File.separator + "backup";
	
	private ISelection selection;

	private InternalMakefileGenerator makefileGenerator;

	@Override
	/** {@inheritDoc} */
	public void run(final IAction action) {
		generateCLIArchive(selection);
	}

	@Override
	/** {@inheritDoc} */
	public void selectionChanged(final IAction action, final ISelection selection) {
		this.selection = selection;
	}

	@Override
	/** {@inheritDoc} */
	public void setActivePart(final IAction action, final IWorkbenchPart targetPart) {
		// Do nothing
	}

	@Override
	/** {@inheritDoc} */
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		selection = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage().getSelection();

		generateCLIArchive(selection);

		return null;
	}

	/**
	 * This is the main entry point of the feature. It uses the internal
	 * makefile generator's infrastructure to collect the files to a working
	 * directory which will be compressed later to an archive.
	 * 
	 * @param selection
	 *            The selected project in the Project explorer
	 */

	public void generateCLIArchive(final ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			final IStructuredSelection structSelection = (IStructuredSelection) selection;
			IProject singleSelectedProject;
			final List<?> selectionList = structSelection.toList();
			if (selectionList.size() == 1) {
				if (selectionList.get(0) instanceof IProject) {
					singleSelectedProject = (IProject) selectionList.get(0);
					makefileGenerator = new InternalMakefileGenerator(singleSelectedProject);

					makefileGenerator.gatherInformation();

					final File binDir = new File(singleSelectedProject.getLocation().toFile(), BIN_SUBPATH);
					final File backupDir = new File(binDir, BACKUP_SUBPATH);
					if ((backupDir.exists()) || (!backupDir.exists() && backupDir.mkdirs())) {

						// Create Zip output stream
						final String dateTime = new SimpleDateFormat("yyyyMMdd_HHmmss")
								.format(Calendar.getInstance().getTime());
						final String projectName = singleSelectedProject.getName();
						try {
							final File zipFile = new File(backupDir, projectName + "_" + dateTime + ".zip");
							zipFile.createNewFile();
							try {
								final ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(zipFile));
								File f;

								final Iterator<ModuleStruct> ttcn3Files = makefileGenerator.getTtcn3Modules().iterator();
								while (ttcn3Files.hasNext()) {
									final ModuleStruct filedesc = ttcn3Files.next();
									f = new File(filedesc.getOriginalLocation());
									addToArchive(f, zip);
								}

								final Iterator<ModuleStruct> ttcn3PPFiles = makefileGenerator.getTtcnppModules().iterator();
								while (ttcn3PPFiles.hasNext()) {
									final ModuleStruct filedesc = ttcn3PPFiles.next();
									f = new File(filedesc.getOriginalLocation());
									addToArchive(f, zip);
								}

								final Iterator<ModuleStruct> ASN1Files = makefileGenerator.getAsn1modules().iterator();
								while (ASN1Files.hasNext()) {
									final ModuleStruct filedesc = ASN1Files.next();
									f = new File(filedesc.getOriginalLocation());
									addToArchive(f, zip);
								}

								final Iterator<UserStruct> userFiles = makefileGenerator.getUserFiles().iterator();
								while (userFiles.hasNext()) {
									final UserStruct filedesc = userFiles.next();
									f = new File(filedesc.getOriginalSourceLocation());
									addToArchive(f, zip);

									final String absPath = f.getAbsolutePath();
									final String pathWithoutExt = absPath.substring(0, absPath.lastIndexOf('.') + 1);

									final File hFile = new File(pathWithoutExt + "h");
									if (hFile.exists()) {
										addToArchive(hFile, zip);
									}

									final File hhFile = new File(pathWithoutExt + "hh");
									if (hhFile.exists()) {
										addToArchive(hhFile, zip);
									}
								}

								final Iterator<OtherFileStruct> otherFiles = makefileGenerator.getOtherFiles().iterator();
								while (otherFiles.hasNext()) {
									final OtherFileStruct filedesc = otherFiles.next();
									String loc;
									if (filedesc.getFileName().equals("Makefile")) {
										loc = binDir + File.separator + filedesc.getFileName();
										f = new File(loc);
										addToArchive(f, f.getName() + ".orig", zip);
									} else {
										loc = filedesc.getOriginalLocation();
										f = new File(loc);
										addToArchive(f, zip);
									}
								}

								createReadme(zip);

								zip.close();
							} catch (IOException e1) {
								ErrorReporter.logExceptionStackTrace(e1);
								TITANConsole.println("Unable to create contents of archive file");
							}
						} catch (IOException ioe) {
							ErrorReporter.logExceptionStackTrace(ioe);
							TITANConsole.println("Unable to create archive file");
						}

					}
				} else {
					TITANConsole.println("Make CLI archive works only for single selected project");
				}
			} else {
				TITANConsole.println("Make CLI archive works only for single selected project");
			}

		}

	}

	/**
	 * Create the README file for explaining how to deal with the archive
	 */
	private void createReadme(final ZipOutputStream zip) {
		final StringBuffer buff = new StringBuffer();

		buff.append("This archive contains the files of one or more Eclipe Titan projects.\n");
		buff.append('\n');
		buff.append("The name of the file itself can be interpreted as follows:\n");
		buff.append("<Project>_<Date>_<time>.tar.gz\n");
		buff.append('\n');
		buff.append("To build it the Makefile has to be generated from sratch using the\n");
		buff.append("\"makefilegen ./*\" command or manually created.\n");
		buff.append('\n');
		buff.append("The original Makefile used to compile at the source system\n");
		buff.append("is also included in this archive named \"Makefile.orig\"\n");

		final byte[] data = buff.toString().getBytes();

		try {
			zip.putNextEntry(new ZipEntry("README"));
			zip.write(data);
			zip.closeEntry();
		} catch (IOException e) {
			ErrorReporter.logExceptionStackTrace(e);
			TITANDebugConsole.println("Unable to add README to zip");
		}

	}

	private void addToArchive(final File input, final ZipOutputStream zip) {
		addToArchive(input, input.getName(), zip);
	}

	private void addToArchive(final File input, final String fileName, final ZipOutputStream zip) {
		try {
			zip.putNextEntry(new ZipEntry(fileName));
			final byte[] buff = Files.readAllBytes(input.toPath());
			zip.write(buff);
			zip.closeEntry();
		} catch (IOException e) {
			ErrorReporter.logExceptionStackTrace(e);
			TITANConsole.println("Unable to add file to zip: " + input.getName());
		}

	}

}
