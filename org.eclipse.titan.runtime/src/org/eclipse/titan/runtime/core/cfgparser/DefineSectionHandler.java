/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core.cfgparser;

import java.util.ArrayList;
import java.util.List;

/**
 * Stores temporary config editor data of the define section
 * @author Kristof Szabados
 * @author Arpad Lovassy
 */
public final class DefineSectionHandler {

	public static class Definition {
		/** definition rule */
		private String definitionName = null;
		private String definitionValue = null;

		public String getDefinitionName() {
			return definitionName;
		}

		public void setDefinitionName(final String aDefinitionName) {
			this.definitionName = aDefinitionName;
		}

		public String getDefinitionValue() {
			return definitionValue;
		}

		public void setDefinitionValue(final String aDefinitionValue) {
			this.definitionValue = aDefinitionValue;
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
