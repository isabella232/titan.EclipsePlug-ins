/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titanium.refactoring.organize;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.window.Window;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.text.edits.DeleteEdit;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Definitions;
import org.eclipse.titan.designer.AST.TTCN3.definitions.ImportModule;
import org.eclipse.titan.designer.AST.TTCN3.definitions.TTCN3Module;
import org.eclipse.titan.designer.consoles.TITANDebugConsole;
import org.eclipse.titan.designer.editors.actions.DeclarationCollectionHelper;
import org.eclipse.titan.designer.editors.actions.OpenDeclarationLabelProvider;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.GlobalParser;
import org.eclipse.titan.designer.parsers.ProjectSourceParser;
import org.eclipse.titan.designer.productUtilities.ProductConstants;
import org.eclipse.ui.console.MessageConsoleStream;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

/**
 * Some methods to make convenient organizing import statements.
 * 
 * @author Farkas Izabella Ingrid 
 * @see org.eclipse.titanium.organize.OrganizeImports
 */

public class ChangeCreator {
	private static final String NEWLINE;
	static {
		NEWLINE = System.getProperty("line.separator");
	}

	private static boolean sortImports = true;
	private static boolean addImports = true;
	private static boolean removeImports = true;
	//private static String importChangeMethod;
	private static boolean reportDebug;
	
	//in
	private final IFile selectedFile;

	//out
	private Change change;

	ChangeCreator(final IFile selectedFile) {
		this.selectedFile = selectedFile;
	}

	public Change getChange() {
		return change;
	}

	/**
	 * Creates the {@link #change} object, which contains all the inserted, deleted and sorted imports 
	 * in the selected resources.
	 * */
	public void perform() {
		if (selectedFile == null) {
			return;
		}
		change = createFileChange(selectedFile);
	}

	private Change createFileChange(final IFile toVisit){
		if (toVisit == null) {
			return null;
		}
		
		final String designerId = ProductConstants.PRODUCT_ID_DESIGNER;
		final String displayDebugInfo = org.eclipse.titan.designer.preferences.PreferenceConstants.DISPLAYDEBUGINFORMATION;
		reportDebug = Platform.getPreferencesService().getBoolean(designerId, displayDebugInfo, false, null);
				
		final ProjectSourceParser sourceParser = GlobalParser.getProjectSourceParser(toVisit.getProject());
		final Module module = sourceParser.containedModule(toVisit);
		if(module == null || !(module instanceof TTCN3Module)) {
			return null;
		}
		
		final TextFileChange tfc = new TextFileChange(toVisit.getName(), toVisit);
		IDocument doc;
		
		final TTCN3Module tModule = (TTCN3Module) module;
		
		try {
			doc = tfc.getCurrentDocument(null);
			final MultiTextEdit resultEdit = organizeImportsEdit(tModule, doc);
			if (!resultEdit.hasChildren()) {
				return null;
			}
			tfc.setEdit(resultEdit);
		} catch (BadLocationException e) {
			ErrorReporter.logExceptionStackTrace("Error while organizing imports", e);
		} catch (CoreException e1) {
			ErrorReporter.logExceptionStackTrace("Error while organizing imports", e1);
		}
		
		return tfc;
	}
	
	/**
	 * Try to find the declaration of the reference in any module of the
	 * project.
	 * <p>
	 * If exactly one declaration is found, then its location is returned. If
	 * multiple modules contain a valid declaration of the reference, then a
	 * dialog is displayed to the user to choose one. If none found, or the user
	 * cancels the dialog, <code>null</code> is returned.
	 * </p>
	 * 
	 * @param reference
	 *            The (missing) reference we are searching for.
	 * @param project
	 *            The project in which we search the declaration.
	 * @return The location of the declaration, if uniquely found,
	 *         <code>null</code> otherwise.
	 */
	public static Location findReferenceInProject(final Reference reference, final IProject project) {
		final ProjectSourceParser projectSourceParser = GlobalParser.getProjectSourceParser(project);
		final List<DeclarationCollectionHelper> collected = new ArrayList<DeclarationCollectionHelper>();
		final Identifier identifier = reference.getId();
		for (final String moduleName : projectSourceParser.getKnownModuleNames()) {
			final Module m = projectSourceParser.getModuleByName(moduleName);
			if (m != null && m.getAssignments().hasLocalAssignmentWithID(CompilationTimeStamp.getBaseTimestamp(), identifier)) {
				Assignment assignment = m.getAssignments().getLocalAssignmentByID(CompilationTimeStamp.getBaseTimestamp(), identifier);
				if (assignment != null) {
					collected.add(new DeclarationCollectionHelper(assignment.getProposalDescription(), assignment.getIdentifier().getLocation(),
							assignment));
				}
			}
		}

		Location loc = null;

		if (collected.size() > 1) {
			final List<String> files = new ArrayList<String>();
			for (final DeclarationCollectionHelper c : collected) {
				files.add(c.location.getFile().getName());
			}
			TITANDebugConsole.println("Multiple possible imports for " + reference.getDisplayName() + ": " + files.toString());

			final ImportSelectionDialog dialog = new ImportSelectionDialog(reference, collected, reference.getLocation().getFile());
			Display.getDefault().syncExec(dialog);
			loc = dialog.getSelected();
		} else if (collected.size() == 1) {
			DeclarationCollectionHelper declaration = collected.get(0);
			loc = declaration.location;
			TITANDebugConsole.println("Exactly one module for " + reference.getDisplayName() + " is found: " + loc.getFile().getName());
		} else {
			TITANDebugConsole.println("No imports for " + reference.getDisplayName() + " is found");
		}

		return loc;
	}
	/**
	 * Organize the imports according to the global preferences. If set,
	 * <ul>
	 * <li>Add imports necessary for missing references,</li>
	 * <li>Remove unused imports,</li>
	 * <li>Sort the import statements.</li>
	 * </ul>
	 * <p>
	 * These changes are not applied in the function, just collected in a
	 * <link>MultiTextEdit</link>, which is then returned.
	 * </p>
	 * TODO: notice and handle ambiguous references
	 * 
	 * @param module
	 *            The module which import statements are to organize.
	 * @param document
	 *            The document that contains the module.
	 * 
	 * @return The edit, which contains the proper changes.
	 */
	private static MultiTextEdit organizeImportsEdit(final TTCN3Module module, final IDocument document) throws BadLocationException {
		final IProject prj = module.getProject();
		final String doc = document.get();
		final MultiTextEdit insertEdit = new MultiTextEdit();
		final MultiTextEdit removeEdit = new MultiTextEdit();
		final List<ImportText> newImports = new ArrayList<ImportText>();
		final List<ImportText> importsKept = new ArrayList<ImportText>();

		boolean needSorting = false;

		if (addImports) {
			// register the new needed imports
			final Set<String> importNamesAdded = new HashSet<String>();
			for (final Reference ref : module.getMissingReferences()) {
				final Location missLoc = findReferenceInProject(ref, prj);
				if (missLoc != null) {
					final IFile file = (IFile) missLoc.getFile();
					final ProjectSourceParser parser = GlobalParser.getProjectSourceParser(file.getProject());
					final Module addMod = parser.containedModule(file);
					final String importName = addMod.getIdentifier().getTtcnName();
					if (!importNamesAdded.contains(importName)) {
						final StringBuilder impText = new StringBuilder("import from ").append(importName).append(" all;");
						//if (importChangeMethod.equals(OrganizeImportPreferencePage.COMMENT_THEM)) {
							impText.append(" // Added automatically to resolve ").append(ref.getDisplayName());
						//}
						newImports.add(new ImportText(importName, impText.toString() + NEWLINE));
						importNamesAdded.add(importName);

						if (reportDebug) {
							final StringBuilder sb = new StringBuilder("For ").append(ref.getDisplayName()).append(": ");
							sb.append(impText.toString());
							TITANDebugConsole.println(sb.toString());
						}
					}
				}
			}

			if (sortImports && !newImports.isEmpty()) {
				needSorting = true;
			}
		}

		if (!needSorting && sortImports) {
			// are the imports already sorted ?
			final List<ImportModule> oldImports = module.getImports();
			for (int size = oldImports.size(), i = 0; i < size - 1 && !needSorting; i++) {
				if (oldImports.get(i).getName().compareTo(oldImports.get(i + 1).getName()) > 0) {
					needSorting = true;
				}
				if (oldImports.get(i).getLocation().getOffset() > oldImports.get(i + 1).getLocation().getOffset()) {
					needSorting = true;
				}
			}

			if (!needSorting && oldImports.size() > 1) {
				// are the import strictly before the definitions ?
				final int lastImportOffset = oldImports.get(oldImports.size() - 1).getLocation().getOffset();
				final Definitions defs = module.getAssignmentsScope();
				if (defs.getNofAssignments() > 0 && !oldImports.isEmpty()) {
					for (int i = 0, size = defs.getNofAssignments(); i < size; ++i) {
						final int temp = defs.getAssignmentByIndex(i).getLocation().getOffset();
						if (temp < lastImportOffset) {
							needSorting = true;
						}
					}
				}
			}
		}

		if (needSorting || removeImports) {
			// remove the imports not needed, or every if sorting is required
			for (final ImportModule m : module.getImports()) {
				final Location delImp = m.getLocation();
				final IRegion startLineRegion = document.getLineInformationOfOffset(delImp.getOffset());
				final IRegion endLineRegion = document.getLineInformationOfOffset(delImp.getEndOffset());
				final String delimeter = document.getLineDelimiter(document.getLineOfOffset(delImp.getEndOffset()));
				final int delLength = delimeter == null ? 0 : delimeter.length();

				if (needSorting || (removeImports && !m.getUsedForImportation())) {
					if (reportDebug) {
						final MessageConsoleStream stream = TITANDebugConsole.getConsole().newMessageStream();
						TITANDebugConsole.println("Removing "
								+ "'"
								+ doc.substring(startLineRegion.getOffset(), endLineRegion.getOffset()
										+ endLineRegion.getLength() + delLength) + "'", stream);
						TITANDebugConsole.println("From "
								+ startLineRegion.getOffset()
								+ " till "
								+ ((endLineRegion.getOffset() - startLineRegion.getOffset())
										+ endLineRegion.getLength() + delLength), stream);
					}
					/*if (importChangeMethod.equals(OrganizeImportPreferencePage.COMMENT_THEM)) {
						removeEdit.addChild(new InsertEdit(m.getLocation().getOffset(), "/*"));
						// hack to handle the semicolon
						removeEdit.addChild(new InsertEdit(m.getLocation().getEndOffset() + 1, "")); 
					} else {*/
						removeEdit.addChild(new DeleteEdit(startLineRegion.getOffset(),
								(endLineRegion.getOffset() - startLineRegion.getOffset()) + endLineRegion.getLength()
										+ delLength));
					//}
				}
				if (needSorting && (!removeImports || m.getUsedForImportation())) {
					importsKept.add(new ImportText(m.getName(), doc.substring(startLineRegion.getOffset(),
							endLineRegion.getOffset() + endLineRegion.getLength() + delLength)));
				}
			}
		}

		if (!newImports.isEmpty() || (sortImports && needSorting)) {
			// always insert at the beginning of the file
			final int line = document.getLineOfOffset(module.getAssignmentsScope().getLocation().getOffset());
			final IRegion lineRegion = document.getLineInformation(line);
			final String delimeter = document.getLineDelimiter(line);
			final int delimeterLength = delimeter == null ? 0 : delimeter.length();
			final int startPos = lineRegion.getOffset() + lineRegion.getLength() + delimeterLength;

			if (sortImports) {
				if (needSorting || !newImports.isEmpty()) {
					final List<ImportText> results = new ArrayList<ImportText>();
					results.addAll(importsKept);
					results.addAll(newImports);
					Collections.sort(results);

					for (final ImportText i : results) {
						insertEdit.addChild(new InsertEdit(startPos, i.getText()));
					}
				}
			} else {
				Collections.sort(newImports);

				for (final ImportText i : newImports) {
					insertEdit.addChild(new InsertEdit(startPos, i.getText()));
				}
			}
		}

		final MultiTextEdit resultEdit = new MultiTextEdit();
		if (insertEdit.hasChildren()) {
			resultEdit.addChild(insertEdit);
		}
		if (removeEdit.hasChildren()) {
			resultEdit.addChild(removeEdit);
		}
		return resultEdit;
	}
}

class ImportText implements Comparable<ImportText> {
		private final String moduleName;
		private final String importText;

		public ImportText(final String moduleName, final String importText) {
			this.moduleName = moduleName;
			this.importText = importText;
		}

		public String getModuleName() {
			return moduleName;
		}

		public String getText() {
			return importText;
		}

		@Override
		public int compareTo(final ImportText rhs) {
			return moduleName.compareTo(rhs.moduleName);
		}

		@Override
		public boolean equals(final Object obj) {
			if (obj == null) {
				return false;
			}

			if (!(obj instanceof ImportText)) {
				return false;
			}

			return moduleName.equals(((ImportText) obj).moduleName);
		}

		@Override
		public int hashCode() {
			return moduleName.hashCode();
		}
}

class ImportSelectionDialog implements Runnable {
		private final Reference reference;
		private final List<DeclarationCollectionHelper> collected;
		private Location selected;
		private final IResource source;

		public ImportSelectionDialog(final Reference reference, final List<DeclarationCollectionHelper> collected, final IResource source) {
			this.reference = reference;
			this.collected = collected;
			this.selected = null;
			this.source = source;
		}

		@Override
		public void run() {
			final OpenDeclarationLabelProvider labelProvider = new OpenDeclarationLabelProvider();
			final ElementListSelectionDialog dialog = new ElementListSelectionDialog(new Shell(Display.getCurrent()), labelProvider);
			dialog.setTitle("Add Import");
			dialog.setMessage("For the missing reference: " + reference.getDisplayName() 
					+ " in " + source.getProjectRelativePath().toString() + ".");
			dialog.setElements(collected.toArray());
			dialog.setHelpAvailable(false);
			if (dialog.open() == Window.OK) {
				selected = ((DeclarationCollectionHelper) dialog.getFirstResult()).location;
			}
		}

		public Location getSelected() {
			return selected;
		}
}
