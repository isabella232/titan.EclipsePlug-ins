/******************************************************************************
 * Copyright (c) 2000-2021 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IDocument;
import org.eclipse.titan.common.logging.ErrorReporter;

/**
 * @author Kristof Szabados
 * */
public final class ClosingBracketIndentationAutoEditStrategy extends GeneralTITANAutoEditStrategy {
	private static final String CLOSING_CURLY_BRACKET = "}";

	@Override
	public void customizeDocumentCommand(final IDocument document, final DocumentCommand command) {
		if (!CLOSING_CURLY_BRACKET.equals(command.text)) {
			return;
		}

		refreshAutoEditStrategy();
		initializeRootInterval(document);

		if (rootInterval == null) {
			return;
		}
		try {
			final StringBuilder builder = new StringBuilder(document.get());
			if (isWithinMultiLineComment(command.offset) || isWithinString(builder, command.offset)) {
				return;
			}
		} catch (BadLocationException e) {
			ErrorReporter.logExceptionStackTrace(e);
		}

		smartInsertAfterBracket(document, command);
	}

	/**
	 * Set the indent of a bracket based on the command provided in the
	 * supplied document.
	 *
	 * @param document
	 *                - the document being parsed
	 * @param command
	 *                - the command being performed
	 */
	protected void smartInsertAfterBracket(final IDocument document, final DocumentCommand command) {
		if (command.offset == -1 || document.getLength() == 0) {
			return;
		}

		try {
			final int p = (command.offset == document.getLength() ? command.offset - 1 : command.offset);
			final int line = document.getLineOfOffset(p);
			final int start = document.getLineOffset(line);
			final int whiteend = findEndOfWhiteSpace(document, start, command.offset);

			// shift only when line does not contain any text up to
			// the closing
			// bracket
			if (whiteend == command.offset) {
				// evaluate the line with the opening bracket
				// that matches out
				// closing bracket
				final int indLine = findMatchingOpenBracket(document, command.offset);
				if (indLine != -1 && indLine != line) {
					// take the indent of the found line
					final StringBuffer replaceText = new StringBuffer(getIndentOfLine(document, indLine, command));
					// add the rest of the current line
					// including the just added
					// close bracket
					// replaceText.append(document.get(whiteend,
					// command.offset
					// - whiteend));
					replaceText.append(command.text);
					// modify document command
					command.length = command.offset - start;
					command.offset = start;
					command.text = replaceText.toString();
				}
			}
		} catch (BadLocationException e) {
			ErrorReporter.logExceptionStackTrace(e);
		}
	}
}
