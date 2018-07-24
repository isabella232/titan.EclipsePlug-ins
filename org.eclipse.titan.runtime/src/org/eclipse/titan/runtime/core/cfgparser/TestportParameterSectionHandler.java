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

import org.antlr.v4.runtime.tree.ParseTree;

/**
 * @author Kristof Szabados
 * @author Arpad Lovassy
 */
public final class TestportParameterSectionHandler {

	public static class TestportParameter {
		private ParseTree componentName = null;
		private ParseTree testportName = null;
		private ParseTree parameterName = null;
		private ParseTree value = null;

		public ParseTree getComponentName() {
			return componentName;
		}

		public void setComponentName(final ParseTree componentName) {
			this.componentName = componentName;
		}

		public ParseTree getTestportName() {
			return testportName;
		}

		public void setTestportName(final ParseTree testportName) {
			this.testportName = testportName;
		}

		public ParseTree getParameterName() {
			return parameterName;
		}

		public void setParameterName(final ParseTree parameterName) {
			this.parameterName = parameterName;
		}

		public ParseTree getValue() {
			return value;
		}

		public void setValue(final ParseTree value) {
			this.value = value;
		}
	}

	private List<TestportParameter> testportParameters = new ArrayList<TestportParameter>();

	public List<TestportParameter> getTestportParameters() {
		return testportParameters;
	}

	public void setTestportParameters(final List<TestportParameter> testportParameters) {
		this.testportParameters = testportParameters;
	}
}
