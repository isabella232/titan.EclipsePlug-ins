/******************************************************************************
 * Copyright (c) 2000-2020 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors.ttcn3editor.actions;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.consoles.TITANDebugConsole;
import org.eclipse.titan.designer.declarationsearch.Declaration;
import org.eclipse.titan.designer.declarationsearch.IdentifierFinderVisitor;
import org.eclipse.titan.designer.editors.actions.OpenDeclarationBase;
import org.eclipse.titan.designer.editors.ttcn3editor.TTCN3Editor;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.GlobalParser;
import org.eclipse.titan.designer.parsers.ProjectSourceParser;
import org.eclipse.titan.designer.preferences.PreferenceConstants;
import org.eclipse.titan.designer.productUtilities.ProductConstants;

/**
 * @author Kristof Szabados
 * @author Adam Knapp
 */
public final class OpenDeclaration extends OpenDeclarationBase {
	public static final String TTCN3EDITOR = "TTCN3";

	/**
	 * Opens an editor for the provided declaration, and in this editor the
	 * location of the declaration is revealed and selected.
	 *
	 * @param declaration
	 *                the declaration to reveal
	 * */
	private void selectAndRevealDeclaration(final Location location) {
		selectAndRevealDeclaration(location, TTCN3EDITOR);
	}

	@Override
	/** {@inheritDoc} */
	protected final void doOpenDeclaration() {
		if (targetEditor == null || !(targetEditor instanceof TTCN3Editor)) {
			return;
		}

		if (!check()) {
			return;
		}

		final IFile file = (IFile) targetEditor.getEditorInput().getAdapter(IFile.class);
		final IPreferencesService prefs = Platform.getPreferencesService();
		final boolean reportDebugInformation = prefs.getBoolean(ProductConstants.PRODUCT_ID_DESIGNER, PreferenceConstants.DISPLAYDEBUGINFORMATION,
				true, null);

		int offset;
		if (selection instanceof TextSelection && !selection.isEmpty() && !"".equals(((TextSelection) selection).getText())) {
			if (reportDebugInformation) {
				TITANDebugConsole.println("text selected: " + ((TextSelection) selection).getText());
			}

			final TextSelection tSelection = (TextSelection) selection;
			offset = tSelection.getOffset() + tSelection.getLength();
		} else {
			offset = ((TTCN3Editor) targetEditor).getCarretOffset();
		}

		final ProjectSourceParser projectSourceParser = GlobalParser.getProjectSourceParser(file.getProject());
		final Module module = projectSourceParser.containedModule(file);

		if (module == null) {
			if (reportDebugInformation) {
				TITANDebugConsole.println("The file " + file.getLocation() + "does not seem to contain a valid module.");
				final CompilationTimeStamp timestamp = projectSourceParser.getLastTimeChecked();
				if(timestamp == null) {
					TITANDebugConsole.println("The project " + file.getProject() + " was not yet analyzed semantically");
				} else {
					TITANDebugConsole.println("The project " + file.getProject() + " was last checked in " + projectSourceParser.getLastTimeChecked().toString());
				}
			}

			return;
		}

		final IdentifierFinderVisitor visitor = new IdentifierFinderVisitor(offset);
		module.accept(visitor);
		final Declaration decl = visitor.getReferencedDeclaration();
		if (decl == null) {
			if (reportDebugInformation) {
				TITANDebugConsole.println("No visible elements found");
			}

			return;
		}

		selectAndRevealDeclaration(decl.getIdentifier().getLocation());
	}
}
