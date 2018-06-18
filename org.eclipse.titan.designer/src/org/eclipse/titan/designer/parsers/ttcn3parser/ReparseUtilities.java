/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.parsers.ttcn3parser;

import java.util.ArrayList;
import java.util.List;

/**
 * This class serves the purpose to create frequently used list of token types.
 *
 * @author Kristof Szabados
 * */
public final class ReparseUtilities {

	private ReparseUtilities() {
		// Hide constructor
	}

	/**
	 * @return all token types that are valid for reparsing (EOF is not)
	 * */
	public static List<Integer> getAllValidTokenTypes() {
		List<Integer> result = new ArrayList<Integer>();

		for (int i = Ttcn3Lexer.EOF + 1; i < Ttcn3Lexer.LEXERPLACEHOLDER; i++) {
			result.add(Integer.valueOf(i));
		}

		return result;
	}
}
