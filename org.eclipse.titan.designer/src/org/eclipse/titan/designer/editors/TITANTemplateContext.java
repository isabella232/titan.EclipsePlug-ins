/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.FindReplaceDocumentAdapter;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.templates.DocumentTemplateContext;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateBuffer;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.TemplateException;
import org.eclipse.jface.text.templates.TemplateTranslator;
import org.eclipse.jface.text.templates.TemplateVariable;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.RangeMarker;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.editors.actions.FirstCharAction;

/**
 * The TITANTemplateContext class represents a context for templates, where they
 * can be transformed according to the rules of TITAN.
 *
 * @author Kristof Szabados
 * */
public final class TITANTemplateContext extends DocumentTemplateContext {

	public TITANTemplateContext(final TemplateContextType type, final IDocument document, final int offset, final int length) {
		super(type, document, offset, length);
	}

	@Override
	public TemplateBuffer evaluate(final Template template) throws BadLocationException, TemplateException {
		if (!canEvaluate(template)) {
			return null;
		}

		final TemplateTranslator translator = new TemplateTranslator();
		final TemplateBuffer buffer = translator.translate(template);

		getContextType().resolve(buffer, this);

		if (isReadOnly()) {
			// if it is read only we should not modify it
			return buffer;
		}

		// calculate base indentation prefix
		final IDocument document = getDocument();
		String prefixString = "";
		String delimeter = null;
		try {
			final IRegion lineRegion = document.getLineInformationOfOffset(getCompletionOffset());
			final int firstCharLocation = FirstCharAction.firstVisibleCharLocation(document, lineRegion);
			if (firstCharLocation != -1) {
				prefixString = document.get(lineRegion.getOffset(), firstCharLocation - lineRegion.getOffset());
			}
			delimeter = document.getLineDelimiter(document.getLineOfOffset(getCompletionOffset()));
		} catch (BadLocationException e) {
			ErrorReporter.logExceptionStackTrace(e);
		}

		final TemplateVariable[] variables = buffer.getVariables();

		// apply the base indentation prefix to every line but the first
		final IDocument temporalDocument = new Document(buffer.getString());
		final MultiTextEdit edit = new MultiTextEdit(0, temporalDocument.getLength());
		final List<RangeMarker> positions = variablesToPositions(variables);
		for (int i = temporalDocument.getNumberOfLines() - 1; i >= 0; i--) {
			edit.addChild(new InsertEdit(temporalDocument.getLineOffset(i), prefixString));
		}
		edit.addChildren(positions.toArray(new TextEdit[positions.size()]));

		// replace line delimeters with the ones at the insertion
		final String delimeterZero = temporalDocument.getLineDelimiter(0);
		if(delimeter != null && delimeterZero != null && !delimeter.equals(delimeterZero)) {
			final FindReplaceDocumentAdapter adapter = new FindReplaceDocumentAdapter(temporalDocument);
			int startOffset = 0;
			IRegion region = adapter.find(startOffset, delimeterZero, true, false, false, false);
			while (region != null) {
				edit.addChild(new ReplaceEdit(region.getOffset(), region.getLength(), delimeter));

				startOffset = region.getOffset() + region.getLength();
				region = adapter.find(startOffset, delimeterZero, true, false, false, false);
			}
		}

		edit.apply(temporalDocument, TextEdit.UPDATE_REGIONS);

		positionsToVariables(positions, variables);

		buffer.setContent(temporalDocument.get(), variables);
		return buffer;
	}

	private static List<RangeMarker> variablesToPositions(final TemplateVariable[] variables) {
		final List<RangeMarker> positions = new ArrayList<RangeMarker>(5);
		for (int i = 0; i != variables.length; i++) {
			final int[] offsets = variables[i].getOffsets();
			for (int j = 0; j != offsets.length; j++) {
				positions.add(new RangeMarker(offsets[j], 0));
			}
		}

		return positions;
	}

	private static void positionsToVariables(final List<RangeMarker> positions, final TemplateVariable[] variables) {
		final Iterator<RangeMarker> iterator = positions.iterator();

		for (int i = 0; i != variables.length; i++) {
			final TemplateVariable variable = variables[i];
			final int[] offsets = new int[variable.getOffsets().length];
			for (int j = 0; j != offsets.length; j++) {
				offsets[j] = ((TextEdit) iterator.next()).getOffset();
			}

			variable.setOffsets(offsets);
		}
	}
}
