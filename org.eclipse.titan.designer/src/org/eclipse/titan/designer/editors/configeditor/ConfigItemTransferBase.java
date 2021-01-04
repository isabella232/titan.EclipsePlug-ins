/******************************************************************************
 * Copyright (c) 2000-2021 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors.configeditor;

import org.antlr.v4.runtime.tree.ParseTree;
import org.eclipse.swt.dnd.ByteArrayTransfer;

/**
 * Base class for ...ItemTransfer classes,
 * they are responsible for handling drag and drop of config editor tab items.
 * @author Arpad Lovassy
 */
public abstract class ConfigItemTransferBase extends ByteArrayTransfer {

	/**
	 * Converts parse tree to its string representation. Used by javaToNative().
	 * @param aRoot parse tree root to convert
	 * @return the converted string representation of the parse tree
	 */
	protected String convertToString( final ParseTree aRoot ) {
		//TODO: get tokenStream, and use this instead
		/*
		final StringBuilder sb = new StringBuilder();
		// it prints also the hidden token before
		ConfigTreeNodeUtilities.print( aRoot, getTokenStream(), sb, null );
		return sb.toString();
		/*/
		return aRoot.getText();
		//*/
	}
}
