/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core.cfgparser;

import java.io.File;

import org.antlr.v4.runtime.Token;

/**
 * The Location class represents a location in configuration files.  It was
 * stolen from "org.eclipse.titan.designer.AST.Location".  It stores only
 * location information.
 *
 * @author eferkov
 * @author Arpad Lovassy
 */
public final class CfgLocation {
	private File file;
	private int line;
	private int offset;
	private int endOffset;

	/**
	 * Constructor for ANTLR v4 tokens
	 * @param aFile the parsed file
	 * @param aStartToken the 1st token, its line and start position will be used for the location
	 *                  NOTE: start position is the column index of the tokens 1st character.
	 *                        Column index starts with 0.
	 * @param aEndToken the last token, its end position will be used for the location.
	 *                  NOTE: end position is the column index after the token's last character.
	 */
	public CfgLocation( final File aFile, final Token aStartToken, final Token aEndToken ) {
		setLocation( aFile, aStartToken.getLine(), aStartToken.getStartIndex(),
				aEndToken.getStopIndex() + 1 );
	}

	private final void setLocation(final File file, final int line, final int offset, final int endOffset) {
		this.file = file;
		this.line = line;
		this.offset = offset;
		this.endOffset = endOffset;
	}

	public File getFile() {
		return file;
	}

	public int getLine() {
		return line;
	}

	public int getOffset() {
		return offset;
	}

	public int getEndOffset() {
		return endOffset;
	}

	public String toString() {
		return "{ " + file + ", "  + line + ", " + offset + ", " + endOffset + " }";
	}
}
