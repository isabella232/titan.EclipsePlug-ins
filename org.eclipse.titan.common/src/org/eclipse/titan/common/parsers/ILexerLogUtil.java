/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.common.parsers;

/**
 * USED FOR LOGGING PURPOSES
 * Interface for token name resolving
 * @see TokenNameResolver
 * @author Arpad Lovassy
 */
public interface ILexerLogUtil {

	/**
	 * @param aIndex token type index
	 * @return resolved token name
	 */
	public String getTokenName( int aIndex );
}
