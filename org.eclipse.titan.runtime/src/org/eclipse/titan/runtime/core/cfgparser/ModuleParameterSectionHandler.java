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
public final class ModuleParameterSectionHandler {

	public static class ModuleParameter {
		private String moduleName = null;

		/**
		 * Separator between module and parameter names,
		 * it is "." if module name is NOT empty,
		 * "" if module name is empty
		 */
		private String separator = null;

		private String parameterName = null;
		private String value = null;

		public String getModuleName() {
			return moduleName;
		}

		public void setModuleName(final String moduleName) {
			this.moduleName = moduleName;
		}

		public String getSeparator() {
			return separator;
		}

		public void setSeparator( final String aSeparator ) {
			this.separator = aSeparator;
		}

		public String getParameterName() {
			return parameterName;
		}

		public void setParameterName(final String parameterName) {
			this.parameterName = parameterName;
		}

		public String getValue() {
			return value;
		}

		public void setValue(final String value) {
			this.value = value;
		}
	}

	private List<ModuleParameter> moduleParameters = new ArrayList<ModuleParameter>();

	public List<ModuleParameter> getModuleParameters() {
		return moduleParameters;
	}

	public void setModuleParameters(final List<ModuleParameter> moduleParameters) {
		this.moduleParameters = moduleParameters;
	}
}
