/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors;

import org.eclipse.jface.text.rules.IWhitespaceDetector;

/**
 * @author Kristof Szabados
 * */
public final class WhiteSpaceDetector implements IWhitespaceDetector {
	@Override
	public boolean isWhitespace(final char c) {
		switch (c) {
		case ' ':
		case '\t':
		case '\n':
		case '\r':
		case 11:
		case 12:
			return true;
		default:
			return false;
		}
	}
}
