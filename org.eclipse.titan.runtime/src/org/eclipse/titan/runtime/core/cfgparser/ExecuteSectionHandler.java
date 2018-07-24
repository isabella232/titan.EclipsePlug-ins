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
 * @author Kristof Szabados
 * @author Arpad Lovassy
 */
public final class ExecuteSectionHandler {

	public static class ExecuteItem {
		private String moduleName = null;
		private String testcaseName = null;

		public String getModuleName() {
			return moduleName;
		}

		public void setModuleName(final String moduleName) {
			this.moduleName = moduleName;
		}

		public String getTestcaseName() {
			return testcaseName;
		}

		public void setTestcaseName(final String testcaseName) {
			this.testcaseName = testcaseName;
		}
	}

	private List<ExecuteItem> executeitems = new ArrayList<ExecuteItem>();

	public List<ExecuteItem> getExecuteitems() {
		return executeitems;
	}

	public void setExecuteitems(final List<ExecuteItem> executeitems) {
		this.executeitems = executeitems;
	}


}
