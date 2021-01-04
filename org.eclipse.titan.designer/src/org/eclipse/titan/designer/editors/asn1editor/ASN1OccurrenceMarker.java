/******************************************************************************
 * Copyright (c) 2000-2021 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors.asn1editor;

import java.util.List;

import org.eclipse.jface.text.IDocument;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.editors.IReferenceParser;
import org.eclipse.titan.designer.editors.OccurencesMarker;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * @author Kristof Szabados
 * */
public class ASN1OccurrenceMarker extends OccurencesMarker {
	private final IReferenceParser referenceParser = new ASN1ReferenceParser();

	public ASN1OccurrenceMarker(final ITextEditor editor) {
		super(editor);
	}

	@Override
	protected IReferenceParser getReferenceParser() {
		return referenceParser;
	}

	@Override
	protected List<Hit> findOccurrences(final IDocument document, final Reference reference, final Module module, final int offset) {
		return findOccurrencesReferenceBased(document, reference, module, offset);
	}
}
