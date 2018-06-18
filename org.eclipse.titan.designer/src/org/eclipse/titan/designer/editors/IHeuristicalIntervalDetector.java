/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.titan.common.parsers.Interval;

/**
 * @author Kristof Szabados
 * */
public interface IHeuristicalIntervalDetector {
	/**
	 * The method determines if the given offset is within a string in the
	 * document.
	 *
	 * @param document
	 *                the document being parsed
	 * @param offset
	 *                the position where parsing starts from
	 * @param enclosingInterval
	 *                an interval, which encloses the offset.
	 * @return Whether offset is within a comment.
	 * @exception BadLocationException
	 *                    if the offset is invalid in this document
	 */
	boolean isWithinString(final StringBuilder document, final int offset, final Interval enclosingInterval) throws BadLocationException;
}
