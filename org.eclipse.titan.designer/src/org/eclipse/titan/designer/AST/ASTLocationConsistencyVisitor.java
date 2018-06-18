/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.titan.designer.consoles.TITANDebugConsole;

/**
 * @author Adam Delic
 * */
public class ASTLocationConsistencyVisitor extends ASTVisitor {
	IDocument document;
	boolean isTtcn;

	public ASTLocationConsistencyVisitor(final IDocument document, final boolean isTtcn) {
		this.document = document;
		this.isTtcn = isTtcn;
	}

	@Override
	/** {@inheritDoc} */
	public int visit(final IVisitableNode node) {
		if (node instanceof Identifier) {
			final Identifier id = (Identifier)node;
			final String name = isTtcn ? id.getTtcnName() : id.getAsnName();
			if (isTtcn && "anytype".equals(name)) {
				// anytype hack in ttcn-3
				return V_CONTINUE;
			}

			final Location loc = id.getLocation();
			final int offset = loc.getOffset();
			final int length = loc.getEndOffset()-loc.getOffset();
			try {
				final String strAtLoc = document.get(offset, length);
				if (!strAtLoc.equals(name)) {
					TITANDebugConsole.println("AST<->document inconsistency: id=["+name+"] at offset,length="+
							offset+","+length+" doc.content=["+strAtLoc+"]");

				}
			} catch (BadLocationException e) {
				TITANDebugConsole.println("AST<->document inconsistency: id=["+name+"] at offset,length="+
						offset+","+length+" BadLocationException: "+e.getMessage());
			}
		}
		return V_CONTINUE;
	}
}
