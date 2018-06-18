/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.common.parsers.cfg.indices;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNodeImpl;

/**
 * Stores temporary config editor data of the define section
 * @author Kristof Szabados
 * @author Arpad Lovassy
 */
public final class DefineSectionHandler extends ConfigSectionHandlerBase {

	public static class Definition {
		/** definition rule */
		private ParseTree mRoot = null;
		private ParseTree mDefinitionName = null;
		private ParseTree mDefinitionValue = null;

		public ParseTree getRoot() {
			return mRoot;
		}

		public void setRoot(final ParseTree aRoot) {
			this.mRoot = aRoot;
		}

		public ParseTree getDefinitionName() {
			return mDefinitionName;
		}

		public void setDefinitionName(final ParseTree aDefinitionName) {
			this.mDefinitionName = aDefinitionName;
		}

		public void setDefinitionName(final Token aDefinitionName) {
			this.mDefinitionName = new TerminalNodeImpl( aDefinitionName );
		}

		public ParseTree getDefinitionValue() {
			return mDefinitionValue;
		}

		public void setDefinitionValue(final ParseTree aDefinitionValue) {
			this.mDefinitionValue = aDefinitionValue;
		}
	}

	private List<Definition> definitions = new ArrayList<Definition>();

	public List<Definition> getDefinitions() {
		return definitions;
	}

	public void setDefinitions(final List<Definition> definitions) {
		this.definitions = definitions;
	}
}
