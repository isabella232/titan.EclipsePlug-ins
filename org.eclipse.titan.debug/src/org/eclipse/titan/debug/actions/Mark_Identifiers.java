/******************************************************************************
 * Copyright (c) 2000-2020 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.debug.actions;

import java.io.InputStream;
import java.io.InputStreamReader;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.consoles.TITANDebugConsole;
import org.eclipse.titan.designer.editors.ttcn3editor.TTCN3Editor;
import org.eclipse.titan.designer.parsers.GlobalParser;
import org.eclipse.titan.designer.parsers.ProjectSourceParser;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;

/**
 * This editor action allows us to debug the possible incorrect shifts happening during the update part of incremental parsing.
 *
 * This is done by checking for each identifier in the file if its name and the text in the file at the stored location is the same.
 * If they are not the same, the identifier is underlined as if being a semantic error.
 * This way if the update process goes wrong from some point onward, the incorrect identifier locations will help debug it.
 *
 * General usage:
 *  add a single space somewhere in the beginning of an editor and save the file.
 *  Invoke the action.
 *
 * @author Kristof Szabados
 */
public class Mark_Identifiers implements IEditorActionDelegate {
	private TTCN3Editor targetEditor = null;
	StringBuilder paddingBuffer = new StringBuilder();

	@Override
	public void run(final IAction action) {
		if (targetEditor == null) {
			return;
		}

		final IFile file = (IFile) targetEditor.getEditorInput().getAdapter(IFile.class);
		final ProjectSourceParser parser = GlobalParser.getProjectSourceParser(file.getProject());
		final Module module = parser.containedModule(file);
		if (module == null) {
			TITANDebugConsole.println("No module was found");
		}

		String content1;

		try {
			final InputStream is1 = file.getContents();
			final char[] buf = new char[1024];
			final StringBuilder sb = new StringBuilder();
			final InputStreamReader isr = new InputStreamReader(is1);
			int len;
			while ((len = isr.read(buf)) > 0) {
				sb.append(buf, 0, len);
			}
			content1 = sb.toString();
			is1.close();
		} catch (Exception e) {
			return;
		}

		final String fileContent = content1;
		module.accept(new ASTVisitor() {
			@Override
			public int visit(final IVisitableNode node) {
				if (node instanceof Identifier) {
					final Identifier id = (Identifier) node;
					final Location loc = id.getLocation();
					if (loc.getOffset() == -1) {
						return super.visit(node);
					}

					final String inFile = fileContent.substring(loc.getOffset(), loc.getEndOffset());
					final String inEditor = id.getDisplayName();
					if (!inEditor.equals(inFile)) {
						loc.reportSemanticError("This is an identifier");
					}
				}

				return super.visit(node);
			}
		});
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		// Nothing to be done
	}

	@Override
	public void setActiveEditor(final IAction action, final IEditorPart targetEditor) {
		if (targetEditor instanceof TTCN3Editor) {
			this.targetEditor = (TTCN3Editor) targetEditor;
		} else {
			this.targetEditor = null;
		}
	}
}
